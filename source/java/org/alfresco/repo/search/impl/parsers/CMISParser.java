// $ANTLR 3.1b1 W:\\workspace-cmis\\ANTLR\\CMIS.g 2008-07-15 16:24:39
package org.alfresco.repo.search.impl.parsers;

import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import org.antlr.runtime.tree.*;

public class CMISParser extends Parser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "QUERY", "ALL_COLUMNS", "COLUMN", "COLUMNS", "COLUMN_REF", "QUALIFIER", "FUNCTION", "SOURCE", "TABLE", "TABLE_REF", "PARAMETER", "CONJUNCTION", "DISJUNCTION", "NEGATION", "PRED_COMPARISON", "PRED_IN", "PRED_EXISTS", "PRED_LIKE", "PRED_FTS", "LIST", "PRED_CHILD", "PRED_DESCENDANT", "SORT_SPECIFICATION", "NUMERIC_LITERAL", "STRING_LITERAL", "SELECT", "STAR", "COMMA", "AS", "DOTSTAR", "DOT", "LPAREN", "RPAREN", "FROM", "JOIN", "INNER", "LEFT", "OUTER", "ON", "EQUALS", "WHERE", "OR", "AND", "NOT", "NOTEQUALS", "LESSTHAN", "GREATERTHAN", "LESSTHANOREQUALS", "GREATERTHANOREQUALS", "IN", "LIKE", "IS", "NULL", "ANY", "CONTAINS", "IN_FOLDER", "IN_TREE", "ORDER", "BY", "ASC", "DESC", "COLON", "QUOTED_STRING", "ID", "DOUBLE_QUOTE", "FLOATING_POINT_LITERAL", "DECIMAL_INTEGER_LITERAL", "UPPER", "LOWER", "SCORE", "DOTDOT", "TILDA", "SINGLE_QUOTE", "ESCAPED_SINGLE_QUOTE", "PLUS", "MINUS", "DECIMAL_NUMERAL", "DIGIT", "EXPONENT", "WS", "ZERO_DIGIT", "NON_ZERO_DIGIT", "E", "SIGNED_INTEGER"
    };
    public static final int FUNCTION=10;
    public static final int WHERE=44;
    public static final int EXPONENT=82;
    public static final int PRED_FTS=22;
    public static final int STAR=30;
    public static final int INNER=39;
    public static final int ORDER=61;
    public static final int DOUBLE_QUOTE=68;
    public static final int NUMERIC_LITERAL=27;
    public static final int PRED_COMPARISON=18;
    public static final int CONTAINS=58;
    public static final int TABLE=12;
    public static final int SOURCE=11;
    public static final int DOTDOT=74;
    public static final int EQUALS=43;
    public static final int NOT=47;
    public static final int ID=67;
    public static final int AND=46;
    public static final int EOF=-1;
    public static final int LPAREN=35;
    public static final int LESSTHANOREQUALS=51;
    public static final int AS=32;
    public static final int SINGLE_QUOTE=76;
    public static final int RPAREN=36;
    public static final int TILDA=75;
    public static final int PRED_LIKE=21;
    public static final int STRING_LITERAL=28;
    public static final int IN=53;
    public static final int DECIMAL_NUMERAL=80;
    public static final int FLOATING_POINT_LITERAL=69;
    public static final int COMMA=31;
    public static final int IS=55;
    public static final int LEFT=40;
    public static final int SIGNED_INTEGER=87;
    public static final int PARAMETER=14;
    public static final int COLUMN=6;
    public static final int PLUS=78;
    public static final int QUOTED_STRING=66;
    public static final int ZERO_DIGIT=84;
    public static final int DIGIT=81;
    public static final int DOT=34;
    public static final int COLUMN_REF=8;
    public static final int SELECT=29;
    public static final int LIKE=54;
    public static final int GREATERTHAN=50;
    public static final int DOTSTAR=33;
    public static final int E=86;
    public static final int OUTER=41;
    public static final int BY=62;
    public static final int LESSTHAN=49;
    public static final int NON_ZERO_DIGIT=85;
    public static final int ASC=63;
    public static final int QUALIFIER=9;
    public static final int CONJUNCTION=15;
    public static final int NULL=56;
    public static final int ON=42;
    public static final int NOTEQUALS=48;
    public static final int MINUS=79;
    public static final int LIST=23;
    public static final int PRED_DESCENDANT=25;
    public static final int JOIN=38;
    public static final int IN_FOLDER=59;
    public static final int COLON=65;
    public static final int GREATERTHANOREQUALS=52;
    public static final int DISJUNCTION=16;
    public static final int COLUMNS=7;
    public static final int WS=83;
    public static final int ANY=57;
    public static final int SCORE=73;
    public static final int NEGATION=17;
    public static final int TABLE_REF=13;
    public static final int SORT_SPECIFICATION=26;
    public static final int IN_TREE=60;
    public static final int OR=45;
    public static final int PRED_CHILD=24;
    public static final int PRED_EXISTS=20;
    public static final int QUERY=4;
    public static final int LOWER=72;
    public static final int DECIMAL_INTEGER_LITERAL=70;
    public static final int DESC=64;
    public static final int ALL_COLUMNS=5;
    public static final int FROM=37;
    public static final int UPPER=71;
    public static final int PRED_IN=19;
    public static final int ESCAPED_SINGLE_QUOTE=77;

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
    public String getGrammarFileName() { return "W:\\workspace-cmis\\ANTLR\\CMIS.g"; }


        private Stack<String> paraphrases = new Stack<String>();

        /**
         * CMIS strict
         */
    	public boolean strict()
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


    public static class query_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start query
    // W:\\workspace-cmis\\ANTLR\\CMIS.g:142:1: query : SELECT selectList fromClause ( whereClause )? ( orderByClause )? EOF -> ^( QUERY selectList fromClause ( whereClause )? ( orderByClause )? ) ;
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
        try {
            // W:\\workspace-cmis\\ANTLR\\CMIS.g:149:2: ( SELECT selectList fromClause ( whereClause )? ( orderByClause )? EOF -> ^( QUERY selectList fromClause ( whereClause )? ( orderByClause )? ) )
            // W:\\workspace-cmis\\ANTLR\\CMIS.g:149:4: SELECT selectList fromClause ( whereClause )? ( orderByClause )? EOF
            {
            SELECT1=(Token)match(input,SELECT,FOLLOW_SELECT_in_query172); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_SELECT.add(SELECT1);

            pushFollow(FOLLOW_selectList_in_query174);
            selectList2=selectList();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_selectList.add(selectList2.getTree());
            pushFollow(FOLLOW_fromClause_in_query176);
            fromClause3=fromClause();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_fromClause.add(fromClause3.getTree());
            // W:\\workspace-cmis\\ANTLR\\CMIS.g:149:33: ( whereClause )?
            int alt1=2;
            int LA1_0 = input.LA(1);

            if ( (LA1_0==WHERE) ) {
                alt1=1;
            }
            switch (alt1) {
                case 1 :
                    // W:\\workspace-cmis\\ANTLR\\CMIS.g:149:33: whereClause
                    {
                    pushFollow(FOLLOW_whereClause_in_query178);
                    whereClause4=whereClause();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_whereClause.add(whereClause4.getTree());

                    }
                    break;

            }

            // W:\\workspace-cmis\\ANTLR\\CMIS.g:149:46: ( orderByClause )?
            int alt2=2;
            int LA2_0 = input.LA(1);

            if ( (LA2_0==ORDER) ) {
                alt2=1;
            }
            switch (alt2) {
                case 1 :
                    // W:\\workspace-cmis\\ANTLR\\CMIS.g:149:46: orderByClause
                    {
                    pushFollow(FOLLOW_orderByClause_in_query181);
                    orderByClause5=orderByClause();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_orderByClause.add(orderByClause5.getTree());

                    }
                    break;

            }

            EOF6=(Token)match(input,EOF,FOLLOW_EOF_in_query184); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_EOF.add(EOF6);



            // AST REWRITE
            // elements: fromClause, selectList, whereClause, orderByClause
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 150:3: -> ^( QUERY selectList fromClause ( whereClause )? ( orderByClause )? )
            {
                // W:\\workspace-cmis\\ANTLR\\CMIS.g:150:6: ^( QUERY selectList fromClause ( whereClause )? ( orderByClause )? )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(QUERY, "QUERY"), root_1);

                adaptor.addChild(root_1, stream_selectList.nextTree());
                adaptor.addChild(root_1, stream_fromClause.nextTree());
                // W:\\workspace-cmis\\ANTLR\\CMIS.g:150:36: ( whereClause )?
                if ( stream_whereClause.hasNext() ) {
                    adaptor.addChild(root_1, stream_whereClause.nextTree());

                }
                stream_whereClause.reset();
                // W:\\workspace-cmis\\ANTLR\\CMIS.g:150:49: ( orderByClause )?
                if ( stream_orderByClause.hasNext() ) {
                    adaptor.addChild(root_1, stream_orderByClause.nextTree());

                }
                stream_orderByClause.reset();

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;retval.tree = root_0;}
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
    // $ANTLR end query

    public static class selectList_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start selectList
    // W:\\workspace-cmis\\ANTLR\\CMIS.g:154:1: selectList : ( STAR -> ^( ALL_COLUMNS ) | selectSubList ( COMMA selectSubList )* -> ^( COLUMNS ( selectSubList )+ ) );
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
            // W:\\workspace-cmis\\ANTLR\\CMIS.g:157:2: ( STAR -> ^( ALL_COLUMNS ) | selectSubList ( COMMA selectSubList )* -> ^( COLUMNS ( selectSubList )+ ) )
            int alt4=2;
            int LA4_0 = input.LA(1);

            if ( (LA4_0==STAR) ) {
                alt4=1;
            }
            else if ( (LA4_0==SELECT||LA4_0==AS||(LA4_0>=FROM && LA4_0<=ON)||(LA4_0>=WHERE && LA4_0<=NOT)||(LA4_0>=IN && LA4_0<=DESC)||(LA4_0>=ID && LA4_0<=DOUBLE_QUOTE)||(LA4_0>=UPPER && LA4_0<=SCORE)) ) {
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
                    // W:\\workspace-cmis\\ANTLR\\CMIS.g:157:4: STAR
                    {
                    STAR7=(Token)match(input,STAR,FOLLOW_STAR_in_selectList233); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_STAR.add(STAR7);



                    // AST REWRITE
                    // elements: 
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 158:3: -> ^( ALL_COLUMNS )
                    {
                        // W:\\workspace-cmis\\ANTLR\\CMIS.g:158:6: ^( ALL_COLUMNS )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(ALL_COLUMNS, "ALL_COLUMNS"), root_1);

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // W:\\workspace-cmis\\ANTLR\\CMIS.g:159:5: selectSubList ( COMMA selectSubList )*
                    {
                    pushFollow(FOLLOW_selectSubList_in_selectList249);
                    selectSubList8=selectSubList();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_selectSubList.add(selectSubList8.getTree());
                    // W:\\workspace-cmis\\ANTLR\\CMIS.g:159:19: ( COMMA selectSubList )*
                    loop3:
                    do {
                        int alt3=2;
                        int LA3_0 = input.LA(1);

                        if ( (LA3_0==COMMA) ) {
                            alt3=1;
                        }


                        switch (alt3) {
                    	case 1 :
                    	    // W:\\workspace-cmis\\ANTLR\\CMIS.g:159:21: COMMA selectSubList
                    	    {
                    	    COMMA9=(Token)match(input,COMMA,FOLLOW_COMMA_in_selectList253); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_COMMA.add(COMMA9);

                    	    pushFollow(FOLLOW_selectSubList_in_selectList255);
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
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 160:3: -> ^( COLUMNS ( selectSubList )+ )
                    {
                        // W:\\workspace-cmis\\ANTLR\\CMIS.g:160:6: ^( COLUMNS ( selectSubList )+ )
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

                    retval.tree = root_0;retval.tree = root_0;}
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
           throw e;
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end selectList

    public static class selectSubList_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start selectSubList
    // W:\\workspace-cmis\\ANTLR\\CMIS.g:164:1: selectSubList : ( ( valueExpression )=> valueExpression ( ( AS )? columnName )? -> ^( COLUMN valueExpression ( columnName )? ) | qualifier DOTSTAR -> ^( ALL_COLUMNS qualifier ) | multiValuedColumnReference ->);
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
            // W:\\workspace-cmis\\ANTLR\\CMIS.g:165:2: ( ( valueExpression )=> valueExpression ( ( AS )? columnName )? -> ^( COLUMN valueExpression ( columnName )? ) | qualifier DOTSTAR -> ^( ALL_COLUMNS qualifier ) | multiValuedColumnReference ->)
            int alt7=3;
            int LA7_0 = input.LA(1);

            if ( (LA7_0==ID) ) {
                int LA7_1 = input.LA(2);

                if ( (LA7_1==DOTSTAR) ) {
                    alt7=2;
                }
                else if ( (synpred1_CMIS()) ) {
                    alt7=1;
                }
                else if ( (true) ) {
                    alt7=3;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 7, 1, input);

                    throw nvae;
                }
            }
            else if ( (LA7_0==DOUBLE_QUOTE) ) {
                int LA7_2 = input.LA(2);

                if ( (LA7_2==SELECT||LA7_2==AS||(LA7_2>=FROM && LA7_2<=ON)||(LA7_2>=WHERE && LA7_2<=NOT)||(LA7_2>=IN && LA7_2<=DESC)||(LA7_2>=UPPER && LA7_2<=SCORE)) ) {
                    int LA7_6 = input.LA(3);

                    if ( (LA7_6==DOUBLE_QUOTE) ) {
                        int LA7_8 = input.LA(4);

                        if ( (LA7_8==DOTSTAR) ) {
                            alt7=2;
                        }
                        else if ( (synpred1_CMIS()) ) {
                            alt7=1;
                        }
                        else if ( (true) ) {
                            alt7=3;
                        }
                        else {
                            if (state.backtracking>0) {state.failed=true; return retval;}
                            NoViableAltException nvae =
                                new NoViableAltException("", 7, 8, input);

                            throw nvae;
                        }
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return retval;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 7, 6, input);

                        throw nvae;
                    }
                }
                else if ( (LA7_2==ID) ) {
                    int LA7_7 = input.LA(3);

                    if ( (LA7_7==DOUBLE_QUOTE) ) {
                        int LA7_8 = input.LA(4);

                        if ( (LA7_8==DOTSTAR) ) {
                            alt7=2;
                        }
                        else if ( (synpred1_CMIS()) ) {
                            alt7=1;
                        }
                        else if ( (true) ) {
                            alt7=3;
                        }
                        else {
                            if (state.backtracking>0) {state.failed=true; return retval;}
                            NoViableAltException nvae =
                                new NoViableAltException("", 7, 8, input);

                            throw nvae;
                        }
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return retval;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 7, 7, input);

                        throw nvae;
                    }
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 7, 2, input);

                    throw nvae;
                }
            }
            else if ( (LA7_0==SELECT||LA7_0==AS||(LA7_0>=FROM && LA7_0<=ON)||(LA7_0>=WHERE && LA7_0<=NOT)||(LA7_0>=IN && LA7_0<=DESC)||(LA7_0>=UPPER && LA7_0<=SCORE)) && (synpred1_CMIS())) {
                alt7=1;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 7, 0, input);

                throw nvae;
            }
            switch (alt7) {
                case 1 :
                    // W:\\workspace-cmis\\ANTLR\\CMIS.g:165:4: ( valueExpression )=> valueExpression ( ( AS )? columnName )?
                    {
                    pushFollow(FOLLOW_valueExpression_in_selectSubList291);
                    valueExpression11=valueExpression();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_valueExpression.add(valueExpression11.getTree());
                    // W:\\workspace-cmis\\ANTLR\\CMIS.g:165:40: ( ( AS )? columnName )?
                    int alt6=2;
                    int LA6_0 = input.LA(1);

                    if ( (LA6_0==AS||(LA6_0>=ID && LA6_0<=DOUBLE_QUOTE)) ) {
                        alt6=1;
                    }
                    switch (alt6) {
                        case 1 :
                            // W:\\workspace-cmis\\ANTLR\\CMIS.g:165:42: ( AS )? columnName
                            {
                            // W:\\workspace-cmis\\ANTLR\\CMIS.g:165:42: ( AS )?
                            int alt5=2;
                            int LA5_0 = input.LA(1);

                            if ( (LA5_0==AS) ) {
                                alt5=1;
                            }
                            switch (alt5) {
                                case 1 :
                                    // W:\\workspace-cmis\\ANTLR\\CMIS.g:165:42: AS
                                    {
                                    AS12=(Token)match(input,AS,FOLLOW_AS_in_selectSubList295); if (state.failed) return retval; 
                                    if ( state.backtracking==0 ) stream_AS.add(AS12);


                                    }
                                    break;

                            }

                            pushFollow(FOLLOW_columnName_in_selectSubList298);
                            columnName13=columnName();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_columnName.add(columnName13.getTree());

                            }
                            break;

                    }



                    // AST REWRITE
                    // elements: valueExpression, columnName
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 166:3: -> ^( COLUMN valueExpression ( columnName )? )
                    {
                        // W:\\workspace-cmis\\ANTLR\\CMIS.g:166:6: ^( COLUMN valueExpression ( columnName )? )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(COLUMN, "COLUMN"), root_1);

                        adaptor.addChild(root_1, stream_valueExpression.nextTree());
                        // W:\\workspace-cmis\\ANTLR\\CMIS.g:166:31: ( columnName )?
                        if ( stream_columnName.hasNext() ) {
                            adaptor.addChild(root_1, stream_columnName.nextTree());

                        }
                        stream_columnName.reset();

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // W:\\workspace-cmis\\ANTLR\\CMIS.g:167:4: qualifier DOTSTAR
                    {
                    pushFollow(FOLLOW_qualifier_in_selectSubList319);
                    qualifier14=qualifier();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_qualifier.add(qualifier14.getTree());
                    DOTSTAR15=(Token)match(input,DOTSTAR,FOLLOW_DOTSTAR_in_selectSubList321); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_DOTSTAR.add(DOTSTAR15);



                    // AST REWRITE
                    // elements: qualifier
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 168:3: -> ^( ALL_COLUMNS qualifier )
                    {
                        // W:\\workspace-cmis\\ANTLR\\CMIS.g:168:6: ^( ALL_COLUMNS qualifier )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(ALL_COLUMNS, "ALL_COLUMNS"), root_1);

                        adaptor.addChild(root_1, stream_qualifier.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;retval.tree = root_0;}
                    }
                    break;
                case 3 :
                    // W:\\workspace-cmis\\ANTLR\\CMIS.g:169:4: multiValuedColumnReference
                    {
                    pushFollow(FOLLOW_multiValuedColumnReference_in_selectSubList337);
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
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 170:3: ->
                    {
                        root_0 = null;
                    }

                    retval.tree = root_0;retval.tree = root_0;}
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
    // $ANTLR end selectSubList

    public static class valueExpression_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start valueExpression
    // W:\\workspace-cmis\\ANTLR\\CMIS.g:173:1: valueExpression : ( columnReference -> columnReference | valueFunction -> valueFunction );
    public final CMISParser.valueExpression_return valueExpression() throws RecognitionException {
        CMISParser.valueExpression_return retval = new CMISParser.valueExpression_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        CMISParser.columnReference_return columnReference17 = null;

        CMISParser.valueFunction_return valueFunction18 = null;


        RewriteRuleSubtreeStream stream_valueFunction=new RewriteRuleSubtreeStream(adaptor,"rule valueFunction");
        RewriteRuleSubtreeStream stream_columnReference=new RewriteRuleSubtreeStream(adaptor,"rule columnReference");
        try {
            // W:\\workspace-cmis\\ANTLR\\CMIS.g:174:2: ( columnReference -> columnReference | valueFunction -> valueFunction )
            int alt8=2;
            switch ( input.LA(1) ) {
            case ID:
                {
                int LA8_1 = input.LA(2);

                if ( (LA8_1==EOF||(LA8_1>=COMMA && LA8_1<=AS)||LA8_1==DOT||LA8_1==FROM||LA8_1==EQUALS||(LA8_1>=NOTEQUALS && LA8_1<=GREATERTHANOREQUALS)||(LA8_1>=ID && LA8_1<=DOUBLE_QUOTE)) ) {
                    alt8=1;
                }
                else if ( (LA8_1==LPAREN) ) {
                    alt8=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 8, 1, input);

                    throw nvae;
                }
                }
                break;
            case DOUBLE_QUOTE:
                {
                alt8=1;
                }
                break;
            case SELECT:
            case AS:
            case FROM:
            case JOIN:
            case INNER:
            case LEFT:
            case OUTER:
            case ON:
            case WHERE:
            case OR:
            case AND:
            case NOT:
            case IN:
            case LIKE:
            case IS:
            case NULL:
            case ANY:
            case CONTAINS:
            case IN_FOLDER:
            case IN_TREE:
            case ORDER:
            case BY:
            case ASC:
            case DESC:
            case UPPER:
            case LOWER:
            case SCORE:
                {
                alt8=2;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 8, 0, input);

                throw nvae;
            }

            switch (alt8) {
                case 1 :
                    // W:\\workspace-cmis\\ANTLR\\CMIS.g:174:4: columnReference
                    {
                    pushFollow(FOLLOW_columnReference_in_valueExpression356);
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
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 175:3: -> columnReference
                    {
                        adaptor.addChild(root_0, stream_columnReference.nextTree());

                    }

                    retval.tree = root_0;retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // W:\\workspace-cmis\\ANTLR\\CMIS.g:176:5: valueFunction
                    {
                    pushFollow(FOLLOW_valueFunction_in_valueExpression369);
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
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 177:3: -> valueFunction
                    {
                        adaptor.addChild(root_0, stream_valueFunction.nextTree());

                    }

                    retval.tree = root_0;retval.tree = root_0;}
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
    // $ANTLR end valueExpression

    public static class columnReference_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start columnReference
    // W:\\workspace-cmis\\ANTLR\\CMIS.g:180:1: columnReference : ( qualifier DOT )? columnName -> ^( COLUMN_REF columnName ( qualifier )? ) ;
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
        try {
            // W:\\workspace-cmis\\ANTLR\\CMIS.g:181:2: ( ( qualifier DOT )? columnName -> ^( COLUMN_REF columnName ( qualifier )? ) )
            // W:\\workspace-cmis\\ANTLR\\CMIS.g:181:4: ( qualifier DOT )? columnName
            {
            // W:\\workspace-cmis\\ANTLR\\CMIS.g:181:4: ( qualifier DOT )?
            int alt9=2;
            int LA9_0 = input.LA(1);

            if ( (LA9_0==ID) ) {
                int LA9_1 = input.LA(2);

                if ( (LA9_1==DOT) ) {
                    alt9=1;
                }
            }
            else if ( (LA9_0==DOUBLE_QUOTE) ) {
                int LA9_2 = input.LA(2);

                if ( (LA9_2==SELECT||LA9_2==AS||(LA9_2>=FROM && LA9_2<=ON)||(LA9_2>=WHERE && LA9_2<=NOT)||(LA9_2>=IN && LA9_2<=DESC)||(LA9_2>=UPPER && LA9_2<=SCORE)) ) {
                    int LA9_5 = input.LA(3);

                    if ( (LA9_5==DOUBLE_QUOTE) ) {
                        int LA9_7 = input.LA(4);

                        if ( (LA9_7==DOT) ) {
                            alt9=1;
                        }
                    }
                }
                else if ( (LA9_2==ID) ) {
                    int LA9_6 = input.LA(3);

                    if ( (LA9_6==DOUBLE_QUOTE) ) {
                        int LA9_7 = input.LA(4);

                        if ( (LA9_7==DOT) ) {
                            alt9=1;
                        }
                    }
                }
            }
            switch (alt9) {
                case 1 :
                    // W:\\workspace-cmis\\ANTLR\\CMIS.g:181:6: qualifier DOT
                    {
                    pushFollow(FOLLOW_qualifier_in_columnReference392);
                    qualifier19=qualifier();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_qualifier.add(qualifier19.getTree());
                    DOT20=(Token)match(input,DOT,FOLLOW_DOT_in_columnReference394); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_DOT.add(DOT20);


                    }
                    break;

            }

            pushFollow(FOLLOW_columnName_in_columnReference399);
            columnName21=columnName();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_columnName.add(columnName21.getTree());


            // AST REWRITE
            // elements: qualifier, columnName
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 182:3: -> ^( COLUMN_REF columnName ( qualifier )? )
            {
                // W:\\workspace-cmis\\ANTLR\\CMIS.g:182:6: ^( COLUMN_REF columnName ( qualifier )? )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(COLUMN_REF, "COLUMN_REF"), root_1);

                adaptor.addChild(root_1, stream_columnName.nextTree());
                // W:\\workspace-cmis\\ANTLR\\CMIS.g:182:30: ( qualifier )?
                if ( stream_qualifier.hasNext() ) {
                    adaptor.addChild(root_1, stream_qualifier.nextTree());

                }
                stream_qualifier.reset();

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;retval.tree = root_0;}
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
    // $ANTLR end columnReference

    public static class multiValuedColumnReference_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start multiValuedColumnReference
    // W:\\workspace-cmis\\ANTLR\\CMIS.g:189:1: multiValuedColumnReference : ( qualifier DOT )? multiValuedColumnName -> ^( COLUMN_REF multiValuedColumnName ( qualifier )? ) ;
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
        try {
            // W:\\workspace-cmis\\ANTLR\\CMIS.g:190:2: ( ( qualifier DOT )? multiValuedColumnName -> ^( COLUMN_REF multiValuedColumnName ( qualifier )? ) )
            // W:\\workspace-cmis\\ANTLR\\CMIS.g:190:10: ( qualifier DOT )? multiValuedColumnName
            {
            // W:\\workspace-cmis\\ANTLR\\CMIS.g:190:10: ( qualifier DOT )?
            int alt10=2;
            int LA10_0 = input.LA(1);

            if ( (LA10_0==ID) ) {
                int LA10_1 = input.LA(2);

                if ( (LA10_1==DOT) ) {
                    alt10=1;
                }
            }
            else if ( (LA10_0==DOUBLE_QUOTE) ) {
                int LA10_2 = input.LA(2);

                if ( (LA10_2==SELECT||LA10_2==AS||(LA10_2>=FROM && LA10_2<=ON)||(LA10_2>=WHERE && LA10_2<=NOT)||(LA10_2>=IN && LA10_2<=DESC)||(LA10_2>=UPPER && LA10_2<=SCORE)) ) {
                    int LA10_5 = input.LA(3);

                    if ( (LA10_5==DOUBLE_QUOTE) ) {
                        int LA10_7 = input.LA(4);

                        if ( (LA10_7==DOT) ) {
                            alt10=1;
                        }
                    }
                }
                else if ( (LA10_2==ID) ) {
                    int LA10_6 = input.LA(3);

                    if ( (LA10_6==DOUBLE_QUOTE) ) {
                        int LA10_7 = input.LA(4);

                        if ( (LA10_7==DOT) ) {
                            alt10=1;
                        }
                    }
                }
            }
            switch (alt10) {
                case 1 :
                    // W:\\workspace-cmis\\ANTLR\\CMIS.g:190:12: qualifier DOT
                    {
                    pushFollow(FOLLOW_qualifier_in_multiValuedColumnReference435);
                    qualifier22=qualifier();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_qualifier.add(qualifier22.getTree());
                    DOT23=(Token)match(input,DOT,FOLLOW_DOT_in_multiValuedColumnReference437); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_DOT.add(DOT23);


                    }
                    break;

            }

            pushFollow(FOLLOW_multiValuedColumnName_in_multiValuedColumnReference443);
            multiValuedColumnName24=multiValuedColumnName();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_multiValuedColumnName.add(multiValuedColumnName24.getTree());


            // AST REWRITE
            // elements: qualifier, multiValuedColumnName
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 191:3: -> ^( COLUMN_REF multiValuedColumnName ( qualifier )? )
            {
                // W:\\workspace-cmis\\ANTLR\\CMIS.g:191:6: ^( COLUMN_REF multiValuedColumnName ( qualifier )? )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(COLUMN_REF, "COLUMN_REF"), root_1);

                adaptor.addChild(root_1, stream_multiValuedColumnName.nextTree());
                // W:\\workspace-cmis\\ANTLR\\CMIS.g:191:41: ( qualifier )?
                if ( stream_qualifier.hasNext() ) {
                    adaptor.addChild(root_1, stream_qualifier.nextTree());

                }
                stream_qualifier.reset();

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;retval.tree = root_0;}
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
    // $ANTLR end multiValuedColumnReference

    public static class valueFunction_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start valueFunction
    // W:\\workspace-cmis\\ANTLR\\CMIS.g:194:1: valueFunction : functionName= keyWordOrId LPAREN ( functionArgument )* RPAREN -> ^( FUNCTION $functionName ( functionArgument )* ) ;
    public final CMISParser.valueFunction_return valueFunction() throws RecognitionException {
        CMISParser.valueFunction_return retval = new CMISParser.valueFunction_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token LPAREN25=null;
        Token RPAREN27=null;
        CMISParser.keyWordOrId_return functionName = null;

        CMISParser.functionArgument_return functionArgument26 = null;


        Object LPAREN25_tree=null;
        Object RPAREN27_tree=null;
        RewriteRuleTokenStream stream_RPAREN=new RewriteRuleTokenStream(adaptor,"token RPAREN");
        RewriteRuleTokenStream stream_LPAREN=new RewriteRuleTokenStream(adaptor,"token LPAREN");
        RewriteRuleSubtreeStream stream_keyWordOrId=new RewriteRuleSubtreeStream(adaptor,"rule keyWordOrId");
        RewriteRuleSubtreeStream stream_functionArgument=new RewriteRuleSubtreeStream(adaptor,"rule functionArgument");
        try {
            // W:\\workspace-cmis\\ANTLR\\CMIS.g:195:2: (functionName= keyWordOrId LPAREN ( functionArgument )* RPAREN -> ^( FUNCTION $functionName ( functionArgument )* ) )
            // W:\\workspace-cmis\\ANTLR\\CMIS.g:195:4: functionName= keyWordOrId LPAREN ( functionArgument )* RPAREN
            {
            pushFollow(FOLLOW_keyWordOrId_in_valueFunction470);
            functionName=keyWordOrId();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_keyWordOrId.add(functionName.getTree());
            LPAREN25=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_valueFunction472); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_LPAREN.add(LPAREN25);

            // W:\\workspace-cmis\\ANTLR\\CMIS.g:195:36: ( functionArgument )*
            loop11:
            do {
                int alt11=2;
                int LA11_0 = input.LA(1);

                if ( ((LA11_0>=COLON && LA11_0<=DECIMAL_INTEGER_LITERAL)) ) {
                    alt11=1;
                }


                switch (alt11) {
            	case 1 :
            	    // W:\\workspace-cmis\\ANTLR\\CMIS.g:195:36: functionArgument
            	    {
            	    pushFollow(FOLLOW_functionArgument_in_valueFunction474);
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

            RPAREN27=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_valueFunction477); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_RPAREN.add(RPAREN27);



            // AST REWRITE
            // elements: functionName, functionArgument
            // token labels: 
            // rule labels: retval, functionName
            // token list labels: 
            // rule list labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);
            RewriteRuleSubtreeStream stream_functionName=new RewriteRuleSubtreeStream(adaptor,"token functionName",functionName!=null?functionName.tree:null);

            root_0 = (Object)adaptor.nil();
            // 196:3: -> ^( FUNCTION $functionName ( functionArgument )* )
            {
                // W:\\workspace-cmis\\ANTLR\\CMIS.g:196:6: ^( FUNCTION $functionName ( functionArgument )* )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(FUNCTION, "FUNCTION"), root_1);

                adaptor.addChild(root_1, stream_functionName.nextTree());
                // W:\\workspace-cmis\\ANTLR\\CMIS.g:196:31: ( functionArgument )*
                while ( stream_functionArgument.hasNext() ) {
                    adaptor.addChild(root_1, stream_functionArgument.nextTree());

                }
                stream_functionArgument.reset();

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;retval.tree = root_0;}
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
    // $ANTLR end valueFunction

    public static class functionArgument_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start functionArgument
    // W:\\workspace-cmis\\ANTLR\\CMIS.g:199:1: functionArgument : ( qualifier DOT columnName -> ^( COLUMN_REF columnName qualifier ) | identifier | literalOrParameterName );
    public final CMISParser.functionArgument_return functionArgument() throws RecognitionException {
        CMISParser.functionArgument_return retval = new CMISParser.functionArgument_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token DOT29=null;
        CMISParser.qualifier_return qualifier28 = null;

        CMISParser.columnName_return columnName30 = null;

        CMISParser.identifier_return identifier31 = null;

        CMISParser.literalOrParameterName_return literalOrParameterName32 = null;


        Object DOT29_tree=null;
        RewriteRuleTokenStream stream_DOT=new RewriteRuleTokenStream(adaptor,"token DOT");
        RewriteRuleSubtreeStream stream_columnName=new RewriteRuleSubtreeStream(adaptor,"rule columnName");
        RewriteRuleSubtreeStream stream_qualifier=new RewriteRuleSubtreeStream(adaptor,"rule qualifier");
        try {
            // W:\\workspace-cmis\\ANTLR\\CMIS.g:200:5: ( qualifier DOT columnName -> ^( COLUMN_REF columnName qualifier ) | identifier | literalOrParameterName )
            int alt12=3;
            switch ( input.LA(1) ) {
            case ID:
                {
                int LA12_1 = input.LA(2);

                if ( (LA12_1==RPAREN||(LA12_1>=COLON && LA12_1<=DECIMAL_INTEGER_LITERAL)) ) {
                    alt12=2;
                }
                else if ( (LA12_1==DOT) ) {
                    alt12=1;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 12, 1, input);

                    throw nvae;
                }
                }
                break;
            case DOUBLE_QUOTE:
                {
                int LA12_2 = input.LA(2);

                if ( (LA12_2==SELECT||LA12_2==AS||(LA12_2>=FROM && LA12_2<=ON)||(LA12_2>=WHERE && LA12_2<=NOT)||(LA12_2>=IN && LA12_2<=DESC)||(LA12_2>=UPPER && LA12_2<=SCORE)) ) {
                    int LA12_6 = input.LA(3);

                    if ( (LA12_6==DOUBLE_QUOTE) ) {
                        int LA12_8 = input.LA(4);

                        if ( (LA12_8==RPAREN||(LA12_8>=COLON && LA12_8<=DECIMAL_INTEGER_LITERAL)) ) {
                            alt12=2;
                        }
                        else if ( (LA12_8==DOT) ) {
                            alt12=1;
                        }
                        else {
                            if (state.backtracking>0) {state.failed=true; return retval;}
                            NoViableAltException nvae =
                                new NoViableAltException("", 12, 8, input);

                            throw nvae;
                        }
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return retval;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 12, 6, input);

                        throw nvae;
                    }
                }
                else if ( (LA12_2==ID) ) {
                    int LA12_7 = input.LA(3);

                    if ( (LA12_7==DOUBLE_QUOTE) ) {
                        int LA12_8 = input.LA(4);

                        if ( (LA12_8==RPAREN||(LA12_8>=COLON && LA12_8<=DECIMAL_INTEGER_LITERAL)) ) {
                            alt12=2;
                        }
                        else if ( (LA12_8==DOT) ) {
                            alt12=1;
                        }
                        else {
                            if (state.backtracking>0) {state.failed=true; return retval;}
                            NoViableAltException nvae =
                                new NoViableAltException("", 12, 8, input);

                            throw nvae;
                        }
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return retval;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 12, 7, input);

                        throw nvae;
                    }
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 12, 2, input);

                    throw nvae;
                }
                }
                break;
            case COLON:
            case QUOTED_STRING:
            case FLOATING_POINT_LITERAL:
            case DECIMAL_INTEGER_LITERAL:
                {
                alt12=3;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 12, 0, input);

                throw nvae;
            }

            switch (alt12) {
                case 1 :
                    // W:\\workspace-cmis\\ANTLR\\CMIS.g:200:9: qualifier DOT columnName
                    {
                    pushFollow(FOLLOW_qualifier_in_functionArgument508);
                    qualifier28=qualifier();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_qualifier.add(qualifier28.getTree());
                    DOT29=(Token)match(input,DOT,FOLLOW_DOT_in_functionArgument510); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_DOT.add(DOT29);

                    pushFollow(FOLLOW_columnName_in_functionArgument512);
                    columnName30=columnName();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_columnName.add(columnName30.getTree());


                    // AST REWRITE
                    // elements: qualifier, columnName
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 201:5: -> ^( COLUMN_REF columnName qualifier )
                    {
                        // W:\\workspace-cmis\\ANTLR\\CMIS.g:201:8: ^( COLUMN_REF columnName qualifier )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(COLUMN_REF, "COLUMN_REF"), root_1);

                        adaptor.addChild(root_1, stream_columnName.nextTree());
                        adaptor.addChild(root_1, stream_qualifier.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // W:\\workspace-cmis\\ANTLR\\CMIS.g:202:9: identifier
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_identifier_in_functionArgument536);
                    identifier31=identifier();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, identifier31.getTree());

                    }
                    break;
                case 3 :
                    // W:\\workspace-cmis\\ANTLR\\CMIS.g:203:9: literalOrParameterName
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_literalOrParameterName_in_functionArgument546);
                    literalOrParameterName32=literalOrParameterName();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, literalOrParameterName32.getTree());

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
    // $ANTLR end functionArgument

    public static class qualifier_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start qualifier
    // W:\\workspace-cmis\\ANTLR\\CMIS.g:206:1: qualifier : ( ( tableName )=> tableName -> tableName | correlationName -> correlationName );
    public final CMISParser.qualifier_return qualifier() throws RecognitionException {
        CMISParser.qualifier_return retval = new CMISParser.qualifier_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        CMISParser.tableName_return tableName33 = null;

        CMISParser.correlationName_return correlationName34 = null;


        RewriteRuleSubtreeStream stream_correlationName=new RewriteRuleSubtreeStream(adaptor,"rule correlationName");
        RewriteRuleSubtreeStream stream_tableName=new RewriteRuleSubtreeStream(adaptor,"rule tableName");
        try {
            // W:\\workspace-cmis\\ANTLR\\CMIS.g:207:2: ( ( tableName )=> tableName -> tableName | correlationName -> correlationName )
            int alt13=2;
            int LA13_0 = input.LA(1);

            if ( (LA13_0==ID) ) {
                int LA13_1 = input.LA(2);

                if ( (synpred2_CMIS()) ) {
                    alt13=1;
                }
                else if ( (true) ) {
                    alt13=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 13, 1, input);

                    throw nvae;
                }
            }
            else if ( (LA13_0==DOUBLE_QUOTE) ) {
                int LA13_2 = input.LA(2);

                if ( (LA13_2==SELECT||LA13_2==AS||(LA13_2>=FROM && LA13_2<=ON)||(LA13_2>=WHERE && LA13_2<=NOT)||(LA13_2>=IN && LA13_2<=DESC)||(LA13_2>=UPPER && LA13_2<=SCORE)) ) {
                    int LA13_5 = input.LA(3);

                    if ( (LA13_5==DOUBLE_QUOTE) ) {
                        int LA13_7 = input.LA(4);

                        if ( (synpred2_CMIS()) ) {
                            alt13=1;
                        }
                        else if ( (true) ) {
                            alt13=2;
                        }
                        else {
                            if (state.backtracking>0) {state.failed=true; return retval;}
                            NoViableAltException nvae =
                                new NoViableAltException("", 13, 7, input);

                            throw nvae;
                        }
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return retval;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 13, 5, input);

                        throw nvae;
                    }
                }
                else if ( (LA13_2==ID) ) {
                    int LA13_6 = input.LA(3);

                    if ( (LA13_6==DOUBLE_QUOTE) ) {
                        int LA13_7 = input.LA(4);

                        if ( (synpred2_CMIS()) ) {
                            alt13=1;
                        }
                        else if ( (true) ) {
                            alt13=2;
                        }
                        else {
                            if (state.backtracking>0) {state.failed=true; return retval;}
                            NoViableAltException nvae =
                                new NoViableAltException("", 13, 7, input);

                            throw nvae;
                        }
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return retval;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 13, 6, input);

                        throw nvae;
                    }
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 13, 2, input);

                    throw nvae;
                }
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 13, 0, input);

                throw nvae;
            }
            switch (alt13) {
                case 1 :
                    // W:\\workspace-cmis\\ANTLR\\CMIS.g:207:4: ( tableName )=> tableName
                    {
                    pushFollow(FOLLOW_tableName_in_qualifier567);
                    tableName33=tableName();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_tableName.add(tableName33.getTree());


                    // AST REWRITE
                    // elements: tableName
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 208:3: -> tableName
                    {
                        adaptor.addChild(root_0, stream_tableName.nextTree());

                    }

                    retval.tree = root_0;retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // W:\\workspace-cmis\\ANTLR\\CMIS.g:209:5: correlationName
                    {
                    pushFollow(FOLLOW_correlationName_in_qualifier579);
                    correlationName34=correlationName();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_correlationName.add(correlationName34.getTree());


                    // AST REWRITE
                    // elements: correlationName
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 210:3: -> correlationName
                    {
                        adaptor.addChild(root_0, stream_correlationName.nextTree());

                    }

                    retval.tree = root_0;retval.tree = root_0;}
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
    // $ANTLR end qualifier

    public static class fromClause_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start fromClause
    // W:\\workspace-cmis\\ANTLR\\CMIS.g:213:1: fromClause : FROM tableReference -> tableReference ;
    public final CMISParser.fromClause_return fromClause() throws RecognitionException {
        CMISParser.fromClause_return retval = new CMISParser.fromClause_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token FROM35=null;
        CMISParser.tableReference_return tableReference36 = null;


        Object FROM35_tree=null;
        RewriteRuleTokenStream stream_FROM=new RewriteRuleTokenStream(adaptor,"token FROM");
        RewriteRuleSubtreeStream stream_tableReference=new RewriteRuleSubtreeStream(adaptor,"rule tableReference");
            paraphrases.push("in from"); 
        try {
            // W:\\workspace-cmis\\ANTLR\\CMIS.g:216:2: ( FROM tableReference -> tableReference )
            // W:\\workspace-cmis\\ANTLR\\CMIS.g:216:4: FROM tableReference
            {
            FROM35=(Token)match(input,FROM,FOLLOW_FROM_in_fromClause616); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_FROM.add(FROM35);

            pushFollow(FOLLOW_tableReference_in_fromClause618);
            tableReference36=tableReference();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_tableReference.add(tableReference36.getTree());


            // AST REWRITE
            // elements: tableReference
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 217:3: -> tableReference
            {
                adaptor.addChild(root_0, stream_tableReference.nextTree());

            }

            retval.tree = root_0;retval.tree = root_0;}
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
           throw e;
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end fromClause

    public static class tableReference_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start tableReference
    // W:\\workspace-cmis\\ANTLR\\CMIS.g:220:1: tableReference : singleTable ( ( joinedTable )=> joinedTable )* -> ^( SOURCE singleTable ( joinedTable )* ) ;
    public final CMISParser.tableReference_return tableReference() throws RecognitionException {
        CMISParser.tableReference_return retval = new CMISParser.tableReference_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        CMISParser.singleTable_return singleTable37 = null;

        CMISParser.joinedTable_return joinedTable38 = null;


        RewriteRuleSubtreeStream stream_singleTable=new RewriteRuleSubtreeStream(adaptor,"rule singleTable");
        RewriteRuleSubtreeStream stream_joinedTable=new RewriteRuleSubtreeStream(adaptor,"rule joinedTable");
        try {
            // W:\\workspace-cmis\\ANTLR\\CMIS.g:221:2: ( singleTable ( ( joinedTable )=> joinedTable )* -> ^( SOURCE singleTable ( joinedTable )* ) )
            // W:\\workspace-cmis\\ANTLR\\CMIS.g:221:4: singleTable ( ( joinedTable )=> joinedTable )*
            {
            pushFollow(FOLLOW_singleTable_in_tableReference636);
            singleTable37=singleTable();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_singleTable.add(singleTable37.getTree());
            // W:\\workspace-cmis\\ANTLR\\CMIS.g:221:16: ( ( joinedTable )=> joinedTable )*
            loop14:
            do {
                int alt14=2;
                switch ( input.LA(1) ) {
                case INNER:
                    {
                    int LA14_2 = input.LA(2);

                    if ( (synpred3_CMIS()) ) {
                        alt14=1;
                    }


                    }
                    break;
                case LEFT:
                    {
                    int LA14_3 = input.LA(2);

                    if ( (synpred3_CMIS()) ) {
                        alt14=1;
                    }


                    }
                    break;
                case JOIN:
                    {
                    int LA14_4 = input.LA(2);

                    if ( (synpred3_CMIS()) ) {
                        alt14=1;
                    }


                    }
                    break;

                }

                switch (alt14) {
            	case 1 :
            	    // W:\\workspace-cmis\\ANTLR\\CMIS.g:221:17: ( joinedTable )=> joinedTable
            	    {
            	    pushFollow(FOLLOW_joinedTable_in_tableReference645);
            	    joinedTable38=joinedTable();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_joinedTable.add(joinedTable38.getTree());

            	    }
            	    break;

            	default :
            	    break loop14;
                }
            } while (true);



            // AST REWRITE
            // elements: joinedTable, singleTable
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 222:3: -> ^( SOURCE singleTable ( joinedTable )* )
            {
                // W:\\workspace-cmis\\ANTLR\\CMIS.g:222:6: ^( SOURCE singleTable ( joinedTable )* )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(SOURCE, "SOURCE"), root_1);

                adaptor.addChild(root_1, stream_singleTable.nextTree());
                // W:\\workspace-cmis\\ANTLR\\CMIS.g:222:27: ( joinedTable )*
                while ( stream_joinedTable.hasNext() ) {
                    adaptor.addChild(root_1, stream_joinedTable.nextTree());

                }
                stream_joinedTable.reset();

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;retval.tree = root_0;}
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
    // $ANTLR end tableReference

    public static class singleTable_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start singleTable
    // W:\\workspace-cmis\\ANTLR\\CMIS.g:228:1: singleTable : ( tableName ( ( AS )? correlationName )? -> ^( TABLE_REF tableName ( correlationName )? ) | LPAREN joinedTables RPAREN -> ^( TABLE joinedTables ) );
    public final CMISParser.singleTable_return singleTable() throws RecognitionException {
        CMISParser.singleTable_return retval = new CMISParser.singleTable_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token AS40=null;
        Token LPAREN42=null;
        Token RPAREN44=null;
        CMISParser.tableName_return tableName39 = null;

        CMISParser.correlationName_return correlationName41 = null;

        CMISParser.joinedTables_return joinedTables43 = null;


        Object AS40_tree=null;
        Object LPAREN42_tree=null;
        Object RPAREN44_tree=null;
        RewriteRuleTokenStream stream_AS=new RewriteRuleTokenStream(adaptor,"token AS");
        RewriteRuleTokenStream stream_RPAREN=new RewriteRuleTokenStream(adaptor,"token RPAREN");
        RewriteRuleTokenStream stream_LPAREN=new RewriteRuleTokenStream(adaptor,"token LPAREN");
        RewriteRuleSubtreeStream stream_correlationName=new RewriteRuleSubtreeStream(adaptor,"rule correlationName");
        RewriteRuleSubtreeStream stream_tableName=new RewriteRuleSubtreeStream(adaptor,"rule tableName");
        RewriteRuleSubtreeStream stream_joinedTables=new RewriteRuleSubtreeStream(adaptor,"rule joinedTables");
        try {
            // W:\\workspace-cmis\\ANTLR\\CMIS.g:229:2: ( tableName ( ( AS )? correlationName )? -> ^( TABLE_REF tableName ( correlationName )? ) | LPAREN joinedTables RPAREN -> ^( TABLE joinedTables ) )
            int alt17=2;
            int LA17_0 = input.LA(1);

            if ( ((LA17_0>=ID && LA17_0<=DOUBLE_QUOTE)) ) {
                alt17=1;
            }
            else if ( (LA17_0==LPAREN) ) {
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
                    // W:\\workspace-cmis\\ANTLR\\CMIS.g:229:4: tableName ( ( AS )? correlationName )?
                    {
                    pushFollow(FOLLOW_tableName_in_singleTable674);
                    tableName39=tableName();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_tableName.add(tableName39.getTree());
                    // W:\\workspace-cmis\\ANTLR\\CMIS.g:229:14: ( ( AS )? correlationName )?
                    int alt16=2;
                    int LA16_0 = input.LA(1);

                    if ( (LA16_0==AS||(LA16_0>=ID && LA16_0<=DOUBLE_QUOTE)) ) {
                        alt16=1;
                    }
                    switch (alt16) {
                        case 1 :
                            // W:\\workspace-cmis\\ANTLR\\CMIS.g:229:16: ( AS )? correlationName
                            {
                            // W:\\workspace-cmis\\ANTLR\\CMIS.g:229:16: ( AS )?
                            int alt15=2;
                            int LA15_0 = input.LA(1);

                            if ( (LA15_0==AS) ) {
                                alt15=1;
                            }
                            switch (alt15) {
                                case 1 :
                                    // W:\\workspace-cmis\\ANTLR\\CMIS.g:229:16: AS
                                    {
                                    AS40=(Token)match(input,AS,FOLLOW_AS_in_singleTable678); if (state.failed) return retval; 
                                    if ( state.backtracking==0 ) stream_AS.add(AS40);


                                    }
                                    break;

                            }

                            pushFollow(FOLLOW_correlationName_in_singleTable681);
                            correlationName41=correlationName();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_correlationName.add(correlationName41.getTree());

                            }
                            break;

                    }



                    // AST REWRITE
                    // elements: tableName, correlationName
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 230:3: -> ^( TABLE_REF tableName ( correlationName )? )
                    {
                        // W:\\workspace-cmis\\ANTLR\\CMIS.g:230:6: ^( TABLE_REF tableName ( correlationName )? )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(TABLE_REF, "TABLE_REF"), root_1);

                        adaptor.addChild(root_1, stream_tableName.nextTree());
                        // W:\\workspace-cmis\\ANTLR\\CMIS.g:230:28: ( correlationName )?
                        if ( stream_correlationName.hasNext() ) {
                            adaptor.addChild(root_1, stream_correlationName.nextTree());

                        }
                        stream_correlationName.reset();

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // W:\\workspace-cmis\\ANTLR\\CMIS.g:231:4: LPAREN joinedTables RPAREN
                    {
                    LPAREN42=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_singleTable702); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_LPAREN.add(LPAREN42);

                    pushFollow(FOLLOW_joinedTables_in_singleTable704);
                    joinedTables43=joinedTables();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_joinedTables.add(joinedTables43.getTree());
                    RPAREN44=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_singleTable706); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_RPAREN.add(RPAREN44);



                    // AST REWRITE
                    // elements: joinedTables
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 232:3: -> ^( TABLE joinedTables )
                    {
                        // W:\\workspace-cmis\\ANTLR\\CMIS.g:232:6: ^( TABLE joinedTables )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(TABLE, "TABLE"), root_1);

                        adaptor.addChild(root_1, stream_joinedTables.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;retval.tree = root_0;}
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
    // $ANTLR end singleTable

    public static class joinedTable_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start joinedTable
    // W:\\workspace-cmis\\ANTLR\\CMIS.g:235:1: joinedTable : ( joinType )? JOIN tableReference ( ( joinSpecification )=> joinSpecification )? -> ^( JOIN tableReference ( joinType )? ( joinSpecification )? ) ;
    public final CMISParser.joinedTable_return joinedTable() throws RecognitionException {
        CMISParser.joinedTable_return retval = new CMISParser.joinedTable_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token JOIN46=null;
        CMISParser.joinType_return joinType45 = null;

        CMISParser.tableReference_return tableReference47 = null;

        CMISParser.joinSpecification_return joinSpecification48 = null;


        Object JOIN46_tree=null;
        RewriteRuleTokenStream stream_JOIN=new RewriteRuleTokenStream(adaptor,"token JOIN");
        RewriteRuleSubtreeStream stream_tableReference=new RewriteRuleSubtreeStream(adaptor,"rule tableReference");
        RewriteRuleSubtreeStream stream_joinType=new RewriteRuleSubtreeStream(adaptor,"rule joinType");
        RewriteRuleSubtreeStream stream_joinSpecification=new RewriteRuleSubtreeStream(adaptor,"rule joinSpecification");
        try {
            // W:\\workspace-cmis\\ANTLR\\CMIS.g:236:2: ( ( joinType )? JOIN tableReference ( ( joinSpecification )=> joinSpecification )? -> ^( JOIN tableReference ( joinType )? ( joinSpecification )? ) )
            // W:\\workspace-cmis\\ANTLR\\CMIS.g:236:4: ( joinType )? JOIN tableReference ( ( joinSpecification )=> joinSpecification )?
            {
            // W:\\workspace-cmis\\ANTLR\\CMIS.g:236:4: ( joinType )?
            int alt18=2;
            int LA18_0 = input.LA(1);

            if ( ((LA18_0>=INNER && LA18_0<=LEFT)) ) {
                alt18=1;
            }
            switch (alt18) {
                case 1 :
                    // W:\\workspace-cmis\\ANTLR\\CMIS.g:236:4: joinType
                    {
                    pushFollow(FOLLOW_joinType_in_joinedTable728);
                    joinType45=joinType();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_joinType.add(joinType45.getTree());

                    }
                    break;

            }

            JOIN46=(Token)match(input,JOIN,FOLLOW_JOIN_in_joinedTable731); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_JOIN.add(JOIN46);

            pushFollow(FOLLOW_tableReference_in_joinedTable733);
            tableReference47=tableReference();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_tableReference.add(tableReference47.getTree());
            // W:\\workspace-cmis\\ANTLR\\CMIS.g:236:34: ( ( joinSpecification )=> joinSpecification )?
            int alt19=2;
            int LA19_0 = input.LA(1);

            if ( (LA19_0==ON) ) {
                int LA19_1 = input.LA(2);

                if ( (synpred4_CMIS()) ) {
                    alt19=1;
                }
            }
            switch (alt19) {
                case 1 :
                    // W:\\workspace-cmis\\ANTLR\\CMIS.g:236:35: ( joinSpecification )=> joinSpecification
                    {
                    pushFollow(FOLLOW_joinSpecification_in_joinedTable742);
                    joinSpecification48=joinSpecification();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_joinSpecification.add(joinSpecification48.getTree());

                    }
                    break;

            }



            // AST REWRITE
            // elements: joinSpecification, tableReference, JOIN, joinType
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 237:3: -> ^( JOIN tableReference ( joinType )? ( joinSpecification )? )
            {
                // W:\\workspace-cmis\\ANTLR\\CMIS.g:237:6: ^( JOIN tableReference ( joinType )? ( joinSpecification )? )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot(stream_JOIN.nextNode(), root_1);

                adaptor.addChild(root_1, stream_tableReference.nextTree());
                // W:\\workspace-cmis\\ANTLR\\CMIS.g:237:28: ( joinType )?
                if ( stream_joinType.hasNext() ) {
                    adaptor.addChild(root_1, stream_joinType.nextTree());

                }
                stream_joinType.reset();
                // W:\\workspace-cmis\\ANTLR\\CMIS.g:237:38: ( joinSpecification )?
                if ( stream_joinSpecification.hasNext() ) {
                    adaptor.addChild(root_1, stream_joinSpecification.nextTree());

                }
                stream_joinSpecification.reset();

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;retval.tree = root_0;}
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
    // $ANTLR end joinedTable

    public static class joinedTables_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start joinedTables
    // W:\\workspace-cmis\\ANTLR\\CMIS.g:241:1: joinedTables : singleTable ( joinedTable )+ -> ^( SOURCE singleTable ( joinedTable )+ ) ;
    public final CMISParser.joinedTables_return joinedTables() throws RecognitionException {
        CMISParser.joinedTables_return retval = new CMISParser.joinedTables_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        CMISParser.singleTable_return singleTable49 = null;

        CMISParser.joinedTable_return joinedTable50 = null;


        RewriteRuleSubtreeStream stream_singleTable=new RewriteRuleSubtreeStream(adaptor,"rule singleTable");
        RewriteRuleSubtreeStream stream_joinedTable=new RewriteRuleSubtreeStream(adaptor,"rule joinedTable");
        try {
            // W:\\workspace-cmis\\ANTLR\\CMIS.g:242:2: ( singleTable ( joinedTable )+ -> ^( SOURCE singleTable ( joinedTable )+ ) )
            // W:\\workspace-cmis\\ANTLR\\CMIS.g:242:4: singleTable ( joinedTable )+
            {
            pushFollow(FOLLOW_singleTable_in_joinedTables773);
            singleTable49=singleTable();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_singleTable.add(singleTable49.getTree());
            // W:\\workspace-cmis\\ANTLR\\CMIS.g:242:16: ( joinedTable )+
            int cnt20=0;
            loop20:
            do {
                int alt20=2;
                int LA20_0 = input.LA(1);

                if ( ((LA20_0>=JOIN && LA20_0<=LEFT)) ) {
                    alt20=1;
                }


                switch (alt20) {
            	case 1 :
            	    // W:\\workspace-cmis\\ANTLR\\CMIS.g:242:16: joinedTable
            	    {
            	    pushFollow(FOLLOW_joinedTable_in_joinedTables775);
            	    joinedTable50=joinedTable();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_joinedTable.add(joinedTable50.getTree());

            	    }
            	    break;

            	default :
            	    if ( cnt20 >= 1 ) break loop20;
            	    if (state.backtracking>0) {state.failed=true; return retval;}
                        EarlyExitException eee =
                            new EarlyExitException(20, input);
                        throw eee;
                }
                cnt20++;
            } while (true);



            // AST REWRITE
            // elements: joinedTable, singleTable
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 243:3: -> ^( SOURCE singleTable ( joinedTable )+ )
            {
                // W:\\workspace-cmis\\ANTLR\\CMIS.g:243:6: ^( SOURCE singleTable ( joinedTable )+ )
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

            retval.tree = root_0;retval.tree = root_0;}
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
    // $ANTLR end joinedTables

    public static class joinType_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start joinType
    // W:\\workspace-cmis\\ANTLR\\CMIS.g:246:1: joinType : ( INNER -> INNER | LEFT ( OUTER )? -> LEFT );
    public final CMISParser.joinType_return joinType() throws RecognitionException {
        CMISParser.joinType_return retval = new CMISParser.joinType_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token INNER51=null;
        Token LEFT52=null;
        Token OUTER53=null;

        Object INNER51_tree=null;
        Object LEFT52_tree=null;
        Object OUTER53_tree=null;
        RewriteRuleTokenStream stream_OUTER=new RewriteRuleTokenStream(adaptor,"token OUTER");
        RewriteRuleTokenStream stream_INNER=new RewriteRuleTokenStream(adaptor,"token INNER");
        RewriteRuleTokenStream stream_LEFT=new RewriteRuleTokenStream(adaptor,"token LEFT");

        try {
            // W:\\workspace-cmis\\ANTLR\\CMIS.g:247:2: ( INNER -> INNER | LEFT ( OUTER )? -> LEFT )
            int alt22=2;
            int LA22_0 = input.LA(1);

            if ( (LA22_0==INNER) ) {
                alt22=1;
            }
            else if ( (LA22_0==LEFT) ) {
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
                    // W:\\workspace-cmis\\ANTLR\\CMIS.g:247:4: INNER
                    {
                    INNER51=(Token)match(input,INNER,FOLLOW_INNER_in_joinType802); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_INNER.add(INNER51);



                    // AST REWRITE
                    // elements: INNER
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 248:3: -> INNER
                    {
                        adaptor.addChild(root_0, stream_INNER.nextNode());

                    }

                    retval.tree = root_0;retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // W:\\workspace-cmis\\ANTLR\\CMIS.g:249:5: LEFT ( OUTER )?
                    {
                    LEFT52=(Token)match(input,LEFT,FOLLOW_LEFT_in_joinType814); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_LEFT.add(LEFT52);

                    // W:\\workspace-cmis\\ANTLR\\CMIS.g:249:10: ( OUTER )?
                    int alt21=2;
                    int LA21_0 = input.LA(1);

                    if ( (LA21_0==OUTER) ) {
                        alt21=1;
                    }
                    switch (alt21) {
                        case 1 :
                            // W:\\workspace-cmis\\ANTLR\\CMIS.g:249:10: OUTER
                            {
                            OUTER53=(Token)match(input,OUTER,FOLLOW_OUTER_in_joinType816); if (state.failed) return retval; 
                            if ( state.backtracking==0 ) stream_OUTER.add(OUTER53);


                            }
                            break;

                    }



                    // AST REWRITE
                    // elements: LEFT
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 250:3: -> LEFT
                    {
                        adaptor.addChild(root_0, stream_LEFT.nextNode());

                    }

                    retval.tree = root_0;retval.tree = root_0;}
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
    // $ANTLR end joinType

    public static class joinSpecification_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start joinSpecification
    // W:\\workspace-cmis\\ANTLR\\CMIS.g:253:1: joinSpecification : ON LPAREN lhs= columnReference EQUALS rhs= columnReference RPAREN -> ^( ON $lhs EQUALS $rhs) ;
    public final CMISParser.joinSpecification_return joinSpecification() throws RecognitionException {
        CMISParser.joinSpecification_return retval = new CMISParser.joinSpecification_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token ON54=null;
        Token LPAREN55=null;
        Token EQUALS56=null;
        Token RPAREN57=null;
        CMISParser.columnReference_return lhs = null;

        CMISParser.columnReference_return rhs = null;


        Object ON54_tree=null;
        Object LPAREN55_tree=null;
        Object EQUALS56_tree=null;
        Object RPAREN57_tree=null;
        RewriteRuleTokenStream stream_ON=new RewriteRuleTokenStream(adaptor,"token ON");
        RewriteRuleTokenStream stream_RPAREN=new RewriteRuleTokenStream(adaptor,"token RPAREN");
        RewriteRuleTokenStream stream_EQUALS=new RewriteRuleTokenStream(adaptor,"token EQUALS");
        RewriteRuleTokenStream stream_LPAREN=new RewriteRuleTokenStream(adaptor,"token LPAREN");
        RewriteRuleSubtreeStream stream_columnReference=new RewriteRuleSubtreeStream(adaptor,"rule columnReference");
        try {
            // W:\\workspace-cmis\\ANTLR\\CMIS.g:254:2: ( ON LPAREN lhs= columnReference EQUALS rhs= columnReference RPAREN -> ^( ON $lhs EQUALS $rhs) )
            // W:\\workspace-cmis\\ANTLR\\CMIS.g:254:4: ON LPAREN lhs= columnReference EQUALS rhs= columnReference RPAREN
            {
            ON54=(Token)match(input,ON,FOLLOW_ON_in_joinSpecification836); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_ON.add(ON54);

            LPAREN55=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_joinSpecification838); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_LPAREN.add(LPAREN55);

            pushFollow(FOLLOW_columnReference_in_joinSpecification842);
            lhs=columnReference();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_columnReference.add(lhs.getTree());
            EQUALS56=(Token)match(input,EQUALS,FOLLOW_EQUALS_in_joinSpecification844); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_EQUALS.add(EQUALS56);

            pushFollow(FOLLOW_columnReference_in_joinSpecification848);
            rhs=columnReference();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_columnReference.add(rhs.getTree());
            RPAREN57=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_joinSpecification850); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_RPAREN.add(RPAREN57);



            // AST REWRITE
            // elements: rhs, EQUALS, lhs, ON
            // token labels: 
            // rule labels: retval, rhs, lhs
            // token list labels: 
            // rule list labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);
            RewriteRuleSubtreeStream stream_rhs=new RewriteRuleSubtreeStream(adaptor,"token rhs",rhs!=null?rhs.tree:null);
            RewriteRuleSubtreeStream stream_lhs=new RewriteRuleSubtreeStream(adaptor,"token lhs",lhs!=null?lhs.tree:null);

            root_0 = (Object)adaptor.nil();
            // 255:3: -> ^( ON $lhs EQUALS $rhs)
            {
                // W:\\workspace-cmis\\ANTLR\\CMIS.g:255:6: ^( ON $lhs EQUALS $rhs)
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot(stream_ON.nextNode(), root_1);

                adaptor.addChild(root_1, stream_lhs.nextTree());
                adaptor.addChild(root_1, stream_EQUALS.nextNode());
                adaptor.addChild(root_1, stream_rhs.nextTree());

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;retval.tree = root_0;}
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
    // $ANTLR end joinSpecification

    public static class whereClause_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start whereClause
    // W:\\workspace-cmis\\ANTLR\\CMIS.g:262:1: whereClause : WHERE searchOrCondition -> searchOrCondition ;
    public final CMISParser.whereClause_return whereClause() throws RecognitionException {
        CMISParser.whereClause_return retval = new CMISParser.whereClause_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token WHERE58=null;
        CMISParser.searchOrCondition_return searchOrCondition59 = null;


        Object WHERE58_tree=null;
        RewriteRuleTokenStream stream_WHERE=new RewriteRuleTokenStream(adaptor,"token WHERE");
        RewriteRuleSubtreeStream stream_searchOrCondition=new RewriteRuleSubtreeStream(adaptor,"rule searchOrCondition");
            paraphrases.push("in where"); 
        try {
            // W:\\workspace-cmis\\ANTLR\\CMIS.g:265:2: ( WHERE searchOrCondition -> searchOrCondition )
            // W:\\workspace-cmis\\ANTLR\\CMIS.g:265:4: WHERE searchOrCondition
            {
            WHERE58=(Token)match(input,WHERE,FOLLOW_WHERE_in_whereClause900); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_WHERE.add(WHERE58);

            pushFollow(FOLLOW_searchOrCondition_in_whereClause902);
            searchOrCondition59=searchOrCondition();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_searchOrCondition.add(searchOrCondition59.getTree());


            // AST REWRITE
            // elements: searchOrCondition
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 266:3: -> searchOrCondition
            {
                adaptor.addChild(root_0, stream_searchOrCondition.nextTree());

            }

            retval.tree = root_0;retval.tree = root_0;}
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
           throw e;
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end whereClause

    public static class searchOrCondition_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start searchOrCondition
    // W:\\workspace-cmis\\ANTLR\\CMIS.g:269:1: searchOrCondition : searchAndCondition ( OR searchAndCondition )* -> ^( DISJUNCTION ( searchAndCondition )+ ) ;
    public final CMISParser.searchOrCondition_return searchOrCondition() throws RecognitionException {
        CMISParser.searchOrCondition_return retval = new CMISParser.searchOrCondition_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token OR61=null;
        CMISParser.searchAndCondition_return searchAndCondition60 = null;

        CMISParser.searchAndCondition_return searchAndCondition62 = null;


        Object OR61_tree=null;
        RewriteRuleTokenStream stream_OR=new RewriteRuleTokenStream(adaptor,"token OR");
        RewriteRuleSubtreeStream stream_searchAndCondition=new RewriteRuleSubtreeStream(adaptor,"rule searchAndCondition");
        try {
            // W:\\workspace-cmis\\ANTLR\\CMIS.g:273:2: ( searchAndCondition ( OR searchAndCondition )* -> ^( DISJUNCTION ( searchAndCondition )+ ) )
            // W:\\workspace-cmis\\ANTLR\\CMIS.g:273:4: searchAndCondition ( OR searchAndCondition )*
            {
            pushFollow(FOLLOW_searchAndCondition_in_searchOrCondition922);
            searchAndCondition60=searchAndCondition();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_searchAndCondition.add(searchAndCondition60.getTree());
            // W:\\workspace-cmis\\ANTLR\\CMIS.g:273:23: ( OR searchAndCondition )*
            loop23:
            do {
                int alt23=2;
                int LA23_0 = input.LA(1);

                if ( (LA23_0==OR) ) {
                    alt23=1;
                }


                switch (alt23) {
            	case 1 :
            	    // W:\\workspace-cmis\\ANTLR\\CMIS.g:273:24: OR searchAndCondition
            	    {
            	    OR61=(Token)match(input,OR,FOLLOW_OR_in_searchOrCondition925); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_OR.add(OR61);

            	    pushFollow(FOLLOW_searchAndCondition_in_searchOrCondition927);
            	    searchAndCondition62=searchAndCondition();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_searchAndCondition.add(searchAndCondition62.getTree());

            	    }
            	    break;

            	default :
            	    break loop23;
                }
            } while (true);



            // AST REWRITE
            // elements: searchAndCondition
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 274:3: -> ^( DISJUNCTION ( searchAndCondition )+ )
            {
                // W:\\workspace-cmis\\ANTLR\\CMIS.g:274:6: ^( DISJUNCTION ( searchAndCondition )+ )
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

            retval.tree = root_0;retval.tree = root_0;}
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
    // $ANTLR end searchOrCondition

    public static class searchAndCondition_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start searchAndCondition
    // W:\\workspace-cmis\\ANTLR\\CMIS.g:278:1: searchAndCondition : searchNotCondition ( AND searchNotCondition )* -> ^( CONJUNCTION ( searchNotCondition )+ ) ;
    public final CMISParser.searchAndCondition_return searchAndCondition() throws RecognitionException {
        CMISParser.searchAndCondition_return retval = new CMISParser.searchAndCondition_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token AND64=null;
        CMISParser.searchNotCondition_return searchNotCondition63 = null;

        CMISParser.searchNotCondition_return searchNotCondition65 = null;


        Object AND64_tree=null;
        RewriteRuleTokenStream stream_AND=new RewriteRuleTokenStream(adaptor,"token AND");
        RewriteRuleSubtreeStream stream_searchNotCondition=new RewriteRuleSubtreeStream(adaptor,"rule searchNotCondition");
        try {
            // W:\\workspace-cmis\\ANTLR\\CMIS.g:282:2: ( searchNotCondition ( AND searchNotCondition )* -> ^( CONJUNCTION ( searchNotCondition )+ ) )
            // W:\\workspace-cmis\\ANTLR\\CMIS.g:282:4: searchNotCondition ( AND searchNotCondition )*
            {
            pushFollow(FOLLOW_searchNotCondition_in_searchAndCondition955);
            searchNotCondition63=searchNotCondition();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_searchNotCondition.add(searchNotCondition63.getTree());
            // W:\\workspace-cmis\\ANTLR\\CMIS.g:282:23: ( AND searchNotCondition )*
            loop24:
            do {
                int alt24=2;
                int LA24_0 = input.LA(1);

                if ( (LA24_0==AND) ) {
                    alt24=1;
                }


                switch (alt24) {
            	case 1 :
            	    // W:\\workspace-cmis\\ANTLR\\CMIS.g:282:24: AND searchNotCondition
            	    {
            	    AND64=(Token)match(input,AND,FOLLOW_AND_in_searchAndCondition958); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_AND.add(AND64);

            	    pushFollow(FOLLOW_searchNotCondition_in_searchAndCondition960);
            	    searchNotCondition65=searchNotCondition();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_searchNotCondition.add(searchNotCondition65.getTree());

            	    }
            	    break;

            	default :
            	    break loop24;
                }
            } while (true);



            // AST REWRITE
            // elements: searchNotCondition
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 283:3: -> ^( CONJUNCTION ( searchNotCondition )+ )
            {
                // W:\\workspace-cmis\\ANTLR\\CMIS.g:283:6: ^( CONJUNCTION ( searchNotCondition )+ )
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

            retval.tree = root_0;retval.tree = root_0;}
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
    // $ANTLR end searchAndCondition

    public static class searchNotCondition_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start searchNotCondition
    // W:\\workspace-cmis\\ANTLR\\CMIS.g:286:1: searchNotCondition : ( NOT searchTest -> ^( NEGATION searchTest ) | searchTest -> searchTest );
    public final CMISParser.searchNotCondition_return searchNotCondition() throws RecognitionException {
        CMISParser.searchNotCondition_return retval = new CMISParser.searchNotCondition_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token NOT66=null;
        CMISParser.searchTest_return searchTest67 = null;

        CMISParser.searchTest_return searchTest68 = null;


        Object NOT66_tree=null;
        RewriteRuleTokenStream stream_NOT=new RewriteRuleTokenStream(adaptor,"token NOT");
        RewriteRuleSubtreeStream stream_searchTest=new RewriteRuleSubtreeStream(adaptor,"rule searchTest");
        try {
            // W:\\workspace-cmis\\ANTLR\\CMIS.g:287:2: ( NOT searchTest -> ^( NEGATION searchTest ) | searchTest -> searchTest )
            int alt25=2;
            alt25 = dfa25.predict(input);
            switch (alt25) {
                case 1 :
                    // W:\\workspace-cmis\\ANTLR\\CMIS.g:287:4: NOT searchTest
                    {
                    NOT66=(Token)match(input,NOT,FOLLOW_NOT_in_searchNotCondition987); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_NOT.add(NOT66);

                    pushFollow(FOLLOW_searchTest_in_searchNotCondition989);
                    searchTest67=searchTest();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_searchTest.add(searchTest67.getTree());


                    // AST REWRITE
                    // elements: searchTest
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 288:3: -> ^( NEGATION searchTest )
                    {
                        // W:\\workspace-cmis\\ANTLR\\CMIS.g:288:6: ^( NEGATION searchTest )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(NEGATION, "NEGATION"), root_1);

                        adaptor.addChild(root_1, stream_searchTest.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // W:\\workspace-cmis\\ANTLR\\CMIS.g:289:4: searchTest
                    {
                    pushFollow(FOLLOW_searchTest_in_searchNotCondition1004);
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
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 290:3: -> searchTest
                    {
                        adaptor.addChild(root_0, stream_searchTest.nextTree());

                    }

                    retval.tree = root_0;retval.tree = root_0;}
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
    // $ANTLR end searchNotCondition

    public static class searchTest_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start searchTest
    // W:\\workspace-cmis\\ANTLR\\CMIS.g:293:1: searchTest : ( predicate -> predicate | LPAREN searchOrCondition RPAREN -> searchOrCondition );
    public final CMISParser.searchTest_return searchTest() throws RecognitionException {
        CMISParser.searchTest_return retval = new CMISParser.searchTest_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token LPAREN70=null;
        Token RPAREN72=null;
        CMISParser.predicate_return predicate69 = null;

        CMISParser.searchOrCondition_return searchOrCondition71 = null;


        Object LPAREN70_tree=null;
        Object RPAREN72_tree=null;
        RewriteRuleTokenStream stream_RPAREN=new RewriteRuleTokenStream(adaptor,"token RPAREN");
        RewriteRuleTokenStream stream_LPAREN=new RewriteRuleTokenStream(adaptor,"token LPAREN");
        RewriteRuleSubtreeStream stream_predicate=new RewriteRuleSubtreeStream(adaptor,"rule predicate");
        RewriteRuleSubtreeStream stream_searchOrCondition=new RewriteRuleSubtreeStream(adaptor,"rule searchOrCondition");
        try {
            // W:\\workspace-cmis\\ANTLR\\CMIS.g:294:2: ( predicate -> predicate | LPAREN searchOrCondition RPAREN -> searchOrCondition )
            int alt26=2;
            int LA26_0 = input.LA(1);

            if ( (LA26_0==SELECT||LA26_0==AS||(LA26_0>=FROM && LA26_0<=ON)||(LA26_0>=WHERE && LA26_0<=NOT)||(LA26_0>=IN && LA26_0<=SCORE)) ) {
                alt26=1;
            }
            else if ( (LA26_0==LPAREN) ) {
                alt26=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 26, 0, input);

                throw nvae;
            }
            switch (alt26) {
                case 1 :
                    // W:\\workspace-cmis\\ANTLR\\CMIS.g:294:4: predicate
                    {
                    pushFollow(FOLLOW_predicate_in_searchTest1022);
                    predicate69=predicate();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_predicate.add(predicate69.getTree());


                    // AST REWRITE
                    // elements: predicate
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 295:3: -> predicate
                    {
                        adaptor.addChild(root_0, stream_predicate.nextTree());

                    }

                    retval.tree = root_0;retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // W:\\workspace-cmis\\ANTLR\\CMIS.g:296:4: LPAREN searchOrCondition RPAREN
                    {
                    LPAREN70=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_searchTest1033); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_LPAREN.add(LPAREN70);

                    pushFollow(FOLLOW_searchOrCondition_in_searchTest1035);
                    searchOrCondition71=searchOrCondition();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_searchOrCondition.add(searchOrCondition71.getTree());
                    RPAREN72=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_searchTest1037); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_RPAREN.add(RPAREN72);



                    // AST REWRITE
                    // elements: searchOrCondition
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 297:3: -> searchOrCondition
                    {
                        adaptor.addChild(root_0, stream_searchOrCondition.nextTree());

                    }

                    retval.tree = root_0;retval.tree = root_0;}
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
    // $ANTLR end searchTest

    public static class predicate_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start predicate
    // W:\\workspace-cmis\\ANTLR\\CMIS.g:300:1: predicate : ( comparisonPredicate | inPredicate | likePredicate | nullPredicate | quantifiedComparisonPredicate | quantifiedInPredicate | textSearchPredicate | folderPredicate );
    public final CMISParser.predicate_return predicate() throws RecognitionException {
        CMISParser.predicate_return retval = new CMISParser.predicate_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        CMISParser.comparisonPredicate_return comparisonPredicate73 = null;

        CMISParser.inPredicate_return inPredicate74 = null;

        CMISParser.likePredicate_return likePredicate75 = null;

        CMISParser.nullPredicate_return nullPredicate76 = null;

        CMISParser.quantifiedComparisonPredicate_return quantifiedComparisonPredicate77 = null;

        CMISParser.quantifiedInPredicate_return quantifiedInPredicate78 = null;

        CMISParser.textSearchPredicate_return textSearchPredicate79 = null;

        CMISParser.folderPredicate_return folderPredicate80 = null;



        try {
            // W:\\workspace-cmis\\ANTLR\\CMIS.g:301:2: ( comparisonPredicate | inPredicate | likePredicate | nullPredicate | quantifiedComparisonPredicate | quantifiedInPredicate | textSearchPredicate | folderPredicate )
            int alt27=8;
            alt27 = dfa27.predict(input);
            switch (alt27) {
                case 1 :
                    // W:\\workspace-cmis\\ANTLR\\CMIS.g:301:4: comparisonPredicate
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_comparisonPredicate_in_predicate1054);
                    comparisonPredicate73=comparisonPredicate();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, comparisonPredicate73.getTree());

                    }
                    break;
                case 2 :
                    // W:\\workspace-cmis\\ANTLR\\CMIS.g:302:4: inPredicate
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_inPredicate_in_predicate1059);
                    inPredicate74=inPredicate();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, inPredicate74.getTree());

                    }
                    break;
                case 3 :
                    // W:\\workspace-cmis\\ANTLR\\CMIS.g:303:4: likePredicate
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_likePredicate_in_predicate1064);
                    likePredicate75=likePredicate();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, likePredicate75.getTree());

                    }
                    break;
                case 4 :
                    // W:\\workspace-cmis\\ANTLR\\CMIS.g:304:4: nullPredicate
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_nullPredicate_in_predicate1069);
                    nullPredicate76=nullPredicate();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, nullPredicate76.getTree());

                    }
                    break;
                case 5 :
                    // W:\\workspace-cmis\\ANTLR\\CMIS.g:305:5: quantifiedComparisonPredicate
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_quantifiedComparisonPredicate_in_predicate1075);
                    quantifiedComparisonPredicate77=quantifiedComparisonPredicate();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, quantifiedComparisonPredicate77.getTree());

                    }
                    break;
                case 6 :
                    // W:\\workspace-cmis\\ANTLR\\CMIS.g:306:4: quantifiedInPredicate
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_quantifiedInPredicate_in_predicate1080);
                    quantifiedInPredicate78=quantifiedInPredicate();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, quantifiedInPredicate78.getTree());

                    }
                    break;
                case 7 :
                    // W:\\workspace-cmis\\ANTLR\\CMIS.g:307:4: textSearchPredicate
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_textSearchPredicate_in_predicate1085);
                    textSearchPredicate79=textSearchPredicate();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, textSearchPredicate79.getTree());

                    }
                    break;
                case 8 :
                    // W:\\workspace-cmis\\ANTLR\\CMIS.g:308:4: folderPredicate
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_folderPredicate_in_predicate1090);
                    folderPredicate80=folderPredicate();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, folderPredicate80.getTree());

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
    // $ANTLR end predicate

    public static class comparisonPredicate_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start comparisonPredicate
    // W:\\workspace-cmis\\ANTLR\\CMIS.g:311:1: comparisonPredicate : valueExpression compOp literalOrParameterName -> ^( PRED_COMPARISON ANY valueExpression compOp literalOrParameterName ) ;
    public final CMISParser.comparisonPredicate_return comparisonPredicate() throws RecognitionException {
        CMISParser.comparisonPredicate_return retval = new CMISParser.comparisonPredicate_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        CMISParser.valueExpression_return valueExpression81 = null;

        CMISParser.compOp_return compOp82 = null;

        CMISParser.literalOrParameterName_return literalOrParameterName83 = null;


        RewriteRuleSubtreeStream stream_valueExpression=new RewriteRuleSubtreeStream(adaptor,"rule valueExpression");
        RewriteRuleSubtreeStream stream_compOp=new RewriteRuleSubtreeStream(adaptor,"rule compOp");
        RewriteRuleSubtreeStream stream_literalOrParameterName=new RewriteRuleSubtreeStream(adaptor,"rule literalOrParameterName");
        try {
            // W:\\workspace-cmis\\ANTLR\\CMIS.g:312:2: ( valueExpression compOp literalOrParameterName -> ^( PRED_COMPARISON ANY valueExpression compOp literalOrParameterName ) )
            // W:\\workspace-cmis\\ANTLR\\CMIS.g:312:4: valueExpression compOp literalOrParameterName
            {
            pushFollow(FOLLOW_valueExpression_in_comparisonPredicate1102);
            valueExpression81=valueExpression();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_valueExpression.add(valueExpression81.getTree());
            pushFollow(FOLLOW_compOp_in_comparisonPredicate1104);
            compOp82=compOp();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_compOp.add(compOp82.getTree());
            pushFollow(FOLLOW_literalOrParameterName_in_comparisonPredicate1106);
            literalOrParameterName83=literalOrParameterName();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_literalOrParameterName.add(literalOrParameterName83.getTree());


            // AST REWRITE
            // elements: valueExpression, literalOrParameterName, compOp
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 313:3: -> ^( PRED_COMPARISON ANY valueExpression compOp literalOrParameterName )
            {
                // W:\\workspace-cmis\\ANTLR\\CMIS.g:313:6: ^( PRED_COMPARISON ANY valueExpression compOp literalOrParameterName )
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

            retval.tree = root_0;retval.tree = root_0;}
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
    // $ANTLR end comparisonPredicate

    public static class compOp_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start compOp
    // W:\\workspace-cmis\\ANTLR\\CMIS.g:316:1: compOp : ( EQUALS | NOTEQUALS | LESSTHAN | GREATERTHAN | LESSTHANOREQUALS | GREATERTHANOREQUALS );
    public final CMISParser.compOp_return compOp() throws RecognitionException {
        CMISParser.compOp_return retval = new CMISParser.compOp_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token set84=null;

        Object set84_tree=null;

        try {
            // W:\\workspace-cmis\\ANTLR\\CMIS.g:317:2: ( EQUALS | NOTEQUALS | LESSTHAN | GREATERTHAN | LESSTHANOREQUALS | GREATERTHANOREQUALS )
            // W:\\workspace-cmis\\ANTLR\\CMIS.g:
            {
            root_0 = (Object)adaptor.nil();

            set84=(Token)input.LT(1);
            if ( input.LA(1)==EQUALS||(input.LA(1)>=NOTEQUALS && input.LA(1)<=GREATERTHANOREQUALS) ) {
                input.consume();
                if ( state.backtracking==0 ) adaptor.addChild(root_0, (Object)adaptor.create(set84));
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
    // $ANTLR end compOp

    public static class literalOrParameterName_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start literalOrParameterName
    // W:\\workspace-cmis\\ANTLR\\CMIS.g:325:1: literalOrParameterName : ( literal | parameterName );
    public final CMISParser.literalOrParameterName_return literalOrParameterName() throws RecognitionException {
        CMISParser.literalOrParameterName_return retval = new CMISParser.literalOrParameterName_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        CMISParser.literal_return literal85 = null;

        CMISParser.parameterName_return parameterName86 = null;



        try {
            // W:\\workspace-cmis\\ANTLR\\CMIS.g:326:2: ( literal | parameterName )
            int alt28=2;
            int LA28_0 = input.LA(1);

            if ( (LA28_0==QUOTED_STRING||(LA28_0>=FLOATING_POINT_LITERAL && LA28_0<=DECIMAL_INTEGER_LITERAL)) ) {
                alt28=1;
            }
            else if ( (LA28_0==COLON) ) {
                alt28=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 28, 0, input);

                throw nvae;
            }
            switch (alt28) {
                case 1 :
                    // W:\\workspace-cmis\\ANTLR\\CMIS.g:326:4: literal
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_literal_in_literalOrParameterName1172);
                    literal85=literal();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, literal85.getTree());

                    }
                    break;
                case 2 :
                    // W:\\workspace-cmis\\ANTLR\\CMIS.g:327:4: parameterName
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_parameterName_in_literalOrParameterName1177);
                    parameterName86=parameterName();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, parameterName86.getTree());

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
    // $ANTLR end literalOrParameterName

    public static class literal_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start literal
    // W:\\workspace-cmis\\ANTLR\\CMIS.g:330:1: literal : ( signedNumericLiteral | characterStringLiteral );
    public final CMISParser.literal_return literal() throws RecognitionException {
        CMISParser.literal_return retval = new CMISParser.literal_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        CMISParser.signedNumericLiteral_return signedNumericLiteral87 = null;

        CMISParser.characterStringLiteral_return characterStringLiteral88 = null;



        try {
            // W:\\workspace-cmis\\ANTLR\\CMIS.g:331:2: ( signedNumericLiteral | characterStringLiteral )
            int alt29=2;
            int LA29_0 = input.LA(1);

            if ( ((LA29_0>=FLOATING_POINT_LITERAL && LA29_0<=DECIMAL_INTEGER_LITERAL)) ) {
                alt29=1;
            }
            else if ( (LA29_0==QUOTED_STRING) ) {
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
                    // W:\\workspace-cmis\\ANTLR\\CMIS.g:331:4: signedNumericLiteral
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_signedNumericLiteral_in_literal1190);
                    signedNumericLiteral87=signedNumericLiteral();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, signedNumericLiteral87.getTree());

                    }
                    break;
                case 2 :
                    // W:\\workspace-cmis\\ANTLR\\CMIS.g:332:4: characterStringLiteral
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_characterStringLiteral_in_literal1195);
                    characterStringLiteral88=characterStringLiteral();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, characterStringLiteral88.getTree());

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
    // $ANTLR end literal

    public static class inPredicate_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start inPredicate
    // W:\\workspace-cmis\\ANTLR\\CMIS.g:335:1: inPredicate : columnReference ( NOT )? IN LPAREN inValueList RPAREN -> ^( PRED_IN ANY columnReference inValueList ( NOT )? ) ;
    public final CMISParser.inPredicate_return inPredicate() throws RecognitionException {
        CMISParser.inPredicate_return retval = new CMISParser.inPredicate_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token NOT90=null;
        Token IN91=null;
        Token LPAREN92=null;
        Token RPAREN94=null;
        CMISParser.columnReference_return columnReference89 = null;

        CMISParser.inValueList_return inValueList93 = null;


        Object NOT90_tree=null;
        Object IN91_tree=null;
        Object LPAREN92_tree=null;
        Object RPAREN94_tree=null;
        RewriteRuleTokenStream stream_RPAREN=new RewriteRuleTokenStream(adaptor,"token RPAREN");
        RewriteRuleTokenStream stream_IN=new RewriteRuleTokenStream(adaptor,"token IN");
        RewriteRuleTokenStream stream_NOT=new RewriteRuleTokenStream(adaptor,"token NOT");
        RewriteRuleTokenStream stream_LPAREN=new RewriteRuleTokenStream(adaptor,"token LPAREN");
        RewriteRuleSubtreeStream stream_columnReference=new RewriteRuleSubtreeStream(adaptor,"rule columnReference");
        RewriteRuleSubtreeStream stream_inValueList=new RewriteRuleSubtreeStream(adaptor,"rule inValueList");
        try {
            // W:\\workspace-cmis\\ANTLR\\CMIS.g:336:2: ( columnReference ( NOT )? IN LPAREN inValueList RPAREN -> ^( PRED_IN ANY columnReference inValueList ( NOT )? ) )
            // W:\\workspace-cmis\\ANTLR\\CMIS.g:336:4: columnReference ( NOT )? IN LPAREN inValueList RPAREN
            {
            pushFollow(FOLLOW_columnReference_in_inPredicate1207);
            columnReference89=columnReference();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_columnReference.add(columnReference89.getTree());
            // W:\\workspace-cmis\\ANTLR\\CMIS.g:336:20: ( NOT )?
            int alt30=2;
            int LA30_0 = input.LA(1);

            if ( (LA30_0==NOT) ) {
                alt30=1;
            }
            switch (alt30) {
                case 1 :
                    // W:\\workspace-cmis\\ANTLR\\CMIS.g:336:20: NOT
                    {
                    NOT90=(Token)match(input,NOT,FOLLOW_NOT_in_inPredicate1209); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_NOT.add(NOT90);


                    }
                    break;

            }

            IN91=(Token)match(input,IN,FOLLOW_IN_in_inPredicate1212); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_IN.add(IN91);

            LPAREN92=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_inPredicate1214); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_LPAREN.add(LPAREN92);

            pushFollow(FOLLOW_inValueList_in_inPredicate1216);
            inValueList93=inValueList();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_inValueList.add(inValueList93.getTree());
            RPAREN94=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_inPredicate1218); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_RPAREN.add(RPAREN94);



            // AST REWRITE
            // elements: inValueList, NOT, columnReference
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 337:3: -> ^( PRED_IN ANY columnReference inValueList ( NOT )? )
            {
                // W:\\workspace-cmis\\ANTLR\\CMIS.g:337:6: ^( PRED_IN ANY columnReference inValueList ( NOT )? )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(PRED_IN, "PRED_IN"), root_1);

                adaptor.addChild(root_1, (Object)adaptor.create(ANY, "ANY"));
                adaptor.addChild(root_1, stream_columnReference.nextTree());
                adaptor.addChild(root_1, stream_inValueList.nextTree());
                // W:\\workspace-cmis\\ANTLR\\CMIS.g:337:48: ( NOT )?
                if ( stream_NOT.hasNext() ) {
                    adaptor.addChild(root_1, stream_NOT.nextNode());

                }
                stream_NOT.reset();

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;retval.tree = root_0;}
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
    // $ANTLR end inPredicate

    public static class inValueList_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start inValueList
    // W:\\workspace-cmis\\ANTLR\\CMIS.g:340:1: inValueList : literalOrParameterName ( COMMA literalOrParameterName )* -> ^( LIST ( literalOrParameterName )+ ) ;
    public final CMISParser.inValueList_return inValueList() throws RecognitionException {
        CMISParser.inValueList_return retval = new CMISParser.inValueList_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token COMMA96=null;
        CMISParser.literalOrParameterName_return literalOrParameterName95 = null;

        CMISParser.literalOrParameterName_return literalOrParameterName97 = null;


        Object COMMA96_tree=null;
        RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
        RewriteRuleSubtreeStream stream_literalOrParameterName=new RewriteRuleSubtreeStream(adaptor,"rule literalOrParameterName");
        try {
            // W:\\workspace-cmis\\ANTLR\\CMIS.g:341:2: ( literalOrParameterName ( COMMA literalOrParameterName )* -> ^( LIST ( literalOrParameterName )+ ) )
            // W:\\workspace-cmis\\ANTLR\\CMIS.g:341:4: literalOrParameterName ( COMMA literalOrParameterName )*
            {
            pushFollow(FOLLOW_literalOrParameterName_in_inValueList1247);
            literalOrParameterName95=literalOrParameterName();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_literalOrParameterName.add(literalOrParameterName95.getTree());
            // W:\\workspace-cmis\\ANTLR\\CMIS.g:341:27: ( COMMA literalOrParameterName )*
            loop31:
            do {
                int alt31=2;
                int LA31_0 = input.LA(1);

                if ( (LA31_0==COMMA) ) {
                    alt31=1;
                }


                switch (alt31) {
            	case 1 :
            	    // W:\\workspace-cmis\\ANTLR\\CMIS.g:341:28: COMMA literalOrParameterName
            	    {
            	    COMMA96=(Token)match(input,COMMA,FOLLOW_COMMA_in_inValueList1250); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_COMMA.add(COMMA96);

            	    pushFollow(FOLLOW_literalOrParameterName_in_inValueList1252);
            	    literalOrParameterName97=literalOrParameterName();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_literalOrParameterName.add(literalOrParameterName97.getTree());

            	    }
            	    break;

            	default :
            	    break loop31;
                }
            } while (true);



            // AST REWRITE
            // elements: literalOrParameterName
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 342:3: -> ^( LIST ( literalOrParameterName )+ )
            {
                // W:\\workspace-cmis\\ANTLR\\CMIS.g:342:6: ^( LIST ( literalOrParameterName )+ )
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

            retval.tree = root_0;retval.tree = root_0;}
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
    // $ANTLR end inValueList

    public static class likePredicate_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start likePredicate
    // W:\\workspace-cmis\\ANTLR\\CMIS.g:345:1: likePredicate : columnReference ( NOT )? LIKE characterStringLiteral -> ^( PRED_LIKE columnReference characterStringLiteral ( NOT )? ) ;
    public final CMISParser.likePredicate_return likePredicate() throws RecognitionException {
        CMISParser.likePredicate_return retval = new CMISParser.likePredicate_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token NOT99=null;
        Token LIKE100=null;
        CMISParser.columnReference_return columnReference98 = null;

        CMISParser.characterStringLiteral_return characterStringLiteral101 = null;


        Object NOT99_tree=null;
        Object LIKE100_tree=null;
        RewriteRuleTokenStream stream_NOT=new RewriteRuleTokenStream(adaptor,"token NOT");
        RewriteRuleTokenStream stream_LIKE=new RewriteRuleTokenStream(adaptor,"token LIKE");
        RewriteRuleSubtreeStream stream_columnReference=new RewriteRuleSubtreeStream(adaptor,"rule columnReference");
        RewriteRuleSubtreeStream stream_characterStringLiteral=new RewriteRuleSubtreeStream(adaptor,"rule characterStringLiteral");
        try {
            // W:\\workspace-cmis\\ANTLR\\CMIS.g:346:2: ( columnReference ( NOT )? LIKE characterStringLiteral -> ^( PRED_LIKE columnReference characterStringLiteral ( NOT )? ) )
            // W:\\workspace-cmis\\ANTLR\\CMIS.g:346:4: columnReference ( NOT )? LIKE characterStringLiteral
            {
            pushFollow(FOLLOW_columnReference_in_likePredicate1278);
            columnReference98=columnReference();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_columnReference.add(columnReference98.getTree());
            // W:\\workspace-cmis\\ANTLR\\CMIS.g:346:20: ( NOT )?
            int alt32=2;
            int LA32_0 = input.LA(1);

            if ( (LA32_0==NOT) ) {
                alt32=1;
            }
            switch (alt32) {
                case 1 :
                    // W:\\workspace-cmis\\ANTLR\\CMIS.g:346:20: NOT
                    {
                    NOT99=(Token)match(input,NOT,FOLLOW_NOT_in_likePredicate1280); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_NOT.add(NOT99);


                    }
                    break;

            }

            LIKE100=(Token)match(input,LIKE,FOLLOW_LIKE_in_likePredicate1283); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_LIKE.add(LIKE100);

            pushFollow(FOLLOW_characterStringLiteral_in_likePredicate1285);
            characterStringLiteral101=characterStringLiteral();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_characterStringLiteral.add(characterStringLiteral101.getTree());


            // AST REWRITE
            // elements: characterStringLiteral, NOT, columnReference
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 347:3: -> ^( PRED_LIKE columnReference characterStringLiteral ( NOT )? )
            {
                // W:\\workspace-cmis\\ANTLR\\CMIS.g:347:6: ^( PRED_LIKE columnReference characterStringLiteral ( NOT )? )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(PRED_LIKE, "PRED_LIKE"), root_1);

                adaptor.addChild(root_1, stream_columnReference.nextTree());
                adaptor.addChild(root_1, stream_characterStringLiteral.nextTree());
                // W:\\workspace-cmis\\ANTLR\\CMIS.g:347:57: ( NOT )?
                if ( stream_NOT.hasNext() ) {
                    adaptor.addChild(root_1, stream_NOT.nextNode());

                }
                stream_NOT.reset();

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;retval.tree = root_0;}
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
    // $ANTLR end likePredicate

    public static class nullPredicate_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start nullPredicate
    // W:\\workspace-cmis\\ANTLR\\CMIS.g:350:1: nullPredicate : ( ( columnReference )=> columnReference | multiValuedColumnReference ) IS ( NOT )? NULL -> ^( PRED_EXISTS columnReference ( NOT )? ) ;
    public final CMISParser.nullPredicate_return nullPredicate() throws RecognitionException {
        CMISParser.nullPredicate_return retval = new CMISParser.nullPredicate_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token IS104=null;
        Token NOT105=null;
        Token NULL106=null;
        CMISParser.columnReference_return columnReference102 = null;

        CMISParser.multiValuedColumnReference_return multiValuedColumnReference103 = null;


        Object IS104_tree=null;
        Object NOT105_tree=null;
        Object NULL106_tree=null;
        RewriteRuleTokenStream stream_NOT=new RewriteRuleTokenStream(adaptor,"token NOT");
        RewriteRuleTokenStream stream_IS=new RewriteRuleTokenStream(adaptor,"token IS");
        RewriteRuleTokenStream stream_NULL=new RewriteRuleTokenStream(adaptor,"token NULL");
        RewriteRuleSubtreeStream stream_columnReference=new RewriteRuleSubtreeStream(adaptor,"rule columnReference");
        RewriteRuleSubtreeStream stream_multiValuedColumnReference=new RewriteRuleSubtreeStream(adaptor,"rule multiValuedColumnReference");
        try {
            // W:\\workspace-cmis\\ANTLR\\CMIS.g:351:2: ( ( ( columnReference )=> columnReference | multiValuedColumnReference ) IS ( NOT )? NULL -> ^( PRED_EXISTS columnReference ( NOT )? ) )
            // W:\\workspace-cmis\\ANTLR\\CMIS.g:351:4: ( ( columnReference )=> columnReference | multiValuedColumnReference ) IS ( NOT )? NULL
            {
            // W:\\workspace-cmis\\ANTLR\\CMIS.g:351:4: ( ( columnReference )=> columnReference | multiValuedColumnReference )
            int alt33=2;
            int LA33_0 = input.LA(1);

            if ( (LA33_0==ID) ) {
                int LA33_1 = input.LA(2);

                if ( (synpred5_CMIS()) ) {
                    alt33=1;
                }
                else if ( (true) ) {
                    alt33=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 33, 1, input);

                    throw nvae;
                }
            }
            else if ( (LA33_0==DOUBLE_QUOTE) ) {
                int LA33_2 = input.LA(2);

                if ( (LA33_2==SELECT||LA33_2==AS||(LA33_2>=FROM && LA33_2<=ON)||(LA33_2>=WHERE && LA33_2<=NOT)||(LA33_2>=IN && LA33_2<=DESC)||(LA33_2>=UPPER && LA33_2<=SCORE)) ) {
                    int LA33_5 = input.LA(3);

                    if ( (LA33_5==DOUBLE_QUOTE) ) {
                        int LA33_7 = input.LA(4);

                        if ( (synpred5_CMIS()) ) {
                            alt33=1;
                        }
                        else if ( (true) ) {
                            alt33=2;
                        }
                        else {
                            if (state.backtracking>0) {state.failed=true; return retval;}
                            NoViableAltException nvae =
                                new NoViableAltException("", 33, 7, input);

                            throw nvae;
                        }
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return retval;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 33, 5, input);

                        throw nvae;
                    }
                }
                else if ( (LA33_2==ID) ) {
                    int LA33_6 = input.LA(3);

                    if ( (LA33_6==DOUBLE_QUOTE) ) {
                        int LA33_7 = input.LA(4);

                        if ( (synpred5_CMIS()) ) {
                            alt33=1;
                        }
                        else if ( (true) ) {
                            alt33=2;
                        }
                        else {
                            if (state.backtracking>0) {state.failed=true; return retval;}
                            NoViableAltException nvae =
                                new NoViableAltException("", 33, 7, input);

                            throw nvae;
                        }
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return retval;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 33, 6, input);

                        throw nvae;
                    }
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 33, 2, input);

                    throw nvae;
                }
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 33, 0, input);

                throw nvae;
            }
            switch (alt33) {
                case 1 :
                    // W:\\workspace-cmis\\ANTLR\\CMIS.g:351:6: ( columnReference )=> columnReference
                    {
                    pushFollow(FOLLOW_columnReference_in_nullPredicate1319);
                    columnReference102=columnReference();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_columnReference.add(columnReference102.getTree());

                    }
                    break;
                case 2 :
                    // W:\\workspace-cmis\\ANTLR\\CMIS.g:351:44: multiValuedColumnReference
                    {
                    pushFollow(FOLLOW_multiValuedColumnReference_in_nullPredicate1323);
                    multiValuedColumnReference103=multiValuedColumnReference();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_multiValuedColumnReference.add(multiValuedColumnReference103.getTree());

                    }
                    break;

            }

            IS104=(Token)match(input,IS,FOLLOW_IS_in_nullPredicate1326); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_IS.add(IS104);

            // W:\\workspace-cmis\\ANTLR\\CMIS.g:351:75: ( NOT )?
            int alt34=2;
            int LA34_0 = input.LA(1);

            if ( (LA34_0==NOT) ) {
                alt34=1;
            }
            switch (alt34) {
                case 1 :
                    // W:\\workspace-cmis\\ANTLR\\CMIS.g:351:75: NOT
                    {
                    NOT105=(Token)match(input,NOT,FOLLOW_NOT_in_nullPredicate1328); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_NOT.add(NOT105);


                    }
                    break;

            }

            NULL106=(Token)match(input,NULL,FOLLOW_NULL_in_nullPredicate1331); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_NULL.add(NULL106);



            // AST REWRITE
            // elements: columnReference, NOT
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 352:3: -> ^( PRED_EXISTS columnReference ( NOT )? )
            {
                // W:\\workspace-cmis\\ANTLR\\CMIS.g:352:6: ^( PRED_EXISTS columnReference ( NOT )? )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(PRED_EXISTS, "PRED_EXISTS"), root_1);

                adaptor.addChild(root_1, stream_columnReference.nextTree());
                // W:\\workspace-cmis\\ANTLR\\CMIS.g:352:36: ( NOT )?
                if ( stream_NOT.hasNext() ) {
                    adaptor.addChild(root_1, stream_NOT.nextNode());

                }
                stream_NOT.reset();

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;retval.tree = root_0;}
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
    // $ANTLR end nullPredicate

    public static class quantifiedComparisonPredicate_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start quantifiedComparisonPredicate
    // W:\\workspace-cmis\\ANTLR\\CMIS.g:355:1: quantifiedComparisonPredicate : literalOrParameterName compOp ANY multiValuedColumnReference -> ^( PRED_COMPARISON ANY literalOrParameterName compOp multiValuedColumnReference ) ;
    public final CMISParser.quantifiedComparisonPredicate_return quantifiedComparisonPredicate() throws RecognitionException {
        CMISParser.quantifiedComparisonPredicate_return retval = new CMISParser.quantifiedComparisonPredicate_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token ANY109=null;
        CMISParser.literalOrParameterName_return literalOrParameterName107 = null;

        CMISParser.compOp_return compOp108 = null;

        CMISParser.multiValuedColumnReference_return multiValuedColumnReference110 = null;


        Object ANY109_tree=null;
        RewriteRuleTokenStream stream_ANY=new RewriteRuleTokenStream(adaptor,"token ANY");
        RewriteRuleSubtreeStream stream_compOp=new RewriteRuleSubtreeStream(adaptor,"rule compOp");
        RewriteRuleSubtreeStream stream_literalOrParameterName=new RewriteRuleSubtreeStream(adaptor,"rule literalOrParameterName");
        RewriteRuleSubtreeStream stream_multiValuedColumnReference=new RewriteRuleSubtreeStream(adaptor,"rule multiValuedColumnReference");
        try {
            // W:\\workspace-cmis\\ANTLR\\CMIS.g:356:2: ( literalOrParameterName compOp ANY multiValuedColumnReference -> ^( PRED_COMPARISON ANY literalOrParameterName compOp multiValuedColumnReference ) )
            // W:\\workspace-cmis\\ANTLR\\CMIS.g:356:4: literalOrParameterName compOp ANY multiValuedColumnReference
            {
            pushFollow(FOLLOW_literalOrParameterName_in_quantifiedComparisonPredicate1356);
            literalOrParameterName107=literalOrParameterName();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_literalOrParameterName.add(literalOrParameterName107.getTree());
            pushFollow(FOLLOW_compOp_in_quantifiedComparisonPredicate1358);
            compOp108=compOp();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_compOp.add(compOp108.getTree());
            ANY109=(Token)match(input,ANY,FOLLOW_ANY_in_quantifiedComparisonPredicate1360); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_ANY.add(ANY109);

            pushFollow(FOLLOW_multiValuedColumnReference_in_quantifiedComparisonPredicate1362);
            multiValuedColumnReference110=multiValuedColumnReference();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_multiValuedColumnReference.add(multiValuedColumnReference110.getTree());


            // AST REWRITE
            // elements: ANY, literalOrParameterName, multiValuedColumnReference, compOp
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 357:2: -> ^( PRED_COMPARISON ANY literalOrParameterName compOp multiValuedColumnReference )
            {
                // W:\\workspace-cmis\\ANTLR\\CMIS.g:357:5: ^( PRED_COMPARISON ANY literalOrParameterName compOp multiValuedColumnReference )
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

            retval.tree = root_0;retval.tree = root_0;}
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
    // $ANTLR end quantifiedComparisonPredicate

    public static class quantifiedInPredicate_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start quantifiedInPredicate
    // W:\\workspace-cmis\\ANTLR\\CMIS.g:361:1: quantifiedInPredicate : ANY multiValuedColumnReference ( NOT )? IN LPAREN inValueList RPAREN -> ^( PRED_IN ANY multiValuedColumnReference inValueList ( NOT )? ) ;
    public final CMISParser.quantifiedInPredicate_return quantifiedInPredicate() throws RecognitionException {
        CMISParser.quantifiedInPredicate_return retval = new CMISParser.quantifiedInPredicate_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token ANY111=null;
        Token NOT113=null;
        Token IN114=null;
        Token LPAREN115=null;
        Token RPAREN117=null;
        CMISParser.multiValuedColumnReference_return multiValuedColumnReference112 = null;

        CMISParser.inValueList_return inValueList116 = null;


        Object ANY111_tree=null;
        Object NOT113_tree=null;
        Object IN114_tree=null;
        Object LPAREN115_tree=null;
        Object RPAREN117_tree=null;
        RewriteRuleTokenStream stream_RPAREN=new RewriteRuleTokenStream(adaptor,"token RPAREN");
        RewriteRuleTokenStream stream_ANY=new RewriteRuleTokenStream(adaptor,"token ANY");
        RewriteRuleTokenStream stream_IN=new RewriteRuleTokenStream(adaptor,"token IN");
        RewriteRuleTokenStream stream_NOT=new RewriteRuleTokenStream(adaptor,"token NOT");
        RewriteRuleTokenStream stream_LPAREN=new RewriteRuleTokenStream(adaptor,"token LPAREN");
        RewriteRuleSubtreeStream stream_multiValuedColumnReference=new RewriteRuleSubtreeStream(adaptor,"rule multiValuedColumnReference");
        RewriteRuleSubtreeStream stream_inValueList=new RewriteRuleSubtreeStream(adaptor,"rule inValueList");
        try {
            // W:\\workspace-cmis\\ANTLR\\CMIS.g:362:2: ( ANY multiValuedColumnReference ( NOT )? IN LPAREN inValueList RPAREN -> ^( PRED_IN ANY multiValuedColumnReference inValueList ( NOT )? ) )
            // W:\\workspace-cmis\\ANTLR\\CMIS.g:362:4: ANY multiValuedColumnReference ( NOT )? IN LPAREN inValueList RPAREN
            {
            ANY111=(Token)match(input,ANY,FOLLOW_ANY_in_quantifiedInPredicate1391); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_ANY.add(ANY111);

            pushFollow(FOLLOW_multiValuedColumnReference_in_quantifiedInPredicate1393);
            multiValuedColumnReference112=multiValuedColumnReference();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_multiValuedColumnReference.add(multiValuedColumnReference112.getTree());
            // W:\\workspace-cmis\\ANTLR\\CMIS.g:362:35: ( NOT )?
            int alt35=2;
            int LA35_0 = input.LA(1);

            if ( (LA35_0==NOT) ) {
                alt35=1;
            }
            switch (alt35) {
                case 1 :
                    // W:\\workspace-cmis\\ANTLR\\CMIS.g:362:35: NOT
                    {
                    NOT113=(Token)match(input,NOT,FOLLOW_NOT_in_quantifiedInPredicate1395); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_NOT.add(NOT113);


                    }
                    break;

            }

            IN114=(Token)match(input,IN,FOLLOW_IN_in_quantifiedInPredicate1398); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_IN.add(IN114);

            LPAREN115=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_quantifiedInPredicate1401); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_LPAREN.add(LPAREN115);

            pushFollow(FOLLOW_inValueList_in_quantifiedInPredicate1403);
            inValueList116=inValueList();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_inValueList.add(inValueList116.getTree());
            RPAREN117=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_quantifiedInPredicate1405); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_RPAREN.add(RPAREN117);



            // AST REWRITE
            // elements: inValueList, ANY, multiValuedColumnReference, NOT
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 363:3: -> ^( PRED_IN ANY multiValuedColumnReference inValueList ( NOT )? )
            {
                // W:\\workspace-cmis\\ANTLR\\CMIS.g:363:6: ^( PRED_IN ANY multiValuedColumnReference inValueList ( NOT )? )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(PRED_IN, "PRED_IN"), root_1);

                adaptor.addChild(root_1, stream_ANY.nextNode());
                adaptor.addChild(root_1, stream_multiValuedColumnReference.nextTree());
                adaptor.addChild(root_1, stream_inValueList.nextTree());
                // W:\\workspace-cmis\\ANTLR\\CMIS.g:363:59: ( NOT )?
                if ( stream_NOT.hasNext() ) {
                    adaptor.addChild(root_1, stream_NOT.nextNode());

                }
                stream_NOT.reset();

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;retval.tree = root_0;}
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
    // $ANTLR end quantifiedInPredicate

    public static class textSearchPredicate_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start textSearchPredicate
    // W:\\workspace-cmis\\ANTLR\\CMIS.g:366:1: textSearchPredicate : CONTAINS LPAREN ( qualifier COMMA | COMMA )? textSearchExpression RPAREN -> ^( PRED_FTS textSearchExpression ( qualifier )? ) ;
    public final CMISParser.textSearchPredicate_return textSearchPredicate() throws RecognitionException {
        CMISParser.textSearchPredicate_return retval = new CMISParser.textSearchPredicate_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token CONTAINS118=null;
        Token LPAREN119=null;
        Token COMMA121=null;
        Token COMMA122=null;
        Token RPAREN124=null;
        CMISParser.qualifier_return qualifier120 = null;

        CMISParser.textSearchExpression_return textSearchExpression123 = null;


        Object CONTAINS118_tree=null;
        Object LPAREN119_tree=null;
        Object COMMA121_tree=null;
        Object COMMA122_tree=null;
        Object RPAREN124_tree=null;
        RewriteRuleTokenStream stream_RPAREN=new RewriteRuleTokenStream(adaptor,"token RPAREN");
        RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
        RewriteRuleTokenStream stream_CONTAINS=new RewriteRuleTokenStream(adaptor,"token CONTAINS");
        RewriteRuleTokenStream stream_LPAREN=new RewriteRuleTokenStream(adaptor,"token LPAREN");
        RewriteRuleSubtreeStream stream_qualifier=new RewriteRuleSubtreeStream(adaptor,"rule qualifier");
        RewriteRuleSubtreeStream stream_textSearchExpression=new RewriteRuleSubtreeStream(adaptor,"rule textSearchExpression");
        try {
            // W:\\workspace-cmis\\ANTLR\\CMIS.g:367:2: ( CONTAINS LPAREN ( qualifier COMMA | COMMA )? textSearchExpression RPAREN -> ^( PRED_FTS textSearchExpression ( qualifier )? ) )
            // W:\\workspace-cmis\\ANTLR\\CMIS.g:367:4: CONTAINS LPAREN ( qualifier COMMA | COMMA )? textSearchExpression RPAREN
            {
            CONTAINS118=(Token)match(input,CONTAINS,FOLLOW_CONTAINS_in_textSearchPredicate1434); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_CONTAINS.add(CONTAINS118);

            LPAREN119=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_textSearchPredicate1436); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_LPAREN.add(LPAREN119);

            // W:\\workspace-cmis\\ANTLR\\CMIS.g:367:20: ( qualifier COMMA | COMMA )?
            int alt36=3;
            int LA36_0 = input.LA(1);

            if ( ((LA36_0>=ID && LA36_0<=DOUBLE_QUOTE)) ) {
                alt36=1;
            }
            else if ( (LA36_0==COMMA) ) {
                alt36=2;
            }
            switch (alt36) {
                case 1 :
                    // W:\\workspace-cmis\\ANTLR\\CMIS.g:367:21: qualifier COMMA
                    {
                    pushFollow(FOLLOW_qualifier_in_textSearchPredicate1439);
                    qualifier120=qualifier();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_qualifier.add(qualifier120.getTree());
                    COMMA121=(Token)match(input,COMMA,FOLLOW_COMMA_in_textSearchPredicate1441); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_COMMA.add(COMMA121);


                    }
                    break;
                case 2 :
                    // W:\\workspace-cmis\\ANTLR\\CMIS.g:367:39: COMMA
                    {
                    COMMA122=(Token)match(input,COMMA,FOLLOW_COMMA_in_textSearchPredicate1445); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_COMMA.add(COMMA122);


                    }
                    break;

            }

            pushFollow(FOLLOW_textSearchExpression_in_textSearchPredicate1449);
            textSearchExpression123=textSearchExpression();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_textSearchExpression.add(textSearchExpression123.getTree());
            RPAREN124=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_textSearchPredicate1451); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_RPAREN.add(RPAREN124);



            // AST REWRITE
            // elements: qualifier, textSearchExpression
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 368:3: -> ^( PRED_FTS textSearchExpression ( qualifier )? )
            {
                // W:\\workspace-cmis\\ANTLR\\CMIS.g:368:6: ^( PRED_FTS textSearchExpression ( qualifier )? )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(PRED_FTS, "PRED_FTS"), root_1);

                adaptor.addChild(root_1, stream_textSearchExpression.nextTree());
                // W:\\workspace-cmis\\ANTLR\\CMIS.g:368:38: ( qualifier )?
                if ( stream_qualifier.hasNext() ) {
                    adaptor.addChild(root_1, stream_qualifier.nextTree());

                }
                stream_qualifier.reset();

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;retval.tree = root_0;}
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
    // $ANTLR end textSearchPredicate

    public static class folderPredicate_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start folderPredicate
    // W:\\workspace-cmis\\ANTLR\\CMIS.g:371:1: folderPredicate : ( IN_FOLDER folderPredicateArgs -> ^( PRED_CHILD folderPredicateArgs ) | IN_TREE folderPredicateArgs -> ^( PRED_DESCENDANT folderPredicateArgs ) );
    public final CMISParser.folderPredicate_return folderPredicate() throws RecognitionException {
        CMISParser.folderPredicate_return retval = new CMISParser.folderPredicate_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token IN_FOLDER125=null;
        Token IN_TREE127=null;
        CMISParser.folderPredicateArgs_return folderPredicateArgs126 = null;

        CMISParser.folderPredicateArgs_return folderPredicateArgs128 = null;


        Object IN_FOLDER125_tree=null;
        Object IN_TREE127_tree=null;
        RewriteRuleTokenStream stream_IN_TREE=new RewriteRuleTokenStream(adaptor,"token IN_TREE");
        RewriteRuleTokenStream stream_IN_FOLDER=new RewriteRuleTokenStream(adaptor,"token IN_FOLDER");
        RewriteRuleSubtreeStream stream_folderPredicateArgs=new RewriteRuleSubtreeStream(adaptor,"rule folderPredicateArgs");
        try {
            // W:\\workspace-cmis\\ANTLR\\CMIS.g:372:2: ( IN_FOLDER folderPredicateArgs -> ^( PRED_CHILD folderPredicateArgs ) | IN_TREE folderPredicateArgs -> ^( PRED_DESCENDANT folderPredicateArgs ) )
            int alt37=2;
            int LA37_0 = input.LA(1);

            if ( (LA37_0==IN_FOLDER) ) {
                alt37=1;
            }
            else if ( (LA37_0==IN_TREE) ) {
                alt37=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 37, 0, input);

                throw nvae;
            }
            switch (alt37) {
                case 1 :
                    // W:\\workspace-cmis\\ANTLR\\CMIS.g:372:4: IN_FOLDER folderPredicateArgs
                    {
                    IN_FOLDER125=(Token)match(input,IN_FOLDER,FOLLOW_IN_FOLDER_in_folderPredicate1476); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_IN_FOLDER.add(IN_FOLDER125);

                    pushFollow(FOLLOW_folderPredicateArgs_in_folderPredicate1479);
                    folderPredicateArgs126=folderPredicateArgs();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_folderPredicateArgs.add(folderPredicateArgs126.getTree());


                    // AST REWRITE
                    // elements: folderPredicateArgs
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 373:3: -> ^( PRED_CHILD folderPredicateArgs )
                    {
                        // W:\\workspace-cmis\\ANTLR\\CMIS.g:373:6: ^( PRED_CHILD folderPredicateArgs )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(PRED_CHILD, "PRED_CHILD"), root_1);

                        adaptor.addChild(root_1, stream_folderPredicateArgs.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // W:\\workspace-cmis\\ANTLR\\CMIS.g:374:10: IN_TREE folderPredicateArgs
                    {
                    IN_TREE127=(Token)match(input,IN_TREE,FOLLOW_IN_TREE_in_folderPredicate1500); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_IN_TREE.add(IN_TREE127);

                    pushFollow(FOLLOW_folderPredicateArgs_in_folderPredicate1502);
                    folderPredicateArgs128=folderPredicateArgs();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_folderPredicateArgs.add(folderPredicateArgs128.getTree());


                    // AST REWRITE
                    // elements: folderPredicateArgs
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 375:3: -> ^( PRED_DESCENDANT folderPredicateArgs )
                    {
                        // W:\\workspace-cmis\\ANTLR\\CMIS.g:375:6: ^( PRED_DESCENDANT folderPredicateArgs )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(PRED_DESCENDANT, "PRED_DESCENDANT"), root_1);

                        adaptor.addChild(root_1, stream_folderPredicateArgs.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;retval.tree = root_0;}
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
    // $ANTLR end folderPredicate

    public static class folderPredicateArgs_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start folderPredicateArgs
    // W:\\workspace-cmis\\ANTLR\\CMIS.g:378:1: folderPredicateArgs : LPAREN ( qualifier COMMA | COMMA )? folderId RPAREN -> folderId ( qualifier )? ;
    public final CMISParser.folderPredicateArgs_return folderPredicateArgs() throws RecognitionException {
        CMISParser.folderPredicateArgs_return retval = new CMISParser.folderPredicateArgs_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token LPAREN129=null;
        Token COMMA131=null;
        Token COMMA132=null;
        Token RPAREN134=null;
        CMISParser.qualifier_return qualifier130 = null;

        CMISParser.folderId_return folderId133 = null;


        Object LPAREN129_tree=null;
        Object COMMA131_tree=null;
        Object COMMA132_tree=null;
        Object RPAREN134_tree=null;
        RewriteRuleTokenStream stream_RPAREN=new RewriteRuleTokenStream(adaptor,"token RPAREN");
        RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
        RewriteRuleTokenStream stream_LPAREN=new RewriteRuleTokenStream(adaptor,"token LPAREN");
        RewriteRuleSubtreeStream stream_qualifier=new RewriteRuleSubtreeStream(adaptor,"rule qualifier");
        RewriteRuleSubtreeStream stream_folderId=new RewriteRuleSubtreeStream(adaptor,"rule folderId");
        try {
            // W:\\workspace-cmis\\ANTLR\\CMIS.g:379:2: ( LPAREN ( qualifier COMMA | COMMA )? folderId RPAREN -> folderId ( qualifier )? )
            // W:\\workspace-cmis\\ANTLR\\CMIS.g:379:4: LPAREN ( qualifier COMMA | COMMA )? folderId RPAREN
            {
            LPAREN129=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_folderPredicateArgs1524); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_LPAREN.add(LPAREN129);

            // W:\\workspace-cmis\\ANTLR\\CMIS.g:379:11: ( qualifier COMMA | COMMA )?
            int alt38=3;
            int LA38_0 = input.LA(1);

            if ( ((LA38_0>=ID && LA38_0<=DOUBLE_QUOTE)) ) {
                alt38=1;
            }
            else if ( (LA38_0==COMMA) ) {
                alt38=2;
            }
            switch (alt38) {
                case 1 :
                    // W:\\workspace-cmis\\ANTLR\\CMIS.g:379:12: qualifier COMMA
                    {
                    pushFollow(FOLLOW_qualifier_in_folderPredicateArgs1527);
                    qualifier130=qualifier();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_qualifier.add(qualifier130.getTree());
                    COMMA131=(Token)match(input,COMMA,FOLLOW_COMMA_in_folderPredicateArgs1529); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_COMMA.add(COMMA131);


                    }
                    break;
                case 2 :
                    // W:\\workspace-cmis\\ANTLR\\CMIS.g:379:30: COMMA
                    {
                    COMMA132=(Token)match(input,COMMA,FOLLOW_COMMA_in_folderPredicateArgs1533); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_COMMA.add(COMMA132);


                    }
                    break;

            }

            pushFollow(FOLLOW_folderId_in_folderPredicateArgs1537);
            folderId133=folderId();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_folderId.add(folderId133.getTree());
            RPAREN134=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_folderPredicateArgs1539); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_RPAREN.add(RPAREN134);



            // AST REWRITE
            // elements: folderId, qualifier
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 380:3: -> folderId ( qualifier )?
            {
                adaptor.addChild(root_0, stream_folderId.nextTree());
                // W:\\workspace-cmis\\ANTLR\\CMIS.g:380:15: ( qualifier )?
                if ( stream_qualifier.hasNext() ) {
                    adaptor.addChild(root_0, stream_qualifier.nextTree());

                }
                stream_qualifier.reset();

            }

            retval.tree = root_0;retval.tree = root_0;}
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
    // $ANTLR end folderPredicateArgs

    public static class orderByClause_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start orderByClause
    // W:\\workspace-cmis\\ANTLR\\CMIS.g:383:1: orderByClause : ORDER BY sortSpecification ( COMMA sortSpecification )* -> ^( ORDER ( sortSpecification )+ ) ;
    public final CMISParser.orderByClause_return orderByClause() throws RecognitionException {
        CMISParser.orderByClause_return retval = new CMISParser.orderByClause_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token ORDER135=null;
        Token BY136=null;
        Token COMMA138=null;
        CMISParser.sortSpecification_return sortSpecification137 = null;

        CMISParser.sortSpecification_return sortSpecification139 = null;


        Object ORDER135_tree=null;
        Object BY136_tree=null;
        Object COMMA138_tree=null;
        RewriteRuleTokenStream stream_BY=new RewriteRuleTokenStream(adaptor,"token BY");
        RewriteRuleTokenStream stream_ORDER=new RewriteRuleTokenStream(adaptor,"token ORDER");
        RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
        RewriteRuleSubtreeStream stream_sortSpecification=new RewriteRuleSubtreeStream(adaptor,"rule sortSpecification");
            paraphrases.push("in order by"); 
        try {
            // W:\\workspace-cmis\\ANTLR\\CMIS.g:386:2: ( ORDER BY sortSpecification ( COMMA sortSpecification )* -> ^( ORDER ( sortSpecification )+ ) )
            // W:\\workspace-cmis\\ANTLR\\CMIS.g:386:4: ORDER BY sortSpecification ( COMMA sortSpecification )*
            {
            ORDER135=(Token)match(input,ORDER,FOLLOW_ORDER_in_orderByClause1578); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_ORDER.add(ORDER135);

            BY136=(Token)match(input,BY,FOLLOW_BY_in_orderByClause1580); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_BY.add(BY136);

            pushFollow(FOLLOW_sortSpecification_in_orderByClause1582);
            sortSpecification137=sortSpecification();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_sortSpecification.add(sortSpecification137.getTree());
            // W:\\workspace-cmis\\ANTLR\\CMIS.g:386:31: ( COMMA sortSpecification )*
            loop39:
            do {
                int alt39=2;
                int LA39_0 = input.LA(1);

                if ( (LA39_0==COMMA) ) {
                    alt39=1;
                }


                switch (alt39) {
            	case 1 :
            	    // W:\\workspace-cmis\\ANTLR\\CMIS.g:386:33: COMMA sortSpecification
            	    {
            	    COMMA138=(Token)match(input,COMMA,FOLLOW_COMMA_in_orderByClause1586); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_COMMA.add(COMMA138);

            	    pushFollow(FOLLOW_sortSpecification_in_orderByClause1588);
            	    sortSpecification139=sortSpecification();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_sortSpecification.add(sortSpecification139.getTree());

            	    }
            	    break;

            	default :
            	    break loop39;
                }
            } while (true);



            // AST REWRITE
            // elements: sortSpecification, ORDER
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 387:3: -> ^( ORDER ( sortSpecification )+ )
            {
                // W:\\workspace-cmis\\ANTLR\\CMIS.g:387:6: ^( ORDER ( sortSpecification )+ )
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

            retval.tree = root_0;retval.tree = root_0;}
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
           throw e;
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end orderByClause

    public static class sortSpecification_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start sortSpecification
    // W:\\workspace-cmis\\ANTLR\\CMIS.g:390:1: sortSpecification : ( columnReference -> ^( SORT_SPECIFICATION columnReference ASC ) | columnReference (by= ASC | by= DESC ) -> ^( SORT_SPECIFICATION columnReference $by) );
    public final CMISParser.sortSpecification_return sortSpecification() throws RecognitionException {
        CMISParser.sortSpecification_return retval = new CMISParser.sortSpecification_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token by=null;
        CMISParser.columnReference_return columnReference140 = null;

        CMISParser.columnReference_return columnReference141 = null;


        Object by_tree=null;
        RewriteRuleTokenStream stream_ASC=new RewriteRuleTokenStream(adaptor,"token ASC");
        RewriteRuleTokenStream stream_DESC=new RewriteRuleTokenStream(adaptor,"token DESC");
        RewriteRuleSubtreeStream stream_columnReference=new RewriteRuleSubtreeStream(adaptor,"rule columnReference");
        try {
            // W:\\workspace-cmis\\ANTLR\\CMIS.g:391:2: ( columnReference -> ^( SORT_SPECIFICATION columnReference ASC ) | columnReference (by= ASC | by= DESC ) -> ^( SORT_SPECIFICATION columnReference $by) )
            int alt41=2;
            alt41 = dfa41.predict(input);
            switch (alt41) {
                case 1 :
                    // W:\\workspace-cmis\\ANTLR\\CMIS.g:391:4: columnReference
                    {
                    pushFollow(FOLLOW_columnReference_in_sortSpecification1614);
                    columnReference140=columnReference();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_columnReference.add(columnReference140.getTree());


                    // AST REWRITE
                    // elements: columnReference
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 392:3: -> ^( SORT_SPECIFICATION columnReference ASC )
                    {
                        // W:\\workspace-cmis\\ANTLR\\CMIS.g:392:6: ^( SORT_SPECIFICATION columnReference ASC )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(SORT_SPECIFICATION, "SORT_SPECIFICATION"), root_1);

                        adaptor.addChild(root_1, stream_columnReference.nextTree());
                        adaptor.addChild(root_1, (Object)adaptor.create(ASC, "ASC"));

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // W:\\workspace-cmis\\ANTLR\\CMIS.g:393:4: columnReference (by= ASC | by= DESC )
                    {
                    pushFollow(FOLLOW_columnReference_in_sortSpecification1632);
                    columnReference141=columnReference();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_columnReference.add(columnReference141.getTree());
                    // W:\\workspace-cmis\\ANTLR\\CMIS.g:393:20: (by= ASC | by= DESC )
                    int alt40=2;
                    int LA40_0 = input.LA(1);

                    if ( (LA40_0==ASC) ) {
                        alt40=1;
                    }
                    else if ( (LA40_0==DESC) ) {
                        alt40=2;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return retval;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 40, 0, input);

                        throw nvae;
                    }
                    switch (alt40) {
                        case 1 :
                            // W:\\workspace-cmis\\ANTLR\\CMIS.g:393:22: by= ASC
                            {
                            by=(Token)match(input,ASC,FOLLOW_ASC_in_sortSpecification1638); if (state.failed) return retval; 
                            if ( state.backtracking==0 ) stream_ASC.add(by);


                            }
                            break;
                        case 2 :
                            // W:\\workspace-cmis\\ANTLR\\CMIS.g:393:31: by= DESC
                            {
                            by=(Token)match(input,DESC,FOLLOW_DESC_in_sortSpecification1644); if (state.failed) return retval; 
                            if ( state.backtracking==0 ) stream_DESC.add(by);


                            }
                            break;

                    }



                    // AST REWRITE
                    // elements: by, columnReference
                    // token labels: by
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleTokenStream stream_by=new RewriteRuleTokenStream(adaptor,"token by",by);
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 394:3: -> ^( SORT_SPECIFICATION columnReference $by)
                    {
                        // W:\\workspace-cmis\\ANTLR\\CMIS.g:394:6: ^( SORT_SPECIFICATION columnReference $by)
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(SORT_SPECIFICATION, "SORT_SPECIFICATION"), root_1);

                        adaptor.addChild(root_1, stream_columnReference.nextTree());
                        adaptor.addChild(root_1, stream_by.nextNode());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;retval.tree = root_0;}
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
    // $ANTLR end sortSpecification

    public static class correlationName_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start correlationName
    // W:\\workspace-cmis\\ANTLR\\CMIS.g:397:1: correlationName : identifier ;
    public final CMISParser.correlationName_return correlationName() throws RecognitionException {
        CMISParser.correlationName_return retval = new CMISParser.correlationName_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        CMISParser.identifier_return identifier142 = null;



        try {
            // W:\\workspace-cmis\\ANTLR\\CMIS.g:398:2: ( identifier )
            // W:\\workspace-cmis\\ANTLR\\CMIS.g:398:4: identifier
            {
            root_0 = (Object)adaptor.nil();

            pushFollow(FOLLOW_identifier_in_correlationName1671);
            identifier142=identifier();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, identifier142.getTree());

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
    // $ANTLR end correlationName

    public static class tableName_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start tableName
    // W:\\workspace-cmis\\ANTLR\\CMIS.g:405:1: tableName : identifier -> identifier ;
    public final CMISParser.tableName_return tableName() throws RecognitionException {
        CMISParser.tableName_return retval = new CMISParser.tableName_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        CMISParser.identifier_return identifier143 = null;


        RewriteRuleSubtreeStream stream_identifier=new RewriteRuleSubtreeStream(adaptor,"rule identifier");
        try {
            // W:\\workspace-cmis\\ANTLR\\CMIS.g:406:2: ( identifier -> identifier )
            // W:\\workspace-cmis\\ANTLR\\CMIS.g:406:4: identifier
            {
            pushFollow(FOLLOW_identifier_in_tableName1685);
            identifier143=identifier();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_identifier.add(identifier143.getTree());


            // AST REWRITE
            // elements: identifier
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 407:3: -> identifier
            {
                adaptor.addChild(root_0, stream_identifier.nextTree());

            }

            retval.tree = root_0;retval.tree = root_0;}
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
    // $ANTLR end tableName

    public static class columnName_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start columnName
    // W:\\workspace-cmis\\ANTLR\\CMIS.g:410:1: columnName : identifier -> identifier ;
    public final CMISParser.columnName_return columnName() throws RecognitionException {
        CMISParser.columnName_return retval = new CMISParser.columnName_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        CMISParser.identifier_return identifier144 = null;


        RewriteRuleSubtreeStream stream_identifier=new RewriteRuleSubtreeStream(adaptor,"rule identifier");
        try {
            // W:\\workspace-cmis\\ANTLR\\CMIS.g:411:2: ( identifier -> identifier )
            // W:\\workspace-cmis\\ANTLR\\CMIS.g:411:4: identifier
            {
            pushFollow(FOLLOW_identifier_in_columnName1703);
            identifier144=identifier();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_identifier.add(identifier144.getTree());


            // AST REWRITE
            // elements: identifier
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 412:3: -> identifier
            {
                adaptor.addChild(root_0, stream_identifier.nextTree());

            }

            retval.tree = root_0;retval.tree = root_0;}
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
    // $ANTLR end columnName

    public static class multiValuedColumnName_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start multiValuedColumnName
    // W:\\workspace-cmis\\ANTLR\\CMIS.g:415:1: multiValuedColumnName : identifier -> identifier ;
    public final CMISParser.multiValuedColumnName_return multiValuedColumnName() throws RecognitionException {
        CMISParser.multiValuedColumnName_return retval = new CMISParser.multiValuedColumnName_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        CMISParser.identifier_return identifier145 = null;


        RewriteRuleSubtreeStream stream_identifier=new RewriteRuleSubtreeStream(adaptor,"rule identifier");
        try {
            // W:\\workspace-cmis\\ANTLR\\CMIS.g:416:2: ( identifier -> identifier )
            // W:\\workspace-cmis\\ANTLR\\CMIS.g:416:4: identifier
            {
            pushFollow(FOLLOW_identifier_in_multiValuedColumnName1722);
            identifier145=identifier();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_identifier.add(identifier145.getTree());


            // AST REWRITE
            // elements: identifier
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 417:3: -> identifier
            {
                adaptor.addChild(root_0, stream_identifier.nextTree());

            }

            retval.tree = root_0;retval.tree = root_0;}
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
    // $ANTLR end multiValuedColumnName

    public static class parameterName_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start parameterName
    // W:\\workspace-cmis\\ANTLR\\CMIS.g:420:1: parameterName : COLON identifier -> ^( PARAMETER identifier ) ;
    public final CMISParser.parameterName_return parameterName() throws RecognitionException {
        CMISParser.parameterName_return retval = new CMISParser.parameterName_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token COLON146=null;
        CMISParser.identifier_return identifier147 = null;


        Object COLON146_tree=null;
        RewriteRuleTokenStream stream_COLON=new RewriteRuleTokenStream(adaptor,"token COLON");
        RewriteRuleSubtreeStream stream_identifier=new RewriteRuleSubtreeStream(adaptor,"rule identifier");
        try {
            // W:\\workspace-cmis\\ANTLR\\CMIS.g:421:2: ( COLON identifier -> ^( PARAMETER identifier ) )
            // W:\\workspace-cmis\\ANTLR\\CMIS.g:421:4: COLON identifier
            {
            COLON146=(Token)match(input,COLON,FOLLOW_COLON_in_parameterName1740); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_COLON.add(COLON146);

            pushFollow(FOLLOW_identifier_in_parameterName1742);
            identifier147=identifier();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_identifier.add(identifier147.getTree());


            // AST REWRITE
            // elements: identifier
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 422:3: -> ^( PARAMETER identifier )
            {
                // W:\\workspace-cmis\\ANTLR\\CMIS.g:422:6: ^( PARAMETER identifier )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(PARAMETER, "PARAMETER"), root_1);

                adaptor.addChild(root_1, stream_identifier.nextTree());

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;retval.tree = root_0;}
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
    // $ANTLR end parameterName

    public static class folderId_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start folderId
    // W:\\workspace-cmis\\ANTLR\\CMIS.g:425:1: folderId : characterStringLiteral -> characterStringLiteral ;
    public final CMISParser.folderId_return folderId() throws RecognitionException {
        CMISParser.folderId_return retval = new CMISParser.folderId_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        CMISParser.characterStringLiteral_return characterStringLiteral148 = null;


        RewriteRuleSubtreeStream stream_characterStringLiteral=new RewriteRuleSubtreeStream(adaptor,"rule characterStringLiteral");
        try {
            // W:\\workspace-cmis\\ANTLR\\CMIS.g:426:3: ( characterStringLiteral -> characterStringLiteral )
            // W:\\workspace-cmis\\ANTLR\\CMIS.g:426:5: characterStringLiteral
            {
            pushFollow(FOLLOW_characterStringLiteral_in_folderId1765);
            characterStringLiteral148=characterStringLiteral();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_characterStringLiteral.add(characterStringLiteral148.getTree());


            // AST REWRITE
            // elements: characterStringLiteral
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 427:4: -> characterStringLiteral
            {
                adaptor.addChild(root_0, stream_characterStringLiteral.nextTree());

            }

            retval.tree = root_0;retval.tree = root_0;}
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
    // $ANTLR end folderId

    public static class textSearchExpression_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start textSearchExpression
    // W:\\workspace-cmis\\ANTLR\\CMIS.g:430:1: textSearchExpression : QUOTED_STRING ;
    public final CMISParser.textSearchExpression_return textSearchExpression() throws RecognitionException {
        CMISParser.textSearchExpression_return retval = new CMISParser.textSearchExpression_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token QUOTED_STRING149=null;

        Object QUOTED_STRING149_tree=null;

        try {
            // W:\\workspace-cmis\\ANTLR\\CMIS.g:431:2: ( QUOTED_STRING )
            // W:\\workspace-cmis\\ANTLR\\CMIS.g:431:4: QUOTED_STRING
            {
            root_0 = (Object)adaptor.nil();

            QUOTED_STRING149=(Token)match(input,QUOTED_STRING,FOLLOW_QUOTED_STRING_in_textSearchExpression1786); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            QUOTED_STRING149_tree = (Object)adaptor.create(QUOTED_STRING149);
            adaptor.addChild(root_0, QUOTED_STRING149_tree);
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
    // $ANTLR end textSearchExpression

    public static class identifier_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start identifier
    // W:\\workspace-cmis\\ANTLR\\CMIS.g:434:1: identifier : ( ID -> ID | DOUBLE_QUOTE keyWordOrId DOUBLE_QUOTE -> ^( keyWordOrId ) );
    public final CMISParser.identifier_return identifier() throws RecognitionException {
        CMISParser.identifier_return retval = new CMISParser.identifier_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token ID150=null;
        Token DOUBLE_QUOTE151=null;
        Token DOUBLE_QUOTE153=null;
        CMISParser.keyWordOrId_return keyWordOrId152 = null;


        Object ID150_tree=null;
        Object DOUBLE_QUOTE151_tree=null;
        Object DOUBLE_QUOTE153_tree=null;
        RewriteRuleTokenStream stream_ID=new RewriteRuleTokenStream(adaptor,"token ID");
        RewriteRuleTokenStream stream_DOUBLE_QUOTE=new RewriteRuleTokenStream(adaptor,"token DOUBLE_QUOTE");
        RewriteRuleSubtreeStream stream_keyWordOrId=new RewriteRuleSubtreeStream(adaptor,"rule keyWordOrId");
        try {
            // W:\\workspace-cmis\\ANTLR\\CMIS.g:435:2: ( ID -> ID | DOUBLE_QUOTE keyWordOrId DOUBLE_QUOTE -> ^( keyWordOrId ) )
            int alt42=2;
            int LA42_0 = input.LA(1);

            if ( (LA42_0==ID) ) {
                alt42=1;
            }
            else if ( (LA42_0==DOUBLE_QUOTE) ) {
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
                    // W:\\workspace-cmis\\ANTLR\\CMIS.g:435:4: ID
                    {
                    ID150=(Token)match(input,ID,FOLLOW_ID_in_identifier1798); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_ID.add(ID150);



                    // AST REWRITE
                    // elements: ID
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 436:3: -> ID
                    {
                        adaptor.addChild(root_0, stream_ID.nextNode());

                    }

                    retval.tree = root_0;retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // W:\\workspace-cmis\\ANTLR\\CMIS.g:437:4: DOUBLE_QUOTE keyWordOrId DOUBLE_QUOTE
                    {
                    DOUBLE_QUOTE151=(Token)match(input,DOUBLE_QUOTE,FOLLOW_DOUBLE_QUOTE_in_identifier1809); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_DOUBLE_QUOTE.add(DOUBLE_QUOTE151);

                    pushFollow(FOLLOW_keyWordOrId_in_identifier1811);
                    keyWordOrId152=keyWordOrId();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_keyWordOrId.add(keyWordOrId152.getTree());
                    DOUBLE_QUOTE153=(Token)match(input,DOUBLE_QUOTE,FOLLOW_DOUBLE_QUOTE_in_identifier1813); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_DOUBLE_QUOTE.add(DOUBLE_QUOTE153);



                    // AST REWRITE
                    // elements: keyWordOrId
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 438:3: -> ^( keyWordOrId )
                    {
                        // W:\\workspace-cmis\\ANTLR\\CMIS.g:438:6: ^( keyWordOrId )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot(stream_keyWordOrId.nextNode(), root_1);

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;retval.tree = root_0;}
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
    // $ANTLR end identifier

    public static class signedNumericLiteral_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start signedNumericLiteral
    // W:\\workspace-cmis\\ANTLR\\CMIS.g:441:1: signedNumericLiteral : ( FLOATING_POINT_LITERAL -> ^( NUMERIC_LITERAL FLOATING_POINT_LITERAL ) | integerLiteral -> integerLiteral );
    public final CMISParser.signedNumericLiteral_return signedNumericLiteral() throws RecognitionException {
        CMISParser.signedNumericLiteral_return retval = new CMISParser.signedNumericLiteral_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token FLOATING_POINT_LITERAL154=null;
        CMISParser.integerLiteral_return integerLiteral155 = null;


        Object FLOATING_POINT_LITERAL154_tree=null;
        RewriteRuleTokenStream stream_FLOATING_POINT_LITERAL=new RewriteRuleTokenStream(adaptor,"token FLOATING_POINT_LITERAL");
        RewriteRuleSubtreeStream stream_integerLiteral=new RewriteRuleSubtreeStream(adaptor,"rule integerLiteral");
        try {
            // W:\\workspace-cmis\\ANTLR\\CMIS.g:442:2: ( FLOATING_POINT_LITERAL -> ^( NUMERIC_LITERAL FLOATING_POINT_LITERAL ) | integerLiteral -> integerLiteral )
            int alt43=2;
            int LA43_0 = input.LA(1);

            if ( (LA43_0==FLOATING_POINT_LITERAL) ) {
                alt43=1;
            }
            else if ( (LA43_0==DECIMAL_INTEGER_LITERAL) ) {
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
                    // W:\\workspace-cmis\\ANTLR\\CMIS.g:442:4: FLOATING_POINT_LITERAL
                    {
                    FLOATING_POINT_LITERAL154=(Token)match(input,FLOATING_POINT_LITERAL,FOLLOW_FLOATING_POINT_LITERAL_in_signedNumericLiteral1833); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_FLOATING_POINT_LITERAL.add(FLOATING_POINT_LITERAL154);



                    // AST REWRITE
                    // elements: FLOATING_POINT_LITERAL
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 443:3: -> ^( NUMERIC_LITERAL FLOATING_POINT_LITERAL )
                    {
                        // W:\\workspace-cmis\\ANTLR\\CMIS.g:443:6: ^( NUMERIC_LITERAL FLOATING_POINT_LITERAL )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(NUMERIC_LITERAL, "NUMERIC_LITERAL"), root_1);

                        adaptor.addChild(root_1, stream_FLOATING_POINT_LITERAL.nextNode());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // W:\\workspace-cmis\\ANTLR\\CMIS.g:444:4: integerLiteral
                    {
                    pushFollow(FOLLOW_integerLiteral_in_signedNumericLiteral1848);
                    integerLiteral155=integerLiteral();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_integerLiteral.add(integerLiteral155.getTree());


                    // AST REWRITE
                    // elements: integerLiteral
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 445:3: -> integerLiteral
                    {
                        adaptor.addChild(root_0, stream_integerLiteral.nextTree());

                    }

                    retval.tree = root_0;retval.tree = root_0;}
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
    // $ANTLR end signedNumericLiteral

    public static class integerLiteral_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start integerLiteral
    // W:\\workspace-cmis\\ANTLR\\CMIS.g:448:1: integerLiteral : DECIMAL_INTEGER_LITERAL -> ^( NUMERIC_LITERAL DECIMAL_INTEGER_LITERAL ) ;
    public final CMISParser.integerLiteral_return integerLiteral() throws RecognitionException {
        CMISParser.integerLiteral_return retval = new CMISParser.integerLiteral_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token DECIMAL_INTEGER_LITERAL156=null;

        Object DECIMAL_INTEGER_LITERAL156_tree=null;
        RewriteRuleTokenStream stream_DECIMAL_INTEGER_LITERAL=new RewriteRuleTokenStream(adaptor,"token DECIMAL_INTEGER_LITERAL");

        try {
            // W:\\workspace-cmis\\ANTLR\\CMIS.g:449:2: ( DECIMAL_INTEGER_LITERAL -> ^( NUMERIC_LITERAL DECIMAL_INTEGER_LITERAL ) )
            // W:\\workspace-cmis\\ANTLR\\CMIS.g:449:4: DECIMAL_INTEGER_LITERAL
            {
            DECIMAL_INTEGER_LITERAL156=(Token)match(input,DECIMAL_INTEGER_LITERAL,FOLLOW_DECIMAL_INTEGER_LITERAL_in_integerLiteral1867); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_DECIMAL_INTEGER_LITERAL.add(DECIMAL_INTEGER_LITERAL156);



            // AST REWRITE
            // elements: DECIMAL_INTEGER_LITERAL
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 450:3: -> ^( NUMERIC_LITERAL DECIMAL_INTEGER_LITERAL )
            {
                // W:\\workspace-cmis\\ANTLR\\CMIS.g:450:6: ^( NUMERIC_LITERAL DECIMAL_INTEGER_LITERAL )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(NUMERIC_LITERAL, "NUMERIC_LITERAL"), root_1);

                adaptor.addChild(root_1, stream_DECIMAL_INTEGER_LITERAL.nextNode());

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;retval.tree = root_0;}
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
    // $ANTLR end integerLiteral

    public static class characterStringLiteral_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start characterStringLiteral
    // W:\\workspace-cmis\\ANTLR\\CMIS.g:453:1: characterStringLiteral : QUOTED_STRING -> ^( STRING_LITERAL QUOTED_STRING ) ;
    public final CMISParser.characterStringLiteral_return characterStringLiteral() throws RecognitionException {
        CMISParser.characterStringLiteral_return retval = new CMISParser.characterStringLiteral_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token QUOTED_STRING157=null;

        Object QUOTED_STRING157_tree=null;
        RewriteRuleTokenStream stream_QUOTED_STRING=new RewriteRuleTokenStream(adaptor,"token QUOTED_STRING");

        try {
            // W:\\workspace-cmis\\ANTLR\\CMIS.g:454:2: ( QUOTED_STRING -> ^( STRING_LITERAL QUOTED_STRING ) )
            // W:\\workspace-cmis\\ANTLR\\CMIS.g:454:4: QUOTED_STRING
            {
            QUOTED_STRING157=(Token)match(input,QUOTED_STRING,FOLLOW_QUOTED_STRING_in_characterStringLiteral1890); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_QUOTED_STRING.add(QUOTED_STRING157);



            // AST REWRITE
            // elements: QUOTED_STRING
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 455:3: -> ^( STRING_LITERAL QUOTED_STRING )
            {
                // W:\\workspace-cmis\\ANTLR\\CMIS.g:455:6: ^( STRING_LITERAL QUOTED_STRING )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(STRING_LITERAL, "STRING_LITERAL"), root_1);

                adaptor.addChild(root_1, stream_QUOTED_STRING.nextNode());

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;retval.tree = root_0;}
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
    // $ANTLR end characterStringLiteral

    public static class keyWord_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start keyWord
    // W:\\workspace-cmis\\ANTLR\\CMIS.g:459:1: keyWord : ( SELECT | AS | UPPER | LOWER | FROM | JOIN | INNER | LEFT | OUTER | ON | WHERE | OR | AND | NOT | IN | LIKE | IS | NULL | ANY | CONTAINS | IN_FOLDER | IN_TREE | ORDER | BY | ASC | DESC | SCORE );
    public final CMISParser.keyWord_return keyWord() throws RecognitionException {
        CMISParser.keyWord_return retval = new CMISParser.keyWord_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token set158=null;

        Object set158_tree=null;

        try {
            // W:\\workspace-cmis\\ANTLR\\CMIS.g:459:9: ( SELECT | AS | UPPER | LOWER | FROM | JOIN | INNER | LEFT | OUTER | ON | WHERE | OR | AND | NOT | IN | LIKE | IS | NULL | ANY | CONTAINS | IN_FOLDER | IN_TREE | ORDER | BY | ASC | DESC | SCORE )
            // W:\\workspace-cmis\\ANTLR\\CMIS.g:
            {
            root_0 = (Object)adaptor.nil();

            set158=(Token)input.LT(1);
            if ( input.LA(1)==SELECT||input.LA(1)==AS||(input.LA(1)>=FROM && input.LA(1)<=ON)||(input.LA(1)>=WHERE && input.LA(1)<=NOT)||(input.LA(1)>=IN && input.LA(1)<=DESC)||(input.LA(1)>=UPPER && input.LA(1)<=SCORE) ) {
                input.consume();
                if ( state.backtracking==0 ) adaptor.addChild(root_0, (Object)adaptor.create(set158));
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
    // $ANTLR end keyWord

    public static class keyWordOrId_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start keyWordOrId
    // W:\\workspace-cmis\\ANTLR\\CMIS.g:488:1: keyWordOrId : ( keyWord -> keyWord | ID -> ID );
    public final CMISParser.keyWordOrId_return keyWordOrId() throws RecognitionException {
        CMISParser.keyWordOrId_return retval = new CMISParser.keyWordOrId_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token ID160=null;
        CMISParser.keyWord_return keyWord159 = null;


        Object ID160_tree=null;
        RewriteRuleTokenStream stream_ID=new RewriteRuleTokenStream(adaptor,"token ID");
        RewriteRuleSubtreeStream stream_keyWord=new RewriteRuleSubtreeStream(adaptor,"rule keyWord");
        try {
            // W:\\workspace-cmis\\ANTLR\\CMIS.g:489:2: ( keyWord -> keyWord | ID -> ID )
            int alt44=2;
            int LA44_0 = input.LA(1);

            if ( (LA44_0==SELECT||LA44_0==AS||(LA44_0>=FROM && LA44_0<=ON)||(LA44_0>=WHERE && LA44_0<=NOT)||(LA44_0>=IN && LA44_0<=DESC)||(LA44_0>=UPPER && LA44_0<=SCORE)) ) {
                alt44=1;
            }
            else if ( (LA44_0==ID) ) {
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
                    // W:\\workspace-cmis\\ANTLR\\CMIS.g:489:4: keyWord
                    {
                    pushFollow(FOLLOW_keyWord_in_keyWordOrId2099);
                    keyWord159=keyWord();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_keyWord.add(keyWord159.getTree());


                    // AST REWRITE
                    // elements: keyWord
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 490:3: -> keyWord
                    {
                        adaptor.addChild(root_0, stream_keyWord.nextTree());

                    }

                    retval.tree = root_0;retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // W:\\workspace-cmis\\ANTLR\\CMIS.g:491:4: ID
                    {
                    ID160=(Token)match(input,ID,FOLLOW_ID_in_keyWordOrId2111); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_ID.add(ID160);



                    // AST REWRITE
                    // elements: ID
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 492:3: -> ID
                    {
                        adaptor.addChild(root_0, stream_ID.nextNode());

                    }

                    retval.tree = root_0;retval.tree = root_0;}
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
    // $ANTLR end keyWordOrId

    // $ANTLR start synpred1_CMIS
    public final void synpred1_CMIS_fragment() throws RecognitionException {   
        // W:\\workspace-cmis\\ANTLR\\CMIS.g:165:4: ( valueExpression )
        // W:\\workspace-cmis\\ANTLR\\CMIS.g:165:5: valueExpression
        {
        pushFollow(FOLLOW_valueExpression_in_synpred1_CMIS287);
        valueExpression();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred1_CMIS

    // $ANTLR start synpred2_CMIS
    public final void synpred2_CMIS_fragment() throws RecognitionException {   
        // W:\\workspace-cmis\\ANTLR\\CMIS.g:207:4: ( tableName )
        // W:\\workspace-cmis\\ANTLR\\CMIS.g:207:5: tableName
        {
        pushFollow(FOLLOW_tableName_in_synpred2_CMIS562);
        tableName();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred2_CMIS

    // $ANTLR start synpred3_CMIS
    public final void synpred3_CMIS_fragment() throws RecognitionException {   
        // W:\\workspace-cmis\\ANTLR\\CMIS.g:221:17: ( joinedTable )
        // W:\\workspace-cmis\\ANTLR\\CMIS.g:221:18: joinedTable
        {
        pushFollow(FOLLOW_joinedTable_in_synpred3_CMIS640);
        joinedTable();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred3_CMIS

    // $ANTLR start synpred4_CMIS
    public final void synpred4_CMIS_fragment() throws RecognitionException {   
        // W:\\workspace-cmis\\ANTLR\\CMIS.g:236:35: ( joinSpecification )
        // W:\\workspace-cmis\\ANTLR\\CMIS.g:236:36: joinSpecification
        {
        pushFollow(FOLLOW_joinSpecification_in_synpred4_CMIS737);
        joinSpecification();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred4_CMIS

    // $ANTLR start synpred5_CMIS
    public final void synpred5_CMIS_fragment() throws RecognitionException {   
        // W:\\workspace-cmis\\ANTLR\\CMIS.g:351:6: ( columnReference )
        // W:\\workspace-cmis\\ANTLR\\CMIS.g:351:7: columnReference
        {
        pushFollow(FOLLOW_columnReference_in_synpred5_CMIS1315);
        columnReference();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred5_CMIS

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
    public final boolean synpred5_CMIS() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred5_CMIS_fragment(); // can never throw exception
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


    protected DFA25 dfa25 = new DFA25(this);
    protected DFA27 dfa27 = new DFA27(this);
    protected DFA41 dfa41 = new DFA41(this);
    static final String DFA25_eotS =
        "\31\uffff";
    static final String DFA25_eofS =
        "\31\uffff";
    static final String DFA25_minS =
        "\2\35\2\uffff\1\35\1\42\1\35\3\44\2\103\2\104\1\44\1\35\1\44\1"+
        "\35\1\42\4\104\2\44";
    static final String DFA25_maxS =
        "\2\111\2\uffff\1\111\1\106\1\111\3\106\4\104\1\106\1\111\1\106"+
        "\1\111\1\106\4\104\2\106";
    static final String DFA25_acceptS =
        "\2\uffff\1\2\1\1\25\uffff";
    static final String DFA25_specialS =
        "\31\uffff}>";
    static final String[] DFA25_transitionS = {
            "\1\2\2\uffff\1\2\2\uffff\1\2\1\uffff\6\2\1\uffff\3\2\1\1\5"+
            "\uffff\25\2",
            "\1\3\2\uffff\1\3\2\uffff\1\4\1\uffff\6\3\1\uffff\4\3\5\uffff"+
            "\25\3",
            "",
            "",
            "\1\3\2\uffff\1\3\2\uffff\1\3\1\2\6\3\1\uffff\4\3\5\uffff\14"+
            "\3\1\12\1\11\1\5\1\6\1\7\1\10\3\3",
            "\1\13\1\3\1\2\6\uffff\1\3\3\uffff\11\3\11\uffff\6\2",
            "\1\14\2\uffff\1\14\4\uffff\6\14\1\uffff\4\14\5\uffff\14\14"+
            "\2\uffff\1\15\3\uffff\3\14",
            "\1\2\6\uffff\1\3\4\uffff\5\3\14\uffff\6\2",
            "\1\2\6\uffff\1\3\4\uffff\5\3\14\uffff\6\2",
            "\1\2\6\uffff\1\3\4\uffff\5\3\14\uffff\6\2",
            "\1\16\1\17",
            "\1\20\1\21",
            "\1\22",
            "\1\22",
            "\1\2\6\uffff\1\3\4\uffff\5\3\14\uffff\6\2",
            "\1\23\2\uffff\1\23\4\uffff\6\23\1\uffff\4\23\5\uffff\14\23"+
            "\2\uffff\1\24\3\uffff\3\23",
            "\1\2\6\uffff\1\3\3\uffff\11\3\11\uffff\6\2",
            "\1\25\2\uffff\1\25\4\uffff\6\25\1\uffff\4\25\5\uffff\14\25"+
            "\2\uffff\1\26\3\uffff\3\25",
            "\1\13\1\uffff\1\2\6\uffff\1\3\3\uffff\11\3\11\uffff\6\2",
            "\1\27",
            "\1\27",
            "\1\30",
            "\1\30",
            "\1\2\6\uffff\1\3\4\uffff\5\3\14\uffff\6\2",
            "\1\2\6\uffff\1\3\3\uffff\11\3\11\uffff\6\2"
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
            return "286:1: searchNotCondition : ( NOT searchTest -> ^( NEGATION searchTest ) | searchTest -> searchTest );";
        }
    }
    static final String DFA27_eotS =
        "\61\uffff";
    static final String DFA27_eofS =
        "\46\uffff\1\32\2\uffff\1\36\2\uffff\1\36\4\uffff";
    static final String DFA27_minS =
        "\1\35\1\42\1\35\1\43\1\uffff\3\43\1\uffff\1\65\2\uffff\1\103\1"+
        "\uffff\2\104\1\uffff\3\37\1\53\1\35\1\42\1\37\1\35\1\44\1\uffff"+
        "\1\37\1\35\1\44\1\uffff\1\37\1\35\1\44\4\104\1\44\2\104\1\44\2\104"+
        "\1\44\1\53\3\37";
    static final String DFA27_maxS =
        "\1\111\1\67\1\111\1\104\1\uffff\3\43\1\uffff\1\66\2\uffff\1\104"+
        "\1\uffff\2\104\1\uffff\3\106\1\67\1\111\1\67\1\106\1\111\1\106\1"+
        "\uffff\1\106\1\111\1\106\1\uffff\1\106\1\111\1\106\4\104\1\75\2"+
        "\104\1\75\2\104\1\75\1\67\3\106";
    static final String DFA27_acceptS =
        "\4\uffff\1\5\3\uffff\1\1\1\uffff\1\2\1\3\1\uffff\1\4\2\uffff\1"+
        "\6\11\uffff\1\7\3\uffff\1\10\22\uffff";
    static final String DFA27_specialS =
        "\61\uffff}>";
    static final String[] DFA27_transitionS = {
            "\1\10\2\uffff\1\10\4\uffff\6\10\1\uffff\4\10\5\uffff\4\10\1"+
            "\3\1\5\1\6\1\7\4\10\2\4\1\1\1\2\2\4\3\10",
            "\1\14\1\10\7\uffff\1\10\3\uffff\1\11\5\10\1\12\1\13\1\15",
            "\1\16\2\uffff\1\16\4\uffff\6\16\1\uffff\4\16\5\uffff\14\16"+
            "\2\uffff\1\17\3\uffff\3\16",
            "\1\10\37\uffff\2\20",
            "",
            "\1\21",
            "\1\22",
            "\1\23",
            "",
            "\1\12\1\13",
            "",
            "",
            "\1\24\1\25",
            "",
            "\1\26",
            "\1\26",
            "",
            "\1\32\4\uffff\1\10\34\uffff\1\10\1\31\1\27\1\30\2\10",
            "\1\36\4\uffff\1\10\34\uffff\1\10\1\35\1\33\1\34\2\10",
            "\1\36\4\uffff\1\10\34\uffff\1\10\1\41\1\37\1\40\2\10",
            "\1\10\3\uffff\1\11\5\10\1\12\1\13\1\15",
            "\1\42\2\uffff\1\42\4\uffff\6\42\1\uffff\4\42\5\uffff\14\42"+
            "\2\uffff\1\43\3\uffff\3\42",
            "\1\14\10\uffff\1\10\3\uffff\1\11\5\10\1\12\1\13\1\15",
            "\1\32\2\uffff\1\10\1\uffff\1\10\34\uffff\6\10",
            "\1\44\2\uffff\1\44\4\uffff\6\44\1\uffff\4\44\5\uffff\14\44"+
            "\2\uffff\1\45\3\uffff\3\44",
            "\1\46\34\uffff\6\10",
            "",
            "\1\36\2\uffff\1\10\1\uffff\1\10\34\uffff\6\10",
            "\1\47\2\uffff\1\47\4\uffff\6\47\1\uffff\4\47\5\uffff\14\47"+
            "\2\uffff\1\50\3\uffff\3\47",
            "\1\51\34\uffff\6\10",
            "",
            "\1\36\2\uffff\1\10\1\uffff\1\10\34\uffff\6\10",
            "\1\52\2\uffff\1\52\4\uffff\6\52\1\uffff\4\52\5\uffff\14\52"+
            "\2\uffff\1\53\3\uffff\3\52",
            "\1\54\34\uffff\6\10",
            "\1\55",
            "\1\55",
            "\1\56",
            "\1\56",
            "\1\32\6\uffff\1\10\1\uffff\2\32\1\uffff\5\10\10\uffff\1\32",
            "\1\57",
            "\1\57",
            "\1\36\6\uffff\1\10\1\uffff\2\36\1\uffff\5\10\10\uffff\1\36",
            "\1\60",
            "\1\60",
            "\1\36\6\uffff\1\10\1\uffff\2\36\1\uffff\5\10\10\uffff\1\36",
            "\1\10\3\uffff\1\11\5\10\1\12\1\13\1\15",
            "\1\32\2\uffff\1\10\1\uffff\1\10\34\uffff\6\10",
            "\1\36\2\uffff\1\10\1\uffff\1\10\34\uffff\6\10",
            "\1\36\2\uffff\1\10\1\uffff\1\10\34\uffff\6\10"
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
            return "300:1: predicate : ( comparisonPredicate | inPredicate | likePredicate | nullPredicate | quantifiedComparisonPredicate | quantifiedInPredicate | textSearchPredicate | folderPredicate );";
        }
    }
    static final String DFA41_eotS =
        "\16\uffff";
    static final String DFA41_eofS =
        "\1\uffff\1\4\6\uffff\1\4\1\uffff\1\4\2\uffff\1\4";
    static final String DFA41_minS =
        "\1\103\1\37\1\35\1\103\2\uffff\2\104\1\37\1\35\1\37\2\104\1\37";
    static final String DFA41_maxS =
        "\1\104\1\100\1\111\1\104\2\uffff\2\104\1\100\1\111\1\100\2\104"+
        "\1\100";
    static final String DFA41_acceptS =
        "\4\uffff\1\1\1\2\10\uffff";
    static final String DFA41_specialS =
        "\16\uffff}>";
    static final String[] DFA41_transitionS = {
            "\1\1\1\2",
            "\1\4\2\uffff\1\3\34\uffff\2\5",
            "\1\6\2\uffff\1\6\4\uffff\6\6\1\uffff\4\6\5\uffff\14\6\2\uffff"+
            "\1\7\3\uffff\3\6",
            "\1\10\1\11",
            "",
            "",
            "\1\12",
            "\1\12",
            "\1\4\37\uffff\2\5",
            "\1\13\2\uffff\1\13\4\uffff\6\13\1\uffff\4\13\5\uffff\14\13"+
            "\2\uffff\1\14\3\uffff\3\13",
            "\1\4\2\uffff\1\3\34\uffff\2\5",
            "\1\15",
            "\1\15",
            "\1\4\37\uffff\2\5"
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
            return "390:1: sortSpecification : ( columnReference -> ^( SORT_SPECIFICATION columnReference ASC ) | columnReference (by= ASC | by= DESC ) -> ^( SORT_SPECIFICATION columnReference $by) );";
        }
    }
 

    public static final BitSet FOLLOW_SELECT_in_query172 = new BitSet(new long[]{0xFFE0F7E160000000L,0x0000000000000399L});
    public static final BitSet FOLLOW_selectList_in_query174 = new BitSet(new long[]{0x0000002000000000L});
    public static final BitSet FOLLOW_fromClause_in_query176 = new BitSet(new long[]{0x2000100000000000L});
    public static final BitSet FOLLOW_whereClause_in_query178 = new BitSet(new long[]{0x2000000000000000L});
    public static final BitSet FOLLOW_orderByClause_in_query181 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_query184 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STAR_in_selectList233 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_selectSubList_in_selectList249 = new BitSet(new long[]{0x0000000080000002L});
    public static final BitSet FOLLOW_COMMA_in_selectList253 = new BitSet(new long[]{0xFFE0F7E160000000L,0x0000000000000399L});
    public static final BitSet FOLLOW_selectSubList_in_selectList255 = new BitSet(new long[]{0x0000000080000002L});
    public static final BitSet FOLLOW_valueExpression_in_selectSubList291 = new BitSet(new long[]{0xFFE0F7E160000002L,0x0000000000000399L});
    public static final BitSet FOLLOW_AS_in_selectSubList295 = new BitSet(new long[]{0xFFE0F7E160000000L,0x0000000000000399L});
    public static final BitSet FOLLOW_columnName_in_selectSubList298 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_qualifier_in_selectSubList319 = new BitSet(new long[]{0x0000000200000000L});
    public static final BitSet FOLLOW_DOTSTAR_in_selectSubList321 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_multiValuedColumnReference_in_selectSubList337 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_columnReference_in_valueExpression356 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_valueFunction_in_valueExpression369 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_qualifier_in_columnReference392 = new BitSet(new long[]{0x0000000400000000L});
    public static final BitSet FOLLOW_DOT_in_columnReference394 = new BitSet(new long[]{0xFFE0F7E160000000L,0x0000000000000399L});
    public static final BitSet FOLLOW_columnName_in_columnReference399 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_qualifier_in_multiValuedColumnReference435 = new BitSet(new long[]{0x0000000400000000L});
    public static final BitSet FOLLOW_DOT_in_multiValuedColumnReference437 = new BitSet(new long[]{0xFFE0F7E160000000L,0x0000000000000399L});
    public static final BitSet FOLLOW_multiValuedColumnName_in_multiValuedColumnReference443 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_keyWordOrId_in_valueFunction470 = new BitSet(new long[]{0x0000000800000000L});
    public static final BitSet FOLLOW_LPAREN_in_valueFunction472 = new BitSet(new long[]{0xFFE0F7F160000000L,0x00000000000003FFL});
    public static final BitSet FOLLOW_functionArgument_in_valueFunction474 = new BitSet(new long[]{0xFFE0F7F160000000L,0x00000000000003FFL});
    public static final BitSet FOLLOW_RPAREN_in_valueFunction477 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_qualifier_in_functionArgument508 = new BitSet(new long[]{0x0000000400000000L});
    public static final BitSet FOLLOW_DOT_in_functionArgument510 = new BitSet(new long[]{0xFFE0F7E160000000L,0x0000000000000399L});
    public static final BitSet FOLLOW_columnName_in_functionArgument512 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_identifier_in_functionArgument536 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_literalOrParameterName_in_functionArgument546 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_tableName_in_qualifier567 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_correlationName_in_qualifier579 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FROM_in_fromClause616 = new BitSet(new long[]{0xFFE0F7E960000000L,0x0000000000000399L});
    public static final BitSet FOLLOW_tableReference_in_fromClause618 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_singleTable_in_tableReference636 = new BitSet(new long[]{0x000001C000000002L});
    public static final BitSet FOLLOW_joinedTable_in_tableReference645 = new BitSet(new long[]{0x000001C000000002L});
    public static final BitSet FOLLOW_tableName_in_singleTable674 = new BitSet(new long[]{0xFFE0F7E160000002L,0x0000000000000399L});
    public static final BitSet FOLLOW_AS_in_singleTable678 = new BitSet(new long[]{0xFFE0F7E160000000L,0x0000000000000399L});
    public static final BitSet FOLLOW_correlationName_in_singleTable681 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_singleTable702 = new BitSet(new long[]{0xFFE0F7E960000000L,0x0000000000000399L});
    public static final BitSet FOLLOW_joinedTables_in_singleTable704 = new BitSet(new long[]{0x0000001000000000L});
    public static final BitSet FOLLOW_RPAREN_in_singleTable706 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_joinType_in_joinedTable728 = new BitSet(new long[]{0x0000004000000000L});
    public static final BitSet FOLLOW_JOIN_in_joinedTable731 = new BitSet(new long[]{0xFFE0F7E960000000L,0x0000000000000399L});
    public static final BitSet FOLLOW_tableReference_in_joinedTable733 = new BitSet(new long[]{0x0000040000000002L});
    public static final BitSet FOLLOW_joinSpecification_in_joinedTable742 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_singleTable_in_joinedTables773 = new BitSet(new long[]{0x000001C000000000L});
    public static final BitSet FOLLOW_joinedTable_in_joinedTables775 = new BitSet(new long[]{0x000001C000000002L});
    public static final BitSet FOLLOW_INNER_in_joinType802 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LEFT_in_joinType814 = new BitSet(new long[]{0x0000020000000002L});
    public static final BitSet FOLLOW_OUTER_in_joinType816 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ON_in_joinSpecification836 = new BitSet(new long[]{0x0000000800000000L});
    public static final BitSet FOLLOW_LPAREN_in_joinSpecification838 = new BitSet(new long[]{0xFFE0F7E160000000L,0x0000000000000399L});
    public static final BitSet FOLLOW_columnReference_in_joinSpecification842 = new BitSet(new long[]{0x0000080000000000L});
    public static final BitSet FOLLOW_EQUALS_in_joinSpecification844 = new BitSet(new long[]{0xFFE0F7E160000000L,0x0000000000000399L});
    public static final BitSet FOLLOW_columnReference_in_joinSpecification848 = new BitSet(new long[]{0x0000001000000000L});
    public static final BitSet FOLLOW_RPAREN_in_joinSpecification850 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_WHERE_in_whereClause900 = new BitSet(new long[]{0xFFE0F7F960000000L,0x00000000000003FFL});
    public static final BitSet FOLLOW_searchOrCondition_in_whereClause902 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_searchAndCondition_in_searchOrCondition922 = new BitSet(new long[]{0x0000200000000002L});
    public static final BitSet FOLLOW_OR_in_searchOrCondition925 = new BitSet(new long[]{0xFFE0F7F960000000L,0x00000000000003FFL});
    public static final BitSet FOLLOW_searchAndCondition_in_searchOrCondition927 = new BitSet(new long[]{0x0000200000000002L});
    public static final BitSet FOLLOW_searchNotCondition_in_searchAndCondition955 = new BitSet(new long[]{0x0000400000000002L});
    public static final BitSet FOLLOW_AND_in_searchAndCondition958 = new BitSet(new long[]{0xFFE0F7F960000000L,0x00000000000003FFL});
    public static final BitSet FOLLOW_searchNotCondition_in_searchAndCondition960 = new BitSet(new long[]{0x0000400000000002L});
    public static final BitSet FOLLOW_NOT_in_searchNotCondition987 = new BitSet(new long[]{0xFFE0F7F960000000L,0x00000000000003FFL});
    public static final BitSet FOLLOW_searchTest_in_searchNotCondition989 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_searchTest_in_searchNotCondition1004 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_predicate_in_searchTest1022 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_searchTest1033 = new BitSet(new long[]{0xFFE0F7F960000000L,0x00000000000003FFL});
    public static final BitSet FOLLOW_searchOrCondition_in_searchTest1035 = new BitSet(new long[]{0x0000001000000000L});
    public static final BitSet FOLLOW_RPAREN_in_searchTest1037 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_comparisonPredicate_in_predicate1054 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_inPredicate_in_predicate1059 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_likePredicate_in_predicate1064 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_nullPredicate_in_predicate1069 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_quantifiedComparisonPredicate_in_predicate1075 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_quantifiedInPredicate_in_predicate1080 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_textSearchPredicate_in_predicate1085 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_folderPredicate_in_predicate1090 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_valueExpression_in_comparisonPredicate1102 = new BitSet(new long[]{0x001F080000000000L});
    public static final BitSet FOLLOW_compOp_in_comparisonPredicate1104 = new BitSet(new long[]{0xFFE0F7F160000000L,0x00000000000003FFL});
    public static final BitSet FOLLOW_literalOrParameterName_in_comparisonPredicate1106 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_compOp0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_literal_in_literalOrParameterName1172 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_parameterName_in_literalOrParameterName1177 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_signedNumericLiteral_in_literal1190 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_characterStringLiteral_in_literal1195 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_columnReference_in_inPredicate1207 = new BitSet(new long[]{0x0020800000000000L});
    public static final BitSet FOLLOW_NOT_in_inPredicate1209 = new BitSet(new long[]{0x0020000000000000L});
    public static final BitSet FOLLOW_IN_in_inPredicate1212 = new BitSet(new long[]{0x0000000800000000L});
    public static final BitSet FOLLOW_LPAREN_in_inPredicate1214 = new BitSet(new long[]{0xFFE0F7F160000000L,0x00000000000003FFL});
    public static final BitSet FOLLOW_inValueList_in_inPredicate1216 = new BitSet(new long[]{0x0000001000000000L});
    public static final BitSet FOLLOW_RPAREN_in_inPredicate1218 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_literalOrParameterName_in_inValueList1247 = new BitSet(new long[]{0x0000000080000002L});
    public static final BitSet FOLLOW_COMMA_in_inValueList1250 = new BitSet(new long[]{0xFFE0F7F160000000L,0x00000000000003FFL});
    public static final BitSet FOLLOW_literalOrParameterName_in_inValueList1252 = new BitSet(new long[]{0x0000000080000002L});
    public static final BitSet FOLLOW_columnReference_in_likePredicate1278 = new BitSet(new long[]{0x0040800000000000L});
    public static final BitSet FOLLOW_NOT_in_likePredicate1280 = new BitSet(new long[]{0x0040000000000000L});
    public static final BitSet FOLLOW_LIKE_in_likePredicate1283 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000064L});
    public static final BitSet FOLLOW_characterStringLiteral_in_likePredicate1285 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_columnReference_in_nullPredicate1319 = new BitSet(new long[]{0x0080000000000000L});
    public static final BitSet FOLLOW_multiValuedColumnReference_in_nullPredicate1323 = new BitSet(new long[]{0x0080000000000000L});
    public static final BitSet FOLLOW_IS_in_nullPredicate1326 = new BitSet(new long[]{0x0100800000000000L});
    public static final BitSet FOLLOW_NOT_in_nullPredicate1328 = new BitSet(new long[]{0x0100000000000000L});
    public static final BitSet FOLLOW_NULL_in_nullPredicate1331 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_literalOrParameterName_in_quantifiedComparisonPredicate1356 = new BitSet(new long[]{0x001F080000000000L});
    public static final BitSet FOLLOW_compOp_in_quantifiedComparisonPredicate1358 = new BitSet(new long[]{0x0200000000000000L});
    public static final BitSet FOLLOW_ANY_in_quantifiedComparisonPredicate1360 = new BitSet(new long[]{0xFFE0F7E160000000L,0x0000000000000399L});
    public static final BitSet FOLLOW_multiValuedColumnReference_in_quantifiedComparisonPredicate1362 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ANY_in_quantifiedInPredicate1391 = new BitSet(new long[]{0xFFE0F7E160000000L,0x0000000000000399L});
    public static final BitSet FOLLOW_multiValuedColumnReference_in_quantifiedInPredicate1393 = new BitSet(new long[]{0x0020800000000000L});
    public static final BitSet FOLLOW_NOT_in_quantifiedInPredicate1395 = new BitSet(new long[]{0x0020000000000000L});
    public static final BitSet FOLLOW_IN_in_quantifiedInPredicate1398 = new BitSet(new long[]{0x0000000800000000L});
    public static final BitSet FOLLOW_LPAREN_in_quantifiedInPredicate1401 = new BitSet(new long[]{0xFFE0F7F160000000L,0x00000000000003FFL});
    public static final BitSet FOLLOW_inValueList_in_quantifiedInPredicate1403 = new BitSet(new long[]{0x0000001000000000L});
    public static final BitSet FOLLOW_RPAREN_in_quantifiedInPredicate1405 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_CONTAINS_in_textSearchPredicate1434 = new BitSet(new long[]{0x0000000800000000L});
    public static final BitSet FOLLOW_LPAREN_in_textSearchPredicate1436 = new BitSet(new long[]{0xFFE0F7E1E0000000L,0x000000000000039DL});
    public static final BitSet FOLLOW_qualifier_in_textSearchPredicate1439 = new BitSet(new long[]{0x0000000080000000L});
    public static final BitSet FOLLOW_COMMA_in_textSearchPredicate1441 = new BitSet(new long[]{0xFFE0F7E1E0000000L,0x000000000000039DL});
    public static final BitSet FOLLOW_COMMA_in_textSearchPredicate1445 = new BitSet(new long[]{0xFFE0F7E1E0000000L,0x000000000000039DL});
    public static final BitSet FOLLOW_textSearchExpression_in_textSearchPredicate1449 = new BitSet(new long[]{0x0000001000000000L});
    public static final BitSet FOLLOW_RPAREN_in_textSearchPredicate1451 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IN_FOLDER_in_folderPredicate1476 = new BitSet(new long[]{0x0000000800000000L});
    public static final BitSet FOLLOW_folderPredicateArgs_in_folderPredicate1479 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IN_TREE_in_folderPredicate1500 = new BitSet(new long[]{0x0000000800000000L});
    public static final BitSet FOLLOW_folderPredicateArgs_in_folderPredicate1502 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_folderPredicateArgs1524 = new BitSet(new long[]{0xFFE0F7E1E0000000L,0x00000000000003FDL});
    public static final BitSet FOLLOW_qualifier_in_folderPredicateArgs1527 = new BitSet(new long[]{0x0000000080000000L});
    public static final BitSet FOLLOW_COMMA_in_folderPredicateArgs1529 = new BitSet(new long[]{0xFFE0F7E1E0000000L,0x00000000000003FDL});
    public static final BitSet FOLLOW_COMMA_in_folderPredicateArgs1533 = new BitSet(new long[]{0xFFE0F7E1E0000000L,0x00000000000003FDL});
    public static final BitSet FOLLOW_folderId_in_folderPredicateArgs1537 = new BitSet(new long[]{0x0000001000000000L});
    public static final BitSet FOLLOW_RPAREN_in_folderPredicateArgs1539 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ORDER_in_orderByClause1578 = new BitSet(new long[]{0x4000000000000000L});
    public static final BitSet FOLLOW_BY_in_orderByClause1580 = new BitSet(new long[]{0xFFE0F7E160000000L,0x0000000000000399L});
    public static final BitSet FOLLOW_sortSpecification_in_orderByClause1582 = new BitSet(new long[]{0x0000000080000002L});
    public static final BitSet FOLLOW_COMMA_in_orderByClause1586 = new BitSet(new long[]{0xFFE0F7E160000000L,0x0000000000000399L});
    public static final BitSet FOLLOW_sortSpecification_in_orderByClause1588 = new BitSet(new long[]{0x0000000080000002L});
    public static final BitSet FOLLOW_columnReference_in_sortSpecification1614 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_columnReference_in_sortSpecification1632 = new BitSet(new long[]{0x8000000000000000L,0x0000000000000001L});
    public static final BitSet FOLLOW_ASC_in_sortSpecification1638 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DESC_in_sortSpecification1644 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_identifier_in_correlationName1671 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_identifier_in_tableName1685 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_identifier_in_columnName1703 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_identifier_in_multiValuedColumnName1722 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_COLON_in_parameterName1740 = new BitSet(new long[]{0xFFE0F7E160000000L,0x0000000000000399L});
    public static final BitSet FOLLOW_identifier_in_parameterName1742 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_characterStringLiteral_in_folderId1765 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_QUOTED_STRING_in_textSearchExpression1786 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_identifier1798 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DOUBLE_QUOTE_in_identifier1809 = new BitSet(new long[]{0xFFE0F7E120000000L,0x0000000000000399L});
    public static final BitSet FOLLOW_keyWordOrId_in_identifier1811 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000010L});
    public static final BitSet FOLLOW_DOUBLE_QUOTE_in_identifier1813 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FLOATING_POINT_LITERAL_in_signedNumericLiteral1833 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_integerLiteral_in_signedNumericLiteral1848 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DECIMAL_INTEGER_LITERAL_in_integerLiteral1867 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_QUOTED_STRING_in_characterStringLiteral1890 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_keyWord0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_keyWord_in_keyWordOrId2099 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_keyWordOrId2111 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_valueExpression_in_synpred1_CMIS287 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_tableName_in_synpred2_CMIS562 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_joinedTable_in_synpred3_CMIS640 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_joinSpecification_in_synpred4_CMIS737 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_columnReference_in_synpred5_CMIS1315 = new BitSet(new long[]{0x0000000000000002L});

}