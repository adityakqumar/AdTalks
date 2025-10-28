package com.ad.adchat.service;

import com.ad.adchat.model.Room;
import com.ad.adchat.repository.RoomRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoomService {

    private final RoomRepository roomRepository;

    public RoomService(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    public List<Room> findAll() {
        return roomRepository.findAll();
    }

    public Room createIfNotExists(String name) {
        return roomRepository.findByName(name)
                .orElseGet(() -> roomRepository.save(new Room(name)));
    }
}
