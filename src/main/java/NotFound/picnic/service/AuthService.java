package NotFound.picnic.service;

import NotFound.picnic.auth.JwtUtil;
import NotFound.picnic.domain.Member;
import NotFound.picnic.dto.auth.*;
import NotFound.picnic.dto.manage.UserGetDto;
import NotFound.picnic.exception.ErrorCode;
import NotFound.picnic.exception.CustomException;
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
import java.security.Principal;
import java.util.Optional;
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
}
