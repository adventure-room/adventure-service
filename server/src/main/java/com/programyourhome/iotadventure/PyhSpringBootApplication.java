package com.programyourhome.iotadventure;

import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.programyourhome.iotadventure.server.ProgramYourHomeServer;

@SpringBootApplication
public class PyhSpringBootApplication {

    public static void startApplication() {
        ProgramYourHomeServer.startServer(PyhSpringBootApplication.class);
    }

}