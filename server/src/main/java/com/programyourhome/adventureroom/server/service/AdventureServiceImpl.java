package com.programyourhome.adventureroom.server.service;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

import com.programyourhome.adventureroom.model.Adventure;
import com.programyourhome.adventureroom.model.event.AdventureStartedEvent;
import com.programyourhome.adventureroom.model.event.AdventureStopEvent;
import com.programyourhome.adventureroom.model.execution.ExecutionContext;
import com.programyourhome.adventureroom.model.module.Task;
import com.programyourhome.adventureroom.model.script.Script;
import com.programyourhome.adventureroom.model.script.ScriptType;
import com.programyourhome.adventureroom.model.script.action.Action;
import com.programyourhome.adventureroom.model.service.AdventureService;
import com.programyourhome.adventureroom.model.util.IOUtil;
import com.programyourhome.adventureroom.model.util.ReflectionUtil;
import com.programyourhome.adventureroom.server.events.EventManager;
import com.programyourhome.iotadventure.runner.action.executor.ActionExecutor;
import com.programyourhome.iotadventure.runner.script.RunningScript;

import one.util.streamex.StreamEx;

@Component
public class AdventureServiceImpl implements AdventureService {

    @Inject
    private EventManager eventManager;

    private ActiveAdventure activeAdventure;
    private final Map<UUID, RunningScript> runningScripts;
    private final UncaughtExceptionHandler handleActionException;

    public AdventureServiceImpl() {
        this.runningScripts = new HashMap<>();
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

    @Override
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

    @Override
    public synchronized void stopAdventure() {
        if (!this.hasActiveAdventure()) {
            throw new IllegalStateException("Cannot stop an adventure when no one is active");
        }
        this.eventManager.fireEvent(new AdventureStopEvent(this.activeAdventure.adventure.getId()));

        this.runningScripts.values().forEach(script -> script.stop(this.activeAdventure.executionContext));
        IOUtil.waitForCondition(() -> StreamEx.of(this.runningScripts.values()).noneMatch(RunningScript::hasAnythingRunning));
        this.runningScripts.clear();
        this.activeAdventure.adventure.getModules().forEach(module -> module.stop(this.activeAdventure.adventure, this.activeAdventure.executionContext));
        this.activeAdventure = null;
    }

    @Override
    public synchronized UUID startScript(Adventure adventure, Script script) {
        // TODO: Add checks on which types of scripts can run at the same time.
        if (this.hasActiveAdventure() && !this.activeAdventure.adventure.equals(adventure)) {
            throw new IllegalStateException("Can only run scripts of the active adventure (or when no adventure is active)");
        }
        if (!this.hasActiveAdventure() && !this.runningScripts.isEmpty()) {
            throw new IllegalStateException("Can only test-run one script at a time in isolation");
        }
        boolean isolatedTestRun = !this.hasActiveAdventure();
        if (isolatedTestRun) {
            this.startAdventure(adventure, isolatedTestRun);
        }
        // FIXME: temp fix for stopping an interaction when a new scene is started
        // Mainly to get the demo working, should be fixed with 'proper triggers' later.
        if (this.runningScripts.size() == 1 && this.runningScripts.values().iterator().next().getScript().type == ScriptType.INTERACTION) {
            this.stopScript(this.runningScripts.keySet().iterator().next());
        }
        RunningScript runningScript = new RunningScript(script);
        this.runningScripts.put(runningScript.getId(), runningScript);
        Thread scriptRunner = new Thread(() -> {
            try {
                System.out.println("Start run script: " + runningScript.getId());
                this.runScriptSynchronous(adventure, runningScript);
                System.out.println("End run script: " + runningScript.getId());
            } catch (Exception e) {
                // TODO: Find out how/why setUncaughtExceptionHandler works/does not work
                System.out.println("Exception in runScriptSynchronous - kills further running of script");
                e.printStackTrace();
                throw e;
            } finally {
                // Explicitly stop the script after it's done to clean up anything still running async.
                this.stopScript(runningScript.getId());
                if (isolatedTestRun) {
                    this.stopAdventure();
                }
            }
        });
        scriptRunner.setName("Running script [" + script.getName() + "]");
        scriptRunner.setUncaughtExceptionHandler(this.handleActionException);
        scriptRunner.start();
        return runningScript.getId();
    }

    @Override
    public synchronized void stopScript(UUID scriptId) {
        Optional.ofNullable(this.runningScripts.get(scriptId)).ifPresent(runningScript -> {
            runningScript.stop(this.activeAdventure.executionContext);
            IOUtil.waitForCondition(() -> !runningScript.hasAnythingRunning());
            this.runningScripts.remove(runningScript.getId());
        });
    }

    private void runScriptSynchronous(Adventure adventure, RunningScript runningScript) {
        ExecutionContext executionContext = this.activeAdventure.executionContext;
        runningScript.getScript().actions.forEach(actionData -> {
            Action action = actionData.action;
            ActionExecutor<?> executor = this.getActionExecutor(action, executionContext);
            if (actionData.synchronous) {
                runningScript.executeAction(executor, action, executionContext);
            } else {
                Thread asyncActionExecutor = new Thread(() -> {
                    try {
                        runningScript.executeAction(executor, action, executionContext);
                    } catch (Exception e) {
                        // TODO: Find out how/why setUncaughtExceptionHandler works/does not work
                        System.out.println("Exception in asyncActionExecutor");
                        e.printStackTrace();
                        throw e;
                    }
                });
                asyncActionExecutor.setName("Executing action [" + action.getClass().getSimpleName() + "]");
                asyncActionExecutor.setUncaughtExceptionHandler(this.handleActionException);
                asyncActionExecutor.start();
            }
        });
        // In case of an interaction script, it should just keep running until it's stopped 'externally' by an event.
        if (runningScript.getScript().type == ScriptType.INTERACTION) {
            System.out.println("Waiting for script to be stopped externally because of type INTERACTION");
            IOUtil.waitForCondition(() -> runningScript.getShouldStop());
        }
    }

    private <A extends Action> ActionExecutor<A> getActionExecutor(A action, ExecutionContext executionContext) {
        String actionClassName = action.getClass().getName();
        String actionExecutorClassName = actionClassName.replace(".model.", ".executor.") + "Executor";
        Class<? extends ActionExecutor<A>> actionExecutorClass = ReflectionUtil.classForNameNoCheckedException(actionExecutorClassName);
        return ReflectionUtil.callConstructorNoCheckedException(actionExecutorClass);
    }

}
