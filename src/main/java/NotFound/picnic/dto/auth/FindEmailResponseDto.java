package NotFound.picnic.dto.auth;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FindEmailResponseDto {
    private String email;
}
