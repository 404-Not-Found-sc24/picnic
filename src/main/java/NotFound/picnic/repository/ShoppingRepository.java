package NotFound.picnic.repository;

import NotFound.picnic.domain.Shopping;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface ShoppingRepository extends JpaRepository<Shopping, Long> {
    Shopping findByLocation_LocationId(Long locationId);
}
