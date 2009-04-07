// $ANTLR 3.1.2 W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g 2009-04-06 14:56:17
package org.alfresco.repo.search.impl.parsers;

import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

public class FTSLexer extends Lexer {
    public static final int DOLLAR=61;
    public static final int TERM=8;
    public static final int STAR=42;
    public static final int LSQUARE=57;
    public static final int AMP=53;
    public static final int FG_PROXIMITY=28;
    public static final int FG_TERM=24;
    public static final int EXACT_TERM=9;
    public static final int FIELD_DISJUNCTION=16;
    public static final int DOTDOT=43;
    public static final int EQUALS=39;
    public static final int FG_EXACT_TERM=25;
    public static final int MANDATORY=13;
    public static final int NOT=52;
    public static final int EXCLUSIVE=32;
    public static final int FIELD_EXCLUDE=23;
    public static final int AND=51;
    public static final int ID=48;
    public static final int EOF=-1;
    public static final int LPAREN=36;
    public static final int RPAREN=37;
    public static final int TILDA=41;
    public static final int EXCLAMATION=54;
    public static final int COMMA=59;
    public static final int FIELD_DEFAULT=20;
    public static final int QUESTION_MARK=56;
    public static final int CARAT=60;
    public static final int PLUS=33;
    public static final int FIELD_OPTIONAL=22;
    public static final int DOT=47;
    public static final int COLUMN_REF=30;
    public static final int F_ESC=55;
    public static final int SYNONYM=11;
    public static final int EXCLUDE=15;
    public static final int TO=44;
    public static final int CONJUNCTION=6;
    public static final int FIELD_GROUP=19;
    public static final int DEFAULT=12;
    public static final int INWORD=62;
    public static final int RSQUARE=58;
    public static final int MINUS=35;
    public static final int PHRASE=10;
    public static final int FTSWORD=49;
    public static final int OPTIONAL=14;
    public static final int COLON=38;
    public static final int DISJUNCTION=5;
    public static final int FTS=4;
    public static final int LCURL=45;
    public static final int FG_SYNONYM=27;
    public static final int WS=64;
    public static final int NEGATION=7;
    public static final int FTSPHRASE=40;
    public static final int FIELD_CONJUNCTION=17;
    public static final int INCLUSIVE=31;
    public static final int RCURL=46;
    public static final int OR=50;
    public static final int FIELD_MANDATORY=21;
    public static final int F_HEX=63;
    public static final int FG_RANGE=29;
    public static final int BAR=34;
    public static final int FG_PHRASE=26;
    public static final int FIELD_NEGATION=18;

    // delegates
    // delegators

    public FTSLexer() {;} 
    public FTSLexer(CharStream input) {
        this(input, new RecognizerSharedState());
    }
    public FTSLexer(CharStream input, RecognizerSharedState state) {
        super(input,state);

    }
    public String getGrammarFileName() { return "W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g"; }

