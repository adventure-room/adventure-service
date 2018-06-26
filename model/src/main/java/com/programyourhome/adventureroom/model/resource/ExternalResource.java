package com.programyourhome.adventureroom.model.resource;

public interface ExternalResource<O> extends Resource {

    public Class<O> getWrappedObjectClass();

    public O getWrappedObject();

}
