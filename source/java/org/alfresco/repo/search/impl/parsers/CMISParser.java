// $ANTLR !Unknown version! W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g 2009-10-13 15:31:44
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
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "QUERY", "ALL_COLUMNS", "COLUMN", "COLUMNS", "COLUMN_REF", "QUALIFIER", "FUNCTION", "SOURCE", "TABLE", "TABLE_REF", "PARAMETER", "CONJUNCTION", "DISJUNCTION", "NEGATION", "PRED_COMPARISON", "PRED_IN", "PRED_EXISTS", "PRED_LIKE", "PRED_FTS", "LIST", "PRED_CHILD", "PRED_DESCENDANT", "SORT_SPECIFICATION", "NUMERIC_LITERAL", "STRING_LITERAL", "DATETIME_LITERAL", "BOOLEAN_LITERAL", "SELECT", "STAR", "COMMA", "AS", "DOTSTAR", "DOT", "LPAREN", "RPAREN", "FROM", "JOIN", "INNER", "LEFT", "OUTER", "ON", "EQUALS", "WHERE", "OR", "AND", "NOT", "NOTEQUALS", "LESSTHAN", "GREATERTHAN", "LESSTHANOREQUALS", "GREATERTHANOREQUALS", "IN", "LIKE", "IS", "NULL", "ANY", "CONTAINS", "IN_FOLDER", "IN_TREE", "ORDER", "BY", "ASC", "DESC", "COLON", "QUOTED_STRING", "ID", "DOUBLE_QUOTE", "FLOATING_POINT_LITERAL", "DECIMAL_INTEGER_LITERAL", "TRUE", "FALSE", "TIMESTAMP", "DOTDOT", "TILDA", "PLUS", "MINUS", "DECIMAL_NUMERAL", "DIGIT", "EXPONENT", "WS", "ZERO_DIGIT", "NON_ZERO_DIGIT", "E", "SIGNED_INTEGER"
    };
    public static final int FUNCTION=10;
    public static final int WHERE=46;
    public static final int EXPONENT=82;
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
    public static final int DOTDOT=76;
    public static final int EQUALS=45;
    public static final int NOT=49;
    public static final int ID=69;
    public static final int AND=48;
    public static final int EOF=-1;
    public static final int LPAREN=37;
    public static final int LESSTHANOREQUALS=53;
    public static final int AS=34;
    public static final int RPAREN=38;
    public static final int TILDA=77;
    public static final int PRED_LIKE=21;
    public static final int STRING_LITERAL=28;
    public static final int IN=55;
    public static final int DECIMAL_NUMERAL=80;
    public static final int FLOATING_POINT_LITERAL=71;
    public static final int COMMA=33;
    public static final int IS=57;
    public static final int LEFT=42;
    public static final int SIGNED_INTEGER=87;
    public static final int PARAMETER=14;
    public static final int COLUMN=6;
    public static final int PLUS=78;
    public static final int QUOTED_STRING=68;
    public static final int ZERO_DIGIT=84;
    public static final int DIGIT=81;
    public static final int DOT=36;
    public static final int COLUMN_REF=8;
    public static final int SELECT=31;
    public static final int LIKE=56;
    public static final int GREATERTHAN=52;
    public static final int DOTSTAR=35;
    public static final int E=86;
    public static final int OUTER=43;
    public static final int BY=64;
    public static final int LESSTHAN=51;
    public static final int NON_ZERO_DIGIT=85;
    public static final int ASC=65;
    public static final int QUALIFIER=9;
    public static final int CONJUNCTION=15;
    public static final int NULL=58;
    public static final int ON=44;
    public static final int NOTEQUALS=50;
    public static final int DATETIME_LITERAL=29;
    public static final int MINUS=79;
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
    public static final int WS=83;
    public static final int ANY=59;
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
        
        private boolean strict = true;

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
    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:151:1: query : SELECT selectList fromClause ( whereClause )? ( orderByClause )? EOF -> ^( QUERY selectList fromClause ( whereClause )? ( orderByClause )? ) ;
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
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:158:2: ( SELECT selectList fromClause ( whereClause )? ( orderByClause )? EOF -> ^( QUERY selectList fromClause ( whereClause )? ( orderByClause )? ) )
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:158:4: SELECT selectList fromClause ( whereClause )? ( orderByClause )? EOF
            {
            SELECT1=(Token)match(input,SELECT,FOLLOW_SELECT_in_query180); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_SELECT.add(SELECT1);

            pushFollow(FOLLOW_selectList_in_query182);
            selectList2=selectList();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_selectList.add(selectList2.getTree());
            pushFollow(FOLLOW_fromClause_in_query184);
            fromClause3=fromClause();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_fromClause.add(fromClause3.getTree());
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:158:33: ( whereClause )?
            int alt1=2;
            int LA1_0 = input.LA(1);

            if ( (LA1_0==WHERE) ) {
                alt1=1;
            }
            switch (alt1) {
                case 1 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:158:33: whereClause
                    {
                    pushFollow(FOLLOW_whereClause_in_query186);
                    whereClause4=whereClause();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_whereClause.add(whereClause4.getTree());

                    }
                    break;

            }

            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:158:46: ( orderByClause )?
            int alt2=2;
            int LA2_0 = input.LA(1);

            if ( (LA2_0==ORDER) ) {
                alt2=1;
            }
            switch (alt2) {
                case 1 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:158:46: orderByClause
                    {
                    pushFollow(FOLLOW_orderByClause_in_query189);
                    orderByClause5=orderByClause();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_orderByClause.add(orderByClause5.getTree());

                    }
                    break;

            }

            EOF6=(Token)match(input,EOF,FOLLOW_EOF_in_query192); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_EOF.add(EOF6);



            // AST REWRITE
            // elements: selectList, whereClause, fromClause, orderByClause
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 159:3: -> ^( QUERY selectList fromClause ( whereClause )? ( orderByClause )? )
            {
                // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:159:6: ^( QUERY selectList fromClause ( whereClause )? ( orderByClause )? )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(QUERY, "QUERY"), root_1);

                adaptor.addChild(root_1, stream_selectList.nextTree());
                adaptor.addChild(root_1, stream_fromClause.nextTree());
                // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:159:36: ( whereClause )?
                if ( stream_whereClause.hasNext() ) {
                    adaptor.addChild(root_1, stream_whereClause.nextTree());

                }
                stream_whereClause.reset();
                // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:159:49: ( orderByClause )?
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
    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:163:1: selectList : ( STAR -> ^( ALL_COLUMNS ) | selectSubList ( COMMA selectSubList )* -> ^( COLUMNS ( selectSubList )+ ) );
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
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:166:2: ( STAR -> ^( ALL_COLUMNS ) | selectSubList ( COMMA selectSubList )* -> ^( COLUMNS ( selectSubList )+ ) )
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
            else if ( (LA4_0==SELECT||LA4_0==AS||(LA4_0>=FROM && LA4_0<=ON)||(LA4_0>=WHERE && LA4_0<=NOT)||(LA4_0>=IN && LA4_0<=DESC)||(LA4_0>=TRUE && LA4_0<=TIMESTAMP)) ) {
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
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:166:4: STAR
                    {
                    STAR7=(Token)match(input,STAR,FOLLOW_STAR_in_selectList241); if (state.failed) return retval; 
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
                    // 167:3: -> ^( ALL_COLUMNS )
                    {
                        // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:167:6: ^( ALL_COLUMNS )
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
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:168:5: selectSubList ( COMMA selectSubList )*
                    {
                    pushFollow(FOLLOW_selectSubList_in_selectList257);
                    selectSubList8=selectSubList();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_selectSubList.add(selectSubList8.getTree());
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:168:19: ( COMMA selectSubList )*
                    loop3:
                    do {
                        int alt3=2;
                        int LA3_0 = input.LA(1);

                        if ( (LA3_0==COMMA) ) {
                            alt3=1;
                        }


                        switch (alt3) {
                    	case 1 :
                    	    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:168:21: COMMA selectSubList
                    	    {
                    	    COMMA9=(Token)match(input,COMMA,FOLLOW_COMMA_in_selectList261); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_COMMA.add(COMMA9);

                    	    pushFollow(FOLLOW_selectSubList_in_selectList263);
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
                    // 169:3: -> ^( COLUMNS ( selectSubList )+ )
                    {
                        // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:169:6: ^( COLUMNS ( selectSubList )+ )
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
    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:173:1: selectSubList : ( ( valueExpression )=> valueExpression ( ( AS )? columnName )? -> ^( COLUMN valueExpression ( columnName )? ) | qualifier DOTSTAR -> ^( ALL_COLUMNS qualifier ) | multiValuedColumnReference ->);
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
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:174:2: ( ( valueExpression )=> valueExpression ( ( AS )? columnName )? -> ^( COLUMN valueExpression ( columnName )? ) | qualifier DOTSTAR -> ^( ALL_COLUMNS qualifier ) | multiValuedColumnReference ->)
            int alt7=3;
            alt7 = dfa7.predict(input);
            switch (alt7) {
                case 1 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:174:4: ( valueExpression )=> valueExpression ( ( AS )? columnName )?
                    {
                    pushFollow(FOLLOW_valueExpression_in_selectSubList299);
                    valueExpression11=valueExpression();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_valueExpression.add(valueExpression11.getTree());
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:174:40: ( ( AS )? columnName )?
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
                            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:174:42: ( AS )? columnName
                            {
                            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:174:42: ( AS )?
                            int alt5=2;
                            int LA5_0 = input.LA(1);

                            if ( (LA5_0==AS) ) {
                                alt5=1;
                            }
                            switch (alt5) {
                                case 1 :
                                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:174:42: AS
                                    {
                                    AS12=(Token)match(input,AS,FOLLOW_AS_in_selectSubList303); if (state.failed) return retval; 
                                    if ( state.backtracking==0 ) stream_AS.add(AS12);


                                    }
                                    break;

                            }

                            pushFollow(FOLLOW_columnName_in_selectSubList306);
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
                    // 175:3: -> ^( COLUMN valueExpression ( columnName )? )
                    {
                        // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:175:6: ^( COLUMN valueExpression ( columnName )? )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(COLUMN, "COLUMN"), root_1);

                        adaptor.addChild(root_1, stream_valueExpression.nextTree());
                        // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:175:31: ( columnName )?
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
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:176:4: qualifier DOTSTAR
                    {
                    pushFollow(FOLLOW_qualifier_in_selectSubList327);
                    qualifier14=qualifier();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_qualifier.add(qualifier14.getTree());
                    DOTSTAR15=(Token)match(input,DOTSTAR,FOLLOW_DOTSTAR_in_selectSubList329); if (state.failed) return retval; 
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
                    // 177:3: -> ^( ALL_COLUMNS qualifier )
                    {
                        // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:177:6: ^( ALL_COLUMNS qualifier )
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
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:178:4: multiValuedColumnReference
                    {
                    pushFollow(FOLLOW_multiValuedColumnReference_in_selectSubList345);
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
                    // 179:3: ->
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
    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:182:1: valueExpression : ( columnReference -> columnReference | valueFunction -> valueFunction );
    public final CMISParser.valueExpression_return valueExpression() throws RecognitionException {
        CMISParser.valueExpression_return retval = new CMISParser.valueExpression_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        CMISParser.columnReference_return columnReference17 = null;

        CMISParser.valueFunction_return valueFunction18 = null;


        RewriteRuleSubtreeStream stream_valueFunction=new RewriteRuleSubtreeStream(adaptor,"rule valueFunction");
        RewriteRuleSubtreeStream stream_columnReference=new RewriteRuleSubtreeStream(adaptor,"rule columnReference");
        try {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:183:2: ( columnReference -> columnReference | valueFunction -> valueFunction )
            int alt8=2;
            int LA8_0 = input.LA(1);

            if ( (LA8_0==ID) ) {
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
            else if ( (LA8_0==DOUBLE_QUOTE) && ((strict == false))) {
                alt8=1;
            }
            else if ( (LA8_0==SELECT||LA8_0==AS||(LA8_0>=FROM && LA8_0<=ON)||(LA8_0>=WHERE && LA8_0<=NOT)||(LA8_0>=IN && LA8_0<=DESC)||(LA8_0>=TRUE && LA8_0<=TIMESTAMP)) ) {
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
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:183:4: columnReference
                    {
                    pushFollow(FOLLOW_columnReference_in_valueExpression364);
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
                    // 184:3: -> columnReference
                    {
                        adaptor.addChild(root_0, stream_columnReference.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:185:5: valueFunction
                    {
                    pushFollow(FOLLOW_valueFunction_in_valueExpression377);
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
                    // 186:3: -> valueFunction
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
    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:189:1: columnReference : ( qualifier DOT )? columnName -> ^( COLUMN_REF columnName ( qualifier )? ) ;
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
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:190:2: ( ( qualifier DOT )? columnName -> ^( COLUMN_REF columnName ( qualifier )? ) )
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:190:4: ( qualifier DOT )? columnName
            {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:190:4: ( qualifier DOT )?
            int alt9=2;
            alt9 = dfa9.predict(input);
            switch (alt9) {
                case 1 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:190:6: qualifier DOT
                    {
                    pushFollow(FOLLOW_qualifier_in_columnReference400);
                    qualifier19=qualifier();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_qualifier.add(qualifier19.getTree());
                    DOT20=(Token)match(input,DOT,FOLLOW_DOT_in_columnReference402); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_DOT.add(DOT20);


                    }
                    break;

            }

            pushFollow(FOLLOW_columnName_in_columnReference407);
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
            // 191:3: -> ^( COLUMN_REF columnName ( qualifier )? )
            {
                // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:191:6: ^( COLUMN_REF columnName ( qualifier )? )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(COLUMN_REF, "COLUMN_REF"), root_1);

                adaptor.addChild(root_1, stream_columnName.nextTree());
                // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:191:30: ( qualifier )?
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
    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:198:1: multiValuedColumnReference : ( qualifier DOT )? multiValuedColumnName -> ^( COLUMN_REF multiValuedColumnName ( qualifier )? ) ;
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
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:199:2: ( ( qualifier DOT )? multiValuedColumnName -> ^( COLUMN_REF multiValuedColumnName ( qualifier )? ) )
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:199:10: ( qualifier DOT )? multiValuedColumnName
            {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:199:10: ( qualifier DOT )?
            int alt10=2;
            alt10 = dfa10.predict(input);
            switch (alt10) {
                case 1 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:199:12: qualifier DOT
                    {
                    pushFollow(FOLLOW_qualifier_in_multiValuedColumnReference443);
                    qualifier22=qualifier();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_qualifier.add(qualifier22.getTree());
                    DOT23=(Token)match(input,DOT,FOLLOW_DOT_in_multiValuedColumnReference445); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_DOT.add(DOT23);


                    }
                    break;

            }

            pushFollow(FOLLOW_multiValuedColumnName_in_multiValuedColumnReference451);
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
            // 200:3: -> ^( COLUMN_REF multiValuedColumnName ( qualifier )? )
            {
                // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:200:6: ^( COLUMN_REF multiValuedColumnName ( qualifier )? )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(COLUMN_REF, "COLUMN_REF"), root_1);

                adaptor.addChild(root_1, stream_multiValuedColumnName.nextTree());
                // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:200:41: ( qualifier )?
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
    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:203:1: valueFunction : functionName= keyWordOrId LPAREN ( functionArgument )* RPAREN -> ^( FUNCTION $functionName LPAREN ( functionArgument )* RPAREN ) ;
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
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:204:2: (functionName= keyWordOrId LPAREN ( functionArgument )* RPAREN -> ^( FUNCTION $functionName LPAREN ( functionArgument )* RPAREN ) )
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:204:4: functionName= keyWordOrId LPAREN ( functionArgument )* RPAREN
            {
            pushFollow(FOLLOW_keyWordOrId_in_valueFunction478);
            functionName=keyWordOrId();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_keyWordOrId.add(functionName.getTree());
            LPAREN25=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_valueFunction480); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_LPAREN.add(LPAREN25);

            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:204:36: ( functionArgument )*
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
            	    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:204:36: functionArgument
            	    {
            	    pushFollow(FOLLOW_functionArgument_in_valueFunction482);
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

            RPAREN27=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_valueFunction485); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_RPAREN.add(RPAREN27);



            // AST REWRITE
            // elements: RPAREN, functionArgument, LPAREN, functionName
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
            // 205:3: -> ^( FUNCTION $functionName LPAREN ( functionArgument )* RPAREN )
            {
                // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:205:6: ^( FUNCTION $functionName LPAREN ( functionArgument )* RPAREN )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(FUNCTION, "FUNCTION"), root_1);

                adaptor.addChild(root_1, stream_functionName.nextTree());
                adaptor.addChild(root_1, stream_LPAREN.nextNode());
                // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:205:38: ( functionArgument )*
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
    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:208:1: functionArgument : ( qualifier DOT columnName -> ^( COLUMN_REF columnName qualifier ) | identifier | literalOrParameterName );
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
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:209:5: ( qualifier DOT columnName -> ^( COLUMN_REF columnName qualifier ) | identifier | literalOrParameterName )
            int alt12=3;
            alt12 = dfa12.predict(input);
            switch (alt12) {
                case 1 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:209:9: qualifier DOT columnName
                    {
                    pushFollow(FOLLOW_qualifier_in_functionArgument520);
                    qualifier28=qualifier();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_qualifier.add(qualifier28.getTree());
                    DOT29=(Token)match(input,DOT,FOLLOW_DOT_in_functionArgument522); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_DOT.add(DOT29);

                    pushFollow(FOLLOW_columnName_in_functionArgument524);
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
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 210:5: -> ^( COLUMN_REF columnName qualifier )
                    {
                        // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:210:8: ^( COLUMN_REF columnName qualifier )
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
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:211:9: identifier
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_identifier_in_functionArgument548);
                    identifier31=identifier();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, identifier31.getTree());

                    }
                    break;
                case 3 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:212:9: literalOrParameterName
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_literalOrParameterName_in_functionArgument558);
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
    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:215:1: qualifier : ( ( tableName )=> tableName -> tableName | correlationName -> correlationName );
    public final CMISParser.qualifier_return qualifier() throws RecognitionException {
        CMISParser.qualifier_return retval = new CMISParser.qualifier_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        CMISParser.tableName_return tableName33 = null;

        CMISParser.correlationName_return correlationName34 = null;


        RewriteRuleSubtreeStream stream_correlationName=new RewriteRuleSubtreeStream(adaptor,"rule correlationName");
        RewriteRuleSubtreeStream stream_tableName=new RewriteRuleSubtreeStream(adaptor,"rule tableName");
        try {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:216:2: ( ( tableName )=> tableName -> tableName | correlationName -> correlationName )
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
            else if ( (LA13_0==DOUBLE_QUOTE) && ((strict == false))) {
                int LA13_2 = input.LA(2);

                if ( (LA13_2==SELECT||LA13_2==AS||(LA13_2>=FROM && LA13_2<=ON)||(LA13_2>=WHERE && LA13_2<=NOT)||(LA13_2>=IN && LA13_2<=DESC)||(LA13_2>=TRUE && LA13_2<=TIMESTAMP)) && ((strict == false))) {
                    int LA13_5 = input.LA(3);

                    if ( (LA13_5==DOUBLE_QUOTE) && ((strict == false))) {
                        int LA13_7 = input.LA(4);

                        if ( ((synpred2_CMIS()&&(strict == false))) ) {
                            alt13=1;
                        }
                        else if ( ((strict == false)) ) {
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
                else if ( (LA13_2==ID) && ((strict == false))) {
                    int LA13_6 = input.LA(3);

                    if ( (LA13_6==DOUBLE_QUOTE) && ((strict == false))) {
                        int LA13_7 = input.LA(4);

                        if ( ((synpred2_CMIS()&&(strict == false))) ) {
                            alt13=1;
                        }
                        else if ( ((strict == false)) ) {
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
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:216:4: ( tableName )=> tableName
                    {
                    pushFollow(FOLLOW_tableName_in_qualifier579);
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
                    // 217:3: -> tableName
                    {
                        adaptor.addChild(root_0, stream_tableName.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:218:5: correlationName
                    {
                    pushFollow(FOLLOW_correlationName_in_qualifier591);
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
                    // 219:3: -> correlationName
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
    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:222:1: fromClause : FROM tableReference -> tableReference ;
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
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:225:2: ( FROM tableReference -> tableReference )
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:225:4: FROM tableReference
            {
            FROM35=(Token)match(input,FROM,FOLLOW_FROM_in_fromClause628); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_FROM.add(FROM35);

            pushFollow(FOLLOW_tableReference_in_fromClause630);
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
            // 226:3: -> tableReference
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
    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:229:1: tableReference : singleTable ( ( joinedTable )=> joinedTable )* -> ^( SOURCE singleTable ( joinedTable )* ) ;
    public final CMISParser.tableReference_return tableReference() throws RecognitionException {
        CMISParser.tableReference_return retval = new CMISParser.tableReference_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        CMISParser.singleTable_return singleTable37 = null;

        CMISParser.joinedTable_return joinedTable38 = null;


        RewriteRuleSubtreeStream stream_singleTable=new RewriteRuleSubtreeStream(adaptor,"rule singleTable");
        RewriteRuleSubtreeStream stream_joinedTable=new RewriteRuleSubtreeStream(adaptor,"rule joinedTable");
        try {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:230:2: ( singleTable ( ( joinedTable )=> joinedTable )* -> ^( SOURCE singleTable ( joinedTable )* ) )
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:230:4: singleTable ( ( joinedTable )=> joinedTable )*
            {
            pushFollow(FOLLOW_singleTable_in_tableReference648);
            singleTable37=singleTable();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_singleTable.add(singleTable37.getTree());
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:230:16: ( ( joinedTable )=> joinedTable )*
            loop14:
            do {
                int alt14=2;
                int LA14_0 = input.LA(1);

                if ( (LA14_0==INNER) && (synpred3_CMIS())) {
                    alt14=1;
                }
                else if ( (LA14_0==LEFT) && (synpred3_CMIS())) {
                    alt14=1;
                }
                else if ( (LA14_0==JOIN) && (synpred3_CMIS())) {
                    alt14=1;
                }


                switch (alt14) {
            	case 1 :
            	    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:230:17: ( joinedTable )=> joinedTable
            	    {
            	    pushFollow(FOLLOW_joinedTable_in_tableReference657);
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
            // 231:3: -> ^( SOURCE singleTable ( joinedTable )* )
            {
                // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:231:6: ^( SOURCE singleTable ( joinedTable )* )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(SOURCE, "SOURCE"), root_1);

                adaptor.addChild(root_1, stream_singleTable.nextTree());
                // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:231:27: ( joinedTable )*
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
    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:237:1: singleTable : ( tableName ( ( AS )? correlationName )? -> ^( TABLE_REF tableName ( correlationName )? ) | LPAREN joinedTables RPAREN -> ^( TABLE joinedTables ) );
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
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:238:2: ( tableName ( ( AS )? correlationName )? -> ^( TABLE_REF tableName ( correlationName )? ) | LPAREN joinedTables RPAREN -> ^( TABLE joinedTables ) )
            int alt17=2;
            int LA17_0 = input.LA(1);

            if ( (LA17_0==ID) ) {
                alt17=1;
            }
            else if ( (LA17_0==DOUBLE_QUOTE) && ((strict == false))) {
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
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:238:4: tableName ( ( AS )? correlationName )?
                    {
                    pushFollow(FOLLOW_tableName_in_singleTable686);
                    tableName39=tableName();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_tableName.add(tableName39.getTree());
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:238:14: ( ( AS )? correlationName )?
                    int alt16=2;
                    int LA16_0 = input.LA(1);

                    if ( (LA16_0==AS||LA16_0==ID) ) {
                        alt16=1;
                    }
                    else if ( (LA16_0==DOUBLE_QUOTE) && ((strict == false))) {
                        alt16=1;
                    }
                    switch (alt16) {
                        case 1 :
                            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:238:16: ( AS )? correlationName
                            {
                            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:238:16: ( AS )?
                            int alt15=2;
                            int LA15_0 = input.LA(1);

                            if ( (LA15_0==AS) ) {
                                alt15=1;
                            }
                            switch (alt15) {
                                case 1 :
                                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:238:16: AS
                                    {
                                    AS40=(Token)match(input,AS,FOLLOW_AS_in_singleTable690); if (state.failed) return retval; 
                                    if ( state.backtracking==0 ) stream_AS.add(AS40);


                                    }
                                    break;

                            }

                            pushFollow(FOLLOW_correlationName_in_singleTable693);
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
                    // 239:3: -> ^( TABLE_REF tableName ( correlationName )? )
                    {
                        // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:239:6: ^( TABLE_REF tableName ( correlationName )? )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(TABLE_REF, "TABLE_REF"), root_1);

                        adaptor.addChild(root_1, stream_tableName.nextTree());
                        // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:239:28: ( correlationName )?
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
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:240:4: LPAREN joinedTables RPAREN
                    {
                    LPAREN42=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_singleTable714); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_LPAREN.add(LPAREN42);

                    pushFollow(FOLLOW_joinedTables_in_singleTable716);
                    joinedTables43=joinedTables();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_joinedTables.add(joinedTables43.getTree());
                    RPAREN44=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_singleTable718); if (state.failed) return retval; 
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
                    // 241:3: -> ^( TABLE joinedTables )
                    {
                        // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:241:6: ^( TABLE joinedTables )
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
    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:244:1: joinedTable : ( joinType )? JOIN tableReference ( joinSpecification )=> joinSpecification -> ^( JOIN tableReference ( joinType )? joinSpecification ) ;
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
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:245:2: ( ( joinType )? JOIN tableReference ( joinSpecification )=> joinSpecification -> ^( JOIN tableReference ( joinType )? joinSpecification ) )
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:245:4: ( joinType )? JOIN tableReference ( joinSpecification )=> joinSpecification
            {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:245:4: ( joinType )?
            int alt18=2;
            int LA18_0 = input.LA(1);

            if ( ((LA18_0>=INNER && LA18_0<=LEFT)) ) {
                alt18=1;
            }
            switch (alt18) {
                case 1 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:245:4: joinType
                    {
                    pushFollow(FOLLOW_joinType_in_joinedTable740);
                    joinType45=joinType();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_joinType.add(joinType45.getTree());

                    }
                    break;

            }

            JOIN46=(Token)match(input,JOIN,FOLLOW_JOIN_in_joinedTable743); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_JOIN.add(JOIN46);

            pushFollow(FOLLOW_tableReference_in_joinedTable745);
            tableReference47=tableReference();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_tableReference.add(tableReference47.getTree());
            pushFollow(FOLLOW_joinSpecification_in_joinedTable753);
            joinSpecification48=joinSpecification();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_joinSpecification.add(joinSpecification48.getTree());


            // AST REWRITE
            // elements: joinSpecification, joinType, tableReference, JOIN
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 246:3: -> ^( JOIN tableReference ( joinType )? joinSpecification )
            {
                // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:246:6: ^( JOIN tableReference ( joinType )? joinSpecification )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot(stream_JOIN.nextNode(), root_1);

                adaptor.addChild(root_1, stream_tableReference.nextTree());
                // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:246:28: ( joinType )?
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
    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:250:1: joinedTables : singleTable ( joinedTable )+ -> ^( SOURCE singleTable ( joinedTable )+ ) ;
    public final CMISParser.joinedTables_return joinedTables() throws RecognitionException {
        CMISParser.joinedTables_return retval = new CMISParser.joinedTables_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        CMISParser.singleTable_return singleTable49 = null;

        CMISParser.joinedTable_return joinedTable50 = null;


        RewriteRuleSubtreeStream stream_singleTable=new RewriteRuleSubtreeStream(adaptor,"rule singleTable");
        RewriteRuleSubtreeStream stream_joinedTable=new RewriteRuleSubtreeStream(adaptor,"rule joinedTable");
        try {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:251:2: ( singleTable ( joinedTable )+ -> ^( SOURCE singleTable ( joinedTable )+ ) )
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:251:4: singleTable ( joinedTable )+
            {
            pushFollow(FOLLOW_singleTable_in_joinedTables781);
            singleTable49=singleTable();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_singleTable.add(singleTable49.getTree());
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:251:16: ( joinedTable )+
            int cnt19=0;
            loop19:
            do {
                int alt19=2;
                int LA19_0 = input.LA(1);

                if ( ((LA19_0>=JOIN && LA19_0<=LEFT)) ) {
                    alt19=1;
                }


                switch (alt19) {
            	case 1 :
            	    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:251:16: joinedTable
            	    {
            	    pushFollow(FOLLOW_joinedTable_in_joinedTables783);
            	    joinedTable50=joinedTable();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_joinedTable.add(joinedTable50.getTree());

            	    }
            	    break;

            	default :
            	    if ( cnt19 >= 1 ) break loop19;
            	    if (state.backtracking>0) {state.failed=true; return retval;}
                        EarlyExitException eee =
                            new EarlyExitException(19, input);
                        throw eee;
                }
                cnt19++;
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
            // 252:3: -> ^( SOURCE singleTable ( joinedTable )+ )
            {
                // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:252:6: ^( SOURCE singleTable ( joinedTable )+ )
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
    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:255:1: joinType : ( INNER -> INNER | LEFT ( OUTER )? -> LEFT );
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
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:256:2: ( INNER -> INNER | LEFT ( OUTER )? -> LEFT )
            int alt21=2;
            int LA21_0 = input.LA(1);

            if ( (LA21_0==INNER) ) {
                alt21=1;
            }
            else if ( (LA21_0==LEFT) ) {
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
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:256:4: INNER
                    {
                    INNER51=(Token)match(input,INNER,FOLLOW_INNER_in_joinType810); if (state.failed) return retval; 
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
                    // 257:3: -> INNER
                    {
                        adaptor.addChild(root_0, stream_INNER.nextNode());

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:258:5: LEFT ( OUTER )?
                    {
                    LEFT52=(Token)match(input,LEFT,FOLLOW_LEFT_in_joinType822); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_LEFT.add(LEFT52);

                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:258:10: ( OUTER )?
                    int alt20=2;
                    int LA20_0 = input.LA(1);

                    if ( (LA20_0==OUTER) ) {
                        alt20=1;
                    }
                    switch (alt20) {
                        case 1 :
                            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:258:10: OUTER
                            {
                            OUTER53=(Token)match(input,OUTER,FOLLOW_OUTER_in_joinType824); if (state.failed) return retval; 
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
                    // 259:3: -> LEFT
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
    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:262:1: joinSpecification : ON lhs= columnReference EQUALS rhs= columnReference -> ^( ON $lhs EQUALS $rhs) ;
    public final CMISParser.joinSpecification_return joinSpecification() throws RecognitionException {
        CMISParser.joinSpecification_return retval = new CMISParser.joinSpecification_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token ON54=null;
        Token EQUALS55=null;
        CMISParser.columnReference_return lhs = null;

        CMISParser.columnReference_return rhs = null;


        Object ON54_tree=null;
        Object EQUALS55_tree=null;
        RewriteRuleTokenStream stream_ON=new RewriteRuleTokenStream(adaptor,"token ON");
        RewriteRuleTokenStream stream_EQUALS=new RewriteRuleTokenStream(adaptor,"token EQUALS");
        RewriteRuleSubtreeStream stream_columnReference=new RewriteRuleSubtreeStream(adaptor,"rule columnReference");
        try {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:263:2: ( ON lhs= columnReference EQUALS rhs= columnReference -> ^( ON $lhs EQUALS $rhs) )
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:263:4: ON lhs= columnReference EQUALS rhs= columnReference
            {
            ON54=(Token)match(input,ON,FOLLOW_ON_in_joinSpecification844); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_ON.add(ON54);

            pushFollow(FOLLOW_columnReference_in_joinSpecification848);
            lhs=columnReference();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_columnReference.add(lhs.getTree());
            EQUALS55=(Token)match(input,EQUALS,FOLLOW_EQUALS_in_joinSpecification850); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_EQUALS.add(EQUALS55);

            pushFollow(FOLLOW_columnReference_in_joinSpecification854);
            rhs=columnReference();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_columnReference.add(rhs.getTree());


            // AST REWRITE
            // elements: lhs, ON, rhs, EQUALS
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
            // 264:3: -> ^( ON $lhs EQUALS $rhs)
            {
                // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:264:6: ^( ON $lhs EQUALS $rhs)
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
    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:271:1: whereClause : WHERE searchOrCondition -> searchOrCondition ;
    public final CMISParser.whereClause_return whereClause() throws RecognitionException {
        CMISParser.whereClause_return retval = new CMISParser.whereClause_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token WHERE56=null;
        CMISParser.searchOrCondition_return searchOrCondition57 = null;


        Object WHERE56_tree=null;
        RewriteRuleTokenStream stream_WHERE=new RewriteRuleTokenStream(adaptor,"token WHERE");
        RewriteRuleSubtreeStream stream_searchOrCondition=new RewriteRuleSubtreeStream(adaptor,"rule searchOrCondition");
            paraphrases.push("in where"); 
        try {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:274:2: ( WHERE searchOrCondition -> searchOrCondition )
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:274:4: WHERE searchOrCondition
            {
            WHERE56=(Token)match(input,WHERE,FOLLOW_WHERE_in_whereClause905); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_WHERE.add(WHERE56);

            pushFollow(FOLLOW_searchOrCondition_in_whereClause907);
            searchOrCondition57=searchOrCondition();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_searchOrCondition.add(searchOrCondition57.getTree());


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
            // 275:3: -> searchOrCondition
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
    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:278:1: searchOrCondition : searchAndCondition ( OR searchAndCondition )* -> ^( DISJUNCTION ( searchAndCondition )+ ) ;
    public final CMISParser.searchOrCondition_return searchOrCondition() throws RecognitionException {
        CMISParser.searchOrCondition_return retval = new CMISParser.searchOrCondition_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token OR59=null;
        CMISParser.searchAndCondition_return searchAndCondition58 = null;

        CMISParser.searchAndCondition_return searchAndCondition60 = null;


        Object OR59_tree=null;
        RewriteRuleTokenStream stream_OR=new RewriteRuleTokenStream(adaptor,"token OR");
        RewriteRuleSubtreeStream stream_searchAndCondition=new RewriteRuleSubtreeStream(adaptor,"rule searchAndCondition");
        try {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:282:2: ( searchAndCondition ( OR searchAndCondition )* -> ^( DISJUNCTION ( searchAndCondition )+ ) )
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:282:4: searchAndCondition ( OR searchAndCondition )*
            {
            pushFollow(FOLLOW_searchAndCondition_in_searchOrCondition927);
            searchAndCondition58=searchAndCondition();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_searchAndCondition.add(searchAndCondition58.getTree());
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:282:23: ( OR searchAndCondition )*
            loop22:
            do {
                int alt22=2;
                int LA22_0 = input.LA(1);

                if ( (LA22_0==OR) ) {
                    alt22=1;
                }


                switch (alt22) {
            	case 1 :
            	    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:282:24: OR searchAndCondition
            	    {
            	    OR59=(Token)match(input,OR,FOLLOW_OR_in_searchOrCondition930); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_OR.add(OR59);

            	    pushFollow(FOLLOW_searchAndCondition_in_searchOrCondition932);
            	    searchAndCondition60=searchAndCondition();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_searchAndCondition.add(searchAndCondition60.getTree());

            	    }
            	    break;

            	default :
            	    break loop22;
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
            // 283:3: -> ^( DISJUNCTION ( searchAndCondition )+ )
            {
                // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:283:6: ^( DISJUNCTION ( searchAndCondition )+ )
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
    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:287:1: searchAndCondition : searchNotCondition ( AND searchNotCondition )* -> ^( CONJUNCTION ( searchNotCondition )+ ) ;
    public final CMISParser.searchAndCondition_return searchAndCondition() throws RecognitionException {
        CMISParser.searchAndCondition_return retval = new CMISParser.searchAndCondition_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token AND62=null;
        CMISParser.searchNotCondition_return searchNotCondition61 = null;

        CMISParser.searchNotCondition_return searchNotCondition63 = null;


        Object AND62_tree=null;
        RewriteRuleTokenStream stream_AND=new RewriteRuleTokenStream(adaptor,"token AND");
        RewriteRuleSubtreeStream stream_searchNotCondition=new RewriteRuleSubtreeStream(adaptor,"rule searchNotCondition");
        try {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:291:2: ( searchNotCondition ( AND searchNotCondition )* -> ^( CONJUNCTION ( searchNotCondition )+ ) )
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:291:4: searchNotCondition ( AND searchNotCondition )*
            {
            pushFollow(FOLLOW_searchNotCondition_in_searchAndCondition960);
            searchNotCondition61=searchNotCondition();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_searchNotCondition.add(searchNotCondition61.getTree());
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:291:23: ( AND searchNotCondition )*
            loop23:
            do {
                int alt23=2;
                int LA23_0 = input.LA(1);

                if ( (LA23_0==AND) ) {
                    alt23=1;
                }


                switch (alt23) {
            	case 1 :
            	    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:291:24: AND searchNotCondition
            	    {
            	    AND62=(Token)match(input,AND,FOLLOW_AND_in_searchAndCondition963); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_AND.add(AND62);

            	    pushFollow(FOLLOW_searchNotCondition_in_searchAndCondition965);
            	    searchNotCondition63=searchNotCondition();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_searchNotCondition.add(searchNotCondition63.getTree());

            	    }
            	    break;

            	default :
            	    break loop23;
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
            // 292:3: -> ^( CONJUNCTION ( searchNotCondition )+ )
            {
                // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:292:6: ^( CONJUNCTION ( searchNotCondition )+ )
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
    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:295:1: searchNotCondition : ( NOT searchTest -> ^( NEGATION searchTest ) | searchTest -> searchTest );
    public final CMISParser.searchNotCondition_return searchNotCondition() throws RecognitionException {
        CMISParser.searchNotCondition_return retval = new CMISParser.searchNotCondition_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token NOT64=null;
        CMISParser.searchTest_return searchTest65 = null;

        CMISParser.searchTest_return searchTest66 = null;


        Object NOT64_tree=null;
        RewriteRuleTokenStream stream_NOT=new RewriteRuleTokenStream(adaptor,"token NOT");
        RewriteRuleSubtreeStream stream_searchTest=new RewriteRuleSubtreeStream(adaptor,"rule searchTest");
        try {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:296:2: ( NOT searchTest -> ^( NEGATION searchTest ) | searchTest -> searchTest )
            int alt24=2;
            alt24 = dfa24.predict(input);
            switch (alt24) {
                case 1 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:296:4: NOT searchTest
                    {
                    NOT64=(Token)match(input,NOT,FOLLOW_NOT_in_searchNotCondition992); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_NOT.add(NOT64);

                    pushFollow(FOLLOW_searchTest_in_searchNotCondition994);
                    searchTest65=searchTest();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_searchTest.add(searchTest65.getTree());


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
                    // 297:3: -> ^( NEGATION searchTest )
                    {
                        // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:297:6: ^( NEGATION searchTest )
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
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:298:4: searchTest
                    {
                    pushFollow(FOLLOW_searchTest_in_searchNotCondition1009);
                    searchTest66=searchTest();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_searchTest.add(searchTest66.getTree());


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
                    // 299:3: -> searchTest
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
    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:302:1: searchTest : ( predicate -> predicate | LPAREN searchOrCondition RPAREN -> searchOrCondition );
    public final CMISParser.searchTest_return searchTest() throws RecognitionException {
        CMISParser.searchTest_return retval = new CMISParser.searchTest_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token LPAREN68=null;
        Token RPAREN70=null;
        CMISParser.predicate_return predicate67 = null;

        CMISParser.searchOrCondition_return searchOrCondition69 = null;


        Object LPAREN68_tree=null;
        Object RPAREN70_tree=null;
        RewriteRuleTokenStream stream_RPAREN=new RewriteRuleTokenStream(adaptor,"token RPAREN");
        RewriteRuleTokenStream stream_LPAREN=new RewriteRuleTokenStream(adaptor,"token LPAREN");
        RewriteRuleSubtreeStream stream_predicate=new RewriteRuleSubtreeStream(adaptor,"rule predicate");
        RewriteRuleSubtreeStream stream_searchOrCondition=new RewriteRuleSubtreeStream(adaptor,"rule searchOrCondition");
        try {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:303:2: ( predicate -> predicate | LPAREN searchOrCondition RPAREN -> searchOrCondition )
            int alt25=2;
            int LA25_0 = input.LA(1);

            if ( (LA25_0==ID) ) {
                alt25=1;
            }
            else if ( (LA25_0==DOUBLE_QUOTE) && ((strict == false))) {
                alt25=1;
            }
            else if ( (LA25_0==ANY||LA25_0==QUOTED_STRING||(LA25_0>=FLOATING_POINT_LITERAL && LA25_0<=TIMESTAMP)) ) {
                alt25=1;
            }
            else if ( (LA25_0==COLON) && ((strict == false))) {
                alt25=1;
            }
            else if ( (LA25_0==SELECT||LA25_0==AS||(LA25_0>=FROM && LA25_0<=ON)||(LA25_0>=WHERE && LA25_0<=NOT)||(LA25_0>=IN && LA25_0<=NULL)||(LA25_0>=CONTAINS && LA25_0<=DESC)) ) {
                alt25=1;
            }
            else if ( (LA25_0==LPAREN) ) {
                alt25=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 25, 0, input);

                throw nvae;
            }
            switch (alt25) {
                case 1 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:303:4: predicate
                    {
                    pushFollow(FOLLOW_predicate_in_searchTest1027);
                    predicate67=predicate();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_predicate.add(predicate67.getTree());


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
                    // 304:3: -> predicate
                    {
                        adaptor.addChild(root_0, stream_predicate.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:305:4: LPAREN searchOrCondition RPAREN
                    {
                    LPAREN68=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_searchTest1038); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_LPAREN.add(LPAREN68);

                    pushFollow(FOLLOW_searchOrCondition_in_searchTest1040);
                    searchOrCondition69=searchOrCondition();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_searchOrCondition.add(searchOrCondition69.getTree());
                    RPAREN70=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_searchTest1042); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_RPAREN.add(RPAREN70);



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
                    // 306:3: -> searchOrCondition
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
    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:309:1: predicate : ( comparisonPredicate | inPredicate | likePredicate | nullPredicate | quantifiedComparisonPredicate | quantifiedInPredicate | textSearchPredicate | folderPredicate );
    public final CMISParser.predicate_return predicate() throws RecognitionException {
        CMISParser.predicate_return retval = new CMISParser.predicate_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        CMISParser.comparisonPredicate_return comparisonPredicate71 = null;

        CMISParser.inPredicate_return inPredicate72 = null;

        CMISParser.likePredicate_return likePredicate73 = null;

        CMISParser.nullPredicate_return nullPredicate74 = null;

        CMISParser.quantifiedComparisonPredicate_return quantifiedComparisonPredicate75 = null;

        CMISParser.quantifiedInPredicate_return quantifiedInPredicate76 = null;

        CMISParser.textSearchPredicate_return textSearchPredicate77 = null;

        CMISParser.folderPredicate_return folderPredicate78 = null;



        try {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:310:2: ( comparisonPredicate | inPredicate | likePredicate | nullPredicate | quantifiedComparisonPredicate | quantifiedInPredicate | textSearchPredicate | folderPredicate )
            int alt26=8;
            alt26 = dfa26.predict(input);
            switch (alt26) {
                case 1 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:310:4: comparisonPredicate
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_comparisonPredicate_in_predicate1059);
                    comparisonPredicate71=comparisonPredicate();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, comparisonPredicate71.getTree());

                    }
                    break;
                case 2 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:311:4: inPredicate
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_inPredicate_in_predicate1064);
                    inPredicate72=inPredicate();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, inPredicate72.getTree());

                    }
                    break;
                case 3 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:312:4: likePredicate
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_likePredicate_in_predicate1069);
                    likePredicate73=likePredicate();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, likePredicate73.getTree());

                    }
                    break;
                case 4 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:313:4: nullPredicate
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_nullPredicate_in_predicate1074);
                    nullPredicate74=nullPredicate();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, nullPredicate74.getTree());

                    }
                    break;
                case 5 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:314:10: quantifiedComparisonPredicate
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_quantifiedComparisonPredicate_in_predicate1085);
                    quantifiedComparisonPredicate75=quantifiedComparisonPredicate();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, quantifiedComparisonPredicate75.getTree());

                    }
                    break;
                case 6 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:315:4: quantifiedInPredicate
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_quantifiedInPredicate_in_predicate1090);
                    quantifiedInPredicate76=quantifiedInPredicate();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, quantifiedInPredicate76.getTree());

                    }
                    break;
                case 7 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:316:4: textSearchPredicate
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_textSearchPredicate_in_predicate1095);
                    textSearchPredicate77=textSearchPredicate();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, textSearchPredicate77.getTree());

                    }
                    break;
                case 8 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:317:4: folderPredicate
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_folderPredicate_in_predicate1100);
                    folderPredicate78=folderPredicate();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, folderPredicate78.getTree());

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
    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:320:1: comparisonPredicate : valueExpression compOp literalOrParameterName -> ^( PRED_COMPARISON ANY valueExpression compOp literalOrParameterName ) ;
    public final CMISParser.comparisonPredicate_return comparisonPredicate() throws RecognitionException {
        CMISParser.comparisonPredicate_return retval = new CMISParser.comparisonPredicate_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        CMISParser.valueExpression_return valueExpression79 = null;

        CMISParser.compOp_return compOp80 = null;

        CMISParser.literalOrParameterName_return literalOrParameterName81 = null;


        RewriteRuleSubtreeStream stream_valueExpression=new RewriteRuleSubtreeStream(adaptor,"rule valueExpression");
        RewriteRuleSubtreeStream stream_compOp=new RewriteRuleSubtreeStream(adaptor,"rule compOp");
        RewriteRuleSubtreeStream stream_literalOrParameterName=new RewriteRuleSubtreeStream(adaptor,"rule literalOrParameterName");
        try {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:321:2: ( valueExpression compOp literalOrParameterName -> ^( PRED_COMPARISON ANY valueExpression compOp literalOrParameterName ) )
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:321:4: valueExpression compOp literalOrParameterName
            {
            pushFollow(FOLLOW_valueExpression_in_comparisonPredicate1112);
            valueExpression79=valueExpression();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_valueExpression.add(valueExpression79.getTree());
            pushFollow(FOLLOW_compOp_in_comparisonPredicate1114);
            compOp80=compOp();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_compOp.add(compOp80.getTree());
            pushFollow(FOLLOW_literalOrParameterName_in_comparisonPredicate1116);
            literalOrParameterName81=literalOrParameterName();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_literalOrParameterName.add(literalOrParameterName81.getTree());


            // AST REWRITE
            // elements: valueExpression, literalOrParameterName, compOp
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 322:3: -> ^( PRED_COMPARISON ANY valueExpression compOp literalOrParameterName )
            {
                // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:322:6: ^( PRED_COMPARISON ANY valueExpression compOp literalOrParameterName )
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
    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:325:1: compOp : ( EQUALS | NOTEQUALS | LESSTHAN | GREATERTHAN | LESSTHANOREQUALS | GREATERTHANOREQUALS );
    public final CMISParser.compOp_return compOp() throws RecognitionException {
        CMISParser.compOp_return retval = new CMISParser.compOp_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token set82=null;

        Object set82_tree=null;

        try {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:326:2: ( EQUALS | NOTEQUALS | LESSTHAN | GREATERTHAN | LESSTHANOREQUALS | GREATERTHANOREQUALS )
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:
            {
            root_0 = (Object)adaptor.nil();

            set82=(Token)input.LT(1);
            if ( input.LA(1)==EQUALS||(input.LA(1)>=NOTEQUALS && input.LA(1)<=GREATERTHANOREQUALS) ) {
                input.consume();
                if ( state.backtracking==0 ) adaptor.addChild(root_0, (Object)adaptor.create(set82));
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
    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:334:1: literalOrParameterName : ( literal | {...}? => parameterName );
    public final CMISParser.literalOrParameterName_return literalOrParameterName() throws RecognitionException {
        CMISParser.literalOrParameterName_return retval = new CMISParser.literalOrParameterName_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        CMISParser.literal_return literal83 = null;

        CMISParser.parameterName_return parameterName84 = null;



        try {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:335:2: ( literal | {...}? => parameterName )
            int alt27=2;
            int LA27_0 = input.LA(1);

            if ( (LA27_0==QUOTED_STRING||(LA27_0>=FLOATING_POINT_LITERAL && LA27_0<=TIMESTAMP)) ) {
                alt27=1;
            }
            else if ( (LA27_0==COLON) && ((strict == false))) {
                alt27=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 27, 0, input);

                throw nvae;
            }
            switch (alt27) {
                case 1 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:335:4: literal
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_literal_in_literalOrParameterName1182);
                    literal83=literal();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, literal83.getTree());

                    }
                    break;
                case 2 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:336:4: {...}? => parameterName
                    {
                    root_0 = (Object)adaptor.nil();

                    if ( !((strict == false)) ) {
                        if (state.backtracking>0) {state.failed=true; return retval;}
                        throw new FailedPredicateException(input, "literalOrParameterName", "strict == false");
                    }
                    pushFollow(FOLLOW_parameterName_in_literalOrParameterName1190);
                    parameterName84=parameterName();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, parameterName84.getTree());

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
    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:339:1: literal : ( signedNumericLiteral | characterStringLiteral | booleanLiteral | datetimeLiteral );
    public final CMISParser.literal_return literal() throws RecognitionException {
        CMISParser.literal_return retval = new CMISParser.literal_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        CMISParser.signedNumericLiteral_return signedNumericLiteral85 = null;

        CMISParser.characterStringLiteral_return characterStringLiteral86 = null;

        CMISParser.booleanLiteral_return booleanLiteral87 = null;

        CMISParser.datetimeLiteral_return datetimeLiteral88 = null;



        try {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:340:2: ( signedNumericLiteral | characterStringLiteral | booleanLiteral | datetimeLiteral )
            int alt28=4;
            switch ( input.LA(1) ) {
            case FLOATING_POINT_LITERAL:
            case DECIMAL_INTEGER_LITERAL:
                {
                alt28=1;
                }
                break;
            case QUOTED_STRING:
                {
                alt28=2;
                }
                break;
            case TRUE:
            case FALSE:
                {
                alt28=3;
                }
                break;
            case TIMESTAMP:
                {
                alt28=4;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 28, 0, input);

                throw nvae;
            }

            switch (alt28) {
                case 1 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:340:4: signedNumericLiteral
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_signedNumericLiteral_in_literal1203);
                    signedNumericLiteral85=signedNumericLiteral();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, signedNumericLiteral85.getTree());

                    }
                    break;
                case 2 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:341:4: characterStringLiteral
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_characterStringLiteral_in_literal1208);
                    characterStringLiteral86=characterStringLiteral();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, characterStringLiteral86.getTree());

                    }
                    break;
                case 3 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:342:4: booleanLiteral
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_booleanLiteral_in_literal1213);
                    booleanLiteral87=booleanLiteral();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, booleanLiteral87.getTree());

                    }
                    break;
                case 4 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:343:4: datetimeLiteral
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_datetimeLiteral_in_literal1218);
                    datetimeLiteral88=datetimeLiteral();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, datetimeLiteral88.getTree());

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
    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:346:1: inPredicate : columnReference ( NOT )? IN LPAREN inValueList RPAREN -> ^( PRED_IN ANY columnReference inValueList ( NOT )? ) ;
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
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:347:2: ( columnReference ( NOT )? IN LPAREN inValueList RPAREN -> ^( PRED_IN ANY columnReference inValueList ( NOT )? ) )
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:347:4: columnReference ( NOT )? IN LPAREN inValueList RPAREN
            {
            pushFollow(FOLLOW_columnReference_in_inPredicate1230);
            columnReference89=columnReference();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_columnReference.add(columnReference89.getTree());
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:347:20: ( NOT )?
            int alt29=2;
            int LA29_0 = input.LA(1);

            if ( (LA29_0==NOT) ) {
                alt29=1;
            }
            switch (alt29) {
                case 1 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:347:20: NOT
                    {
                    NOT90=(Token)match(input,NOT,FOLLOW_NOT_in_inPredicate1232); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_NOT.add(NOT90);


                    }
                    break;

            }

            IN91=(Token)match(input,IN,FOLLOW_IN_in_inPredicate1235); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_IN.add(IN91);

            LPAREN92=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_inPredicate1237); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_LPAREN.add(LPAREN92);

            pushFollow(FOLLOW_inValueList_in_inPredicate1239);
            inValueList93=inValueList();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_inValueList.add(inValueList93.getTree());
            RPAREN94=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_inPredicate1241); if (state.failed) return retval; 
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
            // 348:3: -> ^( PRED_IN ANY columnReference inValueList ( NOT )? )
            {
                // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:348:6: ^( PRED_IN ANY columnReference inValueList ( NOT )? )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(PRED_IN, "PRED_IN"), root_1);

                adaptor.addChild(root_1, (Object)adaptor.create(ANY, "ANY"));
                adaptor.addChild(root_1, stream_columnReference.nextTree());
                adaptor.addChild(root_1, stream_inValueList.nextTree());
                // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:348:48: ( NOT )?
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
    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:351:1: inValueList : literalOrParameterName ( COMMA literalOrParameterName )* -> ^( LIST ( literalOrParameterName )+ ) ;
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
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:352:2: ( literalOrParameterName ( COMMA literalOrParameterName )* -> ^( LIST ( literalOrParameterName )+ ) )
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:352:4: literalOrParameterName ( COMMA literalOrParameterName )*
            {
            pushFollow(FOLLOW_literalOrParameterName_in_inValueList1270);
            literalOrParameterName95=literalOrParameterName();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_literalOrParameterName.add(literalOrParameterName95.getTree());
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:352:27: ( COMMA literalOrParameterName )*
            loop30:
            do {
                int alt30=2;
                int LA30_0 = input.LA(1);

                if ( (LA30_0==COMMA) ) {
                    alt30=1;
                }


                switch (alt30) {
            	case 1 :
            	    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:352:28: COMMA literalOrParameterName
            	    {
            	    COMMA96=(Token)match(input,COMMA,FOLLOW_COMMA_in_inValueList1273); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_COMMA.add(COMMA96);

            	    pushFollow(FOLLOW_literalOrParameterName_in_inValueList1275);
            	    literalOrParameterName97=literalOrParameterName();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_literalOrParameterName.add(literalOrParameterName97.getTree());

            	    }
            	    break;

            	default :
            	    break loop30;
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
            // 353:3: -> ^( LIST ( literalOrParameterName )+ )
            {
                // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:353:6: ^( LIST ( literalOrParameterName )+ )
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
    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:356:1: likePredicate : columnReference ( NOT )? LIKE characterStringLiteral -> ^( PRED_LIKE columnReference characterStringLiteral ( NOT )? ) ;
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
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:357:2: ( columnReference ( NOT )? LIKE characterStringLiteral -> ^( PRED_LIKE columnReference characterStringLiteral ( NOT )? ) )
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:357:4: columnReference ( NOT )? LIKE characterStringLiteral
            {
            pushFollow(FOLLOW_columnReference_in_likePredicate1301);
            columnReference98=columnReference();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_columnReference.add(columnReference98.getTree());
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:357:20: ( NOT )?
            int alt31=2;
            int LA31_0 = input.LA(1);

            if ( (LA31_0==NOT) ) {
                alt31=1;
            }
            switch (alt31) {
                case 1 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:357:20: NOT
                    {
                    NOT99=(Token)match(input,NOT,FOLLOW_NOT_in_likePredicate1303); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_NOT.add(NOT99);


                    }
                    break;

            }

            LIKE100=(Token)match(input,LIKE,FOLLOW_LIKE_in_likePredicate1306); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_LIKE.add(LIKE100);

            pushFollow(FOLLOW_characterStringLiteral_in_likePredicate1308);
            characterStringLiteral101=characterStringLiteral();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_characterStringLiteral.add(characterStringLiteral101.getTree());


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
            // 358:3: -> ^( PRED_LIKE columnReference characterStringLiteral ( NOT )? )
            {
                // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:358:6: ^( PRED_LIKE columnReference characterStringLiteral ( NOT )? )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(PRED_LIKE, "PRED_LIKE"), root_1);

                adaptor.addChild(root_1, stream_columnReference.nextTree());
                adaptor.addChild(root_1, stream_characterStringLiteral.nextTree());
                // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:358:57: ( NOT )?
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
    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:361:1: nullPredicate : ( ( ( columnReference )=> columnReference | multiValuedColumnReference ) IS NULL -> ^( PRED_EXISTS columnReference NOT ) | ( ( columnReference )=> columnReference | multiValuedColumnReference ) IS NOT NULL -> ^( PRED_EXISTS columnReference ) );
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
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:362:2: ( ( ( columnReference )=> columnReference | multiValuedColumnReference ) IS NULL -> ^( PRED_EXISTS columnReference NOT ) | ( ( columnReference )=> columnReference | multiValuedColumnReference ) IS NOT NULL -> ^( PRED_EXISTS columnReference ) )
            int alt34=2;
            alt34 = dfa34.predict(input);
            switch (alt34) {
                case 1 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:362:4: ( ( columnReference )=> columnReference | multiValuedColumnReference ) IS NULL
                    {
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:362:4: ( ( columnReference )=> columnReference | multiValuedColumnReference )
                    int alt32=2;
                    int LA32_0 = input.LA(1);

                    if ( (LA32_0==ID) ) {
                        int LA32_1 = input.LA(2);

                        if ( (synpred5_CMIS()) ) {
                            alt32=1;
                        }
                        else if ( (true) ) {
                            alt32=2;
                        }
                        else {
                            if (state.backtracking>0) {state.failed=true; return retval;}
                            NoViableAltException nvae =
                                new NoViableAltException("", 32, 1, input);

                            throw nvae;
                        }
                    }
                    else if ( (LA32_0==DOUBLE_QUOTE) && ((strict == false))) {
                        int LA32_2 = input.LA(2);

                        if ( (LA32_2==SELECT||LA32_2==AS||(LA32_2>=FROM && LA32_2<=ON)||(LA32_2>=WHERE && LA32_2<=NOT)||(LA32_2>=IN && LA32_2<=DESC)||(LA32_2>=TRUE && LA32_2<=TIMESTAMP)) && ((strict == false))) {
                            int LA32_5 = input.LA(3);

                            if ( (LA32_5==DOUBLE_QUOTE) && ((strict == false))) {
                                int LA32_7 = input.LA(4);

                                if ( (((synpred5_CMIS()&&(strict == false))||(synpred5_CMIS()&&(strict == false))||(synpred5_CMIS()&&(strict == false)))) ) {
                                    alt32=1;
                                }
                                else if ( ((strict == false)) ) {
                                    alt32=2;
                                }
                                else {
                                    if (state.backtracking>0) {state.failed=true; return retval;}
                                    NoViableAltException nvae =
                                        new NoViableAltException("", 32, 7, input);

                                    throw nvae;
                                }
                            }
                            else {
                                if (state.backtracking>0) {state.failed=true; return retval;}
                                NoViableAltException nvae =
                                    new NoViableAltException("", 32, 5, input);

                                throw nvae;
                            }
                        }
                        else if ( (LA32_2==ID) && ((strict == false))) {
                            int LA32_6 = input.LA(3);

                            if ( (LA32_6==DOUBLE_QUOTE) && ((strict == false))) {
                                int LA32_8 = input.LA(4);

                                if ( (((synpred5_CMIS()&&(strict == false))||(synpred5_CMIS()&&(strict == false))||(synpred5_CMIS()&&(strict == false)))) ) {
                                    alt32=1;
                                }
                                else if ( ((strict == false)) ) {
                                    alt32=2;
                                }
                                else {
                                    if (state.backtracking>0) {state.failed=true; return retval;}
                                    NoViableAltException nvae =
                                        new NoViableAltException("", 32, 8, input);

                                    throw nvae;
                                }
                            }
                            else {
                                if (state.backtracking>0) {state.failed=true; return retval;}
                                NoViableAltException nvae =
                                    new NoViableAltException("", 32, 6, input);

                                throw nvae;
                            }
                        }
                        else {
                            if (state.backtracking>0) {state.failed=true; return retval;}
                            NoViableAltException nvae =
                                new NoViableAltException("", 32, 2, input);

                            throw nvae;
                        }
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return retval;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 32, 0, input);

                        throw nvae;
                    }
                    switch (alt32) {
                        case 1 :
                            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:362:6: ( columnReference )=> columnReference
                            {
                            pushFollow(FOLLOW_columnReference_in_nullPredicate1342);
                            columnReference102=columnReference();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_columnReference.add(columnReference102.getTree());

                            }
                            break;
                        case 2 :
                            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:362:44: multiValuedColumnReference
                            {
                            pushFollow(FOLLOW_multiValuedColumnReference_in_nullPredicate1346);
                            multiValuedColumnReference103=multiValuedColumnReference();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_multiValuedColumnReference.add(multiValuedColumnReference103.getTree());

                            }
                            break;

                    }

                    IS104=(Token)match(input,IS,FOLLOW_IS_in_nullPredicate1349); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_IS.add(IS104);

                    NULL105=(Token)match(input,NULL,FOLLOW_NULL_in_nullPredicate1351); if (state.failed) return retval; 
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
                    // 363:3: -> ^( PRED_EXISTS columnReference NOT )
                    {
                        // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:363:6: ^( PRED_EXISTS columnReference NOT )
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
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:364:9: ( ( columnReference )=> columnReference | multiValuedColumnReference ) IS NOT NULL
                    {
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:364:9: ( ( columnReference )=> columnReference | multiValuedColumnReference )
                    int alt33=2;
                    int LA33_0 = input.LA(1);

                    if ( (LA33_0==ID) ) {
                        int LA33_1 = input.LA(2);

                        if ( (synpred6_CMIS()) ) {
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
                    else if ( (LA33_0==DOUBLE_QUOTE) && ((strict == false))) {
                        int LA33_2 = input.LA(2);

                        if ( (LA33_2==SELECT||LA33_2==AS||(LA33_2>=FROM && LA33_2<=ON)||(LA33_2>=WHERE && LA33_2<=NOT)||(LA33_2>=IN && LA33_2<=DESC)||(LA33_2>=TRUE && LA33_2<=TIMESTAMP)) && ((strict == false))) {
                            int LA33_5 = input.LA(3);

                            if ( (LA33_5==DOUBLE_QUOTE) && ((strict == false))) {
                                int LA33_7 = input.LA(4);

                                if ( (((synpred6_CMIS()&&(strict == false))||(synpred6_CMIS()&&(strict == false))||(synpred6_CMIS()&&(strict == false)))) ) {
                                    alt33=1;
                                }
                                else if ( ((strict == false)) ) {
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
                        else if ( (LA33_2==ID) && ((strict == false))) {
                            int LA33_6 = input.LA(3);

                            if ( (LA33_6==DOUBLE_QUOTE) && ((strict == false))) {
                                int LA33_8 = input.LA(4);

                                if ( (((synpred6_CMIS()&&(strict == false))||(synpred6_CMIS()&&(strict == false))||(synpred6_CMIS()&&(strict == false)))) ) {
                                    alt33=1;
                                }
                                else if ( ((strict == false)) ) {
                                    alt33=2;
                                }
                                else {
                                    if (state.backtracking>0) {state.failed=true; return retval;}
                                    NoViableAltException nvae =
                                        new NoViableAltException("", 33, 8, input);

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
                            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:364:11: ( columnReference )=> columnReference
                            {
                            pushFollow(FOLLOW_columnReference_in_nullPredicate1380);
                            columnReference106=columnReference();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_columnReference.add(columnReference106.getTree());

                            }
                            break;
                        case 2 :
                            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:364:49: multiValuedColumnReference
                            {
                            pushFollow(FOLLOW_multiValuedColumnReference_in_nullPredicate1384);
                            multiValuedColumnReference107=multiValuedColumnReference();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_multiValuedColumnReference.add(multiValuedColumnReference107.getTree());

                            }
                            break;

                    }

                    IS108=(Token)match(input,IS,FOLLOW_IS_in_nullPredicate1387); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_IS.add(IS108);

                    NOT109=(Token)match(input,NOT,FOLLOW_NOT_in_nullPredicate1389); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_NOT.add(NOT109);

                    NULL110=(Token)match(input,NULL,FOLLOW_NULL_in_nullPredicate1391); if (state.failed) return retval; 
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
                    // 365:9: -> ^( PRED_EXISTS columnReference )
                    {
                        // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:365:12: ^( PRED_EXISTS columnReference )
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
    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:368:1: quantifiedComparisonPredicate : literalOrParameterName compOp ANY multiValuedColumnReference -> ^( PRED_COMPARISON ANY literalOrParameterName compOp multiValuedColumnReference ) ;
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
        RewriteRuleSubtreeStream stream_compOp=new RewriteRuleSubtreeStream(adaptor,"rule compOp");
        RewriteRuleSubtreeStream stream_literalOrParameterName=new RewriteRuleSubtreeStream(adaptor,"rule literalOrParameterName");
        RewriteRuleSubtreeStream stream_multiValuedColumnReference=new RewriteRuleSubtreeStream(adaptor,"rule multiValuedColumnReference");
        try {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:369:2: ( literalOrParameterName compOp ANY multiValuedColumnReference -> ^( PRED_COMPARISON ANY literalOrParameterName compOp multiValuedColumnReference ) )
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:369:4: literalOrParameterName compOp ANY multiValuedColumnReference
            {
            pushFollow(FOLLOW_literalOrParameterName_in_quantifiedComparisonPredicate1419);
            literalOrParameterName111=literalOrParameterName();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_literalOrParameterName.add(literalOrParameterName111.getTree());
            pushFollow(FOLLOW_compOp_in_quantifiedComparisonPredicate1421);
            compOp112=compOp();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_compOp.add(compOp112.getTree());
            ANY113=(Token)match(input,ANY,FOLLOW_ANY_in_quantifiedComparisonPredicate1423); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_ANY.add(ANY113);

            pushFollow(FOLLOW_multiValuedColumnReference_in_quantifiedComparisonPredicate1425);
            multiValuedColumnReference114=multiValuedColumnReference();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_multiValuedColumnReference.add(multiValuedColumnReference114.getTree());


            // AST REWRITE
            // elements: compOp, ANY, multiValuedColumnReference, literalOrParameterName
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 370:2: -> ^( PRED_COMPARISON ANY literalOrParameterName compOp multiValuedColumnReference )
            {
                // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:370:5: ^( PRED_COMPARISON ANY literalOrParameterName compOp multiValuedColumnReference )
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
    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:374:1: quantifiedInPredicate : ANY multiValuedColumnReference ( NOT )? IN LPAREN inValueList RPAREN -> ^( PRED_IN ANY multiValuedColumnReference inValueList ( NOT )? ) ;
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
        RewriteRuleTokenStream stream_ANY=new RewriteRuleTokenStream(adaptor,"token ANY");
        RewriteRuleTokenStream stream_IN=new RewriteRuleTokenStream(adaptor,"token IN");
        RewriteRuleTokenStream stream_NOT=new RewriteRuleTokenStream(adaptor,"token NOT");
        RewriteRuleTokenStream stream_LPAREN=new RewriteRuleTokenStream(adaptor,"token LPAREN");
        RewriteRuleSubtreeStream stream_multiValuedColumnReference=new RewriteRuleSubtreeStream(adaptor,"rule multiValuedColumnReference");
        RewriteRuleSubtreeStream stream_inValueList=new RewriteRuleSubtreeStream(adaptor,"rule inValueList");
        try {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:375:2: ( ANY multiValuedColumnReference ( NOT )? IN LPAREN inValueList RPAREN -> ^( PRED_IN ANY multiValuedColumnReference inValueList ( NOT )? ) )
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:375:4: ANY multiValuedColumnReference ( NOT )? IN LPAREN inValueList RPAREN
            {
            ANY115=(Token)match(input,ANY,FOLLOW_ANY_in_quantifiedInPredicate1454); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_ANY.add(ANY115);

            pushFollow(FOLLOW_multiValuedColumnReference_in_quantifiedInPredicate1456);
            multiValuedColumnReference116=multiValuedColumnReference();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_multiValuedColumnReference.add(multiValuedColumnReference116.getTree());
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:375:35: ( NOT )?
            int alt35=2;
            int LA35_0 = input.LA(1);

            if ( (LA35_0==NOT) ) {
                alt35=1;
            }
            switch (alt35) {
                case 1 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:375:35: NOT
                    {
                    NOT117=(Token)match(input,NOT,FOLLOW_NOT_in_quantifiedInPredicate1458); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_NOT.add(NOT117);


                    }
                    break;

            }

            IN118=(Token)match(input,IN,FOLLOW_IN_in_quantifiedInPredicate1461); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_IN.add(IN118);

            LPAREN119=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_quantifiedInPredicate1464); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_LPAREN.add(LPAREN119);

            pushFollow(FOLLOW_inValueList_in_quantifiedInPredicate1466);
            inValueList120=inValueList();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_inValueList.add(inValueList120.getTree());
            RPAREN121=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_quantifiedInPredicate1468); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_RPAREN.add(RPAREN121);



            // AST REWRITE
            // elements: NOT, inValueList, multiValuedColumnReference, ANY
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 376:3: -> ^( PRED_IN ANY multiValuedColumnReference inValueList ( NOT )? )
            {
                // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:376:6: ^( PRED_IN ANY multiValuedColumnReference inValueList ( NOT )? )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(PRED_IN, "PRED_IN"), root_1);

                adaptor.addChild(root_1, stream_ANY.nextNode());
                adaptor.addChild(root_1, stream_multiValuedColumnReference.nextTree());
                adaptor.addChild(root_1, stream_inValueList.nextTree());
                // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:376:59: ( NOT )?
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
    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:379:1: textSearchPredicate : CONTAINS LPAREN ( qualifier COMMA )? textSearchExpression RPAREN -> ^( PRED_FTS textSearchExpression ( qualifier )? ) ;
    public final CMISParser.textSearchPredicate_return textSearchPredicate() throws RecognitionException {
        CMISParser.textSearchPredicate_return retval = new CMISParser.textSearchPredicate_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token CONTAINS122=null;
        Token LPAREN123=null;
        Token COMMA125=null;
        Token RPAREN127=null;
        CMISParser.qualifier_return qualifier124 = null;

        CMISParser.textSearchExpression_return textSearchExpression126 = null;


        Object CONTAINS122_tree=null;
        Object LPAREN123_tree=null;
        Object COMMA125_tree=null;
        Object RPAREN127_tree=null;
        RewriteRuleTokenStream stream_RPAREN=new RewriteRuleTokenStream(adaptor,"token RPAREN");
        RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
        RewriteRuleTokenStream stream_CONTAINS=new RewriteRuleTokenStream(adaptor,"token CONTAINS");
        RewriteRuleTokenStream stream_LPAREN=new RewriteRuleTokenStream(adaptor,"token LPAREN");
        RewriteRuleSubtreeStream stream_qualifier=new RewriteRuleSubtreeStream(adaptor,"rule qualifier");
        RewriteRuleSubtreeStream stream_textSearchExpression=new RewriteRuleSubtreeStream(adaptor,"rule textSearchExpression");
        try {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:380:2: ( CONTAINS LPAREN ( qualifier COMMA )? textSearchExpression RPAREN -> ^( PRED_FTS textSearchExpression ( qualifier )? ) )
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:380:4: CONTAINS LPAREN ( qualifier COMMA )? textSearchExpression RPAREN
            {
            CONTAINS122=(Token)match(input,CONTAINS,FOLLOW_CONTAINS_in_textSearchPredicate1497); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_CONTAINS.add(CONTAINS122);

            LPAREN123=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_textSearchPredicate1499); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_LPAREN.add(LPAREN123);

            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:380:20: ( qualifier COMMA )?
            int alt36=2;
            int LA36_0 = input.LA(1);

            if ( (LA36_0==ID) ) {
                alt36=1;
            }
            else if ( (LA36_0==DOUBLE_QUOTE) && ((strict == false))) {
                alt36=1;
            }
            switch (alt36) {
                case 1 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:380:21: qualifier COMMA
                    {
                    pushFollow(FOLLOW_qualifier_in_textSearchPredicate1502);
                    qualifier124=qualifier();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_qualifier.add(qualifier124.getTree());
                    COMMA125=(Token)match(input,COMMA,FOLLOW_COMMA_in_textSearchPredicate1504); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_COMMA.add(COMMA125);


                    }
                    break;

            }

            pushFollow(FOLLOW_textSearchExpression_in_textSearchPredicate1508);
            textSearchExpression126=textSearchExpression();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_textSearchExpression.add(textSearchExpression126.getTree());
            RPAREN127=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_textSearchPredicate1510); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_RPAREN.add(RPAREN127);



            // AST REWRITE
            // elements: textSearchExpression, qualifier
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 381:3: -> ^( PRED_FTS textSearchExpression ( qualifier )? )
            {
                // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:381:6: ^( PRED_FTS textSearchExpression ( qualifier )? )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(PRED_FTS, "PRED_FTS"), root_1);

                adaptor.addChild(root_1, stream_textSearchExpression.nextTree());
                // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:381:38: ( qualifier )?
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
    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:384:1: folderPredicate : ( IN_FOLDER folderPredicateArgs -> ^( PRED_CHILD folderPredicateArgs ) | IN_TREE folderPredicateArgs -> ^( PRED_DESCENDANT folderPredicateArgs ) );
    public final CMISParser.folderPredicate_return folderPredicate() throws RecognitionException {
        CMISParser.folderPredicate_return retval = new CMISParser.folderPredicate_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token IN_FOLDER128=null;
        Token IN_TREE130=null;
        CMISParser.folderPredicateArgs_return folderPredicateArgs129 = null;

        CMISParser.folderPredicateArgs_return folderPredicateArgs131 = null;


        Object IN_FOLDER128_tree=null;
        Object IN_TREE130_tree=null;
        RewriteRuleTokenStream stream_IN_TREE=new RewriteRuleTokenStream(adaptor,"token IN_TREE");
        RewriteRuleTokenStream stream_IN_FOLDER=new RewriteRuleTokenStream(adaptor,"token IN_FOLDER");
        RewriteRuleSubtreeStream stream_folderPredicateArgs=new RewriteRuleSubtreeStream(adaptor,"rule folderPredicateArgs");
        try {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:385:2: ( IN_FOLDER folderPredicateArgs -> ^( PRED_CHILD folderPredicateArgs ) | IN_TREE folderPredicateArgs -> ^( PRED_DESCENDANT folderPredicateArgs ) )
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
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:385:4: IN_FOLDER folderPredicateArgs
                    {
                    IN_FOLDER128=(Token)match(input,IN_FOLDER,FOLLOW_IN_FOLDER_in_folderPredicate1535); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_IN_FOLDER.add(IN_FOLDER128);

                    pushFollow(FOLLOW_folderPredicateArgs_in_folderPredicate1538);
                    folderPredicateArgs129=folderPredicateArgs();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_folderPredicateArgs.add(folderPredicateArgs129.getTree());


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
                    // 386:3: -> ^( PRED_CHILD folderPredicateArgs )
                    {
                        // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:386:6: ^( PRED_CHILD folderPredicateArgs )
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
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:387:10: IN_TREE folderPredicateArgs
                    {
                    IN_TREE130=(Token)match(input,IN_TREE,FOLLOW_IN_TREE_in_folderPredicate1559); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_IN_TREE.add(IN_TREE130);

                    pushFollow(FOLLOW_folderPredicateArgs_in_folderPredicate1561);
                    folderPredicateArgs131=folderPredicateArgs();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_folderPredicateArgs.add(folderPredicateArgs131.getTree());


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
                    // 388:3: -> ^( PRED_DESCENDANT folderPredicateArgs )
                    {
                        // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:388:6: ^( PRED_DESCENDANT folderPredicateArgs )
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
    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:391:1: folderPredicateArgs : LPAREN ( qualifier COMMA )? folderId RPAREN -> folderId ( qualifier )? ;
    public final CMISParser.folderPredicateArgs_return folderPredicateArgs() throws RecognitionException {
        CMISParser.folderPredicateArgs_return retval = new CMISParser.folderPredicateArgs_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token LPAREN132=null;
        Token COMMA134=null;
        Token RPAREN136=null;
        CMISParser.qualifier_return qualifier133 = null;

        CMISParser.folderId_return folderId135 = null;


        Object LPAREN132_tree=null;
        Object COMMA134_tree=null;
        Object RPAREN136_tree=null;
        RewriteRuleTokenStream stream_RPAREN=new RewriteRuleTokenStream(adaptor,"token RPAREN");
        RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
        RewriteRuleTokenStream stream_LPAREN=new RewriteRuleTokenStream(adaptor,"token LPAREN");
        RewriteRuleSubtreeStream stream_qualifier=new RewriteRuleSubtreeStream(adaptor,"rule qualifier");
        RewriteRuleSubtreeStream stream_folderId=new RewriteRuleSubtreeStream(adaptor,"rule folderId");
        try {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:392:2: ( LPAREN ( qualifier COMMA )? folderId RPAREN -> folderId ( qualifier )? )
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:392:4: LPAREN ( qualifier COMMA )? folderId RPAREN
            {
            LPAREN132=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_folderPredicateArgs1583); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_LPAREN.add(LPAREN132);

            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:392:11: ( qualifier COMMA )?
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
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:392:12: qualifier COMMA
                    {
                    pushFollow(FOLLOW_qualifier_in_folderPredicateArgs1586);
                    qualifier133=qualifier();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_qualifier.add(qualifier133.getTree());
                    COMMA134=(Token)match(input,COMMA,FOLLOW_COMMA_in_folderPredicateArgs1588); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_COMMA.add(COMMA134);


                    }
                    break;

            }

            pushFollow(FOLLOW_folderId_in_folderPredicateArgs1592);
            folderId135=folderId();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_folderId.add(folderId135.getTree());
            RPAREN136=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_folderPredicateArgs1594); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_RPAREN.add(RPAREN136);



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
            // 393:3: -> folderId ( qualifier )?
            {
                adaptor.addChild(root_0, stream_folderId.nextTree());
                // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:393:15: ( qualifier )?
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
    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:396:1: orderByClause : ORDER BY sortSpecification ( COMMA sortSpecification )* -> ^( ORDER ( sortSpecification )+ ) ;
    public final CMISParser.orderByClause_return orderByClause() throws RecognitionException {
        CMISParser.orderByClause_return retval = new CMISParser.orderByClause_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token ORDER137=null;
        Token BY138=null;
        Token COMMA140=null;
        CMISParser.sortSpecification_return sortSpecification139 = null;

        CMISParser.sortSpecification_return sortSpecification141 = null;


        Object ORDER137_tree=null;
        Object BY138_tree=null;
        Object COMMA140_tree=null;
        RewriteRuleTokenStream stream_BY=new RewriteRuleTokenStream(adaptor,"token BY");
        RewriteRuleTokenStream stream_ORDER=new RewriteRuleTokenStream(adaptor,"token ORDER");
        RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
        RewriteRuleSubtreeStream stream_sortSpecification=new RewriteRuleSubtreeStream(adaptor,"rule sortSpecification");
            paraphrases.push("in order by"); 
        try {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:399:2: ( ORDER BY sortSpecification ( COMMA sortSpecification )* -> ^( ORDER ( sortSpecification )+ ) )
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:399:4: ORDER BY sortSpecification ( COMMA sortSpecification )*
            {
            ORDER137=(Token)match(input,ORDER,FOLLOW_ORDER_in_orderByClause1633); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_ORDER.add(ORDER137);

            BY138=(Token)match(input,BY,FOLLOW_BY_in_orderByClause1635); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_BY.add(BY138);

            pushFollow(FOLLOW_sortSpecification_in_orderByClause1637);
            sortSpecification139=sortSpecification();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_sortSpecification.add(sortSpecification139.getTree());
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:399:31: ( COMMA sortSpecification )*
            loop39:
            do {
                int alt39=2;
                int LA39_0 = input.LA(1);

                if ( (LA39_0==COMMA) ) {
                    alt39=1;
                }


                switch (alt39) {
            	case 1 :
            	    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:399:33: COMMA sortSpecification
            	    {
            	    COMMA140=(Token)match(input,COMMA,FOLLOW_COMMA_in_orderByClause1641); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_COMMA.add(COMMA140);

            	    pushFollow(FOLLOW_sortSpecification_in_orderByClause1643);
            	    sortSpecification141=sortSpecification();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_sortSpecification.add(sortSpecification141.getTree());

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
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 400:3: -> ^( ORDER ( sortSpecification )+ )
            {
                // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:400:6: ^( ORDER ( sortSpecification )+ )
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
    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:403:1: sortSpecification : ( columnReference -> ^( SORT_SPECIFICATION columnReference ASC ) | columnReference (by= ASC | by= DESC ) -> ^( SORT_SPECIFICATION columnReference $by) );
    public final CMISParser.sortSpecification_return sortSpecification() throws RecognitionException {
        CMISParser.sortSpecification_return retval = new CMISParser.sortSpecification_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token by=null;
        CMISParser.columnReference_return columnReference142 = null;

        CMISParser.columnReference_return columnReference143 = null;


        Object by_tree=null;
        RewriteRuleTokenStream stream_ASC=new RewriteRuleTokenStream(adaptor,"token ASC");
        RewriteRuleTokenStream stream_DESC=new RewriteRuleTokenStream(adaptor,"token DESC");
        RewriteRuleSubtreeStream stream_columnReference=new RewriteRuleSubtreeStream(adaptor,"rule columnReference");
        try {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:404:2: ( columnReference -> ^( SORT_SPECIFICATION columnReference ASC ) | columnReference (by= ASC | by= DESC ) -> ^( SORT_SPECIFICATION columnReference $by) )
            int alt41=2;
            alt41 = dfa41.predict(input);
            switch (alt41) {
                case 1 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:404:4: columnReference
                    {
                    pushFollow(FOLLOW_columnReference_in_sortSpecification1669);
                    columnReference142=columnReference();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_columnReference.add(columnReference142.getTree());


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
                    // 405:3: -> ^( SORT_SPECIFICATION columnReference ASC )
                    {
                        // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:405:6: ^( SORT_SPECIFICATION columnReference ASC )
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
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:406:4: columnReference (by= ASC | by= DESC )
                    {
                    pushFollow(FOLLOW_columnReference_in_sortSpecification1687);
                    columnReference143=columnReference();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_columnReference.add(columnReference143.getTree());
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:406:20: (by= ASC | by= DESC )
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
                            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:406:22: by= ASC
                            {
                            by=(Token)match(input,ASC,FOLLOW_ASC_in_sortSpecification1693); if (state.failed) return retval; 
                            if ( state.backtracking==0 ) stream_ASC.add(by);


                            }
                            break;
                        case 2 :
                            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:406:31: by= DESC
                            {
                            by=(Token)match(input,DESC,FOLLOW_DESC_in_sortSpecification1699); if (state.failed) return retval; 
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
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleTokenStream stream_by=new RewriteRuleTokenStream(adaptor,"token by",by);
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 407:3: -> ^( SORT_SPECIFICATION columnReference $by)
                    {
                        // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:407:6: ^( SORT_SPECIFICATION columnReference $by)
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
    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:410:1: correlationName : identifier ;
    public final CMISParser.correlationName_return correlationName() throws RecognitionException {
        CMISParser.correlationName_return retval = new CMISParser.correlationName_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        CMISParser.identifier_return identifier144 = null;



        try {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:411:2: ( identifier )
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:411:4: identifier
            {
            root_0 = (Object)adaptor.nil();

            pushFollow(FOLLOW_identifier_in_correlationName1726);
            identifier144=identifier();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, identifier144.getTree());

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
    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:418:1: tableName : identifier -> identifier ;
    public final CMISParser.tableName_return tableName() throws RecognitionException {
        CMISParser.tableName_return retval = new CMISParser.tableName_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        CMISParser.identifier_return identifier145 = null;


        RewriteRuleSubtreeStream stream_identifier=new RewriteRuleSubtreeStream(adaptor,"rule identifier");
        try {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:419:2: ( identifier -> identifier )
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:419:4: identifier
            {
            pushFollow(FOLLOW_identifier_in_tableName1740);
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
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 420:3: -> identifier
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
    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:423:1: columnName : identifier -> identifier ;
    public final CMISParser.columnName_return columnName() throws RecognitionException {
        CMISParser.columnName_return retval = new CMISParser.columnName_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        CMISParser.identifier_return identifier146 = null;


        RewriteRuleSubtreeStream stream_identifier=new RewriteRuleSubtreeStream(adaptor,"rule identifier");
        try {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:424:2: ( identifier -> identifier )
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:424:4: identifier
            {
            pushFollow(FOLLOW_identifier_in_columnName1758);
            identifier146=identifier();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_identifier.add(identifier146.getTree());


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
            // 425:3: -> identifier
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
    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:428:1: multiValuedColumnName : identifier -> identifier ;
    public final CMISParser.multiValuedColumnName_return multiValuedColumnName() throws RecognitionException {
        CMISParser.multiValuedColumnName_return retval = new CMISParser.multiValuedColumnName_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        CMISParser.identifier_return identifier147 = null;


        RewriteRuleSubtreeStream stream_identifier=new RewriteRuleSubtreeStream(adaptor,"rule identifier");
        try {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:429:2: ( identifier -> identifier )
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:429:4: identifier
            {
            pushFollow(FOLLOW_identifier_in_multiValuedColumnName1777);
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
            // 430:3: -> identifier
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
    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:433:1: parameterName : COLON identifier -> ^( PARAMETER identifier ) ;
    public final CMISParser.parameterName_return parameterName() throws RecognitionException {
        CMISParser.parameterName_return retval = new CMISParser.parameterName_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token COLON148=null;
        CMISParser.identifier_return identifier149 = null;


        Object COLON148_tree=null;
        RewriteRuleTokenStream stream_COLON=new RewriteRuleTokenStream(adaptor,"token COLON");
        RewriteRuleSubtreeStream stream_identifier=new RewriteRuleSubtreeStream(adaptor,"rule identifier");
        try {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:434:2: ( COLON identifier -> ^( PARAMETER identifier ) )
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:434:4: COLON identifier
            {
            COLON148=(Token)match(input,COLON,FOLLOW_COLON_in_parameterName1795); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_COLON.add(COLON148);

            pushFollow(FOLLOW_identifier_in_parameterName1797);
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
            // 435:3: -> ^( PARAMETER identifier )
            {
                // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:435:6: ^( PARAMETER identifier )
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
    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:438:1: folderId : characterStringLiteral -> characterStringLiteral ;
    public final CMISParser.folderId_return folderId() throws RecognitionException {
        CMISParser.folderId_return retval = new CMISParser.folderId_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        CMISParser.characterStringLiteral_return characterStringLiteral150 = null;


        RewriteRuleSubtreeStream stream_characterStringLiteral=new RewriteRuleSubtreeStream(adaptor,"rule characterStringLiteral");
        try {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:439:3: ( characterStringLiteral -> characterStringLiteral )
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:439:5: characterStringLiteral
            {
            pushFollow(FOLLOW_characterStringLiteral_in_folderId1820);
            characterStringLiteral150=characterStringLiteral();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_characterStringLiteral.add(characterStringLiteral150.getTree());


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
            // 440:4: -> characterStringLiteral
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
    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:443:1: textSearchExpression : QUOTED_STRING ;
    public final CMISParser.textSearchExpression_return textSearchExpression() throws RecognitionException {
        CMISParser.textSearchExpression_return retval = new CMISParser.textSearchExpression_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token QUOTED_STRING151=null;

        Object QUOTED_STRING151_tree=null;

        try {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:444:2: ( QUOTED_STRING )
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:444:4: QUOTED_STRING
            {
            root_0 = (Object)adaptor.nil();

            QUOTED_STRING151=(Token)match(input,QUOTED_STRING,FOLLOW_QUOTED_STRING_in_textSearchExpression1841); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            QUOTED_STRING151_tree = (Object)adaptor.create(QUOTED_STRING151);
            adaptor.addChild(root_0, QUOTED_STRING151_tree);
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
    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:447:1: identifier : ( ID -> ID | {...}? => DOUBLE_QUOTE keyWordOrId DOUBLE_QUOTE -> ^( keyWordOrId ) );
    public final CMISParser.identifier_return identifier() throws RecognitionException {
        CMISParser.identifier_return retval = new CMISParser.identifier_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token ID152=null;
        Token DOUBLE_QUOTE153=null;
        Token DOUBLE_QUOTE155=null;
        CMISParser.keyWordOrId_return keyWordOrId154 = null;


        Object ID152_tree=null;
        Object DOUBLE_QUOTE153_tree=null;
        Object DOUBLE_QUOTE155_tree=null;
        RewriteRuleTokenStream stream_ID=new RewriteRuleTokenStream(adaptor,"token ID");
        RewriteRuleTokenStream stream_DOUBLE_QUOTE=new RewriteRuleTokenStream(adaptor,"token DOUBLE_QUOTE");
        RewriteRuleSubtreeStream stream_keyWordOrId=new RewriteRuleSubtreeStream(adaptor,"rule keyWordOrId");
        try {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:448:2: ( ID -> ID | {...}? => DOUBLE_QUOTE keyWordOrId DOUBLE_QUOTE -> ^( keyWordOrId ) )
            int alt42=2;
            int LA42_0 = input.LA(1);

            if ( (LA42_0==ID) ) {
                alt42=1;
            }
            else if ( (LA42_0==DOUBLE_QUOTE) && ((strict == false))) {
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
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:448:4: ID
                    {
                    ID152=(Token)match(input,ID,FOLLOW_ID_in_identifier1853); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_ID.add(ID152);



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
                    // 449:3: -> ID
                    {
                        adaptor.addChild(root_0, stream_ID.nextNode());

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:450:4: {...}? => DOUBLE_QUOTE keyWordOrId DOUBLE_QUOTE
                    {
                    if ( !((strict == false)) ) {
                        if (state.backtracking>0) {state.failed=true; return retval;}
                        throw new FailedPredicateException(input, "identifier", "strict == false");
                    }
                    DOUBLE_QUOTE153=(Token)match(input,DOUBLE_QUOTE,FOLLOW_DOUBLE_QUOTE_in_identifier1868); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_DOUBLE_QUOTE.add(DOUBLE_QUOTE153);

                    pushFollow(FOLLOW_keyWordOrId_in_identifier1870);
                    keyWordOrId154=keyWordOrId();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_keyWordOrId.add(keyWordOrId154.getTree());
                    DOUBLE_QUOTE155=(Token)match(input,DOUBLE_QUOTE,FOLLOW_DOUBLE_QUOTE_in_identifier1872); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_DOUBLE_QUOTE.add(DOUBLE_QUOTE155);



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
                    // 451:3: -> ^( keyWordOrId )
                    {
                        // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:451:6: ^( keyWordOrId )
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
    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:454:1: signedNumericLiteral : ( FLOATING_POINT_LITERAL -> ^( NUMERIC_LITERAL FLOATING_POINT_LITERAL ) | integerLiteral -> integerLiteral );
    public final CMISParser.signedNumericLiteral_return signedNumericLiteral() throws RecognitionException {
        CMISParser.signedNumericLiteral_return retval = new CMISParser.signedNumericLiteral_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token FLOATING_POINT_LITERAL156=null;
        CMISParser.integerLiteral_return integerLiteral157 = null;


        Object FLOATING_POINT_LITERAL156_tree=null;
        RewriteRuleTokenStream stream_FLOATING_POINT_LITERAL=new RewriteRuleTokenStream(adaptor,"token FLOATING_POINT_LITERAL");
        RewriteRuleSubtreeStream stream_integerLiteral=new RewriteRuleSubtreeStream(adaptor,"rule integerLiteral");
        try {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:455:2: ( FLOATING_POINT_LITERAL -> ^( NUMERIC_LITERAL FLOATING_POINT_LITERAL ) | integerLiteral -> integerLiteral )
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
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:455:4: FLOATING_POINT_LITERAL
                    {
                    FLOATING_POINT_LITERAL156=(Token)match(input,FLOATING_POINT_LITERAL,FOLLOW_FLOATING_POINT_LITERAL_in_signedNumericLiteral1892); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_FLOATING_POINT_LITERAL.add(FLOATING_POINT_LITERAL156);



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
                    // 456:3: -> ^( NUMERIC_LITERAL FLOATING_POINT_LITERAL )
                    {
                        // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:456:6: ^( NUMERIC_LITERAL FLOATING_POINT_LITERAL )
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
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:457:4: integerLiteral
                    {
                    pushFollow(FOLLOW_integerLiteral_in_signedNumericLiteral1907);
                    integerLiteral157=integerLiteral();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_integerLiteral.add(integerLiteral157.getTree());


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
                    // 458:3: -> integerLiteral
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
    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:461:1: integerLiteral : DECIMAL_INTEGER_LITERAL -> ^( NUMERIC_LITERAL DECIMAL_INTEGER_LITERAL ) ;
    public final CMISParser.integerLiteral_return integerLiteral() throws RecognitionException {
        CMISParser.integerLiteral_return retval = new CMISParser.integerLiteral_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token DECIMAL_INTEGER_LITERAL158=null;

        Object DECIMAL_INTEGER_LITERAL158_tree=null;
        RewriteRuleTokenStream stream_DECIMAL_INTEGER_LITERAL=new RewriteRuleTokenStream(adaptor,"token DECIMAL_INTEGER_LITERAL");

        try {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:462:2: ( DECIMAL_INTEGER_LITERAL -> ^( NUMERIC_LITERAL DECIMAL_INTEGER_LITERAL ) )
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:462:4: DECIMAL_INTEGER_LITERAL
            {
            DECIMAL_INTEGER_LITERAL158=(Token)match(input,DECIMAL_INTEGER_LITERAL,FOLLOW_DECIMAL_INTEGER_LITERAL_in_integerLiteral1926); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_DECIMAL_INTEGER_LITERAL.add(DECIMAL_INTEGER_LITERAL158);



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
            // 463:3: -> ^( NUMERIC_LITERAL DECIMAL_INTEGER_LITERAL )
            {
                // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:463:6: ^( NUMERIC_LITERAL DECIMAL_INTEGER_LITERAL )
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

    public static class booleanLiteral_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "booleanLiteral"
    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:466:1: booleanLiteral : ( TRUE -> ^( BOOLEAN_LITERAL TRUE ) | FALSE -> ^( BOOLEAN_LITERAL FALSE ) );
    public final CMISParser.booleanLiteral_return booleanLiteral() throws RecognitionException {
        CMISParser.booleanLiteral_return retval = new CMISParser.booleanLiteral_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token TRUE159=null;
        Token FALSE160=null;

        Object TRUE159_tree=null;
        Object FALSE160_tree=null;
        RewriteRuleTokenStream stream_FALSE=new RewriteRuleTokenStream(adaptor,"token FALSE");
        RewriteRuleTokenStream stream_TRUE=new RewriteRuleTokenStream(adaptor,"token TRUE");

        try {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:467:3: ( TRUE -> ^( BOOLEAN_LITERAL TRUE ) | FALSE -> ^( BOOLEAN_LITERAL FALSE ) )
            int alt44=2;
            int LA44_0 = input.LA(1);

            if ( (LA44_0==TRUE) ) {
                alt44=1;
            }
            else if ( (LA44_0==FALSE) ) {
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
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:467:5: TRUE
                    {
                    TRUE159=(Token)match(input,TRUE,FOLLOW_TRUE_in_booleanLiteral1950); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_TRUE.add(TRUE159);



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
                    // 468:3: -> ^( BOOLEAN_LITERAL TRUE )
                    {
                        // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:468:7: ^( BOOLEAN_LITERAL TRUE )
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
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:469:5: FALSE
                    {
                    FALSE160=(Token)match(input,FALSE,FOLLOW_FALSE_in_booleanLiteral1968); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_FALSE.add(FALSE160);



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
                    // 470:3: -> ^( BOOLEAN_LITERAL FALSE )
                    {
                        // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:470:7: ^( BOOLEAN_LITERAL FALSE )
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
           throw e;
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
    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:473:1: datetimeLiteral : TIMESTAMP QUOTED_STRING -> ^( DATETIME_LITERAL QUOTED_STRING ) ;
    public final CMISParser.datetimeLiteral_return datetimeLiteral() throws RecognitionException {
        CMISParser.datetimeLiteral_return retval = new CMISParser.datetimeLiteral_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token TIMESTAMP161=null;
        Token QUOTED_STRING162=null;

        Object TIMESTAMP161_tree=null;
        Object QUOTED_STRING162_tree=null;
        RewriteRuleTokenStream stream_QUOTED_STRING=new RewriteRuleTokenStream(adaptor,"token QUOTED_STRING");
        RewriteRuleTokenStream stream_TIMESTAMP=new RewriteRuleTokenStream(adaptor,"token TIMESTAMP");

        try {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:474:3: ( TIMESTAMP QUOTED_STRING -> ^( DATETIME_LITERAL QUOTED_STRING ) )
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:474:5: TIMESTAMP QUOTED_STRING
            {
            TIMESTAMP161=(Token)match(input,TIMESTAMP,FOLLOW_TIMESTAMP_in_datetimeLiteral1993); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_TIMESTAMP.add(TIMESTAMP161);

            QUOTED_STRING162=(Token)match(input,QUOTED_STRING,FOLLOW_QUOTED_STRING_in_datetimeLiteral1995); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_QUOTED_STRING.add(QUOTED_STRING162);



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
            // 475:3: -> ^( DATETIME_LITERAL QUOTED_STRING )
            {
                // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:475:6: ^( DATETIME_LITERAL QUOTED_STRING )
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
           throw e;
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
    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:478:1: characterStringLiteral : QUOTED_STRING -> ^( STRING_LITERAL QUOTED_STRING ) ;
    public final CMISParser.characterStringLiteral_return characterStringLiteral() throws RecognitionException {
        CMISParser.characterStringLiteral_return retval = new CMISParser.characterStringLiteral_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token QUOTED_STRING163=null;

        Object QUOTED_STRING163_tree=null;
        RewriteRuleTokenStream stream_QUOTED_STRING=new RewriteRuleTokenStream(adaptor,"token QUOTED_STRING");

        try {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:479:2: ( QUOTED_STRING -> ^( STRING_LITERAL QUOTED_STRING ) )
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:479:4: QUOTED_STRING
            {
            QUOTED_STRING163=(Token)match(input,QUOTED_STRING,FOLLOW_QUOTED_STRING_in_characterStringLiteral2018); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_QUOTED_STRING.add(QUOTED_STRING163);



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
            // 480:3: -> ^( STRING_LITERAL QUOTED_STRING )
            {
                // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:480:6: ^( STRING_LITERAL QUOTED_STRING )
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
    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:484:1: keyWord : ( SELECT | AS | FROM | JOIN | INNER | LEFT | OUTER | ON | WHERE | OR | AND | NOT | IN | LIKE | IS | NULL | ANY | CONTAINS | IN_FOLDER | IN_TREE | ORDER | BY | ASC | DESC | TIMESTAMP | TRUE | FALSE );
    public final CMISParser.keyWord_return keyWord() throws RecognitionException {
        CMISParser.keyWord_return retval = new CMISParser.keyWord_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token set164=null;

        Object set164_tree=null;

        try {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:485:3: ( SELECT | AS | FROM | JOIN | INNER | LEFT | OUTER | ON | WHERE | OR | AND | NOT | IN | LIKE | IS | NULL | ANY | CONTAINS | IN_FOLDER | IN_TREE | ORDER | BY | ASC | DESC | TIMESTAMP | TRUE | FALSE )
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:
            {
            root_0 = (Object)adaptor.nil();

            set164=(Token)input.LT(1);
            if ( input.LA(1)==SELECT||input.LA(1)==AS||(input.LA(1)>=FROM && input.LA(1)<=ON)||(input.LA(1)>=WHERE && input.LA(1)<=NOT)||(input.LA(1)>=IN && input.LA(1)<=DESC)||(input.LA(1)>=TRUE && input.LA(1)<=TIMESTAMP) ) {
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
    // $ANTLR end "keyWord"

    public static class keyWordOrId_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "keyWordOrId"
    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:514:1: keyWordOrId : ( keyWord -> keyWord | ID -> ID );
    public final CMISParser.keyWordOrId_return keyWordOrId() throws RecognitionException {
        CMISParser.keyWordOrId_return retval = new CMISParser.keyWordOrId_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token ID166=null;
        CMISParser.keyWord_return keyWord165 = null;


        Object ID166_tree=null;
        RewriteRuleTokenStream stream_ID=new RewriteRuleTokenStream(adaptor,"token ID");
        RewriteRuleSubtreeStream stream_keyWord=new RewriteRuleSubtreeStream(adaptor,"rule keyWord");
        try {
            // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:515:2: ( keyWord -> keyWord | ID -> ID )
            int alt45=2;
            int LA45_0 = input.LA(1);

            if ( (LA45_0==SELECT||LA45_0==AS||(LA45_0>=FROM && LA45_0<=ON)||(LA45_0>=WHERE && LA45_0<=NOT)||(LA45_0>=IN && LA45_0<=DESC)||(LA45_0>=TRUE && LA45_0<=TIMESTAMP)) ) {
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
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:515:4: keyWord
                    {
                    pushFollow(FOLLOW_keyWord_in_keyWordOrId2208);
                    keyWord165=keyWord();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_keyWord.add(keyWord165.getTree());


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
                    // 516:3: -> keyWord
                    {
                        adaptor.addChild(root_0, stream_keyWord.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:517:4: ID
                    {
                    ID166=(Token)match(input,ID,FOLLOW_ID_in_keyWordOrId2220); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_ID.add(ID166);



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
                    // 518:3: -> ID
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
        // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:174:4: ( valueExpression )
        // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:174:5: valueExpression
        {
        pushFollow(FOLLOW_valueExpression_in_synpred1_CMIS295);
        valueExpression();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred1_CMIS

    // $ANTLR start synpred2_CMIS
    public final void synpred2_CMIS_fragment() throws RecognitionException {   
        // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:216:4: ( tableName )
        // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:216:5: tableName
        {
        pushFollow(FOLLOW_tableName_in_synpred2_CMIS574);
        tableName();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred2_CMIS

    // $ANTLR start synpred3_CMIS
    public final void synpred3_CMIS_fragment() throws RecognitionException {   
        // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:230:17: ( joinedTable )
        // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:230:18: joinedTable
        {
        pushFollow(FOLLOW_joinedTable_in_synpred3_CMIS652);
        joinedTable();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred3_CMIS

    // $ANTLR start synpred5_CMIS
    public final void synpred5_CMIS_fragment() throws RecognitionException {   
        // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:362:6: ( columnReference )
        // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:362:7: columnReference
        {
        pushFollow(FOLLOW_columnReference_in_synpred5_CMIS1338);
        columnReference();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred5_CMIS

    // $ANTLR start synpred6_CMIS
    public final void synpred6_CMIS_fragment() throws RecognitionException {   
        // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:364:11: ( columnReference )
        // W:\\alfresco\\BRANCHES\\DEV\\CMIS063\\root\\projects\\Repository\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:364:12: columnReference
        {
        pushFollow(FOLLOW_columnReference_in_synpred6_CMIS1376);
        columnReference();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred6_CMIS

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


    protected DFA7 dfa7 = new DFA7(this);
    protected DFA9 dfa9 = new DFA9(this);
    protected DFA10 dfa10 = new DFA10(this);
    protected DFA12 dfa12 = new DFA12(this);
    protected DFA24 dfa24 = new DFA24(this);
    protected DFA26 dfa26 = new DFA26(this);
    protected DFA34 dfa34 = new DFA34(this);
    protected DFA41 dfa41 = new DFA41(this);
    static final String DFA7_eotS =
        "\13\uffff";
    static final String DFA7_eofS =
        "\13\uffff";
    static final String DFA7_minS =
        "\1\37\1\43\1\37\3\uffff\2\106\2\43\1\uffff";
    static final String DFA7_maxS =
        "\1\113\1\43\1\113\3\uffff\2\106\2\43\1\uffff";
    static final String DFA7_acceptS =
        "\3\uffff\1\1\1\2\1\3\4\uffff\1\2";
    static final String DFA7_specialS =
        "\1\4\1\3\1\6\3\uffff\1\5\1\2\1\1\1\0\1\uffff}>";
    static final String[] DFA7_transitionS = {
            "\1\3\2\uffff\1\3\4\uffff\6\3\1\uffff\4\3\5\uffff\14\3\2\uffff"+
            "\1\1\1\2\2\uffff\3\3",
            "\1\4",
            "\1\6\2\uffff\1\6\4\uffff\6\6\1\uffff\4\6\5\uffff\14\6\2\uffff"+
            "\1\7\3\uffff\3\6",
            "",
            "",
            "",
            "\1\10",
            "\1\11",
            "\1\12",
            "\1\12",
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
            return "173:1: selectSubList : ( ( valueExpression )=> valueExpression ( ( AS )? columnName )? -> ^( COLUMN valueExpression ( columnName )? ) | qualifier DOTSTAR -> ^( ALL_COLUMNS qualifier ) | multiValuedColumnReference ->);";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA7_9 = input.LA(1);

                         
                        int index7_9 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA7_9==DOTSTAR) && ((strict == false))) {s = 10;}

                        else if ( (((synpred1_CMIS()&&(strict == false))||(synpred1_CMIS()&&(strict == false))||(synpred1_CMIS()&&(strict == false)))) ) {s = 3;}

                        else if ( ((strict == false)) ) {s = 5;}

                         
                        input.seek(index7_9);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA7_8 = input.LA(1);

                         
                        int index7_8 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA7_8==DOTSTAR) && ((strict == false))) {s = 10;}

                        else if ( (((synpred1_CMIS()&&(strict == false))||(synpred1_CMIS()&&(strict == false))||(synpred1_CMIS()&&(strict == false)))) ) {s = 3;}

                        else if ( ((strict == false)) ) {s = 5;}

                         
                        input.seek(index7_8);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA7_7 = input.LA(1);

                         
                        int index7_7 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA7_7==DOUBLE_QUOTE) && ((strict == false))) {s = 9;}

                         
                        input.seek(index7_7);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA7_1 = input.LA(1);

                         
                        int index7_1 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA7_1==DOTSTAR) ) {s = 4;}

                        else if ( (synpred1_CMIS()) ) {s = 3;}

                        else if ( (true) ) {s = 5;}

                         
                        input.seek(index7_1);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA7_0 = input.LA(1);

                         
                        int index7_0 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA7_0==ID) ) {s = 1;}

                        else if ( (LA7_0==DOUBLE_QUOTE) && ((strict == false))) {s = 2;}

                        else if ( (LA7_0==SELECT||LA7_0==AS||(LA7_0>=FROM && LA7_0<=ON)||(LA7_0>=WHERE && LA7_0<=NOT)||(LA7_0>=IN && LA7_0<=DESC)||(LA7_0>=TRUE && LA7_0<=TIMESTAMP)) && (synpred1_CMIS())) {s = 3;}

                         
                        input.seek(index7_0);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA7_6 = input.LA(1);

                         
                        int index7_6 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA7_6==DOUBLE_QUOTE) && ((strict == false))) {s = 8;}

                         
                        input.seek(index7_6);
                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA7_2 = input.LA(1);

                         
                        int index7_2 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA7_2==SELECT||LA7_2==AS||(LA7_2>=FROM && LA7_2<=ON)||(LA7_2>=WHERE && LA7_2<=NOT)||(LA7_2>=IN && LA7_2<=DESC)||(LA7_2>=TRUE && LA7_2<=TIMESTAMP)) && ((strict == false))) {s = 6;}

                        else if ( (LA7_2==ID) && ((strict == false))) {s = 7;}

                         
                        input.seek(index7_2);
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
        "\12\uffff";
    static final String DFA9_eofS =
        "\1\uffff\1\3\5\uffff\1\10\2\uffff";
    static final String DFA9_minS =
        "\1\105\1\41\1\37\2\uffff\2\106\1\41\2\uffff";
    static final String DFA9_maxS =
        "\2\106\1\113\2\uffff\3\106\2\uffff";
    static final String DFA9_acceptS =
        "\3\uffff\1\2\1\1\3\uffff\1\2\1\1";
    static final String DFA9_specialS =
        "\1\2\1\uffff\1\0\2\uffff\1\3\1\4\1\1\2\uffff}>";
    static final String[] DFA9_transitionS = {
            "\1\1\1\2",
            "\2\3\1\uffff\1\4\1\uffff\5\3\1\uffff\3\3\2\uffff\11\3\5\uffff"+
            "\1\3\1\uffff\2\3\2\uffff\2\3",
            "\1\5\2\uffff\1\5\4\uffff\6\5\1\uffff\4\5\5\uffff\14\5\2\uffff"+
            "\1\6\3\uffff\3\5",
            "",
            "",
            "\1\7",
            "\1\7",
            "\2\10\1\uffff\1\11\1\uffff\5\10\1\uffff\3\10\2\uffff\11\10"+
            "\5\uffff\1\10\1\uffff\2\10\2\uffff\2\10",
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
            return "190:4: ( qualifier DOT )?";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA9_2 = input.LA(1);

                         
                        int index9_2 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA9_2==SELECT||LA9_2==AS||(LA9_2>=FROM && LA9_2<=ON)||(LA9_2>=WHERE && LA9_2<=NOT)||(LA9_2>=IN && LA9_2<=DESC)||(LA9_2>=TRUE && LA9_2<=TIMESTAMP)) && ((strict == false))) {s = 5;}

                        else if ( (LA9_2==ID) && ((strict == false))) {s = 6;}

                         
                        input.seek(index9_2);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA9_7 = input.LA(1);

                         
                        int index9_7 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA9_7==EOF||(LA9_7>=COMMA && LA9_7<=AS)||(LA9_7>=RPAREN && LA9_7<=LEFT)||(LA9_7>=ON && LA9_7<=WHERE)||(LA9_7>=NOT && LA9_7<=IS)||LA9_7==ORDER||(LA9_7>=ASC && LA9_7<=DESC)||(LA9_7>=ID && LA9_7<=DOUBLE_QUOTE)) && ((strict == false))) {s = 8;}

                        else if ( (LA9_7==DOT) && ((strict == false))) {s = 9;}

                         
                        input.seek(index9_7);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA9_0 = input.LA(1);

                         
                        int index9_0 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA9_0==ID) ) {s = 1;}

                        else if ( (LA9_0==DOUBLE_QUOTE) && ((strict == false))) {s = 2;}

                         
                        input.seek(index9_0);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA9_5 = input.LA(1);

                         
                        int index9_5 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA9_5==DOUBLE_QUOTE) && ((strict == false))) {s = 7;}

                         
                        input.seek(index9_5);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA9_6 = input.LA(1);

                         
                        int index9_6 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA9_6==DOUBLE_QUOTE) && ((strict == false))) {s = 7;}

                         
                        input.seek(index9_6);
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
        "\12\uffff";
    static final String DFA10_eofS =
        "\1\uffff\1\4\5\uffff\1\10\2\uffff";
    static final String DFA10_minS =
        "\1\105\1\41\1\37\2\uffff\2\106\1\41\2\uffff";
    static final String DFA10_maxS =
        "\1\106\1\77\1\113\2\uffff\2\106\1\77\2\uffff";
    static final String DFA10_acceptS =
        "\3\uffff\1\1\1\2\3\uffff\1\2\1\1";
    static final String DFA10_specialS =
        "\1\3\1\uffff\1\0\2\uffff\1\2\1\4\1\1\2\uffff}>";
    static final String[] DFA10_transitionS = {
            "\1\1\1\2",
            "\1\4\2\uffff\1\3\1\uffff\2\4\7\uffff\3\4\5\uffff\1\4\1\uffff"+
            "\1\4\5\uffff\1\4",
            "\1\5\2\uffff\1\5\4\uffff\6\5\1\uffff\4\5\5\uffff\14\5\2\uffff"+
            "\1\6\3\uffff\3\5",
            "",
            "",
            "\1\7",
            "\1\7",
            "\1\10\2\uffff\1\11\1\uffff\2\10\7\uffff\3\10\5\uffff\1\10"+
            "\1\uffff\1\10\5\uffff\1\10",
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
            return "199:10: ( qualifier DOT )?";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA10_2 = input.LA(1);

                         
                        int index10_2 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA10_2==SELECT||LA10_2==AS||(LA10_2>=FROM && LA10_2<=ON)||(LA10_2>=WHERE && LA10_2<=NOT)||(LA10_2>=IN && LA10_2<=DESC)||(LA10_2>=TRUE && LA10_2<=TIMESTAMP)) && ((strict == false))) {s = 5;}

                        else if ( (LA10_2==ID) && ((strict == false))) {s = 6;}

                         
                        input.seek(index10_2);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA10_7 = input.LA(1);

                         
                        int index10_7 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA10_7==EOF||LA10_7==COMMA||(LA10_7>=RPAREN && LA10_7<=FROM)||(LA10_7>=OR && LA10_7<=NOT)||LA10_7==IN||LA10_7==IS||LA10_7==ORDER) && ((strict == false))) {s = 8;}

                        else if ( (LA10_7==DOT) && ((strict == false))) {s = 9;}

                         
                        input.seek(index10_7);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA10_5 = input.LA(1);

                         
                        int index10_5 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA10_5==DOUBLE_QUOTE) && ((strict == false))) {s = 7;}

                         
                        input.seek(index10_5);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA10_0 = input.LA(1);

                         
                        int index10_0 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA10_0==ID) ) {s = 1;}

                        else if ( (LA10_0==DOUBLE_QUOTE) && ((strict == false))) {s = 2;}

                         
                        input.seek(index10_0);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA10_6 = input.LA(1);

                         
                        int index10_6 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA10_6==DOUBLE_QUOTE) && ((strict == false))) {s = 7;}

                         
                        input.seek(index10_6);
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
    static final String DFA12_eotS =
        "\14\uffff";
    static final String DFA12_eofS =
        "\14\uffff";
    static final String DFA12_minS =
        "\1\103\1\44\1\37\4\uffff\2\106\1\44\2\uffff";
    static final String DFA12_maxS =
        "\3\113\4\uffff\2\106\1\113\2\uffff";
    static final String DFA12_acceptS =
        "\3\uffff\2\3\1\2\1\1\3\uffff\1\1\1\2";
    static final String DFA12_specialS =
        "\1\0\1\uffff\1\1\4\uffff\1\3\1\4\1\2\2\uffff}>";
    static final String[] DFA12_transitionS = {
            "\1\4\1\3\1\1\1\2\5\3",
            "\1\6\1\uffff\1\5\34\uffff\11\5",
            "\1\7\2\uffff\1\7\4\uffff\6\7\1\uffff\4\7\5\uffff\14\7\2\uffff"+
            "\1\10\3\uffff\3\7",
            "",
            "",
            "",
            "",
            "\1\11",
            "\1\11",
            "\1\12\1\uffff\1\13\34\uffff\11\13",
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
            return "208:1: functionArgument : ( qualifier DOT columnName -> ^( COLUMN_REF columnName qualifier ) | identifier | literalOrParameterName );";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA12_0 = input.LA(1);

                         
                        int index12_0 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA12_0==ID) ) {s = 1;}

                        else if ( (LA12_0==DOUBLE_QUOTE) && ((strict == false))) {s = 2;}

                        else if ( (LA12_0==QUOTED_STRING||(LA12_0>=FLOATING_POINT_LITERAL && LA12_0<=TIMESTAMP)) ) {s = 3;}

                        else if ( (LA12_0==COLON) && ((strict == false))) {s = 4;}

                         
                        input.seek(index12_0);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA12_2 = input.LA(1);

                         
                        int index12_2 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA12_2==SELECT||LA12_2==AS||(LA12_2>=FROM && LA12_2<=ON)||(LA12_2>=WHERE && LA12_2<=NOT)||(LA12_2>=IN && LA12_2<=DESC)||(LA12_2>=TRUE && LA12_2<=TIMESTAMP)) && ((strict == false))) {s = 7;}

                        else if ( (LA12_2==ID) && ((strict == false))) {s = 8;}

                         
                        input.seek(index12_2);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA12_9 = input.LA(1);

                         
                        int index12_9 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA12_9==DOT) && ((strict == false))) {s = 10;}

                        else if ( (LA12_9==RPAREN||(LA12_9>=COLON && LA12_9<=TIMESTAMP)) && ((strict == false))) {s = 11;}

                         
                        input.seek(index12_9);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA12_7 = input.LA(1);

                         
                        int index12_7 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA12_7==DOUBLE_QUOTE) && ((strict == false))) {s = 9;}

                         
                        input.seek(index12_7);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA12_8 = input.LA(1);

                         
                        int index12_8 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA12_8==DOUBLE_QUOTE) && ((strict == false))) {s = 9;}

                         
                        input.seek(index12_8);
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
    static final String DFA24_eotS =
        "\41\uffff";
    static final String DFA24_eofS =
        "\41\uffff";
    static final String DFA24_minS =
        "\2\37\5\uffff\1\37\1\uffff\1\44\1\37\1\45\3\46\2\45\2\105\2\106"+
        "\2\46\1\37\1\46\1\37\1\44\4\106\2\46";
    static final String DFA24_maxS =
        "\2\113\5\uffff\1\113\1\uffff\7\113\1\104\4\106\6\113\4\106\2\113";
    static final String DFA24_acceptS =
        "\2\uffff\5\2\1\uffff\1\1\30\uffff";
    static final String DFA24_specialS =
        "\1\0\40\uffff}>";
    static final String[] DFA24_transitionS = {
            "\1\6\2\uffff\1\6\2\uffff\1\6\1\uffff\6\6\1\uffff\3\6\1\1\5"+
            "\uffff\4\6\1\4\7\6\1\5\1\4\1\2\1\3\5\4",
            "\1\10\2\uffff\1\10\2\uffff\1\7\1\uffff\6\10\1\uffff\4\10\5"+
            "\uffff\25\10",
            "",
            "",
            "",
            "",
            "",
            "\1\10\2\uffff\1\10\2\uffff\1\10\1\6\6\10\1\uffff\4\10\5\uffff"+
            "\14\10\1\21\1\16\1\11\1\12\1\14\1\15\1\13\1\17\1\20",
            "",
            "\1\22\1\10\1\6\6\uffff\1\10\3\uffff\11\10\11\uffff\11\6",
            "\1\23\2\uffff\1\23\4\uffff\6\23\1\uffff\4\23\5\uffff\14\23"+
            "\2\uffff\1\24\3\uffff\3\23",
            "\1\10\1\6\6\uffff\1\10\4\uffff\5\10\14\uffff\11\6",
            "\1\6\6\uffff\1\10\4\uffff\5\10\14\uffff\11\6",
            "\1\6\6\uffff\1\10\4\uffff\5\10\14\uffff\11\6",
            "\1\6\6\uffff\1\10\4\uffff\5\10\14\uffff\11\6",
            "\1\10\1\6\6\uffff\1\10\4\uffff\5\10\14\uffff\11\6",
            "\1\10\36\uffff\1\25",
            "\1\26\1\27",
            "\1\30\1\31",
            "\1\32",
            "\1\32",
            "\1\6\6\uffff\1\10\4\uffff\5\10\14\uffff\11\6",
            "\1\6\6\uffff\1\10\4\uffff\5\10\14\uffff\11\6",
            "\1\33\2\uffff\1\33\4\uffff\6\33\1\uffff\4\33\5\uffff\14\33"+
            "\2\uffff\1\34\3\uffff\3\33",
            "\1\6\6\uffff\1\10\3\uffff\11\10\11\uffff\11\6",
            "\1\35\2\uffff\1\35\4\uffff\6\35\1\uffff\4\35\5\uffff\14\35"+
            "\2\uffff\1\36\3\uffff\3\35",
            "\1\22\1\uffff\1\6\6\uffff\1\10\3\uffff\11\10\11\uffff\11\6",
            "\1\37",
            "\1\37",
            "\1\40",
            "\1\40",
            "\1\6\6\uffff\1\10\4\uffff\5\10\14\uffff\11\6",
            "\1\6\6\uffff\1\10\3\uffff\11\10\11\uffff\11\6"
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
            return "295:1: searchNotCondition : ( NOT searchTest -> ^( NEGATION searchTest ) | searchTest -> searchTest );";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA24_0 = input.LA(1);

                         
                        int index24_0 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA24_0==NOT) ) {s = 1;}

                        else if ( (LA24_0==ID) ) {s = 2;}

                        else if ( (LA24_0==DOUBLE_QUOTE) && ((strict == false))) {s = 3;}

                        else if ( (LA24_0==ANY||LA24_0==QUOTED_STRING||(LA24_0>=FLOATING_POINT_LITERAL && LA24_0<=TIMESTAMP)) ) {s = 4;}

                        else if ( (LA24_0==COLON) && ((strict == false))) {s = 5;}

                        else if ( (LA24_0==SELECT||LA24_0==AS||LA24_0==LPAREN||(LA24_0>=FROM && LA24_0<=ON)||(LA24_0>=WHERE && LA24_0<=AND)||(LA24_0>=IN && LA24_0<=NULL)||(LA24_0>=CONTAINS && LA24_0<=DESC)) ) {s = 6;}

                         
                        input.seek(index24_0);
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
    static final String DFA26_eotS =
        "\102\uffff";
    static final String DFA26_eofS =
        "\61\uffff\1\56\3\uffff\1\62\2\uffff\1\62\11\uffff";
    static final String DFA26_minS =
        "\1\37\1\44\1\37\1\45\1\uffff\3\45\1\uffff\3\45\1\uffff\1\67\1\uffff"+
        "\1\105\2\uffff\2\106\2\uffff\3\46\1\55\1\37\1\44\1\41\1\37\1\46"+
        "\1\41\1\37\1\46\1\41\1\37\1\46\2\106\1\uffff\1\105\1\67\5\uffff"+
        "\2\106\1\46\1\uffff\2\106\1\46\2\106\1\46\2\55\1\37\3\41\2\106\1"+
        "\55";
    static final String DFA26_maxS =
        "\1\113\1\71\1\113\1\66\1\uffff\1\66\1\104\1\106\1\uffff\3\45\1"+
        "\uffff\1\70\1\uffff\1\106\2\uffff\2\106\2\uffff\3\113\1\71\1\113"+
        "\1\71\11\113\2\106\1\uffff\1\106\1\70\5\uffff\2\106\1\77\1\uffff"+
        "\2\106\1\77\2\106\1\77\2\71\4\113\2\106\1\71";
    static final String DFA26_acceptS =
        "\4\uffff\1\5\3\uffff\1\5\3\uffff\1\1\1\uffff\1\2\1\uffff\1\4\1"+
        "\3\2\uffff\1\5\1\6\21\uffff\1\4\2\uffff\1\2\1\3\2\1\1\7\3\uffff"+
        "\1\10\17\uffff";
    static final String DFA26_specialS =
        "\1\3\1\uffff\1\4\17\uffff\1\2\1\7\7\uffff\1\11\14\uffff\1\1\1\10"+
        "\20\uffff\1\6\1\0\3\uffff\1\5\1\13\1\12}>";
    static final String[] DFA26_transitionS = {
            "\1\14\2\uffff\1\14\4\uffff\6\14\1\uffff\4\14\5\uffff\4\14\1"+
            "\7\1\11\1\12\1\13\4\14\1\10\1\4\1\1\1\2\2\4\1\3\1\5\1\6",
            "\1\17\1\14\7\uffff\1\14\3\uffff\1\15\5\14\1\16\1\21\1\20",
            "\1\22\2\uffff\1\22\4\uffff\6\22\1\uffff\4\22\5\uffff\14\22"+
            "\2\uffff\1\23\3\uffff\3\22",
            "\1\14\7\uffff\1\24\4\uffff\5\24",
            "",
            "\1\14\7\uffff\1\24\4\uffff\5\24",
            "\1\14\36\uffff\1\24",
            "\1\14\37\uffff\2\25",
            "",
            "\1\26",
            "\1\27",
            "\1\30",
            "",
            "\1\16\1\21",
            "",
            "\1\31\1\32",
            "",
            "",
            "\1\33",
            "\1\33",
            "",
            "",
            "\1\14\34\uffff\1\14\1\36\1\34\1\35\5\14",
            "\1\14\34\uffff\1\14\1\41\1\37\1\40\5\14",
            "\1\14\34\uffff\1\14\1\44\1\42\1\43\5\14",
            "\1\14\3\uffff\1\15\5\14\1\16\1\21\1\20",
            "\1\45\2\uffff\1\45\4\uffff\6\45\1\uffff\4\45\5\uffff\14\45"+
            "\2\uffff\1\46\3\uffff\3\45",
            "\1\50\10\uffff\1\54\3\uffff\1\51\5\54\1\52\1\53\1\47",
            "\1\56\2\uffff\1\55\1\uffff\1\55\34\uffff\11\55",
            "\1\57\2\uffff\1\57\4\uffff\6\57\1\uffff\4\57\5\uffff\14\57"+
            "\2\uffff\1\60\3\uffff\3\57",
            "\1\61\34\uffff\11\55",
            "\1\62\2\uffff\1\55\1\uffff\1\55\34\uffff\11\55",
            "\1\63\2\uffff\1\63\4\uffff\6\63\1\uffff\4\63\5\uffff\14\63"+
            "\2\uffff\1\64\3\uffff\3\63",
            "\1\65\34\uffff\11\55",
            "\1\62\2\uffff\1\55\1\uffff\1\55\34\uffff\11\55",
            "\1\66\2\uffff\1\66\4\uffff\6\66\1\uffff\4\66\5\uffff\14\66"+
            "\2\uffff\1\67\3\uffff\3\66",
            "\1\70\34\uffff\11\55",
            "\1\71",
            "\1\71",
            "",
            "\1\72\1\73",
            "\1\52\1\53",
            "",
            "",
            "",
            "",
            "",
            "\1\74",
            "\1\74",
            "\1\56\6\uffff\1\55\1\uffff\2\56\1\uffff\5\55\10\uffff\1\56",
            "",
            "\1\75",
            "\1\75",
            "\1\62\6\uffff\1\55\1\uffff\2\62\1\uffff\5\55\10\uffff\1\62",
            "\1\76",
            "\1\76",
            "\1\62\6\uffff\1\55\1\uffff\2\62\1\uffff\5\55\10\uffff\1\62",
            "\1\55\3\uffff\1\15\5\55\1\16\1\21\1\20",
            "\1\54\3\uffff\1\51\5\54\1\52\1\53\1\47",
            "\1\77\2\uffff\1\77\4\uffff\6\77\1\uffff\4\77\5\uffff\14\77"+
            "\2\uffff\1\100\3\uffff\3\77",
            "\1\56\2\uffff\1\55\1\uffff\1\55\34\uffff\11\55",
            "\1\62\2\uffff\1\55\1\uffff\1\55\34\uffff\11\55",
            "\1\62\2\uffff\1\55\1\uffff\1\55\34\uffff\11\55",
            "\1\101",
            "\1\101",
            "\1\54\3\uffff\1\51\5\54\1\52\1\53\1\47"
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
            return "309:1: predicate : ( comparisonPredicate | inPredicate | likePredicate | nullPredicate | quantifiedComparisonPredicate | quantifiedInPredicate | textSearchPredicate | folderPredicate );";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA26_59 = input.LA(1);

                         
                        int index26_59 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA26_59==SELECT||LA26_59==AS||(LA26_59>=FROM && LA26_59<=ON)||(LA26_59>=WHERE && LA26_59<=NOT)||(LA26_59>=IN && LA26_59<=DESC)||(LA26_59>=TRUE && LA26_59<=TIMESTAMP)) && ((strict == false))) {s = 63;}

                        else if ( (LA26_59==ID) && ((strict == false))) {s = 64;}

                         
                        input.seek(index26_59);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA26_40 = input.LA(1);

                         
                        int index26_40 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA26_40==ID) && ((strict == false))) {s = 58;}

                        else if ( (LA26_40==DOUBLE_QUOTE) && ((strict == false))) {s = 59;}

                         
                        input.seek(index26_40);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA26_18 = input.LA(1);

                         
                        int index26_18 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA26_18==DOUBLE_QUOTE) && ((strict == false))) {s = 27;}

                         
                        input.seek(index26_18);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA26_0 = input.LA(1);

                         
                        int index26_0 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA26_0==ID) ) {s = 1;}

                        else if ( (LA26_0==DOUBLE_QUOTE) && ((strict == false))) {s = 2;}

                        else if ( (LA26_0==TRUE) ) {s = 3;}

                        else if ( (LA26_0==QUOTED_STRING||(LA26_0>=FLOATING_POINT_LITERAL && LA26_0<=DECIMAL_INTEGER_LITERAL)) ) {s = 4;}

                        else if ( (LA26_0==FALSE) ) {s = 5;}

                        else if ( (LA26_0==TIMESTAMP) ) {s = 6;}

                        else if ( (LA26_0==ANY) ) {s = 7;}

                        else if ( (LA26_0==COLON) && ((strict == false))) {s = 8;}

                        else if ( (LA26_0==CONTAINS) ) {s = 9;}

                        else if ( (LA26_0==IN_FOLDER) ) {s = 10;}

                        else if ( (LA26_0==IN_TREE) ) {s = 11;}

                        else if ( (LA26_0==SELECT||LA26_0==AS||(LA26_0>=FROM && LA26_0<=ON)||(LA26_0>=WHERE && LA26_0<=NOT)||(LA26_0>=IN && LA26_0<=NULL)||(LA26_0>=ORDER && LA26_0<=DESC)) ) {s = 12;}

                         
                        input.seek(index26_0);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA26_2 = input.LA(1);

                         
                        int index26_2 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA26_2==SELECT||LA26_2==AS||(LA26_2>=FROM && LA26_2<=ON)||(LA26_2>=WHERE && LA26_2<=NOT)||(LA26_2>=IN && LA26_2<=DESC)||(LA26_2>=TRUE && LA26_2<=TIMESTAMP)) && ((strict == false))) {s = 18;}

                        else if ( (LA26_2==ID) && ((strict == false))) {s = 19;}

                         
                        input.seek(index26_2);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA26_63 = input.LA(1);

                         
                        int index26_63 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA26_63==DOUBLE_QUOTE) && ((strict == false))) {s = 65;}

                         
                        input.seek(index26_63);
                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA26_58 = input.LA(1);

                         
                        int index26_58 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA26_58==NOT) && ((strict == false))) {s = 41;}

                        else if ( (LA26_58==IN) && ((strict == false))) {s = 42;}

                        else if ( (LA26_58==IS) && ((strict == false))) {s = 39;}

                        else if ( (LA26_58==LIKE) && ((strict == false))) {s = 43;}

                        else if ( (LA26_58==EQUALS||(LA26_58>=NOTEQUALS && LA26_58<=GREATERTHANOREQUALS)) && ((strict == false))) {s = 44;}

                         
                        input.seek(index26_58);
                        if ( s>=0 ) return s;
                        break;
                    case 7 : 
                        int LA26_19 = input.LA(1);

                         
                        int index26_19 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA26_19==DOUBLE_QUOTE) && ((strict == false))) {s = 27;}

                         
                        input.seek(index26_19);
                        if ( s>=0 ) return s;
                        break;
                    case 8 : 
                        int LA26_41 = input.LA(1);

                         
                        int index26_41 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA26_41==IN) && ((strict == false))) {s = 42;}

                        else if ( (LA26_41==LIKE) && ((strict == false))) {s = 43;}

                         
                        input.seek(index26_41);
                        if ( s>=0 ) return s;
                        break;
                    case 9 : 
                        int LA26_27 = input.LA(1);

                         
                        int index26_27 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA26_27==IS) && ((strict == false))) {s = 39;}

                        else if ( (LA26_27==DOT) && ((strict == false))) {s = 40;}

                        else if ( (LA26_27==NOT) && ((strict == false))) {s = 41;}

                        else if ( (LA26_27==IN) && ((strict == false))) {s = 42;}

                        else if ( (LA26_27==LIKE) && ((strict == false))) {s = 43;}

                        else if ( (LA26_27==EQUALS||(LA26_27>=NOTEQUALS && LA26_27<=GREATERTHANOREQUALS)) && ((strict == false))) {s = 44;}

                         
                        input.seek(index26_27);
                        if ( s>=0 ) return s;
                        break;
                    case 10 : 
                        int LA26_65 = input.LA(1);

                         
                        int index26_65 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA26_65==IS) && ((strict == false))) {s = 39;}

                        else if ( (LA26_65==NOT) && ((strict == false))) {s = 41;}

                        else if ( (LA26_65==IN) && ((strict == false))) {s = 42;}

                        else if ( (LA26_65==LIKE) && ((strict == false))) {s = 43;}

                        else if ( (LA26_65==EQUALS||(LA26_65>=NOTEQUALS && LA26_65<=GREATERTHANOREQUALS)) && ((strict == false))) {s = 44;}

                         
                        input.seek(index26_65);
                        if ( s>=0 ) return s;
                        break;
                    case 11 : 
                        int LA26_64 = input.LA(1);

                         
                        int index26_64 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA26_64==DOUBLE_QUOTE) && ((strict == false))) {s = 65;}

                         
                        input.seek(index26_64);
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
    static final String DFA34_eotS =
        "\30\uffff";
    static final String DFA34_eofS =
        "\30\uffff";
    static final String DFA34_minS =
        "\1\105\1\44\1\37\1\61\1\105\2\106\2\uffff\1\71\1\37\1\44\2\106"+
        "\1\105\1\61\2\71\1\37\2\uffff\2\106\1\71";
    static final String DFA34_maxS =
        "\1\106\1\71\1\113\1\72\3\106\2\uffff\1\71\1\113\1\71\3\106\1\72"+
        "\2\71\1\113\2\uffff\2\106\1\71";
    static final String DFA34_acceptS =
        "\7\uffff\1\1\1\2\12\uffff\1\2\1\1\3\uffff";
    static final String DFA34_specialS =
        "\1\1\1\uffff\1\11\2\uffff\1\10\1\2\4\uffff\1\13\2\uffff\1\12\1"+
        "\5\1\uffff\1\3\1\4\2\uffff\1\7\1\0\1\6}>";
    static final String[] DFA34_transitionS = {
            "\1\1\1\2",
            "\1\4\24\uffff\1\3",
            "\1\5\2\uffff\1\5\4\uffff\6\5\1\uffff\4\5\5\uffff\14\5\2\uffff"+
            "\1\6\3\uffff\3\5",
            "\1\10\10\uffff\1\7",
            "\1\11\1\12",
            "\1\13",
            "\1\13",
            "",
            "",
            "\1\3",
            "\1\14\2\uffff\1\14\4\uffff\6\14\1\uffff\4\14\5\uffff\14\14"+
            "\2\uffff\1\15\3\uffff\3\14",
            "\1\16\24\uffff\1\17",
            "\1\20",
            "\1\20",
            "\1\21\1\22",
            "\1\23\10\uffff\1\24",
            "\1\3",
            "\1\17",
            "\1\25\2\uffff\1\25\4\uffff\6\25\1\uffff\4\25\5\uffff\14\25"+
            "\2\uffff\1\26\3\uffff\3\25",
            "",
            "",
            "\1\27",
            "\1\27",
            "\1\17"
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
            return "361:1: nullPredicate : ( ( ( columnReference )=> columnReference | multiValuedColumnReference ) IS NULL -> ^( PRED_EXISTS columnReference NOT ) | ( ( columnReference )=> columnReference | multiValuedColumnReference ) IS NOT NULL -> ^( PRED_EXISTS columnReference ) );";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA34_22 = input.LA(1);

                         
                        int index34_22 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA34_22==DOUBLE_QUOTE) && ((strict == false))) {s = 23;}

                         
                        input.seek(index34_22);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA34_0 = input.LA(1);

                         
                        int index34_0 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA34_0==ID) ) {s = 1;}

                        else if ( (LA34_0==DOUBLE_QUOTE) && ((strict == false))) {s = 2;}

                         
                        input.seek(index34_0);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA34_6 = input.LA(1);

                         
                        int index34_6 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA34_6==DOUBLE_QUOTE) && ((strict == false))) {s = 11;}

                         
                        input.seek(index34_6);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA34_17 = input.LA(1);

                         
                        int index34_17 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA34_17==IS) && ((strict == false))) {s = 15;}

                         
                        input.seek(index34_17);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA34_18 = input.LA(1);

                         
                        int index34_18 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA34_18==SELECT||LA34_18==AS||(LA34_18>=FROM && LA34_18<=ON)||(LA34_18>=WHERE && LA34_18<=NOT)||(LA34_18>=IN && LA34_18<=DESC)||(LA34_18>=TRUE && LA34_18<=TIMESTAMP)) && ((strict == false))) {s = 21;}

                        else if ( (LA34_18==ID) && ((strict == false))) {s = 22;}

                         
                        input.seek(index34_18);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA34_15 = input.LA(1);

                         
                        int index34_15 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA34_15==NOT) && ((strict == false))) {s = 19;}

                        else if ( (LA34_15==NULL) && ((strict == false))) {s = 20;}

                         
                        input.seek(index34_15);
                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA34_23 = input.LA(1);

                         
                        int index34_23 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA34_23==IS) && ((strict == false))) {s = 15;}

                         
                        input.seek(index34_23);
                        if ( s>=0 ) return s;
                        break;
                    case 7 : 
                        int LA34_21 = input.LA(1);

                         
                        int index34_21 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA34_21==DOUBLE_QUOTE) && ((strict == false))) {s = 23;}

                         
                        input.seek(index34_21);
                        if ( s>=0 ) return s;
                        break;
                    case 8 : 
                        int LA34_5 = input.LA(1);

                         
                        int index34_5 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA34_5==DOUBLE_QUOTE) && ((strict == false))) {s = 11;}

                         
                        input.seek(index34_5);
                        if ( s>=0 ) return s;
                        break;
                    case 9 : 
                        int LA34_2 = input.LA(1);

                         
                        int index34_2 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA34_2==SELECT||LA34_2==AS||(LA34_2>=FROM && LA34_2<=ON)||(LA34_2>=WHERE && LA34_2<=NOT)||(LA34_2>=IN && LA34_2<=DESC)||(LA34_2>=TRUE && LA34_2<=TIMESTAMP)) && ((strict == false))) {s = 5;}

                        else if ( (LA34_2==ID) && ((strict == false))) {s = 6;}

                         
                        input.seek(index34_2);
                        if ( s>=0 ) return s;
                        break;
                    case 10 : 
                        int LA34_14 = input.LA(1);

                         
                        int index34_14 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA34_14==ID) && ((strict == false))) {s = 17;}

                        else if ( (LA34_14==DOUBLE_QUOTE) && ((strict == false))) {s = 18;}

                         
                        input.seek(index34_14);
                        if ( s>=0 ) return s;
                        break;
                    case 11 : 
                        int LA34_11 = input.LA(1);

                         
                        int index34_11 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA34_11==DOT) && ((strict == false))) {s = 14;}

                        else if ( (LA34_11==IS) && ((strict == false))) {s = 15;}

                         
                        input.seek(index34_11);
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
    static final String DFA41_eotS =
        "\32\uffff";
    static final String DFA41_eofS =
        "\1\uffff\1\5\6\uffff\1\5\1\uffff\1\16\5\uffff\1\23\1\26\7\uffff"+
        "\1\26";
    static final String DFA41_minS =
        "\1\105\1\41\1\37\1\uffff\1\105\1\uffff\2\106\1\41\1\37\1\41\2\106"+
        "\1\105\2\uffff\2\41\1\37\4\uffff\2\106\1\41";
    static final String DFA41_maxS =
        "\1\106\1\102\1\113\1\uffff\1\106\1\uffff\2\106\1\102\1\113\1\102"+
        "\3\106\2\uffff\2\102\1\113\4\uffff\2\106\1\102";
    static final String DFA41_acceptS =
        "\3\uffff\1\2\1\uffff\1\1\10\uffff\1\1\1\2\3\uffff\1\1\2\2\1\1\3"+
        "\uffff";
    static final String DFA41_specialS =
        "\1\10\1\uffff\1\6\3\uffff\1\12\1\7\2\uffff\1\5\2\uffff\1\11\3\uffff"+
        "\1\2\1\3\4\uffff\1\4\1\1\1\0}>";
    static final String[] DFA41_transitionS = {
            "\1\1\1\2",
            "\1\5\2\uffff\1\4\34\uffff\2\3",
            "\1\6\2\uffff\1\6\4\uffff\6\6\1\uffff\4\6\5\uffff\14\6\2\uffff"+
            "\1\7\3\uffff\3\6",
            "",
            "\1\10\1\11",
            "",
            "\1\12",
            "\1\12",
            "\1\5\37\uffff\2\3",
            "\1\13\2\uffff\1\13\4\uffff\6\13\1\uffff\4\13\5\uffff\14\13"+
            "\2\uffff\1\14\3\uffff\3\13",
            "\1\16\2\uffff\1\15\34\uffff\2\17",
            "\1\20",
            "\1\20",
            "\1\21\1\22",
            "",
            "",
            "\1\5\37\uffff\1\3\1\24",
            "\1\16\37\uffff\1\17\1\25",
            "\1\27\2\uffff\1\27\4\uffff\6\27\1\uffff\4\27\5\uffff\14\27"+
            "\2\uffff\1\30\3\uffff\3\27",
            "",
            "",
            "",
            "",
            "\1\31",
            "\1\31",
            "\1\16\37\uffff\1\17\1\25"
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
            return "403:1: sortSpecification : ( columnReference -> ^( SORT_SPECIFICATION columnReference ASC ) | columnReference (by= ASC | by= DESC ) -> ^( SORT_SPECIFICATION columnReference $by) );";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA41_25 = input.LA(1);

                         
                        int index41_25 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA41_25==COMMA) && ((strict == false))) {s = 14;}

                        else if ( (LA41_25==EOF) && ((strict == false))) {s = 22;}

                        else if ( (LA41_25==ASC) && ((strict == false))) {s = 15;}

                        else if ( (LA41_25==DESC) && ((strict == false))) {s = 21;}

                         
                        input.seek(index41_25);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA41_24 = input.LA(1);

                         
                        int index41_24 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA41_24==DOUBLE_QUOTE) && ((strict == false))) {s = 25;}

                         
                        input.seek(index41_24);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA41_17 = input.LA(1);

                         
                        int index41_17 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA41_17==ASC) && ((strict == false))) {s = 15;}

                        else if ( (LA41_17==DESC) && ((strict == false))) {s = 21;}

                        else if ( (LA41_17==COMMA) && ((strict == false))) {s = 14;}

                        else if ( (LA41_17==EOF) && ((strict == false))) {s = 22;}

                         
                        input.seek(index41_17);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA41_18 = input.LA(1);

                         
                        int index41_18 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA41_18==SELECT||LA41_18==AS||(LA41_18>=FROM && LA41_18<=ON)||(LA41_18>=WHERE && LA41_18<=NOT)||(LA41_18>=IN && LA41_18<=DESC)||(LA41_18>=TRUE && LA41_18<=TIMESTAMP)) && ((strict == false))) {s = 23;}

                        else if ( (LA41_18==ID) && ((strict == false))) {s = 24;}

                         
                        input.seek(index41_18);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA41_23 = input.LA(1);

                         
                        int index41_23 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA41_23==DOUBLE_QUOTE) && ((strict == false))) {s = 25;}

                         
                        input.seek(index41_23);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA41_10 = input.LA(1);

                         
                        int index41_10 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA41_10==DOT) && ((strict == false))) {s = 13;}

                        else if ( (LA41_10==EOF||LA41_10==COMMA) && ((strict == false))) {s = 14;}

                        else if ( ((LA41_10>=ASC && LA41_10<=DESC)) && ((strict == false))) {s = 15;}

                         
                        input.seek(index41_10);
                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA41_2 = input.LA(1);

                         
                        int index41_2 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA41_2==SELECT||LA41_2==AS||(LA41_2>=FROM && LA41_2<=ON)||(LA41_2>=WHERE && LA41_2<=NOT)||(LA41_2>=IN && LA41_2<=DESC)||(LA41_2>=TRUE && LA41_2<=TIMESTAMP)) && ((strict == false))) {s = 6;}

                        else if ( (LA41_2==ID) && ((strict == false))) {s = 7;}

                         
                        input.seek(index41_2);
                        if ( s>=0 ) return s;
                        break;
                    case 7 : 
                        int LA41_7 = input.LA(1);

                         
                        int index41_7 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA41_7==DOUBLE_QUOTE) && ((strict == false))) {s = 10;}

                         
                        input.seek(index41_7);
                        if ( s>=0 ) return s;
                        break;
                    case 8 : 
                        int LA41_0 = input.LA(1);

                         
                        int index41_0 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA41_0==ID) ) {s = 1;}

                        else if ( (LA41_0==DOUBLE_QUOTE) && ((strict == false))) {s = 2;}

                         
                        input.seek(index41_0);
                        if ( s>=0 ) return s;
                        break;
                    case 9 : 
                        int LA41_13 = input.LA(1);

                         
                        int index41_13 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA41_13==ID) && ((strict == false))) {s = 17;}

                        else if ( (LA41_13==DOUBLE_QUOTE) && ((strict == false))) {s = 18;}

                         
                        input.seek(index41_13);
                        if ( s>=0 ) return s;
                        break;
                    case 10 : 
                        int LA41_6 = input.LA(1);

                         
                        int index41_6 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA41_6==DOUBLE_QUOTE) && ((strict == false))) {s = 10;}

                         
                        input.seek(index41_6);
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
 

    public static final BitSet FOLLOW_SELECT_in_query180 = new BitSet(new long[]{0xFF83DF8580000000L,0x0000000000000E67L});
    public static final BitSet FOLLOW_selectList_in_query182 = new BitSet(new long[]{0x0000008000000000L});
    public static final BitSet FOLLOW_fromClause_in_query184 = new BitSet(new long[]{0x8000400000000000L});
    public static final BitSet FOLLOW_whereClause_in_query186 = new BitSet(new long[]{0x8000000000000000L});
    public static final BitSet FOLLOW_orderByClause_in_query189 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_query192 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STAR_in_selectList241 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_selectSubList_in_selectList257 = new BitSet(new long[]{0x0000000200000002L});
    public static final BitSet FOLLOW_COMMA_in_selectList261 = new BitSet(new long[]{0xFF83DF8580000000L,0x0000000000000E67L});
    public static final BitSet FOLLOW_selectSubList_in_selectList263 = new BitSet(new long[]{0x0000000200000002L});
    public static final BitSet FOLLOW_valueExpression_in_selectSubList299 = new BitSet(new long[]{0x0000000400000002L,0x0000000000000060L});
    public static final BitSet FOLLOW_AS_in_selectSubList303 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000060L});
    public static final BitSet FOLLOW_columnName_in_selectSubList306 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_qualifier_in_selectSubList327 = new BitSet(new long[]{0x0000000800000000L});
    public static final BitSet FOLLOW_DOTSTAR_in_selectSubList329 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_multiValuedColumnReference_in_selectSubList345 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_columnReference_in_valueExpression364 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_valueFunction_in_valueExpression377 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_qualifier_in_columnReference400 = new BitSet(new long[]{0x0000001000000000L});
    public static final BitSet FOLLOW_DOT_in_columnReference402 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000060L});
    public static final BitSet FOLLOW_columnName_in_columnReference407 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_qualifier_in_multiValuedColumnReference443 = new BitSet(new long[]{0x0000001000000000L});
    public static final BitSet FOLLOW_DOT_in_multiValuedColumnReference445 = new BitSet(new long[]{0xFF83DF8580000000L,0x0000000000000E67L});
    public static final BitSet FOLLOW_multiValuedColumnName_in_multiValuedColumnReference451 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_keyWordOrId_in_valueFunction478 = new BitSet(new long[]{0x0000002000000000L});
    public static final BitSet FOLLOW_LPAREN_in_valueFunction480 = new BitSet(new long[]{0x0000004000000000L,0x0000000000000FF8L});
    public static final BitSet FOLLOW_functionArgument_in_valueFunction482 = new BitSet(new long[]{0x0000004000000000L,0x0000000000000FF8L});
    public static final BitSet FOLLOW_RPAREN_in_valueFunction485 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_qualifier_in_functionArgument520 = new BitSet(new long[]{0x0000001000000000L});
    public static final BitSet FOLLOW_DOT_in_functionArgument522 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000060L});
    public static final BitSet FOLLOW_columnName_in_functionArgument524 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_identifier_in_functionArgument548 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_literalOrParameterName_in_functionArgument558 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_tableName_in_qualifier579 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_correlationName_in_qualifier591 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FROM_in_fromClause628 = new BitSet(new long[]{0x0000002000000000L,0x0000000000000060L});
    public static final BitSet FOLLOW_tableReference_in_fromClause630 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_singleTable_in_tableReference648 = new BitSet(new long[]{0x0000070000000002L});
    public static final BitSet FOLLOW_joinedTable_in_tableReference657 = new BitSet(new long[]{0x0000070000000002L});
    public static final BitSet FOLLOW_tableName_in_singleTable686 = new BitSet(new long[]{0x0000000400000002L,0x0000000000000060L});
    public static final BitSet FOLLOW_AS_in_singleTable690 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000060L});
    public static final BitSet FOLLOW_correlationName_in_singleTable693 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_singleTable714 = new BitSet(new long[]{0x0000002000000000L,0x0000000000000060L});
    public static final BitSet FOLLOW_joinedTables_in_singleTable716 = new BitSet(new long[]{0x0000004000000000L});
    public static final BitSet FOLLOW_RPAREN_in_singleTable718 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_joinType_in_joinedTable740 = new BitSet(new long[]{0x0000010000000000L});
    public static final BitSet FOLLOW_JOIN_in_joinedTable743 = new BitSet(new long[]{0x0000002000000000L,0x0000000000000060L});
    public static final BitSet FOLLOW_tableReference_in_joinedTable745 = new BitSet(new long[]{0x0000100000000000L});
    public static final BitSet FOLLOW_joinSpecification_in_joinedTable753 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_singleTable_in_joinedTables781 = new BitSet(new long[]{0x0000070000000000L});
    public static final BitSet FOLLOW_joinedTable_in_joinedTables783 = new BitSet(new long[]{0x0000070000000002L});
    public static final BitSet FOLLOW_INNER_in_joinType810 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LEFT_in_joinType822 = new BitSet(new long[]{0x0000080000000002L});
    public static final BitSet FOLLOW_OUTER_in_joinType824 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ON_in_joinSpecification844 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000060L});
    public static final BitSet FOLLOW_columnReference_in_joinSpecification848 = new BitSet(new long[]{0x0000200000000000L});
    public static final BitSet FOLLOW_EQUALS_in_joinSpecification850 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000060L});
    public static final BitSet FOLLOW_columnReference_in_joinSpecification854 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_WHERE_in_whereClause905 = new BitSet(new long[]{0xFF83DFA580000000L,0x0000000000000FFFL});
    public static final BitSet FOLLOW_searchOrCondition_in_whereClause907 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_searchAndCondition_in_searchOrCondition927 = new BitSet(new long[]{0x0000800000000002L});
    public static final BitSet FOLLOW_OR_in_searchOrCondition930 = new BitSet(new long[]{0xFF83DFA580000000L,0x0000000000000FFFL});
    public static final BitSet FOLLOW_searchAndCondition_in_searchOrCondition932 = new BitSet(new long[]{0x0000800000000002L});
    public static final BitSet FOLLOW_searchNotCondition_in_searchAndCondition960 = new BitSet(new long[]{0x0001000000000002L});
    public static final BitSet FOLLOW_AND_in_searchAndCondition963 = new BitSet(new long[]{0xFF83DFA580000000L,0x0000000000000FFFL});
    public static final BitSet FOLLOW_searchNotCondition_in_searchAndCondition965 = new BitSet(new long[]{0x0001000000000002L});
    public static final BitSet FOLLOW_NOT_in_searchNotCondition992 = new BitSet(new long[]{0xFF83DFA580000000L,0x0000000000000FFFL});
    public static final BitSet FOLLOW_searchTest_in_searchNotCondition994 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_searchTest_in_searchNotCondition1009 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_predicate_in_searchTest1027 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_searchTest1038 = new BitSet(new long[]{0xFF83DFA580000000L,0x0000000000000FFFL});
    public static final BitSet FOLLOW_searchOrCondition_in_searchTest1040 = new BitSet(new long[]{0x0000004000000000L});
    public static final BitSet FOLLOW_RPAREN_in_searchTest1042 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_comparisonPredicate_in_predicate1059 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_inPredicate_in_predicate1064 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_likePredicate_in_predicate1069 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_nullPredicate_in_predicate1074 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_quantifiedComparisonPredicate_in_predicate1085 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_quantifiedInPredicate_in_predicate1090 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_textSearchPredicate_in_predicate1095 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_folderPredicate_in_predicate1100 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_valueExpression_in_comparisonPredicate1112 = new BitSet(new long[]{0x007C200000000000L});
    public static final BitSet FOLLOW_compOp_in_comparisonPredicate1114 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000FF8L});
    public static final BitSet FOLLOW_literalOrParameterName_in_comparisonPredicate1116 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_compOp0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_literal_in_literalOrParameterName1182 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_parameterName_in_literalOrParameterName1190 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_signedNumericLiteral_in_literal1203 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_characterStringLiteral_in_literal1208 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_booleanLiteral_in_literal1213 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_datetimeLiteral_in_literal1218 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_columnReference_in_inPredicate1230 = new BitSet(new long[]{0x0082000000000000L});
    public static final BitSet FOLLOW_NOT_in_inPredicate1232 = new BitSet(new long[]{0x0080000000000000L});
    public static final BitSet FOLLOW_IN_in_inPredicate1235 = new BitSet(new long[]{0x0000002000000000L});
    public static final BitSet FOLLOW_LPAREN_in_inPredicate1237 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000FF8L});
    public static final BitSet FOLLOW_inValueList_in_inPredicate1239 = new BitSet(new long[]{0x0000004000000000L});
    public static final BitSet FOLLOW_RPAREN_in_inPredicate1241 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_literalOrParameterName_in_inValueList1270 = new BitSet(new long[]{0x0000000200000002L});
    public static final BitSet FOLLOW_COMMA_in_inValueList1273 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000FF8L});
    public static final BitSet FOLLOW_literalOrParameterName_in_inValueList1275 = new BitSet(new long[]{0x0000000200000002L});
    public static final BitSet FOLLOW_columnReference_in_likePredicate1301 = new BitSet(new long[]{0x0102000000000000L});
    public static final BitSet FOLLOW_NOT_in_likePredicate1303 = new BitSet(new long[]{0x0100000000000000L});
    public static final BitSet FOLLOW_LIKE_in_likePredicate1306 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000010L});
    public static final BitSet FOLLOW_characterStringLiteral_in_likePredicate1308 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_columnReference_in_nullPredicate1342 = new BitSet(new long[]{0x0200000000000000L});
    public static final BitSet FOLLOW_multiValuedColumnReference_in_nullPredicate1346 = new BitSet(new long[]{0x0200000000000000L});
    public static final BitSet FOLLOW_IS_in_nullPredicate1349 = new BitSet(new long[]{0x0400000000000000L});
    public static final BitSet FOLLOW_NULL_in_nullPredicate1351 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_columnReference_in_nullPredicate1380 = new BitSet(new long[]{0x0200000000000000L});
    public static final BitSet FOLLOW_multiValuedColumnReference_in_nullPredicate1384 = new BitSet(new long[]{0x0200000000000000L});
    public static final BitSet FOLLOW_IS_in_nullPredicate1387 = new BitSet(new long[]{0x0002000000000000L});
    public static final BitSet FOLLOW_NOT_in_nullPredicate1389 = new BitSet(new long[]{0x0400000000000000L});
    public static final BitSet FOLLOW_NULL_in_nullPredicate1391 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_literalOrParameterName_in_quantifiedComparisonPredicate1419 = new BitSet(new long[]{0x007C200000000000L});
    public static final BitSet FOLLOW_compOp_in_quantifiedComparisonPredicate1421 = new BitSet(new long[]{0x0800000000000000L});
    public static final BitSet FOLLOW_ANY_in_quantifiedComparisonPredicate1423 = new BitSet(new long[]{0xFF83DF8580000000L,0x0000000000000E67L});
    public static final BitSet FOLLOW_multiValuedColumnReference_in_quantifiedComparisonPredicate1425 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ANY_in_quantifiedInPredicate1454 = new BitSet(new long[]{0xFF83DF8580000000L,0x0000000000000E67L});
    public static final BitSet FOLLOW_multiValuedColumnReference_in_quantifiedInPredicate1456 = new BitSet(new long[]{0x0082000000000000L});
    public static final BitSet FOLLOW_NOT_in_quantifiedInPredicate1458 = new BitSet(new long[]{0x0080000000000000L});
    public static final BitSet FOLLOW_IN_in_quantifiedInPredicate1461 = new BitSet(new long[]{0x0000002000000000L});
    public static final BitSet FOLLOW_LPAREN_in_quantifiedInPredicate1464 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000FF8L});
    public static final BitSet FOLLOW_inValueList_in_quantifiedInPredicate1466 = new BitSet(new long[]{0x0000004000000000L});
    public static final BitSet FOLLOW_RPAREN_in_quantifiedInPredicate1468 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_CONTAINS_in_textSearchPredicate1497 = new BitSet(new long[]{0x0000002000000000L});
    public static final BitSet FOLLOW_LPAREN_in_textSearchPredicate1499 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000070L});
    public static final BitSet FOLLOW_qualifier_in_textSearchPredicate1502 = new BitSet(new long[]{0x0000000200000000L});
    public static final BitSet FOLLOW_COMMA_in_textSearchPredicate1504 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000070L});
    public static final BitSet FOLLOW_textSearchExpression_in_textSearchPredicate1508 = new BitSet(new long[]{0x0000004000000000L});
    public static final BitSet FOLLOW_RPAREN_in_textSearchPredicate1510 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IN_FOLDER_in_folderPredicate1535 = new BitSet(new long[]{0x0000002000000000L});
    public static final BitSet FOLLOW_folderPredicateArgs_in_folderPredicate1538 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IN_TREE_in_folderPredicate1559 = new BitSet(new long[]{0x0000002000000000L});
    public static final BitSet FOLLOW_folderPredicateArgs_in_folderPredicate1561 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_folderPredicateArgs1583 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000070L});
    public static final BitSet FOLLOW_qualifier_in_folderPredicateArgs1586 = new BitSet(new long[]{0x0000000200000000L});
    public static final BitSet FOLLOW_COMMA_in_folderPredicateArgs1588 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000070L});
    public static final BitSet FOLLOW_folderId_in_folderPredicateArgs1592 = new BitSet(new long[]{0x0000004000000000L});
    public static final BitSet FOLLOW_RPAREN_in_folderPredicateArgs1594 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ORDER_in_orderByClause1633 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000001L});
    public static final BitSet FOLLOW_BY_in_orderByClause1635 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000060L});
    public static final BitSet FOLLOW_sortSpecification_in_orderByClause1637 = new BitSet(new long[]{0x0000000200000002L});
    public static final BitSet FOLLOW_COMMA_in_orderByClause1641 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000060L});
    public static final BitSet FOLLOW_sortSpecification_in_orderByClause1643 = new BitSet(new long[]{0x0000000200000002L});
    public static final BitSet FOLLOW_columnReference_in_sortSpecification1669 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_columnReference_in_sortSpecification1687 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000006L});
    public static final BitSet FOLLOW_ASC_in_sortSpecification1693 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DESC_in_sortSpecification1699 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_identifier_in_correlationName1726 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_identifier_in_tableName1740 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_identifier_in_columnName1758 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_identifier_in_multiValuedColumnName1777 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_COLON_in_parameterName1795 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000060L});
    public static final BitSet FOLLOW_identifier_in_parameterName1797 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_characterStringLiteral_in_folderId1820 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_QUOTED_STRING_in_textSearchExpression1841 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_identifier1853 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DOUBLE_QUOTE_in_identifier1868 = new BitSet(new long[]{0xFF83DF8480000000L,0x0000000000000E67L});
    public static final BitSet FOLLOW_keyWordOrId_in_identifier1870 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000040L});
    public static final BitSet FOLLOW_DOUBLE_QUOTE_in_identifier1872 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FLOATING_POINT_LITERAL_in_signedNumericLiteral1892 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_integerLiteral_in_signedNumericLiteral1907 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DECIMAL_INTEGER_LITERAL_in_integerLiteral1926 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TRUE_in_booleanLiteral1950 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FALSE_in_booleanLiteral1968 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TIMESTAMP_in_datetimeLiteral1993 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000010L});
    public static final BitSet FOLLOW_QUOTED_STRING_in_datetimeLiteral1995 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_QUOTED_STRING_in_characterStringLiteral2018 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_keyWord0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_keyWord_in_keyWordOrId2208 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_keyWordOrId2220 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_valueExpression_in_synpred1_CMIS295 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_tableName_in_synpred2_CMIS574 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_joinedTable_in_synpred3_CMIS652 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_columnReference_in_synpred5_CMIS1338 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_columnReference_in_synpred6_CMIS1376 = new BitSet(new long[]{0x0000000000000002L});

}