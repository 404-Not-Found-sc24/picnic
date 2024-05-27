package NotFound.picnic.controller;

import NotFound.picnic.dto.event.AnnounceCreateDto;
import NotFound.picnic.dto.event.EventCreateDto;
import NotFound.picnic.dto.manage.*;
import NotFound.picnic.service.ManageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;


@RestController
@RequiredArgsConstructor
@RequestMapping("/manage")
public class ManageController {
    private final ManageService manageService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/approval")
    public ResponseEntity<List<ApprovalDto>> getApprovalList(Principal principal){
        List<ApprovalDto> approvalDto = manageService.GetApprovalList(principal);
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
    @PatchMapping("/announce/{eventId}")
    public ResponseEntity<String> updateAnnouncement(AnnounceCreateDto announceCreateDto, @PathVariable Long eventId, Principal principal) {
        String res = manageService.UpdateAnnouncement(announceCreateDto, eventId, principal);
        return ResponseEntity.ok().body(res);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/announce/{eventId}")
    public ResponseEntity<String> deleteAnnouncement(@PathVariable Long eventId, Principal principal) {
        String res = manageService.DeleteAnnouncement(eventId, principal);
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
    @PatchMapping("/promotion/{eventId}")
    public ResponseEntity<String> updatePromotion(EventCreateDto eventCreateDto, @PathVariable(name="eventId") Long eventId, Principal principal) {
        String res = manageService.UpdateEvent(eventCreateDto, eventId, principal);
        return ResponseEntity.ok().body(res);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/promotion/{eventId}")
    public ResponseEntity<String> deletePromotion(@PathVariable(name="eventId") Long eventId) {
        String res = manageService.DeleteEvent(eventId);
        return ResponseEntity.ok().body(res);
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
}
