package www.aaltogetherbackend.payloads.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class SignupRequest {
    @NotBlank(message = "Username should not be blank")
    @Size(min = 4, max = 20, message = "Username should be of 4 to 20 characters")
    private String username;

    @NotBlank(message = "Email should not be blank")
    @Size(max = 50, message = "Email should be of maximum 50 characters")
    @Email
    private String email;

    @NotBlank(message = "Password should not be blank")
    @Size(min = 8, max = 40, message = "Password should be of 8 to 40 characters")
    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}
