package NotFound.picnic.controller;

import NotFound.picnic.dto.auth.*;
import NotFound.picnic.dto.manage.UserGetDto;
import NotFound.picnic.service.AuthService;
import NotFound.picnic.service.S3Upload;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
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
            @Valid @RequestBody NotFound.picnic.dto.auth.LoginRequestDto loginRequestDto
            ) {
        LoginResponseDto token = this.authService.login(loginRequestDto);
        return ResponseEntity.status(HttpStatus.OK).body(token);
    }

    @PostMapping("/sign-up")
    public ResponseEntity<Long> addMember(@Valid @RequestBody SignUpDto signUpDto) {
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

    @PatchMapping()
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> updateUser(UserUpdateDto userUpdateDto, Principal principal) throws IOException {
        String res = authService.updateUser(userUpdateDto, principal);
        return ResponseEntity.status(HttpStatus.OK).body(res);
    }

    @DeleteMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> deleteUser(Principal principal) {
        String res = authService.deleteUser(principal);
        return ResponseEntity.status(HttpStatus.OK).body(res);
    }

    @GetMapping("/duplicate")
    public ResponseEntity<String> duplicateEmail (@RequestParam(name="email") String email) throws BadRequestException {
        if (authService.duplicateEmail(email))
            return ResponseEntity.status(HttpStatus.OK).body("사용 가능한 아이디입니다");
        else
            throw new BadRequestException("이미 존재하는 아이디입니다");
    }

    @PatchMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> changePassword (@RequestBody PasswordDto passwordDto, Principal principal) {
        String res = authService.changePassword(passwordDto, principal);
        return ResponseEntity.status(HttpStatus.OK).body(res);
    }

    @PostMapping("/reissue-password")
    public ResponseEntity<String> reissuePassword (@RequestBody ReissuePasswordDto reissuePasswordDto) throws Exception {
        String res = authService.reissuePassword(reissuePasswordDto);
        return ResponseEntity.status(HttpStatus.OK).body(res);
    }
}
