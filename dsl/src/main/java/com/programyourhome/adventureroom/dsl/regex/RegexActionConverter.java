package com.programyourhome.adventureroom.dsl.regex;

import com.programyourhome.adventureroom.model.Adventure;
import com.programyourhome.adventureroom.model.script.action.Action;

public interface RegexActionConverter<A extends Action> {

    public static final RegexVariable CHARACTER_ID = new RegexVariable("characterId", Type.ID);
    public static final RegexVariable RESOURCE_ID = new RegexVariable("resourceId", Type.ID);
    public static final RegexVariable INTEGER = new RegexVariable("integer", Type.INTEGER);
    public static final RegexVariable DOUBLE = new RegexVariable("double", Type.DOUBLE);
    public static final RegexVariable NAME = new RegexVariable("name", Type.NAME);
    public static final RegexVariable TEXT = new RegexVariable("text", Type.TEXT);

    public String getRegexLine();

    public A convert(MatchResult matchResult, Adventure adventure);

}
