package com.programyourhome.iotadventure.runner.action.executor;

import com.programyourhome.adventureroom.model.script.action.Action;

public interface ActionExecutor<A extends Action> {

    public void execute(A action);

}
