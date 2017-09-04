package com.programyourhome.iotadventure.environment.mock;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.programyourhome.iotadventure.hue.PhilipsHue;
import com.programyourhome.iotadventure.hue.model.LightType;
import com.programyourhome.iotadventure.hue.model.PyhColorRGB;
import com.programyourhome.iotadventure.hue.model.PyhLight;

@Configuration
public class PhilipsHueMock extends PyhMock {

    private final Map<Integer, Boolean> lightStates;

    public PhilipsHueMock() {
        this.lightStates = new HashMap<>();
        this.lightStates.put(1, false);
    }

    @Bean
    public PhilipsHue createPhilipsHueMock() {
        final PhilipsHue philipsHueMock = this.createMock(PhilipsHue.class);

        // TODO: abstract out mock creation code.
        // Mock light(s)
        final PyhLight light = Mockito.mock(PyhLight.class);
        Mockito.when(light.getId()).thenReturn(1);
        Mockito.when(light.getName()).thenReturn("Mock name");
        Mockito.when(light.getType()).thenReturn(LightType.HUE_FULL_COLOR_BULB);
        Mockito.when(light.getDim()).thenReturn(10000);
        // TODO: use mock for this, eg 'forward calling' (simple mock somehow creates stackoverflow when marchalling)
        Mockito.when(light.getColorRGB()).thenReturn(new PyhColorRGB() {
            @Override
            public int getRed() {
                return 255;
            }

            @Override
            public int getGreen() {
                return 0;
            }

            @Override
            public int getBlue() {
                return 0;
            }
        });
        Mockito.when(light.isOn()).thenAnswer(invocation -> this.lightStates.get(1));

        final PyhLight light2 = Mockito.mock(PyhLight.class);
        Mockito.when(light2.getId()).thenReturn(2);
        Mockito.when(light2.getName()).thenReturn("Mock name 2");
        Mockito.when(light2.getType()).thenReturn(LightType.HUE_LUX_BULB);
        Mockito.when(light2.isOn()).thenAnswer(invocation -> this.lightStates.get(2));

        Mockito.when(philipsHueMock.getLights()).thenReturn(Arrays.asList(light, light2));

        // Keep track of light state for more realistic behavior when mocking.
        Mockito.doAnswer(invocation -> this.lightStates.put(invocation.getArgument(0), true))
                .when(philipsHueMock).turnOnLight(ArgumentMatchers.anyInt());
        Mockito.doAnswer(invocation -> this.lightStates.put(invocation.getArgument(0), false))
                .when(philipsHueMock).turnOffLight(ArgumentMatchers.anyInt());

        return philipsHueMock;
    }
}
