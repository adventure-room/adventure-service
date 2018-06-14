package com.programyourhome.adventureroom.dsl.antlr;

public class Main {

    // TODO: set the lexer and parser to fail upon errors!

    // public static void main(String[] args) throws Exception {
    // // Reading the DSL script
    // InputStream is = ClassLoader.getSystemResourceAsStream("adventure.adv");
    //
    // // Loading the DSL script into the ANTLR stream.
    // CharStream cs = CharStreams.fromStream(is);
    //
    // // Passing the input to the lexer to create tokens
    // AdventureLexer lexer = new AdventureLexer(cs);
    //
    // lexer.removeErrorListeners();
    // lexer.addErrorListener(new ANTLRErrorListener() {
    //
    // @Override
    // public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
    // System.out.println("Lexer syntax error!");
    // System.out.println(msg);
    // System.out.println("line: " + line);
    // System.out.println("pos: " + charPositionInLine);
    // throw new ParseCancellationException("Whuut!");
    // }
    //
    // @Override
    // public void reportContextSensitivity(Parser recognizer, DFA dfa, int startIndex, int stopIndex, int prediction, ATNConfigSet configs) {
    // System.out.println("Lexer CS!");
    // }
    //
    // @Override
    // public void reportAttemptingFullContext(Parser recognizer, DFA dfa, int startIndex, int stopIndex, BitSet conflictingAlts, ATNConfigSet configs) {
    // System.out.println("Lexer RAFC!");
    // }
    //
    // @Override
    // public void reportAmbiguity(Parser recognizer, DFA dfa, int startIndex, int stopIndex, boolean exact, BitSet ambigAlts, ATNConfigSet configs) {
    // System.out.println("Lexer Am!");
    // }
    // });
    //
    // CommonTokenStream tokens = new CommonTokenStream(lexer);
    //
    // // Passing the tokens to the parser to create the parse tree.
    // AdventureParser parser = new AdventureParser(tokens);
    //
    // // Adding the listener to facilitate walking through parse tree.
    // MyAdventureBaseListener listener = new MyAdventureBaseListener();
    // parser.addParseListener(listener);
    // // parser.addParseListener(new ParseTreeListener() {
    // //
    // // @Override
    // // public void visitTerminal(TerminalNode node) {
    // // System.out.println("Visit terminal: " + node);
    // // }
    // //
    // // @Override
    // // public void visitErrorNode(ErrorNode node) {
    // // System.out.println("Visit error: " + node);
    // // }
    // //
    // // @Override
    // // public void enterEveryRule(ParserRuleContext ctx) {
    // // System.out.println("Enter every rule: " + ctx.getChildCount());
    // // }
    // //
    // // @Override
    // // public void exitEveryRule(ParserRuleContext ctx) {
    // // System.out.println("Exit every rule: " + ctx.getChild(0).getText());
    // // }
    // //
    // // });
    //
    // parser.setErrorHandler(new BailErrorStrategy());
    //
    // parser.removeErrorListeners();
    // parser.addErrorListener(new ANTLRErrorListener() {
    //
    // @Override
    // public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
    // System.out.println("Syntax error!");
    // System.out.println(msg);
    // throw new ParseCancellationException("Whaat!");
    // }
    //
    // @Override
    // public void reportContextSensitivity(Parser recognizer, DFA dfa, int startIndex, int stopIndex, int prediction, ATNConfigSet configs) {
    // System.out.println("Context sensitivity!");
    // }
    //
    // @Override
    // public void reportAttemptingFullContext(Parser recognizer, DFA dfa, int startIndex, int stopIndex, BitSet conflictingAlts, ATNConfigSet configs) {
    // System.out.println("Attempting full context!");
    // }
    //
    // @Override
    // public void reportAmbiguity(Parser recognizer, DFA dfa, int startIndex, int stopIndex, boolean exact, BitSet ambigAlts, ATNConfigSet configs) {
    // System.out.println("Ambiguity!");
    // }
    // });
    //
    // // TODO: Make dynamic, extend Tool class en inhaken op Tool.parseGrammer(String filename).
    // // Modules kunnen vervolgens code genereren dmv dynamic invocation (mvn exec java plugin?)
    // // Is dat echt nodig als je altijd een 'samenkomst' project maakt die alle modules refereert die gebruikt moeten worden?
    // // Dan bouw je daar 1 keer alles samen en heb je een tailor made grammer, parser en listener op basis van de geinclude modules.
    // // Liefst op zo'n manier dat de base grammer zonder extensies en de losse extensies allemaal los kunnen bouwen met de reguliere
    // // Maven plugin zodat er code beschikbaar is om op verder te bouwen.
    //
    // // invoking the parser.
    // parser.adventure();
    //
    // // System.out.println("Done parsing, now executing.");
    // // for (Action action : listener.adventure.scripts.values().iterator().next().actions) {
    // // executeAction(action);
    // // }
    // System.out.println("Done!");
    // }
    //
    // // @SuppressWarnings("unchecked")
    // // private static <T extends Action> void executeAction(T action) throws Exception {
    // // System.out.println("About to execute action: " + action);
    // // Class<? extends ActionExecutor<T>> clazz = (Class<? extends ActionExecutor<T>>) Class.forName(action.getClass().getName() + "Executor");
    // // clazz.newInstance().execute(action);
    // // }
    // }
    //
    ///// **
    //// * Listener used for walking through the parse tree.
    //// */
    //// class MyAdventureBaseListener extends AdventureBaseListener {
    ////
    //// public final Adventure adventure = new Adventure();
    ////
    //// private Script currentScript;
    ////
    //// @Override
    //// public void enterScript(ScriptContext ctx) {
    //// this.currentScript = new Script();
    //// this.adventure.scripts.put("dummy", this.currentScript);
    //// }
    ////
    //// @Override
    //// public void exitDebugAction(DebugActionContext ctx) {
    //// DebugAction debugAction = new DebugAction();
    //// debugAction.message = ctx.NAME().getText();
    //// this.currentScript.actions.add(debugAction);
    //// }
    ////
    //// @Override
    //// public void exitWaitAction(WaitActionContext ctx) {
    //// WaitAction waitAction = new WaitAction();
    //// waitAction.duration = Duration.ofMillis(Integer.parseInt(ctx.NUM().getText()));
    //// this.currentScript.actions.add(waitAction);
    //// }
    ////
    //// // @Override
    //// // public void exitCharacter(CharacterContext ctx) {
    //// // Character character = new Character();
    //// // character.name = ctx.NAME().getText();
    //// // this.adventure.characters.put(character.name, character);
    //// // }
    ////
    //// // TODO: convert this to converter mechanism too?
    //// @Override
    //// public void exitExternalModuleAction(ExternalModuleActionContext ctx) {
    //// try {
    //// this.currentScript.actions.add(this.convertAction(ctx));
    //// } catch (Exception e) {
    //// e.printStackTrace();
    //// }
    //// }
    ////
    //// @SuppressWarnings("unchecked")
    //// private <A extends Action, C extends ExternalModuleActionContext> A convertAction(C ctx) throws Exception {
    //// ModuleActionConverter<A, C> converter = (ModuleActionConverter<A, C>) Class
    //// // TODO: dynamically
    //// .forName("com.programyourhome.iotadventure.model.script.action.HueActionConverter").newInstance();
    //// return converter.convert(ctx);
    //// }

}