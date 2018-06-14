package com.programyourhome.adventureroom.server.controllers;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.programyourhome.adventureroom.server.ProgramYourHomeServer;

@RestController
@RequestMapping("meta")
public class ProgramYourHomeControllerMeta {

    // TODO: more specific health/status check info and other meta service (logs / data / etc)

    /**
     * This provides an easy way the see if the REST service is reachable.
     *
     * @return the text string 'pong'
     */
    @RequestMapping("status/ping")
    public String pingServer() {
        return "pong";
    }

    /**
     * Feature: shutdown the server with a REST request.
     */
    @RequestMapping("server/shutdown")
    public void shutdownServer() {
        ProgramYourHomeServer.stopServer();
    }

    @RequestMapping("test/{var}")
    public String test(@PathVariable("var") Class<?> clazz) {
        return "class: " + clazz;
    }

}
