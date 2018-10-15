package com.programyourhome.adventureroom.model.trigger;

import com.programyourhome.adventureroom.model.event.AdventureStartedEvent;

//TODO: Full Trigger refactoring
public class StartScriptTrigger extends AbstractTrigger {

    public String scriptId;

    public StartScriptTrigger(AdventureStartedEvent event) {
        super(TriggerType.START_SCRIPT, event);
    }

    public String getScriptId() {
        return this.scriptId;
    }

}
