package NotFound.picnic.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ApproveDto {
    private String address;
    private String content;
    private String detail;
    private String name;
}