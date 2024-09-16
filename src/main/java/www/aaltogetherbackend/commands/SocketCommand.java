package www.aaltogetherbackend.commands;

public record SocketCommand(
        String command,
        String value
) {}