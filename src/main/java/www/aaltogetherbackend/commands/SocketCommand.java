package www.aaltogetherbackend.commands;

public record SocketCommand(
        CommandType command,
        String value
) {}