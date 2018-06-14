package com.programyourhome.adventureroom.model.module;

import java.util.HashMap;
import java.util.Map;

import com.programyourhome.adventureroom.model.AbstractDescribable;
import com.programyourhome.adventureroom.model.character.CharacterDescriptor;
import com.programyourhome.adventureroom.model.event.EventDescriptor;
import com.programyourhome.adventureroom.model.resource.ResourceDescriptor;
import com.programyourhome.adventureroom.model.script.action.ActionDescriptor;

public class ModuleConfig extends AbstractDescribable {

    // TODO: add converters with simple Function-like convert interface that can at service module be 'converted' into Spring Converter.

    public Map<String, ActionDescriptor> actionDescriptors = new HashMap<>();
    public Map<String, ResourceDescriptor> resourceDescriptors = new HashMap<>();
    public Map<String, EventDescriptor> eventDescriptors = new HashMap<>();
    public Map<String, CharacterDescriptor> characterDescriptors = new HashMap<>();
    public Map<String, Runnable> deamons = new HashMap<>();

}
