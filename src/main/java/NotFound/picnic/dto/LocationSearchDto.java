package NotFound.picnic.dto;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;

@Data
@Builder
@Getter
public class LocationSearchDto {
	
	private String city;
	private String keyword;

}
