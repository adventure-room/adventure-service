package com.programyourhome.adventureroom.model.trigger;

import com.programyourhome.adventureroom.model.event.AdventureStartedEvent;
import com.programyourhome.adventureroom.model.script.Script;

public class StartScriptTrigger extends AbstractTrigger {

    private final Script script;

    public StartScriptTrigger(AdventureStartedEvent event, Script script) {
        super(TriggerType.START_SCRIPT, event);
        this.script = script;
    }

    public Script getScript() {
        return this.script;
    }

}
