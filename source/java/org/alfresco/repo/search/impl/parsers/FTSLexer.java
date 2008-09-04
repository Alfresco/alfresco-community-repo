// $ANTLR 3.1b1 W:\\workspace-cmis\\ANTLR\\FTS.g 2008-07-15 16:32:02
package org.alfresco.repo.search.impl.parsers;

import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

public class FTSLexer extends Lexer {
    public static final int TERM=8;
    public static final int STAR=32;
    public static final int FG_PROXIMITY=20;
    public static final int CONJUNCTION=6;
    public static final int FG_TERM=16;
    public static final int EXACT_TERM=9;
    public static final int FIELD_GROUP=15;
    public static final int INWORD=38;
    public static final int FIELD_DISJUNCTION=12;
    public static final int DOTDOT=33;
    public static final int NOT=37;
    public static final int FG_EXACT_TERM=17;
    public static final int MINUS=25;
    public static final int ID=35;
    public static final int AND=24;
    public static final int EOF=-1;
    public static final int PHRASE=10;
    public static final int LPAREN=26;
    public static final int FTSWORD=36;
    public static final int COLON=28;
    public static final int DISJUNCTION=5;
    public static final int RPAREN=27;
    public static final int FTS=4;
    public static final int TILDA=31;
    public static final int FG_SYNONYM=19;
    public static final int WS=39;
    public static final int NEGATION=7;
    public static final int FTSPHRASE=30;
    public static final int FIELD_CONJUNCTION=13;
    public static final int OR=23;
    public static final int PLUS=29;
    public static final int DOT=34;
    public static final int COLUMN_REF=22;
    public static final int SYNONYM=11;
    public static final int FG_RANGE=21;
    public static final int FG_PHRASE=18;
    public static final int FIELD_NEGATION=14;

    // delegates
    // delegators

    public FTSLexer() {;} 
    public FTSLexer(CharStream input) {
        this(input, new RecognizerSharedState());
    }
    public FTSLexer(CharStream input, RecognizerSharedState state) {
        super(input,state);

    }
    public String getGrammarFileName() { return "W:\\workspace-cmis\\ANTLR\\FTS.g"; }

