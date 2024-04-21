package NotFound.picnic.repository;

import NotFound.picnic.domain.Diary;
import NotFound.picnic.domain.Place;
import NotFound.picnic.domain.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DiaryRepository  extends JpaRepository<Diary, Long> {
    boolean existsByPlace(Place place);

    @Query("select d from Diary d where d.place in (select p from Place p where p.schedule = :schedule)")
    Optional<List<Diary>> findAllBySchedule(@Param("schedule") Schedule schedule);
}
