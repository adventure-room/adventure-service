package com.programyourhome.adventureroom.server.toolbox;

import com.programyourhome.adventureroom.model.toolbox.ConversionService;

public class AdventureRoomConversionService implements ConversionService {

    private final org.springframework.core.convert.ConversionService springConversionService;

    public AdventureRoomConversionService(org.springframework.core.convert.ConversionService springConversionService) {
        this.springConversionService = springConversionService;
    }

    @Override
    public boolean canConvert(Class<?> sourceType, Class<?> targetType) {
        return this.springConversionService.canConvert(sourceType, targetType);
    }

    @Override
    public <T> T convert(Object source, Class<T> targetType) {
        return this.springConversionService.convert(source, targetType);
    }

}
