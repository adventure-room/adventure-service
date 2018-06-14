package com.programyourhome.adventureroom.model.script;

import java.util.ArrayList;
import java.util.List;

import com.programyourhome.adventureroom.model.AbstractDescribable;
import com.programyourhome.adventureroom.model.script.action.Action;

public class Script extends AbstractDescribable {

    public List<String> requiredModules = new ArrayList<>();

    public List<Action> actions = new ArrayList<>();

}
