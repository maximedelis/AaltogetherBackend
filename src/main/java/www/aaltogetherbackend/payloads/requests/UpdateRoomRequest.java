package www.aaltogetherbackend.payloads.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public record UpdateRoomRequest(
        UUID id,

        @NotBlank(message = "Room name cannot be blank")
        String name,

        String password,

        @JsonProperty("private")
        boolean aprivate
) {}