// $ANTLR !Unknown version! W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g 2009-04-28 14:03:14
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
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "FTS", "DISJUNCTION", "CONJUNCTION", "NEGATION", "TERM", "EXACT_TERM", "PHRASE", "SYNONYM", "RANGE", "PROXIMITY", "DEFAULT", "MANDATORY", "OPTIONAL", "EXCLUDE", "FIELD_DISJUNCTION", "FIELD_CONJUNCTION", "FIELD_NEGATION", "FIELD_GROUP", "FIELD_DEFAULT", "FIELD_MANDATORY", "FIELD_OPTIONAL", "FIELD_EXCLUDE", "FG_TERM", "FG_EXACT_TERM", "FG_PHRASE", "FG_SYNONYM", "FG_PROXIMITY", "FG_RANGE", "FIELD_REF", "INCLUSIVE", "EXCLUSIVE", "QUALIFIER", "PREFIX", "NAME_SPACE", "BOOST", "FUZZY", "PLUS", "BAR", "MINUS", "LPAREN", "RPAREN", "TILDA", "CARAT", "COLON", "EQUALS", "FTSPHRASE", "STAR", "DECIMAL_INTEGER_LITERAL", "DOTDOT", "TO", "LSQUARE", "LT", "RSQUARE", "GT", "AT", "URI", "ID", "FTSWORD", "OR", "AND", "NOT", "FLOATING_POINT_LITERAL", "AMP", "EXCLAMATION", "F_ESC", "F_URI_ALPHA", "F_URI_DIGIT", "F_URI_OTHER", "F_HEX", "F_URI_ESC", "DOT", "QUESTION_MARK", "LCURL", "RCURL", "COMMA", "DOLLAR", "DECIMAL_NUMERAL", "INWORD", "START_RANGE_I", "START_RANGE_F", "DIGIT", "EXPONENT", "ZERO_DIGIT", "NON_ZERO_DIGIT", "E", "SIGNED_INTEGER", "WS"
    };
    public static final int DOLLAR=79;
    public static final int TERM=8;
    public static final int PREFIX=36;
    public static final int EXPONENT=85;
    public static final int START_RANGE_I=82;
    public static final int LT=55;
    public static final int STAR=50;
    public static final int LSQUARE=54;
    public static final int AMP=66;
    public static final int FG_PROXIMITY=30;
    public static final int FG_TERM=26;
    public static final int EXACT_TERM=9;
    public static final int START_RANGE_F=83;
    public static final int FUZZY=39;
    public static final int FIELD_DISJUNCTION=18;
    public static final int F_URI_ALPHA=69;
    public static final int DOTDOT=52;
    public static final int EQUALS=48;
    public static final int NOT=64;
    public static final int MANDATORY=15;
    public static final int FG_EXACT_TERM=27;
    public static final int FIELD_EXCLUDE=25;
    public static final int EXCLUSIVE=34;
    public static final int AND=63;
    public static final int ID=60;
    public static final int EOF=-1;
    public static final int NAME_SPACE=37;
    public static final int LPAREN=43;
    public static final int BOOST=38;
    public static final int AT=58;
    public static final int RPAREN=44;
    public static final int TILDA=45;
    public static final int DECIMAL_NUMERAL=80;
    public static final int EXCLAMATION=67;
    public static final int FLOATING_POINT_LITERAL=65;
    public static final int COMMA=78;
    public static final int F_URI_DIGIT=70;
    public static final int SIGNED_INTEGER=89;
    public static final int FIELD_DEFAULT=22;
    public static final int QUESTION_MARK=75;
    public static final int CARAT=46;
    public static final int PLUS=40;
    public static final int ZERO_DIGIT=86;
    public static final int DIGIT=84;
    public static final int FIELD_OPTIONAL=24;
    public static final int DOT=74;
    public static final int F_ESC=68;
    public static final int SYNONYM=11;
    public static final int EXCLUDE=17;
    public static final int E=88;
    public static final int NON_ZERO_DIGIT=87;
    public static final int TO=53;
    public static final int QUALIFIER=35;
    public static final int CONJUNCTION=6;
    public static final int FIELD_GROUP=21;
    public static final int DEFAULT=14;
    public static final int INWORD=81;
    public static final int RANGE=12;
    public static final int RSQUARE=56;
    public static final int MINUS=42;
    public static final int PROXIMITY=13;
    public static final int FIELD_REF=32;
    public static final int FTSWORD=61;
    public static final int PHRASE=10;
    public static final int OPTIONAL=16;
    public static final int URI=59;
    public static final int COLON=47;
    public static final int DISJUNCTION=5;
    public static final int LCURL=76;
    public static final int FTS=4;
    public static final int WS=90;
    public static final int F_URI_OTHER=71;
    public static final int FG_SYNONYM=29;
    public static final int F_URI_ESC=73;
    public static final int NEGATION=7;
    public static final int FTSPHRASE=49;
    public static final int FIELD_CONJUNCTION=19;
    public static final int INCLUSIVE=33;
    public static final int RCURL=77;
    public static final int OR=62;
    public static final int GT=57;
    public static final int FIELD_MANDATORY=23;
    public static final int F_HEX=72;
    public static final int DECIMAL_INTEGER_LITERAL=51;
    public static final int FG_RANGE=31;
    public static final int BAR=41;
    public static final int FG_PHRASE=28;
    public static final int FIELD_NEGATION=20;

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
    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:185:1: ftsQuery : ftsImplicitConjunctionOrDisjunction EOF -> ftsImplicitConjunctionOrDisjunction ;
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
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:186:5: ( ftsImplicitConjunctionOrDisjunction EOF -> ftsImplicitConjunctionOrDisjunction )
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:186:8: ftsImplicitConjunctionOrDisjunction EOF
            {
            pushFollow(FOLLOW_ftsImplicitConjunctionOrDisjunction_in_ftsQuery240);
            ftsImplicitConjunctionOrDisjunction1=ftsImplicitConjunctionOrDisjunction();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_ftsImplicitConjunctionOrDisjunction.add(ftsImplicitConjunctionOrDisjunction1.getTree());
            EOF2=(Token)match(input,EOF,FOLLOW_EOF_in_ftsQuery242); if (state.failed) return retval; 
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
            // 187:3: -> ftsImplicitConjunctionOrDisjunction
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
    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:190:1: ftsImplicitConjunctionOrDisjunction : ({...}? ftsExplicitDisjunction ( ftsExplicitDisjunction )* -> ^( CONJUNCTION ( ftsExplicitDisjunction )+ ) | ftsExplicitDisjunction ( ftsExplicitDisjunction )* -> ^( DISJUNCTION ( ftsExplicitDisjunction )+ ) );
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
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:191:2: ({...}? ftsExplicitDisjunction ( ftsExplicitDisjunction )* -> ^( CONJUNCTION ( ftsExplicitDisjunction )+ ) | ftsExplicitDisjunction ( ftsExplicitDisjunction )* -> ^( DISJUNCTION ( ftsExplicitDisjunction )+ ) )
            int alt3=2;
            alt3 = dfa3.predict(input);
            switch (alt3) {
                case 1 :
                    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:191:4: {...}? ftsExplicitDisjunction ( ftsExplicitDisjunction )*
                    {
                    if ( !((defaultConjunction())) ) {
                        if (state.backtracking>0) {state.failed=true; return retval;}
                        throw new FailedPredicateException(input, "ftsImplicitConjunctionOrDisjunction", "defaultConjunction()");
                    }
                    pushFollow(FOLLOW_ftsExplicitDisjunction_in_ftsImplicitConjunctionOrDisjunction264);
                    ftsExplicitDisjunction3=ftsExplicitDisjunction();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_ftsExplicitDisjunction.add(ftsExplicitDisjunction3.getTree());
                    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:191:51: ( ftsExplicitDisjunction )*
                    loop1:
                    do {
                        int alt1=2;
                        int LA1_0 = input.LA(1);

                        if ( ((LA1_0>=PLUS && LA1_0<=LPAREN)||LA1_0==TILDA||(LA1_0>=EQUALS && LA1_0<=FTSPHRASE)||LA1_0==DECIMAL_INTEGER_LITERAL||(LA1_0>=TO && LA1_0<=LT)||(LA1_0>=AT && LA1_0<=FLOATING_POINT_LITERAL)||LA1_0==EXCLAMATION) ) {
                            alt1=1;
                        }


                        switch (alt1) {
                    	case 1 :
                    	    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:191:52: ftsExplicitDisjunction
                    	    {
                    	    pushFollow(FOLLOW_ftsExplicitDisjunction_in_ftsImplicitConjunctionOrDisjunction267);
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
                    // 192:3: -> ^( CONJUNCTION ( ftsExplicitDisjunction )+ )
                    {
                        // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:192:6: ^( CONJUNCTION ( ftsExplicitDisjunction )+ )
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
                    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:193:5: ftsExplicitDisjunction ( ftsExplicitDisjunction )*
                    {
                    pushFollow(FOLLOW_ftsExplicitDisjunction_in_ftsImplicitConjunctionOrDisjunction286);
                    ftsExplicitDisjunction5=ftsExplicitDisjunction();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_ftsExplicitDisjunction.add(ftsExplicitDisjunction5.getTree());
                    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:193:28: ( ftsExplicitDisjunction )*
                    loop2:
                    do {
                        int alt2=2;
                        int LA2_0 = input.LA(1);

                        if ( ((LA2_0>=PLUS && LA2_0<=LPAREN)||LA2_0==TILDA||(LA2_0>=EQUALS && LA2_0<=FTSPHRASE)||LA2_0==DECIMAL_INTEGER_LITERAL||(LA2_0>=TO && LA2_0<=LT)||(LA2_0>=AT && LA2_0<=FLOATING_POINT_LITERAL)||LA2_0==EXCLAMATION) ) {
                            alt2=1;
                        }


                        switch (alt2) {
                    	case 1 :
                    	    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:193:29: ftsExplicitDisjunction
                    	    {
                    	    pushFollow(FOLLOW_ftsExplicitDisjunction_in_ftsImplicitConjunctionOrDisjunction289);
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
                    // 194:3: -> ^( DISJUNCTION ( ftsExplicitDisjunction )+ )
                    {
                        // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:194:6: ^( DISJUNCTION ( ftsExplicitDisjunction )+ )
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
    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:197:1: ftsExplicitDisjunction : ftsExplictConjunction ( ( or )=> or ftsExplictConjunction )* -> ^( DISJUNCTION ( ftsExplictConjunction )+ ) ;
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
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:198:2: ( ftsExplictConjunction ( ( or )=> or ftsExplictConjunction )* -> ^( DISJUNCTION ( ftsExplictConjunction )+ ) )
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:198:4: ftsExplictConjunction ( ( or )=> or ftsExplictConjunction )*
            {
            pushFollow(FOLLOW_ftsExplictConjunction_in_ftsExplicitDisjunction314);
            ftsExplictConjunction7=ftsExplictConjunction();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_ftsExplictConjunction.add(ftsExplictConjunction7.getTree());
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:198:26: ( ( or )=> or ftsExplictConjunction )*
            loop4:
            do {
                int alt4=2;
                alt4 = dfa4.predict(input);
                switch (alt4) {
            	case 1 :
            	    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:198:27: ( or )=> or ftsExplictConjunction
            	    {
            	    pushFollow(FOLLOW_or_in_ftsExplicitDisjunction323);
            	    or8=or();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_or.add(or8.getTree());
            	    pushFollow(FOLLOW_ftsExplictConjunction_in_ftsExplicitDisjunction325);
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
            // 199:3: -> ^( DISJUNCTION ( ftsExplictConjunction )+ )
            {
                // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:199:6: ^( DISJUNCTION ( ftsExplictConjunction )+ )
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
    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:202:1: ftsExplictConjunction : ftsPrefixed ( ( and )=> and ftsPrefixed )* -> ^( CONJUNCTION ( ftsPrefixed )+ ) ;
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
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:203:2: ( ftsPrefixed ( ( and )=> and ftsPrefixed )* -> ^( CONJUNCTION ( ftsPrefixed )+ ) )
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:203:4: ftsPrefixed ( ( and )=> and ftsPrefixed )*
            {
            pushFollow(FOLLOW_ftsPrefixed_in_ftsExplictConjunction350);
            ftsPrefixed10=ftsPrefixed();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_ftsPrefixed.add(ftsPrefixed10.getTree());
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:203:16: ( ( and )=> and ftsPrefixed )*
            loop5:
            do {
                int alt5=2;
                alt5 = dfa5.predict(input);
                switch (alt5) {
            	case 1 :
            	    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:203:17: ( and )=> and ftsPrefixed
            	    {
            	    pushFollow(FOLLOW_and_in_ftsExplictConjunction359);
            	    and11=and();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_and.add(and11.getTree());
            	    pushFollow(FOLLOW_ftsPrefixed_in_ftsExplictConjunction361);
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
            // 204:3: -> ^( CONJUNCTION ( ftsPrefixed )+ )
            {
                // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:204:6: ^( CONJUNCTION ( ftsPrefixed )+ )
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
    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:208:1: ftsPrefixed : ( ( not )=> not ftsTest -> ^( NEGATION ftsTest ) | ftsTest -> ^( DEFAULT ftsTest ) | PLUS ftsTest -> ^( MANDATORY ftsTest ) | BAR ftsTest -> ^( OPTIONAL ftsTest ) | MINUS ftsTest -> ^( EXCLUDE ftsTest ) );
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
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:209:5: ( ( not )=> not ftsTest -> ^( NEGATION ftsTest ) | ftsTest -> ^( DEFAULT ftsTest ) | PLUS ftsTest -> ^( MANDATORY ftsTest ) | BAR ftsTest -> ^( OPTIONAL ftsTest ) | MINUS ftsTest -> ^( EXCLUDE ftsTest ) )
            int alt6=5;
            alt6 = dfa6.predict(input);
            switch (alt6) {
                case 1 :
                    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:209:7: ( not )=> not ftsTest
                    {
                    pushFollow(FOLLOW_not_in_ftsPrefixed400);
                    not13=not();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_not.add(not13.getTree());
                    pushFollow(FOLLOW_ftsTest_in_ftsPrefixed402);
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
                    // 210:3: -> ^( NEGATION ftsTest )
                    {
                        // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:210:6: ^( NEGATION ftsTest )
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
                    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:211:7: ftsTest
                    {
                    pushFollow(FOLLOW_ftsTest_in_ftsPrefixed420);
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
                    // 212:3: -> ^( DEFAULT ftsTest )
                    {
                        // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:212:6: ^( DEFAULT ftsTest )
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
                    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:213:9: PLUS ftsTest
                    {
                    PLUS16=(Token)match(input,PLUS,FOLLOW_PLUS_in_ftsPrefixed440); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_PLUS.add(PLUS16);

                    pushFollow(FOLLOW_ftsTest_in_ftsPrefixed442);
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
                    // 214:17: -> ^( MANDATORY ftsTest )
                    {
                        // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:214:20: ^( MANDATORY ftsTest )
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
                    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:215:9: BAR ftsTest
                    {
                    BAR18=(Token)match(input,BAR,FOLLOW_BAR_in_ftsPrefixed476); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_BAR.add(BAR18);

                    pushFollow(FOLLOW_ftsTest_in_ftsPrefixed478);
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
                    // 216:17: -> ^( OPTIONAL ftsTest )
                    {
                        // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:216:20: ^( OPTIONAL ftsTest )
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
                    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:217:9: MINUS ftsTest
                    {
                    MINUS20=(Token)match(input,MINUS,FOLLOW_MINUS_in_ftsPrefixed512); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_MINUS.add(MINUS20);

                    pushFollow(FOLLOW_ftsTest_in_ftsPrefixed514);
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
                    // 218:17: -> ^( EXCLUDE ftsTest )
                    {
                        // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:218:20: ^( EXCLUDE ftsTest )
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
    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:221:1: ftsTest : ( ftsTerm ( ( fuzzy )=> fuzzy )? ( boost )? -> ^( TERM ftsTerm ( fuzzy )? ( boost )? ) | ftsExactTerm ( ( fuzzy )=> fuzzy )? ( boost )? -> ^( EXACT_TERM ftsExactTerm ( fuzzy )? ( boost )? ) | ftsPhrase ( ( fuzzy )=> fuzzy )? ( boost )? -> ^( PHRASE ftsPhrase ( fuzzy )? ( boost )? ) | ftsSynonym ( ( fuzzy )=> fuzzy )? ( boost )? -> ^( SYNONYM ftsSynonym ( fuzzy )? ( boost )? ) | ftsFieldGroupProximity -> ^( PROXIMITY ftsFieldGroupProximity ) | ftsRange ( boost )? -> ^( RANGE ftsRange ( boost )? ) | ftsFieldGroup -> ftsFieldGroup | LPAREN ftsImplicitConjunctionOrDisjunction RPAREN ( boost )? -> ftsImplicitConjunctionOrDisjunction ( boost )? );
    public final FTSParser.ftsTest_return ftsTest() throws RecognitionException {
        FTSParser.ftsTest_return retval = new FTSParser.ftsTest_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token LPAREN38=null;
        Token RPAREN40=null;
        FTSParser.ftsTerm_return ftsTerm22 = null;

        FTSParser.fuzzy_return fuzzy23 = null;

        FTSParser.boost_return boost24 = null;

        FTSParser.ftsExactTerm_return ftsExactTerm25 = null;

        FTSParser.fuzzy_return fuzzy26 = null;

        FTSParser.boost_return boost27 = null;

        FTSParser.ftsPhrase_return ftsPhrase28 = null;

        FTSParser.fuzzy_return fuzzy29 = null;

        FTSParser.boost_return boost30 = null;

        FTSParser.ftsSynonym_return ftsSynonym31 = null;

        FTSParser.fuzzy_return fuzzy32 = null;

        FTSParser.boost_return boost33 = null;

        FTSParser.ftsFieldGroupProximity_return ftsFieldGroupProximity34 = null;

        FTSParser.ftsRange_return ftsRange35 = null;

        FTSParser.boost_return boost36 = null;

        FTSParser.ftsFieldGroup_return ftsFieldGroup37 = null;

        FTSParser.ftsImplicitConjunctionOrDisjunction_return ftsImplicitConjunctionOrDisjunction39 = null;

        FTSParser.boost_return boost41 = null;


        Object LPAREN38_tree=null;
        Object RPAREN40_tree=null;
        RewriteRuleTokenStream stream_RPAREN=new RewriteRuleTokenStream(adaptor,"token RPAREN");
        RewriteRuleTokenStream stream_LPAREN=new RewriteRuleTokenStream(adaptor,"token LPAREN");
        RewriteRuleSubtreeStream stream_ftsFieldGroup=new RewriteRuleSubtreeStream(adaptor,"rule ftsFieldGroup");
        RewriteRuleSubtreeStream stream_ftsTerm=new RewriteRuleSubtreeStream(adaptor,"rule ftsTerm");
        RewriteRuleSubtreeStream stream_ftsExactTerm=new RewriteRuleSubtreeStream(adaptor,"rule ftsExactTerm");
        RewriteRuleSubtreeStream stream_ftsImplicitConjunctionOrDisjunction=new RewriteRuleSubtreeStream(adaptor,"rule ftsImplicitConjunctionOrDisjunction");
        RewriteRuleSubtreeStream stream_fuzzy=new RewriteRuleSubtreeStream(adaptor,"rule fuzzy");
        RewriteRuleSubtreeStream stream_ftsPhrase=new RewriteRuleSubtreeStream(adaptor,"rule ftsPhrase");
        RewriteRuleSubtreeStream stream_boost=new RewriteRuleSubtreeStream(adaptor,"rule boost");
        RewriteRuleSubtreeStream stream_ftsRange=new RewriteRuleSubtreeStream(adaptor,"rule ftsRange");
        RewriteRuleSubtreeStream stream_ftsSynonym=new RewriteRuleSubtreeStream(adaptor,"rule ftsSynonym");
        RewriteRuleSubtreeStream stream_ftsFieldGroupProximity=new RewriteRuleSubtreeStream(adaptor,"rule ftsFieldGroupProximity");
        try {
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:222:5: ( ftsTerm ( ( fuzzy )=> fuzzy )? ( boost )? -> ^( TERM ftsTerm ( fuzzy )? ( boost )? ) | ftsExactTerm ( ( fuzzy )=> fuzzy )? ( boost )? -> ^( EXACT_TERM ftsExactTerm ( fuzzy )? ( boost )? ) | ftsPhrase ( ( fuzzy )=> fuzzy )? ( boost )? -> ^( PHRASE ftsPhrase ( fuzzy )? ( boost )? ) | ftsSynonym ( ( fuzzy )=> fuzzy )? ( boost )? -> ^( SYNONYM ftsSynonym ( fuzzy )? ( boost )? ) | ftsFieldGroupProximity -> ^( PROXIMITY ftsFieldGroupProximity ) | ftsRange ( boost )? -> ^( RANGE ftsRange ( boost )? ) | ftsFieldGroup -> ftsFieldGroup | LPAREN ftsImplicitConjunctionOrDisjunction RPAREN ( boost )? -> ftsImplicitConjunctionOrDisjunction ( boost )? )
            int alt17=8;
            alt17 = dfa17.predict(input);
            switch (alt17) {
                case 1 :
                    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:222:7: ftsTerm ( ( fuzzy )=> fuzzy )? ( boost )?
                    {
                    pushFollow(FOLLOW_ftsTerm_in_ftsTest556);
                    ftsTerm22=ftsTerm();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_ftsTerm.add(ftsTerm22.getTree());
                    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:222:15: ( ( fuzzy )=> fuzzy )?
                    int alt7=2;
                    int LA7_0 = input.LA(1);

                    if ( (LA7_0==TILDA) ) {
                        int LA7_1 = input.LA(2);

                        if ( (LA7_1==DECIMAL_INTEGER_LITERAL||LA7_1==FLOATING_POINT_LITERAL) ) {
                            int LA7_3 = input.LA(3);

                            if ( (synpred4_FTS()) ) {
                                alt7=1;
                            }
                        }
                    }
                    switch (alt7) {
                        case 1 :
                            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:222:16: ( fuzzy )=> fuzzy
                            {
                            pushFollow(FOLLOW_fuzzy_in_ftsTest565);
                            fuzzy23=fuzzy();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_fuzzy.add(fuzzy23.getTree());

                            }
                            break;

                    }

                    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:222:35: ( boost )?
                    int alt8=2;
                    int LA8_0 = input.LA(1);

                    if ( (LA8_0==CARAT) ) {
                        alt8=1;
                    }
                    switch (alt8) {
                        case 1 :
                            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:222:35: boost
                            {
                            pushFollow(FOLLOW_boost_in_ftsTest569);
                            boost24=boost();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_boost.add(boost24.getTree());

                            }
                            break;

                    }



                    // AST REWRITE
                    // elements: boost, ftsTerm, fuzzy
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 223:3: -> ^( TERM ftsTerm ( fuzzy )? ( boost )? )
                    {
                        // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:223:6: ^( TERM ftsTerm ( fuzzy )? ( boost )? )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(TERM, "TERM"), root_1);

                        adaptor.addChild(root_1, stream_ftsTerm.nextTree());
                        // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:223:21: ( fuzzy )?
                        if ( stream_fuzzy.hasNext() ) {
                            adaptor.addChild(root_1, stream_fuzzy.nextTree());

                        }
                        stream_fuzzy.reset();
                        // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:223:28: ( boost )?
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
                    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:224:4: ftsExactTerm ( ( fuzzy )=> fuzzy )? ( boost )?
                    {
                    pushFollow(FOLLOW_ftsExactTerm_in_ftsTest591);
                    ftsExactTerm25=ftsExactTerm();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_ftsExactTerm.add(ftsExactTerm25.getTree());
                    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:224:17: ( ( fuzzy )=> fuzzy )?
                    int alt9=2;
                    int LA9_0 = input.LA(1);

                    if ( (LA9_0==TILDA) ) {
                        int LA9_1 = input.LA(2);

                        if ( (LA9_1==DECIMAL_INTEGER_LITERAL||LA9_1==FLOATING_POINT_LITERAL) ) {
                            int LA9_3 = input.LA(3);

                            if ( (synpred5_FTS()) ) {
                                alt9=1;
                            }
                        }
                    }
                    switch (alt9) {
                        case 1 :
                            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:224:18: ( fuzzy )=> fuzzy
                            {
                            pushFollow(FOLLOW_fuzzy_in_ftsTest600);
                            fuzzy26=fuzzy();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_fuzzy.add(fuzzy26.getTree());

                            }
                            break;

                    }

                    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:224:37: ( boost )?
                    int alt10=2;
                    int LA10_0 = input.LA(1);

                    if ( (LA10_0==CARAT) ) {
                        alt10=1;
                    }
                    switch (alt10) {
                        case 1 :
                            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:224:37: boost
                            {
                            pushFollow(FOLLOW_boost_in_ftsTest604);
                            boost27=boost();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_boost.add(boost27.getTree());

                            }
                            break;

                    }



                    // AST REWRITE
                    // elements: ftsExactTerm, boost, fuzzy
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 225:3: -> ^( EXACT_TERM ftsExactTerm ( fuzzy )? ( boost )? )
                    {
                        // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:225:6: ^( EXACT_TERM ftsExactTerm ( fuzzy )? ( boost )? )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(EXACT_TERM, "EXACT_TERM"), root_1);

                        adaptor.addChild(root_1, stream_ftsExactTerm.nextTree());
                        // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:225:32: ( fuzzy )?
                        if ( stream_fuzzy.hasNext() ) {
                            adaptor.addChild(root_1, stream_fuzzy.nextTree());

                        }
                        stream_fuzzy.reset();
                        // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:225:39: ( boost )?
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
                    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:226:9: ftsPhrase ( ( fuzzy )=> fuzzy )? ( boost )?
                    {
                    pushFollow(FOLLOW_ftsPhrase_in_ftsTest631);
                    ftsPhrase28=ftsPhrase();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_ftsPhrase.add(ftsPhrase28.getTree());
                    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:226:19: ( ( fuzzy )=> fuzzy )?
                    int alt11=2;
                    int LA11_0 = input.LA(1);

                    if ( (LA11_0==TILDA) ) {
                        int LA11_1 = input.LA(2);

                        if ( (LA11_1==DECIMAL_INTEGER_LITERAL||LA11_1==FLOATING_POINT_LITERAL) ) {
                            int LA11_3 = input.LA(3);

                            if ( (synpred6_FTS()) ) {
                                alt11=1;
                            }
                        }
                    }
                    switch (alt11) {
                        case 1 :
                            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:226:20: ( fuzzy )=> fuzzy
                            {
                            pushFollow(FOLLOW_fuzzy_in_ftsTest640);
                            fuzzy29=fuzzy();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_fuzzy.add(fuzzy29.getTree());

                            }
                            break;

                    }

                    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:226:39: ( boost )?
                    int alt12=2;
                    int LA12_0 = input.LA(1);

                    if ( (LA12_0==CARAT) ) {
                        alt12=1;
                    }
                    switch (alt12) {
                        case 1 :
                            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:226:39: boost
                            {
                            pushFollow(FOLLOW_boost_in_ftsTest644);
                            boost30=boost();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_boost.add(boost30.getTree());

                            }
                            break;

                    }



                    // AST REWRITE
                    // elements: boost, fuzzy, ftsPhrase
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 227:9: -> ^( PHRASE ftsPhrase ( fuzzy )? ( boost )? )
                    {
                        // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:227:12: ^( PHRASE ftsPhrase ( fuzzy )? ( boost )? )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(PHRASE, "PHRASE"), root_1);

                        adaptor.addChild(root_1, stream_ftsPhrase.nextTree());
                        // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:227:31: ( fuzzy )?
                        if ( stream_fuzzy.hasNext() ) {
                            adaptor.addChild(root_1, stream_fuzzy.nextTree());

                        }
                        stream_fuzzy.reset();
                        // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:227:38: ( boost )?
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
                    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:228:9: ftsSynonym ( ( fuzzy )=> fuzzy )? ( boost )?
                    {
                    pushFollow(FOLLOW_ftsSynonym_in_ftsTest677);
                    ftsSynonym31=ftsSynonym();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_ftsSynonym.add(ftsSynonym31.getTree());
                    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:228:20: ( ( fuzzy )=> fuzzy )?
                    int alt13=2;
                    int LA13_0 = input.LA(1);

                    if ( (LA13_0==TILDA) ) {
                        int LA13_1 = input.LA(2);

                        if ( (LA13_1==DECIMAL_INTEGER_LITERAL||LA13_1==FLOATING_POINT_LITERAL) ) {
                            int LA13_3 = input.LA(3);

                            if ( (synpred7_FTS()) ) {
                                alt13=1;
                            }
                        }
                    }
                    switch (alt13) {
                        case 1 :
                            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:228:21: ( fuzzy )=> fuzzy
                            {
                            pushFollow(FOLLOW_fuzzy_in_ftsTest686);
                            fuzzy32=fuzzy();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_fuzzy.add(fuzzy32.getTree());

                            }
                            break;

                    }

                    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:228:40: ( boost )?
                    int alt14=2;
                    int LA14_0 = input.LA(1);

                    if ( (LA14_0==CARAT) ) {
                        alt14=1;
                    }
                    switch (alt14) {
                        case 1 :
                            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:228:40: boost
                            {
                            pushFollow(FOLLOW_boost_in_ftsTest690);
                            boost33=boost();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_boost.add(boost33.getTree());

                            }
                            break;

                    }



                    // AST REWRITE
                    // elements: fuzzy, ftsSynonym, boost
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 229:9: -> ^( SYNONYM ftsSynonym ( fuzzy )? ( boost )? )
                    {
                        // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:229:12: ^( SYNONYM ftsSynonym ( fuzzy )? ( boost )? )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(SYNONYM, "SYNONYM"), root_1);

                        adaptor.addChild(root_1, stream_ftsSynonym.nextTree());
                        // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:229:33: ( fuzzy )?
                        if ( stream_fuzzy.hasNext() ) {
                            adaptor.addChild(root_1, stream_fuzzy.nextTree());

                        }
                        stream_fuzzy.reset();
                        // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:229:40: ( boost )?
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
                    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:230:7: ftsFieldGroupProximity
                    {
                    pushFollow(FOLLOW_ftsFieldGroupProximity_in_ftsTest721);
                    ftsFieldGroupProximity34=ftsFieldGroupProximity();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_ftsFieldGroupProximity.add(ftsFieldGroupProximity34.getTree());


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
                    // 231:9: -> ^( PROXIMITY ftsFieldGroupProximity )
                    {
                        // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:231:12: ^( PROXIMITY ftsFieldGroupProximity )
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
                    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:232:8: ftsRange ( boost )?
                    {
                    pushFollow(FOLLOW_ftsRange_in_ftsTest747);
                    ftsRange35=ftsRange();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_ftsRange.add(ftsRange35.getTree());
                    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:232:17: ( boost )?
                    int alt15=2;
                    int LA15_0 = input.LA(1);

                    if ( (LA15_0==CARAT) ) {
                        alt15=1;
                    }
                    switch (alt15) {
                        case 1 :
                            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:232:17: boost
                            {
                            pushFollow(FOLLOW_boost_in_ftsTest749);
                            boost36=boost();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_boost.add(boost36.getTree());

                            }
                            break;

                    }



                    // AST REWRITE
                    // elements: boost, ftsRange
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 233:9: -> ^( RANGE ftsRange ( boost )? )
                    {
                        // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:233:12: ^( RANGE ftsRange ( boost )? )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(RANGE, "RANGE"), root_1);

                        adaptor.addChild(root_1, stream_ftsRange.nextTree());
                        // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:233:29: ( boost )?
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
                case 7 :
                    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:234:7: ftsFieldGroup
                    {
                    pushFollow(FOLLOW_ftsFieldGroup_in_ftsTest777);
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
                    // 235:5: -> ftsFieldGroup
                    {
                        adaptor.addChild(root_0, stream_ftsFieldGroup.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 8 :
                    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:236:4: LPAREN ftsImplicitConjunctionOrDisjunction RPAREN ( boost )?
                    {
                    LPAREN38=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_ftsTest792); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_LPAREN.add(LPAREN38);

                    pushFollow(FOLLOW_ftsImplicitConjunctionOrDisjunction_in_ftsTest794);
                    ftsImplicitConjunctionOrDisjunction39=ftsImplicitConjunctionOrDisjunction();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_ftsImplicitConjunctionOrDisjunction.add(ftsImplicitConjunctionOrDisjunction39.getTree());
                    RPAREN40=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_ftsTest796); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_RPAREN.add(RPAREN40);

                    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:236:54: ( boost )?
                    int alt16=2;
                    int LA16_0 = input.LA(1);

                    if ( (LA16_0==CARAT) ) {
                        alt16=1;
                    }
                    switch (alt16) {
                        case 1 :
                            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:236:54: boost
                            {
                            pushFollow(FOLLOW_boost_in_ftsTest798);
                            boost41=boost();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_boost.add(boost41.getTree());

                            }
                            break;

                    }



                    // AST REWRITE
                    // elements: boost, ftsImplicitConjunctionOrDisjunction
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 237:3: -> ftsImplicitConjunctionOrDisjunction ( boost )?
                    {
                        adaptor.addChild(root_0, stream_ftsImplicitConjunctionOrDisjunction.nextTree());
                        // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:237:42: ( boost )?
                        if ( stream_boost.hasNext() ) {
                            adaptor.addChild(root_0, stream_boost.nextTree());

                        }
                        stream_boost.reset();

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

    public static class fuzzy_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "fuzzy"
    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:240:1: fuzzy : TILDA number -> ^( FUZZY number ) ;
    public final FTSParser.fuzzy_return fuzzy() throws RecognitionException {
        FTSParser.fuzzy_return retval = new FTSParser.fuzzy_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token TILDA42=null;
        FTSParser.number_return number43 = null;


        Object TILDA42_tree=null;
        RewriteRuleTokenStream stream_TILDA=new RewriteRuleTokenStream(adaptor,"token TILDA");
        RewriteRuleSubtreeStream stream_number=new RewriteRuleSubtreeStream(adaptor,"rule number");
        try {
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:241:1: ( TILDA number -> ^( FUZZY number ) )
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:241:3: TILDA number
            {
            TILDA42=(Token)match(input,TILDA,FOLLOW_TILDA_in_fuzzy818); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_TILDA.add(TILDA42);

            pushFollow(FOLLOW_number_in_fuzzy820);
            number43=number();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_number.add(number43.getTree());


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
            // 242:1: -> ^( FUZZY number )
            {
                // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:242:4: ^( FUZZY number )
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

    public static class boost_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "boost"
    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:245:1: boost : CARAT number -> ^( BOOST number ) ;
    public final FTSParser.boost_return boost() throws RecognitionException {
        FTSParser.boost_return retval = new FTSParser.boost_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token CARAT44=null;
        FTSParser.number_return number45 = null;


        Object CARAT44_tree=null;
        RewriteRuleTokenStream stream_CARAT=new RewriteRuleTokenStream(adaptor,"token CARAT");
        RewriteRuleSubtreeStream stream_number=new RewriteRuleSubtreeStream(adaptor,"rule number");
        try {
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:246:1: ( CARAT number -> ^( BOOST number ) )
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:246:3: CARAT number
            {
            CARAT44=(Token)match(input,CARAT,FOLLOW_CARAT_in_boost837); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_CARAT.add(CARAT44);

            pushFollow(FOLLOW_number_in_boost839);
            number45=number();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_number.add(number45.getTree());


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
            // 247:1: -> ^( BOOST number )
            {
                // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:247:4: ^( BOOST number )
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
    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:250:1: ftsTerm : ( fieldReference COLON )? ftsWord -> ftsWord ( fieldReference )? ;
    public final FTSParser.ftsTerm_return ftsTerm() throws RecognitionException {
        FTSParser.ftsTerm_return retval = new FTSParser.ftsTerm_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token COLON47=null;
        FTSParser.fieldReference_return fieldReference46 = null;

        FTSParser.ftsWord_return ftsWord48 = null;


        Object COLON47_tree=null;
        RewriteRuleTokenStream stream_COLON=new RewriteRuleTokenStream(adaptor,"token COLON");
        RewriteRuleSubtreeStream stream_ftsWord=new RewriteRuleSubtreeStream(adaptor,"rule ftsWord");
        RewriteRuleSubtreeStream stream_fieldReference=new RewriteRuleSubtreeStream(adaptor,"rule fieldReference");
        try {
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:251:2: ( ( fieldReference COLON )? ftsWord -> ftsWord ( fieldReference )? )
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:251:4: ( fieldReference COLON )? ftsWord
            {
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:251:4: ( fieldReference COLON )?
            int alt18=2;
            int LA18_0 = input.LA(1);

            if ( ((LA18_0>=AT && LA18_0<=URI)) ) {
                alt18=1;
            }
            else if ( (LA18_0==ID) ) {
                int LA18_2 = input.LA(2);

                if ( (LA18_2==COLON) ) {
                    alt18=1;
                }
            }
            switch (alt18) {
                case 1 :
                    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:251:5: fieldReference COLON
                    {
                    pushFollow(FOLLOW_fieldReference_in_ftsTerm858);
                    fieldReference46=fieldReference();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_fieldReference.add(fieldReference46.getTree());
                    COLON47=(Token)match(input,COLON,FOLLOW_COLON_in_ftsTerm860); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_COLON.add(COLON47);


                    }
                    break;

            }

            pushFollow(FOLLOW_ftsWord_in_ftsTerm864);
            ftsWord48=ftsWord();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_ftsWord.add(ftsWord48.getTree());


            // AST REWRITE
            // elements: ftsWord, fieldReference
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 252:3: -> ftsWord ( fieldReference )?
            {
                adaptor.addChild(root_0, stream_ftsWord.nextTree());
                // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:252:14: ( fieldReference )?
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
    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:255:1: ftsExactTerm : EQUALS ftsTerm -> ftsTerm ;
    public final FTSParser.ftsExactTerm_return ftsExactTerm() throws RecognitionException {
        FTSParser.ftsExactTerm_return retval = new FTSParser.ftsExactTerm_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token EQUALS49=null;
        FTSParser.ftsTerm_return ftsTerm50 = null;


        Object EQUALS49_tree=null;
        RewriteRuleTokenStream stream_EQUALS=new RewriteRuleTokenStream(adaptor,"token EQUALS");
        RewriteRuleSubtreeStream stream_ftsTerm=new RewriteRuleSubtreeStream(adaptor,"rule ftsTerm");
        try {
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:256:2: ( EQUALS ftsTerm -> ftsTerm )
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:256:4: EQUALS ftsTerm
            {
            EQUALS49=(Token)match(input,EQUALS,FOLLOW_EQUALS_in_ftsExactTerm885); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_EQUALS.add(EQUALS49);

            pushFollow(FOLLOW_ftsTerm_in_ftsExactTerm887);
            ftsTerm50=ftsTerm();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_ftsTerm.add(ftsTerm50.getTree());


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
            // 257:3: -> ftsTerm
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
    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:260:1: ftsPhrase : ( fieldReference COLON )? FTSPHRASE -> FTSPHRASE ( fieldReference )? ;
    public final FTSParser.ftsPhrase_return ftsPhrase() throws RecognitionException {
        FTSParser.ftsPhrase_return retval = new FTSParser.ftsPhrase_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token COLON52=null;
        Token FTSPHRASE53=null;
        FTSParser.fieldReference_return fieldReference51 = null;


        Object COLON52_tree=null;
        Object FTSPHRASE53_tree=null;
        RewriteRuleTokenStream stream_COLON=new RewriteRuleTokenStream(adaptor,"token COLON");
        RewriteRuleTokenStream stream_FTSPHRASE=new RewriteRuleTokenStream(adaptor,"token FTSPHRASE");
        RewriteRuleSubtreeStream stream_fieldReference=new RewriteRuleSubtreeStream(adaptor,"rule fieldReference");
        try {
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:261:2: ( ( fieldReference COLON )? FTSPHRASE -> FTSPHRASE ( fieldReference )? )
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:261:6: ( fieldReference COLON )? FTSPHRASE
            {
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:261:6: ( fieldReference COLON )?
            int alt19=2;
            int LA19_0 = input.LA(1);

            if ( ((LA19_0>=AT && LA19_0<=ID)) ) {
                alt19=1;
            }
            switch (alt19) {
                case 1 :
                    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:261:7: fieldReference COLON
                    {
                    pushFollow(FOLLOW_fieldReference_in_ftsPhrase908);
                    fieldReference51=fieldReference();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_fieldReference.add(fieldReference51.getTree());
                    COLON52=(Token)match(input,COLON,FOLLOW_COLON_in_ftsPhrase910); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_COLON.add(COLON52);


                    }
                    break;

            }

            FTSPHRASE53=(Token)match(input,FTSPHRASE,FOLLOW_FTSPHRASE_in_ftsPhrase914); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_FTSPHRASE.add(FTSPHRASE53);



            // AST REWRITE
            // elements: fieldReference, FTSPHRASE
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 262:3: -> FTSPHRASE ( fieldReference )?
            {
                adaptor.addChild(root_0, stream_FTSPHRASE.nextNode());
                // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:262:16: ( fieldReference )?
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
    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:265:1: ftsSynonym : TILDA ftsTerm -> ftsTerm ;
    public final FTSParser.ftsSynonym_return ftsSynonym() throws RecognitionException {
        FTSParser.ftsSynonym_return retval = new FTSParser.ftsSynonym_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token TILDA54=null;
        FTSParser.ftsTerm_return ftsTerm55 = null;


        Object TILDA54_tree=null;
        RewriteRuleTokenStream stream_TILDA=new RewriteRuleTokenStream(adaptor,"token TILDA");
        RewriteRuleSubtreeStream stream_ftsTerm=new RewriteRuleSubtreeStream(adaptor,"rule ftsTerm");
        try {
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:266:2: ( TILDA ftsTerm -> ftsTerm )
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:266:4: TILDA ftsTerm
            {
            TILDA54=(Token)match(input,TILDA,FOLLOW_TILDA_in_ftsSynonym935); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_TILDA.add(TILDA54);

            pushFollow(FOLLOW_ftsTerm_in_ftsSynonym937);
            ftsTerm55=ftsTerm();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_ftsTerm.add(ftsTerm55.getTree());


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
            // 267:3: -> ftsTerm
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
    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:270:1: ftsRange : ( fieldReference COLON )? ftsFieldGroupRange -> ftsFieldGroupRange ( fieldReference )? ;
    public final FTSParser.ftsRange_return ftsRange() throws RecognitionException {
        FTSParser.ftsRange_return retval = new FTSParser.ftsRange_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token COLON57=null;
        FTSParser.fieldReference_return fieldReference56 = null;

        FTSParser.ftsFieldGroupRange_return ftsFieldGroupRange58 = null;


        Object COLON57_tree=null;
        RewriteRuleTokenStream stream_COLON=new RewriteRuleTokenStream(adaptor,"token COLON");
        RewriteRuleSubtreeStream stream_ftsFieldGroupRange=new RewriteRuleSubtreeStream(adaptor,"rule ftsFieldGroupRange");
        RewriteRuleSubtreeStream stream_fieldReference=new RewriteRuleSubtreeStream(adaptor,"rule fieldReference");
        try {
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:271:1: ( ( fieldReference COLON )? ftsFieldGroupRange -> ftsFieldGroupRange ( fieldReference )? )
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:271:3: ( fieldReference COLON )? ftsFieldGroupRange
            {
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:271:3: ( fieldReference COLON )?
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
                    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:271:4: fieldReference COLON
                    {
                    pushFollow(FOLLOW_fieldReference_in_ftsRange955);
                    fieldReference56=fieldReference();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_fieldReference.add(fieldReference56.getTree());
                    COLON57=(Token)match(input,COLON,FOLLOW_COLON_in_ftsRange957); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_COLON.add(COLON57);


                    }
                    break;

            }

            pushFollow(FOLLOW_ftsFieldGroupRange_in_ftsRange961);
            ftsFieldGroupRange58=ftsFieldGroupRange();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_ftsFieldGroupRange.add(ftsFieldGroupRange58.getTree());


            // AST REWRITE
            // elements: fieldReference, ftsFieldGroupRange
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 272:3: -> ftsFieldGroupRange ( fieldReference )?
            {
                adaptor.addChild(root_0, stream_ftsFieldGroupRange.nextTree());
                // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:272:25: ( fieldReference )?
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
    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:275:1: ftsFieldGroup : fieldReference COLON LPAREN ftsFieldGroupImplicitConjunctionOrDisjunction RPAREN -> ^( FIELD_GROUP fieldReference ftsFieldGroupImplicitConjunctionOrDisjunction ) ;
    public final FTSParser.ftsFieldGroup_return ftsFieldGroup() throws RecognitionException {
        FTSParser.ftsFieldGroup_return retval = new FTSParser.ftsFieldGroup_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token COLON60=null;
        Token LPAREN61=null;
        Token RPAREN63=null;
        FTSParser.fieldReference_return fieldReference59 = null;

        FTSParser.ftsFieldGroupImplicitConjunctionOrDisjunction_return ftsFieldGroupImplicitConjunctionOrDisjunction62 = null;


        Object COLON60_tree=null;
        Object LPAREN61_tree=null;
        Object RPAREN63_tree=null;
        RewriteRuleTokenStream stream_COLON=new RewriteRuleTokenStream(adaptor,"token COLON");
        RewriteRuleTokenStream stream_RPAREN=new RewriteRuleTokenStream(adaptor,"token RPAREN");
        RewriteRuleTokenStream stream_LPAREN=new RewriteRuleTokenStream(adaptor,"token LPAREN");
        RewriteRuleSubtreeStream stream_ftsFieldGroupImplicitConjunctionOrDisjunction=new RewriteRuleSubtreeStream(adaptor,"rule ftsFieldGroupImplicitConjunctionOrDisjunction");
        RewriteRuleSubtreeStream stream_fieldReference=new RewriteRuleSubtreeStream(adaptor,"rule fieldReference");
        try {
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:276:2: ( fieldReference COLON LPAREN ftsFieldGroupImplicitConjunctionOrDisjunction RPAREN -> ^( FIELD_GROUP fieldReference ftsFieldGroupImplicitConjunctionOrDisjunction ) )
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:276:4: fieldReference COLON LPAREN ftsFieldGroupImplicitConjunctionOrDisjunction RPAREN
            {
            pushFollow(FOLLOW_fieldReference_in_ftsFieldGroup981);
            fieldReference59=fieldReference();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_fieldReference.add(fieldReference59.getTree());
            COLON60=(Token)match(input,COLON,FOLLOW_COLON_in_ftsFieldGroup983); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_COLON.add(COLON60);

            LPAREN61=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_ftsFieldGroup985); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_LPAREN.add(LPAREN61);

            pushFollow(FOLLOW_ftsFieldGroupImplicitConjunctionOrDisjunction_in_ftsFieldGroup987);
            ftsFieldGroupImplicitConjunctionOrDisjunction62=ftsFieldGroupImplicitConjunctionOrDisjunction();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_ftsFieldGroupImplicitConjunctionOrDisjunction.add(ftsFieldGroupImplicitConjunctionOrDisjunction62.getTree());
            RPAREN63=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_ftsFieldGroup989); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_RPAREN.add(RPAREN63);



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
            // 277:3: -> ^( FIELD_GROUP fieldReference ftsFieldGroupImplicitConjunctionOrDisjunction )
            {
                // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:277:6: ^( FIELD_GROUP fieldReference ftsFieldGroupImplicitConjunctionOrDisjunction )
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
    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:280:1: ftsFieldGroupImplicitConjunctionOrDisjunction : ({...}? ftsFieldGroupExplicitDisjunction ( ftsFieldGroupExplicitDisjunction )* -> ^( FIELD_CONJUNCTION ( ftsFieldGroupExplicitDisjunction )+ ) | ftsFieldGroupExplicitDisjunction ( ftsFieldGroupExplicitDisjunction )* -> ^( FIELD_DISJUNCTION ( ftsFieldGroupExplicitDisjunction )+ ) );
    public final FTSParser.ftsFieldGroupImplicitConjunctionOrDisjunction_return ftsFieldGroupImplicitConjunctionOrDisjunction() throws RecognitionException {
        FTSParser.ftsFieldGroupImplicitConjunctionOrDisjunction_return retval = new FTSParser.ftsFieldGroupImplicitConjunctionOrDisjunction_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        FTSParser.ftsFieldGroupExplicitDisjunction_return ftsFieldGroupExplicitDisjunction64 = null;

        FTSParser.ftsFieldGroupExplicitDisjunction_return ftsFieldGroupExplicitDisjunction65 = null;

        FTSParser.ftsFieldGroupExplicitDisjunction_return ftsFieldGroupExplicitDisjunction66 = null;

        FTSParser.ftsFieldGroupExplicitDisjunction_return ftsFieldGroupExplicitDisjunction67 = null;


        RewriteRuleSubtreeStream stream_ftsFieldGroupExplicitDisjunction=new RewriteRuleSubtreeStream(adaptor,"rule ftsFieldGroupExplicitDisjunction");
        try {
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:281:2: ({...}? ftsFieldGroupExplicitDisjunction ( ftsFieldGroupExplicitDisjunction )* -> ^( FIELD_CONJUNCTION ( ftsFieldGroupExplicitDisjunction )+ ) | ftsFieldGroupExplicitDisjunction ( ftsFieldGroupExplicitDisjunction )* -> ^( FIELD_DISJUNCTION ( ftsFieldGroupExplicitDisjunction )+ ) )
            int alt23=2;
            alt23 = dfa23.predict(input);
            switch (alt23) {
                case 1 :
                    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:281:4: {...}? ftsFieldGroupExplicitDisjunction ( ftsFieldGroupExplicitDisjunction )*
                    {
                    if ( !((defaultFieldConjunction())) ) {
                        if (state.backtracking>0) {state.failed=true; return retval;}
                        throw new FailedPredicateException(input, "ftsFieldGroupImplicitConjunctionOrDisjunction", "defaultFieldConjunction()");
                    }
                    pushFollow(FOLLOW_ftsFieldGroupExplicitDisjunction_in_ftsFieldGroupImplicitConjunctionOrDisjunction1015);
                    ftsFieldGroupExplicitDisjunction64=ftsFieldGroupExplicitDisjunction();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_ftsFieldGroupExplicitDisjunction.add(ftsFieldGroupExplicitDisjunction64.getTree());
                    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:281:66: ( ftsFieldGroupExplicitDisjunction )*
                    loop21:
                    do {
                        int alt21=2;
                        int LA21_0 = input.LA(1);

                        if ( ((LA21_0>=PLUS && LA21_0<=LPAREN)||LA21_0==TILDA||(LA21_0>=EQUALS && LA21_0<=FTSPHRASE)||LA21_0==DECIMAL_INTEGER_LITERAL||(LA21_0>=TO && LA21_0<=LT)||(LA21_0>=ID && LA21_0<=FLOATING_POINT_LITERAL)||LA21_0==EXCLAMATION) ) {
                            alt21=1;
                        }


                        switch (alt21) {
                    	case 1 :
                    	    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:281:67: ftsFieldGroupExplicitDisjunction
                    	    {
                    	    pushFollow(FOLLOW_ftsFieldGroupExplicitDisjunction_in_ftsFieldGroupImplicitConjunctionOrDisjunction1018);
                    	    ftsFieldGroupExplicitDisjunction65=ftsFieldGroupExplicitDisjunction();

                    	    state._fsp--;
                    	    if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) stream_ftsFieldGroupExplicitDisjunction.add(ftsFieldGroupExplicitDisjunction65.getTree());

                    	    }
                    	    break;

                    	default :
                    	    break loop21;
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
                    // 282:3: -> ^( FIELD_CONJUNCTION ( ftsFieldGroupExplicitDisjunction )+ )
                    {
                        // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:282:6: ^( FIELD_CONJUNCTION ( ftsFieldGroupExplicitDisjunction )+ )
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
                    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:283:4: ftsFieldGroupExplicitDisjunction ( ftsFieldGroupExplicitDisjunction )*
                    {
                    pushFollow(FOLLOW_ftsFieldGroupExplicitDisjunction_in_ftsFieldGroupImplicitConjunctionOrDisjunction1036);
                    ftsFieldGroupExplicitDisjunction66=ftsFieldGroupExplicitDisjunction();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_ftsFieldGroupExplicitDisjunction.add(ftsFieldGroupExplicitDisjunction66.getTree());
                    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:283:37: ( ftsFieldGroupExplicitDisjunction )*
                    loop22:
                    do {
                        int alt22=2;
                        int LA22_0 = input.LA(1);

                        if ( ((LA22_0>=PLUS && LA22_0<=LPAREN)||LA22_0==TILDA||(LA22_0>=EQUALS && LA22_0<=FTSPHRASE)||LA22_0==DECIMAL_INTEGER_LITERAL||(LA22_0>=TO && LA22_0<=LT)||(LA22_0>=ID && LA22_0<=FLOATING_POINT_LITERAL)||LA22_0==EXCLAMATION) ) {
                            alt22=1;
                        }


                        switch (alt22) {
                    	case 1 :
                    	    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:283:38: ftsFieldGroupExplicitDisjunction
                    	    {
                    	    pushFollow(FOLLOW_ftsFieldGroupExplicitDisjunction_in_ftsFieldGroupImplicitConjunctionOrDisjunction1039);
                    	    ftsFieldGroupExplicitDisjunction67=ftsFieldGroupExplicitDisjunction();

                    	    state._fsp--;
                    	    if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) stream_ftsFieldGroupExplicitDisjunction.add(ftsFieldGroupExplicitDisjunction67.getTree());

                    	    }
                    	    break;

                    	default :
                    	    break loop22;
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
                    // 284:3: -> ^( FIELD_DISJUNCTION ( ftsFieldGroupExplicitDisjunction )+ )
                    {
                        // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:284:6: ^( FIELD_DISJUNCTION ( ftsFieldGroupExplicitDisjunction )+ )
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
    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:287:1: ftsFieldGroupExplicitDisjunction : ftsFieldGroupExplictConjunction ( ( or )=> or ftsFieldGroupExplictConjunction )* -> ^( FIELD_DISJUNCTION ( ftsFieldGroupExplictConjunction )+ ) ;
    public final FTSParser.ftsFieldGroupExplicitDisjunction_return ftsFieldGroupExplicitDisjunction() throws RecognitionException {
        FTSParser.ftsFieldGroupExplicitDisjunction_return retval = new FTSParser.ftsFieldGroupExplicitDisjunction_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        FTSParser.ftsFieldGroupExplictConjunction_return ftsFieldGroupExplictConjunction68 = null;

        FTSParser.or_return or69 = null;

        FTSParser.ftsFieldGroupExplictConjunction_return ftsFieldGroupExplictConjunction70 = null;


        RewriteRuleSubtreeStream stream_ftsFieldGroupExplictConjunction=new RewriteRuleSubtreeStream(adaptor,"rule ftsFieldGroupExplictConjunction");
        RewriteRuleSubtreeStream stream_or=new RewriteRuleSubtreeStream(adaptor,"rule or");
        try {
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:288:2: ( ftsFieldGroupExplictConjunction ( ( or )=> or ftsFieldGroupExplictConjunction )* -> ^( FIELD_DISJUNCTION ( ftsFieldGroupExplictConjunction )+ ) )
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:288:4: ftsFieldGroupExplictConjunction ( ( or )=> or ftsFieldGroupExplictConjunction )*
            {
            pushFollow(FOLLOW_ftsFieldGroupExplictConjunction_in_ftsFieldGroupExplicitDisjunction1064);
            ftsFieldGroupExplictConjunction68=ftsFieldGroupExplictConjunction();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_ftsFieldGroupExplictConjunction.add(ftsFieldGroupExplictConjunction68.getTree());
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:288:36: ( ( or )=> or ftsFieldGroupExplictConjunction )*
            loop24:
            do {
                int alt24=2;
                alt24 = dfa24.predict(input);
                switch (alt24) {
            	case 1 :
            	    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:288:37: ( or )=> or ftsFieldGroupExplictConjunction
            	    {
            	    pushFollow(FOLLOW_or_in_ftsFieldGroupExplicitDisjunction1073);
            	    or69=or();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_or.add(or69.getTree());
            	    pushFollow(FOLLOW_ftsFieldGroupExplictConjunction_in_ftsFieldGroupExplicitDisjunction1075);
            	    ftsFieldGroupExplictConjunction70=ftsFieldGroupExplictConjunction();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_ftsFieldGroupExplictConjunction.add(ftsFieldGroupExplictConjunction70.getTree());

            	    }
            	    break;

            	default :
            	    break loop24;
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
            // 289:3: -> ^( FIELD_DISJUNCTION ( ftsFieldGroupExplictConjunction )+ )
            {
                // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:289:6: ^( FIELD_DISJUNCTION ( ftsFieldGroupExplictConjunction )+ )
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
    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:292:1: ftsFieldGroupExplictConjunction : ftsFieldGroupPrefixed ( ( and )=> and ftsFieldGroupPrefixed )* -> ^( FIELD_CONJUNCTION ( ftsFieldGroupPrefixed )+ ) ;
    public final FTSParser.ftsFieldGroupExplictConjunction_return ftsFieldGroupExplictConjunction() throws RecognitionException {
        FTSParser.ftsFieldGroupExplictConjunction_return retval = new FTSParser.ftsFieldGroupExplictConjunction_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        FTSParser.ftsFieldGroupPrefixed_return ftsFieldGroupPrefixed71 = null;

        FTSParser.and_return and72 = null;

        FTSParser.ftsFieldGroupPrefixed_return ftsFieldGroupPrefixed73 = null;


        RewriteRuleSubtreeStream stream_ftsFieldGroupPrefixed=new RewriteRuleSubtreeStream(adaptor,"rule ftsFieldGroupPrefixed");
        RewriteRuleSubtreeStream stream_and=new RewriteRuleSubtreeStream(adaptor,"rule and");
        try {
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:293:2: ( ftsFieldGroupPrefixed ( ( and )=> and ftsFieldGroupPrefixed )* -> ^( FIELD_CONJUNCTION ( ftsFieldGroupPrefixed )+ ) )
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:293:4: ftsFieldGroupPrefixed ( ( and )=> and ftsFieldGroupPrefixed )*
            {
            pushFollow(FOLLOW_ftsFieldGroupPrefixed_in_ftsFieldGroupExplictConjunction1100);
            ftsFieldGroupPrefixed71=ftsFieldGroupPrefixed();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_ftsFieldGroupPrefixed.add(ftsFieldGroupPrefixed71.getTree());
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:293:26: ( ( and )=> and ftsFieldGroupPrefixed )*
            loop25:
            do {
                int alt25=2;
                alt25 = dfa25.predict(input);
                switch (alt25) {
            	case 1 :
            	    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:293:27: ( and )=> and ftsFieldGroupPrefixed
            	    {
            	    pushFollow(FOLLOW_and_in_ftsFieldGroupExplictConjunction1109);
            	    and72=and();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_and.add(and72.getTree());
            	    pushFollow(FOLLOW_ftsFieldGroupPrefixed_in_ftsFieldGroupExplictConjunction1111);
            	    ftsFieldGroupPrefixed73=ftsFieldGroupPrefixed();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_ftsFieldGroupPrefixed.add(ftsFieldGroupPrefixed73.getTree());

            	    }
            	    break;

            	default :
            	    break loop25;
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
            // 294:3: -> ^( FIELD_CONJUNCTION ( ftsFieldGroupPrefixed )+ )
            {
                // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:294:6: ^( FIELD_CONJUNCTION ( ftsFieldGroupPrefixed )+ )
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
    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:298:1: ftsFieldGroupPrefixed : ( ( not )=> not ftsFieldGroupTest -> ^( FIELD_NEGATION ftsFieldGroupTest ) | ftsFieldGroupTest -> ^( FIELD_DEFAULT ftsFieldGroupTest ) | PLUS ftsFieldGroupTest -> ^( FIELD_MANDATORY ftsFieldGroupTest ) | BAR ftsFieldGroupTest -> ^( FIELD_OPTIONAL ftsFieldGroupTest ) | MINUS ftsFieldGroupTest -> ^( FIELD_EXCLUDE ftsFieldGroupTest ) );
    public final FTSParser.ftsFieldGroupPrefixed_return ftsFieldGroupPrefixed() throws RecognitionException {
        FTSParser.ftsFieldGroupPrefixed_return retval = new FTSParser.ftsFieldGroupPrefixed_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token PLUS77=null;
        Token BAR79=null;
        Token MINUS81=null;
        FTSParser.not_return not74 = null;

        FTSParser.ftsFieldGroupTest_return ftsFieldGroupTest75 = null;

        FTSParser.ftsFieldGroupTest_return ftsFieldGroupTest76 = null;

        FTSParser.ftsFieldGroupTest_return ftsFieldGroupTest78 = null;

        FTSParser.ftsFieldGroupTest_return ftsFieldGroupTest80 = null;

        FTSParser.ftsFieldGroupTest_return ftsFieldGroupTest82 = null;


        Object PLUS77_tree=null;
        Object BAR79_tree=null;
        Object MINUS81_tree=null;
        RewriteRuleTokenStream stream_PLUS=new RewriteRuleTokenStream(adaptor,"token PLUS");
        RewriteRuleTokenStream stream_MINUS=new RewriteRuleTokenStream(adaptor,"token MINUS");
        RewriteRuleTokenStream stream_BAR=new RewriteRuleTokenStream(adaptor,"token BAR");
        RewriteRuleSubtreeStream stream_not=new RewriteRuleSubtreeStream(adaptor,"rule not");
        RewriteRuleSubtreeStream stream_ftsFieldGroupTest=new RewriteRuleSubtreeStream(adaptor,"rule ftsFieldGroupTest");
        try {
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:299:1: ( ( not )=> not ftsFieldGroupTest -> ^( FIELD_NEGATION ftsFieldGroupTest ) | ftsFieldGroupTest -> ^( FIELD_DEFAULT ftsFieldGroupTest ) | PLUS ftsFieldGroupTest -> ^( FIELD_MANDATORY ftsFieldGroupTest ) | BAR ftsFieldGroupTest -> ^( FIELD_OPTIONAL ftsFieldGroupTest ) | MINUS ftsFieldGroupTest -> ^( FIELD_EXCLUDE ftsFieldGroupTest ) )
            int alt26=5;
            alt26 = dfa26.predict(input);
            switch (alt26) {
                case 1 :
                    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:299:3: ( not )=> not ftsFieldGroupTest
                    {
                    pushFollow(FOLLOW_not_in_ftsFieldGroupPrefixed1145);
                    not74=not();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_not.add(not74.getTree());
                    pushFollow(FOLLOW_ftsFieldGroupTest_in_ftsFieldGroupPrefixed1147);
                    ftsFieldGroupTest75=ftsFieldGroupTest();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_ftsFieldGroupTest.add(ftsFieldGroupTest75.getTree());


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
                    // 300:3: -> ^( FIELD_NEGATION ftsFieldGroupTest )
                    {
                        // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:300:6: ^( FIELD_NEGATION ftsFieldGroupTest )
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
                    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:301:7: ftsFieldGroupTest
                    {
                    pushFollow(FOLLOW_ftsFieldGroupTest_in_ftsFieldGroupPrefixed1165);
                    ftsFieldGroupTest76=ftsFieldGroupTest();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_ftsFieldGroupTest.add(ftsFieldGroupTest76.getTree());


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
                    // 302:3: -> ^( FIELD_DEFAULT ftsFieldGroupTest )
                    {
                        // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:302:6: ^( FIELD_DEFAULT ftsFieldGroupTest )
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
                    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:303:9: PLUS ftsFieldGroupTest
                    {
                    PLUS77=(Token)match(input,PLUS,FOLLOW_PLUS_in_ftsFieldGroupPrefixed1185); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_PLUS.add(PLUS77);

                    pushFollow(FOLLOW_ftsFieldGroupTest_in_ftsFieldGroupPrefixed1187);
                    ftsFieldGroupTest78=ftsFieldGroupTest();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_ftsFieldGroupTest.add(ftsFieldGroupTest78.getTree());


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
                    // 304:17: -> ^( FIELD_MANDATORY ftsFieldGroupTest )
                    {
                        // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:304:20: ^( FIELD_MANDATORY ftsFieldGroupTest )
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
                    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:305:9: BAR ftsFieldGroupTest
                    {
                    BAR79=(Token)match(input,BAR,FOLLOW_BAR_in_ftsFieldGroupPrefixed1221); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_BAR.add(BAR79);

                    pushFollow(FOLLOW_ftsFieldGroupTest_in_ftsFieldGroupPrefixed1223);
                    ftsFieldGroupTest80=ftsFieldGroupTest();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_ftsFieldGroupTest.add(ftsFieldGroupTest80.getTree());


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
                    // 306:17: -> ^( FIELD_OPTIONAL ftsFieldGroupTest )
                    {
                        // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:306:20: ^( FIELD_OPTIONAL ftsFieldGroupTest )
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
                    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:307:9: MINUS ftsFieldGroupTest
                    {
                    MINUS81=(Token)match(input,MINUS,FOLLOW_MINUS_in_ftsFieldGroupPrefixed1257); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_MINUS.add(MINUS81);

                    pushFollow(FOLLOW_ftsFieldGroupTest_in_ftsFieldGroupPrefixed1259);
                    ftsFieldGroupTest82=ftsFieldGroupTest();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_ftsFieldGroupTest.add(ftsFieldGroupTest82.getTree());


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
                    // 308:17: -> ^( FIELD_EXCLUDE ftsFieldGroupTest )
                    {
                        // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:308:20: ^( FIELD_EXCLUDE ftsFieldGroupTest )
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
    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:312:1: ftsFieldGroupTest : ( ftsFieldGroupTerm ( ( fuzzy )=> fuzzy )? ( boost )? -> ^( FG_TERM ftsFieldGroupTerm ( fuzzy )? ( boost )? ) | ftsFieldGroupExactTerm ( ( fuzzy )=> fuzzy )? ( boost )? -> ^( FG_EXACT_TERM ftsFieldGroupExactTerm ( fuzzy )? ( boost )? ) | ftsFieldGroupPhrase ( ( fuzzy )=> fuzzy )? ( boost )? -> ^( FG_PHRASE ftsFieldGroupPhrase ( fuzzy )? ( boost )? ) | ftsFieldGroupSynonym ( ( fuzzy )=> fuzzy )? ( boost )? -> ^( FG_SYNONYM ftsFieldGroupSynonym ( fuzzy )? ( boost )? ) | ftsFieldGroupProximity -> ^( FG_PROXIMITY ftsFieldGroupProximity ) | ftsFieldGroupRange ( boost )? -> ^( FG_RANGE ftsFieldGroupRange ( boost )? ) | LPAREN ftsFieldGroupImplicitConjunctionOrDisjunction RPAREN ( boost )? -> ftsFieldGroupImplicitConjunctionOrDisjunction ( boost )? );
    public final FTSParser.ftsFieldGroupTest_return ftsFieldGroupTest() throws RecognitionException {
        FTSParser.ftsFieldGroupTest_return retval = new FTSParser.ftsFieldGroupTest_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token LPAREN98=null;
        Token RPAREN100=null;
        FTSParser.ftsFieldGroupTerm_return ftsFieldGroupTerm83 = null;

        FTSParser.fuzzy_return fuzzy84 = null;

        FTSParser.boost_return boost85 = null;

        FTSParser.ftsFieldGroupExactTerm_return ftsFieldGroupExactTerm86 = null;

        FTSParser.fuzzy_return fuzzy87 = null;

        FTSParser.boost_return boost88 = null;

        FTSParser.ftsFieldGroupPhrase_return ftsFieldGroupPhrase89 = null;

        FTSParser.fuzzy_return fuzzy90 = null;

        FTSParser.boost_return boost91 = null;

        FTSParser.ftsFieldGroupSynonym_return ftsFieldGroupSynonym92 = null;

        FTSParser.fuzzy_return fuzzy93 = null;

        FTSParser.boost_return boost94 = null;

        FTSParser.ftsFieldGroupProximity_return ftsFieldGroupProximity95 = null;

        FTSParser.ftsFieldGroupRange_return ftsFieldGroupRange96 = null;

        FTSParser.boost_return boost97 = null;

        FTSParser.ftsFieldGroupImplicitConjunctionOrDisjunction_return ftsFieldGroupImplicitConjunctionOrDisjunction99 = null;

        FTSParser.boost_return boost101 = null;


        Object LPAREN98_tree=null;
        Object RPAREN100_tree=null;
        RewriteRuleTokenStream stream_RPAREN=new RewriteRuleTokenStream(adaptor,"token RPAREN");
        RewriteRuleTokenStream stream_LPAREN=new RewriteRuleTokenStream(adaptor,"token LPAREN");
        RewriteRuleSubtreeStream stream_ftsFieldGroupRange=new RewriteRuleSubtreeStream(adaptor,"rule ftsFieldGroupRange");
        RewriteRuleSubtreeStream stream_ftsFieldGroupPhrase=new RewriteRuleSubtreeStream(adaptor,"rule ftsFieldGroupPhrase");
        RewriteRuleSubtreeStream stream_ftsFieldGroupImplicitConjunctionOrDisjunction=new RewriteRuleSubtreeStream(adaptor,"rule ftsFieldGroupImplicitConjunctionOrDisjunction");
        RewriteRuleSubtreeStream stream_fuzzy=new RewriteRuleSubtreeStream(adaptor,"rule fuzzy");
        RewriteRuleSubtreeStream stream_ftsFieldGroupTerm=new RewriteRuleSubtreeStream(adaptor,"rule ftsFieldGroupTerm");
        RewriteRuleSubtreeStream stream_boost=new RewriteRuleSubtreeStream(adaptor,"rule boost");
        RewriteRuleSubtreeStream stream_ftsFieldGroupSynonym=new RewriteRuleSubtreeStream(adaptor,"rule ftsFieldGroupSynonym");
        RewriteRuleSubtreeStream stream_ftsFieldGroupExactTerm=new RewriteRuleSubtreeStream(adaptor,"rule ftsFieldGroupExactTerm");
        RewriteRuleSubtreeStream stream_ftsFieldGroupProximity=new RewriteRuleSubtreeStream(adaptor,"rule ftsFieldGroupProximity");
        try {
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:313:2: ( ftsFieldGroupTerm ( ( fuzzy )=> fuzzy )? ( boost )? -> ^( FG_TERM ftsFieldGroupTerm ( fuzzy )? ( boost )? ) | ftsFieldGroupExactTerm ( ( fuzzy )=> fuzzy )? ( boost )? -> ^( FG_EXACT_TERM ftsFieldGroupExactTerm ( fuzzy )? ( boost )? ) | ftsFieldGroupPhrase ( ( fuzzy )=> fuzzy )? ( boost )? -> ^( FG_PHRASE ftsFieldGroupPhrase ( fuzzy )? ( boost )? ) | ftsFieldGroupSynonym ( ( fuzzy )=> fuzzy )? ( boost )? -> ^( FG_SYNONYM ftsFieldGroupSynonym ( fuzzy )? ( boost )? ) | ftsFieldGroupProximity -> ^( FG_PROXIMITY ftsFieldGroupProximity ) | ftsFieldGroupRange ( boost )? -> ^( FG_RANGE ftsFieldGroupRange ( boost )? ) | LPAREN ftsFieldGroupImplicitConjunctionOrDisjunction RPAREN ( boost )? -> ftsFieldGroupImplicitConjunctionOrDisjunction ( boost )? )
            int alt37=7;
            alt37 = dfa37.predict(input);
            switch (alt37) {
                case 1 :
                    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:313:4: ftsFieldGroupTerm ( ( fuzzy )=> fuzzy )? ( boost )?
                    {
                    pushFollow(FOLLOW_ftsFieldGroupTerm_in_ftsFieldGroupTest1298);
                    ftsFieldGroupTerm83=ftsFieldGroupTerm();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_ftsFieldGroupTerm.add(ftsFieldGroupTerm83.getTree());
                    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:313:22: ( ( fuzzy )=> fuzzy )?
                    int alt27=2;
                    int LA27_0 = input.LA(1);

                    if ( (LA27_0==TILDA) ) {
                        int LA27_1 = input.LA(2);

                        if ( (LA27_1==DECIMAL_INTEGER_LITERAL||LA27_1==FLOATING_POINT_LITERAL) ) {
                            int LA27_3 = input.LA(3);

                            if ( (synpred11_FTS()) ) {
                                alt27=1;
                            }
                        }
                    }
                    switch (alt27) {
                        case 1 :
                            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:313:23: ( fuzzy )=> fuzzy
                            {
                            pushFollow(FOLLOW_fuzzy_in_ftsFieldGroupTest1307);
                            fuzzy84=fuzzy();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_fuzzy.add(fuzzy84.getTree());

                            }
                            break;

                    }

                    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:313:42: ( boost )?
                    int alt28=2;
                    int LA28_0 = input.LA(1);

                    if ( (LA28_0==CARAT) ) {
                        alt28=1;
                    }
                    switch (alt28) {
                        case 1 :
                            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:313:42: boost
                            {
                            pushFollow(FOLLOW_boost_in_ftsFieldGroupTest1311);
                            boost85=boost();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_boost.add(boost85.getTree());

                            }
                            break;

                    }



                    // AST REWRITE
                    // elements: fuzzy, boost, ftsFieldGroupTerm
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 314:3: -> ^( FG_TERM ftsFieldGroupTerm ( fuzzy )? ( boost )? )
                    {
                        // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:314:6: ^( FG_TERM ftsFieldGroupTerm ( fuzzy )? ( boost )? )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(FG_TERM, "FG_TERM"), root_1);

                        adaptor.addChild(root_1, stream_ftsFieldGroupTerm.nextTree());
                        // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:314:34: ( fuzzy )?
                        if ( stream_fuzzy.hasNext() ) {
                            adaptor.addChild(root_1, stream_fuzzy.nextTree());

                        }
                        stream_fuzzy.reset();
                        // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:314:41: ( boost )?
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
                    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:315:4: ftsFieldGroupExactTerm ( ( fuzzy )=> fuzzy )? ( boost )?
                    {
                    pushFollow(FOLLOW_ftsFieldGroupExactTerm_in_ftsFieldGroupTest1333);
                    ftsFieldGroupExactTerm86=ftsFieldGroupExactTerm();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_ftsFieldGroupExactTerm.add(ftsFieldGroupExactTerm86.getTree());
                    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:315:27: ( ( fuzzy )=> fuzzy )?
                    int alt29=2;
                    int LA29_0 = input.LA(1);

                    if ( (LA29_0==TILDA) ) {
                        int LA29_1 = input.LA(2);

                        if ( (LA29_1==DECIMAL_INTEGER_LITERAL||LA29_1==FLOATING_POINT_LITERAL) ) {
                            int LA29_3 = input.LA(3);

                            if ( (synpred12_FTS()) ) {
                                alt29=1;
                            }
                        }
                    }
                    switch (alt29) {
                        case 1 :
                            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:315:28: ( fuzzy )=> fuzzy
                            {
                            pushFollow(FOLLOW_fuzzy_in_ftsFieldGroupTest1342);
                            fuzzy87=fuzzy();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_fuzzy.add(fuzzy87.getTree());

                            }
                            break;

                    }

                    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:315:47: ( boost )?
                    int alt30=2;
                    int LA30_0 = input.LA(1);

                    if ( (LA30_0==CARAT) ) {
                        alt30=1;
                    }
                    switch (alt30) {
                        case 1 :
                            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:315:47: boost
                            {
                            pushFollow(FOLLOW_boost_in_ftsFieldGroupTest1346);
                            boost88=boost();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_boost.add(boost88.getTree());

                            }
                            break;

                    }



                    // AST REWRITE
                    // elements: boost, ftsFieldGroupExactTerm, fuzzy
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 316:3: -> ^( FG_EXACT_TERM ftsFieldGroupExactTerm ( fuzzy )? ( boost )? )
                    {
                        // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:316:6: ^( FG_EXACT_TERM ftsFieldGroupExactTerm ( fuzzy )? ( boost )? )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(FG_EXACT_TERM, "FG_EXACT_TERM"), root_1);

                        adaptor.addChild(root_1, stream_ftsFieldGroupExactTerm.nextTree());
                        // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:316:45: ( fuzzy )?
                        if ( stream_fuzzy.hasNext() ) {
                            adaptor.addChild(root_1, stream_fuzzy.nextTree());

                        }
                        stream_fuzzy.reset();
                        // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:316:52: ( boost )?
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
                    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:317:4: ftsFieldGroupPhrase ( ( fuzzy )=> fuzzy )? ( boost )?
                    {
                    pushFollow(FOLLOW_ftsFieldGroupPhrase_in_ftsFieldGroupTest1368);
                    ftsFieldGroupPhrase89=ftsFieldGroupPhrase();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_ftsFieldGroupPhrase.add(ftsFieldGroupPhrase89.getTree());
                    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:317:24: ( ( fuzzy )=> fuzzy )?
                    int alt31=2;
                    int LA31_0 = input.LA(1);

                    if ( (LA31_0==TILDA) ) {
                        int LA31_1 = input.LA(2);

                        if ( (LA31_1==DECIMAL_INTEGER_LITERAL||LA31_1==FLOATING_POINT_LITERAL) ) {
                            int LA31_3 = input.LA(3);

                            if ( (synpred13_FTS()) ) {
                                alt31=1;
                            }
                        }
                    }
                    switch (alt31) {
                        case 1 :
                            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:317:25: ( fuzzy )=> fuzzy
                            {
                            pushFollow(FOLLOW_fuzzy_in_ftsFieldGroupTest1377);
                            fuzzy90=fuzzy();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_fuzzy.add(fuzzy90.getTree());

                            }
                            break;

                    }

                    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:317:44: ( boost )?
                    int alt32=2;
                    int LA32_0 = input.LA(1);

                    if ( (LA32_0==CARAT) ) {
                        alt32=1;
                    }
                    switch (alt32) {
                        case 1 :
                            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:317:44: boost
                            {
                            pushFollow(FOLLOW_boost_in_ftsFieldGroupTest1381);
                            boost91=boost();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_boost.add(boost91.getTree());

                            }
                            break;

                    }



                    // AST REWRITE
                    // elements: boost, fuzzy, ftsFieldGroupPhrase
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 318:3: -> ^( FG_PHRASE ftsFieldGroupPhrase ( fuzzy )? ( boost )? )
                    {
                        // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:318:6: ^( FG_PHRASE ftsFieldGroupPhrase ( fuzzy )? ( boost )? )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(FG_PHRASE, "FG_PHRASE"), root_1);

                        adaptor.addChild(root_1, stream_ftsFieldGroupPhrase.nextTree());
                        // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:318:38: ( fuzzy )?
                        if ( stream_fuzzy.hasNext() ) {
                            adaptor.addChild(root_1, stream_fuzzy.nextTree());

                        }
                        stream_fuzzy.reset();
                        // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:318:45: ( boost )?
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
                    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:319:4: ftsFieldGroupSynonym ( ( fuzzy )=> fuzzy )? ( boost )?
                    {
                    pushFollow(FOLLOW_ftsFieldGroupSynonym_in_ftsFieldGroupTest1403);
                    ftsFieldGroupSynonym92=ftsFieldGroupSynonym();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_ftsFieldGroupSynonym.add(ftsFieldGroupSynonym92.getTree());
                    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:319:25: ( ( fuzzy )=> fuzzy )?
                    int alt33=2;
                    int LA33_0 = input.LA(1);

                    if ( (LA33_0==TILDA) ) {
                        int LA33_1 = input.LA(2);

                        if ( (LA33_1==DECIMAL_INTEGER_LITERAL||LA33_1==FLOATING_POINT_LITERAL) ) {
                            int LA33_3 = input.LA(3);

                            if ( (synpred14_FTS()) ) {
                                alt33=1;
                            }
                        }
                    }
                    switch (alt33) {
                        case 1 :
                            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:319:26: ( fuzzy )=> fuzzy
                            {
                            pushFollow(FOLLOW_fuzzy_in_ftsFieldGroupTest1412);
                            fuzzy93=fuzzy();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_fuzzy.add(fuzzy93.getTree());

                            }
                            break;

                    }

                    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:319:45: ( boost )?
                    int alt34=2;
                    int LA34_0 = input.LA(1);

                    if ( (LA34_0==CARAT) ) {
                        alt34=1;
                    }
                    switch (alt34) {
                        case 1 :
                            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:319:45: boost
                            {
                            pushFollow(FOLLOW_boost_in_ftsFieldGroupTest1416);
                            boost94=boost();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_boost.add(boost94.getTree());

                            }
                            break;

                    }



                    // AST REWRITE
                    // elements: boost, ftsFieldGroupSynonym, fuzzy
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 320:3: -> ^( FG_SYNONYM ftsFieldGroupSynonym ( fuzzy )? ( boost )? )
                    {
                        // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:320:6: ^( FG_SYNONYM ftsFieldGroupSynonym ( fuzzy )? ( boost )? )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(FG_SYNONYM, "FG_SYNONYM"), root_1);

                        adaptor.addChild(root_1, stream_ftsFieldGroupSynonym.nextTree());
                        // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:320:40: ( fuzzy )?
                        if ( stream_fuzzy.hasNext() ) {
                            adaptor.addChild(root_1, stream_fuzzy.nextTree());

                        }
                        stream_fuzzy.reset();
                        // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:320:47: ( boost )?
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
                    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:321:5: ftsFieldGroupProximity
                    {
                    pushFollow(FOLLOW_ftsFieldGroupProximity_in_ftsFieldGroupTest1439);
                    ftsFieldGroupProximity95=ftsFieldGroupProximity();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_ftsFieldGroupProximity.add(ftsFieldGroupProximity95.getTree());


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
                    // 322:3: -> ^( FG_PROXIMITY ftsFieldGroupProximity )
                    {
                        // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:322:6: ^( FG_PROXIMITY ftsFieldGroupProximity )
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
                    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:323:8: ftsFieldGroupRange ( boost )?
                    {
                    pushFollow(FOLLOW_ftsFieldGroupRange_in_ftsFieldGroupTest1458);
                    ftsFieldGroupRange96=ftsFieldGroupRange();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_ftsFieldGroupRange.add(ftsFieldGroupRange96.getTree());
                    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:323:27: ( boost )?
                    int alt35=2;
                    int LA35_0 = input.LA(1);

                    if ( (LA35_0==CARAT) ) {
                        alt35=1;
                    }
                    switch (alt35) {
                        case 1 :
                            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:323:27: boost
                            {
                            pushFollow(FOLLOW_boost_in_ftsFieldGroupTest1460);
                            boost97=boost();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_boost.add(boost97.getTree());

                            }
                            break;

                    }



                    // AST REWRITE
                    // elements: ftsFieldGroupRange, boost
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 324:9: -> ^( FG_RANGE ftsFieldGroupRange ( boost )? )
                    {
                        // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:324:12: ^( FG_RANGE ftsFieldGroupRange ( boost )? )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(FG_RANGE, "FG_RANGE"), root_1);

                        adaptor.addChild(root_1, stream_ftsFieldGroupRange.nextTree());
                        // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:324:42: ( boost )?
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
                case 7 :
                    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:325:5: LPAREN ftsFieldGroupImplicitConjunctionOrDisjunction RPAREN ( boost )?
                    {
                    LPAREN98=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_ftsFieldGroupTest1486); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_LPAREN.add(LPAREN98);

                    pushFollow(FOLLOW_ftsFieldGroupImplicitConjunctionOrDisjunction_in_ftsFieldGroupTest1488);
                    ftsFieldGroupImplicitConjunctionOrDisjunction99=ftsFieldGroupImplicitConjunctionOrDisjunction();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_ftsFieldGroupImplicitConjunctionOrDisjunction.add(ftsFieldGroupImplicitConjunctionOrDisjunction99.getTree());
                    RPAREN100=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_ftsFieldGroupTest1490); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_RPAREN.add(RPAREN100);

                    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:325:65: ( boost )?
                    int alt36=2;
                    int LA36_0 = input.LA(1);

                    if ( (LA36_0==CARAT) ) {
                        alt36=1;
                    }
                    switch (alt36) {
                        case 1 :
                            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:325:65: boost
                            {
                            pushFollow(FOLLOW_boost_in_ftsFieldGroupTest1492);
                            boost101=boost();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_boost.add(boost101.getTree());

                            }
                            break;

                    }



                    // AST REWRITE
                    // elements: boost, ftsFieldGroupImplicitConjunctionOrDisjunction
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 326:3: -> ftsFieldGroupImplicitConjunctionOrDisjunction ( boost )?
                    {
                        adaptor.addChild(root_0, stream_ftsFieldGroupImplicitConjunctionOrDisjunction.nextTree());
                        // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:326:52: ( boost )?
                        if ( stream_boost.hasNext() ) {
                            adaptor.addChild(root_0, stream_boost.nextTree());

                        }
                        stream_boost.reset();

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
    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:329:1: ftsFieldGroupTerm : ftsWord ;
    public final FTSParser.ftsFieldGroupTerm_return ftsFieldGroupTerm() throws RecognitionException {
        FTSParser.ftsFieldGroupTerm_return retval = new FTSParser.ftsFieldGroupTerm_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        FTSParser.ftsWord_return ftsWord102 = null;



        try {
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:330:2: ( ftsWord )
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:330:4: ftsWord
            {
            root_0 = (Object)adaptor.nil();

            pushFollow(FOLLOW_ftsWord_in_ftsFieldGroupTerm1514);
            ftsWord102=ftsWord();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, ftsWord102.getTree());

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
    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:333:1: ftsFieldGroupExactTerm : EQUALS ftsFieldGroupTerm -> ftsFieldGroupTerm ;
    public final FTSParser.ftsFieldGroupExactTerm_return ftsFieldGroupExactTerm() throws RecognitionException {
        FTSParser.ftsFieldGroupExactTerm_return retval = new FTSParser.ftsFieldGroupExactTerm_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token EQUALS103=null;
        FTSParser.ftsFieldGroupTerm_return ftsFieldGroupTerm104 = null;


        Object EQUALS103_tree=null;
        RewriteRuleTokenStream stream_EQUALS=new RewriteRuleTokenStream(adaptor,"token EQUALS");
        RewriteRuleSubtreeStream stream_ftsFieldGroupTerm=new RewriteRuleSubtreeStream(adaptor,"rule ftsFieldGroupTerm");
        try {
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:334:2: ( EQUALS ftsFieldGroupTerm -> ftsFieldGroupTerm )
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:334:4: EQUALS ftsFieldGroupTerm
            {
            EQUALS103=(Token)match(input,EQUALS,FOLLOW_EQUALS_in_ftsFieldGroupExactTerm1526); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_EQUALS.add(EQUALS103);

            pushFollow(FOLLOW_ftsFieldGroupTerm_in_ftsFieldGroupExactTerm1528);
            ftsFieldGroupTerm104=ftsFieldGroupTerm();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_ftsFieldGroupTerm.add(ftsFieldGroupTerm104.getTree());


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
            // 335:3: -> ftsFieldGroupTerm
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
    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:338:1: ftsFieldGroupPhrase : FTSPHRASE ;
    public final FTSParser.ftsFieldGroupPhrase_return ftsFieldGroupPhrase() throws RecognitionException {
        FTSParser.ftsFieldGroupPhrase_return retval = new FTSParser.ftsFieldGroupPhrase_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token FTSPHRASE105=null;

        Object FTSPHRASE105_tree=null;

        try {
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:339:2: ( FTSPHRASE )
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:339:6: FTSPHRASE
            {
            root_0 = (Object)adaptor.nil();

            FTSPHRASE105=(Token)match(input,FTSPHRASE,FOLLOW_FTSPHRASE_in_ftsFieldGroupPhrase1548); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            FTSPHRASE105_tree = (Object)adaptor.create(FTSPHRASE105);
            adaptor.addChild(root_0, FTSPHRASE105_tree);
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
    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:342:1: ftsFieldGroupSynonym : TILDA ftsFieldGroupTerm -> ftsFieldGroupTerm ;
    public final FTSParser.ftsFieldGroupSynonym_return ftsFieldGroupSynonym() throws RecognitionException {
        FTSParser.ftsFieldGroupSynonym_return retval = new FTSParser.ftsFieldGroupSynonym_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token TILDA106=null;
        FTSParser.ftsFieldGroupTerm_return ftsFieldGroupTerm107 = null;


        Object TILDA106_tree=null;
        RewriteRuleTokenStream stream_TILDA=new RewriteRuleTokenStream(adaptor,"token TILDA");
        RewriteRuleSubtreeStream stream_ftsFieldGroupTerm=new RewriteRuleSubtreeStream(adaptor,"rule ftsFieldGroupTerm");
        try {
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:343:2: ( TILDA ftsFieldGroupTerm -> ftsFieldGroupTerm )
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:343:4: TILDA ftsFieldGroupTerm
            {
            TILDA106=(Token)match(input,TILDA,FOLLOW_TILDA_in_ftsFieldGroupSynonym1560); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_TILDA.add(TILDA106);

            pushFollow(FOLLOW_ftsFieldGroupTerm_in_ftsFieldGroupSynonym1562);
            ftsFieldGroupTerm107=ftsFieldGroupTerm();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_ftsFieldGroupTerm.add(ftsFieldGroupTerm107.getTree());


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
            // 344:3: -> ftsFieldGroupTerm
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
    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:347:1: ftsFieldGroupProximity : ftsFieldGroupTerm ( proximityGroup ftsFieldGroupTerm )+ -> ftsFieldGroupTerm ( proximityGroup ftsFieldGroupTerm )+ ;
    public final FTSParser.ftsFieldGroupProximity_return ftsFieldGroupProximity() throws RecognitionException {
        FTSParser.ftsFieldGroupProximity_return retval = new FTSParser.ftsFieldGroupProximity_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        FTSParser.ftsFieldGroupTerm_return ftsFieldGroupTerm108 = null;

        FTSParser.proximityGroup_return proximityGroup109 = null;

        FTSParser.ftsFieldGroupTerm_return ftsFieldGroupTerm110 = null;


        RewriteRuleSubtreeStream stream_ftsFieldGroupTerm=new RewriteRuleSubtreeStream(adaptor,"rule ftsFieldGroupTerm");
        RewriteRuleSubtreeStream stream_proximityGroup=new RewriteRuleSubtreeStream(adaptor,"rule proximityGroup");
        try {
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:348:2: ( ftsFieldGroupTerm ( proximityGroup ftsFieldGroupTerm )+ -> ftsFieldGroupTerm ( proximityGroup ftsFieldGroupTerm )+ )
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:348:4: ftsFieldGroupTerm ( proximityGroup ftsFieldGroupTerm )+
            {
            pushFollow(FOLLOW_ftsFieldGroupTerm_in_ftsFieldGroupProximity1580);
            ftsFieldGroupTerm108=ftsFieldGroupTerm();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_ftsFieldGroupTerm.add(ftsFieldGroupTerm108.getTree());
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:348:22: ( proximityGroup ftsFieldGroupTerm )+
            int cnt38=0;
            loop38:
            do {
                int alt38=2;
                int LA38_0 = input.LA(1);

                if ( (LA38_0==STAR) ) {
                    alt38=1;
                }


                switch (alt38) {
            	case 1 :
            	    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:348:23: proximityGroup ftsFieldGroupTerm
            	    {
            	    pushFollow(FOLLOW_proximityGroup_in_ftsFieldGroupProximity1583);
            	    proximityGroup109=proximityGroup();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_proximityGroup.add(proximityGroup109.getTree());
            	    pushFollow(FOLLOW_ftsFieldGroupTerm_in_ftsFieldGroupProximity1585);
            	    ftsFieldGroupTerm110=ftsFieldGroupTerm();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_ftsFieldGroupTerm.add(ftsFieldGroupTerm110.getTree());

            	    }
            	    break;

            	default :
            	    if ( cnt38 >= 1 ) break loop38;
            	    if (state.backtracking>0) {state.failed=true; return retval;}
                        EarlyExitException eee =
                            new EarlyExitException(38, input);
                        throw eee;
                }
                cnt38++;
            } while (true);



            // AST REWRITE
            // elements: ftsFieldGroupTerm, ftsFieldGroupTerm, proximityGroup
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 349:3: -> ftsFieldGroupTerm ( proximityGroup ftsFieldGroupTerm )+
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
    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:352:1: proximityGroup : STAR ( LPAREN ( DECIMAL_INTEGER_LITERAL )? RPAREN )? -> ^( PROXIMITY ( DECIMAL_INTEGER_LITERAL )? ) ;
    public final FTSParser.proximityGroup_return proximityGroup() throws RecognitionException {
        FTSParser.proximityGroup_return retval = new FTSParser.proximityGroup_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token STAR111=null;
        Token LPAREN112=null;
        Token DECIMAL_INTEGER_LITERAL113=null;
        Token RPAREN114=null;

        Object STAR111_tree=null;
        Object LPAREN112_tree=null;
        Object DECIMAL_INTEGER_LITERAL113_tree=null;
        Object RPAREN114_tree=null;
        RewriteRuleTokenStream stream_RPAREN=new RewriteRuleTokenStream(adaptor,"token RPAREN");
        RewriteRuleTokenStream stream_STAR=new RewriteRuleTokenStream(adaptor,"token STAR");
        RewriteRuleTokenStream stream_DECIMAL_INTEGER_LITERAL=new RewriteRuleTokenStream(adaptor,"token DECIMAL_INTEGER_LITERAL");
        RewriteRuleTokenStream stream_LPAREN=new RewriteRuleTokenStream(adaptor,"token LPAREN");

        try {
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:353:3: ( STAR ( LPAREN ( DECIMAL_INTEGER_LITERAL )? RPAREN )? -> ^( PROXIMITY ( DECIMAL_INTEGER_LITERAL )? ) )
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:353:5: STAR ( LPAREN ( DECIMAL_INTEGER_LITERAL )? RPAREN )?
            {
            STAR111=(Token)match(input,STAR,FOLLOW_STAR_in_proximityGroup1613); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_STAR.add(STAR111);

            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:353:10: ( LPAREN ( DECIMAL_INTEGER_LITERAL )? RPAREN )?
            int alt40=2;
            int LA40_0 = input.LA(1);

            if ( (LA40_0==LPAREN) ) {
                alt40=1;
            }
            switch (alt40) {
                case 1 :
                    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:353:12: LPAREN ( DECIMAL_INTEGER_LITERAL )? RPAREN
                    {
                    LPAREN112=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_proximityGroup1617); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_LPAREN.add(LPAREN112);

                    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:353:19: ( DECIMAL_INTEGER_LITERAL )?
                    int alt39=2;
                    int LA39_0 = input.LA(1);

                    if ( (LA39_0==DECIMAL_INTEGER_LITERAL) ) {
                        alt39=1;
                    }
                    switch (alt39) {
                        case 1 :
                            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:353:19: DECIMAL_INTEGER_LITERAL
                            {
                            DECIMAL_INTEGER_LITERAL113=(Token)match(input,DECIMAL_INTEGER_LITERAL,FOLLOW_DECIMAL_INTEGER_LITERAL_in_proximityGroup1619); if (state.failed) return retval; 
                            if ( state.backtracking==0 ) stream_DECIMAL_INTEGER_LITERAL.add(DECIMAL_INTEGER_LITERAL113);


                            }
                            break;

                    }

                    RPAREN114=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_proximityGroup1622); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_RPAREN.add(RPAREN114);


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
            // 354:3: -> ^( PROXIMITY ( DECIMAL_INTEGER_LITERAL )? )
            {
                // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:354:6: ^( PROXIMITY ( DECIMAL_INTEGER_LITERAL )? )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(PROXIMITY, "PROXIMITY"), root_1);

                // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:354:18: ( DECIMAL_INTEGER_LITERAL )?
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
    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:357:1: ftsFieldGroupRange : ( ftsRangeWord DOTDOT ftsRangeWord -> INCLUSIVE ftsRangeWord ftsRangeWord INCLUSIVE | range_left ftsRangeWord TO ftsRangeWord range_right -> range_left ftsRangeWord ftsRangeWord range_right );
    public final FTSParser.ftsFieldGroupRange_return ftsFieldGroupRange() throws RecognitionException {
        FTSParser.ftsFieldGroupRange_return retval = new FTSParser.ftsFieldGroupRange_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token DOTDOT116=null;
        Token TO120=null;
        FTSParser.ftsRangeWord_return ftsRangeWord115 = null;

        FTSParser.ftsRangeWord_return ftsRangeWord117 = null;

        FTSParser.range_left_return range_left118 = null;

        FTSParser.ftsRangeWord_return ftsRangeWord119 = null;

        FTSParser.ftsRangeWord_return ftsRangeWord121 = null;

        FTSParser.range_right_return range_right122 = null;


        Object DOTDOT116_tree=null;
        Object TO120_tree=null;
        RewriteRuleTokenStream stream_DOTDOT=new RewriteRuleTokenStream(adaptor,"token DOTDOT");
        RewriteRuleTokenStream stream_TO=new RewriteRuleTokenStream(adaptor,"token TO");
        RewriteRuleSubtreeStream stream_range_left=new RewriteRuleSubtreeStream(adaptor,"rule range_left");
        RewriteRuleSubtreeStream stream_range_right=new RewriteRuleSubtreeStream(adaptor,"rule range_right");
        RewriteRuleSubtreeStream stream_ftsRangeWord=new RewriteRuleSubtreeStream(adaptor,"rule ftsRangeWord");
        try {
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:358:9: ( ftsRangeWord DOTDOT ftsRangeWord -> INCLUSIVE ftsRangeWord ftsRangeWord INCLUSIVE | range_left ftsRangeWord TO ftsRangeWord range_right -> range_left ftsRangeWord ftsRangeWord range_right )
            int alt41=2;
            int LA41_0 = input.LA(1);

            if ( (LA41_0==FTSPHRASE||LA41_0==DECIMAL_INTEGER_LITERAL||(LA41_0>=ID && LA41_0<=FTSWORD)||LA41_0==FLOATING_POINT_LITERAL) ) {
                alt41=1;
            }
            else if ( ((LA41_0>=LSQUARE && LA41_0<=LT)) ) {
                alt41=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 41, 0, input);

                throw nvae;
            }
            switch (alt41) {
                case 1 :
                    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:358:11: ftsRangeWord DOTDOT ftsRangeWord
                    {
                    pushFollow(FOLLOW_ftsRangeWord_in_ftsFieldGroupRange1655);
                    ftsRangeWord115=ftsRangeWord();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_ftsRangeWord.add(ftsRangeWord115.getTree());
                    DOTDOT116=(Token)match(input,DOTDOT,FOLLOW_DOTDOT_in_ftsFieldGroupRange1657); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_DOTDOT.add(DOTDOT116);

                    pushFollow(FOLLOW_ftsRangeWord_in_ftsFieldGroupRange1659);
                    ftsRangeWord117=ftsRangeWord();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_ftsRangeWord.add(ftsRangeWord117.getTree());


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
                    // 359:3: -> INCLUSIVE ftsRangeWord ftsRangeWord INCLUSIVE
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
                    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:360:4: range_left ftsRangeWord TO ftsRangeWord range_right
                    {
                    pushFollow(FOLLOW_range_left_in_ftsFieldGroupRange1676);
                    range_left118=range_left();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_range_left.add(range_left118.getTree());
                    pushFollow(FOLLOW_ftsRangeWord_in_ftsFieldGroupRange1678);
                    ftsRangeWord119=ftsRangeWord();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_ftsRangeWord.add(ftsRangeWord119.getTree());
                    TO120=(Token)match(input,TO,FOLLOW_TO_in_ftsFieldGroupRange1680); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_TO.add(TO120);

                    pushFollow(FOLLOW_ftsRangeWord_in_ftsFieldGroupRange1682);
                    ftsRangeWord121=ftsRangeWord();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_ftsRangeWord.add(ftsRangeWord121.getTree());
                    pushFollow(FOLLOW_range_right_in_ftsFieldGroupRange1684);
                    range_right122=range_right();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_range_right.add(range_right122.getTree());


                    // AST REWRITE
                    // elements: range_right, ftsRangeWord, ftsRangeWord, range_left
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 361:3: -> range_left ftsRangeWord ftsRangeWord range_right
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
    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:364:1: range_left : ( LSQUARE -> INCLUSIVE | LT -> EXCLUSIVE );
    public final FTSParser.range_left_return range_left() throws RecognitionException {
        FTSParser.range_left_return retval = new FTSParser.range_left_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token LSQUARE123=null;
        Token LT124=null;

        Object LSQUARE123_tree=null;
        Object LT124_tree=null;
        RewriteRuleTokenStream stream_LT=new RewriteRuleTokenStream(adaptor,"token LT");
        RewriteRuleTokenStream stream_LSQUARE=new RewriteRuleTokenStream(adaptor,"token LSQUARE");

        try {
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:365:2: ( LSQUARE -> INCLUSIVE | LT -> EXCLUSIVE )
            int alt42=2;
            int LA42_0 = input.LA(1);

            if ( (LA42_0==LSQUARE) ) {
                alt42=1;
            }
            else if ( (LA42_0==LT) ) {
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
                    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:365:10: LSQUARE
                    {
                    LSQUARE123=(Token)match(input,LSQUARE,FOLLOW_LSQUARE_in_range_left1714); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_LSQUARE.add(LSQUARE123);



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
                    // 366:3: -> INCLUSIVE
                    {
                        adaptor.addChild(root_0, (Object)adaptor.create(INCLUSIVE, "INCLUSIVE"));

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:367:4: LT
                    {
                    LT124=(Token)match(input,LT,FOLLOW_LT_in_range_left1725); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_LT.add(LT124);



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
                    // 368:3: -> EXCLUSIVE
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
    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:371:1: range_right : ( RSQUARE -> INCLUSIVE | GT -> EXCLUSIVE );
    public final FTSParser.range_right_return range_right() throws RecognitionException {
        FTSParser.range_right_return retval = new FTSParser.range_right_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token RSQUARE125=null;
        Token GT126=null;

        Object RSQUARE125_tree=null;
        Object GT126_tree=null;
        RewriteRuleTokenStream stream_GT=new RewriteRuleTokenStream(adaptor,"token GT");
        RewriteRuleTokenStream stream_RSQUARE=new RewriteRuleTokenStream(adaptor,"token RSQUARE");

        try {
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:372:2: ( RSQUARE -> INCLUSIVE | GT -> EXCLUSIVE )
            int alt43=2;
            int LA43_0 = input.LA(1);

            if ( (LA43_0==RSQUARE) ) {
                alt43=1;
            }
            else if ( (LA43_0==GT) ) {
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
                    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:372:10: RSQUARE
                    {
                    RSQUARE125=(Token)match(input,RSQUARE,FOLLOW_RSQUARE_in_range_right1749); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_RSQUARE.add(RSQUARE125);



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
                    // 373:3: -> INCLUSIVE
                    {
                        adaptor.addChild(root_0, (Object)adaptor.create(INCLUSIVE, "INCLUSIVE"));

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:374:4: GT
                    {
                    GT126=(Token)match(input,GT,FOLLOW_GT_in_range_right1760); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_GT.add(GT126);



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
                    // 375:3: -> EXCLUSIVE
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
    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:379:1: fieldReference : ( AT )? ( prefix | uri )? identifier -> ^( FIELD_REF identifier ( prefix )? ( uri )? ) ;
    public final FTSParser.fieldReference_return fieldReference() throws RecognitionException {
        FTSParser.fieldReference_return retval = new FTSParser.fieldReference_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token AT127=null;
        FTSParser.prefix_return prefix128 = null;

        FTSParser.uri_return uri129 = null;

        FTSParser.identifier_return identifier130 = null;


        Object AT127_tree=null;
        RewriteRuleTokenStream stream_AT=new RewriteRuleTokenStream(adaptor,"token AT");
        RewriteRuleSubtreeStream stream_prefix=new RewriteRuleSubtreeStream(adaptor,"rule prefix");
        RewriteRuleSubtreeStream stream_uri=new RewriteRuleSubtreeStream(adaptor,"rule uri");
        RewriteRuleSubtreeStream stream_identifier=new RewriteRuleSubtreeStream(adaptor,"rule identifier");
        try {
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:380:2: ( ( AT )? ( prefix | uri )? identifier -> ^( FIELD_REF identifier ( prefix )? ( uri )? ) )
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:380:4: ( AT )? ( prefix | uri )? identifier
            {
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:380:4: ( AT )?
            int alt44=2;
            int LA44_0 = input.LA(1);

            if ( (LA44_0==AT) ) {
                alt44=1;
            }
            switch (alt44) {
                case 1 :
                    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:380:4: AT
                    {
                    AT127=(Token)match(input,AT,FOLLOW_AT_in_fieldReference1781); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_AT.add(AT127);


                    }
                    break;

            }

            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:380:8: ( prefix | uri )?
            int alt45=3;
            int LA45_0 = input.LA(1);

            if ( (LA45_0==ID) ) {
                int LA45_1 = input.LA(2);

                if ( (LA45_1==COLON) ) {
                    int LA45_3 = input.LA(3);

                    if ( (LA45_3==ID) ) {
                        int LA45_5 = input.LA(4);

                        if ( (LA45_5==COLON) ) {
                            alt45=1;
                        }
                    }
                }
            }
            else if ( (LA45_0==URI) ) {
                alt45=2;
            }
            switch (alt45) {
                case 1 :
                    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:380:9: prefix
                    {
                    pushFollow(FOLLOW_prefix_in_fieldReference1785);
                    prefix128=prefix();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_prefix.add(prefix128.getTree());

                    }
                    break;
                case 2 :
                    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:380:16: uri
                    {
                    pushFollow(FOLLOW_uri_in_fieldReference1787);
                    uri129=uri();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_uri.add(uri129.getTree());

                    }
                    break;

            }

            pushFollow(FOLLOW_identifier_in_fieldReference1791);
            identifier130=identifier();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_identifier.add(identifier130.getTree());


            // AST REWRITE
            // elements: uri, identifier, prefix
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 381:5: -> ^( FIELD_REF identifier ( prefix )? ( uri )? )
            {
                // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:381:8: ^( FIELD_REF identifier ( prefix )? ( uri )? )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(FIELD_REF, "FIELD_REF"), root_1);

                adaptor.addChild(root_1, stream_identifier.nextTree());
                // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:381:31: ( prefix )?
                if ( stream_prefix.hasNext() ) {
                    adaptor.addChild(root_1, stream_prefix.nextTree());

                }
                stream_prefix.reset();
                // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:381:39: ( uri )?
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

    public static class prefix_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "prefix"
    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:384:1: prefix : identifier COLON -> ^( PREFIX identifier ) ;
    public final FTSParser.prefix_return prefix() throws RecognitionException {
        FTSParser.prefix_return retval = new FTSParser.prefix_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token COLON132=null;
        FTSParser.identifier_return identifier131 = null;


        Object COLON132_tree=null;
        RewriteRuleTokenStream stream_COLON=new RewriteRuleTokenStream(adaptor,"token COLON");
        RewriteRuleSubtreeStream stream_identifier=new RewriteRuleSubtreeStream(adaptor,"rule identifier");
        try {
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:385:1: ( identifier COLON -> ^( PREFIX identifier ) )
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:385:3: identifier COLON
            {
            pushFollow(FOLLOW_identifier_in_prefix1820);
            identifier131=identifier();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_identifier.add(identifier131.getTree());
            COLON132=(Token)match(input,COLON,FOLLOW_COLON_in_prefix1822); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_COLON.add(COLON132);



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
            // 386:1: -> ^( PREFIX identifier )
            {
                // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:386:4: ^( PREFIX identifier )
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
    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:389:1: uri : URI -> ^( NAME_SPACE URI ) ;
    public final FTSParser.uri_return uri() throws RecognitionException {
        FTSParser.uri_return retval = new FTSParser.uri_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token URI133=null;

        Object URI133_tree=null;
        RewriteRuleTokenStream stream_URI=new RewriteRuleTokenStream(adaptor,"token URI");

        try {
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:390:3: ( URI -> ^( NAME_SPACE URI ) )
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:390:5: URI
            {
            URI133=(Token)match(input,URI,FOLLOW_URI_in_uri1843); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_URI.add(URI133);



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
            // 391:3: -> ^( NAME_SPACE URI )
            {
                // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:391:6: ^( NAME_SPACE URI )
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
    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:394:1: identifier : ID ;
    public final FTSParser.identifier_return identifier() throws RecognitionException {
        FTSParser.identifier_return retval = new FTSParser.identifier_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token ID134=null;

        Object ID134_tree=null;

        try {
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:395:2: ( ID )
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:395:4: ID
            {
            root_0 = (Object)adaptor.nil();

            ID134=(Token)match(input,ID,FOLLOW_ID_in_identifier1866); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            ID134_tree = (Object)adaptor.create(ID134);
            adaptor.addChild(root_0, ID134_tree);
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
    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:398:1: ftsWord : ( ID | FTSWORD | OR | AND | NOT | TO | DECIMAL_INTEGER_LITERAL | FLOATING_POINT_LITERAL );
    public final FTSParser.ftsWord_return ftsWord() throws RecognitionException {
        FTSParser.ftsWord_return retval = new FTSParser.ftsWord_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token set135=null;

        Object set135_tree=null;

        try {
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:399:5: ( ID | FTSWORD | OR | AND | NOT | TO | DECIMAL_INTEGER_LITERAL | FLOATING_POINT_LITERAL )
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:
            {
            root_0 = (Object)adaptor.nil();

            set135=(Token)input.LT(1);
            if ( input.LA(1)==DECIMAL_INTEGER_LITERAL||input.LA(1)==TO||(input.LA(1)>=ID && input.LA(1)<=FLOATING_POINT_LITERAL) ) {
                input.consume();
                if ( state.backtracking==0 ) adaptor.addChild(root_0, (Object)adaptor.create(set135));
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
    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:409:1: number : ( DECIMAL_INTEGER_LITERAL | FLOATING_POINT_LITERAL );
    public final FTSParser.number_return number() throws RecognitionException {
        FTSParser.number_return retval = new FTSParser.number_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token set136=null;

        Object set136_tree=null;

        try {
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:410:5: ( DECIMAL_INTEGER_LITERAL | FLOATING_POINT_LITERAL )
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:
            {
            root_0 = (Object)adaptor.nil();

            set136=(Token)input.LT(1);
            if ( input.LA(1)==DECIMAL_INTEGER_LITERAL||input.LA(1)==FLOATING_POINT_LITERAL ) {
                input.consume();
                if ( state.backtracking==0 ) adaptor.addChild(root_0, (Object)adaptor.create(set136));
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
    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:414:1: ftsRangeWord : ( ID | FTSWORD | FTSPHRASE | DECIMAL_INTEGER_LITERAL | FLOATING_POINT_LITERAL );
    public final FTSParser.ftsRangeWord_return ftsRangeWord() throws RecognitionException {
        FTSParser.ftsRangeWord_return retval = new FTSParser.ftsRangeWord_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token set137=null;

        Object set137_tree=null;

        try {
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:415:5: ( ID | FTSWORD | FTSPHRASE | DECIMAL_INTEGER_LITERAL | FLOATING_POINT_LITERAL )
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:
            {
            root_0 = (Object)adaptor.nil();

            set137=(Token)input.LT(1);
            if ( input.LA(1)==FTSPHRASE||input.LA(1)==DECIMAL_INTEGER_LITERAL||(input.LA(1)>=ID && input.LA(1)<=FTSWORD)||input.LA(1)==FLOATING_POINT_LITERAL ) {
                input.consume();
                if ( state.backtracking==0 ) adaptor.addChild(root_0, (Object)adaptor.create(set137));
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
    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:422:1: or : ( OR | BAR BAR );
    public final FTSParser.or_return or() throws RecognitionException {
        FTSParser.or_return retval = new FTSParser.or_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token OR138=null;
        Token BAR139=null;
        Token BAR140=null;

        Object OR138_tree=null;
        Object BAR139_tree=null;
        Object BAR140_tree=null;

        try {
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:423:5: ( OR | BAR BAR )
            int alt46=2;
            int LA46_0 = input.LA(1);

            if ( (LA46_0==OR) ) {
                alt46=1;
            }
            else if ( (LA46_0==BAR) ) {
                alt46=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 46, 0, input);

                throw nvae;
            }
            switch (alt46) {
                case 1 :
                    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:423:9: OR
                    {
                    root_0 = (Object)adaptor.nil();

                    OR138=(Token)match(input,OR,FOLLOW_OR_in_or2070); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    OR138_tree = (Object)adaptor.create(OR138);
                    adaptor.addChild(root_0, OR138_tree);
                    }

                    }
                    break;
                case 2 :
                    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:424:7: BAR BAR
                    {
                    root_0 = (Object)adaptor.nil();

                    BAR139=(Token)match(input,BAR,FOLLOW_BAR_in_or2078); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    BAR139_tree = (Object)adaptor.create(BAR139);
                    adaptor.addChild(root_0, BAR139_tree);
                    }
                    BAR140=(Token)match(input,BAR,FOLLOW_BAR_in_or2080); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    BAR140_tree = (Object)adaptor.create(BAR140);
                    adaptor.addChild(root_0, BAR140_tree);
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
    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:427:1: and : ( AND | AMP AMP );
    public final FTSParser.and_return and() throws RecognitionException {
        FTSParser.and_return retval = new FTSParser.and_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token AND141=null;
        Token AMP142=null;
        Token AMP143=null;

        Object AND141_tree=null;
        Object AMP142_tree=null;
        Object AMP143_tree=null;

        try {
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:428:5: ( AND | AMP AMP )
            int alt47=2;
            int LA47_0 = input.LA(1);

            if ( (LA47_0==AND) ) {
                alt47=1;
            }
            else if ( (LA47_0==AMP) ) {
                alt47=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 47, 0, input);

                throw nvae;
            }
            switch (alt47) {
                case 1 :
                    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:428:7: AND
                    {
                    root_0 = (Object)adaptor.nil();

                    AND141=(Token)match(input,AND,FOLLOW_AND_in_and2102); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    AND141_tree = (Object)adaptor.create(AND141);
                    adaptor.addChild(root_0, AND141_tree);
                    }

                    }
                    break;
                case 2 :
                    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:429:7: AMP AMP
                    {
                    root_0 = (Object)adaptor.nil();

                    AMP142=(Token)match(input,AMP,FOLLOW_AMP_in_and2110); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    AMP142_tree = (Object)adaptor.create(AMP142);
                    adaptor.addChild(root_0, AMP142_tree);
                    }
                    AMP143=(Token)match(input,AMP,FOLLOW_AMP_in_and2112); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    AMP143_tree = (Object)adaptor.create(AMP143);
                    adaptor.addChild(root_0, AMP143_tree);
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
    // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:432:1: not : ( NOT | EXCLAMATION );
    public final FTSParser.not_return not() throws RecognitionException {
        FTSParser.not_return retval = new FTSParser.not_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token set144=null;

        Object set144_tree=null;

        try {
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:433:5: ( NOT | EXCLAMATION )
            // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:
            {
            root_0 = (Object)adaptor.nil();

            set144=(Token)input.LT(1);
            if ( input.LA(1)==NOT||input.LA(1)==EXCLAMATION ) {
                input.consume();
                if ( state.backtracking==0 ) adaptor.addChild(root_0, (Object)adaptor.create(set144));
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
        // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:198:27: ( or )
        // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:198:28: or
        {
        pushFollow(FOLLOW_or_in_synpred1_FTS318);
        or();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred1_FTS

    // $ANTLR start synpred2_FTS
    public final void synpred2_FTS_fragment() throws RecognitionException {   
        // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:203:17: ( and )
        // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:203:18: and
        {
        pushFollow(FOLLOW_and_in_synpred2_FTS354);
        and();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred2_FTS

    // $ANTLR start synpred3_FTS
    public final void synpred3_FTS_fragment() throws RecognitionException {   
        // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:209:7: ( not )
        // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:209:8: not
        {
        pushFollow(FOLLOW_not_in_synpred3_FTS395);
        not();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred3_FTS

    // $ANTLR start synpred4_FTS
    public final void synpred4_FTS_fragment() throws RecognitionException {   
        // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:222:16: ( fuzzy )
        // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:222:17: fuzzy
        {
        pushFollow(FOLLOW_fuzzy_in_synpred4_FTS560);
        fuzzy();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred4_FTS

    // $ANTLR start synpred5_FTS
    public final void synpred5_FTS_fragment() throws RecognitionException {   
        // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:224:18: ( fuzzy )
        // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:224:19: fuzzy
        {
        pushFollow(FOLLOW_fuzzy_in_synpred5_FTS595);
        fuzzy();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred5_FTS

    // $ANTLR start synpred6_FTS
    public final void synpred6_FTS_fragment() throws RecognitionException {   
        // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:226:20: ( fuzzy )
        // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:226:21: fuzzy
        {
        pushFollow(FOLLOW_fuzzy_in_synpred6_FTS635);
        fuzzy();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred6_FTS

    // $ANTLR start synpred7_FTS
    public final void synpred7_FTS_fragment() throws RecognitionException {   
        // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:228:21: ( fuzzy )
        // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:228:22: fuzzy
        {
        pushFollow(FOLLOW_fuzzy_in_synpred7_FTS681);
        fuzzy();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred7_FTS

    // $ANTLR start synpred8_FTS
    public final void synpred8_FTS_fragment() throws RecognitionException {   
        // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:288:37: ( or )
        // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:288:38: or
        {
        pushFollow(FOLLOW_or_in_synpred8_FTS1068);
        or();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred8_FTS

    // $ANTLR start synpred9_FTS
    public final void synpred9_FTS_fragment() throws RecognitionException {   
        // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:293:27: ( and )
        // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:293:28: and
        {
        pushFollow(FOLLOW_and_in_synpred9_FTS1104);
        and();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred9_FTS

    // $ANTLR start synpred10_FTS
    public final void synpred10_FTS_fragment() throws RecognitionException {   
        // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:299:3: ( not )
        // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:299:4: not
        {
        pushFollow(FOLLOW_not_in_synpred10_FTS1140);
        not();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred10_FTS

    // $ANTLR start synpred11_FTS
    public final void synpred11_FTS_fragment() throws RecognitionException {   
        // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:313:23: ( fuzzy )
        // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:313:24: fuzzy
        {
        pushFollow(FOLLOW_fuzzy_in_synpred11_FTS1302);
        fuzzy();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred11_FTS

    // $ANTLR start synpred12_FTS
    public final void synpred12_FTS_fragment() throws RecognitionException {   
        // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:315:28: ( fuzzy )
        // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:315:29: fuzzy
        {
        pushFollow(FOLLOW_fuzzy_in_synpred12_FTS1337);
        fuzzy();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred12_FTS

    // $ANTLR start synpred13_FTS
    public final void synpred13_FTS_fragment() throws RecognitionException {   
        // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:317:25: ( fuzzy )
        // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:317:26: fuzzy
        {
        pushFollow(FOLLOW_fuzzy_in_synpred13_FTS1372);
        fuzzy();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred13_FTS

    // $ANTLR start synpred14_FTS
    public final void synpred14_FTS_fragment() throws RecognitionException {   
        // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:319:26: ( fuzzy )
        // W:\\alfresco\\HEAD\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:319:27: fuzzy
        {
        pushFollow(FOLLOW_fuzzy_in_synpred14_FTS1407);
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
    protected DFA6 dfa6 = new DFA6(this);
    protected DFA17 dfa17 = new DFA17(this);
    protected DFA23 dfa23 = new DFA23(this);
    protected DFA24 dfa24 = new DFA24(this);
    protected DFA25 dfa25 = new DFA25(this);
    protected DFA26 dfa26 = new DFA26(this);
    protected DFA37 dfa37 = new DFA37(this);
    static final String DFA3_eotS =
        "\23\uffff";
    static final String DFA3_eofS =
        "\23\uffff";
    static final String DFA3_minS =
        "\1\50\20\0\2\uffff";
    static final String DFA3_maxS =
        "\1\103\20\0\2\uffff";
    static final String DFA3_acceptS =
        "\21\uffff\1\1\1\2";
    static final String DFA3_specialS =
        "\1\uffff\1\0\1\1\1\2\1\3\1\4\1\5\1\6\1\7\1\10\1\11\1\12\1\13\1"+
        "\14\1\15\1\16\1\17\2\uffff}>";
    static final String[] DFA3_transitionS = {
            "\1\16\1\17\1\20\1\15\1\uffff\1\11\2\uffff\1\7\1\10\1\uffff"+
            "\1\6\1\uffff\1\12\1\13\1\14\2\uffff\1\2\1\4\1\3\1\6\2\12\1\1"+
            "\1\6\1\uffff\1\5",
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
            return "190:1: ftsImplicitConjunctionOrDisjunction : ({...}? ftsExplicitDisjunction ( ftsExplicitDisjunction )* -> ^( CONJUNCTION ( ftsExplicitDisjunction )+ ) | ftsExplicitDisjunction ( ftsExplicitDisjunction )* -> ^( DISJUNCTION ( ftsExplicitDisjunction )+ ) );";
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
                        if ( ((defaultConjunction())) ) {s = 17;}

                        else if ( (true) ) {s = 18;}

                         
                        input.seek(index3_1);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA3_2 = input.LA(1);

                         
                        int index3_2 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((defaultConjunction())) ) {s = 17;}

                        else if ( (true) ) {s = 18;}

                         
                        input.seek(index3_2);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA3_3 = input.LA(1);

                         
                        int index3_3 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((defaultConjunction())) ) {s = 17;}

                        else if ( (true) ) {s = 18;}

                         
                        input.seek(index3_3);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA3_4 = input.LA(1);

                         
                        int index3_4 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((defaultConjunction())) ) {s = 17;}

                        else if ( (true) ) {s = 18;}

                         
                        input.seek(index3_4);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA3_5 = input.LA(1);

                         
                        int index3_5 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((defaultConjunction())) ) {s = 17;}

                        else if ( (true) ) {s = 18;}

                         
                        input.seek(index3_5);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA3_6 = input.LA(1);

                         
                        int index3_6 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((defaultConjunction())) ) {s = 17;}

                        else if ( (true) ) {s = 18;}

                         
                        input.seek(index3_6);
                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA3_7 = input.LA(1);

                         
                        int index3_7 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((defaultConjunction())) ) {s = 17;}

                        else if ( (true) ) {s = 18;}

                         
                        input.seek(index3_7);
                        if ( s>=0 ) return s;
                        break;
                    case 7 : 
                        int LA3_8 = input.LA(1);

                         
                        int index3_8 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((defaultConjunction())) ) {s = 17;}

                        else if ( (true) ) {s = 18;}

                         
                        input.seek(index3_8);
                        if ( s>=0 ) return s;
                        break;
                    case 8 : 
                        int LA3_9 = input.LA(1);

                         
                        int index3_9 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((defaultConjunction())) ) {s = 17;}

                        else if ( (true) ) {s = 18;}

                         
                        input.seek(index3_9);
                        if ( s>=0 ) return s;
                        break;
                    case 9 : 
                        int LA3_10 = input.LA(1);

                         
                        int index3_10 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((defaultConjunction())) ) {s = 17;}

                        else if ( (true) ) {s = 18;}

                         
                        input.seek(index3_10);
                        if ( s>=0 ) return s;
                        break;
                    case 10 : 
                        int LA3_11 = input.LA(1);

                         
                        int index3_11 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((defaultConjunction())) ) {s = 17;}

                        else if ( (true) ) {s = 18;}

                         
                        input.seek(index3_11);
                        if ( s>=0 ) return s;
                        break;
                    case 11 : 
                        int LA3_12 = input.LA(1);

                         
                        int index3_12 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((defaultConjunction())) ) {s = 17;}

                        else if ( (true) ) {s = 18;}

                         
                        input.seek(index3_12);
                        if ( s>=0 ) return s;
                        break;
                    case 12 : 
                        int LA3_13 = input.LA(1);

                         
                        int index3_13 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((defaultConjunction())) ) {s = 17;}

                        else if ( (true) ) {s = 18;}

                         
                        input.seek(index3_13);
                        if ( s>=0 ) return s;
                        break;
                    case 13 : 
                        int LA3_14 = input.LA(1);

                         
                        int index3_14 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((defaultConjunction())) ) {s = 17;}

                        else if ( (true) ) {s = 18;}

                         
                        input.seek(index3_14);
                        if ( s>=0 ) return s;
                        break;
                    case 14 : 
                        int LA3_15 = input.LA(1);

                         
                        int index3_15 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((defaultConjunction())) ) {s = 17;}

                        else if ( (true) ) {s = 18;}

                         
                        input.seek(index3_15);
                        if ( s>=0 ) return s;
                        break;
                    case 15 : 
                        int LA3_16 = input.LA(1);

                         
                        int index3_16 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((defaultConjunction())) ) {s = 17;}

                        else if ( (true) ) {s = 18;}

                         
                        input.seek(index3_16);
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
        "\25\uffff";
    static final String DFA4_eofS =
        "\1\1\24\uffff";
    static final String DFA4_minS =
        "\1\50\11\uffff\1\0\4\uffff\1\0\5\uffff";
    static final String DFA4_maxS =
        "\1\103\11\uffff\1\0\4\uffff\1\0\5\uffff";
    static final String DFA4_acceptS =
        "\1\uffff\1\2\22\uffff\1\1";
    static final String DFA4_specialS =
        "\12\uffff\1\0\4\uffff\1\1\5\uffff}>";
    static final String[] DFA4_transitionS = {
            "\1\1\1\17\4\1\2\uffff\2\1\1\uffff\1\1\1\uffff\3\1\2\uffff\4"+
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
            return "()* loopback of 198:26: ( ( or )=> or ftsExplictConjunction )*";
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
                        if ( (synpred1_FTS()) ) {s = 20;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index4_10);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA4_15 = input.LA(1);

                         
                        int index4_15 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_FTS()) ) {s = 20;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index4_15);
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
        "\26\uffff";
    static final String DFA5_eofS =
        "\1\1\25\uffff";
    static final String DFA5_minS =
        "\1\50\13\uffff\1\0\11\uffff";
    static final String DFA5_maxS =
        "\1\103\13\uffff\1\0\11\uffff";
    static final String DFA5_acceptS =
        "\1\uffff\1\2\23\uffff\1\1";
    static final String DFA5_specialS =
        "\1\0\13\uffff\1\1\11\uffff}>";
    static final String[] DFA5_transitionS = {
            "\6\1\2\uffff\2\1\1\uffff\1\1\1\uffff\3\1\2\uffff\5\1\1\14\2"+
            "\1\1\25\1\1",
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
            return "()* loopback of 203:16: ( ( and )=> and ftsPrefixed )*";
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
                        if ( (LA5_0==EOF||(LA5_0>=PLUS && LA5_0<=TILDA)||(LA5_0>=EQUALS && LA5_0<=FTSPHRASE)||LA5_0==DECIMAL_INTEGER_LITERAL||(LA5_0>=TO && LA5_0<=LT)||(LA5_0>=AT && LA5_0<=OR)||(LA5_0>=NOT && LA5_0<=FLOATING_POINT_LITERAL)||LA5_0==EXCLAMATION) ) {s = 1;}

                        else if ( (LA5_0==AND) ) {s = 12;}

                        else if ( (LA5_0==AMP) && (synpred2_FTS())) {s = 21;}

                         
                        input.seek(index5_0);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA5_12 = input.LA(1);

                         
                        int index5_12 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_FTS()) ) {s = 21;}

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
    static final String DFA6_eotS =
        "\21\uffff";
    static final String DFA6_eofS =
        "\21\uffff";
    static final String DFA6_minS =
        "\1\50\1\0\17\uffff";
    static final String DFA6_maxS =
        "\1\103\1\0\17\uffff";
    static final String DFA6_acceptS =
        "\2\uffff\1\2\2\uffff\1\1\10\uffff\1\3\1\4\1\5";
    static final String DFA6_specialS =
        "\1\0\1\1\17\uffff}>";
    static final String[] DFA6_transitionS = {
            "\1\16\1\17\1\20\1\2\1\uffff\1\2\2\uffff\2\2\1\uffff\1\2\1\uffff"+
            "\3\2\2\uffff\6\2\1\1\1\2\1\uffff\1\5",
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
            return "208:1: ftsPrefixed : ( ( not )=> not ftsTest -> ^( NEGATION ftsTest ) | ftsTest -> ^( DEFAULT ftsTest ) | PLUS ftsTest -> ^( MANDATORY ftsTest ) | BAR ftsTest -> ^( OPTIONAL ftsTest ) | MINUS ftsTest -> ^( EXCLUDE ftsTest ) );";
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

                        else if ( (LA6_0==LPAREN||LA6_0==TILDA||(LA6_0>=EQUALS && LA6_0<=FTSPHRASE)||LA6_0==DECIMAL_INTEGER_LITERAL||(LA6_0>=TO && LA6_0<=LT)||(LA6_0>=AT && LA6_0<=AND)||LA6_0==FLOATING_POINT_LITERAL) ) {s = 2;}

                        else if ( (LA6_0==EXCLAMATION) && (synpred3_FTS())) {s = 5;}

                        else if ( (LA6_0==PLUS) ) {s = 14;}

                        else if ( (LA6_0==BAR) ) {s = 15;}

                        else if ( (LA6_0==MINUS) ) {s = 16;}

                         
                        input.seek(index6_0);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA6_1 = input.LA(1);

                         
                        int index6_1 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred3_FTS()) ) {s = 5;}

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
    static final String DFA17_eotS =
        "\25\uffff";
    static final String DFA17_eofS =
        "\2\uffff\1\15\1\uffff\1\15\1\uffff\1\20\1\uffff\1\15\11\uffff\2"+
        "\15\1\uffff";
    static final String DFA17_minS =
        "\1\53\1\73\1\50\1\74\1\50\1\uffff\1\50\1\uffff\1\50\2\uffff\1\57"+
        "\1\53\2\uffff\1\57\2\uffff\2\50\1\53";
    static final String DFA17_maxS =
        "\1\101\1\74\1\103\1\74\1\103\1\uffff\1\103\1\uffff\1\103\2\uffff"+
        "\1\57\1\101\2\uffff\1\57\2\uffff\2\103\1\101";
    static final String DFA17_acceptS =
        "\5\uffff\1\2\1\uffff\1\4\1\uffff\1\6\1\10\2\uffff\1\1\1\5\1\uffff"+
        "\1\3\1\7\3\uffff";
    static final String DFA17_specialS =
        "\25\uffff}>";
    static final String[] DFA17_transitionS = {
            "\1\12\1\uffff\1\7\2\uffff\1\5\1\6\1\uffff\1\4\1\uffff\1\10"+
            "\2\11\2\uffff\1\1\1\3\1\2\1\4\3\10\1\4",
            "\1\3\1\13",
            "\7\15\1\14\2\15\1\16\1\15\1\11\3\15\2\uffff\12\15",
            "\1\17",
            "\7\15\1\uffff\2\15\1\16\1\15\1\11\3\15\2\uffff\12\15",
            "",
            "\7\20\1\uffff\2\20\1\uffff\1\20\1\11\3\20\2\uffff\12\20",
            "",
            "\7\15\1\uffff\2\15\1\16\1\15\1\uffff\3\15\2\uffff\12\15",
            "",
            "",
            "\1\14",
            "\1\21\5\uffff\1\6\1\uffff\1\23\1\uffff\1\15\2\11\4\uffff\1"+
            "\22\1\23\3\15\1\23",
            "",
            "",
            "\1\24",
            "",
            "",
            "\7\15\1\24\2\15\1\uffff\1\15\1\11\3\15\2\uffff\12\15",
            "\7\15\1\uffff\2\15\1\uffff\1\15\1\11\3\15\2\uffff\12\15",
            "\1\21\5\uffff\1\6\1\uffff\1\23\1\uffff\1\15\2\11\4\uffff\2"+
            "\23\3\15\1\23"
    };

    static final short[] DFA17_eot = DFA.unpackEncodedString(DFA17_eotS);
    static final short[] DFA17_eof = DFA.unpackEncodedString(DFA17_eofS);
    static final char[] DFA17_min = DFA.unpackEncodedStringToUnsignedChars(DFA17_minS);
    static final char[] DFA17_max = DFA.unpackEncodedStringToUnsignedChars(DFA17_maxS);
    static final short[] DFA17_accept = DFA.unpackEncodedString(DFA17_acceptS);
    static final short[] DFA17_special = DFA.unpackEncodedString(DFA17_specialS);
    static final short[][] DFA17_transition;

    static {
        int numStates = DFA17_transitionS.length;
        DFA17_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA17_transition[i] = DFA.unpackEncodedString(DFA17_transitionS[i]);
        }
    }

    class DFA17 extends DFA {

        public DFA17(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 17;
            this.eot = DFA17_eot;
            this.eof = DFA17_eof;
            this.min = DFA17_min;
            this.max = DFA17_max;
            this.accept = DFA17_accept;
            this.special = DFA17_special;
            this.transition = DFA17_transition;
        }
        public String getDescription() {
            return "221:1: ftsTest : ( ftsTerm ( ( fuzzy )=> fuzzy )? ( boost )? -> ^( TERM ftsTerm ( fuzzy )? ( boost )? ) | ftsExactTerm ( ( fuzzy )=> fuzzy )? ( boost )? -> ^( EXACT_TERM ftsExactTerm ( fuzzy )? ( boost )? ) | ftsPhrase ( ( fuzzy )=> fuzzy )? ( boost )? -> ^( PHRASE ftsPhrase ( fuzzy )? ( boost )? ) | ftsSynonym ( ( fuzzy )=> fuzzy )? ( boost )? -> ^( SYNONYM ftsSynonym ( fuzzy )? ( boost )? ) | ftsFieldGroupProximity -> ^( PROXIMITY ftsFieldGroupProximity ) | ftsRange ( boost )? -> ^( RANGE ftsRange ( boost )? ) | ftsFieldGroup -> ftsFieldGroup | LPAREN ftsImplicitConjunctionOrDisjunction RPAREN ( boost )? -> ftsImplicitConjunctionOrDisjunction ( boost )? );";
        }
    }
    static final String DFA23_eotS =
        "\20\uffff";
    static final String DFA23_eofS =
        "\20\uffff";
    static final String DFA23_minS =
        "\1\50\15\0\2\uffff";
    static final String DFA23_maxS =
        "\1\103\15\0\2\uffff";
    static final String DFA23_acceptS =
        "\16\uffff\1\1\1\2";
    static final String DFA23_specialS =
        "\1\uffff\1\0\1\1\1\2\1\3\1\4\1\5\1\6\1\7\1\10\1\11\1\12\1\13\1"+
        "\14\2\uffff}>";
    static final String[] DFA23_transitionS = {
            "\1\13\1\14\1\15\1\12\1\uffff\1\6\2\uffff\1\4\1\5\1\uffff\1"+
            "\3\1\uffff\1\7\1\10\1\11\4\uffff\2\3\2\7\1\1\1\3\1\uffff\1\2",
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

    static final short[] DFA23_eot = DFA.unpackEncodedString(DFA23_eotS);
    static final short[] DFA23_eof = DFA.unpackEncodedString(DFA23_eofS);
    static final char[] DFA23_min = DFA.unpackEncodedStringToUnsignedChars(DFA23_minS);
    static final char[] DFA23_max = DFA.unpackEncodedStringToUnsignedChars(DFA23_maxS);
    static final short[] DFA23_accept = DFA.unpackEncodedString(DFA23_acceptS);
    static final short[] DFA23_special = DFA.unpackEncodedString(DFA23_specialS);
    static final short[][] DFA23_transition;

    static {
        int numStates = DFA23_transitionS.length;
        DFA23_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA23_transition[i] = DFA.unpackEncodedString(DFA23_transitionS[i]);
        }
    }

    class DFA23 extends DFA {

        public DFA23(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 23;
            this.eot = DFA23_eot;
            this.eof = DFA23_eof;
            this.min = DFA23_min;
            this.max = DFA23_max;
            this.accept = DFA23_accept;
            this.special = DFA23_special;
            this.transition = DFA23_transition;
        }
        public String getDescription() {
            return "280:1: ftsFieldGroupImplicitConjunctionOrDisjunction : ({...}? ftsFieldGroupExplicitDisjunction ( ftsFieldGroupExplicitDisjunction )* -> ^( FIELD_CONJUNCTION ( ftsFieldGroupExplicitDisjunction )+ ) | ftsFieldGroupExplicitDisjunction ( ftsFieldGroupExplicitDisjunction )* -> ^( FIELD_DISJUNCTION ( ftsFieldGroupExplicitDisjunction )+ ) );";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA23_1 = input.LA(1);

                         
                        int index23_1 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((defaultFieldConjunction())) ) {s = 14;}

                        else if ( (true) ) {s = 15;}

                         
                        input.seek(index23_1);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA23_2 = input.LA(1);

                         
                        int index23_2 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((defaultFieldConjunction())) ) {s = 14;}

                        else if ( (true) ) {s = 15;}

                         
                        input.seek(index23_2);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA23_3 = input.LA(1);

                         
                        int index23_3 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((defaultFieldConjunction())) ) {s = 14;}

                        else if ( (true) ) {s = 15;}

                         
                        input.seek(index23_3);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA23_4 = input.LA(1);

                         
                        int index23_4 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((defaultFieldConjunction())) ) {s = 14;}

                        else if ( (true) ) {s = 15;}

                         
                        input.seek(index23_4);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA23_5 = input.LA(1);

                         
                        int index23_5 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((defaultFieldConjunction())) ) {s = 14;}

                        else if ( (true) ) {s = 15;}

                         
                        input.seek(index23_5);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA23_6 = input.LA(1);

                         
                        int index23_6 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((defaultFieldConjunction())) ) {s = 14;}

                        else if ( (true) ) {s = 15;}

                         
                        input.seek(index23_6);
                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA23_7 = input.LA(1);

                         
                        int index23_7 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((defaultFieldConjunction())) ) {s = 14;}

                        else if ( (true) ) {s = 15;}

                         
                        input.seek(index23_7);
                        if ( s>=0 ) return s;
                        break;
                    case 7 : 
                        int LA23_8 = input.LA(1);

                         
                        int index23_8 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((defaultFieldConjunction())) ) {s = 14;}

                        else if ( (true) ) {s = 15;}

                         
                        input.seek(index23_8);
                        if ( s>=0 ) return s;
                        break;
                    case 8 : 
                        int LA23_9 = input.LA(1);

                         
                        int index23_9 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((defaultFieldConjunction())) ) {s = 14;}

                        else if ( (true) ) {s = 15;}

                         
                        input.seek(index23_9);
                        if ( s>=0 ) return s;
                        break;
                    case 9 : 
                        int LA23_10 = input.LA(1);

                         
                        int index23_10 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((defaultFieldConjunction())) ) {s = 14;}

                        else if ( (true) ) {s = 15;}

                         
                        input.seek(index23_10);
                        if ( s>=0 ) return s;
                        break;
                    case 10 : 
                        int LA23_11 = input.LA(1);

                         
                        int index23_11 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((defaultFieldConjunction())) ) {s = 14;}

                        else if ( (true) ) {s = 15;}

                         
                        input.seek(index23_11);
                        if ( s>=0 ) return s;
                        break;
                    case 11 : 
                        int LA23_12 = input.LA(1);

                         
                        int index23_12 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((defaultFieldConjunction())) ) {s = 14;}

                        else if ( (true) ) {s = 15;}

                         
                        input.seek(index23_12);
                        if ( s>=0 ) return s;
                        break;
                    case 12 : 
                        int LA23_13 = input.LA(1);

                         
                        int index23_13 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((defaultFieldConjunction())) ) {s = 14;}

                        else if ( (true) ) {s = 15;}

                         
                        input.seek(index23_13);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 23, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA24_eotS =
        "\21\uffff";
    static final String DFA24_eofS =
        "\21\uffff";
    static final String DFA24_minS =
        "\1\50\6\uffff\1\0\4\uffff\1\0\4\uffff";
    static final String DFA24_maxS =
        "\1\103\6\uffff\1\0\4\uffff\1\0\4\uffff";
    static final String DFA24_acceptS =
        "\1\uffff\1\2\16\uffff\1\1";
    static final String DFA24_specialS =
        "\7\uffff\1\0\4\uffff\1\1\4\uffff}>";
    static final String[] DFA24_transitionS = {
            "\1\1\1\14\4\1\2\uffff\2\1\1\uffff\1\1\1\uffff\3\1\4\uffff\2"+
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

    static final short[] DFA24_eot = DFA.unpackEncodedString(DFA24_eotS);
    static final short[] DFA24_eof = DFA.unpackEncodedString(DFA24_eofS);
    static final char[] DFA24_min = DFA.unpackEncodedStringToUnsignedChars(DFA24_minS);
    static final char[] DFA24_max = DFA.unpackEncodedStringToUnsignedChars(DFA24_maxS);
    static final short[] DFA24_accept = DFA.unpackEncodedString(DFA24_acceptS);
    static final short[] DFA24_special = DFA.unpackEncodedString(DFA24_specialS);
    static final short[][] DFA24_transition;

    static {
        int numStates = DFA24_transitionS.length;
        DFA24_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA24_transition[i] = DFA.unpackEncodedString(DFA24_transitionS[i]);
        }
    }

    class DFA24 extends DFA {

        public DFA24(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 24;
            this.eot = DFA24_eot;
            this.eof = DFA24_eof;
            this.min = DFA24_min;
            this.max = DFA24_max;
            this.accept = DFA24_accept;
            this.special = DFA24_special;
            this.transition = DFA24_transition;
        }
        public String getDescription() {
            return "()* loopback of 288:36: ( ( or )=> or ftsFieldGroupExplictConjunction )*";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA24_7 = input.LA(1);

                         
                        int index24_7 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_FTS()) ) {s = 16;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index24_7);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA24_12 = input.LA(1);

                         
                        int index24_12 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_FTS()) ) {s = 16;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index24_12);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 24, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA25_eotS =
        "\22\uffff";
    static final String DFA25_eofS =
        "\22\uffff";
    static final String DFA25_minS =
        "\1\50\10\uffff\1\0\10\uffff";
    static final String DFA25_maxS =
        "\1\103\10\uffff\1\0\10\uffff";
    static final String DFA25_acceptS =
        "\1\uffff\1\2\17\uffff\1\1";
    static final String DFA25_specialS =
        "\1\0\10\uffff\1\1\10\uffff}>";
    static final String[] DFA25_transitionS = {
            "\6\1\2\uffff\2\1\1\uffff\1\1\1\uffff\3\1\4\uffff\3\1\1\11\2"+
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
            return "()* loopback of 293:26: ( ( and )=> and ftsFieldGroupPrefixed )*";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA25_0 = input.LA(1);

                         
                        int index25_0 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((LA25_0>=PLUS && LA25_0<=TILDA)||(LA25_0>=EQUALS && LA25_0<=FTSPHRASE)||LA25_0==DECIMAL_INTEGER_LITERAL||(LA25_0>=TO && LA25_0<=LT)||(LA25_0>=ID && LA25_0<=OR)||(LA25_0>=NOT && LA25_0<=FLOATING_POINT_LITERAL)||LA25_0==EXCLAMATION) ) {s = 1;}

                        else if ( (LA25_0==AND) ) {s = 9;}

                        else if ( (LA25_0==AMP) && (synpred9_FTS())) {s = 17;}

                         
                        input.seek(index25_0);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA25_9 = input.LA(1);

                         
                        int index25_9 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred9_FTS()) ) {s = 17;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index25_9);
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
        "\16\uffff";
    static final String DFA26_eofS =
        "\16\uffff";
    static final String DFA26_minS =
        "\1\50\1\0\14\uffff";
    static final String DFA26_maxS =
        "\1\103\1\0\14\uffff";
    static final String DFA26_acceptS =
        "\2\uffff\1\1\1\2\7\uffff\1\3\1\4\1\5";
    static final String DFA26_specialS =
        "\1\0\1\1\14\uffff}>";
    static final String[] DFA26_transitionS = {
            "\1\13\1\14\1\15\1\3\1\uffff\1\3\2\uffff\2\3\1\uffff\1\3\1\uffff"+
            "\3\3\4\uffff\4\3\1\1\1\3\1\uffff\1\2",
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
            return "298:1: ftsFieldGroupPrefixed : ( ( not )=> not ftsFieldGroupTest -> ^( FIELD_NEGATION ftsFieldGroupTest ) | ftsFieldGroupTest -> ^( FIELD_DEFAULT ftsFieldGroupTest ) | PLUS ftsFieldGroupTest -> ^( FIELD_MANDATORY ftsFieldGroupTest ) | BAR ftsFieldGroupTest -> ^( FIELD_OPTIONAL ftsFieldGroupTest ) | MINUS ftsFieldGroupTest -> ^( FIELD_EXCLUDE ftsFieldGroupTest ) );";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA26_0 = input.LA(1);

                         
                        int index26_0 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA26_0==NOT) ) {s = 1;}

                        else if ( (LA26_0==EXCLAMATION) && (synpred10_FTS())) {s = 2;}

                        else if ( (LA26_0==LPAREN||LA26_0==TILDA||(LA26_0>=EQUALS && LA26_0<=FTSPHRASE)||LA26_0==DECIMAL_INTEGER_LITERAL||(LA26_0>=TO && LA26_0<=LT)||(LA26_0>=ID && LA26_0<=AND)||LA26_0==FLOATING_POINT_LITERAL) ) {s = 3;}

                        else if ( (LA26_0==PLUS) ) {s = 11;}

                        else if ( (LA26_0==BAR) ) {s = 12;}

                        else if ( (LA26_0==MINUS) ) {s = 13;}

                         
                        input.seek(index26_0);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA26_1 = input.LA(1);

                         
                        int index26_1 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred10_FTS()) ) {s = 2;}

                        else if ( (true) ) {s = 3;}

                         
                        input.seek(index26_1);
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
    static final String DFA37_eotS =
        "\13\uffff";
    static final String DFA37_eofS =
        "\13\uffff";
    static final String DFA37_minS =
        "\1\53\1\50\1\uffff\1\50\1\uffff\1\50\5\uffff";
    static final String DFA37_maxS =
        "\1\101\1\103\1\uffff\1\103\1\uffff\1\103\5\uffff";
    static final String DFA37_acceptS =
        "\2\uffff\1\2\1\uffff\1\4\1\uffff\1\6\1\7\1\5\1\1\1\3";
    static final String DFA37_specialS =
        "\13\uffff}>";
    static final String[] DFA37_transitionS = {
            "\1\7\1\uffff\1\4\2\uffff\1\2\1\3\1\uffff\1\1\1\uffff\1\5\2"+
            "\6\4\uffff\2\1\3\5\1\1",
            "\7\11\1\uffff\2\11\1\10\1\11\1\6\3\11\4\uffff\10\11",
            "",
            "\7\12\1\uffff\2\12\1\uffff\1\12\1\6\3\12\4\uffff\10\12",
            "",
            "\7\11\1\uffff\2\11\1\10\1\11\1\uffff\3\11\4\uffff\10\11",
            "",
            "",
            "",
            "",
            ""
    };

    static final short[] DFA37_eot = DFA.unpackEncodedString(DFA37_eotS);
    static final short[] DFA37_eof = DFA.unpackEncodedString(DFA37_eofS);
    static final char[] DFA37_min = DFA.unpackEncodedStringToUnsignedChars(DFA37_minS);
    static final char[] DFA37_max = DFA.unpackEncodedStringToUnsignedChars(DFA37_maxS);
    static final short[] DFA37_accept = DFA.unpackEncodedString(DFA37_acceptS);
    static final short[] DFA37_special = DFA.unpackEncodedString(DFA37_specialS);
    static final short[][] DFA37_transition;

    static {
        int numStates = DFA37_transitionS.length;
        DFA37_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA37_transition[i] = DFA.unpackEncodedString(DFA37_transitionS[i]);
        }
    }

    class DFA37 extends DFA {

        public DFA37(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 37;
            this.eot = DFA37_eot;
            this.eof = DFA37_eof;
            this.min = DFA37_min;
            this.max = DFA37_max;
            this.accept = DFA37_accept;
            this.special = DFA37_special;
            this.transition = DFA37_transition;
        }
        public String getDescription() {
            return "312:1: ftsFieldGroupTest : ( ftsFieldGroupTerm ( ( fuzzy )=> fuzzy )? ( boost )? -> ^( FG_TERM ftsFieldGroupTerm ( fuzzy )? ( boost )? ) | ftsFieldGroupExactTerm ( ( fuzzy )=> fuzzy )? ( boost )? -> ^( FG_EXACT_TERM ftsFieldGroupExactTerm ( fuzzy )? ( boost )? ) | ftsFieldGroupPhrase ( ( fuzzy )=> fuzzy )? ( boost )? -> ^( FG_PHRASE ftsFieldGroupPhrase ( fuzzy )? ( boost )? ) | ftsFieldGroupSynonym ( ( fuzzy )=> fuzzy )? ( boost )? -> ^( FG_SYNONYM ftsFieldGroupSynonym ( fuzzy )? ( boost )? ) | ftsFieldGroupProximity -> ^( FG_PROXIMITY ftsFieldGroupProximity ) | ftsFieldGroupRange ( boost )? -> ^( FG_RANGE ftsFieldGroupRange ( boost )? ) | LPAREN ftsFieldGroupImplicitConjunctionOrDisjunction RPAREN ( boost )? -> ftsFieldGroupImplicitConjunctionOrDisjunction ( boost )? );";
        }
    }
 

    public static final BitSet FOLLOW_ftsImplicitConjunctionOrDisjunction_in_ftsQuery240 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_ftsQuery242 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ftsExplicitDisjunction_in_ftsImplicitConjunctionOrDisjunction264 = new BitSet(new long[]{0xFCEB2F0000000002L,0x000000000000000BL});
    public static final BitSet FOLLOW_ftsExplicitDisjunction_in_ftsImplicitConjunctionOrDisjunction267 = new BitSet(new long[]{0xFCEB2F0000000002L,0x000000000000000BL});
    public static final BitSet FOLLOW_ftsExplicitDisjunction_in_ftsImplicitConjunctionOrDisjunction286 = new BitSet(new long[]{0xFCEB2F0000000002L,0x000000000000000BL});
    public static final BitSet FOLLOW_ftsExplicitDisjunction_in_ftsImplicitConjunctionOrDisjunction289 = new BitSet(new long[]{0xFCEB2F0000000002L,0x000000000000000BL});
    public static final BitSet FOLLOW_ftsExplictConjunction_in_ftsExplicitDisjunction314 = new BitSet(new long[]{0x4000020000000002L});
    public static final BitSet FOLLOW_or_in_ftsExplicitDisjunction323 = new BitSet(new long[]{0xFCEB2F0000000000L,0x000000000000000BL});
    public static final BitSet FOLLOW_ftsExplictConjunction_in_ftsExplicitDisjunction325 = new BitSet(new long[]{0x4000020000000002L});
    public static final BitSet FOLLOW_ftsPrefixed_in_ftsExplictConjunction350 = new BitSet(new long[]{0x8000000000000002L,0x0000000000000004L});
    public static final BitSet FOLLOW_and_in_ftsExplictConjunction359 = new BitSet(new long[]{0xFCEB2F0000000000L,0x000000000000000BL});
    public static final BitSet FOLLOW_ftsPrefixed_in_ftsExplictConjunction361 = new BitSet(new long[]{0x8000000000000002L,0x0000000000000004L});
    public static final BitSet FOLLOW_not_in_ftsPrefixed400 = new BitSet(new long[]{0xFCEB280000000000L,0x0000000000000003L});
    public static final BitSet FOLLOW_ftsTest_in_ftsPrefixed402 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ftsTest_in_ftsPrefixed420 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_PLUS_in_ftsPrefixed440 = new BitSet(new long[]{0xFCEB280000000000L,0x0000000000000003L});
    public static final BitSet FOLLOW_ftsTest_in_ftsPrefixed442 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_BAR_in_ftsPrefixed476 = new BitSet(new long[]{0xFCEB280000000000L,0x0000000000000003L});
    public static final BitSet FOLLOW_ftsTest_in_ftsPrefixed478 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_MINUS_in_ftsPrefixed512 = new BitSet(new long[]{0xFCEB280000000000L,0x0000000000000003L});
    public static final BitSet FOLLOW_ftsTest_in_ftsPrefixed514 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ftsTerm_in_ftsTest556 = new BitSet(new long[]{0x0000600000000002L});
    public static final BitSet FOLLOW_fuzzy_in_ftsTest565 = new BitSet(new long[]{0x0000400000000002L});
    public static final BitSet FOLLOW_boost_in_ftsTest569 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ftsExactTerm_in_ftsTest591 = new BitSet(new long[]{0x0000600000000002L});
    public static final BitSet FOLLOW_fuzzy_in_ftsTest600 = new BitSet(new long[]{0x0000400000000002L});
    public static final BitSet FOLLOW_boost_in_ftsTest604 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ftsPhrase_in_ftsTest631 = new BitSet(new long[]{0x0000600000000002L});
    public static final BitSet FOLLOW_fuzzy_in_ftsTest640 = new BitSet(new long[]{0x0000400000000002L});
    public static final BitSet FOLLOW_boost_in_ftsTest644 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ftsSynonym_in_ftsTest677 = new BitSet(new long[]{0x0000600000000002L});
    public static final BitSet FOLLOW_fuzzy_in_ftsTest686 = new BitSet(new long[]{0x0000400000000002L});
    public static final BitSet FOLLOW_boost_in_ftsTest690 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ftsFieldGroupProximity_in_ftsTest721 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ftsRange_in_ftsTest747 = new BitSet(new long[]{0x0000400000000002L});
    public static final BitSet FOLLOW_boost_in_ftsTest749 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ftsFieldGroup_in_ftsTest777 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_ftsTest792 = new BitSet(new long[]{0xFCEB2F0000000000L,0x000000000000000BL});
    public static final BitSet FOLLOW_ftsImplicitConjunctionOrDisjunction_in_ftsTest794 = new BitSet(new long[]{0x0000100000000000L});
    public static final BitSet FOLLOW_RPAREN_in_ftsTest796 = new BitSet(new long[]{0x0000400000000002L});
    public static final BitSet FOLLOW_boost_in_ftsTest798 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TILDA_in_fuzzy818 = new BitSet(new long[]{0x0008000000000000L,0x0000000000000002L});
    public static final BitSet FOLLOW_number_in_fuzzy820 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_CARAT_in_boost837 = new BitSet(new long[]{0x0008000000000000L,0x0000000000000002L});
    public static final BitSet FOLLOW_number_in_boost839 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_fieldReference_in_ftsTerm858 = new BitSet(new long[]{0x0000800000000000L});
    public static final BitSet FOLLOW_COLON_in_ftsTerm860 = new BitSet(new long[]{0xFC28000000000000L,0x0000000000000003L});
    public static final BitSet FOLLOW_ftsWord_in_ftsTerm864 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_EQUALS_in_ftsExactTerm885 = new BitSet(new long[]{0xFC28000000000000L,0x0000000000000003L});
    public static final BitSet FOLLOW_ftsTerm_in_ftsExactTerm887 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_fieldReference_in_ftsPhrase908 = new BitSet(new long[]{0x0000800000000000L});
    public static final BitSet FOLLOW_COLON_in_ftsPhrase910 = new BitSet(new long[]{0x0002000000000000L});
    public static final BitSet FOLLOW_FTSPHRASE_in_ftsPhrase914 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TILDA_in_ftsSynonym935 = new BitSet(new long[]{0xFC28000000000000L,0x0000000000000003L});
    public static final BitSet FOLLOW_ftsTerm_in_ftsSynonym937 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_fieldReference_in_ftsRange955 = new BitSet(new long[]{0x0000800000000000L});
    public static final BitSet FOLLOW_COLON_in_ftsRange957 = new BitSet(new long[]{0x3CCA000000000000L,0x0000000000000002L});
    public static final BitSet FOLLOW_ftsFieldGroupRange_in_ftsRange961 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_fieldReference_in_ftsFieldGroup981 = new BitSet(new long[]{0x0000800000000000L});
    public static final BitSet FOLLOW_COLON_in_ftsFieldGroup983 = new BitSet(new long[]{0x0000080000000000L});
    public static final BitSet FOLLOW_LPAREN_in_ftsFieldGroup985 = new BitSet(new long[]{0xFCEB2F0000000000L,0x000000000000000BL});
    public static final BitSet FOLLOW_ftsFieldGroupImplicitConjunctionOrDisjunction_in_ftsFieldGroup987 = new BitSet(new long[]{0x0000100000000000L});
    public static final BitSet FOLLOW_RPAREN_in_ftsFieldGroup989 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ftsFieldGroupExplicitDisjunction_in_ftsFieldGroupImplicitConjunctionOrDisjunction1015 = new BitSet(new long[]{0xFCEB2F0000000002L,0x000000000000000BL});
    public static final BitSet FOLLOW_ftsFieldGroupExplicitDisjunction_in_ftsFieldGroupImplicitConjunctionOrDisjunction1018 = new BitSet(new long[]{0xFCEB2F0000000002L,0x000000000000000BL});
    public static final BitSet FOLLOW_ftsFieldGroupExplicitDisjunction_in_ftsFieldGroupImplicitConjunctionOrDisjunction1036 = new BitSet(new long[]{0xFCEB2F0000000002L,0x000000000000000BL});
    public static final BitSet FOLLOW_ftsFieldGroupExplicitDisjunction_in_ftsFieldGroupImplicitConjunctionOrDisjunction1039 = new BitSet(new long[]{0xFCEB2F0000000002L,0x000000000000000BL});
    public static final BitSet FOLLOW_ftsFieldGroupExplictConjunction_in_ftsFieldGroupExplicitDisjunction1064 = new BitSet(new long[]{0x4000020000000002L});
    public static final BitSet FOLLOW_or_in_ftsFieldGroupExplicitDisjunction1073 = new BitSet(new long[]{0xFCEB2F0000000000L,0x000000000000000BL});
    public static final BitSet FOLLOW_ftsFieldGroupExplictConjunction_in_ftsFieldGroupExplicitDisjunction1075 = new BitSet(new long[]{0x4000020000000002L});
    public static final BitSet FOLLOW_ftsFieldGroupPrefixed_in_ftsFieldGroupExplictConjunction1100 = new BitSet(new long[]{0x8000000000000002L,0x0000000000000004L});
    public static final BitSet FOLLOW_and_in_ftsFieldGroupExplictConjunction1109 = new BitSet(new long[]{0xFCEB2F0000000000L,0x000000000000000BL});
    public static final BitSet FOLLOW_ftsFieldGroupPrefixed_in_ftsFieldGroupExplictConjunction1111 = new BitSet(new long[]{0x8000000000000002L,0x0000000000000004L});
    public static final BitSet FOLLOW_not_in_ftsFieldGroupPrefixed1145 = new BitSet(new long[]{0xFCEB280000000000L,0x0000000000000003L});
    public static final BitSet FOLLOW_ftsFieldGroupTest_in_ftsFieldGroupPrefixed1147 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ftsFieldGroupTest_in_ftsFieldGroupPrefixed1165 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_PLUS_in_ftsFieldGroupPrefixed1185 = new BitSet(new long[]{0xFCEB280000000000L,0x0000000000000003L});
    public static final BitSet FOLLOW_ftsFieldGroupTest_in_ftsFieldGroupPrefixed1187 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_BAR_in_ftsFieldGroupPrefixed1221 = new BitSet(new long[]{0xFCEB280000000000L,0x0000000000000003L});
    public static final BitSet FOLLOW_ftsFieldGroupTest_in_ftsFieldGroupPrefixed1223 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_MINUS_in_ftsFieldGroupPrefixed1257 = new BitSet(new long[]{0xFCEB280000000000L,0x0000000000000003L});
    public static final BitSet FOLLOW_ftsFieldGroupTest_in_ftsFieldGroupPrefixed1259 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ftsFieldGroupTerm_in_ftsFieldGroupTest1298 = new BitSet(new long[]{0x0000600000000002L});
    public static final BitSet FOLLOW_fuzzy_in_ftsFieldGroupTest1307 = new BitSet(new long[]{0x0000400000000002L});
    public static final BitSet FOLLOW_boost_in_ftsFieldGroupTest1311 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ftsFieldGroupExactTerm_in_ftsFieldGroupTest1333 = new BitSet(new long[]{0x0000600000000002L});
    public static final BitSet FOLLOW_fuzzy_in_ftsFieldGroupTest1342 = new BitSet(new long[]{0x0000400000000002L});
    public static final BitSet FOLLOW_boost_in_ftsFieldGroupTest1346 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ftsFieldGroupPhrase_in_ftsFieldGroupTest1368 = new BitSet(new long[]{0x0000600000000002L});
    public static final BitSet FOLLOW_fuzzy_in_ftsFieldGroupTest1377 = new BitSet(new long[]{0x0000400000000002L});
    public static final BitSet FOLLOW_boost_in_ftsFieldGroupTest1381 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ftsFieldGroupSynonym_in_ftsFieldGroupTest1403 = new BitSet(new long[]{0x0000600000000002L});
    public static final BitSet FOLLOW_fuzzy_in_ftsFieldGroupTest1412 = new BitSet(new long[]{0x0000400000000002L});
    public static final BitSet FOLLOW_boost_in_ftsFieldGroupTest1416 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ftsFieldGroupProximity_in_ftsFieldGroupTest1439 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ftsFieldGroupRange_in_ftsFieldGroupTest1458 = new BitSet(new long[]{0x0000400000000002L});
    public static final BitSet FOLLOW_boost_in_ftsFieldGroupTest1460 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_ftsFieldGroupTest1486 = new BitSet(new long[]{0xFCEB2F0000000000L,0x000000000000000BL});
    public static final BitSet FOLLOW_ftsFieldGroupImplicitConjunctionOrDisjunction_in_ftsFieldGroupTest1488 = new BitSet(new long[]{0x0000100000000000L});
    public static final BitSet FOLLOW_RPAREN_in_ftsFieldGroupTest1490 = new BitSet(new long[]{0x0000400000000002L});
    public static final BitSet FOLLOW_boost_in_ftsFieldGroupTest1492 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ftsWord_in_ftsFieldGroupTerm1514 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_EQUALS_in_ftsFieldGroupExactTerm1526 = new BitSet(new long[]{0xFC28000000000000L,0x0000000000000003L});
    public static final BitSet FOLLOW_ftsFieldGroupTerm_in_ftsFieldGroupExactTerm1528 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FTSPHRASE_in_ftsFieldGroupPhrase1548 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TILDA_in_ftsFieldGroupSynonym1560 = new BitSet(new long[]{0xFC28000000000000L,0x0000000000000003L});
    public static final BitSet FOLLOW_ftsFieldGroupTerm_in_ftsFieldGroupSynonym1562 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ftsFieldGroupTerm_in_ftsFieldGroupProximity1580 = new BitSet(new long[]{0x0004000000000000L});
    public static final BitSet FOLLOW_proximityGroup_in_ftsFieldGroupProximity1583 = new BitSet(new long[]{0xFC28000000000000L,0x0000000000000003L});
    public static final BitSet FOLLOW_ftsFieldGroupTerm_in_ftsFieldGroupProximity1585 = new BitSet(new long[]{0x0004000000000002L});
    public static final BitSet FOLLOW_STAR_in_proximityGroup1613 = new BitSet(new long[]{0x0000080000000002L});
    public static final BitSet FOLLOW_LPAREN_in_proximityGroup1617 = new BitSet(new long[]{0x0008100000000000L});
    public static final BitSet FOLLOW_DECIMAL_INTEGER_LITERAL_in_proximityGroup1619 = new BitSet(new long[]{0x0000100000000000L});
    public static final BitSet FOLLOW_RPAREN_in_proximityGroup1622 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ftsRangeWord_in_ftsFieldGroupRange1655 = new BitSet(new long[]{0x0010000000000000L});
    public static final BitSet FOLLOW_DOTDOT_in_ftsFieldGroupRange1657 = new BitSet(new long[]{0x300A000000000000L,0x0000000000000002L});
    public static final BitSet FOLLOW_ftsRangeWord_in_ftsFieldGroupRange1659 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_range_left_in_ftsFieldGroupRange1676 = new BitSet(new long[]{0x300A000000000000L,0x0000000000000002L});
    public static final BitSet FOLLOW_ftsRangeWord_in_ftsFieldGroupRange1678 = new BitSet(new long[]{0x0020000000000000L});
    public static final BitSet FOLLOW_TO_in_ftsFieldGroupRange1680 = new BitSet(new long[]{0x300A000000000000L,0x0000000000000002L});
    public static final BitSet FOLLOW_ftsRangeWord_in_ftsFieldGroupRange1682 = new BitSet(new long[]{0x0300000000000000L});
    public static final BitSet FOLLOW_range_right_in_ftsFieldGroupRange1684 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LSQUARE_in_range_left1714 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LT_in_range_left1725 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RSQUARE_in_range_right1749 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_GT_in_range_right1760 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_AT_in_fieldReference1781 = new BitSet(new long[]{0x1800000000000000L});
    public static final BitSet FOLLOW_prefix_in_fieldReference1785 = new BitSet(new long[]{0x1000000000000000L});
    public static final BitSet FOLLOW_uri_in_fieldReference1787 = new BitSet(new long[]{0x1000000000000000L});
    public static final BitSet FOLLOW_identifier_in_fieldReference1791 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_identifier_in_prefix1820 = new BitSet(new long[]{0x0000800000000000L});
    public static final BitSet FOLLOW_COLON_in_prefix1822 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_URI_in_uri1843 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_identifier1866 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_ftsWord0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_number0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_ftsRangeWord0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_OR_in_or2070 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_BAR_in_or2078 = new BitSet(new long[]{0x0000020000000000L});
    public static final BitSet FOLLOW_BAR_in_or2080 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_AND_in_and2102 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_AMP_in_and2110 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000004L});
    public static final BitSet FOLLOW_AMP_in_and2112 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_not0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_or_in_synpred1_FTS318 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_and_in_synpred2_FTS354 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_not_in_synpred3_FTS395 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_fuzzy_in_synpred4_FTS560 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_fuzzy_in_synpred5_FTS595 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_fuzzy_in_synpred6_FTS635 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_fuzzy_in_synpred7_FTS681 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_or_in_synpred8_FTS1068 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_and_in_synpred9_FTS1104 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_not_in_synpred10_FTS1140 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_fuzzy_in_synpred11_FTS1302 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_fuzzy_in_synpred12_FTS1337 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_fuzzy_in_synpred13_FTS1372 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_fuzzy_in_synpred14_FTS1407 = new BitSet(new long[]{0x0000000000000002L});

}