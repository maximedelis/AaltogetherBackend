package www.aaltogetherbackend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import www.aaltogetherbackend.models.Room;
import www.aaltogetherbackend.models.User;

import java.util.Set;
import java.util.UUID;

public interface RoomRepository extends JpaRepository<Room, UUID> {

    Room findByName(String name);

    boolean existsByName(String name);

    Set<Room> findAllByHost(User user);

    boolean existsById(UUID id);
}
