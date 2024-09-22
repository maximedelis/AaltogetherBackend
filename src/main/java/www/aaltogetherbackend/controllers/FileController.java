package www.aaltogetherbackend.controllers;

import org.springframework.http.HttpHeaders;
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

@RestController
@RequestMapping("/api/files")
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
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

        File file = fileService.getFile(id, user);

        if (file == null) {
            return ResponseEntity.badRequest().body(new ErrorMessageResponse("File not found"));
        }

        return ResponseEntity.ok().body(new FileNoDataResponse(file.getId(), file.getName(), file.getType(), file.getUploader().getUsername()));
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<byte[]> downloadFile(@PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();

        File file = fileService.getFile(id, user);

        if (file == null) {
            return ResponseEntity.notFound().build();
        }

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
    public ResponseEntity<StreamingResponseBody> playFile(@PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();

        File file = fileService.getFile(id, user);

        if (file == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(file.getType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"")
                .body(outputStream -> {
                    try {
                        outputStream.write(file.getData());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
    }

}
