package com.programyourhome.adventureroom.dsl.regex;

import java.util.Collection;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.programyourhome.adventureroom.model.Adventure;
import com.programyourhome.adventureroom.model.module.AdventureModule;
import com.programyourhome.adventureroom.model.script.action.Action;

public abstract class AbstractRegexDslAdventureModule implements AdventureModule {

    @Override
    public Optional<Action> parseForAction(String input, Adventure adventure) {
        // TODO: java-8-yfy
        for (RegexActionConverter<?> converter : this.getRegexActionConverters()) {
            Pattern pattern = Pattern.compile(converter.getRegexLine());
            Matcher matcher = pattern.matcher(input);
            if (matcher.matches()) {
                try {
                    return Optional.of(converter.convert(new MatchResult(matcher), adventure));
                } catch (Exception e) {
                    // TODO: log warning
                    e.printStackTrace();
                }
            }
        }
        return Optional.empty();
    }

    public RegexVariable var(String name, Type type) {
        return new RegexVariable(name, type);
    }

    protected abstract Collection<RegexActionConverter<?>> getRegexActionConverters();

}
