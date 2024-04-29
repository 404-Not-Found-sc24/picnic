package NotFound.picnic.repository;

import NotFound.picnic.domain.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {
    Restaurant findByLocation_LocationId(Long locationId);
}
