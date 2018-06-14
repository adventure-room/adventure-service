package com.programyourhome.adventureroom.model.module;

import java.util.Optional;

import com.programyourhome.adventureroom.model.script.action.Action;

public interface ActionParser {

    public Optional<Action> parseForAction(String input);

}
