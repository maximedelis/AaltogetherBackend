package www.aaltogetherbackend.services;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import www.aaltogetherbackend.models.File;
import www.aaltogetherbackend.models.User;
import www.aaltogetherbackend.payloads.responses.FileNoDataResponse;
import www.aaltogetherbackend.repositories.FileRepository;

import java.io.IOException;
import java.util.Objects;

@Service
public class FileService {
    private final FileRepository fileRepository;

    public FileService(FileRepository fileRepository) {
        this.fileRepository = fileRepository;
    }

    public void store(MultipartFile multipartFile, User uploader) throws IOException {
        String filename = StringUtils.cleanPath(Objects.requireNonNull(multipartFile.getOriginalFilename()));
        File file = new File(filename, multipartFile.getContentType(), uploader, multipartFile.getBytes());

        fileRepository.save(file);
    }

    public File getFile(Long id, User user) {
        File file = fileRepository.findById(id).orElse(null);
        if (file == null) {
            return null;
        }
        if (!file.getUploader().getId().equals(user.getId())) {
            return null;
        }

        return file;
    }

    public void deleteFile(Long id) {
        fileRepository.deleteById(id);
    }

    public boolean existsById(Long id) {
        return fileRepository.existsById(id);
    }

    public boolean isOwner(Long id, User user) {
        File file = fileRepository.findById(id).orElse(null);
        if (file == null) {
            return false;
        }
        return file.getUploader().getId().equals(user.getId());
    }
}
