package NotFound.picnic.dto;

import lombok.Builder;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Builder
@Getter
public class DiaryCreateDto {
    private String title;
    private String content;
    private String weather;
    private List<MultipartFile> images;
}
