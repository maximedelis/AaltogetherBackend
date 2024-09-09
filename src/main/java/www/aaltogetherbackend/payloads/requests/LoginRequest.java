package www.aaltogetherbackend.payloads.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank(message = "Username cannot be blank")
        @Size(min = 4, max = 20) String username,

        @NotBlank(message = "Password cannot be blank")
        @Size(min = 8, max = 40) String password
) {}