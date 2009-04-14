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
	
	COLUMN_REF;
	
	INCLUSIVE;
	EXCLUSIVE;
	
	QUALIFIER;
	PREFIX;
	NAME_SPACE;
}

@lexer::header{package org.alfresco.repo.search.impl.parsers;} 

@header {package org.alfresco.repo.search.impl.parsers;}

@members
{
    private Stack<String> paraphrases = new Stack<String>();
   
    public boolean defaultConjunction()
    {
       return true;
    }
    
    public boolean defaultFieldConjunction()
    {
       return true;
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
            msg = "No viable alt; token="+e.token+
             " (decision="+nvae.decisionNumber+
             " state "+nvae.stateNumber+")"+
             " decision=<<"+nvae.grammarDecisionDescription+">>";
       }
       else
       {
           msg = super.getErrorMessage(e, tokenNames);
       }
       if(paraphrases.size() > 0)
       {
           String paraphrase = (String)paraphrases.peek();
           msg = msg+" "+paraphrase;
       }
       
       return stack+" "+msg;
    }
    
    public String getTokenErrorDisplay(Token t)
    {
       return t.toString();
    }      
}

@rulecatch
{
catch(RecognitionException e)
{
   throw e;
}
}

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



ftsQuery	
    : 	ftsImplicitConjunctionOrDisjunction EOF
		->  ftsImplicitConjunctionOrDisjunction
	;	

ftsImplicitConjunctionOrDisjunction	
	:	{defaultConjunction()}? ftsExplicitDisjunction (ftsExplicitDisjunction)*
		-> ^(CONJUNCTION ftsExplicitDisjunction+)
		| ftsExplicitDisjunction (ftsExplicitDisjunction)*
		-> ^(DISJUNCTION ftsExplicitDisjunction+)
	;
	
ftsExplicitDisjunction
	:	ftsExplictConjunction ((or) => or ftsExplictConjunction)*
		-> ^(DISJUNCTION ftsExplictConjunction+)
	;
	
ftsExplictConjunction
	:	ftsPrefixed ((and) => and ftsPrefixed)*
		-> ^(CONJUNCTION ftsPrefixed+)
	;	
	
	
ftsPrefixed  
    :	(not) => not ftsTest
		-> ^(NEGATION ftsTest)
    |	ftsTest
		-> ^(DEFAULT ftsTest)
    |   PLUS ftsTest
                -> ^(MANDATORY ftsTest)
    |   BAR ftsTest
                -> ^(OPTIONAL ftsTest)
    |   MINUS ftsTest
                -> ^(EXCLUDE ftsTest)
    ;

ftsTest	
    :	ftsTerm
		-> ^(TERM ftsTerm)
	|	ftsExactTerm
		-> ^(EXACT_TERM ftsExactTerm)
    |   ftsPhrase
        -> ^(PHRASE ftsPhrase)
    |   ftsSynonym
        -> ^(SYNONYM ftsSynonym)
    |	ftsFieldGroupProximity  
        -> ^(PROXIMITY ftsFieldGroupProximity)
    | 	ftsRange
        -> ^(RANGE ftsRange)
    |	ftsFieldGroup    
	|	LPAREN ftsImplicitConjunctionOrDisjunction RPAREN
		-> ftsImplicitConjunctionOrDisjunction
	;

ftsTerm
	:	(columnReference COLON)? ftsWord
		-> ftsWord columnReference?
	;
	
ftsExactTerm
	:	EQUALS ftsTerm
		-> ftsTerm
	;
	
ftsPhrase
	:  	(columnReference COLON)? FTSPHRASE
		-> FTSPHRASE columnReference?
	;

ftsSynonym 
	:	TILDA ftsTerm
		-> ftsTerm
	;

ftsRange 
: (columnReference COLON)? ftsFieldGroupRange
  -> ftsFieldGroupRange columnReference?
;
	
ftsFieldGroup
	:	columnReference COLON LPAREN ftsFieldGroupImplicitConjunctionOrDisjunction RPAREN
		-> ^(FIELD_GROUP columnReference ftsFieldGroupImplicitConjunctionOrDisjunction)
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
:	(not) => not ftsFieldGroupTest
		-> ^(FIELD_NEGATION ftsFieldGroupTest)
    |	ftsFieldGroupTest
		-> ^(FIELD_DEFAULT ftsFieldGroupTest)
    |   PLUS ftsFieldGroupTest
                -> ^(FIELD_MANDATORY ftsFieldGroupTest)
    |   BAR ftsFieldGroupTest
                -> ^(FIELD_OPTIONAL ftsFieldGroupTest)
    |   MINUS ftsFieldGroupTest
                -> ^(FIELD_EXCLUDE ftsFieldGroupTest)
    ;


ftsFieldGroupTest
	:	ftsFieldGroupTerm
		-> ^(FG_TERM ftsFieldGroupTerm)
	|	ftsFieldGroupExactTerm 
		-> ^(FG_EXACT_TERM ftsFieldGroupExactTerm)
	|	ftsFieldGroupPhrase
		-> ^(FG_PHRASE ftsFieldGroupPhrase)
	|	ftsFieldGroupSynonym
		-> ^(FG_SYNONYM ftsFieldGroupSynonym)
	|  ftsFieldGroupProximity  
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
	:	ftsFieldGroupTerm STAR ftsFieldGroupTerm
		-> ftsFieldGroupTerm ftsFieldGroupTerm
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
columnReference
	: (prefix|uri)? identifier
    -> ^(COLUMN_REF identifier prefix? uri?)
	;
	
prefix
: identifier COLON
-> ^(PREFIX identifier)
;	
	
uri
  : URI
  -> ^(NAME_SPACE URI)
  ;
	
identifier
	:	ID
	;
	
