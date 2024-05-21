package NotFound.picnic.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    UNAUTHORIZED( "인증에 실패하였습니다.", HttpStatus.UNAUTHORIZED),
    EXPIRED_TOKEN("토큰이 만료되었습니다.", HttpStatus.UNAUTHORIZED),
    UNSUPPORTED_TOKEN("지원되지 않는 토큰입니다.", HttpStatus.UNAUTHORIZED),
    LOGIN_NEEDED("로그인이 필요합니다.", HttpStatus.UNAUTHORIZED),
    NO_AUTHORITY("권한이 없습니다.", HttpStatus.UNAUTHORIZED),
    NO_JWT_CLAIM("JWT claims string is empty.", HttpStatus.BAD_REQUEST),
    LOGIN_FAILED("아이디 또는 비밀번호가 잘못되었습니다.", HttpStatus.BAD_REQUEST),
    PASSWORD_FAILED("비밀번호가 잘못되었습니다.", HttpStatus.BAD_REQUEST),
    REISSUE_PASSWORD_FAILED("비밀번호를 재발급하던 도중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    DUPLICATED_EMAIL("이미 존재하는 이메일입니다.", HttpStatus.BAD_REQUEST),
    DUPLICATED_NICKNAME("이미 존재하는 닉네임입니다.", HttpStatus.BAD_REQUEST),
    DUPLICATED_DIARY("이미 일기를 작성하였습니다.", HttpStatus.BAD_REQUEST),
    USER_NOT_FOUND( "사용자를 찾을 수 없습니다,", HttpStatus.NOT_FOUND),
    EVENT_NOT_FOUND("이벤트를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    LOCATION_NOT_FOUND("해당하는 장소를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    PLACE_NOT_FOUND("일정에 저장된 장소를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    SCHEDULE_NOT_FOUND("해당하는 일정을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    DIARY_NOT_FOUND("일기를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    FILE_CONVERT_FAILED("파일을 전환하던 도중 오류가 발생했습니다.",HttpStatus.BAD_REQUEST),
    IMAGE_UPLOAD_FAILED("이미지를 업로드 하는 도중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    APPROVAL_NOT_FOUND("해당하는 승인 요청 정보를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    APPROVAL_FAILED("승인/거절할 수 없는 장소입니다.", HttpStatus.BAD_REQUEST),
    SERVER_ERROR("서버에서 에러가 발생하였습니다.", HttpStatus.INTERNAL_SERVER_ERROR);



    private final String message;
    private final HttpStatus statusCode;

}
