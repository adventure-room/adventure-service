package com.programyourhome.adventureroom.model.event;

//TODO: Complete trigger refactoring - Trigger as first class citizen
public class TriggerEvent extends Event {

    private final String triggerId;

    public TriggerEvent(String triggerId) {
        super("Trigger [" + triggerId + "] fired.");
        this.triggerId = triggerId;
    }

    public String getTriggerId() {
        return this.triggerId;
    }

}
