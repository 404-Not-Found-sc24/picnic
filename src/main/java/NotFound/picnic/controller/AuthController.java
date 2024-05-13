package NotFound.picnic.controller;

import NotFound.picnic.dto.*;
import NotFound.picnic.service.AuthService;
import NotFound.picnic.service.S3Upload;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;
    private final S3Upload s3Upload;


    @PostMapping("/sign-in")
    public ResponseEntity<LoginResponseDto> getMemberProfile(
            @Valid @RequestBody LoginRequestDto loginRequestDto
            ) {
        LoginResponseDto token = this.authService.login(loginRequestDto);
        return ResponseEntity.status(HttpStatus.OK).body(token);
    }

    @PostMapping("/sign-up")
    public ResponseEntity<Long> addMember(@Valid @RequestBody SignUpDto signUpDto) throws IOException {
        return authService.join(signUpDto);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/test")
    public String test(@RequestPart(required = false) MultipartFile file) {
        if (file.isEmpty()) {
            return "파일이 유효하지 않습니다.";
        }
        try {
            s3Upload.uploadFiles(file, "static");
            return "파일 업로드 성공";
        } catch (Exception e) {
            return "파일 업로드 실패" + e.getMessage();
        }
    }

    @PatchMapping("/reissue-token")
    public ResponseEntity<LoginResponseDto>  reissueAccessToken(@RequestBody ReissueTokenDto refreshToken) {
        LoginResponseDto token = this.authService.reissueAccessToken(refreshToken);
        return ResponseEntity.status(HttpStatus.OK).body(token);
    }

    @GetMapping("")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserGetDto> getUser(Principal principal) {
        UserGetDto userGetDto = authService.getUser(principal);
        return ResponseEntity.status(HttpStatus.OK).body(userGetDto);
    }
}
