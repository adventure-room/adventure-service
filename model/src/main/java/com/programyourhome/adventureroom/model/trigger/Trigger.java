package com.programyourhome.adventureroom.model.trigger;

import com.programyourhome.adventureroom.model.Describable;
import com.programyourhome.adventureroom.model.event.Event;

public interface Trigger extends Describable {

    public Event getEvent();

    public TriggerType getType();

}
