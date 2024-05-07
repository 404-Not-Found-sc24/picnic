package NotFound.picnic.controller;

import NotFound.picnic.dto.AnnotationCreateDto;
import NotFound.picnic.dto.ApprovalDto;
import NotFound.picnic.service.ManageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity<String> createAnnotation(AnnotationCreateDto annotationCreateDto, Principal principal) {
        String res = manageService.CreateAnnotation(annotationCreateDto, principal);
        return ResponseEntity.ok().body(res);
    }
}
