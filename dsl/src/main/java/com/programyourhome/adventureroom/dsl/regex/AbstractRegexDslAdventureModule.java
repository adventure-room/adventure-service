package com.programyourhome.adventureroom.dsl.regex;

import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.programyourhome.adventureroom.model.module.AdventureModule;
import com.programyourhome.adventureroom.model.script.action.Action;

public abstract class AbstractRegexDslAdventureModule implements AdventureModule {

    @Override
    public Optional<Action> parseForAction(String input) {
        // TODO: java-8-yfy
        Map<Pattern, RegexActionConverter<?>> regexActionConverters = this.getRegexActionConverters();
        for (Pattern pattern : regexActionConverters.keySet()) {
            Matcher matcher = pattern.matcher(input);
            if (matcher.matches()) {
                try {
                    return Optional.of(regexActionConverters.get(pattern).convert(new MatchResult(matcher)));
                } catch (Exception e) {
                    // TODO: log warning
                    e.printStackTrace();
                }
            }
        }
        return Optional.empty();
    }

    protected abstract Map<Pattern, RegexActionConverter<?>> getRegexActionConverters();

}
