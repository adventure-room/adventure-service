package com.programyourhome.adventureroom.dsl.antlr;

import java.util.Optional;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;

import com.programyourhome.adventureroom.model.Adventure;
import com.programyourhome.adventureroom.model.script.action.Action;

public interface AntlrActionConverter<C extends ParserRuleContext, A extends Action> {

    public A convert(C context, Adventure adventure);

    public default String toString(Token token) {
        return token.getText();
    }

    public default Optional<String> toOptionalString(Token token) {
        return Optional.of(toString(token));
    }

    public default int toInt(Token token) {
        return Integer.parseInt(token.getText());
    }

    public default Optional<Integer> toOptionalInt(Token token) {
        return Optional.of(toInt(token));
    }

    public default double toDouble(Token token) {
        return Double.parseDouble(token.getText());
    }

    public default Optional<Double> toOptionalDouble(Token token) {
        return Optional.of(toDouble(token));
    }

}
