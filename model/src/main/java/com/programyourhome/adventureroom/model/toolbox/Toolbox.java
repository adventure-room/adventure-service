package com.programyourhome.adventureroom.model.toolbox;

import com.programyourhome.adventureroom.model.service.AdventureService;

public interface Toolbox {

    public ContentService getContentService();

    public CacheService getCacheService();

    public DataStreamToUrl getDataStreamToUrl();

    public ConversionService getConversionService();

    public AdventureService getAdventureService();

}
