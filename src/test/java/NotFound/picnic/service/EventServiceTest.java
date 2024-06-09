package NotFound.picnic.service;

import NotFound.picnic.domain.*;
import NotFound.picnic.dto.event.*;
import NotFound.picnic.enums.EventType;
import NotFound.picnic.repository.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-dev.properties")
class EventServiceTest {

    @Autowired
    private EventService eventService;

    @MockBean
    private EventRepository eventRepository;

    @MockBean
    private EventImageRepository eventImageRepository;

    @MockBean
    private MemberRepository memberRepository;

    @MockBean
    private LocationRepository locationRepository;

    @Test
    @DisplayName("이벤트 목록 가져오기")
    void getEvents() {
        // Given
        EventType type = EventType.EVENT;

        Member member = Member.builder()
                .memberId(1L)
                .name("Test User")
                .build();

        Event event1 = Event.builder()
                .eventId(1L)
                .title("Event 1")
                .content("Content 1")
                .type(type)
                .createAt(LocalDateTime.of(2024, 6, 1, 10, 0))
                .modifiedAt(LocalDateTime.of(2024, 6, 1, 12, 0))
                .member(member)
                .build();

        Event event2 = Event.builder()
                .eventId(2L)
                .title("Event 2")
                .content("Content 2")
                .type(type)
                .createAt(LocalDateTime.of(2024, 6, 2, 10, 0))
                .modifiedAt(LocalDateTime.of(2024, 6, 2, 12, 0))
                .member(member)
                .build();

        List<Event> events = Arrays.asList(event1, event2);

        when(eventRepository.findAllByType(type)).thenReturn(events);

        // When
        List<EventGetDto> result = eventService.GetEvents(type);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());

        EventGetDto dto1 = result.get(0);
        assertEquals(event2.getEventId(), dto1.getEventId());
        assertEquals(event2.getTitle(), dto1.getTitle());
        assertEquals(event2.getContent(), dto1.getContent());
        assertEquals(event2.getCreateAt(), dto1.getCreatedDate());
        assertEquals(event2.getModifiedAt(), dto1.getUpdatedDate());
        assertEquals(member.getName(), dto1.getMemberName());

        EventGetDto dto2 = result.get(1);
        assertEquals(event1.getEventId(), dto2.getEventId());
        assertEquals(event1.getTitle(), dto2.getTitle());
        assertEquals(event1.getContent(), dto2.getContent());
        assertEquals(event1.getCreateAt(), dto2.getCreatedDate());
        assertEquals(event1.getModifiedAt(), dto2.getUpdatedDate());
        assertEquals(member.getName(), dto2.getMemberName());

