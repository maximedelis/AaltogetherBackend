package www.aaltogetherbackend.payloads.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record UpdateRoomRequest(
        UUID id,

        @NotBlank(message = "Room name cannot be blank")
        String name,

        @NotNull(message = "Room privacy cannot be null")
        @JsonProperty("private")
        Boolean aprivate,

        @NotNull(message = "File sharing cannot be null")
        @JsonProperty("fileSharingEnabled")
        Boolean isFileSharingEnabled,

        @NotNull(message = "Chat cannot be null")
        @JsonProperty("chatEnabled")
        Boolean isChatEnabled,

        @NotNull(message = "Commands cannot be null")
        @JsonProperty("commandsEnabled")
        Boolean areCommandsEnabled,

        @Min(value = 2, message = "Room should have 2 users minimum")
        @Max(value = 16, message = "Room should have 16 users maximum")
        @JsonProperty("maxUsers")
        int maxUsers
) {}