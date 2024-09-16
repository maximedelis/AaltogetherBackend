package www.aaltogetherbackend.commands;

public record CommandResponse(
        String command,
        String value
) {}