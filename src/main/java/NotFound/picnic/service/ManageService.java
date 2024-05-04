package NotFound.picnic.service;

import NotFound.picnic.domain.Approval;
import NotFound.picnic.dto.ApprovalDto;
import NotFound.picnic.enums.State;
import NotFound.picnic.repository.ApprovalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ManageService {

    private final ApprovalRepository approvalRepository;

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
}
