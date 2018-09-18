package com.programyourhome.adventureroom.dsl.antlr;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import org.antlr.v4.runtime.ParserRuleContext;

import com.programyourhome.adventureroom.model.script.action.Action;
import com.programyourhome.adventureroom.model.util.ReflectionUtil;

public abstract class AbstractReflectiveParseTreeAntlrActionConverter<C extends ParserRuleContext, A extends Action>
        extends AbstractParseTreeAntlrActionConverter<C, A> {

    @Override
    protected void registerRuleConverters(ParserRuleContext rootContext) {
        List<ParserRuleContext> parseRuleContextList = new ArrayList<>();
        this.treeToList(rootContext, parseRuleContextList);
        for (ParserRuleContext context : parseRuleContextList) {
            String ruleName = context.getClass().getSimpleName().replace("Context", "");
            String methodName = "parse" + ruleName;
            if (ReflectionUtil.hasMethod(this, methodName)) {
                this.registerRuleConverter(context.getClass(), new BiConsumer<ParserRuleContext, A>() {
                    @Override
                    public void accept(ParserRuleContext ruleContext, A action) {
                        ReflectionUtil.callVoidMethodNoCheckedException(this, methodName, ruleContext, action);
                    }
                });
            }
        }
    }

}
