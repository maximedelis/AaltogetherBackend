package www.aaltogetherbackend.modules;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.corundumstudio.socketio.listener.DataListener;
import com.corundumstudio.socketio.listener.DisconnectListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import www.aaltogetherbackend.commands.SocketCommand;
import www.aaltogetherbackend.commands.SocketJoinRoom;
import www.aaltogetherbackend.commands.SocketMessage;
import www.aaltogetherbackend.services.CommandHandlerService;
import www.aaltogetherbackend.services.JwtUtils;
import www.aaltogetherbackend.services.RoomService;
import www.aaltogetherbackend.services.SocketService;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Component
public class SocketModule {

    private final RoomService roomService;
    private final JwtUtils jwtUtils;

    private static final Logger log = LoggerFactory.getLogger(SocketModule.class);
    private final SocketService socketService;
    private final SocketIOServer socketIOServer;
    private final CommandHandlerService commandHandlerService;

    public SocketModule(SocketIOServer server, SocketService socketService, RoomService roomService, JwtUtils jwtUtils, SocketIOServer socketIOServer, CommandHandlerService commandHandlerService) {
        this.socketService = socketService;
        this.roomService = roomService;
        this.jwtUtils = jwtUtils;
        this.socketIOServer = socketIOServer;
        this.commandHandlerService = commandHandlerService;

        server.addConnectListener(onConnected());
        server.addDisconnectListener(onDisconnected());
        server.addEventListener("command", SocketCommand.class, onCommandReceived());
        server.addEventListener("message", SocketMessage.class, onMessageReceived());
        server.addEventListener("join", SocketJoinRoom.class, onJoinRoom());
    }

    private DataListener<SocketCommand> onCommandReceived() {
        return (senderClient, data, ackSender) -> {
            UUID roomUUID = UUID.fromString(senderClient.getHandshakeData().getSingleUrlParam("room"));
            log.info("Command received: {}", data.command());
            commandHandlerService.executeCommand(data.command(),
                    roomUUID,
                    senderClient,
                    data.value());
        };
    }

    private DataListener<SocketMessage> onMessageReceived() {
        return (senderClient, data, ackSender) -> {
            UUID roomUUID = UUID.fromString(senderClient.getHandshakeData().getSingleUrlParam("room"));

            log.info("Message received: {}", data.message());
            socketService.sendMessage(roomUUID,
                    senderClient,
                    data.message());
        };
    }

    private ConnectListener onConnected() {
        return (client) -> {
            String jwt = client.getHandshakeData().getSingleUrlParam("jwt");

            if (!jwtUtils.verifyToken(jwt)) {
                log.info("Socket ID[{}] Invalid JWT", client.getSessionId().toString());
                client.sendEvent("error", "Invalid JWT");
                client.disconnect();
                return;
            }

            log.info("Socket ID[{}] Connected to chat module through", client.getSessionId().toString());
        };
    }

    private DataListener<SocketJoinRoom> onJoinRoom() {
        return (client, data, ackSender) -> {

            if (!roomService.checkExistsById(data.room())) {
                log.info("Socket ID[{}]  Room not found", client.getSessionId().toString());
                client.sendEvent("error", "Room not found");
                client.disconnect();
                return;
            }

            if (!socketService.hasSpace(data.room(), client)) {
                log.info("Socket ID[{}] - room[{}]  Room is full", client.getSessionId().toString(), data.room());
                client.sendEvent("error", "Room is full");
                client.disconnect();
                return;
            }

            String username = jwtUtils.getUsernameFromToken(client.getHandshakeData().getSingleUrlParam("jwt"));

            if (this.isInRoom(data.room(), username)) {
                log.info("Socket ID[{}] - room[{}]  Already in this room", client.getSessionId().toString(), data.room());
                client.sendEvent("error", "Already in this room");
                client.disconnect();
                return;
            }

            client.joinRoom(data.room().toString());
            client.sendEvent("room_info", roomService.getRoomInfoResponse(data.room(), this));
            socketService.sendServerMessage(data.room(), client, username + " has joined the room.");
            log.info("Socket ID[{}] - room[{}]  Joined room", client.getSessionId().toString(), data.room());
        };
    }

    private DisconnectListener onDisconnected() {
        return client -> {
            UUID roomUUID = UUID.fromString(client.getHandshakeData().getSingleUrlParam("room"));
            String jwt = client.getHandshakeData().getSingleUrlParam("jwt");
            String username = jwtUtils.getUsernameFromToken(jwt);

            if (!this.isInRoom(roomUUID, username)) {
                socketService.sendServerMessage(roomUUID, client, username + " has left the room.");
            }

            socketService.deleteRoomIfEmpty(roomUUID, client);
            log.info("Client[{}] - Disconnected from chat module.", client.getSessionId().toString());
        };
    }

    public Set<String> getUsersInRoom(UUID room) {
        Set<String> users = new HashSet<>();
        Collection<SocketIOClient> clients = socketIOServer.getNamespace("").getRoomOperations(room.toString()).getClients();
        for (SocketIOClient client : clients) {
            String jwt = client.getHandshakeData().getSingleUrlParam("jwt");
            String username = jwtUtils.getUsernameFromToken(jwt);
            users.add(username);
        }
        return users;
    }

    public boolean isInRoom(UUID room, String username) {
        Set<String> users = this.getUsersInRoom(room);
        for (String user : users) {
            if (user.equals(username)) {
                return true;
            }
        }
        return false;
    }

    public void sendRoomUpdateInfo(UUID room) {
        Collection<SocketIOClient> clients = socketIOServer.getNamespace("").getRoomOperations(room.toString()).getClients();
        for (SocketIOClient client : clients) {
            client.sendEvent("room_info", roomService.getRoomInfoResponse(room, this));
        }
    }

}
