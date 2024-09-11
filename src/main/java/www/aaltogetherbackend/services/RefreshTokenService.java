package www.aaltogetherbackend.services;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import www.aaltogetherbackend.models.RefreshToken;
import www.aaltogetherbackend.models.User;
import www.aaltogetherbackend.repositories.RefreshTokenRepository;
import www.aaltogetherbackend.repositories.UserRepository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository, UserRepository userRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
    }

    public String generateRefreshToken(String username) {
        User user = userRepository.findByUsername(username);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiryDate(Instant.now().plusSeconds(604800));

        refreshTokenRepository.save(refreshToken);
        return refreshToken.getToken();
    }

    public boolean isExpired(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token).orElse(null);
        assert refreshToken != null;
        if (refreshToken.getExpiryDate().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.delete(refreshToken);
            return true;
        }
        return false;
    }

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    @Transactional
    public void deleteByUser(User user) {
        refreshTokenRepository.deleteByUser(user);
    }

}
