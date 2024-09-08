package www.aaltogetherbackend.services;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import www.aaltogetherbackend.repositories.UserRepository;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if (checkExists(username)) {
            return userRepository.findByUsername(username);
        } else {
            throw new UsernameNotFoundException("User not found");
        }
    }

    public boolean checkExists(String username) {
        return userRepository.existsByUsername(username);
    }

}
