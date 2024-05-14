package NotFound.picnic.service;

import NotFound.picnic.auth.CustomAuthenticationEntryPoint;
import NotFound.picnic.auth.JwtUtil;
import NotFound.picnic.domain.Member;
import NotFound.picnic.dto.*;
import NotFound.picnic.repository.MemberRepository;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {
    private final JwtUtil jwtUtil;
    private final MemberRepository memberRepository;
    private final PasswordEncoder encoder;
    private final BCryptPasswordEncoder BCryptEncoder;

    public LoginResponseDto login(LoginRequestDto dto) {
        String email = dto.getEmail();
        String password = dto.getPassword();
        Optional<Member> optionalMember = memberRepository.findMemberByEmail(email);

        if (optionalMember.isEmpty()) {
            throw new UsernameNotFoundException("이메일이 존재하지 않습니다.");
        }


        Member member = optionalMember.get();
        if (encoder.matches(password, member.getPassword())) {
            throw new BadCredentialsException("비밀번호가 일치하지 않습니다");
        }

        UserInfoDto userInfoDto = UserInfoDto.builder()
                .memberId(member.getMemberId())
                .email(member.getEmail())
                .name(member.getName())
                .password(member.getPassword())
                .role(member.getRole())
                .build();

        String accessToken = jwtUtil.createAccessToken(userInfoDto);
        String refreshToken = jwtUtil.createRefreshToken(userInfoDto);

        return LoginResponseDto.builder()
                .grantType("Authorization")
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .email(member.getEmail())
                .name(member.getName())
                .nickname(member.getNickname())
                .phone(member.getPhone())
                .build();

    }

    @Transactional
    public ResponseEntity<Long> join(SignUpDto signUpDto) throws IOException {
        Optional<Member> member = memberRepository.findMemberByEmail(signUpDto.getEmail());
        if (member.isPresent()) {
            throw new IOException("This member email is already exist." + signUpDto.getEmail());
        }

        String password = BCryptEncoder.encode(signUpDto.getPassword());

        Member newMember = Member.builder()
                .name(signUpDto.getName())
                .nickname(signUpDto.getNickname())
                .email(signUpDto.getEmail())
                .phone(signUpDto.getPhone())
                .password(password)
                .build();

        Long memberId = memberRepository.save(newMember).getMemberId();

        return ResponseEntity.ok().body(memberId);
    }

    public LoginResponseDto reissueAccessToken(ReissueTokenDto refreshToken) {
        String token = refreshToken.getRefreshToken();
        if (!jwtUtil.validateToken(token)) {
            throw new ValidationException("refresh token이 유효하지 않습니다.");
        }
            Long memberId = jwtUtil.getUserId(token);

            Member member = memberRepository.findById(memberId).orElseThrow();

            UserInfoDto userInfoDto = UserInfoDto.builder()
                    .memberId(member.getMemberId())
                    .email(member.getEmail())
                    .name(member.getName())
                    .password(member.getPassword())
                    .role(member.getRole())
                    .build();

            String accessToken = jwtUtil.createAccessToken(userInfoDto);

            return LoginResponseDto.builder()
                    .grantType("Authorization")
                    .accessToken(accessToken)
                    .refreshToken(token)
                    .build();
    }

}
