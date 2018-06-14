package com.programyourhome.iotadventure.runner.action.executor;

import com.programyourhome.adventureroom.model.script.action.Action;
import com.programyourhome.iotadventure.runner.context.ExecutionContext;

public interface ActionExecutor<A extends Action> {

    public void execute(A action, ExecutionContext context);

}
