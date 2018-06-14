package com.programyourhome.adventureroom.server.controllers;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("adventures")
public class ProgramYourHomeControllerAdventures {

    @RequestMapping("{name}")
    public String runAdventure(@PathVariable("name") final String name) {
        // TODO: trigger adventure with that name/id
        return "TODO";
    }

}
