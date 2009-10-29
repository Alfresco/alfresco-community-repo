// $ANTLR 3.1.2 /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g 2009-07-22 13:49:03
package org.alfresco.repo.search.impl.parsers;

import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

public class CMISLexer extends Lexer {
    public static final int COMMA=31;
    public static final int LESSTHAN=49;
    public static final int MINUS=77;
    public static final int AS=32;
    public static final int PRED_COMPARISON=18;
    public static final int LIST=23;
    public static final int TABLE_REF=13;
    public static final int DECIMAL_INTEGER_LITERAL=70;
    public static final int INNER=39;
    public static final int QUERY=4;
    public static final int QUALIFIER=9;
    public static final int PRED_CHILD=24;
    public static final int OR=45;
    public static final int GREATERTHANOREQUALS=52;
    public static final int ON=42;
    public static final int DOT=34;
    public static final int GREATERTHAN=50;
    public static final int ORDER=61;
    public static final int AND=46;
    public static final int COLUMN_REF=8;
    public static final int BY=62;
    public static final int SORT_SPECIFICATION=26;
    public static final int FUNCTION=10;
    public static final int LESSTHANOREQUALS=51;
    public static final int STRING_LITERAL=28;
    public static final int SELECT=29;
    public static final int RPAREN=36;
    public static final int CONTAINS=58;
    public static final int DESC=64;
    public static final int ZERO_DIGIT=82;
    public static final int LPAREN=35;
    public static final int DIGIT=79;
    public static final int PLUS=76;
    public static final int LEFT=40;
    public static final int JOIN=38;
    public static final int CONJUNCTION=15;
    public static final int PRED_FTS=22;
    public static final int OUTER=41;
    public static final int FROM=37;
    public static final int ID=67;
    public static final int ALL_COLUMNS=5;
    public static final int PRED_IN=19;
    public static final int PRED_DESCENDANT=25;
    public static final int PRED_EXISTS=20;
    public static final int NON_ZERO_DIGIT=83;
    public static final int NOTEQUALS=48;
    public static final int NEGATION=17;
    public static final int WS=81;
    public static final int IS=55;
    public static final int IN_FOLDER=59;
    public static final int DOUBLE_QUOTE=68;
    public static final int LOWER=72;
    public static final int DOTSTAR=33;
    public static final int ASC=63;
    public static final int SOURCE=11;
    public static final int LIKE=54;
    public static final int PRED_LIKE=21;
    public static final int DISJUNCTION=16;
    public static final int IN=53;
    public static final int ANY=57;
    public static final int FLOATING_POINT_LITERAL=69;
    public static final int DECIMAL_NUMERAL=78;
    public static final int EQUALS=43;
    public static final int IN_TREE=60;
    public static final int TABLE=12;
    public static final int EXPONENT=80;
    public static final int COLUMNS=7;
    public static final int QUOTED_STRING=66;
    public static final int UPPER=71;
    public static final int EOF=-1;
    public static final int NULL=56;
    public static final int PARAMETER=14;
    public static final int COLON=65;
    public static final int DOTDOT=74;
    public static final int STAR=30;
    public static final int NUMERIC_LITERAL=27;
    public static final int COLUMN=6;
    public static final int SCORE=73;
    public static final int NOT=47;
    public static final int SIGNED_INTEGER=85;
    public static final int TILDA=75;
    public static final int WHERE=44;
    public static final int E=84;

    // delegates
    // delegators

    public CMISLexer() {;} 
    public CMISLexer(CharStream input) {
        this(input, new RecognizerSharedState());
    }
    public CMISLexer(CharStream input, RecognizerSharedState state) {
        super(input,state);

    }
    public String getGrammarFileName() { return "/Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g"; }

