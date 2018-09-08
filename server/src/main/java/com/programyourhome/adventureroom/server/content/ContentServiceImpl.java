package com.programyourhome.adventureroom.server.content;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.programyourhome.adventureroom.model.toolbox.ContentCategory;
import com.programyourhome.adventureroom.model.toolbox.ContentService;
import com.programyourhome.adventureroom.model.toolbox.DataStream;

@Component
public class ContentServiceImpl implements ContentService {

    @Value("${content.path}")
    private File contentPath;

    private final Map<String, String> extensionToContentType;

    public ContentServiceImpl() {
        this.extensionToContentType = new HashMap<>();
        this.extensionToContentType.put("wav", "audio/wav");
    }

    @Override
    public DataStream getContent(ContentCategory category, String filename) {
        File contentFile = new File(this.contentPath.getAbsolutePath() + "/" + category.getFolderName() + "/" + filename);
        String extension = FilenameUtils.getExtension(filename);
        try {
            return new DataStream(new FileInputStream(contentFile), this.extensionToContentType.get(extension), contentFile.length());
        } catch (FileNotFoundException e) {
            throw new IllegalStateException("Exception while getting content file", e);
        }
    }

}
