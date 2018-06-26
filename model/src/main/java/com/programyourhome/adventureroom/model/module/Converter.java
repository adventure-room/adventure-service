package com.programyourhome.adventureroom.model.module;

public interface Converter<From, To> {

    public Class<From> getFromClass();

    public Class<To> getToClass();

    public To convert(From from);

}
