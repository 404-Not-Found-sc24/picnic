package NotFound.picnic.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DiaryGetDto {
    private Long diaryId;
    private Long placeId;
    private String scheduleName;
    private String date;
    private String content;
    private String imageUrl;
}