    // $ANTLR start "QUOTED_STRING"
    public final void mQUOTED_STRING() throws RecognitionException {
        try {
            int _type = QUOTED_STRING;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:507:5: ( '\\'' (~ '\\'' | '\\'\\'' )* '\\'' )
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:507:9: '\\'' (~ '\\'' | '\\'\\'' )* '\\''
            {
            match('\''); 
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:507:14: (~ '\\'' | '\\'\\'' )*
            loop1:
            do {
                int alt1=3;
                int LA1_0 = input.LA(1);

                if ( (LA1_0=='\'') ) {
                    int LA1_1 = input.LA(2);

                    if ( (LA1_1=='\'') ) {
                        alt1=2;
                    }


                }
                else if ( ((LA1_0>='\u0000' && LA1_0<='&')||(LA1_0>='(' && LA1_0<='\uFFFF')) ) {
                    alt1=1;
                }


                switch (alt1) {
            	case 1 :
            	    // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:507:16: ~ '\\''
            	    {
            	    if ( (input.LA(1)>='\u0000' && input.LA(1)<='&')||(input.LA(1)>='(' && input.LA(1)<='\uFFFF') ) {
            	        input.consume();

            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;}


            	    }
            	    break;
            	case 2 :
            	    // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:507:24: '\\'\\''
            	    {
            	    match("''"); 


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
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:510:8: ( ( 'S' | 's' ) ( 'E' | 'e' ) ( 'L' | 'l' ) ( 'E' | 'e' ) ( 'C' | 'c' ) ( 'T' | 't' ) )
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:510:10: ( 'S' | 's' ) ( 'E' | 'e' ) ( 'L' | 'l' ) ( 'E' | 'e' ) ( 'C' | 'c' ) ( 'T' | 't' )
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
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:511:4: ( ( 'A' | 'a' ) ( 'S' | 's' ) )
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:511:6: ( 'A' | 'a' ) ( 'S' | 's' )
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

    // $ANTLR start "UPPER"
    public final void mUPPER() throws RecognitionException {
        try {
            int _type = UPPER;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:512:7: ( ( 'U' | 'u' ) ( 'P' | 'p' ) ( 'P' | 'p' ) ( 'E' | 'e' ) ( 'R' | 'r' ) )
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:512:9: ( 'U' | 'u' ) ( 'P' | 'p' ) ( 'P' | 'p' ) ( 'E' | 'e' ) ( 'R' | 'r' )
            {
            if ( input.LA(1)=='U'||input.LA(1)=='u' ) {
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

            if ( input.LA(1)=='P'||input.LA(1)=='p' ) {
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
    // $ANTLR end "UPPER"

    // $ANTLR start "LOWER"
    public final void mLOWER() throws RecognitionException {
        try {
            int _type = LOWER;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:513:7: ( ( 'L' | 'l' ) ( 'O' | 'o' ) ( 'W' | 'w' ) ( 'E' | 'e' ) ( 'R' | 'r' ) )
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:513:9: ( 'L' | 'l' ) ( 'O' | 'o' ) ( 'W' | 'w' ) ( 'E' | 'e' ) ( 'R' | 'r' )
            {
            if ( input.LA(1)=='L'||input.LA(1)=='l' ) {
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

            if ( input.LA(1)=='W'||input.LA(1)=='w' ) {
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
    // $ANTLR end "LOWER"

    // $ANTLR start "FROM"
    public final void mFROM() throws RecognitionException {
        try {
            int _type = FROM;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:514:6: ( ( 'F' | 'f' ) ( 'R' | 'r' ) ( 'O' | 'o' ) ( 'M' | 'm' ) )
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:514:8: ( 'F' | 'f' ) ( 'R' | 'r' ) ( 'O' | 'o' ) ( 'M' | 'm' )
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
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:515:6: ( ( 'J' | 'j' ) ( 'O' | 'o' ) ( 'I' | 'i' ) ( 'N' | 'n' ) )
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:515:8: ( 'J' | 'j' ) ( 'O' | 'o' ) ( 'I' | 'i' ) ( 'N' | 'n' )
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
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:516:7: ( ( 'I' | 'i' ) ( 'N' | 'n' ) ( 'N' | 'n' ) ( 'E' | 'e' ) ( 'R' | 'r' ) )
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:516:9: ( 'I' | 'i' ) ( 'N' | 'n' ) ( 'N' | 'n' ) ( 'E' | 'e' ) ( 'R' | 'r' )
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
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:517:6: ( ( 'L' | 'l' ) ( 'E' | 'e' ) ( 'F' | 'f' ) ( 'T' | 't' ) )
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:517:8: ( 'L' | 'l' ) ( 'E' | 'e' ) ( 'F' | 'f' ) ( 'T' | 't' )
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
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:518:7: ( ( 'O' | 'o' ) ( 'U' | 'u' ) ( 'T' | 't' ) ( 'E' | 'e' ) ( 'R' | 'r' ) )
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:518:9: ( 'O' | 'o' ) ( 'U' | 'u' ) ( 'T' | 't' ) ( 'E' | 'e' ) ( 'R' | 'r' )
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
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:519:4: ( ( 'O' | 'o' ) ( 'N' | 'n' ) )
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:519:6: ( 'O' | 'o' ) ( 'N' | 'n' )
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
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:520:7: ( ( 'W' | 'w' ) ( 'H' | 'h' ) ( 'E' | 'e' ) ( 'R' | 'r' ) ( 'E' | 'e' ) )
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:520:9: ( 'W' | 'w' ) ( 'H' | 'h' ) ( 'E' | 'e' ) ( 'R' | 'r' ) ( 'E' | 'e' )
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
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:521:4: ( ( 'O' | 'o' ) ( 'R' | 'r' ) )
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:521:6: ( 'O' | 'o' ) ( 'R' | 'r' )
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
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:522:5: ( ( 'A' | 'a' ) ( 'N' | 'n' ) ( 'D' | 'd' ) )
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:522:7: ( 'A' | 'a' ) ( 'N' | 'n' ) ( 'D' | 'd' )
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
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:523:5: ( ( 'N' | 'n' ) ( 'O' | 'o' ) ( 'T' | 't' ) )
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:523:7: ( 'N' | 'n' ) ( 'O' | 'o' ) ( 'T' | 't' )
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
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:524:4: ( ( 'I' | 'i' ) ( 'N' | 'n' ) )
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:524:6: ( 'I' | 'i' ) ( 'N' | 'n' )
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
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:525:6: ( ( 'L' | 'l' ) ( 'I' | 'i' ) ( 'K' | 'k' ) ( 'E' | 'e' ) )
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:525:8: ( 'L' | 'l' ) ( 'I' | 'i' ) ( 'K' | 'k' ) ( 'E' | 'e' )
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
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:526:4: ( ( 'I' | 'i' ) ( 'S' | 's' ) )
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:526:6: ( 'I' | 'i' ) ( 'S' | 's' )
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
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:527:6: ( ( 'N' | 'n' ) ( 'U' | 'u' ) ( 'L' | 'l' ) ( 'L' | 'l' ) )
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:527:8: ( 'N' | 'n' ) ( 'U' | 'u' ) ( 'L' | 'l' ) ( 'L' | 'l' )
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
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:528:5: ( ( 'A' | 'a' ) ( 'N' | 'n' ) ( 'Y' | 'y' ) )
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:528:7: ( 'A' | 'a' ) ( 'N' | 'n' ) ( 'Y' | 'y' )
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
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:529:9: ( ( 'C' | 'c' ) ( 'O' | 'o' ) ( 'N' | 'n' ) ( 'T' | 't' ) ( 'A' | 'a' ) ( 'I' | 'i' ) ( 'N' | 'n' ) ( 'S' | 's' ) )
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:529:11: ( 'C' | 'c' ) ( 'O' | 'o' ) ( 'N' | 'n' ) ( 'T' | 't' ) ( 'A' | 'a' ) ( 'I' | 'i' ) ( 'N' | 'n' ) ( 'S' | 's' )
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
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:531:2: ( ( 'I' | 'i' ) ( 'N' | 'n' ) '_' ( 'F' | 'f' ) ( 'O' | 'o' ) ( 'L' | 'l' ) ( 'D' | 'd' ) ( 'E' | 'e' ) ( 'R' | 'r' ) )
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:531:4: ( 'I' | 'i' ) ( 'N' | 'n' ) '_' ( 'F' | 'f' ) ( 'O' | 'o' ) ( 'L' | 'l' ) ( 'D' | 'd' ) ( 'E' | 'e' ) ( 'R' | 'r' )
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
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:532:9: ( ( 'I' | 'i' ) ( 'N' | 'n' ) '_' ( 'T' | 't' ) ( 'R' | 'r' ) ( 'E' | 'e' ) ( 'E' | 'e' ) )
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:532:11: ( 'I' | 'i' ) ( 'N' | 'n' ) '_' ( 'T' | 't' ) ( 'R' | 'r' ) ( 'E' | 'e' ) ( 'E' | 'e' )
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
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:533:7: ( ( 'O' | 'o' ) ( 'R' | 'r' ) ( 'D' | 'd' ) ( 'E' | 'e' ) ( 'R' | 'r' ) )
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:533:9: ( 'O' | 'o' ) ( 'R' | 'r' ) ( 'D' | 'd' ) ( 'E' | 'e' ) ( 'R' | 'r' )
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
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:534:4: ( ( 'B' | 'b' ) ( 'Y' | 'y' ) )
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:534:6: ( 'B' | 'b' ) ( 'Y' | 'y' )
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
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:535:5: ( ( 'A' | 'a' ) ( 'S' | 's' ) ( 'C' | 'c' ) )
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:535:7: ( 'A' | 'a' ) ( 'S' | 's' ) ( 'C' | 'c' )
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
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:536:6: ( ( 'D' | 'd' ) ( 'E' | 'e' ) ( 'S' | 's' ) ( 'C' | 'c' ) )
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:536:8: ( 'D' | 'd' ) ( 'E' | 'e' ) ( 'S' | 's' ) ( 'C' | 'c' )
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

    // $ANTLR start "SCORE"
    public final void mSCORE() throws RecognitionException {
        try {
            int _type = SCORE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:537:7: ( ( 'S' | 's' ) ( 'C' | 'c' ) ( 'O' | 'o' ) ( 'R' | 'r' ) ( 'E' | 'e' ) )
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:537:9: ( 'S' | 's' ) ( 'C' | 'c' ) ( 'O' | 'o' ) ( 'R' | 'r' ) ( 'E' | 'e' )
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
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:538:8: ( '(' )
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:538:10: '('
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
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:539:8: ( ')' )
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:539:10: ')'
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
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:540:6: ( '*' )
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:540:8: '*'
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
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:541:7: ( ',' )
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:541:9: ','
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
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:542:9: ( '.*' )
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:542:11: '.*'
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
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:543:5: ( '.' )
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:543:7: '.'
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
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:544:8: ( '..' )
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:544:10: '..'
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
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:545:9: ( '=' )
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:545:11: '='
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
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:546:7: ( '~' )
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:546:9: '~'
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
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:548:2: ( '<>' )
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:548:4: '<>'
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
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:550:2: ( '>' )
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:550:4: '>'
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
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:552:2: ( '<' )
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:552:4: '<'
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
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:554:2: ( '>=' )
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:554:4: '>='
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
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:556:2: ( '<=' )
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:556:4: '<='
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
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:557:7: ( ':' )
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:557:9: ':'
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
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:560:2: ( '\"' )
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:560:4: '\"'
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
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:568:9: ( ( PLUS | MINUS )? DECIMAL_NUMERAL )
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:568:11: ( PLUS | MINUS )? DECIMAL_NUMERAL
            {
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:568:11: ( PLUS | MINUS )?
            int alt2=2;
            int LA2_0 = input.LA(1);

            if ( (LA2_0=='+'||LA2_0=='-') ) {
                alt2=1;
            }
            switch (alt2) {
                case 1 :
                    // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:
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
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:575:2: ( ( PLUS | MINUS )? ( DIGIT )+ DOT ( DIGIT )* ( EXPONENT )? | ( PLUS | MINUS )? DOT ( DIGIT )+ ( EXPONENT )? | ( PLUS | MINUS )? ( DIGIT )+ EXPONENT )
            int alt12=3;
            alt12 = dfa12.predict(input);
            switch (alt12) {
                case 1 :
                    // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:575:4: ( PLUS | MINUS )? ( DIGIT )+ DOT ( DIGIT )* ( EXPONENT )?
                    {
                    // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:575:4: ( PLUS | MINUS )?
                    int alt3=2;
                    int LA3_0 = input.LA(1);

                    if ( (LA3_0=='+'||LA3_0=='-') ) {
                        alt3=1;
                    }
                    switch (alt3) {
                        case 1 :
                            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:
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

                    // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:575:22: ( DIGIT )+
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
                    	    // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:575:22: DIGIT
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
                    // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:575:33: ( DIGIT )*
                    loop5:
                    do {
                        int alt5=2;
                        int LA5_0 = input.LA(1);

                        if ( ((LA5_0>='0' && LA5_0<='9')) ) {
                            alt5=1;
                        }


                        switch (alt5) {
                    	case 1 :
                    	    // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:575:33: DIGIT
                    	    {
                    	    mDIGIT(); 

                    	    }
                    	    break;

                    	default :
                    	    break loop5;
                        }
                    } while (true);

                    // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:575:40: ( EXPONENT )?
                    int alt6=2;
                    int LA6_0 = input.LA(1);

                    if ( (LA6_0=='E'||LA6_0=='e') ) {
                        alt6=1;
                    }
                    switch (alt6) {
                        case 1 :
                            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:575:40: EXPONENT
                            {
                            mEXPONENT(); 

                            }
                            break;

                    }


                    }
                    break;
                case 2 :
                    // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:576:4: ( PLUS | MINUS )? DOT ( DIGIT )+ ( EXPONENT )?
                    {
                    // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:576:4: ( PLUS | MINUS )?
                    int alt7=2;
                    int LA7_0 = input.LA(1);

                    if ( (LA7_0=='+'||LA7_0=='-') ) {
                        alt7=1;
                    }
                    switch (alt7) {
                        case 1 :
                            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:
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
                    // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:576:26: ( DIGIT )+
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
                    	    // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:576:26: DIGIT
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

                    // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:576:33: ( EXPONENT )?
                    int alt9=2;
                    int LA9_0 = input.LA(1);

                    if ( (LA9_0=='E'||LA9_0=='e') ) {
                        alt9=1;
                    }
                    switch (alt9) {
                        case 1 :
                            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:576:33: EXPONENT
                            {
                            mEXPONENT(); 

                            }
                            break;

                    }


                    }
                    break;
                case 3 :
                    // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:577:4: ( PLUS | MINUS )? ( DIGIT )+ EXPONENT
                    {
                    // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:577:4: ( PLUS | MINUS )?
                    int alt10=2;
                    int LA10_0 = input.LA(1);

                    if ( (LA10_0=='+'||LA10_0=='-') ) {
                        alt10=1;
                    }
                    switch (alt10) {
                        case 1 :
                            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:
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

                    // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:577:22: ( DIGIT )+
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
                    	    // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:577:22: DIGIT
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
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:584:4: ( ( 'a' .. 'z' | 'A' .. 'Z' | '_' ) ( 'a' .. 'z' | 'A' .. 'Z' | '0' .. '9' | '_' | ':' | '$' | '#' )* )
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:584:6: ( 'a' .. 'z' | 'A' .. 'Z' | '_' ) ( 'a' .. 'z' | 'A' .. 'Z' | '0' .. '9' | '_' | ':' | '$' | '#' )*
            {
            if ( (input.LA(1)>='A' && input.LA(1)<='Z')||input.LA(1)=='_'||(input.LA(1)>='a' && input.LA(1)<='z') ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:584:29: ( 'a' .. 'z' | 'A' .. 'Z' | '0' .. '9' | '_' | ':' | '$' | '#' )*
            loop13:
            do {
                int alt13=2;
                int LA13_0 = input.LA(1);

                if ( ((LA13_0>='#' && LA13_0<='$')||(LA13_0>='0' && LA13_0<=':')||(LA13_0>='A' && LA13_0<='Z')||LA13_0=='_'||(LA13_0>='a' && LA13_0<='z')) ) {
                    alt13=1;
                }


                switch (alt13) {
            	case 1 :
            	    // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:
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
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:586:4: ( ( ' ' | '\\t' | '\\r' | '\\n' )+ )
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:586:6: ( ' ' | '\\t' | '\\r' | '\\n' )+
            {
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:586:6: ( ' ' | '\\t' | '\\r' | '\\n' )+
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
            	    // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:
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
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:595:2: ( ZERO_DIGIT | NON_ZERO_DIGIT ( DIGIT )* )
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
                    // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:595:4: ZERO_DIGIT
                    {
                    mZERO_DIGIT(); 

                    }
                    break;
                case 2 :
                    // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:596:4: NON_ZERO_DIGIT ( DIGIT )*
                    {
                    mNON_ZERO_DIGIT(); 
                    // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:596:19: ( DIGIT )*
                    loop15:
                    do {
                        int alt15=2;
                        int LA15_0 = input.LA(1);

                        if ( ((LA15_0>='0' && LA15_0<='9')) ) {
                            alt15=1;
                        }


                        switch (alt15) {
                    	case 1 :
                    	    // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:596:19: DIGIT
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
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:599:7: ( ZERO_DIGIT | NON_ZERO_DIGIT )
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:
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
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:602:2: ( '0' )
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:602:4: '0'
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
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:605:2: ( '1' .. '9' )
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:605:4: '1' .. '9'
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
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:607:6: ( '+' )
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:607:8: '+'
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
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:609:7: ( '-' )
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:609:9: '-'
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
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:612:3: ( ( 'e' | 'E' ) )
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:612:5: ( 'e' | 'E' )
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
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:623:2: ( E SIGNED_INTEGER )
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:623:4: E SIGNED_INTEGER
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
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:627:2: ( ( PLUS | MINUS )? ( DIGIT )+ )
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:627:4: ( PLUS | MINUS )? ( DIGIT )+
            {
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:627:4: ( PLUS | MINUS )?
            int alt17=2;
            int LA17_0 = input.LA(1);

            if ( (LA17_0=='+'||LA17_0=='-') ) {
                alt17=1;
            }
            switch (alt17) {
                case 1 :
                    // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:
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

            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:627:20: ( DIGIT )+
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
            	    // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:627:20: DIGIT
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
        // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:1:8: ( QUOTED_STRING | SELECT | AS | UPPER | LOWER | FROM | JOIN | INNER | LEFT | OUTER | ON | WHERE | OR | AND | NOT | IN | LIKE | IS | NULL | ANY | CONTAINS | IN_FOLDER | IN_TREE | ORDER | BY | ASC | DESC | SCORE | LPAREN | RPAREN | STAR | COMMA | DOTSTAR | DOT | DOTDOT | EQUALS | TILDA | NOTEQUALS | GREATERTHAN | LESSTHAN | GREATERTHANOREQUALS | LESSTHANOREQUALS | COLON | DOUBLE_QUOTE | DECIMAL_INTEGER_LITERAL | FLOATING_POINT_LITERAL | ID | WS )
        int alt19=48;
        alt19 = dfa19.predict(input);
        switch (alt19) {
            case 1 :
                // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:1:10: QUOTED_STRING
                {
                mQUOTED_STRING(); 

                }
                break;
            case 2 :
                // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:1:24: SELECT
                {
                mSELECT(); 

                }
                break;
            case 3 :
                // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:1:31: AS
                {
                mAS(); 

                }
                break;
            case 4 :
                // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:1:34: UPPER
                {
                mUPPER(); 

                }
                break;
            case 5 :
                // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:1:40: LOWER
                {
                mLOWER(); 

                }
                break;
            case 6 :
                // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:1:46: FROM
                {
                mFROM(); 

                }
                break;
            case 7 :
                // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:1:51: JOIN
                {
                mJOIN(); 

                }
                break;
            case 8 :
                // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:1:56: INNER
                {
                mINNER(); 

                }
                break;
            case 9 :
                // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:1:62: LEFT
                {
                mLEFT(); 

                }
                break;
            case 10 :
                // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:1:67: OUTER
                {
                mOUTER(); 

                }
                break;
            case 11 :
                // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:1:73: ON
                {
                mON(); 

                }
                break;
            case 12 :
                // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:1:76: WHERE
                {
                mWHERE(); 

                }
                break;
            case 13 :
                // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:1:82: OR
                {
                mOR(); 

                }
                break;
            case 14 :
                // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:1:85: AND
                {
                mAND(); 

                }
                break;
            case 15 :
                // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:1:89: NOT
                {
                mNOT(); 

                }
                break;
            case 16 :
                // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:1:93: IN
                {
                mIN(); 

                }
                break;
            case 17 :
                // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:1:96: LIKE
                {
                mLIKE(); 

                }
                break;
            case 18 :
                // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:1:101: IS
                {
                mIS(); 

                }
                break;
            case 19 :
                // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:1:104: NULL
                {
                mNULL(); 

                }
                break;
            case 20 :
                // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:1:109: ANY
                {
                mANY(); 

                }
                break;
            case 21 :
                // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:1:113: CONTAINS
                {
                mCONTAINS(); 

                }
                break;
            case 22 :
                // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:1:122: IN_FOLDER
                {
                mIN_FOLDER(); 

                }
                break;
            case 23 :
                // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:1:132: IN_TREE
                {
                mIN_TREE(); 

                }
                break;
            case 24 :
                // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:1:140: ORDER
                {
                mORDER(); 

                }
                break;
            case 25 :
                // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:1:146: BY
                {
                mBY(); 

                }
                break;
            case 26 :
                // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:1:149: ASC
                {
                mASC(); 

                }
                break;
            case 27 :
                // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:1:153: DESC
                {
                mDESC(); 

                }
                break;
            case 28 :
                // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:1:158: SCORE
                {
                mSCORE(); 

                }
                break;
            case 29 :
                // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:1:164: LPAREN
                {
                mLPAREN(); 

                }
                break;
            case 30 :
                // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:1:171: RPAREN
                {
                mRPAREN(); 

                }
                break;
            case 31 :
                // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:1:178: STAR
                {
                mSTAR(); 

                }
                break;
            case 32 :
                // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:1:183: COMMA
                {
                mCOMMA(); 

                }
                break;
            case 33 :
                // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:1:189: DOTSTAR
                {
                mDOTSTAR(); 

                }
                break;
            case 34 :
                // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:1:197: DOT
                {
                mDOT(); 

                }
                break;
            case 35 :
                // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:1:201: DOTDOT
                {
                mDOTDOT(); 

                }
                break;
            case 36 :
                // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:1:208: EQUALS
                {
                mEQUALS(); 

                }
                break;
            case 37 :
                // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:1:215: TILDA
                {
                mTILDA(); 

                }
                break;
            case 38 :
                // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:1:221: NOTEQUALS
                {
                mNOTEQUALS(); 

                }
                break;
            case 39 :
                // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:1:231: GREATERTHAN
                {
                mGREATERTHAN(); 

                }
                break;
            case 40 :
                // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:1:243: LESSTHAN
                {
                mLESSTHAN(); 

                }
                break;
            case 41 :
                // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:1:252: GREATERTHANOREQUALS
                {
                mGREATERTHANOREQUALS(); 

                }
                break;
            case 42 :
                // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:1:272: LESSTHANOREQUALS
                {
                mLESSTHANOREQUALS(); 

                }
                break;
            case 43 :
                // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:1:289: COLON
                {
                mCOLON(); 

                }
                break;
            case 44 :
                // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:1:295: DOUBLE_QUOTE
                {
                mDOUBLE_QUOTE(); 

                }
                break;
            case 45 :
                // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:1:308: DECIMAL_INTEGER_LITERAL
                {
                mDECIMAL_INTEGER_LITERAL(); 

                }
                break;
            case 46 :
                // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:1:332: FLOATING_POINT_LITERAL
                {
                mFLOATING_POINT_LITERAL(); 

                }
                break;
            case 47 :
                // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:1:355: ID
                {
                mID(); 

                }
                break;
            case 48 :
                // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:1:358: WS
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
        "\3\uffff\1\2\1\1\1\3";
    static final String DFA12_specialS =
        "\6\uffff}>";
    static final String[] DFA12_transitionS = {
            "\1\1\1\uffff\1\1\1\3\1\uffff\12\2",
            "\1\3\1\uffff\12\2",
            "\1\4\1\uffff\12\2\13\uffff\1\5\37\uffff\1\5",
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
            return "574:1: FLOATING_POINT_LITERAL : ( ( PLUS | MINUS )? ( DIGIT )+ DOT ( DIGIT )* ( EXPONENT )? | ( PLUS | MINUS )? DOT ( DIGIT )+ ( EXPONENT )? | ( PLUS | MINUS )? ( DIGIT )+ EXPONENT );";
        }
    }
    static final String DFA19_eotS =
        "\2\uffff\15\35\4\uffff\1\66\2\uffff\1\72\1\74\3\uffff\2\75\2\uffff"+
        "\2\35\1\102\7\35\1\115\1\116\1\35\1\120\1\122\4\35\1\127\1\35\12"+
        "\uffff\1\75\2\35\1\133\1\uffff\1\134\1\135\10\35\2\uffff\1\35\1"+
        "\uffff\1\35\1\uffff\1\35\1\152\2\35\1\uffff\3\35\3\uffff\2\35\1"+
        "\162\1\163\1\164\1\165\6\35\1\uffff\1\174\1\35\1\176\1\35\1\u0080"+
        "\1\u0081\1\u0082\4\uffff\1\u0083\2\35\1\u0086\1\u0087\1\u0088\1"+
        "\uffff\1\35\1\uffff\1\u008a\4\uffff\2\35\3\uffff\1\35\1\uffff\1"+
        "\35\1\u008f\2\35\1\uffff\1\u0092\1\u0093\2\uffff";
    static final String DFA19_eofS =
        "\u0094\uffff";
    static final String DFA19_minS =
        "\1\11\1\uffff\1\103\1\116\1\120\1\105\1\122\1\117\2\116\1\110\2"+
        "\117\1\131\1\105\4\uffff\1\52\2\uffff\2\75\2\uffff\3\56\2\uffff"+
        "\1\114\1\117\1\43\1\104\1\120\1\127\1\106\1\113\1\117\1\111\2\43"+
        "\1\124\2\43\1\105\1\124\1\114\1\116\1\43\1\123\12\uffff\1\56\1\105"+
        "\1\122\1\43\1\uffff\2\43\2\105\1\124\1\105\1\115\1\116\1\105\1\106"+
        "\2\uffff\1\105\1\uffff\1\105\1\uffff\1\122\1\43\1\114\1\124\1\uffff"+
        "\2\103\1\105\3\uffff\2\122\4\43\1\122\1\117\3\122\1\105\1\uffff"+
        "\1\43\1\101\1\43\1\124\3\43\4\uffff\1\43\1\114\1\105\3\43\1\uffff"+
        "\1\111\1\uffff\1\43\4\uffff\1\104\1\105\3\uffff\1\116\1\uffff\1"+
        "\105\1\43\1\123\1\122\1\uffff\2\43\2\uffff";
    static final String DFA19_maxS =
        "\1\176\1\uffff\1\145\1\163\1\160\1\157\1\162\1\157\1\163\1\165\1"+
        "\150\1\165\1\157\1\171\1\145\4\uffff\1\71\2\uffff\1\76\1\75\2\uffff"+
        "\1\71\2\145\2\uffff\1\154\1\157\1\172\1\171\1\160\1\167\1\146\1"+
        "\153\1\157\1\151\2\172\1\164\2\172\1\145\1\164\1\154\1\156\1\172"+
        "\1\163\12\uffff\2\145\1\162\1\172\1\uffff\2\172\2\145\1\164\1\145"+
        "\1\155\1\156\1\145\1\164\2\uffff\1\145\1\uffff\1\145\1\uffff\1\162"+
        "\1\172\1\154\1\164\1\uffff\2\143\1\145\3\uffff\2\162\4\172\1\162"+
        "\1\157\3\162\1\145\1\uffff\1\172\1\141\1\172\1\164\3\172\4\uffff"+
        "\1\172\1\154\1\145\3\172\1\uffff\1\151\1\uffff\1\172\4\uffff\1\144"+
        "\1\145\3\uffff\1\156\1\uffff\1\145\1\172\1\163\1\162\1\uffff\2\172"+
        "\2\uffff";
    static final String DFA19_acceptS =
        "\1\uffff\1\1\15\uffff\1\35\1\36\1\37\1\40\1\uffff\1\44\1\45\2\uffff"+
        "\1\53\1\54\3\uffff\1\57\1\60\25\uffff\1\41\1\43\1\42\1\56\1\46\1"+
        "\52\1\50\1\51\1\47\1\55\4\uffff\1\3\12\uffff\1\20\1\22\1\uffff\1"+
        "\13\1\uffff\1\15\4\uffff\1\31\3\uffff\1\32\1\16\1\24\14\uffff\1"+
        "\17\7\uffff\1\11\1\21\1\6\1\7\6\uffff\1\23\1\uffff\1\33\1\uffff"+
        "\1\34\1\4\1\5\1\10\2\uffff\1\12\1\30\1\14\1\uffff\1\2\4\uffff\1"+
        "\27\2\uffff\1\25\1\26";
    static final String DFA19_specialS =
        "\u0094\uffff}>";
    static final String[] DFA19_transitionS = {
            "\2\36\2\uffff\1\36\22\uffff\1\36\1\uffff\1\31\4\uffff\1\1\1"+
            "\17\1\20\1\21\1\32\1\22\1\32\1\23\1\uffff\1\33\11\34\1\30\1"+
            "\uffff\1\26\1\24\1\27\2\uffff\1\3\1\15\1\14\1\16\1\35\1\6\2"+
            "\35\1\10\1\7\1\35\1\5\1\35\1\13\1\11\3\35\1\2\1\35\1\4\1\35"+
            "\1\12\3\35\4\uffff\1\35\1\uffff\1\3\1\15\1\14\1\16\1\35\1\6"+
            "\2\35\1\10\1\7\1\35\1\5\1\35\1\13\1\11\3\35\1\2\1\35\1\4\1\35"+
            "\1\12\3\35\3\uffff\1\25",
            "",
            "\1\40\1\uffff\1\37\35\uffff\1\40\1\uffff\1\37",
            "\1\42\4\uffff\1\41\32\uffff\1\42\4\uffff\1\41",
            "\1\43\37\uffff\1\43",
            "\1\45\3\uffff\1\46\5\uffff\1\44\25\uffff\1\45\3\uffff\1\46"+
            "\5\uffff\1\44",
            "\1\47\37\uffff\1\47",
            "\1\50\37\uffff\1\50",
            "\1\51\4\uffff\1\52\32\uffff\1\51\4\uffff\1\52",
            "\1\54\3\uffff\1\55\2\uffff\1\53\30\uffff\1\54\3\uffff\1\55"+
            "\2\uffff\1\53",
            "\1\56\37\uffff\1\56",
            "\1\57\5\uffff\1\60\31\uffff\1\57\5\uffff\1\60",
            "\1\61\37\uffff\1\61",
            "\1\62\37\uffff\1\62",
            "\1\63\37\uffff\1\63",
            "",
            "",
            "",
            "",
            "\1\64\3\uffff\1\65\1\uffff\12\67",
            "",
            "",
            "\1\71\1\70",
            "\1\73",
            "",
            "",
            "\1\67\1\uffff\1\33\11\34",
            "\1\67\1\uffff\12\67\13\uffff\1\67\37\uffff\1\67",
            "\1\67\1\uffff\12\76\13\uffff\1\67\37\uffff\1\67",
            "",
            "",
            "\1\77\37\uffff\1\77",
            "\1\100\37\uffff\1\100",
            "\2\35\13\uffff\13\35\6\uffff\2\35\1\101\27\35\4\uffff\1\35"+
            "\1\uffff\2\35\1\101\27\35",
            "\1\103\24\uffff\1\104\12\uffff\1\103\24\uffff\1\104",
            "\1\105\37\uffff\1\105",
            "\1\106\37\uffff\1\106",
            "\1\107\37\uffff\1\107",
            "\1\110\37\uffff\1\110",
            "\1\111\37\uffff\1\111",
            "\1\112\37\uffff\1\112",
            "\2\35\13\uffff\13\35\6\uffff\15\35\1\113\14\35\4\uffff\1\114"+
            "\1\uffff\15\35\1\113\14\35",
            "\2\35\13\uffff\13\35\6\uffff\32\35\4\uffff\1\35\1\uffff\32"+
            "\35",
            "\1\117\37\uffff\1\117",
            "\2\35\13\uffff\13\35\6\uffff\32\35\4\uffff\1\35\1\uffff\32"+
            "\35",
            "\2\35\13\uffff\13\35\6\uffff\3\35\1\121\26\35\4\uffff\1\35"+
            "\1\uffff\3\35\1\121\26\35",
            "\1\123\37\uffff\1\123",
            "\1\124\37\uffff\1\124",
            "\1\125\37\uffff\1\125",
            "\1\126\37\uffff\1\126",
            "\2\35\13\uffff\13\35\6\uffff\32\35\4\uffff\1\35\1\uffff\32"+
            "\35",
            "\1\130\37\uffff\1\130",
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
            "\1\67\1\uffff\12\76\13\uffff\1\67\37\uffff\1\67",
            "\1\131\37\uffff\1\131",
            "\1\132\37\uffff\1\132",
            "\2\35\13\uffff\13\35\6\uffff\32\35\4\uffff\1\35\1\uffff\32"+
            "\35",
            "",
            "\2\35\13\uffff\13\35\6\uffff\32\35\4\uffff\1\35\1\uffff\32"+
            "\35",
            "\2\35\13\uffff\13\35\6\uffff\32\35\4\uffff\1\35\1\uffff\32"+
            "\35",
            "\1\136\37\uffff\1\136",
            "\1\137\37\uffff\1\137",
            "\1\140\37\uffff\1\140",
            "\1\141\37\uffff\1\141",
            "\1\142\37\uffff\1\142",
            "\1\143\37\uffff\1\143",
            "\1\144\37\uffff\1\144",
            "\1\145\15\uffff\1\146\21\uffff\1\145\15\uffff\1\146",
            "",
            "",
            "\1\147\37\uffff\1\147",
            "",
            "\1\150\37\uffff\1\150",
            "",
            "\1\151\37\uffff\1\151",
            "\2\35\13\uffff\13\35\6\uffff\32\35\4\uffff\1\35\1\uffff\32"+
            "\35",
            "\1\153\37\uffff\1\153",
            "\1\154\37\uffff\1\154",
            "",
            "\1\155\37\uffff\1\155",
            "\1\156\37\uffff\1\156",
            "\1\157\37\uffff\1\157",
            "",
            "",
            "",
            "\1\160\37\uffff\1\160",
            "\1\161\37\uffff\1\161",
            "\2\35\13\uffff\13\35\6\uffff\32\35\4\uffff\1\35\1\uffff\32"+
            "\35",
            "\2\35\13\uffff\13\35\6\uffff\32\35\4\uffff\1\35\1\uffff\32"+
            "\35",
            "\2\35\13\uffff\13\35\6\uffff\32\35\4\uffff\1\35\1\uffff\32"+
            "\35",
            "\2\35\13\uffff\13\35\6\uffff\32\35\4\uffff\1\35\1\uffff\32"+
            "\35",
            "\1\166\37\uffff\1\166",
            "\1\167\37\uffff\1\167",
            "\1\170\37\uffff\1\170",
            "\1\171\37\uffff\1\171",
            "\1\172\37\uffff\1\172",
            "\1\173\37\uffff\1\173",
            "",
            "\2\35\13\uffff\13\35\6\uffff\32\35\4\uffff\1\35\1\uffff\32"+
            "\35",
            "\1\175\37\uffff\1\175",
            "\2\35\13\uffff\13\35\6\uffff\32\35\4\uffff\1\35\1\uffff\32"+
            "\35",
            "\1\177\37\uffff\1\177",
            "\2\35\13\uffff\13\35\6\uffff\32\35\4\uffff\1\35\1\uffff\32"+
            "\35",
            "\2\35\13\uffff\13\35\6\uffff\32\35\4\uffff\1\35\1\uffff\32"+
            "\35",
            "\2\35\13\uffff\13\35\6\uffff\32\35\4\uffff\1\35\1\uffff\32"+
            "\35",
            "",
            "",
            "",
            "",
            "\2\35\13\uffff\13\35\6\uffff\32\35\4\uffff\1\35\1\uffff\32"+
            "\35",
            "\1\u0084\37\uffff\1\u0084",
            "\1\u0085\37\uffff\1\u0085",
            "\2\35\13\uffff\13\35\6\uffff\32\35\4\uffff\1\35\1\uffff\32"+
            "\35",
            "\2\35\13\uffff\13\35\6\uffff\32\35\4\uffff\1\35\1\uffff\32"+
            "\35",
            "\2\35\13\uffff\13\35\6\uffff\32\35\4\uffff\1\35\1\uffff\32"+
            "\35",
            "",
            "\1\u0089\37\uffff\1\u0089",
            "",
            "\2\35\13\uffff\13\35\6\uffff\32\35\4\uffff\1\35\1\uffff\32"+
            "\35",
            "",
            "",
            "",
            "",
            "\1\u008b\37\uffff\1\u008b",
            "\1\u008c\37\uffff\1\u008c",
            "",
            "",
            "",
            "\1\u008d\37\uffff\1\u008d",
            "",
            "\1\u008e\37\uffff\1\u008e",
            "\2\35\13\uffff\13\35\6\uffff\32\35\4\uffff\1\35\1\uffff\32"+
            "\35",
            "\1\u0090\37\uffff\1\u0090",
            "\1\u0091\37\uffff\1\u0091",
            "",
            "\2\35\13\uffff\13\35\6\uffff\32\35\4\uffff\1\35\1\uffff\32"+
            "\35",
            "\2\35\13\uffff\13\35\6\uffff\32\35\4\uffff\1\35\1\uffff\32"+
            "\35",
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
            return "1:1: Tokens : ( QUOTED_STRING | SELECT | AS | UPPER | LOWER | FROM | JOIN | INNER | LEFT | OUTER | ON | WHERE | OR | AND | NOT | IN | LIKE | IS | NULL | ANY | CONTAINS | IN_FOLDER | IN_TREE | ORDER | BY | ASC | DESC | SCORE | LPAREN | RPAREN | STAR | COMMA | DOTSTAR | DOT | DOTDOT | EQUALS | TILDA | NOTEQUALS | GREATERTHAN | LESSTHAN | GREATERTHANOREQUALS | LESSTHANOREQUALS | COLON | DOUBLE_QUOTE | DECIMAL_INTEGER_LITERAL | FLOATING_POINT_LITERAL | ID | WS );";
        }
    }
 

}