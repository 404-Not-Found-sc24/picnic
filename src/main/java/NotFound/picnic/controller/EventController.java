package NotFound.picnic.controller;

import NotFound.picnic.dto.EventGetDto;
import NotFound.picnic.enums.EventType;
import NotFound.picnic.dto.EventDetailGetDto;
import NotFound.picnic.dto.EventCreateDto;
import NotFound.picnic.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.List;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/event")
public class EventController {
    private final EventService eventService;
    EventType eventType;

    
    @GetMapping("/event")
    public ResponseEntity<List<EventGetDto>> getEvents() throws UnsupportedEncodingException{
        List<EventGetDto> eventGetDtoList = eventService.GetEvents(0);
        return ResponseEntity.ok().body(eventGetDtoList);
    }

    @GetMapping("/announce")
    public ResponseEntity<List<EventGetDto>> getAnnounces() throws UnsupportedEncodingException{
        List<EventGetDto> AnnounceGetDtoList =eventService.GetEvents(1) ;
        return ResponseEntity.ok().body(AnnounceGetDtoList);
    }
    @GetMapping("/promotion")
    public ResponseEntity<List<EventGetDto>> getPromotions() throws UnsupportedEncodingException{
        List<EventGetDto> PromotionGetDtoList =eventService.GetEvents(2) ;
        return ResponseEntity.ok().body(PromotionGetDtoList);
    }
    
    @GetMapping("/event/{eventId}")
    public ResponseEntity<EventDetailGetDto> getEventDetail(@PathVariable(name="eventId") Long eventId) throws UnsupportedEncodingException{
        EventDetailGetDto eventDetailGetDto = eventService.GetEventDetail(eventId,0);
        return ResponseEntity.ok().body(eventDetailGetDto);
    }

    @GetMapping("/announce/{eventId}")
    public ResponseEntity<EventDetailGetDto> getAnnounceDetail(@PathVariable(name="eventId") Long eventId) throws UnsupportedEncodingException{
        EventDetailGetDto AnnounceDetailGetDto = eventService.GetEventDetail(eventId,1);
        return ResponseEntity.ok().body(AnnounceDetailGetDto);
    }

    @GetMapping("/promotion/{eventId}")
    public ResponseEntity<EventDetailGetDto> getPromotionDetail(@PathVariable(name="eventId") Long eventId) throws UnsupportedEncodingException{
        EventDetailGetDto PromotionDetailGetDto = eventService.GetEventDetail(eventId,2);
        return ResponseEntity.ok().body(PromotionDetailGetDto);
    }
    

}
