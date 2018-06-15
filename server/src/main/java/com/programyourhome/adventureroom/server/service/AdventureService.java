package com.programyourhome.adventureroom.server.service;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

import com.programyourhome.adventureroom.dsl.util.ReflectionUtil;
import com.programyourhome.adventureroom.model.Adventure;
import com.programyourhome.adventureroom.model.event.AdventureStartedEvent;
import com.programyourhome.adventureroom.model.script.Script;
import com.programyourhome.adventureroom.model.script.action.Action;
import com.programyourhome.adventureroom.server.events.EventManager;
import com.programyourhome.iotadventure.runner.action.executor.ActionExecutor;
import com.programyourhome.iotadventure.runner.context.ExecutionContext;

@Component
public class AdventureService {

    @Inject
    private EventManager eventManager;

    private ActiveAdventure activeAdventure;

    public boolean hasActiveAdventure() {
        return this.activeAdventure != null;
    }

    public ActiveAdventure getActiveAdventure() {
        return this.activeAdventure;
    }

    public synchronized void startAdventure(Adventure adventure) {
        if (this.hasActiveAdventure()) {
            throw new IllegalStateException("Cannot start a new adventure when one is still active");
        }
        this.activeAdventure = new ActiveAdventure();
        this.activeAdventure.adventure = adventure;
        this.activeAdventure.executionContext = new ExecutionContext(adventure);
        this.eventManager.resetForAdventure(adventure);
        this.eventManager.fireEvent(new AdventureStartedEvent(adventure.getId()));
    }

    public synchronized void stopAdventure() {
        if (!this.hasActiveAdventure()) {
            throw new IllegalStateException("Cannot stop an adventure when no one is active");
        }
        // TODO: how to stop an adventure: unload modules!?!
        this.activeAdventure = null;
    }

    // TODO: how to check the script is from the active adventure? loop and check
    public void runScript(Script script) {
        script.actions.forEach(action -> this.executeAction(action));
    }

    private <A extends Action> void executeAction(A action) {
        String actionClassName = action.getClass().getName();
        String actionExecutorClassName = actionClassName.replace(".model.", ".executor.") + "Executor";
        Class<? extends ActionExecutor<A>> actionExecutorClass = ReflectionUtil.classForNameNoCheckedException(actionExecutorClassName);
        ActionExecutor<A> actionExecutor = ReflectionUtil.callConstructorNoCheckedException(actionExecutorClass);
        actionExecutor.execute(action, this.activeAdventure.executionContext);
    }

}
