package NotFound.picnic.service;

import NotFound.picnic.domain.*;
import NotFound.picnic.dto.*;
import NotFound.picnic.repository.*;
import NotFound.picnic.domain.City;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TourService {
    private final LocationRepository locationRepository;
    private final LocationImageRepostiory locationImageRepository;
    private final ScheduleRepository scheduleRepository;
    private final MemberRepository memberRepository;
    private final DiaryRepository diaryRepository;
    private final ImageRepository imageRepository;
    private final CityRepository cityRepository;
    private final AccommodationRepository accommodationRepository;
    private final CultureRepository cultureRepository;
    private final FestivalRepository festivalRepository;
    private final LeisureRepository leisureRepository;
    private final RestaurantRepository restaurantRepository;
    private final ShoppingRepository shoppingRepository;
    private final TourRepository tourRepository;
    private final PlaceRepository placeRepository;
    private final ApprovalRepository approvalRepository;
    private final ApprovalImageRepository approvalImageRepository;
    private final S3Upload s3Upload;


    public List<LocationGetDto> GetLocations(String city, String keyword, int lastIdx) throws UnsupportedEncodingException {
        Optional<List<Location>> locations;
        if (city != null) {
            locations = locationRepository.findByCityAndKeyword(city, keyword, lastIdx);
        }
        else {
            locations = locationRepository.findByKeyword(keyword, lastIdx);
        }

        return locations.map(locationList -> locationList.stream()
                .map(location -> {

                    Optional<LocationImage> image = locationImageRepository.findTopByLocation(location);
                    String imageUrl = null;
                    if (image.isPresent())
                        imageUrl = image.get().getImageUrl();

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
        // 해당 도시로 여행 가는 일정들 가져오기
        List<Schedule> scheduleList = scheduleRepository.findAllByLocationContainingAndShare(city, true);

        // 그 일정들에서 location들 확인하면서 해당 키워드와 동일한지 확인
        if (!Objects.equals(keyword, "")) {
            scheduleList = scheduleList.stream()
                    .filter(schedule -> {
                        List<Location> locations = locationRepository.findLocationsBySchedule(schedule);
                        return locations.stream()
                                .anyMatch(location -> location.getName().contains(keyword) || location.getAddress().contains(keyword));
                    })
                    .toList();
        }

        return FindSchedules(scheduleList);
    }

    public List<CityGetDto> GetCities(String keyword, String keyword2) {
        List<City> cities = null;
        if (Objects.equals(keyword2, ""))
            cities = cityRepository.findAllByNameContaining(keyword);
        else
            cities = cityRepository.findAllByNameContainingOrNameContaining(keyword, keyword2);

        return cities.stream()
                .map(city -> CityGetDto.builder()
                        .cityName(city.getName())
                        .cityDetail(city.getDetail())
                        .imageUrl(city.getImageUrl())
                        .build())
                .collect(Collectors.toList());
    }

    public Long DuplicateSchedule(Long scheduleId, ScheduleDuplicateDto scheduleDuplicateDto, Principal principal) {
        // User validate
        Optional<Member> optionalMember = memberRepository.findMemberByEmail(principal.getName());

        if (optionalMember.isEmpty()) {
            throw new UsernameNotFoundException("유저가 존재하지 않습니다.");
        }
        Member member = optionalMember.get();
        // schedule Id validate
        Schedule scheduleOld = scheduleRepository.findById(scheduleId).orElseThrow();

        // Duplicate schedule
        Schedule scheduleNew = Schedule.builder()
                .name(scheduleDuplicateDto.getName())
                .location(scheduleOld.getLocation())
                .startDate(scheduleDuplicateDto.getStartDate())
                .endDate(scheduleDuplicateDto.getEndDate())
                .member(member)
                .build();

        Schedule savedSchedule = scheduleRepository.save(scheduleNew);
        //Duplicate place
        List<Place> places = placeRepository.findBySchedule(scheduleOld);

        for (Place place : places) {
            Place place_new = Place.builder()
                    .date(place.getDate())
                    .time(place.getTime())
                    .location(place.getLocation())
                    .schedule(savedSchedule)
                    .build();
            Place test = placeRepository.save(place_new);
        }

        return savedSchedule.getScheduleId();
    }

    public LocationDetailDto GetLocationDetail(Long locationId) {

        //location check
        Location location = locationRepository.findByLocationId(locationId).orElseThrow();
        //division check
        String division = location.getDivision();
        //location dto create
        LocationDetailDto locationDetail = LocationDetailDto.builder()
                .name(location.getName())
                .address(location.getAddress())
                .detail(location.getDetail())
                .latitude(location.getLatitude())
                .longitude(location.getLongitude())
                .division(location.getDivision())
                .phone(location.getPhone())
                .build();

        //Accommodation
        if (division.contains("숙박")) {
            Accommodation accommodation = accommodationRepository.findByLocation_LocationId(locationId);
            locationDetail.setAccommodation(LocationDetailDto.Accommodation.builder()
                    .checkIn(accommodation.getCheckIn())
                    .checkOut(accommodation.getCheckOut())
                    .cook(accommodation.getCook())
                    .detail(accommodation.getDetail())
                    .parking(accommodation.getParking())
                    .reservation(accommodation.getReservation())
                    .build());
        }
        //Culture
        else if (division.contains("문화시설")) {
            Culture culture = cultureRepository.findByLocation_LocationId(locationId);
            locationDetail.setCulture(LocationDetailDto.Culture.builder()
                    .babycar(culture.getBabycar())
                    .detail(culture.getDetail())
                    .discount(culture.getDiscount())
                    .fee(culture.getFee())
                    .offDate(culture.getOffDate())
                    .parking(culture.getParking())
                    .pet(culture.getPet())
                    .time(culture.getTime())
                    .build());
        }
        //Festival
        else if (division.contains("축제 공연 행사")) {
            Festival festival = festivalRepository.findByLocation_LocationId(locationId);
            locationDetail.setFestival(LocationDetailDto.Festival.builder()
                    .detail(festival.getDetail())
                    .startDate(festival.getStartDate())
                    .endDate(festival.getEndDate())
                    .fee(festival.getFee())
                    .time(festival.getTime())
                    .build());
        }
        //Leisure
        else if (division.contains("레포츠")) {
            Leisure leisure = leisureRepository.findByLocation_LocationId(locationId);
            locationDetail.setLeisure(LocationDetailDto.Leisure.builder()
                    .babycar(leisure.getBabycar())
                    .detail(leisure.getDetail())
                    .fee(leisure.getFee())
                    .openDate(leisure.getOpenDate())
                    .offDate(leisure.getOffDate())
                    .parking(leisure.getParking())
                    .pet(leisure.getPet())
                    .time(leisure.getTime())
                    .build());
        }
        //Restaurant
        else if (division.contains("음식점")) {
            Restaurant restaurant = restaurantRepository.findByLocation_LocationId(locationId);
            locationDetail.setRestaurant(LocationDetailDto.Restaurant.builder()
                    .dayOff(restaurant.getDayOff())
                    .mainMenu(restaurant.getMainMenu())
                    .menu(restaurant.getMenu())
                    .packaging(restaurant.getPackaging())
                    .build());
        }
        //Shopping
        else if (division.contains("쇼핑")) {
            Shopping shopping = shoppingRepository.findByLocation_LocationId(locationId);
            locationDetail.setShopping(LocationDetailDto.Shopping.builder()
                    .babycar(shopping.getBabycar())
                    .offDate(shopping.getOffDate())
                    .parking(shopping.getParking())
                    .pet(shopping.getPet())
                    .time(shopping.getTime())
                    .build());

        }
        //Tour
        else if (division.contains("관광지")) {
            Tour tour = tourRepository.findByLocation_LocationId(locationId);
            locationDetail.setTour(LocationDetailDto.Tour.builder()
                    .detail(tour.getDetail())
                    .offDate(tour.getOffDate())
                    .parking(tour.getParking())
                    .pet(tour.getPet())
                    .time(tour.getTime())
                    .build());
        }
        return locationDetail;
    }

    public String AddPlaceToSchedule(ScheduleAddPlaceDto scheduleAddPlaceDto, Principal principal) {

        //Validation
        Schedule schedule = scheduleRepository.findByScheduleId(scheduleAddPlaceDto.getScheduleId()).orElseThrow();
        Location location = locationRepository.findByLocationId(scheduleAddPlaceDto.getLocationId()).orElseThrow();

        //Place add
        Place place = Place.builder()
                .date(scheduleAddPlaceDto.getDate())
                .time(scheduleAddPlaceDto.getTime())
                .location(location)
                .schedule(schedule)
                .build();

        placeRepository.save(place);
        return "해당 스케쥴에 장소를 추가하였습니다";
    }

    public List<DiaryGetDto> GetDiaries(Long locationId) {
      
        Optional<List<Place>> placeList = placeRepository.findAllByLocation_LocationId(locationId);

        return placeList.map(places -> places.stream()
                .map(place -> {
                    Schedule schedule = scheduleRepository.findById(place.getSchedule().getScheduleId()).orElseThrow();
                    Optional<Diary> diary = diaryRepository.findByPlace(place);
                    if (diary.isPresent()) {
                        Optional<Image> image = imageRepository.findTopByDiary(diary.get());
                        String imageUrl = image.map(Image::getImageUrl).orElse(null);

                        return DiaryGetDto.builder()
                                .diaryId(diary.get().getDiaryId())
                                .placeId(place.getPlaceId())
                                .title(diary.get().getTitle())
                                .date(place.getDate())
                                .content(diary.get().getContent())
                                .imageUrl(imageUrl)
                                .build();
                    } else {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList())
        ).orElse(Collections.emptyList());
    }

    public List<ScheduleGetDto> GetSchedulesByLocationId(Long locationId) {
        List<Schedule> schedules = scheduleRepository.findDistinctSchedulesByLocation(locationId);

        return FindSchedules(schedules);
    }

    private List<ScheduleGetDto> FindSchedules(List<Schedule> schedules) {
        return schedules.stream()
                .map(schedule -> {
                    Member member = memberRepository.findById(schedule.getMember().getMemberId()).orElseThrow(() -> new RuntimeException("Member not found"));

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

    public String AddNewLocation(NewLocationDto newLocationDto, Principal principal) {
        Optional<Member> optionalMember = memberRepository.findMemberByEmail(principal.getName());
        if (optionalMember.isEmpty()) {
            throw new UsernameNotFoundException("유저가 존재하지 않습니다.");
        }
        Member member = optionalMember.get();

        Approval approval = Approval.builder()
                .member(member)
                .name(newLocationDto.getName())
                .address(newLocationDto.getAddress())
                .detail(newLocationDto.getDetail())
                .latitude(newLocationDto.getLatitude())
                .longitude(newLocationDto.getLongitude())
                .division(newLocationDto.getDivision())
                .phone(newLocationDto.getPhone())
                .content(newLocationDto.getContent())
                .build();

        Approval apply_approval = approvalRepository.save(approval);

        List<MultipartFile> images = newLocationDto.getImages();
        if (images != null) {
            images.forEach(image -> {
                try {
                    String url = s3Upload.uploadFiles(image, "new-location");
                    ApprovalImage approvalImage = ApprovalImage.builder()
                            .imageUrl(url)
                            .approval(apply_approval)
                            .build();

                    approvalImageRepository.save(approvalImage);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            });
        }
        return "새로운 장소 approval 추가 완료";
    }

}
