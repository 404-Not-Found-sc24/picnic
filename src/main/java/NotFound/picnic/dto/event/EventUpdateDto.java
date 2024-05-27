package NotFound.picnic.dto.event;

import NotFound.picnic.enums.EventType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@AllArgsConstructor
public class EventUpdateDto {
    private String title;
    private String content;
    private List<MultipartFile> images;
}
