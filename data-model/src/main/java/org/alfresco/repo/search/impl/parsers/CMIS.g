/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
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
        output = AST;
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
        DATETIME_LITERAL;
        BOOLEAN_LITERAL;
        SINGLE_VALUED_PROPERTY;
}

@lexer::header
{
    package org.alfresco.repo.search.impl.parsers;
    import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;
}

@header
{
    package org.alfresco.repo.search.impl.parsers;
    import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;
}
/*
 * Instance methods and properties for the parser.
 * Realisations of the parser should over-ride these as required
 */


@members
{
    private Stack<String> paraphrases = new Stack<String>();
    
    private boolean strict = false;

    /**
     * CMIS strict
     */
    public boolean strict()
    {
        return strict;
    }
	
    public void setStrict(boolean strict)
    {
        this.strict = strict;
    }
        
    protected Object recoverFromMismatchedToken(IntStream input, int ttype, BitSet follow) throws RecognitionException
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
        String msg = e.getMessage();
        if ( e instanceof UnwantedTokenException ) 
	    {
            UnwantedTokenException ute = (UnwantedTokenException)e;
            String tokenName="<unknown>";
            if ( ute.expecting== Token.EOF ) 
            {
                tokenName = "EOF";
            }
            else 
            {
                tokenName = tokenNames[ute.expecting];
            }
            msg = "extraneous input " + getTokenErrorDisplay(ute.getUnexpectedToken())
                + " expecting "+tokenName;
        }
        else if ( e instanceof MissingTokenException ) 
        {
            MissingTokenException mte = (MissingTokenException)e;
            String tokenName="<unknown>";
            if ( mte.expecting== Token.EOF ) 
            {
                tokenName = "EOF";
            }
            else 
            {
                tokenName = tokenNames[mte.expecting];
            }
            msg = "missing " + tokenName+" at " + getTokenErrorDisplay(e.token)
                + "  (" + getLongTokenErrorDisplay(e.token) +")";
        }
        else if ( e instanceof MismatchedTokenException ) 
        {
            MismatchedTokenException mte = (MismatchedTokenException)e;
            String tokenName="<unknown>";
            if ( mte.expecting== Token.EOF ) 
            {
                tokenName = "EOF";
            }
            else
            {
                tokenName = tokenNames[mte.expecting];
            }
            msg = "mismatched input " + getTokenErrorDisplay(e.token)
                + " expecting " + tokenName +"  (" + getLongTokenErrorDisplay(e.token) + ")";
        }
        else if ( e instanceof MismatchedTreeNodeException ) 
        {
            MismatchedTreeNodeException mtne = (MismatchedTreeNodeException)e;
            String tokenName="<unknown>";
            if ( mtne.expecting==Token.EOF )  
            {
                tokenName = "EOF";
            }
            else 
            {
                tokenName = tokenNames[mtne.expecting];
            }
            msg = "mismatched tree node: " + mtne.node + " expecting " + tokenName;
        }
        else if ( e instanceof NoViableAltException ) 
        {
            NoViableAltException nvae = (NoViableAltException)e;
            msg = "no viable alternative at input " + getTokenErrorDisplay(e.token)
                + "\n\t (decision=" + nvae.decisionNumber
                + " state " + nvae.stateNumber + ")" 
                + " decision=<<" + nvae.grammarDecisionDescription + ">>";
        }
        else if ( e instanceof EarlyExitException ) 
        {
            //EarlyExitException eee = (EarlyExitException)e;
            // for development, can add "(decision="+eee.decisionNumber+")"
            msg = "required (...)+ loop did not match anything at input " + getTokenErrorDisplay(e.token);
        }
	    else if ( e instanceof MismatchedSetException ) 
	    {
	        MismatchedSetException mse = (MismatchedSetException)e;
	        msg = "mismatched input " + getTokenErrorDisplay(e.token)
                + " expecting set " + mse.expecting;
        }
        else if ( e instanceof MismatchedNotSetException ) 
        {
            MismatchedNotSetException mse = (MismatchedNotSetException)e;
            msg = "mismatched input " + getTokenErrorDisplay(e.token)
                + " expecting set " + mse.expecting;
        }
        else if ( e instanceof FailedPredicateException ) 
        {
            FailedPredicateException fpe = (FailedPredicateException)e;
            msg = "rule " + fpe.ruleName + " failed predicate: {" + fpe.predicateText + "}?";
        }
		
        if(paraphrases.size() > 0)
        {
            String paraphrase = (String)paraphrases.peek();
            msg = msg+" "+paraphrase;
        }
        return msg +"\n\t"+stack;
    }
	
    public String getLongTokenErrorDisplay(Token t)
    {
        return t.toString();
    }
    

    public String getErrorString(RecognitionException e)
    {
        String hdr = getErrorHeader(e);
        String msg = getErrorMessage(e, this.getTokenNames());
        return hdr+" "+msg;
    }
}

