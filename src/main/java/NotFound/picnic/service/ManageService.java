package NotFound.picnic.service;

import NotFound.picnic.domain.*;
import NotFound.picnic.dto.AnnounceCreateDto;
import NotFound.picnic.dto.ApprovalDto;
import NotFound.picnic.enums.State;
import NotFound.picnic.repository.ApprovalRepository;
import NotFound.picnic.repository.EventImageRepository;
import NotFound.picnic.repository.EventRepository;
import NotFound.picnic.repository.MemberRepository;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ManageService {

    private final ApprovalRepository approvalRepository;
    private final EventRepository eventRepository;
    private final EventImageRepository eventImageRepository;
    private final MemberRepository memberRepository;
    private final S3Upload s3Upload;

    public List<ApprovalDto> GetApprovalList(Principal principal){
        List<Approval> approvals = approvalRepository.findAll();
        List<ApprovalDto> approvalDtos = new ArrayList<>();
        for(Approval approval:approvals){
            ApprovalDto approvalDto = ApprovalDto.builder()
                    .approvalId(approval.getApprovalId())
                    .address(approval.getAddress())
                    .content(approval.getContent())
                    .date(approval.getDate())
                    .detail(approval.getDetail())
                    .division(approval.getDivision())
                    .latitude(approval.getLatitude())
                    .longitude(approval.getLongitude())
                    .name(approval.getName())
                    .state(approval.getState().name().toLowerCase())
                    .userName(principal.getName())
                    .build();
            approvalDtos.add(approvalDto);
        }
        
        return approvalDtos;
    }

    public String CreateAnnouncement (AnnounceCreateDto announceCreateDto, Principal principal) {
        Member member = memberRepository.findMemberByEmail(principal.getName()).orElseThrow();

        Event event = Event.builder()
                .title(announceCreateDto.getTitle())
                .content(announceCreateDto.getContent())
                .type(announceCreateDto.getEventType())
                .member(member)
                .build();
        eventRepository.save(event);

        saveEventImages(announceCreateDto.getImages(), event);

        return "공지 작성 완료";
    }

    public String UpdateAnnouncement(AnnounceCreateDto announceCreateDto, Long eventId, Principal principal) {
        Member member = memberRepository.findMemberByEmail(principal.getName()).orElseThrow();

        Event event = eventRepository.findById(eventId).orElseThrow();
        if (event.getMember() != member)
            throw new ValidationException();

        if (announceCreateDto.getTitle() != null) event.setTitle(announceCreateDto.getTitle());
        if (announceCreateDto.getContent() != null) event.setContent(announceCreateDto.getContent());
        eventRepository.save(event);

        if (announceCreateDto.getImages() != null) {
            List<EventImage> eventImageList = eventImageRepository.findAllByEvent(event);

            if (eventImageList != null)
                eventImageRepository.deleteAll(eventImageList);

            saveEventImages(announceCreateDto.getImages(), event);
        }

        return "공지 수정 완료";
    }

    private void saveEventImages(List<MultipartFile> images, Event event) {
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
    }

    public String DeleteAnnouncement(Long eventId, Principal principal) {
        Member member = memberRepository.findMemberByEmail(principal.getName()).orElseThrow();
        Event event = eventRepository.findById(eventId).orElseThrow();

        if (event.getMember() != member)
            throw new ValidationException();

        List<EventImage> eventList = eventImageRepository.findAllByEvent(event);
        eventImageRepository.deleteAll(eventList);
        eventRepository.delete(event);
        return "공지 삭제 완료";
    }
}
