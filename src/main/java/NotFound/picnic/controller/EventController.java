package NotFound.picnic.controller;

import NotFound.picnic.dto.event.EventCreateDto;
import NotFound.picnic.dto.event.EventDetailGetDto;
import NotFound.picnic.dto.event.EventGetDto;
import NotFound.picnic.dto.event.EventUpdateDto;
import NotFound.picnic.enums.EventType;
import NotFound.picnic.service.EventService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/event")
public class EventController {
    private final EventService eventService;

    
    @GetMapping("/event")
    public ResponseEntity<List<EventGetDto>> getEvents() {
        List<EventGetDto> eventGetDtoList = eventService.GetEvents(EventType.EVENT);
        return ResponseEntity.ok().body(eventGetDtoList);
    }

    @GetMapping("/announce")
    public ResponseEntity<List<EventGetDto>> getAnnounces() {
        List<EventGetDto> AnnounceGetDtoList =eventService.GetEvents(EventType.ANNOUNCEMENT) ;
        return ResponseEntity.ok().body(AnnounceGetDtoList);
    }
    @GetMapping("/promotion")
    public ResponseEntity<List<EventGetDto>> getPromotions() {
        List<EventGetDto> PromotionGetDtoList =eventService.GetEvents(EventType.PROMOTION) ;
        return ResponseEntity.ok().body(PromotionGetDtoList);
    }
    
    @GetMapping("/event/{eventId}")
    public ResponseEntity<EventDetailGetDto> getEventDetail(@PathVariable(name="eventId") Long eventId) {
        EventDetailGetDto eventDetailGetDto = eventService.GetEventDetail(eventId);
        return ResponseEntity.ok().body(eventDetailGetDto);
    }

    @GetMapping("/announce/{eventId}")
    public ResponseEntity<EventDetailGetDto> getAnnounceDetail(@PathVariable(name="eventId") Long eventId) {
        EventDetailGetDto AnnounceDetailGetDto = eventService.GetEventDetail(eventId);
        return ResponseEntity.ok().body(AnnounceDetailGetDto);
    }

    @GetMapping("/promotion/{eventId}")
    public ResponseEntity<EventDetailGetDto> getPromotionDetail(@PathVariable(name="eventId") Long eventId) {
        EventDetailGetDto PromotionDetailGetDto = eventService.GetEventDetail(eventId);
        return ResponseEntity.ok().body(PromotionDetailGetDto);
    }
    

    @PreAuthorize("hasRole('COMPANY')")
    @PostMapping()
    public ResponseEntity<String> creatEvent(EventCreateDto eventCreateDto, Principal principal) {
        String Response = eventService.createEvent(eventCreateDto, principal);

        return ResponseEntity.ok().body(Response);
    }

    @PreAuthorize("hasRole('COMPANY')")
    @PatchMapping("/{eventId}")
    public ResponseEntity<String> updateEvent(@PathVariable(name="eventId") Long eventId, EventUpdateDto eventUpdateDto, Principal principal) {
        String res = eventService.UpdateEvent(eventId, eventUpdateDto, principal);
        return ResponseEntity.ok().body(res);
    }
    




}
