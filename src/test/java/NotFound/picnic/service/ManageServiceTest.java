package NotFound.picnic.service;

import NotFound.picnic.domain.*;
import NotFound.picnic.dto.event.AnnounceCreateDto;
import NotFound.picnic.dto.event.EventGetDto;
import NotFound.picnic.dto.manage.*;
import NotFound.picnic.enums.EventType;
import NotFound.picnic.enums.Role;
import NotFound.picnic.enums.State;
import NotFound.picnic.exception.CustomException;
import NotFound.picnic.exception.ErrorCode;
import NotFound.picnic.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.time.LocalDateTime;
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
class ManageServiceTest {

    @Autowired
    private ManageService manageService;

    @MockBean
    private MemberRepository memberRepository;

    @MockBean
    private EventRepository eventRepository;

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

    @Test
    @DisplayName("Approval 리스트 확인")
    void getApprovalList() {
        // Given
        String keyword = "test";
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("testUser");

        Approval approval1 = Approval.builder()
                .approvalId(1L)
                .address("Address 1")
                .content("Content 1")
                .date("2024-06-01")
                .detail("Detail 1")
                .division("Division 1")
                .latitude(37.5665)
                .longitude(126.9780)
                .name("Approval 1")
                .state(State.APPLIED)
                .build();

        Approval approval2 = Approval.builder()
                .approvalId(2L)
                .address("Address 2")
                .content("Content 2")
                .date("2024-06-02")
                .detail("Detail 2")
                .division("Division 2")
                .latitude(37.5665)
                .longitude(126.9780)
                .name("Approval 2")
                .state(State.DENIED)
                .build();

        List<Approval> approvals = Arrays.asList(approval1, approval2);

        when(approvalRepository.findApprovals(keyword)).thenReturn(approvals);

        // When
        List<ApprovalDto> result = manageService.GetApprovalList(keyword, principal);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());

        ApprovalDto dto1 = result.get(0);
        assertEquals(approval1.getApprovalId(), dto1.getApprovalId());
        assertEquals(approval1.getAddress(), dto1.getAddress());
        assertEquals(approval1.getContent(), dto1.getContent());
        assertEquals(approval1.getDate(), dto1.getDate());
        assertEquals(approval1.getDetail(), dto1.getDetail());
        assertEquals(approval1.getDivision(), dto1.getDivision());
        assertEquals(approval1.getLatitude(), dto1.getLatitude());
        assertEquals(approval1.getLongitude(), dto1.getLongitude());
        assertEquals(approval1.getName(), dto1.getName());
        assertEquals(approval1.getState().name().toLowerCase(), dto1.getState());
        assertEquals(principal.getName(), dto1.getUserName());

        ApprovalDto dto2 = result.get(1);
        assertEquals(approval2.getApprovalId(), dto2.getApprovalId());
        assertEquals(approval2.getAddress(), dto2.getAddress());
        assertEquals(approval2.getContent(), dto2.getContent());
        assertEquals(approval2.getDate(), dto2.getDate());
        assertEquals(approval2.getDetail(), dto2.getDetail());
        assertEquals(approval2.getDivision(), dto2.getDivision());
        assertEquals(approval2.getLatitude(), dto2.getLatitude());
        assertEquals(approval2.getLongitude(), dto2.getLongitude());
        assertEquals(approval2.getName(), dto2.getName());
        assertEquals(approval2.getState().name().toLowerCase(), dto2.getState());
        assertEquals(principal.getName(), dto2.getUserName());

