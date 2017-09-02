package com.programyourhome.iotadventure.environment.mock;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.springframework.stereotype.Component;

import com.programyourhome.iotadventure.common.serialize.SerializationSettings;
import com.programyourhome.iotadventure.hue.model.PyhLight;

@Component
public class MockingConfiguration {

    @Inject
    private SerializationSettings serializationSettings;

    @PostConstruct
    public void provideSerializationSettings() {
        this.serializationSettings.fixSerializationScope(PyhLight.class);
    }

}
