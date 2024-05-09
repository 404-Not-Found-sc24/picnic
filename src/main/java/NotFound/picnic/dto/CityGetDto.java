package NotFound.picnic.dto;

import lombok.*;

@Data
@Builder
public class CityGetDto {
    private String cityName;
    private String cityDetail;
    private String imageUrl;


}
