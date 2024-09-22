package www.aaltogetherbackend.services;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import www.aaltogetherbackend.models.File;
import www.aaltogetherbackend.repositories.FileRepository;

@Service
public class FileLoaderService {
    private final FileRepository fileRepository;

    public FileLoaderService(FileRepository fileRepository) {
        this.fileRepository = fileRepository;
    }

    public ResponseEntity<StreamingResponseBody> loadPartialFile(Long id, Long start, Long end) {
        File file = fileRepository.findById(id).orElse(null);
        if (file == null) {
            return null;
        }
        return null;
    }
}
