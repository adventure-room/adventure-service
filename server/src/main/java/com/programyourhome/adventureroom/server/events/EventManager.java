package com.programyourhome.adventureroom.server.events;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

import com.programyourhome.adventureroom.model.Adventure;
import com.programyourhome.adventureroom.model.event.Event;
import com.programyourhome.adventureroom.server.service.AdventureService;

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
        this.adventure.getTriggeredScripts(event).forEach(script -> this.adventureService.runScript(this.adventure, script));
    }

}