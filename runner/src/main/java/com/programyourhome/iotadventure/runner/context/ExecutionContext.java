package com.programyourhome.iotadventure.runner.context;

import java.util.Map;

import com.programyourhome.adventureroom.model.module.AdventureModule;

public class ExecutionContext {

    public Map<String, AdventureModule> modules;

    public Map<String, Object> variables;

    @SuppressWarnings("unchecked")
    public <M extends AdventureModule> M getModule(String id) {
        return (M) this.modules.get(id);
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
