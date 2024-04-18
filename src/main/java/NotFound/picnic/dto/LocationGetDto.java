package NotFound.picnic.dto;

import lombok.*;

@Data
@Builder
public class LocationGetDto {
    private Long locationId;
    private String name;
    private String address;
    private Double latitude;
    private Double longitude;
    private String imageUrl;
}
