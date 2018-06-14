package com.programyourhome.iotadventure.model.script.action.sound;

import com.programyourhome.iotadventure.model.Sound;
import com.programyourhome.iotadventure.model.script.action.Action;

// Keep simple for now: just stop latest started sound with this name, if any still going (needs Immerse feature btw)
public class StopSoundAction implements Action {

    public Sound sound;

}
