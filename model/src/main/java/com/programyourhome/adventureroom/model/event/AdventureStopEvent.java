package com.programyourhome.adventureroom.model.event;

public class AdventureStopEvent extends Event {

    private final String adventureId;

    public AdventureStopEvent(String adventureId) {
        super("Adventure [" + adventureId + "] will be stopped.");
        this.adventureId = adventureId;
    }

    public String getAdventureId() {
        return this.adventureId;
    }

}
