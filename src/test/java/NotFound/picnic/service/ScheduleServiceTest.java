package NotFound.picnic.service;

import NotFound.picnic.domain.*;
import NotFound.picnic.dto.schedule.*;
import NotFound.picnic.exception.CustomException;
import NotFound.picnic.exception.ErrorCode;
import NotFound.picnic.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-local.properties")
class ScheduleServiceTest {

    @Autowired
    private ScheduleService scheduleService;

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
    private LocationImageRepostiory locationImageRepostiory;

    @BeforeEach
    public void setUp() {
        // No need for MockitoAnnotations.openMocks(this) when using @SpringBootTest with @MockBean
    }

    @Test
    @DisplayName("일정 생성 성공")
    void createSchedule() {
        // Given
        String email = "test@example.com";
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn(email);

        Member member = new Member();
        member.setEmail(email);
        when(memberRepository.findMemberByEmail(email)).thenReturn(Optional.of(member));

        ScheduleCreateDto scheduleCreateDto = ScheduleCreateDto.builder()
                .name("test schedule")
                .location("서울특별시")
                .startDate("2024-05-05")
                .endDate("2024-05-07")
                .share(true)
                .build();

        Schedule schedule = Schedule.builder()
                .name(scheduleCreateDto.getName())
                .location(scheduleCreateDto.getLocation())
                .startDate(scheduleCreateDto.getStartDate())
                .endDate(scheduleCreateDto.getEndDate())
                .member(member)
                .share(scheduleCreateDto.isShare())
                .build();

        // Mocking scheduleId
        when(scheduleRepository.save(any(Schedule.class))).thenAnswer(invocation -> {
            Schedule savedSchedule = invocation.getArgument(0);
            savedSchedule.setScheduleId(1L); // 설정된 scheduleId
            return savedSchedule;
        });

        // When
        Long scheduleId = scheduleService.createSchedule(scheduleCreateDto, principal);

        // Then
        assertNotNull(scheduleId);
        assertEquals(1L, scheduleId);
    }

    @Test
    @DisplayName("일정 생성 실패 - 유저 존재하지 않는 경우")
    void createSchedule_UserNotFound() {
        // Given
        String email = "test@example.com";
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn(email);

        when(memberRepository.findMemberByEmail(email)).thenReturn(Optional.empty());

        ScheduleCreateDto scheduleCreateDto = ScheduleCreateDto.builder()
                .name("test schedule")
                .location("서울특별시")
                .startDate("2024-05-05")
                .endDate("2024-05-07")
                .share(true)
                .build();

        // When & Then
        CustomException exception = assertThrows(CustomException.class, () -> {
            scheduleService.createSchedule(scheduleCreateDto, principal);
        });

        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
        verify(memberRepository, times(1)).findMemberByEmail(email);
        verify(scheduleRepository, never()).save(any(Schedule.class));
    }

    @Test
    @DisplayName("일정에 포함된 장소 정보와 일기 정보 불러오기")
    void getSchedulePlaceDiary() {
        // Given
        Long scheduleId = 1L;
        String email = "test@example.com";
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn(email);

        Member member = new Member();
        member.setEmail(email);

        Schedule schedule = new Schedule();
        schedule.setScheduleId(scheduleId);
        schedule.setMember(member);

        Location location = Location.builder()
                .locationId(1L)
                .name("Test Location")
                .latitude(37.5665)
                .longitude(126.9780)
                .build();

        Place place1 = Place.builder()
                .placeId(1L)
                .date("2024-06-09")
                .time("10:00")
                .schedule(schedule)
                .location(location)
                .build();

        Place place2 = Place.builder()
                .placeId(2L)
                .date("2024-06-10")
                .time("11:00")
                .schedule(schedule)
                .location(location)
                .build();

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

        Image image2 = Image.builder()
                .diary(diary2)
                .imageUrl("http://testurl.com/image2")
                .build();

        when(scheduleRepository.findById(scheduleId)).thenReturn(Optional.of(schedule));
        when(placeRepository.findByScheduleOrderByDateAscTimeAsc(schedule)).thenReturn(Arrays.asList(place1, place2));
        when(diaryRepository.findAllByPlace_PlaceId(place1.getPlaceId())).thenReturn(Arrays.asList(diary1));
        when(diaryRepository.findAllByPlace_PlaceId(place2.getPlaceId())).thenReturn(Arrays.asList(diary2));
        when(imageRepository.findTopImageUrlByDiary_DiaryId(diary1.getDiaryId())).thenReturn(Optional.of(image1));
        when(imageRepository.findTopImageUrlByDiary_DiaryId(diary2.getDiaryId())).thenReturn(Optional.of(image2));

        // When
        List<SchedulePlaceDiaryGetDto> result = scheduleService.getSchedulePlaceDiary(scheduleId, principal);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());

