// Generated from GScript.g by ANTLR 4.0

package edu.washington.cs.gscript.parser;

import edu.washington.cs.gscript.models.*;
import java.util.LinkedList;

import org.antlr.v4.runtime.tree.*;
import org.antlr.v4.runtime.Token;

public interface GScriptListener extends ParseTreeListener {
	void enterProg(GScriptParser.ProgContext ctx);
	void exitProg(GScriptParser.ProgContext ctx);

	void enterRepeatExpr(GScriptParser.RepeatExprContext ctx);
	void exitRepeatExpr(GScriptParser.RepeatExprContext ctx);

	void enterExpr(GScriptParser.ExprContext ctx);
	void exitExpr(GScriptParser.ExprContext ctx);

	void enterExprs(GScriptParser.ExprsContext ctx);
	void exitExprs(GScriptParser.ExprsContext ctx);

	void enterPrimitiveExpr(GScriptParser.PrimitiveExprContext ctx);
	void exitPrimitiveExpr(GScriptParser.PrimitiveExprContext ctx);
}