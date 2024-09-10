package www.aaltogetherbackend.models;

import jakarta.persistence.*;

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

}
