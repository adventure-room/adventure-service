package com.programyourhome.adventureroom.server.events;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

import com.programyourhome.adventureroom.model.Adventure;
import com.programyourhome.adventureroom.model.event.Event;
import com.programyourhome.adventureroom.server.service.AdventureService;

import one.util.streamex.EntryStream;

//TODO: better name
@Component
public class EventManager {

    @Inject
    private AdventureService adventureService;

    private Adventure adventure;

    public void resetForAdventure(Adventure adventure) {
        this.adventure = adventure;
    }

    public void fireEvent(Event event) {
        // TODO: handle in new thread?!?
        this.adventure.getModules().forEach(module -> module.handleEvent(event));

        EntryStream.of(this.adventure.triggers)
                .filter(entry -> event.paramEquals(entry.getKey()))
                .forEach(entry -> this.adventureService.runScript(entry.getValue()));
    }

}