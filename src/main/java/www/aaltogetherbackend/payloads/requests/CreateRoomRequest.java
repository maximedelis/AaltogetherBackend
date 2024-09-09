package www.aaltogetherbackend.payloads.requests;

import jakarta.validation.constraints.NotBlank;

public record CreateRoomRequest(
        @NotBlank(message = "Room name cannot be blank") String name,
        String password,
        boolean aprivate
) {}
