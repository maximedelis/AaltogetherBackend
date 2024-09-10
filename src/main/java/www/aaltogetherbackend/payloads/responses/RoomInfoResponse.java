package www.aaltogetherbackend.payloads.responses;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public record RoomInfoResponse(
        UUID id,
        String name,
        String password,
        @JsonProperty("private")
        boolean aprivate,
        @JsonProperty("maxUsers")
        int maxUsers
) {}
