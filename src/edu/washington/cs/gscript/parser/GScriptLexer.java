// Generated from GScript.g by ANTLR 4.0

package edu.washington.cs.gscript.parser;

import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class GScriptLexer extends Lexer {
	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		REPEAT=1, DRAW=2, ROTATE=3, SYMBOL=4, INTEGER=5, FLOAT=6, COMMA=7, LPAREN=8, 
		RPAREN=9, LINE_COMMENT=10, WHITESPACE=11;
	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	public static final String[] tokenNames = {
		"<INVALID>",
		"REPEAT", "DRAW", "ROTATE", "SYMBOL", "INTEGER", "FLOAT", "','", "'('", 
		"')'", "LINE_COMMENT", "WHITESPACE"
	};
	public static final String[] ruleNames = {
		"REPEAT", "DRAW", "ROTATE", "SYMBOL", "INTEGER", "FLOAT", "LOWERCASE", 
		"UPPERCASE", "UNDERSCORE", "LETTER", "SIGN", "DIGIT", "COMMA", "LPAREN", 
		"RPAREN", "LINE_COMMENT", "WHITESPACE"
	};


	public GScriptLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "GScript.g"; }

	@Override
	public String[] getTokenNames() { return tokenNames; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

	@Override
	public void action(RuleContext _localctx, int ruleIndex, int actionIndex) {
		switch (ruleIndex) {
		case 15: LINE_COMMENT_action((RuleContext)_localctx, actionIndex); break;

		case 16: WHITESPACE_action((RuleContext)_localctx, actionIndex); break;
		}
	}
	private void WHITESPACE_action(RuleContext _localctx, int actionIndex) {
		switch (actionIndex) {
		case 1: skip();  break;
		}
	}
	private void LINE_COMMENT_action(RuleContext _localctx, int actionIndex) {
		switch (actionIndex) {
		case 0: skip();  break;
		}
	}

	public static final String _serializedATN =
		"\2\4\r\u0090\b\1\4\2\t\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t"+
		"\b\4\t\t\t\4\n\t\n\4\13\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20"+
		"\t\20\4\21\t\21\4\22\t\22\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\3\3\3\3\3\3\3"+
		"\3\3\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\5\3\5\5\5;\n\5\3\5\3\5\3\5\7\5@\n\5"+
		"\f\5\16\5C\13\5\3\5\5\5F\n\5\3\6\5\6I\n\6\3\6\6\6L\n\6\r\6\16\6M\3\7\5"+
		"\7Q\n\7\3\7\6\7T\n\7\r\7\16\7U\3\7\3\7\6\7Z\n\7\r\7\16\7[\5\7^\n\7\3\7"+
		"\3\7\5\7b\n\7\3\7\6\7e\n\7\r\7\16\7f\5\7i\n\7\3\b\3\b\3\t\3\t\3\n\3\n"+
		"\3\13\3\13\5\13s\n\13\3\f\3\f\3\r\3\r\3\16\3\16\3\17\3\17\3\20\3\20\3"+
		"\21\3\21\3\21\3\21\7\21\u0083\n\21\f\21\16\21\u0086\13\21\3\21\3\21\3"+
		"\22\6\22\u008b\n\22\r\22\16\22\u008c\3\22\3\22\2\23\3\3\1\5\4\1\7\5\1"+
		"\t\6\1\13\7\1\r\b\1\17\2\1\21\2\1\23\2\1\25\2\1\27\2\1\31\2\1\33\t\1\35"+
		"\n\1\37\13\1!\f\2#\r\3\3\2\27\4TTtt\4GGgg\4RRrr\4GGgg\4CCcc\4VVvv\4FF"+
		"ff\4TTtt\4CCcc\4YYyy\4TTtt\4QQqq\4VVvv\4CCcc\4VVvv\4GGgg\5,-//\61\61\4"+
		"GGgg\4--//\3\f\f\5\13\f\17\17\"\"\u009a\2\3\3\2\2\2\2\5\3\2\2\2\2\7\3"+
		"\2\2\2\2\t\3\2\2\2\2\13\3\2\2\2\2\r\3\2\2\2\2\33\3\2\2\2\2\35\3\2\2\2"+
		"\2\37\3\2\2\2\2!\3\2\2\2\2#\3\2\2\2\3%\3\2\2\2\5,\3\2\2\2\7\61\3\2\2\2"+
		"\tE\3\2\2\2\13H\3\2\2\2\rP\3\2\2\2\17j\3\2\2\2\21l\3\2\2\2\23n\3\2\2\2"+
		"\25r\3\2\2\2\27t\3\2\2\2\31v\3\2\2\2\33x\3\2\2\2\35z\3\2\2\2\37|\3\2\2"+
		"\2!~\3\2\2\2#\u008a\3\2\2\2%&\t\2\2\2&\'\t\3\2\2\'(\t\4\2\2()\t\5\2\2"+
		")*\t\6\2\2*+\t\7\2\2+\4\3\2\2\2,-\t\b\2\2-.\t\t\2\2./\t\n\2\2/\60\t\13"+
		"\2\2\60\6\3\2\2\2\61\62\t\f\2\2\62\63\t\r\2\2\63\64\t\16\2\2\64\65\t\17"+
		"\2\2\65\66\t\20\2\2\66\67\t\21\2\2\67\b\3\2\2\28;\5\25\13\29;\5\23\n\2"+
		":8\3\2\2\2:9\3\2\2\2;A\3\2\2\2<@\5\25\13\2=@\5\31\r\2>@\5\23\n\2?<\3\2"+
		"\2\2?=\3\2\2\2?>\3\2\2\2@C\3\2\2\2A?\3\2\2\2AB\3\2\2\2BF\3\2\2\2CA\3\2"+
		"\2\2DF\t\22\2\2E:\3\2\2\2ED\3\2\2\2F\n\3\2\2\2GI\5\27\f\2HG\3\2\2\2HI"+
		"\3\2\2\2IK\3\2\2\2JL\5\31\r\2KJ\3\2\2\2LM\3\2\2\2MK\3\2\2\2MN\3\2\2\2"+
		"N\f\3\2\2\2OQ\5\27\f\2PO\3\2\2\2PQ\3\2\2\2QS\3\2\2\2RT\5\31\r\2SR\3\2"+
		"\2\2TU\3\2\2\2US\3\2\2\2UV\3\2\2\2V]\3\2\2\2WY\7\60\2\2XZ\5\31\r\2YX\3"+
		"\2\2\2Z[\3\2\2\2[Y\3\2\2\2[\\\3\2\2\2\\^\3\2\2\2]W\3\2\2\2]^\3\2\2\2^"+
		"h\3\2\2\2_a\t\23\2\2`b\5\27\f\2a`\3\2\2\2ab\3\2\2\2bd\3\2\2\2ce\5\31\r"+
		"\2dc\3\2\2\2ef\3\2\2\2fd\3\2\2\2fg\3\2\2\2gi\3\2\2\2h_\3\2\2\2hi\3\2\2"+
		"\2i\16\3\2\2\2jk\4c|\2k\20\3\2\2\2lm\4C\\\2m\22\3\2\2\2no\7a\2\2o\24\3"+
		"\2\2\2ps\5\17\b\2qs\5\21\t\2rp\3\2\2\2rq\3\2\2\2s\26\3\2\2\2tu\t\24\2"+
		"\2u\30\3\2\2\2vw\4\62;\2w\32\3\2\2\2xy\7.\2\2y\34\3\2\2\2z{\7*\2\2{\36"+
		"\3\2\2\2|}\7+\2\2} \3\2\2\2~\177\7\61\2\2\177\u0080\7\61\2\2\u0080\u0084"+
		"\3\2\2\2\u0081\u0083\n\25\2\2\u0082\u0081\3\2\2\2\u0083\u0086\3\2\2\2"+
		"\u0084\u0082\3\2\2\2\u0084\u0085\3\2\2\2\u0085\u0087\3\2\2\2\u0086\u0084"+
		"\3\2\2\2\u0087\u0088\b\21\2\2\u0088\"\3\2\2\2\u0089\u008b\t\26\2\2\u008a"+
		"\u0089\3\2\2\2\u008b\u008c\3\2\2\2\u008c\u008a\3\2\2\2\u008c\u008d\3\2"+
		"\2\2\u008d\u008e\3\2\2\2\u008e\u008f\b\22\3\2\u008f$\3\2\2\2\23\2:?AE"+
		"HMPU[]afhr\u0084\u008c";
	public static final ATN _ATN =
		ATNSimulator.deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
	}
}