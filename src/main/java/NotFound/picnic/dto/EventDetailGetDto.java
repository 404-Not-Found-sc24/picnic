package NotFound.picnic.dto;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

import lombok.*;

@Data
@Builder
public class EventDetailGetDto {
    private Long eventId;
    private Long locationId;
    private String title;
    private String content;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
    private String memberName;
    private String imageUrl;



}
