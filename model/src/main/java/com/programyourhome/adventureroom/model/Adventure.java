package com.programyourhome.adventureroom.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.programyourhome.adventureroom.model.character.Character;
import com.programyourhome.adventureroom.model.event.Event;
import com.programyourhome.adventureroom.model.module.AdventureModule;
import com.programyourhome.adventureroom.model.resource.Resource;
import com.programyourhome.adventureroom.model.script.Script;

public class Adventure extends AbstractDescribable {

    public Set<String> requiredModules;

    public Map<String, AdventureModule> modules = new HashMap<>();
    public Map<String, Script> scripts = new HashMap<>();
    public Map<Event, Script> triggers = new HashMap<>();
    public Map<String, Character> characters = new HashMap<>();
    public Map<Class<? extends Resource>, Map<String, Resource>> resources = new HashMap<>();

}
