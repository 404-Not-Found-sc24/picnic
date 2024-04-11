package NotFound.picnic.dto;

import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequestDto {
    @NotNull(message = "이메일 입력은 필수입니다")
    @Email
    private String email;

    @NotNull(message = "비밀번호 입력은 필수입니다")
    private String password;
}
