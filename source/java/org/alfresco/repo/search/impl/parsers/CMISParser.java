// $ANTLR 3.1.2 /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g 2009-07-22 13:49:02
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
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "QUERY", "ALL_COLUMNS", "COLUMN", "COLUMNS", "COLUMN_REF", "QUALIFIER", "FUNCTION", "SOURCE", "TABLE", "TABLE_REF", "PARAMETER", "CONJUNCTION", "DISJUNCTION", "NEGATION", "PRED_COMPARISON", "PRED_IN", "PRED_EXISTS", "PRED_LIKE", "PRED_FTS", "LIST", "PRED_CHILD", "PRED_DESCENDANT", "SORT_SPECIFICATION", "NUMERIC_LITERAL", "STRING_LITERAL", "SELECT", "STAR", "COMMA", "AS", "DOTSTAR", "DOT", "LPAREN", "RPAREN", "FROM", "JOIN", "INNER", "LEFT", "OUTER", "ON", "EQUALS", "WHERE", "OR", "AND", "NOT", "NOTEQUALS", "LESSTHAN", "GREATERTHAN", "LESSTHANOREQUALS", "GREATERTHANOREQUALS", "IN", "LIKE", "IS", "NULL", "ANY", "CONTAINS", "IN_FOLDER", "IN_TREE", "ORDER", "BY", "ASC", "DESC", "COLON", "QUOTED_STRING", "ID", "DOUBLE_QUOTE", "FLOATING_POINT_LITERAL", "DECIMAL_INTEGER_LITERAL", "UPPER", "LOWER", "SCORE", "DOTDOT", "TILDA", "PLUS", "MINUS", "DECIMAL_NUMERAL", "DIGIT", "EXPONENT", "WS", "ZERO_DIGIT", "NON_ZERO_DIGIT", "E", "SIGNED_INTEGER"
    };
    public static final int COMMA=31;
    public static final int LESSTHAN=49;
    public static final int MINUS=77;
    public static final int PRED_COMPARISON=18;
    public static final int AS=32;
    public static final int LIST=23;
    public static final int TABLE_REF=13;
    public static final int DECIMAL_INTEGER_LITERAL=70;
    public static final int QUERY=4;
    public static final int INNER=39;
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
    public static final int ID=67;
    public static final int ALL_COLUMNS=5;
    public static final int FROM=37;
    public static final int PRED_IN=19;
    public static final int NON_ZERO_DIGIT=83;
    public static final int PRED_EXISTS=20;
    public static final int PRED_DESCENDANT=25;
    public static final int NOTEQUALS=48;
    public static final int NEGATION=17;
    public static final int WS=81;
    public static final int IS=55;
    public static final int IN_FOLDER=59;
    public static final int DOUBLE_QUOTE=68;
    public static final int LOWER=72;
    public static final int ASC=63;
    public static final int DOTSTAR=33;
    public static final int SOURCE=11;
    public static final int LIKE=54;
    public static final int DISJUNCTION=16;
    public static final int PRED_LIKE=21;
    public static final int FLOATING_POINT_LITERAL=69;
    public static final int ANY=57;
    public static final int IN=53;
    public static final int DECIMAL_NUMERAL=78;
    public static final int EQUALS=43;
    public static final int IN_TREE=60;
    public static final int TABLE=12;
    public static final int EXPONENT=80;
    public static final int UPPER=71;
    public static final int QUOTED_STRING=66;
    public static final int COLUMNS=7;
    public static final int EOF=-1;
    public static final int NULL=56;
    public static final int PARAMETER=14;
    public static final int COLON=65;
    public static final int DOTDOT=74;
    public static final int NUMERIC_LITERAL=27;
    public static final int STAR=30;
    public static final int COLUMN=6;
    public static final int SCORE=73;
    public static final int SIGNED_INTEGER=85;
    public static final int NOT=47;
    public static final int TILDA=75;
    public static final int E=84;
    public static final int WHERE=44;

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
    public String getGrammarFileName() { return "/Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g"; }


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


    public static class query_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "query"
    // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:142:1: query : SELECT selectList fromClause ( whereClause )? ( orderByClause )? EOF -> ^( QUERY selectList fromClause ( whereClause )? ( orderByClause )? ) ;
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
        RewriteRuleSubtreeStream stream_fromClause=new RewriteRuleSubtreeStream(adaptor,"rule fromClause");
        RewriteRuleSubtreeStream stream_whereClause=new RewriteRuleSubtreeStream(adaptor,"rule whereClause");
        RewriteRuleSubtreeStream stream_selectList=new RewriteRuleSubtreeStream(adaptor,"rule selectList");
        RewriteRuleSubtreeStream stream_orderByClause=new RewriteRuleSubtreeStream(adaptor,"rule orderByClause");
        try {
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:149:2: ( SELECT selectList fromClause ( whereClause )? ( orderByClause )? EOF -> ^( QUERY selectList fromClause ( whereClause )? ( orderByClause )? ) )
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:149:4: SELECT selectList fromClause ( whereClause )? ( orderByClause )? EOF
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
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:149:33: ( whereClause )?
            int alt1=2;
            int LA1_0 = input.LA(1);

            if ( (LA1_0==WHERE) ) {
                alt1=1;
            }
            switch (alt1) {
                case 1 :
                    // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:149:33: whereClause
                    {
                    pushFollow(FOLLOW_whereClause_in_query178);
                    whereClause4=whereClause();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_whereClause.add(whereClause4.getTree());

                    }
                    break;

            }

            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:149:46: ( orderByClause )?
            int alt2=2;
            int LA2_0 = input.LA(1);

            if ( (LA2_0==ORDER) ) {
                alt2=1;
            }
            switch (alt2) {
                case 1 :
                    // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:149:46: orderByClause
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
            // elements: whereClause, fromClause, selectList, orderByClause
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 150:3: -> ^( QUERY selectList fromClause ( whereClause )? ( orderByClause )? )
            {
                // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:150:6: ^( QUERY selectList fromClause ( whereClause )? ( orderByClause )? )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(QUERY, "QUERY"), root_1);

                adaptor.addChild(root_1, stream_selectList.nextTree());
                adaptor.addChild(root_1, stream_fromClause.nextTree());
                // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:150:36: ( whereClause )?
                if ( stream_whereClause.hasNext() ) {
                    adaptor.addChild(root_1, stream_whereClause.nextTree());

                }
                stream_whereClause.reset();
                // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:150:49: ( orderByClause )?
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
        }

        catch(RecognitionException e)
        {
           throw e;
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
    // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:154:1: selectList : ( STAR -> ^( ALL_COLUMNS ) | selectSubList ( COMMA selectSubList )* -> ^( COLUMNS ( selectSubList )+ ) );
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
        RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
        RewriteRuleTokenStream stream_STAR=new RewriteRuleTokenStream(adaptor,"token STAR");
        RewriteRuleSubtreeStream stream_selectSubList=new RewriteRuleSubtreeStream(adaptor,"rule selectSubList");
            paraphrases.push("in select list"); 
        try {
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:157:2: ( STAR -> ^( ALL_COLUMNS ) | selectSubList ( COMMA selectSubList )* -> ^( COLUMNS ( selectSubList )+ ) )
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
                    // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:157:4: STAR
                    {
                    STAR7=(Token)match(input,STAR,FOLLOW_STAR_in_selectList233); if (state.failed) return retval; 
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
                    // 158:3: -> ^( ALL_COLUMNS )
                    {
                        // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:158:6: ^( ALL_COLUMNS )
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
                    // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:159:5: selectSubList ( COMMA selectSubList )*
                    {
                    pushFollow(FOLLOW_selectSubList_in_selectList249);
                    selectSubList8=selectSubList();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_selectSubList.add(selectSubList8.getTree());
                    // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:159:19: ( COMMA selectSubList )*
                    loop3:
                    do {
                        int alt3=2;
                        int LA3_0 = input.LA(1);

                        if ( (LA3_0==COMMA) ) {
                            alt3=1;
                        }


                        switch (alt3) {
                        case 1 :
                            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:159:21: COMMA selectSubList
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
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 160:3: -> ^( COLUMNS ( selectSubList )+ )
                    {
                        // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:160:6: ^( COLUMNS ( selectSubList )+ )
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
           throw e;
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
    // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:164:1: selectSubList : ( ( valueExpression )=> valueExpression ( ( AS )? columnName )? -> ^( COLUMN valueExpression ( columnName )? ) | qualifier DOTSTAR -> ^( ALL_COLUMNS qualifier ) | multiValuedColumnReference ->);
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
        RewriteRuleTokenStream stream_DOTSTAR=new RewriteRuleTokenStream(adaptor,"token DOTSTAR");
        RewriteRuleTokenStream stream_AS=new RewriteRuleTokenStream(adaptor,"token AS");
        RewriteRuleSubtreeStream stream_valueExpression=new RewriteRuleSubtreeStream(adaptor,"rule valueExpression");
        RewriteRuleSubtreeStream stream_columnName=new RewriteRuleSubtreeStream(adaptor,"rule columnName");
        RewriteRuleSubtreeStream stream_qualifier=new RewriteRuleSubtreeStream(adaptor,"rule qualifier");
        RewriteRuleSubtreeStream stream_multiValuedColumnReference=new RewriteRuleSubtreeStream(adaptor,"rule multiValuedColumnReference");
        try {
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:165:2: ( ( valueExpression )=> valueExpression ( ( AS )? columnName )? -> ^( COLUMN valueExpression ( columnName )? ) | qualifier DOTSTAR -> ^( ALL_COLUMNS qualifier ) | multiValuedColumnReference ->)
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
                    // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:165:4: ( valueExpression )=> valueExpression ( ( AS )? columnName )?
                    {
                    pushFollow(FOLLOW_valueExpression_in_selectSubList291);
                    valueExpression11=valueExpression();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_valueExpression.add(valueExpression11.getTree());
                    // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:165:40: ( ( AS )? columnName )?
                    int alt6=2;
                    int LA6_0 = input.LA(1);

                    if ( (LA6_0==AS||(LA6_0>=ID && LA6_0<=DOUBLE_QUOTE)) ) {
                        alt6=1;
                    }
                    switch (alt6) {
                        case 1 :
                            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:165:42: ( AS )? columnName
                            {
                            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:165:42: ( AS )?
                            int alt5=2;
                            int LA5_0 = input.LA(1);

                            if ( (LA5_0==AS) ) {
                                alt5=1;
                            }
                            switch (alt5) {
                                case 1 :
                                    // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:165:42: AS
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
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 166:3: -> ^( COLUMN valueExpression ( columnName )? )
                    {
                        // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:166:6: ^( COLUMN valueExpression ( columnName )? )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(COLUMN, "COLUMN"), root_1);

                        adaptor.addChild(root_1, stream_valueExpression.nextTree());
                        // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:166:31: ( columnName )?
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
                    // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:167:4: qualifier DOTSTAR
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
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 168:3: -> ^( ALL_COLUMNS qualifier )
                    {
                        // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:168:6: ^( ALL_COLUMNS qualifier )
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
                    // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:169:4: multiValuedColumnReference
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
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 170:3: ->
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
           throw e;
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
    // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:173:1: valueExpression : ( columnReference -> columnReference | valueFunction -> valueFunction );
    public final CMISParser.valueExpression_return valueExpression() throws RecognitionException {
        CMISParser.valueExpression_return retval = new CMISParser.valueExpression_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        CMISParser.columnReference_return columnReference17 = null;

        CMISParser.valueFunction_return valueFunction18 = null;


        RewriteRuleSubtreeStream stream_columnReference=new RewriteRuleSubtreeStream(adaptor,"rule columnReference");
        RewriteRuleSubtreeStream stream_valueFunction=new RewriteRuleSubtreeStream(adaptor,"rule valueFunction");
        try {
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:174:2: ( columnReference -> columnReference | valueFunction -> valueFunction )
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
                    // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:174:4: columnReference
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
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 175:3: -> columnReference
                    {
                        adaptor.addChild(root_0, stream_columnReference.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:176:5: valueFunction
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
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 177:3: -> valueFunction
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
        }

        catch(RecognitionException e)
        {
           throw e;
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
    // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:180:1: columnReference : ( qualifier DOT )? columnName -> ^( COLUMN_REF columnName ( qualifier )? ) ;
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
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:181:2: ( ( qualifier DOT )? columnName -> ^( COLUMN_REF columnName ( qualifier )? ) )
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:181:4: ( qualifier DOT )? columnName
            {
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:181:4: ( qualifier DOT )?
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
                    // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:181:6: qualifier DOT
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
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 182:3: -> ^( COLUMN_REF columnName ( qualifier )? )
            {
                // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:182:6: ^( COLUMN_REF columnName ( qualifier )? )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(COLUMN_REF, "COLUMN_REF"), root_1);

                adaptor.addChild(root_1, stream_columnName.nextTree());
                // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:182:30: ( qualifier )?
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

    public static class multiValuedColumnReference_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "multiValuedColumnReference"
    // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:189:1: multiValuedColumnReference : ( qualifier DOT )? multiValuedColumnName -> ^( COLUMN_REF multiValuedColumnName ( qualifier )? ) ;
    public final CMISParser.multiValuedColumnReference_return multiValuedColumnReference() throws RecognitionException {
        CMISParser.multiValuedColumnReference_return retval = new CMISParser.multiValuedColumnReference_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token DOT23=null;
        CMISParser.qualifier_return qualifier22 = null;

        CMISParser.multiValuedColumnName_return multiValuedColumnName24 = null;


        Object DOT23_tree=null;
        RewriteRuleTokenStream stream_DOT=new RewriteRuleTokenStream(adaptor,"token DOT");
        RewriteRuleSubtreeStream stream_multiValuedColumnName=new RewriteRuleSubtreeStream(adaptor,"rule multiValuedColumnName");
        RewriteRuleSubtreeStream stream_qualifier=new RewriteRuleSubtreeStream(adaptor,"rule qualifier");
        try {
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:190:2: ( ( qualifier DOT )? multiValuedColumnName -> ^( COLUMN_REF multiValuedColumnName ( qualifier )? ) )
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:190:10: ( qualifier DOT )? multiValuedColumnName
            {
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:190:10: ( qualifier DOT )?
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
                    // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:190:12: qualifier DOT
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
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 191:3: -> ^( COLUMN_REF multiValuedColumnName ( qualifier )? )
            {
                // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:191:6: ^( COLUMN_REF multiValuedColumnName ( qualifier )? )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(COLUMN_REF, "COLUMN_REF"), root_1);

                adaptor.addChild(root_1, stream_multiValuedColumnName.nextTree());
                // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:191:41: ( qualifier )?
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
    // $ANTLR end "multiValuedColumnReference"

    public static class valueFunction_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "valueFunction"
    // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:194:1: valueFunction : functionName= keyWordOrId LPAREN ( functionArgument )* RPAREN -> ^( FUNCTION $functionName LPAREN ( functionArgument )* RPAREN ) ;
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
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:195:2: (functionName= keyWordOrId LPAREN ( functionArgument )* RPAREN -> ^( FUNCTION $functionName LPAREN ( functionArgument )* RPAREN ) )
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:195:4: functionName= keyWordOrId LPAREN ( functionArgument )* RPAREN
            {
            pushFollow(FOLLOW_keyWordOrId_in_valueFunction470);
            functionName=keyWordOrId();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_keyWordOrId.add(functionName.getTree());
            LPAREN25=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_valueFunction472); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_LPAREN.add(LPAREN25);

            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:195:36: ( functionArgument )*
            loop11:
            do {
                int alt11=2;
                int LA11_0 = input.LA(1);

                if ( ((LA11_0>=COLON && LA11_0<=DECIMAL_INTEGER_LITERAL)) ) {
                    alt11=1;
                }


                switch (alt11) {
                case 1 :
                    // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:195:36: functionArgument
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
            // elements: RPAREN, functionArgument, functionName, LPAREN
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
            // 196:3: -> ^( FUNCTION $functionName LPAREN ( functionArgument )* RPAREN )
            {
                // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:196:6: ^( FUNCTION $functionName LPAREN ( functionArgument )* RPAREN )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(FUNCTION, "FUNCTION"), root_1);

                adaptor.addChild(root_1, stream_functionName.nextTree());
                adaptor.addChild(root_1, stream_LPAREN.nextNode());
                // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:196:38: ( functionArgument )*
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
    // $ANTLR end "valueFunction"

    public static class functionArgument_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "functionArgument"
    // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:199:1: functionArgument : ( qualifier DOT columnName -> ^( COLUMN_REF columnName qualifier ) | identifier | literalOrParameterName );
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
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:200:5: ( qualifier DOT columnName -> ^( COLUMN_REF columnName qualifier ) | identifier | literalOrParameterName )
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

                        if ( (LA12_8==DOT) ) {
                            alt12=1;
                        }
                        else if ( (LA12_8==RPAREN||(LA12_8>=COLON && LA12_8<=DECIMAL_INTEGER_LITERAL)) ) {
                            alt12=2;
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

                        if ( (LA12_8==DOT) ) {
                            alt12=1;
                        }
                        else if ( (LA12_8==RPAREN||(LA12_8>=COLON && LA12_8<=DECIMAL_INTEGER_LITERAL)) ) {
                            alt12=2;
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
                    // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:200:9: qualifier DOT columnName
                    {
                    pushFollow(FOLLOW_qualifier_in_functionArgument512);
                    qualifier28=qualifier();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_qualifier.add(qualifier28.getTree());
                    DOT29=(Token)match(input,DOT,FOLLOW_DOT_in_functionArgument514); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_DOT.add(DOT29);

                    pushFollow(FOLLOW_columnName_in_functionArgument516);
                    columnName30=columnName();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_columnName.add(columnName30.getTree());


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
                    // 201:5: -> ^( COLUMN_REF columnName qualifier )
                    {
                        // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:201:8: ^( COLUMN_REF columnName qualifier )
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
                    // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:202:9: identifier
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_identifier_in_functionArgument540);
                    identifier31=identifier();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, identifier31.getTree());

                    }
                    break;
                case 3 :
                    // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:203:9: literalOrParameterName
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_literalOrParameterName_in_functionArgument550);
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
    // $ANTLR end "functionArgument"

    public static class qualifier_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "qualifier"
    // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:206:1: qualifier : ( ( tableName )=> tableName -> tableName | correlationName -> correlationName );
    public final CMISParser.qualifier_return qualifier() throws RecognitionException {
        CMISParser.qualifier_return retval = new CMISParser.qualifier_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        CMISParser.tableName_return tableName33 = null;

        CMISParser.correlationName_return correlationName34 = null;


        RewriteRuleSubtreeStream stream_correlationName=new RewriteRuleSubtreeStream(adaptor,"rule correlationName");
        RewriteRuleSubtreeStream stream_tableName=new RewriteRuleSubtreeStream(adaptor,"rule tableName");
        try {
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:207:2: ( ( tableName )=> tableName -> tableName | correlationName -> correlationName )
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
                    // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:207:4: ( tableName )=> tableName
                    {
                    pushFollow(FOLLOW_tableName_in_qualifier571);
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
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 208:3: -> tableName
                    {
                        adaptor.addChild(root_0, stream_tableName.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:209:5: correlationName
                    {
                    pushFollow(FOLLOW_correlationName_in_qualifier583);
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
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 210:3: -> correlationName
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
           throw e;
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
    // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:213:1: fromClause : FROM tableReference -> tableReference ;
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
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:216:2: ( FROM tableReference -> tableReference )
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:216:4: FROM tableReference
            {
            FROM35=(Token)match(input,FROM,FOLLOW_FROM_in_fromClause620); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_FROM.add(FROM35);

            pushFollow(FOLLOW_tableReference_in_fromClause622);
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
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 217:3: -> tableReference
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
           throw e;
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
    // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:220:1: tableReference : singleTable ( ( joinedTable )=> joinedTable )* -> ^( SOURCE singleTable ( joinedTable )* ) ;
    public final CMISParser.tableReference_return tableReference() throws RecognitionException {
        CMISParser.tableReference_return retval = new CMISParser.tableReference_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        CMISParser.singleTable_return singleTable37 = null;

        CMISParser.joinedTable_return joinedTable38 = null;


        RewriteRuleSubtreeStream stream_joinedTable=new RewriteRuleSubtreeStream(adaptor,"rule joinedTable");
        RewriteRuleSubtreeStream stream_singleTable=new RewriteRuleSubtreeStream(adaptor,"rule singleTable");
        try {
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:221:2: ( singleTable ( ( joinedTable )=> joinedTable )* -> ^( SOURCE singleTable ( joinedTable )* ) )
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:221:4: singleTable ( ( joinedTable )=> joinedTable )*
            {
            pushFollow(FOLLOW_singleTable_in_tableReference640);
            singleTable37=singleTable();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_singleTable.add(singleTable37.getTree());
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:221:16: ( ( joinedTable )=> joinedTable )*
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
                    // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:221:17: ( joinedTable )=> joinedTable
                    {
                    pushFollow(FOLLOW_joinedTable_in_tableReference649);
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
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 222:3: -> ^( SOURCE singleTable ( joinedTable )* )
            {
                // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:222:6: ^( SOURCE singleTable ( joinedTable )* )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(SOURCE, "SOURCE"), root_1);

                adaptor.addChild(root_1, stream_singleTable.nextTree());
                // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:222:27: ( joinedTable )*
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
           throw e;
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
    // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:228:1: singleTable : ( tableName ( ( AS )? correlationName )? -> ^( TABLE_REF tableName ( correlationName )? ) | LPAREN joinedTables RPAREN -> ^( TABLE joinedTables ) );
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
        RewriteRuleTokenStream stream_RPAREN=new RewriteRuleTokenStream(adaptor,"token RPAREN");
        RewriteRuleTokenStream stream_LPAREN=new RewriteRuleTokenStream(adaptor,"token LPAREN");
        RewriteRuleTokenStream stream_AS=new RewriteRuleTokenStream(adaptor,"token AS");
        RewriteRuleSubtreeStream stream_correlationName=new RewriteRuleSubtreeStream(adaptor,"rule correlationName");
        RewriteRuleSubtreeStream stream_tableName=new RewriteRuleSubtreeStream(adaptor,"rule tableName");
        RewriteRuleSubtreeStream stream_joinedTables=new RewriteRuleSubtreeStream(adaptor,"rule joinedTables");
        try {
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:229:2: ( tableName ( ( AS )? correlationName )? -> ^( TABLE_REF tableName ( correlationName )? ) | LPAREN joinedTables RPAREN -> ^( TABLE joinedTables ) )
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
                    // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:229:4: tableName ( ( AS )? correlationName )?
                    {
                    pushFollow(FOLLOW_tableName_in_singleTable678);
                    tableName39=tableName();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_tableName.add(tableName39.getTree());
                    // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:229:14: ( ( AS )? correlationName )?
                    int alt16=2;
                    int LA16_0 = input.LA(1);

                    if ( (LA16_0==AS||(LA16_0>=ID && LA16_0<=DOUBLE_QUOTE)) ) {
                        alt16=1;
                    }
                    switch (alt16) {
                        case 1 :
                            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:229:16: ( AS )? correlationName
                            {
                            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:229:16: ( AS )?
                            int alt15=2;
                            int LA15_0 = input.LA(1);

                            if ( (LA15_0==AS) ) {
                                alt15=1;
                            }
                            switch (alt15) {
                                case 1 :
                                    // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:229:16: AS
                                    {
                                    AS40=(Token)match(input,AS,FOLLOW_AS_in_singleTable682); if (state.failed) return retval; 
                                    if ( state.backtracking==0 ) stream_AS.add(AS40);


                                    }
                                    break;

                            }

                            pushFollow(FOLLOW_correlationName_in_singleTable685);
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
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 230:3: -> ^( TABLE_REF tableName ( correlationName )? )
                    {
                        // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:230:6: ^( TABLE_REF tableName ( correlationName )? )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(TABLE_REF, "TABLE_REF"), root_1);

                        adaptor.addChild(root_1, stream_tableName.nextTree());
                        // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:230:28: ( correlationName )?
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
                    // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:231:4: LPAREN joinedTables RPAREN
                    {
                    LPAREN42=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_singleTable706); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_LPAREN.add(LPAREN42);

                    pushFollow(FOLLOW_joinedTables_in_singleTable708);
                    joinedTables43=joinedTables();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_joinedTables.add(joinedTables43.getTree());
                    RPAREN44=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_singleTable710); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_RPAREN.add(RPAREN44);



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
                    // 232:3: -> ^( TABLE joinedTables )
                    {
                        // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:232:6: ^( TABLE joinedTables )
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
           throw e;
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
    // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:235:1: joinedTable : ( joinType )? JOIN tableReference ( ( joinSpecification )=> joinSpecification )? -> ^( JOIN tableReference ( joinType )? ( joinSpecification )? ) ;
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
        RewriteRuleSubtreeStream stream_joinType=new RewriteRuleSubtreeStream(adaptor,"rule joinType");
        RewriteRuleSubtreeStream stream_tableReference=new RewriteRuleSubtreeStream(adaptor,"rule tableReference");
        RewriteRuleSubtreeStream stream_joinSpecification=new RewriteRuleSubtreeStream(adaptor,"rule joinSpecification");
        try {
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:236:2: ( ( joinType )? JOIN tableReference ( ( joinSpecification )=> joinSpecification )? -> ^( JOIN tableReference ( joinType )? ( joinSpecification )? ) )
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:236:4: ( joinType )? JOIN tableReference ( ( joinSpecification )=> joinSpecification )?
            {
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:236:4: ( joinType )?
            int alt18=2;
            int LA18_0 = input.LA(1);

            if ( ((LA18_0>=INNER && LA18_0<=LEFT)) ) {
                alt18=1;
            }
            switch (alt18) {
                case 1 :
                    // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:236:4: joinType
                    {
                    pushFollow(FOLLOW_joinType_in_joinedTable732);
                    joinType45=joinType();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_joinType.add(joinType45.getTree());

                    }
                    break;

            }

            JOIN46=(Token)match(input,JOIN,FOLLOW_JOIN_in_joinedTable735); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_JOIN.add(JOIN46);

            pushFollow(FOLLOW_tableReference_in_joinedTable737);
            tableReference47=tableReference();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_tableReference.add(tableReference47.getTree());
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:236:34: ( ( joinSpecification )=> joinSpecification )?
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
                    // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:236:35: ( joinSpecification )=> joinSpecification
                    {
                    pushFollow(FOLLOW_joinSpecification_in_joinedTable746);
                    joinSpecification48=joinSpecification();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_joinSpecification.add(joinSpecification48.getTree());

                    }
                    break;

            }



            // AST REWRITE
            // elements: JOIN, joinType, joinSpecification, tableReference
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 237:3: -> ^( JOIN tableReference ( joinType )? ( joinSpecification )? )
            {
                // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:237:6: ^( JOIN tableReference ( joinType )? ( joinSpecification )? )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot(stream_JOIN.nextNode(), root_1);

                adaptor.addChild(root_1, stream_tableReference.nextTree());
                // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:237:28: ( joinType )?
                if ( stream_joinType.hasNext() ) {
                    adaptor.addChild(root_1, stream_joinType.nextTree());

                }
                stream_joinType.reset();
                // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:237:38: ( joinSpecification )?
                if ( stream_joinSpecification.hasNext() ) {
                    adaptor.addChild(root_1, stream_joinSpecification.nextTree());

                }
                stream_joinSpecification.reset();

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
    // $ANTLR end "joinedTable"

    public static class joinedTables_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "joinedTables"
    // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:241:1: joinedTables : singleTable ( joinedTable )+ -> ^( SOURCE singleTable ( joinedTable )+ ) ;
    public final CMISParser.joinedTables_return joinedTables() throws RecognitionException {
        CMISParser.joinedTables_return retval = new CMISParser.joinedTables_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        CMISParser.singleTable_return singleTable49 = null;

        CMISParser.joinedTable_return joinedTable50 = null;


        RewriteRuleSubtreeStream stream_joinedTable=new RewriteRuleSubtreeStream(adaptor,"rule joinedTable");
        RewriteRuleSubtreeStream stream_singleTable=new RewriteRuleSubtreeStream(adaptor,"rule singleTable");
        try {
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:242:2: ( singleTable ( joinedTable )+ -> ^( SOURCE singleTable ( joinedTable )+ ) )
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:242:4: singleTable ( joinedTable )+
            {
            pushFollow(FOLLOW_singleTable_in_joinedTables777);
            singleTable49=singleTable();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_singleTable.add(singleTable49.getTree());
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:242:16: ( joinedTable )+
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
                    // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:242:16: joinedTable
                    {
                    pushFollow(FOLLOW_joinedTable_in_joinedTables779);
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
            // 243:3: -> ^( SOURCE singleTable ( joinedTable )+ )
            {
                // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:243:6: ^( SOURCE singleTable ( joinedTable )+ )
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
           throw e;
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
    // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:246:1: joinType : ( INNER -> INNER | LEFT ( OUTER )? -> LEFT );
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
        RewriteRuleTokenStream stream_INNER=new RewriteRuleTokenStream(adaptor,"token INNER");
        RewriteRuleTokenStream stream_LEFT=new RewriteRuleTokenStream(adaptor,"token LEFT");
        RewriteRuleTokenStream stream_OUTER=new RewriteRuleTokenStream(adaptor,"token OUTER");

        try {
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:247:2: ( INNER -> INNER | LEFT ( OUTER )? -> LEFT )
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
                    // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:247:4: INNER
                    {
                    INNER51=(Token)match(input,INNER,FOLLOW_INNER_in_joinType806); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_INNER.add(INNER51);



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
                    // 248:3: -> INNER
                    {
                        adaptor.addChild(root_0, stream_INNER.nextNode());

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:249:5: LEFT ( OUTER )?
                    {
                    LEFT52=(Token)match(input,LEFT,FOLLOW_LEFT_in_joinType818); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_LEFT.add(LEFT52);

                    // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:249:10: ( OUTER )?
                    int alt21=2;
                    int LA21_0 = input.LA(1);

                    if ( (LA21_0==OUTER) ) {
                        alt21=1;
                    }
                    switch (alt21) {
                        case 1 :
                            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:249:10: OUTER
                            {
                            OUTER53=(Token)match(input,OUTER,FOLLOW_OUTER_in_joinType820); if (state.failed) return retval; 
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
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 250:3: -> LEFT
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
           throw e;
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
    // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:253:1: joinSpecification : ON LPAREN lhs= columnReference EQUALS rhs= columnReference RPAREN -> ^( ON $lhs EQUALS $rhs) ;
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
        RewriteRuleTokenStream stream_RPAREN=new RewriteRuleTokenStream(adaptor,"token RPAREN");
        RewriteRuleTokenStream stream_EQUALS=new RewriteRuleTokenStream(adaptor,"token EQUALS");
        RewriteRuleTokenStream stream_LPAREN=new RewriteRuleTokenStream(adaptor,"token LPAREN");
        RewriteRuleTokenStream stream_ON=new RewriteRuleTokenStream(adaptor,"token ON");
        RewriteRuleSubtreeStream stream_columnReference=new RewriteRuleSubtreeStream(adaptor,"rule columnReference");
        try {
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:254:2: ( ON LPAREN lhs= columnReference EQUALS rhs= columnReference RPAREN -> ^( ON $lhs EQUALS $rhs) )
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:254:4: ON LPAREN lhs= columnReference EQUALS rhs= columnReference RPAREN
            {
            ON54=(Token)match(input,ON,FOLLOW_ON_in_joinSpecification840); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_ON.add(ON54);

            LPAREN55=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_joinSpecification842); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_LPAREN.add(LPAREN55);

            pushFollow(FOLLOW_columnReference_in_joinSpecification846);
            lhs=columnReference();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_columnReference.add(lhs.getTree());
            EQUALS56=(Token)match(input,EQUALS,FOLLOW_EQUALS_in_joinSpecification848); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_EQUALS.add(EQUALS56);

            pushFollow(FOLLOW_columnReference_in_joinSpecification852);
            rhs=columnReference();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_columnReference.add(rhs.getTree());
            RPAREN57=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_joinSpecification854); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_RPAREN.add(RPAREN57);



            // AST REWRITE
            // elements: ON, rhs, EQUALS, lhs
            // token labels: 
            // rule labels: lhs, retval, rhs
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_lhs=new RewriteRuleSubtreeStream(adaptor,"rule lhs",lhs!=null?lhs.tree:null);
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            RewriteRuleSubtreeStream stream_rhs=new RewriteRuleSubtreeStream(adaptor,"rule rhs",rhs!=null?rhs.tree:null);

            root_0 = (Object)adaptor.nil();
            // 255:3: -> ^( ON $lhs EQUALS $rhs)
            {
                // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:255:6: ^( ON $lhs EQUALS $rhs)
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
        }

        catch(RecognitionException e)
        {
           throw e;
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
    // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:262:1: whereClause : WHERE searchOrCondition -> searchOrCondition ;
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
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:265:2: ( WHERE searchOrCondition -> searchOrCondition )
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:265:4: WHERE searchOrCondition
            {
            WHERE58=(Token)match(input,WHERE,FOLLOW_WHERE_in_whereClause904); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_WHERE.add(WHERE58);

            pushFollow(FOLLOW_searchOrCondition_in_whereClause906);
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
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 266:3: -> searchOrCondition
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
           throw e;
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
    // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:269:1: searchOrCondition : searchAndCondition ( OR searchAndCondition )* -> ^( DISJUNCTION ( searchAndCondition )+ ) ;
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
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:273:2: ( searchAndCondition ( OR searchAndCondition )* -> ^( DISJUNCTION ( searchAndCondition )+ ) )
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:273:4: searchAndCondition ( OR searchAndCondition )*
            {
            pushFollow(FOLLOW_searchAndCondition_in_searchOrCondition926);
            searchAndCondition60=searchAndCondition();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_searchAndCondition.add(searchAndCondition60.getTree());
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:273:23: ( OR searchAndCondition )*
            loop23:
            do {
                int alt23=2;
                int LA23_0 = input.LA(1);

                if ( (LA23_0==OR) ) {
                    alt23=1;
                }


                switch (alt23) {
                case 1 :
                    // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:273:24: OR searchAndCondition
                    {
                    OR61=(Token)match(input,OR,FOLLOW_OR_in_searchOrCondition929); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_OR.add(OR61);

                    pushFollow(FOLLOW_searchAndCondition_in_searchOrCondition931);
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
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 274:3: -> ^( DISJUNCTION ( searchAndCondition )+ )
            {
                // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:274:6: ^( DISJUNCTION ( searchAndCondition )+ )
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
           throw e;
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
    // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:278:1: searchAndCondition : searchNotCondition ( AND searchNotCondition )* -> ^( CONJUNCTION ( searchNotCondition )+ ) ;
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
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:282:2: ( searchNotCondition ( AND searchNotCondition )* -> ^( CONJUNCTION ( searchNotCondition )+ ) )
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:282:4: searchNotCondition ( AND searchNotCondition )*
            {
            pushFollow(FOLLOW_searchNotCondition_in_searchAndCondition959);
            searchNotCondition63=searchNotCondition();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_searchNotCondition.add(searchNotCondition63.getTree());
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:282:23: ( AND searchNotCondition )*
            loop24:
            do {
                int alt24=2;
                int LA24_0 = input.LA(1);

                if ( (LA24_0==AND) ) {
                    alt24=1;
                }


                switch (alt24) {
                case 1 :
                    // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:282:24: AND searchNotCondition
                    {
                    AND64=(Token)match(input,AND,FOLLOW_AND_in_searchAndCondition962); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_AND.add(AND64);

                    pushFollow(FOLLOW_searchNotCondition_in_searchAndCondition964);
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
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 283:3: -> ^( CONJUNCTION ( searchNotCondition )+ )
            {
                // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:283:6: ^( CONJUNCTION ( searchNotCondition )+ )
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
           throw e;
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
    // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:286:1: searchNotCondition : ( NOT searchTest -> ^( NEGATION searchTest ) | searchTest -> searchTest );
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
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:287:2: ( NOT searchTest -> ^( NEGATION searchTest ) | searchTest -> searchTest )
            int alt25=2;
            alt25 = dfa25.predict(input);
            switch (alt25) {
                case 1 :
                    // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:287:4: NOT searchTest
                    {
                    NOT66=(Token)match(input,NOT,FOLLOW_NOT_in_searchNotCondition991); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_NOT.add(NOT66);

                    pushFollow(FOLLOW_searchTest_in_searchNotCondition993);
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
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 288:3: -> ^( NEGATION searchTest )
                    {
                        // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:288:6: ^( NEGATION searchTest )
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
                    // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:289:4: searchTest
                    {
                    pushFollow(FOLLOW_searchTest_in_searchNotCondition1008);
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
                    // 290:3: -> searchTest
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
           throw e;
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
    // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:293:1: searchTest : ( predicate -> predicate | LPAREN searchOrCondition RPAREN -> searchOrCondition );
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
        RewriteRuleSubtreeStream stream_searchOrCondition=new RewriteRuleSubtreeStream(adaptor,"rule searchOrCondition");
        RewriteRuleSubtreeStream stream_predicate=new RewriteRuleSubtreeStream(adaptor,"rule predicate");
        try {
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:294:2: ( predicate -> predicate | LPAREN searchOrCondition RPAREN -> searchOrCondition )
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
                    // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:294:4: predicate
                    {
                    pushFollow(FOLLOW_predicate_in_searchTest1026);
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
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 295:3: -> predicate
                    {
                        adaptor.addChild(root_0, stream_predicate.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:296:4: LPAREN searchOrCondition RPAREN
                    {
                    LPAREN70=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_searchTest1037); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_LPAREN.add(LPAREN70);

                    pushFollow(FOLLOW_searchOrCondition_in_searchTest1039);
                    searchOrCondition71=searchOrCondition();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_searchOrCondition.add(searchOrCondition71.getTree());
                    RPAREN72=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_searchTest1041); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_RPAREN.add(RPAREN72);



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
                    // 297:3: -> searchOrCondition
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
           throw e;
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
    // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:300:1: predicate : ( comparisonPredicate | inPredicate | likePredicate | nullPredicate | quantifiedComparisonPredicate | quantifiedInPredicate | textSearchPredicate | folderPredicate );
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
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:301:2: ( comparisonPredicate | inPredicate | likePredicate | nullPredicate | quantifiedComparisonPredicate | quantifiedInPredicate | textSearchPredicate | folderPredicate )
            int alt27=8;
            alt27 = dfa27.predict(input);
            switch (alt27) {
                case 1 :
                    // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:301:4: comparisonPredicate
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_comparisonPredicate_in_predicate1058);
                    comparisonPredicate73=comparisonPredicate();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, comparisonPredicate73.getTree());

                    }
                    break;
                case 2 :
                    // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:302:4: inPredicate
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_inPredicate_in_predicate1063);
                    inPredicate74=inPredicate();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, inPredicate74.getTree());

                    }
                    break;
                case 3 :
                    // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:303:4: likePredicate
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_likePredicate_in_predicate1068);
                    likePredicate75=likePredicate();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, likePredicate75.getTree());

                    }
                    break;
                case 4 :
                    // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:304:4: nullPredicate
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_nullPredicate_in_predicate1073);
                    nullPredicate76=nullPredicate();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, nullPredicate76.getTree());

                    }
                    break;
                case 5 :
                    // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:305:5: quantifiedComparisonPredicate
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_quantifiedComparisonPredicate_in_predicate1079);
                    quantifiedComparisonPredicate77=quantifiedComparisonPredicate();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, quantifiedComparisonPredicate77.getTree());

                    }
                    break;
                case 6 :
                    // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:306:4: quantifiedInPredicate
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_quantifiedInPredicate_in_predicate1084);
                    quantifiedInPredicate78=quantifiedInPredicate();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, quantifiedInPredicate78.getTree());

                    }
                    break;
                case 7 :
                    // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:307:4: textSearchPredicate
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_textSearchPredicate_in_predicate1089);
                    textSearchPredicate79=textSearchPredicate();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, textSearchPredicate79.getTree());

                    }
                    break;
                case 8 :
                    // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:308:4: folderPredicate
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_folderPredicate_in_predicate1094);
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
    // $ANTLR end "predicate"

    public static class comparisonPredicate_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "comparisonPredicate"
    // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:311:1: comparisonPredicate : valueExpression compOp literalOrParameterName -> ^( PRED_COMPARISON ANY valueExpression compOp literalOrParameterName ) ;
    public final CMISParser.comparisonPredicate_return comparisonPredicate() throws RecognitionException {
        CMISParser.comparisonPredicate_return retval = new CMISParser.comparisonPredicate_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        CMISParser.valueExpression_return valueExpression81 = null;

        CMISParser.compOp_return compOp82 = null;

        CMISParser.literalOrParameterName_return literalOrParameterName83 = null;


        RewriteRuleSubtreeStream stream_valueExpression=new RewriteRuleSubtreeStream(adaptor,"rule valueExpression");
        RewriteRuleSubtreeStream stream_literalOrParameterName=new RewriteRuleSubtreeStream(adaptor,"rule literalOrParameterName");
        RewriteRuleSubtreeStream stream_compOp=new RewriteRuleSubtreeStream(adaptor,"rule compOp");
        try {
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:312:2: ( valueExpression compOp literalOrParameterName -> ^( PRED_COMPARISON ANY valueExpression compOp literalOrParameterName ) )
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:312:4: valueExpression compOp literalOrParameterName
            {
            pushFollow(FOLLOW_valueExpression_in_comparisonPredicate1106);
            valueExpression81=valueExpression();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_valueExpression.add(valueExpression81.getTree());
            pushFollow(FOLLOW_compOp_in_comparisonPredicate1108);
            compOp82=compOp();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_compOp.add(compOp82.getTree());
            pushFollow(FOLLOW_literalOrParameterName_in_comparisonPredicate1110);
            literalOrParameterName83=literalOrParameterName();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_literalOrParameterName.add(literalOrParameterName83.getTree());


            // AST REWRITE
            // elements: literalOrParameterName, compOp, valueExpression
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 313:3: -> ^( PRED_COMPARISON ANY valueExpression compOp literalOrParameterName )
            {
                // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:313:6: ^( PRED_COMPARISON ANY valueExpression compOp literalOrParameterName )
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
           throw e;
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
    // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:316:1: compOp : ( EQUALS | NOTEQUALS | LESSTHAN | GREATERTHAN | LESSTHANOREQUALS | GREATERTHANOREQUALS );
    public final CMISParser.compOp_return compOp() throws RecognitionException {
        CMISParser.compOp_return retval = new CMISParser.compOp_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token set84=null;

        Object set84_tree=null;

        try {
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:317:2: ( EQUALS | NOTEQUALS | LESSTHAN | GREATERTHAN | LESSTHANOREQUALS | GREATERTHANOREQUALS )
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:
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
    // $ANTLR end "compOp"

    public static class literalOrParameterName_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "literalOrParameterName"
    // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:325:1: literalOrParameterName : ( literal | parameterName );
    public final CMISParser.literalOrParameterName_return literalOrParameterName() throws RecognitionException {
        CMISParser.literalOrParameterName_return retval = new CMISParser.literalOrParameterName_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        CMISParser.literal_return literal85 = null;

        CMISParser.parameterName_return parameterName86 = null;



        try {
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:326:2: ( literal | parameterName )
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
                    // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:326:4: literal
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_literal_in_literalOrParameterName1176);
                    literal85=literal();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, literal85.getTree());

                    }
                    break;
                case 2 :
                    // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:327:4: parameterName
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_parameterName_in_literalOrParameterName1181);
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
    // $ANTLR end "literalOrParameterName"

    public static class literal_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "literal"
    // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:330:1: literal : ( signedNumericLiteral | characterStringLiteral );
    public final CMISParser.literal_return literal() throws RecognitionException {
        CMISParser.literal_return retval = new CMISParser.literal_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        CMISParser.signedNumericLiteral_return signedNumericLiteral87 = null;

        CMISParser.characterStringLiteral_return characterStringLiteral88 = null;



        try {
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:331:2: ( signedNumericLiteral | characterStringLiteral )
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
                    // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:331:4: signedNumericLiteral
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_signedNumericLiteral_in_literal1194);
                    signedNumericLiteral87=signedNumericLiteral();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, signedNumericLiteral87.getTree());

                    }
                    break;
                case 2 :
                    // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:332:4: characterStringLiteral
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_characterStringLiteral_in_literal1199);
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
    // $ANTLR end "literal"

    public static class inPredicate_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "inPredicate"
    // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:335:1: inPredicate : columnReference ( NOT )? IN LPAREN inValueList RPAREN -> ^( PRED_IN ANY columnReference inValueList ( NOT )? ) ;
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
        RewriteRuleTokenStream stream_LPAREN=new RewriteRuleTokenStream(adaptor,"token LPAREN");
        RewriteRuleTokenStream stream_NOT=new RewriteRuleTokenStream(adaptor,"token NOT");
        RewriteRuleTokenStream stream_IN=new RewriteRuleTokenStream(adaptor,"token IN");
        RewriteRuleSubtreeStream stream_inValueList=new RewriteRuleSubtreeStream(adaptor,"rule inValueList");
        RewriteRuleSubtreeStream stream_columnReference=new RewriteRuleSubtreeStream(adaptor,"rule columnReference");
        try {
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:336:2: ( columnReference ( NOT )? IN LPAREN inValueList RPAREN -> ^( PRED_IN ANY columnReference inValueList ( NOT )? ) )
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:336:4: columnReference ( NOT )? IN LPAREN inValueList RPAREN
            {
            pushFollow(FOLLOW_columnReference_in_inPredicate1211);
            columnReference89=columnReference();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_columnReference.add(columnReference89.getTree());
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:336:20: ( NOT )?
            int alt30=2;
            int LA30_0 = input.LA(1);

            if ( (LA30_0==NOT) ) {
                alt30=1;
            }
            switch (alt30) {
                case 1 :
                    // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:336:20: NOT
                    {
                    NOT90=(Token)match(input,NOT,FOLLOW_NOT_in_inPredicate1213); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_NOT.add(NOT90);


                    }
                    break;

            }

            IN91=(Token)match(input,IN,FOLLOW_IN_in_inPredicate1216); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_IN.add(IN91);

            LPAREN92=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_inPredicate1218); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_LPAREN.add(LPAREN92);

            pushFollow(FOLLOW_inValueList_in_inPredicate1220);
            inValueList93=inValueList();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_inValueList.add(inValueList93.getTree());
            RPAREN94=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_inPredicate1222); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_RPAREN.add(RPAREN94);



            // AST REWRITE
            // elements: NOT, inValueList, columnReference
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 337:3: -> ^( PRED_IN ANY columnReference inValueList ( NOT )? )
            {
                // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:337:6: ^( PRED_IN ANY columnReference inValueList ( NOT )? )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(PRED_IN, "PRED_IN"), root_1);

                adaptor.addChild(root_1, (Object)adaptor.create(ANY, "ANY"));
                adaptor.addChild(root_1, stream_columnReference.nextTree());
                adaptor.addChild(root_1, stream_inValueList.nextTree());
                // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:337:48: ( NOT )?
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
           throw e;
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
    // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:340:1: inValueList : literalOrParameterName ( COMMA literalOrParameterName )* -> ^( LIST ( literalOrParameterName )+ ) ;
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
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:341:2: ( literalOrParameterName ( COMMA literalOrParameterName )* -> ^( LIST ( literalOrParameterName )+ ) )
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:341:4: literalOrParameterName ( COMMA literalOrParameterName )*
            {
            pushFollow(FOLLOW_literalOrParameterName_in_inValueList1251);
            literalOrParameterName95=literalOrParameterName();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_literalOrParameterName.add(literalOrParameterName95.getTree());
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:341:27: ( COMMA literalOrParameterName )*
            loop31:
            do {
                int alt31=2;
                int LA31_0 = input.LA(1);

                if ( (LA31_0==COMMA) ) {
                    alt31=1;
                }


                switch (alt31) {
                case 1 :
                    // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:341:28: COMMA literalOrParameterName
                    {
                    COMMA96=(Token)match(input,COMMA,FOLLOW_COMMA_in_inValueList1254); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_COMMA.add(COMMA96);

                    pushFollow(FOLLOW_literalOrParameterName_in_inValueList1256);
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
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 342:3: -> ^( LIST ( literalOrParameterName )+ )
            {
                // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:342:6: ^( LIST ( literalOrParameterName )+ )
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
           throw e;
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
    // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:345:1: likePredicate : columnReference ( NOT )? LIKE characterStringLiteral -> ^( PRED_LIKE columnReference characterStringLiteral ( NOT )? ) ;
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
        RewriteRuleTokenStream stream_LIKE=new RewriteRuleTokenStream(adaptor,"token LIKE");
        RewriteRuleTokenStream stream_NOT=new RewriteRuleTokenStream(adaptor,"token NOT");
        RewriteRuleSubtreeStream stream_columnReference=new RewriteRuleSubtreeStream(adaptor,"rule columnReference");
        RewriteRuleSubtreeStream stream_characterStringLiteral=new RewriteRuleSubtreeStream(adaptor,"rule characterStringLiteral");
        try {
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:346:2: ( columnReference ( NOT )? LIKE characterStringLiteral -> ^( PRED_LIKE columnReference characterStringLiteral ( NOT )? ) )
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:346:4: columnReference ( NOT )? LIKE characterStringLiteral
            {
            pushFollow(FOLLOW_columnReference_in_likePredicate1282);
            columnReference98=columnReference();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_columnReference.add(columnReference98.getTree());
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:346:20: ( NOT )?
            int alt32=2;
            int LA32_0 = input.LA(1);

            if ( (LA32_0==NOT) ) {
                alt32=1;
            }
            switch (alt32) {
                case 1 :
                    // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:346:20: NOT
                    {
                    NOT99=(Token)match(input,NOT,FOLLOW_NOT_in_likePredicate1284); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_NOT.add(NOT99);


                    }
                    break;

            }

            LIKE100=(Token)match(input,LIKE,FOLLOW_LIKE_in_likePredicate1287); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_LIKE.add(LIKE100);

            pushFollow(FOLLOW_characterStringLiteral_in_likePredicate1289);
            characterStringLiteral101=characterStringLiteral();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_characterStringLiteral.add(characterStringLiteral101.getTree());


            // AST REWRITE
            // elements: NOT, characterStringLiteral, columnReference
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 347:3: -> ^( PRED_LIKE columnReference characterStringLiteral ( NOT )? )
            {
                // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:347:6: ^( PRED_LIKE columnReference characterStringLiteral ( NOT )? )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(PRED_LIKE, "PRED_LIKE"), root_1);

                adaptor.addChild(root_1, stream_columnReference.nextTree());
                adaptor.addChild(root_1, stream_characterStringLiteral.nextTree());
                // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:347:57: ( NOT )?
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
           throw e;
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
    // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:350:1: nullPredicate : ( ( ( columnReference )=> columnReference | multiValuedColumnReference ) IS NULL -> ^( PRED_EXISTS columnReference NOT ) | ( ( columnReference )=> columnReference | multiValuedColumnReference ) IS NOT NULL -> ^( PRED_EXISTS columnReference ) );
    public final CMISParser.nullPredicate_return nullPredicate() throws RecognitionException {
        CMISParser.nullPredicate_return retval = new CMISParser.nullPredicate_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token IS104=null;
        Token NULL105=null;
        Token IS108=null;
        Token NOT109=null;
        Token NULL110=null;
        CMISParser.columnReference_return columnReference102 = null;

        CMISParser.multiValuedColumnReference_return multiValuedColumnReference103 = null;

        CMISParser.columnReference_return columnReference106 = null;

        CMISParser.multiValuedColumnReference_return multiValuedColumnReference107 = null;


        Object IS104_tree=null;
        Object NULL105_tree=null;
        Object IS108_tree=null;
        Object NOT109_tree=null;
        Object NULL110_tree=null;
        RewriteRuleTokenStream stream_NOT=new RewriteRuleTokenStream(adaptor,"token NOT");
        RewriteRuleTokenStream stream_IS=new RewriteRuleTokenStream(adaptor,"token IS");
        RewriteRuleTokenStream stream_NULL=new RewriteRuleTokenStream(adaptor,"token NULL");
        RewriteRuleSubtreeStream stream_columnReference=new RewriteRuleSubtreeStream(adaptor,"rule columnReference");
        RewriteRuleSubtreeStream stream_multiValuedColumnReference=new RewriteRuleSubtreeStream(adaptor,"rule multiValuedColumnReference");
        try {
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:351:2: ( ( ( columnReference )=> columnReference | multiValuedColumnReference ) IS NULL -> ^( PRED_EXISTS columnReference NOT ) | ( ( columnReference )=> columnReference | multiValuedColumnReference ) IS NOT NULL -> ^( PRED_EXISTS columnReference ) )
            int alt35=2;
            alt35 = dfa35.predict(input);
            switch (alt35) {
                case 1 :
                    // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:351:4: ( ( columnReference )=> columnReference | multiValuedColumnReference ) IS NULL
                    {
                    // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:351:4: ( ( columnReference )=> columnReference | multiValuedColumnReference )
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
                            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:351:6: ( columnReference )=> columnReference
                            {
                            pushFollow(FOLLOW_columnReference_in_nullPredicate1323);
                            columnReference102=columnReference();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_columnReference.add(columnReference102.getTree());

                            }
                            break;
                        case 2 :
                            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:351:44: multiValuedColumnReference
                            {
                            pushFollow(FOLLOW_multiValuedColumnReference_in_nullPredicate1327);
                            multiValuedColumnReference103=multiValuedColumnReference();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_multiValuedColumnReference.add(multiValuedColumnReference103.getTree());

                            }
                            break;

                    }

                    IS104=(Token)match(input,IS,FOLLOW_IS_in_nullPredicate1330); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_IS.add(IS104);

                    NULL105=(Token)match(input,NULL,FOLLOW_NULL_in_nullPredicate1332); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_NULL.add(NULL105);



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
                    // 352:3: -> ^( PRED_EXISTS columnReference NOT )
                    {
                        // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:352:6: ^( PRED_EXISTS columnReference NOT )
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
                    // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:353:9: ( ( columnReference )=> columnReference | multiValuedColumnReference ) IS NOT NULL
                    {
                    // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:353:9: ( ( columnReference )=> columnReference | multiValuedColumnReference )
                    int alt34=2;
                    int LA34_0 = input.LA(1);

                    if ( (LA34_0==ID) ) {
                        int LA34_1 = input.LA(2);

                        if ( (synpred6_CMIS()) ) {
                            alt34=1;
                        }
                        else if ( (true) ) {
                            alt34=2;
                        }
                        else {
                            if (state.backtracking>0) {state.failed=true; return retval;}
                            NoViableAltException nvae =
                                new NoViableAltException("", 34, 1, input);

                            throw nvae;
                        }
                    }
                    else if ( (LA34_0==DOUBLE_QUOTE) ) {
                        int LA34_2 = input.LA(2);

                        if ( (LA34_2==SELECT||LA34_2==AS||(LA34_2>=FROM && LA34_2<=ON)||(LA34_2>=WHERE && LA34_2<=NOT)||(LA34_2>=IN && LA34_2<=DESC)||(LA34_2>=UPPER && LA34_2<=SCORE)) ) {
                            int LA34_5 = input.LA(3);

                            if ( (LA34_5==DOUBLE_QUOTE) ) {
                                int LA34_7 = input.LA(4);

                                if ( (synpred6_CMIS()) ) {
                                    alt34=1;
                                }
                                else if ( (true) ) {
                                    alt34=2;
                                }
                                else {
                                    if (state.backtracking>0) {state.failed=true; return retval;}
                                    NoViableAltException nvae =
                                        new NoViableAltException("", 34, 7, input);

                                    throw nvae;
                                }
                            }
                            else {
                                if (state.backtracking>0) {state.failed=true; return retval;}
                                NoViableAltException nvae =
                                    new NoViableAltException("", 34, 5, input);

                                throw nvae;
                            }
                        }
                        else if ( (LA34_2==ID) ) {
                            int LA34_6 = input.LA(3);

                            if ( (LA34_6==DOUBLE_QUOTE) ) {
                                int LA34_7 = input.LA(4);

                                if ( (synpred6_CMIS()) ) {
                                    alt34=1;
                                }
                                else if ( (true) ) {
                                    alt34=2;
                                }
                                else {
                                    if (state.backtracking>0) {state.failed=true; return retval;}
                                    NoViableAltException nvae =
                                        new NoViableAltException("", 34, 7, input);

                                    throw nvae;
                                }
                            }
                            else {
                                if (state.backtracking>0) {state.failed=true; return retval;}
                                NoViableAltException nvae =
                                    new NoViableAltException("", 34, 6, input);

                                throw nvae;
                            }
                        }
                        else {
                            if (state.backtracking>0) {state.failed=true; return retval;}
                            NoViableAltException nvae =
                                new NoViableAltException("", 34, 2, input);

                            throw nvae;
                        }
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return retval;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 34, 0, input);

                        throw nvae;
                    }
                    switch (alt34) {
                        case 1 :
                            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:353:11: ( columnReference )=> columnReference
                            {
                            pushFollow(FOLLOW_columnReference_in_nullPredicate1361);
                            columnReference106=columnReference();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_columnReference.add(columnReference106.getTree());

                            }
                            break;
                        case 2 :
                            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:353:49: multiValuedColumnReference
                            {
                            pushFollow(FOLLOW_multiValuedColumnReference_in_nullPredicate1365);
                            multiValuedColumnReference107=multiValuedColumnReference();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_multiValuedColumnReference.add(multiValuedColumnReference107.getTree());

                            }
                            break;

                    }

                    IS108=(Token)match(input,IS,FOLLOW_IS_in_nullPredicate1368); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_IS.add(IS108);

                    NOT109=(Token)match(input,NOT,FOLLOW_NOT_in_nullPredicate1370); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_NOT.add(NOT109);

                    NULL110=(Token)match(input,NULL,FOLLOW_NULL_in_nullPredicate1372); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_NULL.add(NULL110);



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
                    // 354:9: -> ^( PRED_EXISTS columnReference )
                    {
                        // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:354:12: ^( PRED_EXISTS columnReference )
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
           throw e;
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
    // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:357:1: quantifiedComparisonPredicate : literalOrParameterName compOp ANY multiValuedColumnReference -> ^( PRED_COMPARISON ANY literalOrParameterName compOp multiValuedColumnReference ) ;
    public final CMISParser.quantifiedComparisonPredicate_return quantifiedComparisonPredicate() throws RecognitionException {
        CMISParser.quantifiedComparisonPredicate_return retval = new CMISParser.quantifiedComparisonPredicate_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token ANY113=null;
        CMISParser.literalOrParameterName_return literalOrParameterName111 = null;

        CMISParser.compOp_return compOp112 = null;

        CMISParser.multiValuedColumnReference_return multiValuedColumnReference114 = null;


        Object ANY113_tree=null;
        RewriteRuleTokenStream stream_ANY=new RewriteRuleTokenStream(adaptor,"token ANY");
        RewriteRuleSubtreeStream stream_literalOrParameterName=new RewriteRuleSubtreeStream(adaptor,"rule literalOrParameterName");
        RewriteRuleSubtreeStream stream_compOp=new RewriteRuleSubtreeStream(adaptor,"rule compOp");
        RewriteRuleSubtreeStream stream_multiValuedColumnReference=new RewriteRuleSubtreeStream(adaptor,"rule multiValuedColumnReference");
        try {
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:358:2: ( literalOrParameterName compOp ANY multiValuedColumnReference -> ^( PRED_COMPARISON ANY literalOrParameterName compOp multiValuedColumnReference ) )
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:358:4: literalOrParameterName compOp ANY multiValuedColumnReference
            {
            pushFollow(FOLLOW_literalOrParameterName_in_quantifiedComparisonPredicate1400);
            literalOrParameterName111=literalOrParameterName();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_literalOrParameterName.add(literalOrParameterName111.getTree());
            pushFollow(FOLLOW_compOp_in_quantifiedComparisonPredicate1402);
            compOp112=compOp();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_compOp.add(compOp112.getTree());
            ANY113=(Token)match(input,ANY,FOLLOW_ANY_in_quantifiedComparisonPredicate1404); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_ANY.add(ANY113);

            pushFollow(FOLLOW_multiValuedColumnReference_in_quantifiedComparisonPredicate1406);
            multiValuedColumnReference114=multiValuedColumnReference();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_multiValuedColumnReference.add(multiValuedColumnReference114.getTree());


            // AST REWRITE
            // elements: ANY, multiValuedColumnReference, compOp, literalOrParameterName
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 359:2: -> ^( PRED_COMPARISON ANY literalOrParameterName compOp multiValuedColumnReference )
            {
                // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:359:5: ^( PRED_COMPARISON ANY literalOrParameterName compOp multiValuedColumnReference )
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
           throw e;
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
    // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:363:1: quantifiedInPredicate : ANY multiValuedColumnReference ( NOT )? IN LPAREN inValueList RPAREN -> ^( PRED_IN ANY multiValuedColumnReference inValueList ( NOT )? ) ;
    public final CMISParser.quantifiedInPredicate_return quantifiedInPredicate() throws RecognitionException {
        CMISParser.quantifiedInPredicate_return retval = new CMISParser.quantifiedInPredicate_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token ANY115=null;
        Token NOT117=null;
        Token IN118=null;
        Token LPAREN119=null;
        Token RPAREN121=null;
        CMISParser.multiValuedColumnReference_return multiValuedColumnReference116 = null;

        CMISParser.inValueList_return inValueList120 = null;


        Object ANY115_tree=null;
        Object NOT117_tree=null;
        Object IN118_tree=null;
        Object LPAREN119_tree=null;
        Object RPAREN121_tree=null;
        RewriteRuleTokenStream stream_RPAREN=new RewriteRuleTokenStream(adaptor,"token RPAREN");
        RewriteRuleTokenStream stream_LPAREN=new RewriteRuleTokenStream(adaptor,"token LPAREN");
        RewriteRuleTokenStream stream_NOT=new RewriteRuleTokenStream(adaptor,"token NOT");
        RewriteRuleTokenStream stream_ANY=new RewriteRuleTokenStream(adaptor,"token ANY");
        RewriteRuleTokenStream stream_IN=new RewriteRuleTokenStream(adaptor,"token IN");
        RewriteRuleSubtreeStream stream_inValueList=new RewriteRuleSubtreeStream(adaptor,"rule inValueList");
        RewriteRuleSubtreeStream stream_multiValuedColumnReference=new RewriteRuleSubtreeStream(adaptor,"rule multiValuedColumnReference");
        try {
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:364:2: ( ANY multiValuedColumnReference ( NOT )? IN LPAREN inValueList RPAREN -> ^( PRED_IN ANY multiValuedColumnReference inValueList ( NOT )? ) )
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:364:4: ANY multiValuedColumnReference ( NOT )? IN LPAREN inValueList RPAREN
            {
            ANY115=(Token)match(input,ANY,FOLLOW_ANY_in_quantifiedInPredicate1435); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_ANY.add(ANY115);

            pushFollow(FOLLOW_multiValuedColumnReference_in_quantifiedInPredicate1437);
            multiValuedColumnReference116=multiValuedColumnReference();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_multiValuedColumnReference.add(multiValuedColumnReference116.getTree());
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:364:35: ( NOT )?
            int alt36=2;
            int LA36_0 = input.LA(1);

            if ( (LA36_0==NOT) ) {
                alt36=1;
            }
            switch (alt36) {
                case 1 :
                    // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:364:35: NOT
                    {
                    NOT117=(Token)match(input,NOT,FOLLOW_NOT_in_quantifiedInPredicate1439); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_NOT.add(NOT117);


                    }
                    break;

            }

            IN118=(Token)match(input,IN,FOLLOW_IN_in_quantifiedInPredicate1442); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_IN.add(IN118);

            LPAREN119=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_quantifiedInPredicate1445); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_LPAREN.add(LPAREN119);

            pushFollow(FOLLOW_inValueList_in_quantifiedInPredicate1447);
            inValueList120=inValueList();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_inValueList.add(inValueList120.getTree());
            RPAREN121=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_quantifiedInPredicate1449); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_RPAREN.add(RPAREN121);



            // AST REWRITE
            // elements: multiValuedColumnReference, inValueList, ANY, NOT
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 365:3: -> ^( PRED_IN ANY multiValuedColumnReference inValueList ( NOT )? )
            {
                // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:365:6: ^( PRED_IN ANY multiValuedColumnReference inValueList ( NOT )? )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(PRED_IN, "PRED_IN"), root_1);

                adaptor.addChild(root_1, stream_ANY.nextNode());
                adaptor.addChild(root_1, stream_multiValuedColumnReference.nextTree());
                adaptor.addChild(root_1, stream_inValueList.nextTree());
                // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:365:59: ( NOT )?
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
           throw e;
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
    // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:368:1: textSearchPredicate : CONTAINS LPAREN ( qualifier COMMA | COMMA )? textSearchExpression RPAREN -> ^( PRED_FTS textSearchExpression ( qualifier )? ) ;
    public final CMISParser.textSearchPredicate_return textSearchPredicate() throws RecognitionException {
        CMISParser.textSearchPredicate_return retval = new CMISParser.textSearchPredicate_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token CONTAINS122=null;
        Token LPAREN123=null;
        Token COMMA125=null;
        Token COMMA126=null;
        Token RPAREN128=null;
        CMISParser.qualifier_return qualifier124 = null;

        CMISParser.textSearchExpression_return textSearchExpression127 = null;


        Object CONTAINS122_tree=null;
        Object LPAREN123_tree=null;
        Object COMMA125_tree=null;
        Object COMMA126_tree=null;
        Object RPAREN128_tree=null;
        RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
        RewriteRuleTokenStream stream_RPAREN=new RewriteRuleTokenStream(adaptor,"token RPAREN");
        RewriteRuleTokenStream stream_CONTAINS=new RewriteRuleTokenStream(adaptor,"token CONTAINS");
        RewriteRuleTokenStream stream_LPAREN=new RewriteRuleTokenStream(adaptor,"token LPAREN");
        RewriteRuleSubtreeStream stream_qualifier=new RewriteRuleSubtreeStream(adaptor,"rule qualifier");
        RewriteRuleSubtreeStream stream_textSearchExpression=new RewriteRuleSubtreeStream(adaptor,"rule textSearchExpression");
        try {
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:369:2: ( CONTAINS LPAREN ( qualifier COMMA | COMMA )? textSearchExpression RPAREN -> ^( PRED_FTS textSearchExpression ( qualifier )? ) )
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:369:4: CONTAINS LPAREN ( qualifier COMMA | COMMA )? textSearchExpression RPAREN
            {
            CONTAINS122=(Token)match(input,CONTAINS,FOLLOW_CONTAINS_in_textSearchPredicate1478); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_CONTAINS.add(CONTAINS122);

            LPAREN123=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_textSearchPredicate1480); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_LPAREN.add(LPAREN123);

            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:369:20: ( qualifier COMMA | COMMA )?
            int alt37=3;
            int LA37_0 = input.LA(1);

            if ( ((LA37_0>=ID && LA37_0<=DOUBLE_QUOTE)) ) {
                alt37=1;
            }
            else if ( (LA37_0==COMMA) ) {
                alt37=2;
            }
            switch (alt37) {
                case 1 :
                    // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:369:21: qualifier COMMA
                    {
                    pushFollow(FOLLOW_qualifier_in_textSearchPredicate1483);
                    qualifier124=qualifier();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_qualifier.add(qualifier124.getTree());
                    COMMA125=(Token)match(input,COMMA,FOLLOW_COMMA_in_textSearchPredicate1485); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_COMMA.add(COMMA125);


                    }
                    break;
                case 2 :
                    // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:369:39: COMMA
                    {
                    COMMA126=(Token)match(input,COMMA,FOLLOW_COMMA_in_textSearchPredicate1489); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_COMMA.add(COMMA126);


                    }
                    break;

            }

            pushFollow(FOLLOW_textSearchExpression_in_textSearchPredicate1493);
            textSearchExpression127=textSearchExpression();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_textSearchExpression.add(textSearchExpression127.getTree());
            RPAREN128=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_textSearchPredicate1495); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_RPAREN.add(RPAREN128);



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
            // 370:3: -> ^( PRED_FTS textSearchExpression ( qualifier )? )
            {
                // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:370:6: ^( PRED_FTS textSearchExpression ( qualifier )? )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(PRED_FTS, "PRED_FTS"), root_1);

                adaptor.addChild(root_1, stream_textSearchExpression.nextTree());
                // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:370:38: ( qualifier )?
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
    // $ANTLR end "textSearchPredicate"

    public static class folderPredicate_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "folderPredicate"
    // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:373:1: folderPredicate : ( IN_FOLDER folderPredicateArgs -> ^( PRED_CHILD folderPredicateArgs ) | IN_TREE folderPredicateArgs -> ^( PRED_DESCENDANT folderPredicateArgs ) );
    public final CMISParser.folderPredicate_return folderPredicate() throws RecognitionException {
        CMISParser.folderPredicate_return retval = new CMISParser.folderPredicate_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token IN_FOLDER129=null;
        Token IN_TREE131=null;
        CMISParser.folderPredicateArgs_return folderPredicateArgs130 = null;

        CMISParser.folderPredicateArgs_return folderPredicateArgs132 = null;


        Object IN_FOLDER129_tree=null;
        Object IN_TREE131_tree=null;
        RewriteRuleTokenStream stream_IN_TREE=new RewriteRuleTokenStream(adaptor,"token IN_TREE");
        RewriteRuleTokenStream stream_IN_FOLDER=new RewriteRuleTokenStream(adaptor,"token IN_FOLDER");
        RewriteRuleSubtreeStream stream_folderPredicateArgs=new RewriteRuleSubtreeStream(adaptor,"rule folderPredicateArgs");
        try {
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:374:2: ( IN_FOLDER folderPredicateArgs -> ^( PRED_CHILD folderPredicateArgs ) | IN_TREE folderPredicateArgs -> ^( PRED_DESCENDANT folderPredicateArgs ) )
            int alt38=2;
            int LA38_0 = input.LA(1);

            if ( (LA38_0==IN_FOLDER) ) {
                alt38=1;
            }
            else if ( (LA38_0==IN_TREE) ) {
                alt38=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 38, 0, input);

                throw nvae;
            }
            switch (alt38) {
                case 1 :
                    // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:374:4: IN_FOLDER folderPredicateArgs
                    {
                    IN_FOLDER129=(Token)match(input,IN_FOLDER,FOLLOW_IN_FOLDER_in_folderPredicate1520); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_IN_FOLDER.add(IN_FOLDER129);

                    pushFollow(FOLLOW_folderPredicateArgs_in_folderPredicate1523);
                    folderPredicateArgs130=folderPredicateArgs();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_folderPredicateArgs.add(folderPredicateArgs130.getTree());


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
                    // 375:3: -> ^( PRED_CHILD folderPredicateArgs )
                    {
                        // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:375:6: ^( PRED_CHILD folderPredicateArgs )
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
                    // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:376:10: IN_TREE folderPredicateArgs
                    {
                    IN_TREE131=(Token)match(input,IN_TREE,FOLLOW_IN_TREE_in_folderPredicate1544); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_IN_TREE.add(IN_TREE131);

                    pushFollow(FOLLOW_folderPredicateArgs_in_folderPredicate1546);
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
                    // 377:3: -> ^( PRED_DESCENDANT folderPredicateArgs )
                    {
                        // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:377:6: ^( PRED_DESCENDANT folderPredicateArgs )
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
           throw e;
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
    // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:380:1: folderPredicateArgs : LPAREN ( qualifier COMMA | COMMA )? folderId RPAREN -> folderId ( qualifier )? ;
    public final CMISParser.folderPredicateArgs_return folderPredicateArgs() throws RecognitionException {
        CMISParser.folderPredicateArgs_return retval = new CMISParser.folderPredicateArgs_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token LPAREN133=null;
        Token COMMA135=null;
        Token COMMA136=null;
        Token RPAREN138=null;
        CMISParser.qualifier_return qualifier134 = null;

        CMISParser.folderId_return folderId137 = null;


        Object LPAREN133_tree=null;
        Object COMMA135_tree=null;
        Object COMMA136_tree=null;
        Object RPAREN138_tree=null;
        RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
        RewriteRuleTokenStream stream_RPAREN=new RewriteRuleTokenStream(adaptor,"token RPAREN");
        RewriteRuleTokenStream stream_LPAREN=new RewriteRuleTokenStream(adaptor,"token LPAREN");
        RewriteRuleSubtreeStream stream_folderId=new RewriteRuleSubtreeStream(adaptor,"rule folderId");
        RewriteRuleSubtreeStream stream_qualifier=new RewriteRuleSubtreeStream(adaptor,"rule qualifier");
        try {
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:381:2: ( LPAREN ( qualifier COMMA | COMMA )? folderId RPAREN -> folderId ( qualifier )? )
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:381:4: LPAREN ( qualifier COMMA | COMMA )? folderId RPAREN
            {
            LPAREN133=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_folderPredicateArgs1568); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_LPAREN.add(LPAREN133);

            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:381:11: ( qualifier COMMA | COMMA )?
            int alt39=3;
            int LA39_0 = input.LA(1);

            if ( ((LA39_0>=ID && LA39_0<=DOUBLE_QUOTE)) ) {
                alt39=1;
            }
            else if ( (LA39_0==COMMA) ) {
                alt39=2;
            }
            switch (alt39) {
                case 1 :
                    // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:381:12: qualifier COMMA
                    {
                    pushFollow(FOLLOW_qualifier_in_folderPredicateArgs1571);
                    qualifier134=qualifier();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_qualifier.add(qualifier134.getTree());
                    COMMA135=(Token)match(input,COMMA,FOLLOW_COMMA_in_folderPredicateArgs1573); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_COMMA.add(COMMA135);


                    }
                    break;
                case 2 :
                    // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:381:30: COMMA
                    {
                    COMMA136=(Token)match(input,COMMA,FOLLOW_COMMA_in_folderPredicateArgs1577); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_COMMA.add(COMMA136);


                    }
                    break;

            }

            pushFollow(FOLLOW_folderId_in_folderPredicateArgs1581);
            folderId137=folderId();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_folderId.add(folderId137.getTree());
            RPAREN138=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_folderPredicateArgs1583); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_RPAREN.add(RPAREN138);



            // AST REWRITE
            // elements: qualifier, folderId
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 382:3: -> folderId ( qualifier )?
            {
                adaptor.addChild(root_0, stream_folderId.nextTree());
                // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:382:15: ( qualifier )?
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
           throw e;
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
    // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:385:1: orderByClause : ORDER BY sortSpecification ( COMMA sortSpecification )* -> ^( ORDER ( sortSpecification )+ ) ;
    public final CMISParser.orderByClause_return orderByClause() throws RecognitionException {
        CMISParser.orderByClause_return retval = new CMISParser.orderByClause_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token ORDER139=null;
        Token BY140=null;
        Token COMMA142=null;
        CMISParser.sortSpecification_return sortSpecification141 = null;

        CMISParser.sortSpecification_return sortSpecification143 = null;


        Object ORDER139_tree=null;
        Object BY140_tree=null;
        Object COMMA142_tree=null;
        RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
        RewriteRuleTokenStream stream_ORDER=new RewriteRuleTokenStream(adaptor,"token ORDER");
        RewriteRuleTokenStream stream_BY=new RewriteRuleTokenStream(adaptor,"token BY");
        RewriteRuleSubtreeStream stream_sortSpecification=new RewriteRuleSubtreeStream(adaptor,"rule sortSpecification");
            paraphrases.push("in order by"); 
        try {
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:388:2: ( ORDER BY sortSpecification ( COMMA sortSpecification )* -> ^( ORDER ( sortSpecification )+ ) )
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:388:4: ORDER BY sortSpecification ( COMMA sortSpecification )*
            {
            ORDER139=(Token)match(input,ORDER,FOLLOW_ORDER_in_orderByClause1622); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_ORDER.add(ORDER139);

            BY140=(Token)match(input,BY,FOLLOW_BY_in_orderByClause1624); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_BY.add(BY140);

            pushFollow(FOLLOW_sortSpecification_in_orderByClause1626);
            sortSpecification141=sortSpecification();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_sortSpecification.add(sortSpecification141.getTree());
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:388:31: ( COMMA sortSpecification )*
            loop40:
            do {
                int alt40=2;
                int LA40_0 = input.LA(1);

                if ( (LA40_0==COMMA) ) {
                    alt40=1;
                }


                switch (alt40) {
                case 1 :
                    // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:388:33: COMMA sortSpecification
                    {
                    COMMA142=(Token)match(input,COMMA,FOLLOW_COMMA_in_orderByClause1630); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_COMMA.add(COMMA142);

                    pushFollow(FOLLOW_sortSpecification_in_orderByClause1632);
                    sortSpecification143=sortSpecification();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_sortSpecification.add(sortSpecification143.getTree());

                    }
                    break;

                default :
                    break loop40;
                }
            } while (true);



            // AST REWRITE
            // elements: ORDER, sortSpecification
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 389:3: -> ^( ORDER ( sortSpecification )+ )
            {
                // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:389:6: ^( ORDER ( sortSpecification )+ )
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
           throw e;
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
    // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:392:1: sortSpecification : ( columnReference -> ^( SORT_SPECIFICATION columnReference ASC ) | columnReference (by= ASC | by= DESC ) -> ^( SORT_SPECIFICATION columnReference $by) );
    public final CMISParser.sortSpecification_return sortSpecification() throws RecognitionException {
        CMISParser.sortSpecification_return retval = new CMISParser.sortSpecification_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token by=null;
        CMISParser.columnReference_return columnReference144 = null;

        CMISParser.columnReference_return columnReference145 = null;


        Object by_tree=null;
        RewriteRuleTokenStream stream_DESC=new RewriteRuleTokenStream(adaptor,"token DESC");
        RewriteRuleTokenStream stream_ASC=new RewriteRuleTokenStream(adaptor,"token ASC");
        RewriteRuleSubtreeStream stream_columnReference=new RewriteRuleSubtreeStream(adaptor,"rule columnReference");
        try {
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:393:2: ( columnReference -> ^( SORT_SPECIFICATION columnReference ASC ) | columnReference (by= ASC | by= DESC ) -> ^( SORT_SPECIFICATION columnReference $by) )
            int alt42=2;
            alt42 = dfa42.predict(input);
            switch (alt42) {
                case 1 :
                    // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:393:4: columnReference
                    {
                    pushFollow(FOLLOW_columnReference_in_sortSpecification1658);
                    columnReference144=columnReference();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_columnReference.add(columnReference144.getTree());


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
                    // 394:3: -> ^( SORT_SPECIFICATION columnReference ASC )
                    {
                        // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:394:6: ^( SORT_SPECIFICATION columnReference ASC )
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
                    // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:395:4: columnReference (by= ASC | by= DESC )
                    {
                    pushFollow(FOLLOW_columnReference_in_sortSpecification1676);
                    columnReference145=columnReference();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_columnReference.add(columnReference145.getTree());
                    // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:395:20: (by= ASC | by= DESC )
                    int alt41=2;
                    int LA41_0 = input.LA(1);

                    if ( (LA41_0==ASC) ) {
                        alt41=1;
                    }
                    else if ( (LA41_0==DESC) ) {
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
                            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:395:22: by= ASC
                            {
                            by=(Token)match(input,ASC,FOLLOW_ASC_in_sortSpecification1682); if (state.failed) return retval; 
                            if ( state.backtracking==0 ) stream_ASC.add(by);


                            }
                            break;
                        case 2 :
                            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:395:31: by= DESC
                            {
                            by=(Token)match(input,DESC,FOLLOW_DESC_in_sortSpecification1688); if (state.failed) return retval; 
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
                    // 396:3: -> ^( SORT_SPECIFICATION columnReference $by)
                    {
                        // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:396:6: ^( SORT_SPECIFICATION columnReference $by)
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
           throw e;
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
    // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:399:1: correlationName : identifier ;
    public final CMISParser.correlationName_return correlationName() throws RecognitionException {
        CMISParser.correlationName_return retval = new CMISParser.correlationName_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        CMISParser.identifier_return identifier146 = null;



        try {
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:400:2: ( identifier )
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:400:4: identifier
            {
            root_0 = (Object)adaptor.nil();

            pushFollow(FOLLOW_identifier_in_correlationName1715);
            identifier146=identifier();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, identifier146.getTree());

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
    // $ANTLR end "correlationName"

    public static class tableName_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "tableName"
    // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:407:1: tableName : identifier -> identifier ;
    public final CMISParser.tableName_return tableName() throws RecognitionException {
        CMISParser.tableName_return retval = new CMISParser.tableName_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        CMISParser.identifier_return identifier147 = null;


        RewriteRuleSubtreeStream stream_identifier=new RewriteRuleSubtreeStream(adaptor,"rule identifier");
        try {
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:408:2: ( identifier -> identifier )
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:408:4: identifier
            {
            pushFollow(FOLLOW_identifier_in_tableName1729);
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
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 409:3: -> identifier
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
           throw e;
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
    // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:412:1: columnName : identifier -> identifier ;
    public final CMISParser.columnName_return columnName() throws RecognitionException {
        CMISParser.columnName_return retval = new CMISParser.columnName_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        CMISParser.identifier_return identifier148 = null;


        RewriteRuleSubtreeStream stream_identifier=new RewriteRuleSubtreeStream(adaptor,"rule identifier");
        try {
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:413:2: ( identifier -> identifier )
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:413:4: identifier
            {
            pushFollow(FOLLOW_identifier_in_columnName1747);
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
            // 414:3: -> identifier
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
           throw e;
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
    // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:417:1: multiValuedColumnName : identifier -> identifier ;
    public final CMISParser.multiValuedColumnName_return multiValuedColumnName() throws RecognitionException {
        CMISParser.multiValuedColumnName_return retval = new CMISParser.multiValuedColumnName_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        CMISParser.identifier_return identifier149 = null;


        RewriteRuleSubtreeStream stream_identifier=new RewriteRuleSubtreeStream(adaptor,"rule identifier");
        try {
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:418:2: ( identifier -> identifier )
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:418:4: identifier
            {
            pushFollow(FOLLOW_identifier_in_multiValuedColumnName1766);
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
            // 419:3: -> identifier
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
           throw e;
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
    // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:422:1: parameterName : COLON identifier -> ^( PARAMETER identifier ) ;
    public final CMISParser.parameterName_return parameterName() throws RecognitionException {
        CMISParser.parameterName_return retval = new CMISParser.parameterName_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token COLON150=null;
        CMISParser.identifier_return identifier151 = null;


        Object COLON150_tree=null;
        RewriteRuleTokenStream stream_COLON=new RewriteRuleTokenStream(adaptor,"token COLON");
        RewriteRuleSubtreeStream stream_identifier=new RewriteRuleSubtreeStream(adaptor,"rule identifier");
        try {
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:423:2: ( COLON identifier -> ^( PARAMETER identifier ) )
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:423:4: COLON identifier
            {
            COLON150=(Token)match(input,COLON,FOLLOW_COLON_in_parameterName1784); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_COLON.add(COLON150);

            pushFollow(FOLLOW_identifier_in_parameterName1786);
            identifier151=identifier();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_identifier.add(identifier151.getTree());


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
            // 424:3: -> ^( PARAMETER identifier )
            {
                // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:424:6: ^( PARAMETER identifier )
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
           throw e;
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
    // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:427:1: folderId : characterStringLiteral -> characterStringLiteral ;
    public final CMISParser.folderId_return folderId() throws RecognitionException {
        CMISParser.folderId_return retval = new CMISParser.folderId_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        CMISParser.characterStringLiteral_return characterStringLiteral152 = null;


        RewriteRuleSubtreeStream stream_characterStringLiteral=new RewriteRuleSubtreeStream(adaptor,"rule characterStringLiteral");
        try {
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:428:3: ( characterStringLiteral -> characterStringLiteral )
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:428:5: characterStringLiteral
            {
            pushFollow(FOLLOW_characterStringLiteral_in_folderId1809);
            characterStringLiteral152=characterStringLiteral();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_characterStringLiteral.add(characterStringLiteral152.getTree());


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
            // 429:4: -> characterStringLiteral
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
           throw e;
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
    // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:432:1: textSearchExpression : QUOTED_STRING ;
    public final CMISParser.textSearchExpression_return textSearchExpression() throws RecognitionException {
        CMISParser.textSearchExpression_return retval = new CMISParser.textSearchExpression_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token QUOTED_STRING153=null;

        Object QUOTED_STRING153_tree=null;

        try {
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:433:2: ( QUOTED_STRING )
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:433:4: QUOTED_STRING
            {
            root_0 = (Object)adaptor.nil();

            QUOTED_STRING153=(Token)match(input,QUOTED_STRING,FOLLOW_QUOTED_STRING_in_textSearchExpression1830); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            QUOTED_STRING153_tree = (Object)adaptor.create(QUOTED_STRING153);
            adaptor.addChild(root_0, QUOTED_STRING153_tree);
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
    // $ANTLR end "textSearchExpression"

    public static class identifier_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "identifier"
    // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:436:1: identifier : ( ID -> ID | DOUBLE_QUOTE keyWordOrId DOUBLE_QUOTE -> ^( keyWordOrId ) );
    public final CMISParser.identifier_return identifier() throws RecognitionException {
        CMISParser.identifier_return retval = new CMISParser.identifier_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token ID154=null;
        Token DOUBLE_QUOTE155=null;
        Token DOUBLE_QUOTE157=null;
        CMISParser.keyWordOrId_return keyWordOrId156 = null;


        Object ID154_tree=null;
        Object DOUBLE_QUOTE155_tree=null;
        Object DOUBLE_QUOTE157_tree=null;
        RewriteRuleTokenStream stream_DOUBLE_QUOTE=new RewriteRuleTokenStream(adaptor,"token DOUBLE_QUOTE");
        RewriteRuleTokenStream stream_ID=new RewriteRuleTokenStream(adaptor,"token ID");
        RewriteRuleSubtreeStream stream_keyWordOrId=new RewriteRuleSubtreeStream(adaptor,"rule keyWordOrId");
        try {
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:437:2: ( ID -> ID | DOUBLE_QUOTE keyWordOrId DOUBLE_QUOTE -> ^( keyWordOrId ) )
            int alt43=2;
            int LA43_0 = input.LA(1);

            if ( (LA43_0==ID) ) {
                alt43=1;
            }
            else if ( (LA43_0==DOUBLE_QUOTE) ) {
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
                    // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:437:4: ID
                    {
                    ID154=(Token)match(input,ID,FOLLOW_ID_in_identifier1842); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_ID.add(ID154);



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
                    // 438:3: -> ID
                    {
                        adaptor.addChild(root_0, stream_ID.nextNode());

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:439:4: DOUBLE_QUOTE keyWordOrId DOUBLE_QUOTE
                    {
                    DOUBLE_QUOTE155=(Token)match(input,DOUBLE_QUOTE,FOLLOW_DOUBLE_QUOTE_in_identifier1853); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_DOUBLE_QUOTE.add(DOUBLE_QUOTE155);

                    pushFollow(FOLLOW_keyWordOrId_in_identifier1855);
                    keyWordOrId156=keyWordOrId();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_keyWordOrId.add(keyWordOrId156.getTree());
                    DOUBLE_QUOTE157=(Token)match(input,DOUBLE_QUOTE,FOLLOW_DOUBLE_QUOTE_in_identifier1857); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_DOUBLE_QUOTE.add(DOUBLE_QUOTE157);



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
                    // 440:3: -> ^( keyWordOrId )
                    {
                        // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:440:6: ^( keyWordOrId )
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
           throw e;
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
    // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:443:1: signedNumericLiteral : ( FLOATING_POINT_LITERAL -> ^( NUMERIC_LITERAL FLOATING_POINT_LITERAL ) | integerLiteral -> integerLiteral );
    public final CMISParser.signedNumericLiteral_return signedNumericLiteral() throws RecognitionException {
        CMISParser.signedNumericLiteral_return retval = new CMISParser.signedNumericLiteral_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token FLOATING_POINT_LITERAL158=null;
        CMISParser.integerLiteral_return integerLiteral159 = null;


        Object FLOATING_POINT_LITERAL158_tree=null;
        RewriteRuleTokenStream stream_FLOATING_POINT_LITERAL=new RewriteRuleTokenStream(adaptor,"token FLOATING_POINT_LITERAL");
        RewriteRuleSubtreeStream stream_integerLiteral=new RewriteRuleSubtreeStream(adaptor,"rule integerLiteral");
        try {
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:444:2: ( FLOATING_POINT_LITERAL -> ^( NUMERIC_LITERAL FLOATING_POINT_LITERAL ) | integerLiteral -> integerLiteral )
            int alt44=2;
            int LA44_0 = input.LA(1);

            if ( (LA44_0==FLOATING_POINT_LITERAL) ) {
                alt44=1;
            }
            else if ( (LA44_0==DECIMAL_INTEGER_LITERAL) ) {
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
                    // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:444:4: FLOATING_POINT_LITERAL
                    {
                    FLOATING_POINT_LITERAL158=(Token)match(input,FLOATING_POINT_LITERAL,FOLLOW_FLOATING_POINT_LITERAL_in_signedNumericLiteral1877); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_FLOATING_POINT_LITERAL.add(FLOATING_POINT_LITERAL158);



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
                    // 445:3: -> ^( NUMERIC_LITERAL FLOATING_POINT_LITERAL )
                    {
                        // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:445:6: ^( NUMERIC_LITERAL FLOATING_POINT_LITERAL )
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
                    // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:446:4: integerLiteral
                    {
                    pushFollow(FOLLOW_integerLiteral_in_signedNumericLiteral1892);
                    integerLiteral159=integerLiteral();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_integerLiteral.add(integerLiteral159.getTree());


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
                    // 447:3: -> integerLiteral
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
           throw e;
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
    // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:450:1: integerLiteral : DECIMAL_INTEGER_LITERAL -> ^( NUMERIC_LITERAL DECIMAL_INTEGER_LITERAL ) ;
    public final CMISParser.integerLiteral_return integerLiteral() throws RecognitionException {
        CMISParser.integerLiteral_return retval = new CMISParser.integerLiteral_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token DECIMAL_INTEGER_LITERAL160=null;

        Object DECIMAL_INTEGER_LITERAL160_tree=null;
        RewriteRuleTokenStream stream_DECIMAL_INTEGER_LITERAL=new RewriteRuleTokenStream(adaptor,"token DECIMAL_INTEGER_LITERAL");

        try {
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:451:2: ( DECIMAL_INTEGER_LITERAL -> ^( NUMERIC_LITERAL DECIMAL_INTEGER_LITERAL ) )
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:451:4: DECIMAL_INTEGER_LITERAL
            {
            DECIMAL_INTEGER_LITERAL160=(Token)match(input,DECIMAL_INTEGER_LITERAL,FOLLOW_DECIMAL_INTEGER_LITERAL_in_integerLiteral1911); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_DECIMAL_INTEGER_LITERAL.add(DECIMAL_INTEGER_LITERAL160);



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
            // 452:3: -> ^( NUMERIC_LITERAL DECIMAL_INTEGER_LITERAL )
            {
                // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:452:6: ^( NUMERIC_LITERAL DECIMAL_INTEGER_LITERAL )
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
           throw e;
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "integerLiteral"

    public static class characterStringLiteral_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "characterStringLiteral"
    // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:455:1: characterStringLiteral : QUOTED_STRING -> ^( STRING_LITERAL QUOTED_STRING ) ;
    public final CMISParser.characterStringLiteral_return characterStringLiteral() throws RecognitionException {
        CMISParser.characterStringLiteral_return retval = new CMISParser.characterStringLiteral_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token QUOTED_STRING161=null;

        Object QUOTED_STRING161_tree=null;
        RewriteRuleTokenStream stream_QUOTED_STRING=new RewriteRuleTokenStream(adaptor,"token QUOTED_STRING");

        try {
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:456:2: ( QUOTED_STRING -> ^( STRING_LITERAL QUOTED_STRING ) )
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:456:4: QUOTED_STRING
            {
            QUOTED_STRING161=(Token)match(input,QUOTED_STRING,FOLLOW_QUOTED_STRING_in_characterStringLiteral1934); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_QUOTED_STRING.add(QUOTED_STRING161);



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
            // 457:3: -> ^( STRING_LITERAL QUOTED_STRING )
            {
                // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:457:6: ^( STRING_LITERAL QUOTED_STRING )
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
           throw e;
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
    // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:461:1: keyWord : ( SELECT | AS | UPPER | LOWER | FROM | JOIN | INNER | LEFT | OUTER | ON | WHERE | OR | AND | NOT | IN | LIKE | IS | NULL | ANY | CONTAINS | IN_FOLDER | IN_TREE | ORDER | BY | ASC | DESC | SCORE );
    public final CMISParser.keyWord_return keyWord() throws RecognitionException {
        CMISParser.keyWord_return retval = new CMISParser.keyWord_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token set162=null;

        Object set162_tree=null;

        try {
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:461:9: ( SELECT | AS | UPPER | LOWER | FROM | JOIN | INNER | LEFT | OUTER | ON | WHERE | OR | AND | NOT | IN | LIKE | IS | NULL | ANY | CONTAINS | IN_FOLDER | IN_TREE | ORDER | BY | ASC | DESC | SCORE )
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:
            {
            root_0 = (Object)adaptor.nil();

            set162=(Token)input.LT(1);
            if ( input.LA(1)==SELECT||input.LA(1)==AS||(input.LA(1)>=FROM && input.LA(1)<=ON)||(input.LA(1)>=WHERE && input.LA(1)<=NOT)||(input.LA(1)>=IN && input.LA(1)<=DESC)||(input.LA(1)>=UPPER && input.LA(1)<=SCORE) ) {
                input.consume();
                if ( state.backtracking==0 ) adaptor.addChild(root_0, (Object)adaptor.create(set162));
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
    // $ANTLR end "keyWord"

    public static class keyWordOrId_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "keyWordOrId"
    // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:490:1: keyWordOrId : ( keyWord -> keyWord | ID -> ID );
    public final CMISParser.keyWordOrId_return keyWordOrId() throws RecognitionException {
        CMISParser.keyWordOrId_return retval = new CMISParser.keyWordOrId_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token ID164=null;
        CMISParser.keyWord_return keyWord163 = null;


        Object ID164_tree=null;
        RewriteRuleTokenStream stream_ID=new RewriteRuleTokenStream(adaptor,"token ID");
        RewriteRuleSubtreeStream stream_keyWord=new RewriteRuleSubtreeStream(adaptor,"rule keyWord");
        try {
            // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:491:2: ( keyWord -> keyWord | ID -> ID )
            int alt45=2;
            int LA45_0 = input.LA(1);

            if ( (LA45_0==SELECT||LA45_0==AS||(LA45_0>=FROM && LA45_0<=ON)||(LA45_0>=WHERE && LA45_0<=NOT)||(LA45_0>=IN && LA45_0<=DESC)||(LA45_0>=UPPER && LA45_0<=SCORE)) ) {
                alt45=1;
            }
            else if ( (LA45_0==ID) ) {
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
                    // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:491:4: keyWord
                    {
                    pushFollow(FOLLOW_keyWord_in_keyWordOrId2143);
                    keyWord163=keyWord();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_keyWord.add(keyWord163.getTree());


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
                    // 492:3: -> keyWord
                    {
                        adaptor.addChild(root_0, stream_keyWord.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:493:4: ID
                    {
                    ID164=(Token)match(input,ID,FOLLOW_ID_in_keyWordOrId2155); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_ID.add(ID164);



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
                    // 494:3: -> ID
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
           throw e;
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "keyWordOrId"

    // $ANTLR start synpred1_CMIS
    public final void synpred1_CMIS_fragment() throws RecognitionException {   
        // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:165:4: ( valueExpression )
        // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:165:5: valueExpression
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
        // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:207:4: ( tableName )
        // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:207:5: tableName
        {
        pushFollow(FOLLOW_tableName_in_synpred2_CMIS566);
        tableName();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred2_CMIS

    // $ANTLR start synpred3_CMIS
    public final void synpred3_CMIS_fragment() throws RecognitionException {   
        // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:221:17: ( joinedTable )
        // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:221:18: joinedTable
        {
        pushFollow(FOLLOW_joinedTable_in_synpred3_CMIS644);
        joinedTable();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred3_CMIS

    // $ANTLR start synpred4_CMIS
    public final void synpred4_CMIS_fragment() throws RecognitionException {   
        // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:236:35: ( joinSpecification )
        // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:236:36: joinSpecification
        {
        pushFollow(FOLLOW_joinSpecification_in_synpred4_CMIS741);
        joinSpecification();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred4_CMIS

    // $ANTLR start synpred5_CMIS
    public final void synpred5_CMIS_fragment() throws RecognitionException {   
        // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:351:6: ( columnReference )
        // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:351:7: columnReference
        {
        pushFollow(FOLLOW_columnReference_in_synpred5_CMIS1319);
        columnReference();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred5_CMIS

    // $ANTLR start synpred6_CMIS
    public final void synpred6_CMIS_fragment() throws RecognitionException {   
        // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:353:11: ( columnReference )
        // /Users/dcaruana/Dev/projects/cmis062/code/root/projects/repository/source/java/org/alfresco/repo/search/impl/parsers/CMIS.g:353:12: columnReference
        {
        pushFollow(FOLLOW_columnReference_in_synpred6_CMIS1357);
        columnReference();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred6_CMIS

    // Delegated rules

    public final boolean synpred6_CMIS() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred6_CMIS_fragment(); // can never throw exception
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


    protected DFA25 dfa25 = new DFA25(this);
    protected DFA27 dfa27 = new DFA27(this);
    protected DFA35 dfa35 = new DFA35(this);
    protected DFA42 dfa42 = new DFA42(this);
    static final String DFA25_eotS =
        "\31\uffff";
    static final String DFA25_eofS =
        "\31\uffff";
    static final String DFA25_minS =
        "\2\35\2\uffff\1\35\1\42\1\35\3\44\2\103\2\104\1\44\1\35\1\44\1\35"+
        "\1\42\4\104\2\44";
    static final String DFA25_maxS =
        "\2\111\2\uffff\1\111\1\106\1\111\3\106\4\104\1\106\1\111\1\106\1"+
        "\111\1\106\4\104\2\106";
    static final String DFA25_acceptS =
        "\2\uffff\1\2\1\1\25\uffff";
    static final String DFA25_specialS =
        "\31\uffff}>";
    static final String[] DFA25_transitionS = {
            "\1\2\2\uffff\1\2\2\uffff\1\2\1\uffff\6\2\1\uffff\3\2\1\1\5\uffff"+
            "\25\2",
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
        "\46\uffff\1\31\2\uffff\1\35\2\uffff\1\35\4\uffff";
    static final String DFA27_minS =
        "\1\35\1\42\1\35\1\43\1\uffff\3\43\2\uffff\1\103\1\65\2\uffff\2\104"+
        "\1\uffff\3\37\1\53\1\35\1\42\1\37\1\35\1\uffff\1\44\1\37\1\35\1"+
        "\uffff\1\44\1\37\1\35\1\44\4\104\1\44\2\104\1\44\2\104\1\44\1\53"+
        "\3\37";
    static final String DFA27_maxS =
        "\1\111\1\67\1\111\1\104\1\uffff\3\43\2\uffff\1\104\1\66\2\uffff"+
        "\2\104\1\uffff\3\106\1\67\1\111\1\67\1\106\1\111\1\uffff\2\106\1"+
        "\111\1\uffff\2\106\1\111\1\106\4\104\1\75\2\104\1\75\2\104\1\75"+
        "\1\67\3\106";
    static final String DFA27_acceptS =
        "\4\uffff\1\5\3\uffff\1\1\1\4\2\uffff\1\2\1\3\2\uffff\1\6\10\uffff"+
        "\1\7\3\uffff\1\10\23\uffff";
    static final String DFA27_specialS =
        "\61\uffff}>";
    static final String[] DFA27_transitionS = {
            "\1\10\2\uffff\1\10\4\uffff\6\10\1\uffff\4\10\5\uffff\4\10\1"+
            "\3\1\5\1\6\1\7\4\10\2\4\1\1\1\2\2\4\3\10",
            "\1\12\1\10\7\uffff\1\10\3\uffff\1\13\5\10\1\14\1\15\1\11",
            "\1\16\2\uffff\1\16\4\uffff\6\16\1\uffff\4\16\5\uffff\14\16"+
            "\2\uffff\1\17\3\uffff\3\16",
            "\1\10\37\uffff\2\20",
            "",
            "\1\21",
            "\1\22",
            "\1\23",
            "",
            "",
            "\1\24\1\25",
            "\1\14\1\15",
            "",
            "",
            "\1\26",
            "\1\26",
            "",
            "\1\31\4\uffff\1\10\34\uffff\1\10\1\32\1\27\1\30\2\10",
            "\1\35\4\uffff\1\10\34\uffff\1\10\1\36\1\33\1\34\2\10",
            "\1\35\4\uffff\1\10\34\uffff\1\10\1\41\1\37\1\40\2\10",
            "\1\10\3\uffff\1\13\5\10\1\14\1\15\1\11",
            "\1\42\2\uffff\1\42\4\uffff\6\42\1\uffff\4\42\5\uffff\14\42"+
            "\2\uffff\1\43\3\uffff\3\42",
            "\1\12\10\uffff\1\10\3\uffff\1\13\5\10\1\14\1\15\1\11",
            "\1\31\2\uffff\1\10\1\uffff\1\10\34\uffff\6\10",
            "\1\44\2\uffff\1\44\4\uffff\6\44\1\uffff\4\44\5\uffff\14\44"+
            "\2\uffff\1\45\3\uffff\3\44",
            "",
            "\1\46\34\uffff\6\10",
            "\1\35\2\uffff\1\10\1\uffff\1\10\34\uffff\6\10",
            "\1\47\2\uffff\1\47\4\uffff\6\47\1\uffff\4\47\5\uffff\14\47"+
            "\2\uffff\1\50\3\uffff\3\47",
            "",
            "\1\51\34\uffff\6\10",
            "\1\35\2\uffff\1\10\1\uffff\1\10\34\uffff\6\10",
            "\1\52\2\uffff\1\52\4\uffff\6\52\1\uffff\4\52\5\uffff\14\52"+
            "\2\uffff\1\53\3\uffff\3\52",
            "\1\54\34\uffff\6\10",
            "\1\55",
            "\1\55",
            "\1\56",
            "\1\56",
            "\1\31\6\uffff\1\10\1\uffff\2\31\1\uffff\5\10\10\uffff\1\31",
            "\1\57",
            "\1\57",
            "\1\35\6\uffff\1\10\1\uffff\2\35\1\uffff\5\10\10\uffff\1\35",
            "\1\60",
            "\1\60",
            "\1\35\6\uffff\1\10\1\uffff\2\35\1\uffff\5\10\10\uffff\1\35",
            "\1\10\3\uffff\1\13\5\10\1\14\1\15\1\11",
            "\1\31\2\uffff\1\10\1\uffff\1\10\34\uffff\6\10",
            "\1\35\2\uffff\1\10\1\uffff\1\10\34\uffff\6\10",
            "\1\35\2\uffff\1\10\1\uffff\1\10\34\uffff\6\10"
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
    static final String DFA35_eotS =
        "\17\uffff";
    static final String DFA35_eofS =
        "\17\uffff";
    static final String DFA35_minS =
        "\1\103\1\42\1\35\1\57\1\103\2\104\2\uffff\1\67\1\35\1\42\2\104\1"+
        "\67";
    static final String DFA35_maxS =
        "\1\104\1\67\1\111\1\70\3\104\2\uffff\1\67\1\111\1\67\2\104\1\67";
    static final String DFA35_acceptS =
        "\7\uffff\1\2\1\1\6\uffff";
    static final String DFA35_specialS =
        "\17\uffff}>";
    static final String[] DFA35_transitionS = {
            "\1\1\1\2",
            "\1\4\24\uffff\1\3",
            "\1\5\2\uffff\1\5\4\uffff\6\5\1\uffff\4\5\5\uffff\14\5\2\uffff"+
            "\1\6\3\uffff\3\5",
            "\1\7\10\uffff\1\10",
            "\1\11\1\12",
            "\1\13",
            "\1\13",
            "",
            "",
            "\1\3",
            "\1\14\2\uffff\1\14\4\uffff\6\14\1\uffff\4\14\5\uffff\14\14"+
            "\2\uffff\1\15\3\uffff\3\14",
            "\1\4\24\uffff\1\3",
            "\1\16",
            "\1\16",
            "\1\3"
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
            return "350:1: nullPredicate : ( ( ( columnReference )=> columnReference | multiValuedColumnReference ) IS NULL -> ^( PRED_EXISTS columnReference NOT ) | ( ( columnReference )=> columnReference | multiValuedColumnReference ) IS NOT NULL -> ^( PRED_EXISTS columnReference ) );";
        }
    }
    static final String DFA42_eotS =
        "\16\uffff";
    static final String DFA42_eofS =
        "\1\uffff\1\4\6\uffff\1\4\1\uffff\1\4\2\uffff\1\4";
    static final String DFA42_minS =
        "\1\103\1\37\1\35\1\103\2\uffff\2\104\1\37\1\35\1\37\2\104\1\37";
    static final String DFA42_maxS =
        "\1\104\1\100\1\111\1\104\2\uffff\2\104\1\100\1\111\1\100\2\104\1"+
        "\100";
    static final String DFA42_acceptS =
        "\4\uffff\1\1\1\2\10\uffff";
    static final String DFA42_specialS =
        "\16\uffff}>";
    static final String[] DFA42_transitionS = {
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
            return "392:1: sortSpecification : ( columnReference -> ^( SORT_SPECIFICATION columnReference ASC ) | columnReference (by= ASC | by= DESC ) -> ^( SORT_SPECIFICATION columnReference $by) );";
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
    public static final BitSet FOLLOW_valueExpression_in_selectSubList291 = new BitSet(new long[]{0x0000000100000002L,0x0000000000000018L});
    public static final BitSet FOLLOW_AS_in_selectSubList295 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000018L});
    public static final BitSet FOLLOW_columnName_in_selectSubList298 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_qualifier_in_selectSubList319 = new BitSet(new long[]{0x0000000200000000L});
    public static final BitSet FOLLOW_DOTSTAR_in_selectSubList321 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_multiValuedColumnReference_in_selectSubList337 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_columnReference_in_valueExpression356 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_valueFunction_in_valueExpression369 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_qualifier_in_columnReference392 = new BitSet(new long[]{0x0000000400000000L});
    public static final BitSet FOLLOW_DOT_in_columnReference394 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000018L});
    public static final BitSet FOLLOW_columnName_in_columnReference399 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_qualifier_in_multiValuedColumnReference435 = new BitSet(new long[]{0x0000000400000000L});
    public static final BitSet FOLLOW_DOT_in_multiValuedColumnReference437 = new BitSet(new long[]{0xFFE0F7E160000000L,0x0000000000000399L});
    public static final BitSet FOLLOW_multiValuedColumnName_in_multiValuedColumnReference443 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_keyWordOrId_in_valueFunction470 = new BitSet(new long[]{0x0000000800000000L});
    public static final BitSet FOLLOW_LPAREN_in_valueFunction472 = new BitSet(new long[]{0x0000001000000000L,0x000000000000007EL});
    public static final BitSet FOLLOW_functionArgument_in_valueFunction474 = new BitSet(new long[]{0x0000001000000000L,0x000000000000007EL});
    public static final BitSet FOLLOW_RPAREN_in_valueFunction477 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_qualifier_in_functionArgument512 = new BitSet(new long[]{0x0000000400000000L});
    public static final BitSet FOLLOW_DOT_in_functionArgument514 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000018L});
    public static final BitSet FOLLOW_columnName_in_functionArgument516 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_identifier_in_functionArgument540 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_literalOrParameterName_in_functionArgument550 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_tableName_in_qualifier571 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_correlationName_in_qualifier583 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FROM_in_fromClause620 = new BitSet(new long[]{0x0000000800000000L,0x0000000000000018L});
    public static final BitSet FOLLOW_tableReference_in_fromClause622 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_singleTable_in_tableReference640 = new BitSet(new long[]{0x000001C000000002L});
    public static final BitSet FOLLOW_joinedTable_in_tableReference649 = new BitSet(new long[]{0x000001C000000002L});
    public static final BitSet FOLLOW_tableName_in_singleTable678 = new BitSet(new long[]{0x0000000100000002L,0x0000000000000018L});
    public static final BitSet FOLLOW_AS_in_singleTable682 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000018L});
    public static final BitSet FOLLOW_correlationName_in_singleTable685 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_singleTable706 = new BitSet(new long[]{0x0000000800000000L,0x0000000000000018L});
    public static final BitSet FOLLOW_joinedTables_in_singleTable708 = new BitSet(new long[]{0x0000001000000000L});
    public static final BitSet FOLLOW_RPAREN_in_singleTable710 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_joinType_in_joinedTable732 = new BitSet(new long[]{0x0000004000000000L});
    public static final BitSet FOLLOW_JOIN_in_joinedTable735 = new BitSet(new long[]{0x0000000800000000L,0x0000000000000018L});
    public static final BitSet FOLLOW_tableReference_in_joinedTable737 = new BitSet(new long[]{0x0000040000000002L});
    public static final BitSet FOLLOW_joinSpecification_in_joinedTable746 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_singleTable_in_joinedTables777 = new BitSet(new long[]{0x000001C000000000L});
    public static final BitSet FOLLOW_joinedTable_in_joinedTables779 = new BitSet(new long[]{0x000001C000000002L});
    public static final BitSet FOLLOW_INNER_in_joinType806 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LEFT_in_joinType818 = new BitSet(new long[]{0x0000020000000002L});
    public static final BitSet FOLLOW_OUTER_in_joinType820 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ON_in_joinSpecification840 = new BitSet(new long[]{0x0000000800000000L});
    public static final BitSet FOLLOW_LPAREN_in_joinSpecification842 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000018L});
    public static final BitSet FOLLOW_columnReference_in_joinSpecification846 = new BitSet(new long[]{0x0000080000000000L});
    public static final BitSet FOLLOW_EQUALS_in_joinSpecification848 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000018L});
    public static final BitSet FOLLOW_columnReference_in_joinSpecification852 = new BitSet(new long[]{0x0000001000000000L});
    public static final BitSet FOLLOW_RPAREN_in_joinSpecification854 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_WHERE_in_whereClause904 = new BitSet(new long[]{0xFFE0F7E960000000L,0x00000000000003FFL});
    public static final BitSet FOLLOW_searchOrCondition_in_whereClause906 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_searchAndCondition_in_searchOrCondition926 = new BitSet(new long[]{0x0000200000000002L});
    public static final BitSet FOLLOW_OR_in_searchOrCondition929 = new BitSet(new long[]{0xFFE0F7E960000000L,0x00000000000003FFL});
    public static final BitSet FOLLOW_searchAndCondition_in_searchOrCondition931 = new BitSet(new long[]{0x0000200000000002L});
    public static final BitSet FOLLOW_searchNotCondition_in_searchAndCondition959 = new BitSet(new long[]{0x0000400000000002L});
    public static final BitSet FOLLOW_AND_in_searchAndCondition962 = new BitSet(new long[]{0xFFE0F7E960000000L,0x00000000000003FFL});
    public static final BitSet FOLLOW_searchNotCondition_in_searchAndCondition964 = new BitSet(new long[]{0x0000400000000002L});
    public static final BitSet FOLLOW_NOT_in_searchNotCondition991 = new BitSet(new long[]{0xFFE0F7E960000000L,0x00000000000003FFL});
    public static final BitSet FOLLOW_searchTest_in_searchNotCondition993 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_searchTest_in_searchNotCondition1008 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_predicate_in_searchTest1026 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_searchTest1037 = new BitSet(new long[]{0xFFE0F7E960000000L,0x00000000000003FFL});
    public static final BitSet FOLLOW_searchOrCondition_in_searchTest1039 = new BitSet(new long[]{0x0000001000000000L});
    public static final BitSet FOLLOW_RPAREN_in_searchTest1041 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_comparisonPredicate_in_predicate1058 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_inPredicate_in_predicate1063 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_likePredicate_in_predicate1068 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_nullPredicate_in_predicate1073 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_quantifiedComparisonPredicate_in_predicate1079 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_quantifiedInPredicate_in_predicate1084 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_textSearchPredicate_in_predicate1089 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_folderPredicate_in_predicate1094 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_valueExpression_in_comparisonPredicate1106 = new BitSet(new long[]{0x001F080000000000L});
    public static final BitSet FOLLOW_compOp_in_comparisonPredicate1108 = new BitSet(new long[]{0x0000000000000000L,0x000000000000007EL});
    public static final BitSet FOLLOW_literalOrParameterName_in_comparisonPredicate1110 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_compOp0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_literal_in_literalOrParameterName1176 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_parameterName_in_literalOrParameterName1181 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_signedNumericLiteral_in_literal1194 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_characterStringLiteral_in_literal1199 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_columnReference_in_inPredicate1211 = new BitSet(new long[]{0x0020800000000000L});
    public static final BitSet FOLLOW_NOT_in_inPredicate1213 = new BitSet(new long[]{0x0020000000000000L});
    public static final BitSet FOLLOW_IN_in_inPredicate1216 = new BitSet(new long[]{0x0000000800000000L});
    public static final BitSet FOLLOW_LPAREN_in_inPredicate1218 = new BitSet(new long[]{0x0000000000000000L,0x000000000000007EL});
    public static final BitSet FOLLOW_inValueList_in_inPredicate1220 = new BitSet(new long[]{0x0000001000000000L});
    public static final BitSet FOLLOW_RPAREN_in_inPredicate1222 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_literalOrParameterName_in_inValueList1251 = new BitSet(new long[]{0x0000000080000002L});
    public static final BitSet FOLLOW_COMMA_in_inValueList1254 = new BitSet(new long[]{0x0000000000000000L,0x000000000000007EL});
    public static final BitSet FOLLOW_literalOrParameterName_in_inValueList1256 = new BitSet(new long[]{0x0000000080000002L});
    public static final BitSet FOLLOW_columnReference_in_likePredicate1282 = new BitSet(new long[]{0x0040800000000000L});
    public static final BitSet FOLLOW_NOT_in_likePredicate1284 = new BitSet(new long[]{0x0040000000000000L});
    public static final BitSet FOLLOW_LIKE_in_likePredicate1287 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000064L});
    public static final BitSet FOLLOW_characterStringLiteral_in_likePredicate1289 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_columnReference_in_nullPredicate1323 = new BitSet(new long[]{0x0080000000000000L});
    public static final BitSet FOLLOW_multiValuedColumnReference_in_nullPredicate1327 = new BitSet(new long[]{0x0080000000000000L});
    public static final BitSet FOLLOW_IS_in_nullPredicate1330 = new BitSet(new long[]{0x0100000000000000L});
    public static final BitSet FOLLOW_NULL_in_nullPredicate1332 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_columnReference_in_nullPredicate1361 = new BitSet(new long[]{0x0080000000000000L});
    public static final BitSet FOLLOW_multiValuedColumnReference_in_nullPredicate1365 = new BitSet(new long[]{0x0080000000000000L});
    public static final BitSet FOLLOW_IS_in_nullPredicate1368 = new BitSet(new long[]{0x0000800000000000L});
    public static final BitSet FOLLOW_NOT_in_nullPredicate1370 = new BitSet(new long[]{0x0100000000000000L});
    public static final BitSet FOLLOW_NULL_in_nullPredicate1372 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_literalOrParameterName_in_quantifiedComparisonPredicate1400 = new BitSet(new long[]{0x001F080000000000L});
    public static final BitSet FOLLOW_compOp_in_quantifiedComparisonPredicate1402 = new BitSet(new long[]{0x0200000000000000L});
    public static final BitSet FOLLOW_ANY_in_quantifiedComparisonPredicate1404 = new BitSet(new long[]{0xFFE0F7E160000000L,0x0000000000000399L});
    public static final BitSet FOLLOW_multiValuedColumnReference_in_quantifiedComparisonPredicate1406 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ANY_in_quantifiedInPredicate1435 = new BitSet(new long[]{0xFFE0F7E160000000L,0x0000000000000399L});
    public static final BitSet FOLLOW_multiValuedColumnReference_in_quantifiedInPredicate1437 = new BitSet(new long[]{0x0020800000000000L});
    public static final BitSet FOLLOW_NOT_in_quantifiedInPredicate1439 = new BitSet(new long[]{0x0020000000000000L});
    public static final BitSet FOLLOW_IN_in_quantifiedInPredicate1442 = new BitSet(new long[]{0x0000000800000000L});
    public static final BitSet FOLLOW_LPAREN_in_quantifiedInPredicate1445 = new BitSet(new long[]{0x0000000000000000L,0x000000000000007EL});
    public static final BitSet FOLLOW_inValueList_in_quantifiedInPredicate1447 = new BitSet(new long[]{0x0000001000000000L});
    public static final BitSet FOLLOW_RPAREN_in_quantifiedInPredicate1449 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_CONTAINS_in_textSearchPredicate1478 = new BitSet(new long[]{0x0000000800000000L});
    public static final BitSet FOLLOW_LPAREN_in_textSearchPredicate1480 = new BitSet(new long[]{0x0000000080000000L,0x000000000000001CL});
    public static final BitSet FOLLOW_qualifier_in_textSearchPredicate1483 = new BitSet(new long[]{0x0000000080000000L});
    public static final BitSet FOLLOW_COMMA_in_textSearchPredicate1485 = new BitSet(new long[]{0x0000000080000000L,0x000000000000001CL});
    public static final BitSet FOLLOW_COMMA_in_textSearchPredicate1489 = new BitSet(new long[]{0x0000000080000000L,0x000000000000001CL});
    public static final BitSet FOLLOW_textSearchExpression_in_textSearchPredicate1493 = new BitSet(new long[]{0x0000001000000000L});
    public static final BitSet FOLLOW_RPAREN_in_textSearchPredicate1495 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IN_FOLDER_in_folderPredicate1520 = new BitSet(new long[]{0x0000000800000000L});
    public static final BitSet FOLLOW_folderPredicateArgs_in_folderPredicate1523 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IN_TREE_in_folderPredicate1544 = new BitSet(new long[]{0x0000000800000000L});
    public static final BitSet FOLLOW_folderPredicateArgs_in_folderPredicate1546 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_folderPredicateArgs1568 = new BitSet(new long[]{0x0000000080000000L,0x000000000000007CL});
    public static final BitSet FOLLOW_qualifier_in_folderPredicateArgs1571 = new BitSet(new long[]{0x0000000080000000L});
    public static final BitSet FOLLOW_COMMA_in_folderPredicateArgs1573 = new BitSet(new long[]{0x0000000080000000L,0x000000000000007CL});
    public static final BitSet FOLLOW_COMMA_in_folderPredicateArgs1577 = new BitSet(new long[]{0x0000000080000000L,0x000000000000007CL});
    public static final BitSet FOLLOW_folderId_in_folderPredicateArgs1581 = new BitSet(new long[]{0x0000001000000000L});
    public static final BitSet FOLLOW_RPAREN_in_folderPredicateArgs1583 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ORDER_in_orderByClause1622 = new BitSet(new long[]{0x4000000000000000L});
    public static final BitSet FOLLOW_BY_in_orderByClause1624 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000018L});
    public static final BitSet FOLLOW_sortSpecification_in_orderByClause1626 = new BitSet(new long[]{0x0000000080000002L});
    public static final BitSet FOLLOW_COMMA_in_orderByClause1630 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000018L});
    public static final BitSet FOLLOW_sortSpecification_in_orderByClause1632 = new BitSet(new long[]{0x0000000080000002L});
    public static final BitSet FOLLOW_columnReference_in_sortSpecification1658 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_columnReference_in_sortSpecification1676 = new BitSet(new long[]{0x8000000000000000L,0x0000000000000001L});
    public static final BitSet FOLLOW_ASC_in_sortSpecification1682 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DESC_in_sortSpecification1688 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_identifier_in_correlationName1715 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_identifier_in_tableName1729 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_identifier_in_columnName1747 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_identifier_in_multiValuedColumnName1766 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_COLON_in_parameterName1784 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000018L});
    public static final BitSet FOLLOW_identifier_in_parameterName1786 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_characterStringLiteral_in_folderId1809 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_QUOTED_STRING_in_textSearchExpression1830 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_identifier1842 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DOUBLE_QUOTE_in_identifier1853 = new BitSet(new long[]{0xFFE0F7E120000000L,0x0000000000000399L});
    public static final BitSet FOLLOW_keyWordOrId_in_identifier1855 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000010L});
    public static final BitSet FOLLOW_DOUBLE_QUOTE_in_identifier1857 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FLOATING_POINT_LITERAL_in_signedNumericLiteral1877 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_integerLiteral_in_signedNumericLiteral1892 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DECIMAL_INTEGER_LITERAL_in_integerLiteral1911 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_QUOTED_STRING_in_characterStringLiteral1934 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_keyWord0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_keyWord_in_keyWordOrId2143 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_keyWordOrId2155 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_valueExpression_in_synpred1_CMIS287 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_tableName_in_synpred2_CMIS566 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_joinedTable_in_synpred3_CMIS644 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_joinSpecification_in_synpred4_CMIS741 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_columnReference_in_synpred5_CMIS1319 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_columnReference_in_synpred6_CMIS1357 = new BitSet(new long[]{0x0000000000000002L});

}