package com.programyourhome.adventureroom.server.service;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.HashSet;
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

        this.startModules(adventure);

        this.eventManager.resetForAdventure(adventure);
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
        if (task.failOnException()) {
            this.stopAdventure();
            throw new IllegalStateException("Module task failed", throwable);
        } else {
            // TODO: just log exception
        }
    }

    public synchronized void stopAdventure() {
        if (!this.hasActiveAdventure()) {
            throw new IllegalStateException("Cannot stop an adventure when no one is active");
        }
        this.eventManager.fireEvent(new AdventureStopEvent(this.activeAdventure.adventure.getId()));
        // TODO: Find better mechanism for stopping script runners.
        this.scriptRunners.forEach(Thread::stop);
        this.activeAdventure.adventure.getModules().forEach(AdventureModule::stop);
        this.activeAdventure = null;
    }

    // TODO: how to check the script is from the active adventure? loop and check
    public void runScript(Script script) {
        Thread scriptRunner = new Thread(() -> this.runScriptSynchronous(script), "Running script [" + script.getName() + "]");
        scriptRunner.setUncaughtExceptionHandler(this.handleActionException);
        this.scriptRunners.add(scriptRunner);
        scriptRunner.start();
    }

    private void runScriptSynchronous(Script script) {
        script.actions.forEach(actionData -> {
            if (actionData.synchronous) {
                this.executeAction(actionData.action);
            } else {
                Thread asyncActionExecutor = new Thread(() -> this.executeAction(actionData.action));
                asyncActionExecutor.setUncaughtExceptionHandler(this.handleActionException);
                asyncActionExecutor.start();
            }
        });
    }

    private <A extends Action> void executeAction(A action) {
        String actionClassName = action.getClass().getName();
        String actionExecutorClassName = actionClassName.replace(".model.", ".executor.") + "Executor";
        Class<? extends ActionExecutor<A>> actionExecutorClass = ReflectionUtil.classForNameNoCheckedException(actionExecutorClassName);
        ActionExecutor<A> actionExecutor = ReflectionUtil.callConstructorNoCheckedException(actionExecutorClass);
        actionExecutor.execute(action, this.activeAdventure.executionContext);
    }

}
