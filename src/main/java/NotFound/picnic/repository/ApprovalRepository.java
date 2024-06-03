package NotFound.picnic.repository;

import NotFound.picnic.domain.Approval;
import NotFound.picnic.enums.State;
import lombok.NonNull;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;


public interface ApprovalRepository extends JpaRepository<Approval, Long> {
    @NonNull
    List <Approval> findAll();

    @Query(value="select * from approval where (name like concat('%', :keyword, '%') or address like concat('%', :keyword, '%')) and division like concat('%', :division, '%') and state like concat('%',:state,'%') order by name, approval_id", nativeQuery = true)
    List<Approval> findApprovals(@Param("keyword") String keyword, @Param("division") String division, @Param("state") String state);
}
