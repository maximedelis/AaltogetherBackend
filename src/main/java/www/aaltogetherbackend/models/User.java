package www.aaltogetherbackend.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.UUID;

@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(columnNames = "username"),
        @UniqueConstraint(columnNames = "email")
})
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank(message = "Username should not be blank")
    @Size(min = 4, max = 16, message = "Username should be of 4 to 16 characters")
    @Column(unique = true)
    private String username;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @NotBlank(message = "Password should not be blank")
    private String password;

    @NotBlank
    @Size(max = 50, message = "Email should be of 50 characters max")
    @Email
    private String email;

    @JsonIgnore
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    public boolean isEnabled;

    @Override
    public boolean isEnabled() {
        return isEnabled;
    }

    private boolean isEmailVerified;

    public boolean isEmailVerified() {
        return isEmailVerified;
    }

    public void setEmailVerified(boolean emailVerified) {
        isEmailVerified = emailVerified;
    }

    public String getEmail() {
        return email;
    }

    public User (String username, String password, String email) {
        this.username = username;
        this.password = password;
        this.email = email;
    }

    public User() {
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public UUID getId() {
        return id;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }
}
