// $ANTLR 3.1b1 W:\\workspace-cmis\\ANTLR\\FTS.g 2008-07-03 14:45:12
package org.alfresco.repo.search.impl.parsers;

import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;


import org.antlr.runtime.tree.*;

public class FTSParser extends Parser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "FTS", "DISJUNCTION", "CONJUNCTION", "NEGATION", "TERM", "EXACT_TERM", "PHRASE", "SYNONYM", "FIELD_DISJUNCTION", "FIELD_CONJUNCTION", "FIELD_NEGATION", "FIELD_GROUP", "FG_TERM", "FG_EXACT_TERM", "FG_PHRASE", "FG_SYNONYM", "FG_PROXIMITY", "FG_RANGE", "COLUMN_REF", "OR", "AND", "MINUS", "LPAREN", "RPAREN", "COLON", "FTSWORD", "PLUS", "FTSPHRASE", "TILDA", "STAR", "DOTDOT", "DOT", "ID", "NOT", "INWORD", "WS"
    };
    public static final int TERM=8;
    public static final int STAR=33;
    public static final int FG_PROXIMITY=20;
    public static final int CONJUNCTION=6;
    public static final int FG_TERM=16;
    public static final int EXACT_TERM=9;
    public static final int FIELD_GROUP=15;
    public static final int INWORD=38;
    public static final int FIELD_DISJUNCTION=12;
    public static final int DOTDOT=34;
    public static final int NOT=37;
    public static final int FG_EXACT_TERM=17;
    public static final int MINUS=25;
    public static final int ID=36;
    public static final int AND=24;
    public static final int EOF=-1;
    public static final int FTSWORD=29;
    public static final int LPAREN=26;
    public static final int PHRASE=10;
    public static final int COLON=28;
    public static final int DISJUNCTION=5;
    public static final int RPAREN=27;
    public static final int TILDA=32;
    public static final int FTS=4;
    public static final int WS=39;
    public static final int FG_SYNONYM=19;
    public static final int NEGATION=7;
    public static final int FTSPHRASE=31;
    public static final int FIELD_CONJUNCTION=13;
    public static final int OR=23;
    public static final int PLUS=30;
    public static final int DOT=35;
    public static final int COLUMN_REF=22;
    public static final int FG_RANGE=21;
    public static final int SYNONYM=11;
    public static final int FG_PHRASE=18;
    public static final int FIELD_NEGATION=14;

    // delegates
    // delegators


        public FTSParser(TokenStream input) {
            this(input, new RecognizerSharedState());
        }
        public FTSParser(TokenStream input, RecognizerSharedState state) {
            super(input, state);
        }
        
    protected TreeAdaptor adaptor = new CommonTreeAdaptor();

    public void setTreeAdaptor(TreeAdaptor adaptor) {
        this.adaptor = adaptor;
    }
    public TreeAdaptor getTreeAdaptor() {
        return adaptor;
    }

    public String[] getTokenNames() { return FTSParser.tokenNames; }
    public String getGrammarFileName() { return "W:\\workspace-cmis\\ANTLR\\FTS.g"; }


           
    	public boolean defaultConjunction()
    	{
    	   return true;
    	}
    	
    	public boolean defaultFieldConjunction()
    	{
    	   return true;
    	}
    	
    	


    public static class fts_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start fts
    // W:\\workspace-cmis\\ANTLR\\FTS.g:85:1: fts : ftsImplicitConjunctionOrDisjunction -> ftsImplicitConjunctionOrDisjunction ;
    public final FTSParser.fts_return fts() throws RecognitionException {
        FTSParser.fts_return retval = new FTSParser.fts_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        FTSParser.ftsImplicitConjunctionOrDisjunction_return ftsImplicitConjunctionOrDisjunction1 = null;


        RewriteRuleSubtreeStream stream_ftsImplicitConjunctionOrDisjunction=new RewriteRuleSubtreeStream(adaptor,"rule ftsImplicitConjunctionOrDisjunction");
        try {
            // W:\\workspace-cmis\\ANTLR\\FTS.g:85:5: ( ftsImplicitConjunctionOrDisjunction -> ftsImplicitConjunctionOrDisjunction )
            // W:\\workspace-cmis\\ANTLR\\FTS.g:85:8: ftsImplicitConjunctionOrDisjunction
            {
            pushFollow(FOLLOW_ftsImplicitConjunctionOrDisjunction_in_fts135);
            ftsImplicitConjunctionOrDisjunction1=ftsImplicitConjunctionOrDisjunction();

            state._fsp--;

            stream_ftsImplicitConjunctionOrDisjunction.add(ftsImplicitConjunctionOrDisjunction1.getTree());


            // AST REWRITE
            // elements: ftsImplicitConjunctionOrDisjunction
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 86:3: -> ftsImplicitConjunctionOrDisjunction
            {
                adaptor.addChild(root_0, stream_ftsImplicitConjunctionOrDisjunction.nextTree());

            }

            retval.tree = root_0;retval.tree = root_0;
            }

            retval.stop = input.LT(-1);

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end fts

    public static class ftsImplicitConjunctionOrDisjunction_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start ftsImplicitConjunctionOrDisjunction
    // W:\\workspace-cmis\\ANTLR\\FTS.g:89:1: ftsImplicitConjunctionOrDisjunction : ({...}? ftsExplicitDisjunction ( ftsExplicitDisjunction )* -> ^( CONJUNCTION ( ftsExplicitDisjunction )+ ) | ftsExplicitDisjunction ( ftsExplicitDisjunction )* -> ^( DISJUNCTION ( ftsExplicitDisjunction )+ ) );
    public final FTSParser.ftsImplicitConjunctionOrDisjunction_return ftsImplicitConjunctionOrDisjunction() throws RecognitionException {
        FTSParser.ftsImplicitConjunctionOrDisjunction_return retval = new FTSParser.ftsImplicitConjunctionOrDisjunction_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        FTSParser.ftsExplicitDisjunction_return ftsExplicitDisjunction2 = null;

        FTSParser.ftsExplicitDisjunction_return ftsExplicitDisjunction3 = null;

        FTSParser.ftsExplicitDisjunction_return ftsExplicitDisjunction4 = null;

        FTSParser.ftsExplicitDisjunction_return ftsExplicitDisjunction5 = null;


        RewriteRuleSubtreeStream stream_ftsExplicitDisjunction=new RewriteRuleSubtreeStream(adaptor,"rule ftsExplicitDisjunction");
        try {
            // W:\\workspace-cmis\\ANTLR\\FTS.g:90:2: ({...}? ftsExplicitDisjunction ( ftsExplicitDisjunction )* -> ^( CONJUNCTION ( ftsExplicitDisjunction )+ ) | ftsExplicitDisjunction ( ftsExplicitDisjunction )* -> ^( DISJUNCTION ( ftsExplicitDisjunction )+ ) )
            int alt3=2;
            alt3 = dfa3.predict(input);
            switch (alt3) {
                case 1 :
                    // W:\\workspace-cmis\\ANTLR\\FTS.g:90:4: {...}? ftsExplicitDisjunction ( ftsExplicitDisjunction )*
                    {
                    if ( !(defaultConjunction()) ) {
                        throw new FailedPredicateException(input, "ftsImplicitConjunctionOrDisjunction", "defaultConjunction()");
                    }
                    pushFollow(FOLLOW_ftsExplicitDisjunction_in_ftsImplicitConjunctionOrDisjunction157);
                    ftsExplicitDisjunction2=ftsExplicitDisjunction();

                    state._fsp--;

                    stream_ftsExplicitDisjunction.add(ftsExplicitDisjunction2.getTree());
                    // W:\\workspace-cmis\\ANTLR\\FTS.g:90:51: ( ftsExplicitDisjunction )*
                    loop1:
                    do {
                        int alt1=2;
                        int LA1_0 = input.LA(1);

                        if ( ((LA1_0>=MINUS && LA1_0<=LPAREN)||(LA1_0>=FTSWORD && LA1_0<=TILDA)||LA1_0==ID) ) {
                            alt1=1;
                        }


                        switch (alt1) {
                    	case 1 :
                    	    // W:\\workspace-cmis\\ANTLR\\FTS.g:90:52: ftsExplicitDisjunction
                    	    {
                    	    pushFollow(FOLLOW_ftsExplicitDisjunction_in_ftsImplicitConjunctionOrDisjunction160);
                    	    ftsExplicitDisjunction3=ftsExplicitDisjunction();

                    	    state._fsp--;

                    	    stream_ftsExplicitDisjunction.add(ftsExplicitDisjunction3.getTree());

                    	    }
                    	    break;

                    	default :
                    	    break loop1;
                        }
                    } while (true);



                    // AST REWRITE
                    // elements: ftsExplicitDisjunction
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 91:3: -> ^( CONJUNCTION ( ftsExplicitDisjunction )+ )
                    {
                        // W:\\workspace-cmis\\ANTLR\\FTS.g:91:6: ^( CONJUNCTION ( ftsExplicitDisjunction )+ )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(CONJUNCTION, "CONJUNCTION"), root_1);

                        if ( !(stream_ftsExplicitDisjunction.hasNext()) ) {
                            throw new RewriteEarlyExitException();
                        }
                        while ( stream_ftsExplicitDisjunction.hasNext() ) {
                            adaptor.addChild(root_1, stream_ftsExplicitDisjunction.nextTree());

                        }
                        stream_ftsExplicitDisjunction.reset();

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;retval.tree = root_0;
                    }
                    break;
                case 2 :
                    // W:\\workspace-cmis\\ANTLR\\FTS.g:92:5: ftsExplicitDisjunction ( ftsExplicitDisjunction )*
                    {
                    pushFollow(FOLLOW_ftsExplicitDisjunction_in_ftsImplicitConjunctionOrDisjunction179);
                    ftsExplicitDisjunction4=ftsExplicitDisjunction();

                    state._fsp--;

                    stream_ftsExplicitDisjunction.add(ftsExplicitDisjunction4.getTree());
                    // W:\\workspace-cmis\\ANTLR\\FTS.g:92:28: ( ftsExplicitDisjunction )*
                    loop2:
                    do {
                        int alt2=2;
                        int LA2_0 = input.LA(1);

                        if ( ((LA2_0>=MINUS && LA2_0<=LPAREN)||(LA2_0>=FTSWORD && LA2_0<=TILDA)||LA2_0==ID) ) {
                            alt2=1;
                        }


                        switch (alt2) {
                    	case 1 :
                    	    // W:\\workspace-cmis\\ANTLR\\FTS.g:92:29: ftsExplicitDisjunction
                    	    {
                    	    pushFollow(FOLLOW_ftsExplicitDisjunction_in_ftsImplicitConjunctionOrDisjunction182);
                    	    ftsExplicitDisjunction5=ftsExplicitDisjunction();

                    	    state._fsp--;

                    	    stream_ftsExplicitDisjunction.add(ftsExplicitDisjunction5.getTree());

                    	    }
                    	    break;

                    	default :
                    	    break loop2;
                        }
                    } while (true);



                    // AST REWRITE
                    // elements: ftsExplicitDisjunction
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 93:3: -> ^( DISJUNCTION ( ftsExplicitDisjunction )+ )
                    {
                        // W:\\workspace-cmis\\ANTLR\\FTS.g:93:6: ^( DISJUNCTION ( ftsExplicitDisjunction )+ )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(DISJUNCTION, "DISJUNCTION"), root_1);

                        if ( !(stream_ftsExplicitDisjunction.hasNext()) ) {
                            throw new RewriteEarlyExitException();
                        }
                        while ( stream_ftsExplicitDisjunction.hasNext() ) {
                            adaptor.addChild(root_1, stream_ftsExplicitDisjunction.nextTree());

                        }
                        stream_ftsExplicitDisjunction.reset();

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;retval.tree = root_0;
                    }
                    break;

            }
            retval.stop = input.LT(-1);

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end ftsImplicitConjunctionOrDisjunction

    public static class ftsExplicitDisjunction_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start ftsExplicitDisjunction
    // W:\\workspace-cmis\\ANTLR\\FTS.g:96:1: ftsExplicitDisjunction : ftsExplictConjunction ( OR ftsExplictConjunction )* -> ^( DISJUNCTION ( ftsExplictConjunction )+ ) ;
    public final FTSParser.ftsExplicitDisjunction_return ftsExplicitDisjunction() throws RecognitionException {
        FTSParser.ftsExplicitDisjunction_return retval = new FTSParser.ftsExplicitDisjunction_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token OR7=null;
        FTSParser.ftsExplictConjunction_return ftsExplictConjunction6 = null;

        FTSParser.ftsExplictConjunction_return ftsExplictConjunction8 = null;


        Object OR7_tree=null;
        RewriteRuleTokenStream stream_OR=new RewriteRuleTokenStream(adaptor,"token OR");
        RewriteRuleSubtreeStream stream_ftsExplictConjunction=new RewriteRuleSubtreeStream(adaptor,"rule ftsExplictConjunction");
        try {
            // W:\\workspace-cmis\\ANTLR\\FTS.g:97:2: ( ftsExplictConjunction ( OR ftsExplictConjunction )* -> ^( DISJUNCTION ( ftsExplictConjunction )+ ) )
            // W:\\workspace-cmis\\ANTLR\\FTS.g:97:4: ftsExplictConjunction ( OR ftsExplictConjunction )*
            {
            pushFollow(FOLLOW_ftsExplictConjunction_in_ftsExplicitDisjunction207);
            ftsExplictConjunction6=ftsExplictConjunction();

            state._fsp--;

            stream_ftsExplictConjunction.add(ftsExplictConjunction6.getTree());
            // W:\\workspace-cmis\\ANTLR\\FTS.g:97:26: ( OR ftsExplictConjunction )*
            loop4:
            do {
                int alt4=2;
                int LA4_0 = input.LA(1);

                if ( (LA4_0==OR) ) {
                    alt4=1;
                }


                switch (alt4) {
            	case 1 :
            	    // W:\\workspace-cmis\\ANTLR\\FTS.g:97:27: OR ftsExplictConjunction
            	    {
            	    OR7=(Token)match(input,OR,FOLLOW_OR_in_ftsExplicitDisjunction210);  
            	    stream_OR.add(OR7);

            	    pushFollow(FOLLOW_ftsExplictConjunction_in_ftsExplicitDisjunction212);
            	    ftsExplictConjunction8=ftsExplictConjunction();

            	    state._fsp--;

            	    stream_ftsExplictConjunction.add(ftsExplictConjunction8.getTree());

            	    }
            	    break;

            	default :
            	    break loop4;
                }
            } while (true);



            // AST REWRITE
            // elements: ftsExplictConjunction
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 98:3: -> ^( DISJUNCTION ( ftsExplictConjunction )+ )
            {
                // W:\\workspace-cmis\\ANTLR\\FTS.g:98:6: ^( DISJUNCTION ( ftsExplictConjunction )+ )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(DISJUNCTION, "DISJUNCTION"), root_1);

                if ( !(stream_ftsExplictConjunction.hasNext()) ) {
                    throw new RewriteEarlyExitException();
                }
                while ( stream_ftsExplictConjunction.hasNext() ) {
                    adaptor.addChild(root_1, stream_ftsExplictConjunction.nextTree());

                }
                stream_ftsExplictConjunction.reset();

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;retval.tree = root_0;
            }

            retval.stop = input.LT(-1);

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end ftsExplicitDisjunction

    public static class ftsExplictConjunction_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start ftsExplictConjunction
    // W:\\workspace-cmis\\ANTLR\\FTS.g:101:1: ftsExplictConjunction : ftsNot ( AND ftsNot )* -> ^( CONJUNCTION ftsNot ) ;
    public final FTSParser.ftsExplictConjunction_return ftsExplictConjunction() throws RecognitionException {
        FTSParser.ftsExplictConjunction_return retval = new FTSParser.ftsExplictConjunction_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token AND10=null;
        FTSParser.ftsNot_return ftsNot9 = null;

        FTSParser.ftsNot_return ftsNot11 = null;


        Object AND10_tree=null;
        RewriteRuleTokenStream stream_AND=new RewriteRuleTokenStream(adaptor,"token AND");
        RewriteRuleSubtreeStream stream_ftsNot=new RewriteRuleSubtreeStream(adaptor,"rule ftsNot");
        try {
            // W:\\workspace-cmis\\ANTLR\\FTS.g:102:2: ( ftsNot ( AND ftsNot )* -> ^( CONJUNCTION ftsNot ) )
            // W:\\workspace-cmis\\ANTLR\\FTS.g:102:4: ftsNot ( AND ftsNot )*
            {
            pushFollow(FOLLOW_ftsNot_in_ftsExplictConjunction237);
            ftsNot9=ftsNot();

            state._fsp--;

            stream_ftsNot.add(ftsNot9.getTree());
            // W:\\workspace-cmis\\ANTLR\\FTS.g:102:11: ( AND ftsNot )*
            loop5:
            do {
                int alt5=2;
                int LA5_0 = input.LA(1);

                if ( (LA5_0==AND) ) {
                    alt5=1;
                }


                switch (alt5) {
            	case 1 :
            	    // W:\\workspace-cmis\\ANTLR\\FTS.g:102:12: AND ftsNot
            	    {
            	    AND10=(Token)match(input,AND,FOLLOW_AND_in_ftsExplictConjunction240);  
            	    stream_AND.add(AND10);

            	    pushFollow(FOLLOW_ftsNot_in_ftsExplictConjunction242);
            	    ftsNot11=ftsNot();

            	    state._fsp--;

            	    stream_ftsNot.add(ftsNot11.getTree());

            	    }
            	    break;

            	default :
            	    break loop5;
                }
            } while (true);



            // AST REWRITE
            // elements: ftsNot
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 103:3: -> ^( CONJUNCTION ftsNot )
            {
                // W:\\workspace-cmis\\ANTLR\\FTS.g:103:6: ^( CONJUNCTION ftsNot )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(CONJUNCTION, "CONJUNCTION"), root_1);

                adaptor.addChild(root_1, stream_ftsNot.nextTree());

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;retval.tree = root_0;
            }

            retval.stop = input.LT(-1);

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end ftsExplictConjunction

    public static class ftsNot_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start ftsNot
    // W:\\workspace-cmis\\ANTLR\\FTS.g:107:1: ftsNot : ( MINUS ftsTest -> ^( NEGATION ftsTest ) | ftsTest -> ftsTest );
    public final FTSParser.ftsNot_return ftsNot() throws RecognitionException {
        FTSParser.ftsNot_return retval = new FTSParser.ftsNot_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token MINUS12=null;
        FTSParser.ftsTest_return ftsTest13 = null;

        FTSParser.ftsTest_return ftsTest14 = null;


        Object MINUS12_tree=null;
        RewriteRuleTokenStream stream_MINUS=new RewriteRuleTokenStream(adaptor,"token MINUS");
        RewriteRuleSubtreeStream stream_ftsTest=new RewriteRuleSubtreeStream(adaptor,"rule ftsTest");
        try {
            // W:\\workspace-cmis\\ANTLR\\FTS.g:107:9: ( MINUS ftsTest -> ^( NEGATION ftsTest ) | ftsTest -> ftsTest )
            int alt6=2;
            int LA6_0 = input.LA(1);

            if ( (LA6_0==MINUS) ) {
                alt6=1;
            }
            else if ( (LA6_0==LPAREN||(LA6_0>=FTSWORD && LA6_0<=TILDA)||LA6_0==ID) ) {
                alt6=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 6, 0, input);

                throw nvae;
            }
            switch (alt6) {
                case 1 :
                    // W:\\workspace-cmis\\ANTLR\\FTS.g:107:11: MINUS ftsTest
                    {
                    MINUS12=(Token)match(input,MINUS,FOLLOW_MINUS_in_ftsNot268);  
                    stream_MINUS.add(MINUS12);

                    pushFollow(FOLLOW_ftsTest_in_ftsNot270);
                    ftsTest13=ftsTest();

                    state._fsp--;

                    stream_ftsTest.add(ftsTest13.getTree());


                    // AST REWRITE
                    // elements: ftsTest
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 108:3: -> ^( NEGATION ftsTest )
                    {
                        // W:\\workspace-cmis\\ANTLR\\FTS.g:108:6: ^( NEGATION ftsTest )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(NEGATION, "NEGATION"), root_1);

                        adaptor.addChild(root_1, stream_ftsTest.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;retval.tree = root_0;
                    }
                    break;
                case 2 :
                    // W:\\workspace-cmis\\ANTLR\\FTS.g:109:4: ftsTest
                    {
                    pushFollow(FOLLOW_ftsTest_in_ftsNot285);
                    ftsTest14=ftsTest();

                    state._fsp--;

                    stream_ftsTest.add(ftsTest14.getTree());


                    // AST REWRITE
                    // elements: ftsTest
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 110:3: -> ftsTest
                    {
                        adaptor.addChild(root_0, stream_ftsTest.nextTree());

                    }

                    retval.tree = root_0;retval.tree = root_0;
                    }
                    break;

            }
            retval.stop = input.LT(-1);

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end ftsNot

    public static class ftsTest_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start ftsTest
    // W:\\workspace-cmis\\ANTLR\\FTS.g:113:1: ftsTest : ( ftsTerm -> ^( TERM ftsTerm ) | ftsExactTerm -> ^( EXACT_TERM ftsExactTerm ) | ftsPhrase -> ^( PHRASE ftsPhrase ) | ftsSynonym -> ^( SYNONYM ftsSynonym ) | ftsFieldGroupProximity -> ^( FG_PROXIMITY ftsFieldGroupProximity ) | ftsFieldGroupRange -> ^( FG_RANGE ftsFieldGroupRange ) | ftsFieldGroup | LPAREN ftsImplicitConjunctionOrDisjunction RPAREN -> ftsImplicitConjunctionOrDisjunction );
    public final FTSParser.ftsTest_return ftsTest() throws RecognitionException {
        FTSParser.ftsTest_return retval = new FTSParser.ftsTest_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token LPAREN22=null;
        Token RPAREN24=null;
        FTSParser.ftsTerm_return ftsTerm15 = null;

        FTSParser.ftsExactTerm_return ftsExactTerm16 = null;

        FTSParser.ftsPhrase_return ftsPhrase17 = null;

        FTSParser.ftsSynonym_return ftsSynonym18 = null;

        FTSParser.ftsFieldGroupProximity_return ftsFieldGroupProximity19 = null;

        FTSParser.ftsFieldGroupRange_return ftsFieldGroupRange20 = null;

        FTSParser.ftsFieldGroup_return ftsFieldGroup21 = null;

        FTSParser.ftsImplicitConjunctionOrDisjunction_return ftsImplicitConjunctionOrDisjunction23 = null;


        Object LPAREN22_tree=null;
        Object RPAREN24_tree=null;
        RewriteRuleTokenStream stream_RPAREN=new RewriteRuleTokenStream(adaptor,"token RPAREN");
        RewriteRuleTokenStream stream_LPAREN=new RewriteRuleTokenStream(adaptor,"token LPAREN");
        RewriteRuleSubtreeStream stream_ftsFieldGroupRange=new RewriteRuleSubtreeStream(adaptor,"rule ftsFieldGroupRange");
        RewriteRuleSubtreeStream stream_ftsTerm=new RewriteRuleSubtreeStream(adaptor,"rule ftsTerm");
        RewriteRuleSubtreeStream stream_ftsExactTerm=new RewriteRuleSubtreeStream(adaptor,"rule ftsExactTerm");
        RewriteRuleSubtreeStream stream_ftsImplicitConjunctionOrDisjunction=new RewriteRuleSubtreeStream(adaptor,"rule ftsImplicitConjunctionOrDisjunction");
        RewriteRuleSubtreeStream stream_ftsPhrase=new RewriteRuleSubtreeStream(adaptor,"rule ftsPhrase");
        RewriteRuleSubtreeStream stream_ftsSynonym=new RewriteRuleSubtreeStream(adaptor,"rule ftsSynonym");
        RewriteRuleSubtreeStream stream_ftsFieldGroupProximity=new RewriteRuleSubtreeStream(adaptor,"rule ftsFieldGroupProximity");
        try {
            // W:\\workspace-cmis\\ANTLR\\FTS.g:113:9: ( ftsTerm -> ^( TERM ftsTerm ) | ftsExactTerm -> ^( EXACT_TERM ftsExactTerm ) | ftsPhrase -> ^( PHRASE ftsPhrase ) | ftsSynonym -> ^( SYNONYM ftsSynonym ) | ftsFieldGroupProximity -> ^( FG_PROXIMITY ftsFieldGroupProximity ) | ftsFieldGroupRange -> ^( FG_RANGE ftsFieldGroupRange ) | ftsFieldGroup | LPAREN ftsImplicitConjunctionOrDisjunction RPAREN -> ftsImplicitConjunctionOrDisjunction )
            int alt7=8;
            alt7 = dfa7.predict(input);
            switch (alt7) {
                case 1 :
                    // W:\\workspace-cmis\\ANTLR\\FTS.g:113:11: ftsTerm
                    {
                    pushFollow(FOLLOW_ftsTerm_in_ftsTest301);
                    ftsTerm15=ftsTerm();

                    state._fsp--;

                    stream_ftsTerm.add(ftsTerm15.getTree());


                    // AST REWRITE
                    // elements: ftsTerm
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 114:3: -> ^( TERM ftsTerm )
                    {
                        // W:\\workspace-cmis\\ANTLR\\FTS.g:114:6: ^( TERM ftsTerm )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(TERM, "TERM"), root_1);

                        adaptor.addChild(root_1, stream_ftsTerm.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;retval.tree = root_0;
                    }
                    break;
                case 2 :
                    // W:\\workspace-cmis\\ANTLR\\FTS.g:115:4: ftsExactTerm
                    {
                    pushFollow(FOLLOW_ftsExactTerm_in_ftsTest316);
                    ftsExactTerm16=ftsExactTerm();

                    state._fsp--;

                    stream_ftsExactTerm.add(ftsExactTerm16.getTree());


                    // AST REWRITE
                    // elements: ftsExactTerm
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 116:3: -> ^( EXACT_TERM ftsExactTerm )
                    {
                        // W:\\workspace-cmis\\ANTLR\\FTS.g:116:6: ^( EXACT_TERM ftsExactTerm )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(EXACT_TERM, "EXACT_TERM"), root_1);

                        adaptor.addChild(root_1, stream_ftsExactTerm.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;retval.tree = root_0;
                    }
                    break;
                case 3 :
                    // W:\\workspace-cmis\\ANTLR\\FTS.g:117:17: ftsPhrase
                    {
                    pushFollow(FOLLOW_ftsPhrase_in_ftsTest344);
                    ftsPhrase17=ftsPhrase();

                    state._fsp--;

                    stream_ftsPhrase.add(ftsPhrase17.getTree());


                    // AST REWRITE
                    // elements: ftsPhrase
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 118:10: -> ^( PHRASE ftsPhrase )
                    {
                        // W:\\workspace-cmis\\ANTLR\\FTS.g:118:13: ^( PHRASE ftsPhrase )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(PHRASE, "PHRASE"), root_1);

                        adaptor.addChild(root_1, stream_ftsPhrase.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;retval.tree = root_0;
                    }
                    break;
                case 4 :
                    // W:\\workspace-cmis\\ANTLR\\FTS.g:119:17: ftsSynonym
                    {
                    pushFollow(FOLLOW_ftsSynonym_in_ftsTest379);
                    ftsSynonym18=ftsSynonym();

                    state._fsp--;

                    stream_ftsSynonym.add(ftsSynonym18.getTree());


                    // AST REWRITE
                    // elements: ftsSynonym
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 120:10: -> ^( SYNONYM ftsSynonym )
                    {
                        // W:\\workspace-cmis\\ANTLR\\FTS.g:120:13: ^( SYNONYM ftsSynonym )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(SYNONYM, "SYNONYM"), root_1);

                        adaptor.addChild(root_1, stream_ftsSynonym.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;retval.tree = root_0;
                    }
                    break;
                case 5 :
                    // W:\\workspace-cmis\\ANTLR\\FTS.g:121:11: ftsFieldGroupProximity
                    {
                    pushFollow(FOLLOW_ftsFieldGroupProximity_in_ftsTest408);
                    ftsFieldGroupProximity19=ftsFieldGroupProximity();

                    state._fsp--;

                    stream_ftsFieldGroupProximity.add(ftsFieldGroupProximity19.getTree());


                    // AST REWRITE
                    // elements: ftsFieldGroupProximity
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 122:10: -> ^( FG_PROXIMITY ftsFieldGroupProximity )
                    {
                        // W:\\workspace-cmis\\ANTLR\\FTS.g:122:13: ^( FG_PROXIMITY ftsFieldGroupProximity )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(FG_PROXIMITY, "FG_PROXIMITY"), root_1);

                        adaptor.addChild(root_1, stream_ftsFieldGroupProximity.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;retval.tree = root_0;
                    }
                    break;
                case 6 :
                    // W:\\workspace-cmis\\ANTLR\\FTS.g:123:12: ftsFieldGroupRange
                    {
                    pushFollow(FOLLOW_ftsFieldGroupRange_in_ftsTest440);
                    ftsFieldGroupRange20=ftsFieldGroupRange();

                    state._fsp--;

                    stream_ftsFieldGroupRange.add(ftsFieldGroupRange20.getTree());


                    // AST REWRITE
                    // elements: ftsFieldGroupRange
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 124:10: -> ^( FG_RANGE ftsFieldGroupRange )
                    {
                        // W:\\workspace-cmis\\ANTLR\\FTS.g:124:13: ^( FG_RANGE ftsFieldGroupRange )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(FG_RANGE, "FG_RANGE"), root_1);

                        adaptor.addChild(root_1, stream_ftsFieldGroupRange.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;retval.tree = root_0;
                    }
                    break;
                case 7 :
                    // W:\\workspace-cmis\\ANTLR\\FTS.g:125:11: ftsFieldGroup
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_ftsFieldGroup_in_ftsTest469);
                    ftsFieldGroup21=ftsFieldGroup();

                    state._fsp--;

                    adaptor.addChild(root_0, ftsFieldGroup21.getTree());

                    }
                    break;
                case 8 :
                    // W:\\workspace-cmis\\ANTLR\\FTS.g:126:4: LPAREN ftsImplicitConjunctionOrDisjunction RPAREN
                    {
                    LPAREN22=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_ftsTest478);  
                    stream_LPAREN.add(LPAREN22);

                    pushFollow(FOLLOW_ftsImplicitConjunctionOrDisjunction_in_ftsTest480);
                    ftsImplicitConjunctionOrDisjunction23=ftsImplicitConjunctionOrDisjunction();

                    state._fsp--;

                    stream_ftsImplicitConjunctionOrDisjunction.add(ftsImplicitConjunctionOrDisjunction23.getTree());
                    RPAREN24=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_ftsTest482);  
                    stream_RPAREN.add(RPAREN24);



                    // AST REWRITE
                    // elements: ftsImplicitConjunctionOrDisjunction
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 127:3: -> ftsImplicitConjunctionOrDisjunction
                    {
                        adaptor.addChild(root_0, stream_ftsImplicitConjunctionOrDisjunction.nextTree());

                    }

                    retval.tree = root_0;retval.tree = root_0;
                    }
                    break;

            }
            retval.stop = input.LT(-1);

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end ftsTest

    public static class ftsTerm_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start ftsTerm
    // W:\\workspace-cmis\\ANTLR\\FTS.g:130:1: ftsTerm : ( columnReference COLON )? FTSWORD -> FTSWORD ( columnReference )? ;
    public final FTSParser.ftsTerm_return ftsTerm() throws RecognitionException {
        FTSParser.ftsTerm_return retval = new FTSParser.ftsTerm_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token COLON26=null;
        Token FTSWORD27=null;
        FTSParser.columnReference_return columnReference25 = null;


        Object COLON26_tree=null;
        Object FTSWORD27_tree=null;
        RewriteRuleTokenStream stream_COLON=new RewriteRuleTokenStream(adaptor,"token COLON");
        RewriteRuleTokenStream stream_FTSWORD=new RewriteRuleTokenStream(adaptor,"token FTSWORD");
        RewriteRuleSubtreeStream stream_columnReference=new RewriteRuleSubtreeStream(adaptor,"rule columnReference");
        try {
            // W:\\workspace-cmis\\ANTLR\\FTS.g:131:2: ( ( columnReference COLON )? FTSWORD -> FTSWORD ( columnReference )? )
            // W:\\workspace-cmis\\ANTLR\\FTS.g:131:4: ( columnReference COLON )? FTSWORD
            {
            // W:\\workspace-cmis\\ANTLR\\FTS.g:131:4: ( columnReference COLON )?
            int alt8=2;
            int LA8_0 = input.LA(1);

            if ( (LA8_0==ID) ) {
                alt8=1;
            }
            switch (alt8) {
                case 1 :
                    // W:\\workspace-cmis\\ANTLR\\FTS.g:131:5: columnReference COLON
                    {
                    pushFollow(FOLLOW_columnReference_in_ftsTerm500);
                    columnReference25=columnReference();

                    state._fsp--;

                    stream_columnReference.add(columnReference25.getTree());
                    COLON26=(Token)match(input,COLON,FOLLOW_COLON_in_ftsTerm502);  
                    stream_COLON.add(COLON26);


                    }
                    break;

            }

            FTSWORD27=(Token)match(input,FTSWORD,FOLLOW_FTSWORD_in_ftsTerm506);  
            stream_FTSWORD.add(FTSWORD27);



            // AST REWRITE
            // elements: FTSWORD, columnReference
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 132:3: -> FTSWORD ( columnReference )?
            {
                adaptor.addChild(root_0, stream_FTSWORD.nextNode());
                // W:\\workspace-cmis\\ANTLR\\FTS.g:132:14: ( columnReference )?
                if ( stream_columnReference.hasNext() ) {
                    adaptor.addChild(root_0, stream_columnReference.nextTree());

                }
                stream_columnReference.reset();

            }

            retval.tree = root_0;retval.tree = root_0;
            }

            retval.stop = input.LT(-1);

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end ftsTerm

    public static class ftsExactTerm_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start ftsExactTerm
    // W:\\workspace-cmis\\ANTLR\\FTS.g:135:1: ftsExactTerm : PLUS ftsTerm -> ftsTerm ;
    public final FTSParser.ftsExactTerm_return ftsExactTerm() throws RecognitionException {
        FTSParser.ftsExactTerm_return retval = new FTSParser.ftsExactTerm_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token PLUS28=null;
        FTSParser.ftsTerm_return ftsTerm29 = null;


        Object PLUS28_tree=null;
        RewriteRuleTokenStream stream_PLUS=new RewriteRuleTokenStream(adaptor,"token PLUS");
        RewriteRuleSubtreeStream stream_ftsTerm=new RewriteRuleSubtreeStream(adaptor,"rule ftsTerm");
        try {
            // W:\\workspace-cmis\\ANTLR\\FTS.g:136:2: ( PLUS ftsTerm -> ftsTerm )
            // W:\\workspace-cmis\\ANTLR\\FTS.g:136:4: PLUS ftsTerm
            {
            PLUS28=(Token)match(input,PLUS,FOLLOW_PLUS_in_ftsExactTerm527);  
            stream_PLUS.add(PLUS28);

            pushFollow(FOLLOW_ftsTerm_in_ftsExactTerm529);
            ftsTerm29=ftsTerm();

            state._fsp--;

            stream_ftsTerm.add(ftsTerm29.getTree());


            // AST REWRITE
            // elements: ftsTerm
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 137:3: -> ftsTerm
            {
                adaptor.addChild(root_0, stream_ftsTerm.nextTree());

            }

            retval.tree = root_0;retval.tree = root_0;
            }

            retval.stop = input.LT(-1);

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end ftsExactTerm

    public static class ftsPhrase_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start ftsPhrase
    // W:\\workspace-cmis\\ANTLR\\FTS.g:140:1: ftsPhrase : ( columnReference COLON )? FTSPHRASE -> FTSPHRASE ( columnReference )? ;
    public final FTSParser.ftsPhrase_return ftsPhrase() throws RecognitionException {
        FTSParser.ftsPhrase_return retval = new FTSParser.ftsPhrase_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token COLON31=null;
        Token FTSPHRASE32=null;
        FTSParser.columnReference_return columnReference30 = null;


        Object COLON31_tree=null;
        Object FTSPHRASE32_tree=null;
        RewriteRuleTokenStream stream_COLON=new RewriteRuleTokenStream(adaptor,"token COLON");
        RewriteRuleTokenStream stream_FTSPHRASE=new RewriteRuleTokenStream(adaptor,"token FTSPHRASE");
        RewriteRuleSubtreeStream stream_columnReference=new RewriteRuleSubtreeStream(adaptor,"rule columnReference");
        try {
            // W:\\workspace-cmis\\ANTLR\\FTS.g:141:2: ( ( columnReference COLON )? FTSPHRASE -> FTSPHRASE ( columnReference )? )
            // W:\\workspace-cmis\\ANTLR\\FTS.g:141:6: ( columnReference COLON )? FTSPHRASE
            {
            // W:\\workspace-cmis\\ANTLR\\FTS.g:141:6: ( columnReference COLON )?
            int alt9=2;
            int LA9_0 = input.LA(1);

            if ( (LA9_0==ID) ) {
                alt9=1;
            }
            switch (alt9) {
                case 1 :
                    // W:\\workspace-cmis\\ANTLR\\FTS.g:141:7: columnReference COLON
                    {
                    pushFollow(FOLLOW_columnReference_in_ftsPhrase550);
                    columnReference30=columnReference();

                    state._fsp--;

                    stream_columnReference.add(columnReference30.getTree());
                    COLON31=(Token)match(input,COLON,FOLLOW_COLON_in_ftsPhrase552);  
                    stream_COLON.add(COLON31);


                    }
                    break;

            }

            FTSPHRASE32=(Token)match(input,FTSPHRASE,FOLLOW_FTSPHRASE_in_ftsPhrase556);  
            stream_FTSPHRASE.add(FTSPHRASE32);



            // AST REWRITE
            // elements: columnReference, FTSPHRASE
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 142:3: -> FTSPHRASE ( columnReference )?
            {
                adaptor.addChild(root_0, stream_FTSPHRASE.nextNode());
                // W:\\workspace-cmis\\ANTLR\\FTS.g:142:16: ( columnReference )?
                if ( stream_columnReference.hasNext() ) {
                    adaptor.addChild(root_0, stream_columnReference.nextTree());

                }
                stream_columnReference.reset();

            }

            retval.tree = root_0;retval.tree = root_0;
            }

            retval.stop = input.LT(-1);

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end ftsPhrase

    public static class ftsSynonym_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start ftsSynonym
    // W:\\workspace-cmis\\ANTLR\\FTS.g:145:1: ftsSynonym : TILDA ftsTerm -> ftsTerm ;
    public final FTSParser.ftsSynonym_return ftsSynonym() throws RecognitionException {
        FTSParser.ftsSynonym_return retval = new FTSParser.ftsSynonym_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token TILDA33=null;
        FTSParser.ftsTerm_return ftsTerm34 = null;


        Object TILDA33_tree=null;
        RewriteRuleTokenStream stream_TILDA=new RewriteRuleTokenStream(adaptor,"token TILDA");
        RewriteRuleSubtreeStream stream_ftsTerm=new RewriteRuleSubtreeStream(adaptor,"rule ftsTerm");
        try {
            // W:\\workspace-cmis\\ANTLR\\FTS.g:146:2: ( TILDA ftsTerm -> ftsTerm )
            // W:\\workspace-cmis\\ANTLR\\FTS.g:146:4: TILDA ftsTerm
            {
            TILDA33=(Token)match(input,TILDA,FOLLOW_TILDA_in_ftsSynonym577);  
            stream_TILDA.add(TILDA33);

            pushFollow(FOLLOW_ftsTerm_in_ftsSynonym579);
            ftsTerm34=ftsTerm();

            state._fsp--;

            stream_ftsTerm.add(ftsTerm34.getTree());


            // AST REWRITE
            // elements: ftsTerm
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 147:3: -> ftsTerm
            {
                adaptor.addChild(root_0, stream_ftsTerm.nextTree());

            }

            retval.tree = root_0;retval.tree = root_0;
            }

            retval.stop = input.LT(-1);

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end ftsSynonym

    public static class ftsFieldGroup_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start ftsFieldGroup
    // W:\\workspace-cmis\\ANTLR\\FTS.g:151:1: ftsFieldGroup : columnReference COLON LPAREN ftsFieldGroupImplicitConjunctionOrDisjunction RPAREN -> ^( FIELD_GROUP columnReference ftsFieldGroupImplicitConjunctionOrDisjunction ) ;
    public final FTSParser.ftsFieldGroup_return ftsFieldGroup() throws RecognitionException {
        FTSParser.ftsFieldGroup_return retval = new FTSParser.ftsFieldGroup_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token COLON36=null;
        Token LPAREN37=null;
        Token RPAREN39=null;
        FTSParser.columnReference_return columnReference35 = null;

        FTSParser.ftsFieldGroupImplicitConjunctionOrDisjunction_return ftsFieldGroupImplicitConjunctionOrDisjunction38 = null;


        Object COLON36_tree=null;
        Object LPAREN37_tree=null;
        Object RPAREN39_tree=null;
        RewriteRuleTokenStream stream_COLON=new RewriteRuleTokenStream(adaptor,"token COLON");
        RewriteRuleTokenStream stream_RPAREN=new RewriteRuleTokenStream(adaptor,"token RPAREN");
        RewriteRuleTokenStream stream_LPAREN=new RewriteRuleTokenStream(adaptor,"token LPAREN");
        RewriteRuleSubtreeStream stream_columnReference=new RewriteRuleSubtreeStream(adaptor,"rule columnReference");
        RewriteRuleSubtreeStream stream_ftsFieldGroupImplicitConjunctionOrDisjunction=new RewriteRuleSubtreeStream(adaptor,"rule ftsFieldGroupImplicitConjunctionOrDisjunction");
        try {
            // W:\\workspace-cmis\\ANTLR\\FTS.g:152:2: ( columnReference COLON LPAREN ftsFieldGroupImplicitConjunctionOrDisjunction RPAREN -> ^( FIELD_GROUP columnReference ftsFieldGroupImplicitConjunctionOrDisjunction ) )
            // W:\\workspace-cmis\\ANTLR\\FTS.g:152:4: columnReference COLON LPAREN ftsFieldGroupImplicitConjunctionOrDisjunction RPAREN
            {
            pushFollow(FOLLOW_columnReference_in_ftsFieldGroup598);
            columnReference35=columnReference();

            state._fsp--;

            stream_columnReference.add(columnReference35.getTree());
            COLON36=(Token)match(input,COLON,FOLLOW_COLON_in_ftsFieldGroup600);  
            stream_COLON.add(COLON36);

            LPAREN37=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_ftsFieldGroup602);  
            stream_LPAREN.add(LPAREN37);

            pushFollow(FOLLOW_ftsFieldGroupImplicitConjunctionOrDisjunction_in_ftsFieldGroup604);
            ftsFieldGroupImplicitConjunctionOrDisjunction38=ftsFieldGroupImplicitConjunctionOrDisjunction();

            state._fsp--;

            stream_ftsFieldGroupImplicitConjunctionOrDisjunction.add(ftsFieldGroupImplicitConjunctionOrDisjunction38.getTree());
            RPAREN39=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_ftsFieldGroup606);  
            stream_RPAREN.add(RPAREN39);



            // AST REWRITE
            // elements: columnReference, ftsFieldGroupImplicitConjunctionOrDisjunction
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 153:3: -> ^( FIELD_GROUP columnReference ftsFieldGroupImplicitConjunctionOrDisjunction )
            {
                // W:\\workspace-cmis\\ANTLR\\FTS.g:153:6: ^( FIELD_GROUP columnReference ftsFieldGroupImplicitConjunctionOrDisjunction )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(FIELD_GROUP, "FIELD_GROUP"), root_1);

                adaptor.addChild(root_1, stream_columnReference.nextTree());
                adaptor.addChild(root_1, stream_ftsFieldGroupImplicitConjunctionOrDisjunction.nextTree());

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;retval.tree = root_0;
            }

            retval.stop = input.LT(-1);

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end ftsFieldGroup

    public static class ftsFieldGroupImplicitConjunctionOrDisjunction_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start ftsFieldGroupImplicitConjunctionOrDisjunction
    // W:\\workspace-cmis\\ANTLR\\FTS.g:156:1: ftsFieldGroupImplicitConjunctionOrDisjunction : ({...}? ftsFieldGroupExplicitDisjunction ( ftsFieldGroupExplicitDisjunction )* -> ^( FIELD_CONJUNCTION ( ftsFieldGroupExplicitDisjunction )+ ) | ftsFieldGroupExplicitDisjunction ( ftsFieldGroupExplicitDisjunction )* -> ^( FIELD_DISJUNCTION ( ftsFieldGroupExplicitDisjunction )+ ) );
    public final FTSParser.ftsFieldGroupImplicitConjunctionOrDisjunction_return ftsFieldGroupImplicitConjunctionOrDisjunction() throws RecognitionException {
        FTSParser.ftsFieldGroupImplicitConjunctionOrDisjunction_return retval = new FTSParser.ftsFieldGroupImplicitConjunctionOrDisjunction_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        FTSParser.ftsFieldGroupExplicitDisjunction_return ftsFieldGroupExplicitDisjunction40 = null;

        FTSParser.ftsFieldGroupExplicitDisjunction_return ftsFieldGroupExplicitDisjunction41 = null;

        FTSParser.ftsFieldGroupExplicitDisjunction_return ftsFieldGroupExplicitDisjunction42 = null;

        FTSParser.ftsFieldGroupExplicitDisjunction_return ftsFieldGroupExplicitDisjunction43 = null;


        RewriteRuleSubtreeStream stream_ftsFieldGroupExplicitDisjunction=new RewriteRuleSubtreeStream(adaptor,"rule ftsFieldGroupExplicitDisjunction");
        try {
            // W:\\workspace-cmis\\ANTLR\\FTS.g:157:2: ({...}? ftsFieldGroupExplicitDisjunction ( ftsFieldGroupExplicitDisjunction )* -> ^( FIELD_CONJUNCTION ( ftsFieldGroupExplicitDisjunction )+ ) | ftsFieldGroupExplicitDisjunction ( ftsFieldGroupExplicitDisjunction )* -> ^( FIELD_DISJUNCTION ( ftsFieldGroupExplicitDisjunction )+ ) )
            int alt12=2;
            switch ( input.LA(1) ) {
            case MINUS:
                {
                int LA12_1 = input.LA(2);

                if ( (defaultFieldConjunction()) ) {
                    alt12=1;
                }
                else if ( (true) ) {
                    alt12=2;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 12, 1, input);

                    throw nvae;
                }
                }
                break;
            case FTSWORD:
                {
                int LA12_2 = input.LA(2);

                if ( (defaultFieldConjunction()) ) {
                    alt12=1;
                }
                else if ( (true) ) {
                    alt12=2;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 12, 2, input);

                    throw nvae;
                }
                }
                break;
            case PLUS:
                {
                int LA12_3 = input.LA(2);

                if ( (defaultFieldConjunction()) ) {
                    alt12=1;
                }
                else if ( (true) ) {
                    alt12=2;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 12, 3, input);

                    throw nvae;
                }
                }
                break;
            case FTSPHRASE:
                {
                int LA12_4 = input.LA(2);

                if ( (defaultFieldConjunction()) ) {
                    alt12=1;
                }
                else if ( (true) ) {
                    alt12=2;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 12, 4, input);

                    throw nvae;
                }
                }
                break;
            case TILDA:
                {
                int LA12_5 = input.LA(2);

                if ( (defaultFieldConjunction()) ) {
                    alt12=1;
                }
                else if ( (true) ) {
                    alt12=2;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 12, 5, input);

                    throw nvae;
                }
                }
                break;
            case LPAREN:
                {
                int LA12_6 = input.LA(2);

                if ( (defaultFieldConjunction()) ) {
                    alt12=1;
                }
                else if ( (true) ) {
                    alt12=2;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 12, 6, input);

                    throw nvae;
                }
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 12, 0, input);

                throw nvae;
            }

            switch (alt12) {
                case 1 :
                    // W:\\workspace-cmis\\ANTLR\\FTS.g:157:4: {...}? ftsFieldGroupExplicitDisjunction ( ftsFieldGroupExplicitDisjunction )*
                    {
                    if ( !(defaultFieldConjunction()) ) {
                        throw new FailedPredicateException(input, "ftsFieldGroupImplicitConjunctionOrDisjunction", "defaultFieldConjunction()");
                    }
                    pushFollow(FOLLOW_ftsFieldGroupExplicitDisjunction_in_ftsFieldGroupImplicitConjunctionOrDisjunction632);
                    ftsFieldGroupExplicitDisjunction40=ftsFieldGroupExplicitDisjunction();

                    state._fsp--;

                    stream_ftsFieldGroupExplicitDisjunction.add(ftsFieldGroupExplicitDisjunction40.getTree());
                    // W:\\workspace-cmis\\ANTLR\\FTS.g:157:66: ( ftsFieldGroupExplicitDisjunction )*
                    loop10:
                    do {
                        int alt10=2;
                        int LA10_0 = input.LA(1);

                        if ( ((LA10_0>=MINUS && LA10_0<=LPAREN)||(LA10_0>=FTSWORD && LA10_0<=TILDA)) ) {
                            alt10=1;
                        }


                        switch (alt10) {
                    	case 1 :
                    	    // W:\\workspace-cmis\\ANTLR\\FTS.g:157:67: ftsFieldGroupExplicitDisjunction
                    	    {
                    	    pushFollow(FOLLOW_ftsFieldGroupExplicitDisjunction_in_ftsFieldGroupImplicitConjunctionOrDisjunction635);
                    	    ftsFieldGroupExplicitDisjunction41=ftsFieldGroupExplicitDisjunction();

                    	    state._fsp--;

                    	    stream_ftsFieldGroupExplicitDisjunction.add(ftsFieldGroupExplicitDisjunction41.getTree());

                    	    }
                    	    break;

                    	default :
                    	    break loop10;
                        }
                    } while (true);



                    // AST REWRITE
                    // elements: ftsFieldGroupExplicitDisjunction
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 158:3: -> ^( FIELD_CONJUNCTION ( ftsFieldGroupExplicitDisjunction )+ )
                    {
                        // W:\\workspace-cmis\\ANTLR\\FTS.g:158:6: ^( FIELD_CONJUNCTION ( ftsFieldGroupExplicitDisjunction )+ )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(FIELD_CONJUNCTION, "FIELD_CONJUNCTION"), root_1);

                        if ( !(stream_ftsFieldGroupExplicitDisjunction.hasNext()) ) {
                            throw new RewriteEarlyExitException();
                        }
                        while ( stream_ftsFieldGroupExplicitDisjunction.hasNext() ) {
                            adaptor.addChild(root_1, stream_ftsFieldGroupExplicitDisjunction.nextTree());

                        }
                        stream_ftsFieldGroupExplicitDisjunction.reset();

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;retval.tree = root_0;
                    }
                    break;
                case 2 :
                    // W:\\workspace-cmis\\ANTLR\\FTS.g:159:4: ftsFieldGroupExplicitDisjunction ( ftsFieldGroupExplicitDisjunction )*
                    {
                    pushFollow(FOLLOW_ftsFieldGroupExplicitDisjunction_in_ftsFieldGroupImplicitConjunctionOrDisjunction653);
                    ftsFieldGroupExplicitDisjunction42=ftsFieldGroupExplicitDisjunction();

                    state._fsp--;

                    stream_ftsFieldGroupExplicitDisjunction.add(ftsFieldGroupExplicitDisjunction42.getTree());
                    // W:\\workspace-cmis\\ANTLR\\FTS.g:159:37: ( ftsFieldGroupExplicitDisjunction )*
                    loop11:
                    do {
                        int alt11=2;
                        int LA11_0 = input.LA(1);

                        if ( ((LA11_0>=MINUS && LA11_0<=LPAREN)||(LA11_0>=FTSWORD && LA11_0<=TILDA)) ) {
                            alt11=1;
                        }


                        switch (alt11) {
                    	case 1 :
                    	    // W:\\workspace-cmis\\ANTLR\\FTS.g:159:38: ftsFieldGroupExplicitDisjunction
                    	    {
                    	    pushFollow(FOLLOW_ftsFieldGroupExplicitDisjunction_in_ftsFieldGroupImplicitConjunctionOrDisjunction656);
                    	    ftsFieldGroupExplicitDisjunction43=ftsFieldGroupExplicitDisjunction();

                    	    state._fsp--;

                    	    stream_ftsFieldGroupExplicitDisjunction.add(ftsFieldGroupExplicitDisjunction43.getTree());

                    	    }
                    	    break;

                    	default :
                    	    break loop11;
                        }
                    } while (true);



                    // AST REWRITE
                    // elements: ftsFieldGroupExplicitDisjunction
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 160:3: -> ^( FIELD_DISJUNCTION ( ftsFieldGroupExplicitDisjunction )+ )
                    {
                        // W:\\workspace-cmis\\ANTLR\\FTS.g:160:6: ^( FIELD_DISJUNCTION ( ftsFieldGroupExplicitDisjunction )+ )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(FIELD_DISJUNCTION, "FIELD_DISJUNCTION"), root_1);

                        if ( !(stream_ftsFieldGroupExplicitDisjunction.hasNext()) ) {
                            throw new RewriteEarlyExitException();
                        }
                        while ( stream_ftsFieldGroupExplicitDisjunction.hasNext() ) {
                            adaptor.addChild(root_1, stream_ftsFieldGroupExplicitDisjunction.nextTree());

                        }
                        stream_ftsFieldGroupExplicitDisjunction.reset();

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;retval.tree = root_0;
                    }
                    break;

            }
            retval.stop = input.LT(-1);

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end ftsFieldGroupImplicitConjunctionOrDisjunction

    public static class ftsFieldGroupExplicitDisjunction_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start ftsFieldGroupExplicitDisjunction
    // W:\\workspace-cmis\\ANTLR\\FTS.g:163:1: ftsFieldGroupExplicitDisjunction : ftsFieldGroupExplictConjunction ( OR ftsFieldGroupExplictConjunction )* -> ^( FIELD_DISJUNCTION ( ftsFieldGroupExplictConjunction )+ ) ;
    public final FTSParser.ftsFieldGroupExplicitDisjunction_return ftsFieldGroupExplicitDisjunction() throws RecognitionException {
        FTSParser.ftsFieldGroupExplicitDisjunction_return retval = new FTSParser.ftsFieldGroupExplicitDisjunction_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token OR45=null;
        FTSParser.ftsFieldGroupExplictConjunction_return ftsFieldGroupExplictConjunction44 = null;

        FTSParser.ftsFieldGroupExplictConjunction_return ftsFieldGroupExplictConjunction46 = null;


        Object OR45_tree=null;
        RewriteRuleTokenStream stream_OR=new RewriteRuleTokenStream(adaptor,"token OR");
        RewriteRuleSubtreeStream stream_ftsFieldGroupExplictConjunction=new RewriteRuleSubtreeStream(adaptor,"rule ftsFieldGroupExplictConjunction");
        try {
            // W:\\workspace-cmis\\ANTLR\\FTS.g:164:2: ( ftsFieldGroupExplictConjunction ( OR ftsFieldGroupExplictConjunction )* -> ^( FIELD_DISJUNCTION ( ftsFieldGroupExplictConjunction )+ ) )
            // W:\\workspace-cmis\\ANTLR\\FTS.g:164:4: ftsFieldGroupExplictConjunction ( OR ftsFieldGroupExplictConjunction )*
            {
            pushFollow(FOLLOW_ftsFieldGroupExplictConjunction_in_ftsFieldGroupExplicitDisjunction681);
            ftsFieldGroupExplictConjunction44=ftsFieldGroupExplictConjunction();

            state._fsp--;

            stream_ftsFieldGroupExplictConjunction.add(ftsFieldGroupExplictConjunction44.getTree());
            // W:\\workspace-cmis\\ANTLR\\FTS.g:164:36: ( OR ftsFieldGroupExplictConjunction )*
            loop13:
            do {
                int alt13=2;
                int LA13_0 = input.LA(1);

                if ( (LA13_0==OR) ) {
                    alt13=1;
                }


                switch (alt13) {
            	case 1 :
            	    // W:\\workspace-cmis\\ANTLR\\FTS.g:164:37: OR ftsFieldGroupExplictConjunction
            	    {
            	    OR45=(Token)match(input,OR,FOLLOW_OR_in_ftsFieldGroupExplicitDisjunction684);  
            	    stream_OR.add(OR45);

            	    pushFollow(FOLLOW_ftsFieldGroupExplictConjunction_in_ftsFieldGroupExplicitDisjunction686);
            	    ftsFieldGroupExplictConjunction46=ftsFieldGroupExplictConjunction();

            	    state._fsp--;

            	    stream_ftsFieldGroupExplictConjunction.add(ftsFieldGroupExplictConjunction46.getTree());

            	    }
            	    break;

            	default :
            	    break loop13;
                }
            } while (true);



            // AST REWRITE
            // elements: ftsFieldGroupExplictConjunction
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 165:3: -> ^( FIELD_DISJUNCTION ( ftsFieldGroupExplictConjunction )+ )
            {
                // W:\\workspace-cmis\\ANTLR\\FTS.g:165:6: ^( FIELD_DISJUNCTION ( ftsFieldGroupExplictConjunction )+ )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(FIELD_DISJUNCTION, "FIELD_DISJUNCTION"), root_1);

                if ( !(stream_ftsFieldGroupExplictConjunction.hasNext()) ) {
                    throw new RewriteEarlyExitException();
                }
                while ( stream_ftsFieldGroupExplictConjunction.hasNext() ) {
                    adaptor.addChild(root_1, stream_ftsFieldGroupExplictConjunction.nextTree());

                }
                stream_ftsFieldGroupExplictConjunction.reset();

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;retval.tree = root_0;
            }

            retval.stop = input.LT(-1);

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end ftsFieldGroupExplicitDisjunction

    public static class ftsFieldGroupExplictConjunction_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start ftsFieldGroupExplictConjunction
    // W:\\workspace-cmis\\ANTLR\\FTS.g:168:1: ftsFieldGroupExplictConjunction : ftsFieldGroupNot ( AND ftsFieldGroupNot )* -> ^( FIELD_CONJUNCTION ( ftsFieldGroupNot )+ ) ;
    public final FTSParser.ftsFieldGroupExplictConjunction_return ftsFieldGroupExplictConjunction() throws RecognitionException {
        FTSParser.ftsFieldGroupExplictConjunction_return retval = new FTSParser.ftsFieldGroupExplictConjunction_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token AND48=null;
        FTSParser.ftsFieldGroupNot_return ftsFieldGroupNot47 = null;

        FTSParser.ftsFieldGroupNot_return ftsFieldGroupNot49 = null;


        Object AND48_tree=null;
        RewriteRuleTokenStream stream_AND=new RewriteRuleTokenStream(adaptor,"token AND");
        RewriteRuleSubtreeStream stream_ftsFieldGroupNot=new RewriteRuleSubtreeStream(adaptor,"rule ftsFieldGroupNot");
        try {
            // W:\\workspace-cmis\\ANTLR\\FTS.g:169:2: ( ftsFieldGroupNot ( AND ftsFieldGroupNot )* -> ^( FIELD_CONJUNCTION ( ftsFieldGroupNot )+ ) )
            // W:\\workspace-cmis\\ANTLR\\FTS.g:169:4: ftsFieldGroupNot ( AND ftsFieldGroupNot )*
            {
            pushFollow(FOLLOW_ftsFieldGroupNot_in_ftsFieldGroupExplictConjunction711);
            ftsFieldGroupNot47=ftsFieldGroupNot();

            state._fsp--;

            stream_ftsFieldGroupNot.add(ftsFieldGroupNot47.getTree());
            // W:\\workspace-cmis\\ANTLR\\FTS.g:169:21: ( AND ftsFieldGroupNot )*
            loop14:
            do {
                int alt14=2;
                int LA14_0 = input.LA(1);

                if ( (LA14_0==AND) ) {
                    alt14=1;
                }


                switch (alt14) {
            	case 1 :
            	    // W:\\workspace-cmis\\ANTLR\\FTS.g:169:22: AND ftsFieldGroupNot
            	    {
            	    AND48=(Token)match(input,AND,FOLLOW_AND_in_ftsFieldGroupExplictConjunction714);  
            	    stream_AND.add(AND48);

            	    pushFollow(FOLLOW_ftsFieldGroupNot_in_ftsFieldGroupExplictConjunction716);
            	    ftsFieldGroupNot49=ftsFieldGroupNot();

            	    state._fsp--;

            	    stream_ftsFieldGroupNot.add(ftsFieldGroupNot49.getTree());

            	    }
            	    break;

            	default :
            	    break loop14;
                }
            } while (true);



            // AST REWRITE
            // elements: ftsFieldGroupNot
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 170:3: -> ^( FIELD_CONJUNCTION ( ftsFieldGroupNot )+ )
            {
                // W:\\workspace-cmis\\ANTLR\\FTS.g:170:6: ^( FIELD_CONJUNCTION ( ftsFieldGroupNot )+ )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(FIELD_CONJUNCTION, "FIELD_CONJUNCTION"), root_1);

                if ( !(stream_ftsFieldGroupNot.hasNext()) ) {
                    throw new RewriteEarlyExitException();
                }
                while ( stream_ftsFieldGroupNot.hasNext() ) {
                    adaptor.addChild(root_1, stream_ftsFieldGroupNot.nextTree());

                }
                stream_ftsFieldGroupNot.reset();

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;retval.tree = root_0;
            }

            retval.stop = input.LT(-1);

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end ftsFieldGroupExplictConjunction

    public static class ftsFieldGroupNot_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start ftsFieldGroupNot
    // W:\\workspace-cmis\\ANTLR\\FTS.g:174:1: ftsFieldGroupNot : ( MINUS ftsFieldGroupTest -> FIELD_NEGATION ftsFieldGroupTest | ftsFieldGroupTest -> ftsFieldGroupTest );
    public final FTSParser.ftsFieldGroupNot_return ftsFieldGroupNot() throws RecognitionException {
        FTSParser.ftsFieldGroupNot_return retval = new FTSParser.ftsFieldGroupNot_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token MINUS50=null;
        FTSParser.ftsFieldGroupTest_return ftsFieldGroupTest51 = null;

        FTSParser.ftsFieldGroupTest_return ftsFieldGroupTest52 = null;


        Object MINUS50_tree=null;
        RewriteRuleTokenStream stream_MINUS=new RewriteRuleTokenStream(adaptor,"token MINUS");
        RewriteRuleSubtreeStream stream_ftsFieldGroupTest=new RewriteRuleSubtreeStream(adaptor,"rule ftsFieldGroupTest");
        try {
            // W:\\workspace-cmis\\ANTLR\\FTS.g:174:19: ( MINUS ftsFieldGroupTest -> FIELD_NEGATION ftsFieldGroupTest | ftsFieldGroupTest -> ftsFieldGroupTest )
            int alt15=2;
            int LA15_0 = input.LA(1);

            if ( (LA15_0==MINUS) ) {
                alt15=1;
            }
            else if ( (LA15_0==LPAREN||(LA15_0>=FTSWORD && LA15_0<=TILDA)) ) {
                alt15=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 15, 0, input);

                throw nvae;
            }
            switch (alt15) {
                case 1 :
                    // W:\\workspace-cmis\\ANTLR\\FTS.g:174:21: MINUS ftsFieldGroupTest
                    {
                    MINUS50=(Token)match(input,MINUS,FOLLOW_MINUS_in_ftsFieldGroupNot743);  
                    stream_MINUS.add(MINUS50);

                    pushFollow(FOLLOW_ftsFieldGroupTest_in_ftsFieldGroupNot745);
                    ftsFieldGroupTest51=ftsFieldGroupTest();

                    state._fsp--;

                    stream_ftsFieldGroupTest.add(ftsFieldGroupTest51.getTree());


                    // AST REWRITE
                    // elements: ftsFieldGroupTest
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 175:4: -> FIELD_NEGATION ftsFieldGroupTest
                    {
                        adaptor.addChild(root_0, (Object)adaptor.create(FIELD_NEGATION, "FIELD_NEGATION"));
                        adaptor.addChild(root_0, stream_ftsFieldGroupTest.nextTree());

                    }

                    retval.tree = root_0;retval.tree = root_0;
                    }
                    break;
                case 2 :
                    // W:\\workspace-cmis\\ANTLR\\FTS.g:176:5: ftsFieldGroupTest
                    {
                    pushFollow(FOLLOW_ftsFieldGroupTest_in_ftsFieldGroupNot760);
                    ftsFieldGroupTest52=ftsFieldGroupTest();

                    state._fsp--;

                    stream_ftsFieldGroupTest.add(ftsFieldGroupTest52.getTree());


                    // AST REWRITE
                    // elements: ftsFieldGroupTest
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 177:4: -> ftsFieldGroupTest
                    {
                        adaptor.addChild(root_0, stream_ftsFieldGroupTest.nextTree());

                    }

                    retval.tree = root_0;retval.tree = root_0;
                    }
                    break;

            }
            retval.stop = input.LT(-1);

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end ftsFieldGroupNot

    public static class ftsFieldGroupTest_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start ftsFieldGroupTest
    // W:\\workspace-cmis\\ANTLR\\FTS.g:181:1: ftsFieldGroupTest : ( ftsFieldGroupTerm -> ^( FG_TERM ftsFieldGroupTerm ) | ftsFieldGroupExactTerm -> ^( FG_EXACT_TERM ftsFieldGroupExactTerm ) | ftsFieldGroupPhrase -> ^( FG_PHRASE ftsFieldGroupPhrase ) | ftsFieldGroupSynonym -> ^( FG_SYNONYM ftsFieldGroupSynonym ) | ftsFieldGroupProximity -> ^( FG_PROXIMITY ftsFieldGroupProximity ) | ftsFieldGroupRange -> ^( FG_RANGE ftsFieldGroupRange ) | LPAREN ftsFieldGroupImplicitConjunctionOrDisjunction RPAREN -> ftsFieldGroupImplicitConjunctionOrDisjunction );
    public final FTSParser.ftsFieldGroupTest_return ftsFieldGroupTest() throws RecognitionException {
        FTSParser.ftsFieldGroupTest_return retval = new FTSParser.ftsFieldGroupTest_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token LPAREN59=null;
        Token RPAREN61=null;
        FTSParser.ftsFieldGroupTerm_return ftsFieldGroupTerm53 = null;

        FTSParser.ftsFieldGroupExactTerm_return ftsFieldGroupExactTerm54 = null;

        FTSParser.ftsFieldGroupPhrase_return ftsFieldGroupPhrase55 = null;

        FTSParser.ftsFieldGroupSynonym_return ftsFieldGroupSynonym56 = null;

        FTSParser.ftsFieldGroupProximity_return ftsFieldGroupProximity57 = null;

        FTSParser.ftsFieldGroupRange_return ftsFieldGroupRange58 = null;

        FTSParser.ftsFieldGroupImplicitConjunctionOrDisjunction_return ftsFieldGroupImplicitConjunctionOrDisjunction60 = null;


        Object LPAREN59_tree=null;
        Object RPAREN61_tree=null;
        RewriteRuleTokenStream stream_RPAREN=new RewriteRuleTokenStream(adaptor,"token RPAREN");
        RewriteRuleTokenStream stream_LPAREN=new RewriteRuleTokenStream(adaptor,"token LPAREN");
        RewriteRuleSubtreeStream stream_ftsFieldGroupRange=new RewriteRuleSubtreeStream(adaptor,"rule ftsFieldGroupRange");
        RewriteRuleSubtreeStream stream_ftsFieldGroupPhrase=new RewriteRuleSubtreeStream(adaptor,"rule ftsFieldGroupPhrase");
        RewriteRuleSubtreeStream stream_ftsFieldGroupImplicitConjunctionOrDisjunction=new RewriteRuleSubtreeStream(adaptor,"rule ftsFieldGroupImplicitConjunctionOrDisjunction");
        RewriteRuleSubtreeStream stream_ftsFieldGroupTerm=new RewriteRuleSubtreeStream(adaptor,"rule ftsFieldGroupTerm");
        RewriteRuleSubtreeStream stream_ftsFieldGroupSynonym=new RewriteRuleSubtreeStream(adaptor,"rule ftsFieldGroupSynonym");
        RewriteRuleSubtreeStream stream_ftsFieldGroupExactTerm=new RewriteRuleSubtreeStream(adaptor,"rule ftsFieldGroupExactTerm");
        RewriteRuleSubtreeStream stream_ftsFieldGroupProximity=new RewriteRuleSubtreeStream(adaptor,"rule ftsFieldGroupProximity");
        try {
            // W:\\workspace-cmis\\ANTLR\\FTS.g:182:2: ( ftsFieldGroupTerm -> ^( FG_TERM ftsFieldGroupTerm ) | ftsFieldGroupExactTerm -> ^( FG_EXACT_TERM ftsFieldGroupExactTerm ) | ftsFieldGroupPhrase -> ^( FG_PHRASE ftsFieldGroupPhrase ) | ftsFieldGroupSynonym -> ^( FG_SYNONYM ftsFieldGroupSynonym ) | ftsFieldGroupProximity -> ^( FG_PROXIMITY ftsFieldGroupProximity ) | ftsFieldGroupRange -> ^( FG_RANGE ftsFieldGroupRange ) | LPAREN ftsFieldGroupImplicitConjunctionOrDisjunction RPAREN -> ftsFieldGroupImplicitConjunctionOrDisjunction )
            int alt16=7;
            switch ( input.LA(1) ) {
            case FTSWORD:
                {
                switch ( input.LA(2) ) {
                case STAR:
                    {
                    alt16=5;
                    }
                    break;
                case OR:
                case AND:
                case MINUS:
                case LPAREN:
                case RPAREN:
                case FTSWORD:
                case PLUS:
                case FTSPHRASE:
                case TILDA:
                    {
                    alt16=1;
                    }
                    break;
                case DOTDOT:
                    {
                    alt16=6;
                    }
                    break;
                default:
                    NoViableAltException nvae =
                        new NoViableAltException("", 16, 1, input);

                    throw nvae;
                }

                }
                break;
            case PLUS:
                {
                alt16=2;
                }
                break;
            case FTSPHRASE:
                {
                alt16=3;
                }
                break;
            case TILDA:
                {
                alt16=4;
                }
                break;
            case LPAREN:
                {
                alt16=7;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 16, 0, input);

                throw nvae;
            }

            switch (alt16) {
                case 1 :
                    // W:\\workspace-cmis\\ANTLR\\FTS.g:182:4: ftsFieldGroupTerm
                    {
                    pushFollow(FOLLOW_ftsFieldGroupTerm_in_ftsFieldGroupTest779);
                    ftsFieldGroupTerm53=ftsFieldGroupTerm();

                    state._fsp--;

                    stream_ftsFieldGroupTerm.add(ftsFieldGroupTerm53.getTree());


                    // AST REWRITE
                    // elements: ftsFieldGroupTerm
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 183:3: -> ^( FG_TERM ftsFieldGroupTerm )
                    {
                        // W:\\workspace-cmis\\ANTLR\\FTS.g:183:6: ^( FG_TERM ftsFieldGroupTerm )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(FG_TERM, "FG_TERM"), root_1);

                        adaptor.addChild(root_1, stream_ftsFieldGroupTerm.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;retval.tree = root_0;
                    }
                    break;
                case 2 :
                    // W:\\workspace-cmis\\ANTLR\\FTS.g:184:4: ftsFieldGroupExactTerm
                    {
                    pushFollow(FOLLOW_ftsFieldGroupExactTerm_in_ftsFieldGroupTest794);
                    ftsFieldGroupExactTerm54=ftsFieldGroupExactTerm();

                    state._fsp--;

                    stream_ftsFieldGroupExactTerm.add(ftsFieldGroupExactTerm54.getTree());


                    // AST REWRITE
                    // elements: ftsFieldGroupExactTerm
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 185:3: -> ^( FG_EXACT_TERM ftsFieldGroupExactTerm )
                    {
                        // W:\\workspace-cmis\\ANTLR\\FTS.g:185:6: ^( FG_EXACT_TERM ftsFieldGroupExactTerm )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(FG_EXACT_TERM, "FG_EXACT_TERM"), root_1);

                        adaptor.addChild(root_1, stream_ftsFieldGroupExactTerm.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;retval.tree = root_0;
                    }
                    break;
                case 3 :
                    // W:\\workspace-cmis\\ANTLR\\FTS.g:186:4: ftsFieldGroupPhrase
                    {
                    pushFollow(FOLLOW_ftsFieldGroupPhrase_in_ftsFieldGroupTest810);
                    ftsFieldGroupPhrase55=ftsFieldGroupPhrase();

                    state._fsp--;

                    stream_ftsFieldGroupPhrase.add(ftsFieldGroupPhrase55.getTree());


                    // AST REWRITE
                    // elements: ftsFieldGroupPhrase
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 187:3: -> ^( FG_PHRASE ftsFieldGroupPhrase )
                    {
                        // W:\\workspace-cmis\\ANTLR\\FTS.g:187:6: ^( FG_PHRASE ftsFieldGroupPhrase )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(FG_PHRASE, "FG_PHRASE"), root_1);

                        adaptor.addChild(root_1, stream_ftsFieldGroupPhrase.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;retval.tree = root_0;
                    }
                    break;
                case 4 :
                    // W:\\workspace-cmis\\ANTLR\\FTS.g:188:4: ftsFieldGroupSynonym
                    {
                    pushFollow(FOLLOW_ftsFieldGroupSynonym_in_ftsFieldGroupTest825);
                    ftsFieldGroupSynonym56=ftsFieldGroupSynonym();

                    state._fsp--;

                    stream_ftsFieldGroupSynonym.add(ftsFieldGroupSynonym56.getTree());


                    // AST REWRITE
                    // elements: ftsFieldGroupSynonym
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 189:3: -> ^( FG_SYNONYM ftsFieldGroupSynonym )
                    {
                        // W:\\workspace-cmis\\ANTLR\\FTS.g:189:6: ^( FG_SYNONYM ftsFieldGroupSynonym )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(FG_SYNONYM, "FG_SYNONYM"), root_1);

                        adaptor.addChild(root_1, stream_ftsFieldGroupSynonym.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;retval.tree = root_0;
                    }
                    break;
                case 5 :
                    // W:\\workspace-cmis\\ANTLR\\FTS.g:190:10: ftsFieldGroupProximity
                    {
                    pushFollow(FOLLOW_ftsFieldGroupProximity_in_ftsFieldGroupTest846);
                    ftsFieldGroupProximity57=ftsFieldGroupProximity();

                    state._fsp--;

                    stream_ftsFieldGroupProximity.add(ftsFieldGroupProximity57.getTree());


                    // AST REWRITE
                    // elements: ftsFieldGroupProximity
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 191:3: -> ^( FG_PROXIMITY ftsFieldGroupProximity )
                    {
                        // W:\\workspace-cmis\\ANTLR\\FTS.g:191:6: ^( FG_PROXIMITY ftsFieldGroupProximity )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(FG_PROXIMITY, "FG_PROXIMITY"), root_1);

                        adaptor.addChild(root_1, stream_ftsFieldGroupProximity.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;retval.tree = root_0;
                    }
                    break;
                case 6 :
                    // W:\\workspace-cmis\\ANTLR\\FTS.g:192:12: ftsFieldGroupRange
                    {
                    pushFollow(FOLLOW_ftsFieldGroupRange_in_ftsFieldGroupTest871);
                    ftsFieldGroupRange58=ftsFieldGroupRange();

                    state._fsp--;

                    stream_ftsFieldGroupRange.add(ftsFieldGroupRange58.getTree());


                    // AST REWRITE
                    // elements: ftsFieldGroupRange
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 193:10: -> ^( FG_RANGE ftsFieldGroupRange )
                    {
                        // W:\\workspace-cmis\\ANTLR\\FTS.g:193:13: ^( FG_RANGE ftsFieldGroupRange )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(FG_RANGE, "FG_RANGE"), root_1);

                        adaptor.addChild(root_1, stream_ftsFieldGroupRange.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;retval.tree = root_0;
                    }
                    break;
                case 7 :
                    // W:\\workspace-cmis\\ANTLR\\FTS.g:194:4: LPAREN ftsFieldGroupImplicitConjunctionOrDisjunction RPAREN
                    {
                    LPAREN59=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_ftsFieldGroupTest893);  
                    stream_LPAREN.add(LPAREN59);

                    pushFollow(FOLLOW_ftsFieldGroupImplicitConjunctionOrDisjunction_in_ftsFieldGroupTest895);
                    ftsFieldGroupImplicitConjunctionOrDisjunction60=ftsFieldGroupImplicitConjunctionOrDisjunction();

                    state._fsp--;

                    stream_ftsFieldGroupImplicitConjunctionOrDisjunction.add(ftsFieldGroupImplicitConjunctionOrDisjunction60.getTree());
                    RPAREN61=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_ftsFieldGroupTest897);  
                    stream_RPAREN.add(RPAREN61);



                    // AST REWRITE
                    // elements: ftsFieldGroupImplicitConjunctionOrDisjunction
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 195:3: -> ftsFieldGroupImplicitConjunctionOrDisjunction
                    {
                        adaptor.addChild(root_0, stream_ftsFieldGroupImplicitConjunctionOrDisjunction.nextTree());

                    }

                    retval.tree = root_0;retval.tree = root_0;
                    }
                    break;

            }
            retval.stop = input.LT(-1);

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end ftsFieldGroupTest

    public static class ftsFieldGroupTerm_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start ftsFieldGroupTerm
    // W:\\workspace-cmis\\ANTLR\\FTS.g:198:1: ftsFieldGroupTerm : FTSWORD ;
    public final FTSParser.ftsFieldGroupTerm_return ftsFieldGroupTerm() throws RecognitionException {
        FTSParser.ftsFieldGroupTerm_return retval = new FTSParser.ftsFieldGroupTerm_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token FTSWORD62=null;

        Object FTSWORD62_tree=null;

        try {
            // W:\\workspace-cmis\\ANTLR\\FTS.g:199:2: ( FTSWORD )
            // W:\\workspace-cmis\\ANTLR\\FTS.g:199:4: FTSWORD
            {
            root_0 = (Object)adaptor.nil();

            FTSWORD62=(Token)match(input,FTSWORD,FOLLOW_FTSWORD_in_ftsFieldGroupTerm915); 
            FTSWORD62_tree = (Object)adaptor.create(FTSWORD62);
            adaptor.addChild(root_0, FTSWORD62_tree);


            }

            retval.stop = input.LT(-1);

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end ftsFieldGroupTerm

    public static class ftsFieldGroupExactTerm_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start ftsFieldGroupExactTerm
    // W:\\workspace-cmis\\ANTLR\\FTS.g:202:1: ftsFieldGroupExactTerm : PLUS ftsFieldGroupTerm -> ftsFieldGroupTerm ;
    public final FTSParser.ftsFieldGroupExactTerm_return ftsFieldGroupExactTerm() throws RecognitionException {
        FTSParser.ftsFieldGroupExactTerm_return retval = new FTSParser.ftsFieldGroupExactTerm_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token PLUS63=null;
        FTSParser.ftsFieldGroupTerm_return ftsFieldGroupTerm64 = null;


        Object PLUS63_tree=null;
        RewriteRuleTokenStream stream_PLUS=new RewriteRuleTokenStream(adaptor,"token PLUS");
        RewriteRuleSubtreeStream stream_ftsFieldGroupTerm=new RewriteRuleSubtreeStream(adaptor,"rule ftsFieldGroupTerm");
        try {
            // W:\\workspace-cmis\\ANTLR\\FTS.g:203:2: ( PLUS ftsFieldGroupTerm -> ftsFieldGroupTerm )
            // W:\\workspace-cmis\\ANTLR\\FTS.g:203:4: PLUS ftsFieldGroupTerm
            {
            PLUS63=(Token)match(input,PLUS,FOLLOW_PLUS_in_ftsFieldGroupExactTerm927);  
            stream_PLUS.add(PLUS63);

            pushFollow(FOLLOW_ftsFieldGroupTerm_in_ftsFieldGroupExactTerm929);
            ftsFieldGroupTerm64=ftsFieldGroupTerm();

            state._fsp--;

            stream_ftsFieldGroupTerm.add(ftsFieldGroupTerm64.getTree());


            // AST REWRITE
            // elements: ftsFieldGroupTerm
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 204:3: -> ftsFieldGroupTerm
            {
                adaptor.addChild(root_0, stream_ftsFieldGroupTerm.nextTree());

            }

            retval.tree = root_0;retval.tree = root_0;
            }

            retval.stop = input.LT(-1);

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end ftsFieldGroupExactTerm

    public static class ftsFieldGroupPhrase_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start ftsFieldGroupPhrase
    // W:\\workspace-cmis\\ANTLR\\FTS.g:207:1: ftsFieldGroupPhrase : FTSPHRASE ;
    public final FTSParser.ftsFieldGroupPhrase_return ftsFieldGroupPhrase() throws RecognitionException {
        FTSParser.ftsFieldGroupPhrase_return retval = new FTSParser.ftsFieldGroupPhrase_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token FTSPHRASE65=null;

        Object FTSPHRASE65_tree=null;

        try {
            // W:\\workspace-cmis\\ANTLR\\FTS.g:208:2: ( FTSPHRASE )
            // W:\\workspace-cmis\\ANTLR\\FTS.g:208:6: FTSPHRASE
            {
            root_0 = (Object)adaptor.nil();

            FTSPHRASE65=(Token)match(input,FTSPHRASE,FOLLOW_FTSPHRASE_in_ftsFieldGroupPhrase949); 
            FTSPHRASE65_tree = (Object)adaptor.create(FTSPHRASE65);
            adaptor.addChild(root_0, FTSPHRASE65_tree);


            }

            retval.stop = input.LT(-1);

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end ftsFieldGroupPhrase

    public static class ftsFieldGroupSynonym_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start ftsFieldGroupSynonym
    // W:\\workspace-cmis\\ANTLR\\FTS.g:211:1: ftsFieldGroupSynonym : TILDA ftsFieldGroupTerm -> ftsFieldGroupTerm ;
    public final FTSParser.ftsFieldGroupSynonym_return ftsFieldGroupSynonym() throws RecognitionException {
        FTSParser.ftsFieldGroupSynonym_return retval = new FTSParser.ftsFieldGroupSynonym_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token TILDA66=null;
        FTSParser.ftsFieldGroupTerm_return ftsFieldGroupTerm67 = null;


        Object TILDA66_tree=null;
        RewriteRuleTokenStream stream_TILDA=new RewriteRuleTokenStream(adaptor,"token TILDA");
        RewriteRuleSubtreeStream stream_ftsFieldGroupTerm=new RewriteRuleSubtreeStream(adaptor,"rule ftsFieldGroupTerm");
        try {
            // W:\\workspace-cmis\\ANTLR\\FTS.g:212:2: ( TILDA ftsFieldGroupTerm -> ftsFieldGroupTerm )
            // W:\\workspace-cmis\\ANTLR\\FTS.g:212:4: TILDA ftsFieldGroupTerm
            {
            TILDA66=(Token)match(input,TILDA,FOLLOW_TILDA_in_ftsFieldGroupSynonym961);  
            stream_TILDA.add(TILDA66);

            pushFollow(FOLLOW_ftsFieldGroupTerm_in_ftsFieldGroupSynonym963);
            ftsFieldGroupTerm67=ftsFieldGroupTerm();

            state._fsp--;

            stream_ftsFieldGroupTerm.add(ftsFieldGroupTerm67.getTree());


            // AST REWRITE
            // elements: ftsFieldGroupTerm
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 213:3: -> ftsFieldGroupTerm
            {
                adaptor.addChild(root_0, stream_ftsFieldGroupTerm.nextTree());

            }

            retval.tree = root_0;retval.tree = root_0;
            }

            retval.stop = input.LT(-1);

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end ftsFieldGroupSynonym

    public static class ftsFieldGroupProximity_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start ftsFieldGroupProximity
    // W:\\workspace-cmis\\ANTLR\\FTS.g:216:1: ftsFieldGroupProximity : ftsFieldGroupTerm STAR ftsFieldGroupTerm -> ftsFieldGroupTerm ftsFieldGroupTerm ;
    public final FTSParser.ftsFieldGroupProximity_return ftsFieldGroupProximity() throws RecognitionException {
        FTSParser.ftsFieldGroupProximity_return retval = new FTSParser.ftsFieldGroupProximity_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token STAR69=null;
        FTSParser.ftsFieldGroupTerm_return ftsFieldGroupTerm68 = null;

        FTSParser.ftsFieldGroupTerm_return ftsFieldGroupTerm70 = null;


        Object STAR69_tree=null;
        RewriteRuleTokenStream stream_STAR=new RewriteRuleTokenStream(adaptor,"token STAR");
        RewriteRuleSubtreeStream stream_ftsFieldGroupTerm=new RewriteRuleSubtreeStream(adaptor,"rule ftsFieldGroupTerm");
        try {
            // W:\\workspace-cmis\\ANTLR\\FTS.g:217:2: ( ftsFieldGroupTerm STAR ftsFieldGroupTerm -> ftsFieldGroupTerm ftsFieldGroupTerm )
            // W:\\workspace-cmis\\ANTLR\\FTS.g:217:4: ftsFieldGroupTerm STAR ftsFieldGroupTerm
            {
            pushFollow(FOLLOW_ftsFieldGroupTerm_in_ftsFieldGroupProximity981);
            ftsFieldGroupTerm68=ftsFieldGroupTerm();

            state._fsp--;

            stream_ftsFieldGroupTerm.add(ftsFieldGroupTerm68.getTree());
            STAR69=(Token)match(input,STAR,FOLLOW_STAR_in_ftsFieldGroupProximity983);  
            stream_STAR.add(STAR69);

            pushFollow(FOLLOW_ftsFieldGroupTerm_in_ftsFieldGroupProximity985);
            ftsFieldGroupTerm70=ftsFieldGroupTerm();

            state._fsp--;

            stream_ftsFieldGroupTerm.add(ftsFieldGroupTerm70.getTree());


            // AST REWRITE
            // elements: ftsFieldGroupTerm, ftsFieldGroupTerm
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 218:3: -> ftsFieldGroupTerm ftsFieldGroupTerm
            {
                adaptor.addChild(root_0, stream_ftsFieldGroupTerm.nextTree());
                adaptor.addChild(root_0, stream_ftsFieldGroupTerm.nextTree());

            }

            retval.tree = root_0;retval.tree = root_0;
            }

            retval.stop = input.LT(-1);

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end ftsFieldGroupProximity

    public static class ftsFieldGroupRange_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start ftsFieldGroupRange
    // W:\\workspace-cmis\\ANTLR\\FTS.g:221:1: ftsFieldGroupRange : ftsFieldGroupTerm DOTDOT ftsFieldGroupTerm -> ftsFieldGroupTerm ftsFieldGroupTerm ;
    public final FTSParser.ftsFieldGroupRange_return ftsFieldGroupRange() throws RecognitionException {
        FTSParser.ftsFieldGroupRange_return retval = new FTSParser.ftsFieldGroupRange_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token DOTDOT72=null;
        FTSParser.ftsFieldGroupTerm_return ftsFieldGroupTerm71 = null;

        FTSParser.ftsFieldGroupTerm_return ftsFieldGroupTerm73 = null;


        Object DOTDOT72_tree=null;
        RewriteRuleTokenStream stream_DOTDOT=new RewriteRuleTokenStream(adaptor,"token DOTDOT");
        RewriteRuleSubtreeStream stream_ftsFieldGroupTerm=new RewriteRuleSubtreeStream(adaptor,"rule ftsFieldGroupTerm");
        try {
            // W:\\workspace-cmis\\ANTLR\\FTS.g:221:19: ( ftsFieldGroupTerm DOTDOT ftsFieldGroupTerm -> ftsFieldGroupTerm ftsFieldGroupTerm )
            // W:\\workspace-cmis\\ANTLR\\FTS.g:221:21: ftsFieldGroupTerm DOTDOT ftsFieldGroupTerm
            {
            pushFollow(FOLLOW_ftsFieldGroupTerm_in_ftsFieldGroupRange1003);
            ftsFieldGroupTerm71=ftsFieldGroupTerm();

            state._fsp--;

            stream_ftsFieldGroupTerm.add(ftsFieldGroupTerm71.getTree());
            DOTDOT72=(Token)match(input,DOTDOT,FOLLOW_DOTDOT_in_ftsFieldGroupRange1005);  
            stream_DOTDOT.add(DOTDOT72);

            pushFollow(FOLLOW_ftsFieldGroupTerm_in_ftsFieldGroupRange1007);
            ftsFieldGroupTerm73=ftsFieldGroupTerm();

            state._fsp--;

            stream_ftsFieldGroupTerm.add(ftsFieldGroupTerm73.getTree());


            // AST REWRITE
            // elements: ftsFieldGroupTerm, ftsFieldGroupTerm
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 222:3: -> ftsFieldGroupTerm ftsFieldGroupTerm
            {
                adaptor.addChild(root_0, stream_ftsFieldGroupTerm.nextTree());
                adaptor.addChild(root_0, stream_ftsFieldGroupTerm.nextTree());

            }

            retval.tree = root_0;retval.tree = root_0;
            }

            retval.stop = input.LT(-1);

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end ftsFieldGroupRange

    public static class columnReference_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start columnReference
    // W:\\workspace-cmis\\ANTLR\\FTS.g:225:1: columnReference : (qualifier= identifier DOT )? name= identifier -> ^( COLUMN_REF $name ( $qualifier)? ) ;
    public final FTSParser.columnReference_return columnReference() throws RecognitionException {
        FTSParser.columnReference_return retval = new FTSParser.columnReference_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token DOT74=null;
        FTSParser.identifier_return qualifier = null;

        FTSParser.identifier_return name = null;


        Object DOT74_tree=null;
        RewriteRuleTokenStream stream_DOT=new RewriteRuleTokenStream(adaptor,"token DOT");
        RewriteRuleSubtreeStream stream_identifier=new RewriteRuleSubtreeStream(adaptor,"rule identifier");
        try {
            // W:\\workspace-cmis\\ANTLR\\FTS.g:226:2: ( (qualifier= identifier DOT )? name= identifier -> ^( COLUMN_REF $name ( $qualifier)? ) )
            // W:\\workspace-cmis\\ANTLR\\FTS.g:226:4: (qualifier= identifier DOT )? name= identifier
            {
            // W:\\workspace-cmis\\ANTLR\\FTS.g:226:4: (qualifier= identifier DOT )?
            int alt17=2;
            int LA17_0 = input.LA(1);

            if ( (LA17_0==ID) ) {
                int LA17_1 = input.LA(2);

                if ( (LA17_1==DOT) ) {
                    alt17=1;
                }
            }
            switch (alt17) {
                case 1 :
                    // W:\\workspace-cmis\\ANTLR\\FTS.g:226:6: qualifier= identifier DOT
                    {
                    pushFollow(FOLLOW_identifier_in_columnReference1031);
                    qualifier=identifier();

                    state._fsp--;

                    stream_identifier.add(qualifier.getTree());
                    DOT74=(Token)match(input,DOT,FOLLOW_DOT_in_columnReference1033);  
                    stream_DOT.add(DOT74);


                    }
                    break;

            }

            pushFollow(FOLLOW_identifier_in_columnReference1040);
            name=identifier();

            state._fsp--;

            stream_identifier.add(name.getTree());


            // AST REWRITE
            // elements: name, qualifier
            // token labels: 
            // rule labels: retval, name, qualifier
            // token list labels: 
            // rule list labels: 
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);
            RewriteRuleSubtreeStream stream_name=new RewriteRuleSubtreeStream(adaptor,"token name",name!=null?name.tree:null);
            RewriteRuleSubtreeStream stream_qualifier=new RewriteRuleSubtreeStream(adaptor,"token qualifier",qualifier!=null?qualifier.tree:null);

            root_0 = (Object)adaptor.nil();
            // 227:3: -> ^( COLUMN_REF $name ( $qualifier)? )
            {
                // W:\\workspace-cmis\\ANTLR\\FTS.g:227:6: ^( COLUMN_REF $name ( $qualifier)? )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(COLUMN_REF, "COLUMN_REF"), root_1);

                adaptor.addChild(root_1, stream_name.nextTree());
                // W:\\workspace-cmis\\ANTLR\\FTS.g:227:25: ( $qualifier)?
                if ( stream_qualifier.hasNext() ) {
                    adaptor.addChild(root_1, stream_qualifier.nextTree());

                }
                stream_qualifier.reset();

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;retval.tree = root_0;
            }

            retval.stop = input.LT(-1);

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end columnReference

    public static class identifier_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start identifier
    // W:\\workspace-cmis\\ANTLR\\FTS.g:230:1: identifier : ID ;
    public final FTSParser.identifier_return identifier() throws RecognitionException {
        FTSParser.identifier_return retval = new FTSParser.identifier_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token ID75=null;

        Object ID75_tree=null;

        try {
            // W:\\workspace-cmis\\ANTLR\\FTS.g:231:2: ( ID )
            // W:\\workspace-cmis\\ANTLR\\FTS.g:231:4: ID
            {
            root_0 = (Object)adaptor.nil();

            ID75=(Token)match(input,ID,FOLLOW_ID_in_identifier1067); 
            ID75_tree = (Object)adaptor.create(ID75);
            adaptor.addChild(root_0, ID75_tree);


            }

            retval.stop = input.LT(-1);

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end identifier

    // Delegated rules


    protected DFA3 dfa3 = new DFA3(this);
    protected DFA7 dfa7 = new DFA7(this);
    static final String DFA3_eotS =
        "\12\uffff";
    static final String DFA3_eofS =
        "\12\uffff";
    static final String DFA3_minS =
        "\1\31\7\0\2\uffff";
    static final String DFA3_maxS =
        "\1\44\7\0\2\uffff";
    static final String DFA3_acceptS =
        "\10\uffff\1\1\1\2";
    static final String DFA3_specialS =
        "\1\uffff\1\0\1\1\1\2\1\3\1\4\1\5\1\6\2\uffff}>";
    static final String[] DFA3_transitionS = {
            "\1\1\1\7\2\uffff\1\3\1\4\1\5\1\6\3\uffff\1\2",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "",
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
            return "89:1: ftsImplicitConjunctionOrDisjunction : ({...}? ftsExplicitDisjunction ( ftsExplicitDisjunction )* -> ^( CONJUNCTION ( ftsExplicitDisjunction )+ ) | ftsExplicitDisjunction ( ftsExplicitDisjunction )* -> ^( DISJUNCTION ( ftsExplicitDisjunction )+ ) );";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA3_1 = input.LA(1);

                         
                        int index3_1 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (defaultConjunction()) ) {s = 8;}

                        else if ( (true) ) {s = 9;}

                         
                        input.seek(index3_1);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA3_2 = input.LA(1);

                         
                        int index3_2 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (defaultConjunction()) ) {s = 8;}

                        else if ( (true) ) {s = 9;}

                         
                        input.seek(index3_2);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA3_3 = input.LA(1);

                         
                        int index3_3 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (defaultConjunction()) ) {s = 8;}

                        else if ( (true) ) {s = 9;}

                         
                        input.seek(index3_3);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA3_4 = input.LA(1);

                         
                        int index3_4 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (defaultConjunction()) ) {s = 8;}

                        else if ( (true) ) {s = 9;}

                         
                        input.seek(index3_4);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA3_5 = input.LA(1);

                         
                        int index3_5 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (defaultConjunction()) ) {s = 8;}

                        else if ( (true) ) {s = 9;}

                         
                        input.seek(index3_5);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA3_6 = input.LA(1);

                         
                        int index3_6 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (defaultConjunction()) ) {s = 8;}

                        else if ( (true) ) {s = 9;}

                         
                        input.seek(index3_6);
                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA3_7 = input.LA(1);

                         
                        int index3_7 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (defaultConjunction()) ) {s = 8;}

                        else if ( (true) ) {s = 9;}

                         
                        input.seek(index3_7);
                        if ( s>=0 ) return s;
                        break;
            }
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 3, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA7_eotS =
        "\16\uffff";
    static final String DFA7_eofS =
        "\2\uffff\1\11\13\uffff";
    static final String DFA7_minS =
        "\1\32\1\34\1\27\4\uffff\1\44\1\32\3\uffff\1\34\1\uffff";
    static final String DFA7_maxS =
        "\1\44\1\43\1\44\4\uffff\1\44\1\37\3\uffff\1\34\1\uffff";
    static final String DFA7_acceptS =
        "\3\uffff\1\2\1\3\1\4\1\10\2\uffff\1\1\1\5\1\6\1\uffff\1\7";
    static final String DFA7_specialS =
        "\16\uffff}>";
    static final String[] DFA7_transitionS = {
            "\1\6\2\uffff\1\2\1\3\1\4\1\5\3\uffff\1\1",
            "\1\10\6\uffff\1\7",
            "\5\11\1\uffff\4\11\1\12\1\13\1\uffff\1\11",
            "",
            "",
            "",
            "",
            "\1\14",
            "\1\15\2\uffff\1\11\1\uffff\1\4",
            "",
            "",
            "",
            "\1\10",
            ""
    };

    static final short[] DFA7_eot = DFA.unpackEncodedString(DFA7_eotS);
    static final short[] DFA7_eof = DFA.unpackEncodedString(DFA7_eofS);
    static final char[] DFA7_min = DFA.unpackEncodedStringToUnsignedChars(DFA7_minS);
    static final char[] DFA7_max = DFA.unpackEncodedStringToUnsignedChars(DFA7_maxS);
    static final short[] DFA7_accept = DFA.unpackEncodedString(DFA7_acceptS);
    static final short[] DFA7_special = DFA.unpackEncodedString(DFA7_specialS);
    static final short[][] DFA7_transition;

    static {
        int numStates = DFA7_transitionS.length;
        DFA7_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA7_transition[i] = DFA.unpackEncodedString(DFA7_transitionS[i]);
        }
    }

    class DFA7 extends DFA {

        public DFA7(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 7;
            this.eot = DFA7_eot;
            this.eof = DFA7_eof;
            this.min = DFA7_min;
            this.max = DFA7_max;
            this.accept = DFA7_accept;
            this.special = DFA7_special;
            this.transition = DFA7_transition;
        }
        public String getDescription() {
            return "113:1: ftsTest : ( ftsTerm -> ^( TERM ftsTerm ) | ftsExactTerm -> ^( EXACT_TERM ftsExactTerm ) | ftsPhrase -> ^( PHRASE ftsPhrase ) | ftsSynonym -> ^( SYNONYM ftsSynonym ) | ftsFieldGroupProximity -> ^( FG_PROXIMITY ftsFieldGroupProximity ) | ftsFieldGroupRange -> ^( FG_RANGE ftsFieldGroupRange ) | ftsFieldGroup | LPAREN ftsImplicitConjunctionOrDisjunction RPAREN -> ftsImplicitConjunctionOrDisjunction );";
        }
    }
 

    public static final BitSet FOLLOW_ftsImplicitConjunctionOrDisjunction_in_fts135 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ftsExplicitDisjunction_in_ftsImplicitConjunctionOrDisjunction157 = new BitSet(new long[]{0x00000011E6000000L});
    public static final BitSet FOLLOW_ftsExplicitDisjunction_in_ftsImplicitConjunctionOrDisjunction160 = new BitSet(new long[]{0x00000011E6000002L});
    public static final BitSet FOLLOW_ftsExplicitDisjunction_in_ftsImplicitConjunctionOrDisjunction179 = new BitSet(new long[]{0x00000011E6000000L});
    public static final BitSet FOLLOW_ftsExplicitDisjunction_in_ftsImplicitConjunctionOrDisjunction182 = new BitSet(new long[]{0x00000011E6000002L});
    public static final BitSet FOLLOW_ftsExplictConjunction_in_ftsExplicitDisjunction207 = new BitSet(new long[]{0x0000000000800002L});
    public static final BitSet FOLLOW_OR_in_ftsExplicitDisjunction210 = new BitSet(new long[]{0x00000011E6800000L});
    public static final BitSet FOLLOW_ftsExplictConjunction_in_ftsExplicitDisjunction212 = new BitSet(new long[]{0x0000000000800002L});
    public static final BitSet FOLLOW_ftsNot_in_ftsExplictConjunction237 = new BitSet(new long[]{0x0000000001000002L});
    public static final BitSet FOLLOW_AND_in_ftsExplictConjunction240 = new BitSet(new long[]{0x00000011E7000000L});
    public static final BitSet FOLLOW_ftsNot_in_ftsExplictConjunction242 = new BitSet(new long[]{0x0000000001000002L});
    public static final BitSet FOLLOW_MINUS_in_ftsNot268 = new BitSet(new long[]{0x00000011E6000000L});
    public static final BitSet FOLLOW_ftsTest_in_ftsNot270 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ftsTest_in_ftsNot285 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ftsTerm_in_ftsTest301 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ftsExactTerm_in_ftsTest316 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ftsPhrase_in_ftsTest344 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ftsSynonym_in_ftsTest379 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ftsFieldGroupProximity_in_ftsTest408 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ftsFieldGroupRange_in_ftsTest440 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ftsFieldGroup_in_ftsTest469 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_ftsTest478 = new BitSet(new long[]{0x00000011E6000000L});
    public static final BitSet FOLLOW_ftsImplicitConjunctionOrDisjunction_in_ftsTest480 = new BitSet(new long[]{0x0000000008000000L});
    public static final BitSet FOLLOW_RPAREN_in_ftsTest482 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_columnReference_in_ftsTerm500 = new BitSet(new long[]{0x0000000010000000L});
    public static final BitSet FOLLOW_COLON_in_ftsTerm502 = new BitSet(new long[]{0x0000000020000000L});
    public static final BitSet FOLLOW_FTSWORD_in_ftsTerm506 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_PLUS_in_ftsExactTerm527 = new BitSet(new long[]{0x0000001020000000L});
    public static final BitSet FOLLOW_ftsTerm_in_ftsExactTerm529 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_columnReference_in_ftsPhrase550 = new BitSet(new long[]{0x0000000010000000L});
    public static final BitSet FOLLOW_COLON_in_ftsPhrase552 = new BitSet(new long[]{0x0000000080000000L});
    public static final BitSet FOLLOW_FTSPHRASE_in_ftsPhrase556 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TILDA_in_ftsSynonym577 = new BitSet(new long[]{0x0000001020000000L});
    public static final BitSet FOLLOW_ftsTerm_in_ftsSynonym579 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_columnReference_in_ftsFieldGroup598 = new BitSet(new long[]{0x0000000010000000L});
    public static final BitSet FOLLOW_COLON_in_ftsFieldGroup600 = new BitSet(new long[]{0x0000000004000000L});
    public static final BitSet FOLLOW_LPAREN_in_ftsFieldGroup602 = new BitSet(new long[]{0x00000001EE000000L});
    public static final BitSet FOLLOW_ftsFieldGroupImplicitConjunctionOrDisjunction_in_ftsFieldGroup604 = new BitSet(new long[]{0x0000000008000000L});
    public static final BitSet FOLLOW_RPAREN_in_ftsFieldGroup606 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ftsFieldGroupExplicitDisjunction_in_ftsFieldGroupImplicitConjunctionOrDisjunction632 = new BitSet(new long[]{0x00000001E6000000L});
    public static final BitSet FOLLOW_ftsFieldGroupExplicitDisjunction_in_ftsFieldGroupImplicitConjunctionOrDisjunction635 = new BitSet(new long[]{0x00000001E6000002L});
    public static final BitSet FOLLOW_ftsFieldGroupExplicitDisjunction_in_ftsFieldGroupImplicitConjunctionOrDisjunction653 = new BitSet(new long[]{0x00000001E6000000L});
    public static final BitSet FOLLOW_ftsFieldGroupExplicitDisjunction_in_ftsFieldGroupImplicitConjunctionOrDisjunction656 = new BitSet(new long[]{0x00000001E6000002L});
    public static final BitSet FOLLOW_ftsFieldGroupExplictConjunction_in_ftsFieldGroupExplicitDisjunction681 = new BitSet(new long[]{0x0000000000800002L});
    public static final BitSet FOLLOW_OR_in_ftsFieldGroupExplicitDisjunction684 = new BitSet(new long[]{0x00000001E6800000L});
    public static final BitSet FOLLOW_ftsFieldGroupExplictConjunction_in_ftsFieldGroupExplicitDisjunction686 = new BitSet(new long[]{0x0000000000800002L});
    public static final BitSet FOLLOW_ftsFieldGroupNot_in_ftsFieldGroupExplictConjunction711 = new BitSet(new long[]{0x0000000001000002L});
    public static final BitSet FOLLOW_AND_in_ftsFieldGroupExplictConjunction714 = new BitSet(new long[]{0x00000001E7000000L});
    public static final BitSet FOLLOW_ftsFieldGroupNot_in_ftsFieldGroupExplictConjunction716 = new BitSet(new long[]{0x0000000001000002L});
    public static final BitSet FOLLOW_MINUS_in_ftsFieldGroupNot743 = new BitSet(new long[]{0x00000001E6000000L});
    public static final BitSet FOLLOW_ftsFieldGroupTest_in_ftsFieldGroupNot745 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ftsFieldGroupTest_in_ftsFieldGroupNot760 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ftsFieldGroupTerm_in_ftsFieldGroupTest779 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ftsFieldGroupExactTerm_in_ftsFieldGroupTest794 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ftsFieldGroupPhrase_in_ftsFieldGroupTest810 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ftsFieldGroupSynonym_in_ftsFieldGroupTest825 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ftsFieldGroupProximity_in_ftsFieldGroupTest846 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ftsFieldGroupRange_in_ftsFieldGroupTest871 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_ftsFieldGroupTest893 = new BitSet(new long[]{0x00000001EE000000L});
    public static final BitSet FOLLOW_ftsFieldGroupImplicitConjunctionOrDisjunction_in_ftsFieldGroupTest895 = new BitSet(new long[]{0x0000000008000000L});
    public static final BitSet FOLLOW_RPAREN_in_ftsFieldGroupTest897 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FTSWORD_in_ftsFieldGroupTerm915 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_PLUS_in_ftsFieldGroupExactTerm927 = new BitSet(new long[]{0x0000000020000000L});
    public static final BitSet FOLLOW_ftsFieldGroupTerm_in_ftsFieldGroupExactTerm929 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FTSPHRASE_in_ftsFieldGroupPhrase949 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TILDA_in_ftsFieldGroupSynonym961 = new BitSet(new long[]{0x0000000020000000L});
    public static final BitSet FOLLOW_ftsFieldGroupTerm_in_ftsFieldGroupSynonym963 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ftsFieldGroupTerm_in_ftsFieldGroupProximity981 = new BitSet(new long[]{0x0000000200000000L});
    public static final BitSet FOLLOW_STAR_in_ftsFieldGroupProximity983 = new BitSet(new long[]{0x0000000020000000L});
    public static final BitSet FOLLOW_ftsFieldGroupTerm_in_ftsFieldGroupProximity985 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ftsFieldGroupTerm_in_ftsFieldGroupRange1003 = new BitSet(new long[]{0x0000000400000000L});
    public static final BitSet FOLLOW_DOTDOT_in_ftsFieldGroupRange1005 = new BitSet(new long[]{0x0000000020000000L});
    public static final BitSet FOLLOW_ftsFieldGroupTerm_in_ftsFieldGroupRange1007 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_identifier_in_columnReference1031 = new BitSet(new long[]{0x0000000800000000L});
    public static final BitSet FOLLOW_DOT_in_columnReference1033 = new BitSet(new long[]{0x0000001000000000L});
    public static final BitSet FOLLOW_identifier_in_columnReference1040 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_identifier1067 = new BitSet(new long[]{0x0000000000000002L});

}