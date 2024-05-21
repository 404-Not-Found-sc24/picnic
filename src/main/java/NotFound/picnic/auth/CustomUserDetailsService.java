package NotFound.picnic.auth;

import NotFound.picnic.domain.Member;
import NotFound.picnic.dto.auth.UserInfoDto;
import NotFound.picnic.exception.CustomException;
import NotFound.picnic.exception.ErrorCode;
import NotFound.picnic.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String id) {
        Member member = memberRepository.findById(Long.parseLong(id))
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        UserInfoDto dto = UserInfoDto.builder()
                .memberId(member.getMemberId())
                .email(member.getEmail())
                .name(member.getName())
                .role(member.getRole())
                .build();

        return new CustomUserDetails(dto);
    }
}
