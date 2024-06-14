package NotFound.picnic.dto.manage;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserUpdateDto {
    private String name;
    private String nickname;
    private String email;
    private String phone;
    private Long locationId;
}
