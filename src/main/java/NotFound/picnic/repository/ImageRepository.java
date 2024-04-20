package NotFound.picnic.repository;

import NotFound.picnic.domain.Image;
import NotFound.picnic.domain.Place;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ImageRepository extends JpaRepository<Image, Long> {
    Optional <Image> findImageUrlByDiary_RecordId(Long recordId);
}