@rulecatch
{
    catch(RecognitionException e)
    {
        throw new CmisInvalidArgumentException(getErrorString(e), e);
    }
}

@lexer::members
{
    public Token nextToken() {
        while (true) 
        {
            state.token = null;
            state.channel = Token.DEFAULT_CHANNEL;
            state.tokenStartCharIndex = input.index();
            state.tokenStartCharPositionInLine = input.getCharPositionInLine();
            state.tokenStartLine = input.getLine();
            state.text = null;
            if ( input.LA(1)==CharStream.EOF ) 
            {
                return getEOFToken();
            }
            try 
            {
                mTokens();
                if ( state.token==null ) 
                {
                    emit();
                }
                else if ( state.token==Token.SKIP_TOKEN ) 
                {
                    continue;
                }
                return state.token;
            }
            catch (RecognitionException re) 
            {
                throw new CmisInvalidArgumentException(getErrorString(re), re);
            }
        }
    }
    
    public String getErrorString(RecognitionException e)
    {
        String hdr = getErrorHeader(e);
        String msg = getErrorMessage(e, this.getTokenNames());
        return hdr+" "+msg;
    }
}

/**
 * This is mostly a direct take fom the CMIS spec.
 * The only significant chnanges are to remove left recursion which is not supported in antlr
 *
 * The top level rule for the parser
 */
query
@init
{
    paraphrases.push("in query"); 
}
@after
{
    paraphrases.pop(); 
}
        :
        SELECT selectList fromClause whereClause? orderByClause? EOF
                ->
                        ^(QUERY selectList fromClause whereClause? orderByClause?)
        ;

selectList
@init
{
    paraphrases.push("in select list"); 
}
@after
{
    paraphrases.pop(); 
}
        :
        STAR
                ->
                        ^(ALL_COLUMNS)
        | selectSubList (COMMA selectSubList)*
                ->
                        ^(COLUMNS selectSubList+)
        ;

selectSubList
        :
        valueExpression (AS? columnName)?
                ->
                        ^(COLUMN valueExpression columnName?)
        | qualifier DOTSTAR
                ->
                        ^(ALL_COLUMNS qualifier)
        ;

valueExpression
@init
{
    paraphrases.push("in value expression"); 
}
@after
{
    paraphrases.pop(); 
}
        :
        columnReference
                -> columnReference
        | valueFunction
                -> valueFunction
        ;

columnReference
@init
{
    paraphrases.push("in column reference"); 
}
@after
{
    paraphrases.pop(); 
}
        :
        (qualifier DOT)? columnName
                ->
                        ^(COLUMN_REF columnName qualifier?)
        ;

valueFunction
@init
{
    paraphrases.push("in function"); 
}
@after
{
    paraphrases.pop(); 
}
        :
        cmisFunctionName=cmisFunction LPAREN functionArgument* RPAREN
                ->
                        ^(FUNCTION $cmisFunctionName LPAREN functionArgument* RPAREN)
        | {strict == false}?=> functionName=keyWordOrId LPAREN functionArgument* RPAREN
                ->
                        ^(FUNCTION $functionName LPAREN functionArgument* RPAREN)
        ;

functionArgument
        :
        qualifier DOT columnName
                ->
                        ^(COLUMN_REF columnName qualifier)
        | identifier
        | literalOrParameterName
        ;

qualifier
        :
        (tableName) => tableName
                -> tableName
        | correlationName
                -> correlationName
        ;

fromClause
@init
{
    paraphrases.push("in fromClause"); 
}
@after
{
    paraphrases.pop(); 
}
        :
        FROM tableReference
                -> tableReference
        ;

tableReference
@init
{
    paraphrases.push("in tableReference"); 
}
@after
{
    paraphrases.pop(); 
}
        :
        singleTable joinedTable*
                ->
                        ^(SOURCE singleTable joinedTable*)
        ;
