package www.aaltogetherbackend.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "rooms", uniqueConstraints =
        @UniqueConstraint(columnNames = "code")
)
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String name;

    private boolean aprivate;
    private boolean isFileSharingEnabled;
    private boolean isChatEnabled;
    private boolean areCommandsEnabled;

    @Column(nullable = true, unique = true)
    private String code;

    @Min(value = 2, message = "Room should have 2 users minimum")
    @Max(value = 16, message = "Room should have 16 users maximum")
    private int maxUsers;

    @ManyToOne
    @JoinColumn(name = "host_id")
    private User host;

    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
    @JoinTable(name = "shared_files", joinColumns = @JoinColumn(name = "room_id"), inverseJoinColumns = @JoinColumn(name = "file_id"))
    private Set<File> sharedFiles = new HashSet<>();

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    public void addSharedFile(File file) {
        this.sharedFiles.add(file);
    }

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

    public String getCode() {
        return code;
    }

    public void setCode(String password) {
        this.code = password;
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

    public boolean isFileSharingEnabled() {
        return isFileSharingEnabled;
    }

    public void setFileSharingEnabled(boolean fileSharingEnabled) {
        isFileSharingEnabled = fileSharingEnabled;
    }

    public boolean isChatEnabled() {
        return isChatEnabled;
    }

    public void setChatEnabled(boolean chatEnabled) {
        isChatEnabled = chatEnabled;
    }

    public boolean areCommandsEnabled() {
        return areCommandsEnabled;
    }

    public void setAreCommandsEnabled(boolean areCommandsEnabled) {
        this.areCommandsEnabled = areCommandsEnabled;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
