package com.programyourhome.adventureroom.server.cache;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.programyourhome.adventureroom.dsl.util.ReflectionUtil;
import com.programyourhome.adventureroom.model.toolbox.CacheResource;
import com.programyourhome.adventureroom.model.toolbox.CacheService;
import com.programyourhome.adventureroom.model.toolbox.DataStream;

@Controller
public class CacheServiceImpl implements CacheService {

    private static final FileFilter DESCRIPTION_FILE_FILTER = new SuffixFileFilter(".description");
    private static final String DESCRIPTION_SPLITTER = "---";

    @Value("${cache.path}")
    private File cachePath;

    private final Map<String, CacheResource> cache;
    private final ObjectMapper objectMapper;

    public CacheServiceImpl() {
        this.cache = new HashMap<>();
        this.objectMapper = new ObjectMapper();
        this.objectMapper.setVisibility(this.objectMapper.getSerializationConfig().getDefaultVisibilityChecker()
                .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
                .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withCreatorVisibility(JsonAutoDetect.Visibility.NONE));
    }

    @PostConstruct
    public void init() {
        if (!this.cachePath.exists()) {
            throw new IllegalStateException("Cache path [" + this.cachePath.getAbsolutePath() + "] does not exist");
        }
        for (File descriptionFile : this.cachePath.listFiles(DESCRIPTION_FILE_FILTER)) {
            try {
                String descriptionContents = IOUtils.toString(new FileInputStream(descriptionFile), StandardCharsets.UTF_8);
                BufferedReader reader = new BufferedReader(new StringReader(descriptionContents));
                String cacheResourceTypeString = reader.readLine();
                Class<? extends CacheResource> cacheResourceClass = ReflectionUtil.classForNameNoCheckedException(cacheResourceTypeString);
                String splitter = reader.readLine();
                // Sanity check
                if (!splitter.equals(DESCRIPTION_SPLITTER)) {
                    throw new IllegalStateException("Illegal splitter: [" + splitter + "]");
                }
                String jsonContents = IOUtils.toString(reader);
                CacheResource resource = this.objectMapper.readValue(jsonContents, cacheResourceClass);
                this.cache.put(resource.getId(), resource);
            } catch (IOException e) {
                throw new IllegalStateException("IOException during cache loading", e);
            }
        }
    }

    @Override
    public int getCacheSize() {
        return this.cache.size();
    }

    @Override
    public boolean hasResource(String id) {
        return this.cache.containsKey(id);
    }

    @Override
    public CacheResource getResource(String id) {
        if (!this.hasResource(id)) {
            throw new IllegalStateException("Resource with id [" + id + "] not found");
        }
        return this.cache.get(id);
    }

    private File contructDescriptionFile(CacheResource resource) {
        return new File(this.cachePath.getAbsolutePath() + "/" + resource.getSanitizedId() + ".description");
    }

    private File constructCacheFile(String id) {
        return this.constructCacheFile(this.getResource(id));
    }

    private File constructCacheFile(CacheResource resource) {
        return new File(this.cachePath.getAbsolutePath() + "/" + resource.getFilePath());
    }

    @Override
    public File getCacheFile(String id) {
        File cacheFile = this.constructCacheFile(id);
        if (!cacheFile.exists()) {
            throw new IllegalStateException("Cache file: [" + cacheFile.getAbsolutePath() + "] does not exist");
        }
        return cacheFile;
    }

    @Override
    public DataStream getCacheDataStream(String id) {
        try {
            File contentFile = this.getCacheFile(id);
            CacheResource cacheResource = this.getResource(id);
            return new DataStream(new FileInputStream(contentFile), cacheResource.mimeType, contentFile.length());
        } catch (IOException e) {
            throw new IllegalStateException("IOException while getting cache stream", e);
        }
    }

    @Override
    public void storeResource(CacheResource resource, DataStream data) {
        File cacheFile = this.constructCacheFile(resource);
        if (cacheFile.exists()) {
            throw new IllegalStateException("Cache file: [" + cacheFile.getAbsolutePath() + "] already exists");
        }
        File descriptionFile = this.contructDescriptionFile(resource);
        if (descriptionFile.exists()) {
            throw new IllegalStateException("Description file: [" + descriptionFile.getAbsolutePath() + "] already exists");
        }
        try {
            IOUtils.copy(data.getInputStream(), new FileOutputStream(cacheFile));
            OutputStream outputStream = new FileOutputStream(descriptionFile);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream));
            writer.write(resource.getClass().getName() + "\n");
            writer.write(DESCRIPTION_SPLITTER + "\n");
            writer.flush();
            this.objectMapper.writerWithDefaultPrettyPrinter().writeValue(outputStream, resource);
        } catch (IOException e) {
            throw new IllegalStateException("IOException while stroing new entry in cache", e);
        }
        this.cache.put(resource.getId(), resource);
    }

}
