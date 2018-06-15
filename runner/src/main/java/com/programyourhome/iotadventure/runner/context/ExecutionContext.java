package com.programyourhome.iotadventure.runner.context;

import java.util.Map;

import com.programyourhome.adventureroom.model.Adventure;
import com.programyourhome.adventureroom.model.module.AdventureModule;

public class ExecutionContext {

    private final Adventure adventure;

    private Map<String, Object> variables;

    public ExecutionContext(Adventure adventure) {
        this.adventure = adventure;
    }

    @SuppressWarnings("unchecked")
    public <M extends AdventureModule> M getModule(String id) {
        return (M) this.adventure.getModule(id);
    }

    @SuppressWarnings("unchecked")
    public <T> T getVariableValue(String name) {
        return (T) this.variables.get(name);
    }

    public void setVariableValue(String name, Object value) {
        this.variables.put(name, value);
    }

    public void removeVariable(String name) {
        this.variables.remove(name);
    }

}
