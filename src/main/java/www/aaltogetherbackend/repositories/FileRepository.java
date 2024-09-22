package www.aaltogetherbackend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import www.aaltogetherbackend.models.File;

public interface FileRepository extends JpaRepository<File, Long> {
}
