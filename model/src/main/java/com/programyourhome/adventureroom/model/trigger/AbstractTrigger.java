package com.programyourhome.adventureroom.model.trigger;

import com.programyourhome.adventureroom.model.AbstractDescribable;
import com.programyourhome.adventureroom.model.event.Event;

public class AbstractTrigger extends AbstractDescribable implements Trigger {

    private final TriggerType type;
    private final Event event;

    public AbstractTrigger(TriggerType type, Event event) {
        this.type = type;
        this.event = event;
    }

    @Override
    public TriggerType getType() {
        return this.type;
    }

    @Override
    public Event getEvent() {
        return this.event;
    }

}
