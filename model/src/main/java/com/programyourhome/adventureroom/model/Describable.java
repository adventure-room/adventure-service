package com.programyourhome.adventureroom.model;

public interface Describable {

    public String getId();

    public default String getName() {
        return this.getId();
    }

    public default String getDescription() {
        return this.getName();
    }

}
