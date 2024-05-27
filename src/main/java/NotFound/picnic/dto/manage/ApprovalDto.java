package NotFound.picnic.dto.manage;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ApprovalDto {
    private Long approvalId;
    private String address;
    private String content;
    private String date;
    private String detail;
    private String division;
    private Double latitude;
    private Double longitude;
    private String name;
    private String state;
    private String userName;
}