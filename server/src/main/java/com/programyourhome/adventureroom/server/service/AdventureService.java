package com.programyourhome.adventureroom.server.service;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

import com.programyourhome.adventureroom.dsl.util.ReflectionUtil;
import com.programyourhome.adventureroom.model.Adventure;
import com.programyourhome.adventureroom.model.event.AdventureStartedEvent;
import com.programyourhome.adventureroom.model.event.AdventureStopEvent;
import com.programyourhome.adventureroom.model.module.AdventureModule;
import com.programyourhome.adventureroom.model.module.Task;
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
        if (this.hasActiveAdventure()) {
            throw new IllegalStateException("Cannot start a new adventure when one is still active");
        }
        this.activeAdventure = new ActiveAdventure();
        this.activeAdventure.adventure = adventure;
        this.activeAdventure.executionContext = new ExecutionContext(adventure);

        this.eventManager.resetForAdventure(adventure);
        this.startModules(adventure);
        this.eventManager.fireEvent(new AdventureStartedEvent(adventure.getId()));
    }

    private void startModules(Adventure adventure) {
        adventure.getModules().forEach(module -> {
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
        this.activeAdventure.adventure.getModules().forEach(AdventureModule::stop);
        this.activeAdventure = null;
    }

    public synchronized void runScript(Adventure adventure, Script script) {
        if (this.hasActiveAdventure() && !this.activeAdventure.adventure.equals(adventure)) {
            throw new IllegalStateException("Can only run scripts of the active adventure (or when no adventure is active)");
        }
        if (!this.hasActiveAdventure() && !this.scriptRunners.isEmpty()) {
            throw new IllegalStateException("Can only test-run one script at a time in isolation");
        }
        // TODO: find a nicer solution for this.
        List<Thread> finalThreadList = new ArrayList<>();
        Thread scriptRunner = new Thread(() -> {
            try {
                this.runScriptSynchronous(adventure, script);
            } finally {
                // Make sure the script runner is always removed after the script is done.
                this.scriptRunners.remove(finalThreadList.get(0));
            }
        });
        finalThreadList.add(scriptRunner);
        scriptRunner.setName("Running script [" + script.getName() + "]");
        scriptRunner.setUncaughtExceptionHandler(this.handleActionException);
        this.scriptRunners.add(scriptRunner);
        scriptRunner.start();
    }

    private void runScriptSynchronous(Adventure adventure, Script script) {
        ExecutionContext executionContext;
        if (this.hasActiveAdventure()) {
            executionContext = this.activeAdventure.executionContext;
        } else {
            // No adventure is running: run the script in 'isolation' (feature for testing).
            this.startModules(adventure);
            executionContext = new ExecutionContext(adventure);
        }
        script.actions.forEach(actionData -> {
            if (actionData.synchronous) {
                this.executeAction(actionData.action, executionContext);
            } else {
                Thread asyncActionExecutor = new Thread(() -> this.executeAction(actionData.action, executionContext));
                asyncActionExecutor.setUncaughtExceptionHandler(this.handleActionException);
                asyncActionExecutor.start();
            }
        });
        if (!this.hasActiveAdventure()) {
            // Stop the isolation run.
            adventure.getModules().forEach(AdventureModule::stop);
        }
    }

    private <A extends Action> void executeAction(A action, ExecutionContext executionContext) {
        String actionClassName = action.getClass().getName();
        String actionExecutorClassName = actionClassName.replace(".model.", ".executor.") + "Executor";
        Class<? extends ActionExecutor<A>> actionExecutorClass = ReflectionUtil.classForNameNoCheckedException(actionExecutorClassName);
        ActionExecutor<A> actionExecutor = ReflectionUtil.callConstructorNoCheckedException(actionExecutorClass);
        actionExecutor.execute(action, executionContext);
    }

}
