package www.aaltogetherbackend.controllers;

import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import www.aaltogetherbackend.exceptions.InvalidMediaFileException;
import www.aaltogetherbackend.models.User;
import www.aaltogetherbackend.modules.SocketModule;
import www.aaltogetherbackend.payloads.requests.PlayRequest;
import www.aaltogetherbackend.payloads.requests.UpdateFileRequest;
import www.aaltogetherbackend.payloads.responses.ErrorMessageResponse;
import www.aaltogetherbackend.payloads.responses.MessageResponse;
import www.aaltogetherbackend.services.FileService;
import www.aaltogetherbackend.services.RoomService;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private final FileService fileService;
    private final ResourceLoader resourceLoader;
    private final SocketModule socketModule;
    private final RoomService roomService;

    public FileController(FileService fileService, ResourceLoader resourceLoader, SocketModule socketModule, RoomService roomService) {
        this.fileService = fileService;
        this.resourceLoader = resourceLoader;
        this.socketModule = socketModule;
        this.roomService = roomService;
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestBody MultipartFile file) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();

        try {
            Long fileId = fileService.store(file, user);
            return ResponseEntity.ok().body(fileService.getFileNoData(fileId));
        } catch (IOException | InvalidMediaFileException e) {
            return ResponseEntity.badRequest().body(new ErrorMessageResponse(e.getMessage()));
        }
    }

    @PatchMapping("/update/{id}")
    public ResponseEntity<?> updateFile(@PathVariable Long id, @Valid @RequestBody UpdateFileRequest updateFileRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();

        if (!fileService.existsById(id)) {
            return ResponseEntity.badRequest().body(new ErrorMessageResponse("File not found"));
        }

        if (!fileService.isOwner(id, user)) {
            return ResponseEntity.badRequest().body(new ErrorMessageResponse("You are not the owner of this file"));
        }

        fileService.updateFile(id, updateFileRequest.name());
        return ResponseEntity.ok().body(fileService.getFileNoData(id));
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

        return ResponseEntity.ok().body(fileService.getFileNoData(id));
    }

    @GetMapping("/my-files")
    public ResponseEntity<?> getMyFiles() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();

        return ResponseEntity.ok().body(fileService.getAllFileNoData(user));
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

        String filePath = "db:" + id;
        Resource resource = resourceLoader.getResource(filePath);

        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }

        String contentType = fileService.getContentType(id);
        if (contentType == null) {
            contentType = "application/octet-stream";
        }
        String headerValue = "attachment; filename=\"" + fileService.getName(id) + "\"";

        try (InputStream inputStream = resource.getInputStream()) {
            byte[] fileData = inputStream.readAllBytes();
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, headerValue)
                    .body(fileData);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
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

    @GetMapping("/play")
    public ResponseEntity<StreamingResponseBody> streamFile(@RequestParam String roomId,
                                                            @RequestParam long fileId,
                                                            @RequestHeader(value = "Range", required = false) String rangeHeader) throws IOException {

//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        User user = (User) authentication.getPrincipal();
//        String username = user.getUsername();

        if (!roomService.isFileShared(UUID.fromString(roomId), fileId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

//        if (!socketModule.isInRoom(UUID.fromString(roomId), username)) {
//            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
//        }

        String filePath = "db:" + fileId;
        Resource resource = resourceLoader.getResource(filePath);

        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }

        String contentType = fileService.getContentType(fileId);

        long contentLength = resource.contentLength();

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
        headers.add("Content-Type", MediaType.parseMediaType(contentType).toString());
        headers.add("Accept-Ranges", "bytes");
        headers.add("Content-Length", String.valueOf(contentSize));
        headers.add("Content-Range", String.format("bytes %d-%d/%d", rangeStart, rangeEnd, contentLength));

        StreamingResponseBody responseBody = outputStream -> {
            try (InputStream inputStream = resource.getInputStream()) {
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
