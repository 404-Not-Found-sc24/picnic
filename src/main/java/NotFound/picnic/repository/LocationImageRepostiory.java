package NotFound.picnic.repository;

import NotFound.picnic.domain.Location;
import NotFound.picnic.domain.LocationImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LocationImageRepostiory extends JpaRepository<LocationImage, Long> {
    Optional<LocationImage> findTopByLocation(Location location);
}
