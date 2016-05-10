// $ANTLR 3.4 org/alfresco/rest/antlr/WhereClause.g 2015-12-22 09:31:21

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
    public static final int COLON=6;
    public static final int COMMA=7;
    public static final int EQUALS=8;
    public static final int EXISTS=9;
    public static final int GREATERTHAN=10;
    public static final int GREATERTHANOREQUALS=11;
    public static final int IDENTIFIER=12;
    public static final int IDENTIFIERDIGIT=13;
    public static final int IDENTIFIERLETTER=14;
    public static final int IDENTIFIERLETTERORDIGIT=15;
    public static final int IN=16;
    public static final int LEFTPAREN=17;
    public static final int LESSTHAN=18;
    public static final int LESSTHANOREQUALS=19;
    public static final int MATCHES=20;
    public static final int NEGATION=21;
    public static final int OR=22;
    public static final int PROPERTYNAME=23;
    public static final int PROPERTYVALUE=24;
    public static final int RIGHTPAREN=25;
    public static final int SINGLEQUOTE=26;
    public static final int WS=27;


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
            int LA1_0 = input.LA(1);

            if ( (LA1_0=='n') ) {
                alt1=1;
            }
            else if ( (LA1_0=='N') ) {
                alt1=2;
            }
            else {
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
            int LA2_0 = input.LA(1);

            if ( (LA2_0=='e') ) {
                alt2=1;
            }
            else if ( (LA2_0=='E') ) {
                alt2=2;
            }
            else {
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
            int LA3_0 = input.LA(1);

            if ( (LA3_0=='i') ) {
                alt3=1;
            }
            else if ( (LA3_0=='I') ) {
                alt3=2;
            }
            else {
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
            int LA4_0 = input.LA(1);

            if ( (LA4_0=='m') ) {
                alt4=1;
            }
            else if ( (LA4_0=='M') ) {
                alt4=2;
            }
            else {
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
            int LA5_0 = input.LA(1);

            if ( (LA5_0=='b') ) {
                alt5=1;
            }
            else if ( (LA5_0=='B') ) {
                alt5=2;
            }
            else {
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
            int LA6_0 = input.LA(1);

            if ( (LA6_0=='o') ) {
                alt6=1;
            }
            else if ( (LA6_0=='O') ) {
                alt6=2;
            }
            else {
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
            int LA7_0 = input.LA(1);

            if ( (LA7_0=='a') ) {
                alt7=1;
            }
            else if ( (LA7_0=='A') ) {
                alt7=2;
            }
            else {
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
            int LA8_0 = input.LA(1);

            if ( ((LA8_0 >= '\t' && LA8_0 <= '\n')||LA8_0=='\r'||LA8_0==' ') ) {
                alt8=1;
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
            int LA9_0 = input.LA(1);

            if ( ((LA9_0 >= '\t' && LA9_0 <= '\n')||LA9_0=='\r'||LA9_0==' ') ) {
                alt9=1;
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
            int LA10_0 = input.LA(1);

            if ( ((LA10_0 >= '\t' && LA10_0 <= '\n')||LA10_0=='\r'||LA10_0==' ') ) {
                alt10=1;
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
            int LA11_0 = input.LA(1);

            if ( ((LA11_0 >= '\t' && LA11_0 <= '\n')||LA11_0=='\r'||LA11_0==' ') ) {
                alt11=1;
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
            int LA12_0 = input.LA(1);

            if ( ((LA12_0 >= '\t' && LA12_0 <= '\n')||LA12_0=='\r'||LA12_0==' ') ) {
                alt12=1;
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
            int LA13_0 = input.LA(1);

            if ( ((LA13_0 >= '\t' && LA13_0 <= '\n')||LA13_0=='\r'||LA13_0==' ') ) {
                alt13=1;
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
            int LA14_0 = input.LA(1);

            if ( ((LA14_0 >= '\t' && LA14_0 <= '\n')||LA14_0=='\r'||LA14_0==' ') ) {
                alt14=1;
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
            int LA15_0 = input.LA(1);

            if ( ((LA15_0 >= '\t' && LA15_0 <= '\n')||LA15_0=='\r'||LA15_0==' ') ) {
                alt15=1;
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
            int LA16_0 = input.LA(1);

            if ( ((LA16_0 >= '\t' && LA16_0 <= '\n')||LA16_0=='\r'||LA16_0==' ') ) {
                alt16=1;
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
            int LA17_0 = input.LA(1);

            if ( ((LA17_0 >= '\t' && LA17_0 <= '\n')||LA17_0=='\r'||LA17_0==' ') ) {
                alt17=1;
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

    // $ANTLR start "COLON"
    public final void mCOLON() throws RecognitionException {
        try {
            int _type = COLON;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // org/alfresco/rest/antlr/WhereClause.g:87:6: ( ':' )
            // org/alfresco/rest/antlr/WhereClause.g:87:8: ':'
            {
            match(':'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "COLON"

    // $ANTLR start "SINGLEQUOTE"
    public final void mSINGLEQUOTE() throws RecognitionException {
        try {
            int _type = SINGLEQUOTE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // org/alfresco/rest/antlr/WhereClause.g:88:12: ( '\\'' )
            // org/alfresco/rest/antlr/WhereClause.g:88:14: '\\''
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
            // org/alfresco/rest/antlr/WhereClause.g:89:14: ( ( SINGLEQUOTE (~ SINGLEQUOTE | '\\\\' SINGLEQUOTE )* SINGLEQUOTE ) | ( IDENTIFIERDIGIT )+ )
            int alt20=2;
            int LA20_0 = input.LA(1);

            if ( (LA20_0=='\'') ) {
                alt20=1;
            }
            else if ( ((LA20_0 >= '0' && LA20_0 <= '9')||(LA20_0 >= '\u0660' && LA20_0 <= '\u0669')||(LA20_0 >= '\u06F0' && LA20_0 <= '\u06F9')||(LA20_0 >= '\u0966' && LA20_0 <= '\u096F')||(LA20_0 >= '\u09E6' && LA20_0 <= '\u09EF')||(LA20_0 >= '\u0A66' && LA20_0 <= '\u0A6F')||(LA20_0 >= '\u0AE6' && LA20_0 <= '\u0AEF')||(LA20_0 >= '\u0B66' && LA20_0 <= '\u0B6F')||(LA20_0 >= '\u0BE7' && LA20_0 <= '\u0BEF')||(LA20_0 >= '\u0C66' && LA20_0 <= '\u0C6F')||(LA20_0 >= '\u0CE6' && LA20_0 <= '\u0CEF')||(LA20_0 >= '\u0D66' && LA20_0 <= '\u0D6F')||(LA20_0 >= '\u0E50' && LA20_0 <= '\u0E59')||(LA20_0 >= '\u0ED0' && LA20_0 <= '\u0ED9')||(LA20_0 >= '\u1040' && LA20_0 <= '\u1049')) ) {
                alt20=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 20, 0, input);

                throw nvae;

            }
            switch (alt20) {
                case 1 :
                    // org/alfresco/rest/antlr/WhereClause.g:89:16: ( SINGLEQUOTE (~ SINGLEQUOTE | '\\\\' SINGLEQUOTE )* SINGLEQUOTE )
                    {
                    // org/alfresco/rest/antlr/WhereClause.g:89:16: ( SINGLEQUOTE (~ SINGLEQUOTE | '\\\\' SINGLEQUOTE )* SINGLEQUOTE )
                    // org/alfresco/rest/antlr/WhereClause.g:89:17: SINGLEQUOTE (~ SINGLEQUOTE | '\\\\' SINGLEQUOTE )* SINGLEQUOTE
                    {
                    mSINGLEQUOTE(); 


                    // org/alfresco/rest/antlr/WhereClause.g:89:29: (~ SINGLEQUOTE | '\\\\' SINGLEQUOTE )*
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
                    	    // org/alfresco/rest/antlr/WhereClause.g:89:30: ~ SINGLEQUOTE
                    	    {
                    	    if ( (input.LA(1) >= '\u0000' && input.LA(1) <= '\u0019')||(input.LA(1) >= '\u001B' && input.LA(1) <= '\uFFFF') ) {
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
                    	    // org/alfresco/rest/antlr/WhereClause.g:89:43: '\\\\' SINGLEQUOTE
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
                    // org/alfresco/rest/antlr/WhereClause.g:89:75: ( IDENTIFIERDIGIT )+
                    {
                    // org/alfresco/rest/antlr/WhereClause.g:89:75: ( IDENTIFIERDIGIT )+
                    int cnt19=0;
                    loop19:
                    do {
                        int alt19=2;
                        int LA19_0 = input.LA(1);

                        if ( ((LA19_0 >= '0' && LA19_0 <= '9')||(LA19_0 >= '\u0660' && LA19_0 <= '\u0669')||(LA19_0 >= '\u06F0' && LA19_0 <= '\u06F9')||(LA19_0 >= '\u0966' && LA19_0 <= '\u096F')||(LA19_0 >= '\u09E6' && LA19_0 <= '\u09EF')||(LA19_0 >= '\u0A66' && LA19_0 <= '\u0A6F')||(LA19_0 >= '\u0AE6' && LA19_0 <= '\u0AEF')||(LA19_0 >= '\u0B66' && LA19_0 <= '\u0B6F')||(LA19_0 >= '\u0BE7' && LA19_0 <= '\u0BEF')||(LA19_0 >= '\u0C66' && LA19_0 <= '\u0C6F')||(LA19_0 >= '\u0CE6' && LA19_0 <= '\u0CEF')||(LA19_0 >= '\u0D66' && LA19_0 <= '\u0D6F')||(LA19_0 >= '\u0E50' && LA19_0 <= '\u0E59')||(LA19_0 >= '\u0ED0' && LA19_0 <= '\u0ED9')||(LA19_0 >= '\u1040' && LA19_0 <= '\u1049')) ) {
                            alt19=1;
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
            // org/alfresco/rest/antlr/WhereClause.g:90:13: ( ( '/' )? IDENTIFIER ( '/' IDENTIFIER )* )
            // org/alfresco/rest/antlr/WhereClause.g:90:15: ( '/' )? IDENTIFIER ( '/' IDENTIFIER )*
            {
            // org/alfresco/rest/antlr/WhereClause.g:90:15: ( '/' )?
            int alt21=2;
            int LA21_0 = input.LA(1);

            if ( (LA21_0=='/') ) {
                alt21=1;
            }
            switch (alt21) {
                case 1 :
                    // org/alfresco/rest/antlr/WhereClause.g:90:15: '/'
                    {
                    match('/'); 

                    }
                    break;

            }


            mIDENTIFIER(); 


            // org/alfresco/rest/antlr/WhereClause.g:90:31: ( '/' IDENTIFIER )*
            loop22:
            do {
                int alt22=2;
                int LA22_0 = input.LA(1);

                if ( (LA22_0=='/') ) {
                    alt22=1;
                }


                switch (alt22) {
            	case 1 :
            	    // org/alfresco/rest/antlr/WhereClause.g:90:32: '/' IDENTIFIER
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

    // $ANTLR start "IDENTIFIERLETTERORDIGIT"
    public final void mIDENTIFIERLETTERORDIGIT() throws RecognitionException {
        try {
            // org/alfresco/rest/antlr/WhereClause.g:91:33: ( ( IDENTIFIERLETTER | IDENTIFIERDIGIT ) )
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


        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "IDENTIFIERLETTERORDIGIT"

    // $ANTLR start "IDENTIFIER"
    public final void mIDENTIFIER() throws RecognitionException {
        try {
            // org/alfresco/rest/antlr/WhereClause.g:92:21: ( ( IDENTIFIERLETTER ( ( IDENTIFIERLETTERORDIGIT )* | ( ( IDENTIFIERLETTERORDIGIT )* COLON ( IDENTIFIERLETTERORDIGIT )* ) ) ) )
            // org/alfresco/rest/antlr/WhereClause.g:92:23: ( IDENTIFIERLETTER ( ( IDENTIFIERLETTERORDIGIT )* | ( ( IDENTIFIERLETTERORDIGIT )* COLON ( IDENTIFIERLETTERORDIGIT )* ) ) )
            {
            // org/alfresco/rest/antlr/WhereClause.g:92:23: ( IDENTIFIERLETTER ( ( IDENTIFIERLETTERORDIGIT )* | ( ( IDENTIFIERLETTERORDIGIT )* COLON ( IDENTIFIERLETTERORDIGIT )* ) ) )
            // org/alfresco/rest/antlr/WhereClause.g:92:24: IDENTIFIERLETTER ( ( IDENTIFIERLETTERORDIGIT )* | ( ( IDENTIFIERLETTERORDIGIT )* COLON ( IDENTIFIERLETTERORDIGIT )* ) )
            {
            mIDENTIFIERLETTER(); 


            // org/alfresco/rest/antlr/WhereClause.g:92:41: ( ( IDENTIFIERLETTERORDIGIT )* | ( ( IDENTIFIERLETTERORDIGIT )* COLON ( IDENTIFIERLETTERORDIGIT )* ) )
            int alt26=2;
            alt26 = dfa26.predict(input);
            switch (alt26) {
                case 1 :
                    // org/alfresco/rest/antlr/WhereClause.g:92:42: ( IDENTIFIERLETTERORDIGIT )*
                    {
                    // org/alfresco/rest/antlr/WhereClause.g:92:42: ( IDENTIFIERLETTERORDIGIT )*
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
                    break;
                case 2 :
                    // org/alfresco/rest/antlr/WhereClause.g:92:69: ( ( IDENTIFIERLETTERORDIGIT )* COLON ( IDENTIFIERLETTERORDIGIT )* )
                    {
                    // org/alfresco/rest/antlr/WhereClause.g:92:69: ( ( IDENTIFIERLETTERORDIGIT )* COLON ( IDENTIFIERLETTERORDIGIT )* )
                    // org/alfresco/rest/antlr/WhereClause.g:92:70: ( IDENTIFIERLETTERORDIGIT )* COLON ( IDENTIFIERLETTERORDIGIT )*
                    {
                    // org/alfresco/rest/antlr/WhereClause.g:92:70: ( IDENTIFIERLETTERORDIGIT )*
                    loop24:
                    do {
                        int alt24=2;
                        int LA24_0 = input.LA(1);

                        if ( ((LA24_0 >= '0' && LA24_0 <= '9')||(LA24_0 >= 'A' && LA24_0 <= 'Z')||LA24_0=='_'||(LA24_0 >= 'a' && LA24_0 <= 'z')||(LA24_0 >= '\u00C0' && LA24_0 <= '\u00D6')||(LA24_0 >= '\u00D8' && LA24_0 <= '\u00F6')||(LA24_0 >= '\u00F8' && LA24_0 <= '\u1FFF')||(LA24_0 >= '\u3040' && LA24_0 <= '\u318F')||(LA24_0 >= '\u3300' && LA24_0 <= '\u337F')||(LA24_0 >= '\u3400' && LA24_0 <= '\u3D2D')||(LA24_0 >= '\u4E00' && LA24_0 <= '\u9FFF')||(LA24_0 >= '\uF900' && LA24_0 <= '\uFAFF')) ) {
                            alt24=1;
                        }


                        switch (alt24) {
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
                    	    break loop24;
                        }
                    } while (true);


                    mCOLON(); 


                    // org/alfresco/rest/antlr/WhereClause.g:92:101: ( IDENTIFIERLETTERORDIGIT )*
                    loop25:
                    do {
                        int alt25=2;
                        int LA25_0 = input.LA(1);

                        if ( ((LA25_0 >= '0' && LA25_0 <= '9')||(LA25_0 >= 'A' && LA25_0 <= 'Z')||LA25_0=='_'||(LA25_0 >= 'a' && LA25_0 <= 'z')||(LA25_0 >= '\u00C0' && LA25_0 <= '\u00D6')||(LA25_0 >= '\u00D8' && LA25_0 <= '\u00F6')||(LA25_0 >= '\u00F8' && LA25_0 <= '\u1FFF')||(LA25_0 >= '\u3040' && LA25_0 <= '\u318F')||(LA25_0 >= '\u3300' && LA25_0 <= '\u337F')||(LA25_0 >= '\u3400' && LA25_0 <= '\u3D2D')||(LA25_0 >= '\u4E00' && LA25_0 <= '\u9FFF')||(LA25_0 >= '\uF900' && LA25_0 <= '\uFAFF')) ) {
                            alt25=1;
                        }


                        switch (alt25) {
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
                    	    break loop25;
                        }
                    } while (true);


                    }


                    }
                    break;

            }


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
            // org/alfresco/rest/antlr/WhereClause.g:94:4: ( ( ' ' | '\\t' | '\\r' | '\\n' )+ )
            // org/alfresco/rest/antlr/WhereClause.g:94:6: ( ' ' | '\\t' | '\\r' | '\\n' )+
            {
            // org/alfresco/rest/antlr/WhereClause.g:94:6: ( ' ' | '\\t' | '\\r' | '\\n' )+
            int cnt27=0;
            loop27:
            do {
                int alt27=2;
                int LA27_0 = input.LA(1);

                if ( ((LA27_0 >= '\t' && LA27_0 <= '\n')||LA27_0=='\r'||LA27_0==' ') ) {
                    alt27=1;
                }


                switch (alt27) {
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
            	    if ( cnt27 >= 1 ) break loop27;
                        EarlyExitException eee =
                            new EarlyExitException(27, input);
                        throw eee;
                }
                cnt27++;
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
            // org/alfresco/rest/antlr/WhereClause.g:96:5: ( '\\u0041' .. '\\u005a' | '\\u005f' | '\\u0061' .. '\\u007a' | '\\u00c0' .. '\\u00d6' | '\\u00d8' .. '\\u00f6' | '\\u00f8' .. '\\u00ff' | '\\u0100' .. '\\u1fff' | '\\u3040' .. '\\u318f' | '\\u3300' .. '\\u337f' | '\\u3400' .. '\\u3d2d' | '\\u4e00' .. '\\u9fff' | '\\uf900' .. '\\ufaff' )
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
            // org/alfresco/rest/antlr/WhereClause.g:110:5: ( '\\u0030' .. '\\u0039' | '\\u0660' .. '\\u0669' | '\\u06f0' .. '\\u06f9' | '\\u0966' .. '\\u096f' | '\\u09e6' .. '\\u09ef' | '\\u0a66' .. '\\u0a6f' | '\\u0ae6' .. '\\u0aef' | '\\u0b66' .. '\\u0b6f' | '\\u0be7' .. '\\u0bef' | '\\u0c66' .. '\\u0c6f' | '\\u0ce6' .. '\\u0cef' | '\\u0d66' .. '\\u0d6f' | '\\u0e50' .. '\\u0e59' | '\\u0ed0' .. '\\u0ed9' | '\\u1040' .. '\\u1049' )
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
        // org/alfresco/rest/antlr/WhereClause.g:1:8: ( NEGATION | EXISTS | IN | MATCHES | BETWEEN | OR | AND | EQUALS | LESSTHAN | GREATERTHAN | LESSTHANOREQUALS | GREATERTHANOREQUALS | LEFTPAREN | RIGHTPAREN | COMMA | COLON | SINGLEQUOTE | PROPERTYVALUE | PROPERTYNAME | WS )
        int alt28=20;
        alt28 = dfa28.predict(input);
        switch (alt28) {
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
                // org/alfresco/rest/antlr/WhereClause.g:1:144: COLON
                {
                mCOLON(); 


                }
                break;
            case 17 :
                // org/alfresco/rest/antlr/WhereClause.g:1:150: SINGLEQUOTE
                {
                mSINGLEQUOTE(); 


                }
                break;
            case 18 :
                // org/alfresco/rest/antlr/WhereClause.g:1:162: PROPERTYVALUE
                {
                mPROPERTYVALUE(); 


                }
                break;
            case 19 :
                // org/alfresco/rest/antlr/WhereClause.g:1:176: PROPERTYNAME
                {
                mPROPERTYNAME(); 


                }
                break;
            case 20 :
                // org/alfresco/rest/antlr/WhereClause.g:1:189: WS
                {
                mWS(); 


                }
                break;

        }

    }


    protected DFA26 dfa26 = new DFA26(this);
    protected DFA28 dfa28 = new DFA28(this);
    static final String DFA26_eotS =
        "\2\2\2\uffff";
    static final String DFA26_eofS =
        "\4\uffff";
    static final String DFA26_minS =
        "\2\60\2\uffff";
    static final String DFA26_maxS =
        "\2\ufaff\2\uffff";
    static final String DFA26_acceptS =
        "\2\uffff\1\1\1\2";
    static final String DFA26_specialS =
        "\4\uffff}>";
    static final String[] DFA26_transitionS = {
            "\12\1\1\3\6\uffff\32\1\4\uffff\1\1\1\uffff\32\1\105\uffff\27"+
            "\1\1\uffff\37\1\1\uffff\u1f08\1\u1040\uffff\u0150\1\u0170\uffff"+
            "\u0080\1\u0080\uffff\u092e\1\u10d2\uffff\u5200\1\u5900\uffff"+
            "\u0200\1",
            "\12\1\1\3\6\uffff\32\1\4\uffff\1\1\1\uffff\32\1\105\uffff\27"+
            "\1\1\uffff\37\1\1\uffff\u1f08\1\u1040\uffff\u0150\1\u0170\uffff"+
            "\u0080\1\u0080\uffff\u092e\1\u10d2\uffff\u5200\1\u5900\uffff"+
            "\u0200\1",
            "",
            ""
    };

    static final short[] DFA26_eot = DFA.unpackEncodedString(DFA26_eotS);
    static final short[] DFA26_eof = DFA.unpackEncodedString(DFA26_eofS);
    static final char[] DFA26_min = DFA.unpackEncodedStringToUnsignedChars(DFA26_minS);
    static final char[] DFA26_max = DFA.unpackEncodedStringToUnsignedChars(DFA26_maxS);
    static final short[] DFA26_accept = DFA.unpackEncodedString(DFA26_acceptS);
    static final short[] DFA26_special = DFA.unpackEncodedString(DFA26_specialS);
    static final short[][] DFA26_transition;

    static {
        int numStates = DFA26_transitionS.length;
        DFA26_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA26_transition[i] = DFA.unpackEncodedString(DFA26_transitionS[i]);
        }
    }

    class DFA26 extends DFA {

        public DFA26(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 26;
            this.eot = DFA26_eot;
            this.eof = DFA26_eof;
            this.min = DFA26_min;
            this.max = DFA26_max;
            this.accept = DFA26_accept;
            this.special = DFA26_special;
            this.transition = DFA26_transition;
        }
        public String getDescription() {
            return "92:41: ( ( IDENTIFIERLETTERORDIGIT )* | ( ( IDENTIFIERLETTERORDIGIT )* COLON ( IDENTIFIERLETTERORDIGIT )* ) )";
        }
    }
    static final String DFA28_eotS =
        "\1\uffff\4\17\1\25\1\uffff\1\34\1\36\4\uffff\1\37\1\20\2\uffff\4"+
        "\17\13\uffff\1\20\4\17\1\uffff\4\17\2\54\1\uffff";
    static final String DFA28_eofS =
        "\55\uffff";
    static final String DFA28_minS =
        "\1\11\1\157\1\117\1\170\1\130\1\11\1\uffff\2\75\4\uffff\1\0\1\57"+
        "\2\uffff\1\164\1\124\1\151\1\111\13\uffff\1\57\2\11\1\163\1\123"+
        "\1\uffff\1\164\1\124\1\163\1\123\2\57\1\uffff";
    static final String DFA28_maxS =
        "\1\ufaff\1\157\1\117\1\170\1\130\1\157\1\uffff\2\75\4\uffff\1\uffff"+
        "\1\ufaff\2\uffff\1\164\1\124\1\151\1\111\13\uffff\1\ufaff\2\40\1"+
        "\163\1\123\1\uffff\1\164\1\124\1\163\1\123\2\ufaff\1\uffff";
    static final String DFA28_acceptS =
        "\6\uffff\1\10\2\uffff\1\15\1\16\1\17\1\20\2\uffff\1\23\1\22\4\uffff"+
        "\1\24\1\3\1\4\1\5\1\6\1\7\1\13\1\11\1\14\1\12\1\21\5\uffff\1\1\6"+
        "\uffff\1\2";
    static final String DFA28_specialS =
        "\15\uffff\1\0\37\uffff}>";
    static final String[] DFA28_transitionS = {
            "\2\5\2\uffff\1\5\22\uffff\1\5\6\uffff\1\15\1\11\1\12\2\uffff"+
            "\1\13\2\uffff\1\17\12\20\1\14\1\uffff\1\7\1\6\1\10\2\uffff\4"+
            "\17\1\4\10\17\1\2\14\17\4\uffff\1\17\1\uffff\4\17\1\3\10\17"+
            "\1\1\14\17\105\uffff\27\17\1\uffff\37\17\1\uffff\u0568\17\12"+
            "\16\u0086\17\12\16\u026c\17\12\16\166\17\12\16\166\17\12\16"+
            "\166\17\12\16\166\17\12\16\167\17\11\16\166\17\12\16\166\17"+
            "\12\16\166\17\12\16\u00e0\17\12\16\166\17\12\16\u0166\17\12"+
            "\16\u0fb6\17\u1040\uffff\u0150\17\u0170\uffff\u0080\17\u0080"+
            "\uffff\u092e\17\u10d2\uffff\u5200\17\u5900\uffff\u0200\17",
            "\1\21",
            "\1\22",
            "\1\23",
            "\1\24",
            "\2\5\2\uffff\1\5\22\uffff\1\5\33\uffff\1\7\1\6\1\10\2\uffff"+
            "\1\32\1\30\6\uffff\1\26\3\uffff\1\27\1\uffff\1\31\21\uffff\1"+
            "\32\1\30\6\uffff\1\26\3\uffff\1\27\1\uffff\1\31",
            "",
            "\1\33",
            "\1\35",
            "",
            "",
            "",
            "",
            "\0\20",
            "\1\17\12\40\1\17\6\uffff\32\17\4\uffff\1\17\1\uffff\32\17\105"+
            "\uffff\27\17\1\uffff\37\17\1\uffff\u0568\17\12\40\u0086\17\12"+
            "\40\u026c\17\12\40\166\17\12\40\166\17\12\40\166\17\12\40\166"+
            "\17\12\40\167\17\11\40\166\17\12\40\166\17\12\40\166\17\12\40"+
            "\u00e0\17\12\40\166\17\12\40\u0166\17\12\40\u0fb6\17\u1040\uffff"+
            "\u0150\17\u0170\uffff\u0080\17\u0080\uffff\u092e\17\u10d2\uffff"+
            "\u5200\17\u5900\uffff\u0200\17",
            "",
            "",
            "\1\41",
            "\1\42",
            "\1\43",
            "\1\44",
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
            "\1\17\12\40\1\17\6\uffff\32\17\4\uffff\1\17\1\uffff\32\17\105"+
            "\uffff\27\17\1\uffff\37\17\1\uffff\u0568\17\12\40\u0086\17\12"+
            "\40\u026c\17\12\40\166\17\12\40\166\17\12\40\166\17\12\40\166"+
            "\17\12\40\167\17\11\40\166\17\12\40\166\17\12\40\166\17\12\40"+
            "\u00e0\17\12\40\166\17\12\40\u0166\17\12\40\u0fb6\17\u1040\uffff"+
            "\u0150\17\u0170\uffff\u0080\17\u0080\uffff\u092e\17\u10d2\uffff"+
            "\u5200\17\u5900\uffff\u0200\17",
            "\2\45\2\uffff\1\45\22\uffff\1\45",
            "\2\45\2\uffff\1\45\22\uffff\1\45",
            "\1\46",
            "\1\47",
            "",
            "\1\50",
            "\1\51",
            "\1\52",
            "\1\53",
            "\14\17\6\uffff\32\17\4\uffff\1\17\1\uffff\32\17\105\uffff\27"+
            "\17\1\uffff\37\17\1\uffff\u1f08\17\u1040\uffff\u0150\17\u0170"+
            "\uffff\u0080\17\u0080\uffff\u092e\17\u10d2\uffff\u5200\17\u5900"+
            "\uffff\u0200\17",
            "\14\17\6\uffff\32\17\4\uffff\1\17\1\uffff\32\17\105\uffff\27"+
            "\17\1\uffff\37\17\1\uffff\u1f08\17\u1040\uffff\u0150\17\u0170"+
            "\uffff\u0080\17\u0080\uffff\u092e\17\u10d2\uffff\u5200\17\u5900"+
            "\uffff\u0200\17",
            ""
    };

    static final short[] DFA28_eot = DFA.unpackEncodedString(DFA28_eotS);
    static final short[] DFA28_eof = DFA.unpackEncodedString(DFA28_eofS);
    static final char[] DFA28_min = DFA.unpackEncodedStringToUnsignedChars(DFA28_minS);
    static final char[] DFA28_max = DFA.unpackEncodedStringToUnsignedChars(DFA28_maxS);
    static final short[] DFA28_accept = DFA.unpackEncodedString(DFA28_acceptS);
    static final short[] DFA28_special = DFA.unpackEncodedString(DFA28_specialS);
    static final short[][] DFA28_transition;

    static {
        int numStates = DFA28_transitionS.length;
        DFA28_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA28_transition[i] = DFA.unpackEncodedString(DFA28_transitionS[i]);
        }
    }

    class DFA28 extends DFA {

        public DFA28(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 28;
            this.eot = DFA28_eot;
            this.eof = DFA28_eof;
            this.min = DFA28_min;
            this.max = DFA28_max;
            this.accept = DFA28_accept;
            this.special = DFA28_special;
            this.transition = DFA28_transition;
        }
        public String getDescription() {
            return "1:1: Tokens : ( NEGATION | EXISTS | IN | MATCHES | BETWEEN | OR | AND | EQUALS | LESSTHAN | GREATERTHAN | LESSTHANOREQUALS | GREATERTHANOREQUALS | LEFTPAREN | RIGHTPAREN | COMMA | COLON | SINGLEQUOTE | PROPERTYVALUE | PROPERTYNAME | WS );";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            IntStream input = _input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA28_13 = input.LA(1);

                        s = -1;
                        if ( ((LA28_13 >= '\u0000' && LA28_13 <= '\uFFFF')) ) {s = 16;}

                        else s = 31;

                        if ( s>=0 ) return s;
                        break;
            }
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 28, _s, input);
            error(nvae);
            throw nvae;
        }

    }
 

}