    // $ANTLR start OR
    public final void mOR() throws RecognitionException {
        try {
            int _type = OR;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // W:\\workspace-cmis\\ANTLR\\FTS.g:289:4: ( ( 'O' | 'o' ) ( 'R' | 'r' ) )
            // W:\\workspace-cmis\\ANTLR\\FTS.g:289:6: ( 'O' | 'o' ) ( 'R' | 'r' )
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
    // $ANTLR end OR

    // $ANTLR start AND
    public final void mAND() throws RecognitionException {
        try {
            int _type = AND;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // W:\\workspace-cmis\\ANTLR\\FTS.g:290:5: ( ( 'A' | 'a' ) ( 'N' | 'n' ) ( 'D' | 'd' ) )
            // W:\\workspace-cmis\\ANTLR\\FTS.g:290:7: ( 'A' | 'a' ) ( 'N' | 'n' ) ( 'D' | 'd' )
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
    // $ANTLR end AND

    // $ANTLR start NOT
    public final void mNOT() throws RecognitionException {
        try {
            int _type = NOT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // W:\\workspace-cmis\\ANTLR\\FTS.g:291:5: ( ( 'N' | 'n' ) ( 'O' | 'o' ) ( 'T' | 't' ) )
            // W:\\workspace-cmis\\ANTLR\\FTS.g:291:7: ( 'N' | 'n' ) ( 'O' | 'o' ) ( 'T' | 't' )
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
    // $ANTLR end NOT

    // $ANTLR start TILDA
    public final void mTILDA() throws RecognitionException {
        try {
            int _type = TILDA;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // W:\\workspace-cmis\\ANTLR\\FTS.g:292:7: ( '~' )
            // W:\\workspace-cmis\\ANTLR\\FTS.g:292:9: '~'
            {
            match('~'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end TILDA

    // $ANTLR start LPAREN
    public final void mLPAREN() throws RecognitionException {
        try {
            int _type = LPAREN;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // W:\\workspace-cmis\\ANTLR\\FTS.g:293:8: ( '(' )
            // W:\\workspace-cmis\\ANTLR\\FTS.g:293:10: '('
            {
            match('('); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end LPAREN

    // $ANTLR start RPAREN
    public final void mRPAREN() throws RecognitionException {
        try {
            int _type = RPAREN;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // W:\\workspace-cmis\\ANTLR\\FTS.g:294:8: ( ')' )
            // W:\\workspace-cmis\\ANTLR\\FTS.g:294:10: ')'
            {
            match(')'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end RPAREN

    // $ANTLR start PLUS
    public final void mPLUS() throws RecognitionException {
        try {
            int _type = PLUS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // W:\\workspace-cmis\\ANTLR\\FTS.g:295:6: ( '+' )
            // W:\\workspace-cmis\\ANTLR\\FTS.g:295:8: '+'
            {
            match('+'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end PLUS

    // $ANTLR start MINUS
    public final void mMINUS() throws RecognitionException {
        try {
            int _type = MINUS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // W:\\workspace-cmis\\ANTLR\\FTS.g:296:7: ( '-' )
            // W:\\workspace-cmis\\ANTLR\\FTS.g:296:9: '-'
            {
            match('-'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end MINUS

    // $ANTLR start COLON
    public final void mCOLON() throws RecognitionException {
        try {
            int _type = COLON;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // W:\\workspace-cmis\\ANTLR\\FTS.g:297:7: ( ':' )
            // W:\\workspace-cmis\\ANTLR\\FTS.g:297:9: ':'
            {
            match(':'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end COLON

    // $ANTLR start STAR
    public final void mSTAR() throws RecognitionException {
        try {
            int _type = STAR;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // W:\\workspace-cmis\\ANTLR\\FTS.g:298:6: ( '*' )
            // W:\\workspace-cmis\\ANTLR\\FTS.g:298:8: '*'
            {
            match('*'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end STAR

    // $ANTLR start DOTDOT
    public final void mDOTDOT() throws RecognitionException {
        try {
            int _type = DOTDOT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // W:\\workspace-cmis\\ANTLR\\FTS.g:299:8: ( '..' )
            // W:\\workspace-cmis\\ANTLR\\FTS.g:299:10: '..'
            {
            match(".."); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end DOTDOT

    // $ANTLR start DOT
    public final void mDOT() throws RecognitionException {
        try {
            int _type = DOT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // W:\\workspace-cmis\\ANTLR\\FTS.g:300:5: ( '.' )
            // W:\\workspace-cmis\\ANTLR\\FTS.g:300:7: '.'
            {
            match('.'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end DOT

    // $ANTLR start ID
    public final void mID() throws RecognitionException {
        try {
            int _type = ID;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // W:\\workspace-cmis\\ANTLR\\FTS.g:302:5: ( ( 'a' .. 'z' | 'A' .. 'Z' | '_' ) ( 'a' .. 'z' | 'A' .. 'Z' | '0' .. '0' | '_' | '$' | '#' )* )
            // W:\\workspace-cmis\\ANTLR\\FTS.g:302:9: ( 'a' .. 'z' | 'A' .. 'Z' | '_' ) ( 'a' .. 'z' | 'A' .. 'Z' | '0' .. '0' | '_' | '$' | '#' )*
            {
            if ( (input.LA(1)>='A' && input.LA(1)<='Z')||input.LA(1)=='_'||(input.LA(1)>='a' && input.LA(1)<='z') ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            // W:\\workspace-cmis\\ANTLR\\FTS.g:302:32: ( 'a' .. 'z' | 'A' .. 'Z' | '0' .. '0' | '_' | '$' | '#' )*
            loop1:
            do {
                int alt1=2;
                int LA1_0 = input.LA(1);

                if ( ((LA1_0>='#' && LA1_0<='$')||LA1_0=='0'||(LA1_0>='A' && LA1_0<='Z')||LA1_0=='_'||(LA1_0>='a' && LA1_0<='z')) ) {
                    alt1=1;
                }


                switch (alt1) {
            	case 1 :
            	    // W:\\workspace-cmis\\ANTLR\\FTS.g:
            	    {
            	    if ( (input.LA(1)>='#' && input.LA(1)<='$')||input.LA(1)=='0'||(input.LA(1)>='A' && input.LA(1)<='Z')||input.LA(1)=='_'||(input.LA(1)>='a' && input.LA(1)<='z') ) {
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


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end ID

    // $ANTLR start FTSWORD
    public final void mFTSWORD() throws RecognitionException {
        try {
            int _type = FTSWORD;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // W:\\workspace-cmis\\ANTLR\\FTS.g:304:9: ( ( INWORD )+ )
            // W:\\workspace-cmis\\ANTLR\\FTS.g:304:12: ( INWORD )+
            {
            // W:\\workspace-cmis\\ANTLR\\FTS.g:304:12: ( INWORD )+
            int cnt2=0;
            loop2:
            do {
                int alt2=2;
                int LA2_0 = input.LA(1);

                if ( ((LA2_0>='0' && LA2_0<='9')||(LA2_0>='A' && LA2_0<='Z')||(LA2_0>='a' && LA2_0<='z')||(LA2_0>='\u00C0' && LA2_0<='\u00D6')||(LA2_0>='\u00D8' && LA2_0<='\u00F6')||(LA2_0>='\u00F8' && LA2_0<='\u1FFF')||(LA2_0>='\u3040' && LA2_0<='\u318F')||(LA2_0>='\u3300' && LA2_0<='\u337F')||(LA2_0>='\u3400' && LA2_0<='\u3D2D')||(LA2_0>='\u4E00' && LA2_0<='\u9FFF')||(LA2_0>='\uAC00' && LA2_0<='\uD7AF')||(LA2_0>='\uF900' && LA2_0<='\uFAFF')) ) {
                    alt2=1;
                }


                switch (alt2) {
            	case 1 :
            	    // W:\\workspace-cmis\\ANTLR\\FTS.g:304:12: INWORD
            	    {
            	    mINWORD(); 

            	    }
            	    break;

            	default :
            	    if ( cnt2 >= 1 ) break loop2;
                        EarlyExitException eee =
                            new EarlyExitException(2, input);
                        throw eee;
                }
                cnt2++;
            } while (true);


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end FTSWORD

    // $ANTLR start INWORD
    public final void mINWORD() throws RecognitionException {
        try {
            // W:\\workspace-cmis\\ANTLR\\FTS.g:307:8: ( '\\u0041' .. '\\u005A' | '\\u0061' .. '\\u007A' | '\\u00C0' .. '\\u00D6' | '\\u00D8' .. '\\u00F6' | '\\u00F8' .. '\\u00FF' | '\\u0100' .. '\\u1FFF' | '\\u3040' .. '\\u318F' | '\\u3300' .. '\\u337F' | '\\u3400' .. '\\u3D2D' | '\\u4E00' .. '\\u9FFF' | '\\uF900' .. '\\uFAFF' | '\\uAC00' .. '\\uD7AF' | '\\u0030' .. '\\u0039' | '\\u0660' .. '\\u0669' | '\\u06F0' .. '\\u06F9' | '\\u0966' .. '\\u096F' | '\\u09E6' .. '\\u09EF' | '\\u0A66' .. '\\u0A6F' | '\\u0AE6' .. '\\u0AEF' | '\\u0B66' .. '\\u0B6F' | '\\u0BE7' .. '\\u0BEF' | '\\u0C66' .. '\\u0C6F' | '\\u0CE6' .. '\\u0CEF' | '\\u0D66' .. '\\u0D6F' | '\\u0E50' .. '\\u0E59' | '\\u0ED0' .. '\\u0ED9' | '\\u1040' .. '\\u1049' )
            // W:\\workspace-cmis\\ANTLR\\FTS.g:
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
    // $ANTLR end INWORD

    // $ANTLR start FTSPHRASE
    public final void mFTSPHRASE() throws RecognitionException {
        try {
            int _type = FTSPHRASE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // W:\\workspace-cmis\\ANTLR\\FTS.g:337:2: ( '\"' (~ '\"' | '\"\"' )* '\"' )
            // W:\\workspace-cmis\\ANTLR\\FTS.g:337:4: '\"' (~ '\"' | '\"\"' )* '\"'
            {
            match('\"'); 
            // W:\\workspace-cmis\\ANTLR\\FTS.g:337:8: (~ '\"' | '\"\"' )*
            loop3:
            do {
                int alt3=3;
                int LA3_0 = input.LA(1);

                if ( (LA3_0=='\"') ) {
                    int LA3_1 = input.LA(2);

                    if ( (LA3_1=='\"') ) {
                        alt3=2;
                    }


                }
                else if ( ((LA3_0>='\u0000' && LA3_0<='!')||(LA3_0>='#' && LA3_0<='\uFFFE')) ) {
                    alt3=1;
                }


                switch (alt3) {
            	case 1 :
            	    // W:\\workspace-cmis\\ANTLR\\FTS.g:337:9: ~ '\"'
            	    {
            	    if ( (input.LA(1)>='\u0000' && input.LA(1)<='!')||(input.LA(1)>='#' && input.LA(1)<='\uFFFE') ) {
            	        input.consume();

            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;}


            	    }
            	    break;
            	case 2 :
            	    // W:\\workspace-cmis\\ANTLR\\FTS.g:337:16: '\"\"'
            	    {
            	    match("\"\""); 


            	    }
            	    break;

            	default :
            	    break loop3;
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
    // $ANTLR end FTSPHRASE

    // $ANTLR start WS
    public final void mWS() throws RecognitionException {
        try {
            int _type = WS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // W:\\workspace-cmis\\ANTLR\\FTS.g:339:4: ( ( ' ' | '\\t' | '\\r' | '\\n' )+ )
            // W:\\workspace-cmis\\ANTLR\\FTS.g:339:6: ( ' ' | '\\t' | '\\r' | '\\n' )+
            {
            // W:\\workspace-cmis\\ANTLR\\FTS.g:339:6: ( ' ' | '\\t' | '\\r' | '\\n' )+
            int cnt4=0;
            loop4:
            do {
                int alt4=2;
                int LA4_0 = input.LA(1);

                if ( ((LA4_0>='\t' && LA4_0<='\n')||LA4_0=='\r'||LA4_0==' ') ) {
                    alt4=1;
                }


                switch (alt4) {
            	case 1 :
            	    // W:\\workspace-cmis\\ANTLR\\FTS.g:
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
            	    if ( cnt4 >= 1 ) break loop4;
                        EarlyExitException eee =
                            new EarlyExitException(4, input);
                        throw eee;
                }
                cnt4++;
            } while (true);

             _channel = HIDDEN; 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end WS

    public void mTokens() throws RecognitionException {
        // W:\\workspace-cmis\\ANTLR\\FTS.g:1:8: ( OR | AND | NOT | TILDA | LPAREN | RPAREN | PLUS | MINUS | COLON | STAR | DOTDOT | DOT | ID | FTSWORD | FTSPHRASE | WS )
        int alt5=16;
        alt5 = dfa5.predict(input);
        switch (alt5) {
            case 1 :
                // W:\\workspace-cmis\\ANTLR\\FTS.g:1:10: OR
                {
                mOR(); 

                }
                break;
            case 2 :
                // W:\\workspace-cmis\\ANTLR\\FTS.g:1:13: AND
                {
                mAND(); 

                }
                break;
            case 3 :
                // W:\\workspace-cmis\\ANTLR\\FTS.g:1:17: NOT
                {
                mNOT(); 

                }
                break;
            case 4 :
                // W:\\workspace-cmis\\ANTLR\\FTS.g:1:21: TILDA
                {
                mTILDA(); 

                }
                break;
            case 5 :
                // W:\\workspace-cmis\\ANTLR\\FTS.g:1:27: LPAREN
                {
                mLPAREN(); 

                }
                break;
            case 6 :
                // W:\\workspace-cmis\\ANTLR\\FTS.g:1:34: RPAREN
                {
                mRPAREN(); 

                }
                break;
            case 7 :
                // W:\\workspace-cmis\\ANTLR\\FTS.g:1:41: PLUS
                {
                mPLUS(); 

                }
                break;
            case 8 :
                // W:\\workspace-cmis\\ANTLR\\FTS.g:1:46: MINUS
                {
                mMINUS(); 

                }
                break;
            case 9 :
                // W:\\workspace-cmis\\ANTLR\\FTS.g:1:52: COLON
                {
                mCOLON(); 

                }
                break;
            case 10 :
                // W:\\workspace-cmis\\ANTLR\\FTS.g:1:58: STAR
                {
                mSTAR(); 

                }
                break;
            case 11 :
                // W:\\workspace-cmis\\ANTLR\\FTS.g:1:63: DOTDOT
                {
                mDOTDOT(); 

                }
                break;
            case 12 :
                // W:\\workspace-cmis\\ANTLR\\FTS.g:1:70: DOT
                {
                mDOT(); 

                }
                break;
            case 13 :
                // W:\\workspace-cmis\\ANTLR\\FTS.g:1:74: ID
                {
                mID(); 

                }
                break;
            case 14 :
                // W:\\workspace-cmis\\ANTLR\\FTS.g:1:77: FTSWORD
                {
                mFTSWORD(); 

                }
                break;
            case 15 :
                // W:\\workspace-cmis\\ANTLR\\FTS.g:1:85: FTSPHRASE
                {
                mFTSPHRASE(); 

                }
                break;
            case 16 :
                // W:\\workspace-cmis\\ANTLR\\FTS.g:1:95: WS
                {
                mWS(); 

                }
                break;

        }

    }


    protected DFA5 dfa5 = new DFA5(this);
    static final String DFA5_eotS =
        "\1\uffff\3\15\7\uffff\1\26\1\15\4\uffff\1\27\3\15\3\uffff\1\32"+
        "\1\33\2\uffff";
    static final String DFA5_eofS =
        "\34\uffff";
    static final String DFA5_minS =
        "\1\11\3\60\7\uffff\1\56\1\60\4\uffff\1\43\3\60\3\uffff\2\43\2\uffff";
    static final String DFA5_maxS =
        "\4\ufaff\7\uffff\1\56\1\ufaff\4\uffff\4\ufaff\3\uffff\2\ufaff\2"+
        "\uffff";
    static final String DFA5_acceptS =
        "\4\uffff\1\4\1\5\1\6\1\7\1\10\1\11\1\12\2\uffff\1\15\1\16\1\17"+
        "\1\20\4\uffff\1\13\1\14\1\1\2\uffff\1\2\1\3";
    static final String DFA5_specialS =
        "\34\uffff}>";
    static final String[] DFA5_transitionS = {
            "\2\20\2\uffff\1\20\22\uffff\1\20\1\uffff\1\17\5\uffff\1\5\1"+
            "\6\1\12\1\7\1\uffff\1\10\1\13\1\uffff\12\16\1\11\6\uffff\1\2"+
            "\14\14\1\3\1\1\13\14\4\uffff\1\15\1\uffff\1\2\14\14\1\3\1\1"+
            "\13\14\3\uffff\1\4\101\uffff\27\16\1\uffff\37\16\1\uffff\u1f08"+
            "\16\u1040\uffff\u0150\16\u0170\uffff\u0080\16\u0080\uffff\u092e"+
            "\16\u10d2\uffff\u5200\16\u0c00\uffff\u2bb0\16\u2150\uffff\u0200"+
            "\16",
            "\1\22\11\16\7\uffff\21\22\1\21\10\22\6\uffff\21\22\1\21\10"+
            "\22\105\uffff\27\16\1\uffff\37\16\1\uffff\u1f08\16\u1040\uffff"+
            "\u0150\16\u0170\uffff\u0080\16\u0080\uffff\u092e\16\u10d2\uffff"+
            "\u5200\16\u0c00\uffff\u2bb0\16\u2150\uffff\u0200\16",
            "\1\22\11\16\7\uffff\15\22\1\23\14\22\6\uffff\15\22\1\23\14"+
            "\22\105\uffff\27\16\1\uffff\37\16\1\uffff\u1f08\16\u1040\uffff"+
            "\u0150\16\u0170\uffff\u0080\16\u0080\uffff\u092e\16\u10d2\uffff"+
            "\u5200\16\u0c00\uffff\u2bb0\16\u2150\uffff\u0200\16",
            "\1\22\11\16\7\uffff\16\22\1\24\13\22\6\uffff\16\22\1\24\13"+
            "\22\105\uffff\27\16\1\uffff\37\16\1\uffff\u1f08\16\u1040\uffff"+
            "\u0150\16\u0170\uffff\u0080\16\u0080\uffff\u092e\16\u10d2\uffff"+
            "\u5200\16\u0c00\uffff\u2bb0\16\u2150\uffff\u0200\16",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\25",
            "\1\22\11\16\7\uffff\32\22\6\uffff\32\22\105\uffff\27\16\1"+
            "\uffff\37\16\1\uffff\u1f08\16\u1040\uffff\u0150\16\u0170\uffff"+
            "\u0080\16\u0080\uffff\u092e\16\u10d2\uffff\u5200\16\u0c00\uffff"+
            "\u2bb0\16\u2150\uffff\u0200\16",
            "",
            "",
            "",
            "",
            "\2\15\13\uffff\1\22\11\16\7\uffff\32\22\4\uffff\1\15\1\uffff"+
            "\32\22\105\uffff\27\16\1\uffff\37\16\1\uffff\u1f08\16\u1040"+
            "\uffff\u0150\16\u0170\uffff\u0080\16\u0080\uffff\u092e\16\u10d2"+
            "\uffff\u5200\16\u0c00\uffff\u2bb0\16\u2150\uffff\u0200\16",
            "\1\22\11\16\7\uffff\32\22\6\uffff\32\22\105\uffff\27\16\1"+
            "\uffff\37\16\1\uffff\u1f08\16\u1040\uffff\u0150\16\u0170\uffff"+
            "\u0080\16\u0080\uffff\u092e\16\u10d2\uffff\u5200\16\u0c00\uffff"+
            "\u2bb0\16\u2150\uffff\u0200\16",
            "\1\22\11\16\7\uffff\3\22\1\30\26\22\6\uffff\3\22\1\30\26\22"+
            "\105\uffff\27\16\1\uffff\37\16\1\uffff\u1f08\16\u1040\uffff"+
            "\u0150\16\u0170\uffff\u0080\16\u0080\uffff\u092e\16\u10d2\uffff"+
            "\u5200\16\u0c00\uffff\u2bb0\16\u2150\uffff\u0200\16",
            "\1\22\11\16\7\uffff\23\22\1\31\6\22\6\uffff\23\22\1\31\6\22"+
            "\105\uffff\27\16\1\uffff\37\16\1\uffff\u1f08\16\u1040\uffff"+
            "\u0150\16\u0170\uffff\u0080\16\u0080\uffff\u092e\16\u10d2\uffff"+
            "\u5200\16\u0c00\uffff\u2bb0\16\u2150\uffff\u0200\16",
            "",
            "",
            "",
            "\2\15\13\uffff\1\22\11\16\7\uffff\32\22\4\uffff\1\15\1\uffff"+
            "\32\22\105\uffff\27\16\1\uffff\37\16\1\uffff\u1f08\16\u1040"+
            "\uffff\u0150\16\u0170\uffff\u0080\16\u0080\uffff\u092e\16\u10d2"+
            "\uffff\u5200\16\u0c00\uffff\u2bb0\16\u2150\uffff\u0200\16",
            "\2\15\13\uffff\1\22\11\16\7\uffff\32\22\4\uffff\1\15\1\uffff"+
            "\32\22\105\uffff\27\16\1\uffff\37\16\1\uffff\u1f08\16\u1040"+
            "\uffff\u0150\16\u0170\uffff\u0080\16\u0080\uffff\u092e\16\u10d2"+
            "\uffff\u5200\16\u0c00\uffff\u2bb0\16\u2150\uffff\u0200\16",
            "",
            ""
    };

    static final short[] DFA5_eot = DFA.unpackEncodedString(DFA5_eotS);
    static final short[] DFA5_eof = DFA.unpackEncodedString(DFA5_eofS);
    static final char[] DFA5_min = DFA.unpackEncodedStringToUnsignedChars(DFA5_minS);
    static final char[] DFA5_max = DFA.unpackEncodedStringToUnsignedChars(DFA5_maxS);
    static final short[] DFA5_accept = DFA.unpackEncodedString(DFA5_acceptS);
    static final short[] DFA5_special = DFA.unpackEncodedString(DFA5_specialS);
    static final short[][] DFA5_transition;

    static {
        int numStates = DFA5_transitionS.length;
        DFA5_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA5_transition[i] = DFA.unpackEncodedString(DFA5_transitionS[i]);
        }
    }

    class DFA5 extends DFA {

        public DFA5(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 5;
            this.eot = DFA5_eot;
            this.eof = DFA5_eof;
            this.min = DFA5_min;
            this.max = DFA5_max;
            this.accept = DFA5_accept;
            this.special = DFA5_special;
            this.transition = DFA5_transition;
        }
        public String getDescription() {
            return "1:1: Tokens : ( OR | AND | NOT | TILDA | LPAREN | RPAREN | PLUS | MINUS | COLON | STAR | DOTDOT | DOT | ID | FTSWORD | FTSPHRASE | WS );";
        }
    }
 

}