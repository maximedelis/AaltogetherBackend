package www.aaltogetherbackend.services;

import com.corundumstudio.socketio.SocketIOClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import www.aaltogetherbackend.commands.CommandType;
import www.aaltogetherbackend.commands.SocketCommand;
import www.aaltogetherbackend.commands.SocketMessage;
import www.aaltogetherbackend.models.Room;
import www.aaltogetherbackend.models.User;

import java.util.UUID;

@Service
public class SocketService {
    private static final Logger log = LoggerFactory.getLogger(SocketService.class);

    private final RoomService roomService;
    private final JwtUtils jwtUtils;
    private final UserService userService;

    public SocketService(RoomService roomService, JwtUtils jwtUtils, UserService userService) {
        this.roomService = roomService;
        this.jwtUtils = jwtUtils;
        this.userService = userService;
    }

    public void sendCommand(UUID room, SocketIOClient senderClient, CommandType command, String commandValue) {
        log.info("Command sent: {}", command);
        for (
                SocketIOClient clients : senderClient.getNamespace().getRoomOperations(room.toString()).getClients()) {
            if (!clients.getSessionId().equals(senderClient.getSessionId())) {
                clients.sendEvent("get_command", new SocketCommand(command, commandValue));
            }
        }
    }

    public void sendMessage(UUID room, SocketIOClient senderClient, String message) {
        log.info("Message sent: {}", message);
        for (SocketIOClient clients : senderClient.getNamespace().getRoomOperations(room.toString()).getClients())
        {
            clients.sendEvent("get_message", new SocketMessage(message));
        }
    }

    public void deleteRoomIfEmpty(UUID room, SocketIOClient client) {
        if (client.getNamespace().getRoomOperations(room.toString()).getClients().isEmpty()) {
            if (roomService.checkExistsById(room)) {
                roomService.deleteRoom(room);
            }
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
        if (roomService.checkExistsById(room)) {
            roomService.deleteRoom(room);
        }
    }

    public void kickUser(UUID roomId, SocketIOClient client, String username) {
        String jwt = client.getHandshakeData().getSingleUrlParam("jwt");
        String host = jwtUtils.getUsernameFromToken(jwt);
        User hostUser = userService.loadUserByUsername(host);
        if (roomService.isHost(roomId, hostUser)) {
            for (SocketIOClient clients : client.getNamespace().getRoomOperations(roomId.toString()).getClients()) {
                String clientJwt = clients.getHandshakeData().getSingleUrlParam("jwt");
                String clientUsername = jwtUtils.getUsernameFromToken(clientJwt);
                if (clientUsername.equals(username)) {
                    clients.disconnect();
                }
            }
        }
    }
}
