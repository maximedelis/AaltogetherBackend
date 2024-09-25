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
import www.aaltogetherbackend.commands.SocketMessage;
import www.aaltogetherbackend.models.Room;
import www.aaltogetherbackend.payloads.responses.UsernameInfoResponse;
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
            String roomUUID = client.getHandshakeData().getSingleUrlParam("room");
            String jwt = client.getHandshakeData().getSingleUrlParam("jwt");

            if (!jwtUtils.verifyToken(jwt)) {
                log.info("Socket ID[{}] - room[{}]  Invalid JWT", client.getSessionId().toString(), roomUUID);
                client.disconnect();
                return;
            }

            Room room = roomService.getRoom(UUID.fromString(roomUUID));

            if (room == null) {
                log.info("Socket ID[{}] - room[{}]  Room not found", client.getSessionId().toString(), roomUUID);
                client.disconnect();
                return;
            }

            if (!socketService.hasSpace(UUID.fromString(roomUUID), client)) {
                log.info("Socket ID[{}] - room[{}]  Room is full", client.getSessionId().toString(), roomUUID);
                client.disconnect();
                return;
            }

            client.joinRoom(roomUUID);

            //String clientUsername = jwtUtils.getUsernameFromToken(jwt);
            //socketService.sendCommand(UUID.fromString(roomUUID), client, "join", clientUsername);

            log.info("Socket ID[{}] - room[{}]  Connected to chat module through", client.getSessionId().toString(), room);
        };
    }

    private DisconnectListener onDisconnected() {
        return client -> {
            UUID roomUUID = UUID.fromString(client.getHandshakeData().getSingleUrlParam("room"));
            String jwt = client.getHandshakeData().getSingleUrlParam("jwt");
            //String clientUsername = jwtUtils.getUsernameFromToken(jwt);
            //socketService.sendCommand(roomUUID, client, "leave", clientUsername);
            socketService.deleteRoomIfEmpty(roomUUID, client);
            log.info("Client[{}] - Disconnected from chat module.", client.getSessionId().toString());
        };
    }

    public Set<UsernameInfoResponse> getUsersInRoom(UUID room) {
        Set<UsernameInfoResponse> users = new HashSet<>();
        Collection<SocketIOClient> clients = socketIOServer.getNamespace("").getRoomOperations(room.toString()).getClients();
        for (SocketIOClient client : clients) {
            String jwt = client.getHandshakeData().getSingleUrlParam("jwt");
            String username = jwtUtils.getUsernameFromToken(jwt);
            users.add(new UsernameInfoResponse(username));
        }
        return users;
    }

}
