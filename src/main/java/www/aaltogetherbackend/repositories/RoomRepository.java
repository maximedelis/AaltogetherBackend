package www.aaltogetherbackend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import www.aaltogetherbackend.models.Room;
import www.aaltogetherbackend.models.User;
import www.aaltogetherbackend.payloads.responses.FileNoDataInterface;
import www.aaltogetherbackend.payloads.responses.RoomInfoInterface;

import java.util.Set;
import java.util.UUID;

public interface RoomRepository extends JpaRepository<Room, UUID> {

    @Query("SELECT r.id as id, r.name as name, r.code as code, r.aprivate as isPrivate, r.isFileSharingEnabled as isFileSharingEnabled, r.maxUsers as maxUsers, r.host.username as host FROM Room r WHERE r.aprivate = false")
    Set<RoomInfoInterface> findAllByAprivateFalse();

    @Query("SELECT r.id as id, r.name as name, r.code as code, r.aprivate as isPrivate, r.isFileSharingEnabled as isFileSharingEnabled, r.maxUsers as maxUsers, r.host.username as host FROM Room r WHERE r.host = :host")
    Set<RoomInfoInterface> findAllByHost(@Param("host") User host);

    boolean existsById(UUID id);

    @Query("SELECT r.id as id, r.name as name, r.code as code, r.aprivate as isPrivate, r.isFileSharingEnabled as isFileSharingEnabled, r.maxUsers as maxUsers, r.host.username as host FROM Room r WHERE r.code = :code")
    RoomInfoInterface findByCode(String code);

    boolean existsByIdAndSharedFilesId(UUID roomId, long fileId);

    @Query("SELECT f.name as name, f.id as id, f.type as type, f.uploader.username as uploader FROM Room r JOIN r.sharedFiles f WHERE r.id = :id")
    Set<FileNoDataInterface> findAllSharedFilesById(@Param("id") UUID id);
}
