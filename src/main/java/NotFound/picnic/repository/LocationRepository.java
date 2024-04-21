package NotFound.picnic.repository;

import NotFound.picnic.domain.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LocationRepository  extends JpaRepository<Location, Long> {
    Optional<List<Location>> findAllByCity(String city);

    @Query("select l from Location l where l.city = :city and (l.name like %:keyword% or l.address like %:keyword%)")
    Optional<List<Location>> findByCityAndKeyword(@Param("city") String city, @Param("keyword") String keyword);


}