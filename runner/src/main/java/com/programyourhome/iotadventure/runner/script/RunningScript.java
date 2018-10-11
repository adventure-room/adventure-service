package com.programyourhome.iotadventure.runner.script;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.programyourhome.adventureroom.model.execution.ExecutionContext;
import com.programyourhome.adventureroom.model.script.Script;
import com.programyourhome.adventureroom.model.script.action.Action;
import com.programyourhome.iotadventure.runner.action.executor.ActionExecutor;

import one.util.streamex.StreamEx;

public class RunningScript {

    private final UUID id;
    private final Script script;
    private final Set<ActionExecutor<?>> executingActions;
    private final Map<UUID, RunningScript> runningSubScripts;
    private boolean shouldStop;

    public RunningScript(Script script) {
        this.id = UUID.randomUUID();
        this.script = script;
        this.executingActions = new HashSet<>();
        this.runningSubScripts = new HashMap<>();
        this.shouldStop = false;
    }

    public UUID getId() {
        return this.id;
    }

    public Script getScript() {
        return this.script;
    }

    public boolean getShouldStop() {
        return this.shouldStop;
    }

    public boolean hasAnythingRunning() {
        return !this.executingActions.isEmpty() || this.hasAnySubscriptAnythingRunning();
    }

    private boolean hasAnySubscriptAnythingRunning() {
        return StreamEx.of(this.runningSubScripts.values())
                .anyMatch(RunningScript::hasAnythingRunning);
    }

    @SuppressWarnings("unchecked")
    public <A extends Action> void executeAction(ActionExecutor<A> executor, Action action, ExecutionContext context) {
        synchronized (this.executingActions) {
            if (this.shouldStop) {
                return;
            } else {
                this.executingActions.add(executor);
            }
        }
        System.out.println("Before execute action");
        executor.execute((A) action, context);
        System.out.println("After execute action");
        this.executingActions.remove(executor);
    }

    public void stop() {
        synchronized (this.executingActions) {
            this.shouldStop = true;
            this.executingActions.forEach(ActionExecutor::stop);
        }
        this.runningSubScripts.values().forEach(RunningScript::stop);
    }

}
