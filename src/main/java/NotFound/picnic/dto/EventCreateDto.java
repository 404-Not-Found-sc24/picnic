package NotFound.picnic.dto;

import java.util.List;
import org.springframework.web.multipart.MultipartFile;

import lombok.*;

@Data
@Builder
public class EventCreateDto {

    private String title;
    private String content;
    private Long locationId;
    private List<MultipartFile> images;

}
