package NotFound.picnic.dto.schedule;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;

@Data
@Builder
@Getter
public class ScheduleCreateDto {
    private String name;
    private String location;
    private String startDate;
    private String endDate;
    private boolean share;
}
