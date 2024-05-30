package NotFound.picnic.service;

import NotFound.picnic.domain.*;
import NotFound.picnic.dto.schedule.*;
import NotFound.picnic.exception.CustomException;
import NotFound.picnic.exception.ErrorCode;
import NotFound.picnic.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
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
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
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
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new CustomException(ErrorCode.SCHEDULE_NOT_FOUND));

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
                                .orElseThrow(() -> new CustomException(ErrorCode.SERVER_ERROR)); // 일치하는 dto가 없는 경우 예외 처리
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
                        .location(locationRepository.findById(placeCreateDto.getLocationId())
                                .orElseThrow(() -> new CustomException(ErrorCode.LOCATION_NOT_FOUND)))
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
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new CustomException(ErrorCode.SCHEDULE_NOT_FOUND));

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
                        .placeId(place.getPlaceId())
                        .locationId(place.getLocation().getLocationId())
                        .locationName(place.getLocation().getName())
                        .date(place.getDate())
                        .time(place.getTime())
                        .latitude(place.getLocation().getLatitude())
                        .longitude(place.getLocation().getLongitude())
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
    public String createDiary(Long placeId, DiaryCreateDto diaryCreateDto) {

        Place place = placeRepository.findById(placeId).orElseThrow(() -> new CustomException(ErrorCode.PLACE_NOT_FOUND));

        if (diaryRepository.existsByPlace(place))
            throw new CustomException(ErrorCode.DUPLICATED_DIARY);

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
                } catch (Exception e) {
                    throw new CustomException(ErrorCode.IMAGE_UPLOAD_FAILED);
                }

            });

        }

        return "일기 저장 완료";
    }

    @Transactional
    public String DeleteDiary(Long diaryId, Principal principal) throws CustomException {
        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new CustomException(ErrorCode.DIARY_NOT_FOUND));
        Member member = memberRepository.findMemberByEmail(principal.getName())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if(!placeRepository.findByDiary_DiaryId(diaryId).getSchedule().getMember().equals(member)){
            throw new CustomException(ErrorCode.NO_AUTHORITY);
        }

        diaryRepository.delete(diary);

        return "일기 삭제 완료";
    }

    // 여행 일정 보기 in MyPage
    public List<List<PlaceGetDto>> getPlaces (Long scheduleId) {

        Optional<List<Place>> placeList = placeRepository.findAllBySchedule_ScheduleId(scheduleId);

        List<List<PlaceGetDto>> placeGetDtoList = new ArrayList<>();

        if (placeList.isPresent()) {
            List<Place> sortedPlaces = placeList.get().stream()
                    .sorted(Comparator.comparing(Place::getDate).thenComparing(Place::getTime))
                    .toList();

            List<String> uniqueDates = sortedPlaces.stream()
                    .map(Place::getDate)
                    .distinct()
                    .toList();

            for (String date : uniqueDates) {
                List<PlaceGetDto> placesWithSameDate = sortedPlaces.stream()
                        .filter(place -> place.getDate().equals(date))
                        .map(place -> {
                            Location location = place.getLocation();
                            Optional<LocationImage> image = locationImageRepostiory.findTopByLocation(location);
                            String imageUrl = null;
                            if (image.isPresent()) imageUrl = image.get().getImageUrl();
                            return PlaceGetDto.builder()
                                    .placeId(place.getPlaceId())
                                    .locationId(location.getLocationId())
                                    .name(location.getName())
                                    .address(location.getAddress())
                                    .latitude(location.getLatitude())
                                    .longitude(location.getLongitude())
                                    .imageUrl(imageUrl)
                                    .build();
                        })
                        .toList();
                placeGetDtoList.add(placesWithSameDate);
            }
        }

        return placeGetDtoList;
    }

    public MyScheduleListDto GetSchedulesInMyPage (Principal principal) {
        Member member = memberRepository.findMemberByEmail(principal.getName())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        List<Schedule> scheduleList = scheduleRepository.findAllByMember(member);

        List<MyScheduleGetDto> beforeTravel = new ArrayList<>();
        List<MyScheduleGetDto> traveling = new ArrayList<>();
        List<MyScheduleGetDto> afterTravel = new ArrayList<>();

        LocalDate now = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        scheduleList.forEach(schedule -> {
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

            MyScheduleGetDto dto = MyScheduleGetDto.builder()
                    .scheduleId(schedule.getScheduleId())
                    .name(schedule.getName())
                    .startDate(schedule.getStartDate())
                    .endDate(schedule.getEndDate())
                    .share(schedule.isShare())
                    .location(schedule.getLocation())
                    .imageUrl(imageUrl)
                    .build();

            ZoneId zoneId = ZoneId.of("Asia/Seoul");
            LocalDate startDate = LocalDate.parse(schedule.getStartDate(), formatter.withZone(zoneId));
            LocalDate endDate = LocalDate.parse(schedule.getEndDate(), formatter.withZone(zoneId));
            LocalDate today = LocalDate.now(zoneId);


            if (startDate.isAfter(today)) {
                beforeTravel.add(dto);
            } else if (!startDate.isAfter(today) && !endDate.isBefore(today)) {
                traveling.add(dto);
            } else if (endDate.isBefore(today)) {
                afterTravel.add(dto);
            }
        });

        Comparator<MyScheduleGetDto> dateComparator = Comparator.comparing(dto -> LocalDate.parse(dto.getStartDate(), formatter));

        beforeTravel.sort(dateComparator); // 거꾸로 정렬
        traveling.sort(dateComparator); // 기본 날짜 순으로 정렬
        afterTravel.sort(dateComparator.reversed());

        return MyScheduleListDto.builder()
                .beforeTravel(beforeTravel)
                .traveling(traveling)
                .afterTravel(afterTravel)
                .build();
    }

    @Transactional
    public String deleteSchedule (Long scheduleId, Principal principal) throws IOException {
        Member member = memberRepository.findMemberByEmail(principal.getName())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new CustomException(ErrorCode.SCHEDULE_NOT_FOUND));

        if (schedule.getMember() != member)
            throw new CustomException(ErrorCode.NO_AUTHORITY);

        scheduleRepository.delete(schedule);
        return "일정 삭제 완료";
    }

    public String DeletePlace(Long placeId, Principal principal) throws IOException{
        Member member = memberRepository.findMemberByEmail(principal.getName()).orElseThrow();
        Place place = placeRepository.findById(placeId).orElseThrow();

        if(member != place.getSchedule().getMember()){
            throw  new IOException();
        }

        placeRepository.delete(place);
        return "장소 삭제 완료";
    }

    public List<MyScheduleGetDto> GetSchedules (Principal principal) {
        Member member = memberRepository.findMemberByEmail(principal.getName())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

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

    public String UpdateSchedule (ScheduleCreateDto scheduleCreateDto, Long scheduleId) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new CustomException(ErrorCode.SCHEDULE_NOT_FOUND));

        if (scheduleCreateDto.getName() != null) schedule.setName(scheduleCreateDto.getName());
        if (scheduleCreateDto.getLocation() != null) schedule.setLocation(scheduleCreateDto.getLocation());
        if (scheduleCreateDto.getStartDate() != null) schedule.setStartDate(scheduleCreateDto.getStartDate());
        if (scheduleCreateDto.getEndDate() != null) schedule.setEndDate(scheduleCreateDto.getEndDate());

        scheduleRepository.save(schedule);

        return "수정 완료";
    }

    public String ChangeSharing(Long scheduleId, Principal principal) throws CustomException{
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new CustomException(ErrorCode.SCHEDULE_NOT_FOUND));
        Member member = memberRepository.findMemberByEmail(principal.getName())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if(!schedule.getMember().equals(member)){
            throw new CustomException(ErrorCode.NO_AUTHORITY);
        }

        schedule.setShare(!schedule.isShare());
        scheduleRepository.save(schedule);

        if(schedule.isShare()){
            return "일정 공개 처리 완료";
        }
        return "일정 비공개 처리 완료";
    }
}
