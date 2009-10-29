// $ANTLR !Unknown version! W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g 2009-10-15 15:44:00

    package org.alfresco.repo.search.impl.parsers;
    import org.alfresco.cmis.CMISQueryException;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import org.antlr.runtime.tree.*;

public class CMISParser extends Parser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "QUERY", "ALL_COLUMNS", "COLUMN", "COLUMNS", "COLUMN_REF", "QUALIFIER", "FUNCTION", "SOURCE", "TABLE", "TABLE_REF", "PARAMETER", "CONJUNCTION", "DISJUNCTION", "NEGATION", "PRED_COMPARISON", "PRED_IN", "PRED_EXISTS", "PRED_LIKE", "PRED_FTS", "LIST", "PRED_CHILD", "PRED_DESCENDANT", "SORT_SPECIFICATION", "NUMERIC_LITERAL", "STRING_LITERAL", "DATETIME_LITERAL", "BOOLEAN_LITERAL", "SELECT", "STAR", "COMMA", "AS", "DOTSTAR", "DOT", "LPAREN", "RPAREN", "FROM", "JOIN", "INNER", "LEFT", "OUTER", "ON", "EQUALS", "WHERE", "OR", "AND", "NOT", "NOTEQUALS", "LESSTHAN", "GREATERTHAN", "LESSTHANOREQUALS", "GREATERTHANOREQUALS", "IN", "LIKE", "IS", "NULL", "ANY", "CONTAINS", "IN_FOLDER", "IN_TREE", "ORDER", "BY", "ASC", "DESC", "COLON", "QUOTED_STRING", "ID", "DOUBLE_QUOTE", "FLOATING_POINT_LITERAL", "DECIMAL_INTEGER_LITERAL", "TRUE", "FALSE", "TIMESTAMP", "SCORE", "DOTDOT", "TILDA", "PLUS", "MINUS", "DECIMAL_NUMERAL", "DIGIT", "EXPONENT", "WS", "ZERO_DIGIT", "NON_ZERO_DIGIT", "E", "SIGNED_INTEGER"
    };
    public static final int FUNCTION=10;
    public static final int WHERE=46;
    public static final int EXPONENT=83;
    public static final int PRED_FTS=22;
    public static final int STAR=32;
    public static final int INNER=41;
    public static final int ORDER=63;
    public static final int DOUBLE_QUOTE=70;
    public static final int NUMERIC_LITERAL=27;
    public static final int PRED_COMPARISON=18;
    public static final int CONTAINS=60;
    public static final int TABLE=12;
    public static final int SOURCE=11;
    public static final int DOTDOT=77;
    public static final int EQUALS=45;
    public static final int NOT=49;
    public static final int ID=69;
    public static final int AND=48;
    public static final int EOF=-1;
    public static final int LPAREN=37;
    public static final int LESSTHANOREQUALS=53;
    public static final int AS=34;
    public static final int RPAREN=38;
    public static final int TILDA=78;
    public static final int PRED_LIKE=21;
    public static final int STRING_LITERAL=28;
    public static final int IN=55;
    public static final int DECIMAL_NUMERAL=81;
    public static final int FLOATING_POINT_LITERAL=71;
    public static final int COMMA=33;
    public static final int IS=57;
    public static final int LEFT=42;
    public static final int SIGNED_INTEGER=88;
    public static final int PARAMETER=14;
    public static final int COLUMN=6;
    public static final int PLUS=79;
    public static final int QUOTED_STRING=68;
    public static final int ZERO_DIGIT=85;
    public static final int DIGIT=82;
    public static final int DOT=36;
    public static final int COLUMN_REF=8;
    public static final int SELECT=31;
    public static final int LIKE=56;
    public static final int GREATERTHAN=52;
    public static final int DOTSTAR=35;
    public static final int E=87;
    public static final int OUTER=43;
    public static final int BY=64;
    public static final int LESSTHAN=51;
    public static final int NON_ZERO_DIGIT=86;
    public static final int ASC=65;
    public static final int QUALIFIER=9;
    public static final int CONJUNCTION=15;
    public static final int NULL=58;
    public static final int ON=44;
    public static final int NOTEQUALS=50;
    public static final int DATETIME_LITERAL=29;
    public static final int MINUS=80;
    public static final int LIST=23;
    public static final int TRUE=73;
    public static final int PRED_DESCENDANT=25;
    public static final int JOIN=40;
    public static final int IN_FOLDER=61;
    public static final int COLON=67;
    public static final int GREATERTHANOREQUALS=54;
    public static final int BOOLEAN_LITERAL=30;
    public static final int DISJUNCTION=16;
    public static final int COLUMNS=7;
    public static final int WS=84;
    public static final int ANY=59;
    public static final int SCORE=76;
    public static final int NEGATION=17;
    public static final int TABLE_REF=13;
    public static final int SORT_SPECIFICATION=26;
    public static final int IN_TREE=62;
    public static final int OR=47;
    public static final int PRED_CHILD=24;
    public static final int PRED_EXISTS=20;
    public static final int QUERY=4;
    public static final int DECIMAL_INTEGER_LITERAL=72;
    public static final int DESC=66;
    public static final int ALL_COLUMNS=5;
    public static final int FROM=39;
    public static final int FALSE=74;
    public static final int TIMESTAMP=75;
    public static final int PRED_IN=19;

    // delegates
    // delegators


        public CMISParser(TokenStream input) {
            this(input, new RecognizerSharedState());
        }
        public CMISParser(TokenStream input, RecognizerSharedState state) {
            super(input, state);
             
        }
        
    protected TreeAdaptor adaptor = new CommonTreeAdaptor();

    public void setTreeAdaptor(TreeAdaptor adaptor) {
        this.adaptor = adaptor;
    }
    public TreeAdaptor getTreeAdaptor() {
        return adaptor;
    }

    public String[] getTokenNames() { return CMISParser.tokenNames; }
    public String getGrammarFileName() { return "W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g"; }


        private Stack<String> paraphrases = new Stack<String>();
        
        private boolean strict = false;

        /**
         * CMIS strict
         */
        public boolean strict()
        {
            return strict;
        }
    	
        public void setStrict(boolean strict)
        {
            this.strict = strict;
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


    public static class query_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "query"
    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:289:1: query : SELECT selectList fromClause ( whereClause )? ( orderByClause )? EOF -> ^( QUERY selectList fromClause ( whereClause )? ( orderByClause )? ) ;
    public final CMISParser.query_return query() throws RecognitionException {
        CMISParser.query_return retval = new CMISParser.query_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token SELECT1=null;
        Token EOF6=null;
        CMISParser.selectList_return selectList2 = null;

        CMISParser.fromClause_return fromClause3 = null;

        CMISParser.whereClause_return whereClause4 = null;

        CMISParser.orderByClause_return orderByClause5 = null;


        Object SELECT1_tree=null;
        Object EOF6_tree=null;
        RewriteRuleTokenStream stream_EOF=new RewriteRuleTokenStream(adaptor,"token EOF");
        RewriteRuleTokenStream stream_SELECT=new RewriteRuleTokenStream(adaptor,"token SELECT");
        RewriteRuleSubtreeStream stream_whereClause=new RewriteRuleSubtreeStream(adaptor,"rule whereClause");
        RewriteRuleSubtreeStream stream_orderByClause=new RewriteRuleSubtreeStream(adaptor,"rule orderByClause");
        RewriteRuleSubtreeStream stream_selectList=new RewriteRuleSubtreeStream(adaptor,"rule selectList");
        RewriteRuleSubtreeStream stream_fromClause=new RewriteRuleSubtreeStream(adaptor,"rule fromClause");
            paraphrases.push("in query"); 
        try {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:298:2: ( SELECT selectList fromClause ( whereClause )? ( orderByClause )? EOF -> ^( QUERY selectList fromClause ( whereClause )? ( orderByClause )? ) )
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:298:4: SELECT selectList fromClause ( whereClause )? ( orderByClause )? EOF
            {
            SELECT1=(Token)match(input,SELECT,FOLLOW_SELECT_in_query217); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_SELECT.add(SELECT1);

            pushFollow(FOLLOW_selectList_in_query219);
            selectList2=selectList();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_selectList.add(selectList2.getTree());
            pushFollow(FOLLOW_fromClause_in_query221);
            fromClause3=fromClause();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_fromClause.add(fromClause3.getTree());
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:298:33: ( whereClause )?
            int alt1=2;
            int LA1_0 = input.LA(1);

            if ( (LA1_0==WHERE) ) {
                alt1=1;
            }
            switch (alt1) {
                case 1 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:298:33: whereClause
                    {
                    pushFollow(FOLLOW_whereClause_in_query223);
                    whereClause4=whereClause();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_whereClause.add(whereClause4.getTree());

                    }
                    break;

            }

            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:298:46: ( orderByClause )?
            int alt2=2;
            int LA2_0 = input.LA(1);

            if ( (LA2_0==ORDER) ) {
                alt2=1;
            }
            switch (alt2) {
                case 1 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:298:46: orderByClause
                    {
                    pushFollow(FOLLOW_orderByClause_in_query226);
                    orderByClause5=orderByClause();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_orderByClause.add(orderByClause5.getTree());

                    }
                    break;

            }

            EOF6=(Token)match(input,EOF,FOLLOW_EOF_in_query229); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_EOF.add(EOF6);



            // AST REWRITE
            // elements: selectList, orderByClause, fromClause, whereClause
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 299:3: -> ^( QUERY selectList fromClause ( whereClause )? ( orderByClause )? )
            {
                // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:299:6: ^( QUERY selectList fromClause ( whereClause )? ( orderByClause )? )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(QUERY, "QUERY"), root_1);

                adaptor.addChild(root_1, stream_selectList.nextTree());
                adaptor.addChild(root_1, stream_fromClause.nextTree());
                // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:299:36: ( whereClause )?
                if ( stream_whereClause.hasNext() ) {
                    adaptor.addChild(root_1, stream_whereClause.nextTree());

                }
                stream_whereClause.reset();
                // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:299:49: ( orderByClause )?
                if ( stream_orderByClause.hasNext() ) {
                    adaptor.addChild(root_1, stream_orderByClause.nextTree());

                }
                stream_orderByClause.reset();

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
            if ( state.backtracking==0 ) {
                  paraphrases.pop(); 
            }
        }

            catch(RecognitionException e)
            {
                throw new CMISQueryException(getErrorString(e), e);
            }
        finally {
        }
        return retval;
    }
    // $ANTLR end "query"

    public static class selectList_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "selectList"
    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:303:1: selectList : ( STAR -> ^( ALL_COLUMNS ) | selectSubList ( COMMA selectSubList )* -> ^( COLUMNS ( selectSubList )+ ) );
    public final CMISParser.selectList_return selectList() throws RecognitionException {
        CMISParser.selectList_return retval = new CMISParser.selectList_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token STAR7=null;
        Token COMMA9=null;
        CMISParser.selectSubList_return selectSubList8 = null;

        CMISParser.selectSubList_return selectSubList10 = null;


        Object STAR7_tree=null;
        Object COMMA9_tree=null;
        RewriteRuleTokenStream stream_STAR=new RewriteRuleTokenStream(adaptor,"token STAR");
        RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
        RewriteRuleSubtreeStream stream_selectSubList=new RewriteRuleSubtreeStream(adaptor,"rule selectSubList");
            paraphrases.push("in select list"); 
        try {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:306:2: ( STAR -> ^( ALL_COLUMNS ) | selectSubList ( COMMA selectSubList )* -> ^( COLUMNS ( selectSubList )+ ) )
            int alt4=2;
            int LA4_0 = input.LA(1);

            if ( (LA4_0==STAR) ) {
                alt4=1;
            }
            else if ( (LA4_0==ID) ) {
                alt4=2;
            }
            else if ( (LA4_0==DOUBLE_QUOTE) && ((strict == false))) {
                alt4=2;
            }
            else if ( (LA4_0==SCORE) ) {
                alt4=2;
            }
            else if ( (LA4_0==SELECT||LA4_0==AS||(LA4_0>=FROM && LA4_0<=ON)||(LA4_0>=WHERE && LA4_0<=NOT)||(LA4_0>=IN && LA4_0<=DESC)||(LA4_0>=TRUE && LA4_0<=TIMESTAMP)) && ((strict == false))) {
                alt4=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 4, 0, input);

                throw nvae;
            }
            switch (alt4) {
                case 1 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:306:4: STAR
                    {
                    STAR7=(Token)match(input,STAR,FOLLOW_STAR_in_selectList286); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_STAR.add(STAR7);



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
                    // 307:3: -> ^( ALL_COLUMNS )
                    {
                        // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:307:6: ^( ALL_COLUMNS )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(ALL_COLUMNS, "ALL_COLUMNS"), root_1);

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:308:5: selectSubList ( COMMA selectSubList )*
                    {
                    pushFollow(FOLLOW_selectSubList_in_selectList302);
                    selectSubList8=selectSubList();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_selectSubList.add(selectSubList8.getTree());
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:308:19: ( COMMA selectSubList )*
                    loop3:
                    do {
                        int alt3=2;
                        int LA3_0 = input.LA(1);

                        if ( (LA3_0==COMMA) ) {
                            alt3=1;
                        }


                        switch (alt3) {
                    	case 1 :
                    	    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:308:21: COMMA selectSubList
                    	    {
                    	    COMMA9=(Token)match(input,COMMA,FOLLOW_COMMA_in_selectList306); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_COMMA.add(COMMA9);

                    	    pushFollow(FOLLOW_selectSubList_in_selectList308);
                    	    selectSubList10=selectSubList();

                    	    state._fsp--;
                    	    if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) stream_selectSubList.add(selectSubList10.getTree());

                    	    }
                    	    break;

                    	default :
                    	    break loop3;
                        }
                    } while (true);



                    // AST REWRITE
                    // elements: selectSubList
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 309:3: -> ^( COLUMNS ( selectSubList )+ )
                    {
                        // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:309:6: ^( COLUMNS ( selectSubList )+ )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(COLUMNS, "COLUMNS"), root_1);

                        if ( !(stream_selectSubList.hasNext()) ) {
                            throw new RewriteEarlyExitException();
                        }
                        while ( stream_selectSubList.hasNext() ) {
                            adaptor.addChild(root_1, stream_selectSubList.nextTree());

                        }
                        stream_selectSubList.reset();

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
            if ( state.backtracking==0 ) {
                  paraphrases.pop(); 
            }
        }

            catch(RecognitionException e)
            {
                throw new CMISQueryException(getErrorString(e), e);
            }
        finally {
        }
        return retval;
    }
    // $ANTLR end "selectList"

    public static class selectSubList_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "selectSubList"
    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:313:1: selectSubList : ( ( valueExpression )=> valueExpression ( ( AS )? columnName )? -> ^( COLUMN valueExpression ( columnName )? ) | qualifier DOTSTAR -> ^( ALL_COLUMNS qualifier ) | multiValuedColumnReference ->);
    public final CMISParser.selectSubList_return selectSubList() throws RecognitionException {
        CMISParser.selectSubList_return retval = new CMISParser.selectSubList_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token AS12=null;
        Token DOTSTAR15=null;
        CMISParser.valueExpression_return valueExpression11 = null;

        CMISParser.columnName_return columnName13 = null;

        CMISParser.qualifier_return qualifier14 = null;

        CMISParser.multiValuedColumnReference_return multiValuedColumnReference16 = null;


        Object AS12_tree=null;
        Object DOTSTAR15_tree=null;
        RewriteRuleTokenStream stream_AS=new RewriteRuleTokenStream(adaptor,"token AS");
        RewriteRuleTokenStream stream_DOTSTAR=new RewriteRuleTokenStream(adaptor,"token DOTSTAR");
        RewriteRuleSubtreeStream stream_valueExpression=new RewriteRuleSubtreeStream(adaptor,"rule valueExpression");
        RewriteRuleSubtreeStream stream_multiValuedColumnReference=new RewriteRuleSubtreeStream(adaptor,"rule multiValuedColumnReference");
        RewriteRuleSubtreeStream stream_columnName=new RewriteRuleSubtreeStream(adaptor,"rule columnName");
        RewriteRuleSubtreeStream stream_qualifier=new RewriteRuleSubtreeStream(adaptor,"rule qualifier");
        try {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:314:2: ( ( valueExpression )=> valueExpression ( ( AS )? columnName )? -> ^( COLUMN valueExpression ( columnName )? ) | qualifier DOTSTAR -> ^( ALL_COLUMNS qualifier ) | multiValuedColumnReference ->)
            int alt7=3;
            alt7 = dfa7.predict(input);
            switch (alt7) {
                case 1 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:314:4: ( valueExpression )=> valueExpression ( ( AS )? columnName )?
                    {
                    pushFollow(FOLLOW_valueExpression_in_selectSubList344);
                    valueExpression11=valueExpression();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_valueExpression.add(valueExpression11.getTree());
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:314:40: ( ( AS )? columnName )?
                    int alt6=2;
                    int LA6_0 = input.LA(1);

                    if ( (LA6_0==AS||LA6_0==ID) ) {
                        alt6=1;
                    }
                    else if ( (LA6_0==DOUBLE_QUOTE) && ((strict == false))) {
                        alt6=1;
                    }
                    switch (alt6) {
                        case 1 :
                            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:314:42: ( AS )? columnName
                            {
                            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:314:42: ( AS )?
                            int alt5=2;
                            int LA5_0 = input.LA(1);

                            if ( (LA5_0==AS) ) {
                                alt5=1;
                            }
                            switch (alt5) {
                                case 1 :
                                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:314:42: AS
                                    {
                                    AS12=(Token)match(input,AS,FOLLOW_AS_in_selectSubList348); if (state.failed) return retval; 
                                    if ( state.backtracking==0 ) stream_AS.add(AS12);


                                    }
                                    break;

                            }

                            pushFollow(FOLLOW_columnName_in_selectSubList351);
                            columnName13=columnName();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_columnName.add(columnName13.getTree());

                            }
                            break;

                    }



                    // AST REWRITE
                    // elements: columnName, valueExpression
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 315:3: -> ^( COLUMN valueExpression ( columnName )? )
                    {
                        // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:315:6: ^( COLUMN valueExpression ( columnName )? )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(COLUMN, "COLUMN"), root_1);

                        adaptor.addChild(root_1, stream_valueExpression.nextTree());
                        // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:315:31: ( columnName )?
                        if ( stream_columnName.hasNext() ) {
                            adaptor.addChild(root_1, stream_columnName.nextTree());

                        }
                        stream_columnName.reset();

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:316:4: qualifier DOTSTAR
                    {
                    pushFollow(FOLLOW_qualifier_in_selectSubList372);
                    qualifier14=qualifier();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_qualifier.add(qualifier14.getTree());
                    DOTSTAR15=(Token)match(input,DOTSTAR,FOLLOW_DOTSTAR_in_selectSubList374); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_DOTSTAR.add(DOTSTAR15);



                    // AST REWRITE
                    // elements: qualifier
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 317:3: -> ^( ALL_COLUMNS qualifier )
                    {
                        // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:317:6: ^( ALL_COLUMNS qualifier )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(ALL_COLUMNS, "ALL_COLUMNS"), root_1);

                        adaptor.addChild(root_1, stream_qualifier.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 3 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:318:4: multiValuedColumnReference
                    {
                    pushFollow(FOLLOW_multiValuedColumnReference_in_selectSubList390);
                    multiValuedColumnReference16=multiValuedColumnReference();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_multiValuedColumnReference.add(multiValuedColumnReference16.getTree());


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
                    // 319:3: ->
                    {
                        root_0 = null;
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
                throw new CMISQueryException(getErrorString(e), e);
            }
        finally {
        }
        return retval;
    }
    // $ANTLR end "selectSubList"

    public static class valueExpression_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "valueExpression"
    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:322:1: valueExpression : ( columnReference -> columnReference | valueFunction -> valueFunction );
    public final CMISParser.valueExpression_return valueExpression() throws RecognitionException {
        CMISParser.valueExpression_return retval = new CMISParser.valueExpression_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        CMISParser.columnReference_return columnReference17 = null;

        CMISParser.valueFunction_return valueFunction18 = null;


        RewriteRuleSubtreeStream stream_valueFunction=new RewriteRuleSubtreeStream(adaptor,"rule valueFunction");
        RewriteRuleSubtreeStream stream_columnReference=new RewriteRuleSubtreeStream(adaptor,"rule columnReference");
            paraphrases.push("in value expression"); 
        try {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:325:2: ( columnReference -> columnReference | valueFunction -> valueFunction )
            int alt8=2;
            int LA8_0 = input.LA(1);

            if ( (LA8_0==ID) ) {
                int LA8_1 = input.LA(2);

                if ( (LA8_1==EOF||(LA8_1>=COMMA && LA8_1<=AS)||LA8_1==DOT||LA8_1==FROM||LA8_1==EQUALS||(LA8_1>=NOTEQUALS && LA8_1<=GREATERTHANOREQUALS)||(LA8_1>=ID && LA8_1<=DOUBLE_QUOTE)) ) {
                    alt8=1;
                }
                else if ( (LA8_1==LPAREN) && ((strict == false))) {
                    alt8=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 8, 1, input);

                    throw nvae;
                }
            }
            else if ( (LA8_0==DOUBLE_QUOTE) && ((strict == false))) {
                alt8=1;
            }
            else if ( (LA8_0==SCORE) ) {
                alt8=2;
            }
            else if ( (LA8_0==SELECT||LA8_0==AS||(LA8_0>=FROM && LA8_0<=ON)||(LA8_0>=WHERE && LA8_0<=NOT)||(LA8_0>=IN && LA8_0<=DESC)||(LA8_0>=TRUE && LA8_0<=TIMESTAMP)) && ((strict == false))) {
                alt8=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 8, 0, input);

                throw nvae;
            }
            switch (alt8) {
                case 1 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:325:4: columnReference
                    {
                    pushFollow(FOLLOW_columnReference_in_valueExpression435);
                    columnReference17=columnReference();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_columnReference.add(columnReference17.getTree());


                    // AST REWRITE
                    // elements: columnReference
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 326:3: -> columnReference
                    {
                        adaptor.addChild(root_0, stream_columnReference.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:327:5: valueFunction
                    {
                    pushFollow(FOLLOW_valueFunction_in_valueExpression448);
                    valueFunction18=valueFunction();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_valueFunction.add(valueFunction18.getTree());


                    // AST REWRITE
                    // elements: valueFunction
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 328:3: -> valueFunction
                    {
                        adaptor.addChild(root_0, stream_valueFunction.nextTree());

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
            if ( state.backtracking==0 ) {
                  paraphrases.pop(); 
            }
        }

            catch(RecognitionException e)
            {
                throw new CMISQueryException(getErrorString(e), e);
            }
        finally {
        }
        return retval;
    }
    // $ANTLR end "valueExpression"

    public static class columnReference_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "columnReference"
    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:331:1: columnReference : ( qualifier DOT )? columnName -> ^( COLUMN_REF columnName ( qualifier )? ) ;
    public final CMISParser.columnReference_return columnReference() throws RecognitionException {
        CMISParser.columnReference_return retval = new CMISParser.columnReference_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token DOT20=null;
        CMISParser.qualifier_return qualifier19 = null;

        CMISParser.columnName_return columnName21 = null;


        Object DOT20_tree=null;
        RewriteRuleTokenStream stream_DOT=new RewriteRuleTokenStream(adaptor,"token DOT");
        RewriteRuleSubtreeStream stream_columnName=new RewriteRuleSubtreeStream(adaptor,"rule columnName");
        RewriteRuleSubtreeStream stream_qualifier=new RewriteRuleSubtreeStream(adaptor,"rule qualifier");
            paraphrases.push("in column reference"); 
        try {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:334:2: ( ( qualifier DOT )? columnName -> ^( COLUMN_REF columnName ( qualifier )? ) )
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:334:4: ( qualifier DOT )? columnName
            {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:334:4: ( qualifier DOT )?
            int alt9=2;
            alt9 = dfa9.predict(input);
            switch (alt9) {
                case 1 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:334:6: qualifier DOT
                    {
                    pushFollow(FOLLOW_qualifier_in_columnReference497);
                    qualifier19=qualifier();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_qualifier.add(qualifier19.getTree());
                    DOT20=(Token)match(input,DOT,FOLLOW_DOT_in_columnReference499); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_DOT.add(DOT20);


                    }
                    break;

            }

            pushFollow(FOLLOW_columnName_in_columnReference504);
            columnName21=columnName();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_columnName.add(columnName21.getTree());


            // AST REWRITE
            // elements: columnName, qualifier
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 335:3: -> ^( COLUMN_REF columnName ( qualifier )? )
            {
                // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:335:6: ^( COLUMN_REF columnName ( qualifier )? )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(COLUMN_REF, "COLUMN_REF"), root_1);

                adaptor.addChild(root_1, stream_columnName.nextTree());
                // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:335:30: ( qualifier )?
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
            if ( state.backtracking==0 ) {
                  paraphrases.pop(); 
            }
        }

            catch(RecognitionException e)
            {
                throw new CMISQueryException(getErrorString(e), e);
            }
        finally {
        }
        return retval;
    }
    // $ANTLR end "columnReference"

    public static class multiValuedColumnReference_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "multiValuedColumnReference"
    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:342:1: multiValuedColumnReference : ( qualifier DOT )? multiValuedColumnName -> ^( COLUMN_REF multiValuedColumnName ( qualifier )? ) ;
    public final CMISParser.multiValuedColumnReference_return multiValuedColumnReference() throws RecognitionException {
        CMISParser.multiValuedColumnReference_return retval = new CMISParser.multiValuedColumnReference_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token DOT23=null;
        CMISParser.qualifier_return qualifier22 = null;

        CMISParser.multiValuedColumnName_return multiValuedColumnName24 = null;


        Object DOT23_tree=null;
        RewriteRuleTokenStream stream_DOT=new RewriteRuleTokenStream(adaptor,"token DOT");
        RewriteRuleSubtreeStream stream_qualifier=new RewriteRuleSubtreeStream(adaptor,"rule qualifier");
        RewriteRuleSubtreeStream stream_multiValuedColumnName=new RewriteRuleSubtreeStream(adaptor,"rule multiValuedColumnName");
            paraphrases.push("in column reference"); 
        try {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:345:2: ( ( qualifier DOT )? multiValuedColumnName -> ^( COLUMN_REF multiValuedColumnName ( qualifier )? ) )
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:345:10: ( qualifier DOT )? multiValuedColumnName
            {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:345:10: ( qualifier DOT )?
            int alt10=2;
            alt10 = dfa10.predict(input);
            switch (alt10) {
                case 1 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:345:12: qualifier DOT
                    {
                    pushFollow(FOLLOW_qualifier_in_multiValuedColumnReference566);
                    qualifier22=qualifier();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_qualifier.add(qualifier22.getTree());
                    DOT23=(Token)match(input,DOT,FOLLOW_DOT_in_multiValuedColumnReference568); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_DOT.add(DOT23);


                    }
                    break;

            }

            pushFollow(FOLLOW_multiValuedColumnName_in_multiValuedColumnReference574);
            multiValuedColumnName24=multiValuedColumnName();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_multiValuedColumnName.add(multiValuedColumnName24.getTree());


            // AST REWRITE
            // elements: multiValuedColumnName, qualifier
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 346:3: -> ^( COLUMN_REF multiValuedColumnName ( qualifier )? )
            {
                // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:346:6: ^( COLUMN_REF multiValuedColumnName ( qualifier )? )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(COLUMN_REF, "COLUMN_REF"), root_1);

                adaptor.addChild(root_1, stream_multiValuedColumnName.nextTree());
                // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:346:41: ( qualifier )?
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
            if ( state.backtracking==0 ) {
                  paraphrases.pop(); 
            }
        }

            catch(RecognitionException e)
            {
                throw new CMISQueryException(getErrorString(e), e);
            }
        finally {
        }
        return retval;
    }
    // $ANTLR end "multiValuedColumnReference"

    public static class valueFunction_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "valueFunction"
    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:349:1: valueFunction : (cmisFunctionName= cmisFunction LPAREN ( functionArgument )* RPAREN -> ^( FUNCTION $cmisFunctionName LPAREN ( functionArgument )* RPAREN ) | {...}? =>functionName= keyWordOrId LPAREN ( functionArgument )* RPAREN -> ^( FUNCTION $functionName LPAREN ( functionArgument )* RPAREN ) );
    public final CMISParser.valueFunction_return valueFunction() throws RecognitionException {
        CMISParser.valueFunction_return retval = new CMISParser.valueFunction_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token LPAREN25=null;
        Token RPAREN27=null;
        Token LPAREN28=null;
        Token RPAREN30=null;
        CMISParser.cmisFunction_return cmisFunctionName = null;

        CMISParser.keyWordOrId_return functionName = null;

        CMISParser.functionArgument_return functionArgument26 = null;

        CMISParser.functionArgument_return functionArgument29 = null;


        Object LPAREN25_tree=null;
        Object RPAREN27_tree=null;
        Object LPAREN28_tree=null;
        Object RPAREN30_tree=null;
        RewriteRuleTokenStream stream_RPAREN=new RewriteRuleTokenStream(adaptor,"token RPAREN");
        RewriteRuleTokenStream stream_LPAREN=new RewriteRuleTokenStream(adaptor,"token LPAREN");
        RewriteRuleSubtreeStream stream_cmisFunction=new RewriteRuleSubtreeStream(adaptor,"rule cmisFunction");
        RewriteRuleSubtreeStream stream_keyWordOrId=new RewriteRuleSubtreeStream(adaptor,"rule keyWordOrId");
        RewriteRuleSubtreeStream stream_functionArgument=new RewriteRuleSubtreeStream(adaptor,"rule functionArgument");
            paraphrases.push("in function"); 
        try {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:352:9: (cmisFunctionName= cmisFunction LPAREN ( functionArgument )* RPAREN -> ^( FUNCTION $cmisFunctionName LPAREN ( functionArgument )* RPAREN ) | {...}? =>functionName= keyWordOrId LPAREN ( functionArgument )* RPAREN -> ^( FUNCTION $functionName LPAREN ( functionArgument )* RPAREN ) )
            int alt13=2;
            alt13 = dfa13.predict(input);
            switch (alt13) {
                case 1 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:352:17: cmisFunctionName= cmisFunction LPAREN ( functionArgument )* RPAREN
                    {
                    pushFollow(FOLLOW_cmisFunction_in_valueFunction640);
                    cmisFunctionName=cmisFunction();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_cmisFunction.add(cmisFunctionName.getTree());
                    LPAREN25=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_valueFunction642); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_LPAREN.add(LPAREN25);

                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:352:54: ( functionArgument )*
                    loop11:
                    do {
                        int alt11=2;
                        int LA11_0 = input.LA(1);

                        if ( (LA11_0==ID) ) {
                            alt11=1;
                        }
                        else if ( (LA11_0==DOUBLE_QUOTE) && ((strict == false))) {
                            alt11=1;
                        }
                        else if ( (LA11_0==QUOTED_STRING||(LA11_0>=FLOATING_POINT_LITERAL && LA11_0<=TIMESTAMP)) ) {
                            alt11=1;
                        }
                        else if ( (LA11_0==COLON) && ((strict == false))) {
                            alt11=1;
                        }


                        switch (alt11) {
                    	case 1 :
                    	    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:352:54: functionArgument
                    	    {
                    	    pushFollow(FOLLOW_functionArgument_in_valueFunction644);
                    	    functionArgument26=functionArgument();

                    	    state._fsp--;
                    	    if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) stream_functionArgument.add(functionArgument26.getTree());

                    	    }
                    	    break;

                    	default :
                    	    break loop11;
                        }
                    } while (true);

                    RPAREN27=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_valueFunction647); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_RPAREN.add(RPAREN27);



                    // AST REWRITE
                    // elements: cmisFunctionName, functionArgument, LPAREN, RPAREN
                    // token labels: 
                    // rule labels: retval, cmisFunctionName
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
                    RewriteRuleSubtreeStream stream_cmisFunctionName=new RewriteRuleSubtreeStream(adaptor,"rule cmisFunctionName",cmisFunctionName!=null?cmisFunctionName.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 353:3: -> ^( FUNCTION $cmisFunctionName LPAREN ( functionArgument )* RPAREN )
                    {
                        // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:353:6: ^( FUNCTION $cmisFunctionName LPAREN ( functionArgument )* RPAREN )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(FUNCTION, "FUNCTION"), root_1);

                        adaptor.addChild(root_1, stream_cmisFunctionName.nextTree());
                        adaptor.addChild(root_1, stream_LPAREN.nextNode());
                        // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:353:42: ( functionArgument )*
                        while ( stream_functionArgument.hasNext() ) {
                            adaptor.addChild(root_1, stream_functionArgument.nextTree());

                        }
                        stream_functionArgument.reset();
                        adaptor.addChild(root_1, stream_RPAREN.nextNode());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:354:4: {...}? =>functionName= keyWordOrId LPAREN ( functionArgument )* RPAREN
                    {
                    if ( !((strict == false)) ) {
                        if (state.backtracking>0) {state.failed=true; return retval;}
                        throw new FailedPredicateException(input, "valueFunction", "strict == false");
                    }
                    pushFollow(FOLLOW_keyWordOrId_in_valueFunction675);
                    functionName=keyWordOrId();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_keyWordOrId.add(functionName.getTree());
                    LPAREN28=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_valueFunction677); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_LPAREN.add(LPAREN28);

                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:354:57: ( functionArgument )*
                    loop12:
                    do {
                        int alt12=2;
                        int LA12_0 = input.LA(1);

                        if ( (LA12_0==ID) ) {
                            alt12=1;
                        }
                        else if ( (LA12_0==DOUBLE_QUOTE) && ((strict == false))) {
                            alt12=1;
                        }
                        else if ( (LA12_0==QUOTED_STRING||(LA12_0>=FLOATING_POINT_LITERAL && LA12_0<=TIMESTAMP)) ) {
                            alt12=1;
                        }
                        else if ( (LA12_0==COLON) && ((strict == false))) {
                            alt12=1;
                        }


                        switch (alt12) {
                    	case 1 :
                    	    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:354:57: functionArgument
                    	    {
                    	    pushFollow(FOLLOW_functionArgument_in_valueFunction679);
                    	    functionArgument29=functionArgument();

                    	    state._fsp--;
                    	    if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) stream_functionArgument.add(functionArgument29.getTree());

                    	    }
                    	    break;

                    	default :
                    	    break loop12;
                        }
                    } while (true);

                    RPAREN30=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_valueFunction682); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_RPAREN.add(RPAREN30);



                    // AST REWRITE
                    // elements: RPAREN, LPAREN, functionName, functionArgument
                    // token labels: 
                    // rule labels: retval, functionName
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
                    RewriteRuleSubtreeStream stream_functionName=new RewriteRuleSubtreeStream(adaptor,"rule functionName",functionName!=null?functionName.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 355:3: -> ^( FUNCTION $functionName LPAREN ( functionArgument )* RPAREN )
                    {
                        // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:355:6: ^( FUNCTION $functionName LPAREN ( functionArgument )* RPAREN )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(FUNCTION, "FUNCTION"), root_1);

                        adaptor.addChild(root_1, stream_functionName.nextTree());
                        adaptor.addChild(root_1, stream_LPAREN.nextNode());
                        // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:355:38: ( functionArgument )*
                        while ( stream_functionArgument.hasNext() ) {
                            adaptor.addChild(root_1, stream_functionArgument.nextTree());

                        }
                        stream_functionArgument.reset();
                        adaptor.addChild(root_1, stream_RPAREN.nextNode());

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
            if ( state.backtracking==0 ) {
                  paraphrases.pop(); 
            }
        }

            catch(RecognitionException e)
            {
                throw new CMISQueryException(getErrorString(e), e);
            }
        finally {
        }
        return retval;
    }
    // $ANTLR end "valueFunction"

    public static class functionArgument_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "functionArgument"
    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:358:1: functionArgument : ( qualifier DOT columnName -> ^( COLUMN_REF columnName qualifier ) | identifier | literalOrParameterName );
    public final CMISParser.functionArgument_return functionArgument() throws RecognitionException {
        CMISParser.functionArgument_return retval = new CMISParser.functionArgument_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token DOT32=null;
        CMISParser.qualifier_return qualifier31 = null;

        CMISParser.columnName_return columnName33 = null;

        CMISParser.identifier_return identifier34 = null;

        CMISParser.literalOrParameterName_return literalOrParameterName35 = null;


        Object DOT32_tree=null;
        RewriteRuleTokenStream stream_DOT=new RewriteRuleTokenStream(adaptor,"token DOT");
        RewriteRuleSubtreeStream stream_columnName=new RewriteRuleSubtreeStream(adaptor,"rule columnName");
        RewriteRuleSubtreeStream stream_qualifier=new RewriteRuleSubtreeStream(adaptor,"rule qualifier");
        try {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:359:5: ( qualifier DOT columnName -> ^( COLUMN_REF columnName qualifier ) | identifier | literalOrParameterName )
            int alt14=3;
            alt14 = dfa14.predict(input);
            switch (alt14) {
                case 1 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:359:9: qualifier DOT columnName
                    {
                    pushFollow(FOLLOW_qualifier_in_functionArgument717);
                    qualifier31=qualifier();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_qualifier.add(qualifier31.getTree());
                    DOT32=(Token)match(input,DOT,FOLLOW_DOT_in_functionArgument719); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_DOT.add(DOT32);

                    pushFollow(FOLLOW_columnName_in_functionArgument721);
                    columnName33=columnName();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_columnName.add(columnName33.getTree());


                    // AST REWRITE
                    // elements: columnName, qualifier
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 360:5: -> ^( COLUMN_REF columnName qualifier )
                    {
                        // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:360:8: ^( COLUMN_REF columnName qualifier )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(COLUMN_REF, "COLUMN_REF"), root_1);

                        adaptor.addChild(root_1, stream_columnName.nextTree());
                        adaptor.addChild(root_1, stream_qualifier.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:361:9: identifier
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_identifier_in_functionArgument745);
                    identifier34=identifier();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, identifier34.getTree());

                    }
                    break;
                case 3 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:362:9: literalOrParameterName
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_literalOrParameterName_in_functionArgument755);
                    literalOrParameterName35=literalOrParameterName();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, literalOrParameterName35.getTree());

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
                throw new CMISQueryException(getErrorString(e), e);
            }
        finally {
        }
        return retval;
    }
    // $ANTLR end "functionArgument"

    public static class qualifier_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "qualifier"
    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:365:1: qualifier : ( ( tableName )=> tableName -> tableName | correlationName -> correlationName );
    public final CMISParser.qualifier_return qualifier() throws RecognitionException {
        CMISParser.qualifier_return retval = new CMISParser.qualifier_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        CMISParser.tableName_return tableName36 = null;

        CMISParser.correlationName_return correlationName37 = null;


        RewriteRuleSubtreeStream stream_correlationName=new RewriteRuleSubtreeStream(adaptor,"rule correlationName");
        RewriteRuleSubtreeStream stream_tableName=new RewriteRuleSubtreeStream(adaptor,"rule tableName");
        try {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:366:2: ( ( tableName )=> tableName -> tableName | correlationName -> correlationName )
            int alt15=2;
            alt15 = dfa15.predict(input);
            switch (alt15) {
                case 1 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:366:4: ( tableName )=> tableName
                    {
                    pushFollow(FOLLOW_tableName_in_qualifier776);
                    tableName36=tableName();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_tableName.add(tableName36.getTree());


                    // AST REWRITE
                    // elements: tableName
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 367:3: -> tableName
                    {
                        adaptor.addChild(root_0, stream_tableName.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:368:5: correlationName
                    {
                    pushFollow(FOLLOW_correlationName_in_qualifier788);
                    correlationName37=correlationName();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_correlationName.add(correlationName37.getTree());


                    // AST REWRITE
                    // elements: correlationName
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 369:3: -> correlationName
                    {
                        adaptor.addChild(root_0, stream_correlationName.nextTree());

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
                throw new CMISQueryException(getErrorString(e), e);
            }
        finally {
        }
        return retval;
    }
    // $ANTLR end "qualifier"

    public static class fromClause_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "fromClause"
    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:372:1: fromClause : FROM tableReference -> tableReference ;
    public final CMISParser.fromClause_return fromClause() throws RecognitionException {
        CMISParser.fromClause_return retval = new CMISParser.fromClause_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token FROM38=null;
        CMISParser.tableReference_return tableReference39 = null;


        Object FROM38_tree=null;
        RewriteRuleTokenStream stream_FROM=new RewriteRuleTokenStream(adaptor,"token FROM");
        RewriteRuleSubtreeStream stream_tableReference=new RewriteRuleSubtreeStream(adaptor,"rule tableReference");
            paraphrases.push("in from"); 
        try {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:375:2: ( FROM tableReference -> tableReference )
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:375:4: FROM tableReference
            {
            FROM38=(Token)match(input,FROM,FOLLOW_FROM_in_fromClause833); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_FROM.add(FROM38);

            pushFollow(FOLLOW_tableReference_in_fromClause835);
            tableReference39=tableReference();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_tableReference.add(tableReference39.getTree());


            // AST REWRITE
            // elements: tableReference
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 376:3: -> tableReference
            {
                adaptor.addChild(root_0, stream_tableReference.nextTree());

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
            if ( state.backtracking==0 ) {
                  paraphrases.pop(); 
            }
        }

            catch(RecognitionException e)
            {
                throw new CMISQueryException(getErrorString(e), e);
            }
        finally {
        }
        return retval;
    }
    // $ANTLR end "fromClause"

    public static class tableReference_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "tableReference"
    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:379:1: tableReference : singleTable ( joinedTable )* -> ^( SOURCE singleTable ( joinedTable )* ) ;
    public final CMISParser.tableReference_return tableReference() throws RecognitionException {
        CMISParser.tableReference_return retval = new CMISParser.tableReference_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        CMISParser.singleTable_return singleTable40 = null;

        CMISParser.joinedTable_return joinedTable41 = null;


        RewriteRuleSubtreeStream stream_singleTable=new RewriteRuleSubtreeStream(adaptor,"rule singleTable");
        RewriteRuleSubtreeStream stream_joinedTable=new RewriteRuleSubtreeStream(adaptor,"rule joinedTable");
        try {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:380:2: ( singleTable ( joinedTable )* -> ^( SOURCE singleTable ( joinedTable )* ) )
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:380:4: singleTable ( joinedTable )*
            {
            pushFollow(FOLLOW_singleTable_in_tableReference856);
            singleTable40=singleTable();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_singleTable.add(singleTable40.getTree());
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:380:16: ( joinedTable )*
            loop16:
            do {
                int alt16=2;
                int LA16_0 = input.LA(1);

                if ( ((LA16_0>=JOIN && LA16_0<=LEFT)) ) {
                    alt16=1;
                }


                switch (alt16) {
            	case 1 :
            	    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:380:16: joinedTable
            	    {
            	    pushFollow(FOLLOW_joinedTable_in_tableReference858);
            	    joinedTable41=joinedTable();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_joinedTable.add(joinedTable41.getTree());

            	    }
            	    break;

            	default :
            	    break loop16;
                }
            } while (true);



            // AST REWRITE
            // elements: singleTable, joinedTable
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 381:3: -> ^( SOURCE singleTable ( joinedTable )* )
            {
                // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:381:6: ^( SOURCE singleTable ( joinedTable )* )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(SOURCE, "SOURCE"), root_1);

                adaptor.addChild(root_1, stream_singleTable.nextTree());
                // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:381:27: ( joinedTable )*
                while ( stream_joinedTable.hasNext() ) {
                    adaptor.addChild(root_1, stream_joinedTable.nextTree());

                }
                stream_joinedTable.reset();

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
                throw new CMISQueryException(getErrorString(e), e);
            }
        finally {
        }
        return retval;
    }
    // $ANTLR end "tableReference"

    public static class singleTable_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "singleTable"
    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:387:1: singleTable : ( tableName ( ( AS )? correlationName )? -> ^( TABLE_REF tableName ( correlationName )? ) | LPAREN joinedTables RPAREN -> ^( TABLE joinedTables ) );
    public final CMISParser.singleTable_return singleTable() throws RecognitionException {
        CMISParser.singleTable_return retval = new CMISParser.singleTable_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token AS43=null;
        Token LPAREN45=null;
        Token RPAREN47=null;
        CMISParser.tableName_return tableName42 = null;

        CMISParser.correlationName_return correlationName44 = null;

        CMISParser.joinedTables_return joinedTables46 = null;


        Object AS43_tree=null;
        Object LPAREN45_tree=null;
        Object RPAREN47_tree=null;
        RewriteRuleTokenStream stream_AS=new RewriteRuleTokenStream(adaptor,"token AS");
        RewriteRuleTokenStream stream_RPAREN=new RewriteRuleTokenStream(adaptor,"token RPAREN");
        RewriteRuleTokenStream stream_LPAREN=new RewriteRuleTokenStream(adaptor,"token LPAREN");
        RewriteRuleSubtreeStream stream_correlationName=new RewriteRuleSubtreeStream(adaptor,"rule correlationName");
        RewriteRuleSubtreeStream stream_tableName=new RewriteRuleSubtreeStream(adaptor,"rule tableName");
        RewriteRuleSubtreeStream stream_joinedTables=new RewriteRuleSubtreeStream(adaptor,"rule joinedTables");
        try {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:388:2: ( tableName ( ( AS )? correlationName )? -> ^( TABLE_REF tableName ( correlationName )? ) | LPAREN joinedTables RPAREN -> ^( TABLE joinedTables ) )
            int alt19=2;
            int LA19_0 = input.LA(1);

            if ( (LA19_0==ID) ) {
                alt19=1;
            }
            else if ( (LA19_0==DOUBLE_QUOTE) && ((strict == false))) {
                alt19=1;
            }
            else if ( (LA19_0==LPAREN) ) {
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
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:388:4: tableName ( ( AS )? correlationName )?
                    {
                    pushFollow(FOLLOW_tableName_in_singleTable886);
                    tableName42=tableName();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_tableName.add(tableName42.getTree());
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:388:14: ( ( AS )? correlationName )?
                    int alt18=2;
                    int LA18_0 = input.LA(1);

                    if ( (LA18_0==AS||LA18_0==ID) ) {
                        alt18=1;
                    }
                    else if ( (LA18_0==DOUBLE_QUOTE) && ((strict == false))) {
                        alt18=1;
                    }
                    switch (alt18) {
                        case 1 :
                            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:388:16: ( AS )? correlationName
                            {
                            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:388:16: ( AS )?
                            int alt17=2;
                            int LA17_0 = input.LA(1);

                            if ( (LA17_0==AS) ) {
                                alt17=1;
                            }
                            switch (alt17) {
                                case 1 :
                                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:388:16: AS
                                    {
                                    AS43=(Token)match(input,AS,FOLLOW_AS_in_singleTable890); if (state.failed) return retval; 
                                    if ( state.backtracking==0 ) stream_AS.add(AS43);


                                    }
                                    break;

                            }

                            pushFollow(FOLLOW_correlationName_in_singleTable893);
                            correlationName44=correlationName();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_correlationName.add(correlationName44.getTree());

                            }
                            break;

                    }



                    // AST REWRITE
                    // elements: tableName, correlationName
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 389:3: -> ^( TABLE_REF tableName ( correlationName )? )
                    {
                        // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:389:6: ^( TABLE_REF tableName ( correlationName )? )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(TABLE_REF, "TABLE_REF"), root_1);

                        adaptor.addChild(root_1, stream_tableName.nextTree());
                        // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:389:28: ( correlationName )?
                        if ( stream_correlationName.hasNext() ) {
                            adaptor.addChild(root_1, stream_correlationName.nextTree());

                        }
                        stream_correlationName.reset();

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:390:4: LPAREN joinedTables RPAREN
                    {
                    LPAREN45=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_singleTable914); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_LPAREN.add(LPAREN45);

                    pushFollow(FOLLOW_joinedTables_in_singleTable916);
                    joinedTables46=joinedTables();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_joinedTables.add(joinedTables46.getTree());
                    RPAREN47=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_singleTable918); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_RPAREN.add(RPAREN47);



                    // AST REWRITE
                    // elements: joinedTables
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 391:3: -> ^( TABLE joinedTables )
                    {
                        // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:391:6: ^( TABLE joinedTables )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(TABLE, "TABLE"), root_1);

                        adaptor.addChild(root_1, stream_joinedTables.nextTree());

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
                throw new CMISQueryException(getErrorString(e), e);
            }
        finally {
        }
        return retval;
    }
    // $ANTLR end "singleTable"

    public static class joinedTable_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "joinedTable"
    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:394:1: joinedTable : ( joinType )? JOIN tableReference joinSpecification -> ^( JOIN tableReference ( joinType )? joinSpecification ) ;
    public final CMISParser.joinedTable_return joinedTable() throws RecognitionException {
        CMISParser.joinedTable_return retval = new CMISParser.joinedTable_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token JOIN49=null;
        CMISParser.joinType_return joinType48 = null;

        CMISParser.tableReference_return tableReference50 = null;

        CMISParser.joinSpecification_return joinSpecification51 = null;


        Object JOIN49_tree=null;
        RewriteRuleTokenStream stream_JOIN=new RewriteRuleTokenStream(adaptor,"token JOIN");
        RewriteRuleSubtreeStream stream_tableReference=new RewriteRuleSubtreeStream(adaptor,"rule tableReference");
        RewriteRuleSubtreeStream stream_joinType=new RewriteRuleSubtreeStream(adaptor,"rule joinType");
        RewriteRuleSubtreeStream stream_joinSpecification=new RewriteRuleSubtreeStream(adaptor,"rule joinSpecification");
            paraphrases.push("in join"); 
        try {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:397:2: ( ( joinType )? JOIN tableReference joinSpecification -> ^( JOIN tableReference ( joinType )? joinSpecification ) )
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:397:4: ( joinType )? JOIN tableReference joinSpecification
            {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:397:4: ( joinType )?
            int alt20=2;
            int LA20_0 = input.LA(1);

            if ( ((LA20_0>=INNER && LA20_0<=LEFT)) ) {
                alt20=1;
            }
            switch (alt20) {
                case 1 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:397:4: joinType
                    {
                    pushFollow(FOLLOW_joinType_in_joinedTable966);
                    joinType48=joinType();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_joinType.add(joinType48.getTree());

                    }
                    break;

            }

            JOIN49=(Token)match(input,JOIN,FOLLOW_JOIN_in_joinedTable969); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_JOIN.add(JOIN49);

            pushFollow(FOLLOW_tableReference_in_joinedTable971);
            tableReference50=tableReference();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_tableReference.add(tableReference50.getTree());
            pushFollow(FOLLOW_joinSpecification_in_joinedTable973);
            joinSpecification51=joinSpecification();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_joinSpecification.add(joinSpecification51.getTree());


            // AST REWRITE
            // elements: joinType, joinSpecification, tableReference, JOIN
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 398:3: -> ^( JOIN tableReference ( joinType )? joinSpecification )
            {
                // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:398:6: ^( JOIN tableReference ( joinType )? joinSpecification )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot(stream_JOIN.nextNode(), root_1);

                adaptor.addChild(root_1, stream_tableReference.nextTree());
                // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:398:28: ( joinType )?
                if ( stream_joinType.hasNext() ) {
                    adaptor.addChild(root_1, stream_joinType.nextTree());

                }
                stream_joinType.reset();
                adaptor.addChild(root_1, stream_joinSpecification.nextTree());

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
            if ( state.backtracking==0 ) {
                  paraphrases.pop(); 
            }
        }

            catch(RecognitionException e)
            {
                throw new CMISQueryException(getErrorString(e), e);
            }
        finally {
        }
        return retval;
    }
    // $ANTLR end "joinedTable"

    public static class joinedTables_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "joinedTables"
    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:402:1: joinedTables : singleTable ( joinedTable )+ -> ^( SOURCE singleTable ( joinedTable )+ ) ;
    public final CMISParser.joinedTables_return joinedTables() throws RecognitionException {
        CMISParser.joinedTables_return retval = new CMISParser.joinedTables_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        CMISParser.singleTable_return singleTable52 = null;

        CMISParser.joinedTable_return joinedTable53 = null;


        RewriteRuleSubtreeStream stream_singleTable=new RewriteRuleSubtreeStream(adaptor,"rule singleTable");
        RewriteRuleSubtreeStream stream_joinedTable=new RewriteRuleSubtreeStream(adaptor,"rule joinedTable");
        try {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:403:2: ( singleTable ( joinedTable )+ -> ^( SOURCE singleTable ( joinedTable )+ ) )
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:403:4: singleTable ( joinedTable )+
            {
            pushFollow(FOLLOW_singleTable_in_joinedTables1001);
            singleTable52=singleTable();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_singleTable.add(singleTable52.getTree());
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:403:16: ( joinedTable )+
            int cnt21=0;
            loop21:
            do {
                int alt21=2;
                int LA21_0 = input.LA(1);

                if ( ((LA21_0>=JOIN && LA21_0<=LEFT)) ) {
                    alt21=1;
                }


                switch (alt21) {
            	case 1 :
            	    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:403:16: joinedTable
            	    {
            	    pushFollow(FOLLOW_joinedTable_in_joinedTables1003);
            	    joinedTable53=joinedTable();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_joinedTable.add(joinedTable53.getTree());

            	    }
            	    break;

            	default :
            	    if ( cnt21 >= 1 ) break loop21;
            	    if (state.backtracking>0) {state.failed=true; return retval;}
                        EarlyExitException eee =
                            new EarlyExitException(21, input);
                        throw eee;
                }
                cnt21++;
            } while (true);



            // AST REWRITE
            // elements: singleTable, joinedTable
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 404:3: -> ^( SOURCE singleTable ( joinedTable )+ )
            {
                // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:404:6: ^( SOURCE singleTable ( joinedTable )+ )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(SOURCE, "SOURCE"), root_1);

                adaptor.addChild(root_1, stream_singleTable.nextTree());
                if ( !(stream_joinedTable.hasNext()) ) {
                    throw new RewriteEarlyExitException();
                }
                while ( stream_joinedTable.hasNext() ) {
                    adaptor.addChild(root_1, stream_joinedTable.nextTree());

                }
                stream_joinedTable.reset();

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
                throw new CMISQueryException(getErrorString(e), e);
            }
        finally {
        }
        return retval;
    }
    // $ANTLR end "joinedTables"

    public static class joinType_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "joinType"
    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:407:1: joinType : ( INNER -> INNER | LEFT ( OUTER )? -> LEFT );
    public final CMISParser.joinType_return joinType() throws RecognitionException {
        CMISParser.joinType_return retval = new CMISParser.joinType_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token INNER54=null;
        Token LEFT55=null;
        Token OUTER56=null;

        Object INNER54_tree=null;
        Object LEFT55_tree=null;
        Object OUTER56_tree=null;
        RewriteRuleTokenStream stream_OUTER=new RewriteRuleTokenStream(adaptor,"token OUTER");
        RewriteRuleTokenStream stream_INNER=new RewriteRuleTokenStream(adaptor,"token INNER");
        RewriteRuleTokenStream stream_LEFT=new RewriteRuleTokenStream(adaptor,"token LEFT");

        try {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:408:2: ( INNER -> INNER | LEFT ( OUTER )? -> LEFT )
            int alt23=2;
            int LA23_0 = input.LA(1);

            if ( (LA23_0==INNER) ) {
                alt23=1;
            }
            else if ( (LA23_0==LEFT) ) {
                alt23=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 23, 0, input);

                throw nvae;
            }
            switch (alt23) {
                case 1 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:408:4: INNER
                    {
                    INNER54=(Token)match(input,INNER,FOLLOW_INNER_in_joinType1030); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_INNER.add(INNER54);



                    // AST REWRITE
                    // elements: INNER
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 409:3: -> INNER
                    {
                        adaptor.addChild(root_0, stream_INNER.nextNode());

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:410:5: LEFT ( OUTER )?
                    {
                    LEFT55=(Token)match(input,LEFT,FOLLOW_LEFT_in_joinType1042); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_LEFT.add(LEFT55);

                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:410:10: ( OUTER )?
                    int alt22=2;
                    int LA22_0 = input.LA(1);

                    if ( (LA22_0==OUTER) ) {
                        alt22=1;
                    }
                    switch (alt22) {
                        case 1 :
                            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:410:10: OUTER
                            {
                            OUTER56=(Token)match(input,OUTER,FOLLOW_OUTER_in_joinType1044); if (state.failed) return retval; 
                            if ( state.backtracking==0 ) stream_OUTER.add(OUTER56);


                            }
                            break;

                    }



                    // AST REWRITE
                    // elements: LEFT
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 411:3: -> LEFT
                    {
                        adaptor.addChild(root_0, stream_LEFT.nextNode());

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
                throw new CMISQueryException(getErrorString(e), e);
            }
        finally {
        }
        return retval;
    }
    // $ANTLR end "joinType"

    public static class joinSpecification_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "joinSpecification"
    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:414:1: joinSpecification : ON lhs= columnReference EQUALS rhs= columnReference -> ^( ON $lhs EQUALS $rhs) ;
    public final CMISParser.joinSpecification_return joinSpecification() throws RecognitionException {
        CMISParser.joinSpecification_return retval = new CMISParser.joinSpecification_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token ON57=null;
        Token EQUALS58=null;
        CMISParser.columnReference_return lhs = null;

        CMISParser.columnReference_return rhs = null;


        Object ON57_tree=null;
        Object EQUALS58_tree=null;
        RewriteRuleTokenStream stream_ON=new RewriteRuleTokenStream(adaptor,"token ON");
        RewriteRuleTokenStream stream_EQUALS=new RewriteRuleTokenStream(adaptor,"token EQUALS");
        RewriteRuleSubtreeStream stream_columnReference=new RewriteRuleSubtreeStream(adaptor,"rule columnReference");
            paraphrases.push("in join condition"); 
        try {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:417:2: ( ON lhs= columnReference EQUALS rhs= columnReference -> ^( ON $lhs EQUALS $rhs) )
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:417:4: ON lhs= columnReference EQUALS rhs= columnReference
            {
            ON57=(Token)match(input,ON,FOLLOW_ON_in_joinSpecification1090); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_ON.add(ON57);

            pushFollow(FOLLOW_columnReference_in_joinSpecification1094);
            lhs=columnReference();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_columnReference.add(lhs.getTree());
            EQUALS58=(Token)match(input,EQUALS,FOLLOW_EQUALS_in_joinSpecification1096); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_EQUALS.add(EQUALS58);

            pushFollow(FOLLOW_columnReference_in_joinSpecification1100);
            rhs=columnReference();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_columnReference.add(rhs.getTree());


            // AST REWRITE
            // elements: EQUALS, lhs, rhs, ON
            // token labels: 
            // rule labels: retval, rhs, lhs
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            RewriteRuleSubtreeStream stream_rhs=new RewriteRuleSubtreeStream(adaptor,"rule rhs",rhs!=null?rhs.tree:null);
            RewriteRuleSubtreeStream stream_lhs=new RewriteRuleSubtreeStream(adaptor,"rule lhs",lhs!=null?lhs.tree:null);

            root_0 = (Object)adaptor.nil();
            // 418:3: -> ^( ON $lhs EQUALS $rhs)
            {
                // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:418:6: ^( ON $lhs EQUALS $rhs)
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot(stream_ON.nextNode(), root_1);

                adaptor.addChild(root_1, stream_lhs.nextTree());
                adaptor.addChild(root_1, stream_EQUALS.nextNode());
                adaptor.addChild(root_1, stream_rhs.nextTree());

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
            if ( state.backtracking==0 ) {
                  paraphrases.pop(); 
            }
        }

            catch(RecognitionException e)
            {
                throw new CMISQueryException(getErrorString(e), e);
            }
        finally {
        }
        return retval;
    }
    // $ANTLR end "joinSpecification"

    public static class whereClause_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "whereClause"
    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:425:1: whereClause : WHERE searchOrCondition -> searchOrCondition ;
    public final CMISParser.whereClause_return whereClause() throws RecognitionException {
        CMISParser.whereClause_return retval = new CMISParser.whereClause_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token WHERE59=null;
        CMISParser.searchOrCondition_return searchOrCondition60 = null;


        Object WHERE59_tree=null;
        RewriteRuleTokenStream stream_WHERE=new RewriteRuleTokenStream(adaptor,"token WHERE");
        RewriteRuleSubtreeStream stream_searchOrCondition=new RewriteRuleSubtreeStream(adaptor,"rule searchOrCondition");
            paraphrases.push("in where"); 
        try {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:428:2: ( WHERE searchOrCondition -> searchOrCondition )
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:428:4: WHERE searchOrCondition
            {
            WHERE59=(Token)match(input,WHERE,FOLLOW_WHERE_in_whereClause1159); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_WHERE.add(WHERE59);

            pushFollow(FOLLOW_searchOrCondition_in_whereClause1161);
            searchOrCondition60=searchOrCondition();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_searchOrCondition.add(searchOrCondition60.getTree());


            // AST REWRITE
            // elements: searchOrCondition
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 429:3: -> searchOrCondition
            {
                adaptor.addChild(root_0, stream_searchOrCondition.nextTree());

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
            if ( state.backtracking==0 ) {
                  paraphrases.pop(); 
            }
        }

            catch(RecognitionException e)
            {
                throw new CMISQueryException(getErrorString(e), e);
            }
        finally {
        }
        return retval;
    }
    // $ANTLR end "whereClause"

    public static class searchOrCondition_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "searchOrCondition"
    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:432:1: searchOrCondition : searchAndCondition ( OR searchAndCondition )* -> ^( DISJUNCTION ( searchAndCondition )+ ) ;
    public final CMISParser.searchOrCondition_return searchOrCondition() throws RecognitionException {
        CMISParser.searchOrCondition_return retval = new CMISParser.searchOrCondition_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token OR62=null;
        CMISParser.searchAndCondition_return searchAndCondition61 = null;

        CMISParser.searchAndCondition_return searchAndCondition63 = null;


        Object OR62_tree=null;
        RewriteRuleTokenStream stream_OR=new RewriteRuleTokenStream(adaptor,"token OR");
        RewriteRuleSubtreeStream stream_searchAndCondition=new RewriteRuleSubtreeStream(adaptor,"rule searchAndCondition");
        try {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:436:2: ( searchAndCondition ( OR searchAndCondition )* -> ^( DISJUNCTION ( searchAndCondition )+ ) )
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:436:4: searchAndCondition ( OR searchAndCondition )*
            {
            pushFollow(FOLLOW_searchAndCondition_in_searchOrCondition1181);
            searchAndCondition61=searchAndCondition();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_searchAndCondition.add(searchAndCondition61.getTree());
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:436:23: ( OR searchAndCondition )*
            loop24:
            do {
                int alt24=2;
                int LA24_0 = input.LA(1);

                if ( (LA24_0==OR) ) {
                    alt24=1;
                }


                switch (alt24) {
            	case 1 :
            	    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:436:24: OR searchAndCondition
            	    {
            	    OR62=(Token)match(input,OR,FOLLOW_OR_in_searchOrCondition1184); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_OR.add(OR62);

            	    pushFollow(FOLLOW_searchAndCondition_in_searchOrCondition1186);
            	    searchAndCondition63=searchAndCondition();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_searchAndCondition.add(searchAndCondition63.getTree());

            	    }
            	    break;

            	default :
            	    break loop24;
                }
            } while (true);



            // AST REWRITE
            // elements: searchAndCondition
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 437:3: -> ^( DISJUNCTION ( searchAndCondition )+ )
            {
                // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:437:6: ^( DISJUNCTION ( searchAndCondition )+ )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(DISJUNCTION, "DISJUNCTION"), root_1);

                if ( !(stream_searchAndCondition.hasNext()) ) {
                    throw new RewriteEarlyExitException();
                }
                while ( stream_searchAndCondition.hasNext() ) {
                    adaptor.addChild(root_1, stream_searchAndCondition.nextTree());

                }
                stream_searchAndCondition.reset();

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
                throw new CMISQueryException(getErrorString(e), e);
            }
        finally {
        }
        return retval;
    }
    // $ANTLR end "searchOrCondition"

    public static class searchAndCondition_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "searchAndCondition"
    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:441:1: searchAndCondition : searchNotCondition ( AND searchNotCondition )* -> ^( CONJUNCTION ( searchNotCondition )+ ) ;
    public final CMISParser.searchAndCondition_return searchAndCondition() throws RecognitionException {
        CMISParser.searchAndCondition_return retval = new CMISParser.searchAndCondition_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token AND65=null;
        CMISParser.searchNotCondition_return searchNotCondition64 = null;

        CMISParser.searchNotCondition_return searchNotCondition66 = null;


        Object AND65_tree=null;
        RewriteRuleTokenStream stream_AND=new RewriteRuleTokenStream(adaptor,"token AND");
        RewriteRuleSubtreeStream stream_searchNotCondition=new RewriteRuleSubtreeStream(adaptor,"rule searchNotCondition");
        try {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:445:2: ( searchNotCondition ( AND searchNotCondition )* -> ^( CONJUNCTION ( searchNotCondition )+ ) )
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:445:4: searchNotCondition ( AND searchNotCondition )*
            {
            pushFollow(FOLLOW_searchNotCondition_in_searchAndCondition1214);
            searchNotCondition64=searchNotCondition();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_searchNotCondition.add(searchNotCondition64.getTree());
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:445:23: ( AND searchNotCondition )*
            loop25:
            do {
                int alt25=2;
                int LA25_0 = input.LA(1);

                if ( (LA25_0==AND) ) {
                    alt25=1;
                }


                switch (alt25) {
            	case 1 :
            	    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:445:24: AND searchNotCondition
            	    {
            	    AND65=(Token)match(input,AND,FOLLOW_AND_in_searchAndCondition1217); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_AND.add(AND65);

            	    pushFollow(FOLLOW_searchNotCondition_in_searchAndCondition1219);
            	    searchNotCondition66=searchNotCondition();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_searchNotCondition.add(searchNotCondition66.getTree());

            	    }
            	    break;

            	default :
            	    break loop25;
                }
            } while (true);



            // AST REWRITE
            // elements: searchNotCondition
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 446:3: -> ^( CONJUNCTION ( searchNotCondition )+ )
            {
                // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:446:6: ^( CONJUNCTION ( searchNotCondition )+ )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(CONJUNCTION, "CONJUNCTION"), root_1);

                if ( !(stream_searchNotCondition.hasNext()) ) {
                    throw new RewriteEarlyExitException();
                }
                while ( stream_searchNotCondition.hasNext() ) {
                    adaptor.addChild(root_1, stream_searchNotCondition.nextTree());

                }
                stream_searchNotCondition.reset();

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
                throw new CMISQueryException(getErrorString(e), e);
            }
        finally {
        }
        return retval;
    }
    // $ANTLR end "searchAndCondition"

    public static class searchNotCondition_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "searchNotCondition"
    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:449:1: searchNotCondition : ( NOT searchTest -> ^( NEGATION searchTest ) | searchTest -> searchTest );
    public final CMISParser.searchNotCondition_return searchNotCondition() throws RecognitionException {
        CMISParser.searchNotCondition_return retval = new CMISParser.searchNotCondition_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token NOT67=null;
        CMISParser.searchTest_return searchTest68 = null;

        CMISParser.searchTest_return searchTest69 = null;


        Object NOT67_tree=null;
        RewriteRuleTokenStream stream_NOT=new RewriteRuleTokenStream(adaptor,"token NOT");
        RewriteRuleSubtreeStream stream_searchTest=new RewriteRuleSubtreeStream(adaptor,"rule searchTest");
        try {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:450:2: ( NOT searchTest -> ^( NEGATION searchTest ) | searchTest -> searchTest )
            int alt26=2;
            alt26 = dfa26.predict(input);
            switch (alt26) {
                case 1 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:450:4: NOT searchTest
                    {
                    NOT67=(Token)match(input,NOT,FOLLOW_NOT_in_searchNotCondition1246); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_NOT.add(NOT67);

                    pushFollow(FOLLOW_searchTest_in_searchNotCondition1248);
                    searchTest68=searchTest();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_searchTest.add(searchTest68.getTree());


                    // AST REWRITE
                    // elements: searchTest
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 451:3: -> ^( NEGATION searchTest )
                    {
                        // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:451:6: ^( NEGATION searchTest )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(NEGATION, "NEGATION"), root_1);

                        adaptor.addChild(root_1, stream_searchTest.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:452:4: searchTest
                    {
                    pushFollow(FOLLOW_searchTest_in_searchNotCondition1263);
                    searchTest69=searchTest();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_searchTest.add(searchTest69.getTree());


                    // AST REWRITE
                    // elements: searchTest
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 453:3: -> searchTest
                    {
                        adaptor.addChild(root_0, stream_searchTest.nextTree());

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
                throw new CMISQueryException(getErrorString(e), e);
            }
        finally {
        }
        return retval;
    }
    // $ANTLR end "searchNotCondition"

    public static class searchTest_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "searchTest"
    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:456:1: searchTest : ( predicate -> predicate | LPAREN searchOrCondition RPAREN -> searchOrCondition );
    public final CMISParser.searchTest_return searchTest() throws RecognitionException {
        CMISParser.searchTest_return retval = new CMISParser.searchTest_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token LPAREN71=null;
        Token RPAREN73=null;
        CMISParser.predicate_return predicate70 = null;

        CMISParser.searchOrCondition_return searchOrCondition72 = null;


        Object LPAREN71_tree=null;
        Object RPAREN73_tree=null;
        RewriteRuleTokenStream stream_RPAREN=new RewriteRuleTokenStream(adaptor,"token RPAREN");
        RewriteRuleTokenStream stream_LPAREN=new RewriteRuleTokenStream(adaptor,"token LPAREN");
        RewriteRuleSubtreeStream stream_predicate=new RewriteRuleSubtreeStream(adaptor,"rule predicate");
        RewriteRuleSubtreeStream stream_searchOrCondition=new RewriteRuleSubtreeStream(adaptor,"rule searchOrCondition");
        try {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:457:2: ( predicate -> predicate | LPAREN searchOrCondition RPAREN -> searchOrCondition )
            int alt27=2;
            alt27 = dfa27.predict(input);
            switch (alt27) {
                case 1 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:457:4: predicate
                    {
                    pushFollow(FOLLOW_predicate_in_searchTest1281);
                    predicate70=predicate();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_predicate.add(predicate70.getTree());


                    // AST REWRITE
                    // elements: predicate
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 458:3: -> predicate
                    {
                        adaptor.addChild(root_0, stream_predicate.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:459:4: LPAREN searchOrCondition RPAREN
                    {
                    LPAREN71=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_searchTest1292); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_LPAREN.add(LPAREN71);

                    pushFollow(FOLLOW_searchOrCondition_in_searchTest1294);
                    searchOrCondition72=searchOrCondition();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_searchOrCondition.add(searchOrCondition72.getTree());
                    RPAREN73=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_searchTest1296); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_RPAREN.add(RPAREN73);



                    // AST REWRITE
                    // elements: searchOrCondition
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 460:3: -> searchOrCondition
                    {
                        adaptor.addChild(root_0, stream_searchOrCondition.nextTree());

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
                throw new CMISQueryException(getErrorString(e), e);
            }
        finally {
        }
        return retval;
    }
    // $ANTLR end "searchTest"

    public static class predicate_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "predicate"
    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:463:1: predicate : ( comparisonPredicate | inPredicate | likePredicate | nullPredicate | quantifiedComparisonPredicate | quantifiedInPredicate | textSearchPredicate | folderPredicate );
    public final CMISParser.predicate_return predicate() throws RecognitionException {
        CMISParser.predicate_return retval = new CMISParser.predicate_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        CMISParser.comparisonPredicate_return comparisonPredicate74 = null;

        CMISParser.inPredicate_return inPredicate75 = null;

        CMISParser.likePredicate_return likePredicate76 = null;

        CMISParser.nullPredicate_return nullPredicate77 = null;

        CMISParser.quantifiedComparisonPredicate_return quantifiedComparisonPredicate78 = null;

        CMISParser.quantifiedInPredicate_return quantifiedInPredicate79 = null;

        CMISParser.textSearchPredicate_return textSearchPredicate80 = null;

        CMISParser.folderPredicate_return folderPredicate81 = null;



        try {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:464:2: ( comparisonPredicate | inPredicate | likePredicate | nullPredicate | quantifiedComparisonPredicate | quantifiedInPredicate | textSearchPredicate | folderPredicate )
            int alt28=8;
            alt28 = dfa28.predict(input);
            switch (alt28) {
                case 1 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:464:4: comparisonPredicate
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_comparisonPredicate_in_predicate1313);
                    comparisonPredicate74=comparisonPredicate();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, comparisonPredicate74.getTree());

                    }
                    break;
                case 2 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:465:4: inPredicate
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_inPredicate_in_predicate1318);
                    inPredicate75=inPredicate();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, inPredicate75.getTree());

                    }
                    break;
                case 3 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:466:4: likePredicate
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_likePredicate_in_predicate1323);
                    likePredicate76=likePredicate();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, likePredicate76.getTree());

                    }
                    break;
                case 4 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:467:4: nullPredicate
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_nullPredicate_in_predicate1328);
                    nullPredicate77=nullPredicate();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, nullPredicate77.getTree());

                    }
                    break;
                case 5 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:468:10: quantifiedComparisonPredicate
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_quantifiedComparisonPredicate_in_predicate1339);
                    quantifiedComparisonPredicate78=quantifiedComparisonPredicate();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, quantifiedComparisonPredicate78.getTree());

                    }
                    break;
                case 6 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:469:4: quantifiedInPredicate
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_quantifiedInPredicate_in_predicate1344);
                    quantifiedInPredicate79=quantifiedInPredicate();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, quantifiedInPredicate79.getTree());

                    }
                    break;
                case 7 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:470:4: textSearchPredicate
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_textSearchPredicate_in_predicate1349);
                    textSearchPredicate80=textSearchPredicate();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, textSearchPredicate80.getTree());

                    }
                    break;
                case 8 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:471:4: folderPredicate
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_folderPredicate_in_predicate1354);
                    folderPredicate81=folderPredicate();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, folderPredicate81.getTree());

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
                throw new CMISQueryException(getErrorString(e), e);
            }
        finally {
        }
        return retval;
    }
    // $ANTLR end "predicate"

    public static class comparisonPredicate_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "comparisonPredicate"
    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:474:1: comparisonPredicate : valueExpression compOp literalOrParameterName -> ^( PRED_COMPARISON ANY valueExpression compOp literalOrParameterName ) ;
    public final CMISParser.comparisonPredicate_return comparisonPredicate() throws RecognitionException {
        CMISParser.comparisonPredicate_return retval = new CMISParser.comparisonPredicate_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        CMISParser.valueExpression_return valueExpression82 = null;

        CMISParser.compOp_return compOp83 = null;

        CMISParser.literalOrParameterName_return literalOrParameterName84 = null;


        RewriteRuleSubtreeStream stream_valueExpression=new RewriteRuleSubtreeStream(adaptor,"rule valueExpression");
        RewriteRuleSubtreeStream stream_compOp=new RewriteRuleSubtreeStream(adaptor,"rule compOp");
        RewriteRuleSubtreeStream stream_literalOrParameterName=new RewriteRuleSubtreeStream(adaptor,"rule literalOrParameterName");
        try {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:475:2: ( valueExpression compOp literalOrParameterName -> ^( PRED_COMPARISON ANY valueExpression compOp literalOrParameterName ) )
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:475:4: valueExpression compOp literalOrParameterName
            {
            pushFollow(FOLLOW_valueExpression_in_comparisonPredicate1366);
            valueExpression82=valueExpression();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_valueExpression.add(valueExpression82.getTree());
            pushFollow(FOLLOW_compOp_in_comparisonPredicate1368);
            compOp83=compOp();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_compOp.add(compOp83.getTree());
            pushFollow(FOLLOW_literalOrParameterName_in_comparisonPredicate1370);
            literalOrParameterName84=literalOrParameterName();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_literalOrParameterName.add(literalOrParameterName84.getTree());


            // AST REWRITE
            // elements: compOp, valueExpression, literalOrParameterName
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 476:3: -> ^( PRED_COMPARISON ANY valueExpression compOp literalOrParameterName )
            {
                // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:476:6: ^( PRED_COMPARISON ANY valueExpression compOp literalOrParameterName )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(PRED_COMPARISON, "PRED_COMPARISON"), root_1);

                adaptor.addChild(root_1, (Object)adaptor.create(ANY, "ANY"));
                adaptor.addChild(root_1, stream_valueExpression.nextTree());
                adaptor.addChild(root_1, stream_compOp.nextTree());
                adaptor.addChild(root_1, stream_literalOrParameterName.nextTree());

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
                throw new CMISQueryException(getErrorString(e), e);
            }
        finally {
        }
        return retval;
    }
    // $ANTLR end "comparisonPredicate"

    public static class compOp_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "compOp"
    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:479:1: compOp : ( EQUALS | NOTEQUALS | LESSTHAN | GREATERTHAN | LESSTHANOREQUALS | GREATERTHANOREQUALS );
    public final CMISParser.compOp_return compOp() throws RecognitionException {
        CMISParser.compOp_return retval = new CMISParser.compOp_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token set85=null;

        Object set85_tree=null;

        try {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:480:2: ( EQUALS | NOTEQUALS | LESSTHAN | GREATERTHAN | LESSTHANOREQUALS | GREATERTHANOREQUALS )
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:
            {
            root_0 = (Object)adaptor.nil();

            set85=(Token)input.LT(1);
            if ( input.LA(1)==EQUALS||(input.LA(1)>=NOTEQUALS && input.LA(1)<=GREATERTHANOREQUALS) ) {
                input.consume();
                if ( state.backtracking==0 ) adaptor.addChild(root_0, (Object)adaptor.create(set85));
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
                throw new CMISQueryException(getErrorString(e), e);
            }
        finally {
        }
        return retval;
    }
    // $ANTLR end "compOp"

    public static class literalOrParameterName_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "literalOrParameterName"
    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:488:1: literalOrParameterName : ( literal | {...}? => parameterName );
    public final CMISParser.literalOrParameterName_return literalOrParameterName() throws RecognitionException {
        CMISParser.literalOrParameterName_return retval = new CMISParser.literalOrParameterName_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        CMISParser.literal_return literal86 = null;

        CMISParser.parameterName_return parameterName87 = null;



        try {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:489:2: ( literal | {...}? => parameterName )
            int alt29=2;
            int LA29_0 = input.LA(1);

            if ( (LA29_0==QUOTED_STRING||(LA29_0>=FLOATING_POINT_LITERAL && LA29_0<=TIMESTAMP)) ) {
                alt29=1;
            }
            else if ( (LA29_0==COLON) && ((strict == false))) {
                alt29=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 29, 0, input);

                throw nvae;
            }
            switch (alt29) {
                case 1 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:489:4: literal
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_literal_in_literalOrParameterName1436);
                    literal86=literal();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, literal86.getTree());

                    }
                    break;
                case 2 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:490:4: {...}? => parameterName
                    {
                    root_0 = (Object)adaptor.nil();

                    if ( !((strict == false)) ) {
                        if (state.backtracking>0) {state.failed=true; return retval;}
                        throw new FailedPredicateException(input, "literalOrParameterName", "strict == false");
                    }
                    pushFollow(FOLLOW_parameterName_in_literalOrParameterName1444);
                    parameterName87=parameterName();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, parameterName87.getTree());

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
                throw new CMISQueryException(getErrorString(e), e);
            }
        finally {
        }
        return retval;
    }
    // $ANTLR end "literalOrParameterName"

    public static class literal_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "literal"
    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:493:1: literal : ( signedNumericLiteral | characterStringLiteral | booleanLiteral | datetimeLiteral );
    public final CMISParser.literal_return literal() throws RecognitionException {
        CMISParser.literal_return retval = new CMISParser.literal_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        CMISParser.signedNumericLiteral_return signedNumericLiteral88 = null;

        CMISParser.characterStringLiteral_return characterStringLiteral89 = null;

        CMISParser.booleanLiteral_return booleanLiteral90 = null;

        CMISParser.datetimeLiteral_return datetimeLiteral91 = null;



        try {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:494:2: ( signedNumericLiteral | characterStringLiteral | booleanLiteral | datetimeLiteral )
            int alt30=4;
            switch ( input.LA(1) ) {
            case FLOATING_POINT_LITERAL:
            case DECIMAL_INTEGER_LITERAL:
                {
                alt30=1;
                }
                break;
            case QUOTED_STRING:
                {
                alt30=2;
                }
                break;
            case TRUE:
            case FALSE:
                {
                alt30=3;
                }
                break;
            case TIMESTAMP:
                {
                alt30=4;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 30, 0, input);

                throw nvae;
            }

            switch (alt30) {
                case 1 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:494:4: signedNumericLiteral
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_signedNumericLiteral_in_literal1457);
                    signedNumericLiteral88=signedNumericLiteral();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, signedNumericLiteral88.getTree());

                    }
                    break;
                case 2 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:495:4: characterStringLiteral
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_characterStringLiteral_in_literal1462);
                    characterStringLiteral89=characterStringLiteral();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, characterStringLiteral89.getTree());

                    }
                    break;
                case 3 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:496:4: booleanLiteral
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_booleanLiteral_in_literal1467);
                    booleanLiteral90=booleanLiteral();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, booleanLiteral90.getTree());

                    }
                    break;
                case 4 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:497:4: datetimeLiteral
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_datetimeLiteral_in_literal1472);
                    datetimeLiteral91=datetimeLiteral();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, datetimeLiteral91.getTree());

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
                throw new CMISQueryException(getErrorString(e), e);
            }
        finally {
        }
        return retval;
    }
    // $ANTLR end "literal"

    public static class inPredicate_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "inPredicate"
    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:500:1: inPredicate : columnReference ( NOT )? IN LPAREN inValueList RPAREN -> ^( PRED_IN ANY columnReference inValueList ( NOT )? ) ;
    public final CMISParser.inPredicate_return inPredicate() throws RecognitionException {
        CMISParser.inPredicate_return retval = new CMISParser.inPredicate_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token NOT93=null;
        Token IN94=null;
        Token LPAREN95=null;
        Token RPAREN97=null;
        CMISParser.columnReference_return columnReference92 = null;

        CMISParser.inValueList_return inValueList96 = null;


        Object NOT93_tree=null;
        Object IN94_tree=null;
        Object LPAREN95_tree=null;
        Object RPAREN97_tree=null;
        RewriteRuleTokenStream stream_RPAREN=new RewriteRuleTokenStream(adaptor,"token RPAREN");
        RewriteRuleTokenStream stream_IN=new RewriteRuleTokenStream(adaptor,"token IN");
        RewriteRuleTokenStream stream_NOT=new RewriteRuleTokenStream(adaptor,"token NOT");
        RewriteRuleTokenStream stream_LPAREN=new RewriteRuleTokenStream(adaptor,"token LPAREN");
        RewriteRuleSubtreeStream stream_columnReference=new RewriteRuleSubtreeStream(adaptor,"rule columnReference");
        RewriteRuleSubtreeStream stream_inValueList=new RewriteRuleSubtreeStream(adaptor,"rule inValueList");
        try {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:501:2: ( columnReference ( NOT )? IN LPAREN inValueList RPAREN -> ^( PRED_IN ANY columnReference inValueList ( NOT )? ) )
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:501:4: columnReference ( NOT )? IN LPAREN inValueList RPAREN
            {
            pushFollow(FOLLOW_columnReference_in_inPredicate1484);
            columnReference92=columnReference();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_columnReference.add(columnReference92.getTree());
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:501:20: ( NOT )?
            int alt31=2;
            int LA31_0 = input.LA(1);

            if ( (LA31_0==NOT) ) {
                alt31=1;
            }
            switch (alt31) {
                case 1 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:501:20: NOT
                    {
                    NOT93=(Token)match(input,NOT,FOLLOW_NOT_in_inPredicate1486); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_NOT.add(NOT93);


                    }
                    break;

            }

            IN94=(Token)match(input,IN,FOLLOW_IN_in_inPredicate1489); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_IN.add(IN94);

            LPAREN95=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_inPredicate1491); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_LPAREN.add(LPAREN95);

            pushFollow(FOLLOW_inValueList_in_inPredicate1493);
            inValueList96=inValueList();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_inValueList.add(inValueList96.getTree());
            RPAREN97=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_inPredicate1495); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_RPAREN.add(RPAREN97);



            // AST REWRITE
            // elements: NOT, columnReference, inValueList
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 502:3: -> ^( PRED_IN ANY columnReference inValueList ( NOT )? )
            {
                // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:502:6: ^( PRED_IN ANY columnReference inValueList ( NOT )? )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(PRED_IN, "PRED_IN"), root_1);

                adaptor.addChild(root_1, (Object)adaptor.create(ANY, "ANY"));
                adaptor.addChild(root_1, stream_columnReference.nextTree());
                adaptor.addChild(root_1, stream_inValueList.nextTree());
                // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:502:48: ( NOT )?
                if ( stream_NOT.hasNext() ) {
                    adaptor.addChild(root_1, stream_NOT.nextNode());

                }
                stream_NOT.reset();

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
                throw new CMISQueryException(getErrorString(e), e);
            }
        finally {
        }
        return retval;
    }
    // $ANTLR end "inPredicate"

    public static class inValueList_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "inValueList"
    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:505:1: inValueList : literalOrParameterName ( COMMA literalOrParameterName )* -> ^( LIST ( literalOrParameterName )+ ) ;
    public final CMISParser.inValueList_return inValueList() throws RecognitionException {
        CMISParser.inValueList_return retval = new CMISParser.inValueList_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token COMMA99=null;
        CMISParser.literalOrParameterName_return literalOrParameterName98 = null;

        CMISParser.literalOrParameterName_return literalOrParameterName100 = null;


        Object COMMA99_tree=null;
        RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
        RewriteRuleSubtreeStream stream_literalOrParameterName=new RewriteRuleSubtreeStream(adaptor,"rule literalOrParameterName");
        try {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:506:2: ( literalOrParameterName ( COMMA literalOrParameterName )* -> ^( LIST ( literalOrParameterName )+ ) )
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:506:4: literalOrParameterName ( COMMA literalOrParameterName )*
            {
            pushFollow(FOLLOW_literalOrParameterName_in_inValueList1524);
            literalOrParameterName98=literalOrParameterName();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_literalOrParameterName.add(literalOrParameterName98.getTree());
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:506:27: ( COMMA literalOrParameterName )*
            loop32:
            do {
                int alt32=2;
                int LA32_0 = input.LA(1);

                if ( (LA32_0==COMMA) ) {
                    alt32=1;
                }


                switch (alt32) {
            	case 1 :
            	    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:506:28: COMMA literalOrParameterName
            	    {
            	    COMMA99=(Token)match(input,COMMA,FOLLOW_COMMA_in_inValueList1527); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_COMMA.add(COMMA99);

            	    pushFollow(FOLLOW_literalOrParameterName_in_inValueList1529);
            	    literalOrParameterName100=literalOrParameterName();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_literalOrParameterName.add(literalOrParameterName100.getTree());

            	    }
            	    break;

            	default :
            	    break loop32;
                }
            } while (true);



            // AST REWRITE
            // elements: literalOrParameterName
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 507:3: -> ^( LIST ( literalOrParameterName )+ )
            {
                // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:507:6: ^( LIST ( literalOrParameterName )+ )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(LIST, "LIST"), root_1);

                if ( !(stream_literalOrParameterName.hasNext()) ) {
                    throw new RewriteEarlyExitException();
                }
                while ( stream_literalOrParameterName.hasNext() ) {
                    adaptor.addChild(root_1, stream_literalOrParameterName.nextTree());

                }
                stream_literalOrParameterName.reset();

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
                throw new CMISQueryException(getErrorString(e), e);
            }
        finally {
        }
        return retval;
    }
    // $ANTLR end "inValueList"

    public static class likePredicate_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "likePredicate"
    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:510:1: likePredicate : columnReference ( NOT )? LIKE characterStringLiteral -> ^( PRED_LIKE columnReference characterStringLiteral ( NOT )? ) ;
    public final CMISParser.likePredicate_return likePredicate() throws RecognitionException {
        CMISParser.likePredicate_return retval = new CMISParser.likePredicate_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token NOT102=null;
        Token LIKE103=null;
        CMISParser.columnReference_return columnReference101 = null;

        CMISParser.characterStringLiteral_return characterStringLiteral104 = null;


        Object NOT102_tree=null;
        Object LIKE103_tree=null;
        RewriteRuleTokenStream stream_NOT=new RewriteRuleTokenStream(adaptor,"token NOT");
        RewriteRuleTokenStream stream_LIKE=new RewriteRuleTokenStream(adaptor,"token LIKE");
        RewriteRuleSubtreeStream stream_columnReference=new RewriteRuleSubtreeStream(adaptor,"rule columnReference");
        RewriteRuleSubtreeStream stream_characterStringLiteral=new RewriteRuleSubtreeStream(adaptor,"rule characterStringLiteral");
        try {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:511:2: ( columnReference ( NOT )? LIKE characterStringLiteral -> ^( PRED_LIKE columnReference characterStringLiteral ( NOT )? ) )
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:511:4: columnReference ( NOT )? LIKE characterStringLiteral
            {
            pushFollow(FOLLOW_columnReference_in_likePredicate1555);
            columnReference101=columnReference();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_columnReference.add(columnReference101.getTree());
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:511:20: ( NOT )?
            int alt33=2;
            int LA33_0 = input.LA(1);

            if ( (LA33_0==NOT) ) {
                alt33=1;
            }
            switch (alt33) {
                case 1 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:511:20: NOT
                    {
                    NOT102=(Token)match(input,NOT,FOLLOW_NOT_in_likePredicate1557); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_NOT.add(NOT102);


                    }
                    break;

            }

            LIKE103=(Token)match(input,LIKE,FOLLOW_LIKE_in_likePredicate1560); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_LIKE.add(LIKE103);

            pushFollow(FOLLOW_characterStringLiteral_in_likePredicate1562);
            characterStringLiteral104=characterStringLiteral();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_characterStringLiteral.add(characterStringLiteral104.getTree());


            // AST REWRITE
            // elements: columnReference, NOT, characterStringLiteral
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 512:3: -> ^( PRED_LIKE columnReference characterStringLiteral ( NOT )? )
            {
                // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:512:6: ^( PRED_LIKE columnReference characterStringLiteral ( NOT )? )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(PRED_LIKE, "PRED_LIKE"), root_1);

                adaptor.addChild(root_1, stream_columnReference.nextTree());
                adaptor.addChild(root_1, stream_characterStringLiteral.nextTree());
                // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:512:57: ( NOT )?
                if ( stream_NOT.hasNext() ) {
                    adaptor.addChild(root_1, stream_NOT.nextNode());

                }
                stream_NOT.reset();

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
                throw new CMISQueryException(getErrorString(e), e);
            }
        finally {
        }
        return retval;
    }
    // $ANTLR end "likePredicate"

    public static class nullPredicate_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "nullPredicate"
    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:515:1: nullPredicate : ( ( ( columnReference )=> columnReference | multiValuedColumnReference ) IS NULL -> ^( PRED_EXISTS columnReference NOT ) | ( ( columnReference )=> columnReference | multiValuedColumnReference ) IS NOT NULL -> ^( PRED_EXISTS columnReference ) );
    public final CMISParser.nullPredicate_return nullPredicate() throws RecognitionException {
        CMISParser.nullPredicate_return retval = new CMISParser.nullPredicate_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token IS107=null;
        Token NULL108=null;
        Token IS111=null;
        Token NOT112=null;
        Token NULL113=null;
        CMISParser.columnReference_return columnReference105 = null;

        CMISParser.multiValuedColumnReference_return multiValuedColumnReference106 = null;

        CMISParser.columnReference_return columnReference109 = null;

        CMISParser.multiValuedColumnReference_return multiValuedColumnReference110 = null;


        Object IS107_tree=null;
        Object NULL108_tree=null;
        Object IS111_tree=null;
        Object NOT112_tree=null;
        Object NULL113_tree=null;
        RewriteRuleTokenStream stream_NOT=new RewriteRuleTokenStream(adaptor,"token NOT");
        RewriteRuleTokenStream stream_IS=new RewriteRuleTokenStream(adaptor,"token IS");
        RewriteRuleTokenStream stream_NULL=new RewriteRuleTokenStream(adaptor,"token NULL");
        RewriteRuleSubtreeStream stream_columnReference=new RewriteRuleSubtreeStream(adaptor,"rule columnReference");
        RewriteRuleSubtreeStream stream_multiValuedColumnReference=new RewriteRuleSubtreeStream(adaptor,"rule multiValuedColumnReference");
        try {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:516:2: ( ( ( columnReference )=> columnReference | multiValuedColumnReference ) IS NULL -> ^( PRED_EXISTS columnReference NOT ) | ( ( columnReference )=> columnReference | multiValuedColumnReference ) IS NOT NULL -> ^( PRED_EXISTS columnReference ) )
            int alt36=2;
            alt36 = dfa36.predict(input);
            switch (alt36) {
                case 1 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:516:4: ( ( columnReference )=> columnReference | multiValuedColumnReference ) IS NULL
                    {
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:516:4: ( ( columnReference )=> columnReference | multiValuedColumnReference )
                    int alt34=2;
                    alt34 = dfa34.predict(input);
                    switch (alt34) {
                        case 1 :
                            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:516:6: ( columnReference )=> columnReference
                            {
                            pushFollow(FOLLOW_columnReference_in_nullPredicate1596);
                            columnReference105=columnReference();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_columnReference.add(columnReference105.getTree());

                            }
                            break;
                        case 2 :
                            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:516:44: multiValuedColumnReference
                            {
                            pushFollow(FOLLOW_multiValuedColumnReference_in_nullPredicate1600);
                            multiValuedColumnReference106=multiValuedColumnReference();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_multiValuedColumnReference.add(multiValuedColumnReference106.getTree());

                            }
                            break;

                    }

                    IS107=(Token)match(input,IS,FOLLOW_IS_in_nullPredicate1603); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_IS.add(IS107);

                    NULL108=(Token)match(input,NULL,FOLLOW_NULL_in_nullPredicate1605); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_NULL.add(NULL108);



                    // AST REWRITE
                    // elements: columnReference
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 517:3: -> ^( PRED_EXISTS columnReference NOT )
                    {
                        // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:517:6: ^( PRED_EXISTS columnReference NOT )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(PRED_EXISTS, "PRED_EXISTS"), root_1);

                        adaptor.addChild(root_1, stream_columnReference.nextTree());
                        adaptor.addChild(root_1, (Object)adaptor.create(NOT, "NOT"));

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:518:9: ( ( columnReference )=> columnReference | multiValuedColumnReference ) IS NOT NULL
                    {
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:518:9: ( ( columnReference )=> columnReference | multiValuedColumnReference )
                    int alt35=2;
                    alt35 = dfa35.predict(input);
                    switch (alt35) {
                        case 1 :
                            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:518:11: ( columnReference )=> columnReference
                            {
                            pushFollow(FOLLOW_columnReference_in_nullPredicate1634);
                            columnReference109=columnReference();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_columnReference.add(columnReference109.getTree());

                            }
                            break;
                        case 2 :
                            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:518:49: multiValuedColumnReference
                            {
                            pushFollow(FOLLOW_multiValuedColumnReference_in_nullPredicate1638);
                            multiValuedColumnReference110=multiValuedColumnReference();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_multiValuedColumnReference.add(multiValuedColumnReference110.getTree());

                            }
                            break;

                    }

                    IS111=(Token)match(input,IS,FOLLOW_IS_in_nullPredicate1641); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_IS.add(IS111);

                    NOT112=(Token)match(input,NOT,FOLLOW_NOT_in_nullPredicate1643); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_NOT.add(NOT112);

                    NULL113=(Token)match(input,NULL,FOLLOW_NULL_in_nullPredicate1645); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_NULL.add(NULL113);



                    // AST REWRITE
                    // elements: columnReference
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 519:9: -> ^( PRED_EXISTS columnReference )
                    {
                        // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:519:12: ^( PRED_EXISTS columnReference )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(PRED_EXISTS, "PRED_EXISTS"), root_1);

                        adaptor.addChild(root_1, stream_columnReference.nextTree());

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
                throw new CMISQueryException(getErrorString(e), e);
            }
        finally {
        }
        return retval;
    }
    // $ANTLR end "nullPredicate"

    public static class quantifiedComparisonPredicate_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "quantifiedComparisonPredicate"
    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:522:1: quantifiedComparisonPredicate : literalOrParameterName compOp ANY multiValuedColumnReference -> ^( PRED_COMPARISON ANY literalOrParameterName compOp multiValuedColumnReference ) ;
    public final CMISParser.quantifiedComparisonPredicate_return quantifiedComparisonPredicate() throws RecognitionException {
        CMISParser.quantifiedComparisonPredicate_return retval = new CMISParser.quantifiedComparisonPredicate_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token ANY116=null;
        CMISParser.literalOrParameterName_return literalOrParameterName114 = null;

        CMISParser.compOp_return compOp115 = null;

        CMISParser.multiValuedColumnReference_return multiValuedColumnReference117 = null;


        Object ANY116_tree=null;
        RewriteRuleTokenStream stream_ANY=new RewriteRuleTokenStream(adaptor,"token ANY");
        RewriteRuleSubtreeStream stream_compOp=new RewriteRuleSubtreeStream(adaptor,"rule compOp");
        RewriteRuleSubtreeStream stream_literalOrParameterName=new RewriteRuleSubtreeStream(adaptor,"rule literalOrParameterName");
        RewriteRuleSubtreeStream stream_multiValuedColumnReference=new RewriteRuleSubtreeStream(adaptor,"rule multiValuedColumnReference");
        try {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:523:2: ( literalOrParameterName compOp ANY multiValuedColumnReference -> ^( PRED_COMPARISON ANY literalOrParameterName compOp multiValuedColumnReference ) )
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:523:4: literalOrParameterName compOp ANY multiValuedColumnReference
            {
            pushFollow(FOLLOW_literalOrParameterName_in_quantifiedComparisonPredicate1673);
            literalOrParameterName114=literalOrParameterName();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_literalOrParameterName.add(literalOrParameterName114.getTree());
            pushFollow(FOLLOW_compOp_in_quantifiedComparisonPredicate1675);
            compOp115=compOp();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_compOp.add(compOp115.getTree());
            ANY116=(Token)match(input,ANY,FOLLOW_ANY_in_quantifiedComparisonPredicate1677); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_ANY.add(ANY116);

            pushFollow(FOLLOW_multiValuedColumnReference_in_quantifiedComparisonPredicate1679);
            multiValuedColumnReference117=multiValuedColumnReference();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_multiValuedColumnReference.add(multiValuedColumnReference117.getTree());


            // AST REWRITE
            // elements: multiValuedColumnReference, ANY, compOp, literalOrParameterName
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 524:2: -> ^( PRED_COMPARISON ANY literalOrParameterName compOp multiValuedColumnReference )
            {
                // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:524:5: ^( PRED_COMPARISON ANY literalOrParameterName compOp multiValuedColumnReference )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(PRED_COMPARISON, "PRED_COMPARISON"), root_1);

                adaptor.addChild(root_1, stream_ANY.nextNode());
                adaptor.addChild(root_1, stream_literalOrParameterName.nextTree());
                adaptor.addChild(root_1, stream_compOp.nextTree());
                adaptor.addChild(root_1, stream_multiValuedColumnReference.nextTree());

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
                throw new CMISQueryException(getErrorString(e), e);
            }
        finally {
        }
        return retval;
    }
    // $ANTLR end "quantifiedComparisonPredicate"

    public static class quantifiedInPredicate_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "quantifiedInPredicate"
    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:528:1: quantifiedInPredicate : ANY multiValuedColumnReference ( NOT )? IN LPAREN inValueList RPAREN -> ^( PRED_IN ANY multiValuedColumnReference inValueList ( NOT )? ) ;
    public final CMISParser.quantifiedInPredicate_return quantifiedInPredicate() throws RecognitionException {
        CMISParser.quantifiedInPredicate_return retval = new CMISParser.quantifiedInPredicate_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token ANY118=null;
        Token NOT120=null;
        Token IN121=null;
        Token LPAREN122=null;
        Token RPAREN124=null;
        CMISParser.multiValuedColumnReference_return multiValuedColumnReference119 = null;

        CMISParser.inValueList_return inValueList123 = null;


        Object ANY118_tree=null;
        Object NOT120_tree=null;
        Object IN121_tree=null;
        Object LPAREN122_tree=null;
        Object RPAREN124_tree=null;
        RewriteRuleTokenStream stream_RPAREN=new RewriteRuleTokenStream(adaptor,"token RPAREN");
        RewriteRuleTokenStream stream_ANY=new RewriteRuleTokenStream(adaptor,"token ANY");
        RewriteRuleTokenStream stream_IN=new RewriteRuleTokenStream(adaptor,"token IN");
        RewriteRuleTokenStream stream_NOT=new RewriteRuleTokenStream(adaptor,"token NOT");
        RewriteRuleTokenStream stream_LPAREN=new RewriteRuleTokenStream(adaptor,"token LPAREN");
        RewriteRuleSubtreeStream stream_multiValuedColumnReference=new RewriteRuleSubtreeStream(adaptor,"rule multiValuedColumnReference");
        RewriteRuleSubtreeStream stream_inValueList=new RewriteRuleSubtreeStream(adaptor,"rule inValueList");
        try {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:529:2: ( ANY multiValuedColumnReference ( NOT )? IN LPAREN inValueList RPAREN -> ^( PRED_IN ANY multiValuedColumnReference inValueList ( NOT )? ) )
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:529:4: ANY multiValuedColumnReference ( NOT )? IN LPAREN inValueList RPAREN
            {
            ANY118=(Token)match(input,ANY,FOLLOW_ANY_in_quantifiedInPredicate1708); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_ANY.add(ANY118);

            pushFollow(FOLLOW_multiValuedColumnReference_in_quantifiedInPredicate1710);
            multiValuedColumnReference119=multiValuedColumnReference();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_multiValuedColumnReference.add(multiValuedColumnReference119.getTree());
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:529:35: ( NOT )?
            int alt37=2;
            int LA37_0 = input.LA(1);

            if ( (LA37_0==NOT) ) {
                alt37=1;
            }
            switch (alt37) {
                case 1 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:529:35: NOT
                    {
                    NOT120=(Token)match(input,NOT,FOLLOW_NOT_in_quantifiedInPredicate1712); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_NOT.add(NOT120);


                    }
                    break;

            }

            IN121=(Token)match(input,IN,FOLLOW_IN_in_quantifiedInPredicate1715); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_IN.add(IN121);

            LPAREN122=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_quantifiedInPredicate1718); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_LPAREN.add(LPAREN122);

            pushFollow(FOLLOW_inValueList_in_quantifiedInPredicate1720);
            inValueList123=inValueList();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_inValueList.add(inValueList123.getTree());
            RPAREN124=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_quantifiedInPredicate1722); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_RPAREN.add(RPAREN124);



            // AST REWRITE
            // elements: NOT, multiValuedColumnReference, inValueList, ANY
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 530:3: -> ^( PRED_IN ANY multiValuedColumnReference inValueList ( NOT )? )
            {
                // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:530:6: ^( PRED_IN ANY multiValuedColumnReference inValueList ( NOT )? )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(PRED_IN, "PRED_IN"), root_1);

                adaptor.addChild(root_1, stream_ANY.nextNode());
                adaptor.addChild(root_1, stream_multiValuedColumnReference.nextTree());
                adaptor.addChild(root_1, stream_inValueList.nextTree());
                // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:530:59: ( NOT )?
                if ( stream_NOT.hasNext() ) {
                    adaptor.addChild(root_1, stream_NOT.nextNode());

                }
                stream_NOT.reset();

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
                throw new CMISQueryException(getErrorString(e), e);
            }
        finally {
        }
        return retval;
    }
    // $ANTLR end "quantifiedInPredicate"

    public static class textSearchPredicate_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "textSearchPredicate"
    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:533:1: textSearchPredicate : CONTAINS LPAREN ( qualifier COMMA )? textSearchExpression RPAREN -> ^( PRED_FTS textSearchExpression ( qualifier )? ) ;
    public final CMISParser.textSearchPredicate_return textSearchPredicate() throws RecognitionException {
        CMISParser.textSearchPredicate_return retval = new CMISParser.textSearchPredicate_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token CONTAINS125=null;
        Token LPAREN126=null;
        Token COMMA128=null;
        Token RPAREN130=null;
        CMISParser.qualifier_return qualifier127 = null;

        CMISParser.textSearchExpression_return textSearchExpression129 = null;


        Object CONTAINS125_tree=null;
        Object LPAREN126_tree=null;
        Object COMMA128_tree=null;
        Object RPAREN130_tree=null;
        RewriteRuleTokenStream stream_RPAREN=new RewriteRuleTokenStream(adaptor,"token RPAREN");
        RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
        RewriteRuleTokenStream stream_CONTAINS=new RewriteRuleTokenStream(adaptor,"token CONTAINS");
        RewriteRuleTokenStream stream_LPAREN=new RewriteRuleTokenStream(adaptor,"token LPAREN");
        RewriteRuleSubtreeStream stream_qualifier=new RewriteRuleSubtreeStream(adaptor,"rule qualifier");
        RewriteRuleSubtreeStream stream_textSearchExpression=new RewriteRuleSubtreeStream(adaptor,"rule textSearchExpression");
        try {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:534:2: ( CONTAINS LPAREN ( qualifier COMMA )? textSearchExpression RPAREN -> ^( PRED_FTS textSearchExpression ( qualifier )? ) )
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:534:4: CONTAINS LPAREN ( qualifier COMMA )? textSearchExpression RPAREN
            {
            CONTAINS125=(Token)match(input,CONTAINS,FOLLOW_CONTAINS_in_textSearchPredicate1751); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_CONTAINS.add(CONTAINS125);

            LPAREN126=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_textSearchPredicate1753); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_LPAREN.add(LPAREN126);

            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:534:20: ( qualifier COMMA )?
            int alt38=2;
            int LA38_0 = input.LA(1);

            if ( (LA38_0==ID) ) {
                alt38=1;
            }
            else if ( (LA38_0==DOUBLE_QUOTE) && ((strict == false))) {
                alt38=1;
            }
            switch (alt38) {
                case 1 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:534:21: qualifier COMMA
                    {
                    pushFollow(FOLLOW_qualifier_in_textSearchPredicate1756);
                    qualifier127=qualifier();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_qualifier.add(qualifier127.getTree());
                    COMMA128=(Token)match(input,COMMA,FOLLOW_COMMA_in_textSearchPredicate1758); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_COMMA.add(COMMA128);


                    }
                    break;

            }

            pushFollow(FOLLOW_textSearchExpression_in_textSearchPredicate1762);
            textSearchExpression129=textSearchExpression();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_textSearchExpression.add(textSearchExpression129.getTree());
            RPAREN130=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_textSearchPredicate1764); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_RPAREN.add(RPAREN130);



            // AST REWRITE
            // elements: qualifier, textSearchExpression
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 535:3: -> ^( PRED_FTS textSearchExpression ( qualifier )? )
            {
                // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:535:6: ^( PRED_FTS textSearchExpression ( qualifier )? )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(PRED_FTS, "PRED_FTS"), root_1);

                adaptor.addChild(root_1, stream_textSearchExpression.nextTree());
                // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:535:38: ( qualifier )?
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
                throw new CMISQueryException(getErrorString(e), e);
            }
        finally {
        }
        return retval;
    }
    // $ANTLR end "textSearchPredicate"

    public static class folderPredicate_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "folderPredicate"
    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:538:1: folderPredicate : ( IN_FOLDER folderPredicateArgs -> ^( PRED_CHILD folderPredicateArgs ) | IN_TREE folderPredicateArgs -> ^( PRED_DESCENDANT folderPredicateArgs ) );
    public final CMISParser.folderPredicate_return folderPredicate() throws RecognitionException {
        CMISParser.folderPredicate_return retval = new CMISParser.folderPredicate_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token IN_FOLDER131=null;
        Token IN_TREE133=null;
        CMISParser.folderPredicateArgs_return folderPredicateArgs132 = null;

        CMISParser.folderPredicateArgs_return folderPredicateArgs134 = null;


        Object IN_FOLDER131_tree=null;
        Object IN_TREE133_tree=null;
        RewriteRuleTokenStream stream_IN_TREE=new RewriteRuleTokenStream(adaptor,"token IN_TREE");
        RewriteRuleTokenStream stream_IN_FOLDER=new RewriteRuleTokenStream(adaptor,"token IN_FOLDER");
        RewriteRuleSubtreeStream stream_folderPredicateArgs=new RewriteRuleSubtreeStream(adaptor,"rule folderPredicateArgs");
        try {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:539:2: ( IN_FOLDER folderPredicateArgs -> ^( PRED_CHILD folderPredicateArgs ) | IN_TREE folderPredicateArgs -> ^( PRED_DESCENDANT folderPredicateArgs ) )
            int alt39=2;
            int LA39_0 = input.LA(1);

            if ( (LA39_0==IN_FOLDER) ) {
                alt39=1;
            }
            else if ( (LA39_0==IN_TREE) ) {
                alt39=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 39, 0, input);

                throw nvae;
            }
            switch (alt39) {
                case 1 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:539:4: IN_FOLDER folderPredicateArgs
                    {
                    IN_FOLDER131=(Token)match(input,IN_FOLDER,FOLLOW_IN_FOLDER_in_folderPredicate1789); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_IN_FOLDER.add(IN_FOLDER131);

                    pushFollow(FOLLOW_folderPredicateArgs_in_folderPredicate1792);
                    folderPredicateArgs132=folderPredicateArgs();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_folderPredicateArgs.add(folderPredicateArgs132.getTree());


                    // AST REWRITE
                    // elements: folderPredicateArgs
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 540:3: -> ^( PRED_CHILD folderPredicateArgs )
                    {
                        // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:540:6: ^( PRED_CHILD folderPredicateArgs )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(PRED_CHILD, "PRED_CHILD"), root_1);

                        adaptor.addChild(root_1, stream_folderPredicateArgs.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:541:10: IN_TREE folderPredicateArgs
                    {
                    IN_TREE133=(Token)match(input,IN_TREE,FOLLOW_IN_TREE_in_folderPredicate1813); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_IN_TREE.add(IN_TREE133);

                    pushFollow(FOLLOW_folderPredicateArgs_in_folderPredicate1815);
                    folderPredicateArgs134=folderPredicateArgs();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_folderPredicateArgs.add(folderPredicateArgs134.getTree());


                    // AST REWRITE
                    // elements: folderPredicateArgs
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 542:3: -> ^( PRED_DESCENDANT folderPredicateArgs )
                    {
                        // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:542:6: ^( PRED_DESCENDANT folderPredicateArgs )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(PRED_DESCENDANT, "PRED_DESCENDANT"), root_1);

                        adaptor.addChild(root_1, stream_folderPredicateArgs.nextTree());

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
                throw new CMISQueryException(getErrorString(e), e);
            }
        finally {
        }
        return retval;
    }
    // $ANTLR end "folderPredicate"

    public static class folderPredicateArgs_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "folderPredicateArgs"
    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:545:1: folderPredicateArgs : LPAREN ( qualifier COMMA )? folderId RPAREN -> folderId ( qualifier )? ;
    public final CMISParser.folderPredicateArgs_return folderPredicateArgs() throws RecognitionException {
        CMISParser.folderPredicateArgs_return retval = new CMISParser.folderPredicateArgs_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token LPAREN135=null;
        Token COMMA137=null;
        Token RPAREN139=null;
        CMISParser.qualifier_return qualifier136 = null;

        CMISParser.folderId_return folderId138 = null;


        Object LPAREN135_tree=null;
        Object COMMA137_tree=null;
        Object RPAREN139_tree=null;
        RewriteRuleTokenStream stream_RPAREN=new RewriteRuleTokenStream(adaptor,"token RPAREN");
        RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
        RewriteRuleTokenStream stream_LPAREN=new RewriteRuleTokenStream(adaptor,"token LPAREN");
        RewriteRuleSubtreeStream stream_qualifier=new RewriteRuleSubtreeStream(adaptor,"rule qualifier");
        RewriteRuleSubtreeStream stream_folderId=new RewriteRuleSubtreeStream(adaptor,"rule folderId");
        try {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:546:2: ( LPAREN ( qualifier COMMA )? folderId RPAREN -> folderId ( qualifier )? )
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:546:4: LPAREN ( qualifier COMMA )? folderId RPAREN
            {
            LPAREN135=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_folderPredicateArgs1837); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_LPAREN.add(LPAREN135);

            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:546:11: ( qualifier COMMA )?
            int alt40=2;
            int LA40_0 = input.LA(1);

            if ( (LA40_0==ID) ) {
                alt40=1;
            }
            else if ( (LA40_0==DOUBLE_QUOTE) && ((strict == false))) {
                alt40=1;
            }
            switch (alt40) {
                case 1 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:546:12: qualifier COMMA
                    {
                    pushFollow(FOLLOW_qualifier_in_folderPredicateArgs1840);
                    qualifier136=qualifier();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_qualifier.add(qualifier136.getTree());
                    COMMA137=(Token)match(input,COMMA,FOLLOW_COMMA_in_folderPredicateArgs1842); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_COMMA.add(COMMA137);


                    }
                    break;

            }

            pushFollow(FOLLOW_folderId_in_folderPredicateArgs1846);
            folderId138=folderId();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_folderId.add(folderId138.getTree());
            RPAREN139=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_folderPredicateArgs1848); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_RPAREN.add(RPAREN139);



            // AST REWRITE
            // elements: folderId, qualifier
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 547:3: -> folderId ( qualifier )?
            {
                adaptor.addChild(root_0, stream_folderId.nextTree());
                // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:547:15: ( qualifier )?
                if ( stream_qualifier.hasNext() ) {
                    adaptor.addChild(root_0, stream_qualifier.nextTree());

                }
                stream_qualifier.reset();

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
                throw new CMISQueryException(getErrorString(e), e);
            }
        finally {
        }
        return retval;
    }
    // $ANTLR end "folderPredicateArgs"

    public static class orderByClause_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "orderByClause"
    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:550:1: orderByClause : ORDER BY sortSpecification ( COMMA sortSpecification )* -> ^( ORDER ( sortSpecification )+ ) ;
    public final CMISParser.orderByClause_return orderByClause() throws RecognitionException {
        CMISParser.orderByClause_return retval = new CMISParser.orderByClause_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token ORDER140=null;
        Token BY141=null;
        Token COMMA143=null;
        CMISParser.sortSpecification_return sortSpecification142 = null;

        CMISParser.sortSpecification_return sortSpecification144 = null;


        Object ORDER140_tree=null;
        Object BY141_tree=null;
        Object COMMA143_tree=null;
        RewriteRuleTokenStream stream_BY=new RewriteRuleTokenStream(adaptor,"token BY");
        RewriteRuleTokenStream stream_ORDER=new RewriteRuleTokenStream(adaptor,"token ORDER");
        RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
        RewriteRuleSubtreeStream stream_sortSpecification=new RewriteRuleSubtreeStream(adaptor,"rule sortSpecification");
            paraphrases.push("in order by"); 
        try {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:553:2: ( ORDER BY sortSpecification ( COMMA sortSpecification )* -> ^( ORDER ( sortSpecification )+ ) )
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:553:4: ORDER BY sortSpecification ( COMMA sortSpecification )*
            {
            ORDER140=(Token)match(input,ORDER,FOLLOW_ORDER_in_orderByClause1887); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_ORDER.add(ORDER140);

            BY141=(Token)match(input,BY,FOLLOW_BY_in_orderByClause1889); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_BY.add(BY141);

            pushFollow(FOLLOW_sortSpecification_in_orderByClause1891);
            sortSpecification142=sortSpecification();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_sortSpecification.add(sortSpecification142.getTree());
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:553:31: ( COMMA sortSpecification )*
            loop41:
            do {
                int alt41=2;
                int LA41_0 = input.LA(1);

                if ( (LA41_0==COMMA) ) {
                    alt41=1;
                }


                switch (alt41) {
            	case 1 :
            	    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:553:33: COMMA sortSpecification
            	    {
            	    COMMA143=(Token)match(input,COMMA,FOLLOW_COMMA_in_orderByClause1895); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_COMMA.add(COMMA143);

            	    pushFollow(FOLLOW_sortSpecification_in_orderByClause1897);
            	    sortSpecification144=sortSpecification();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_sortSpecification.add(sortSpecification144.getTree());

            	    }
            	    break;

            	default :
            	    break loop41;
                }
            } while (true);



            // AST REWRITE
            // elements: sortSpecification, ORDER
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 554:3: -> ^( ORDER ( sortSpecification )+ )
            {
                // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:554:6: ^( ORDER ( sortSpecification )+ )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot(stream_ORDER.nextNode(), root_1);

                if ( !(stream_sortSpecification.hasNext()) ) {
                    throw new RewriteEarlyExitException();
                }
                while ( stream_sortSpecification.hasNext() ) {
                    adaptor.addChild(root_1, stream_sortSpecification.nextTree());

                }
                stream_sortSpecification.reset();

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
            if ( state.backtracking==0 ) {
                  paraphrases.pop(); 
            }
        }

            catch(RecognitionException e)
            {
                throw new CMISQueryException(getErrorString(e), e);
            }
        finally {
        }
        return retval;
    }
    // $ANTLR end "orderByClause"

    public static class sortSpecification_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "sortSpecification"
    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:557:1: sortSpecification : ( columnReference -> ^( SORT_SPECIFICATION columnReference ASC ) | columnReference (by= ASC | by= DESC ) -> ^( SORT_SPECIFICATION columnReference $by) );
    public final CMISParser.sortSpecification_return sortSpecification() throws RecognitionException {
        CMISParser.sortSpecification_return retval = new CMISParser.sortSpecification_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token by=null;
        CMISParser.columnReference_return columnReference145 = null;

        CMISParser.columnReference_return columnReference146 = null;


        Object by_tree=null;
        RewriteRuleTokenStream stream_ASC=new RewriteRuleTokenStream(adaptor,"token ASC");
        RewriteRuleTokenStream stream_DESC=new RewriteRuleTokenStream(adaptor,"token DESC");
        RewriteRuleSubtreeStream stream_columnReference=new RewriteRuleSubtreeStream(adaptor,"rule columnReference");
        try {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:558:2: ( columnReference -> ^( SORT_SPECIFICATION columnReference ASC ) | columnReference (by= ASC | by= DESC ) -> ^( SORT_SPECIFICATION columnReference $by) )
            int alt43=2;
            alt43 = dfa43.predict(input);
            switch (alt43) {
                case 1 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:558:4: columnReference
                    {
                    pushFollow(FOLLOW_columnReference_in_sortSpecification1923);
                    columnReference145=columnReference();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_columnReference.add(columnReference145.getTree());


                    // AST REWRITE
                    // elements: columnReference
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 559:3: -> ^( SORT_SPECIFICATION columnReference ASC )
                    {
                        // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:559:6: ^( SORT_SPECIFICATION columnReference ASC )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(SORT_SPECIFICATION, "SORT_SPECIFICATION"), root_1);

                        adaptor.addChild(root_1, stream_columnReference.nextTree());
                        adaptor.addChild(root_1, (Object)adaptor.create(ASC, "ASC"));

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:560:4: columnReference (by= ASC | by= DESC )
                    {
                    pushFollow(FOLLOW_columnReference_in_sortSpecification1941);
                    columnReference146=columnReference();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_columnReference.add(columnReference146.getTree());
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:560:20: (by= ASC | by= DESC )
                    int alt42=2;
                    int LA42_0 = input.LA(1);

                    if ( (LA42_0==ASC) ) {
                        alt42=1;
                    }
                    else if ( (LA42_0==DESC) ) {
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
                            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:560:22: by= ASC
                            {
                            by=(Token)match(input,ASC,FOLLOW_ASC_in_sortSpecification1947); if (state.failed) return retval; 
                            if ( state.backtracking==0 ) stream_ASC.add(by);


                            }
                            break;
                        case 2 :
                            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:560:31: by= DESC
                            {
                            by=(Token)match(input,DESC,FOLLOW_DESC_in_sortSpecification1953); if (state.failed) return retval; 
                            if ( state.backtracking==0 ) stream_DESC.add(by);


                            }
                            break;

                    }



                    // AST REWRITE
                    // elements: columnReference, by
                    // token labels: by
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleTokenStream stream_by=new RewriteRuleTokenStream(adaptor,"token by",by);
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 561:3: -> ^( SORT_SPECIFICATION columnReference $by)
                    {
                        // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:561:6: ^( SORT_SPECIFICATION columnReference $by)
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(SORT_SPECIFICATION, "SORT_SPECIFICATION"), root_1);

                        adaptor.addChild(root_1, stream_columnReference.nextTree());
                        adaptor.addChild(root_1, stream_by.nextNode());

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
                throw new CMISQueryException(getErrorString(e), e);
            }
        finally {
        }
        return retval;
    }
    // $ANTLR end "sortSpecification"

    public static class correlationName_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "correlationName"
    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:564:1: correlationName : identifier ;
    public final CMISParser.correlationName_return correlationName() throws RecognitionException {
        CMISParser.correlationName_return retval = new CMISParser.correlationName_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        CMISParser.identifier_return identifier147 = null;



        try {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:565:2: ( identifier )
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:565:4: identifier
            {
            root_0 = (Object)adaptor.nil();

            pushFollow(FOLLOW_identifier_in_correlationName1980);
            identifier147=identifier();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, identifier147.getTree());

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

            catch(RecognitionException e)
            {
                throw new CMISQueryException(getErrorString(e), e);
            }
        finally {
        }
        return retval;
    }
    // $ANTLR end "correlationName"

    public static class tableName_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "tableName"
    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:572:1: tableName : identifier -> identifier ;
    public final CMISParser.tableName_return tableName() throws RecognitionException {
        CMISParser.tableName_return retval = new CMISParser.tableName_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        CMISParser.identifier_return identifier148 = null;


        RewriteRuleSubtreeStream stream_identifier=new RewriteRuleSubtreeStream(adaptor,"rule identifier");
        try {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:573:2: ( identifier -> identifier )
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:573:4: identifier
            {
            pushFollow(FOLLOW_identifier_in_tableName1994);
            identifier148=identifier();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_identifier.add(identifier148.getTree());


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
            // 574:3: -> identifier
            {
                adaptor.addChild(root_0, stream_identifier.nextTree());

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
                throw new CMISQueryException(getErrorString(e), e);
            }
        finally {
        }
        return retval;
    }
    // $ANTLR end "tableName"

    public static class columnName_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "columnName"
    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:577:1: columnName : identifier -> identifier ;
    public final CMISParser.columnName_return columnName() throws RecognitionException {
        CMISParser.columnName_return retval = new CMISParser.columnName_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        CMISParser.identifier_return identifier149 = null;


        RewriteRuleSubtreeStream stream_identifier=new RewriteRuleSubtreeStream(adaptor,"rule identifier");
        try {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:578:2: ( identifier -> identifier )
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:578:4: identifier
            {
            pushFollow(FOLLOW_identifier_in_columnName2012);
            identifier149=identifier();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_identifier.add(identifier149.getTree());


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
            // 579:3: -> identifier
            {
                adaptor.addChild(root_0, stream_identifier.nextTree());

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
                throw new CMISQueryException(getErrorString(e), e);
            }
        finally {
        }
        return retval;
    }
    // $ANTLR end "columnName"

    public static class multiValuedColumnName_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "multiValuedColumnName"
    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:582:1: multiValuedColumnName : identifier -> identifier ;
    public final CMISParser.multiValuedColumnName_return multiValuedColumnName() throws RecognitionException {
        CMISParser.multiValuedColumnName_return retval = new CMISParser.multiValuedColumnName_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        CMISParser.identifier_return identifier150 = null;


        RewriteRuleSubtreeStream stream_identifier=new RewriteRuleSubtreeStream(adaptor,"rule identifier");
        try {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:583:2: ( identifier -> identifier )
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:583:4: identifier
            {
            pushFollow(FOLLOW_identifier_in_multiValuedColumnName2031);
            identifier150=identifier();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_identifier.add(identifier150.getTree());


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
            // 584:3: -> identifier
            {
                adaptor.addChild(root_0, stream_identifier.nextTree());

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
                throw new CMISQueryException(getErrorString(e), e);
            }
        finally {
        }
        return retval;
    }
    // $ANTLR end "multiValuedColumnName"

    public static class parameterName_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "parameterName"
    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:587:1: parameterName : COLON identifier -> ^( PARAMETER identifier ) ;
    public final CMISParser.parameterName_return parameterName() throws RecognitionException {
        CMISParser.parameterName_return retval = new CMISParser.parameterName_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token COLON151=null;
        CMISParser.identifier_return identifier152 = null;


        Object COLON151_tree=null;
        RewriteRuleTokenStream stream_COLON=new RewriteRuleTokenStream(adaptor,"token COLON");
        RewriteRuleSubtreeStream stream_identifier=new RewriteRuleSubtreeStream(adaptor,"rule identifier");
        try {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:588:2: ( COLON identifier -> ^( PARAMETER identifier ) )
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:588:4: COLON identifier
            {
            COLON151=(Token)match(input,COLON,FOLLOW_COLON_in_parameterName2049); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_COLON.add(COLON151);

            pushFollow(FOLLOW_identifier_in_parameterName2051);
            identifier152=identifier();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_identifier.add(identifier152.getTree());


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
            // 589:3: -> ^( PARAMETER identifier )
            {
                // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:589:6: ^( PARAMETER identifier )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(PARAMETER, "PARAMETER"), root_1);

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
                throw new CMISQueryException(getErrorString(e), e);
            }
        finally {
        }
        return retval;
    }
    // $ANTLR end "parameterName"

    public static class folderId_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "folderId"
    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:592:1: folderId : characterStringLiteral -> characterStringLiteral ;
    public final CMISParser.folderId_return folderId() throws RecognitionException {
        CMISParser.folderId_return retval = new CMISParser.folderId_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        CMISParser.characterStringLiteral_return characterStringLiteral153 = null;


        RewriteRuleSubtreeStream stream_characterStringLiteral=new RewriteRuleSubtreeStream(adaptor,"rule characterStringLiteral");
        try {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:593:3: ( characterStringLiteral -> characterStringLiteral )
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:593:5: characterStringLiteral
            {
            pushFollow(FOLLOW_characterStringLiteral_in_folderId2074);
            characterStringLiteral153=characterStringLiteral();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_characterStringLiteral.add(characterStringLiteral153.getTree());


            // AST REWRITE
            // elements: characterStringLiteral
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 594:4: -> characterStringLiteral
            {
                adaptor.addChild(root_0, stream_characterStringLiteral.nextTree());

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
                throw new CMISQueryException(getErrorString(e), e);
            }
        finally {
        }
        return retval;
    }
    // $ANTLR end "folderId"

    public static class textSearchExpression_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "textSearchExpression"
    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:597:1: textSearchExpression : QUOTED_STRING ;
    public final CMISParser.textSearchExpression_return textSearchExpression() throws RecognitionException {
        CMISParser.textSearchExpression_return retval = new CMISParser.textSearchExpression_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token QUOTED_STRING154=null;

        Object QUOTED_STRING154_tree=null;

        try {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:598:2: ( QUOTED_STRING )
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:598:4: QUOTED_STRING
            {
            root_0 = (Object)adaptor.nil();

            QUOTED_STRING154=(Token)match(input,QUOTED_STRING,FOLLOW_QUOTED_STRING_in_textSearchExpression2095); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            QUOTED_STRING154_tree = (Object)adaptor.create(QUOTED_STRING154);
            adaptor.addChild(root_0, QUOTED_STRING154_tree);
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
                throw new CMISQueryException(getErrorString(e), e);
            }
        finally {
        }
        return retval;
    }
    // $ANTLR end "textSearchExpression"

    public static class identifier_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "identifier"
    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:601:1: identifier : ( ID -> ID | {...}? => DOUBLE_QUOTE keyWordOrId DOUBLE_QUOTE -> ^( keyWordOrId ) );
    public final CMISParser.identifier_return identifier() throws RecognitionException {
        CMISParser.identifier_return retval = new CMISParser.identifier_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token ID155=null;
        Token DOUBLE_QUOTE156=null;
        Token DOUBLE_QUOTE158=null;
        CMISParser.keyWordOrId_return keyWordOrId157 = null;


        Object ID155_tree=null;
        Object DOUBLE_QUOTE156_tree=null;
        Object DOUBLE_QUOTE158_tree=null;
        RewriteRuleTokenStream stream_ID=new RewriteRuleTokenStream(adaptor,"token ID");
        RewriteRuleTokenStream stream_DOUBLE_QUOTE=new RewriteRuleTokenStream(adaptor,"token DOUBLE_QUOTE");
        RewriteRuleSubtreeStream stream_keyWordOrId=new RewriteRuleSubtreeStream(adaptor,"rule keyWordOrId");
        try {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:602:2: ( ID -> ID | {...}? => DOUBLE_QUOTE keyWordOrId DOUBLE_QUOTE -> ^( keyWordOrId ) )
            int alt44=2;
            int LA44_0 = input.LA(1);

            if ( (LA44_0==ID) ) {
                alt44=1;
            }
            else if ( (LA44_0==DOUBLE_QUOTE) && ((strict == false))) {
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
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:602:4: ID
                    {
                    ID155=(Token)match(input,ID,FOLLOW_ID_in_identifier2107); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_ID.add(ID155);



                    // AST REWRITE
                    // elements: ID
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 603:3: -> ID
                    {
                        adaptor.addChild(root_0, stream_ID.nextNode());

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:604:4: {...}? => DOUBLE_QUOTE keyWordOrId DOUBLE_QUOTE
                    {
                    if ( !((strict == false)) ) {
                        if (state.backtracking>0) {state.failed=true; return retval;}
                        throw new FailedPredicateException(input, "identifier", "strict == false");
                    }
                    DOUBLE_QUOTE156=(Token)match(input,DOUBLE_QUOTE,FOLLOW_DOUBLE_QUOTE_in_identifier2122); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_DOUBLE_QUOTE.add(DOUBLE_QUOTE156);

                    pushFollow(FOLLOW_keyWordOrId_in_identifier2124);
                    keyWordOrId157=keyWordOrId();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_keyWordOrId.add(keyWordOrId157.getTree());
                    DOUBLE_QUOTE158=(Token)match(input,DOUBLE_QUOTE,FOLLOW_DOUBLE_QUOTE_in_identifier2126); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_DOUBLE_QUOTE.add(DOUBLE_QUOTE158);



                    // AST REWRITE
                    // elements: keyWordOrId
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 605:3: -> ^( keyWordOrId )
                    {
                        // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:605:6: ^( keyWordOrId )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot(stream_keyWordOrId.nextNode(), root_1);

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
                throw new CMISQueryException(getErrorString(e), e);
            }
        finally {
        }
        return retval;
    }
    // $ANTLR end "identifier"

    public static class signedNumericLiteral_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "signedNumericLiteral"
    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:608:1: signedNumericLiteral : ( FLOATING_POINT_LITERAL -> ^( NUMERIC_LITERAL FLOATING_POINT_LITERAL ) | integerLiteral -> integerLiteral );
    public final CMISParser.signedNumericLiteral_return signedNumericLiteral() throws RecognitionException {
        CMISParser.signedNumericLiteral_return retval = new CMISParser.signedNumericLiteral_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token FLOATING_POINT_LITERAL159=null;
        CMISParser.integerLiteral_return integerLiteral160 = null;


        Object FLOATING_POINT_LITERAL159_tree=null;
        RewriteRuleTokenStream stream_FLOATING_POINT_LITERAL=new RewriteRuleTokenStream(adaptor,"token FLOATING_POINT_LITERAL");
        RewriteRuleSubtreeStream stream_integerLiteral=new RewriteRuleSubtreeStream(adaptor,"rule integerLiteral");
        try {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:609:2: ( FLOATING_POINT_LITERAL -> ^( NUMERIC_LITERAL FLOATING_POINT_LITERAL ) | integerLiteral -> integerLiteral )
            int alt45=2;
            int LA45_0 = input.LA(1);

            if ( (LA45_0==FLOATING_POINT_LITERAL) ) {
                alt45=1;
            }
            else if ( (LA45_0==DECIMAL_INTEGER_LITERAL) ) {
                alt45=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 45, 0, input);

                throw nvae;
            }
            switch (alt45) {
                case 1 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:609:4: FLOATING_POINT_LITERAL
                    {
                    FLOATING_POINT_LITERAL159=(Token)match(input,FLOATING_POINT_LITERAL,FOLLOW_FLOATING_POINT_LITERAL_in_signedNumericLiteral2146); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_FLOATING_POINT_LITERAL.add(FLOATING_POINT_LITERAL159);



                    // AST REWRITE
                    // elements: FLOATING_POINT_LITERAL
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 610:3: -> ^( NUMERIC_LITERAL FLOATING_POINT_LITERAL )
                    {
                        // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:610:6: ^( NUMERIC_LITERAL FLOATING_POINT_LITERAL )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(NUMERIC_LITERAL, "NUMERIC_LITERAL"), root_1);

                        adaptor.addChild(root_1, stream_FLOATING_POINT_LITERAL.nextNode());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:611:4: integerLiteral
                    {
                    pushFollow(FOLLOW_integerLiteral_in_signedNumericLiteral2161);
                    integerLiteral160=integerLiteral();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_integerLiteral.add(integerLiteral160.getTree());


                    // AST REWRITE
                    // elements: integerLiteral
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 612:3: -> integerLiteral
                    {
                        adaptor.addChild(root_0, stream_integerLiteral.nextTree());

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
                throw new CMISQueryException(getErrorString(e), e);
            }
        finally {
        }
        return retval;
    }
    // $ANTLR end "signedNumericLiteral"

    public static class integerLiteral_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "integerLiteral"
    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:615:1: integerLiteral : DECIMAL_INTEGER_LITERAL -> ^( NUMERIC_LITERAL DECIMAL_INTEGER_LITERAL ) ;
    public final CMISParser.integerLiteral_return integerLiteral() throws RecognitionException {
        CMISParser.integerLiteral_return retval = new CMISParser.integerLiteral_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token DECIMAL_INTEGER_LITERAL161=null;

        Object DECIMAL_INTEGER_LITERAL161_tree=null;
        RewriteRuleTokenStream stream_DECIMAL_INTEGER_LITERAL=new RewriteRuleTokenStream(adaptor,"token DECIMAL_INTEGER_LITERAL");

        try {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:616:2: ( DECIMAL_INTEGER_LITERAL -> ^( NUMERIC_LITERAL DECIMAL_INTEGER_LITERAL ) )
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:616:4: DECIMAL_INTEGER_LITERAL
            {
            DECIMAL_INTEGER_LITERAL161=(Token)match(input,DECIMAL_INTEGER_LITERAL,FOLLOW_DECIMAL_INTEGER_LITERAL_in_integerLiteral2180); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_DECIMAL_INTEGER_LITERAL.add(DECIMAL_INTEGER_LITERAL161);



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
            // 617:3: -> ^( NUMERIC_LITERAL DECIMAL_INTEGER_LITERAL )
            {
                // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:617:6: ^( NUMERIC_LITERAL DECIMAL_INTEGER_LITERAL )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(NUMERIC_LITERAL, "NUMERIC_LITERAL"), root_1);

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
                throw new CMISQueryException(getErrorString(e), e);
            }
        finally {
        }
        return retval;
    }
    // $ANTLR end "integerLiteral"

    public static class booleanLiteral_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "booleanLiteral"
    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:620:1: booleanLiteral : ( TRUE -> ^( BOOLEAN_LITERAL TRUE ) | FALSE -> ^( BOOLEAN_LITERAL FALSE ) );
    public final CMISParser.booleanLiteral_return booleanLiteral() throws RecognitionException {
        CMISParser.booleanLiteral_return retval = new CMISParser.booleanLiteral_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token TRUE162=null;
        Token FALSE163=null;

        Object TRUE162_tree=null;
        Object FALSE163_tree=null;
        RewriteRuleTokenStream stream_FALSE=new RewriteRuleTokenStream(adaptor,"token FALSE");
        RewriteRuleTokenStream stream_TRUE=new RewriteRuleTokenStream(adaptor,"token TRUE");

        try {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:621:3: ( TRUE -> ^( BOOLEAN_LITERAL TRUE ) | FALSE -> ^( BOOLEAN_LITERAL FALSE ) )
            int alt46=2;
            int LA46_0 = input.LA(1);

            if ( (LA46_0==TRUE) ) {
                alt46=1;
            }
            else if ( (LA46_0==FALSE) ) {
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
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:621:5: TRUE
                    {
                    TRUE162=(Token)match(input,TRUE,FOLLOW_TRUE_in_booleanLiteral2204); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_TRUE.add(TRUE162);



                    // AST REWRITE
                    // elements: TRUE
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 622:3: -> ^( BOOLEAN_LITERAL TRUE )
                    {
                        // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:622:7: ^( BOOLEAN_LITERAL TRUE )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(BOOLEAN_LITERAL, "BOOLEAN_LITERAL"), root_1);

                        adaptor.addChild(root_1, stream_TRUE.nextNode());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:623:5: FALSE
                    {
                    FALSE163=(Token)match(input,FALSE,FOLLOW_FALSE_in_booleanLiteral2222); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_FALSE.add(FALSE163);



                    // AST REWRITE
                    // elements: FALSE
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 624:3: -> ^( BOOLEAN_LITERAL FALSE )
                    {
                        // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:624:7: ^( BOOLEAN_LITERAL FALSE )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(BOOLEAN_LITERAL, "BOOLEAN_LITERAL"), root_1);

                        adaptor.addChild(root_1, stream_FALSE.nextNode());

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
                throw new CMISQueryException(getErrorString(e), e);
            }
        finally {
        }
        return retval;
    }
    // $ANTLR end "booleanLiteral"

    public static class datetimeLiteral_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "datetimeLiteral"
    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:627:1: datetimeLiteral : TIMESTAMP QUOTED_STRING -> ^( DATETIME_LITERAL QUOTED_STRING ) ;
    public final CMISParser.datetimeLiteral_return datetimeLiteral() throws RecognitionException {
        CMISParser.datetimeLiteral_return retval = new CMISParser.datetimeLiteral_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token TIMESTAMP164=null;
        Token QUOTED_STRING165=null;

        Object TIMESTAMP164_tree=null;
        Object QUOTED_STRING165_tree=null;
        RewriteRuleTokenStream stream_QUOTED_STRING=new RewriteRuleTokenStream(adaptor,"token QUOTED_STRING");
        RewriteRuleTokenStream stream_TIMESTAMP=new RewriteRuleTokenStream(adaptor,"token TIMESTAMP");

        try {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:628:3: ( TIMESTAMP QUOTED_STRING -> ^( DATETIME_LITERAL QUOTED_STRING ) )
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:628:5: TIMESTAMP QUOTED_STRING
            {
            TIMESTAMP164=(Token)match(input,TIMESTAMP,FOLLOW_TIMESTAMP_in_datetimeLiteral2247); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_TIMESTAMP.add(TIMESTAMP164);

            QUOTED_STRING165=(Token)match(input,QUOTED_STRING,FOLLOW_QUOTED_STRING_in_datetimeLiteral2249); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_QUOTED_STRING.add(QUOTED_STRING165);



            // AST REWRITE
            // elements: QUOTED_STRING
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 629:3: -> ^( DATETIME_LITERAL QUOTED_STRING )
            {
                // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:629:6: ^( DATETIME_LITERAL QUOTED_STRING )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(DATETIME_LITERAL, "DATETIME_LITERAL"), root_1);

                adaptor.addChild(root_1, stream_QUOTED_STRING.nextNode());

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
                throw new CMISQueryException(getErrorString(e), e);
            }
        finally {
        }
        return retval;
    }
    // $ANTLR end "datetimeLiteral"

    public static class characterStringLiteral_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "characterStringLiteral"
    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:632:1: characterStringLiteral : QUOTED_STRING -> ^( STRING_LITERAL QUOTED_STRING ) ;
    public final CMISParser.characterStringLiteral_return characterStringLiteral() throws RecognitionException {
        CMISParser.characterStringLiteral_return retval = new CMISParser.characterStringLiteral_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token QUOTED_STRING166=null;

        Object QUOTED_STRING166_tree=null;
        RewriteRuleTokenStream stream_QUOTED_STRING=new RewriteRuleTokenStream(adaptor,"token QUOTED_STRING");

        try {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:633:2: ( QUOTED_STRING -> ^( STRING_LITERAL QUOTED_STRING ) )
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:633:4: QUOTED_STRING
            {
            QUOTED_STRING166=(Token)match(input,QUOTED_STRING,FOLLOW_QUOTED_STRING_in_characterStringLiteral2272); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_QUOTED_STRING.add(QUOTED_STRING166);



            // AST REWRITE
            // elements: QUOTED_STRING
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 634:3: -> ^( STRING_LITERAL QUOTED_STRING )
            {
                // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:634:6: ^( STRING_LITERAL QUOTED_STRING )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(STRING_LITERAL, "STRING_LITERAL"), root_1);

                adaptor.addChild(root_1, stream_QUOTED_STRING.nextNode());

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
                throw new CMISQueryException(getErrorString(e), e);
            }
        finally {
        }
        return retval;
    }
    // $ANTLR end "characterStringLiteral"

    public static class keyWord_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "keyWord"
    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:638:1: keyWord : ( SELECT | AS | FROM | JOIN | INNER | LEFT | OUTER | ON | WHERE | OR | AND | NOT | IN | LIKE | IS | NULL | ANY | CONTAINS | IN_FOLDER | IN_TREE | ORDER | BY | ASC | DESC | TIMESTAMP | TRUE | FALSE | cmisFunction );
    public final CMISParser.keyWord_return keyWord() throws RecognitionException {
        CMISParser.keyWord_return retval = new CMISParser.keyWord_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token SELECT167=null;
        Token AS168=null;
        Token FROM169=null;
        Token JOIN170=null;
        Token INNER171=null;
        Token LEFT172=null;
        Token OUTER173=null;
        Token ON174=null;
        Token WHERE175=null;
        Token OR176=null;
        Token AND177=null;
        Token NOT178=null;
        Token IN179=null;
        Token LIKE180=null;
        Token IS181=null;
        Token NULL182=null;
        Token ANY183=null;
        Token CONTAINS184=null;
        Token IN_FOLDER185=null;
        Token IN_TREE186=null;
        Token ORDER187=null;
        Token BY188=null;
        Token ASC189=null;
        Token DESC190=null;
        Token TIMESTAMP191=null;
        Token TRUE192=null;
        Token FALSE193=null;
        CMISParser.cmisFunction_return cmisFunction194 = null;


        Object SELECT167_tree=null;
        Object AS168_tree=null;
        Object FROM169_tree=null;
        Object JOIN170_tree=null;
        Object INNER171_tree=null;
        Object LEFT172_tree=null;
        Object OUTER173_tree=null;
        Object ON174_tree=null;
        Object WHERE175_tree=null;
        Object OR176_tree=null;
        Object AND177_tree=null;
        Object NOT178_tree=null;
        Object IN179_tree=null;
        Object LIKE180_tree=null;
        Object IS181_tree=null;
        Object NULL182_tree=null;
        Object ANY183_tree=null;
        Object CONTAINS184_tree=null;
        Object IN_FOLDER185_tree=null;
        Object IN_TREE186_tree=null;
        Object ORDER187_tree=null;
        Object BY188_tree=null;
        Object ASC189_tree=null;
        Object DESC190_tree=null;
        Object TIMESTAMP191_tree=null;
        Object TRUE192_tree=null;
        Object FALSE193_tree=null;

        try {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:639:3: ( SELECT | AS | FROM | JOIN | INNER | LEFT | OUTER | ON | WHERE | OR | AND | NOT | IN | LIKE | IS | NULL | ANY | CONTAINS | IN_FOLDER | IN_TREE | ORDER | BY | ASC | DESC | TIMESTAMP | TRUE | FALSE | cmisFunction )
            int alt47=28;
            switch ( input.LA(1) ) {
            case SELECT:
                {
                alt47=1;
                }
                break;
            case AS:
                {
                alt47=2;
                }
                break;
            case FROM:
                {
                alt47=3;
                }
                break;
            case JOIN:
                {
                alt47=4;
                }
                break;
            case INNER:
                {
                alt47=5;
                }
                break;
            case LEFT:
                {
                alt47=6;
                }
                break;
            case OUTER:
                {
                alt47=7;
                }
                break;
            case ON:
                {
                alt47=8;
                }
                break;
            case WHERE:
                {
                alt47=9;
                }
                break;
            case OR:
                {
                alt47=10;
                }
                break;
            case AND:
                {
                alt47=11;
                }
                break;
            case NOT:
                {
                alt47=12;
                }
                break;
            case IN:
                {
                alt47=13;
                }
                break;
            case LIKE:
                {
                alt47=14;
                }
                break;
            case IS:
                {
                alt47=15;
                }
                break;
            case NULL:
                {
                alt47=16;
                }
                break;
            case ANY:
                {
                alt47=17;
                }
                break;
            case CONTAINS:
                {
                alt47=18;
                }
                break;
            case IN_FOLDER:
                {
                alt47=19;
                }
                break;
            case IN_TREE:
                {
                alt47=20;
                }
                break;
            case ORDER:
                {
                alt47=21;
                }
                break;
            case BY:
                {
                alt47=22;
                }
                break;
            case ASC:
                {
                alt47=23;
                }
                break;
            case DESC:
                {
                alt47=24;
                }
                break;
            case TIMESTAMP:
                {
                alt47=25;
                }
                break;
            case TRUE:
                {
                alt47=26;
                }
                break;
            case FALSE:
                {
                alt47=27;
                }
                break;
            case SCORE:
                {
                alt47=28;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 47, 0, input);

                throw nvae;
            }

            switch (alt47) {
                case 1 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:639:5: SELECT
                    {
                    root_0 = (Object)adaptor.nil();

                    SELECT167=(Token)match(input,SELECT,FOLLOW_SELECT_in_keyWord2298); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    SELECT167_tree = (Object)adaptor.create(SELECT167);
                    adaptor.addChild(root_0, SELECT167_tree);
                    }

                    }
                    break;
                case 2 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:640:4: AS
                    {
                    root_0 = (Object)adaptor.nil();

                    AS168=(Token)match(input,AS,FOLLOW_AS_in_keyWord2303); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    AS168_tree = (Object)adaptor.create(AS168);
                    adaptor.addChild(root_0, AS168_tree);
                    }

                    }
                    break;
                case 3 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:641:4: FROM
                    {
                    root_0 = (Object)adaptor.nil();

                    FROM169=(Token)match(input,FROM,FOLLOW_FROM_in_keyWord2308); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    FROM169_tree = (Object)adaptor.create(FROM169);
                    adaptor.addChild(root_0, FROM169_tree);
                    }

                    }
                    break;
                case 4 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:642:4: JOIN
                    {
                    root_0 = (Object)adaptor.nil();

                    JOIN170=(Token)match(input,JOIN,FOLLOW_JOIN_in_keyWord2314); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    JOIN170_tree = (Object)adaptor.create(JOIN170);
                    adaptor.addChild(root_0, JOIN170_tree);
                    }

                    }
                    break;
                case 5 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:643:4: INNER
                    {
                    root_0 = (Object)adaptor.nil();

                    INNER171=(Token)match(input,INNER,FOLLOW_INNER_in_keyWord2320); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    INNER171_tree = (Object)adaptor.create(INNER171);
                    adaptor.addChild(root_0, INNER171_tree);
                    }

                    }
                    break;
                case 6 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:644:4: LEFT
                    {
                    root_0 = (Object)adaptor.nil();

                    LEFT172=(Token)match(input,LEFT,FOLLOW_LEFT_in_keyWord2326); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    LEFT172_tree = (Object)adaptor.create(LEFT172);
                    adaptor.addChild(root_0, LEFT172_tree);
                    }

                    }
                    break;
                case 7 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:645:4: OUTER
                    {
                    root_0 = (Object)adaptor.nil();

                    OUTER173=(Token)match(input,OUTER,FOLLOW_OUTER_in_keyWord2332); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    OUTER173_tree = (Object)adaptor.create(OUTER173);
                    adaptor.addChild(root_0, OUTER173_tree);
                    }

                    }
                    break;
                case 8 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:646:4: ON
                    {
                    root_0 = (Object)adaptor.nil();

                    ON174=(Token)match(input,ON,FOLLOW_ON_in_keyWord2338); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    ON174_tree = (Object)adaptor.create(ON174);
                    adaptor.addChild(root_0, ON174_tree);
                    }

                    }
                    break;
                case 9 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:647:4: WHERE
                    {
                    root_0 = (Object)adaptor.nil();

                    WHERE175=(Token)match(input,WHERE,FOLLOW_WHERE_in_keyWord2344); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    WHERE175_tree = (Object)adaptor.create(WHERE175);
                    adaptor.addChild(root_0, WHERE175_tree);
                    }

                    }
                    break;
                case 10 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:648:4: OR
                    {
                    root_0 = (Object)adaptor.nil();

                    OR176=(Token)match(input,OR,FOLLOW_OR_in_keyWord2350); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    OR176_tree = (Object)adaptor.create(OR176);
                    adaptor.addChild(root_0, OR176_tree);
                    }

                    }
                    break;
                case 11 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:649:4: AND
                    {
                    root_0 = (Object)adaptor.nil();

                    AND177=(Token)match(input,AND,FOLLOW_AND_in_keyWord2356); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    AND177_tree = (Object)adaptor.create(AND177);
                    adaptor.addChild(root_0, AND177_tree);
                    }

                    }
                    break;
                case 12 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:650:4: NOT
                    {
                    root_0 = (Object)adaptor.nil();

                    NOT178=(Token)match(input,NOT,FOLLOW_NOT_in_keyWord2362); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    NOT178_tree = (Object)adaptor.create(NOT178);
                    adaptor.addChild(root_0, NOT178_tree);
                    }

                    }
                    break;
                case 13 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:651:4: IN
                    {
                    root_0 = (Object)adaptor.nil();

                    IN179=(Token)match(input,IN,FOLLOW_IN_in_keyWord2368); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    IN179_tree = (Object)adaptor.create(IN179);
                    adaptor.addChild(root_0, IN179_tree);
                    }

                    }
                    break;
                case 14 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:652:4: LIKE
                    {
                    root_0 = (Object)adaptor.nil();

                    LIKE180=(Token)match(input,LIKE,FOLLOW_LIKE_in_keyWord2374); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    LIKE180_tree = (Object)adaptor.create(LIKE180);
                    adaptor.addChild(root_0, LIKE180_tree);
                    }

                    }
                    break;
                case 15 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:653:4: IS
                    {
                    root_0 = (Object)adaptor.nil();

                    IS181=(Token)match(input,IS,FOLLOW_IS_in_keyWord2380); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    IS181_tree = (Object)adaptor.create(IS181);
                    adaptor.addChild(root_0, IS181_tree);
                    }

                    }
                    break;
                case 16 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:654:4: NULL
                    {
                    root_0 = (Object)adaptor.nil();

                    NULL182=(Token)match(input,NULL,FOLLOW_NULL_in_keyWord2386); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    NULL182_tree = (Object)adaptor.create(NULL182);
                    adaptor.addChild(root_0, NULL182_tree);
                    }

                    }
                    break;
                case 17 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:655:4: ANY
                    {
                    root_0 = (Object)adaptor.nil();

                    ANY183=(Token)match(input,ANY,FOLLOW_ANY_in_keyWord2392); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    ANY183_tree = (Object)adaptor.create(ANY183);
                    adaptor.addChild(root_0, ANY183_tree);
                    }

                    }
                    break;
                case 18 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:656:4: CONTAINS
                    {
                    root_0 = (Object)adaptor.nil();

                    CONTAINS184=(Token)match(input,CONTAINS,FOLLOW_CONTAINS_in_keyWord2398); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    CONTAINS184_tree = (Object)adaptor.create(CONTAINS184);
                    adaptor.addChild(root_0, CONTAINS184_tree);
                    }

                    }
                    break;
                case 19 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:657:4: IN_FOLDER
                    {
                    root_0 = (Object)adaptor.nil();

                    IN_FOLDER185=(Token)match(input,IN_FOLDER,FOLLOW_IN_FOLDER_in_keyWord2405); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    IN_FOLDER185_tree = (Object)adaptor.create(IN_FOLDER185);
                    adaptor.addChild(root_0, IN_FOLDER185_tree);
                    }

                    }
                    break;
                case 20 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:658:4: IN_TREE
                    {
                    root_0 = (Object)adaptor.nil();

                    IN_TREE186=(Token)match(input,IN_TREE,FOLLOW_IN_TREE_in_keyWord2411); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    IN_TREE186_tree = (Object)adaptor.create(IN_TREE186);
                    adaptor.addChild(root_0, IN_TREE186_tree);
                    }

                    }
                    break;
                case 21 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:659:4: ORDER
                    {
                    root_0 = (Object)adaptor.nil();

                    ORDER187=(Token)match(input,ORDER,FOLLOW_ORDER_in_keyWord2417); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    ORDER187_tree = (Object)adaptor.create(ORDER187);
                    adaptor.addChild(root_0, ORDER187_tree);
                    }

                    }
                    break;
                case 22 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:660:4: BY
                    {
                    root_0 = (Object)adaptor.nil();

                    BY188=(Token)match(input,BY,FOLLOW_BY_in_keyWord2422); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    BY188_tree = (Object)adaptor.create(BY188);
                    adaptor.addChild(root_0, BY188_tree);
                    }

                    }
                    break;
                case 23 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:661:4: ASC
                    {
                    root_0 = (Object)adaptor.nil();

                    ASC189=(Token)match(input,ASC,FOLLOW_ASC_in_keyWord2428); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    ASC189_tree = (Object)adaptor.create(ASC189);
                    adaptor.addChild(root_0, ASC189_tree);
                    }

                    }
                    break;
                case 24 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:662:4: DESC
                    {
                    root_0 = (Object)adaptor.nil();

                    DESC190=(Token)match(input,DESC,FOLLOW_DESC_in_keyWord2434); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    DESC190_tree = (Object)adaptor.create(DESC190);
                    adaptor.addChild(root_0, DESC190_tree);
                    }

                    }
                    break;
                case 25 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:663:4: TIMESTAMP
                    {
                    root_0 = (Object)adaptor.nil();

                    TIMESTAMP191=(Token)match(input,TIMESTAMP,FOLLOW_TIMESTAMP_in_keyWord2439); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    TIMESTAMP191_tree = (Object)adaptor.create(TIMESTAMP191);
                    adaptor.addChild(root_0, TIMESTAMP191_tree);
                    }

                    }
                    break;
                case 26 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:664:4: TRUE
                    {
                    root_0 = (Object)adaptor.nil();

                    TRUE192=(Token)match(input,TRUE,FOLLOW_TRUE_in_keyWord2444); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    TRUE192_tree = (Object)adaptor.create(TRUE192);
                    adaptor.addChild(root_0, TRUE192_tree);
                    }

                    }
                    break;
                case 27 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:665:4: FALSE
                    {
                    root_0 = (Object)adaptor.nil();

                    FALSE193=(Token)match(input,FALSE,FOLLOW_FALSE_in_keyWord2449); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    FALSE193_tree = (Object)adaptor.create(FALSE193);
                    adaptor.addChild(root_0, FALSE193_tree);
                    }

                    }
                    break;
                case 28 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:666:4: cmisFunction
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_cmisFunction_in_keyWord2454);
                    cmisFunction194=cmisFunction();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, cmisFunction194.getTree());

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
                throw new CMISQueryException(getErrorString(e), e);
            }
        finally {
        }
        return retval;
    }
    // $ANTLR end "keyWord"

    public static class cmisFunction_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "cmisFunction"
    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:669:1: cmisFunction : SCORE -> SCORE ;
    public final CMISParser.cmisFunction_return cmisFunction() throws RecognitionException {
        CMISParser.cmisFunction_return retval = new CMISParser.cmisFunction_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token SCORE195=null;

        Object SCORE195_tree=null;
        RewriteRuleTokenStream stream_SCORE=new RewriteRuleTokenStream(adaptor,"token SCORE");

        try {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:669:14: ( SCORE -> SCORE )
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:669:16: SCORE
            {
            SCORE195=(Token)match(input,SCORE,FOLLOW_SCORE_in_cmisFunction2465); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_SCORE.add(SCORE195);



            // AST REWRITE
            // elements: SCORE
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 670:2: -> SCORE
            {
                adaptor.addChild(root_0, stream_SCORE.nextNode());

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
                throw new CMISQueryException(getErrorString(e), e);
            }
        finally {
        }
        return retval;
    }
    // $ANTLR end "cmisFunction"

    public static class keyWordOrId_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "keyWordOrId"
    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:673:1: keyWordOrId : ( keyWord -> keyWord | ID -> ID );
    public final CMISParser.keyWordOrId_return keyWordOrId() throws RecognitionException {
        CMISParser.keyWordOrId_return retval = new CMISParser.keyWordOrId_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token ID197=null;
        CMISParser.keyWord_return keyWord196 = null;


        Object ID197_tree=null;
        RewriteRuleTokenStream stream_ID=new RewriteRuleTokenStream(adaptor,"token ID");
        RewriteRuleSubtreeStream stream_keyWord=new RewriteRuleSubtreeStream(adaptor,"rule keyWord");
        try {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:674:2: ( keyWord -> keyWord | ID -> ID )
            int alt48=2;
            int LA48_0 = input.LA(1);

            if ( (LA48_0==SELECT||LA48_0==AS||(LA48_0>=FROM && LA48_0<=ON)||(LA48_0>=WHERE && LA48_0<=NOT)||(LA48_0>=IN && LA48_0<=DESC)||(LA48_0>=TRUE && LA48_0<=SCORE)) ) {
                alt48=1;
            }
            else if ( (LA48_0==ID) ) {
                alt48=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 48, 0, input);

                throw nvae;
            }
            switch (alt48) {
                case 1 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:674:4: keyWord
                    {
                    pushFollow(FOLLOW_keyWord_in_keyWordOrId2483);
                    keyWord196=keyWord();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_keyWord.add(keyWord196.getTree());


                    // AST REWRITE
                    // elements: keyWord
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 675:3: -> keyWord
                    {
                        adaptor.addChild(root_0, stream_keyWord.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:676:4: ID
                    {
                    ID197=(Token)match(input,ID,FOLLOW_ID_in_keyWordOrId2495); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_ID.add(ID197);



                    // AST REWRITE
                    // elements: ID
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 677:3: -> ID
                    {
                        adaptor.addChild(root_0, stream_ID.nextNode());

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
                throw new CMISQueryException(getErrorString(e), e);
            }
        finally {
        }
        return retval;
    }
    // $ANTLR end "keyWordOrId"

    // $ANTLR start synpred1_CMIS
    public final void synpred1_CMIS_fragment() throws RecognitionException {   
        // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:314:4: ( valueExpression )
        // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:314:5: valueExpression
        {
        pushFollow(FOLLOW_valueExpression_in_synpred1_CMIS340);
        valueExpression();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred1_CMIS

    // $ANTLR start synpred2_CMIS
    public final void synpred2_CMIS_fragment() throws RecognitionException {   
        // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:366:4: ( tableName )
        // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:366:5: tableName
        {
        pushFollow(FOLLOW_tableName_in_synpred2_CMIS771);
        tableName();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred2_CMIS

    // $ANTLR start synpred3_CMIS
    public final void synpred3_CMIS_fragment() throws RecognitionException {   
        // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:516:6: ( columnReference )
        // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:516:7: columnReference
        {
        pushFollow(FOLLOW_columnReference_in_synpred3_CMIS1592);
        columnReference();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred3_CMIS

    // $ANTLR start synpred4_CMIS
    public final void synpred4_CMIS_fragment() throws RecognitionException {   
        // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:518:11: ( columnReference )
        // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:518:12: columnReference
        {
        pushFollow(FOLLOW_columnReference_in_synpred4_CMIS1630);
        columnReference();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred4_CMIS

    // Delegated rules

    public final boolean synpred2_CMIS() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred2_CMIS_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred4_CMIS() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred4_CMIS_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred3_CMIS() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred3_CMIS_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred1_CMIS() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred1_CMIS_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }


    protected DFA7 dfa7 = new DFA7(this);
    protected DFA9 dfa9 = new DFA9(this);
    protected DFA10 dfa10 = new DFA10(this);
    protected DFA13 dfa13 = new DFA13(this);
    protected DFA14 dfa14 = new DFA14(this);
    protected DFA15 dfa15 = new DFA15(this);
    protected DFA26 dfa26 = new DFA26(this);
    protected DFA27 dfa27 = new DFA27(this);
    protected DFA28 dfa28 = new DFA28(this);
    protected DFA36 dfa36 = new DFA36(this);
    protected DFA34 dfa34 = new DFA34(this);
    protected DFA35 dfa35 = new DFA35(this);
    protected DFA43 dfa43 = new DFA43(this);
    static final String DFA7_eotS =
        "\134\uffff";
    static final String DFA7_eofS =
        "\134\uffff";
    static final String DFA7_minS =
        "\1\37\1\43\1\37\36\uffff\35\106\35\43\1\uffff";
    static final String DFA7_maxS =
        "\1\114\1\43\1\114\36\uffff\35\106\35\43\1\uffff";
    static final String DFA7_acceptS =
        "\3\uffff\34\1\1\2\1\3\72\uffff\1\2";
    static final String DFA7_specialS =
        "\1\74\1\7\1\22\36\uffff\1\71\1\0\1\4\1\12\1\17\1\20\1\13\1\5\1"+
        "\1\1\72\1\66\1\26\1\24\1\23\1\15\1\10\1\3\1\73\1\67\1\27\1\25\1"+
        "\11\1\16\1\2\1\6\1\30\1\70\1\14\1\21\1\65\1\64\1\63\1\62\1\61\1"+
        "\60\1\57\1\56\1\55\1\54\1\53\1\52\1\51\1\50\1\47\1\46\1\45\1\44"+
        "\1\43\1\42\1\41\1\40\1\37\1\36\1\35\1\34\1\33\1\32\1\31\1\uffff}>";
    static final String[] DFA7_transitionS = {
            "\1\4\2\uffff\1\5\4\uffff\1\6\1\7\1\10\1\11\1\12\1\13\1\uffff"+
            "\1\14\1\15\1\16\1\17\5\uffff\1\20\1\21\1\22\1\23\1\24\1\25\1"+
            "\26\1\27\1\30\1\31\1\32\1\33\2\uffff\1\1\1\2\2\uffff\1\35\1"+
            "\36\1\34\1\3",
            "\1\37",
            "\1\41\2\uffff\1\42\4\uffff\1\43\1\44\1\45\1\46\1\47\1\50\1"+
            "\uffff\1\51\1\52\1\53\1\54\5\uffff\1\55\1\56\1\57\1\60\1\61"+
            "\1\62\1\63\1\64\1\65\1\66\1\67\1\70\2\uffff\1\75\3\uffff\1\72"+
            "\1\73\1\71\1\74",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\76",
            "\1\77",
            "\1\100",
            "\1\101",
            "\1\102",
            "\1\103",
            "\1\104",
            "\1\105",
            "\1\106",
            "\1\107",
            "\1\110",
            "\1\111",
            "\1\112",
            "\1\113",
            "\1\114",
            "\1\115",
            "\1\116",
            "\1\117",
            "\1\120",
            "\1\121",
            "\1\122",
            "\1\123",
            "\1\124",
            "\1\125",
            "\1\126",
            "\1\127",
            "\1\130",
            "\1\131",
            "\1\132",
            "\1\133",
            "\1\133",
            "\1\133",
            "\1\133",
            "\1\133",
            "\1\133",
            "\1\133",
            "\1\133",
            "\1\133",
            "\1\133",
            "\1\133",
            "\1\133",
            "\1\133",
            "\1\133",
            "\1\133",
            "\1\133",
            "\1\133",
            "\1\133",
            "\1\133",
            "\1\133",
            "\1\133",
            "\1\133",
            "\1\133",
            "\1\133",
            "\1\133",
            "\1\133",
            "\1\133",
            "\1\133",
            "\1\133",
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
            return "313:1: selectSubList : ( ( valueExpression )=> valueExpression ( ( AS )? columnName )? -> ^( COLUMN valueExpression ( columnName )? ) | qualifier DOTSTAR -> ^( ALL_COLUMNS qualifier ) | multiValuedColumnReference ->);";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA7_34 = input.LA(1);

                         
                        int index7_34 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA7_34==DOUBLE_QUOTE) && ((strict == false))) {s = 63;}

                         
                        input.seek(index7_34);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA7_41 = input.LA(1);

                         
                        int index7_41 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA7_41==DOUBLE_QUOTE) && ((strict == false))) {s = 70;}

                         
                        input.seek(index7_41);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA7_56 = input.LA(1);

                         
                        int index7_56 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA7_56==DOUBLE_QUOTE) && ((strict == false))) {s = 85;}

                         
                        input.seek(index7_56);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA7_49 = input.LA(1);

                         
                        int index7_49 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA7_49==DOUBLE_QUOTE) && ((strict == false))) {s = 78;}

                         
                        input.seek(index7_49);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA7_35 = input.LA(1);

                         
                        int index7_35 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA7_35==DOUBLE_QUOTE) && ((strict == false))) {s = 64;}

                         
                        input.seek(index7_35);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA7_40 = input.LA(1);

                         
                        int index7_40 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA7_40==DOUBLE_QUOTE) && ((strict == false))) {s = 69;}

                         
                        input.seek(index7_40);
                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA7_57 = input.LA(1);

                         
                        int index7_57 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA7_57==DOUBLE_QUOTE) && ((strict == false))) {s = 86;}

                         
                        input.seek(index7_57);
                        if ( s>=0 ) return s;
                        break;
                    case 7 : 
                        int LA7_1 = input.LA(1);

                         
                        int index7_1 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA7_1==DOTSTAR) ) {s = 31;}

                        else if ( (((synpred1_CMIS()&&(strict == false))||synpred1_CMIS())) ) {s = 30;}

                        else if ( (true) ) {s = 32;}

                         
                        input.seek(index7_1);
                        if ( s>=0 ) return s;
                        break;
                    case 8 : 
                        int LA7_48 = input.LA(1);

                         
                        int index7_48 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA7_48==DOUBLE_QUOTE) && ((strict == false))) {s = 77;}

                         
                        input.seek(index7_48);
                        if ( s>=0 ) return s;
                        break;
                    case 9 : 
                        int LA7_54 = input.LA(1);

                         
                        int index7_54 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA7_54==DOUBLE_QUOTE) && ((strict == false))) {s = 83;}

                         
                        input.seek(index7_54);
                        if ( s>=0 ) return s;
                        break;
                    case 10 : 
                        int LA7_36 = input.LA(1);

                         
                        int index7_36 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA7_36==DOUBLE_QUOTE) && ((strict == false))) {s = 65;}

                         
                        input.seek(index7_36);
                        if ( s>=0 ) return s;
                        break;
                    case 11 : 
                        int LA7_39 = input.LA(1);

                         
                        int index7_39 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA7_39==DOUBLE_QUOTE) && ((strict == false))) {s = 68;}

                         
                        input.seek(index7_39);
                        if ( s>=0 ) return s;
                        break;
                    case 12 : 
                        int LA7_60 = input.LA(1);

                         
                        int index7_60 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA7_60==DOUBLE_QUOTE) && ((strict == false))) {s = 89;}

                         
                        input.seek(index7_60);
                        if ( s>=0 ) return s;
                        break;
                    case 13 : 
                        int LA7_47 = input.LA(1);

                         
                        int index7_47 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA7_47==DOUBLE_QUOTE) && ((strict == false))) {s = 76;}

                         
                        input.seek(index7_47);
                        if ( s>=0 ) return s;
                        break;
                    case 14 : 
                        int LA7_55 = input.LA(1);

                         
                        int index7_55 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA7_55==DOUBLE_QUOTE) && ((strict == false))) {s = 84;}

                         
                        input.seek(index7_55);
                        if ( s>=0 ) return s;
                        break;
                    case 15 : 
                        int LA7_37 = input.LA(1);

                         
                        int index7_37 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA7_37==DOUBLE_QUOTE) && ((strict == false))) {s = 66;}

                         
                        input.seek(index7_37);
                        if ( s>=0 ) return s;
                        break;
                    case 16 : 
                        int LA7_38 = input.LA(1);

                         
                        int index7_38 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA7_38==DOUBLE_QUOTE) && ((strict == false))) {s = 67;}

                         
                        input.seek(index7_38);
                        if ( s>=0 ) return s;
                        break;
                    case 17 : 
                        int LA7_61 = input.LA(1);

                         
                        int index7_61 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA7_61==DOUBLE_QUOTE) && ((strict == false))) {s = 90;}

                         
                        input.seek(index7_61);
                        if ( s>=0 ) return s;
                        break;
                    case 18 : 
                        int LA7_2 = input.LA(1);

                         
                        int index7_2 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA7_2==SELECT) && ((strict == false))) {s = 33;}

                        else if ( (LA7_2==AS) && ((strict == false))) {s = 34;}

                        else if ( (LA7_2==FROM) && ((strict == false))) {s = 35;}

                        else if ( (LA7_2==JOIN) && ((strict == false))) {s = 36;}

                        else if ( (LA7_2==INNER) && ((strict == false))) {s = 37;}

                        else if ( (LA7_2==LEFT) && ((strict == false))) {s = 38;}

                        else if ( (LA7_2==OUTER) && ((strict == false))) {s = 39;}

                        else if ( (LA7_2==ON) && ((strict == false))) {s = 40;}

                        else if ( (LA7_2==WHERE) && ((strict == false))) {s = 41;}

                        else if ( (LA7_2==OR) && ((strict == false))) {s = 42;}

                        else if ( (LA7_2==AND) && ((strict == false))) {s = 43;}

                        else if ( (LA7_2==NOT) && ((strict == false))) {s = 44;}

                        else if ( (LA7_2==IN) && ((strict == false))) {s = 45;}

                        else if ( (LA7_2==LIKE) && ((strict == false))) {s = 46;}

                        else if ( (LA7_2==IS) && ((strict == false))) {s = 47;}

                        else if ( (LA7_2==NULL) && ((strict == false))) {s = 48;}

                        else if ( (LA7_2==ANY) && ((strict == false))) {s = 49;}

                        else if ( (LA7_2==CONTAINS) && ((strict == false))) {s = 50;}

                        else if ( (LA7_2==IN_FOLDER) && ((strict == false))) {s = 51;}

                        else if ( (LA7_2==IN_TREE) && ((strict == false))) {s = 52;}

                        else if ( (LA7_2==ORDER) && ((strict == false))) {s = 53;}

                        else if ( (LA7_2==BY) && ((strict == false))) {s = 54;}

                        else if ( (LA7_2==ASC) && ((strict == false))) {s = 55;}

                        else if ( (LA7_2==DESC) && ((strict == false))) {s = 56;}

                        else if ( (LA7_2==TIMESTAMP) && ((strict == false))) {s = 57;}

                        else if ( (LA7_2==TRUE) && ((strict == false))) {s = 58;}

                        else if ( (LA7_2==FALSE) && ((strict == false))) {s = 59;}

                        else if ( (LA7_2==SCORE) && ((strict == false))) {s = 60;}

                        else if ( (LA7_2==ID) && ((strict == false))) {s = 61;}

                         
                        input.seek(index7_2);
                        if ( s>=0 ) return s;
                        break;
                    case 19 : 
                        int LA7_46 = input.LA(1);

                         
                        int index7_46 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA7_46==DOUBLE_QUOTE) && ((strict == false))) {s = 75;}

                         
                        input.seek(index7_46);
                        if ( s>=0 ) return s;
                        break;
                    case 20 : 
                        int LA7_45 = input.LA(1);

                         
                        int index7_45 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA7_45==DOUBLE_QUOTE) && ((strict == false))) {s = 74;}

                         
                        input.seek(index7_45);
                        if ( s>=0 ) return s;
                        break;
                    case 21 : 
                        int LA7_53 = input.LA(1);

                         
                        int index7_53 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA7_53==DOUBLE_QUOTE) && ((strict == false))) {s = 82;}

                         
                        input.seek(index7_53);
                        if ( s>=0 ) return s;
                        break;
                    case 22 : 
                        int LA7_44 = input.LA(1);

                         
                        int index7_44 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA7_44==DOUBLE_QUOTE) && ((strict == false))) {s = 73;}

                         
                        input.seek(index7_44);
                        if ( s>=0 ) return s;
                        break;
                    case 23 : 
                        int LA7_52 = input.LA(1);

                         
                        int index7_52 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA7_52==DOUBLE_QUOTE) && ((strict == false))) {s = 81;}

                         
                        input.seek(index7_52);
                        if ( s>=0 ) return s;
                        break;
                    case 24 : 
                        int LA7_58 = input.LA(1);

                         
                        int index7_58 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA7_58==DOUBLE_QUOTE) && ((strict == false))) {s = 87;}

                         
                        input.seek(index7_58);
                        if ( s>=0 ) return s;
                        break;
                    case 25 : 
                        int LA7_90 = input.LA(1);

                         
                        int index7_90 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA7_90==DOTSTAR) && ((strict == false))) {s = 91;}

                        else if ( (((synpred1_CMIS()&&(strict == false))||(synpred1_CMIS()&&(strict == false))||(synpred1_CMIS()&&(strict == false)))) ) {s = 30;}

                        else if ( ((strict == false)) ) {s = 32;}

                         
                        input.seek(index7_90);
                        if ( s>=0 ) return s;
                        break;
                    case 26 : 
                        int LA7_89 = input.LA(1);

                         
                        int index7_89 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA7_89==DOTSTAR) && ((strict == false))) {s = 91;}

                        else if ( (((synpred1_CMIS()&&(strict == false))||(synpred1_CMIS()&&(strict == false))||(synpred1_CMIS()&&(strict == false)))) ) {s = 30;}

                        else if ( ((strict == false)) ) {s = 32;}

                         
                        input.seek(index7_89);
                        if ( s>=0 ) return s;
                        break;
                    case 27 : 
                        int LA7_88 = input.LA(1);

                         
                        int index7_88 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA7_88==DOTSTAR) && ((strict == false))) {s = 91;}

                        else if ( (((synpred1_CMIS()&&(strict == false))||(synpred1_CMIS()&&(strict == false))||(synpred1_CMIS()&&(strict == false)))) ) {s = 30;}

                        else if ( ((strict == false)) ) {s = 32;}

                         
                        input.seek(index7_88);
                        if ( s>=0 ) return s;
                        break;
                    case 28 : 
                        int LA7_87 = input.LA(1);

                         
                        int index7_87 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA7_87==DOTSTAR) && ((strict == false))) {s = 91;}

                        else if ( (((synpred1_CMIS()&&(strict == false))||(synpred1_CMIS()&&(strict == false))||(synpred1_CMIS()&&(strict == false)))) ) {s = 30;}

                        else if ( ((strict == false)) ) {s = 32;}

                         
                        input.seek(index7_87);
                        if ( s>=0 ) return s;
                        break;
                    case 29 : 
                        int LA7_86 = input.LA(1);

                         
                        int index7_86 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA7_86==DOTSTAR) && ((strict == false))) {s = 91;}

                        else if ( (((synpred1_CMIS()&&(strict == false))||(synpred1_CMIS()&&(strict == false))||(synpred1_CMIS()&&(strict == false)))) ) {s = 30;}

                        else if ( ((strict == false)) ) {s = 32;}

                         
                        input.seek(index7_86);
                        if ( s>=0 ) return s;
                        break;
                    case 30 : 
                        int LA7_85 = input.LA(1);

                         
                        int index7_85 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA7_85==DOTSTAR) && ((strict == false))) {s = 91;}

                        else if ( (((synpred1_CMIS()&&(strict == false))||(synpred1_CMIS()&&(strict == false))||(synpred1_CMIS()&&(strict == false)))) ) {s = 30;}

                        else if ( ((strict == false)) ) {s = 32;}

                         
                        input.seek(index7_85);
                        if ( s>=0 ) return s;
                        break;
                    case 31 : 
                        int LA7_84 = input.LA(1);

                         
                        int index7_84 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA7_84==DOTSTAR) && ((strict == false))) {s = 91;}

                        else if ( (((synpred1_CMIS()&&(strict == false))||(synpred1_CMIS()&&(strict == false))||(synpred1_CMIS()&&(strict == false)))) ) {s = 30;}

                        else if ( ((strict == false)) ) {s = 32;}

                         
                        input.seek(index7_84);
                        if ( s>=0 ) return s;
                        break;
                    case 32 : 
                        int LA7_83 = input.LA(1);

                         
                        int index7_83 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA7_83==DOTSTAR) && ((strict == false))) {s = 91;}

                        else if ( (((synpred1_CMIS()&&(strict == false))||(synpred1_CMIS()&&(strict == false))||(synpred1_CMIS()&&(strict == false)))) ) {s = 30;}

                        else if ( ((strict == false)) ) {s = 32;}

                         
                        input.seek(index7_83);
                        if ( s>=0 ) return s;
                        break;
                    case 33 : 
                        int LA7_82 = input.LA(1);

                         
                        int index7_82 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA7_82==DOTSTAR) && ((strict == false))) {s = 91;}

                        else if ( (((synpred1_CMIS()&&(strict == false))||(synpred1_CMIS()&&(strict == false))||(synpred1_CMIS()&&(strict == false)))) ) {s = 30;}

                        else if ( ((strict == false)) ) {s = 32;}

                         
                        input.seek(index7_82);
                        if ( s>=0 ) return s;
                        break;
                    case 34 : 
                        int LA7_81 = input.LA(1);

                         
                        int index7_81 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA7_81==DOTSTAR) && ((strict == false))) {s = 91;}

                        else if ( (((synpred1_CMIS()&&(strict == false))||(synpred1_CMIS()&&(strict == false))||(synpred1_CMIS()&&(strict == false)))) ) {s = 30;}

                        else if ( ((strict == false)) ) {s = 32;}

                         
                        input.seek(index7_81);
                        if ( s>=0 ) return s;
                        break;
                    case 35 : 
                        int LA7_80 = input.LA(1);

                         
                        int index7_80 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA7_80==DOTSTAR) && ((strict == false))) {s = 91;}

                        else if ( (((synpred1_CMIS()&&(strict == false))||(synpred1_CMIS()&&(strict == false))||(synpred1_CMIS()&&(strict == false)))) ) {s = 30;}

                        else if ( ((strict == false)) ) {s = 32;}

                         
                        input.seek(index7_80);
                        if ( s>=0 ) return s;
                        break;
                    case 36 : 
                        int LA7_79 = input.LA(1);

                         
                        int index7_79 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA7_79==DOTSTAR) && ((strict == false))) {s = 91;}

                        else if ( (((synpred1_CMIS()&&(strict == false))||(synpred1_CMIS()&&(strict == false))||(synpred1_CMIS()&&(strict == false)))) ) {s = 30;}

                        else if ( ((strict == false)) ) {s = 32;}

                         
                        input.seek(index7_79);
                        if ( s>=0 ) return s;
                        break;
                    case 37 : 
                        int LA7_78 = input.LA(1);

                         
                        int index7_78 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA7_78==DOTSTAR) && ((strict == false))) {s = 91;}

                        else if ( (((synpred1_CMIS()&&(strict == false))||(synpred1_CMIS()&&(strict == false))||(synpred1_CMIS()&&(strict == false)))) ) {s = 30;}

                        else if ( ((strict == false)) ) {s = 32;}

                         
                        input.seek(index7_78);
                        if ( s>=0 ) return s;
                        break;
                    case 38 : 
                        int LA7_77 = input.LA(1);

                         
                        int index7_77 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA7_77==DOTSTAR) && ((strict == false))) {s = 91;}

                        else if ( (((synpred1_CMIS()&&(strict == false))||(synpred1_CMIS()&&(strict == false))||(synpred1_CMIS()&&(strict == false)))) ) {s = 30;}

                        else if ( ((strict == false)) ) {s = 32;}

                         
                        input.seek(index7_77);
                        if ( s>=0 ) return s;
                        break;
                    case 39 : 
                        int LA7_76 = input.LA(1);

                         
                        int index7_76 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA7_76==DOTSTAR) && ((strict == false))) {s = 91;}

                        else if ( (((synpred1_CMIS()&&(strict == false))||(synpred1_CMIS()&&(strict == false))||(synpred1_CMIS()&&(strict == false)))) ) {s = 30;}

                        else if ( ((strict == false)) ) {s = 32;}

                         
                        input.seek(index7_76);
                        if ( s>=0 ) return s;
                        break;
                    case 40 : 
                        int LA7_75 = input.LA(1);

                         
                        int index7_75 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA7_75==DOTSTAR) && ((strict == false))) {s = 91;}

                        else if ( (((synpred1_CMIS()&&(strict == false))||(synpred1_CMIS()&&(strict == false))||(synpred1_CMIS()&&(strict == false)))) ) {s = 30;}

                        else if ( ((strict == false)) ) {s = 32;}

                         
                        input.seek(index7_75);
                        if ( s>=0 ) return s;
                        break;
                    case 41 : 
                        int LA7_74 = input.LA(1);

                         
                        int index7_74 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA7_74==DOTSTAR) && ((strict == false))) {s = 91;}

                        else if ( (((synpred1_CMIS()&&(strict == false))||(synpred1_CMIS()&&(strict == false))||(synpred1_CMIS()&&(strict == false)))) ) {s = 30;}

                        else if ( ((strict == false)) ) {s = 32;}

                         
                        input.seek(index7_74);
                        if ( s>=0 ) return s;
                        break;
                    case 42 : 
                        int LA7_73 = input.LA(1);

                         
                        int index7_73 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA7_73==DOTSTAR) && ((strict == false))) {s = 91;}

                        else if ( (((synpred1_CMIS()&&(strict == false))||(synpred1_CMIS()&&(strict == false))||(synpred1_CMIS()&&(strict == false)))) ) {s = 30;}

                        else if ( ((strict == false)) ) {s = 32;}

                         
                        input.seek(index7_73);
                        if ( s>=0 ) return s;
                        break;
                    case 43 : 
                        int LA7_72 = input.LA(1);

                         
                        int index7_72 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA7_72==DOTSTAR) && ((strict == false))) {s = 91;}

                        else if ( (((synpred1_CMIS()&&(strict == false))||(synpred1_CMIS()&&(strict == false))||(synpred1_CMIS()&&(strict == false)))) ) {s = 30;}

                        else if ( ((strict == false)) ) {s = 32;}

                         
                        input.seek(index7_72);
                        if ( s>=0 ) return s;
                        break;
                    case 44 : 
                        int LA7_71 = input.LA(1);

                         
                        int index7_71 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA7_71==DOTSTAR) && ((strict == false))) {s = 91;}

                        else if ( (((synpred1_CMIS()&&(strict == false))||(synpred1_CMIS()&&(strict == false))||(synpred1_CMIS()&&(strict == false)))) ) {s = 30;}

                        else if ( ((strict == false)) ) {s = 32;}

                         
                        input.seek(index7_71);
                        if ( s>=0 ) return s;
                        break;
                    case 45 : 
                        int LA7_70 = input.LA(1);

                         
                        int index7_70 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA7_70==DOTSTAR) && ((strict == false))) {s = 91;}

                        else if ( (((synpred1_CMIS()&&(strict == false))||(synpred1_CMIS()&&(strict == false))||(synpred1_CMIS()&&(strict == false)))) ) {s = 30;}

                        else if ( ((strict == false)) ) {s = 32;}

                         
                        input.seek(index7_70);
                        if ( s>=0 ) return s;
                        break;
                    case 46 : 
                        int LA7_69 = input.LA(1);

                         
                        int index7_69 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA7_69==DOTSTAR) && ((strict == false))) {s = 91;}

                        else if ( (((synpred1_CMIS()&&(strict == false))||(synpred1_CMIS()&&(strict == false))||(synpred1_CMIS()&&(strict == false)))) ) {s = 30;}

                        else if ( ((strict == false)) ) {s = 32;}

                         
                        input.seek(index7_69);
                        if ( s>=0 ) return s;
                        break;
                    case 47 : 
                        int LA7_68 = input.LA(1);

                         
                        int index7_68 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA7_68==DOTSTAR) && ((strict == false))) {s = 91;}

                        else if ( (((synpred1_CMIS()&&(strict == false))||(synpred1_CMIS()&&(strict == false))||(synpred1_CMIS()&&(strict == false)))) ) {s = 30;}

                        else if ( ((strict == false)) ) {s = 32;}

                         
                        input.seek(index7_68);
                        if ( s>=0 ) return s;
                        break;
                    case 48 : 
                        int LA7_67 = input.LA(1);

                         
                        int index7_67 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA7_67==DOTSTAR) && ((strict == false))) {s = 91;}

                        else if ( (((synpred1_CMIS()&&(strict == false))||(synpred1_CMIS()&&(strict == false))||(synpred1_CMIS()&&(strict == false)))) ) {s = 30;}

                        else if ( ((strict == false)) ) {s = 32;}

                         
                        input.seek(index7_67);
                        if ( s>=0 ) return s;
                        break;
                    case 49 : 
                        int LA7_66 = input.LA(1);

                         
                        int index7_66 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA7_66==DOTSTAR) && ((strict == false))) {s = 91;}

                        else if ( (((synpred1_CMIS()&&(strict == false))||(synpred1_CMIS()&&(strict == false))||(synpred1_CMIS()&&(strict == false)))) ) {s = 30;}

                        else if ( ((strict == false)) ) {s = 32;}

                         
                        input.seek(index7_66);
                        if ( s>=0 ) return s;
                        break;
                    case 50 : 
                        int LA7_65 = input.LA(1);

                         
                        int index7_65 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA7_65==DOTSTAR) && ((strict == false))) {s = 91;}

                        else if ( (((synpred1_CMIS()&&(strict == false))||(synpred1_CMIS()&&(strict == false))||(synpred1_CMIS()&&(strict == false)))) ) {s = 30;}

                        else if ( ((strict == false)) ) {s = 32;}

                         
                        input.seek(index7_65);
                        if ( s>=0 ) return s;
                        break;
                    case 51 : 
                        int LA7_64 = input.LA(1);

                         
                        int index7_64 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA7_64==DOTSTAR) && ((strict == false))) {s = 91;}

                        else if ( (((synpred1_CMIS()&&(strict == false))||(synpred1_CMIS()&&(strict == false))||(synpred1_CMIS()&&(strict == false)))) ) {s = 30;}

                        else if ( ((strict == false)) ) {s = 32;}

                         
                        input.seek(index7_64);
                        if ( s>=0 ) return s;
                        break;
                    case 52 : 
                        int LA7_63 = input.LA(1);

                         
                        int index7_63 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA7_63==DOTSTAR) && ((strict == false))) {s = 91;}

                        else if ( (((synpred1_CMIS()&&(strict == false))||(synpred1_CMIS()&&(strict == false))||(synpred1_CMIS()&&(strict == false)))) ) {s = 30;}

                        else if ( ((strict == false)) ) {s = 32;}

                         
                        input.seek(index7_63);
                        if ( s>=0 ) return s;
                        break;
                    case 53 : 
                        int LA7_62 = input.LA(1);

                         
                        int index7_62 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA7_62==DOTSTAR) && ((strict == false))) {s = 91;}

                        else if ( (((synpred1_CMIS()&&(strict == false))||(synpred1_CMIS()&&(strict == false))||(synpred1_CMIS()&&(strict == false)))) ) {s = 30;}

                        else if ( ((strict == false)) ) {s = 32;}

                         
                        input.seek(index7_62);
                        if ( s>=0 ) return s;
                        break;
                    case 54 : 
                        int LA7_43 = input.LA(1);

                         
                        int index7_43 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA7_43==DOUBLE_QUOTE) && ((strict == false))) {s = 72;}

                         
                        input.seek(index7_43);
                        if ( s>=0 ) return s;
                        break;
                    case 55 : 
                        int LA7_51 = input.LA(1);

                         
                        int index7_51 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA7_51==DOUBLE_QUOTE) && ((strict == false))) {s = 80;}

                         
                        input.seek(index7_51);
                        if ( s>=0 ) return s;
                        break;
                    case 56 : 
                        int LA7_59 = input.LA(1);

                         
                        int index7_59 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA7_59==DOUBLE_QUOTE) && ((strict == false))) {s = 88;}

                         
                        input.seek(index7_59);
                        if ( s>=0 ) return s;
                        break;
                    case 57 : 
                        int LA7_33 = input.LA(1);

                         
                        int index7_33 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA7_33==DOUBLE_QUOTE) && ((strict == false))) {s = 62;}

                         
                        input.seek(index7_33);
                        if ( s>=0 ) return s;
                        break;
                    case 58 : 
                        int LA7_42 = input.LA(1);

                         
                        int index7_42 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA7_42==DOUBLE_QUOTE) && ((strict == false))) {s = 71;}

                         
                        input.seek(index7_42);
                        if ( s>=0 ) return s;
                        break;
                    case 59 : 
                        int LA7_50 = input.LA(1);

                         
                        int index7_50 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA7_50==DOUBLE_QUOTE) && ((strict == false))) {s = 79;}

                         
                        input.seek(index7_50);
                        if ( s>=0 ) return s;
                        break;
                    case 60 : 
                        int LA7_0 = input.LA(1);

                         
                        int index7_0 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA7_0==ID) ) {s = 1;}

                        else if ( (LA7_0==DOUBLE_QUOTE) && ((strict == false))) {s = 2;}

                        else if ( (LA7_0==SCORE) && (((synpred1_CMIS()&&(strict == false))||synpred1_CMIS()))) {s = 3;}

                        else if ( (LA7_0==SELECT) && ((synpred1_CMIS()&&(strict == false)))) {s = 4;}

                        else if ( (LA7_0==AS) && ((synpred1_CMIS()&&(strict == false)))) {s = 5;}

                        else if ( (LA7_0==FROM) && ((synpred1_CMIS()&&(strict == false)))) {s = 6;}

                        else if ( (LA7_0==JOIN) && ((synpred1_CMIS()&&(strict == false)))) {s = 7;}

                        else if ( (LA7_0==INNER) && ((synpred1_CMIS()&&(strict == false)))) {s = 8;}

                        else if ( (LA7_0==LEFT) && ((synpred1_CMIS()&&(strict == false)))) {s = 9;}

                        else if ( (LA7_0==OUTER) && ((synpred1_CMIS()&&(strict == false)))) {s = 10;}

                        else if ( (LA7_0==ON) && ((synpred1_CMIS()&&(strict == false)))) {s = 11;}

                        else if ( (LA7_0==WHERE) && ((synpred1_CMIS()&&(strict == false)))) {s = 12;}

                        else if ( (LA7_0==OR) && ((synpred1_CMIS()&&(strict == false)))) {s = 13;}

                        else if ( (LA7_0==AND) && ((synpred1_CMIS()&&(strict == false)))) {s = 14;}

                        else if ( (LA7_0==NOT) && ((synpred1_CMIS()&&(strict == false)))) {s = 15;}

                        else if ( (LA7_0==IN) && ((synpred1_CMIS()&&(strict == false)))) {s = 16;}

                        else if ( (LA7_0==LIKE) && ((synpred1_CMIS()&&(strict == false)))) {s = 17;}

                        else if ( (LA7_0==IS) && ((synpred1_CMIS()&&(strict == false)))) {s = 18;}

                        else if ( (LA7_0==NULL) && ((synpred1_CMIS()&&(strict == false)))) {s = 19;}

                        else if ( (LA7_0==ANY) && ((synpred1_CMIS()&&(strict == false)))) {s = 20;}

                        else if ( (LA7_0==CONTAINS) && ((synpred1_CMIS()&&(strict == false)))) {s = 21;}

                        else if ( (LA7_0==IN_FOLDER) && ((synpred1_CMIS()&&(strict == false)))) {s = 22;}

                        else if ( (LA7_0==IN_TREE) && ((synpred1_CMIS()&&(strict == false)))) {s = 23;}

                        else if ( (LA7_0==ORDER) && ((synpred1_CMIS()&&(strict == false)))) {s = 24;}

                        else if ( (LA7_0==BY) && ((synpred1_CMIS()&&(strict == false)))) {s = 25;}

                        else if ( (LA7_0==ASC) && ((synpred1_CMIS()&&(strict == false)))) {s = 26;}

                        else if ( (LA7_0==DESC) && ((synpred1_CMIS()&&(strict == false)))) {s = 27;}

                        else if ( (LA7_0==TIMESTAMP) && ((synpred1_CMIS()&&(strict == false)))) {s = 28;}

                        else if ( (LA7_0==TRUE) && ((synpred1_CMIS()&&(strict == false)))) {s = 29;}

                        else if ( (LA7_0==FALSE) && ((synpred1_CMIS()&&(strict == false)))) {s = 30;}

                         
                        input.seek(index7_0);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 7, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA9_eotS =
        "\45\uffff";
    static final String DFA9_eofS =
        "\1\uffff\1\3\40\uffff\1\44\2\uffff";
    static final String DFA9_minS =
        "\1\105\1\41\1\37\2\uffff\35\106\1\41\2\uffff";
    static final String DFA9_maxS =
        "\2\106\1\114\2\uffff\36\106\2\uffff";
    static final String DFA9_acceptS =
        "\3\uffff\1\2\1\1\36\uffff\1\1\1\2";
    static final String DFA9_specialS =
        "\1\0\1\uffff\1\27\2\uffff\1\30\1\22\1\15\1\12\1\14\1\7\1\1\1\34"+
        "\1\32\1\35\1\21\1\31\1\26\1\11\1\17\1\5\1\3\1\6\1\33\1\36\1\37\1"+
        "\23\1\25\1\10\1\13\1\16\1\2\1\24\1\20\1\4\2\uffff}>";
    static final String[] DFA9_transitionS = {
            "\1\1\1\2",
            "\2\3\1\uffff\1\4\1\uffff\5\3\1\uffff\3\3\2\uffff\11\3\5\uffff"+
            "\1\3\1\uffff\2\3\2\uffff\2\3",
            "\1\5\2\uffff\1\6\4\uffff\1\7\1\10\1\11\1\12\1\13\1\14\1\uffff"+
            "\1\15\1\16\1\17\1\20\5\uffff\1\21\1\22\1\23\1\24\1\25\1\26\1"+
            "\27\1\30\1\31\1\32\1\33\1\34\2\uffff\1\41\3\uffff\1\36\1\37"+
            "\1\35\1\40",
            "",
            "",
            "\1\42",
            "\1\42",
            "\1\42",
            "\1\42",
            "\1\42",
            "\1\42",
            "\1\42",
            "\1\42",
            "\1\42",
            "\1\42",
            "\1\42",
            "\1\42",
            "\1\42",
            "\1\42",
            "\1\42",
            "\1\42",
            "\1\42",
            "\1\42",
            "\1\42",
            "\1\42",
            "\1\42",
            "\1\42",
            "\1\42",
            "\1\42",
            "\1\42",
            "\1\42",
            "\1\42",
            "\1\42",
            "\1\42",
            "\2\44\1\uffff\1\43\1\uffff\5\44\1\uffff\3\44\2\uffff\11\44"+
            "\5\uffff\1\44\1\uffff\2\44\2\uffff\2\44",
            "",
            ""
    };

    static final short[] DFA9_eot = DFA.unpackEncodedString(DFA9_eotS);
    static final short[] DFA9_eof = DFA.unpackEncodedString(DFA9_eofS);
    static final char[] DFA9_min = DFA.unpackEncodedStringToUnsignedChars(DFA9_minS);
    static final char[] DFA9_max = DFA.unpackEncodedStringToUnsignedChars(DFA9_maxS);
    static final short[] DFA9_accept = DFA.unpackEncodedString(DFA9_acceptS);
    static final short[] DFA9_special = DFA.unpackEncodedString(DFA9_specialS);
    static final short[][] DFA9_transition;

    static {
        int numStates = DFA9_transitionS.length;
        DFA9_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA9_transition[i] = DFA.unpackEncodedString(DFA9_transitionS[i]);
        }
    }

    class DFA9 extends DFA {

        public DFA9(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 9;
            this.eot = DFA9_eot;
            this.eof = DFA9_eof;
            this.min = DFA9_min;
            this.max = DFA9_max;
            this.accept = DFA9_accept;
            this.special = DFA9_special;
            this.transition = DFA9_transition;
        }
        public String getDescription() {
            return "334:4: ( qualifier DOT )?";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA9_0 = input.LA(1);

                         
                        int index9_0 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA9_0==ID) ) {s = 1;}

                        else if ( (LA9_0==DOUBLE_QUOTE) && ((strict == false))) {s = 2;}

                         
                        input.seek(index9_0);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA9_11 = input.LA(1);

                         
                        int index9_11 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA9_11==DOUBLE_QUOTE) && ((strict == false))) {s = 34;}

                         
                        input.seek(index9_11);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA9_31 = input.LA(1);

                         
                        int index9_31 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA9_31==DOUBLE_QUOTE) && ((strict == false))) {s = 34;}

                         
                        input.seek(index9_31);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA9_21 = input.LA(1);

                         
                        int index9_21 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA9_21==DOUBLE_QUOTE) && ((strict == false))) {s = 34;}

                         
                        input.seek(index9_21);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA9_34 = input.LA(1);

                         
                        int index9_34 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA9_34==DOT) && ((strict == false))) {s = 35;}

                        else if ( (LA9_34==EOF||(LA9_34>=COMMA && LA9_34<=AS)||(LA9_34>=RPAREN && LA9_34<=LEFT)||(LA9_34>=ON && LA9_34<=WHERE)||(LA9_34>=NOT && LA9_34<=IS)||LA9_34==ORDER||(LA9_34>=ASC && LA9_34<=DESC)||(LA9_34>=ID && LA9_34<=DOUBLE_QUOTE)) && ((strict == false))) {s = 36;}

                         
                        input.seek(index9_34);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA9_20 = input.LA(1);

                         
                        int index9_20 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA9_20==DOUBLE_QUOTE) && ((strict == false))) {s = 34;}

                         
                        input.seek(index9_20);
                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA9_22 = input.LA(1);

                         
                        int index9_22 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA9_22==DOUBLE_QUOTE) && ((strict == false))) {s = 34;}

                         
                        input.seek(index9_22);
                        if ( s>=0 ) return s;
                        break;
                    case 7 : 
                        int LA9_10 = input.LA(1);

                         
                        int index9_10 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA9_10==DOUBLE_QUOTE) && ((strict == false))) {s = 34;}

                         
                        input.seek(index9_10);
                        if ( s>=0 ) return s;
                        break;
                    case 8 : 
                        int LA9_28 = input.LA(1);

                         
                        int index9_28 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA9_28==DOUBLE_QUOTE) && ((strict == false))) {s = 34;}

                         
                        input.seek(index9_28);
                        if ( s>=0 ) return s;
                        break;
                    case 9 : 
                        int LA9_18 = input.LA(1);

                         
                        int index9_18 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA9_18==DOUBLE_QUOTE) && ((strict == false))) {s = 34;}

                         
                        input.seek(index9_18);
                        if ( s>=0 ) return s;
                        break;
                    case 10 : 
                        int LA9_8 = input.LA(1);

                         
                        int index9_8 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA9_8==DOUBLE_QUOTE) && ((strict == false))) {s = 34;}

                         
                        input.seek(index9_8);
                        if ( s>=0 ) return s;
                        break;
                    case 11 : 
                        int LA9_29 = input.LA(1);

                         
                        int index9_29 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA9_29==DOUBLE_QUOTE) && ((strict == false))) {s = 34;}

                         
                        input.seek(index9_29);
                        if ( s>=0 ) return s;
                        break;
                    case 12 : 
                        int LA9_9 = input.LA(1);

                         
                        int index9_9 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA9_9==DOUBLE_QUOTE) && ((strict == false))) {s = 34;}

                         
                        input.seek(index9_9);
                        if ( s>=0 ) return s;
                        break;
                    case 13 : 
                        int LA9_7 = input.LA(1);

                         
                        int index9_7 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA9_7==DOUBLE_QUOTE) && ((strict == false))) {s = 34;}

                         
                        input.seek(index9_7);
                        if ( s>=0 ) return s;
                        break;
                    case 14 : 
                        int LA9_30 = input.LA(1);

                         
                        int index9_30 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA9_30==DOUBLE_QUOTE) && ((strict == false))) {s = 34;}

                         
                        input.seek(index9_30);
                        if ( s>=0 ) return s;
                        break;
                    case 15 : 
                        int LA9_19 = input.LA(1);

                         
                        int index9_19 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA9_19==DOUBLE_QUOTE) && ((strict == false))) {s = 34;}

                         
                        input.seek(index9_19);
                        if ( s>=0 ) return s;
                        break;
                    case 16 : 
                        int LA9_33 = input.LA(1);

                         
                        int index9_33 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA9_33==DOUBLE_QUOTE) && ((strict == false))) {s = 34;}

                         
                        input.seek(index9_33);
                        if ( s>=0 ) return s;
                        break;
                    case 17 : 
                        int LA9_15 = input.LA(1);

                         
                        int index9_15 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA9_15==DOUBLE_QUOTE) && ((strict == false))) {s = 34;}

                         
                        input.seek(index9_15);
                        if ( s>=0 ) return s;
                        break;
                    case 18 : 
                        int LA9_6 = input.LA(1);

                         
                        int index9_6 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA9_6==DOUBLE_QUOTE) && ((strict == false))) {s = 34;}

                         
                        input.seek(index9_6);
                        if ( s>=0 ) return s;
                        break;
                    case 19 : 
                        int LA9_26 = input.LA(1);

                         
                        int index9_26 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA9_26==DOUBLE_QUOTE) && ((strict == false))) {s = 34;}

                         
                        input.seek(index9_26);
                        if ( s>=0 ) return s;
                        break;
                    case 20 : 
                        int LA9_32 = input.LA(1);

                         
                        int index9_32 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA9_32==DOUBLE_QUOTE) && ((strict == false))) {s = 34;}

                         
                        input.seek(index9_32);
                        if ( s>=0 ) return s;
                        break;
                    case 21 : 
                        int LA9_27 = input.LA(1);

                         
                        int index9_27 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA9_27==DOUBLE_QUOTE) && ((strict == false))) {s = 34;}

                         
                        input.seek(index9_27);
                        if ( s>=0 ) return s;
                        break;
                    case 22 : 
                        int LA9_17 = input.LA(1);

                         
                        int index9_17 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA9_17==DOUBLE_QUOTE) && ((strict == false))) {s = 34;}

                         
                        input.seek(index9_17);
                        if ( s>=0 ) return s;
                        break;
                    case 23 : 
                        int LA9_2 = input.LA(1);

                         
                        int index9_2 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA9_2==SELECT) && ((strict == false))) {s = 5;}

                        else if ( (LA9_2==AS) && ((strict == false))) {s = 6;}

                        else if ( (LA9_2==FROM) && ((strict == false))) {s = 7;}

                        else if ( (LA9_2==JOIN) && ((strict == false))) {s = 8;}

                        else if ( (LA9_2==INNER) && ((strict == false))) {s = 9;}

                        else if ( (LA9_2==LEFT) && ((strict == false))) {s = 10;}

                        else if ( (LA9_2==OUTER) && ((strict == false))) {s = 11;}

                        else if ( (LA9_2==ON) && ((strict == false))) {s = 12;}

                        else if ( (LA9_2==WHERE) && ((strict == false))) {s = 13;}

                        else if ( (LA9_2==OR) && ((strict == false))) {s = 14;}

                        else if ( (LA9_2==AND) && ((strict == false))) {s = 15;}

                        else if ( (LA9_2==NOT) && ((strict == false))) {s = 16;}

                        else if ( (LA9_2==IN) && ((strict == false))) {s = 17;}

                        else if ( (LA9_2==LIKE) && ((strict == false))) {s = 18;}

                        else if ( (LA9_2==IS) && ((strict == false))) {s = 19;}

                        else if ( (LA9_2==NULL) && ((strict == false))) {s = 20;}

                        else if ( (LA9_2==ANY) && ((strict == false))) {s = 21;}

                        else if ( (LA9_2==CONTAINS) && ((strict == false))) {s = 22;}

                        else if ( (LA9_2==IN_FOLDER) && ((strict == false))) {s = 23;}

                        else if ( (LA9_2==IN_TREE) && ((strict == false))) {s = 24;}

                        else if ( (LA9_2==ORDER) && ((strict == false))) {s = 25;}

                        else if ( (LA9_2==BY) && ((strict == false))) {s = 26;}

                        else if ( (LA9_2==ASC) && ((strict == false))) {s = 27;}

                        else if ( (LA9_2==DESC) && ((strict == false))) {s = 28;}

                        else if ( (LA9_2==TIMESTAMP) && ((strict == false))) {s = 29;}

                        else if ( (LA9_2==TRUE) && ((strict == false))) {s = 30;}

                        else if ( (LA9_2==FALSE) && ((strict == false))) {s = 31;}

                        else if ( (LA9_2==SCORE) && ((strict == false))) {s = 32;}

                        else if ( (LA9_2==ID) && ((strict == false))) {s = 33;}

                         
                        input.seek(index9_2);
                        if ( s>=0 ) return s;
                        break;
                    case 24 : 
                        int LA9_5 = input.LA(1);

                         
                        int index9_5 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA9_5==DOUBLE_QUOTE) && ((strict == false))) {s = 34;}

                         
                        input.seek(index9_5);
                        if ( s>=0 ) return s;
                        break;
                    case 25 : 
                        int LA9_16 = input.LA(1);

                         
                        int index9_16 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA9_16==DOUBLE_QUOTE) && ((strict == false))) {s = 34;}

                         
                        input.seek(index9_16);
                        if ( s>=0 ) return s;
                        break;
                    case 26 : 
                        int LA9_13 = input.LA(1);

                         
                        int index9_13 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA9_13==DOUBLE_QUOTE) && ((strict == false))) {s = 34;}

                         
                        input.seek(index9_13);
                        if ( s>=0 ) return s;
                        break;
                    case 27 : 
                        int LA9_23 = input.LA(1);

                         
                        int index9_23 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA9_23==DOUBLE_QUOTE) && ((strict == false))) {s = 34;}

                         
                        input.seek(index9_23);
                        if ( s>=0 ) return s;
                        break;
                    case 28 : 
                        int LA9_12 = input.LA(1);

                         
                        int index9_12 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA9_12==DOUBLE_QUOTE) && ((strict == false))) {s = 34;}

                         
                        input.seek(index9_12);
                        if ( s>=0 ) return s;
                        break;
                    case 29 : 
                        int LA9_14 = input.LA(1);

                         
                        int index9_14 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA9_14==DOUBLE_QUOTE) && ((strict == false))) {s = 34;}

                         
                        input.seek(index9_14);
                        if ( s>=0 ) return s;
                        break;
                    case 30 : 
                        int LA9_24 = input.LA(1);

                         
                        int index9_24 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA9_24==DOUBLE_QUOTE) && ((strict == false))) {s = 34;}

                         
                        input.seek(index9_24);
                        if ( s>=0 ) return s;
                        break;
                    case 31 : 
                        int LA9_25 = input.LA(1);

                         
                        int index9_25 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA9_25==DOUBLE_QUOTE) && ((strict == false))) {s = 34;}

                         
                        input.seek(index9_25);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 9, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA10_eotS =
        "\45\uffff";
    static final String DFA10_eofS =
        "\1\uffff\1\4\40\uffff\1\44\2\uffff";
    static final String DFA10_minS =
        "\1\105\1\41\1\37\2\uffff\35\106\1\41\2\uffff";
    static final String DFA10_maxS =
        "\1\106\1\77\1\114\2\uffff\35\106\1\77\2\uffff";
    static final String DFA10_acceptS =
        "\3\uffff\1\1\1\2\36\uffff\1\1\1\2";
    static final String DFA10_specialS =
        "\1\20\1\uffff\1\27\2\uffff\1\30\1\22\1\14\1\10\1\13\1\5\1\0\1\34"+
        "\1\32\1\35\1\21\1\31\1\26\1\7\1\16\1\3\1\2\1\4\1\33\1\36\1\37\1"+
        "\23\1\25\1\6\1\12\1\15\1\1\1\24\1\17\1\11\2\uffff}>";
    static final String[] DFA10_transitionS = {
            "\1\1\1\2",
            "\1\4\2\uffff\1\3\1\uffff\2\4\7\uffff\3\4\5\uffff\1\4\1\uffff"+
            "\1\4\5\uffff\1\4",
            "\1\5\2\uffff\1\6\4\uffff\1\7\1\10\1\11\1\12\1\13\1\14\1\uffff"+
            "\1\15\1\16\1\17\1\20\5\uffff\1\21\1\22\1\23\1\24\1\25\1\26\1"+
            "\27\1\30\1\31\1\32\1\33\1\34\2\uffff\1\41\3\uffff\1\36\1\37"+
            "\1\35\1\40",
            "",
            "",
            "\1\42",
            "\1\42",
            "\1\42",
            "\1\42",
            "\1\42",
            "\1\42",
            "\1\42",
            "\1\42",
            "\1\42",
            "\1\42",
            "\1\42",
            "\1\42",
            "\1\42",
            "\1\42",
            "\1\42",
            "\1\42",
            "\1\42",
            "\1\42",
            "\1\42",
            "\1\42",
            "\1\42",
            "\1\42",
            "\1\42",
            "\1\42",
            "\1\42",
            "\1\42",
            "\1\42",
            "\1\42",
            "\1\42",
            "\1\44\2\uffff\1\43\1\uffff\2\44\7\uffff\3\44\5\uffff\1\44"+
            "\1\uffff\1\44\5\uffff\1\44",
            "",
            ""
    };

    static final short[] DFA10_eot = DFA.unpackEncodedString(DFA10_eotS);
    static final short[] DFA10_eof = DFA.unpackEncodedString(DFA10_eofS);
    static final char[] DFA10_min = DFA.unpackEncodedStringToUnsignedChars(DFA10_minS);
    static final char[] DFA10_max = DFA.unpackEncodedStringToUnsignedChars(DFA10_maxS);
    static final short[] DFA10_accept = DFA.unpackEncodedString(DFA10_acceptS);
    static final short[] DFA10_special = DFA.unpackEncodedString(DFA10_specialS);
    static final short[][] DFA10_transition;

    static {
        int numStates = DFA10_transitionS.length;
        DFA10_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA10_transition[i] = DFA.unpackEncodedString(DFA10_transitionS[i]);
        }
    }

    class DFA10 extends DFA {

        public DFA10(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 10;
            this.eot = DFA10_eot;
            this.eof = DFA10_eof;
            this.min = DFA10_min;
            this.max = DFA10_max;
            this.accept = DFA10_accept;
            this.special = DFA10_special;
            this.transition = DFA10_transition;
        }
        public String getDescription() {
            return "345:10: ( qualifier DOT )?";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA10_11 = input.LA(1);

                         
                        int index10_11 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA10_11==DOUBLE_QUOTE) && ((strict == false))) {s = 34;}

                         
                        input.seek(index10_11);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA10_31 = input.LA(1);

                         
                        int index10_31 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA10_31==DOUBLE_QUOTE) && ((strict == false))) {s = 34;}

                         
                        input.seek(index10_31);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA10_21 = input.LA(1);

                         
                        int index10_21 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA10_21==DOUBLE_QUOTE) && ((strict == false))) {s = 34;}

                         
                        input.seek(index10_21);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA10_20 = input.LA(1);

                         
                        int index10_20 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA10_20==DOUBLE_QUOTE) && ((strict == false))) {s = 34;}

                         
                        input.seek(index10_20);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA10_22 = input.LA(1);

                         
                        int index10_22 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA10_22==DOUBLE_QUOTE) && ((strict == false))) {s = 34;}

                         
                        input.seek(index10_22);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA10_10 = input.LA(1);

                         
                        int index10_10 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA10_10==DOUBLE_QUOTE) && ((strict == false))) {s = 34;}

                         
                        input.seek(index10_10);
                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA10_28 = input.LA(1);

                         
                        int index10_28 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA10_28==DOUBLE_QUOTE) && ((strict == false))) {s = 34;}

                         
                        input.seek(index10_28);
                        if ( s>=0 ) return s;
                        break;
                    case 7 : 
                        int LA10_18 = input.LA(1);

                         
                        int index10_18 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA10_18==DOUBLE_QUOTE) && ((strict == false))) {s = 34;}

                         
                        input.seek(index10_18);
                        if ( s>=0 ) return s;
                        break;
                    case 8 : 
                        int LA10_8 = input.LA(1);

                         
                        int index10_8 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA10_8==DOUBLE_QUOTE) && ((strict == false))) {s = 34;}

                         
                        input.seek(index10_8);
                        if ( s>=0 ) return s;
                        break;
                    case 9 : 
                        int LA10_34 = input.LA(1);

                         
                        int index10_34 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA10_34==DOT) && ((strict == false))) {s = 35;}

                        else if ( (LA10_34==EOF||LA10_34==COMMA||(LA10_34>=RPAREN && LA10_34<=FROM)||(LA10_34>=OR && LA10_34<=NOT)||LA10_34==IN||LA10_34==IS||LA10_34==ORDER) && ((strict == false))) {s = 36;}

                         
                        input.seek(index10_34);
                        if ( s>=0 ) return s;
                        break;
                    case 10 : 
                        int LA10_29 = input.LA(1);

                         
                        int index10_29 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA10_29==DOUBLE_QUOTE) && ((strict == false))) {s = 34;}

                         
                        input.seek(index10_29);
                        if ( s>=0 ) return s;
                        break;
                    case 11 : 
                        int LA10_9 = input.LA(1);

                         
                        int index10_9 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA10_9==DOUBLE_QUOTE) && ((strict == false))) {s = 34;}

                         
                        input.seek(index10_9);
                        if ( s>=0 ) return s;
                        break;
                    case 12 : 
                        int LA10_7 = input.LA(1);

                         
                        int index10_7 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA10_7==DOUBLE_QUOTE) && ((strict == false))) {s = 34;}

                         
                        input.seek(index10_7);
                        if ( s>=0 ) return s;
                        break;
                    case 13 : 
                        int LA10_30 = input.LA(1);

                         
                        int index10_30 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA10_30==DOUBLE_QUOTE) && ((strict == false))) {s = 34;}

                         
                        input.seek(index10_30);
                        if ( s>=0 ) return s;
                        break;
                    case 14 : 
                        int LA10_19 = input.LA(1);

                         
                        int index10_19 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA10_19==DOUBLE_QUOTE) && ((strict == false))) {s = 34;}

                         
                        input.seek(index10_19);
                        if ( s>=0 ) return s;
                        break;
                    case 15 : 
                        int LA10_33 = input.LA(1);

                         
                        int index10_33 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA10_33==DOUBLE_QUOTE) && ((strict == false))) {s = 34;}

                         
                        input.seek(index10_33);
                        if ( s>=0 ) return s;
                        break;
                    case 16 : 
                        int LA10_0 = input.LA(1);

                         
                        int index10_0 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA10_0==ID) ) {s = 1;}

                        else if ( (LA10_0==DOUBLE_QUOTE) && ((strict == false))) {s = 2;}

                         
                        input.seek(index10_0);
                        if ( s>=0 ) return s;
                        break;
                    case 17 : 
                        int LA10_15 = input.LA(1);

                         
                        int index10_15 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA10_15==DOUBLE_QUOTE) && ((strict == false))) {s = 34;}

                         
                        input.seek(index10_15);
                        if ( s>=0 ) return s;
                        break;
                    case 18 : 
                        int LA10_6 = input.LA(1);

                         
                        int index10_6 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA10_6==DOUBLE_QUOTE) && ((strict == false))) {s = 34;}

                         
                        input.seek(index10_6);
                        if ( s>=0 ) return s;
                        break;
                    case 19 : 
                        int LA10_26 = input.LA(1);

                         
                        int index10_26 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA10_26==DOUBLE_QUOTE) && ((strict == false))) {s = 34;}

                         
                        input.seek(index10_26);
                        if ( s>=0 ) return s;
                        break;
                    case 20 : 
                        int LA10_32 = input.LA(1);

                         
                        int index10_32 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA10_32==DOUBLE_QUOTE) && ((strict == false))) {s = 34;}

                         
                        input.seek(index10_32);
                        if ( s>=0 ) return s;
                        break;
                    case 21 : 
                        int LA10_27 = input.LA(1);

                         
                        int index10_27 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA10_27==DOUBLE_QUOTE) && ((strict == false))) {s = 34;}

                         
                        input.seek(index10_27);
                        if ( s>=0 ) return s;
                        break;
                    case 22 : 
                        int LA10_17 = input.LA(1);

                         
                        int index10_17 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA10_17==DOUBLE_QUOTE) && ((strict == false))) {s = 34;}

                         
                        input.seek(index10_17);
                        if ( s>=0 ) return s;
                        break;
                    case 23 : 
                        int LA10_2 = input.LA(1);

                         
                        int index10_2 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA10_2==SELECT) && ((strict == false))) {s = 5;}

                        else if ( (LA10_2==AS) && ((strict == false))) {s = 6;}

                        else if ( (LA10_2==FROM) && ((strict == false))) {s = 7;}

                        else if ( (LA10_2==JOIN) && ((strict == false))) {s = 8;}

                        else if ( (LA10_2==INNER) && ((strict == false))) {s = 9;}

                        else if ( (LA10_2==LEFT) && ((strict == false))) {s = 10;}

                        else if ( (LA10_2==OUTER) && ((strict == false))) {s = 11;}

                        else if ( (LA10_2==ON) && ((strict == false))) {s = 12;}

                        else if ( (LA10_2==WHERE) && ((strict == false))) {s = 13;}

                        else if ( (LA10_2==OR) && ((strict == false))) {s = 14;}

                        else if ( (LA10_2==AND) && ((strict == false))) {s = 15;}

                        else if ( (LA10_2==NOT) && ((strict == false))) {s = 16;}

                        else if ( (LA10_2==IN) && ((strict == false))) {s = 17;}

                        else if ( (LA10_2==LIKE) && ((strict == false))) {s = 18;}

                        else if ( (LA10_2==IS) && ((strict == false))) {s = 19;}

                        else if ( (LA10_2==NULL) && ((strict == false))) {s = 20;}

                        else if ( (LA10_2==ANY) && ((strict == false))) {s = 21;}

                        else if ( (LA10_2==CONTAINS) && ((strict == false))) {s = 22;}

                        else if ( (LA10_2==IN_FOLDER) && ((strict == false))) {s = 23;}

                        else if ( (LA10_2==IN_TREE) && ((strict == false))) {s = 24;}

                        else if ( (LA10_2==ORDER) && ((strict == false))) {s = 25;}

                        else if ( (LA10_2==BY) && ((strict == false))) {s = 26;}

                        else if ( (LA10_2==ASC) && ((strict == false))) {s = 27;}

                        else if ( (LA10_2==DESC) && ((strict == false))) {s = 28;}

                        else if ( (LA10_2==TIMESTAMP) && ((strict == false))) {s = 29;}

                        else if ( (LA10_2==TRUE) && ((strict == false))) {s = 30;}

                        else if ( (LA10_2==FALSE) && ((strict == false))) {s = 31;}

                        else if ( (LA10_2==SCORE) && ((strict == false))) {s = 32;}

                        else if ( (LA10_2==ID) && ((strict == false))) {s = 33;}

                         
                        input.seek(index10_2);
                        if ( s>=0 ) return s;
                        break;
                    case 24 : 
                        int LA10_5 = input.LA(1);

                         
                        int index10_5 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA10_5==DOUBLE_QUOTE) && ((strict == false))) {s = 34;}

                         
                        input.seek(index10_5);
                        if ( s>=0 ) return s;
                        break;
                    case 25 : 
                        int LA10_16 = input.LA(1);

                         
                        int index10_16 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA10_16==DOUBLE_QUOTE) && ((strict == false))) {s = 34;}

                         
                        input.seek(index10_16);
                        if ( s>=0 ) return s;
                        break;
                    case 26 : 
                        int LA10_13 = input.LA(1);

                         
                        int index10_13 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA10_13==DOUBLE_QUOTE) && ((strict == false))) {s = 34;}

                         
                        input.seek(index10_13);
                        if ( s>=0 ) return s;
                        break;
                    case 27 : 
                        int LA10_23 = input.LA(1);

                         
                        int index10_23 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA10_23==DOUBLE_QUOTE) && ((strict == false))) {s = 34;}

                         
                        input.seek(index10_23);
                        if ( s>=0 ) return s;
                        break;
                    case 28 : 
                        int LA10_12 = input.LA(1);

                         
                        int index10_12 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA10_12==DOUBLE_QUOTE) && ((strict == false))) {s = 34;}

                         
                        input.seek(index10_12);
                        if ( s>=0 ) return s;
                        break;
                    case 29 : 
                        int LA10_14 = input.LA(1);

                         
                        int index10_14 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA10_14==DOUBLE_QUOTE) && ((strict == false))) {s = 34;}

                         
                        input.seek(index10_14);
                        if ( s>=0 ) return s;
                        break;
                    case 30 : 
                        int LA10_24 = input.LA(1);

                         
                        int index10_24 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA10_24==DOUBLE_QUOTE) && ((strict == false))) {s = 34;}

                         
                        input.seek(index10_24);
                        if ( s>=0 ) return s;
                        break;
                    case 31 : 
                        int LA10_25 = input.LA(1);

                         
                        int index10_25 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA10_25==DOUBLE_QUOTE) && ((strict == false))) {s = 34;}

                         
                        input.seek(index10_25);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 10, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA13_eotS =
        "\173\uffff";
    static final String DFA13_eofS =
        "\173\uffff";
    static final String DFA13_minS =
        "\1\37\1\45\1\uffff\1\46\1\44\1\37\5\46\1\104\1\105\2\0\1\105\35"+
        "\106\5\0\2\46\1\37\1\uffff\1\46\1\37\1\44\2\0\35\106\1\0\35\106"+
        "\1\0\2\46\2\0";
    static final String DFA13_maxS =
        "\1\114\1\45\1\uffff\2\113\1\114\5\113\1\104\1\106\2\0\36\106\5"+
        "\0\2\113\1\114\1\uffff\1\113\1\114\1\113\2\0\35\106\1\0\35\106\1"+
        "\0\2\113\2\0";
    static final String DFA13_acceptS =
        "\2\uffff\1\2\62\uffff\1\1\105\uffff";
    static final String DFA13_specialS =
        "\1\0\14\uffff\1\10\1\11\36\uffff\1\7\1\6\1\5\1\4\1\12\7\uffff\1"+
        "\13\1\14\35\uffff\1\15\35\uffff\1\3\2\uffff\1\2\1\1}>";
    static final String[] DFA13_transitionS = {
            "\1\2\2\uffff\1\2\4\uffff\6\2\1\uffff\4\2\5\uffff\14\2\2\uffff"+
            "\1\2\3\uffff\3\2\1\1",
            "\1\3",
            "",
            "\1\15\34\uffff\1\14\1\10\1\4\1\5\1\6\1\7\1\11\1\12\1\13",
            "\1\17\1\uffff\1\16\34\uffff\1\14\1\10\1\4\1\5\1\6\1\7\1\11"+
            "\1\12\1\13",
            "\1\20\2\uffff\1\21\4\uffff\1\22\1\23\1\24\1\25\1\26\1\27\1"+
            "\uffff\1\30\1\31\1\32\1\33\5\uffff\1\34\1\35\1\36\1\37\1\40"+
            "\1\41\1\42\1\43\1\44\1\45\1\46\1\47\2\uffff\1\54\3\uffff\1\51"+
            "\1\52\1\50\1\53",
            "\1\55\34\uffff\1\14\1\10\1\4\1\5\1\6\1\7\1\11\1\12\1\13",
            "\1\56\34\uffff\1\14\1\10\1\4\1\5\1\6\1\7\1\11\1\12\1\13",
            "\1\57\34\uffff\1\14\1\10\1\4\1\5\1\6\1\7\1\11\1\12\1\13",
            "\1\60\34\uffff\1\14\1\10\1\4\1\5\1\6\1\7\1\11\1\12\1\13",
            "\1\61\34\uffff\1\14\1\10\1\4\1\5\1\6\1\7\1\11\1\12\1\13",
            "\1\62",
            "\1\63\1\64",
            "\1\uffff",
            "\1\uffff",
            "\1\66\1\67",
            "\1\70",
            "\1\70",
            "\1\70",
            "\1\70",
            "\1\70",
            "\1\70",
            "\1\70",
            "\1\70",
            "\1\70",
            "\1\70",
            "\1\70",
            "\1\70",
            "\1\70",
            "\1\70",
            "\1\70",
            "\1\70",
            "\1\70",
            "\1\70",
            "\1\70",
            "\1\70",
            "\1\70",
            "\1\70",
            "\1\70",
            "\1\70",
            "\1\70",
            "\1\70",
            "\1\70",
            "\1\70",
            "\1\70",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\71\34\uffff\1\14\1\10\1\4\1\5\1\6\1\7\1\11\1\12\1\13",
            "\1\72\34\uffff\1\14\1\10\1\4\1\5\1\6\1\7\1\11\1\12\1\13",
            "\1\73\2\uffff\1\74\4\uffff\1\75\1\76\1\77\1\100\1\101\1\102"+
            "\1\uffff\1\103\1\104\1\105\1\106\5\uffff\1\107\1\110\1\111\1"+
            "\112\1\113\1\114\1\115\1\116\1\117\1\120\1\121\1\122\2\uffff"+
            "\1\127\3\uffff\1\124\1\125\1\123\1\126",
            "",
            "\1\130\34\uffff\1\14\1\10\1\4\1\5\1\6\1\7\1\11\1\12\1\13",
            "\1\131\2\uffff\1\132\4\uffff\1\133\1\134\1\135\1\136\1\137"+
            "\1\140\1\uffff\1\141\1\142\1\143\1\144\5\uffff\1\145\1\146\1"+
            "\147\1\150\1\151\1\152\1\153\1\154\1\155\1\156\1\157\1\160\2"+
            "\uffff\1\165\3\uffff\1\162\1\163\1\161\1\164",
            "\1\17\1\uffff\1\166\34\uffff\1\14\1\10\1\4\1\5\1\6\1\7\1\11"+
            "\1\12\1\13",
            "\1\uffff",
            "\1\uffff",
            "\1\167",
            "\1\167",
            "\1\167",
            "\1\167",
            "\1\167",
            "\1\167",
            "\1\167",
            "\1\167",
            "\1\167",
            "\1\167",
            "\1\167",
            "\1\167",
            "\1\167",
            "\1\167",
            "\1\167",
            "\1\167",
            "\1\167",
            "\1\167",
            "\1\167",
            "\1\167",
            "\1\167",
            "\1\167",
            "\1\167",
            "\1\167",
            "\1\167",
            "\1\167",
            "\1\167",
            "\1\167",
            "\1\167",
            "\1\uffff",
            "\1\170",
            "\1\170",
            "\1\170",
            "\1\170",
            "\1\170",
            "\1\170",
            "\1\170",
            "\1\170",
            "\1\170",
            "\1\170",
            "\1\170",
            "\1\170",
            "\1\170",
            "\1\170",
            "\1\170",
            "\1\170",
            "\1\170",
            "\1\170",
            "\1\170",
            "\1\170",
            "\1\170",
            "\1\170",
            "\1\170",
            "\1\170",
            "\1\170",
            "\1\170",
            "\1\170",
            "\1\170",
            "\1\170",
            "\1\uffff",
            "\1\171\34\uffff\1\14\1\10\1\4\1\5\1\6\1\7\1\11\1\12\1\13",
            "\1\172\34\uffff\1\14\1\10\1\4\1\5\1\6\1\7\1\11\1\12\1\13",
            "\1\uffff",
            "\1\uffff"
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
            return "349:1: valueFunction : (cmisFunctionName= cmisFunction LPAREN ( functionArgument )* RPAREN -> ^( FUNCTION $cmisFunctionName LPAREN ( functionArgument )* RPAREN ) | {...}? =>functionName= keyWordOrId LPAREN ( functionArgument )* RPAREN -> ^( FUNCTION $functionName LPAREN ( functionArgument )* RPAREN ) );";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA13_0 = input.LA(1);

                         
                        int index13_0 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA13_0==SCORE) ) {s = 1;}

                        else if ( (LA13_0==SELECT||LA13_0==AS||(LA13_0>=FROM && LA13_0<=ON)||(LA13_0>=WHERE && LA13_0<=NOT)||(LA13_0>=IN && LA13_0<=DESC)||LA13_0==ID||(LA13_0>=TRUE && LA13_0<=TIMESTAMP)) && ((strict == false))) {s = 2;}

                         
                        input.seek(index13_0);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA13_122 = input.LA(1);

                         
                        int index13_122 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (!(((strict == false)))) ) {s = 53;}

                        else if ( ((strict == false)) ) {s = 2;}

                         
                        input.seek(index13_122);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA13_121 = input.LA(1);

                         
                        int index13_121 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (!(((strict == false)))) ) {s = 53;}

                        else if ( ((strict == false)) ) {s = 2;}

                         
                        input.seek(index13_121);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA13_118 = input.LA(1);

                         
                        int index13_118 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (!(((strict == false)))) ) {s = 53;}

                        else if ( ((strict == false)) ) {s = 2;}

                         
                        input.seek(index13_118);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA13_48 = input.LA(1);

                         
                        int index13_48 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (!(((strict == false)))) ) {s = 53;}

                        else if ( ((strict == false)) ) {s = 2;}

                         
                        input.seek(index13_48);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA13_47 = input.LA(1);

                         
                        int index13_47 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (!(((strict == false)))) ) {s = 53;}

                        else if ( ((strict == false)) ) {s = 2;}

                         
                        input.seek(index13_47);
                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA13_46 = input.LA(1);

                         
                        int index13_46 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (!(((strict == false)))) ) {s = 53;}

                        else if ( ((strict == false)) ) {s = 2;}

                         
                        input.seek(index13_46);
                        if ( s>=0 ) return s;
                        break;
                    case 7 : 
                        int LA13_45 = input.LA(1);

                         
                        int index13_45 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (!(((strict == false)))) ) {s = 53;}

                        else if ( ((strict == false)) ) {s = 2;}

                         
                        input.seek(index13_45);
                        if ( s>=0 ) return s;
                        break;
                    case 8 : 
                        int LA13_13 = input.LA(1);

                         
                        int index13_13 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (!(((strict == false)))) ) {s = 53;}

                        else if ( ((strict == false)) ) {s = 2;}

                         
                        input.seek(index13_13);
                        if ( s>=0 ) return s;
                        break;
                    case 9 : 
                        int LA13_14 = input.LA(1);

                         
                        int index13_14 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (!(((strict == false)))) ) {s = 53;}

                        else if ( ((strict == false)) ) {s = 2;}

                         
                        input.seek(index13_14);
                        if ( s>=0 ) return s;
                        break;
                    case 10 : 
                        int LA13_49 = input.LA(1);

                         
                        int index13_49 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (!(((strict == false)))) ) {s = 53;}

                        else if ( ((strict == false)) ) {s = 2;}

                         
                        input.seek(index13_49);
                        if ( s>=0 ) return s;
                        break;
                    case 11 : 
                        int LA13_57 = input.LA(1);

                         
                        int index13_57 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (!(((strict == false)))) ) {s = 53;}

                        else if ( ((strict == false)) ) {s = 2;}

                         
                        input.seek(index13_57);
                        if ( s>=0 ) return s;
                        break;
                    case 12 : 
                        int LA13_58 = input.LA(1);

                         
                        int index13_58 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (!(((strict == false)))) ) {s = 53;}

                        else if ( ((strict == false)) ) {s = 2;}

                         
                        input.seek(index13_58);
                        if ( s>=0 ) return s;
                        break;
                    case 13 : 
                        int LA13_88 = input.LA(1);

                         
                        int index13_88 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (!(((strict == false)))) ) {s = 53;}

                        else if ( ((strict == false)) ) {s = 2;}

                         
                        input.seek(index13_88);
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
        "\47\uffff";
    static final String DFA14_eofS =
        "\47\uffff";
    static final String DFA14_minS =
        "\1\103\1\44\1\37\4\uffff\35\106\1\44\2\uffff";
    static final String DFA14_maxS =
        "\2\113\1\114\4\uffff\35\106\1\113\2\uffff";
    static final String DFA14_acceptS =
        "\3\uffff\2\3\1\2\1\1\36\uffff\1\1\1\2";
    static final String DFA14_specialS =
        "\1\26\1\uffff\1\25\4\uffff\1\27\1\20\1\13\1\10\1\12\1\5\1\0\1\34"+
        "\1\33\1\36\1\17\1\31\1\24\1\7\1\15\1\3\1\2\1\4\1\32\1\35\1\37\1"+
        "\21\1\23\1\6\1\11\1\14\1\1\1\22\1\16\1\30\2\uffff}>";
    static final String[] DFA14_transitionS = {
            "\1\4\1\3\1\1\1\2\5\3",
            "\1\6\1\uffff\1\5\34\uffff\11\5",
            "\1\7\2\uffff\1\10\4\uffff\1\11\1\12\1\13\1\14\1\15\1\16\1"+
            "\uffff\1\17\1\20\1\21\1\22\5\uffff\1\23\1\24\1\25\1\26\1\27"+
            "\1\30\1\31\1\32\1\33\1\34\1\35\1\36\2\uffff\1\43\3\uffff\1\40"+
            "\1\41\1\37\1\42",
            "",
            "",
            "",
            "",
            "\1\44",
            "\1\44",
            "\1\44",
            "\1\44",
            "\1\44",
            "\1\44",
            "\1\44",
            "\1\44",
            "\1\44",
            "\1\44",
            "\1\44",
            "\1\44",
            "\1\44",
            "\1\44",
            "\1\44",
            "\1\44",
            "\1\44",
            "\1\44",
            "\1\44",
            "\1\44",
            "\1\44",
            "\1\44",
            "\1\44",
            "\1\44",
            "\1\44",
            "\1\44",
            "\1\44",
            "\1\44",
            "\1\44",
            "\1\45\1\uffff\1\46\34\uffff\11\46",
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
            return "358:1: functionArgument : ( qualifier DOT columnName -> ^( COLUMN_REF columnName qualifier ) | identifier | literalOrParameterName );";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA14_13 = input.LA(1);

                         
                        int index14_13 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA14_13==DOUBLE_QUOTE) && ((strict == false))) {s = 36;}

                         
                        input.seek(index14_13);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA14_33 = input.LA(1);

                         
                        int index14_33 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA14_33==DOUBLE_QUOTE) && ((strict == false))) {s = 36;}

                         
                        input.seek(index14_33);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA14_23 = input.LA(1);

                         
                        int index14_23 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA14_23==DOUBLE_QUOTE) && ((strict == false))) {s = 36;}

                         
                        input.seek(index14_23);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA14_22 = input.LA(1);

                         
                        int index14_22 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA14_22==DOUBLE_QUOTE) && ((strict == false))) {s = 36;}

                         
                        input.seek(index14_22);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA14_24 = input.LA(1);

                         
                        int index14_24 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA14_24==DOUBLE_QUOTE) && ((strict == false))) {s = 36;}

                         
                        input.seek(index14_24);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA14_12 = input.LA(1);

                         
                        int index14_12 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA14_12==DOUBLE_QUOTE) && ((strict == false))) {s = 36;}

                         
                        input.seek(index14_12);
                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA14_30 = input.LA(1);

                         
                        int index14_30 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA14_30==DOUBLE_QUOTE) && ((strict == false))) {s = 36;}

                         
                        input.seek(index14_30);
                        if ( s>=0 ) return s;
                        break;
                    case 7 : 
                        int LA14_20 = input.LA(1);

                         
                        int index14_20 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA14_20==DOUBLE_QUOTE) && ((strict == false))) {s = 36;}

                         
                        input.seek(index14_20);
                        if ( s>=0 ) return s;
                        break;
                    case 8 : 
                        int LA14_10 = input.LA(1);

                         
                        int index14_10 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA14_10==DOUBLE_QUOTE) && ((strict == false))) {s = 36;}

                         
                        input.seek(index14_10);
                        if ( s>=0 ) return s;
                        break;
                    case 9 : 
                        int LA14_31 = input.LA(1);

                         
                        int index14_31 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA14_31==DOUBLE_QUOTE) && ((strict == false))) {s = 36;}

                         
                        input.seek(index14_31);
                        if ( s>=0 ) return s;
                        break;
                    case 10 : 
                        int LA14_11 = input.LA(1);

                         
                        int index14_11 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA14_11==DOUBLE_QUOTE) && ((strict == false))) {s = 36;}

                         
                        input.seek(index14_11);
                        if ( s>=0 ) return s;
                        break;
                    case 11 : 
                        int LA14_9 = input.LA(1);

                         
                        int index14_9 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA14_9==DOUBLE_QUOTE) && ((strict == false))) {s = 36;}

                         
                        input.seek(index14_9);
                        if ( s>=0 ) return s;
                        break;
                    case 12 : 
                        int LA14_32 = input.LA(1);

                         
                        int index14_32 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA14_32==DOUBLE_QUOTE) && ((strict == false))) {s = 36;}

                         
                        input.seek(index14_32);
                        if ( s>=0 ) return s;
                        break;
                    case 13 : 
                        int LA14_21 = input.LA(1);

                         
                        int index14_21 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA14_21==DOUBLE_QUOTE) && ((strict == false))) {s = 36;}

                         
                        input.seek(index14_21);
                        if ( s>=0 ) return s;
                        break;
                    case 14 : 
                        int LA14_35 = input.LA(1);

                         
                        int index14_35 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA14_35==DOUBLE_QUOTE) && ((strict == false))) {s = 36;}

                         
                        input.seek(index14_35);
                        if ( s>=0 ) return s;
                        break;
                    case 15 : 
                        int LA14_17 = input.LA(1);

                         
                        int index14_17 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA14_17==DOUBLE_QUOTE) && ((strict == false))) {s = 36;}

                         
                        input.seek(index14_17);
                        if ( s>=0 ) return s;
                        break;
                    case 16 : 
                        int LA14_8 = input.LA(1);

                         
                        int index14_8 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA14_8==DOUBLE_QUOTE) && ((strict == false))) {s = 36;}

                         
                        input.seek(index14_8);
                        if ( s>=0 ) return s;
                        break;
                    case 17 : 
                        int LA14_28 = input.LA(1);

                         
                        int index14_28 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA14_28==DOUBLE_QUOTE) && ((strict == false))) {s = 36;}

                         
                        input.seek(index14_28);
                        if ( s>=0 ) return s;
                        break;
                    case 18 : 
                        int LA14_34 = input.LA(1);

                         
                        int index14_34 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA14_34==DOUBLE_QUOTE) && ((strict == false))) {s = 36;}

                         
                        input.seek(index14_34);
                        if ( s>=0 ) return s;
                        break;
                    case 19 : 
                        int LA14_29 = input.LA(1);

                         
                        int index14_29 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA14_29==DOUBLE_QUOTE) && ((strict == false))) {s = 36;}

                         
                        input.seek(index14_29);
                        if ( s>=0 ) return s;
                        break;
                    case 20 : 
                        int LA14_19 = input.LA(1);

                         
                        int index14_19 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA14_19==DOUBLE_QUOTE) && ((strict == false))) {s = 36;}

                         
                        input.seek(index14_19);
                        if ( s>=0 ) return s;
                        break;
                    case 21 : 
                        int LA14_2 = input.LA(1);

                         
                        int index14_2 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA14_2==SELECT) && ((strict == false))) {s = 7;}

                        else if ( (LA14_2==AS) && ((strict == false))) {s = 8;}

                        else if ( (LA14_2==FROM) && ((strict == false))) {s = 9;}

                        else if ( (LA14_2==JOIN) && ((strict == false))) {s = 10;}

                        else if ( (LA14_2==INNER) && ((strict == false))) {s = 11;}

                        else if ( (LA14_2==LEFT) && ((strict == false))) {s = 12;}

                        else if ( (LA14_2==OUTER) && ((strict == false))) {s = 13;}

                        else if ( (LA14_2==ON) && ((strict == false))) {s = 14;}

                        else if ( (LA14_2==WHERE) && ((strict == false))) {s = 15;}

                        else if ( (LA14_2==OR) && ((strict == false))) {s = 16;}

                        else if ( (LA14_2==AND) && ((strict == false))) {s = 17;}

                        else if ( (LA14_2==NOT) && ((strict == false))) {s = 18;}

                        else if ( (LA14_2==IN) && ((strict == false))) {s = 19;}

                        else if ( (LA14_2==LIKE) && ((strict == false))) {s = 20;}

                        else if ( (LA14_2==IS) && ((strict == false))) {s = 21;}

                        else if ( (LA14_2==NULL) && ((strict == false))) {s = 22;}

                        else if ( (LA14_2==ANY) && ((strict == false))) {s = 23;}

                        else if ( (LA14_2==CONTAINS) && ((strict == false))) {s = 24;}

                        else if ( (LA14_2==IN_FOLDER) && ((strict == false))) {s = 25;}

                        else if ( (LA14_2==IN_TREE) && ((strict == false))) {s = 26;}

                        else if ( (LA14_2==ORDER) && ((strict == false))) {s = 27;}

                        else if ( (LA14_2==BY) && ((strict == false))) {s = 28;}

                        else if ( (LA14_2==ASC) && ((strict == false))) {s = 29;}

                        else if ( (LA14_2==DESC) && ((strict == false))) {s = 30;}

                        else if ( (LA14_2==TIMESTAMP) && ((strict == false))) {s = 31;}

                        else if ( (LA14_2==TRUE) && ((strict == false))) {s = 32;}

                        else if ( (LA14_2==FALSE) && ((strict == false))) {s = 33;}

                        else if ( (LA14_2==SCORE) && ((strict == false))) {s = 34;}

                        else if ( (LA14_2==ID) && ((strict == false))) {s = 35;}

                         
                        input.seek(index14_2);
                        if ( s>=0 ) return s;
                        break;
                    case 22 : 
                        int LA14_0 = input.LA(1);

                         
                        int index14_0 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA14_0==ID) ) {s = 1;}

                        else if ( (LA14_0==DOUBLE_QUOTE) && ((strict == false))) {s = 2;}

                        else if ( (LA14_0==QUOTED_STRING||(LA14_0>=FLOATING_POINT_LITERAL && LA14_0<=TIMESTAMP)) ) {s = 3;}

                        else if ( (LA14_0==COLON) && ((strict == false))) {s = 4;}

                         
                        input.seek(index14_0);
                        if ( s>=0 ) return s;
                        break;
                    case 23 : 
                        int LA14_7 = input.LA(1);

                         
                        int index14_7 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA14_7==DOUBLE_QUOTE) && ((strict == false))) {s = 36;}

                         
                        input.seek(index14_7);
                        if ( s>=0 ) return s;
                        break;
                    case 24 : 
                        int LA14_36 = input.LA(1);

                         
                        int index14_36 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA14_36==DOT) && ((strict == false))) {s = 37;}

                        else if ( (LA14_36==RPAREN||(LA14_36>=COLON && LA14_36<=TIMESTAMP)) && ((strict == false))) {s = 38;}

                         
                        input.seek(index14_36);
                        if ( s>=0 ) return s;
                        break;
                    case 25 : 
                        int LA14_18 = input.LA(1);

                         
                        int index14_18 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA14_18==DOUBLE_QUOTE) && ((strict == false))) {s = 36;}

                         
                        input.seek(index14_18);
                        if ( s>=0 ) return s;
                        break;
                    case 26 : 
                        int LA14_25 = input.LA(1);

                         
                        int index14_25 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA14_25==DOUBLE_QUOTE) && ((strict == false))) {s = 36;}

                         
                        input.seek(index14_25);
                        if ( s>=0 ) return s;
                        break;
                    case 27 : 
                        int LA14_15 = input.LA(1);

                         
                        int index14_15 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA14_15==DOUBLE_QUOTE) && ((strict == false))) {s = 36;}

                         
                        input.seek(index14_15);
                        if ( s>=0 ) return s;
                        break;
                    case 28 : 
                        int LA14_14 = input.LA(1);

                         
                        int index14_14 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA14_14==DOUBLE_QUOTE) && ((strict == false))) {s = 36;}

                         
                        input.seek(index14_14);
                        if ( s>=0 ) return s;
                        break;
                    case 29 : 
                        int LA14_26 = input.LA(1);

                         
                        int index14_26 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA14_26==DOUBLE_QUOTE) && ((strict == false))) {s = 36;}

                         
                        input.seek(index14_26);
                        if ( s>=0 ) return s;
                        break;
                    case 30 : 
                        int LA14_16 = input.LA(1);

                         
                        int index14_16 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA14_16==DOUBLE_QUOTE) && ((strict == false))) {s = 36;}

                         
                        input.seek(index14_16);
                        if ( s>=0 ) return s;
                        break;
                    case 31 : 
                        int LA14_27 = input.LA(1);

                         
                        int index14_27 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA14_27==DOUBLE_QUOTE) && ((strict == false))) {s = 36;}

                         
                        input.seek(index14_27);
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
        "\43\uffff";
    static final String DFA15_eofS =
        "\43\uffff";
    static final String DFA15_minS =
        "\1\105\1\0\1\37\2\uffff\35\106\1\0";
    static final String DFA15_maxS =
        "\1\106\1\0\1\114\2\uffff\35\106\1\0";
    static final String DFA15_acceptS =
        "\3\uffff\1\1\1\2\36\uffff";
    static final String DFA15_specialS =
        "\1\36\1\16\1\5\2\uffff\1\32\1\34\1\37\1\17\1\21\1\23\1\25\1\10"+
        "\1\6\1\3\1\1\1\15\1\14\1\13\1\12\1\27\1\24\1\22\1\20\1\40\1\35\1"+
        "\33\1\31\1\4\1\7\1\0\1\2\1\30\1\11\1\26}>";
    static final String[] DFA15_transitionS = {
            "\1\1\1\2",
            "\1\uffff",
            "\1\5\2\uffff\1\6\4\uffff\1\7\1\10\1\11\1\12\1\13\1\14\1\uffff"+
            "\1\15\1\16\1\17\1\20\5\uffff\1\21\1\22\1\23\1\24\1\25\1\26\1"+
            "\27\1\30\1\31\1\32\1\33\1\34\2\uffff\1\41\3\uffff\1\36\1\37"+
            "\1\35\1\40",
            "",
            "",
            "\1\42",
            "\1\42",
            "\1\42",
            "\1\42",
            "\1\42",
            "\1\42",
            "\1\42",
            "\1\42",
            "\1\42",
            "\1\42",
            "\1\42",
            "\1\42",
            "\1\42",
            "\1\42",
            "\1\42",
            "\1\42",
            "\1\42",
            "\1\42",
            "\1\42",
            "\1\42",
            "\1\42",
            "\1\42",
            "\1\42",
            "\1\42",
            "\1\42",
            "\1\42",
            "\1\42",
            "\1\42",
            "\1\42",
            "\1\uffff"
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
            return "365:1: qualifier : ( ( tableName )=> tableName -> tableName | correlationName -> correlationName );";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA15_30 = input.LA(1);

                         
                        int index15_30 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA15_30==DOUBLE_QUOTE) && ((strict == false))) {s = 34;}

                         
                        input.seek(index15_30);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA15_15 = input.LA(1);

                         
                        int index15_15 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA15_15==DOUBLE_QUOTE) && ((strict == false))) {s = 34;}

                         
                        input.seek(index15_15);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA15_31 = input.LA(1);

                         
                        int index15_31 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA15_31==DOUBLE_QUOTE) && ((strict == false))) {s = 34;}

                         
                        input.seek(index15_31);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA15_14 = input.LA(1);

                         
                        int index15_14 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA15_14==DOUBLE_QUOTE) && ((strict == false))) {s = 34;}

                         
                        input.seek(index15_14);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA15_28 = input.LA(1);

                         
                        int index15_28 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA15_28==DOUBLE_QUOTE) && ((strict == false))) {s = 34;}

                         
                        input.seek(index15_28);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA15_2 = input.LA(1);

                         
                        int index15_2 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA15_2==SELECT) && ((strict == false))) {s = 5;}

                        else if ( (LA15_2==AS) && ((strict == false))) {s = 6;}

                        else if ( (LA15_2==FROM) && ((strict == false))) {s = 7;}

                        else if ( (LA15_2==JOIN) && ((strict == false))) {s = 8;}

                        else if ( (LA15_2==INNER) && ((strict == false))) {s = 9;}

                        else if ( (LA15_2==LEFT) && ((strict == false))) {s = 10;}

                        else if ( (LA15_2==OUTER) && ((strict == false))) {s = 11;}

                        else if ( (LA15_2==ON) && ((strict == false))) {s = 12;}

                        else if ( (LA15_2==WHERE) && ((strict == false))) {s = 13;}

                        else if ( (LA15_2==OR) && ((strict == false))) {s = 14;}

                        else if ( (LA15_2==AND) && ((strict == false))) {s = 15;}

                        else if ( (LA15_2==NOT) && ((strict == false))) {s = 16;}

                        else if ( (LA15_2==IN) && ((strict == false))) {s = 17;}

                        else if ( (LA15_2==LIKE) && ((strict == false))) {s = 18;}

                        else if ( (LA15_2==IS) && ((strict == false))) {s = 19;}

                        else if ( (LA15_2==NULL) && ((strict == false))) {s = 20;}

                        else if ( (LA15_2==ANY) && ((strict == false))) {s = 21;}

                        else if ( (LA15_2==CONTAINS) && ((strict == false))) {s = 22;}

                        else if ( (LA15_2==IN_FOLDER) && ((strict == false))) {s = 23;}

                        else if ( (LA15_2==IN_TREE) && ((strict == false))) {s = 24;}

                        else if ( (LA15_2==ORDER) && ((strict == false))) {s = 25;}

                        else if ( (LA15_2==BY) && ((strict == false))) {s = 26;}

                        else if ( (LA15_2==ASC) && ((strict == false))) {s = 27;}

                        else if ( (LA15_2==DESC) && ((strict == false))) {s = 28;}

                        else if ( (LA15_2==TIMESTAMP) && ((strict == false))) {s = 29;}

                        else if ( (LA15_2==TRUE) && ((strict == false))) {s = 30;}

                        else if ( (LA15_2==FALSE) && ((strict == false))) {s = 31;}

                        else if ( (LA15_2==SCORE) && ((strict == false))) {s = 32;}

                        else if ( (LA15_2==ID) && ((strict == false))) {s = 33;}

                         
                        input.seek(index15_2);
                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA15_13 = input.LA(1);

                         
                        int index15_13 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA15_13==DOUBLE_QUOTE) && ((strict == false))) {s = 34;}

                         
                        input.seek(index15_13);
                        if ( s>=0 ) return s;
                        break;
                    case 7 : 
                        int LA15_29 = input.LA(1);

                         
                        int index15_29 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA15_29==DOUBLE_QUOTE) && ((strict == false))) {s = 34;}

                         
                        input.seek(index15_29);
                        if ( s>=0 ) return s;
                        break;
                    case 8 : 
                        int LA15_12 = input.LA(1);

                         
                        int index15_12 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA15_12==DOUBLE_QUOTE) && ((strict == false))) {s = 34;}

                         
                        input.seek(index15_12);
                        if ( s>=0 ) return s;
                        break;
                    case 9 : 
                        int LA15_33 = input.LA(1);

                         
                        int index15_33 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA15_33==DOUBLE_QUOTE) && ((strict == false))) {s = 34;}

                         
                        input.seek(index15_33);
                        if ( s>=0 ) return s;
                        break;
                    case 10 : 
                        int LA15_19 = input.LA(1);

                         
                        int index15_19 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA15_19==DOUBLE_QUOTE) && ((strict == false))) {s = 34;}

                         
                        input.seek(index15_19);
                        if ( s>=0 ) return s;
                        break;
                    case 11 : 
                        int LA15_18 = input.LA(1);

                         
                        int index15_18 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA15_18==DOUBLE_QUOTE) && ((strict == false))) {s = 34;}

                         
                        input.seek(index15_18);
                        if ( s>=0 ) return s;
                        break;
                    case 12 : 
                        int LA15_17 = input.LA(1);

                         
                        int index15_17 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA15_17==DOUBLE_QUOTE) && ((strict == false))) {s = 34;}

                         
                        input.seek(index15_17);
                        if ( s>=0 ) return s;
                        break;
                    case 13 : 
                        int LA15_16 = input.LA(1);

                         
                        int index15_16 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA15_16==DOUBLE_QUOTE) && ((strict == false))) {s = 34;}

                         
                        input.seek(index15_16);
                        if ( s>=0 ) return s;
                        break;
                    case 14 : 
                        int LA15_1 = input.LA(1);

                         
                        int index15_1 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_CMIS()) ) {s = 3;}

                        else if ( (true) ) {s = 4;}

                         
                        input.seek(index15_1);
                        if ( s>=0 ) return s;
                        break;
                    case 15 : 
                        int LA15_8 = input.LA(1);

                         
                        int index15_8 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA15_8==DOUBLE_QUOTE) && ((strict == false))) {s = 34;}

                         
                        input.seek(index15_8);
                        if ( s>=0 ) return s;
                        break;
                    case 16 : 
                        int LA15_23 = input.LA(1);

                         
                        int index15_23 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA15_23==DOUBLE_QUOTE) && ((strict == false))) {s = 34;}

                         
                        input.seek(index15_23);
                        if ( s>=0 ) return s;
                        break;
                    case 17 : 
                        int LA15_9 = input.LA(1);

                         
                        int index15_9 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA15_9==DOUBLE_QUOTE) && ((strict == false))) {s = 34;}

                         
                        input.seek(index15_9);
                        if ( s>=0 ) return s;
                        break;
                    case 18 : 
                        int LA15_22 = input.LA(1);

                         
                        int index15_22 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA15_22==DOUBLE_QUOTE) && ((strict == false))) {s = 34;}

                         
                        input.seek(index15_22);
                        if ( s>=0 ) return s;
                        break;
                    case 19 : 
                        int LA15_10 = input.LA(1);

                         
                        int index15_10 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA15_10==DOUBLE_QUOTE) && ((strict == false))) {s = 34;}

                         
                        input.seek(index15_10);
                        if ( s>=0 ) return s;
                        break;
                    case 20 : 
                        int LA15_21 = input.LA(1);

                         
                        int index15_21 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA15_21==DOUBLE_QUOTE) && ((strict == false))) {s = 34;}

                         
                        input.seek(index15_21);
                        if ( s>=0 ) return s;
                        break;
                    case 21 : 
                        int LA15_11 = input.LA(1);

                         
                        int index15_11 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA15_11==DOUBLE_QUOTE) && ((strict == false))) {s = 34;}

                         
                        input.seek(index15_11);
                        if ( s>=0 ) return s;
                        break;
                    case 22 : 
                        int LA15_34 = input.LA(1);

                         
                        int index15_34 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((synpred2_CMIS()&&(strict == false))) ) {s = 3;}

                        else if ( ((strict == false)) ) {s = 4;}

                         
                        input.seek(index15_34);
                        if ( s>=0 ) return s;
                        break;
                    case 23 : 
                        int LA15_20 = input.LA(1);

                         
                        int index15_20 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA15_20==DOUBLE_QUOTE) && ((strict == false))) {s = 34;}

                         
                        input.seek(index15_20);
                        if ( s>=0 ) return s;
                        break;
                    case 24 : 
                        int LA15_32 = input.LA(1);

                         
                        int index15_32 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA15_32==DOUBLE_QUOTE) && ((strict == false))) {s = 34;}

                         
                        input.seek(index15_32);
                        if ( s>=0 ) return s;
                        break;
                    case 25 : 
                        int LA15_27 = input.LA(1);

                         
                        int index15_27 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA15_27==DOUBLE_QUOTE) && ((strict == false))) {s = 34;}

                         
                        input.seek(index15_27);
                        if ( s>=0 ) return s;
                        break;
                    case 26 : 
                        int LA15_5 = input.LA(1);

                         
                        int index15_5 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA15_5==DOUBLE_QUOTE) && ((strict == false))) {s = 34;}

                         
                        input.seek(index15_5);
                        if ( s>=0 ) return s;
                        break;
                    case 27 : 
                        int LA15_26 = input.LA(1);

                         
                        int index15_26 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA15_26==DOUBLE_QUOTE) && ((strict == false))) {s = 34;}

                         
                        input.seek(index15_26);
                        if ( s>=0 ) return s;
                        break;
                    case 28 : 
                        int LA15_6 = input.LA(1);

                         
                        int index15_6 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA15_6==DOUBLE_QUOTE) && ((strict == false))) {s = 34;}

                         
                        input.seek(index15_6);
                        if ( s>=0 ) return s;
                        break;
                    case 29 : 
                        int LA15_25 = input.LA(1);

                         
                        int index15_25 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA15_25==DOUBLE_QUOTE) && ((strict == false))) {s = 34;}

                         
                        input.seek(index15_25);
                        if ( s>=0 ) return s;
                        break;
                    case 30 : 
                        int LA15_0 = input.LA(1);

                         
                        int index15_0 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA15_0==ID) ) {s = 1;}

                        else if ( (LA15_0==DOUBLE_QUOTE) && ((strict == false))) {s = 2;}

                         
                        input.seek(index15_0);
                        if ( s>=0 ) return s;
                        break;
                    case 31 : 
                        int LA15_7 = input.LA(1);

                         
                        int index15_7 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA15_7==DOUBLE_QUOTE) && ((strict == false))) {s = 34;}

                         
                        input.seek(index15_7);
                        if ( s>=0 ) return s;
                        break;
                    case 32 : 
                        int LA15_24 = input.LA(1);

                         
                        int index15_24 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA15_24==DOUBLE_QUOTE) && ((strict == false))) {s = 34;}

                         
                        input.seek(index15_24);
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
    static final String DFA26_eotS =
        "\167\uffff";
    static final String DFA26_eofS =
        "\167\uffff";
    static final String DFA26_minS =
        "\2\37\12\uffff\1\37\1\44\1\37\3\45\3\46\1\105\1\uffff\1\105\35"+
        "\106\2\46\1\37\1\46\1\37\1\44\72\106\2\46";
    static final String DFA26_maxS =
        "\2\114\12\uffff\1\114\1\113\1\114\1\104\5\113\1\106\1\uffff\36"+
        "\106\2\113\1\114\1\113\1\114\1\113\72\106\2\113";
    static final String DFA26_acceptS =
        "\2\uffff\11\2\1\1\12\uffff\1\2\140\uffff";
    static final String DFA26_specialS =
        "\1\15\13\uffff\1\7\1\5\2\uffff\1\3\1\4\1\6\1\0\1\1\40\uffff\1\2"+
        "\1\11\1\uffff\1\10\1\uffff\1\14\72\uffff\1\12\1\13}>";
    static final String[] DFA26_transitionS = {
            "\1\5\2\uffff\1\5\2\uffff\1\12\1\uffff\6\5\1\uffff\3\5\1\1\5"+
            "\uffff\4\5\4\6\4\7\1\11\1\10\1\2\1\3\5\10\1\4",
            "\1\13\2\uffff\1\13\2\uffff\1\14\1\uffff\6\13\1\uffff\4\13"+
            "\5\uffff\26\13",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\13\2\uffff\1\13\2\uffff\1\13\1\26\6\13\1\uffff\4\13\5\uffff"+
            "\14\13\1\25\1\24\1\15\1\16\1\22\1\23\1\20\1\21\1\17\1\13",
            "\1\27\1\13\1\26\6\uffff\1\13\3\uffff\11\13\11\uffff\11\26",
            "\1\30\2\uffff\1\31\4\uffff\1\32\1\33\1\34\1\35\1\36\1\37\1"+
            "\uffff\1\40\1\41\1\42\1\43\5\uffff\1\44\1\45\1\46\1\47\1\50"+
            "\1\51\1\52\1\53\1\54\1\55\1\56\1\57\2\uffff\1\64\3\uffff\1\61"+
            "\1\62\1\60\1\63",
            "\1\13\36\uffff\1\65",
            "\1\13\1\26\6\uffff\1\13\4\uffff\5\13\14\uffff\11\26",
            "\1\13\1\26\6\uffff\1\13\4\uffff\5\13\14\uffff\11\26",
            "\1\26\6\uffff\1\13\4\uffff\5\13\14\uffff\11\26",
            "\1\26\6\uffff\1\13\4\uffff\5\13\14\uffff\11\26",
            "\1\26\6\uffff\1\13\4\uffff\5\13\14\uffff\11\26",
            "\1\66\1\67",
            "",
            "\1\70\1\71",
            "\1\72",
            "\1\72",
            "\1\72",
            "\1\72",
            "\1\72",
            "\1\72",
            "\1\72",
            "\1\72",
            "\1\72",
            "\1\72",
            "\1\72",
            "\1\72",
            "\1\72",
            "\1\72",
            "\1\72",
            "\1\72",
            "\1\72",
            "\1\72",
            "\1\72",
            "\1\72",
            "\1\72",
            "\1\72",
            "\1\72",
            "\1\72",
            "\1\72",
            "\1\72",
            "\1\72",
            "\1\72",
            "\1\72",
            "\1\26\6\uffff\1\13\4\uffff\5\13\14\uffff\11\26",
            "\1\26\6\uffff\1\13\4\uffff\5\13\14\uffff\11\26",
            "\1\73\2\uffff\1\74\4\uffff\1\75\1\76\1\77\1\100\1\101\1\102"+
            "\1\uffff\1\103\1\104\1\105\1\106\5\uffff\1\107\1\110\1\111\1"+
            "\112\1\113\1\114\1\115\1\116\1\117\1\120\1\121\1\122\2\uffff"+
            "\1\127\3\uffff\1\124\1\125\1\123\1\126",
            "\1\26\6\uffff\1\13\3\uffff\11\13\11\uffff\11\26",
            "\1\130\2\uffff\1\131\4\uffff\1\132\1\133\1\134\1\135\1\136"+
            "\1\137\1\uffff\1\140\1\141\1\142\1\143\5\uffff\1\144\1\145\1"+
            "\146\1\147\1\150\1\151\1\152\1\153\1\154\1\155\1\156\1\157\2"+
            "\uffff\1\164\3\uffff\1\161\1\162\1\160\1\163",
            "\1\27\1\uffff\1\26\6\uffff\1\13\3\uffff\11\13\11\uffff\11"+
            "\26",
            "\1\165",
            "\1\165",
            "\1\165",
            "\1\165",
            "\1\165",
            "\1\165",
            "\1\165",
            "\1\165",
            "\1\165",
            "\1\165",
            "\1\165",
            "\1\165",
            "\1\165",
            "\1\165",
            "\1\165",
            "\1\165",
            "\1\165",
            "\1\165",
            "\1\165",
            "\1\165",
            "\1\165",
            "\1\165",
            "\1\165",
            "\1\165",
            "\1\165",
            "\1\165",
            "\1\165",
            "\1\165",
            "\1\165",
            "\1\166",
            "\1\166",
            "\1\166",
            "\1\166",
            "\1\166",
            "\1\166",
            "\1\166",
            "\1\166",
            "\1\166",
            "\1\166",
            "\1\166",
            "\1\166",
            "\1\166",
            "\1\166",
            "\1\166",
            "\1\166",
            "\1\166",
            "\1\166",
            "\1\166",
            "\1\166",
            "\1\166",
            "\1\166",
            "\1\166",
            "\1\166",
            "\1\166",
            "\1\166",
            "\1\166",
            "\1\166",
            "\1\166",
            "\1\26\6\uffff\1\13\4\uffff\5\13\14\uffff\11\26",
            "\1\26\6\uffff\1\13\3\uffff\11\13\11\uffff\11\26"
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
            return "449:1: searchNotCondition : ( NOT searchTest -> ^( NEGATION searchTest ) | searchTest -> searchTest );";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA26_19 = input.LA(1);

                         
                        int index26_19 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA26_19==RPAREN||(LA26_19>=COLON && LA26_19<=TIMESTAMP)) && ((strict == false))) {s = 22;}

                        else if ( (LA26_19==EQUALS||(LA26_19>=NOTEQUALS && LA26_19<=GREATERTHANOREQUALS)) ) {s = 11;}

                         
                        input.seek(index26_19);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA26_20 = input.LA(1);

                         
                        int index26_20 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA26_20==RPAREN||(LA26_20>=COLON && LA26_20<=TIMESTAMP)) && ((strict == false))) {s = 22;}

                        else if ( (LA26_20==EQUALS||(LA26_20>=NOTEQUALS && LA26_20<=GREATERTHANOREQUALS)) ) {s = 11;}

                         
                        input.seek(index26_20);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA26_53 = input.LA(1);

                         
                        int index26_53 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA26_53==RPAREN||(LA26_53>=COLON && LA26_53<=TIMESTAMP)) && ((strict == false))) {s = 22;}

                        else if ( (LA26_53==EQUALS||(LA26_53>=NOTEQUALS && LA26_53<=GREATERTHANOREQUALS)) ) {s = 11;}

                         
                        input.seek(index26_53);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA26_16 = input.LA(1);

                         
                        int index26_16 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA26_16==RPAREN||(LA26_16>=COLON && LA26_16<=TIMESTAMP)) && ((strict == false))) {s = 22;}

                        else if ( (LA26_16==LPAREN||LA26_16==EQUALS||(LA26_16>=NOTEQUALS && LA26_16<=GREATERTHANOREQUALS)) ) {s = 11;}

                         
                        input.seek(index26_16);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA26_17 = input.LA(1);

                         
                        int index26_17 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA26_17==LPAREN||LA26_17==EQUALS||(LA26_17>=NOTEQUALS && LA26_17<=GREATERTHANOREQUALS)) ) {s = 11;}

                        else if ( (LA26_17==RPAREN||(LA26_17>=COLON && LA26_17<=TIMESTAMP)) && ((strict == false))) {s = 22;}

                         
                        input.seek(index26_17);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA26_13 = input.LA(1);

                         
                        int index26_13 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA26_13==LPAREN||LA26_13==EQUALS||(LA26_13>=NOT && LA26_13<=IS)) ) {s = 11;}

                        else if ( (LA26_13==RPAREN||(LA26_13>=COLON && LA26_13<=TIMESTAMP)) && ((strict == false))) {s = 22;}

                        else if ( (LA26_13==DOT) ) {s = 23;}

                         
                        input.seek(index26_13);
                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA26_18 = input.LA(1);

                         
                        int index26_18 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA26_18==EQUALS||(LA26_18>=NOTEQUALS && LA26_18<=GREATERTHANOREQUALS)) ) {s = 11;}

                        else if ( (LA26_18==RPAREN||(LA26_18>=COLON && LA26_18<=TIMESTAMP)) && ((strict == false))) {s = 22;}

                         
                        input.seek(index26_18);
                        if ( s>=0 ) return s;
                        break;
                    case 7 : 
                        int LA26_12 = input.LA(1);

                         
                        int index26_12 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA26_12==SELECT||LA26_12==AS||LA26_12==LPAREN||(LA26_12>=FROM && LA26_12<=ON)||(LA26_12>=WHERE && LA26_12<=NOT)||(LA26_12>=IN && LA26_12<=DESC)||LA26_12==SCORE) ) {s = 11;}

                        else if ( (LA26_12==ID) ) {s = 13;}

                        else if ( (LA26_12==DOUBLE_QUOTE) ) {s = 14;}

                        else if ( (LA26_12==TIMESTAMP) ) {s = 15;}

                        else if ( (LA26_12==TRUE) ) {s = 16;}

                        else if ( (LA26_12==FALSE) ) {s = 17;}

                        else if ( (LA26_12==FLOATING_POINT_LITERAL) ) {s = 18;}

                        else if ( (LA26_12==DECIMAL_INTEGER_LITERAL) ) {s = 19;}

                        else if ( (LA26_12==QUOTED_STRING) ) {s = 20;}

                        else if ( (LA26_12==COLON) ) {s = 21;}

                        else if ( (LA26_12==RPAREN) && ((strict == false))) {s = 22;}

                         
                        input.seek(index26_12);
                        if ( s>=0 ) return s;
                        break;
                    case 8 : 
                        int LA26_56 = input.LA(1);

                         
                        int index26_56 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA26_56==EQUALS||(LA26_56>=NOT && LA26_56<=IS)) ) {s = 11;}

                        else if ( (LA26_56==RPAREN||(LA26_56>=COLON && LA26_56<=TIMESTAMP)) && ((strict == false))) {s = 22;}

                         
                        input.seek(index26_56);
                        if ( s>=0 ) return s;
                        break;
                    case 9 : 
                        int LA26_54 = input.LA(1);

                         
                        int index26_54 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA26_54==EQUALS||(LA26_54>=NOTEQUALS && LA26_54<=GREATERTHANOREQUALS)) ) {s = 11;}

                        else if ( (LA26_54==RPAREN||(LA26_54>=COLON && LA26_54<=TIMESTAMP)) && ((strict == false))) {s = 22;}

                         
                        input.seek(index26_54);
                        if ( s>=0 ) return s;
                        break;
                    case 10 : 
                        int LA26_117 = input.LA(1);

                         
                        int index26_117 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA26_117==RPAREN||(LA26_117>=COLON && LA26_117<=TIMESTAMP)) && ((strict == false))) {s = 22;}

                        else if ( (LA26_117==EQUALS||(LA26_117>=NOTEQUALS && LA26_117<=GREATERTHANOREQUALS)) ) {s = 11;}

                         
                        input.seek(index26_117);
                        if ( s>=0 ) return s;
                        break;
                    case 11 : 
                        int LA26_118 = input.LA(1);

                         
                        int index26_118 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA26_118==EQUALS||(LA26_118>=NOT && LA26_118<=IS)) ) {s = 11;}

                        else if ( (LA26_118==RPAREN||(LA26_118>=COLON && LA26_118<=TIMESTAMP)) && ((strict == false))) {s = 22;}

                         
                        input.seek(index26_118);
                        if ( s>=0 ) return s;
                        break;
                    case 12 : 
                        int LA26_58 = input.LA(1);

                         
                        int index26_58 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA26_58==DOT) ) {s = 23;}

                        else if ( (LA26_58==EQUALS||(LA26_58>=NOT && LA26_58<=IS)) ) {s = 11;}

                        else if ( (LA26_58==RPAREN||(LA26_58>=COLON && LA26_58<=TIMESTAMP)) && ((strict == false))) {s = 22;}

                         
                        input.seek(index26_58);
                        if ( s>=0 ) return s;
                        break;
                    case 13 : 
                        int LA26_0 = input.LA(1);

                         
                        int index26_0 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA26_0==NOT) ) {s = 1;}

                        else if ( (LA26_0==ID) ) {s = 2;}

                        else if ( (LA26_0==DOUBLE_QUOTE) && ((strict == false))) {s = 3;}

                        else if ( (LA26_0==SCORE) ) {s = 4;}

                        else if ( (LA26_0==SELECT||LA26_0==AS||(LA26_0>=FROM && LA26_0<=ON)||(LA26_0>=WHERE && LA26_0<=AND)||(LA26_0>=IN && LA26_0<=NULL)) && ((strict == false))) {s = 5;}

                        else if ( ((LA26_0>=ANY && LA26_0<=IN_TREE)) ) {s = 6;}

                        else if ( ((LA26_0>=ORDER && LA26_0<=DESC)) && ((strict == false))) {s = 7;}

                        else if ( (LA26_0==QUOTED_STRING||(LA26_0>=FLOATING_POINT_LITERAL && LA26_0<=TIMESTAMP)) ) {s = 8;}

                        else if ( (LA26_0==COLON) && ((strict == false))) {s = 9;}

                        else if ( (LA26_0==LPAREN) ) {s = 10;}

                         
                        input.seek(index26_0);
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
        "\12\uffff";
    static final String DFA27_eofS =
        "\12\uffff";
    static final String DFA27_minS =
        "\1\37\11\uffff";
    static final String DFA27_maxS =
        "\1\114\11\uffff";
    static final String DFA27_acceptS =
        "\1\uffff\10\1\1\2";
    static final String DFA27_specialS =
        "\1\0\11\uffff}>";
    static final String[] DFA27_transitionS = {
            "\1\4\2\uffff\1\4\2\uffff\1\11\1\uffff\6\4\1\uffff\4\4\5\uffff"+
            "\4\4\4\5\4\6\1\10\1\7\1\1\1\2\5\7\1\3",
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
            return "456:1: searchTest : ( predicate -> predicate | LPAREN searchOrCondition RPAREN -> searchOrCondition );";
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
                        if ( (LA27_0==ID) ) {s = 1;}

                        else if ( (LA27_0==DOUBLE_QUOTE) && ((strict == false))) {s = 2;}

                        else if ( (LA27_0==SCORE) ) {s = 3;}

                        else if ( (LA27_0==SELECT||LA27_0==AS||(LA27_0>=FROM && LA27_0<=ON)||(LA27_0>=WHERE && LA27_0<=NOT)||(LA27_0>=IN && LA27_0<=NULL)) && ((strict == false))) {s = 4;}

                        else if ( ((LA27_0>=ANY && LA27_0<=IN_TREE)) ) {s = 5;}

                        else if ( ((LA27_0>=ORDER && LA27_0<=DESC)) && ((strict == false))) {s = 6;}

                        else if ( (LA27_0==QUOTED_STRING||(LA27_0>=FLOATING_POINT_LITERAL && LA27_0<=TIMESTAMP)) ) {s = 7;}

                        else if ( (LA27_0==COLON) && ((strict == false))) {s = 8;}

                        else if ( (LA27_0==LPAREN) ) {s = 9;}

                         
                        input.seek(index27_0);
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
    static final String DFA28_eotS =
        "\u00e5\uffff";
    static final String DFA28_eofS =
        "\u0083\uffff\1\145\36\uffff\1\u0084\35\uffff\1\u0084\44\uffff";
    static final String DFA28_minS =
        "\1\37\1\44\1\37\2\uffff\7\45\3\uffff\1\105\1\67\4\uffff\35\106"+
        "\1\uffff\3\46\1\uffff\1\55\1\37\1\44\1\41\1\37\1\46\1\41\1\37\1"+
        "\46\1\41\1\37\1\46\35\106\1\uffff\1\105\1\67\3\uffff\35\106\1\46"+
        "\1\uffff\35\106\1\46\35\106\1\46\2\55\1\37\3\41\35\106\1\55";
    static final String DFA28_maxS =
        "\1\114\1\71\1\114\2\uffff\1\106\3\45\1\104\2\66\3\uffff\1\106\1"+
        "\70\4\uffff\35\106\1\uffff\3\113\1\uffff\1\71\1\114\1\71\1\113\1"+
        "\114\2\113\1\114\2\113\1\114\1\113\35\106\1\uffff\1\106\1\70\3\uffff"+
        "\35\106\1\77\1\uffff\35\106\1\77\35\106\1\77\2\71\1\114\3\113\35"+
        "\106\1\71";
    static final String DFA28_acceptS =
        "\3\uffff\2\1\7\uffff\2\5\1\4\2\uffff\1\3\1\2\2\1\35\uffff\1\6\3"+
        "\uffff\1\5\51\uffff\1\4\2\uffff\1\3\1\2\1\7\36\uffff\1\10\140\uffff";
    static final String DFA28_specialS =
        "\1\41\1\55\1\116\2\uffff\1\112\3\uffff\1\110\1\44\1\45\11\uffff"+
        "\1\106\1\70\1\75\1\60\1\43\1\32\1\24\1\14\1\11\1\1\1\122\1\117\1"+
        "\101\1\74\1\71\1\50\1\0\1\7\1\12\1\21\1\31\1\34\1\52\1\57\1\66\1"+
        "\105\1\111\1\121\1\10\1\uffff\1\35\1\64\1\63\3\uffff\1\3\1\23\1"+
        "\uffff\1\72\1\17\1\uffff\1\40\1\16\1\uffff\1\37\36\uffff\1\27\1"+
        "\53\40\uffff\1\2\36\uffff\1\103\35\uffff\1\77\1\uffff\1\47\1\124"+
        "\1\15\1\6\1\5\1\13\1\22\1\20\1\30\1\25\1\26\1\36\1\33\1\46\1\42"+
        "\1\51\1\54\1\61\1\56\1\62\1\65\1\67\1\73\1\76\1\100\1\102\1\104"+
        "\1\107\1\113\1\115\1\120\1\123\1\125\1\114\1\4}>";
    static final String[] DFA28_transitionS = {
            "\1\4\2\uffff\1\4\4\uffff\6\4\1\uffff\4\4\5\uffff\4\4\1\5\1"+
            "\6\1\7\1\10\4\4\1\15\1\14\1\1\1\2\2\14\1\12\1\13\1\11\1\3",
            "\1\17\1\24\7\uffff\1\23\3\uffff\1\20\5\23\1\22\1\21\1\16",
            "\1\25\2\uffff\1\26\4\uffff\1\27\1\30\1\31\1\32\1\33\1\34\1"+
            "\uffff\1\35\1\36\1\37\1\40\5\uffff\1\41\1\42\1\43\1\44\1\45"+
            "\1\46\1\47\1\50\1\51\1\52\1\53\1\54\2\uffff\1\61\3\uffff\1\56"+
            "\1\57\1\55\1\60",
            "",
            "",
            "\1\24\37\uffff\2\62",
            "\1\63",
            "\1\64",
            "\1\65",
            "\1\24\36\uffff\1\66",
            "\1\24\7\uffff\1\66\4\uffff\5\66",
            "\1\24\7\uffff\1\66\4\uffff\5\66",
            "",
            "",
            "",
            "\1\67\1\70",
            "\1\22\1\21",
            "",
            "",
            "",
            "",
            "\1\71",
            "\1\71",
            "\1\71",
            "\1\71",
            "\1\71",
            "\1\71",
            "\1\71",
            "\1\71",
            "\1\71",
            "\1\71",
            "\1\71",
            "\1\71",
            "\1\71",
            "\1\71",
            "\1\71",
            "\1\71",
            "\1\71",
            "\1\71",
            "\1\71",
            "\1\71",
            "\1\71",
            "\1\71",
            "\1\71",
            "\1\71",
            "\1\71",
            "\1\71",
            "\1\71",
            "\1\71",
            "\1\71",
            "",
            "\1\24\34\uffff\1\24\1\74\1\72\1\73\5\24",
            "\1\24\34\uffff\1\24\1\77\1\75\1\76\5\24",
            "\1\24\34\uffff\1\24\1\102\1\100\1\101\5\24",
            "",
            "\1\23\3\uffff\1\20\5\23\1\22\1\21\1\16",
            "\1\103\2\uffff\1\104\4\uffff\1\105\1\106\1\107\1\110\1\111"+
            "\1\112\1\uffff\1\113\1\114\1\115\1\116\5\uffff\1\117\1\120\1"+
            "\121\1\122\1\123\1\124\1\125\1\126\1\127\1\130\1\131\1\132\2"+
            "\uffff\1\137\3\uffff\1\134\1\135\1\133\1\136",
            "\1\141\10\uffff\1\24\3\uffff\1\142\5\24\1\144\1\143\1\140",
            "\1\145\2\uffff\1\24\1\uffff\1\24\34\uffff\11\24",
            "\1\146\2\uffff\1\147\4\uffff\1\150\1\151\1\152\1\153\1\154"+
            "\1\155\1\uffff\1\156\1\157\1\160\1\161\5\uffff\1\162\1\163\1"+
            "\164\1\165\1\166\1\167\1\170\1\171\1\172\1\173\1\174\1\175\2"+
            "\uffff\1\u0082\3\uffff\1\177\1\u0080\1\176\1\u0081",
            "\1\u0083\34\uffff\11\24",
            "\1\u0084\2\uffff\1\24\1\uffff\1\24\34\uffff\11\24",
            "\1\u0085\2\uffff\1\u0086\4\uffff\1\u0087\1\u0088\1\u0089\1"+
            "\u008a\1\u008b\1\u008c\1\uffff\1\u008d\1\u008e\1\u008f\1\u0090"+
            "\5\uffff\1\u0091\1\u0092\1\u0093\1\u0094\1\u0095\1\u0096\1\u0097"+
            "\1\u0098\1\u0099\1\u009a\1\u009b\1\u009c\2\uffff\1\u00a1\3\uffff"+
            "\1\u009e\1\u009f\1\u009d\1\u00a0",
            "\1\u00a2\34\uffff\11\24",
            "\1\u0084\2\uffff\1\24\1\uffff\1\24\34\uffff\11\24",
            "\1\u00a3\2\uffff\1\u00a4\4\uffff\1\u00a5\1\u00a6\1\u00a7\1"+
            "\u00a8\1\u00a9\1\u00aa\1\uffff\1\u00ab\1\u00ac\1\u00ad\1\u00ae"+
            "\5\uffff\1\u00af\1\u00b0\1\u00b1\1\u00b2\1\u00b3\1\u00b4\1\u00b5"+
            "\1\u00b6\1\u00b7\1\u00b8\1\u00b9\1\u00ba\2\uffff\1\u00bf\3\uffff"+
            "\1\u00bc\1\u00bd\1\u00bb\1\u00be",
            "\1\u00c0\34\uffff\11\24",
            "\1\u00c1",
            "\1\u00c1",
            "\1\u00c1",
            "\1\u00c1",
            "\1\u00c1",
            "\1\u00c1",
            "\1\u00c1",
            "\1\u00c1",
            "\1\u00c1",
            "\1\u00c1",
            "\1\u00c1",
            "\1\u00c1",
            "\1\u00c1",
            "\1\u00c1",
            "\1\u00c1",
            "\1\u00c1",
            "\1\u00c1",
            "\1\u00c1",
            "\1\u00c1",
            "\1\u00c1",
            "\1\u00c1",
            "\1\u00c1",
            "\1\u00c1",
            "\1\u00c1",
            "\1\u00c1",
            "\1\u00c1",
            "\1\u00c1",
            "\1\u00c1",
            "\1\u00c1",
            "",
            "\1\u00c2\1\u00c3",
            "\1\144\1\143",
            "",
            "",
            "",
            "\1\u00c4",
            "\1\u00c4",
            "\1\u00c4",
            "\1\u00c4",
            "\1\u00c4",
            "\1\u00c4",
            "\1\u00c4",
            "\1\u00c4",
            "\1\u00c4",
            "\1\u00c4",
            "\1\u00c4",
            "\1\u00c4",
            "\1\u00c4",
            "\1\u00c4",
            "\1\u00c4",
            "\1\u00c4",
            "\1\u00c4",
            "\1\u00c4",
            "\1\u00c4",
            "\1\u00c4",
            "\1\u00c4",
            "\1\u00c4",
            "\1\u00c4",
            "\1\u00c4",
            "\1\u00c4",
            "\1\u00c4",
            "\1\u00c4",
            "\1\u00c4",
            "\1\u00c4",
            "\1\145\6\uffff\1\24\1\uffff\2\145\1\uffff\5\24\10\uffff\1"+
            "\145",
            "",
            "\1\u00c5",
            "\1\u00c5",
            "\1\u00c5",
            "\1\u00c5",
            "\1\u00c5",
            "\1\u00c5",
            "\1\u00c5",
            "\1\u00c5",
            "\1\u00c5",
            "\1\u00c5",
            "\1\u00c5",
            "\1\u00c5",
            "\1\u00c5",
            "\1\u00c5",
            "\1\u00c5",
            "\1\u00c5",
            "\1\u00c5",
            "\1\u00c5",
            "\1\u00c5",
            "\1\u00c5",
            "\1\u00c5",
            "\1\u00c5",
            "\1\u00c5",
            "\1\u00c5",
            "\1\u00c5",
            "\1\u00c5",
            "\1\u00c5",
            "\1\u00c5",
            "\1\u00c5",
            "\1\u0084\6\uffff\1\24\1\uffff\2\u0084\1\uffff\5\24\10\uffff"+
            "\1\u0084",
            "\1\u00c6",
            "\1\u00c6",
            "\1\u00c6",
            "\1\u00c6",
            "\1\u00c6",
            "\1\u00c6",
            "\1\u00c6",
            "\1\u00c6",
            "\1\u00c6",
            "\1\u00c6",
            "\1\u00c6",
            "\1\u00c6",
            "\1\u00c6",
            "\1\u00c6",
            "\1\u00c6",
            "\1\u00c6",
            "\1\u00c6",
            "\1\u00c6",
            "\1\u00c6",
            "\1\u00c6",
            "\1\u00c6",
            "\1\u00c6",
            "\1\u00c6",
            "\1\u00c6",
            "\1\u00c6",
            "\1\u00c6",
            "\1\u00c6",
            "\1\u00c6",
            "\1\u00c6",
            "\1\u0084\6\uffff\1\24\1\uffff\2\u0084\1\uffff\5\24\10\uffff"+
            "\1\u0084",
            "\1\23\3\uffff\1\20\5\23\1\22\1\21\1\16",
            "\1\24\3\uffff\1\142\5\24\1\144\1\143\1\140",
            "\1\u00c7\2\uffff\1\u00c8\4\uffff\1\u00c9\1\u00ca\1\u00cb\1"+
            "\u00cc\1\u00cd\1\u00ce\1\uffff\1\u00cf\1\u00d0\1\u00d1\1\u00d2"+
            "\5\uffff\1\u00d3\1\u00d4\1\u00d5\1\u00d6\1\u00d7\1\u00d8\1\u00d9"+
            "\1\u00da\1\u00db\1\u00dc\1\u00dd\1\u00de\2\uffff\1\u00e3\3\uffff"+
            "\1\u00e0\1\u00e1\1\u00df\1\u00e2",
            "\1\145\2\uffff\1\24\1\uffff\1\24\34\uffff\11\24",
            "\1\u0084\2\uffff\1\24\1\uffff\1\24\34\uffff\11\24",
            "\1\u0084\2\uffff\1\24\1\uffff\1\24\34\uffff\11\24",
            "\1\u00e4",
            "\1\u00e4",
            "\1\u00e4",
            "\1\u00e4",
            "\1\u00e4",
            "\1\u00e4",
            "\1\u00e4",
            "\1\u00e4",
            "\1\u00e4",
            "\1\u00e4",
            "\1\u00e4",
            "\1\u00e4",
            "\1\u00e4",
            "\1\u00e4",
            "\1\u00e4",
            "\1\u00e4",
            "\1\u00e4",
            "\1\u00e4",
            "\1\u00e4",
            "\1\u00e4",
            "\1\u00e4",
            "\1\u00e4",
            "\1\u00e4",
            "\1\u00e4",
            "\1\u00e4",
            "\1\u00e4",
            "\1\u00e4",
            "\1\u00e4",
            "\1\u00e4",
            "\1\24\3\uffff\1\142\5\24\1\144\1\143\1\140"
    };

    static final short[] DFA28_eot = DFA.unpackEncodedString(DFA28_eotS);
    static final short[] DFA28_eof = DFA.unpackEncodedString(DFA28_eofS);
    static final char[] DFA28_min = DFA.unpackEncodedStringToUnsignedChars(DFA28_minS);
    static final char[] DFA28_max = DFA.unpackEncodedStringToUnsignedChars(DFA28_maxS);
    static final short[] DFA28_accept = DFA.unpackEncodedString(DFA28_acceptS);
    static final short[] DFA28_special = DFA.unpackEncodedString(DFA28_specialS);
    static final short[][] DFA28_transition;

    static {
        int numStates = DFA28_transitionS.length;
        DFA28_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA28_transition[i] = DFA.unpackEncodedString(DFA28_transitionS[i]);
        }
    }

    class DFA28 extends DFA {

        public DFA28(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 28;
            this.eot = DFA28_eot;
            this.eof = DFA28_eof;
            this.min = DFA28_min;
            this.max = DFA28_max;
            this.accept = DFA28_accept;
            this.special = DFA28_special;
            this.transition = DFA28_transition;
        }
        public String getDescription() {
            return "463:1: predicate : ( comparisonPredicate | inPredicate | likePredicate | nullPredicate | quantifiedComparisonPredicate | quantifiedInPredicate | textSearchPredicate | folderPredicate );";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA28_37 = input.LA(1);

                         
                        int index28_37 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA28_37==DOUBLE_QUOTE) && ((strict == false))) {s = 57;}

                         
                        input.seek(index28_37);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA28_30 = input.LA(1);

                         
                        int index28_30 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA28_30==DOUBLE_QUOTE) && ((strict == false))) {s = 57;}

                         
                        input.seek(index28_30);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA28_131 = input.LA(1);

                         
                        int index28_131 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA28_131==EOF||LA28_131==RPAREN||(LA28_131>=OR && LA28_131<=AND)||LA28_131==ORDER) ) {s = 101;}

                        else if ( (LA28_131==EQUALS||(LA28_131>=NOTEQUALS && LA28_131<=GREATERTHANOREQUALS)) && ((strict == false))) {s = 20;}

                         
                        input.seek(index28_131);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA28_57 = input.LA(1);

                         
                        int index28_57 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA28_57==IS) && ((strict == false))) {s = 96;}

                        else if ( (LA28_57==DOT) && ((strict == false))) {s = 97;}

                        else if ( (LA28_57==NOT) && ((strict == false))) {s = 98;}

                        else if ( (LA28_57==LIKE) && ((strict == false))) {s = 99;}

                        else if ( (LA28_57==IN) && ((strict == false))) {s = 100;}

                        else if ( (LA28_57==EQUALS||(LA28_57>=NOTEQUALS && LA28_57<=GREATERTHANOREQUALS)) && ((strict == false))) {s = 20;}

                         
                        input.seek(index28_57);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA28_228 = input.LA(1);

                         
                        int index28_228 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA28_228==IS) && ((strict == false))) {s = 96;}

                        else if ( (LA28_228==NOT) && ((strict == false))) {s = 98;}

                        else if ( (LA28_228==LIKE) && ((strict == false))) {s = 99;}

                        else if ( (LA28_228==IN) && ((strict == false))) {s = 100;}

                        else if ( (LA28_228==EQUALS||(LA28_228>=NOTEQUALS && LA28_228<=GREATERTHANOREQUALS)) && ((strict == false))) {s = 20;}

                         
                        input.seek(index28_228);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA28_198 = input.LA(1);

                         
                        int index28_198 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA28_198==DOT||LA28_198==RPAREN||(LA28_198>=COLON && LA28_198<=TIMESTAMP)) && ((strict == false))) {s = 20;}

                        else if ( (LA28_198==COMMA) ) {s = 132;}

                         
                        input.seek(index28_198);
                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA28_197 = input.LA(1);

                         
                        int index28_197 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA28_197==DOT||LA28_197==RPAREN||(LA28_197>=COLON && LA28_197<=TIMESTAMP)) && ((strict == false))) {s = 20;}

                        else if ( (LA28_197==COMMA) ) {s = 132;}

                         
                        input.seek(index28_197);
                        if ( s>=0 ) return s;
                        break;
                    case 7 : 
                        int LA28_38 = input.LA(1);

                         
                        int index28_38 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA28_38==DOUBLE_QUOTE) && ((strict == false))) {s = 57;}

                         
                        input.seek(index28_38);
                        if ( s>=0 ) return s;
                        break;
                    case 8 : 
                        int LA28_49 = input.LA(1);

                         
                        int index28_49 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA28_49==DOUBLE_QUOTE) && ((strict == false))) {s = 57;}

                         
                        input.seek(index28_49);
                        if ( s>=0 ) return s;
                        break;
                    case 9 : 
                        int LA28_29 = input.LA(1);

                         
                        int index28_29 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA28_29==DOUBLE_QUOTE) && ((strict == false))) {s = 57;}

                         
                        input.seek(index28_29);
                        if ( s>=0 ) return s;
                        break;
                    case 10 : 
                        int LA28_39 = input.LA(1);

                         
                        int index28_39 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA28_39==DOUBLE_QUOTE) && ((strict == false))) {s = 57;}

                         
                        input.seek(index28_39);
                        if ( s>=0 ) return s;
                        break;
                    case 11 : 
                        int LA28_199 = input.LA(1);

                         
                        int index28_199 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA28_199==DOUBLE_QUOTE) && ((strict == false))) {s = 228;}

                         
                        input.seek(index28_199);
                        if ( s>=0 ) return s;
                        break;
                    case 12 : 
                        int LA28_28 = input.LA(1);

                         
                        int index28_28 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA28_28==DOUBLE_QUOTE) && ((strict == false))) {s = 57;}

                         
                        input.seek(index28_28);
                        if ( s>=0 ) return s;
                        break;
                    case 13 : 
                        int LA28_196 = input.LA(1);

                         
                        int index28_196 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA28_196==DOT||LA28_196==RPAREN||(LA28_196>=COLON && LA28_196<=TIMESTAMP)) && ((strict == false))) {s = 20;}

                        else if ( (LA28_196==COMMA) ) {s = 101;}

                         
                        input.seek(index28_196);
                        if ( s>=0 ) return s;
                        break;
                    case 14 : 
                        int LA28_64 = input.LA(1);

                         
                        int index28_64 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA28_64==COMMA) ) {s = 132;}

                        else if ( (LA28_64==DOT||LA28_64==RPAREN||(LA28_64>=COLON && LA28_64<=TIMESTAMP)) && ((strict == false))) {s = 20;}

                         
                        input.seek(index28_64);
                        if ( s>=0 ) return s;
                        break;
                    case 15 : 
                        int LA28_61 = input.LA(1);

                         
                        int index28_61 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA28_61==COMMA) ) {s = 132;}

                        else if ( (LA28_61==DOT||LA28_61==RPAREN||(LA28_61>=COLON && LA28_61<=TIMESTAMP)) && ((strict == false))) {s = 20;}

                         
                        input.seek(index28_61);
                        if ( s>=0 ) return s;
                        break;
                    case 16 : 
                        int LA28_201 = input.LA(1);

                         
                        int index28_201 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA28_201==DOUBLE_QUOTE) && ((strict == false))) {s = 228;}

                         
                        input.seek(index28_201);
                        if ( s>=0 ) return s;
                        break;
                    case 17 : 
                        int LA28_40 = input.LA(1);

                         
                        int index28_40 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA28_40==DOUBLE_QUOTE) && ((strict == false))) {s = 57;}

                         
                        input.seek(index28_40);
                        if ( s>=0 ) return s;
                        break;
                    case 18 : 
                        int LA28_200 = input.LA(1);

                         
                        int index28_200 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA28_200==DOUBLE_QUOTE) && ((strict == false))) {s = 228;}

                         
                        input.seek(index28_200);
                        if ( s>=0 ) return s;
                        break;
                    case 19 : 
                        int LA28_58 = input.LA(1);

                         
                        int index28_58 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA28_58==COMMA) ) {s = 101;}

                        else if ( (LA28_58==DOT||LA28_58==RPAREN||(LA28_58>=COLON && LA28_58<=TIMESTAMP)) && ((strict == false))) {s = 20;}

                         
                        input.seek(index28_58);
                        if ( s>=0 ) return s;
                        break;
                    case 20 : 
                        int LA28_27 = input.LA(1);

                         
                        int index28_27 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA28_27==DOUBLE_QUOTE) && ((strict == false))) {s = 57;}

                         
                        input.seek(index28_27);
                        if ( s>=0 ) return s;
                        break;
                    case 21 : 
                        int LA28_203 = input.LA(1);

                         
                        int index28_203 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA28_203==DOUBLE_QUOTE) && ((strict == false))) {s = 228;}

                         
                        input.seek(index28_203);
                        if ( s>=0 ) return s;
                        break;
                    case 22 : 
                        int LA28_204 = input.LA(1);

                         
                        int index28_204 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA28_204==DOUBLE_QUOTE) && ((strict == false))) {s = 228;}

                         
                        input.seek(index28_204);
                        if ( s>=0 ) return s;
                        break;
                    case 23 : 
                        int LA28_97 = input.LA(1);

                         
                        int index28_97 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA28_97==ID) && ((strict == false))) {s = 194;}

                        else if ( (LA28_97==DOUBLE_QUOTE) && ((strict == false))) {s = 195;}

                         
                        input.seek(index28_97);
                        if ( s>=0 ) return s;
                        break;
                    case 24 : 
                        int LA28_202 = input.LA(1);

                         
                        int index28_202 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA28_202==DOUBLE_QUOTE) && ((strict == false))) {s = 228;}

                         
                        input.seek(index28_202);
                        if ( s>=0 ) return s;
                        break;
                    case 25 : 
                        int LA28_41 = input.LA(1);

                         
                        int index28_41 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA28_41==DOUBLE_QUOTE) && ((strict == false))) {s = 57;}

                         
                        input.seek(index28_41);
                        if ( s>=0 ) return s;
                        break;
                    case 26 : 
                        int LA28_26 = input.LA(1);

                         
                        int index28_26 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA28_26==DOUBLE_QUOTE) && ((strict == false))) {s = 57;}

                         
                        input.seek(index28_26);
                        if ( s>=0 ) return s;
                        break;
                    case 27 : 
                        int LA28_206 = input.LA(1);

                         
                        int index28_206 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA28_206==DOUBLE_QUOTE) && ((strict == false))) {s = 228;}

                         
                        input.seek(index28_206);
                        if ( s>=0 ) return s;
                        break;
                    case 28 : 
                        int LA28_42 = input.LA(1);

                         
                        int index28_42 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA28_42==DOUBLE_QUOTE) && ((strict == false))) {s = 57;}

                         
                        input.seek(index28_42);
                        if ( s>=0 ) return s;
                        break;
                    case 29 : 
                        int LA28_51 = input.LA(1);

                         
                        int index28_51 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA28_51==ID) ) {s = 58;}

                        else if ( (LA28_51==DOUBLE_QUOTE) ) {s = 59;}

                        else if ( (LA28_51==RPAREN||LA28_51==COLON||(LA28_51>=FLOATING_POINT_LITERAL && LA28_51<=TIMESTAMP)) && ((strict == false))) {s = 20;}

                        else if ( (LA28_51==QUOTED_STRING) ) {s = 60;}

                         
                        input.seek(index28_51);
                        if ( s>=0 ) return s;
                        break;
                    case 30 : 
                        int LA28_205 = input.LA(1);

                         
                        int index28_205 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA28_205==DOUBLE_QUOTE) && ((strict == false))) {s = 228;}

                         
                        input.seek(index28_205);
                        if ( s>=0 ) return s;
                        break;
                    case 31 : 
                        int LA28_66 = input.LA(1);

                         
                        int index28_66 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA28_66==RPAREN) ) {s = 192;}

                        else if ( ((LA28_66>=COLON && LA28_66<=TIMESTAMP)) && ((strict == false))) {s = 20;}

                         
                        input.seek(index28_66);
                        if ( s>=0 ) return s;
                        break;
                    case 32 : 
                        int LA28_63 = input.LA(1);

                         
                        int index28_63 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA28_63==RPAREN) ) {s = 162;}

                        else if ( ((LA28_63>=COLON && LA28_63<=TIMESTAMP)) && ((strict == false))) {s = 20;}

                         
                        input.seek(index28_63);
                        if ( s>=0 ) return s;
                        break;
                    case 33 : 
                        int LA28_0 = input.LA(1);

                         
                        int index28_0 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA28_0==ID) ) {s = 1;}

                        else if ( (LA28_0==DOUBLE_QUOTE) && ((strict == false))) {s = 2;}

                        else if ( (LA28_0==SCORE) ) {s = 3;}

                        else if ( (LA28_0==SELECT||LA28_0==AS||(LA28_0>=FROM && LA28_0<=ON)||(LA28_0>=WHERE && LA28_0<=NOT)||(LA28_0>=IN && LA28_0<=NULL)||(LA28_0>=ORDER && LA28_0<=DESC)) && ((strict == false))) {s = 4;}

                        else if ( (LA28_0==ANY) ) {s = 5;}

                        else if ( (LA28_0==CONTAINS) ) {s = 6;}

                        else if ( (LA28_0==IN_FOLDER) ) {s = 7;}

                        else if ( (LA28_0==IN_TREE) ) {s = 8;}

                        else if ( (LA28_0==TIMESTAMP) ) {s = 9;}

                        else if ( (LA28_0==TRUE) ) {s = 10;}

                        else if ( (LA28_0==FALSE) ) {s = 11;}

                        else if ( (LA28_0==QUOTED_STRING||(LA28_0>=FLOATING_POINT_LITERAL && LA28_0<=DECIMAL_INTEGER_LITERAL)) ) {s = 12;}

                        else if ( (LA28_0==COLON) && ((strict == false))) {s = 13;}

                         
                        input.seek(index28_0);
                        if ( s>=0 ) return s;
                        break;
                    case 34 : 
                        int LA28_208 = input.LA(1);

                         
                        int index28_208 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA28_208==DOUBLE_QUOTE) && ((strict == false))) {s = 228;}

                         
                        input.seek(index28_208);
                        if ( s>=0 ) return s;
                        break;
                    case 35 : 
                        int LA28_25 = input.LA(1);

                         
                        int index28_25 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA28_25==DOUBLE_QUOTE) && ((strict == false))) {s = 57;}

                         
                        input.seek(index28_25);
                        if ( s>=0 ) return s;
                        break;
                    case 36 : 
                        int LA28_10 = input.LA(1);

                         
                        int index28_10 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA28_10==LPAREN) && ((strict == false))) {s = 20;}

                        else if ( (LA28_10==EQUALS||(LA28_10>=NOTEQUALS && LA28_10<=GREATERTHANOREQUALS)) ) {s = 54;}

                         
                        input.seek(index28_10);
                        if ( s>=0 ) return s;
                        break;
                    case 37 : 
                        int LA28_11 = input.LA(1);

                         
                        int index28_11 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA28_11==LPAREN) && ((strict == false))) {s = 20;}

                        else if ( (LA28_11==EQUALS||(LA28_11>=NOTEQUALS && LA28_11<=GREATERTHANOREQUALS)) ) {s = 54;}

                         
                        input.seek(index28_11);
                        if ( s>=0 ) return s;
                        break;
                    case 38 : 
                        int LA28_207 = input.LA(1);

                         
                        int index28_207 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA28_207==DOUBLE_QUOTE) && ((strict == false))) {s = 228;}

                         
                        input.seek(index28_207);
                        if ( s>=0 ) return s;
                        break;
                    case 39 : 
                        int LA28_194 = input.LA(1);

                         
                        int index28_194 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA28_194==IS) && ((strict == false))) {s = 96;}

                        else if ( (LA28_194==NOT) && ((strict == false))) {s = 98;}

                        else if ( (LA28_194==LIKE) && ((strict == false))) {s = 99;}

                        else if ( (LA28_194==IN) && ((strict == false))) {s = 100;}

                        else if ( (LA28_194==EQUALS||(LA28_194>=NOTEQUALS && LA28_194<=GREATERTHANOREQUALS)) && ((strict == false))) {s = 20;}

                         
                        input.seek(index28_194);
                        if ( s>=0 ) return s;
                        break;
                    case 40 : 
                        int LA28_36 = input.LA(1);

                         
                        int index28_36 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA28_36==DOUBLE_QUOTE) && ((strict == false))) {s = 57;}

                         
                        input.seek(index28_36);
                        if ( s>=0 ) return s;
                        break;
                    case 41 : 
                        int LA28_209 = input.LA(1);

                         
                        int index28_209 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA28_209==DOUBLE_QUOTE) && ((strict == false))) {s = 228;}

                         
                        input.seek(index28_209);
                        if ( s>=0 ) return s;
                        break;
                    case 42 : 
                        int LA28_43 = input.LA(1);

                         
                        int index28_43 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA28_43==DOUBLE_QUOTE) && ((strict == false))) {s = 57;}

                         
                        input.seek(index28_43);
                        if ( s>=0 ) return s;
                        break;
                    case 43 : 
                        int LA28_98 = input.LA(1);

                         
                        int index28_98 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA28_98==IN) && ((strict == false))) {s = 100;}

                        else if ( (LA28_98==LIKE) && ((strict == false))) {s = 99;}

                         
                        input.seek(index28_98);
                        if ( s>=0 ) return s;
                        break;
                    case 44 : 
                        int LA28_210 = input.LA(1);

                         
                        int index28_210 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA28_210==DOUBLE_QUOTE) && ((strict == false))) {s = 228;}

                         
                        input.seek(index28_210);
                        if ( s>=0 ) return s;
                        break;
                    case 45 : 
                        int LA28_1 = input.LA(1);

                         
                        int index28_1 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA28_1==IS) ) {s = 14;}

                        else if ( (LA28_1==DOT) ) {s = 15;}

                        else if ( (LA28_1==NOT) ) {s = 16;}

                        else if ( (LA28_1==LIKE) ) {s = 17;}

                        else if ( (LA28_1==IN) ) {s = 18;}

                        else if ( (LA28_1==EQUALS||(LA28_1>=NOTEQUALS && LA28_1<=GREATERTHANOREQUALS)) ) {s = 19;}

                        else if ( (LA28_1==LPAREN) && ((strict == false))) {s = 20;}

                         
                        input.seek(index28_1);
                        if ( s>=0 ) return s;
                        break;
                    case 46 : 
                        int LA28_212 = input.LA(1);

                         
                        int index28_212 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA28_212==DOUBLE_QUOTE) && ((strict == false))) {s = 228;}

                         
                        input.seek(index28_212);
                        if ( s>=0 ) return s;
                        break;
                    case 47 : 
                        int LA28_44 = input.LA(1);

                         
                        int index28_44 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA28_44==DOUBLE_QUOTE) && ((strict == false))) {s = 57;}

                         
                        input.seek(index28_44);
                        if ( s>=0 ) return s;
                        break;
                    case 48 : 
                        int LA28_24 = input.LA(1);

                         
                        int index28_24 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA28_24==DOUBLE_QUOTE) && ((strict == false))) {s = 57;}

                         
                        input.seek(index28_24);
                        if ( s>=0 ) return s;
                        break;
                    case 49 : 
                        int LA28_211 = input.LA(1);

                         
                        int index28_211 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA28_211==DOUBLE_QUOTE) && ((strict == false))) {s = 228;}

                         
                        input.seek(index28_211);
                        if ( s>=0 ) return s;
                        break;
                    case 50 : 
                        int LA28_213 = input.LA(1);

                         
                        int index28_213 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA28_213==DOUBLE_QUOTE) && ((strict == false))) {s = 228;}

                         
                        input.seek(index28_213);
                        if ( s>=0 ) return s;
                        break;
                    case 51 : 
                        int LA28_53 = input.LA(1);

                         
                        int index28_53 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA28_53==ID) ) {s = 64;}

                        else if ( (LA28_53==DOUBLE_QUOTE) ) {s = 65;}

                        else if ( (LA28_53==RPAREN||LA28_53==COLON||(LA28_53>=FLOATING_POINT_LITERAL && LA28_53<=TIMESTAMP)) && ((strict == false))) {s = 20;}

                        else if ( (LA28_53==QUOTED_STRING) ) {s = 66;}

                         
                        input.seek(index28_53);
                        if ( s>=0 ) return s;
                        break;
                    case 52 : 
                        int LA28_52 = input.LA(1);

                         
                        int index28_52 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA28_52==ID) ) {s = 61;}

                        else if ( (LA28_52==DOUBLE_QUOTE) ) {s = 62;}

                        else if ( (LA28_52==RPAREN||LA28_52==COLON||(LA28_52>=FLOATING_POINT_LITERAL && LA28_52<=TIMESTAMP)) && ((strict == false))) {s = 20;}

                        else if ( (LA28_52==QUOTED_STRING) ) {s = 63;}

                         
                        input.seek(index28_52);
                        if ( s>=0 ) return s;
                        break;
                    case 53 : 
                        int LA28_214 = input.LA(1);

                         
                        int index28_214 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA28_214==DOUBLE_QUOTE) && ((strict == false))) {s = 228;}

                         
                        input.seek(index28_214);
                        if ( s>=0 ) return s;
                        break;
                    case 54 : 
                        int LA28_45 = input.LA(1);

                         
                        int index28_45 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA28_45==DOUBLE_QUOTE) && ((strict == false))) {s = 57;}

                         
                        input.seek(index28_45);
                        if ( s>=0 ) return s;
                        break;
                    case 55 : 
                        int LA28_215 = input.LA(1);

                         
                        int index28_215 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA28_215==DOUBLE_QUOTE) && ((strict == false))) {s = 228;}

                         
                        input.seek(index28_215);
                        if ( s>=0 ) return s;
                        break;
                    case 56 : 
                        int LA28_22 = input.LA(1);

                         
                        int index28_22 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA28_22==DOUBLE_QUOTE) && ((strict == false))) {s = 57;}

                         
                        input.seek(index28_22);
                        if ( s>=0 ) return s;
                        break;
                    case 57 : 
                        int LA28_35 = input.LA(1);

                         
                        int index28_35 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA28_35==DOUBLE_QUOTE) && ((strict == false))) {s = 57;}

                         
                        input.seek(index28_35);
                        if ( s>=0 ) return s;
                        break;
                    case 58 : 
                        int LA28_60 = input.LA(1);

                         
                        int index28_60 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA28_60==RPAREN) ) {s = 131;}

                        else if ( ((LA28_60>=COLON && LA28_60<=TIMESTAMP)) && ((strict == false))) {s = 20;}

                         
                        input.seek(index28_60);
                        if ( s>=0 ) return s;
                        break;
                    case 59 : 
                        int LA28_216 = input.LA(1);

                         
                        int index28_216 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA28_216==DOUBLE_QUOTE) && ((strict == false))) {s = 228;}

                         
                        input.seek(index28_216);
                        if ( s>=0 ) return s;
                        break;
                    case 60 : 
                        int LA28_34 = input.LA(1);

                         
                        int index28_34 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA28_34==DOUBLE_QUOTE) && ((strict == false))) {s = 57;}

                         
                        input.seek(index28_34);
                        if ( s>=0 ) return s;
                        break;
                    case 61 : 
                        int LA28_23 = input.LA(1);

                         
                        int index28_23 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA28_23==DOUBLE_QUOTE) && ((strict == false))) {s = 57;}

                         
                        input.seek(index28_23);
                        if ( s>=0 ) return s;
                        break;
                    case 62 : 
                        int LA28_217 = input.LA(1);

                         
                        int index28_217 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA28_217==DOUBLE_QUOTE) && ((strict == false))) {s = 228;}

                         
                        input.seek(index28_217);
                        if ( s>=0 ) return s;
                        break;
                    case 63 : 
                        int LA28_192 = input.LA(1);

                         
                        int index28_192 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA28_192==EOF||LA28_192==RPAREN||(LA28_192>=OR && LA28_192<=AND)||LA28_192==ORDER) ) {s = 132;}

                        else if ( (LA28_192==EQUALS||(LA28_192>=NOTEQUALS && LA28_192<=GREATERTHANOREQUALS)) && ((strict == false))) {s = 20;}

                         
                        input.seek(index28_192);
                        if ( s>=0 ) return s;
                        break;
                    case 64 : 
                        int LA28_218 = input.LA(1);

                         
                        int index28_218 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA28_218==DOUBLE_QUOTE) && ((strict == false))) {s = 228;}

                         
                        input.seek(index28_218);
                        if ( s>=0 ) return s;
                        break;
                    case 65 : 
                        int LA28_33 = input.LA(1);

                         
                        int index28_33 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA28_33==DOUBLE_QUOTE) && ((strict == false))) {s = 57;}

                         
                        input.seek(index28_33);
                        if ( s>=0 ) return s;
                        break;
                    case 66 : 
                        int LA28_219 = input.LA(1);

                         
                        int index28_219 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA28_219==DOUBLE_QUOTE) && ((strict == false))) {s = 228;}

                         
                        input.seek(index28_219);
                        if ( s>=0 ) return s;
                        break;
                    case 67 : 
                        int LA28_162 = input.LA(1);

                         
                        int index28_162 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA28_162==EOF||LA28_162==RPAREN||(LA28_162>=OR && LA28_162<=AND)||LA28_162==ORDER) ) {s = 132;}

                        else if ( (LA28_162==EQUALS||(LA28_162>=NOTEQUALS && LA28_162<=GREATERTHANOREQUALS)) && ((strict == false))) {s = 20;}

                         
                        input.seek(index28_162);
                        if ( s>=0 ) return s;
                        break;
                    case 68 : 
                        int LA28_220 = input.LA(1);

                         
                        int index28_220 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA28_220==DOUBLE_QUOTE) && ((strict == false))) {s = 228;}

                         
                        input.seek(index28_220);
                        if ( s>=0 ) return s;
                        break;
                    case 69 : 
                        int LA28_46 = input.LA(1);

                         
                        int index28_46 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA28_46==DOUBLE_QUOTE) && ((strict == false))) {s = 57;}

                         
                        input.seek(index28_46);
                        if ( s>=0 ) return s;
                        break;
                    case 70 : 
                        int LA28_21 = input.LA(1);

                         
                        int index28_21 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA28_21==DOUBLE_QUOTE) && ((strict == false))) {s = 57;}

                         
                        input.seek(index28_21);
                        if ( s>=0 ) return s;
                        break;
                    case 71 : 
                        int LA28_221 = input.LA(1);

                         
                        int index28_221 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA28_221==DOUBLE_QUOTE) && ((strict == false))) {s = 228;}

                         
                        input.seek(index28_221);
                        if ( s>=0 ) return s;
                        break;
                    case 72 : 
                        int LA28_9 = input.LA(1);

                         
                        int index28_9 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA28_9==QUOTED_STRING) ) {s = 54;}

                        else if ( (LA28_9==LPAREN) && ((strict == false))) {s = 20;}

                         
                        input.seek(index28_9);
                        if ( s>=0 ) return s;
                        break;
                    case 73 : 
                        int LA28_47 = input.LA(1);

                         
                        int index28_47 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA28_47==DOUBLE_QUOTE) && ((strict == false))) {s = 57;}

                         
                        input.seek(index28_47);
                        if ( s>=0 ) return s;
                        break;
                    case 74 : 
                        int LA28_5 = input.LA(1);

                         
                        int index28_5 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA28_5==LPAREN) && ((strict == false))) {s = 20;}

                        else if ( ((LA28_5>=ID && LA28_5<=DOUBLE_QUOTE)) ) {s = 50;}

                         
                        input.seek(index28_5);
                        if ( s>=0 ) return s;
                        break;
                    case 75 : 
                        int LA28_222 = input.LA(1);

                         
                        int index28_222 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA28_222==DOUBLE_QUOTE) && ((strict == false))) {s = 228;}

                         
                        input.seek(index28_222);
                        if ( s>=0 ) return s;
                        break;
                    case 76 : 
                        int LA28_227 = input.LA(1);

                         
                        int index28_227 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA28_227==DOUBLE_QUOTE) && ((strict == false))) {s = 228;}

                         
                        input.seek(index28_227);
                        if ( s>=0 ) return s;
                        break;
                    case 77 : 
                        int LA28_223 = input.LA(1);

                         
                        int index28_223 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA28_223==DOUBLE_QUOTE) && ((strict == false))) {s = 228;}

                         
                        input.seek(index28_223);
                        if ( s>=0 ) return s;
                        break;
                    case 78 : 
                        int LA28_2 = input.LA(1);

                         
                        int index28_2 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA28_2==SELECT) && ((strict == false))) {s = 21;}

                        else if ( (LA28_2==AS) && ((strict == false))) {s = 22;}

                        else if ( (LA28_2==FROM) && ((strict == false))) {s = 23;}

                        else if ( (LA28_2==JOIN) && ((strict == false))) {s = 24;}

                        else if ( (LA28_2==INNER) && ((strict == false))) {s = 25;}

                        else if ( (LA28_2==LEFT) && ((strict == false))) {s = 26;}

                        else if ( (LA28_2==OUTER) && ((strict == false))) {s = 27;}

                        else if ( (LA28_2==ON) && ((strict == false))) {s = 28;}

                        else if ( (LA28_2==WHERE) && ((strict == false))) {s = 29;}

                        else if ( (LA28_2==OR) && ((strict == false))) {s = 30;}

                        else if ( (LA28_2==AND) && ((strict == false))) {s = 31;}

                        else if ( (LA28_2==NOT) && ((strict == false))) {s = 32;}

                        else if ( (LA28_2==IN) && ((strict == false))) {s = 33;}

                        else if ( (LA28_2==LIKE) && ((strict == false))) {s = 34;}

                        else if ( (LA28_2==IS) && ((strict == false))) {s = 35;}

                        else if ( (LA28_2==NULL) && ((strict == false))) {s = 36;}

                        else if ( (LA28_2==ANY) && ((strict == false))) {s = 37;}

                        else if ( (LA28_2==CONTAINS) && ((strict == false))) {s = 38;}

                        else if ( (LA28_2==IN_FOLDER) && ((strict == false))) {s = 39;}

                        else if ( (LA28_2==IN_TREE) && ((strict == false))) {s = 40;}

                        else if ( (LA28_2==ORDER) && ((strict == false))) {s = 41;}

                        else if ( (LA28_2==BY) && ((strict == false))) {s = 42;}

                        else if ( (LA28_2==ASC) && ((strict == false))) {s = 43;}

                        else if ( (LA28_2==DESC) && ((strict == false))) {s = 44;}

                        else if ( (LA28_2==TIMESTAMP) && ((strict == false))) {s = 45;}

                        else if ( (LA28_2==TRUE) && ((strict == false))) {s = 46;}

                        else if ( (LA28_2==FALSE) && ((strict == false))) {s = 47;}

                        else if ( (LA28_2==SCORE) && ((strict == false))) {s = 48;}

                        else if ( (LA28_2==ID) && ((strict == false))) {s = 49;}

                         
                        input.seek(index28_2);
                        if ( s>=0 ) return s;
                        break;
                    case 79 : 
                        int LA28_32 = input.LA(1);

                         
                        int index28_32 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA28_32==DOUBLE_QUOTE) && ((strict == false))) {s = 57;}

                         
                        input.seek(index28_32);
                        if ( s>=0 ) return s;
                        break;
                    case 80 : 
                        int LA28_224 = input.LA(1);

                         
                        int index28_224 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA28_224==DOUBLE_QUOTE) && ((strict == false))) {s = 228;}

                         
                        input.seek(index28_224);
                        if ( s>=0 ) return s;
                        break;
                    case 81 : 
                        int LA28_48 = input.LA(1);

                         
                        int index28_48 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA28_48==DOUBLE_QUOTE) && ((strict == false))) {s = 57;}

                         
                        input.seek(index28_48);
                        if ( s>=0 ) return s;
                        break;
                    case 82 : 
                        int LA28_31 = input.LA(1);

                         
                        int index28_31 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA28_31==DOUBLE_QUOTE) && ((strict == false))) {s = 57;}

                         
                        input.seek(index28_31);
                        if ( s>=0 ) return s;
                        break;
                    case 83 : 
                        int LA28_225 = input.LA(1);

                         
                        int index28_225 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA28_225==DOUBLE_QUOTE) && ((strict == false))) {s = 228;}

                         
                        input.seek(index28_225);
                        if ( s>=0 ) return s;
                        break;
                    case 84 : 
                        int LA28_195 = input.LA(1);

                         
                        int index28_195 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA28_195==SELECT) && ((strict == false))) {s = 199;}

                        else if ( (LA28_195==AS) && ((strict == false))) {s = 200;}

                        else if ( (LA28_195==FROM) && ((strict == false))) {s = 201;}

                        else if ( (LA28_195==JOIN) && ((strict == false))) {s = 202;}

                        else if ( (LA28_195==INNER) && ((strict == false))) {s = 203;}

                        else if ( (LA28_195==LEFT) && ((strict == false))) {s = 204;}

                        else if ( (LA28_195==OUTER) && ((strict == false))) {s = 205;}

                        else if ( (LA28_195==ON) && ((strict == false))) {s = 206;}

                        else if ( (LA28_195==WHERE) && ((strict == false))) {s = 207;}

                        else if ( (LA28_195==OR) && ((strict == false))) {s = 208;}

                        else if ( (LA28_195==AND) && ((strict == false))) {s = 209;}

                        else if ( (LA28_195==NOT) && ((strict == false))) {s = 210;}

                        else if ( (LA28_195==IN) && ((strict == false))) {s = 211;}

                        else if ( (LA28_195==LIKE) && ((strict == false))) {s = 212;}

                        else if ( (LA28_195==IS) && ((strict == false))) {s = 213;}

                        else if ( (LA28_195==NULL) && ((strict == false))) {s = 214;}

                        else if ( (LA28_195==ANY) && ((strict == false))) {s = 215;}

                        else if ( (LA28_195==CONTAINS) && ((strict == false))) {s = 216;}

                        else if ( (LA28_195==IN_FOLDER) && ((strict == false))) {s = 217;}

                        else if ( (LA28_195==IN_TREE) && ((strict == false))) {s = 218;}

                        else if ( (LA28_195==ORDER) && ((strict == false))) {s = 219;}

                        else if ( (LA28_195==BY) && ((strict == false))) {s = 220;}

                        else if ( (LA28_195==ASC) && ((strict == false))) {s = 221;}

                        else if ( (LA28_195==DESC) && ((strict == false))) {s = 222;}

                        else if ( (LA28_195==TIMESTAMP) && ((strict == false))) {s = 223;}

                        else if ( (LA28_195==TRUE) && ((strict == false))) {s = 224;}

                        else if ( (LA28_195==FALSE) && ((strict == false))) {s = 225;}

                        else if ( (LA28_195==SCORE) && ((strict == false))) {s = 226;}

                        else if ( (LA28_195==ID) && ((strict == false))) {s = 227;}

                         
                        input.seek(index28_195);
                        if ( s>=0 ) return s;
                        break;
                    case 85 : 
                        int LA28_226 = input.LA(1);

                         
                        int index28_226 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA28_226==DOUBLE_QUOTE) && ((strict == false))) {s = 228;}

                         
                        input.seek(index28_226);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 28, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA36_eotS =
        "\151\uffff";
    static final String DFA36_eofS =
        "\151\uffff";
    static final String DFA36_minS =
        "\1\105\1\44\1\37\1\105\1\61\35\106\1\71\1\37\2\uffff\1\44\35\106"+
        "\1\61\1\105\1\71\2\uffff\1\71\1\37\35\106\1\71";
    static final String DFA36_maxS =
        "\1\106\1\71\1\114\1\106\1\72\35\106\1\71\1\114\2\uffff\1\71\35"+
        "\106\1\72\1\106\1\71\2\uffff\1\71\1\114\35\106\1\71";
    static final String DFA36_acceptS =
        "\44\uffff\1\2\1\1\41\uffff\1\1\1\2\40\uffff";
    static final String DFA36_specialS =
        "\1\41\1\uffff\1\16\2\uffff\1\45\1\30\1\37\1\22\1\13\1\3\1\11\1"+
        "\65\1\76\1\52\1\42\1\56\1\27\1\35\1\33\1\1\1\15\1\67\1\71\1\100"+
        "\1\44\1\55\1\60\1\25\1\32\1\2\1\10\1\62\1\47\4\uffff\1\36\35\uffff"+
        "\1\17\1\4\3\uffff\1\24\1\50\1\31\1\34\1\40\1\20\1\21\1\23\1\26\1"+
        "\53\1\51\1\46\1\43\1\63\1\61\1\57\1\54\1\72\1\70\1\66\1\64\1\101"+
        "\1\77\1\75\1\74\1\6\1\7\1\0\1\5\1\73\1\12\1\14}>";
    static final String[] DFA36_transitionS = {
            "\1\1\1\2",
            "\1\3\24\uffff\1\4",
            "\1\5\2\uffff\1\6\4\uffff\1\7\1\10\1\11\1\12\1\13\1\14\1\uffff"+
            "\1\15\1\16\1\17\1\20\5\uffff\1\21\1\22\1\23\1\24\1\25\1\26\1"+
            "\27\1\30\1\31\1\32\1\33\1\34\2\uffff\1\41\3\uffff\1\36\1\37"+
            "\1\35\1\40",
            "\1\42\1\43",
            "\1\44\10\uffff\1\45",
            "\1\46",
            "\1\46",
            "\1\46",
            "\1\46",
            "\1\46",
            "\1\46",
            "\1\46",
            "\1\46",
            "\1\46",
            "\1\46",
            "\1\46",
            "\1\46",
            "\1\46",
            "\1\46",
            "\1\46",
            "\1\46",
            "\1\46",
            "\1\46",
            "\1\46",
            "\1\46",
            "\1\46",
            "\1\46",
            "\1\46",
            "\1\46",
            "\1\46",
            "\1\46",
            "\1\46",
            "\1\46",
            "\1\46",
            "\1\4",
            "\1\47\2\uffff\1\50\4\uffff\1\51\1\52\1\53\1\54\1\55\1\56\1"+
            "\uffff\1\57\1\60\1\61\1\62\5\uffff\1\63\1\64\1\65\1\66\1\67"+
            "\1\70\1\71\1\72\1\73\1\74\1\75\1\76\2\uffff\1\103\3\uffff\1"+
            "\100\1\101\1\77\1\102",
            "",
            "",
            "\1\105\24\uffff\1\104",
            "\1\106",
            "\1\106",
            "\1\106",
            "\1\106",
            "\1\106",
            "\1\106",
            "\1\106",
            "\1\106",
            "\1\106",
            "\1\106",
            "\1\106",
            "\1\106",
            "\1\106",
            "\1\106",
            "\1\106",
            "\1\106",
            "\1\106",
            "\1\106",
            "\1\106",
            "\1\106",
            "\1\106",
            "\1\106",
            "\1\106",
            "\1\106",
            "\1\106",
            "\1\106",
            "\1\106",
            "\1\106",
            "\1\106",
            "\1\110\10\uffff\1\107",
            "\1\111\1\112",
            "\1\4",
            "",
            "",
            "\1\104",
            "\1\113\2\uffff\1\114\4\uffff\1\115\1\116\1\117\1\120\1\121"+
            "\1\122\1\uffff\1\123\1\124\1\125\1\126\5\uffff\1\127\1\130\1"+
            "\131\1\132\1\133\1\134\1\135\1\136\1\137\1\140\1\141\1\142\2"+
            "\uffff\1\147\3\uffff\1\144\1\145\1\143\1\146",
            "\1\150",
            "\1\150",
            "\1\150",
            "\1\150",
            "\1\150",
            "\1\150",
            "\1\150",
            "\1\150",
            "\1\150",
            "\1\150",
            "\1\150",
            "\1\150",
            "\1\150",
            "\1\150",
            "\1\150",
            "\1\150",
            "\1\150",
            "\1\150",
            "\1\150",
            "\1\150",
            "\1\150",
            "\1\150",
            "\1\150",
            "\1\150",
            "\1\150",
            "\1\150",
            "\1\150",
            "\1\150",
            "\1\150",
            "\1\104"
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
            return "515:1: nullPredicate : ( ( ( columnReference )=> columnReference | multiValuedColumnReference ) IS NULL -> ^( PRED_EXISTS columnReference NOT ) | ( ( columnReference )=> columnReference | multiValuedColumnReference ) IS NOT NULL -> ^( PRED_EXISTS columnReference ) );";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA36_100 = input.LA(1);

                         
                        int index36_100 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA36_100==DOUBLE_QUOTE) && ((strict == false))) {s = 104;}

                         
                        input.seek(index36_100);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA36_20 = input.LA(1);

                         
                        int index36_20 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA36_20==DOUBLE_QUOTE) && ((strict == false))) {s = 38;}

                         
                        input.seek(index36_20);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA36_30 = input.LA(1);

                         
                        int index36_30 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA36_30==DOUBLE_QUOTE) && ((strict == false))) {s = 38;}

                         
                        input.seek(index36_30);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA36_10 = input.LA(1);

                         
                        int index36_10 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA36_10==DOUBLE_QUOTE) && ((strict == false))) {s = 38;}

                         
                        input.seek(index36_10);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA36_69 = input.LA(1);

                         
                        int index36_69 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA36_69==ID) && ((strict == false))) {s = 73;}

                        else if ( (LA36_69==DOUBLE_QUOTE) && ((strict == false))) {s = 74;}

                         
                        input.seek(index36_69);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA36_101 = input.LA(1);

                         
                        int index36_101 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA36_101==DOUBLE_QUOTE) && ((strict == false))) {s = 104;}

                         
                        input.seek(index36_101);
                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA36_98 = input.LA(1);

                         
                        int index36_98 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA36_98==DOUBLE_QUOTE) && ((strict == false))) {s = 104;}

                         
                        input.seek(index36_98);
                        if ( s>=0 ) return s;
                        break;
                    case 7 : 
                        int LA36_99 = input.LA(1);

                         
                        int index36_99 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA36_99==DOUBLE_QUOTE) && ((strict == false))) {s = 104;}

                         
                        input.seek(index36_99);
                        if ( s>=0 ) return s;
                        break;
                    case 8 : 
                        int LA36_31 = input.LA(1);

                         
                        int index36_31 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA36_31==DOUBLE_QUOTE) && ((strict == false))) {s = 38;}

                         
                        input.seek(index36_31);
                        if ( s>=0 ) return s;
                        break;
                    case 9 : 
                        int LA36_11 = input.LA(1);

                         
                        int index36_11 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA36_11==DOUBLE_QUOTE) && ((strict == false))) {s = 38;}

                         
                        input.seek(index36_11);
                        if ( s>=0 ) return s;
                        break;
                    case 10 : 
                        int LA36_103 = input.LA(1);

                         
                        int index36_103 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA36_103==DOUBLE_QUOTE) && ((strict == false))) {s = 104;}

                         
                        input.seek(index36_103);
                        if ( s>=0 ) return s;
                        break;
                    case 11 : 
                        int LA36_9 = input.LA(1);

                         
                        int index36_9 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA36_9==DOUBLE_QUOTE) && ((strict == false))) {s = 38;}

                         
                        input.seek(index36_9);
                        if ( s>=0 ) return s;
                        break;
                    case 12 : 
                        int LA36_104 = input.LA(1);

                         
                        int index36_104 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA36_104==IS) && ((strict == false))) {s = 68;}

                         
                        input.seek(index36_104);
                        if ( s>=0 ) return s;
                        break;
                    case 13 : 
                        int LA36_21 = input.LA(1);

                         
                        int index36_21 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA36_21==DOUBLE_QUOTE) && ((strict == false))) {s = 38;}

                         
                        input.seek(index36_21);
                        if ( s>=0 ) return s;
                        break;
                    case 14 : 
                        int LA36_2 = input.LA(1);

                         
                        int index36_2 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA36_2==SELECT) && ((strict == false))) {s = 5;}

                        else if ( (LA36_2==AS) && ((strict == false))) {s = 6;}

                        else if ( (LA36_2==FROM) && ((strict == false))) {s = 7;}

                        else if ( (LA36_2==JOIN) && ((strict == false))) {s = 8;}

                        else if ( (LA36_2==INNER) && ((strict == false))) {s = 9;}

                        else if ( (LA36_2==LEFT) && ((strict == false))) {s = 10;}

                        else if ( (LA36_2==OUTER) && ((strict == false))) {s = 11;}

                        else if ( (LA36_2==ON) && ((strict == false))) {s = 12;}

                        else if ( (LA36_2==WHERE) && ((strict == false))) {s = 13;}

                        else if ( (LA36_2==OR) && ((strict == false))) {s = 14;}

                        else if ( (LA36_2==AND) && ((strict == false))) {s = 15;}

                        else if ( (LA36_2==NOT) && ((strict == false))) {s = 16;}

                        else if ( (LA36_2==IN) && ((strict == false))) {s = 17;}

                        else if ( (LA36_2==LIKE) && ((strict == false))) {s = 18;}

                        else if ( (LA36_2==IS) && ((strict == false))) {s = 19;}

                        else if ( (LA36_2==NULL) && ((strict == false))) {s = 20;}

                        else if ( (LA36_2==ANY) && ((strict == false))) {s = 21;}

                        else if ( (LA36_2==CONTAINS) && ((strict == false))) {s = 22;}

                        else if ( (LA36_2==IN_FOLDER) && ((strict == false))) {s = 23;}

                        else if ( (LA36_2==IN_TREE) && ((strict == false))) {s = 24;}

                        else if ( (LA36_2==ORDER) && ((strict == false))) {s = 25;}

                        else if ( (LA36_2==BY) && ((strict == false))) {s = 26;}

                        else if ( (LA36_2==ASC) && ((strict == false))) {s = 27;}

                        else if ( (LA36_2==DESC) && ((strict == false))) {s = 28;}

                        else if ( (LA36_2==TIMESTAMP) && ((strict == false))) {s = 29;}

                        else if ( (LA36_2==TRUE) && ((strict == false))) {s = 30;}

                        else if ( (LA36_2==FALSE) && ((strict == false))) {s = 31;}

                        else if ( (LA36_2==SCORE) && ((strict == false))) {s = 32;}

                        else if ( (LA36_2==ID) && ((strict == false))) {s = 33;}

                         
                        input.seek(index36_2);
                        if ( s>=0 ) return s;
                        break;
                    case 15 : 
                        int LA36_68 = input.LA(1);

                         
                        int index36_68 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA36_68==NULL) && ((strict == false))) {s = 71;}

                        else if ( (LA36_68==NOT) && ((strict == false))) {s = 72;}

                         
                        input.seek(index36_68);
                        if ( s>=0 ) return s;
                        break;
                    case 16 : 
                        int LA36_78 = input.LA(1);

                         
                        int index36_78 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA36_78==DOUBLE_QUOTE) && ((strict == false))) {s = 104;}

                         
                        input.seek(index36_78);
                        if ( s>=0 ) return s;
                        break;
                    case 17 : 
                        int LA36_79 = input.LA(1);

                         
                        int index36_79 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA36_79==DOUBLE_QUOTE) && ((strict == false))) {s = 104;}

                         
                        input.seek(index36_79);
                        if ( s>=0 ) return s;
                        break;
                    case 18 : 
                        int LA36_8 = input.LA(1);

                         
                        int index36_8 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA36_8==DOUBLE_QUOTE) && ((strict == false))) {s = 38;}

                         
                        input.seek(index36_8);
                        if ( s>=0 ) return s;
                        break;
                    case 19 : 
                        int LA36_80 = input.LA(1);

                         
                        int index36_80 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA36_80==DOUBLE_QUOTE) && ((strict == false))) {s = 104;}

                         
                        input.seek(index36_80);
                        if ( s>=0 ) return s;
                        break;
                    case 20 : 
                        int LA36_73 = input.LA(1);

                         
                        int index36_73 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA36_73==IS) && ((strict == false))) {s = 68;}

                         
                        input.seek(index36_73);
                        if ( s>=0 ) return s;
                        break;
                    case 21 : 
                        int LA36_28 = input.LA(1);

                         
                        int index36_28 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA36_28==DOUBLE_QUOTE) && ((strict == false))) {s = 38;}

                         
                        input.seek(index36_28);
                        if ( s>=0 ) return s;
                        break;
                    case 22 : 
                        int LA36_81 = input.LA(1);

                         
                        int index36_81 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA36_81==DOUBLE_QUOTE) && ((strict == false))) {s = 104;}

                         
                        input.seek(index36_81);
                        if ( s>=0 ) return s;
                        break;
                    case 23 : 
                        int LA36_17 = input.LA(1);

                         
                        int index36_17 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA36_17==DOUBLE_QUOTE) && ((strict == false))) {s = 38;}

                         
                        input.seek(index36_17);
                        if ( s>=0 ) return s;
                        break;
                    case 24 : 
                        int LA36_6 = input.LA(1);

                         
                        int index36_6 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA36_6==DOUBLE_QUOTE) && ((strict == false))) {s = 38;}

                         
                        input.seek(index36_6);
                        if ( s>=0 ) return s;
                        break;
                    case 25 : 
                        int LA36_75 = input.LA(1);

                         
                        int index36_75 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA36_75==DOUBLE_QUOTE) && ((strict == false))) {s = 104;}

                         
                        input.seek(index36_75);
                        if ( s>=0 ) return s;
                        break;
                    case 26 : 
                        int LA36_29 = input.LA(1);

                         
                        int index36_29 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA36_29==DOUBLE_QUOTE) && ((strict == false))) {s = 38;}

                         
                        input.seek(index36_29);
                        if ( s>=0 ) return s;
                        break;
                    case 27 : 
                        int LA36_19 = input.LA(1);

                         
                        int index36_19 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA36_19==DOUBLE_QUOTE) && ((strict == false))) {s = 38;}

                         
                        input.seek(index36_19);
                        if ( s>=0 ) return s;
                        break;
                    case 28 : 
                        int LA36_76 = input.LA(1);

                         
                        int index36_76 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA36_76==DOUBLE_QUOTE) && ((strict == false))) {s = 104;}

                         
                        input.seek(index36_76);
                        if ( s>=0 ) return s;
                        break;
                    case 29 : 
                        int LA36_18 = input.LA(1);

                         
                        int index36_18 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA36_18==DOUBLE_QUOTE) && ((strict == false))) {s = 38;}

                         
                        input.seek(index36_18);
                        if ( s>=0 ) return s;
                        break;
                    case 30 : 
                        int LA36_38 = input.LA(1);

                         
                        int index36_38 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA36_38==IS) && ((strict == false))) {s = 68;}

                        else if ( (LA36_38==DOT) && ((strict == false))) {s = 69;}

                         
                        input.seek(index36_38);
                        if ( s>=0 ) return s;
                        break;
                    case 31 : 
                        int LA36_7 = input.LA(1);

                         
                        int index36_7 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA36_7==DOUBLE_QUOTE) && ((strict == false))) {s = 38;}

                         
                        input.seek(index36_7);
                        if ( s>=0 ) return s;
                        break;
                    case 32 : 
                        int LA36_77 = input.LA(1);

                         
                        int index36_77 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA36_77==DOUBLE_QUOTE) && ((strict == false))) {s = 104;}

                         
                        input.seek(index36_77);
                        if ( s>=0 ) return s;
                        break;
                    case 33 : 
                        int LA36_0 = input.LA(1);

                         
                        int index36_0 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA36_0==ID) ) {s = 1;}

                        else if ( (LA36_0==DOUBLE_QUOTE) && ((strict == false))) {s = 2;}

                         
                        input.seek(index36_0);
                        if ( s>=0 ) return s;
                        break;
                    case 34 : 
                        int LA36_15 = input.LA(1);

                         
                        int index36_15 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA36_15==DOUBLE_QUOTE) && ((strict == false))) {s = 38;}

                         
                        input.seek(index36_15);
                        if ( s>=0 ) return s;
                        break;
                    case 35 : 
                        int LA36_85 = input.LA(1);

                         
                        int index36_85 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA36_85==DOUBLE_QUOTE) && ((strict == false))) {s = 104;}

                         
                        input.seek(index36_85);
                        if ( s>=0 ) return s;
                        break;
                    case 36 : 
                        int LA36_25 = input.LA(1);

                         
                        int index36_25 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA36_25==DOUBLE_QUOTE) && ((strict == false))) {s = 38;}

                         
                        input.seek(index36_25);
                        if ( s>=0 ) return s;
                        break;
                    case 37 : 
                        int LA36_5 = input.LA(1);

                         
                        int index36_5 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA36_5==DOUBLE_QUOTE) && ((strict == false))) {s = 38;}

                         
                        input.seek(index36_5);
                        if ( s>=0 ) return s;
                        break;
                    case 38 : 
                        int LA36_84 = input.LA(1);

                         
                        int index36_84 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA36_84==DOUBLE_QUOTE) && ((strict == false))) {s = 104;}

                         
                        input.seek(index36_84);
                        if ( s>=0 ) return s;
                        break;
                    case 39 : 
                        int LA36_33 = input.LA(1);

                         
                        int index36_33 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA36_33==DOUBLE_QUOTE) && ((strict == false))) {s = 38;}

                         
                        input.seek(index36_33);
                        if ( s>=0 ) return s;
                        break;
                    case 40 : 
                        int LA36_74 = input.LA(1);

                         
                        int index36_74 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA36_74==SELECT) && ((strict == false))) {s = 75;}

                        else if ( (LA36_74==AS) && ((strict == false))) {s = 76;}

                        else if ( (LA36_74==FROM) && ((strict == false))) {s = 77;}

                        else if ( (LA36_74==JOIN) && ((strict == false))) {s = 78;}

                        else if ( (LA36_74==INNER) && ((strict == false))) {s = 79;}

                        else if ( (LA36_74==LEFT) && ((strict == false))) {s = 80;}

                        else if ( (LA36_74==OUTER) && ((strict == false))) {s = 81;}

                        else if ( (LA36_74==ON) && ((strict == false))) {s = 82;}

                        else if ( (LA36_74==WHERE) && ((strict == false))) {s = 83;}

                        else if ( (LA36_74==OR) && ((strict == false))) {s = 84;}

                        else if ( (LA36_74==AND) && ((strict == false))) {s = 85;}

                        else if ( (LA36_74==NOT) && ((strict == false))) {s = 86;}

                        else if ( (LA36_74==IN) && ((strict == false))) {s = 87;}

                        else if ( (LA36_74==LIKE) && ((strict == false))) {s = 88;}

                        else if ( (LA36_74==IS) && ((strict == false))) {s = 89;}

                        else if ( (LA36_74==NULL) && ((strict == false))) {s = 90;}

                        else if ( (LA36_74==ANY) && ((strict == false))) {s = 91;}

                        else if ( (LA36_74==CONTAINS) && ((strict == false))) {s = 92;}

                        else if ( (LA36_74==IN_FOLDER) && ((strict == false))) {s = 93;}

                        else if ( (LA36_74==IN_TREE) && ((strict == false))) {s = 94;}

                        else if ( (LA36_74==ORDER) && ((strict == false))) {s = 95;}

                        else if ( (LA36_74==BY) && ((strict == false))) {s = 96;}

                        else if ( (LA36_74==ASC) && ((strict == false))) {s = 97;}

                        else if ( (LA36_74==DESC) && ((strict == false))) {s = 98;}

                        else if ( (LA36_74==TIMESTAMP) && ((strict == false))) {s = 99;}

                        else if ( (LA36_74==TRUE) && ((strict == false))) {s = 100;}

                        else if ( (LA36_74==FALSE) && ((strict == false))) {s = 101;}

                        else if ( (LA36_74==SCORE) && ((strict == false))) {s = 102;}

                        else if ( (LA36_74==ID) && ((strict == false))) {s = 103;}

                         
                        input.seek(index36_74);
                        if ( s>=0 ) return s;
                        break;
                    case 41 : 
                        int LA36_83 = input.LA(1);

                         
                        int index36_83 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA36_83==DOUBLE_QUOTE) && ((strict == false))) {s = 104;}

                         
                        input.seek(index36_83);
                        if ( s>=0 ) return s;
                        break;
                    case 42 : 
                        int LA36_14 = input.LA(1);

                         
                        int index36_14 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA36_14==DOUBLE_QUOTE) && ((strict == false))) {s = 38;}

                         
                        input.seek(index36_14);
                        if ( s>=0 ) return s;
                        break;
                    case 43 : 
                        int LA36_82 = input.LA(1);

                         
                        int index36_82 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA36_82==DOUBLE_QUOTE) && ((strict == false))) {s = 104;}

                         
                        input.seek(index36_82);
                        if ( s>=0 ) return s;
                        break;
                    case 44 : 
                        int LA36_89 = input.LA(1);

                         
                        int index36_89 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA36_89==DOUBLE_QUOTE) && ((strict == false))) {s = 104;}

                         
                        input.seek(index36_89);
                        if ( s>=0 ) return s;
                        break;
                    case 45 : 
                        int LA36_26 = input.LA(1);

                         
                        int index36_26 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA36_26==DOUBLE_QUOTE) && ((strict == false))) {s = 38;}

                         
                        input.seek(index36_26);
                        if ( s>=0 ) return s;
                        break;
                    case 46 : 
                        int LA36_16 = input.LA(1);

                         
                        int index36_16 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA36_16==DOUBLE_QUOTE) && ((strict == false))) {s = 38;}

                         
                        input.seek(index36_16);
                        if ( s>=0 ) return s;
                        break;
                    case 47 : 
                        int LA36_88 = input.LA(1);

                         
                        int index36_88 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA36_88==DOUBLE_QUOTE) && ((strict == false))) {s = 104;}

                         
                        input.seek(index36_88);
                        if ( s>=0 ) return s;
                        break;
                    case 48 : 
                        int LA36_27 = input.LA(1);

                         
                        int index36_27 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA36_27==DOUBLE_QUOTE) && ((strict == false))) {s = 38;}

                         
                        input.seek(index36_27);
                        if ( s>=0 ) return s;
                        break;
                    case 49 : 
                        int LA36_87 = input.LA(1);

                         
                        int index36_87 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA36_87==DOUBLE_QUOTE) && ((strict == false))) {s = 104;}

                         
                        input.seek(index36_87);
                        if ( s>=0 ) return s;
                        break;
                    case 50 : 
                        int LA36_32 = input.LA(1);

                         
                        int index36_32 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA36_32==DOUBLE_QUOTE) && ((strict == false))) {s = 38;}

                         
                        input.seek(index36_32);
                        if ( s>=0 ) return s;
                        break;
                    case 51 : 
                        int LA36_86 = input.LA(1);

                         
                        int index36_86 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA36_86==DOUBLE_QUOTE) && ((strict == false))) {s = 104;}

                         
                        input.seek(index36_86);
                        if ( s>=0 ) return s;
                        break;
                    case 52 : 
                        int LA36_93 = input.LA(1);

                         
                        int index36_93 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA36_93==DOUBLE_QUOTE) && ((strict == false))) {s = 104;}

                         
                        input.seek(index36_93);
                        if ( s>=0 ) return s;
                        break;
                    case 53 : 
                        int LA36_12 = input.LA(1);

                         
                        int index36_12 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA36_12==DOUBLE_QUOTE) && ((strict == false))) {s = 38;}

                         
                        input.seek(index36_12);
                        if ( s>=0 ) return s;
                        break;
                    case 54 : 
                        int LA36_92 = input.LA(1);

                         
                        int index36_92 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA36_92==DOUBLE_QUOTE) && ((strict == false))) {s = 104;}

                         
                        input.seek(index36_92);
                        if ( s>=0 ) return s;
                        break;
                    case 55 : 
                        int LA36_22 = input.LA(1);

                         
                        int index36_22 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA36_22==DOUBLE_QUOTE) && ((strict == false))) {s = 38;}

                         
                        input.seek(index36_22);
                        if ( s>=0 ) return s;
                        break;
                    case 56 : 
                        int LA36_91 = input.LA(1);

                         
                        int index36_91 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA36_91==DOUBLE_QUOTE) && ((strict == false))) {s = 104;}

                         
                        input.seek(index36_91);
                        if ( s>=0 ) return s;
                        break;
                    case 57 : 
                        int LA36_23 = input.LA(1);

                         
                        int index36_23 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA36_23==DOUBLE_QUOTE) && ((strict == false))) {s = 38;}

                         
                        input.seek(index36_23);
                        if ( s>=0 ) return s;
                        break;
                    case 58 : 
                        int LA36_90 = input.LA(1);

                         
                        int index36_90 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA36_90==DOUBLE_QUOTE) && ((strict == false))) {s = 104;}

                         
                        input.seek(index36_90);
                        if ( s>=0 ) return s;
                        break;
                    case 59 : 
                        int LA36_102 = input.LA(1);

                         
                        int index36_102 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA36_102==DOUBLE_QUOTE) && ((strict == false))) {s = 104;}

                         
                        input.seek(index36_102);
                        if ( s>=0 ) return s;
                        break;
                    case 60 : 
                        int LA36_97 = input.LA(1);

                         
                        int index36_97 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA36_97==DOUBLE_QUOTE) && ((strict == false))) {s = 104;}

                         
                        input.seek(index36_97);
                        if ( s>=0 ) return s;
                        break;
                    case 61 : 
                        int LA36_96 = input.LA(1);

                         
                        int index36_96 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA36_96==DOUBLE_QUOTE) && ((strict == false))) {s = 104;}

                         
                        input.seek(index36_96);
                        if ( s>=0 ) return s;
                        break;
                    case 62 : 
                        int LA36_13 = input.LA(1);

                         
                        int index36_13 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA36_13==DOUBLE_QUOTE) && ((strict == false))) {s = 38;}

                         
                        input.seek(index36_13);
                        if ( s>=0 ) return s;
                        break;
                    case 63 : 
                        int LA36_95 = input.LA(1);

                         
                        int index36_95 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA36_95==DOUBLE_QUOTE) && ((strict == false))) {s = 104;}

                         
                        input.seek(index36_95);
                        if ( s>=0 ) return s;
                        break;
                    case 64 : 
                        int LA36_24 = input.LA(1);

                         
                        int index36_24 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA36_24==DOUBLE_QUOTE) && ((strict == false))) {s = 38;}

                         
                        input.seek(index36_24);
                        if ( s>=0 ) return s;
                        break;
                    case 65 : 
                        int LA36_94 = input.LA(1);

                         
                        int index36_94 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA36_94==DOUBLE_QUOTE) && ((strict == false))) {s = 104;}

                         
                        input.seek(index36_94);
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
    static final String DFA34_eotS =
        "\77\uffff";
    static final String DFA34_eofS =
        "\77\uffff";
    static final String DFA34_minS =
        "\1\105\1\0\1\37\2\uffff\35\106\35\0";
    static final String DFA34_maxS =
        "\1\106\1\0\1\114\2\uffff\35\106\35\0";
    static final String DFA34_acceptS =
        "\3\uffff\1\1\1\2\72\uffff";
    static final String DFA34_specialS =
        "\1\62\1\72\1\44\2\uffff\1\57\1\50\1\54\1\45\1\42\1\2\1\4\1\67\1"+
        "\73\1\61\1\55\1\64\1\47\1\53\1\52\1\0\1\43\1\70\1\71\1\74\1\56\1"+
        "\63\1\65\1\46\1\51\1\1\1\3\1\66\1\60\1\23\1\24\1\25\1\26\1\27\1"+
        "\30\1\31\1\32\1\33\1\34\1\35\1\36\1\37\1\40\1\41\1\22\1\21\1\20"+
        "\1\17\1\16\1\15\1\14\1\13\1\12\1\11\1\10\1\7\1\6\1\5}>";
    static final String[] DFA34_transitionS = {
            "\1\1\1\2",
            "\1\uffff",
            "\1\5\2\uffff\1\6\4\uffff\1\7\1\10\1\11\1\12\1\13\1\14\1\uffff"+
            "\1\15\1\16\1\17\1\20\5\uffff\1\21\1\22\1\23\1\24\1\25\1\26\1"+
            "\27\1\30\1\31\1\32\1\33\1\34\2\uffff\1\41\3\uffff\1\36\1\37"+
            "\1\35\1\40",
            "",
            "",
            "\1\42",
            "\1\43",
            "\1\44",
            "\1\45",
            "\1\46",
            "\1\47",
            "\1\50",
            "\1\51",
            "\1\52",
            "\1\53",
            "\1\54",
            "\1\55",
            "\1\56",
            "\1\57",
            "\1\60",
            "\1\61",
            "\1\62",
            "\1\63",
            "\1\64",
            "\1\65",
            "\1\66",
            "\1\67",
            "\1\70",
            "\1\71",
            "\1\72",
            "\1\73",
            "\1\74",
            "\1\75",
            "\1\76",
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
            "\1\uffff"
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
            return "516:4: ( ( columnReference )=> columnReference | multiValuedColumnReference )";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA34_20 = input.LA(1);

                         
                        int index34_20 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA34_20==DOUBLE_QUOTE) && ((strict == false))) {s = 49;}

                         
                        input.seek(index34_20);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA34_30 = input.LA(1);

                         
                        int index34_30 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA34_30==DOUBLE_QUOTE) && ((strict == false))) {s = 59;}

                         
                        input.seek(index34_30);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA34_10 = input.LA(1);

                         
                        int index34_10 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA34_10==DOUBLE_QUOTE) && ((strict == false))) {s = 39;}

                         
                        input.seek(index34_10);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA34_31 = input.LA(1);

                         
                        int index34_31 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA34_31==DOUBLE_QUOTE) && ((strict == false))) {s = 60;}

                         
                        input.seek(index34_31);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA34_11 = input.LA(1);

                         
                        int index34_11 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA34_11==DOUBLE_QUOTE) && ((strict == false))) {s = 40;}

                         
                        input.seek(index34_11);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA34_62 = input.LA(1);

                         
                        int index34_62 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (((synpred3_CMIS()&&(strict == false))||(synpred3_CMIS()&&(strict == false))||(synpred3_CMIS()&&(strict == false)))) ) {s = 3;}

                        else if ( ((strict == false)) ) {s = 4;}

                         
                        input.seek(index34_62);
                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA34_61 = input.LA(1);

                         
                        int index34_61 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (((synpred3_CMIS()&&(strict == false))||(synpred3_CMIS()&&(strict == false))||(synpred3_CMIS()&&(strict == false)))) ) {s = 3;}

                        else if ( ((strict == false)) ) {s = 4;}

                         
                        input.seek(index34_61);
                        if ( s>=0 ) return s;
                        break;
                    case 7 : 
                        int LA34_60 = input.LA(1);

                         
                        int index34_60 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (((synpred3_CMIS()&&(strict == false))||(synpred3_CMIS()&&(strict == false))||(synpred3_CMIS()&&(strict == false)))) ) {s = 3;}

                        else if ( ((strict == false)) ) {s = 4;}

                         
                        input.seek(index34_60);
                        if ( s>=0 ) return s;
                        break;
                    case 8 : 
                        int LA34_59 = input.LA(1);

                         
                        int index34_59 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (((synpred3_CMIS()&&(strict == false))||(synpred3_CMIS()&&(strict == false))||(synpred3_CMIS()&&(strict == false)))) ) {s = 3;}

                        else if ( ((strict == false)) ) {s = 4;}

                         
                        input.seek(index34_59);
                        if ( s>=0 ) return s;
                        break;
                    case 9 : 
                        int LA34_58 = input.LA(1);

                         
                        int index34_58 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (((synpred3_CMIS()&&(strict == false))||(synpred3_CMIS()&&(strict == false))||(synpred3_CMIS()&&(strict == false)))) ) {s = 3;}

                        else if ( ((strict == false)) ) {s = 4;}

                         
                        input.seek(index34_58);
                        if ( s>=0 ) return s;
                        break;
                    case 10 : 
                        int LA34_57 = input.LA(1);

                         
                        int index34_57 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (((synpred3_CMIS()&&(strict == false))||(synpred3_CMIS()&&(strict == false))||(synpred3_CMIS()&&(strict == false)))) ) {s = 3;}

                        else if ( ((strict == false)) ) {s = 4;}

                         
                        input.seek(index34_57);
                        if ( s>=0 ) return s;
                        break;
                    case 11 : 
                        int LA34_56 = input.LA(1);

                         
                        int index34_56 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (((synpred3_CMIS()&&(strict == false))||(synpred3_CMIS()&&(strict == false))||(synpred3_CMIS()&&(strict == false)))) ) {s = 3;}

                        else if ( ((strict == false)) ) {s = 4;}

                         
                        input.seek(index34_56);
                        if ( s>=0 ) return s;
                        break;
                    case 12 : 
                        int LA34_55 = input.LA(1);

                         
                        int index34_55 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (((synpred3_CMIS()&&(strict == false))||(synpred3_CMIS()&&(strict == false))||(synpred3_CMIS()&&(strict == false)))) ) {s = 3;}

                        else if ( ((strict == false)) ) {s = 4;}

                         
                        input.seek(index34_55);
                        if ( s>=0 ) return s;
                        break;
                    case 13 : 
                        int LA34_54 = input.LA(1);

                         
                        int index34_54 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (((synpred3_CMIS()&&(strict == false))||(synpred3_CMIS()&&(strict == false))||(synpred3_CMIS()&&(strict == false)))) ) {s = 3;}

                        else if ( ((strict == false)) ) {s = 4;}

                         
                        input.seek(index34_54);
                        if ( s>=0 ) return s;
                        break;
                    case 14 : 
                        int LA34_53 = input.LA(1);

                         
                        int index34_53 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (((synpred3_CMIS()&&(strict == false))||(synpred3_CMIS()&&(strict == false))||(synpred3_CMIS()&&(strict == false)))) ) {s = 3;}

                        else if ( ((strict == false)) ) {s = 4;}

                         
                        input.seek(index34_53);
                        if ( s>=0 ) return s;
                        break;
                    case 15 : 
                        int LA34_52 = input.LA(1);

                         
                        int index34_52 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (((synpred3_CMIS()&&(strict == false))||(synpred3_CMIS()&&(strict == false))||(synpred3_CMIS()&&(strict == false)))) ) {s = 3;}

                        else if ( ((strict == false)) ) {s = 4;}

                         
                        input.seek(index34_52);
                        if ( s>=0 ) return s;
                        break;
                    case 16 : 
                        int LA34_51 = input.LA(1);

                         
                        int index34_51 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (((synpred3_CMIS()&&(strict == false))||(synpred3_CMIS()&&(strict == false))||(synpred3_CMIS()&&(strict == false)))) ) {s = 3;}

                        else if ( ((strict == false)) ) {s = 4;}

                         
                        input.seek(index34_51);
                        if ( s>=0 ) return s;
                        break;
                    case 17 : 
                        int LA34_50 = input.LA(1);

                         
                        int index34_50 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (((synpred3_CMIS()&&(strict == false))||(synpred3_CMIS()&&(strict == false))||(synpred3_CMIS()&&(strict == false)))) ) {s = 3;}

                        else if ( ((strict == false)) ) {s = 4;}

                         
                        input.seek(index34_50);
                        if ( s>=0 ) return s;
                        break;
                    case 18 : 
                        int LA34_49 = input.LA(1);

                         
                        int index34_49 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (((synpred3_CMIS()&&(strict == false))||(synpred3_CMIS()&&(strict == false))||(synpred3_CMIS()&&(strict == false)))) ) {s = 3;}

                        else if ( ((strict == false)) ) {s = 4;}

                         
                        input.seek(index34_49);
                        if ( s>=0 ) return s;
                        break;
                    case 19 : 
                        int LA34_34 = input.LA(1);

                         
                        int index34_34 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (((synpred3_CMIS()&&(strict == false))||(synpred3_CMIS()&&(strict == false))||(synpred3_CMIS()&&(strict == false)))) ) {s = 3;}

                        else if ( ((strict == false)) ) {s = 4;}

                         
                        input.seek(index34_34);
                        if ( s>=0 ) return s;
                        break;
                    case 20 : 
                        int LA34_35 = input.LA(1);

                         
                        int index34_35 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (((synpred3_CMIS()&&(strict == false))||(synpred3_CMIS()&&(strict == false))||(synpred3_CMIS()&&(strict == false)))) ) {s = 3;}

                        else if ( ((strict == false)) ) {s = 4;}

                         
                        input.seek(index34_35);
                        if ( s>=0 ) return s;
                        break;
                    case 21 : 
                        int LA34_36 = input.LA(1);

                         
                        int index34_36 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (((synpred3_CMIS()&&(strict == false))||(synpred3_CMIS()&&(strict == false))||(synpred3_CMIS()&&(strict == false)))) ) {s = 3;}

                        else if ( ((strict == false)) ) {s = 4;}

                         
                        input.seek(index34_36);
                        if ( s>=0 ) return s;
                        break;
                    case 22 : 
                        int LA34_37 = input.LA(1);

                         
                        int index34_37 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (((synpred3_CMIS()&&(strict == false))||(synpred3_CMIS()&&(strict == false))||(synpred3_CMIS()&&(strict == false)))) ) {s = 3;}

                        else if ( ((strict == false)) ) {s = 4;}

                         
                        input.seek(index34_37);
                        if ( s>=0 ) return s;
                        break;
                    case 23 : 
                        int LA34_38 = input.LA(1);

                         
                        int index34_38 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (((synpred3_CMIS()&&(strict == false))||(synpred3_CMIS()&&(strict == false))||(synpred3_CMIS()&&(strict == false)))) ) {s = 3;}

                        else if ( ((strict == false)) ) {s = 4;}

                         
                        input.seek(index34_38);
                        if ( s>=0 ) return s;
                        break;
                    case 24 : 
                        int LA34_39 = input.LA(1);

                         
                        int index34_39 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (((synpred3_CMIS()&&(strict == false))||(synpred3_CMIS()&&(strict == false))||(synpred3_CMIS()&&(strict == false)))) ) {s = 3;}

                        else if ( ((strict == false)) ) {s = 4;}

                         
                        input.seek(index34_39);
                        if ( s>=0 ) return s;
                        break;
                    case 25 : 
                        int LA34_40 = input.LA(1);

                         
                        int index34_40 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (((synpred3_CMIS()&&(strict == false))||(synpred3_CMIS()&&(strict == false))||(synpred3_CMIS()&&(strict == false)))) ) {s = 3;}

                        else if ( ((strict == false)) ) {s = 4;}

                         
                        input.seek(index34_40);
                        if ( s>=0 ) return s;
                        break;
                    case 26 : 
                        int LA34_41 = input.LA(1);

                         
                        int index34_41 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (((synpred3_CMIS()&&(strict == false))||(synpred3_CMIS()&&(strict == false))||(synpred3_CMIS()&&(strict == false)))) ) {s = 3;}

                        else if ( ((strict == false)) ) {s = 4;}

                         
                        input.seek(index34_41);
                        if ( s>=0 ) return s;
                        break;
                    case 27 : 
                        int LA34_42 = input.LA(1);

                         
                        int index34_42 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (((synpred3_CMIS()&&(strict == false))||(synpred3_CMIS()&&(strict == false))||(synpred3_CMIS()&&(strict == false)))) ) {s = 3;}

                        else if ( ((strict == false)) ) {s = 4;}

                         
                        input.seek(index34_42);
                        if ( s>=0 ) return s;
                        break;
                    case 28 : 
                        int LA34_43 = input.LA(1);

                         
                        int index34_43 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (((synpred3_CMIS()&&(strict == false))||(synpred3_CMIS()&&(strict == false))||(synpred3_CMIS()&&(strict == false)))) ) {s = 3;}

                        else if ( ((strict == false)) ) {s = 4;}

                         
                        input.seek(index34_43);
                        if ( s>=0 ) return s;
                        break;
                    case 29 : 
                        int LA34_44 = input.LA(1);

                         
                        int index34_44 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (((synpred3_CMIS()&&(strict == false))||(synpred3_CMIS()&&(strict == false))||(synpred3_CMIS()&&(strict == false)))) ) {s = 3;}

                        else if ( ((strict == false)) ) {s = 4;}

                         
                        input.seek(index34_44);
                        if ( s>=0 ) return s;
                        break;
                    case 30 : 
                        int LA34_45 = input.LA(1);

                         
                        int index34_45 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (((synpred3_CMIS()&&(strict == false))||(synpred3_CMIS()&&(strict == false))||(synpred3_CMIS()&&(strict == false)))) ) {s = 3;}

                        else if ( ((strict == false)) ) {s = 4;}

                         
                        input.seek(index34_45);
                        if ( s>=0 ) return s;
                        break;
                    case 31 : 
                        int LA34_46 = input.LA(1);

                         
                        int index34_46 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (((synpred3_CMIS()&&(strict == false))||(synpred3_CMIS()&&(strict == false))||(synpred3_CMIS()&&(strict == false)))) ) {s = 3;}

                        else if ( ((strict == false)) ) {s = 4;}

                         
                        input.seek(index34_46);
                        if ( s>=0 ) return s;
                        break;
                    case 32 : 
                        int LA34_47 = input.LA(1);

                         
                        int index34_47 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (((synpred3_CMIS()&&(strict == false))||(synpred3_CMIS()&&(strict == false))||(synpred3_CMIS()&&(strict == false)))) ) {s = 3;}

                        else if ( ((strict == false)) ) {s = 4;}

                         
                        input.seek(index34_47);
                        if ( s>=0 ) return s;
                        break;
                    case 33 : 
                        int LA34_48 = input.LA(1);

                         
                        int index34_48 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (((synpred3_CMIS()&&(strict == false))||(synpred3_CMIS()&&(strict == false))||(synpred3_CMIS()&&(strict == false)))) ) {s = 3;}

                        else if ( ((strict == false)) ) {s = 4;}

                         
                        input.seek(index34_48);
                        if ( s>=0 ) return s;
                        break;
                    case 34 : 
                        int LA34_9 = input.LA(1);

                         
                        int index34_9 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA34_9==DOUBLE_QUOTE) && ((strict == false))) {s = 38;}

                         
                        input.seek(index34_9);
                        if ( s>=0 ) return s;
                        break;
                    case 35 : 
                        int LA34_21 = input.LA(1);

                         
                        int index34_21 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA34_21==DOUBLE_QUOTE) && ((strict == false))) {s = 50;}

                         
                        input.seek(index34_21);
                        if ( s>=0 ) return s;
                        break;
                    case 36 : 
                        int LA34_2 = input.LA(1);

                         
                        int index34_2 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA34_2==SELECT) && ((strict == false))) {s = 5;}

                        else if ( (LA34_2==AS) && ((strict == false))) {s = 6;}

                        else if ( (LA34_2==FROM) && ((strict == false))) {s = 7;}

                        else if ( (LA34_2==JOIN) && ((strict == false))) {s = 8;}

                        else if ( (LA34_2==INNER) && ((strict == false))) {s = 9;}

                        else if ( (LA34_2==LEFT) && ((strict == false))) {s = 10;}

                        else if ( (LA34_2==OUTER) && ((strict == false))) {s = 11;}

                        else if ( (LA34_2==ON) && ((strict == false))) {s = 12;}

                        else if ( (LA34_2==WHERE) && ((strict == false))) {s = 13;}

                        else if ( (LA34_2==OR) && ((strict == false))) {s = 14;}

                        else if ( (LA34_2==AND) && ((strict == false))) {s = 15;}

                        else if ( (LA34_2==NOT) && ((strict == false))) {s = 16;}

                        else if ( (LA34_2==IN) && ((strict == false))) {s = 17;}

                        else if ( (LA34_2==LIKE) && ((strict == false))) {s = 18;}

                        else if ( (LA34_2==IS) && ((strict == false))) {s = 19;}

                        else if ( (LA34_2==NULL) && ((strict == false))) {s = 20;}

                        else if ( (LA34_2==ANY) && ((strict == false))) {s = 21;}

                        else if ( (LA34_2==CONTAINS) && ((strict == false))) {s = 22;}

                        else if ( (LA34_2==IN_FOLDER) && ((strict == false))) {s = 23;}

                        else if ( (LA34_2==IN_TREE) && ((strict == false))) {s = 24;}

                        else if ( (LA34_2==ORDER) && ((strict == false))) {s = 25;}

                        else if ( (LA34_2==BY) && ((strict == false))) {s = 26;}

                        else if ( (LA34_2==ASC) && ((strict == false))) {s = 27;}

                        else if ( (LA34_2==DESC) && ((strict == false))) {s = 28;}

                        else if ( (LA34_2==TIMESTAMP) && ((strict == false))) {s = 29;}

                        else if ( (LA34_2==TRUE) && ((strict == false))) {s = 30;}

                        else if ( (LA34_2==FALSE) && ((strict == false))) {s = 31;}

                        else if ( (LA34_2==SCORE) && ((strict == false))) {s = 32;}

                        else if ( (LA34_2==ID) && ((strict == false))) {s = 33;}

                         
                        input.seek(index34_2);
                        if ( s>=0 ) return s;
                        break;
                    case 37 : 
                        int LA34_8 = input.LA(1);

                         
                        int index34_8 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA34_8==DOUBLE_QUOTE) && ((strict == false))) {s = 37;}

                         
                        input.seek(index34_8);
                        if ( s>=0 ) return s;
                        break;
                    case 38 : 
                        int LA34_28 = input.LA(1);

                         
                        int index34_28 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA34_28==DOUBLE_QUOTE) && ((strict == false))) {s = 57;}

                         
                        input.seek(index34_28);
                        if ( s>=0 ) return s;
                        break;
                    case 39 : 
                        int LA34_17 = input.LA(1);

                         
                        int index34_17 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA34_17==DOUBLE_QUOTE) && ((strict == false))) {s = 46;}

                         
                        input.seek(index34_17);
                        if ( s>=0 ) return s;
                        break;
                    case 40 : 
                        int LA34_6 = input.LA(1);

                         
                        int index34_6 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA34_6==DOUBLE_QUOTE) && ((strict == false))) {s = 35;}

                         
                        input.seek(index34_6);
                        if ( s>=0 ) return s;
                        break;
                    case 41 : 
                        int LA34_29 = input.LA(1);

                         
                        int index34_29 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA34_29==DOUBLE_QUOTE) && ((strict == false))) {s = 58;}

                         
                        input.seek(index34_29);
                        if ( s>=0 ) return s;
                        break;
                    case 42 : 
                        int LA34_19 = input.LA(1);

                         
                        int index34_19 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA34_19==DOUBLE_QUOTE) && ((strict == false))) {s = 48;}

                         
                        input.seek(index34_19);
                        if ( s>=0 ) return s;
                        break;
                    case 43 : 
                        int LA34_18 = input.LA(1);

                         
                        int index34_18 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA34_18==DOUBLE_QUOTE) && ((strict == false))) {s = 47;}

                         
                        input.seek(index34_18);
                        if ( s>=0 ) return s;
                        break;
                    case 44 : 
                        int LA34_7 = input.LA(1);

                         
                        int index34_7 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA34_7==DOUBLE_QUOTE) && ((strict == false))) {s = 36;}

                         
                        input.seek(index34_7);
                        if ( s>=0 ) return s;
                        break;
                    case 45 : 
                        int LA34_15 = input.LA(1);

                         
                        int index34_15 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA34_15==DOUBLE_QUOTE) && ((strict == false))) {s = 44;}

                         
                        input.seek(index34_15);
                        if ( s>=0 ) return s;
                        break;
                    case 46 : 
                        int LA34_25 = input.LA(1);

                         
                        int index34_25 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA34_25==DOUBLE_QUOTE) && ((strict == false))) {s = 54;}

                         
                        input.seek(index34_25);
                        if ( s>=0 ) return s;
                        break;
                    case 47 : 
                        int LA34_5 = input.LA(1);

                         
                        int index34_5 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA34_5==DOUBLE_QUOTE) && ((strict == false))) {s = 34;}

                         
                        input.seek(index34_5);
                        if ( s>=0 ) return s;
                        break;
                    case 48 : 
                        int LA34_33 = input.LA(1);

                         
                        int index34_33 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA34_33==DOUBLE_QUOTE) && ((strict == false))) {s = 62;}

                         
                        input.seek(index34_33);
                        if ( s>=0 ) return s;
                        break;
                    case 49 : 
                        int LA34_14 = input.LA(1);

                         
                        int index34_14 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA34_14==DOUBLE_QUOTE) && ((strict == false))) {s = 43;}

                         
                        input.seek(index34_14);
                        if ( s>=0 ) return s;
                        break;
                    case 50 : 
                        int LA34_0 = input.LA(1);

                         
                        int index34_0 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA34_0==ID) ) {s = 1;}

                        else if ( (LA34_0==DOUBLE_QUOTE) && ((strict == false))) {s = 2;}

                         
                        input.seek(index34_0);
                        if ( s>=0 ) return s;
                        break;
                    case 51 : 
                        int LA34_26 = input.LA(1);

                         
                        int index34_26 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA34_26==DOUBLE_QUOTE) && ((strict == false))) {s = 55;}

                         
                        input.seek(index34_26);
                        if ( s>=0 ) return s;
                        break;
                    case 52 : 
                        int LA34_16 = input.LA(1);

                         
                        int index34_16 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA34_16==DOUBLE_QUOTE) && ((strict == false))) {s = 45;}

                         
                        input.seek(index34_16);
                        if ( s>=0 ) return s;
                        break;
                    case 53 : 
                        int LA34_27 = input.LA(1);

                         
                        int index34_27 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA34_27==DOUBLE_QUOTE) && ((strict == false))) {s = 56;}

                         
                        input.seek(index34_27);
                        if ( s>=0 ) return s;
                        break;
                    case 54 : 
                        int LA34_32 = input.LA(1);

                         
                        int index34_32 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA34_32==DOUBLE_QUOTE) && ((strict == false))) {s = 61;}

                         
                        input.seek(index34_32);
                        if ( s>=0 ) return s;
                        break;
                    case 55 : 
                        int LA34_12 = input.LA(1);

                         
                        int index34_12 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA34_12==DOUBLE_QUOTE) && ((strict == false))) {s = 41;}

                         
                        input.seek(index34_12);
                        if ( s>=0 ) return s;
                        break;
                    case 56 : 
                        int LA34_22 = input.LA(1);

                         
                        int index34_22 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA34_22==DOUBLE_QUOTE) && ((strict == false))) {s = 51;}

                         
                        input.seek(index34_22);
                        if ( s>=0 ) return s;
                        break;
                    case 57 : 
                        int LA34_23 = input.LA(1);

                         
                        int index34_23 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA34_23==DOUBLE_QUOTE) && ((strict == false))) {s = 52;}

                         
                        input.seek(index34_23);
                        if ( s>=0 ) return s;
                        break;
                    case 58 : 
                        int LA34_1 = input.LA(1);

                         
                        int index34_1 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred3_CMIS()) ) {s = 3;}

                        else if ( (true) ) {s = 4;}

                         
                        input.seek(index34_1);
                        if ( s>=0 ) return s;
                        break;
                    case 59 : 
                        int LA34_13 = input.LA(1);

                         
                        int index34_13 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA34_13==DOUBLE_QUOTE) && ((strict == false))) {s = 42;}

                         
                        input.seek(index34_13);
                        if ( s>=0 ) return s;
                        break;
                    case 60 : 
                        int LA34_24 = input.LA(1);

                         
                        int index34_24 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA34_24==DOUBLE_QUOTE) && ((strict == false))) {s = 53;}

                         
                        input.seek(index34_24);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 34, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA35_eotS =
        "\77\uffff";
    static final String DFA35_eofS =
        "\77\uffff";
    static final String DFA35_minS =
        "\1\105\1\0\1\37\2\uffff\35\106\35\0";
    static final String DFA35_maxS =
        "\1\106\1\0\1\114\2\uffff\35\106\35\0";
    static final String DFA35_acceptS =
        "\3\uffff\1\1\1\2\72\uffff";
    static final String DFA35_specialS =
        "\1\32\1\24\1\7\2\uffff\1\22\1\13\1\17\1\10\1\5\1\2\1\4\1\33\1\73"+
        "\1\25\1\20\1\27\1\12\1\16\1\15\1\0\1\6\1\34\1\53\1\74\1\21\1\26"+
        "\1\30\1\11\1\14\1\1\1\3\1\31\1\23\1\54\1\55\1\56\1\57\1\60\1\61"+
        "\1\62\1\63\1\64\1\65\1\66\1\67\1\70\1\71\1\72\1\52\1\51\1\50\1\47"+
        "\1\46\1\45\1\44\1\43\1\42\1\41\1\40\1\37\1\36\1\35}>";
    static final String[] DFA35_transitionS = {
            "\1\1\1\2",
            "\1\uffff",
            "\1\5\2\uffff\1\6\4\uffff\1\7\1\10\1\11\1\12\1\13\1\14\1\uffff"+
            "\1\15\1\16\1\17\1\20\5\uffff\1\21\1\22\1\23\1\24\1\25\1\26\1"+
            "\27\1\30\1\31\1\32\1\33\1\34\2\uffff\1\41\3\uffff\1\36\1\37"+
            "\1\35\1\40",
            "",
            "",
            "\1\42",
            "\1\43",
            "\1\44",
            "\1\45",
            "\1\46",
            "\1\47",
            "\1\50",
            "\1\51",
            "\1\52",
            "\1\53",
            "\1\54",
            "\1\55",
            "\1\56",
            "\1\57",
            "\1\60",
            "\1\61",
            "\1\62",
            "\1\63",
            "\1\64",
            "\1\65",
            "\1\66",
            "\1\67",
            "\1\70",
            "\1\71",
            "\1\72",
            "\1\73",
            "\1\74",
            "\1\75",
            "\1\76",
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
            "\1\uffff"
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
            return "518:9: ( ( columnReference )=> columnReference | multiValuedColumnReference )";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA35_20 = input.LA(1);

                         
                        int index35_20 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA35_20==DOUBLE_QUOTE) && ((strict == false))) {s = 49;}

                         
                        input.seek(index35_20);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA35_30 = input.LA(1);

                         
                        int index35_30 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA35_30==DOUBLE_QUOTE) && ((strict == false))) {s = 59;}

                         
                        input.seek(index35_30);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA35_10 = input.LA(1);

                         
                        int index35_10 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA35_10==DOUBLE_QUOTE) && ((strict == false))) {s = 39;}

                         
                        input.seek(index35_10);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA35_31 = input.LA(1);

                         
                        int index35_31 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA35_31==DOUBLE_QUOTE) && ((strict == false))) {s = 60;}

                         
                        input.seek(index35_31);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA35_11 = input.LA(1);

                         
                        int index35_11 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA35_11==DOUBLE_QUOTE) && ((strict == false))) {s = 40;}

                         
                        input.seek(index35_11);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA35_9 = input.LA(1);

                         
                        int index35_9 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA35_9==DOUBLE_QUOTE) && ((strict == false))) {s = 38;}

                         
                        input.seek(index35_9);
                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA35_21 = input.LA(1);

                         
                        int index35_21 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA35_21==DOUBLE_QUOTE) && ((strict == false))) {s = 50;}

                         
                        input.seek(index35_21);
                        if ( s>=0 ) return s;
                        break;
                    case 7 : 
                        int LA35_2 = input.LA(1);

                         
                        int index35_2 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA35_2==SELECT) && ((strict == false))) {s = 5;}

                        else if ( (LA35_2==AS) && ((strict == false))) {s = 6;}

                        else if ( (LA35_2==FROM) && ((strict == false))) {s = 7;}

                        else if ( (LA35_2==JOIN) && ((strict == false))) {s = 8;}

                        else if ( (LA35_2==INNER) && ((strict == false))) {s = 9;}

                        else if ( (LA35_2==LEFT) && ((strict == false))) {s = 10;}

                        else if ( (LA35_2==OUTER) && ((strict == false))) {s = 11;}

                        else if ( (LA35_2==ON) && ((strict == false))) {s = 12;}

                        else if ( (LA35_2==WHERE) && ((strict == false))) {s = 13;}

                        else if ( (LA35_2==OR) && ((strict == false))) {s = 14;}

                        else if ( (LA35_2==AND) && ((strict == false))) {s = 15;}

                        else if ( (LA35_2==NOT) && ((strict == false))) {s = 16;}

                        else if ( (LA35_2==IN) && ((strict == false))) {s = 17;}

                        else if ( (LA35_2==LIKE) && ((strict == false))) {s = 18;}

                        else if ( (LA35_2==IS) && ((strict == false))) {s = 19;}

                        else if ( (LA35_2==NULL) && ((strict == false))) {s = 20;}

                        else if ( (LA35_2==ANY) && ((strict == false))) {s = 21;}

                        else if ( (LA35_2==CONTAINS) && ((strict == false))) {s = 22;}

                        else if ( (LA35_2==IN_FOLDER) && ((strict == false))) {s = 23;}

                        else if ( (LA35_2==IN_TREE) && ((strict == false))) {s = 24;}

                        else if ( (LA35_2==ORDER) && ((strict == false))) {s = 25;}

                        else if ( (LA35_2==BY) && ((strict == false))) {s = 26;}

                        else if ( (LA35_2==ASC) && ((strict == false))) {s = 27;}

                        else if ( (LA35_2==DESC) && ((strict == false))) {s = 28;}

                        else if ( (LA35_2==TIMESTAMP) && ((strict == false))) {s = 29;}

                        else if ( (LA35_2==TRUE) && ((strict == false))) {s = 30;}

                        else if ( (LA35_2==FALSE) && ((strict == false))) {s = 31;}

                        else if ( (LA35_2==SCORE) && ((strict == false))) {s = 32;}

                        else if ( (LA35_2==ID) && ((strict == false))) {s = 33;}

                         
                        input.seek(index35_2);
                        if ( s>=0 ) return s;
                        break;
                    case 8 : 
                        int LA35_8 = input.LA(1);

                         
                        int index35_8 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA35_8==DOUBLE_QUOTE) && ((strict == false))) {s = 37;}

                         
                        input.seek(index35_8);
                        if ( s>=0 ) return s;
                        break;
                    case 9 : 
                        int LA35_28 = input.LA(1);

                         
                        int index35_28 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA35_28==DOUBLE_QUOTE) && ((strict == false))) {s = 57;}

                         
                        input.seek(index35_28);
                        if ( s>=0 ) return s;
                        break;
                    case 10 : 
                        int LA35_17 = input.LA(1);

                         
                        int index35_17 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA35_17==DOUBLE_QUOTE) && ((strict == false))) {s = 46;}

                         
                        input.seek(index35_17);
                        if ( s>=0 ) return s;
                        break;
                    case 11 : 
                        int LA35_6 = input.LA(1);

                         
                        int index35_6 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA35_6==DOUBLE_QUOTE) && ((strict == false))) {s = 35;}

                         
                        input.seek(index35_6);
                        if ( s>=0 ) return s;
                        break;
                    case 12 : 
                        int LA35_29 = input.LA(1);

                         
                        int index35_29 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA35_29==DOUBLE_QUOTE) && ((strict == false))) {s = 58;}

                         
                        input.seek(index35_29);
                        if ( s>=0 ) return s;
                        break;
                    case 13 : 
                        int LA35_19 = input.LA(1);

                         
                        int index35_19 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA35_19==DOUBLE_QUOTE) && ((strict == false))) {s = 48;}

                         
                        input.seek(index35_19);
                        if ( s>=0 ) return s;
                        break;
                    case 14 : 
                        int LA35_18 = input.LA(1);

                         
                        int index35_18 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA35_18==DOUBLE_QUOTE) && ((strict == false))) {s = 47;}

                         
                        input.seek(index35_18);
                        if ( s>=0 ) return s;
                        break;
                    case 15 : 
                        int LA35_7 = input.LA(1);

                         
                        int index35_7 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA35_7==DOUBLE_QUOTE) && ((strict == false))) {s = 36;}

                         
                        input.seek(index35_7);
                        if ( s>=0 ) return s;
                        break;
                    case 16 : 
                        int LA35_15 = input.LA(1);

                         
                        int index35_15 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA35_15==DOUBLE_QUOTE) && ((strict == false))) {s = 44;}

                         
                        input.seek(index35_15);
                        if ( s>=0 ) return s;
                        break;
                    case 17 : 
                        int LA35_25 = input.LA(1);

                         
                        int index35_25 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA35_25==DOUBLE_QUOTE) && ((strict == false))) {s = 54;}

                         
                        input.seek(index35_25);
                        if ( s>=0 ) return s;
                        break;
                    case 18 : 
                        int LA35_5 = input.LA(1);

                         
                        int index35_5 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA35_5==DOUBLE_QUOTE) && ((strict == false))) {s = 34;}

                         
                        input.seek(index35_5);
                        if ( s>=0 ) return s;
                        break;
                    case 19 : 
                        int LA35_33 = input.LA(1);

                         
                        int index35_33 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA35_33==DOUBLE_QUOTE) && ((strict == false))) {s = 62;}

                         
                        input.seek(index35_33);
                        if ( s>=0 ) return s;
                        break;
                    case 20 : 
                        int LA35_1 = input.LA(1);

                         
                        int index35_1 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_CMIS()) ) {s = 3;}

                        else if ( (true) ) {s = 4;}

                         
                        input.seek(index35_1);
                        if ( s>=0 ) return s;
                        break;
                    case 21 : 
                        int LA35_14 = input.LA(1);

                         
                        int index35_14 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA35_14==DOUBLE_QUOTE) && ((strict == false))) {s = 43;}

                         
                        input.seek(index35_14);
                        if ( s>=0 ) return s;
                        break;
                    case 22 : 
                        int LA35_26 = input.LA(1);

                         
                        int index35_26 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA35_26==DOUBLE_QUOTE) && ((strict == false))) {s = 55;}

                         
                        input.seek(index35_26);
                        if ( s>=0 ) return s;
                        break;
                    case 23 : 
                        int LA35_16 = input.LA(1);

                         
                        int index35_16 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA35_16==DOUBLE_QUOTE) && ((strict == false))) {s = 45;}

                         
                        input.seek(index35_16);
                        if ( s>=0 ) return s;
                        break;
                    case 24 : 
                        int LA35_27 = input.LA(1);

                         
                        int index35_27 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA35_27==DOUBLE_QUOTE) && ((strict == false))) {s = 56;}

                         
                        input.seek(index35_27);
                        if ( s>=0 ) return s;
                        break;
                    case 25 : 
                        int LA35_32 = input.LA(1);

                         
                        int index35_32 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA35_32==DOUBLE_QUOTE) && ((strict == false))) {s = 61;}

                         
                        input.seek(index35_32);
                        if ( s>=0 ) return s;
                        break;
                    case 26 : 
                        int LA35_0 = input.LA(1);

                         
                        int index35_0 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA35_0==ID) ) {s = 1;}

                        else if ( (LA35_0==DOUBLE_QUOTE) && ((strict == false))) {s = 2;}

                         
                        input.seek(index35_0);
                        if ( s>=0 ) return s;
                        break;
                    case 27 : 
                        int LA35_12 = input.LA(1);

                         
                        int index35_12 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA35_12==DOUBLE_QUOTE) && ((strict == false))) {s = 41;}

                         
                        input.seek(index35_12);
                        if ( s>=0 ) return s;
                        break;
                    case 28 : 
                        int LA35_22 = input.LA(1);

                         
                        int index35_22 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA35_22==DOUBLE_QUOTE) && ((strict == false))) {s = 51;}

                         
                        input.seek(index35_22);
                        if ( s>=0 ) return s;
                        break;
                    case 29 : 
                        int LA35_62 = input.LA(1);

                         
                        int index35_62 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (((synpred4_CMIS()&&(strict == false))||(synpred4_CMIS()&&(strict == false))||(synpred4_CMIS()&&(strict == false)))) ) {s = 3;}

                        else if ( ((strict == false)) ) {s = 4;}

                         
                        input.seek(index35_62);
                        if ( s>=0 ) return s;
                        break;
                    case 30 : 
                        int LA35_61 = input.LA(1);

                         
                        int index35_61 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (((synpred4_CMIS()&&(strict == false))||(synpred4_CMIS()&&(strict == false))||(synpred4_CMIS()&&(strict == false)))) ) {s = 3;}

                        else if ( ((strict == false)) ) {s = 4;}

                         
                        input.seek(index35_61);
                        if ( s>=0 ) return s;
                        break;
                    case 31 : 
                        int LA35_60 = input.LA(1);

                         
                        int index35_60 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (((synpred4_CMIS()&&(strict == false))||(synpred4_CMIS()&&(strict == false))||(synpred4_CMIS()&&(strict == false)))) ) {s = 3;}

                        else if ( ((strict == false)) ) {s = 4;}

                         
                        input.seek(index35_60);
                        if ( s>=0 ) return s;
                        break;
                    case 32 : 
                        int LA35_59 = input.LA(1);

                         
                        int index35_59 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (((synpred4_CMIS()&&(strict == false))||(synpred4_CMIS()&&(strict == false))||(synpred4_CMIS()&&(strict == false)))) ) {s = 3;}

                        else if ( ((strict == false)) ) {s = 4;}

                         
                        input.seek(index35_59);
                        if ( s>=0 ) return s;
                        break;
                    case 33 : 
                        int LA35_58 = input.LA(1);

                         
                        int index35_58 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (((synpred4_CMIS()&&(strict == false))||(synpred4_CMIS()&&(strict == false))||(synpred4_CMIS()&&(strict == false)))) ) {s = 3;}

                        else if ( ((strict == false)) ) {s = 4;}

                         
                        input.seek(index35_58);
                        if ( s>=0 ) return s;
                        break;
                    case 34 : 
                        int LA35_57 = input.LA(1);

                         
                        int index35_57 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (((synpred4_CMIS()&&(strict == false))||(synpred4_CMIS()&&(strict == false))||(synpred4_CMIS()&&(strict == false)))) ) {s = 3;}

                        else if ( ((strict == false)) ) {s = 4;}

                         
                        input.seek(index35_57);
                        if ( s>=0 ) return s;
                        break;
                    case 35 : 
                        int LA35_56 = input.LA(1);

                         
                        int index35_56 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (((synpred4_CMIS()&&(strict == false))||(synpred4_CMIS()&&(strict == false))||(synpred4_CMIS()&&(strict == false)))) ) {s = 3;}

                        else if ( ((strict == false)) ) {s = 4;}

                         
                        input.seek(index35_56);
                        if ( s>=0 ) return s;
                        break;
                    case 36 : 
                        int LA35_55 = input.LA(1);

                         
                        int index35_55 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (((synpred4_CMIS()&&(strict == false))||(synpred4_CMIS()&&(strict == false))||(synpred4_CMIS()&&(strict == false)))) ) {s = 3;}

                        else if ( ((strict == false)) ) {s = 4;}

                         
                        input.seek(index35_55);
                        if ( s>=0 ) return s;
                        break;
                    case 37 : 
                        int LA35_54 = input.LA(1);

                         
                        int index35_54 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (((synpred4_CMIS()&&(strict == false))||(synpred4_CMIS()&&(strict == false))||(synpred4_CMIS()&&(strict == false)))) ) {s = 3;}

                        else if ( ((strict == false)) ) {s = 4;}

                         
                        input.seek(index35_54);
                        if ( s>=0 ) return s;
                        break;
                    case 38 : 
                        int LA35_53 = input.LA(1);

                         
                        int index35_53 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (((synpred4_CMIS()&&(strict == false))||(synpred4_CMIS()&&(strict == false))||(synpred4_CMIS()&&(strict == false)))) ) {s = 3;}

                        else if ( ((strict == false)) ) {s = 4;}

                         
                        input.seek(index35_53);
                        if ( s>=0 ) return s;
                        break;
                    case 39 : 
                        int LA35_52 = input.LA(1);

                         
                        int index35_52 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (((synpred4_CMIS()&&(strict == false))||(synpred4_CMIS()&&(strict == false))||(synpred4_CMIS()&&(strict == false)))) ) {s = 3;}

                        else if ( ((strict == false)) ) {s = 4;}

                         
                        input.seek(index35_52);
                        if ( s>=0 ) return s;
                        break;
                    case 40 : 
                        int LA35_51 = input.LA(1);

                         
                        int index35_51 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (((synpred4_CMIS()&&(strict == false))||(synpred4_CMIS()&&(strict == false))||(synpred4_CMIS()&&(strict == false)))) ) {s = 3;}

                        else if ( ((strict == false)) ) {s = 4;}

                         
                        input.seek(index35_51);
                        if ( s>=0 ) return s;
                        break;
                    case 41 : 
                        int LA35_50 = input.LA(1);

                         
                        int index35_50 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (((synpred4_CMIS()&&(strict == false))||(synpred4_CMIS()&&(strict == false))||(synpred4_CMIS()&&(strict == false)))) ) {s = 3;}

                        else if ( ((strict == false)) ) {s = 4;}

                         
                        input.seek(index35_50);
                        if ( s>=0 ) return s;
                        break;
                    case 42 : 
                        int LA35_49 = input.LA(1);

                         
                        int index35_49 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (((synpred4_CMIS()&&(strict == false))||(synpred4_CMIS()&&(strict == false))||(synpred4_CMIS()&&(strict == false)))) ) {s = 3;}

                        else if ( ((strict == false)) ) {s = 4;}

                         
                        input.seek(index35_49);
                        if ( s>=0 ) return s;
                        break;
                    case 43 : 
                        int LA35_23 = input.LA(1);

                         
                        int index35_23 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA35_23==DOUBLE_QUOTE) && ((strict == false))) {s = 52;}

                         
                        input.seek(index35_23);
                        if ( s>=0 ) return s;
                        break;
                    case 44 : 
                        int LA35_34 = input.LA(1);

                         
                        int index35_34 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (((synpred4_CMIS()&&(strict == false))||(synpred4_CMIS()&&(strict == false))||(synpred4_CMIS()&&(strict == false)))) ) {s = 3;}

                        else if ( ((strict == false)) ) {s = 4;}

                         
                        input.seek(index35_34);
                        if ( s>=0 ) return s;
                        break;
                    case 45 : 
                        int LA35_35 = input.LA(1);

                         
                        int index35_35 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (((synpred4_CMIS()&&(strict == false))||(synpred4_CMIS()&&(strict == false))||(synpred4_CMIS()&&(strict == false)))) ) {s = 3;}

                        else if ( ((strict == false)) ) {s = 4;}

                         
                        input.seek(index35_35);
                        if ( s>=0 ) return s;
                        break;
                    case 46 : 
                        int LA35_36 = input.LA(1);

                         
                        int index35_36 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (((synpred4_CMIS()&&(strict == false))||(synpred4_CMIS()&&(strict == false))||(synpred4_CMIS()&&(strict == false)))) ) {s = 3;}

                        else if ( ((strict == false)) ) {s = 4;}

                         
                        input.seek(index35_36);
                        if ( s>=0 ) return s;
                        break;
                    case 47 : 
                        int LA35_37 = input.LA(1);

                         
                        int index35_37 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (((synpred4_CMIS()&&(strict == false))||(synpred4_CMIS()&&(strict == false))||(synpred4_CMIS()&&(strict == false)))) ) {s = 3;}

                        else if ( ((strict == false)) ) {s = 4;}

                         
                        input.seek(index35_37);
                        if ( s>=0 ) return s;
                        break;
                    case 48 : 
                        int LA35_38 = input.LA(1);

                         
                        int index35_38 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (((synpred4_CMIS()&&(strict == false))||(synpred4_CMIS()&&(strict == false))||(synpred4_CMIS()&&(strict == false)))) ) {s = 3;}

                        else if ( ((strict == false)) ) {s = 4;}

                         
                        input.seek(index35_38);
                        if ( s>=0 ) return s;
                        break;
                    case 49 : 
                        int LA35_39 = input.LA(1);

                         
                        int index35_39 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (((synpred4_CMIS()&&(strict == false))||(synpred4_CMIS()&&(strict == false))||(synpred4_CMIS()&&(strict == false)))) ) {s = 3;}

                        else if ( ((strict == false)) ) {s = 4;}

                         
                        input.seek(index35_39);
                        if ( s>=0 ) return s;
                        break;
                    case 50 : 
                        int LA35_40 = input.LA(1);

                         
                        int index35_40 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (((synpred4_CMIS()&&(strict == false))||(synpred4_CMIS()&&(strict == false))||(synpred4_CMIS()&&(strict == false)))) ) {s = 3;}

                        else if ( ((strict == false)) ) {s = 4;}

                         
                        input.seek(index35_40);
                        if ( s>=0 ) return s;
                        break;
                    case 51 : 
                        int LA35_41 = input.LA(1);

                         
                        int index35_41 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (((synpred4_CMIS()&&(strict == false))||(synpred4_CMIS()&&(strict == false))||(synpred4_CMIS()&&(strict == false)))) ) {s = 3;}

                        else if ( ((strict == false)) ) {s = 4;}

                         
                        input.seek(index35_41);
                        if ( s>=0 ) return s;
                        break;
                    case 52 : 
                        int LA35_42 = input.LA(1);

                         
                        int index35_42 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (((synpred4_CMIS()&&(strict == false))||(synpred4_CMIS()&&(strict == false))||(synpred4_CMIS()&&(strict == false)))) ) {s = 3;}

                        else if ( ((strict == false)) ) {s = 4;}

                         
                        input.seek(index35_42);
                        if ( s>=0 ) return s;
                        break;
                    case 53 : 
                        int LA35_43 = input.LA(1);

                         
                        int index35_43 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (((synpred4_CMIS()&&(strict == false))||(synpred4_CMIS()&&(strict == false))||(synpred4_CMIS()&&(strict == false)))) ) {s = 3;}

                        else if ( ((strict == false)) ) {s = 4;}

                         
                        input.seek(index35_43);
                        if ( s>=0 ) return s;
                        break;
                    case 54 : 
                        int LA35_44 = input.LA(1);

                         
                        int index35_44 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (((synpred4_CMIS()&&(strict == false))||(synpred4_CMIS()&&(strict == false))||(synpred4_CMIS()&&(strict == false)))) ) {s = 3;}

                        else if ( ((strict == false)) ) {s = 4;}

                         
                        input.seek(index35_44);
                        if ( s>=0 ) return s;
                        break;
                    case 55 : 
                        int LA35_45 = input.LA(1);

                         
                        int index35_45 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (((synpred4_CMIS()&&(strict == false))||(synpred4_CMIS()&&(strict == false))||(synpred4_CMIS()&&(strict == false)))) ) {s = 3;}

                        else if ( ((strict == false)) ) {s = 4;}

                         
                        input.seek(index35_45);
                        if ( s>=0 ) return s;
                        break;
                    case 56 : 
                        int LA35_46 = input.LA(1);

                         
                        int index35_46 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (((synpred4_CMIS()&&(strict == false))||(synpred4_CMIS()&&(strict == false))||(synpred4_CMIS()&&(strict == false)))) ) {s = 3;}

                        else if ( ((strict == false)) ) {s = 4;}

                         
                        input.seek(index35_46);
                        if ( s>=0 ) return s;
                        break;
                    case 57 : 
                        int LA35_47 = input.LA(1);

                         
                        int index35_47 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (((synpred4_CMIS()&&(strict == false))||(synpred4_CMIS()&&(strict == false))||(synpred4_CMIS()&&(strict == false)))) ) {s = 3;}

                        else if ( ((strict == false)) ) {s = 4;}

                         
                        input.seek(index35_47);
                        if ( s>=0 ) return s;
                        break;
                    case 58 : 
                        int LA35_48 = input.LA(1);

                         
                        int index35_48 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (((synpred4_CMIS()&&(strict == false))||(synpred4_CMIS()&&(strict == false))||(synpred4_CMIS()&&(strict == false)))) ) {s = 3;}

                        else if ( ((strict == false)) ) {s = 4;}

                         
                        input.seek(index35_48);
                        if ( s>=0 ) return s;
                        break;
                    case 59 : 
                        int LA35_13 = input.LA(1);

                         
                        int index35_13 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA35_13==DOUBLE_QUOTE) && ((strict == false))) {s = 42;}

                         
                        input.seek(index35_13);
                        if ( s>=0 ) return s;
                        break;
                    case 60 : 
                        int LA35_24 = input.LA(1);

                         
                        int index35_24 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA35_24==DOUBLE_QUOTE) && ((strict == false))) {s = 53;}

                         
                        input.seek(index35_24);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 35, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA43_eotS =
        "\153\uffff";
    static final String DFA43_eofS =
        "\1\uffff\1\4\41\uffff\1\4\1\uffff\1\103\40\uffff\1\111\1\114\42"+
        "\uffff\1\114";
    static final String DFA43_minS =
        "\1\105\1\41\1\37\2\uffff\1\105\35\106\1\41\1\37\1\41\35\106\1\uffff"+
        "\1\105\1\uffff\2\41\1\37\4\uffff\35\106\1\41";
    static final String DFA43_maxS =
        "\1\106\1\102\1\114\2\uffff\36\106\1\102\1\114\1\102\35\106\1\uffff"+
        "\1\106\1\uffff\2\102\1\114\4\uffff\35\106\1\102";
    static final String DFA43_acceptS =
        "\3\uffff\1\2\1\1\76\uffff\1\1\1\uffff\1\2\3\uffff\1\1\2\2\1\1\36"+
        "\uffff";
    static final String DFA43_specialS =
        "\1\74\1\uffff\1\40\3\uffff\1\50\1\43\1\45\1\41\1\37\1\30\1\35\1"+
        "\23\1\25\1\17\1\14\1\20\1\6\1\12\1\10\1\0\1\1\1\67\1\71\1\77\1\47"+
        "\1\57\1\61\1\42\1\44\1\27\1\34\1\63\1\53\2\uffff\1\51\36\uffff\1"+
        "\22\2\uffff\1\21\1\15\4\uffff\1\7\1\11\1\13\1\2\1\3\1\4\1\5\1\55"+
        "\1\54\1\52\1\46\1\64\1\62\1\60\1\56\1\72\1\70\1\66\1\65\1\100\1"+
        "\76\1\75\1\73\1\32\1\33\1\26\1\31\1\24\1\36\1\16}>";
    static final String[] DFA43_transitionS = {
            "\1\1\1\2",
            "\1\4\2\uffff\1\5\34\uffff\2\3",
            "\1\6\2\uffff\1\7\4\uffff\1\10\1\11\1\12\1\13\1\14\1\15\1\uffff"+
            "\1\16\1\17\1\20\1\21\5\uffff\1\22\1\23\1\24\1\25\1\26\1\27\1"+
            "\30\1\31\1\32\1\33\1\34\1\35\2\uffff\1\42\3\uffff\1\37\1\40"+
            "\1\36\1\41",
            "",
            "",
            "\1\43\1\44",
            "\1\45",
            "\1\45",
            "\1\45",
            "\1\45",
            "\1\45",
            "\1\45",
            "\1\45",
            "\1\45",
            "\1\45",
            "\1\45",
            "\1\45",
            "\1\45",
            "\1\45",
            "\1\45",
            "\1\45",
            "\1\45",
            "\1\45",
            "\1\45",
            "\1\45",
            "\1\45",
            "\1\45",
            "\1\45",
            "\1\45",
            "\1\45",
            "\1\45",
            "\1\45",
            "\1\45",
            "\1\45",
            "\1\45",
            "\1\4\37\uffff\2\3",
            "\1\46\2\uffff\1\47\4\uffff\1\50\1\51\1\52\1\53\1\54\1\55\1"+
            "\uffff\1\56\1\57\1\60\1\61\5\uffff\1\62\1\63\1\64\1\65\1\66"+
            "\1\67\1\70\1\71\1\72\1\73\1\74\1\75\2\uffff\1\102\3\uffff\1"+
            "\77\1\100\1\76\1\101",
            "\1\103\2\uffff\1\104\34\uffff\2\105",
            "\1\106",
            "\1\106",
            "\1\106",
            "\1\106",
            "\1\106",
            "\1\106",
            "\1\106",
            "\1\106",
            "\1\106",
            "\1\106",
            "\1\106",
            "\1\106",
            "\1\106",
            "\1\106",
            "\1\106",
            "\1\106",
            "\1\106",
            "\1\106",
            "\1\106",
            "\1\106",
            "\1\106",
            "\1\106",
            "\1\106",
            "\1\106",
            "\1\106",
            "\1\106",
            "\1\106",
            "\1\106",
            "\1\106",
            "",
            "\1\107\1\110",
            "",
            "\1\4\37\uffff\1\3\1\112",
            "\1\103\37\uffff\1\105\1\113",
            "\1\115\2\uffff\1\116\4\uffff\1\117\1\120\1\121\1\122\1\123"+
            "\1\124\1\uffff\1\125\1\126\1\127\1\130\5\uffff\1\131\1\132\1"+
            "\133\1\134\1\135\1\136\1\137\1\140\1\141\1\142\1\143\1\144\2"+
            "\uffff\1\151\3\uffff\1\146\1\147\1\145\1\150",
            "",
            "",
            "",
            "",
            "\1\152",
            "\1\152",
            "\1\152",
            "\1\152",
            "\1\152",
            "\1\152",
            "\1\152",
            "\1\152",
            "\1\152",
            "\1\152",
            "\1\152",
            "\1\152",
            "\1\152",
            "\1\152",
            "\1\152",
            "\1\152",
            "\1\152",
            "\1\152",
            "\1\152",
            "\1\152",
            "\1\152",
            "\1\152",
            "\1\152",
            "\1\152",
            "\1\152",
            "\1\152",
            "\1\152",
            "\1\152",
            "\1\152",
            "\1\103\37\uffff\1\105\1\113"
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
            return "557:1: sortSpecification : ( columnReference -> ^( SORT_SPECIFICATION columnReference ASC ) | columnReference (by= ASC | by= DESC ) -> ^( SORT_SPECIFICATION columnReference $by) );";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA43_21 = input.LA(1);

                         
                        int index43_21 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA43_21==DOUBLE_QUOTE) && ((strict == false))) {s = 37;}

                         
                        input.seek(index43_21);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA43_22 = input.LA(1);

                         
                        int index43_22 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA43_22==DOUBLE_QUOTE) && ((strict == false))) {s = 37;}

                         
                        input.seek(index43_22);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA43_80 = input.LA(1);

                         
                        int index43_80 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA43_80==DOUBLE_QUOTE) && ((strict == false))) {s = 106;}

                         
                        input.seek(index43_80);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA43_81 = input.LA(1);

                         
                        int index43_81 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA43_81==DOUBLE_QUOTE) && ((strict == false))) {s = 106;}

                         
                        input.seek(index43_81);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA43_82 = input.LA(1);

                         
                        int index43_82 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA43_82==DOUBLE_QUOTE) && ((strict == false))) {s = 106;}

                         
                        input.seek(index43_82);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA43_83 = input.LA(1);

                         
                        int index43_83 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA43_83==DOUBLE_QUOTE) && ((strict == false))) {s = 106;}

                         
                        input.seek(index43_83);
                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA43_18 = input.LA(1);

                         
                        int index43_18 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA43_18==DOUBLE_QUOTE) && ((strict == false))) {s = 37;}

                         
                        input.seek(index43_18);
                        if ( s>=0 ) return s;
                        break;
                    case 7 : 
                        int LA43_77 = input.LA(1);

                         
                        int index43_77 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA43_77==DOUBLE_QUOTE) && ((strict == false))) {s = 106;}

                         
                        input.seek(index43_77);
                        if ( s>=0 ) return s;
                        break;
                    case 8 : 
                        int LA43_20 = input.LA(1);

                         
                        int index43_20 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA43_20==DOUBLE_QUOTE) && ((strict == false))) {s = 37;}

                         
                        input.seek(index43_20);
                        if ( s>=0 ) return s;
                        break;
                    case 9 : 
                        int LA43_78 = input.LA(1);

                         
                        int index43_78 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA43_78==DOUBLE_QUOTE) && ((strict == false))) {s = 106;}

                         
                        input.seek(index43_78);
                        if ( s>=0 ) return s;
                        break;
                    case 10 : 
                        int LA43_19 = input.LA(1);

                         
                        int index43_19 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA43_19==DOUBLE_QUOTE) && ((strict == false))) {s = 37;}

                         
                        input.seek(index43_19);
                        if ( s>=0 ) return s;
                        break;
                    case 11 : 
                        int LA43_79 = input.LA(1);

                         
                        int index43_79 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA43_79==DOUBLE_QUOTE) && ((strict == false))) {s = 106;}

                         
                        input.seek(index43_79);
                        if ( s>=0 ) return s;
                        break;
                    case 12 : 
                        int LA43_16 = input.LA(1);

                         
                        int index43_16 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA43_16==DOUBLE_QUOTE) && ((strict == false))) {s = 37;}

                         
                        input.seek(index43_16);
                        if ( s>=0 ) return s;
                        break;
                    case 13 : 
                        int LA43_72 = input.LA(1);

                         
                        int index43_72 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA43_72==SELECT) && ((strict == false))) {s = 77;}

                        else if ( (LA43_72==AS) && ((strict == false))) {s = 78;}

                        else if ( (LA43_72==FROM) && ((strict == false))) {s = 79;}

                        else if ( (LA43_72==JOIN) && ((strict == false))) {s = 80;}

                        else if ( (LA43_72==INNER) && ((strict == false))) {s = 81;}

                        else if ( (LA43_72==LEFT) && ((strict == false))) {s = 82;}

                        else if ( (LA43_72==OUTER) && ((strict == false))) {s = 83;}

                        else if ( (LA43_72==ON) && ((strict == false))) {s = 84;}

                        else if ( (LA43_72==WHERE) && ((strict == false))) {s = 85;}

                        else if ( (LA43_72==OR) && ((strict == false))) {s = 86;}

                        else if ( (LA43_72==AND) && ((strict == false))) {s = 87;}

                        else if ( (LA43_72==NOT) && ((strict == false))) {s = 88;}

                        else if ( (LA43_72==IN) && ((strict == false))) {s = 89;}

                        else if ( (LA43_72==LIKE) && ((strict == false))) {s = 90;}

                        else if ( (LA43_72==IS) && ((strict == false))) {s = 91;}

                        else if ( (LA43_72==NULL) && ((strict == false))) {s = 92;}

                        else if ( (LA43_72==ANY) && ((strict == false))) {s = 93;}

                        else if ( (LA43_72==CONTAINS) && ((strict == false))) {s = 94;}

                        else if ( (LA43_72==IN_FOLDER) && ((strict == false))) {s = 95;}

                        else if ( (LA43_72==IN_TREE) && ((strict == false))) {s = 96;}

                        else if ( (LA43_72==ORDER) && ((strict == false))) {s = 97;}

                        else if ( (LA43_72==BY) && ((strict == false))) {s = 98;}

                        else if ( (LA43_72==ASC) && ((strict == false))) {s = 99;}

                        else if ( (LA43_72==DESC) && ((strict == false))) {s = 100;}

                        else if ( (LA43_72==TIMESTAMP) && ((strict == false))) {s = 101;}

                        else if ( (LA43_72==TRUE) && ((strict == false))) {s = 102;}

                        else if ( (LA43_72==FALSE) && ((strict == false))) {s = 103;}

                        else if ( (LA43_72==SCORE) && ((strict == false))) {s = 104;}

                        else if ( (LA43_72==ID) && ((strict == false))) {s = 105;}

                         
                        input.seek(index43_72);
                        if ( s>=0 ) return s;
                        break;
                    case 14 : 
                        int LA43_106 = input.LA(1);

                         
                        int index43_106 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA43_106==COMMA) && ((strict == false))) {s = 67;}

                        else if ( (LA43_106==EOF) && ((strict == false))) {s = 76;}

                        else if ( (LA43_106==ASC) && ((strict == false))) {s = 69;}

                        else if ( (LA43_106==DESC) && ((strict == false))) {s = 75;}

                         
                        input.seek(index43_106);
                        if ( s>=0 ) return s;
                        break;
                    case 15 : 
                        int LA43_15 = input.LA(1);

                         
                        int index43_15 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA43_15==DOUBLE_QUOTE) && ((strict == false))) {s = 37;}

                         
                        input.seek(index43_15);
                        if ( s>=0 ) return s;
                        break;
                    case 16 : 
                        int LA43_17 = input.LA(1);

                         
                        int index43_17 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA43_17==DOUBLE_QUOTE) && ((strict == false))) {s = 37;}

                         
                        input.seek(index43_17);
                        if ( s>=0 ) return s;
                        break;
                    case 17 : 
                        int LA43_71 = input.LA(1);

                         
                        int index43_71 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA43_71==ASC) && ((strict == false))) {s = 69;}

                        else if ( (LA43_71==DESC) && ((strict == false))) {s = 75;}

                        else if ( (LA43_71==COMMA) && ((strict == false))) {s = 67;}

                        else if ( (LA43_71==EOF) && ((strict == false))) {s = 76;}

                         
                        input.seek(index43_71);
                        if ( s>=0 ) return s;
                        break;
                    case 18 : 
                        int LA43_68 = input.LA(1);

                         
                        int index43_68 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA43_68==ID) && ((strict == false))) {s = 71;}

                        else if ( (LA43_68==DOUBLE_QUOTE) && ((strict == false))) {s = 72;}

                         
                        input.seek(index43_68);
                        if ( s>=0 ) return s;
                        break;
                    case 19 : 
                        int LA43_13 = input.LA(1);

                         
                        int index43_13 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA43_13==DOUBLE_QUOTE) && ((strict == false))) {s = 37;}

                         
                        input.seek(index43_13);
                        if ( s>=0 ) return s;
                        break;
                    case 20 : 
                        int LA43_104 = input.LA(1);

                         
                        int index43_104 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA43_104==DOUBLE_QUOTE) && ((strict == false))) {s = 106;}

                         
                        input.seek(index43_104);
                        if ( s>=0 ) return s;
                        break;
                    case 21 : 
                        int LA43_14 = input.LA(1);

                         
                        int index43_14 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA43_14==DOUBLE_QUOTE) && ((strict == false))) {s = 37;}

                         
                        input.seek(index43_14);
                        if ( s>=0 ) return s;
                        break;
                    case 22 : 
                        int LA43_102 = input.LA(1);

                         
                        int index43_102 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA43_102==DOUBLE_QUOTE) && ((strict == false))) {s = 106;}

                         
                        input.seek(index43_102);
                        if ( s>=0 ) return s;
                        break;
                    case 23 : 
                        int LA43_31 = input.LA(1);

                         
                        int index43_31 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA43_31==DOUBLE_QUOTE) && ((strict == false))) {s = 37;}

                         
                        input.seek(index43_31);
                        if ( s>=0 ) return s;
                        break;
                    case 24 : 
                        int LA43_11 = input.LA(1);

                         
                        int index43_11 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA43_11==DOUBLE_QUOTE) && ((strict == false))) {s = 37;}

                         
                        input.seek(index43_11);
                        if ( s>=0 ) return s;
                        break;
                    case 25 : 
                        int LA43_103 = input.LA(1);

                         
                        int index43_103 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA43_103==DOUBLE_QUOTE) && ((strict == false))) {s = 106;}

                         
                        input.seek(index43_103);
                        if ( s>=0 ) return s;
                        break;
                    case 26 : 
                        int LA43_100 = input.LA(1);

                         
                        int index43_100 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA43_100==DOUBLE_QUOTE) && ((strict == false))) {s = 106;}

                         
                        input.seek(index43_100);
                        if ( s>=0 ) return s;
                        break;
                    case 27 : 
                        int LA43_101 = input.LA(1);

                         
                        int index43_101 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA43_101==DOUBLE_QUOTE) && ((strict == false))) {s = 106;}

                         
                        input.seek(index43_101);
                        if ( s>=0 ) return s;
                        break;
                    case 28 : 
                        int LA43_32 = input.LA(1);

                         
                        int index43_32 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA43_32==DOUBLE_QUOTE) && ((strict == false))) {s = 37;}

                         
                        input.seek(index43_32);
                        if ( s>=0 ) return s;
                        break;
                    case 29 : 
                        int LA43_12 = input.LA(1);

                         
                        int index43_12 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA43_12==DOUBLE_QUOTE) && ((strict == false))) {s = 37;}

                         
                        input.seek(index43_12);
                        if ( s>=0 ) return s;
                        break;
                    case 30 : 
                        int LA43_105 = input.LA(1);

                         
                        int index43_105 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA43_105==DOUBLE_QUOTE) && ((strict == false))) {s = 106;}

                         
                        input.seek(index43_105);
                        if ( s>=0 ) return s;
                        break;
                    case 31 : 
                        int LA43_10 = input.LA(1);

                         
                        int index43_10 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA43_10==DOUBLE_QUOTE) && ((strict == false))) {s = 37;}

                         
                        input.seek(index43_10);
                        if ( s>=0 ) return s;
                        break;
                    case 32 : 
                        int LA43_2 = input.LA(1);

                         
                        int index43_2 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA43_2==SELECT) && ((strict == false))) {s = 6;}

                        else if ( (LA43_2==AS) && ((strict == false))) {s = 7;}

                        else if ( (LA43_2==FROM) && ((strict == false))) {s = 8;}

                        else if ( (LA43_2==JOIN) && ((strict == false))) {s = 9;}

                        else if ( (LA43_2==INNER) && ((strict == false))) {s = 10;}

                        else if ( (LA43_2==LEFT) && ((strict == false))) {s = 11;}

                        else if ( (LA43_2==OUTER) && ((strict == false))) {s = 12;}

                        else if ( (LA43_2==ON) && ((strict == false))) {s = 13;}

                        else if ( (LA43_2==WHERE) && ((strict == false))) {s = 14;}

                        else if ( (LA43_2==OR) && ((strict == false))) {s = 15;}

                        else if ( (LA43_2==AND) && ((strict == false))) {s = 16;}

                        else if ( (LA43_2==NOT) && ((strict == false))) {s = 17;}

                        else if ( (LA43_2==IN) && ((strict == false))) {s = 18;}

                        else if ( (LA43_2==LIKE) && ((strict == false))) {s = 19;}

                        else if ( (LA43_2==IS) && ((strict == false))) {s = 20;}

                        else if ( (LA43_2==NULL) && ((strict == false))) {s = 21;}

                        else if ( (LA43_2==ANY) && ((strict == false))) {s = 22;}

                        else if ( (LA43_2==CONTAINS) && ((strict == false))) {s = 23;}

                        else if ( (LA43_2==IN_FOLDER) && ((strict == false))) {s = 24;}

                        else if ( (LA43_2==IN_TREE) && ((strict == false))) {s = 25;}

                        else if ( (LA43_2==ORDER) && ((strict == false))) {s = 26;}

                        else if ( (LA43_2==BY) && ((strict == false))) {s = 27;}

                        else if ( (LA43_2==ASC) && ((strict == false))) {s = 28;}

                        else if ( (LA43_2==DESC) && ((strict == false))) {s = 29;}

                        else if ( (LA43_2==TIMESTAMP) && ((strict == false))) {s = 30;}

                        else if ( (LA43_2==TRUE) && ((strict == false))) {s = 31;}

                        else if ( (LA43_2==FALSE) && ((strict == false))) {s = 32;}

                        else if ( (LA43_2==SCORE) && ((strict == false))) {s = 33;}

                        else if ( (LA43_2==ID) && ((strict == false))) {s = 34;}

                         
                        input.seek(index43_2);
                        if ( s>=0 ) return s;
                        break;
                    case 33 : 
                        int LA43_9 = input.LA(1);

                         
                        int index43_9 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA43_9==DOUBLE_QUOTE) && ((strict == false))) {s = 37;}

                         
                        input.seek(index43_9);
                        if ( s>=0 ) return s;
                        break;
                    case 34 : 
                        int LA43_29 = input.LA(1);

                         
                        int index43_29 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA43_29==DOUBLE_QUOTE) && ((strict == false))) {s = 37;}

                         
                        input.seek(index43_29);
                        if ( s>=0 ) return s;
                        break;
                    case 35 : 
                        int LA43_7 = input.LA(1);

                         
                        int index43_7 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA43_7==DOUBLE_QUOTE) && ((strict == false))) {s = 37;}

                         
                        input.seek(index43_7);
                        if ( s>=0 ) return s;
                        break;
                    case 36 : 
                        int LA43_30 = input.LA(1);

                         
                        int index43_30 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA43_30==DOUBLE_QUOTE) && ((strict == false))) {s = 37;}

                         
                        input.seek(index43_30);
                        if ( s>=0 ) return s;
                        break;
                    case 37 : 
                        int LA43_8 = input.LA(1);

                         
                        int index43_8 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA43_8==DOUBLE_QUOTE) && ((strict == false))) {s = 37;}

                         
                        input.seek(index43_8);
                        if ( s>=0 ) return s;
                        break;
                    case 38 : 
                        int LA43_87 = input.LA(1);

                         
                        int index43_87 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA43_87==DOUBLE_QUOTE) && ((strict == false))) {s = 106;}

                         
                        input.seek(index43_87);
                        if ( s>=0 ) return s;
                        break;
                    case 39 : 
                        int LA43_26 = input.LA(1);

                         
                        int index43_26 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA43_26==DOUBLE_QUOTE) && ((strict == false))) {s = 37;}

                         
                        input.seek(index43_26);
                        if ( s>=0 ) return s;
                        break;
                    case 40 : 
                        int LA43_6 = input.LA(1);

                         
                        int index43_6 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA43_6==DOUBLE_QUOTE) && ((strict == false))) {s = 37;}

                         
                        input.seek(index43_6);
                        if ( s>=0 ) return s;
                        break;
                    case 41 : 
                        int LA43_37 = input.LA(1);

                         
                        int index43_37 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA43_37==EOF||LA43_37==COMMA) && ((strict == false))) {s = 67;}

                        else if ( (LA43_37==DOT) && ((strict == false))) {s = 68;}

                        else if ( ((LA43_37>=ASC && LA43_37<=DESC)) && ((strict == false))) {s = 69;}

                         
                        input.seek(index43_37);
                        if ( s>=0 ) return s;
                        break;
                    case 42 : 
                        int LA43_86 = input.LA(1);

                         
                        int index43_86 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA43_86==DOUBLE_QUOTE) && ((strict == false))) {s = 106;}

                         
                        input.seek(index43_86);
                        if ( s>=0 ) return s;
                        break;
                    case 43 : 
                        int LA43_34 = input.LA(1);

                         
                        int index43_34 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA43_34==DOUBLE_QUOTE) && ((strict == false))) {s = 37;}

                         
                        input.seek(index43_34);
                        if ( s>=0 ) return s;
                        break;
                    case 44 : 
                        int LA43_85 = input.LA(1);

                         
                        int index43_85 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA43_85==DOUBLE_QUOTE) && ((strict == false))) {s = 106;}

                         
                        input.seek(index43_85);
                        if ( s>=0 ) return s;
                        break;
                    case 45 : 
                        int LA43_84 = input.LA(1);

                         
                        int index43_84 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA43_84==DOUBLE_QUOTE) && ((strict == false))) {s = 106;}

                         
                        input.seek(index43_84);
                        if ( s>=0 ) return s;
                        break;
                    case 46 : 
                        int LA43_91 = input.LA(1);

                         
                        int index43_91 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA43_91==DOUBLE_QUOTE) && ((strict == false))) {s = 106;}

                         
                        input.seek(index43_91);
                        if ( s>=0 ) return s;
                        break;
                    case 47 : 
                        int LA43_27 = input.LA(1);

                         
                        int index43_27 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA43_27==DOUBLE_QUOTE) && ((strict == false))) {s = 37;}

                         
                        input.seek(index43_27);
                        if ( s>=0 ) return s;
                        break;
                    case 48 : 
                        int LA43_90 = input.LA(1);

                         
                        int index43_90 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA43_90==DOUBLE_QUOTE) && ((strict == false))) {s = 106;}

                         
                        input.seek(index43_90);
                        if ( s>=0 ) return s;
                        break;
                    case 49 : 
                        int LA43_28 = input.LA(1);

                         
                        int index43_28 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA43_28==DOUBLE_QUOTE) && ((strict == false))) {s = 37;}

                         
                        input.seek(index43_28);
                        if ( s>=0 ) return s;
                        break;
                    case 50 : 
                        int LA43_89 = input.LA(1);

                         
                        int index43_89 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA43_89==DOUBLE_QUOTE) && ((strict == false))) {s = 106;}

                         
                        input.seek(index43_89);
                        if ( s>=0 ) return s;
                        break;
                    case 51 : 
                        int LA43_33 = input.LA(1);

                         
                        int index43_33 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA43_33==DOUBLE_QUOTE) && ((strict == false))) {s = 37;}

                         
                        input.seek(index43_33);
                        if ( s>=0 ) return s;
                        break;
                    case 52 : 
                        int LA43_88 = input.LA(1);

                         
                        int index43_88 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA43_88==DOUBLE_QUOTE) && ((strict == false))) {s = 106;}

                         
                        input.seek(index43_88);
                        if ( s>=0 ) return s;
                        break;
                    case 53 : 
                        int LA43_95 = input.LA(1);

                         
                        int index43_95 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA43_95==DOUBLE_QUOTE) && ((strict == false))) {s = 106;}

                         
                        input.seek(index43_95);
                        if ( s>=0 ) return s;
                        break;
                    case 54 : 
                        int LA43_94 = input.LA(1);

                         
                        int index43_94 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA43_94==DOUBLE_QUOTE) && ((strict == false))) {s = 106;}

                         
                        input.seek(index43_94);
                        if ( s>=0 ) return s;
                        break;
                    case 55 : 
                        int LA43_23 = input.LA(1);

                         
                        int index43_23 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA43_23==DOUBLE_QUOTE) && ((strict == false))) {s = 37;}

                         
                        input.seek(index43_23);
                        if ( s>=0 ) return s;
                        break;
                    case 56 : 
                        int LA43_93 = input.LA(1);

                         
                        int index43_93 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA43_93==DOUBLE_QUOTE) && ((strict == false))) {s = 106;}

                         
                        input.seek(index43_93);
                        if ( s>=0 ) return s;
                        break;
                    case 57 : 
                        int LA43_24 = input.LA(1);

                         
                        int index43_24 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA43_24==DOUBLE_QUOTE) && ((strict == false))) {s = 37;}

                         
                        input.seek(index43_24);
                        if ( s>=0 ) return s;
                        break;
                    case 58 : 
                        int LA43_92 = input.LA(1);

                         
                        int index43_92 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA43_92==DOUBLE_QUOTE) && ((strict == false))) {s = 106;}

                         
                        input.seek(index43_92);
                        if ( s>=0 ) return s;
                        break;
                    case 59 : 
                        int LA43_99 = input.LA(1);

                         
                        int index43_99 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA43_99==DOUBLE_QUOTE) && ((strict == false))) {s = 106;}

                         
                        input.seek(index43_99);
                        if ( s>=0 ) return s;
                        break;
                    case 60 : 
                        int LA43_0 = input.LA(1);

                         
                        int index43_0 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA43_0==ID) ) {s = 1;}

                        else if ( (LA43_0==DOUBLE_QUOTE) && ((strict == false))) {s = 2;}

                         
                        input.seek(index43_0);
                        if ( s>=0 ) return s;
                        break;
                    case 61 : 
                        int LA43_98 = input.LA(1);

                         
                        int index43_98 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA43_98==DOUBLE_QUOTE) && ((strict == false))) {s = 106;}

                         
                        input.seek(index43_98);
                        if ( s>=0 ) return s;
                        break;
                    case 62 : 
                        int LA43_97 = input.LA(1);

                         
                        int index43_97 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA43_97==DOUBLE_QUOTE) && ((strict == false))) {s = 106;}

                         
                        input.seek(index43_97);
                        if ( s>=0 ) return s;
                        break;
                    case 63 : 
                        int LA43_25 = input.LA(1);

                         
                        int index43_25 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA43_25==DOUBLE_QUOTE) && ((strict == false))) {s = 37;}

                         
                        input.seek(index43_25);
                        if ( s>=0 ) return s;
                        break;
                    case 64 : 
                        int LA43_96 = input.LA(1);

                         
                        int index43_96 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA43_96==DOUBLE_QUOTE) && ((strict == false))) {s = 106;}

                         
                        input.seek(index43_96);
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
 

    public static final BitSet FOLLOW_SELECT_in_query217 = new BitSet(new long[]{0xFF83DF8580000000L,0x0000000000001E67L});
    public static final BitSet FOLLOW_selectList_in_query219 = new BitSet(new long[]{0x0000008000000000L});
    public static final BitSet FOLLOW_fromClause_in_query221 = new BitSet(new long[]{0x8000400000000000L});
    public static final BitSet FOLLOW_whereClause_in_query223 = new BitSet(new long[]{0x8000000000000000L});
    public static final BitSet FOLLOW_orderByClause_in_query226 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_query229 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STAR_in_selectList286 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_selectSubList_in_selectList302 = new BitSet(new long[]{0x0000000200000002L});
    public static final BitSet FOLLOW_COMMA_in_selectList306 = new BitSet(new long[]{0xFF83DF8580000000L,0x0000000000001E67L});
    public static final BitSet FOLLOW_selectSubList_in_selectList308 = new BitSet(new long[]{0x0000000200000002L});
    public static final BitSet FOLLOW_valueExpression_in_selectSubList344 = new BitSet(new long[]{0x0000000400000002L,0x0000000000000060L});
    public static final BitSet FOLLOW_AS_in_selectSubList348 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000060L});
    public static final BitSet FOLLOW_columnName_in_selectSubList351 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_qualifier_in_selectSubList372 = new BitSet(new long[]{0x0000000800000000L});
    public static final BitSet FOLLOW_DOTSTAR_in_selectSubList374 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_multiValuedColumnReference_in_selectSubList390 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_columnReference_in_valueExpression435 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_valueFunction_in_valueExpression448 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_qualifier_in_columnReference497 = new BitSet(new long[]{0x0000001000000000L});
    public static final BitSet FOLLOW_DOT_in_columnReference499 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000060L});
    public static final BitSet FOLLOW_columnName_in_columnReference504 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_qualifier_in_multiValuedColumnReference566 = new BitSet(new long[]{0x0000001000000000L});
    public static final BitSet FOLLOW_DOT_in_multiValuedColumnReference568 = new BitSet(new long[]{0xFF83DF8580000000L,0x0000000000001E67L});
    public static final BitSet FOLLOW_multiValuedColumnName_in_multiValuedColumnReference574 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cmisFunction_in_valueFunction640 = new BitSet(new long[]{0x0000002000000000L});
    public static final BitSet FOLLOW_LPAREN_in_valueFunction642 = new BitSet(new long[]{0x0000004000000000L,0x0000000000000FF8L});
    public static final BitSet FOLLOW_functionArgument_in_valueFunction644 = new BitSet(new long[]{0x0000004000000000L,0x0000000000000FF8L});
    public static final BitSet FOLLOW_RPAREN_in_valueFunction647 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_keyWordOrId_in_valueFunction675 = new BitSet(new long[]{0x0000002000000000L});
    public static final BitSet FOLLOW_LPAREN_in_valueFunction677 = new BitSet(new long[]{0x0000004000000000L,0x0000000000000FF8L});
    public static final BitSet FOLLOW_functionArgument_in_valueFunction679 = new BitSet(new long[]{0x0000004000000000L,0x0000000000000FF8L});
    public static final BitSet FOLLOW_RPAREN_in_valueFunction682 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_qualifier_in_functionArgument717 = new BitSet(new long[]{0x0000001000000000L});
    public static final BitSet FOLLOW_DOT_in_functionArgument719 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000060L});
    public static final BitSet FOLLOW_columnName_in_functionArgument721 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_identifier_in_functionArgument745 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_literalOrParameterName_in_functionArgument755 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_tableName_in_qualifier776 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_correlationName_in_qualifier788 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FROM_in_fromClause833 = new BitSet(new long[]{0x0000002000000000L,0x0000000000000060L});
    public static final BitSet FOLLOW_tableReference_in_fromClause835 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_singleTable_in_tableReference856 = new BitSet(new long[]{0x0000070000000002L});
    public static final BitSet FOLLOW_joinedTable_in_tableReference858 = new BitSet(new long[]{0x0000070000000002L});
    public static final BitSet FOLLOW_tableName_in_singleTable886 = new BitSet(new long[]{0x0000000400000002L,0x0000000000000060L});
    public static final BitSet FOLLOW_AS_in_singleTable890 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000060L});
    public static final BitSet FOLLOW_correlationName_in_singleTable893 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_singleTable914 = new BitSet(new long[]{0x0000002000000000L,0x0000000000000060L});
    public static final BitSet FOLLOW_joinedTables_in_singleTable916 = new BitSet(new long[]{0x0000004000000000L});
    public static final BitSet FOLLOW_RPAREN_in_singleTable918 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_joinType_in_joinedTable966 = new BitSet(new long[]{0x0000010000000000L});
    public static final BitSet FOLLOW_JOIN_in_joinedTable969 = new BitSet(new long[]{0x0000002000000000L,0x0000000000000060L});
    public static final BitSet FOLLOW_tableReference_in_joinedTable971 = new BitSet(new long[]{0x0000100000000000L});
    public static final BitSet FOLLOW_joinSpecification_in_joinedTable973 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_singleTable_in_joinedTables1001 = new BitSet(new long[]{0x0000070000000000L});
    public static final BitSet FOLLOW_joinedTable_in_joinedTables1003 = new BitSet(new long[]{0x0000070000000002L});
    public static final BitSet FOLLOW_INNER_in_joinType1030 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LEFT_in_joinType1042 = new BitSet(new long[]{0x0000080000000002L});
    public static final BitSet FOLLOW_OUTER_in_joinType1044 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ON_in_joinSpecification1090 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000060L});
    public static final BitSet FOLLOW_columnReference_in_joinSpecification1094 = new BitSet(new long[]{0x0000200000000000L});
    public static final BitSet FOLLOW_EQUALS_in_joinSpecification1096 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000060L});
    public static final BitSet FOLLOW_columnReference_in_joinSpecification1100 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_WHERE_in_whereClause1159 = new BitSet(new long[]{0xFF83DFA580000000L,0x0000000000001FFFL});
    public static final BitSet FOLLOW_searchOrCondition_in_whereClause1161 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_searchAndCondition_in_searchOrCondition1181 = new BitSet(new long[]{0x0000800000000002L});
    public static final BitSet FOLLOW_OR_in_searchOrCondition1184 = new BitSet(new long[]{0xFF83DFA580000000L,0x0000000000001FFFL});
    public static final BitSet FOLLOW_searchAndCondition_in_searchOrCondition1186 = new BitSet(new long[]{0x0000800000000002L});
    public static final BitSet FOLLOW_searchNotCondition_in_searchAndCondition1214 = new BitSet(new long[]{0x0001000000000002L});
    public static final BitSet FOLLOW_AND_in_searchAndCondition1217 = new BitSet(new long[]{0xFF83DFA580000000L,0x0000000000001FFFL});
    public static final BitSet FOLLOW_searchNotCondition_in_searchAndCondition1219 = new BitSet(new long[]{0x0001000000000002L});
    public static final BitSet FOLLOW_NOT_in_searchNotCondition1246 = new BitSet(new long[]{0xFF83DFA580000000L,0x0000000000001FFFL});
    public static final BitSet FOLLOW_searchTest_in_searchNotCondition1248 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_searchTest_in_searchNotCondition1263 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_predicate_in_searchTest1281 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_searchTest1292 = new BitSet(new long[]{0xFF83DFA580000000L,0x0000000000001FFFL});
    public static final BitSet FOLLOW_searchOrCondition_in_searchTest1294 = new BitSet(new long[]{0x0000004000000000L});
    public static final BitSet FOLLOW_RPAREN_in_searchTest1296 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_comparisonPredicate_in_predicate1313 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_inPredicate_in_predicate1318 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_likePredicate_in_predicate1323 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_nullPredicate_in_predicate1328 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_quantifiedComparisonPredicate_in_predicate1339 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_quantifiedInPredicate_in_predicate1344 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_textSearchPredicate_in_predicate1349 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_folderPredicate_in_predicate1354 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_valueExpression_in_comparisonPredicate1366 = new BitSet(new long[]{0x007C200000000000L});
    public static final BitSet FOLLOW_compOp_in_comparisonPredicate1368 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000FF8L});
    public static final BitSet FOLLOW_literalOrParameterName_in_comparisonPredicate1370 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_compOp0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_literal_in_literalOrParameterName1436 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_parameterName_in_literalOrParameterName1444 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_signedNumericLiteral_in_literal1457 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_characterStringLiteral_in_literal1462 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_booleanLiteral_in_literal1467 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_datetimeLiteral_in_literal1472 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_columnReference_in_inPredicate1484 = new BitSet(new long[]{0x0082000000000000L});
    public static final BitSet FOLLOW_NOT_in_inPredicate1486 = new BitSet(new long[]{0x0080000000000000L});
    public static final BitSet FOLLOW_IN_in_inPredicate1489 = new BitSet(new long[]{0x0000002000000000L});
    public static final BitSet FOLLOW_LPAREN_in_inPredicate1491 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000FF8L});
    public static final BitSet FOLLOW_inValueList_in_inPredicate1493 = new BitSet(new long[]{0x0000004000000000L});
    public static final BitSet FOLLOW_RPAREN_in_inPredicate1495 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_literalOrParameterName_in_inValueList1524 = new BitSet(new long[]{0x0000000200000002L});
    public static final BitSet FOLLOW_COMMA_in_inValueList1527 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000FF8L});
    public static final BitSet FOLLOW_literalOrParameterName_in_inValueList1529 = new BitSet(new long[]{0x0000000200000002L});
    public static final BitSet FOLLOW_columnReference_in_likePredicate1555 = new BitSet(new long[]{0x0102000000000000L});
    public static final BitSet FOLLOW_NOT_in_likePredicate1557 = new BitSet(new long[]{0x0100000000000000L});
    public static final BitSet FOLLOW_LIKE_in_likePredicate1560 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000010L});
    public static final BitSet FOLLOW_characterStringLiteral_in_likePredicate1562 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_columnReference_in_nullPredicate1596 = new BitSet(new long[]{0x0200000000000000L});
    public static final BitSet FOLLOW_multiValuedColumnReference_in_nullPredicate1600 = new BitSet(new long[]{0x0200000000000000L});
    public static final BitSet FOLLOW_IS_in_nullPredicate1603 = new BitSet(new long[]{0x0400000000000000L});
    public static final BitSet FOLLOW_NULL_in_nullPredicate1605 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_columnReference_in_nullPredicate1634 = new BitSet(new long[]{0x0200000000000000L});
    public static final BitSet FOLLOW_multiValuedColumnReference_in_nullPredicate1638 = new BitSet(new long[]{0x0200000000000000L});
    public static final BitSet FOLLOW_IS_in_nullPredicate1641 = new BitSet(new long[]{0x0002000000000000L});
    public static final BitSet FOLLOW_NOT_in_nullPredicate1643 = new BitSet(new long[]{0x0400000000000000L});
    public static final BitSet FOLLOW_NULL_in_nullPredicate1645 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_literalOrParameterName_in_quantifiedComparisonPredicate1673 = new BitSet(new long[]{0x007C200000000000L});
    public static final BitSet FOLLOW_compOp_in_quantifiedComparisonPredicate1675 = new BitSet(new long[]{0x0800000000000000L});
    public static final BitSet FOLLOW_ANY_in_quantifiedComparisonPredicate1677 = new BitSet(new long[]{0xFF83DF8580000000L,0x0000000000001E67L});
    public static final BitSet FOLLOW_multiValuedColumnReference_in_quantifiedComparisonPredicate1679 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ANY_in_quantifiedInPredicate1708 = new BitSet(new long[]{0xFF83DF8580000000L,0x0000000000001E67L});
    public static final BitSet FOLLOW_multiValuedColumnReference_in_quantifiedInPredicate1710 = new BitSet(new long[]{0x0082000000000000L});
    public static final BitSet FOLLOW_NOT_in_quantifiedInPredicate1712 = new BitSet(new long[]{0x0080000000000000L});
    public static final BitSet FOLLOW_IN_in_quantifiedInPredicate1715 = new BitSet(new long[]{0x0000002000000000L});
    public static final BitSet FOLLOW_LPAREN_in_quantifiedInPredicate1718 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000FF8L});
    public static final BitSet FOLLOW_inValueList_in_quantifiedInPredicate1720 = new BitSet(new long[]{0x0000004000000000L});
    public static final BitSet FOLLOW_RPAREN_in_quantifiedInPredicate1722 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_CONTAINS_in_textSearchPredicate1751 = new BitSet(new long[]{0x0000002000000000L});
    public static final BitSet FOLLOW_LPAREN_in_textSearchPredicate1753 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000070L});
    public static final BitSet FOLLOW_qualifier_in_textSearchPredicate1756 = new BitSet(new long[]{0x0000000200000000L});
    public static final BitSet FOLLOW_COMMA_in_textSearchPredicate1758 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000070L});
    public static final BitSet FOLLOW_textSearchExpression_in_textSearchPredicate1762 = new BitSet(new long[]{0x0000004000000000L});
    public static final BitSet FOLLOW_RPAREN_in_textSearchPredicate1764 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IN_FOLDER_in_folderPredicate1789 = new BitSet(new long[]{0x0000002000000000L});
    public static final BitSet FOLLOW_folderPredicateArgs_in_folderPredicate1792 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IN_TREE_in_folderPredicate1813 = new BitSet(new long[]{0x0000002000000000L});
    public static final BitSet FOLLOW_folderPredicateArgs_in_folderPredicate1815 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_folderPredicateArgs1837 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000070L});
    public static final BitSet FOLLOW_qualifier_in_folderPredicateArgs1840 = new BitSet(new long[]{0x0000000200000000L});
    public static final BitSet FOLLOW_COMMA_in_folderPredicateArgs1842 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000070L});
    public static final BitSet FOLLOW_folderId_in_folderPredicateArgs1846 = new BitSet(new long[]{0x0000004000000000L});
    public static final BitSet FOLLOW_RPAREN_in_folderPredicateArgs1848 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ORDER_in_orderByClause1887 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000001L});
    public static final BitSet FOLLOW_BY_in_orderByClause1889 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000060L});
    public static final BitSet FOLLOW_sortSpecification_in_orderByClause1891 = new BitSet(new long[]{0x0000000200000002L});
    public static final BitSet FOLLOW_COMMA_in_orderByClause1895 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000060L});
    public static final BitSet FOLLOW_sortSpecification_in_orderByClause1897 = new BitSet(new long[]{0x0000000200000002L});
    public static final BitSet FOLLOW_columnReference_in_sortSpecification1923 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_columnReference_in_sortSpecification1941 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000006L});
    public static final BitSet FOLLOW_ASC_in_sortSpecification1947 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DESC_in_sortSpecification1953 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_identifier_in_correlationName1980 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_identifier_in_tableName1994 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_identifier_in_columnName2012 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_identifier_in_multiValuedColumnName2031 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_COLON_in_parameterName2049 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000060L});
    public static final BitSet FOLLOW_identifier_in_parameterName2051 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_characterStringLiteral_in_folderId2074 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_QUOTED_STRING_in_textSearchExpression2095 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_identifier2107 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DOUBLE_QUOTE_in_identifier2122 = new BitSet(new long[]{0xFF83DF8480000000L,0x0000000000001E67L});
    public static final BitSet FOLLOW_keyWordOrId_in_identifier2124 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000040L});
    public static final BitSet FOLLOW_DOUBLE_QUOTE_in_identifier2126 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FLOATING_POINT_LITERAL_in_signedNumericLiteral2146 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_integerLiteral_in_signedNumericLiteral2161 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DECIMAL_INTEGER_LITERAL_in_integerLiteral2180 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TRUE_in_booleanLiteral2204 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FALSE_in_booleanLiteral2222 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TIMESTAMP_in_datetimeLiteral2247 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000010L});
    public static final BitSet FOLLOW_QUOTED_STRING_in_datetimeLiteral2249 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_QUOTED_STRING_in_characterStringLiteral2272 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SELECT_in_keyWord2298 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_AS_in_keyWord2303 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FROM_in_keyWord2308 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_JOIN_in_keyWord2314 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_INNER_in_keyWord2320 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LEFT_in_keyWord2326 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_OUTER_in_keyWord2332 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ON_in_keyWord2338 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_WHERE_in_keyWord2344 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_OR_in_keyWord2350 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_AND_in_keyWord2356 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NOT_in_keyWord2362 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IN_in_keyWord2368 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LIKE_in_keyWord2374 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IS_in_keyWord2380 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NULL_in_keyWord2386 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ANY_in_keyWord2392 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_CONTAINS_in_keyWord2398 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IN_FOLDER_in_keyWord2405 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IN_TREE_in_keyWord2411 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ORDER_in_keyWord2417 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_BY_in_keyWord2422 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ASC_in_keyWord2428 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DESC_in_keyWord2434 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TIMESTAMP_in_keyWord2439 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TRUE_in_keyWord2444 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FALSE_in_keyWord2449 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cmisFunction_in_keyWord2454 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SCORE_in_cmisFunction2465 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_keyWord_in_keyWordOrId2483 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_keyWordOrId2495 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_valueExpression_in_synpred1_CMIS340 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_tableName_in_synpred2_CMIS771 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_columnReference_in_synpred3_CMIS1592 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_columnReference_in_synpred4_CMIS1630 = new BitSet(new long[]{0x0000000000000002L});

}