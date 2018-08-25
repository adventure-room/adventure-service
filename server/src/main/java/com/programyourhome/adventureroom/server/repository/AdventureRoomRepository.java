package com.programyourhome.adventureroom.server.repository;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.programyourhome.adventureroom.model.Adventure;
import com.programyourhome.adventureroom.model.Room;
import com.programyourhome.adventureroom.server.loader.AdventureRoomLoader;

@Component
public class AdventureRoomRepository {

    @Value("${rooms.basepath}")
    private String roomsBasepath;

    @Inject
    private AdventureRoomLoader loader;

    private Map<String, Room> rooms;

    @PostConstruct
    public void init() {
        this.reload();
    }

    public void reload() {
        this.rooms = this.loader.loadRooms(this.roomsBasepath);
    }

    public Map<String, Room> getRoomsMap() {
        return this.rooms;
    }

    public Set<String> getRoomIds() {
        return this.rooms.keySet();
    }

    public Collection<Room> getRooms() {
        return this.rooms.values();
    }

    public boolean hasRoom(String roomId) {
        return this.rooms.containsKey(roomId);
    }

    public Room getRoom(String roomId) {
        return this.rooms.get(roomId);
    }

    public Map<String, Adventure> getAdventuresMap(String roomId) {
        return this.rooms.get(roomId).adventures;
    }

    public Set<String> getAdventureIds(String roomId) {
        return this.rooms.get(roomId).adventures.keySet();
    }

    public Collection<Adventure> getAdventures(String roomId) {
        return this.rooms.get(roomId).adventures.values();
    }

    public boolean hasAdventure(String roomId, String adventureId) {
        return this.rooms.get(roomId).adventures.containsKey(adventureId);
    }

    public Adventure getAdventure(String roomId, String adventureId) {
        return this.rooms.get(roomId).adventures.get(adventureId);
    }

}
