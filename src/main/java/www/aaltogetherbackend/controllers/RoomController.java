package www.aaltogetherbackend.controllers;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import www.aaltogetherbackend.models.Room;
import www.aaltogetherbackend.models.User;
import www.aaltogetherbackend.payloads.requests.CreateRoomRequest;
import www.aaltogetherbackend.payloads.requests.JoinRoomRequest;
import www.aaltogetherbackend.payloads.requests.UpdateRoomRequest;
import www.aaltogetherbackend.payloads.responses.ErrorMessageResponse;
import www.aaltogetherbackend.services.RoomService;

@RestController
@RequestMapping("/api/room")
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createRoom(@Valid @RequestBody CreateRoomRequest createRoomRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();

        if (roomService.checkExists(createRoomRequest.name())) {
            return ResponseEntity.badRequest().body(new ErrorMessageResponse("Room already exists"));
        }
        Room room = new Room();
        room.setName(createRoomRequest.name());
        room.setAprivate(createRoomRequest.aprivate());
        room.setHost(user);
        if (createRoomRequest.aprivate()) {
            if (createRoomRequest.password() == null) {
                return ResponseEntity.badRequest().body(new ErrorMessageResponse("Password is required for private rooms"));
            }
            room.setPassword(createRoomRequest.password());
        }
        roomService.saveRoom(room);
        return ResponseEntity.ok().body(room.getId());
    }

    /* ACTUALLY USELESS
    @GetMapping("/join")
    public ResponseEntity<?> joinRoom(@Valid @RequestBody JoinRoomRequest joinRoomRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();

        if (!roomService.checkExists(joinRoomRequest.getName())) {
            return ResponseEntity.badRequest().body(new ErrorMessageResponse("Room does not exist"));
        }
        Room room = roomService.getRoom(joinRoomRequest.getName());

        if (room.getHost().getId().equals(user.getId())) {
            return ResponseEntity.ok().body(room.getId());
        }

        if (room.isAprivate() && !room.getPassword().equals(joinRoomRequest.getPassword())) {
            return ResponseEntity.badRequest().body(new ErrorMessageResponse("Incorrect password"));
        }

        return ResponseEntity.ok().body(room.getId());
    }
    */

    @PostMapping("/update")
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

        if (updateRoomRequest.aprivate() && updateRoomRequest.password() == null) {
            return ResponseEntity.badRequest().body(new ErrorMessageResponse("Password is required for private rooms"));
        }

        room.setAprivate(updateRoomRequest.aprivate());
        room.setPassword(updateRoomRequest.password());
        roomService.saveRoom(room);

        return ResponseEntity.ok().body(room.getId());
    }

}