/*
 * Created to avoid left recursion between tableReference and joinedTable.
 */


singleTable
@init
{
    paraphrases.push("in singleTable"); 
}
@after
{
    paraphrases.pop(); 
}
        :
        simpleTable
                -> simpleTable
        | complexTable
                ->
                        ^(TABLE complexTable)
        ;

simpleTable
@init
{
    paraphrases.push("in simpleTable"); 
}
@after
{
    paraphrases.pop(); 
}
        :
        tableName (AS? correlationName)?
                ->
                        ^(TABLE_REF tableName correlationName?)
        ;

joinedTable
@init
{
    paraphrases.push("in joinedTable"); 
}
@after
{
    paraphrases.pop(); 
}
        :
        joinType? JOIN tableReference joinSpecification
                ->
                        ^(JOIN tableReference joinType? joinSpecification)
        ;

complexTable
@init
{
    paraphrases.push("in complexTable"); 
}
@after
{
    paraphrases.pop(); 
}
        :
        (LPAREN singleTable joinedTable+ RPAREN) => LPAREN singleTable joinedTable+ RPAREN
                ->
                        ^(SOURCE singleTable joinedTable+)
        | LPAREN complexTable RPAREN
                -> complexTable
        ;

joinType
        :
        INNER
                -> INNER
        | LEFT OUTER?
                -> LEFT
        ;

joinSpecification
@init
{
    paraphrases.push("in join condition"); 
}
@after
{
    paraphrases.pop(); 
}
        :
        ON lhs=columnReference EQUALS rhs=columnReference
                ->
                        ^(ON $lhs EQUALS $rhs)
        ;
/*
 * Broken out the left recursion from the spec 
 */


whereClause
@init
{
    paraphrases.push("in where"); 
}
@after
{
    paraphrases.pop(); 
}
        :
        WHERE searchOrCondition
                -> searchOrCondition
        ;

/**
 * Broken left recursion.
 */
searchOrCondition
        :
        searchAndCondition (OR searchAndCondition)*
                ->
                        ^(DISJUNCTION searchAndCondition+)
        ;

/**
 * Broken left recursion.
 */
searchAndCondition
        :
        searchNotCondition (AND searchNotCondition)*
                ->
                        ^(CONJUNCTION searchNotCondition+)
        ;

searchNotCondition
        :
        NOT searchTest
                ->
                        ^(NEGATION searchTest)
        | searchTest
                -> searchTest
        ;

searchTest
        :
        predicate
                -> predicate
        | LPAREN searchOrCondition RPAREN
                -> searchOrCondition
        ;

predicate
        :
        comparisonPredicate
        | inPredicate
        | likePredicate
        | nullPredicate
        | quantifiedComparisonPredicate
        | quantifiedInPredicate
        | textSearchPredicate
        | folderPredicate
        ;

comparisonPredicate
        :
        valueExpression compOp literalOrParameterName
                ->
                        ^(PRED_COMPARISON SINGLE_VALUED_PROPERTY valueExpression compOp literalOrParameterName)
        ;

compOp
        :
        EQUALS
        | NOTEQUALS
        | LESSTHAN
        | GREATERTHAN
        | LESSTHANOREQUALS
        | GREATERTHANOREQUALS
        ;

literalOrParameterName
        :
        literal
        | {strict == false}?=> parameterName
        ;

literal
        :
        signedNumericLiteral
        | characterStringLiteral
        | booleanLiteral
        | datetimeLiteral
        ;

inPredicate
        :
        columnReference NOT? IN LPAREN inValueList RPAREN
                ->
                        ^(PRED_IN SINGLE_VALUED_PROPERTY columnReference inValueList NOT?)
        ;

inValueList
        :
        literalOrParameterName (COMMA literalOrParameterName)*
                ->
                        ^(LIST literalOrParameterName+)
        ;

likePredicate
        :
        columnReference NOT? LIKE characterStringLiteral
                ->
                        ^(PRED_LIKE columnReference characterStringLiteral NOT?)
        ;

nullPredicate
        :
        columnReference IS NULL
                ->
                        ^(PRED_EXISTS columnReference NOT)
        | columnReference IS NOT NULL
                ->
                        ^(PRED_EXISTS columnReference)
        ;

