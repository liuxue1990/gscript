// Generated from GScript.g by ANTLR 4.0

package edu.washington.cs.gscript.parser;

import edu.washington.cs.gscript.models.*;
import java.util.LinkedList;

import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class GScriptParser extends Parser {
	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		REPEAT=1, DRAW=2, ROTATE=3, SYMBOL=4, INTEGER=5, FLOAT=6, COMMA=7, LPAREN=8, 
		RPAREN=9, LINE_COMMENT=10, WHITESPACE=11;
	public static final String[] tokenNames = {
		"<INVALID>", "REPEAT", "DRAW", "ROTATE", "SYMBOL", "INTEGER", "FLOAT", 
		"','", "'('", "')'", "LINE_COMMENT", "WHITESPACE"
	};
	public static final int
		RULE_prog = 0, RULE_exprs = 1, RULE_expr = 2, RULE_repeatExpr = 3, RULE_primitiveExpr = 4;
	public static final String[] ruleNames = {
		"prog", "exprs", "expr", "repeatExpr", "primitiveExpr"
	};

	@Override
	public String getGrammarFileName() { return "GScript.g"; }

	@Override
	public String[] getTokenNames() { return tokenNames; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public ATN getATN() { return _ATN; }

	public GScriptParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}
	public static class ProgContext extends ParserRuleContext {
		public LinkedList<ShapeSpec> shapeList;
		public ExprsContext exprs;
		public TerminalNode EOF() { return getToken(GScriptParser.EOF, 0); }
		public ExprsContext exprs() {
			return getRuleContext(ExprsContext.class,0);
		}
		public ProgContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_prog; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof GScriptListener ) ((GScriptListener)listener).enterProg(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof GScriptListener ) ((GScriptListener)listener).exitProg(this);
		}
	}

	public final ProgContext prog() throws RecognitionException {
		ProgContext _localctx = new ProgContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_prog);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(10); ((ProgContext)_localctx).exprs = exprs();
			setState(11); match(EOF);
			((ProgContext)_localctx).shapeList =  ((ProgContext)_localctx).exprs.shapeList;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ExprsContext extends ParserRuleContext {
		public LinkedList<ShapeSpec> shapeList;
		public ExprContext expr;
		public ExprsContext s;
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public ExprsContext exprs() {
			return getRuleContext(ExprsContext.class,0);
		}
		public ExprsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_exprs; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof GScriptListener ) ((GScriptListener)listener).enterExprs(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof GScriptListener ) ((GScriptListener)listener).exitExprs(this);
		}
	}

	public final ExprsContext exprs() throws RecognitionException {
		ExprsContext _localctx = new ExprsContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_exprs);
		try {
			setState(19);
			switch (_input.LA(1)) {
			case REPEAT:
			case DRAW:
			case ROTATE:
				enterOuterAlt(_localctx, 1);
				{
				setState(14); ((ExprsContext)_localctx).expr = expr();
				setState(15); ((ExprsContext)_localctx).s = exprs();
				((ExprsContext)_localctx).shapeList =  ((ExprsContext)_localctx).s.shapeList; _localctx.shapeList.addFirst(((ExprsContext)_localctx).expr.shape);
				}
				break;
			case EOF:
				enterOuterAlt(_localctx, 2);
				{
				((ExprsContext)_localctx).shapeList =  new LinkedList<ShapeSpec>();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ExprContext extends ParserRuleContext {
		public ShapeSpec shape;
		public Token r;
		public RepeatExprContext s;
		public RepeatExprContext repeatExpr;
		public TerminalNode RPAREN() { return getToken(GScriptParser.RPAREN, 0); }
		public TerminalNode ROTATE() { return getToken(GScriptParser.ROTATE, 0); }
		public RepeatExprContext repeatExpr() {
			return getRuleContext(RepeatExprContext.class,0);
		}
		public TerminalNode SYMBOL() { return getToken(GScriptParser.SYMBOL, 0); }
		public TerminalNode LPAREN() { return getToken(GScriptParser.LPAREN, 0); }
		public ExprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_expr; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof GScriptListener ) ((GScriptListener)listener).enterExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof GScriptListener ) ((GScriptListener)listener).exitExpr(this);
		}
	}

	public final ExprContext expr() throws RecognitionException {
		ExprContext _localctx = new ExprContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_expr);
		try {
			setState(31);
			switch (_input.LA(1)) {
			case ROTATE:
				enterOuterAlt(_localctx, 1);
				{
				setState(21); match(ROTATE);
				setState(22); match(LPAREN);
				setState(23); ((ExprContext)_localctx).r = match(SYMBOL);
				setState(24); match(RPAREN);
				setState(25); ((ExprContext)_localctx).s = repeatExpr();
				((ExprContext)_localctx).shape =  ((ExprContext)_localctx).s.shape; _localctx.shape.setNameOfAngle((((ExprContext)_localctx).r!=null?((ExprContext)_localctx).r.getText():null));
				}
				break;
			case REPEAT:
			case DRAW:
				enterOuterAlt(_localctx, 2);
				{
				setState(28); ((ExprContext)_localctx).repeatExpr = repeatExpr();
				((ExprContext)_localctx).shape =  ((ExprContext)_localctx).repeatExpr.shape;
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class RepeatExprContext extends ParserRuleContext {
		public ShapeSpec shape;
		public Token n;
		public Token r;
		public PrimitiveExprContext s;
		public PrimitiveExprContext primitiveExpr;
		public TerminalNode RPAREN() { return getToken(GScriptParser.RPAREN, 0); }
		public TerminalNode REPEAT() { return getToken(GScriptParser.REPEAT, 0); }
		public List<TerminalNode> SYMBOL() { return getTokens(GScriptParser.SYMBOL); }
		public TerminalNode COMMA() { return getToken(GScriptParser.COMMA, 0); }
		public TerminalNode SYMBOL(int i) {
			return getToken(GScriptParser.SYMBOL, i);
		}
		public TerminalNode LPAREN() { return getToken(GScriptParser.LPAREN, 0); }
		public PrimitiveExprContext primitiveExpr() {
			return getRuleContext(PrimitiveExprContext.class,0);
		}
		public RepeatExprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_repeatExpr; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof GScriptListener ) ((GScriptListener)listener).enterRepeatExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof GScriptListener ) ((GScriptListener)listener).exitRepeatExpr(this);
		}
	}

	public final RepeatExprContext repeatExpr() throws RecognitionException {
		RepeatExprContext _localctx = new RepeatExprContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_repeatExpr);
		int _la;
		try {
			setState(49);
			switch (_input.LA(1)) {
			case REPEAT:
				enterOuterAlt(_localctx, 1);
				{
				setState(33); match(REPEAT);
				setState(41);
				_la = _input.LA(1);
				if (_la==LPAREN) {
					{
					setState(34); match(LPAREN);
					setState(35); ((RepeatExprContext)_localctx).n = match(SYMBOL);
					setState(38);
					_la = _input.LA(1);
					if (_la==COMMA) {
						{
						setState(36); match(COMMA);
						setState(37); ((RepeatExprContext)_localctx).r = match(SYMBOL);
						}
					}

					setState(40); match(RPAREN);
					}
				}

				setState(43); ((RepeatExprContext)_localctx).s = primitiveExpr();

				            ((RepeatExprContext)_localctx).shape =  ((RepeatExprContext)_localctx).s.shape;
				            _localctx.shape.setRepeatable(true);
				            _localctx.shape.setNameOfNumOfRepetition((((RepeatExprContext)_localctx).n!=null?((RepeatExprContext)_localctx).n.getText():null));
				            _localctx.shape.setNameOfRepeatAngle((((RepeatExprContext)_localctx).r!=null?((RepeatExprContext)_localctx).r.getText():null));
				        
				}
				break;
			case DRAW:
				enterOuterAlt(_localctx, 2);
				{
				setState(46); ((RepeatExprContext)_localctx).primitiveExpr = primitiveExpr();
				((RepeatExprContext)_localctx).shape =  ((RepeatExprContext)_localctx).primitiveExpr.shape;
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class PrimitiveExprContext extends ParserRuleContext {
		public ShapeSpec shape;
		public Token part;
		public TerminalNode RPAREN() { return getToken(GScriptParser.RPAREN, 0); }
		public TerminalNode SYMBOL() { return getToken(GScriptParser.SYMBOL, 0); }
		public TerminalNode LPAREN() { return getToken(GScriptParser.LPAREN, 0); }
		public TerminalNode DRAW() { return getToken(GScriptParser.DRAW, 0); }
		public PrimitiveExprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_primitiveExpr; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof GScriptListener ) ((GScriptListener)listener).enterPrimitiveExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof GScriptListener ) ((GScriptListener)listener).exitPrimitiveExpr(this);
		}
	}

	public final PrimitiveExprContext primitiveExpr() throws RecognitionException {
		PrimitiveExprContext _localctx = new PrimitiveExprContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_primitiveExpr);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(51); match(DRAW);
			setState(52); match(LPAREN);
			setState(53); ((PrimitiveExprContext)_localctx).part = match(SYMBOL);
			setState(54); match(RPAREN);
			((PrimitiveExprContext)_localctx).shape =  new ShapeSpec(); _localctx.shape.setPartName((((PrimitiveExprContext)_localctx).part!=null?((PrimitiveExprContext)_localctx).part.getText():null));
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static final String _serializedATN =
		"\2\3\r<\4\2\t\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\3\2\3\2\3\2\3\2\3\3\3"+
		"\3\3\3\3\3\3\3\5\3\26\n\3\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4\5\4"+
		"\"\n\4\3\5\3\5\3\5\3\5\3\5\5\5)\n\5\3\5\5\5,\n\5\3\5\3\5\3\5\3\5\3\5\3"+
		"\5\5\5\64\n\5\3\6\3\6\3\6\3\6\3\6\3\6\3\6\2\7\2\4\6\b\n\2\2;\2\f\3\2\2"+
		"\2\4\25\3\2\2\2\6!\3\2\2\2\b\63\3\2\2\2\n\65\3\2\2\2\f\r\5\4\3\2\r\16"+
		"\7\1\2\2\16\17\b\2\1\2\17\3\3\2\2\2\20\21\5\6\4\2\21\22\5\4\3\2\22\23"+
		"\b\3\1\2\23\26\3\2\2\2\24\26\b\3\1\2\25\20\3\2\2\2\25\24\3\2\2\2\26\5"+
		"\3\2\2\2\27\30\7\5\2\2\30\31\7\n\2\2\31\32\7\6\2\2\32\33\7\13\2\2\33\34"+
		"\5\b\5\2\34\35\b\4\1\2\35\"\3\2\2\2\36\37\5\b\5\2\37 \b\4\1\2 \"\3\2\2"+
		"\2!\27\3\2\2\2!\36\3\2\2\2\"\7\3\2\2\2#+\7\3\2\2$%\7\n\2\2%(\7\6\2\2&"+
		"\'\7\t\2\2\')\7\6\2\2(&\3\2\2\2()\3\2\2\2)*\3\2\2\2*,\7\13\2\2+$\3\2\2"+
		"\2+,\3\2\2\2,-\3\2\2\2-.\5\n\6\2./\b\5\1\2/\64\3\2\2\2\60\61\5\n\6\2\61"+
		"\62\b\5\1\2\62\64\3\2\2\2\63#\3\2\2\2\63\60\3\2\2\2\64\t\3\2\2\2\65\66"+
		"\7\4\2\2\66\67\7\n\2\2\678\7\6\2\289\7\13\2\29:\b\6\1\2:\13\3\2\2\2\7"+
		"\25!(+\63";
	public static final ATN _ATN =
		ATNSimulator.deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
	}
}