package NotFound.picnic.auth;

import NotFound.picnic.dto.ErrorResponseDto;
import NotFound.picnic.exception.CustomException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

// 인증 정보 없을 때 401 에러
@Slf4j(topic= "UNAUTHORIZATION_EXCEPTION_HANDLER")
@AllArgsConstructor
@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authenticationException) throws IOException, ServletException {
        log.error("Not Authenticated Request", authenticationException);
        log.info("jdkfjkl");

        Object exception = request.getAttribute("exception");

        if (exception instanceof CustomException) {
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");

            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("status", "FAILED");
            responseBody.put("message", authenticationException.getMessage());

            PrintWriter writer = response.getWriter();
            writer.write(new ObjectMapper().writeValueAsString(responseBody));
            writer.flush();
            writer.close();
        }
        else {
            ErrorResponseDto errorResponseDto = new ErrorResponseDto(HttpStatus.UNAUTHORIZED.value(), authenticationException.getMessage(), LocalDateTime.now());

            String responseBody = objectMapper.writeValueAsString(errorResponseDto);

            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(responseBody);
        }
    }
}
