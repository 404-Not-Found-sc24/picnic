package NotFound.picnic.dto.manage;

import java.util.List;
import org.springframework.web.multipart.MultipartFile;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class LocationCreateDto {
    private String name;
    private String address;
    private String detail;
    private Double latitude;
    private Double longitude;
    private String division;
    private String phone;
    private List<MultipartFile> images;
}
