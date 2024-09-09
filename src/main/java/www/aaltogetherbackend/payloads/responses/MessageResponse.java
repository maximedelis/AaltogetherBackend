package www.aaltogetherbackend.payloads.responses;

public record MessageResponse(String message, String jwt) {
    public MessageResponse(String message) {
        this(message, null);
    }
}