package www.aaltogetherbackend.payloads.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignupRequest(
        @NotBlank(message = "Username should not be blank")
        @Size(min = 4, max = 20, message = "Username should be of 4 to 20 characters")
        String username,

        @NotBlank(message = "Email should not be blank")
        @Size(max = 50, message = "Email should be of maximum 50 characters")
        @Email
        String email,

        @NotBlank(message = "Password should not be blank")
        @Size(min = 8, max = 40, message = "Password should be of 8 to 40 characters")
        String password
) {}