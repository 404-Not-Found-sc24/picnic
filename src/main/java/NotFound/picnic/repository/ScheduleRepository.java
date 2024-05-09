package NotFound.picnic.repository;

import NotFound.picnic.domain.Location;
import NotFound.picnic.domain.Member;
import NotFound.picnic.domain.Place;
import NotFound.picnic.domain.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ScheduleRepository  extends JpaRepository<Schedule, Long> {

    @Query(value="select * from schedule where schedule_id in (select distinct schedule_id from place where location_id in (:locationIds)) and share = true;", nativeQuery = true)
    List<Schedule> findDistinctSchedulesByLocations(@Param("locationIds") List<Long> locationIds);


    @Query(value="select * from schedule where schedule_id in (select distinct schedule_id from place where location_id = :locationId) and share = true;", nativeQuery = true)
    List<Schedule> findDistinctSchedulesByLocation(@Param("locationId") Long locationId);

    Optional<Schedule> findByScheduleId(Long scheduleId);

    List<Schedule> findAllByLocationContainingAndShare(String keyword, boolean share);

    List<Schedule> findAllByMember(Member member);
}
