package www.aaltogetherbackend.payloads.responses;

public record LoginResponse(
        String message,
        String jwt
) {}