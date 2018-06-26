package com.programyourhome.adventureroom.model.module;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import com.programyourhome.adventureroom.model.AbstractDescribable;
import com.programyourhome.adventureroom.model.character.Character;
import com.programyourhome.adventureroom.model.character.CharacterDescriptor;
import com.programyourhome.adventureroom.model.event.Event;
import com.programyourhome.adventureroom.model.event.EventDescriptor;
import com.programyourhome.adventureroom.model.resource.Resource;
import com.programyourhome.adventureroom.model.resource.ResourceDescriptor;
import com.programyourhome.adventureroom.model.script.action.Action;
import com.programyourhome.adventureroom.model.script.action.ActionDescriptor;

public class ModuleConfig extends AbstractDescribable {

    private final Map<String, ActionDescriptor<? extends Action>> actionDescriptors;
    private final Map<String, CharacterDescriptor<? extends Character>> characterDescriptors;
    private final Map<String, ResourceDescriptor<? extends Resource>> resourceDescriptors;
    private final Map<String, EventDescriptor<? extends Event>> eventDescriptors;
    private final Set<Converter<?, ?>> converters;
    private final Map<String, Task> tasks;

    public ModuleConfig() {
        this.actionDescriptors = new HashMap<>();
        this.characterDescriptors = new HashMap<>();
        this.resourceDescriptors = new HashMap<>();
        this.eventDescriptors = new HashMap<>();
        this.converters = new HashSet<>();
        this.tasks = new HashMap<>();
    }

    public Map<String, ActionDescriptor<? extends Action>> getActionDescriptorMap() {
        return this.actionDescriptors;
    }

    public Collection<ActionDescriptor<? extends Action>> getActionDescriptors() {
        return this.actionDescriptors.values();
    }

    public ActionDescriptor<? extends Action> getActionDescriptor(String id) {
        return this.actionDescriptors.get(id);
    }

    public Map<String, CharacterDescriptor<? extends Character>> getCharacterDescriptorMap() {
        return this.characterDescriptors;
    }

    public Collection<CharacterDescriptor<? extends Character>> getCharacterDescriptors() {
        return this.characterDescriptors.values();
    }

    public CharacterDescriptor<? extends Character> getCharacterDescriptor(String id) {
        return this.characterDescriptors.get(id);
    }

    public Map<String, ResourceDescriptor<? extends Resource>> getResourceDescriptorMap() {
        return this.resourceDescriptors;
    }

    public Collection<ResourceDescriptor<? extends Resource>> getResourceDescriptors() {
        return this.resourceDescriptors.values();
    }

    public ResourceDescriptor<? extends Resource> getResourceDescriptor(String id) {
        return this.resourceDescriptors.get(id);
    }

    public Map<String, EventDescriptor<? extends Event>> getEventDescriptorMap() {
        return this.eventDescriptors;
    }

    public Collection<EventDescriptor<? extends Event>> getEventDescriptors() {
        return this.eventDescriptors.values();
    }

    public EventDescriptor<? extends Event> getEventDescriptor(String id) {
        return this.eventDescriptors.get(id);
    }

    public Set<Converter<?, ?>> getConverters() {
        return this.converters;
    }

    public Map<String, Task> getTasks() {
        return this.tasks;
    }

    public void addActionDescriptor(ActionDescriptor<? extends Action> actionDescriptor) {
        this.actionDescriptors.put(actionDescriptor.getId(), actionDescriptor);
    }

    public void addCharacterDescriptor(CharacterDescriptor<? extends Character> characterDescriptor) {
        this.characterDescriptors.put(characterDescriptor.getId(), characterDescriptor);
    }

    public void addResourceDescriptor(ResourceDescriptor<? extends Resource> resourceDescriptor) {
        this.resourceDescriptors.put(resourceDescriptor.getId(), resourceDescriptor);
    }

    public void addEventDescriptor(EventDescriptor<? extends Event> eventDescriptor) {
        this.eventDescriptors.put(eventDescriptor.getId(), eventDescriptor);
    }

    public <From, To> void addConverter(Class<From> fromClass, Class<To> toClass, Function<From, To> converter) {
        this.converters.add(new Converter<From, To>() {
            @Override
            public Class<From> getFromClass() {
                return fromClass;
            }

            @Override
            public Class<To> getToClass() {
                return toClass;
            }

            @Override
            public To convert(From from) {
                return converter.apply(from);
            }
        });
    }

    public void addTask(String name, Runnable runnable) {
        this.tasks.put(name, () -> runnable.run());
    }

}
