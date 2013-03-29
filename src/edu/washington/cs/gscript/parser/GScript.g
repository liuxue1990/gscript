grammar GScript;

tokens {
}

@lexer::header {
package edu.washington.cs.gscript.parser;
}

@parser::header {
package edu.washington.cs.gscript.parser;

import edu.washington.cs.gscript.models.*;
import java.util.LinkedList;
}

@rulecatch {
    catch(RecognitionException e) {
        throw e;
    }
}

prog returns [LinkedList<ShapeSpec> shapeList]
    : exprs EOF! {$shapeList = $exprs.shapeList;}
    ;

exprs returns [LinkedList<ShapeSpec> shapeList]
    : expr s=exprs {$shapeList = $s.shapeList; $shapeList.addFirst($expr.shape);}
    | {$shapeList = new LinkedList<ShapeSpec>();}
    ;

expr returns [ShapeSpec shape]
    : ROTATE LPAREN r=SYMBOL RPAREN s=repeatExpr {$shape = $s.shape; $shape.setNameOfAngle($r.text);}
    | repeatExpr {$shape = $repeatExpr.shape;}
    ;

repeatExpr returns [ShapeSpec shape]
    : REPEAT (LPAREN n=SYMBOL (COMMA r=SYMBOL)? RPAREN)? s=primitiveExpr {
            $shape = $s.shape;
            $shape.setRepeatable(true);
            $shape.setNameOfNumOfRepetition($n.text);
            $shape.setNameOfRepeatAngle($r.text);
        }
    | primitiveExpr {$shape = $primitiveExpr.shape;}
    ;

primitiveExpr returns [ShapeSpec shape]
    : DRAW LPAREN part=SYMBOL RPAREN {$shape = new ShapeSpec(); $shape.setPartName($part.text);}
    ;

REPEAT : [Rr][Ee][Pp][Ee][Aa][Tt];

DRAW : [Dd][Rr][Aa][Ww];

ROTATE : [Rr][Oo][Tt][Aa][Tt][Ee];

SYMBOL : (LETTER | UNDERSCORE) (LETTER | DIGIT | UNDERSCORE)* | '+' | '-' | '*' | '/';

INTEGER : SIGN? DIGIT+;

FLOAT : SIGN? DIGIT+ ('.' DIGIT+)? (('e'|'E') SIGN? DIGIT+)?;

fragment LOWERCASE : 'a'..'z';

fragment UPPERCASE : 'A'..'Z';

fragment UNDERSCORE : '_';

fragment LETTER : LOWERCASE | UPPERCASE;

fragment SIGN : '+'|'-';

fragment DIGIT : '0'..'9';

COMMA : ',';

LPAREN : '(';

RPAREN : ')';

LINE_COMMENT
    : '//' (~'\n')* -> skip
    ;

WHITESPACE
    : (' ' | '\t' | '\r' | '\n')+ -> skip
    ;
