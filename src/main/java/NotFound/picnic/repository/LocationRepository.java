package NotFound.picnic.repository;
import NotFound.picnic.domain.Location;

import java.util.Optional;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;


public interface LocationRepository extends JpaRepository<Location,Long>{
	
}
