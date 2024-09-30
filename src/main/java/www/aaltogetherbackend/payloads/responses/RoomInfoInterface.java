package www.aaltogetherbackend.payloads.responses;

import java.time.Instant;
import java.util.UUID;

public interface RoomInfoInterface {
    UUID getId();
    String getName();
    String getCode();
    boolean isPrivate();
    boolean isFileSharingEnabled();
    boolean isChatEnabled();
    boolean areCommandsEnabled();
    int getMaxUsers();
    String getHost();
    Instant getCreatedAt();
}
