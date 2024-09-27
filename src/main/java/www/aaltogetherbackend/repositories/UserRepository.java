package www.aaltogetherbackend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import www.aaltogetherbackend.models.User;
import www.aaltogetherbackend.payloads.responses.UserInfoInterface;

import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    User findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    User findByEmail(String email);

    @Query("SELECT u.username as username, u.email as email FROM User u WHERE u.username = :username")
    UserInfoInterface findUserInfoByUsername(String username);
}
