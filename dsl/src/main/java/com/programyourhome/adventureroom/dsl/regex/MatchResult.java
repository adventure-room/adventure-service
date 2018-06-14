package com.programyourhome.adventureroom.dsl.regex;

import java.util.regex.Matcher;

public class MatchResult {

    private final Matcher matcher;

    public MatchResult(Matcher matcher) {
        if (!matcher.matches()) {
            throw new IllegalArgumentException("The provided matcher must match");
        }
        this.matcher = matcher;
    }

    public String getFullText() {
        return this.matcher.group(0);
    }

    public String getValue(String parameterName) {
        return this.matcher.group(parameterName);
    }

}
