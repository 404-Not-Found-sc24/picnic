package NotFound.picnic.dto;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;

@Data
@Builder
@Getter
public class ApproveDto {
    private String address;
    private String content;
    private String detail;
    private String name;
}