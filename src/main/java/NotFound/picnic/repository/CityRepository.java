package NotFound.picnic.repository;

import NotFound.picnic.domain.City;
import lombok.NonNull;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;


public interface CityRepository extends JpaRepository<City, Long> {
    List<City> findAllByNameContaining(String keyword);

}
