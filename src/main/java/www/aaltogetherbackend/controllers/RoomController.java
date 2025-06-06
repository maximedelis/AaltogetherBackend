package www.aaltogetherbackend.controllers;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import www.aaltogetherbackend.models.Room;
import www.aaltogetherbackend.models.User;
import www.aaltogetherbackend.modules.SocketModule;
import www.aaltogetherbackend.payloads.requests.AddFilesRequest;
import www.aaltogetherbackend.payloads.requests.CreateRoomRequest;
import www.aaltogetherbackend.payloads.requests.UpdateRoomRequest;
import www.aaltogetherbackend.payloads.responses.ErrorMessageResponse;
import www.aaltogetherbackend.payloads.responses.RoomInfoResponse;
import www.aaltogetherbackend.services.RoomService;

import java.time.Instant;
import java.util.Random;
import java.util.Set;

@RestController
@RequestMapping("/api/room")
public class RoomController {

    private final RoomService roomService;
    private final SocketModule socketModule;

    public RoomController(RoomService roomService, SocketModule socketModule) {
        this.roomService = roomService;
        this.socketModule = socketModule;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createRoom(@Valid @RequestBody CreateRoomRequest createRoomRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();

        Room room = new Room();
        room.setName(createRoomRequest.name());
        room.setAprivate(createRoomRequest.aprivate());
        room.setHost(user);
        room.setFileSharingEnabled(createRoomRequest.isFileSharingEnabled());
        room.setChatEnabled(createRoomRequest.isChatEnabled());
        room.setAreCommandsEnabled(createRoomRequest.areCommandsEnabled());
        room.setMaxUsers(createRoomRequest.maxUsers());

        Instant now = Instant.now();
        room.setCreatedAt(now);
        room.setUpdatedAt(now);

        if (createRoomRequest.aprivate()) {
            Random random = new Random();
            int code = 100000 + random.nextInt(900000);
            String roomCode = String.format("%06d", code);
            room.setCode(roomCode);
        }
        roomService.saveRoom(room);
        return ResponseEntity.ok().body(roomService.getRoomInfoResponse(room.getId(), socketModule));
    }

    @PatchMapping("/update")
    public ResponseEntity<?> updateRoom(@Valid @RequestBody UpdateRoomRequest updateRoomRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();

        if (!roomService.checkExistsById(updateRoomRequest.id())) {
            return ResponseEntity.badRequest().body(new ErrorMessageResponse("Room does not exist"));
        }

        Room room = roomService.getRoom(updateRoomRequest.id());

        if (!room.getHost().getId().equals(user.getId())) {
            return ResponseEntity.badRequest().body(new ErrorMessageResponse("You are not the host of this room"));
        }

        room.setName(updateRoomRequest.name());

        if (updateRoomRequest.aprivate() && !room.isAprivate()) {
            room.setAprivate(true);
            if (room.getCode() == null) {
                Random random = new Random();
                int code = 100000 + random.nextInt(900000);
                String roomCode = String.format("%06d", code);
                room.setCode(roomCode);
            }
        }

        if (!updateRoomRequest.aprivate() && room.isAprivate()) {
            room.setAprivate(false);
            room.setCode(null);
        }

        room.setFileSharingEnabled(updateRoomRequest.isFileSharingEnabled());
        room.setChatEnabled(updateRoomRequest.isChatEnabled());
        room.setAreCommandsEnabled(updateRoomRequest.areCommandsEnabled());
        room.setMaxUsers(updateRoomRequest.maxUsers());

        Instant now = Instant.now();
        room.setUpdatedAt(now);

        roomService.saveRoom(room);

        socketModule.sendRoomUpdateInfo(room.getId());

        return ResponseEntity.ok().body(roomService.getRoomInfoResponse(room.getId(), socketModule));
    }


    @GetMapping("/get-rooms")
    public ResponseEntity<?> getRooms() {
        Set<RoomInfoResponse> rooms = getPublicRooms();
        Set<RoomInfoResponse> personalRooms = getPersonalRooms();
        rooms.addAll(personalRooms);
        return ResponseEntity.ok().body(rooms);

    }

    public Set<RoomInfoResponse> getPublicRooms() {
        return roomService.getPublicRooms(socketModule);
    }

    public Set<RoomInfoResponse> getPersonalRooms() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        return roomService.getRoomsByHost(user, socketModule);
    }

    @GetMapping("/get-room-by-code")
    public ResponseEntity<?> getRoomByCode(@RequestParam String code) {
        RoomInfoResponse room = roomService.getRoomByCode(code, socketModule);
        if (room == null) {
            return ResponseEntity.badRequest().body(new ErrorMessageResponse("Room does not exist"));
        }
        return ResponseEntity.ok().body(room);
    }

    @PostMapping("/add-files")
    public ResponseEntity<?> addFilesToRoom(@Valid @RequestBody AddFilesRequest addFilesRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        if (!roomService.checkExistsById(addFilesRequest.roomId())) {
            return ResponseEntity.badRequest().body(new ErrorMessageResponse("Room does not exist"));
        }
        if (!roomService.isHost(addFilesRequest.roomId(), user.getId()) && !roomService.isSharingEnabled(addFilesRequest.roomId())) {
            return ResponseEntity.badRequest().body(new ErrorMessageResponse("You cannot add files to this room"));
        }

        for (Long fileId : addFilesRequest.fileIds()) {
            roomService.addFileToRoom(addFilesRequest.roomId(), fileId);
        }

        socketModule.sendRoomUpdateInfo(addFilesRequest.roomId());

        return ResponseEntity.ok().body(roomService.getRoomInfoResponse(addFilesRequest.roomId(), socketModule));
    }

}
