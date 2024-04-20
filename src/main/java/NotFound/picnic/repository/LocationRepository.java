package NotFound.picnic.repository;

import NotFound.picnic.domain.Location;
import NotFound.picnic.domain.Place;
import NotFound.picnic.domain.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LocationRepository  extends JpaRepository<Location, Long> {
    Location findByLocationId(Long locationId);

    String findNameByLocationId(Long locationId);

}
