package www.aaltogetherbackend.services;

import org.springframework.stereotype.Service;
import www.aaltogetherbackend.models.Room;
import www.aaltogetherbackend.models.User;
import www.aaltogetherbackend.payloads.responses.RoomInfoResponse;
import www.aaltogetherbackend.repositories.RoomRepository;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
public class RoomService {

    private final RoomRepository roomRepository;

    public RoomService(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    public boolean checkExistsById(UUID id) {
        return roomRepository.existsById(id);
    }

    public Room getRoom(UUID id) {
        return roomRepository.findById(id).orElse(null);
    }

    public void saveRoom(Room room) {
        roomRepository.save(room);
    }

    public void deleteRoom(UUID id) {
        roomRepository.deleteById(id);
    }

    public Set<RoomInfoResponse> getPublicRooms() {
        Set<RoomInfoResponse> rooms = new HashSet<>();
        Set<Room> publicRooms = roomRepository.findAllByAprivateFalse();
        for (Room room : publicRooms) {
            rooms.add(new RoomInfoResponse(room.getId(), room.getName(), null, room.isAprivate(), room.getMaxUsers()));
        }
        return rooms;
    }

    public Set<RoomInfoResponse> getRoomsByHost(User user) {
        Set<RoomInfoResponse> rooms = new HashSet<>();
        Set<Room> userRooms = roomRepository.findAllByHost(user);
        for (Room room : userRooms) {
            rooms.add(new RoomInfoResponse(room.getId(), room.getName(), room.getCode(), room.isAprivate(), room.getMaxUsers()));
        }
        return rooms;
    }

    public RoomInfoResponse getRoomByCode(String code) {
        Room room = roomRepository.findByCode(code);
        if (room == null) {
            return null;
        }
        return new RoomInfoResponse(room.getId(), room.getName(), room.getCode(), room.isAprivate(), room.getMaxUsers());
    }

    public boolean isHost(UUID roomId, User user) {
        Room room = roomRepository.findById(roomId).orElse(null);
        return room != null && room.getHost().getId().equals(user.getId());
    }

    public boolean isFileShared(UUID roomId, long fileId) {
        return roomRepository.existsByIdAndSharedFilesId(roomId, fileId);
    }

}
