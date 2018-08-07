package com.programyourhome.adventureroom.model.toolbox;

import java.util.UUID;

public class UUIDCacheResource extends CacheResource {

    private final UUID id;

    public UUIDCacheResource() {
        this.id = UUID.randomUUID();
    }

    @Override
    public String getId() {
        return this.id.toString();
    }

}
