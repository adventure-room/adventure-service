package com.programyourhome.iotadventure.server.scripts;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class ScriptRunner {

    /**
     * Main method to start a script directly, mainly for testing purposes.
     */
    public static void main(String[] args) {
        if (args.length % 2 == 0) {
            System.out.println("Usage: ScriptRunner <script-name> (<script-arg-name> <script-arg-value>)*");
        }
        Map<String, String> arguments = new HashMap<>();
        for (int i = 1; i < args.length; i += 2) {
            arguments.put(args[i], args[i + 1]);
        }
        new ScriptRunner().runScript(args[0], arguments);
    }

    public void runScript(String name, Map<String, String> arguments) {
        try {
            Class.forName(name).getMethod("runFromJava", Map.class).invoke(null, arguments);
        } catch (Exception e) {
            throw new IllegalStateException("Exception while trying to run script " + name, e);
        }
    }

}
