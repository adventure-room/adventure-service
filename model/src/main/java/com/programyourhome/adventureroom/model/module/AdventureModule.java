package com.programyourhome.adventureroom.model.module;

import java.util.Optional;
import java.util.ServiceLoader;

import com.programyourhome.adventureroom.model.Adventure;
import com.programyourhome.adventureroom.model.event.Event;
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

    public void stop();

    @SuppressWarnings("unchecked")
    public default <Api, Impl extends Api> Impl loadImpl(Class<Api> apiInterface) {
        ServiceLoader<Api> apiLoader = ServiceLoader.load(apiInterface);
        if (!apiLoader.iterator().hasNext()) {
            throw new IllegalStateException("No implementation found for api: " + apiInterface);
        }
        return (Impl) apiLoader.iterator().next();
    }

}