        SchedulePlaceDiaryGetDto dto1 = result.get(0);
        assertEquals(place1.getPlaceId(), dto1.getPlaceId());
        assertEquals(location.getLocationId(), dto1.getLocationId());
        assertEquals(location.getName(), dto1.getLocationName());
        assertEquals(place1.getDate(), dto1.getDate());
        assertEquals(place1.getTime(), dto1.getTime());
        assertEquals(location.getLatitude(), dto1.getLatitude());
        assertEquals(location.getLongitude(), dto1.getLongitude());
        assertEquals(diary1.getDiaryId(), dto1.getDiaryId());
        assertEquals(diary1.getTitle(), dto1.getTitle());
        assertEquals(diary1.getContent(), dto1.getContent());
        assertEquals(image1.getImageUrl(), dto1.getImageUrl());

        SchedulePlaceDiaryGetDto dto2 = result.get(1);
        assertEquals(place2.getPlaceId(), dto2.getPlaceId());
        assertEquals(location.getLocationId(), dto2.getLocationId());
        assertEquals(location.getName(), dto2.getLocationName());
        assertEquals(place2.getDate(), dto2.getDate());
        assertEquals(place2.getTime(), dto2.getTime());
        assertEquals(location.getLatitude(), dto2.getLatitude());
        assertEquals(location.getLongitude(), dto2.getLongitude());
        assertEquals(diary2.getDiaryId(), dto2.getDiaryId());
        assertEquals(diary2.getTitle(), dto2.getTitle());
        assertEquals(diary2.getContent(), dto2.getContent());
        assertEquals(image2.getImageUrl(), dto2.getImageUrl());

