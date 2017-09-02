package com.programyourhome.iotadventure.config.server.xsd;

import com.programyourhome.iotadventure.common.config.xsd.XsdStyleCompliancyTest;

public class ServerXsdStyleCompliancyTest extends XsdStyleCompliancyTest {

    @Override
    protected String getXsdLocation() {
        return "/com/programyourhome/config/server/xsd/program-your-home-config-server.xsd";
    }

}
