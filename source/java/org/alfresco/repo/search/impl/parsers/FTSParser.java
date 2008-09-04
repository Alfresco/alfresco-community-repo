// $ANTLR 3.1b1 W:\\workspace-cmis\\ANTLR\\FTS.g 2008-07-15 16:32:01
package org.alfresco.repo.search.impl.parsers;

import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;


import org.antlr.runtime.tree.*;

public class FTSParser extends Parser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "FTS", "DISJUNCTION", "CONJUNCTION", "NEGATION", "TERM", "EXACT_TERM", "PHRASE", "SYNONYM", "FIELD_DISJUNCTION", "FIELD_CONJUNCTION", "FIELD_NEGATION", "FIELD_GROUP", "FG_TERM", "FG_EXACT_TERM", "FG_PHRASE", "FG_SYNONYM", "FG_PROXIMITY", "FG_RANGE", "COLUMN_REF", "OR", "AND", "MINUS", "LPAREN", "RPAREN", "COLON", "PLUS", "FTSPHRASE", "TILDA", "STAR", "DOTDOT", "DOT", "ID", "FTSWORD", "NOT", "INWORD", "WS"
    };
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
    public static final int FTSWORD=36;
    public static final int LPAREN=26;
    public static final int PHRASE=10;
    public static final int COLON=28;
    public static final int DISJUNCTION=5;
    public static final int RPAREN=27;
    public static final int TILDA=31;
    public static final int FTS=4;
    public static final int WS=39;
    public static final int FG_SYNONYM=19;
    public static final int NEGATION=7;
    public static final int FTSPHRASE=30;
    public static final int FIELD_CONJUNCTION=13;
    public static final int OR=23;
    public static final int PLUS=29;
    public static final int DOT=34;
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


        private Stack<String> paraphrases = new Stack<String>();
       
        public boolean defaultConjunction()
        {
           return true;
        }
        
        public boolean defaultFieldConjunction()
        {
           return true;
        }
        
        protected void mismatch(IntStream input, int ttype, BitSet follow) throws RecognitionException
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
           String msg = null;
           if(e instanceof NoViableAltException)
           {
                NoViableAltException nvae = (NoViableAltException)e;
                msg = " no viable alt; token="+e.token+
                 " (decision="+nvae.decisionNumber+
                 " state "+nvae.stateNumber+")"+
                 " decision=<<"+nvae.grammarDecisionDescription+">>";
           }
           else
           {
               msg = super.getErrorMessage(e, tokenNames);
           }
           if(paraphrases.size() > 0)
           {
               String paraphrase = (String)paraphrases.peek();
               msg = msg+" "+paraphrase;
           }
           
           return stack+" "+msg;
        }
        
        public String getTokenErrorDisplay(Token t)
        {
           return t.toString();
        }      


    public static class fts_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start fts
    // W:\\workspace-cmis\\ANTLR\\FTS.g:132:1: fts : ftsImplicitConjunctionOrDisjunction EOF -> ftsImplicitConjunctionOrDisjunction ;
    public final FTSParser.fts_return fts() throws RecognitionException {
        FTSParser.fts_return retval = new FTSParser.fts_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token EOF2=null;
        FTSParser.ftsImplicitConjunctionOrDisjunction_return ftsImplicitConjunctionOrDisjunction1 = null;


        Object EOF2_tree=null;
        RewriteRuleTokenStream stream_EOF=new RewriteRuleTokenStream(adaptor,"token EOF");
        RewriteRuleSubtreeStream stream_ftsImplicitConjunctionOrDisjunction=new RewriteRuleSubtreeStream(adaptor,"rule ftsImplicitConjunctionOrDisjunction");
        try {
            // W:\\workspace-cmis\\ANTLR\\FTS.g:133:5: ( ftsImplicitConjunctionOrDisjunction EOF -> ftsImplicitConjunctionOrDisjunction )
            // W:\\workspace-cmis\\ANTLR\\FTS.g:133:8: ftsImplicitConjunctionOrDisjunction EOF
            {
            pushFollow(FOLLOW_ftsImplicitConjunctionOrDisjunction_in_fts146);
            ftsImplicitConjunctionOrDisjunction1=ftsImplicitConjunctionOrDisjunction();

            state._fsp--;

            stream_ftsImplicitConjunctionOrDisjunction.add(ftsImplicitConjunctionOrDisjunction1.getTree());
            EOF2=(Token)match(input,EOF,FOLLOW_EOF_in_fts148);  
            stream_EOF.add(EOF2);



            // AST REWRITE
            // elements: ftsImplicitConjunctionOrDisjunction
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 134:3: -> ftsImplicitConjunctionOrDisjunction
            {
                adaptor.addChild(root_0, stream_ftsImplicitConjunctionOrDisjunction.nextTree());

            }

            retval.tree = root_0;retval.tree = root_0;
            }

            retval.stop = input.LT(-1);

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }

        catch(RecognitionException e)
        {
           throw e;
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
    // W:\\workspace-cmis\\ANTLR\\FTS.g:137:1: ftsImplicitConjunctionOrDisjunction : ({...}? ftsExplicitDisjunction ( ftsExplicitDisjunction )* -> ^( CONJUNCTION ( ftsExplicitDisjunction )+ ) | ftsExplicitDisjunction ( ftsExplicitDisjunction )* -> ^( DISJUNCTION ( ftsExplicitDisjunction )+ ) );
    public final FTSParser.ftsImplicitConjunctionOrDisjunction_return ftsImplicitConjunctionOrDisjunction() throws RecognitionException {
        FTSParser.ftsImplicitConjunctionOrDisjunction_return retval = new FTSParser.ftsImplicitConjunctionOrDisjunction_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        FTSParser.ftsExplicitDisjunction_return ftsExplicitDisjunction3 = null;

        FTSParser.ftsExplicitDisjunction_return ftsExplicitDisjunction4 = null;

        FTSParser.ftsExplicitDisjunction_return ftsExplicitDisjunction5 = null;

        FTSParser.ftsExplicitDisjunction_return ftsExplicitDisjunction6 = null;


        RewriteRuleSubtreeStream stream_ftsExplicitDisjunction=new RewriteRuleSubtreeStream(adaptor,"rule ftsExplicitDisjunction");
        try {
            // W:\\workspace-cmis\\ANTLR\\FTS.g:138:2: ({...}? ftsExplicitDisjunction ( ftsExplicitDisjunction )* -> ^( CONJUNCTION ( ftsExplicitDisjunction )+ ) | ftsExplicitDisjunction ( ftsExplicitDisjunction )* -> ^( DISJUNCTION ( ftsExplicitDisjunction )+ ) )
            int alt3=2;
            alt3 = dfa3.predict(input);
            switch (alt3) {
                case 1 :
                    // W:\\workspace-cmis\\ANTLR\\FTS.g:138:4: {...}? ftsExplicitDisjunction ( ftsExplicitDisjunction )*
                    {
                    if ( !(defaultConjunction()) ) {
                        throw new FailedPredicateException(input, "ftsImplicitConjunctionOrDisjunction", "defaultConjunction()");
                    }
                    pushFollow(FOLLOW_ftsExplicitDisjunction_in_ftsImplicitConjunctionOrDisjunction170);
                    ftsExplicitDisjunction3=ftsExplicitDisjunction();

                    state._fsp--;

                    stream_ftsExplicitDisjunction.add(ftsExplicitDisjunction3.getTree());
                    // W:\\workspace-cmis\\ANTLR\\FTS.g:138:51: ( ftsExplicitDisjunction )*
                    loop1:
                    do {
                        int alt1=2;
                        int LA1_0 = input.LA(1);

                        if ( ((LA1_0>=MINUS && LA1_0<=LPAREN)||(LA1_0>=PLUS && LA1_0<=TILDA)||(LA1_0>=ID && LA1_0<=FTSWORD)) ) {
                            alt1=1;
                        }


                        switch (alt1) {
                    	case 1 :
                    	    // W:\\workspace-cmis\\ANTLR\\FTS.g:138:52: ftsExplicitDisjunction
                    	    {
                    	    pushFollow(FOLLOW_ftsExplicitDisjunction_in_ftsImplicitConjunctionOrDisjunction173);
                    	    ftsExplicitDisjunction4=ftsExplicitDisjunction();

                    	    state._fsp--;

                    	    stream_ftsExplicitDisjunction.add(ftsExplicitDisjunction4.getTree());

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
                    // 139:3: -> ^( CONJUNCTION ( ftsExplicitDisjunction )+ )
                    {
                        // W:\\workspace-cmis\\ANTLR\\FTS.g:139:6: ^( CONJUNCTION ( ftsExplicitDisjunction )+ )
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
                    // W:\\workspace-cmis\\ANTLR\\FTS.g:140:5: ftsExplicitDisjunction ( ftsExplicitDisjunction )*
                    {
                    pushFollow(FOLLOW_ftsExplicitDisjunction_in_ftsImplicitConjunctionOrDisjunction192);
                    ftsExplicitDisjunction5=ftsExplicitDisjunction();

                    state._fsp--;

                    stream_ftsExplicitDisjunction.add(ftsExplicitDisjunction5.getTree());
                    // W:\\workspace-cmis\\ANTLR\\FTS.g:140:28: ( ftsExplicitDisjunction )*
                    loop2:
                    do {
                        int alt2=2;
                        int LA2_0 = input.LA(1);

                        if ( ((LA2_0>=MINUS && LA2_0<=LPAREN)||(LA2_0>=PLUS && LA2_0<=TILDA)||(LA2_0>=ID && LA2_0<=FTSWORD)) ) {
                            alt2=1;
                        }


                        switch (alt2) {
                    	case 1 :
                    	    // W:\\workspace-cmis\\ANTLR\\FTS.g:140:29: ftsExplicitDisjunction
                    	    {
                    	    pushFollow(FOLLOW_ftsExplicitDisjunction_in_ftsImplicitConjunctionOrDisjunction195);
                    	    ftsExplicitDisjunction6=ftsExplicitDisjunction();

                    	    state._fsp--;

                    	    stream_ftsExplicitDisjunction.add(ftsExplicitDisjunction6.getTree());

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
                    // 141:3: -> ^( DISJUNCTION ( ftsExplicitDisjunction )+ )
                    {
                        // W:\\workspace-cmis\\ANTLR\\FTS.g:141:6: ^( DISJUNCTION ( ftsExplicitDisjunction )+ )
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

        catch(RecognitionException e)
        {
           throw e;
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
    // W:\\workspace-cmis\\ANTLR\\FTS.g:144:1: ftsExplicitDisjunction : ftsExplictConjunction ( OR ftsExplictConjunction )* -> ^( DISJUNCTION ( ftsExplictConjunction )+ ) ;
    public final FTSParser.ftsExplicitDisjunction_return ftsExplicitDisjunction() throws RecognitionException {
        FTSParser.ftsExplicitDisjunction_return retval = new FTSParser.ftsExplicitDisjunction_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token OR8=null;
        FTSParser.ftsExplictConjunction_return ftsExplictConjunction7 = null;

        FTSParser.ftsExplictConjunction_return ftsExplictConjunction9 = null;


        Object OR8_tree=null;
        RewriteRuleTokenStream stream_OR=new RewriteRuleTokenStream(adaptor,"token OR");
        RewriteRuleSubtreeStream stream_ftsExplictConjunction=new RewriteRuleSubtreeStream(adaptor,"rule ftsExplictConjunction");
        try {
            // W:\\workspace-cmis\\ANTLR\\FTS.g:145:2: ( ftsExplictConjunction ( OR ftsExplictConjunction )* -> ^( DISJUNCTION ( ftsExplictConjunction )+ ) )
            // W:\\workspace-cmis\\ANTLR\\FTS.g:145:4: ftsExplictConjunction ( OR ftsExplictConjunction )*
            {
            pushFollow(FOLLOW_ftsExplictConjunction_in_ftsExplicitDisjunction220);
            ftsExplictConjunction7=ftsExplictConjunction();

            state._fsp--;

            stream_ftsExplictConjunction.add(ftsExplictConjunction7.getTree());
            // W:\\workspace-cmis\\ANTLR\\FTS.g:145:26: ( OR ftsExplictConjunction )*
            loop4:
            do {
                int alt4=2;
                int LA4_0 = input.LA(1);

                if ( (LA4_0==OR) ) {
                    alt4=1;
                }


                switch (alt4) {
            	case 1 :
            	    // W:\\workspace-cmis\\ANTLR\\FTS.g:145:27: OR ftsExplictConjunction
            	    {
            	    OR8=(Token)match(input,OR,FOLLOW_OR_in_ftsExplicitDisjunction223);  
            	    stream_OR.add(OR8);

            	    pushFollow(FOLLOW_ftsExplictConjunction_in_ftsExplicitDisjunction225);
            	    ftsExplictConjunction9=ftsExplictConjunction();

            	    state._fsp--;

            	    stream_ftsExplictConjunction.add(ftsExplictConjunction9.getTree());

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
            // 146:3: -> ^( DISJUNCTION ( ftsExplictConjunction )+ )
            {
                // W:\\workspace-cmis\\ANTLR\\FTS.g:146:6: ^( DISJUNCTION ( ftsExplictConjunction )+ )
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

        catch(RecognitionException e)
        {
           throw e;
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
    // W:\\workspace-cmis\\ANTLR\\FTS.g:149:1: ftsExplictConjunction : ftsNot ( AND ftsNot )* -> ^( CONJUNCTION ftsNot ) ;
    public final FTSParser.ftsExplictConjunction_return ftsExplictConjunction() throws RecognitionException {
        FTSParser.ftsExplictConjunction_return retval = new FTSParser.ftsExplictConjunction_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token AND11=null;
        FTSParser.ftsNot_return ftsNot10 = null;

        FTSParser.ftsNot_return ftsNot12 = null;


        Object AND11_tree=null;
        RewriteRuleTokenStream stream_AND=new RewriteRuleTokenStream(adaptor,"token AND");
        RewriteRuleSubtreeStream stream_ftsNot=new RewriteRuleSubtreeStream(adaptor,"rule ftsNot");
        try {
            // W:\\workspace-cmis\\ANTLR\\FTS.g:150:2: ( ftsNot ( AND ftsNot )* -> ^( CONJUNCTION ftsNot ) )
            // W:\\workspace-cmis\\ANTLR\\FTS.g:150:4: ftsNot ( AND ftsNot )*
            {
            pushFollow(FOLLOW_ftsNot_in_ftsExplictConjunction250);
            ftsNot10=ftsNot();

            state._fsp--;

            stream_ftsNot.add(ftsNot10.getTree());
            // W:\\workspace-cmis\\ANTLR\\FTS.g:150:11: ( AND ftsNot )*
            loop5:
            do {
                int alt5=2;
                int LA5_0 = input.LA(1);

                if ( (LA5_0==AND) ) {
                    alt5=1;
                }


                switch (alt5) {
            	case 1 :
            	    // W:\\workspace-cmis\\ANTLR\\FTS.g:150:12: AND ftsNot
            	    {
            	    AND11=(Token)match(input,AND,FOLLOW_AND_in_ftsExplictConjunction253);  
            	    stream_AND.add(AND11);

            	    pushFollow(FOLLOW_ftsNot_in_ftsExplictConjunction255);
            	    ftsNot12=ftsNot();

            	    state._fsp--;

            	    stream_ftsNot.add(ftsNot12.getTree());

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
            // 151:3: -> ^( CONJUNCTION ftsNot )
            {
                // W:\\workspace-cmis\\ANTLR\\FTS.g:151:6: ^( CONJUNCTION ftsNot )
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

        catch(RecognitionException e)
        {
           throw e;
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
    // W:\\workspace-cmis\\ANTLR\\FTS.g:155:1: ftsNot : ( MINUS ftsTest -> ^( NEGATION ftsTest ) | ftsTest -> ftsTest );
    public final FTSParser.ftsNot_return ftsNot() throws RecognitionException {
        FTSParser.ftsNot_return retval = new FTSParser.ftsNot_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token MINUS13=null;
        FTSParser.ftsTest_return ftsTest14 = null;

        FTSParser.ftsTest_return ftsTest15 = null;


        Object MINUS13_tree=null;
        RewriteRuleTokenStream stream_MINUS=new RewriteRuleTokenStream(adaptor,"token MINUS");
        RewriteRuleSubtreeStream stream_ftsTest=new RewriteRuleSubtreeStream(adaptor,"rule ftsTest");
        try {
            // W:\\workspace-cmis\\ANTLR\\FTS.g:156:5: ( MINUS ftsTest -> ^( NEGATION ftsTest ) | ftsTest -> ftsTest )
            int alt6=2;
            int LA6_0 = input.LA(1);

            if ( (LA6_0==MINUS) ) {
                alt6=1;
            }
            else if ( (LA6_0==LPAREN||(LA6_0>=PLUS && LA6_0<=TILDA)||(LA6_0>=ID && LA6_0<=FTSWORD)) ) {
                alt6=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 6, 0, input);

                throw nvae;
            }
            switch (alt6) {
                case 1 :
                    // W:\\workspace-cmis\\ANTLR\\FTS.g:156:7: MINUS ftsTest
                    {
                    MINUS13=(Token)match(input,MINUS,FOLLOW_MINUS_in_ftsNot286);  
                    stream_MINUS.add(MINUS13);

                    pushFollow(FOLLOW_ftsTest_in_ftsNot288);
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
                    // 157:3: -> ^( NEGATION ftsTest )
                    {
                        // W:\\workspace-cmis\\ANTLR\\FTS.g:157:6: ^( NEGATION ftsTest )
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
                    // W:\\workspace-cmis\\ANTLR\\FTS.g:158:4: ftsTest
                    {
                    pushFollow(FOLLOW_ftsTest_in_ftsNot303);
                    ftsTest15=ftsTest();

                    state._fsp--;

                    stream_ftsTest.add(ftsTest15.getTree());


                    // AST REWRITE
                    // elements: ftsTest
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 159:3: -> ftsTest
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

        catch(RecognitionException e)
        {
           throw e;
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
    // W:\\workspace-cmis\\ANTLR\\FTS.g:162:1: ftsTest : ( ftsTerm -> ^( TERM ftsTerm ) | ftsExactTerm -> ^( EXACT_TERM ftsExactTerm ) | ftsPhrase -> ^( PHRASE ftsPhrase ) | ftsSynonym -> ^( SYNONYM ftsSynonym ) | ftsFieldGroupProximity -> ^( FG_PROXIMITY ftsFieldGroupProximity ) | ftsFieldGroupRange -> ^( FG_RANGE ftsFieldGroupRange ) | ftsFieldGroup | LPAREN ftsImplicitConjunctionOrDisjunction RPAREN -> ftsImplicitConjunctionOrDisjunction );
    public final FTSParser.ftsTest_return ftsTest() throws RecognitionException {
        FTSParser.ftsTest_return retval = new FTSParser.ftsTest_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token LPAREN23=null;
        Token RPAREN25=null;
        FTSParser.ftsTerm_return ftsTerm16 = null;

        FTSParser.ftsExactTerm_return ftsExactTerm17 = null;

        FTSParser.ftsPhrase_return ftsPhrase18 = null;

        FTSParser.ftsSynonym_return ftsSynonym19 = null;

        FTSParser.ftsFieldGroupProximity_return ftsFieldGroupProximity20 = null;

        FTSParser.ftsFieldGroupRange_return ftsFieldGroupRange21 = null;

        FTSParser.ftsFieldGroup_return ftsFieldGroup22 = null;

        FTSParser.ftsImplicitConjunctionOrDisjunction_return ftsImplicitConjunctionOrDisjunction24 = null;


        Object LPAREN23_tree=null;
        Object RPAREN25_tree=null;
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
            // W:\\workspace-cmis\\ANTLR\\FTS.g:163:5: ( ftsTerm -> ^( TERM ftsTerm ) | ftsExactTerm -> ^( EXACT_TERM ftsExactTerm ) | ftsPhrase -> ^( PHRASE ftsPhrase ) | ftsSynonym -> ^( SYNONYM ftsSynonym ) | ftsFieldGroupProximity -> ^( FG_PROXIMITY ftsFieldGroupProximity ) | ftsFieldGroupRange -> ^( FG_RANGE ftsFieldGroupRange ) | ftsFieldGroup | LPAREN ftsImplicitConjunctionOrDisjunction RPAREN -> ftsImplicitConjunctionOrDisjunction )
            int alt7=8;
            alt7 = dfa7.predict(input);
            switch (alt7) {
                case 1 :
                    // W:\\workspace-cmis\\ANTLR\\FTS.g:163:7: ftsTerm
                    {
                    pushFollow(FOLLOW_ftsTerm_in_ftsTest324);
                    ftsTerm16=ftsTerm();

                    state._fsp--;

                    stream_ftsTerm.add(ftsTerm16.getTree());


                    // AST REWRITE
                    // elements: ftsTerm
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 164:3: -> ^( TERM ftsTerm )
                    {
                        // W:\\workspace-cmis\\ANTLR\\FTS.g:164:6: ^( TERM ftsTerm )
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
                    // W:\\workspace-cmis\\ANTLR\\FTS.g:165:4: ftsExactTerm
                    {
                    pushFollow(FOLLOW_ftsExactTerm_in_ftsTest339);
                    ftsExactTerm17=ftsExactTerm();

                    state._fsp--;

                    stream_ftsExactTerm.add(ftsExactTerm17.getTree());


                    // AST REWRITE
                    // elements: ftsExactTerm
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 166:3: -> ^( EXACT_TERM ftsExactTerm )
                    {
                        // W:\\workspace-cmis\\ANTLR\\FTS.g:166:6: ^( EXACT_TERM ftsExactTerm )
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
                    // W:\\workspace-cmis\\ANTLR\\FTS.g:167:9: ftsPhrase
                    {
                    pushFollow(FOLLOW_ftsPhrase_in_ftsTest359);
                    ftsPhrase18=ftsPhrase();

                    state._fsp--;

                    stream_ftsPhrase.add(ftsPhrase18.getTree());


                    // AST REWRITE
                    // elements: ftsPhrase
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 168:9: -> ^( PHRASE ftsPhrase )
                    {
                        // W:\\workspace-cmis\\ANTLR\\FTS.g:168:12: ^( PHRASE ftsPhrase )
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
                    // W:\\workspace-cmis\\ANTLR\\FTS.g:169:9: ftsSynonym
                    {
                    pushFollow(FOLLOW_ftsSynonym_in_ftsTest385);
                    ftsSynonym19=ftsSynonym();

                    state._fsp--;

                    stream_ftsSynonym.add(ftsSynonym19.getTree());


                    // AST REWRITE
                    // elements: ftsSynonym
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 170:9: -> ^( SYNONYM ftsSynonym )
                    {
                        // W:\\workspace-cmis\\ANTLR\\FTS.g:170:12: ^( SYNONYM ftsSynonym )
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
                    // W:\\workspace-cmis\\ANTLR\\FTS.g:171:7: ftsFieldGroupProximity
                    {
                    pushFollow(FOLLOW_ftsFieldGroupProximity_in_ftsTest409);
                    ftsFieldGroupProximity20=ftsFieldGroupProximity();

                    state._fsp--;

                    stream_ftsFieldGroupProximity.add(ftsFieldGroupProximity20.getTree());


                    // AST REWRITE
                    // elements: ftsFieldGroupProximity
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 172:9: -> ^( FG_PROXIMITY ftsFieldGroupProximity )
                    {
                        // W:\\workspace-cmis\\ANTLR\\FTS.g:172:12: ^( FG_PROXIMITY ftsFieldGroupProximity )
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
                    // W:\\workspace-cmis\\ANTLR\\FTS.g:173:8: ftsFieldGroupRange
                    {
                    pushFollow(FOLLOW_ftsFieldGroupRange_in_ftsTest436);
                    ftsFieldGroupRange21=ftsFieldGroupRange();

                    state._fsp--;

                    stream_ftsFieldGroupRange.add(ftsFieldGroupRange21.getTree());


                    // AST REWRITE
                    // elements: ftsFieldGroupRange
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 174:9: -> ^( FG_RANGE ftsFieldGroupRange )
                    {
                        // W:\\workspace-cmis\\ANTLR\\FTS.g:174:12: ^( FG_RANGE ftsFieldGroupRange )
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
                    // W:\\workspace-cmis\\ANTLR\\FTS.g:175:7: ftsFieldGroup
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_ftsFieldGroup_in_ftsTest460);
                    ftsFieldGroup22=ftsFieldGroup();

                    state._fsp--;

                    adaptor.addChild(root_0, ftsFieldGroup22.getTree());

                    }
                    break;
                case 8 :
                    // W:\\workspace-cmis\\ANTLR\\FTS.g:176:4: LPAREN ftsImplicitConjunctionOrDisjunction RPAREN
                    {
                    LPAREN23=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_ftsTest469);  
                    stream_LPAREN.add(LPAREN23);

                    pushFollow(FOLLOW_ftsImplicitConjunctionOrDisjunction_in_ftsTest471);
                    ftsImplicitConjunctionOrDisjunction24=ftsImplicitConjunctionOrDisjunction();

                    state._fsp--;

                    stream_ftsImplicitConjunctionOrDisjunction.add(ftsImplicitConjunctionOrDisjunction24.getTree());
                    RPAREN25=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_ftsTest473);  
                    stream_RPAREN.add(RPAREN25);



                    // AST REWRITE
                    // elements: ftsImplicitConjunctionOrDisjunction
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 177:3: -> ftsImplicitConjunctionOrDisjunction
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

        catch(RecognitionException e)
        {
           throw e;
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
    // W:\\workspace-cmis\\ANTLR\\FTS.g:180:1: ftsTerm : ( columnReference COLON )? ftsWord -> ftsWord ( columnReference )? ;
    public final FTSParser.ftsTerm_return ftsTerm() throws RecognitionException {
        FTSParser.ftsTerm_return retval = new FTSParser.ftsTerm_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token COLON27=null;
        FTSParser.columnReference_return columnReference26 = null;

        FTSParser.ftsWord_return ftsWord28 = null;


        Object COLON27_tree=null;
        RewriteRuleTokenStream stream_COLON=new RewriteRuleTokenStream(adaptor,"token COLON");
        RewriteRuleSubtreeStream stream_columnReference=new RewriteRuleSubtreeStream(adaptor,"rule columnReference");
        RewriteRuleSubtreeStream stream_ftsWord=new RewriteRuleSubtreeStream(adaptor,"rule ftsWord");
        try {
            // W:\\workspace-cmis\\ANTLR\\FTS.g:181:2: ( ( columnReference COLON )? ftsWord -> ftsWord ( columnReference )? )
            // W:\\workspace-cmis\\ANTLR\\FTS.g:181:4: ( columnReference COLON )? ftsWord
            {
            // W:\\workspace-cmis\\ANTLR\\FTS.g:181:4: ( columnReference COLON )?
            int alt8=2;
            int LA8_0 = input.LA(1);

            if ( (LA8_0==ID) ) {
                int LA8_1 = input.LA(2);

                if ( (LA8_1==COLON||LA8_1==DOT) ) {
                    alt8=1;
                }
            }
            switch (alt8) {
                case 1 :
                    // W:\\workspace-cmis\\ANTLR\\FTS.g:181:5: columnReference COLON
                    {
                    pushFollow(FOLLOW_columnReference_in_ftsTerm491);
                    columnReference26=columnReference();

                    state._fsp--;

                    stream_columnReference.add(columnReference26.getTree());
                    COLON27=(Token)match(input,COLON,FOLLOW_COLON_in_ftsTerm493);  
                    stream_COLON.add(COLON27);


                    }
                    break;

            }

            pushFollow(FOLLOW_ftsWord_in_ftsTerm497);
            ftsWord28=ftsWord();

            state._fsp--;

            stream_ftsWord.add(ftsWord28.getTree());


            // AST REWRITE
            // elements: columnReference, ftsWord
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 182:3: -> ftsWord ( columnReference )?
            {
                adaptor.addChild(root_0, stream_ftsWord.nextTree());
                // W:\\workspace-cmis\\ANTLR\\FTS.g:182:14: ( columnReference )?
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

        catch(RecognitionException e)
        {
           throw e;
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
    // W:\\workspace-cmis\\ANTLR\\FTS.g:185:1: ftsExactTerm : PLUS ftsTerm -> ftsTerm ;
    public final FTSParser.ftsExactTerm_return ftsExactTerm() throws RecognitionException {
        FTSParser.ftsExactTerm_return retval = new FTSParser.ftsExactTerm_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token PLUS29=null;
        FTSParser.ftsTerm_return ftsTerm30 = null;


        Object PLUS29_tree=null;
        RewriteRuleTokenStream stream_PLUS=new RewriteRuleTokenStream(adaptor,"token PLUS");
        RewriteRuleSubtreeStream stream_ftsTerm=new RewriteRuleSubtreeStream(adaptor,"rule ftsTerm");
        try {
            // W:\\workspace-cmis\\ANTLR\\FTS.g:186:2: ( PLUS ftsTerm -> ftsTerm )
            // W:\\workspace-cmis\\ANTLR\\FTS.g:186:4: PLUS ftsTerm
            {
            PLUS29=(Token)match(input,PLUS,FOLLOW_PLUS_in_ftsExactTerm518);  
            stream_PLUS.add(PLUS29);

            pushFollow(FOLLOW_ftsTerm_in_ftsExactTerm520);
            ftsTerm30=ftsTerm();

            state._fsp--;

            stream_ftsTerm.add(ftsTerm30.getTree());


            // AST REWRITE
            // elements: ftsTerm
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 187:3: -> ftsTerm
            {
                adaptor.addChild(root_0, stream_ftsTerm.nextTree());

            }

            retval.tree = root_0;retval.tree = root_0;
            }

            retval.stop = input.LT(-1);

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }

        catch(RecognitionException e)
        {
           throw e;
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
    // W:\\workspace-cmis\\ANTLR\\FTS.g:190:1: ftsPhrase : ( columnReference COLON )? FTSPHRASE -> FTSPHRASE ( columnReference )? ;
    public final FTSParser.ftsPhrase_return ftsPhrase() throws RecognitionException {
        FTSParser.ftsPhrase_return retval = new FTSParser.ftsPhrase_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token COLON32=null;
        Token FTSPHRASE33=null;
        FTSParser.columnReference_return columnReference31 = null;


        Object COLON32_tree=null;
        Object FTSPHRASE33_tree=null;
        RewriteRuleTokenStream stream_COLON=new RewriteRuleTokenStream(adaptor,"token COLON");
        RewriteRuleTokenStream stream_FTSPHRASE=new RewriteRuleTokenStream(adaptor,"token FTSPHRASE");
        RewriteRuleSubtreeStream stream_columnReference=new RewriteRuleSubtreeStream(adaptor,"rule columnReference");
        try {
            // W:\\workspace-cmis\\ANTLR\\FTS.g:191:2: ( ( columnReference COLON )? FTSPHRASE -> FTSPHRASE ( columnReference )? )
            // W:\\workspace-cmis\\ANTLR\\FTS.g:191:6: ( columnReference COLON )? FTSPHRASE
            {
            // W:\\workspace-cmis\\ANTLR\\FTS.g:191:6: ( columnReference COLON )?
            int alt9=2;
            int LA9_0 = input.LA(1);

            if ( (LA9_0==ID) ) {
                alt9=1;
            }
            switch (alt9) {
                case 1 :
                    // W:\\workspace-cmis\\ANTLR\\FTS.g:191:7: columnReference COLON
                    {
                    pushFollow(FOLLOW_columnReference_in_ftsPhrase541);
                    columnReference31=columnReference();

                    state._fsp--;

                    stream_columnReference.add(columnReference31.getTree());
                    COLON32=(Token)match(input,COLON,FOLLOW_COLON_in_ftsPhrase543);  
                    stream_COLON.add(COLON32);


                    }
                    break;

            }

            FTSPHRASE33=(Token)match(input,FTSPHRASE,FOLLOW_FTSPHRASE_in_ftsPhrase547);  
            stream_FTSPHRASE.add(FTSPHRASE33);



            // AST REWRITE
            // elements: columnReference, FTSPHRASE
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 192:3: -> FTSPHRASE ( columnReference )?
            {
                adaptor.addChild(root_0, stream_FTSPHRASE.nextNode());
                // W:\\workspace-cmis\\ANTLR\\FTS.g:192:16: ( columnReference )?
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

        catch(RecognitionException e)
        {
           throw e;
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
    // W:\\workspace-cmis\\ANTLR\\FTS.g:195:1: ftsSynonym : TILDA ftsTerm -> ftsTerm ;
    public final FTSParser.ftsSynonym_return ftsSynonym() throws RecognitionException {
        FTSParser.ftsSynonym_return retval = new FTSParser.ftsSynonym_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token TILDA34=null;
        FTSParser.ftsTerm_return ftsTerm35 = null;


        Object TILDA34_tree=null;
        RewriteRuleTokenStream stream_TILDA=new RewriteRuleTokenStream(adaptor,"token TILDA");
        RewriteRuleSubtreeStream stream_ftsTerm=new RewriteRuleSubtreeStream(adaptor,"rule ftsTerm");
        try {
            // W:\\workspace-cmis\\ANTLR\\FTS.g:196:2: ( TILDA ftsTerm -> ftsTerm )
            // W:\\workspace-cmis\\ANTLR\\FTS.g:196:4: TILDA ftsTerm
            {
            TILDA34=(Token)match(input,TILDA,FOLLOW_TILDA_in_ftsSynonym568);  
            stream_TILDA.add(TILDA34);

            pushFollow(FOLLOW_ftsTerm_in_ftsSynonym570);
            ftsTerm35=ftsTerm();

            state._fsp--;

            stream_ftsTerm.add(ftsTerm35.getTree());


            // AST REWRITE
            // elements: ftsTerm
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 197:3: -> ftsTerm
            {
                adaptor.addChild(root_0, stream_ftsTerm.nextTree());

            }

            retval.tree = root_0;retval.tree = root_0;
            }

            retval.stop = input.LT(-1);

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }

        catch(RecognitionException e)
        {
           throw e;
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
    // W:\\workspace-cmis\\ANTLR\\FTS.g:201:1: ftsFieldGroup : columnReference COLON LPAREN ftsFieldGroupImplicitConjunctionOrDisjunction RPAREN -> ^( FIELD_GROUP columnReference ftsFieldGroupImplicitConjunctionOrDisjunction ) ;
    public final FTSParser.ftsFieldGroup_return ftsFieldGroup() throws RecognitionException {
        FTSParser.ftsFieldGroup_return retval = new FTSParser.ftsFieldGroup_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token COLON37=null;
        Token LPAREN38=null;
        Token RPAREN40=null;
        FTSParser.columnReference_return columnReference36 = null;

        FTSParser.ftsFieldGroupImplicitConjunctionOrDisjunction_return ftsFieldGroupImplicitConjunctionOrDisjunction39 = null;


        Object COLON37_tree=null;
        Object LPAREN38_tree=null;
        Object RPAREN40_tree=null;
        RewriteRuleTokenStream stream_COLON=new RewriteRuleTokenStream(adaptor,"token COLON");
        RewriteRuleTokenStream stream_RPAREN=new RewriteRuleTokenStream(adaptor,"token RPAREN");
        RewriteRuleTokenStream stream_LPAREN=new RewriteRuleTokenStream(adaptor,"token LPAREN");
        RewriteRuleSubtreeStream stream_columnReference=new RewriteRuleSubtreeStream(adaptor,"rule columnReference");
        RewriteRuleSubtreeStream stream_ftsFieldGroupImplicitConjunctionOrDisjunction=new RewriteRuleSubtreeStream(adaptor,"rule ftsFieldGroupImplicitConjunctionOrDisjunction");
        try {
            // W:\\workspace-cmis\\ANTLR\\FTS.g:202:2: ( columnReference COLON LPAREN ftsFieldGroupImplicitConjunctionOrDisjunction RPAREN -> ^( FIELD_GROUP columnReference ftsFieldGroupImplicitConjunctionOrDisjunction ) )
            // W:\\workspace-cmis\\ANTLR\\FTS.g:202:4: columnReference COLON LPAREN ftsFieldGroupImplicitConjunctionOrDisjunction RPAREN
            {
            pushFollow(FOLLOW_columnReference_in_ftsFieldGroup589);
            columnReference36=columnReference();

            state._fsp--;

            stream_columnReference.add(columnReference36.getTree());
            COLON37=(Token)match(input,COLON,FOLLOW_COLON_in_ftsFieldGroup591);  
            stream_COLON.add(COLON37);

            LPAREN38=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_ftsFieldGroup593);  
            stream_LPAREN.add(LPAREN38);

            pushFollow(FOLLOW_ftsFieldGroupImplicitConjunctionOrDisjunction_in_ftsFieldGroup595);
            ftsFieldGroupImplicitConjunctionOrDisjunction39=ftsFieldGroupImplicitConjunctionOrDisjunction();

            state._fsp--;

            stream_ftsFieldGroupImplicitConjunctionOrDisjunction.add(ftsFieldGroupImplicitConjunctionOrDisjunction39.getTree());
            RPAREN40=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_ftsFieldGroup597);  
            stream_RPAREN.add(RPAREN40);



            // AST REWRITE
            // elements: ftsFieldGroupImplicitConjunctionOrDisjunction, columnReference
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 203:3: -> ^( FIELD_GROUP columnReference ftsFieldGroupImplicitConjunctionOrDisjunction )
            {
                // W:\\workspace-cmis\\ANTLR\\FTS.g:203:6: ^( FIELD_GROUP columnReference ftsFieldGroupImplicitConjunctionOrDisjunction )
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

        catch(RecognitionException e)
        {
           throw e;
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
    // W:\\workspace-cmis\\ANTLR\\FTS.g:206:1: ftsFieldGroupImplicitConjunctionOrDisjunction : ({...}? ftsFieldGroupExplicitDisjunction ( ftsFieldGroupExplicitDisjunction )* -> ^( FIELD_CONJUNCTION ( ftsFieldGroupExplicitDisjunction )+ ) | ftsFieldGroupExplicitDisjunction ( ftsFieldGroupExplicitDisjunction )* -> ^( FIELD_DISJUNCTION ( ftsFieldGroupExplicitDisjunction )+ ) );
    public final FTSParser.ftsFieldGroupImplicitConjunctionOrDisjunction_return ftsFieldGroupImplicitConjunctionOrDisjunction() throws RecognitionException {
        FTSParser.ftsFieldGroupImplicitConjunctionOrDisjunction_return retval = new FTSParser.ftsFieldGroupImplicitConjunctionOrDisjunction_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        FTSParser.ftsFieldGroupExplicitDisjunction_return ftsFieldGroupExplicitDisjunction41 = null;

        FTSParser.ftsFieldGroupExplicitDisjunction_return ftsFieldGroupExplicitDisjunction42 = null;

        FTSParser.ftsFieldGroupExplicitDisjunction_return ftsFieldGroupExplicitDisjunction43 = null;

        FTSParser.ftsFieldGroupExplicitDisjunction_return ftsFieldGroupExplicitDisjunction44 = null;


        RewriteRuleSubtreeStream stream_ftsFieldGroupExplicitDisjunction=new RewriteRuleSubtreeStream(adaptor,"rule ftsFieldGroupExplicitDisjunction");
        try {
            // W:\\workspace-cmis\\ANTLR\\FTS.g:207:2: ({...}? ftsFieldGroupExplicitDisjunction ( ftsFieldGroupExplicitDisjunction )* -> ^( FIELD_CONJUNCTION ( ftsFieldGroupExplicitDisjunction )+ ) | ftsFieldGroupExplicitDisjunction ( ftsFieldGroupExplicitDisjunction )* -> ^( FIELD_DISJUNCTION ( ftsFieldGroupExplicitDisjunction )+ ) )
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
            case ID:
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
                    // W:\\workspace-cmis\\ANTLR\\FTS.g:207:4: {...}? ftsFieldGroupExplicitDisjunction ( ftsFieldGroupExplicitDisjunction )*
                    {
                    if ( !(defaultFieldConjunction()) ) {
                        throw new FailedPredicateException(input, "ftsFieldGroupImplicitConjunctionOrDisjunction", "defaultFieldConjunction()");
                    }
                    pushFollow(FOLLOW_ftsFieldGroupExplicitDisjunction_in_ftsFieldGroupImplicitConjunctionOrDisjunction623);
                    ftsFieldGroupExplicitDisjunction41=ftsFieldGroupExplicitDisjunction();

                    state._fsp--;

                    stream_ftsFieldGroupExplicitDisjunction.add(ftsFieldGroupExplicitDisjunction41.getTree());
                    // W:\\workspace-cmis\\ANTLR\\FTS.g:207:66: ( ftsFieldGroupExplicitDisjunction )*
                    loop10:
                    do {
                        int alt10=2;
                        int LA10_0 = input.LA(1);

                        if ( ((LA10_0>=MINUS && LA10_0<=LPAREN)||(LA10_0>=PLUS && LA10_0<=TILDA)||(LA10_0>=ID && LA10_0<=FTSWORD)) ) {
                            alt10=1;
                        }


                        switch (alt10) {
                    	case 1 :
                    	    // W:\\workspace-cmis\\ANTLR\\FTS.g:207:67: ftsFieldGroupExplicitDisjunction
                    	    {
                    	    pushFollow(FOLLOW_ftsFieldGroupExplicitDisjunction_in_ftsFieldGroupImplicitConjunctionOrDisjunction626);
                    	    ftsFieldGroupExplicitDisjunction42=ftsFieldGroupExplicitDisjunction();

                    	    state._fsp--;

                    	    stream_ftsFieldGroupExplicitDisjunction.add(ftsFieldGroupExplicitDisjunction42.getTree());

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
                    // 208:3: -> ^( FIELD_CONJUNCTION ( ftsFieldGroupExplicitDisjunction )+ )
                    {
                        // W:\\workspace-cmis\\ANTLR\\FTS.g:208:6: ^( FIELD_CONJUNCTION ( ftsFieldGroupExplicitDisjunction )+ )
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
                    // W:\\workspace-cmis\\ANTLR\\FTS.g:209:4: ftsFieldGroupExplicitDisjunction ( ftsFieldGroupExplicitDisjunction )*
                    {
                    pushFollow(FOLLOW_ftsFieldGroupExplicitDisjunction_in_ftsFieldGroupImplicitConjunctionOrDisjunction644);
                    ftsFieldGroupExplicitDisjunction43=ftsFieldGroupExplicitDisjunction();

                    state._fsp--;

                    stream_ftsFieldGroupExplicitDisjunction.add(ftsFieldGroupExplicitDisjunction43.getTree());
                    // W:\\workspace-cmis\\ANTLR\\FTS.g:209:37: ( ftsFieldGroupExplicitDisjunction )*
                    loop11:
                    do {
                        int alt11=2;
                        int LA11_0 = input.LA(1);

                        if ( ((LA11_0>=MINUS && LA11_0<=LPAREN)||(LA11_0>=PLUS && LA11_0<=TILDA)||(LA11_0>=ID && LA11_0<=FTSWORD)) ) {
                            alt11=1;
                        }


                        switch (alt11) {
                    	case 1 :
                    	    // W:\\workspace-cmis\\ANTLR\\FTS.g:209:38: ftsFieldGroupExplicitDisjunction
                    	    {
                    	    pushFollow(FOLLOW_ftsFieldGroupExplicitDisjunction_in_ftsFieldGroupImplicitConjunctionOrDisjunction647);
                    	    ftsFieldGroupExplicitDisjunction44=ftsFieldGroupExplicitDisjunction();

                    	    state._fsp--;

                    	    stream_ftsFieldGroupExplicitDisjunction.add(ftsFieldGroupExplicitDisjunction44.getTree());

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
                    // 210:3: -> ^( FIELD_DISJUNCTION ( ftsFieldGroupExplicitDisjunction )+ )
                    {
                        // W:\\workspace-cmis\\ANTLR\\FTS.g:210:6: ^( FIELD_DISJUNCTION ( ftsFieldGroupExplicitDisjunction )+ )
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

        catch(RecognitionException e)
        {
           throw e;
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
    // W:\\workspace-cmis\\ANTLR\\FTS.g:213:1: ftsFieldGroupExplicitDisjunction : ftsFieldGroupExplictConjunction ( OR ftsFieldGroupExplictConjunction )* -> ^( FIELD_DISJUNCTION ( ftsFieldGroupExplictConjunction )+ ) ;
    public final FTSParser.ftsFieldGroupExplicitDisjunction_return ftsFieldGroupExplicitDisjunction() throws RecognitionException {
        FTSParser.ftsFieldGroupExplicitDisjunction_return retval = new FTSParser.ftsFieldGroupExplicitDisjunction_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token OR46=null;
        FTSParser.ftsFieldGroupExplictConjunction_return ftsFieldGroupExplictConjunction45 = null;

        FTSParser.ftsFieldGroupExplictConjunction_return ftsFieldGroupExplictConjunction47 = null;


        Object OR46_tree=null;
        RewriteRuleTokenStream stream_OR=new RewriteRuleTokenStream(adaptor,"token OR");
        RewriteRuleSubtreeStream stream_ftsFieldGroupExplictConjunction=new RewriteRuleSubtreeStream(adaptor,"rule ftsFieldGroupExplictConjunction");
        try {
            // W:\\workspace-cmis\\ANTLR\\FTS.g:214:2: ( ftsFieldGroupExplictConjunction ( OR ftsFieldGroupExplictConjunction )* -> ^( FIELD_DISJUNCTION ( ftsFieldGroupExplictConjunction )+ ) )
            // W:\\workspace-cmis\\ANTLR\\FTS.g:214:4: ftsFieldGroupExplictConjunction ( OR ftsFieldGroupExplictConjunction )*
            {
            pushFollow(FOLLOW_ftsFieldGroupExplictConjunction_in_ftsFieldGroupExplicitDisjunction672);
            ftsFieldGroupExplictConjunction45=ftsFieldGroupExplictConjunction();

            state._fsp--;

            stream_ftsFieldGroupExplictConjunction.add(ftsFieldGroupExplictConjunction45.getTree());
            // W:\\workspace-cmis\\ANTLR\\FTS.g:214:36: ( OR ftsFieldGroupExplictConjunction )*
            loop13:
            do {
                int alt13=2;
                int LA13_0 = input.LA(1);

                if ( (LA13_0==OR) ) {
                    alt13=1;
                }


                switch (alt13) {
            	case 1 :
            	    // W:\\workspace-cmis\\ANTLR\\FTS.g:214:37: OR ftsFieldGroupExplictConjunction
            	    {
            	    OR46=(Token)match(input,OR,FOLLOW_OR_in_ftsFieldGroupExplicitDisjunction675);  
            	    stream_OR.add(OR46);

            	    pushFollow(FOLLOW_ftsFieldGroupExplictConjunction_in_ftsFieldGroupExplicitDisjunction677);
            	    ftsFieldGroupExplictConjunction47=ftsFieldGroupExplictConjunction();

            	    state._fsp--;

            	    stream_ftsFieldGroupExplictConjunction.add(ftsFieldGroupExplictConjunction47.getTree());

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
            // 215:3: -> ^( FIELD_DISJUNCTION ( ftsFieldGroupExplictConjunction )+ )
            {
                // W:\\workspace-cmis\\ANTLR\\FTS.g:215:6: ^( FIELD_DISJUNCTION ( ftsFieldGroupExplictConjunction )+ )
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

        catch(RecognitionException e)
        {
           throw e;
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
    // W:\\workspace-cmis\\ANTLR\\FTS.g:218:1: ftsFieldGroupExplictConjunction : ftsFieldGroupNot ( AND ftsFieldGroupNot )* -> ^( FIELD_CONJUNCTION ( ftsFieldGroupNot )+ ) ;
    public final FTSParser.ftsFieldGroupExplictConjunction_return ftsFieldGroupExplictConjunction() throws RecognitionException {
        FTSParser.ftsFieldGroupExplictConjunction_return retval = new FTSParser.ftsFieldGroupExplictConjunction_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token AND49=null;
        FTSParser.ftsFieldGroupNot_return ftsFieldGroupNot48 = null;

        FTSParser.ftsFieldGroupNot_return ftsFieldGroupNot50 = null;


        Object AND49_tree=null;
        RewriteRuleTokenStream stream_AND=new RewriteRuleTokenStream(adaptor,"token AND");
        RewriteRuleSubtreeStream stream_ftsFieldGroupNot=new RewriteRuleSubtreeStream(adaptor,"rule ftsFieldGroupNot");
        try {
            // W:\\workspace-cmis\\ANTLR\\FTS.g:219:2: ( ftsFieldGroupNot ( AND ftsFieldGroupNot )* -> ^( FIELD_CONJUNCTION ( ftsFieldGroupNot )+ ) )
            // W:\\workspace-cmis\\ANTLR\\FTS.g:219:4: ftsFieldGroupNot ( AND ftsFieldGroupNot )*
            {
            pushFollow(FOLLOW_ftsFieldGroupNot_in_ftsFieldGroupExplictConjunction702);
            ftsFieldGroupNot48=ftsFieldGroupNot();

            state._fsp--;

            stream_ftsFieldGroupNot.add(ftsFieldGroupNot48.getTree());
            // W:\\workspace-cmis\\ANTLR\\FTS.g:219:21: ( AND ftsFieldGroupNot )*
            loop14:
            do {
                int alt14=2;
                int LA14_0 = input.LA(1);

                if ( (LA14_0==AND) ) {
                    alt14=1;
                }


                switch (alt14) {
            	case 1 :
            	    // W:\\workspace-cmis\\ANTLR\\FTS.g:219:22: AND ftsFieldGroupNot
            	    {
            	    AND49=(Token)match(input,AND,FOLLOW_AND_in_ftsFieldGroupExplictConjunction705);  
            	    stream_AND.add(AND49);

            	    pushFollow(FOLLOW_ftsFieldGroupNot_in_ftsFieldGroupExplictConjunction707);
            	    ftsFieldGroupNot50=ftsFieldGroupNot();

            	    state._fsp--;

            	    stream_ftsFieldGroupNot.add(ftsFieldGroupNot50.getTree());

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
            // 220:3: -> ^( FIELD_CONJUNCTION ( ftsFieldGroupNot )+ )
            {
                // W:\\workspace-cmis\\ANTLR\\FTS.g:220:6: ^( FIELD_CONJUNCTION ( ftsFieldGroupNot )+ )
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

        catch(RecognitionException e)
        {
           throw e;
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
    // W:\\workspace-cmis\\ANTLR\\FTS.g:224:1: ftsFieldGroupNot : ( MINUS ftsFieldGroupTest -> FIELD_NEGATION ftsFieldGroupTest | ftsFieldGroupTest -> ftsFieldGroupTest );
    public final FTSParser.ftsFieldGroupNot_return ftsFieldGroupNot() throws RecognitionException {
        FTSParser.ftsFieldGroupNot_return retval = new FTSParser.ftsFieldGroupNot_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token MINUS51=null;
        FTSParser.ftsFieldGroupTest_return ftsFieldGroupTest52 = null;

        FTSParser.ftsFieldGroupTest_return ftsFieldGroupTest53 = null;


        Object MINUS51_tree=null;
        RewriteRuleTokenStream stream_MINUS=new RewriteRuleTokenStream(adaptor,"token MINUS");
        RewriteRuleSubtreeStream stream_ftsFieldGroupTest=new RewriteRuleSubtreeStream(adaptor,"rule ftsFieldGroupTest");
        try {
            // W:\\workspace-cmis\\ANTLR\\FTS.g:224:19: ( MINUS ftsFieldGroupTest -> FIELD_NEGATION ftsFieldGroupTest | ftsFieldGroupTest -> ftsFieldGroupTest )
            int alt15=2;
            int LA15_0 = input.LA(1);

            if ( (LA15_0==MINUS) ) {
                alt15=1;
            }
            else if ( (LA15_0==LPAREN||(LA15_0>=PLUS && LA15_0<=TILDA)||(LA15_0>=ID && LA15_0<=FTSWORD)) ) {
                alt15=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 15, 0, input);

                throw nvae;
            }
            switch (alt15) {
                case 1 :
                    // W:\\workspace-cmis\\ANTLR\\FTS.g:224:21: MINUS ftsFieldGroupTest
                    {
                    MINUS51=(Token)match(input,MINUS,FOLLOW_MINUS_in_ftsFieldGroupNot734);  
                    stream_MINUS.add(MINUS51);

                    pushFollow(FOLLOW_ftsFieldGroupTest_in_ftsFieldGroupNot736);
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
                    // 225:4: -> FIELD_NEGATION ftsFieldGroupTest
                    {
                        adaptor.addChild(root_0, (Object)adaptor.create(FIELD_NEGATION, "FIELD_NEGATION"));
                        adaptor.addChild(root_0, stream_ftsFieldGroupTest.nextTree());

                    }

                    retval.tree = root_0;retval.tree = root_0;
                    }
                    break;
                case 2 :
                    // W:\\workspace-cmis\\ANTLR\\FTS.g:226:5: ftsFieldGroupTest
                    {
                    pushFollow(FOLLOW_ftsFieldGroupTest_in_ftsFieldGroupNot751);
                    ftsFieldGroupTest53=ftsFieldGroupTest();

                    state._fsp--;

                    stream_ftsFieldGroupTest.add(ftsFieldGroupTest53.getTree());


                    // AST REWRITE
                    // elements: ftsFieldGroupTest
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 227:4: -> ftsFieldGroupTest
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

        catch(RecognitionException e)
        {
           throw e;
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
    // W:\\workspace-cmis\\ANTLR\\FTS.g:231:1: ftsFieldGroupTest : ( ftsFieldGroupTerm -> ^( FG_TERM ftsFieldGroupTerm ) | ftsFieldGroupExactTerm -> ^( FG_EXACT_TERM ftsFieldGroupExactTerm ) | ftsFieldGroupPhrase -> ^( FG_PHRASE ftsFieldGroupPhrase ) | ftsFieldGroupSynonym -> ^( FG_SYNONYM ftsFieldGroupSynonym ) | ftsFieldGroupProximity -> ^( FG_PROXIMITY ftsFieldGroupProximity ) | ftsFieldGroupRange -> ^( FG_RANGE ftsFieldGroupRange ) | LPAREN ftsFieldGroupImplicitConjunctionOrDisjunction RPAREN -> ftsFieldGroupImplicitConjunctionOrDisjunction );
    public final FTSParser.ftsFieldGroupTest_return ftsFieldGroupTest() throws RecognitionException {
        FTSParser.ftsFieldGroupTest_return retval = new FTSParser.ftsFieldGroupTest_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token LPAREN60=null;
        Token RPAREN62=null;
        FTSParser.ftsFieldGroupTerm_return ftsFieldGroupTerm54 = null;

        FTSParser.ftsFieldGroupExactTerm_return ftsFieldGroupExactTerm55 = null;

        FTSParser.ftsFieldGroupPhrase_return ftsFieldGroupPhrase56 = null;

        FTSParser.ftsFieldGroupSynonym_return ftsFieldGroupSynonym57 = null;

        FTSParser.ftsFieldGroupProximity_return ftsFieldGroupProximity58 = null;

        FTSParser.ftsFieldGroupRange_return ftsFieldGroupRange59 = null;

        FTSParser.ftsFieldGroupImplicitConjunctionOrDisjunction_return ftsFieldGroupImplicitConjunctionOrDisjunction61 = null;


        Object LPAREN60_tree=null;
        Object RPAREN62_tree=null;
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
            // W:\\workspace-cmis\\ANTLR\\FTS.g:232:2: ( ftsFieldGroupTerm -> ^( FG_TERM ftsFieldGroupTerm ) | ftsFieldGroupExactTerm -> ^( FG_EXACT_TERM ftsFieldGroupExactTerm ) | ftsFieldGroupPhrase -> ^( FG_PHRASE ftsFieldGroupPhrase ) | ftsFieldGroupSynonym -> ^( FG_SYNONYM ftsFieldGroupSynonym ) | ftsFieldGroupProximity -> ^( FG_PROXIMITY ftsFieldGroupProximity ) | ftsFieldGroupRange -> ^( FG_RANGE ftsFieldGroupRange ) | LPAREN ftsFieldGroupImplicitConjunctionOrDisjunction RPAREN -> ftsFieldGroupImplicitConjunctionOrDisjunction )
            int alt16=7;
            switch ( input.LA(1) ) {
            case ID:
            case FTSWORD:
                {
                switch ( input.LA(2) ) {
                case DOTDOT:
                    {
                    alt16=6;
                    }
                    break;
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
                case PLUS:
                case FTSPHRASE:
                case TILDA:
                case ID:
                case FTSWORD:
                    {
                    alt16=1;
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
                    // W:\\workspace-cmis\\ANTLR\\FTS.g:232:4: ftsFieldGroupTerm
                    {
                    pushFollow(FOLLOW_ftsFieldGroupTerm_in_ftsFieldGroupTest770);
                    ftsFieldGroupTerm54=ftsFieldGroupTerm();

                    state._fsp--;

                    stream_ftsFieldGroupTerm.add(ftsFieldGroupTerm54.getTree());


                    // AST REWRITE
                    // elements: ftsFieldGroupTerm
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 233:3: -> ^( FG_TERM ftsFieldGroupTerm )
                    {
                        // W:\\workspace-cmis\\ANTLR\\FTS.g:233:6: ^( FG_TERM ftsFieldGroupTerm )
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
                    // W:\\workspace-cmis\\ANTLR\\FTS.g:234:4: ftsFieldGroupExactTerm
                    {
                    pushFollow(FOLLOW_ftsFieldGroupExactTerm_in_ftsFieldGroupTest785);
                    ftsFieldGroupExactTerm55=ftsFieldGroupExactTerm();

                    state._fsp--;

                    stream_ftsFieldGroupExactTerm.add(ftsFieldGroupExactTerm55.getTree());


                    // AST REWRITE
                    // elements: ftsFieldGroupExactTerm
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 235:3: -> ^( FG_EXACT_TERM ftsFieldGroupExactTerm )
                    {
                        // W:\\workspace-cmis\\ANTLR\\FTS.g:235:6: ^( FG_EXACT_TERM ftsFieldGroupExactTerm )
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
                    // W:\\workspace-cmis\\ANTLR\\FTS.g:236:4: ftsFieldGroupPhrase
                    {
                    pushFollow(FOLLOW_ftsFieldGroupPhrase_in_ftsFieldGroupTest801);
                    ftsFieldGroupPhrase56=ftsFieldGroupPhrase();

                    state._fsp--;

                    stream_ftsFieldGroupPhrase.add(ftsFieldGroupPhrase56.getTree());


                    // AST REWRITE
                    // elements: ftsFieldGroupPhrase
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 237:3: -> ^( FG_PHRASE ftsFieldGroupPhrase )
                    {
                        // W:\\workspace-cmis\\ANTLR\\FTS.g:237:6: ^( FG_PHRASE ftsFieldGroupPhrase )
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
                    // W:\\workspace-cmis\\ANTLR\\FTS.g:238:4: ftsFieldGroupSynonym
                    {
                    pushFollow(FOLLOW_ftsFieldGroupSynonym_in_ftsFieldGroupTest816);
                    ftsFieldGroupSynonym57=ftsFieldGroupSynonym();

                    state._fsp--;

                    stream_ftsFieldGroupSynonym.add(ftsFieldGroupSynonym57.getTree());


                    // AST REWRITE
                    // elements: ftsFieldGroupSynonym
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 239:3: -> ^( FG_SYNONYM ftsFieldGroupSynonym )
                    {
                        // W:\\workspace-cmis\\ANTLR\\FTS.g:239:6: ^( FG_SYNONYM ftsFieldGroupSynonym )
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
                    // W:\\workspace-cmis\\ANTLR\\FTS.g:240:6: ftsFieldGroupProximity
                    {
                    pushFollow(FOLLOW_ftsFieldGroupProximity_in_ftsFieldGroupTest833);
                    ftsFieldGroupProximity58=ftsFieldGroupProximity();

                    state._fsp--;

                    stream_ftsFieldGroupProximity.add(ftsFieldGroupProximity58.getTree());


                    // AST REWRITE
                    // elements: ftsFieldGroupProximity
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 241:3: -> ^( FG_PROXIMITY ftsFieldGroupProximity )
                    {
                        // W:\\workspace-cmis\\ANTLR\\FTS.g:241:6: ^( FG_PROXIMITY ftsFieldGroupProximity )
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
                    // W:\\workspace-cmis\\ANTLR\\FTS.g:242:8: ftsFieldGroupRange
                    {
                    pushFollow(FOLLOW_ftsFieldGroupRange_in_ftsFieldGroupTest854);
                    ftsFieldGroupRange59=ftsFieldGroupRange();

                    state._fsp--;

                    stream_ftsFieldGroupRange.add(ftsFieldGroupRange59.getTree());


                    // AST REWRITE
                    // elements: ftsFieldGroupRange
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 243:9: -> ^( FG_RANGE ftsFieldGroupRange )
                    {
                        // W:\\workspace-cmis\\ANTLR\\FTS.g:243:12: ^( FG_RANGE ftsFieldGroupRange )
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
                    // W:\\workspace-cmis\\ANTLR\\FTS.g:244:4: LPAREN ftsFieldGroupImplicitConjunctionOrDisjunction RPAREN
                    {
                    LPAREN60=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_ftsFieldGroupTest875);  
                    stream_LPAREN.add(LPAREN60);

                    pushFollow(FOLLOW_ftsFieldGroupImplicitConjunctionOrDisjunction_in_ftsFieldGroupTest877);
                    ftsFieldGroupImplicitConjunctionOrDisjunction61=ftsFieldGroupImplicitConjunctionOrDisjunction();

                    state._fsp--;

                    stream_ftsFieldGroupImplicitConjunctionOrDisjunction.add(ftsFieldGroupImplicitConjunctionOrDisjunction61.getTree());
                    RPAREN62=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_ftsFieldGroupTest879);  
                    stream_RPAREN.add(RPAREN62);



                    // AST REWRITE
                    // elements: ftsFieldGroupImplicitConjunctionOrDisjunction
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 245:3: -> ftsFieldGroupImplicitConjunctionOrDisjunction
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

        catch(RecognitionException e)
        {
           throw e;
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
    // W:\\workspace-cmis\\ANTLR\\FTS.g:248:1: ftsFieldGroupTerm : ftsWord ;
    public final FTSParser.ftsFieldGroupTerm_return ftsFieldGroupTerm() throws RecognitionException {
        FTSParser.ftsFieldGroupTerm_return retval = new FTSParser.ftsFieldGroupTerm_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        FTSParser.ftsWord_return ftsWord63 = null;



        try {
            // W:\\workspace-cmis\\ANTLR\\FTS.g:249:2: ( ftsWord )
            // W:\\workspace-cmis\\ANTLR\\FTS.g:249:4: ftsWord
            {
            root_0 = (Object)adaptor.nil();

            pushFollow(FOLLOW_ftsWord_in_ftsFieldGroupTerm897);
            ftsWord63=ftsWord();

            state._fsp--;

            adaptor.addChild(root_0, ftsWord63.getTree());

            }

            retval.stop = input.LT(-1);

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }

        catch(RecognitionException e)
        {
           throw e;
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
    // W:\\workspace-cmis\\ANTLR\\FTS.g:252:1: ftsFieldGroupExactTerm : PLUS ftsFieldGroupTerm -> ftsFieldGroupTerm ;
    public final FTSParser.ftsFieldGroupExactTerm_return ftsFieldGroupExactTerm() throws RecognitionException {
        FTSParser.ftsFieldGroupExactTerm_return retval = new FTSParser.ftsFieldGroupExactTerm_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token PLUS64=null;
        FTSParser.ftsFieldGroupTerm_return ftsFieldGroupTerm65 = null;


        Object PLUS64_tree=null;
        RewriteRuleTokenStream stream_PLUS=new RewriteRuleTokenStream(adaptor,"token PLUS");
        RewriteRuleSubtreeStream stream_ftsFieldGroupTerm=new RewriteRuleSubtreeStream(adaptor,"rule ftsFieldGroupTerm");
        try {
            // W:\\workspace-cmis\\ANTLR\\FTS.g:253:2: ( PLUS ftsFieldGroupTerm -> ftsFieldGroupTerm )
            // W:\\workspace-cmis\\ANTLR\\FTS.g:253:4: PLUS ftsFieldGroupTerm
            {
            PLUS64=(Token)match(input,PLUS,FOLLOW_PLUS_in_ftsFieldGroupExactTerm909);  
            stream_PLUS.add(PLUS64);

            pushFollow(FOLLOW_ftsFieldGroupTerm_in_ftsFieldGroupExactTerm911);
            ftsFieldGroupTerm65=ftsFieldGroupTerm();

            state._fsp--;

            stream_ftsFieldGroupTerm.add(ftsFieldGroupTerm65.getTree());


            // AST REWRITE
            // elements: ftsFieldGroupTerm
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 254:3: -> ftsFieldGroupTerm
            {
                adaptor.addChild(root_0, stream_ftsFieldGroupTerm.nextTree());

            }

            retval.tree = root_0;retval.tree = root_0;
            }

            retval.stop = input.LT(-1);

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }

        catch(RecognitionException e)
        {
           throw e;
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
    // W:\\workspace-cmis\\ANTLR\\FTS.g:257:1: ftsFieldGroupPhrase : FTSPHRASE ;
    public final FTSParser.ftsFieldGroupPhrase_return ftsFieldGroupPhrase() throws RecognitionException {
        FTSParser.ftsFieldGroupPhrase_return retval = new FTSParser.ftsFieldGroupPhrase_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token FTSPHRASE66=null;

        Object FTSPHRASE66_tree=null;

        try {
            // W:\\workspace-cmis\\ANTLR\\FTS.g:258:2: ( FTSPHRASE )
            // W:\\workspace-cmis\\ANTLR\\FTS.g:258:6: FTSPHRASE
            {
            root_0 = (Object)adaptor.nil();

            FTSPHRASE66=(Token)match(input,FTSPHRASE,FOLLOW_FTSPHRASE_in_ftsFieldGroupPhrase931); 
            FTSPHRASE66_tree = (Object)adaptor.create(FTSPHRASE66);
            adaptor.addChild(root_0, FTSPHRASE66_tree);


            }

            retval.stop = input.LT(-1);

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }

        catch(RecognitionException e)
        {
           throw e;
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
    // W:\\workspace-cmis\\ANTLR\\FTS.g:261:1: ftsFieldGroupSynonym : TILDA ftsFieldGroupTerm -> ftsFieldGroupTerm ;
    public final FTSParser.ftsFieldGroupSynonym_return ftsFieldGroupSynonym() throws RecognitionException {
        FTSParser.ftsFieldGroupSynonym_return retval = new FTSParser.ftsFieldGroupSynonym_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token TILDA67=null;
        FTSParser.ftsFieldGroupTerm_return ftsFieldGroupTerm68 = null;


        Object TILDA67_tree=null;
        RewriteRuleTokenStream stream_TILDA=new RewriteRuleTokenStream(adaptor,"token TILDA");
        RewriteRuleSubtreeStream stream_ftsFieldGroupTerm=new RewriteRuleSubtreeStream(adaptor,"rule ftsFieldGroupTerm");
        try {
            // W:\\workspace-cmis\\ANTLR\\FTS.g:262:2: ( TILDA ftsFieldGroupTerm -> ftsFieldGroupTerm )
            // W:\\workspace-cmis\\ANTLR\\FTS.g:262:4: TILDA ftsFieldGroupTerm
            {
            TILDA67=(Token)match(input,TILDA,FOLLOW_TILDA_in_ftsFieldGroupSynonym943);  
            stream_TILDA.add(TILDA67);

            pushFollow(FOLLOW_ftsFieldGroupTerm_in_ftsFieldGroupSynonym945);
            ftsFieldGroupTerm68=ftsFieldGroupTerm();

            state._fsp--;

            stream_ftsFieldGroupTerm.add(ftsFieldGroupTerm68.getTree());


            // AST REWRITE
            // elements: ftsFieldGroupTerm
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 263:3: -> ftsFieldGroupTerm
            {
                adaptor.addChild(root_0, stream_ftsFieldGroupTerm.nextTree());

            }

            retval.tree = root_0;retval.tree = root_0;
            }

            retval.stop = input.LT(-1);

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }

        catch(RecognitionException e)
        {
           throw e;
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
    // W:\\workspace-cmis\\ANTLR\\FTS.g:266:1: ftsFieldGroupProximity : ftsFieldGroupTerm STAR ftsFieldGroupTerm -> ftsFieldGroupTerm ftsFieldGroupTerm ;
    public final FTSParser.ftsFieldGroupProximity_return ftsFieldGroupProximity() throws RecognitionException {
        FTSParser.ftsFieldGroupProximity_return retval = new FTSParser.ftsFieldGroupProximity_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token STAR70=null;
        FTSParser.ftsFieldGroupTerm_return ftsFieldGroupTerm69 = null;

        FTSParser.ftsFieldGroupTerm_return ftsFieldGroupTerm71 = null;


        Object STAR70_tree=null;
        RewriteRuleTokenStream stream_STAR=new RewriteRuleTokenStream(adaptor,"token STAR");
        RewriteRuleSubtreeStream stream_ftsFieldGroupTerm=new RewriteRuleSubtreeStream(adaptor,"rule ftsFieldGroupTerm");
        try {
            // W:\\workspace-cmis\\ANTLR\\FTS.g:267:2: ( ftsFieldGroupTerm STAR ftsFieldGroupTerm -> ftsFieldGroupTerm ftsFieldGroupTerm )
            // W:\\workspace-cmis\\ANTLR\\FTS.g:267:4: ftsFieldGroupTerm STAR ftsFieldGroupTerm
            {
            pushFollow(FOLLOW_ftsFieldGroupTerm_in_ftsFieldGroupProximity963);
            ftsFieldGroupTerm69=ftsFieldGroupTerm();

            state._fsp--;

            stream_ftsFieldGroupTerm.add(ftsFieldGroupTerm69.getTree());
            STAR70=(Token)match(input,STAR,FOLLOW_STAR_in_ftsFieldGroupProximity965);  
            stream_STAR.add(STAR70);

            pushFollow(FOLLOW_ftsFieldGroupTerm_in_ftsFieldGroupProximity967);
            ftsFieldGroupTerm71=ftsFieldGroupTerm();

            state._fsp--;

            stream_ftsFieldGroupTerm.add(ftsFieldGroupTerm71.getTree());


            // AST REWRITE
            // elements: ftsFieldGroupTerm, ftsFieldGroupTerm
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 268:3: -> ftsFieldGroupTerm ftsFieldGroupTerm
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

        catch(RecognitionException e)
        {
           throw e;
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
    // W:\\workspace-cmis\\ANTLR\\FTS.g:271:1: ftsFieldGroupRange : ftsFieldGroupTerm DOTDOT ftsFieldGroupTerm -> ftsFieldGroupTerm ftsFieldGroupTerm ;
    public final FTSParser.ftsFieldGroupRange_return ftsFieldGroupRange() throws RecognitionException {
        FTSParser.ftsFieldGroupRange_return retval = new FTSParser.ftsFieldGroupRange_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token DOTDOT73=null;
        FTSParser.ftsFieldGroupTerm_return ftsFieldGroupTerm72 = null;

        FTSParser.ftsFieldGroupTerm_return ftsFieldGroupTerm74 = null;


        Object DOTDOT73_tree=null;
        RewriteRuleTokenStream stream_DOTDOT=new RewriteRuleTokenStream(adaptor,"token DOTDOT");
        RewriteRuleSubtreeStream stream_ftsFieldGroupTerm=new RewriteRuleSubtreeStream(adaptor,"rule ftsFieldGroupTerm");
        try {
            // W:\\workspace-cmis\\ANTLR\\FTS.g:271:19: ( ftsFieldGroupTerm DOTDOT ftsFieldGroupTerm -> ftsFieldGroupTerm ftsFieldGroupTerm )
            // W:\\workspace-cmis\\ANTLR\\FTS.g:271:21: ftsFieldGroupTerm DOTDOT ftsFieldGroupTerm
            {
            pushFollow(FOLLOW_ftsFieldGroupTerm_in_ftsFieldGroupRange985);
            ftsFieldGroupTerm72=ftsFieldGroupTerm();

            state._fsp--;

            stream_ftsFieldGroupTerm.add(ftsFieldGroupTerm72.getTree());
            DOTDOT73=(Token)match(input,DOTDOT,FOLLOW_DOTDOT_in_ftsFieldGroupRange987);  
            stream_DOTDOT.add(DOTDOT73);

            pushFollow(FOLLOW_ftsFieldGroupTerm_in_ftsFieldGroupRange989);
            ftsFieldGroupTerm74=ftsFieldGroupTerm();

            state._fsp--;

            stream_ftsFieldGroupTerm.add(ftsFieldGroupTerm74.getTree());


            // AST REWRITE
            // elements: ftsFieldGroupTerm, ftsFieldGroupTerm
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 272:3: -> ftsFieldGroupTerm ftsFieldGroupTerm
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

        catch(RecognitionException e)
        {
           throw e;
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
    // W:\\workspace-cmis\\ANTLR\\FTS.g:275:1: columnReference : (qualifier= identifier DOT )? name= identifier -> ^( COLUMN_REF $name ( $qualifier)? ) ;
    public final FTSParser.columnReference_return columnReference() throws RecognitionException {
        FTSParser.columnReference_return retval = new FTSParser.columnReference_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token DOT75=null;
        FTSParser.identifier_return qualifier = null;

        FTSParser.identifier_return name = null;


        Object DOT75_tree=null;
        RewriteRuleTokenStream stream_DOT=new RewriteRuleTokenStream(adaptor,"token DOT");
        RewriteRuleSubtreeStream stream_identifier=new RewriteRuleSubtreeStream(adaptor,"rule identifier");
        try {
            // W:\\workspace-cmis\\ANTLR\\FTS.g:276:2: ( (qualifier= identifier DOT )? name= identifier -> ^( COLUMN_REF $name ( $qualifier)? ) )
            // W:\\workspace-cmis\\ANTLR\\FTS.g:276:4: (qualifier= identifier DOT )? name= identifier
            {
            // W:\\workspace-cmis\\ANTLR\\FTS.g:276:4: (qualifier= identifier DOT )?
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
                    // W:\\workspace-cmis\\ANTLR\\FTS.g:276:6: qualifier= identifier DOT
                    {
                    pushFollow(FOLLOW_identifier_in_columnReference1013);
                    qualifier=identifier();

                    state._fsp--;

                    stream_identifier.add(qualifier.getTree());
                    DOT75=(Token)match(input,DOT,FOLLOW_DOT_in_columnReference1015);  
                    stream_DOT.add(DOT75);


                    }
                    break;

            }

            pushFollow(FOLLOW_identifier_in_columnReference1022);
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
            // 277:3: -> ^( COLUMN_REF $name ( $qualifier)? )
            {
                // W:\\workspace-cmis\\ANTLR\\FTS.g:277:6: ^( COLUMN_REF $name ( $qualifier)? )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(COLUMN_REF, "COLUMN_REF"), root_1);

                adaptor.addChild(root_1, stream_name.nextTree());
                // W:\\workspace-cmis\\ANTLR\\FTS.g:277:25: ( $qualifier)?
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

        catch(RecognitionException e)
        {
           throw e;
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
    // W:\\workspace-cmis\\ANTLR\\FTS.g:280:1: identifier : ID ;
    public final FTSParser.identifier_return identifier() throws RecognitionException {
        FTSParser.identifier_return retval = new FTSParser.identifier_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token ID76=null;

        Object ID76_tree=null;

        try {
            // W:\\workspace-cmis\\ANTLR\\FTS.g:281:2: ( ID )
            // W:\\workspace-cmis\\ANTLR\\FTS.g:281:4: ID
            {
            root_0 = (Object)adaptor.nil();

            ID76=(Token)match(input,ID,FOLLOW_ID_in_identifier1050); 
            ID76_tree = (Object)adaptor.create(ID76);
            adaptor.addChild(root_0, ID76_tree);


            }

            retval.stop = input.LT(-1);

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }

        catch(RecognitionException e)
        {
           throw e;
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end identifier

    public static class ftsWord_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start ftsWord
    // W:\\workspace-cmis\\ANTLR\\FTS.g:284:1: ftsWord : ( ID | FTSWORD );
    public final FTSParser.ftsWord_return ftsWord() throws RecognitionException {
        FTSParser.ftsWord_return retval = new FTSParser.ftsWord_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token set77=null;

        Object set77_tree=null;

        try {
            // W:\\workspace-cmis\\ANTLR\\FTS.g:285:5: ( ID | FTSWORD )
            // W:\\workspace-cmis\\ANTLR\\FTS.g:
            {
            root_0 = (Object)adaptor.nil();

            set77=(Token)input.LT(1);
            if ( (input.LA(1)>=ID && input.LA(1)<=FTSWORD) ) {
                input.consume();
                adaptor.addChild(root_0, (Object)adaptor.create(set77));
                state.errorRecovery=false;
            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                throw mse;
            }


            }

            retval.stop = input.LT(-1);

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }

        catch(RecognitionException e)
        {
           throw e;
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end ftsWord

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
            "\1\1\1\7\2\uffff\1\4\1\5\1\6\3\uffff\1\2\1\3",
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
            return "137:1: ftsImplicitConjunctionOrDisjunction : ({...}? ftsExplicitDisjunction ( ftsExplicitDisjunction )* -> ^( CONJUNCTION ( ftsExplicitDisjunction )+ ) | ftsExplicitDisjunction ( ftsExplicitDisjunction )* -> ^( DISJUNCTION ( ftsExplicitDisjunction )+ ) );";
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
        "\1\uffff\2\13\13\uffff";
    static final String DFA7_minS =
        "\1\32\2\27\6\uffff\1\32\1\43\2\uffff\1\34";
    static final String DFA7_maxS =
        "\3\44\6\uffff\1\44\1\43\2\uffff\1\34";
    static final String DFA7_acceptS =
        "\3\uffff\1\2\1\3\1\4\1\10\1\6\1\5\2\uffff\1\1\1\7\1\uffff";
    static final String DFA7_specialS =
        "\16\uffff}>";
    static final String[] DFA7_transitionS = {
            "\1\6\2\uffff\1\3\1\4\1\5\3\uffff\1\1\1\2",
            "\5\13\1\11\3\13\1\10\1\7\1\12\2\13",
            "\5\13\1\uffff\3\13\1\10\1\7\1\uffff\2\13",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\14\3\uffff\1\4\4\uffff\2\13",
            "\1\15",
            "",
            "",
            "\1\11"
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
            return "162:1: ftsTest : ( ftsTerm -> ^( TERM ftsTerm ) | ftsExactTerm -> ^( EXACT_TERM ftsExactTerm ) | ftsPhrase -> ^( PHRASE ftsPhrase ) | ftsSynonym -> ^( SYNONYM ftsSynonym ) | ftsFieldGroupProximity -> ^( FG_PROXIMITY ftsFieldGroupProximity ) | ftsFieldGroupRange -> ^( FG_RANGE ftsFieldGroupRange ) | ftsFieldGroup | LPAREN ftsImplicitConjunctionOrDisjunction RPAREN -> ftsImplicitConjunctionOrDisjunction );";
        }
    }
 

    public static final BitSet FOLLOW_ftsImplicitConjunctionOrDisjunction_in_fts146 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_fts148 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ftsExplicitDisjunction_in_ftsImplicitConjunctionOrDisjunction170 = new BitSet(new long[]{0x00000018E6000002L});
    public static final BitSet FOLLOW_ftsExplicitDisjunction_in_ftsImplicitConjunctionOrDisjunction173 = new BitSet(new long[]{0x00000018E6000002L});
    public static final BitSet FOLLOW_ftsExplicitDisjunction_in_ftsImplicitConjunctionOrDisjunction192 = new BitSet(new long[]{0x00000018E6000000L});
    public static final BitSet FOLLOW_ftsExplicitDisjunction_in_ftsImplicitConjunctionOrDisjunction195 = new BitSet(new long[]{0x00000018E6000002L});
    public static final BitSet FOLLOW_ftsExplictConjunction_in_ftsExplicitDisjunction220 = new BitSet(new long[]{0x0000000000800002L});
    public static final BitSet FOLLOW_OR_in_ftsExplicitDisjunction223 = new BitSet(new long[]{0x00000018E6800000L});
    public static final BitSet FOLLOW_ftsExplictConjunction_in_ftsExplicitDisjunction225 = new BitSet(new long[]{0x0000000000800002L});
    public static final BitSet FOLLOW_ftsNot_in_ftsExplictConjunction250 = new BitSet(new long[]{0x0000000001000002L});
    public static final BitSet FOLLOW_AND_in_ftsExplictConjunction253 = new BitSet(new long[]{0x00000018E7000000L});
    public static final BitSet FOLLOW_ftsNot_in_ftsExplictConjunction255 = new BitSet(new long[]{0x0000000001000002L});
    public static final BitSet FOLLOW_MINUS_in_ftsNot286 = new BitSet(new long[]{0x00000018E6000000L});
    public static final BitSet FOLLOW_ftsTest_in_ftsNot288 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ftsTest_in_ftsNot303 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ftsTerm_in_ftsTest324 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ftsExactTerm_in_ftsTest339 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ftsPhrase_in_ftsTest359 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ftsSynonym_in_ftsTest385 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ftsFieldGroupProximity_in_ftsTest409 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ftsFieldGroupRange_in_ftsTest436 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ftsFieldGroup_in_ftsTest460 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_ftsTest469 = new BitSet(new long[]{0x00000018E6000000L});
    public static final BitSet FOLLOW_ftsImplicitConjunctionOrDisjunction_in_ftsTest471 = new BitSet(new long[]{0x0000000008000000L});
    public static final BitSet FOLLOW_RPAREN_in_ftsTest473 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_columnReference_in_ftsTerm491 = new BitSet(new long[]{0x0000000010000000L});
    public static final BitSet FOLLOW_COLON_in_ftsTerm493 = new BitSet(new long[]{0x0000001800000000L});
    public static final BitSet FOLLOW_ftsWord_in_ftsTerm497 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_PLUS_in_ftsExactTerm518 = new BitSet(new long[]{0x0000001800000000L});
    public static final BitSet FOLLOW_ftsTerm_in_ftsExactTerm520 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_columnReference_in_ftsPhrase541 = new BitSet(new long[]{0x0000000010000000L});
    public static final BitSet FOLLOW_COLON_in_ftsPhrase543 = new BitSet(new long[]{0x0000000040000000L});
    public static final BitSet FOLLOW_FTSPHRASE_in_ftsPhrase547 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TILDA_in_ftsSynonym568 = new BitSet(new long[]{0x0000001800000000L});
    public static final BitSet FOLLOW_ftsTerm_in_ftsSynonym570 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_columnReference_in_ftsFieldGroup589 = new BitSet(new long[]{0x0000000010000000L});
    public static final BitSet FOLLOW_COLON_in_ftsFieldGroup591 = new BitSet(new long[]{0x0000000004000000L});
    public static final BitSet FOLLOW_LPAREN_in_ftsFieldGroup593 = new BitSet(new long[]{0x00000018E6000000L});
    public static final BitSet FOLLOW_ftsFieldGroupImplicitConjunctionOrDisjunction_in_ftsFieldGroup595 = new BitSet(new long[]{0x0000000008000000L});
    public static final BitSet FOLLOW_RPAREN_in_ftsFieldGroup597 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ftsFieldGroupExplicitDisjunction_in_ftsFieldGroupImplicitConjunctionOrDisjunction623 = new BitSet(new long[]{0x00000018E6000002L});
    public static final BitSet FOLLOW_ftsFieldGroupExplicitDisjunction_in_ftsFieldGroupImplicitConjunctionOrDisjunction626 = new BitSet(new long[]{0x00000018E6000002L});
    public static final BitSet FOLLOW_ftsFieldGroupExplicitDisjunction_in_ftsFieldGroupImplicitConjunctionOrDisjunction644 = new BitSet(new long[]{0x00000018E6000000L});
    public static final BitSet FOLLOW_ftsFieldGroupExplicitDisjunction_in_ftsFieldGroupImplicitConjunctionOrDisjunction647 = new BitSet(new long[]{0x00000018E6000002L});
    public static final BitSet FOLLOW_ftsFieldGroupExplictConjunction_in_ftsFieldGroupExplicitDisjunction672 = new BitSet(new long[]{0x0000000000800002L});
    public static final BitSet FOLLOW_OR_in_ftsFieldGroupExplicitDisjunction675 = new BitSet(new long[]{0x00000018E6800000L});
    public static final BitSet FOLLOW_ftsFieldGroupExplictConjunction_in_ftsFieldGroupExplicitDisjunction677 = new BitSet(new long[]{0x0000000000800002L});
    public static final BitSet FOLLOW_ftsFieldGroupNot_in_ftsFieldGroupExplictConjunction702 = new BitSet(new long[]{0x0000000001000002L});
    public static final BitSet FOLLOW_AND_in_ftsFieldGroupExplictConjunction705 = new BitSet(new long[]{0x00000018E7000000L});
    public static final BitSet FOLLOW_ftsFieldGroupNot_in_ftsFieldGroupExplictConjunction707 = new BitSet(new long[]{0x0000000001000002L});
    public static final BitSet FOLLOW_MINUS_in_ftsFieldGroupNot734 = new BitSet(new long[]{0x00000018E6000000L});
    public static final BitSet FOLLOW_ftsFieldGroupTest_in_ftsFieldGroupNot736 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ftsFieldGroupTest_in_ftsFieldGroupNot751 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ftsFieldGroupTerm_in_ftsFieldGroupTest770 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ftsFieldGroupExactTerm_in_ftsFieldGroupTest785 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ftsFieldGroupPhrase_in_ftsFieldGroupTest801 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ftsFieldGroupSynonym_in_ftsFieldGroupTest816 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ftsFieldGroupProximity_in_ftsFieldGroupTest833 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ftsFieldGroupRange_in_ftsFieldGroupTest854 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_ftsFieldGroupTest875 = new BitSet(new long[]{0x00000018EE000000L});
    public static final BitSet FOLLOW_ftsFieldGroupImplicitConjunctionOrDisjunction_in_ftsFieldGroupTest877 = new BitSet(new long[]{0x0000000008000000L});
    public static final BitSet FOLLOW_RPAREN_in_ftsFieldGroupTest879 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ftsWord_in_ftsFieldGroupTerm897 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_PLUS_in_ftsFieldGroupExactTerm909 = new BitSet(new long[]{0x0000001800000000L});
    public static final BitSet FOLLOW_ftsFieldGroupTerm_in_ftsFieldGroupExactTerm911 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FTSPHRASE_in_ftsFieldGroupPhrase931 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TILDA_in_ftsFieldGroupSynonym943 = new BitSet(new long[]{0x0000001800000000L});
    public static final BitSet FOLLOW_ftsFieldGroupTerm_in_ftsFieldGroupSynonym945 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ftsFieldGroupTerm_in_ftsFieldGroupProximity963 = new BitSet(new long[]{0x0000000100000000L});
    public static final BitSet FOLLOW_STAR_in_ftsFieldGroupProximity965 = new BitSet(new long[]{0x0000001800000000L});
    public static final BitSet FOLLOW_ftsFieldGroupTerm_in_ftsFieldGroupProximity967 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ftsFieldGroupTerm_in_ftsFieldGroupRange985 = new BitSet(new long[]{0x0000000200000000L});
    public static final BitSet FOLLOW_DOTDOT_in_ftsFieldGroupRange987 = new BitSet(new long[]{0x0000001800000000L});
    public static final BitSet FOLLOW_ftsFieldGroupTerm_in_ftsFieldGroupRange989 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_identifier_in_columnReference1013 = new BitSet(new long[]{0x0000000400000000L});
    public static final BitSet FOLLOW_DOT_in_columnReference1015 = new BitSet(new long[]{0x0000000800000000L});
    public static final BitSet FOLLOW_identifier_in_columnReference1022 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_identifier1050 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_ftsWord0 = new BitSet(new long[]{0x0000000000000002L});

}