package www.aaltogetherbackend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import www.aaltogetherbackend.models.Room;
import www.aaltogetherbackend.models.User;
import www.aaltogetherbackend.payloads.responses.FileNoDataInterface;

import java.util.Set;
import java.util.UUID;

public interface RoomRepository extends JpaRepository<Room, UUID> {

    Set<Room> findAllByAprivateFalse();

    Set<Room> findAllByHost(User user);

    boolean existsById(UUID id);

    Room findByCode(String code);

    boolean existsByIdAndSharedFilesId(UUID roomId, long fileId);

    @Query("SELECT r.sharedFiles FROM Room r WHERE r.id = :id")
    Set<FileNoDataInterface> findAllSharedFilesById(UUID id);
}
