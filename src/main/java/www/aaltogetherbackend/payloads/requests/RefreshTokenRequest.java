package www.aaltogetherbackend.payloads.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequest(
        @NotBlank(message = "Refresh token cannot be blank")
        @JsonProperty("refreshToken")
        String refreshToken
) {
}
