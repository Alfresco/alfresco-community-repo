/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */

/*
 * Parser for the Alfresco full text query language.
 * It may be used stand-alone or embedded, for example, in CMIS SQL contains() 
 *
 */

grammar FTS;

options
{
	output=AST;
	backtrack=false;
}

/*
 * Additional tokens for tree building.
 */
tokens
{
	FTS;
	DISJUNCTION;
	CONJUNCTION;
	NEGATION;
	TERM;
	EXACT_TERM;
	PHRASE;
	SYNONYM;
	RANGE;
	PROXIMITY;
	
	DEFAULT;
	MANDATORY;
	OPTIONAL;
	EXCLUDE;
	 
	FIELD_DISJUNCTION;
	FIELD_CONJUNCTION;
	FIELD_NEGATION;
	FIELD_GROUP;
	FIELD_DEFAULT;
	FIELD_MANDATORY;
	FIELD_OPTIONAL;
	FIELD_EXCLUDE;
	FG_TERM;
	FG_EXACT_TERM;
	FG_PHRASE;
	FG_SYNONYM;
	FG_PROXIMITY;
	FG_RANGE;
	
	FIELD_REF;
	
	INCLUSIVE;
	EXCLUSIVE;
	
	QUALIFIER;
	PREFIX;
	NAME_SPACE;
	
	BOOST;
	FUZZY;
	
	TEMPLATE;
}


/*
 * Make sure the lexer and parser are generated in the correct package
 */
@lexer::header{package org.alfresco.repo.search.impl.parsers;} 

@header {package org.alfresco.repo.search.impl.parsers;}

/*
 * Embeded java to control the default connective when not specified.
 *
 * Do not support recover from errors
 *
 * Add extra detail to teh error message
 */
@members
{
    private Stack<String> paraphrases = new Stack<String>();
   
    private boolean defaultConjunction = true;
    
    private boolean defaultFieldConjunction = true;
   
    public boolean defaultConjunction()
    {
       return defaultConjunction;
    }
    
    public boolean defaultFieldConjunction()
    {
       return defaultFieldConjunction;
    }
    
    public void setDefaultConjunction(boolean defaultConjunction)
    {
       this.defaultConjunction = defaultConjunction;
    }
    
    public void setDefaultFieldConjunction(boolean defaultFieldConjunction)
    {
       this.defaultFieldConjunction = defaultFieldConjunction;
    }
    
    protected void mismatch(IntStream input, int ttype, BitSet follow) throws RecognitionException
    {
       throw new MismatchedTokenException(ttype, input);
    }
    
    public Object recoverFromMismatchedSet(IntStream input, RecognitionException e, BitSet follow) throws RecognitionException
    {
       throw e;
    }
    
    public String getErrorMessage(RecognitionException e, String[] tokenNames)
    {
       List stack = getRuleInvocationStack(e, this.getClass().getName());
       String msg = null;
       if(e instanceof NoViableAltException)
       {
            NoViableAltException nvae = (NoViableAltException)e;
            msg = "Syntax error at line "+nvae.line+ " position "+nvae.charPositionInLine+" at token " + nvae.token.getText() + 
             "\n\tNo viable alternative: token="+e.token+
             "\n\t\tDecision No ="+nvae.decisionNumber+
             "\n\t\tState No "+nvae.stateNumber+
             "\n\t\tDecision=<<"+nvae.grammarDecisionDescription+">>";
       }
       else
       {
           msg = super.getErrorMessage(e, tokenNames);
       }
       if(paraphrases.size() > 0)
       {
           String paraphrase = (String)paraphrases.peek();
           msg = msg+"\n\t\tParaphrase: "+paraphrase;
       }
       
       return msg+"\n\tStack\n\t\t"+stack;
    }
    
    public String getTokenErrorDisplay(Token t)
    {
       return t.toString();
    }      
}

/*
 * Always throw exceptions
 */
@rulecatch
{
catch(RecognitionException e)
{
   throw e;
}
}

/*
 * Support for emitting duplicate tokens from the lexer
 * - required to emit ranges after matching floating point literals ...
 */
@lexer::members {
List tokens = new ArrayList();
public void emit(Token token) {
        state.token = token;
        tokens.add(token);
}
public Token nextToken() {
        super.nextToken();
        if ( tokens.size()==0 ) {
            return Token.EOF_TOKEN;
        }
        return (Token)tokens.remove(0);
}
}


/*
 * Top level query
 */
ftsQuery	
    	: 	ftsImplicitConjunctionOrDisjunction EOF
		->  ftsImplicitConjunctionOrDisjunction
	;	

