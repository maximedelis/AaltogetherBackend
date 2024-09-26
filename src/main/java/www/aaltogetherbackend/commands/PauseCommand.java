package www.aaltogetherbackend.commands;

import com.corundumstudio.socketio.SocketIOClient;
import org.springframework.stereotype.Service;
import www.aaltogetherbackend.services.SocketService;

import java.util.UUID;

@Service(CommandType.PAUSE_BEAN_NAME)
public class PauseCommand implements Command {

    private final SocketService service;

    public PauseCommand(SocketService service) {
        this.service = service;
    }

    @Override
    public void execute(UUID room, String value, SocketIOClient senderClient) {
        service.sendCommand(room, senderClient, CommandType.PAUSE, "");
    }
}
