package com.programyourhome.adventureroom.model.resource;

public abstract class AbstractExternalResource<O> implements ExternalResource<O> {

    private final O wrappedObject;

    public AbstractExternalResource(O wrappedObject) {
        this.wrappedObject = wrappedObject;
    }

    @Override
    public O getWrappedObject() {
        return this.wrappedObject;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Class<O> getWrappedObjectClass() {
        return (Class<O>) this.wrappedObject.getClass();
    }

}
