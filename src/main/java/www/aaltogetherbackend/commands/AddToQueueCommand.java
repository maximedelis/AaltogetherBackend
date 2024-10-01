package www.aaltogetherbackend.commands;

import com.corundumstudio.socketio.SocketIOClient;
import org.springframework.stereotype.Service;
import www.aaltogetherbackend.services.SocketService;

import java.util.UUID;

@Service(CommandType.ADD_TO_QUEUE_BEAN_NAME)
public class AddToQueueCommand implements Command {

    private final SocketService service;

    public AddToQueueCommand(SocketService service) {
        this.service = service;
    }

    @Override
    public void execute(UUID room, String value, SocketIOClient senderClient) {
        service.addToQueue(room, senderClient, value);
    }
}