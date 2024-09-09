package www.aaltogetherbackend.services;

import org.springframework.stereotype.Service;
import www.aaltogetherbackend.models.Room;
import www.aaltogetherbackend.repositories.RoomRepository;

import java.util.UUID;

@Service
public class RoomService {

    private final RoomRepository roomRepository;

    public RoomService(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    public boolean checkExists(String name) {
        return roomRepository.existsByName(name);
    }

    public boolean checkExistsById(UUID id) {
        return roomRepository.existsById(id);
    }

    public Room getRoom(String name) {
        return roomRepository.findByName(name);
    }

    public Room getRoom(UUID id) {
        return roomRepository.findById(id).orElse(null);
    }

    public void saveRoom(Room room) {
        roomRepository.save(room);
    }

}
