package NotFound.picnic.repository;

import NotFound.picnic.domain.Location;
import NotFound.picnic.domain.Place;
import NotFound.picnic.domain.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

import java.util.List;

public interface LocationRepository  extends JpaRepository<Location, Long> {
    Optional<Location> findByLocationId(Long locationId);

    String findNameByLocationId(Long locationId);

    Optional<List<Location>> findAllByCity(String city);

    @Query(value="select * from Location where city = :city and (name like concat('%',:keyword,'%') or address like concat('%',:keyword,'%')) order by name, location_id desc limit 20 offset :lastIdx", nativeQuery = true)
    Optional<List<Location>> findByCityAndKeyword(@Param("city") String city, @Param("keyword") String keyword, @Param("lastIdx") int lastIdx);

}
