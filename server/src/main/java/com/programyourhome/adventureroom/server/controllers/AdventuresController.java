package com.programyourhome.adventureroom.server.controllers;

import java.util.Set;

import javax.inject.Inject;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.programyourhome.adventureroom.model.Adventure;
import com.programyourhome.adventureroom.model.Describable;
import com.programyourhome.adventureroom.server.repository.AdventureRepository;
import com.programyourhome.adventureroom.server.service.AdventureService;

import one.util.streamex.StreamEx;

@RestController
@RequestMapping("adventures")
public class AdventuresController {

    @Inject
    private AdventureRepository adventureRepository;

    @Inject
    private AdventureService adventureService;

    @Inject
    private ObjectConverter objectConverter;

    @RequestMapping("")
    public Set<Describable> listAdventures() {
        return StreamEx.of(this.adventureRepository.getAdventures())
                .map(adventure -> this.objectConverter.convert(adventure, Describable.class))
                .toSet();
    }

    @RequestMapping("{id}")
    public Describable getAdventure(@PathVariable("id") final String id) {
        Adventure adventure = this.adventureRepository.getAdventure(id);
        return this.objectConverter.convert(adventure, Describable.class);
    }

    @RequestMapping("{id}/start")
    public void startAdventure(@PathVariable("id") final String id) {
        Adventure adventure = this.adventureRepository.getAdventure(id);
        this.adventureService.startAdventure(adventure);
    }

    @RequestMapping("stop")
    public void stopAdventure() {
        this.adventureService.stopAdventure();
    }

}
