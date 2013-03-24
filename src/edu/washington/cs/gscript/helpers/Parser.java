package edu.washington.cs.gscript.helpers;

import edu.washington.cs.gscript.models.Part;
import edu.washington.cs.gscript.models.ShapeSpec;
import edu.washington.cs.gscript.parser.GScriptLexer;
import edu.washington.cs.gscript.parser.GScriptParser;
import org.antlr.runtime.ANTLRStringStream;
import org.antlr.v4.runtime.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;

public class Parser {

//    public static ArrayList<Part> parseScript(String scriptText) {
//        ArrayList<Part> parts = new ArrayList<Part>();
//
//        for (String line : scriptText.split("\n")) {
//            if (line.trim().isEmpty()) {
//                continue;
//            }
//
//            line = line.trim();
//
//            Part part;
//            if (line.endsWith("*")) {
//                part = new Part(line.substring(0, line.length() - 1), true);
//            } else {
//                part = new Part(line, false);
//
//                if (line.contains(" ")) {
//                    return null;
//                }
//            }
//
//            parts.add(part);
//        }
//
//        return parts;
//    }

    public static ArrayList<ShapeSpec> parseScript(String scriptText) {
        try {
            CharStream charStream = new ANTLRInputStream(new ByteArrayInputStream(scriptText.getBytes()));
            GScriptLexer lexer = new GScriptLexer(charStream);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            GScriptParser parser = new GScriptParser(tokens);

            parser.setErrorHandler(new BailErrorStrategy());
            parser.removeErrorListeners();
            parser.addErrorListener(new BaseErrorListener() {
                @Override
                public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
                }
            });

            LinkedList<ShapeSpec> shapeList = parser.prog().shapeList;
            return new ArrayList<ShapeSpec>(shapeList);

        } catch (Throwable e) {
        }

        return null;
    }

    public static ArrayList<ShapeSpec> parseScript(String scriptText, String defaultName) {
        ArrayList<ShapeSpec> shapes = parseScript(scriptText);

        if (shapes == null || shapes.size() == 0) {
        }

        return shapes;
    }

}