        verify(approvalRepository, times(1)).findApprovals(keyword);
    }

    @Test
    @DisplayName("공지사항 작성")
    void createAnnouncement() {
        // Given
        String email = "test@example.com";
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn(email);

        Member member = Member.builder()
                .memberId(1L)
                .name("Test User")
                .email(email)
                .build();

        AnnounceCreateDto announceCreateDto = AnnounceCreateDto.builder()
                .title("Test Announcement")
                .content("This is a test announcement")
                .eventType(EventType.ANNOUNCEMENT)
                .build();

        Event event = Event.builder()
                .eventId(1L)
                .title("Test Announcement")
                .content("This is a test announcement")
                .type(EventType.ANNOUNCEMENT)
                .member(member)
                .build();

        when(memberRepository.findMemberByEmail(email)).thenReturn(Optional.of(member));
        when(eventRepository.save(any(Event.class))).thenReturn(event);

        // When
        String result = manageService.CreateAnnouncement(announceCreateDto, principal);

        // Then
        assertNotNull(result);
        assertEquals("공지 작성 완료", result);

        verify(memberRepository, times(1)).findMemberByEmail(email);
        verify(eventRepository, times(1)).save(any(Event.class));
    }

    @Test
    @DisplayName("장소 승인 완료")
    void approveApproval() {
        // Given
        Long approvalId = 1L;
        ApproveDto approveDto = ApproveDto.builder()
                .address("서울특별시 강남구")
                .name("Approved Location")
                .detail("Location Detail")
                .content("Approval Content")
                .build();

        Approval approval = Approval.builder()
                .approvalId(approvalId)
                .address("서울특별시 강남구")
                .name("Old Name")
                .detail("Old Detail")
                .content("Old Content")
                .state(State.APPLIED)
                .latitude(37.5665)
                .longitude(126.9780)
                .division("숙박")
                .build();

        when(approvalRepository.findById(approvalId)).thenReturn(Optional.of(approval));
        when(locationRepository.save(any(Location.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        String result = manageService.ApproveApproval(approvalId, approveDto);

        // Then
        assertNotNull(result);
        assertEquals("장소 추가 완료", result);

        verify(approvalRepository, times(1)).findById(approvalId);
        verify(approvalRepository, times(1)).save(any(Approval.class));
        verify(locationRepository, times(1)).save(any(Location.class));
        verify(accommodationRepository, times(1)).save(any(Accommodation.class));

        assertEquals(State.APPROVED, approval.getState());
        assertEquals(approveDto.getAddress(), approval.getAddress());
        assertEquals(approveDto.getName(), approval.getName());
        assertEquals(approveDto.getDetail(), approval.getDetail());
        assertEquals(approveDto.getContent(), approval.getContent());
    }

    @Test
    @DisplayName("장소 거절 완료")
    void denyApproval() {
        // Given
        Long approvalId = 1L;

        Approval approval = Approval.builder()
                .approvalId(approvalId)
                .state(State.APPLIED)
                .build();

        when(approvalRepository.findById(approvalId)).thenReturn(Optional.of(approval));

        // When
        String result = manageService.DenyApproval(approvalId);

        // Then
        assertNotNull(result);
        assertEquals("장소 거절이 완료되었습니다.", result);

        verify(approvalRepository, times(1)).findById(approvalId);
        verify(approvalRepository, times(1)).save(approval);

        assertEquals(State.DENIED, approval.getState());
    }

    @Test
    @DisplayName("이벤트 수정 완료")
    void updateEvent() {
        // Given
        Long eventId = 1L;

        AnnounceCreateDto announceCreateDto = AnnounceCreateDto.builder()
                .title("Updated Title")
                .content("Updated Content")
                .build();

        Event event = Event.builder()
                .eventId(eventId)
                .title("Old Title")
                .content("Old Content")
                .build();

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));

        // When
        String result = manageService.UpdateEvent(announceCreateDto, eventId);

        // Then
        assertNotNull(result);
        assertEquals("이벤트 수정 완료", result);

        verify(eventRepository, times(1)).findById(eventId);
        verify(eventRepository, times(1)).save(event);

        assertEquals("Updated Title", event.getTitle());
        assertEquals("Updated Content", event.getContent());
    }

    @Test
    @DisplayName("이벤트 삭제 완료")
    void deleteEvent() {
        // Given
        Long eventId = 1L;

        Event event = Event.builder()
                .eventId(eventId)
                .title("Test Event")
                .content("Test Content")
                .build();

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));

        // When
        String result = manageService.DeleteEvent(eventId);

        // Then
        assertNotNull(result);
        assertEquals("이벤트 삭제 완료", result);

        verify(eventRepository, times(1)).findById(eventId);
        verify(eventRepository, times(1)).delete(event);
    }

    @Test
    @DisplayName("사용자 정보 리스트 불러오기")
    void getUsers() {
        // Given
        String keyword = "test";

        Member member1 = Member.builder()
                .memberId(1L)
                .email("test1@example.com")
                .name("Test User 1")
                .nickname("Nickname1")
                .phone("123-456-7890")
                .role(Role.USER)
                .imageUrl("http://example.com/image1")
                .build();

        Member member2 = Member.builder()
                .memberId(2L)
                .email("test2@example.com")
                .name("Test User 2")
                .nickname("Nickname2")
                .phone("098-765-4321")
                .role(Role.ADMIN)
                .imageUrl("http://example.com/image2")
                .build();

        List<Member> memberList = Arrays.asList(member1, member2);

        when(memberRepository.findMembersBySearch(keyword)).thenReturn(memberList);

        // When
        List<UserGetDto> result = manageService.getUsers(keyword);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());

        UserGetDto user1 = result.get(0);
        assertEquals(member1.getMemberId(), user1.getMemberId());
        assertEquals(member1.getEmail(), user1.getEmail());
        assertEquals(member1.getName(), user1.getName());
        assertEquals(member1.getNickname(), user1.getNickname());
        assertEquals(member1.getPhone(), user1.getPhone());
        assertEquals(member1.getRole(), user1.getRole());
        assertEquals(member1.getImageUrl(), user1.getImageUrl());

        UserGetDto user2 = result.get(1);
        assertEquals(member2.getMemberId(), user2.getMemberId());
        assertEquals(member2.getEmail(), user2.getEmail());
        assertEquals(member2.getName(), user2.getName());
        assertEquals(member2.getNickname(), user2.getNickname());
        assertEquals(member2.getPhone(), user2.getPhone());
        assertEquals(member2.getRole(), user2.getRole());
        assertEquals(member2.getImageUrl(), user2.getImageUrl());

        verify(memberRepository, times(1)).findMembersBySearch(keyword);
    }

    @Test
    @DisplayName("USER -> ADMIN 권한 변경")
    void userRoleChangeUserToAdmin() {
        // Given
        Long memberId = 1L;
        UserRoleChangeDto userRoleChangeDto = UserRoleChangeDto.builder()
                .memberId(memberId)
                .targetRole("ADMIN")
                .build();

        Member member = Member.builder()
                .memberId(memberId)
                .role(Role.USER)
                .build();

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));

        // When
        String result = manageService.UserRoleChange(userRoleChangeDto);

        // Then
        assertNotNull(result);
        assertEquals("권한 변경 완료", result);
        assertEquals(Role.ADMIN, member.getRole());

        verify(memberRepository, times(1)).findById(memberId);
        verify(memberRepository, times(1)).save(member);
    }

    @Test
    @DisplayName("USER -> COMPANY 권한 변경")
    void userRoleChangeUserToCompany() {
        // Given
        Long memberId = 2L;
        UserRoleChangeDto userRoleChangeDto = UserRoleChangeDto.builder()
                .memberId(memberId)
                .targetRole("COMPANY")
                .build();

        Member member = Member.builder()
                .memberId(memberId)
                .role(Role.USER)
                .build();

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));

        // When
        String result = manageService.UserRoleChange(userRoleChangeDto);

        // Then
        assertNotNull(result);
        assertEquals("권한 변경 완료", result);
        assertEquals(Role.COMPANY, member.getRole());

        verify(memberRepository, times(1)).findById(memberId);
        verify(memberRepository, times(1)).save(member);
    }

    @Test
    @DisplayName("사용자 정보 수정")
    void updateUser() {
        // Given
        Long memberId = 1L;
        Long locationId = 2L;

        UserUpdateDto userUpdateDto = UserUpdateDto.builder()
                .name("Updated Name")
                .nickname("Updated Nickname")
                .email("updated@example.com")
                .phone("010-1234-5678")
                .locationId(locationId)
                .build();

        Member member = Member.builder()
                .memberId(memberId)
                .name("Old Name")
                .nickname("Old Nickname")
                .email("old@example.com")
                .phone("010-8765-4321")
                .build();

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(locationRepository.existsById(locationId)).thenReturn(true);

        // When
        String result = manageService.UpdateUser(userUpdateDto, memberId);

        // Then
        assertNotNull(result);
        assertEquals("수정 완료", result);

        assertEquals("Updated Name", member.getName());
        assertEquals("Updated Nickname", member.getNickname());
        assertEquals("updated@example.com", member.getEmail());
        assertEquals("010-1234-5678", member.getPhone());
        assertEquals(locationId, member.getLocationId());

        verify(memberRepository, times(1)).findById(memberId);
        verify(locationRepository, times(1)).existsById(locationId);
        verify(memberRepository, times(1)).save(member);
    }

    @Test
    @DisplayName("사용자 삭제")
    void deleteUser() {
        // Given
        Long memberId = 1L;

        Member member = Member.builder()
                .memberId(memberId)
                .name("Test User")
                .build();

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));

        // When
        String result = manageService.DeleteUser(memberId);

        // Then
        assertNotNull(result);
        assertEquals("삭제 완료", result);

        verify(memberRepository, times(1)).findById(memberId);
        verify(memberRepository, times(1)).delete(member);
    }

    @Test
    @DisplayName("장소 생성")
    void createLocation() {
        // Given
        LocationCreateDto locationCreateDto = LocationCreateDto.builder()
                .name("Test Location")
                .address("서울특별시 강남구")
                .detail("Test Detail")
                .latitude(37.5665)
                .longitude(126.9780)
                .division("숙박")
                .phone("010-1234-5678")
                .build();

        Location location = Location.builder()
                .locationId(1L)
                .name("Test Location")
                .address("서울특별시 강남구")
                .city("서울특별시")
                .detail("Test Detail")
                .latitude(37.5665)
                .longitude(126.9780)
                .division("숙박")
                .phone("010-1234-5678")
                .build();

        when(locationRepository.save(any(Location.class))).thenReturn(location);

        // When
        String result = manageService.CreateLocation(locationCreateDto);

        // Then
        assertNotNull(result);
        assertEquals("장소 생성 완료", result);

        verify(locationRepository, times(1)).save(any(Location.class));
        verify(accommodationRepository, times(1)).save(any(Accommodation.class));
    }

    @Test
    @DisplayName("장소 삭제 완료")
    void deleteLocation() {
        // Given
        Long locationId = 1L;

        Location location = Location.builder()
                .locationId(locationId)
                .division("숙박")
                .build();

        Accommodation accommodation = Accommodation.builder().location(location).build();

        when(locationRepository.findById(locationId)).thenReturn(Optional.of(location));
        when(accommodationRepository.findByLocation_LocationId(locationId)).thenReturn(accommodation);

        // When
        String result = manageService.DeleteLocation(locationId);

        // Then
        assertNotNull(result);
        assertEquals("장소 삭제 완료", result);

        verify(locationRepository, times(1)).findById(locationId);
        verify(accommodationRepository, times(1)).findByLocation_LocationId(locationId);
        verify(accommodationRepository, times(1)).delete(accommodation);
        verify(locationRepository, times(1)).delete(location);
    }

    @Test
    @DisplayName("이벤트 검색")
    void findEvent() {
        // Given
        String div = "eventType";
        String keyword = "keyword";

        Event event1 = Event.builder()
                .eventId(1L)
                .title("Event 1")
                .content("Content 1")
                .createAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .member(Member.builder().name("Member 1").build())
                .build();

        Event event2 = Event.builder()
                .eventId(2L)
                .title("Event 2")
                .content("Content 2")
                .createAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .member(Member.builder().name("Member 2").build())
                .build();

        List<Event> events = Arrays.asList(event1, event2);

        when(eventRepository.findByKeyword(div, keyword)).thenReturn(events);

        // When
        List<EventGetDto> result = manageService.FindEvent(div, keyword);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());

        EventGetDto dto1 = result.get(0);
        assertEquals(event1.getEventId(), dto1.getEventId());
        assertEquals(event1.getContent(), dto1.getContent());
        assertEquals(event1.getTitle(), dto1.getTitle());
        assertEquals(event1.getCreateAt(), dto1.getCreatedDate());
        assertEquals(event1.getModifiedAt(), dto1.getUpdatedDate());
        assertEquals(event1.getMember().getName(), dto1.getMemberName());

        EventGetDto dto2 = result.get(1);
        assertEquals(event2.getEventId(), dto2.getEventId());
        assertEquals(event2.getContent(), dto2.getContent());
        assertEquals(event2.getTitle(), dto2.getTitle());
        assertEquals(event2.getCreateAt(), dto2.getCreatedDate());
        assertEquals(event2.getModifiedAt(), dto2.getUpdatedDate());
        assertEquals(event2.getMember().getName(), dto2.getMemberName());

        verify(eventRepository, times(1)).findByKeyword(div, keyword);
    }
}