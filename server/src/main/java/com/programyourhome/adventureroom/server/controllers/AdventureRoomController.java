package com.programyourhome.adventureroom.server.controllers;

import java.util.Set;

import javax.inject.Inject;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.programyourhome.adventureroom.model.Adventure;
import com.programyourhome.adventureroom.model.Describable;
import com.programyourhome.adventureroom.model.Room;
import com.programyourhome.adventureroom.model.script.Script;
import com.programyourhome.adventureroom.server.repository.AdventureRoomRepository;
import com.programyourhome.adventureroom.server.service.AdventureService;

import one.util.streamex.StreamEx;

@RestController
@RequestMapping("rooms")
public class AdventureRoomController {

    @Inject
    private AdventureRoomRepository repository;

    @Inject
    private AdventureService adventureService;

    @Inject
    private ObjectConverter objectConverter;

    @RequestMapping("")
    public Set<Describable> listRooms() {
        return StreamEx.of(this.repository.getRooms())
                .map(room -> this.objectConverter.convert(room, Describable.class))
                .toSet();
    }

    @RequestMapping("{roomId}")
    public Describable getRoom(@PathVariable("roomId") final String roomId) {
        Room room = this.repository.getRoom(roomId);
        return this.objectConverter.convert(room, Describable.class);
    }

    @RequestMapping("{roomId}/reload")
    public void reloadRoom(@PathVariable("roomId") final String roomId) {
        if (!this.repository.hasRoom(roomId)) {
            throw new IllegalArgumentException("Room [" + roomId + "] does not exist");
        }
        this.repository.reloadRoom(roomId);
    }

    @RequestMapping("{roomId}/adventures")
    public Set<Describable> getRoomAdventures(@PathVariable("roomId") final String roomId) {
        return StreamEx.of(this.repository.getAdventures(roomId))
                .map(adventure -> this.objectConverter.convert(adventure, Describable.class))
                .toSet();
    }

    @RequestMapping("{roomId}/adventures/{adventureId}")
    public Describable getRoomAdventure(@PathVariable("roomId") final String roomId, @PathVariable("adventureId") final String adventureId) {
        Adventure adventure = this.repository.getAdventure(roomId, adventureId);
        return this.objectConverter.convert(adventure, Describable.class);
    }

    @RequestMapping("{roomId}/adventures/{adventureId}/start")
    public void startRoomAdventure(@PathVariable("roomId") final String roomId, @PathVariable("adventureId") final String adventureId) {
        Adventure adventure = this.repository.getAdventure(roomId, adventureId);
        this.adventureService.startAdventure(adventure);
    }

    @RequestMapping("{roomId}/adventures/{adventureId}/stop")
    public void stopRoomAdventure(@PathVariable("roomId") final String roomId, @PathVariable("adventureId") final String adventureId) {
        if (!this.adventureService.hasActiveAdventure()) {
            throw new IllegalStateException("No active adventure");
        }
        Adventure adventure = this.repository.getAdventure(roomId, adventureId);
        String activeAdventureId = this.adventureService.getActiveAdventure().adventure.id;
        if (!activeAdventureId.equals(adventure.id)) {
            throw new IllegalStateException("That adventure is not active, but " + activeAdventureId + " is");
        }
        this.adventureService.stopAdventure();
    }

    @RequestMapping("{roomId}/adventures/{adventureId}/scripts")
    public Set<Describable> getRoomAdventureScripts(@PathVariable("roomId") final String roomId, @PathVariable("adventureId") final String adventureId) {
        return StreamEx.of(this.repository.getAdventure(roomId, adventureId).getScripts())
                .map(script -> this.objectConverter.convert(script, Describable.class))
                .toSet();
    }

    @RequestMapping("{roomId}/adventures/{adventureId}/scripts/{scriptId}")
    public Describable getRoomAdventureScript(@PathVariable("roomId") final String roomId, @PathVariable("adventureId") final String adventureId,
            @PathVariable("scriptId") final String scriptId) {
        Script script = this.repository.getAdventure(roomId, adventureId).getScript(scriptId);
        return this.objectConverter.convert(script, Describable.class);
    }

    /**
     * Just run the specified script. This is mainly meant for testing purposes, cause normally you would start
     * an adventure and the scripts will be ran based on configured triggers.
     */
    @RequestMapping("{roomId}/adventures/{adventureId}/scripts/{scriptId}/run")
    public void runRoomAdventureScript(@PathVariable("roomId") final String roomId, @PathVariable("adventureId") final String adventureId,
            @PathVariable("scriptId") final String scriptId) {
        Adventure adventure = this.repository.getAdventure(roomId, adventureId);
        Script script = adventure.getScript(scriptId);
        this.adventureService.runScript(adventure, script);
    }

}
