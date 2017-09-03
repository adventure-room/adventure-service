package com.programyourhome.iotadventure.server.config;

import org.springframework.stereotype.Component;

import com.programyourhome.iotadventure.common.config.ConfigLoader;
import com.programyourhome.iotadventure.common.config.ConfigurationException;
import com.programyourhome.server.config.model.ServerConfig;

@Component
public class ServerConfigLoader extends ConfigLoader<ServerConfig> {

    private static final String CONFIG_BASE_PATH = "/com/programyourhome/config/server/";
    private static final String XSD_BASE_PATH = CONFIG_BASE_PATH + "xsd/";
    private static final String XML_BASE_PATH = CONFIG_BASE_PATH + "xml/";
    private static final String XSD_FILENAME = "program-your-home-config-server.xsd";
    private static final String XML_FILENAME = "program-your-home-config-server.xml";

    @Override
    protected Class<ServerConfig> getConfigType() {
        return ServerConfig.class;
    }

    @Override
    protected String getPathToXsd() {
        return XSD_BASE_PATH + XSD_FILENAME;
    }

    @Override
    protected String getPathToXml() {
        return XML_BASE_PATH + XML_FILENAME;
    }
    
    @Override
    protected void validateConfig(ServerConfig config) throws ConfigurationException {
    	
    }

}
