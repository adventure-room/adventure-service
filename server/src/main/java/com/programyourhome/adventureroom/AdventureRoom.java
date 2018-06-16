package com.programyourhome.adventureroom;

import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.programyourhome.adventureroom.server.AdventureRoomServer;

@SpringBootApplication
public class AdventureRoom {

    public static void main(String[] args) {
        // Needed by Philips Hue API to function properly.
        System.setProperty("java.net.preferIPv4Stack", "true");
        AdventureRoomServer.startServer(AdventureRoom.class);
    }

}