package NotFound.picnic.repository;

import NotFound.picnic.domain.Diary;
import NotFound.picnic.domain.Image;
import NotFound.picnic.domain.Place;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface ImageRepository extends JpaRepository<Image, Long> {
    Optional<Image> findTopByDiary(Diary diary);
  
    Optional <Image> findImageUrlByDiary_RecordId(Long recordId);
}
