// $ANTLR 3.4 org/alfresco/rest/antlr/WhereClause.g 2013-05-24 09:01:14

package org.alfresco.rest.antlr;
import java.util.Map;
import java.util.HashMap;
import org.alfresco.rest.framework.resource.parameters.where.InvalidQueryException;
import org.alfresco.rest.framework.resource.parameters.where.WhereCompiler;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked"})
public class WhereClauseLexer extends Lexer {
    public static final int EOF=-1;
    public static final int AND=4;
    public static final int BETWEEN=5;
    public static final int COMMA=6;
    public static final int EQUALS=7;
    public static final int EXISTS=8;
    public static final int GREATERTHAN=9;
    public static final int GREATERTHANOREQUALS=10;
    public static final int IDENTIFIER=11;
    public static final int IDENTIFIERDIGIT=12;
    public static final int IDENTIFIERLETTER=13;
    public static final int IN=14;
    public static final int LEFTPAREN=15;
    public static final int LESSTHAN=16;
    public static final int LESSTHANOREQUALS=17;
    public static final int MATCHES=18;
    public static final int NEGATION=19;
    public static final int OR=20;
    public static final int PROPERTYNAME=21;
    public static final int PROPERTYVALUE=22;
    public static final int RIGHTPAREN=23;
    public static final int SINGLEQUOTE=24;
    public static final int WS=25;


        @Override
        public void recover(RecognitionException e)
        {
            throw new InvalidQueryException(WhereCompiler.resolveMessage(e));
        }


    // delegates
    // delegators
    public Lexer[] getDelegates() {
        return new Lexer[] {};
    }

    public WhereClauseLexer() {} 
    public WhereClauseLexer(CharStream input) {
        this(input, new RecognizerSharedState());
    }
    public WhereClauseLexer(CharStream input, RecognizerSharedState state) {
        super(input,state);
    }
    public String getGrammarFileName() { return "org/alfresco/rest/antlr/WhereClause.g"; }

