package www.aaltogetherbackend.services;

import com.corundumstudio.socketio.SocketIOClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import www.aaltogetherbackend.commands.CommandType;
import www.aaltogetherbackend.commands.SocketCommand;
import www.aaltogetherbackend.commands.SocketMessage;

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
        if (this.notInRoom(room, senderClient)) {
            return;
        }
        log.info("Command sent: {}", command);
        for (
                SocketIOClient clients : senderClient.getNamespace().getRoomOperations(room.toString()).getClients()) {
            if (!clients.getSessionId().equals(senderClient.getSessionId())) {
                clients.sendEvent("get_command", new SocketCommand(command, commandValue));
            }
        }
    }

    public void sendMessage(UUID room, SocketIOClient senderClient, String message) {
        if (this.notInRoom(room, senderClient)) {
            return;
        }

        UUID id = jwtUtils.getIdFromToken(senderClient.getHandshakeData().getSingleUrlParam("jwt"));

        if (!roomService.isChatEnabled(room) && !roomService.isHost(room, id)) {
            log.info("Client[{}] - Chat is disabled", senderClient.getSessionId().toString());
            senderClient.sendEvent("error", "Chat is disabled");
            return;
        }

        log.info("Message sent: {}", message);
        for (SocketIOClient clients : senderClient.getNamespace().getRoomOperations(room.toString()).getClients())
        {
            clients.sendEvent("get_message", new SocketMessage(message));
        }
    }

    public void sendServerMessage(UUID room, SocketIOClient senderClient, String message) {
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
        return client.getNamespace().getRoomOperations(room.toString()).getClients().size() < roomService.getMaxUsers(room) + 1;
    }

    public void endSession(UUID room, SocketIOClient client) {
        if (this.notInRoom(room, client)) {
            return;
        }

        UUID id = jwtUtils.getIdFromToken(client.getHandshakeData().getSingleUrlParam("jwt"));

        if (!roomService.isHost(room, id)) {
            log.info("Client[{}] - Not host", client.getSessionId().toString());
            client.sendEvent("error", "You are not the host");
            return;
        }

        for (SocketIOClient clients : client.getNamespace().getRoomOperations(room.toString()).getClients()) {
            clients.disconnect();
        }
        if (roomService.checkExistsById(room)) {
            roomService.deleteRoom(room);
        }
    }

    public void kickUser(UUID roomId, SocketIOClient client, String username) {
        UUID id = jwtUtils.getIdFromToken(client.getHandshakeData().getSingleUrlParam("jwt"));
        String host = userService.loadById(id).getUsername();
        if (roomService.isHost(roomId, id)) {
            for (SocketIOClient clients : client.getNamespace().getRoomOperations(roomId.toString()).getClients()) {
                String clientJwt = clients.getHandshakeData().getSingleUrlParam("jwt");
                String clientUsername = jwtUtils.getUsernameFromToken(clientJwt);
                if (clientUsername.equals(username)) {
                    clients.disconnect();
                }
            }
        }
    }

    public boolean notInRoom(UUID room, SocketIOClient senderClient) {
        if (!senderClient.getNamespace().getRoomOperations(room.toString()).getClients().contains(senderClient)) {
            log.info("Client[{}] - Not in room", senderClient.getSessionId().toString());
            senderClient.sendEvent("error", "You are not in the room");
            senderClient.disconnect();
            return true;
        }
        return false;
    }

}
