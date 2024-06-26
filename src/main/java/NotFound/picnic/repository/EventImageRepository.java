package NotFound.picnic.repository;

import NotFound.picnic.domain.Event;
import NotFound.picnic.domain.EventImage;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface EventImageRepository extends JpaRepository <EventImage, Long>{
    List<EventImage> findAllByEvent(Event event);

    Optional<EventImage> findEventImageByEvent_EventId(Long eventId);
}
