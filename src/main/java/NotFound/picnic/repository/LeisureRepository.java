package NotFound.picnic.repository;

import NotFound.picnic.domain.Accommodation;
import NotFound.picnic.domain.Leisure;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface LeisureRepository extends JpaRepository<Leisure, Long> {
    Leisure findByLocation_LocationId(Long locationId);
}
