package www.aaltogetherbackend.payloads.requests;

import com.fasterxml.jackson.annotation.JsonProperty;

public record UpdatePasswordRequest(
        @JsonProperty("oldPassword")
        String oldPassword,
        @JsonProperty("newPassword")
        String newPassword
) {}
