package www.aaltogetherbackend.payloads.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class LoginRequest {
    @NotBlank(message = "Username cannot be blank")
    @Size(min = 4, max = 20)
    private String username;

    @NotBlank(message = "Password cannot be blank")
    @Size(min = 8, max = 40)
    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
