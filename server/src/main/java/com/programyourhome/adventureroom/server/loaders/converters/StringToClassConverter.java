package com.programyourhome.adventureroom.server.loaders.converters;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringToClassConverter implements Converter<String, Class<?>> {

    @Override
    public Class<?> convert(String source) {
        try {
            return Class.forName(source);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Class: '" + source + "' not found", e);
        }
    }

}
