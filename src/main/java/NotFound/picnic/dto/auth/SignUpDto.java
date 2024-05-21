package NotFound.picnic.dto.auth;

import jakarta.validation.constraints.Email;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

@Data
@Getter
@Builder
public class SignUpDto {
    private String name;
    private String nickname;

    @Email
    private String email;
    private String phone;
    private String password;
}
