package NotFound.picnic.dto.manage;

import lombok.Getter;

@Getter
public class UserUpdateDto {
    private String name;
    private String nickname;
    private String email;
    private String phone;
    private Long locationId;
}
