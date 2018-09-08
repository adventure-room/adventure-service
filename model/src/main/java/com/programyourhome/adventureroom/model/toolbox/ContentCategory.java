package com.programyourhome.adventureroom.model.toolbox;

public enum ContentCategory {

    AUDIO("audio"),
    VIDEO("video"),
    IMAGES("images"),
    OTHER("other");

    private String folderName;

    private ContentCategory(String folderName) {
        this.folderName = folderName;
    }

    public String getFolderName() {
        return this.folderName;
    }

}
