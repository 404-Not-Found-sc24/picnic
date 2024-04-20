package NotFound.picnic.repository;

import NotFound.picnic.domain.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScheduleRepository  extends JpaRepository<Schedule, Long> {

    Schedule findByScheduleId(Long scheduleId);
}
