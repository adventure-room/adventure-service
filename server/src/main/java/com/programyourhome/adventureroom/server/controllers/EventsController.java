package com.programyourhome.adventureroom.server.controllers;

import javax.inject.Inject;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.programyourhome.adventureroom.model.event.TriggerEvent;
import com.programyourhome.adventureroom.server.events.EventManager;

@RestController
@RequestMapping("events")
public class EventsController {

    @Inject
    private EventManager eventManager;

    @RequestMapping("trigger/{triggerId}")
    public void triggerEvent(@PathVariable("triggerId") final String triggerId) {
        this.eventManager.fireEvent(new TriggerEvent(triggerId));
    }

}