package NotFound.picnic.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class DiaryCreateDto {
    private String title;
    private String content;
    private String weather;
}
