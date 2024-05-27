package NotFound.picnic.dto.auth;

import lombok.Data;

@Data
public class ReissuePasswordDto {
    private String email;
    private String phone;
}
