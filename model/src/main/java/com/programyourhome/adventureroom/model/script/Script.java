package com.programyourhome.adventureroom.model.script;

import java.util.ArrayList;
import java.util.List;

import com.programyourhome.adventureroom.model.AbstractDescribable;
import com.programyourhome.adventureroom.model.script.action.ActionData;

public class Script extends AbstractDescribable {

    public ScriptType type;

    public List<String> requiredModules = new ArrayList<>();

    public List<ActionData> actions = new ArrayList<>();

}
