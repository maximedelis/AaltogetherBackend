package www.aaltogetherbackend.payloads.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
        @NotBlank(message = "Token cannot be blank")
        @JsonProperty("token")
        String token,
        @NotBlank(message = "Password should not be blank")
        @Size(min = 8, max = 40, message = "Password should be of 8 to 40 characters")
        String password
) {
}
