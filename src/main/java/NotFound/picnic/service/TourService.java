package NotFound.picnic.service;

import NotFound.picnic.domain.*;
import NotFound.picnic.dto.LocationGetDto;
import NotFound.picnic.dto.ScheduleGetDto;
import NotFound.picnic.repository.*;
import NotFound.picnic.domain.City;
import NotFound.picnic.dto.CityGetDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TourService {
    private final LocationRepository locationRepository;
    private final LocationImageRepostiory locationImageRepostiory;
    private final ScheduleRepository scheduleRepository;
    private final MemberRepository memberRepository;
    private final DiaryRepository diaryRepository;
    private final ImageRepository imageRepository;
    private final CityRepository cityRepository;
    private final S3Upload s3Upload;


    public List<LocationGetDto> GetLocations(String city, String keyword) throws UnsupportedEncodingException {

        Optional<List<Location>> locations;


        if (keyword == null)
            locations = locationRepository.findAllByCity(city);
        else
            locations = locationRepository.findByCityAndKeyword(city, keyword);

        return locations.map(locationList -> locationList.stream()
                .map(location -> {

                    Optional<LocationImage> image = locationImageRepostiory.findTopByLocation(location);
                    String imageUrl = null;
                    if (image.isPresent())
                        imageUrl = s3Upload.getImageUrl(image.get().getImageUrl());

                    return LocationGetDto.builder()
                            .locationId(location.getLocationId())
                            .name(location.getName())
                            .address(location.getAddress())
                            .latitude(location.getLatitude())
                            .longitude(location.getLongitude())
                            .imageUrl(imageUrl)
                            .build();
                })
                .toList()).orElse(null);
    }

    public List<ScheduleGetDto> GetSchedules(String city, String keyword) {
        Optional<List<Location>> locations = locationRepository.findByCityAndKeyword(city, keyword);
        if (locations.isPresent()) {
            List<Location> locationList = locations.get();

            List<Long> locationIds = locations.get().stream()
                    .map(Location::getLocationId)
                    .toList();

            Optional<List<Schedule>> schedules = scheduleRepository.findDistinctSchedulesByLocations(locationIds);

            if (schedules.isPresent()) {
                return schedules.get().stream()
                        .map(schedule -> {
                            Member member = memberRepository.findById(schedule.getMember().getMemberId()).orElseThrow();

                            Optional<List<Diary>> diaries = diaryRepository.findAllBySchedule(schedule);
                            String imageUrl = null;

                            if (diaries.isPresent()) {
                                Optional<String> imageUrlOptional = diaries.get().stream()
                                        .map(imageRepository::findTopByDiary)
                                        .filter(Optional::isPresent)
                                        .map(Optional::get)
                                        .map(Image::getImageUrl)
                                        .findFirst();

                                if (imageUrlOptional.isPresent())
                                    imageUrl = imageUrlOptional.get();
                            }


                            return ScheduleGetDto.builder()
                                    .scheduleId(schedule.getScheduleId())
                                    .name(schedule.getName())
                                    .startDate(schedule.getStartDate())
                                    .endDate(schedule.getEndDate())
                                    .username(member.getName())
                                    .imageUrl(imageUrl)
                                    .build();
                        })
                        .toList();
            }
        }
        return null;
    }

    public List<CityGetDto> GetCities(){
        List<City> cities = cityRepository.findAll();
        return cities.stream()
                .map(city -> CityGetDto.builder()
                        .cityName(city.getName())
                        .imageUrl(city.getImageUrl())
                        .build())
                .collect(Collectors.toList());
    }

    public String DuplicateSchedule(Long scheduleId, Principal principal){

        // User validate
        Optional<Member> optionalMember = memberRepository.findMemberByEmail(principal.getName());

        if (optionalMember.isEmpty()) {
            throw new UsernameNotFoundException("유저가 존재하지 않습니다.");
        }
        Member member = optionalMember.get();
        // schedule Id validate
        Schedule scheduleOld = scheduleRepository.findById(scheduleId).orElseThrow();

        // Duplicate doing

        Schedule scheduleNew = Schedule.builder()
                .name(scheduleOld.getName())
                .location(scheduleOld.getLocation())
                .startDate(scheduleOld.getStartDate())
                .endDate(scheduleOld.getEndDate())
                .member(member)
                .build();

        scheduleRepository.save(scheduleNew);

        return "일정 복제 완료";
    }

}
