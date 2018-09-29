package com.programyourhome.adventureroom.dsl.regex;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.programyourhome.adventureroom.model.Adventure;
import com.programyourhome.adventureroom.model.script.action.Action;

public interface RegexActionConverter<A extends Action> {

    public static final RegexVariable CHARACTER_ID = new RegexVariable("characterId", Type.ID);
    public static final RegexVariable RESOURCE_ID = new RegexVariable("resourceId", Type.ID);
    public static final RegexVariable RESOURCE_IDS = new RegexVariable("resourceIdList", Type.ID_LIST);
    public static final RegexVariable INTEGER = new RegexVariable("integer", Type.INTEGER);
    public static final RegexVariable DOUBLE = new RegexVariable("double", Type.DOUBLE);
    public static final RegexVariable NAME = new RegexVariable("name", Type.NAME);
    public static final RegexVariable NAME_WITH_DASHES = new RegexVariable("propertyName", Type.NAME_WITH_DASHES);
    public static final RegexVariable WORD = new RegexVariable("word", Type.WORD);
    public static final RegexVariable TEXT = new RegexVariable("text", Type.TEXT);
    public static final RegexVariable FILENAME = new RegexVariable("filename", Type.FILENAME);
    public static final RegexVariable DURATION = new RegexVariable("duration", Type.DURATION);
    public static final RegexVariable LOCATION = new RegexVariable("location", Type.LOCATION);
    public static final RegexVariable LOCATION_PATH = new RegexVariable("locationPath", Type.LOCATION_PATH);
    public static final RegexVariable REST_OF_THE_LINE = new RegexVariable("rest", Type.EVERYTHING);

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

    public default String either(String... regexes) {
        return "(" + StringUtils.join(regexes, '|') + ")";
    }

    public default String optional(String regex) {
        return "(" + regex + ")?";
    }

    public default String asVariable(String name, String regex) {
        return new RegexVariable(name, regex).toString();
    }

    public default Duration parseDuration(String durationMatch) {
        String[] lengthAndName = durationMatch.split(" ");
        long amount = Long.parseLong(lengthAndName[0]);
        ChronoUnit chronoUnit = ChronoUnit.valueOf(lengthAndName[1].toUpperCase());
        return Duration.of(amount, chronoUnit);
    }

    public A convert(MatchResult matchResult, Adventure adventure);

}
