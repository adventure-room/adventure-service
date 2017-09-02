package com.programyourhome.iotadventure.server.controllers;

import javax.inject.Inject;

import com.programyourhome.iotadventure.common.controller.AbstractProgramYourHomeController;
import com.programyourhome.iotadventure.server.config.ServerConfigHolder;
import com.programyourhome.server.config.model.ServerConfig;

public abstract class AbstractProgramYourHomeServerController extends AbstractProgramYourHomeController {

    @Inject
    private ServerConfigHolder configHolder;

    protected ServerConfig getServerConfig() {
        return this.configHolder.getConfig();
    }

}
