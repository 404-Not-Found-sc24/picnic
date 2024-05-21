package NotFound.picnic.dto.schedule;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Builder
@Getter
@Setter
public class SchedulePlaceDiaryGetDto {
    private Long placeId;
    private Long locationId;
    private String locationName;
    private String date;
    private String time;
    private Long diaryId;
    private String title;
    private String content;
    private String imageUrl;
    private Double latitude;
    private Double longitude;
}