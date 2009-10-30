// $ANTLR !Unknown version! W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g 2009-10-28 12:52:46
package org.alfresco.repo.search.impl.parsers;

import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
public class FTSLexer extends Lexer {
    public static final int PREFIX=36;
    public static final int EXPONENT=89;
    public static final int LT=58;
    public static final int STAR=54;
    public static final int LSQUARE=57;
    public static final int FG_TERM=26;
    public static final int FUZZY=39;
    public static final int FIELD_DISJUNCTION=18;
    public static final int EQUALS=52;
    public static final int F_URI_ALPHA=74;
    public static final int FG_EXACT_TERM=27;
    public static final int NOT=69;
    public static final int FIELD_EXCLUDE=25;
    public static final int EOF=-1;
    public static final int NAME_SPACE=37;
    public static final int RPAREN=45;
    public static final int EXCLAMATION=72;
    public static final int FLOATING_POINT_LITERAL=70;
    public static final int QUESTION_MARK=80;
    public static final int ZERO_DIGIT=90;
    public static final int FIELD_OPTIONAL=24;
    public static final int SYNONYM=11;
    public static final int E=92;
    public static final int CONJUNCTION=6;
    public static final int FTSWORD=64;
    public static final int URI=62;
    public static final int DISJUNCTION=5;
    public static final int FTS=4;
    public static final int FG_SYNONYM=29;
    public static final int WS=94;
    public static final int FTSPHRASE=53;
    public static final int FIELD_CONJUNCTION=19;
    public static final int INCLUSIVE=33;
    public static final int OR=67;
    public static final int GT=60;
    public static final int F_HEX=77;
    public static final int DECIMAL_INTEGER_LITERAL=49;
    public static final int FTSPRE=65;
    public static final int FG_PHRASE=28;
    public static final int FIELD_NEGATION=20;
    public static final int TERM=8;
    public static final int DOLLAR=83;
    public static final int START_RANGE_I=86;
    public static final int AMP=71;
    public static final int FG_PROXIMITY=30;
    public static final int EXACT_TERM=9;
    public static final int START_RANGE_F=87;
    public static final int DOTDOT=55;
    public static final int MANDATORY=15;
    public static final int EXCLUSIVE=34;
    public static final int ID=63;
    public static final int AND=68;
    public static final int LPAREN=44;
    public static final int BOOST=38;
    public static final int AT=61;
    public static final int TILDA=48;
    public static final int DECIMAL_NUMERAL=84;
    public static final int COMMA=47;
    public static final int F_URI_DIGIT=75;
    public static final int SIGNED_INTEGER=93;
    public static final int FIELD_DEFAULT=22;
    public static final int CARAT=50;
    public static final int PLUS=41;
    public static final int DIGIT=88;
    public static final int DOT=79;
    public static final int F_ESC=73;
    public static final int EXCLUDE=17;
    public static final int PERCENT=46;
    public static final int NON_ZERO_DIGIT=91;
    public static final int QUALIFIER=35;
    public static final int TO=56;
    public static final int FIELD_GROUP=21;
    public static final int DEFAULT=14;
    public static final int INWORD=85;
    public static final int RANGE=12;
    public static final int MINUS=43;
    public static final int RSQUARE=59;
    public static final int FIELD_REF=32;
    public static final int PROXIMITY=13;
    public static final int PHRASE=10;
    public static final int OPTIONAL=16;
    public static final int COLON=51;
    public static final int LCURL=81;
    public static final int F_URI_OTHER=76;
    public static final int NEGATION=7;
    public static final int F_URI_ESC=78;
    public static final int TEMPLATE=40;
    public static final int RCURL=82;
    public static final int FIELD_MANDATORY=23;
    public static final int FG_RANGE=31;
    public static final int BAR=42;
    public static final int FTSWILD=66;

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


    // delegates
    // delegators

    public FTSLexer() {;} 
    public FTSLexer(CharStream input) {
        this(input, new RecognizerSharedState());
    }
    public FTSLexer(CharStream input, RecognizerSharedState state) {
        super(input,state);

    }
    public String getGrammarFileName() { return "W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g"; }

