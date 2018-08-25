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

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.stereotype.Component;

import com.programyourhome.adventureroom.dsl.util.ReflectionUtil;
import com.programyourhome.adventureroom.model.Adventure;
import com.programyourhome.adventureroom.model.Room;
import com.programyourhome.adventureroom.model.character.Character;
import com.programyourhome.adventureroom.model.character.CharacterDescriptor;
import com.programyourhome.adventureroom.model.event.AdventureStartedEvent;
import com.programyourhome.adventureroom.model.event.AdventureStopEvent;
import com.programyourhome.adventureroom.model.event.Event;
import com.programyourhome.adventureroom.model.event.EventDescriptor;
import com.programyourhome.adventureroom.model.module.AdventureModule;
import com.programyourhome.adventureroom.model.module.Converter;
import com.programyourhome.adventureroom.model.resource.AbstractExternalResource;
import com.programyourhome.adventureroom.model.resource.ExternalResource;
import com.programyourhome.adventureroom.model.resource.Resource;
import com.programyourhome.adventureroom.model.resource.ResourceDescriptor;
import com.programyourhome.adventureroom.model.script.Script;
import com.programyourhome.adventureroom.model.script.action.Action;
import com.programyourhome.adventureroom.model.script.action.ActionData;
import com.programyourhome.adventureroom.model.toolbox.CacheService;
import com.programyourhome.adventureroom.model.toolbox.DataStreamToUrl;
import com.programyourhome.adventureroom.model.toolbox.Toolbox;
import com.programyourhome.adventureroom.model.toolbox.ToolboxImpl;
import com.programyourhome.adventureroom.server.util.FileUtil;
import com.programyourhome.adventureroom.server.util.PropertiesUtil;
import com.programyourhome.adventureroom.server.util.StreamUtil;

import one.util.streamex.StreamEx;

@Component
public class AdventureRoomLoader {

    public static final String ROOM_PROPERTIES_FILENAME = "room.properties";
    public static final String ADVENTURES_DIRECTORY_NAME = "adventures";
    public static final String ADVENTURE_PROPERTIES_FILENAME = "adventure.properties";
    public static final String MODULE_PROPERTIES_FILENAME = "module.properties";

    @Value("${rooms.tempMergePath}")
    private String roomsTempMergePath;

    @Inject
    private GenericConversionService conversionService;

    @Inject
    private CacheService cacheService;

    @Inject
    private DataStreamToUrl dataStreamToUrl;

    private final Map<String, Class<? extends AdventureModule>> availableModules;
    private Toolbox toolbox;

    public AdventureRoomLoader() {
        this.availableModules = StreamEx.of(ServiceLoader.load(AdventureModule.class).iterator())
                .toMap(module -> module.getConfig().getId(), module -> module.getClass());
    }

    @PostConstruct
    public void init() {
        // Create a toolbox that gives access to the different services of the server.
        this.toolbox = new ToolboxImpl(this.cacheService, this.dataStreamToUrl);
    }

    private AdventureModule createNewModule(String moduleId) {
        return ReflectionUtil.callConstructorNoCheckedException(this.availableModules.get(moduleId));
    }

    public Map<String, Room> loadRooms(String roomsBasepath) {
        FileUtil.assertDirectoryExists(roomsBasepath);
        return StreamEx.of(this.getRoomPaths(roomsBasepath))
                .map(this::loadRoom)
                .toMap(Room::getId, a -> a);
    }

    private List<File> getRoomPaths(String roomsBasepath) {
        return Arrays.asList(new File(roomsBasepath).listFiles(File::isDirectory));
    }

    public Room loadRoom(File roomPath) {
        try {
            roomPath = this.mergeRoomConfigIntoAdventures(roomPath);
            FileUtil.assertDirectoryExists(roomPath);
            File roomPropertiesFile = new File(roomPath.getAbsolutePath() + "/" + ROOM_PROPERTIES_FILENAME);
            FileUtil.assertFileExists(roomPropertiesFile);

            Room room = PropertiesUtil.loadPropertiesIntoFields(new FileInputStream(roomPropertiesFile), Room.class, this.conversionService);
            room.adventures = this.loadAdventures(roomPath + "/" + ADVENTURES_DIRECTORY_NAME);
            return room;
        } catch (IOException e) {
            throw new IllegalStateException("Exception during loading of room", e);
        }
    }

    private File mergeRoomConfigIntoAdventures(File roomPath) throws IOException {
        File mergePath = new File(this.roomsTempMergePath + "/" + System.currentTimeMillis() + "/" + roomPath.getName());
        if (!mergePath.mkdirs()) {
            throw new IllegalStateException("Could not create rooms temp merge path: " + this.roomsTempMergePath);
        }
        FileUtils.copyDirectory(roomPath, mergePath);
        for (File adventurePath : new File(mergePath + "/adventures").listFiles(File::isDirectory)) {
            for (File moduleConfigPath : new File(mergePath + "/modules").listFiles(File::isDirectory)) {
                File adventureModuleConfigPath = new File(adventurePath.getAbsolutePath() + "/modules/" + moduleConfigPath.getName());
                if (adventureModuleConfigPath.exists()) {
                    // Little trick: If the adventure module also has it's own config,
                    // first copy the adventure module config over the room module config
                    // to overwrite any duplicate files in favor of the adventure module.
                    FileUtils.copyDirectory(adventureModuleConfigPath, moduleConfigPath);
                }
                // Then copy the whole thing back into the adventure module, so it can be read by the adventure loading.
                FileUtils.copyDirectory(moduleConfigPath, adventureModuleConfigPath);
            }
        }
        return mergePath;
    }

    private Map<String, Adventure> loadAdventures(String adventuresBasepath) {
        FileUtil.assertDirectoryExists(adventuresBasepath);
        return StreamEx.of(this.getAdventurePaths(adventuresBasepath))
                .map(this::loadAdventure)
                .toMap(Adventure::getId, a -> a);
    }

    private List<File> getAdventurePaths(String adventuresBasepath) {
        return Arrays.asList(new File(adventuresBasepath).listFiles(File::isDirectory));
    }

    private Adventure loadAdventure(File adventurePath) {
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

        return PropertiesUtil.loadPropertiesIntoFields(new FileInputStream(adventurePropertiesFile), Adventure.class, this.conversionService);
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
                throw new IllegalStateException("Required module: '" + requiredModuleId + "' not available");
            }
            // Create a new module instance for each adventure using that module, to allow for different config and state.
            AdventureModule module = this.createNewModule(requiredModuleId);
            // Since the modules don't use Spring, the toolbox is 'injected' with the setter.
            module.setToolbox(this.toolbox);

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
        File moduleConfigPropertiesFile = new File(moduleBasePath + "/" + MODULE_PROPERTIES_FILENAME);
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
                        Class<?> externalClass = ReflectionUtil.getGenericParameter(resourceClass,
                                Arrays.asList(ExternalResource.class, AbstractExternalResource.class));
                        Object externalObject = PropertiesUtil.loadPropertiesIntoFields(new FileInputStream(resourceDefinition),
                                externalClass, this.conversionService);
                        resource = (Resource) ReflectionUtil.callConstructorNoCheckedExceptionUntypedParameter(resourceClass, externalClass, externalObject);
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
                // TODO: check on adventure req modules, should be superset of script req modules
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
