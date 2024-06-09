package NotFound.picnic.service;

import NotFound.picnic.domain.*;
import NotFound.picnic.dto.tour.*;
import NotFound.picnic.dto.schedule.*;
import NotFound.picnic.enums.State;
import NotFound.picnic.exception.CustomException;
import NotFound.picnic.exception.ErrorCode;
import NotFound.picnic.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.security.Principal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-dev.properties")
class TourServiceTest {

    @Autowired
    private TourService tourService;

    @MockBean
    private MemberRepository memberRepository;

    @MockBean
    private ScheduleRepository scheduleRepository;

    @MockBean
    private PlaceRepository placeRepository;

    @MockBean
    private DiaryRepository diaryRepository;

    @MockBean
    private ImageRepository imageRepository;

    @MockBean
    private LocationRepository locationRepository;

    @MockBean
    private LocationImageRepostiory locationImageRepository;

    @MockBean
    private CityRepository cityRepository;

    @MockBean
    private AccommodationRepository accommodationRepository;

    @MockBean
    private CultureRepository cultureRepository;

    @MockBean
    private FestivalRepository festivalRepository;

    @MockBean
    private LeisureRepository leisureRepository;

    @MockBean
    private RestaurantRepository restaurantRepository;

    @MockBean
    private ShoppingRepository shoppingRepository;

    @MockBean
    private TourRepository tourRepository;

    @MockBean
    private ApprovalRepository approvalRepository;

    @BeforeEach
    public void setUp() {
    }

    @Test
    @DisplayName("장소 리스트")
    void getLocations() {
        // Given
        String city = "Seoul";
        String keyword = "park";
        String division = "attraction";
        int lastIdx = 0;

        Location location1 = Location.builder()
                .locationId(1L)
                .name("Park 1")
                .address("Address 1")
                .latitude(37.5665)
                .longitude(126.9780)
                .build();

        Location location2 = Location.builder()
                .locationId(2L)
                .name("Park 2")
                .address("Address 2")
                .latitude(37.5765)
                .longitude(126.9880)
                .build();

        List<Location> locationList = Arrays.asList(location1, location2);

        LocationImage locationImage1 = LocationImage.builder()
                .location(location1)
                .imageUrl("http://testurl.com/image1")
                .build();

        when(locationRepository.findByCityAndKeyword(city, division, keyword, lastIdx)).thenReturn(Optional.of(locationList));
        when(locationImageRepository.findTopByLocation(location1)).thenReturn(Optional.of(locationImage1));
        when(locationImageRepository.findTopByLocation(location2)).thenReturn(Optional.empty());

        // When
        List<LocationGetDto> result = tourService.GetLocations(city, keyword, division, lastIdx);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());

        LocationGetDto dto1 = result.get(0);
        assertEquals(location1.getLocationId(), dto1.getLocationId());
        assertEquals(location1.getName(), dto1.getName());
        assertEquals(location1.getAddress(), dto1.getAddress());
        assertEquals(location1.getLatitude(), dto1.getLatitude());
        assertEquals(location1.getLongitude(), dto1.getLongitude());
        assertEquals(locationImage1.getImageUrl(), dto1.getImageUrl());

        LocationGetDto dto2 = result.get(1);
        assertEquals(location2.getLocationId(), dto2.getLocationId());
        assertEquals(location2.getName(), dto2.getName());
        assertEquals(location2.getAddress(), dto2.getAddress());
        assertEquals(location2.getLatitude(), dto2.getLatitude());
        assertEquals(location2.getLongitude(), dto2.getLongitude());
        assertNull(dto2.getImageUrl());

