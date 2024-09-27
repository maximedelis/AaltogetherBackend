package www.aaltogetherbackend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import www.aaltogetherbackend.models.EmailConfirmationToken;
import www.aaltogetherbackend.models.User;

import java.util.Optional;

public interface EmailConfirmationTokenRepository extends JpaRepository<EmailConfirmationToken, Long> {
    Optional<EmailConfirmationToken> findByToken(String token);

    void deleteByToken(String token);

}
