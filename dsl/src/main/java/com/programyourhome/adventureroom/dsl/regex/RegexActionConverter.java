package com.programyourhome.adventureroom.dsl.regex;

import java.util.HashMap;
import java.util.Map;

import com.programyourhome.adventureroom.model.Adventure;
import com.programyourhome.adventureroom.model.script.action.Action;

public interface RegexActionConverter<A extends Action> {

    public static final RegexVariable CHARACTER_ID = new RegexVariable("characterId", Type.ID);
    public static final RegexVariable RESOURCE_ID = new RegexVariable("resourceId", Type.ID);
    public static final RegexVariable RESOURCE_IDS = new RegexVariable("resourceIdList", Type.ID_LIST);
    public static final RegexVariable INTEGER = new RegexVariable("integer", Type.INTEGER);
    public static final RegexVariable DOUBLE = new RegexVariable("double", Type.DOUBLE);
    public static final RegexVariable NAME = new RegexVariable("name", Type.NAME);
    public static final RegexVariable TEXT = new RegexVariable("text", Type.TEXT);

    // Some often used regex names
    public static final String SINGLE = "single";
    public static final String MULTIPLE = "multiple";
    public static final String ALL = "all";
    public static final String DEFAULT = "default";

    public Map<String, String> getRegexMap();

    public default Map<String, String> createRegexes(String... regexes) {
        if (regexes.length % 2 != 0) {
            throw new IllegalArgumentException("Supplied regexes should be of even length");
        }
        Map<String, String> regexMap = new HashMap<>();
        for (int i = 0; i < regexes.length; i += 2) {
            String regexName = regexes[i];
            String regexValue = regexes[i + 1];
            regexMap.put(regexName, regexValue);
        }
        return regexMap;
    }

    public A convert(MatchResult matchResult, Adventure adventure);

}