        verify(locationRepository, times(1)).findByCityAndKeyword(city, division, keyword, lastIdx);
        verify(locationImageRepository, times(1)).findTopByLocation(location1);
        verify(locationImageRepository, times(1)).findTopByLocation(location2);
    }

    @Test
    @DisplayName("도시 정보 리스트")
    void getCities() {
        // Given
        String keyword = "Seoul";
        String keyword2 = "Busan";

        City city1 = City.builder()
                .name("Seoul")
                .detail("Capital of South Korea")
                .imageUrl("http://testurl.com/seoul.jpg")
                .build();

        City city2 = City.builder()
                .name("Busan")
                .detail("A major port city in South Korea")
                .imageUrl("http://testurl.com/busan.jpg")
                .build();

        List<City> cityList = Arrays.asList(city1, city2);

        when(cityRepository.findAllByNameContainingOrNameContaining(keyword, keyword2)).thenReturn(cityList);

        // When
        List<CityGetDto> result = tourService.GetCities(keyword, keyword2);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());

        CityGetDto dto1 = result.get(0);
        assertEquals(city1.getName(), dto1.getCityName());
        assertEquals(city1.getDetail(), dto1.getCityDetail());
        assertEquals(city1.getImageUrl(), dto1.getImageUrl());

        CityGetDto dto2 = result.get(1);
        assertEquals(city2.getName(), dto2.getCityName());
        assertEquals(city2.getDetail(), dto2.getCityDetail());
        assertEquals(city2.getImageUrl(), dto2.getImageUrl());

        verify(cityRepository, times(1)).findAllByNameContainingOrNameContaining(keyword, keyword2);
    }


    @Test
    @DisplayName("일정 복사")
    void duplicateSchedule() {
        // Given
        Long scheduleId = 1L;
        String email = "test@example.com";
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn(email);

        Member member = new Member();
        member.setEmail(email);
        member.setMemberId(1L);

        Schedule scheduleOld = Schedule.builder()
                .scheduleId(scheduleId)
                .name("Old Schedule")
                .location("Old Location")
                .startDate("2024-06-01")
                .endDate("2024-06-10")
                .member(member)
                .build();

        ScheduleDuplicateDto scheduleDuplicateDto = ScheduleDuplicateDto.builder()
                .name("New Schedule")
                .startDate("2024-07-01")
                .endDate("2024-07-10")
                .build();

        Schedule scheduleNew = Schedule.builder()
                .scheduleId(2L)
                .name("New Schedule")
                .location("Old Location")
                .startDate("2024-07-01")
                .endDate("2024-07-10")
                .member(member)
                .build();

        Location location = Location.builder()
                .locationId(1L)
                .name("Test Location")
                .build();

        Place place1 = Place.builder()
                .placeId(1L)
                .date("2024-06-02")
                .time("10:00")
                .location(location)
                .schedule(scheduleOld)
                .build();

        Place place2 = Place.builder()
                .placeId(2L)
                .date("2024-06-03")
                .time("11:00")
                .location(location)
                .schedule(scheduleOld)
                .build();

        List<Place> oldPlaces = Arrays.asList(place1, place2);

        when(memberRepository.findMemberByEmail(email)).thenReturn(Optional.of(member));
        when(scheduleRepository.findById(scheduleId)).thenReturn(Optional.of(scheduleOld));
        when(scheduleRepository.save(any(Schedule.class))).thenReturn(scheduleNew);
        when(placeRepository.findBySchedule(scheduleOld)).thenReturn(oldPlaces);
        when(placeRepository.save(any(Place.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Long newScheduleId = tourService.DuplicateSchedule(scheduleId, scheduleDuplicateDto, principal);

        // Then
        assertNotNull(newScheduleId);
        assertEquals(scheduleNew.getScheduleId(), newScheduleId);

        verify(memberRepository, times(1)).findMemberByEmail(email);
        verify(scheduleRepository, times(1)).findById(scheduleId);
        verify(scheduleRepository, times(1)).save(any(Schedule.class));
        verify(placeRepository, times(1)).findBySchedule(scheduleOld);
        verify(placeRepository, times(2)).save(any(Place.class)); // 두 개의 장소가 저장되어야 함
    }



    @Test
    @DisplayName("장소 세부 정보 보기")
    void getLocationDetail() {
        // Given
        Long locationId = 1L;

        Location location = Location.builder()
                .locationId(locationId)
                .name("Test Location")
                .address("123 Test St")
                .detail("Test detail")
                .latitude(37.5665)
                .longitude(126.9780)
                .division("숙박") // 테스트 케이스마다 변경될 수 있음
                .phone("123-4567")
                .build();

        List<LocationImage> images = Arrays.asList(
                LocationImage.builder().imageUrl("http://testurl.com/image1").location(location).build(),
                LocationImage.builder().imageUrl("http://testurl.com/image2").location(location).build()
        );

        Accommodation accommodation = Accommodation.builder()
                .checkIn("14:00")
                .checkOut("11:00")
                .cook("Available")
                .detail("Accommodation detail")
                .parking("Available")
                .reservation("Required")
                .build();

        Culture culture = Culture.builder()
                .babycar("Allowed")
                .detail("Culture detail")
                .discount("None")
                .fee("Free")
                .offDate("Mondays")
                .parking("Available")
                .pet("Not allowed")
                .time("09:00-18:00")
                .build();

        Festival festival = Festival.builder()
                .detail("Festival detail")
                .startDate("2024-01-01")
                .endDate("2024-01-10")
                .fee("Free")
                .time("09:00-22:00")
                .build();

        Leisure leisure = Leisure.builder()
                .babycar("Allowed")
                .detail("Leisure detail")
                .fee("Varies")
                .openDate("2024-01-01")
                .offDate("Mondays")
                .parking("Available")
                .pet("Allowed")
                .time("09:00-18:00")
                .build();

        Restaurant restaurant = Restaurant.builder()
                .dayOff("Sundays")
                .mainMenu("Korean BBQ")
                .menu("Various")
                .packaging("Available")
                .build();

        Shopping shopping = Shopping.builder()
                .babycar("Allowed")
                .offDate("Public holidays")
                .parking("Available")
                .pet("Allowed")
                .time("09:00-22:00")
                .build();

        Tour tour = Tour.builder()
                .detail("Tour detail")
                .offDate("No off days")
                .parking("Available")
                .pet("Allowed")
                .time("09:00-18:00")
                .build();

        when(locationRepository.findByLocationId(locationId)).thenReturn(Optional.of(location));
        when(locationImageRepository.findAllByLocation(location)).thenReturn(images);

        // When division is "숙박"
        location.setDivision("숙박");
        when(accommodationRepository.findByLocation_LocationId(locationId)).thenReturn(accommodation);
        LocationDetailDto result = tourService.GetLocationDetail(locationId);
        assertNotNull(result);
        assertEquals(accommodation.getCheckIn(), result.getAccommodation().getCheckIn());
        assertEquals(accommodation.getCheckOut(), result.getAccommodation().getCheckOut());
        verify(accommodationRepository, times(1)).findByLocation_LocationId(locationId);

        // When division is "문화시설"
        location.setDivision("문화시설");
        when(cultureRepository.findByLocation_LocationId(locationId)).thenReturn(culture);
        result = tourService.GetLocationDetail(locationId);
        assertNotNull(result);
        assertEquals(culture.getDetail(), result.getCulture().getDetail());
        assertEquals(culture.getFee(), result.getCulture().getFee());
        verify(cultureRepository, times(1)).findByLocation_LocationId(locationId);

        // When division is "축제 공연 행사"
        location.setDivision("축제 공연 행사");
        when(festivalRepository.findByLocation_LocationId(locationId)).thenReturn(festival);
        result = tourService.GetLocationDetail(locationId);
        assertNotNull(result);
        assertEquals(festival.getDetail(), result.getFestival().getDetail());
        assertEquals(festival.getStartDate(), result.getFestival().getStartDate());
        verify(festivalRepository, times(1)).findByLocation_LocationId(locationId);

        // When division is "레포츠"
        location.setDivision("레포츠");
        when(leisureRepository.findByLocation_LocationId(locationId)).thenReturn(leisure);
        result = tourService.GetLocationDetail(locationId);
        assertNotNull(result);
        assertEquals(leisure.getDetail(), result.getLeisure().getDetail());
        assertEquals(leisure.getFee(), result.getLeisure().getFee());
        verify(leisureRepository, times(1)).findByLocation_LocationId(locationId);

        // When division is "음식점"
        location.setDivision("음식점");
        when(restaurantRepository.findByLocation_LocationId(locationId)).thenReturn(restaurant);
        result = tourService.GetLocationDetail(locationId);
        assertNotNull(result);
        assertEquals(restaurant.getMainMenu(), result.getRestaurant().getMainMenu());
        assertEquals(restaurant.getMenu(), result.getRestaurant().getMenu());
        verify(restaurantRepository, times(1)).findByLocation_LocationId(locationId);

        // When division is "쇼핑"
        location.setDivision("쇼핑");
        when(shoppingRepository.findByLocation_LocationId(locationId)).thenReturn(shopping);
        result = tourService.GetLocationDetail(locationId);
        assertNotNull(result);
        assertEquals(shopping.getTime(), result.getShopping().getTime());
        assertEquals(shopping.getParking(), result.getShopping().getParking());
        verify(shoppingRepository, times(1)).findByLocation_LocationId(locationId);

        // When division is "관광지"
        location.setDivision("관광지");
        when(tourRepository.findByLocation_LocationId(locationId)).thenReturn(tour);
        result = tourService.GetLocationDetail(locationId);
        assertNotNull(result);
        assertEquals(tour.getDetail(), result.getTour().getDetail());
        assertEquals(tour.getOffDate(), result.getTour().getOffDate());
        verify(tourRepository, times(1)).findByLocation_LocationId(locationId);

        verify(locationRepository, times(7)).findByLocationId(locationId);
        verify(locationImageRepository, times(7)).findAllByLocation(location);
    }


    @Test
    @DisplayName("장소를 일정에 추가")
    void addPlaceToSchedule() {
        // Given
        ScheduleAddPlaceDto scheduleAddPlaceDto = ScheduleAddPlaceDto.builder()
                .scheduleId(1L)
                .locationId(2L)
                .date("2024-06-01")
                .time("10:00")
                .build();

        Schedule schedule = Schedule.builder()
                .scheduleId(scheduleAddPlaceDto.getScheduleId())
                .name("Test Schedule")
                .build();

        Location location = Location.builder()
                .locationId(scheduleAddPlaceDto.getLocationId())
                .name("Test Location")
                .build();

        when(scheduleRepository.findByScheduleId(scheduleAddPlaceDto.getScheduleId())).thenReturn(Optional.of(schedule));
        when(locationRepository.findByLocationId(scheduleAddPlaceDto.getLocationId())).thenReturn(Optional.of(location));
        when(placeRepository.save(any(Place.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("test@example.com");

        // When
        String result = tourService.AddPlaceToSchedule(scheduleAddPlaceDto, principal);

        // Then
        assertNotNull(result);
        assertEquals("해당 스케쥴에 장소를 추가하였습니다", result);

        ArgumentCaptor<Place> placeCaptor = ArgumentCaptor.forClass(Place.class);
        verify(placeRepository).save(placeCaptor.capture());
        Place savedPlace = placeCaptor.getValue();

        assertEquals(scheduleAddPlaceDto.getDate(), savedPlace.getDate());
        assertEquals(scheduleAddPlaceDto.getTime(), savedPlace.getTime());
        assertEquals(location, savedPlace.getLocation());
        assertEquals(schedule, savedPlace.getSchedule());

        verify(scheduleRepository, times(1)).findByScheduleId(scheduleAddPlaceDto.getScheduleId());
        verify(locationRepository, times(1)).findByLocationId(scheduleAddPlaceDto.getLocationId());
    }


    @Test
    @DisplayName("일기 리스트")
    void getDiaries() {
        // Given
        Long locationId = 1L;

        Member member = new Member();
        member.setMemberId(1L);
        member.setName("Test User");

        Schedule schedule = Schedule.builder()
                .scheduleId(1L)
                .name("Test Schedule")
                .member(member)
                .build();

        Place place1 = Place.builder()
                .placeId(1L)
                .date("2024-06-01")
                .schedule(schedule)
                .location(new Location())
                .build();

        Place place2 = Place.builder()
                .placeId(2L)
                .date("2024-06-02")
                .schedule(schedule)
                .location(new Location())
                .build();

        List<Place> places = Arrays.asList(place1, place2);

        Diary diary1 = Diary.builder()
                .diaryId(1L)
                .title("Diary 1")
                .content("Content 1")
                .place(place1)
                .build();

        Diary diary2 = Diary.builder()
                .diaryId(2L)
                .title("Diary 2")
                .content("Content 2")
                .place(place2)
                .build();

        Image image1 = Image.builder()
                .diary(diary1)
                .imageUrl("http://testurl.com/image1")
                .build();

        when(placeRepository.findAllByLocation_LocationId(locationId)).thenReturn(Optional.of(places));
        when(scheduleRepository.findById(place1.getSchedule().getScheduleId())).thenReturn(Optional.of(schedule));
        when(diaryRepository.findByPlace(place1)).thenReturn(Optional.of(diary1));
        when(diaryRepository.findByPlace(place2)).thenReturn(Optional.of(diary2));
        when(imageRepository.findTopByDiary(diary1)).thenReturn(Optional.of(image1));
        when(imageRepository.findTopByDiary(diary2)).thenReturn(Optional.empty());

        // When
        List<DiaryGetDto> result = tourService.GetDiaries(locationId);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());

        DiaryGetDto dto1 = result.get(0);
        assertEquals(diary1.getDiaryId(), dto1.getDiaryId());
        assertEquals(place1.getPlaceId(), dto1.getPlaceId());
        assertEquals(member.getName(), dto1.getUserName());
        assertEquals(diary1.getTitle(), dto1.getTitle());
        assertEquals(place1.getDate(), dto1.getDate());
        assertEquals(diary1.getContent(), dto1.getContent());
        assertEquals("http://testurl.com/image1", dto1.getImageUrl());

        DiaryGetDto dto2 = result.get(1);
        assertEquals(diary2.getDiaryId(), dto2.getDiaryId());
        assertEquals(place2.getPlaceId(), dto2.getPlaceId());
        assertEquals(member.getName(), dto2.getUserName());
        assertEquals(diary2.getTitle(), dto2.getTitle());
        assertEquals(place2.getDate(), dto2.getDate());
        assertEquals(diary2.getContent(), dto2.getContent());
        assertNull(dto2.getImageUrl());

        verify(placeRepository, times(1)).findAllByLocation_LocationId(locationId);
        verify(scheduleRepository, times(1)).findById(place1.getSchedule().getScheduleId());
        verify(diaryRepository, times(1)).findByPlace(place1);
        verify(diaryRepository, times(1)).findByPlace(place2);
        verify(imageRepository, times(1)).findTopByDiary(diary1);
        verify(imageRepository, times(1)).findTopByDiary(diary2);
    }

    @Test
    @DisplayName("일기 상세보기")
    void getDiaryDetail() {
        // Given
        Long diaryId = 1L;

        Member member = new Member();
        member.setMemberId(1L);
        member.setName("Test User");

        Schedule schedule = Schedule.builder()
                .scheduleId(1L)
                .name("Test Schedule")
                .member(member)
                .build();

        Location location = Location.builder()
                .locationId(1L)
                .longitude(126.9780)
                .latitude(37.5665)
                .build();

        Place place = Place.builder()
                .placeId(1L)
                .date("2024-06-01")
                .schedule(schedule)
                .location(location)
                .build();

        Diary diary = Diary.builder()
                .diaryId(diaryId)
                .title("Test Diary")
                .content("This is a test diary.")
                .weather("Sunny")
                .place(place)
                .build();

        Image image1 = Image.builder()
                .diary(diary)
                .imageUrl("http://testurl.com/image1")
                .build();

        Image image2 = Image.builder()
                .diary(diary)
                .imageUrl("http://testurl.com/image2")
                .build();

        List<Image> images = Arrays.asList(image1, image2);

        when(diaryRepository.findById(diaryId)).thenReturn(Optional.of(diary));
        when(imageRepository.findAllByDiary(diary)).thenReturn(images);
        when(placeRepository.findByDiary_DiaryId(diaryId)).thenReturn(place);

        // When
        DiaryDetailDto result = tourService.GetDiaryDetail(diaryId);

        // Then
        assertNotNull(result);
        assertEquals(diary.getTitle(), result.getTitle());
        assertEquals(diary.getContent(), result.getContent());
        assertEquals(diary.getWeather(), result.getWeather());
        assertEquals(place.getLocation().getLongitude(), result.getLongitude());
        assertEquals(place.getLocation().getLatitude(), result.getLatitude());
        assertEquals(place.getDate(), result.getDate());
        assertEquals(member.getName(), result.getUserName());
        assertEquals(2, result.getImageUrl().size());
        assertTrue(result.getImageUrl().contains("http://testurl.com/image1"));
        assertTrue(result.getImageUrl().contains("http://testurl.com/image2"));

        verify(diaryRepository, times(1)).findById(diaryId);
        verify(imageRepository, times(1)).findAllByDiary(diary);
        verify(placeRepository, times(1)).findByDiary_DiaryId(diaryId);
    }

    @Test
    @DisplayName("새로운 장소 추가")
    void addNewLocation() throws IOException {
        // Given
        String email = "test@example.com";
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn(email);

        Member member = new Member();
        member.setEmail(email);
        member.setMemberId(1L);

        NewLocationDto newLocationDto = NewLocationDto.builder()
                .name("New Location")
                .address("123 Test St")
                .detail("Test Detail")
                .latitude(37.5665)
                .longitude(126.9780)
                .division("Test Division")
                .phone("123-4567")
                .content("Test Content")
                .build();

        Approval approval = Approval.builder()
                .approvalId(1L)
                .member(member)
                .name(newLocationDto.getName())
                .address(newLocationDto.getAddress())
                .detail(newLocationDto.getDetail())
                .latitude(newLocationDto.getLatitude())
                .longitude(newLocationDto.getLongitude())
                .division(newLocationDto.getDivision())
                .phone(newLocationDto.getPhone())
                .content(newLocationDto.getContent())
                .state(State.APPLIED)
                .build();

        when(memberRepository.findMemberByEmail(email)).thenReturn(Optional.of(member));
        when(approvalRepository.save(any(Approval.class))).thenReturn(approval);

        // When
        String result = tourService.AddNewLocation(newLocationDto, principal);

        // Then
        assertNotNull(result);
        assertEquals("새로운 장소 approval 추가 완료", result);

        verify(memberRepository, times(1)).findMemberByEmail(email);
        verify(approvalRepository, times(1)).save(any(Approval.class));
    }

}