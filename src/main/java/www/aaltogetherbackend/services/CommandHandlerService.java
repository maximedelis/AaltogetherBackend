package www.aaltogetherbackend.services;

import com.corundumstudio.socketio.SocketIOClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import www.aaltogetherbackend.commands.Command;
import www.aaltogetherbackend.commands.CommandType;

import java.util.Map;
import java.util.UUID;

@Service
public class CommandHandlerService {
    private static final Logger log = LoggerFactory.getLogger(CommandHandlerService.class);
    private final Map<String, Command> commandHandlersMap;

    public CommandHandlerService(Map<String, Command> commandHandlersMap) {
        this.commandHandlersMap = commandHandlersMap;
    }

    public void executeCommand(CommandType commandType,
                               UUID room,
                               SocketIOClient senderClient,
                               String commandValue) {
        log.info("Selected command: {}", commandType.beanName());
        commandHandlersMap.get(commandType.beanName()).execute(room, commandValue, senderClient);
    }
}
