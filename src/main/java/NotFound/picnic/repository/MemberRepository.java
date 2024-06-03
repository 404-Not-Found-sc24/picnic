package NotFound.picnic.repository;

import NotFound.picnic.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findMemberByEmail(String email);

    Boolean existsMemberByNickname(String nickname);

    Boolean existsMemberByEmail(String email);

    Member findMemberByEmailAndPhone(String email, String phone);

    Member findMemberByNameAndPhone(String name, String phone);


    @Query(value="select * from member " +
            "where email like concat('%', :keyword, '%') " +
            "or name like concat('%', :keyword, '%') " +
            "or nickname like concat('%', :keyword, '%') " +
            "or phone like concat('%',:keyword,'%') " +
            "or role like concat('%',:keyword,'%') " +
            "order by name, member_id", nativeQuery = true)
    List<Member> findMembersBySearch(@Param("keyword") String keyword);
}
