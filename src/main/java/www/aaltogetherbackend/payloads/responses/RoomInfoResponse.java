package www.aaltogetherbackend.payloads.responses;

import com.fasterxml.jackson.annotation.JsonProperty;

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
        @JsonProperty("maxUsers")
        int maxUsers,
        String host,
        Set<UsernameInfoResponse> connectedUsers,
        Set<FileNoDataInterface> sharedFiles
) {}
