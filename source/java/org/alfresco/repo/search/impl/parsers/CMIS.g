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
 
grammar CMIS;

options
{
	output=AST;
}

tokens
{
	QUERY;
	ALL_COLUMNS;
	COLUMN;
	COLUMNS;
	COLUMN_REF;
	QUALIFIER;
	FUNCTION;
	SOURCE;
	TABLE;
	TABLE_REF;
	PARAMETER;
	
	CONJUNCTION;
	DISJUNCTION;
	NEGATION;
	PRED_COMPARISON;
	PRED_IN;
	PRED_EXISTS;
	PRED_LIKE;
	PRED_FTS;
	LIST;
	PRED_CHILD;
	PRED_DESCENDANT;
	SORT_SPECIFICATION;
	
	NUMERIC_LITERAL;
	STRING_LITERAL;
}

@lexer::header{package org.alfresco.repo.search.impl.parsers;} 

@header {package org.alfresco.repo.search.impl.parsers;}

/*
 * Instance methods and properties for the parser.
 * Realisations of the parser should over-ride these as required
 */
 
@members
{
    private Stack<String> paraphrases = new Stack<String>();

    /**
     * CMIS strict
     */
	public boolean strict()
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


/**
 * This is mostly a direct take fom the CMIS spec.
 * The only significant chnanges are to remove left recursion which is not supported in antlr
 *
 * The top level rule for the parser
 */
query
	:	SELECT selectList fromClause whereClause? orderByClause? EOF
		-> ^(QUERY selectList fromClause whereClause? orderByClause?)
	;

	
selectList
    @init {    paraphrases.push("in select list"); }
    @after{    paraphrases.pop(); } 
	:	STAR  
		-> ^(ALL_COLUMNS)
	| 	selectSubList ( COMMA selectSubList )*  
		-> ^(COLUMNS selectSubList+) 
	;
	
	
selectSubList
	:	(valueExpression)=> valueExpression ( AS? columnName )?
		-> ^(COLUMN valueExpression columnName?)
	|	qualifier DOTSTAR 
		-> ^(ALL_COLUMNS qualifier)
	|	multiValuedColumnReference 
		-> /* No AST - MVCs are included in value expressions */
	;
	
valueExpression
	:	columnReference 
		-> columnReference
	| 	valueFunction
		-> valueFunction
	;				

columnReference
	:	( qualifier DOT )? columnName
		-> ^(COLUMN_REF columnName qualifier?)
	;
	
/*
 * This production is proteted by a dynamic predicate.
 * TODO Add look a head and perform the test
 */	
multiValuedColumnReference
	:       ( qualifier DOT )?  multiValuedColumnName
		-> ^(COLUMN_REF multiValuedColumnName qualifier?)
	;
	
valueFunction
	:	functionName=keyWordOrId LPAREN functionArgument* RPAREN
		-> ^(FUNCTION $functionName LPAREN functionArgument* RPAREN)
	;
	
functionArgument
    :   qualifier DOT columnName
    -> ^(COLUMN_REF columnName qualifier)
    |   identifier
    |   literalOrParameterName
    ;
	
qualifier
	:	(tableName) => tableName
		-> tableName
	| 	correlationName 
		-> correlationName
	;
	
fromClause
    @init {    paraphrases.push("in from"); }
    @after{    paraphrases.pop(); } 
	:	FROM tableReference
		-> tableReference
	;
	
tableReference
	:	singleTable ((joinedTable) => joinedTable)*
		-> ^(SOURCE singleTable joinedTable*)
	;
	
/*
 * Created to avoid left recursion between tableReference and joinedTable.
 */
singleTable
	:	tableName ( AS? correlationName )?
		-> ^(TABLE_REF tableName correlationName?)
	|	LPAREN joinedTables RPAREN
		-> ^(TABLE joinedTables)
	;
	
joinedTable
	:	joinType? JOIN tableReference ((joinSpecification) => joinSpecification)?
		-> ^(JOIN tableReference joinType? joinSpecification?)
	;

	
joinedTables
	:	singleTable joinedTable+
		-> ^(SOURCE singleTable joinedTable+)
	;
	
joinType 
	:	INNER
		-> INNER
	| 	LEFT OUTER?
		-> LEFT 
	;
	
joinSpecification
	:	ON LPAREN lhs=columnReference EQUALS rhs=columnReference RPAREN
		->	^(ON $lhs EQUALS $rhs)
	;
	
	
/*
 * Broken out the left recursion from the spec 
 */
whereClause
    @init {    paraphrases.push("in where"); }
    @after{    paraphrases.pop(); } 
	:	WHERE searchOrCondition
		-> searchOrCondition
	;
	
/**
 * Broken left recursion.
 */
searchOrCondition
	:	searchAndCondition (OR searchAndCondition)*
		-> ^(DISJUNCTION searchAndCondition+)
	;


/**
 * Broken left recursion.
 */	
searchAndCondition
	:	searchNotCondition (AND searchNotCondition)* 	
		-> ^(CONJUNCTION searchNotCondition+)
	;
	
searchNotCondition
	:	NOT searchTest
		-> ^(NEGATION searchTest)
	|	searchTest
		-> searchTest
	;
	
searchTest
	:	predicate
		-> predicate
	|	LPAREN searchOrCondition RPAREN
		-> searchOrCondition
	;

predicate
	:	comparisonPredicate
	|	inPredicate
	|	likePredicate
	|	nullPredicate
	| 	quantifiedComparisonPredicate
	|	quantifiedInPredicate
	|	textSearchPredicate
	|	folderPredicate
	;
	
comparisonPredicate
	:	valueExpression compOp literalOrParameterName
		-> ^(PRED_COMPARISON ANY valueExpression compOp literalOrParameterName)
	;
	
compOp	
	:	EQUALS
	|	NOTEQUALS
	|	LESSTHAN
	|	GREATERTHAN
	|	LESSTHANOREQUALS
	|	GREATERTHANOREQUALS
	;
	
literalOrParameterName
	:	literal
	|	parameterName
	;
	
literal	
	:	signedNumericLiteral
	|	characterStringLiteral
	;
	
inPredicate
	:	columnReference NOT? IN LPAREN inValueList RPAREN
		-> ^(PRED_IN ANY columnReference inValueList NOT?)
	;
	
inValueList
	:	literalOrParameterName (COMMA literalOrParameterName )*
		-> ^(LIST literalOrParameterName+)
	;
	
likePredicate
	:	columnReference NOT? LIKE characterStringLiteral
		-> ^(PRED_LIKE columnReference characterStringLiteral NOT?)
	;
	
nullPredicate
	:	( (columnReference)=> columnReference | multiValuedColumnReference) IS NULL
		-> ^(PRED_EXISTS columnReference NOT)
    |   ( (columnReference)=> columnReference | multiValuedColumnReference) IS NOT NULL
        -> ^(PRED_EXISTS columnReference)
	;
	
quantifiedComparisonPredicate
	:	literalOrParameterName compOp ANY multiValuedColumnReference
	-> ^(PRED_COMPARISON ANY literalOrParameterName compOp multiValuedColumnReference)
	;
	
	
quantifiedInPredicate
	:	ANY multiValuedColumnReference NOT? IN  LPAREN inValueList RPAREN
		-> ^(PRED_IN ANY multiValuedColumnReference inValueList NOT?)
	;
	
textSearchPredicate
	:	CONTAINS LPAREN (qualifier COMMA | COMMA)? textSearchExpression RPAREN
		-> ^(PRED_FTS textSearchExpression qualifier?)
	;
	
folderPredicate
	:	IN_FOLDER  folderPredicateArgs
		-> ^(PRED_CHILD folderPredicateArgs)
	|       IN_TREE folderPredicateArgs
		-> ^(PRED_DESCENDANT folderPredicateArgs)
	;
	
folderPredicateArgs
	:	LPAREN (qualifier COMMA | COMMA)? folderId RPAREN
		-> folderId qualifier?
	;
	
orderByClause
    @init {    paraphrases.push("in order by"); }
    @after{    paraphrases.pop(); } 
	:	ORDER BY sortSpecification ( COMMA sortSpecification )*
		-> ^(ORDER sortSpecification+)
	;
	
sortSpecification
	:	columnReference 
		-> ^(SORT_SPECIFICATION columnReference ASC)
	|	columnReference ( by=ASC | by=DESC )
		-> ^(SORT_SPECIFICATION columnReference $by)
	;
	
correlationName
	:	identifier
	;
	
/*
 * Parse time validation of the table name
 * TODO  wire up the look a head
 */
tableName
	:	identifier
		-> identifier
	;
	
columnName
	:	identifier
		-> identifier
	;
	
multiValuedColumnName 
	:	identifier
		-> identifier
	;
	
parameterName
	:	COLON identifier
		-> ^(PARAMETER identifier)
	;
	
folderId
 	:	characterStringLiteral
 		-> characterStringLiteral
 	;
 	
textSearchExpression
	:	QUOTED_STRING
	;
	
identifier
	:	ID
		-> ID
	|	DOUBLE_QUOTE keyWordOrId DOUBLE_QUOTE
		-> ^(keyWordOrId)
	;
	
signedNumericLiteral
	:	FLOATING_POINT_LITERAL
		-> ^(NUMERIC_LITERAL FLOATING_POINT_LITERAL)
	|	integerLiteral 
		-> integerLiteral
	;
	
integerLiteral
	:	DECIMAL_INTEGER_LITERAL
		-> ^(NUMERIC_LITERAL DECIMAL_INTEGER_LITERAL)
	;	
	
characterStringLiteral
	:	QUOTED_STRING
		-> ^(STRING_LITERAL QUOTED_STRING)
	;
	
	
keyWord	:	SELECT
	|	AS
	|	UPPER
	|	LOWER
	|	FROM 
	| 	JOIN 
	| 	INNER 
	| 	LEFT 
	| 	OUTER 
	| 	ON 
	| 	WHERE 
	| 	OR 
	| 	AND 
	| 	NOT 
	| 	IN 
	| 	LIKE 
	| 	IS 
	| 	NULL 
	| 	ANY 
	| 	CONTAINS	 
	| 	IN_FOLDER 
	| 	IN_TREE 
	| 	ORDER
	| 	BY 
	| 	ASC 
	| 	DESC
	| 	SCORE
	;
	
keyWordOrId 
	:	keyWord 
		-> keyWord
	|	ID
		-> ID
	;
	
/*
 * LEXER
 */	

/*
 * Quoted strings take precedence
 */


QUOTED_STRING 
    :   '\'' ( ~'\'' | '\'\'')* '\'' 
    ;
	
SELECT	:	('S'|'s')('E'|'e')('L'|'l')('E'|'e')('C'|'c')('T'|'t');
AS	:	('A'|'a')('S'|'s');
UPPER	:	('U'|'u')('P'|'p')('P'|'p')('E'|'e')('R'|'r');
LOWER	:	('L'|'l')('O'|'o')('W'|'w')('E'|'e')('R'|'r');
FROM	:	('F'|'f')('R'|'r')('O'|'o')('M'|'m');
JOIN	:	('J'|'j')('O'|'o')('I'|'i')('N'|'n');
INNER	:	('I'|'i')('N'|'n')('N'|'n')('E'|'e')('R'|'r');
LEFT	:	('L'|'l')('E'|'e')('F'|'f')('T'|'t');
OUTER	:	('O'|'o')('U'|'u')('T'|'t')('E'|'e')('R'|'r');
ON	:	('O'|'o')('N'|'n');
WHERE	:	('W'|'w')('H'|'h')('E'|'e')('R'|'r')('E'|'e');
OR	:	('O'|'o')('R'|'r');
AND	:	('A'|'a')('N'|'n')('D'|'d');
NOT	:	('N'|'n')('O'|'o')('T'|'t');
IN	:	('I'|'i')('N'|'n');
LIKE	:	('L'|'l')('I'|'i')('K'|'k')('E'|'e');
IS	:	('I'|'i')('S'|'s');
NULL	:	('N'|'n')('U'|'u')('L'|'l')('L'|'l');
ANY	:	('A'|'a')('N'|'n')('Y'|'y');
CONTAINS:	('C'|'c')('O'|'o')('N'|'n')('T'|'t')('A'|'a')('I'|'i')('N'|'n')('S'|'s');
IN_FOLDER
	:	('I'|'i')('N'|'n')'_'('F'|'f')('O'|'o')('L'|'l')('D'|'d')('E'|'e')('R'|'r');
IN_TREE	:	('I'|'i')('N'|'n')'_'('T'|'t')('R'|'r')('E'|'e')('E'|'e');
ORDER	:	('O'|'o')('R'|'r')('D'|'d')('E'|'e')('R'|'r');
BY	:	('B'|'b')('Y'|'y');
ASC	:	('A'|'a')('S'|'s')('C'|'c');
DESC	:	('D'|'d')('E'|'e')('S'|'s')('C'|'c');
SCORE	:	('S'|'s')('C'|'c')('O'|'o')('R'|'r')('E'|'e');
LPAREN	:	'(' ;
RPAREN	:	')' ;
STAR	:	'*' ;
COMMA	:	',' ;
DOTSTAR	:	'.*' ;
DOT	:	'.' ;
DOTDOT	:	'..' ;
EQUALS 	:	'=' ;
TILDA	:	'~' ;
NOTEQUALS
	:	'<>' ;
GREATERTHAN
	:	'>' ;
LESSTHAN 
	:	'<' ;
GREATERTHANOREQUALS
	:	'>=' ;
LESSTHANOREQUALS
	:	'<=' ;
COLON	:	':' ;

DOUBLE_QUOTE
	:	'"'
	;


/*
 * Decimal adapted from the Java spec 
 */
DECIMAL_INTEGER_LITERAL
        :	( PLUS | MINUS )? DECIMAL_NUMERAL
        ;

/*
 * Floating point adapted from the Java spec 
 */
FLOATING_POINT_LITERAL
	:	( PLUS | MINUS )? DIGIT+ DOT DIGIT* EXPONENT?
	|	( PLUS | MINUS )? DOT DIGIT+ EXPONENT?
	|	( PLUS | MINUS )? DIGIT+	EXPONENT
	;
        
        
/**
 * We should support _x????_ encoding for invalid sql characters 
 */        
ID	:	('a'..'z'|'A'..'Z'|'_')('a'..'z'|'A'..'Z'|'0'..'9'|'_'|':'|'$'|'#')* ;

WS	:	( ' ' | '\t' | '\r' | '\n' )+ { $channel = HIDDEN; } ;
  

/**   
 * Fragments for decimal
 */
        
fragment
DECIMAL_NUMERAL
	:	ZERO_DIGIT 
	|	NON_ZERO_DIGIT DIGIT*	
	;
fragment
DIGIT	:	ZERO_DIGIT | NON_ZERO_DIGIT ;
fragment
ZERO_DIGIT 	
	:	'0' ;
fragment
NON_ZERO_DIGIT 
	:	'1'..'9' ;
fragment
PLUS	:	'+' ;
fragment
MINUS	:	'-' ;

fragment
E	:	('e' | 'E') ;



	
/*
 * Fragments for floating point
 */
	
fragment
EXPONENT
	:	E SIGNED_INTEGER
	;
fragment
SIGNED_INTEGER
	:	(PLUS | MINUS)? DIGIT+	
	;
	


