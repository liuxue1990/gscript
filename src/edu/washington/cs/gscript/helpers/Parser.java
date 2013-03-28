package edu.washington.cs.gscript.helpers;

import edu.washington.cs.gscript.models.ShapeSpec;
import edu.washington.cs.gscript.parser.GScriptLexer;
import edu.washington.cs.gscript.parser.GScriptParser;
import org.antlr.v4.runtime.*;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.LinkedList;

public class Parser {

    public static ArrayList<ShapeSpec> parseScript(String scriptText) {
        try {
            CharStream charStream = new ANTLRInputStream(new ByteArrayInputStream(scriptText.getBytes()));
            GScriptLexer lexer = new GScriptLexer(charStream);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            GScriptParser parser = new GScriptParser(tokens);

            BaseErrorListener silentErrorListener = new BaseErrorListener() {
                @Override
                public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
                }
            };

            lexer.removeErrorListeners();
            lexer.addErrorListener(silentErrorListener);

            parser.removeErrorListeners();
            parser.addErrorListener(silentErrorListener);

            parser.setErrorHandler(new BailErrorStrategy());

            LinkedList<ShapeSpec> shapeList = parser.prog().shapeList;
            return new ArrayList<ShapeSpec>(shapeList);

        } catch (Throwable e) {
        }

        return null;
    }

}
