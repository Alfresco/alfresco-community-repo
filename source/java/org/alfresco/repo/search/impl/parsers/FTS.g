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
 * Parser for the CMIS query language
 *
 * The semantics of multivalued properties are ignored for the initial parse of the language.
 * They are applied in a second pass, when we have enough information to determine the column type. 
 */
 
grammar FTS;

options
{
	output=AST;
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
	
	FIELD_DISJUNCTION;
	FIELD_CONJUNCTION;
	FIELD_NEGATION;
	FIELD_GROUP;
	FG_TERM;
	FG_EXACT_TERM;
	FG_PHRASE;
	FG_SYNONYM;
	FG_PROXIMITY;
	FG_RANGE;
	
	COLUMN_REF;
}

@members
{
       
	public boolean defaultConjunction()
	{
	   return true;
	}
	
	public boolean defaultFieldConjunction()
	{
	   return true;
	}
	
	
}

fts	: 	ftsImplicitConjunctionOrDisjunction
		->  ftsImplicitConjunctionOrDisjunction
	;	

ftsImplicitConjunctionOrDisjunction	
	:	{defaultConjunction()}? ftsExplicitDisjunction (ftsExplicitDisjunction)*
		-> ^(CONJUNCTION ftsExplicitDisjunction+)
		| ftsExplicitDisjunction (ftsExplicitDisjunction)*
		-> ^(DISJUNCTION ftsExplicitDisjunction+)
	;
	
ftsExplicitDisjunction
	:	ftsExplictConjunction (OR ftsExplictConjunction)*
		-> ^(DISJUNCTION ftsExplictConjunction+)
	;
	
ftsExplictConjunction
	:	ftsNot (AND ftsNot)*
		-> ^(CONJUNCTION ftsNot)
	;
	
	
ftsNot  :	MINUS ftsTest
		-> ^(NEGATION ftsTest)
	|	ftsTest
		-> ftsTest
	;

ftsTest	:	ftsTerm
		-> ^(TERM ftsTerm)
	|	ftsExactTerm
		-> ^(EXACT_TERM ftsExactTerm)
        |       ftsPhrase
        	-> ^(PHRASE ftsPhrase)
        |       ftsSynonym
        	-> ^(SYNONYM ftsSynonym)
        |	ftsFieldGroupProximity  
        	-> ^(FG_PROXIMITY ftsFieldGroupProximity)
        | 	ftsFieldGroupRange
        	-> ^(FG_RANGE ftsFieldGroupRange)
        |	ftsFieldGroup    
	|	LPAREN ftsImplicitConjunctionOrDisjunction RPAREN
		-> ftsImplicitConjunctionOrDisjunction
	;

ftsTerm
	:	(columnReference COLON)? FTSWORD
		-> FTSWORD columnReference?
	;
	
ftsExactTerm
	:	PLUS ftsTerm
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

	
ftsFieldGroup
	:	columnReference COLON LPAREN ftsFieldGroupImplicitConjunctionOrDisjunction RPAREN
		-> ^(FIELD_GROUP columnReference ftsFieldGroupImplicitConjunctionOrDisjunction)
	;

ftsFieldGroupImplicitConjunctionOrDisjunction	
	:	{defaultFieldConjunction}? ftsFieldGroupExplicitDisjunction (ftsFieldGroupExplicitDisjunction)*
		-> ^(FIELD_CONJUNCTION ftsFieldGroupExplicitDisjunction+)
	|	ftsFieldGroupExplicitDisjunction (ftsFieldGroupExplicitDisjunction)*
		-> ^(FIELD_DISJUNCTION ftsFieldGroupExplicitDisjunction+)
	;
	
ftsFieldGroupExplicitDisjunction
	:	ftsFieldGroupExplictConjunction (OR ftsFieldGroupExplictConjunction)*
		-> ^(FIELD_DISJUNCTION ftsFieldGroupExplictConjunction+)
	;
	
ftsFieldGroupExplictConjunction
	:	ftsFieldGroupNot (AND ftsFieldGroupNot)*
		-> ^(FIELD_CONJUNCTION ftsFieldGroupNot+)
	;
	
	
ftsFieldGroupNot  :	MINUS ftsFieldGroupTest
			-> FIELD_NEGATION ftsFieldGroupTest
	|		ftsFieldGroupTest
			-> ftsFieldGroupTest
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
	|       ftsFieldGroupProximity  
		-> ^(FG_PROXIMITY ftsFieldGroupProximity)
        | 	ftsFieldGroupRange
        	-> ^(FG_RANGE ftsFieldGroupRange)
	|	LPAREN ftsFieldGroupImplicitConjunctionOrDisjunction RPAREN
		-> ftsFieldGroupImplicitConjunctionOrDisjunction
	;
	
ftsFieldGroupTerm
	:	FTSWORD
	;
	
ftsFieldGroupExactTerm
	:	PLUS ftsFieldGroupTerm
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
	
ftsFieldGroupRange:	ftsFieldGroupTerm DOTDOT ftsFieldGroupTerm
		-> ftsFieldGroupTerm ftsFieldGroupTerm
	;
	
columnReference
	:	( qualifier=identifier DOT )? name=identifier
		-> ^(COLUMN_REF $name $qualifier?)
	;
	
identifier
	:	ID
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
DOTDOT	:	'..' ;
DOT	:	'.' ;

FTSWORD :	 INWORD+;
	
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
	
FTSPHRASE
	:	'"' (~'"' | '""')* '"'	;

ID	:	('a'..'z'|'A'..'Z'|'_')('a'..'z'|'A'..'Z'|'0'..'0'|'_'|'$'|'#')* ;

WS	:	( ' ' | '\t' | '\r' | '\n' )+ { $channel = HIDDEN; } ;
