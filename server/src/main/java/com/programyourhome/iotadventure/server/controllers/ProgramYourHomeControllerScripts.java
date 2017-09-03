package com.programyourhome.iotadventure.server.controllers;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.programyourhome.iotadventure.amazon.polly.AmazonPolly;
import com.programyourhome.iotadventure.common.response.ServiceResultSuccess;
import com.programyourhome.iotadventure.dsl.Dsl;
import com.programyourhome.iotadventure.events.ButtonEvent;
import com.programyourhome.iotadventure.events.Event;
import com.programyourhome.iotadventure.events.SwitchEvent;
import com.programyourhome.iotadventure.hue.PhilipsHue;
import com.programyourhome.iotadventure.server.scripts.ScriptRunner;
import com.programyourhome.iotadventure.services.Services;

@RestController
@RequestMapping("scripts")
public class ProgramYourHomeControllerScripts extends AbstractProgramYourHomeServerController {

	@Inject
	private ScriptRunner scriptRunner;

	@Inject
	private PhilipsHue philipsHue;

	@Inject
	private AmazonPolly amazonPolly;

	private Dsl dsl;
	
	public ProgramYourHomeControllerScripts() {
		this.dsl = Dsl.instance();
		dsl.subscribeListener(event -> System.out.println("Java Event listener: " + event), Event.class);
		dsl.subscribeListener(event -> System.out.println("Java ButtonEvent: " + event), ButtonEvent.class);
		dsl.subscribeListener(event -> System.out.println("Java SwitchEvent: " + event), SwitchEvent.class);
	}
	
	@PostConstruct
	public void init() {
		Services.setPhilipsHue(philipsHue);
		Services.setAmazonPolly(amazonPolly);
	}

    @RequestMapping("events/button/{id}")
    public String buttonEvent(@PathVariable("id") final String id) {
    	dsl.post(new ButtonEvent(id));
    	return "Button pressed";
    }

    @RequestMapping("events/switch/{id}/{on}")
    public String switchEvent(@PathVariable("id") final String id, @PathVariable("on") final boolean on) {
    	dsl.post(new SwitchEvent(id, on));
    	return "Switch pressed";
    }

    @RequestMapping("{name}")
    public String test(@PathVariable("name") final String name) {
    	Map<String, String> arguments = new HashMap<>();
    	arguments.put("test-arg", "test-value");
    	scriptRunner.runScript(name, arguments);
    	return "success!";
    }

}
