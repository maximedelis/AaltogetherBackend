package www.aaltogetherbackend.payloads.responses;

import com.fasterxml.jackson.annotation.JsonProperty;

public record LoginResponse(
        String message,
        String jwt,
        @JsonProperty("refreshToken")
        String refreshToken
) {}