package com.programyourhome.adventureroom.dsl.antlr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import com.programyourhome.adventureroom.model.Adventure;
import com.programyourhome.adventureroom.model.script.action.Action;
import com.programyourhome.adventureroom.model.util.ReflectionUtil;

@SuppressWarnings("unchecked")
public abstract class AbstractParseTreeAntlrActionConverter<C extends ParserRuleContext, A extends Action> implements AntlrActionConverter<C, A> {

    private Adventure adventure;
    private final Map<Class<? extends ParserRuleContext>, BiConsumer<? extends ParserRuleContext, A>> ruleConsumers;

    public AbstractParseTreeAntlrActionConverter() {
        this.ruleConsumers = new HashMap<>();
    }

    public Adventure getAdventure() {
        return this.adventure;
    }

    protected void registerRuleConverter(Class<? extends ParserRuleContext> parserRuleContextClass, BiConsumer<ParserRuleContext, A> consumer) {
        this.ruleConsumers.put(parserRuleContextClass, consumer);
    }

    protected <T extends ParserRuleContext> void registerRuleConverterGeneric(Class<T> parserRuleContextClass, BiConsumer<T, A> consumer) {
        this.ruleConsumers.put(parserRuleContextClass, consumer);
    }

    protected abstract void registerRuleConverters(ParserRuleContext rootContext);

    /**
     * Implementation of convert that dispatches building the action
     * to the registered consumers of the different parser rule contexts in the parse tree.
     */
    @Override
    public A convert(C context, Adventure adventure) {
        this.adventure = adventure;
        // Let the subclass register it's rule converters.
        this.registerRuleConverters(context);

        // Create an empty action of the right type, based on the actual type parameter for Action of this converter.
        Class<A> actionClass = (Class<A>) ReflectionUtil.getGenericParameterWithSuperClass(this.getClass(), Action.class);
        A action = ReflectionUtil.callConstructorNoCheckedException(actionClass);

        List<ParserRuleContext> parseRuleContextList = new ArrayList<>();
        this.treeToList(context, parseRuleContextList);
        // Walk over the parse rule context list.
        for (ParserRuleContext rueContext : parseRuleContextList) {
            // If there is a consumer registered for this parse rule context, call it.
            if (this.ruleConsumers.containsKey(rueContext.getClass())) {
                BiConsumer<? extends ParserRuleContext, A> consumer = this.ruleConsumers.get(rueContext.getClass());
                ((BiConsumer<ParserRuleContext, A>) consumer).accept(rueContext, action);
            }
        }
        System.out.println(ReflectionToStringBuilder.toString(action));
        return action;
    }

    /**
     * Recursively build a list of all parse rule context nodes.
     */
    protected void treeToList(ParseTree parent, List<ParserRuleContext> parseTreeList) {
        if (parent instanceof ParserRuleContext) {
            parseTreeList.add((ParserRuleContext) parent);
        }
        for (int i = 0; i < parent.getChildCount(); i++) {
            ParseTree child = parent.getChild(i);
            this.treeToList(child, parseTreeList);
        }
    }

}
