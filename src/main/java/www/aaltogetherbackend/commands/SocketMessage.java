package www.aaltogetherbackend.commands;

public record SocketMessage(
        String command,
        String value
) {}
