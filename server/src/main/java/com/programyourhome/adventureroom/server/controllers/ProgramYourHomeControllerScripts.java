package com.programyourhome.adventureroom.server.controllers;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("scripts")
public class ProgramYourHomeControllerScripts {

    @RequestMapping("{name}")
    public String runScript(@PathVariable("name") final String name) {
        // TODO: trigger script with that name/id
        return "TODO";
    }

}
