package NotFound.picnic.dto.schedule;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PlaceGetDto {
    private Long placeId;
    private Long locationId;
    private String locationName;
    private String date;
    private String time;
    private String address;
    private Double latitude;
    private Double longitude;
    private String imageUrl;
}
