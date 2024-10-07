package www.aaltogetherbackend.services;

import com.corundumstudio.socketio.SocketIOClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import www.aaltogetherbackend.commands.*;
import www.aaltogetherbackend.models.RoomQueue;

import java.util.*;

@Service
public class SocketService {
    private static final Logger log = LoggerFactory.getLogger(SocketService.class);

    private final RoomService roomService;
    private final JwtUtils jwtUtils;
    private final UserService userService;
    private final Map<UUID, RoomQueue> roomQueues;

    public SocketService(RoomService roomService, JwtUtils jwtUtils, UserService userService) {
        this.roomService = roomService;
        this.jwtUtils = jwtUtils;
        this.userService = userService;
        this.roomQueues = new HashMap<>();
    }

    public void sendCommand(UUID room, SocketIOClient senderClient, CommandType command, String commandValue) {
        if (this.notInRoom(room, senderClient)) {
            return;
        }

        UUID clientId = jwtUtils.getIdFromToken(senderClient.getHandshakeData().getSingleUrlParam("jwt"));

        if (!roomService.areCommandsEnabled(room) && !roomService.isHost(room, clientId)) {
            log.info("Client[{}] - Commands are disabled", senderClient.getSessionId().toString());
            senderClient.sendEvent("error", "Commands are disabled");
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

        UUID clientId = jwtUtils.getIdFromToken(senderClient.getHandshakeData().getSingleUrlParam("jwt"));

        if (!roomService.isChatEnabled(room) && !roomService.isHost(room, clientId)) {
            log.info("Client[{}] - Chat is disabled", senderClient.getSessionId().toString());
            senderClient.sendEvent("error", "Chat is disabled");
            return;
        }

        log.info("Message sent: {}", message);
        for (SocketIOClient clients : senderClient.getNamespace().getRoomOperations(room.toString()).getClients())
        {
            clients.sendEvent("get_message", new SocketGetMessage(userService.loadById(clientId).getUsername(), message));
        }
    }

    public void sendServerMessage(UUID room, SocketIOClient senderClient, String message, String username) {
        log.info("Message sent: {}", message);
        for (SocketIOClient clients : senderClient.getNamespace().getRoomOperations(room.toString()).getClients())
        {
            clients.sendEvent("get_info", new SocketGetInfo(username, message));
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
            clients.sendEvent("end_session", "The host has ended the session");
            clients.disconnect();
        }
        if (roomService.checkExistsById(room)) {
            roomService.deleteRoom(room);
        }
    }

    public void kickUser(UUID roomId, SocketIOClient client, String username) {
        UUID id = jwtUtils.getIdFromToken(client.getHandshakeData().getSingleUrlParam("jwt"));
        if (roomService.isHost(roomId, id)) {
            for (SocketIOClient clients : client.getNamespace().getRoomOperations(roomId.toString()).getClients()) {
                UUID clientId = jwtUtils.getIdFromToken(clients.getHandshakeData().getSingleUrlParam("jwt"));
                String clientUsername = userService.loadById(clientId).getUsername();
                if (clientUsername.equals(username)) {
                    clients.sendEvent("error", "You have been kicked from the room");
                    clients.disconnect();
                    this.sendServerMessage(roomId, client, "KICKED", username);
                }
            }
        }
    }

    private boolean notInRoom(UUID room, SocketIOClient senderClient) {
        if (!senderClient.getNamespace().getRoomOperations(room.toString()).getClients().contains(senderClient)) {
            log.info("Client[{}] - Not in room", senderClient.getSessionId().toString());
            senderClient.sendEvent("error", "You are not in the room");
            senderClient.disconnect();
            return true;
        }
        return false;
    }

    public void addToQueue(UUID room, SocketIOClient senderClient, String value) {
        if (this.notInRoom(room, senderClient)) {
            return;
        }

        UUID clientId = jwtUtils.getIdFromToken(senderClient.getHandshakeData().getSingleUrlParam("jwt"));

        if (!roomService.areCommandsEnabled(room) && !roomService.isHost(room, clientId)) {
            log.info("Client[{}] - Not host", senderClient.getSessionId().toString());
            senderClient.sendEvent("error", "Commands are disabled");
            return;
        }

        long fileId = Long.parseLong(value);

        if (!roomService.isFileShared(room, fileId)) {
            log.info("Client[{}] - File not shared", senderClient.getSessionId().toString());
            senderClient.sendEvent("error", "File not shared");
            return;
        }

        this.handleAddFileToQueue(room, fileId);

        Queue<Long> fileQueue = this.getSongQueue(room);
        for (SocketIOClient clients : senderClient.getNamespace().getRoomOperations(room.toString()).getClients()) {
            clients.sendEvent("get_queue", fileQueue);
        }

        log.info("Client[{}] - Added file to queue", senderClient.getSessionId().toString());
    }

    public Queue<Long> getSongQueue(UUID roomId) {
        RoomQueue roomQueue = roomQueues.get(roomId);
        if (roomQueue != null) {
            return roomQueue.getFileQueue();
        }
        return new LinkedList<>();
    }

    public void skipFile(UUID room, SocketIOClient senderClient) {
        if (this.notInRoom(room, senderClient)) {
            return;
        }

        UUID clientId = jwtUtils.getIdFromToken(senderClient.getHandshakeData().getSingleUrlParam("jwt"));

        if (!roomService.areCommandsEnabled(room) && !roomService.isHost(room, clientId)) {
            log.info("Client[{}] - Not host", senderClient.getSessionId().toString());
            senderClient.sendEvent("error", "Commands are disabled");
            return;
        }

        RoomQueue roomQueue = roomQueues.get(room);
        if (roomQueue != null) {
            roomQueue.getNextFile();
        }
        log.info("Client[{}] - Skipped file", senderClient.getSessionId().toString());
        Queue<Long> fileQueue = this.getSongQueue(room);
        for (SocketIOClient clients : senderClient.getNamespace().getRoomOperations(room.toString()).getClients()) {
            clients.sendEvent("get_queue", fileQueue);
        }
    }

    private void handleAddFileToQueue(UUID roomUUID, Long fileId) {
        RoomQueue roomQueue = roomQueues.computeIfAbsent(roomUUID, RoomQueue::new);
        roomQueue.addFileToQueue(fileId);
        log.info("File added to queue in room {}: {}", roomUUID, fileId);
    }

}