    // $ANTLR start "FTSPHRASE"
    public final void mFTSPHRASE() throws RecognitionException {
        try {
            int _type = FTSPHRASE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:359:3: ( '\"' ( F_ESC | ~ ( '\\\\' | '\"' ) )* '\"' )
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:359:5: '\"' ( F_ESC | ~ ( '\\\\' | '\"' ) )* '\"'
            {
            match('\"'); 
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:359:9: ( F_ESC | ~ ( '\\\\' | '\"' ) )*
            loop1:
            do {
                int alt1=3;
                int LA1_0 = input.LA(1);

                if ( (LA1_0=='\\') ) {
                    alt1=1;
                }
                else if ( ((LA1_0>='\u0000' && LA1_0<='!')||(LA1_0>='#' && LA1_0<='[')||(LA1_0>=']' && LA1_0<='\uFFFF')) ) {
                    alt1=2;
                }


                switch (alt1) {
            	case 1 :
            	    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:359:10: F_ESC
            	    {
            	    mF_ESC(); 

            	    }
            	    break;
            	case 2 :
            	    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:359:18: ~ ( '\\\\' | '\"' )
            	    {
            	    if ( (input.LA(1)>='\u0000' && input.LA(1)<='!')||(input.LA(1)>='#' && input.LA(1)<='[')||(input.LA(1)>=']' && input.LA(1)<='\uFFFF') ) {
            	        input.consume();

            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;}


            	    }
            	    break;

            	default :
            	    break loop1;
                }
            } while (true);

            match('\"'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "FTSPHRASE"

    // $ANTLR start "OR"
    public final void mOR() throws RecognitionException {
        try {
            int _type = OR;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:362:4: ( ( 'O' | 'o' ) ( 'R' | 'r' ) )
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:362:6: ( 'O' | 'o' ) ( 'R' | 'r' )
            {
            if ( input.LA(1)=='O'||input.LA(1)=='o' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='R'||input.LA(1)=='r' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "OR"

    // $ANTLR start "AND"
    public final void mAND() throws RecognitionException {
        try {
            int _type = AND;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:363:5: ( ( 'A' | 'a' ) ( 'N' | 'n' ) ( 'D' | 'd' ) )
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:363:7: ( 'A' | 'a' ) ( 'N' | 'n' ) ( 'D' | 'd' )
            {
            if ( input.LA(1)=='A'||input.LA(1)=='a' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='N'||input.LA(1)=='n' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='D'||input.LA(1)=='d' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "AND"

    // $ANTLR start "NOT"
    public final void mNOT() throws RecognitionException {
        try {
            int _type = NOT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:364:5: ( ( 'N' | 'n' ) ( 'O' | 'o' ) ( 'T' | 't' ) )
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:364:7: ( 'N' | 'n' ) ( 'O' | 'o' ) ( 'T' | 't' )
            {
            if ( input.LA(1)=='N'||input.LA(1)=='n' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='O'||input.LA(1)=='o' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='T'||input.LA(1)=='t' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "NOT"

    // $ANTLR start "TILDA"
    public final void mTILDA() throws RecognitionException {
        try {
            int _type = TILDA;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:365:7: ( '~' )
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:365:9: '~'
            {
            match('~'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "TILDA"

    // $ANTLR start "LPAREN"
    public final void mLPAREN() throws RecognitionException {
        try {
            int _type = LPAREN;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:366:8: ( '(' )
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:366:10: '('
            {
            match('('); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "LPAREN"

    // $ANTLR start "RPAREN"
    public final void mRPAREN() throws RecognitionException {
        try {
            int _type = RPAREN;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:367:8: ( ')' )
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:367:10: ')'
            {
            match(')'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "RPAREN"

    // $ANTLR start "PLUS"
    public final void mPLUS() throws RecognitionException {
        try {
            int _type = PLUS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:368:6: ( '+' )
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:368:8: '+'
            {
            match('+'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "PLUS"

    // $ANTLR start "MINUS"
    public final void mMINUS() throws RecognitionException {
        try {
            int _type = MINUS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:369:7: ( '-' )
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:369:9: '-'
            {
            match('-'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "MINUS"

    // $ANTLR start "COLON"
    public final void mCOLON() throws RecognitionException {
        try {
            int _type = COLON;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:370:7: ( ':' )
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:370:9: ':'
            {
            match(':'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "COLON"

    // $ANTLR start "STAR"
    public final void mSTAR() throws RecognitionException {
        try {
            int _type = STAR;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:371:6: ( '*' )
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:371:8: '*'
            {
            match('*'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "STAR"

    // $ANTLR start "DOTDOT"
    public final void mDOTDOT() throws RecognitionException {
        try {
            int _type = DOTDOT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:372:8: ( '..' )
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:372:10: '..'
            {
            match(".."); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "DOTDOT"

    // $ANTLR start "DOT"
    public final void mDOT() throws RecognitionException {
        try {
            int _type = DOT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:373:5: ( '.' )
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:373:7: '.'
            {
            match('.'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "DOT"

    // $ANTLR start "AMP"
    public final void mAMP() throws RecognitionException {
        try {
            int _type = AMP;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:374:5: ( '&' )
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:374:7: '&'
            {
            match('&'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "AMP"

    // $ANTLR start "EXCLAMATION"
    public final void mEXCLAMATION() throws RecognitionException {
        try {
            int _type = EXCLAMATION;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:375:13: ( '!' )
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:375:15: '!'
            {
            match('!'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "EXCLAMATION"

    // $ANTLR start "BAR"
    public final void mBAR() throws RecognitionException {
        try {
            int _type = BAR;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:376:5: ( '|' )
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:376:7: '|'
            {
            match('|'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "BAR"

    // $ANTLR start "EQUALS"
    public final void mEQUALS() throws RecognitionException {
        try {
            int _type = EQUALS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:377:8: ( '=' )
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:377:10: '='
            {
            match('='); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "EQUALS"

    // $ANTLR start "QUESTION_MARK"
    public final void mQUESTION_MARK() throws RecognitionException {
        try {
            int _type = QUESTION_MARK;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:378:15: ( '?' )
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:378:17: '?'
            {
            match('?'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "QUESTION_MARK"

    // $ANTLR start "LCURL"
    public final void mLCURL() throws RecognitionException {
        try {
            int _type = LCURL;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:379:7: ( '{' )
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:379:9: '{'
            {
            match('{'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "LCURL"

    // $ANTLR start "RCURL"
    public final void mRCURL() throws RecognitionException {
        try {
            int _type = RCURL;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:380:7: ( '}' )
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:380:9: '}'
            {
            match('}'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "RCURL"

    // $ANTLR start "LSQUARE"
    public final void mLSQUARE() throws RecognitionException {
        try {
            int _type = LSQUARE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:381:9: ( '[' )
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:381:11: '['
            {
            match('['); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "LSQUARE"

    // $ANTLR start "RSQUARE"
    public final void mRSQUARE() throws RecognitionException {
        try {
            int _type = RSQUARE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:382:9: ( ']' )
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:382:11: ']'
            {
            match(']'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "RSQUARE"

    // $ANTLR start "TO"
    public final void mTO() throws RecognitionException {
        try {
            int _type = TO;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:383:4: ( ( 'T' | 't' ) ( 'O' | 'o' ) )
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:383:6: ( 'T' | 't' ) ( 'O' | 'o' )
            {
            if ( input.LA(1)=='T'||input.LA(1)=='t' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='O'||input.LA(1)=='o' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "TO"

    // $ANTLR start "COMMA"
    public final void mCOMMA() throws RecognitionException {
        try {
            int _type = COMMA;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:384:7: ( ',' )
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:384:9: ','
            {
            match(','); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "COMMA"

    // $ANTLR start "CARAT"
    public final void mCARAT() throws RecognitionException {
        try {
            int _type = CARAT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:385:7: ( '^' )
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:385:9: '^'
            {
            match('^'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "CARAT"

    // $ANTLR start "DOLLAR"
    public final void mDOLLAR() throws RecognitionException {
        try {
            int _type = DOLLAR;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:386:8: ( '$' )
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:386:11: '$'
            {
            match('$'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "DOLLAR"

    // $ANTLR start "ID"
    public final void mID() throws RecognitionException {
        try {
            int _type = ID;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:391:5: ( ( 'a' .. 'z' | 'A' .. 'Z' | '_' ) ( 'a' .. 'z' | 'A' .. 'Z' | '0' .. '9' | '_' | '$' | '#' )* )
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:391:9: ( 'a' .. 'z' | 'A' .. 'Z' | '_' ) ( 'a' .. 'z' | 'A' .. 'Z' | '0' .. '9' | '_' | '$' | '#' )*
            {
            if ( (input.LA(1)>='A' && input.LA(1)<='Z')||input.LA(1)=='_'||(input.LA(1)>='a' && input.LA(1)<='z') ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:391:32: ( 'a' .. 'z' | 'A' .. 'Z' | '0' .. '9' | '_' | '$' | '#' )*
            loop2:
            do {
                int alt2=2;
                int LA2_0 = input.LA(1);

                if ( ((LA2_0>='#' && LA2_0<='$')||(LA2_0>='0' && LA2_0<='9')||(LA2_0>='A' && LA2_0<='Z')||LA2_0=='_'||(LA2_0>='a' && LA2_0<='z')) ) {
                    alt2=1;
                }


                switch (alt2) {
            	case 1 :
            	    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:
            	    {
            	    if ( (input.LA(1)>='#' && input.LA(1)<='$')||(input.LA(1)>='0' && input.LA(1)<='9')||(input.LA(1)>='A' && input.LA(1)<='Z')||input.LA(1)=='_'||(input.LA(1)>='a' && input.LA(1)<='z') ) {
            	        input.consume();

            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;}


            	    }
            	    break;

            	default :
            	    break loop2;
                }
            } while (true);


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "ID"

    // $ANTLR start "FTSWORD"
    public final void mFTSWORD() throws RecognitionException {
        try {
            int _type = FTSWORD;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:394:9: ( ( F_ESC | INWORD )+ )
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:394:12: ( F_ESC | INWORD )+
            {
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:394:12: ( F_ESC | INWORD )+
            int cnt3=0;
            loop3:
            do {
                int alt3=3;
                int LA3_0 = input.LA(1);

                if ( (LA3_0=='\\') ) {
                    alt3=1;
                }
                else if ( ((LA3_0>='0' && LA3_0<='9')||(LA3_0>='A' && LA3_0<='Z')||(LA3_0>='a' && LA3_0<='z')||(LA3_0>='\u00C0' && LA3_0<='\u00D6')||(LA3_0>='\u00D8' && LA3_0<='\u00F6')||(LA3_0>='\u00F8' && LA3_0<='\u1FFF')||(LA3_0>='\u3040' && LA3_0<='\u318F')||(LA3_0>='\u3300' && LA3_0<='\u337F')||(LA3_0>='\u3400' && LA3_0<='\u3D2D')||(LA3_0>='\u4E00' && LA3_0<='\u9FFF')||(LA3_0>='\uAC00' && LA3_0<='\uD7AF')||(LA3_0>='\uF900' && LA3_0<='\uFAFF')) ) {
                    alt3=2;
                }


                switch (alt3) {
            	case 1 :
            	    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:394:13: F_ESC
            	    {
            	    mF_ESC(); 

            	    }
            	    break;
            	case 2 :
            	    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:394:21: INWORD
            	    {
            	    mINWORD(); 

            	    }
            	    break;

            	default :
            	    if ( cnt3 >= 1 ) break loop3;
                        EarlyExitException eee =
                            new EarlyExitException(3, input);
                        throw eee;
                }
                cnt3++;
            } while (true);


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "FTSWORD"

    // $ANTLR start "F_ESC"
    public final void mF_ESC() throws RecognitionException {
        try {
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:397:9: ( '\\\\' ( 'u' F_HEX F_HEX F_HEX F_HEX | . ) )
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:397:11: '\\\\' ( 'u' F_HEX F_HEX F_HEX F_HEX | . )
            {
            match('\\'); 
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:398:5: ( 'u' F_HEX F_HEX F_HEX F_HEX | . )
            int alt4=2;
            int LA4_0 = input.LA(1);

            if ( (LA4_0=='u') ) {
                int LA4_1 = input.LA(2);

                if ( ((LA4_1>='0' && LA4_1<='9')||(LA4_1>='A' && LA4_1<='F')||(LA4_1>='a' && LA4_1<='f')) ) {
                    alt4=1;
                }
                else {
                    alt4=2;}
            }
            else if ( ((LA4_0>='\u0000' && LA4_0<='t')||(LA4_0>='v' && LA4_0<='\uFFFF')) ) {
                alt4=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 4, 0, input);

                throw nvae;
            }
            switch (alt4) {
                case 1 :
                    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:398:7: 'u' F_HEX F_HEX F_HEX F_HEX
                    {
                    match('u'); 
                    mF_HEX(); 
                    mF_HEX(); 
                    mF_HEX(); 
                    mF_HEX(); 

                    }
                    break;
                case 2 :
                    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:399:7: .
                    {
                    matchAny(); 

                    }
                    break;

            }


            }

        }
        finally {
        }
    }
    // $ANTLR end "F_ESC"

    // $ANTLR start "F_HEX"
    public final void mF_HEX() throws RecognitionException {
        try {
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:404:7: ( '0' .. '9' | 'a' .. 'f' | 'A' .. 'F' )
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:
            {
            if ( (input.LA(1)>='0' && input.LA(1)<='9')||(input.LA(1)>='A' && input.LA(1)<='F')||(input.LA(1)>='a' && input.LA(1)<='f') ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "F_HEX"

    // $ANTLR start "INWORD"
    public final void mINWORD() throws RecognitionException {
        try {
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:411:8: ( '\\u0041' .. '\\u005A' | '\\u0061' .. '\\u007A' | '\\u00C0' .. '\\u00D6' | '\\u00D8' .. '\\u00F6' | '\\u00F8' .. '\\u00FF' | '\\u0100' .. '\\u1FFF' | '\\u3040' .. '\\u318F' | '\\u3300' .. '\\u337F' | '\\u3400' .. '\\u3D2D' | '\\u4E00' .. '\\u9FFF' | '\\uF900' .. '\\uFAFF' | '\\uAC00' .. '\\uD7AF' | '\\u0030' .. '\\u0039' | '\\u0660' .. '\\u0669' | '\\u06F0' .. '\\u06F9' | '\\u0966' .. '\\u096F' | '\\u09E6' .. '\\u09EF' | '\\u0A66' .. '\\u0A6F' | '\\u0AE6' .. '\\u0AEF' | '\\u0B66' .. '\\u0B6F' | '\\u0BE7' .. '\\u0BEF' | '\\u0C66' .. '\\u0C6F' | '\\u0CE6' .. '\\u0CEF' | '\\u0D66' .. '\\u0D6F' | '\\u0E50' .. '\\u0E59' | '\\u0ED0' .. '\\u0ED9' | '\\u1040' .. '\\u1049' )
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:
            {
            if ( (input.LA(1)>='0' && input.LA(1)<='9')||(input.LA(1)>='A' && input.LA(1)<='Z')||(input.LA(1)>='a' && input.LA(1)<='z')||(input.LA(1)>='\u00C0' && input.LA(1)<='\u00D6')||(input.LA(1)>='\u00D8' && input.LA(1)<='\u00F6')||(input.LA(1)>='\u00F8' && input.LA(1)<='\u1FFF')||(input.LA(1)>='\u3040' && input.LA(1)<='\u318F')||(input.LA(1)>='\u3300' && input.LA(1)<='\u337F')||(input.LA(1)>='\u3400' && input.LA(1)<='\u3D2D')||(input.LA(1)>='\u4E00' && input.LA(1)<='\u9FFF')||(input.LA(1)>='\uAC00' && input.LA(1)<='\uD7AF')||(input.LA(1)>='\uF900' && input.LA(1)<='\uFAFF') ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "INWORD"

    // $ANTLR start "WS"
    public final void mWS() throws RecognitionException {
        try {
            int _type = WS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:442:4: ( ( ' ' | '\\t' | '\\r' | '\\n' )+ )
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:442:6: ( ' ' | '\\t' | '\\r' | '\\n' )+
            {
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:442:6: ( ' ' | '\\t' | '\\r' | '\\n' )+
            int cnt5=0;
            loop5:
            do {
                int alt5=2;
                int LA5_0 = input.LA(1);

                if ( ((LA5_0>='\t' && LA5_0<='\n')||LA5_0=='\r'||LA5_0==' ') ) {
                    alt5=1;
                }


                switch (alt5) {
            	case 1 :
            	    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:
            	    {
            	    if ( (input.LA(1)>='\t' && input.LA(1)<='\n')||input.LA(1)=='\r'||input.LA(1)==' ' ) {
            	        input.consume();

            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;}


            	    }
            	    break;

            	default :
            	    if ( cnt5 >= 1 ) break loop5;
                        EarlyExitException eee =
                            new EarlyExitException(5, input);
                        throw eee;
                }
                cnt5++;
            } while (true);

             _channel = HIDDEN; 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "WS"

    public void mTokens() throws RecognitionException {
        // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:1:8: ( FTSPHRASE | OR | AND | NOT | TILDA | LPAREN | RPAREN | PLUS | MINUS | COLON | STAR | DOTDOT | DOT | AMP | EXCLAMATION | BAR | EQUALS | QUESTION_MARK | LCURL | RCURL | LSQUARE | RSQUARE | TO | COMMA | CARAT | DOLLAR | ID | FTSWORD | WS )
        int alt6=29;
        alt6 = dfa6.predict(input);
        switch (alt6) {
            case 1 :
                // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:1:10: FTSPHRASE
                {
                mFTSPHRASE(); 

                }
                break;
            case 2 :
                // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:1:20: OR
                {
                mOR(); 

                }
                break;
            case 3 :
                // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:1:23: AND
                {
                mAND(); 

                }
                break;
            case 4 :
                // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:1:27: NOT
                {
                mNOT(); 

                }
                break;
            case 5 :
                // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:1:31: TILDA
                {
                mTILDA(); 

                }
                break;
            case 6 :
                // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:1:37: LPAREN
                {
                mLPAREN(); 

                }
                break;
            case 7 :
                // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:1:44: RPAREN
                {
                mRPAREN(); 

                }
                break;
            case 8 :
                // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:1:51: PLUS
                {
                mPLUS(); 

                }
                break;
            case 9 :
                // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:1:56: MINUS
                {
                mMINUS(); 

                }
                break;
            case 10 :
                // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:1:62: COLON
                {
                mCOLON(); 

                }
                break;
            case 11 :
                // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:1:68: STAR
                {
                mSTAR(); 

                }
                break;
            case 12 :
                // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:1:73: DOTDOT
                {
                mDOTDOT(); 

                }
                break;
            case 13 :
                // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:1:80: DOT
                {
                mDOT(); 

                }
                break;
            case 14 :
                // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:1:84: AMP
                {
                mAMP(); 

                }
                break;
            case 15 :
                // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:1:88: EXCLAMATION
                {
                mEXCLAMATION(); 

                }
                break;
            case 16 :
                // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:1:100: BAR
                {
                mBAR(); 

                }
                break;
            case 17 :
                // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:1:104: EQUALS
                {
                mEQUALS(); 

                }
                break;
            case 18 :
                // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:1:111: QUESTION_MARK
                {
                mQUESTION_MARK(); 

                }
                break;
            case 19 :
                // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:1:125: LCURL
                {
                mLCURL(); 

                }
                break;
            case 20 :
                // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:1:131: RCURL
                {
                mRCURL(); 

                }
                break;
            case 21 :
                // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:1:137: LSQUARE
                {
                mLSQUARE(); 

                }
                break;
            case 22 :
                // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:1:145: RSQUARE
                {
                mRSQUARE(); 

                }
                break;
            case 23 :
                // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:1:153: TO
                {
                mTO(); 

                }
                break;
            case 24 :
                // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:1:156: COMMA
                {
                mCOMMA(); 

                }
                break;
            case 25 :
                // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:1:162: CARAT
                {
                mCARAT(); 

                }
                break;
            case 26 :
                // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:1:168: DOLLAR
                {
                mDOLLAR(); 

                }
                break;
            case 27 :
                // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:1:175: ID
                {
                mID(); 

                }
                break;
            case 28 :
                // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:1:178: FTSWORD
                {
                mFTSWORD(); 

                }
                break;
            case 29 :
                // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:1:186: WS
                {
                mWS(); 

                }
                break;

        }

    }


    protected DFA6 dfa6 = new DFA6(this);
    static final String DFA6_eotS =
        "\2\uffff\3\34\7\uffff\1\43\11\uffff\1\34\3\uffff\1\34\3\uffff\1"+
        "\45\3\34\2\uffff\1\50\1\uffff\1\51\1\52\3\uffff";
    static final String DFA6_eofS =
        "\53\uffff";
    static final String DFA6_minS =
        "\1\11\1\uffff\3\60\7\uffff\1\56\11\uffff\1\60\3\uffff\1\60\3\uffff"+
        "\1\43\3\60\2\uffff\1\43\1\uffff\2\43\3\uffff";
    static final String DFA6_maxS =
        "\1\ufaff\1\uffff\3\ufaff\7\uffff\1\56\11\uffff\1\ufaff\3\uffff"+
        "\1\ufaff\3\uffff\4\ufaff\2\uffff\1\ufaff\1\uffff\2\ufaff\3\uffff";
    static final String DFA6_acceptS =
        "\1\uffff\1\1\3\uffff\1\5\1\6\1\7\1\10\1\11\1\12\1\13\1\uffff\1"+
        "\16\1\17\1\20\1\21\1\22\1\23\1\24\1\25\1\26\1\uffff\1\30\1\31\1"+
        "\32\1\uffff\1\34\1\33\1\35\4\uffff\1\14\1\15\1\uffff\1\2\2\uffff"+
        "\1\27\1\3\1\4";
    static final String DFA6_specialS =
        "\53\uffff}>";
    static final String[] DFA6_transitionS = {
            "\2\35\2\uffff\1\35\22\uffff\1\35\1\16\1\1\1\uffff\1\31\1\uffff"+
            "\1\15\1\uffff\1\6\1\7\1\13\1\10\1\27\1\11\1\14\1\uffff\12\33"+
            "\1\12\2\uffff\1\20\1\uffff\1\21\1\uffff\1\3\14\32\1\4\1\2\4"+
            "\32\1\26\6\32\1\24\1\33\1\25\1\30\1\34\1\uffff\1\3\14\32\1\4"+
            "\1\2\4\32\1\26\6\32\1\22\1\17\1\23\1\5\101\uffff\27\33\1\uffff"+
            "\37\33\1\uffff\u1f08\33\u1040\uffff\u0150\33\u0170\uffff\u0080"+
            "\33\u0080\uffff\u092e\33\u10d2\uffff\u5200\33\u0c00\uffff\u2bb0"+
            "\33\u2150\uffff\u0200\33",
            "",
            "\12\37\7\uffff\21\37\1\36\10\37\1\uffff\1\33\4\uffff\21\37"+
            "\1\36\10\37\105\uffff\27\33\1\uffff\37\33\1\uffff\u1f08\33\u1040"+
            "\uffff\u0150\33\u0170\uffff\u0080\33\u0080\uffff\u092e\33\u10d2"+
            "\uffff\u5200\33\u0c00\uffff\u2bb0\33\u2150\uffff\u0200\33",
            "\12\37\7\uffff\15\37\1\40\14\37\1\uffff\1\33\4\uffff\15\37"+
            "\1\40\14\37\105\uffff\27\33\1\uffff\37\33\1\uffff\u1f08\33\u1040"+
            "\uffff\u0150\33\u0170\uffff\u0080\33\u0080\uffff\u092e\33\u10d2"+
            "\uffff\u5200\33\u0c00\uffff\u2bb0\33\u2150\uffff\u0200\33",
            "\12\37\7\uffff\16\37\1\41\13\37\1\uffff\1\33\4\uffff\16\37"+
            "\1\41\13\37\105\uffff\27\33\1\uffff\37\33\1\uffff\u1f08\33\u1040"+
            "\uffff\u0150\33\u0170\uffff\u0080\33\u0080\uffff\u092e\33\u10d2"+
            "\uffff\u5200\33\u0c00\uffff\u2bb0\33\u2150\uffff\u0200\33",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\42",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\12\37\7\uffff\16\37\1\44\13\37\1\uffff\1\33\4\uffff\16\37"+
            "\1\44\13\37\105\uffff\27\33\1\uffff\37\33\1\uffff\u1f08\33\u1040"+
            "\uffff\u0150\33\u0170\uffff\u0080\33\u0080\uffff\u092e\33\u10d2"+
            "\uffff\u5200\33\u0c00\uffff\u2bb0\33\u2150\uffff\u0200\33",
            "",
            "",
            "",
            "\12\37\7\uffff\32\37\1\uffff\1\33\4\uffff\32\37\105\uffff"+
            "\27\33\1\uffff\37\33\1\uffff\u1f08\33\u1040\uffff\u0150\33\u0170"+
            "\uffff\u0080\33\u0080\uffff\u092e\33\u10d2\uffff\u5200\33\u0c00"+
            "\uffff\u2bb0\33\u2150\uffff\u0200\33",
            "",
            "",
            "",
            "\2\34\13\uffff\12\37\7\uffff\32\37\1\uffff\1\33\2\uffff\1"+
            "\34\1\uffff\32\37\105\uffff\27\33\1\uffff\37\33\1\uffff\u1f08"+
            "\33\u1040\uffff\u0150\33\u0170\uffff\u0080\33\u0080\uffff\u092e"+
            "\33\u10d2\uffff\u5200\33\u0c00\uffff\u2bb0\33\u2150\uffff\u0200"+
            "\33",
            "\12\37\7\uffff\32\37\1\uffff\1\33\4\uffff\32\37\105\uffff"+
            "\27\33\1\uffff\37\33\1\uffff\u1f08\33\u1040\uffff\u0150\33\u0170"+
            "\uffff\u0080\33\u0080\uffff\u092e\33\u10d2\uffff\u5200\33\u0c00"+
            "\uffff\u2bb0\33\u2150\uffff\u0200\33",
            "\12\37\7\uffff\3\37\1\46\26\37\1\uffff\1\33\4\uffff\3\37\1"+
            "\46\26\37\105\uffff\27\33\1\uffff\37\33\1\uffff\u1f08\33\u1040"+
            "\uffff\u0150\33\u0170\uffff\u0080\33\u0080\uffff\u092e\33\u10d2"+
            "\uffff\u5200\33\u0c00\uffff\u2bb0\33\u2150\uffff\u0200\33",
            "\12\37\7\uffff\23\37\1\47\6\37\1\uffff\1\33\4\uffff\23\37"+
            "\1\47\6\37\105\uffff\27\33\1\uffff\37\33\1\uffff\u1f08\33\u1040"+
            "\uffff\u0150\33\u0170\uffff\u0080\33\u0080\uffff\u092e\33\u10d2"+
            "\uffff\u5200\33\u0c00\uffff\u2bb0\33\u2150\uffff\u0200\33",
            "",
            "",
            "\2\34\13\uffff\12\37\7\uffff\32\37\1\uffff\1\33\2\uffff\1"+
            "\34\1\uffff\32\37\105\uffff\27\33\1\uffff\37\33\1\uffff\u1f08"+
            "\33\u1040\uffff\u0150\33\u0170\uffff\u0080\33\u0080\uffff\u092e"+
            "\33\u10d2\uffff\u5200\33\u0c00\uffff\u2bb0\33\u2150\uffff\u0200"+
            "\33",
            "",
            "\2\34\13\uffff\12\37\7\uffff\32\37\1\uffff\1\33\2\uffff\1"+
            "\34\1\uffff\32\37\105\uffff\27\33\1\uffff\37\33\1\uffff\u1f08"+
            "\33\u1040\uffff\u0150\33\u0170\uffff\u0080\33\u0080\uffff\u092e"+
            "\33\u10d2\uffff\u5200\33\u0c00\uffff\u2bb0\33\u2150\uffff\u0200"+
            "\33",
            "\2\34\13\uffff\12\37\7\uffff\32\37\1\uffff\1\33\2\uffff\1"+
            "\34\1\uffff\32\37\105\uffff\27\33\1\uffff\37\33\1\uffff\u1f08"+
            "\33\u1040\uffff\u0150\33\u0170\uffff\u0080\33\u0080\uffff\u092e"+
            "\33\u10d2\uffff\u5200\33\u0c00\uffff\u2bb0\33\u2150\uffff\u0200"+
            "\33",
            "",
            "",
            ""
    };

    static final short[] DFA6_eot = DFA.unpackEncodedString(DFA6_eotS);
    static final short[] DFA6_eof = DFA.unpackEncodedString(DFA6_eofS);
    static final char[] DFA6_min = DFA.unpackEncodedStringToUnsignedChars(DFA6_minS);
    static final char[] DFA6_max = DFA.unpackEncodedStringToUnsignedChars(DFA6_maxS);
    static final short[] DFA6_accept = DFA.unpackEncodedString(DFA6_acceptS);
    static final short[] DFA6_special = DFA.unpackEncodedString(DFA6_specialS);
    static final short[][] DFA6_transition;

    static {
        int numStates = DFA6_transitionS.length;
        DFA6_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA6_transition[i] = DFA.unpackEncodedString(DFA6_transitionS[i]);
        }
    }

    class DFA6 extends DFA {

        public DFA6(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 6;
            this.eot = DFA6_eot;
            this.eof = DFA6_eof;
            this.min = DFA6_min;
            this.max = DFA6_max;
            this.accept = DFA6_accept;
            this.special = DFA6_special;
            this.transition = DFA6_transition;
        }
        public String getDescription() {
            return "1:1: Tokens : ( FTSPHRASE | OR | AND | NOT | TILDA | LPAREN | RPAREN | PLUS | MINUS | COLON | STAR | DOTDOT | DOT | AMP | EXCLAMATION | BAR | EQUALS | QUESTION_MARK | LCURL | RCURL | LSQUARE | RSQUARE | TO | COMMA | CARAT | DOLLAR | ID | FTSWORD | WS );";
        }
    }
 

}