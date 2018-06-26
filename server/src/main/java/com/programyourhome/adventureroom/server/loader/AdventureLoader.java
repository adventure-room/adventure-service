package com.programyourhome.adventureroom.server.loader;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.ServiceLoader;

import javax.inject.Inject;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.stereotype.Component;

import com.programyourhome.adventureroom.dsl.util.ReflectionUtil;
import com.programyourhome.adventureroom.model.Adventure;
import com.programyourhome.adventureroom.model.character.Character;
import com.programyourhome.adventureroom.model.character.CharacterDescriptor;
import com.programyourhome.adventureroom.model.event.AdventureStartedEvent;
import com.programyourhome.adventureroom.model.event.AdventureStopEvent;
import com.programyourhome.adventureroom.model.event.Event;
import com.programyourhome.adventureroom.model.event.EventDescriptor;
import com.programyourhome.adventureroom.model.module.AdventureModule;
import com.programyourhome.adventureroom.model.module.Converter;
import com.programyourhome.adventureroom.model.resource.ExternalResource;
import com.programyourhome.adventureroom.model.resource.Resource;
import com.programyourhome.adventureroom.model.resource.ResourceDescriptor;
import com.programyourhome.adventureroom.model.script.Script;
import com.programyourhome.adventureroom.model.script.action.Action;
import com.programyourhome.adventureroom.model.script.action.ActionData;
import com.programyourhome.adventureroom.server.util.FileUtil;
import com.programyourhome.adventureroom.server.util.PropertiesUtil;
import com.programyourhome.adventureroom.server.util.StreamUtil;

import one.util.streamex.StreamEx;

@Component
public class AdventureLoader {

    public static final String ADVENTURE_PROPERTIES_FILENAME = "adventure.properties";

    @Inject
    private GenericConversionService conversionService;

    private final Map<String, AdventureModule> availableModules;

    public AdventureLoader() {
        this.availableModules = StreamEx.of(ServiceLoader.load(AdventureModule.class).iterator())
                .toMap(module -> module.getConfig().getId(), module -> module);
    }

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
            Adventure adventure = this.loadAdventureBase(adventurePath);

            Map<String, EventDescriptor<? extends Event>> availableEvents = new HashMap<>();
            this.loadBaseAvailableEvents(availableEvents);

            this.loadAdventureData(adventurePath, adventure, availableEvents);

