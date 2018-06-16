package com.programyourhome.adventureroom.server.loader;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import java.util.ServiceLoader;

import javax.inject.Inject;

import org.apache.commons.io.IOUtils;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Component;

import com.programyourhome.adventureroom.model.Adventure;
import com.programyourhome.adventureroom.model.character.Character;
import com.programyourhome.adventureroom.model.character.CharacterDescriptor;
import com.programyourhome.adventureroom.model.module.AdventureModule;
import com.programyourhome.adventureroom.model.resource.Resource;
import com.programyourhome.adventureroom.model.resource.ResourceDescriptor;
import com.programyourhome.adventureroom.model.script.Script;
import com.programyourhome.adventureroom.model.script.action.Action;
import com.programyourhome.adventureroom.server.util.FileUtil;
import com.programyourhome.adventureroom.server.util.PropertiesUtil;

import one.util.streamex.StreamEx;

@Component
public class AdventureLoader {

    @Inject
    private ConversionService conversionService;

    public static final String ADVENTURE_PROPERTIES_FILENAME = "adventure.properties";

    public Map<String, Adventure> loadAdventures(String adventureBasepath) {
        FileUtil.assertDirectoryExists(adventureBasepath);
        return StreamEx.of(this.getAdventurePaths(adventureBasepath))
                .map(this::loadAdventure)
                .toMap(Adventure::getId, a -> a);
    }

    private List<File> getAdventurePaths(String adventureBasepath) {
        return Arrays.asList(new File(adventureBasepath).listFiles(File::isDirectory));
    }

    public Adventure loadAdventure(File adventurePath) {
        try {
            FileUtil.assertDirectoryExists(adventurePath);
            File adventurePropertiesFile = new File(adventurePath.getAbsolutePath() + "/" + ADVENTURE_PROPERTIES_FILENAME);
            FileUtil.assertFileExists(adventurePropertiesFile);

            Adventure adventure = PropertiesUtil.loadPropertiesIntoFields(new FileInputStream(adventurePropertiesFile), Adventure.class,
                    this.conversionService);

            // TODO: Loading modules should be done somewhere at startup and not here!
            // TODO: hmm, then how to handle different properties for modules per adventure?
            // Maybe we do need to reload the whole thing with each adventure, also more dynamic regarding classpath changes during runtime.
            ServiceLoader<AdventureModule> moduleLoader = ServiceLoader.load(AdventureModule.class);
            Map<String, AdventureModule> availableModules = new HashMap<>();
            moduleLoader.forEach(module -> availableModules.put(module.getConfig().getId(), module));

            for (String requiredModuleId : adventure.requiredModules) {
                if (!availableModules.keySet().contains(requiredModuleId)) {
                    throw new IllegalStateException("Required module: '" + requiredModuleId + "' not present");
                }
                AdventureModule module = availableModules.get(requiredModuleId);
                String moduleBasePath = adventurePath.getAbsolutePath() + "/modules/" + requiredModuleId + "/";
                FileUtil.assertDirectoryExists(moduleBasePath);
                File moduleConfigPropertiesFile = new File(moduleBasePath + "module.properties");
                FileUtil.assertFileExists(moduleConfigPropertiesFile);
                PropertiesUtil.loadPropertiesIntoFields(new FileInputStream(moduleConfigPropertiesFile), module.getConfig(), this.conversionService);

                // Load characters
                File charactersFolder = new File(moduleBasePath + "characters");
                if (charactersFolder.exists()) {
                    File[] requiredModuleCharacterSuppliers = charactersFolder.listFiles(File::isDirectory);
                    for (File requiredModuleCharacterSupplier : requiredModuleCharacterSuppliers) {
                        if (!module.getConfig().characterDescriptors.containsKey(requiredModuleCharacterSupplier.getName())) {
                            throw new IllegalStateException(
                                    "Required character supplier: '" + requiredModuleCharacterSupplier.getName() + "' not found in module: '" + requiredModuleId
                                            + "'");
                        }
                        CharacterDescriptor characterDescriptor = module.getConfig().characterDescriptors.get(requiredModuleCharacterSupplier.getName());
                        for (File characterDefinition : requiredModuleCharacterSupplier.listFiles()) {
                            Character character = (Character) PropertiesUtil.loadPropertiesIntoFields(
                                    new FileInputStream(characterDefinition), characterDescriptor.clazz, this.conversionService);
                            System.out.println("Character: " + character);
                            adventure.characters.put(character.getId(), character);
                        }
                    }
                }

                // Load resources of module
                File resourcesFolder = new File(moduleBasePath + "resources");
                if (resourcesFolder.exists()) {
                    File[] requiredModuleResources = resourcesFolder.listFiles(File::isDirectory);
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
                }

                adventure.modules.put(module.getConfig().getId(), module);
            }

            // Load scripts
            // TODO: verify required modules
            String scriptsBasePath = adventurePath + "/scripts/";
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
                    if (!trimmedLine.equals("") && !trimmedLine.startsWith("#")) {
                        // TODO: streamify!
                        for (AdventureModule adventureModule : adventure.modules.values()) {
                            Optional<Action> optionalAction = adventureModule.parseForAction(trimmedLine, adventure);
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
            // adventure.triggers.put(new AdventureStartedEvent("testing"), adventure.scripts.values().iterator().next());

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
        } catch (IOException e) {
            throw new IllegalStateException("Exception during loading of adventure", e);
        }
    }

}
