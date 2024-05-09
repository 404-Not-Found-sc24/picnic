package NotFound.picnic.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MyScheduleGetDto {
    private Long scheduleId;
    private String name;
    private String startDate;
    private String endDate;
    private String imageUrl;
    private boolean share;
}
