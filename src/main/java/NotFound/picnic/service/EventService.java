package NotFound.picnic.service;

import NotFound.picnic.domain.*;
import NotFound.picnic.repository.*;
import NotFound.picnic.dto.*;
import NotFound.picnic.enums.EventType;

import org.springframework.web.multipart.MultipartFile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.security.Principal;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventService {
    private final EventRepository eventRepository;
    private final EventImageRepository eventImageRepository;
    private final LocationRepository locationRepository;
    private final MemberRepository memberRepository;
    private final S3Upload s3Upload;

    public List<EventGetDto> GetEvents(int Type) throws UnsupportedEncodingException{
        EventType eventType;
        if (Type ==1){
            eventType = EventType.ANNOUNCEMENT;
        }
        else if (Type == 2){
            eventType = EventType.PROMOTION;
        }
        else {
            eventType=EventType.EVENT;
        }
        
        List<Event> events =eventRepository.findAllByType(eventType);

        events.sort(Comparator.comparing(Event::getCreateAt).reversed());
        
        return events.stream().map(event -> EventGetDto.builder()
                .eventId(event.getEventId())
                .title(event.getTitle())
                .content(event.getContent())
                .createdDate(event.getCreateAt())
                .updatedDate(event.getModifiedAt())
                .memberName(event.getMember().getName())
                .build())
                .collect(Collectors.toList());
                
    }

    public EventDetailGetDto GetEventDetail(Long eventId,int Type){
        EventType eventType;
        if (Type ==1){
            eventType = EventType.ANNOUNCEMENT;
        }
        else if (Type == 2){
            eventType = EventType.PROMOTION;
        }
        else {
            eventType=EventType.EVENT;
        }
        List<Event> events =eventRepository.findAllByType(eventType);
        Event event = events.stream()
        .filter(e -> e.getEventId().equals(eventId))
        .findFirst()
        .orElseThrow(() -> new RuntimeException("해당 정보를 찾을 수 없습니다."));

        List<EventImageDto> imageDtos = event.getEventImageList().stream()
                                          .map(image -> EventImageDto.builder()
                                          .imageUrl(image.getImageUrl())
                                          .build())
                                          .collect(Collectors.toList());
        

        return EventDetailGetDto.builder()
                                    .eventId(event.getEventId())
                                    .locationId(event.getLocation().getLocationId())
                                    .title(event.getTitle())
                                    .content(event.getContent())
                                    .createdDate(event.getCreateAt())
                                    .updatedDate(event.getModifiedAt())
                                    .memberName(event.getMember().getName())
                                    .images(imageDtos)
                                    .build();
       
    }



    public String createEvent(EventCreateDto eventCreateDto, Principal principal){

        Optional<Member> optionalMember = memberRepository.findMemberByEmail(principal.getName());

        if (optionalMember.isEmpty()) {
            throw new UsernameNotFoundException("유저가 존재하지 않습니다.");
        }
        Member member = optionalMember.get();

        EventType eventType = EventType.valueOf(eventCreateDto.getEventType());
        Optional<Location> optionalLocation = locationRepository.findById(eventCreateDto.getLocationId());
        Location location = optionalLocation.get();
        // type이 공지일 경우와 location을 못 찾았을 경우의 예외처리
        // git pull 후 member enum 확인




        Event event = Event.builder()
        .title(eventCreateDto.getTitle())
        .content(eventCreateDto.getContent())
        .member(member)
        .location(location)
        .type(eventType)
        .build();

        event.prePersist();

        

        List<MultipartFile> images = eventCreateDto.getImages();
        if (images != null) {
            images.forEach(image -> {
                try {
                    String url = s3Upload.uploadFiles(image, "event");
                    EventImage img = EventImage.builder()
                            .event(event)
                            .imageUrl(url)
                            .build();

                    
                    event.getEventImageList().add(img);
                    eventImageRepository.save(img);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            });

        }
        eventRepository.save(event);

        return "hi";

    }
    

}
