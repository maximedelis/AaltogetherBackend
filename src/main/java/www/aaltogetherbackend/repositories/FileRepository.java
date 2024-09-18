package www.aaltogetherbackend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import www.aaltogetherbackend.models.File;
import www.aaltogetherbackend.payloads.responses.FileNoDataResponse;

import java.util.List;

public interface FileRepository extends JpaRepository<File, Long> {
    @Query("SELECT f.id as id, f.name as name, f.type as type FROM File f")
    List<FileNoDataResponse> findAllFilesButData();

    @Query("SELECT f.id as id, f.name as name, f.type as type FROM File f WHERE f.uploader = :uploader")
    List<FileNoDataResponse> findAllFilesButDataByUploader(String uploader);

    void deleteById(Long id);
}
