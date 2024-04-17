package NotFound.picnic.repository;

import NotFound.picnic.domain.Diary;
import NotFound.picnic.domain.Place;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiaryRepository  extends JpaRepository<Diary, Long> {
    boolean existsByPlace(Place place);
}
