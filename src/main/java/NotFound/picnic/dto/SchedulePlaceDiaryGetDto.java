package NotFound.picnic.dto;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Builder
@Getter
@Setter
public class SchedulePlaceDiaryGetDto {
    private Long placeID;
    private Long locationId;
    private String locationName;
    private String date;
    private String time;
    private Long diaryId;
    private String title;
    private String content;
    private String imageUrl;
}