package www.aaltogetherbackend.payloads.requests;

import jakarta.validation.constraints.NotBlank;

public record UpdateFileRequest(
        @NotBlank(message = "Name cannot be empty.")
        String name
) {}
