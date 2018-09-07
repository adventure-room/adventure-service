package com.programyourhome.adventureroom.model.execution;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.programyourhome.adventureroom.model.Adventure;
import com.programyourhome.adventureroom.model.module.AdventureModule;
import com.programyourhome.adventureroom.model.resource.Resource;
import com.programyourhome.adventureroom.model.toolbox.Toolbox;

@SuppressWarnings("unchecked")
public class ExecutionContext {

    private final Adventure adventure;

    private final Map<String, Object> variables;
    private final Map<String, Object> properties;

    public ExecutionContext(Adventure adventure) {
        this.adventure = adventure;
        this.variables = new HashMap<>();
        this.properties = new HashMap<>();
    }

    public Adventure getAdventure() {
        return this.adventure;
    }

    public Toolbox getToolbox() {
        return this.adventure.getToolbox();
    }

    public <R extends Resource> R getResource(Class<R> clazz, String id) {
        return this.adventure.getResource(clazz, id);
    }

    public <R extends Resource> Collection<R> getResources(Class<R> clazz) {
        return this.adventure.getResources(clazz);
    }

    public <M extends AdventureModule> M getModule(String id) {
        return (M) this.adventure.getModule(id);
    }

    public boolean isVariableDefined(String name) {
        return this.variables.containsKey(name);
    }

    public <T> T getVariableValue(String name) {
        return (T) this.variables.get(name);
    }

    public void setVariableValue(String name, Object value) {
        this.variables.put(name, value);
    }

    public void removeVariable(String name) {
        this.variables.remove(name);
    }

    public boolean isPropertyDefined(String name) {
        return this.properties.containsKey(name);
    }

    public <T> T getPropertyValue(String name) {
        return (T) this.properties.get(name);
    }

    public void setPropertyValue(String name, Object value) {
        this.properties.put(name, value);
    }

}
