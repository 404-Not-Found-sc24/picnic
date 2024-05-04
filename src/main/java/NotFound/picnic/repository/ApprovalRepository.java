package NotFound.picnic.repository;

import NotFound.picnic.domain.Approval;
import lombok.NonNull;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface ApprovalRepository extends JpaRepository<Approval, Long> {
    @NonNull
    List <Approval> findAll();

}
