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

    @Query(value="select * from location where (city like concat('%', :city, '%')) and division like concat('%', :division, '%') and (name like concat('%',:keyword,'%') or address like concat('%',:keyword,'%')) order by name, location_id desc limit 20 offset :lastIdx", nativeQuery = true)
    Optional<List<Location>> findByCityAndKeyword(@Param("city") String city, @Param("division") String division, @Param("keyword") String keyword, @Param("lastIdx") int lastIdx);

    @Query(value="select * from location where division like concat('%', :division, '%') and (city like concat('%', :keyword, '%') or name like concat('%',:keyword,'%') or address like concat('%',:keyword,'%')) order by name, location_id desc limit 20 offset :lastIdx", nativeQuery = true)
    Optional<List<Location>> findByKeyword(@Param("keyword") String keyword, @Param("division") String division, @Param("lastIdx") int lastIdx);

    @Query("select distinct p.location from Place p where p.schedule = :schedule")
    List<Location> findLocationsBySchedule(Schedule schedule);
}
