package NotFound.picnic.dto.tour;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DiaryGetDto {
    private Long diaryId;
    private Long placeId;
    private String userName;
    private String title;
    private String date;
    private String content;
    private String imageUrl;
}
