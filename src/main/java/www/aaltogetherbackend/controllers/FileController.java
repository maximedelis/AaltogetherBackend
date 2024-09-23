package www.aaltogetherbackend.controllers;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import www.aaltogetherbackend.models.File;
import www.aaltogetherbackend.models.User;
import www.aaltogetherbackend.payloads.responses.ErrorMessageResponse;
import www.aaltogetherbackend.payloads.responses.FileNoDataResponse;
import www.aaltogetherbackend.payloads.responses.MessageResponse;
import www.aaltogetherbackend.services.FileService;

import java.io.IOException;
import java.io.InputStream;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private final FileService fileService;
    private final ResourceLoader resourceLoader;

    public FileController(FileService fileService, ResourceLoader resourceLoader) {
        this.fileService = fileService;
        this.resourceLoader = resourceLoader;
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestBody MultipartFile file) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();

        try {
            fileService.store(file, user);
        } catch (IOException e) {
            return ResponseEntity.badRequest().body(new ErrorMessageResponse("Could not upload file"));
        }
        return ResponseEntity.ok().body(new MessageResponse("File uploaded successfully"));
    }

    @GetMapping("/info/{id}")
    public ResponseEntity<?> getFileInfo(@PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();

        if (!fileService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        if (!fileService.isOwner(id, user)) {
            return ResponseEntity.status(401).build();
        }

        File file = fileService.getFile(id);

        return ResponseEntity.ok().body(new FileNoDataResponse(file.getId(), file.getName(), file.getType(), file.getUploader().getUsername()));
    }

    // User should own the file
    @GetMapping("/download/{id}")
    public ResponseEntity<byte[]> downloadFile(@PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();

        if (!fileService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        if (!fileService.isOwner(id, user)) {
            return ResponseEntity.status(401).build();
        }

        File file = fileService.getFile(id);

        String contentType = file.getType();
        if (contentType == null) {
            contentType = "application/octet-stream";
        }
        String headerValue = "attachment; filename=\"" + file.getName() + "\"";

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, headerValue)
                .body(file.getData());
    }

    // User should own the file
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteFile(@PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();

        if (!fileService.existsById(id)) {
            return ResponseEntity.badRequest().body(new ErrorMessageResponse("File not found"));
        }

        if (!fileService.isOwner(id, user)) {
            return ResponseEntity.badRequest().body(new ErrorMessageResponse("You are not the owner of this file"));
        }

        fileService.deleteFile(id);
        return ResponseEntity.ok().body(new MessageResponse("File deleted successfully"));
    }

    @GetMapping("/play/{id}")
    public ResponseEntity<StreamingResponseBody> streamVideo(@PathVariable Long id,
                                                             @RequestHeader(value = "Range", required = false) String rangeHeader) throws IOException {
        String audioPath = "db:" + id;
        Resource audioResource = resourceLoader.getResource(audioPath);
        String contentType = fileService.getContentType(id);

        long contentLength = audioResource.contentLength();

        long rangeStart;
        long rangeEnd = contentLength - 1;

        if (rangeHeader != null) {
            String[] ranges = rangeHeader.substring("bytes=".length()).split("-");
            rangeStart = Long.parseLong(ranges[0]);
            if (ranges.length > 1) {
                rangeEnd = Long.parseLong(ranges[1]);
            }
        } else {
            rangeStart = 0;
        }

        long contentSize = rangeEnd - rangeStart + 1;

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", contentType);
        headers.add("Accept-Ranges", "bytes");
        headers.add("Content-Length", String.valueOf(contentSize));
        headers.add("Content-Range", String.format("bytes %d-%d/%d", rangeStart, rangeEnd, contentLength));

        StreamingResponseBody responseBody = outputStream -> {
            try (InputStream inputStream = audioResource.getInputStream()) {
                inputStream.skip(rangeStart);
                byte[] buffer = new byte[4096];
                int bytesRead;
                long totalBytesRead = 0;
                while ((bytesRead = inputStream.read(buffer)) != -1 && totalBytesRead < contentSize) {
                    outputStream.write(buffer, 0, bytesRead);
                    totalBytesRead += bytesRead;
                }
            }
        };

        return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                .headers(headers)
                .body(responseBody);
    }

}
