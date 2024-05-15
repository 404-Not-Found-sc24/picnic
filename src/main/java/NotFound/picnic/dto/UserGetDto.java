package NotFound.picnic.dto;

import NotFound.picnic.enums.Role;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserGetDto {
    private Long memberId;
    private String email;
    private String name;
    private String nickname;
    private String phone;
    private Role role;
    private String imageUrl;
}
