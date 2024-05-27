package NotFound.picnic.dto.auth;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReissueTokenDto {
    String refreshToken;
}
