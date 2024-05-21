package NotFound.picnic.dto.tour;

import lombok.*;

@Data
@Builder
public class CityGetDto {
    private String cityName;
    private String cityDetail;
    private String imageUrl;


}
