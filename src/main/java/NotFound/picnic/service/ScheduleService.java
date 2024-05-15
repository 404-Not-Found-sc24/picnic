package NotFound.picnic.service;

import NotFound.picnic.domain.*;
import NotFound.picnic.dto.*;
import NotFound.picnic.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleService {
    private final MemberRepository memberRepository;
    private final ScheduleRepository scheduleRepository;
    private final PlaceRepository placeRepository;
    private final LocationRepository locationRepository;
    private final S3Upload s3Upload;
    private final ImageRepository imageRepository;
    private final DiaryRepository diaryRepository;
    private final LocationImageRepostiory locationImageRepostiory;

    // 여행 일정 생성
    public Long createSchedule(ScheduleCreateDto scheduleCreateDto, Principal principal) {
        Optional<Member> optionalMember = memberRepository.findMemberByEmail(principal.getName());

        if (optionalMember.isEmpty()) {
            throw new UsernameNotFoundException("유저가 존재하지 않습니다.");
        }
        Member member = optionalMember.get();

        log.info("name"+ scheduleCreateDto.getName());

        Schedule schedule = Schedule.builder()
                .name(scheduleCreateDto.getName())
                .location(scheduleCreateDto.getLocation())
                .startDate(scheduleCreateDto.getStartDate())
                .endDate(scheduleCreateDto.getEndDate())
                .member(member)
                .share(scheduleCreateDto.isShare())
                .build();

        scheduleRepository.save(schedule);

        return schedule.getScheduleId();
    }

    // 일정에 장소 추가
    public String createLocations(Long scheduleId, List<PlaceCreateDto> placeCreateDtoList, Principal principal) {
        // 이미 추가된 장소가 있는지 확인
        Schedule schedule = scheduleRepository.findById(scheduleId).orElseThrow();

        if (placeRepository.existsBySchedule(schedule)) {
            // 이미 추가된 장소가 있으면 장소 일자랑 시간만 변경
            List<Place> oldPlaces = placeRepository.findBySchedule(schedule);
            List<Place> placesToRemove = new ArrayList<>();

            oldPlaces.stream()
                    .filter(oldPlace -> placeCreateDtoList.stream()
                            .anyMatch(placeCreateDto -> oldPlace.getLocation().getLocationId().equals(placeCreateDto.getLocationId())))
                    .forEach(oldPlace -> {
                        PlaceCreateDto matchingDto = placeCreateDtoList.stream()
                                .filter(placeCreateDto -> oldPlace.getLocation().getLocationId().equals(placeCreateDto.getLocationId()))
                                .findFirst()
                                .orElseThrow(() -> new RuntimeException("Matching dto not found")); // 일치하는 dto가 없는 경우 예외 처리
                        // Place의 값을 변경
                        oldPlace.setDate(matchingDto.getDate());
                        oldPlace.setTime(matchingDto.getTime());
                        placeRepository.save(oldPlace);
                        // scheduleCreateDtoList에서 해당 객체를 제거
                        placeCreateDtoList.remove(matchingDto);
                        placesToRemove.add(oldPlace);
                    });

            oldPlaces.removeAll(placesToRemove);
            placeRepository.deleteAll(oldPlaces);
        }

        // 추가된 장소 제외하고는 새로 추가
        List<Place> places = placeCreateDtoList.stream()
                .map(placeCreateDto -> Place.builder()
                        .location(locationRepository.findById(placeCreateDto.getLocationId()).orElseThrow(() -> new RuntimeException("일치하는 장소가 없습니다.")))
                                .date(placeCreateDto.getDate())
                        .time(placeCreateDto.getTime())
                        .schedule(schedule)
                        .build())
                .toList();
        placeRepository.saveAll(places);

        return "장소 추가 완료";
    }

    public List<SchedulePlaceDiaryGetDto> getSchedulePlaceDiary(Long scheduleId, Principal principal) {
        // 존재하지 않는 scheduleId인 경우 예외 발생
        Schedule schedule = scheduleRepository.findById(scheduleId).orElseThrow();

        // 존재하는 schedule에 속하는 place list 조회
        List<Place> places = placeRepository.findBySchedule(schedule);

        // SchedulePlaceDiaryGetDto로 매핑
        return places.stream().flatMap(place -> {
            // 각 place에 속한 diary 조회
            List<Diary> diaryList = diaryRepository.findAllByPlace_PlaceId(place.getPlaceId());

            // diary가 없으면 빈 Diary 객체 생성
            if (diaryList.isEmpty()) {
                diaryList.add(new Diary()); // 빈 Diary 객체 추가
            }

            // Stream<Diary>로 변환
            return diaryList.stream().map(diary -> {
                // SchedulePlaceDiaryGetDto 빌더 생성
                SchedulePlaceDiaryGetDto.SchedulePlaceDiaryGetDtoBuilder builder = SchedulePlaceDiaryGetDto.builder()
                        .placeID(place.getPlaceId())
                        .locationId(place.getLocation().getLocationId())
                        .locationName(place.getLocation().getName())
                        .date(place.getDate())
                        .time(place.getTime())
                        .diaryId(diary.getDiaryId())
                        .title(diary.getTitle())
                        .content(diary.getContent());

                // diary에 매칭되는 이미지 조회
                Optional<Image> optionalImage = imageRepository.findTopImageUrlByDiary_DiaryId(diary.getDiaryId());
                // 이미지가 존재하면 imageUrl 설정
                String imageUrl = optionalImage.map(Image::getImageUrl).orElse(null);
                optionalImage.ifPresentOrElse(
                        image -> builder.imageUrl(image.getImageUrl()),
                        () -> {} // 값이 없는 경우 아무 작업도 수행하지 않음
                );

                // SchedulePlaceDiaryGetDto 생성
                return builder.build();
            });
        }).collect(Collectors.toList());
    }


    // 여행 일기 생성
    public String createDiary(Long placeId, DiaryCreateDto diaryCreateDto) throws IOException {

        Place place = placeRepository.findById(placeId).orElseThrow(() -> new NullPointerException("장소 정보가 없습니다."));

        if (diaryRepository.existsByPlace(place))
            throw new IOException("이미 일기를 작성하였습니다.");

        Diary diary = Diary.builder()
                .place(place)
                .title(diaryCreateDto.getTitle())
                .content(diaryCreateDto.getContent())
                .weather(diaryCreateDto.getWeather())
                .build();

        diaryRepository.save(diary);

        List<MultipartFile> images = diaryCreateDto.getImages();
        if (images != null) {
            images.forEach(image -> {
                try {
                    String url = s3Upload.uploadFiles(image, "diary");
                    Image img = Image.builder()
                            .diary(diary)
                            .imageUrl(url)
                            .build();

                    imageRepository.save(img);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            });

        }

        return "일기 저장 완료";
    }

    // 여행 일정 보기 in MyPage
    public List<List<PlaceGetDto>> getPlaces (Long scheduleId) {

        Optional<List<Place>> placeList = placeRepository.findAllBySchedule_ScheduleId(scheduleId);

        List<List<PlaceGetDto>> placeGetDtoList = new ArrayList<>();

        if (placeList.isPresent()) {
            List<String> uniqueDates = placeList.get().stream()
                    .map(Place::getDate)
                    .distinct()
                    .sorted()
                    .toList();

            for (String date : uniqueDates) {
                List<PlaceGetDto> placesWithSameDate = placeList.get().stream()
                        .filter(place -> place.getDate().equals(date))
                        .map(place -> {
                            Location location = place.getLocation();
                            Optional<LocationImage> image = locationImageRepostiory.findTopByLocation(location);
                            String imageUrl = null;
                            if (image.isPresent()) imageUrl = image.get().getImageUrl();
                            return PlaceGetDto.builder()
                                    .placeId(place.getPlaceId())
                                    .locationId(location.getLocationId())
                                    .locationName(location.getName())
                                    .date(place.getDate())
                                    .time(place.getTime())
                                    .address(location.getAddress())
                                    .latitude(location.getLatitude())
                                    .longitude(location.getLongitude())
                                    .imageUrl(imageUrl)
                                    .build();
                        })
                        .sorted(Comparator.comparing(PlaceGetDto::getTime))
                        .toList();
                placeGetDtoList.add(placesWithSameDate);
            }
        }

        return placeGetDtoList;
    }

    public List<MyScheduleGetDto> GetSchedulesInMyPage (Principal principal) {
        Member member = memberRepository.findMemberByEmail(principal.getName()).orElseThrow();

        List<Schedule> scheduleList = scheduleRepository.findAllByMemberOrderByStartDateDesc(member);

        return scheduleList.stream().map(schedule -> {
            Optional<List<Diary>> diaries = diaryRepository.findAllBySchedule(schedule);
            Optional<Image> image = diaries.flatMap(diaryList ->
                    diaryList.stream()
                            .map(imageRepository::findTopByDiary)
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .findFirst()
            );

            String imageUrl = null;
            if (image.isPresent())
                imageUrl = image.get().getImageUrl();

            return MyScheduleGetDto.builder()
                    .scheduleId(schedule.getScheduleId())
                    .name(schedule.getName())
                    .startDate(schedule.getStartDate())
                    .endDate(schedule.getEndDate())
                    .share(schedule.isShare())
                    .location(schedule.getLocation())
                    .imageUrl(imageUrl)
                    .build();
        }).collect(Collectors.toList());
    }

}
