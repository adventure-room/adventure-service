package com.programyourhome.adventureroom.server.service;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

import com.programyourhome.adventureroom.model.Adventure;
import com.programyourhome.adventureroom.model.event.AdventureStartedEvent;
import com.programyourhome.adventureroom.model.event.AdventureStopEvent;
import com.programyourhome.adventureroom.model.execution.ExecutionContext;
import com.programyourhome.adventureroom.model.module.Task;
import com.programyourhome.adventureroom.model.script.Script;
import com.programyourhome.adventureroom.model.script.action.Action;
import com.programyourhome.adventureroom.model.util.ReflectionUtil;
import com.programyourhome.adventureroom.server.events.EventManager;
import com.programyourhome.iotadventure.runner.action.executor.ActionExecutor;

@Component
public class AdventureService {

    @Inject
    private EventManager eventManager;

    private ActiveAdventure activeAdventure;
    private final Set<Thread> scriptRunners;
    private final UncaughtExceptionHandler handleActionException;

    public AdventureService() {
        this.scriptRunners = new HashSet<>();
        this.handleActionException = (thread, throwable) -> {
            // TODO: proper exception logging
            System.out.println("Thread [" + thread + "] died because of exception in action execution");
            throwable.printStackTrace();
        };
    }

    public boolean hasActiveAdventure() {
        return this.activeAdventure != null;
    }

    public ActiveAdventure getActiveAdventure() {
        return this.activeAdventure;
    }

    public synchronized void startAdventure(Adventure adventure) {
        this.startAdventure(adventure, false);
    }

    private void startAdventure(Adventure adventure, boolean isolatedTestRun) {
        if (this.hasActiveAdventure()) {
            throw new IllegalStateException("Cannot start a new adventure when one is still active");
        }
        this.activeAdventure = new ActiveAdventure();
        this.activeAdventure.adventure = adventure;
        this.activeAdventure.executionContext = new ExecutionContext(adventure);

        this.eventManager.resetForAdventure(adventure);
        this.startModules(this.activeAdventure);
        if (!isolatedTestRun) {
            this.eventManager.fireEvent(new AdventureStartedEvent(adventure.getId()));
        }
    }

    private void startModules(ActiveAdventure activeAdventure) {
        activeAdventure.adventure.getModules().forEach(module -> {
            module.start(activeAdventure.adventure, activeAdventure.executionContext);
            module.getConfig().getTasks().forEach((name, task) -> {
                System.out.println("Starting task [" + name + "] for module [" + module.getConfig().getName() + "]");
                if (task.isDeamon()) {
                    Thread thread = new Thread(task);
                    thread.setDaemon(true);
                    thread.setUncaughtExceptionHandler((t, exception) -> this.handleTaskException(task, exception));
                } else {
                    try {
                        task.run();
                    } catch (Exception e) {
                        this.handleTaskException(task, e);
                    }
                }
            });
        });
    }

    private void handleTaskException(Task task, Throwable throwable) {
        // TODO: log
        throwable.printStackTrace();
        if (task.failOnException()) {
            this.stopAdventure();
            throw new IllegalStateException("Module task failed", throwable);
        }
    }

    public synchronized void stopAdventure() {
        if (!this.hasActiveAdventure()) {
            throw new IllegalStateException("Cannot stop an adventure when no one is active");
        }
        this.eventManager.fireEvent(new AdventureStopEvent(this.activeAdventure.adventure.getId()));
        // TODO: Find better mechanism for stopping script runners.
        this.scriptRunners.forEach(Thread::stop);
        this.scriptRunners.clear();
        this.activeAdventure.adventure.getModules().forEach(module -> module.stop(this.activeAdventure.adventure, this.activeAdventure.executionContext));
        this.activeAdventure = null;
    }

    public synchronized void runScript(Adventure adventure, Script script) {
        if (this.hasActiveAdventure() && !this.activeAdventure.adventure.equals(adventure)) {
            throw new IllegalStateException("Can only run scripts of the active adventure (or when no adventure is active)");
        }
        if (!this.hasActiveAdventure() && !this.scriptRunners.isEmpty()) {
            throw new IllegalStateException("Can only test-run one script at a time in isolation");
        }
        boolean isolatedTestRun = !this.hasActiveAdventure();
        if (isolatedTestRun) {
            this.startAdventure(adventure, isolatedTestRun);
        }

        // TODO: find a nicer solution for this.
        List<Thread> finalThreadList = new ArrayList<>();
        Thread scriptRunner = new Thread(() -> {
            try {
                this.runScriptSynchronous(adventure, script);
            } finally {
                // Make sure the script runner is always removed after the script is done.
                this.scriptRunners.remove(finalThreadList.get(0));
                if (isolatedTestRun) {
                    this.stopAdventure();
                }
            }
        });
        finalThreadList.add(scriptRunner);
        scriptRunner.setName("Running script [" + script.getName() + "]");
        scriptRunner.setUncaughtExceptionHandler(this.handleActionException);
        this.scriptRunners.add(scriptRunner);
        scriptRunner.start();
    }

    private void runScriptSynchronous(Adventure adventure, Script script) {
        ExecutionContext executionContext = this.activeAdventure.executionContext;
        script.actions.forEach(actionData -> {
            if (actionData.synchronous) {
                this.executeAction(actionData.action, executionContext);
            } else {
                Thread asyncActionExecutor = new Thread(() -> this.executeAction(actionData.action, executionContext));
                asyncActionExecutor.setUncaughtExceptionHandler(this.handleActionException);
                asyncActionExecutor.start();
            }
        });
    }

    private <A extends Action> void executeAction(A action, ExecutionContext executionContext) {
        String actionClassName = action.getClass().getName();
        String actionExecutorClassName = actionClassName.replace(".model.", ".executor.") + "Executor";
        Class<? extends ActionExecutor<A>> actionExecutorClass = ReflectionUtil.classForNameNoCheckedException(actionExecutorClassName);
        ActionExecutor<A> actionExecutor = ReflectionUtil.callConstructorNoCheckedException(actionExecutorClass);
        System.out.println("About to execute action: " + action);
        actionExecutor.execute(action, executionContext);
    }

}
