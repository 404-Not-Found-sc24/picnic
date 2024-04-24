package NotFound.picnic.dto;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;

@Data
@Builder
@Getter
public class ScheduleAddPlaceDto {
    private Long locationId;
    private Long scheduleId;
    private String date;
    private String time;
}