            return adventure;
        } catch (IOException e) {
            throw new IllegalStateException("Exception during loading of adventure", e);
        }
    }

    private Adventure loadAdventureBase(File adventurePath) throws IOException {
        FileUtil.assertDirectoryExists(adventurePath);
        File adventurePropertiesFile = new File(adventurePath.getAbsolutePath() + "/" + ADVENTURE_PROPERTIES_FILENAME);
        FileUtil.assertFileExists(adventurePropertiesFile);

        return PropertiesUtil.loadPropertiesIntoFields(new FileInputStream(adventurePropertiesFile), Adventure.class,
                this.conversionService);
    }

    private void loadBaseAvailableEvents(Map<String, EventDescriptor<? extends Event>> availableEvents) {
        EventDescriptor<AdventureStartedEvent> adventureStartEventDescriptor = new EventDescriptor<>();
        adventureStartEventDescriptor.id = AdventureStartedEvent.class.getSimpleName();
        adventureStartEventDescriptor.name = "Adventure Started";
        adventureStartEventDescriptor.clazz = AdventureStartedEvent.class;
        availableEvents.put(adventureStartEventDescriptor.getId(), adventureStartEventDescriptor);

        EventDescriptor<AdventureStopEvent> adventureStopEventDescriptor = new EventDescriptor<>();
        adventureStopEventDescriptor.id = AdventureStopEvent.class.getSimpleName();
        adventureStopEventDescriptor.name = "Adventure Stopping";
        adventureStopEventDescriptor.clazz = AdventureStopEvent.class;
        availableEvents.put(adventureStopEventDescriptor.getId(), adventureStopEventDescriptor);
    }

    private void loadAdventureData(File adventurePath, Adventure adventure, Map<String, EventDescriptor<? extends Event>> availableEvents) throws IOException {
        for (String requiredModuleId : adventure.getRequiredModules()) {
            if (!this.availableModules.keySet().contains(requiredModuleId)) {
                throw new IllegalStateException("Required module: '" + requiredModuleId + "' not present");
            }
            AdventureModule module = this.availableModules.get(requiredModuleId);

            String moduleBasePath = adventurePath.getAbsolutePath() + "/modules/" + requiredModuleId + "/";

            this.loadModuleBase(moduleBasePath, module);
            module.getConfig().getConverters().forEach(this::addSpringConverter);
            availableEvents.putAll(module.getConfig().getEventDescriptorMap());

            this.loadCharacters(moduleBasePath, module, adventure);

            this.loadResources(moduleBasePath, module, adventure);

            adventure.addModule(module);
        }

        this.loadScripts(adventurePath, adventure);

        this.loadTriggers(adventurePath, adventure, availableEvents);
    }

    private void loadModuleBase(String moduleBasePath, AdventureModule module) throws IOException {
        FileUtil.assertDirectoryExists(moduleBasePath);
        File moduleConfigPropertiesFile = new File(moduleBasePath + "module.properties");
        FileUtil.assertFileExists(moduleConfigPropertiesFile);
        PropertiesUtil.loadPropertiesIntoFields(new FileInputStream(moduleConfigPropertiesFile), module.getConfig(), this.conversionService);
    }

    private void loadCharacters(String moduleBasePath, AdventureModule module, Adventure adventure) throws FileNotFoundException {
        File charactersFolder = new File(moduleBasePath + "characters");
        if (charactersFolder.exists()) {
            File[] requiredModuleCharacterTypes = charactersFolder.listFiles(File::isDirectory);
            for (File requiredModuleCharacterType : requiredModuleCharacterTypes) {
                String characterTypeId = requiredModuleCharacterType.getName();
                if (!module.getConfig().getCharacterDescriptorMap().containsKey(characterTypeId)) {
                    throw new IllegalStateException(
                            "Required character descriptor: '" + characterTypeId + "' not found in module: '"
                                    + module.getConfig().getId() + "'");
                }
                CharacterDescriptor<? extends Character> characterDescriptor = module.getConfig().getCharacterDescriptor(characterTypeId);
                for (File characterDefinition : requiredModuleCharacterType.listFiles()) {
                    Character character = PropertiesUtil.loadPropertiesIntoFields(
                            new FileInputStream(characterDefinition), characterDescriptor.clazz, this.conversionService);
                    System.out.println("Character: " + character);
                    adventure.addCharacter(character);
                }
            }
        }
    }

    // TODO: abstract load character / resource

    private void loadResources(String moduleBasePath, AdventureModule module, Adventure adventure) throws FileNotFoundException {
        File resourcesFolder = new File(moduleBasePath + "resources");
        if (resourcesFolder.exists()) {
            File[] requiredModuleResourceTypes = resourcesFolder.listFiles(File::isDirectory);
            for (File requiredModuleResourceType : requiredModuleResourceTypes) {
                String moduleTypeId = requiredModuleResourceType.getName();
                if (!module.getConfig().getResourceDescriptorMap().containsKey(moduleTypeId)) {
                    throw new IllegalStateException(
                            "Required resource: '" + moduleTypeId + "' not found in module: '" + module.getConfig().getId() + "'");
                }
                ResourceDescriptor<? extends Resource> resourceDescriptor = module.getConfig().getResourceDescriptor(moduleTypeId);
                for (File resourceDefinition : requiredModuleResourceType.listFiles()) {
                    Class<? extends Resource> resourceClass = resourceDescriptor.clazz;
                    Resource resource;
                    if (ExternalResource.class.isAssignableFrom(resourceClass)) {
                        Class<?> externalClass = ReflectionUtil.getGenericParameter(resourceClass, ExternalResource.class);
                        Object externalObject = PropertiesUtil.loadPropertiesIntoFields(new FileInputStream(resourceDefinition),
                                externalClass, this.conversionService);
                        resource = (Resource) ReflectionUtil.callConstructorNoCheckedExceptionNoTypes(resourceClass, externalClass, externalObject);
                    } else {
                        resource = PropertiesUtil.loadPropertiesIntoFields(new FileInputStream(resourceDefinition),
                                resourceClass, this.conversionService);
                    }
                    System.out.println("Resource: " + resource);
                    adventure.addResource(resource);
                }
            }
        }
    }

    private void loadScripts(File adventurePath, Adventure adventure) throws IOException {
        String scriptsBasePath = adventurePath + "/scripts/";
        FileUtil.assertDirectoryExists(scriptsBasePath);
        for (File scriptFile : new File(scriptsBasePath).listFiles()) {
            String scriptString = IOUtils.toString(new FileInputStream(scriptFile), Charset.forName("UTF-8"));
            String[] scriptParts = scriptString.split("---");
            if (scriptParts.length != 2) {
                throw new IllegalStateException("Script: '" + scriptFile + "' should contain splitter '---' (once)");
            }
            String scriptProperties = scriptParts[0];
            String scriptActions = scriptParts[1];
            Script script = PropertiesUtil.loadPropertiesIntoFields(scriptProperties, Script.class, this.conversionService);
            script.requiredModules.forEach(requiredModuleId -> {
                if (!this.availableModules.containsKey(requiredModuleId)) {
                    throw new IllegalStateException("Module [" + requiredModuleId + "] is not available but is required by script [" + script.id + "]");
                }
            });
            for (String rawLine : IOUtils.readLines(new StringReader(scriptActions))) {
                rawLine = rawLine.trim();
                if (!rawLine.equals("") && !rawLine.startsWith("#")) {
                    boolean synchronous = true;
                    if (rawLine.endsWith(" &")) {
                        rawLine = StringUtils.removeEnd(rawLine, " &");
                        synchronous = false;
                    }
                    String line = rawLine;
                    ActionData actionData = new ActionData();
                    actionData.synchronous = synchronous;
                    actionData.action = this.parseForAction(line, adventure).orElseThrow(
                            () -> new IllegalStateException("Line [" + line + "] could not be parsed into an action"));
                    script.actions.add(actionData);
                }
            }
            adventure.addScript(script);
        }
    }

    private Optional<Action> parseForAction(String line, Adventure adventure) {
        return StreamEx.of(adventure.getModules())
                .map(module -> module.parseForAction(line, adventure))
                .flatMap(StreamUtil::optionalToStream)
                .findFirst();
    }

    private void loadTriggers(File adventurePath, Adventure adventure, Map<String, EventDescriptor<? extends Event>> availableEvents)
            throws IOException, FileNotFoundException {
        String triggersBasePath = adventurePath + "/triggers/";
        FileUtil.assertDirectoryExists(triggersBasePath);
        File[] triggerFiles = new File(triggersBasePath).listFiles();
        for (File triggerFile : triggerFiles) {
            String triggerString = IOUtils.toString(new FileInputStream(triggerFile), Charset.forName("UTF-8"));
            Properties properties = new Properties();
            properties.load(new ByteArrayInputStream(triggerString.getBytes()));

            String eventId = properties.getProperty("event");
            String scriptId = properties.getProperty("script");
            EventDescriptor<? extends Event> eventDescriptor = availableEvents.get(eventId);
            Script script = adventure.getScript(scriptId);

            Map<String, String> propertyMap = StreamEx.of(properties.stringPropertyNames())
                    .filter(name -> !name.equals("event") && !name.equals("script"))
                    .toMap(properties::getProperty);
            Event event = ReflectionUtil.callConstructorNoCheckedException(eventDescriptor.clazz, propertyMap,
                    (value, targetType) -> this.conversionService.convert(value, targetType));

            adventure.addTrigger(event, script);
        }
    }

    private <From, To> void addSpringConverter(Converter<From, To> converter) {
        this.conversionService.addConverter(converter.getFromClass(), converter.getToClass(), from -> converter.convert(from));
    }

}
