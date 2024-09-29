package www.aaltogetherbackend.services;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import www.aaltogetherbackend.models.User;
import www.aaltogetherbackend.repositories.UserRepository;

import java.util.UUID;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Mandatory method for UserDetailsService
    public User loadUserByUsername(String username) {
        if (checkExists(username)) {
            return userRepository.findByUsername(username);
        } else {
            throw new UsernameNotFoundException("User not found");
        }
    }

    public boolean checkExists(String username) {
        return userRepository.existsByUsername(username);
    }

    public User loadById(UUID id) {
        return userRepository.findById(id).orElse(null);
    }

}
