package NotFound.picnic.dto.event;

import java.util.ArrayList;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;
import lombok.*;
import NotFound.picnic.enums.*;


@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class EventCreateDto {

    private String title;
    private String content;
    private List<MultipartFile> images;
    private EventType eventType;

}
