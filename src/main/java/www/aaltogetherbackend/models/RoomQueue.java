package www.aaltogetherbackend.models;

import java.util.LinkedList;
import java.util.Queue;
import java.util.UUID;

public class RoomQueue {
    private final UUID roomId;
    private final Queue<Long> fileQueue;

    public RoomQueue(UUID roomId) {
        this.roomId = roomId;
        this.fileQueue = new LinkedList<>();
    }

    public UUID getRoomId() {
        return roomId;
    }

    public void addFileToQueue(Long fileId) {
        fileQueue.add(fileId);
    }

    public Long getNextFile() {
        return fileQueue.poll();
    }

    public Queue<Long> getFileQueue() {
        return fileQueue;
    }
}
