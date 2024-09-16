package www.aaltogetherbackend.services;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import www.aaltogetherbackend.models.EmailConfirmationToken;
import www.aaltogetherbackend.models.User;
import www.aaltogetherbackend.repositories.EmailConfirmationTokenRepository;
import www.aaltogetherbackend.repositories.UserRepository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class EmailConfirmationTokenService {

    private final EmailConfirmationTokenRepository emailConfirmationTokenRepository;

    public EmailConfirmationTokenService(EmailConfirmationTokenRepository emailConfirmationTokenRepository) {
        this.emailConfirmationTokenRepository = emailConfirmationTokenRepository;
    }

    public String generateEmailVerificationToken(User user) {
        EmailConfirmationToken emailConfirmationToken = new EmailConfirmationToken();
        emailConfirmationToken.setUser(user);
        emailConfirmationToken.setToken(UUID.randomUUID().toString());

        emailConfirmationToken.setExpiryDate(Instant.now().plusSeconds(86400));

        emailConfirmationTokenRepository.save(emailConfirmationToken);
        return emailConfirmationToken.getToken();
    }

    public boolean isExpired(String token) {
        EmailConfirmationToken emailConfirmationToken = emailConfirmationTokenRepository.findByToken(token).orElse(null);
        assert emailConfirmationToken != null;
        if (emailConfirmationToken.getExpiryDate().compareTo(Instant.now()) < 0) {
            emailConfirmationTokenRepository.delete(emailConfirmationToken);
            return true;
        }
        return false;
    }

    public Optional<EmailConfirmationToken> findByToken(String token) {
        return emailConfirmationTokenRepository.findByToken(token);
    }

    @Transactional
    public void deleteByUser(User user) {
        emailConfirmationTokenRepository.deleteByUser(user);
    }

}
