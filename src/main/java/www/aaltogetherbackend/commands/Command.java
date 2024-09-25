package www.aaltogetherbackend.commands;

import com.corundumstudio.socketio.SocketIOClient;

import java.util.UUID;

public interface Command {
    void execute(UUID room, String value, SocketIOClient senderClient);
}
