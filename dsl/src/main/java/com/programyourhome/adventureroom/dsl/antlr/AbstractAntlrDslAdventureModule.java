package com.programyourhome.adventureroom.dsl.antlr;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.TokenStream;

import com.programyourhome.adventureroom.dsl.util.ReflectionUtil;
import com.programyourhome.adventureroom.model.Adventure;
import com.programyourhome.adventureroom.model.module.AdventureModule;
import com.programyourhome.adventureroom.model.script.action.Action;

public abstract class AbstractAntlrDslAdventureModule implements AdventureModule {

    public static final String ADVENTURE_ROOM_PACKAGE_NAME = "com.programyourhome.adventureroom";
    public static final String DSL_PACKAGE_NAME = "dsl";
    public static final String CONVERTERS_PACKAGE_NAME = "converters";
    public static final String PARSER_RULE_NAME = "action";

    private final String dslId;
    private final String dslIdLowerCase;

    public AbstractAntlrDslAdventureModule(String dslId) {
        this.dslId = dslId;
        this.dslIdLowerCase = dslId.toLowerCase();
    }

    protected String getAntlrPackageName() {
        return ADVENTURE_ROOM_PACKAGE_NAME + "." + this.dslIdLowerCase + "." + DSL_PACKAGE_NAME;
    }

    protected String getLexerClassName() {
        return this.getLexerParserClassName("Lexer");
    }

    protected String getParserClassName() {
        return this.getLexerParserClassName("Parser");
    }

    private String getLexerParserClassName(String type) {
        return this.getAntlrPackageName() + "." + this.dslId + "AdventureModule" + type;
    }

    protected Class<? extends Lexer> getLexerClass() {
        return ReflectionUtil.classForNameNoCheckedException(this.getLexerClassName());
    }

    protected Class<? extends Parser> getParserClass() {
        return ReflectionUtil.classForNameNoCheckedException(this.getParserClassName());
    }

    protected Lexer getLexer(CharStream charStream) {
        return ReflectionUtil.callConstructorNoCheckedException(this.getLexerClass(), CharStream.class, charStream);
    }

    protected Parser getParser(CommonTokenStream tokenStream) {
        return ReflectionUtil.callConstructorNoCheckedException(this.getParserClass(), TokenStream.class, tokenStream);
    }

    protected AntlrActionConverter<ParserRuleContext, Action> getActionConverter(ParserRuleContext context) {
        String childContextClassSimpleName = context.getClass().getSimpleName();
        String actionConverterClassName = ADVENTURE_ROOM_PACKAGE_NAME + "." + this.dslIdLowerCase + "."
                + DSL_PACKAGE_NAME + "." + CONVERTERS_PACKAGE_NAME + "." + childContextClassSimpleName.replace("Context", "Converter");
        Class<? extends AntlrActionConverter<ParserRuleContext, Action>> actionConverterClass = ReflectionUtil
                .classForNameNoCheckedException(actionConverterClassName);
        return ReflectionUtil.callConstructorNoCheckedException(actionConverterClass);
    }

    protected <C extends ParserRuleContext> ParserRuleContext extractInnerContext(C outerContext) {
        if (outerContext.children.size() != 1 || !(outerContext.getChild(0) instanceof ParserRuleContext)) {
            throw new IllegalArgumentException(
                    "ParserRuleContext: '" + outerContext + "' not structured as expected. Should have 1 child of type ParserRuleContext");
        }
        return (ParserRuleContext) outerContext.getChild(0);
    }

    protected Action convertAction(ParserRuleContext outerContext, Adventure adventure) {
        ParserRuleContext innerContext = this.extractInnerContext(outerContext);
        AntlrActionConverter<ParserRuleContext, Action> actionConverter = this.getActionConverter(innerContext);
        return actionConverter.convert(innerContext, adventure);
    }

    protected String getParserRuleName() {
        return PARSER_RULE_NAME;
    }

    @Override
    public Optional<Action> parseForAction(String input, Adventure adventure) {
        // TODO: set the lexer and parser to fail upon errors!
        try {
            Lexer lexer = this.getLexer(CharStreams.fromString(input));
            Parser parser = this.getParser(new CommonTokenStream(lexer));
            Method parseMethod = parser.getClass().getMethod(this.getParserRuleName());
            ParserRuleContext context = (ParserRuleContext) parseMethod.invoke(parser);
            if (context.exception != null) {
                // TODO: config antlr to throw this
                throw context.exception;
            }
            return Optional.of(this.convertAction(context, adventure));
        } catch (RecognitionException | NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException e) {
            // TODO: debug logging of exception
            e.printStackTrace();
            return Optional.empty();
        }
    }

}
