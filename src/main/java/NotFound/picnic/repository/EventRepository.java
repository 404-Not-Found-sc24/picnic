package NotFound.picnic.repository;

import NotFound.picnic.domain.Event;
import NotFound.picnic.enums.EventType;
import lombok.NonNull;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
 
public interface EventRepository extends JpaRepository<Event, Long>{
     
    List<Event> findAllByType(EventType  type);  

}
