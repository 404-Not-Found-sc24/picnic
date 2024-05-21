package NotFound.picnic.exception;

import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {
    private final ErrorCode customErrorCode;

    public CustomException(ErrorCode customErrorCode) {
        super(customErrorCode.getMessage());
        this.customErrorCode = customErrorCode;
    }

    public ErrorCode getErrorCode() {
        return customErrorCode;
    }
}
