package www.aaltogetherbackend.payloads.requests;

import jakarta.validation.constraints.NotBlank;

public record JoinRoomRequest(
        @NotBlank(message = "Room name cannot be blank") String name,
        String password
) {}