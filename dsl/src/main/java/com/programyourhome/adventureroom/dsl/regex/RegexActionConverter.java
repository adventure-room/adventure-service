package com.programyourhome.adventureroom.dsl.regex;

import com.programyourhome.adventureroom.model.script.action.Action;

public interface RegexActionConverter<A extends Action> {

    public A convert(MatchResult matchResult);

}
