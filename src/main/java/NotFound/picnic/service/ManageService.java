package NotFound.picnic.service;

import NotFound.picnic.domain.*;
import NotFound.picnic.dto.event.AnnounceCreateDto;
import NotFound.picnic.dto.event.EventCreateDto;
import NotFound.picnic.dto.manage.ApprovalDto;
import NotFound.picnic.dto.manage.ApproveDto;
import NotFound.picnic.dto.manage.UserGetDto;
import NotFound.picnic.dto.manage.UserRoleChangeDto;
import NotFound.picnic.enums.*;
import NotFound.picnic.exception.CustomException;
import NotFound.picnic.exception.ErrorCode;
import NotFound.picnic.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
public class ManageService {

    private final ApprovalRepository approvalRepository;
    private final ApprovalImageRepository approvalImageRepository;
    private final EventRepository eventRepository;
    private final EventImageRepository eventImageRepository;
    private final MemberRepository memberRepository;
    private final LocationRepository locationRepository;
    private final LocationImageRepostiory locationImageRepostiory;
    private final AccommodationRepository accommodationRepository;
    private final CultureRepository cultureRepository;
    private final FestivalRepository festivalRepository;
    private final LeisureRepository leisureRepository;
    private final RestaurantRepository restaurantRepository;
    private final ShoppingRepository shoppingRepository;
    private final TourRepository tourRepository;
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
        Member member = memberRepository.findMemberByEmail(principal.getName())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

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
        Approval approval = approvalRepository.findById(approvalId)
                .orElseThrow(() -> new CustomException(ErrorCode.APPROVAL_NOT_FOUND));

        if(approval.getState() != State.APPLIED){
            throw new CustomException(ErrorCode.APPROVAL_FAILED);
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

        switch(approval.getDivision()){
            case "숙박":
                Accommodation accommodation = Accommodation.builder()
                        .location(location)
                        .build();
                accommodationRepository.save(accommodation);
                break;
            case "문화시설":
                Culture culture = Culture.builder()
                        .location(location)
                        .build();
                cultureRepository.save(culture);
                break;
            case "축제 공연 행사":
                Festival festival = Festival.builder()
                        .location(location)
                        .build();
                festivalRepository.save(festival);
                break;
            case "레포츠":
                Leisure leisure = Leisure.builder()
                        .location(location)
                        .build();
                leisureRepository.save(leisure);
                break;
            case "음식점":
                Restaurant restaurant = Restaurant.builder()
                        .location(location)
                        .build();
                restaurantRepository.save(restaurant);
                break;
            case "쇼핑":
                Shopping shopping = Shopping.builder()
                        .location(location)
                        .build();
                shoppingRepository.save(shopping);
                break;
            case "관광지":
                Tour tour = Tour.builder()
                        .location(location)
                        .build();
                tourRepository.save(tour);
                break;
        }

        Optional<ApprovalImage> approvalImage = approvalImageRepository.findApprovalImageByApproval_ApprovalId(approvalId);
        if(approvalImage.isEmpty()){
            return "장소 추가 완료";
        }
        ApprovalImage image = approvalImage.get();
        LocationImage locationImage = LocationImage.builder()
                .imageUrl(image.getImageUrl())
                .location(location)
                .build();
        locationImageRepostiory.save(locationImage);

        return "장소 추가 완료";
    }

    public String DenyApproval(Long approvalId){

        Approval approval = approvalRepository.findById(approvalId)
                .orElseThrow(() -> new CustomException(ErrorCode.APPROVAL_NOT_FOUND));
        if(approval.getState() != State.APPLIED){
            throw new CustomException(ErrorCode.APPROVAL_FAILED);
        }
        approval.setState(State.DENIED);
        approvalRepository.save(approval);

        return "장소 거절이 완료되었습니다.";
    }

    public String UpdateAnnouncement(AnnounceCreateDto announceCreateDto, Long eventId, Principal principal) {
        Member member = memberRepository.findMemberByEmail(principal.getName())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Event event = eventRepository.findById(eventId).orElseThrow(() -> new CustomException(ErrorCode.EVENT_NOT_FOUND));

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
                } catch (Exception e) {
                    throw new CustomException(ErrorCode.IMAGE_UPLOAD_FAILED);
                }

            });
        
    }

    public String DeleteAnnouncement(Long eventId, Principal principal) {
        Member member = memberRepository.findMemberByEmail(principal.getName())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new CustomException(ErrorCode.EVENT_NOT_FOUND));


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
        Member member = memberRepository.findById(userRoleChangeDto.getMemberId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
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

    public String UpdateEvent(EventCreateDto eventCreateDto, Long eventId, Principal principal) {
        Member member = memberRepository.findMemberByEmail(principal.getName())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new CustomException(ErrorCode.EVENT_NOT_FOUND));

        if (eventCreateDto.getTitle() != null) event.setTitle(eventCreateDto.getTitle());
        if (eventCreateDto.getContent() != null) event.setContent(eventCreateDto.getContent());
        //관리자가 LocationId를 수정시 해당 event와 event와 같은 locationId를 가진 member(COMPANY)도 수정합니다.
        if (eventCreateDto.getLocationId() !=null) {
            Member memberWhoCompany =memberRepository.findById(event.getLocation().getLocationId())
                    .orElseThrow();
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

    public String DeleteEvent(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new CustomException(ErrorCode.EVENT_NOT_FOUND));

        List<EventImage> eventList = eventImageRepository.findAllByEvent(event);
        eventImageRepository.deleteAll(eventList);
        eventRepository.delete(event);
        return returnMessage(event.getType(),"삭제");
    }

    public boolean EventCreateDtoImagesCheck(EventCreateDto eventCreateDto){
        List<MultipartFile> images = eventCreateDto.getImages();

        return images.stream().anyMatch(image -> !image.isEmpty());
    }

    public boolean EventImageListCheck(List<EventImage> eventImageList){

        return eventImageList.stream().anyMatch(image -> !image.getImageUrl().isEmpty());
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

       return String.format("%s(이)가 성공적으로 %s 되었습니다", message,propose);

   }
}
