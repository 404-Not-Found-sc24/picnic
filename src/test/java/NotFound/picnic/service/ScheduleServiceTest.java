package NotFound.picnic.service;

import NotFound.picnic.domain.*;
import NotFound.picnic.dto.schedule.*;
import NotFound.picnic.exception.CustomException;
import NotFound.picnic.exception.ErrorCode;
import NotFound.picnic.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.security.Principal;
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

    @BeforeEach
    public void setUp() {
        // No need for MockitoAnnotations.openMocks(this) when using @SpringBootTest with @MockBean
    }

    @Test
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
    void createDiary() {
    }

    @Test
    void updateDiary() {
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