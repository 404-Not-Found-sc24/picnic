package NotFound.picnic.service;

import NotFound.picnic.domain.*;
import NotFound.picnic.dto.*;
import NotFound.picnic.enums.*;
import NotFound.picnic.repository.*;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
public class ManageService {

    private final ApprovalRepository approvalRepository;
    private final EventRepository eventRepository;
    private final EventImageRepository eventImageRepository;
    private final MemberRepository memberRepository;
    private final LocationRepository locationRepository;
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
    

    public String ApproveApproval(Long approvalId, ApproveDto approveDto){
        Approval approval = approvalRepository.findById(approvalId).orElseThrow();
        if(approval.getState() != State.APPLIED){
            return "승인할 수 없는 장소입니다.";
        }
        approval.setAddress(approveDto.getAddress());
        approval.setName(approveDto.getName());
        approval.setDetail(approveDto.getDetail());
        approval.setContent(approveDto.getContent());
        approval.setState(State.APPROVED);

        approvalRepository.save(approval);

        String city = approval.getAddress().split(" ")[0];
        String[] strArray = {"서울특별시", "인천광역시", "대구광역시", "대전광역시", "부산광역시", "울산광역시", "세종특별자치시", "제주특별자치도"};
        List<String> strList = new ArrayList<>(Arrays.asList(strArray));
        if(!strList.contains(city)) {
            city = city + " " +approval.getAddress().split(" ")[1];
        }
        Location location = Location.builder()
                .name(approval.getName())
                .address(approval.getAddress())
                .city(city)
                .detail(approval.getDetail())
                .latitude(approval.getLatitude())
                .longitude(approval.getLongitude())
                .division(approval.getDivision())
                .phone(approval.getPhone())
                .build();
        locationRepository.save(location);

        return "장소 추가 완료";
    }

    public String DenyApproval(Long approvalId){

        Approval approval = approvalRepository.findById(approvalId).orElseThrow();
        if(approval.getState() != State.APPLIED){
            return "거절할 수 없는 장소입니다.";
        }
        approval.setState(State.DENIED);
        approvalRepository.save(approval);

        return "장소 거절이 완료되었습니다.";
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

    public List<UserGetDto> getUsers() {
        List<Member> memberList = memberRepository.findAll();

        return memberList.stream().map(member -> UserGetDto.builder()
                .memberId(member.getMemberId())
                .email(member.getEmail())
                .name(member.getName())
                .nickname(member.getNickname())
                .phone(member.getPhone())
                .role(member.getRole())
                .imageUrl(member.getImageUrl())
                .build())
                .collect(Collectors.toList());
    }

    public String UserRoleChange(UserRoleChangeDto userRoleChangeDto){
        Member member = memberRepository.findById(userRoleChangeDto.getMemberId()).orElseThrow();
        switch (userRoleChangeDto.getTargetRole()){
            case "ADMIN":
                member.setRole(Role.ADMIN);
                memberRepository.save(member);
                break;
            case "COMPANY":
                member.setRole(Role.COMPANY);
                memberRepository.save(member);
                break;
            case "USER":
                member.setRole(Role.USER);
                memberRepository.save(member);
                break;
        }

        return "권한 변경 완료";
    }

    public String UpdateEvent(EventCreateDto eventCreateDto, Long eventId, Principal principal, int Type) {
        Member member = memberRepository.findMemberByEmail(principal.getName()).orElseThrow();
        

        Event event =eventRepository.findByEventIdAndType(eventId,CheckEventType(Type)).orElseThrow();

        
        if (event.getMember() != member)
            throw new ValidationException();

        if (eventCreateDto.getTitle() != null) event.setTitle(eventCreateDto.getTitle());
        if (eventCreateDto.getContent() != null) event.setContent(eventCreateDto.getContent());
        //관리자가 LocationId를 수정시 해당 event와 event와 같은 locationId를 가진 member(COMPANY)도 수정합니다.
        if (eventCreateDto.getLocationId() !=null) {
            Member memberWhoCompany =memberRepository.findById(event.getLocation().getLocationId()).orElseThrow();
            Location location =locationRepository.findById(eventCreateDto.getLocationId()).orElseThrow();
            event.setLocation(location);
            
            memberWhoCompany.setLocationId(eventCreateDto.getLocationId());
        }
        eventRepository.save(event);

        if (EventCreateDtoImagesCheck(eventCreateDto)) {

           
            
            List<EventImage> eventImageList = eventImageRepository.findAllByEvent(event);

            if (EventImageListCheck(eventImageList))
                eventImageRepository.deleteAll(eventImageList);
                
            
            

           saveEventImages(eventCreateDto.getImages(), event);
        }
        
        

       return returnMessage(event.getType(),"수정");
    }

    public String DeleteEvent(Long eventId, Principal principal,int Type) {
        Event event = eventRepository.findById(eventId).orElseThrow();

        List<EventImage> eventList = eventImageRepository.findAllByEvent(event);
        eventImageRepository.deleteAll(eventList);
        eventRepository.delete(event);
        return returnMessage(event.getType(),"삭제");
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

    public boolean EventCreateDtoImagesCheck(EventCreateDto eventCreateDto){
        List<MultipartFile> images = eventCreateDto.getImages();
         
        boolean anyImageNotEmpty = images.stream().anyMatch(image -> !image.isEmpty());

        return anyImageNotEmpty;
    }

    public boolean EventImageListCheck(List<EventImage> eventImageList){
        
        boolean anyImageNotEmpty = eventImageList.stream().anyMatch(image -> !image.getImageUrl().isEmpty());

        return anyImageNotEmpty;
    }

    public String returnMessage(EventType eventType, String propose){
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



       String returnMessage = String.format("%s(이)가 성공적으로 %s 되었습니다", message,propose);

       return returnMessage;

   }
}
