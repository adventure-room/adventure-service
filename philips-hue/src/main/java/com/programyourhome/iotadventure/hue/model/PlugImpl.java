package com.programyourhome.iotadventure.hue.model;

import com.programyourhome.iotadventure.common.model.PyhImpl;
import com.programyourhome.iotadventure.hue.model.PyhLight;
import com.programyourhome.iotadventure.hue.model.PyhPlug;

public class PlugImpl extends PyhImpl implements PyhPlug {

    private final String name;

    public PlugImpl(final PyhLight light) {
        this.name = light.getName();
    }

    @Override
    public String getName() {
        return this.name;
    }

}