quantifiedComparisonPredicate
        :
        literalOrParameterName compOp ANY columnReference
                ->
                        ^(PRED_COMPARISON ANY literalOrParameterName compOp columnReference)
        ;

quantifiedInPredicate
        :
        ANY columnReference NOT? IN LPAREN inValueList RPAREN
                ->
                        ^(PRED_IN ANY columnReference inValueList NOT?)
        ;

textSearchPredicate
        :
        CONTAINS LPAREN (qualifier COMMA)? textSearchExpression RPAREN
                ->
                        ^(PRED_FTS textSearchExpression qualifier?)
        ;

folderPredicate
        :
        IN_FOLDER folderPredicateArgs
                ->
                        ^(PRED_CHILD folderPredicateArgs)
        | IN_TREE folderPredicateArgs
                ->
                        ^(PRED_DESCENDANT folderPredicateArgs)
        ;

folderPredicateArgs
        :
        LPAREN (qualifier COMMA)? folderId RPAREN
                -> folderId qualifier?
        ;

orderByClause
@init
{
    paraphrases.push("in order by"); 
}
@after
{
    paraphrases.pop(); 
}
        :
        ORDER BY sortSpecification (COMMA sortSpecification)*
                ->
                        ^(ORDER sortSpecification+)
        ;

sortSpecification
        :
        columnReference
                ->
                        ^(SORT_SPECIFICATION columnReference ASC)
        | columnReference
        (
                by=ASC
                | by=DESC
        )
                ->
                        ^(SORT_SPECIFICATION columnReference $by)
        ;

correlationName
        :
        identifier
        ;
/*
 * Parse time validation of the table name
 * TODO  wire up the look a head
 */


tableName
        :
        identifier
                -> identifier
        ;

columnName
        :
        identifier
                -> identifier
        ;

parameterName
        :
        COLON identifier
                ->
                        ^(PARAMETER identifier)
        ;

folderId
        :
        characterStringLiteral
                -> characterStringLiteral
        ;

textSearchExpression
        :
        QUOTED_STRING
        ;

identifier
        :
        ID
                -> ID
        | {strict == false}?=> DOUBLE_QUOTE keyWordOrId DOUBLE_QUOTE
                ->
                        ^(keyWordOrId)
        ;

signedNumericLiteral
        :
        FLOATING_POINT_LITERAL
                ->
                        ^(NUMERIC_LITERAL FLOATING_POINT_LITERAL)
        | integerLiteral
                -> integerLiteral
        ;

integerLiteral
        :
        DECIMAL_INTEGER_LITERAL
                ->
                        ^(NUMERIC_LITERAL DECIMAL_INTEGER_LITERAL)
        ;

booleanLiteral
        :
        TRUE
                ->
                        ^(BOOLEAN_LITERAL TRUE)
        | FALSE
                ->
                        ^(BOOLEAN_LITERAL FALSE)
        ;

datetimeLiteral
        :
        TIMESTAMP QUOTED_STRING
                ->
                        ^(DATETIME_LITERAL QUOTED_STRING)
        ;

characterStringLiteral
        :
        QUOTED_STRING
                ->
                        ^(STRING_LITERAL QUOTED_STRING)
        ;

keyWord
        :
        SELECT
        | AS
        | FROM
        | JOIN
        | INNER
        | LEFT
        | OUTER
        | ON
        | WHERE
        | OR
        | AND
        | NOT
        | IN
        | LIKE
        | IS
        | NULL
        | ANY
        | CONTAINS
        | IN_FOLDER
        | IN_TREE
        | ORDER
        | BY
        | ASC
        | DESC
        | TIMESTAMP
        | TRUE
        | FALSE
        | cmisFunction
        ;

cmisFunction
        :
        SCORE
                -> SCORE
        ;

keyWordOrId
        :
        keyWord
                -> keyWord
        | ID
                -> ID
        ;
/*
 * LEXER
 */
/*
 * Quoted strings take precedence
 */



QUOTED_STRING
        :
        '\''
        (
                ~('\'' | '\\')
                | '\\' .
        )*
        '\''
        ;

SELECT
        :
        (
                'S'
                | 's'
        )
        (
                'E'
                | 'e'
        )
        (
                'L'
                | 'l'
        )
        (
                'E'
                | 'e'
        )
        (
                'C'
                | 'c'
        )
        (
                'T'
                | 't'
        )
        ;