/*
 * When and and or are not specified between query components decide what to do ...
 */
ftsImplicitConjunctionOrDisjunction	
	:	{defaultConjunction()}? ftsExplicitDisjunction (ftsExplicitDisjunction)*
		-> ^(CONJUNCTION ftsExplicitDisjunction+)
		| ftsExplicitDisjunction (ftsExplicitDisjunction)*
		-> ^(DISJUNCTION ftsExplicitDisjunction+)
	;
	
/*
 * "OR"
 * As SQL, OR has lower precedence than AND
 */
ftsExplicitDisjunction
	:	ftsExplictConjunction ((or) => or ftsExplictConjunction)*
		-> ^(DISJUNCTION ftsExplictConjunction+)
	;

/*
 * "AND"
 */	
ftsExplictConjunction
	:	ftsPrefixed ((and) => and ftsPrefixed)*
		-> ^(CONJUNCTION ftsPrefixed+)
	;	
	
/*
 * Additional info around query compoents 
 * - negation, default, mandatory, optional, exclude and boost
 * These options control how individual elements are embedded in OR and AND
 * and how matches affect the overall score.
 */	
ftsPrefixed  
    	:	(not) => not ftsTest  boost?
		-> ^(NEGATION ftsTest  boost?)
    	|	ftsTest  boost?
		-> ^(DEFAULT ftsTest boost?)
    	|   	PLUS ftsTest boost?
                -> ^(MANDATORY ftsTest boost?)
    	|   	BAR ftsTest boost?
                -> ^(OPTIONAL ftsTest boost?)
    	|   	MINUS ftsTest boost?
                -> ^(EXCLUDE ftsTest boost?)
    	;

/*
 * Individual query components
 */
ftsTest	
    	:	ftsTerm ((fuzzy) => fuzzy)?
		-> ^(TERM ftsTerm fuzzy?)
	|	ftsExactTerm ((fuzzy) => fuzzy)?
		-> ^(EXACT_TERM ftsExactTerm fuzzy?)
    	|   	ftsPhrase ((slop) => slop)?
       		-> ^(PHRASE ftsPhrase slop?)
    	|   	ftsSynonym ((fuzzy) => fuzzy)? 
        	-> ^(SYNONYM ftsSynonym fuzzy?)
    	|	ftsFieldGroupProximity 
        	-> ^(PROXIMITY ftsFieldGroupProximity)
    	| 	ftsRange 
        	-> ^(RANGE ftsRange)
    	|	ftsFieldGroup 
    		-> ftsFieldGroup 
	|	LPAREN ftsImplicitConjunctionOrDisjunction RPAREN
		-> ftsImplicitConjunctionOrDisjunction
	|	template
	  	-> template
	;

template
	:	PERCENT tempReference
		-> ^(TEMPLATE tempReference)
	|	PERCENT LPAREN (tempReference COMMA?)+ RPAREN
		-> ^(TEMPLATE tempReference+)
	;	
	


fuzzy
	: 	TILDA number
		-> ^(FUZZY number)
	;
	
slop
	: 	TILDA DECIMAL_INTEGER_LITERAL
		-> ^(FUZZY DECIMAL_INTEGER_LITERAL)
	;

boost
	: 	CARAT number
		-> ^(BOOST number)
	;

ftsTerm
	:	(fieldReference COLON)? ftsWord
		-> ftsWord fieldReference?
	;
	
ftsExactTerm
	:	EQUALS ftsTerm
		-> ftsTerm
	;
	
ftsPhrase
	:  	(fieldReference COLON)? FTSPHRASE
		-> FTSPHRASE fieldReference?
	;

ftsSynonym 
	:	TILDA ftsTerm
		-> ftsTerm
	;

ftsRange 
	: 	(fieldReference COLON)? ftsFieldGroupRange
  		-> ftsFieldGroupRange fieldReference?
	;
	
ftsFieldGroup
	:	fieldReference COLON LPAREN ftsFieldGroupImplicitConjunctionOrDisjunction RPAREN
		-> ^(FIELD_GROUP fieldReference ftsFieldGroupImplicitConjunctionOrDisjunction)
	;

ftsFieldGroupImplicitConjunctionOrDisjunction	
	:	{defaultFieldConjunction()}? ftsFieldGroupExplicitDisjunction (ftsFieldGroupExplicitDisjunction)*
		-> ^(FIELD_CONJUNCTION ftsFieldGroupExplicitDisjunction+)
	|	ftsFieldGroupExplicitDisjunction (ftsFieldGroupExplicitDisjunction)*
		-> ^(FIELD_DISJUNCTION ftsFieldGroupExplicitDisjunction+)
	;
	
