package www.aaltogetherbackend.payloads.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public record CreateRoomRequest(
        @NotBlank(message = "Room name cannot be blank") String name,
        String password,
        @JsonProperty("private")
        boolean aprivate
) {}
