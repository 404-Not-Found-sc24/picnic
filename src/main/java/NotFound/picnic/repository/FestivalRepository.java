package NotFound.picnic.repository;

import NotFound.picnic.domain.Festival;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface FestivalRepository extends JpaRepository<Festival, Long> {
    Festival findByLocation_LocationId(Long locationId);
}
