package NotFound.picnic.dto;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;

@Data
@Builder
@Getter
public class PlaceCreateDto {
    private Long locationId;
    private String date;
    private String time;
}
