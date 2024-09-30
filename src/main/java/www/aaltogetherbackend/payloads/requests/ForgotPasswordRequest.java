package www.aaltogetherbackend.payloads.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ForgotPasswordRequest(
        @NotBlank(message = "Email should not be blank")
        @Size(max = 50, message = "Email should be of maximum 50 characters")
        @Email
        String email
) {}