AS
        :
        (
                'A'
                | 'a'
        )
        (
                'S'
                | 's'
        )
        ;

FROM
        :
        (
                'F'
                | 'f'
        )
        (
                'R'
                | 'r'
        )
        (
                'O'
                | 'o'
        )
        (
                'M'
                | 'm'
        )
        ;

JOIN
        :
        (
                'J'
                | 'j'
        )
        (
                'O'
                | 'o'
        )
        (
                'I'
                | 'i'
        )
        (
                'N'
                | 'n'
        )
        ;

INNER
        :
        (
                'I'
                | 'i'
        )
        (
                'N'
                | 'n'
        )
        (
                'N'
                | 'n'
        )
        (
                'E'
                | 'e'
        )
        (
                'R'
                | 'r'
        )
        ;

LEFT
        :
        (
                'L'
                | 'l'
        )
        (
                'E'
                | 'e'
        )
        (
                'F'
                | 'f'
        )
        (
                'T'
                | 't'
        )
        ;

OUTER
        :
        (
                'O'
                | 'o'
        )
        (
                'U'
                | 'u'
        )
        (
                'T'
                | 't'
        )
        (
                'E'
                | 'e'
        )
        (
                'R'
                | 'r'
        )
        ;

ON
        :
        (
                'O'
                | 'o'
        )
        (
                'N'
                | 'n'
        )
        ;

WHERE
        :
        (
                'W'
                | 'w'
        )
        (
                'H'
                | 'h'
        )
        (
                'E'
                | 'e'
        )
        (
                'R'
                | 'r'
        )
        (
                'E'
                | 'e'
        )
        ;

OR
        :
        (
                'O'
                | 'o'
        )
        (
                'R'
                | 'r'
        )
        ;

AND
        :
        (
                'A'
                | 'a'
        )
        (
                'N'
                | 'n'
        )
        (
                'D'
                | 'd'
        )
        ;

NOT
        :
        (
                'N'
                | 'n'
        )
        (
                'O'
                | 'o'
        )
        (
                'T'
                | 't'
        )
        ;

IN
        :
        (
                'I'
                | 'i'
        )
        (
                'N'
                | 'n'
        )
        ;

LIKE
        :
        (
                'L'
                | 'l'
        )
        (
                'I'
                | 'i'
        )
        (
                'K'
                | 'k'
        )
        (
                'E'
                | 'e'
        )
        ;

IS
        :
        (
                'I'
                | 'i'
        )
        (
                'S'
                | 's'
        )
        ;

NULL
        :
        (
                'N'
                | 'n'
        )
        (
                'U'
                | 'u'
        )
        (
                'L'
                | 'l'
        )
        (
                'L'
                | 'l'
        )
        ;

ANY
        :
        (
                'A'
                | 'a'
        )
        (
                'N'
                | 'n'
        )
        (
                'Y'
                | 'y'
        )
        ;

CONTAINS
        :
        (
                'C'
                | 'c'
        )
        (
                'O'
                | 'o'
        )
        (
                'N'
                | 'n'
        )
        (
                'T'
                | 't'
        )
        (
                'A'
                | 'a'
        )
        (
                'I'
                | 'i'
        )
        (
                'N'
                | 'n'
        )
        (
                'S'
                | 's'
        )
        ;

IN_FOLDER
        :
        (
                'I'
                | 'i'
        )
        (
                'N'
                | 'n'
        )
        '_'
        (
                'F'
                | 'f'
        )
        (
                'O'
                | 'o'
        )
        (
                'L'
                | 'l'
        )
        (
                'D'
                | 'd'
        )
        (
                'E'
                | 'e'
        )
        (
                'R'
                | 'r'
        )
        ;

IN_TREE
        :
        (
                'I'
                | 'i'
        )
        (
                'N'
                | 'n'
        )
        '_'
        (
                'T'
                | 't'
        )
        (
                'R'
                | 'r'
        )
        (
                'E'
                | 'e'
        )
        (
                'E'
                | 'e'
        )
        ;

ORDER
        :
        (
                'O'
                | 'o'
        )
        (
                'R'
                | 'r'
        )
        (
                'D'
                | 'd'
        )
        (
                'E'
                | 'e'
        )
        (
                'R'
                | 'r'
        )
        ;

BY
        :
        (
                'B'
                | 'b'
        )
        (
                'Y'
                | 'y'
        )
        ;

