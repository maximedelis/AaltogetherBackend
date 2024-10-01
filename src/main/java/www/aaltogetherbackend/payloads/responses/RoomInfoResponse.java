package www.aaltogetherbackend.payloads.responses;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record RoomInfoResponse(
        UUID id,
        String name,
        String code,
        @JsonProperty("private")
        boolean aprivate,
        @JsonProperty("fileSharingEnabled")
        boolean isFileSharingEnabled,
        @JsonProperty("chatEnabled")
        boolean isChatEnabled,
        @JsonProperty("commandsEnabled")
        boolean areCommandsEnabled,
        @JsonProperty("maxUsers")
        int maxUsers,
        String host,
        String createdAt,
        Set<String> connectedUsers,
        Set<FileNoDataInterface> sharedFiles
) {}
