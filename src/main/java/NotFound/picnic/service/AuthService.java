package NotFound.picnic.service;

import NotFound.picnic.auth.JwtUtil;
import NotFound.picnic.auth.OAuth;
import NotFound.picnic.domain.EmailCheck;
import NotFound.picnic.domain.Member;
import NotFound.picnic.dto.auth.*;
import NotFound.picnic.dto.manage.UserGetDto;
import NotFound.picnic.exception.ErrorCode;
import NotFound.picnic.exception.CustomException;
import NotFound.picnic.repository.EmailCheckRedisRepository;
import NotFound.picnic.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.SecureRandom;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {
    private final JwtUtil jwtUtil;
    private final MemberRepository memberRepository;
    private final PasswordEncoder encoder;
    private final BCryptPasswordEncoder BCryptEncoder;
    private final S3Upload s3Upload;
    private final JavaMailSender javaMailSender;
    private final EmailCheckRedisRepository emailCheckRedisRepository;
    private final OAuth oAuth;

    public LoginResponseDto login(LoginRequestDto dto) {
        String email = dto.getEmail();
        String password = dto.getPassword();
        Optional<Member> optionalMember = memberRepository.findMemberByEmail(email);

        if (optionalMember.isEmpty()) {
            throw new CustomException(ErrorCode.LOGIN_FAILED);
        }


        Member member = optionalMember.get();
        if (!encoder.matches(password, member.getPassword())) {
            throw new CustomException(ErrorCode.LOGIN_FAILED);
        }

        return getToken(member);

    }


    public LoginResponseDto socialLogin (OAuthDto oAuthDto) throws GeneralSecurityException, IOException {
        String googleAccessToken = oAuth.requestGoogleAccessToken(oAuthDto.getAuthorizationCode());
        UserInfoGetDto userInfoGetDto = oAuth.printUserResource(googleAccessToken);

        Member member = memberRepository.findMemberByEmail(userInfoGetDto.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        return getToken(member);
    }

    public LoginResponseDto getToken (Member member) {
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
                .Role(member.getRole().toString())
                .phone(member.getPhone())
                .build();
    }

    @Transactional
    public ResponseEntity<Long> join(SignUpDto signUpDto) {
        Optional<Member> member = memberRepository.findMemberByEmail(signUpDto.getEmail());
        if (member.isPresent()) {
            throw new CustomException(ErrorCode.DUPLICATED_EMAIL);
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
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }
            Long memberId = jwtUtil.getUserId(token);

            Member member = memberRepository.findById(memberId)
                    .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

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

    public UserGetDto getUser(Principal principal) {
        Member member =  memberRepository.findMemberByEmail(principal.getName())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        return UserGetDto.builder()
                        .memberId(member.getMemberId())
                        .email(member.getEmail())
                        .name(member.getName())
                        .nickname(member.getNickname())
                        .phone(member.getPhone())
                        .role(member.getRole())
                        .imageUrl(member.getImageUrl())
                        .build();
    }

    @Transactional
    public String updateUser(UserUpdateDto userUpdateDto, Principal principal) throws IOException {
        Member member = memberRepository.findMemberByEmail(principal.getName())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (userUpdateDto.getName() != null) member.setName(userUpdateDto.getName());
        if (userUpdateDto.getNickname() != null && !userUpdateDto.getNickname().equals(member.getNickname())) {
            if (memberRepository.existsMemberByNickname(userUpdateDto.getNickname()))
                throw new CustomException(ErrorCode.DUPLICATED_NICKNAME);
            member.setNickname(userUpdateDto.getNickname());
        }
        if (userUpdateDto.getPhone() != null) member.setPhone(userUpdateDto.getPhone());
        if (userUpdateDto.getImage() != null) {
            String imageUrl = s3Upload.uploadFiles(userUpdateDto.getImage(), "user");
            member.setImageUrl(imageUrl);
        }
        memberRepository.save(member);

        return "수정 완료";
    }

    @Transactional
    public String deleteUser (Principal principal) {
        Member member = memberRepository.findMemberByEmail(principal.getName())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        memberRepository.delete(member);
        return "삭제 완료";
    }

    public boolean duplicateEmail (String email) {
        return !memberRepository.existsMemberByEmail(email);
    }

    @Transactional
    public String changePassword (PasswordDto passwordDto, Principal principal) {
        Member member = memberRepository.findMemberByEmail(principal.getName())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        if (!BCryptEncoder.matches(passwordDto.getPassword(), member.getPassword())) {
            throw new CustomException(ErrorCode.PASSWORD_FAILED);
        }

        String newPassword = BCryptEncoder.encode(passwordDto.getNewPassword());
        member.setPassword(newPassword);
        memberRepository.save(member);
        return "비밀번호 변경 완료";
    }

    @Transactional
    public String reissuePassword(ReissuePasswordDto reissuePasswordDto) throws Exception {
        String newPw = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Member member = memberRepository.findMemberByEmailAndPhone(reissuePasswordDto.getEmail(), reissuePasswordDto.getPhone());
        if (member == null)
            throw new CustomException(ErrorCode.USER_NOT_FOUND);

        member.setPassword(BCryptEncoder.encode(newPw));

        try {
            SMTPMsgDto smtpMsgDto = SMTPMsgDto.builder()
                    .address(member.getEmail())
                    .title(member.getNickname() + "님의 [나들이] 임시비밀번호 안내 이메일 입니다.")
                    .message("안녕하세요. [나들이] 임시 비밀번호 안내 관련 이메일 입니다. \n" + "[" + member.getNickname() + "]" + "님의 임시 비밀번호는 "
                            + newPw + " 입니다.").build();
            SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
            simpleMailMessage.setTo(smtpMsgDto.getAddress());
            simpleMailMessage.setSubject(smtpMsgDto.getTitle());
            simpleMailMessage.setText(smtpMsgDto.getMessage());
            javaMailSender.send(simpleMailMessage);
        } catch (Exception exception) {
            log.error("PW Reissue ::{} ", exception.getMessage());
            throw new CustomException(ErrorCode.REISSUE_PASSWORD_FAILED);
        }
        return "비밀번호 재설정 완료";
    }

    public FindEmailResponseDto FindEmail (FindEmailRequestDto findEmailRequestDto) {
        Member member = memberRepository.findMemberByNameAndPhone(findEmailRequestDto.getName(), findEmailRequestDto.getPhone());
        if (member == null)
            throw new CustomException(ErrorCode.USER_NOT_FOUND);

        return FindEmailResponseDto.builder()
                .email(member.getEmail())
                .build();
    }

    @Transactional
    public String EmailCheckRequest(String email) {
        String authCode = this.createCode();
        EmailCheck existingEmailCheck = emailCheckRedisRepository.findByEmail(email);
        if (existingEmailCheck != null) {
            // 이미 해당 이메일이 Redis에 저장되어 있는 경우
            existingEmailCheck.setCode(authCode);
            emailCheckRedisRepository.save(existingEmailCheck);
        } else {
            EmailCheck emailCheck = new EmailCheck(email, authCode);
            emailCheck.setId(UUID.randomUUID().toString());

            emailCheckRedisRepository.save(emailCheck);
        }

        try {
            SMTPMsgDto smtpMsgDto = SMTPMsgDto.builder()
                    .address(email)
                    .title(email + "님의 [나들이] 이메일 인증 안내 이메일 입니다.")
                    .message("안녕하세요. [나들이] 이메일 인증 안내 관련 이메일 입니다. \n" + "[" + email + "]" + "님의 코드는 "
                            + authCode + " 입니다.").build();
            SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
            simpleMailMessage.setTo(smtpMsgDto.getAddress());
            simpleMailMessage.setSubject(smtpMsgDto.getTitle());
            simpleMailMessage.setText(smtpMsgDto.getMessage());
            javaMailSender.send(simpleMailMessage);
        } catch (Exception exception) {
            log.error("이메일 인증 ::{} ", exception.getMessage());
            throw new CustomException(ErrorCode.EMAIL_CHECK_FAILED);
        }
        return "이메일 전송 완료";
    }

    private String createCode() {
        int lenth = 6;
        try {
            Random random = SecureRandom.getInstanceStrong();
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < lenth; i++) {
                builder.append(random.nextInt(10));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException e) {
            log.debug("MemberService.createCode() exception occur");
            throw new CustomException(ErrorCode.NO_SUCH_ALGORITHM);
        }
    }

    public String EmailCheck(String email, String code) {
        EmailCheck emailCheck = emailCheckRedisRepository.findByEmail(email);
        if (emailCheck == null) throw new CustomException(ErrorCode.USER_NOT_FOUND);
        if (!Objects.equals(emailCheck.getCode(), code))
            throw new CustomException(ErrorCode.CODE_FAILED);
        return "인증 성공";
    }

}
