package www.aaltogetherbackend.payloads.requests;

import jakarta.validation.constraints.Size;

public record GetRoomRequest(
        @Size(min=6, max=6, message = "Code should be 6 digit long") String code
) {}