ftsFieldGroupExplicitDisjunction
	:	ftsFieldGroupExplictConjunction ((or) => or ftsFieldGroupExplictConjunction)*
		-> ^(FIELD_DISJUNCTION ftsFieldGroupExplictConjunction+)
	;
	
ftsFieldGroupExplictConjunction
	:	ftsFieldGroupPrefixed ((and) => and ftsFieldGroupPrefixed)*
		-> ^(FIELD_CONJUNCTION ftsFieldGroupPrefixed+)
	;
	
	
ftsFieldGroupPrefixed  
	:	(not) => not ftsFieldGroupTest boost?
		-> ^(FIELD_NEGATION ftsFieldGroupTest boost?)
    	|	ftsFieldGroupTest boost?
		-> ^(FIELD_DEFAULT ftsFieldGroupTest boost?)
	|   	PLUS ftsFieldGroupTest boost?
                -> ^(FIELD_MANDATORY ftsFieldGroupTest boost?)
    	|   	BAR ftsFieldGroupTest boost?
                -> ^(FIELD_OPTIONAL ftsFieldGroupTest boost?)
    	|   	MINUS ftsFieldGroupTest boost?
                -> ^(FIELD_EXCLUDE ftsFieldGroupTest boost?)
    	;


ftsFieldGroupTest
	:	ftsFieldGroupTerm ((fuzzy) => fuzzy)?
		-> ^(FG_TERM ftsFieldGroupTerm fuzzy?)
	|	ftsFieldGroupExactTerm ((fuzzy) => fuzzy)?
		-> ^(FG_EXACT_TERM ftsFieldGroupExactTerm fuzzy?)
	|	ftsFieldGroupPhrase ((slop) => slop)? 
		-> ^(FG_PHRASE ftsFieldGroupPhrase slop? )
	|	ftsFieldGroupSynonym ((fuzzy) => fuzzy)?
		-> ^(FG_SYNONYM ftsFieldGroupSynonym fuzzy?)
	|  	ftsFieldGroupProximity
		-> ^(FG_PROXIMITY ftsFieldGroupProximity)
    	| 	ftsFieldGroupRange
        	-> ^(FG_RANGE ftsFieldGroupRange)
	|	 LPAREN ftsFieldGroupImplicitConjunctionOrDisjunction RPAREN 
		-> ftsFieldGroupImplicitConjunctionOrDisjunction 
	;
	
ftsFieldGroupTerm
	:	ftsWord
	;
	
ftsFieldGroupExactTerm
	:	EQUALS ftsFieldGroupTerm
		-> ftsFieldGroupTerm
	;
	
ftsFieldGroupPhrase
	:  	FTSPHRASE
	;

ftsFieldGroupSynonym 
	:	TILDA ftsFieldGroupTerm
		-> ftsFieldGroupTerm
	;	

ftsFieldGroupProximity
	:	ftsFieldGroupTerm (proximityGroup ftsFieldGroupTerm)+
		-> ftsFieldGroupTerm (proximityGroup ftsFieldGroupTerm)+
	;
	
proximityGroup
  	: 	STAR ( LPAREN DECIMAL_INTEGER_LITERAL? RPAREN)?
  		-> ^(PROXIMITY DECIMAL_INTEGER_LITERAL?)
  	;
	
ftsFieldGroupRange
        :	ftsRangeWord DOTDOT ftsRangeWord
		-> INCLUSIVE ftsRangeWord ftsRangeWord INCLUSIVE
	|	range_left ftsRangeWord TO ftsRangeWord range_right
		-> range_left ftsRangeWord ftsRangeWord range_right
	;
	
range_left
	:       LSQUARE
		-> INCLUSIVE
	|	LT
		-> EXCLUSIVE
	;
	
range_right
	:       RSQUARE
		-> INCLUSIVE
	|	GT
		-> EXCLUSIVE
	;
	
	/* Need to fix the generated parser for extra COLON check ??*/
fieldReference
	: 	AT? (prefix|uri)? identifier
    		-> ^(FIELD_REF identifier prefix? uri?)
	;
	
tempReference
	: 	AT? (prefix|uri)? identifier
    		-> ^(FIELD_REF identifier prefix? uri?)
	;
	
prefix
	: 	identifier COLON
		-> ^(PREFIX identifier)
	;	
	
