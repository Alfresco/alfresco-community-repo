// $ANTLR 3.2 Sep 23, 2009 12:02:23 W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS_FTS.g 2010-04-26 15:09:27

package org.alfresco.repo.search.impl.parsers;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;


import org.antlr.runtime.tree.*;

public class CMIS_FTSParser extends Parser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "FTS", "DISJUNCTION", "CONJUNCTION", "NEGATION", "TERM", "EXACT_TERM", "PHRASE", "SYNONYM", "RANGE", "PROXIMITY", "DEFAULT", "MANDATORY", "OPTIONAL", "EXCLUDE", "FIELD_DISJUNCTION", "FIELD_CONJUNCTION", "FIELD_NEGATION", "FIELD_GROUP", "FIELD_DEFAULT", "FIELD_MANDATORY", "FIELD_OPTIONAL", "FIELD_EXCLUDE", "FG_TERM", "FG_EXACT_TERM", "FG_PHRASE", "FG_SYNONYM", "FG_PROXIMITY", "FG_RANGE", "FIELD_REF", "INCLUSIVE", "EXCLUSIVE", "QUALIFIER", "PREFIX", "NAME_SPACE", "BOOST", "FUZZY", "TEMPLATE", "MINUS", "FTSWORD", "FTSPHRASE", "OR", "F_ESC", "WS", "START_WORD", "IN_WORD"
    };
    public static final int TERM=8;
    public static final int PREFIX=36;
    public static final int FG_PROXIMITY=30;
    public static final int FG_TERM=26;
    public static final int EXACT_TERM=9;
    public static final int FUZZY=39;
    public static final int FIELD_DISJUNCTION=18;
    public static final int MANDATORY=15;
    public static final int FG_EXACT_TERM=27;
    public static final int FIELD_EXCLUDE=25;
    public static final int EXCLUSIVE=34;
    public static final int EOF=-1;
    public static final int NAME_SPACE=37;
    public static final int BOOST=38;
    public static final int START_WORD=47;
    public static final int FIELD_DEFAULT=22;
    public static final int FIELD_OPTIONAL=24;
    public static final int F_ESC=45;
    public static final int SYNONYM=11;
    public static final int EXCLUDE=17;
    public static final int QUALIFIER=35;
    public static final int CONJUNCTION=6;
    public static final int FIELD_GROUP=21;
    public static final int DEFAULT=14;
    public static final int IN_WORD=48;
    public static final int RANGE=12;
    public static final int MINUS=41;
    public static final int PROXIMITY=13;
    public static final int FIELD_REF=32;
    public static final int PHRASE=10;
    public static final int FTSWORD=42;
    public static final int OPTIONAL=16;
    public static final int DISJUNCTION=5;
    public static final int FTS=4;
    public static final int WS=46;
    public static final int FG_SYNONYM=29;
    public static final int NEGATION=7;
    public static final int TEMPLATE=40;
    public static final int FTSPHRASE=43;
    public static final int FIELD_CONJUNCTION=19;
    public static final int INCLUSIVE=33;
    public static final int OR=44;
    public static final int FIELD_MANDATORY=23;
    public static final int FG_RANGE=31;
    public static final int FG_PHRASE=28;
    public static final int FIELD_NEGATION=20;

    // delegates
    // delegators


        public CMIS_FTSParser(TokenStream input) {
            this(input, new RecognizerSharedState());
        }
        public CMIS_FTSParser(TokenStream input, RecognizerSharedState state) {
            super(input, state);
             
        }
        
    protected TreeAdaptor adaptor = new CommonTreeAdaptor();

    public void setTreeAdaptor(TreeAdaptor adaptor) {
        this.adaptor = adaptor;
    }
    public TreeAdaptor getTreeAdaptor() {
        return adaptor;
    }

    public String[] getTokenNames() { return CMIS_FTSParser.tokenNames; }
    public String getGrammarFileName() { return "W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS_FTS.g"; }


        private Stack<String> paraphrases = new Stack<String>();
        
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


    public static class cmisFtsQuery_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "cmisFtsQuery"
    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS_FTS.g:309:1: cmisFtsQuery : ftsCmisDisjunction EOF -> ftsCmisDisjunction ;
    public final CMIS_FTSParser.cmisFtsQuery_return cmisFtsQuery() throws RecognitionException {
        CMIS_FTSParser.cmisFtsQuery_return retval = new CMIS_FTSParser.cmisFtsQuery_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token EOF2=null;
        CMIS_FTSParser.ftsCmisDisjunction_return ftsCmisDisjunction1 = null;


        Object EOF2_tree=null;
        RewriteRuleTokenStream stream_EOF=new RewriteRuleTokenStream(adaptor,"token EOF");
        RewriteRuleSubtreeStream stream_ftsCmisDisjunction=new RewriteRuleSubtreeStream(adaptor,"rule ftsCmisDisjunction");
        try {
            // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS_FTS.g:310:9: ( ftsCmisDisjunction EOF -> ftsCmisDisjunction )
            // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS_FTS.g:311:9: ftsCmisDisjunction EOF
            {
            pushFollow(FOLLOW_ftsCmisDisjunction_in_cmisFtsQuery535);
            ftsCmisDisjunction1=ftsCmisDisjunction();

            state._fsp--;

            stream_ftsCmisDisjunction.add(ftsCmisDisjunction1.getTree());
            EOF2=(Token)match(input,EOF,FOLLOW_EOF_in_cmisFtsQuery537);  
            stream_EOF.add(EOF2);



            // AST REWRITE
            // elements: ftsCmisDisjunction
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 312:17: -> ftsCmisDisjunction
            {
                adaptor.addChild(root_0, stream_ftsCmisDisjunction.nextTree());

            }

            retval.tree = root_0;
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
    // $ANTLR end "cmisFtsQuery"

    public static class ftsCmisDisjunction_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "ftsCmisDisjunction"
    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS_FTS.g:320:1: ftsCmisDisjunction : ftsCmisConjunction ( or ftsCmisConjunction )* -> ^( DISJUNCTION ( ftsCmisConjunction )+ ) ;
    public final CMIS_FTSParser.ftsCmisDisjunction_return ftsCmisDisjunction() throws RecognitionException {
        CMIS_FTSParser.ftsCmisDisjunction_return retval = new CMIS_FTSParser.ftsCmisDisjunction_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        CMIS_FTSParser.ftsCmisConjunction_return ftsCmisConjunction3 = null;

        CMIS_FTSParser.or_return or4 = null;

        CMIS_FTSParser.ftsCmisConjunction_return ftsCmisConjunction5 = null;


        RewriteRuleSubtreeStream stream_ftsCmisConjunction=new RewriteRuleSubtreeStream(adaptor,"rule ftsCmisConjunction");
        RewriteRuleSubtreeStream stream_or=new RewriteRuleSubtreeStream(adaptor,"rule or");
        try {
            // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS_FTS.g:321:9: ( ftsCmisConjunction ( or ftsCmisConjunction )* -> ^( DISJUNCTION ( ftsCmisConjunction )+ ) )
            // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS_FTS.g:322:9: ftsCmisConjunction ( or ftsCmisConjunction )*
            {
            pushFollow(FOLLOW_ftsCmisConjunction_in_ftsCmisDisjunction593);
            ftsCmisConjunction3=ftsCmisConjunction();

            state._fsp--;

            stream_ftsCmisConjunction.add(ftsCmisConjunction3.getTree());
            // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS_FTS.g:322:28: ( or ftsCmisConjunction )*
            loop1:
            do {
                int alt1=2;
                int LA1_0 = input.LA(1);

                if ( (LA1_0==OR) ) {
                    alt1=1;
                }


                switch (alt1) {
            	case 1 :
            	    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS_FTS.g:322:29: or ftsCmisConjunction
            	    {
            	    pushFollow(FOLLOW_or_in_ftsCmisDisjunction596);
            	    or4=or();

            	    state._fsp--;

            	    stream_or.add(or4.getTree());
            	    pushFollow(FOLLOW_ftsCmisConjunction_in_ftsCmisDisjunction598);
            	    ftsCmisConjunction5=ftsCmisConjunction();

            	    state._fsp--;

            	    stream_ftsCmisConjunction.add(ftsCmisConjunction5.getTree());

            	    }
            	    break;

            	default :
            	    break loop1;
                }
            } while (true);



            // AST REWRITE
            // elements: ftsCmisConjunction
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 323:17: -> ^( DISJUNCTION ( ftsCmisConjunction )+ )
            {
                // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS_FTS.g:324:25: ^( DISJUNCTION ( ftsCmisConjunction )+ )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(DISJUNCTION, "DISJUNCTION"), root_1);

                if ( !(stream_ftsCmisConjunction.hasNext()) ) {
                    throw new RewriteEarlyExitException();
                }
                while ( stream_ftsCmisConjunction.hasNext() ) {
                    adaptor.addChild(root_1, stream_ftsCmisConjunction.nextTree());

                }
                stream_ftsCmisConjunction.reset();

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;
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
    // $ANTLR end "ftsCmisDisjunction"

    public static class ftsCmisConjunction_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "ftsCmisConjunction"
    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS_FTS.g:327:1: ftsCmisConjunction : ( ftsCmisPrefixed )+ -> ^( CONJUNCTION ( ftsCmisPrefixed )+ ) ;
    public final CMIS_FTSParser.ftsCmisConjunction_return ftsCmisConjunction() throws RecognitionException {
        CMIS_FTSParser.ftsCmisConjunction_return retval = new CMIS_FTSParser.ftsCmisConjunction_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        CMIS_FTSParser.ftsCmisPrefixed_return ftsCmisPrefixed6 = null;


        RewriteRuleSubtreeStream stream_ftsCmisPrefixed=new RewriteRuleSubtreeStream(adaptor,"rule ftsCmisPrefixed");
        try {
            // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS_FTS.g:328:9: ( ( ftsCmisPrefixed )+ -> ^( CONJUNCTION ( ftsCmisPrefixed )+ ) )
            // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS_FTS.g:329:9: ( ftsCmisPrefixed )+
            {
            // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS_FTS.g:329:9: ( ftsCmisPrefixed )+
            int cnt2=0;
            loop2:
            do {
                int alt2=2;
                int LA2_0 = input.LA(1);

                if ( ((LA2_0>=MINUS && LA2_0<=FTSPHRASE)) ) {
                    alt2=1;
                }


                switch (alt2) {
            	case 1 :
            	    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS_FTS.g:329:9: ftsCmisPrefixed
            	    {
            	    pushFollow(FOLLOW_ftsCmisPrefixed_in_ftsCmisConjunction682);
            	    ftsCmisPrefixed6=ftsCmisPrefixed();

            	    state._fsp--;

            	    stream_ftsCmisPrefixed.add(ftsCmisPrefixed6.getTree());

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



            // AST REWRITE
            // elements: ftsCmisPrefixed
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 330:17: -> ^( CONJUNCTION ( ftsCmisPrefixed )+ )
            {
                // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS_FTS.g:331:25: ^( CONJUNCTION ( ftsCmisPrefixed )+ )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(CONJUNCTION, "CONJUNCTION"), root_1);

                if ( !(stream_ftsCmisPrefixed.hasNext()) ) {
                    throw new RewriteEarlyExitException();
                }
                while ( stream_ftsCmisPrefixed.hasNext() ) {
                    adaptor.addChild(root_1, stream_ftsCmisPrefixed.nextTree());

                }
                stream_ftsCmisPrefixed.reset();

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;
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
    // $ANTLR end "ftsCmisConjunction"

    public static class ftsCmisPrefixed_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "ftsCmisPrefixed"
    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS_FTS.g:334:1: ftsCmisPrefixed : ( cmisTest -> ^( DEFAULT cmisTest ) | MINUS cmisTest -> ^( EXCLUDE cmisTest ) );
    public final CMIS_FTSParser.ftsCmisPrefixed_return ftsCmisPrefixed() throws RecognitionException {
        CMIS_FTSParser.ftsCmisPrefixed_return retval = new CMIS_FTSParser.ftsCmisPrefixed_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token MINUS8=null;
        CMIS_FTSParser.cmisTest_return cmisTest7 = null;

        CMIS_FTSParser.cmisTest_return cmisTest9 = null;


        Object MINUS8_tree=null;
        RewriteRuleTokenStream stream_MINUS=new RewriteRuleTokenStream(adaptor,"token MINUS");
        RewriteRuleSubtreeStream stream_cmisTest=new RewriteRuleSubtreeStream(adaptor,"rule cmisTest");
        try {
            // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS_FTS.g:335:9: ( cmisTest -> ^( DEFAULT cmisTest ) | MINUS cmisTest -> ^( EXCLUDE cmisTest ) )
            int alt3=2;
            int LA3_0 = input.LA(1);

            if ( ((LA3_0>=FTSWORD && LA3_0<=FTSPHRASE)) ) {
                alt3=1;
            }
            else if ( (LA3_0==MINUS) ) {
                alt3=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 3, 0, input);

                throw nvae;
            }
            switch (alt3) {
                case 1 :
                    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS_FTS.g:336:9: cmisTest
                    {
                    pushFollow(FOLLOW_cmisTest_in_ftsCmisPrefixed765);
                    cmisTest7=cmisTest();

                    state._fsp--;

                    stream_cmisTest.add(cmisTest7.getTree());


                    // AST REWRITE
                    // elements: cmisTest
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 337:17: -> ^( DEFAULT cmisTest )
                    {
                        // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS_FTS.g:338:25: ^( DEFAULT cmisTest )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(DEFAULT, "DEFAULT"), root_1);

                        adaptor.addChild(root_1, stream_cmisTest.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;
                    }
                    break;
                case 2 :
                    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS_FTS.g:339:11: MINUS cmisTest
                    {
                    MINUS8=(Token)match(input,MINUS,FOLLOW_MINUS_in_ftsCmisPrefixed825);  
                    stream_MINUS.add(MINUS8);

                    pushFollow(FOLLOW_cmisTest_in_ftsCmisPrefixed827);
                    cmisTest9=cmisTest();

                    state._fsp--;

                    stream_cmisTest.add(cmisTest9.getTree());


                    // AST REWRITE
                    // elements: cmisTest
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 340:17: -> ^( EXCLUDE cmisTest )
                    {
                        // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS_FTS.g:341:25: ^( EXCLUDE cmisTest )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(EXCLUDE, "EXCLUDE"), root_1);

                        adaptor.addChild(root_1, stream_cmisTest.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;
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
    // $ANTLR end "ftsCmisPrefixed"

    public static class cmisTest_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "cmisTest"
    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS_FTS.g:344:1: cmisTest : ( cmisTerm -> ^( TERM cmisTerm ) | cmisPhrase -> ^( PHRASE cmisPhrase ) );
    public final CMIS_FTSParser.cmisTest_return cmisTest() throws RecognitionException {
        CMIS_FTSParser.cmisTest_return retval = new CMIS_FTSParser.cmisTest_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        CMIS_FTSParser.cmisTerm_return cmisTerm10 = null;

        CMIS_FTSParser.cmisPhrase_return cmisPhrase11 = null;


        RewriteRuleSubtreeStream stream_cmisPhrase=new RewriteRuleSubtreeStream(adaptor,"rule cmisPhrase");
        RewriteRuleSubtreeStream stream_cmisTerm=new RewriteRuleSubtreeStream(adaptor,"rule cmisTerm");
        try {
            // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS_FTS.g:345:9: ( cmisTerm -> ^( TERM cmisTerm ) | cmisPhrase -> ^( PHRASE cmisPhrase ) )
            int alt4=2;
            int LA4_0 = input.LA(1);

            if ( (LA4_0==FTSWORD) ) {
                alt4=1;
            }
            else if ( (LA4_0==FTSPHRASE) ) {
                alt4=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 4, 0, input);

                throw nvae;
            }
            switch (alt4) {
                case 1 :
                    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS_FTS.g:346:9: cmisTerm
                    {
                    pushFollow(FOLLOW_cmisTerm_in_cmisTest908);
                    cmisTerm10=cmisTerm();

                    state._fsp--;

                    stream_cmisTerm.add(cmisTerm10.getTree());


                    // AST REWRITE
                    // elements: cmisTerm
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 347:17: -> ^( TERM cmisTerm )
                    {
                        // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS_FTS.g:348:25: ^( TERM cmisTerm )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(TERM, "TERM"), root_1);

                        adaptor.addChild(root_1, stream_cmisTerm.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;
                    }
                    break;
                case 2 :
                    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS_FTS.g:349:11: cmisPhrase
                    {
                    pushFollow(FOLLOW_cmisPhrase_in_cmisTest968);
                    cmisPhrase11=cmisPhrase();

                    state._fsp--;

                    stream_cmisPhrase.add(cmisPhrase11.getTree());


                    // AST REWRITE
                    // elements: cmisPhrase
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 350:17: -> ^( PHRASE cmisPhrase )
                    {
                        // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS_FTS.g:351:25: ^( PHRASE cmisPhrase )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(PHRASE, "PHRASE"), root_1);

                        adaptor.addChild(root_1, stream_cmisPhrase.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;
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
    // $ANTLR end "cmisTest"

    public static class cmisTerm_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "cmisTerm"
    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS_FTS.g:355:1: cmisTerm : FTSWORD -> FTSWORD ;
    public final CMIS_FTSParser.cmisTerm_return cmisTerm() throws RecognitionException {
        CMIS_FTSParser.cmisTerm_return retval = new CMIS_FTSParser.cmisTerm_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token FTSWORD12=null;

        Object FTSWORD12_tree=null;
        RewriteRuleTokenStream stream_FTSWORD=new RewriteRuleTokenStream(adaptor,"token FTSWORD");

        try {
            // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS_FTS.g:356:9: ( FTSWORD -> FTSWORD )
            // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS_FTS.g:357:9: FTSWORD
            {
            FTSWORD12=(Token)match(input,FTSWORD,FOLLOW_FTSWORD_in_cmisTerm1058);  
            stream_FTSWORD.add(FTSWORD12);



            // AST REWRITE
            // elements: FTSWORD
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 358:17: -> FTSWORD
            {
                adaptor.addChild(root_0, stream_FTSWORD.nextNode());

            }

            retval.tree = root_0;
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
    // $ANTLR end "cmisTerm"

    public static class cmisPhrase_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "cmisPhrase"
    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS_FTS.g:361:1: cmisPhrase : FTSPHRASE -> FTSPHRASE ;
    public final CMIS_FTSParser.cmisPhrase_return cmisPhrase() throws RecognitionException {
        CMIS_FTSParser.cmisPhrase_return retval = new CMIS_FTSParser.cmisPhrase_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token FTSPHRASE13=null;

        Object FTSPHRASE13_tree=null;
        RewriteRuleTokenStream stream_FTSPHRASE=new RewriteRuleTokenStream(adaptor,"token FTSPHRASE");

        try {
            // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS_FTS.g:362:9: ( FTSPHRASE -> FTSPHRASE )
            // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS_FTS.g:363:9: FTSPHRASE
            {
            FTSPHRASE13=(Token)match(input,FTSPHRASE,FOLLOW_FTSPHRASE_in_cmisPhrase1111);  
            stream_FTSPHRASE.add(FTSPHRASE13);



            // AST REWRITE
            // elements: FTSPHRASE
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 364:17: -> FTSPHRASE
            {
                adaptor.addChild(root_0, stream_FTSPHRASE.nextNode());

            }

            retval.tree = root_0;
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
    // $ANTLR end "cmisPhrase"

    public static class or_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "or"
    // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS_FTS.g:367:1: or : OR ;
    public final CMIS_FTSParser.or_return or() throws RecognitionException {
        CMIS_FTSParser.or_return retval = new CMIS_FTSParser.or_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token OR14=null;

        Object OR14_tree=null;

        try {
            // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS_FTS.g:368:9: ( OR )
            // W:\\alfresco\\HEAD\\root\\projects\\repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS_FTS.g:369:9: OR
            {
            root_0 = (Object)adaptor.nil();

            OR14=(Token)match(input,OR,FOLLOW_OR_in_or1164); 
            OR14_tree = (Object)adaptor.create(OR14);
            adaptor.addChild(root_0, OR14_tree);


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
    // $ANTLR end "or"

    // Delegated rules


 

    public static final BitSet FOLLOW_ftsCmisDisjunction_in_cmisFtsQuery535 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_cmisFtsQuery537 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ftsCmisConjunction_in_ftsCmisDisjunction593 = new BitSet(new long[]{0x0000100000000002L});
    public static final BitSet FOLLOW_or_in_ftsCmisDisjunction596 = new BitSet(new long[]{0x00000E0000000000L});
    public static final BitSet FOLLOW_ftsCmisConjunction_in_ftsCmisDisjunction598 = new BitSet(new long[]{0x0000100000000002L});
    public static final BitSet FOLLOW_ftsCmisPrefixed_in_ftsCmisConjunction682 = new BitSet(new long[]{0x00000E0000000002L});
    public static final BitSet FOLLOW_cmisTest_in_ftsCmisPrefixed765 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_MINUS_in_ftsCmisPrefixed825 = new BitSet(new long[]{0x00000C0000000000L});
    public static final BitSet FOLLOW_cmisTest_in_ftsCmisPrefixed827 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cmisTerm_in_cmisTest908 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cmisPhrase_in_cmisTest968 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FTSWORD_in_cmisTerm1058 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FTSPHRASE_in_cmisPhrase1111 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_OR_in_or1164 = new BitSet(new long[]{0x0000000000000002L});

}