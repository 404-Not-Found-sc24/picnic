package NotFound.picnic.repository;
import NotFound.picnic.domain.Location;

import java.util.Optional;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;


public interface LocationRepository extends JpaRepository<Location,Long>{
	List<Location> findLocationByname(String name);
	List<Location> findLocationByaddress(String address);
    List<Location> findLocationBycity(String city);
    
    List<Location> findBynameContaining(String name);
    List<Location> findByaddressContaining(String address);
    List<Location> findBycityContaining(String city);
}
