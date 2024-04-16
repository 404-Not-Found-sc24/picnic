package NotFound.picnic.dto;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;

@Data
@Builder
@Getter
public class CityDto {
	private String cityName;
	private String imageUrl;

}
