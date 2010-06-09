// $ANTLR 3.2 Sep 23, 2009 12:02:23 W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g 2010-05-01 13:03:38

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
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "FTS", "DISJUNCTION", "CONJUNCTION", "NEGATION", "TERM", "EXACT_TERM", "PHRASE", "SYNONYM", "RANGE", "PROXIMITY", "DEFAULT", "MANDATORY", "OPTIONAL", "EXCLUDE", "FIELD_DISJUNCTION", "FIELD_CONJUNCTION", "FIELD_NEGATION", "FIELD_GROUP", "FIELD_DEFAULT", "FIELD_MANDATORY", "FIELD_OPTIONAL", "FIELD_EXCLUDE", "FG_TERM", "FG_EXACT_TERM", "FG_PHRASE", "FG_SYNONYM", "FG_PROXIMITY", "FG_RANGE", "FIELD_REF", "INCLUSIVE", "EXCLUSIVE", "QUALIFIER", "PREFIX", "NAME_SPACE", "BOOST", "FUZZY", "TEMPLATE", "PLUS", "BAR", "MINUS", "LPAREN", "RPAREN", "PERCENT", "COMMA", "TILDA", "DECIMAL_INTEGER_LITERAL", "CARAT", "COLON", "EQUALS", "FTSPHRASE", "ID", "FTSWORD", "FTSPRE", "FTSWILD", "NOT", "TO", "FLOATING_POINT_LITERAL", "STAR", "DOTDOT", "LSQUARE", "LT", "RSQUARE", "GT", "AT", "URI", "QUESTION_MARK", "OR", "AND", "AMP", "EXCLAMATION", "F_ESC", "F_URI_ALPHA", "F_URI_DIGIT", "F_URI_OTHER", "F_HEX", "F_URI_ESC", "DOT", "LCURL", "RCURL", "DOLLAR", "DECIMAL_NUMERAL", "INWORD", "START_RANGE_I", "START_RANGE_F", "DIGIT", "EXPONENT", "ZERO_DIGIT", "NON_ZERO_DIGIT", "E", "SIGNED_INTEGER", "WS"
    };
    public static final int PREFIX=36;
    public static final int LT=64;
    public static final int EXPONENT=89;
    public static final int STAR=61;
    public static final int LSQUARE=63;
    public static final int FG_TERM=26;
    public static final int FUZZY=39;
    public static final int FIELD_DISJUNCTION=18;
    public static final int EQUALS=52;
    public static final int F_URI_ALPHA=75;
    public static final int FG_EXACT_TERM=27;
    public static final int NOT=58;
    public static final int FIELD_EXCLUDE=25;
    public static final int EOF=-1;
    public static final int NAME_SPACE=37;
    public static final int RPAREN=45;
    public static final int FLOATING_POINT_LITERAL=60;
    public static final int EXCLAMATION=73;
    public static final int QUESTION_MARK=69;
    public static final int ZERO_DIGIT=90;
    public static final int FIELD_OPTIONAL=24;
    public static final int SYNONYM=11;
    public static final int E=92;
    public static final int CONJUNCTION=6;
    public static final int FTSWORD=55;
    public static final int URI=68;
    public static final int DISJUNCTION=5;
    public static final int FTS=4;
    public static final int WS=94;
    public static final int FG_SYNONYM=29;
    public static final int FTSPHRASE=53;
    public static final int FIELD_CONJUNCTION=19;
    public static final int INCLUSIVE=33;
    public static final int OR=70;
    public static final int GT=66;
    public static final int F_HEX=78;
    public static final int DECIMAL_INTEGER_LITERAL=49;
    public static final int FTSPRE=56;
    public static final int FG_PHRASE=28;
    public static final int FIELD_NEGATION=20;
    public static final int TERM=8;
    public static final int DOLLAR=83;
    public static final int START_RANGE_I=86;
    public static final int AMP=72;
    public static final int FG_PROXIMITY=30;
    public static final int EXACT_TERM=9;
    public static final int START_RANGE_F=87;
    public static final int DOTDOT=62;
    public static final int MANDATORY=15;
    public static final int EXCLUSIVE=34;
    public static final int ID=54;
    public static final int AND=71;
    public static final int LPAREN=44;
    public static final int BOOST=38;
    public static final int AT=67;
    public static final int TILDA=48;
    public static final int DECIMAL_NUMERAL=84;
    public static final int COMMA=47;
    public static final int F_URI_DIGIT=76;
    public static final int SIGNED_INTEGER=93;
    public static final int FIELD_DEFAULT=22;
    public static final int CARAT=50;
    public static final int PLUS=41;
    public static final int DIGIT=88;
    public static final int DOT=80;
    public static final int F_ESC=74;
    public static final int EXCLUDE=17;
    public static final int PERCENT=46;
    public static final int NON_ZERO_DIGIT=91;
    public static final int QUALIFIER=35;
    public static final int TO=59;
    public static final int FIELD_GROUP=21;
    public static final int DEFAULT=14;
    public static final int INWORD=85;
    public static final int RANGE=12;
    public static final int MINUS=43;
    public static final int RSQUARE=65;
    public static final int FIELD_REF=32;
    public static final int PROXIMITY=13;
    public static final int PHRASE=10;
    public static final int OPTIONAL=16;
    public static final int COLON=51;
    public static final int LCURL=81;
    public static final int F_URI_OTHER=77;
    public static final int NEGATION=7;
    public static final int F_URI_ESC=79;
    public static final int TEMPLATE=40;
    public static final int RCURL=82;
    public static final int FIELD_MANDATORY=23;
    public static final int FG_RANGE=31;
    public static final int BAR=42;
    public static final int FTSWILD=57;

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
    public String getGrammarFileName() { return "W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g"; }


        public enum Mode
        {
            CMIS, DEFAULT_CONJUNCTION, DEFAULT_DISJUNCTION
        }

        private Stack<String> paraphrases = new Stack<String>();
        
        private boolean defaultFieldConjunction = true;
        
        private Mode mode = Mode.DEFAULT_CONJUNCTION;
        
        public Mode getMode()
        {
           return mode;
        }
        
        public void setMode(Mode mode)
        {
           this.mode = mode;
        }
        
        public boolean defaultFieldConjunction()
        {
           return defaultFieldConjunction;
        }
        
        public void setDefaultFieldConjunction(boolean defaultFieldConjunction)
        {
           this.defaultFieldConjunction = defaultFieldConjunction;
        }
        
        protected Object recoverFromMismatchedToken(IntStream input, int ttype, BitSet follow) throws RecognitionException
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
            String msg = e.getMessage();
            if ( e instanceof UnwantedTokenException ) 
                {
                UnwantedTokenException ute = (UnwantedTokenException)e;
                String tokenName="<unknown>";
                if ( ute.expecting== Token.EOF ) 
                {
                    tokenName = "EOF";
                }
                else 
                {
                    tokenName = tokenNames[ute.expecting];
                }
                msg = "extraneous input " + getTokenErrorDisplay(ute.getUnexpectedToken())
                    + " expecting "+tokenName;
            }
            else if ( e instanceof MissingTokenException ) 
            {
                MissingTokenException mte = (MissingTokenException)e;
                String tokenName="<unknown>";
                if ( mte.expecting== Token.EOF ) 
                {
                    tokenName = "EOF";
                }
                else 
                {
                    tokenName = tokenNames[mte.expecting];
                }
                msg = "missing " + tokenName+" at " + getTokenErrorDisplay(e.token)
                    + "  (" + getLongTokenErrorDisplay(e.token) +")";
            }
            else if ( e instanceof MismatchedTokenException ) 
            {
                MismatchedTokenException mte = (MismatchedTokenException)e;
                String tokenName="<unknown>";
                if ( mte.expecting== Token.EOF ) 
                {
                    tokenName = "EOF";
                }
                else
                {
                    tokenName = tokenNames[mte.expecting];
                }
                msg = "mismatched input " + getTokenErrorDisplay(e.token)
                    + " expecting " + tokenName +"  (" + getLongTokenErrorDisplay(e.token) + ")";
            }
            else if ( e instanceof MismatchedTreeNodeException ) 
            {
                MismatchedTreeNodeException mtne = (MismatchedTreeNodeException)e;
                String tokenName="<unknown>";
                if ( mtne.expecting==Token.EOF )  
                {
                    tokenName = "EOF";
                }
                else 
                {
                    tokenName = tokenNames[mtne.expecting];
                }
                msg = "mismatched tree node: " + mtne.node + " expecting " + tokenName;
            }
            else if ( e instanceof NoViableAltException ) 
            {
                NoViableAltException nvae = (NoViableAltException)e;
                msg = "no viable alternative at input " + getTokenErrorDisplay(e.token)
                    + "\n\t (decision=" + nvae.decisionNumber
                    + " state " + nvae.stateNumber + ")" 
                    + " decision=<<" + nvae.grammarDecisionDescription + ">>";
            }
            else if ( e instanceof EarlyExitException ) 
            {
                //EarlyExitException eee = (EarlyExitException)e;
                // for development, can add "(decision="+eee.decisionNumber+")"
                msg = "required (...)+ loop did not match anything at input " + getTokenErrorDisplay(e.token);
            }
                else if ( e instanceof MismatchedSetException ) 
                {
                    MismatchedSetException mse = (MismatchedSetException)e;
                    msg = "mismatched input " + getTokenErrorDisplay(e.token)
                    + " expecting set " + mse.expecting;
            }
            else if ( e instanceof MismatchedNotSetException ) 
            {
                MismatchedNotSetException mse = (MismatchedNotSetException)e;
                msg = "mismatched input " + getTokenErrorDisplay(e.token)
                    + " expecting set " + mse.expecting;
            }
            else if ( e instanceof FailedPredicateException ) 
            {
                FailedPredicateException fpe = (FailedPredicateException)e;
                msg = "rule " + fpe.ruleName + " failed predicate: {" + fpe.predicateText + "}?";
            }
                    
            if(paraphrases.size() > 0)
            {
                String paraphrase = (String)paraphrases.peek();
                msg = msg+" "+paraphrase;
            }
            return msg +"\n\t"+stack;
        }
            
        public String getLongTokenErrorDisplay(Token t)
        {
            return t.toString();
        }
        

        public String getErrorString(RecognitionException e)
        {
            String hdr = getErrorHeader(e);
            String msg = getErrorMessage(e, this.getTokenNames());
            return hdr+" "+msg;
        } 


    public static class ftsQuery_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "ftsQuery"
    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:338:1: ftsQuery : ftsDisjunction EOF -> ftsDisjunction ;
    public final FTSParser.ftsQuery_return ftsQuery() throws RecognitionException {
        FTSParser.ftsQuery_return retval = new FTSParser.ftsQuery_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token EOF2=null;
        FTSParser.ftsDisjunction_return ftsDisjunction1 = null;


        Object EOF2_tree=null;
        RewriteRuleTokenStream stream_EOF=new RewriteRuleTokenStream(adaptor,"token EOF");
        RewriteRuleSubtreeStream stream_ftsDisjunction=new RewriteRuleSubtreeStream(adaptor,"rule ftsDisjunction");
        try {
            // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:339:9: ( ftsDisjunction EOF -> ftsDisjunction )
            // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:340:9: ftsDisjunction EOF
            {
            pushFollow(FOLLOW_ftsDisjunction_in_ftsQuery535);
            ftsDisjunction1=ftsDisjunction();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_ftsDisjunction.add(ftsDisjunction1.getTree());
            EOF2=(Token)match(input,EOF,FOLLOW_EOF_in_ftsQuery537); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_EOF.add(EOF2);



            // AST REWRITE
            // elements: ftsDisjunction
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 341:17: -> ftsDisjunction
            {
                adaptor.addChild(root_0, stream_ftsDisjunction.nextTree());

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

    public static class ftsDisjunction_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "ftsDisjunction"
    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:349:1: ftsDisjunction : ({...}? => cmisExplicitDisjunction | {...}? => ftsExplicitDisjunction | {...}? => ftsImplicitDisjunction );
    public final FTSParser.ftsDisjunction_return ftsDisjunction() throws RecognitionException {
        FTSParser.ftsDisjunction_return retval = new FTSParser.ftsDisjunction_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        FTSParser.cmisExplicitDisjunction_return cmisExplicitDisjunction3 = null;

        FTSParser.ftsExplicitDisjunction_return ftsExplicitDisjunction4 = null;

        FTSParser.ftsImplicitDisjunction_return ftsImplicitDisjunction5 = null;



        try {
            // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:350:9: ({...}? => cmisExplicitDisjunction | {...}? => ftsExplicitDisjunction | {...}? => ftsImplicitDisjunction )
            int alt1=3;
            alt1 = dfa1.predict(input);
            switch (alt1) {
                case 1 :
                    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:351:9: {...}? => cmisExplicitDisjunction
                    {
                    root_0 = (Object)adaptor.nil();

                    if ( !((getMode() == Mode.CMIS)) ) {
                        if (state.backtracking>0) {state.failed=true; return retval;}
                        throw new FailedPredicateException(input, "ftsDisjunction", "getMode() == Mode.CMIS");
                    }
                    pushFollow(FOLLOW_cmisExplicitDisjunction_in_ftsDisjunction596);
                    cmisExplicitDisjunction3=cmisExplicitDisjunction();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, cmisExplicitDisjunction3.getTree());

                    }
                    break;
                case 2 :
                    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:352:11: {...}? => ftsExplicitDisjunction
                    {
                    root_0 = (Object)adaptor.nil();

                    if ( !((getMode() == Mode.DEFAULT_CONJUNCTION)) ) {
                        if (state.backtracking>0) {state.failed=true; return retval;}
                        throw new FailedPredicateException(input, "ftsDisjunction", "getMode() == Mode.DEFAULT_CONJUNCTION");
                    }
                    pushFollow(FOLLOW_ftsExplicitDisjunction_in_ftsDisjunction611);
                    ftsExplicitDisjunction4=ftsExplicitDisjunction();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, ftsExplicitDisjunction4.getTree());

                    }
                    break;
                case 3 :
                    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:353:11: {...}? => ftsImplicitDisjunction
                    {
                    root_0 = (Object)adaptor.nil();

                    if ( !((getMode() == Mode.DEFAULT_DISJUNCTION)) ) {
                        if (state.backtracking>0) {state.failed=true; return retval;}
                        throw new FailedPredicateException(input, "ftsDisjunction", "getMode() == Mode.DEFAULT_DISJUNCTION");
                    }
                    pushFollow(FOLLOW_ftsImplicitDisjunction_in_ftsDisjunction626);
                    ftsImplicitDisjunction5=ftsImplicitDisjunction();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, ftsImplicitDisjunction5.getTree());

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
    // $ANTLR end "ftsDisjunction"

    public static class ftsExplicitDisjunction_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "ftsExplicitDisjunction"
    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:356:1: ftsExplicitDisjunction : ftsImplicitConjunction ( or ftsImplicitConjunction )* -> ^( DISJUNCTION ( ftsImplicitConjunction )+ ) ;
    public final FTSParser.ftsExplicitDisjunction_return ftsExplicitDisjunction() throws RecognitionException {
        FTSParser.ftsExplicitDisjunction_return retval = new FTSParser.ftsExplicitDisjunction_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        FTSParser.ftsImplicitConjunction_return ftsImplicitConjunction6 = null;

        FTSParser.or_return or7 = null;

        FTSParser.ftsImplicitConjunction_return ftsImplicitConjunction8 = null;


        RewriteRuleSubtreeStream stream_or=new RewriteRuleSubtreeStream(adaptor,"rule or");
        RewriteRuleSubtreeStream stream_ftsImplicitConjunction=new RewriteRuleSubtreeStream(adaptor,"rule ftsImplicitConjunction");
        try {
            // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:357:9: ( ftsImplicitConjunction ( or ftsImplicitConjunction )* -> ^( DISJUNCTION ( ftsImplicitConjunction )+ ) )
            // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:358:9: ftsImplicitConjunction ( or ftsImplicitConjunction )*
            {
            pushFollow(FOLLOW_ftsImplicitConjunction_in_ftsExplicitDisjunction659);
            ftsImplicitConjunction6=ftsImplicitConjunction();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_ftsImplicitConjunction.add(ftsImplicitConjunction6.getTree());
            // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:358:32: ( or ftsImplicitConjunction )*
            loop2:
            do {
                int alt2=2;
                int LA2_0 = input.LA(1);

                if ( (LA2_0==BAR||LA2_0==OR) ) {
                    alt2=1;
                }


                switch (alt2) {
            	case 1 :
            	    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:358:33: or ftsImplicitConjunction
            	    {
            	    pushFollow(FOLLOW_or_in_ftsExplicitDisjunction662);
            	    or7=or();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_or.add(or7.getTree());
            	    pushFollow(FOLLOW_ftsImplicitConjunction_in_ftsExplicitDisjunction664);
            	    ftsImplicitConjunction8=ftsImplicitConjunction();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_ftsImplicitConjunction.add(ftsImplicitConjunction8.getTree());

            	    }
            	    break;

            	default :
            	    break loop2;
                }
            } while (true);



            // AST REWRITE
            // elements: ftsImplicitConjunction
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 359:17: -> ^( DISJUNCTION ( ftsImplicitConjunction )+ )
            {
                // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:360:25: ^( DISJUNCTION ( ftsImplicitConjunction )+ )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(DISJUNCTION, "DISJUNCTION"), root_1);

                if ( !(stream_ftsImplicitConjunction.hasNext()) ) {
                    throw new RewriteEarlyExitException();
                }
                while ( stream_ftsImplicitConjunction.hasNext() ) {
                    adaptor.addChild(root_1, stream_ftsImplicitConjunction.nextTree());

                }
                stream_ftsImplicitConjunction.reset();

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

    public static class cmisExplicitDisjunction_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "cmisExplicitDisjunction"
    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:363:1: cmisExplicitDisjunction : cmisConjunction ( or cmisConjunction )* -> ^( DISJUNCTION ( cmisConjunction )+ ) ;
    public final FTSParser.cmisExplicitDisjunction_return cmisExplicitDisjunction() throws RecognitionException {
        FTSParser.cmisExplicitDisjunction_return retval = new FTSParser.cmisExplicitDisjunction_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        FTSParser.cmisConjunction_return cmisConjunction9 = null;

        FTSParser.or_return or10 = null;

        FTSParser.cmisConjunction_return cmisConjunction11 = null;


        RewriteRuleSubtreeStream stream_cmisConjunction=new RewriteRuleSubtreeStream(adaptor,"rule cmisConjunction");
        RewriteRuleSubtreeStream stream_or=new RewriteRuleSubtreeStream(adaptor,"rule or");
        try {
            // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:364:9: ( cmisConjunction ( or cmisConjunction )* -> ^( DISJUNCTION ( cmisConjunction )+ ) )
            // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:365:9: cmisConjunction ( or cmisConjunction )*
            {
            pushFollow(FOLLOW_cmisConjunction_in_cmisExplicitDisjunction748);
            cmisConjunction9=cmisConjunction();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_cmisConjunction.add(cmisConjunction9.getTree());
            // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:365:25: ( or cmisConjunction )*
            loop3:
            do {
                int alt3=2;
                int LA3_0 = input.LA(1);

                if ( (LA3_0==BAR||LA3_0==OR) ) {
                    alt3=1;
                }


                switch (alt3) {
            	case 1 :
            	    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:365:26: or cmisConjunction
            	    {
            	    pushFollow(FOLLOW_or_in_cmisExplicitDisjunction751);
            	    or10=or();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_or.add(or10.getTree());
            	    pushFollow(FOLLOW_cmisConjunction_in_cmisExplicitDisjunction753);
            	    cmisConjunction11=cmisConjunction();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_cmisConjunction.add(cmisConjunction11.getTree());

            	    }
            	    break;

            	default :
            	    break loop3;
                }
            } while (true);



            // AST REWRITE
            // elements: cmisConjunction
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 366:17: -> ^( DISJUNCTION ( cmisConjunction )+ )
            {
                // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:367:25: ^( DISJUNCTION ( cmisConjunction )+ )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(DISJUNCTION, "DISJUNCTION"), root_1);

                if ( !(stream_cmisConjunction.hasNext()) ) {
                    throw new RewriteEarlyExitException();
                }
                while ( stream_cmisConjunction.hasNext() ) {
                    adaptor.addChild(root_1, stream_cmisConjunction.nextTree());

                }
                stream_cmisConjunction.reset();

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
    // $ANTLR end "cmisExplicitDisjunction"

    public static class ftsImplicitDisjunction_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "ftsImplicitDisjunction"
    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:370:1: ftsImplicitDisjunction : ( ( or )? ftsExplicitConjunction )+ -> ^( DISJUNCTION ( ftsExplicitConjunction )+ ) ;
    public final FTSParser.ftsImplicitDisjunction_return ftsImplicitDisjunction() throws RecognitionException {
        FTSParser.ftsImplicitDisjunction_return retval = new FTSParser.ftsImplicitDisjunction_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        FTSParser.or_return or12 = null;

        FTSParser.ftsExplicitConjunction_return ftsExplicitConjunction13 = null;


        RewriteRuleSubtreeStream stream_or=new RewriteRuleSubtreeStream(adaptor,"rule or");
        RewriteRuleSubtreeStream stream_ftsExplicitConjunction=new RewriteRuleSubtreeStream(adaptor,"rule ftsExplicitConjunction");
        try {
            // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:371:9: ( ( ( or )? ftsExplicitConjunction )+ -> ^( DISJUNCTION ( ftsExplicitConjunction )+ ) )
            // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:372:9: ( ( or )? ftsExplicitConjunction )+
            {
            // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:372:9: ( ( or )? ftsExplicitConjunction )+
            int cnt5=0;
            loop5:
            do {
                int alt5=2;
                alt5 = dfa5.predict(input);
                switch (alt5) {
            	case 1 :
            	    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:372:10: ( or )? ftsExplicitConjunction
            	    {
            	    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:372:10: ( or )?
            	    int alt4=2;
            	    alt4 = dfa4.predict(input);
            	    switch (alt4) {
            	        case 1 :
            	            // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:372:10: or
            	            {
            	            pushFollow(FOLLOW_or_in_ftsImplicitDisjunction838);
            	            or12=or();

            	            state._fsp--;
            	            if (state.failed) return retval;
            	            if ( state.backtracking==0 ) stream_or.add(or12.getTree());

            	            }
            	            break;

            	    }

            	    pushFollow(FOLLOW_ftsExplicitConjunction_in_ftsImplicitDisjunction841);
            	    ftsExplicitConjunction13=ftsExplicitConjunction();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_ftsExplicitConjunction.add(ftsExplicitConjunction13.getTree());

            	    }
            	    break;

            	default :
            	    if ( cnt5 >= 1 ) break loop5;
            	    if (state.backtracking>0) {state.failed=true; return retval;}
                        EarlyExitException eee =
                            new EarlyExitException(5, input);
                        throw eee;
                }
                cnt5++;
            } while (true);



            // AST REWRITE
            // elements: ftsExplicitConjunction
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 373:17: -> ^( DISJUNCTION ( ftsExplicitConjunction )+ )
            {
                // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:374:25: ^( DISJUNCTION ( ftsExplicitConjunction )+ )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(DISJUNCTION, "DISJUNCTION"), root_1);

                if ( !(stream_ftsExplicitConjunction.hasNext()) ) {
                    throw new RewriteEarlyExitException();
                }
                while ( stream_ftsExplicitConjunction.hasNext() ) {
                    adaptor.addChild(root_1, stream_ftsExplicitConjunction.nextTree());

                }
                stream_ftsExplicitConjunction.reset();

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
    // $ANTLR end "ftsImplicitDisjunction"

    public static class ftsExplicitConjunction_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "ftsExplicitConjunction"
    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:381:1: ftsExplicitConjunction : ftsPrefixed ( and ftsPrefixed )* -> ^( CONJUNCTION ( ftsPrefixed )+ ) ;
    public final FTSParser.ftsExplicitConjunction_return ftsExplicitConjunction() throws RecognitionException {
        FTSParser.ftsExplicitConjunction_return retval = new FTSParser.ftsExplicitConjunction_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        FTSParser.ftsPrefixed_return ftsPrefixed14 = null;

        FTSParser.and_return and15 = null;

        FTSParser.ftsPrefixed_return ftsPrefixed16 = null;


        RewriteRuleSubtreeStream stream_ftsPrefixed=new RewriteRuleSubtreeStream(adaptor,"rule ftsPrefixed");
        RewriteRuleSubtreeStream stream_and=new RewriteRuleSubtreeStream(adaptor,"rule and");
        try {
            // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:382:9: ( ftsPrefixed ( and ftsPrefixed )* -> ^( CONJUNCTION ( ftsPrefixed )+ ) )
            // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:383:9: ftsPrefixed ( and ftsPrefixed )*
            {
            pushFollow(FOLLOW_ftsPrefixed_in_ftsExplicitConjunction928);
            ftsPrefixed14=ftsPrefixed();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_ftsPrefixed.add(ftsPrefixed14.getTree());
            // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:383:21: ( and ftsPrefixed )*
            loop6:
            do {
                int alt6=2;
                int LA6_0 = input.LA(1);

                if ( ((LA6_0>=AND && LA6_0<=AMP)) ) {
                    alt6=1;
                }


                switch (alt6) {
            	case 1 :
            	    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:383:22: and ftsPrefixed
            	    {
            	    pushFollow(FOLLOW_and_in_ftsExplicitConjunction931);
            	    and15=and();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_and.add(and15.getTree());
            	    pushFollow(FOLLOW_ftsPrefixed_in_ftsExplicitConjunction933);
            	    ftsPrefixed16=ftsPrefixed();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_ftsPrefixed.add(ftsPrefixed16.getTree());

            	    }
            	    break;

            	default :
            	    break loop6;
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
            // 384:17: -> ^( CONJUNCTION ( ftsPrefixed )+ )
            {
                // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:385:25: ^( CONJUNCTION ( ftsPrefixed )+ )
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
    // $ANTLR end "ftsExplicitConjunction"

    public static class ftsImplicitConjunction_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "ftsImplicitConjunction"
    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:388:1: ftsImplicitConjunction : ( ( and )? ftsPrefixed )+ -> ^( CONJUNCTION ( ftsPrefixed )+ ) ;
    public final FTSParser.ftsImplicitConjunction_return ftsImplicitConjunction() throws RecognitionException {
        FTSParser.ftsImplicitConjunction_return retval = new FTSParser.ftsImplicitConjunction_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        FTSParser.and_return and17 = null;

        FTSParser.ftsPrefixed_return ftsPrefixed18 = null;


        RewriteRuleSubtreeStream stream_ftsPrefixed=new RewriteRuleSubtreeStream(adaptor,"rule ftsPrefixed");
        RewriteRuleSubtreeStream stream_and=new RewriteRuleSubtreeStream(adaptor,"rule and");
        try {
            // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:389:9: ( ( ( and )? ftsPrefixed )+ -> ^( CONJUNCTION ( ftsPrefixed )+ ) )
            // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:390:9: ( ( and )? ftsPrefixed )+
            {
            // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:390:9: ( ( and )? ftsPrefixed )+
            int cnt8=0;
            loop8:
            do {
                int alt8=2;
                alt8 = dfa8.predict(input);
                switch (alt8) {
            	case 1 :
            	    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:390:10: ( and )? ftsPrefixed
            	    {
            	    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:390:10: ( and )?
            	    int alt7=2;
            	    alt7 = dfa7.predict(input);
            	    switch (alt7) {
            	        case 1 :
            	            // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:390:10: and
            	            {
            	            pushFollow(FOLLOW_and_in_ftsImplicitConjunction1018);
            	            and17=and();

            	            state._fsp--;
            	            if (state.failed) return retval;
            	            if ( state.backtracking==0 ) stream_and.add(and17.getTree());

            	            }
            	            break;

            	    }

            	    pushFollow(FOLLOW_ftsPrefixed_in_ftsImplicitConjunction1021);
            	    ftsPrefixed18=ftsPrefixed();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_ftsPrefixed.add(ftsPrefixed18.getTree());

            	    }
            	    break;

            	default :
            	    if ( cnt8 >= 1 ) break loop8;
            	    if (state.backtracking>0) {state.failed=true; return retval;}
                        EarlyExitException eee =
                            new EarlyExitException(8, input);
                        throw eee;
                }
                cnt8++;
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
            // 391:17: -> ^( CONJUNCTION ( ftsPrefixed )+ )
            {
                // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:392:25: ^( CONJUNCTION ( ftsPrefixed )+ )
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
    // $ANTLR end "ftsImplicitConjunction"

    public static class cmisConjunction_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "cmisConjunction"
    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:395:1: cmisConjunction : ( cmisPrefixed )+ -> ^( CONJUNCTION ( cmisPrefixed )+ ) ;
    public final FTSParser.cmisConjunction_return cmisConjunction() throws RecognitionException {
        FTSParser.cmisConjunction_return retval = new FTSParser.cmisConjunction_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        FTSParser.cmisPrefixed_return cmisPrefixed19 = null;


        RewriteRuleSubtreeStream stream_cmisPrefixed=new RewriteRuleSubtreeStream(adaptor,"rule cmisPrefixed");
        try {
            // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:396:9: ( ( cmisPrefixed )+ -> ^( CONJUNCTION ( cmisPrefixed )+ ) )
            // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:397:9: ( cmisPrefixed )+
            {
            // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:397:9: ( cmisPrefixed )+
            int cnt9=0;
            loop9:
            do {
                int alt9=2;
                int LA9_0 = input.LA(1);

                if ( (LA9_0==MINUS||LA9_0==DECIMAL_INTEGER_LITERAL||(LA9_0>=FTSPHRASE && LA9_0<=STAR)||LA9_0==QUESTION_MARK) ) {
                    alt9=1;
                }


                switch (alt9) {
            	case 1 :
            	    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:397:9: cmisPrefixed
            	    {
            	    pushFollow(FOLLOW_cmisPrefixed_in_cmisConjunction1105);
            	    cmisPrefixed19=cmisPrefixed();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_cmisPrefixed.add(cmisPrefixed19.getTree());

            	    }
            	    break;

            	default :
            	    if ( cnt9 >= 1 ) break loop9;
            	    if (state.backtracking>0) {state.failed=true; return retval;}
                        EarlyExitException eee =
                            new EarlyExitException(9, input);
                        throw eee;
                }
                cnt9++;
            } while (true);



            // AST REWRITE
            // elements: cmisPrefixed
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 398:17: -> ^( CONJUNCTION ( cmisPrefixed )+ )
            {
                // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:399:25: ^( CONJUNCTION ( cmisPrefixed )+ )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(CONJUNCTION, "CONJUNCTION"), root_1);

                if ( !(stream_cmisPrefixed.hasNext()) ) {
                    throw new RewriteEarlyExitException();
                }
                while ( stream_cmisPrefixed.hasNext() ) {
                    adaptor.addChild(root_1, stream_cmisPrefixed.nextTree());

                }
                stream_cmisPrefixed.reset();

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
    // $ANTLR end "cmisConjunction"

    public static class ftsPrefixed_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "ftsPrefixed"
    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:409:1: ftsPrefixed : ( ( not )=> not ftsTest ( boost )? -> ^( NEGATION ftsTest ( boost )? ) | ftsTest ( boost )? -> ^( DEFAULT ftsTest ( boost )? ) | PLUS ftsTest ( boost )? -> ^( MANDATORY ftsTest ( boost )? ) | BAR ftsTest ( boost )? -> ^( OPTIONAL ftsTest ( boost )? ) | MINUS ftsTest ( boost )? -> ^( EXCLUDE ftsTest ( boost )? ) );
    public final FTSParser.ftsPrefixed_return ftsPrefixed() throws RecognitionException {
        FTSParser.ftsPrefixed_return retval = new FTSParser.ftsPrefixed_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token PLUS25=null;
        Token BAR28=null;
        Token MINUS31=null;
        FTSParser.not_return not20 = null;

        FTSParser.ftsTest_return ftsTest21 = null;

        FTSParser.boost_return boost22 = null;

        FTSParser.ftsTest_return ftsTest23 = null;

        FTSParser.boost_return boost24 = null;

        FTSParser.ftsTest_return ftsTest26 = null;

        FTSParser.boost_return boost27 = null;

        FTSParser.ftsTest_return ftsTest29 = null;

        FTSParser.boost_return boost30 = null;

        FTSParser.ftsTest_return ftsTest32 = null;

        FTSParser.boost_return boost33 = null;


        Object PLUS25_tree=null;
        Object BAR28_tree=null;
        Object MINUS31_tree=null;
        RewriteRuleTokenStream stream_PLUS=new RewriteRuleTokenStream(adaptor,"token PLUS");
        RewriteRuleTokenStream stream_MINUS=new RewriteRuleTokenStream(adaptor,"token MINUS");
        RewriteRuleTokenStream stream_BAR=new RewriteRuleTokenStream(adaptor,"token BAR");
        RewriteRuleSubtreeStream stream_not=new RewriteRuleSubtreeStream(adaptor,"rule not");
        RewriteRuleSubtreeStream stream_ftsTest=new RewriteRuleSubtreeStream(adaptor,"rule ftsTest");
        RewriteRuleSubtreeStream stream_boost=new RewriteRuleSubtreeStream(adaptor,"rule boost");
        try {
            // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:410:9: ( ( not )=> not ftsTest ( boost )? -> ^( NEGATION ftsTest ( boost )? ) | ftsTest ( boost )? -> ^( DEFAULT ftsTest ( boost )? ) | PLUS ftsTest ( boost )? -> ^( MANDATORY ftsTest ( boost )? ) | BAR ftsTest ( boost )? -> ^( OPTIONAL ftsTest ( boost )? ) | MINUS ftsTest ( boost )? -> ^( EXCLUDE ftsTest ( boost )? ) )
            int alt15=5;
            alt15 = dfa15.predict(input);
            switch (alt15) {
                case 1 :
                    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:411:9: ( not )=> not ftsTest ( boost )?
                    {
                    pushFollow(FOLLOW_not_in_ftsPrefixed1197);
                    not20=not();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_not.add(not20.getTree());
                    pushFollow(FOLLOW_ftsTest_in_ftsPrefixed1199);
                    ftsTest21=ftsTest();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_ftsTest.add(ftsTest21.getTree());
                    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:411:30: ( boost )?
                    int alt10=2;
                    int LA10_0 = input.LA(1);

                    if ( (LA10_0==CARAT) ) {
                        alt10=1;
                    }
                    switch (alt10) {
                        case 1 :
                            // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:411:30: boost
                            {
                            pushFollow(FOLLOW_boost_in_ftsPrefixed1201);
                            boost22=boost();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_boost.add(boost22.getTree());

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
                    // 412:17: -> ^( NEGATION ftsTest ( boost )? )
                    {
                        // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:413:25: ^( NEGATION ftsTest ( boost )? )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(NEGATION, "NEGATION"), root_1);

                        adaptor.addChild(root_1, stream_ftsTest.nextTree());
                        // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:413:44: ( boost )?
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
                    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:414:11: ftsTest ( boost )?
                    {
                    pushFollow(FOLLOW_ftsTest_in_ftsPrefixed1265);
                    ftsTest23=ftsTest();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_ftsTest.add(ftsTest23.getTree());
                    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:414:19: ( boost )?
                    int alt11=2;
                    int LA11_0 = input.LA(1);

                    if ( (LA11_0==CARAT) ) {
                        alt11=1;
                    }
                    switch (alt11) {
                        case 1 :
                            // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:414:19: boost
                            {
                            pushFollow(FOLLOW_boost_in_ftsPrefixed1267);
                            boost24=boost();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_boost.add(boost24.getTree());

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
                    // 415:17: -> ^( DEFAULT ftsTest ( boost )? )
                    {
                        // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:416:25: ^( DEFAULT ftsTest ( boost )? )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(DEFAULT, "DEFAULT"), root_1);

                        adaptor.addChild(root_1, stream_ftsTest.nextTree());
                        // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:416:43: ( boost )?
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
                    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:417:11: PLUS ftsTest ( boost )?
                    {
                    PLUS25=(Token)match(input,PLUS,FOLLOW_PLUS_in_ftsPrefixed1331); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_PLUS.add(PLUS25);

                    pushFollow(FOLLOW_ftsTest_in_ftsPrefixed1333);
                    ftsTest26=ftsTest();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_ftsTest.add(ftsTest26.getTree());
                    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:417:24: ( boost )?
                    int alt12=2;
                    int LA12_0 = input.LA(1);

                    if ( (LA12_0==CARAT) ) {
                        alt12=1;
                    }
                    switch (alt12) {
                        case 1 :
                            // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:417:24: boost
                            {
                            pushFollow(FOLLOW_boost_in_ftsPrefixed1335);
                            boost27=boost();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_boost.add(boost27.getTree());

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
                    // 418:17: -> ^( MANDATORY ftsTest ( boost )? )
                    {
                        // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:419:25: ^( MANDATORY ftsTest ( boost )? )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(MANDATORY, "MANDATORY"), root_1);

                        adaptor.addChild(root_1, stream_ftsTest.nextTree());
                        // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:419:45: ( boost )?
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
                    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:420:11: BAR ftsTest ( boost )?
                    {
                    BAR28=(Token)match(input,BAR,FOLLOW_BAR_in_ftsPrefixed1399); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_BAR.add(BAR28);

                    pushFollow(FOLLOW_ftsTest_in_ftsPrefixed1401);
                    ftsTest29=ftsTest();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_ftsTest.add(ftsTest29.getTree());
                    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:420:23: ( boost )?
                    int alt13=2;
                    int LA13_0 = input.LA(1);

                    if ( (LA13_0==CARAT) ) {
                        alt13=1;
                    }
                    switch (alt13) {
                        case 1 :
                            // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:420:23: boost
                            {
                            pushFollow(FOLLOW_boost_in_ftsPrefixed1403);
                            boost30=boost();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_boost.add(boost30.getTree());

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
                    // 421:17: -> ^( OPTIONAL ftsTest ( boost )? )
                    {
                        // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:422:25: ^( OPTIONAL ftsTest ( boost )? )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(OPTIONAL, "OPTIONAL"), root_1);

                        adaptor.addChild(root_1, stream_ftsTest.nextTree());
                        // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:422:44: ( boost )?
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
                    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:423:11: MINUS ftsTest ( boost )?
                    {
                    MINUS31=(Token)match(input,MINUS,FOLLOW_MINUS_in_ftsPrefixed1467); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_MINUS.add(MINUS31);

                    pushFollow(FOLLOW_ftsTest_in_ftsPrefixed1469);
                    ftsTest32=ftsTest();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_ftsTest.add(ftsTest32.getTree());
                    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:423:25: ( boost )?
                    int alt14=2;
                    int LA14_0 = input.LA(1);

                    if ( (LA14_0==CARAT) ) {
                        alt14=1;
                    }
                    switch (alt14) {
                        case 1 :
                            // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:423:25: boost
                            {
                            pushFollow(FOLLOW_boost_in_ftsPrefixed1471);
                            boost33=boost();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_boost.add(boost33.getTree());

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
                    // 424:17: -> ^( EXCLUDE ftsTest ( boost )? )
                    {
                        // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:425:25: ^( EXCLUDE ftsTest ( boost )? )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(EXCLUDE, "EXCLUDE"), root_1);

                        adaptor.addChild(root_1, stream_ftsTest.nextTree());
                        // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:425:43: ( boost )?
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

    public static class cmisPrefixed_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "cmisPrefixed"
    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:428:1: cmisPrefixed : ( cmisTest -> ^( DEFAULT cmisTest ) | MINUS cmisTest -> ^( EXCLUDE cmisTest ) );
    public final FTSParser.cmisPrefixed_return cmisPrefixed() throws RecognitionException {
        FTSParser.cmisPrefixed_return retval = new FTSParser.cmisPrefixed_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token MINUS35=null;
        FTSParser.cmisTest_return cmisTest34 = null;

        FTSParser.cmisTest_return cmisTest36 = null;


        Object MINUS35_tree=null;
        RewriteRuleTokenStream stream_MINUS=new RewriteRuleTokenStream(adaptor,"token MINUS");
        RewriteRuleSubtreeStream stream_cmisTest=new RewriteRuleSubtreeStream(adaptor,"rule cmisTest");
        try {
            // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:429:9: ( cmisTest -> ^( DEFAULT cmisTest ) | MINUS cmisTest -> ^( EXCLUDE cmisTest ) )
            int alt16=2;
            int LA16_0 = input.LA(1);

            if ( (LA16_0==DECIMAL_INTEGER_LITERAL||(LA16_0>=FTSPHRASE && LA16_0<=STAR)||LA16_0==QUESTION_MARK) ) {
                alt16=1;
            }
            else if ( (LA16_0==MINUS) ) {
                alt16=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 16, 0, input);

                throw nvae;
            }
            switch (alt16) {
                case 1 :
                    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:430:9: cmisTest
                    {
                    pushFollow(FOLLOW_cmisTest_in_cmisPrefixed1556);
                    cmisTest34=cmisTest();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_cmisTest.add(cmisTest34.getTree());


                    // AST REWRITE
                    // elements: cmisTest
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 431:17: -> ^( DEFAULT cmisTest )
                    {
                        // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:432:25: ^( DEFAULT cmisTest )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(DEFAULT, "DEFAULT"), root_1);

                        adaptor.addChild(root_1, stream_cmisTest.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:433:11: MINUS cmisTest
                    {
                    MINUS35=(Token)match(input,MINUS,FOLLOW_MINUS_in_cmisPrefixed1616); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_MINUS.add(MINUS35);

                    pushFollow(FOLLOW_cmisTest_in_cmisPrefixed1618);
                    cmisTest36=cmisTest();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_cmisTest.add(cmisTest36.getTree());


                    // AST REWRITE
                    // elements: cmisTest
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 434:17: -> ^( EXCLUDE cmisTest )
                    {
                        // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:435:25: ^( EXCLUDE cmisTest )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(EXCLUDE, "EXCLUDE"), root_1);

                        adaptor.addChild(root_1, stream_cmisTest.nextTree());

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
    // $ANTLR end "cmisPrefixed"

    public static class ftsTest_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "ftsTest"
    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:442:1: ftsTest : ( ( ftsFieldGroupProximity )=> ftsFieldGroupProximity -> ^( PROXIMITY ftsFieldGroupProximity ) | ftsTerm ( ( fuzzy )=> fuzzy )? -> ^( TERM ftsTerm ( fuzzy )? ) | ftsExactTerm ( ( fuzzy )=> fuzzy )? -> ^( EXACT_TERM ftsExactTerm ( fuzzy )? ) | ftsPhrase ( ( slop )=> slop )? -> ^( PHRASE ftsPhrase ( slop )? ) | ftsSynonym ( ( fuzzy )=> fuzzy )? -> ^( SYNONYM ftsSynonym ( fuzzy )? ) | ftsRange -> ^( RANGE ftsRange ) | ftsFieldGroup -> ftsFieldGroup | LPAREN ftsDisjunction RPAREN -> ftsDisjunction | template -> template );
    public final FTSParser.ftsTest_return ftsTest() throws RecognitionException {
        FTSParser.ftsTest_return retval = new FTSParser.ftsTest_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token LPAREN48=null;
        Token RPAREN50=null;
        FTSParser.ftsFieldGroupProximity_return ftsFieldGroupProximity37 = null;

        FTSParser.ftsTerm_return ftsTerm38 = null;

        FTSParser.fuzzy_return fuzzy39 = null;

        FTSParser.ftsExactTerm_return ftsExactTerm40 = null;

        FTSParser.fuzzy_return fuzzy41 = null;

        FTSParser.ftsPhrase_return ftsPhrase42 = null;

        FTSParser.slop_return slop43 = null;

        FTSParser.ftsSynonym_return ftsSynonym44 = null;

        FTSParser.fuzzy_return fuzzy45 = null;

        FTSParser.ftsRange_return ftsRange46 = null;

        FTSParser.ftsFieldGroup_return ftsFieldGroup47 = null;

        FTSParser.ftsDisjunction_return ftsDisjunction49 = null;

        FTSParser.template_return template51 = null;


        Object LPAREN48_tree=null;
        Object RPAREN50_tree=null;
        RewriteRuleTokenStream stream_RPAREN=new RewriteRuleTokenStream(adaptor,"token RPAREN");
        RewriteRuleTokenStream stream_LPAREN=new RewriteRuleTokenStream(adaptor,"token LPAREN");
        RewriteRuleSubtreeStream stream_template=new RewriteRuleSubtreeStream(adaptor,"rule template");
        RewriteRuleSubtreeStream stream_ftsFieldGroup=new RewriteRuleSubtreeStream(adaptor,"rule ftsFieldGroup");
        RewriteRuleSubtreeStream stream_ftsTerm=new RewriteRuleSubtreeStream(adaptor,"rule ftsTerm");
        RewriteRuleSubtreeStream stream_ftsExactTerm=new RewriteRuleSubtreeStream(adaptor,"rule ftsExactTerm");
        RewriteRuleSubtreeStream stream_fuzzy=new RewriteRuleSubtreeStream(adaptor,"rule fuzzy");
        RewriteRuleSubtreeStream stream_ftsDisjunction=new RewriteRuleSubtreeStream(adaptor,"rule ftsDisjunction");
        RewriteRuleSubtreeStream stream_ftsPhrase=new RewriteRuleSubtreeStream(adaptor,"rule ftsPhrase");
        RewriteRuleSubtreeStream stream_slop=new RewriteRuleSubtreeStream(adaptor,"rule slop");
        RewriteRuleSubtreeStream stream_ftsRange=new RewriteRuleSubtreeStream(adaptor,"rule ftsRange");
        RewriteRuleSubtreeStream stream_ftsSynonym=new RewriteRuleSubtreeStream(adaptor,"rule ftsSynonym");
        RewriteRuleSubtreeStream stream_ftsFieldGroupProximity=new RewriteRuleSubtreeStream(adaptor,"rule ftsFieldGroupProximity");
        try {
            // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:443:9: ( ( ftsFieldGroupProximity )=> ftsFieldGroupProximity -> ^( PROXIMITY ftsFieldGroupProximity ) | ftsTerm ( ( fuzzy )=> fuzzy )? -> ^( TERM ftsTerm ( fuzzy )? ) | ftsExactTerm ( ( fuzzy )=> fuzzy )? -> ^( EXACT_TERM ftsExactTerm ( fuzzy )? ) | ftsPhrase ( ( slop )=> slop )? -> ^( PHRASE ftsPhrase ( slop )? ) | ftsSynonym ( ( fuzzy )=> fuzzy )? -> ^( SYNONYM ftsSynonym ( fuzzy )? ) | ftsRange -> ^( RANGE ftsRange ) | ftsFieldGroup -> ftsFieldGroup | LPAREN ftsDisjunction RPAREN -> ftsDisjunction | template -> template )
            int alt21=9;
            alt21 = dfa21.predict(input);
            switch (alt21) {
                case 1 :
                    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:444:9: ( ftsFieldGroupProximity )=> ftsFieldGroupProximity
                    {
                    pushFollow(FOLLOW_ftsFieldGroupProximity_in_ftsTest1708);
                    ftsFieldGroupProximity37=ftsFieldGroupProximity();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_ftsFieldGroupProximity.add(ftsFieldGroupProximity37.getTree());


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
                    // 445:17: -> ^( PROXIMITY ftsFieldGroupProximity )
                    {
                        // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:446:25: ^( PROXIMITY ftsFieldGroupProximity )
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
                case 2 :
                    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:447:11: ftsTerm ( ( fuzzy )=> fuzzy )?
                    {
                    pushFollow(FOLLOW_ftsTerm_in_ftsTest1768);
                    ftsTerm38=ftsTerm();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_ftsTerm.add(ftsTerm38.getTree());
                    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:447:19: ( ( fuzzy )=> fuzzy )?
                    int alt17=2;
                    alt17 = dfa17.predict(input);
                    switch (alt17) {
                        case 1 :
                            // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:447:21: ( fuzzy )=> fuzzy
                            {
                            pushFollow(FOLLOW_fuzzy_in_ftsTest1778);
                            fuzzy39=fuzzy();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_fuzzy.add(fuzzy39.getTree());

                            }
                            break;

                    }



                    // AST REWRITE
                    // elements: fuzzy, ftsTerm
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 448:17: -> ^( TERM ftsTerm ( fuzzy )? )
                    {
                        // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:449:25: ^( TERM ftsTerm ( fuzzy )? )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(TERM, "TERM"), root_1);

                        adaptor.addChild(root_1, stream_ftsTerm.nextTree());
                        // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:449:40: ( fuzzy )?
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
                    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:450:11: ftsExactTerm ( ( fuzzy )=> fuzzy )?
                    {
                    pushFollow(FOLLOW_ftsExactTerm_in_ftsTest1843);
                    ftsExactTerm40=ftsExactTerm();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_ftsExactTerm.add(ftsExactTerm40.getTree());
                    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:450:24: ( ( fuzzy )=> fuzzy )?
                    int alt18=2;
                    alt18 = dfa18.predict(input);
                    switch (alt18) {
                        case 1 :
                            // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:450:26: ( fuzzy )=> fuzzy
                            {
                            pushFollow(FOLLOW_fuzzy_in_ftsTest1853);
                            fuzzy41=fuzzy();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_fuzzy.add(fuzzy41.getTree());

                            }
                            break;

                    }



                    // AST REWRITE
                    // elements: ftsExactTerm, fuzzy
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 451:17: -> ^( EXACT_TERM ftsExactTerm ( fuzzy )? )
                    {
                        // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:452:25: ^( EXACT_TERM ftsExactTerm ( fuzzy )? )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(EXACT_TERM, "EXACT_TERM"), root_1);

                        adaptor.addChild(root_1, stream_ftsExactTerm.nextTree());
                        // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:452:51: ( fuzzy )?
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
                case 4 :
                    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:453:11: ftsPhrase ( ( slop )=> slop )?
                    {
                    pushFollow(FOLLOW_ftsPhrase_in_ftsTest1918);
                    ftsPhrase42=ftsPhrase();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_ftsPhrase.add(ftsPhrase42.getTree());
                    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:453:21: ( ( slop )=> slop )?
                    int alt19=2;
                    alt19 = dfa19.predict(input);
                    switch (alt19) {
                        case 1 :
                            // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:453:23: ( slop )=> slop
                            {
                            pushFollow(FOLLOW_slop_in_ftsTest1928);
                            slop43=slop();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_slop.add(slop43.getTree());

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
                    // 454:17: -> ^( PHRASE ftsPhrase ( slop )? )
                    {
                        // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:455:25: ^( PHRASE ftsPhrase ( slop )? )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(PHRASE, "PHRASE"), root_1);

                        adaptor.addChild(root_1, stream_ftsPhrase.nextTree());
                        // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:455:44: ( slop )?
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
                case 5 :
                    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:456:11: ftsSynonym ( ( fuzzy )=> fuzzy )?
                    {
                    pushFollow(FOLLOW_ftsSynonym_in_ftsTest1993);
                    ftsSynonym44=ftsSynonym();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_ftsSynonym.add(ftsSynonym44.getTree());
                    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:456:22: ( ( fuzzy )=> fuzzy )?
                    int alt20=2;
                    alt20 = dfa20.predict(input);
                    switch (alt20) {
                        case 1 :
                            // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:456:24: ( fuzzy )=> fuzzy
                            {
                            pushFollow(FOLLOW_fuzzy_in_ftsTest2003);
                            fuzzy45=fuzzy();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_fuzzy.add(fuzzy45.getTree());

                            }
                            break;

                    }



                    // AST REWRITE
                    // elements: ftsSynonym, fuzzy
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 457:17: -> ^( SYNONYM ftsSynonym ( fuzzy )? )
                    {
                        // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:458:25: ^( SYNONYM ftsSynonym ( fuzzy )? )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(SYNONYM, "SYNONYM"), root_1);

                        adaptor.addChild(root_1, stream_ftsSynonym.nextTree());
                        // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:458:46: ( fuzzy )?
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
                case 6 :
                    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:459:11: ftsRange
                    {
                    pushFollow(FOLLOW_ftsRange_in_ftsTest2068);
                    ftsRange46=ftsRange();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_ftsRange.add(ftsRange46.getTree());


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
                    // 460:17: -> ^( RANGE ftsRange )
                    {
                        // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:461:25: ^( RANGE ftsRange )
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
                    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:462:11: ftsFieldGroup
                    {
                    pushFollow(FOLLOW_ftsFieldGroup_in_ftsTest2128);
                    ftsFieldGroup47=ftsFieldGroup();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_ftsFieldGroup.add(ftsFieldGroup47.getTree());


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
                    // 463:17: -> ftsFieldGroup
                    {
                        adaptor.addChild(root_0, stream_ftsFieldGroup.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 8 :
                    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:464:11: LPAREN ftsDisjunction RPAREN
                    {
                    LPAREN48=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_ftsTest2160); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_LPAREN.add(LPAREN48);

                    pushFollow(FOLLOW_ftsDisjunction_in_ftsTest2162);
                    ftsDisjunction49=ftsDisjunction();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_ftsDisjunction.add(ftsDisjunction49.getTree());
                    RPAREN50=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_ftsTest2164); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_RPAREN.add(RPAREN50);



                    // AST REWRITE
                    // elements: ftsDisjunction
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 465:17: -> ftsDisjunction
                    {
                        adaptor.addChild(root_0, stream_ftsDisjunction.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 9 :
                    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:466:11: template
                    {
                    pushFollow(FOLLOW_template_in_ftsTest2196);
                    template51=template();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_template.add(template51.getTree());


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
                    // 467:17: -> template
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

    public static class cmisTest_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "cmisTest"
    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:470:1: cmisTest : ( cmisTerm -> ^( TERM cmisTerm ) | cmisPhrase -> ^( PHRASE cmisPhrase ) );
    public final FTSParser.cmisTest_return cmisTest() throws RecognitionException {
        FTSParser.cmisTest_return retval = new FTSParser.cmisTest_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        FTSParser.cmisTerm_return cmisTerm52 = null;

        FTSParser.cmisPhrase_return cmisPhrase53 = null;


        RewriteRuleSubtreeStream stream_cmisPhrase=new RewriteRuleSubtreeStream(adaptor,"rule cmisPhrase");
        RewriteRuleSubtreeStream stream_cmisTerm=new RewriteRuleSubtreeStream(adaptor,"rule cmisTerm");
        try {
            // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:471:9: ( cmisTerm -> ^( TERM cmisTerm ) | cmisPhrase -> ^( PHRASE cmisPhrase ) )
            int alt22=2;
            int LA22_0 = input.LA(1);

            if ( (LA22_0==DECIMAL_INTEGER_LITERAL||(LA22_0>=ID && LA22_0<=STAR)||LA22_0==QUESTION_MARK) ) {
                alt22=1;
            }
            else if ( (LA22_0==FTSPHRASE) ) {
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
                    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:472:9: cmisTerm
                    {
                    pushFollow(FOLLOW_cmisTerm_in_cmisTest2249);
                    cmisTerm52=cmisTerm();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_cmisTerm.add(cmisTerm52.getTree());


                    // AST REWRITE
                    // elements: cmisTerm
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 473:17: -> ^( TERM cmisTerm )
                    {
                        // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:474:25: ^( TERM cmisTerm )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(TERM, "TERM"), root_1);

                        adaptor.addChild(root_1, stream_cmisTerm.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:475:11: cmisPhrase
                    {
                    pushFollow(FOLLOW_cmisPhrase_in_cmisTest2309);
                    cmisPhrase53=cmisPhrase();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_cmisPhrase.add(cmisPhrase53.getTree());


                    // AST REWRITE
                    // elements: cmisPhrase
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 476:17: -> ^( PHRASE cmisPhrase )
                    {
                        // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:477:25: ^( PHRASE cmisPhrase )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(PHRASE, "PHRASE"), root_1);

                        adaptor.addChild(root_1, stream_cmisPhrase.nextTree());

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
    // $ANTLR end "cmisTest"

    public static class template_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "template"
    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:480:1: template : ( PERCENT tempReference -> ^( TEMPLATE tempReference ) | PERCENT LPAREN ( tempReference ( COMMA )? )+ RPAREN -> ^( TEMPLATE ( tempReference )+ ) );
    public final FTSParser.template_return template() throws RecognitionException {
        FTSParser.template_return retval = new FTSParser.template_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token PERCENT54=null;
        Token PERCENT56=null;
        Token LPAREN57=null;
        Token COMMA59=null;
        Token RPAREN60=null;
        FTSParser.tempReference_return tempReference55 = null;

        FTSParser.tempReference_return tempReference58 = null;


        Object PERCENT54_tree=null;
        Object PERCENT56_tree=null;
        Object LPAREN57_tree=null;
        Object COMMA59_tree=null;
        Object RPAREN60_tree=null;
        RewriteRuleTokenStream stream_RPAREN=new RewriteRuleTokenStream(adaptor,"token RPAREN");
        RewriteRuleTokenStream stream_PERCENT=new RewriteRuleTokenStream(adaptor,"token PERCENT");
        RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
        RewriteRuleTokenStream stream_LPAREN=new RewriteRuleTokenStream(adaptor,"token LPAREN");
        RewriteRuleSubtreeStream stream_tempReference=new RewriteRuleSubtreeStream(adaptor,"rule tempReference");
        try {
            // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:481:9: ( PERCENT tempReference -> ^( TEMPLATE tempReference ) | PERCENT LPAREN ( tempReference ( COMMA )? )+ RPAREN -> ^( TEMPLATE ( tempReference )+ ) )
            int alt25=2;
            int LA25_0 = input.LA(1);

            if ( (LA25_0==PERCENT) ) {
                switch ( input.LA(2) ) {
                case LPAREN:
                    {
                    alt25=2;
                    }
                    break;
                case AT:
                    {
                    alt25=1;
                    }
                    break;
                case ID:
                    {
                    alt25=1;
                    }
                    break;
                case URI:
                    {
                    alt25=1;
                    }
                    break;
                default:
                    if (state.backtracking>0) {state.failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 25, 1, input);

                    throw nvae;
                }

            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 25, 0, input);

                throw nvae;
            }
            switch (alt25) {
                case 1 :
                    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:482:9: PERCENT tempReference
                    {
                    PERCENT54=(Token)match(input,PERCENT,FOLLOW_PERCENT_in_template2390); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_PERCENT.add(PERCENT54);

                    pushFollow(FOLLOW_tempReference_in_template2392);
                    tempReference55=tempReference();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_tempReference.add(tempReference55.getTree());


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
                    // 483:17: -> ^( TEMPLATE tempReference )
                    {
                        // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:484:25: ^( TEMPLATE tempReference )
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
                    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:485:11: PERCENT LPAREN ( tempReference ( COMMA )? )+ RPAREN
                    {
                    PERCENT56=(Token)match(input,PERCENT,FOLLOW_PERCENT_in_template2452); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_PERCENT.add(PERCENT56);

                    LPAREN57=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_template2454); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_LPAREN.add(LPAREN57);

                    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:485:26: ( tempReference ( COMMA )? )+
                    int cnt24=0;
                    loop24:
                    do {
                        int alt24=2;
                        int LA24_0 = input.LA(1);

                        if ( (LA24_0==ID||(LA24_0>=AT && LA24_0<=URI)) ) {
                            alt24=1;
                        }


                        switch (alt24) {
                    	case 1 :
                    	    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:485:27: tempReference ( COMMA )?
                    	    {
                    	    pushFollow(FOLLOW_tempReference_in_template2457);
                    	    tempReference58=tempReference();

                    	    state._fsp--;
                    	    if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) stream_tempReference.add(tempReference58.getTree());
                    	    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:485:41: ( COMMA )?
                    	    int alt23=2;
                    	    int LA23_0 = input.LA(1);

                    	    if ( (LA23_0==COMMA) ) {
                    	        alt23=1;
                    	    }
                    	    switch (alt23) {
                    	        case 1 :
                    	            // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:485:41: COMMA
                    	            {
                    	            COMMA59=(Token)match(input,COMMA,FOLLOW_COMMA_in_template2459); if (state.failed) return retval; 
                    	            if ( state.backtracking==0 ) stream_COMMA.add(COMMA59);


                    	            }
                    	            break;

                    	    }


                    	    }
                    	    break;

                    	default :
                    	    if ( cnt24 >= 1 ) break loop24;
                    	    if (state.backtracking>0) {state.failed=true; return retval;}
                                EarlyExitException eee =
                                    new EarlyExitException(24, input);
                                throw eee;
                        }
                        cnt24++;
                    } while (true);

                    RPAREN60=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_template2464); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_RPAREN.add(RPAREN60);



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
                    // 486:17: -> ^( TEMPLATE ( tempReference )+ )
                    {
                        // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:487:25: ^( TEMPLATE ( tempReference )+ )
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
    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:490:1: fuzzy : TILDA number -> ^( FUZZY number ) ;
    public final FTSParser.fuzzy_return fuzzy() throws RecognitionException {
        FTSParser.fuzzy_return retval = new FTSParser.fuzzy_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token TILDA61=null;
        FTSParser.number_return number62 = null;


        Object TILDA61_tree=null;
        RewriteRuleTokenStream stream_TILDA=new RewriteRuleTokenStream(adaptor,"token TILDA");
        RewriteRuleSubtreeStream stream_number=new RewriteRuleSubtreeStream(adaptor,"rule number");
        try {
            // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:491:9: ( TILDA number -> ^( FUZZY number ) )
            // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:492:9: TILDA number
            {
            TILDA61=(Token)match(input,TILDA,FOLLOW_TILDA_in_fuzzy2546); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_TILDA.add(TILDA61);

            pushFollow(FOLLOW_number_in_fuzzy2548);
            number62=number();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_number.add(number62.getTree());


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
            // 493:17: -> ^( FUZZY number )
            {
                // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:494:25: ^( FUZZY number )
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
    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:497:1: slop : TILDA DECIMAL_INTEGER_LITERAL -> ^( FUZZY DECIMAL_INTEGER_LITERAL ) ;
    public final FTSParser.slop_return slop() throws RecognitionException {
        FTSParser.slop_return retval = new FTSParser.slop_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token TILDA63=null;
        Token DECIMAL_INTEGER_LITERAL64=null;

        Object TILDA63_tree=null;
        Object DECIMAL_INTEGER_LITERAL64_tree=null;
        RewriteRuleTokenStream stream_TILDA=new RewriteRuleTokenStream(adaptor,"token TILDA");
        RewriteRuleTokenStream stream_DECIMAL_INTEGER_LITERAL=new RewriteRuleTokenStream(adaptor,"token DECIMAL_INTEGER_LITERAL");

        try {
            // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:498:9: ( TILDA DECIMAL_INTEGER_LITERAL -> ^( FUZZY DECIMAL_INTEGER_LITERAL ) )
            // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:499:9: TILDA DECIMAL_INTEGER_LITERAL
            {
            TILDA63=(Token)match(input,TILDA,FOLLOW_TILDA_in_slop2629); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_TILDA.add(TILDA63);

            DECIMAL_INTEGER_LITERAL64=(Token)match(input,DECIMAL_INTEGER_LITERAL,FOLLOW_DECIMAL_INTEGER_LITERAL_in_slop2631); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_DECIMAL_INTEGER_LITERAL.add(DECIMAL_INTEGER_LITERAL64);



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
            // 500:17: -> ^( FUZZY DECIMAL_INTEGER_LITERAL )
            {
                // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:501:25: ^( FUZZY DECIMAL_INTEGER_LITERAL )
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
    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:504:1: boost : CARAT number -> ^( BOOST number ) ;
    public final FTSParser.boost_return boost() throws RecognitionException {
        FTSParser.boost_return retval = new FTSParser.boost_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token CARAT65=null;
        FTSParser.number_return number66 = null;


        Object CARAT65_tree=null;
        RewriteRuleTokenStream stream_CARAT=new RewriteRuleTokenStream(adaptor,"token CARAT");
        RewriteRuleSubtreeStream stream_number=new RewriteRuleSubtreeStream(adaptor,"rule number");
        try {
            // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:505:9: ( CARAT number -> ^( BOOST number ) )
            // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:506:9: CARAT number
            {
            CARAT65=(Token)match(input,CARAT,FOLLOW_CARAT_in_boost2712); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_CARAT.add(CARAT65);

            pushFollow(FOLLOW_number_in_boost2714);
            number66=number();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_number.add(number66.getTree());


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
            // 507:17: -> ^( BOOST number )
            {
                // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:508:25: ^( BOOST number )
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
    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:511:1: ftsTerm : ( fieldReference COLON )? ftsWord -> ftsWord ( fieldReference )? ;
    public final FTSParser.ftsTerm_return ftsTerm() throws RecognitionException {
        FTSParser.ftsTerm_return retval = new FTSParser.ftsTerm_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token COLON68=null;
        FTSParser.fieldReference_return fieldReference67 = null;

        FTSParser.ftsWord_return ftsWord69 = null;


        Object COLON68_tree=null;
        RewriteRuleTokenStream stream_COLON=new RewriteRuleTokenStream(adaptor,"token COLON");
        RewriteRuleSubtreeStream stream_ftsWord=new RewriteRuleSubtreeStream(adaptor,"rule ftsWord");
        RewriteRuleSubtreeStream stream_fieldReference=new RewriteRuleSubtreeStream(adaptor,"rule fieldReference");
        try {
            // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:512:9: ( ( fieldReference COLON )? ftsWord -> ftsWord ( fieldReference )? )
            // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:513:9: ( fieldReference COLON )? ftsWord
            {
            // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:513:9: ( fieldReference COLON )?
            int alt26=2;
            alt26 = dfa26.predict(input);
            switch (alt26) {
                case 1 :
                    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:513:10: fieldReference COLON
                    {
                    pushFollow(FOLLOW_fieldReference_in_ftsTerm2796);
                    fieldReference67=fieldReference();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_fieldReference.add(fieldReference67.getTree());
                    COLON68=(Token)match(input,COLON,FOLLOW_COLON_in_ftsTerm2798); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_COLON.add(COLON68);


                    }
                    break;

            }

            pushFollow(FOLLOW_ftsWord_in_ftsTerm2802);
            ftsWord69=ftsWord();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_ftsWord.add(ftsWord69.getTree());


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
            // 514:17: -> ftsWord ( fieldReference )?
            {
                adaptor.addChild(root_0, stream_ftsWord.nextTree());
                // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:514:28: ( fieldReference )?
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

    public static class cmisTerm_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "cmisTerm"
    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:517:1: cmisTerm : ftsWord -> ftsWord ;
    public final FTSParser.cmisTerm_return cmisTerm() throws RecognitionException {
        FTSParser.cmisTerm_return retval = new FTSParser.cmisTerm_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        FTSParser.ftsWord_return ftsWord70 = null;


        RewriteRuleSubtreeStream stream_ftsWord=new RewriteRuleSubtreeStream(adaptor,"rule ftsWord");
        try {
            // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:518:9: ( ftsWord -> ftsWord )
            // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:519:9: ftsWord
            {
            pushFollow(FOLLOW_ftsWord_in_cmisTerm2858);
            ftsWord70=ftsWord();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_ftsWord.add(ftsWord70.getTree());


            // AST REWRITE
            // elements: ftsWord
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 520:17: -> ftsWord
            {
                adaptor.addChild(root_0, stream_ftsWord.nextTree());

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
    // $ANTLR end "cmisTerm"

    public static class ftsExactTerm_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "ftsExactTerm"
    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:523:1: ftsExactTerm : EQUALS ftsTerm -> ftsTerm ;
    public final FTSParser.ftsExactTerm_return ftsExactTerm() throws RecognitionException {
        FTSParser.ftsExactTerm_return retval = new FTSParser.ftsExactTerm_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token EQUALS71=null;
        FTSParser.ftsTerm_return ftsTerm72 = null;


        Object EQUALS71_tree=null;
        RewriteRuleTokenStream stream_EQUALS=new RewriteRuleTokenStream(adaptor,"token EQUALS");
        RewriteRuleSubtreeStream stream_ftsTerm=new RewriteRuleSubtreeStream(adaptor,"rule ftsTerm");
        try {
            // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:524:9: ( EQUALS ftsTerm -> ftsTerm )
            // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:525:9: EQUALS ftsTerm
            {
            EQUALS71=(Token)match(input,EQUALS,FOLLOW_EQUALS_in_ftsExactTerm2911); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_EQUALS.add(EQUALS71);

            pushFollow(FOLLOW_ftsTerm_in_ftsExactTerm2913);
            ftsTerm72=ftsTerm();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_ftsTerm.add(ftsTerm72.getTree());


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
            // 526:17: -> ftsTerm
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
    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:529:1: ftsPhrase : ( fieldReference COLON )? FTSPHRASE -> FTSPHRASE ( fieldReference )? ;
    public final FTSParser.ftsPhrase_return ftsPhrase() throws RecognitionException {
        FTSParser.ftsPhrase_return retval = new FTSParser.ftsPhrase_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token COLON74=null;
        Token FTSPHRASE75=null;
        FTSParser.fieldReference_return fieldReference73 = null;


        Object COLON74_tree=null;
        Object FTSPHRASE75_tree=null;
        RewriteRuleTokenStream stream_COLON=new RewriteRuleTokenStream(adaptor,"token COLON");
        RewriteRuleTokenStream stream_FTSPHRASE=new RewriteRuleTokenStream(adaptor,"token FTSPHRASE");
        RewriteRuleSubtreeStream stream_fieldReference=new RewriteRuleSubtreeStream(adaptor,"rule fieldReference");
        try {
            // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:530:9: ( ( fieldReference COLON )? FTSPHRASE -> FTSPHRASE ( fieldReference )? )
            // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:531:9: ( fieldReference COLON )? FTSPHRASE
            {
            // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:531:9: ( fieldReference COLON )?
            int alt27=2;
            int LA27_0 = input.LA(1);

            if ( (LA27_0==ID||(LA27_0>=AT && LA27_0<=URI)) ) {
                alt27=1;
            }
            switch (alt27) {
                case 1 :
                    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:531:10: fieldReference COLON
                    {
                    pushFollow(FOLLOW_fieldReference_in_ftsPhrase2967);
                    fieldReference73=fieldReference();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_fieldReference.add(fieldReference73.getTree());
                    COLON74=(Token)match(input,COLON,FOLLOW_COLON_in_ftsPhrase2969); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_COLON.add(COLON74);


                    }
                    break;

            }

            FTSPHRASE75=(Token)match(input,FTSPHRASE,FOLLOW_FTSPHRASE_in_ftsPhrase2973); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_FTSPHRASE.add(FTSPHRASE75);



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
            // 532:17: -> FTSPHRASE ( fieldReference )?
            {
                adaptor.addChild(root_0, stream_FTSPHRASE.nextNode());
                // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:532:30: ( fieldReference )?
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

    public static class cmisPhrase_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "cmisPhrase"
    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:535:1: cmisPhrase : FTSPHRASE -> FTSPHRASE ;
    public final FTSParser.cmisPhrase_return cmisPhrase() throws RecognitionException {
        FTSParser.cmisPhrase_return retval = new FTSParser.cmisPhrase_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token FTSPHRASE76=null;

        Object FTSPHRASE76_tree=null;
        RewriteRuleTokenStream stream_FTSPHRASE=new RewriteRuleTokenStream(adaptor,"token FTSPHRASE");

        try {
            // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:536:9: ( FTSPHRASE -> FTSPHRASE )
            // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:537:9: FTSPHRASE
            {
            FTSPHRASE76=(Token)match(input,FTSPHRASE,FOLLOW_FTSPHRASE_in_cmisPhrase3029); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_FTSPHRASE.add(FTSPHRASE76);



            // AST REWRITE
            // elements: FTSPHRASE
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 538:17: -> FTSPHRASE
            {
                adaptor.addChild(root_0, stream_FTSPHRASE.nextNode());

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
    // $ANTLR end "cmisPhrase"

    public static class ftsSynonym_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "ftsSynonym"
    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:541:1: ftsSynonym : TILDA ftsTerm -> ftsTerm ;
    public final FTSParser.ftsSynonym_return ftsSynonym() throws RecognitionException {
        FTSParser.ftsSynonym_return retval = new FTSParser.ftsSynonym_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token TILDA77=null;
        FTSParser.ftsTerm_return ftsTerm78 = null;


        Object TILDA77_tree=null;
        RewriteRuleTokenStream stream_TILDA=new RewriteRuleTokenStream(adaptor,"token TILDA");
        RewriteRuleSubtreeStream stream_ftsTerm=new RewriteRuleSubtreeStream(adaptor,"rule ftsTerm");
        try {
            // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:542:9: ( TILDA ftsTerm -> ftsTerm )
            // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:543:9: TILDA ftsTerm
            {
            TILDA77=(Token)match(input,TILDA,FOLLOW_TILDA_in_ftsSynonym3082); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_TILDA.add(TILDA77);

            pushFollow(FOLLOW_ftsTerm_in_ftsSynonym3084);
            ftsTerm78=ftsTerm();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_ftsTerm.add(ftsTerm78.getTree());


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
            // 544:17: -> ftsTerm
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
    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:547:1: ftsRange : ( fieldReference COLON )? ftsFieldGroupRange -> ftsFieldGroupRange ( fieldReference )? ;
    public final FTSParser.ftsRange_return ftsRange() throws RecognitionException {
        FTSParser.ftsRange_return retval = new FTSParser.ftsRange_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token COLON80=null;
        FTSParser.fieldReference_return fieldReference79 = null;

        FTSParser.ftsFieldGroupRange_return ftsFieldGroupRange81 = null;


        Object COLON80_tree=null;
        RewriteRuleTokenStream stream_COLON=new RewriteRuleTokenStream(adaptor,"token COLON");
        RewriteRuleSubtreeStream stream_ftsFieldGroupRange=new RewriteRuleSubtreeStream(adaptor,"rule ftsFieldGroupRange");
        RewriteRuleSubtreeStream stream_fieldReference=new RewriteRuleSubtreeStream(adaptor,"rule fieldReference");
        try {
            // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:548:9: ( ( fieldReference COLON )? ftsFieldGroupRange -> ftsFieldGroupRange ( fieldReference )? )
            // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:549:9: ( fieldReference COLON )? ftsFieldGroupRange
            {
            // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:549:9: ( fieldReference COLON )?
            int alt28=2;
            switch ( input.LA(1) ) {
                case AT:
                    {
                    alt28=1;
                    }
                    break;
                case ID:
                    {
                    int LA28_2 = input.LA(2);

                    if ( (LA28_2==COLON) ) {
                        alt28=1;
                    }
                    }
                    break;
                case URI:
                    {
                    alt28=1;
                    }
                    break;
            }

            switch (alt28) {
                case 1 :
                    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:549:10: fieldReference COLON
                    {
                    pushFollow(FOLLOW_fieldReference_in_ftsRange3138);
                    fieldReference79=fieldReference();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_fieldReference.add(fieldReference79.getTree());
                    COLON80=(Token)match(input,COLON,FOLLOW_COLON_in_ftsRange3140); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_COLON.add(COLON80);


                    }
                    break;

            }

            pushFollow(FOLLOW_ftsFieldGroupRange_in_ftsRange3144);
            ftsFieldGroupRange81=ftsFieldGroupRange();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_ftsFieldGroupRange.add(ftsFieldGroupRange81.getTree());


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
            // 550:17: -> ftsFieldGroupRange ( fieldReference )?
            {
                adaptor.addChild(root_0, stream_ftsFieldGroupRange.nextTree());
                // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:550:39: ( fieldReference )?
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
    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:553:1: ftsFieldGroup : fieldReference COLON LPAREN ftsFieldGroupDisjunction RPAREN -> ^( FIELD_GROUP fieldReference ftsFieldGroupDisjunction ) ;
    public final FTSParser.ftsFieldGroup_return ftsFieldGroup() throws RecognitionException {
        FTSParser.ftsFieldGroup_return retval = new FTSParser.ftsFieldGroup_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token COLON83=null;
        Token LPAREN84=null;
        Token RPAREN86=null;
        FTSParser.fieldReference_return fieldReference82 = null;

        FTSParser.ftsFieldGroupDisjunction_return ftsFieldGroupDisjunction85 = null;


        Object COLON83_tree=null;
        Object LPAREN84_tree=null;
        Object RPAREN86_tree=null;
        RewriteRuleTokenStream stream_COLON=new RewriteRuleTokenStream(adaptor,"token COLON");
        RewriteRuleTokenStream stream_RPAREN=new RewriteRuleTokenStream(adaptor,"token RPAREN");
        RewriteRuleTokenStream stream_LPAREN=new RewriteRuleTokenStream(adaptor,"token LPAREN");
        RewriteRuleSubtreeStream stream_fieldReference=new RewriteRuleSubtreeStream(adaptor,"rule fieldReference");
        RewriteRuleSubtreeStream stream_ftsFieldGroupDisjunction=new RewriteRuleSubtreeStream(adaptor,"rule ftsFieldGroupDisjunction");
        try {
            // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:554:9: ( fieldReference COLON LPAREN ftsFieldGroupDisjunction RPAREN -> ^( FIELD_GROUP fieldReference ftsFieldGroupDisjunction ) )
            // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:555:9: fieldReference COLON LPAREN ftsFieldGroupDisjunction RPAREN
            {
            pushFollow(FOLLOW_fieldReference_in_ftsFieldGroup3200);
            fieldReference82=fieldReference();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_fieldReference.add(fieldReference82.getTree());
            COLON83=(Token)match(input,COLON,FOLLOW_COLON_in_ftsFieldGroup3202); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_COLON.add(COLON83);

            LPAREN84=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_ftsFieldGroup3204); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_LPAREN.add(LPAREN84);

            pushFollow(FOLLOW_ftsFieldGroupDisjunction_in_ftsFieldGroup3206);
            ftsFieldGroupDisjunction85=ftsFieldGroupDisjunction();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_ftsFieldGroupDisjunction.add(ftsFieldGroupDisjunction85.getTree());
            RPAREN86=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_ftsFieldGroup3208); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_RPAREN.add(RPAREN86);



            // AST REWRITE
            // elements: ftsFieldGroupDisjunction, fieldReference
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 556:17: -> ^( FIELD_GROUP fieldReference ftsFieldGroupDisjunction )
            {
                // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:557:25: ^( FIELD_GROUP fieldReference ftsFieldGroupDisjunction )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(FIELD_GROUP, "FIELD_GROUP"), root_1);

                adaptor.addChild(root_1, stream_fieldReference.nextTree());
                adaptor.addChild(root_1, stream_ftsFieldGroupDisjunction.nextTree());

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

    public static class ftsFieldGroupDisjunction_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "ftsFieldGroupDisjunction"
    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:560:1: ftsFieldGroupDisjunction : ({...}? => ftsFieldGroupExplicitDisjunction | {...}? => ftsFieldGroupImplicitDisjunction );
    public final FTSParser.ftsFieldGroupDisjunction_return ftsFieldGroupDisjunction() throws RecognitionException {
        FTSParser.ftsFieldGroupDisjunction_return retval = new FTSParser.ftsFieldGroupDisjunction_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        FTSParser.ftsFieldGroupExplicitDisjunction_return ftsFieldGroupExplicitDisjunction87 = null;

        FTSParser.ftsFieldGroupImplicitDisjunction_return ftsFieldGroupImplicitDisjunction88 = null;



        try {
            // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:561:9: ({...}? => ftsFieldGroupExplicitDisjunction | {...}? => ftsFieldGroupImplicitDisjunction )
            int alt29=2;
            alt29 = dfa29.predict(input);
            switch (alt29) {
                case 1 :
                    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:562:9: {...}? => ftsFieldGroupExplicitDisjunction
                    {
                    root_0 = (Object)adaptor.nil();

                    if ( !((defaultFieldConjunction() == true)) ) {
                        if (state.backtracking>0) {state.failed=true; return retval;}
                        throw new FailedPredicateException(input, "ftsFieldGroupDisjunction", "defaultFieldConjunction() == true");
                    }
                    pushFollow(FOLLOW_ftsFieldGroupExplicitDisjunction_in_ftsFieldGroupDisjunction3294);
                    ftsFieldGroupExplicitDisjunction87=ftsFieldGroupExplicitDisjunction();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, ftsFieldGroupExplicitDisjunction87.getTree());

                    }
                    break;
                case 2 :
                    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:563:11: {...}? => ftsFieldGroupImplicitDisjunction
                    {
                    root_0 = (Object)adaptor.nil();

                    if ( !((defaultFieldConjunction() == false)) ) {
                        if (state.backtracking>0) {state.failed=true; return retval;}
                        throw new FailedPredicateException(input, "ftsFieldGroupDisjunction", "defaultFieldConjunction() == false");
                    }
                    pushFollow(FOLLOW_ftsFieldGroupImplicitDisjunction_in_ftsFieldGroupDisjunction3309);
                    ftsFieldGroupImplicitDisjunction88=ftsFieldGroupImplicitDisjunction();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, ftsFieldGroupImplicitDisjunction88.getTree());

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
    // $ANTLR end "ftsFieldGroupDisjunction"

    public static class ftsFieldGroupExplicitDisjunction_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "ftsFieldGroupExplicitDisjunction"
    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:566:1: ftsFieldGroupExplicitDisjunction : ftsFieldGroupImplicitConjunction ( or ftsFieldGroupImplicitConjunction )* -> ^( FIELD_DISJUNCTION ( ftsFieldGroupImplicitConjunction )+ ) ;
    public final FTSParser.ftsFieldGroupExplicitDisjunction_return ftsFieldGroupExplicitDisjunction() throws RecognitionException {
        FTSParser.ftsFieldGroupExplicitDisjunction_return retval = new FTSParser.ftsFieldGroupExplicitDisjunction_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        FTSParser.ftsFieldGroupImplicitConjunction_return ftsFieldGroupImplicitConjunction89 = null;

        FTSParser.or_return or90 = null;

        FTSParser.ftsFieldGroupImplicitConjunction_return ftsFieldGroupImplicitConjunction91 = null;


        RewriteRuleSubtreeStream stream_ftsFieldGroupImplicitConjunction=new RewriteRuleSubtreeStream(adaptor,"rule ftsFieldGroupImplicitConjunction");
        RewriteRuleSubtreeStream stream_or=new RewriteRuleSubtreeStream(adaptor,"rule or");
        try {
            // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:567:9: ( ftsFieldGroupImplicitConjunction ( or ftsFieldGroupImplicitConjunction )* -> ^( FIELD_DISJUNCTION ( ftsFieldGroupImplicitConjunction )+ ) )
            // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:568:9: ftsFieldGroupImplicitConjunction ( or ftsFieldGroupImplicitConjunction )*
            {
            pushFollow(FOLLOW_ftsFieldGroupImplicitConjunction_in_ftsFieldGroupExplicitDisjunction3342);
            ftsFieldGroupImplicitConjunction89=ftsFieldGroupImplicitConjunction();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_ftsFieldGroupImplicitConjunction.add(ftsFieldGroupImplicitConjunction89.getTree());
            // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:568:42: ( or ftsFieldGroupImplicitConjunction )*
            loop30:
            do {
                int alt30=2;
                int LA30_0 = input.LA(1);

                if ( (LA30_0==BAR||LA30_0==OR) ) {
                    alt30=1;
                }


                switch (alt30) {
            	case 1 :
            	    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:568:43: or ftsFieldGroupImplicitConjunction
            	    {
            	    pushFollow(FOLLOW_or_in_ftsFieldGroupExplicitDisjunction3345);
            	    or90=or();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_or.add(or90.getTree());
            	    pushFollow(FOLLOW_ftsFieldGroupImplicitConjunction_in_ftsFieldGroupExplicitDisjunction3347);
            	    ftsFieldGroupImplicitConjunction91=ftsFieldGroupImplicitConjunction();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_ftsFieldGroupImplicitConjunction.add(ftsFieldGroupImplicitConjunction91.getTree());

            	    }
            	    break;

            	default :
            	    break loop30;
                }
            } while (true);



            // AST REWRITE
            // elements: ftsFieldGroupImplicitConjunction
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 569:17: -> ^( FIELD_DISJUNCTION ( ftsFieldGroupImplicitConjunction )+ )
            {
                // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:570:25: ^( FIELD_DISJUNCTION ( ftsFieldGroupImplicitConjunction )+ )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(FIELD_DISJUNCTION, "FIELD_DISJUNCTION"), root_1);

                if ( !(stream_ftsFieldGroupImplicitConjunction.hasNext()) ) {
                    throw new RewriteEarlyExitException();
                }
                while ( stream_ftsFieldGroupImplicitConjunction.hasNext() ) {
                    adaptor.addChild(root_1, stream_ftsFieldGroupImplicitConjunction.nextTree());

                }
                stream_ftsFieldGroupImplicitConjunction.reset();

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

    public static class ftsFieldGroupImplicitDisjunction_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "ftsFieldGroupImplicitDisjunction"
    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:573:1: ftsFieldGroupImplicitDisjunction : ( ( or )? ftsFieldGroupExplicitConjunction )+ -> ^( FIELD_DISJUNCTION ( ftsFieldGroupExplicitConjunction )+ ) ;
    public final FTSParser.ftsFieldGroupImplicitDisjunction_return ftsFieldGroupImplicitDisjunction() throws RecognitionException {
        FTSParser.ftsFieldGroupImplicitDisjunction_return retval = new FTSParser.ftsFieldGroupImplicitDisjunction_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        FTSParser.or_return or92 = null;

        FTSParser.ftsFieldGroupExplicitConjunction_return ftsFieldGroupExplicitConjunction93 = null;


        RewriteRuleSubtreeStream stream_or=new RewriteRuleSubtreeStream(adaptor,"rule or");
        RewriteRuleSubtreeStream stream_ftsFieldGroupExplicitConjunction=new RewriteRuleSubtreeStream(adaptor,"rule ftsFieldGroupExplicitConjunction");
        try {
            // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:574:9: ( ( ( or )? ftsFieldGroupExplicitConjunction )+ -> ^( FIELD_DISJUNCTION ( ftsFieldGroupExplicitConjunction )+ ) )
            // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:575:9: ( ( or )? ftsFieldGroupExplicitConjunction )+
            {
            // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:575:9: ( ( or )? ftsFieldGroupExplicitConjunction )+
            int cnt32=0;
            loop32:
            do {
                int alt32=2;
                alt32 = dfa32.predict(input);
                switch (alt32) {
            	case 1 :
            	    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:575:10: ( or )? ftsFieldGroupExplicitConjunction
            	    {
            	    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:575:10: ( or )?
            	    int alt31=2;
            	    alt31 = dfa31.predict(input);
            	    switch (alt31) {
            	        case 1 :
            	            // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:575:10: or
            	            {
            	            pushFollow(FOLLOW_or_in_ftsFieldGroupImplicitDisjunction3432);
            	            or92=or();

            	            state._fsp--;
            	            if (state.failed) return retval;
            	            if ( state.backtracking==0 ) stream_or.add(or92.getTree());

            	            }
            	            break;

            	    }

            	    pushFollow(FOLLOW_ftsFieldGroupExplicitConjunction_in_ftsFieldGroupImplicitDisjunction3435);
            	    ftsFieldGroupExplicitConjunction93=ftsFieldGroupExplicitConjunction();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_ftsFieldGroupExplicitConjunction.add(ftsFieldGroupExplicitConjunction93.getTree());

            	    }
            	    break;

            	default :
            	    if ( cnt32 >= 1 ) break loop32;
            	    if (state.backtracking>0) {state.failed=true; return retval;}
                        EarlyExitException eee =
                            new EarlyExitException(32, input);
                        throw eee;
                }
                cnt32++;
            } while (true);



            // AST REWRITE
            // elements: ftsFieldGroupExplicitConjunction
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 576:17: -> ^( FIELD_DISJUNCTION ( ftsFieldGroupExplicitConjunction )+ )
            {
                // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:577:25: ^( FIELD_DISJUNCTION ( ftsFieldGroupExplicitConjunction )+ )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(FIELD_DISJUNCTION, "FIELD_DISJUNCTION"), root_1);

                if ( !(stream_ftsFieldGroupExplicitConjunction.hasNext()) ) {
                    throw new RewriteEarlyExitException();
                }
                while ( stream_ftsFieldGroupExplicitConjunction.hasNext() ) {
                    adaptor.addChild(root_1, stream_ftsFieldGroupExplicitConjunction.nextTree());

                }
                stream_ftsFieldGroupExplicitConjunction.reset();

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
    // $ANTLR end "ftsFieldGroupImplicitDisjunction"

    public static class ftsFieldGroupExplicitConjunction_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "ftsFieldGroupExplicitConjunction"
    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:584:1: ftsFieldGroupExplicitConjunction : ftsFieldGroupPrefixed ( and ftsFieldGroupPrefixed )* -> ^( FIELD_CONJUNCTION ( ftsFieldGroupPrefixed )+ ) ;
    public final FTSParser.ftsFieldGroupExplicitConjunction_return ftsFieldGroupExplicitConjunction() throws RecognitionException {
        FTSParser.ftsFieldGroupExplicitConjunction_return retval = new FTSParser.ftsFieldGroupExplicitConjunction_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        FTSParser.ftsFieldGroupPrefixed_return ftsFieldGroupPrefixed94 = null;

        FTSParser.and_return and95 = null;

        FTSParser.ftsFieldGroupPrefixed_return ftsFieldGroupPrefixed96 = null;


        RewriteRuleSubtreeStream stream_ftsFieldGroupPrefixed=new RewriteRuleSubtreeStream(adaptor,"rule ftsFieldGroupPrefixed");
        RewriteRuleSubtreeStream stream_and=new RewriteRuleSubtreeStream(adaptor,"rule and");
        try {
            // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:585:9: ( ftsFieldGroupPrefixed ( and ftsFieldGroupPrefixed )* -> ^( FIELD_CONJUNCTION ( ftsFieldGroupPrefixed )+ ) )
            // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:586:9: ftsFieldGroupPrefixed ( and ftsFieldGroupPrefixed )*
            {
            pushFollow(FOLLOW_ftsFieldGroupPrefixed_in_ftsFieldGroupExplicitConjunction3522);
            ftsFieldGroupPrefixed94=ftsFieldGroupPrefixed();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_ftsFieldGroupPrefixed.add(ftsFieldGroupPrefixed94.getTree());
            // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:586:31: ( and ftsFieldGroupPrefixed )*
            loop33:
            do {
                int alt33=2;
                int LA33_0 = input.LA(1);

                if ( ((LA33_0>=AND && LA33_0<=AMP)) ) {
                    alt33=1;
                }


                switch (alt33) {
            	case 1 :
            	    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:586:32: and ftsFieldGroupPrefixed
            	    {
            	    pushFollow(FOLLOW_and_in_ftsFieldGroupExplicitConjunction3525);
            	    and95=and();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_and.add(and95.getTree());
            	    pushFollow(FOLLOW_ftsFieldGroupPrefixed_in_ftsFieldGroupExplicitConjunction3527);
            	    ftsFieldGroupPrefixed96=ftsFieldGroupPrefixed();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_ftsFieldGroupPrefixed.add(ftsFieldGroupPrefixed96.getTree());

            	    }
            	    break;

            	default :
            	    break loop33;
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
            // 587:17: -> ^( FIELD_CONJUNCTION ( ftsFieldGroupPrefixed )+ )
            {
                // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:588:25: ^( FIELD_CONJUNCTION ( ftsFieldGroupPrefixed )+ )
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
    // $ANTLR end "ftsFieldGroupExplicitConjunction"

    public static class ftsFieldGroupImplicitConjunction_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "ftsFieldGroupImplicitConjunction"
    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:591:1: ftsFieldGroupImplicitConjunction : ( ( and )? ftsFieldGroupPrefixed )+ -> ^( FIELD_CONJUNCTION ( ftsFieldGroupPrefixed )+ ) ;
    public final FTSParser.ftsFieldGroupImplicitConjunction_return ftsFieldGroupImplicitConjunction() throws RecognitionException {
        FTSParser.ftsFieldGroupImplicitConjunction_return retval = new FTSParser.ftsFieldGroupImplicitConjunction_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        FTSParser.and_return and97 = null;

        FTSParser.ftsFieldGroupPrefixed_return ftsFieldGroupPrefixed98 = null;


        RewriteRuleSubtreeStream stream_ftsFieldGroupPrefixed=new RewriteRuleSubtreeStream(adaptor,"rule ftsFieldGroupPrefixed");
        RewriteRuleSubtreeStream stream_and=new RewriteRuleSubtreeStream(adaptor,"rule and");
        try {
            // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:592:9: ( ( ( and )? ftsFieldGroupPrefixed )+ -> ^( FIELD_CONJUNCTION ( ftsFieldGroupPrefixed )+ ) )
            // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:593:9: ( ( and )? ftsFieldGroupPrefixed )+
            {
            // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:593:9: ( ( and )? ftsFieldGroupPrefixed )+
            int cnt35=0;
            loop35:
            do {
                int alt35=2;
                alt35 = dfa35.predict(input);
                switch (alt35) {
            	case 1 :
            	    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:593:10: ( and )? ftsFieldGroupPrefixed
            	    {
            	    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:593:10: ( and )?
            	    int alt34=2;
            	    alt34 = dfa34.predict(input);
            	    switch (alt34) {
            	        case 1 :
            	            // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:593:10: and
            	            {
            	            pushFollow(FOLLOW_and_in_ftsFieldGroupImplicitConjunction3612);
            	            and97=and();

            	            state._fsp--;
            	            if (state.failed) return retval;
            	            if ( state.backtracking==0 ) stream_and.add(and97.getTree());

            	            }
            	            break;

            	    }

            	    pushFollow(FOLLOW_ftsFieldGroupPrefixed_in_ftsFieldGroupImplicitConjunction3615);
            	    ftsFieldGroupPrefixed98=ftsFieldGroupPrefixed();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_ftsFieldGroupPrefixed.add(ftsFieldGroupPrefixed98.getTree());

            	    }
            	    break;

            	default :
            	    if ( cnt35 >= 1 ) break loop35;
            	    if (state.backtracking>0) {state.failed=true; return retval;}
                        EarlyExitException eee =
                            new EarlyExitException(35, input);
                        throw eee;
                }
                cnt35++;
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
            // 594:17: -> ^( FIELD_CONJUNCTION ( ftsFieldGroupPrefixed )+ )
            {
                // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:595:25: ^( FIELD_CONJUNCTION ( ftsFieldGroupPrefixed )+ )
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
    // $ANTLR end "ftsFieldGroupImplicitConjunction"

    public static class ftsFieldGroupPrefixed_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "ftsFieldGroupPrefixed"
    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:598:1: ftsFieldGroupPrefixed : ( ( not )=> not ftsFieldGroupTest ( boost )? -> ^( FIELD_NEGATION ftsFieldGroupTest ( boost )? ) | ftsFieldGroupTest ( boost )? -> ^( FIELD_DEFAULT ftsFieldGroupTest ( boost )? ) | PLUS ftsFieldGroupTest ( boost )? -> ^( FIELD_MANDATORY ftsFieldGroupTest ( boost )? ) | BAR ftsFieldGroupTest ( boost )? -> ^( FIELD_OPTIONAL ftsFieldGroupTest ( boost )? ) | MINUS ftsFieldGroupTest ( boost )? -> ^( FIELD_EXCLUDE ftsFieldGroupTest ( boost )? ) );
    public final FTSParser.ftsFieldGroupPrefixed_return ftsFieldGroupPrefixed() throws RecognitionException {
        FTSParser.ftsFieldGroupPrefixed_return retval = new FTSParser.ftsFieldGroupPrefixed_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token PLUS104=null;
        Token BAR107=null;
        Token MINUS110=null;
        FTSParser.not_return not99 = null;

        FTSParser.ftsFieldGroupTest_return ftsFieldGroupTest100 = null;

        FTSParser.boost_return boost101 = null;

        FTSParser.ftsFieldGroupTest_return ftsFieldGroupTest102 = null;

        FTSParser.boost_return boost103 = null;

        FTSParser.ftsFieldGroupTest_return ftsFieldGroupTest105 = null;

        FTSParser.boost_return boost106 = null;

        FTSParser.ftsFieldGroupTest_return ftsFieldGroupTest108 = null;

        FTSParser.boost_return boost109 = null;

        FTSParser.ftsFieldGroupTest_return ftsFieldGroupTest111 = null;

        FTSParser.boost_return boost112 = null;


        Object PLUS104_tree=null;
        Object BAR107_tree=null;
        Object MINUS110_tree=null;
        RewriteRuleTokenStream stream_PLUS=new RewriteRuleTokenStream(adaptor,"token PLUS");
        RewriteRuleTokenStream stream_MINUS=new RewriteRuleTokenStream(adaptor,"token MINUS");
        RewriteRuleTokenStream stream_BAR=new RewriteRuleTokenStream(adaptor,"token BAR");
        RewriteRuleSubtreeStream stream_not=new RewriteRuleSubtreeStream(adaptor,"rule not");
        RewriteRuleSubtreeStream stream_boost=new RewriteRuleSubtreeStream(adaptor,"rule boost");
        RewriteRuleSubtreeStream stream_ftsFieldGroupTest=new RewriteRuleSubtreeStream(adaptor,"rule ftsFieldGroupTest");
        try {
            // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:599:9: ( ( not )=> not ftsFieldGroupTest ( boost )? -> ^( FIELD_NEGATION ftsFieldGroupTest ( boost )? ) | ftsFieldGroupTest ( boost )? -> ^( FIELD_DEFAULT ftsFieldGroupTest ( boost )? ) | PLUS ftsFieldGroupTest ( boost )? -> ^( FIELD_MANDATORY ftsFieldGroupTest ( boost )? ) | BAR ftsFieldGroupTest ( boost )? -> ^( FIELD_OPTIONAL ftsFieldGroupTest ( boost )? ) | MINUS ftsFieldGroupTest ( boost )? -> ^( FIELD_EXCLUDE ftsFieldGroupTest ( boost )? ) )
            int alt41=5;
            alt41 = dfa41.predict(input);
            switch (alt41) {
                case 1 :
                    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:600:9: ( not )=> not ftsFieldGroupTest ( boost )?
                    {
                    pushFollow(FOLLOW_not_in_ftsFieldGroupPrefixed3705);
                    not99=not();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_not.add(not99.getTree());
                    pushFollow(FOLLOW_ftsFieldGroupTest_in_ftsFieldGroupPrefixed3707);
                    ftsFieldGroupTest100=ftsFieldGroupTest();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_ftsFieldGroupTest.add(ftsFieldGroupTest100.getTree());
                    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:600:40: ( boost )?
                    int alt36=2;
                    int LA36_0 = input.LA(1);

                    if ( (LA36_0==CARAT) ) {
                        alt36=1;
                    }
                    switch (alt36) {
                        case 1 :
                            // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:600:40: boost
                            {
                            pushFollow(FOLLOW_boost_in_ftsFieldGroupPrefixed3709);
                            boost101=boost();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_boost.add(boost101.getTree());

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
                    // 601:17: -> ^( FIELD_NEGATION ftsFieldGroupTest ( boost )? )
                    {
                        // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:602:25: ^( FIELD_NEGATION ftsFieldGroupTest ( boost )? )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(FIELD_NEGATION, "FIELD_NEGATION"), root_1);

                        adaptor.addChild(root_1, stream_ftsFieldGroupTest.nextTree());
                        // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:602:60: ( boost )?
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
                    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:603:11: ftsFieldGroupTest ( boost )?
                    {
                    pushFollow(FOLLOW_ftsFieldGroupTest_in_ftsFieldGroupPrefixed3773);
                    ftsFieldGroupTest102=ftsFieldGroupTest();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_ftsFieldGroupTest.add(ftsFieldGroupTest102.getTree());
                    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:603:29: ( boost )?
                    int alt37=2;
                    int LA37_0 = input.LA(1);

                    if ( (LA37_0==CARAT) ) {
                        alt37=1;
                    }
                    switch (alt37) {
                        case 1 :
                            // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:603:29: boost
                            {
                            pushFollow(FOLLOW_boost_in_ftsFieldGroupPrefixed3775);
                            boost103=boost();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_boost.add(boost103.getTree());

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
                    // 604:17: -> ^( FIELD_DEFAULT ftsFieldGroupTest ( boost )? )
                    {
                        // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:605:25: ^( FIELD_DEFAULT ftsFieldGroupTest ( boost )? )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(FIELD_DEFAULT, "FIELD_DEFAULT"), root_1);

                        adaptor.addChild(root_1, stream_ftsFieldGroupTest.nextTree());
                        // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:605:59: ( boost )?
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
                    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:606:11: PLUS ftsFieldGroupTest ( boost )?
                    {
                    PLUS104=(Token)match(input,PLUS,FOLLOW_PLUS_in_ftsFieldGroupPrefixed3839); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_PLUS.add(PLUS104);

                    pushFollow(FOLLOW_ftsFieldGroupTest_in_ftsFieldGroupPrefixed3841);
                    ftsFieldGroupTest105=ftsFieldGroupTest();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_ftsFieldGroupTest.add(ftsFieldGroupTest105.getTree());
                    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:606:34: ( boost )?
                    int alt38=2;
                    int LA38_0 = input.LA(1);

                    if ( (LA38_0==CARAT) ) {
                        alt38=1;
                    }
                    switch (alt38) {
                        case 1 :
                            // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:606:34: boost
                            {
                            pushFollow(FOLLOW_boost_in_ftsFieldGroupPrefixed3843);
                            boost106=boost();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_boost.add(boost106.getTree());

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
                    // 607:17: -> ^( FIELD_MANDATORY ftsFieldGroupTest ( boost )? )
                    {
                        // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:608:25: ^( FIELD_MANDATORY ftsFieldGroupTest ( boost )? )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(FIELD_MANDATORY, "FIELD_MANDATORY"), root_1);

                        adaptor.addChild(root_1, stream_ftsFieldGroupTest.nextTree());
                        // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:608:61: ( boost )?
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
                    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:609:11: BAR ftsFieldGroupTest ( boost )?
                    {
                    BAR107=(Token)match(input,BAR,FOLLOW_BAR_in_ftsFieldGroupPrefixed3907); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_BAR.add(BAR107);

                    pushFollow(FOLLOW_ftsFieldGroupTest_in_ftsFieldGroupPrefixed3909);
                    ftsFieldGroupTest108=ftsFieldGroupTest();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_ftsFieldGroupTest.add(ftsFieldGroupTest108.getTree());
                    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:609:33: ( boost )?
                    int alt39=2;
                    int LA39_0 = input.LA(1);

                    if ( (LA39_0==CARAT) ) {
                        alt39=1;
                    }
                    switch (alt39) {
                        case 1 :
                            // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:609:33: boost
                            {
                            pushFollow(FOLLOW_boost_in_ftsFieldGroupPrefixed3911);
                            boost109=boost();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_boost.add(boost109.getTree());

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
                    // 610:17: -> ^( FIELD_OPTIONAL ftsFieldGroupTest ( boost )? )
                    {
                        // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:611:25: ^( FIELD_OPTIONAL ftsFieldGroupTest ( boost )? )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(FIELD_OPTIONAL, "FIELD_OPTIONAL"), root_1);

                        adaptor.addChild(root_1, stream_ftsFieldGroupTest.nextTree());
                        // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:611:60: ( boost )?
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
                    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:612:11: MINUS ftsFieldGroupTest ( boost )?
                    {
                    MINUS110=(Token)match(input,MINUS,FOLLOW_MINUS_in_ftsFieldGroupPrefixed3975); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_MINUS.add(MINUS110);

                    pushFollow(FOLLOW_ftsFieldGroupTest_in_ftsFieldGroupPrefixed3977);
                    ftsFieldGroupTest111=ftsFieldGroupTest();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_ftsFieldGroupTest.add(ftsFieldGroupTest111.getTree());
                    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:612:35: ( boost )?
                    int alt40=2;
                    int LA40_0 = input.LA(1);

                    if ( (LA40_0==CARAT) ) {
                        alt40=1;
                    }
                    switch (alt40) {
                        case 1 :
                            // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:612:35: boost
                            {
                            pushFollow(FOLLOW_boost_in_ftsFieldGroupPrefixed3979);
                            boost112=boost();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_boost.add(boost112.getTree());

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
                    // 613:17: -> ^( FIELD_EXCLUDE ftsFieldGroupTest ( boost )? )
                    {
                        // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:614:25: ^( FIELD_EXCLUDE ftsFieldGroupTest ( boost )? )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(FIELD_EXCLUDE, "FIELD_EXCLUDE"), root_1);

                        adaptor.addChild(root_1, stream_ftsFieldGroupTest.nextTree());
                        // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:614:59: ( boost )?
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
    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:617:1: ftsFieldGroupTest : ( ( ftsFieldGroupProximity )=> ftsFieldGroupProximity -> ^( FG_PROXIMITY ftsFieldGroupProximity ) | ftsFieldGroupTerm ( ( fuzzy )=> fuzzy )? -> ^( FG_TERM ftsFieldGroupTerm ( fuzzy )? ) | ftsFieldGroupExactTerm ( ( fuzzy )=> fuzzy )? -> ^( FG_EXACT_TERM ftsFieldGroupExactTerm ( fuzzy )? ) | ftsFieldGroupPhrase ( ( slop )=> slop )? -> ^( FG_PHRASE ftsFieldGroupPhrase ( slop )? ) | ftsFieldGroupSynonym ( ( fuzzy )=> fuzzy )? -> ^( FG_SYNONYM ftsFieldGroupSynonym ( fuzzy )? ) | ftsFieldGroupRange -> ^( FG_RANGE ftsFieldGroupRange ) | LPAREN ftsFieldGroupDisjunction RPAREN -> ftsFieldGroupDisjunction );
    public final FTSParser.ftsFieldGroupTest_return ftsFieldGroupTest() throws RecognitionException {
        FTSParser.ftsFieldGroupTest_return retval = new FTSParser.ftsFieldGroupTest_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token LPAREN123=null;
        Token RPAREN125=null;
        FTSParser.ftsFieldGroupProximity_return ftsFieldGroupProximity113 = null;

        FTSParser.ftsFieldGroupTerm_return ftsFieldGroupTerm114 = null;

        FTSParser.fuzzy_return fuzzy115 = null;

        FTSParser.ftsFieldGroupExactTerm_return ftsFieldGroupExactTerm116 = null;

        FTSParser.fuzzy_return fuzzy117 = null;

        FTSParser.ftsFieldGroupPhrase_return ftsFieldGroupPhrase118 = null;

        FTSParser.slop_return slop119 = null;

        FTSParser.ftsFieldGroupSynonym_return ftsFieldGroupSynonym120 = null;

        FTSParser.fuzzy_return fuzzy121 = null;

        FTSParser.ftsFieldGroupRange_return ftsFieldGroupRange122 = null;

        FTSParser.ftsFieldGroupDisjunction_return ftsFieldGroupDisjunction124 = null;


        Object LPAREN123_tree=null;
        Object RPAREN125_tree=null;
        RewriteRuleTokenStream stream_RPAREN=new RewriteRuleTokenStream(adaptor,"token RPAREN");
        RewriteRuleTokenStream stream_LPAREN=new RewriteRuleTokenStream(adaptor,"token LPAREN");
        RewriteRuleSubtreeStream stream_ftsFieldGroupRange=new RewriteRuleSubtreeStream(adaptor,"rule ftsFieldGroupRange");
        RewriteRuleSubtreeStream stream_ftsFieldGroupPhrase=new RewriteRuleSubtreeStream(adaptor,"rule ftsFieldGroupPhrase");
        RewriteRuleSubtreeStream stream_fuzzy=new RewriteRuleSubtreeStream(adaptor,"rule fuzzy");
        RewriteRuleSubtreeStream stream_slop=new RewriteRuleSubtreeStream(adaptor,"rule slop");
        RewriteRuleSubtreeStream stream_ftsFieldGroupTerm=new RewriteRuleSubtreeStream(adaptor,"rule ftsFieldGroupTerm");
        RewriteRuleSubtreeStream stream_ftsFieldGroupSynonym=new RewriteRuleSubtreeStream(adaptor,"rule ftsFieldGroupSynonym");
        RewriteRuleSubtreeStream stream_ftsFieldGroupExactTerm=new RewriteRuleSubtreeStream(adaptor,"rule ftsFieldGroupExactTerm");
        RewriteRuleSubtreeStream stream_ftsFieldGroupDisjunction=new RewriteRuleSubtreeStream(adaptor,"rule ftsFieldGroupDisjunction");
        RewriteRuleSubtreeStream stream_ftsFieldGroupProximity=new RewriteRuleSubtreeStream(adaptor,"rule ftsFieldGroupProximity");
        try {
            // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:618:9: ( ( ftsFieldGroupProximity )=> ftsFieldGroupProximity -> ^( FG_PROXIMITY ftsFieldGroupProximity ) | ftsFieldGroupTerm ( ( fuzzy )=> fuzzy )? -> ^( FG_TERM ftsFieldGroupTerm ( fuzzy )? ) | ftsFieldGroupExactTerm ( ( fuzzy )=> fuzzy )? -> ^( FG_EXACT_TERM ftsFieldGroupExactTerm ( fuzzy )? ) | ftsFieldGroupPhrase ( ( slop )=> slop )? -> ^( FG_PHRASE ftsFieldGroupPhrase ( slop )? ) | ftsFieldGroupSynonym ( ( fuzzy )=> fuzzy )? -> ^( FG_SYNONYM ftsFieldGroupSynonym ( fuzzy )? ) | ftsFieldGroupRange -> ^( FG_RANGE ftsFieldGroupRange ) | LPAREN ftsFieldGroupDisjunction RPAREN -> ftsFieldGroupDisjunction )
            int alt46=7;
            alt46 = dfa46.predict(input);
            switch (alt46) {
                case 1 :
                    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:619:9: ( ftsFieldGroupProximity )=> ftsFieldGroupProximity
                    {
                    pushFollow(FOLLOW_ftsFieldGroupProximity_in_ftsFieldGroupTest4070);
                    ftsFieldGroupProximity113=ftsFieldGroupProximity();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_ftsFieldGroupProximity.add(ftsFieldGroupProximity113.getTree());


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
                    // 620:17: -> ^( FG_PROXIMITY ftsFieldGroupProximity )
                    {
                        // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:621:25: ^( FG_PROXIMITY ftsFieldGroupProximity )
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
                case 2 :
                    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:622:11: ftsFieldGroupTerm ( ( fuzzy )=> fuzzy )?
                    {
                    pushFollow(FOLLOW_ftsFieldGroupTerm_in_ftsFieldGroupTest4130);
                    ftsFieldGroupTerm114=ftsFieldGroupTerm();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_ftsFieldGroupTerm.add(ftsFieldGroupTerm114.getTree());
                    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:622:29: ( ( fuzzy )=> fuzzy )?
                    int alt42=2;
                    alt42 = dfa42.predict(input);
                    switch (alt42) {
                        case 1 :
                            // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:622:31: ( fuzzy )=> fuzzy
                            {
                            pushFollow(FOLLOW_fuzzy_in_ftsFieldGroupTest4140);
                            fuzzy115=fuzzy();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_fuzzy.add(fuzzy115.getTree());

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
                    // 623:17: -> ^( FG_TERM ftsFieldGroupTerm ( fuzzy )? )
                    {
                        // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:624:25: ^( FG_TERM ftsFieldGroupTerm ( fuzzy )? )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(FG_TERM, "FG_TERM"), root_1);

                        adaptor.addChild(root_1, stream_ftsFieldGroupTerm.nextTree());
                        // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:624:53: ( fuzzy )?
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
                    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:625:11: ftsFieldGroupExactTerm ( ( fuzzy )=> fuzzy )?
                    {
                    pushFollow(FOLLOW_ftsFieldGroupExactTerm_in_ftsFieldGroupTest4205);
                    ftsFieldGroupExactTerm116=ftsFieldGroupExactTerm();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_ftsFieldGroupExactTerm.add(ftsFieldGroupExactTerm116.getTree());
                    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:625:34: ( ( fuzzy )=> fuzzy )?
                    int alt43=2;
                    alt43 = dfa43.predict(input);
                    switch (alt43) {
                        case 1 :
                            // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:625:36: ( fuzzy )=> fuzzy
                            {
                            pushFollow(FOLLOW_fuzzy_in_ftsFieldGroupTest4215);
                            fuzzy117=fuzzy();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_fuzzy.add(fuzzy117.getTree());

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
                    // 626:17: -> ^( FG_EXACT_TERM ftsFieldGroupExactTerm ( fuzzy )? )
                    {
                        // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:627:25: ^( FG_EXACT_TERM ftsFieldGroupExactTerm ( fuzzy )? )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(FG_EXACT_TERM, "FG_EXACT_TERM"), root_1);

                        adaptor.addChild(root_1, stream_ftsFieldGroupExactTerm.nextTree());
                        // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:627:64: ( fuzzy )?
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
                case 4 :
                    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:628:11: ftsFieldGroupPhrase ( ( slop )=> slop )?
                    {
                    pushFollow(FOLLOW_ftsFieldGroupPhrase_in_ftsFieldGroupTest4280);
                    ftsFieldGroupPhrase118=ftsFieldGroupPhrase();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_ftsFieldGroupPhrase.add(ftsFieldGroupPhrase118.getTree());
                    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:628:31: ( ( slop )=> slop )?
                    int alt44=2;
                    alt44 = dfa44.predict(input);
                    switch (alt44) {
                        case 1 :
                            // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:628:33: ( slop )=> slop
                            {
                            pushFollow(FOLLOW_slop_in_ftsFieldGroupTest4290);
                            slop119=slop();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_slop.add(slop119.getTree());

                            }
                            break;

                    }



                    // AST REWRITE
                    // elements: ftsFieldGroupPhrase, slop
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 629:17: -> ^( FG_PHRASE ftsFieldGroupPhrase ( slop )? )
                    {
                        // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:630:25: ^( FG_PHRASE ftsFieldGroupPhrase ( slop )? )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(FG_PHRASE, "FG_PHRASE"), root_1);

                        adaptor.addChild(root_1, stream_ftsFieldGroupPhrase.nextTree());
                        // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:630:57: ( slop )?
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
                case 5 :
                    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:631:11: ftsFieldGroupSynonym ( ( fuzzy )=> fuzzy )?
                    {
                    pushFollow(FOLLOW_ftsFieldGroupSynonym_in_ftsFieldGroupTest4355);
                    ftsFieldGroupSynonym120=ftsFieldGroupSynonym();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_ftsFieldGroupSynonym.add(ftsFieldGroupSynonym120.getTree());
                    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:631:32: ( ( fuzzy )=> fuzzy )?
                    int alt45=2;
                    alt45 = dfa45.predict(input);
                    switch (alt45) {
                        case 1 :
                            // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:631:34: ( fuzzy )=> fuzzy
                            {
                            pushFollow(FOLLOW_fuzzy_in_ftsFieldGroupTest4365);
                            fuzzy121=fuzzy();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_fuzzy.add(fuzzy121.getTree());

                            }
                            break;

                    }



                    // AST REWRITE
                    // elements: ftsFieldGroupSynonym, fuzzy
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 632:17: -> ^( FG_SYNONYM ftsFieldGroupSynonym ( fuzzy )? )
                    {
                        // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:633:25: ^( FG_SYNONYM ftsFieldGroupSynonym ( fuzzy )? )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(FG_SYNONYM, "FG_SYNONYM"), root_1);

                        adaptor.addChild(root_1, stream_ftsFieldGroupSynonym.nextTree());
                        // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:633:59: ( fuzzy )?
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
                case 6 :
                    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:634:11: ftsFieldGroupRange
                    {
                    pushFollow(FOLLOW_ftsFieldGroupRange_in_ftsFieldGroupTest4430);
                    ftsFieldGroupRange122=ftsFieldGroupRange();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_ftsFieldGroupRange.add(ftsFieldGroupRange122.getTree());


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
                    // 635:17: -> ^( FG_RANGE ftsFieldGroupRange )
                    {
                        // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:636:25: ^( FG_RANGE ftsFieldGroupRange )
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
                    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:637:11: LPAREN ftsFieldGroupDisjunction RPAREN
                    {
                    LPAREN123=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_ftsFieldGroupTest4490); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_LPAREN.add(LPAREN123);

                    pushFollow(FOLLOW_ftsFieldGroupDisjunction_in_ftsFieldGroupTest4492);
                    ftsFieldGroupDisjunction124=ftsFieldGroupDisjunction();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_ftsFieldGroupDisjunction.add(ftsFieldGroupDisjunction124.getTree());
                    RPAREN125=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_ftsFieldGroupTest4494); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_RPAREN.add(RPAREN125);



                    // AST REWRITE
                    // elements: ftsFieldGroupDisjunction
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 638:17: -> ftsFieldGroupDisjunction
                    {
                        adaptor.addChild(root_0, stream_ftsFieldGroupDisjunction.nextTree());

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
    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:641:1: ftsFieldGroupTerm : ftsWord ;
    public final FTSParser.ftsFieldGroupTerm_return ftsFieldGroupTerm() throws RecognitionException {
        FTSParser.ftsFieldGroupTerm_return retval = new FTSParser.ftsFieldGroupTerm_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        FTSParser.ftsWord_return ftsWord126 = null;



        try {
            // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:642:9: ( ftsWord )
            // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:643:9: ftsWord
            {
            root_0 = (Object)adaptor.nil();

            pushFollow(FOLLOW_ftsWord_in_ftsFieldGroupTerm4547);
            ftsWord126=ftsWord();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, ftsWord126.getTree());

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
    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:646:1: ftsFieldGroupExactTerm : EQUALS ftsFieldGroupTerm -> ftsFieldGroupTerm ;
    public final FTSParser.ftsFieldGroupExactTerm_return ftsFieldGroupExactTerm() throws RecognitionException {
        FTSParser.ftsFieldGroupExactTerm_return retval = new FTSParser.ftsFieldGroupExactTerm_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token EQUALS127=null;
        FTSParser.ftsFieldGroupTerm_return ftsFieldGroupTerm128 = null;


        Object EQUALS127_tree=null;
        RewriteRuleTokenStream stream_EQUALS=new RewriteRuleTokenStream(adaptor,"token EQUALS");
        RewriteRuleSubtreeStream stream_ftsFieldGroupTerm=new RewriteRuleSubtreeStream(adaptor,"rule ftsFieldGroupTerm");
        try {
            // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:647:9: ( EQUALS ftsFieldGroupTerm -> ftsFieldGroupTerm )
            // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:648:9: EQUALS ftsFieldGroupTerm
            {
            EQUALS127=(Token)match(input,EQUALS,FOLLOW_EQUALS_in_ftsFieldGroupExactTerm4580); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_EQUALS.add(EQUALS127);

            pushFollow(FOLLOW_ftsFieldGroupTerm_in_ftsFieldGroupExactTerm4582);
            ftsFieldGroupTerm128=ftsFieldGroupTerm();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_ftsFieldGroupTerm.add(ftsFieldGroupTerm128.getTree());


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
            // 649:17: -> ftsFieldGroupTerm
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
    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:652:1: ftsFieldGroupPhrase : FTSPHRASE ;
    public final FTSParser.ftsFieldGroupPhrase_return ftsFieldGroupPhrase() throws RecognitionException {
        FTSParser.ftsFieldGroupPhrase_return retval = new FTSParser.ftsFieldGroupPhrase_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token FTSPHRASE129=null;

        Object FTSPHRASE129_tree=null;

        try {
            // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:653:9: ( FTSPHRASE )
            // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:654:9: FTSPHRASE
            {
            root_0 = (Object)adaptor.nil();

            FTSPHRASE129=(Token)match(input,FTSPHRASE,FOLLOW_FTSPHRASE_in_ftsFieldGroupPhrase4635); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            FTSPHRASE129_tree = (Object)adaptor.create(FTSPHRASE129);
            adaptor.addChild(root_0, FTSPHRASE129_tree);
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
    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:657:1: ftsFieldGroupSynonym : TILDA ftsFieldGroupTerm -> ftsFieldGroupTerm ;
    public final FTSParser.ftsFieldGroupSynonym_return ftsFieldGroupSynonym() throws RecognitionException {
        FTSParser.ftsFieldGroupSynonym_return retval = new FTSParser.ftsFieldGroupSynonym_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token TILDA130=null;
        FTSParser.ftsFieldGroupTerm_return ftsFieldGroupTerm131 = null;


        Object TILDA130_tree=null;
        RewriteRuleTokenStream stream_TILDA=new RewriteRuleTokenStream(adaptor,"token TILDA");
        RewriteRuleSubtreeStream stream_ftsFieldGroupTerm=new RewriteRuleSubtreeStream(adaptor,"rule ftsFieldGroupTerm");
        try {
            // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:658:9: ( TILDA ftsFieldGroupTerm -> ftsFieldGroupTerm )
            // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:659:9: TILDA ftsFieldGroupTerm
            {
            TILDA130=(Token)match(input,TILDA,FOLLOW_TILDA_in_ftsFieldGroupSynonym4668); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_TILDA.add(TILDA130);

            pushFollow(FOLLOW_ftsFieldGroupTerm_in_ftsFieldGroupSynonym4670);
            ftsFieldGroupTerm131=ftsFieldGroupTerm();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_ftsFieldGroupTerm.add(ftsFieldGroupTerm131.getTree());


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
            // 660:17: -> ftsFieldGroupTerm
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
    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:663:1: ftsFieldGroupProximity : ftsFieldGroupProximityTerm ( ( proximityGroup )=> proximityGroup ftsFieldGroupProximityTerm )+ -> ftsFieldGroupProximityTerm ( proximityGroup ftsFieldGroupProximityTerm )+ ;
    public final FTSParser.ftsFieldGroupProximity_return ftsFieldGroupProximity() throws RecognitionException {
        FTSParser.ftsFieldGroupProximity_return retval = new FTSParser.ftsFieldGroupProximity_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        FTSParser.ftsFieldGroupProximityTerm_return ftsFieldGroupProximityTerm132 = null;

        FTSParser.proximityGroup_return proximityGroup133 = null;

        FTSParser.ftsFieldGroupProximityTerm_return ftsFieldGroupProximityTerm134 = null;


        RewriteRuleSubtreeStream stream_proximityGroup=new RewriteRuleSubtreeStream(adaptor,"rule proximityGroup");
        RewriteRuleSubtreeStream stream_ftsFieldGroupProximityTerm=new RewriteRuleSubtreeStream(adaptor,"rule ftsFieldGroupProximityTerm");
        try {
            // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:664:9: ( ftsFieldGroupProximityTerm ( ( proximityGroup )=> proximityGroup ftsFieldGroupProximityTerm )+ -> ftsFieldGroupProximityTerm ( proximityGroup ftsFieldGroupProximityTerm )+ )
            // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:665:9: ftsFieldGroupProximityTerm ( ( proximityGroup )=> proximityGroup ftsFieldGroupProximityTerm )+
            {
            pushFollow(FOLLOW_ftsFieldGroupProximityTerm_in_ftsFieldGroupProximity4723);
            ftsFieldGroupProximityTerm132=ftsFieldGroupProximityTerm();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_ftsFieldGroupProximityTerm.add(ftsFieldGroupProximityTerm132.getTree());
            // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:665:36: ( ( proximityGroup )=> proximityGroup ftsFieldGroupProximityTerm )+
            int cnt47=0;
            loop47:
            do {
                int alt47=2;
                alt47 = dfa47.predict(input);
                switch (alt47) {
            	case 1 :
            	    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:665:38: ( proximityGroup )=> proximityGroup ftsFieldGroupProximityTerm
            	    {
            	    pushFollow(FOLLOW_proximityGroup_in_ftsFieldGroupProximity4733);
            	    proximityGroup133=proximityGroup();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_proximityGroup.add(proximityGroup133.getTree());
            	    pushFollow(FOLLOW_ftsFieldGroupProximityTerm_in_ftsFieldGroupProximity4735);
            	    ftsFieldGroupProximityTerm134=ftsFieldGroupProximityTerm();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_ftsFieldGroupProximityTerm.add(ftsFieldGroupProximityTerm134.getTree());

            	    }
            	    break;

            	default :
            	    if ( cnt47 >= 1 ) break loop47;
            	    if (state.backtracking>0) {state.failed=true; return retval;}
                        EarlyExitException eee =
                            new EarlyExitException(47, input);
                        throw eee;
                }
                cnt47++;
            } while (true);



            // AST REWRITE
            // elements: ftsFieldGroupProximityTerm, ftsFieldGroupProximityTerm, proximityGroup
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 666:17: -> ftsFieldGroupProximityTerm ( proximityGroup ftsFieldGroupProximityTerm )+
            {
                adaptor.addChild(root_0, stream_ftsFieldGroupProximityTerm.nextTree());
                if ( !(stream_ftsFieldGroupProximityTerm.hasNext()||stream_proximityGroup.hasNext()) ) {
                    throw new RewriteEarlyExitException();
                }
                while ( stream_ftsFieldGroupProximityTerm.hasNext()||stream_proximityGroup.hasNext() ) {
                    adaptor.addChild(root_0, stream_proximityGroup.nextTree());
                    adaptor.addChild(root_0, stream_ftsFieldGroupProximityTerm.nextTree());

                }
                stream_ftsFieldGroupProximityTerm.reset();
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

    public static class ftsFieldGroupProximityTerm_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "ftsFieldGroupProximityTerm"
    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:669:1: ftsFieldGroupProximityTerm : ( ID | FTSWORD | FTSPRE | FTSWILD | NOT | TO | DECIMAL_INTEGER_LITERAL | FLOATING_POINT_LITERAL );
    public final FTSParser.ftsFieldGroupProximityTerm_return ftsFieldGroupProximityTerm() throws RecognitionException {
        FTSParser.ftsFieldGroupProximityTerm_return retval = new FTSParser.ftsFieldGroupProximityTerm_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token set135=null;

        Object set135_tree=null;

        try {
            // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:670:9: ( ID | FTSWORD | FTSPRE | FTSWILD | NOT | TO | DECIMAL_INTEGER_LITERAL | FLOATING_POINT_LITERAL )
            // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:
            {
            root_0 = (Object)adaptor.nil();

            set135=(Token)input.LT(1);
            if ( input.LA(1)==DECIMAL_INTEGER_LITERAL||(input.LA(1)>=ID && input.LA(1)<=FLOATING_POINT_LITERAL) ) {
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
    // $ANTLR end "ftsFieldGroupProximityTerm"

    public static class proximityGroup_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "proximityGroup"
    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:681:1: proximityGroup : STAR ( LPAREN ( DECIMAL_INTEGER_LITERAL )? RPAREN )? -> ^( PROXIMITY ( DECIMAL_INTEGER_LITERAL )? ) ;
    public final FTSParser.proximityGroup_return proximityGroup() throws RecognitionException {
        FTSParser.proximityGroup_return retval = new FTSParser.proximityGroup_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token STAR136=null;
        Token LPAREN137=null;
        Token DECIMAL_INTEGER_LITERAL138=null;
        Token RPAREN139=null;

        Object STAR136_tree=null;
        Object LPAREN137_tree=null;
        Object DECIMAL_INTEGER_LITERAL138_tree=null;
        Object RPAREN139_tree=null;
        RewriteRuleTokenStream stream_RPAREN=new RewriteRuleTokenStream(adaptor,"token RPAREN");
        RewriteRuleTokenStream stream_STAR=new RewriteRuleTokenStream(adaptor,"token STAR");
        RewriteRuleTokenStream stream_DECIMAL_INTEGER_LITERAL=new RewriteRuleTokenStream(adaptor,"token DECIMAL_INTEGER_LITERAL");
        RewriteRuleTokenStream stream_LPAREN=new RewriteRuleTokenStream(adaptor,"token LPAREN");

        try {
            // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:682:9: ( STAR ( LPAREN ( DECIMAL_INTEGER_LITERAL )? RPAREN )? -> ^( PROXIMITY ( DECIMAL_INTEGER_LITERAL )? ) )
            // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:683:9: STAR ( LPAREN ( DECIMAL_INTEGER_LITERAL )? RPAREN )?
            {
            STAR136=(Token)match(input,STAR,FOLLOW_STAR_in_proximityGroup4914); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_STAR.add(STAR136);

            // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:683:14: ( LPAREN ( DECIMAL_INTEGER_LITERAL )? RPAREN )?
            int alt49=2;
            int LA49_0 = input.LA(1);

            if ( (LA49_0==LPAREN) ) {
                alt49=1;
            }
            switch (alt49) {
                case 1 :
                    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:683:15: LPAREN ( DECIMAL_INTEGER_LITERAL )? RPAREN
                    {
                    LPAREN137=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_proximityGroup4917); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_LPAREN.add(LPAREN137);

                    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:683:22: ( DECIMAL_INTEGER_LITERAL )?
                    int alt48=2;
                    int LA48_0 = input.LA(1);

                    if ( (LA48_0==DECIMAL_INTEGER_LITERAL) ) {
                        alt48=1;
                    }
                    switch (alt48) {
                        case 1 :
                            // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:683:22: DECIMAL_INTEGER_LITERAL
                            {
                            DECIMAL_INTEGER_LITERAL138=(Token)match(input,DECIMAL_INTEGER_LITERAL,FOLLOW_DECIMAL_INTEGER_LITERAL_in_proximityGroup4919); if (state.failed) return retval; 
                            if ( state.backtracking==0 ) stream_DECIMAL_INTEGER_LITERAL.add(DECIMAL_INTEGER_LITERAL138);


                            }
                            break;

                    }

                    RPAREN139=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_proximityGroup4922); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_RPAREN.add(RPAREN139);


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
            // 684:17: -> ^( PROXIMITY ( DECIMAL_INTEGER_LITERAL )? )
            {
                // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:685:25: ^( PROXIMITY ( DECIMAL_INTEGER_LITERAL )? )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(PROXIMITY, "PROXIMITY"), root_1);

                // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:685:37: ( DECIMAL_INTEGER_LITERAL )?
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
    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:688:1: ftsFieldGroupRange : ( ftsRangeWord DOTDOT ftsRangeWord -> INCLUSIVE ftsRangeWord ftsRangeWord INCLUSIVE | range_left ftsRangeWord TO ftsRangeWord range_right -> range_left ftsRangeWord ftsRangeWord range_right );
    public final FTSParser.ftsFieldGroupRange_return ftsFieldGroupRange() throws RecognitionException {
        FTSParser.ftsFieldGroupRange_return retval = new FTSParser.ftsFieldGroupRange_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token DOTDOT141=null;
        Token TO145=null;
        FTSParser.ftsRangeWord_return ftsRangeWord140 = null;

        FTSParser.ftsRangeWord_return ftsRangeWord142 = null;

        FTSParser.range_left_return range_left143 = null;

        FTSParser.ftsRangeWord_return ftsRangeWord144 = null;

        FTSParser.ftsRangeWord_return ftsRangeWord146 = null;

        FTSParser.range_right_return range_right147 = null;


        Object DOTDOT141_tree=null;
        Object TO145_tree=null;
        RewriteRuleTokenStream stream_DOTDOT=new RewriteRuleTokenStream(adaptor,"token DOTDOT");
        RewriteRuleTokenStream stream_TO=new RewriteRuleTokenStream(adaptor,"token TO");
        RewriteRuleSubtreeStream stream_range_left=new RewriteRuleSubtreeStream(adaptor,"rule range_left");
        RewriteRuleSubtreeStream stream_range_right=new RewriteRuleSubtreeStream(adaptor,"rule range_right");
        RewriteRuleSubtreeStream stream_ftsRangeWord=new RewriteRuleSubtreeStream(adaptor,"rule ftsRangeWord");
        try {
            // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:689:9: ( ftsRangeWord DOTDOT ftsRangeWord -> INCLUSIVE ftsRangeWord ftsRangeWord INCLUSIVE | range_left ftsRangeWord TO ftsRangeWord range_right -> range_left ftsRangeWord ftsRangeWord range_right )
            int alt50=2;
            int LA50_0 = input.LA(1);

            if ( (LA50_0==DECIMAL_INTEGER_LITERAL||(LA50_0>=FTSPHRASE && LA50_0<=FTSWILD)||LA50_0==FLOATING_POINT_LITERAL) ) {
                alt50=1;
            }
            else if ( ((LA50_0>=LSQUARE && LA50_0<=LT)) ) {
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
                    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:690:9: ftsRangeWord DOTDOT ftsRangeWord
                    {
                    pushFollow(FOLLOW_ftsRangeWord_in_ftsFieldGroupRange5006);
                    ftsRangeWord140=ftsRangeWord();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_ftsRangeWord.add(ftsRangeWord140.getTree());
                    DOTDOT141=(Token)match(input,DOTDOT,FOLLOW_DOTDOT_in_ftsFieldGroupRange5008); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_DOTDOT.add(DOTDOT141);

                    pushFollow(FOLLOW_ftsRangeWord_in_ftsFieldGroupRange5010);
                    ftsRangeWord142=ftsRangeWord();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_ftsRangeWord.add(ftsRangeWord142.getTree());


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
                    // 691:17: -> INCLUSIVE ftsRangeWord ftsRangeWord INCLUSIVE
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
                    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:692:11: range_left ftsRangeWord TO ftsRangeWord range_right
                    {
                    pushFollow(FOLLOW_range_left_in_ftsFieldGroupRange5048);
                    range_left143=range_left();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_range_left.add(range_left143.getTree());
                    pushFollow(FOLLOW_ftsRangeWord_in_ftsFieldGroupRange5050);
                    ftsRangeWord144=ftsRangeWord();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_ftsRangeWord.add(ftsRangeWord144.getTree());
                    TO145=(Token)match(input,TO,FOLLOW_TO_in_ftsFieldGroupRange5052); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_TO.add(TO145);

                    pushFollow(FOLLOW_ftsRangeWord_in_ftsFieldGroupRange5054);
                    ftsRangeWord146=ftsRangeWord();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_ftsRangeWord.add(ftsRangeWord146.getTree());
                    pushFollow(FOLLOW_range_right_in_ftsFieldGroupRange5056);
                    range_right147=range_right();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_range_right.add(range_right147.getTree());


                    // AST REWRITE
                    // elements: range_right, ftsRangeWord, range_left, ftsRangeWord
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 693:17: -> range_left ftsRangeWord ftsRangeWord range_right
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
    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:696:1: range_left : ( LSQUARE -> INCLUSIVE | LT -> EXCLUSIVE );
    public final FTSParser.range_left_return range_left() throws RecognitionException {
        FTSParser.range_left_return retval = new FTSParser.range_left_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token LSQUARE148=null;
        Token LT149=null;

        Object LSQUARE148_tree=null;
        Object LT149_tree=null;
        RewriteRuleTokenStream stream_LT=new RewriteRuleTokenStream(adaptor,"token LT");
        RewriteRuleTokenStream stream_LSQUARE=new RewriteRuleTokenStream(adaptor,"token LSQUARE");

        try {
            // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:697:9: ( LSQUARE -> INCLUSIVE | LT -> EXCLUSIVE )
            int alt51=2;
            int LA51_0 = input.LA(1);

            if ( (LA51_0==LSQUARE) ) {
                alt51=1;
            }
            else if ( (LA51_0==LT) ) {
                alt51=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 51, 0, input);

                throw nvae;
            }
            switch (alt51) {
                case 1 :
                    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:698:9: LSQUARE
                    {
                    LSQUARE148=(Token)match(input,LSQUARE,FOLLOW_LSQUARE_in_range_left5115); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_LSQUARE.add(LSQUARE148);



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
                    // 699:17: -> INCLUSIVE
                    {
                        adaptor.addChild(root_0, (Object)adaptor.create(INCLUSIVE, "INCLUSIVE"));

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:700:11: LT
                    {
                    LT149=(Token)match(input,LT,FOLLOW_LT_in_range_left5147); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_LT.add(LT149);



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
                    // 701:17: -> EXCLUSIVE
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
    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:704:1: range_right : ( RSQUARE -> INCLUSIVE | GT -> EXCLUSIVE );
    public final FTSParser.range_right_return range_right() throws RecognitionException {
        FTSParser.range_right_return retval = new FTSParser.range_right_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token RSQUARE150=null;
        Token GT151=null;

        Object RSQUARE150_tree=null;
        Object GT151_tree=null;
        RewriteRuleTokenStream stream_GT=new RewriteRuleTokenStream(adaptor,"token GT");
        RewriteRuleTokenStream stream_RSQUARE=new RewriteRuleTokenStream(adaptor,"token RSQUARE");

        try {
            // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:705:9: ( RSQUARE -> INCLUSIVE | GT -> EXCLUSIVE )
            int alt52=2;
            int LA52_0 = input.LA(1);

            if ( (LA52_0==RSQUARE) ) {
                alt52=1;
            }
            else if ( (LA52_0==GT) ) {
                alt52=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 52, 0, input);

                throw nvae;
            }
            switch (alt52) {
                case 1 :
                    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:706:9: RSQUARE
                    {
                    RSQUARE150=(Token)match(input,RSQUARE,FOLLOW_RSQUARE_in_range_right5200); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_RSQUARE.add(RSQUARE150);



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
                    // 707:17: -> INCLUSIVE
                    {
                        adaptor.addChild(root_0, (Object)adaptor.create(INCLUSIVE, "INCLUSIVE"));

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:708:11: GT
                    {
                    GT151=(Token)match(input,GT,FOLLOW_GT_in_range_right5232); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_GT.add(GT151);



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
                    // 709:17: -> EXCLUSIVE
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
    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:714:1: fieldReference : ( AT )? ( prefix | uri )? identifier -> ^( FIELD_REF identifier ( prefix )? ( uri )? ) ;
    public final FTSParser.fieldReference_return fieldReference() throws RecognitionException {
        FTSParser.fieldReference_return retval = new FTSParser.fieldReference_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token AT152=null;
        FTSParser.prefix_return prefix153 = null;

        FTSParser.uri_return uri154 = null;

        FTSParser.identifier_return identifier155 = null;


        Object AT152_tree=null;
        RewriteRuleTokenStream stream_AT=new RewriteRuleTokenStream(adaptor,"token AT");
        RewriteRuleSubtreeStream stream_prefix=new RewriteRuleSubtreeStream(adaptor,"rule prefix");
        RewriteRuleSubtreeStream stream_uri=new RewriteRuleSubtreeStream(adaptor,"rule uri");
        RewriteRuleSubtreeStream stream_identifier=new RewriteRuleSubtreeStream(adaptor,"rule identifier");
        try {
            // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:715:9: ( ( AT )? ( prefix | uri )? identifier -> ^( FIELD_REF identifier ( prefix )? ( uri )? ) )
            // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:716:9: ( AT )? ( prefix | uri )? identifier
            {
            // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:716:9: ( AT )?
            int alt53=2;
            int LA53_0 = input.LA(1);

            if ( (LA53_0==AT) ) {
                alt53=1;
            }
            switch (alt53) {
                case 1 :
                    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:716:9: AT
                    {
                    AT152=(Token)match(input,AT,FOLLOW_AT_in_fieldReference5288); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_AT.add(AT152);


                    }
                    break;

            }

            // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:717:9: ( prefix | uri )?
            int alt54=3;
            alt54 = dfa54.predict(input);
            switch (alt54) {
                case 1 :
                    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:718:17: prefix
                    {
                    pushFollow(FOLLOW_prefix_in_fieldReference5317);
                    prefix153=prefix();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_prefix.add(prefix153.getTree());

                    }
                    break;
                case 2 :
                    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:719:19: uri
                    {
                    pushFollow(FOLLOW_uri_in_fieldReference5337);
                    uri154=uri();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_uri.add(uri154.getTree());

                    }
                    break;

            }

            pushFollow(FOLLOW_identifier_in_fieldReference5358);
            identifier155=identifier();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_identifier.add(identifier155.getTree());


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
            // 722:17: -> ^( FIELD_REF identifier ( prefix )? ( uri )? )
            {
                // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:723:25: ^( FIELD_REF identifier ( prefix )? ( uri )? )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(FIELD_REF, "FIELD_REF"), root_1);

                adaptor.addChild(root_1, stream_identifier.nextTree());
                // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:723:48: ( prefix )?
                if ( stream_prefix.hasNext() ) {
                    adaptor.addChild(root_1, stream_prefix.nextTree());

                }
                stream_prefix.reset();
                // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:723:56: ( uri )?
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
    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:726:1: tempReference : ( AT )? ( prefix | uri )? identifier -> ^( FIELD_REF identifier ( prefix )? ( uri )? ) ;
    public final FTSParser.tempReference_return tempReference() throws RecognitionException {
        FTSParser.tempReference_return retval = new FTSParser.tempReference_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token AT156=null;
        FTSParser.prefix_return prefix157 = null;

        FTSParser.uri_return uri158 = null;

        FTSParser.identifier_return identifier159 = null;


        Object AT156_tree=null;
        RewriteRuleTokenStream stream_AT=new RewriteRuleTokenStream(adaptor,"token AT");
        RewriteRuleSubtreeStream stream_prefix=new RewriteRuleSubtreeStream(adaptor,"rule prefix");
        RewriteRuleSubtreeStream stream_uri=new RewriteRuleSubtreeStream(adaptor,"rule uri");
        RewriteRuleSubtreeStream stream_identifier=new RewriteRuleSubtreeStream(adaptor,"rule identifier");
        try {
            // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:727:9: ( ( AT )? ( prefix | uri )? identifier -> ^( FIELD_REF identifier ( prefix )? ( uri )? ) )
            // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:728:9: ( AT )? ( prefix | uri )? identifier
            {
            // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:728:9: ( AT )?
            int alt55=2;
            int LA55_0 = input.LA(1);

            if ( (LA55_0==AT) ) {
                alt55=1;
            }
            switch (alt55) {
                case 1 :
                    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:728:9: AT
                    {
                    AT156=(Token)match(input,AT,FOLLOW_AT_in_tempReference5445); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_AT.add(AT156);


                    }
                    break;

            }

            // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:729:9: ( prefix | uri )?
            int alt56=3;
            alt56 = dfa56.predict(input);
            switch (alt56) {
                case 1 :
                    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:730:17: prefix
                    {
                    pushFollow(FOLLOW_prefix_in_tempReference5474);
                    prefix157=prefix();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_prefix.add(prefix157.getTree());

                    }
                    break;
                case 2 :
                    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:731:19: uri
                    {
                    pushFollow(FOLLOW_uri_in_tempReference5494);
                    uri158=uri();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_uri.add(uri158.getTree());

                    }
                    break;

            }

            pushFollow(FOLLOW_identifier_in_tempReference5515);
            identifier159=identifier();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_identifier.add(identifier159.getTree());


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
            // 734:17: -> ^( FIELD_REF identifier ( prefix )? ( uri )? )
            {
                // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:735:25: ^( FIELD_REF identifier ( prefix )? ( uri )? )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(FIELD_REF, "FIELD_REF"), root_1);

                adaptor.addChild(root_1, stream_identifier.nextTree());
                // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:735:48: ( prefix )?
                if ( stream_prefix.hasNext() ) {
                    adaptor.addChild(root_1, stream_prefix.nextTree());

                }
                stream_prefix.reset();
                // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:735:56: ( uri )?
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
    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:738:1: prefix : identifier COLON -> ^( PREFIX identifier ) ;
    public final FTSParser.prefix_return prefix() throws RecognitionException {
        FTSParser.prefix_return retval = new FTSParser.prefix_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token COLON161=null;
        FTSParser.identifier_return identifier160 = null;


        Object COLON161_tree=null;
        RewriteRuleTokenStream stream_COLON=new RewriteRuleTokenStream(adaptor,"token COLON");
        RewriteRuleSubtreeStream stream_identifier=new RewriteRuleSubtreeStream(adaptor,"rule identifier");
        try {
            // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:739:9: ( identifier COLON -> ^( PREFIX identifier ) )
            // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:740:9: identifier COLON
            {
            pushFollow(FOLLOW_identifier_in_prefix5602);
            identifier160=identifier();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_identifier.add(identifier160.getTree());
            COLON161=(Token)match(input,COLON,FOLLOW_COLON_in_prefix5604); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_COLON.add(COLON161);



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
            // 741:17: -> ^( PREFIX identifier )
            {
                // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:742:25: ^( PREFIX identifier )
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
    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:745:1: uri : URI -> ^( NAME_SPACE URI ) ;
    public final FTSParser.uri_return uri() throws RecognitionException {
        FTSParser.uri_return retval = new FTSParser.uri_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token URI162=null;

        Object URI162_tree=null;
        RewriteRuleTokenStream stream_URI=new RewriteRuleTokenStream(adaptor,"token URI");

        try {
            // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:746:9: ( URI -> ^( NAME_SPACE URI ) )
            // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:747:9: URI
            {
            URI162=(Token)match(input,URI,FOLLOW_URI_in_uri5685); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_URI.add(URI162);



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
            // 748:17: -> ^( NAME_SPACE URI )
            {
                // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:749:25: ^( NAME_SPACE URI )
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
    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:752:1: identifier : ID ;
    public final FTSParser.identifier_return identifier() throws RecognitionException {
        FTSParser.identifier_return retval = new FTSParser.identifier_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token ID163=null;

        Object ID163_tree=null;

        try {
            // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:753:9: ( ID )
            // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:754:9: ID
            {
            root_0 = (Object)adaptor.nil();

            ID163=(Token)match(input,ID,FOLLOW_ID_in_identifier5766); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            ID163_tree = (Object)adaptor.create(ID163);
            adaptor.addChild(root_0, ID163_tree);
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
    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:757:1: ftsWord : ( ID | FTSWORD | FTSPRE | FTSWILD | NOT | TO | DECIMAL_INTEGER_LITERAL | FLOATING_POINT_LITERAL | STAR | QUESTION_MARK );
    public final FTSParser.ftsWord_return ftsWord() throws RecognitionException {
        FTSParser.ftsWord_return retval = new FTSParser.ftsWord_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token set164=null;

        Object set164_tree=null;

        try {
            // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:758:9: ( ID | FTSWORD | FTSPRE | FTSWILD | NOT | TO | DECIMAL_INTEGER_LITERAL | FLOATING_POINT_LITERAL | STAR | QUESTION_MARK )
            // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:
            {
            root_0 = (Object)adaptor.nil();

            set164=(Token)input.LT(1);
            if ( input.LA(1)==DECIMAL_INTEGER_LITERAL||(input.LA(1)>=ID && input.LA(1)<=STAR)||input.LA(1)==QUESTION_MARK ) {
                input.consume();
                if ( state.backtracking==0 ) adaptor.addChild(root_0, (Object)adaptor.create(set164));
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
    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:771:1: number : ( DECIMAL_INTEGER_LITERAL | FLOATING_POINT_LITERAL );
    public final FTSParser.number_return number() throws RecognitionException {
        FTSParser.number_return retval = new FTSParser.number_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token set165=null;

        Object set165_tree=null;

        try {
            // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:772:9: ( DECIMAL_INTEGER_LITERAL | FLOATING_POINT_LITERAL )
            // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:
            {
            root_0 = (Object)adaptor.nil();

            set165=(Token)input.LT(1);
            if ( input.LA(1)==DECIMAL_INTEGER_LITERAL||input.LA(1)==FLOATING_POINT_LITERAL ) {
                input.consume();
                if ( state.backtracking==0 ) adaptor.addChild(root_0, (Object)adaptor.create(set165));
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
    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:777:1: ftsRangeWord : ( ID | FTSWORD | FTSPRE | FTSWILD | FTSPHRASE | DECIMAL_INTEGER_LITERAL | FLOATING_POINT_LITERAL );
    public final FTSParser.ftsRangeWord_return ftsRangeWord() throws RecognitionException {
        FTSParser.ftsRangeWord_return retval = new FTSParser.ftsRangeWord_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token set166=null;

        Object set166_tree=null;

        try {
            // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:778:9: ( ID | FTSWORD | FTSPRE | FTSWILD | FTSPHRASE | DECIMAL_INTEGER_LITERAL | FLOATING_POINT_LITERAL )
            // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:
            {
            root_0 = (Object)adaptor.nil();

            set166=(Token)input.LT(1);
            if ( input.LA(1)==DECIMAL_INTEGER_LITERAL||(input.LA(1)>=FTSPHRASE && input.LA(1)<=FTSWILD)||input.LA(1)==FLOATING_POINT_LITERAL ) {
                input.consume();
                if ( state.backtracking==0 ) adaptor.addChild(root_0, (Object)adaptor.create(set166));
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
    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:790:1: or : ( OR | BAR BAR );
    public final FTSParser.or_return or() throws RecognitionException {
        FTSParser.or_return retval = new FTSParser.or_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token OR167=null;
        Token BAR168=null;
        Token BAR169=null;

        Object OR167_tree=null;
        Object BAR168_tree=null;
        Object BAR169_tree=null;

        try {
            // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:791:9: ( OR | BAR BAR )
            int alt57=2;
            int LA57_0 = input.LA(1);

            if ( (LA57_0==OR) ) {
                alt57=1;
            }
            else if ( (LA57_0==BAR) ) {
                alt57=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 57, 0, input);

                throw nvae;
            }
            switch (alt57) {
                case 1 :
                    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:792:9: OR
                    {
                    root_0 = (Object)adaptor.nil();

                    OR167=(Token)match(input,OR,FOLLOW_OR_in_or6092); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    OR167_tree = (Object)adaptor.create(OR167);
                    adaptor.addChild(root_0, OR167_tree);
                    }

                    }
                    break;
                case 2 :
                    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:793:11: BAR BAR
                    {
                    root_0 = (Object)adaptor.nil();

                    BAR168=(Token)match(input,BAR,FOLLOW_BAR_in_or6104); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    BAR168_tree = (Object)adaptor.create(BAR168);
                    adaptor.addChild(root_0, BAR168_tree);
                    }
                    BAR169=(Token)match(input,BAR,FOLLOW_BAR_in_or6106); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    BAR169_tree = (Object)adaptor.create(BAR169);
                    adaptor.addChild(root_0, BAR169_tree);
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
    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:796:1: and : ( AND | AMP AMP );
    public final FTSParser.and_return and() throws RecognitionException {
        FTSParser.and_return retval = new FTSParser.and_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token AND170=null;
        Token AMP171=null;
        Token AMP172=null;

        Object AND170_tree=null;
        Object AMP171_tree=null;
        Object AMP172_tree=null;

        try {
            // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:797:9: ( AND | AMP AMP )
            int alt58=2;
            int LA58_0 = input.LA(1);

            if ( (LA58_0==AND) ) {
                alt58=1;
            }
            else if ( (LA58_0==AMP) ) {
                alt58=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 58, 0, input);

                throw nvae;
            }
            switch (alt58) {
                case 1 :
                    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:798:9: AND
                    {
                    root_0 = (Object)adaptor.nil();

                    AND170=(Token)match(input,AND,FOLLOW_AND_in_and6139); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    AND170_tree = (Object)adaptor.create(AND170);
                    adaptor.addChild(root_0, AND170_tree);
                    }

                    }
                    break;
                case 2 :
                    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:799:11: AMP AMP
                    {
                    root_0 = (Object)adaptor.nil();

                    AMP171=(Token)match(input,AMP,FOLLOW_AMP_in_and6151); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    AMP171_tree = (Object)adaptor.create(AMP171);
                    adaptor.addChild(root_0, AMP171_tree);
                    }
                    AMP172=(Token)match(input,AMP,FOLLOW_AMP_in_and6153); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    AMP172_tree = (Object)adaptor.create(AMP172);
                    adaptor.addChild(root_0, AMP172_tree);
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
    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:802:1: not : ( NOT | EXCLAMATION );
    public final FTSParser.not_return not() throws RecognitionException {
        FTSParser.not_return retval = new FTSParser.not_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token set173=null;

        Object set173_tree=null;

        try {
            // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:803:9: ( NOT | EXCLAMATION )
            // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:
            {
            root_0 = (Object)adaptor.nil();

            set173=(Token)input.LT(1);
            if ( input.LA(1)==NOT||input.LA(1)==EXCLAMATION ) {
                input.consume();
                if ( state.backtracking==0 ) adaptor.addChild(root_0, (Object)adaptor.create(set173));
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
        // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:411:9: ( not )
        // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:411:10: not
        {
        pushFollow(FOLLOW_not_in_synpred1_FTS1192);
        not();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred1_FTS

    // $ANTLR start synpred2_FTS
    public final void synpred2_FTS_fragment() throws RecognitionException {   
        // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:444:9: ( ftsFieldGroupProximity )
        // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:444:10: ftsFieldGroupProximity
        {
        pushFollow(FOLLOW_ftsFieldGroupProximity_in_synpred2_FTS1703);
        ftsFieldGroupProximity();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred2_FTS

    // $ANTLR start synpred3_FTS
    public final void synpred3_FTS_fragment() throws RecognitionException {   
        // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:447:21: ( fuzzy )
        // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:447:22: fuzzy
        {
        pushFollow(FOLLOW_fuzzy_in_synpred3_FTS1773);
        fuzzy();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred3_FTS

    // $ANTLR start synpred4_FTS
    public final void synpred4_FTS_fragment() throws RecognitionException {   
        // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:450:26: ( fuzzy )
        // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:450:27: fuzzy
        {
        pushFollow(FOLLOW_fuzzy_in_synpred4_FTS1848);
        fuzzy();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred4_FTS

    // $ANTLR start synpred5_FTS
    public final void synpred5_FTS_fragment() throws RecognitionException {   
        // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:453:23: ( slop )
        // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:453:24: slop
        {
        pushFollow(FOLLOW_slop_in_synpred5_FTS1923);
        slop();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred5_FTS

    // $ANTLR start synpred6_FTS
    public final void synpred6_FTS_fragment() throws RecognitionException {   
        // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:456:24: ( fuzzy )
        // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:456:25: fuzzy
        {
        pushFollow(FOLLOW_fuzzy_in_synpred6_FTS1998);
        fuzzy();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred6_FTS

    // $ANTLR start synpred7_FTS
    public final void synpred7_FTS_fragment() throws RecognitionException {   
        // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:600:9: ( not )
        // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:600:10: not
        {
        pushFollow(FOLLOW_not_in_synpred7_FTS3700);
        not();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred7_FTS

    // $ANTLR start synpred8_FTS
    public final void synpred8_FTS_fragment() throws RecognitionException {   
        // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:619:9: ( ftsFieldGroupProximity )
        // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:619:10: ftsFieldGroupProximity
        {
        pushFollow(FOLLOW_ftsFieldGroupProximity_in_synpred8_FTS4065);
        ftsFieldGroupProximity();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred8_FTS

    // $ANTLR start synpred9_FTS
    public final void synpred9_FTS_fragment() throws RecognitionException {   
        // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:622:31: ( fuzzy )
        // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:622:32: fuzzy
        {
        pushFollow(FOLLOW_fuzzy_in_synpred9_FTS4135);
        fuzzy();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred9_FTS

    // $ANTLR start synpred10_FTS
    public final void synpred10_FTS_fragment() throws RecognitionException {   
        // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:625:36: ( fuzzy )
        // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:625:37: fuzzy
        {
        pushFollow(FOLLOW_fuzzy_in_synpred10_FTS4210);
        fuzzy();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred10_FTS

    // $ANTLR start synpred11_FTS
    public final void synpred11_FTS_fragment() throws RecognitionException {   
        // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:628:33: ( slop )
        // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:628:34: slop
        {
        pushFollow(FOLLOW_slop_in_synpred11_FTS4285);
        slop();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred11_FTS

    // $ANTLR start synpred12_FTS
    public final void synpred12_FTS_fragment() throws RecognitionException {   
        // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:631:34: ( fuzzy )
        // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:631:35: fuzzy
        {
        pushFollow(FOLLOW_fuzzy_in_synpred12_FTS4360);
        fuzzy();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred12_FTS

    // $ANTLR start synpred13_FTS
    public final void synpred13_FTS_fragment() throws RecognitionException {   
        // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:665:38: ( proximityGroup )
        // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:665:39: proximityGroup
        {
        pushFollow(FOLLOW_proximityGroup_in_synpred13_FTS4728);
        proximityGroup();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred13_FTS

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


    protected DFA1 dfa1 = new DFA1(this);
    protected DFA5 dfa5 = new DFA5(this);
    protected DFA4 dfa4 = new DFA4(this);
    protected DFA8 dfa8 = new DFA8(this);
    protected DFA7 dfa7 = new DFA7(this);
    protected DFA15 dfa15 = new DFA15(this);
    protected DFA21 dfa21 = new DFA21(this);
    protected DFA17 dfa17 = new DFA17(this);
    protected DFA18 dfa18 = new DFA18(this);
    protected DFA19 dfa19 = new DFA19(this);
    protected DFA20 dfa20 = new DFA20(this);
    protected DFA26 dfa26 = new DFA26(this);
    protected DFA29 dfa29 = new DFA29(this);
    protected DFA32 dfa32 = new DFA32(this);
    protected DFA31 dfa31 = new DFA31(this);
    protected DFA35 dfa35 = new DFA35(this);
    protected DFA34 dfa34 = new DFA34(this);
    protected DFA41 dfa41 = new DFA41(this);
    protected DFA46 dfa46 = new DFA46(this);
    protected DFA42 dfa42 = new DFA42(this);
    protected DFA43 dfa43 = new DFA43(this);
    protected DFA44 dfa44 = new DFA44(this);
    protected DFA45 dfa45 = new DFA45(this);
    protected DFA47 dfa47 = new DFA47(this);
    protected DFA54 dfa54 = new DFA54(this);
    protected DFA56 dfa56 = new DFA56(this);
    static final String DFA1_eotS =
        "\27\uffff";
    static final String DFA1_eofS =
        "\27\uffff";
    static final String DFA1_minS =
        "\1\51\3\0\2\uffff\17\0\2\uffff";
    static final String DFA1_maxS =
        "\1\111\3\0\2\uffff\17\0\2\uffff";
    static final String DFA1_acceptS =
        "\4\uffff\2\2\17\uffff\1\3\1\1";
    static final String DFA1_specialS =
        "\1\0\1\1\1\2\1\3\2\uffff\1\4\1\5\1\6\1\7\1\10\1\11\1\12\1\13\1"+
        "\14\1\15\1\16\1\17\1\20\1\21\1\22\2\uffff}>";
    static final String[] DFA1_transitionS = {
            "\1\23\1\24\1\3\1\21\1\uffff\1\22\1\uffff\1\15\1\12\2\uffff"+
            "\1\14\1\2\1\6\3\12\1\1\1\16\1\12\1\10\1\uffff\1\17\1\20\2\uffff"+
            "\1\11\1\13\1\10\1\25\1\4\1\5\1\7",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "",
            "",
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

    static final short[] DFA1_eot = DFA.unpackEncodedString(DFA1_eotS);
    static final short[] DFA1_eof = DFA.unpackEncodedString(DFA1_eofS);
    static final char[] DFA1_min = DFA.unpackEncodedStringToUnsignedChars(DFA1_minS);
    static final char[] DFA1_max = DFA.unpackEncodedStringToUnsignedChars(DFA1_maxS);
    static final short[] DFA1_accept = DFA.unpackEncodedString(DFA1_acceptS);
    static final short[] DFA1_special = DFA.unpackEncodedString(DFA1_specialS);
    static final short[][] DFA1_transition;

    static {
        int numStates = DFA1_transitionS.length;
        DFA1_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA1_transition[i] = DFA.unpackEncodedString(DFA1_transitionS[i]);
        }
    }

    class DFA1 extends DFA {

        public DFA1(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 1;
            this.eot = DFA1_eot;
            this.eof = DFA1_eof;
            this.min = DFA1_min;
            this.max = DFA1_max;
            this.accept = DFA1_accept;
            this.special = DFA1_special;
            this.transition = DFA1_transition;
        }
        public String getDescription() {
            return "349:1: ftsDisjunction : ({...}? => cmisExplicitDisjunction | {...}? => ftsExplicitDisjunction | {...}? => ftsImplicitDisjunction );";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA1_0 = input.LA(1);

                         
                        int index1_0 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA1_0==NOT) && (((getMode() == Mode.DEFAULT_DISJUNCTION)||(getMode() == Mode.CMIS)||(getMode() == Mode.DEFAULT_CONJUNCTION)))) {s = 1;}

                        else if ( (LA1_0==FTSPHRASE) && (((getMode() == Mode.DEFAULT_DISJUNCTION)||(getMode() == Mode.CMIS)||(getMode() == Mode.DEFAULT_CONJUNCTION)))) {s = 2;}

                        else if ( (LA1_0==MINUS) && (((getMode() == Mode.DEFAULT_DISJUNCTION)||(getMode() == Mode.CMIS)||(getMode() == Mode.DEFAULT_CONJUNCTION)))) {s = 3;}

                        else if ( (LA1_0==AND) && ((getMode() == Mode.DEFAULT_CONJUNCTION))) {s = 4;}

                        else if ( (LA1_0==AMP) && ((getMode() == Mode.DEFAULT_CONJUNCTION))) {s = 5;}

                        else if ( (LA1_0==ID) && (((getMode() == Mode.DEFAULT_DISJUNCTION)||(getMode() == Mode.CMIS)||(getMode() == Mode.DEFAULT_CONJUNCTION)))) {s = 6;}

                        else if ( (LA1_0==EXCLAMATION) && (((getMode() == Mode.DEFAULT_DISJUNCTION)||(getMode() == Mode.DEFAULT_CONJUNCTION)))) {s = 7;}

                        else if ( (LA1_0==STAR||LA1_0==QUESTION_MARK) && (((getMode() == Mode.DEFAULT_DISJUNCTION)||(getMode() == Mode.CMIS)||(getMode() == Mode.DEFAULT_CONJUNCTION)))) {s = 8;}

                        else if ( (LA1_0==AT) && (((getMode() == Mode.DEFAULT_DISJUNCTION)||(getMode() == Mode.DEFAULT_CONJUNCTION)))) {s = 9;}

                        else if ( (LA1_0==DECIMAL_INTEGER_LITERAL||(LA1_0>=FTSWORD && LA1_0<=FTSWILD)||LA1_0==FLOATING_POINT_LITERAL) && (((getMode() == Mode.DEFAULT_DISJUNCTION)||(getMode() == Mode.CMIS)||(getMode() == Mode.DEFAULT_CONJUNCTION)))) {s = 10;}

                        else if ( (LA1_0==URI) && (((getMode() == Mode.DEFAULT_DISJUNCTION)||(getMode() == Mode.DEFAULT_CONJUNCTION)))) {s = 11;}

                        else if ( (LA1_0==EQUALS) && (((getMode() == Mode.DEFAULT_DISJUNCTION)||(getMode() == Mode.DEFAULT_CONJUNCTION)))) {s = 12;}

                        else if ( (LA1_0==TILDA) && (((getMode() == Mode.DEFAULT_DISJUNCTION)||(getMode() == Mode.DEFAULT_CONJUNCTION)))) {s = 13;}

                        else if ( (LA1_0==TO) && (((getMode() == Mode.DEFAULT_DISJUNCTION)||(getMode() == Mode.CMIS)||(getMode() == Mode.DEFAULT_CONJUNCTION)))) {s = 14;}

                        else if ( (LA1_0==LSQUARE) && (((getMode() == Mode.DEFAULT_DISJUNCTION)||(getMode() == Mode.DEFAULT_CONJUNCTION)))) {s = 15;}

                        else if ( (LA1_0==LT) && (((getMode() == Mode.DEFAULT_DISJUNCTION)||(getMode() == Mode.DEFAULT_CONJUNCTION)))) {s = 16;}

                        else if ( (LA1_0==LPAREN) && (((getMode() == Mode.DEFAULT_DISJUNCTION)||(getMode() == Mode.DEFAULT_CONJUNCTION)))) {s = 17;}

                        else if ( (LA1_0==PERCENT) && (((getMode() == Mode.DEFAULT_DISJUNCTION)||(getMode() == Mode.DEFAULT_CONJUNCTION)))) {s = 18;}

                        else if ( (LA1_0==PLUS) && (((getMode() == Mode.DEFAULT_DISJUNCTION)||(getMode() == Mode.DEFAULT_CONJUNCTION)))) {s = 19;}

                        else if ( (LA1_0==BAR) && (((getMode() == Mode.DEFAULT_DISJUNCTION)||(getMode() == Mode.DEFAULT_CONJUNCTION)))) {s = 20;}

                        else if ( (LA1_0==OR) && ((getMode() == Mode.DEFAULT_DISJUNCTION))) {s = 21;}

                         
                        input.seek(index1_0);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA1_1 = input.LA(1);

                         
                        int index1_1 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((getMode() == Mode.CMIS)) ) {s = 22;}

                        else if ( ((getMode() == Mode.DEFAULT_CONJUNCTION)) ) {s = 5;}

                        else if ( ((getMode() == Mode.DEFAULT_DISJUNCTION)) ) {s = 21;}

                         
                        input.seek(index1_1);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA1_2 = input.LA(1);

                         
                        int index1_2 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((getMode() == Mode.CMIS)) ) {s = 22;}

                        else if ( ((getMode() == Mode.DEFAULT_CONJUNCTION)) ) {s = 5;}

                        else if ( ((getMode() == Mode.DEFAULT_DISJUNCTION)) ) {s = 21;}

                         
                        input.seek(index1_2);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA1_3 = input.LA(1);

                         
                        int index1_3 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((getMode() == Mode.CMIS)) ) {s = 22;}

                        else if ( ((getMode() == Mode.DEFAULT_CONJUNCTION)) ) {s = 5;}

                        else if ( ((getMode() == Mode.DEFAULT_DISJUNCTION)) ) {s = 21;}

                         
                        input.seek(index1_3);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA1_6 = input.LA(1);

                         
                        int index1_6 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((getMode() == Mode.CMIS)) ) {s = 22;}

                        else if ( ((getMode() == Mode.DEFAULT_CONJUNCTION)) ) {s = 5;}

                        else if ( ((getMode() == Mode.DEFAULT_DISJUNCTION)) ) {s = 21;}

                         
                        input.seek(index1_6);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA1_7 = input.LA(1);

                         
                        int index1_7 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((getMode() == Mode.DEFAULT_CONJUNCTION)) ) {s = 5;}

                        else if ( ((getMode() == Mode.DEFAULT_DISJUNCTION)) ) {s = 21;}

                         
                        input.seek(index1_7);
                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA1_8 = input.LA(1);

                         
                        int index1_8 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((getMode() == Mode.CMIS)) ) {s = 22;}

                        else if ( ((getMode() == Mode.DEFAULT_CONJUNCTION)) ) {s = 5;}

                        else if ( ((getMode() == Mode.DEFAULT_DISJUNCTION)) ) {s = 21;}

                         
                        input.seek(index1_8);
                        if ( s>=0 ) return s;
                        break;
                    case 7 : 
                        int LA1_9 = input.LA(1);

                         
                        int index1_9 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((getMode() == Mode.DEFAULT_CONJUNCTION)) ) {s = 5;}

                        else if ( ((getMode() == Mode.DEFAULT_DISJUNCTION)) ) {s = 21;}

                         
                        input.seek(index1_9);
                        if ( s>=0 ) return s;
                        break;
                    case 8 : 
                        int LA1_10 = input.LA(1);

                         
                        int index1_10 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((getMode() == Mode.CMIS)) ) {s = 22;}

                        else if ( ((getMode() == Mode.DEFAULT_CONJUNCTION)) ) {s = 5;}

                        else if ( ((getMode() == Mode.DEFAULT_DISJUNCTION)) ) {s = 21;}

                         
                        input.seek(index1_10);
                        if ( s>=0 ) return s;
                        break;
                    case 9 : 
                        int LA1_11 = input.LA(1);

                         
                        int index1_11 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((getMode() == Mode.DEFAULT_CONJUNCTION)) ) {s = 5;}

                        else if ( ((getMode() == Mode.DEFAULT_DISJUNCTION)) ) {s = 21;}

                         
                        input.seek(index1_11);
                        if ( s>=0 ) return s;
                        break;
                    case 10 : 
                        int LA1_12 = input.LA(1);

                         
                        int index1_12 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((getMode() == Mode.DEFAULT_CONJUNCTION)) ) {s = 5;}

                        else if ( ((getMode() == Mode.DEFAULT_DISJUNCTION)) ) {s = 21;}

                         
                        input.seek(index1_12);
                        if ( s>=0 ) return s;
                        break;
                    case 11 : 
                        int LA1_13 = input.LA(1);

                         
                        int index1_13 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((getMode() == Mode.DEFAULT_CONJUNCTION)) ) {s = 5;}

                        else if ( ((getMode() == Mode.DEFAULT_DISJUNCTION)) ) {s = 21;}

                         
                        input.seek(index1_13);
                        if ( s>=0 ) return s;
                        break;
                    case 12 : 
                        int LA1_14 = input.LA(1);

                         
                        int index1_14 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((getMode() == Mode.CMIS)) ) {s = 22;}

                        else if ( ((getMode() == Mode.DEFAULT_CONJUNCTION)) ) {s = 5;}

                        else if ( ((getMode() == Mode.DEFAULT_DISJUNCTION)) ) {s = 21;}

                         
                        input.seek(index1_14);
                        if ( s>=0 ) return s;
                        break;
                    case 13 : 
                        int LA1_15 = input.LA(1);

                         
                        int index1_15 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((getMode() == Mode.DEFAULT_CONJUNCTION)) ) {s = 5;}

                        else if ( ((getMode() == Mode.DEFAULT_DISJUNCTION)) ) {s = 21;}

                         
                        input.seek(index1_15);
                        if ( s>=0 ) return s;
                        break;
                    case 14 : 
                        int LA1_16 = input.LA(1);

                         
                        int index1_16 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((getMode() == Mode.DEFAULT_CONJUNCTION)) ) {s = 5;}

                        else if ( ((getMode() == Mode.DEFAULT_DISJUNCTION)) ) {s = 21;}

                         
                        input.seek(index1_16);
                        if ( s>=0 ) return s;
                        break;
                    case 15 : 
                        int LA1_17 = input.LA(1);

                         
                        int index1_17 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((getMode() == Mode.DEFAULT_CONJUNCTION)) ) {s = 5;}

                        else if ( ((getMode() == Mode.DEFAULT_DISJUNCTION)) ) {s = 21;}

                         
                        input.seek(index1_17);
                        if ( s>=0 ) return s;
                        break;
                    case 16 : 
                        int LA1_18 = input.LA(1);

                         
                        int index1_18 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((getMode() == Mode.DEFAULT_CONJUNCTION)) ) {s = 5;}

                        else if ( ((getMode() == Mode.DEFAULT_DISJUNCTION)) ) {s = 21;}

                         
                        input.seek(index1_18);
                        if ( s>=0 ) return s;
                        break;
                    case 17 : 
                        int LA1_19 = input.LA(1);

                         
                        int index1_19 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((getMode() == Mode.DEFAULT_CONJUNCTION)) ) {s = 5;}

                        else if ( ((getMode() == Mode.DEFAULT_DISJUNCTION)) ) {s = 21;}

                         
                        input.seek(index1_19);
                        if ( s>=0 ) return s;
                        break;
                    case 18 : 
                        int LA1_20 = input.LA(1);

                         
                        int index1_20 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((getMode() == Mode.DEFAULT_CONJUNCTION)) ) {s = 5;}

                        else if ( ((getMode() == Mode.DEFAULT_DISJUNCTION)) ) {s = 21;}

                         
                        input.seek(index1_20);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 1, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA5_eotS =
        "\26\uffff";
    static final String DFA5_eofS =
        "\1\1\25\uffff";
    static final String DFA5_minS =
        "\1\51\25\uffff";
    static final String DFA5_maxS =
        "\1\111\25\uffff";
    static final String DFA5_acceptS =
        "\1\uffff\2\2\23\1";
    static final String DFA5_specialS =
        "\26\uffff}>";
    static final String[] DFA5_transitionS = {
            "\1\24\1\4\1\25\1\22\1\2\1\23\1\uffff\1\16\1\11\2\uffff\1\14"+
            "\1\15\1\7\3\11\1\5\1\17\1\11\1\13\1\uffff\1\20\1\21\2\uffff"+
            "\1\10\1\12\1\13\1\3\2\uffff\1\6",
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
            return "()+ loopback of 372:9: ( ( or )? ftsExplicitConjunction )+";
        }
    }
    static final String DFA4_eotS =
        "\42\uffff";
    static final String DFA4_eofS =
        "\42\uffff";
    static final String DFA4_minS =
        "\1\51\1\uffff\1\52\37\uffff";
    static final String DFA4_maxS =
        "\1\111\1\uffff\1\105\37\uffff";
    static final String DFA4_acceptS =
        "\1\uffff\1\1\1\uffff\21\2\1\1\15\2";
    static final String DFA4_specialS =
        "\42\uffff}>";
    static final String[] DFA4_transitionS = {
            "\1\22\1\2\1\23\1\20\1\uffff\1\21\1\uffff\1\14\1\7\2\uffff\1"+
            "\12\1\13\1\5\3\7\1\3\1\15\1\7\1\11\1\uffff\1\16\1\17\2\uffff"+
            "\1\6\1\10\1\11\1\1\2\uffff\1\4",
            "",
            "\1\24\1\uffff\1\40\1\uffff\1\41\1\uffff\1\34\1\27\2\uffff"+
            "\1\32\1\33\1\25\3\27\2\35\1\27\1\31\1\uffff\1\36\1\37\2\uffff"+
            "\1\26\1\30\1\31",
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
            return "372:10: ( or )?";
        }
    }
    static final String DFA8_eotS =
        "\46\uffff";
    static final String DFA8_eofS =
        "\1\3\45\uffff";
    static final String DFA8_minS =
        "\1\51\1\uffff\1\52\43\uffff";
    static final String DFA8_maxS =
        "\1\111\1\uffff\1\105\43\uffff";
    static final String DFA8_acceptS =
        "\1\uffff\1\2\1\uffff\2\2\23\1\1\2\15\1";
    static final String DFA8_specialS =
        "\46\uffff}>";
    static final String[] DFA8_transitionS = {
            "\1\26\1\2\1\27\1\24\1\4\1\25\1\uffff\1\20\1\13\2\uffff\1\16"+
            "\1\17\1\11\3\13\1\7\1\21\1\13\1\15\1\uffff\1\22\1\23\2\uffff"+
            "\1\12\1\14\1\15\1\1\1\5\1\6\1\10",
            "",
            "\1\30\1\uffff\1\44\1\uffff\1\45\1\uffff\1\40\1\33\2\uffff"+
            "\1\36\1\37\1\31\3\33\2\41\1\33\1\35\1\uffff\1\42\1\43\2\uffff"+
            "\1\32\1\34\1\35",
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
            "",
            "",
            "",
            "",
            ""
    };

    static final short[] DFA8_eot = DFA.unpackEncodedString(DFA8_eotS);
    static final short[] DFA8_eof = DFA.unpackEncodedString(DFA8_eofS);
    static final char[] DFA8_min = DFA.unpackEncodedStringToUnsignedChars(DFA8_minS);
    static final char[] DFA8_max = DFA.unpackEncodedStringToUnsignedChars(DFA8_maxS);
    static final short[] DFA8_accept = DFA.unpackEncodedString(DFA8_acceptS);
    static final short[] DFA8_special = DFA.unpackEncodedString(DFA8_specialS);
    static final short[][] DFA8_transition;

    static {
        int numStates = DFA8_transitionS.length;
        DFA8_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA8_transition[i] = DFA.unpackEncodedString(DFA8_transitionS[i]);
        }
    }

    class DFA8 extends DFA {

        public DFA8(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 8;
            this.eot = DFA8_eot;
            this.eof = DFA8_eof;
            this.min = DFA8_min;
            this.max = DFA8_max;
            this.accept = DFA8_accept;
            this.special = DFA8_special;
            this.transition = DFA8_transition;
        }
        public String getDescription() {
            return "()+ loopback of 390:9: ( ( and )? ftsPrefixed )+";
        }
    }
    static final String DFA7_eotS =
        "\25\uffff";
    static final String DFA7_eofS =
        "\25\uffff";
    static final String DFA7_minS =
        "\1\51\24\uffff";
    static final String DFA7_maxS =
        "\1\111\24\uffff";
    static final String DFA7_acceptS =
        "\1\uffff\2\1\22\2";
    static final String DFA7_specialS =
        "\25\uffff}>";
    static final String[] DFA7_transitionS = {
            "\1\22\1\23\1\24\1\20\1\uffff\1\21\1\uffff\1\14\1\7\2\uffff"+
            "\1\12\1\13\1\5\3\7\1\3\1\15\1\7\1\11\1\uffff\1\16\1\17\2\uffff"+
            "\1\6\1\10\1\11\1\uffff\1\1\1\2\1\4",
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
            "",
            "",
            "",
            "",
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
            return "390:10: ( and )?";
        }
    }
    static final String DFA15_eotS =
        "\23\uffff";
    static final String DFA15_eofS =
        "\23\uffff";
    static final String DFA15_minS =
        "\1\51\1\0\21\uffff";
    static final String DFA15_maxS =
        "\1\111\1\0\21\uffff";
    static final String DFA15_acceptS =
        "\2\uffff\1\1\15\2\1\3\1\4\1\5";
    static final String DFA15_specialS =
        "\1\0\1\1\21\uffff}>";
    static final String[] DFA15_transitionS = {
            "\1\20\1\21\1\22\1\16\1\uffff\1\17\1\uffff\1\12\1\5\2\uffff"+
            "\1\10\1\11\1\3\3\5\1\1\1\13\1\5\1\7\1\uffff\1\14\1\15\2\uffff"+
            "\1\4\1\6\1\7\3\uffff\1\2",
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
            return "409:1: ftsPrefixed : ( ( not )=> not ftsTest ( boost )? -> ^( NEGATION ftsTest ( boost )? ) | ftsTest ( boost )? -> ^( DEFAULT ftsTest ( boost )? ) | PLUS ftsTest ( boost )? -> ^( MANDATORY ftsTest ( boost )? ) | BAR ftsTest ( boost )? -> ^( OPTIONAL ftsTest ( boost )? ) | MINUS ftsTest ( boost )? -> ^( EXCLUDE ftsTest ( boost )? ) );";
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

                        else if ( (LA15_0==EXCLAMATION) && (synpred1_FTS())) {s = 2;}

                        else if ( (LA15_0==ID) ) {s = 3;}

                        else if ( (LA15_0==AT) ) {s = 4;}

                        else if ( (LA15_0==DECIMAL_INTEGER_LITERAL||(LA15_0>=FTSWORD && LA15_0<=FTSWILD)||LA15_0==FLOATING_POINT_LITERAL) ) {s = 5;}

                        else if ( (LA15_0==URI) ) {s = 6;}

                        else if ( (LA15_0==STAR||LA15_0==QUESTION_MARK) ) {s = 7;}

                        else if ( (LA15_0==EQUALS) ) {s = 8;}

                        else if ( (LA15_0==FTSPHRASE) ) {s = 9;}

                        else if ( (LA15_0==TILDA) ) {s = 10;}

                        else if ( (LA15_0==TO) ) {s = 11;}

                        else if ( (LA15_0==LSQUARE) ) {s = 12;}

                        else if ( (LA15_0==LT) ) {s = 13;}

                        else if ( (LA15_0==LPAREN) ) {s = 14;}

                        else if ( (LA15_0==PERCENT) ) {s = 15;}

                        else if ( (LA15_0==PLUS) ) {s = 16;}

                        else if ( (LA15_0==BAR) ) {s = 17;}

                        else if ( (LA15_0==MINUS) ) {s = 18;}

                         
                        input.seek(index15_0);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA15_1 = input.LA(1);

                         
                        int index15_1 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_FTS()) ) {s = 2;}

                        else if ( (true) ) {s = 15;}

                         
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
    static final String DFA21_eotS =
        "\162\uffff";
    static final String DFA21_eofS =
        "\1\uffff\1\24\1\uffff\1\24\3\uffff\1\57\1\uffff\1\24\4\uffff\1"+
        "\24\76\uffff\2\24\36\uffff\1\24\4\uffff";
    static final String DFA21_minS =
        "\1\54\1\51\1\66\1\51\1\66\2\uffff\1\51\1\uffff\1\51\4\uffff\1\51"+
        "\1\54\31\uffff\2\63\34\uffff\4\0\1\51\1\uffff\2\51\1\54\12\uffff"+
        "\1\51\22\uffff\1\51\4\uffff";
    static final String DFA21_maxS =
        "\1\105\1\111\1\104\1\111\1\66\2\uffff\1\111\1\uffff\1\111\4\uffff"+
        "\1\111\1\105\31\uffff\2\63\34\uffff\4\0\1\111\1\uffff\2\111\1\105"+
        "\12\uffff\1\111\22\uffff\1\111\4\uffff";
    static final String DFA21_acceptS =
        "\5\uffff\1\2\1\3\1\uffff\1\5\1\uffff\2\6\1\10\1\11\2\uffff\30\2"+
        "\1\6\2\uffff\30\4\4\2\5\uffff\1\7\3\uffff\1\1\11\2\1\uffff\14\2"+
        "\1\1\5\2\1\uffff\4\2";
    static final String DFA21_specialS =
        "\107\uffff\1\1\1\4\1\2\1\0\1\3\46\uffff}>";
    static final String[] DFA21_transitionS = {
            "\1\14\1\uffff\1\15\1\uffff\1\10\1\3\2\uffff\1\6\1\7\1\1\3\3"+
            "\2\11\1\3\1\5\1\uffff\1\12\1\13\2\uffff\1\2\1\4\1\5",
            "\1\46\1\27\1\47\1\44\1\25\1\45\1\uffff\1\20\1\34\1\21\1\17"+
            "\1\37\1\40\1\32\3\34\1\30\1\41\1\34\1\16\1\50\1\42\1\43\2\uffff"+
            "\1\33\1\35\1\36\1\26\1\22\1\23\1\31",
            "\1\51\15\uffff\1\4",
            "\1\46\1\27\1\47\1\44\1\25\1\45\1\uffff\1\20\1\34\1\21\1\uffff"+
            "\1\37\1\40\1\32\3\34\1\30\1\41\1\34\1\16\1\50\1\42\1\43\2\uffff"+
            "\1\33\1\35\1\36\1\26\1\22\1\23\1\31",
            "\1\52",
            "",
            "",
            "\1\101\1\62\1\102\1\77\1\60\1\100\1\uffff\1\53\1\67\1\54\1"+
            "\uffff\1\72\1\73\1\65\3\67\1\63\1\74\1\67\1\71\1\50\1\75\1\76"+
            "\2\uffff\1\66\1\70\1\71\1\61\1\55\1\56\1\64",
            "",
            "\1\46\1\27\1\47\1\44\1\25\1\45\1\uffff\1\20\1\34\1\21\1\uffff"+
            "\1\37\1\40\1\32\3\34\1\30\1\41\1\34\1\16\1\uffff\1\42\1\43\2"+
            "\uffff\1\33\1\35\1\36\1\26\1\22\1\23\1\31",
            "",
            "",
            "",
            "",
            "\1\46\1\27\1\47\1\113\1\25\1\45\1\uffff\1\103\1\111\1\104"+
            "\1\uffff\1\37\1\40\1\110\3\111\1\107\1\112\1\111\1\36\1\uffff"+
            "\1\42\1\43\2\uffff\1\33\1\35\1\36\1\26\1\105\1\106\1\31",
            "\1\114\4\uffff\1\116\3\uffff\1\7\1\115\3\116\2\5\1\116\1\5"+
            "\1\uffff\1\12\1\13\4\uffff\1\5",
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
            "\1\17",
            "\1\117",
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
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\143\1\144\1\123\1\141\1\147\1\142\1\uffff\1\135\1\132\2"+
            "\uffff\1\134\1\122\1\126\3\146\1\121\1\136\1\146\1\130\1\uffff"+
            "\1\137\1\140\2\uffff\1\131\1\133\1\130\1\145\1\124\1\125\1\127",
            "",
            "\1\46\1\27\1\47\1\44\1\25\1\45\1\uffff\1\20\1\34\1\21\1\117"+
            "\1\37\1\40\1\32\3\34\1\30\1\41\1\34\1\36\1\50\1\42\1\43\2\uffff"+
            "\1\33\1\35\1\36\1\26\1\22\1\23\1\31",
            "\1\46\1\27\1\47\1\44\1\25\1\45\1\uffff\1\20\1\34\1\21\1\uffff"+
            "\1\37\1\40\1\32\3\34\1\30\1\41\1\34\1\36\1\50\1\42\1\43\2\uffff"+
            "\1\33\1\35\1\36\1\26\1\22\1\23\1\31",
            "\1\114\4\uffff\1\116\3\uffff\1\7\4\116\2\5\1\116\1\5\1\uffff"+
            "\1\12\1\13\4\uffff\1\5",
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
            "\1\143\1\157\1\123\1\141\1\155\1\142\1\uffff\1\151\1\146\1"+
            "\152\1\uffff\1\134\1\122\1\126\3\146\1\121\1\136\1\146\1\160"+
            "\1\150\1\137\1\140\2\uffff\1\131\1\133\1\130\1\156\1\153\1\154"+
            "\1\127",
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
            "",
            "",
            "",
            "\1\46\1\27\1\47\1\44\1\25\1\45\1\uffff\1\161\1\111\1\104\1"+
            "\uffff\1\37\1\40\1\110\3\111\1\107\1\112\1\111\1\36\1\uffff"+
            "\1\42\1\43\2\uffff\1\33\1\35\1\36\1\26\1\105\1\106\1\31",
            "",
            "",
            "",
            ""
    };

    static final short[] DFA21_eot = DFA.unpackEncodedString(DFA21_eotS);
    static final short[] DFA21_eof = DFA.unpackEncodedString(DFA21_eofS);
    static final char[] DFA21_min = DFA.unpackEncodedStringToUnsignedChars(DFA21_minS);
    static final char[] DFA21_max = DFA.unpackEncodedStringToUnsignedChars(DFA21_maxS);
    static final short[] DFA21_accept = DFA.unpackEncodedString(DFA21_acceptS);
    static final short[] DFA21_special = DFA.unpackEncodedString(DFA21_specialS);
    static final short[][] DFA21_transition;

    static {
        int numStates = DFA21_transitionS.length;
        DFA21_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA21_transition[i] = DFA.unpackEncodedString(DFA21_transitionS[i]);
        }
    }

    class DFA21 extends DFA {

        public DFA21(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 21;
            this.eot = DFA21_eot;
            this.eof = DFA21_eof;
            this.min = DFA21_min;
            this.max = DFA21_max;
            this.accept = DFA21_accept;
            this.special = DFA21_special;
            this.transition = DFA21_transition;
        }
        public String getDescription() {
            return "442:1: ftsTest : ( ( ftsFieldGroupProximity )=> ftsFieldGroupProximity -> ^( PROXIMITY ftsFieldGroupProximity ) | ftsTerm ( ( fuzzy )=> fuzzy )? -> ^( TERM ftsTerm ( fuzzy )? ) | ftsExactTerm ( ( fuzzy )=> fuzzy )? -> ^( EXACT_TERM ftsExactTerm ( fuzzy )? ) | ftsPhrase ( ( slop )=> slop )? -> ^( PHRASE ftsPhrase ( slop )? ) | ftsSynonym ( ( fuzzy )=> fuzzy )? -> ^( SYNONYM ftsSynonym ( fuzzy )? ) | ftsRange -> ^( RANGE ftsRange ) | ftsFieldGroup -> ftsFieldGroup | LPAREN ftsDisjunction RPAREN -> ftsDisjunction | template -> template );";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA21_74 = input.LA(1);

                         
                        int index21_74 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_FTS()) ) {s = 80;}

                        else if ( (true) ) {s = 70;}

                         
                        input.seek(index21_74);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA21_71 = input.LA(1);

                         
                        int index21_71 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_FTS()) ) {s = 80;}

                        else if ( (true) ) {s = 70;}

                         
                        input.seek(index21_71);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA21_73 = input.LA(1);

                         
                        int index21_73 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_FTS()) ) {s = 80;}

                        else if ( (true) ) {s = 70;}

                         
                        input.seek(index21_73);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA21_75 = input.LA(1);

                         
                        int index21_75 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA21_75==NOT) ) {s = 81;}

                        else if ( (LA21_75==FTSPHRASE) ) {s = 82;}

                        else if ( (LA21_75==MINUS) ) {s = 83;}

                        else if ( (LA21_75==AND) ) {s = 84;}

                        else if ( (LA21_75==AMP) ) {s = 85;}

                        else if ( (LA21_75==ID) ) {s = 86;}

                        else if ( (LA21_75==EXCLAMATION) ) {s = 87;}

                        else if ( (LA21_75==STAR||LA21_75==QUESTION_MARK) ) {s = 88;}

                        else if ( (LA21_75==AT) ) {s = 89;}

                        else if ( (LA21_75==DECIMAL_INTEGER_LITERAL) ) {s = 90;}

                        else if ( (LA21_75==URI) ) {s = 91;}

                        else if ( (LA21_75==EQUALS) ) {s = 92;}

                        else if ( (LA21_75==TILDA) ) {s = 93;}

                        else if ( (LA21_75==TO) ) {s = 94;}

                        else if ( (LA21_75==LSQUARE) ) {s = 95;}

                        else if ( (LA21_75==LT) ) {s = 96;}

                        else if ( (LA21_75==LPAREN) ) {s = 97;}

                        else if ( (LA21_75==PERCENT) ) {s = 98;}

                        else if ( (LA21_75==PLUS) ) {s = 99;}

                        else if ( (LA21_75==BAR) ) {s = 100;}

                        else if ( (LA21_75==OR) ) {s = 101;}

                        else if ( ((LA21_75>=FTSWORD && LA21_75<=FTSWILD)||LA21_75==FLOATING_POINT_LITERAL) ) {s = 102;}

                        else if ( (LA21_75==RPAREN) && (synpred2_FTS())) {s = 103;}

                         
                        input.seek(index21_75);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA21_72 = input.LA(1);

                         
                        int index21_72 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_FTS()) ) {s = 80;}

                        else if ( (true) ) {s = 70;}

                         
                        input.seek(index21_72);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 21, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA17_eotS =
        "\37\uffff";
    static final String DFA17_eofS =
        "\1\5\36\uffff";
    static final String DFA17_minS =
        "\1\51\1\61\32\uffff\1\0\2\uffff";
    static final String DFA17_maxS =
        "\1\111\1\105\32\uffff\1\0\2\uffff";
    static final String DFA17_acceptS =
        "\2\uffff\32\2\1\uffff\1\2\1\1";
    static final String DFA17_specialS =
        "\34\uffff\1\0\2\uffff}>";
    static final String[] DFA17_transitionS = {
            "\1\27\1\10\1\30\1\25\1\6\1\26\1\uffff\1\1\1\15\1\2\1\uffff"+
            "\1\20\1\21\1\13\3\15\1\11\1\22\1\15\1\17\1\uffff\1\23\1\24\2"+
            "\uffff\1\14\1\16\1\17\1\7\1\3\1\4\1\12",
            "\1\34\4\uffff\1\32\5\35\1\34\1\35\5\uffff\1\31\1\33\1\35",
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
            ""
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
            return "447:19: ( ( fuzzy )=> fuzzy )?";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA17_28 = input.LA(1);

                         
                        int index17_28 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred3_FTS()) ) {s = 30;}

                        else if ( (true) ) {s = 29;}

                         
                        input.seek(index17_28);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 17, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA18_eotS =
        "\37\uffff";
    static final String DFA18_eofS =
        "\1\5\36\uffff";
    static final String DFA18_minS =
        "\1\51\1\61\32\uffff\1\0\2\uffff";
    static final String DFA18_maxS =
        "\1\111\1\105\32\uffff\1\0\2\uffff";
    static final String DFA18_acceptS =
        "\2\uffff\32\2\1\uffff\1\2\1\1";
    static final String DFA18_specialS =
        "\34\uffff\1\0\2\uffff}>";
    static final String[] DFA18_transitionS = {
            "\1\27\1\10\1\30\1\25\1\6\1\26\1\uffff\1\1\1\15\1\2\1\uffff"+
            "\1\20\1\21\1\13\3\15\1\11\1\22\1\15\1\17\1\uffff\1\23\1\24\2"+
            "\uffff\1\14\1\16\1\17\1\7\1\3\1\4\1\12",
            "\1\34\4\uffff\1\32\5\35\1\34\1\35\5\uffff\1\31\1\33\1\35",
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
            ""
    };

    static final short[] DFA18_eot = DFA.unpackEncodedString(DFA18_eotS);
    static final short[] DFA18_eof = DFA.unpackEncodedString(DFA18_eofS);
    static final char[] DFA18_min = DFA.unpackEncodedStringToUnsignedChars(DFA18_minS);
    static final char[] DFA18_max = DFA.unpackEncodedStringToUnsignedChars(DFA18_maxS);
    static final short[] DFA18_accept = DFA.unpackEncodedString(DFA18_acceptS);
    static final short[] DFA18_special = DFA.unpackEncodedString(DFA18_specialS);
    static final short[][] DFA18_transition;

    static {
        int numStates = DFA18_transitionS.length;
        DFA18_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA18_transition[i] = DFA.unpackEncodedString(DFA18_transitionS[i]);
        }
    }

    class DFA18 extends DFA {

        public DFA18(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 18;
            this.eot = DFA18_eot;
            this.eof = DFA18_eof;
            this.min = DFA18_min;
            this.max = DFA18_max;
            this.accept = DFA18_accept;
            this.special = DFA18_special;
            this.transition = DFA18_transition;
        }
        public String getDescription() {
            return "450:24: ( ( fuzzy )=> fuzzy )?";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA18_28 = input.LA(1);

                         
                        int index18_28 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_FTS()) ) {s = 30;}

                        else if ( (true) ) {s = 29;}

                         
                        input.seek(index18_28);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 18, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA19_eotS =
        "\37\uffff";
    static final String DFA19_eofS =
        "\1\5\36\uffff";
    static final String DFA19_minS =
        "\1\51\1\61\27\uffff\1\0\5\uffff";
    static final String DFA19_maxS =
        "\1\111\1\105\27\uffff\1\0\5\uffff";
    static final String DFA19_acceptS =
        "\2\uffff\27\2\1\uffff\4\2\1\1";
    static final String DFA19_specialS =
        "\31\uffff\1\0\5\uffff}>";
    static final String[] DFA19_transitionS = {
            "\1\27\1\10\1\30\1\25\1\6\1\26\1\uffff\1\1\1\15\1\2\1\uffff"+
            "\1\20\1\21\1\13\3\15\1\11\1\22\1\15\1\17\1\uffff\1\23\1\24\2"+
            "\uffff\1\14\1\16\1\17\1\7\1\3\1\4\1\12",
            "\1\31\4\uffff\1\33\7\35\5\uffff\1\32\1\34\1\35",
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
            return "453:21: ( ( slop )=> slop )?";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA19_25 = input.LA(1);

                         
                        int index19_25 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred5_FTS()) ) {s = 30;}

                        else if ( (true) ) {s = 29;}

                         
                        input.seek(index19_25);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 19, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA20_eotS =
        "\37\uffff";
    static final String DFA20_eofS =
        "\1\5\36\uffff";
    static final String DFA20_minS =
        "\1\51\1\61\32\uffff\1\0\2\uffff";
    static final String DFA20_maxS =
        "\1\111\1\105\32\uffff\1\0\2\uffff";
    static final String DFA20_acceptS =
        "\2\uffff\32\2\1\uffff\1\2\1\1";
    static final String DFA20_specialS =
        "\34\uffff\1\0\2\uffff}>";
    static final String[] DFA20_transitionS = {
            "\1\27\1\10\1\30\1\25\1\6\1\26\1\uffff\1\1\1\15\1\2\1\uffff"+
            "\1\20\1\21\1\13\3\15\1\11\1\22\1\15\1\17\1\uffff\1\23\1\24\2"+
            "\uffff\1\14\1\16\1\17\1\7\1\3\1\4\1\12",
            "\1\34\4\uffff\1\32\5\35\1\34\1\35\5\uffff\1\31\1\33\1\35",
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
            ""
    };

    static final short[] DFA20_eot = DFA.unpackEncodedString(DFA20_eotS);
    static final short[] DFA20_eof = DFA.unpackEncodedString(DFA20_eofS);
    static final char[] DFA20_min = DFA.unpackEncodedStringToUnsignedChars(DFA20_minS);
    static final char[] DFA20_max = DFA.unpackEncodedStringToUnsignedChars(DFA20_maxS);
    static final short[] DFA20_accept = DFA.unpackEncodedString(DFA20_acceptS);
    static final short[] DFA20_special = DFA.unpackEncodedString(DFA20_specialS);
    static final short[][] DFA20_transition;

    static {
        int numStates = DFA20_transitionS.length;
        DFA20_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA20_transition[i] = DFA.unpackEncodedString(DFA20_transitionS[i]);
        }
    }

    class DFA20 extends DFA {

        public DFA20(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 20;
            this.eot = DFA20_eot;
            this.eof = DFA20_eof;
            this.min = DFA20_min;
            this.max = DFA20_max;
            this.accept = DFA20_accept;
            this.special = DFA20_special;
            this.transition = DFA20_transition;
        }
        public String getDescription() {
            return "456:22: ( ( fuzzy )=> fuzzy )?";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA20_28 = input.LA(1);

                         
                        int index20_28 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_FTS()) ) {s = 30;}

                        else if ( (true) ) {s = 29;}

                         
                        input.seek(index20_28);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 20, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA26_eotS =
        "\36\uffff";
    static final String DFA26_eofS =
        "\2\uffff\1\11\33\uffff";
    static final String DFA26_minS =
        "\1\61\1\uffff\1\51\33\uffff";
    static final String DFA26_maxS =
        "\1\105\1\uffff\1\111\33\uffff";
    static final String DFA26_acceptS =
        "\1\uffff\1\1\1\uffff\1\1\31\2\1\1";
    static final String DFA26_specialS =
        "\36\uffff}>";
    static final String[] DFA26_transitionS = {
            "\1\4\4\uffff\1\2\7\4\5\uffff\1\1\1\3\1\4",
            "",
            "\1\33\1\14\1\34\1\31\1\12\1\32\1\uffff\1\5\1\21\1\6\1\35\1"+
            "\24\1\25\1\17\3\21\1\15\1\26\1\21\1\23\1\uffff\1\27\1\30\2\uffff"+
            "\1\20\1\22\1\23\1\13\1\7\1\10\1\16",
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
            return "513:9: ( fieldReference COLON )?";
        }
    }
    static final String DFA29_eotS =
        "\22\uffff";
    static final String DFA29_eofS =
        "\22\uffff";
    static final String DFA29_minS =
        "\1\51\2\uffff\16\0\1\uffff";
    static final String DFA29_maxS =
        "\1\111\2\uffff\16\0\1\uffff";
    static final String DFA29_acceptS =
        "\1\uffff\2\1\16\uffff\1\2";
    static final String DFA29_specialS =
        "\1\0\2\uffff\1\1\1\2\1\3\1\4\1\5\1\6\1\7\1\10\1\11\1\12\1\13\1"+
        "\14\1\15\1\16\1\uffff}>";
    static final String[] DFA29_transitionS = {
            "\1\16\1\17\1\20\1\15\3\uffff\1\11\1\5\2\uffff\1\7\1\10\4\5"+
            "\1\3\1\12\1\5\1\6\1\uffff\1\13\1\14\4\uffff\1\6\1\21\1\1\1\2"+
            "\1\4",
            "",
            "",
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
            ""
    };

    static final short[] DFA29_eot = DFA.unpackEncodedString(DFA29_eotS);
    static final short[] DFA29_eof = DFA.unpackEncodedString(DFA29_eofS);
    static final char[] DFA29_min = DFA.unpackEncodedStringToUnsignedChars(DFA29_minS);
    static final char[] DFA29_max = DFA.unpackEncodedStringToUnsignedChars(DFA29_maxS);
    static final short[] DFA29_accept = DFA.unpackEncodedString(DFA29_acceptS);
    static final short[] DFA29_special = DFA.unpackEncodedString(DFA29_specialS);
    static final short[][] DFA29_transition;

    static {
        int numStates = DFA29_transitionS.length;
        DFA29_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA29_transition[i] = DFA.unpackEncodedString(DFA29_transitionS[i]);
        }
    }

    class DFA29 extends DFA {

        public DFA29(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 29;
            this.eot = DFA29_eot;
            this.eof = DFA29_eof;
            this.min = DFA29_min;
            this.max = DFA29_max;
            this.accept = DFA29_accept;
            this.special = DFA29_special;
            this.transition = DFA29_transition;
        }
        public String getDescription() {
            return "560:1: ftsFieldGroupDisjunction : ({...}? => ftsFieldGroupExplicitDisjunction | {...}? => ftsFieldGroupImplicitDisjunction );";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA29_0 = input.LA(1);

                         
                        int index29_0 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA29_0==AND) && ((defaultFieldConjunction() == true))) {s = 1;}

                        else if ( (LA29_0==AMP) && ((defaultFieldConjunction() == true))) {s = 2;}

                        else if ( (LA29_0==NOT) && (((defaultFieldConjunction() == false)||(defaultFieldConjunction() == true)))) {s = 3;}

                        else if ( (LA29_0==EXCLAMATION) && (((defaultFieldConjunction() == false)||(defaultFieldConjunction() == true)))) {s = 4;}

                        else if ( (LA29_0==DECIMAL_INTEGER_LITERAL||(LA29_0>=ID && LA29_0<=FTSWILD)||LA29_0==FLOATING_POINT_LITERAL) && (((defaultFieldConjunction() == false)||(defaultFieldConjunction() == true)))) {s = 5;}

                        else if ( (LA29_0==STAR||LA29_0==QUESTION_MARK) && (((defaultFieldConjunction() == false)||(defaultFieldConjunction() == true)))) {s = 6;}

                        else if ( (LA29_0==EQUALS) && (((defaultFieldConjunction() == false)||(defaultFieldConjunction() == true)))) {s = 7;}

                        else if ( (LA29_0==FTSPHRASE) && (((defaultFieldConjunction() == false)||(defaultFieldConjunction() == true)))) {s = 8;}

                        else if ( (LA29_0==TILDA) && (((defaultFieldConjunction() == false)||(defaultFieldConjunction() == true)))) {s = 9;}

                        else if ( (LA29_0==TO) && (((defaultFieldConjunction() == false)||(defaultFieldConjunction() == true)))) {s = 10;}

                        else if ( (LA29_0==LSQUARE) && (((defaultFieldConjunction() == false)||(defaultFieldConjunction() == true)))) {s = 11;}

                        else if ( (LA29_0==LT) && (((defaultFieldConjunction() == false)||(defaultFieldConjunction() == true)))) {s = 12;}

                        else if ( (LA29_0==LPAREN) && (((defaultFieldConjunction() == false)||(defaultFieldConjunction() == true)))) {s = 13;}

                        else if ( (LA29_0==PLUS) && (((defaultFieldConjunction() == false)||(defaultFieldConjunction() == true)))) {s = 14;}

                        else if ( (LA29_0==BAR) && (((defaultFieldConjunction() == false)||(defaultFieldConjunction() == true)))) {s = 15;}

                        else if ( (LA29_0==MINUS) && (((defaultFieldConjunction() == false)||(defaultFieldConjunction() == true)))) {s = 16;}

                        else if ( (LA29_0==OR) && ((defaultFieldConjunction() == false))) {s = 17;}

                         
                        input.seek(index29_0);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA29_3 = input.LA(1);

                         
                        int index29_3 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((defaultFieldConjunction() == true)) ) {s = 2;}

                        else if ( ((defaultFieldConjunction() == false)) ) {s = 17;}

                         
                        input.seek(index29_3);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA29_4 = input.LA(1);

                         
                        int index29_4 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((defaultFieldConjunction() == true)) ) {s = 2;}

                        else if ( ((defaultFieldConjunction() == false)) ) {s = 17;}

                         
                        input.seek(index29_4);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA29_5 = input.LA(1);

                         
                        int index29_5 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((defaultFieldConjunction() == true)) ) {s = 2;}

                        else if ( ((defaultFieldConjunction() == false)) ) {s = 17;}

                         
                        input.seek(index29_5);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA29_6 = input.LA(1);

                         
                        int index29_6 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((defaultFieldConjunction() == true)) ) {s = 2;}

                        else if ( ((defaultFieldConjunction() == false)) ) {s = 17;}

                         
                        input.seek(index29_6);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA29_7 = input.LA(1);

                         
                        int index29_7 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((defaultFieldConjunction() == true)) ) {s = 2;}

                        else if ( ((defaultFieldConjunction() == false)) ) {s = 17;}

                         
                        input.seek(index29_7);
                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA29_8 = input.LA(1);

                         
                        int index29_8 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((defaultFieldConjunction() == true)) ) {s = 2;}

                        else if ( ((defaultFieldConjunction() == false)) ) {s = 17;}

                         
                        input.seek(index29_8);
                        if ( s>=0 ) return s;
                        break;
                    case 7 : 
                        int LA29_9 = input.LA(1);

                         
                        int index29_9 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((defaultFieldConjunction() == true)) ) {s = 2;}

                        else if ( ((defaultFieldConjunction() == false)) ) {s = 17;}

                         
                        input.seek(index29_9);
                        if ( s>=0 ) return s;
                        break;
                    case 8 : 
                        int LA29_10 = input.LA(1);

                         
                        int index29_10 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((defaultFieldConjunction() == true)) ) {s = 2;}

                        else if ( ((defaultFieldConjunction() == false)) ) {s = 17;}

                         
                        input.seek(index29_10);
                        if ( s>=0 ) return s;
                        break;
                    case 9 : 
                        int LA29_11 = input.LA(1);

                         
                        int index29_11 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((defaultFieldConjunction() == true)) ) {s = 2;}

                        else if ( ((defaultFieldConjunction() == false)) ) {s = 17;}

                         
                        input.seek(index29_11);
                        if ( s>=0 ) return s;
                        break;
                    case 10 : 
                        int LA29_12 = input.LA(1);

                         
                        int index29_12 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((defaultFieldConjunction() == true)) ) {s = 2;}

                        else if ( ((defaultFieldConjunction() == false)) ) {s = 17;}

                         
                        input.seek(index29_12);
                        if ( s>=0 ) return s;
                        break;
                    case 11 : 
                        int LA29_13 = input.LA(1);

                         
                        int index29_13 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((defaultFieldConjunction() == true)) ) {s = 2;}

                        else if ( ((defaultFieldConjunction() == false)) ) {s = 17;}

                         
                        input.seek(index29_13);
                        if ( s>=0 ) return s;
                        break;
                    case 12 : 
                        int LA29_14 = input.LA(1);

                         
                        int index29_14 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((defaultFieldConjunction() == true)) ) {s = 2;}

                        else if ( ((defaultFieldConjunction() == false)) ) {s = 17;}

                         
                        input.seek(index29_14);
                        if ( s>=0 ) return s;
                        break;
                    case 13 : 
                        int LA29_15 = input.LA(1);

                         
                        int index29_15 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((defaultFieldConjunction() == true)) ) {s = 2;}

                        else if ( ((defaultFieldConjunction() == false)) ) {s = 17;}

                         
                        input.seek(index29_15);
                        if ( s>=0 ) return s;
                        break;
                    case 14 : 
                        int LA29_16 = input.LA(1);

                         
                        int index29_16 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((defaultFieldConjunction() == true)) ) {s = 2;}

                        else if ( ((defaultFieldConjunction() == false)) ) {s = 17;}

                         
                        input.seek(index29_16);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 29, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA32_eotS =
        "\21\uffff";
    static final String DFA32_eofS =
        "\21\uffff";
    static final String DFA32_minS =
        "\1\51\20\uffff";
    static final String DFA32_maxS =
        "\1\111\20\uffff";
    static final String DFA32_acceptS =
        "\1\uffff\1\2\17\1";
    static final String DFA32_specialS =
        "\21\uffff}>";
    static final String[] DFA32_transitionS = {
            "\1\17\1\3\1\20\1\16\1\1\2\uffff\1\12\1\6\2\uffff\1\10\1\11"+
            "\4\6\1\4\1\13\1\6\1\7\1\uffff\1\14\1\15\4\uffff\1\7\1\2\2\uffff"+
            "\1\5",
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

    static final short[] DFA32_eot = DFA.unpackEncodedString(DFA32_eotS);
    static final short[] DFA32_eof = DFA.unpackEncodedString(DFA32_eofS);
    static final char[] DFA32_min = DFA.unpackEncodedStringToUnsignedChars(DFA32_minS);
    static final char[] DFA32_max = DFA.unpackEncodedStringToUnsignedChars(DFA32_maxS);
    static final short[] DFA32_accept = DFA.unpackEncodedString(DFA32_acceptS);
    static final short[] DFA32_special = DFA.unpackEncodedString(DFA32_specialS);
    static final short[][] DFA32_transition;

    static {
        int numStates = DFA32_transitionS.length;
        DFA32_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA32_transition[i] = DFA.unpackEncodedString(DFA32_transitionS[i]);
        }
    }

    class DFA32 extends DFA {

        public DFA32(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 32;
            this.eot = DFA32_eot;
            this.eof = DFA32_eof;
            this.min = DFA32_min;
            this.max = DFA32_max;
            this.accept = DFA32_accept;
            this.special = DFA32_special;
            this.transition = DFA32_transition;
        }
        public String getDescription() {
            return "()+ loopback of 575:9: ( ( or )? ftsFieldGroupExplicitConjunction )+";
        }
    }
    static final String DFA31_eotS =
        "\32\uffff";
    static final String DFA31_eofS =
        "\32\uffff";
    static final String DFA31_minS =
        "\1\51\1\uffff\1\52\27\uffff";
    static final String DFA31_maxS =
        "\1\111\1\uffff\1\105\27\uffff";
    static final String DFA31_acceptS =
        "\1\uffff\1\1\1\uffff\15\2\1\1\11\2";
    static final String DFA31_specialS =
        "\32\uffff}>";
    static final String[] DFA31_transitionS = {
            "\1\16\1\2\1\17\1\15\3\uffff\1\11\1\5\2\uffff\1\7\1\10\4\5\1"+
            "\3\1\12\1\5\1\6\1\uffff\1\13\1\14\4\uffff\1\6\1\1\2\uffff\1"+
            "\4",
            "",
            "\1\20\1\uffff\1\31\3\uffff\1\25\1\21\2\uffff\1\23\1\24\4\21"+
            "\2\26\1\21\1\22\1\uffff\1\27\1\30\4\uffff\1\22",
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
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            ""
    };

    static final short[] DFA31_eot = DFA.unpackEncodedString(DFA31_eotS);
    static final short[] DFA31_eof = DFA.unpackEncodedString(DFA31_eofS);
    static final char[] DFA31_min = DFA.unpackEncodedStringToUnsignedChars(DFA31_minS);
    static final char[] DFA31_max = DFA.unpackEncodedStringToUnsignedChars(DFA31_maxS);
    static final short[] DFA31_accept = DFA.unpackEncodedString(DFA31_acceptS);
    static final short[] DFA31_special = DFA.unpackEncodedString(DFA31_specialS);
    static final short[][] DFA31_transition;

    static {
        int numStates = DFA31_transitionS.length;
        DFA31_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA31_transition[i] = DFA.unpackEncodedString(DFA31_transitionS[i]);
        }
    }

    class DFA31 extends DFA {

        public DFA31(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 31;
            this.eot = DFA31_eot;
            this.eof = DFA31_eof;
            this.min = DFA31_min;
            this.max = DFA31_max;
            this.accept = DFA31_accept;
            this.special = DFA31_special;
            this.transition = DFA31_transition;
        }
        public String getDescription() {
            return "575:10: ( or )?";
        }
    }
    static final String DFA35_eotS =
        "\35\uffff";
    static final String DFA35_eofS =
        "\35\uffff";
    static final String DFA35_minS =
        "\1\51\1\uffff\1\52\32\uffff";
    static final String DFA35_maxS =
        "\1\111\1\uffff\1\105\32\uffff";
    static final String DFA35_acceptS =
        "\1\uffff\1\2\1\uffff\1\2\17\1\1\2\11\1";
    static final String DFA35_specialS =
        "\35\uffff}>";
    static final String[] DFA35_transitionS = {
            "\1\21\1\2\1\22\1\20\1\3\2\uffff\1\14\1\10\2\uffff\1\12\1\13"+
            "\4\10\1\6\1\15\1\10\1\11\1\uffff\1\16\1\17\4\uffff\1\11\1\1"+
            "\1\4\1\5\1\7",
            "",
            "\1\23\1\uffff\1\34\3\uffff\1\30\1\24\2\uffff\1\26\1\27\4\24"+
            "\2\31\1\24\1\25\1\uffff\1\32\1\33\4\uffff\1\25",
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

    static final short[] DFA35_eot = DFA.unpackEncodedString(DFA35_eotS);
    static final short[] DFA35_eof = DFA.unpackEncodedString(DFA35_eofS);
    static final char[] DFA35_min = DFA.unpackEncodedStringToUnsignedChars(DFA35_minS);
    static final char[] DFA35_max = DFA.unpackEncodedStringToUnsignedChars(DFA35_maxS);
    static final short[] DFA35_accept = DFA.unpackEncodedString(DFA35_acceptS);
    static final short[] DFA35_special = DFA.unpackEncodedString(DFA35_specialS);
    static final short[][] DFA35_transition;

    static {
        int numStates = DFA35_transitionS.length;
        DFA35_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA35_transition[i] = DFA.unpackEncodedString(DFA35_transitionS[i]);
        }
    }

    class DFA35 extends DFA {

        public DFA35(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 35;
            this.eot = DFA35_eot;
            this.eof = DFA35_eof;
            this.min = DFA35_min;
            this.max = DFA35_max;
            this.accept = DFA35_accept;
            this.special = DFA35_special;
            this.transition = DFA35_transition;
        }
        public String getDescription() {
            return "()+ loopback of 593:9: ( ( and )? ftsFieldGroupPrefixed )+";
        }
    }
    static final String DFA34_eotS =
        "\21\uffff";
    static final String DFA34_eofS =
        "\21\uffff";
    static final String DFA34_minS =
        "\1\51\20\uffff";
    static final String DFA34_maxS =
        "\1\111\20\uffff";
    static final String DFA34_acceptS =
        "\1\uffff\2\1\16\2";
    static final String DFA34_specialS =
        "\21\uffff}>";
    static final String[] DFA34_transitionS = {
            "\1\16\1\17\1\20\1\15\3\uffff\1\11\1\5\2\uffff\1\7\1\10\4\5"+
            "\1\3\1\12\1\5\1\6\1\uffff\1\13\1\14\4\uffff\1\6\1\uffff\1\1"+
            "\1\2\1\4",
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

    static final short[] DFA34_eot = DFA.unpackEncodedString(DFA34_eotS);
    static final short[] DFA34_eof = DFA.unpackEncodedString(DFA34_eofS);
    static final char[] DFA34_min = DFA.unpackEncodedStringToUnsignedChars(DFA34_minS);
    static final char[] DFA34_max = DFA.unpackEncodedStringToUnsignedChars(DFA34_maxS);
    static final short[] DFA34_accept = DFA.unpackEncodedString(DFA34_acceptS);
    static final short[] DFA34_special = DFA.unpackEncodedString(DFA34_specialS);
    static final short[][] DFA34_transition;

    static {
        int numStates = DFA34_transitionS.length;
        DFA34_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA34_transition[i] = DFA.unpackEncodedString(DFA34_transitionS[i]);
        }
    }

    class DFA34 extends DFA {

        public DFA34(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 34;
            this.eot = DFA34_eot;
            this.eof = DFA34_eof;
            this.min = DFA34_min;
            this.max = DFA34_max;
            this.accept = DFA34_accept;
            this.special = DFA34_special;
            this.transition = DFA34_transition;
        }
        public String getDescription() {
            return "593:10: ( and )?";
        }
    }
    static final String DFA41_eotS =
        "\17\uffff";
    static final String DFA41_eofS =
        "\17\uffff";
    static final String DFA41_minS =
        "\1\51\1\0\15\uffff";
    static final String DFA41_maxS =
        "\1\111\1\0\15\uffff";
    static final String DFA41_acceptS =
        "\2\uffff\1\1\11\2\1\3\1\4\1\5";
    static final String DFA41_specialS =
        "\1\0\1\1\15\uffff}>";
    static final String[] DFA41_transitionS = {
            "\1\14\1\15\1\16\1\13\3\uffff\1\7\1\3\2\uffff\1\5\1\6\4\3\1"+
            "\1\1\10\1\3\1\4\1\uffff\1\11\1\12\4\uffff\1\4\3\uffff\1\2",
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
            ""
    };

    static final short[] DFA41_eot = DFA.unpackEncodedString(DFA41_eotS);
    static final short[] DFA41_eof = DFA.unpackEncodedString(DFA41_eofS);
    static final char[] DFA41_min = DFA.unpackEncodedStringToUnsignedChars(DFA41_minS);
    static final char[] DFA41_max = DFA.unpackEncodedStringToUnsignedChars(DFA41_maxS);
    static final short[] DFA41_accept = DFA.unpackEncodedString(DFA41_acceptS);
    static final short[] DFA41_special = DFA.unpackEncodedString(DFA41_specialS);
    static final short[][] DFA41_transition;

    static {
        int numStates = DFA41_transitionS.length;
        DFA41_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA41_transition[i] = DFA.unpackEncodedString(DFA41_transitionS[i]);
        }
    }

    class DFA41 extends DFA {

        public DFA41(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 41;
            this.eot = DFA41_eot;
            this.eof = DFA41_eof;
            this.min = DFA41_min;
            this.max = DFA41_max;
            this.accept = DFA41_accept;
            this.special = DFA41_special;
            this.transition = DFA41_transition;
        }
        public String getDescription() {
            return "598:1: ftsFieldGroupPrefixed : ( ( not )=> not ftsFieldGroupTest ( boost )? -> ^( FIELD_NEGATION ftsFieldGroupTest ( boost )? ) | ftsFieldGroupTest ( boost )? -> ^( FIELD_DEFAULT ftsFieldGroupTest ( boost )? ) | PLUS ftsFieldGroupTest ( boost )? -> ^( FIELD_MANDATORY ftsFieldGroupTest ( boost )? ) | BAR ftsFieldGroupTest ( boost )? -> ^( FIELD_OPTIONAL ftsFieldGroupTest ( boost )? ) | MINUS ftsFieldGroupTest ( boost )? -> ^( FIELD_EXCLUDE ftsFieldGroupTest ( boost )? ) );";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA41_0 = input.LA(1);

                         
                        int index41_0 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA41_0==NOT) ) {s = 1;}

                        else if ( (LA41_0==EXCLAMATION) && (synpred7_FTS())) {s = 2;}

                        else if ( (LA41_0==DECIMAL_INTEGER_LITERAL||(LA41_0>=ID && LA41_0<=FTSWILD)||LA41_0==FLOATING_POINT_LITERAL) ) {s = 3;}

                        else if ( (LA41_0==STAR||LA41_0==QUESTION_MARK) ) {s = 4;}

                        else if ( (LA41_0==EQUALS) ) {s = 5;}

                        else if ( (LA41_0==FTSPHRASE) ) {s = 6;}

                        else if ( (LA41_0==TILDA) ) {s = 7;}

                        else if ( (LA41_0==TO) ) {s = 8;}

                        else if ( (LA41_0==LSQUARE) ) {s = 9;}

                        else if ( (LA41_0==LT) ) {s = 10;}

                        else if ( (LA41_0==LPAREN) ) {s = 11;}

                        else if ( (LA41_0==PLUS) ) {s = 12;}

                        else if ( (LA41_0==BAR) ) {s = 13;}

                        else if ( (LA41_0==MINUS) ) {s = 14;}

                         
                        input.seek(index41_0);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA41_1 = input.LA(1);

                         
                        int index41_1 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred7_FTS()) ) {s = 2;}

                        else if ( (true) ) {s = 11;}

                         
                        input.seek(index41_1);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 41, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA46_eotS =
        "\130\uffff";
    static final String DFA46_eofS =
        "\130\uffff";
    static final String DFA46_minS =
        "\1\54\1\51\2\uffff\1\51\1\uffff\1\51\3\uffff\1\51\53\uffff\3\0"+
        "\1\51\1\uffff\1\51\26\uffff\1\51\5\uffff";
    static final String DFA46_maxS =
        "\1\105\1\111\2\uffff\1\111\1\uffff\1\111\3\uffff\1\111\53\uffff"+
        "\3\0\1\111\1\uffff\1\111\26\uffff\1\111\5\uffff";
    static final String DFA46_acceptS =
        "\2\uffff\1\2\1\3\1\uffff\1\5\1\uffff\2\6\1\7\1\uffff\1\6\23\2\23"+
        "\4\4\2\4\uffff\1\1\1\uffff\1\1\25\2\1\uffff\5\2";
    static final String DFA46_specialS =
        "\66\uffff\1\2\1\0\1\1\1\3\36\uffff}>";
    static final String[] DFA46_transitionS = {
            "\1\11\3\uffff\1\5\1\1\2\uffff\1\3\1\4\4\1\2\6\1\1\1\2\1\uffff"+
            "\1\7\1\10\4\uffff\1\2",
            "\1\35\1\22\1\36\1\34\1\20\2\uffff\1\14\1\25\1\15\1\uffff\1"+
            "\27\1\30\4\25\1\23\1\31\1\25\1\12\1\13\1\32\1\33\4\uffff\1\26"+
            "\1\21\1\16\1\17\1\24",
            "",
            "",
            "\1\60\1\45\1\61\1\57\1\43\2\uffff\1\37\1\50\1\40\1\uffff\1"+
            "\52\1\53\4\50\1\46\1\54\1\50\1\51\1\13\1\55\1\56\4\uffff\1\51"+
            "\1\44\1\41\1\42\1\47",
            "",
            "\1\35\1\22\1\36\1\34\1\20\2\uffff\1\14\1\25\1\15\1\uffff\1"+
            "\27\1\30\4\25\1\23\1\31\1\25\1\12\1\uffff\1\32\1\33\4\uffff"+
            "\1\26\1\21\1\16\1\17\1\24",
            "",
            "",
            "",
            "\1\35\1\22\1\36\1\71\1\20\2\uffff\1\62\1\67\1\63\1\uffff\1"+
            "\27\1\30\4\67\1\66\1\70\1\67\1\26\1\uffff\1\32\1\33\4\uffff"+
            "\1\26\1\21\1\64\1\65\1\24",
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
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\112\1\113\1\114\1\111\1\74\2\uffff\1\105\1\73\2\uffff\1"+
            "\103\1\104\4\101\1\77\1\106\1\101\1\102\1\uffff\1\107\1\110"+
            "\4\uffff\1\102\1\115\1\75\1\76\1\100",
            "",
            "\1\112\1\121\1\114\1\111\1\122\2\uffff\1\116\1\101\1\117\1"+
            "\uffff\1\103\1\104\4\101\1\77\1\106\1\101\1\125\1\126\1\107"+
            "\1\110\4\uffff\1\102\1\120\1\123\1\124\1\100",
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
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\35\1\22\1\36\1\34\1\20\2\uffff\1\127\1\67\1\63\1\uffff"+
            "\1\27\1\30\4\67\1\66\1\70\1\67\1\26\1\uffff\1\32\1\33\4\uffff"+
            "\1\26\1\21\1\64\1\65\1\24",
            "",
            "",
            "",
            "",
            ""
    };

    static final short[] DFA46_eot = DFA.unpackEncodedString(DFA46_eotS);
    static final short[] DFA46_eof = DFA.unpackEncodedString(DFA46_eofS);
    static final char[] DFA46_min = DFA.unpackEncodedStringToUnsignedChars(DFA46_minS);
    static final char[] DFA46_max = DFA.unpackEncodedStringToUnsignedChars(DFA46_maxS);
    static final short[] DFA46_accept = DFA.unpackEncodedString(DFA46_acceptS);
    static final short[] DFA46_special = DFA.unpackEncodedString(DFA46_specialS);
    static final short[][] DFA46_transition;

    static {
        int numStates = DFA46_transitionS.length;
        DFA46_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA46_transition[i] = DFA.unpackEncodedString(DFA46_transitionS[i]);
        }
    }

    class DFA46 extends DFA {

        public DFA46(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 46;
            this.eot = DFA46_eot;
            this.eof = DFA46_eof;
            this.min = DFA46_min;
            this.max = DFA46_max;
            this.accept = DFA46_accept;
            this.special = DFA46_special;
            this.transition = DFA46_transition;
        }
        public String getDescription() {
            return "617:1: ftsFieldGroupTest : ( ( ftsFieldGroupProximity )=> ftsFieldGroupProximity -> ^( FG_PROXIMITY ftsFieldGroupProximity ) | ftsFieldGroupTerm ( ( fuzzy )=> fuzzy )? -> ^( FG_TERM ftsFieldGroupTerm ( fuzzy )? ) | ftsFieldGroupExactTerm ( ( fuzzy )=> fuzzy )? -> ^( FG_EXACT_TERM ftsFieldGroupExactTerm ( fuzzy )? ) | ftsFieldGroupPhrase ( ( slop )=> slop )? -> ^( FG_PHRASE ftsFieldGroupPhrase ( slop )? ) | ftsFieldGroupSynonym ( ( fuzzy )=> fuzzy )? -> ^( FG_SYNONYM ftsFieldGroupSynonym ( fuzzy )? ) | ftsFieldGroupRange -> ^( FG_RANGE ftsFieldGroupRange ) | LPAREN ftsFieldGroupDisjunction RPAREN -> ftsFieldGroupDisjunction );";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA46_55 = input.LA(1);

                         
                        int index46_55 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_FTS()) ) {s = 58;}

                        else if ( (true) ) {s = 53;}

                         
                        input.seek(index46_55);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA46_56 = input.LA(1);

                         
                        int index46_56 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_FTS()) ) {s = 58;}

                        else if ( (true) ) {s = 53;}

                         
                        input.seek(index46_56);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA46_54 = input.LA(1);

                         
                        int index46_54 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_FTS()) ) {s = 58;}

                        else if ( (true) ) {s = 53;}

                         
                        input.seek(index46_54);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA46_57 = input.LA(1);

                         
                        int index46_57 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA46_57==DECIMAL_INTEGER_LITERAL) ) {s = 59;}

                        else if ( (LA46_57==RPAREN) && (synpred8_FTS())) {s = 60;}

                        else if ( (LA46_57==AND) ) {s = 61;}

                        else if ( (LA46_57==AMP) ) {s = 62;}

                        else if ( (LA46_57==NOT) ) {s = 63;}

                        else if ( (LA46_57==EXCLAMATION) ) {s = 64;}

                        else if ( ((LA46_57>=ID && LA46_57<=FTSWILD)||LA46_57==FLOATING_POINT_LITERAL) ) {s = 65;}

                        else if ( (LA46_57==STAR||LA46_57==QUESTION_MARK) ) {s = 66;}

                        else if ( (LA46_57==EQUALS) ) {s = 67;}

                        else if ( (LA46_57==FTSPHRASE) ) {s = 68;}

                        else if ( (LA46_57==TILDA) ) {s = 69;}

                        else if ( (LA46_57==TO) ) {s = 70;}

                        else if ( (LA46_57==LSQUARE) ) {s = 71;}

                        else if ( (LA46_57==LT) ) {s = 72;}

                        else if ( (LA46_57==LPAREN) ) {s = 73;}

                        else if ( (LA46_57==PLUS) ) {s = 74;}

                        else if ( (LA46_57==BAR) ) {s = 75;}

                        else if ( (LA46_57==MINUS) ) {s = 76;}

                        else if ( (LA46_57==OR) ) {s = 77;}

                         
                        input.seek(index46_57);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 46, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA42_eotS =
        "\27\uffff";
    static final String DFA42_eofS =
        "\27\uffff";
    static final String DFA42_minS =
        "\1\51\1\61\22\uffff\1\0\2\uffff";
    static final String DFA42_maxS =
        "\1\111\1\105\22\uffff\1\0\2\uffff";
    static final String DFA42_acceptS =
        "\2\uffff\22\2\1\uffff\1\2\1\1";
    static final String DFA42_specialS =
        "\24\uffff\1\0\2\uffff}>";
    static final String[] DFA42_transitionS = {
            "\1\22\1\7\1\23\1\21\1\5\2\uffff\1\1\1\12\1\2\1\uffff\1\14\1"+
            "\15\4\12\1\10\1\16\1\12\1\13\1\uffff\1\17\1\20\4\uffff\1\13"+
            "\1\6\1\3\1\4\1\11",
            "\1\24\4\uffff\6\25\1\24\1\25\7\uffff\1\25",
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
            "",
            "",
            "",
            "\1\uffff",
            "",
            ""
    };

    static final short[] DFA42_eot = DFA.unpackEncodedString(DFA42_eotS);
    static final short[] DFA42_eof = DFA.unpackEncodedString(DFA42_eofS);
    static final char[] DFA42_min = DFA.unpackEncodedStringToUnsignedChars(DFA42_minS);
    static final char[] DFA42_max = DFA.unpackEncodedStringToUnsignedChars(DFA42_maxS);
    static final short[] DFA42_accept = DFA.unpackEncodedString(DFA42_acceptS);
    static final short[] DFA42_special = DFA.unpackEncodedString(DFA42_specialS);
    static final short[][] DFA42_transition;

    static {
        int numStates = DFA42_transitionS.length;
        DFA42_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA42_transition[i] = DFA.unpackEncodedString(DFA42_transitionS[i]);
        }
    }

    class DFA42 extends DFA {

        public DFA42(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 42;
            this.eot = DFA42_eot;
            this.eof = DFA42_eof;
            this.min = DFA42_min;
            this.max = DFA42_max;
            this.accept = DFA42_accept;
            this.special = DFA42_special;
            this.transition = DFA42_transition;
        }
        public String getDescription() {
            return "622:29: ( ( fuzzy )=> fuzzy )?";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA42_20 = input.LA(1);

                         
                        int index42_20 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred9_FTS()) ) {s = 22;}

                        else if ( (true) ) {s = 21;}

                         
                        input.seek(index42_20);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 42, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA43_eotS =
        "\27\uffff";
    static final String DFA43_eofS =
        "\27\uffff";
    static final String DFA43_minS =
        "\1\51\1\61\22\uffff\1\0\2\uffff";
    static final String DFA43_maxS =
        "\1\111\1\105\22\uffff\1\0\2\uffff";
    static final String DFA43_acceptS =
        "\2\uffff\22\2\1\uffff\1\2\1\1";
    static final String DFA43_specialS =
        "\24\uffff\1\0\2\uffff}>";
    static final String[] DFA43_transitionS = {
            "\1\22\1\7\1\23\1\21\1\5\2\uffff\1\1\1\12\1\2\1\uffff\1\14\1"+
            "\15\4\12\1\10\1\16\1\12\1\13\1\uffff\1\17\1\20\4\uffff\1\13"+
            "\1\6\1\3\1\4\1\11",
            "\1\24\4\uffff\6\25\1\24\1\25\7\uffff\1\25",
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
            "",
            "",
            "",
            "\1\uffff",
            "",
            ""
    };

    static final short[] DFA43_eot = DFA.unpackEncodedString(DFA43_eotS);
    static final short[] DFA43_eof = DFA.unpackEncodedString(DFA43_eofS);
    static final char[] DFA43_min = DFA.unpackEncodedStringToUnsignedChars(DFA43_minS);
    static final char[] DFA43_max = DFA.unpackEncodedStringToUnsignedChars(DFA43_maxS);
    static final short[] DFA43_accept = DFA.unpackEncodedString(DFA43_acceptS);
    static final short[] DFA43_special = DFA.unpackEncodedString(DFA43_specialS);
    static final short[][] DFA43_transition;

    static {
        int numStates = DFA43_transitionS.length;
        DFA43_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA43_transition[i] = DFA.unpackEncodedString(DFA43_transitionS[i]);
        }
    }

    class DFA43 extends DFA {

        public DFA43(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 43;
            this.eot = DFA43_eot;
            this.eof = DFA43_eof;
            this.min = DFA43_min;
            this.max = DFA43_max;
            this.accept = DFA43_accept;
            this.special = DFA43_special;
            this.transition = DFA43_transition;
        }
        public String getDescription() {
            return "625:34: ( ( fuzzy )=> fuzzy )?";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA43_20 = input.LA(1);

                         
                        int index43_20 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred10_FTS()) ) {s = 22;}

                        else if ( (true) ) {s = 21;}

                         
                        input.seek(index43_20);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 43, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA44_eotS =
        "\27\uffff";
    static final String DFA44_eofS =
        "\27\uffff";
    static final String DFA44_minS =
        "\1\51\1\61\22\uffff\1\0\2\uffff";
    static final String DFA44_maxS =
        "\1\111\1\105\22\uffff\1\0\2\uffff";
    static final String DFA44_acceptS =
        "\2\uffff\22\2\1\uffff\1\2\1\1";
    static final String DFA44_specialS =
        "\24\uffff\1\0\2\uffff}>";
    static final String[] DFA44_transitionS = {
            "\1\22\1\7\1\23\1\21\1\5\2\uffff\1\1\1\12\1\2\1\uffff\1\14\1"+
            "\15\4\12\1\10\1\16\1\12\1\13\1\uffff\1\17\1\20\4\uffff\1\13"+
            "\1\6\1\3\1\4\1\11",
            "\1\24\4\uffff\10\25\7\uffff\1\25",
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
            "",
            "",
            "",
            "\1\uffff",
            "",
            ""
    };

    static final short[] DFA44_eot = DFA.unpackEncodedString(DFA44_eotS);
    static final short[] DFA44_eof = DFA.unpackEncodedString(DFA44_eofS);
    static final char[] DFA44_min = DFA.unpackEncodedStringToUnsignedChars(DFA44_minS);
    static final char[] DFA44_max = DFA.unpackEncodedStringToUnsignedChars(DFA44_maxS);
    static final short[] DFA44_accept = DFA.unpackEncodedString(DFA44_acceptS);
    static final short[] DFA44_special = DFA.unpackEncodedString(DFA44_specialS);
    static final short[][] DFA44_transition;

    static {
        int numStates = DFA44_transitionS.length;
        DFA44_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA44_transition[i] = DFA.unpackEncodedString(DFA44_transitionS[i]);
        }
    }

    class DFA44 extends DFA {

        public DFA44(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 44;
            this.eot = DFA44_eot;
            this.eof = DFA44_eof;
            this.min = DFA44_min;
            this.max = DFA44_max;
            this.accept = DFA44_accept;
            this.special = DFA44_special;
            this.transition = DFA44_transition;
        }
        public String getDescription() {
            return "628:31: ( ( slop )=> slop )?";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA44_20 = input.LA(1);

                         
                        int index44_20 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred11_FTS()) ) {s = 22;}

                        else if ( (true) ) {s = 21;}

                         
                        input.seek(index44_20);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 44, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA45_eotS =
        "\27\uffff";
    static final String DFA45_eofS =
        "\27\uffff";
    static final String DFA45_minS =
        "\1\51\1\61\22\uffff\1\0\2\uffff";
    static final String DFA45_maxS =
        "\1\111\1\105\22\uffff\1\0\2\uffff";
    static final String DFA45_acceptS =
        "\2\uffff\22\2\1\uffff\1\2\1\1";
    static final String DFA45_specialS =
        "\24\uffff\1\0\2\uffff}>";
    static final String[] DFA45_transitionS = {
            "\1\22\1\7\1\23\1\21\1\5\2\uffff\1\1\1\12\1\2\1\uffff\1\14\1"+
            "\15\4\12\1\10\1\16\1\12\1\13\1\uffff\1\17\1\20\4\uffff\1\13"+
            "\1\6\1\3\1\4\1\11",
            "\1\24\4\uffff\6\25\1\24\1\25\7\uffff\1\25",
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
            "",
            "",
            "",
            "\1\uffff",
            "",
            ""
    };

    static final short[] DFA45_eot = DFA.unpackEncodedString(DFA45_eotS);
    static final short[] DFA45_eof = DFA.unpackEncodedString(DFA45_eofS);
    static final char[] DFA45_min = DFA.unpackEncodedStringToUnsignedChars(DFA45_minS);
    static final char[] DFA45_max = DFA.unpackEncodedStringToUnsignedChars(DFA45_maxS);
    static final short[] DFA45_accept = DFA.unpackEncodedString(DFA45_acceptS);
    static final short[] DFA45_special = DFA.unpackEncodedString(DFA45_specialS);
    static final short[][] DFA45_transition;

    static {
        int numStates = DFA45_transitionS.length;
        DFA45_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA45_transition[i] = DFA.unpackEncodedString(DFA45_transitionS[i]);
        }
    }

    class DFA45 extends DFA {

        public DFA45(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 45;
            this.eot = DFA45_eot;
            this.eof = DFA45_eof;
            this.min = DFA45_min;
            this.max = DFA45_max;
            this.accept = DFA45_accept;
            this.special = DFA45_special;
            this.transition = DFA45_transition;
        }
        public String getDescription() {
            return "631:32: ( ( fuzzy )=> fuzzy )?";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA45_20 = input.LA(1);

                         
                        int index45_20 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred12_FTS()) ) {s = 22;}

                        else if ( (true) ) {s = 21;}

                         
                        input.seek(index45_20);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 45, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA47_eotS =
        "\104\uffff";
    static final String DFA47_eofS =
        "\1\4\15\uffff\1\42\61\uffff\1\42\3\uffff";
    static final String DFA47_minS =
        "\1\51\15\uffff\1\51\13\uffff\1\51\1\0\4\uffff\2\0\1\uffff\1\0\11"+
        "\uffff\1\51\22\uffff\1\51\3\uffff";
    static final String DFA47_maxS =
        "\1\111\15\uffff\1\111\13\uffff\1\111\1\0\4\uffff\2\0\1\uffff\1"+
        "\0\11\uffff\1\111\22\uffff\1\111\3\uffff";
    static final String DFA47_acceptS =
        "\1\uffff\15\2\1\uffff\13\2\2\uffff\4\2\2\uffff\1\2\1\uffff\11\2"+
        "\1\uffff\14\2\1\1\5\2\1\uffff\3\2";
    static final String DFA47_specialS =
        "\32\uffff\1\1\1\4\4\uffff\1\3\1\2\1\uffff\1\0\40\uffff}>";
    static final String[] DFA47_transitionS = {
            "\1\27\1\7\1\30\1\25\1\5\1\26\1\uffff\1\21\1\14\1\1\1\uffff"+
            "\1\17\1\20\1\12\3\14\1\10\1\22\1\14\1\16\1\uffff\1\23\1\24\2"+
            "\uffff\1\13\1\15\1\31\1\6\1\2\1\3\1\11",
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
            "\1\27\1\7\1\30\1\32\1\5\1\26\1\uffff\1\34\1\43\1\35\1\uffff"+
            "\1\17\1\20\1\40\3\43\1\33\1\41\1\43\1\31\1\uffff\1\23\1\24\2"+
            "\uffff\1\13\1\15\1\31\1\6\1\36\1\37\1\11",
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
            "\1\66\1\67\1\46\1\64\1\72\1\65\1\uffff\1\60\1\55\2\uffff\1"+
            "\57\1\45\1\51\3\71\1\44\1\61\1\71\1\53\1\uffff\1\62\1\63\2\uffff"+
            "\1\54\1\56\1\53\1\70\1\47\1\50\1\52",
            "\1\uffff",
            "",
            "",
            "",
            "",
            "\1\uffff",
            "\1\uffff",
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
            "\1\66\1\77\1\46\1\64\1\100\1\65\1\uffff\1\74\1\71\1\75\1\uffff"+
            "\1\57\1\45\1\51\3\71\1\44\1\61\1\71\1\103\1\73\1\62\1\63\2\uffff"+
            "\1\54\1\56\1\53\1\76\1\101\1\102\1\52",
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
            "",
            "",
            "",
            "\1\27\1\7\1\30\1\25\1\5\1\26\1\uffff\1\21\1\43\1\35\1\uffff"+
            "\1\17\1\20\1\40\3\43\1\33\1\41\1\43\1\31\1\uffff\1\23\1\24\2"+
            "\uffff\1\13\1\15\1\31\1\6\1\36\1\37\1\11",
            "",
            "",
            ""
    };

    static final short[] DFA47_eot = DFA.unpackEncodedString(DFA47_eotS);
    static final short[] DFA47_eof = DFA.unpackEncodedString(DFA47_eofS);
    static final char[] DFA47_min = DFA.unpackEncodedStringToUnsignedChars(DFA47_minS);
    static final char[] DFA47_max = DFA.unpackEncodedStringToUnsignedChars(DFA47_maxS);
    static final short[] DFA47_accept = DFA.unpackEncodedString(DFA47_acceptS);
    static final short[] DFA47_special = DFA.unpackEncodedString(DFA47_specialS);
    static final short[][] DFA47_transition;

    static {
        int numStates = DFA47_transitionS.length;
        DFA47_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA47_transition[i] = DFA.unpackEncodedString(DFA47_transitionS[i]);
        }
    }

    class DFA47 extends DFA {

        public DFA47(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 47;
            this.eot = DFA47_eot;
            this.eof = DFA47_eof;
            this.min = DFA47_min;
            this.max = DFA47_max;
            this.accept = DFA47_accept;
            this.special = DFA47_special;
            this.transition = DFA47_transition;
        }
        public String getDescription() {
            return "()+ loopback of 665:36: ( ( proximityGroup )=> proximityGroup ftsFieldGroupProximityTerm )+";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA47_35 = input.LA(1);

                         
                        int index47_35 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred13_FTS()) ) {s = 58;}

                        else if ( (true) ) {s = 57;}

                         
                        input.seek(index47_35);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA47_26 = input.LA(1);

                         
                        int index47_26 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA47_26==NOT) ) {s = 36;}

                        else if ( (LA47_26==FTSPHRASE) ) {s = 37;}

                        else if ( (LA47_26==MINUS) ) {s = 38;}

                        else if ( (LA47_26==AND) ) {s = 39;}

                        else if ( (LA47_26==AMP) ) {s = 40;}

                        else if ( (LA47_26==ID) ) {s = 41;}

                        else if ( (LA47_26==EXCLAMATION) ) {s = 42;}

                        else if ( (LA47_26==STAR||LA47_26==QUESTION_MARK) ) {s = 43;}

                        else if ( (LA47_26==AT) ) {s = 44;}

                        else if ( (LA47_26==DECIMAL_INTEGER_LITERAL) ) {s = 45;}

                        else if ( (LA47_26==URI) ) {s = 46;}

                        else if ( (LA47_26==EQUALS) ) {s = 47;}

                        else if ( (LA47_26==TILDA) ) {s = 48;}

                        else if ( (LA47_26==TO) ) {s = 49;}

                        else if ( (LA47_26==LSQUARE) ) {s = 50;}

                        else if ( (LA47_26==LT) ) {s = 51;}

                        else if ( (LA47_26==LPAREN) ) {s = 52;}

                        else if ( (LA47_26==PERCENT) ) {s = 53;}

                        else if ( (LA47_26==PLUS) ) {s = 54;}

                        else if ( (LA47_26==BAR) ) {s = 55;}

                        else if ( (LA47_26==OR) ) {s = 56;}

                        else if ( ((LA47_26>=FTSWORD && LA47_26<=FTSWILD)||LA47_26==FLOATING_POINT_LITERAL) ) {s = 57;}

                        else if ( (LA47_26==RPAREN) && (synpred13_FTS())) {s = 58;}

                         
                        input.seek(index47_26);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA47_33 = input.LA(1);

                         
                        int index47_33 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred13_FTS()) ) {s = 58;}

                        else if ( (true) ) {s = 57;}

                         
                        input.seek(index47_33);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA47_32 = input.LA(1);

                         
                        int index47_32 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred13_FTS()) ) {s = 58;}

                        else if ( (true) ) {s = 57;}

                         
                        input.seek(index47_32);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA47_27 = input.LA(1);

                         
                        int index47_27 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred13_FTS()) ) {s = 58;}

                        else if ( (true) ) {s = 57;}

                         
                        input.seek(index47_27);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 47, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA54_eotS =
        "\45\uffff";
    static final String DFA54_eofS =
        "\6\uffff\1\21\36\uffff";
    static final String DFA54_minS =
        "\1\66\1\63\1\uffff\1\54\2\uffff\1\51\36\uffff";
    static final String DFA54_maxS =
        "\1\104\1\63\1\uffff\1\105\2\uffff\1\111\36\uffff";
    static final String DFA54_acceptS =
        "\2\uffff\1\2\1\uffff\2\3\1\uffff\5\3\1\1\30\3";
    static final String DFA54_specialS =
        "\45\uffff}>";
    static final String[] DFA54_transitionS = {
            "\1\1\15\uffff\1\2",
            "\1\3",
            "",
            "\1\4\4\uffff\1\7\3\uffff\1\5\1\6\3\7\2\10\1\7\1\10\1\uffff"+
            "\1\11\1\12\4\uffff\1\10",
            "",
            "",
            "\1\43\1\24\1\44\1\41\1\22\1\42\1\uffff\1\15\1\31\1\16\1\14"+
            "\1\34\1\35\1\27\3\31\1\25\1\36\1\31\1\33\1\13\1\37\1\40\2\uffff"+
            "\1\30\1\32\1\33\1\23\1\17\1\20\1\26",
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

    static final short[] DFA54_eot = DFA.unpackEncodedString(DFA54_eotS);
    static final short[] DFA54_eof = DFA.unpackEncodedString(DFA54_eofS);
    static final char[] DFA54_min = DFA.unpackEncodedStringToUnsignedChars(DFA54_minS);
    static final char[] DFA54_max = DFA.unpackEncodedStringToUnsignedChars(DFA54_maxS);
    static final short[] DFA54_accept = DFA.unpackEncodedString(DFA54_acceptS);
    static final short[] DFA54_special = DFA.unpackEncodedString(DFA54_specialS);
    static final short[][] DFA54_transition;

    static {
        int numStates = DFA54_transitionS.length;
        DFA54_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA54_transition[i] = DFA.unpackEncodedString(DFA54_transitionS[i]);
        }
    }

    class DFA54 extends DFA {

        public DFA54(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 54;
            this.eot = DFA54_eot;
            this.eof = DFA54_eof;
            this.min = DFA54_min;
            this.max = DFA54_max;
            this.accept = DFA54_accept;
            this.special = DFA54_special;
            this.transition = DFA54_transition;
        }
        public String getDescription() {
            return "717:9: ( prefix | uri )?";
        }
    }
    static final String DFA56_eotS =
        "\35\uffff";
    static final String DFA56_eofS =
        "\1\uffff\1\7\33\uffff";
    static final String DFA56_minS =
        "\1\66\1\51\33\uffff";
    static final String DFA56_maxS =
        "\1\104\1\111\33\uffff";
    static final String DFA56_acceptS =
        "\2\uffff\1\2\1\1\31\3";
    static final String DFA56_specialS =
        "\35\uffff}>";
    static final String[] DFA56_transitionS = {
            "\1\1\15\uffff\1\2",
            "\1\32\1\12\1\33\1\30\1\10\1\31\1\34\1\24\1\17\1\4\1\3\1\22"+
            "\1\23\1\15\3\17\1\13\1\25\1\17\1\21\1\uffff\1\26\1\27\2\uffff"+
            "\1\16\1\20\1\21\1\11\1\5\1\6\1\14",
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

    static final short[] DFA56_eot = DFA.unpackEncodedString(DFA56_eotS);
    static final short[] DFA56_eof = DFA.unpackEncodedString(DFA56_eofS);
    static final char[] DFA56_min = DFA.unpackEncodedStringToUnsignedChars(DFA56_minS);
    static final char[] DFA56_max = DFA.unpackEncodedStringToUnsignedChars(DFA56_maxS);
    static final short[] DFA56_accept = DFA.unpackEncodedString(DFA56_acceptS);
    static final short[] DFA56_special = DFA.unpackEncodedString(DFA56_specialS);
    static final short[][] DFA56_transition;

    static {
        int numStates = DFA56_transitionS.length;
        DFA56_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA56_transition[i] = DFA.unpackEncodedString(DFA56_transitionS[i]);
        }
    }

    class DFA56 extends DFA {

        public DFA56(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 56;
            this.eot = DFA56_eot;
            this.eof = DFA56_eof;
            this.min = DFA56_min;
            this.max = DFA56_max;
            this.accept = DFA56_accept;
            this.special = DFA56_special;
            this.transition = DFA56_transition;
        }
        public String getDescription() {
            return "729:9: ( prefix | uri )?";
        }
    }
 

    public static final BitSet FOLLOW_ftsDisjunction_in_ftsQuery535 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_ftsQuery537 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cmisExplicitDisjunction_in_ftsDisjunction596 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ftsExplicitDisjunction_in_ftsDisjunction611 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ftsImplicitDisjunction_in_ftsDisjunction626 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ftsImplicitConjunction_in_ftsExplicitDisjunction659 = new BitSet(new long[]{0x0000040000000002L,0x0000000000000040L});
    public static final BitSet FOLLOW_or_in_ftsExplicitDisjunction662 = new BitSet(new long[]{0xBFF35E0000000000L,0x00000000000003B9L});
    public static final BitSet FOLLOW_ftsImplicitConjunction_in_ftsExplicitDisjunction664 = new BitSet(new long[]{0x0000040000000002L,0x0000000000000040L});
    public static final BitSet FOLLOW_cmisConjunction_in_cmisExplicitDisjunction748 = new BitSet(new long[]{0x0000040000000002L,0x0000000000000040L});
    public static final BitSet FOLLOW_or_in_cmisExplicitDisjunction751 = new BitSet(new long[]{0x3FE2080000000000L,0x0000000000000038L});
    public static final BitSet FOLLOW_cmisConjunction_in_cmisExplicitDisjunction753 = new BitSet(new long[]{0x0000040000000002L,0x0000000000000040L});
    public static final BitSet FOLLOW_or_in_ftsImplicitDisjunction838 = new BitSet(new long[]{0xBFF35E0000000000L,0x00000000000003B9L});
    public static final BitSet FOLLOW_ftsExplicitConjunction_in_ftsImplicitDisjunction841 = new BitSet(new long[]{0xBFF35E0000000002L,0x00000000000003F9L});
    public static final BitSet FOLLOW_ftsPrefixed_in_ftsExplicitConjunction928 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000180L});
    public static final BitSet FOLLOW_and_in_ftsExplicitConjunction931 = new BitSet(new long[]{0xBFF35E0000000000L,0x00000000000003B9L});
    public static final BitSet FOLLOW_ftsPrefixed_in_ftsExplicitConjunction933 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000180L});
    public static final BitSet FOLLOW_and_in_ftsImplicitConjunction1018 = new BitSet(new long[]{0xBFF35E0000000000L,0x00000000000003B9L});
    public static final BitSet FOLLOW_ftsPrefixed_in_ftsImplicitConjunction1021 = new BitSet(new long[]{0xBFF35E0000000002L,0x00000000000003B9L});
    public static final BitSet FOLLOW_cmisPrefixed_in_cmisConjunction1105 = new BitSet(new long[]{0x3FE2080000000002L,0x0000000000000038L});
    public static final BitSet FOLLOW_not_in_ftsPrefixed1197 = new BitSet(new long[]{0xBFF3500000000000L,0x0000000000000039L});
    public static final BitSet FOLLOW_ftsTest_in_ftsPrefixed1199 = new BitSet(new long[]{0x0004000000000002L});
    public static final BitSet FOLLOW_boost_in_ftsPrefixed1201 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ftsTest_in_ftsPrefixed1265 = new BitSet(new long[]{0x0004000000000002L});
    public static final BitSet FOLLOW_boost_in_ftsPrefixed1267 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_PLUS_in_ftsPrefixed1331 = new BitSet(new long[]{0xBFF3500000000000L,0x0000000000000039L});
    public static final BitSet FOLLOW_ftsTest_in_ftsPrefixed1333 = new BitSet(new long[]{0x0004000000000002L});
    public static final BitSet FOLLOW_boost_in_ftsPrefixed1335 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_BAR_in_ftsPrefixed1399 = new BitSet(new long[]{0xBFF3500000000000L,0x0000000000000039L});
    public static final BitSet FOLLOW_ftsTest_in_ftsPrefixed1401 = new BitSet(new long[]{0x0004000000000002L});
    public static final BitSet FOLLOW_boost_in_ftsPrefixed1403 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_MINUS_in_ftsPrefixed1467 = new BitSet(new long[]{0xBFF3500000000000L,0x0000000000000039L});
    public static final BitSet FOLLOW_ftsTest_in_ftsPrefixed1469 = new BitSet(new long[]{0x0004000000000002L});
    public static final BitSet FOLLOW_boost_in_ftsPrefixed1471 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cmisTest_in_cmisPrefixed1556 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_MINUS_in_cmisPrefixed1616 = new BitSet(new long[]{0x3FE2000000000000L,0x0000000000000038L});
    public static final BitSet FOLLOW_cmisTest_in_cmisPrefixed1618 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ftsFieldGroupProximity_in_ftsTest1708 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ftsTerm_in_ftsTest1768 = new BitSet(new long[]{0x0001000000000002L});
    public static final BitSet FOLLOW_fuzzy_in_ftsTest1778 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ftsExactTerm_in_ftsTest1843 = new BitSet(new long[]{0x0001000000000002L});
    public static final BitSet FOLLOW_fuzzy_in_ftsTest1853 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ftsPhrase_in_ftsTest1918 = new BitSet(new long[]{0x0001000000000002L});
    public static final BitSet FOLLOW_slop_in_ftsTest1928 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ftsSynonym_in_ftsTest1993 = new BitSet(new long[]{0x0001000000000002L});
    public static final BitSet FOLLOW_fuzzy_in_ftsTest2003 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ftsRange_in_ftsTest2068 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ftsFieldGroup_in_ftsTest2128 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_ftsTest2160 = new BitSet(new long[]{0xBFF35E0000000000L,0x00000000000003F9L});
    public static final BitSet FOLLOW_ftsDisjunction_in_ftsTest2162 = new BitSet(new long[]{0x0000200000000000L});
    public static final BitSet FOLLOW_RPAREN_in_ftsTest2164 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_template_in_ftsTest2196 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cmisTerm_in_cmisTest2249 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cmisPhrase_in_cmisTest2309 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_PERCENT_in_template2390 = new BitSet(new long[]{0x0040000000000000L,0x0000000000000018L});
    public static final BitSet FOLLOW_tempReference_in_template2392 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_PERCENT_in_template2452 = new BitSet(new long[]{0x0000100000000000L});
    public static final BitSet FOLLOW_LPAREN_in_template2454 = new BitSet(new long[]{0x0040000000000000L,0x0000000000000018L});
    public static final BitSet FOLLOW_tempReference_in_template2457 = new BitSet(new long[]{0x0040A00000000000L,0x0000000000000018L});
    public static final BitSet FOLLOW_COMMA_in_template2459 = new BitSet(new long[]{0x0040200000000000L,0x0000000000000018L});
    public static final BitSet FOLLOW_RPAREN_in_template2464 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TILDA_in_fuzzy2546 = new BitSet(new long[]{0x1002000000000000L});
    public static final BitSet FOLLOW_number_in_fuzzy2548 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TILDA_in_slop2629 = new BitSet(new long[]{0x0002000000000000L});
    public static final BitSet FOLLOW_DECIMAL_INTEGER_LITERAL_in_slop2631 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_CARAT_in_boost2712 = new BitSet(new long[]{0x1002000000000000L});
    public static final BitSet FOLLOW_number_in_boost2714 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_fieldReference_in_ftsTerm2796 = new BitSet(new long[]{0x0008000000000000L});
    public static final BitSet FOLLOW_COLON_in_ftsTerm2798 = new BitSet(new long[]{0x3FC2000000000000L,0x0000000000000038L});
    public static final BitSet FOLLOW_ftsWord_in_ftsTerm2802 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ftsWord_in_cmisTerm2858 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_EQUALS_in_ftsExactTerm2911 = new BitSet(new long[]{0x3FC2000000000000L,0x0000000000000038L});
    public static final BitSet FOLLOW_ftsTerm_in_ftsExactTerm2913 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_fieldReference_in_ftsPhrase2967 = new BitSet(new long[]{0x0008000000000000L});
    public static final BitSet FOLLOW_COLON_in_ftsPhrase2969 = new BitSet(new long[]{0x0020000000000000L});
    public static final BitSet FOLLOW_FTSPHRASE_in_ftsPhrase2973 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FTSPHRASE_in_cmisPhrase3029 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TILDA_in_ftsSynonym3082 = new BitSet(new long[]{0x3FC2000000000000L,0x0000000000000038L});
    public static final BitSet FOLLOW_ftsTerm_in_ftsSynonym3084 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_fieldReference_in_ftsRange3138 = new BitSet(new long[]{0x0008000000000000L});
    public static final BitSet FOLLOW_COLON_in_ftsRange3140 = new BitSet(new long[]{0x93E2000000000000L,0x0000000000000019L});
    public static final BitSet FOLLOW_ftsFieldGroupRange_in_ftsRange3144 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_fieldReference_in_ftsFieldGroup3200 = new BitSet(new long[]{0x0008000000000000L});
    public static final BitSet FOLLOW_COLON_in_ftsFieldGroup3202 = new BitSet(new long[]{0x0000100000000000L});
    public static final BitSet FOLLOW_LPAREN_in_ftsFieldGroup3204 = new BitSet(new long[]{0xBFF31E0000000000L,0x00000000000003F9L});
    public static final BitSet FOLLOW_ftsFieldGroupDisjunction_in_ftsFieldGroup3206 = new BitSet(new long[]{0x0000200000000000L});
    public static final BitSet FOLLOW_RPAREN_in_ftsFieldGroup3208 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ftsFieldGroupExplicitDisjunction_in_ftsFieldGroupDisjunction3294 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ftsFieldGroupImplicitDisjunction_in_ftsFieldGroupDisjunction3309 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ftsFieldGroupImplicitConjunction_in_ftsFieldGroupExplicitDisjunction3342 = new BitSet(new long[]{0x0000040000000002L,0x0000000000000040L});
    public static final BitSet FOLLOW_or_in_ftsFieldGroupExplicitDisjunction3345 = new BitSet(new long[]{0xBFF31E0000000000L,0x00000000000003B9L});
    public static final BitSet FOLLOW_ftsFieldGroupImplicitConjunction_in_ftsFieldGroupExplicitDisjunction3347 = new BitSet(new long[]{0x0000040000000002L,0x0000000000000040L});
    public static final BitSet FOLLOW_or_in_ftsFieldGroupImplicitDisjunction3432 = new BitSet(new long[]{0xBFF31E0000000000L,0x00000000000003F9L});
    public static final BitSet FOLLOW_ftsFieldGroupExplicitConjunction_in_ftsFieldGroupImplicitDisjunction3435 = new BitSet(new long[]{0xBFF31E0000000002L,0x00000000000003F9L});
    public static final BitSet FOLLOW_ftsFieldGroupPrefixed_in_ftsFieldGroupExplicitConjunction3522 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000180L});
    public static final BitSet FOLLOW_and_in_ftsFieldGroupExplicitConjunction3525 = new BitSet(new long[]{0xBFF31E0000000000L,0x00000000000003B9L});
    public static final BitSet FOLLOW_ftsFieldGroupPrefixed_in_ftsFieldGroupExplicitConjunction3527 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000180L});
    public static final BitSet FOLLOW_and_in_ftsFieldGroupImplicitConjunction3612 = new BitSet(new long[]{0xBFF31E0000000000L,0x00000000000003B9L});
    public static final BitSet FOLLOW_ftsFieldGroupPrefixed_in_ftsFieldGroupImplicitConjunction3615 = new BitSet(new long[]{0xBFF31E0000000002L,0x00000000000003B9L});
    public static final BitSet FOLLOW_not_in_ftsFieldGroupPrefixed3705 = new BitSet(new long[]{0xBFF3100000000000L,0x0000000000000039L});
    public static final BitSet FOLLOW_ftsFieldGroupTest_in_ftsFieldGroupPrefixed3707 = new BitSet(new long[]{0x0004000000000002L});
    public static final BitSet FOLLOW_boost_in_ftsFieldGroupPrefixed3709 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ftsFieldGroupTest_in_ftsFieldGroupPrefixed3773 = new BitSet(new long[]{0x0004000000000002L});
    public static final BitSet FOLLOW_boost_in_ftsFieldGroupPrefixed3775 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_PLUS_in_ftsFieldGroupPrefixed3839 = new BitSet(new long[]{0xBFF3100000000000L,0x0000000000000039L});
    public static final BitSet FOLLOW_ftsFieldGroupTest_in_ftsFieldGroupPrefixed3841 = new BitSet(new long[]{0x0004000000000002L});
    public static final BitSet FOLLOW_boost_in_ftsFieldGroupPrefixed3843 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_BAR_in_ftsFieldGroupPrefixed3907 = new BitSet(new long[]{0xBFF3100000000000L,0x0000000000000039L});
    public static final BitSet FOLLOW_ftsFieldGroupTest_in_ftsFieldGroupPrefixed3909 = new BitSet(new long[]{0x0004000000000002L});
    public static final BitSet FOLLOW_boost_in_ftsFieldGroupPrefixed3911 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_MINUS_in_ftsFieldGroupPrefixed3975 = new BitSet(new long[]{0xBFF3100000000000L,0x0000000000000039L});
    public static final BitSet FOLLOW_ftsFieldGroupTest_in_ftsFieldGroupPrefixed3977 = new BitSet(new long[]{0x0004000000000002L});
    public static final BitSet FOLLOW_boost_in_ftsFieldGroupPrefixed3979 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ftsFieldGroupProximity_in_ftsFieldGroupTest4070 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ftsFieldGroupTerm_in_ftsFieldGroupTest4130 = new BitSet(new long[]{0x0001000000000002L});
    public static final BitSet FOLLOW_fuzzy_in_ftsFieldGroupTest4140 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ftsFieldGroupExactTerm_in_ftsFieldGroupTest4205 = new BitSet(new long[]{0x0001000000000002L});
    public static final BitSet FOLLOW_fuzzy_in_ftsFieldGroupTest4215 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ftsFieldGroupPhrase_in_ftsFieldGroupTest4280 = new BitSet(new long[]{0x0001000000000002L});
    public static final BitSet FOLLOW_slop_in_ftsFieldGroupTest4290 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ftsFieldGroupSynonym_in_ftsFieldGroupTest4355 = new BitSet(new long[]{0x0001000000000002L});
    public static final BitSet FOLLOW_fuzzy_in_ftsFieldGroupTest4365 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ftsFieldGroupRange_in_ftsFieldGroupTest4430 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_ftsFieldGroupTest4490 = new BitSet(new long[]{0xBFF31E0000000000L,0x00000000000003F9L});
    public static final BitSet FOLLOW_ftsFieldGroupDisjunction_in_ftsFieldGroupTest4492 = new BitSet(new long[]{0x0000200000000000L});
    public static final BitSet FOLLOW_RPAREN_in_ftsFieldGroupTest4494 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ftsWord_in_ftsFieldGroupTerm4547 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_EQUALS_in_ftsFieldGroupExactTerm4580 = new BitSet(new long[]{0x3FC2000000000000L,0x0000000000000038L});
    public static final BitSet FOLLOW_ftsFieldGroupTerm_in_ftsFieldGroupExactTerm4582 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FTSPHRASE_in_ftsFieldGroupPhrase4635 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TILDA_in_ftsFieldGroupSynonym4668 = new BitSet(new long[]{0x3FC2000000000000L,0x0000000000000038L});
    public static final BitSet FOLLOW_ftsFieldGroupTerm_in_ftsFieldGroupSynonym4670 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ftsFieldGroupProximityTerm_in_ftsFieldGroupProximity4723 = new BitSet(new long[]{0x2000000000000000L});
    public static final BitSet FOLLOW_proximityGroup_in_ftsFieldGroupProximity4733 = new BitSet(new long[]{0x1FC2000000000000L});
    public static final BitSet FOLLOW_ftsFieldGroupProximityTerm_in_ftsFieldGroupProximity4735 = new BitSet(new long[]{0x2000000000000002L});
    public static final BitSet FOLLOW_set_in_ftsFieldGroupProximityTerm0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STAR_in_proximityGroup4914 = new BitSet(new long[]{0x0000100000000002L});
    public static final BitSet FOLLOW_LPAREN_in_proximityGroup4917 = new BitSet(new long[]{0x0002200000000000L});
    public static final BitSet FOLLOW_DECIMAL_INTEGER_LITERAL_in_proximityGroup4919 = new BitSet(new long[]{0x0000200000000000L});
    public static final BitSet FOLLOW_RPAREN_in_proximityGroup4922 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ftsRangeWord_in_ftsFieldGroupRange5006 = new BitSet(new long[]{0x4000000000000000L});
    public static final BitSet FOLLOW_DOTDOT_in_ftsFieldGroupRange5008 = new BitSet(new long[]{0x13E2000000000000L});
    public static final BitSet FOLLOW_ftsRangeWord_in_ftsFieldGroupRange5010 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_range_left_in_ftsFieldGroupRange5048 = new BitSet(new long[]{0x13E2000000000000L});
    public static final BitSet FOLLOW_ftsRangeWord_in_ftsFieldGroupRange5050 = new BitSet(new long[]{0x0800000000000000L});
    public static final BitSet FOLLOW_TO_in_ftsFieldGroupRange5052 = new BitSet(new long[]{0x13E2000000000000L});
    public static final BitSet FOLLOW_ftsRangeWord_in_ftsFieldGroupRange5054 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000006L});
    public static final BitSet FOLLOW_range_right_in_ftsFieldGroupRange5056 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LSQUARE_in_range_left5115 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LT_in_range_left5147 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RSQUARE_in_range_right5200 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_GT_in_range_right5232 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_AT_in_fieldReference5288 = new BitSet(new long[]{0x0040000000000000L,0x0000000000000010L});
    public static final BitSet FOLLOW_prefix_in_fieldReference5317 = new BitSet(new long[]{0x0040000000000000L});
    public static final BitSet FOLLOW_uri_in_fieldReference5337 = new BitSet(new long[]{0x0040000000000000L});
    public static final BitSet FOLLOW_identifier_in_fieldReference5358 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_AT_in_tempReference5445 = new BitSet(new long[]{0x0040000000000000L,0x0000000000000010L});
    public static final BitSet FOLLOW_prefix_in_tempReference5474 = new BitSet(new long[]{0x0040000000000000L});
    public static final BitSet FOLLOW_uri_in_tempReference5494 = new BitSet(new long[]{0x0040000000000000L});
    public static final BitSet FOLLOW_identifier_in_tempReference5515 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_identifier_in_prefix5602 = new BitSet(new long[]{0x0008000000000000L});
    public static final BitSet FOLLOW_COLON_in_prefix5604 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_URI_in_uri5685 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_identifier5766 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_ftsWord0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_number0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_ftsRangeWord0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_OR_in_or6092 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_BAR_in_or6104 = new BitSet(new long[]{0x0000040000000000L});
    public static final BitSet FOLLOW_BAR_in_or6106 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_AND_in_and6139 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_AMP_in_and6151 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000100L});
    public static final BitSet FOLLOW_AMP_in_and6153 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_not0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_not_in_synpred1_FTS1192 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ftsFieldGroupProximity_in_synpred2_FTS1703 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_fuzzy_in_synpred3_FTS1773 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_fuzzy_in_synpred4_FTS1848 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_slop_in_synpred5_FTS1923 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_fuzzy_in_synpred6_FTS1998 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_not_in_synpred7_FTS3700 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ftsFieldGroupProximity_in_synpred8_FTS4065 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_fuzzy_in_synpred9_FTS4135 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_fuzzy_in_synpred10_FTS4210 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_slop_in_synpred11_FTS4285 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_fuzzy_in_synpred12_FTS4360 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_proximityGroup_in_synpred13_FTS4728 = new BitSet(new long[]{0x0000000000000002L});

}