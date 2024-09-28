package www.aaltogetherbackend.payloads.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record UpdateRoomRequest(
        UUID id,

        @NotBlank(message = "Room name cannot be blank")
        String name,

        @JsonProperty("private")
        boolean aprivate,

        @JsonProperty("fileSharingEnabled")
        boolean isFileSharingEnabled,

        @Min(value = 2, message = "Room should have 2 users minimum")
        @Max(value = 16, message = "Room should have 16 users maximum")
        @JsonProperty("maxUsers")
        int maxUsers
) {}