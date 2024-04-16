package NotFound.picnic.repository;

import NotFound.picnic.domain.City;
import org.springframework.data.jpa.repository.JpaRepository;



public interface CityRepository extends JpaRepository <City,Long> {

}
