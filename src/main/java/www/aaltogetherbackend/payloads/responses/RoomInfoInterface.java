package www.aaltogetherbackend.payloads.responses;

import java.util.UUID;

public interface RoomInfoInterface {
    UUID getId();
    String getName();
    String getCode();
    boolean isPrivate();
    boolean isFileSharingEnabled();
    int getMaxUsers();
    String getHost();
}
