package NotFound.picnic.controller;

import NotFound.picnic.domain.Event;
import NotFound.picnic.dto.event.AnnounceCreateDto;
import NotFound.picnic.dto.event.EventCreateDto;
import NotFound.picnic.dto.event.EventGetDto;
import NotFound.picnic.dto.manage.*;
import NotFound.picnic.enums.State;
import NotFound.picnic.enums.EventType;
import NotFound.picnic.service.ManageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("/manage")
public class ManageController {
    private final ManageService manageService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/approval")
    public ResponseEntity<List<ApprovalDto>> getApprovalList(@RequestParam(required = false, defaultValue = "", name="keyword") String keyword,
                                                             @RequestParam(required = false, defaultValue = "", name="division") String division,
                                                             @RequestParam(required = false, defaultValue = "", name="state") String state,
                                                             Principal principal){
        List<ApprovalDto> approvalDto = manageService.GetApprovalList(keyword, division, state, principal);
        return ResponseEntity.ok().body(approvalDto);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/approve/{approvalId}")
    public ResponseEntity<String> approveApproval(@PathVariable Long approvalId, @RequestBody ApproveDto approveDto){
        String response = manageService.ApproveApproval(approvalId, approveDto);
        return ResponseEntity.ok().body(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/deny/{approvalId}")
    public ResponseEntity<String> denyApproval(@PathVariable Long approvalId){
        String response = manageService.DenyApproval(approvalId);
        return ResponseEntity.ok().body(response);

    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/announce")
    public ResponseEntity<String> createAnnouncement(AnnounceCreateDto announceCreateDto, Principal principal) {
        String res = manageService.CreateAnnouncement(announceCreateDto, principal);
        return ResponseEntity.ok().body(res);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/event/{eventId}")
    public ResponseEntity<String> updateEvent(AnnounceCreateDto announceCreateDto, @PathVariable Long eventId) {
        String res = manageService.UpdateEvent(announceCreateDto, eventId);
        return ResponseEntity.ok().body(res);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/event/{eventId}")
    public ResponseEntity<String> deleteEvent(@PathVariable Long eventId) {
        String res = manageService.DeleteEvent(eventId);
        return ResponseEntity.ok().body(res);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/member")
    public ResponseEntity<List<UserGetDto>> getUsers() {
        List<UserGetDto> userGetDtoList = manageService.getUsers();
        return ResponseEntity.ok().body(userGetDtoList);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/member")
    public ResponseEntity<String> userRoleChange(@RequestBody UserRoleChangeDto userRoleChangeDto){
        String response = manageService.UserRoleChange(userRoleChangeDto);
        return ResponseEntity.ok().body(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/member/{memberId}")
    public ResponseEntity<String> updateMember(@PathVariable(name="memberId") Long memberId, @RequestBody UserUpdateDto userUpdateDto) {
        String res = manageService.UpdateUser(userUpdateDto, memberId);
        return ResponseEntity.ok().body(res);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/member/{memberId}")
    public ResponseEntity<String> deleteUser(@PathVariable(name="memberId") Long memberId) {
        String res = manageService.DeleteUser(memberId);
        return ResponseEntity.ok().body(res);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/location")
    public ResponseEntity<String> createLocation(LocationCreateDto locationCreateDto){
        String response = manageService.CreateLocation(locationCreateDto);
        return ResponseEntity.ok().body(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/location/{locationId}")
    public ResponseEntity<String> deleteLocation(@PathVariable(name="locationId") Long locationId) {
        String res = manageService.DeleteLocation(locationId);
        return ResponseEntity.ok().body(res);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/announce")
    public ResponseEntity<List<EventGetDto>> findEvent(
            @RequestParam(required = false, defaultValue = "", name="div") String div,
            @RequestParam(required = false, defaultValue = "", name="keyword") String keyword
    ){
        List<EventGetDto> res = manageService.FindEvent(div, keyword);
        return ResponseEntity.ok().body(res);
    }
}
