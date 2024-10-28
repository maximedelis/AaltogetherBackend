package www.aaltogetherbackend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import www.aaltogetherbackend.models.File;
import www.aaltogetherbackend.models.User;
import www.aaltogetherbackend.payloads.responses.FileNoDataInterface;

import java.util.Set;

public interface FileRepository extends JpaRepository<File, Long> {
    @Query("SELECT f.id as id, f.name as name, f.type as type, f.uploader.username as uploader, f.duration as duration FROM File f WHERE f.id = :id")
    FileNoDataInterface findFileNoDataById(Long id);

    @Query("SELECT f.id as id, f.name as name, f.type as type, f.uploader.username as uploader, f.duration as duration FROM File f WHERE f.uploader = :uploader")
    Set<FileNoDataInterface> findAllFileNoDataByUploader(User uploader);

    @Query("SELECT f.type FROM File f WHERE f.id = :id")
    String findTypeById(Long id);

    @Query("SELECT f.name FROM File f WHERE f.id = :id")
    String findNameById(Long id);

    @Query("SELECT f.data FROM File f WHERE f.id = :id")
    byte[] findDataById(Long id);

    boolean existsById(Long id);

    @Modifying
    @Query("UPDATE File f SET f.name = :name WHERE f.id = :id")
    void updateNameById(Long id, String name);

}
