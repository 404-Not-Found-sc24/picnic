package NotFound.picnic.dto;

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
    private Long locationId;
    private List<MultipartFile> images;
    private EventType eventType;

}
