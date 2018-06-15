package com.programyourhome.adventureroom.server.controllers;

import java.util.Collection;

import javax.inject.Inject;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.programyourhome.adventureroom.model.Describable;
import com.programyourhome.adventureroom.model.script.Script;
import com.programyourhome.adventureroom.server.service.AdventureService;

import one.util.streamex.StreamEx;

@RestController
@RequestMapping("scripts")
public class ScriptsController {

    @Inject
    private AdventureService adventureService;

    @Inject
    private ObjectConverter objectConverter;

    @RequestMapping("")
    public Collection<Describable> listScripts() {
        // TODO: generify and put into abstract adventure room controller
        return StreamEx.of(this.adventureService.getActiveAdventure().adventure.scripts)
                .map(script -> this.objectConverter.convert(script, Describable.class))
                .toSet();
    }

    @RequestMapping("{id}")
    public Describable getScript(@PathVariable("id") final String id) {
        Script script = this.adventureService.getActiveAdventure().adventure.scripts.get(id);
        return this.objectConverter.convert(script, Describable.class);
    }

    // Just run the script with the specified name, within the active adventure.
    @RequestMapping("{id}/run")
    public void runScript(@PathVariable("id") final String id) {
        // TODO: private helper method for getScript
        this.adventureService.runScript(this.adventureService.getActiveAdventure().adventure.scripts.get(id));
    }

}
