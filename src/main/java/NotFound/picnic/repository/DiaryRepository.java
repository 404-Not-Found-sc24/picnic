package NotFound.picnic.repository;

import NotFound.picnic.domain.Diary;
import NotFound.picnic.domain.Place;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DiaryRepository extends JpaRepository<Diary, Long> {
  
    List<Diary> findAllByPlace_PlaceId(Long placeId);
  
    boolean existsByPlace(Place place);
}
