package www.aaltogetherbackend.services;

import com.mpatric.mp3agic.Mp3File;
import jakarta.transaction.Transactional;
import org.apache.tika.Tika;
import com.xuggle.xuggler.IContainer;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import www.aaltogetherbackend.exceptions.InvalidMediaFileException;
import www.aaltogetherbackend.models.File;
import www.aaltogetherbackend.models.User;
import www.aaltogetherbackend.payloads.responses.FileNoDataInterface;
import www.aaltogetherbackend.repositories.FileRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;
import java.util.Set;

@Service
public class FileService {

    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            // Video formats
            "video/mp4",
            "video/mpeg",
            "video/quicktime",
            "video/x-msvideo",
            "video/x-matroska",
            // Audio formats
            "audio/mpeg",
            "audio/mp4",
            "audio/wav",
            "audio/x-wav",
            "audio/ogg"
    );

    private final FileRepository fileRepository;
    private final Tika tika;

    public FileService(FileRepository fileRepository) {
        this.fileRepository = fileRepository;
        this.tika = new Tika();
    }

    public Long store(MultipartFile multipartFile, User uploader) throws IOException, InvalidMediaFileException {
        String filename = StringUtils.cleanPath(Objects.requireNonNull(multipartFile.getOriginalFilename()));

        byte[] content = multipartFile.getBytes();
        String detectedMimeType = tika.detect(content);

        Long duration = extractMediaDuration(content, detectedMimeType);

        if (!ALLOWED_MIME_TYPES.contains(detectedMimeType)) {
            throw new InvalidMediaFileException("File type not allowed. Only audio and video files are permitted.");
        }

        File file = new File(filename, detectedMimeType, uploader, content, duration);

        fileRepository.save(file);
        return file.getId();
    }

    private long extractMediaDuration(byte[] content, String mimeType) throws IOException {
        java.io.File tempFile = Files.createTempFile("media", null).toFile();
        try {
            Files.write(tempFile.toPath(), content);

            if (mimeType.startsWith("audio/")) {
                return extractAudioDuration(tempFile);
            } else if (mimeType.startsWith("video/")) {
                return extractVideoDuration(tempFile);
            }

            throw new IOException("Unsupported media type: " + mimeType);
        } finally {
            tempFile.delete();
        }
    }

    private long extractAudioDuration(java.io.File file) throws IOException {
        try {
            if (file.getName().toLowerCase().endsWith(".mp3")) {
                Mp3File mp3file = new Mp3File(file);
                return mp3file.getLengthInSeconds();
            }
            return extractVideoDuration(file);
        } catch (Exception e) {
            throw new IOException("Failed to extract audio duration", e);
        }
    }

    private long extractVideoDuration(java.io.File file) throws IOException {
        try {
            IContainer container = IContainer.make();
            if (container.open(file.getAbsolutePath(), IContainer.Type.READ, null) < 0) {
                throw new IOException("Failed to open media file");
            }

            long duration = container.getDuration();
            container.close();

            return duration / 1000000;
        } catch (Exception e) {
            throw new IOException("Failed to extract video duration", e);
        }
    }

    @Transactional
    public byte[] getDataById(Long id) {
        return fileRepository.findDataById(id);
    }

    public FileNoDataInterface getFileNoData(Long id) {
        return fileRepository.findFileNoDataById(id);
    }

    public Set<FileNoDataInterface> getAllFileNoData(User user) {
        return fileRepository.findAllFileNoDataByUploader(user);
    }

    public File getFileById(Long id) {
        return fileRepository.findById(id).orElse(null);
    }

    @Transactional
    public void updateFile(Long id, String name) {
        fileRepository.updateNameById(id, name);
    }

    @Transactional
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

    public String getContentType(Long id) {
        return fileRepository.findTypeById(id);
    }

    public String getName(Long id) {
        return fileRepository.findNameById(id);
    }

}

