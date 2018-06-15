package com.programyourhome.adventureroom.model.event;

public class AdventureStartedEvent extends Event {

    private final String adventureId;

    public AdventureStartedEvent(String adventureId) {
        super("Adventure [" + adventureId + "] started.");
        this.adventureId = adventureId;
    }

    public String getAdventureId() {
        return this.adventureId;
    }

}
