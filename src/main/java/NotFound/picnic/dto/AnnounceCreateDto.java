package NotFound.picnic.dto;

import NotFound.picnic.enums.EventType;
import lombok.Builder;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@Builder
public class AnnounceCreateDto {
    private String title;
    private String content;
    private List<MultipartFile> images;
    private EventType eventType;
}