uri
  	: 	URI
  		-> ^(NAME_SPACE URI)
  	;
	
identifier
	:	ID
	;
	
ftsWord
    	:   	ID
    	|  	FTSWORD
    	|  	FTSPRE
    	|   	FTSWILD
    	|   	OR
    	|   	AND
    	|   	NOT
    	|   	TO
    	|   	DECIMAL_INTEGER_LITERAL
    	|  	FLOATING_POINT_LITERAL
    	;
    
number 
    	:   	DECIMAL_INTEGER_LITERAL
    	|   	FLOATING_POINT_LITERAL
    	;
    
ftsRangeWord
    	:   	ID
    	|   	FTSWORD
    	|   	FTSPRE
    	|   	FTSWILD
    	|   	FTSPHRASE
    	|   	DECIMAL_INTEGER_LITERAL
    	|   	FLOATING_POINT_LITERAL
    	;
	
or
    	:   	OR
    	|	BAR BAR
    	;
    
and	
    	:	AND
    	|	AMP AMP
    	;
    
not
    	:  	NOT
    	|   	EXCLAMATION
    	;			

// ===== //
// LEXER //
// ===== //

FTSPHRASE
  	: 	'"' (F_ESC | ~('\\'|'"') )* '"' ;


/*
 * Basic URI pattern based on the regular expression patttern taken from the RFC (it it not full URI parsing)
 * Note this means the language can not use {} anywhere else in the syntax
 */
URI 
	: 	'{' 
	        ((F_URI_ALPHA|F_URI_DIGIT|F_URI_OTHER) => (F_URI_ALPHA|F_URI_DIGIT|F_URI_OTHER)+ COLON)? 
	        ((('//') =>'//') 
	        ( (F_URI_ALPHA|F_URI_DIGIT|F_URI_OTHER|COLON) => (F_URI_ALPHA|F_URI_DIGIT|F_URI_OTHER|COLON))*)? 
	        (F_URI_ALPHA|F_URI_DIGIT|F_URI_OTHER|COLON|'/')* 
	        ('?' (F_URI_ALPHA|F_URI_DIGIT|F_URI_OTHER|COLON|'/'|'?')*)? 
	        ('#' (F_URI_ALPHA|F_URI_DIGIT|F_URI_OTHER|COLON|'/'|'?'|'#')*)? 
	        '}'  
	;


fragment 
F_URI_ALPHA	
	:	'A'..'Z' | 'a'..'z'
	;

fragment
F_URI_DIGIT 
	:	'0' ..'9'
	;
	
fragment
F_URI_ESC
	:       '%' F_HEX F_HEX
	;
	
fragment
F_URI_OTHER
	:	'-' |'.'|'_'|'~'|'['|']'|'@'|'!'|'$'|'&'|'\''|'('|')'|'*'|'+'|','|';'|'='
	;

/*
 * Simple tokens, note all are case insensitive
 */ 

OR	:	('O'|'o')('R'|'r');
AND	:	('A'|'a')('N'|'n')('D'|'d');
NOT	:	('N'|'n')('O'|'o')('T'|'t');
TILDA	:	'~' ;
LPAREN	:	'(' ;
RPAREN	:	')' ;
PLUS	:	'+' ;
MINUS	:	'-' ;
COLON	:	':' ;
STAR	:	'*' ;
DOTDOT  : 	'..' ;
DOT	:	'.' ;
AMP	:	'&' ;
EXCLAMATION : 	'!' ;
BAR 	: 	'|' ;
EQUALS 	: 	'=' ;
QUESTION_MARK : '?' ;
LCURL 	: 	'{' ;
RCURL 	: 	'}' ;
LSQUARE : 	'[' ;
RSQUARE : 	']' ;
TO 	: 	('T'|'t')('O'|'o') ;
COMMA 	: 	',';
CARAT 	: 	'^';
DOLLAR 	:  	'$';
GT 	: 	'>';
LT 	: 	'<';
AT	: 	'@';
PERCENT :       '%';

/**
 * ID
 * _x????_ encoding is supported for invalid sql characters but requires nothing here, they are handled in the code 
 * Also supports \ style escaping for non CMIS SQL 
 */ 
ID  	:   	('a'..'z'|'A'..'Z'|'_')('a'..'z'|'A'..'Z'|'0'..'9'|'_'|'$'|'#'|F_ESC)* ;

DECIMAL_INTEGER_LITERAL
        : 	( PLUS | MINUS )? DECIMAL_NUMERAL
        ;

FTSWORD :  	(F_ESC | INWORD)+;

