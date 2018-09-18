package com.programyourhome.adventureroom.dsl.antlr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;

import com.programyourhome.adventureroom.model.Adventure;
import com.programyourhome.adventureroom.model.script.action.Action;
import com.programyourhome.adventureroom.model.util.ReflectionUtil;

@SuppressWarnings("unchecked")
public abstract class AbstractParseTreeAntlrActionConverter<C extends ParserRuleContext, A extends Action> implements AntlrActionConverter<C, A> {

    private final Map<Class<? extends ParserRuleContext>, BiConsumer<? extends ParserRuleContext, A>> ruleConsumers;

    public AbstractParseTreeAntlrActionConverter() {
        this.ruleConsumers = new HashMap<>();
    }

    protected <T extends ParserRuleContext> void registerRuleConverter(Class<T> parserRuleContextClass, BiConsumer<T, A> consumer) {
        this.ruleConsumers.put(parserRuleContextClass, consumer);
    }

    protected abstract void registerRuleConverters();

    /**
     * Implementation of convert that dispatches building the action
     * to the registered consumers of the different parser rule contexts in the parse tree.
     */
    @Override
    public A convert(C context, Adventure adventure) {
        // Let the subclass register it's rule converters.
        this.registerRuleConverters();

        // Create an empty action of the right type, based on the actual type parameter for Action of this converter.
        Class<A> actionClass = (Class<A>) ReflectionUtil.getGenericParameterWithSuperClass(this.getClass(), Action.class);
        A action = ReflectionUtil.callConstructorNoCheckedException(actionClass);

        List<ParseTree> parseTreeList = new ArrayList<>();
        this.treeToList(context, parseTreeList);
        // Walk over the parse tree list.
        for (ParseTree tree : parseTreeList) {
            if (tree instanceof ParserRuleContext) {
                // If there is a consumer registered for this parse rule context, call it.
                if (this.ruleConsumers.containsKey(tree.getClass())) {
                    BiConsumer<? extends ParserRuleContext, A> consumer = this.ruleConsumers.get(tree.getClass());
                    ((BiConsumer<ParserRuleContext, A>) consumer).accept((ParserRuleContext) tree, action);
                }
            }
        }
        return action;
    }

    /**
     * Recursively build a list of all parse tree nodes.
     */
    private void treeToList(ParseTree parent, List<ParseTree> parseTreeList) {
        parseTreeList.add(parent);
        for (int i = 0; i < parent.getChildCount(); i++) {
            ParseTree child = parent.getChild(i);
            this.treeToList(child, parseTreeList);
        }
    }

}
