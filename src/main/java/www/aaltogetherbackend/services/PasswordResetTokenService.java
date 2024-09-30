package www.aaltogetherbackend.services;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import www.aaltogetherbackend.models.PasswordResetToken;
import www.aaltogetherbackend.models.User;
import www.aaltogetherbackend.repositories.PasswordResetTokenRepository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class PasswordResetTokenService {
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    public PasswordResetTokenService(PasswordResetTokenRepository passwordResetTokenRepository) {
        this.passwordResetTokenRepository = passwordResetTokenRepository;
    }

    public String generatePasswordResetToken(User user) {
        PasswordResetToken passwordResetToken = new PasswordResetToken();
        passwordResetToken.setUser(user);
        passwordResetToken.setToken(UUID.randomUUID().toString());

        passwordResetToken.setExpiryDate(Instant.now().plusSeconds(900));

        passwordResetTokenRepository.save(passwordResetToken);
        return passwordResetToken.getToken();
    }

    public Optional<PasswordResetToken> findByToken(String token) {
        return passwordResetTokenRepository.findByToken(token);
    }

    public boolean isExpired(String token) {
        PasswordResetToken passwordResetToken = passwordResetTokenRepository.findByToken(token).orElse(null);
        assert passwordResetToken != null;
        if (passwordResetToken.getExpiryDate().compareTo(Instant.now()) < 0) {
            passwordResetTokenRepository.delete(passwordResetToken);
            return true;
        }
        return false;
    }

    @Transactional
    public void delete(String token) {
        passwordResetTokenRepository.deleteByToken(token);
    }

    @Transactional
    public void deleteByUser(User user) {
        passwordResetTokenRepository.deleteByUser(user);
    }
}
