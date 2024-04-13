package NotFound.picnic.repository;

import NotFound.picnic.domain.Location;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocationRepository  extends JpaRepository<Location, Long> {
}
