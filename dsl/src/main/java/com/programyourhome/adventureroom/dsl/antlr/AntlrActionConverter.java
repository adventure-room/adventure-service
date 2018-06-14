package com.programyourhome.adventureroom.dsl.antlr;

import org.antlr.v4.runtime.ParserRuleContext;

import com.programyourhome.adventureroom.model.script.action.Action;

public interface AntlrActionConverter<C extends ParserRuleContext, A extends Action> {

    public A convert(C context);

}
