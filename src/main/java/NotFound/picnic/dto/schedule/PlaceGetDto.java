package NotFound.picnic.dto.schedule;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PlaceGetDto {
    private Long locationId;
    private String name;
    private String address;
    private Double latitude;
    private Double longitude;
    private String imageUrl;
}
