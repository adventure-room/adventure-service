package com.programyourhome.iotadventure.model.script.action.speech;

import com.programyourhome.iotadventure.model.Character;
import com.programyourhome.iotadventure.model.script.action.Action;

public class TextToSpeechAction implements Action {

    public Character character;
    public SpeechType speechType;
    public String speech;

}