    // $ANTLR start "NEGATION"
    public final void mNEGATION() throws RecognitionException {
        try {
            int _type = NEGATION;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // org/alfresco/rest/antlr/WhereClause.g:72:9: ( ( 'not' | 'NOT' ) WS )
            // org/alfresco/rest/antlr/WhereClause.g:72:11: ( 'not' | 'NOT' ) WS
            {
            // org/alfresco/rest/antlr/WhereClause.g:72:11: ( 'not' | 'NOT' )
            int alt1=2;
            switch ( input.LA(1) ) {
            case 'n':
                {
                alt1=1;
                }
                break;
            case 'N':
                {
                alt1=2;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 1, 0, input);

                throw nvae;

            }

            switch (alt1) {
                case 1 :
                    // org/alfresco/rest/antlr/WhereClause.g:72:12: 'not'
                    {
                    match("not"); 



                    }
                    break;
                case 2 :
                    // org/alfresco/rest/antlr/WhereClause.g:72:18: 'NOT'
                    {
                    match("NOT"); 



                    }
                    break;

            }


            mWS(); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "NEGATION"

    // $ANTLR start "EXISTS"
    public final void mEXISTS() throws RecognitionException {
        try {
            int _type = EXISTS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // org/alfresco/rest/antlr/WhereClause.g:73:7: ( 'exists' | 'EXISTS' )
            int alt2=2;
            switch ( input.LA(1) ) {
            case 'e':
                {
                alt2=1;
                }
                break;
            case 'E':
                {
                alt2=2;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 2, 0, input);

                throw nvae;

            }

            switch (alt2) {
                case 1 :
                    // org/alfresco/rest/antlr/WhereClause.g:73:9: 'exists'
                    {
                    match("exists"); 



                    }
                    break;
                case 2 :
                    // org/alfresco/rest/antlr/WhereClause.g:73:18: 'EXISTS'
                    {
                    match("EXISTS"); 



                    }
                    break;

            }
            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "EXISTS"

    // $ANTLR start "IN"
    public final void mIN() throws RecognitionException {
        try {
            int _type = IN;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // org/alfresco/rest/antlr/WhereClause.g:74:3: ( WS ( 'in' | 'IN' ) )
            // org/alfresco/rest/antlr/WhereClause.g:74:5: WS ( 'in' | 'IN' )
            {
            mWS(); 


            // org/alfresco/rest/antlr/WhereClause.g:74:7: ( 'in' | 'IN' )
            int alt3=2;
            switch ( input.LA(1) ) {
            case 'i':
                {
                alt3=1;
                }
                break;
            case 'I':
                {
                alt3=2;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 3, 0, input);

                throw nvae;

            }

            switch (alt3) {
                case 1 :
                    // org/alfresco/rest/antlr/WhereClause.g:74:8: 'in'
                    {
                    match("in"); 



                    }
                    break;
                case 2 :
                    // org/alfresco/rest/antlr/WhereClause.g:74:13: 'IN'
                    {
                    match("IN"); 



                    }
                    break;

            }


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "IN"

    // $ANTLR start "MATCHES"
    public final void mMATCHES() throws RecognitionException {
        try {
            int _type = MATCHES;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // org/alfresco/rest/antlr/WhereClause.g:75:8: ( WS ( 'matches' | 'MATCHES' ) )
            // org/alfresco/rest/antlr/WhereClause.g:75:10: WS ( 'matches' | 'MATCHES' )
            {
            mWS(); 


            // org/alfresco/rest/antlr/WhereClause.g:75:12: ( 'matches' | 'MATCHES' )
            int alt4=2;
            switch ( input.LA(1) ) {
            case 'm':
                {
                alt4=1;
                }
                break;
            case 'M':
                {
                alt4=2;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 4, 0, input);

                throw nvae;

            }

            switch (alt4) {
                case 1 :
                    // org/alfresco/rest/antlr/WhereClause.g:75:13: 'matches'
                    {
                    match("matches"); 



                    }
                    break;
                case 2 :
                    // org/alfresco/rest/antlr/WhereClause.g:75:23: 'MATCHES'
                    {
                    match("MATCHES"); 



                    }
                    break;

            }


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "MATCHES"

    // $ANTLR start "BETWEEN"
    public final void mBETWEEN() throws RecognitionException {
        try {
            int _type = BETWEEN;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // org/alfresco/rest/antlr/WhereClause.g:76:8: ( WS ( 'between' | 'BETWEEN' ) )
            // org/alfresco/rest/antlr/WhereClause.g:76:10: WS ( 'between' | 'BETWEEN' )
            {
            mWS(); 


            // org/alfresco/rest/antlr/WhereClause.g:76:12: ( 'between' | 'BETWEEN' )
            int alt5=2;
            switch ( input.LA(1) ) {
            case 'b':
                {
                alt5=1;
                }
                break;
            case 'B':
                {
                alt5=2;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 5, 0, input);

                throw nvae;

            }

            switch (alt5) {
                case 1 :
                    // org/alfresco/rest/antlr/WhereClause.g:76:13: 'between'
                    {
                    match("between"); 



                    }
                    break;
                case 2 :
                    // org/alfresco/rest/antlr/WhereClause.g:76:23: 'BETWEEN'
                    {
                    match("BETWEEN"); 



                    }
                    break;

            }


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "BETWEEN"

    // $ANTLR start "OR"
    public final void mOR() throws RecognitionException {
        try {
            int _type = OR;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // org/alfresco/rest/antlr/WhereClause.g:77:3: ( WS ( 'or' | 'OR' ) WS )
            // org/alfresco/rest/antlr/WhereClause.g:77:5: WS ( 'or' | 'OR' ) WS
            {
            mWS(); 


            // org/alfresco/rest/antlr/WhereClause.g:77:7: ( 'or' | 'OR' )
            int alt6=2;
            switch ( input.LA(1) ) {
            case 'o':
                {
                alt6=1;
                }
                break;
            case 'O':
                {
                alt6=2;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 6, 0, input);

                throw nvae;

            }

            switch (alt6) {
                case 1 :
                    // org/alfresco/rest/antlr/WhereClause.g:77:8: 'or'
                    {
                    match("or"); 



                    }
                    break;
                case 2 :
                    // org/alfresco/rest/antlr/WhereClause.g:77:13: 'OR'
                    {
                    match("OR"); 



                    }
                    break;

            }


            mWS(); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "OR"

    // $ANTLR start "AND"
    public final void mAND() throws RecognitionException {
        try {
            int _type = AND;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // org/alfresco/rest/antlr/WhereClause.g:78:4: ( WS ( 'and' | 'AND' ) WS )
            // org/alfresco/rest/antlr/WhereClause.g:78:6: WS ( 'and' | 'AND' ) WS
            {
            mWS(); 


            // org/alfresco/rest/antlr/WhereClause.g:78:8: ( 'and' | 'AND' )
            int alt7=2;
            switch ( input.LA(1) ) {
            case 'a':
                {
                alt7=1;
                }
                break;
            case 'A':
                {
                alt7=2;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 7, 0, input);

                throw nvae;

            }

            switch (alt7) {
                case 1 :
                    // org/alfresco/rest/antlr/WhereClause.g:78:9: 'and'
                    {
                    match("and"); 



                    }
                    break;
                case 2 :
                    // org/alfresco/rest/antlr/WhereClause.g:78:15: 'AND'
                    {
                    match("AND"); 



                    }
                    break;

            }


            mWS(); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "AND"

    // $ANTLR start "EQUALS"
    public final void mEQUALS() throws RecognitionException {
        try {
            int _type = EQUALS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // org/alfresco/rest/antlr/WhereClause.g:79:7: ( ( WS )? '=' ( WS )? )
            // org/alfresco/rest/antlr/WhereClause.g:79:9: ( WS )? '=' ( WS )?
            {
            // org/alfresco/rest/antlr/WhereClause.g:79:9: ( WS )?
            int alt8=2;
            switch ( input.LA(1) ) {
                case '\t':
                case '\n':
                case '\r':
                case ' ':
                    {
                    alt8=1;
                    }
                    break;
            }

            switch (alt8) {
                case 1 :
                    // org/alfresco/rest/antlr/WhereClause.g:79:9: WS
                    {
                    mWS(); 


                    }
                    break;

            }


            match('='); 

            // org/alfresco/rest/antlr/WhereClause.g:79:15: ( WS )?
            int alt9=2;
            switch ( input.LA(1) ) {
                case '\t':
                case '\n':
                case '\r':
                case ' ':
                    {
                    alt9=1;
                    }
                    break;
            }

            switch (alt9) {
                case 1 :
                    // org/alfresco/rest/antlr/WhereClause.g:79:15: WS
                    {
                    mWS(); 


                    }
                    break;

            }


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "EQUALS"

    // $ANTLR start "LESSTHAN"
    public final void mLESSTHAN() throws RecognitionException {
        try {
            int _type = LESSTHAN;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // org/alfresco/rest/antlr/WhereClause.g:80:9: ( ( WS )? '<' ( WS )? )
            // org/alfresco/rest/antlr/WhereClause.g:80:11: ( WS )? '<' ( WS )?
            {
            // org/alfresco/rest/antlr/WhereClause.g:80:11: ( WS )?
            int alt10=2;
            switch ( input.LA(1) ) {
                case '\t':
                case '\n':
                case '\r':
                case ' ':
                    {
                    alt10=1;
                    }
                    break;
            }

            switch (alt10) {
                case 1 :
                    // org/alfresco/rest/antlr/WhereClause.g:80:11: WS
                    {
                    mWS(); 


                    }
                    break;

            }


            match('<'); 

            // org/alfresco/rest/antlr/WhereClause.g:80:17: ( WS )?
            int alt11=2;
            switch ( input.LA(1) ) {
                case '\t':
                case '\n':
                case '\r':
                case ' ':
                    {
                    alt11=1;
                    }
                    break;
            }

            switch (alt11) {
                case 1 :
                    // org/alfresco/rest/antlr/WhereClause.g:80:17: WS
                    {
                    mWS(); 


                    }
                    break;

            }


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "LESSTHAN"

    // $ANTLR start "GREATERTHAN"
    public final void mGREATERTHAN() throws RecognitionException {
        try {
            int _type = GREATERTHAN;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // org/alfresco/rest/antlr/WhereClause.g:81:12: ( ( WS )? '>' ( WS )? )
            // org/alfresco/rest/antlr/WhereClause.g:81:14: ( WS )? '>' ( WS )?
            {
            // org/alfresco/rest/antlr/WhereClause.g:81:14: ( WS )?
            int alt12=2;
            switch ( input.LA(1) ) {
                case '\t':
                case '\n':
                case '\r':
                case ' ':
                    {
                    alt12=1;
                    }
                    break;
            }

            switch (alt12) {
                case 1 :
                    // org/alfresco/rest/antlr/WhereClause.g:81:14: WS
                    {
                    mWS(); 


                    }
                    break;

            }


            match('>'); 

            // org/alfresco/rest/antlr/WhereClause.g:81:20: ( WS )?
            int alt13=2;
            switch ( input.LA(1) ) {
                case '\t':
                case '\n':
                case '\r':
                case ' ':
                    {
                    alt13=1;
                    }
                    break;
            }

            switch (alt13) {
                case 1 :
                    // org/alfresco/rest/antlr/WhereClause.g:81:20: WS
                    {
                    mWS(); 


                    }
                    break;

            }


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "GREATERTHAN"

    // $ANTLR start "LESSTHANOREQUALS"
    public final void mLESSTHANOREQUALS() throws RecognitionException {
        try {
            int _type = LESSTHANOREQUALS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // org/alfresco/rest/antlr/WhereClause.g:82:17: ( ( WS )? '<=' ( WS )? )
            // org/alfresco/rest/antlr/WhereClause.g:82:19: ( WS )? '<=' ( WS )?
            {
            // org/alfresco/rest/antlr/WhereClause.g:82:19: ( WS )?
            int alt14=2;
            switch ( input.LA(1) ) {
                case '\t':
                case '\n':
                case '\r':
                case ' ':
                    {
                    alt14=1;
                    }
                    break;
            }

            switch (alt14) {
                case 1 :
                    // org/alfresco/rest/antlr/WhereClause.g:82:19: WS
                    {
                    mWS(); 


                    }
                    break;

            }


            match("<="); 



            // org/alfresco/rest/antlr/WhereClause.g:82:26: ( WS )?
            int alt15=2;
            switch ( input.LA(1) ) {
                case '\t':
                case '\n':
                case '\r':
                case ' ':
                    {
                    alt15=1;
                    }
                    break;
            }

            switch (alt15) {
                case 1 :
                    // org/alfresco/rest/antlr/WhereClause.g:82:26: WS
                    {
                    mWS(); 


                    }
                    break;

            }


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "LESSTHANOREQUALS"

    // $ANTLR start "GREATERTHANOREQUALS"
    public final void mGREATERTHANOREQUALS() throws RecognitionException {
        try {
            int _type = GREATERTHANOREQUALS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // org/alfresco/rest/antlr/WhereClause.g:83:20: ( ( WS )? '>=' ( WS )? )
            // org/alfresco/rest/antlr/WhereClause.g:83:22: ( WS )? '>=' ( WS )?
            {
            // org/alfresco/rest/antlr/WhereClause.g:83:22: ( WS )?
            int alt16=2;
            switch ( input.LA(1) ) {
                case '\t':
                case '\n':
                case '\r':
                case ' ':
                    {
                    alt16=1;
                    }
                    break;
            }

            switch (alt16) {
                case 1 :
                    // org/alfresco/rest/antlr/WhereClause.g:83:22: WS
                    {
                    mWS(); 


                    }
                    break;

            }


            match(">="); 



            // org/alfresco/rest/antlr/WhereClause.g:83:29: ( WS )?
            int alt17=2;
            switch ( input.LA(1) ) {
                case '\t':
                case '\n':
                case '\r':
                case ' ':
                    {
                    alt17=1;
                    }
                    break;
            }

            switch (alt17) {
                case 1 :
                    // org/alfresco/rest/antlr/WhereClause.g:83:29: WS
                    {
                    mWS(); 


                    }
                    break;

            }


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "GREATERTHANOREQUALS"

    // $ANTLR start "LEFTPAREN"
    public final void mLEFTPAREN() throws RecognitionException {
        try {
            int _type = LEFTPAREN;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // org/alfresco/rest/antlr/WhereClause.g:84:10: ( '(' )
            // org/alfresco/rest/antlr/WhereClause.g:84:12: '('
            {
            match('('); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "LEFTPAREN"

    // $ANTLR start "RIGHTPAREN"
    public final void mRIGHTPAREN() throws RecognitionException {
        try {
            int _type = RIGHTPAREN;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // org/alfresco/rest/antlr/WhereClause.g:85:11: ( ')' )
            // org/alfresco/rest/antlr/WhereClause.g:85:13: ')'
            {
            match(')'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "RIGHTPAREN"

    // $ANTLR start "COMMA"
    public final void mCOMMA() throws RecognitionException {
        try {
            int _type = COMMA;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // org/alfresco/rest/antlr/WhereClause.g:86:6: ( ',' )
            // org/alfresco/rest/antlr/WhereClause.g:86:8: ','
            {
            match(','); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "COMMA"

    // $ANTLR start "SINGLEQUOTE"
    public final void mSINGLEQUOTE() throws RecognitionException {
        try {
            int _type = SINGLEQUOTE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // org/alfresco/rest/antlr/WhereClause.g:87:12: ( '\\'' )
            // org/alfresco/rest/antlr/WhereClause.g:87:14: '\\''
            {
            match('\''); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "SINGLEQUOTE"

    // $ANTLR start "PROPERTYVALUE"
    public final void mPROPERTYVALUE() throws RecognitionException {
        try {
            int _type = PROPERTYVALUE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // org/alfresco/rest/antlr/WhereClause.g:88:14: ( ( SINGLEQUOTE (~ SINGLEQUOTE | '\\\\' SINGLEQUOTE )* SINGLEQUOTE ) | ( IDENTIFIERDIGIT )+ )
            int alt20=2;
            switch ( input.LA(1) ) {
            case '\'':
                {
                alt20=1;
                }
                break;
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
            case '\u0660':
            case '\u0661':
            case '\u0662':
            case '\u0663':
            case '\u0664':
            case '\u0665':
            case '\u0666':
            case '\u0667':
            case '\u0668':
            case '\u0669':
            case '\u06F0':
            case '\u06F1':
            case '\u06F2':
            case '\u06F3':
            case '\u06F4':
            case '\u06F5':
            case '\u06F6':
            case '\u06F7':
            case '\u06F8':
            case '\u06F9':
            case '\u0966':
            case '\u0967':
            case '\u0968':
            case '\u0969':
            case '\u096A':
            case '\u096B':
            case '\u096C':
            case '\u096D':
            case '\u096E':
            case '\u096F':
            case '\u09E6':
            case '\u09E7':
            case '\u09E8':
            case '\u09E9':
            case '\u09EA':
            case '\u09EB':
            case '\u09EC':
            case '\u09ED':
            case '\u09EE':
            case '\u09EF':
            case '\u0A66':
            case '\u0A67':
            case '\u0A68':
            case '\u0A69':
            case '\u0A6A':
            case '\u0A6B':
            case '\u0A6C':
            case '\u0A6D':
            case '\u0A6E':
            case '\u0A6F':
            case '\u0AE6':
            case '\u0AE7':
            case '\u0AE8':
            case '\u0AE9':
            case '\u0AEA':
            case '\u0AEB':
            case '\u0AEC':
            case '\u0AED':
            case '\u0AEE':
            case '\u0AEF':
            case '\u0B66':
            case '\u0B67':
            case '\u0B68':
            case '\u0B69':
            case '\u0B6A':
            case '\u0B6B':
            case '\u0B6C':
            case '\u0B6D':
            case '\u0B6E':
            case '\u0B6F':
            case '\u0BE7':
            case '\u0BE8':
            case '\u0BE9':
            case '\u0BEA':
            case '\u0BEB':
            case '\u0BEC':
            case '\u0BED':
            case '\u0BEE':
            case '\u0BEF':
            case '\u0C66':
            case '\u0C67':
            case '\u0C68':
            case '\u0C69':
            case '\u0C6A':
            case '\u0C6B':
            case '\u0C6C':
            case '\u0C6D':
            case '\u0C6E':
            case '\u0C6F':
            case '\u0CE6':
            case '\u0CE7':
            case '\u0CE8':
            case '\u0CE9':
            case '\u0CEA':
            case '\u0CEB':
            case '\u0CEC':
            case '\u0CED':
            case '\u0CEE':
            case '\u0CEF':
            case '\u0D66':
            case '\u0D67':
            case '\u0D68':
            case '\u0D69':
            case '\u0D6A':
            case '\u0D6B':
            case '\u0D6C':
            case '\u0D6D':
            case '\u0D6E':
            case '\u0D6F':
            case '\u0E50':
            case '\u0E51':
            case '\u0E52':
            case '\u0E53':
            case '\u0E54':
            case '\u0E55':
            case '\u0E56':
            case '\u0E57':
            case '\u0E58':
            case '\u0E59':
            case '\u0ED0':
            case '\u0ED1':
            case '\u0ED2':
            case '\u0ED3':
            case '\u0ED4':
            case '\u0ED5':
            case '\u0ED6':
            case '\u0ED7':
            case '\u0ED8':
            case '\u0ED9':
            case '\u1040':
            case '\u1041':
            case '\u1042':
            case '\u1043':
            case '\u1044':
            case '\u1045':
            case '\u1046':
            case '\u1047':
            case '\u1048':
            case '\u1049':
                {
                alt20=2;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 20, 0, input);

                throw nvae;

            }

            switch (alt20) {
                case 1 :
                    // org/alfresco/rest/antlr/WhereClause.g:88:16: ( SINGLEQUOTE (~ SINGLEQUOTE | '\\\\' SINGLEQUOTE )* SINGLEQUOTE )
                    {
                    // org/alfresco/rest/antlr/WhereClause.g:88:16: ( SINGLEQUOTE (~ SINGLEQUOTE | '\\\\' SINGLEQUOTE )* SINGLEQUOTE )
                    // org/alfresco/rest/antlr/WhereClause.g:88:17: SINGLEQUOTE (~ SINGLEQUOTE | '\\\\' SINGLEQUOTE )* SINGLEQUOTE
                    {
                    mSINGLEQUOTE(); 


                    // org/alfresco/rest/antlr/WhereClause.g:88:29: (~ SINGLEQUOTE | '\\\\' SINGLEQUOTE )*
                    loop18:
                    do {
                        int alt18=3;
                        int LA18_0 = input.LA(1);

                        if ( (LA18_0=='\\') ) {
                            int LA18_2 = input.LA(2);

                            if ( (LA18_2=='\'') ) {
                                int LA18_4 = input.LA(3);

                                if ( ((LA18_4 >= '\u0000' && LA18_4 <= '\uFFFF')) ) {
                                    alt18=2;
                                }

                                else {
                                    alt18=1;
                                }


                            }
                            else if ( ((LA18_2 >= '\u0000' && LA18_2 <= '&')||(LA18_2 >= '(' && LA18_2 <= '\uFFFF')) ) {
                                alt18=1;
                            }


                        }
                        else if ( ((LA18_0 >= '\u0000' && LA18_0 <= '&')||(LA18_0 >= '(' && LA18_0 <= '[')||(LA18_0 >= ']' && LA18_0 <= '\uFFFF')) ) {
                            alt18=1;
                        }


                        switch (alt18) {
                    	case 1 :
                    	    // org/alfresco/rest/antlr/WhereClause.g:88:30: ~ SINGLEQUOTE
                    	    {
                    	    if ( (input.LA(1) >= '\u0000' && input.LA(1) <= '\u0017')||(input.LA(1) >= '\u0019' && input.LA(1) <= '\uFFFF') ) {
                    	        input.consume();
                    	    }
                    	    else {
                    	        MismatchedSetException mse = new MismatchedSetException(null,input);
                    	        recover(mse);
                    	        throw mse;
                    	    }


                    	    }
                    	    break;
                    	case 2 :
                    	    // org/alfresco/rest/antlr/WhereClause.g:88:43: '\\\\' SINGLEQUOTE
                    	    {
                    	    match('\\'); 

                    	    mSINGLEQUOTE(); 


                    	    }
                    	    break;

                    	default :
                    	    break loop18;
                        }
                    } while (true);


                    mSINGLEQUOTE(); 


                    }


                    }
                    break;
                case 2 :
                    // org/alfresco/rest/antlr/WhereClause.g:88:75: ( IDENTIFIERDIGIT )+
                    {
                    // org/alfresco/rest/antlr/WhereClause.g:88:75: ( IDENTIFIERDIGIT )+
                    int cnt19=0;
                    loop19:
                    do {
                        int alt19=2;
                        switch ( input.LA(1) ) {
                        case '0':
                        case '1':
                        case '2':
                        case '3':
                        case '4':
                        case '5':
                        case '6':
                        case '7':
                        case '8':
                        case '9':
                        case '\u0660':
                        case '\u0661':
                        case '\u0662':
                        case '\u0663':
                        case '\u0664':
                        case '\u0665':
                        case '\u0666':
                        case '\u0667':
                        case '\u0668':
                        case '\u0669':
                        case '\u06F0':
                        case '\u06F1':
                        case '\u06F2':
                        case '\u06F3':
                        case '\u06F4':
                        case '\u06F5':
                        case '\u06F6':
                        case '\u06F7':
                        case '\u06F8':
                        case '\u06F9':
                        case '\u0966':
                        case '\u0967':
                        case '\u0968':
                        case '\u0969':
                        case '\u096A':
                        case '\u096B':
                        case '\u096C':
                        case '\u096D':
                        case '\u096E':
                        case '\u096F':
                        case '\u09E6':
                        case '\u09E7':
                        case '\u09E8':
                        case '\u09E9':
                        case '\u09EA':
                        case '\u09EB':
                        case '\u09EC':
                        case '\u09ED':
                        case '\u09EE':
                        case '\u09EF':
                        case '\u0A66':
                        case '\u0A67':
                        case '\u0A68':
                        case '\u0A69':
                        case '\u0A6A':
                        case '\u0A6B':
                        case '\u0A6C':
                        case '\u0A6D':
                        case '\u0A6E':
                        case '\u0A6F':
                        case '\u0AE6':
                        case '\u0AE7':
                        case '\u0AE8':
                        case '\u0AE9':
                        case '\u0AEA':
                        case '\u0AEB':
                        case '\u0AEC':
                        case '\u0AED':
                        case '\u0AEE':
                        case '\u0AEF':
                        case '\u0B66':
                        case '\u0B67':
                        case '\u0B68':
                        case '\u0B69':
                        case '\u0B6A':
                        case '\u0B6B':
                        case '\u0B6C':
                        case '\u0B6D':
                        case '\u0B6E':
                        case '\u0B6F':
                        case '\u0BE7':
                        case '\u0BE8':
                        case '\u0BE9':
                        case '\u0BEA':
                        case '\u0BEB':
                        case '\u0BEC':
                        case '\u0BED':
                        case '\u0BEE':
                        case '\u0BEF':
                        case '\u0C66':
                        case '\u0C67':
                        case '\u0C68':
                        case '\u0C69':
                        case '\u0C6A':
                        case '\u0C6B':
                        case '\u0C6C':
                        case '\u0C6D':
                        case '\u0C6E':
                        case '\u0C6F':
                        case '\u0CE6':
                        case '\u0CE7':
                        case '\u0CE8':
                        case '\u0CE9':
                        case '\u0CEA':
                        case '\u0CEB':
                        case '\u0CEC':
                        case '\u0CED':
                        case '\u0CEE':
                        case '\u0CEF':
                        case '\u0D66':
                        case '\u0D67':
                        case '\u0D68':
                        case '\u0D69':
                        case '\u0D6A':
                        case '\u0D6B':
                        case '\u0D6C':
                        case '\u0D6D':
                        case '\u0D6E':
                        case '\u0D6F':
                        case '\u0E50':
                        case '\u0E51':
                        case '\u0E52':
                        case '\u0E53':
                        case '\u0E54':
                        case '\u0E55':
                        case '\u0E56':
                        case '\u0E57':
                        case '\u0E58':
                        case '\u0E59':
                        case '\u0ED0':
                        case '\u0ED1':
                        case '\u0ED2':
                        case '\u0ED3':
                        case '\u0ED4':
                        case '\u0ED5':
                        case '\u0ED6':
                        case '\u0ED7':
                        case '\u0ED8':
                        case '\u0ED9':
                        case '\u1040':
                        case '\u1041':
                        case '\u1042':
                        case '\u1043':
                        case '\u1044':
                        case '\u1045':
                        case '\u1046':
                        case '\u1047':
                        case '\u1048':
                        case '\u1049':
                            {
                            alt19=1;
                            }
                            break;

                        }

                        switch (alt19) {
                    	case 1 :
                    	    // org/alfresco/rest/antlr/WhereClause.g:
                    	    {
                    	    if ( (input.LA(1) >= '0' && input.LA(1) <= '9')||(input.LA(1) >= '\u0660' && input.LA(1) <= '\u0669')||(input.LA(1) >= '\u06F0' && input.LA(1) <= '\u06F9')||(input.LA(1) >= '\u0966' && input.LA(1) <= '\u096F')||(input.LA(1) >= '\u09E6' && input.LA(1) <= '\u09EF')||(input.LA(1) >= '\u0A66' && input.LA(1) <= '\u0A6F')||(input.LA(1) >= '\u0AE6' && input.LA(1) <= '\u0AEF')||(input.LA(1) >= '\u0B66' && input.LA(1) <= '\u0B6F')||(input.LA(1) >= '\u0BE7' && input.LA(1) <= '\u0BEF')||(input.LA(1) >= '\u0C66' && input.LA(1) <= '\u0C6F')||(input.LA(1) >= '\u0CE6' && input.LA(1) <= '\u0CEF')||(input.LA(1) >= '\u0D66' && input.LA(1) <= '\u0D6F')||(input.LA(1) >= '\u0E50' && input.LA(1) <= '\u0E59')||(input.LA(1) >= '\u0ED0' && input.LA(1) <= '\u0ED9')||(input.LA(1) >= '\u1040' && input.LA(1) <= '\u1049') ) {
                    	        input.consume();
                    	    }
                    	    else {
                    	        MismatchedSetException mse = new MismatchedSetException(null,input);
                    	        recover(mse);
                    	        throw mse;
                    	    }


                    	    }
                    	    break;

                    	default :
                    	    if ( cnt19 >= 1 ) break loop19;
                                EarlyExitException eee =
                                    new EarlyExitException(19, input);
                                throw eee;
                        }
                        cnt19++;
                    } while (true);


                    }
                    break;

            }
            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "PROPERTYVALUE"

    // $ANTLR start "PROPERTYNAME"
    public final void mPROPERTYNAME() throws RecognitionException {
        try {
            int _type = PROPERTYNAME;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // org/alfresco/rest/antlr/WhereClause.g:89:13: ( ( '/' )? IDENTIFIER ( '/' IDENTIFIER )* )
            // org/alfresco/rest/antlr/WhereClause.g:89:15: ( '/' )? IDENTIFIER ( '/' IDENTIFIER )*
            {
            // org/alfresco/rest/antlr/WhereClause.g:89:15: ( '/' )?
            int alt21=2;
            switch ( input.LA(1) ) {
                case '/':
                    {
                    alt21=1;
                    }
                    break;
            }

            switch (alt21) {
                case 1 :
                    // org/alfresco/rest/antlr/WhereClause.g:89:15: '/'
                    {
                    match('/'); 

                    }
                    break;

            }


            mIDENTIFIER(); 


            // org/alfresco/rest/antlr/WhereClause.g:89:31: ( '/' IDENTIFIER )*
            loop22:
            do {
                int alt22=2;
                switch ( input.LA(1) ) {
                case '/':
                    {
                    alt22=1;
                    }
                    break;

                }

                switch (alt22) {
            	case 1 :
            	    // org/alfresco/rest/antlr/WhereClause.g:89:32: '/' IDENTIFIER
            	    {
            	    match('/'); 

            	    mIDENTIFIER(); 


            	    }
            	    break;

            	default :
            	    break loop22;
                }
            } while (true);


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "PROPERTYNAME"

    // $ANTLR start "IDENTIFIER"
    public final void mIDENTIFIER() throws RecognitionException {
        try {
            // org/alfresco/rest/antlr/WhereClause.g:90:21: ( ( IDENTIFIERLETTER ( IDENTIFIERLETTER | IDENTIFIERDIGIT )* ) )
            // org/alfresco/rest/antlr/WhereClause.g:90:23: ( IDENTIFIERLETTER ( IDENTIFIERLETTER | IDENTIFIERDIGIT )* )
            {
            // org/alfresco/rest/antlr/WhereClause.g:90:23: ( IDENTIFIERLETTER ( IDENTIFIERLETTER | IDENTIFIERDIGIT )* )
            // org/alfresco/rest/antlr/WhereClause.g:90:24: IDENTIFIERLETTER ( IDENTIFIERLETTER | IDENTIFIERDIGIT )*
            {
            mIDENTIFIERLETTER(); 


            // org/alfresco/rest/antlr/WhereClause.g:90:41: ( IDENTIFIERLETTER | IDENTIFIERDIGIT )*
            loop23:
            do {
                int alt23=2;
                int LA23_0 = input.LA(1);

                if ( ((LA23_0 >= '0' && LA23_0 <= '9')||(LA23_0 >= 'A' && LA23_0 <= 'Z')||LA23_0=='_'||(LA23_0 >= 'a' && LA23_0 <= 'z')||(LA23_0 >= '\u00C0' && LA23_0 <= '\u00D6')||(LA23_0 >= '\u00D8' && LA23_0 <= '\u00F6')||(LA23_0 >= '\u00F8' && LA23_0 <= '\u1FFF')||(LA23_0 >= '\u3040' && LA23_0 <= '\u318F')||(LA23_0 >= '\u3300' && LA23_0 <= '\u337F')||(LA23_0 >= '\u3400' && LA23_0 <= '\u3D2D')||(LA23_0 >= '\u4E00' && LA23_0 <= '\u9FFF')||(LA23_0 >= '\uF900' && LA23_0 <= '\uFAFF')) ) {
                    alt23=1;
                }


                switch (alt23) {
            	case 1 :
            	    // org/alfresco/rest/antlr/WhereClause.g:
            	    {
            	    if ( (input.LA(1) >= '0' && input.LA(1) <= '9')||(input.LA(1) >= 'A' && input.LA(1) <= 'Z')||input.LA(1)=='_'||(input.LA(1) >= 'a' && input.LA(1) <= 'z')||(input.LA(1) >= '\u00C0' && input.LA(1) <= '\u00D6')||(input.LA(1) >= '\u00D8' && input.LA(1) <= '\u00F6')||(input.LA(1) >= '\u00F8' && input.LA(1) <= '\u1FFF')||(input.LA(1) >= '\u3040' && input.LA(1) <= '\u318F')||(input.LA(1) >= '\u3300' && input.LA(1) <= '\u337F')||(input.LA(1) >= '\u3400' && input.LA(1) <= '\u3D2D')||(input.LA(1) >= '\u4E00' && input.LA(1) <= '\u9FFF')||(input.LA(1) >= '\uF900' && input.LA(1) <= '\uFAFF') ) {
            	        input.consume();
            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;
            	    }


            	    }
            	    break;

            	default :
            	    break loop23;
                }
            } while (true);


            }


            }


        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "IDENTIFIER"

    // $ANTLR start "WS"
    public final void mWS() throws RecognitionException {
        try {
            int _type = WS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // org/alfresco/rest/antlr/WhereClause.g:91:4: ( ( ' ' | '\\t' | '\\r' | '\\n' )+ )
            // org/alfresco/rest/antlr/WhereClause.g:91:6: ( ' ' | '\\t' | '\\r' | '\\n' )+
            {
            // org/alfresco/rest/antlr/WhereClause.g:91:6: ( ' ' | '\\t' | '\\r' | '\\n' )+
            int cnt24=0;
            loop24:
            do {
                int alt24=2;
                switch ( input.LA(1) ) {
                case '\t':
                case '\n':
                case '\r':
                case ' ':
                    {
                    alt24=1;
                    }
                    break;

                }

                switch (alt24) {
            	case 1 :
            	    // org/alfresco/rest/antlr/WhereClause.g:
            	    {
            	    if ( (input.LA(1) >= '\t' && input.LA(1) <= '\n')||input.LA(1)=='\r'||input.LA(1)==' ' ) {
            	        input.consume();
            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;
            	    }


            	    }
            	    break;

            	default :
            	    if ( cnt24 >= 1 ) break loop24;
                        EarlyExitException eee =
                            new EarlyExitException(24, input);
                        throw eee;
                }
                cnt24++;
            } while (true);


             _channel = HIDDEN; 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "WS"

    // $ANTLR start "IDENTIFIERLETTER"
    public final void mIDENTIFIERLETTER() throws RecognitionException {
        try {
            // org/alfresco/rest/antlr/WhereClause.g:93:5: ( '\\u0041' .. '\\u005a' | '\\u005f' | '\\u0061' .. '\\u007a' | '\\u00c0' .. '\\u00d6' | '\\u00d8' .. '\\u00f6' | '\\u00f8' .. '\\u00ff' | '\\u0100' .. '\\u1fff' | '\\u3040' .. '\\u318f' | '\\u3300' .. '\\u337f' | '\\u3400' .. '\\u3d2d' | '\\u4e00' .. '\\u9fff' | '\\uf900' .. '\\ufaff' )
            // org/alfresco/rest/antlr/WhereClause.g:
            {
            if ( (input.LA(1) >= 'A' && input.LA(1) <= 'Z')||input.LA(1)=='_'||(input.LA(1) >= 'a' && input.LA(1) <= 'z')||(input.LA(1) >= '\u00C0' && input.LA(1) <= '\u00D6')||(input.LA(1) >= '\u00D8' && input.LA(1) <= '\u00F6')||(input.LA(1) >= '\u00F8' && input.LA(1) <= '\u1FFF')||(input.LA(1) >= '\u3040' && input.LA(1) <= '\u318F')||(input.LA(1) >= '\u3300' && input.LA(1) <= '\u337F')||(input.LA(1) >= '\u3400' && input.LA(1) <= '\u3D2D')||(input.LA(1) >= '\u4E00' && input.LA(1) <= '\u9FFF')||(input.LA(1) >= '\uF900' && input.LA(1) <= '\uFAFF') ) {
                input.consume();
            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;
            }


            }


        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "IDENTIFIERLETTER"

    // $ANTLR start "IDENTIFIERDIGIT"
    public final void mIDENTIFIERDIGIT() throws RecognitionException {
        try {
            // org/alfresco/rest/antlr/WhereClause.g:107:5: ( '\\u0030' .. '\\u0039' | '\\u0660' .. '\\u0669' | '\\u06f0' .. '\\u06f9' | '\\u0966' .. '\\u096f' | '\\u09e6' .. '\\u09ef' | '\\u0a66' .. '\\u0a6f' | '\\u0ae6' .. '\\u0aef' | '\\u0b66' .. '\\u0b6f' | '\\u0be7' .. '\\u0bef' | '\\u0c66' .. '\\u0c6f' | '\\u0ce6' .. '\\u0cef' | '\\u0d66' .. '\\u0d6f' | '\\u0e50' .. '\\u0e59' | '\\u0ed0' .. '\\u0ed9' | '\\u1040' .. '\\u1049' )
            // org/alfresco/rest/antlr/WhereClause.g:
            {
            if ( (input.LA(1) >= '0' && input.LA(1) <= '9')||(input.LA(1) >= '\u0660' && input.LA(1) <= '\u0669')||(input.LA(1) >= '\u06F0' && input.LA(1) <= '\u06F9')||(input.LA(1) >= '\u0966' && input.LA(1) <= '\u096F')||(input.LA(1) >= '\u09E6' && input.LA(1) <= '\u09EF')||(input.LA(1) >= '\u0A66' && input.LA(1) <= '\u0A6F')||(input.LA(1) >= '\u0AE6' && input.LA(1) <= '\u0AEF')||(input.LA(1) >= '\u0B66' && input.LA(1) <= '\u0B6F')||(input.LA(1) >= '\u0BE7' && input.LA(1) <= '\u0BEF')||(input.LA(1) >= '\u0C66' && input.LA(1) <= '\u0C6F')||(input.LA(1) >= '\u0CE6' && input.LA(1) <= '\u0CEF')||(input.LA(1) >= '\u0D66' && input.LA(1) <= '\u0D6F')||(input.LA(1) >= '\u0E50' && input.LA(1) <= '\u0E59')||(input.LA(1) >= '\u0ED0' && input.LA(1) <= '\u0ED9')||(input.LA(1) >= '\u1040' && input.LA(1) <= '\u1049') ) {
                input.consume();
            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;
            }


            }


        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "IDENTIFIERDIGIT"

    public void mTokens() throws RecognitionException {
        // org/alfresco/rest/antlr/WhereClause.g:1:8: ( NEGATION | EXISTS | IN | MATCHES | BETWEEN | OR | AND | EQUALS | LESSTHAN | GREATERTHAN | LESSTHANOREQUALS | GREATERTHANOREQUALS | LEFTPAREN | RIGHTPAREN | COMMA | SINGLEQUOTE | PROPERTYVALUE | PROPERTYNAME | WS )
        int alt25=19;
        alt25 = dfa25.predict(input);
        switch (alt25) {
            case 1 :
                // org/alfresco/rest/antlr/WhereClause.g:1:10: NEGATION
                {
                mNEGATION(); 


                }
                break;
            case 2 :
                // org/alfresco/rest/antlr/WhereClause.g:1:19: EXISTS
                {
                mEXISTS(); 


                }
                break;
            case 3 :
                // org/alfresco/rest/antlr/WhereClause.g:1:26: IN
                {
                mIN(); 


                }
                break;
            case 4 :
                // org/alfresco/rest/antlr/WhereClause.g:1:29: MATCHES
                {
                mMATCHES(); 


                }
                break;
            case 5 :
                // org/alfresco/rest/antlr/WhereClause.g:1:37: BETWEEN
                {
                mBETWEEN(); 


                }
                break;
            case 6 :
                // org/alfresco/rest/antlr/WhereClause.g:1:45: OR
                {
                mOR(); 


                }
                break;
            case 7 :
                // org/alfresco/rest/antlr/WhereClause.g:1:48: AND
                {
                mAND(); 


                }
                break;
            case 8 :
                // org/alfresco/rest/antlr/WhereClause.g:1:52: EQUALS
                {
                mEQUALS(); 


                }
                break;
            case 9 :
                // org/alfresco/rest/antlr/WhereClause.g:1:59: LESSTHAN
                {
                mLESSTHAN(); 


                }
                break;
            case 10 :
                // org/alfresco/rest/antlr/WhereClause.g:1:68: GREATERTHAN
                {
                mGREATERTHAN(); 


                }
                break;
            case 11 :
                // org/alfresco/rest/antlr/WhereClause.g:1:80: LESSTHANOREQUALS
                {
                mLESSTHANOREQUALS(); 


                }
                break;
            case 12 :
                // org/alfresco/rest/antlr/WhereClause.g:1:97: GREATERTHANOREQUALS
                {
                mGREATERTHANOREQUALS(); 


                }
                break;
            case 13 :
                // org/alfresco/rest/antlr/WhereClause.g:1:117: LEFTPAREN
                {
                mLEFTPAREN(); 


                }
                break;
            case 14 :
                // org/alfresco/rest/antlr/WhereClause.g:1:127: RIGHTPAREN
                {
                mRIGHTPAREN(); 


                }
                break;
            case 15 :
                // org/alfresco/rest/antlr/WhereClause.g:1:138: COMMA
                {
                mCOMMA(); 


                }
                break;
            case 16 :
                // org/alfresco/rest/antlr/WhereClause.g:1:144: SINGLEQUOTE
                {
                mSINGLEQUOTE(); 


                }
                break;
            case 17 :
                // org/alfresco/rest/antlr/WhereClause.g:1:156: PROPERTYVALUE
                {
                mPROPERTYVALUE(); 


                }
                break;
            case 18 :
                // org/alfresco/rest/antlr/WhereClause.g:1:170: PROPERTYNAME
                {
                mPROPERTYNAME(); 


                }
                break;
            case 19 :
                // org/alfresco/rest/antlr/WhereClause.g:1:183: WS
                {
                mWS(); 


                }
                break;

        }

    }


    protected DFA25 dfa25 = new DFA25(this);
    static final String DFA25_eotS =
        "\1\uffff\4\16\1\24\1\uffff\1\33\1\35\3\uffff\1\36\1\17\2\uffff\4"+
        "\16\13\uffff\1\17\4\16\1\uffff\4\16\2\53\1\uffff";
    static final String DFA25_eofS =
        "\54\uffff";
    static final String DFA25_minS =
        "\1\11\1\157\1\117\1\170\1\130\1\11\1\uffff\2\75\3\uffff\1\0\1\57"+
        "\2\uffff\1\164\1\124\1\151\1\111\13\uffff\1\57\2\11\1\163\1\123"+
        "\1\uffff\1\164\1\124\1\163\1\123\2\57\1\uffff";
    static final String DFA25_maxS =
        "\1\ufaff\1\157\1\117\1\170\1\130\1\157\1\uffff\2\75\3\uffff\1\uffff"+
        "\1\ufaff\2\uffff\1\164\1\124\1\151\1\111\13\uffff\1\ufaff\2\40\1"+
        "\163\1\123\1\uffff\1\164\1\124\1\163\1\123\2\ufaff\1\uffff";
    static final String DFA25_acceptS =
        "\6\uffff\1\10\2\uffff\1\15\1\16\1\17\2\uffff\1\22\1\21\4\uffff\1"+
        "\23\1\3\1\4\1\5\1\6\1\7\1\13\1\11\1\14\1\12\1\20\5\uffff\1\1\6\uffff"+
        "\1\2";
    static final String DFA25_specialS =
        "\14\uffff\1\0\37\uffff}>";
    static final String[] DFA25_transitionS = {
            "\2\5\2\uffff\1\5\22\uffff\1\5\6\uffff\1\14\1\11\1\12\2\uffff"+
            "\1\13\2\uffff\1\16\12\17\2\uffff\1\7\1\6\1\10\2\uffff\4\16\1"+
            "\4\10\16\1\2\14\16\4\uffff\1\16\1\uffff\4\16\1\3\10\16\1\1\14"+
            "\16\105\uffff\27\16\1\uffff\37\16\1\uffff\u0568\16\12\15\u0086"+
            "\16\12\15\u026c\16\12\15\166\16\12\15\166\16\12\15\166\16\12"+
            "\15\166\16\12\15\167\16\11\15\166\16\12\15\166\16\12\15\166"+
            "\16\12\15\u00e0\16\12\15\166\16\12\15\u0166\16\12\15\u0fb6\16"+
            "\u1040\uffff\u0150\16\u0170\uffff\u0080\16\u0080\uffff\u092e"+
            "\16\u10d2\uffff\u5200\16\u5900\uffff\u0200\16",
            "\1\20",
            "\1\21",
            "\1\22",
            "\1\23",
            "\2\5\2\uffff\1\5\22\uffff\1\5\33\uffff\1\7\1\6\1\10\2\uffff"+
            "\1\31\1\27\6\uffff\1\25\3\uffff\1\26\1\uffff\1\30\21\uffff\1"+
            "\31\1\27\6\uffff\1\25\3\uffff\1\26\1\uffff\1\30",
            "",
            "\1\32",
            "\1\34",
            "",
            "",
            "",
            "\0\17",
            "\1\16\12\37\7\uffff\32\16\4\uffff\1\16\1\uffff\32\16\105\uffff"+
            "\27\16\1\uffff\37\16\1\uffff\u0568\16\12\37\u0086\16\12\37\u026c"+
            "\16\12\37\166\16\12\37\166\16\12\37\166\16\12\37\166\16\12\37"+
            "\167\16\11\37\166\16\12\37\166\16\12\37\166\16\12\37\u00e0\16"+
            "\12\37\166\16\12\37\u0166\16\12\37\u0fb6\16\u1040\uffff\u0150"+
            "\16\u0170\uffff\u0080\16\u0080\uffff\u092e\16\u10d2\uffff\u5200"+
            "\16\u5900\uffff\u0200\16",
            "",
            "",
            "\1\40",
            "\1\41",
            "\1\42",
            "\1\43",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\16\12\37\7\uffff\32\16\4\uffff\1\16\1\uffff\32\16\105\uffff"+
            "\27\16\1\uffff\37\16\1\uffff\u0568\16\12\37\u0086\16\12\37\u026c"+
            "\16\12\37\166\16\12\37\166\16\12\37\166\16\12\37\166\16\12\37"+
            "\167\16\11\37\166\16\12\37\166\16\12\37\166\16\12\37\u00e0\16"+
            "\12\37\166\16\12\37\u0166\16\12\37\u0fb6\16\u1040\uffff\u0150"+
            "\16\u0170\uffff\u0080\16\u0080\uffff\u092e\16\u10d2\uffff\u5200"+
            "\16\u5900\uffff\u0200\16",
            "\2\44\2\uffff\1\44\22\uffff\1\44",
            "\2\44\2\uffff\1\44\22\uffff\1\44",
            "\1\45",
            "\1\46",
            "",
            "\1\47",
            "\1\50",
            "\1\51",
            "\1\52",
            "\13\16\7\uffff\32\16\4\uffff\1\16\1\uffff\32\16\105\uffff\27"+
            "\16\1\uffff\37\16\1\uffff\u1f08\16\u1040\uffff\u0150\16\u0170"+
            "\uffff\u0080\16\u0080\uffff\u092e\16\u10d2\uffff\u5200\16\u5900"+
            "\uffff\u0200\16",
            "\13\16\7\uffff\32\16\4\uffff\1\16\1\uffff\32\16\105\uffff\27"+
            "\16\1\uffff\37\16\1\uffff\u1f08\16\u1040\uffff\u0150\16\u0170"+
            "\uffff\u0080\16\u0080\uffff\u092e\16\u10d2\uffff\u5200\16\u5900"+
            "\uffff\u0200\16",
            ""
    };

    static final short[] DFA25_eot = DFA.unpackEncodedString(DFA25_eotS);
    static final short[] DFA25_eof = DFA.unpackEncodedString(DFA25_eofS);
    static final char[] DFA25_min = DFA.unpackEncodedStringToUnsignedChars(DFA25_minS);
    static final char[] DFA25_max = DFA.unpackEncodedStringToUnsignedChars(DFA25_maxS);
    static final short[] DFA25_accept = DFA.unpackEncodedString(DFA25_acceptS);
    static final short[] DFA25_special = DFA.unpackEncodedString(DFA25_specialS);
    static final short[][] DFA25_transition;

    static {
        int numStates = DFA25_transitionS.length;
        DFA25_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA25_transition[i] = DFA.unpackEncodedString(DFA25_transitionS[i]);
        }
    }

    class DFA25 extends DFA {

        public DFA25(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 25;
            this.eot = DFA25_eot;
            this.eof = DFA25_eof;
            this.min = DFA25_min;
            this.max = DFA25_max;
            this.accept = DFA25_accept;
            this.special = DFA25_special;
            this.transition = DFA25_transition;
        }
        public String getDescription() {
            return "1:1: Tokens : ( NEGATION | EXISTS | IN | MATCHES | BETWEEN | OR | AND | EQUALS | LESSTHAN | GREATERTHAN | LESSTHANOREQUALS | GREATERTHANOREQUALS | LEFTPAREN | RIGHTPAREN | COMMA | SINGLEQUOTE | PROPERTYVALUE | PROPERTYNAME | WS );";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            IntStream input = _input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA25_12 = input.LA(1);

                        s = -1;
                        if ( ((LA25_12 >= '\u0000' && LA25_12 <= '\uFFFF')) ) {s = 15;}

                        else s = 30;

                        if ( s>=0 ) return s;
                        break;
            }
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 25, _s, input);
            error(nvae);
            throw nvae;
        }

    }
 

}