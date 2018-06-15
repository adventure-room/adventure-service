package com.programyourhome.adventureroom.model;

import java.util.Collection;
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

    public Collection<AdventureModule> getModules() {
        return this.modules.values();
    }

    public AdventureModule getModule(String id) {
        return this.modules.get(id);
    }

    @SuppressWarnings("unchecked")
    public <C extends Character> C getCharacter(String id) {
        return (C) this.characters.get(id);
    }

    @SuppressWarnings("unchecked")
    public <R extends Resource> R getResource(Class<R> clazz, String id) {
        return (R) this.resources.get(clazz).get(id);
    }

}
