package www.aaltogetherbackend.payloads.requests;

import java.util.UUID;

public record PlayRequest(
        UUID roomId,
        long fileId
) {
}
