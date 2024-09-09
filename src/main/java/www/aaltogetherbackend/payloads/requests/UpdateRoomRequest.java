package www.aaltogetherbackend.payloads.requests;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public record UpdateRoomRequest(
        @org.hibernate.validator.constraints.UUID(message = "Room ID should be a valid UUID")
        UUID id,

        @NotBlank(message = "Room name cannot be blank")
        String name,

        String password,
        boolean aprivate
) {}