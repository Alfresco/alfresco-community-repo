/*
 * Copyright (C) 2005-2015 Alfresco Software Limited.
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
 * Parser for the Alfresco full text query language.
 * It may be used stand-alone or embedded, for example, in CMIS SQL contains() 
 *
 */

grammar FTS;

options
{
        output    = AST;
        backtrack = false;
        memoize   = false; 
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
        EXACT_PHRASE;
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
        FG_EXACT_PHRASE;
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


@lexer::header
{
package org.alfresco.repo.search.impl.parsers;
}

@header
{
package org.alfresco.repo.search.impl.parsers;
}
/*
 * Embeded java to control the default connective when not specified.
 *
 * Do not support recover from errors
 *
 * Add extra detail to teh error message
 */


@members
{
    public enum Mode
    {
        CMIS, DEFAULT_CONJUNCTION, DEFAULT_DISJUNCTION
    }
    
    private Stack<String> paraphrases = new Stack<String>();
    
    private boolean defaultFieldConjunction = true;
    
    private Mode mode = Mode.DEFAULT_CONJUNCTION;
    
    public Mode getMode()
    {
       return mode;
    }
    
    public void setMode(Mode mode)
    {
       this.mode = mode;
    }
    
    public boolean defaultFieldConjunction()
    {
       return defaultFieldConjunction;
    }
    
    public void setDefaultFieldConjunction(boolean defaultFieldConjunction)
    {
       this.defaultFieldConjunction = defaultFieldConjunction;
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


@lexer::members
{
List tokens = new ArrayList();
public void emit(Token token) {
        state.token = token;
        tokens.add(token);
}
public Token nextToken() {
        nextTokenImpl();
        if ( tokens.size()==0 ) {
            return getEOFToken();
        }
        return (Token)tokens.remove(0);
}

public Token nextTokenImpl() {
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
                throw new FTSQueryException(getErrorString(re), re);
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


/*
 * Top level query
 */


ftsQuery
        :
        ftsDisjunction EOF
                -> ftsDisjunction
        ;
/*
 * "OR"
 * As SQL, OR has lower precedence than AND
 */


ftsDisjunction
        :
          {getMode() == Mode.CMIS}? cmisExplicitDisjunction
        | {getMode() == Mode.DEFAULT_CONJUNCTION}? ftsExplicitDisjunction
        | {getMode() == Mode.DEFAULT_DISJUNCTION}? ftsImplicitDisjunction
        ;

ftsExplicitDisjunction
        :
        ftsImplicitConjunction (or ftsImplicitConjunction)*
                ->
                        ^(DISJUNCTION ftsImplicitConjunction+)
        ;

cmisExplicitDisjunction
        :
        cmisConjunction (or cmisConjunction)*
                ->
                        ^(DISJUNCTION cmisConjunction+)
        ;

ftsImplicitDisjunction
        :
        (or? ftsExplicitConjunction)+
                ->
                        ^(DISJUNCTION ftsExplicitConjunction+)
        ;
/*
 * "AND"
 */


ftsExplicitConjunction
        :
        ftsPrefixed (and ftsPrefixed)*
                ->
                        ^(CONJUNCTION ftsPrefixed+)
        ;

ftsImplicitConjunction
        :
        (and? ftsPrefixed)+
                ->
                        ^(CONJUNCTION ftsPrefixed+)
        ;

cmisConjunction
        :
        cmisPrefixed+
                ->
                        ^(CONJUNCTION cmisPrefixed+)
        ;
/*
 * Additional info around query compoents 
 * - negation, default, mandatory, optional, exclude and boost
 * These options control how individual elements are embedded in OR and AND
 * and how matches affect the overall score.
 */


ftsPrefixed
        :
        (not) => not ftsTest boost?
                ->
                        ^(NEGATION ftsTest boost?)
        | ftsTest boost?
                ->
                        ^(DEFAULT ftsTest boost?)
        | PLUS ftsTest boost?
                ->
                        ^(MANDATORY ftsTest boost?)
        | BAR ftsTest boost?
                ->
                        ^(OPTIONAL ftsTest boost?)
        | MINUS ftsTest boost?
                ->
                        ^(EXCLUDE ftsTest boost?)
        ;

cmisPrefixed
        :
        cmisTest
                ->
                        ^(DEFAULT cmisTest)
        | MINUS cmisTest
                ->
                        ^(EXCLUDE cmisTest)
        ;
/*
 * Individual query components
 */

ftsTest
        :
           (ftsFieldGroupProximity) => ftsFieldGroupProximity
                ->
                        ^(PROXIMITY ftsFieldGroupProximity)
        |
           (ftsRange) => ftsRange
                ->
                        ^(RANGE ftsRange)
        |  
           (ftsFieldGroup) => ftsFieldGroup
                -> ftsFieldGroup
        |
           (ftsTermOrPhrase) => ftsTermOrPhrase
        |
           (ftsExactTermOrPhrase) => ftsExactTermOrPhrase
        | 
           (ftsTokenisedTermOrPhrase) => ftsTokenisedTermOrPhrase
        
        
        |  LPAREN ftsDisjunction RPAREN
                -> ftsDisjunction
        |  template
                -> template
        ;

cmisTest
        :
        cmisTerm
                ->
                        ^(TERM cmisTerm)
        | cmisPhrase
                ->
                        ^(PHRASE cmisPhrase)
        ;

template
        :
        PERCENT tempReference
                ->
                        ^(TEMPLATE tempReference)
        | PERCENT LPAREN (tempReference COMMA?)+ RPAREN
                ->
                        ^(TEMPLATE tempReference+)
        ;

fuzzy
        :
        TILDA number
                ->
                        ^(FUZZY number)
        ;

slop
        :
        TILDA DECIMAL_INTEGER_LITERAL
                ->
                        ^(FUZZY DECIMAL_INTEGER_LITERAL)
        ;

boost
        :
        CARAT number
                ->
                        ^(BOOST number)
        ;

ftsTermOrPhrase
        :
        (fieldReference COLON) => fieldReference COLON
        (
                FTSPHRASE ((slop)=> slop)?
                -> ^(PHRASE FTSPHRASE fieldReference slop?)
                |
                ftsWord ((fuzzy) => fuzzy)?
                -> ^(TERM ftsWord fieldReference fuzzy?)
        )
        |
        FTSPHRASE ((slop)=> slop)?
                -> ^(PHRASE FTSPHRASE slop?)
        | 
        ftsWord ((fuzzy) => fuzzy)?
                -> ^(TERM ftsWord fuzzy?)
        ;
        
        
ftsExactTermOrPhrase
        :
        EQUALS
        (
        (fieldReference COLON) => fieldReference COLON
        (
                FTSPHRASE ((slop)=> slop)?
                -> ^(EXACT_PHRASE FTSPHRASE fieldReference slop?)
                |
                ftsWord ((fuzzy) => fuzzy)?
                -> ^(EXACT_TERM ftsWord fieldReference fuzzy?)
        )
        |
        FTSPHRASE ((slop)=> slop)?
                -> ^(EXACT_PHRASE FTSPHRASE slop?)
        | 
        ftsWord ((fuzzy) => fuzzy)?
                -> ^(EXACT_TERM ftsWord fuzzy?)
        )
        ;
        

ftsTokenisedTermOrPhrase
        :
        TILDA
        (
        (fieldReference COLON) => fieldReference COLON
        (
                FTSPHRASE ((slop)=> slop)?
                -> ^(PHRASE FTSPHRASE fieldReference slop?)
                |
                ftsWord ((fuzzy) => fuzzy)?
                -> ^(TERM ftsWord fieldReference fuzzy?)
        )
        |
        FTSPHRASE ((slop)=> slop)?
                -> ^(PHRASE FTSPHRASE slop?)
        | 
        ftsWord ((fuzzy) => fuzzy)?
                -> ^(TERM ftsWord fuzzy?)
        )
        ;


cmisTerm
        :
        ftsWord
                -> ftsWord
        ;


cmisPhrase
        :
        FTSPHRASE
                -> FTSPHRASE
        ;


ftsRange
        :
        (fieldReference COLON)? ftsFieldGroupRange
                -> ftsFieldGroupRange fieldReference?
        ;

ftsFieldGroup
        :
        fieldReference COLON LPAREN ftsFieldGroupDisjunction RPAREN
                ->
                        ^(FIELD_GROUP fieldReference ftsFieldGroupDisjunction)
        ;

ftsFieldGroupDisjunction
        :
        {defaultFieldConjunction() == true}? ftsFieldGroupExplicitDisjunction
        | {defaultFieldConjunction() == false}? ftsFieldGroupImplicitDisjunction
        ;

ftsFieldGroupExplicitDisjunction
        :
        ftsFieldGroupImplicitConjunction (or ftsFieldGroupImplicitConjunction)*
                ->
                        ^(FIELD_DISJUNCTION ftsFieldGroupImplicitConjunction+)
        ;

ftsFieldGroupImplicitDisjunction
        :
        (or? ftsFieldGroupExplicitConjunction)+
                ->
                        ^(FIELD_DISJUNCTION ftsFieldGroupExplicitConjunction+)
        ;
/*
 * "AND"
 */


ftsFieldGroupExplicitConjunction
        :
        ftsFieldGroupPrefixed (and ftsFieldGroupPrefixed)*
                ->
                        ^(FIELD_CONJUNCTION ftsFieldGroupPrefixed+)
        ;

ftsFieldGroupImplicitConjunction
        :
        (and? ftsFieldGroupPrefixed)+
                ->
                        ^(FIELD_CONJUNCTION ftsFieldGroupPrefixed+)
        ;

ftsFieldGroupPrefixed
        :
        (not) => not ftsFieldGroupTest boost?
                ->
                        ^(FIELD_NEGATION ftsFieldGroupTest boost?)
        | ftsFieldGroupTest boost?
                ->
                        ^(FIELD_DEFAULT ftsFieldGroupTest boost?)
        | PLUS ftsFieldGroupTest boost?
                ->
                        ^(FIELD_MANDATORY ftsFieldGroupTest boost?)
        | BAR ftsFieldGroupTest boost?
                ->
                        ^(FIELD_OPTIONAL ftsFieldGroupTest boost?)
        | MINUS ftsFieldGroupTest boost?
                ->
                        ^(FIELD_EXCLUDE ftsFieldGroupTest boost?)
        ;

ftsFieldGroupTest
        :
        (ftsFieldGroupProximity) => ftsFieldGroupProximity
                ->
                        ^(FG_PROXIMITY ftsFieldGroupProximity)
        | (ftsFieldGroupTerm) => ftsFieldGroupTerm ( (fuzzy) => fuzzy)?
                ->
                        ^(FG_TERM ftsFieldGroupTerm fuzzy?)
        | (ftsFieldGroupExactTerm) => ftsFieldGroupExactTerm ( (fuzzy) => fuzzy)?
                ->
                        ^(FG_EXACT_TERM ftsFieldGroupExactTerm fuzzy?)
        | (ftsFieldGroupPhrase) => ftsFieldGroupPhrase ( (slop) => slop)?
                ->
                        ^(FG_PHRASE ftsFieldGroupPhrase slop?)
        | (ftsFieldGroupExactPhrase) => ftsFieldGroupExactPhrase ( (slop) => slop)?
                ->
                        ^(FG_EXACT_PHRASE ftsFieldGroupExactPhrase slop?)
        | (ftsFieldGroupTokenisedPhrase) => ftsFieldGroupTokenisedPhrase ( (slop) => slop)?
                ->
                        ^(FG_PHRASE ftsFieldGroupTokenisedPhrase slop?)
        | (ftsFieldGroupSynonym) => ftsFieldGroupSynonym ( (fuzzy) => fuzzy)?
                ->
                        ^(FG_SYNONYM ftsFieldGroupSynonym fuzzy?)
        | (ftsFieldGroupRange) => ftsFieldGroupRange
                ->
                        ^(FG_RANGE ftsFieldGroupRange)
        | LPAREN ftsFieldGroupDisjunction RPAREN
                -> ftsFieldGroupDisjunction
        ;

ftsFieldGroupTerm
        :
        ftsWord
        ;

ftsFieldGroupExactTerm
        :
        EQUALS ftsFieldGroupTerm
                -> ftsFieldGroupTerm
        ;

ftsFieldGroupPhrase
        :
        FTSPHRASE
        ;
        
ftsFieldGroupExactPhrase
        :
        EQUALS ftsFieldGroupExactPhrase
                -> ftsFieldGroupExactPhrase
        ;
        
ftsFieldGroupTokenisedPhrase
        :
        TILDA ftsFieldGroupExactPhrase
                -> ftsFieldGroupExactPhrase
        ;

ftsFieldGroupSynonym
        :
        TILDA ftsFieldGroupTerm
                -> ftsFieldGroupTerm
        ;

ftsFieldGroupProximity
        :
        ftsFieldGroupProximityTerm ( (proximityGroup) => proximityGroup ftsFieldGroupProximityTerm)+
                -> ftsFieldGroupProximityTerm (proximityGroup ftsFieldGroupProximityTerm)+
        ;

ftsFieldGroupProximityTerm
        :
          ID
        | FTSWORD
        | FTSPRE
        | FTSWILD
        | NOT
        | TO
        | DECIMAL_INTEGER_LITERAL
        | FLOATING_POINT_LITERAL
        | DATETIME
        | STAR
        | URI identifier
        ;

proximityGroup
        :
        STAR (LPAREN DECIMAL_INTEGER_LITERAL? RPAREN)?
                ->
                        ^(PROXIMITY DECIMAL_INTEGER_LITERAL?)
        ;

ftsFieldGroupRange
        :
        ftsRangeWord DOTDOT ftsRangeWord
                -> INCLUSIVE ftsRangeWord ftsRangeWord INCLUSIVE
        | range_left ftsRangeWord TO ftsRangeWord range_right
                -> range_left ftsRangeWord ftsRangeWord range_right
        ;

range_left
        :
        LSQUARE
                -> INCLUSIVE
        | LT
                -> EXCLUSIVE
        ;

range_right
        :
        RSQUARE
                -> INCLUSIVE
        | GT
                -> EXCLUSIVE
        ;

/* Need to fix the generated parser for extra COLON check ??*/

fieldReference
        :
        AT?
        (
                  (prefix) => prefix
                | uri
        )?
        identifier
                ->
                        ^(FIELD_REF identifier prefix? uri?)
        ;

tempReference
        :
        AT?
        (
                prefix
                | uri
        )?
        identifier
                ->
                        ^(FIELD_REF identifier prefix? uri?)
        ;

prefix
        :
        identifier COLON
                ->
                        ^(PREFIX identifier)
        ;

uri
        :
        URI
                ->
                        ^(NAME_SPACE URI)
        ;

identifier
        :
        (ID DOT ID) =>
         id1=ID DOT id2=ID
                ->      {new CommonTree(new CommonToken(FTSLexer.ID, $id1.text+$DOT.text+$id2.text))}
        | 
           ID
                ->
                        ID
        |
           TO
                ->      TO
        |  OR
                ->      OR
        |  AND 
                ->      AND
        |  NOT
                ->      NOT
        ; 

ftsWord
        :
           ((DOT|COMMA) ftsWordBase (DOT|COMMA) ftsWordBase (DOT|COMMA) ftsWordBase (DOT|COMMA) ftsWordBase (DOT|COMMA) ftsWordBase) => 
           (DOT|COMMA) ftsWordBase (DOT|COMMA) ftsWordBase (DOT|COMMA) ftsWordBase (DOT|COMMA) ftsWordBase (DOT|COMMA) ftsWordBase
        | (ftsWordBase (DOT|COMMA) ftsWordBase (DOT|COMMA) ftsWordBase (DOT|COMMA) ftsWordBase DOT|COMMA ftsWordBase) => 
           ftsWordBase (DOT|COMMA) ftsWordBase (DOT|COMMA) ftsWordBase (DOT|COMMA) ftsWordBase (DOT|COMMA) ftsWordBase
        |  ((DOT|COMMA) ftsWordBase (DOT|COMMA) ftsWordBase (DOT|COMMA) ftsWordBase (DOT|COMMA) ftsWordBase (DOT|COMMA)) => 
           (DOT|COMMA) ftsWordBase (DOT|COMMA) ftsWordBase (DOT|COMMA) ftsWordBase (DOT|COMMA) ftsWordBase (DOT|COMMA)
        | (ftsWordBase (DOT|COMMA) ftsWordBase (DOT|COMMA) ftsWordBase (DOT|COMMA) ftsWordBase (DOT|COMMA)) => 
           ftsWordBase (DOT|COMMA) ftsWordBase (DOT|COMMA) ftsWordBase (DOT|COMMA) ftsWordBase (DOT|COMMA)
        |  ((DOT|COMMA) ftsWordBase (DOT|COMMA) ftsWordBase (DOT|COMMA) ftsWordBase (DOT|COMMA) ftsWordBase) => 
           (DOT|COMMA) ftsWordBase (DOT|COMMA) ftsWordBase (DOT|COMMA) ftsWordBase (DOT|COMMA) ftsWordBase
        | (ftsWordBase (DOT|COMMA) ftsWordBase (DOT|COMMA) ftsWordBase (DOT|COMMA) ftsWordBase) => 
           ftsWordBase (DOT|COMMA) ftsWordBase (DOT|COMMA) ftsWordBase (DOT|COMMA) ftsWordBase
        | ((DOT|COMMA) ftsWordBase (DOT|COMMA) ftsWordBase (DOT|COMMA) ftsWordBase (DOT|COMMA)) => 
           (DOT|COMMA) ftsWordBase (DOT|COMMA) ftsWordBase (DOT|COMMA) ftsWordBase (DOT|COMMA)
        | (ftsWordBase (DOT|COMMA) ftsWordBase (DOT|COMMA) ftsWordBase (DOT|COMMA)) => 
           ftsWordBase (DOT|COMMA) ftsWordBase (DOT|COMMA) ftsWordBase (DOT|COMMA)
        | ((DOT|COMMA) ftsWordBase (DOT|COMMA) ftsWordBase (DOT|COMMA) ftsWordBase) => 
           (DOT|COMMA) ftsWordBase (DOT|COMMA) ftsWordBase (DOT|COMMA) ftsWordBase
        | (ftsWordBase (DOT|COMMA) ftsWordBase (DOT|COMMA) ftsWordBase) => 
           ftsWordBase (DOT|COMMA) ftsWordBase (DOT|COMMA) ftsWordBase 
        | ((DOT|COMMA) ftsWordBase (DOT|COMMA) ftsWordBase (DOT|COMMA)) => 
           (DOT|COMMA) ftsWordBase (DOT|COMMA) ftsWordBase (DOT|COMMA)
        | (ftsWordBase (DOT|COMMA) ftsWordBase (DOT|COMMA) ) => 
           ftsWordBase (DOT|COMMA) ftsWordBase (DOT|COMMA) 
        | ((DOT|COMMA) ftsWordBase (DOT|COMMA) ftsWordBase) => 
           (DOT|COMMA) ftsWordBase (DOT|COMMA) ftsWordBase
        | (ftsWordBase (DOT|COMMA) ftsWordBase) => 
           ftsWordBase (DOT|COMMA) ftsWordBase
        | ((DOT|COMMA) ftsWordBase (DOT|COMMA)) => 
           (DOT|COMMA) ftsWordBase (DOT|COMMA)
        | (ftsWordBase (DOT|COMMA)) => 
           ftsWordBase (DOT|COMMA)
        | (DOT|COMMA) ftsWordBase 
        | ftsWordBase 
        ;

        
ftsWordBase
        :
          ID
        | FTSWORD
        | FTSPRE 
        | FTSWILD 
        | NOT
        | TO
        | DECIMAL_INTEGER_LITERAL
        | FLOATING_POINT_LITERAL
        | STAR
        | QUESTION_MARK
        | DATETIME
        | URI identifier
        ;

number
        :
        DECIMAL_INTEGER_LITERAL
        | FLOATING_POINT_LITERAL
        ;

ftsRangeWord
        :
          ID
        | FTSWORD
        | FTSPRE
        | FTSWILD
        | FTSPHRASE
        | DECIMAL_INTEGER_LITERAL
        | FLOATING_POINT_LITERAL
        | DATETIME
        | STAR
        | URI identifier
        ;

//

or
        :
        OR
        | BAR BAR
        ;

and
        :
        AND
        | AMP AMP
        ;

not
        :
        NOT
        | EXCLAMATION
        ;

// ===== //
// LEXER //
// ===== //

FTSPHRASE
        :
        '"'
        (
                F_ESC
                |
                ~(
                        '\\'
                        | '"'
                 )
        )*
        '"'
        | '\''
        (
                F_ESC
                |
                ~(
                        '\\'
                        | '\''
                 )
        )*
        '\''
        ;
/*
 * Basic URI pattern based on the regular expression patttern taken from the RFC (it it not full URI parsing)
 * Note this means the language can not use {} anywhere else in the syntax
 */


URI
        :
        '{'
        (
                (
                        F_URI_ALPHA
                        | F_URI_DIGIT
                        | F_URI_OTHER
                )
                        =>
                (
                        F_URI_ALPHA
                        | F_URI_DIGIT
                        | F_URI_OTHER
                )+
                COLON
        )?
        (
                ( ('//') => '//')
                (
                        (
                                F_URI_ALPHA
                                | F_URI_DIGIT
                                | F_URI_OTHER
                                | COLON
                        )
                                =>
                        (
                                F_URI_ALPHA
                                | F_URI_DIGIT
                                | F_URI_OTHER
                                | COLON
                        )
                )*
        )?
        (
                F_URI_ALPHA
                | F_URI_DIGIT
                | F_URI_OTHER
                | COLON
                | '/'
        )*
        (
                '?'
                (
                        F_URI_ALPHA
                        | F_URI_DIGIT
                        | F_URI_OTHER
                        | COLON
                        | '/'
                        | '?'
                )*
        )?
        (
                '#'
                (
                        F_URI_ALPHA
                        | F_URI_DIGIT
                        | F_URI_OTHER
                        | COLON
                        | '/'
                        | '?'
                        | '#'
                )*
        )?
        '}'
        ;

fragment
F_URI_ALPHA
        :
        'A'..'Z'
        | 'a'..'z'
        ;

fragment
F_URI_DIGIT
        :
        '0'..'9'
        ;

fragment
F_URI_ESC
        :
        '%' F_HEX F_HEX
        ;

fragment
F_URI_OTHER
        :
        '-'
        | '.'
        | '_'
        | '~'
        | '['
        | ']'
        | '@'
        | '!'
        | '$'
        | '&'
        | '\''
        | '('
        | ')'
        | '*'
        | '+'
        | ','
        | ';'
        | '='
        ;
        
        
        
 /**
 * DATE literal
 */
 
DATETIME
        :
         (SPECIFICDATETIME | NOW) (FS UNIT)? ( (PLUS|MINUS) DIGIT+ UNIT)*
        ; 

fragment UNIT 
        :
        (YEAR | MONTH | DAY | HOUR | MINUTE | SECOND | MILLIS)
        ;
 
fragment SPECIFICDATETIME
        :
           DIGIT DIGIT DIGIT DIGIT 
              ( '-' DIGIT DIGIT ( '-' DIGIT DIGIT ( 'T' (DIGIT DIGIT ( ':' DIGIT DIGIT ( ':' DIGIT DIGIT ( '.' DIGIT DIGIT DIGIT ( 'Z' | (( '+' | '-') DIGIT DIGIT ( ':' DIGIT DIGIT)? ) )? )? )? )? )? )? )? )?
        ;
        
fragment NOW
        :
           ('N'|'n') ('O'|'o') ('W'|'w')
        ;
        
fragment YEAR
        :
          ('Y'|'y') ('E'|'e') ('A'|'a') ('R'|'r') ('S'|'s')? 
        ; 
        
fragment MONTH
        :
          ('M'|'m') ('O'|'o') ('N'|'n') ('T'|'t') ('H'|'h') ('S'|'s')? 
        ;

fragment DAY
        :
          ('D'|'d') ('A'|'a') ('Y'|'y') ('S'|'s')?
        | ('D'|'d') ('A'|'a') ('T'|'t') ('E'|'e')
        ; 
        
fragment HOUR
        :
          ('H'|'h') ('O'|'o') ('U'|'u') ('R'|'r') ('S'|'s')? 
        ; 
        
fragment MINUTE
        :
          ('M'|'m') ('I'|'i') ('N'|'n') ('U'|'u') ('T'|'t') ('E'|'e') ('S'|'s')? 
        ; 
        
fragment SECOND
        :
          ('S'|'s') ('E'|'e') ('C'|'c') ('O'|'o') ('N'|'n') ('D'|'d') ('S'|'s')? 
        ;
        
fragment MILLIS
        :
          ('M'|'m') ('I'|'i') ('L'|'l') ('L'|'l') ('I'|'i') ('S'|'s') ('E'|'e') ('C'|'c') ('O'|'o') ('N'|'n') ('D'|'d') ('S'|'s')?
        | ('M'|'m') ('I'|'i') ('L'|'l') ('L'|'l') ('I'|'i') ('S'|'s')?
        ;         
        
fragment FS
        :
        '/'
        ;
        
/*
 * Simple tokens, note all are case insensitive
 */


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

TILDA
        :
        '~'
        ;

LPAREN
        :
        '('
        ;

RPAREN
        :
        ')'
        ;

PLUS
        :
        '+'
        ;

MINUS
        :
        '-'
        ;

COLON
        :
        ':'
        ;

STAR
        :
        '*'
        ;

// This is handled sa part for FLOATING_POINT_LITERAL to reduce lexer complexity 
fragment DOTDOT
        :
        '..'
        ;

// This is handled sa part for FLOATING_POINT_LITERAL to reduce lexer complexity 
fragment DOT
        :
        '.'
        ;

AMP
        :
        '&'
        ;

EXCLAMATION
        :
        '!'
        ;

BAR
        :
        '|'
        ;

EQUALS
        :
        '='
        ;

QUESTION_MARK
        :
        '?'
        ;

LCURL
        :
        '{'
        ;

RCURL
        :
        '}'
        ;

LSQUARE
        :
        '['
        ;

RSQUARE
        :
        ']'
        ;

TO
        :
        (
                'T'
                | 't'
        )
        (
                'O'
                | 'o'
        )
        ;

COMMA
        :
        ','
        ;

CARAT
        :
        '^'
        ;

DOLLAR
        :
        '$'
        ;

GT
        :
        '>'
        ;

LT
        :
        '<'
        ;

AT
        :
        '@'
        ;

PERCENT
        :
        '%'
        ;


/**
 * ID
 * _x????_ encoding is supported for invalid sql characters but requires nothing here, they are handled in the code 
 * Also supports \ style escaping for non CMIS SQL 
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
                | '$'
                | '#'
                | F_ESC
        )*
        ;




// This is handled sa part for FLOATING_POINT_LITERAL to reduce lexer complexity 
fragment DECIMAL_INTEGER_LITERAL
        :
        ;

FLOATING_POINT_LITERAL
        :
         (PLUS|MINUS)?
         (
                DIGIT+
                (
                        {input.LA(2) != '.'}?=> DOT 
                        (
                                DIGIT+
                                (
                                    EXPONENT
                                    {$type = FLOATING_POINT_LITERAL; }
                                    |
                                    {input.LA(2) != '.'}?=> DOT 
                                    {
                                         int index = $text.indexOf('.');
                                         
                                         CommonToken digits1 = new CommonToken(input, DECIMAL_INTEGER_LITERAL, Token.DEFAULT_CHANNEL, $pos, $pos+index-1);
                                         emit(digits1);
                                        
                                         CommonToken dot1 = new CommonToken(input, DOT, Token.DEFAULT_CHANNEL, $pos+index, $pos+index);
                                         emit(dot1);
                    
                                         CommonToken digits2 = new CommonToken(input, DECIMAL_INTEGER_LITERAL, Token.DEFAULT_CHANNEL, $pos+index+1, $pos + $text.length() -2);
                                         emit(digits2);
                                
                                         CommonToken dot2 = new CommonToken(input, DOT, Token.DEFAULT_CHANNEL, $pos + $text.length() -1, $pos + $text.length() -1);
                                         emit(dot2);
                                        
                                    }
                                    |
                                    {$type = FLOATING_POINT_LITERAL; }
                                )
                                |
                                EXPONENT
                                {$type = FLOATING_POINT_LITERAL; }
                                |
                                {$type = FLOATING_POINT_LITERAL; }
                        )
                        |
                        (
                                EXPONENT
                                {$type = FLOATING_POINT_LITERAL; }
                                |
                                {$type = DECIMAL_INTEGER_LITERAL; }
                        )        
                       
                )
                |
                
                DOT
                (   
                        DIGIT+ 
                        (
                            EXPONENT
                            {$type = FLOATING_POINT_LITERAL; }
                            |
                            {$text.startsWith(".")}? {input.LA(2) != '.'}?=> DOT 
                           
                                {
                               
                                CommonToken dot1 = new CommonToken(input, DOT, Token.DEFAULT_CHANNEL, $pos, $pos);
                                emit(dot1);
                    
                                CommonToken digits = new CommonToken(input, DECIMAL_INTEGER_LITERAL, Token.DEFAULT_CHANNEL, $pos+1, $pos + $text.length() -2);
                                emit(digits);
                                
                                CommonToken dot2 = new CommonToken(input, DOT, Token.DEFAULT_CHANNEL, $pos + $text.length() -1, $pos + $text.length() -1);
                                emit(dot2);
                               
                                }
                            |
                            {$type = FLOATING_POINT_LITERAL; }
                                
                        )
                        |
                        {input.LA(2) != '.'}?=> '.'
                        {$type = DOTDOT; }
                        |
                        {$type = DOT; }
                 )
         )
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
E
        :
        (
                'e'
                | 'E'
        )
        ;

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


FTSWORD
        :
        (
                F_ESC
                | START_WORD
        )
        (
                F_ESC
                | IN_WORD
        )*
        ;

FTSPRE
        :
        (
                F_ESC
                | START_WORD
        )
        (
                F_ESC
                | IN_WORD
        )*
        STAR
        ;

FTSWILD
        :
        (
                F_ESC
                | START_WORD
                | STAR
                | QUESTION_MARK
        )
        (
                F_ESC
                | IN_WORD
                | STAR
                | QUESTION_MARK
        )*
        ;

fragment
F_ESC
        :
        '\\'
        (
                // unicode
                'u' F_HEX F_HEX F_HEX F_HEX
                // any single char escaped
                | .
        )
        ;

fragment
F_HEX
        :
        '0'..'9'
        | 'a'..'f'
        | 'A'..'F'
        ;

fragment
START_WORD
        :  // Generated from Java Character.isLetterOrDigit()
          '\u0024'
        | '\u0030'..'\u0039'
        | '\u0041'..'\u005a'
        | '\u0061'..'\u007a'
        | '\u00a2'..'\u00a7'
        | '\u00a9'..'\u00aa'
        | '\u00ae'
        | '\u00b0'
        | '\u00b2'..'\u00b3'
        | '\u00b5'..'\u00b6'
        | '\u00b9'..'\u00ba'
        | '\u00bc'..'\u00be'
        | '\u00c0'..'\u00d6'
        | '\u00d8'..'\u00f6'
        | '\u00f8'..'\u0236'
        | '\u0250'..'\u02c1'
        | '\u02c6'..'\u02d1'
        | '\u02e0'..'\u02e4'
        | '\u02ee'
        | '\u0300'..'\u0357'
        | '\u035d'..'\u036f'
        | '\u037a'..'\u037a'
        | '\u0386'
        | '\u0388'..'\u038a'
        | '\u038c'..'\u038c'
        | '\u038e'..'\u03a1'
        | '\u03a3'..'\u03ce'
        | '\u03d0'..'\u03f5'
        | '\u03f7'..'\u03fb'
        | '\u0400'..'\u0486'
        | '\u0488'..'\u04ce'
        | '\u04d0'..'\u04f5'
        | '\u04f8'..'\u04f9'
        | '\u0500'..'\u050f'
        | '\u0531'..'\u0556'
        | '\u0559'
        | '\u0561'..'\u0587'
        | '\u0591'..'\u05a1'
        | '\u05a3'..'\u05b9'
        | '\u05bb'..'\u05bd'
        | '\u05bf'
        | '\u05c1'..'\u05c2'
        | '\u05c4'..'\u05c4'
        | '\u05d0'..'\u05ea'
        | '\u05f0'..'\u05f2'
        | '\u060e'..'\u0615'
        | '\u0621'..'\u063a'
        | '\u0640'..'\u0658'
        | '\u0660'..'\u0669'
        | '\u066e'..'\u06d3'
        | '\u06d5'..'\u06dc'
        | '\u06de'..'\u06ff'
        | '\u0710'..'\u074a'
        | '\u074d'..'\u074f'
        | '\u0780'..'\u07b1'
        | '\u0901'..'\u0939'
        | '\u093c'..'\u094d'
        | '\u0950'..'\u0954'
        | '\u0958'..'\u0963'
        | '\u0966'..'\u096f'
        | '\u0981'..'\u0983'
        | '\u0985'..'\u098c'
        | '\u098f'..'\u0990'
        | '\u0993'..'\u09a8'
        | '\u09aa'..'\u09b0'
        | '\u09b2'..'\u09b2'
        | '\u09b6'..'\u09b9'
        | '\u09bc'..'\u09c4'
        | '\u09c7'..'\u09c8'
        | '\u09cb'..'\u09cd'
        | '\u09d7'..'\u09d7'
        | '\u09dc'..'\u09dd'
        | '\u09df'..'\u09e3'
        | '\u09e6'..'\u09fa'
        | '\u0a01'..'\u0a03'
        | '\u0a05'..'\u0a0a'
        | '\u0a0f'..'\u0a10'
        | '\u0a13'..'\u0a28'
        | '\u0a2a'..'\u0a30'
        | '\u0a32'..'\u0a33'
        | '\u0a35'..'\u0a36'
        | '\u0a38'..'\u0a39'
        | '\u0a3c'..'\u0a3c'
        | '\u0a3e'..'\u0a42'
        | '\u0a47'..'\u0a48'
        | '\u0a4b'..'\u0a4d'
        | '\u0a59'..'\u0a5c'
        | '\u0a5e'..'\u0a5e'
        | '\u0a66'..'\u0a74'
        | '\u0a81'..'\u0a83'
        | '\u0a85'..'\u0a8d'
        | '\u0a8f'..'\u0a91'
        | '\u0a93'..'\u0aa8'
        | '\u0aaa'..'\u0ab0'
        | '\u0ab2'..'\u0ab3'
        | '\u0ab5'..'\u0ab9'
        | '\u0abc'..'\u0ac5'
        | '\u0ac7'..'\u0ac9'
        | '\u0acb'..'\u0acd'
        | '\u0ad0'..'\u0ad0'
        | '\u0ae0'..'\u0ae3'
        | '\u0ae6'..'\u0aef'
        | '\u0af1'..'\u0af1'
        | '\u0b01'..'\u0b03'
        | '\u0b05'..'\u0b0c'
        | '\u0b0f'..'\u0b10'
        | '\u0b13'..'\u0b28'
        | '\u0b2a'..'\u0b30'
        | '\u0b32'..'\u0b33'
        | '\u0b35'..'\u0b39'
        | '\u0b3c'..'\u0b43'
        | '\u0b47'..'\u0b48'
        | '\u0b4b'..'\u0b4d'
        | '\u0b56'..'\u0b57'
        | '\u0b5c'..'\u0b5d'
        | '\u0b5f'..'\u0b61'
        | '\u0b66'..'\u0b71'
        | '\u0b82'..'\u0b83'
        | '\u0b85'..'\u0b8a'
        | '\u0b8e'..'\u0b90'
        | '\u0b92'..'\u0b95'
        | '\u0b99'..'\u0b9a'
        | '\u0b9c'..'\u0b9c'
        | '\u0b9e'..'\u0b9f'
        | '\u0ba3'..'\u0ba4'
        | '\u0ba8'..'\u0baa'
        | '\u0bae'..'\u0bb5'
        | '\u0bb7'..'\u0bb9'
        | '\u0bbe'..'\u0bc2'
        | '\u0bc6'..'\u0bc8'
        | '\u0bca'..'\u0bcd'
        | '\u0bd7'..'\u0bd7'
        | '\u0be7'..'\u0bfa'
        | '\u0c01'..'\u0c03'
        | '\u0c05'..'\u0c0c'
        | '\u0c0e'..'\u0c10'
        | '\u0c12'..'\u0c28'
        | '\u0c2a'..'\u0c33'
        | '\u0c35'..'\u0c39'
        | '\u0c3e'..'\u0c44'
        | '\u0c46'..'\u0c48'
        | '\u0c4a'..'\u0c4d'
        | '\u0c55'..'\u0c56'
        | '\u0c60'..'\u0c61'
        | '\u0c66'..'\u0c6f'
        | '\u0c82'..'\u0c83'
        | '\u0c85'..'\u0c8c'
        | '\u0c8e'..'\u0c90'
        | '\u0c92'..'\u0ca8'
        | '\u0caa'..'\u0cb3'
        | '\u0cb5'..'\u0cb9'
        | '\u0cbc'..'\u0cc4'
        | '\u0cc6'..'\u0cc8'
        | '\u0cca'..'\u0ccd'
        | '\u0cd5'..'\u0cd6'
        | '\u0cde'..'\u0cde'
        | '\u0ce0'..'\u0ce1'
        | '\u0ce6'..'\u0cef'
        | '\u0d02'..'\u0d03'
        | '\u0d05'..'\u0d0c'
        | '\u0d0e'..'\u0d10'
        | '\u0d12'..'\u0d28'
        | '\u0d2a'..'\u0d39'
        | '\u0d3e'..'\u0d43'
        | '\u0d46'..'\u0d48'
        | '\u0d4a'..'\u0d4d'
        | '\u0d57'..'\u0d57'
        | '\u0d60'..'\u0d61'
        | '\u0d66'..'\u0d6f'
        | '\u0d82'..'\u0d83'
        | '\u0d85'..'\u0d96'
        | '\u0d9a'..'\u0db1'
        | '\u0db3'..'\u0dbb'
        | '\u0dbd'..'\u0dbd'
        | '\u0dc0'..'\u0dc6'
        | '\u0dca'..'\u0dca'
        | '\u0dcf'..'\u0dd4'
        | '\u0dd6'..'\u0dd6'
        | '\u0dd8'..'\u0ddf'
        | '\u0df2'..'\u0df3'
        | '\u0e01'..'\u0e3a'
        | '\u0e3f'..'\u0e4e'
        | '\u0e50'..'\u0e59'
        | '\u0e81'..'\u0e82'
        | '\u0e84'..'\u0e84'
        | '\u0e87'..'\u0e88'
        | '\u0e8a'..'\u0e8a'
        | '\u0e8d'..'\u0e8d'
        | '\u0e94'..'\u0e97'
        | '\u0e99'..'\u0e9f'
        | '\u0ea1'..'\u0ea3'
        | '\u0ea5'..'\u0ea5'
        | '\u0ea7'..'\u0ea7'
        | '\u0eaa'..'\u0eab'
        | '\u0ead'..'\u0eb9'
        | '\u0ebb'..'\u0ebd'
        | '\u0ec0'..'\u0ec4'
        | '\u0ec6'..'\u0ec6'
        | '\u0ec8'..'\u0ecd'
        | '\u0ed0'..'\u0ed9'
        | '\u0edc'..'\u0edd'
        | '\u0f00'..'\u0f03'
        | '\u0f13'..'\u0f39'
        | '\u0f3e'..'\u0f47'
        | '\u0f49'..'\u0f6a'
        | '\u0f71'..'\u0f84'
        | '\u0f86'..'\u0f8b'
        | '\u0f90'..'\u0f97'
        | '\u0f99'..'\u0fbc'
        | '\u0fbe'..'\u0fcc'
        | '\u0fcf'..'\u0fcf'
        | '\u1000'..'\u1021'
        | '\u1023'..'\u1027'
        | '\u1029'..'\u102a'
        | '\u102c'..'\u1032'
        | '\u1036'..'\u1039'
        | '\u1040'..'\u1049'
        | '\u1050'..'\u1059'
        | '\u10a0'..'\u10c5'
        | '\u10d0'..'\u10f8'
        | '\u1100'..'\u1159'
        | '\u115f'..'\u11a2'
        | '\u11a8'..'\u11f9'
        | '\u1200'..'\u1206'
        | '\u1208'..'\u1246'
        | '\u1248'..'\u1248'
        | '\u124a'..'\u124d'
        | '\u1250'..'\u1256'
        | '\u1258'..'\u1258'
        | '\u125a'..'\u125d'
        | '\u1260'..'\u1286'
        | '\u1288'..'\u1288'
        | '\u128a'..'\u128d'
        | '\u1290'..'\u12ae'
        | '\u12b0'..'\u12b0'
        | '\u12b2'..'\u12b5'
        | '\u12b8'..'\u12be'
        | '\u12c0'..'\u12c0'
        | '\u12c2'..'\u12c5'
        | '\u12c8'..'\u12ce'
        | '\u12d0'..'\u12d6'
        | '\u12d8'..'\u12ee'
        | '\u12f0'..'\u130e'
        | '\u1310'..'\u1310'
        | '\u1312'..'\u1315'
        | '\u1318'..'\u131e'
        | '\u1320'..'\u1346'
        | '\u1348'..'\u135a'
        | '\u1369'..'\u137c'
        | '\u13a0'..'\u13f4'
        | '\u1401'..'\u166c'
        | '\u166f'..'\u1676'
        | '\u1681'..'\u169a'
        | '\u16a0'..'\u16ea'
        | '\u16ee'..'\u16f0'
        | '\u1700'..'\u170c'
        | '\u170e'..'\u1714'
        | '\u1720'..'\u1734'
        | '\u1740'..'\u1753'
        | '\u1760'..'\u176c'
        | '\u176e'..'\u1770'
        | '\u1772'..'\u1773'
        | '\u1780'..'\u17b3'
        | '\u17b6'..'\u17d3'
        | '\u17d7'
        | '\u17db'..'\u17dd'
        | '\u17e0'..'\u17e9'
        | '\u17f0'..'\u17f9'
        | '\u180b'..'\u180d'
        | '\u1810'..'\u1819'
        | '\u1820'..'\u1877'
        | '\u1880'..'\u18a9'
        | '\u1900'..'\u191c'
        | '\u1920'..'\u192b'
        | '\u1930'..'\u193b'
        | '\u1940'..'\u1940'
        | '\u1946'..'\u196d'
        | '\u1970'..'\u1974'
        | '\u19e0'..'\u19ff'
        | '\u1d00'..'\u1d6b'
        | '\u1e00'..'\u1e9b'
        | '\u1ea0'..'\u1ef9'
        | '\u1f00'..'\u1f15'
        | '\u1f18'..'\u1f1d'
        | '\u1f20'..'\u1f45'
        | '\u1f48'..'\u1f4d'
        | '\u1f50'..'\u1f57'
        | '\u1f59'..'\u1f59'
        | '\u1f5b'..'\u1f5b'
        | '\u1f5d'..'\u1f5d'
        | '\u1f5f'..'\u1f7d'
        | '\u1f80'..'\u1fb4'
        | '\u1fb6'..'\u1fbc'
        | '\u1fbe'
        | '\u1fc2'..'\u1fc4'
        | '\u1fc6'..'\u1fcc'
        | '\u1fd0'..'\u1fd3'
        | '\u1fd6'..'\u1fdb'
        | '\u1fe0'..'\u1fec'
        | '\u1ff2'..'\u1ff4'
        | '\u1ff6'..'\u1ffc'
        | '\u2070'..'\u2071'
        | '\u2074'..'\u2079'
        | '\u207f'..'\u2089'
        | '\u20a0'..'\u20b1'
        | '\u20d0'..'\u20ea'
        | '\u2100'..'\u213b'
        | '\u213d'..'\u213f'
        | '\u2145'..'\u214a'
        | '\u2153'..'\u2183'
        | '\u2195'..'\u2199'
        | '\u219c'..'\u219f'
        | '\u21a1'..'\u21a2'
        | '\u21a4'..'\u21a5'
        | '\u21a7'..'\u21ad'
        | '\u21af'..'\u21cd'
        | '\u21d0'..'\u21d1'
        | '\u21d3'
        | '\u21d5'..'\u21f3'
        | '\u2300'..'\u2307'
        | '\u230c'..'\u231f'
        | '\u2322'..'\u2328'
        | '\u232b'..'\u237b'
        | '\u237d'..'\u239a'
        | '\u23b7'..'\u23d0'
        | '\u2400'..'\u2426'
        | '\u2440'..'\u244a'
        | '\u2460'..'\u25b6'
        | '\u25b8'..'\u25c0'
        | '\u25c2'..'\u25f7'
        | '\u2600'..'\u2617'
        | '\u2619'..'\u266e'
        | '\u2670'..'\u267d'
        | '\u2680'..'\u2691'
        | '\u26a0'..'\u26a1'
        | '\u2701'..'\u2704'
        | '\u2706'..'\u2709'
        | '\u270c'..'\u2727'
        | '\u2729'..'\u274b'
        | '\u274d'..'\u274d'
        | '\u274f'..'\u2752'
        | '\u2756'..'\u2756'
        | '\u2758'..'\u275e'
        | '\u2761'..'\u2767'
        | '\u2776'..'\u2794'
        | '\u2798'..'\u27af'
        | '\u27b1'..'\u27be'
        | '\u2800'..'\u28ff'
        | '\u2b00'..'\u2b0d'
        | '\u2e80'..'\u2e99'
        | '\u2e9b'..'\u2ef3'
        | '\u2f00'..'\u2fd5'
        | '\u2ff0'..'\u2ffb'
        | '\u3004'..'\u3007'
        | '\u3012'..'\u3013'
        | '\u3020'..'\u302f'
        | '\u3031'..'\u303c'
        | '\u303e'..'\u303f'
        | '\u3041'..'\u3096'
        | '\u3099'..'\u309a'
        | '\u309d'..'\u309f'
        | '\u30a1'..'\u30fa'
        | '\u30fc'..'\u30ff'
        | '\u3105'..'\u312c'
        | '\u3131'..'\u318e'
        | '\u3190'..'\u31b7'
        | '\u31f0'..'\u321e'
        | '\u3220'..'\u3243'
        | '\u3250'..'\u327d'
        | '\u327f'..'\u32fe'
        | '\u3300'..'\u4db5'
        | '\u4dc0'..'\u9fa5'
        | '\ua000'..'\ua48c'
        | '\ua490'..'\ua4c6'
        | '\uac00'..'\ud7a3'
        | '\uf900'..'\ufa2d'
        | '\ufa30'..'\ufa6a'
        | '\ufb00'..'\ufb06'
        | '\ufb13'..'\ufb17'
        | '\ufb1d'..'\ufb28'
        | '\ufb2a'..'\ufb36'
        | '\ufb38'..'\ufb3c'
        | '\ufb3e'..'\ufb3e'
        | '\ufb40'..'\ufb41'
        | '\ufb43'..'\ufb44'
        | '\ufb46'..'\ufbb1'
        | '\ufbd3'..'\ufd3d'
        | '\ufd50'..'\ufd8f'
        | '\ufd92'..'\ufdc7'
        | '\ufdf0'..'\ufdfd'
        | '\ufe00'..'\ufe0f'
        | '\ufe20'..'\ufe23'
        | '\ufe69'
        | '\ufe70'..'\ufe74'
        | '\ufe76'..'\ufefc'
        | '\uff04'
        | '\uff10'..'\uff19'
        | '\uff21'..'\uff3a'
        | '\uff41'..'\uff5a'
        | '\uff66'..'\uffbe'
        | '\uffc2'..'\uffc7'
        | '\uffca'..'\uffcf'
        | '\uffd2'..'\uffd7'
        | '\uffda'..'\uffdc'
        | '\uffe0'..'\uffe1'
        | '\uffe4'..'\uffe6'
        | '\uffe8'
        | '\uffed'..'\uffee'
        ;
       
// exclude ? 003F - wildcard
// exclude * 002A - wildcard
// exclude \ 005C - escape
// exclude : 003A - field indicator
// exclude ~ 00&E - fuzzy queries
// exclude > 003C - ranges
// exclude < 003E - range
// exclude . 002E
// exclude , 002C 
// exclude  
fragment
IN_WORD
        :
          '\u0021'..'\u0027'
        | '\u002b'
        | '\u002d'
        | '\u002f'..'\u0039'
        | '\u003b'
        | '\u003d'
        | '\u0040'..'\u005a'
        | '\u005f'
        | '\u0061'..'\u007a'
        | '\u007c'
        | '\u00a1'..'\u00a7'
        | '\u00a9'..'\u00aa'
        | '\u00ac'
        | '\u00ae'
        | '\u00b0'..'\u00b3'
        | '\u00b5'..'\u00b7'
        | '\u00b9'..'\u00ba'
        | '\u00bc'..'\u0236'
        | '\u0250'..'\u02c1'
        | '\u02c6'..'\u02d1'
        | '\u02e0'..'\u02e4'
        | '\u02ee'
        | '\u0300'..'\u0357'
        | '\u035d'..'\u036f'
        | '\u037a'..'\u037a'
        | '\u037e'..'\u037e'
        | '\u0386'..'\u038a'
        | '\u038c'..'\u038c'
        | '\u038e'..'\u03a1'
        | '\u03a3'..'\u03ce'
        | '\u03d0'..'\u03fb'
        | '\u0400'..'\u0486'
        | '\u0488'..'\u04ce'
        | '\u04d0'..'\u04f5'
        | '\u04f8'..'\u04f9'
        | '\u0500'..'\u050f'
        | '\u0531'..'\u0556'
        | '\u0559'..'\u055f'
        | '\u0561'..'\u0587'
        | '\u0589'..'\u058a'
        | '\u0591'..'\u05a1'
        | '\u05a3'..'\u05b9'
        | '\u05bb'..'\u05c4'
        | '\u05d0'..'\u05ea'
        | '\u05f0'..'\u05f4'
        | '\u060c'..'\u0615'
        | '\u061b'..'\u061b'
        | '\u061f'..'\u061f'
        | '\u0621'..'\u063a'
        | '\u0640'..'\u0658'
        | '\u0660'..'\u06dc'
        | '\u06de'..'\u070d'
        | '\u0710'..'\u074a'
        | '\u074d'..'\u074f'
        | '\u0780'..'\u07b1'
        | '\u0901'..'\u0939'
        | '\u093c'..'\u094d'
        | '\u0950'..'\u0954'
        | '\u0958'..'\u0970'
        | '\u0981'..'\u0983'
        | '\u0985'..'\u098c'
        | '\u098f'..'\u0990'
        | '\u0993'..'\u09a8'
        | '\u09aa'..'\u09b0'
        | '\u09b2'..'\u09b2'
        | '\u09b6'..'\u09b9'
        | '\u09bc'..'\u09c4'
        | '\u09c7'..'\u09c8'
        | '\u09cb'..'\u09cd'
        | '\u09d7'..'\u09d7'
        | '\u09dc'..'\u09dd'
        | '\u09df'..'\u09e3'
        | '\u09e6'..'\u09fa'
        | '\u0a01'..'\u0a03'
        | '\u0a05'..'\u0a0a'
        | '\u0a0f'..'\u0a10'
        | '\u0a13'..'\u0a28'
        | '\u0a2a'..'\u0a30'
        | '\u0a32'..'\u0a33'
        | '\u0a35'..'\u0a36'
        | '\u0a38'..'\u0a39'
        | '\u0a3c'..'\u0a3c'
        | '\u0a3e'..'\u0a42'
        | '\u0a47'..'\u0a48'
        | '\u0a4b'..'\u0a4d'
        | '\u0a59'..'\u0a5c'
        | '\u0a5e'..'\u0a5e'
        | '\u0a66'..'\u0a74'
        | '\u0a81'..'\u0a83'
        | '\u0a85'..'\u0a8d'
        | '\u0a8f'..'\u0a91'
        | '\u0a93'..'\u0aa8'
        | '\u0aaa'..'\u0ab0'
        | '\u0ab2'..'\u0ab3'
        | '\u0ab5'..'\u0ab9'
        | '\u0abc'..'\u0ac5'
        | '\u0ac7'..'\u0ac9'
        | '\u0acb'..'\u0acd'
        | '\u0ad0'..'\u0ad0'
        | '\u0ae0'..'\u0ae3'
        | '\u0ae6'..'\u0aef'
        | '\u0af1'..'\u0af1'
        | '\u0b01'..'\u0b03'
        | '\u0b05'..'\u0b0c'
        | '\u0b0f'..'\u0b10'
        | '\u0b13'..'\u0b28'
        | '\u0b2a'..'\u0b30'
        | '\u0b32'..'\u0b33'
        | '\u0b35'..'\u0b39'
        | '\u0b3c'..'\u0b43'
        | '\u0b47'..'\u0b48'
        | '\u0b4b'..'\u0b4d'
        | '\u0b56'..'\u0b57'
        | '\u0b5c'..'\u0b5d'
        | '\u0b5f'..'\u0b61'
        | '\u0b66'..'\u0b71'
        | '\u0b82'..'\u0b83'
        | '\u0b85'..'\u0b8a'
        | '\u0b8e'..'\u0b90'
        | '\u0b92'..'\u0b95'
        | '\u0b99'..'\u0b9a'
        | '\u0b9c'..'\u0b9c'
        | '\u0b9e'..'\u0b9f'
        | '\u0ba3'..'\u0ba4'
        | '\u0ba8'..'\u0baa'
        | '\u0bae'..'\u0bb5'
        | '\u0bb7'..'\u0bb9'
        | '\u0bbe'..'\u0bc2'
        | '\u0bc6'..'\u0bc8'
        | '\u0bca'..'\u0bcd'
        | '\u0bd7'..'\u0bd7'
        | '\u0be7'..'\u0bfa'
        | '\u0c01'..'\u0c03'
        | '\u0c05'..'\u0c0c'
        | '\u0c0e'..'\u0c10'
        | '\u0c12'..'\u0c28'
        | '\u0c2a'..'\u0c33'
        | '\u0c35'..'\u0c39'
        | '\u0c3e'..'\u0c44'
        | '\u0c46'..'\u0c48'
        | '\u0c4a'..'\u0c4d'
        | '\u0c55'..'\u0c56'
        | '\u0c60'..'\u0c61'
        | '\u0c66'..'\u0c6f'
        | '\u0c82'..'\u0c83'
        | '\u0c85'..'\u0c8c'
        | '\u0c8e'..'\u0c90'
        | '\u0c92'..'\u0ca8'
        | '\u0caa'..'\u0cb3'
        | '\u0cb5'..'\u0cb9'
        | '\u0cbc'..'\u0cc4'
        | '\u0cc6'..'\u0cc8'
        | '\u0cca'..'\u0ccd'
        | '\u0cd5'..'\u0cd6'
        | '\u0cde'..'\u0cde'
        | '\u0ce0'..'\u0ce1'
        | '\u0ce6'..'\u0cef'
        | '\u0d02'..'\u0d03'
        | '\u0d05'..'\u0d0c'
        | '\u0d0e'..'\u0d10'
        | '\u0d12'..'\u0d28'
        | '\u0d2a'..'\u0d39'
        | '\u0d3e'..'\u0d43'
        | '\u0d46'..'\u0d48'
        | '\u0d4a'..'\u0d4d'
        | '\u0d57'..'\u0d57'
        | '\u0d60'..'\u0d61'
        | '\u0d66'..'\u0d6f'
        | '\u0d82'..'\u0d83'
        | '\u0d85'..'\u0d96'
        | '\u0d9a'..'\u0db1'
        | '\u0db3'..'\u0dbb'
        | '\u0dbd'..'\u0dbd'
        | '\u0dc0'..'\u0dc6'
        | '\u0dca'..'\u0dca'
        | '\u0dcf'..'\u0dd4'
        | '\u0dd6'..'\u0dd6'
        | '\u0dd8'..'\u0ddf'
        | '\u0df2'..'\u0df4'
        | '\u0e01'..'\u0e3a'
        | '\u0e3f'..'\u0e5b'
        | '\u0e81'..'\u0e82'
        | '\u0e84'..'\u0e84'
        | '\u0e87'..'\u0e88'
        | '\u0e8a'..'\u0e8a'
        | '\u0e8d'..'\u0e8d'
        | '\u0e94'..'\u0e97'
        | '\u0e99'..'\u0e9f'
        | '\u0ea1'..'\u0ea3'
        | '\u0ea5'..'\u0ea5'
        | '\u0ea7'..'\u0ea7'
        | '\u0eaa'..'\u0eab'
        | '\u0ead'..'\u0eb9'
        | '\u0ebb'..'\u0ebd'
        | '\u0ec0'..'\u0ec4'
        | '\u0ec6'..'\u0ec6'
        | '\u0ec8'..'\u0ecd'
        | '\u0ed0'..'\u0ed9'
        | '\u0edc'..'\u0edd'
        | '\u0f00'..'\u0f39'
        | '\u0f3e'..'\u0f47'
        | '\u0f49'..'\u0f6a'
        | '\u0f71'..'\u0f8b'
        | '\u0f90'..'\u0f97'
        | '\u0f99'..'\u0fbc'
        | '\u0fbe'..'\u0fcc'
        | '\u0fcf'..'\u0fcf'
        | '\u1000'..'\u1021'
        | '\u1023'..'\u1027'
        | '\u1029'..'\u102a'
        | '\u102c'..'\u1032'
        | '\u1036'..'\u1039'
        | '\u1040'..'\u1059'
        | '\u10a0'..'\u10c5'
        | '\u10d0'..'\u10f8'
        | '\u10fb'..'\u10fb'
        | '\u1100'..'\u1159'
        | '\u115f'..'\u11a2'
        | '\u11a8'..'\u11f9'
        | '\u1200'..'\u1206'
        | '\u1208'..'\u1246'
        | '\u1248'..'\u1248'
        | '\u124a'..'\u124d'
        | '\u1250'..'\u1256'
        | '\u1258'..'\u1258'
        | '\u125a'..'\u125d'
        | '\u1260'..'\u1286'
        | '\u1288'..'\u1288'
        | '\u128a'..'\u128d'
        | '\u1290'..'\u12ae'
        | '\u12b0'..'\u12b0'
        | '\u12b2'..'\u12b5'
        | '\u12b8'..'\u12be'
        | '\u12c0'..'\u12c0'
        | '\u12c2'..'\u12c5'
        | '\u12c8'..'\u12ce'
        | '\u12d0'..'\u12d6'
        | '\u12d8'..'\u12ee'
        | '\u12f0'..'\u130e'
        | '\u1310'..'\u1310'
        | '\u1312'..'\u1315'
        | '\u1318'..'\u131e'
        | '\u1320'..'\u1346'
        | '\u1348'..'\u135a'
        | '\u1361'..'\u137c'
        | '\u13a0'..'\u13f4'
        | '\u1401'..'\u1676'
        | '\u1681'..'\u169a'
        | '\u16a0'..'\u16f0'
        | '\u1700'..'\u170c'
        | '\u170e'..'\u1714'
        | '\u1720'..'\u1736'
        | '\u1740'..'\u1753'
        | '\u1760'..'\u176c'
        | '\u176e'..'\u1770'
        | '\u1772'..'\u1773'
        | '\u1780'..'\u17b3'
        | '\u17b6'..'\u17dd'
        | '\u17e0'..'\u17e9'
        | '\u17f0'..'\u17f9'
        | '\u1800'..'\u180d'
        | '\u1810'..'\u1819'
        | '\u1820'..'\u1877'
        | '\u1880'..'\u18a9'
        | '\u1900'..'\u191c'
        | '\u1920'..'\u192b'
        | '\u1930'..'\u193b'
        | '\u1940'..'\u1940'
        | '\u1944'..'\u196d'
        | '\u1970'..'\u1974'
        | '\u19e0'..'\u19ff'
        | '\u1d00'..'\u1d6b'
        | '\u1e00'..'\u1e9b'
        | '\u1ea0'..'\u1ef9'
        | '\u1f00'..'\u1f15'
        | '\u1f18'..'\u1f1d'
        | '\u1f20'..'\u1f45'
        | '\u1f48'..'\u1f4d'
        | '\u1f50'..'\u1f57'
        | '\u1f59'..'\u1f59'
        | '\u1f5b'..'\u1f5b'
        | '\u1f5d'..'\u1f5d'
        | '\u1f5f'..'\u1f7d'
        | '\u1f80'..'\u1fb4'
        | '\u1fb6'..'\u1fbc'
        | '\u1fbe'
        | '\u1fc2'..'\u1fc4'
        | '\u1fc6'..'\u1fcc'
        | '\u1fd0'..'\u1fd3'
        | '\u1fd6'..'\u1fdb'
        | '\u1fe0'..'\u1fec'
        | '\u1ff2'..'\u1ff4'
        | '\u1ff6'..'\u1ffc'
        | '\u2010'..'\u2017'
        | '\u2020'..'\u2027'
        | '\u2030'..'\u2038'
        | '\u203b'..'\u2044'
        | '\u2047'..'\u2054'
        | '\u2057'..'\u2057'
        | '\u2070'..'\u2071'
        | '\u2074'..'\u207c'
        | '\u207f'..'\u208c'
        | '\u20a0'..'\u20b1'
        | '\u20d0'..'\u20ea'
        | '\u2100'..'\u213b'
        | '\u213d'..'\u214b'
        | '\u2153'..'\u2183'
        | '\u2190'..'\u2328'
        | '\u232b'..'\u23b3'
        | '\u23b6'..'\u23d0'
        | '\u2400'..'\u2426'
        | '\u2440'..'\u244a'
        | '\u2460'..'\u2617'
        | '\u2619'..'\u267d'
        | '\u2680'..'\u2691'
        | '\u26a0'..'\u26a1'
        | '\u2701'..'\u2704'
        | '\u2706'..'\u2709'
        | '\u270c'..'\u2727'
        | '\u2729'..'\u274b'
        | '\u274d'..'\u274d'
        | '\u274f'..'\u2752'
        | '\u2756'..'\u2756'
        | '\u2758'..'\u275e'
        | '\u2761'..'\u2767'
        | '\u2776'..'\u2794'
        | '\u2798'..'\u27af'
        | '\u27b1'..'\u27be'
        | '\u27d0'..'\u27e5'
        | '\u27f0'..'\u2982'
        | '\u2999'..'\u29d7'
        | '\u29dc'..'\u29fb'
        | '\u29fe'..'\u2b0d'
        | '\u2e80'..'\u2e99'
        | '\u2e9b'..'\u2ef3'
        | '\u2f00'..'\u2fd5'
        | '\u2ff0'..'\u2ffb'
        | '\u3001'..'\u3007'
        | '\u3012'..'\u3013'
        | '\u301c'
        | '\u3020'..'\u303f'
        | '\u3041'..'\u3096'
        | '\u3099'..'\u309a'
        | '\u309d'..'\u30ff'
        | '\u3105'..'\u312c'
        | '\u3131'..'\u318e'
        | '\u3190'..'\u31b7'
        | '\u31f0'..'\u321e'
        | '\u3220'..'\u3243'
        | '\u3250'..'\u327d'
        | '\u327f'..'\u32fe'
        | '\u3300'..'\u4db5'
        | '\u4dc0'..'\u9fa5'
        | '\ua000'..'\ua48c'
        | '\ua490'..'\ua4c6'
        | '\uac00'..'\ud7a3'
        | '\uf900'..'\ufa2d'
        | '\ufa30'..'\ufa6a'
        | '\ufb00'..'\ufb06'
        | '\ufb13'..'\ufb17'
        | '\ufb1d'..'\ufb36'
        | '\ufb38'..'\ufb3c'
        | '\ufb3e'..'\ufb3e'
        | '\ufb40'..'\ufb41'
        | '\ufb43'..'\ufb44'
        | '\ufb46'..'\ufbb1'
        | '\ufbd3'..'\ufd3d'
        | '\ufd50'..'\ufd8f'
        | '\ufd92'..'\ufdc7'
        | '\ufdf0'..'\ufdfd'
        | '\ufe00'..'\ufe0f'
        | '\ufe20'..'\ufe23'
        | '\ufe30'..'\ufe34'
        | '\ufe45'..'\ufe46'
        | '\ufe49'..'\ufe52'
        | '\ufe54'..'\ufe58'
        | '\ufe5f'..'\ufe66'
        | '\ufe68'..'\ufe6b'
        | '\ufe70'..'\ufe74'
        | '\ufe76'..'\ufefc'
        | '\uff01'..'\uff07'
        | '\uff0a'..'\uff3a'
        | '\uff3c'
        | '\uff3f'
        | '\uff41'..'\uff5a'
        | '\uff5c'
        | '\uff5e'
        | '\uff61'
        | '\uff64'..'\uffbe'
        | '\uffc2'..'\uffc7'
        | '\uffca'..'\uffcf'
        | '\uffd2'..'\uffd7'
        | '\uffda'..'\uffdc'
        | '\uffe0'..'\uffe2'
        | '\uffe4'..'\uffe6'
        | '\uffe8'..'\uffee'
        ;
        

/*
 * Standard white space
 * White space may be escaped by \ in some tokens 
 */


WS
        :
        (
                ' '
                | '\t'
                | '\r'
                | '\n'
                | '\u000C'   // FF
                | '\u00a0'   // Additional Unicode space from Character.isSpaceChar()
                | '\u1680'
                | '\u180e'
                | '\u2000' ..  '\u200b'
                | '\u2028' ..  '\u2029'
                | '\u202f'
                | '\u205f'
                | '\u3000'
        )+
        { $channel = HIDDEN; }
        ;
