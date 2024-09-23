package www.aaltogetherbackend.repositories;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Repository;
import www.aaltogetherbackend.modules.DatabaseLoader;
import www.aaltogetherbackend.services.FileService;

@Repository
public class DatabaseLoaderImpl implements DatabaseLoader {
    private final FileService fileService;

    public DatabaseLoaderImpl(FileService fileService) {
        this.fileService = fileService;
    }

    public Resource getResource(Long id) {
        byte[] byteArray = fileService.getFile(id).getData();
        return new ByteArrayResource(byteArray);
    }
}
