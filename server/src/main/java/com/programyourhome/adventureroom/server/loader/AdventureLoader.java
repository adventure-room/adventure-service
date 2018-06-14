package com.programyourhome.adventureroom.server.loader;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import java.util.ServiceLoader;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.commons.io.IOUtils;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Component;

import com.programyourhome.adventureroom.PyhSpringBootApplication;
import com.programyourhome.adventureroom.dsl.util.ReflectionUtil;
import com.programyourhome.adventureroom.model.Adventure;
import com.programyourhome.adventureroom.model.character.Character;
import com.programyourhome.adventureroom.model.character.CharacterDescriptor;
import com.programyourhome.adventureroom.model.module.AdventureModule;
import com.programyourhome.adventureroom.model.resource.Resource;
import com.programyourhome.adventureroom.model.resource.ResourceDescriptor;
import com.programyourhome.adventureroom.model.script.Script;
import com.programyourhome.adventureroom.model.script.action.Action;
import com.programyourhome.adventureroom.server.ProgramYourHomeServer;
import com.programyourhome.adventureroom.server.util.FileUtil;
import com.programyourhome.adventureroom.server.util.PropertiesUtil;
import com.programyourhome.iotadventure.runner.action.executor.ActionExecutor;
import com.programyourhome.iotadventure.runner.context.ExecutionContext;

import one.util.streamex.StreamEx;

@Component
public class AdventureLoader {

    @Inject
    private ConversionService conversionService;

    public static final String ADVENTURE_PROPERTIES_FILENAME = "adventure.properties";

    public static void main(String[] args) {
        PyhSpringBootApplication.main(new String[0]);
    }

