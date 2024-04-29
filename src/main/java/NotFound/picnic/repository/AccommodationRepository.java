package NotFound.picnic.repository;

import NotFound.picnic.domain.Accommodation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface AccommodationRepository extends JpaRepository<Accommodation, Long> {
    Accommodation findByLocation_LocationId(Long locationId);
}
