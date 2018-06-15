package com.programyourhome.adventureroom.dsl.regex;

import com.programyourhome.adventureroom.model.Adventure;
import com.programyourhome.adventureroom.model.script.action.Action;

public interface RegexActionConverter<A extends Action> {

    public A convert(MatchResult matchResult, Adventure adventure);

}
