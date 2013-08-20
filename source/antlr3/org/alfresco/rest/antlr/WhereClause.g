grammar WhereClause;

options
{
  // antlr will generate java lexer and parser
  language = Java;
  // generated parser should create abstract syntax tree
  output = AST;
}

//package, we have to add package declaration on top of it
@lexer::header {
package org.alfresco.rest.antlr;
import java.util.Map;
import java.util.HashMap;
import org.alfresco.rest.framework.resource.parameters.where.InvalidQueryException;
import org.alfresco.rest.framework.resource.parameters.where.WhereCompiler;
}

@lexer::members {

    @Override
    public void recover(RecognitionException e)
    {
        throw new InvalidQueryException(WhereCompiler.resolveMessage(e));
    }
}
//package, we have to add package declaration on top of it
@parser::header {
package org.alfresco.rest.antlr;
import org.alfresco.rest.framework.resource.parameters.where.InvalidQueryException;
}

@parser::members {
   
  // These methods are here to force the parser to error instead of suppressing problems.
//	  @Override
//	  public void reportError(RecognitionException e) {
//      System.out.println("CUSTOM ERROR...\n" + e);
//      throw new InvalidQueryException(e.getMessage());
//	  }

    @Override
    protected Object recoverFromMismatchedToken(IntStream input, int ttype, BitSet follow) throws RecognitionException
    {
        throw new MismatchedTokenException(ttype, input);
    }
        
    @Override
    public Object recoverFromMismatchedSet(IntStream input, RecognitionException e, BitSet follow) throws RecognitionException
    {
        throw e;
    }
//    
//    @Override
//    public String getErrorMessage(RecognitionException e, String[] tokenNames) 
//    {
//      System.out.println("THROW ME...\n" + e);
//      throw new InvalidQueryException(e.getMessage());
//    }
// End of methods here to force the parser to error instead of supressing problems.
}

@rulecatch
{
catch(RecognitionException e)
{
   throw e;
}
}
// ***************** lexer rules:
NEGATION: ('not'|'NOT')WS;
EXISTS: 'exists'|'EXISTS';
IN: WS('in'|'IN');
MATCHES: WS('matches'|'MATCHES');
BETWEEN: WS('between'|'BETWEEN');
OR: WS('or'|'OR')WS;
AND: WS('and'|'AND')WS;
EQUALS: WS?'='WS?;
LESSTHAN: WS?'<'WS?;
GREATERTHAN: WS?'>'WS?;
LESSTHANOREQUALS: WS?'<='WS?;
GREATERTHANOREQUALS: WS?'>='WS?;
LEFTPAREN: '(';
RIGHTPAREN: ')';
COMMA: ',';
SINGLEQUOTE: '\'';
PROPERTYVALUE: (SINGLEQUOTE (~SINGLEQUOTE|'\\'SINGLEQUOTE)* SINGLEQUOTE) |IDENTIFIERDIGIT+;
PROPERTYNAME: '/'? IDENTIFIER ('/'IDENTIFIER)*;
fragment IDENTIFIER : (IDENTIFIERLETTER (IDENTIFIERLETTER | IDENTIFIERDIGIT)*);
WS : ( ' ' | '\t' | '\r' | '\n' )+ { $channel = HIDDEN; };
fragment IDENTIFIERLETTER  // any Unicode character that is a Java letter (see below)
    :    '\u0041'..'\u005a' // A-Z
    |    '\u005f'           // _
    |    '\u0061'..'\u007a' // a-z
    |    '\u00c0'..'\u00d6' // À-Ö
    |    '\u00d8'..'\u00f6' // Ø-ö
    |    '\u00f8'..'\u00ff' // ø-ÿ
    |    '\u0100'..'\u1fff'
    |    '\u3040'..'\u318f'
    |    '\u3300'..'\u337f'
    |    '\u3400'..'\u3d2d'
    |    '\u4e00'..'\u9fff'
    |    '\uf900'..'\ufaff'
    ;
fragment IDENTIFIERDIGIT
    :    '\u0030'..'\u0039'   // 0-9
    |    '\u0660'..'\u0669'   // Arabic 0-9
    |    '\u06f0'..'\u06f9'   // Arabic-Indic 0-9
    |    '\u0966'..'\u096f'   // Devanagari 0-9
    |    '\u09e6'..'\u09ef'   // Bengali 0-9
    |    '\u0a66'..'\u0a6f'   // Gurmukhi 0-9
    |    '\u0ae6'..'\u0aef'   // Gujarati 0-9
    |    '\u0b66'..'\u0b6f'   // Oriya 0-9
    |    '\u0be7'..'\u0bef'   // Tamil 0-9
    |    '\u0c66'..'\u0c6f'   // Telugu 0-9
    |    '\u0ce6'..'\u0cef'   // Kannada 0-9
    |    '\u0d66'..'\u0d6f'   // Malayalam 0-9
    |    '\u0e50'..'\u0e59'   // Thai 0-9
    |    '\u0ed0'..'\u0ed9'   // Lao 0-9
    |    '\u1040'..'\u1049'   // Myanmar 0-9
    ;

// ***************** parser rules:
whereclause : WS? LEFTPAREN! WS? predicate RIGHTPAREN! WS?;
predicate : simplepredicate
          | simplepredicate (AND simplepredicate)+ -> ^(AND simplepredicate+)
          | simplepredicate (OR simplepredicate)+ -> ^(OR simplepredicate+);
simplepredicate : allowedpredicates -> allowedpredicates
                | NEGATION allowedpredicates -> ^(NEGATION allowedpredicates);
allowedpredicates : comparisonpredicate | existspredicate | betweenpredicate | inpredicate | matchespredicate;
comparisonpredicate: PROPERTYNAME comparisonoperator value -> ^(comparisonoperator PROPERTYNAME value);
comparisonoperator: EQUALS|LESSTHAN|GREATERTHAN|LESSTHANOREQUALS|GREATERTHANOREQUALS;
existspredicate: EXISTS LEFTPAREN WS? PROPERTYNAME RIGHTPAREN -> ^(EXISTS PROPERTYNAME);
betweenpredicate: PROPERTYNAME BETWEEN LEFTPAREN WS? propertyvaluepair RIGHTPAREN -> ^(BETWEEN PROPERTYNAME propertyvaluepair);
inpredicate: PROPERTYNAME IN LEFTPAREN WS? propertyvaluelist RIGHTPAREN -> ^(IN PROPERTYNAME propertyvaluelist);
matchespredicate: PROPERTYNAME MATCHES LEFTPAREN WS? value RIGHTPAREN -> ^(MATCHES PROPERTYNAME value);
propertyvaluepair: value COMMA value -> value+;
propertyvaluelist: value (COMMA value)* -> value+;
value: a=PROPERTYVALUE -> ^(PROPERTYVALUE[$a] )
      | b=PROPERTYNAME -> ^(PROPERTYVALUE[$b] ); //rewrite this a propertyvalue
selectClause:  PROPERTYNAME (COMMA PROPERTYNAME)* -> PROPERTYNAME+;     
// ****
// SOME NOTES
// () - Parentheses. Used to group several elements, so they are treated as one single token
// ? - Any token followed by ? occurs 0 or 1 times
// * - Any token followed by * can occur 0 or more times
// + - Any token followed by + can occur 1 or more times
// . - Any character/token can occur one time
// ~ - Any character/token following the ~ may not occur at the current place
// .. - Between two characters .. spans a range which accepts every character between both boundaries inclusive
// ****