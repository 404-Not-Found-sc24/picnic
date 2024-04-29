package NotFound.picnic.dto;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;

@Data
@Builder
@Getter
public class ScheduleDuplicateDto {
    private String name;
    private String startDate;
    private String endDate;
}
