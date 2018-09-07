package com.programyourhome.adventureroom.model.module;

import java.util.Iterator;
import java.util.Optional;
import java.util.ServiceLoader;

import com.programyourhome.adventureroom.model.Adventure;
import com.programyourhome.adventureroom.model.event.Event;
import com.programyourhome.adventureroom.model.execution.ExecutionContext;
import com.programyourhome.adventureroom.model.script.action.Action;
import com.programyourhome.adventureroom.model.toolbox.Toolbox;

public interface AdventureModule {

    public ModuleConfig getConfig();

    public Optional<Action> parseForAction(String input, Adventure adventure);

    public default void handleEvent(Event event) {
        // Default no-op.
    }

    public default void setToolbox(Toolbox toolbox) {
        // Default no-op.
    }

    public void start(Adventure adventure, ExecutionContext context);

    public void stop(Adventure adventure, ExecutionContext context);

    public default <Api> Api loadApiImpl(Class<Api> apiInterface) {
        ServiceLoader<Api> apiLoader = ServiceLoader.load(apiInterface);
        Iterator<Api> iter = apiLoader.iterator();
        if (!iter.hasNext()) {
            throw new IllegalStateException("No implementation found for api: " + apiInterface);
        }
        Api impl = apiLoader.iterator().next();
        if (iter.hasNext()) {
            throw new IllegalStateException("More than one implementation found for api: " + apiInterface);
        }
        return impl;
    }

}
