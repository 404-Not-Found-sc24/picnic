package NotFound.picnic.dto.auth;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class UserUpdateDto {
    private String name;
    private String nickname;
    private String phone;
    private MultipartFile image;
}
