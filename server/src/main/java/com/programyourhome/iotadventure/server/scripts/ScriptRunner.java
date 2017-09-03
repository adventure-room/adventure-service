package com.programyourhome.iotadventure.server.scripts;

import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class ScriptRunner {

	public void runScript(String name, Map<String, String> arguments) {
		try {
			Class.forName(name).getMethod("runFromJava", Map.class).invoke(null, arguments);
		} catch (Exception e) {
			throw new IllegalStateException("Exception while trying to run script " + name, e);
		}
	}

}
