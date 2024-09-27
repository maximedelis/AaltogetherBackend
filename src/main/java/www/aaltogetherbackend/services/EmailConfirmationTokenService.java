package www.aaltogetherbackend.services;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import www.aaltogetherbackend.models.EmailConfirmationToken;
import www.aaltogetherbackend.models.User;
import www.aaltogetherbackend.repositories.EmailConfirmationTokenRepository;

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

        emailConfirmationTokenRepository.save(emailConfirmationToken);
        return emailConfirmationToken.getToken();
    }

    public Optional<EmailConfirmationToken> findByToken(String token) {
        return emailConfirmationTokenRepository.findByToken(token);
    }

    @Transactional
    public void delete(String token) {
        emailConfirmationTokenRepository.deleteByToken(token);
    }

}