ftsWord
    :   ID
    |   FTSWORD
    |   OR
    |   AND
    |   NOT
    |   TO
    |   DECIMAL_INTEGER_LITERAL
    |   FLOATING_POINT_LITERAL
    ;
    
ftsRangeWord
    :   ID
    |   FTSWORD
    |   FTSPHRASE
    |   DECIMAL_INTEGER_LITERAL
    |   FLOATING_POINT_LITERAL
    ;
	
or
    :   OR
    |	BAR BAR
    ;
    
and	
    :	AND
    |	AMP AMP
    ;
    
not
    :   NOT
    |   EXCLAMATION
    ;		

FTSPHRASE
  : '"' (F_ESC | ~('\\'|'"') )* '"' ;

URI 
: '{' ((F_URI_ALPHA|F_URI_DIGIT|F_URI_OTHER) => (F_URI_ALPHA|F_URI_DIGIT|F_URI_OTHER)+ COLON)? ((('//') =>'//') ( (F_URI_ALPHA|F_URI_DIGIT|F_URI_OTHER|COLON) => (F_URI_ALPHA|F_URI_DIGIT|F_URI_OTHER|COLON))*)? (F_URI_ALPHA|F_URI_DIGIT|F_URI_OTHER|COLON|'/')* ('?' (F_URI_ALPHA|F_URI_DIGIT|F_URI_OTHER|COLON|'/'|'?')*)? ('#' (F_URI_ALPHA|F_URI_DIGIT|F_URI_OTHER|COLON|'/'|'?'|'#')*)? '}'  
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
DOTDOT  : '..' ;
DOT	:	'.' ;
AMP	:	'&' ;
EXCLAMATION : '!' ;
BAR : '|' ;
EQUALS : '=' ;
QUESTION_MARK : '?' ;
LCURL : '{' ;
RCURL : '}' ;
LSQUARE : '[' ;
RSQUARE : ']' ;
TO : ('T'|'t')('O'|'o') ;
COMMA : ',';
CARAT : '^';
DOLLAR :  '$';
GT : '>';
LT : '<';

/**
 * We should support _x????_ encoding for invalid sql characters 
 */ 
ID  :   ('a'..'z'|'A'..'Z'|'_')('a'..'z'|'A'..'Z'|'0'..'9'|'_'|'$'|'#')* ;

FTSWORD :	 (F_ESC | INWORD)+;
	

	
fragment
F_ESC   : '\\'
    ( 'u' F_HEX F_HEX F_HEX F_HEX
    | . // any single char escaped 
    )
  ;

fragment
F_HEX :
    '0' .. '9'
  | 'a' .. 'f'
  | 'A' .. 'F'
  ;	
	
fragment 
INWORD	: '\u0041' .. '\u005A'
	| '\u0061' .. '\u007A'
	| '\u00C0' .. '\u00D6'
	| '\u00D8' .. '\u00F6'
	| '\u00F8' .. '\u00FF'
	| '\u0100' .. '\u1FFF'
	| '\u3040' .. '\u318F'
	| '\u3300' .. '\u337F'
	| '\u3400' .. '\u3D2D'
	| '\u4E00' .. '\u9FFF'
	| '\uF900' .. '\uFAFF'
	| '\uAC00' .. '\uD7AF'
	| '\u0030' .. '\u0039'
	| '\u0660' .. '\u0669'
	| '\u06F0' .. '\u06F9'
	| '\u0966' .. '\u096F'
	| '\u09E6' .. '\u09EF' 
	| '\u0A66' .. '\u0A6F'
	| '\u0AE6' .. '\u0AEF'
	| '\u0B66' .. '\u0B6F'
	| '\u0BE7' .. '\u0BEF'
	| '\u0C66' .. '\u0C6F'
	| '\u0CE6' .. '\u0CEF'
	| '\u0D66' .. '\u0D6F'
	| '\u0E50' .. '\u0E59'
	| '\u0ED0' .. '\u0ED9'
	| '\u1040' .. '\u1049'
	;
	
DECIMAL_INTEGER_LITERAL
        : ( PLUS | MINUS )? DECIMAL_NUMERAL
        ;
        
      
      
        
FLOATING_POINT_LITERAL 
  : d=START_RANGE_I r=DOTDOT
    {
      $d.setType(DECIMAL_INTEGER_LITERAL);
      emit($d);
      $r.setType(DOTDOT);
      emit($r);
    }
  | d=START_RANGE_F r=DOTDOT
    {
      $d.setType(FLOATING_POINT_LITERAL);
      emit($d);
      $r.setType(DOTDOT);
      emit($r);
    }
  | (PLUS | MINUS)? DIGIT+ DOT DIGIT* EXPONENT?
  | (PLUS | MINUS)? DOT DIGIT+ EXPONENT?
  | (PLUS | MINUS)? DIGIT+  EXPONENT
  ;

fragment
START_RANGE_I: (PLUS | MINUS)? DIGIT+ 
;

fragment
START_RANGE_F: (PLUS | MINUS)? DIGIT+ DOT
;

/**   
 * Fragments for decimal
 */
        
fragment
DECIMAL_NUMERAL
  : ZERO_DIGIT 
  | NON_ZERO_DIGIT DIGIT* 
  ;
fragment
DIGIT : ZERO_DIGIT | NON_ZERO_DIGIT ;
fragment
ZERO_DIGIT  
  : '0' ;
fragment
NON_ZERO_DIGIT 
  : '1'..'9' ;

fragment
E : ('e' | 'E') ;
  
fragment
EXPONENT
  : E SIGNED_INTEGER
  ;
fragment
SIGNED_INTEGER
  : (PLUS | MINUS)? DIGIT+  
  ;


WS	:	( ' ' | '\t' | '\r' | '\n' )+ { $channel = HIDDEN; } ;
