package www.aaltogetherbackend.commands;

import com.corundumstudio.socketio.SocketIOClient;
import org.springframework.stereotype.Service;
import www.aaltogetherbackend.services.SocketService;

import java.util.UUID;

@Service(CommandType.OPEN_BEAN_NAME)
public class OpenCommand implements Command {

    private final SocketService service;

    public OpenCommand(SocketService service) {
        this.service = service;
    }

    @Override
    public void execute(UUID room, String value, SocketIOClient senderClient) {
        service.sendCommand(room, senderClient, CommandType.OPEN, value);
    }
}