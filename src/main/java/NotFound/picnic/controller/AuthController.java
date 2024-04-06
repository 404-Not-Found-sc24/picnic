package NotFound.picnic.controller;

import NotFound.picnic.dto.LoginRequestDto;
import NotFound.picnic.dto.LoginResponseDto;
import NotFound.picnic.dto.ReissueTokenDto;
import NotFound.picnic.dto.SignUpDto;
import NotFound.picnic.service.AuthService;
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

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;


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

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/test")
    public String test() {
        return "success";
    }

    @PatchMapping("/reissueToken")
    public ResponseEntity<LoginResponseDto>  reissueAccessToken(@RequestBody ReissueTokenDto refreshToken) {
        LoginResponseDto token = this.authService.reissueAccessToken(refreshToken);
        return ResponseEntity.status(HttpStatus.OK).body(token);
    }
}
