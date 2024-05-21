package NotFound.picnic.dto.schedule;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class MyScheduleListDto {
    private List<MyScheduleGetDto> beforeTravel;
    private List<MyScheduleGetDto> traveling;
    private List<MyScheduleGetDto> afterTravel;
}
