package com.programyourhome.adventureroom.model.service;

import java.util.UUID;

import com.programyourhome.adventureroom.model.Adventure;
import com.programyourhome.adventureroom.model.script.Script;

public interface AdventureService {

    public void startAdventure(Adventure adventure);

    public void stopAdventure();

    public UUID startScript(Adventure adventure, Script script);

    public void stopScript(UUID scriptId);

}