        verify(scheduleRepository, times(1)).findById(scheduleId);
        verify(placeRepository, times(1)).findByScheduleOrderByDateAscTimeAsc(schedule);
        verify(diaryRepository, times(1)).findAllByPlace_PlaceId(place1.getPlaceId());
        verify(diaryRepository, times(1)).findAllByPlace_PlaceId(place2.getPlaceId());
        verify(imageRepository, times(1)).findTopImageUrlByDiary_DiaryId(diary1.getDiaryId());
        verify(imageRepository, times(1)).findTopImageUrlByDiary_DiaryId(diary2.getDiaryId());
    }


    @Test
    @DisplayName("일기 생성 성공")
    void createDiary() {
        // Given
        Long placeId = 1L;
        String title = "Test Diary";
        String content = "Test Content";
        String weather = "Sunny";

        // Place 객체 설정
        Schedule schedule = new Schedule();
        schedule.setScheduleId(1L);

        Location location = new Location();
        location.setLocationId(1L);

        Place place = Place.builder()
                .placeId(placeId)
                .date("2024-06-09")
                .time("10:00")
                .schedule(schedule)
                .location(location)
                .build();

        DiaryCreateDto diaryCreateDto = DiaryCreateDto.builder()
                .title(title)
                .content(content)
                .weather(weather)
                .build();

        when(placeRepository.findById(placeId)).thenReturn(Optional.of(place));
        when(diaryRepository.existsByPlace(place)).thenReturn(false);

        Diary diary = Diary.builder()
                .place(place)
                .title(title)
                .content(content)
                .weather(weather)
                .build();
        diary.setDiaryId(1L); // Mocking diaryId

        when(diaryRepository.save(any(Diary.class))).thenReturn(diary);

        // When
        DiaryResponseDto diaryResponseDto = scheduleService.createDiary(placeId, diaryCreateDto);

        // Then
        assertNotNull(diaryResponseDto);
        assertEquals(1L, diaryResponseDto.getDiaryId());

        verify(placeRepository, times(1)).findById(placeId);
        verify(diaryRepository, times(1)).existsByPlace(place);
        verify(diaryRepository, times(1)).save(any(Diary.class));
    }



    @Test
    @DisplayName("일기 수정 성공")
    void updateDiary() throws CustomException {
        // Given
        Long diaryId = 1L;
        String newTitle = "Updated Diary Title";
        String newContent = "Updated Diary Content";
        String newWeather = "Rainy";

        String email = "test@example.com";
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn(email);

        Member member = new Member();
        member.setEmail(email);

        Schedule schedule = new Schedule();
        schedule.setMember(member);

        Place place = new Place();
        place.setSchedule(schedule);

        Diary diary = Diary.builder()
                .diaryId(diaryId)
                .title("Old Diary Title")
                .content("Old Diary Content")
                .weather("Sunny")
                .place(place)
                .build();

        DiaryCreateDto diaryCreateDto = DiaryCreateDto.builder()
                .title(newTitle)
                .content(newContent)
                .weather(newWeather)
                .build();

        when(diaryRepository.findById(diaryId)).thenReturn(Optional.of(diary));
        when(memberRepository.findMemberByEmail(email)).thenReturn(Optional.of(member));
        when(placeRepository.findByDiary_DiaryId(diaryId)).thenReturn(place);

        // When
        String result = scheduleService.UpdateDiary(diaryId, diaryCreateDto, principal);

        // Then
        assertEquals("일기 수정 완료", result);
        assertEquals(newTitle, diary.getTitle());
        assertEquals(newContent, diary.getContent());
        assertEquals(newWeather, diary.getWeather());

        verify(diaryRepository, times(1)).findById(diaryId);
        verify(memberRepository, times(1)).findMemberByEmail(email);
        verify(placeRepository, times(1)).findByDiary_DiaryId(diaryId);
    }


    @Test
    @DisplayName("일기 삭제 성공")
    void deleteDiary() throws CustomException {
        // Given
        Long diaryId = 1L;
        String email = "test@example.com";
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn(email);

        Member member = new Member();
        member.setEmail(email);

        Schedule schedule = new Schedule();
        schedule.setMember(member);

        Place place = new Place();
        place.setSchedule(schedule);

        Diary diary = Diary.builder()
                .diaryId(diaryId)
                .title("Test Diary")
                .content("Test Content")
                .place(place)
                .build();

        when(diaryRepository.findById(diaryId)).thenReturn(Optional.of(diary));
        when(memberRepository.findMemberByEmail(email)).thenReturn(Optional.of(member));
        when(placeRepository.findByDiary_DiaryId(diaryId)).thenReturn(place);

        // When
        String result = scheduleService.DeleteDiary(diaryId, principal);

        // Then
        assertEquals("일기 삭제 완료", result);

        verify(diaryRepository, times(1)).findById(diaryId);
        verify(memberRepository, times(1)).findMemberByEmail(email);
        verify(placeRepository, times(1)).findByDiary_DiaryId(diaryId);
        verify(imageRepository, times(1)).deleteByDiary_DiaryId(diaryId);
        verify(diaryRepository, times(1)).deleteByDiaryId(diaryId);
    }

    @Test
    @DisplayName("여행 일정 보기")
    void getPlaces() {
        // Given
        Long scheduleId = 1L;

        Schedule schedule = new Schedule();
        schedule.setScheduleId(scheduleId);

        Location location1 = Location.builder()
                .locationId(1L)
                .name("Location 1")
                .address("Address 1")
                .latitude(37.5665)
                .longitude(126.9780)
                .build();

        Location location2 = Location.builder()
                .locationId(2L)
                .name("Location 2")
                .address("Address 2")
                .latitude(37.5775)
                .longitude(126.9870)
                .build();

        Place place1 = Place.builder()
                .placeId(1L)
                .date("2024-06-09")
                .time("10:00")
                .schedule(schedule)
                .location(location1)
                .build();

        Place place2 = Place.builder()
                .placeId(2L)
                .date("2024-06-09")
                .time("11:00")
                .schedule(schedule)
                .location(location2)
                .build();

        Place place3 = Place.builder()
                .placeId(3L)
                .date("2024-06-10")
                .time("12:00")
                .schedule(schedule)
                .location(location1)
                .build();

        List<Place> placeList = Arrays.asList(place1, place2, place3);

        when(placeRepository.findAllBySchedule_ScheduleId(scheduleId)).thenReturn(Optional.of(placeList));
        when(locationImageRepostiory.findTopByLocation(location1)).thenReturn(Optional.empty());
        when(locationImageRepostiory.findTopByLocation(location2)).thenReturn(Optional.empty());

        // When
        List<List<PlaceGetDto>> result = scheduleService.getPlaces(scheduleId);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size()); // 날짜가 2개이므로 리스트 크기는 2
        assertEquals(2, result.get(0).size()); // 첫 번째 날짜의 장소 개수는 2
        assertEquals(1, result.get(1).size()); // 두 번째 날짜의 장소 개수는 1

        PlaceGetDto dto1 = result.get(0).get(0);
        assertEquals(place1.getPlaceId(), dto1.getPlaceId());
        assertEquals(location1.getLocationId(), dto1.getLocationId());
        assertEquals(location1.getName(), dto1.getName());
        assertEquals(location1.getAddress(), dto1.getAddress());
        assertEquals(location1.getLatitude(), dto1.getLatitude());
        assertEquals(location1.getLongitude(), dto1.getLongitude());
        assertNull(dto1.getImageUrl());
        assertEquals(place1.getTime(), dto1.getTime());

        PlaceGetDto dto2 = result.get(0).get(1);
        assertEquals(place2.getPlaceId(), dto2.getPlaceId());
        assertEquals(location2.getLocationId(), dto2.getLocationId());
        assertEquals(location2.getName(), dto2.getName());
        assertEquals(location2.getAddress(), dto2.getAddress());
        assertEquals(location2.getLatitude(), dto2.getLatitude());
        assertEquals(location2.getLongitude(), dto2.getLongitude());
        assertNull(dto2.getImageUrl());
        assertEquals(place2.getTime(), dto2.getTime());

        PlaceGetDto dto3 = result.get(1).get(0);
        assertEquals(place3.getPlaceId(), dto3.getPlaceId());
        assertEquals(location1.getLocationId(), dto3.getLocationId());
        assertEquals(location1.getName(), dto3.getName());
        assertEquals(location1.getAddress(), dto3.getAddress());
        assertEquals(location1.getLatitude(), dto3.getLatitude());
        assertEquals(location1.getLongitude(), dto3.getLongitude());
        assertNull(dto3.getImageUrl());
        assertEquals(place3.getTime(), dto3.getTime());

        verify(placeRepository, times(1)).findAllBySchedule_ScheduleId(scheduleId);
        verify(locationImageRepostiory, times(2)).findTopByLocation(location1);
        verify(locationImageRepostiory, times(1)).findTopByLocation(location2);
    }


    @Test
    @DisplayName("마이페이지의 일정들 관람")
    void getSchedulesInMyPage() {
        // Given
        String email = "test@example.com";
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn(email);

        Member member = new Member();
        member.setEmail(email);

        Schedule schedule1 = Schedule.builder()
                .scheduleId(1L)
                .name("Upcoming Trip")
                .startDate("2024-07-01")
                .endDate("2024-07-10")
                .location("Location 1")
                .share(true)
                .member(member)
                .build();

        Schedule schedule2 = Schedule.builder()
                .scheduleId(2L)
                .name("Current Trip")
                .startDate("2024-06-01")
                .endDate("2024-06-30")
                .location("Location 2")
                .share(true)
                .member(member)
                .build();

        Schedule schedule3 = Schedule.builder()
                .scheduleId(3L)
                .name("Past Trip")
                .startDate("2024-05-01")
                .endDate("2024-05-10")
                .location("Location 3")
                .share(true)
                .member(member)
                .build();

        List<Schedule> scheduleList = Arrays.asList(schedule1, schedule2, schedule3);

        when(memberRepository.findMemberByEmail(email)).thenReturn(Optional.of(member));
        when(scheduleRepository.findAllByMember(member)).thenReturn(scheduleList);
        when(diaryRepository.findAllBySchedule(schedule1)).thenReturn(Optional.empty());
        when(diaryRepository.findAllBySchedule(schedule2)).thenReturn(Optional.empty());
        when(diaryRepository.findAllBySchedule(schedule3)).thenReturn(Optional.empty());

        // When
        MyScheduleListDto result = scheduleService.GetSchedulesInMyPage(principal);

        // Then
        assertNotNull(result);

        // beforeTravel, traveling, afterTravel의 리스트를 검증합니다.
        assertEquals(1, result.getBeforeTravel().size());
        assertEquals(1, result.getTraveling().size());
        assertEquals(1, result.getAfterTravel().size());

        MyScheduleGetDto beforeTravelDto = result.getBeforeTravel().get(0);
        assertEquals(schedule1.getScheduleId(), beforeTravelDto.getScheduleId());
        assertEquals(schedule1.getName(), beforeTravelDto.getName());
        assertEquals(schedule1.getStartDate(), beforeTravelDto.getStartDate());
        assertEquals(schedule1.getEndDate(), beforeTravelDto.getEndDate());
        assertEquals(schedule1.getLocation(), beforeTravelDto.getLocation());
        assertEquals(schedule1.isShare(), beforeTravelDto.isShare());
        assertNull(beforeTravelDto.getImageUrl());

        MyScheduleGetDto travelingDto = result.getTraveling().get(0);
        assertEquals(schedule2.getScheduleId(), travelingDto.getScheduleId());
        assertEquals(schedule2.getName(), travelingDto.getName());
        assertEquals(schedule2.getStartDate(), travelingDto.getStartDate());
        assertEquals(schedule2.getEndDate(), travelingDto.getEndDate());
        assertEquals(schedule2.getLocation(), travelingDto.getLocation());
        assertEquals(schedule2.isShare(), travelingDto.isShare());
        assertNull(travelingDto.getImageUrl());

        MyScheduleGetDto afterTravelDto = result.getAfterTravel().get(0);
        assertEquals(schedule3.getScheduleId(), afterTravelDto.getScheduleId());
        assertEquals(schedule3.getName(), afterTravelDto.getName());
        assertEquals(schedule3.getStartDate(), afterTravelDto.getStartDate());
        assertEquals(schedule3.getEndDate(), afterTravelDto.getEndDate());
        assertEquals(schedule3.getLocation(), afterTravelDto.getLocation());
        assertEquals(schedule3.isShare(), afterTravelDto.isShare());
        assertNull(afterTravelDto.getImageUrl());

        verify(memberRepository, times(1)).findMemberByEmail(email);
        verify(scheduleRepository, times(1)).findAllByMember(member);
        verify(diaryRepository, times(1)).findAllBySchedule(schedule1);
        verify(diaryRepository, times(1)).findAllBySchedule(schedule2);
        verify(diaryRepository, times(1)).findAllBySchedule(schedule3);
    }


    @Test
    @DisplayName("일정 삭제")
    void deleteSchedule() throws IOException {
        // Given
        Long scheduleId = 1L;
        String email = "test@example.com";
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn(email);

        Member member = new Member();
        member.setEmail(email);

        Schedule schedule = Schedule.builder()
                .scheduleId(scheduleId)
                .name("Test Schedule")
                .startDate("2024-06-01")
                .endDate("2024-06-10")
                .location("Test Location")
                .share(true)
                .member(member)
                .build();

        when(memberRepository.findMemberByEmail(email)).thenReturn(Optional.of(member));
        when(scheduleRepository.findById(scheduleId)).thenReturn(Optional.of(schedule));

        // When
        String result = scheduleService.deleteSchedule(scheduleId, principal);

        // Then
        assertEquals("일정 삭제 완료", result);

        verify(memberRepository, times(1)).findMemberByEmail(email);
        verify(scheduleRepository, times(1)).findById(scheduleId);
        verify(scheduleRepository, times(1)).delete(schedule);
    }


    @Test
    @DisplayName("일정에서 장소 삭제")
    void deletePlace() throws IOException {
        // Given
        Long placeId = 1L;
        String email = "test@example.com";
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn(email);

        Member member = new Member();
        member.setEmail(email);

        Schedule schedule = Schedule.builder()
                .scheduleId(1L)
                .name("Test Schedule")
                .startDate("2024-06-01")
                .endDate("2024-06-10")
                .location("Test Location")
                .share(true)
                .member(member)
                .build();

        Place place = Place.builder()
                .placeId(placeId)
                .date("2024-06-09")
                .time("10:00")
                .schedule(schedule)
                .location(new Location())
                .build();

        when(memberRepository.findMemberByEmail(email)).thenReturn(Optional.of(member));
        when(placeRepository.findById(placeId)).thenReturn(Optional.of(place));

        // When
        String result = scheduleService.DeletePlace(placeId, principal);

        // Then
        assertEquals("장소 삭제 완료", result);

        verify(memberRepository, times(1)).findMemberByEmail(email);
        verify(placeRepository, times(1)).findById(placeId);
        verify(placeRepository, times(1)).delete(place);
    }



    @Test
    @DisplayName("일정 리스트 관람")
    void getSchedules() {
        // Given
        String email = "test@example.com";
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn(email);

        Member member = new Member();
        member.setEmail(email);

        Schedule schedule1 = Schedule.builder()
                .scheduleId(1L)
                .name("Schedule 1")
                .startDate("2024-06-01")
                .endDate("2024-06-10")
                .location("Location 1")
                .share(true)
                .member(member)
                .build();

        Schedule schedule2 = Schedule.builder()
                .scheduleId(2L)
                .name("Schedule 2")
                .startDate("2024-05-01")
                .endDate("2024-05-10")
                .location("Location 2")
                .share(true)
                .member(member)
                .build();

        List<Schedule> scheduleList = Arrays.asList(schedule1, schedule2);

        when(memberRepository.findMemberByEmail(email)).thenReturn(Optional.of(member));
        when(scheduleRepository.findAllByMemberOrderByStartDateDesc(member)).thenReturn(scheduleList);
        when(diaryRepository.findAllBySchedule(schedule1)).thenReturn(Optional.empty());
        when(diaryRepository.findAllBySchedule(schedule2)).thenReturn(Optional.empty());

        // When
        List<MyScheduleGetDto> result = scheduleService.GetSchedules(principal);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());

        MyScheduleGetDto dto1 = result.get(0);
        assertEquals(schedule1.getScheduleId(), dto1.getScheduleId());
        assertEquals(schedule1.getName(), dto1.getName());
        assertEquals(schedule1.getStartDate(), dto1.getStartDate());
        assertEquals(schedule1.getEndDate(), dto1.getEndDate());
        assertEquals(schedule1.isShare(), dto1.isShare());
        assertEquals(schedule1.getLocation(), dto1.getLocation());
        assertNull(dto1.getImageUrl());

        MyScheduleGetDto dto2 = result.get(1);
        assertEquals(schedule2.getScheduleId(), dto2.getScheduleId());
        assertEquals(schedule2.getName(), dto2.getName());
        assertEquals(schedule2.getStartDate(), dto2.getStartDate());
        assertEquals(schedule2.getEndDate(), dto2.getEndDate());
        assertEquals(schedule2.isShare(), dto2.isShare());
        assertEquals(schedule2.getLocation(), dto2.getLocation());
        assertNull(dto2.getImageUrl());

        verify(memberRepository, times(1)).findMemberByEmail(email);
        verify(scheduleRepository, times(1)).findAllByMemberOrderByStartDateDesc(member);
        verify(diaryRepository, times(1)).findAllBySchedule(schedule1);
        verify(diaryRepository, times(1)).findAllBySchedule(schedule2);
    }


    @Test
    @DisplayName("일정 수정")
    void updateSchedule() {
        // Given
        Long scheduleId = 1L;

        ScheduleCreateDto scheduleCreateDto = ScheduleCreateDto.builder()
                .name("Updated Schedule")
                .location("Updated Location")
                .startDate("2024-06-15")
                .endDate("2024-06-20")
                .build();

        Schedule existingSchedule = Schedule.builder()
                .scheduleId(scheduleId)
                .name("Original Schedule")
                .location("Original Location")
                .startDate("2024-06-01")
                .endDate("2024-06-10")
                .build();

        when(scheduleRepository.findById(scheduleId)).thenReturn(Optional.of(existingSchedule));
        when(scheduleRepository.save(any(Schedule.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        String result = scheduleService.UpdateSchedule(scheduleCreateDto, scheduleId);

        // Then
        assertEquals("수정 완료", result);

        assertEquals("Updated Schedule", existingSchedule.getName());
        assertEquals("Updated Location", existingSchedule.getLocation());
        assertEquals("2024-06-15", existingSchedule.getStartDate());
        assertEquals("2024-06-20", existingSchedule.getEndDate());

        verify(scheduleRepository, times(1)).findById(scheduleId);
        verify(scheduleRepository, times(1)).save(existingSchedule);
    }


    @Test
    @DisplayName("일정 공개/비공개 설정")
    void changeSharing() throws CustomException {
        // Given
        Long scheduleId = 1L;
        String email = "test@example.com";
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn(email);

        Member member = new Member();
        member.setEmail(email);

        Schedule schedule = Schedule.builder()
                .scheduleId(scheduleId)
                .name("Test Schedule")
                .startDate("2024-06-01")
                .endDate("2024-06-10")
                .location("Test Location")
                .share(false) // 초기 값은 비공개로 설정
                .member(member)
                .build();

        when(scheduleRepository.findById(scheduleId)).thenReturn(Optional.of(schedule));
        when(memberRepository.findMemberByEmail(email)).thenReturn(Optional.of(member));
        when(scheduleRepository.save(any(Schedule.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        String result = scheduleService.ChangeSharing(scheduleId, principal);

        // Then
        assertEquals("일정 공개 처리 완료", result);
        assertTrue(schedule.isShare());

        verify(scheduleRepository, times(1)).findById(scheduleId);
        verify(memberRepository, times(1)).findMemberByEmail(email);
        verify(scheduleRepository, times(1)).save(schedule);

        // 공유 상태를 다시 비공개로 변경하는 경우도 테스트
        schedule.setShare(true);
        when(scheduleRepository.findById(scheduleId)).thenReturn(Optional.of(schedule));

        // When
        result = scheduleService.ChangeSharing(scheduleId, principal);

        // Then
        assertEquals("일정 비공개 처리 완료", result);
        assertFalse(schedule.isShare());

        verify(scheduleRepository, times(2)).findById(scheduleId);
        verify(memberRepository, times(2)).findMemberByEmail(email);
        verify(scheduleRepository, times(2)).save(schedule);
    }

}