package www.aaltogetherbackend.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

@Entity
@Table(name = "files")
public class File {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @NotBlank(message = "Name cannot be empty.")
    private String name;
    private String type;

    @ManyToOne
    @JoinColumn(name = "uploader_id")
    private User uploader;

    @Lob
    @NotEmpty(message = "File cannot be empty.")
    private byte[] data;

    Long duration;

    public File() {
    }

    public File(String name, String type, User uploader, byte[] data, Long duration) {
        this.name = name;
        this.type = type;
        this.uploader = uploader;
        this.data = data;
        this.duration = duration;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public @NotBlank(message = "Name cannot be empty.") String getName() {
        return name;
    }

    public void setName(@NotBlank(message = "Name cannot be empty.") String name) {
        this.name = name;
    }

    public User getUploader() {
        return uploader;
    }

    @NotEmpty(message = "File cannot be empty.")
    public byte[] getData() {
        return data;
    }

    public void setData(@NotEmpty(message = "File cannot be empty.") byte[] data) {
        this.data = data;
    }

}
