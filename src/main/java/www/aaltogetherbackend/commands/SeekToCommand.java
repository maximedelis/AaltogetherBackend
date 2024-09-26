package www.aaltogetherbackend.commands;

import com.corundumstudio.socketio.SocketIOClient;
import org.springframework.stereotype.Service;
import www.aaltogetherbackend.services.SocketService;

import java.util.UUID;

@Service(CommandType.SEEK_TO_BEAN_NAME)
public class SeekToCommand implements Command {

    private final SocketService service;

    public SeekToCommand(SocketService service) {
        this.service = service;
    }

    @Override
    public void execute(UUID room, String value, SocketIOClient senderClient) {
        service.sendCommand(room, senderClient, CommandType.SEEK_TO, value);
    }
}
