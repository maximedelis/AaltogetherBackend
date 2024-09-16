package www.aaltogetherbackend.services;

import com.corundumstudio.socketio.SocketIOClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import www.aaltogetherbackend.commands.CommandResponse;
import www.aaltogetherbackend.models.Room;

import java.util.UUID;

@Service
public class SocketService {
    private static final Logger log = LoggerFactory.getLogger(SocketService.class);

    private final RoomService roomService;

    public SocketService(RoomService roomService) {
        this.roomService = roomService;
    }

    public void sendCommand(UUID room, SocketIOClient senderClient, String command, String commandValue) {
        log.info("Command sent: {}", command);
        for (
                SocketIOClient clients : senderClient.getNamespace().getRoomOperations(room.toString()).getClients()) {
            if (!clients.getSessionId().equals(senderClient.getSessionId())) {
                clients.sendEvent("get_command", new CommandResponse(command, commandValue));
            }
        }
    }

    public void deleteRoomIfEmpty(UUID room, SocketIOClient client) {
        if (client.getNamespace().getRoomOperations(room.toString()).getClients().isEmpty()) {
            roomService.deleteRoom(room);
        }
    }

    public boolean hasSpace(UUID room, SocketIOClient client) {
        Room currentRoom = roomService.getRoom(room);
        return client.getNamespace().getRoomOperations(room.toString()).getClients().size() < currentRoom.getMaxUsers() + 1;
    }

    public void endSession(UUID room, SocketIOClient client) {
        for (SocketIOClient clients : client.getNamespace().getRoomOperations(room.toString()).getClients()) {
            clients.disconnect();
        }
        roomService.deleteRoom(room);
    }
}
