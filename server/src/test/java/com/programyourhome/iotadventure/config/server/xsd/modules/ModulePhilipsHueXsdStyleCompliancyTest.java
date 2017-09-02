package com.programyourhome.iotadventure.config.server.xsd.modules;

import com.programyourhome.iotadventure.common.config.xsd.XsdStyleCompliancyTest;

public class ModulePhilipsHueXsdStyleCompliancyTest extends XsdStyleCompliancyTest {

    @Override
    protected String getXsdLocation() {
        return "/com/programyourhome/config/server/xsd/modules/program-your-home-config-philips-hue.xsd";
    }

}
