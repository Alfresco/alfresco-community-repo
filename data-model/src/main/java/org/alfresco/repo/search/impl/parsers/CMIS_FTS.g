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
 * Parser for the Alfresco full text query language.
 * It may be used stand-alone or embedded, for example, in CMIS SQL contains() 
 *
 */

grammar CMIS_FTS;

options
{
        output    = AST;
        backtrack = false;
}
/*
 * Additional tokens for tree building.
 */


tokens
{
        DISJUNCTION;
        CONJUNCTION;
        TERM;
        PHRASE;
        DEFAULT;
        EXCLUDE;
}
/*
 * Make sure the lexer and parser are generated in the correct package
 */


@lexer::header
{
package org.alfresco.repo.search.impl.parsers;
import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;
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
    private Stack<String> paraphrases = new Stack<String>();
    
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
/*
 * Top level query
 */


cmisFtsQuery
        :
        ftsCmisDisjunction EOF
                -> ftsCmisDisjunction
        ;
/*
 * "OR"
 * As SQL, OR has lower precedence than implicit AND
 */


ftsCmisDisjunction
        :
        ftsCmisConjunction (or ftsCmisConjunction)*
                ->
                        ^(DISJUNCTION ftsCmisConjunction+)
        ;

ftsCmisConjunction
        :
        ftsCmisPrefixed+
                ->
                        ^(CONJUNCTION ftsCmisPrefixed+)
        ;

ftsCmisPrefixed
        :
        cmisTest
                ->
                        ^(DEFAULT cmisTest)
        | MINUS cmisTest
                ->
                        ^(EXCLUDE cmisTest)
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

cmisTerm
        :
        FTSWORD
                -> FTSWORD
        ;

cmisPhrase
        :
        FTSPHRASE
                -> FTSPHRASE
        ;

or
        :
        OR
        ;

// ===== //
// LEXER //
// ===== //

FTSPHRASE
        :
        '\''
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
        
        
fragment
F_ESC
        :
        '\\' ('\\' | '\'')
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

MINUS
        :
        '-'
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
        )+
        { $channel = HIDDEN; }
        ;


FTSWORD
        :
        START_WORD IN_WORD*
        ;

fragment
START_WORD
        :
        ~ (
                ' '
                | '\t'
                | '\r'
                | '\n'
                | '-'
        )
        ;

fragment
IN_WORD
        :
        ~ (
                ' '
                | '\t'
                | '\r'
                | '\n'
        )
        ;

