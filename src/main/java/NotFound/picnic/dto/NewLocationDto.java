package NotFound.picnic.dto;

import org.springframework.web.multipart.MultipartFile;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.util.List;

@Data
@Builder
@Getter
public class NewLocationDto {
    private String name;
    private String address;
    private String detail;
    private Double latitude;
    private Double longitude;
    private String division;
    private String phone;
    private String content;
    private List<MultipartFile> images;

}
