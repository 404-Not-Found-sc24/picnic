package NotFound.picnic.dto;
import NotFound.picnic.domain.Image;
import org.springframework.web.multipart.MultipartFile;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.util.List;
@Data
@Builder
@Getter
public class RecordCreateDto {
	
	private Long placeId;
	private String title;
	private String content;
	private List<Long> imageIds;

}
