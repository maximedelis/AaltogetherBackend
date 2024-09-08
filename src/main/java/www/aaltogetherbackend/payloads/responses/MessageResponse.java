package www.aaltogetherbackend.payloads.responses;

public class MessageResponse {
    private final String message;

    private final String jwt;

    public MessageResponse(String message, String jwt) {
        this.message = message;
        this.jwt = jwt;
    }

    public MessageResponse(String message) {
        this.message = message;
        this.jwt = null;
    }

    public String getMessage() {
        return message;
    }

    public String getJwt() {
        return jwt;
    }
}
