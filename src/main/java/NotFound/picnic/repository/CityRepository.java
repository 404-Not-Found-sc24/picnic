package NotFound.picnic.repository;

import NotFound.picnic.domain.City;
import lombok.NonNull;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface CityRepository extends JpaRepository<City, Long> {
    @NonNull
    List <City> findAll();

}
