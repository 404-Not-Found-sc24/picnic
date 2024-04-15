package NotFound.picnic.dto;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;

@Data
@Builder
@Getter
public class RecordCreateDto {
	
	private Long placeId;
	private String title;
	private String content;

}