FTSPRE 	:  	(F_ESC | INWORD)+ STAR;

FTSWILD : 	(F_ESC | INWORD | STAR | QUESTION_MARK)+;
	
fragment
F_ESC   : 	'\\'
    		(
    		// unicode 	
    		'u' F_HEX F_HEX F_HEX F_HEX
    		// any single char escaped 
   		| . 
    		)
  	;

fragment
F_HEX 	
	:	'0' .. '9'
  	| 	'a' .. 'f'
  	| 	'A' .. 'F'
  	;	
	
fragment 
INWORD	
	: 	'\u0041' .. '\u005A'
	| 	'\u0061' .. '\u007A'
	| 	'\u00C0' .. '\u00D6'
	| 	'\u00D8' .. '\u00F6'
	| 	'\u00F8' .. '\u00FF'
	| 	'\u0100' .. '\u1FFF'
	| 	'\u3040' .. '\u318F'
	|	'\u3300' .. '\u337F'
	| 	'\u3400' .. '\u3D2D'
	| 	'\u4E00' .. '\u9FFF'
	| 	'\uF900' .. '\uFAFF'
	| 	'\uAC00' .. '\uD7AF'
	| 	'\u0030' .. '\u0039'
	| 	'\u0660' .. '\u0669'
	| 	'\u06F0' .. '\u06F9'
	| 	'\u0966' .. '\u096F'
	|	'\u09E6' .. '\u09EF' 
	|	'\u0A66' .. '\u0A6F'
	| 	'\u0AE6' .. '\u0AEF'
	| 	'\u0B66' .. '\u0B6F'
	| 	'\u0BE7' .. '\u0BEF'
	| 	'\u0C66' .. '\u0C6F'
	| 	'\u0CE6' .. '\u0CEF'
	| 	'\u0D66' .. '\u0D6F'
	| 	'\u0E50' .. '\u0E59'
	| 	'\u0ED0' .. '\u0ED9'
	| 	'\u1040' .. '\u1049'
	;
             
        
/*
 * Range and floating point have to be conbined to avoid lexer issues.
 * This requires multi-token emits and addition supporting java code - see above ...
 *
 * Special rules for the likes of
 * 1..  integer ranges
 * 1... float range with the float terminated by .
 * If floats are 'full' e.g. 2.4.. then the parse matches the normal float tokem and a DOTDOT token
 * Likewise .1...2 does not require any special support
 *
 * Float and integer are based on the Java language spec.
 */        
FLOATING_POINT_LITERAL 
                // Integer ranges
  	: 	d=START_RANGE_I r=DOTDOT
    		{
      			$d.setType(DECIMAL_INTEGER_LITERAL);
      			emit($d);
      			$r.setType(DOTDOT);
      			emit($r);
    		}
    		// Float ranges
  	| 	d=START_RANGE_F r=DOTDOT
    		{
      			$d.setType(FLOATING_POINT_LITERAL);
      			emit($d);
      			$r.setType(DOTDOT);
      			emit($r);
    		}
    		// Normal float rules
  	| 	(PLUS | MINUS)? DIGIT+ DOT DIGIT* EXPONENT?
  	| 	(PLUS | MINUS)? DOT DIGIT+ EXPONENT?
  	| 	(PLUS | MINUS)? DIGIT+  EXPONENT
  	;

fragment
START_RANGE_I
	: 	(PLUS | MINUS)? DIGIT+ 
	;

fragment
START_RANGE_F
	: 	(PLUS | MINUS)? DIGIT+ DOT
	;

/**   
 * Fragments for decimal
 */
        
fragment
DECIMAL_NUMERAL
  	: 	ZERO_DIGIT 
  	| 	NON_ZERO_DIGIT DIGIT* 
  	;
  	
fragment
DIGIT 
	: 	ZERO_DIGIT 
	| 	NON_ZERO_DIGIT 
	;
	
fragment
ZERO_DIGIT  
  	: 	'0' ;
  	
fragment
NON_ZERO_DIGIT 
  	: 	'1'..'9' 
  	;

fragment
E 	: 	('e' | 'E') ;
  
fragment
EXPONENT
  	: 	E SIGNED_INTEGER
  	;
  	
fragment
SIGNED_INTEGER
  	: 	(PLUS | MINUS)? DIGIT+  
  	;

/*
 * Standard white space
 * White space may be escaped by \ in some tokens 
 */
WS	:	( ' ' | '\t' | '\r' | '\n' )+ { $channel = HIDDEN; } ;
