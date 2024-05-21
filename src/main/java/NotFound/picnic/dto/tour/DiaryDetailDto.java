package NotFound.picnic.dto.tour;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class DiaryDetailDto {
    private String userName;
    private String title;
    private String date;
    private String weather;
    private String content;
    private Double latitude;
    private Double longitude;
    private List<String> imageUrl;
}
