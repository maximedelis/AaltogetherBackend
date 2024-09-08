package www.aaltogetherbackend.payloads.responses;

public class ErrorMessageResponse {
    private final String message;
    private final String field;

    public ErrorMessageResponse(String message, String field) {
        this.message = message;
        this.field = field;
    }

    public String getMessage() {
        return message;
    }

    public String getField() {
        return field;
    }
}
