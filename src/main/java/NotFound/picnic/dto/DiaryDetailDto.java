package NotFound.picnic.dto;

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
    private List<String> imageUrl;
}