        verify(eventRepository, times(1)).findAllByType(type);
    }


    @Test
    @DisplayName("이벤트 상세 정보 가져오기")
    void getEventDetail() {
        // Given
        Long eventId = 1L;

        Member member = Member.builder()
                .memberId(1L)
                .name("Test User")
                .build();

        Location location = Location.builder()
                .locationId(1L)
                .name("Test Location")
                .build();

        EventImage eventImage = EventImage.builder()
                .imageUrl("http://testurl.com/image1")
                .build();

        Event event = Event.builder()
                .eventId(eventId)
                .title("Event Title")
                .content("Event Content")
                .createAt(LocalDateTime.of(2024, 6, 1, 10, 0))
                .modifiedAt(LocalDateTime.of(2024, 6, 1, 12, 0))
                .member(member)
                .location(location)
                .eventImageList(Arrays.asList(eventImage))
                .build();

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(eventImageRepository.findEventImageByEvent_EventId(eventId)).thenReturn(Optional.of(eventImage));

        // When
        EventDetailGetDto result = eventService.GetEventDetail(eventId);

        // Then
        assertNotNull(result);
        assertEquals(event.getEventId(), result.getEventId());
        assertEquals(event.getLocation().getLocationId(), result.getLocationId());
        assertEquals(event.getTitle(), result.getTitle());
        assertEquals(event.getContent(), result.getContent());
        assertEquals(event.getCreateAt(), result.getCreatedDate());
        assertEquals(event.getModifiedAt(), result.getUpdatedDate());
        assertEquals(member.getName(), result.getMemberName());
        assertEquals(eventImage.getImageUrl(), result.getImageUrl());

        verify(eventRepository, times(1)).findById(eventId);
        verify(eventImageRepository, times(1)).findEventImageByEvent_EventId(eventId);
    }


    @Test
    @DisplayName("이벤트 생성")
    void createEvent() {
        // Given
        String email = "test@example.com";
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn(email);

        Member member = Member.builder()
                .memberId(1L)
                .name("Test User")
                .email(email)
                .locationId(1L)
                .build();

        Location location = Location.builder()
                .locationId(1L)
                .name("Test Location")
                .build();

        EventCreateDto eventCreateDto = EventCreateDto.builder()
                .title("Test Event")
                .content("Test Content")
                .eventType(EventType.EVENT)
                .build();

        Event event = Event.builder()
                .eventId(1L)
                .title("Test Event")
                .content("Test Content")
                .member(member)
                .location(location)
                .type(EventType.EVENT)
                .build();

        when(memberRepository.findMemberByEmail(email)).thenReturn(Optional.of(member));
        when(locationRepository.findById(member.getLocationId())).thenReturn(Optional.of(location));
        when(eventRepository.save(any(Event.class))).thenReturn(event);

        // When
        String result = eventService.createEvent(eventCreateDto, principal);

        // Then
        assertNotNull(result);

        verify(memberRepository, times(1)).findMemberByEmail(email);
        verify(locationRepository, times(1)).findById(member.getLocationId());
        verify(eventRepository, times(1)).save(any(Event.class));
    }


    @Test
    @DisplayName("이벤트 수정")
    void updateEvent() {
        // Given
        Long eventId = 1L;
        String email = "test@example.com";
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn(email);

        Member member = Member.builder()
                .memberId(1L)
                .name("Test User")
                .email(email)
                .locationId(1L)
                .build();

        Location location = Location.builder()
                .locationId(1L)
                .name("Test Location")
                .build();

        Event event = Event.builder()
                .eventId(eventId)
                .title("Old Event Title")
                .content("Old Event Content")
                .member(member)
                .location(location)
                .type(EventType.EVENT)
                .build();

        EventUpdateDto eventUpdateDto = EventUpdateDto.builder()
                .title("Updated Event Title")
                .content("Updated Event Content")
                .build();

        when(memberRepository.findMemberByEmail(email)).thenReturn(Optional.of(member));
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));

        // When
        String result = eventService.UpdateEvent(eventId, eventUpdateDto, principal);

        // Then
        assertNotNull(result);
        assertEquals("이벤트 수정 완료", result);

        verify(memberRepository, times(1)).findMemberByEmail(email);
        verify(eventRepository, times(1)).findById(eventId);
        verify(eventRepository, times(1)).save(event);

        assertEquals(eventUpdateDto.getTitle(), event.getTitle());
        assertEquals(eventUpdateDto.getContent(), event.getContent());
    }

    @Test
    @DisplayName("이벤트 삭제")
    void deleteEvent() {
        // Given
        Long eventId = 1L;
        String email = "test@example.com";
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn(email);

        Member member = Member.builder()
                .memberId(1L)
                .name("Test User")
                .email(email)
                .build();

        Location location = Location.builder()
                .locationId(1L)
                .name("Test Location")
                .build();

        Event event = Event.builder()
                .eventId(eventId)
                .title("Event Title")
                .content("Event Content")
                .member(member)
                .location(location)
                .type(EventType.EVENT)
                .build();

        when(memberRepository.findMemberByEmail(email)).thenReturn(Optional.of(member));
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));

        // When
        String result = eventService.DeleteEvent(eventId, principal);

        // Then
        assertNotNull(result);
        assertEquals("이벤트 삭제 완료", result);

        verify(memberRepository, times(1)).findMemberByEmail(email);
        verify(eventRepository, times(1)).findById(eventId);
        verify(eventRepository, times(1)).delete(event);
    }
}