package NotFound.picnic.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ScheduleGetDto {
    private Long scheduleId;
    private String name;
    private String startDate;
    private String endDate;
    private String username;
    private String imageUrl;
}
