package www.aaltogetherbackend.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.util.UUID;

@Entity
@Table(name = "rooms")
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String name;

    private boolean aprivate;

    private String password;

    @Min(value = 2, message = "Room should have 2 users minimum")
    @Max(value = 16, message = "Room should have 16 users maximum")
    private int maxUsers;

    @ManyToOne
    @JoinColumn(name = "host_id")
    private User host;

    // TODO: Files available in the room

    public Room() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isAprivate() {
        return aprivate;
    }

    public void setAprivate(boolean isprivate) {
        this.aprivate = isprivate;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public User getHost() {
        return host;
    }

    public void setHost(User host) {
        this.host = host;
    }

    public int getMaxUsers() {
        return maxUsers;
    }

    public void setMaxUsers(int maxUsers) {
        this.maxUsers = maxUsers;
    }
}
