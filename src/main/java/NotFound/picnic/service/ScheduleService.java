package NotFound.picnic.service;

import NotFound.picnic.domain.*;
import NotFound.picnic.dto.PlaceCreateDto;
import NotFound.picnic.dto.DiaryCreateDto;
import NotFound.picnic.dto.ScheduleCreateDto;
import NotFound.picnic.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    // 여행 일정 생성
    public String createSchedule(ScheduleCreateDto scheduleCreateDto, Principal principal) {
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
                .build();

        scheduleRepository.save(schedule);

        return "저장 완료";
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

}
