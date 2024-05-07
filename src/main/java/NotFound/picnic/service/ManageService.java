package NotFound.picnic.service;

import NotFound.picnic.domain.*;
import NotFound.picnic.dto.AnnotationCreateDto;
import NotFound.picnic.dto.ApprovalDto;
import NotFound.picnic.enums.State;
import NotFound.picnic.repository.ApprovalRepository;
import NotFound.picnic.repository.EventImageRepository;
import NotFound.picnic.repository.EventRepository;
import NotFound.picnic.repository.MemberRepository;
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
            if(approval.getState() != State.APPLIED){
                continue;
            }
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
                    .userName(principal.getName())
                    .build();
            approvalDtos.add(approvalDto);
        }
        
        return approvalDtos;
    }

    public String CreateAnnotation (AnnotationCreateDto annotationCreateDto, Principal principal) {
        Member member = memberRepository.findMemberByEmail(principal.getName()).orElseThrow();

        Event event = Event.builder()
                .title(annotationCreateDto.getTitle())
                .content(annotationCreateDto.getContent())
                .type(annotationCreateDto.getEventType())
                .member(member)
                .build();
        eventRepository.save(event);

        List<MultipartFile> images = annotationCreateDto.getImages();
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

        return "공지 작성 완료";
    }
}
