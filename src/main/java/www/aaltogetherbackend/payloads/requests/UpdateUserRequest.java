package www.aaltogetherbackend.payloads.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateUserRequest(
        @NotBlank @Size(min = 4, max = 16, message = "Username should be of 4 to 16 characters")
        String username,
        @Email @NotBlank @Size(max = 50, message = "Email should be of 50 characters max")
        String email
) {}
