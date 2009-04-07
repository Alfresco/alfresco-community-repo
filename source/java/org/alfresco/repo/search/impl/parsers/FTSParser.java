// $ANTLR 3.1.2 W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g 2009-04-06 14:56:16
package org.alfresco.repo.search.impl.parsers;

import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import org.antlr.runtime.tree.*;

public class FTSParser extends Parser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "FTS", "DISJUNCTION", "CONJUNCTION", "NEGATION", "TERM", "EXACT_TERM", "PHRASE", "SYNONYM", "DEFAULT", "MANDATORY", "OPTIONAL", "EXCLUDE", "FIELD_DISJUNCTION", "FIELD_CONJUNCTION", "FIELD_NEGATION", "FIELD_GROUP", "FIELD_DEFAULT", "FIELD_MANDATORY", "FIELD_OPTIONAL", "FIELD_EXCLUDE", "FG_TERM", "FG_EXACT_TERM", "FG_PHRASE", "FG_SYNONYM", "FG_PROXIMITY", "FG_RANGE", "COLUMN_REF", "INCLUSIVE", "EXCLUSIVE", "PLUS", "BAR", "MINUS", "LPAREN", "RPAREN", "COLON", "EQUALS", "FTSPHRASE", "TILDA", "STAR", "DOTDOT", "TO", "LCURL", "RCURL", "DOT", "ID", "FTSWORD", "OR", "AND", "NOT", "AMP", "EXCLAMATION", "F_ESC", "QUESTION_MARK", "LSQUARE", "RSQUARE", "COMMA", "CARAT", "DOLLAR", "INWORD", "F_HEX", "WS"
    };
    public static final int DOLLAR=61;
    public static final int TERM=8;
    public static final int STAR=42;
    public static final int LSQUARE=57;
    public static final int AMP=53;
    public static final int FG_PROXIMITY=28;
    public static final int FG_TERM=24;
    public static final int EXACT_TERM=9;
    public static final int FIELD_DISJUNCTION=16;
    public static final int EQUALS=39;
    public static final int DOTDOT=43;
    public static final int NOT=52;
    public static final int MANDATORY=13;
    public static final int FG_EXACT_TERM=25;
    public static final int FIELD_EXCLUDE=23;
    public static final int EXCLUSIVE=32;
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
    public static final int FTSWORD=49;
    public static final int PHRASE=10;
    public static final int OPTIONAL=14;
    public static final int COLON=38;
    public static final int DISJUNCTION=5;
    public static final int LCURL=45;
    public static final int FTS=4;
    public static final int WS=64;
    public static final int FG_SYNONYM=27;
    public static final int NEGATION=7;
    public static final int FTSPHRASE=40;
    public static final int FIELD_CONJUNCTION=17;
    public static final int INCLUSIVE=31;
    public static final int OR=50;
    public static final int RCURL=46;
    public static final int FIELD_MANDATORY=21;
    public static final int F_HEX=63;
    public static final int FG_RANGE=29;
    public static final int BAR=34;
    public static final int FG_PHRASE=26;
    public static final int FIELD_NEGATION=18;

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
    public String getGrammarFileName() { return "W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g"; }


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
                msg = "No viable alt; token="+e.token+
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


    public static class ftsQuery_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "ftsQuery"
    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:143:1: ftsQuery : ftsImplicitConjunctionOrDisjunction EOF -> ftsImplicitConjunctionOrDisjunction ;
    public final FTSParser.ftsQuery_return ftsQuery() throws RecognitionException {
        FTSParser.ftsQuery_return retval = new FTSParser.ftsQuery_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token EOF2=null;
        FTSParser.ftsImplicitConjunctionOrDisjunction_return ftsImplicitConjunctionOrDisjunction1 = null;


        Object EOF2_tree=null;
        RewriteRuleTokenStream stream_EOF=new RewriteRuleTokenStream(adaptor,"token EOF");
        RewriteRuleSubtreeStream stream_ftsImplicitConjunctionOrDisjunction=new RewriteRuleSubtreeStream(adaptor,"rule ftsImplicitConjunctionOrDisjunction");
        try {
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:144:5: ( ftsImplicitConjunctionOrDisjunction EOF -> ftsImplicitConjunctionOrDisjunction )
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:144:8: ftsImplicitConjunctionOrDisjunction EOF
            {
            pushFollow(FOLLOW_ftsImplicitConjunctionOrDisjunction_in_ftsQuery190);
            ftsImplicitConjunctionOrDisjunction1=ftsImplicitConjunctionOrDisjunction();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_ftsImplicitConjunctionOrDisjunction.add(ftsImplicitConjunctionOrDisjunction1.getTree());
            EOF2=(Token)match(input,EOF,FOLLOW_EOF_in_ftsQuery192); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_EOF.add(EOF2);



            // AST REWRITE
            // elements: ftsImplicitConjunctionOrDisjunction
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 145:3: -> ftsImplicitConjunctionOrDisjunction
            {
                adaptor.addChild(root_0, stream_ftsImplicitConjunctionOrDisjunction.nextTree());

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        catch(RecognitionException e)
        {
           throw e;
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "ftsQuery"

    public static class ftsImplicitConjunctionOrDisjunction_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "ftsImplicitConjunctionOrDisjunction"
    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:148:1: ftsImplicitConjunctionOrDisjunction : ({...}? ftsExplicitDisjunction ( ftsExplicitDisjunction )* -> ^( CONJUNCTION ( ftsExplicitDisjunction )+ ) | ftsExplicitDisjunction ( ftsExplicitDisjunction )* -> ^( DISJUNCTION ( ftsExplicitDisjunction )+ ) );
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
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:149:2: ({...}? ftsExplicitDisjunction ( ftsExplicitDisjunction )* -> ^( CONJUNCTION ( ftsExplicitDisjunction )+ ) | ftsExplicitDisjunction ( ftsExplicitDisjunction )* -> ^( DISJUNCTION ( ftsExplicitDisjunction )+ ) )
            int alt3=2;
            alt3 = dfa3.predict(input);
            switch (alt3) {
                case 1 :
                    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:149:4: {...}? ftsExplicitDisjunction ( ftsExplicitDisjunction )*
                    {
                    if ( !((defaultConjunction())) ) {
                        if (state.backtracking>0) {state.failed=true; return retval;}
                        throw new FailedPredicateException(input, "ftsImplicitConjunctionOrDisjunction", "defaultConjunction()");
                    }
                    pushFollow(FOLLOW_ftsExplicitDisjunction_in_ftsImplicitConjunctionOrDisjunction214);
                    ftsExplicitDisjunction3=ftsExplicitDisjunction();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_ftsExplicitDisjunction.add(ftsExplicitDisjunction3.getTree());
                    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:149:51: ( ftsExplicitDisjunction )*
                    loop1:
                    do {
                        int alt1=2;
                        int LA1_0 = input.LA(1);

                        if ( ((LA1_0>=PLUS && LA1_0<=LPAREN)||(LA1_0>=EQUALS && LA1_0<=TILDA)||(LA1_0>=TO && LA1_0<=LCURL)||(LA1_0>=ID && LA1_0<=NOT)||LA1_0==EXCLAMATION) ) {
                            alt1=1;
                        }


                        switch (alt1) {
                    	case 1 :
                    	    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:149:52: ftsExplicitDisjunction
                    	    {
                    	    pushFollow(FOLLOW_ftsExplicitDisjunction_in_ftsImplicitConjunctionOrDisjunction217);
                    	    ftsExplicitDisjunction4=ftsExplicitDisjunction();

                    	    state._fsp--;
                    	    if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) stream_ftsExplicitDisjunction.add(ftsExplicitDisjunction4.getTree());

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
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 150:3: -> ^( CONJUNCTION ( ftsExplicitDisjunction )+ )
                    {
                        // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:150:6: ^( CONJUNCTION ( ftsExplicitDisjunction )+ )
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

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:151:5: ftsExplicitDisjunction ( ftsExplicitDisjunction )*
                    {
                    pushFollow(FOLLOW_ftsExplicitDisjunction_in_ftsImplicitConjunctionOrDisjunction236);
                    ftsExplicitDisjunction5=ftsExplicitDisjunction();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_ftsExplicitDisjunction.add(ftsExplicitDisjunction5.getTree());
                    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:151:28: ( ftsExplicitDisjunction )*
                    loop2:
                    do {
                        int alt2=2;
                        int LA2_0 = input.LA(1);

                        if ( ((LA2_0>=PLUS && LA2_0<=LPAREN)||(LA2_0>=EQUALS && LA2_0<=TILDA)||(LA2_0>=TO && LA2_0<=LCURL)||(LA2_0>=ID && LA2_0<=NOT)||LA2_0==EXCLAMATION) ) {
                            alt2=1;
                        }


                        switch (alt2) {
                    	case 1 :
                    	    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:151:29: ftsExplicitDisjunction
                    	    {
                    	    pushFollow(FOLLOW_ftsExplicitDisjunction_in_ftsImplicitConjunctionOrDisjunction239);
                    	    ftsExplicitDisjunction6=ftsExplicitDisjunction();

                    	    state._fsp--;
                    	    if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) stream_ftsExplicitDisjunction.add(ftsExplicitDisjunction6.getTree());

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
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 152:3: -> ^( DISJUNCTION ( ftsExplicitDisjunction )+ )
                    {
                        // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:152:6: ^( DISJUNCTION ( ftsExplicitDisjunction )+ )
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

                    retval.tree = root_0;}
                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        catch(RecognitionException e)
        {
           throw e;
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "ftsImplicitConjunctionOrDisjunction"

    public static class ftsExplicitDisjunction_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "ftsExplicitDisjunction"
    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:155:1: ftsExplicitDisjunction : ftsExplictConjunction ( ( or )=> or ftsExplictConjunction )* -> ^( DISJUNCTION ( ftsExplictConjunction )+ ) ;
    public final FTSParser.ftsExplicitDisjunction_return ftsExplicitDisjunction() throws RecognitionException {
        FTSParser.ftsExplicitDisjunction_return retval = new FTSParser.ftsExplicitDisjunction_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        FTSParser.ftsExplictConjunction_return ftsExplictConjunction7 = null;

        FTSParser.or_return or8 = null;

        FTSParser.ftsExplictConjunction_return ftsExplictConjunction9 = null;


        RewriteRuleSubtreeStream stream_or=new RewriteRuleSubtreeStream(adaptor,"rule or");
        RewriteRuleSubtreeStream stream_ftsExplictConjunction=new RewriteRuleSubtreeStream(adaptor,"rule ftsExplictConjunction");
        try {
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:156:2: ( ftsExplictConjunction ( ( or )=> or ftsExplictConjunction )* -> ^( DISJUNCTION ( ftsExplictConjunction )+ ) )
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:156:4: ftsExplictConjunction ( ( or )=> or ftsExplictConjunction )*
            {
            pushFollow(FOLLOW_ftsExplictConjunction_in_ftsExplicitDisjunction264);
            ftsExplictConjunction7=ftsExplictConjunction();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_ftsExplictConjunction.add(ftsExplictConjunction7.getTree());
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:156:26: ( ( or )=> or ftsExplictConjunction )*
            loop4:
            do {
                int alt4=2;
                alt4 = dfa4.predict(input);
                switch (alt4) {
            	case 1 :
            	    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:156:27: ( or )=> or ftsExplictConjunction
            	    {
            	    pushFollow(FOLLOW_or_in_ftsExplicitDisjunction273);
            	    or8=or();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_or.add(or8.getTree());
            	    pushFollow(FOLLOW_ftsExplictConjunction_in_ftsExplicitDisjunction275);
            	    ftsExplictConjunction9=ftsExplictConjunction();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_ftsExplictConjunction.add(ftsExplictConjunction9.getTree());

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
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 157:3: -> ^( DISJUNCTION ( ftsExplictConjunction )+ )
            {
                // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:157:6: ^( DISJUNCTION ( ftsExplictConjunction )+ )
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

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        catch(RecognitionException e)
        {
           throw e;
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "ftsExplicitDisjunction"

    public static class ftsExplictConjunction_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "ftsExplictConjunction"
    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:160:1: ftsExplictConjunction : ftsPrefixed ( ( and )=> and ftsPrefixed )* -> ^( CONJUNCTION ftsPrefixed ) ;
    public final FTSParser.ftsExplictConjunction_return ftsExplictConjunction() throws RecognitionException {
        FTSParser.ftsExplictConjunction_return retval = new FTSParser.ftsExplictConjunction_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        FTSParser.ftsPrefixed_return ftsPrefixed10 = null;

        FTSParser.and_return and11 = null;

        FTSParser.ftsPrefixed_return ftsPrefixed12 = null;


        RewriteRuleSubtreeStream stream_ftsPrefixed=new RewriteRuleSubtreeStream(adaptor,"rule ftsPrefixed");
        RewriteRuleSubtreeStream stream_and=new RewriteRuleSubtreeStream(adaptor,"rule and");
        try {
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:161:2: ( ftsPrefixed ( ( and )=> and ftsPrefixed )* -> ^( CONJUNCTION ftsPrefixed ) )
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:161:4: ftsPrefixed ( ( and )=> and ftsPrefixed )*
            {
            pushFollow(FOLLOW_ftsPrefixed_in_ftsExplictConjunction300);
            ftsPrefixed10=ftsPrefixed();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_ftsPrefixed.add(ftsPrefixed10.getTree());
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:161:16: ( ( and )=> and ftsPrefixed )*
            loop5:
            do {
                int alt5=2;
                alt5 = dfa5.predict(input);
                switch (alt5) {
            	case 1 :
            	    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:161:17: ( and )=> and ftsPrefixed
            	    {
            	    pushFollow(FOLLOW_and_in_ftsExplictConjunction309);
            	    and11=and();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_and.add(and11.getTree());
            	    pushFollow(FOLLOW_ftsPrefixed_in_ftsExplictConjunction311);
            	    ftsPrefixed12=ftsPrefixed();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_ftsPrefixed.add(ftsPrefixed12.getTree());

            	    }
            	    break;

            	default :
            	    break loop5;
                }
            } while (true);



            // AST REWRITE
            // elements: ftsPrefixed
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 162:3: -> ^( CONJUNCTION ftsPrefixed )
            {
                // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:162:6: ^( CONJUNCTION ftsPrefixed )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(CONJUNCTION, "CONJUNCTION"), root_1);

                adaptor.addChild(root_1, stream_ftsPrefixed.nextTree());

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        catch(RecognitionException e)
        {
           throw e;
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "ftsExplictConjunction"

    public static class ftsPrefixed_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "ftsPrefixed"
    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:166:1: ftsPrefixed : ( ( not )=> not ftsTest -> ^( NEGATION ftsTest ) | ftsTest -> ^( DEFAULT ftsTest ) | PLUS ftsTest -> ^( MANDATORY ftsTest ) | BAR ftsTest -> ^( OPTIONAL ftsTest ) | MINUS ftsTest -> ^( EXCLUDE ftsTest ) );
    public final FTSParser.ftsPrefixed_return ftsPrefixed() throws RecognitionException {
        FTSParser.ftsPrefixed_return retval = new FTSParser.ftsPrefixed_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token PLUS16=null;
        Token BAR18=null;
        Token MINUS20=null;
        FTSParser.not_return not13 = null;

        FTSParser.ftsTest_return ftsTest14 = null;

        FTSParser.ftsTest_return ftsTest15 = null;

        FTSParser.ftsTest_return ftsTest17 = null;

        FTSParser.ftsTest_return ftsTest19 = null;

        FTSParser.ftsTest_return ftsTest21 = null;


        Object PLUS16_tree=null;
        Object BAR18_tree=null;
        Object MINUS20_tree=null;
        RewriteRuleTokenStream stream_PLUS=new RewriteRuleTokenStream(adaptor,"token PLUS");
        RewriteRuleTokenStream stream_MINUS=new RewriteRuleTokenStream(adaptor,"token MINUS");
        RewriteRuleTokenStream stream_BAR=new RewriteRuleTokenStream(adaptor,"token BAR");
        RewriteRuleSubtreeStream stream_not=new RewriteRuleSubtreeStream(adaptor,"rule not");
        RewriteRuleSubtreeStream stream_ftsTest=new RewriteRuleSubtreeStream(adaptor,"rule ftsTest");
        try {
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:167:5: ( ( not )=> not ftsTest -> ^( NEGATION ftsTest ) | ftsTest -> ^( DEFAULT ftsTest ) | PLUS ftsTest -> ^( MANDATORY ftsTest ) | BAR ftsTest -> ^( OPTIONAL ftsTest ) | MINUS ftsTest -> ^( EXCLUDE ftsTest ) )
            int alt6=5;
            alt6 = dfa6.predict(input);
            switch (alt6) {
                case 1 :
                    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:167:7: ( not )=> not ftsTest
                    {
                    pushFollow(FOLLOW_not_in_ftsPrefixed349);
                    not13=not();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_not.add(not13.getTree());
                    pushFollow(FOLLOW_ftsTest_in_ftsPrefixed351);
                    ftsTest14=ftsTest();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_ftsTest.add(ftsTest14.getTree());


                    // AST REWRITE
                    // elements: ftsTest
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 168:3: -> ^( NEGATION ftsTest )
                    {
                        // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:168:6: ^( NEGATION ftsTest )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(NEGATION, "NEGATION"), root_1);

                        adaptor.addChild(root_1, stream_ftsTest.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:169:7: ftsTest
                    {
                    pushFollow(FOLLOW_ftsTest_in_ftsPrefixed369);
                    ftsTest15=ftsTest();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_ftsTest.add(ftsTest15.getTree());


                    // AST REWRITE
                    // elements: ftsTest
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 170:3: -> ^( DEFAULT ftsTest )
                    {
                        // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:170:6: ^( DEFAULT ftsTest )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(DEFAULT, "DEFAULT"), root_1);

                        adaptor.addChild(root_1, stream_ftsTest.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 3 :
                    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:171:9: PLUS ftsTest
                    {
                    PLUS16=(Token)match(input,PLUS,FOLLOW_PLUS_in_ftsPrefixed389); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_PLUS.add(PLUS16);

                    pushFollow(FOLLOW_ftsTest_in_ftsPrefixed391);
                    ftsTest17=ftsTest();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_ftsTest.add(ftsTest17.getTree());


                    // AST REWRITE
                    // elements: ftsTest
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 172:17: -> ^( MANDATORY ftsTest )
                    {
                        // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:172:20: ^( MANDATORY ftsTest )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(MANDATORY, "MANDATORY"), root_1);

                        adaptor.addChild(root_1, stream_ftsTest.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 4 :
                    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:173:9: BAR ftsTest
                    {
                    BAR18=(Token)match(input,BAR,FOLLOW_BAR_in_ftsPrefixed425); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_BAR.add(BAR18);

                    pushFollow(FOLLOW_ftsTest_in_ftsPrefixed427);
                    ftsTest19=ftsTest();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_ftsTest.add(ftsTest19.getTree());


                    // AST REWRITE
                    // elements: ftsTest
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 174:17: -> ^( OPTIONAL ftsTest )
                    {
                        // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:174:20: ^( OPTIONAL ftsTest )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(OPTIONAL, "OPTIONAL"), root_1);

                        adaptor.addChild(root_1, stream_ftsTest.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 5 :
                    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:175:9: MINUS ftsTest
                    {
                    MINUS20=(Token)match(input,MINUS,FOLLOW_MINUS_in_ftsPrefixed461); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_MINUS.add(MINUS20);

                    pushFollow(FOLLOW_ftsTest_in_ftsPrefixed463);
                    ftsTest21=ftsTest();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_ftsTest.add(ftsTest21.getTree());


                    // AST REWRITE
                    // elements: ftsTest
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 176:17: -> ^( EXCLUDE ftsTest )
                    {
                        // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:176:20: ^( EXCLUDE ftsTest )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(EXCLUDE, "EXCLUDE"), root_1);

                        adaptor.addChild(root_1, stream_ftsTest.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        catch(RecognitionException e)
        {
           throw e;
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "ftsPrefixed"

    public static class ftsTest_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "ftsTest"
    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:179:1: ftsTest : ( ftsTerm -> ^( TERM ftsTerm ) | ftsExactTerm -> ^( EXACT_TERM ftsExactTerm ) | ftsPhrase -> ^( PHRASE ftsPhrase ) | ftsSynonym -> ^( SYNONYM ftsSynonym ) | ftsFieldGroupProximity -> ^( FG_PROXIMITY ftsFieldGroupProximity ) | ftsFieldGroupRange -> ^( FG_RANGE ftsFieldGroupRange ) | ftsFieldGroup | LPAREN ftsImplicitConjunctionOrDisjunction RPAREN -> ftsImplicitConjunctionOrDisjunction );
    public final FTSParser.ftsTest_return ftsTest() throws RecognitionException {
        FTSParser.ftsTest_return retval = new FTSParser.ftsTest_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token LPAREN29=null;
        Token RPAREN31=null;
        FTSParser.ftsTerm_return ftsTerm22 = null;

        FTSParser.ftsExactTerm_return ftsExactTerm23 = null;

        FTSParser.ftsPhrase_return ftsPhrase24 = null;

        FTSParser.ftsSynonym_return ftsSynonym25 = null;

        FTSParser.ftsFieldGroupProximity_return ftsFieldGroupProximity26 = null;

        FTSParser.ftsFieldGroupRange_return ftsFieldGroupRange27 = null;

        FTSParser.ftsFieldGroup_return ftsFieldGroup28 = null;

        FTSParser.ftsImplicitConjunctionOrDisjunction_return ftsImplicitConjunctionOrDisjunction30 = null;


        Object LPAREN29_tree=null;
        Object RPAREN31_tree=null;
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
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:180:5: ( ftsTerm -> ^( TERM ftsTerm ) | ftsExactTerm -> ^( EXACT_TERM ftsExactTerm ) | ftsPhrase -> ^( PHRASE ftsPhrase ) | ftsSynonym -> ^( SYNONYM ftsSynonym ) | ftsFieldGroupProximity -> ^( FG_PROXIMITY ftsFieldGroupProximity ) | ftsFieldGroupRange -> ^( FG_RANGE ftsFieldGroupRange ) | ftsFieldGroup | LPAREN ftsImplicitConjunctionOrDisjunction RPAREN -> ftsImplicitConjunctionOrDisjunction )
            int alt7=8;
            alt7 = dfa7.predict(input);
            switch (alt7) {
                case 1 :
                    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:180:7: ftsTerm
                    {
                    pushFollow(FOLLOW_ftsTerm_in_ftsTest505);
                    ftsTerm22=ftsTerm();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_ftsTerm.add(ftsTerm22.getTree());


                    // AST REWRITE
                    // elements: ftsTerm
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 181:3: -> ^( TERM ftsTerm )
                    {
                        // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:181:6: ^( TERM ftsTerm )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(TERM, "TERM"), root_1);

                        adaptor.addChild(root_1, stream_ftsTerm.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:182:4: ftsExactTerm
                    {
                    pushFollow(FOLLOW_ftsExactTerm_in_ftsTest520);
                    ftsExactTerm23=ftsExactTerm();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_ftsExactTerm.add(ftsExactTerm23.getTree());


                    // AST REWRITE
                    // elements: ftsExactTerm
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 183:3: -> ^( EXACT_TERM ftsExactTerm )
                    {
                        // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:183:6: ^( EXACT_TERM ftsExactTerm )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(EXACT_TERM, "EXACT_TERM"), root_1);

                        adaptor.addChild(root_1, stream_ftsExactTerm.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 3 :
                    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:184:9: ftsPhrase
                    {
                    pushFollow(FOLLOW_ftsPhrase_in_ftsTest540);
                    ftsPhrase24=ftsPhrase();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_ftsPhrase.add(ftsPhrase24.getTree());


                    // AST REWRITE
                    // elements: ftsPhrase
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 185:9: -> ^( PHRASE ftsPhrase )
                    {
                        // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:185:12: ^( PHRASE ftsPhrase )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(PHRASE, "PHRASE"), root_1);

                        adaptor.addChild(root_1, stream_ftsPhrase.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 4 :
                    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:186:9: ftsSynonym
                    {
                    pushFollow(FOLLOW_ftsSynonym_in_ftsTest566);
                    ftsSynonym25=ftsSynonym();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_ftsSynonym.add(ftsSynonym25.getTree());


                    // AST REWRITE
                    // elements: ftsSynonym
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 187:9: -> ^( SYNONYM ftsSynonym )
                    {
                        // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:187:12: ^( SYNONYM ftsSynonym )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(SYNONYM, "SYNONYM"), root_1);

                        adaptor.addChild(root_1, stream_ftsSynonym.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 5 :
                    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:188:7: ftsFieldGroupProximity
                    {
                    pushFollow(FOLLOW_ftsFieldGroupProximity_in_ftsTest590);
                    ftsFieldGroupProximity26=ftsFieldGroupProximity();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_ftsFieldGroupProximity.add(ftsFieldGroupProximity26.getTree());


                    // AST REWRITE
                    // elements: ftsFieldGroupProximity
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 189:9: -> ^( FG_PROXIMITY ftsFieldGroupProximity )
                    {
                        // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:189:12: ^( FG_PROXIMITY ftsFieldGroupProximity )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(FG_PROXIMITY, "FG_PROXIMITY"), root_1);

                        adaptor.addChild(root_1, stream_ftsFieldGroupProximity.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 6 :
                    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:190:8: ftsFieldGroupRange
                    {
                    pushFollow(FOLLOW_ftsFieldGroupRange_in_ftsTest617);
                    ftsFieldGroupRange27=ftsFieldGroupRange();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_ftsFieldGroupRange.add(ftsFieldGroupRange27.getTree());


                    // AST REWRITE
                    // elements: ftsFieldGroupRange
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 191:9: -> ^( FG_RANGE ftsFieldGroupRange )
                    {
                        // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:191:12: ^( FG_RANGE ftsFieldGroupRange )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(FG_RANGE, "FG_RANGE"), root_1);

                        adaptor.addChild(root_1, stream_ftsFieldGroupRange.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 7 :
                    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:192:7: ftsFieldGroup
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_ftsFieldGroup_in_ftsTest641);
                    ftsFieldGroup28=ftsFieldGroup();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, ftsFieldGroup28.getTree());

                    }
                    break;
                case 8 :
                    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:193:4: LPAREN ftsImplicitConjunctionOrDisjunction RPAREN
                    {
                    LPAREN29=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_ftsTest650); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_LPAREN.add(LPAREN29);

                    pushFollow(FOLLOW_ftsImplicitConjunctionOrDisjunction_in_ftsTest652);
                    ftsImplicitConjunctionOrDisjunction30=ftsImplicitConjunctionOrDisjunction();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_ftsImplicitConjunctionOrDisjunction.add(ftsImplicitConjunctionOrDisjunction30.getTree());
                    RPAREN31=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_ftsTest654); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_RPAREN.add(RPAREN31);



                    // AST REWRITE
                    // elements: ftsImplicitConjunctionOrDisjunction
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 194:3: -> ftsImplicitConjunctionOrDisjunction
                    {
                        adaptor.addChild(root_0, stream_ftsImplicitConjunctionOrDisjunction.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        catch(RecognitionException e)
        {
           throw e;
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "ftsTest"

    public static class ftsTerm_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "ftsTerm"
    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:197:1: ftsTerm : ( columnReference COLON )? ftsWord -> ftsWord ( columnReference )? ;
    public final FTSParser.ftsTerm_return ftsTerm() throws RecognitionException {
        FTSParser.ftsTerm_return retval = new FTSParser.ftsTerm_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token COLON33=null;
        FTSParser.columnReference_return columnReference32 = null;

        FTSParser.ftsWord_return ftsWord34 = null;


        Object COLON33_tree=null;
        RewriteRuleTokenStream stream_COLON=new RewriteRuleTokenStream(adaptor,"token COLON");
        RewriteRuleSubtreeStream stream_columnReference=new RewriteRuleSubtreeStream(adaptor,"rule columnReference");
        RewriteRuleSubtreeStream stream_ftsWord=new RewriteRuleSubtreeStream(adaptor,"rule ftsWord");
        try {
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:198:2: ( ( columnReference COLON )? ftsWord -> ftsWord ( columnReference )? )
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:198:4: ( columnReference COLON )? ftsWord
            {
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:198:4: ( columnReference COLON )?
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
                    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:198:5: columnReference COLON
                    {
                    pushFollow(FOLLOW_columnReference_in_ftsTerm672);
                    columnReference32=columnReference();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_columnReference.add(columnReference32.getTree());
                    COLON33=(Token)match(input,COLON,FOLLOW_COLON_in_ftsTerm674); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_COLON.add(COLON33);


                    }
                    break;

            }

            pushFollow(FOLLOW_ftsWord_in_ftsTerm678);
            ftsWord34=ftsWord();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_ftsWord.add(ftsWord34.getTree());


            // AST REWRITE
            // elements: columnReference, ftsWord
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 199:3: -> ftsWord ( columnReference )?
            {
                adaptor.addChild(root_0, stream_ftsWord.nextTree());
                // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:199:14: ( columnReference )?
                if ( stream_columnReference.hasNext() ) {
                    adaptor.addChild(root_0, stream_columnReference.nextTree());

                }
                stream_columnReference.reset();

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        catch(RecognitionException e)
        {
           throw e;
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "ftsTerm"

    public static class ftsExactTerm_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "ftsExactTerm"
    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:202:1: ftsExactTerm : EQUALS ftsTerm -> ftsTerm ;
    public final FTSParser.ftsExactTerm_return ftsExactTerm() throws RecognitionException {
        FTSParser.ftsExactTerm_return retval = new FTSParser.ftsExactTerm_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token EQUALS35=null;
        FTSParser.ftsTerm_return ftsTerm36 = null;


        Object EQUALS35_tree=null;
        RewriteRuleTokenStream stream_EQUALS=new RewriteRuleTokenStream(adaptor,"token EQUALS");
        RewriteRuleSubtreeStream stream_ftsTerm=new RewriteRuleSubtreeStream(adaptor,"rule ftsTerm");
        try {
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:203:2: ( EQUALS ftsTerm -> ftsTerm )
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:203:4: EQUALS ftsTerm
            {
            EQUALS35=(Token)match(input,EQUALS,FOLLOW_EQUALS_in_ftsExactTerm699); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_EQUALS.add(EQUALS35);

            pushFollow(FOLLOW_ftsTerm_in_ftsExactTerm701);
            ftsTerm36=ftsTerm();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_ftsTerm.add(ftsTerm36.getTree());


            // AST REWRITE
            // elements: ftsTerm
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 204:3: -> ftsTerm
            {
                adaptor.addChild(root_0, stream_ftsTerm.nextTree());

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        catch(RecognitionException e)
        {
           throw e;
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "ftsExactTerm"

    public static class ftsPhrase_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "ftsPhrase"
    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:207:1: ftsPhrase : ( columnReference COLON )? FTSPHRASE -> FTSPHRASE ( columnReference )? ;
    public final FTSParser.ftsPhrase_return ftsPhrase() throws RecognitionException {
        FTSParser.ftsPhrase_return retval = new FTSParser.ftsPhrase_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token COLON38=null;
        Token FTSPHRASE39=null;
        FTSParser.columnReference_return columnReference37 = null;


        Object COLON38_tree=null;
        Object FTSPHRASE39_tree=null;
        RewriteRuleTokenStream stream_COLON=new RewriteRuleTokenStream(adaptor,"token COLON");
        RewriteRuleTokenStream stream_FTSPHRASE=new RewriteRuleTokenStream(adaptor,"token FTSPHRASE");
        RewriteRuleSubtreeStream stream_columnReference=new RewriteRuleSubtreeStream(adaptor,"rule columnReference");
        try {
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:208:2: ( ( columnReference COLON )? FTSPHRASE -> FTSPHRASE ( columnReference )? )
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:208:6: ( columnReference COLON )? FTSPHRASE
            {
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:208:6: ( columnReference COLON )?
            int alt9=2;
            int LA9_0 = input.LA(1);

            if ( (LA9_0==ID) ) {
                alt9=1;
            }
            switch (alt9) {
                case 1 :
                    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:208:7: columnReference COLON
                    {
                    pushFollow(FOLLOW_columnReference_in_ftsPhrase722);
                    columnReference37=columnReference();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_columnReference.add(columnReference37.getTree());
                    COLON38=(Token)match(input,COLON,FOLLOW_COLON_in_ftsPhrase724); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_COLON.add(COLON38);


                    }
                    break;

            }

            FTSPHRASE39=(Token)match(input,FTSPHRASE,FOLLOW_FTSPHRASE_in_ftsPhrase728); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_FTSPHRASE.add(FTSPHRASE39);



            // AST REWRITE
            // elements: columnReference, FTSPHRASE
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 209:3: -> FTSPHRASE ( columnReference )?
            {
                adaptor.addChild(root_0, stream_FTSPHRASE.nextNode());
                // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:209:16: ( columnReference )?
                if ( stream_columnReference.hasNext() ) {
                    adaptor.addChild(root_0, stream_columnReference.nextTree());

                }
                stream_columnReference.reset();

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        catch(RecognitionException e)
        {
           throw e;
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "ftsPhrase"

    public static class ftsSynonym_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "ftsSynonym"
    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:212:1: ftsSynonym : TILDA ftsTerm -> ftsTerm ;
    public final FTSParser.ftsSynonym_return ftsSynonym() throws RecognitionException {
        FTSParser.ftsSynonym_return retval = new FTSParser.ftsSynonym_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token TILDA40=null;
        FTSParser.ftsTerm_return ftsTerm41 = null;


        Object TILDA40_tree=null;
        RewriteRuleTokenStream stream_TILDA=new RewriteRuleTokenStream(adaptor,"token TILDA");
        RewriteRuleSubtreeStream stream_ftsTerm=new RewriteRuleSubtreeStream(adaptor,"rule ftsTerm");
        try {
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:213:2: ( TILDA ftsTerm -> ftsTerm )
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:213:4: TILDA ftsTerm
            {
            TILDA40=(Token)match(input,TILDA,FOLLOW_TILDA_in_ftsSynonym749); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_TILDA.add(TILDA40);

            pushFollow(FOLLOW_ftsTerm_in_ftsSynonym751);
            ftsTerm41=ftsTerm();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_ftsTerm.add(ftsTerm41.getTree());


            // AST REWRITE
            // elements: ftsTerm
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 214:3: -> ftsTerm
            {
                adaptor.addChild(root_0, stream_ftsTerm.nextTree());

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        catch(RecognitionException e)
        {
           throw e;
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "ftsSynonym"

    public static class ftsFieldGroup_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "ftsFieldGroup"
    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:218:1: ftsFieldGroup : columnReference COLON LPAREN ftsFieldGroupImplicitConjunctionOrDisjunction RPAREN -> ^( FIELD_GROUP columnReference ftsFieldGroupImplicitConjunctionOrDisjunction ) ;
    public final FTSParser.ftsFieldGroup_return ftsFieldGroup() throws RecognitionException {
        FTSParser.ftsFieldGroup_return retval = new FTSParser.ftsFieldGroup_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token COLON43=null;
        Token LPAREN44=null;
        Token RPAREN46=null;
        FTSParser.columnReference_return columnReference42 = null;

        FTSParser.ftsFieldGroupImplicitConjunctionOrDisjunction_return ftsFieldGroupImplicitConjunctionOrDisjunction45 = null;


        Object COLON43_tree=null;
        Object LPAREN44_tree=null;
        Object RPAREN46_tree=null;
        RewriteRuleTokenStream stream_COLON=new RewriteRuleTokenStream(adaptor,"token COLON");
        RewriteRuleTokenStream stream_RPAREN=new RewriteRuleTokenStream(adaptor,"token RPAREN");
        RewriteRuleTokenStream stream_LPAREN=new RewriteRuleTokenStream(adaptor,"token LPAREN");
        RewriteRuleSubtreeStream stream_columnReference=new RewriteRuleSubtreeStream(adaptor,"rule columnReference");
        RewriteRuleSubtreeStream stream_ftsFieldGroupImplicitConjunctionOrDisjunction=new RewriteRuleSubtreeStream(adaptor,"rule ftsFieldGroupImplicitConjunctionOrDisjunction");
        try {
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:219:2: ( columnReference COLON LPAREN ftsFieldGroupImplicitConjunctionOrDisjunction RPAREN -> ^( FIELD_GROUP columnReference ftsFieldGroupImplicitConjunctionOrDisjunction ) )
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:219:4: columnReference COLON LPAREN ftsFieldGroupImplicitConjunctionOrDisjunction RPAREN
            {
            pushFollow(FOLLOW_columnReference_in_ftsFieldGroup770);
            columnReference42=columnReference();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_columnReference.add(columnReference42.getTree());
            COLON43=(Token)match(input,COLON,FOLLOW_COLON_in_ftsFieldGroup772); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_COLON.add(COLON43);

            LPAREN44=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_ftsFieldGroup774); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_LPAREN.add(LPAREN44);

            pushFollow(FOLLOW_ftsFieldGroupImplicitConjunctionOrDisjunction_in_ftsFieldGroup776);
            ftsFieldGroupImplicitConjunctionOrDisjunction45=ftsFieldGroupImplicitConjunctionOrDisjunction();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_ftsFieldGroupImplicitConjunctionOrDisjunction.add(ftsFieldGroupImplicitConjunctionOrDisjunction45.getTree());
            RPAREN46=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_ftsFieldGroup778); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_RPAREN.add(RPAREN46);



            // AST REWRITE
            // elements: columnReference, ftsFieldGroupImplicitConjunctionOrDisjunction
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 220:3: -> ^( FIELD_GROUP columnReference ftsFieldGroupImplicitConjunctionOrDisjunction )
            {
                // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:220:6: ^( FIELD_GROUP columnReference ftsFieldGroupImplicitConjunctionOrDisjunction )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(FIELD_GROUP, "FIELD_GROUP"), root_1);

                adaptor.addChild(root_1, stream_columnReference.nextTree());
                adaptor.addChild(root_1, stream_ftsFieldGroupImplicitConjunctionOrDisjunction.nextTree());

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        catch(RecognitionException e)
        {
           throw e;
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "ftsFieldGroup"

    public static class ftsFieldGroupImplicitConjunctionOrDisjunction_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "ftsFieldGroupImplicitConjunctionOrDisjunction"
    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:223:1: ftsFieldGroupImplicitConjunctionOrDisjunction : ({...}? ftsFieldGroupExplicitDisjunction ( ftsFieldGroupExplicitDisjunction )* -> ^( FIELD_CONJUNCTION ( ftsFieldGroupExplicitDisjunction )+ ) | ftsFieldGroupExplicitDisjunction ( ftsFieldGroupExplicitDisjunction )* -> ^( FIELD_DISJUNCTION ( ftsFieldGroupExplicitDisjunction )+ ) );
    public final FTSParser.ftsFieldGroupImplicitConjunctionOrDisjunction_return ftsFieldGroupImplicitConjunctionOrDisjunction() throws RecognitionException {
        FTSParser.ftsFieldGroupImplicitConjunctionOrDisjunction_return retval = new FTSParser.ftsFieldGroupImplicitConjunctionOrDisjunction_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        FTSParser.ftsFieldGroupExplicitDisjunction_return ftsFieldGroupExplicitDisjunction47 = null;

        FTSParser.ftsFieldGroupExplicitDisjunction_return ftsFieldGroupExplicitDisjunction48 = null;

        FTSParser.ftsFieldGroupExplicitDisjunction_return ftsFieldGroupExplicitDisjunction49 = null;

        FTSParser.ftsFieldGroupExplicitDisjunction_return ftsFieldGroupExplicitDisjunction50 = null;


        RewriteRuleSubtreeStream stream_ftsFieldGroupExplicitDisjunction=new RewriteRuleSubtreeStream(adaptor,"rule ftsFieldGroupExplicitDisjunction");
        try {
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:224:2: ({...}? ftsFieldGroupExplicitDisjunction ( ftsFieldGroupExplicitDisjunction )* -> ^( FIELD_CONJUNCTION ( ftsFieldGroupExplicitDisjunction )+ ) | ftsFieldGroupExplicitDisjunction ( ftsFieldGroupExplicitDisjunction )* -> ^( FIELD_DISJUNCTION ( ftsFieldGroupExplicitDisjunction )+ ) )
            int alt12=2;
            alt12 = dfa12.predict(input);
            switch (alt12) {
                case 1 :
                    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:224:4: {...}? ftsFieldGroupExplicitDisjunction ( ftsFieldGroupExplicitDisjunction )*
                    {
                    if ( !((defaultFieldConjunction())) ) {
                        if (state.backtracking>0) {state.failed=true; return retval;}
                        throw new FailedPredicateException(input, "ftsFieldGroupImplicitConjunctionOrDisjunction", "defaultFieldConjunction()");
                    }
                    pushFollow(FOLLOW_ftsFieldGroupExplicitDisjunction_in_ftsFieldGroupImplicitConjunctionOrDisjunction804);
                    ftsFieldGroupExplicitDisjunction47=ftsFieldGroupExplicitDisjunction();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_ftsFieldGroupExplicitDisjunction.add(ftsFieldGroupExplicitDisjunction47.getTree());
                    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:224:66: ( ftsFieldGroupExplicitDisjunction )*
                    loop10:
                    do {
                        int alt10=2;
                        int LA10_0 = input.LA(1);

                        if ( ((LA10_0>=PLUS && LA10_0<=LPAREN)||(LA10_0>=EQUALS && LA10_0<=TILDA)||(LA10_0>=TO && LA10_0<=LCURL)||(LA10_0>=ID && LA10_0<=NOT)||LA10_0==EXCLAMATION) ) {
                            alt10=1;
                        }


                        switch (alt10) {
                    	case 1 :
                    	    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:224:67: ftsFieldGroupExplicitDisjunction
                    	    {
                    	    pushFollow(FOLLOW_ftsFieldGroupExplicitDisjunction_in_ftsFieldGroupImplicitConjunctionOrDisjunction807);
                    	    ftsFieldGroupExplicitDisjunction48=ftsFieldGroupExplicitDisjunction();

                    	    state._fsp--;
                    	    if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) stream_ftsFieldGroupExplicitDisjunction.add(ftsFieldGroupExplicitDisjunction48.getTree());

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
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 225:3: -> ^( FIELD_CONJUNCTION ( ftsFieldGroupExplicitDisjunction )+ )
                    {
                        // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:225:6: ^( FIELD_CONJUNCTION ( ftsFieldGroupExplicitDisjunction )+ )
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

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:226:4: ftsFieldGroupExplicitDisjunction ( ftsFieldGroupExplicitDisjunction )*
                    {
                    pushFollow(FOLLOW_ftsFieldGroupExplicitDisjunction_in_ftsFieldGroupImplicitConjunctionOrDisjunction825);
                    ftsFieldGroupExplicitDisjunction49=ftsFieldGroupExplicitDisjunction();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_ftsFieldGroupExplicitDisjunction.add(ftsFieldGroupExplicitDisjunction49.getTree());
                    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:226:37: ( ftsFieldGroupExplicitDisjunction )*
                    loop11:
                    do {
                        int alt11=2;
                        int LA11_0 = input.LA(1);

                        if ( ((LA11_0>=PLUS && LA11_0<=LPAREN)||(LA11_0>=EQUALS && LA11_0<=TILDA)||(LA11_0>=TO && LA11_0<=LCURL)||(LA11_0>=ID && LA11_0<=NOT)||LA11_0==EXCLAMATION) ) {
                            alt11=1;
                        }


                        switch (alt11) {
                    	case 1 :
                    	    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:226:38: ftsFieldGroupExplicitDisjunction
                    	    {
                    	    pushFollow(FOLLOW_ftsFieldGroupExplicitDisjunction_in_ftsFieldGroupImplicitConjunctionOrDisjunction828);
                    	    ftsFieldGroupExplicitDisjunction50=ftsFieldGroupExplicitDisjunction();

                    	    state._fsp--;
                    	    if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) stream_ftsFieldGroupExplicitDisjunction.add(ftsFieldGroupExplicitDisjunction50.getTree());

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
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 227:3: -> ^( FIELD_DISJUNCTION ( ftsFieldGroupExplicitDisjunction )+ )
                    {
                        // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:227:6: ^( FIELD_DISJUNCTION ( ftsFieldGroupExplicitDisjunction )+ )
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

                    retval.tree = root_0;}
                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        catch(RecognitionException e)
        {
           throw e;
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "ftsFieldGroupImplicitConjunctionOrDisjunction"

    public static class ftsFieldGroupExplicitDisjunction_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "ftsFieldGroupExplicitDisjunction"
    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:230:1: ftsFieldGroupExplicitDisjunction : ftsFieldGroupExplictConjunction ( ( or )=> or ftsFieldGroupExplictConjunction )* -> ^( FIELD_DISJUNCTION ( ftsFieldGroupExplictConjunction )+ ) ;
    public final FTSParser.ftsFieldGroupExplicitDisjunction_return ftsFieldGroupExplicitDisjunction() throws RecognitionException {
        FTSParser.ftsFieldGroupExplicitDisjunction_return retval = new FTSParser.ftsFieldGroupExplicitDisjunction_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        FTSParser.ftsFieldGroupExplictConjunction_return ftsFieldGroupExplictConjunction51 = null;

        FTSParser.or_return or52 = null;

        FTSParser.ftsFieldGroupExplictConjunction_return ftsFieldGroupExplictConjunction53 = null;


        RewriteRuleSubtreeStream stream_ftsFieldGroupExplictConjunction=new RewriteRuleSubtreeStream(adaptor,"rule ftsFieldGroupExplictConjunction");
        RewriteRuleSubtreeStream stream_or=new RewriteRuleSubtreeStream(adaptor,"rule or");
        try {
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:231:2: ( ftsFieldGroupExplictConjunction ( ( or )=> or ftsFieldGroupExplictConjunction )* -> ^( FIELD_DISJUNCTION ( ftsFieldGroupExplictConjunction )+ ) )
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:231:4: ftsFieldGroupExplictConjunction ( ( or )=> or ftsFieldGroupExplictConjunction )*
            {
            pushFollow(FOLLOW_ftsFieldGroupExplictConjunction_in_ftsFieldGroupExplicitDisjunction853);
            ftsFieldGroupExplictConjunction51=ftsFieldGroupExplictConjunction();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_ftsFieldGroupExplictConjunction.add(ftsFieldGroupExplictConjunction51.getTree());
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:231:36: ( ( or )=> or ftsFieldGroupExplictConjunction )*
            loop13:
            do {
                int alt13=2;
                alt13 = dfa13.predict(input);
                switch (alt13) {
            	case 1 :
            	    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:231:37: ( or )=> or ftsFieldGroupExplictConjunction
            	    {
            	    pushFollow(FOLLOW_or_in_ftsFieldGroupExplicitDisjunction862);
            	    or52=or();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_or.add(or52.getTree());
            	    pushFollow(FOLLOW_ftsFieldGroupExplictConjunction_in_ftsFieldGroupExplicitDisjunction864);
            	    ftsFieldGroupExplictConjunction53=ftsFieldGroupExplictConjunction();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_ftsFieldGroupExplictConjunction.add(ftsFieldGroupExplictConjunction53.getTree());

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
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 232:3: -> ^( FIELD_DISJUNCTION ( ftsFieldGroupExplictConjunction )+ )
            {
                // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:232:6: ^( FIELD_DISJUNCTION ( ftsFieldGroupExplictConjunction )+ )
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

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        catch(RecognitionException e)
        {
           throw e;
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "ftsFieldGroupExplicitDisjunction"

    public static class ftsFieldGroupExplictConjunction_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "ftsFieldGroupExplictConjunction"
    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:235:1: ftsFieldGroupExplictConjunction : ftsFieldGroupPrefixed ( ( and )=> and ftsFieldGroupPrefixed )* -> ^( FIELD_CONJUNCTION ( ftsFieldGroupPrefixed )+ ) ;
    public final FTSParser.ftsFieldGroupExplictConjunction_return ftsFieldGroupExplictConjunction() throws RecognitionException {
        FTSParser.ftsFieldGroupExplictConjunction_return retval = new FTSParser.ftsFieldGroupExplictConjunction_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        FTSParser.ftsFieldGroupPrefixed_return ftsFieldGroupPrefixed54 = null;

        FTSParser.and_return and55 = null;

        FTSParser.ftsFieldGroupPrefixed_return ftsFieldGroupPrefixed56 = null;


        RewriteRuleSubtreeStream stream_ftsFieldGroupPrefixed=new RewriteRuleSubtreeStream(adaptor,"rule ftsFieldGroupPrefixed");
        RewriteRuleSubtreeStream stream_and=new RewriteRuleSubtreeStream(adaptor,"rule and");
        try {
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:236:2: ( ftsFieldGroupPrefixed ( ( and )=> and ftsFieldGroupPrefixed )* -> ^( FIELD_CONJUNCTION ( ftsFieldGroupPrefixed )+ ) )
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:236:4: ftsFieldGroupPrefixed ( ( and )=> and ftsFieldGroupPrefixed )*
            {
            pushFollow(FOLLOW_ftsFieldGroupPrefixed_in_ftsFieldGroupExplictConjunction889);
            ftsFieldGroupPrefixed54=ftsFieldGroupPrefixed();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_ftsFieldGroupPrefixed.add(ftsFieldGroupPrefixed54.getTree());
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:236:26: ( ( and )=> and ftsFieldGroupPrefixed )*
            loop14:
            do {
                int alt14=2;
                alt14 = dfa14.predict(input);
                switch (alt14) {
            	case 1 :
            	    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:236:27: ( and )=> and ftsFieldGroupPrefixed
            	    {
            	    pushFollow(FOLLOW_and_in_ftsFieldGroupExplictConjunction898);
            	    and55=and();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_and.add(and55.getTree());
            	    pushFollow(FOLLOW_ftsFieldGroupPrefixed_in_ftsFieldGroupExplictConjunction900);
            	    ftsFieldGroupPrefixed56=ftsFieldGroupPrefixed();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_ftsFieldGroupPrefixed.add(ftsFieldGroupPrefixed56.getTree());

            	    }
            	    break;

            	default :
            	    break loop14;
                }
            } while (true);



            // AST REWRITE
            // elements: ftsFieldGroupPrefixed
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 237:3: -> ^( FIELD_CONJUNCTION ( ftsFieldGroupPrefixed )+ )
            {
                // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:237:6: ^( FIELD_CONJUNCTION ( ftsFieldGroupPrefixed )+ )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(FIELD_CONJUNCTION, "FIELD_CONJUNCTION"), root_1);

                if ( !(stream_ftsFieldGroupPrefixed.hasNext()) ) {
                    throw new RewriteEarlyExitException();
                }
                while ( stream_ftsFieldGroupPrefixed.hasNext() ) {
                    adaptor.addChild(root_1, stream_ftsFieldGroupPrefixed.nextTree());

                }
                stream_ftsFieldGroupPrefixed.reset();

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        catch(RecognitionException e)
        {
           throw e;
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "ftsFieldGroupExplictConjunction"

    public static class ftsFieldGroupPrefixed_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "ftsFieldGroupPrefixed"
    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:241:1: ftsFieldGroupPrefixed : ( ( not )=> not ftsFieldGroupTest -> ^( FIELD_NEGATION ftsFieldGroupTest ) | ftsFieldGroupTest -> ^( FIELD_DEFAULT ftsFieldGroupTest ) | PLUS ftsFieldGroupTest -> ^( FIELD_MANDATORY ftsFieldGroupTest ) | BAR ftsFieldGroupTest -> ^( FIELD_OPTIONAL ftsFieldGroupTest ) | MINUS ftsFieldGroupTest -> ^( FIELD_EXCLUDE ftsFieldGroupTest ) );
    public final FTSParser.ftsFieldGroupPrefixed_return ftsFieldGroupPrefixed() throws RecognitionException {
        FTSParser.ftsFieldGroupPrefixed_return retval = new FTSParser.ftsFieldGroupPrefixed_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token PLUS60=null;
        Token BAR62=null;
        Token MINUS64=null;
        FTSParser.not_return not57 = null;

        FTSParser.ftsFieldGroupTest_return ftsFieldGroupTest58 = null;

        FTSParser.ftsFieldGroupTest_return ftsFieldGroupTest59 = null;

        FTSParser.ftsFieldGroupTest_return ftsFieldGroupTest61 = null;

        FTSParser.ftsFieldGroupTest_return ftsFieldGroupTest63 = null;

        FTSParser.ftsFieldGroupTest_return ftsFieldGroupTest65 = null;


        Object PLUS60_tree=null;
        Object BAR62_tree=null;
        Object MINUS64_tree=null;
        RewriteRuleTokenStream stream_PLUS=new RewriteRuleTokenStream(adaptor,"token PLUS");
        RewriteRuleTokenStream stream_MINUS=new RewriteRuleTokenStream(adaptor,"token MINUS");
        RewriteRuleTokenStream stream_BAR=new RewriteRuleTokenStream(adaptor,"token BAR");
        RewriteRuleSubtreeStream stream_not=new RewriteRuleSubtreeStream(adaptor,"rule not");
        RewriteRuleSubtreeStream stream_ftsFieldGroupTest=new RewriteRuleSubtreeStream(adaptor,"rule ftsFieldGroupTest");
        try {
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:242:1: ( ( not )=> not ftsFieldGroupTest -> ^( FIELD_NEGATION ftsFieldGroupTest ) | ftsFieldGroupTest -> ^( FIELD_DEFAULT ftsFieldGroupTest ) | PLUS ftsFieldGroupTest -> ^( FIELD_MANDATORY ftsFieldGroupTest ) | BAR ftsFieldGroupTest -> ^( FIELD_OPTIONAL ftsFieldGroupTest ) | MINUS ftsFieldGroupTest -> ^( FIELD_EXCLUDE ftsFieldGroupTest ) )
            int alt15=5;
            alt15 = dfa15.predict(input);
            switch (alt15) {
                case 1 :
                    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:242:3: ( not )=> not ftsFieldGroupTest
                    {
                    pushFollow(FOLLOW_not_in_ftsFieldGroupPrefixed934);
                    not57=not();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_not.add(not57.getTree());
                    pushFollow(FOLLOW_ftsFieldGroupTest_in_ftsFieldGroupPrefixed936);
                    ftsFieldGroupTest58=ftsFieldGroupTest();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_ftsFieldGroupTest.add(ftsFieldGroupTest58.getTree());


                    // AST REWRITE
                    // elements: ftsFieldGroupTest
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 243:3: -> ^( FIELD_NEGATION ftsFieldGroupTest )
                    {
                        // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:243:6: ^( FIELD_NEGATION ftsFieldGroupTest )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(FIELD_NEGATION, "FIELD_NEGATION"), root_1);

                        adaptor.addChild(root_1, stream_ftsFieldGroupTest.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:244:7: ftsFieldGroupTest
                    {
                    pushFollow(FOLLOW_ftsFieldGroupTest_in_ftsFieldGroupPrefixed954);
                    ftsFieldGroupTest59=ftsFieldGroupTest();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_ftsFieldGroupTest.add(ftsFieldGroupTest59.getTree());


                    // AST REWRITE
                    // elements: ftsFieldGroupTest
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 245:3: -> ^( FIELD_DEFAULT ftsFieldGroupTest )
                    {
                        // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:245:6: ^( FIELD_DEFAULT ftsFieldGroupTest )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(FIELD_DEFAULT, "FIELD_DEFAULT"), root_1);

                        adaptor.addChild(root_1, stream_ftsFieldGroupTest.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 3 :
                    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:246:9: PLUS ftsFieldGroupTest
                    {
                    PLUS60=(Token)match(input,PLUS,FOLLOW_PLUS_in_ftsFieldGroupPrefixed974); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_PLUS.add(PLUS60);

                    pushFollow(FOLLOW_ftsFieldGroupTest_in_ftsFieldGroupPrefixed976);
                    ftsFieldGroupTest61=ftsFieldGroupTest();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_ftsFieldGroupTest.add(ftsFieldGroupTest61.getTree());


                    // AST REWRITE
                    // elements: ftsFieldGroupTest
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 247:17: -> ^( FIELD_MANDATORY ftsFieldGroupTest )
                    {
                        // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:247:20: ^( FIELD_MANDATORY ftsFieldGroupTest )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(FIELD_MANDATORY, "FIELD_MANDATORY"), root_1);

                        adaptor.addChild(root_1, stream_ftsFieldGroupTest.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 4 :
                    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:248:9: BAR ftsFieldGroupTest
                    {
                    BAR62=(Token)match(input,BAR,FOLLOW_BAR_in_ftsFieldGroupPrefixed1010); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_BAR.add(BAR62);

                    pushFollow(FOLLOW_ftsFieldGroupTest_in_ftsFieldGroupPrefixed1012);
                    ftsFieldGroupTest63=ftsFieldGroupTest();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_ftsFieldGroupTest.add(ftsFieldGroupTest63.getTree());


                    // AST REWRITE
                    // elements: ftsFieldGroupTest
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 249:17: -> ^( FIELD_OPTIONAL ftsFieldGroupTest )
                    {
                        // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:249:20: ^( FIELD_OPTIONAL ftsFieldGroupTest )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(FIELD_OPTIONAL, "FIELD_OPTIONAL"), root_1);

                        adaptor.addChild(root_1, stream_ftsFieldGroupTest.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 5 :
                    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:250:9: MINUS ftsFieldGroupTest
                    {
                    MINUS64=(Token)match(input,MINUS,FOLLOW_MINUS_in_ftsFieldGroupPrefixed1046); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_MINUS.add(MINUS64);

                    pushFollow(FOLLOW_ftsFieldGroupTest_in_ftsFieldGroupPrefixed1048);
                    ftsFieldGroupTest65=ftsFieldGroupTest();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_ftsFieldGroupTest.add(ftsFieldGroupTest65.getTree());


                    // AST REWRITE
                    // elements: ftsFieldGroupTest
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 251:17: -> ^( FIELD_EXCLUDE ftsFieldGroupTest )
                    {
                        // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:251:20: ^( FIELD_EXCLUDE ftsFieldGroupTest )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(FIELD_EXCLUDE, "FIELD_EXCLUDE"), root_1);

                        adaptor.addChild(root_1, stream_ftsFieldGroupTest.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        catch(RecognitionException e)
        {
           throw e;
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "ftsFieldGroupPrefixed"

    public static class ftsFieldGroupTest_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "ftsFieldGroupTest"
    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:255:1: ftsFieldGroupTest : ( ftsFieldGroupTerm -> ^( FG_TERM ftsFieldGroupTerm ) | ftsFieldGroupExactTerm -> ^( FG_EXACT_TERM ftsFieldGroupExactTerm ) | ftsFieldGroupPhrase -> ^( FG_PHRASE ftsFieldGroupPhrase ) | ftsFieldGroupSynonym -> ^( FG_SYNONYM ftsFieldGroupSynonym ) | ftsFieldGroupProximity -> ^( FG_PROXIMITY ftsFieldGroupProximity ) | ftsFieldGroupRange -> ^( FG_RANGE ftsFieldGroupRange ) | LPAREN ftsFieldGroupImplicitConjunctionOrDisjunction RPAREN -> ftsFieldGroupImplicitConjunctionOrDisjunction );
    public final FTSParser.ftsFieldGroupTest_return ftsFieldGroupTest() throws RecognitionException {
        FTSParser.ftsFieldGroupTest_return retval = new FTSParser.ftsFieldGroupTest_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token LPAREN72=null;
        Token RPAREN74=null;
        FTSParser.ftsFieldGroupTerm_return ftsFieldGroupTerm66 = null;

        FTSParser.ftsFieldGroupExactTerm_return ftsFieldGroupExactTerm67 = null;

        FTSParser.ftsFieldGroupPhrase_return ftsFieldGroupPhrase68 = null;

        FTSParser.ftsFieldGroupSynonym_return ftsFieldGroupSynonym69 = null;

        FTSParser.ftsFieldGroupProximity_return ftsFieldGroupProximity70 = null;

        FTSParser.ftsFieldGroupRange_return ftsFieldGroupRange71 = null;

        FTSParser.ftsFieldGroupImplicitConjunctionOrDisjunction_return ftsFieldGroupImplicitConjunctionOrDisjunction73 = null;


        Object LPAREN72_tree=null;
        Object RPAREN74_tree=null;
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
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:256:2: ( ftsFieldGroupTerm -> ^( FG_TERM ftsFieldGroupTerm ) | ftsFieldGroupExactTerm -> ^( FG_EXACT_TERM ftsFieldGroupExactTerm ) | ftsFieldGroupPhrase -> ^( FG_PHRASE ftsFieldGroupPhrase ) | ftsFieldGroupSynonym -> ^( FG_SYNONYM ftsFieldGroupSynonym ) | ftsFieldGroupProximity -> ^( FG_PROXIMITY ftsFieldGroupProximity ) | ftsFieldGroupRange -> ^( FG_RANGE ftsFieldGroupRange ) | LPAREN ftsFieldGroupImplicitConjunctionOrDisjunction RPAREN -> ftsFieldGroupImplicitConjunctionOrDisjunction )
            int alt16=7;
            alt16 = dfa16.predict(input);
            switch (alt16) {
                case 1 :
                    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:256:4: ftsFieldGroupTerm
                    {
                    pushFollow(FOLLOW_ftsFieldGroupTerm_in_ftsFieldGroupTest1087);
                    ftsFieldGroupTerm66=ftsFieldGroupTerm();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_ftsFieldGroupTerm.add(ftsFieldGroupTerm66.getTree());


                    // AST REWRITE
                    // elements: ftsFieldGroupTerm
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 257:3: -> ^( FG_TERM ftsFieldGroupTerm )
                    {
                        // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:257:6: ^( FG_TERM ftsFieldGroupTerm )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(FG_TERM, "FG_TERM"), root_1);

                        adaptor.addChild(root_1, stream_ftsFieldGroupTerm.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:258:4: ftsFieldGroupExactTerm
                    {
                    pushFollow(FOLLOW_ftsFieldGroupExactTerm_in_ftsFieldGroupTest1102);
                    ftsFieldGroupExactTerm67=ftsFieldGroupExactTerm();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_ftsFieldGroupExactTerm.add(ftsFieldGroupExactTerm67.getTree());


                    // AST REWRITE
                    // elements: ftsFieldGroupExactTerm
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 259:3: -> ^( FG_EXACT_TERM ftsFieldGroupExactTerm )
                    {
                        // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:259:6: ^( FG_EXACT_TERM ftsFieldGroupExactTerm )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(FG_EXACT_TERM, "FG_EXACT_TERM"), root_1);

                        adaptor.addChild(root_1, stream_ftsFieldGroupExactTerm.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 3 :
                    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:260:4: ftsFieldGroupPhrase
                    {
                    pushFollow(FOLLOW_ftsFieldGroupPhrase_in_ftsFieldGroupTest1118);
                    ftsFieldGroupPhrase68=ftsFieldGroupPhrase();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_ftsFieldGroupPhrase.add(ftsFieldGroupPhrase68.getTree());


                    // AST REWRITE
                    // elements: ftsFieldGroupPhrase
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 261:3: -> ^( FG_PHRASE ftsFieldGroupPhrase )
                    {
                        // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:261:6: ^( FG_PHRASE ftsFieldGroupPhrase )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(FG_PHRASE, "FG_PHRASE"), root_1);

                        adaptor.addChild(root_1, stream_ftsFieldGroupPhrase.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 4 :
                    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:262:4: ftsFieldGroupSynonym
                    {
                    pushFollow(FOLLOW_ftsFieldGroupSynonym_in_ftsFieldGroupTest1133);
                    ftsFieldGroupSynonym69=ftsFieldGroupSynonym();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_ftsFieldGroupSynonym.add(ftsFieldGroupSynonym69.getTree());


                    // AST REWRITE
                    // elements: ftsFieldGroupSynonym
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 263:3: -> ^( FG_SYNONYM ftsFieldGroupSynonym )
                    {
                        // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:263:6: ^( FG_SYNONYM ftsFieldGroupSynonym )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(FG_SYNONYM, "FG_SYNONYM"), root_1);

                        adaptor.addChild(root_1, stream_ftsFieldGroupSynonym.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 5 :
                    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:264:6: ftsFieldGroupProximity
                    {
                    pushFollow(FOLLOW_ftsFieldGroupProximity_in_ftsFieldGroupTest1150);
                    ftsFieldGroupProximity70=ftsFieldGroupProximity();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_ftsFieldGroupProximity.add(ftsFieldGroupProximity70.getTree());


                    // AST REWRITE
                    // elements: ftsFieldGroupProximity
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 265:3: -> ^( FG_PROXIMITY ftsFieldGroupProximity )
                    {
                        // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:265:6: ^( FG_PROXIMITY ftsFieldGroupProximity )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(FG_PROXIMITY, "FG_PROXIMITY"), root_1);

                        adaptor.addChild(root_1, stream_ftsFieldGroupProximity.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 6 :
                    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:266:8: ftsFieldGroupRange
                    {
                    pushFollow(FOLLOW_ftsFieldGroupRange_in_ftsFieldGroupTest1171);
                    ftsFieldGroupRange71=ftsFieldGroupRange();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_ftsFieldGroupRange.add(ftsFieldGroupRange71.getTree());


                    // AST REWRITE
                    // elements: ftsFieldGroupRange
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 267:9: -> ^( FG_RANGE ftsFieldGroupRange )
                    {
                        // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:267:12: ^( FG_RANGE ftsFieldGroupRange )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(FG_RANGE, "FG_RANGE"), root_1);

                        adaptor.addChild(root_1, stream_ftsFieldGroupRange.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 7 :
                    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:268:4: LPAREN ftsFieldGroupImplicitConjunctionOrDisjunction RPAREN
                    {
                    LPAREN72=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_ftsFieldGroupTest1192); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_LPAREN.add(LPAREN72);

                    pushFollow(FOLLOW_ftsFieldGroupImplicitConjunctionOrDisjunction_in_ftsFieldGroupTest1194);
                    ftsFieldGroupImplicitConjunctionOrDisjunction73=ftsFieldGroupImplicitConjunctionOrDisjunction();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_ftsFieldGroupImplicitConjunctionOrDisjunction.add(ftsFieldGroupImplicitConjunctionOrDisjunction73.getTree());
                    RPAREN74=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_ftsFieldGroupTest1196); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_RPAREN.add(RPAREN74);



                    // AST REWRITE
                    // elements: ftsFieldGroupImplicitConjunctionOrDisjunction
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 269:3: -> ftsFieldGroupImplicitConjunctionOrDisjunction
                    {
                        adaptor.addChild(root_0, stream_ftsFieldGroupImplicitConjunctionOrDisjunction.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        catch(RecognitionException e)
        {
           throw e;
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "ftsFieldGroupTest"

    public static class ftsFieldGroupTerm_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "ftsFieldGroupTerm"
    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:272:1: ftsFieldGroupTerm : ftsWord ;
    public final FTSParser.ftsFieldGroupTerm_return ftsFieldGroupTerm() throws RecognitionException {
        FTSParser.ftsFieldGroupTerm_return retval = new FTSParser.ftsFieldGroupTerm_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        FTSParser.ftsWord_return ftsWord75 = null;



        try {
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:273:2: ( ftsWord )
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:273:4: ftsWord
            {
            root_0 = (Object)adaptor.nil();

            pushFollow(FOLLOW_ftsWord_in_ftsFieldGroupTerm1214);
            ftsWord75=ftsWord();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, ftsWord75.getTree());

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        catch(RecognitionException e)
        {
           throw e;
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "ftsFieldGroupTerm"

    public static class ftsFieldGroupExactTerm_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "ftsFieldGroupExactTerm"
    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:276:1: ftsFieldGroupExactTerm : EQUALS ftsFieldGroupTerm -> ftsFieldGroupTerm ;
    public final FTSParser.ftsFieldGroupExactTerm_return ftsFieldGroupExactTerm() throws RecognitionException {
        FTSParser.ftsFieldGroupExactTerm_return retval = new FTSParser.ftsFieldGroupExactTerm_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token EQUALS76=null;
        FTSParser.ftsFieldGroupTerm_return ftsFieldGroupTerm77 = null;


        Object EQUALS76_tree=null;
        RewriteRuleTokenStream stream_EQUALS=new RewriteRuleTokenStream(adaptor,"token EQUALS");
        RewriteRuleSubtreeStream stream_ftsFieldGroupTerm=new RewriteRuleSubtreeStream(adaptor,"rule ftsFieldGroupTerm");
        try {
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:277:2: ( EQUALS ftsFieldGroupTerm -> ftsFieldGroupTerm )
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:277:4: EQUALS ftsFieldGroupTerm
            {
            EQUALS76=(Token)match(input,EQUALS,FOLLOW_EQUALS_in_ftsFieldGroupExactTerm1226); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_EQUALS.add(EQUALS76);

            pushFollow(FOLLOW_ftsFieldGroupTerm_in_ftsFieldGroupExactTerm1228);
            ftsFieldGroupTerm77=ftsFieldGroupTerm();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_ftsFieldGroupTerm.add(ftsFieldGroupTerm77.getTree());


            // AST REWRITE
            // elements: ftsFieldGroupTerm
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 278:3: -> ftsFieldGroupTerm
            {
                adaptor.addChild(root_0, stream_ftsFieldGroupTerm.nextTree());

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        catch(RecognitionException e)
        {
           throw e;
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "ftsFieldGroupExactTerm"

    public static class ftsFieldGroupPhrase_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "ftsFieldGroupPhrase"
    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:281:1: ftsFieldGroupPhrase : FTSPHRASE ;
    public final FTSParser.ftsFieldGroupPhrase_return ftsFieldGroupPhrase() throws RecognitionException {
        FTSParser.ftsFieldGroupPhrase_return retval = new FTSParser.ftsFieldGroupPhrase_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token FTSPHRASE78=null;

        Object FTSPHRASE78_tree=null;

        try {
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:282:2: ( FTSPHRASE )
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:282:6: FTSPHRASE
            {
            root_0 = (Object)adaptor.nil();

            FTSPHRASE78=(Token)match(input,FTSPHRASE,FOLLOW_FTSPHRASE_in_ftsFieldGroupPhrase1248); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            FTSPHRASE78_tree = (Object)adaptor.create(FTSPHRASE78);
            adaptor.addChild(root_0, FTSPHRASE78_tree);
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        catch(RecognitionException e)
        {
           throw e;
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "ftsFieldGroupPhrase"

    public static class ftsFieldGroupSynonym_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "ftsFieldGroupSynonym"
    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:285:1: ftsFieldGroupSynonym : TILDA ftsFieldGroupTerm -> ftsFieldGroupTerm ;
    public final FTSParser.ftsFieldGroupSynonym_return ftsFieldGroupSynonym() throws RecognitionException {
        FTSParser.ftsFieldGroupSynonym_return retval = new FTSParser.ftsFieldGroupSynonym_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token TILDA79=null;
        FTSParser.ftsFieldGroupTerm_return ftsFieldGroupTerm80 = null;


        Object TILDA79_tree=null;
        RewriteRuleTokenStream stream_TILDA=new RewriteRuleTokenStream(adaptor,"token TILDA");
        RewriteRuleSubtreeStream stream_ftsFieldGroupTerm=new RewriteRuleSubtreeStream(adaptor,"rule ftsFieldGroupTerm");
        try {
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:286:2: ( TILDA ftsFieldGroupTerm -> ftsFieldGroupTerm )
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:286:4: TILDA ftsFieldGroupTerm
            {
            TILDA79=(Token)match(input,TILDA,FOLLOW_TILDA_in_ftsFieldGroupSynonym1260); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_TILDA.add(TILDA79);

            pushFollow(FOLLOW_ftsFieldGroupTerm_in_ftsFieldGroupSynonym1262);
            ftsFieldGroupTerm80=ftsFieldGroupTerm();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_ftsFieldGroupTerm.add(ftsFieldGroupTerm80.getTree());


            // AST REWRITE
            // elements: ftsFieldGroupTerm
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 287:3: -> ftsFieldGroupTerm
            {
                adaptor.addChild(root_0, stream_ftsFieldGroupTerm.nextTree());

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        catch(RecognitionException e)
        {
           throw e;
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "ftsFieldGroupSynonym"

    public static class ftsFieldGroupProximity_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "ftsFieldGroupProximity"
    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:290:1: ftsFieldGroupProximity : ftsFieldGroupTerm STAR ftsFieldGroupTerm -> ftsFieldGroupTerm ftsFieldGroupTerm ;
    public final FTSParser.ftsFieldGroupProximity_return ftsFieldGroupProximity() throws RecognitionException {
        FTSParser.ftsFieldGroupProximity_return retval = new FTSParser.ftsFieldGroupProximity_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token STAR82=null;
        FTSParser.ftsFieldGroupTerm_return ftsFieldGroupTerm81 = null;

        FTSParser.ftsFieldGroupTerm_return ftsFieldGroupTerm83 = null;


        Object STAR82_tree=null;
        RewriteRuleTokenStream stream_STAR=new RewriteRuleTokenStream(adaptor,"token STAR");
        RewriteRuleSubtreeStream stream_ftsFieldGroupTerm=new RewriteRuleSubtreeStream(adaptor,"rule ftsFieldGroupTerm");
        try {
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:291:2: ( ftsFieldGroupTerm STAR ftsFieldGroupTerm -> ftsFieldGroupTerm ftsFieldGroupTerm )
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:291:4: ftsFieldGroupTerm STAR ftsFieldGroupTerm
            {
            pushFollow(FOLLOW_ftsFieldGroupTerm_in_ftsFieldGroupProximity1280);
            ftsFieldGroupTerm81=ftsFieldGroupTerm();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_ftsFieldGroupTerm.add(ftsFieldGroupTerm81.getTree());
            STAR82=(Token)match(input,STAR,FOLLOW_STAR_in_ftsFieldGroupProximity1282); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_STAR.add(STAR82);

            pushFollow(FOLLOW_ftsFieldGroupTerm_in_ftsFieldGroupProximity1284);
            ftsFieldGroupTerm83=ftsFieldGroupTerm();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_ftsFieldGroupTerm.add(ftsFieldGroupTerm83.getTree());


            // AST REWRITE
            // elements: ftsFieldGroupTerm, ftsFieldGroupTerm
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 292:3: -> ftsFieldGroupTerm ftsFieldGroupTerm
            {
                adaptor.addChild(root_0, stream_ftsFieldGroupTerm.nextTree());
                adaptor.addChild(root_0, stream_ftsFieldGroupTerm.nextTree());

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        catch(RecognitionException e)
        {
           throw e;
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "ftsFieldGroupProximity"

    public static class ftsFieldGroupRange_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "ftsFieldGroupRange"
    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:295:1: ftsFieldGroupRange : ( ftsRangeWord DOTDOT ftsRangeWord -> INCLUSIVE ftsRangeWord ftsRangeWord INCLUSIVE | range_left ftsRangeWord TO ftsRangeWord range_right -> range_left ftsRangeWord ftsRangeWord range_right );
    public final FTSParser.ftsFieldGroupRange_return ftsFieldGroupRange() throws RecognitionException {
        FTSParser.ftsFieldGroupRange_return retval = new FTSParser.ftsFieldGroupRange_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token DOTDOT85=null;
        Token TO89=null;
        FTSParser.ftsRangeWord_return ftsRangeWord84 = null;

        FTSParser.ftsRangeWord_return ftsRangeWord86 = null;

        FTSParser.range_left_return range_left87 = null;

        FTSParser.ftsRangeWord_return ftsRangeWord88 = null;

        FTSParser.ftsRangeWord_return ftsRangeWord90 = null;

        FTSParser.range_right_return range_right91 = null;


        Object DOTDOT85_tree=null;
        Object TO89_tree=null;
        RewriteRuleTokenStream stream_DOTDOT=new RewriteRuleTokenStream(adaptor,"token DOTDOT");
        RewriteRuleTokenStream stream_TO=new RewriteRuleTokenStream(adaptor,"token TO");
        RewriteRuleSubtreeStream stream_range_left=new RewriteRuleSubtreeStream(adaptor,"rule range_left");
        RewriteRuleSubtreeStream stream_range_right=new RewriteRuleSubtreeStream(adaptor,"rule range_right");
        RewriteRuleSubtreeStream stream_ftsRangeWord=new RewriteRuleSubtreeStream(adaptor,"rule ftsRangeWord");
        try {
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:296:9: ( ftsRangeWord DOTDOT ftsRangeWord -> INCLUSIVE ftsRangeWord ftsRangeWord INCLUSIVE | range_left ftsRangeWord TO ftsRangeWord range_right -> range_left ftsRangeWord ftsRangeWord range_right )
            int alt17=2;
            int LA17_0 = input.LA(1);

            if ( (LA17_0==FTSPHRASE||(LA17_0>=ID && LA17_0<=FTSWORD)) ) {
                alt17=1;
            }
            else if ( (LA17_0==LPAREN||LA17_0==LCURL) ) {
                alt17=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 17, 0, input);

                throw nvae;
            }
            switch (alt17) {
                case 1 :
                    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:296:11: ftsRangeWord DOTDOT ftsRangeWord
                    {
                    pushFollow(FOLLOW_ftsRangeWord_in_ftsFieldGroupRange1311);
                    ftsRangeWord84=ftsRangeWord();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_ftsRangeWord.add(ftsRangeWord84.getTree());
                    DOTDOT85=(Token)match(input,DOTDOT,FOLLOW_DOTDOT_in_ftsFieldGroupRange1313); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_DOTDOT.add(DOTDOT85);

                    pushFollow(FOLLOW_ftsRangeWord_in_ftsFieldGroupRange1315);
                    ftsRangeWord86=ftsRangeWord();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_ftsRangeWord.add(ftsRangeWord86.getTree());


                    // AST REWRITE
                    // elements: ftsRangeWord, ftsRangeWord
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 297:3: -> INCLUSIVE ftsRangeWord ftsRangeWord INCLUSIVE
                    {
                        adaptor.addChild(root_0, (Object)adaptor.create(INCLUSIVE, "INCLUSIVE"));
                        adaptor.addChild(root_0, stream_ftsRangeWord.nextTree());
                        adaptor.addChild(root_0, stream_ftsRangeWord.nextTree());
                        adaptor.addChild(root_0, (Object)adaptor.create(INCLUSIVE, "INCLUSIVE"));

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:298:4: range_left ftsRangeWord TO ftsRangeWord range_right
                    {
                    pushFollow(FOLLOW_range_left_in_ftsFieldGroupRange1332);
                    range_left87=range_left();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_range_left.add(range_left87.getTree());
                    pushFollow(FOLLOW_ftsRangeWord_in_ftsFieldGroupRange1334);
                    ftsRangeWord88=ftsRangeWord();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_ftsRangeWord.add(ftsRangeWord88.getTree());
                    TO89=(Token)match(input,TO,FOLLOW_TO_in_ftsFieldGroupRange1336); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_TO.add(TO89);

                    pushFollow(FOLLOW_ftsRangeWord_in_ftsFieldGroupRange1338);
                    ftsRangeWord90=ftsRangeWord();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_ftsRangeWord.add(ftsRangeWord90.getTree());
                    pushFollow(FOLLOW_range_right_in_ftsFieldGroupRange1340);
                    range_right91=range_right();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_range_right.add(range_right91.getTree());


                    // AST REWRITE
                    // elements: range_left, ftsRangeWord, ftsRangeWord, range_right
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 299:3: -> range_left ftsRangeWord ftsRangeWord range_right
                    {
                        adaptor.addChild(root_0, stream_range_left.nextTree());
                        adaptor.addChild(root_0, stream_ftsRangeWord.nextTree());
                        adaptor.addChild(root_0, stream_ftsRangeWord.nextTree());
                        adaptor.addChild(root_0, stream_range_right.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        catch(RecognitionException e)
        {
           throw e;
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "ftsFieldGroupRange"

    public static class range_left_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "range_left"
    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:302:1: range_left : ( LPAREN -> INCLUSIVE | LCURL -> EXCLUSIVE );
    public final FTSParser.range_left_return range_left() throws RecognitionException {
        FTSParser.range_left_return retval = new FTSParser.range_left_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token LPAREN92=null;
        Token LCURL93=null;

        Object LPAREN92_tree=null;
        Object LCURL93_tree=null;
        RewriteRuleTokenStream stream_LCURL=new RewriteRuleTokenStream(adaptor,"token LCURL");
        RewriteRuleTokenStream stream_LPAREN=new RewriteRuleTokenStream(adaptor,"token LPAREN");

        try {
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:303:2: ( LPAREN -> INCLUSIVE | LCURL -> EXCLUSIVE )
            int alt18=2;
            int LA18_0 = input.LA(1);

            if ( (LA18_0==LPAREN) ) {
                alt18=1;
            }
            else if ( (LA18_0==LCURL) ) {
                alt18=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 18, 0, input);

                throw nvae;
            }
            switch (alt18) {
                case 1 :
                    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:303:10: LPAREN
                    {
                    LPAREN92=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_range_left1370); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_LPAREN.add(LPAREN92);



                    // AST REWRITE
                    // elements: 
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 304:3: -> INCLUSIVE
                    {
                        adaptor.addChild(root_0, (Object)adaptor.create(INCLUSIVE, "INCLUSIVE"));

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:305:4: LCURL
                    {
                    LCURL93=(Token)match(input,LCURL,FOLLOW_LCURL_in_range_left1381); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_LCURL.add(LCURL93);



                    // AST REWRITE
                    // elements: 
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 306:3: -> EXCLUSIVE
                    {
                        adaptor.addChild(root_0, (Object)adaptor.create(EXCLUSIVE, "EXCLUSIVE"));

                    }

                    retval.tree = root_0;}
                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        catch(RecognitionException e)
        {
           throw e;
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "range_left"

    public static class range_right_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "range_right"
    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:309:1: range_right : ( RPAREN -> INCLUSIVE | RCURL -> EXCLUSIVE );
    public final FTSParser.range_right_return range_right() throws RecognitionException {
        FTSParser.range_right_return retval = new FTSParser.range_right_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token RPAREN94=null;
        Token RCURL95=null;

        Object RPAREN94_tree=null;
        Object RCURL95_tree=null;
        RewriteRuleTokenStream stream_RPAREN=new RewriteRuleTokenStream(adaptor,"token RPAREN");
        RewriteRuleTokenStream stream_RCURL=new RewriteRuleTokenStream(adaptor,"token RCURL");

        try {
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:310:2: ( RPAREN -> INCLUSIVE | RCURL -> EXCLUSIVE )
            int alt19=2;
            int LA19_0 = input.LA(1);

            if ( (LA19_0==RPAREN) ) {
                alt19=1;
            }
            else if ( (LA19_0==RCURL) ) {
                alt19=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 19, 0, input);

                throw nvae;
            }
            switch (alt19) {
                case 1 :
                    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:310:10: RPAREN
                    {
                    RPAREN94=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_range_right1405); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_RPAREN.add(RPAREN94);



                    // AST REWRITE
                    // elements: 
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 311:3: -> INCLUSIVE
                    {
                        adaptor.addChild(root_0, (Object)adaptor.create(INCLUSIVE, "INCLUSIVE"));

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:312:4: RCURL
                    {
                    RCURL95=(Token)match(input,RCURL,FOLLOW_RCURL_in_range_right1416); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_RCURL.add(RCURL95);



                    // AST REWRITE
                    // elements: 
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 313:3: -> EXCLUSIVE
                    {
                        adaptor.addChild(root_0, (Object)adaptor.create(EXCLUSIVE, "EXCLUSIVE"));

                    }

                    retval.tree = root_0;}
                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        catch(RecognitionException e)
        {
           throw e;
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "range_right"

    public static class columnReference_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "columnReference"
    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:317:1: columnReference : (qualifier= identifier DOT )? name= identifier -> ^( COLUMN_REF $name ( $qualifier)? ) ;
    public final FTSParser.columnReference_return columnReference() throws RecognitionException {
        FTSParser.columnReference_return retval = new FTSParser.columnReference_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token DOT96=null;
        FTSParser.identifier_return qualifier = null;

        FTSParser.identifier_return name = null;


        Object DOT96_tree=null;
        RewriteRuleTokenStream stream_DOT=new RewriteRuleTokenStream(adaptor,"token DOT");
        RewriteRuleSubtreeStream stream_identifier=new RewriteRuleSubtreeStream(adaptor,"rule identifier");
        try {
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:318:2: ( (qualifier= identifier DOT )? name= identifier -> ^( COLUMN_REF $name ( $qualifier)? ) )
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:318:4: (qualifier= identifier DOT )? name= identifier
            {
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:318:4: (qualifier= identifier DOT )?
            int alt20=2;
            int LA20_0 = input.LA(1);

            if ( (LA20_0==ID) ) {
                int LA20_1 = input.LA(2);

                if ( (LA20_1==DOT) ) {
                    alt20=1;
                }
            }
            switch (alt20) {
                case 1 :
                    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:318:6: qualifier= identifier DOT
                    {
                    pushFollow(FOLLOW_identifier_in_columnReference1441);
                    qualifier=identifier();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_identifier.add(qualifier.getTree());
                    DOT96=(Token)match(input,DOT,FOLLOW_DOT_in_columnReference1443); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_DOT.add(DOT96);


                    }
                    break;

            }

            pushFollow(FOLLOW_identifier_in_columnReference1450);
            name=identifier();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_identifier.add(name.getTree());


            // AST REWRITE
            // elements: name, qualifier
            // token labels: 
            // rule labels: retval, name, qualifier
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            RewriteRuleSubtreeStream stream_name=new RewriteRuleSubtreeStream(adaptor,"rule name",name!=null?name.tree:null);
            RewriteRuleSubtreeStream stream_qualifier=new RewriteRuleSubtreeStream(adaptor,"rule qualifier",qualifier!=null?qualifier.tree:null);

            root_0 = (Object)adaptor.nil();
            // 319:3: -> ^( COLUMN_REF $name ( $qualifier)? )
            {
                // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:319:6: ^( COLUMN_REF $name ( $qualifier)? )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(COLUMN_REF, "COLUMN_REF"), root_1);

                adaptor.addChild(root_1, stream_name.nextTree());
                // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:319:25: ( $qualifier)?
                if ( stream_qualifier.hasNext() ) {
                    adaptor.addChild(root_1, stream_qualifier.nextTree());

                }
                stream_qualifier.reset();

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        catch(RecognitionException e)
        {
           throw e;
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "columnReference"

    public static class identifier_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "identifier"
    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:322:1: identifier : ID ;
    public final FTSParser.identifier_return identifier() throws RecognitionException {
        FTSParser.identifier_return retval = new FTSParser.identifier_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token ID97=null;

        Object ID97_tree=null;

        try {
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:323:2: ( ID )
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:323:4: ID
            {
            root_0 = (Object)adaptor.nil();

            ID97=(Token)match(input,ID,FOLLOW_ID_in_identifier1478); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            ID97_tree = (Object)adaptor.create(ID97);
            adaptor.addChild(root_0, ID97_tree);
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        catch(RecognitionException e)
        {
           throw e;
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "identifier"

    public static class ftsWord_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "ftsWord"
    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:326:1: ftsWord : ( ID | FTSWORD | OR | AND | NOT | TO );
    public final FTSParser.ftsWord_return ftsWord() throws RecognitionException {
        FTSParser.ftsWord_return retval = new FTSParser.ftsWord_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token set98=null;

        Object set98_tree=null;

        try {
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:327:5: ( ID | FTSWORD | OR | AND | NOT | TO )
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:
            {
            root_0 = (Object)adaptor.nil();

            set98=(Token)input.LT(1);
            if ( input.LA(1)==TO||(input.LA(1)>=ID && input.LA(1)<=NOT) ) {
                input.consume();
                if ( state.backtracking==0 ) adaptor.addChild(root_0, (Object)adaptor.create(set98));
                state.errorRecovery=false;state.failed=false;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                MismatchedSetException mse = new MismatchedSetException(null,input);
                throw mse;
            }


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        catch(RecognitionException e)
        {
           throw e;
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "ftsWord"

    public static class ftsRangeWord_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "ftsRangeWord"
    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:335:1: ftsRangeWord : ( ID | FTSWORD | FTSPHRASE );
    public final FTSParser.ftsRangeWord_return ftsRangeWord() throws RecognitionException {
        FTSParser.ftsRangeWord_return retval = new FTSParser.ftsRangeWord_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token set99=null;

        Object set99_tree=null;

        try {
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:336:5: ( ID | FTSWORD | FTSPHRASE )
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:
            {
            root_0 = (Object)adaptor.nil();

            set99=(Token)input.LT(1);
            if ( input.LA(1)==FTSPHRASE||(input.LA(1)>=ID && input.LA(1)<=FTSWORD) ) {
                input.consume();
                if ( state.backtracking==0 ) adaptor.addChild(root_0, (Object)adaptor.create(set99));
                state.errorRecovery=false;state.failed=false;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                MismatchedSetException mse = new MismatchedSetException(null,input);
                throw mse;
            }


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        catch(RecognitionException e)
        {
           throw e;
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "ftsRangeWord"

    public static class or_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "or"
    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:341:1: or : ( OR | BAR BAR );
    public final FTSParser.or_return or() throws RecognitionException {
        FTSParser.or_return retval = new FTSParser.or_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token OR100=null;
        Token BAR101=null;
        Token BAR102=null;

        Object OR100_tree=null;
        Object BAR101_tree=null;
        Object BAR102_tree=null;

        try {
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:342:5: ( OR | BAR BAR )
            int alt21=2;
            int LA21_0 = input.LA(1);

            if ( (LA21_0==OR) ) {
                alt21=1;
            }
            else if ( (LA21_0==BAR) ) {
                alt21=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 21, 0, input);

                throw nvae;
            }
            switch (alt21) {
                case 1 :
                    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:342:9: OR
                    {
                    root_0 = (Object)adaptor.nil();

                    OR100=(Token)match(input,OR,FOLLOW_OR_in_or1608); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    OR100_tree = (Object)adaptor.create(OR100);
                    adaptor.addChild(root_0, OR100_tree);
                    }

                    }
                    break;
                case 2 :
                    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:343:7: BAR BAR
                    {
                    root_0 = (Object)adaptor.nil();

                    BAR101=(Token)match(input,BAR,FOLLOW_BAR_in_or1616); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    BAR101_tree = (Object)adaptor.create(BAR101);
                    adaptor.addChild(root_0, BAR101_tree);
                    }
                    BAR102=(Token)match(input,BAR,FOLLOW_BAR_in_or1618); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    BAR102_tree = (Object)adaptor.create(BAR102);
                    adaptor.addChild(root_0, BAR102_tree);
                    }

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        catch(RecognitionException e)
        {
           throw e;
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "or"

    public static class and_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "and"
    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:346:1: and : ( AND | AMP AMP );
    public final FTSParser.and_return and() throws RecognitionException {
        FTSParser.and_return retval = new FTSParser.and_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token AND103=null;
        Token AMP104=null;
        Token AMP105=null;

        Object AND103_tree=null;
        Object AMP104_tree=null;
        Object AMP105_tree=null;

        try {
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:347:5: ( AND | AMP AMP )
            int alt22=2;
            int LA22_0 = input.LA(1);

            if ( (LA22_0==AND) ) {
                alt22=1;
            }
            else if ( (LA22_0==AMP) ) {
                alt22=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 22, 0, input);

                throw nvae;
            }
            switch (alt22) {
                case 1 :
                    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:347:7: AND
                    {
                    root_0 = (Object)adaptor.nil();

                    AND103=(Token)match(input,AND,FOLLOW_AND_in_and1640); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    AND103_tree = (Object)adaptor.create(AND103);
                    adaptor.addChild(root_0, AND103_tree);
                    }

                    }
                    break;
                case 2 :
                    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:348:7: AMP AMP
                    {
                    root_0 = (Object)adaptor.nil();

                    AMP104=(Token)match(input,AMP,FOLLOW_AMP_in_and1648); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    AMP104_tree = (Object)adaptor.create(AMP104);
                    adaptor.addChild(root_0, AMP104_tree);
                    }
                    AMP105=(Token)match(input,AMP,FOLLOW_AMP_in_and1650); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    AMP105_tree = (Object)adaptor.create(AMP105);
                    adaptor.addChild(root_0, AMP105_tree);
                    }

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        catch(RecognitionException e)
        {
           throw e;
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "and"

    public static class not_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "not"
    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:351:1: not : ( NOT | EXCLAMATION );
    public final FTSParser.not_return not() throws RecognitionException {
        FTSParser.not_return retval = new FTSParser.not_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token set106=null;

        Object set106_tree=null;

        try {
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:352:5: ( NOT | EXCLAMATION )
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:
            {
            root_0 = (Object)adaptor.nil();

            set106=(Token)input.LT(1);
            if ( input.LA(1)==NOT||input.LA(1)==EXCLAMATION ) {
                input.consume();
                if ( state.backtracking==0 ) adaptor.addChild(root_0, (Object)adaptor.create(set106));
                state.errorRecovery=false;state.failed=false;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                MismatchedSetException mse = new MismatchedSetException(null,input);
                throw mse;
            }


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        catch(RecognitionException e)
        {
           throw e;
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "not"

    // $ANTLR start synpred1_FTS
    public final void synpred1_FTS_fragment() throws RecognitionException {   
        // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:156:27: ( or )
        // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:156:28: or
        {
        pushFollow(FOLLOW_or_in_synpred1_FTS268);
        or();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred1_FTS

    // $ANTLR start synpred2_FTS
    public final void synpred2_FTS_fragment() throws RecognitionException {   
        // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:161:17: ( and )
        // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:161:18: and
        {
        pushFollow(FOLLOW_and_in_synpred2_FTS304);
        and();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred2_FTS

    // $ANTLR start synpred3_FTS
    public final void synpred3_FTS_fragment() throws RecognitionException {   
        // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:167:7: ( not )
        // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:167:8: not
        {
        pushFollow(FOLLOW_not_in_synpred3_FTS344);
        not();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred3_FTS

    // $ANTLR start synpred4_FTS
    public final void synpred4_FTS_fragment() throws RecognitionException {   
        // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:231:37: ( or )
        // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:231:38: or
        {
        pushFollow(FOLLOW_or_in_synpred4_FTS857);
        or();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred4_FTS

    // $ANTLR start synpred5_FTS
    public final void synpred5_FTS_fragment() throws RecognitionException {   
        // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:236:27: ( and )
        // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:236:28: and
        {
        pushFollow(FOLLOW_and_in_synpred5_FTS893);
        and();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred5_FTS

    // $ANTLR start synpred6_FTS
    public final void synpred6_FTS_fragment() throws RecognitionException {   
        // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:242:3: ( not )
        // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:242:4: not
        {
        pushFollow(FOLLOW_not_in_synpred6_FTS929);
        not();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred6_FTS

    // Delegated rules

    public final boolean synpred6_FTS() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred6_FTS_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred4_FTS() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred4_FTS_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred5_FTS() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred5_FTS_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
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
    protected DFA4 dfa4 = new DFA4(this);
    protected DFA5 dfa5 = new DFA5(this);
    protected DFA6 dfa6 = new DFA6(this);
    protected DFA7 dfa7 = new DFA7(this);
    protected DFA12 dfa12 = new DFA12(this);
    protected DFA13 dfa13 = new DFA13(this);
    protected DFA14 dfa14 = new DFA14(this);
    protected DFA15 dfa15 = new DFA15(this);
    protected DFA16 dfa16 = new DFA16(this);
    static final String DFA3_eotS =
        "\20\uffff";
    static final String DFA3_eofS =
        "\20\uffff";
    static final String DFA3_minS =
        "\1\41\15\0\2\uffff";
    static final String DFA3_maxS =
        "\1\66\15\0\2\uffff";
    static final String DFA3_acceptS =
        "\16\uffff\1\1\1\2";
    static final String DFA3_specialS =
        "\1\uffff\1\0\1\1\1\2\1\3\1\4\1\5\1\6\1\7\1\10\1\11\1\12\1\13\1"+
        "\14\2\uffff}>";
    static final String[] DFA3_transitionS = {
            "\1\13\1\14\1\15\1\11\2\uffff\1\5\1\6\1\7\2\uffff\1\10\1\12"+
            "\2\uffff\1\2\1\4\2\10\1\1\1\uffff\1\3",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
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
            return "148:1: ftsImplicitConjunctionOrDisjunction : ({...}? ftsExplicitDisjunction ( ftsExplicitDisjunction )* -> ^( CONJUNCTION ( ftsExplicitDisjunction )+ ) | ftsExplicitDisjunction ( ftsExplicitDisjunction )* -> ^( DISJUNCTION ( ftsExplicitDisjunction )+ ) );";
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
                        if ( ((defaultConjunction())) ) {s = 14;}

                        else if ( (true) ) {s = 15;}

                         
                        input.seek(index3_1);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA3_2 = input.LA(1);

                         
                        int index3_2 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((defaultConjunction())) ) {s = 14;}

                        else if ( (true) ) {s = 15;}

                         
                        input.seek(index3_2);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA3_3 = input.LA(1);

                         
                        int index3_3 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((defaultConjunction())) ) {s = 14;}

                        else if ( (true) ) {s = 15;}

                         
                        input.seek(index3_3);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA3_4 = input.LA(1);

                         
                        int index3_4 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((defaultConjunction())) ) {s = 14;}

                        else if ( (true) ) {s = 15;}

                         
                        input.seek(index3_4);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA3_5 = input.LA(1);

                         
                        int index3_5 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((defaultConjunction())) ) {s = 14;}

                        else if ( (true) ) {s = 15;}

                         
                        input.seek(index3_5);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA3_6 = input.LA(1);

                         
                        int index3_6 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((defaultConjunction())) ) {s = 14;}

                        else if ( (true) ) {s = 15;}

                         
                        input.seek(index3_6);
                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA3_7 = input.LA(1);

                         
                        int index3_7 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((defaultConjunction())) ) {s = 14;}

                        else if ( (true) ) {s = 15;}

                         
                        input.seek(index3_7);
                        if ( s>=0 ) return s;
                        break;
                    case 7 : 
                        int LA3_8 = input.LA(1);

                         
                        int index3_8 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((defaultConjunction())) ) {s = 14;}

                        else if ( (true) ) {s = 15;}

                         
                        input.seek(index3_8);
                        if ( s>=0 ) return s;
                        break;
                    case 8 : 
                        int LA3_9 = input.LA(1);

                         
                        int index3_9 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((defaultConjunction())) ) {s = 14;}

                        else if ( (true) ) {s = 15;}

                         
                        input.seek(index3_9);
                        if ( s>=0 ) return s;
                        break;
                    case 9 : 
                        int LA3_10 = input.LA(1);

                         
                        int index3_10 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((defaultConjunction())) ) {s = 14;}

                        else if ( (true) ) {s = 15;}

                         
                        input.seek(index3_10);
                        if ( s>=0 ) return s;
                        break;
                    case 10 : 
                        int LA3_11 = input.LA(1);

                         
                        int index3_11 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((defaultConjunction())) ) {s = 14;}

                        else if ( (true) ) {s = 15;}

                         
                        input.seek(index3_11);
                        if ( s>=0 ) return s;
                        break;
                    case 11 : 
                        int LA3_12 = input.LA(1);

                         
                        int index3_12 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((defaultConjunction())) ) {s = 14;}

                        else if ( (true) ) {s = 15;}

                         
                        input.seek(index3_12);
                        if ( s>=0 ) return s;
                        break;
                    case 12 : 
                        int LA3_13 = input.LA(1);

                         
                        int index3_13 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((defaultConjunction())) ) {s = 14;}

                        else if ( (true) ) {s = 15;}

                         
                        input.seek(index3_13);
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
    static final String DFA4_eotS =
        "\22\uffff";
    static final String DFA4_eofS =
        "\1\1\21\uffff";
    static final String DFA4_minS =
        "\1\41\7\uffff\1\0\3\uffff\1\0\5\uffff";
    static final String DFA4_maxS =
        "\1\66\7\uffff\1\0\3\uffff\1\0\5\uffff";
    static final String DFA4_acceptS =
        "\1\uffff\1\2\17\uffff\1\1";
    static final String DFA4_specialS =
        "\10\uffff\1\0\3\uffff\1\1\5\uffff}>";
    static final String[] DFA4_transitionS = {
            "\1\1\1\14\3\1\1\uffff\3\1\2\uffff\2\1\2\uffff\2\1\1\10\2\1"+
            "\1\uffff\1\1",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\uffff",
            "",
            "",
            "",
            "\1\uffff",
            "",
            "",
            "",
            "",
            ""
    };

    static final short[] DFA4_eot = DFA.unpackEncodedString(DFA4_eotS);
    static final short[] DFA4_eof = DFA.unpackEncodedString(DFA4_eofS);
    static final char[] DFA4_min = DFA.unpackEncodedStringToUnsignedChars(DFA4_minS);
    static final char[] DFA4_max = DFA.unpackEncodedStringToUnsignedChars(DFA4_maxS);
    static final short[] DFA4_accept = DFA.unpackEncodedString(DFA4_acceptS);
    static final short[] DFA4_special = DFA.unpackEncodedString(DFA4_specialS);
    static final short[][] DFA4_transition;

    static {
        int numStates = DFA4_transitionS.length;
        DFA4_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA4_transition[i] = DFA.unpackEncodedString(DFA4_transitionS[i]);
        }
    }

    class DFA4 extends DFA {

        public DFA4(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 4;
            this.eot = DFA4_eot;
            this.eof = DFA4_eof;
            this.min = DFA4_min;
            this.max = DFA4_max;
            this.accept = DFA4_accept;
            this.special = DFA4_special;
            this.transition = DFA4_transition;
        }
        public String getDescription() {
            return "()* loopback of 156:26: ( ( or )=> or ftsExplictConjunction )*";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA4_8 = input.LA(1);

                         
                        int index4_8 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_FTS()) ) {s = 17;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index4_8);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA4_12 = input.LA(1);

                         
                        int index4_12 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_FTS()) ) {s = 17;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index4_12);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 4, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA5_eotS =
        "\23\uffff";
    static final String DFA5_eofS =
        "\1\1\22\uffff";
    static final String DFA5_minS =
        "\1\41\11\uffff\1\0\10\uffff";
    static final String DFA5_maxS =
        "\1\66\11\uffff\1\0\10\uffff";
    static final String DFA5_acceptS =
        "\1\uffff\1\2\20\uffff\1\1";
    static final String DFA5_specialS =
        "\1\0\11\uffff\1\1\10\uffff}>";
    static final String[] DFA5_transitionS = {
            "\5\1\1\uffff\3\1\2\uffff\2\1\2\uffff\3\1\1\12\1\1\1\22\1\1",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\uffff",
            "",
            "",
            "",
            "",
            "",
            "",
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
            return "()* loopback of 161:16: ( ( and )=> and ftsPrefixed )*";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA5_0 = input.LA(1);

                         
                        int index5_0 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA5_0==EOF||(LA5_0>=PLUS && LA5_0<=RPAREN)||(LA5_0>=EQUALS && LA5_0<=TILDA)||(LA5_0>=TO && LA5_0<=LCURL)||(LA5_0>=ID && LA5_0<=OR)||LA5_0==NOT||LA5_0==EXCLAMATION) ) {s = 1;}

                        else if ( (LA5_0==AND) ) {s = 10;}

                        else if ( (LA5_0==AMP) && (synpred2_FTS())) {s = 18;}

                         
                        input.seek(index5_0);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA5_10 = input.LA(1);

                         
                        int index5_10 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_FTS()) ) {s = 18;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index5_10);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 5, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA6_eotS =
        "\16\uffff";
    static final String DFA6_eofS =
        "\16\uffff";
    static final String DFA6_minS =
        "\1\41\1\0\14\uffff";
    static final String DFA6_maxS =
        "\1\66\1\0\14\uffff";
    static final String DFA6_acceptS =
        "\2\uffff\1\2\1\1\7\uffff\1\3\1\4\1\5";
    static final String DFA6_specialS =
        "\1\0\1\1\14\uffff}>";
    static final String[] DFA6_transitionS = {
            "\1\13\1\14\1\15\1\2\2\uffff\3\2\2\uffff\2\2\2\uffff\4\2\1\1"+
            "\1\uffff\1\3",
            "\1\uffff",
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
            return "166:1: ftsPrefixed : ( ( not )=> not ftsTest -> ^( NEGATION ftsTest ) | ftsTest -> ^( DEFAULT ftsTest ) | PLUS ftsTest -> ^( MANDATORY ftsTest ) | BAR ftsTest -> ^( OPTIONAL ftsTest ) | MINUS ftsTest -> ^( EXCLUDE ftsTest ) );";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA6_0 = input.LA(1);

                         
                        int index6_0 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA6_0==NOT) ) {s = 1;}

                        else if ( (LA6_0==LPAREN||(LA6_0>=EQUALS && LA6_0<=TILDA)||(LA6_0>=TO && LA6_0<=LCURL)||(LA6_0>=ID && LA6_0<=AND)) ) {s = 2;}

                        else if ( (LA6_0==EXCLAMATION) && (synpred3_FTS())) {s = 3;}

                        else if ( (LA6_0==PLUS) ) {s = 11;}

                        else if ( (LA6_0==BAR) ) {s = 12;}

                        else if ( (LA6_0==MINUS) ) {s = 13;}

                         
                        input.seek(index6_0);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA6_1 = input.LA(1);

                         
                        int index6_1 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred3_FTS()) ) {s = 3;}

                        else if ( (true) ) {s = 2;}

                         
                        input.seek(index6_1);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 6, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA7_eotS =
        "\31\uffff";
    static final String DFA7_eofS =
        "\1\uffff\2\13\1\uffff\1\15\1\uffff\1\13\22\uffff";
    static final String DFA7_minS =
        "\1\44\2\41\1\uffff\1\41\1\uffff\2\41\1\uffff\1\44\2\uffff\1\60"+
        "\2\uffff\3\41\1\uffff\1\46\4\41\1\uffff";
    static final String DFA7_maxS =
        "\1\64\2\66\1\uffff\1\66\1\uffff\2\66\1\uffff\1\64\2\uffff\1\60"+
        "\2\uffff\3\66\1\uffff\1\46\4\66\1\uffff";
    static final String DFA7_acceptS =
        "\3\uffff\1\2\1\uffff\1\4\2\uffff\1\6\1\uffff\1\5\1\1\1\uffff\1"+
        "\3\1\10\3\uffff\1\7\5\uffff\1\6";
    static final String DFA7_specialS =
        "\31\uffff}>";
    static final String[] DFA7_transitionS = {
            "\1\7\2\uffff\1\3\1\4\1\5\2\uffff\1\6\1\10\2\uffff\1\1\1\2\3"+
            "\6",
            "\5\13\1\11\3\13\1\12\1\10\2\13\1\uffff\1\14\7\13",
            "\5\13\1\uffff\3\13\1\12\1\10\2\13\2\uffff\7\13",
            "",
            "\5\15\1\uffff\3\15\1\uffff\1\10\2\15\2\uffff\7\15",
            "",
            "\5\13\1\uffff\3\13\1\12\1\uffff\2\13\2\uffff\7\13",
            "\4\16\2\uffff\1\16\1\21\1\16\2\uffff\2\16\2\uffff\1\17\1\20"+
            "\3\16\1\uffff\1\16",
            "",
            "\1\22\3\uffff\1\15\3\uffff\1\13\3\uffff\5\13",
            "",
            "",
            "\1\23",
            "",
            "",
            "\13\16\1\24\1\16\1\uffff\10\16",
            "\5\16\1\uffff\5\16\1\24\1\16\2\uffff\7\16",
            "\5\16\1\uffff\3\16\1\uffff\1\16\1\24\1\16\2\uffff\7\16",
            "",
            "\1\11",
            "\5\16\1\uffff\1\16\1\27\2\16\1\uffff\2\16\2\uffff\1\25\1\26"+
            "\5\16",
            "\4\16\1\30\10\16\1\30\10\16",
            "\4\16\1\30\1\uffff\7\16\1\30\1\uffff\7\16",
            "\4\16\1\30\1\uffff\3\16\1\uffff\3\16\1\30\1\uffff\7\16",
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
            return "179:1: ftsTest : ( ftsTerm -> ^( TERM ftsTerm ) | ftsExactTerm -> ^( EXACT_TERM ftsExactTerm ) | ftsPhrase -> ^( PHRASE ftsPhrase ) | ftsSynonym -> ^( SYNONYM ftsSynonym ) | ftsFieldGroupProximity -> ^( FG_PROXIMITY ftsFieldGroupProximity ) | ftsFieldGroupRange -> ^( FG_RANGE ftsFieldGroupRange ) | ftsFieldGroup | LPAREN ftsImplicitConjunctionOrDisjunction RPAREN -> ftsImplicitConjunctionOrDisjunction );";
        }
    }
    static final String DFA12_eotS =
        "\17\uffff";
    static final String DFA12_eofS =
        "\17\uffff";
    static final String DFA12_minS =
        "\1\41\14\0\2\uffff";
    static final String DFA12_maxS =
        "\1\66\14\0\2\uffff";
    static final String DFA12_acceptS =
        "\15\uffff\1\1\1\2";
    static final String DFA12_specialS =
        "\1\uffff\1\0\1\1\1\2\1\3\1\4\1\5\1\6\1\7\1\10\1\11\1\12\1\13\2"+
        "\uffff}>";
    static final String[] DFA12_transitionS = {
            "\1\12\1\13\1\14\1\10\2\uffff\1\4\1\5\1\6\2\uffff\1\7\1\11\2"+
            "\uffff\2\3\2\7\1\1\1\uffff\1\2",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
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
            return "223:1: ftsFieldGroupImplicitConjunctionOrDisjunction : ({...}? ftsFieldGroupExplicitDisjunction ( ftsFieldGroupExplicitDisjunction )* -> ^( FIELD_CONJUNCTION ( ftsFieldGroupExplicitDisjunction )+ ) | ftsFieldGroupExplicitDisjunction ( ftsFieldGroupExplicitDisjunction )* -> ^( FIELD_DISJUNCTION ( ftsFieldGroupExplicitDisjunction )+ ) );";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA12_1 = input.LA(1);

                         
                        int index12_1 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((defaultFieldConjunction())) ) {s = 13;}

                        else if ( (true) ) {s = 14;}

                         
                        input.seek(index12_1);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA12_2 = input.LA(1);

                         
                        int index12_2 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((defaultFieldConjunction())) ) {s = 13;}

                        else if ( (true) ) {s = 14;}

                         
                        input.seek(index12_2);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA12_3 = input.LA(1);

                         
                        int index12_3 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((defaultFieldConjunction())) ) {s = 13;}

                        else if ( (true) ) {s = 14;}

                         
                        input.seek(index12_3);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA12_4 = input.LA(1);

                         
                        int index12_4 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((defaultFieldConjunction())) ) {s = 13;}

                        else if ( (true) ) {s = 14;}

                         
                        input.seek(index12_4);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA12_5 = input.LA(1);

                         
                        int index12_5 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((defaultFieldConjunction())) ) {s = 13;}

                        else if ( (true) ) {s = 14;}

                         
                        input.seek(index12_5);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA12_6 = input.LA(1);

                         
                        int index12_6 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((defaultFieldConjunction())) ) {s = 13;}

                        else if ( (true) ) {s = 14;}

                         
                        input.seek(index12_6);
                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA12_7 = input.LA(1);

                         
                        int index12_7 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((defaultFieldConjunction())) ) {s = 13;}

                        else if ( (true) ) {s = 14;}

                         
                        input.seek(index12_7);
                        if ( s>=0 ) return s;
                        break;
                    case 7 : 
                        int LA12_8 = input.LA(1);

                         
                        int index12_8 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((defaultFieldConjunction())) ) {s = 13;}

                        else if ( (true) ) {s = 14;}

                         
                        input.seek(index12_8);
                        if ( s>=0 ) return s;
                        break;
                    case 8 : 
                        int LA12_9 = input.LA(1);

                         
                        int index12_9 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((defaultFieldConjunction())) ) {s = 13;}

                        else if ( (true) ) {s = 14;}

                         
                        input.seek(index12_9);
                        if ( s>=0 ) return s;
                        break;
                    case 9 : 
                        int LA12_10 = input.LA(1);

                         
                        int index12_10 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((defaultFieldConjunction())) ) {s = 13;}

                        else if ( (true) ) {s = 14;}

                         
                        input.seek(index12_10);
                        if ( s>=0 ) return s;
                        break;
                    case 10 : 
                        int LA12_11 = input.LA(1);

                         
                        int index12_11 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((defaultFieldConjunction())) ) {s = 13;}

                        else if ( (true) ) {s = 14;}

                         
                        input.seek(index12_11);
                        if ( s>=0 ) return s;
                        break;
                    case 11 : 
                        int LA12_12 = input.LA(1);

                         
                        int index12_12 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((defaultFieldConjunction())) ) {s = 13;}

                        else if ( (true) ) {s = 14;}

                         
                        input.seek(index12_12);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 12, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA13_eotS =
        "\20\uffff";
    static final String DFA13_eofS =
        "\20\uffff";
    static final String DFA13_minS =
        "\1\41\6\uffff\1\0\3\uffff\1\0\4\uffff";
    static final String DFA13_maxS =
        "\1\66\6\uffff\1\0\3\uffff\1\0\4\uffff";
    static final String DFA13_acceptS =
        "\1\uffff\1\2\15\uffff\1\1";
    static final String DFA13_specialS =
        "\7\uffff\1\0\3\uffff\1\1\4\uffff}>";
    static final String[] DFA13_transitionS = {
            "\1\1\1\13\3\1\1\uffff\3\1\2\uffff\2\1\2\uffff\2\1\1\7\2\1\1"+
            "\uffff\1\1",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\uffff",
            "",
            "",
            "",
            "\1\uffff",
            "",
            "",
            "",
            ""
    };

    static final short[] DFA13_eot = DFA.unpackEncodedString(DFA13_eotS);
    static final short[] DFA13_eof = DFA.unpackEncodedString(DFA13_eofS);
    static final char[] DFA13_min = DFA.unpackEncodedStringToUnsignedChars(DFA13_minS);
    static final char[] DFA13_max = DFA.unpackEncodedStringToUnsignedChars(DFA13_maxS);
    static final short[] DFA13_accept = DFA.unpackEncodedString(DFA13_acceptS);
    static final short[] DFA13_special = DFA.unpackEncodedString(DFA13_specialS);
    static final short[][] DFA13_transition;

    static {
        int numStates = DFA13_transitionS.length;
        DFA13_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA13_transition[i] = DFA.unpackEncodedString(DFA13_transitionS[i]);
        }
    }

    class DFA13 extends DFA {

        public DFA13(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 13;
            this.eot = DFA13_eot;
            this.eof = DFA13_eof;
            this.min = DFA13_min;
            this.max = DFA13_max;
            this.accept = DFA13_accept;
            this.special = DFA13_special;
            this.transition = DFA13_transition;
        }
        public String getDescription() {
            return "()* loopback of 231:36: ( ( or )=> or ftsFieldGroupExplictConjunction )*";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA13_7 = input.LA(1);

                         
                        int index13_7 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_FTS()) ) {s = 15;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index13_7);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA13_11 = input.LA(1);

                         
                        int index13_11 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_FTS()) ) {s = 15;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index13_11);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 13, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA14_eotS =
        "\21\uffff";
    static final String DFA14_eofS =
        "\21\uffff";
    static final String DFA14_minS =
        "\1\41\10\uffff\1\0\7\uffff";
    static final String DFA14_maxS =
        "\1\66\10\uffff\1\0\7\uffff";
    static final String DFA14_acceptS =
        "\1\uffff\1\2\16\uffff\1\1";
    static final String DFA14_specialS =
        "\1\0\10\uffff\1\1\7\uffff}>";
    static final String[] DFA14_transitionS = {
            "\5\1\1\uffff\3\1\2\uffff\2\1\2\uffff\3\1\1\11\1\1\1\20\1\1",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\uffff",
            "",
            "",
            "",
            "",
            "",
            "",
            ""
    };

    static final short[] DFA14_eot = DFA.unpackEncodedString(DFA14_eotS);
    static final short[] DFA14_eof = DFA.unpackEncodedString(DFA14_eofS);
    static final char[] DFA14_min = DFA.unpackEncodedStringToUnsignedChars(DFA14_minS);
    static final char[] DFA14_max = DFA.unpackEncodedStringToUnsignedChars(DFA14_maxS);
    static final short[] DFA14_accept = DFA.unpackEncodedString(DFA14_acceptS);
    static final short[] DFA14_special = DFA.unpackEncodedString(DFA14_specialS);
    static final short[][] DFA14_transition;

    static {
        int numStates = DFA14_transitionS.length;
        DFA14_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA14_transition[i] = DFA.unpackEncodedString(DFA14_transitionS[i]);
        }
    }

    class DFA14 extends DFA {

        public DFA14(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 14;
            this.eot = DFA14_eot;
            this.eof = DFA14_eof;
            this.min = DFA14_min;
            this.max = DFA14_max;
            this.accept = DFA14_accept;
            this.special = DFA14_special;
            this.transition = DFA14_transition;
        }
        public String getDescription() {
            return "()* loopback of 236:26: ( ( and )=> and ftsFieldGroupPrefixed )*";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA14_0 = input.LA(1);

                         
                        int index14_0 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((LA14_0>=PLUS && LA14_0<=RPAREN)||(LA14_0>=EQUALS && LA14_0<=TILDA)||(LA14_0>=TO && LA14_0<=LCURL)||(LA14_0>=ID && LA14_0<=OR)||LA14_0==NOT||LA14_0==EXCLAMATION) ) {s = 1;}

                        else if ( (LA14_0==AND) ) {s = 9;}

                        else if ( (LA14_0==AMP) && (synpred5_FTS())) {s = 16;}

                         
                        input.seek(index14_0);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA14_9 = input.LA(1);

                         
                        int index14_9 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred5_FTS()) ) {s = 16;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index14_9);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 14, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA15_eotS =
        "\15\uffff";
    static final String DFA15_eofS =
        "\15\uffff";
    static final String DFA15_minS =
        "\1\41\1\0\13\uffff";
    static final String DFA15_maxS =
        "\1\66\1\0\13\uffff";
    static final String DFA15_acceptS =
        "\2\uffff\1\1\1\2\6\uffff\1\3\1\4\1\5";
    static final String DFA15_specialS =
        "\1\0\1\1\13\uffff}>";
    static final String[] DFA15_transitionS = {
            "\1\12\1\13\1\14\1\3\2\uffff\3\3\2\uffff\2\3\2\uffff\4\3\1\1"+
            "\1\uffff\1\2",
            "\1\uffff",
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
            ""
    };

    static final short[] DFA15_eot = DFA.unpackEncodedString(DFA15_eotS);
    static final short[] DFA15_eof = DFA.unpackEncodedString(DFA15_eofS);
    static final char[] DFA15_min = DFA.unpackEncodedStringToUnsignedChars(DFA15_minS);
    static final char[] DFA15_max = DFA.unpackEncodedStringToUnsignedChars(DFA15_maxS);
    static final short[] DFA15_accept = DFA.unpackEncodedString(DFA15_acceptS);
    static final short[] DFA15_special = DFA.unpackEncodedString(DFA15_specialS);
    static final short[][] DFA15_transition;

    static {
        int numStates = DFA15_transitionS.length;
        DFA15_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA15_transition[i] = DFA.unpackEncodedString(DFA15_transitionS[i]);
        }
    }

    class DFA15 extends DFA {

        public DFA15(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 15;
            this.eot = DFA15_eot;
            this.eof = DFA15_eof;
            this.min = DFA15_min;
            this.max = DFA15_max;
            this.accept = DFA15_accept;
            this.special = DFA15_special;
            this.transition = DFA15_transition;
        }
        public String getDescription() {
            return "241:1: ftsFieldGroupPrefixed : ( ( not )=> not ftsFieldGroupTest -> ^( FIELD_NEGATION ftsFieldGroupTest ) | ftsFieldGroupTest -> ^( FIELD_DEFAULT ftsFieldGroupTest ) | PLUS ftsFieldGroupTest -> ^( FIELD_MANDATORY ftsFieldGroupTest ) | BAR ftsFieldGroupTest -> ^( FIELD_OPTIONAL ftsFieldGroupTest ) | MINUS ftsFieldGroupTest -> ^( FIELD_EXCLUDE ftsFieldGroupTest ) );";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA15_0 = input.LA(1);

                         
                        int index15_0 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA15_0==NOT) ) {s = 1;}

                        else if ( (LA15_0==EXCLAMATION) && (synpred6_FTS())) {s = 2;}

                        else if ( (LA15_0==LPAREN||(LA15_0>=EQUALS && LA15_0<=TILDA)||(LA15_0>=TO && LA15_0<=LCURL)||(LA15_0>=ID && LA15_0<=AND)) ) {s = 3;}

                        else if ( (LA15_0==PLUS) ) {s = 10;}

                        else if ( (LA15_0==BAR) ) {s = 11;}

                        else if ( (LA15_0==MINUS) ) {s = 12;}

                         
                        input.seek(index15_0);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA15_1 = input.LA(1);

                         
                        int index15_1 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_FTS()) ) {s = 2;}

                        else if ( (true) ) {s = 3;}

                         
                        input.seek(index15_1);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 15, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA16_eotS =
        "\22\uffff";
    static final String DFA16_eofS =
        "\22\uffff";
    static final String DFA16_minS =
        "\1\44\1\41\1\uffff\1\41\1\uffff\2\41\5\uffff\5\41\1\uffff";
    static final String DFA16_maxS =
        "\1\64\1\66\1\uffff\1\66\1\uffff\2\66\5\uffff\5\66\1\uffff";
    static final String DFA16_acceptS =
        "\2\uffff\1\2\1\uffff\1\4\2\uffff\1\6\1\1\1\5\1\3\1\7\5\uffff\1"+
        "\6";
    static final String DFA16_specialS =
        "\22\uffff}>";
    static final String[] DFA16_transitionS = {
            "\1\6\2\uffff\1\2\1\3\1\4\2\uffff\1\5\1\7\2\uffff\2\1\3\5",
            "\5\10\1\uffff\3\10\1\11\1\7\2\10\2\uffff\7\10",
            "",
            "\5\12\1\uffff\3\12\1\uffff\1\7\2\12\2\uffff\7\12",
            "",
            "\5\10\1\uffff\3\10\1\11\1\uffff\2\10\2\uffff\7\10",
            "\4\13\2\uffff\1\13\1\15\1\13\2\uffff\2\13\2\uffff\2\14\3\13"+
            "\1\uffff\1\13",
            "",
            "",
            "",
            "",
            "",
            "\5\13\1\uffff\5\13\1\16\1\13\2\uffff\7\13",
            "\5\13\1\uffff\3\13\1\uffff\1\13\1\16\1\13\2\uffff\7\13",
            "\5\13\1\uffff\1\13\1\20\2\13\1\uffff\2\13\2\uffff\2\17\5\13",
            "\4\13\1\21\1\uffff\7\13\1\21\1\uffff\7\13",
            "\4\13\1\21\1\uffff\3\13\1\uffff\3\13\1\21\1\uffff\7\13",
            ""
    };

    static final short[] DFA16_eot = DFA.unpackEncodedString(DFA16_eotS);
    static final short[] DFA16_eof = DFA.unpackEncodedString(DFA16_eofS);
    static final char[] DFA16_min = DFA.unpackEncodedStringToUnsignedChars(DFA16_minS);
    static final char[] DFA16_max = DFA.unpackEncodedStringToUnsignedChars(DFA16_maxS);
    static final short[] DFA16_accept = DFA.unpackEncodedString(DFA16_acceptS);
    static final short[] DFA16_special = DFA.unpackEncodedString(DFA16_specialS);
    static final short[][] DFA16_transition;

    static {
        int numStates = DFA16_transitionS.length;
        DFA16_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA16_transition[i] = DFA.unpackEncodedString(DFA16_transitionS[i]);
        }
    }

    class DFA16 extends DFA {

        public DFA16(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 16;
            this.eot = DFA16_eot;
            this.eof = DFA16_eof;
            this.min = DFA16_min;
            this.max = DFA16_max;
            this.accept = DFA16_accept;
            this.special = DFA16_special;
            this.transition = DFA16_transition;
        }
        public String getDescription() {
            return "255:1: ftsFieldGroupTest : ( ftsFieldGroupTerm -> ^( FG_TERM ftsFieldGroupTerm ) | ftsFieldGroupExactTerm -> ^( FG_EXACT_TERM ftsFieldGroupExactTerm ) | ftsFieldGroupPhrase -> ^( FG_PHRASE ftsFieldGroupPhrase ) | ftsFieldGroupSynonym -> ^( FG_SYNONYM ftsFieldGroupSynonym ) | ftsFieldGroupProximity -> ^( FG_PROXIMITY ftsFieldGroupProximity ) | ftsFieldGroupRange -> ^( FG_RANGE ftsFieldGroupRange ) | LPAREN ftsFieldGroupImplicitConjunctionOrDisjunction RPAREN -> ftsFieldGroupImplicitConjunctionOrDisjunction );";
        }
    }
 

    public static final BitSet FOLLOW_ftsImplicitConjunctionOrDisjunction_in_ftsQuery190 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_ftsQuery192 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ftsExplicitDisjunction_in_ftsImplicitConjunctionOrDisjunction214 = new BitSet(new long[]{0x005F339E00000002L});
    public static final BitSet FOLLOW_ftsExplicitDisjunction_in_ftsImplicitConjunctionOrDisjunction217 = new BitSet(new long[]{0x005F339E00000002L});
    public static final BitSet FOLLOW_ftsExplicitDisjunction_in_ftsImplicitConjunctionOrDisjunction236 = new BitSet(new long[]{0x005F339E00000002L});
    public static final BitSet FOLLOW_ftsExplicitDisjunction_in_ftsImplicitConjunctionOrDisjunction239 = new BitSet(new long[]{0x005F339E00000002L});
    public static final BitSet FOLLOW_ftsExplictConjunction_in_ftsExplicitDisjunction264 = new BitSet(new long[]{0x0004000400000002L});
    public static final BitSet FOLLOW_or_in_ftsExplicitDisjunction273 = new BitSet(new long[]{0x005F339E00000000L});
    public static final BitSet FOLLOW_ftsExplictConjunction_in_ftsExplicitDisjunction275 = new BitSet(new long[]{0x0004000400000002L});
    public static final BitSet FOLLOW_ftsPrefixed_in_ftsExplictConjunction300 = new BitSet(new long[]{0x0028000000000002L});
    public static final BitSet FOLLOW_and_in_ftsExplictConjunction309 = new BitSet(new long[]{0x005F339E00000000L});
    public static final BitSet FOLLOW_ftsPrefixed_in_ftsExplictConjunction311 = new BitSet(new long[]{0x0028000000000002L});
    public static final BitSet FOLLOW_not_in_ftsPrefixed349 = new BitSet(new long[]{0x001F339000000000L});
    public static final BitSet FOLLOW_ftsTest_in_ftsPrefixed351 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ftsTest_in_ftsPrefixed369 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_PLUS_in_ftsPrefixed389 = new BitSet(new long[]{0x001F339000000000L});
    public static final BitSet FOLLOW_ftsTest_in_ftsPrefixed391 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_BAR_in_ftsPrefixed425 = new BitSet(new long[]{0x001F339000000000L});
    public static final BitSet FOLLOW_ftsTest_in_ftsPrefixed427 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_MINUS_in_ftsPrefixed461 = new BitSet(new long[]{0x001F339000000000L});
    public static final BitSet FOLLOW_ftsTest_in_ftsPrefixed463 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ftsTerm_in_ftsTest505 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ftsExactTerm_in_ftsTest520 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ftsPhrase_in_ftsTest540 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ftsSynonym_in_ftsTest566 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ftsFieldGroupProximity_in_ftsTest590 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ftsFieldGroupRange_in_ftsTest617 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ftsFieldGroup_in_ftsTest641 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_ftsTest650 = new BitSet(new long[]{0x005F339E00000000L});
    public static final BitSet FOLLOW_ftsImplicitConjunctionOrDisjunction_in_ftsTest652 = new BitSet(new long[]{0x0000002000000000L});
    public static final BitSet FOLLOW_RPAREN_in_ftsTest654 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_columnReference_in_ftsTerm672 = new BitSet(new long[]{0x0000004000000000L});
    public static final BitSet FOLLOW_COLON_in_ftsTerm674 = new BitSet(new long[]{0x001F100000000000L});
    public static final BitSet FOLLOW_ftsWord_in_ftsTerm678 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_EQUALS_in_ftsExactTerm699 = new BitSet(new long[]{0x001F100000000000L});
    public static final BitSet FOLLOW_ftsTerm_in_ftsExactTerm701 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_columnReference_in_ftsPhrase722 = new BitSet(new long[]{0x0000004000000000L});
    public static final BitSet FOLLOW_COLON_in_ftsPhrase724 = new BitSet(new long[]{0x0000010000000000L});
    public static final BitSet FOLLOW_FTSPHRASE_in_ftsPhrase728 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TILDA_in_ftsSynonym749 = new BitSet(new long[]{0x001F100000000000L});
    public static final BitSet FOLLOW_ftsTerm_in_ftsSynonym751 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_columnReference_in_ftsFieldGroup770 = new BitSet(new long[]{0x0000004000000000L});
    public static final BitSet FOLLOW_COLON_in_ftsFieldGroup772 = new BitSet(new long[]{0x0000001000000000L});
    public static final BitSet FOLLOW_LPAREN_in_ftsFieldGroup774 = new BitSet(new long[]{0x005F339E00000000L});
    public static final BitSet FOLLOW_ftsFieldGroupImplicitConjunctionOrDisjunction_in_ftsFieldGroup776 = new BitSet(new long[]{0x0000002000000000L});
    public static final BitSet FOLLOW_RPAREN_in_ftsFieldGroup778 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ftsFieldGroupExplicitDisjunction_in_ftsFieldGroupImplicitConjunctionOrDisjunction804 = new BitSet(new long[]{0x005F339E00000002L});
    public static final BitSet FOLLOW_ftsFieldGroupExplicitDisjunction_in_ftsFieldGroupImplicitConjunctionOrDisjunction807 = new BitSet(new long[]{0x005F339E00000002L});
    public static final BitSet FOLLOW_ftsFieldGroupExplicitDisjunction_in_ftsFieldGroupImplicitConjunctionOrDisjunction825 = new BitSet(new long[]{0x005F339E00000002L});
    public static final BitSet FOLLOW_ftsFieldGroupExplicitDisjunction_in_ftsFieldGroupImplicitConjunctionOrDisjunction828 = new BitSet(new long[]{0x005F339E00000002L});
    public static final BitSet FOLLOW_ftsFieldGroupExplictConjunction_in_ftsFieldGroupExplicitDisjunction853 = new BitSet(new long[]{0x0004000400000002L});
    public static final BitSet FOLLOW_or_in_ftsFieldGroupExplicitDisjunction862 = new BitSet(new long[]{0x005F339E00000000L});
    public static final BitSet FOLLOW_ftsFieldGroupExplictConjunction_in_ftsFieldGroupExplicitDisjunction864 = new BitSet(new long[]{0x0004000400000002L});
    public static final BitSet FOLLOW_ftsFieldGroupPrefixed_in_ftsFieldGroupExplictConjunction889 = new BitSet(new long[]{0x0028000000000002L});
    public static final BitSet FOLLOW_and_in_ftsFieldGroupExplictConjunction898 = new BitSet(new long[]{0x005F339E00000000L});
    public static final BitSet FOLLOW_ftsFieldGroupPrefixed_in_ftsFieldGroupExplictConjunction900 = new BitSet(new long[]{0x0028000000000002L});
    public static final BitSet FOLLOW_not_in_ftsFieldGroupPrefixed934 = new BitSet(new long[]{0x001F339000000000L});
    public static final BitSet FOLLOW_ftsFieldGroupTest_in_ftsFieldGroupPrefixed936 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ftsFieldGroupTest_in_ftsFieldGroupPrefixed954 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_PLUS_in_ftsFieldGroupPrefixed974 = new BitSet(new long[]{0x001F339000000000L});
    public static final BitSet FOLLOW_ftsFieldGroupTest_in_ftsFieldGroupPrefixed976 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_BAR_in_ftsFieldGroupPrefixed1010 = new BitSet(new long[]{0x001F339000000000L});
    public static final BitSet FOLLOW_ftsFieldGroupTest_in_ftsFieldGroupPrefixed1012 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_MINUS_in_ftsFieldGroupPrefixed1046 = new BitSet(new long[]{0x001F339000000000L});
    public static final BitSet FOLLOW_ftsFieldGroupTest_in_ftsFieldGroupPrefixed1048 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ftsFieldGroupTerm_in_ftsFieldGroupTest1087 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ftsFieldGroupExactTerm_in_ftsFieldGroupTest1102 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ftsFieldGroupPhrase_in_ftsFieldGroupTest1118 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ftsFieldGroupSynonym_in_ftsFieldGroupTest1133 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ftsFieldGroupProximity_in_ftsFieldGroupTest1150 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ftsFieldGroupRange_in_ftsFieldGroupTest1171 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_ftsFieldGroupTest1192 = new BitSet(new long[]{0x005F339E00000000L});
    public static final BitSet FOLLOW_ftsFieldGroupImplicitConjunctionOrDisjunction_in_ftsFieldGroupTest1194 = new BitSet(new long[]{0x0000002000000000L});
    public static final BitSet FOLLOW_RPAREN_in_ftsFieldGroupTest1196 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ftsWord_in_ftsFieldGroupTerm1214 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_EQUALS_in_ftsFieldGroupExactTerm1226 = new BitSet(new long[]{0x001F100000000000L});
    public static final BitSet FOLLOW_ftsFieldGroupTerm_in_ftsFieldGroupExactTerm1228 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FTSPHRASE_in_ftsFieldGroupPhrase1248 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TILDA_in_ftsFieldGroupSynonym1260 = new BitSet(new long[]{0x001F100000000000L});
    public static final BitSet FOLLOW_ftsFieldGroupTerm_in_ftsFieldGroupSynonym1262 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ftsFieldGroupTerm_in_ftsFieldGroupProximity1280 = new BitSet(new long[]{0x0000040000000000L});
    public static final BitSet FOLLOW_STAR_in_ftsFieldGroupProximity1282 = new BitSet(new long[]{0x001F100000000000L});
    public static final BitSet FOLLOW_ftsFieldGroupTerm_in_ftsFieldGroupProximity1284 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ftsRangeWord_in_ftsFieldGroupRange1311 = new BitSet(new long[]{0x0000080000000000L});
    public static final BitSet FOLLOW_DOTDOT_in_ftsFieldGroupRange1313 = new BitSet(new long[]{0x0003010000000000L});
    public static final BitSet FOLLOW_ftsRangeWord_in_ftsFieldGroupRange1315 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_range_left_in_ftsFieldGroupRange1332 = new BitSet(new long[]{0x0003010000000000L});
    public static final BitSet FOLLOW_ftsRangeWord_in_ftsFieldGroupRange1334 = new BitSet(new long[]{0x0000100000000000L});
    public static final BitSet FOLLOW_TO_in_ftsFieldGroupRange1336 = new BitSet(new long[]{0x0003010000000000L});
    public static final BitSet FOLLOW_ftsRangeWord_in_ftsFieldGroupRange1338 = new BitSet(new long[]{0x0000402000000000L});
    public static final BitSet FOLLOW_range_right_in_ftsFieldGroupRange1340 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_range_left1370 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LCURL_in_range_left1381 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RPAREN_in_range_right1405 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RCURL_in_range_right1416 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_identifier_in_columnReference1441 = new BitSet(new long[]{0x0000800000000000L});
    public static final BitSet FOLLOW_DOT_in_columnReference1443 = new BitSet(new long[]{0x0001000000000000L});
    public static final BitSet FOLLOW_identifier_in_columnReference1450 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_identifier1478 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_ftsWord0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_ftsRangeWord0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_OR_in_or1608 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_BAR_in_or1616 = new BitSet(new long[]{0x0000000400000000L});
    public static final BitSet FOLLOW_BAR_in_or1618 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_AND_in_and1640 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_AMP_in_and1648 = new BitSet(new long[]{0x0020000000000000L});
    public static final BitSet FOLLOW_AMP_in_and1650 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_not0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_or_in_synpred1_FTS268 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_and_in_synpred2_FTS304 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_not_in_synpred3_FTS344 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_or_in_synpred4_FTS857 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_and_in_synpred5_FTS893 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_not_in_synpred6_FTS929 = new BitSet(new long[]{0x0000000000000002L});

}