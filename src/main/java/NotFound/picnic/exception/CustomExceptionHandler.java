package NotFound.picnic.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Date;

@Slf4j
@RestController
@ControllerAdvice
public class CustomExceptionHandler extends ResponseEntityExceptionHandler {

	@ExceptionHandler(CustomException.class)
	public ResponseEntity<CustomErrorResponse> handleCustomException(CustomException ex) {
		log.error("Handling custom exception: {}", ex.getMessage());
		CustomErrorResponse errorResponse = CustomErrorResponse.builder()
				.status(ex.getErrorCode().getStatusCode())
				.message(ex.getMessage())
				.build();

		return new ResponseEntity<>(errorResponse, ex.getErrorCode().getStatusCode());
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<CustomErrorResponse> handleException(Exception ex) {
		CustomErrorResponse errorResponse = CustomErrorResponse.builder()
				.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.message("Internal Server Error")
				.build();
		return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
	}

}