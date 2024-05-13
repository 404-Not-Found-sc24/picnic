package NotFound.picnic.dto;

import NotFound.picnic.enums.Role;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserRoleChangeDto {
    private Long memberId;
    private String targetRole;
}
