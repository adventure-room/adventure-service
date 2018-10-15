package com.programyourhome.iotadventure.runner.action.executor;

import com.programyourhome.adventureroom.model.execution.ExecutionContext;
import com.programyourhome.adventureroom.model.script.action.Action;

/**
 * Execute a certain type of action.
 * Can also stop a running action execution.
 */
public interface ActionExecutor<A extends Action> {

    /**
     * Execute the action given the context.
     * This method should block as long as any effect of this action is still 'active'.
     * That means that something is still changing (audio / video / light / etc) based on this action.
     * When the result of the action is still visible (e.g. another light color) but not there are no changes anymore
     * based on this action, the method should return.
     * No effort has to be taken to run things async inside the executor, since if async is desired, this method will
     * be called in a separate thread.
     * Effort does need to be taken to be able to stop as soon as possible if the stop method is called.
     */
    public void execute(A action, ExecutionContext context);

    /**
     * Stop executing the action as soon as possible. This method should be called in a different thread than the execute method.
     * So the stop (and execute) implementation should take this multi-threaded behavior into account.
     * Should gracefully do nothing in case no action was ever executed or the action execution is already done.
     * The implementation of the executor should make a best effort to stop as soon as possible after this method is called.
     * When this method exits, stopping should be set in motion, but might not be done yet.
     * The thread calling the execute method should exit as quickly as possible. If it does, that indicates the stopping is complete.
     * No absolute guarantee time wise or any other way can be made on how quickly stop will actually work.
     */
    public void stop(ExecutionContext context);

}
