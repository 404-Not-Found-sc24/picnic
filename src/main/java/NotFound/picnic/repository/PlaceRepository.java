package NotFound.picnic.repository;

import NotFound.picnic.domain.Place;
import NotFound.picnic.domain.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlaceRepository extends JpaRepository<Place, Long> {
    List<Place> findBySchedule(Schedule schedule);

    boolean existsBySchedule(Schedule schedule);
}
