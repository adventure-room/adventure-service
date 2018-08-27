package com.programyourhome.adventureroom.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.programyourhome.adventureroom.model.character.Character;
import com.programyourhome.adventureroom.model.event.Event;
import com.programyourhome.adventureroom.model.module.AdventureModule;
import com.programyourhome.adventureroom.model.resource.ExternalResource;
import com.programyourhome.adventureroom.model.resource.Resource;
import com.programyourhome.adventureroom.model.script.Script;

import one.util.streamex.EntryStream;
import one.util.streamex.StreamEx;

@SuppressWarnings("unchecked")
public class Adventure extends AbstractDescribable {

    private Set<String> requiredModules;

    private final Map<String, AdventureModule> modules;
    private final Map<String, Character> characters;
    private final Map<Class<? extends Resource>, Map<String, Resource>> resources;
    private final Map<String, Script> scripts;
    private final Map<Event, Set<Script>> triggers;

    public Adventure() {
        this.modules = new HashMap<>();
        this.characters = new HashMap<>();
        this.resources = new HashMap<>();
        this.scripts = new HashMap<>();
        this.triggers = new HashMap<>();
    }

    public Set<String> getRequiredModules() {
        return this.requiredModules;
    }

    public Map<String, AdventureModule> getModuleMap() {
        return this.modules;
    }

    public Collection<AdventureModule> getModules() {
        return this.modules.values();
    }

    public <M extends AdventureModule> M getModule(String id) {
        return (M) this.modules.get(id);
    }

    public Map<String, Character> getCharacterMap() {
        return this.characters;
    }

    public Collection<Character> getCharacters() {
        return this.characters.values();
    }

    public <C extends Character> C getCharacter(String id) {
        return (C) this.characters.get(id);
    }

    public Map<Class<? extends Resource>, Map<String, Resource>> getResourceMaps() {
        return this.resources;
    }

    public <R extends Resource> Map<String, R> getResourceMap(Class<R> clazz) {
        return (Map<String, R>) this.resources.getOrDefault(clazz, new HashMap<>());
    }

    public <R extends Resource> Collection<R> getResources(Class<R> clazz) {
        return this.getResourceMap(clazz).values();
    }

    public <R extends Resource> R getResource(Class<R> clazz, String id) {
        return this.getResourceMap(clazz).get(id);
    }

    public <O, R extends ExternalResource<O>> Map<String, O> getExternalResourceMap(Class<R> clazz) {
        return EntryStream.of(this.getResourceMap(clazz))
                .mapValues(ExternalResource::getWrappedObject)
                .toMap();
    }

    public <O, R extends ExternalResource<O>> Collection<O> getExternalResources(Class<R> clazz) {
        return StreamEx.of(this.getResources(clazz))
                .map(ExternalResource::getWrappedObject)
                .toSet();
    }

    public <O, R extends ExternalResource<O>> O getExternalResource(Class<R> clazz, String id) {
        return this.getResource(clazz, id).getWrappedObject();
    }

    public Map<String, Script> getScriptMap() {
        return this.scripts;
    }

    public Collection<Script> getScripts() {
        return this.scripts.values();
    }

    public Script getScript(String id) {
        return this.scripts.get(id);
    }

    public Map<Event, Set<Script>> getTriggerMap() {
        return this.triggers;
    }

    public Set<Script> getTriggeredScripts(Event event) {
        return this.triggers.getOrDefault(event, new HashSet<>());
    }

    public void addModule(AdventureModule module) {
        this.modules.put(module.getConfig().getId(), module);
    }

    public void addCharacter(Character character) {
        this.characters.put(character.getId(), character);
    }

    public void addResource(Resource resource) {
        this.resources.computeIfAbsent(resource.getClass(), clazz -> new HashMap<>()).put(resource.getId(), resource);
    }

    public void addScript(Script script) {
        this.scripts.put(script.getId(), script);
    }

    public void addTrigger(Event event, Script script) {
        this.triggers.computeIfAbsent(event, e -> new HashSet<>()).add(script);
    }

}
