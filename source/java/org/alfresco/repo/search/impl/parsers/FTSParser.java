// $ANTLR !Unknown version! W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g 2009-10-28 12:52:46
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
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "FTS", "DISJUNCTION", "CONJUNCTION", "NEGATION", "TERM", "EXACT_TERM", "PHRASE", "SYNONYM", "RANGE", "PROXIMITY", "DEFAULT", "MANDATORY", "OPTIONAL", "EXCLUDE", "FIELD_DISJUNCTION", "FIELD_CONJUNCTION", "FIELD_NEGATION", "FIELD_GROUP", "FIELD_DEFAULT", "FIELD_MANDATORY", "FIELD_OPTIONAL", "FIELD_EXCLUDE", "FG_TERM", "FG_EXACT_TERM", "FG_PHRASE", "FG_SYNONYM", "FG_PROXIMITY", "FG_RANGE", "FIELD_REF", "INCLUSIVE", "EXCLUSIVE", "QUALIFIER", "PREFIX", "NAME_SPACE", "BOOST", "FUZZY", "TEMPLATE", "PLUS", "BAR", "MINUS", "LPAREN", "RPAREN", "PERCENT", "COMMA", "TILDA", "DECIMAL_INTEGER_LITERAL", "CARAT", "COLON", "EQUALS", "FTSPHRASE", "STAR", "DOTDOT", "TO", "LSQUARE", "LT", "RSQUARE", "GT", "AT", "URI", "ID", "FTSWORD", "FTSPRE", "FTSWILD", "OR", "AND", "NOT", "FLOATING_POINT_LITERAL", "AMP", "EXCLAMATION", "F_ESC", "F_URI_ALPHA", "F_URI_DIGIT", "F_URI_OTHER", "F_HEX", "F_URI_ESC", "DOT", "QUESTION_MARK", "LCURL", "RCURL", "DOLLAR", "DECIMAL_NUMERAL", "INWORD", "START_RANGE_I", "START_RANGE_F", "DIGIT", "EXPONENT", "ZERO_DIGIT", "NON_ZERO_DIGIT", "E", "SIGNED_INTEGER", "WS"
    };
    public static final int PREFIX=36;
    public static final int LT=58;
    public static final int EXPONENT=89;
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
    public static final int FLOATING_POINT_LITERAL=70;
    public static final int EXCLAMATION=72;
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
    public static final int WS=94;
    public static final int FG_SYNONYM=29;
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
    public String getGrammarFileName() { return "W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g"; }


        private Stack<String> paraphrases = new Stack<String>();
       
        private boolean defaultConjunction = true;
        
        private boolean defaultFieldConjunction = true;
       
        public boolean defaultConjunction()
        {
           return defaultConjunction;
        }
        
        public boolean defaultFieldConjunction()
        {
           return defaultFieldConjunction;
        }
        
        public void setDefaultConjunction(boolean defaultConjunction)
        {
           this.defaultConjunction = defaultConjunction;
        }
        
        public void setDefaultFieldConjunction(boolean defaultFieldConjunction)
        {
           this.defaultFieldConjunction = defaultFieldConjunction;
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
                msg = "Syntax error at line "+nvae.line+ " position "+nvae.charPositionInLine+" at token " + nvae.token.getText() + 
                 "\n\tNo viable alternative: token="+e.token+
                 "\n\t\tDecision No ="+nvae.decisionNumber+
                 "\n\t\tState No "+nvae.stateNumber+
                 "\n\t\tDecision=<<"+nvae.grammarDecisionDescription+">>";
           }
           else
           {
               msg = super.getErrorMessage(e, tokenNames);
           }
           if(paraphrases.size() > 0)
           {
               String paraphrase = (String)paraphrases.peek();
               msg = msg+"\n\t\tParaphrase: "+paraphrase;
           }
           
           return msg+"\n\tStack\n\t\t"+stack;
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
    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:210:1: ftsQuery : ftsImplicitConjunctionOrDisjunction EOF -> ftsImplicitConjunctionOrDisjunction ;
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
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:211:6: ( ftsImplicitConjunctionOrDisjunction EOF -> ftsImplicitConjunctionOrDisjunction )
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:211:9: ftsImplicitConjunctionOrDisjunction EOF
            {
            pushFollow(FOLLOW_ftsImplicitConjunctionOrDisjunction_in_ftsQuery258);
            ftsImplicitConjunctionOrDisjunction1=ftsImplicitConjunctionOrDisjunction();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_ftsImplicitConjunctionOrDisjunction.add(ftsImplicitConjunctionOrDisjunction1.getTree());
            EOF2=(Token)match(input,EOF,FOLLOW_EOF_in_ftsQuery260); if (state.failed) return retval; 
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
            // 212:3: -> ftsImplicitConjunctionOrDisjunction
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
    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:218:1: ftsImplicitConjunctionOrDisjunction : ({...}? ftsExplicitDisjunction ( ftsExplicitDisjunction )* -> ^( CONJUNCTION ( ftsExplicitDisjunction )+ ) | ftsExplicitDisjunction ( ftsExplicitDisjunction )* -> ^( DISJUNCTION ( ftsExplicitDisjunction )+ ) );
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
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:219:2: ({...}? ftsExplicitDisjunction ( ftsExplicitDisjunction )* -> ^( CONJUNCTION ( ftsExplicitDisjunction )+ ) | ftsExplicitDisjunction ( ftsExplicitDisjunction )* -> ^( DISJUNCTION ( ftsExplicitDisjunction )+ ) )
            int alt3=2;
            alt3 = dfa3.predict(input);
            switch (alt3) {
                case 1 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:219:4: {...}? ftsExplicitDisjunction ( ftsExplicitDisjunction )*
                    {
                    if ( !((defaultConjunction())) ) {
                        if (state.backtracking>0) {state.failed=true; return retval;}
                        throw new FailedPredicateException(input, "ftsImplicitConjunctionOrDisjunction", "defaultConjunction()");
                    }
                    pushFollow(FOLLOW_ftsExplicitDisjunction_in_ftsImplicitConjunctionOrDisjunction284);
                    ftsExplicitDisjunction3=ftsExplicitDisjunction();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_ftsExplicitDisjunction.add(ftsExplicitDisjunction3.getTree());
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:219:51: ( ftsExplicitDisjunction )*
                    loop1:
                    do {
                        int alt1=2;
                        int LA1_0 = input.LA(1);

                        if ( ((LA1_0>=PLUS && LA1_0<=LPAREN)||LA1_0==PERCENT||(LA1_0>=TILDA && LA1_0<=DECIMAL_INTEGER_LITERAL)||(LA1_0>=EQUALS && LA1_0<=FTSPHRASE)||(LA1_0>=TO && LA1_0<=LT)||(LA1_0>=AT && LA1_0<=FLOATING_POINT_LITERAL)||LA1_0==EXCLAMATION) ) {
                            alt1=1;
                        }


                        switch (alt1) {
                    	case 1 :
                    	    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:219:52: ftsExplicitDisjunction
                    	    {
                    	    pushFollow(FOLLOW_ftsExplicitDisjunction_in_ftsImplicitConjunctionOrDisjunction287);
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
                    // 220:3: -> ^( CONJUNCTION ( ftsExplicitDisjunction )+ )
                    {
                        // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:220:6: ^( CONJUNCTION ( ftsExplicitDisjunction )+ )
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
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:221:5: ftsExplicitDisjunction ( ftsExplicitDisjunction )*
                    {
                    pushFollow(FOLLOW_ftsExplicitDisjunction_in_ftsImplicitConjunctionOrDisjunction306);
                    ftsExplicitDisjunction5=ftsExplicitDisjunction();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_ftsExplicitDisjunction.add(ftsExplicitDisjunction5.getTree());
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:221:28: ( ftsExplicitDisjunction )*
                    loop2:
                    do {
                        int alt2=2;
                        int LA2_0 = input.LA(1);

                        if ( ((LA2_0>=PLUS && LA2_0<=LPAREN)||LA2_0==PERCENT||(LA2_0>=TILDA && LA2_0<=DECIMAL_INTEGER_LITERAL)||(LA2_0>=EQUALS && LA2_0<=FTSPHRASE)||(LA2_0>=TO && LA2_0<=LT)||(LA2_0>=AT && LA2_0<=FLOATING_POINT_LITERAL)||LA2_0==EXCLAMATION) ) {
                            alt2=1;
                        }


                        switch (alt2) {
                    	case 1 :
                    	    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:221:29: ftsExplicitDisjunction
                    	    {
                    	    pushFollow(FOLLOW_ftsExplicitDisjunction_in_ftsImplicitConjunctionOrDisjunction309);
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
                    // 222:3: -> ^( DISJUNCTION ( ftsExplicitDisjunction )+ )
                    {
                        // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:222:6: ^( DISJUNCTION ( ftsExplicitDisjunction )+ )
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
    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:229:1: ftsExplicitDisjunction : ftsExplictConjunction ( ( or )=> or ftsExplictConjunction )* -> ^( DISJUNCTION ( ftsExplictConjunction )+ ) ;
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
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:230:2: ( ftsExplictConjunction ( ( or )=> or ftsExplictConjunction )* -> ^( DISJUNCTION ( ftsExplictConjunction )+ ) )
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:230:4: ftsExplictConjunction ( ( or )=> or ftsExplictConjunction )*
            {
            pushFollow(FOLLOW_ftsExplictConjunction_in_ftsExplicitDisjunction336);
            ftsExplictConjunction7=ftsExplictConjunction();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_ftsExplictConjunction.add(ftsExplictConjunction7.getTree());
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:230:26: ( ( or )=> or ftsExplictConjunction )*
            loop4:
            do {
                int alt4=2;
                alt4 = dfa4.predict(input);
                switch (alt4) {
            	case 1 :
            	    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:230:27: ( or )=> or ftsExplictConjunction
            	    {
            	    pushFollow(FOLLOW_or_in_ftsExplicitDisjunction345);
            	    or8=or();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_or.add(or8.getTree());
            	    pushFollow(FOLLOW_ftsExplictConjunction_in_ftsExplicitDisjunction347);
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
            // 231:3: -> ^( DISJUNCTION ( ftsExplictConjunction )+ )
            {
                // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:231:6: ^( DISJUNCTION ( ftsExplictConjunction )+ )
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
    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:237:1: ftsExplictConjunction : ftsPrefixed ( ( and )=> and ftsPrefixed )* -> ^( CONJUNCTION ( ftsPrefixed )+ ) ;
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
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:238:2: ( ftsPrefixed ( ( and )=> and ftsPrefixed )* -> ^( CONJUNCTION ( ftsPrefixed )+ ) )
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:238:4: ftsPrefixed ( ( and )=> and ftsPrefixed )*
            {
            pushFollow(FOLLOW_ftsPrefixed_in_ftsExplictConjunction374);
            ftsPrefixed10=ftsPrefixed();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_ftsPrefixed.add(ftsPrefixed10.getTree());
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:238:16: ( ( and )=> and ftsPrefixed )*
            loop5:
            do {
                int alt5=2;
                alt5 = dfa5.predict(input);
                switch (alt5) {
            	case 1 :
            	    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:238:17: ( and )=> and ftsPrefixed
            	    {
            	    pushFollow(FOLLOW_and_in_ftsExplictConjunction383);
            	    and11=and();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_and.add(and11.getTree());
            	    pushFollow(FOLLOW_ftsPrefixed_in_ftsExplictConjunction385);
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
            // 239:3: -> ^( CONJUNCTION ( ftsPrefixed )+ )
            {
                // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:239:6: ^( CONJUNCTION ( ftsPrefixed )+ )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(CONJUNCTION, "CONJUNCTION"), root_1);

                if ( !(stream_ftsPrefixed.hasNext()) ) {
                    throw new RewriteEarlyExitException();
                }
                while ( stream_ftsPrefixed.hasNext() ) {
                    adaptor.addChild(root_1, stream_ftsPrefixed.nextTree());

                }
                stream_ftsPrefixed.reset();

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
    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:248:1: ftsPrefixed : ( ( not )=> not ftsTest ( boost )? -> ^( NEGATION ftsTest ( boost )? ) | ftsTest ( boost )? -> ^( DEFAULT ftsTest ( boost )? ) | PLUS ftsTest ( boost )? -> ^( MANDATORY ftsTest ( boost )? ) | BAR ftsTest ( boost )? -> ^( OPTIONAL ftsTest ( boost )? ) | MINUS ftsTest ( boost )? -> ^( EXCLUDE ftsTest ( boost )? ) );
    public final FTSParser.ftsPrefixed_return ftsPrefixed() throws RecognitionException {
        FTSParser.ftsPrefixed_return retval = new FTSParser.ftsPrefixed_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token PLUS18=null;
        Token BAR21=null;
        Token MINUS24=null;
        FTSParser.not_return not13 = null;

        FTSParser.ftsTest_return ftsTest14 = null;

        FTSParser.boost_return boost15 = null;

        FTSParser.ftsTest_return ftsTest16 = null;

        FTSParser.boost_return boost17 = null;

        FTSParser.ftsTest_return ftsTest19 = null;

        FTSParser.boost_return boost20 = null;

        FTSParser.ftsTest_return ftsTest22 = null;

        FTSParser.boost_return boost23 = null;

        FTSParser.ftsTest_return ftsTest25 = null;

        FTSParser.boost_return boost26 = null;


        Object PLUS18_tree=null;
        Object BAR21_tree=null;
        Object MINUS24_tree=null;
        RewriteRuleTokenStream stream_PLUS=new RewriteRuleTokenStream(adaptor,"token PLUS");
        RewriteRuleTokenStream stream_MINUS=new RewriteRuleTokenStream(adaptor,"token MINUS");
        RewriteRuleTokenStream stream_BAR=new RewriteRuleTokenStream(adaptor,"token BAR");
        RewriteRuleSubtreeStream stream_not=new RewriteRuleSubtreeStream(adaptor,"rule not");
        RewriteRuleSubtreeStream stream_ftsTest=new RewriteRuleSubtreeStream(adaptor,"rule ftsTest");
        RewriteRuleSubtreeStream stream_boost=new RewriteRuleSubtreeStream(adaptor,"rule boost");
        try {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:249:6: ( ( not )=> not ftsTest ( boost )? -> ^( NEGATION ftsTest ( boost )? ) | ftsTest ( boost )? -> ^( DEFAULT ftsTest ( boost )? ) | PLUS ftsTest ( boost )? -> ^( MANDATORY ftsTest ( boost )? ) | BAR ftsTest ( boost )? -> ^( OPTIONAL ftsTest ( boost )? ) | MINUS ftsTest ( boost )? -> ^( EXCLUDE ftsTest ( boost )? ) )
            int alt11=5;
            alt11 = dfa11.predict(input);
            switch (alt11) {
                case 1 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:249:8: ( not )=> not ftsTest ( boost )?
                    {
                    pushFollow(FOLLOW_not_in_ftsPrefixed426);
                    not13=not();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_not.add(not13.getTree());
                    pushFollow(FOLLOW_ftsTest_in_ftsPrefixed428);
                    ftsTest14=ftsTest();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_ftsTest.add(ftsTest14.getTree());
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:249:30: ( boost )?
                    int alt6=2;
                    int LA6_0 = input.LA(1);

                    if ( (LA6_0==CARAT) ) {
                        alt6=1;
                    }
                    switch (alt6) {
                        case 1 :
                            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:249:30: boost
                            {
                            pushFollow(FOLLOW_boost_in_ftsPrefixed431);
                            boost15=boost();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_boost.add(boost15.getTree());

                            }
                            break;

                    }



                    // AST REWRITE
                    // elements: ftsTest, boost
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 250:3: -> ^( NEGATION ftsTest ( boost )? )
                    {
                        // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:250:6: ^( NEGATION ftsTest ( boost )? )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(NEGATION, "NEGATION"), root_1);

                        adaptor.addChild(root_1, stream_ftsTest.nextTree());
                        // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:250:26: ( boost )?
                        if ( stream_boost.hasNext() ) {
                            adaptor.addChild(root_1, stream_boost.nextTree());

                        }
                        stream_boost.reset();

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:251:8: ftsTest ( boost )?
                    {
                    pushFollow(FOLLOW_ftsTest_in_ftsPrefixed455);
                    ftsTest16=ftsTest();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_ftsTest.add(ftsTest16.getTree());
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:251:17: ( boost )?
                    int alt7=2;
                    int LA7_0 = input.LA(1);

                    if ( (LA7_0==CARAT) ) {
                        alt7=1;
                    }
                    switch (alt7) {
                        case 1 :
                            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:251:17: boost
                            {
                            pushFollow(FOLLOW_boost_in_ftsPrefixed458);
                            boost17=boost();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_boost.add(boost17.getTree());

                            }
                            break;

                    }



                    // AST REWRITE
                    // elements: ftsTest, boost
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 252:3: -> ^( DEFAULT ftsTest ( boost )? )
                    {
                        // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:252:6: ^( DEFAULT ftsTest ( boost )? )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(DEFAULT, "DEFAULT"), root_1);

                        adaptor.addChild(root_1, stream_ftsTest.nextTree());
                        // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:252:24: ( boost )?
                        if ( stream_boost.hasNext() ) {
                            adaptor.addChild(root_1, stream_boost.nextTree());

                        }
                        stream_boost.reset();

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 3 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:253:11: PLUS ftsTest ( boost )?
                    {
                    PLUS18=(Token)match(input,PLUS,FOLLOW_PLUS_in_ftsPrefixed484); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_PLUS.add(PLUS18);

                    pushFollow(FOLLOW_ftsTest_in_ftsPrefixed486);
                    ftsTest19=ftsTest();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_ftsTest.add(ftsTest19.getTree());
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:253:24: ( boost )?
                    int alt8=2;
                    int LA8_0 = input.LA(1);

                    if ( (LA8_0==CARAT) ) {
                        alt8=1;
                    }
                    switch (alt8) {
                        case 1 :
                            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:253:24: boost
                            {
                            pushFollow(FOLLOW_boost_in_ftsPrefixed488);
                            boost20=boost();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_boost.add(boost20.getTree());

                            }
                            break;

                    }



                    // AST REWRITE
                    // elements: boost, ftsTest
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 254:17: -> ^( MANDATORY ftsTest ( boost )? )
                    {
                        // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:254:20: ^( MANDATORY ftsTest ( boost )? )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(MANDATORY, "MANDATORY"), root_1);

                        adaptor.addChild(root_1, stream_ftsTest.nextTree());
                        // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:254:40: ( boost )?
                        if ( stream_boost.hasNext() ) {
                            adaptor.addChild(root_1, stream_boost.nextTree());

                        }
                        stream_boost.reset();

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 4 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:255:11: BAR ftsTest ( boost )?
                    {
                    BAR21=(Token)match(input,BAR,FOLLOW_BAR_in_ftsPrefixed528); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_BAR.add(BAR21);

                    pushFollow(FOLLOW_ftsTest_in_ftsPrefixed530);
                    ftsTest22=ftsTest();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_ftsTest.add(ftsTest22.getTree());
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:255:23: ( boost )?
                    int alt9=2;
                    int LA9_0 = input.LA(1);

                    if ( (LA9_0==CARAT) ) {
                        alt9=1;
                    }
                    switch (alt9) {
                        case 1 :
                            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:255:23: boost
                            {
                            pushFollow(FOLLOW_boost_in_ftsPrefixed532);
                            boost23=boost();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_boost.add(boost23.getTree());

                            }
                            break;

                    }



                    // AST REWRITE
                    // elements: ftsTest, boost
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 256:17: -> ^( OPTIONAL ftsTest ( boost )? )
                    {
                        // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:256:20: ^( OPTIONAL ftsTest ( boost )? )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(OPTIONAL, "OPTIONAL"), root_1);

                        adaptor.addChild(root_1, stream_ftsTest.nextTree());
                        // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:256:39: ( boost )?
                        if ( stream_boost.hasNext() ) {
                            adaptor.addChild(root_1, stream_boost.nextTree());

                        }
                        stream_boost.reset();

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 5 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:257:11: MINUS ftsTest ( boost )?
                    {
                    MINUS24=(Token)match(input,MINUS,FOLLOW_MINUS_in_ftsPrefixed572); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_MINUS.add(MINUS24);

                    pushFollow(FOLLOW_ftsTest_in_ftsPrefixed574);
                    ftsTest25=ftsTest();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_ftsTest.add(ftsTest25.getTree());
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:257:25: ( boost )?
                    int alt10=2;
                    int LA10_0 = input.LA(1);

                    if ( (LA10_0==CARAT) ) {
                        alt10=1;
                    }
                    switch (alt10) {
                        case 1 :
                            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:257:25: boost
                            {
                            pushFollow(FOLLOW_boost_in_ftsPrefixed576);
                            boost26=boost();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_boost.add(boost26.getTree());

                            }
                            break;

                    }



                    // AST REWRITE
                    // elements: boost, ftsTest
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 258:17: -> ^( EXCLUDE ftsTest ( boost )? )
                    {
                        // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:258:20: ^( EXCLUDE ftsTest ( boost )? )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(EXCLUDE, "EXCLUDE"), root_1);

                        adaptor.addChild(root_1, stream_ftsTest.nextTree());
                        // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:258:38: ( boost )?
                        if ( stream_boost.hasNext() ) {
                            adaptor.addChild(root_1, stream_boost.nextTree());

                        }
                        stream_boost.reset();

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
    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:264:1: ftsTest : ( ftsTerm ( ( fuzzy )=> fuzzy )? -> ^( TERM ftsTerm ( fuzzy )? ) | ftsExactTerm ( ( fuzzy )=> fuzzy )? -> ^( EXACT_TERM ftsExactTerm ( fuzzy )? ) | ftsPhrase ( ( slop )=> slop )? -> ^( PHRASE ftsPhrase ( slop )? ) | ftsSynonym ( ( fuzzy )=> fuzzy )? -> ^( SYNONYM ftsSynonym ( fuzzy )? ) | ftsFieldGroupProximity -> ^( PROXIMITY ftsFieldGroupProximity ) | ftsRange -> ^( RANGE ftsRange ) | ftsFieldGroup -> ftsFieldGroup | LPAREN ftsImplicitConjunctionOrDisjunction RPAREN -> ftsImplicitConjunctionOrDisjunction | template -> template );
    public final FTSParser.ftsTest_return ftsTest() throws RecognitionException {
        FTSParser.ftsTest_return retval = new FTSParser.ftsTest_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token LPAREN38=null;
        Token RPAREN40=null;
        FTSParser.ftsTerm_return ftsTerm27 = null;

        FTSParser.fuzzy_return fuzzy28 = null;

        FTSParser.ftsExactTerm_return ftsExactTerm29 = null;

        FTSParser.fuzzy_return fuzzy30 = null;

        FTSParser.ftsPhrase_return ftsPhrase31 = null;

        FTSParser.slop_return slop32 = null;

        FTSParser.ftsSynonym_return ftsSynonym33 = null;

        FTSParser.fuzzy_return fuzzy34 = null;

        FTSParser.ftsFieldGroupProximity_return ftsFieldGroupProximity35 = null;

        FTSParser.ftsRange_return ftsRange36 = null;

        FTSParser.ftsFieldGroup_return ftsFieldGroup37 = null;

        FTSParser.ftsImplicitConjunctionOrDisjunction_return ftsImplicitConjunctionOrDisjunction39 = null;

        FTSParser.template_return template41 = null;


        Object LPAREN38_tree=null;
        Object RPAREN40_tree=null;
        RewriteRuleTokenStream stream_RPAREN=new RewriteRuleTokenStream(adaptor,"token RPAREN");
        RewriteRuleTokenStream stream_LPAREN=new RewriteRuleTokenStream(adaptor,"token LPAREN");
        RewriteRuleSubtreeStream stream_template=new RewriteRuleSubtreeStream(adaptor,"rule template");
        RewriteRuleSubtreeStream stream_ftsFieldGroup=new RewriteRuleSubtreeStream(adaptor,"rule ftsFieldGroup");
        RewriteRuleSubtreeStream stream_ftsTerm=new RewriteRuleSubtreeStream(adaptor,"rule ftsTerm");
        RewriteRuleSubtreeStream stream_ftsExactTerm=new RewriteRuleSubtreeStream(adaptor,"rule ftsExactTerm");
        RewriteRuleSubtreeStream stream_ftsImplicitConjunctionOrDisjunction=new RewriteRuleSubtreeStream(adaptor,"rule ftsImplicitConjunctionOrDisjunction");
        RewriteRuleSubtreeStream stream_fuzzy=new RewriteRuleSubtreeStream(adaptor,"rule fuzzy");
        RewriteRuleSubtreeStream stream_ftsPhrase=new RewriteRuleSubtreeStream(adaptor,"rule ftsPhrase");
        RewriteRuleSubtreeStream stream_slop=new RewriteRuleSubtreeStream(adaptor,"rule slop");
        RewriteRuleSubtreeStream stream_ftsRange=new RewriteRuleSubtreeStream(adaptor,"rule ftsRange");
        RewriteRuleSubtreeStream stream_ftsSynonym=new RewriteRuleSubtreeStream(adaptor,"rule ftsSynonym");
        RewriteRuleSubtreeStream stream_ftsFieldGroupProximity=new RewriteRuleSubtreeStream(adaptor,"rule ftsFieldGroupProximity");
        try {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:265:6: ( ftsTerm ( ( fuzzy )=> fuzzy )? -> ^( TERM ftsTerm ( fuzzy )? ) | ftsExactTerm ( ( fuzzy )=> fuzzy )? -> ^( EXACT_TERM ftsExactTerm ( fuzzy )? ) | ftsPhrase ( ( slop )=> slop )? -> ^( PHRASE ftsPhrase ( slop )? ) | ftsSynonym ( ( fuzzy )=> fuzzy )? -> ^( SYNONYM ftsSynonym ( fuzzy )? ) | ftsFieldGroupProximity -> ^( PROXIMITY ftsFieldGroupProximity ) | ftsRange -> ^( RANGE ftsRange ) | ftsFieldGroup -> ftsFieldGroup | LPAREN ftsImplicitConjunctionOrDisjunction RPAREN -> ftsImplicitConjunctionOrDisjunction | template -> template )
            int alt16=9;
            alt16 = dfa16.predict(input);
            switch (alt16) {
                case 1 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:265:8: ftsTerm ( ( fuzzy )=> fuzzy )?
                    {
                    pushFollow(FOLLOW_ftsTerm_in_ftsTest626);
                    ftsTerm27=ftsTerm();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_ftsTerm.add(ftsTerm27.getTree());
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:265:16: ( ( fuzzy )=> fuzzy )?
                    int alt12=2;
                    int LA12_0 = input.LA(1);

                    if ( (LA12_0==TILDA) ) {
                        int LA12_1 = input.LA(2);

                        if ( (LA12_1==DECIMAL_INTEGER_LITERAL||LA12_1==FLOATING_POINT_LITERAL) ) {
                            int LA12_3 = input.LA(3);

                            if ( (synpred4_FTS()) ) {
                                alt12=1;
                            }
                        }
                    }
                    switch (alt12) {
                        case 1 :
                            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:265:17: ( fuzzy )=> fuzzy
                            {
                            pushFollow(FOLLOW_fuzzy_in_ftsTest635);
                            fuzzy28=fuzzy();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_fuzzy.add(fuzzy28.getTree());

                            }
                            break;

                    }



                    // AST REWRITE
                    // elements: ftsTerm, fuzzy
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 266:3: -> ^( TERM ftsTerm ( fuzzy )? )
                    {
                        // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:266:6: ^( TERM ftsTerm ( fuzzy )? )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(TERM, "TERM"), root_1);

                        adaptor.addChild(root_1, stream_ftsTerm.nextTree());
                        // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:266:21: ( fuzzy )?
                        if ( stream_fuzzy.hasNext() ) {
                            adaptor.addChild(root_1, stream_fuzzy.nextTree());

                        }
                        stream_fuzzy.reset();

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:267:4: ftsExactTerm ( ( fuzzy )=> fuzzy )?
                    {
                    pushFollow(FOLLOW_ftsExactTerm_in_ftsTest655);
                    ftsExactTerm29=ftsExactTerm();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_ftsExactTerm.add(ftsExactTerm29.getTree());
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:267:17: ( ( fuzzy )=> fuzzy )?
                    int alt13=2;
                    int LA13_0 = input.LA(1);

                    if ( (LA13_0==TILDA) ) {
                        int LA13_1 = input.LA(2);

                        if ( (LA13_1==DECIMAL_INTEGER_LITERAL||LA13_1==FLOATING_POINT_LITERAL) ) {
                            int LA13_3 = input.LA(3);

                            if ( (synpred5_FTS()) ) {
                                alt13=1;
                            }
                        }
                    }
                    switch (alt13) {
                        case 1 :
                            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:267:18: ( fuzzy )=> fuzzy
                            {
                            pushFollow(FOLLOW_fuzzy_in_ftsTest664);
                            fuzzy30=fuzzy();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_fuzzy.add(fuzzy30.getTree());

                            }
                            break;

                    }



                    // AST REWRITE
                    // elements: fuzzy, ftsExactTerm
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 268:3: -> ^( EXACT_TERM ftsExactTerm ( fuzzy )? )
                    {
                        // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:268:6: ^( EXACT_TERM ftsExactTerm ( fuzzy )? )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(EXACT_TERM, "EXACT_TERM"), root_1);

                        adaptor.addChild(root_1, stream_ftsExactTerm.nextTree());
                        // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:268:32: ( fuzzy )?
                        if ( stream_fuzzy.hasNext() ) {
                            adaptor.addChild(root_1, stream_fuzzy.nextTree());

                        }
                        stream_fuzzy.reset();

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 3 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:269:11: ftsPhrase ( ( slop )=> slop )?
                    {
                    pushFollow(FOLLOW_ftsPhrase_in_ftsTest691);
                    ftsPhrase31=ftsPhrase();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_ftsPhrase.add(ftsPhrase31.getTree());
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:269:21: ( ( slop )=> slop )?
                    int alt14=2;
                    int LA14_0 = input.LA(1);

                    if ( (LA14_0==TILDA) ) {
                        int LA14_1 = input.LA(2);

                        if ( (LA14_1==DECIMAL_INTEGER_LITERAL) ) {
                            int LA14_3 = input.LA(3);

                            if ( (synpred6_FTS()) ) {
                                alt14=1;
                            }
                        }
                    }
                    switch (alt14) {
                        case 1 :
                            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:269:22: ( slop )=> slop
                            {
                            pushFollow(FOLLOW_slop_in_ftsTest700);
                            slop32=slop();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_slop.add(slop32.getTree());

                            }
                            break;

                    }



                    // AST REWRITE
                    // elements: ftsPhrase, slop
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 270:10: -> ^( PHRASE ftsPhrase ( slop )? )
                    {
                        // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:270:13: ^( PHRASE ftsPhrase ( slop )? )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(PHRASE, "PHRASE"), root_1);

                        adaptor.addChild(root_1, stream_ftsPhrase.nextTree());
                        // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:270:32: ( slop )?
                        if ( stream_slop.hasNext() ) {
                            adaptor.addChild(root_1, stream_slop.nextTree());

                        }
                        stream_slop.reset();

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 4 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:271:11: ftsSynonym ( ( fuzzy )=> fuzzy )?
                    {
                    pushFollow(FOLLOW_ftsSynonym_in_ftsTest734);
                    ftsSynonym33=ftsSynonym();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_ftsSynonym.add(ftsSynonym33.getTree());
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:271:22: ( ( fuzzy )=> fuzzy )?
                    int alt15=2;
                    int LA15_0 = input.LA(1);

                    if ( (LA15_0==TILDA) ) {
                        int LA15_1 = input.LA(2);

                        if ( (LA15_1==DECIMAL_INTEGER_LITERAL||LA15_1==FLOATING_POINT_LITERAL) ) {
                            int LA15_3 = input.LA(3);

                            if ( (synpred7_FTS()) ) {
                                alt15=1;
                            }
                        }
                    }
                    switch (alt15) {
                        case 1 :
                            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:271:23: ( fuzzy )=> fuzzy
                            {
                            pushFollow(FOLLOW_fuzzy_in_ftsTest743);
                            fuzzy34=fuzzy();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_fuzzy.add(fuzzy34.getTree());

                            }
                            break;

                    }



                    // AST REWRITE
                    // elements: fuzzy, ftsSynonym
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 272:10: -> ^( SYNONYM ftsSynonym ( fuzzy )? )
                    {
                        // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:272:13: ^( SYNONYM ftsSynonym ( fuzzy )? )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(SYNONYM, "SYNONYM"), root_1);

                        adaptor.addChild(root_1, stream_ftsSynonym.nextTree());
                        // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:272:34: ( fuzzy )?
                        if ( stream_fuzzy.hasNext() ) {
                            adaptor.addChild(root_1, stream_fuzzy.nextTree());

                        }
                        stream_fuzzy.reset();

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 5 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:273:8: ftsFieldGroupProximity
                    {
                    pushFollow(FOLLOW_ftsFieldGroupProximity_in_ftsTest775);
                    ftsFieldGroupProximity35=ftsFieldGroupProximity();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_ftsFieldGroupProximity.add(ftsFieldGroupProximity35.getTree());


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
                    // 274:10: -> ^( PROXIMITY ftsFieldGroupProximity )
                    {
                        // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:274:13: ^( PROXIMITY ftsFieldGroupProximity )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(PROXIMITY, "PROXIMITY"), root_1);

                        adaptor.addChild(root_1, stream_ftsFieldGroupProximity.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 6 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:275:9: ftsRange
                    {
                    pushFollow(FOLLOW_ftsRange_in_ftsTest803);
                    ftsRange36=ftsRange();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_ftsRange.add(ftsRange36.getTree());


                    // AST REWRITE
                    // elements: ftsRange
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 276:10: -> ^( RANGE ftsRange )
                    {
                        // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:276:13: ^( RANGE ftsRange )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(RANGE, "RANGE"), root_1);

                        adaptor.addChild(root_1, stream_ftsRange.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 7 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:277:8: ftsFieldGroup
                    {
                    pushFollow(FOLLOW_ftsFieldGroup_in_ftsTest830);
                    ftsFieldGroup37=ftsFieldGroup();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_ftsFieldGroup.add(ftsFieldGroup37.getTree());


                    // AST REWRITE
                    // elements: ftsFieldGroup
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 278:7: -> ftsFieldGroup
                    {
                        adaptor.addChild(root_0, stream_ftsFieldGroup.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 8 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:279:4: LPAREN ftsImplicitConjunctionOrDisjunction RPAREN
                    {
                    LPAREN38=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_ftsTest847); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_LPAREN.add(LPAREN38);

                    pushFollow(FOLLOW_ftsImplicitConjunctionOrDisjunction_in_ftsTest849);
                    ftsImplicitConjunctionOrDisjunction39=ftsImplicitConjunctionOrDisjunction();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_ftsImplicitConjunctionOrDisjunction.add(ftsImplicitConjunctionOrDisjunction39.getTree());
                    RPAREN40=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_ftsTest851); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_RPAREN.add(RPAREN40);



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
                    // 280:3: -> ftsImplicitConjunctionOrDisjunction
                    {
                        adaptor.addChild(root_0, stream_ftsImplicitConjunctionOrDisjunction.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 9 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:281:4: template
                    {
                    pushFollow(FOLLOW_template_in_ftsTest862);
                    template41=template();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_template.add(template41.getTree());


                    // AST REWRITE
                    // elements: template
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 282:5: -> template
                    {
                        adaptor.addChild(root_0, stream_template.nextTree());

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

    public static class template_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "template"
    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:285:1: template : ( PERCENT tempReference -> ^( TEMPLATE tempReference ) | PERCENT LPAREN ( tempReference ( COMMA )? )+ RPAREN -> ^( TEMPLATE ( tempReference )+ ) );
    public final FTSParser.template_return template() throws RecognitionException {
        FTSParser.template_return retval = new FTSParser.template_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token PERCENT42=null;
        Token PERCENT44=null;
        Token LPAREN45=null;
        Token COMMA47=null;
        Token RPAREN48=null;
        FTSParser.tempReference_return tempReference43 = null;

        FTSParser.tempReference_return tempReference46 = null;


        Object PERCENT42_tree=null;
        Object PERCENT44_tree=null;
        Object LPAREN45_tree=null;
        Object COMMA47_tree=null;
        Object RPAREN48_tree=null;
        RewriteRuleTokenStream stream_RPAREN=new RewriteRuleTokenStream(adaptor,"token RPAREN");
        RewriteRuleTokenStream stream_PERCENT=new RewriteRuleTokenStream(adaptor,"token PERCENT");
        RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
        RewriteRuleTokenStream stream_LPAREN=new RewriteRuleTokenStream(adaptor,"token LPAREN");
        RewriteRuleSubtreeStream stream_tempReference=new RewriteRuleSubtreeStream(adaptor,"rule tempReference");
        try {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:286:2: ( PERCENT tempReference -> ^( TEMPLATE tempReference ) | PERCENT LPAREN ( tempReference ( COMMA )? )+ RPAREN -> ^( TEMPLATE ( tempReference )+ ) )
            int alt19=2;
            int LA19_0 = input.LA(1);

            if ( (LA19_0==PERCENT) ) {
                int LA19_1 = input.LA(2);

                if ( (LA19_1==LPAREN) ) {
                    alt19=2;
                }
                else if ( ((LA19_1>=AT && LA19_1<=ID)) ) {
                    alt19=1;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 19, 1, input);

                    throw nvae;
                }
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 19, 0, input);

                throw nvae;
            }
            switch (alt19) {
                case 1 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:286:4: PERCENT tempReference
                    {
                    PERCENT42=(Token)match(input,PERCENT,FOLLOW_PERCENT_in_template881); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_PERCENT.add(PERCENT42);

                    pushFollow(FOLLOW_tempReference_in_template883);
                    tempReference43=tempReference();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_tempReference.add(tempReference43.getTree());


                    // AST REWRITE
                    // elements: tempReference
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 287:3: -> ^( TEMPLATE tempReference )
                    {
                        // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:287:6: ^( TEMPLATE tempReference )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(TEMPLATE, "TEMPLATE"), root_1);

                        adaptor.addChild(root_1, stream_tempReference.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:288:4: PERCENT LPAREN ( tempReference ( COMMA )? )+ RPAREN
                    {
                    PERCENT44=(Token)match(input,PERCENT,FOLLOW_PERCENT_in_template898); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_PERCENT.add(PERCENT44);

                    LPAREN45=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_template900); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_LPAREN.add(LPAREN45);

                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:288:19: ( tempReference ( COMMA )? )+
                    int cnt18=0;
                    loop18:
                    do {
                        int alt18=2;
                        int LA18_0 = input.LA(1);

                        if ( ((LA18_0>=AT && LA18_0<=ID)) ) {
                            alt18=1;
                        }


                        switch (alt18) {
                    	case 1 :
                    	    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:288:20: tempReference ( COMMA )?
                    	    {
                    	    pushFollow(FOLLOW_tempReference_in_template903);
                    	    tempReference46=tempReference();

                    	    state._fsp--;
                    	    if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) stream_tempReference.add(tempReference46.getTree());
                    	    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:288:34: ( COMMA )?
                    	    int alt17=2;
                    	    int LA17_0 = input.LA(1);

                    	    if ( (LA17_0==COMMA) ) {
                    	        alt17=1;
                    	    }
                    	    switch (alt17) {
                    	        case 1 :
                    	            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:288:34: COMMA
                    	            {
                    	            COMMA47=(Token)match(input,COMMA,FOLLOW_COMMA_in_template905); if (state.failed) return retval; 
                    	            if ( state.backtracking==0 ) stream_COMMA.add(COMMA47);


                    	            }
                    	            break;

                    	    }


                    	    }
                    	    break;

                    	default :
                    	    if ( cnt18 >= 1 ) break loop18;
                    	    if (state.backtracking>0) {state.failed=true; return retval;}
                                EarlyExitException eee =
                                    new EarlyExitException(18, input);
                                throw eee;
                        }
                        cnt18++;
                    } while (true);

                    RPAREN48=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_template910); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_RPAREN.add(RPAREN48);



                    // AST REWRITE
                    // elements: tempReference
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 289:3: -> ^( TEMPLATE ( tempReference )+ )
                    {
                        // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:289:6: ^( TEMPLATE ( tempReference )+ )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(TEMPLATE, "TEMPLATE"), root_1);

                        if ( !(stream_tempReference.hasNext()) ) {
                            throw new RewriteEarlyExitException();
                        }
                        while ( stream_tempReference.hasNext() ) {
                            adaptor.addChild(root_1, stream_tempReference.nextTree());

                        }
                        stream_tempReference.reset();

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
    // $ANTLR end "template"

    public static class fuzzy_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "fuzzy"
    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:294:1: fuzzy : TILDA number -> ^( FUZZY number ) ;
    public final FTSParser.fuzzy_return fuzzy() throws RecognitionException {
        FTSParser.fuzzy_return retval = new FTSParser.fuzzy_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token TILDA49=null;
        FTSParser.number_return number50 = null;


        Object TILDA49_tree=null;
        RewriteRuleTokenStream stream_TILDA=new RewriteRuleTokenStream(adaptor,"token TILDA");
        RewriteRuleSubtreeStream stream_number=new RewriteRuleSubtreeStream(adaptor,"rule number");
        try {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:295:2: ( TILDA number -> ^( FUZZY number ) )
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:295:5: TILDA number
            {
            TILDA49=(Token)match(input,TILDA,FOLLOW_TILDA_in_fuzzy937); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_TILDA.add(TILDA49);

            pushFollow(FOLLOW_number_in_fuzzy939);
            number50=number();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_number.add(number50.getTree());


            // AST REWRITE
            // elements: number
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 296:3: -> ^( FUZZY number )
            {
                // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:296:6: ^( FUZZY number )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(FUZZY, "FUZZY"), root_1);

                adaptor.addChild(root_1, stream_number.nextTree());

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
    // $ANTLR end "fuzzy"

    public static class slop_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "slop"
    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:299:1: slop : TILDA DECIMAL_INTEGER_LITERAL -> ^( FUZZY DECIMAL_INTEGER_LITERAL ) ;
    public final FTSParser.slop_return slop() throws RecognitionException {
        FTSParser.slop_return retval = new FTSParser.slop_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token TILDA51=null;
        Token DECIMAL_INTEGER_LITERAL52=null;

        Object TILDA51_tree=null;
        Object DECIMAL_INTEGER_LITERAL52_tree=null;
        RewriteRuleTokenStream stream_TILDA=new RewriteRuleTokenStream(adaptor,"token TILDA");
        RewriteRuleTokenStream stream_DECIMAL_INTEGER_LITERAL=new RewriteRuleTokenStream(adaptor,"token DECIMAL_INTEGER_LITERAL");

        try {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:300:2: ( TILDA DECIMAL_INTEGER_LITERAL -> ^( FUZZY DECIMAL_INTEGER_LITERAL ) )
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:300:5: TILDA DECIMAL_INTEGER_LITERAL
            {
            TILDA51=(Token)match(input,TILDA,FOLLOW_TILDA_in_slop962); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_TILDA.add(TILDA51);

            DECIMAL_INTEGER_LITERAL52=(Token)match(input,DECIMAL_INTEGER_LITERAL,FOLLOW_DECIMAL_INTEGER_LITERAL_in_slop964); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_DECIMAL_INTEGER_LITERAL.add(DECIMAL_INTEGER_LITERAL52);



            // AST REWRITE
            // elements: DECIMAL_INTEGER_LITERAL
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 301:3: -> ^( FUZZY DECIMAL_INTEGER_LITERAL )
            {
                // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:301:6: ^( FUZZY DECIMAL_INTEGER_LITERAL )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(FUZZY, "FUZZY"), root_1);

                adaptor.addChild(root_1, stream_DECIMAL_INTEGER_LITERAL.nextNode());

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
    // $ANTLR end "slop"

    public static class boost_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "boost"
    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:304:1: boost : CARAT number -> ^( BOOST number ) ;
    public final FTSParser.boost_return boost() throws RecognitionException {
        FTSParser.boost_return retval = new FTSParser.boost_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token CARAT53=null;
        FTSParser.number_return number54 = null;


        Object CARAT53_tree=null;
        RewriteRuleTokenStream stream_CARAT=new RewriteRuleTokenStream(adaptor,"token CARAT");
        RewriteRuleSubtreeStream stream_number=new RewriteRuleSubtreeStream(adaptor,"rule number");
        try {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:305:2: ( CARAT number -> ^( BOOST number ) )
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:305:5: CARAT number
            {
            CARAT53=(Token)match(input,CARAT,FOLLOW_CARAT_in_boost986); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_CARAT.add(CARAT53);

            pushFollow(FOLLOW_number_in_boost988);
            number54=number();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_number.add(number54.getTree());


            // AST REWRITE
            // elements: number
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 306:3: -> ^( BOOST number )
            {
                // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:306:6: ^( BOOST number )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(BOOST, "BOOST"), root_1);

                adaptor.addChild(root_1, stream_number.nextTree());

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
    // $ANTLR end "boost"

    public static class ftsTerm_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "ftsTerm"
    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:309:1: ftsTerm : ( fieldReference COLON )? ftsWord -> ftsWord ( fieldReference )? ;
    public final FTSParser.ftsTerm_return ftsTerm() throws RecognitionException {
        FTSParser.ftsTerm_return retval = new FTSParser.ftsTerm_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token COLON56=null;
        FTSParser.fieldReference_return fieldReference55 = null;

        FTSParser.ftsWord_return ftsWord57 = null;


        Object COLON56_tree=null;
        RewriteRuleTokenStream stream_COLON=new RewriteRuleTokenStream(adaptor,"token COLON");
        RewriteRuleSubtreeStream stream_ftsWord=new RewriteRuleSubtreeStream(adaptor,"rule ftsWord");
        RewriteRuleSubtreeStream stream_fieldReference=new RewriteRuleSubtreeStream(adaptor,"rule fieldReference");
        try {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:310:2: ( ( fieldReference COLON )? ftsWord -> ftsWord ( fieldReference )? )
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:310:4: ( fieldReference COLON )? ftsWord
            {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:310:4: ( fieldReference COLON )?
            int alt20=2;
            int LA20_0 = input.LA(1);

            if ( ((LA20_0>=AT && LA20_0<=URI)) ) {
                alt20=1;
            }
            else if ( (LA20_0==ID) ) {
                int LA20_2 = input.LA(2);

                if ( (LA20_2==COLON) ) {
                    alt20=1;
                }
            }
            switch (alt20) {
                case 1 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:310:5: fieldReference COLON
                    {
                    pushFollow(FOLLOW_fieldReference_in_ftsTerm1010);
                    fieldReference55=fieldReference();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_fieldReference.add(fieldReference55.getTree());
                    COLON56=(Token)match(input,COLON,FOLLOW_COLON_in_ftsTerm1012); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_COLON.add(COLON56);


                    }
                    break;

            }

            pushFollow(FOLLOW_ftsWord_in_ftsTerm1016);
            ftsWord57=ftsWord();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_ftsWord.add(ftsWord57.getTree());


            // AST REWRITE
            // elements: fieldReference, ftsWord
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 311:3: -> ftsWord ( fieldReference )?
            {
                adaptor.addChild(root_0, stream_ftsWord.nextTree());
                // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:311:14: ( fieldReference )?
                if ( stream_fieldReference.hasNext() ) {
                    adaptor.addChild(root_0, stream_fieldReference.nextTree());

                }
                stream_fieldReference.reset();

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
    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:314:1: ftsExactTerm : EQUALS ftsTerm -> ftsTerm ;
    public final FTSParser.ftsExactTerm_return ftsExactTerm() throws RecognitionException {
        FTSParser.ftsExactTerm_return retval = new FTSParser.ftsExactTerm_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token EQUALS58=null;
        FTSParser.ftsTerm_return ftsTerm59 = null;


        Object EQUALS58_tree=null;
        RewriteRuleTokenStream stream_EQUALS=new RewriteRuleTokenStream(adaptor,"token EQUALS");
        RewriteRuleSubtreeStream stream_ftsTerm=new RewriteRuleSubtreeStream(adaptor,"rule ftsTerm");
        try {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:315:2: ( EQUALS ftsTerm -> ftsTerm )
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:315:4: EQUALS ftsTerm
            {
            EQUALS58=(Token)match(input,EQUALS,FOLLOW_EQUALS_in_ftsExactTerm1037); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_EQUALS.add(EQUALS58);

            pushFollow(FOLLOW_ftsTerm_in_ftsExactTerm1039);
            ftsTerm59=ftsTerm();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_ftsTerm.add(ftsTerm59.getTree());


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
            // 316:3: -> ftsTerm
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
    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:319:1: ftsPhrase : ( fieldReference COLON )? FTSPHRASE -> FTSPHRASE ( fieldReference )? ;
    public final FTSParser.ftsPhrase_return ftsPhrase() throws RecognitionException {
        FTSParser.ftsPhrase_return retval = new FTSParser.ftsPhrase_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token COLON61=null;
        Token FTSPHRASE62=null;
        FTSParser.fieldReference_return fieldReference60 = null;


        Object COLON61_tree=null;
        Object FTSPHRASE62_tree=null;
        RewriteRuleTokenStream stream_COLON=new RewriteRuleTokenStream(adaptor,"token COLON");
        RewriteRuleTokenStream stream_FTSPHRASE=new RewriteRuleTokenStream(adaptor,"token FTSPHRASE");
        RewriteRuleSubtreeStream stream_fieldReference=new RewriteRuleSubtreeStream(adaptor,"rule fieldReference");
        try {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:320:2: ( ( fieldReference COLON )? FTSPHRASE -> FTSPHRASE ( fieldReference )? )
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:320:6: ( fieldReference COLON )? FTSPHRASE
            {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:320:6: ( fieldReference COLON )?
            int alt21=2;
            int LA21_0 = input.LA(1);

            if ( ((LA21_0>=AT && LA21_0<=ID)) ) {
                alt21=1;
            }
            switch (alt21) {
                case 1 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:320:7: fieldReference COLON
                    {
                    pushFollow(FOLLOW_fieldReference_in_ftsPhrase1060);
                    fieldReference60=fieldReference();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_fieldReference.add(fieldReference60.getTree());
                    COLON61=(Token)match(input,COLON,FOLLOW_COLON_in_ftsPhrase1062); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_COLON.add(COLON61);


                    }
                    break;

            }

            FTSPHRASE62=(Token)match(input,FTSPHRASE,FOLLOW_FTSPHRASE_in_ftsPhrase1066); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_FTSPHRASE.add(FTSPHRASE62);



            // AST REWRITE
            // elements: FTSPHRASE, fieldReference
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 321:3: -> FTSPHRASE ( fieldReference )?
            {
                adaptor.addChild(root_0, stream_FTSPHRASE.nextNode());
                // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:321:16: ( fieldReference )?
                if ( stream_fieldReference.hasNext() ) {
                    adaptor.addChild(root_0, stream_fieldReference.nextTree());

                }
                stream_fieldReference.reset();

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
    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:324:1: ftsSynonym : TILDA ftsTerm -> ftsTerm ;
    public final FTSParser.ftsSynonym_return ftsSynonym() throws RecognitionException {
        FTSParser.ftsSynonym_return retval = new FTSParser.ftsSynonym_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token TILDA63=null;
        FTSParser.ftsTerm_return ftsTerm64 = null;


        Object TILDA63_tree=null;
        RewriteRuleTokenStream stream_TILDA=new RewriteRuleTokenStream(adaptor,"token TILDA");
        RewriteRuleSubtreeStream stream_ftsTerm=new RewriteRuleSubtreeStream(adaptor,"rule ftsTerm");
        try {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:325:2: ( TILDA ftsTerm -> ftsTerm )
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:325:4: TILDA ftsTerm
            {
            TILDA63=(Token)match(input,TILDA,FOLLOW_TILDA_in_ftsSynonym1087); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_TILDA.add(TILDA63);

            pushFollow(FOLLOW_ftsTerm_in_ftsSynonym1089);
            ftsTerm64=ftsTerm();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_ftsTerm.add(ftsTerm64.getTree());


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
            // 326:3: -> ftsTerm
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

    public static class ftsRange_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "ftsRange"
    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:329:1: ftsRange : ( fieldReference COLON )? ftsFieldGroupRange -> ftsFieldGroupRange ( fieldReference )? ;
    public final FTSParser.ftsRange_return ftsRange() throws RecognitionException {
        FTSParser.ftsRange_return retval = new FTSParser.ftsRange_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token COLON66=null;
        FTSParser.fieldReference_return fieldReference65 = null;

        FTSParser.ftsFieldGroupRange_return ftsFieldGroupRange67 = null;


        Object COLON66_tree=null;
        RewriteRuleTokenStream stream_COLON=new RewriteRuleTokenStream(adaptor,"token COLON");
        RewriteRuleSubtreeStream stream_ftsFieldGroupRange=new RewriteRuleSubtreeStream(adaptor,"rule ftsFieldGroupRange");
        RewriteRuleSubtreeStream stream_fieldReference=new RewriteRuleSubtreeStream(adaptor,"rule fieldReference");
        try {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:330:2: ( ( fieldReference COLON )? ftsFieldGroupRange -> ftsFieldGroupRange ( fieldReference )? )
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:330:5: ( fieldReference COLON )? ftsFieldGroupRange
            {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:330:5: ( fieldReference COLON )?
            int alt22=2;
            int LA22_0 = input.LA(1);

            if ( ((LA22_0>=AT && LA22_0<=URI)) ) {
                alt22=1;
            }
            else if ( (LA22_0==ID) ) {
                int LA22_2 = input.LA(2);

                if ( (LA22_2==COLON) ) {
                    alt22=1;
                }
            }
            switch (alt22) {
                case 1 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:330:6: fieldReference COLON
                    {
                    pushFollow(FOLLOW_fieldReference_in_ftsRange1109);
                    fieldReference65=fieldReference();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_fieldReference.add(fieldReference65.getTree());
                    COLON66=(Token)match(input,COLON,FOLLOW_COLON_in_ftsRange1111); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_COLON.add(COLON66);


                    }
                    break;

            }

            pushFollow(FOLLOW_ftsFieldGroupRange_in_ftsRange1115);
            ftsFieldGroupRange67=ftsFieldGroupRange();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_ftsFieldGroupRange.add(ftsFieldGroupRange67.getTree());


            // AST REWRITE
            // elements: ftsFieldGroupRange, fieldReference
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 331:5: -> ftsFieldGroupRange ( fieldReference )?
            {
                adaptor.addChild(root_0, stream_ftsFieldGroupRange.nextTree());
                // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:331:27: ( fieldReference )?
                if ( stream_fieldReference.hasNext() ) {
                    adaptor.addChild(root_0, stream_fieldReference.nextTree());

                }
                stream_fieldReference.reset();

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
    // $ANTLR end "ftsRange"

    public static class ftsFieldGroup_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "ftsFieldGroup"
    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:334:1: ftsFieldGroup : fieldReference COLON LPAREN ftsFieldGroupImplicitConjunctionOrDisjunction RPAREN -> ^( FIELD_GROUP fieldReference ftsFieldGroupImplicitConjunctionOrDisjunction ) ;
    public final FTSParser.ftsFieldGroup_return ftsFieldGroup() throws RecognitionException {
        FTSParser.ftsFieldGroup_return retval = new FTSParser.ftsFieldGroup_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token COLON69=null;
        Token LPAREN70=null;
        Token RPAREN72=null;
        FTSParser.fieldReference_return fieldReference68 = null;

        FTSParser.ftsFieldGroupImplicitConjunctionOrDisjunction_return ftsFieldGroupImplicitConjunctionOrDisjunction71 = null;


        Object COLON69_tree=null;
        Object LPAREN70_tree=null;
        Object RPAREN72_tree=null;
        RewriteRuleTokenStream stream_COLON=new RewriteRuleTokenStream(adaptor,"token COLON");
        RewriteRuleTokenStream stream_RPAREN=new RewriteRuleTokenStream(adaptor,"token RPAREN");
        RewriteRuleTokenStream stream_LPAREN=new RewriteRuleTokenStream(adaptor,"token LPAREN");
        RewriteRuleSubtreeStream stream_ftsFieldGroupImplicitConjunctionOrDisjunction=new RewriteRuleSubtreeStream(adaptor,"rule ftsFieldGroupImplicitConjunctionOrDisjunction");
        RewriteRuleSubtreeStream stream_fieldReference=new RewriteRuleSubtreeStream(adaptor,"rule fieldReference");
        try {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:335:2: ( fieldReference COLON LPAREN ftsFieldGroupImplicitConjunctionOrDisjunction RPAREN -> ^( FIELD_GROUP fieldReference ftsFieldGroupImplicitConjunctionOrDisjunction ) )
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:335:4: fieldReference COLON LPAREN ftsFieldGroupImplicitConjunctionOrDisjunction RPAREN
            {
            pushFollow(FOLLOW_fieldReference_in_ftsFieldGroup1138);
            fieldReference68=fieldReference();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_fieldReference.add(fieldReference68.getTree());
            COLON69=(Token)match(input,COLON,FOLLOW_COLON_in_ftsFieldGroup1140); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_COLON.add(COLON69);

            LPAREN70=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_ftsFieldGroup1142); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_LPAREN.add(LPAREN70);

            pushFollow(FOLLOW_ftsFieldGroupImplicitConjunctionOrDisjunction_in_ftsFieldGroup1144);
            ftsFieldGroupImplicitConjunctionOrDisjunction71=ftsFieldGroupImplicitConjunctionOrDisjunction();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_ftsFieldGroupImplicitConjunctionOrDisjunction.add(ftsFieldGroupImplicitConjunctionOrDisjunction71.getTree());
            RPAREN72=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_ftsFieldGroup1146); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_RPAREN.add(RPAREN72);



            // AST REWRITE
            // elements: ftsFieldGroupImplicitConjunctionOrDisjunction, fieldReference
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 336:3: -> ^( FIELD_GROUP fieldReference ftsFieldGroupImplicitConjunctionOrDisjunction )
            {
                // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:336:6: ^( FIELD_GROUP fieldReference ftsFieldGroupImplicitConjunctionOrDisjunction )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(FIELD_GROUP, "FIELD_GROUP"), root_1);

                adaptor.addChild(root_1, stream_fieldReference.nextTree());
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
    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:339:1: ftsFieldGroupImplicitConjunctionOrDisjunction : ({...}? ftsFieldGroupExplicitDisjunction ( ftsFieldGroupExplicitDisjunction )* -> ^( FIELD_CONJUNCTION ( ftsFieldGroupExplicitDisjunction )+ ) | ftsFieldGroupExplicitDisjunction ( ftsFieldGroupExplicitDisjunction )* -> ^( FIELD_DISJUNCTION ( ftsFieldGroupExplicitDisjunction )+ ) );
    public final FTSParser.ftsFieldGroupImplicitConjunctionOrDisjunction_return ftsFieldGroupImplicitConjunctionOrDisjunction() throws RecognitionException {
        FTSParser.ftsFieldGroupImplicitConjunctionOrDisjunction_return retval = new FTSParser.ftsFieldGroupImplicitConjunctionOrDisjunction_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        FTSParser.ftsFieldGroupExplicitDisjunction_return ftsFieldGroupExplicitDisjunction73 = null;

        FTSParser.ftsFieldGroupExplicitDisjunction_return ftsFieldGroupExplicitDisjunction74 = null;

        FTSParser.ftsFieldGroupExplicitDisjunction_return ftsFieldGroupExplicitDisjunction75 = null;

        FTSParser.ftsFieldGroupExplicitDisjunction_return ftsFieldGroupExplicitDisjunction76 = null;


        RewriteRuleSubtreeStream stream_ftsFieldGroupExplicitDisjunction=new RewriteRuleSubtreeStream(adaptor,"rule ftsFieldGroupExplicitDisjunction");
        try {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:340:2: ({...}? ftsFieldGroupExplicitDisjunction ( ftsFieldGroupExplicitDisjunction )* -> ^( FIELD_CONJUNCTION ( ftsFieldGroupExplicitDisjunction )+ ) | ftsFieldGroupExplicitDisjunction ( ftsFieldGroupExplicitDisjunction )* -> ^( FIELD_DISJUNCTION ( ftsFieldGroupExplicitDisjunction )+ ) )
            int alt25=2;
            alt25 = dfa25.predict(input);
            switch (alt25) {
                case 1 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:340:4: {...}? ftsFieldGroupExplicitDisjunction ( ftsFieldGroupExplicitDisjunction )*
                    {
                    if ( !((defaultFieldConjunction())) ) {
                        if (state.backtracking>0) {state.failed=true; return retval;}
                        throw new FailedPredicateException(input, "ftsFieldGroupImplicitConjunctionOrDisjunction", "defaultFieldConjunction()");
                    }
                    pushFollow(FOLLOW_ftsFieldGroupExplicitDisjunction_in_ftsFieldGroupImplicitConjunctionOrDisjunction1172);
                    ftsFieldGroupExplicitDisjunction73=ftsFieldGroupExplicitDisjunction();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_ftsFieldGroupExplicitDisjunction.add(ftsFieldGroupExplicitDisjunction73.getTree());
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:340:66: ( ftsFieldGroupExplicitDisjunction )*
                    loop23:
                    do {
                        int alt23=2;
                        int LA23_0 = input.LA(1);

                        if ( ((LA23_0>=PLUS && LA23_0<=LPAREN)||(LA23_0>=TILDA && LA23_0<=DECIMAL_INTEGER_LITERAL)||(LA23_0>=EQUALS && LA23_0<=FTSPHRASE)||(LA23_0>=TO && LA23_0<=LT)||(LA23_0>=ID && LA23_0<=FLOATING_POINT_LITERAL)||LA23_0==EXCLAMATION) ) {
                            alt23=1;
                        }


                        switch (alt23) {
                    	case 1 :
                    	    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:340:67: ftsFieldGroupExplicitDisjunction
                    	    {
                    	    pushFollow(FOLLOW_ftsFieldGroupExplicitDisjunction_in_ftsFieldGroupImplicitConjunctionOrDisjunction1175);
                    	    ftsFieldGroupExplicitDisjunction74=ftsFieldGroupExplicitDisjunction();

                    	    state._fsp--;
                    	    if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) stream_ftsFieldGroupExplicitDisjunction.add(ftsFieldGroupExplicitDisjunction74.getTree());

                    	    }
                    	    break;

                    	default :
                    	    break loop23;
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
                    // 341:3: -> ^( FIELD_CONJUNCTION ( ftsFieldGroupExplicitDisjunction )+ )
                    {
                        // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:341:6: ^( FIELD_CONJUNCTION ( ftsFieldGroupExplicitDisjunction )+ )
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
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:342:4: ftsFieldGroupExplicitDisjunction ( ftsFieldGroupExplicitDisjunction )*
                    {
                    pushFollow(FOLLOW_ftsFieldGroupExplicitDisjunction_in_ftsFieldGroupImplicitConjunctionOrDisjunction1193);
                    ftsFieldGroupExplicitDisjunction75=ftsFieldGroupExplicitDisjunction();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_ftsFieldGroupExplicitDisjunction.add(ftsFieldGroupExplicitDisjunction75.getTree());
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:342:37: ( ftsFieldGroupExplicitDisjunction )*
                    loop24:
                    do {
                        int alt24=2;
                        int LA24_0 = input.LA(1);

                        if ( ((LA24_0>=PLUS && LA24_0<=LPAREN)||(LA24_0>=TILDA && LA24_0<=DECIMAL_INTEGER_LITERAL)||(LA24_0>=EQUALS && LA24_0<=FTSPHRASE)||(LA24_0>=TO && LA24_0<=LT)||(LA24_0>=ID && LA24_0<=FLOATING_POINT_LITERAL)||LA24_0==EXCLAMATION) ) {
                            alt24=1;
                        }


                        switch (alt24) {
                    	case 1 :
                    	    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:342:38: ftsFieldGroupExplicitDisjunction
                    	    {
                    	    pushFollow(FOLLOW_ftsFieldGroupExplicitDisjunction_in_ftsFieldGroupImplicitConjunctionOrDisjunction1196);
                    	    ftsFieldGroupExplicitDisjunction76=ftsFieldGroupExplicitDisjunction();

                    	    state._fsp--;
                    	    if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) stream_ftsFieldGroupExplicitDisjunction.add(ftsFieldGroupExplicitDisjunction76.getTree());

                    	    }
                    	    break;

                    	default :
                    	    break loop24;
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
                    // 343:3: -> ^( FIELD_DISJUNCTION ( ftsFieldGroupExplicitDisjunction )+ )
                    {
                        // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:343:6: ^( FIELD_DISJUNCTION ( ftsFieldGroupExplicitDisjunction )+ )
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
    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:346:1: ftsFieldGroupExplicitDisjunction : ftsFieldGroupExplictConjunction ( ( or )=> or ftsFieldGroupExplictConjunction )* -> ^( FIELD_DISJUNCTION ( ftsFieldGroupExplictConjunction )+ ) ;
    public final FTSParser.ftsFieldGroupExplicitDisjunction_return ftsFieldGroupExplicitDisjunction() throws RecognitionException {
        FTSParser.ftsFieldGroupExplicitDisjunction_return retval = new FTSParser.ftsFieldGroupExplicitDisjunction_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        FTSParser.ftsFieldGroupExplictConjunction_return ftsFieldGroupExplictConjunction77 = null;

        FTSParser.or_return or78 = null;

        FTSParser.ftsFieldGroupExplictConjunction_return ftsFieldGroupExplictConjunction79 = null;


        RewriteRuleSubtreeStream stream_ftsFieldGroupExplictConjunction=new RewriteRuleSubtreeStream(adaptor,"rule ftsFieldGroupExplictConjunction");
        RewriteRuleSubtreeStream stream_or=new RewriteRuleSubtreeStream(adaptor,"rule or");
        try {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:347:2: ( ftsFieldGroupExplictConjunction ( ( or )=> or ftsFieldGroupExplictConjunction )* -> ^( FIELD_DISJUNCTION ( ftsFieldGroupExplictConjunction )+ ) )
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:347:4: ftsFieldGroupExplictConjunction ( ( or )=> or ftsFieldGroupExplictConjunction )*
            {
            pushFollow(FOLLOW_ftsFieldGroupExplictConjunction_in_ftsFieldGroupExplicitDisjunction1221);
            ftsFieldGroupExplictConjunction77=ftsFieldGroupExplictConjunction();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_ftsFieldGroupExplictConjunction.add(ftsFieldGroupExplictConjunction77.getTree());
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:347:36: ( ( or )=> or ftsFieldGroupExplictConjunction )*
            loop26:
            do {
                int alt26=2;
                alt26 = dfa26.predict(input);
                switch (alt26) {
            	case 1 :
            	    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:347:37: ( or )=> or ftsFieldGroupExplictConjunction
            	    {
            	    pushFollow(FOLLOW_or_in_ftsFieldGroupExplicitDisjunction1230);
            	    or78=or();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_or.add(or78.getTree());
            	    pushFollow(FOLLOW_ftsFieldGroupExplictConjunction_in_ftsFieldGroupExplicitDisjunction1232);
            	    ftsFieldGroupExplictConjunction79=ftsFieldGroupExplictConjunction();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_ftsFieldGroupExplictConjunction.add(ftsFieldGroupExplictConjunction79.getTree());

            	    }
            	    break;

            	default :
            	    break loop26;
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
            // 348:3: -> ^( FIELD_DISJUNCTION ( ftsFieldGroupExplictConjunction )+ )
            {
                // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:348:6: ^( FIELD_DISJUNCTION ( ftsFieldGroupExplictConjunction )+ )
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
    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:351:1: ftsFieldGroupExplictConjunction : ftsFieldGroupPrefixed ( ( and )=> and ftsFieldGroupPrefixed )* -> ^( FIELD_CONJUNCTION ( ftsFieldGroupPrefixed )+ ) ;
    public final FTSParser.ftsFieldGroupExplictConjunction_return ftsFieldGroupExplictConjunction() throws RecognitionException {
        FTSParser.ftsFieldGroupExplictConjunction_return retval = new FTSParser.ftsFieldGroupExplictConjunction_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        FTSParser.ftsFieldGroupPrefixed_return ftsFieldGroupPrefixed80 = null;

        FTSParser.and_return and81 = null;

        FTSParser.ftsFieldGroupPrefixed_return ftsFieldGroupPrefixed82 = null;


        RewriteRuleSubtreeStream stream_ftsFieldGroupPrefixed=new RewriteRuleSubtreeStream(adaptor,"rule ftsFieldGroupPrefixed");
        RewriteRuleSubtreeStream stream_and=new RewriteRuleSubtreeStream(adaptor,"rule and");
        try {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:352:2: ( ftsFieldGroupPrefixed ( ( and )=> and ftsFieldGroupPrefixed )* -> ^( FIELD_CONJUNCTION ( ftsFieldGroupPrefixed )+ ) )
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:352:4: ftsFieldGroupPrefixed ( ( and )=> and ftsFieldGroupPrefixed )*
            {
            pushFollow(FOLLOW_ftsFieldGroupPrefixed_in_ftsFieldGroupExplictConjunction1257);
            ftsFieldGroupPrefixed80=ftsFieldGroupPrefixed();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_ftsFieldGroupPrefixed.add(ftsFieldGroupPrefixed80.getTree());
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:352:26: ( ( and )=> and ftsFieldGroupPrefixed )*
            loop27:
            do {
                int alt27=2;
                alt27 = dfa27.predict(input);
                switch (alt27) {
            	case 1 :
            	    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:352:27: ( and )=> and ftsFieldGroupPrefixed
            	    {
            	    pushFollow(FOLLOW_and_in_ftsFieldGroupExplictConjunction1266);
            	    and81=and();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_and.add(and81.getTree());
            	    pushFollow(FOLLOW_ftsFieldGroupPrefixed_in_ftsFieldGroupExplictConjunction1268);
            	    ftsFieldGroupPrefixed82=ftsFieldGroupPrefixed();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_ftsFieldGroupPrefixed.add(ftsFieldGroupPrefixed82.getTree());

            	    }
            	    break;

            	default :
            	    break loop27;
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
            // 353:3: -> ^( FIELD_CONJUNCTION ( ftsFieldGroupPrefixed )+ )
            {
                // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:353:6: ^( FIELD_CONJUNCTION ( ftsFieldGroupPrefixed )+ )
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
    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:357:1: ftsFieldGroupPrefixed : ( ( not )=> not ftsFieldGroupTest ( boost )? -> ^( FIELD_NEGATION ftsFieldGroupTest ( boost )? ) | ftsFieldGroupTest ( boost )? -> ^( FIELD_DEFAULT ftsFieldGroupTest ( boost )? ) | PLUS ftsFieldGroupTest ( boost )? -> ^( FIELD_MANDATORY ftsFieldGroupTest ( boost )? ) | BAR ftsFieldGroupTest ( boost )? -> ^( FIELD_OPTIONAL ftsFieldGroupTest ( boost )? ) | MINUS ftsFieldGroupTest ( boost )? -> ^( FIELD_EXCLUDE ftsFieldGroupTest ( boost )? ) );
    public final FTSParser.ftsFieldGroupPrefixed_return ftsFieldGroupPrefixed() throws RecognitionException {
        FTSParser.ftsFieldGroupPrefixed_return retval = new FTSParser.ftsFieldGroupPrefixed_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token PLUS88=null;
        Token BAR91=null;
        Token MINUS94=null;
        FTSParser.not_return not83 = null;

        FTSParser.ftsFieldGroupTest_return ftsFieldGroupTest84 = null;

        FTSParser.boost_return boost85 = null;

        FTSParser.ftsFieldGroupTest_return ftsFieldGroupTest86 = null;

        FTSParser.boost_return boost87 = null;

        FTSParser.ftsFieldGroupTest_return ftsFieldGroupTest89 = null;

        FTSParser.boost_return boost90 = null;

        FTSParser.ftsFieldGroupTest_return ftsFieldGroupTest92 = null;

        FTSParser.boost_return boost93 = null;

        FTSParser.ftsFieldGroupTest_return ftsFieldGroupTest95 = null;

        FTSParser.boost_return boost96 = null;


        Object PLUS88_tree=null;
        Object BAR91_tree=null;
        Object MINUS94_tree=null;
        RewriteRuleTokenStream stream_PLUS=new RewriteRuleTokenStream(adaptor,"token PLUS");
        RewriteRuleTokenStream stream_MINUS=new RewriteRuleTokenStream(adaptor,"token MINUS");
        RewriteRuleTokenStream stream_BAR=new RewriteRuleTokenStream(adaptor,"token BAR");
        RewriteRuleSubtreeStream stream_not=new RewriteRuleSubtreeStream(adaptor,"rule not");
        RewriteRuleSubtreeStream stream_boost=new RewriteRuleSubtreeStream(adaptor,"rule boost");
        RewriteRuleSubtreeStream stream_ftsFieldGroupTest=new RewriteRuleSubtreeStream(adaptor,"rule ftsFieldGroupTest");
        try {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:358:2: ( ( not )=> not ftsFieldGroupTest ( boost )? -> ^( FIELD_NEGATION ftsFieldGroupTest ( boost )? ) | ftsFieldGroupTest ( boost )? -> ^( FIELD_DEFAULT ftsFieldGroupTest ( boost )? ) | PLUS ftsFieldGroupTest ( boost )? -> ^( FIELD_MANDATORY ftsFieldGroupTest ( boost )? ) | BAR ftsFieldGroupTest ( boost )? -> ^( FIELD_OPTIONAL ftsFieldGroupTest ( boost )? ) | MINUS ftsFieldGroupTest ( boost )? -> ^( FIELD_EXCLUDE ftsFieldGroupTest ( boost )? ) )
            int alt33=5;
            alt33 = dfa33.predict(input);
            switch (alt33) {
                case 1 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:358:4: ( not )=> not ftsFieldGroupTest ( boost )?
                    {
                    pushFollow(FOLLOW_not_in_ftsFieldGroupPrefixed1303);
                    not83=not();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_not.add(not83.getTree());
                    pushFollow(FOLLOW_ftsFieldGroupTest_in_ftsFieldGroupPrefixed1305);
                    ftsFieldGroupTest84=ftsFieldGroupTest();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_ftsFieldGroupTest.add(ftsFieldGroupTest84.getTree());
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:358:35: ( boost )?
                    int alt28=2;
                    int LA28_0 = input.LA(1);

                    if ( (LA28_0==CARAT) ) {
                        alt28=1;
                    }
                    switch (alt28) {
                        case 1 :
                            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:358:35: boost
                            {
                            pushFollow(FOLLOW_boost_in_ftsFieldGroupPrefixed1307);
                            boost85=boost();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_boost.add(boost85.getTree());

                            }
                            break;

                    }



                    // AST REWRITE
                    // elements: boost, ftsFieldGroupTest
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 359:3: -> ^( FIELD_NEGATION ftsFieldGroupTest ( boost )? )
                    {
                        // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:359:6: ^( FIELD_NEGATION ftsFieldGroupTest ( boost )? )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(FIELD_NEGATION, "FIELD_NEGATION"), root_1);

                        adaptor.addChild(root_1, stream_ftsFieldGroupTest.nextTree());
                        // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:359:41: ( boost )?
                        if ( stream_boost.hasNext() ) {
                            adaptor.addChild(root_1, stream_boost.nextTree());

                        }
                        stream_boost.reset();

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:360:8: ftsFieldGroupTest ( boost )?
                    {
                    pushFollow(FOLLOW_ftsFieldGroupTest_in_ftsFieldGroupPrefixed1330);
                    ftsFieldGroupTest86=ftsFieldGroupTest();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_ftsFieldGroupTest.add(ftsFieldGroupTest86.getTree());
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:360:26: ( boost )?
                    int alt29=2;
                    int LA29_0 = input.LA(1);

                    if ( (LA29_0==CARAT) ) {
                        alt29=1;
                    }
                    switch (alt29) {
                        case 1 :
                            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:360:26: boost
                            {
                            pushFollow(FOLLOW_boost_in_ftsFieldGroupPrefixed1332);
                            boost87=boost();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_boost.add(boost87.getTree());

                            }
                            break;

                    }



                    // AST REWRITE
                    // elements: ftsFieldGroupTest, boost
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 361:3: -> ^( FIELD_DEFAULT ftsFieldGroupTest ( boost )? )
                    {
                        // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:361:6: ^( FIELD_DEFAULT ftsFieldGroupTest ( boost )? )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(FIELD_DEFAULT, "FIELD_DEFAULT"), root_1);

                        adaptor.addChild(root_1, stream_ftsFieldGroupTest.nextTree());
                        // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:361:40: ( boost )?
                        if ( stream_boost.hasNext() ) {
                            adaptor.addChild(root_1, stream_boost.nextTree());

                        }
                        stream_boost.reset();

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 3 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:362:7: PLUS ftsFieldGroupTest ( boost )?
                    {
                    PLUS88=(Token)match(input,PLUS,FOLLOW_PLUS_in_ftsFieldGroupPrefixed1354); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_PLUS.add(PLUS88);

                    pushFollow(FOLLOW_ftsFieldGroupTest_in_ftsFieldGroupPrefixed1356);
                    ftsFieldGroupTest89=ftsFieldGroupTest();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_ftsFieldGroupTest.add(ftsFieldGroupTest89.getTree());
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:362:30: ( boost )?
                    int alt30=2;
                    int LA30_0 = input.LA(1);

                    if ( (LA30_0==CARAT) ) {
                        alt30=1;
                    }
                    switch (alt30) {
                        case 1 :
                            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:362:30: boost
                            {
                            pushFollow(FOLLOW_boost_in_ftsFieldGroupPrefixed1358);
                            boost90=boost();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_boost.add(boost90.getTree());

                            }
                            break;

                    }



                    // AST REWRITE
                    // elements: boost, ftsFieldGroupTest
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 363:17: -> ^( FIELD_MANDATORY ftsFieldGroupTest ( boost )? )
                    {
                        // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:363:20: ^( FIELD_MANDATORY ftsFieldGroupTest ( boost )? )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(FIELD_MANDATORY, "FIELD_MANDATORY"), root_1);

                        adaptor.addChild(root_1, stream_ftsFieldGroupTest.nextTree());
                        // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:363:56: ( boost )?
                        if ( stream_boost.hasNext() ) {
                            adaptor.addChild(root_1, stream_boost.nextTree());

                        }
                        stream_boost.reset();

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 4 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:364:11: BAR ftsFieldGroupTest ( boost )?
                    {
                    BAR91=(Token)match(input,BAR,FOLLOW_BAR_in_ftsFieldGroupPrefixed1398); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_BAR.add(BAR91);

                    pushFollow(FOLLOW_ftsFieldGroupTest_in_ftsFieldGroupPrefixed1400);
                    ftsFieldGroupTest92=ftsFieldGroupTest();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_ftsFieldGroupTest.add(ftsFieldGroupTest92.getTree());
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:364:33: ( boost )?
                    int alt31=2;
                    int LA31_0 = input.LA(1);

                    if ( (LA31_0==CARAT) ) {
                        alt31=1;
                    }
                    switch (alt31) {
                        case 1 :
                            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:364:33: boost
                            {
                            pushFollow(FOLLOW_boost_in_ftsFieldGroupPrefixed1402);
                            boost93=boost();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_boost.add(boost93.getTree());

                            }
                            break;

                    }



                    // AST REWRITE
                    // elements: boost, ftsFieldGroupTest
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 365:17: -> ^( FIELD_OPTIONAL ftsFieldGroupTest ( boost )? )
                    {
                        // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:365:20: ^( FIELD_OPTIONAL ftsFieldGroupTest ( boost )? )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(FIELD_OPTIONAL, "FIELD_OPTIONAL"), root_1);

                        adaptor.addChild(root_1, stream_ftsFieldGroupTest.nextTree());
                        // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:365:55: ( boost )?
                        if ( stream_boost.hasNext() ) {
                            adaptor.addChild(root_1, stream_boost.nextTree());

                        }
                        stream_boost.reset();

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 5 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:366:11: MINUS ftsFieldGroupTest ( boost )?
                    {
                    MINUS94=(Token)match(input,MINUS,FOLLOW_MINUS_in_ftsFieldGroupPrefixed1442); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_MINUS.add(MINUS94);

                    pushFollow(FOLLOW_ftsFieldGroupTest_in_ftsFieldGroupPrefixed1444);
                    ftsFieldGroupTest95=ftsFieldGroupTest();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_ftsFieldGroupTest.add(ftsFieldGroupTest95.getTree());
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:366:35: ( boost )?
                    int alt32=2;
                    int LA32_0 = input.LA(1);

                    if ( (LA32_0==CARAT) ) {
                        alt32=1;
                    }
                    switch (alt32) {
                        case 1 :
                            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:366:35: boost
                            {
                            pushFollow(FOLLOW_boost_in_ftsFieldGroupPrefixed1446);
                            boost96=boost();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_boost.add(boost96.getTree());

                            }
                            break;

                    }



                    // AST REWRITE
                    // elements: boost, ftsFieldGroupTest
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 367:17: -> ^( FIELD_EXCLUDE ftsFieldGroupTest ( boost )? )
                    {
                        // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:367:20: ^( FIELD_EXCLUDE ftsFieldGroupTest ( boost )? )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(FIELD_EXCLUDE, "FIELD_EXCLUDE"), root_1);

                        adaptor.addChild(root_1, stream_ftsFieldGroupTest.nextTree());
                        // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:367:54: ( boost )?
                        if ( stream_boost.hasNext() ) {
                            adaptor.addChild(root_1, stream_boost.nextTree());

                        }
                        stream_boost.reset();

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
    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:371:1: ftsFieldGroupTest : ( ftsFieldGroupTerm ( ( fuzzy )=> fuzzy )? -> ^( FG_TERM ftsFieldGroupTerm ( fuzzy )? ) | ftsFieldGroupExactTerm ( ( fuzzy )=> fuzzy )? -> ^( FG_EXACT_TERM ftsFieldGroupExactTerm ( fuzzy )? ) | ftsFieldGroupPhrase ( ( slop )=> slop )? -> ^( FG_PHRASE ftsFieldGroupPhrase ( slop )? ) | ftsFieldGroupSynonym ( ( fuzzy )=> fuzzy )? -> ^( FG_SYNONYM ftsFieldGroupSynonym ( fuzzy )? ) | ftsFieldGroupProximity -> ^( FG_PROXIMITY ftsFieldGroupProximity ) | ftsFieldGroupRange -> ^( FG_RANGE ftsFieldGroupRange ) | LPAREN ftsFieldGroupImplicitConjunctionOrDisjunction RPAREN -> ftsFieldGroupImplicitConjunctionOrDisjunction );
    public final FTSParser.ftsFieldGroupTest_return ftsFieldGroupTest() throws RecognitionException {
        FTSParser.ftsFieldGroupTest_return retval = new FTSParser.ftsFieldGroupTest_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token LPAREN107=null;
        Token RPAREN109=null;
        FTSParser.ftsFieldGroupTerm_return ftsFieldGroupTerm97 = null;

        FTSParser.fuzzy_return fuzzy98 = null;

        FTSParser.ftsFieldGroupExactTerm_return ftsFieldGroupExactTerm99 = null;

        FTSParser.fuzzy_return fuzzy100 = null;

        FTSParser.ftsFieldGroupPhrase_return ftsFieldGroupPhrase101 = null;

        FTSParser.slop_return slop102 = null;

        FTSParser.ftsFieldGroupSynonym_return ftsFieldGroupSynonym103 = null;

        FTSParser.fuzzy_return fuzzy104 = null;

        FTSParser.ftsFieldGroupProximity_return ftsFieldGroupProximity105 = null;

        FTSParser.ftsFieldGroupRange_return ftsFieldGroupRange106 = null;

        FTSParser.ftsFieldGroupImplicitConjunctionOrDisjunction_return ftsFieldGroupImplicitConjunctionOrDisjunction108 = null;


        Object LPAREN107_tree=null;
        Object RPAREN109_tree=null;
        RewriteRuleTokenStream stream_RPAREN=new RewriteRuleTokenStream(adaptor,"token RPAREN");
        RewriteRuleTokenStream stream_LPAREN=new RewriteRuleTokenStream(adaptor,"token LPAREN");
        RewriteRuleSubtreeStream stream_ftsFieldGroupRange=new RewriteRuleSubtreeStream(adaptor,"rule ftsFieldGroupRange");
        RewriteRuleSubtreeStream stream_ftsFieldGroupPhrase=new RewriteRuleSubtreeStream(adaptor,"rule ftsFieldGroupPhrase");
        RewriteRuleSubtreeStream stream_ftsFieldGroupImplicitConjunctionOrDisjunction=new RewriteRuleSubtreeStream(adaptor,"rule ftsFieldGroupImplicitConjunctionOrDisjunction");
        RewriteRuleSubtreeStream stream_fuzzy=new RewriteRuleSubtreeStream(adaptor,"rule fuzzy");
        RewriteRuleSubtreeStream stream_slop=new RewriteRuleSubtreeStream(adaptor,"rule slop");
        RewriteRuleSubtreeStream stream_ftsFieldGroupTerm=new RewriteRuleSubtreeStream(adaptor,"rule ftsFieldGroupTerm");
        RewriteRuleSubtreeStream stream_ftsFieldGroupSynonym=new RewriteRuleSubtreeStream(adaptor,"rule ftsFieldGroupSynonym");
        RewriteRuleSubtreeStream stream_ftsFieldGroupExactTerm=new RewriteRuleSubtreeStream(adaptor,"rule ftsFieldGroupExactTerm");
        RewriteRuleSubtreeStream stream_ftsFieldGroupProximity=new RewriteRuleSubtreeStream(adaptor,"rule ftsFieldGroupProximity");
        try {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:372:2: ( ftsFieldGroupTerm ( ( fuzzy )=> fuzzy )? -> ^( FG_TERM ftsFieldGroupTerm ( fuzzy )? ) | ftsFieldGroupExactTerm ( ( fuzzy )=> fuzzy )? -> ^( FG_EXACT_TERM ftsFieldGroupExactTerm ( fuzzy )? ) | ftsFieldGroupPhrase ( ( slop )=> slop )? -> ^( FG_PHRASE ftsFieldGroupPhrase ( slop )? ) | ftsFieldGroupSynonym ( ( fuzzy )=> fuzzy )? -> ^( FG_SYNONYM ftsFieldGroupSynonym ( fuzzy )? ) | ftsFieldGroupProximity -> ^( FG_PROXIMITY ftsFieldGroupProximity ) | ftsFieldGroupRange -> ^( FG_RANGE ftsFieldGroupRange ) | LPAREN ftsFieldGroupImplicitConjunctionOrDisjunction RPAREN -> ftsFieldGroupImplicitConjunctionOrDisjunction )
            int alt38=7;
            alt38 = dfa38.predict(input);
            switch (alt38) {
                case 1 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:372:4: ftsFieldGroupTerm ( ( fuzzy )=> fuzzy )?
                    {
                    pushFollow(FOLLOW_ftsFieldGroupTerm_in_ftsFieldGroupTest1490);
                    ftsFieldGroupTerm97=ftsFieldGroupTerm();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_ftsFieldGroupTerm.add(ftsFieldGroupTerm97.getTree());
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:372:22: ( ( fuzzy )=> fuzzy )?
                    int alt34=2;
                    int LA34_0 = input.LA(1);

                    if ( (LA34_0==TILDA) ) {
                        int LA34_1 = input.LA(2);

                        if ( (LA34_1==DECIMAL_INTEGER_LITERAL||LA34_1==FLOATING_POINT_LITERAL) ) {
                            int LA34_3 = input.LA(3);

                            if ( (synpred11_FTS()) ) {
                                alt34=1;
                            }
                        }
                    }
                    switch (alt34) {
                        case 1 :
                            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:372:23: ( fuzzy )=> fuzzy
                            {
                            pushFollow(FOLLOW_fuzzy_in_ftsFieldGroupTest1499);
                            fuzzy98=fuzzy();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_fuzzy.add(fuzzy98.getTree());

                            }
                            break;

                    }



                    // AST REWRITE
                    // elements: ftsFieldGroupTerm, fuzzy
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 373:3: -> ^( FG_TERM ftsFieldGroupTerm ( fuzzy )? )
                    {
                        // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:373:6: ^( FG_TERM ftsFieldGroupTerm ( fuzzy )? )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(FG_TERM, "FG_TERM"), root_1);

                        adaptor.addChild(root_1, stream_ftsFieldGroupTerm.nextTree());
                        // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:373:34: ( fuzzy )?
                        if ( stream_fuzzy.hasNext() ) {
                            adaptor.addChild(root_1, stream_fuzzy.nextTree());

                        }
                        stream_fuzzy.reset();

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:374:4: ftsFieldGroupExactTerm ( ( fuzzy )=> fuzzy )?
                    {
                    pushFollow(FOLLOW_ftsFieldGroupExactTerm_in_ftsFieldGroupTest1519);
                    ftsFieldGroupExactTerm99=ftsFieldGroupExactTerm();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_ftsFieldGroupExactTerm.add(ftsFieldGroupExactTerm99.getTree());
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:374:27: ( ( fuzzy )=> fuzzy )?
                    int alt35=2;
                    int LA35_0 = input.LA(1);

                    if ( (LA35_0==TILDA) ) {
                        int LA35_1 = input.LA(2);

                        if ( (LA35_1==DECIMAL_INTEGER_LITERAL||LA35_1==FLOATING_POINT_LITERAL) ) {
                            int LA35_3 = input.LA(3);

                            if ( (synpred12_FTS()) ) {
                                alt35=1;
                            }
                        }
                    }
                    switch (alt35) {
                        case 1 :
                            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:374:28: ( fuzzy )=> fuzzy
                            {
                            pushFollow(FOLLOW_fuzzy_in_ftsFieldGroupTest1528);
                            fuzzy100=fuzzy();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_fuzzy.add(fuzzy100.getTree());

                            }
                            break;

                    }



                    // AST REWRITE
                    // elements: ftsFieldGroupExactTerm, fuzzy
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 375:3: -> ^( FG_EXACT_TERM ftsFieldGroupExactTerm ( fuzzy )? )
                    {
                        // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:375:6: ^( FG_EXACT_TERM ftsFieldGroupExactTerm ( fuzzy )? )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(FG_EXACT_TERM, "FG_EXACT_TERM"), root_1);

                        adaptor.addChild(root_1, stream_ftsFieldGroupExactTerm.nextTree());
                        // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:375:45: ( fuzzy )?
                        if ( stream_fuzzy.hasNext() ) {
                            adaptor.addChild(root_1, stream_fuzzy.nextTree());

                        }
                        stream_fuzzy.reset();

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 3 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:376:4: ftsFieldGroupPhrase ( ( slop )=> slop )?
                    {
                    pushFollow(FOLLOW_ftsFieldGroupPhrase_in_ftsFieldGroupTest1548);
                    ftsFieldGroupPhrase101=ftsFieldGroupPhrase();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_ftsFieldGroupPhrase.add(ftsFieldGroupPhrase101.getTree());
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:376:24: ( ( slop )=> slop )?
                    int alt36=2;
                    int LA36_0 = input.LA(1);

                    if ( (LA36_0==TILDA) ) {
                        int LA36_1 = input.LA(2);

                        if ( (LA36_1==DECIMAL_INTEGER_LITERAL) ) {
                            int LA36_3 = input.LA(3);

                            if ( (synpred13_FTS()) ) {
                                alt36=1;
                            }
                        }
                    }
                    switch (alt36) {
                        case 1 :
                            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:376:25: ( slop )=> slop
                            {
                            pushFollow(FOLLOW_slop_in_ftsFieldGroupTest1557);
                            slop102=slop();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_slop.add(slop102.getTree());

                            }
                            break;

                    }



                    // AST REWRITE
                    // elements: slop, ftsFieldGroupPhrase
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 377:3: -> ^( FG_PHRASE ftsFieldGroupPhrase ( slop )? )
                    {
                        // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:377:6: ^( FG_PHRASE ftsFieldGroupPhrase ( slop )? )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(FG_PHRASE, "FG_PHRASE"), root_1);

                        adaptor.addChild(root_1, stream_ftsFieldGroupPhrase.nextTree());
                        // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:377:38: ( slop )?
                        if ( stream_slop.hasNext() ) {
                            adaptor.addChild(root_1, stream_slop.nextTree());

                        }
                        stream_slop.reset();

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 4 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:378:4: ftsFieldGroupSynonym ( ( fuzzy )=> fuzzy )?
                    {
                    pushFollow(FOLLOW_ftsFieldGroupSynonym_in_ftsFieldGroupTest1579);
                    ftsFieldGroupSynonym103=ftsFieldGroupSynonym();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_ftsFieldGroupSynonym.add(ftsFieldGroupSynonym103.getTree());
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:378:25: ( ( fuzzy )=> fuzzy )?
                    int alt37=2;
                    int LA37_0 = input.LA(1);

                    if ( (LA37_0==TILDA) ) {
                        int LA37_1 = input.LA(2);

                        if ( (LA37_1==DECIMAL_INTEGER_LITERAL||LA37_1==FLOATING_POINT_LITERAL) ) {
                            int LA37_3 = input.LA(3);

                            if ( (synpred14_FTS()) ) {
                                alt37=1;
                            }
                        }
                    }
                    switch (alt37) {
                        case 1 :
                            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:378:26: ( fuzzy )=> fuzzy
                            {
                            pushFollow(FOLLOW_fuzzy_in_ftsFieldGroupTest1588);
                            fuzzy104=fuzzy();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_fuzzy.add(fuzzy104.getTree());

                            }
                            break;

                    }



                    // AST REWRITE
                    // elements: fuzzy, ftsFieldGroupSynonym
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 379:3: -> ^( FG_SYNONYM ftsFieldGroupSynonym ( fuzzy )? )
                    {
                        // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:379:6: ^( FG_SYNONYM ftsFieldGroupSynonym ( fuzzy )? )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(FG_SYNONYM, "FG_SYNONYM"), root_1);

                        adaptor.addChild(root_1, stream_ftsFieldGroupSynonym.nextTree());
                        // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:379:40: ( fuzzy )?
                        if ( stream_fuzzy.hasNext() ) {
                            adaptor.addChild(root_1, stream_fuzzy.nextTree());

                        }
                        stream_fuzzy.reset();

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 5 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:380:6: ftsFieldGroupProximity
                    {
                    pushFollow(FOLLOW_ftsFieldGroupProximity_in_ftsFieldGroupTest1610);
                    ftsFieldGroupProximity105=ftsFieldGroupProximity();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_ftsFieldGroupProximity.add(ftsFieldGroupProximity105.getTree());


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
                    // 381:3: -> ^( FG_PROXIMITY ftsFieldGroupProximity )
                    {
                        // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:381:6: ^( FG_PROXIMITY ftsFieldGroupProximity )
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
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:382:9: ftsFieldGroupRange
                    {
                    pushFollow(FOLLOW_ftsFieldGroupRange_in_ftsFieldGroupTest1630);
                    ftsFieldGroupRange106=ftsFieldGroupRange();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_ftsFieldGroupRange.add(ftsFieldGroupRange106.getTree());


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
                    // 383:10: -> ^( FG_RANGE ftsFieldGroupRange )
                    {
                        // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:383:13: ^( FG_RANGE ftsFieldGroupRange )
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
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:384:5: LPAREN ftsFieldGroupImplicitConjunctionOrDisjunction RPAREN
                    {
                    LPAREN107=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_ftsFieldGroupTest1653); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_LPAREN.add(LPAREN107);

                    pushFollow(FOLLOW_ftsFieldGroupImplicitConjunctionOrDisjunction_in_ftsFieldGroupTest1655);
                    ftsFieldGroupImplicitConjunctionOrDisjunction108=ftsFieldGroupImplicitConjunctionOrDisjunction();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_ftsFieldGroupImplicitConjunctionOrDisjunction.add(ftsFieldGroupImplicitConjunctionOrDisjunction108.getTree());
                    RPAREN109=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_ftsFieldGroupTest1657); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_RPAREN.add(RPAREN109);



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
                    // 385:3: -> ftsFieldGroupImplicitConjunctionOrDisjunction
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
    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:388:1: ftsFieldGroupTerm : ftsWord ;
    public final FTSParser.ftsFieldGroupTerm_return ftsFieldGroupTerm() throws RecognitionException {
        FTSParser.ftsFieldGroupTerm_return retval = new FTSParser.ftsFieldGroupTerm_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        FTSParser.ftsWord_return ftsWord110 = null;



        try {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:389:2: ( ftsWord )
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:389:4: ftsWord
            {
            root_0 = (Object)adaptor.nil();

            pushFollow(FOLLOW_ftsWord_in_ftsFieldGroupTerm1677);
            ftsWord110=ftsWord();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, ftsWord110.getTree());

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
    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:392:1: ftsFieldGroupExactTerm : EQUALS ftsFieldGroupTerm -> ftsFieldGroupTerm ;
    public final FTSParser.ftsFieldGroupExactTerm_return ftsFieldGroupExactTerm() throws RecognitionException {
        FTSParser.ftsFieldGroupExactTerm_return retval = new FTSParser.ftsFieldGroupExactTerm_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token EQUALS111=null;
        FTSParser.ftsFieldGroupTerm_return ftsFieldGroupTerm112 = null;


        Object EQUALS111_tree=null;
        RewriteRuleTokenStream stream_EQUALS=new RewriteRuleTokenStream(adaptor,"token EQUALS");
        RewriteRuleSubtreeStream stream_ftsFieldGroupTerm=new RewriteRuleSubtreeStream(adaptor,"rule ftsFieldGroupTerm");
        try {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:393:2: ( EQUALS ftsFieldGroupTerm -> ftsFieldGroupTerm )
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:393:4: EQUALS ftsFieldGroupTerm
            {
            EQUALS111=(Token)match(input,EQUALS,FOLLOW_EQUALS_in_ftsFieldGroupExactTerm1689); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_EQUALS.add(EQUALS111);

            pushFollow(FOLLOW_ftsFieldGroupTerm_in_ftsFieldGroupExactTerm1691);
            ftsFieldGroupTerm112=ftsFieldGroupTerm();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_ftsFieldGroupTerm.add(ftsFieldGroupTerm112.getTree());


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
            // 394:3: -> ftsFieldGroupTerm
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
    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:397:1: ftsFieldGroupPhrase : FTSPHRASE ;
    public final FTSParser.ftsFieldGroupPhrase_return ftsFieldGroupPhrase() throws RecognitionException {
        FTSParser.ftsFieldGroupPhrase_return retval = new FTSParser.ftsFieldGroupPhrase_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token FTSPHRASE113=null;

        Object FTSPHRASE113_tree=null;

        try {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:398:2: ( FTSPHRASE )
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:398:6: FTSPHRASE
            {
            root_0 = (Object)adaptor.nil();

            FTSPHRASE113=(Token)match(input,FTSPHRASE,FOLLOW_FTSPHRASE_in_ftsFieldGroupPhrase1711); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            FTSPHRASE113_tree = (Object)adaptor.create(FTSPHRASE113);
            adaptor.addChild(root_0, FTSPHRASE113_tree);
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
    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:401:1: ftsFieldGroupSynonym : TILDA ftsFieldGroupTerm -> ftsFieldGroupTerm ;
    public final FTSParser.ftsFieldGroupSynonym_return ftsFieldGroupSynonym() throws RecognitionException {
        FTSParser.ftsFieldGroupSynonym_return retval = new FTSParser.ftsFieldGroupSynonym_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token TILDA114=null;
        FTSParser.ftsFieldGroupTerm_return ftsFieldGroupTerm115 = null;


        Object TILDA114_tree=null;
        RewriteRuleTokenStream stream_TILDA=new RewriteRuleTokenStream(adaptor,"token TILDA");
        RewriteRuleSubtreeStream stream_ftsFieldGroupTerm=new RewriteRuleSubtreeStream(adaptor,"rule ftsFieldGroupTerm");
        try {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:402:2: ( TILDA ftsFieldGroupTerm -> ftsFieldGroupTerm )
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:402:4: TILDA ftsFieldGroupTerm
            {
            TILDA114=(Token)match(input,TILDA,FOLLOW_TILDA_in_ftsFieldGroupSynonym1723); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_TILDA.add(TILDA114);

            pushFollow(FOLLOW_ftsFieldGroupTerm_in_ftsFieldGroupSynonym1725);
            ftsFieldGroupTerm115=ftsFieldGroupTerm();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_ftsFieldGroupTerm.add(ftsFieldGroupTerm115.getTree());


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
            // 403:3: -> ftsFieldGroupTerm
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
    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:406:1: ftsFieldGroupProximity : ftsFieldGroupTerm ( proximityGroup ftsFieldGroupTerm )+ -> ftsFieldGroupTerm ( proximityGroup ftsFieldGroupTerm )+ ;
    public final FTSParser.ftsFieldGroupProximity_return ftsFieldGroupProximity() throws RecognitionException {
        FTSParser.ftsFieldGroupProximity_return retval = new FTSParser.ftsFieldGroupProximity_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        FTSParser.ftsFieldGroupTerm_return ftsFieldGroupTerm116 = null;

        FTSParser.proximityGroup_return proximityGroup117 = null;

        FTSParser.ftsFieldGroupTerm_return ftsFieldGroupTerm118 = null;


        RewriteRuleSubtreeStream stream_ftsFieldGroupTerm=new RewriteRuleSubtreeStream(adaptor,"rule ftsFieldGroupTerm");
        RewriteRuleSubtreeStream stream_proximityGroup=new RewriteRuleSubtreeStream(adaptor,"rule proximityGroup");
        try {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:407:2: ( ftsFieldGroupTerm ( proximityGroup ftsFieldGroupTerm )+ -> ftsFieldGroupTerm ( proximityGroup ftsFieldGroupTerm )+ )
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:407:4: ftsFieldGroupTerm ( proximityGroup ftsFieldGroupTerm )+
            {
            pushFollow(FOLLOW_ftsFieldGroupTerm_in_ftsFieldGroupProximity1743);
            ftsFieldGroupTerm116=ftsFieldGroupTerm();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_ftsFieldGroupTerm.add(ftsFieldGroupTerm116.getTree());
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:407:22: ( proximityGroup ftsFieldGroupTerm )+
            int cnt39=0;
            loop39:
            do {
                int alt39=2;
                int LA39_0 = input.LA(1);

                if ( (LA39_0==STAR) ) {
                    alt39=1;
                }


                switch (alt39) {
            	case 1 :
            	    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:407:23: proximityGroup ftsFieldGroupTerm
            	    {
            	    pushFollow(FOLLOW_proximityGroup_in_ftsFieldGroupProximity1746);
            	    proximityGroup117=proximityGroup();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_proximityGroup.add(proximityGroup117.getTree());
            	    pushFollow(FOLLOW_ftsFieldGroupTerm_in_ftsFieldGroupProximity1748);
            	    ftsFieldGroupTerm118=ftsFieldGroupTerm();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_ftsFieldGroupTerm.add(ftsFieldGroupTerm118.getTree());

            	    }
            	    break;

            	default :
            	    if ( cnt39 >= 1 ) break loop39;
            	    if (state.backtracking>0) {state.failed=true; return retval;}
                        EarlyExitException eee =
                            new EarlyExitException(39, input);
                        throw eee;
                }
                cnt39++;
            } while (true);



            // AST REWRITE
            // elements: ftsFieldGroupTerm, proximityGroup, ftsFieldGroupTerm
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 408:3: -> ftsFieldGroupTerm ( proximityGroup ftsFieldGroupTerm )+
            {
                adaptor.addChild(root_0, stream_ftsFieldGroupTerm.nextTree());
                if ( !(stream_ftsFieldGroupTerm.hasNext()||stream_proximityGroup.hasNext()) ) {
                    throw new RewriteEarlyExitException();
                }
                while ( stream_ftsFieldGroupTerm.hasNext()||stream_proximityGroup.hasNext() ) {
                    adaptor.addChild(root_0, stream_proximityGroup.nextTree());
                    adaptor.addChild(root_0, stream_ftsFieldGroupTerm.nextTree());

                }
                stream_ftsFieldGroupTerm.reset();
                stream_proximityGroup.reset();

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

    public static class proximityGroup_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "proximityGroup"
    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:411:1: proximityGroup : STAR ( LPAREN ( DECIMAL_INTEGER_LITERAL )? RPAREN )? -> ^( PROXIMITY ( DECIMAL_INTEGER_LITERAL )? ) ;
    public final FTSParser.proximityGroup_return proximityGroup() throws RecognitionException {
        FTSParser.proximityGroup_return retval = new FTSParser.proximityGroup_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token STAR119=null;
        Token LPAREN120=null;
        Token DECIMAL_INTEGER_LITERAL121=null;
        Token RPAREN122=null;

        Object STAR119_tree=null;
        Object LPAREN120_tree=null;
        Object DECIMAL_INTEGER_LITERAL121_tree=null;
        Object RPAREN122_tree=null;
        RewriteRuleTokenStream stream_RPAREN=new RewriteRuleTokenStream(adaptor,"token RPAREN");
        RewriteRuleTokenStream stream_STAR=new RewriteRuleTokenStream(adaptor,"token STAR");
        RewriteRuleTokenStream stream_DECIMAL_INTEGER_LITERAL=new RewriteRuleTokenStream(adaptor,"token DECIMAL_INTEGER_LITERAL");
        RewriteRuleTokenStream stream_LPAREN=new RewriteRuleTokenStream(adaptor,"token LPAREN");

        try {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:412:4: ( STAR ( LPAREN ( DECIMAL_INTEGER_LITERAL )? RPAREN )? -> ^( PROXIMITY ( DECIMAL_INTEGER_LITERAL )? ) )
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:412:7: STAR ( LPAREN ( DECIMAL_INTEGER_LITERAL )? RPAREN )?
            {
            STAR119=(Token)match(input,STAR,FOLLOW_STAR_in_proximityGroup1778); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_STAR.add(STAR119);

            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:412:12: ( LPAREN ( DECIMAL_INTEGER_LITERAL )? RPAREN )?
            int alt41=2;
            int LA41_0 = input.LA(1);

            if ( (LA41_0==LPAREN) ) {
                alt41=1;
            }
            switch (alt41) {
                case 1 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:412:14: LPAREN ( DECIMAL_INTEGER_LITERAL )? RPAREN
                    {
                    LPAREN120=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_proximityGroup1782); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_LPAREN.add(LPAREN120);

                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:412:21: ( DECIMAL_INTEGER_LITERAL )?
                    int alt40=2;
                    int LA40_0 = input.LA(1);

                    if ( (LA40_0==DECIMAL_INTEGER_LITERAL) ) {
                        alt40=1;
                    }
                    switch (alt40) {
                        case 1 :
                            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:412:21: DECIMAL_INTEGER_LITERAL
                            {
                            DECIMAL_INTEGER_LITERAL121=(Token)match(input,DECIMAL_INTEGER_LITERAL,FOLLOW_DECIMAL_INTEGER_LITERAL_in_proximityGroup1784); if (state.failed) return retval; 
                            if ( state.backtracking==0 ) stream_DECIMAL_INTEGER_LITERAL.add(DECIMAL_INTEGER_LITERAL121);


                            }
                            break;

                    }

                    RPAREN122=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_proximityGroup1787); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_RPAREN.add(RPAREN122);


                    }
                    break;

            }



            // AST REWRITE
            // elements: DECIMAL_INTEGER_LITERAL
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 413:5: -> ^( PROXIMITY ( DECIMAL_INTEGER_LITERAL )? )
            {
                // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:413:8: ^( PROXIMITY ( DECIMAL_INTEGER_LITERAL )? )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(PROXIMITY, "PROXIMITY"), root_1);

                // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:413:20: ( DECIMAL_INTEGER_LITERAL )?
                if ( stream_DECIMAL_INTEGER_LITERAL.hasNext() ) {
                    adaptor.addChild(root_1, stream_DECIMAL_INTEGER_LITERAL.nextNode());

                }
                stream_DECIMAL_INTEGER_LITERAL.reset();

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
    // $ANTLR end "proximityGroup"

    public static class ftsFieldGroupRange_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "ftsFieldGroupRange"
    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:416:1: ftsFieldGroupRange : ( ftsRangeWord DOTDOT ftsRangeWord -> INCLUSIVE ftsRangeWord ftsRangeWord INCLUSIVE | range_left ftsRangeWord TO ftsRangeWord range_right -> range_left ftsRangeWord ftsRangeWord range_right );
    public final FTSParser.ftsFieldGroupRange_return ftsFieldGroupRange() throws RecognitionException {
        FTSParser.ftsFieldGroupRange_return retval = new FTSParser.ftsFieldGroupRange_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token DOTDOT124=null;
        Token TO128=null;
        FTSParser.ftsRangeWord_return ftsRangeWord123 = null;

        FTSParser.ftsRangeWord_return ftsRangeWord125 = null;

        FTSParser.range_left_return range_left126 = null;

        FTSParser.ftsRangeWord_return ftsRangeWord127 = null;

        FTSParser.ftsRangeWord_return ftsRangeWord129 = null;

        FTSParser.range_right_return range_right130 = null;


        Object DOTDOT124_tree=null;
        Object TO128_tree=null;
        RewriteRuleTokenStream stream_DOTDOT=new RewriteRuleTokenStream(adaptor,"token DOTDOT");
        RewriteRuleTokenStream stream_TO=new RewriteRuleTokenStream(adaptor,"token TO");
        RewriteRuleSubtreeStream stream_range_left=new RewriteRuleSubtreeStream(adaptor,"rule range_left");
        RewriteRuleSubtreeStream stream_range_right=new RewriteRuleSubtreeStream(adaptor,"rule range_right");
        RewriteRuleSubtreeStream stream_ftsRangeWord=new RewriteRuleSubtreeStream(adaptor,"rule ftsRangeWord");
        try {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:417:9: ( ftsRangeWord DOTDOT ftsRangeWord -> INCLUSIVE ftsRangeWord ftsRangeWord INCLUSIVE | range_left ftsRangeWord TO ftsRangeWord range_right -> range_left ftsRangeWord ftsRangeWord range_right )
            int alt42=2;
            int LA42_0 = input.LA(1);

            if ( (LA42_0==DECIMAL_INTEGER_LITERAL||LA42_0==FTSPHRASE||(LA42_0>=ID && LA42_0<=FTSWILD)||LA42_0==FLOATING_POINT_LITERAL) ) {
                alt42=1;
            }
            else if ( ((LA42_0>=LSQUARE && LA42_0<=LT)) ) {
                alt42=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 42, 0, input);

                throw nvae;
            }
            switch (alt42) {
                case 1 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:417:11: ftsRangeWord DOTDOT ftsRangeWord
                    {
                    pushFollow(FOLLOW_ftsRangeWord_in_ftsFieldGroupRange1823);
                    ftsRangeWord123=ftsRangeWord();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_ftsRangeWord.add(ftsRangeWord123.getTree());
                    DOTDOT124=(Token)match(input,DOTDOT,FOLLOW_DOTDOT_in_ftsFieldGroupRange1825); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_DOTDOT.add(DOTDOT124);

                    pushFollow(FOLLOW_ftsRangeWord_in_ftsFieldGroupRange1827);
                    ftsRangeWord125=ftsRangeWord();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_ftsRangeWord.add(ftsRangeWord125.getTree());


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
                    // 418:3: -> INCLUSIVE ftsRangeWord ftsRangeWord INCLUSIVE
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
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:419:4: range_left ftsRangeWord TO ftsRangeWord range_right
                    {
                    pushFollow(FOLLOW_range_left_in_ftsFieldGroupRange1844);
                    range_left126=range_left();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_range_left.add(range_left126.getTree());
                    pushFollow(FOLLOW_ftsRangeWord_in_ftsFieldGroupRange1846);
                    ftsRangeWord127=ftsRangeWord();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_ftsRangeWord.add(ftsRangeWord127.getTree());
                    TO128=(Token)match(input,TO,FOLLOW_TO_in_ftsFieldGroupRange1848); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_TO.add(TO128);

                    pushFollow(FOLLOW_ftsRangeWord_in_ftsFieldGroupRange1850);
                    ftsRangeWord129=ftsRangeWord();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_ftsRangeWord.add(ftsRangeWord129.getTree());
                    pushFollow(FOLLOW_range_right_in_ftsFieldGroupRange1852);
                    range_right130=range_right();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_range_right.add(range_right130.getTree());


                    // AST REWRITE
                    // elements: ftsRangeWord, range_right, range_left, ftsRangeWord
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 420:3: -> range_left ftsRangeWord ftsRangeWord range_right
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
    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:423:1: range_left : ( LSQUARE -> INCLUSIVE | LT -> EXCLUSIVE );
    public final FTSParser.range_left_return range_left() throws RecognitionException {
        FTSParser.range_left_return retval = new FTSParser.range_left_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token LSQUARE131=null;
        Token LT132=null;

        Object LSQUARE131_tree=null;
        Object LT132_tree=null;
        RewriteRuleTokenStream stream_LT=new RewriteRuleTokenStream(adaptor,"token LT");
        RewriteRuleTokenStream stream_LSQUARE=new RewriteRuleTokenStream(adaptor,"token LSQUARE");

        try {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:424:2: ( LSQUARE -> INCLUSIVE | LT -> EXCLUSIVE )
            int alt43=2;
            int LA43_0 = input.LA(1);

            if ( (LA43_0==LSQUARE) ) {
                alt43=1;
            }
            else if ( (LA43_0==LT) ) {
                alt43=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 43, 0, input);

                throw nvae;
            }
            switch (alt43) {
                case 1 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:424:10: LSQUARE
                    {
                    LSQUARE131=(Token)match(input,LSQUARE,FOLLOW_LSQUARE_in_range_left1882); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_LSQUARE.add(LSQUARE131);



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
                    // 425:3: -> INCLUSIVE
                    {
                        adaptor.addChild(root_0, (Object)adaptor.create(INCLUSIVE, "INCLUSIVE"));

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:426:4: LT
                    {
                    LT132=(Token)match(input,LT,FOLLOW_LT_in_range_left1893); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_LT.add(LT132);



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
                    // 427:3: -> EXCLUSIVE
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
    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:430:1: range_right : ( RSQUARE -> INCLUSIVE | GT -> EXCLUSIVE );
    public final FTSParser.range_right_return range_right() throws RecognitionException {
        FTSParser.range_right_return retval = new FTSParser.range_right_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token RSQUARE133=null;
        Token GT134=null;

        Object RSQUARE133_tree=null;
        Object GT134_tree=null;
        RewriteRuleTokenStream stream_GT=new RewriteRuleTokenStream(adaptor,"token GT");
        RewriteRuleTokenStream stream_RSQUARE=new RewriteRuleTokenStream(adaptor,"token RSQUARE");

        try {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:431:2: ( RSQUARE -> INCLUSIVE | GT -> EXCLUSIVE )
            int alt44=2;
            int LA44_0 = input.LA(1);

            if ( (LA44_0==RSQUARE) ) {
                alt44=1;
            }
            else if ( (LA44_0==GT) ) {
                alt44=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 44, 0, input);

                throw nvae;
            }
            switch (alt44) {
                case 1 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:431:10: RSQUARE
                    {
                    RSQUARE133=(Token)match(input,RSQUARE,FOLLOW_RSQUARE_in_range_right1917); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_RSQUARE.add(RSQUARE133);



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
                    // 432:3: -> INCLUSIVE
                    {
                        adaptor.addChild(root_0, (Object)adaptor.create(INCLUSIVE, "INCLUSIVE"));

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:433:4: GT
                    {
                    GT134=(Token)match(input,GT,FOLLOW_GT_in_range_right1928); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_GT.add(GT134);



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
                    // 434:3: -> EXCLUSIVE
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

    public static class fieldReference_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "fieldReference"
    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:438:1: fieldReference : ( AT )? ( prefix | uri )? identifier -> ^( FIELD_REF identifier ( prefix )? ( uri )? ) ;
    public final FTSParser.fieldReference_return fieldReference() throws RecognitionException {
        FTSParser.fieldReference_return retval = new FTSParser.fieldReference_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token AT135=null;
        FTSParser.prefix_return prefix136 = null;

        FTSParser.uri_return uri137 = null;

        FTSParser.identifier_return identifier138 = null;


        Object AT135_tree=null;
        RewriteRuleTokenStream stream_AT=new RewriteRuleTokenStream(adaptor,"token AT");
        RewriteRuleSubtreeStream stream_prefix=new RewriteRuleSubtreeStream(adaptor,"rule prefix");
        RewriteRuleSubtreeStream stream_uri=new RewriteRuleSubtreeStream(adaptor,"rule uri");
        RewriteRuleSubtreeStream stream_identifier=new RewriteRuleSubtreeStream(adaptor,"rule identifier");
        try {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:439:2: ( ( AT )? ( prefix | uri )? identifier -> ^( FIELD_REF identifier ( prefix )? ( uri )? ) )
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:439:5: ( AT )? ( prefix | uri )? identifier
            {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:439:5: ( AT )?
            int alt45=2;
            int LA45_0 = input.LA(1);

            if ( (LA45_0==AT) ) {
                alt45=1;
            }
            switch (alt45) {
                case 1 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:439:5: AT
                    {
                    AT135=(Token)match(input,AT,FOLLOW_AT_in_fieldReference1950); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_AT.add(AT135);


                    }
                    break;

            }

            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:439:9: ( prefix | uri )?
            int alt46=3;
            int LA46_0 = input.LA(1);

            if ( (LA46_0==ID) ) {
                int LA46_1 = input.LA(2);

                if ( (LA46_1==COLON) ) {
                    int LA46_3 = input.LA(3);

                    if ( (LA46_3==ID) ) {
                        int LA46_5 = input.LA(4);

                        if ( (LA46_5==COLON) ) {
                            alt46=1;
                        }
                    }
                }
            }
            else if ( (LA46_0==URI) ) {
                alt46=2;
            }
            switch (alt46) {
                case 1 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:439:10: prefix
                    {
                    pushFollow(FOLLOW_prefix_in_fieldReference1954);
                    prefix136=prefix();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_prefix.add(prefix136.getTree());

                    }
                    break;
                case 2 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:439:17: uri
                    {
                    pushFollow(FOLLOW_uri_in_fieldReference1956);
                    uri137=uri();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_uri.add(uri137.getTree());

                    }
                    break;

            }

            pushFollow(FOLLOW_identifier_in_fieldReference1960);
            identifier138=identifier();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_identifier.add(identifier138.getTree());


            // AST REWRITE
            // elements: prefix, uri, identifier
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 440:7: -> ^( FIELD_REF identifier ( prefix )? ( uri )? )
            {
                // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:440:10: ^( FIELD_REF identifier ( prefix )? ( uri )? )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(FIELD_REF, "FIELD_REF"), root_1);

                adaptor.addChild(root_1, stream_identifier.nextTree());
                // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:440:33: ( prefix )?
                if ( stream_prefix.hasNext() ) {
                    adaptor.addChild(root_1, stream_prefix.nextTree());

                }
                stream_prefix.reset();
                // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:440:41: ( uri )?
                if ( stream_uri.hasNext() ) {
                    adaptor.addChild(root_1, stream_uri.nextTree());

                }
                stream_uri.reset();

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
    // $ANTLR end "fieldReference"

    public static class tempReference_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "tempReference"
    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:443:1: tempReference : ( AT )? ( prefix | uri )? identifier -> ^( FIELD_REF identifier ( prefix )? ( uri )? ) ;
    public final FTSParser.tempReference_return tempReference() throws RecognitionException {
        FTSParser.tempReference_return retval = new FTSParser.tempReference_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token AT139=null;
        FTSParser.prefix_return prefix140 = null;

        FTSParser.uri_return uri141 = null;

        FTSParser.identifier_return identifier142 = null;


        Object AT139_tree=null;
        RewriteRuleTokenStream stream_AT=new RewriteRuleTokenStream(adaptor,"token AT");
        RewriteRuleSubtreeStream stream_prefix=new RewriteRuleSubtreeStream(adaptor,"rule prefix");
        RewriteRuleSubtreeStream stream_uri=new RewriteRuleSubtreeStream(adaptor,"rule uri");
        RewriteRuleSubtreeStream stream_identifier=new RewriteRuleSubtreeStream(adaptor,"rule identifier");
        try {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:444:2: ( ( AT )? ( prefix | uri )? identifier -> ^( FIELD_REF identifier ( prefix )? ( uri )? ) )
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:444:5: ( AT )? ( prefix | uri )? identifier
            {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:444:5: ( AT )?
            int alt47=2;
            int LA47_0 = input.LA(1);

            if ( (LA47_0==AT) ) {
                alt47=1;
            }
            switch (alt47) {
                case 1 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:444:5: AT
                    {
                    AT139=(Token)match(input,AT,FOLLOW_AT_in_tempReference1993); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_AT.add(AT139);


                    }
                    break;

            }

            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:444:9: ( prefix | uri )?
            int alt48=3;
            int LA48_0 = input.LA(1);

            if ( (LA48_0==ID) ) {
                int LA48_1 = input.LA(2);

                if ( (LA48_1==COLON) ) {
                    alt48=1;
                }
            }
            else if ( (LA48_0==URI) ) {
                alt48=2;
            }
            switch (alt48) {
                case 1 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:444:10: prefix
                    {
                    pushFollow(FOLLOW_prefix_in_tempReference1997);
                    prefix140=prefix();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_prefix.add(prefix140.getTree());

                    }
                    break;
                case 2 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:444:17: uri
                    {
                    pushFollow(FOLLOW_uri_in_tempReference1999);
                    uri141=uri();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_uri.add(uri141.getTree());

                    }
                    break;

            }

            pushFollow(FOLLOW_identifier_in_tempReference2003);
            identifier142=identifier();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_identifier.add(identifier142.getTree());


            // AST REWRITE
            // elements: identifier, uri, prefix
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 445:7: -> ^( FIELD_REF identifier ( prefix )? ( uri )? )
            {
                // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:445:10: ^( FIELD_REF identifier ( prefix )? ( uri )? )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(FIELD_REF, "FIELD_REF"), root_1);

                adaptor.addChild(root_1, stream_identifier.nextTree());
                // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:445:33: ( prefix )?
                if ( stream_prefix.hasNext() ) {
                    adaptor.addChild(root_1, stream_prefix.nextTree());

                }
                stream_prefix.reset();
                // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:445:41: ( uri )?
                if ( stream_uri.hasNext() ) {
                    adaptor.addChild(root_1, stream_uri.nextTree());

                }
                stream_uri.reset();

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
    // $ANTLR end "tempReference"

    public static class prefix_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "prefix"
    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:448:1: prefix : identifier COLON -> ^( PREFIX identifier ) ;
    public final FTSParser.prefix_return prefix() throws RecognitionException {
        FTSParser.prefix_return retval = new FTSParser.prefix_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token COLON144=null;
        FTSParser.identifier_return identifier143 = null;


        Object COLON144_tree=null;
        RewriteRuleTokenStream stream_COLON=new RewriteRuleTokenStream(adaptor,"token COLON");
        RewriteRuleSubtreeStream stream_identifier=new RewriteRuleSubtreeStream(adaptor,"rule identifier");
        try {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:449:2: ( identifier COLON -> ^( PREFIX identifier ) )
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:449:5: identifier COLON
            {
            pushFollow(FOLLOW_identifier_in_prefix2036);
            identifier143=identifier();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_identifier.add(identifier143.getTree());
            COLON144=(Token)match(input,COLON,FOLLOW_COLON_in_prefix2038); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_COLON.add(COLON144);



            // AST REWRITE
            // elements: identifier
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 450:3: -> ^( PREFIX identifier )
            {
                // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:450:6: ^( PREFIX identifier )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(PREFIX, "PREFIX"), root_1);

                adaptor.addChild(root_1, stream_identifier.nextTree());

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
    // $ANTLR end "prefix"

    public static class uri_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "uri"
    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:453:1: uri : URI -> ^( NAME_SPACE URI ) ;
    public final FTSParser.uri_return uri() throws RecognitionException {
        FTSParser.uri_return retval = new FTSParser.uri_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token URI145=null;

        Object URI145_tree=null;
        RewriteRuleTokenStream stream_URI=new RewriteRuleTokenStream(adaptor,"token URI");

        try {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:454:4: ( URI -> ^( NAME_SPACE URI ) )
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:454:7: URI
            {
            URI145=(Token)match(input,URI,FOLLOW_URI_in_uri2064); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_URI.add(URI145);



            // AST REWRITE
            // elements: URI
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 455:5: -> ^( NAME_SPACE URI )
            {
                // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:455:8: ^( NAME_SPACE URI )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(NAME_SPACE, "NAME_SPACE"), root_1);

                adaptor.addChild(root_1, stream_URI.nextNode());

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
    // $ANTLR end "uri"

    public static class identifier_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "identifier"
    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:458:1: identifier : ID ;
    public final FTSParser.identifier_return identifier() throws RecognitionException {
        FTSParser.identifier_return retval = new FTSParser.identifier_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token ID146=null;

        Object ID146_tree=null;

        try {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:459:2: ( ID )
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:459:4: ID
            {
            root_0 = (Object)adaptor.nil();

            ID146=(Token)match(input,ID,FOLLOW_ID_in_identifier2090); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            ID146_tree = (Object)adaptor.create(ID146);
            adaptor.addChild(root_0, ID146_tree);
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
    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:462:1: ftsWord : ( ID | FTSWORD | FTSPRE | FTSWILD | OR | AND | NOT | TO | DECIMAL_INTEGER_LITERAL | FLOATING_POINT_LITERAL );
    public final FTSParser.ftsWord_return ftsWord() throws RecognitionException {
        FTSParser.ftsWord_return retval = new FTSParser.ftsWord_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token set147=null;

        Object set147_tree=null;

        try {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:463:6: ( ID | FTSWORD | FTSPRE | FTSWILD | OR | AND | NOT | TO | DECIMAL_INTEGER_LITERAL | FLOATING_POINT_LITERAL )
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:
            {
            root_0 = (Object)adaptor.nil();

            set147=(Token)input.LT(1);
            if ( input.LA(1)==DECIMAL_INTEGER_LITERAL||input.LA(1)==TO||(input.LA(1)>=ID && input.LA(1)<=FLOATING_POINT_LITERAL) ) {
                input.consume();
                if ( state.backtracking==0 ) adaptor.addChild(root_0, (Object)adaptor.create(set147));
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

    public static class number_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "number"
    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:475:1: number : ( DECIMAL_INTEGER_LITERAL | FLOATING_POINT_LITERAL );
    public final FTSParser.number_return number() throws RecognitionException {
        FTSParser.number_return retval = new FTSParser.number_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token set148=null;

        Object set148_tree=null;

        try {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:476:6: ( DECIMAL_INTEGER_LITERAL | FLOATING_POINT_LITERAL )
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:
            {
            root_0 = (Object)adaptor.nil();

            set148=(Token)input.LT(1);
            if ( input.LA(1)==DECIMAL_INTEGER_LITERAL||input.LA(1)==FLOATING_POINT_LITERAL ) {
                input.consume();
                if ( state.backtracking==0 ) adaptor.addChild(root_0, (Object)adaptor.create(set148));
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
    // $ANTLR end "number"

    public static class ftsRangeWord_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "ftsRangeWord"
    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:480:1: ftsRangeWord : ( ID | FTSWORD | FTSPRE | FTSWILD | FTSPHRASE | DECIMAL_INTEGER_LITERAL | FLOATING_POINT_LITERAL );
    public final FTSParser.ftsRangeWord_return ftsRangeWord() throws RecognitionException {
        FTSParser.ftsRangeWord_return retval = new FTSParser.ftsRangeWord_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token set149=null;

        Object set149_tree=null;

        try {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:481:6: ( ID | FTSWORD | FTSPRE | FTSWILD | FTSPHRASE | DECIMAL_INTEGER_LITERAL | FLOATING_POINT_LITERAL )
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:
            {
            root_0 = (Object)adaptor.nil();

            set149=(Token)input.LT(1);
            if ( input.LA(1)==DECIMAL_INTEGER_LITERAL||input.LA(1)==FTSPHRASE||(input.LA(1)>=ID && input.LA(1)<=FTSWILD)||input.LA(1)==FLOATING_POINT_LITERAL ) {
                input.consume();
                if ( state.backtracking==0 ) adaptor.addChild(root_0, (Object)adaptor.create(set149));
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
    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:490:1: or : ( OR | BAR BAR );
    public final FTSParser.or_return or() throws RecognitionException {
        FTSParser.or_return retval = new FTSParser.or_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token OR150=null;
        Token BAR151=null;
        Token BAR152=null;

        Object OR150_tree=null;
        Object BAR151_tree=null;
        Object BAR152_tree=null;

        try {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:491:6: ( OR | BAR BAR )
            int alt49=2;
            int LA49_0 = input.LA(1);

            if ( (LA49_0==OR) ) {
                alt49=1;
            }
            else if ( (LA49_0==BAR) ) {
                alt49=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 49, 0, input);

                throw nvae;
            }
            switch (alt49) {
                case 1 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:491:11: OR
                    {
                    root_0 = (Object)adaptor.nil();

                    OR150=(Token)match(input,OR,FOLLOW_OR_in_or2374); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    OR150_tree = (Object)adaptor.create(OR150);
                    adaptor.addChild(root_0, OR150_tree);
                    }

                    }
                    break;
                case 2 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:492:8: BAR BAR
                    {
                    root_0 = (Object)adaptor.nil();

                    BAR151=(Token)match(input,BAR,FOLLOW_BAR_in_or2383); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    BAR151_tree = (Object)adaptor.create(BAR151);
                    adaptor.addChild(root_0, BAR151_tree);
                    }
                    BAR152=(Token)match(input,BAR,FOLLOW_BAR_in_or2385); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    BAR152_tree = (Object)adaptor.create(BAR152);
                    adaptor.addChild(root_0, BAR152_tree);
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
    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:495:1: and : ( AND | AMP AMP );
    public final FTSParser.and_return and() throws RecognitionException {
        FTSParser.and_return retval = new FTSParser.and_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token AND153=null;
        Token AMP154=null;
        Token AMP155=null;

        Object AND153_tree=null;
        Object AMP154_tree=null;
        Object AMP155_tree=null;

        try {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:496:6: ( AND | AMP AMP )
            int alt50=2;
            int LA50_0 = input.LA(1);

            if ( (LA50_0==AND) ) {
                alt50=1;
            }
            else if ( (LA50_0==AMP) ) {
                alt50=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 50, 0, input);

                throw nvae;
            }
            switch (alt50) {
                case 1 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:496:8: AND
                    {
                    root_0 = (Object)adaptor.nil();

                    AND153=(Token)match(input,AND,FOLLOW_AND_in_and2409); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    AND153_tree = (Object)adaptor.create(AND153);
                    adaptor.addChild(root_0, AND153_tree);
                    }

                    }
                    break;
                case 2 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:497:8: AMP AMP
                    {
                    root_0 = (Object)adaptor.nil();

                    AMP154=(Token)match(input,AMP,FOLLOW_AMP_in_and2418); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    AMP154_tree = (Object)adaptor.create(AMP154);
                    adaptor.addChild(root_0, AMP154_tree);
                    }
                    AMP155=(Token)match(input,AMP,FOLLOW_AMP_in_and2420); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    AMP155_tree = (Object)adaptor.create(AMP155);
                    adaptor.addChild(root_0, AMP155_tree);
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
    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:500:1: not : ( NOT | EXCLAMATION );
    public final FTSParser.not_return not() throws RecognitionException {
        FTSParser.not_return retval = new FTSParser.not_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token set156=null;

        Object set156_tree=null;

        try {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:501:6: ( NOT | EXCLAMATION )
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:
            {
            root_0 = (Object)adaptor.nil();

            set156=(Token)input.LT(1);
            if ( input.LA(1)==NOT||input.LA(1)==EXCLAMATION ) {
                input.consume();
                if ( state.backtracking==0 ) adaptor.addChild(root_0, (Object)adaptor.create(set156));
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
        // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:230:27: ( or )
        // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:230:28: or
        {
        pushFollow(FOLLOW_or_in_synpred1_FTS340);
        or();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred1_FTS

    // $ANTLR start synpred2_FTS
    public final void synpred2_FTS_fragment() throws RecognitionException {   
        // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:238:17: ( and )
        // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:238:18: and
        {
        pushFollow(FOLLOW_and_in_synpred2_FTS378);
        and();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred2_FTS

    // $ANTLR start synpred3_FTS
    public final void synpred3_FTS_fragment() throws RecognitionException {   
        // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:249:8: ( not )
        // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:249:9: not
        {
        pushFollow(FOLLOW_not_in_synpred3_FTS421);
        not();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred3_FTS

    // $ANTLR start synpred4_FTS
    public final void synpred4_FTS_fragment() throws RecognitionException {   
        // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:265:17: ( fuzzy )
        // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:265:18: fuzzy
        {
        pushFollow(FOLLOW_fuzzy_in_synpred4_FTS630);
        fuzzy();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred4_FTS

    // $ANTLR start synpred5_FTS
    public final void synpred5_FTS_fragment() throws RecognitionException {   
        // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:267:18: ( fuzzy )
        // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:267:19: fuzzy
        {
        pushFollow(FOLLOW_fuzzy_in_synpred5_FTS659);
        fuzzy();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred5_FTS

    // $ANTLR start synpred6_FTS
    public final void synpred6_FTS_fragment() throws RecognitionException {   
        // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:269:22: ( slop )
        // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:269:23: slop
        {
        pushFollow(FOLLOW_slop_in_synpred6_FTS695);
        slop();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred6_FTS

    // $ANTLR start synpred7_FTS
    public final void synpred7_FTS_fragment() throws RecognitionException {   
        // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:271:23: ( fuzzy )
        // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:271:24: fuzzy
        {
        pushFollow(FOLLOW_fuzzy_in_synpred7_FTS738);
        fuzzy();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred7_FTS

    // $ANTLR start synpred8_FTS
    public final void synpred8_FTS_fragment() throws RecognitionException {   
        // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:347:37: ( or )
        // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:347:38: or
        {
        pushFollow(FOLLOW_or_in_synpred8_FTS1225);
        or();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred8_FTS

    // $ANTLR start synpred9_FTS
    public final void synpred9_FTS_fragment() throws RecognitionException {   
        // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:352:27: ( and )
        // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:352:28: and
        {
        pushFollow(FOLLOW_and_in_synpred9_FTS1261);
        and();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred9_FTS

    // $ANTLR start synpred10_FTS
    public final void synpred10_FTS_fragment() throws RecognitionException {   
        // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:358:4: ( not )
        // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:358:5: not
        {
        pushFollow(FOLLOW_not_in_synpred10_FTS1298);
        not();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred10_FTS

    // $ANTLR start synpred11_FTS
    public final void synpred11_FTS_fragment() throws RecognitionException {   
        // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:372:23: ( fuzzy )
        // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:372:24: fuzzy
        {
        pushFollow(FOLLOW_fuzzy_in_synpred11_FTS1494);
        fuzzy();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred11_FTS

    // $ANTLR start synpred12_FTS
    public final void synpred12_FTS_fragment() throws RecognitionException {   
        // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:374:28: ( fuzzy )
        // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:374:29: fuzzy
        {
        pushFollow(FOLLOW_fuzzy_in_synpred12_FTS1523);
        fuzzy();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred12_FTS

    // $ANTLR start synpred13_FTS
    public final void synpred13_FTS_fragment() throws RecognitionException {   
        // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:376:25: ( slop )
        // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:376:26: slop
        {
        pushFollow(FOLLOW_slop_in_synpred13_FTS1552);
        slop();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred13_FTS

    // $ANTLR start synpred14_FTS
    public final void synpred14_FTS_fragment() throws RecognitionException {   
        // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:378:26: ( fuzzy )
        // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:378:27: fuzzy
        {
        pushFollow(FOLLOW_fuzzy_in_synpred14_FTS1583);
        fuzzy();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred14_FTS

    // Delegated rules

    public final boolean synpred10_FTS() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred10_FTS_fragment(); // can never throw exception
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
    public final boolean synpred14_FTS() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred14_FTS_fragment(); // can never throw exception
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
    public final boolean synpred12_FTS() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred12_FTS_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred7_FTS() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred7_FTS_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred8_FTS() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred8_FTS_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred13_FTS() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred13_FTS_fragment(); // can never throw exception
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
    public final boolean synpred9_FTS() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred9_FTS_fragment(); // can never throw exception
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
    public final boolean synpred11_FTS() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred11_FTS_fragment(); // can never throw exception
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
    protected DFA11 dfa11 = new DFA11(this);
    protected DFA16 dfa16 = new DFA16(this);
    protected DFA25 dfa25 = new DFA25(this);
    protected DFA26 dfa26 = new DFA26(this);
    protected DFA27 dfa27 = new DFA27(this);
    protected DFA33 dfa33 = new DFA33(this);
    protected DFA38 dfa38 = new DFA38(this);
    static final String DFA3_eotS =
        "\24\uffff";
    static final String DFA3_eofS =
        "\24\uffff";
    static final String DFA3_minS =
        "\1\51\21\0\2\uffff";
    static final String DFA3_maxS =
        "\1\110\21\0\2\uffff";
    static final String DFA3_acceptS =
        "\22\uffff\1\1\1\2";
    static final String DFA3_specialS =
        "\1\uffff\1\0\1\1\1\2\1\3\1\4\1\5\1\6\1\7\1\10\1\11\1\12\1\13\1"+
        "\14\1\15\1\16\1\17\1\20\2\uffff}>";
    static final String[] DFA3_transitionS = {
            "\1\17\1\20\1\21\1\15\1\uffff\1\16\1\uffff\1\11\1\6\2\uffff"+
            "\1\7\1\10\2\uffff\1\12\1\13\1\14\2\uffff\1\2\1\4\1\3\3\6\2\12"+
            "\1\1\1\6\1\uffff\1\5",
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
            return "218:1: ftsImplicitConjunctionOrDisjunction : ({...}? ftsExplicitDisjunction ( ftsExplicitDisjunction )* -> ^( CONJUNCTION ( ftsExplicitDisjunction )+ ) | ftsExplicitDisjunction ( ftsExplicitDisjunction )* -> ^( DISJUNCTION ( ftsExplicitDisjunction )+ ) );";
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
                        if ( ((defaultConjunction())) ) {s = 18;}

                        else if ( (true) ) {s = 19;}

                         
                        input.seek(index3_1);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA3_2 = input.LA(1);

                         
                        int index3_2 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((defaultConjunction())) ) {s = 18;}

                        else if ( (true) ) {s = 19;}

                         
                        input.seek(index3_2);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA3_3 = input.LA(1);

                         
                        int index3_3 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((defaultConjunction())) ) {s = 18;}

                        else if ( (true) ) {s = 19;}

                         
                        input.seek(index3_3);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA3_4 = input.LA(1);

                         
                        int index3_4 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((defaultConjunction())) ) {s = 18;}

                        else if ( (true) ) {s = 19;}

                         
                        input.seek(index3_4);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA3_5 = input.LA(1);

                         
                        int index3_5 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((defaultConjunction())) ) {s = 18;}

                        else if ( (true) ) {s = 19;}

                         
                        input.seek(index3_5);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA3_6 = input.LA(1);

                         
                        int index3_6 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((defaultConjunction())) ) {s = 18;}

                        else if ( (true) ) {s = 19;}

                         
                        input.seek(index3_6);
                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA3_7 = input.LA(1);

                         
                        int index3_7 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((defaultConjunction())) ) {s = 18;}

                        else if ( (true) ) {s = 19;}

                         
                        input.seek(index3_7);
                        if ( s>=0 ) return s;
                        break;
                    case 7 : 
                        int LA3_8 = input.LA(1);

                         
                        int index3_8 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((defaultConjunction())) ) {s = 18;}

                        else if ( (true) ) {s = 19;}

                         
                        input.seek(index3_8);
                        if ( s>=0 ) return s;
                        break;
                    case 8 : 
                        int LA3_9 = input.LA(1);

                         
                        int index3_9 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((defaultConjunction())) ) {s = 18;}

                        else if ( (true) ) {s = 19;}

                         
                        input.seek(index3_9);
                        if ( s>=0 ) return s;
                        break;
                    case 9 : 
                        int LA3_10 = input.LA(1);

                         
                        int index3_10 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((defaultConjunction())) ) {s = 18;}

                        else if ( (true) ) {s = 19;}

                         
                        input.seek(index3_10);
                        if ( s>=0 ) return s;
                        break;
                    case 10 : 
                        int LA3_11 = input.LA(1);

                         
                        int index3_11 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((defaultConjunction())) ) {s = 18;}

                        else if ( (true) ) {s = 19;}

                         
                        input.seek(index3_11);
                        if ( s>=0 ) return s;
                        break;
                    case 11 : 
                        int LA3_12 = input.LA(1);

                         
                        int index3_12 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((defaultConjunction())) ) {s = 18;}

                        else if ( (true) ) {s = 19;}

                         
                        input.seek(index3_12);
                        if ( s>=0 ) return s;
                        break;
                    case 12 : 
                        int LA3_13 = input.LA(1);

                         
                        int index3_13 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((defaultConjunction())) ) {s = 18;}

                        else if ( (true) ) {s = 19;}

                         
                        input.seek(index3_13);
                        if ( s>=0 ) return s;
                        break;
                    case 13 : 
                        int LA3_14 = input.LA(1);

                         
                        int index3_14 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((defaultConjunction())) ) {s = 18;}

                        else if ( (true) ) {s = 19;}

                         
                        input.seek(index3_14);
                        if ( s>=0 ) return s;
                        break;
                    case 14 : 
                        int LA3_15 = input.LA(1);

                         
                        int index3_15 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((defaultConjunction())) ) {s = 18;}

                        else if ( (true) ) {s = 19;}

                         
                        input.seek(index3_15);
                        if ( s>=0 ) return s;
                        break;
                    case 15 : 
                        int LA3_16 = input.LA(1);

                         
                        int index3_16 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((defaultConjunction())) ) {s = 18;}

                        else if ( (true) ) {s = 19;}

                         
                        input.seek(index3_16);
                        if ( s>=0 ) return s;
                        break;
                    case 16 : 
                        int LA3_17 = input.LA(1);

                         
                        int index3_17 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((defaultConjunction())) ) {s = 18;}

                        else if ( (true) ) {s = 19;}

                         
                        input.seek(index3_17);
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
        "\26\uffff";
    static final String DFA4_eofS =
        "\1\1\25\uffff";
    static final String DFA4_minS =
        "\1\51\11\uffff\1\0\5\uffff\1\0\5\uffff";
    static final String DFA4_maxS =
        "\1\110\11\uffff\1\0\5\uffff\1\0\5\uffff";
    static final String DFA4_acceptS =
        "\1\uffff\1\2\23\uffff\1\1";
    static final String DFA4_specialS =
        "\12\uffff\1\0\5\uffff\1\1\5\uffff}>";
    static final String[] DFA4_transitionS = {
            "\1\1\1\20\4\1\1\uffff\2\1\2\uffff\2\1\2\uffff\3\1\2\uffff\6"+
            "\1\1\12\3\1\1\uffff\1\1",
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
            return "()* loopback of 230:26: ( ( or )=> or ftsExplictConjunction )*";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA4_10 = input.LA(1);

                         
                        int index4_10 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_FTS()) ) {s = 21;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index4_10);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA4_16 = input.LA(1);

                         
                        int index4_16 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_FTS()) ) {s = 21;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index4_16);
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
        "\27\uffff";
    static final String DFA5_eofS =
        "\1\1\26\uffff";
    static final String DFA5_minS =
        "\1\51\13\uffff\1\0\12\uffff";
    static final String DFA5_maxS =
        "\1\110\13\uffff\1\0\12\uffff";
    static final String DFA5_acceptS =
        "\1\uffff\1\2\24\uffff\1\1";
    static final String DFA5_specialS =
        "\1\0\13\uffff\1\1\12\uffff}>";
    static final String[] DFA5_transitionS = {
            "\6\1\1\uffff\2\1\2\uffff\2\1\2\uffff\3\1\2\uffff\7\1\1\14\2"+
            "\1\1\26\1\1",
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
            return "()* loopback of 238:16: ( ( and )=> and ftsPrefixed )*";
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
                        if ( (LA5_0==EOF||(LA5_0>=PLUS && LA5_0<=PERCENT)||(LA5_0>=TILDA && LA5_0<=DECIMAL_INTEGER_LITERAL)||(LA5_0>=EQUALS && LA5_0<=FTSPHRASE)||(LA5_0>=TO && LA5_0<=LT)||(LA5_0>=AT && LA5_0<=OR)||(LA5_0>=NOT && LA5_0<=FLOATING_POINT_LITERAL)||LA5_0==EXCLAMATION) ) {s = 1;}

                        else if ( (LA5_0==AND) ) {s = 12;}

                        else if ( (LA5_0==AMP) && (synpred2_FTS())) {s = 22;}

                         
                        input.seek(index5_0);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA5_12 = input.LA(1);

                         
                        int index5_12 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_FTS()) ) {s = 22;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index5_12);
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
    static final String DFA11_eotS =
        "\22\uffff";
    static final String DFA11_eofS =
        "\22\uffff";
    static final String DFA11_minS =
        "\1\51\1\0\20\uffff";
    static final String DFA11_maxS =
        "\1\110\1\0\20\uffff";
    static final String DFA11_acceptS =
        "\2\uffff\1\2\2\uffff\1\1\11\uffff\1\3\1\4\1\5";
    static final String DFA11_specialS =
        "\1\0\1\1\20\uffff}>";
    static final String[] DFA11_transitionS = {
            "\1\17\1\20\1\21\1\2\1\uffff\1\2\1\uffff\2\2\2\uffff\2\2\2\uffff"+
            "\3\2\2\uffff\10\2\1\1\1\2\1\uffff\1\5",
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
            "",
            "",
            "",
            "",
            ""
    };

    static final short[] DFA11_eot = DFA.unpackEncodedString(DFA11_eotS);
    static final short[] DFA11_eof = DFA.unpackEncodedString(DFA11_eofS);
    static final char[] DFA11_min = DFA.unpackEncodedStringToUnsignedChars(DFA11_minS);
    static final char[] DFA11_max = DFA.unpackEncodedStringToUnsignedChars(DFA11_maxS);
    static final short[] DFA11_accept = DFA.unpackEncodedString(DFA11_acceptS);
    static final short[] DFA11_special = DFA.unpackEncodedString(DFA11_specialS);
    static final short[][] DFA11_transition;

    static {
        int numStates = DFA11_transitionS.length;
        DFA11_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA11_transition[i] = DFA.unpackEncodedString(DFA11_transitionS[i]);
        }
    }

    class DFA11 extends DFA {

        public DFA11(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 11;
            this.eot = DFA11_eot;
            this.eof = DFA11_eof;
            this.min = DFA11_min;
            this.max = DFA11_max;
            this.accept = DFA11_accept;
            this.special = DFA11_special;
            this.transition = DFA11_transition;
        }
        public String getDescription() {
            return "248:1: ftsPrefixed : ( ( not )=> not ftsTest ( boost )? -> ^( NEGATION ftsTest ( boost )? ) | ftsTest ( boost )? -> ^( DEFAULT ftsTest ( boost )? ) | PLUS ftsTest ( boost )? -> ^( MANDATORY ftsTest ( boost )? ) | BAR ftsTest ( boost )? -> ^( OPTIONAL ftsTest ( boost )? ) | MINUS ftsTest ( boost )? -> ^( EXCLUDE ftsTest ( boost )? ) );";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA11_0 = input.LA(1);

                         
                        int index11_0 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA11_0==NOT) ) {s = 1;}

                        else if ( (LA11_0==LPAREN||LA11_0==PERCENT||(LA11_0>=TILDA && LA11_0<=DECIMAL_INTEGER_LITERAL)||(LA11_0>=EQUALS && LA11_0<=FTSPHRASE)||(LA11_0>=TO && LA11_0<=LT)||(LA11_0>=AT && LA11_0<=AND)||LA11_0==FLOATING_POINT_LITERAL) ) {s = 2;}

                        else if ( (LA11_0==EXCLAMATION) && (synpred3_FTS())) {s = 5;}

                        else if ( (LA11_0==PLUS) ) {s = 15;}

                        else if ( (LA11_0==BAR) ) {s = 16;}

                        else if ( (LA11_0==MINUS) ) {s = 17;}

                         
                        input.seek(index11_0);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA11_1 = input.LA(1);

                         
                        int index11_1 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred3_FTS()) ) {s = 5;}

                        else if ( (true) ) {s = 2;}

                         
                        input.seek(index11_1);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 11, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA16_eotS =
        "\26\uffff";
    static final String DFA16_eofS =
        "\2\uffff\1\15\1\uffff\1\15\1\uffff\1\21\1\uffff\1\15\12\uffff\2"+
        "\15\1\uffff";
    static final String DFA16_minS =
        "\1\54\1\76\1\51\1\77\1\51\1\uffff\1\51\1\uffff\1\51\3\uffff\1\63"+
        "\1\uffff\1\54\1\uffff\1\63\2\uffff\2\51\1\54";
    static final String DFA16_maxS =
        "\1\106\1\77\1\110\1\77\1\110\1\uffff\1\110\1\uffff\1\110\3\uffff"+
        "\1\63\1\uffff\1\106\1\uffff\1\63\2\uffff\2\110\1\106";
    static final String DFA16_acceptS =
        "\5\uffff\1\2\1\uffff\1\4\1\uffff\1\6\1\10\1\11\1\uffff\1\1\1\uffff"+
        "\1\5\1\uffff\1\3\1\7\3\uffff";
    static final String DFA16_specialS =
        "\26\uffff}>";
    static final String[] DFA16_transitionS = {
            "\1\12\1\uffff\1\13\1\uffff\1\7\1\4\2\uffff\1\5\1\6\2\uffff"+
            "\1\10\2\11\2\uffff\1\1\1\3\1\2\3\4\3\10\1\4",
            "\1\3\1\14",
            "\6\15\1\uffff\3\15\1\16\2\15\1\17\1\11\3\15\2\uffff\14\15",
            "\1\20",
            "\6\15\1\uffff\3\15\1\uffff\2\15\1\17\1\11\3\15\2\uffff\14"+
            "\15",
            "",
            "\6\21\1\uffff\3\21\1\uffff\2\21\1\uffff\1\11\3\21\2\uffff"+
            "\14\21",
            "",
            "\6\15\1\uffff\3\15\1\uffff\2\15\1\17\1\uffff\3\15\2\uffff"+
            "\14\15",
            "",
            "",
            "",
            "\1\16",
            "",
            "\1\22\4\uffff\1\24\3\uffff\1\6\2\uffff\1\15\2\11\4\uffff\1"+
            "\23\3\24\3\15\1\24",
            "",
            "\1\25",
            "",
            "",
            "\6\15\1\uffff\3\15\1\25\2\15\1\uffff\1\11\3\15\2\uffff\14"+
            "\15",
            "\6\15\1\uffff\3\15\1\uffff\2\15\1\uffff\1\11\3\15\2\uffff"+
            "\14\15",
            "\1\22\4\uffff\1\24\3\uffff\1\6\2\uffff\1\15\2\11\4\uffff\4"+
            "\24\3\15\1\24"
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
            return "264:1: ftsTest : ( ftsTerm ( ( fuzzy )=> fuzzy )? -> ^( TERM ftsTerm ( fuzzy )? ) | ftsExactTerm ( ( fuzzy )=> fuzzy )? -> ^( EXACT_TERM ftsExactTerm ( fuzzy )? ) | ftsPhrase ( ( slop )=> slop )? -> ^( PHRASE ftsPhrase ( slop )? ) | ftsSynonym ( ( fuzzy )=> fuzzy )? -> ^( SYNONYM ftsSynonym ( fuzzy )? ) | ftsFieldGroupProximity -> ^( PROXIMITY ftsFieldGroupProximity ) | ftsRange -> ^( RANGE ftsRange ) | ftsFieldGroup -> ftsFieldGroup | LPAREN ftsImplicitConjunctionOrDisjunction RPAREN -> ftsImplicitConjunctionOrDisjunction | template -> template );";
        }
    }
    static final String DFA25_eotS =
        "\20\uffff";
    static final String DFA25_eofS =
        "\20\uffff";
    static final String DFA25_minS =
        "\1\51\15\0\2\uffff";
    static final String DFA25_maxS =
        "\1\110\15\0\2\uffff";
    static final String DFA25_acceptS =
        "\16\uffff\1\1\1\2";
    static final String DFA25_specialS =
        "\1\uffff\1\0\1\1\1\2\1\3\1\4\1\5\1\6\1\7\1\10\1\11\1\12\1\13\1"+
        "\14\2\uffff}>";
    static final String[] DFA25_transitionS = {
            "\1\13\1\14\1\15\1\12\3\uffff\1\6\1\3\2\uffff\1\4\1\5\2\uffff"+
            "\1\7\1\10\1\11\4\uffff\4\3\2\7\1\1\1\3\1\uffff\1\2",
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
            return "339:1: ftsFieldGroupImplicitConjunctionOrDisjunction : ({...}? ftsFieldGroupExplicitDisjunction ( ftsFieldGroupExplicitDisjunction )* -> ^( FIELD_CONJUNCTION ( ftsFieldGroupExplicitDisjunction )+ ) | ftsFieldGroupExplicitDisjunction ( ftsFieldGroupExplicitDisjunction )* -> ^( FIELD_DISJUNCTION ( ftsFieldGroupExplicitDisjunction )+ ) );";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA25_1 = input.LA(1);

                         
                        int index25_1 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((defaultFieldConjunction())) ) {s = 14;}

                        else if ( (true) ) {s = 15;}

                         
                        input.seek(index25_1);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA25_2 = input.LA(1);

                         
                        int index25_2 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((defaultFieldConjunction())) ) {s = 14;}

                        else if ( (true) ) {s = 15;}

                         
                        input.seek(index25_2);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA25_3 = input.LA(1);

                         
                        int index25_3 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((defaultFieldConjunction())) ) {s = 14;}

                        else if ( (true) ) {s = 15;}

                         
                        input.seek(index25_3);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA25_4 = input.LA(1);

                         
                        int index25_4 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((defaultFieldConjunction())) ) {s = 14;}

                        else if ( (true) ) {s = 15;}

                         
                        input.seek(index25_4);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA25_5 = input.LA(1);

                         
                        int index25_5 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((defaultFieldConjunction())) ) {s = 14;}

                        else if ( (true) ) {s = 15;}

                         
                        input.seek(index25_5);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA25_6 = input.LA(1);

                         
                        int index25_6 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((defaultFieldConjunction())) ) {s = 14;}

                        else if ( (true) ) {s = 15;}

                         
                        input.seek(index25_6);
                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA25_7 = input.LA(1);

                         
                        int index25_7 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((defaultFieldConjunction())) ) {s = 14;}

                        else if ( (true) ) {s = 15;}

                         
                        input.seek(index25_7);
                        if ( s>=0 ) return s;
                        break;
                    case 7 : 
                        int LA25_8 = input.LA(1);

                         
                        int index25_8 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((defaultFieldConjunction())) ) {s = 14;}

                        else if ( (true) ) {s = 15;}

                         
                        input.seek(index25_8);
                        if ( s>=0 ) return s;
                        break;
                    case 8 : 
                        int LA25_9 = input.LA(1);

                         
                        int index25_9 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((defaultFieldConjunction())) ) {s = 14;}

                        else if ( (true) ) {s = 15;}

                         
                        input.seek(index25_9);
                        if ( s>=0 ) return s;
                        break;
                    case 9 : 
                        int LA25_10 = input.LA(1);

                         
                        int index25_10 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((defaultFieldConjunction())) ) {s = 14;}

                        else if ( (true) ) {s = 15;}

                         
                        input.seek(index25_10);
                        if ( s>=0 ) return s;
                        break;
                    case 10 : 
                        int LA25_11 = input.LA(1);

                         
                        int index25_11 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((defaultFieldConjunction())) ) {s = 14;}

                        else if ( (true) ) {s = 15;}

                         
                        input.seek(index25_11);
                        if ( s>=0 ) return s;
                        break;
                    case 11 : 
                        int LA25_12 = input.LA(1);

                         
                        int index25_12 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((defaultFieldConjunction())) ) {s = 14;}

                        else if ( (true) ) {s = 15;}

                         
                        input.seek(index25_12);
                        if ( s>=0 ) return s;
                        break;
                    case 12 : 
                        int LA25_13 = input.LA(1);

                         
                        int index25_13 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((defaultFieldConjunction())) ) {s = 14;}

                        else if ( (true) ) {s = 15;}

                         
                        input.seek(index25_13);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 25, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA26_eotS =
        "\21\uffff";
    static final String DFA26_eofS =
        "\21\uffff";
    static final String DFA26_minS =
        "\1\51\6\uffff\1\0\4\uffff\1\0\4\uffff";
    static final String DFA26_maxS =
        "\1\110\6\uffff\1\0\4\uffff\1\0\4\uffff";
    static final String DFA26_acceptS =
        "\1\uffff\1\2\16\uffff\1\1";
    static final String DFA26_specialS =
        "\7\uffff\1\0\4\uffff\1\1\4\uffff}>";
    static final String[] DFA26_transitionS = {
            "\1\1\1\14\3\1\2\uffff\2\1\2\uffff\2\1\2\uffff\3\1\4\uffff\4"+
            "\1\1\7\3\1\1\uffff\1\1",
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
            "\1\uffff",
            "",
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
            return "()* loopback of 347:36: ( ( or )=> or ftsFieldGroupExplictConjunction )*";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA26_7 = input.LA(1);

                         
                        int index26_7 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_FTS()) ) {s = 16;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index26_7);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA26_12 = input.LA(1);

                         
                        int index26_12 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_FTS()) ) {s = 16;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index26_12);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 26, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA27_eotS =
        "\22\uffff";
    static final String DFA27_eofS =
        "\22\uffff";
    static final String DFA27_minS =
        "\1\51\10\uffff\1\0\10\uffff";
    static final String DFA27_maxS =
        "\1\110\10\uffff\1\0\10\uffff";
    static final String DFA27_acceptS =
        "\1\uffff\1\2\17\uffff\1\1";
    static final String DFA27_specialS =
        "\1\0\10\uffff\1\1\10\uffff}>";
    static final String[] DFA27_transitionS = {
            "\5\1\2\uffff\2\1\2\uffff\2\1\2\uffff\3\1\4\uffff\5\1\1\11\2"+
            "\1\1\21\1\1",
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

    static final short[] DFA27_eot = DFA.unpackEncodedString(DFA27_eotS);
    static final short[] DFA27_eof = DFA.unpackEncodedString(DFA27_eofS);
    static final char[] DFA27_min = DFA.unpackEncodedStringToUnsignedChars(DFA27_minS);
    static final char[] DFA27_max = DFA.unpackEncodedStringToUnsignedChars(DFA27_maxS);
    static final short[] DFA27_accept = DFA.unpackEncodedString(DFA27_acceptS);
    static final short[] DFA27_special = DFA.unpackEncodedString(DFA27_specialS);
    static final short[][] DFA27_transition;

    static {
        int numStates = DFA27_transitionS.length;
        DFA27_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA27_transition[i] = DFA.unpackEncodedString(DFA27_transitionS[i]);
        }
    }

    class DFA27 extends DFA {

        public DFA27(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 27;
            this.eot = DFA27_eot;
            this.eof = DFA27_eof;
            this.min = DFA27_min;
            this.max = DFA27_max;
            this.accept = DFA27_accept;
            this.special = DFA27_special;
            this.transition = DFA27_transition;
        }
        public String getDescription() {
            return "()* loopback of 352:26: ( ( and )=> and ftsFieldGroupPrefixed )*";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA27_0 = input.LA(1);

                         
                        int index27_0 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((LA27_0>=PLUS && LA27_0<=RPAREN)||(LA27_0>=TILDA && LA27_0<=DECIMAL_INTEGER_LITERAL)||(LA27_0>=EQUALS && LA27_0<=FTSPHRASE)||(LA27_0>=TO && LA27_0<=LT)||(LA27_0>=ID && LA27_0<=OR)||(LA27_0>=NOT && LA27_0<=FLOATING_POINT_LITERAL)||LA27_0==EXCLAMATION) ) {s = 1;}

                        else if ( (LA27_0==AND) ) {s = 9;}

                        else if ( (LA27_0==AMP) && (synpred9_FTS())) {s = 17;}

                         
                        input.seek(index27_0);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA27_9 = input.LA(1);

                         
                        int index27_9 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred9_FTS()) ) {s = 17;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index27_9);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 27, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA33_eotS =
        "\16\uffff";
    static final String DFA33_eofS =
        "\16\uffff";
    static final String DFA33_minS =
        "\1\51\1\0\14\uffff";
    static final String DFA33_maxS =
        "\1\110\1\0\14\uffff";
    static final String DFA33_acceptS =
        "\2\uffff\1\1\1\2\7\uffff\1\3\1\4\1\5";
    static final String DFA33_specialS =
        "\1\0\1\1\14\uffff}>";
    static final String[] DFA33_transitionS = {
            "\1\13\1\14\1\15\1\3\3\uffff\2\3\2\uffff\2\3\2\uffff\3\3\4\uffff"+
            "\6\3\1\1\1\3\1\uffff\1\2",
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

    static final short[] DFA33_eot = DFA.unpackEncodedString(DFA33_eotS);
    static final short[] DFA33_eof = DFA.unpackEncodedString(DFA33_eofS);
    static final char[] DFA33_min = DFA.unpackEncodedStringToUnsignedChars(DFA33_minS);
    static final char[] DFA33_max = DFA.unpackEncodedStringToUnsignedChars(DFA33_maxS);
    static final short[] DFA33_accept = DFA.unpackEncodedString(DFA33_acceptS);
    static final short[] DFA33_special = DFA.unpackEncodedString(DFA33_specialS);
    static final short[][] DFA33_transition;

    static {
        int numStates = DFA33_transitionS.length;
        DFA33_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA33_transition[i] = DFA.unpackEncodedString(DFA33_transitionS[i]);
        }
    }

    class DFA33 extends DFA {

        public DFA33(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 33;
            this.eot = DFA33_eot;
            this.eof = DFA33_eof;
            this.min = DFA33_min;
            this.max = DFA33_max;
            this.accept = DFA33_accept;
            this.special = DFA33_special;
            this.transition = DFA33_transition;
        }
        public String getDescription() {
            return "357:1: ftsFieldGroupPrefixed : ( ( not )=> not ftsFieldGroupTest ( boost )? -> ^( FIELD_NEGATION ftsFieldGroupTest ( boost )? ) | ftsFieldGroupTest ( boost )? -> ^( FIELD_DEFAULT ftsFieldGroupTest ( boost )? ) | PLUS ftsFieldGroupTest ( boost )? -> ^( FIELD_MANDATORY ftsFieldGroupTest ( boost )? ) | BAR ftsFieldGroupTest ( boost )? -> ^( FIELD_OPTIONAL ftsFieldGroupTest ( boost )? ) | MINUS ftsFieldGroupTest ( boost )? -> ^( FIELD_EXCLUDE ftsFieldGroupTest ( boost )? ) );";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA33_0 = input.LA(1);

                         
                        int index33_0 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA33_0==NOT) ) {s = 1;}

                        else if ( (LA33_0==EXCLAMATION) && (synpred10_FTS())) {s = 2;}

                        else if ( (LA33_0==LPAREN||(LA33_0>=TILDA && LA33_0<=DECIMAL_INTEGER_LITERAL)||(LA33_0>=EQUALS && LA33_0<=FTSPHRASE)||(LA33_0>=TO && LA33_0<=LT)||(LA33_0>=ID && LA33_0<=AND)||LA33_0==FLOATING_POINT_LITERAL) ) {s = 3;}

                        else if ( (LA33_0==PLUS) ) {s = 11;}

                        else if ( (LA33_0==BAR) ) {s = 12;}

                        else if ( (LA33_0==MINUS) ) {s = 13;}

                         
                        input.seek(index33_0);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA33_1 = input.LA(1);

                         
                        int index33_1 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred10_FTS()) ) {s = 2;}

                        else if ( (true) ) {s = 3;}

                         
                        input.seek(index33_1);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 33, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA38_eotS =
        "\13\uffff";
    static final String DFA38_eofS =
        "\13\uffff";
    static final String DFA38_minS =
        "\1\54\1\51\1\uffff\1\51\1\uffff\1\51\5\uffff";
    static final String DFA38_maxS =
        "\1\106\1\110\1\uffff\1\110\1\uffff\1\110\5\uffff";
    static final String DFA38_acceptS =
        "\2\uffff\1\2\1\uffff\1\4\1\uffff\1\6\1\7\1\5\1\1\1\3";
    static final String DFA38_specialS =
        "\13\uffff}>";
    static final String[] DFA38_transitionS = {
            "\1\7\3\uffff\1\4\1\1\2\uffff\1\2\1\3\2\uffff\1\5\2\6\4\uffff"+
            "\4\1\3\5\1\1",
            "\5\11\2\uffff\3\11\1\uffff\2\11\1\10\1\6\3\11\4\uffff\12\11",
            "",
            "\5\12\2\uffff\3\12\1\uffff\2\12\1\uffff\1\6\3\12\4\uffff\12"+
            "\12",
            "",
            "\5\11\2\uffff\3\11\1\uffff\2\11\1\10\1\uffff\3\11\4\uffff"+
            "\12\11",
            "",
            "",
            "",
            "",
            ""
    };

    static final short[] DFA38_eot = DFA.unpackEncodedString(DFA38_eotS);
    static final short[] DFA38_eof = DFA.unpackEncodedString(DFA38_eofS);
    static final char[] DFA38_min = DFA.unpackEncodedStringToUnsignedChars(DFA38_minS);
    static final char[] DFA38_max = DFA.unpackEncodedStringToUnsignedChars(DFA38_maxS);
    static final short[] DFA38_accept = DFA.unpackEncodedString(DFA38_acceptS);
    static final short[] DFA38_special = DFA.unpackEncodedString(DFA38_specialS);
    static final short[][] DFA38_transition;

    static {
        int numStates = DFA38_transitionS.length;
        DFA38_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA38_transition[i] = DFA.unpackEncodedString(DFA38_transitionS[i]);
        }
    }

    class DFA38 extends DFA {

        public DFA38(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 38;
            this.eot = DFA38_eot;
            this.eof = DFA38_eof;
            this.min = DFA38_min;
            this.max = DFA38_max;
            this.accept = DFA38_accept;
            this.special = DFA38_special;
            this.transition = DFA38_transition;
        }
        public String getDescription() {
            return "371:1: ftsFieldGroupTest : ( ftsFieldGroupTerm ( ( fuzzy )=> fuzzy )? -> ^( FG_TERM ftsFieldGroupTerm ( fuzzy )? ) | ftsFieldGroupExactTerm ( ( fuzzy )=> fuzzy )? -> ^( FG_EXACT_TERM ftsFieldGroupExactTerm ( fuzzy )? ) | ftsFieldGroupPhrase ( ( slop )=> slop )? -> ^( FG_PHRASE ftsFieldGroupPhrase ( slop )? ) | ftsFieldGroupSynonym ( ( fuzzy )=> fuzzy )? -> ^( FG_SYNONYM ftsFieldGroupSynonym ( fuzzy )? ) | ftsFieldGroupProximity -> ^( FG_PROXIMITY ftsFieldGroupProximity ) | ftsFieldGroupRange -> ^( FG_RANGE ftsFieldGroupRange ) | LPAREN ftsFieldGroupImplicitConjunctionOrDisjunction RPAREN -> ftsFieldGroupImplicitConjunctionOrDisjunction );";
        }
    }
 

    public static final BitSet FOLLOW_ftsImplicitConjunctionOrDisjunction_in_ftsQuery258 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_ftsQuery260 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ftsExplicitDisjunction_in_ftsImplicitConjunctionOrDisjunction284 = new BitSet(new long[]{0xE7335E0000000002L,0x000000000000017FL});
    public static final BitSet FOLLOW_ftsExplicitDisjunction_in_ftsImplicitConjunctionOrDisjunction287 = new BitSet(new long[]{0xE7335E0000000002L,0x000000000000017FL});
    public static final BitSet FOLLOW_ftsExplicitDisjunction_in_ftsImplicitConjunctionOrDisjunction306 = new BitSet(new long[]{0xE7335E0000000002L,0x000000000000017FL});
    public static final BitSet FOLLOW_ftsExplicitDisjunction_in_ftsImplicitConjunctionOrDisjunction309 = new BitSet(new long[]{0xE7335E0000000002L,0x000000000000017FL});
    public static final BitSet FOLLOW_ftsExplictConjunction_in_ftsExplicitDisjunction336 = new BitSet(new long[]{0x0000040000000002L,0x0000000000000008L});
    public static final BitSet FOLLOW_or_in_ftsExplicitDisjunction345 = new BitSet(new long[]{0xE7335E0000000000L,0x000000000000017FL});
    public static final BitSet FOLLOW_ftsExplictConjunction_in_ftsExplicitDisjunction347 = new BitSet(new long[]{0x0000040000000002L,0x0000000000000008L});
    public static final BitSet FOLLOW_ftsPrefixed_in_ftsExplictConjunction374 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000090L});
    public static final BitSet FOLLOW_and_in_ftsExplictConjunction383 = new BitSet(new long[]{0xE7335E0000000000L,0x000000000000017FL});
    public static final BitSet FOLLOW_ftsPrefixed_in_ftsExplictConjunction385 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000090L});
    public static final BitSet FOLLOW_not_in_ftsPrefixed426 = new BitSet(new long[]{0xE733500000000000L,0x000000000000007FL});
    public static final BitSet FOLLOW_ftsTest_in_ftsPrefixed428 = new BitSet(new long[]{0x0004000000000002L});
    public static final BitSet FOLLOW_boost_in_ftsPrefixed431 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ftsTest_in_ftsPrefixed455 = new BitSet(new long[]{0x0004000000000002L});
    public static final BitSet FOLLOW_boost_in_ftsPrefixed458 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_PLUS_in_ftsPrefixed484 = new BitSet(new long[]{0xE733500000000000L,0x000000000000007FL});
    public static final BitSet FOLLOW_ftsTest_in_ftsPrefixed486 = new BitSet(new long[]{0x0004000000000002L});
    public static final BitSet FOLLOW_boost_in_ftsPrefixed488 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_BAR_in_ftsPrefixed528 = new BitSet(new long[]{0xE733500000000000L,0x000000000000007FL});
    public static final BitSet FOLLOW_ftsTest_in_ftsPrefixed530 = new BitSet(new long[]{0x0004000000000002L});
    public static final BitSet FOLLOW_boost_in_ftsPrefixed532 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_MINUS_in_ftsPrefixed572 = new BitSet(new long[]{0xE733500000000000L,0x000000000000007FL});
    public static final BitSet FOLLOW_ftsTest_in_ftsPrefixed574 = new BitSet(new long[]{0x0004000000000002L});
    public static final BitSet FOLLOW_boost_in_ftsPrefixed576 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ftsTerm_in_ftsTest626 = new BitSet(new long[]{0x0001000000000002L});
    public static final BitSet FOLLOW_fuzzy_in_ftsTest635 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ftsExactTerm_in_ftsTest655 = new BitSet(new long[]{0x0001000000000002L});
    public static final BitSet FOLLOW_fuzzy_in_ftsTest664 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ftsPhrase_in_ftsTest691 = new BitSet(new long[]{0x0001000000000002L});
    public static final BitSet FOLLOW_slop_in_ftsTest700 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ftsSynonym_in_ftsTest734 = new BitSet(new long[]{0x0001000000000002L});
    public static final BitSet FOLLOW_fuzzy_in_ftsTest743 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ftsFieldGroupProximity_in_ftsTest775 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ftsRange_in_ftsTest803 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ftsFieldGroup_in_ftsTest830 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_ftsTest847 = new BitSet(new long[]{0xE7335E0000000000L,0x000000000000017FL});
    public static final BitSet FOLLOW_ftsImplicitConjunctionOrDisjunction_in_ftsTest849 = new BitSet(new long[]{0x0000200000000000L});
    public static final BitSet FOLLOW_RPAREN_in_ftsTest851 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_template_in_ftsTest862 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_PERCENT_in_template881 = new BitSet(new long[]{0xE000000000000000L});
    public static final BitSet FOLLOW_tempReference_in_template883 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_PERCENT_in_template898 = new BitSet(new long[]{0x0000100000000000L});
    public static final BitSet FOLLOW_LPAREN_in_template900 = new BitSet(new long[]{0xE000000000000000L});
    public static final BitSet FOLLOW_tempReference_in_template903 = new BitSet(new long[]{0xE000A00000000000L});
    public static final BitSet FOLLOW_COMMA_in_template905 = new BitSet(new long[]{0xE000200000000000L});
    public static final BitSet FOLLOW_RPAREN_in_template910 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TILDA_in_fuzzy937 = new BitSet(new long[]{0x0002000000000000L,0x0000000000000040L});
    public static final BitSet FOLLOW_number_in_fuzzy939 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TILDA_in_slop962 = new BitSet(new long[]{0x0002000000000000L});
    public static final BitSet FOLLOW_DECIMAL_INTEGER_LITERAL_in_slop964 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_CARAT_in_boost986 = new BitSet(new long[]{0x0002000000000000L,0x0000000000000040L});
    public static final BitSet FOLLOW_number_in_boost988 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_fieldReference_in_ftsTerm1010 = new BitSet(new long[]{0x0008000000000000L});
    public static final BitSet FOLLOW_COLON_in_ftsTerm1012 = new BitSet(new long[]{0xE102000000000000L,0x000000000000007FL});
    public static final BitSet FOLLOW_ftsWord_in_ftsTerm1016 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_EQUALS_in_ftsExactTerm1037 = new BitSet(new long[]{0xE102000000000000L,0x000000000000007FL});
    public static final BitSet FOLLOW_ftsTerm_in_ftsExactTerm1039 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_fieldReference_in_ftsPhrase1060 = new BitSet(new long[]{0x0008000000000000L});
    public static final BitSet FOLLOW_COLON_in_ftsPhrase1062 = new BitSet(new long[]{0x0020000000000000L});
    public static final BitSet FOLLOW_FTSPHRASE_in_ftsPhrase1066 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TILDA_in_ftsSynonym1087 = new BitSet(new long[]{0xE102000000000000L,0x000000000000007FL});
    public static final BitSet FOLLOW_ftsTerm_in_ftsSynonym1089 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_fieldReference_in_ftsRange1109 = new BitSet(new long[]{0x0008000000000000L});
    public static final BitSet FOLLOW_COLON_in_ftsRange1111 = new BitSet(new long[]{0xE622000000000000L,0x0000000000000047L});
    public static final BitSet FOLLOW_ftsFieldGroupRange_in_ftsRange1115 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_fieldReference_in_ftsFieldGroup1138 = new BitSet(new long[]{0x0008000000000000L});
    public static final BitSet FOLLOW_COLON_in_ftsFieldGroup1140 = new BitSet(new long[]{0x0000100000000000L});
    public static final BitSet FOLLOW_LPAREN_in_ftsFieldGroup1142 = new BitSet(new long[]{0xE7331E0000000000L,0x000000000000017FL});
    public static final BitSet FOLLOW_ftsFieldGroupImplicitConjunctionOrDisjunction_in_ftsFieldGroup1144 = new BitSet(new long[]{0x0000200000000000L});
    public static final BitSet FOLLOW_RPAREN_in_ftsFieldGroup1146 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ftsFieldGroupExplicitDisjunction_in_ftsFieldGroupImplicitConjunctionOrDisjunction1172 = new BitSet(new long[]{0xE7331E0000000002L,0x000000000000017FL});
    public static final BitSet FOLLOW_ftsFieldGroupExplicitDisjunction_in_ftsFieldGroupImplicitConjunctionOrDisjunction1175 = new BitSet(new long[]{0xE7331E0000000002L,0x000000000000017FL});
    public static final BitSet FOLLOW_ftsFieldGroupExplicitDisjunction_in_ftsFieldGroupImplicitConjunctionOrDisjunction1193 = new BitSet(new long[]{0xE7331E0000000002L,0x000000000000017FL});
    public static final BitSet FOLLOW_ftsFieldGroupExplicitDisjunction_in_ftsFieldGroupImplicitConjunctionOrDisjunction1196 = new BitSet(new long[]{0xE7331E0000000002L,0x000000000000017FL});
    public static final BitSet FOLLOW_ftsFieldGroupExplictConjunction_in_ftsFieldGroupExplicitDisjunction1221 = new BitSet(new long[]{0x0000040000000002L,0x0000000000000008L});
    public static final BitSet FOLLOW_or_in_ftsFieldGroupExplicitDisjunction1230 = new BitSet(new long[]{0xE7331E0000000000L,0x000000000000017FL});
    public static final BitSet FOLLOW_ftsFieldGroupExplictConjunction_in_ftsFieldGroupExplicitDisjunction1232 = new BitSet(new long[]{0x0000040000000002L,0x0000000000000008L});
    public static final BitSet FOLLOW_ftsFieldGroupPrefixed_in_ftsFieldGroupExplictConjunction1257 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000090L});
    public static final BitSet FOLLOW_and_in_ftsFieldGroupExplictConjunction1266 = new BitSet(new long[]{0xE7331E0000000000L,0x000000000000017FL});
    public static final BitSet FOLLOW_ftsFieldGroupPrefixed_in_ftsFieldGroupExplictConjunction1268 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000090L});
    public static final BitSet FOLLOW_not_in_ftsFieldGroupPrefixed1303 = new BitSet(new long[]{0xE733100000000000L,0x000000000000007FL});
    public static final BitSet FOLLOW_ftsFieldGroupTest_in_ftsFieldGroupPrefixed1305 = new BitSet(new long[]{0x0004000000000002L});
    public static final BitSet FOLLOW_boost_in_ftsFieldGroupPrefixed1307 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ftsFieldGroupTest_in_ftsFieldGroupPrefixed1330 = new BitSet(new long[]{0x0004000000000002L});
    public static final BitSet FOLLOW_boost_in_ftsFieldGroupPrefixed1332 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_PLUS_in_ftsFieldGroupPrefixed1354 = new BitSet(new long[]{0xE733100000000000L,0x000000000000007FL});
    public static final BitSet FOLLOW_ftsFieldGroupTest_in_ftsFieldGroupPrefixed1356 = new BitSet(new long[]{0x0004000000000002L});
    public static final BitSet FOLLOW_boost_in_ftsFieldGroupPrefixed1358 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_BAR_in_ftsFieldGroupPrefixed1398 = new BitSet(new long[]{0xE733100000000000L,0x000000000000007FL});
    public static final BitSet FOLLOW_ftsFieldGroupTest_in_ftsFieldGroupPrefixed1400 = new BitSet(new long[]{0x0004000000000002L});
    public static final BitSet FOLLOW_boost_in_ftsFieldGroupPrefixed1402 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_MINUS_in_ftsFieldGroupPrefixed1442 = new BitSet(new long[]{0xE733100000000000L,0x000000000000007FL});
    public static final BitSet FOLLOW_ftsFieldGroupTest_in_ftsFieldGroupPrefixed1444 = new BitSet(new long[]{0x0004000000000002L});
    public static final BitSet FOLLOW_boost_in_ftsFieldGroupPrefixed1446 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ftsFieldGroupTerm_in_ftsFieldGroupTest1490 = new BitSet(new long[]{0x0001000000000002L});
    public static final BitSet FOLLOW_fuzzy_in_ftsFieldGroupTest1499 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ftsFieldGroupExactTerm_in_ftsFieldGroupTest1519 = new BitSet(new long[]{0x0001000000000002L});
    public static final BitSet FOLLOW_fuzzy_in_ftsFieldGroupTest1528 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ftsFieldGroupPhrase_in_ftsFieldGroupTest1548 = new BitSet(new long[]{0x0001000000000002L});
    public static final BitSet FOLLOW_slop_in_ftsFieldGroupTest1557 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ftsFieldGroupSynonym_in_ftsFieldGroupTest1579 = new BitSet(new long[]{0x0001000000000002L});
    public static final BitSet FOLLOW_fuzzy_in_ftsFieldGroupTest1588 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ftsFieldGroupProximity_in_ftsFieldGroupTest1610 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ftsFieldGroupRange_in_ftsFieldGroupTest1630 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_ftsFieldGroupTest1653 = new BitSet(new long[]{0xE7331E0000000000L,0x000000000000017FL});
    public static final BitSet FOLLOW_ftsFieldGroupImplicitConjunctionOrDisjunction_in_ftsFieldGroupTest1655 = new BitSet(new long[]{0x0000200000000000L});
    public static final BitSet FOLLOW_RPAREN_in_ftsFieldGroupTest1657 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ftsWord_in_ftsFieldGroupTerm1677 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_EQUALS_in_ftsFieldGroupExactTerm1689 = new BitSet(new long[]{0xE102000000000000L,0x000000000000007FL});
    public static final BitSet FOLLOW_ftsFieldGroupTerm_in_ftsFieldGroupExactTerm1691 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FTSPHRASE_in_ftsFieldGroupPhrase1711 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TILDA_in_ftsFieldGroupSynonym1723 = new BitSet(new long[]{0xE102000000000000L,0x000000000000007FL});
    public static final BitSet FOLLOW_ftsFieldGroupTerm_in_ftsFieldGroupSynonym1725 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ftsFieldGroupTerm_in_ftsFieldGroupProximity1743 = new BitSet(new long[]{0x0040000000000000L});
    public static final BitSet FOLLOW_proximityGroup_in_ftsFieldGroupProximity1746 = new BitSet(new long[]{0xE102000000000000L,0x000000000000007FL});
    public static final BitSet FOLLOW_ftsFieldGroupTerm_in_ftsFieldGroupProximity1748 = new BitSet(new long[]{0x0040000000000002L});
    public static final BitSet FOLLOW_STAR_in_proximityGroup1778 = new BitSet(new long[]{0x0000100000000002L});
    public static final BitSet FOLLOW_LPAREN_in_proximityGroup1782 = new BitSet(new long[]{0x0002200000000000L});
    public static final BitSet FOLLOW_DECIMAL_INTEGER_LITERAL_in_proximityGroup1784 = new BitSet(new long[]{0x0000200000000000L});
    public static final BitSet FOLLOW_RPAREN_in_proximityGroup1787 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ftsRangeWord_in_ftsFieldGroupRange1823 = new BitSet(new long[]{0x0080000000000000L});
    public static final BitSet FOLLOW_DOTDOT_in_ftsFieldGroupRange1825 = new BitSet(new long[]{0x8022000000000000L,0x0000000000000047L});
    public static final BitSet FOLLOW_ftsRangeWord_in_ftsFieldGroupRange1827 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_range_left_in_ftsFieldGroupRange1844 = new BitSet(new long[]{0x8022000000000000L,0x0000000000000047L});
    public static final BitSet FOLLOW_ftsRangeWord_in_ftsFieldGroupRange1846 = new BitSet(new long[]{0x0100000000000000L});
    public static final BitSet FOLLOW_TO_in_ftsFieldGroupRange1848 = new BitSet(new long[]{0x8022000000000000L,0x0000000000000047L});
    public static final BitSet FOLLOW_ftsRangeWord_in_ftsFieldGroupRange1850 = new BitSet(new long[]{0x1800000000000000L});
    public static final BitSet FOLLOW_range_right_in_ftsFieldGroupRange1852 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LSQUARE_in_range_left1882 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LT_in_range_left1893 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RSQUARE_in_range_right1917 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_GT_in_range_right1928 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_AT_in_fieldReference1950 = new BitSet(new long[]{0xC000000000000000L});
    public static final BitSet FOLLOW_prefix_in_fieldReference1954 = new BitSet(new long[]{0x8000000000000000L});
    public static final BitSet FOLLOW_uri_in_fieldReference1956 = new BitSet(new long[]{0x8000000000000000L});
    public static final BitSet FOLLOW_identifier_in_fieldReference1960 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_AT_in_tempReference1993 = new BitSet(new long[]{0xC000000000000000L});
    public static final BitSet FOLLOW_prefix_in_tempReference1997 = new BitSet(new long[]{0x8000000000000000L});
    public static final BitSet FOLLOW_uri_in_tempReference1999 = new BitSet(new long[]{0x8000000000000000L});
    public static final BitSet FOLLOW_identifier_in_tempReference2003 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_identifier_in_prefix2036 = new BitSet(new long[]{0x0008000000000000L});
    public static final BitSet FOLLOW_COLON_in_prefix2038 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_URI_in_uri2064 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_identifier2090 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_ftsWord0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_number0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_ftsRangeWord0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_OR_in_or2374 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_BAR_in_or2383 = new BitSet(new long[]{0x0000040000000000L});
    public static final BitSet FOLLOW_BAR_in_or2385 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_AND_in_and2409 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_AMP_in_and2418 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000080L});
    public static final BitSet FOLLOW_AMP_in_and2420 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_not0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_or_in_synpred1_FTS340 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_and_in_synpred2_FTS378 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_not_in_synpred3_FTS421 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_fuzzy_in_synpred4_FTS630 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_fuzzy_in_synpred5_FTS659 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_slop_in_synpred6_FTS695 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_fuzzy_in_synpred7_FTS738 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_or_in_synpred8_FTS1225 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_and_in_synpred9_FTS1261 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_not_in_synpred10_FTS1298 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_fuzzy_in_synpred11_FTS1494 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_fuzzy_in_synpred12_FTS1523 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_slop_in_synpred13_FTS1552 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_fuzzy_in_synpred14_FTS1583 = new BitSet(new long[]{0x0000000000000002L});

}