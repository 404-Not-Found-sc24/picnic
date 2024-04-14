package NotFound.picnic.repository;

import NotFound.picnic.domain.Record;

import java.util.Optional;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecordRepository extends JpaRepository<Record, Long> {
	void deleteBytitle(String title);

}
