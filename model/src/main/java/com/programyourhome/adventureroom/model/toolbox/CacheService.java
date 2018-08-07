package com.programyourhome.adventureroom.model.toolbox;

import java.io.File;

public interface CacheService {

    public int getCacheSize();

    public boolean hasResource(String id);

    public CacheResource getResource(String id);

    public File getCacheFile(String id);

    public DataStream getCacheDataStream(String id);

    public void storeResource(CacheResource resource, DataStream data);

}
