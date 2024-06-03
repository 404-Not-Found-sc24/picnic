package NotFound.picnic.repository;

import NotFound.picnic.domain.Event;
import NotFound.picnic.domain.Location;
import NotFound.picnic.enums.EventType;
import lombok.NonNull;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
 
public interface EventRepository extends JpaRepository<Event, Long>{
     
    List<Event> findAllByType(EventType  type);
    Optional<Event> findByEventIdAndType(Long id, EventType type);

    @Query(value="select * from Event where type like concat('%', :div, '%') and (content like concat('%', :keyword, '%') or title like concat('%',:keyword,'%')) order by title", nativeQuery = true)
    List<Event> findByKeyword(@Param("div") String div, @Param("keyword") String keyword);

}
