package com.programyourhome.adventureroom.server.repository;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.programyourhome.adventureroom.model.Adventure;
import com.programyourhome.adventureroom.server.loader.AdventureLoader;

@Component
public class AdventureRepository {

    @Value("${adventure.basepath}")
    private String adventureBasepath;

    @Inject
    private AdventureLoader adventureLoader;

    private Map<String, Adventure> adventures;

    @PostConstruct
    public void init() {
        this.reloadAdventures();
    }

    public void reloadAdventures() {
        this.adventures = this.adventureLoader.loadAdventures(this.adventureBasepath);
    }

    public Map<String, Adventure> getAdventuresMap() {
        return this.adventures;
    }

    public Set<String> getAdventureIds() {
        return this.adventures.keySet();
    }

    public Collection<Adventure> getAdventures() {
        return this.adventures.values();
    }

    public Adventure getAdventure(String id) {
        return this.adventures.get(id);
    }

}
