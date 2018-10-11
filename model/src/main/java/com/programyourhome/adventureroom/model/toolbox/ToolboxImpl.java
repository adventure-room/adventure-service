package com.programyourhome.adventureroom.model.toolbox;

import com.programyourhome.adventureroom.model.service.AdventureService;

public class ToolboxImpl implements Toolbox {

    private final ContentService contentService;
    private final CacheService cacheService;
    private final DataStreamToUrl dataStreamToUrl;
    private final ConversionService conversionService;
    private final AdventureService adventureService;

    public ToolboxImpl(ContentService contentService, CacheService cacheService,
            DataStreamToUrl dataStreamToUrl, ConversionService conversionService, AdventureService adventureService) {
        this.contentService = contentService;
        this.cacheService = cacheService;
        this.dataStreamToUrl = dataStreamToUrl;
        this.conversionService = conversionService;
        this.adventureService = adventureService;
    }

    @Override
    public ContentService getContentService() {
        return this.contentService;
    }

    @Override
    public CacheService getCacheService() {
        return this.cacheService;
    }

    @Override
    public DataStreamToUrl getDataStreamToUrl() {
        return this.dataStreamToUrl;
    }

    @Override
    public ConversionService getConversionService() {
        return this.conversionService;
    }

    @Override
    public AdventureService getAdventureService() {
        return this.adventureService;
    }

}
