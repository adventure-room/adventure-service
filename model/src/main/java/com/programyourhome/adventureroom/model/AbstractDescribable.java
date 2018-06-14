package com.programyourhome.adventureroom.model;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

public abstract class AbstractDescribable implements Describable {

    // TODO: builders! (with inheritance... see NS project)

    public String id;
    public String name;
    public String description;

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }

}
