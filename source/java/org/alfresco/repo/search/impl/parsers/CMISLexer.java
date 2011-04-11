// $ANTLR 3.2 Sep 23, 2009 12:02:23 W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g 2011-01-12 12:54:12

    package org.alfresco.repo.search.impl.parsers;
    import org.alfresco.cmis.CMISQueryException;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

public class CMISLexer extends Lexer {
    public static final int FUNCTION=10;
    public static final int WHERE=47;
    public static final int EXPONENT=84;
    public static final int PRED_FTS=22;
    public static final int STAR=33;
    public static final int INNER=42;
    public static final int ORDER=64;
    public static final int DOUBLE_QUOTE=71;
    public static final int NUMERIC_LITERAL=27;
    public static final int PRED_COMPARISON=18;
    public static final int CONTAINS=61;
    public static final int TABLE=12;
    public static final int SOURCE=11;
    public static final int EQUALS=46;
    public static final int DOTDOT=78;
    public static final int NOT=50;
    public static final int ID=70;
    public static final int AND=49;
    public static final int EOF=-1;
    public static final int LPAREN=38;
    public static final int LESSTHANOREQUALS=54;
    public static final int AS=35;
    public static final int RPAREN=39;
    public static final int TILDA=79;
    public static final int PRED_LIKE=21;
    public static final int STRING_LITERAL=28;
    public static final int IN=56;
    public static final int DECIMAL_NUMERAL=82;
    public static final int FLOATING_POINT_LITERAL=72;
    public static final int COMMA=34;
    public static final int IS=58;
    public static final int LEFT=43;
    public static final int SIGNED_INTEGER=89;
    public static final int PARAMETER=14;
    public static final int COLUMN=6;
    public static final int PLUS=80;
    public static final int QUOTED_STRING=69;
    public static final int SINGLE_VALUED_PROPERTY=31;
    public static final int ZERO_DIGIT=86;
    public static final int DIGIT=83;
    public static final int DOT=37;
    public static final int COLUMN_REF=8;
    public static final int SELECT=32;
    public static final int LIKE=57;
    public static final int DOTSTAR=36;
    public static final int GREATERTHAN=53;
    public static final int OUTER=44;
    public static final int E=88;
    public static final int LESSTHAN=52;
    public static final int BY=65;
    public static final int ASC=66;
    public static final int NON_ZERO_DIGIT=87;
    public static final int QUALIFIER=9;
    public static final int CONJUNCTION=15;
    public static final int NULL=59;
    public static final int ON=45;
    public static final int NOTEQUALS=51;
    public static final int DATETIME_LITERAL=29;
    public static final int MINUS=81;
    public static final int LIST=23;
    public static final int PRED_DESCENDANT=25;
    public static final int TRUE=74;
    public static final int JOIN=41;
    public static final int IN_FOLDER=62;
    public static final int BOOLEAN_LITERAL=30;
    public static final int GREATERTHANOREQUALS=55;
    public static final int COLON=68;
    public static final int COLUMNS=7;
    public static final int DISJUNCTION=16;
    public static final int ANY=60;
    public static final int WS=85;
    public static final int SCORE=77;
    public static final int NEGATION=17;
    public static final int TABLE_REF=13;
    public static final int SORT_SPECIFICATION=26;
    public static final int IN_TREE=63;
    public static final int OR=48;
    public static final int PRED_CHILD=24;
    public static final int PRED_EXISTS=20;
    public static final int QUERY=4;
    public static final int ALL_COLUMNS=5;
    public static final int DESC=67;
    public static final int DECIMAL_INTEGER_LITERAL=73;
    public static final int FROM=40;
    public static final int FALSE=75;
    public static final int TIMESTAMP=76;
    public static final int PRED_IN=19;

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
                    return Token.EOF_TOKEN;
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
                    throw new CMISQueryException(getErrorString(re), re);
                }
            }
        }
        
        public String getErrorString(RecognitionException e)
        {
            String hdr = getErrorHeader(e);
            String msg = getErrorMessage(e, this.getTokenNames());
            return hdr+" "+msg;
        }


    // delegates
    // delegators

    public CMISLexer() {;} 
    public CMISLexer(CharStream input) {
        this(input, new RecognizerSharedState());
    }
    public CMISLexer(CharStream input, RecognizerSharedState state) {
        super(input,state);

    }
    public String getGrammarFileName() { return "W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g"; }

    // $ANTLR start "QUOTED_STRING"
    public final void mQUOTED_STRING() throws RecognitionException {
        try {
            int _type = QUOTED_STRING;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:853:9: ( '\\'' (~ ( '\\'' | '\\\\' ) | '\\\\' . )* '\\'' )
            // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:854:9: '\\'' (~ ( '\\'' | '\\\\' ) | '\\\\' . )* '\\''
            {
            match('\''); 
            // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:855:9: (~ ( '\\'' | '\\\\' ) | '\\\\' . )*
            loop1:
            do {
                int alt1=3;
                int LA1_0 = input.LA(1);

                if ( ((LA1_0>='\u0000' && LA1_0<='&')||(LA1_0>='(' && LA1_0<='[')||(LA1_0>=']' && LA1_0<='\uFFFF')) ) {
                    alt1=1;
                }
                else if ( (LA1_0=='\\') ) {
                    alt1=2;
                }


                switch (alt1) {
            	case 1 :
            	    // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:856:17: ~ ( '\\'' | '\\\\' )
            	    {
            	    if ( (input.LA(1)>='\u0000' && input.LA(1)<='&')||(input.LA(1)>='(' && input.LA(1)<='[')||(input.LA(1)>=']' && input.LA(1)<='\uFFFF') ) {
            	        input.consume();

            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;}


            	    }
            	    break;
            	case 2 :
            	    // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:857:19: '\\\\' .
            	    {
            	    match('\\'); 
            	    matchAny(); 

            	    }
            	    break;

            	default :
            	    break loop1;
                }
            } while (true);

            match('\''); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "QUOTED_STRING"

    // $ANTLR start "SELECT"
    public final void mSELECT() throws RecognitionException {
        try {
            int _type = SELECT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:863:9: ( ( 'S' | 's' ) ( 'E' | 'e' ) ( 'L' | 'l' ) ( 'E' | 'e' ) ( 'C' | 'c' ) ( 'T' | 't' ) )
            // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:864:9: ( 'S' | 's' ) ( 'E' | 'e' ) ( 'L' | 'l' ) ( 'E' | 'e' ) ( 'C' | 'c' ) ( 'T' | 't' )
            {
            if ( input.LA(1)=='S'||input.LA(1)=='s' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='L'||input.LA(1)=='l' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='C'||input.LA(1)=='c' ) {
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
    // $ANTLR end "SELECT"

    // $ANTLR start "AS"
    public final void mAS() throws RecognitionException {
        try {
            int _type = AS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:891:9: ( ( 'A' | 'a' ) ( 'S' | 's' ) )
            // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:892:9: ( 'A' | 'a' ) ( 'S' | 's' )
            {
            if ( input.LA(1)=='A'||input.LA(1)=='a' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='S'||input.LA(1)=='s' ) {
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
    // $ANTLR end "AS"

    // $ANTLR start "FROM"
    public final void mFROM() throws RecognitionException {
        try {
            int _type = FROM;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:903:9: ( ( 'F' | 'f' ) ( 'R' | 'r' ) ( 'O' | 'o' ) ( 'M' | 'm' ) )
            // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:904:9: ( 'F' | 'f' ) ( 'R' | 'r' ) ( 'O' | 'o' ) ( 'M' | 'm' )
            {
            if ( input.LA(1)=='F'||input.LA(1)=='f' ) {
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

            if ( input.LA(1)=='O'||input.LA(1)=='o' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='M'||input.LA(1)=='m' ) {
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
    // $ANTLR end "FROM"

    // $ANTLR start "JOIN"
    public final void mJOIN() throws RecognitionException {
        try {
            int _type = JOIN;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:923:9: ( ( 'J' | 'j' ) ( 'O' | 'o' ) ( 'I' | 'i' ) ( 'N' | 'n' ) )
            // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:924:9: ( 'J' | 'j' ) ( 'O' | 'o' ) ( 'I' | 'i' ) ( 'N' | 'n' )
            {
            if ( input.LA(1)=='J'||input.LA(1)=='j' ) {
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

            if ( input.LA(1)=='I'||input.LA(1)=='i' ) {
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


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "JOIN"

    // $ANTLR start "INNER"
    public final void mINNER() throws RecognitionException {
        try {
            int _type = INNER;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:943:9: ( ( 'I' | 'i' ) ( 'N' | 'n' ) ( 'N' | 'n' ) ( 'E' | 'e' ) ( 'R' | 'r' ) )
            // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:944:9: ( 'I' | 'i' ) ( 'N' | 'n' ) ( 'N' | 'n' ) ( 'E' | 'e' ) ( 'R' | 'r' )
            {
            if ( input.LA(1)=='I'||input.LA(1)=='i' ) {
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

            if ( input.LA(1)=='N'||input.LA(1)=='n' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
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
    // $ANTLR end "INNER"

    // $ANTLR start "LEFT"
    public final void mLEFT() throws RecognitionException {
        try {
            int _type = LEFT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:967:9: ( ( 'L' | 'l' ) ( 'E' | 'e' ) ( 'F' | 'f' ) ( 'T' | 't' ) )
            // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:968:9: ( 'L' | 'l' ) ( 'E' | 'e' ) ( 'F' | 'f' ) ( 'T' | 't' )
            {
            if ( input.LA(1)=='L'||input.LA(1)=='l' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='F'||input.LA(1)=='f' ) {
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
    // $ANTLR end "LEFT"

    // $ANTLR start "OUTER"
    public final void mOUTER() throws RecognitionException {
        try {
            int _type = OUTER;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:987:9: ( ( 'O' | 'o' ) ( 'U' | 'u' ) ( 'T' | 't' ) ( 'E' | 'e' ) ( 'R' | 'r' ) )
            // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:988:9: ( 'O' | 'o' ) ( 'U' | 'u' ) ( 'T' | 't' ) ( 'E' | 'e' ) ( 'R' | 'r' )
            {
            if ( input.LA(1)=='O'||input.LA(1)=='o' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='U'||input.LA(1)=='u' ) {
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

            if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
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
    // $ANTLR end "OUTER"

    // $ANTLR start "ON"
    public final void mON() throws RecognitionException {
        try {
            int _type = ON;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1011:9: ( ( 'O' | 'o' ) ( 'N' | 'n' ) )
            // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1012:9: ( 'O' | 'o' ) ( 'N' | 'n' )
            {
            if ( input.LA(1)=='O'||input.LA(1)=='o' ) {
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


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "ON"

    // $ANTLR start "WHERE"
    public final void mWHERE() throws RecognitionException {
        try {
            int _type = WHERE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1023:9: ( ( 'W' | 'w' ) ( 'H' | 'h' ) ( 'E' | 'e' ) ( 'R' | 'r' ) ( 'E' | 'e' ) )
            // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1024:9: ( 'W' | 'w' ) ( 'H' | 'h' ) ( 'E' | 'e' ) ( 'R' | 'r' ) ( 'E' | 'e' )
            {
            if ( input.LA(1)=='W'||input.LA(1)=='w' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='H'||input.LA(1)=='h' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
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

            if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
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
    // $ANTLR end "WHERE"

    // $ANTLR start "OR"
    public final void mOR() throws RecognitionException {
        try {
            int _type = OR;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1047:9: ( ( 'O' | 'o' ) ( 'R' | 'r' ) )
            // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1048:9: ( 'O' | 'o' ) ( 'R' | 'r' )
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
            // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1059:9: ( ( 'A' | 'a' ) ( 'N' | 'n' ) ( 'D' | 'd' ) )
            // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1060:9: ( 'A' | 'a' ) ( 'N' | 'n' ) ( 'D' | 'd' )
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
            // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1075:9: ( ( 'N' | 'n' ) ( 'O' | 'o' ) ( 'T' | 't' ) )
            // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1076:9: ( 'N' | 'n' ) ( 'O' | 'o' ) ( 'T' | 't' )
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

    // $ANTLR start "IN"
    public final void mIN() throws RecognitionException {
        try {
            int _type = IN;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1091:9: ( ( 'I' | 'i' ) ( 'N' | 'n' ) )
            // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1092:9: ( 'I' | 'i' ) ( 'N' | 'n' )
            {
            if ( input.LA(1)=='I'||input.LA(1)=='i' ) {
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


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "IN"

    // $ANTLR start "LIKE"
    public final void mLIKE() throws RecognitionException {
        try {
            int _type = LIKE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1103:9: ( ( 'L' | 'l' ) ( 'I' | 'i' ) ( 'K' | 'k' ) ( 'E' | 'e' ) )
            // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1104:9: ( 'L' | 'l' ) ( 'I' | 'i' ) ( 'K' | 'k' ) ( 'E' | 'e' )
            {
            if ( input.LA(1)=='L'||input.LA(1)=='l' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='I'||input.LA(1)=='i' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='K'||input.LA(1)=='k' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
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
    // $ANTLR end "LIKE"

    // $ANTLR start "IS"
    public final void mIS() throws RecognitionException {
        try {
            int _type = IS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1123:9: ( ( 'I' | 'i' ) ( 'S' | 's' ) )
            // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1124:9: ( 'I' | 'i' ) ( 'S' | 's' )
            {
            if ( input.LA(1)=='I'||input.LA(1)=='i' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='S'||input.LA(1)=='s' ) {
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
    // $ANTLR end "IS"

    // $ANTLR start "NULL"
    public final void mNULL() throws RecognitionException {
        try {
            int _type = NULL;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1135:9: ( ( 'N' | 'n' ) ( 'U' | 'u' ) ( 'L' | 'l' ) ( 'L' | 'l' ) )
            // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1136:9: ( 'N' | 'n' ) ( 'U' | 'u' ) ( 'L' | 'l' ) ( 'L' | 'l' )
            {
            if ( input.LA(1)=='N'||input.LA(1)=='n' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='U'||input.LA(1)=='u' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='L'||input.LA(1)=='l' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='L'||input.LA(1)=='l' ) {
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
    // $ANTLR end "NULL"

    // $ANTLR start "ANY"
    public final void mANY() throws RecognitionException {
        try {
            int _type = ANY;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1155:9: ( ( 'A' | 'a' ) ( 'N' | 'n' ) ( 'Y' | 'y' ) )
            // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1156:9: ( 'A' | 'a' ) ( 'N' | 'n' ) ( 'Y' | 'y' )
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

            if ( input.LA(1)=='Y'||input.LA(1)=='y' ) {
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
    // $ANTLR end "ANY"

    // $ANTLR start "CONTAINS"
    public final void mCONTAINS() throws RecognitionException {
        try {
            int _type = CONTAINS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1171:9: ( ( 'C' | 'c' ) ( 'O' | 'o' ) ( 'N' | 'n' ) ( 'T' | 't' ) ( 'A' | 'a' ) ( 'I' | 'i' ) ( 'N' | 'n' ) ( 'S' | 's' ) )
            // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1172:9: ( 'C' | 'c' ) ( 'O' | 'o' ) ( 'N' | 'n' ) ( 'T' | 't' ) ( 'A' | 'a' ) ( 'I' | 'i' ) ( 'N' | 'n' ) ( 'S' | 's' )
            {
            if ( input.LA(1)=='C'||input.LA(1)=='c' ) {
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

            if ( input.LA(1)=='N'||input.LA(1)=='n' ) {
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

            if ( input.LA(1)=='A'||input.LA(1)=='a' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='I'||input.LA(1)=='i' ) {
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

            if ( input.LA(1)=='S'||input.LA(1)=='s' ) {
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
    // $ANTLR end "CONTAINS"

    // $ANTLR start "IN_FOLDER"
    public final void mIN_FOLDER() throws RecognitionException {
        try {
            int _type = IN_FOLDER;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1207:9: ( ( 'I' | 'i' ) ( 'N' | 'n' ) '_' ( 'F' | 'f' ) ( 'O' | 'o' ) ( 'L' | 'l' ) ( 'D' | 'd' ) ( 'E' | 'e' ) ( 'R' | 'r' ) )
            // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1208:9: ( 'I' | 'i' ) ( 'N' | 'n' ) '_' ( 'F' | 'f' ) ( 'O' | 'o' ) ( 'L' | 'l' ) ( 'D' | 'd' ) ( 'E' | 'e' ) ( 'R' | 'r' )
            {
            if ( input.LA(1)=='I'||input.LA(1)=='i' ) {
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

            match('_'); 
            if ( input.LA(1)=='F'||input.LA(1)=='f' ) {
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

            if ( input.LA(1)=='L'||input.LA(1)=='l' ) {
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

            if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
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
    // $ANTLR end "IN_FOLDER"

    // $ANTLR start "IN_TREE"
    public final void mIN_TREE() throws RecognitionException {
        try {
            int _type = IN_TREE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1244:9: ( ( 'I' | 'i' ) ( 'N' | 'n' ) '_' ( 'T' | 't' ) ( 'R' | 'r' ) ( 'E' | 'e' ) ( 'E' | 'e' ) )
            // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1245:9: ( 'I' | 'i' ) ( 'N' | 'n' ) '_' ( 'T' | 't' ) ( 'R' | 'r' ) ( 'E' | 'e' ) ( 'E' | 'e' )
            {
            if ( input.LA(1)=='I'||input.LA(1)=='i' ) {
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

            match('_'); 
            if ( input.LA(1)=='T'||input.LA(1)=='t' ) {
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

            if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
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
    // $ANTLR end "IN_TREE"

    // $ANTLR start "ORDER"
    public final void mORDER() throws RecognitionException {
        try {
            int _type = ORDER;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1273:9: ( ( 'O' | 'o' ) ( 'R' | 'r' ) ( 'D' | 'd' ) ( 'E' | 'e' ) ( 'R' | 'r' ) )
            // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1274:9: ( 'O' | 'o' ) ( 'R' | 'r' ) ( 'D' | 'd' ) ( 'E' | 'e' ) ( 'R' | 'r' )
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

            if ( input.LA(1)=='D'||input.LA(1)=='d' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
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
    // $ANTLR end "ORDER"

    // $ANTLR start "BY"
    public final void mBY() throws RecognitionException {
        try {
            int _type = BY;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1297:9: ( ( 'B' | 'b' ) ( 'Y' | 'y' ) )
            // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1298:9: ( 'B' | 'b' ) ( 'Y' | 'y' )
            {
            if ( input.LA(1)=='B'||input.LA(1)=='b' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='Y'||input.LA(1)=='y' ) {
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
    // $ANTLR end "BY"

    // $ANTLR start "ASC"
    public final void mASC() throws RecognitionException {
        try {
            int _type = ASC;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1309:9: ( ( 'A' | 'a' ) ( 'S' | 's' ) ( 'C' | 'c' ) )
            // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1310:9: ( 'A' | 'a' ) ( 'S' | 's' ) ( 'C' | 'c' )
            {
            if ( input.LA(1)=='A'||input.LA(1)=='a' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='S'||input.LA(1)=='s' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='C'||input.LA(1)=='c' ) {
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
    // $ANTLR end "ASC"

    // $ANTLR start "DESC"
    public final void mDESC() throws RecognitionException {
        try {
            int _type = DESC;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1325:9: ( ( 'D' | 'd' ) ( 'E' | 'e' ) ( 'S' | 's' ) ( 'C' | 'c' ) )
            // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1326:9: ( 'D' | 'd' ) ( 'E' | 'e' ) ( 'S' | 's' ) ( 'C' | 'c' )
            {
            if ( input.LA(1)=='D'||input.LA(1)=='d' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='S'||input.LA(1)=='s' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='C'||input.LA(1)=='c' ) {
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
    // $ANTLR end "DESC"

    // $ANTLR start "TIMESTAMP"
    public final void mTIMESTAMP() throws RecognitionException {
        try {
            int _type = TIMESTAMP;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1345:9: ( ( 'T' | 't' ) ( 'I' | 'i' ) ( 'M' | 'm' ) ( 'E' | 'e' ) ( 'S' | 's' ) ( 'T' | 't' ) ( 'A' | 'a' ) ( 'M' | 'm' ) ( 'P' | 'p' ) )
            // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1346:9: ( 'T' | 't' ) ( 'I' | 'i' ) ( 'M' | 'm' ) ( 'E' | 'e' ) ( 'S' | 's' ) ( 'T' | 't' ) ( 'A' | 'a' ) ( 'M' | 'm' ) ( 'P' | 'p' )
            {
            if ( input.LA(1)=='T'||input.LA(1)=='t' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='I'||input.LA(1)=='i' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='M'||input.LA(1)=='m' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='S'||input.LA(1)=='s' ) {
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

            if ( input.LA(1)=='A'||input.LA(1)=='a' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='M'||input.LA(1)=='m' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='P'||input.LA(1)=='p' ) {
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
    // $ANTLR end "TIMESTAMP"

    // $ANTLR start "TRUE"
    public final void mTRUE() throws RecognitionException {
        try {
            int _type = TRUE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1385:9: ( ( 'T' | 't' ) ( 'R' | 'r' ) ( 'U' | 'u' ) ( 'E' | 'e' ) )
            // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1386:9: ( 'T' | 't' ) ( 'R' | 'r' ) ( 'U' | 'u' ) ( 'E' | 'e' )
            {
            if ( input.LA(1)=='T'||input.LA(1)=='t' ) {
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

            if ( input.LA(1)=='U'||input.LA(1)=='u' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
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
    // $ANTLR end "TRUE"

    // $ANTLR start "FALSE"
    public final void mFALSE() throws RecognitionException {
        try {
            int _type = FALSE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1405:9: ( ( 'F' | 'f' ) ( 'A' | 'a' ) ( 'L' | 'l' ) ( 'S' | 's' ) ( 'E' | 'e' ) )
            // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1406:9: ( 'F' | 'f' ) ( 'A' | 'a' ) ( 'L' | 'l' ) ( 'S' | 's' ) ( 'E' | 'e' )
            {
            if ( input.LA(1)=='F'||input.LA(1)=='f' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='A'||input.LA(1)=='a' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='L'||input.LA(1)=='l' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='S'||input.LA(1)=='s' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
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
    // $ANTLR end "FALSE"

    // $ANTLR start "SCORE"
    public final void mSCORE() throws RecognitionException {
        try {
            int _type = SCORE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1429:9: ( ( 'S' | 's' ) ( 'C' | 'c' ) ( 'O' | 'o' ) ( 'R' | 'r' ) ( 'E' | 'e' ) )
            // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1430:9: ( 'S' | 's' ) ( 'C' | 'c' ) ( 'O' | 'o' ) ( 'R' | 'r' ) ( 'E' | 'e' )
            {
            if ( input.LA(1)=='S'||input.LA(1)=='s' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='C'||input.LA(1)=='c' ) {
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

            if ( input.LA(1)=='R'||input.LA(1)=='r' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
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
    // $ANTLR end "SCORE"

    // $ANTLR start "LPAREN"
    public final void mLPAREN() throws RecognitionException {
        try {
            int _type = LPAREN;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1453:9: ( '(' )
            // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1454:9: '('
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
            // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1458:9: ( ')' )
            // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1459:9: ')'
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

    // $ANTLR start "STAR"
    public final void mSTAR() throws RecognitionException {
        try {
            int _type = STAR;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1463:9: ( '*' )
            // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1464:9: '*'
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

    // $ANTLR start "COMMA"
    public final void mCOMMA() throws RecognitionException {
        try {
            int _type = COMMA;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1468:9: ( ',' )
            // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1469:9: ','
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

    // $ANTLR start "DOTSTAR"
    public final void mDOTSTAR() throws RecognitionException {
        try {
            int _type = DOTSTAR;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1473:9: ( '.*' )
            // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1474:9: '.*'
            {
            match(".*"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "DOTSTAR"

    // $ANTLR start "DOT"
    public final void mDOT() throws RecognitionException {
        try {
            int _type = DOT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1478:9: ( '.' )
            // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1479:9: '.'
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

    // $ANTLR start "DOTDOT"
    public final void mDOTDOT() throws RecognitionException {
        try {
            int _type = DOTDOT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1483:9: ( '..' )
            // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1484:9: '..'
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

    // $ANTLR start "EQUALS"
    public final void mEQUALS() throws RecognitionException {
        try {
            int _type = EQUALS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1488:9: ( '=' )
            // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1489:9: '='
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

    // $ANTLR start "TILDA"
    public final void mTILDA() throws RecognitionException {
        try {
            int _type = TILDA;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1493:9: ( '~' )
            // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1494:9: '~'
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

    // $ANTLR start "NOTEQUALS"
    public final void mNOTEQUALS() throws RecognitionException {
        try {
            int _type = NOTEQUALS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1498:9: ( '<>' )
            // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1499:9: '<>'
            {
            match("<>"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "NOTEQUALS"

    // $ANTLR start "GREATERTHAN"
    public final void mGREATERTHAN() throws RecognitionException {
        try {
            int _type = GREATERTHAN;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1503:9: ( '>' )
            // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1504:9: '>'
            {
            match('>'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "GREATERTHAN"

    // $ANTLR start "LESSTHAN"
    public final void mLESSTHAN() throws RecognitionException {
        try {
            int _type = LESSTHAN;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1508:9: ( '<' )
            // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1509:9: '<'
            {
            match('<'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "LESSTHAN"

    // $ANTLR start "GREATERTHANOREQUALS"
    public final void mGREATERTHANOREQUALS() throws RecognitionException {
        try {
            int _type = GREATERTHANOREQUALS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1513:9: ( '>=' )
            // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1514:9: '>='
            {
            match(">="); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "GREATERTHANOREQUALS"

    // $ANTLR start "LESSTHANOREQUALS"
    public final void mLESSTHANOREQUALS() throws RecognitionException {
        try {
            int _type = LESSTHANOREQUALS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1518:9: ( '<=' )
            // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1519:9: '<='
            {
            match("<="); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "LESSTHANOREQUALS"

    // $ANTLR start "COLON"
    public final void mCOLON() throws RecognitionException {
        try {
            int _type = COLON;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1523:9: ( ':' )
            // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1524:9: ':'
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

    // $ANTLR start "DOUBLE_QUOTE"
    public final void mDOUBLE_QUOTE() throws RecognitionException {
        try {
            int _type = DOUBLE_QUOTE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1528:9: ( '\"' )
            // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1529:9: '\"'
            {
            match('\"'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "DOUBLE_QUOTE"

    // $ANTLR start "DECIMAL_INTEGER_LITERAL"
    public final void mDECIMAL_INTEGER_LITERAL() throws RecognitionException {
        try {
            int _type = DECIMAL_INTEGER_LITERAL;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1537:9: ( ( PLUS | MINUS )? DECIMAL_NUMERAL )
            // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1538:9: ( PLUS | MINUS )? DECIMAL_NUMERAL
            {
            // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1538:9: ( PLUS | MINUS )?
            int alt2=2;
            int LA2_0 = input.LA(1);

            if ( (LA2_0=='+'||LA2_0=='-') ) {
                alt2=1;
            }
            switch (alt2) {
                case 1 :
                    // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:
                    {
                    if ( input.LA(1)=='+'||input.LA(1)=='-' ) {
                        input.consume();

                    }
                    else {
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;}


                    }
                    break;

            }

            mDECIMAL_NUMERAL(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "DECIMAL_INTEGER_LITERAL"

    // $ANTLR start "FLOATING_POINT_LITERAL"
    public final void mFLOATING_POINT_LITERAL() throws RecognitionException {
        try {
            int _type = FLOATING_POINT_LITERAL;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1550:9: ( ( PLUS | MINUS )? ( DIGIT )+ DOT ( DIGIT )* ( EXPONENT )? | ( PLUS | MINUS )? DOT ( DIGIT )+ ( EXPONENT )? | ( PLUS | MINUS )? ( DIGIT )+ EXPONENT )
            int alt12=3;
            alt12 = dfa12.predict(input);
            switch (alt12) {
                case 1 :
                    // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1551:9: ( PLUS | MINUS )? ( DIGIT )+ DOT ( DIGIT )* ( EXPONENT )?
                    {
                    // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1551:9: ( PLUS | MINUS )?
                    int alt3=2;
                    int LA3_0 = input.LA(1);

                    if ( (LA3_0=='+'||LA3_0=='-') ) {
                        alt3=1;
                    }
                    switch (alt3) {
                        case 1 :
                            // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:
                            {
                            if ( input.LA(1)=='+'||input.LA(1)=='-' ) {
                                input.consume();

                            }
                            else {
                                MismatchedSetException mse = new MismatchedSetException(null,input);
                                recover(mse);
                                throw mse;}


                            }
                            break;

                    }

                    // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1555:9: ( DIGIT )+
                    int cnt4=0;
                    loop4:
                    do {
                        int alt4=2;
                        int LA4_0 = input.LA(1);

                        if ( ((LA4_0>='0' && LA4_0<='9')) ) {
                            alt4=1;
                        }


                        switch (alt4) {
                    	case 1 :
                    	    // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1555:9: DIGIT
                    	    {
                    	    mDIGIT(); 

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

                    mDOT(); 
                    // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1555:20: ( DIGIT )*
                    loop5:
                    do {
                        int alt5=2;
                        int LA5_0 = input.LA(1);

                        if ( ((LA5_0>='0' && LA5_0<='9')) ) {
                            alt5=1;
                        }


                        switch (alt5) {
                    	case 1 :
                    	    // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1555:20: DIGIT
                    	    {
                    	    mDIGIT(); 

                    	    }
                    	    break;

                    	default :
                    	    break loop5;
                        }
                    } while (true);

                    // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1555:27: ( EXPONENT )?
                    int alt6=2;
                    int LA6_0 = input.LA(1);

                    if ( (LA6_0=='E'||LA6_0=='e') ) {
                        alt6=1;
                    }
                    switch (alt6) {
                        case 1 :
                            // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1555:27: EXPONENT
                            {
                            mEXPONENT(); 

                            }
                            break;

                    }


                    }
                    break;
                case 2 :
                    // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1557:9: ( PLUS | MINUS )? DOT ( DIGIT )+ ( EXPONENT )?
                    {
                    // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1557:9: ( PLUS | MINUS )?
                    int alt7=2;
                    int LA7_0 = input.LA(1);

                    if ( (LA7_0=='+'||LA7_0=='-') ) {
                        alt7=1;
                    }
                    switch (alt7) {
                        case 1 :
                            // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:
                            {
                            if ( input.LA(1)=='+'||input.LA(1)=='-' ) {
                                input.consume();

                            }
                            else {
                                MismatchedSetException mse = new MismatchedSetException(null,input);
                                recover(mse);
                                throw mse;}


                            }
                            break;

                    }

                    mDOT(); 
                    // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1561:13: ( DIGIT )+
                    int cnt8=0;
                    loop8:
                    do {
                        int alt8=2;
                        int LA8_0 = input.LA(1);

                        if ( ((LA8_0>='0' && LA8_0<='9')) ) {
                            alt8=1;
                        }


                        switch (alt8) {
                    	case 1 :
                    	    // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1561:13: DIGIT
                    	    {
                    	    mDIGIT(); 

                    	    }
                    	    break;

                    	default :
                    	    if ( cnt8 >= 1 ) break loop8;
                                EarlyExitException eee =
                                    new EarlyExitException(8, input);
                                throw eee;
                        }
                        cnt8++;
                    } while (true);

                    // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1561:20: ( EXPONENT )?
                    int alt9=2;
                    int LA9_0 = input.LA(1);

                    if ( (LA9_0=='E'||LA9_0=='e') ) {
                        alt9=1;
                    }
                    switch (alt9) {
                        case 1 :
                            // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1561:20: EXPONENT
                            {
                            mEXPONENT(); 

                            }
                            break;

                    }


                    }
                    break;
                case 3 :
                    // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1563:9: ( PLUS | MINUS )? ( DIGIT )+ EXPONENT
                    {
                    // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1563:9: ( PLUS | MINUS )?
                    int alt10=2;
                    int LA10_0 = input.LA(1);

                    if ( (LA10_0=='+'||LA10_0=='-') ) {
                        alt10=1;
                    }
                    switch (alt10) {
                        case 1 :
                            // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:
                            {
                            if ( input.LA(1)=='+'||input.LA(1)=='-' ) {
                                input.consume();

                            }
                            else {
                                MismatchedSetException mse = new MismatchedSetException(null,input);
                                recover(mse);
                                throw mse;}


                            }
                            break;

                    }

                    // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1567:9: ( DIGIT )+
                    int cnt11=0;
                    loop11:
                    do {
                        int alt11=2;
                        int LA11_0 = input.LA(1);

                        if ( ((LA11_0>='0' && LA11_0<='9')) ) {
                            alt11=1;
                        }


                        switch (alt11) {
                    	case 1 :
                    	    // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1567:9: DIGIT
                    	    {
                    	    mDIGIT(); 

                    	    }
                    	    break;

                    	default :
                    	    if ( cnt11 >= 1 ) break loop11;
                                EarlyExitException eee =
                                    new EarlyExitException(11, input);
                                throw eee;
                        }
                        cnt11++;
                    } while (true);

                    mEXPONENT(); 

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

    // $ANTLR start "ID"
    public final void mID() throws RecognitionException {
        try {
            int _type = ID;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1574:9: ( ( 'a' .. 'z' | 'A' .. 'Z' | '_' ) ( 'a' .. 'z' | 'A' .. 'Z' | '0' .. '9' | '_' | ':' | '$' | '#' )* )
            // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1575:9: ( 'a' .. 'z' | 'A' .. 'Z' | '_' ) ( 'a' .. 'z' | 'A' .. 'Z' | '0' .. '9' | '_' | ':' | '$' | '#' )*
            {
            if ( (input.LA(1)>='A' && input.LA(1)<='Z')||input.LA(1)=='_'||(input.LA(1)>='a' && input.LA(1)<='z') ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1580:9: ( 'a' .. 'z' | 'A' .. 'Z' | '0' .. '9' | '_' | ':' | '$' | '#' )*
            loop13:
            do {
                int alt13=2;
                int LA13_0 = input.LA(1);

                if ( ((LA13_0>='#' && LA13_0<='$')||(LA13_0>='0' && LA13_0<=':')||(LA13_0>='A' && LA13_0<='Z')||LA13_0=='_'||(LA13_0>='a' && LA13_0<='z')) ) {
                    alt13=1;
                }


                switch (alt13) {
            	case 1 :
            	    // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:
            	    {
            	    if ( (input.LA(1)>='#' && input.LA(1)<='$')||(input.LA(1)>='0' && input.LA(1)<=':')||(input.LA(1)>='A' && input.LA(1)<='Z')||input.LA(1)=='_'||(input.LA(1)>='a' && input.LA(1)<='z') ) {
            	        input.consume();

            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;}


            	    }
            	    break;

            	default :
            	    break loop13;
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

    // $ANTLR start "WS"
    public final void mWS() throws RecognitionException {
        try {
            int _type = WS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1592:9: ( ( ' ' | '\\t' | '\\r' | '\\n' )+ )
            // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1593:9: ( ' ' | '\\t' | '\\r' | '\\n' )+
            {
            // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1593:9: ( ' ' | '\\t' | '\\r' | '\\n' )+
            int cnt14=0;
            loop14:
            do {
                int alt14=2;
                int LA14_0 = input.LA(1);

                if ( ((LA14_0>='\t' && LA14_0<='\n')||LA14_0=='\r'||LA14_0==' ') ) {
                    alt14=1;
                }


                switch (alt14) {
            	case 1 :
            	    // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:
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
            	    if ( cnt14 >= 1 ) break loop14;
                        EarlyExitException eee =
                            new EarlyExitException(14, input);
                        throw eee;
                }
                cnt14++;
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

    // $ANTLR start "DECIMAL_NUMERAL"
    public final void mDECIMAL_NUMERAL() throws RecognitionException {
        try {
            // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1607:9: ( ZERO_DIGIT | NON_ZERO_DIGIT ( DIGIT )* )
            int alt16=2;
            int LA16_0 = input.LA(1);

            if ( (LA16_0=='0') ) {
                alt16=1;
            }
            else if ( ((LA16_0>='1' && LA16_0<='9')) ) {
                alt16=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 16, 0, input);

                throw nvae;
            }
            switch (alt16) {
                case 1 :
                    // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1608:9: ZERO_DIGIT
                    {
                    mZERO_DIGIT(); 

                    }
                    break;
                case 2 :
                    // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1609:11: NON_ZERO_DIGIT ( DIGIT )*
                    {
                    mNON_ZERO_DIGIT(); 
                    // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1609:26: ( DIGIT )*
                    loop15:
                    do {
                        int alt15=2;
                        int LA15_0 = input.LA(1);

                        if ( ((LA15_0>='0' && LA15_0<='9')) ) {
                            alt15=1;
                        }


                        switch (alt15) {
                    	case 1 :
                    	    // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1609:26: DIGIT
                    	    {
                    	    mDIGIT(); 

                    	    }
                    	    break;

                    	default :
                    	    break loop15;
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
            // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1614:9: ( ZERO_DIGIT | NON_ZERO_DIGIT )
            // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:
            {
            if ( (input.LA(1)>='0' && input.LA(1)<='9') ) {
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
    // $ANTLR end "DIGIT"

    // $ANTLR start "ZERO_DIGIT"
    public final void mZERO_DIGIT() throws RecognitionException {
        try {
            // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1621:9: ( '0' )
            // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1622:9: '0'
            {
            match('0'); 

            }

        }
        finally {
        }
    }
    // $ANTLR end "ZERO_DIGIT"

    // $ANTLR start "NON_ZERO_DIGIT"
    public final void mNON_ZERO_DIGIT() throws RecognitionException {
        try {
            // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1627:9: ( '1' .. '9' )
            // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1628:9: '1' .. '9'
            {
            matchRange('1','9'); 

            }

        }
        finally {
        }
    }
    // $ANTLR end "NON_ZERO_DIGIT"

    // $ANTLR start "PLUS"
    public final void mPLUS() throws RecognitionException {
        try {
            // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1633:9: ( '+' )
            // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1634:9: '+'
            {
            match('+'); 

            }

        }
        finally {
        }
    }
    // $ANTLR end "PLUS"

    // $ANTLR start "MINUS"
    public final void mMINUS() throws RecognitionException {
        try {
            // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1639:9: ( '-' )
            // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1640:9: '-'
            {
            match('-'); 

            }

        }
        finally {
        }
    }
    // $ANTLR end "MINUS"

    // $ANTLR start "E"
    public final void mE() throws RecognitionException {
        try {
            // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1645:9: ( ( 'e' | 'E' ) )
            // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1646:9: ( 'e' | 'E' )
            {
            if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
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
    // $ANTLR end "E"

    // $ANTLR start "EXPONENT"
    public final void mEXPONENT() throws RecognitionException {
        try {
            // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1658:9: ( E SIGNED_INTEGER )
            // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1659:9: E SIGNED_INTEGER
            {
            mE(); 
            mSIGNED_INTEGER(); 

            }

        }
        finally {
        }
    }
    // $ANTLR end "EXPONENT"

    // $ANTLR start "SIGNED_INTEGER"
    public final void mSIGNED_INTEGER() throws RecognitionException {
        try {
            // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1664:9: ( ( PLUS | MINUS )? ( DIGIT )+ )
            // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1665:9: ( PLUS | MINUS )? ( DIGIT )+
            {
            // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1665:9: ( PLUS | MINUS )?
            int alt17=2;
            int LA17_0 = input.LA(1);

            if ( (LA17_0=='+'||LA17_0=='-') ) {
                alt17=1;
            }
            switch (alt17) {
                case 1 :
                    // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:
                    {
                    if ( input.LA(1)=='+'||input.LA(1)=='-' ) {
                        input.consume();

                    }
                    else {
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;}


                    }
                    break;

            }

            // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1669:9: ( DIGIT )+
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
            	    // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1669:9: DIGIT
            	    {
            	    mDIGIT(); 

            	    }
            	    break;

            	default :
            	    if ( cnt18 >= 1 ) break loop18;
                        EarlyExitException eee =
                            new EarlyExitException(18, input);
                        throw eee;
                }
                cnt18++;
            } while (true);


            }

        }
        finally {
        }
    }
    // $ANTLR end "SIGNED_INTEGER"

    public void mTokens() throws RecognitionException {
        // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1:8: ( QUOTED_STRING | SELECT | AS | FROM | JOIN | INNER | LEFT | OUTER | ON | WHERE | OR | AND | NOT | IN | LIKE | IS | NULL | ANY | CONTAINS | IN_FOLDER | IN_TREE | ORDER | BY | ASC | DESC | TIMESTAMP | TRUE | FALSE | SCORE | LPAREN | RPAREN | STAR | COMMA | DOTSTAR | DOT | DOTDOT | EQUALS | TILDA | NOTEQUALS | GREATERTHAN | LESSTHAN | GREATERTHANOREQUALS | LESSTHANOREQUALS | COLON | DOUBLE_QUOTE | DECIMAL_INTEGER_LITERAL | FLOATING_POINT_LITERAL | ID | WS )
        int alt19=49;
        alt19 = dfa19.predict(input);
        switch (alt19) {
            case 1 :
                // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1:10: QUOTED_STRING
                {
                mQUOTED_STRING(); 

                }
                break;
            case 2 :
                // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1:24: SELECT
                {
                mSELECT(); 

                }
                break;
            case 3 :
                // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1:31: AS
                {
                mAS(); 

                }
                break;
            case 4 :
                // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1:34: FROM
                {
                mFROM(); 

                }
                break;
            case 5 :
                // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1:39: JOIN
                {
                mJOIN(); 

                }
                break;
            case 6 :
                // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1:44: INNER
                {
                mINNER(); 

                }
                break;
            case 7 :
                // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1:50: LEFT
                {
                mLEFT(); 

                }
                break;
            case 8 :
                // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1:55: OUTER
                {
                mOUTER(); 

                }
                break;
            case 9 :
                // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1:61: ON
                {
                mON(); 

                }
                break;
            case 10 :
                // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1:64: WHERE
                {
                mWHERE(); 

                }
                break;
            case 11 :
                // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1:70: OR
                {
                mOR(); 

                }
                break;
            case 12 :
                // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1:73: AND
                {
                mAND(); 

                }
                break;
            case 13 :
                // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1:77: NOT
                {
                mNOT(); 

                }
                break;
            case 14 :
                // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1:81: IN
                {
                mIN(); 

                }
                break;
            case 15 :
                // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1:84: LIKE
                {
                mLIKE(); 

                }
                break;
            case 16 :
                // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1:89: IS
                {
                mIS(); 

                }
                break;
            case 17 :
                // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1:92: NULL
                {
                mNULL(); 

                }
                break;
            case 18 :
                // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1:97: ANY
                {
                mANY(); 

                }
                break;
            case 19 :
                // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1:101: CONTAINS
                {
                mCONTAINS(); 

                }
                break;
            case 20 :
                // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1:110: IN_FOLDER
                {
                mIN_FOLDER(); 

                }
                break;
            case 21 :
                // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1:120: IN_TREE
                {
                mIN_TREE(); 

                }
                break;
            case 22 :
                // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1:128: ORDER
                {
                mORDER(); 

                }
                break;
            case 23 :
                // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1:134: BY
                {
                mBY(); 

                }
                break;
            case 24 :
                // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1:137: ASC
                {
                mASC(); 

                }
                break;
            case 25 :
                // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1:141: DESC
                {
                mDESC(); 

                }
                break;
            case 26 :
                // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1:146: TIMESTAMP
                {
                mTIMESTAMP(); 

                }
                break;
            case 27 :
                // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1:156: TRUE
                {
                mTRUE(); 

                }
                break;
            case 28 :
                // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1:161: FALSE
                {
                mFALSE(); 

                }
                break;
            case 29 :
                // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1:167: SCORE
                {
                mSCORE(); 

                }
                break;
            case 30 :
                // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1:173: LPAREN
                {
                mLPAREN(); 

                }
                break;
            case 31 :
                // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1:180: RPAREN
                {
                mRPAREN(); 

                }
                break;
            case 32 :
                // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1:187: STAR
                {
                mSTAR(); 

                }
                break;
            case 33 :
                // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1:192: COMMA
                {
                mCOMMA(); 

                }
                break;
            case 34 :
                // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1:198: DOTSTAR
                {
                mDOTSTAR(); 

                }
                break;
            case 35 :
                // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1:206: DOT
                {
                mDOT(); 

                }
                break;
            case 36 :
                // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1:210: DOTDOT
                {
                mDOTDOT(); 

                }
                break;
            case 37 :
                // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1:217: EQUALS
                {
                mEQUALS(); 

                }
                break;
            case 38 :
                // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1:224: TILDA
                {
                mTILDA(); 

                }
                break;
            case 39 :
                // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1:230: NOTEQUALS
                {
                mNOTEQUALS(); 

                }
                break;
            case 40 :
                // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1:240: GREATERTHAN
                {
                mGREATERTHAN(); 

                }
                break;
            case 41 :
                // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1:252: LESSTHAN
                {
                mLESSTHAN(); 

                }
                break;
            case 42 :
                // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1:261: GREATERTHANOREQUALS
                {
                mGREATERTHANOREQUALS(); 

                }
                break;
            case 43 :
                // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1:281: LESSTHANOREQUALS
                {
                mLESSTHANOREQUALS(); 

                }
                break;
            case 44 :
                // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1:298: COLON
                {
                mCOLON(); 

                }
                break;
            case 45 :
                // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1:304: DOUBLE_QUOTE
                {
                mDOUBLE_QUOTE(); 

                }
                break;
            case 46 :
                // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1:317: DECIMAL_INTEGER_LITERAL
                {
                mDECIMAL_INTEGER_LITERAL(); 

                }
                break;
            case 47 :
                // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1:341: FLOATING_POINT_LITERAL
                {
                mFLOATING_POINT_LITERAL(); 

                }
                break;
            case 48 :
                // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1:364: ID
                {
                mID(); 

                }
                break;
            case 49 :
                // W:\\alfresco\\BRANCHES\\V3.3\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:1:367: WS
                {
                mWS(); 

                }
                break;

        }

    }


    protected DFA12 dfa12 = new DFA12(this);
    protected DFA19 dfa19 = new DFA19(this);
    static final String DFA12_eotS =
        "\6\uffff";
    static final String DFA12_eofS =
        "\6\uffff";
    static final String DFA12_minS =
        "\1\53\2\56\3\uffff";
    static final String DFA12_maxS =
        "\2\71\1\145\3\uffff";
    static final String DFA12_acceptS =
        "\3\uffff\1\2\1\3\1\1";
    static final String DFA12_specialS =
        "\6\uffff}>";
    static final String[] DFA12_transitionS = {
            "\1\1\1\uffff\1\1\1\3\1\uffff\12\2",
            "\1\3\1\uffff\12\2",
            "\1\5\1\uffff\12\2\13\uffff\1\4\37\uffff\1\4",
            "",
            "",
            ""
    };

    static final short[] DFA12_eot = DFA.unpackEncodedString(DFA12_eotS);
    static final short[] DFA12_eof = DFA.unpackEncodedString(DFA12_eofS);
    static final char[] DFA12_min = DFA.unpackEncodedStringToUnsignedChars(DFA12_minS);
    static final char[] DFA12_max = DFA.unpackEncodedStringToUnsignedChars(DFA12_maxS);
    static final short[] DFA12_accept = DFA.unpackEncodedString(DFA12_acceptS);
    static final short[] DFA12_special = DFA.unpackEncodedString(DFA12_specialS);
    static final short[][] DFA12_transition;

    static {
        int numStates = DFA12_transitionS.length;
        DFA12_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA12_transition[i] = DFA.unpackEncodedString(DFA12_transitionS[i]);
        }
    }

    class DFA12 extends DFA {

        public DFA12(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 12;
            this.eot = DFA12_eot;
            this.eof = DFA12_eof;
            this.min = DFA12_min;
            this.max = DFA12_max;
            this.accept = DFA12_accept;
            this.special = DFA12_special;
            this.transition = DFA12_transition;
        }
        public String getDescription() {
            return "1549:1: FLOATING_POINT_LITERAL : ( ( PLUS | MINUS )? ( DIGIT )+ DOT ( DIGIT )* ( EXPONENT )? | ( PLUS | MINUS )? DOT ( DIGIT )+ ( EXPONENT )? | ( PLUS | MINUS )? ( DIGIT )+ EXPONENT );";
        }
    }
    static final String DFA19_eotS =
        "\2\uffff\15\42\4\uffff\1\71\2\uffff\1\75\1\77\3\uffff\2\101\2\uffff"+
        "\2\42\2\uffff\1\111\4\42\1\121\1\122\3\42\1\126\1\130\4\42\1\135"+
        "\3\42\16\uffff\1\101\2\42\1\143\1\uffff\1\144\1\145\5\42\2\uffff"+
        "\3\42\1\uffff\1\42\1\uffff\1\42\1\161\2\42\1\uffff\5\42\3\uffff"+
        "\1\171\1\42\1\173\3\42\1\177\1\u0080\3\42\1\uffff\1\u0084\1\42\1"+
        "\u0086\1\42\1\u0088\1\42\1\u008a\1\uffff\1\u008b\1\uffff\1\u008c"+
        "\2\42\2\uffff\1\u008f\1\u0090\1\u0091\1\uffff\1\42\1\uffff\1\42"+
        "\1\uffff\1\u0094\3\uffff\2\42\3\uffff\2\42\1\uffff\1\42\1\u009a"+
        "\3\42\1\uffff\1\u009e\1\42\1\u00a0\1\uffff\1\u00a1\2\uffff";
    static final String DFA19_eofS =
        "\u00a2\uffff";
    static final String DFA19_minS =
        "\1\11\1\uffff\15\43\4\uffff\1\52\2\uffff\2\75\2\uffff\3\56\2\uffff"+
        "\2\43\2\uffff\24\43\16\uffff\1\56\3\43\1\uffff\7\43\2\uffff\3\43"+
        "\1\uffff\1\43\1\uffff\4\43\1\uffff\5\43\3\uffff\13\43\1\uffff\7"+
        "\43\1\uffff\1\43\1\uffff\3\43\2\uffff\3\43\1\uffff\1\43\1\uffff"+
        "\1\43\1\uffff\1\43\3\uffff\2\43\3\uffff\2\43\1\uffff\5\43\1\uffff"+
        "\3\43\1\uffff\1\43\2\uffff";
    static final String DFA19_maxS =
        "\1\176\1\uffff\15\172\4\uffff\1\71\2\uffff\1\76\1\75\2\uffff\1"+
        "\71\2\145\2\uffff\2\172\2\uffff\24\172\16\uffff\1\145\3\172\1\uffff"+
        "\7\172\2\uffff\3\172\1\uffff\1\172\1\uffff\4\172\1\uffff\5\172\3"+
        "\uffff\13\172\1\uffff\7\172\1\uffff\1\172\1\uffff\3\172\2\uffff"+
        "\3\172\1\uffff\1\172\1\uffff\1\172\1\uffff\1\172\3\uffff\2\172\3"+
        "\uffff\2\172\1\uffff\5\172\1\uffff\3\172\1\uffff\1\172\2\uffff";
    static final String DFA19_acceptS =
        "\1\uffff\1\1\15\uffff\1\36\1\37\1\40\1\41\1\uffff\1\45\1\46\2\uffff"+
        "\1\54\1\55\3\uffff\1\60\1\61\2\uffff\2\60\24\uffff\1\42\1\44\1\43"+
        "\1\57\1\47\1\53\1\51\1\52\1\50\1\57\1\56\3\57\4\uffff\1\3\7\uffff"+
        "\1\16\1\20\3\uffff\1\11\1\uffff\1\13\4\uffff\1\27\5\uffff\1\30\1"+
        "\14\1\22\13\uffff\1\15\7\uffff\1\4\1\uffff\1\5\3\uffff\1\7\1\17"+
        "\3\uffff\1\21\1\uffff\1\31\1\uffff\1\33\1\uffff\1\35\1\34\1\6\2"+
        "\uffff\1\10\1\26\1\12\2\uffff\1\2\5\uffff\1\25\3\uffff\1\23\1\uffff"+
        "\1\24\1\32";
    static final String DFA19_specialS =
        "\u00a2\uffff}>";
    static final String[] DFA19_transitionS = {
            "\2\36\2\uffff\1\36\22\uffff\1\36\1\uffff\1\31\4\uffff\1\1\1"+
            "\17\1\20\1\21\1\32\1\22\1\32\1\23\1\uffff\1\33\11\34\1\30\1"+
            "\uffff\1\26\1\24\1\27\2\uffff\1\3\1\14\1\13\1\15\1\35\1\4\2"+
            "\35\1\6\1\5\1\35\1\7\1\35\1\12\1\10\3\35\1\2\1\16\2\35\1\11"+
            "\3\35\4\uffff\1\35\1\uffff\1\3\1\14\1\13\1\15\1\35\1\4\2\35"+
            "\1\6\1\5\1\35\1\7\1\35\1\12\1\10\3\35\1\2\1\16\2\35\1\11\3\35"+
            "\3\uffff\1\25",
            "",
            "\2\41\13\uffff\13\41\6\uffff\2\41\1\40\1\41\1\37\25\41\4\uffff"+
            "\1\41\1\uffff\2\41\1\40\1\41\1\37\25\41",
            "\2\41\13\uffff\13\41\6\uffff\15\41\1\44\4\41\1\43\7\41\4\uffff"+
            "\1\41\1\uffff\15\41\1\44\4\41\1\43\7\41",
            "\2\41\13\uffff\13\41\6\uffff\1\46\20\41\1\45\10\41\4\uffff"+
            "\1\41\1\uffff\1\46\20\41\1\45\10\41",
            "\2\41\13\uffff\13\41\6\uffff\16\41\1\47\13\41\4\uffff\1\41"+
            "\1\uffff\16\41\1\47\13\41",
            "\2\41\13\uffff\13\41\6\uffff\15\41\1\50\4\41\1\51\7\41\4\uffff"+
            "\1\41\1\uffff\15\41\1\50\4\41\1\51\7\41",
            "\2\41\13\uffff\13\41\6\uffff\4\41\1\52\3\41\1\53\21\41\4\uffff"+
            "\1\41\1\uffff\4\41\1\52\3\41\1\53\21\41",
            "\2\41\13\uffff\13\41\6\uffff\15\41\1\55\3\41\1\56\2\41\1\54"+
            "\5\41\4\uffff\1\41\1\uffff\15\41\1\55\3\41\1\56\2\41\1\54\5"+
            "\41",
            "\2\41\13\uffff\13\41\6\uffff\7\41\1\57\22\41\4\uffff\1\41"+
            "\1\uffff\7\41\1\57\22\41",
            "\2\41\13\uffff\13\41\6\uffff\16\41\1\60\5\41\1\61\5\41\4\uffff"+
            "\1\41\1\uffff\16\41\1\60\5\41\1\61\5\41",
            "\2\41\13\uffff\13\41\6\uffff\16\41\1\62\13\41\4\uffff\1\41"+
            "\1\uffff\16\41\1\62\13\41",
            "\2\41\13\uffff\13\41\6\uffff\30\41\1\63\1\41\4\uffff\1\41"+
            "\1\uffff\30\41\1\63\1\41",
            "\2\41\13\uffff\13\41\6\uffff\4\41\1\64\25\41\4\uffff\1\41"+
            "\1\uffff\4\41\1\64\25\41",
            "\2\41\13\uffff\13\41\6\uffff\10\41\1\65\10\41\1\66\10\41\4"+
            "\uffff\1\41\1\uffff\10\41\1\65\10\41\1\66\10\41",
            "",
            "",
            "",
            "",
            "\1\67\3\uffff\1\70\1\uffff\12\72",
            "",
            "",
            "\1\74\1\73",
            "\1\76",
            "",
            "",
            "\1\100\1\uffff\1\33\11\34",
            "\1\102\1\uffff\12\103\13\uffff\1\104\37\uffff\1\104",
            "\1\102\1\uffff\12\105\13\uffff\1\104\37\uffff\1\104",
            "",
            "",
            "\2\41\13\uffff\13\41\6\uffff\13\41\1\106\16\41\4\uffff\1\41"+
            "\1\uffff\13\41\1\106\16\41",
            "\2\41\13\uffff\13\41\6\uffff\16\41\1\107\13\41\4\uffff\1\41"+
            "\1\uffff\16\41\1\107\13\41",
            "",
            "",
            "\2\41\13\uffff\13\41\6\uffff\2\41\1\110\27\41\4\uffff\1\41"+
            "\1\uffff\2\41\1\110\27\41",
            "\2\41\13\uffff\13\41\6\uffff\3\41\1\112\24\41\1\113\1\41\4"+
            "\uffff\1\41\1\uffff\3\41\1\112\24\41\1\113\1\41",
            "\2\41\13\uffff\13\41\6\uffff\16\41\1\114\13\41\4\uffff\1\41"+
            "\1\uffff\16\41\1\114\13\41",
            "\2\41\13\uffff\13\41\6\uffff\13\41\1\115\16\41\4\uffff\1\41"+
            "\1\uffff\13\41\1\115\16\41",
            "\2\41\13\uffff\13\41\6\uffff\10\41\1\116\21\41\4\uffff\1\41"+
            "\1\uffff\10\41\1\116\21\41",
            "\2\41\13\uffff\13\41\6\uffff\15\41\1\117\14\41\4\uffff\1\120"+
            "\1\uffff\15\41\1\117\14\41",
            "\2\41\13\uffff\13\41\6\uffff\32\41\4\uffff\1\41\1\uffff\32"+
            "\41",
            "\2\41\13\uffff\13\41\6\uffff\5\41\1\123\24\41\4\uffff\1\41"+
            "\1\uffff\5\41\1\123\24\41",
            "\2\41\13\uffff\13\41\6\uffff\12\41\1\124\17\41\4\uffff\1\41"+
            "\1\uffff\12\41\1\124\17\41",
            "\2\41\13\uffff\13\41\6\uffff\23\41\1\125\6\41\4\uffff\1\41"+
            "\1\uffff\23\41\1\125\6\41",
            "\2\41\13\uffff\13\41\6\uffff\32\41\4\uffff\1\41\1\uffff\32"+
            "\41",
            "\2\41\13\uffff\13\41\6\uffff\3\41\1\127\26\41\4\uffff\1\41"+
            "\1\uffff\3\41\1\127\26\41",
            "\2\41\13\uffff\13\41\6\uffff\4\41\1\131\25\41\4\uffff\1\41"+
            "\1\uffff\4\41\1\131\25\41",
            "\2\41\13\uffff\13\41\6\uffff\23\41\1\132\6\41\4\uffff\1\41"+
            "\1\uffff\23\41\1\132\6\41",
            "\2\41\13\uffff\13\41\6\uffff\13\41\1\133\16\41\4\uffff\1\41"+
            "\1\uffff\13\41\1\133\16\41",
            "\2\41\13\uffff\13\41\6\uffff\15\41\1\134\14\41\4\uffff\1\41"+
            "\1\uffff\15\41\1\134\14\41",
            "\2\41\13\uffff\13\41\6\uffff\32\41\4\uffff\1\41\1\uffff\32"+
            "\41",
            "\2\41\13\uffff\13\41\6\uffff\22\41\1\136\7\41\4\uffff\1\41"+
            "\1\uffff\22\41\1\136\7\41",
            "\2\41\13\uffff\13\41\6\uffff\14\41\1\137\15\41\4\uffff\1\41"+
            "\1\uffff\14\41\1\137\15\41",
            "\2\41\13\uffff\13\41\6\uffff\24\41\1\140\5\41\4\uffff\1\41"+
            "\1\uffff\24\41\1\140\5\41",
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
            "",
            "",
            "",
            "\1\102\1\uffff\12\105\13\uffff\1\104\37\uffff\1\104",
            "\2\41\13\uffff\13\41\6\uffff\4\41\1\141\25\41\4\uffff\1\41"+
            "\1\uffff\4\41\1\141\25\41",
            "\2\41\13\uffff\13\41\6\uffff\21\41\1\142\10\41\4\uffff\1\41"+
            "\1\uffff\21\41\1\142\10\41",
            "\2\41\13\uffff\13\41\6\uffff\32\41\4\uffff\1\41\1\uffff\32"+
            "\41",
            "",
            "\2\41\13\uffff\13\41\6\uffff\32\41\4\uffff\1\41\1\uffff\32"+
            "\41",
            "\2\41\13\uffff\13\41\6\uffff\32\41\4\uffff\1\41\1\uffff\32"+
            "\41",
            "\2\41\13\uffff\13\41\6\uffff\14\41\1\146\15\41\4\uffff\1\41"+
            "\1\uffff\14\41\1\146\15\41",
            "\2\41\13\uffff\13\41\6\uffff\22\41\1\147\7\41\4\uffff\1\41"+
            "\1\uffff\22\41\1\147\7\41",
            "\2\41\13\uffff\13\41\6\uffff\15\41\1\150\14\41\4\uffff\1\41"+
            "\1\uffff\15\41\1\150\14\41",
            "\2\41\13\uffff\13\41\6\uffff\4\41\1\151\25\41\4\uffff\1\41"+
            "\1\uffff\4\41\1\151\25\41",
            "\2\41\13\uffff\13\41\6\uffff\5\41\1\152\15\41\1\153\6\41\4"+
            "\uffff\1\41\1\uffff\5\41\1\152\15\41\1\153\6\41",
            "",
            "",
            "\2\41\13\uffff\13\41\6\uffff\23\41\1\154\6\41\4\uffff\1\41"+
            "\1\uffff\23\41\1\154\6\41",
            "\2\41\13\uffff\13\41\6\uffff\4\41\1\155\25\41\4\uffff\1\41"+
            "\1\uffff\4\41\1\155\25\41",
            "\2\41\13\uffff\13\41\6\uffff\4\41\1\156\25\41\4\uffff\1\41"+
            "\1\uffff\4\41\1\156\25\41",
            "",
            "\2\41\13\uffff\13\41\6\uffff\4\41\1\157\25\41\4\uffff\1\41"+
            "\1\uffff\4\41\1\157\25\41",
            "",
            "\2\41\13\uffff\13\41\6\uffff\21\41\1\160\10\41\4\uffff\1\41"+
            "\1\uffff\21\41\1\160\10\41",
            "\2\41\13\uffff\13\41\6\uffff\32\41\4\uffff\1\41\1\uffff\32"+
            "\41",
            "\2\41\13\uffff\13\41\6\uffff\13\41\1\162\16\41\4\uffff\1\41"+
            "\1\uffff\13\41\1\162\16\41",
            "\2\41\13\uffff\13\41\6\uffff\23\41\1\163\6\41\4\uffff\1\41"+
            "\1\uffff\23\41\1\163\6\41",
            "",
            "\2\41\13\uffff\13\41\6\uffff\2\41\1\164\27\41\4\uffff\1\41"+
            "\1\uffff\2\41\1\164\27\41",
            "\2\41\13\uffff\13\41\6\uffff\4\41\1\165\25\41\4\uffff\1\41"+
            "\1\uffff\4\41\1\165\25\41",
            "\2\41\13\uffff\13\41\6\uffff\4\41\1\166\25\41\4\uffff\1\41"+
            "\1\uffff\4\41\1\166\25\41",
            "\2\41\13\uffff\13\41\6\uffff\2\41\1\167\27\41\4\uffff\1\41"+
            "\1\uffff\2\41\1\167\27\41",
            "\2\41\13\uffff\13\41\6\uffff\4\41\1\170\25\41\4\uffff\1\41"+
            "\1\uffff\4\41\1\170\25\41",
            "",
            "",
            "",
            "\2\41\13\uffff\13\41\6\uffff\32\41\4\uffff\1\41\1\uffff\32"+
            "\41",
            "\2\41\13\uffff\13\41\6\uffff\4\41\1\172\25\41\4\uffff\1\41"+
            "\1\uffff\4\41\1\172\25\41",
            "\2\41\13\uffff\13\41\6\uffff\32\41\4\uffff\1\41\1\uffff\32"+
            "\41",
            "\2\41\13\uffff\13\41\6\uffff\21\41\1\174\10\41\4\uffff\1\41"+
            "\1\uffff\21\41\1\174\10\41",
            "\2\41\13\uffff\13\41\6\uffff\16\41\1\175\13\41\4\uffff\1\41"+
            "\1\uffff\16\41\1\175\13\41",
            "\2\41\13\uffff\13\41\6\uffff\21\41\1\176\10\41\4\uffff\1\41"+
            "\1\uffff\21\41\1\176\10\41",
            "\2\41\13\uffff\13\41\6\uffff\32\41\4\uffff\1\41\1\uffff\32"+
            "\41",
            "\2\41\13\uffff\13\41\6\uffff\32\41\4\uffff\1\41\1\uffff\32"+
            "\41",
            "\2\41\13\uffff\13\41\6\uffff\21\41\1\u0081\10\41\4\uffff\1"+
            "\41\1\uffff\21\41\1\u0081\10\41",
            "\2\41\13\uffff\13\41\6\uffff\21\41\1\u0082\10\41\4\uffff\1"+
            "\41\1\uffff\21\41\1\u0082\10\41",
            "\2\41\13\uffff\13\41\6\uffff\4\41\1\u0083\25\41\4\uffff\1"+
            "\41\1\uffff\4\41\1\u0083\25\41",
            "",
            "\2\41\13\uffff\13\41\6\uffff\32\41\4\uffff\1\41\1\uffff\32"+
            "\41",
            "\2\41\13\uffff\13\41\6\uffff\1\u0085\31\41\4\uffff\1\41\1"+
            "\uffff\1\u0085\31\41",
            "\2\41\13\uffff\13\41\6\uffff\32\41\4\uffff\1\41\1\uffff\32"+
            "\41",
            "\2\41\13\uffff\13\41\6\uffff\22\41\1\u0087\7\41\4\uffff\1"+
            "\41\1\uffff\22\41\1\u0087\7\41",
            "\2\41\13\uffff\13\41\6\uffff\32\41\4\uffff\1\41\1\uffff\32"+
            "\41",
            "\2\41\13\uffff\13\41\6\uffff\23\41\1\u0089\6\41\4\uffff\1"+
            "\41\1\uffff\23\41\1\u0089\6\41",
            "\2\41\13\uffff\13\41\6\uffff\32\41\4\uffff\1\41\1\uffff\32"+
            "\41",
            "",
            "\2\41\13\uffff\13\41\6\uffff\32\41\4\uffff\1\41\1\uffff\32"+
            "\41",
            "",
            "\2\41\13\uffff\13\41\6\uffff\32\41\4\uffff\1\41\1\uffff\32"+
            "\41",
            "\2\41\13\uffff\13\41\6\uffff\13\41\1\u008d\16\41\4\uffff\1"+
            "\41\1\uffff\13\41\1\u008d\16\41",
            "\2\41\13\uffff\13\41\6\uffff\4\41\1\u008e\25\41\4\uffff\1"+
            "\41\1\uffff\4\41\1\u008e\25\41",
            "",
            "",
            "\2\41\13\uffff\13\41\6\uffff\32\41\4\uffff\1\41\1\uffff\32"+
            "\41",
            "\2\41\13\uffff\13\41\6\uffff\32\41\4\uffff\1\41\1\uffff\32"+
            "\41",
            "\2\41\13\uffff\13\41\6\uffff\32\41\4\uffff\1\41\1\uffff\32"+
            "\41",
            "",
            "\2\41\13\uffff\13\41\6\uffff\10\41\1\u0092\21\41\4\uffff\1"+
            "\41\1\uffff\10\41\1\u0092\21\41",
            "",
            "\2\41\13\uffff\13\41\6\uffff\23\41\1\u0093\6\41\4\uffff\1"+
            "\41\1\uffff\23\41\1\u0093\6\41",
            "",
            "\2\41\13\uffff\13\41\6\uffff\32\41\4\uffff\1\41\1\uffff\32"+
            "\41",
            "",
            "",
            "",
            "\2\41\13\uffff\13\41\6\uffff\3\41\1\u0095\26\41\4\uffff\1"+
            "\41\1\uffff\3\41\1\u0095\26\41",
            "\2\41\13\uffff\13\41\6\uffff\4\41\1\u0096\25\41\4\uffff\1"+
            "\41\1\uffff\4\41\1\u0096\25\41",
            "",
            "",
            "",
            "\2\41\13\uffff\13\41\6\uffff\15\41\1\u0097\14\41\4\uffff\1"+
            "\41\1\uffff\15\41\1\u0097\14\41",
            "\2\41\13\uffff\13\41\6\uffff\1\u0098\31\41\4\uffff\1\41\1"+
            "\uffff\1\u0098\31\41",
            "",
            "\2\41\13\uffff\13\41\6\uffff\4\41\1\u0099\25\41\4\uffff\1"+
            "\41\1\uffff\4\41\1\u0099\25\41",
            "\2\41\13\uffff\13\41\6\uffff\32\41\4\uffff\1\41\1\uffff\32"+
            "\41",
            "\2\41\13\uffff\13\41\6\uffff\22\41\1\u009b\7\41\4\uffff\1"+
            "\41\1\uffff\22\41\1\u009b\7\41",
            "\2\41\13\uffff\13\41\6\uffff\14\41\1\u009c\15\41\4\uffff\1"+
            "\41\1\uffff\14\41\1\u009c\15\41",
            "\2\41\13\uffff\13\41\6\uffff\21\41\1\u009d\10\41\4\uffff\1"+
            "\41\1\uffff\21\41\1\u009d\10\41",
            "",
            "\2\41\13\uffff\13\41\6\uffff\32\41\4\uffff\1\41\1\uffff\32"+
            "\41",
            "\2\41\13\uffff\13\41\6\uffff\17\41\1\u009f\12\41\4\uffff\1"+
            "\41\1\uffff\17\41\1\u009f\12\41",
            "\2\41\13\uffff\13\41\6\uffff\32\41\4\uffff\1\41\1\uffff\32"+
            "\41",
            "",
            "\2\41\13\uffff\13\41\6\uffff\32\41\4\uffff\1\41\1\uffff\32"+
            "\41",
            "",
            ""
    };

    static final short[] DFA19_eot = DFA.unpackEncodedString(DFA19_eotS);
    static final short[] DFA19_eof = DFA.unpackEncodedString(DFA19_eofS);
    static final char[] DFA19_min = DFA.unpackEncodedStringToUnsignedChars(DFA19_minS);
    static final char[] DFA19_max = DFA.unpackEncodedStringToUnsignedChars(DFA19_maxS);
    static final short[] DFA19_accept = DFA.unpackEncodedString(DFA19_acceptS);
    static final short[] DFA19_special = DFA.unpackEncodedString(DFA19_specialS);
    static final short[][] DFA19_transition;

    static {
        int numStates = DFA19_transitionS.length;
        DFA19_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA19_transition[i] = DFA.unpackEncodedString(DFA19_transitionS[i]);
        }
    }

    class DFA19 extends DFA {

        public DFA19(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 19;
            this.eot = DFA19_eot;
            this.eof = DFA19_eof;
            this.min = DFA19_min;
            this.max = DFA19_max;
            this.accept = DFA19_accept;
            this.special = DFA19_special;
            this.transition = DFA19_transition;
        }
        public String getDescription() {
            return "1:1: Tokens : ( QUOTED_STRING | SELECT | AS | FROM | JOIN | INNER | LEFT | OUTER | ON | WHERE | OR | AND | NOT | IN | LIKE | IS | NULL | ANY | CONTAINS | IN_FOLDER | IN_TREE | ORDER | BY | ASC | DESC | TIMESTAMP | TRUE | FALSE | SCORE | LPAREN | RPAREN | STAR | COMMA | DOTSTAR | DOT | DOTDOT | EQUALS | TILDA | NOTEQUALS | GREATERTHAN | LESSTHAN | GREATERTHANOREQUALS | LESSTHANOREQUALS | COLON | DOUBLE_QUOTE | DECIMAL_INTEGER_LITERAL | FLOATING_POINT_LITERAL | ID | WS );";
        }
    }
 

}