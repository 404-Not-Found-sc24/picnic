package NotFound.picnic.repository;

import NotFound.picnic.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findMemberByEmail(String email);

    Boolean existsMemberByNickname(String nickname);

    Boolean existsMemberByEmail(String email);
}
