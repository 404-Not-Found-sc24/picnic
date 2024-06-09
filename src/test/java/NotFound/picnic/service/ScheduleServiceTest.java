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

import java.security.Principal;
import java.util.ArrayList;
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
    private S3Upload s3Upload;

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
    void createLocations() {
    }

    @Test
    void getSchedulePlaceDiary() {
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
    void deleteDiary() {
    }

    @Test
    void getPlaces() {
    }

    @Test
    void getSchedulesInMyPage() {
    }

    @Test
    void deleteSchedule() {
    }

    @Test
    void deletePlace() {
    }

    @Test
    void getSchedules() {
    }

    @Test
    void updateSchedule() {
    }

    @Test
    void changeSharing() {
    }
}