ASC
        :
        (
                'A'
                | 'a'
        )
        (
                'S'
                | 's'
        )
        (
                'C'
                | 'c'
        )
        ;

DESC
        :
        (
                'D'
                | 'd'
        )
        (
                'E'
                | 'e'
        )
        (
                'S'
                | 's'
        )
        (
                'C'
                | 'c'
        )
        ;

TIMESTAMP
        :
        (
                'T'
                | 't'
        )
        (
                'I'
                | 'i'
        )
        (
                'M'
                | 'm'
        )
        (
                'E'
                | 'e'
        )
        (
                'S'
                | 's'
        )
        (
                'T'
                | 't'
        )
        (
                'A'
                | 'a'
        )
        (
                'M'
                | 'm'
        )
        (
                'P'
                | 'p'
        )
        ;

TRUE
        :
        (
                'T'
                | 't'
        )
        (
                'R'
                | 'r'
        )
        (
                'U'
                | 'u'
        )
        (
                'E'
                | 'e'
        )
        ;

FALSE
        :
        (
                'F'
                | 'f'
        )
        (
                'A'
                | 'a'
        )
        (
                'L'
                | 'l'
        )
        (
                'S'
                | 's'
        )
        (
                'E'
                | 'e'
        )
        ;

SCORE
        :
        (
                'S'
                | 's'
        )
        (
                'C'
                | 'c'
        )
        (
                'O'
                | 'o'
        )
        (
                'R'
                | 'r'
        )
        (
                'E'
                | 'e'
        )
        ;

LPAREN
        :
        '('
        ;

RPAREN
        :
        ')'
        ;

STAR
        :
        '*'
        ;

COMMA
        :
        ','
        ;

DOTSTAR
        :
        '.*'
        ;

DOT
        :
        '.'
        ;

DOTDOT
        :
        '..'
        ;

EQUALS
        :
        '='
        ;

TILDA
        :
        '~'
        ;

NOTEQUALS
        :
        '<>'
        ;

GREATERTHAN
        :
        '>'
        ;

LESSTHAN
        :
        '<'
        ;

GREATERTHANOREQUALS
        :
        '>='
        ;

LESSTHANOREQUALS
        :
        '<='
        ;

COLON
        :
        ':'
        ;

DOUBLE_QUOTE
        :
        '"'
        ;
/*
 * Decimal adapted from the Java spec 
 */


DECIMAL_INTEGER_LITERAL
        :
        (
                PLUS
                | MINUS
        )?
        DECIMAL_NUMERAL
        ;
/*
 * Floating point adapted from the Java spec 
 */


FLOATING_POINT_LITERAL
        :
        (
                PLUS
                | MINUS
        )?
        DIGIT+ DOT DIGIT* EXPONENT?
        |
        (
                PLUS
                | MINUS
        )?
        DOT DIGIT+ EXPONENT?
        |
        (
                PLUS
                | MINUS
        )?
        DIGIT+ EXPONENT
        ;

/**
 * We should support _x????_ encoding for invalid sql characters 
 */
ID
        :
        (
                'a'..'z'
                | 'A'..'Z'
                | '_'
        )
        (
                'a'..'z'
                | 'A'..'Z'
                | '0'..'9'
                | '_'
                | ':'
                | '$'
                | '#'
        )*
        ;

WS
        :
        (
                ' '
                | '\t'
                | '\r'
                | '\n'
        )+
        { $channel = HIDDEN; }
        ;

/**   
 * Fragments for decimal
 */
fragment
DECIMAL_NUMERAL
        :
        ZERO_DIGIT
        | NON_ZERO_DIGIT DIGIT*
        ;

fragment
DIGIT
        :
        ZERO_DIGIT
        | NON_ZERO_DIGIT
        ;

fragment
ZERO_DIGIT
        :
        '0'
        ;

fragment
NON_ZERO_DIGIT
        :
        '1'..'9'
        ;

fragment
PLUS
        :
        '+'
        ;

fragment
MINUS
        :
        '-'
        ;

fragment
E
        :
        (
                'e'
                | 'E'
        )
        ;
/*
 * Fragments for floating point
 */


fragment
EXPONENT
        :
        E SIGNED_INTEGER
        ;

fragment
SIGNED_INTEGER
        :
        (
                PLUS
                | MINUS
        )?
        DIGIT+
        ;
