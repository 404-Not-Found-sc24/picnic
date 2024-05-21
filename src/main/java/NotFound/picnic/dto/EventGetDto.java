package NotFound.picnic.dto;

import java.time.LocalDateTime;

import NotFound.picnic.enums.EventType;
import lombok.*;

@Data
@Builder
public class EventGetDto {
    private Long eventId;
    private String title;
    private String content;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
    private Long locationId;
    private String memberName;

}
