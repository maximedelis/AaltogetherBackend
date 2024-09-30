package www.aaltogetherbackend.services;

import org.springframework.stereotype.Service;
import www.aaltogetherbackend.models.Room;
import www.aaltogetherbackend.models.User;
import www.aaltogetherbackend.modules.SocketModule;
import www.aaltogetherbackend.payloads.responses.FileNoDataInterface;
import www.aaltogetherbackend.payloads.responses.RoomInfoInterface;
import www.aaltogetherbackend.payloads.responses.RoomInfoResponse;
import www.aaltogetherbackend.repositories.RoomRepository;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
public class RoomService {

    private final RoomRepository roomRepository;
    private final FileService fileService;

    public RoomService(RoomRepository roomRepository, FileService fileService) {
        this.roomRepository = roomRepository;
        this.fileService = fileService;
    }

    public boolean checkExistsById(UUID id) {
        return roomRepository.existsById(id);
    }

    public Room getRoom(UUID id) {
        return roomRepository.findById(id).orElse(null);
    }

    public RoomInfoResponse getRoomInfoResponse(UUID id, SocketModule socketModule) {
        Room room = roomRepository.findById(id).orElse(null);
        if (room == null) {
            return null;
        }
        Set<FileNoDataInterface> sharedFiles = roomRepository.findAllSharedFilesById(room.getId());
        return new RoomInfoResponse(room.getId(), room.getName(), room.getCode(), room.isAprivate(), room.isFileSharingEnabled(), room.isChatEnabled(), room.areCommandsEnabled(), room.getMaxUsers(), room.getHost().getUsername(), room.getCreatedAt(), socketModule.getUsersInRoom(room.getId()), sharedFiles);
    }

    public void saveRoom(Room room) {
        roomRepository.save(room);
    }

    public void deleteRoom(UUID id) {
        roomRepository.deleteById(id);
    }

    public int getMaxUsers(UUID id) {
        return roomRepository.getMaxUsersById(id);
    }

    public Set<RoomInfoResponse> getPublicRooms(SocketModule socketModule) {
        Set<RoomInfoResponse> rooms = new HashSet<>();
        Set<RoomInfoInterface> publicRooms = roomRepository.findAllByAprivateFalse();
        for (RoomInfoInterface room : publicRooms) {
            rooms.add(this.getRoomInfoResponse(room.getId(), socketModule));
        }
        return rooms;
    }

    public Set<RoomInfoResponse> getRoomsByHost(User user, SocketModule socketModule) {
        Set<RoomInfoResponse> rooms = new HashSet<>();
        Set<RoomInfoInterface> userRooms = roomRepository.findAllByHost(user);
        for (RoomInfoInterface room : userRooms) {
            rooms.add(this.getRoomInfoResponse(room.getId(), socketModule));
        }
        return rooms;
    }

    public RoomInfoResponse getRoomByCode(String code, SocketModule socketModule) {
        RoomInfoInterface room = roomRepository.findByCode(code);
        if (room == null) {
            return null;
        }
        return this.getRoomInfoResponse(room.getId(), socketModule);
    }

    public boolean isHost(UUID roomId, UUID userId) {
        return roomRepository.getHostIdById(roomId).equals(userId);
    }

    public boolean isChatEnabled(UUID roomId) {
        return roomRepository.isChatEnabledById(roomId);
    }

    public boolean isSharingEnabled(UUID roomId) {
        return roomRepository.isFileSharingEnabledById(roomId);
    }

    public boolean isFileShared(UUID roomId, long fileId) {
        return roomRepository.existsByIdAndSharedFilesId(roomId, fileId);
    }

    public boolean areCommandsEnabled(UUID roomId) {
        return roomRepository.areCommandsEnabledById(roomId);
    }

    public void addFileToRoom(UUID roomId, long fileId) {
        Room room = roomRepository.findById(roomId).orElse(null);
        if (room != null) {
            room.addSharedFile(fileService.getFileById(fileId));
            this.saveRoom(room);
        }
    }

}