    @PostConstruct
    public void testLoading() {
        // UGLY!!
        new Thread(() -> {
            try {
                Thread.sleep(1000);
                Adventure adventure = this.load("testing");
                System.out.println();
                System.out.println();
                System.out.println(adventure);
                System.out.println();
                System.out.println();
                System.out.println("DONE LOADING!!");

                System.out.println();
                System.out.println();
                System.out.println("Start execution...");

                Script script = adventure.scripts.values().iterator().next();
                for (Action action : script.actions) {
                    String actionClassName = action.getClass().getName();
                    String actionExecutorClassName = actionClassName.replace(".model.", ".executor.") + "Executor";
                    this.executeAction(action, actionExecutorClassName);
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                ProgramYourHomeServer.stopServer();
            }
        }).start();
    }

    private <A extends Action> void executeAction(A action, String actionExecutorClassName) {
        Class<? extends ActionExecutor<A>> actionExecutorClass = ReflectionUtil.classForNameNoCheckedException(actionExecutorClassName);
        ActionExecutor<A> actionExecutor = ReflectionUtil.callConstructorNoCheckedException(actionExecutorClass);
        actionExecutor.execute(action, this.executionContect);
    }

    // TODO: make configurable
    private final String adventuresBasePath = "src/test/resources/adventures/";

    private String getAdventurePath(String id) {
        return this.adventuresBasePath + "/" + id + "/";
    }

    public List<String> getAdventureIds() {
        return StreamEx.of(new File(this.adventuresBasePath).listFiles(File::isDirectory))
                .map(File::getName)
                .toList();
    }

    public boolean doesAdventureExist(String id) {
        return new File(this.getAdventurePath(id)).exists();
    }

    private ExecutionContext executionContect;

    // TODO: completely refactor this once logic works well.
    // TODO: Also support unloading of modules, since you should be able to switch between adventures
    public Adventure load(String id) throws IOException {
        FileUtil.assertDirectoryExists(this.getAdventurePath(id));
        File adventurePropertiesFile = new File(this.getAdventurePath(id) + ADVENTURE_PROPERTIES_FILENAME);
        FileUtil.assertFileExists(adventurePropertiesFile);

        Adventure adventure = PropertiesUtil.loadPropertiesIntoFields(new FileInputStream(adventurePropertiesFile), Adventure.class, this.conversionService);

        // TODO: Loading modules should be done somewhere at startup and not here!
        // TODO: hmm, then how to handle different properties for modules per adventure?
        // Maybe we do need to reload the whole thing with each adventure, also more dynamic regarding classpath changes during runtime.
        ServiceLoader<AdventureModule> moduleLoader = ServiceLoader.load(AdventureModule.class);
        Map<String, AdventureModule> availableModules = new HashMap<>();
        moduleLoader.forEach(module -> availableModules.put(module.getConfig().getId(), module));

        // TODO: load this separately
        this.executionContect = new ExecutionContext();
        this.executionContect.modules = availableModules;

        for (String requiredModuleId : adventure.requiredModules) {
            if (!availableModules.keySet().contains(requiredModuleId)) {
                throw new IllegalStateException("Required module: '" + requiredModuleId + "' not present");
            }
            AdventureModule module = availableModules.get(requiredModuleId);
            String moduleBasePath = this.getAdventurePath(id) + "modules/" + requiredModuleId + "/";
            FileUtil.assertDirectoryExists(moduleBasePath);
            File moduleConfigPropertiesFile = new File(moduleBasePath + "module.properties");
            FileUtil.assertFileExists(moduleConfigPropertiesFile);
            PropertiesUtil.loadPropertiesIntoFields(new FileInputStream(moduleConfigPropertiesFile), module.getConfig(), this.conversionService);

            // Load characters
            File[] requiredModuleCharacterSuppliers = new File(moduleBasePath + "characters").listFiles(File::isDirectory);
            for (File requiredModuleCharacterSupplier : requiredModuleCharacterSuppliers) {
                if (!module.getConfig().characterDescriptors.containsKey(requiredModuleCharacterSupplier.getName())) {
                    throw new IllegalStateException(
                            "Required character supplier: '" + requiredModuleCharacterSupplier.getName() + "' not found in module: '" + requiredModuleId + "'");
                }
                CharacterDescriptor characterDescriptor = module.getConfig().characterDescriptors.get(requiredModuleCharacterSupplier.getName());
                for (File characterDefinition : requiredModuleCharacterSupplier.listFiles()) {
                    Character character = (Character) PropertiesUtil.loadPropertiesIntoFields(
                            new FileInputStream(characterDefinition), characterDescriptor.clazz, this.conversionService);
                    System.out.println("Character: " + character);
                }
            }

            // Load resources of module
            File[] requiredModuleResources = new File(moduleBasePath + "resources").listFiles(File::isDirectory);
            for (File requiredModuleResource : requiredModuleResources) {
                if (!module.getConfig().resourceDescriptors.containsKey(requiredModuleResource.getName())) {
                    throw new IllegalStateException(
                            "Required resource: '" + requiredModuleResource.getName() + "' not found in module: '" + requiredModuleId + "'");
                }
                ResourceDescriptor resourceDescriptor = module.getConfig().resourceDescriptors.get(requiredModuleResource.getName());
                for (File resourceDefinition : requiredModuleResource.listFiles()) {
                    Resource resource = (Resource) PropertiesUtil.loadPropertiesIntoFields(new FileInputStream(resourceDefinition),
                            resourceDescriptor.clazz, this.conversionService);
                    System.out.println("Resource: " + resource);
                }
            }

            adventure.modules.put(module.getConfig().getName(), module);
        }

        // Load scripts
        String scriptsBasePath = this.getAdventurePath(id) + "scripts/";
        FileUtil.assertDirectoryExists(scriptsBasePath);
        File[] scriptFiles = new File(scriptsBasePath).listFiles();
        for (File scriptFile : scriptFiles) {
            // TODO: non checked exception toString
            String scriptString = IOUtils.toString(new FileInputStream(scriptFile), Charset.forName("UTF-8"));
            String[] scriptParts = scriptString.split("---");
            if (scriptParts.length != 2) {
                throw new IllegalStateException("Script: '" + scriptFile + "' should contain splitter '---' (once)");
            }
            String scriptProperties = scriptParts[0];
            String scriptActions = scriptParts[1];
            Script script = PropertiesUtil.loadPropertiesIntoFields(new ByteArrayInputStream(scriptProperties.getBytes()),
                    Script.class, this.conversionService);
            // TODO: string to lines -> util
            List<String> lines = new ArrayList<>();
            @SuppressWarnings("resource")
            Scanner scanner = new Scanner(scriptActions);
            while (scanner.hasNextLine()) {
                lines.add(scanner.nextLine());
            }
            for (String line : lines) {
                String trimmedLine = line.trim();
                if (!trimmedLine.equals("") && !trimmedLine.startsWith("//")) {
                    // TODO: streamify!
                    for (AdventureModule adventureModule : adventure.modules.values()) {
                        Optional<Action> optionalAction = adventureModule.parseForAction(trimmedLine);
                        if (optionalAction.isPresent()) {
                            System.out.println("Action: '" + optionalAction.get() + "' found with module: '" + adventureModule + " when parsing line: '"
                                    + trimmedLine + "'");
                            script.actions.add(optionalAction.get());
                            break;
                        }
                        // TODO: fail if no module could parse the action line
                    }
                }
            }
            adventure.scripts.put(script.id, script);
        }

        // TODO: load triggers

        // Must be done now instead of when discovering modules, cause otherwise the extra props are not available
        moduleLoader.forEach(module -> {
            // Start deamons
            // TODO: only start required modules!
            module.getConfig().deamons.forEach((name, runnable) -> {
                System.out.println("Starting deamon [" + name + "] for module [" + module.getConfig().getName() + "]");
                new Thread(runnable).start();
            });
        });

        return adventure;
    }

}
