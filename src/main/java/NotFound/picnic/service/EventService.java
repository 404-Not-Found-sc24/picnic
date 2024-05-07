package NotFound.picnic.service;

import NotFound.picnic.domain.*;
import NotFound.picnic.repository.*;
import NotFound.picnic.dto.*;
import NotFound.picnic.enums.*;

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
        
        
        List<Event> events =eventRepository.findAllByType(CheckEventType(Type));

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
        
        List<Event> events =eventRepository.findAllByType(CheckEventType(Type));
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
        if(Role.USER==member.getRole()){
            return "You are not allowed";
        }

        
        if(EventType.ANNOUNCEMENT==eventCreateDto.getEventType()){
            return "You are not allowed";
        }
        Optional<Location> optionalLocation = locationRepository.findById(eventCreateDto.getLocationId());
        Location location=optionalLocation.orElseThrow(() -> new IllegalArgumentException("해당하는 장소를 찾을 수 없습니다."));
        Event event = Event.builder()
        .title(eventCreateDto.getTitle())
        .content(eventCreateDto.getContent())
        .member(member)
        .location(location)
        .type(eventCreateDto.getEventType())
        .build();

        event.prePersist();
        eventRepository.save(event);

        
        List<MultipartFile> images = eventCreateDto.getImages();
         
             images.forEach(image -> {
                try {
                    if (!image.isEmpty()){
                    String url = s3Upload.uploadFiles(image, "event");
                    EventImage img = EventImage.builder()
                            .event(event)
                            .imageUrl(url)
                            .build();

                    eventImageRepository.save(img);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            });
            
        

        return returnMessage(eventCreateDto.getEventType());

    }

    public EventType CheckEventType(int Type){

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

        return eventType;
    }

    public String returnMessage(EventType eventType){
         String message="이벤트";

        if(eventType==EventType.EVENT){
            ;
        }

        else if(eventType==EventType.PROMOTION){
            message="홍보자료";
        }
        else if(eventType==EventType.ANNOUNCEMENT){
            message="공지사항";
        }


        String returnMessage = String.format("%s(이)가 성공적으로 업로드 되었습니다", message);

        return returnMessage;

    }
    


}
