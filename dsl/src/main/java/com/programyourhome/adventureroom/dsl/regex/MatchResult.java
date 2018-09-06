package com.programyourhome.adventureroom.dsl.regex;

import java.util.Optional;
import java.util.regex.Matcher;

public class MatchResult {

    private final String regexName;
    private final Matcher matcher;

    public MatchResult(String regexName, Matcher matcher) {
        this.regexName = regexName;
        if (!matcher.matches()) {
            throw new IllegalArgumentException("The provided matcher must match");
        }
        this.matcher = matcher;
    }

    public boolean is(String regexName) {
        return this.regexName.equals(regexName);
    }

    public String getRegexName() {
        return this.regexName;
    }

    public String getFullText() {
        return this.matcher.group(0);
    }

    public String getValue(RegexVariable regexVariable) {
        return this.getValue(regexVariable.name);
    }

    public Optional<String> getOptionalValue(RegexVariable regexVariable) {
        return Optional.ofNullable(this.getValue(regexVariable));
    }

    public String getValue(String parameterName) {
        return this.matcher.group(parameterName);
    }

    public Optional<String> getOptionalValue(String parameterName) {
        return Optional.ofNullable(this.getValue(parameterName));
    }

}
