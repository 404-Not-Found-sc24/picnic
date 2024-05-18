package NotFound.picnic.repository;

import NotFound.picnic.domain.Diary;
import NotFound.picnic.domain.Image;
import NotFound.picnic.domain.Place;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface ImageRepository extends JpaRepository<Image, Long> {
    Optional<Image> findTopByDiary(Diary diary);
  
<<<<<<< HEAD
    Optional <Image> findImageUrlByDiary_DiaryId(Long diaryId);
=======
    Optional <Image> findTopImageUrlByDiary_DiaryId(Long diaryId);
>>>>>>> dev

    List<Image> findAllByDiary(Diary diary);
}
