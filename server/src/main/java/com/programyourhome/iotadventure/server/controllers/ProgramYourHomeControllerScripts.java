package com.programyourhome.iotadventure.server.controllers;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.programyourhome.iotadventure.amazon.polly.AmazonPolly;
import com.programyourhome.iotadventure.dsl.Dsl;
import com.programyourhome.iotadventure.dsl.events.ButtonEvent;
import com.programyourhome.iotadventure.dsl.events.Event;
import com.programyourhome.iotadventure.dsl.events.SwitchEvent;
import com.programyourhome.iotadventure.dsl.services.Services;
import com.programyourhome.iotadventure.hue.PhilipsHue;
import com.programyourhome.iotadventure.server.scripts.ScriptRunner;

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
        this.dsl.subscribeFromJava(event -> System.out.println("Java Event listener: " + event), Event.class);
        this.dsl.subscribeFromJava(event -> System.out.println("Java ButtonEvent: " + event), ButtonEvent.class);
        this.dsl.subscribeFromJava(event -> System.out.println("Java SwitchEvent: " + event), SwitchEvent.class);
    }

    @PostConstruct
    public void init() {
        Services.setPhilipsHue(this.philipsHue);
        Services.setAmazonPolly(this.amazonPolly);
    }

    @RequestMapping("events/button/{id}")
    public String buttonEvent(@PathVariable("id") final String id) {
        this.dsl.post(new ButtonEvent(id));
        return "Button pressed";
    }

    @RequestMapping("events/switch/{id}/{on}")
    public String switchEvent(@PathVariable("id") final String id, @PathVariable("on") final boolean on) {
        this.dsl.post(new SwitchEvent(id, on));
        return "Switch pressed";
    }

    @RequestMapping("{name}")
    public String test(@PathVariable("name") final String name) {
        Map<String, String> arguments = new HashMap<>();
        arguments.put("test-arg", "test-value");
        this.scriptRunner.runScript(name, arguments);
        return "success!";
    }

}
