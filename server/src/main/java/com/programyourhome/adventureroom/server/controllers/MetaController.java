package com.programyourhome.adventureroom.server.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.programyourhome.adventureroom.server.AdventureRoomServer;

@RestController
@RequestMapping("meta")
public class MetaController {

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
        AdventureRoomServer.stopServer();
    }

}
