package NotFound.picnic.controller;

import NotFound.picnic.dto.AnnounceCreateDto;
import NotFound.picnic.dto.ApprovalDto;
import NotFound.picnic.service.ManageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    public ResponseEntity<List<ApprovalDto>> getApprovalList(Principal principal){
        List<ApprovalDto> approvalDto = manageService.GetApprovalList(principal);
        return ResponseEntity.ok().body(approvalDto);
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
}
