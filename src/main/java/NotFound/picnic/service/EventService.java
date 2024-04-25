package NotFound.picnic.service;

import NotFound.picnic.domain.Event;
import NotFound.picnic.domain.EventImage;
import NotFound.picnic.repository.EventRepository;
import NotFound.picnic.repository.EventImageRepository;
import NotFound.picnic.dto.EventGetDto;
import NotFound.picnic.dto.EventDetailGetDto;
import NotFound.picnic.dto.EventCreateDto;
import NotFound.picnic.dto.EventImageDto;
import NotFound.picnic.enums.EventType;
import NotFound.picnic.domain.Member;
import NotFound.picnic.repository.MemberRepository;

import org.springframework.web.multipart.MultipartFile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private final S3Upload s3Upload;

    public List<EventGetDto> GetEvents(int Type) throws UnsupportedEncodingException{
        
        List<Event> events =eventRepository.findAllByType(Type);

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
        List<Event> events =eventRepository.findAllByType(Type);
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


    /* 

    public String createEvent(EventCreateDto eventCreateDto, Principal princial){

        Event event = Event.builder()
        .title(eventCreateDto.getTitle())
        .content(eventCreateDto.getContent())
        .build();

        eventRepository.save(event);

        List<MultipartFile> images = eventCreateDto.getImages();
        if (images != null) {
            images.forEach(image -> {
                try {
                    String url = s3Upload.uploadFiles(image, "event");
                    EventImage img = EventImage.builder()
                            .event(event)
                            .imageUrl(url)
                            .build();

                    eventImageRepository.save(img);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            });

        }

        return "hi";

    }
    */
    

}
