package NotFound.picnic.repository;

import NotFound.picnic.domain.ApprovalImage;
import lombok.NonNull;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface ApprovalImageRepository extends JpaRepository<ApprovalImage, Long> {
    @NonNull
    List <ApprovalImage> findAll();

}
