package NotFound.picnic.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserInfoDto {
    private Long memberId;
    private String email;
    private String name;
    private String password;
    private int role;
}
