package NotFound.picnic.exception;

import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@Builder
public class CustomErrorResponse {
    private final HttpStatus status;
    private final String message;
}
