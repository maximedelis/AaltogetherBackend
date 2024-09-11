package www.aaltogetherbackend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import www.aaltogetherbackend.models.RefreshToken;
import www.aaltogetherbackend.models.User;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);

    void deleteByUser(User user);
}
