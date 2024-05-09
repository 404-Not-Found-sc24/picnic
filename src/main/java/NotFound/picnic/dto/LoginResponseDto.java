package NotFound.picnic.dto;

import lombok.*;

@Builder
@Getter
public class LoginResponseDto {
    private String grantType;
    private String accessToken;
    private String refreshToken;
    private String email;
    private String name;
    private String nickname;
    private String phone;
}
