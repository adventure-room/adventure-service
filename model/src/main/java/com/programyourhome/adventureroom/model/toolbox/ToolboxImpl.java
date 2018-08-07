package com.programyourhome.adventureroom.model.toolbox;

public class ToolboxImpl implements Toolbox {

    private final CacheService cacheService;
    private final DataStreamToUrl dataStreamToUrl;

    public ToolboxImpl(CacheService cacheService, DataStreamToUrl dataStreamToUrl) {
        this.cacheService = cacheService;
        this.dataStreamToUrl = dataStreamToUrl;
    }

    @Override
    public CacheService getCacheService() {
        return this.cacheService;
    }

    @Override
    public DataStreamToUrl getDataStreamToUrl() {
        return this.dataStreamToUrl;
    }

}
