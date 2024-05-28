package NotFound.picnic.service;

import NotFound.picnic.domain.*;
import NotFound.picnic.dto.event.AnnounceCreateDto;
import NotFound.picnic.dto.event.EventCreateDto;
import NotFound.picnic.dto.manage.*;
import NotFound.picnic.enums.*;
import NotFound.picnic.exception.CustomException;
import NotFound.picnic.exception.ErrorCode;
import NotFound.picnic.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Type;
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
        if(!announceCreateDto.getEventType().equals(EventType.ANNOUNCEMENT)){
            throw new CustomException(ErrorCode.SERVER_ERROR);
        }
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

    public String UpdateEvent(AnnounceCreateDto announceCreateDto, Long eventId) {
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

        return "이벤트 수정 완료";
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

    public String DeleteEvent(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new CustomException(ErrorCode.EVENT_NOT_FOUND));

        List<EventImage> eventList = eventImageRepository.findAllByEvent(event);
        eventImageRepository.deleteAll(eventList);
        eventRepository.delete(event);
        return "이벤트 삭제 완료";
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

    

   public String UpdateUser(UserUpdateDto userUpdateDto, Long memberId) {
       Member member = memberRepository.findById(memberId).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (userUpdateDto.getName() != null) member.setName(userUpdateDto.getName());
        if (userUpdateDto.getNickname() != null) member.setNickname(userUpdateDto.getNickname());
        if (userUpdateDto.getEmail() != null) member.setEmail(userUpdateDto.getEmail());
        if (userUpdateDto.getPhone() != null) member.setPhone(userUpdateDto.getPhone());
        if (userUpdateDto.getLocationId() != null) {
            if (!locationRepository.existsById(userUpdateDto.getLocationId()))
                throw new CustomException(ErrorCode.LOCATION_NOT_FOUND);
            member.setLocationId(userUpdateDto.getLocationId());
        }

        memberRepository.save(member);

        return "수정 완료";
   }

   public String DeleteUser(Long memberId) {
       Member member = memberRepository.findById(memberId).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

       memberRepository.delete(member);

       return "삭제 완료";
   }
   
   public String CreateLocation(LocationCreateDto locationCreateDto){

    String city = locationCreateDto.getAddress().split(" ")[0];
        String[] strArray = {"서울특별시", "인천광역시", "대구광역시", "대전광역시", "부산광역시", "울산광역시", "세종특별자치시", "제주특별자치도"};
        List<String> strList = new ArrayList<>(Arrays.asList(strArray));
        if(!strList.contains(city)) {
            city = city + " " +locationCreateDto.getAddress().split(" ")[1];
        }

    Location location = Location.builder()
                .name(locationCreateDto.getName())
                .address(locationCreateDto.getAddress())
                .city(city)
                .detail(locationCreateDto.getDetail())
                .latitude(locationCreateDto.getLatitude())
                .longitude(locationCreateDto.getLongitude())
                .division(locationCreateDto.getDivision())
                .phone(locationCreateDto.getPhone())
                .build();
        locationRepository.save(location);

        switch(locationCreateDto.getDivision()){
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

    List<MultipartFile> images = locationCreateDto.getImages();

    saveLocationImages(images,location);

    return "장소 생성 완료";
   }

   public String DeleteLocation(Long locationId) {
    Location location = locationRepository.findById(locationId)
            .orElseThrow(() -> new CustomException(ErrorCode.LOCATION_NOT_FOUND));

    List<LocationImage> locationImageList = locationImageRepostiory.findAllByLocation(location);
    locationImageRepostiory.deleteAll(locationImageList);
    switch(location.getDivision()){
        case "숙박":
            Accommodation accommodation = accommodationRepository.findByLocation_LocationId(location.getLocationId());
            accommodationRepository.delete(accommodation);
            break;
        case "문화시설":
            Culture culture = cultureRepository.findByLocation_LocationId(location.getLocationId());
            cultureRepository.delete(culture);
            break;
        case "축제 공연 행사":
            Festival festival = festivalRepository.findByLocation_LocationId(location.getLocationId());
            festivalRepository.delete(festival);
            break;
        case "레포츠":
            Leisure leisure = leisureRepository.findByLocation_LocationId(location.getLocationId());
            leisureRepository.delete(leisure);
            break;
        case "음식점":
            Restaurant restaurant = restaurantRepository.findByLocation_LocationId(location.getLocationId());
            restaurantRepository.delete(restaurant);
            break;
        case "쇼핑":
            Shopping shopping = shoppingRepository.findByLocation_LocationId(location.getLocationId());
            shoppingRepository.delete(shopping);
            break;
        case "관광지":
            Tour tour = tourRepository.findByLocation_LocationId(location.getLocationId());
            tourRepository.delete(tour);
            break;
    }
    locationRepository.delete(location);
    return "장소 삭제 완료";
}

private void saveLocationImages(List<MultipartFile> images, Location location) {
    images.forEach(image -> {
        try {
            if (!image.isEmpty()){
            String url = s3Upload.uploadFiles(image, "location");
            LocationImage img = LocationImage.builder()
                    .location(location)
                    .imageUrl(url)
                    .build();

            locationImageRepostiory.save(img);
            }
        } catch (Exception e) {
            throw new CustomException(ErrorCode.IMAGE_UPLOAD_FAILED);
        }

    });

}
}
