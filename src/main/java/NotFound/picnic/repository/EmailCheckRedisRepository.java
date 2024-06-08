package NotFound.picnic.repository;

import NotFound.picnic.domain.EmailCheck;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmailCheckRedisRepository extends CrudRepository<EmailCheck, String> {
    EmailCheck findByEmail(String email);
}
