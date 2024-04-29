package NotFound.picnic.repository;

import NotFound.picnic.domain.Tour;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface TourRepository extends JpaRepository<Tour, Long> {
    Tour findByLocation_LocationId(Long locationId);
}
