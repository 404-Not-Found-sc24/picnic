package NotFound.picnic.repository;

import NotFound.picnic.domain.Culture;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface CultureRepository extends JpaRepository<Culture, Long> {
    Culture findByLocation_LocationId(Long locationId);
}
