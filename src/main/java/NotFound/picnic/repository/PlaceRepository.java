package NotFound.picnic.repository;

import NotFound.picnic.domain.Place;
import NotFound.picnic.domain.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PlaceRepository extends JpaRepository<Place, Long> {
    List<Place> findBySchedule(Schedule schedule);
    Optional<List<Place>> findAllByLocation_LocationId(Long locationId);

    Optional<List<Place>> findAllBySchedule_ScheduleId(Long scheduleId);

    boolean existsBySchedule(Schedule schedule);
}