    // $ANTLR start "FTSPHRASE"
    public final void mFTSPHRASE() throws RecognitionException {
        try {
            int _type = FTSPHRASE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:510:4: ( '\"' ( F_ESC | ~ ( '\\\\' | '\"' ) )* '\"' )
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:510:7: '\"' ( F_ESC | ~ ( '\\\\' | '\"' ) )* '\"'
            {
            match('\"'); if (state.failed) return ;
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:510:11: ( F_ESC | ~ ( '\\\\' | '\"' ) )*
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
            	    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:510:12: F_ESC
            	    {
            	    mF_ESC(); if (state.failed) return ;

            	    }
            	    break;
            	case 2 :
            	    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:510:20: ~ ( '\\\\' | '\"' )
            	    {
            	    if ( (input.LA(1)>='\u0000' && input.LA(1)<='!')||(input.LA(1)>='#' && input.LA(1)<='[')||(input.LA(1)>=']' && input.LA(1)<='\uFFFF') ) {
            	        input.consume();
            	    state.failed=false;
            	    }
            	    else {
            	        if (state.backtracking>0) {state.failed=true; return ;}
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;}


            	    }
            	    break;

            	default :
            	    break loop1;
                }
            } while (true);

            match('\"'); if (state.failed) return ;

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "FTSPHRASE"

    // $ANTLR start "URI"
    public final void mURI() throws RecognitionException {
        try {
            int _type = URI;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:518:2: ( '{' ( ( F_URI_ALPHA | F_URI_DIGIT | F_URI_OTHER )=> ( F_URI_ALPHA | F_URI_DIGIT | F_URI_OTHER )+ COLON )? ( ( ( '//' )=> '//' ) ( ( F_URI_ALPHA | F_URI_DIGIT | F_URI_OTHER | COLON )=> ( F_URI_ALPHA | F_URI_DIGIT | F_URI_OTHER | COLON ) )* )? ( F_URI_ALPHA | F_URI_DIGIT | F_URI_OTHER | COLON | '/' )* ( '?' ( F_URI_ALPHA | F_URI_DIGIT | F_URI_OTHER | COLON | '/' | '?' )* )? ( '#' ( F_URI_ALPHA | F_URI_DIGIT | F_URI_OTHER | COLON | '/' | '?' | '#' )* )? '}' )
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:518:5: '{' ( ( F_URI_ALPHA | F_URI_DIGIT | F_URI_OTHER )=> ( F_URI_ALPHA | F_URI_DIGIT | F_URI_OTHER )+ COLON )? ( ( ( '//' )=> '//' ) ( ( F_URI_ALPHA | F_URI_DIGIT | F_URI_OTHER | COLON )=> ( F_URI_ALPHA | F_URI_DIGIT | F_URI_OTHER | COLON ) )* )? ( F_URI_ALPHA | F_URI_DIGIT | F_URI_OTHER | COLON | '/' )* ( '?' ( F_URI_ALPHA | F_URI_DIGIT | F_URI_OTHER | COLON | '/' | '?' )* )? ( '#' ( F_URI_ALPHA | F_URI_DIGIT | F_URI_OTHER | COLON | '/' | '?' | '#' )* )? '}'
            {
            match('{'); if (state.failed) return ;
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:519:10: ( ( F_URI_ALPHA | F_URI_DIGIT | F_URI_OTHER )=> ( F_URI_ALPHA | F_URI_DIGIT | F_URI_OTHER )+ COLON )?
            int alt3=2;
            alt3 = dfa3.predict(input);
            switch (alt3) {
                case 1 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:519:11: ( F_URI_ALPHA | F_URI_DIGIT | F_URI_OTHER )=> ( F_URI_ALPHA | F_URI_DIGIT | F_URI_OTHER )+ COLON
                    {
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:519:52: ( F_URI_ALPHA | F_URI_DIGIT | F_URI_OTHER )+
                    int cnt2=0;
                    loop2:
                    do {
                        int alt2=2;
                        int LA2_0 = input.LA(1);

                        if ( (LA2_0=='!'||LA2_0=='$'||(LA2_0>='&' && LA2_0<='.')||(LA2_0>='0' && LA2_0<='9')||LA2_0==';'||LA2_0=='='||(LA2_0>='@' && LA2_0<='[')||LA2_0==']'||LA2_0=='_'||(LA2_0>='a' && LA2_0<='z')||LA2_0=='~') ) {
                            alt2=1;
                        }


                        switch (alt2) {
                    	case 1 :
                    	    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:
                    	    {
                    	    if ( input.LA(1)=='!'||input.LA(1)=='$'||(input.LA(1)>='&' && input.LA(1)<='.')||(input.LA(1)>='0' && input.LA(1)<='9')||input.LA(1)==';'||input.LA(1)=='='||(input.LA(1)>='@' && input.LA(1)<='[')||input.LA(1)==']'||input.LA(1)=='_'||(input.LA(1)>='a' && input.LA(1)<='z')||input.LA(1)=='~' ) {
                    	        input.consume();
                    	    state.failed=false;
                    	    }
                    	    else {
                    	        if (state.backtracking>0) {state.failed=true; return ;}
                    	        MismatchedSetException mse = new MismatchedSetException(null,input);
                    	        recover(mse);
                    	        throw mse;}


                    	    }
                    	    break;

                    	default :
                    	    if ( cnt2 >= 1 ) break loop2;
                    	    if (state.backtracking>0) {state.failed=true; return ;}
                                EarlyExitException eee =
                                    new EarlyExitException(2, input);
                                throw eee;
                        }
                        cnt2++;
                    } while (true);

                    mCOLON(); if (state.failed) return ;

                    }
                    break;

            }

            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:520:10: ( ( ( '//' )=> '//' ) ( ( F_URI_ALPHA | F_URI_DIGIT | F_URI_OTHER | COLON )=> ( F_URI_ALPHA | F_URI_DIGIT | F_URI_OTHER | COLON ) )* )?
            int alt5=2;
            int LA5_0 = input.LA(1);

            if ( (LA5_0=='/') ) {
                int LA5_1 = input.LA(2);

                if ( (LA5_1=='/') ) {
                    int LA5_3 = input.LA(3);

                    if ( (synpred2_FTS()) ) {
                        alt5=1;
                    }
                }
            }
            switch (alt5) {
                case 1 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:520:11: ( ( '//' )=> '//' ) ( ( F_URI_ALPHA | F_URI_DIGIT | F_URI_OTHER | COLON )=> ( F_URI_ALPHA | F_URI_DIGIT | F_URI_OTHER | COLON ) )*
                    {
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:520:11: ( ( '//' )=> '//' )
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:520:12: ( '//' )=> '//'
                    {
                    match("//"); if (state.failed) return ;


                    }

                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:521:10: ( ( F_URI_ALPHA | F_URI_DIGIT | F_URI_OTHER | COLON )=> ( F_URI_ALPHA | F_URI_DIGIT | F_URI_OTHER | COLON ) )*
                    loop4:
                    do {
                        int alt4=2;
                        int LA4_0 = input.LA(1);

                        if ( (LA4_0=='!'||LA4_0=='$'||(LA4_0>='&' && LA4_0<='.')||(LA4_0>='0' && LA4_0<=';')||LA4_0=='='||(LA4_0>='@' && LA4_0<='[')||LA4_0==']'||LA4_0=='_'||(LA4_0>='a' && LA4_0<='z')||LA4_0=='~') ) {
                            int LA4_1 = input.LA(2);

                            if ( (synpred3_FTS()) ) {
                                alt4=1;
                            }


                        }


                        switch (alt4) {
                    	case 1 :
                    	    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:521:12: ( F_URI_ALPHA | F_URI_DIGIT | F_URI_OTHER | COLON )=> ( F_URI_ALPHA | F_URI_DIGIT | F_URI_OTHER | COLON )
                    	    {
                    	    if ( input.LA(1)=='!'||input.LA(1)=='$'||(input.LA(1)>='&' && input.LA(1)<='.')||(input.LA(1)>='0' && input.LA(1)<=';')||input.LA(1)=='='||(input.LA(1)>='@' && input.LA(1)<='[')||input.LA(1)==']'||input.LA(1)=='_'||(input.LA(1)>='a' && input.LA(1)<='z')||input.LA(1)=='~' ) {
                    	        input.consume();
                    	    state.failed=false;
                    	    }
                    	    else {
                    	        if (state.backtracking>0) {state.failed=true; return ;}
                    	        MismatchedSetException mse = new MismatchedSetException(null,input);
                    	        recover(mse);
                    	        throw mse;}


                    	    }
                    	    break;

                    	default :
                    	    break loop4;
                        }
                    } while (true);


                    }
                    break;

            }

            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:522:10: ( F_URI_ALPHA | F_URI_DIGIT | F_URI_OTHER | COLON | '/' )*
            loop6:
            do {
                int alt6=2;
                int LA6_0 = input.LA(1);

                if ( (LA6_0=='!'||LA6_0=='$'||(LA6_0>='&' && LA6_0<=';')||LA6_0=='='||(LA6_0>='@' && LA6_0<='[')||LA6_0==']'||LA6_0=='_'||(LA6_0>='a' && LA6_0<='z')||LA6_0=='~') ) {
                    alt6=1;
                }


                switch (alt6) {
            	case 1 :
            	    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:
            	    {
            	    if ( input.LA(1)=='!'||input.LA(1)=='$'||(input.LA(1)>='&' && input.LA(1)<=';')||input.LA(1)=='='||(input.LA(1)>='@' && input.LA(1)<='[')||input.LA(1)==']'||input.LA(1)=='_'||(input.LA(1)>='a' && input.LA(1)<='z')||input.LA(1)=='~' ) {
            	        input.consume();
            	    state.failed=false;
            	    }
            	    else {
            	        if (state.backtracking>0) {state.failed=true; return ;}
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;}


            	    }
            	    break;

            	default :
            	    break loop6;
                }
            } while (true);

            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:523:10: ( '?' ( F_URI_ALPHA | F_URI_DIGIT | F_URI_OTHER | COLON | '/' | '?' )* )?
            int alt8=2;
            int LA8_0 = input.LA(1);

            if ( (LA8_0=='?') ) {
                alt8=1;
            }
            switch (alt8) {
                case 1 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:523:11: '?' ( F_URI_ALPHA | F_URI_DIGIT | F_URI_OTHER | COLON | '/' | '?' )*
                    {
                    match('?'); if (state.failed) return ;
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:523:15: ( F_URI_ALPHA | F_URI_DIGIT | F_URI_OTHER | COLON | '/' | '?' )*
                    loop7:
                    do {
                        int alt7=2;
                        int LA7_0 = input.LA(1);

                        if ( (LA7_0=='!'||LA7_0=='$'||(LA7_0>='&' && LA7_0<=';')||LA7_0=='='||(LA7_0>='?' && LA7_0<='[')||LA7_0==']'||LA7_0=='_'||(LA7_0>='a' && LA7_0<='z')||LA7_0=='~') ) {
                            alt7=1;
                        }


                        switch (alt7) {
                    	case 1 :
                    	    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:
                    	    {
                    	    if ( input.LA(1)=='!'||input.LA(1)=='$'||(input.LA(1)>='&' && input.LA(1)<=';')||input.LA(1)=='='||(input.LA(1)>='?' && input.LA(1)<='[')||input.LA(1)==']'||input.LA(1)=='_'||(input.LA(1)>='a' && input.LA(1)<='z')||input.LA(1)=='~' ) {
                    	        input.consume();
                    	    state.failed=false;
                    	    }
                    	    else {
                    	        if (state.backtracking>0) {state.failed=true; return ;}
                    	        MismatchedSetException mse = new MismatchedSetException(null,input);
                    	        recover(mse);
                    	        throw mse;}


                    	    }
                    	    break;

                    	default :
                    	    break loop7;
                        }
                    } while (true);


                    }
                    break;

            }

            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:524:10: ( '#' ( F_URI_ALPHA | F_URI_DIGIT | F_URI_OTHER | COLON | '/' | '?' | '#' )* )?
            int alt10=2;
            int LA10_0 = input.LA(1);

            if ( (LA10_0=='#') ) {
                alt10=1;
            }
            switch (alt10) {
                case 1 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:524:11: '#' ( F_URI_ALPHA | F_URI_DIGIT | F_URI_OTHER | COLON | '/' | '?' | '#' )*
                    {
                    match('#'); if (state.failed) return ;
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:524:15: ( F_URI_ALPHA | F_URI_DIGIT | F_URI_OTHER | COLON | '/' | '?' | '#' )*
                    loop9:
                    do {
                        int alt9=2;
                        int LA9_0 = input.LA(1);

                        if ( (LA9_0=='!'||(LA9_0>='#' && LA9_0<='$')||(LA9_0>='&' && LA9_0<=';')||LA9_0=='='||(LA9_0>='?' && LA9_0<='[')||LA9_0==']'||LA9_0=='_'||(LA9_0>='a' && LA9_0<='z')||LA9_0=='~') ) {
                            alt9=1;
                        }


                        switch (alt9) {
                    	case 1 :
                    	    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:
                    	    {
                    	    if ( input.LA(1)=='!'||(input.LA(1)>='#' && input.LA(1)<='$')||(input.LA(1)>='&' && input.LA(1)<=';')||input.LA(1)=='='||(input.LA(1)>='?' && input.LA(1)<='[')||input.LA(1)==']'||input.LA(1)=='_'||(input.LA(1)>='a' && input.LA(1)<='z')||input.LA(1)=='~' ) {
                    	        input.consume();
                    	    state.failed=false;
                    	    }
                    	    else {
                    	        if (state.backtracking>0) {state.failed=true; return ;}
                    	        MismatchedSetException mse = new MismatchedSetException(null,input);
                    	        recover(mse);
                    	        throw mse;}


                    	    }
                    	    break;

                    	default :
                    	    break loop9;
                        }
                    } while (true);


                    }
                    break;

            }

            match('}'); if (state.failed) return ;

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "URI"

    // $ANTLR start "F_URI_ALPHA"
    public final void mF_URI_ALPHA() throws RecognitionException {
        try {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:531:2: ( 'A' .. 'Z' | 'a' .. 'z' )
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:
            {
            if ( (input.LA(1)>='A' && input.LA(1)<='Z')||(input.LA(1)>='a' && input.LA(1)<='z') ) {
                input.consume();
            state.failed=false;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "F_URI_ALPHA"

    // $ANTLR start "F_URI_DIGIT"
    public final void mF_URI_DIGIT() throws RecognitionException {
        try {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:536:2: ( '0' .. '9' )
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:536:4: '0' .. '9'
            {
            matchRange('0','9'); if (state.failed) return ;

            }

        }
        finally {
        }
    }
    // $ANTLR end "F_URI_DIGIT"

    // $ANTLR start "F_URI_ESC"
    public final void mF_URI_ESC() throws RecognitionException {
        try {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:541:2: ( '%' F_HEX F_HEX )
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:541:10: '%' F_HEX F_HEX
            {
            match('%'); if (state.failed) return ;
            mF_HEX(); if (state.failed) return ;
            mF_HEX(); if (state.failed) return ;

            }

        }
        finally {
        }
    }
    // $ANTLR end "F_URI_ESC"

    // $ANTLR start "F_URI_OTHER"
    public final void mF_URI_OTHER() throws RecognitionException {
        try {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:546:2: ( '-' | '.' | '_' | '~' | '[' | ']' | '@' | '!' | '$' | '&' | '\\'' | '(' | ')' | '*' | '+' | ',' | ';' | '=' )
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:
            {
            if ( input.LA(1)=='!'||input.LA(1)=='$'||(input.LA(1)>='&' && input.LA(1)<='.')||input.LA(1)==';'||input.LA(1)=='='||input.LA(1)=='@'||input.LA(1)=='['||input.LA(1)==']'||input.LA(1)=='_'||input.LA(1)=='~' ) {
                input.consume();
            state.failed=false;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "F_URI_OTHER"

    // $ANTLR start "OR"
    public final void mOR() throws RecognitionException {
        try {
            int _type = OR;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:553:4: ( ( 'O' | 'o' ) ( 'R' | 'r' ) )
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:553:6: ( 'O' | 'o' ) ( 'R' | 'r' )
            {
            if ( input.LA(1)=='O'||input.LA(1)=='o' ) {
                input.consume();
            state.failed=false;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='R'||input.LA(1)=='r' ) {
                input.consume();
            state.failed=false;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
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
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:554:5: ( ( 'A' | 'a' ) ( 'N' | 'n' ) ( 'D' | 'd' ) )
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:554:7: ( 'A' | 'a' ) ( 'N' | 'n' ) ( 'D' | 'd' )
            {
            if ( input.LA(1)=='A'||input.LA(1)=='a' ) {
                input.consume();
            state.failed=false;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='N'||input.LA(1)=='n' ) {
                input.consume();
            state.failed=false;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='D'||input.LA(1)=='d' ) {
                input.consume();
            state.failed=false;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
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
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:555:5: ( ( 'N' | 'n' ) ( 'O' | 'o' ) ( 'T' | 't' ) )
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:555:7: ( 'N' | 'n' ) ( 'O' | 'o' ) ( 'T' | 't' )
            {
            if ( input.LA(1)=='N'||input.LA(1)=='n' ) {
                input.consume();
            state.failed=false;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='O'||input.LA(1)=='o' ) {
                input.consume();
            state.failed=false;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='T'||input.LA(1)=='t' ) {
                input.consume();
            state.failed=false;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
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
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:556:7: ( '~' )
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:556:9: '~'
            {
            match('~'); if (state.failed) return ;

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
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:557:8: ( '(' )
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:557:10: '('
            {
            match('('); if (state.failed) return ;

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
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:558:8: ( ')' )
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:558:10: ')'
            {
            match(')'); if (state.failed) return ;

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
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:559:6: ( '+' )
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:559:8: '+'
            {
            match('+'); if (state.failed) return ;

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
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:560:7: ( '-' )
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:560:9: '-'
            {
            match('-'); if (state.failed) return ;

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
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:561:7: ( ':' )
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:561:9: ':'
            {
            match(':'); if (state.failed) return ;

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
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:562:6: ( '*' )
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:562:8: '*'
            {
            match('*'); if (state.failed) return ;

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
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:563:9: ( '..' )
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:563:12: '..'
            {
            match(".."); if (state.failed) return ;


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
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:564:5: ( '.' )
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:564:7: '.'
            {
            match('.'); if (state.failed) return ;

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
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:565:5: ( '&' )
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:565:7: '&'
            {
            match('&'); if (state.failed) return ;

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
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:566:13: ( '!' )
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:566:16: '!'
            {
            match('!'); if (state.failed) return ;

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
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:567:6: ( '|' )
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:567:9: '|'
            {
            match('|'); if (state.failed) return ;

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
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:568:9: ( '=' )
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:568:12: '='
            {
            match('='); if (state.failed) return ;

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
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:569:15: ( '?' )
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:569:17: '?'
            {
            match('?'); if (state.failed) return ;

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
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:570:8: ( '{' )
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:570:11: '{'
            {
            match('{'); if (state.failed) return ;

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
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:571:8: ( '}' )
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:571:11: '}'
            {
            match('}'); if (state.failed) return ;

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
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:572:9: ( '[' )
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:572:12: '['
            {
            match('['); if (state.failed) return ;

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
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:573:9: ( ']' )
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:573:12: ']'
            {
            match(']'); if (state.failed) return ;

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
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:574:5: ( ( 'T' | 't' ) ( 'O' | 'o' ) )
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:574:8: ( 'T' | 't' ) ( 'O' | 'o' )
            {
            if ( input.LA(1)=='T'||input.LA(1)=='t' ) {
                input.consume();
            state.failed=false;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='O'||input.LA(1)=='o' ) {
                input.consume();
            state.failed=false;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
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
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:575:8: ( ',' )
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:575:11: ','
            {
            match(','); if (state.failed) return ;

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
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:576:8: ( '^' )
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:576:11: '^'
            {
            match('^'); if (state.failed) return ;

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
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:577:9: ( '$' )
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:577:13: '$'
            {
            match('$'); if (state.failed) return ;

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "DOLLAR"

    // $ANTLR start "GT"
    public final void mGT() throws RecognitionException {
        try {
            int _type = GT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:578:5: ( '>' )
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:578:8: '>'
            {
            match('>'); if (state.failed) return ;

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "GT"

    // $ANTLR start "LT"
    public final void mLT() throws RecognitionException {
        try {
            int _type = LT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:579:5: ( '<' )
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:579:8: '<'
            {
            match('<'); if (state.failed) return ;

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "LT"

    // $ANTLR start "AT"
    public final void mAT() throws RecognitionException {
        try {
            int _type = AT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:580:4: ( '@' )
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:580:7: '@'
            {
            match('@'); if (state.failed) return ;

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "AT"

    // $ANTLR start "PERCENT"
    public final void mPERCENT() throws RecognitionException {
        try {
            int _type = PERCENT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:581:9: ( '%' )
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:581:17: '%'
            {
            match('%'); if (state.failed) return ;

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "PERCENT"

    // $ANTLR start "ID"
    public final void mID() throws RecognitionException {
        try {
            int _type = ID;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:588:6: ( ( 'a' .. 'z' | 'A' .. 'Z' | '_' ) ( 'a' .. 'z' | 'A' .. 'Z' | '0' .. '9' | '_' | '$' | '#' | F_ESC )* )
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:588:11: ( 'a' .. 'z' | 'A' .. 'Z' | '_' ) ( 'a' .. 'z' | 'A' .. 'Z' | '0' .. '9' | '_' | '$' | '#' | F_ESC )*
            {
            if ( (input.LA(1)>='A' && input.LA(1)<='Z')||input.LA(1)=='_'||(input.LA(1)>='a' && input.LA(1)<='z') ) {
                input.consume();
            state.failed=false;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:588:34: ( 'a' .. 'z' | 'A' .. 'Z' | '0' .. '9' | '_' | '$' | '#' | F_ESC )*
            loop11:
            do {
                int alt11=8;
                switch ( input.LA(1) ) {
                case 'a':
                case 'b':
                case 'c':
                case 'd':
                case 'e':
                case 'f':
                case 'g':
                case 'h':
                case 'i':
                case 'j':
                case 'k':
                case 'l':
                case 'm':
                case 'n':
                case 'o':
                case 'p':
                case 'q':
                case 'r':
                case 's':
                case 't':
                case 'u':
                case 'v':
                case 'w':
                case 'x':
                case 'y':
                case 'z':
                    {
                    alt11=1;
                    }
                    break;
                case 'A':
                case 'B':
                case 'C':
                case 'D':
                case 'E':
                case 'F':
                case 'G':
                case 'H':
                case 'I':
                case 'J':
                case 'K':
                case 'L':
                case 'M':
                case 'N':
                case 'O':
                case 'P':
                case 'Q':
                case 'R':
                case 'S':
                case 'T':
                case 'U':
                case 'V':
                case 'W':
                case 'X':
                case 'Y':
                case 'Z':
                    {
                    alt11=2;
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
                    {
                    alt11=3;
                    }
                    break;
                case '_':
                    {
                    alt11=4;
                    }
                    break;
                case '$':
                    {
                    alt11=5;
                    }
                    break;
                case '#':
                    {
                    alt11=6;
                    }
                    break;
                case '\\':
                    {
                    alt11=7;
                    }
                    break;

                }

                switch (alt11) {
            	case 1 :
            	    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:588:35: 'a' .. 'z'
            	    {
            	    matchRange('a','z'); if (state.failed) return ;

            	    }
            	    break;
            	case 2 :
            	    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:588:44: 'A' .. 'Z'
            	    {
            	    matchRange('A','Z'); if (state.failed) return ;

            	    }
            	    break;
            	case 3 :
            	    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:588:53: '0' .. '9'
            	    {
            	    matchRange('0','9'); if (state.failed) return ;

            	    }
            	    break;
            	case 4 :
            	    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:588:62: '_'
            	    {
            	    match('_'); if (state.failed) return ;

            	    }
            	    break;
            	case 5 :
            	    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:588:66: '$'
            	    {
            	    match('$'); if (state.failed) return ;

            	    }
            	    break;
            	case 6 :
            	    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:588:70: '#'
            	    {
            	    match('#'); if (state.failed) return ;

            	    }
            	    break;
            	case 7 :
            	    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:588:74: F_ESC
            	    {
            	    mF_ESC(); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop11;
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

    // $ANTLR start "DECIMAL_INTEGER_LITERAL"
    public final void mDECIMAL_INTEGER_LITERAL() throws RecognitionException {
        try {
            int _type = DECIMAL_INTEGER_LITERAL;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:591:9: ( ( PLUS | MINUS )? DECIMAL_NUMERAL )
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:591:12: ( PLUS | MINUS )? DECIMAL_NUMERAL
            {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:591:12: ( PLUS | MINUS )?
            int alt12=2;
            int LA12_0 = input.LA(1);

            if ( (LA12_0=='+'||LA12_0=='-') ) {
                alt12=1;
            }
            switch (alt12) {
                case 1 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:
                    {
                    if ( input.LA(1)=='+'||input.LA(1)=='-' ) {
                        input.consume();
                    state.failed=false;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;}


                    }
                    break;

            }

            mDECIMAL_NUMERAL(); if (state.failed) return ;

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "DECIMAL_INTEGER_LITERAL"

    // $ANTLR start "FTSWORD"
    public final void mFTSWORD() throws RecognitionException {
        try {
            int _type = FTSWORD;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:594:9: ( ( F_ESC | INWORD )+ )
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:594:13: ( F_ESC | INWORD )+
            {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:594:13: ( F_ESC | INWORD )+
            int cnt13=0;
            loop13:
            do {
                int alt13=3;
                int LA13_0 = input.LA(1);

                if ( (LA13_0=='\\') ) {
                    alt13=1;
                }
                else if ( ((LA13_0>='0' && LA13_0<='9')||(LA13_0>='A' && LA13_0<='Z')||(LA13_0>='a' && LA13_0<='z')||(LA13_0>='\u00C0' && LA13_0<='\u00D6')||(LA13_0>='\u00D8' && LA13_0<='\u00F6')||(LA13_0>='\u00F8' && LA13_0<='\u1FFF')||(LA13_0>='\u3040' && LA13_0<='\u318F')||(LA13_0>='\u3300' && LA13_0<='\u337F')||(LA13_0>='\u3400' && LA13_0<='\u3D2D')||(LA13_0>='\u4E00' && LA13_0<='\u9FFF')||(LA13_0>='\uAC00' && LA13_0<='\uD7AF')||(LA13_0>='\uF900' && LA13_0<='\uFAFF')) ) {
                    alt13=2;
                }


                switch (alt13) {
            	case 1 :
            	    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:594:14: F_ESC
            	    {
            	    mF_ESC(); if (state.failed) return ;

            	    }
            	    break;
            	case 2 :
            	    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:594:22: INWORD
            	    {
            	    mINWORD(); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    if ( cnt13 >= 1 ) break loop13;
            	    if (state.backtracking>0) {state.failed=true; return ;}
                        EarlyExitException eee =
                            new EarlyExitException(13, input);
                        throw eee;
                }
                cnt13++;
            } while (true);


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "FTSWORD"

    // $ANTLR start "FTSPRE"
    public final void mFTSPRE() throws RecognitionException {
        try {
            int _type = FTSPRE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:596:9: ( ( F_ESC | INWORD )+ STAR )
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:596:13: ( F_ESC | INWORD )+ STAR
            {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:596:13: ( F_ESC | INWORD )+
            int cnt14=0;
            loop14:
            do {
                int alt14=3;
                int LA14_0 = input.LA(1);

                if ( (LA14_0=='\\') ) {
                    alt14=1;
                }
                else if ( ((LA14_0>='0' && LA14_0<='9')||(LA14_0>='A' && LA14_0<='Z')||(LA14_0>='a' && LA14_0<='z')||(LA14_0>='\u00C0' && LA14_0<='\u00D6')||(LA14_0>='\u00D8' && LA14_0<='\u00F6')||(LA14_0>='\u00F8' && LA14_0<='\u1FFF')||(LA14_0>='\u3040' && LA14_0<='\u318F')||(LA14_0>='\u3300' && LA14_0<='\u337F')||(LA14_0>='\u3400' && LA14_0<='\u3D2D')||(LA14_0>='\u4E00' && LA14_0<='\u9FFF')||(LA14_0>='\uAC00' && LA14_0<='\uD7AF')||(LA14_0>='\uF900' && LA14_0<='\uFAFF')) ) {
                    alt14=2;
                }


                switch (alt14) {
            	case 1 :
            	    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:596:14: F_ESC
            	    {
            	    mF_ESC(); if (state.failed) return ;

            	    }
            	    break;
            	case 2 :
            	    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:596:22: INWORD
            	    {
            	    mINWORD(); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    if ( cnt14 >= 1 ) break loop14;
            	    if (state.backtracking>0) {state.failed=true; return ;}
                        EarlyExitException eee =
                            new EarlyExitException(14, input);
                        throw eee;
                }
                cnt14++;
            } while (true);

            mSTAR(); if (state.failed) return ;

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "FTSPRE"

    // $ANTLR start "FTSWILD"
    public final void mFTSWILD() throws RecognitionException {
        try {
            int _type = FTSWILD;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:598:9: ( ( F_ESC | INWORD | STAR | QUESTION_MARK )+ )
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:598:12: ( F_ESC | INWORD | STAR | QUESTION_MARK )+
            {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:598:12: ( F_ESC | INWORD | STAR | QUESTION_MARK )+
            int cnt15=0;
            loop15:
            do {
                int alt15=5;
                int LA15_0 = input.LA(1);

                if ( (LA15_0=='\\') ) {
                    alt15=1;
                }
                else if ( ((LA15_0>='0' && LA15_0<='9')||(LA15_0>='A' && LA15_0<='Z')||(LA15_0>='a' && LA15_0<='z')||(LA15_0>='\u00C0' && LA15_0<='\u00D6')||(LA15_0>='\u00D8' && LA15_0<='\u00F6')||(LA15_0>='\u00F8' && LA15_0<='\u1FFF')||(LA15_0>='\u3040' && LA15_0<='\u318F')||(LA15_0>='\u3300' && LA15_0<='\u337F')||(LA15_0>='\u3400' && LA15_0<='\u3D2D')||(LA15_0>='\u4E00' && LA15_0<='\u9FFF')||(LA15_0>='\uAC00' && LA15_0<='\uD7AF')||(LA15_0>='\uF900' && LA15_0<='\uFAFF')) ) {
                    alt15=2;
                }
                else if ( (LA15_0=='*') ) {
                    alt15=3;
                }
                else if ( (LA15_0=='?') ) {
                    alt15=4;
                }


                switch (alt15) {
            	case 1 :
            	    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:598:13: F_ESC
            	    {
            	    mF_ESC(); if (state.failed) return ;

            	    }
            	    break;
            	case 2 :
            	    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:598:21: INWORD
            	    {
            	    mINWORD(); if (state.failed) return ;

            	    }
            	    break;
            	case 3 :
            	    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:598:30: STAR
            	    {
            	    mSTAR(); if (state.failed) return ;

            	    }
            	    break;
            	case 4 :
            	    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:598:37: QUESTION_MARK
            	    {
            	    mQUESTION_MARK(); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    if ( cnt15 >= 1 ) break loop15;
            	    if (state.backtracking>0) {state.failed=true; return ;}
                        EarlyExitException eee =
                            new EarlyExitException(15, input);
                        throw eee;
                }
                cnt15++;
            } while (true);


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "FTSWILD"

    // $ANTLR start "F_ESC"
    public final void mF_ESC() throws RecognitionException {
        try {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:601:9: ( '\\\\' ( 'u' F_HEX F_HEX F_HEX F_HEX | . ) )
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:601:12: '\\\\' ( 'u' F_HEX F_HEX F_HEX F_HEX | . )
            {
            match('\\'); if (state.failed) return ;
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:602:7: ( 'u' F_HEX F_HEX F_HEX F_HEX | . )
            int alt16=2;
            int LA16_0 = input.LA(1);

            if ( (LA16_0=='u') ) {
                int LA16_1 = input.LA(2);

                if ( ((LA16_1>='0' && LA16_1<='9')||(LA16_1>='A' && LA16_1<='F')||(LA16_1>='a' && LA16_1<='f')) ) {
                    alt16=1;
                }
                else {
                    alt16=2;}
            }
            else if ( ((LA16_0>='\u0000' && LA16_0<='t')||(LA16_0>='v' && LA16_0<='\uFFFF')) ) {
                alt16=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 16, 0, input);

                throw nvae;
            }
            switch (alt16) {
                case 1 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:604:7: 'u' F_HEX F_HEX F_HEX F_HEX
                    {
                    match('u'); if (state.failed) return ;
                    mF_HEX(); if (state.failed) return ;
                    mF_HEX(); if (state.failed) return ;
                    mF_HEX(); if (state.failed) return ;
                    mF_HEX(); if (state.failed) return ;

                    }
                    break;
                case 2 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:606:8: .
                    {
                    matchAny(); if (state.failed) return ;

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
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:612:2: ( '0' .. '9' | 'a' .. 'f' | 'A' .. 'F' )
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:
            {
            if ( (input.LA(1)>='0' && input.LA(1)<='9')||(input.LA(1)>='A' && input.LA(1)<='F')||(input.LA(1)>='a' && input.LA(1)<='f') ) {
                input.consume();
            state.failed=false;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
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
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:619:2: ( '\\u0041' .. '\\u005A' | '\\u0061' .. '\\u007A' | '\\u00C0' .. '\\u00D6' | '\\u00D8' .. '\\u00F6' | '\\u00F8' .. '\\u00FF' | '\\u0100' .. '\\u1FFF' | '\\u3040' .. '\\u318F' | '\\u3300' .. '\\u337F' | '\\u3400' .. '\\u3D2D' | '\\u4E00' .. '\\u9FFF' | '\\uF900' .. '\\uFAFF' | '\\uAC00' .. '\\uD7AF' | '\\u0030' .. '\\u0039' | '\\u0660' .. '\\u0669' | '\\u06F0' .. '\\u06F9' | '\\u0966' .. '\\u096F' | '\\u09E6' .. '\\u09EF' | '\\u0A66' .. '\\u0A6F' | '\\u0AE6' .. '\\u0AEF' | '\\u0B66' .. '\\u0B6F' | '\\u0BE7' .. '\\u0BEF' | '\\u0C66' .. '\\u0C6F' | '\\u0CE6' .. '\\u0CEF' | '\\u0D66' .. '\\u0D6F' | '\\u0E50' .. '\\u0E59' | '\\u0ED0' .. '\\u0ED9' | '\\u1040' .. '\\u1049' )
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:
            {
            if ( (input.LA(1)>='0' && input.LA(1)<='9')||(input.LA(1)>='A' && input.LA(1)<='Z')||(input.LA(1)>='a' && input.LA(1)<='z')||(input.LA(1)>='\u00C0' && input.LA(1)<='\u00D6')||(input.LA(1)>='\u00D8' && input.LA(1)<='\u00F6')||(input.LA(1)>='\u00F8' && input.LA(1)<='\u1FFF')||(input.LA(1)>='\u3040' && input.LA(1)<='\u318F')||(input.LA(1)>='\u3300' && input.LA(1)<='\u337F')||(input.LA(1)>='\u3400' && input.LA(1)<='\u3D2D')||(input.LA(1)>='\u4E00' && input.LA(1)<='\u9FFF')||(input.LA(1)>='\uAC00' && input.LA(1)<='\uD7AF')||(input.LA(1)>='\uF900' && input.LA(1)<='\uFAFF') ) {
                input.consume();
            state.failed=false;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "INWORD"

    // $ANTLR start "FLOATING_POINT_LITERAL"
    public final void mFLOATING_POINT_LITERAL() throws RecognitionException {
        try {
            int _type = FLOATING_POINT_LITERAL;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            Token d=null;
            Token r=null;

            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:663:4: (d= START_RANGE_I r= DOTDOT | d= START_RANGE_F r= DOTDOT | ( PLUS | MINUS )? ( DIGIT )+ DOT ( DIGIT )* ( EXPONENT )? | ( PLUS | MINUS )? DOT ( DIGIT )+ ( EXPONENT )? | ( PLUS | MINUS )? ( DIGIT )+ EXPONENT )
            int alt26=5;
            alt26 = dfa26.predict(input);
            switch (alt26) {
                case 1 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:663:7: d= START_RANGE_I r= DOTDOT
                    {
                    int dStart1326 = getCharIndex();
                    mSTART_RANGE_I(); if (state.failed) return ;
                    d = new CommonToken(input, Token.INVALID_TOKEN_TYPE, Token.DEFAULT_CHANNEL, dStart1326, getCharIndex()-1);
                    int rStart1330 = getCharIndex();
                    mDOTDOT(); if (state.failed) return ;
                    r = new CommonToken(input, Token.INVALID_TOKEN_TYPE, Token.DEFAULT_CHANNEL, rStart1330, getCharIndex()-1);
                    if ( state.backtracking==0 ) {

                            			d.setType(DECIMAL_INTEGER_LITERAL);
                            			emit(d);
                            			r.setType(DOTDOT);
                            			emit(r);
                          		
                    }

                    }
                    break;
                case 2 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:671:7: d= START_RANGE_F r= DOTDOT
                    {
                    int dStart1355 = getCharIndex();
                    mSTART_RANGE_F(); if (state.failed) return ;
                    d = new CommonToken(input, Token.INVALID_TOKEN_TYPE, Token.DEFAULT_CHANNEL, dStart1355, getCharIndex()-1);
                    int rStart1359 = getCharIndex();
                    mDOTDOT(); if (state.failed) return ;
                    r = new CommonToken(input, Token.INVALID_TOKEN_TYPE, Token.DEFAULT_CHANNEL, rStart1359, getCharIndex()-1);
                    if ( state.backtracking==0 ) {

                            			d.setType(FLOATING_POINT_LITERAL);
                            			emit(d);
                            			r.setType(DOTDOT);
                            			emit(r);
                          		
                    }

                    }
                    break;
                case 3 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:679:7: ( PLUS | MINUS )? ( DIGIT )+ DOT ( DIGIT )* ( EXPONENT )?
                    {
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:679:7: ( PLUS | MINUS )?
                    int alt17=2;
                    int LA17_0 = input.LA(1);

                    if ( (LA17_0=='+'||LA17_0=='-') ) {
                        alt17=1;
                    }
                    switch (alt17) {
                        case 1 :
                            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:
                            {
                            if ( input.LA(1)=='+'||input.LA(1)=='-' ) {
                                input.consume();
                            state.failed=false;
                            }
                            else {
                                if (state.backtracking>0) {state.failed=true; return ;}
                                MismatchedSetException mse = new MismatchedSetException(null,input);
                                recover(mse);
                                throw mse;}


                            }
                            break;

                    }

                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:679:23: ( DIGIT )+
                    int cnt18=0;
                    loop18:
                    do {
                        int alt18=2;
                        int LA18_0 = input.LA(1);

                        if ( ((LA18_0>='0' && LA18_0<='9')) ) {
                            alt18=1;
                        }


                        switch (alt18) {
                    	case 1 :
                    	    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:679:23: DIGIT
                    	    {
                    	    mDIGIT(); if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    if ( cnt18 >= 1 ) break loop18;
                    	    if (state.backtracking>0) {state.failed=true; return ;}
                                EarlyExitException eee =
                                    new EarlyExitException(18, input);
                                throw eee;
                        }
                        cnt18++;
                    } while (true);

                    mDOT(); if (state.failed) return ;
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:679:34: ( DIGIT )*
                    loop19:
                    do {
                        int alt19=2;
                        int LA19_0 = input.LA(1);

                        if ( ((LA19_0>='0' && LA19_0<='9')) ) {
                            alt19=1;
                        }


                        switch (alt19) {
                    	case 1 :
                    	    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:679:34: DIGIT
                    	    {
                    	    mDIGIT(); if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop19;
                        }
                    } while (true);

                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:679:41: ( EXPONENT )?
                    int alt20=2;
                    int LA20_0 = input.LA(1);

                    if ( (LA20_0=='E'||LA20_0=='e') ) {
                        alt20=1;
                    }
                    switch (alt20) {
                        case 1 :
                            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:679:41: EXPONENT
                            {
                            mEXPONENT(); if (state.failed) return ;

                            }
                            break;

                    }


                    }
                    break;
                case 4 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:680:7: ( PLUS | MINUS )? DOT ( DIGIT )+ ( EXPONENT )?
                    {
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:680:7: ( PLUS | MINUS )?
                    int alt21=2;
                    int LA21_0 = input.LA(1);

                    if ( (LA21_0=='+'||LA21_0=='-') ) {
                        alt21=1;
                    }
                    switch (alt21) {
                        case 1 :
                            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:
                            {
                            if ( input.LA(1)=='+'||input.LA(1)=='-' ) {
                                input.consume();
                            state.failed=false;
                            }
                            else {
                                if (state.backtracking>0) {state.failed=true; return ;}
                                MismatchedSetException mse = new MismatchedSetException(null,input);
                                recover(mse);
                                throw mse;}


                            }
                            break;

                    }

                    mDOT(); if (state.failed) return ;
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:680:27: ( DIGIT )+
                    int cnt22=0;
                    loop22:
                    do {
                        int alt22=2;
                        int LA22_0 = input.LA(1);

                        if ( ((LA22_0>='0' && LA22_0<='9')) ) {
                            alt22=1;
                        }


                        switch (alt22) {
                    	case 1 :
                    	    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:680:27: DIGIT
                    	    {
                    	    mDIGIT(); if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    if ( cnt22 >= 1 ) break loop22;
                    	    if (state.backtracking>0) {state.failed=true; return ;}
                                EarlyExitException eee =
                                    new EarlyExitException(22, input);
                                throw eee;
                        }
                        cnt22++;
                    } while (true);

                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:680:34: ( EXPONENT )?
                    int alt23=2;
                    int LA23_0 = input.LA(1);

                    if ( (LA23_0=='E'||LA23_0=='e') ) {
                        alt23=1;
                    }
                    switch (alt23) {
                        case 1 :
                            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:680:34: EXPONENT
                            {
                            mEXPONENT(); if (state.failed) return ;

                            }
                            break;

                    }


                    }
                    break;
                case 5 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:681:7: ( PLUS | MINUS )? ( DIGIT )+ EXPONENT
                    {
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:681:7: ( PLUS | MINUS )?
                    int alt24=2;
                    int LA24_0 = input.LA(1);

                    if ( (LA24_0=='+'||LA24_0=='-') ) {
                        alt24=1;
                    }
                    switch (alt24) {
                        case 1 :
                            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:
                            {
                            if ( input.LA(1)=='+'||input.LA(1)=='-' ) {
                                input.consume();
                            state.failed=false;
                            }
                            else {
                                if (state.backtracking>0) {state.failed=true; return ;}
                                MismatchedSetException mse = new MismatchedSetException(null,input);
                                recover(mse);
                                throw mse;}


                            }
                            break;

                    }

                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:681:23: ( DIGIT )+
                    int cnt25=0;
                    loop25:
                    do {
                        int alt25=2;
                        int LA25_0 = input.LA(1);

                        if ( ((LA25_0>='0' && LA25_0<='9')) ) {
                            alt25=1;
                        }


                        switch (alt25) {
                    	case 1 :
                    	    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:681:23: DIGIT
                    	    {
                    	    mDIGIT(); if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    if ( cnt25 >= 1 ) break loop25;
                    	    if (state.backtracking>0) {state.failed=true; return ;}
                                EarlyExitException eee =
                                    new EarlyExitException(25, input);
                                throw eee;
                        }
                        cnt25++;
                    } while (true);

                    mEXPONENT(); if (state.failed) return ;

                    }
                    break;

            }
            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "FLOATING_POINT_LITERAL"

    // $ANTLR start "START_RANGE_I"
    public final void mSTART_RANGE_I() throws RecognitionException {
        try {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:686:2: ( ( PLUS | MINUS )? ( DIGIT )+ )
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:686:5: ( PLUS | MINUS )? ( DIGIT )+
            {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:686:5: ( PLUS | MINUS )?
            int alt27=2;
            int LA27_0 = input.LA(1);

            if ( (LA27_0=='+'||LA27_0=='-') ) {
                alt27=1;
            }
            switch (alt27) {
                case 1 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:
                    {
                    if ( input.LA(1)=='+'||input.LA(1)=='-' ) {
                        input.consume();
                    state.failed=false;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;}


                    }
                    break;

            }

            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:686:21: ( DIGIT )+
            int cnt28=0;
            loop28:
            do {
                int alt28=2;
                int LA28_0 = input.LA(1);

                if ( ((LA28_0>='0' && LA28_0<='9')) ) {
                    alt28=1;
                }


                switch (alt28) {
            	case 1 :
            	    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:686:21: DIGIT
            	    {
            	    mDIGIT(); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    if ( cnt28 >= 1 ) break loop28;
            	    if (state.backtracking>0) {state.failed=true; return ;}
                        EarlyExitException eee =
                            new EarlyExitException(28, input);
                        throw eee;
                }
                cnt28++;
            } while (true);


            }

        }
        finally {
        }
    }
    // $ANTLR end "START_RANGE_I"

    // $ANTLR start "START_RANGE_F"
    public final void mSTART_RANGE_F() throws RecognitionException {
        try {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:691:2: ( ( PLUS | MINUS )? ( DIGIT )+ DOT )
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:691:5: ( PLUS | MINUS )? ( DIGIT )+ DOT
            {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:691:5: ( PLUS | MINUS )?
            int alt29=2;
            int LA29_0 = input.LA(1);

            if ( (LA29_0=='+'||LA29_0=='-') ) {
                alt29=1;
            }
            switch (alt29) {
                case 1 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:
                    {
                    if ( input.LA(1)=='+'||input.LA(1)=='-' ) {
                        input.consume();
                    state.failed=false;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;}


                    }
                    break;

            }

            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:691:21: ( DIGIT )+
            int cnt30=0;
            loop30:
            do {
                int alt30=2;
                int LA30_0 = input.LA(1);

                if ( ((LA30_0>='0' && LA30_0<='9')) ) {
                    alt30=1;
                }


                switch (alt30) {
            	case 1 :
            	    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:691:21: DIGIT
            	    {
            	    mDIGIT(); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    if ( cnt30 >= 1 ) break loop30;
            	    if (state.backtracking>0) {state.failed=true; return ;}
                        EarlyExitException eee =
                            new EarlyExitException(30, input);
                        throw eee;
                }
                cnt30++;
            } while (true);

            mDOT(); if (state.failed) return ;

            }

        }
        finally {
        }
    }
    // $ANTLR end "START_RANGE_F"

    // $ANTLR start "DECIMAL_NUMERAL"
    public final void mDECIMAL_NUMERAL() throws RecognitionException {
        try {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:700:4: ( ZERO_DIGIT | NON_ZERO_DIGIT ( DIGIT )* )
            int alt32=2;
            int LA32_0 = input.LA(1);

            if ( (LA32_0=='0') ) {
                alt32=1;
            }
            else if ( ((LA32_0>='1' && LA32_0<='9')) ) {
                alt32=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 32, 0, input);

                throw nvae;
            }
            switch (alt32) {
                case 1 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:700:7: ZERO_DIGIT
                    {
                    mZERO_DIGIT(); if (state.failed) return ;

                    }
                    break;
                case 2 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:701:7: NON_ZERO_DIGIT ( DIGIT )*
                    {
                    mNON_ZERO_DIGIT(); if (state.failed) return ;
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:701:22: ( DIGIT )*
                    loop31:
                    do {
                        int alt31=2;
                        int LA31_0 = input.LA(1);

                        if ( ((LA31_0>='0' && LA31_0<='9')) ) {
                            alt31=1;
                        }


                        switch (alt31) {
                    	case 1 :
                    	    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:701:22: DIGIT
                    	    {
                    	    mDIGIT(); if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop31;
                        }
                    } while (true);


                    }
                    break;

            }
        }
        finally {
        }
    }
    // $ANTLR end "DECIMAL_NUMERAL"

    // $ANTLR start "DIGIT"
    public final void mDIGIT() throws RecognitionException {
        try {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:706:2: ( ZERO_DIGIT | NON_ZERO_DIGIT )
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:
            {
            if ( (input.LA(1)>='0' && input.LA(1)<='9') ) {
                input.consume();
            state.failed=false;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "DIGIT"

    // $ANTLR start "ZERO_DIGIT"
    public final void mZERO_DIGIT() throws RecognitionException {
        try {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:712:4: ( '0' )
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:712:7: '0'
            {
            match('0'); if (state.failed) return ;

            }

        }
        finally {
        }
    }
    // $ANTLR end "ZERO_DIGIT"

    // $ANTLR start "NON_ZERO_DIGIT"
    public final void mNON_ZERO_DIGIT() throws RecognitionException {
        try {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:716:4: ( '1' .. '9' )
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:716:7: '1' .. '9'
            {
            matchRange('1','9'); if (state.failed) return ;

            }

        }
        finally {
        }
    }
    // $ANTLR end "NON_ZERO_DIGIT"

    // $ANTLR start "E"
    public final void mE() throws RecognitionException {
        try {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:720:4: ( ( 'e' | 'E' ) )
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:720:7: ( 'e' | 'E' )
            {
            if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
                input.consume();
            state.failed=false;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "E"

    // $ANTLR start "EXPONENT"
    public final void mEXPONENT() throws RecognitionException {
        try {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:724:4: ( E SIGNED_INTEGER )
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:724:7: E SIGNED_INTEGER
            {
            mE(); if (state.failed) return ;
            mSIGNED_INTEGER(); if (state.failed) return ;

            }

        }
        finally {
        }
    }
    // $ANTLR end "EXPONENT"

    // $ANTLR start "SIGNED_INTEGER"
    public final void mSIGNED_INTEGER() throws RecognitionException {
        try {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:729:4: ( ( PLUS | MINUS )? ( DIGIT )+ )
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:729:7: ( PLUS | MINUS )? ( DIGIT )+
            {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:729:7: ( PLUS | MINUS )?
            int alt33=2;
            int LA33_0 = input.LA(1);

            if ( (LA33_0=='+'||LA33_0=='-') ) {
                alt33=1;
            }
            switch (alt33) {
                case 1 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:
                    {
                    if ( input.LA(1)=='+'||input.LA(1)=='-' ) {
                        input.consume();
                    state.failed=false;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;}


                    }
                    break;

            }

            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:729:23: ( DIGIT )+
            int cnt34=0;
            loop34:
            do {
                int alt34=2;
                int LA34_0 = input.LA(1);

                if ( ((LA34_0>='0' && LA34_0<='9')) ) {
                    alt34=1;
                }


                switch (alt34) {
            	case 1 :
            	    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:729:23: DIGIT
            	    {
            	    mDIGIT(); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    if ( cnt34 >= 1 ) break loop34;
            	    if (state.backtracking>0) {state.failed=true; return ;}
                        EarlyExitException eee =
                            new EarlyExitException(34, input);
                        throw eee;
                }
                cnt34++;
            } while (true);


            }

        }
        finally {
        }
    }
    // $ANTLR end "SIGNED_INTEGER"

    // $ANTLR start "WS"
    public final void mWS() throws RecognitionException {
        try {
            int _type = WS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:736:4: ( ( ' ' | '\\t' | '\\r' | '\\n' )+ )
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:736:6: ( ' ' | '\\t' | '\\r' | '\\n' )+
            {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:736:6: ( ' ' | '\\t' | '\\r' | '\\n' )+
            int cnt35=0;
            loop35:
            do {
                int alt35=2;
                int LA35_0 = input.LA(1);

                if ( ((LA35_0>='\t' && LA35_0<='\n')||LA35_0=='\r'||LA35_0==' ') ) {
                    alt35=1;
                }


                switch (alt35) {
            	case 1 :
            	    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:
            	    {
            	    if ( (input.LA(1)>='\t' && input.LA(1)<='\n')||input.LA(1)=='\r'||input.LA(1)==' ' ) {
            	        input.consume();
            	    state.failed=false;
            	    }
            	    else {
            	        if (state.backtracking>0) {state.failed=true; return ;}
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;}


            	    }
            	    break;

            	default :
            	    if ( cnt35 >= 1 ) break loop35;
            	    if (state.backtracking>0) {state.failed=true; return ;}
                        EarlyExitException eee =
                            new EarlyExitException(35, input);
                        throw eee;
                }
                cnt35++;
            } while (true);

            if ( state.backtracking==0 ) {
               _channel = HIDDEN; 
            }

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "WS"

    public void mTokens() throws RecognitionException {
        // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:1:8: ( FTSPHRASE | URI | OR | AND | NOT | TILDA | LPAREN | RPAREN | PLUS | MINUS | COLON | STAR | DOTDOT | DOT | AMP | EXCLAMATION | BAR | EQUALS | QUESTION_MARK | LCURL | RCURL | LSQUARE | RSQUARE | TO | COMMA | CARAT | DOLLAR | GT | LT | AT | PERCENT | ID | DECIMAL_INTEGER_LITERAL | FTSWORD | FTSPRE | FTSWILD | FLOATING_POINT_LITERAL | WS )
        int alt36=38;
        alt36 = dfa36.predict(input);
        switch (alt36) {
            case 1 :
                // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:1:10: FTSPHRASE
                {
                mFTSPHRASE(); if (state.failed) return ;

                }
                break;
            case 2 :
                // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:1:20: URI
                {
                mURI(); if (state.failed) return ;

                }
                break;
            case 3 :
                // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:1:24: OR
                {
                mOR(); if (state.failed) return ;

                }
                break;
            case 4 :
                // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:1:27: AND
                {
                mAND(); if (state.failed) return ;

                }
                break;
            case 5 :
                // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:1:31: NOT
                {
                mNOT(); if (state.failed) return ;

                }
                break;
            case 6 :
                // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:1:35: TILDA
                {
                mTILDA(); if (state.failed) return ;

                }
                break;
            case 7 :
                // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:1:41: LPAREN
                {
                mLPAREN(); if (state.failed) return ;

                }
                break;
            case 8 :
                // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:1:48: RPAREN
                {
                mRPAREN(); if (state.failed) return ;

                }
                break;
            case 9 :
                // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:1:55: PLUS
                {
                mPLUS(); if (state.failed) return ;

                }
                break;
            case 10 :
                // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:1:60: MINUS
                {
                mMINUS(); if (state.failed) return ;

                }
                break;
            case 11 :
                // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:1:66: COLON
                {
                mCOLON(); if (state.failed) return ;

                }
                break;
            case 12 :
                // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:1:72: STAR
                {
                mSTAR(); if (state.failed) return ;

                }
                break;
            case 13 :
                // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:1:77: DOTDOT
                {
                mDOTDOT(); if (state.failed) return ;

                }
                break;
            case 14 :
                // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:1:84: DOT
                {
                mDOT(); if (state.failed) return ;

                }
                break;
            case 15 :
                // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:1:88: AMP
                {
                mAMP(); if (state.failed) return ;

                }
                break;
            case 16 :
                // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:1:92: EXCLAMATION
                {
                mEXCLAMATION(); if (state.failed) return ;

                }
                break;
            case 17 :
                // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:1:104: BAR
                {
                mBAR(); if (state.failed) return ;

                }
                break;
            case 18 :
                // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:1:108: EQUALS
                {
                mEQUALS(); if (state.failed) return ;

                }
                break;
            case 19 :
                // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:1:115: QUESTION_MARK
                {
                mQUESTION_MARK(); if (state.failed) return ;

                }
                break;
            case 20 :
                // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:1:129: LCURL
                {
                mLCURL(); if (state.failed) return ;

                }
                break;
            case 21 :
                // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:1:135: RCURL
                {
                mRCURL(); if (state.failed) return ;

                }
                break;
            case 22 :
                // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:1:141: LSQUARE
                {
                mLSQUARE(); if (state.failed) return ;

                }
                break;
            case 23 :
                // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:1:149: RSQUARE
                {
                mRSQUARE(); if (state.failed) return ;

                }
                break;
            case 24 :
                // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:1:157: TO
                {
                mTO(); if (state.failed) return ;

                }
                break;
            case 25 :
                // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:1:160: COMMA
                {
                mCOMMA(); if (state.failed) return ;

                }
                break;
            case 26 :
                // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:1:166: CARAT
                {
                mCARAT(); if (state.failed) return ;

                }
                break;
            case 27 :
                // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:1:172: DOLLAR
                {
                mDOLLAR(); if (state.failed) return ;

                }
                break;
            case 28 :
                // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:1:179: GT
                {
                mGT(); if (state.failed) return ;

                }
                break;
            case 29 :
                // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:1:182: LT
                {
                mLT(); if (state.failed) return ;

                }
                break;
            case 30 :
                // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:1:185: AT
                {
                mAT(); if (state.failed) return ;

                }
                break;
            case 31 :
                // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:1:188: PERCENT
                {
                mPERCENT(); if (state.failed) return ;

                }
                break;
            case 32 :
                // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:1:196: ID
                {
                mID(); if (state.failed) return ;

                }
                break;
            case 33 :
                // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:1:199: DECIMAL_INTEGER_LITERAL
                {
                mDECIMAL_INTEGER_LITERAL(); if (state.failed) return ;

                }
                break;
            case 34 :
                // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:1:223: FTSWORD
                {
                mFTSWORD(); if (state.failed) return ;

                }
                break;
            case 35 :
                // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:1:231: FTSPRE
                {
                mFTSPRE(); if (state.failed) return ;

                }
                break;
            case 36 :
                // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:1:238: FTSWILD
                {
                mFTSWILD(); if (state.failed) return ;

                }
                break;
            case 37 :
                // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:1:246: FLOATING_POINT_LITERAL
                {
                mFLOATING_POINT_LITERAL(); if (state.failed) return ;

                }
                break;
            case 38 :
                // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:1:269: WS
                {
                mWS(); if (state.failed) return ;

                }
                break;

        }

    }

    // $ANTLR start synpred1_FTS
    public final void synpred1_FTS_fragment() throws RecognitionException {   
        // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:519:11: ( F_URI_ALPHA | F_URI_DIGIT | F_URI_OTHER )
        // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:
        {
        if ( input.LA(1)=='!'||input.LA(1)=='$'||(input.LA(1)>='&' && input.LA(1)<='.')||(input.LA(1)>='0' && input.LA(1)<='9')||input.LA(1)==';'||input.LA(1)=='='||(input.LA(1)>='@' && input.LA(1)<='[')||input.LA(1)==']'||input.LA(1)=='_'||(input.LA(1)>='a' && input.LA(1)<='z')||input.LA(1)=='~' ) {
            input.consume();
        state.failed=false;
        }
        else {
            if (state.backtracking>0) {state.failed=true; return ;}
            MismatchedSetException mse = new MismatchedSetException(null,input);
            recover(mse);
            throw mse;}


        }
    }
    // $ANTLR end synpred1_FTS

    // $ANTLR start synpred2_FTS
    public final void synpred2_FTS_fragment() throws RecognitionException {   
        // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:520:12: ( '//' )
        // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:520:13: '//'
        {
        match("//"); if (state.failed) return ;


        }
    }
    // $ANTLR end synpred2_FTS

    // $ANTLR start synpred3_FTS
    public final void synpred3_FTS_fragment() throws RecognitionException {   
        // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:521:12: ( F_URI_ALPHA | F_URI_DIGIT | F_URI_OTHER | COLON )
        // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:
        {
        if ( input.LA(1)=='!'||input.LA(1)=='$'||(input.LA(1)>='&' && input.LA(1)<='.')||(input.LA(1)>='0' && input.LA(1)<=';')||input.LA(1)=='='||(input.LA(1)>='@' && input.LA(1)<='[')||input.LA(1)==']'||input.LA(1)=='_'||(input.LA(1)>='a' && input.LA(1)<='z')||input.LA(1)=='~' ) {
            input.consume();
        state.failed=false;
        }
        else {
            if (state.backtracking>0) {state.failed=true; return ;}
            MismatchedSetException mse = new MismatchedSetException(null,input);
            recover(mse);
            throw mse;}


        }
    }
    // $ANTLR end synpred3_FTS

    public final boolean synpred1_FTS() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred1_FTS_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred2_FTS() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred2_FTS_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred3_FTS() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred3_FTS_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }


    protected DFA3 dfa3 = new DFA3(this);
    protected DFA26 dfa26 = new DFA26(this);
    protected DFA36 dfa36 = new DFA36(this);
    static final String DFA3_eotS =
        "\5\uffff";
    static final String DFA3_eofS =
        "\5\uffff";
    static final String DFA3_minS =
        "\2\41\1\uffff\1\0\1\uffff";
    static final String DFA3_maxS =
        "\2\176\1\uffff\1\0\1\uffff";
    static final String DFA3_acceptS =
        "\2\uffff\1\2\1\uffff\1\1";
    static final String DFA3_specialS =
        "\3\uffff\1\0\1\uffff}>";
    static final String[] DFA3_transitionS = {
            "\1\1\1\uffff\1\2\1\1\1\uffff\11\1\1\2\12\1\1\2\1\1\1\uffff"+
            "\1\1\1\uffff\1\2\34\1\1\uffff\1\1\1\uffff\1\1\1\uffff\32\1\2"+
            "\uffff\1\2\1\1",
            "\1\1\1\uffff\1\2\1\1\1\uffff\11\1\1\2\12\1\1\3\1\1\1\uffff"+
            "\1\1\1\uffff\1\2\34\1\1\uffff\1\1\1\uffff\1\1\1\uffff\32\1\2"+
            "\uffff\1\2\1\1",
            "",
            "\1\uffff",
            ""
    };

    static final short[] DFA3_eot = DFA.unpackEncodedString(DFA3_eotS);
    static final short[] DFA3_eof = DFA.unpackEncodedString(DFA3_eofS);
    static final char[] DFA3_min = DFA.unpackEncodedStringToUnsignedChars(DFA3_minS);
    static final char[] DFA3_max = DFA.unpackEncodedStringToUnsignedChars(DFA3_maxS);
    static final short[] DFA3_accept = DFA.unpackEncodedString(DFA3_acceptS);
    static final short[] DFA3_special = DFA.unpackEncodedString(DFA3_specialS);
    static final short[][] DFA3_transition;

    static {
        int numStates = DFA3_transitionS.length;
        DFA3_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA3_transition[i] = DFA.unpackEncodedString(DFA3_transitionS[i]);
        }
    }

    class DFA3 extends DFA {

        public DFA3(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 3;
            this.eot = DFA3_eot;
            this.eof = DFA3_eof;
            this.min = DFA3_min;
            this.max = DFA3_max;
            this.accept = DFA3_accept;
            this.special = DFA3_special;
            this.transition = DFA3_transition;
        }
        public String getDescription() {
            return "519:10: ( ( F_URI_ALPHA | F_URI_DIGIT | F_URI_OTHER )=> ( F_URI_ALPHA | F_URI_DIGIT | F_URI_OTHER )+ COLON )?";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            IntStream input = _input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA3_3 = input.LA(1);

                         
                        int index3_3 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_FTS()) ) {s = 4;}

                        else if ( (true) ) {s = 2;}

                         
                        input.seek(index3_3);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 3, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA26_eotS =
        "\4\uffff\1\7\1\uffff\1\11\3\uffff";
    static final String DFA26_eofS =
        "\12\uffff";
    static final String DFA26_minS =
        "\1\53\2\56\1\uffff\1\56\1\uffff\1\56\3\uffff";
    static final String DFA26_maxS =
        "\2\71\1\145\1\uffff\1\56\1\uffff\1\56\3\uffff";
    static final String DFA26_acceptS =
        "\3\uffff\1\4\1\uffff\1\5\1\uffff\1\3\1\2\1\1";
    static final String DFA26_specialS =
        "\12\uffff}>";
    static final String[] DFA26_transitionS = {
            "\1\1\1\uffff\1\1\1\3\1\uffff\12\2",
            "\1\3\1\uffff\12\2",
            "\1\4\1\uffff\12\2\13\uffff\1\5\37\uffff\1\5",
            "",
            "\1\6",
            "",
            "\1\10",
            "",
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
            return "661:1: FLOATING_POINT_LITERAL : (d= START_RANGE_I r= DOTDOT | d= START_RANGE_F r= DOTDOT | ( PLUS | MINUS )? ( DIGIT )+ DOT ( DIGIT )* ( EXPONENT )? | ( PLUS | MINUS )? DOT ( DIGIT )+ ( EXPONENT )? | ( PLUS | MINUS )? ( DIGIT )+ EXPONENT );";
        }
    }
    static final String DFA36_eotS =
        "\2\uffff\1\45\3\42\3\uffff\1\64\1\67\1\uffff\1\70\1\72\4\uffff"+
        "\1\73\3\uffff\1\42\7\uffff\1\42\2\76\2\uffff\1\104\3\uffff\2\105"+
        "\3\42\1\uffff\1\110\1\uffff\4\42\1\76\1\uffff\1\76\6\uffff\2\116"+
        "\1\uffff\2\104\1\76\2\104\2\uffff\2\42\1\uffff\2\124\2\125\1\76"+
        "\1\uffff\2\104\3\42\2\uffff\1\104\3\42\1\104\3\42\1\104\3\42";
    static final String DFA36_eofS =
        "\142\uffff";
    static final String DFA36_minS =
        "\1\11\1\uffff\1\41\3\52\3\uffff\2\56\1\uffff\1\52\1\56\4\uffff"+
        "\1\52\3\uffff\1\52\7\uffff\3\52\1\0\1\uffff\1\52\3\uffff\2\43\3"+
        "\52\1\0\1\52\1\uffff\4\52\1\56\1\uffff\1\56\6\uffff\2\43\1\uffff"+
        "\5\52\2\uffff\2\52\1\uffff\4\43\1\56\1\uffff\5\52\2\uffff\14\52";
    static final String DFA36_maxS =
        "\1\ufaff\1\uffff\1\176\3\ufaff\3\uffff\2\71\1\uffff\1\ufaff\1\71"+
        "\4\uffff\1\ufaff\3\uffff\1\ufaff\7\uffff\3\ufaff\1\uffff\1\uffff"+
        "\1\ufaff\3\uffff\5\ufaff\1\uffff\1\ufaff\1\uffff\4\ufaff\1\145\1"+
        "\uffff\1\145\6\uffff\2\ufaff\1\uffff\5\ufaff\2\uffff\2\ufaff\1\uffff"+
        "\4\ufaff\1\145\1\uffff\5\ufaff\2\uffff\14\ufaff";
    static final String DFA36_acceptS =
        "\1\uffff\1\1\4\uffff\1\6\1\7\1\10\2\uffff\1\13\2\uffff\1\17\1\20"+
        "\1\21\1\22\1\uffff\1\25\1\26\1\27\1\uffff\1\31\1\32\1\33\1\34\1"+
        "\35\1\36\1\37\4\uffff\1\40\1\uffff\1\46\1\24\1\2\7\uffff\1\44\5"+
        "\uffff\1\11\1\uffff\1\45\1\12\1\14\1\15\1\16\1\23\2\uffff\1\41\5"+
        "\uffff\1\42\1\3\2\uffff\1\43\5\uffff\1\30\5\uffff\1\4\1\5\14\uffff";
    static final String DFA36_specialS =
        "\41\uffff\1\1\12\uffff\1\0\65\uffff}>";
    static final String[] DFA36_transitionS = {
            "\2\44\2\uffff\1\44\22\uffff\1\44\1\17\1\1\1\uffff\1\31\1\35"+
            "\1\16\1\uffff\1\7\1\10\1\14\1\11\1\27\1\12\1\15\1\uffff\1\37"+
            "\11\40\1\13\1\uffff\1\33\1\21\1\32\1\22\1\34\1\4\14\36\1\5\1"+
            "\3\4\36\1\26\6\36\1\24\1\41\1\25\1\30\1\42\1\uffff\1\4\14\36"+
            "\1\5\1\3\4\36\1\26\6\36\1\2\1\20\1\23\1\6\101\uffff\27\43\1"+
            "\uffff\37\43\1\uffff\u1f08\43\u1040\uffff\u0150\43\u0170\uffff"+
            "\u0080\43\u0080\uffff\u092e\43\u10d2\uffff\u5200\43\u0c00\uffff"+
            "\u2bb0\43\u2150\uffff\u0200\43",
            "",
            "\1\46\1\uffff\2\46\1\uffff\26\46\1\uffff\1\46\1\uffff\35\46"+
            "\1\uffff\1\46\1\uffff\1\46\1\uffff\32\46\2\uffff\2\46",
            "\1\55\5\uffff\12\53\5\uffff\1\56\1\uffff\21\52\1\50\10\52"+
            "\1\uffff\1\54\4\uffff\21\51\1\47\10\51\105\uffff\27\43\1\uffff"+
            "\37\43\1\uffff\u1f08\43\u1040\uffff\u0150\43\u0170\uffff\u0080"+
            "\43\u0080\uffff\u092e\43\u10d2\uffff\u5200\43\u0c00\uffff\u2bb0"+
            "\43\u2150\uffff\u0200\43",
            "\1\55\5\uffff\12\53\5\uffff\1\56\1\uffff\15\52\1\60\14\52"+
            "\1\uffff\1\54\4\uffff\15\51\1\57\14\51\105\uffff\27\43\1\uffff"+
            "\37\43\1\uffff\u1f08\43\u1040\uffff\u0150\43\u0170\uffff\u0080"+
            "\43\u0080\uffff\u092e\43\u10d2\uffff\u5200\43\u0c00\uffff\u2bb0"+
            "\43\u2150\uffff\u0200\43",
            "\1\55\5\uffff\12\53\5\uffff\1\56\1\uffff\16\52\1\62\13\52"+
            "\1\uffff\1\54\4\uffff\16\51\1\61\13\51\105\uffff\27\43\1\uffff"+
            "\37\43\1\uffff\u1f08\43\u1040\uffff\u0150\43\u0170\uffff\u0080"+
            "\43\u0080\uffff\u092e\43\u10d2\uffff\u5200\43\u0c00\uffff\u2bb0"+
            "\43\u2150\uffff\u0200\43",
            "",
            "",
            "",
            "\1\66\1\uffff\1\63\11\65",
            "\1\66\1\uffff\1\63\11\65",
            "",
            "\1\56\5\uffff\12\56\5\uffff\1\56\1\uffff\32\56\1\uffff\1\56"+
            "\4\uffff\32\56\105\uffff\27\56\1\uffff\37\56\1\uffff\u1f08\56"+
            "\u1040\uffff\u0150\56\u0170\uffff\u0080\56\u0080\uffff\u092e"+
            "\56\u10d2\uffff\u5200\56\u0c00\uffff\u2bb0\56\u2150\uffff\u0200"+
            "\56",
            "\1\71\1\uffff\12\66",
            "",
            "",
            "",
            "",
            "\1\56\5\uffff\12\56\5\uffff\1\56\1\uffff\32\56\1\uffff\1\56"+
            "\4\uffff\32\56\105\uffff\27\56\1\uffff\37\56\1\uffff\u1f08\56"+
            "\u1040\uffff\u0150\56\u0170\uffff\u0080\56\u0080\uffff\u092e"+
            "\56\u10d2\uffff\u5200\56\u0c00\uffff\u2bb0\56\u2150\uffff\u0200"+
            "\56",
            "",
            "",
            "",
            "\1\55\5\uffff\12\53\5\uffff\1\56\1\uffff\16\52\1\75\13\52"+
            "\1\uffff\1\54\4\uffff\16\51\1\74\13\51\105\uffff\27\43\1\uffff"+
            "\37\43\1\uffff\u1f08\43\u1040\uffff\u0150\43\u0170\uffff\u0080"+
            "\43\u0080\uffff\u092e\43\u10d2\uffff\u5200\43\u0c00\uffff\u2bb0"+
            "\43\u2150\uffff\u0200\43",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\55\5\uffff\12\53\5\uffff\1\56\1\uffff\32\52\1\uffff\1\54"+
            "\4\uffff\32\51\105\uffff\27\43\1\uffff\37\43\1\uffff\u1f08\43"+
            "\u1040\uffff\u0150\43\u0170\uffff\u0080\43\u0080\uffff\u092e"+
            "\43\u10d2\uffff\u5200\43\u0c00\uffff\u2bb0\43\u2150\uffff\u0200"+
            "\43",
            "\1\55\3\uffff\1\66\1\uffff\12\100\5\uffff\1\56\1\uffff\4\43"+
            "\1\77\25\43\1\uffff\1\41\4\uffff\4\43\1\77\25\43\105\uffff\27"+
            "\43\1\uffff\37\43\1\uffff\u1f08\43\u1040\uffff\u0150\43\u0170"+
            "\uffff\u0080\43\u0080\uffff\u092e\43\u10d2\uffff\u5200\43\u0c00"+
            "\uffff\u2bb0\43\u2150\uffff\u0200\43",
            "\1\55\3\uffff\1\66\1\uffff\12\101\5\uffff\1\56\1\uffff\4\43"+
            "\1\77\25\43\1\uffff\1\41\4\uffff\4\43\1\77\25\43\105\uffff\27"+
            "\43\1\uffff\37\43\1\uffff\u1f08\43\u1040\uffff\u0150\43\u0170"+
            "\uffff\u0080\43\u0080\uffff\u092e\43\u10d2\uffff\u5200\43\u0c00"+
            "\uffff\u2bb0\43\u2150\uffff\u0200\43",
            "\165\103\1\102\uff8a\103",
            "",
            "\1\55\5\uffff\12\43\5\uffff\1\56\1\uffff\32\43\1\uffff\1\41"+
            "\4\uffff\32\43\105\uffff\27\43\1\uffff\37\43\1\uffff\u1f08\43"+
            "\u1040\uffff\u0150\43\u0170\uffff\u0080\43\u0080\uffff\u092e"+
            "\43\u10d2\uffff\u5200\43\u0c00\uffff\u2bb0\43\u2150\uffff\u0200"+
            "\43",
            "",
            "",
            "",
            "\2\42\5\uffff\1\55\5\uffff\12\53\5\uffff\1\56\1\uffff\32\52"+
            "\1\uffff\1\54\2\uffff\1\42\1\uffff\32\51\105\uffff\27\43\1\uffff"+
            "\37\43\1\uffff\u1f08\43\u1040\uffff\u0150\43\u0170\uffff\u0080"+
            "\43\u0080\uffff\u092e\43\u10d2\uffff\u5200\43\u0c00\uffff\u2bb0"+
            "\43\u2150\uffff\u0200\43",
            "\2\42\5\uffff\1\55\5\uffff\12\53\5\uffff\1\56\1\uffff\32\52"+
            "\1\uffff\1\54\2\uffff\1\42\1\uffff\32\51\105\uffff\27\43\1\uffff"+
            "\37\43\1\uffff\u1f08\43\u1040\uffff\u0150\43\u0170\uffff\u0080"+
            "\43\u0080\uffff\u092e\43\u10d2\uffff\u5200\43\u0c00\uffff\u2bb0"+
            "\43\u2150\uffff\u0200\43",
            "\1\55\5\uffff\12\53\5\uffff\1\56\1\uffff\32\52\1\uffff\1\54"+
            "\4\uffff\32\51\105\uffff\27\43\1\uffff\37\43\1\uffff\u1f08\43"+
            "\u1040\uffff\u0150\43\u0170\uffff\u0080\43\u0080\uffff\u092e"+
            "\43\u10d2\uffff\u5200\43\u0c00\uffff\u2bb0\43\u2150\uffff\u0200"+
            "\43",
            "\1\55\5\uffff\12\53\5\uffff\1\56\1\uffff\32\52\1\uffff\1\54"+
            "\4\uffff\32\51\105\uffff\27\43\1\uffff\37\43\1\uffff\u1f08\43"+
            "\u1040\uffff\u0150\43\u0170\uffff\u0080\43\u0080\uffff\u092e"+
            "\43\u10d2\uffff\u5200\43\u0c00\uffff\u2bb0\43\u2150\uffff\u0200"+
            "\43",
            "\1\55\5\uffff\12\53\5\uffff\1\56\1\uffff\32\52\1\uffff\1\54"+
            "\4\uffff\32\51\105\uffff\27\43\1\uffff\37\43\1\uffff\u1f08\43"+
            "\u1040\uffff\u0150\43\u0170\uffff\u0080\43\u0080\uffff\u092e"+
            "\43\u10d2\uffff\u5200\43\u0c00\uffff\u2bb0\43\u2150\uffff\u0200"+
            "\43",
            "\165\107\1\106\uff8a\107",
            "\1\56\5\uffff\12\56\5\uffff\1\56\1\uffff\32\56\1\uffff\1\56"+
            "\4\uffff\32\56\105\uffff\27\56\1\uffff\37\56\1\uffff\u1f08\56"+
            "\u1040\uffff\u0150\56\u0170\uffff\u0080\56\u0080\uffff\u092e"+
            "\56\u10d2\uffff\u5200\56\u0c00\uffff\u2bb0\56\u2150\uffff\u0200"+
            "\56",
            "",
            "\1\55\5\uffff\12\53\5\uffff\1\56\1\uffff\3\52\1\112\26\52"+
            "\1\uffff\1\54\4\uffff\3\51\1\111\26\51\105\uffff\27\43\1\uffff"+
            "\37\43\1\uffff\u1f08\43\u1040\uffff\u0150\43\u0170\uffff\u0080"+
            "\43\u0080\uffff\u092e\43\u10d2\uffff\u5200\43\u0c00\uffff\u2bb0"+
            "\43\u2150\uffff\u0200\43",
            "\1\55\5\uffff\12\53\5\uffff\1\56\1\uffff\3\52\1\112\26\52"+
            "\1\uffff\1\54\4\uffff\3\51\1\111\26\51\105\uffff\27\43\1\uffff"+
            "\37\43\1\uffff\u1f08\43\u1040\uffff\u0150\43\u0170\uffff\u0080"+
            "\43\u0080\uffff\u092e\43\u10d2\uffff\u5200\43\u0c00\uffff\u2bb0"+
            "\43\u2150\uffff\u0200\43",
            "\1\55\5\uffff\12\53\5\uffff\1\56\1\uffff\23\52\1\114\6\52"+
            "\1\uffff\1\54\4\uffff\23\51\1\113\6\51\105\uffff\27\43\1\uffff"+
            "\37\43\1\uffff\u1f08\43\u1040\uffff\u0150\43\u0170\uffff\u0080"+
            "\43\u0080\uffff\u092e\43\u10d2\uffff\u5200\43\u0c00\uffff\u2bb0"+
            "\43\u2150\uffff\u0200\43",
            "\1\55\5\uffff\12\53\5\uffff\1\56\1\uffff\23\52\1\114\6\52"+
            "\1\uffff\1\54\4\uffff\23\51\1\113\6\51\105\uffff\27\43\1\uffff"+
            "\37\43\1\uffff\u1f08\43\u1040\uffff\u0150\43\u0170\uffff\u0080"+
            "\43\u0080\uffff\u092e\43\u10d2\uffff\u5200\43\u0c00\uffff\u2bb0"+
            "\43\u2150\uffff\u0200\43",
            "\1\66\1\uffff\12\66\13\uffff\1\66\37\uffff\1\66",
            "",
            "\1\66\1\uffff\12\115\13\uffff\1\66\37\uffff\1\66",
            "",
            "",
            "",
            "",
            "",
            "",
            "\2\42\5\uffff\1\55\5\uffff\12\53\5\uffff\1\56\1\uffff\32\52"+
            "\1\uffff\1\54\2\uffff\1\42\1\uffff\32\51\105\uffff\27\43\1\uffff"+
            "\37\43\1\uffff\u1f08\43\u1040\uffff\u0150\43\u0170\uffff\u0080"+
            "\43\u0080\uffff\u092e\43\u10d2\uffff\u5200\43\u0c00\uffff\u2bb0"+
            "\43\u2150\uffff\u0200\43",
            "\2\42\5\uffff\1\55\5\uffff\12\53\5\uffff\1\56\1\uffff\32\52"+
            "\1\uffff\1\54\2\uffff\1\42\1\uffff\32\51\105\uffff\27\43\1\uffff"+
            "\37\43\1\uffff\u1f08\43\u1040\uffff\u0150\43\u0170\uffff\u0080"+
            "\43\u0080\uffff\u092e\43\u10d2\uffff\u5200\43\u0c00\uffff\u2bb0"+
            "\43\u2150\uffff\u0200\43",
            "",
            "\1\55\1\66\1\uffff\1\66\2\uffff\12\117\5\uffff\1\56\1\uffff"+
            "\32\43\1\uffff\1\41\4\uffff\32\43\105\uffff\27\43\1\uffff\37"+
            "\43\1\uffff\u1f08\43\u1040\uffff\u0150\43\u0170\uffff\u0080"+
            "\43\u0080\uffff\u092e\43\u10d2\uffff\u5200\43\u0c00\uffff\u2bb0"+
            "\43\u2150\uffff\u0200\43",
            "\1\55\3\uffff\1\66\1\uffff\12\100\5\uffff\1\56\1\uffff\4\43"+
            "\1\77\25\43\1\uffff\1\41\4\uffff\4\43\1\77\25\43\105\uffff\27"+
            "\43\1\uffff\37\43\1\uffff\u1f08\43\u1040\uffff\u0150\43\u0170"+
            "\uffff\u0080\43\u0080\uffff\u092e\43\u10d2\uffff\u5200\43\u0c00"+
            "\uffff\u2bb0\43\u2150\uffff\u0200\43",
            "\1\55\3\uffff\1\66\1\uffff\12\101\5\uffff\1\56\1\uffff\4\43"+
            "\1\77\25\43\1\uffff\1\41\4\uffff\4\43\1\77\25\43\105\uffff\27"+
            "\43\1\uffff\37\43\1\uffff\u1f08\43\u1040\uffff\u0150\43\u0170"+
            "\uffff\u0080\43\u0080\uffff\u092e\43\u10d2\uffff\u5200\43\u0c00"+
            "\uffff\u2bb0\43\u2150\uffff\u0200\43",
            "\1\55\5\uffff\12\120\5\uffff\1\56\1\uffff\6\120\24\43\1\uffff"+
            "\1\41\4\uffff\6\120\24\43\105\uffff\27\43\1\uffff\37\43\1\uffff"+
            "\u1f08\43\u1040\uffff\u0150\43\u0170\uffff\u0080\43\u0080\uffff"+
            "\u092e\43\u10d2\uffff\u5200\43\u0c00\uffff\u2bb0\43\u2150\uffff"+
            "\u0200\43",
            "\1\55\5\uffff\12\43\5\uffff\1\56\1\uffff\32\43\1\uffff\1\41"+
            "\4\uffff\32\43\105\uffff\27\43\1\uffff\37\43\1\uffff\u1f08\43"+
            "\u1040\uffff\u0150\43\u0170\uffff\u0080\43\u0080\uffff\u092e"+
            "\43\u10d2\uffff\u5200\43\u0c00\uffff\u2bb0\43\u2150\uffff\u0200"+
            "\43",
            "",
            "",
            "\1\55\5\uffff\12\123\5\uffff\1\56\1\uffff\6\122\24\52\1\uffff"+
            "\1\54\4\uffff\6\121\24\51\105\uffff\27\43\1\uffff\37\43\1\uffff"+
            "\u1f08\43\u1040\uffff\u0150\43\u0170\uffff\u0080\43\u0080\uffff"+
            "\u092e\43\u10d2\uffff\u5200\43\u0c00\uffff\u2bb0\43\u2150\uffff"+
            "\u0200\43",
            "\1\55\5\uffff\12\53\5\uffff\1\56\1\uffff\32\52\1\uffff\1\54"+
            "\4\uffff\32\51\105\uffff\27\43\1\uffff\37\43\1\uffff\u1f08\43"+
            "\u1040\uffff\u0150\43\u0170\uffff\u0080\43\u0080\uffff\u092e"+
            "\43\u10d2\uffff\u5200\43\u0c00\uffff\u2bb0\43\u2150\uffff\u0200"+
            "\43",
            "",
            "\2\42\5\uffff\1\55\5\uffff\12\53\5\uffff\1\56\1\uffff\32\52"+
            "\1\uffff\1\54\2\uffff\1\42\1\uffff\32\51\105\uffff\27\43\1\uffff"+
            "\37\43\1\uffff\u1f08\43\u1040\uffff\u0150\43\u0170\uffff\u0080"+
            "\43\u0080\uffff\u092e\43\u10d2\uffff\u5200\43\u0c00\uffff\u2bb0"+
            "\43\u2150\uffff\u0200\43",
            "\2\42\5\uffff\1\55\5\uffff\12\53\5\uffff\1\56\1\uffff\32\52"+
            "\1\uffff\1\54\2\uffff\1\42\1\uffff\32\51\105\uffff\27\43\1\uffff"+
            "\37\43\1\uffff\u1f08\43\u1040\uffff\u0150\43\u0170\uffff\u0080"+
            "\43\u0080\uffff\u092e\43\u10d2\uffff\u5200\43\u0c00\uffff\u2bb0"+
            "\43\u2150\uffff\u0200\43",
            "\2\42\5\uffff\1\55\5\uffff\12\53\5\uffff\1\56\1\uffff\32\52"+
            "\1\uffff\1\54\2\uffff\1\42\1\uffff\32\51\105\uffff\27\43\1\uffff"+
            "\37\43\1\uffff\u1f08\43\u1040\uffff\u0150\43\u0170\uffff\u0080"+
            "\43\u0080\uffff\u092e\43\u10d2\uffff\u5200\43\u0c00\uffff\u2bb0"+
            "\43\u2150\uffff\u0200\43",
            "\2\42\5\uffff\1\55\5\uffff\12\53\5\uffff\1\56\1\uffff\32\52"+
            "\1\uffff\1\54\2\uffff\1\42\1\uffff\32\51\105\uffff\27\43\1\uffff"+
            "\37\43\1\uffff\u1f08\43\u1040\uffff\u0150\43\u0170\uffff\u0080"+
            "\43\u0080\uffff\u092e\43\u10d2\uffff\u5200\43\u0c00\uffff\u2bb0"+
            "\43\u2150\uffff\u0200\43",
            "\1\66\1\uffff\12\115\13\uffff\1\66\37\uffff\1\66",
            "",
            "\1\55\5\uffff\12\117\5\uffff\1\56\1\uffff\32\43\1\uffff\1"+
            "\41\4\uffff\32\43\105\uffff\27\43\1\uffff\37\43\1\uffff\u1f08"+
            "\43\u1040\uffff\u0150\43\u0170\uffff\u0080\43\u0080\uffff\u092e"+
            "\43\u10d2\uffff\u5200\43\u0c00\uffff\u2bb0\43\u2150\uffff\u0200"+
            "\43",
            "\1\55\5\uffff\12\126\5\uffff\1\56\1\uffff\6\126\24\43\1\uffff"+
            "\1\41\4\uffff\6\126\24\43\105\uffff\27\43\1\uffff\37\43\1\uffff"+
            "\u1f08\43\u1040\uffff\u0150\43\u0170\uffff\u0080\43\u0080\uffff"+
            "\u092e\43\u10d2\uffff\u5200\43\u0c00\uffff\u2bb0\43\u2150\uffff"+
            "\u0200\43",
            "\1\55\5\uffff\12\131\5\uffff\1\56\1\uffff\6\130\24\52\1\uffff"+
            "\1\54\4\uffff\6\127\24\51\105\uffff\27\43\1\uffff\37\43\1\uffff"+
            "\u1f08\43\u1040\uffff\u0150\43\u0170\uffff\u0080\43\u0080\uffff"+
            "\u092e\43\u10d2\uffff\u5200\43\u0c00\uffff\u2bb0\43\u2150\uffff"+
            "\u0200\43",
            "\1\55\5\uffff\12\131\5\uffff\1\56\1\uffff\6\130\24\52\1\uffff"+
            "\1\54\4\uffff\6\127\24\51\105\uffff\27\43\1\uffff\37\43\1\uffff"+
            "\u1f08\43\u1040\uffff\u0150\43\u0170\uffff\u0080\43\u0080\uffff"+
            "\u092e\43\u10d2\uffff\u5200\43\u0c00\uffff\u2bb0\43\u2150\uffff"+
            "\u0200\43",
            "\1\55\5\uffff\12\131\5\uffff\1\56\1\uffff\6\130\24\52\1\uffff"+
            "\1\54\4\uffff\6\127\24\51\105\uffff\27\43\1\uffff\37\43\1\uffff"+
            "\u1f08\43\u1040\uffff\u0150\43\u0170\uffff\u0080\43\u0080\uffff"+
            "\u092e\43\u10d2\uffff\u5200\43\u0c00\uffff\u2bb0\43\u2150\uffff"+
            "\u0200\43",
            "",
            "",
            "\1\55\5\uffff\12\132\5\uffff\1\56\1\uffff\6\132\24\43\1\uffff"+
            "\1\41\4\uffff\6\132\24\43\105\uffff\27\43\1\uffff\37\43\1\uffff"+
            "\u1f08\43\u1040\uffff\u0150\43\u0170\uffff\u0080\43\u0080\uffff"+
            "\u092e\43\u10d2\uffff\u5200\43\u0c00\uffff\u2bb0\43\u2150\uffff"+
            "\u0200\43",
            "\1\55\5\uffff\12\135\5\uffff\1\56\1\uffff\6\134\24\52\1\uffff"+
            "\1\54\4\uffff\6\133\24\51\105\uffff\27\43\1\uffff\37\43\1\uffff"+
            "\u1f08\43\u1040\uffff\u0150\43\u0170\uffff\u0080\43\u0080\uffff"+
            "\u092e\43\u10d2\uffff\u5200\43\u0c00\uffff\u2bb0\43\u2150\uffff"+
            "\u0200\43",
            "\1\55\5\uffff\12\135\5\uffff\1\56\1\uffff\6\134\24\52\1\uffff"+
            "\1\54\4\uffff\6\133\24\51\105\uffff\27\43\1\uffff\37\43\1\uffff"+
            "\u1f08\43\u1040\uffff\u0150\43\u0170\uffff\u0080\43\u0080\uffff"+
            "\u092e\43\u10d2\uffff\u5200\43\u0c00\uffff\u2bb0\43\u2150\uffff"+
            "\u0200\43",
            "\1\55\5\uffff\12\135\5\uffff\1\56\1\uffff\6\134\24\52\1\uffff"+
            "\1\54\4\uffff\6\133\24\51\105\uffff\27\43\1\uffff\37\43\1\uffff"+
            "\u1f08\43\u1040\uffff\u0150\43\u0170\uffff\u0080\43\u0080\uffff"+
            "\u092e\43\u10d2\uffff\u5200\43\u0c00\uffff\u2bb0\43\u2150\uffff"+
            "\u0200\43",
            "\1\55\5\uffff\12\136\5\uffff\1\56\1\uffff\6\136\24\43\1\uffff"+
            "\1\41\4\uffff\6\136\24\43\105\uffff\27\43\1\uffff\37\43\1\uffff"+
            "\u1f08\43\u1040\uffff\u0150\43\u0170\uffff\u0080\43\u0080\uffff"+
            "\u092e\43\u10d2\uffff\u5200\43\u0c00\uffff\u2bb0\43\u2150\uffff"+
            "\u0200\43",
            "\1\55\5\uffff\12\141\5\uffff\1\56\1\uffff\6\140\24\52\1\uffff"+
            "\1\54\4\uffff\6\137\24\51\105\uffff\27\43\1\uffff\37\43\1\uffff"+
            "\u1f08\43\u1040\uffff\u0150\43\u0170\uffff\u0080\43\u0080\uffff"+
            "\u092e\43\u10d2\uffff\u5200\43\u0c00\uffff\u2bb0\43\u2150\uffff"+
            "\u0200\43",
            "\1\55\5\uffff\12\141\5\uffff\1\56\1\uffff\6\140\24\52\1\uffff"+
            "\1\54\4\uffff\6\137\24\51\105\uffff\27\43\1\uffff\37\43\1\uffff"+
            "\u1f08\43\u1040\uffff\u0150\43\u0170\uffff\u0080\43\u0080\uffff"+
            "\u092e\43\u10d2\uffff\u5200\43\u0c00\uffff\u2bb0\43\u2150\uffff"+
            "\u0200\43",
            "\1\55\5\uffff\12\141\5\uffff\1\56\1\uffff\6\140\24\52\1\uffff"+
            "\1\54\4\uffff\6\137\24\51\105\uffff\27\43\1\uffff\37\43\1\uffff"+
            "\u1f08\43\u1040\uffff\u0150\43\u0170\uffff\u0080\43\u0080\uffff"+
            "\u092e\43\u10d2\uffff\u5200\43\u0c00\uffff\u2bb0\43\u2150\uffff"+
            "\u0200\43",
            "\1\55\5\uffff\12\43\5\uffff\1\56\1\uffff\32\43\1\uffff\1\41"+
            "\4\uffff\32\43\105\uffff\27\43\1\uffff\37\43\1\uffff\u1f08\43"+
            "\u1040\uffff\u0150\43\u0170\uffff\u0080\43\u0080\uffff\u092e"+
            "\43\u10d2\uffff\u5200\43\u0c00\uffff\u2bb0\43\u2150\uffff\u0200"+
            "\43",
            "\1\55\5\uffff\12\53\5\uffff\1\56\1\uffff\32\52\1\uffff\1\54"+
            "\4\uffff\32\51\105\uffff\27\43\1\uffff\37\43\1\uffff\u1f08\43"+
            "\u1040\uffff\u0150\43\u0170\uffff\u0080\43\u0080\uffff\u092e"+
            "\43\u10d2\uffff\u5200\43\u0c00\uffff\u2bb0\43\u2150\uffff\u0200"+
            "\43",
            "\1\55\5\uffff\12\53\5\uffff\1\56\1\uffff\32\52\1\uffff\1\54"+
            "\4\uffff\32\51\105\uffff\27\43\1\uffff\37\43\1\uffff\u1f08\43"+
            "\u1040\uffff\u0150\43\u0170\uffff\u0080\43\u0080\uffff\u092e"+
            "\43\u10d2\uffff\u5200\43\u0c00\uffff\u2bb0\43\u2150\uffff\u0200"+
            "\43",
            "\1\55\5\uffff\12\53\5\uffff\1\56\1\uffff\32\52\1\uffff\1\54"+
            "\4\uffff\32\51\105\uffff\27\43\1\uffff\37\43\1\uffff\u1f08\43"+
            "\u1040\uffff\u0150\43\u0170\uffff\u0080\43\u0080\uffff\u092e"+
            "\43\u10d2\uffff\u5200\43\u0c00\uffff\u2bb0\43\u2150\uffff\u0200"+
            "\43"
    };

    static final short[] DFA36_eot = DFA.unpackEncodedString(DFA36_eotS);
    static final short[] DFA36_eof = DFA.unpackEncodedString(DFA36_eofS);
    static final char[] DFA36_min = DFA.unpackEncodedStringToUnsignedChars(DFA36_minS);
    static final char[] DFA36_max = DFA.unpackEncodedStringToUnsignedChars(DFA36_maxS);
    static final short[] DFA36_accept = DFA.unpackEncodedString(DFA36_acceptS);
    static final short[] DFA36_special = DFA.unpackEncodedString(DFA36_specialS);
    static final short[][] DFA36_transition;

    static {
        int numStates = DFA36_transitionS.length;
        DFA36_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA36_transition[i] = DFA.unpackEncodedString(DFA36_transitionS[i]);
        }
    }

    class DFA36 extends DFA {

        public DFA36(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 36;
            this.eot = DFA36_eot;
            this.eof = DFA36_eof;
            this.min = DFA36_min;
            this.max = DFA36_max;
            this.accept = DFA36_accept;
            this.special = DFA36_special;
            this.transition = DFA36_transition;
        }
        public String getDescription() {
            return "1:1: Tokens : ( FTSPHRASE | URI | OR | AND | NOT | TILDA | LPAREN | RPAREN | PLUS | MINUS | COLON | STAR | DOTDOT | DOT | AMP | EXCLAMATION | BAR | EQUALS | QUESTION_MARK | LCURL | RCURL | LSQUARE | RSQUARE | TO | COMMA | CARAT | DOLLAR | GT | LT | AT | PERCENT | ID | DECIMAL_INTEGER_LITERAL | FTSWORD | FTSPRE | FTSWILD | FLOATING_POINT_LITERAL | WS );";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            IntStream input = _input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA36_44 = input.LA(1);

                        s = -1;
                        if ( (LA36_44=='u') ) {s = 70;}

                        else if ( ((LA36_44>='\u0000' && LA36_44<='t')||(LA36_44>='v' && LA36_44<='\uFFFF')) ) {s = 71;}

                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA36_33 = input.LA(1);

                        s = -1;
                        if ( (LA36_33=='u') ) {s = 66;}

                        else if ( ((LA36_33>='\u0000' && LA36_33<='t')||(LA36_33>='v' && LA36_33<='\uFFFF')) ) {s = 67;}

                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 36, _s, input);
            error(nvae);
            throw nvae;
        }
    }
 

}