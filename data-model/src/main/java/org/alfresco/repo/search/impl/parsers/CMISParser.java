/*
 * #%L
 * Alfresco Data model classes
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
 * 
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
// $ANTLR 3.5.2 W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g 2015-06-18 19:38:02

package org.alfresco.repo.search.impl.parsers;

import java.util.List;
import java.util.Stack;

import org.antlr.runtime.*;
import org.antlr.runtime.tree.*;
import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;

@SuppressWarnings("all")
public class CMISParser extends Parser
{
    public static final String[] tokenNames = new String[]{
            "<invalid>", "<EOR>", "<DOWN>", "<UP>", "ALL_COLUMNS", "AND", "ANY", "AS",
            "ASC", "BOOLEAN_LITERAL", "BY", "COLON", "COLUMN", "COLUMNS", "COLUMN_REF",
            "COMMA", "CONJUNCTION", "CONTAINS", "DATETIME_LITERAL", "DECIMAL_INTEGER_LITERAL",
            "DECIMAL_NUMERAL", "DESC", "DIGIT", "DISJUNCTION", "DOT", "DOTDOT", "DOTSTAR",
            "DOUBLE_QUOTE", "E", "EQUALS", "EXPONENT", "FALSE", "FLOATING_POINT_LITERAL",
            "FROM", "FUNCTION", "GREATERTHAN", "GREATERTHANOREQUALS", "ID", "IN",
            "INNER", "IN_FOLDER", "IN_TREE", "IS", "JOIN", "LEFT", "LESSTHAN", "LESSTHANOREQUALS",
            "LIKE", "LIST", "LPAREN", "MINUS", "NEGATION", "NON_ZERO_DIGIT", "NOT",
            "NOTEQUALS", "NULL", "NUMERIC_LITERAL", "ON", "OR", "ORDER", "OUTER",
            "PARAMETER", "PLUS", "PRED_CHILD", "PRED_COMPARISON", "PRED_DESCENDANT",
            "PRED_EXISTS", "PRED_FTS", "PRED_IN", "PRED_LIKE", "QUALIFIER", "QUERY",
            "QUOTED_STRING", "RPAREN", "SCORE", "SELECT", "SIGNED_INTEGER", "SINGLE_VALUED_PROPERTY",
            "SORT_SPECIFICATION", "SOURCE", "STAR", "STRING_LITERAL", "TABLE", "TABLE_REF",
            "TILDA", "TIMESTAMP", "TRUE", "WHERE", "WS", "ZERO_DIGIT"
    };
    public static final int EOF = -1;
    public static final int ALL_COLUMNS = 4;
    public static final int AND = 5;
    public static final int ANY = 6;
    public static final int AS = 7;
    public static final int ASC = 8;
    public static final int BOOLEAN_LITERAL = 9;
    public static final int BY = 10;
    public static final int COLON = 11;
    public static final int COLUMN = 12;
    public static final int COLUMNS = 13;
    public static final int COLUMN_REF = 14;
    public static final int COMMA = 15;
    public static final int CONJUNCTION = 16;
    public static final int CONTAINS = 17;
    public static final int DATETIME_LITERAL = 18;
    public static final int DECIMAL_INTEGER_LITERAL = 19;
    public static final int DECIMAL_NUMERAL = 20;
    public static final int DESC = 21;
    public static final int DIGIT = 22;
    public static final int DISJUNCTION = 23;
    public static final int DOT = 24;
    public static final int DOTDOT = 25;
    public static final int DOTSTAR = 26;
    public static final int DOUBLE_QUOTE = 27;
    public static final int E = 28;
    public static final int EQUALS = 29;
    public static final int EXPONENT = 30;
    public static final int FALSE = 31;
    public static final int FLOATING_POINT_LITERAL = 32;
    public static final int FROM = 33;
    public static final int FUNCTION = 34;
    public static final int GREATERTHAN = 35;
    public static final int GREATERTHANOREQUALS = 36;
    public static final int ID = 37;
    public static final int IN = 38;
    public static final int INNER = 39;
    public static final int IN_FOLDER = 40;
    public static final int IN_TREE = 41;
    public static final int IS = 42;
    public static final int JOIN = 43;
    public static final int LEFT = 44;
    public static final int LESSTHAN = 45;
    public static final int LESSTHANOREQUALS = 46;
    public static final int LIKE = 47;
    public static final int LIST = 48;
    public static final int LPAREN = 49;
    public static final int MINUS = 50;
    public static final int NEGATION = 51;
    public static final int NON_ZERO_DIGIT = 52;
    public static final int NOT = 53;
    public static final int NOTEQUALS = 54;
    public static final int NULL = 55;
    public static final int NUMERIC_LITERAL = 56;
    public static final int ON = 57;
    public static final int OR = 58;
    public static final int ORDER = 59;
    public static final int OUTER = 60;
    public static final int PARAMETER = 61;
    public static final int PLUS = 62;
    public static final int PRED_CHILD = 63;
    public static final int PRED_COMPARISON = 64;
    public static final int PRED_DESCENDANT = 65;
    public static final int PRED_EXISTS = 66;
    public static final int PRED_FTS = 67;
    public static final int PRED_IN = 68;
    public static final int PRED_LIKE = 69;
    public static final int QUALIFIER = 70;
    public static final int QUERY = 71;
    public static final int QUOTED_STRING = 72;
    public static final int RPAREN = 73;
    public static final int SCORE = 74;
    public static final int SELECT = 75;
    public static final int SIGNED_INTEGER = 76;
    public static final int SINGLE_VALUED_PROPERTY = 77;
    public static final int SORT_SPECIFICATION = 78;
    public static final int SOURCE = 79;
    public static final int STAR = 80;
    public static final int STRING_LITERAL = 81;
    public static final int TABLE = 82;
    public static final int TABLE_REF = 83;
    public static final int TILDA = 84;
    public static final int TIMESTAMP = 85;
    public static final int TRUE = 86;
    public static final int WHERE = 87;
    public static final int WS = 88;
    public static final int ZERO_DIGIT = 89;

    // delegates
    public Parser[] getDelegates()
    {
        return new Parser[]{};
    }

    // delegators

    public CMISParser(TokenStream input)
    {
        this(input, new RecognizerSharedState());
    }

    public CMISParser(TokenStream input, RecognizerSharedState state)
    {
        super(input, state);
    }

    protected TreeAdaptor adaptor = new CommonTreeAdaptor();

    public void setTreeAdaptor(TreeAdaptor adaptor)
    {
        this.adaptor = adaptor;
    }

    public TreeAdaptor getTreeAdaptor()
    {
        return adaptor;
    }

    @Override
    public String[] getTokenNames()
    {
        return CMISParser.tokenNames;
    }

    @Override
    public String getGrammarFileName()
    {
        return "W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g";
    }

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
        if (e instanceof UnwantedTokenException)
        {
            UnwantedTokenException ute = (UnwantedTokenException) e;
            String tokenName = "<unknown>";
            if (ute.expecting == Token.EOF)
            {
                tokenName = "EOF";
            }
            else
            {
                tokenName = tokenNames[ute.expecting];
            }
            msg = "extraneous input " + getTokenErrorDisplay(ute.getUnexpectedToken())
                    + " expecting " + tokenName;
        }
        else if (e instanceof MissingTokenException)
        {
            MissingTokenException mte = (MissingTokenException) e;
            String tokenName = "<unknown>";
            if (mte.expecting == Token.EOF)
            {
                tokenName = "EOF";
            }
            else
            {
                tokenName = tokenNames[mte.expecting];
            }
            msg = "missing " + tokenName + " at " + getTokenErrorDisplay(e.token)
                    + "  (" + getLongTokenErrorDisplay(e.token) + ")";
        }
        else if (e instanceof MismatchedTokenException)
        {
            MismatchedTokenException mte = (MismatchedTokenException) e;
            String tokenName = "<unknown>";
            if (mte.expecting == Token.EOF)
            {
                tokenName = "EOF";
            }
            else
            {
                tokenName = tokenNames[mte.expecting];
            }
            msg = "mismatched input " + getTokenErrorDisplay(e.token)
                    + " expecting " + tokenName + "  (" + getLongTokenErrorDisplay(e.token) + ")";
        }
        else if (e instanceof MismatchedTreeNodeException)
        {
            MismatchedTreeNodeException mtne = (MismatchedTreeNodeException) e;
            String tokenName = "<unknown>";
            if (mtne.expecting == Token.EOF)
            {
                tokenName = "EOF";
            }
            else
            {
                tokenName = tokenNames[mtne.expecting];
            }
            msg = "mismatched tree node: " + mtne.node + " expecting " + tokenName;
        }
        else if (e instanceof NoViableAltException)
        {
            NoViableAltException nvae = (NoViableAltException) e;
            msg = "no viable alternative at input " + getTokenErrorDisplay(e.token)
                    + "\n\t (decision=" + nvae.decisionNumber
                    + " state " + nvae.stateNumber + ")"
                    + " decision=<<" + nvae.grammarDecisionDescription + ">>";
        }
        else if (e instanceof EarlyExitException)
        {
            // EarlyExitException eee = (EarlyExitException)e;
            // for development, can add "(decision="+eee.decisionNumber+")"
            msg = "required (...)+ loop did not match anything at input " + getTokenErrorDisplay(e.token);
        }
        else if (e instanceof MismatchedSetException)
        {
            MismatchedSetException mse = (MismatchedSetException) e;
            msg = "mismatched input " + getTokenErrorDisplay(e.token)
                    + " expecting set " + mse.expecting;
        }
        else if (e instanceof MismatchedNotSetException)
        {
            MismatchedNotSetException mse = (MismatchedNotSetException) e;
            msg = "mismatched input " + getTokenErrorDisplay(e.token)
                    + " expecting set " + mse.expecting;
        }
        else if (e instanceof FailedPredicateException)
        {
            FailedPredicateException fpe = (FailedPredicateException) e;
            msg = "rule " + fpe.ruleName + " failed predicate: {" + fpe.predicateText + "}?";
        }

        if (paraphrases.size() > 0)
        {
            String paraphrase = (String) paraphrases.peek();
            msg = msg + " " + paraphrase;
        }
        return msg + "\n\t" + stack;
    }

    public String getLongTokenErrorDisplay(Token t)
    {
        return t.toString();
    }

    public String getErrorString(RecognitionException e)
    {
        String hdr = getErrorHeader(e);
        String msg = getErrorMessage(e, this.getTokenNames());
        return hdr + " " + msg;
    }

    public static class query_return extends ParserRuleReturnScope
    {
        Object tree;

        @Override
        public Object getTree()
        {
            return tree;
        }
    };

    // $ANTLR start "query"
    // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:285:1: query : SELECT selectList fromClause ( whereClause )? ( orderByClause )? EOF -> ^( QUERY selectList fromClause ( whereClause )? ( orderByClause )? ) ;
    public final CMISParser.query_return query() throws RecognitionException
    {
        CMISParser.query_return retval = new CMISParser.query_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token SELECT1 = null;
        Token EOF6 = null;
        ParserRuleReturnScope selectList2 = null;
        ParserRuleReturnScope fromClause3 = null;
        ParserRuleReturnScope whereClause4 = null;
        ParserRuleReturnScope orderByClause5 = null;

        Object SELECT1_tree = null;
        Object EOF6_tree = null;
        RewriteRuleTokenStream stream_EOF = new RewriteRuleTokenStream(adaptor, "token EOF");
        RewriteRuleTokenStream stream_SELECT = new RewriteRuleTokenStream(adaptor, "token SELECT");
        RewriteRuleSubtreeStream stream_whereClause = new RewriteRuleSubtreeStream(adaptor, "rule whereClause");
        RewriteRuleSubtreeStream stream_orderByClause = new RewriteRuleSubtreeStream(adaptor, "rule orderByClause");
        RewriteRuleSubtreeStream stream_selectList = new RewriteRuleSubtreeStream(adaptor, "rule selectList");
        RewriteRuleSubtreeStream stream_fromClause = new RewriteRuleSubtreeStream(adaptor, "rule fromClause");

        paraphrases.push("in query");

        try
        {
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:294:9: ( SELECT selectList fromClause ( whereClause )? ( orderByClause )? EOF -> ^( QUERY selectList fromClause ( whereClause )? ( orderByClause )? ) )
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:295:9: SELECT selectList fromClause ( whereClause )? ( orderByClause )? EOF
            {
                SELECT1 = (Token) match(input, SELECT, FOLLOW_SELECT_in_query415);
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                    stream_SELECT.add(SELECT1);

                pushFollow(FOLLOW_selectList_in_query417);
                selectList2 = selectList();
                state._fsp--;
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                    stream_selectList.add(selectList2.getTree());
                pushFollow(FOLLOW_fromClause_in_query419);
                fromClause3 = fromClause();
                state._fsp--;
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                    stream_fromClause.add(fromClause3.getTree());
                // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:295:38: ( whereClause )?
                int alt1 = 2;
                int LA1_0 = input.LA(1);
                if ((LA1_0 == WHERE))
                {
                    alt1 = 1;
                }
                switch (alt1)
                {
                case 1:
                // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:295:38: whereClause
                {
                    pushFollow(FOLLOW_whereClause_in_query421);
                    whereClause4 = whereClause();
                    state._fsp--;
                    if (state.failed)
                        return retval;
                    if (state.backtracking == 0)
                        stream_whereClause.add(whereClause4.getTree());
                }
                    break;

                }

                // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:295:51: ( orderByClause )?
                int alt2 = 2;
                int LA2_0 = input.LA(1);
                if ((LA2_0 == ORDER))
                {
                    alt2 = 1;
                }
                switch (alt2)
                {
                case 1:
                // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:295:51: orderByClause
                {
                    pushFollow(FOLLOW_orderByClause_in_query424);
                    orderByClause5 = orderByClause();
                    state._fsp--;
                    if (state.failed)
                        return retval;
                    if (state.backtracking == 0)
                        stream_orderByClause.add(orderByClause5.getTree());
                }
                    break;

                }

                EOF6 = (Token) match(input, EOF, FOLLOW_EOF_in_query427);
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                    stream_EOF.add(EOF6);

                // AST REWRITE
                // elements: orderByClause, fromClause, selectList, whereClause
                // token labels:
                // rule labels: retval
                // token list labels:
                // rule list labels:
                // wildcard labels:
                if (state.backtracking == 0)
                {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval = new RewriteRuleSubtreeStream(adaptor, "rule retval", retval != null ? retval.getTree() : null);

                    root_0 = (Object) adaptor.nil();
                    // 296:17: -> ^( QUERY selectList fromClause ( whereClause )? ( orderByClause )? )
                    {
                        // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:297:25: ^( QUERY selectList fromClause ( whereClause )? ( orderByClause )? )
                        {
                            Object root_1 = (Object) adaptor.nil();
                            root_1 = (Object) adaptor.becomeRoot((Object) adaptor.create(QUERY, "QUERY"), root_1);
                            adaptor.addChild(root_1, stream_selectList.nextTree());
                            adaptor.addChild(root_1, stream_fromClause.nextTree());
                            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:297:55: ( whereClause )?
                            if (stream_whereClause.hasNext())
                            {
                                adaptor.addChild(root_1, stream_whereClause.nextTree());
                            }
                            stream_whereClause.reset();

                            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:297:68: ( orderByClause )?
                            if (stream_orderByClause.hasNext())
                            {
                                adaptor.addChild(root_1, stream_orderByClause.nextTree());
                            }
                            stream_orderByClause.reset();

                            adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;
                }

            }

            retval.stop = input.LT(-1);

            if (state.backtracking == 0)
            {
                retval.tree = (Object) adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
            if (state.backtracking == 0)
            {
                paraphrases.pop();
            }
        }

        catch (RecognitionException e)
        {
            throw new CmisInvalidArgumentException(getErrorString(e), e);
        }

        finally
        {
            // do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "query"

    public static class selectList_return extends ParserRuleReturnScope
    {
        Object tree;

        @Override
        public Object getTree()
        {
            return tree;
        }
    };

    // $ANTLR start "selectList"
    // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:300:1: selectList : ( STAR -> ^( ALL_COLUMNS ) | selectSubList ( COMMA selectSubList )* -> ^( COLUMNS ( selectSubList )+ ) );
    public final CMISParser.selectList_return selectList() throws RecognitionException
    {
        CMISParser.selectList_return retval = new CMISParser.selectList_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token STAR7 = null;
        Token COMMA9 = null;
        ParserRuleReturnScope selectSubList8 = null;
        ParserRuleReturnScope selectSubList10 = null;

        Object STAR7_tree = null;
        Object COMMA9_tree = null;
        RewriteRuleTokenStream stream_STAR = new RewriteRuleTokenStream(adaptor, "token STAR");
        RewriteRuleTokenStream stream_COMMA = new RewriteRuleTokenStream(adaptor, "token COMMA");
        RewriteRuleSubtreeStream stream_selectSubList = new RewriteRuleSubtreeStream(adaptor, "rule selectSubList");

        paraphrases.push("in select list");

        try
        {
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:309:9: ( STAR -> ^( ALL_COLUMNS ) | selectSubList ( COMMA selectSubList )* -> ^( COLUMNS ( selectSubList )+ ) )
            int alt4 = 2;
            int LA4_0 = input.LA(1);
            if ((LA4_0 == STAR))
            {
                alt4 = 1;
            }
            else if ((LA4_0 == ID))
            {
                alt4 = 2;
            }
            else if ((LA4_0 == DOUBLE_QUOTE) && ((strict == false)))
            {
                alt4 = 2;
            }
            else if ((LA4_0 == SCORE))
            {
                alt4 = 2;
            }
            else if (((LA4_0 >= AND && LA4_0 <= ASC) || LA4_0 == BY || LA4_0 == CONTAINS || LA4_0 == DESC || LA4_0 == FALSE || LA4_0 == FROM || (LA4_0 >= IN && LA4_0 <= LEFT) || LA4_0 == LIKE || LA4_0 == NOT || LA4_0 == NULL || (LA4_0 >= ON && LA4_0 <= OUTER) || LA4_0 == SELECT || (LA4_0 >= TIMESTAMP && LA4_0 <= WHERE)) && ((strict == false)))
            {
                alt4 = 2;
            }

            switch (alt4)
            {
            case 1:
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:310:9: STAR
            {
                STAR7 = (Token) match(input, STAR, FOLLOW_STAR_in_selectList526);
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                    stream_STAR.add(STAR7);

                // AST REWRITE
                // elements:
                // token labels:
                // rule labels: retval
                // token list labels:
                // rule list labels:
                // wildcard labels:
                if (state.backtracking == 0)
                {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval = new RewriteRuleSubtreeStream(adaptor, "rule retval", retval != null ? retval.getTree() : null);

                    root_0 = (Object) adaptor.nil();
                    // 311:17: -> ^( ALL_COLUMNS )
                    {
                        // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:312:25: ^( ALL_COLUMNS )
                        {
                            Object root_1 = (Object) adaptor.nil();
                            root_1 = (Object) adaptor.becomeRoot((Object) adaptor.create(ALL_COLUMNS, "ALL_COLUMNS"), root_1);
                            adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;
                }

            }
                break;
            case 2:
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:313:11: selectSubList ( COMMA selectSubList )*
            {
                pushFollow(FOLLOW_selectSubList_in_selectList584);
                selectSubList8 = selectSubList();
                state._fsp--;
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                    stream_selectSubList.add(selectSubList8.getTree());
                // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:313:25: ( COMMA selectSubList )*
                loop3: while (true)
                {
                    int alt3 = 2;
                    int LA3_0 = input.LA(1);
                    if ((LA3_0 == COMMA))
                    {
                        alt3 = 1;
                    }

                    switch (alt3)
                    {
                    case 1:
                    // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:313:26: COMMA selectSubList
                    {
                        COMMA9 = (Token) match(input, COMMA, FOLLOW_COMMA_in_selectList587);
                        if (state.failed)
                            return retval;
                        if (state.backtracking == 0)
                            stream_COMMA.add(COMMA9);

                        pushFollow(FOLLOW_selectSubList_in_selectList589);
                        selectSubList10 = selectSubList();
                        state._fsp--;
                        if (state.failed)
                            return retval;
                        if (state.backtracking == 0)
                            stream_selectSubList.add(selectSubList10.getTree());
                    }
                        break;

                    default:
                        break loop3;
                    }
                }

                // AST REWRITE
                // elements: selectSubList
                // token labels:
                // rule labels: retval
                // token list labels:
                // rule list labels:
                // wildcard labels:
                if (state.backtracking == 0)
                {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval = new RewriteRuleSubtreeStream(adaptor, "rule retval", retval != null ? retval.getTree() : null);

                    root_0 = (Object) adaptor.nil();
                    // 314:17: -> ^( COLUMNS ( selectSubList )+ )
                    {
                        // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:315:25: ^( COLUMNS ( selectSubList )+ )
                        {
                            Object root_1 = (Object) adaptor.nil();
                            root_1 = (Object) adaptor.becomeRoot((Object) adaptor.create(COLUMNS, "COLUMNS"), root_1);
                            if (!(stream_selectSubList.hasNext()))
                            {
                                throw new RewriteEarlyExitException();
                            }
                            while (stream_selectSubList.hasNext())
                            {
                                adaptor.addChild(root_1, stream_selectSubList.nextTree());
                            }
                            stream_selectSubList.reset();

                            adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;
                }

            }
                break;

            }
            retval.stop = input.LT(-1);

            if (state.backtracking == 0)
            {
                retval.tree = (Object) adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
            if (state.backtracking == 0)
            {
                paraphrases.pop();
            }
        }

        catch (RecognitionException e)
        {
            throw new CmisInvalidArgumentException(getErrorString(e), e);
        }

        finally
        {
            // do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "selectList"

    public static class selectSubList_return extends ParserRuleReturnScope
    {
        Object tree;

        @Override
        public Object getTree()
        {
            return tree;
        }
    };

    // $ANTLR start "selectSubList"
    // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:318:1: selectSubList : ( valueExpression ( ( AS )? columnName )? -> ^( COLUMN valueExpression ( columnName )? ) | qualifier DOTSTAR -> ^( ALL_COLUMNS qualifier ) );
    public final CMISParser.selectSubList_return selectSubList() throws RecognitionException
    {
        CMISParser.selectSubList_return retval = new CMISParser.selectSubList_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token AS12 = null;
        Token DOTSTAR15 = null;
        ParserRuleReturnScope valueExpression11 = null;
        ParserRuleReturnScope columnName13 = null;
        ParserRuleReturnScope qualifier14 = null;

        Object AS12_tree = null;
        Object DOTSTAR15_tree = null;
        RewriteRuleTokenStream stream_AS = new RewriteRuleTokenStream(adaptor, "token AS");
        RewriteRuleTokenStream stream_DOTSTAR = new RewriteRuleTokenStream(adaptor, "token DOTSTAR");
        RewriteRuleSubtreeStream stream_valueExpression = new RewriteRuleSubtreeStream(adaptor, "rule valueExpression");
        RewriteRuleSubtreeStream stream_columnName = new RewriteRuleSubtreeStream(adaptor, "rule columnName");
        RewriteRuleSubtreeStream stream_qualifier = new RewriteRuleSubtreeStream(adaptor, "rule qualifier");

        try
        {
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:319:9: ( valueExpression ( ( AS )? columnName )? -> ^( COLUMN valueExpression ( columnName )? ) | qualifier DOTSTAR -> ^( ALL_COLUMNS qualifier ) )
            int alt7 = 2;
            int LA7_0 = input.LA(1);
            if ((LA7_0 == ID))
            {
                int LA7_1 = input.LA(2);
                if ((LA7_1 == AS || LA7_1 == COMMA || LA7_1 == DOT || LA7_1 == DOUBLE_QUOTE || LA7_1 == FROM || LA7_1 == ID))
                {
                    alt7 = 1;
                }
                else if ((LA7_1 == LPAREN) && ((strict == false)))
                {
                    alt7 = 1;
                }
                else if ((LA7_1 == DOTSTAR))
                {
                    alt7 = 2;
                }

                else
                {
                    if (state.backtracking > 0)
                    {
                        state.failed = true;
                        return retval;
                    }
                    int nvaeMark = input.mark();
                    try
                    {
                        input.consume();
                        NoViableAltException nvae = new NoViableAltException("", 7, 1, input);
                        throw nvae;
                    }
                    finally
                    {
                        input.rewind(nvaeMark);
                    }
                }

            }
            else if ((LA7_0 == DOUBLE_QUOTE) && ((strict == false)))
            {
                int LA7_2 = input.LA(2);
                if ((LA7_2 == SELECT) && ((strict == false)))
                {
                    int LA7_8 = input.LA(3);
                    if ((LA7_8 == DOUBLE_QUOTE) && ((strict == false)))
                    {
                        int LA7_37 = input.LA(4);
                        if ((LA7_37 == AS || LA7_37 == COMMA || LA7_37 == DOT || LA7_37 == DOUBLE_QUOTE || LA7_37 == FROM || LA7_37 == ID) && ((strict == false)))
                        {
                            alt7 = 1;
                        }
                        else if ((LA7_37 == DOTSTAR) && ((strict == false)))
                        {
                            alt7 = 2;
                        }

                    }

                }
                else if ((LA7_2 == AS) && ((strict == false)))
                {
                    int LA7_9 = input.LA(3);
                    if ((LA7_9 == DOUBLE_QUOTE) && ((strict == false)))
                    {
                        int LA7_37 = input.LA(4);
                        if ((LA7_37 == AS || LA7_37 == COMMA || LA7_37 == DOT || LA7_37 == DOUBLE_QUOTE || LA7_37 == FROM || LA7_37 == ID) && ((strict == false)))
                        {
                            alt7 = 1;
                        }
                        else if ((LA7_37 == DOTSTAR) && ((strict == false)))
                        {
                            alt7 = 2;
                        }

                    }

                }
                else if ((LA7_2 == FROM) && ((strict == false)))
                {
                    int LA7_10 = input.LA(3);
                    if ((LA7_10 == DOUBLE_QUOTE) && ((strict == false)))
                    {
                        int LA7_37 = input.LA(4);
                        if ((LA7_37 == AS || LA7_37 == COMMA || LA7_37 == DOT || LA7_37 == DOUBLE_QUOTE || LA7_37 == FROM || LA7_37 == ID) && ((strict == false)))
                        {
                            alt7 = 1;
                        }
                        else if ((LA7_37 == DOTSTAR) && ((strict == false)))
                        {
                            alt7 = 2;
                        }

                    }

                }
                else if ((LA7_2 == JOIN) && ((strict == false)))
                {
                    int LA7_11 = input.LA(3);
                    if ((LA7_11 == DOUBLE_QUOTE) && ((strict == false)))
                    {
                        int LA7_37 = input.LA(4);
                        if ((LA7_37 == AS || LA7_37 == COMMA || LA7_37 == DOT || LA7_37 == DOUBLE_QUOTE || LA7_37 == FROM || LA7_37 == ID) && ((strict == false)))
                        {
                            alt7 = 1;
                        }
                        else if ((LA7_37 == DOTSTAR) && ((strict == false)))
                        {
                            alt7 = 2;
                        }

                    }

                }
                else if ((LA7_2 == INNER) && ((strict == false)))
                {
                    int LA7_12 = input.LA(3);
                    if ((LA7_12 == DOUBLE_QUOTE) && ((strict == false)))
                    {
                        int LA7_37 = input.LA(4);
                        if ((LA7_37 == AS || LA7_37 == COMMA || LA7_37 == DOT || LA7_37 == DOUBLE_QUOTE || LA7_37 == FROM || LA7_37 == ID) && ((strict == false)))
                        {
                            alt7 = 1;
                        }
                        else if ((LA7_37 == DOTSTAR) && ((strict == false)))
                        {
                            alt7 = 2;
                        }

                    }

                }
                else if ((LA7_2 == LEFT) && ((strict == false)))
                {
                    int LA7_13 = input.LA(3);
                    if ((LA7_13 == DOUBLE_QUOTE) && ((strict == false)))
                    {
                        int LA7_37 = input.LA(4);
                        if ((LA7_37 == AS || LA7_37 == COMMA || LA7_37 == DOT || LA7_37 == DOUBLE_QUOTE || LA7_37 == FROM || LA7_37 == ID) && ((strict == false)))
                        {
                            alt7 = 1;
                        }
                        else if ((LA7_37 == DOTSTAR) && ((strict == false)))
                        {
                            alt7 = 2;
                        }

                    }

                }
                else if ((LA7_2 == OUTER) && ((strict == false)))
                {
                    int LA7_14 = input.LA(3);
                    if ((LA7_14 == DOUBLE_QUOTE) && ((strict == false)))
                    {
                        int LA7_37 = input.LA(4);
                        if ((LA7_37 == AS || LA7_37 == COMMA || LA7_37 == DOT || LA7_37 == DOUBLE_QUOTE || LA7_37 == FROM || LA7_37 == ID) && ((strict == false)))
                        {
                            alt7 = 1;
                        }
                        else if ((LA7_37 == DOTSTAR) && ((strict == false)))
                        {
                            alt7 = 2;
                        }

                    }

                }
                else if ((LA7_2 == ON) && ((strict == false)))
                {
                    int LA7_15 = input.LA(3);
                    if ((LA7_15 == DOUBLE_QUOTE) && ((strict == false)))
                    {
                        int LA7_37 = input.LA(4);
                        if ((LA7_37 == AS || LA7_37 == COMMA || LA7_37 == DOT || LA7_37 == DOUBLE_QUOTE || LA7_37 == FROM || LA7_37 == ID) && ((strict == false)))
                        {
                            alt7 = 1;
                        }
                        else if ((LA7_37 == DOTSTAR) && ((strict == false)))
                        {
                            alt7 = 2;
                        }

                    }

                }
                else if ((LA7_2 == WHERE) && ((strict == false)))
                {
                    int LA7_16 = input.LA(3);
                    if ((LA7_16 == DOUBLE_QUOTE) && ((strict == false)))
                    {
                        int LA7_37 = input.LA(4);
                        if ((LA7_37 == AS || LA7_37 == COMMA || LA7_37 == DOT || LA7_37 == DOUBLE_QUOTE || LA7_37 == FROM || LA7_37 == ID) && ((strict == false)))
                        {
                            alt7 = 1;
                        }
                        else if ((LA7_37 == DOTSTAR) && ((strict == false)))
                        {
                            alt7 = 2;
                        }

                    }

                }
                else if ((LA7_2 == OR) && ((strict == false)))
                {
                    int LA7_17 = input.LA(3);
                    if ((LA7_17 == DOUBLE_QUOTE) && ((strict == false)))
                    {
                        int LA7_37 = input.LA(4);
                        if ((LA7_37 == AS || LA7_37 == COMMA || LA7_37 == DOT || LA7_37 == DOUBLE_QUOTE || LA7_37 == FROM || LA7_37 == ID) && ((strict == false)))
                        {
                            alt7 = 1;
                        }
                        else if ((LA7_37 == DOTSTAR) && ((strict == false)))
                        {
                            alt7 = 2;
                        }

                    }

                }
                else if ((LA7_2 == AND) && ((strict == false)))
                {
                    int LA7_18 = input.LA(3);
                    if ((LA7_18 == DOUBLE_QUOTE) && ((strict == false)))
                    {
                        int LA7_37 = input.LA(4);
                        if ((LA7_37 == AS || LA7_37 == COMMA || LA7_37 == DOT || LA7_37 == DOUBLE_QUOTE || LA7_37 == FROM || LA7_37 == ID) && ((strict == false)))
                        {
                            alt7 = 1;
                        }
                        else if ((LA7_37 == DOTSTAR) && ((strict == false)))
                        {
                            alt7 = 2;
                        }

                    }

                }
                else if ((LA7_2 == NOT) && ((strict == false)))
                {
                    int LA7_19 = input.LA(3);
                    if ((LA7_19 == DOUBLE_QUOTE) && ((strict == false)))
                    {
                        int LA7_37 = input.LA(4);
                        if ((LA7_37 == AS || LA7_37 == COMMA || LA7_37 == DOT || LA7_37 == DOUBLE_QUOTE || LA7_37 == FROM || LA7_37 == ID) && ((strict == false)))
                        {
                            alt7 = 1;
                        }
                        else if ((LA7_37 == DOTSTAR) && ((strict == false)))
                        {
                            alt7 = 2;
                        }

                    }

                }
                else if ((LA7_2 == IN) && ((strict == false)))
                {
                    int LA7_20 = input.LA(3);
                    if ((LA7_20 == DOUBLE_QUOTE) && ((strict == false)))
                    {
                        int LA7_37 = input.LA(4);
                        if ((LA7_37 == AS || LA7_37 == COMMA || LA7_37 == DOT || LA7_37 == DOUBLE_QUOTE || LA7_37 == FROM || LA7_37 == ID) && ((strict == false)))
                        {
                            alt7 = 1;
                        }
                        else if ((LA7_37 == DOTSTAR) && ((strict == false)))
                        {
                            alt7 = 2;
                        }

                    }

                }
                else if ((LA7_2 == LIKE) && ((strict == false)))
                {
                    int LA7_21 = input.LA(3);
                    if ((LA7_21 == DOUBLE_QUOTE) && ((strict == false)))
                    {
                        int LA7_37 = input.LA(4);
                        if ((LA7_37 == AS || LA7_37 == COMMA || LA7_37 == DOT || LA7_37 == DOUBLE_QUOTE || LA7_37 == FROM || LA7_37 == ID) && ((strict == false)))
                        {
                            alt7 = 1;
                        }
                        else if ((LA7_37 == DOTSTAR) && ((strict == false)))
                        {
                            alt7 = 2;
                        }

                    }

                }
                else if ((LA7_2 == IS) && ((strict == false)))
                {
                    int LA7_22 = input.LA(3);
                    if ((LA7_22 == DOUBLE_QUOTE) && ((strict == false)))
                    {
                        int LA7_37 = input.LA(4);
                        if ((LA7_37 == AS || LA7_37 == COMMA || LA7_37 == DOT || LA7_37 == DOUBLE_QUOTE || LA7_37 == FROM || LA7_37 == ID) && ((strict == false)))
                        {
                            alt7 = 1;
                        }
                        else if ((LA7_37 == DOTSTAR) && ((strict == false)))
                        {
                            alt7 = 2;
                        }

                    }

                }
                else if ((LA7_2 == NULL) && ((strict == false)))
                {
                    int LA7_23 = input.LA(3);
                    if ((LA7_23 == DOUBLE_QUOTE) && ((strict == false)))
                    {
                        int LA7_37 = input.LA(4);
                        if ((LA7_37 == AS || LA7_37 == COMMA || LA7_37 == DOT || LA7_37 == DOUBLE_QUOTE || LA7_37 == FROM || LA7_37 == ID) && ((strict == false)))
                        {
                            alt7 = 1;
                        }
                        else if ((LA7_37 == DOTSTAR) && ((strict == false)))
                        {
                            alt7 = 2;
                        }

                    }

                }
                else if ((LA7_2 == ANY) && ((strict == false)))
                {
                    int LA7_24 = input.LA(3);
                    if ((LA7_24 == DOUBLE_QUOTE) && ((strict == false)))
                    {
                        int LA7_37 = input.LA(4);
                        if ((LA7_37 == AS || LA7_37 == COMMA || LA7_37 == DOT || LA7_37 == DOUBLE_QUOTE || LA7_37 == FROM || LA7_37 == ID) && ((strict == false)))
                        {
                            alt7 = 1;
                        }
                        else if ((LA7_37 == DOTSTAR) && ((strict == false)))
                        {
                            alt7 = 2;
                        }

                    }

                }
                else if ((LA7_2 == CONTAINS) && ((strict == false)))
                {
                    int LA7_25 = input.LA(3);
                    if ((LA7_25 == DOUBLE_QUOTE) && ((strict == false)))
                    {
                        int LA7_37 = input.LA(4);
                        if ((LA7_37 == AS || LA7_37 == COMMA || LA7_37 == DOT || LA7_37 == DOUBLE_QUOTE || LA7_37 == FROM || LA7_37 == ID) && ((strict == false)))
                        {
                            alt7 = 1;
                        }
                        else if ((LA7_37 == DOTSTAR) && ((strict == false)))
                        {
                            alt7 = 2;
                        }

                    }

                }
                else if ((LA7_2 == IN_FOLDER) && ((strict == false)))
                {
                    int LA7_26 = input.LA(3);
                    if ((LA7_26 == DOUBLE_QUOTE) && ((strict == false)))
                    {
                        int LA7_37 = input.LA(4);
                        if ((LA7_37 == AS || LA7_37 == COMMA || LA7_37 == DOT || LA7_37 == DOUBLE_QUOTE || LA7_37 == FROM || LA7_37 == ID) && ((strict == false)))
                        {
                            alt7 = 1;
                        }
                        else if ((LA7_37 == DOTSTAR) && ((strict == false)))
                        {
                            alt7 = 2;
                        }

                    }

                }
                else if ((LA7_2 == IN_TREE) && ((strict == false)))
                {
                    int LA7_27 = input.LA(3);
                    if ((LA7_27 == DOUBLE_QUOTE) && ((strict == false)))
                    {
                        int LA7_37 = input.LA(4);
                        if ((LA7_37 == AS || LA7_37 == COMMA || LA7_37 == DOT || LA7_37 == DOUBLE_QUOTE || LA7_37 == FROM || LA7_37 == ID) && ((strict == false)))
                        {
                            alt7 = 1;
                        }
                        else if ((LA7_37 == DOTSTAR) && ((strict == false)))
                        {
                            alt7 = 2;
                        }

                    }

                }
                else if ((LA7_2 == ORDER) && ((strict == false)))
                {
                    int LA7_28 = input.LA(3);
                    if ((LA7_28 == DOUBLE_QUOTE) && ((strict == false)))
                    {
                        int LA7_37 = input.LA(4);
                        if ((LA7_37 == AS || LA7_37 == COMMA || LA7_37 == DOT || LA7_37 == DOUBLE_QUOTE || LA7_37 == FROM || LA7_37 == ID) && ((strict == false)))
                        {
                            alt7 = 1;
                        }
                        else if ((LA7_37 == DOTSTAR) && ((strict == false)))
                        {
                            alt7 = 2;
                        }

                    }

                }
                else if ((LA7_2 == BY) && ((strict == false)))
                {
                    int LA7_29 = input.LA(3);
                    if ((LA7_29 == DOUBLE_QUOTE) && ((strict == false)))
                    {
                        int LA7_37 = input.LA(4);
                        if ((LA7_37 == AS || LA7_37 == COMMA || LA7_37 == DOT || LA7_37 == DOUBLE_QUOTE || LA7_37 == FROM || LA7_37 == ID) && ((strict == false)))
                        {
                            alt7 = 1;
                        }
                        else if ((LA7_37 == DOTSTAR) && ((strict == false)))
                        {
                            alt7 = 2;
                        }

                    }

                }
                else if ((LA7_2 == ASC) && ((strict == false)))
                {
                    int LA7_30 = input.LA(3);
                    if ((LA7_30 == DOUBLE_QUOTE) && ((strict == false)))
                    {
                        int LA7_37 = input.LA(4);
                        if ((LA7_37 == AS || LA7_37 == COMMA || LA7_37 == DOT || LA7_37 == DOUBLE_QUOTE || LA7_37 == FROM || LA7_37 == ID) && ((strict == false)))
                        {
                            alt7 = 1;
                        }
                        else if ((LA7_37 == DOTSTAR) && ((strict == false)))
                        {
                            alt7 = 2;
                        }

                    }

                }
                else if ((LA7_2 == DESC) && ((strict == false)))
                {
                    int LA7_31 = input.LA(3);
                    if ((LA7_31 == DOUBLE_QUOTE) && ((strict == false)))
                    {
                        int LA7_37 = input.LA(4);
                        if ((LA7_37 == AS || LA7_37 == COMMA || LA7_37 == DOT || LA7_37 == DOUBLE_QUOTE || LA7_37 == FROM || LA7_37 == ID) && ((strict == false)))
                        {
                            alt7 = 1;
                        }
                        else if ((LA7_37 == DOTSTAR) && ((strict == false)))
                        {
                            alt7 = 2;
                        }

                    }

                }
                else if ((LA7_2 == TIMESTAMP) && ((strict == false)))
                {
                    int LA7_32 = input.LA(3);
                    if ((LA7_32 == DOUBLE_QUOTE) && ((strict == false)))
                    {
                        int LA7_37 = input.LA(4);
                        if ((LA7_37 == AS || LA7_37 == COMMA || LA7_37 == DOT || LA7_37 == DOUBLE_QUOTE || LA7_37 == FROM || LA7_37 == ID) && ((strict == false)))
                        {
                            alt7 = 1;
                        }
                        else if ((LA7_37 == DOTSTAR) && ((strict == false)))
                        {
                            alt7 = 2;
                        }

                    }

                }
                else if ((LA7_2 == TRUE) && ((strict == false)))
                {
                    int LA7_33 = input.LA(3);
                    if ((LA7_33 == DOUBLE_QUOTE) && ((strict == false)))
                    {
                        int LA7_37 = input.LA(4);
                        if ((LA7_37 == AS || LA7_37 == COMMA || LA7_37 == DOT || LA7_37 == DOUBLE_QUOTE || LA7_37 == FROM || LA7_37 == ID) && ((strict == false)))
                        {
                            alt7 = 1;
                        }
                        else if ((LA7_37 == DOTSTAR) && ((strict == false)))
                        {
                            alt7 = 2;
                        }

                    }

                }
                else if ((LA7_2 == FALSE) && ((strict == false)))
                {
                    int LA7_34 = input.LA(3);
                    if ((LA7_34 == DOUBLE_QUOTE) && ((strict == false)))
                    {
                        int LA7_37 = input.LA(4);
                        if ((LA7_37 == AS || LA7_37 == COMMA || LA7_37 == DOT || LA7_37 == DOUBLE_QUOTE || LA7_37 == FROM || LA7_37 == ID) && ((strict == false)))
                        {
                            alt7 = 1;
                        }
                        else if ((LA7_37 == DOTSTAR) && ((strict == false)))
                        {
                            alt7 = 2;
                        }

                    }

                }
                else if ((LA7_2 == SCORE) && ((strict == false)))
                {
                    int LA7_35 = input.LA(3);
                    if ((LA7_35 == DOUBLE_QUOTE) && ((strict == false)))
                    {
                        int LA7_37 = input.LA(4);
                        if ((LA7_37 == AS || LA7_37 == COMMA || LA7_37 == DOT || LA7_37 == DOUBLE_QUOTE || LA7_37 == FROM || LA7_37 == ID) && ((strict == false)))
                        {
                            alt7 = 1;
                        }
                        else if ((LA7_37 == DOTSTAR) && ((strict == false)))
                        {
                            alt7 = 2;
                        }

                    }

                }
                else if ((LA7_2 == ID) && ((strict == false)))
                {
                    int LA7_36 = input.LA(3);
                    if ((LA7_36 == DOUBLE_QUOTE) && ((strict == false)))
                    {
                        int LA7_37 = input.LA(4);
                        if ((LA7_37 == AS || LA7_37 == COMMA || LA7_37 == DOT || LA7_37 == DOUBLE_QUOTE || LA7_37 == FROM || LA7_37 == ID) && ((strict == false)))
                        {
                            alt7 = 1;
                        }
                        else if ((LA7_37 == DOTSTAR) && ((strict == false)))
                        {
                            alt7 = 2;
                        }

                    }

                }

            }
            else if ((LA7_0 == SCORE))
            {
                alt7 = 1;
            }
            else if (((LA7_0 >= AND && LA7_0 <= ASC) || LA7_0 == BY || LA7_0 == CONTAINS || LA7_0 == DESC || LA7_0 == FALSE || LA7_0 == FROM || (LA7_0 >= IN && LA7_0 <= LEFT) || LA7_0 == LIKE || LA7_0 == NOT || LA7_0 == NULL || (LA7_0 >= ON && LA7_0 <= OUTER) || LA7_0 == SELECT || (LA7_0 >= TIMESTAMP && LA7_0 <= WHERE)) && ((strict == false)))
            {
                alt7 = 1;
            }

            switch (alt7)
            {
            case 1:
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:320:9: valueExpression ( ( AS )? columnName )?
            {
                pushFollow(FOLLOW_valueExpression_in_selectSubList673);
                valueExpression11 = valueExpression();
                state._fsp--;
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                    stream_valueExpression.add(valueExpression11.getTree());
                // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:320:25: ( ( AS )? columnName )?
                int alt6 = 2;
                int LA6_0 = input.LA(1);
                if ((LA6_0 == AS || LA6_0 == ID))
                {
                    alt6 = 1;
                }
                else if ((LA6_0 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    alt6 = 1;
                }
                switch (alt6)
                {
                case 1:
                // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:320:26: ( AS )? columnName
                {
                    // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:320:26: ( AS )?
                    int alt5 = 2;
                    int LA5_0 = input.LA(1);
                    if ((LA5_0 == AS))
                    {
                        alt5 = 1;
                    }
                    switch (alt5)
                    {
                    case 1:
                    // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:320:26: AS
                    {
                        AS12 = (Token) match(input, AS, FOLLOW_AS_in_selectSubList676);
                        if (state.failed)
                            return retval;
                        if (state.backtracking == 0)
                            stream_AS.add(AS12);

                    }
                        break;

                    }

                    pushFollow(FOLLOW_columnName_in_selectSubList679);
                    columnName13 = columnName();
                    state._fsp--;
                    if (state.failed)
                        return retval;
                    if (state.backtracking == 0)
                        stream_columnName.add(columnName13.getTree());
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
                if (state.backtracking == 0)
                {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval = new RewriteRuleSubtreeStream(adaptor, "rule retval", retval != null ? retval.getTree() : null);

                    root_0 = (Object) adaptor.nil();
                    // 321:17: -> ^( COLUMN valueExpression ( columnName )? )
                    {
                        // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:322:25: ^( COLUMN valueExpression ( columnName )? )
                        {
                            Object root_1 = (Object) adaptor.nil();
                            root_1 = (Object) adaptor.becomeRoot((Object) adaptor.create(COLUMN, "COLUMN"), root_1);
                            adaptor.addChild(root_1, stream_valueExpression.nextTree());
                            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:322:50: ( columnName )?
                            if (stream_columnName.hasNext())
                            {
                                adaptor.addChild(root_1, stream_columnName.nextTree());
                            }
                            stream_columnName.reset();

                            adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;
                }

            }
                break;
            case 2:
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:323:11: qualifier DOTSTAR
            {
                pushFollow(FOLLOW_qualifier_in_selectSubList744);
                qualifier14 = qualifier();
                state._fsp--;
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                    stream_qualifier.add(qualifier14.getTree());
                DOTSTAR15 = (Token) match(input, DOTSTAR, FOLLOW_DOTSTAR_in_selectSubList746);
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                    stream_DOTSTAR.add(DOTSTAR15);

                // AST REWRITE
                // elements: qualifier
                // token labels:
                // rule labels: retval
                // token list labels:
                // rule list labels:
                // wildcard labels:
                if (state.backtracking == 0)
                {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval = new RewriteRuleSubtreeStream(adaptor, "rule retval", retval != null ? retval.getTree() : null);

                    root_0 = (Object) adaptor.nil();
                    // 324:17: -> ^( ALL_COLUMNS qualifier )
                    {
                        // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:325:25: ^( ALL_COLUMNS qualifier )
                        {
                            Object root_1 = (Object) adaptor.nil();
                            root_1 = (Object) adaptor.becomeRoot((Object) adaptor.create(ALL_COLUMNS, "ALL_COLUMNS"), root_1);
                            adaptor.addChild(root_1, stream_qualifier.nextTree());
                            adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;
                }

            }
                break;

            }
            retval.stop = input.LT(-1);

            if (state.backtracking == 0)
            {
                retval.tree = (Object) adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        catch (RecognitionException e)
        {
            throw new CmisInvalidArgumentException(getErrorString(e), e);
        }

        finally
        {
            // do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "selectSubList"

    public static class valueExpression_return extends ParserRuleReturnScope
    {
        Object tree;

        @Override
        public Object getTree()
        {
            return tree;
        }
    };

    // $ANTLR start "valueExpression"
    // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:328:1: valueExpression : ( columnReference -> columnReference | valueFunction -> valueFunction );
    public final CMISParser.valueExpression_return valueExpression() throws RecognitionException
    {
        CMISParser.valueExpression_return retval = new CMISParser.valueExpression_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        ParserRuleReturnScope columnReference16 = null;
        ParserRuleReturnScope valueFunction17 = null;

        RewriteRuleSubtreeStream stream_valueFunction = new RewriteRuleSubtreeStream(adaptor, "rule valueFunction");
        RewriteRuleSubtreeStream stream_columnReference = new RewriteRuleSubtreeStream(adaptor, "rule columnReference");

        paraphrases.push("in value expression");

        try
        {
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:337:9: ( columnReference -> columnReference | valueFunction -> valueFunction )
            int alt8 = 2;
            int LA8_0 = input.LA(1);
            if ((LA8_0 == ID))
            {
                int LA8_1 = input.LA(2);
                if ((LA8_1 == AS || LA8_1 == COMMA || LA8_1 == DOT || LA8_1 == DOUBLE_QUOTE || LA8_1 == EQUALS || LA8_1 == FROM || (LA8_1 >= GREATERTHAN && LA8_1 <= ID) || (LA8_1 >= LESSTHAN && LA8_1 <= LESSTHANOREQUALS) || LA8_1 == NOTEQUALS))
                {
                    alt8 = 1;
                }
                else if ((LA8_1 == LPAREN) && ((strict == false)))
                {
                    alt8 = 2;
                }

            }
            else if ((LA8_0 == DOUBLE_QUOTE) && ((strict == false)))
            {
                alt8 = 1;
            }
            else if ((LA8_0 == SCORE))
            {
                alt8 = 2;
            }
            else if (((LA8_0 >= AND && LA8_0 <= ASC) || LA8_0 == BY || LA8_0 == CONTAINS || LA8_0 == DESC || LA8_0 == FALSE || LA8_0 == FROM || (LA8_0 >= IN && LA8_0 <= LEFT) || LA8_0 == LIKE || LA8_0 == NOT || LA8_0 == NULL || (LA8_0 >= ON && LA8_0 <= OUTER) || LA8_0 == SELECT || (LA8_0 >= TIMESTAMP && LA8_0 <= WHERE)) && ((strict == false)))
            {
                alt8 = 2;
            }

            switch (alt8)
            {
            case 1:
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:338:9: columnReference
            {
                pushFollow(FOLLOW_columnReference_in_valueExpression837);
                columnReference16 = columnReference();
                state._fsp--;
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                    stream_columnReference.add(columnReference16.getTree());
                // AST REWRITE
                // elements: columnReference
                // token labels:
                // rule labels: retval
                // token list labels:
                // rule list labels:
                // wildcard labels:
                if (state.backtracking == 0)
                {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval = new RewriteRuleSubtreeStream(adaptor, "rule retval", retval != null ? retval.getTree() : null);

                    root_0 = (Object) adaptor.nil();
                    // 339:17: -> columnReference
                    {
                        adaptor.addChild(root_0, stream_columnReference.nextTree());
                    }

                    retval.tree = root_0;
                }

            }
                break;
            case 2:
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:340:11: valueFunction
            {
                pushFollow(FOLLOW_valueFunction_in_valueExpression869);
                valueFunction17 = valueFunction();
                state._fsp--;
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                    stream_valueFunction.add(valueFunction17.getTree());
                // AST REWRITE
                // elements: valueFunction
                // token labels:
                // rule labels: retval
                // token list labels:
                // rule list labels:
                // wildcard labels:
                if (state.backtracking == 0)
                {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval = new RewriteRuleSubtreeStream(adaptor, "rule retval", retval != null ? retval.getTree() : null);

                    root_0 = (Object) adaptor.nil();
                    // 341:17: -> valueFunction
                    {
                        adaptor.addChild(root_0, stream_valueFunction.nextTree());
                    }

                    retval.tree = root_0;
                }

            }
                break;

            }
            retval.stop = input.LT(-1);

            if (state.backtracking == 0)
            {
                retval.tree = (Object) adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
            if (state.backtracking == 0)
            {
                paraphrases.pop();
            }
        }

        catch (RecognitionException e)
        {
            throw new CmisInvalidArgumentException(getErrorString(e), e);
        }

        finally
        {
            // do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "valueExpression"

    public static class columnReference_return extends ParserRuleReturnScope
    {
        Object tree;

        @Override
        public Object getTree()
        {
            return tree;
        }
    };

    // $ANTLR start "columnReference"
    // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:344:1: columnReference : ( qualifier DOT )? columnName -> ^( COLUMN_REF columnName ( qualifier )? ) ;
    public final CMISParser.columnReference_return columnReference() throws RecognitionException
    {
        CMISParser.columnReference_return retval = new CMISParser.columnReference_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token DOT19 = null;
        ParserRuleReturnScope qualifier18 = null;
        ParserRuleReturnScope columnName20 = null;

        Object DOT19_tree = null;
        RewriteRuleTokenStream stream_DOT = new RewriteRuleTokenStream(adaptor, "token DOT");
        RewriteRuleSubtreeStream stream_columnName = new RewriteRuleSubtreeStream(adaptor, "rule columnName");
        RewriteRuleSubtreeStream stream_qualifier = new RewriteRuleSubtreeStream(adaptor, "rule qualifier");

        paraphrases.push("in column reference");

        try
        {
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:353:9: ( ( qualifier DOT )? columnName -> ^( COLUMN_REF columnName ( qualifier )? ) )
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:354:9: ( qualifier DOT )? columnName
            {
                // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:354:9: ( qualifier DOT )?
                int alt9 = 2;
                int LA9_0 = input.LA(1);
                if ((LA9_0 == ID))
                {
                    int LA9_1 = input.LA(2);
                    if ((LA9_1 == DOT))
                    {
                        alt9 = 1;
                    }
                }
                else if ((LA9_0 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    int LA9_2 = input.LA(2);
                    if ((LA9_2 == SELECT) && ((strict == false)))
                    {
                        int LA9_5 = input.LA(3);
                        if ((LA9_5 == DOUBLE_QUOTE) && ((strict == false)))
                        {
                            int LA9_34 = input.LA(4);
                            if ((LA9_34 == DOT) && ((strict == false)))
                            {
                                alt9 = 1;
                            }
                        }
                    }
                    else if ((LA9_2 == AS) && ((strict == false)))
                    {
                        int LA9_6 = input.LA(3);
                        if ((LA9_6 == DOUBLE_QUOTE) && ((strict == false)))
                        {
                            int LA9_34 = input.LA(4);
                            if ((LA9_34 == DOT) && ((strict == false)))
                            {
                                alt9 = 1;
                            }
                        }
                    }
                    else if ((LA9_2 == FROM) && ((strict == false)))
                    {
                        int LA9_7 = input.LA(3);
                        if ((LA9_7 == DOUBLE_QUOTE) && ((strict == false)))
                        {
                            int LA9_34 = input.LA(4);
                            if ((LA9_34 == DOT) && ((strict == false)))
                            {
                                alt9 = 1;
                            }
                        }
                    }
                    else if ((LA9_2 == JOIN) && ((strict == false)))
                    {
                        int LA9_8 = input.LA(3);
                        if ((LA9_8 == DOUBLE_QUOTE) && ((strict == false)))
                        {
                            int LA9_34 = input.LA(4);
                            if ((LA9_34 == DOT) && ((strict == false)))
                            {
                                alt9 = 1;
                            }
                        }
                    }
                    else if ((LA9_2 == INNER) && ((strict == false)))
                    {
                        int LA9_9 = input.LA(3);
                        if ((LA9_9 == DOUBLE_QUOTE) && ((strict == false)))
                        {
                            int LA9_34 = input.LA(4);
                            if ((LA9_34 == DOT) && ((strict == false)))
                            {
                                alt9 = 1;
                            }
                        }
                    }
                    else if ((LA9_2 == LEFT) && ((strict == false)))
                    {
                        int LA9_10 = input.LA(3);
                        if ((LA9_10 == DOUBLE_QUOTE) && ((strict == false)))
                        {
                            int LA9_34 = input.LA(4);
                            if ((LA9_34 == DOT) && ((strict == false)))
                            {
                                alt9 = 1;
                            }
                        }
                    }
                    else if ((LA9_2 == OUTER) && ((strict == false)))
                    {
                        int LA9_11 = input.LA(3);
                        if ((LA9_11 == DOUBLE_QUOTE) && ((strict == false)))
                        {
                            int LA9_34 = input.LA(4);
                            if ((LA9_34 == DOT) && ((strict == false)))
                            {
                                alt9 = 1;
                            }
                        }
                    }
                    else if ((LA9_2 == ON) && ((strict == false)))
                    {
                        int LA9_12 = input.LA(3);
                        if ((LA9_12 == DOUBLE_QUOTE) && ((strict == false)))
                        {
                            int LA9_34 = input.LA(4);
                            if ((LA9_34 == DOT) && ((strict == false)))
                            {
                                alt9 = 1;
                            }
                        }
                    }
                    else if ((LA9_2 == WHERE) && ((strict == false)))
                    {
                        int LA9_13 = input.LA(3);
                        if ((LA9_13 == DOUBLE_QUOTE) && ((strict == false)))
                        {
                            int LA9_34 = input.LA(4);
                            if ((LA9_34 == DOT) && ((strict == false)))
                            {
                                alt9 = 1;
                            }
                        }
                    }
                    else if ((LA9_2 == OR) && ((strict == false)))
                    {
                        int LA9_14 = input.LA(3);
                        if ((LA9_14 == DOUBLE_QUOTE) && ((strict == false)))
                        {
                            int LA9_34 = input.LA(4);
                            if ((LA9_34 == DOT) && ((strict == false)))
                            {
                                alt9 = 1;
                            }
                        }
                    }
                    else if ((LA9_2 == AND) && ((strict == false)))
                    {
                        int LA9_15 = input.LA(3);
                        if ((LA9_15 == DOUBLE_QUOTE) && ((strict == false)))
                        {
                            int LA9_34 = input.LA(4);
                            if ((LA9_34 == DOT) && ((strict == false)))
                            {
                                alt9 = 1;
                            }
                        }
                    }
                    else if ((LA9_2 == NOT) && ((strict == false)))
                    {
                        int LA9_16 = input.LA(3);
                        if ((LA9_16 == DOUBLE_QUOTE) && ((strict == false)))
                        {
                            int LA9_34 = input.LA(4);
                            if ((LA9_34 == DOT) && ((strict == false)))
                            {
                                alt9 = 1;
                            }
                        }
                    }
                    else if ((LA9_2 == IN) && ((strict == false)))
                    {
                        int LA9_17 = input.LA(3);
                        if ((LA9_17 == DOUBLE_QUOTE) && ((strict == false)))
                        {
                            int LA9_34 = input.LA(4);
                            if ((LA9_34 == DOT) && ((strict == false)))
                            {
                                alt9 = 1;
                            }
                        }
                    }
                    else if ((LA9_2 == LIKE) && ((strict == false)))
                    {
                        int LA9_18 = input.LA(3);
                        if ((LA9_18 == DOUBLE_QUOTE) && ((strict == false)))
                        {
                            int LA9_34 = input.LA(4);
                            if ((LA9_34 == DOT) && ((strict == false)))
                            {
                                alt9 = 1;
                            }
                        }
                    }
                    else if ((LA9_2 == IS) && ((strict == false)))
                    {
                        int LA9_19 = input.LA(3);
                        if ((LA9_19 == DOUBLE_QUOTE) && ((strict == false)))
                        {
                            int LA9_34 = input.LA(4);
                            if ((LA9_34 == DOT) && ((strict == false)))
                            {
                                alt9 = 1;
                            }
                        }
                    }
                    else if ((LA9_2 == NULL) && ((strict == false)))
                    {
                        int LA9_20 = input.LA(3);
                        if ((LA9_20 == DOUBLE_QUOTE) && ((strict == false)))
                        {
                            int LA9_34 = input.LA(4);
                            if ((LA9_34 == DOT) && ((strict == false)))
                            {
                                alt9 = 1;
                            }
                        }
                    }
                    else if ((LA9_2 == ANY) && ((strict == false)))
                    {
                        int LA9_21 = input.LA(3);
                        if ((LA9_21 == DOUBLE_QUOTE) && ((strict == false)))
                        {
                            int LA9_34 = input.LA(4);
                            if ((LA9_34 == DOT) && ((strict == false)))
                            {
                                alt9 = 1;
                            }
                        }
                    }
                    else if ((LA9_2 == CONTAINS) && ((strict == false)))
                    {
                        int LA9_22 = input.LA(3);
                        if ((LA9_22 == DOUBLE_QUOTE) && ((strict == false)))
                        {
                            int LA9_34 = input.LA(4);
                            if ((LA9_34 == DOT) && ((strict == false)))
                            {
                                alt9 = 1;
                            }
                        }
                    }
                    else if ((LA9_2 == IN_FOLDER) && ((strict == false)))
                    {
                        int LA9_23 = input.LA(3);
                        if ((LA9_23 == DOUBLE_QUOTE) && ((strict == false)))
                        {
                            int LA9_34 = input.LA(4);
                            if ((LA9_34 == DOT) && ((strict == false)))
                            {
                                alt9 = 1;
                            }
                        }
                    }
                    else if ((LA9_2 == IN_TREE) && ((strict == false)))
                    {
                        int LA9_24 = input.LA(3);
                        if ((LA9_24 == DOUBLE_QUOTE) && ((strict == false)))
                        {
                            int LA9_34 = input.LA(4);
                            if ((LA9_34 == DOT) && ((strict == false)))
                            {
                                alt9 = 1;
                            }
                        }
                    }
                    else if ((LA9_2 == ORDER) && ((strict == false)))
                    {
                        int LA9_25 = input.LA(3);
                        if ((LA9_25 == DOUBLE_QUOTE) && ((strict == false)))
                        {
                            int LA9_34 = input.LA(4);
                            if ((LA9_34 == DOT) && ((strict == false)))
                            {
                                alt9 = 1;
                            }
                        }
                    }
                    else if ((LA9_2 == BY) && ((strict == false)))
                    {
                        int LA9_26 = input.LA(3);
                        if ((LA9_26 == DOUBLE_QUOTE) && ((strict == false)))
                        {
                            int LA9_34 = input.LA(4);
                            if ((LA9_34 == DOT) && ((strict == false)))
                            {
                                alt9 = 1;
                            }
                        }
                    }
                    else if ((LA9_2 == ASC) && ((strict == false)))
                    {
                        int LA9_27 = input.LA(3);
                        if ((LA9_27 == DOUBLE_QUOTE) && ((strict == false)))
                        {
                            int LA9_34 = input.LA(4);
                            if ((LA9_34 == DOT) && ((strict == false)))
                            {
                                alt9 = 1;
                            }
                        }
                    }
                    else if ((LA9_2 == DESC) && ((strict == false)))
                    {
                        int LA9_28 = input.LA(3);
                        if ((LA9_28 == DOUBLE_QUOTE) && ((strict == false)))
                        {
                            int LA9_34 = input.LA(4);
                            if ((LA9_34 == DOT) && ((strict == false)))
                            {
                                alt9 = 1;
                            }
                        }
                    }
                    else if ((LA9_2 == TIMESTAMP) && ((strict == false)))
                    {
                        int LA9_29 = input.LA(3);
                        if ((LA9_29 == DOUBLE_QUOTE) && ((strict == false)))
                        {
                            int LA9_34 = input.LA(4);
                            if ((LA9_34 == DOT) && ((strict == false)))
                            {
                                alt9 = 1;
                            }
                        }
                    }
                    else if ((LA9_2 == TRUE) && ((strict == false)))
                    {
                        int LA9_30 = input.LA(3);
                        if ((LA9_30 == DOUBLE_QUOTE) && ((strict == false)))
                        {
                            int LA9_34 = input.LA(4);
                            if ((LA9_34 == DOT) && ((strict == false)))
                            {
                                alt9 = 1;
                            }
                        }
                    }
                    else if ((LA9_2 == FALSE) && ((strict == false)))
                    {
                        int LA9_31 = input.LA(3);
                        if ((LA9_31 == DOUBLE_QUOTE) && ((strict == false)))
                        {
                            int LA9_34 = input.LA(4);
                            if ((LA9_34 == DOT) && ((strict == false)))
                            {
                                alt9 = 1;
                            }
                        }
                    }
                    else if ((LA9_2 == SCORE) && ((strict == false)))
                    {
                        int LA9_32 = input.LA(3);
                        if ((LA9_32 == DOUBLE_QUOTE) && ((strict == false)))
                        {
                            int LA9_34 = input.LA(4);
                            if ((LA9_34 == DOT) && ((strict == false)))
                            {
                                alt9 = 1;
                            }
                        }
                    }
                    else if ((LA9_2 == ID) && ((strict == false)))
                    {
                        int LA9_33 = input.LA(3);
                        if ((LA9_33 == DOUBLE_QUOTE) && ((strict == false)))
                        {
                            int LA9_34 = input.LA(4);
                            if ((LA9_34 == DOT) && ((strict == false)))
                            {
                                alt9 = 1;
                            }
                        }
                    }
                }
                switch (alt9)
                {
                case 1:
                // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:354:10: qualifier DOT
                {
                    pushFollow(FOLLOW_qualifier_in_columnReference933);
                    qualifier18 = qualifier();
                    state._fsp--;
                    if (state.failed)
                        return retval;
                    if (state.backtracking == 0)
                        stream_qualifier.add(qualifier18.getTree());
                    DOT19 = (Token) match(input, DOT, FOLLOW_DOT_in_columnReference935);
                    if (state.failed)
                        return retval;
                    if (state.backtracking == 0)
                        stream_DOT.add(DOT19);

                }
                    break;

                }

                pushFollow(FOLLOW_columnName_in_columnReference939);
                columnName20 = columnName();
                state._fsp--;
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                    stream_columnName.add(columnName20.getTree());
                // AST REWRITE
                // elements: qualifier, columnName
                // token labels:
                // rule labels: retval
                // token list labels:
                // rule list labels:
                // wildcard labels:
                if (state.backtracking == 0)
                {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval = new RewriteRuleSubtreeStream(adaptor, "rule retval", retval != null ? retval.getTree() : null);

                    root_0 = (Object) adaptor.nil();
                    // 355:17: -> ^( COLUMN_REF columnName ( qualifier )? )
                    {
                        // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:356:25: ^( COLUMN_REF columnName ( qualifier )? )
                        {
                            Object root_1 = (Object) adaptor.nil();
                            root_1 = (Object) adaptor.becomeRoot((Object) adaptor.create(COLUMN_REF, "COLUMN_REF"), root_1);
                            adaptor.addChild(root_1, stream_columnName.nextTree());
                            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:356:49: ( qualifier )?
                            if (stream_qualifier.hasNext())
                            {
                                adaptor.addChild(root_1, stream_qualifier.nextTree());
                            }
                            stream_qualifier.reset();

                            adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;
                }

            }

            retval.stop = input.LT(-1);

            if (state.backtracking == 0)
            {
                retval.tree = (Object) adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
            if (state.backtracking == 0)
            {
                paraphrases.pop();
            }
        }

        catch (RecognitionException e)
        {
            throw new CmisInvalidArgumentException(getErrorString(e), e);
        }

        finally
        {
            // do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "columnReference"

    public static class valueFunction_return extends ParserRuleReturnScope
    {
        Object tree;

        @Override
        public Object getTree()
        {
            return tree;
        }
    };

    // $ANTLR start "valueFunction"
    // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:359:1: valueFunction : (cmisFunctionName= cmisFunction LPAREN ( functionArgument )* RPAREN -> ^( FUNCTION $cmisFunctionName LPAREN ( functionArgument )* RPAREN ) |{...}? =>functionName= keyWordOrId LPAREN ( functionArgument )* RPAREN -> ^( FUNCTION $functionName LPAREN ( functionArgument )* RPAREN ) );
    public final CMISParser.valueFunction_return valueFunction() throws RecognitionException
    {
        CMISParser.valueFunction_return retval = new CMISParser.valueFunction_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token LPAREN21 = null;
        Token RPAREN23 = null;
        Token LPAREN24 = null;
        Token RPAREN26 = null;
        ParserRuleReturnScope cmisFunctionName = null;
        ParserRuleReturnScope functionName = null;
        ParserRuleReturnScope functionArgument22 = null;
        ParserRuleReturnScope functionArgument25 = null;

        Object LPAREN21_tree = null;
        Object RPAREN23_tree = null;
        Object LPAREN24_tree = null;
        Object RPAREN26_tree = null;
        RewriteRuleTokenStream stream_RPAREN = new RewriteRuleTokenStream(adaptor, "token RPAREN");
        RewriteRuleTokenStream stream_LPAREN = new RewriteRuleTokenStream(adaptor, "token LPAREN");
        RewriteRuleSubtreeStream stream_cmisFunction = new RewriteRuleSubtreeStream(adaptor, "rule cmisFunction");
        RewriteRuleSubtreeStream stream_keyWordOrId = new RewriteRuleSubtreeStream(adaptor, "rule keyWordOrId");
        RewriteRuleSubtreeStream stream_functionArgument = new RewriteRuleSubtreeStream(adaptor, "rule functionArgument");

        paraphrases.push("in function");

        try
        {
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:368:9: (cmisFunctionName= cmisFunction LPAREN ( functionArgument )* RPAREN -> ^( FUNCTION $cmisFunctionName LPAREN ( functionArgument )* RPAREN ) |{...}? =>functionName= keyWordOrId LPAREN ( functionArgument )* RPAREN -> ^( FUNCTION $functionName LPAREN ( functionArgument )* RPAREN ) )
            int alt12 = 2;
            alt12 = dfa12.predict(input);
            switch (alt12)
            {
            case 1:
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:369:9: cmisFunctionName= cmisFunction LPAREN ( functionArgument )* RPAREN
            {
                pushFollow(FOLLOW_cmisFunction_in_valueFunction1035);
                cmisFunctionName = cmisFunction();
                state._fsp--;
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                    stream_cmisFunction.add(cmisFunctionName.getTree());
                LPAREN21 = (Token) match(input, LPAREN, FOLLOW_LPAREN_in_valueFunction1037);
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                    stream_LPAREN.add(LPAREN21);

                // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:369:46: ( functionArgument )*
                loop10: while (true)
                {
                    int alt10 = 2;
                    int LA10_0 = input.LA(1);
                    if ((LA10_0 == ID))
                    {
                        alt10 = 1;
                    }
                    else if ((LA10_0 == DOUBLE_QUOTE) && ((strict == false)))
                    {
                        alt10 = 1;
                    }
                    else if ((LA10_0 == DECIMAL_INTEGER_LITERAL || (LA10_0 >= FALSE && LA10_0 <= FLOATING_POINT_LITERAL) || LA10_0 == QUOTED_STRING || (LA10_0 >= TIMESTAMP && LA10_0 <= TRUE)))
                    {
                        alt10 = 1;
                    }
                    else if ((LA10_0 == COLON) && ((strict == false)))
                    {
                        alt10 = 1;
                    }

                    switch (alt10)
                    {
                    case 1:
                    // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:369:46: functionArgument
                    {
                        pushFollow(FOLLOW_functionArgument_in_valueFunction1039);
                        functionArgument22 = functionArgument();
                        state._fsp--;
                        if (state.failed)
                            return retval;
                        if (state.backtracking == 0)
                            stream_functionArgument.add(functionArgument22.getTree());
                    }
                        break;

                    default:
                        break loop10;
                    }
                }

                RPAREN23 = (Token) match(input, RPAREN, FOLLOW_RPAREN_in_valueFunction1042);
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                    stream_RPAREN.add(RPAREN23);

                // AST REWRITE
                // elements: functionArgument, cmisFunctionName, LPAREN, RPAREN
                // token labels:
                // rule labels: retval, cmisFunctionName
                // token list labels:
                // rule list labels:
                // wildcard labels:
                if (state.backtracking == 0)
                {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval = new RewriteRuleSubtreeStream(adaptor, "rule retval", retval != null ? retval.getTree() : null);
                    RewriteRuleSubtreeStream stream_cmisFunctionName = new RewriteRuleSubtreeStream(adaptor, "rule cmisFunctionName", cmisFunctionName != null ? cmisFunctionName.getTree() : null);

                    root_0 = (Object) adaptor.nil();
                    // 370:17: -> ^( FUNCTION $cmisFunctionName LPAREN ( functionArgument )* RPAREN )
                    {
                        // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:371:25: ^( FUNCTION $cmisFunctionName LPAREN ( functionArgument )* RPAREN )
                        {
                            Object root_1 = (Object) adaptor.nil();
                            root_1 = (Object) adaptor.becomeRoot((Object) adaptor.create(FUNCTION, "FUNCTION"), root_1);
                            adaptor.addChild(root_1, stream_cmisFunctionName.nextTree());
                            adaptor.addChild(root_1, stream_LPAREN.nextNode());
                            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:371:61: ( functionArgument )*
                            while (stream_functionArgument.hasNext())
                            {
                                adaptor.addChild(root_1, stream_functionArgument.nextTree());
                            }
                            stream_functionArgument.reset();

                            adaptor.addChild(root_1, stream_RPAREN.nextNode());
                            adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;
                }

            }
                break;
            case 2:
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:372:11: {...}? =>functionName= keyWordOrId LPAREN ( functionArgument )* RPAREN
            {
                if (!((strict == false)))
                {
                    if (state.backtracking > 0)
                    {
                        state.failed = true;
                        return retval;
                    }
                    throw new FailedPredicateException(input, "valueFunction", "strict == false");
                }
                pushFollow(FOLLOW_keyWordOrId_in_valueFunction1115);
                functionName = keyWordOrId();
                state._fsp--;
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                    stream_keyWordOrId.add(functionName.getTree());
                LPAREN24 = (Token) match(input, LPAREN, FOLLOW_LPAREN_in_valueFunction1117);
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                    stream_LPAREN.add(LPAREN24);

                // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:372:64: ( functionArgument )*
                loop11: while (true)
                {
                    int alt11 = 2;
                    int LA11_0 = input.LA(1);
                    if ((LA11_0 == ID))
                    {
                        alt11 = 1;
                    }
                    else if ((LA11_0 == DOUBLE_QUOTE) && ((strict == false)))
                    {
                        alt11 = 1;
                    }
                    else if ((LA11_0 == DECIMAL_INTEGER_LITERAL || (LA11_0 >= FALSE && LA11_0 <= FLOATING_POINT_LITERAL) || LA11_0 == QUOTED_STRING || (LA11_0 >= TIMESTAMP && LA11_0 <= TRUE)))
                    {
                        alt11 = 1;
                    }
                    else if ((LA11_0 == COLON) && ((strict == false)))
                    {
                        alt11 = 1;
                    }

                    switch (alt11)
                    {
                    case 1:
                    // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:372:64: functionArgument
                    {
                        pushFollow(FOLLOW_functionArgument_in_valueFunction1119);
                        functionArgument25 = functionArgument();
                        state._fsp--;
                        if (state.failed)
                            return retval;
                        if (state.backtracking == 0)
                            stream_functionArgument.add(functionArgument25.getTree());
                    }
                        break;

                    default:
                        break loop11;
                    }
                }

                RPAREN26 = (Token) match(input, RPAREN, FOLLOW_RPAREN_in_valueFunction1122);
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                    stream_RPAREN.add(RPAREN26);

                // AST REWRITE
                // elements: functionArgument, LPAREN, functionName, RPAREN
                // token labels:
                // rule labels: retval, functionName
                // token list labels:
                // rule list labels:
                // wildcard labels:
                if (state.backtracking == 0)
                {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval = new RewriteRuleSubtreeStream(adaptor, "rule retval", retval != null ? retval.getTree() : null);
                    RewriteRuleSubtreeStream stream_functionName = new RewriteRuleSubtreeStream(adaptor, "rule functionName", functionName != null ? functionName.getTree() : null);

                    root_0 = (Object) adaptor.nil();
                    // 373:17: -> ^( FUNCTION $functionName LPAREN ( functionArgument )* RPAREN )
                    {
                        // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:374:25: ^( FUNCTION $functionName LPAREN ( functionArgument )* RPAREN )
                        {
                            Object root_1 = (Object) adaptor.nil();
                            root_1 = (Object) adaptor.becomeRoot((Object) adaptor.create(FUNCTION, "FUNCTION"), root_1);
                            adaptor.addChild(root_1, stream_functionName.nextTree());
                            adaptor.addChild(root_1, stream_LPAREN.nextNode());
                            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:374:57: ( functionArgument )*
                            while (stream_functionArgument.hasNext())
                            {
                                adaptor.addChild(root_1, stream_functionArgument.nextTree());
                            }
                            stream_functionArgument.reset();

                            adaptor.addChild(root_1, stream_RPAREN.nextNode());
                            adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;
                }

            }
                break;

            }
            retval.stop = input.LT(-1);

            if (state.backtracking == 0)
            {
                retval.tree = (Object) adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
            if (state.backtracking == 0)
            {
                paraphrases.pop();
            }
        }

        catch (RecognitionException e)
        {
            throw new CmisInvalidArgumentException(getErrorString(e), e);
        }

        finally
        {
            // do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "valueFunction"

    public static class functionArgument_return extends ParserRuleReturnScope
    {
        Object tree;

        @Override
        public Object getTree()
        {
            return tree;
        }
    };

    // $ANTLR start "functionArgument"
    // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:377:1: functionArgument : ( qualifier DOT columnName -> ^( COLUMN_REF columnName qualifier ) | identifier | literalOrParameterName );
    public final CMISParser.functionArgument_return functionArgument() throws RecognitionException
    {
        CMISParser.functionArgument_return retval = new CMISParser.functionArgument_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token DOT28 = null;
        ParserRuleReturnScope qualifier27 = null;
        ParserRuleReturnScope columnName29 = null;
        ParserRuleReturnScope identifier30 = null;
        ParserRuleReturnScope literalOrParameterName31 = null;

        Object DOT28_tree = null;
        RewriteRuleTokenStream stream_DOT = new RewriteRuleTokenStream(adaptor, "token DOT");
        RewriteRuleSubtreeStream stream_columnName = new RewriteRuleSubtreeStream(adaptor, "rule columnName");
        RewriteRuleSubtreeStream stream_qualifier = new RewriteRuleSubtreeStream(adaptor, "rule qualifier");

        try
        {
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:378:9: ( qualifier DOT columnName -> ^( COLUMN_REF columnName qualifier ) | identifier | literalOrParameterName )
            int alt13 = 3;
            int LA13_0 = input.LA(1);
            if ((LA13_0 == ID))
            {
                int LA13_1 = input.LA(2);
                if ((LA13_1 == DOT))
                {
                    alt13 = 1;
                }
                else if ((LA13_1 == COLON || LA13_1 == DECIMAL_INTEGER_LITERAL || LA13_1 == DOUBLE_QUOTE || (LA13_1 >= FALSE && LA13_1 <= FLOATING_POINT_LITERAL) || LA13_1 == ID || (LA13_1 >= QUOTED_STRING && LA13_1 <= RPAREN) || (LA13_1 >= TIMESTAMP && LA13_1 <= TRUE)))
                {
                    alt13 = 2;
                }

                else
                {
                    if (state.backtracking > 0)
                    {
                        state.failed = true;
                        return retval;
                    }
                    int nvaeMark = input.mark();
                    try
                    {
                        input.consume();
                        NoViableAltException nvae = new NoViableAltException("", 13, 1, input);
                        throw nvae;
                    }
                    finally
                    {
                        input.rewind(nvaeMark);
                    }
                }

            }
            else if ((LA13_0 == DOUBLE_QUOTE) && ((strict == false)))
            {
                int LA13_2 = input.LA(2);
                if ((LA13_2 == SELECT) && ((strict == false)))
                {
                    int LA13_7 = input.LA(3);
                    if ((LA13_7 == DOUBLE_QUOTE) && ((strict == false)))
                    {
                        int LA13_36 = input.LA(4);
                        if ((LA13_36 == DOT) && ((strict == false)))
                        {
                            alt13 = 1;
                        }
                        else if ((LA13_36 == COLON || LA13_36 == DECIMAL_INTEGER_LITERAL || LA13_36 == DOUBLE_QUOTE || (LA13_36 >= FALSE && LA13_36 <= FLOATING_POINT_LITERAL) || LA13_36 == ID || (LA13_36 >= QUOTED_STRING && LA13_36 <= RPAREN) || (LA13_36 >= TIMESTAMP && LA13_36 <= TRUE)) && ((strict == false)))
                        {
                            alt13 = 2;
                        }

                    }

                }
                else if ((LA13_2 == AS) && ((strict == false)))
                {
                    int LA13_8 = input.LA(3);
                    if ((LA13_8 == DOUBLE_QUOTE) && ((strict == false)))
                    {
                        int LA13_36 = input.LA(4);
                        if ((LA13_36 == DOT) && ((strict == false)))
                        {
                            alt13 = 1;
                        }
                        else if ((LA13_36 == COLON || LA13_36 == DECIMAL_INTEGER_LITERAL || LA13_36 == DOUBLE_QUOTE || (LA13_36 >= FALSE && LA13_36 <= FLOATING_POINT_LITERAL) || LA13_36 == ID || (LA13_36 >= QUOTED_STRING && LA13_36 <= RPAREN) || (LA13_36 >= TIMESTAMP && LA13_36 <= TRUE)) && ((strict == false)))
                        {
                            alt13 = 2;
                        }

                    }

                }
                else if ((LA13_2 == FROM) && ((strict == false)))
                {
                    int LA13_9 = input.LA(3);
                    if ((LA13_9 == DOUBLE_QUOTE) && ((strict == false)))
                    {
                        int LA13_36 = input.LA(4);
                        if ((LA13_36 == DOT) && ((strict == false)))
                        {
                            alt13 = 1;
                        }
                        else if ((LA13_36 == COLON || LA13_36 == DECIMAL_INTEGER_LITERAL || LA13_36 == DOUBLE_QUOTE || (LA13_36 >= FALSE && LA13_36 <= FLOATING_POINT_LITERAL) || LA13_36 == ID || (LA13_36 >= QUOTED_STRING && LA13_36 <= RPAREN) || (LA13_36 >= TIMESTAMP && LA13_36 <= TRUE)) && ((strict == false)))
                        {
                            alt13 = 2;
                        }

                    }

                }
                else if ((LA13_2 == JOIN) && ((strict == false)))
                {
                    int LA13_10 = input.LA(3);
                    if ((LA13_10 == DOUBLE_QUOTE) && ((strict == false)))
                    {
                        int LA13_36 = input.LA(4);
                        if ((LA13_36 == DOT) && ((strict == false)))
                        {
                            alt13 = 1;
                        }
                        else if ((LA13_36 == COLON || LA13_36 == DECIMAL_INTEGER_LITERAL || LA13_36 == DOUBLE_QUOTE || (LA13_36 >= FALSE && LA13_36 <= FLOATING_POINT_LITERAL) || LA13_36 == ID || (LA13_36 >= QUOTED_STRING && LA13_36 <= RPAREN) || (LA13_36 >= TIMESTAMP && LA13_36 <= TRUE)) && ((strict == false)))
                        {
                            alt13 = 2;
                        }

                    }

                }
                else if ((LA13_2 == INNER) && ((strict == false)))
                {
                    int LA13_11 = input.LA(3);
                    if ((LA13_11 == DOUBLE_QUOTE) && ((strict == false)))
                    {
                        int LA13_36 = input.LA(4);
                        if ((LA13_36 == DOT) && ((strict == false)))
                        {
                            alt13 = 1;
                        }
                        else if ((LA13_36 == COLON || LA13_36 == DECIMAL_INTEGER_LITERAL || LA13_36 == DOUBLE_QUOTE || (LA13_36 >= FALSE && LA13_36 <= FLOATING_POINT_LITERAL) || LA13_36 == ID || (LA13_36 >= QUOTED_STRING && LA13_36 <= RPAREN) || (LA13_36 >= TIMESTAMP && LA13_36 <= TRUE)) && ((strict == false)))
                        {
                            alt13 = 2;
                        }

                    }

                }
                else if ((LA13_2 == LEFT) && ((strict == false)))
                {
                    int LA13_12 = input.LA(3);
                    if ((LA13_12 == DOUBLE_QUOTE) && ((strict == false)))
                    {
                        int LA13_36 = input.LA(4);
                        if ((LA13_36 == DOT) && ((strict == false)))
                        {
                            alt13 = 1;
                        }
                        else if ((LA13_36 == COLON || LA13_36 == DECIMAL_INTEGER_LITERAL || LA13_36 == DOUBLE_QUOTE || (LA13_36 >= FALSE && LA13_36 <= FLOATING_POINT_LITERAL) || LA13_36 == ID || (LA13_36 >= QUOTED_STRING && LA13_36 <= RPAREN) || (LA13_36 >= TIMESTAMP && LA13_36 <= TRUE)) && ((strict == false)))
                        {
                            alt13 = 2;
                        }

                    }

                }
                else if ((LA13_2 == OUTER) && ((strict == false)))
                {
                    int LA13_13 = input.LA(3);
                    if ((LA13_13 == DOUBLE_QUOTE) && ((strict == false)))
                    {
                        int LA13_36 = input.LA(4);
                        if ((LA13_36 == DOT) && ((strict == false)))
                        {
                            alt13 = 1;
                        }
                        else if ((LA13_36 == COLON || LA13_36 == DECIMAL_INTEGER_LITERAL || LA13_36 == DOUBLE_QUOTE || (LA13_36 >= FALSE && LA13_36 <= FLOATING_POINT_LITERAL) || LA13_36 == ID || (LA13_36 >= QUOTED_STRING && LA13_36 <= RPAREN) || (LA13_36 >= TIMESTAMP && LA13_36 <= TRUE)) && ((strict == false)))
                        {
                            alt13 = 2;
                        }

                    }

                }
                else if ((LA13_2 == ON) && ((strict == false)))
                {
                    int LA13_14 = input.LA(3);
                    if ((LA13_14 == DOUBLE_QUOTE) && ((strict == false)))
                    {
                        int LA13_36 = input.LA(4);
                        if ((LA13_36 == DOT) && ((strict == false)))
                        {
                            alt13 = 1;
                        }
                        else if ((LA13_36 == COLON || LA13_36 == DECIMAL_INTEGER_LITERAL || LA13_36 == DOUBLE_QUOTE || (LA13_36 >= FALSE && LA13_36 <= FLOATING_POINT_LITERAL) || LA13_36 == ID || (LA13_36 >= QUOTED_STRING && LA13_36 <= RPAREN) || (LA13_36 >= TIMESTAMP && LA13_36 <= TRUE)) && ((strict == false)))
                        {
                            alt13 = 2;
                        }

                    }

                }
                else if ((LA13_2 == WHERE) && ((strict == false)))
                {
                    int LA13_15 = input.LA(3);
                    if ((LA13_15 == DOUBLE_QUOTE) && ((strict == false)))
                    {
                        int LA13_36 = input.LA(4);
                        if ((LA13_36 == DOT) && ((strict == false)))
                        {
                            alt13 = 1;
                        }
                        else if ((LA13_36 == COLON || LA13_36 == DECIMAL_INTEGER_LITERAL || LA13_36 == DOUBLE_QUOTE || (LA13_36 >= FALSE && LA13_36 <= FLOATING_POINT_LITERAL) || LA13_36 == ID || (LA13_36 >= QUOTED_STRING && LA13_36 <= RPAREN) || (LA13_36 >= TIMESTAMP && LA13_36 <= TRUE)) && ((strict == false)))
                        {
                            alt13 = 2;
                        }

                    }

                }
                else if ((LA13_2 == OR) && ((strict == false)))
                {
                    int LA13_16 = input.LA(3);
                    if ((LA13_16 == DOUBLE_QUOTE) && ((strict == false)))
                    {
                        int LA13_36 = input.LA(4);
                        if ((LA13_36 == DOT) && ((strict == false)))
                        {
                            alt13 = 1;
                        }
                        else if ((LA13_36 == COLON || LA13_36 == DECIMAL_INTEGER_LITERAL || LA13_36 == DOUBLE_QUOTE || (LA13_36 >= FALSE && LA13_36 <= FLOATING_POINT_LITERAL) || LA13_36 == ID || (LA13_36 >= QUOTED_STRING && LA13_36 <= RPAREN) || (LA13_36 >= TIMESTAMP && LA13_36 <= TRUE)) && ((strict == false)))
                        {
                            alt13 = 2;
                        }

                    }

                }
                else if ((LA13_2 == AND) && ((strict == false)))
                {
                    int LA13_17 = input.LA(3);
                    if ((LA13_17 == DOUBLE_QUOTE) && ((strict == false)))
                    {
                        int LA13_36 = input.LA(4);
                        if ((LA13_36 == DOT) && ((strict == false)))
                        {
                            alt13 = 1;
                        }
                        else if ((LA13_36 == COLON || LA13_36 == DECIMAL_INTEGER_LITERAL || LA13_36 == DOUBLE_QUOTE || (LA13_36 >= FALSE && LA13_36 <= FLOATING_POINT_LITERAL) || LA13_36 == ID || (LA13_36 >= QUOTED_STRING && LA13_36 <= RPAREN) || (LA13_36 >= TIMESTAMP && LA13_36 <= TRUE)) && ((strict == false)))
                        {
                            alt13 = 2;
                        }

                    }

                }
                else if ((LA13_2 == NOT) && ((strict == false)))
                {
                    int LA13_18 = input.LA(3);
                    if ((LA13_18 == DOUBLE_QUOTE) && ((strict == false)))
                    {
                        int LA13_36 = input.LA(4);
                        if ((LA13_36 == DOT) && ((strict == false)))
                        {
                            alt13 = 1;
                        }
                        else if ((LA13_36 == COLON || LA13_36 == DECIMAL_INTEGER_LITERAL || LA13_36 == DOUBLE_QUOTE || (LA13_36 >= FALSE && LA13_36 <= FLOATING_POINT_LITERAL) || LA13_36 == ID || (LA13_36 >= QUOTED_STRING && LA13_36 <= RPAREN) || (LA13_36 >= TIMESTAMP && LA13_36 <= TRUE)) && ((strict == false)))
                        {
                            alt13 = 2;
                        }

                    }

                }
                else if ((LA13_2 == IN) && ((strict == false)))
                {
                    int LA13_19 = input.LA(3);
                    if ((LA13_19 == DOUBLE_QUOTE) && ((strict == false)))
                    {
                        int LA13_36 = input.LA(4);
                        if ((LA13_36 == DOT) && ((strict == false)))
                        {
                            alt13 = 1;
                        }
                        else if ((LA13_36 == COLON || LA13_36 == DECIMAL_INTEGER_LITERAL || LA13_36 == DOUBLE_QUOTE || (LA13_36 >= FALSE && LA13_36 <= FLOATING_POINT_LITERAL) || LA13_36 == ID || (LA13_36 >= QUOTED_STRING && LA13_36 <= RPAREN) || (LA13_36 >= TIMESTAMP && LA13_36 <= TRUE)) && ((strict == false)))
                        {
                            alt13 = 2;
                        }

                    }

                }
                else if ((LA13_2 == LIKE) && ((strict == false)))
                {
                    int LA13_20 = input.LA(3);
                    if ((LA13_20 == DOUBLE_QUOTE) && ((strict == false)))
                    {
                        int LA13_36 = input.LA(4);
                        if ((LA13_36 == DOT) && ((strict == false)))
                        {
                            alt13 = 1;
                        }
                        else if ((LA13_36 == COLON || LA13_36 == DECIMAL_INTEGER_LITERAL || LA13_36 == DOUBLE_QUOTE || (LA13_36 >= FALSE && LA13_36 <= FLOATING_POINT_LITERAL) || LA13_36 == ID || (LA13_36 >= QUOTED_STRING && LA13_36 <= RPAREN) || (LA13_36 >= TIMESTAMP && LA13_36 <= TRUE)) && ((strict == false)))
                        {
                            alt13 = 2;
                        }

                    }

                }
                else if ((LA13_2 == IS) && ((strict == false)))
                {
                    int LA13_21 = input.LA(3);
                    if ((LA13_21 == DOUBLE_QUOTE) && ((strict == false)))
                    {
                        int LA13_36 = input.LA(4);
                        if ((LA13_36 == DOT) && ((strict == false)))
                        {
                            alt13 = 1;
                        }
                        else if ((LA13_36 == COLON || LA13_36 == DECIMAL_INTEGER_LITERAL || LA13_36 == DOUBLE_QUOTE || (LA13_36 >= FALSE && LA13_36 <= FLOATING_POINT_LITERAL) || LA13_36 == ID || (LA13_36 >= QUOTED_STRING && LA13_36 <= RPAREN) || (LA13_36 >= TIMESTAMP && LA13_36 <= TRUE)) && ((strict == false)))
                        {
                            alt13 = 2;
                        }

                    }

                }
                else if ((LA13_2 == NULL) && ((strict == false)))
                {
                    int LA13_22 = input.LA(3);
                    if ((LA13_22 == DOUBLE_QUOTE) && ((strict == false)))
                    {
                        int LA13_36 = input.LA(4);
                        if ((LA13_36 == DOT) && ((strict == false)))
                        {
                            alt13 = 1;
                        }
                        else if ((LA13_36 == COLON || LA13_36 == DECIMAL_INTEGER_LITERAL || LA13_36 == DOUBLE_QUOTE || (LA13_36 >= FALSE && LA13_36 <= FLOATING_POINT_LITERAL) || LA13_36 == ID || (LA13_36 >= QUOTED_STRING && LA13_36 <= RPAREN) || (LA13_36 >= TIMESTAMP && LA13_36 <= TRUE)) && ((strict == false)))
                        {
                            alt13 = 2;
                        }

                    }

                }
                else if ((LA13_2 == ANY) && ((strict == false)))
                {
                    int LA13_23 = input.LA(3);
                    if ((LA13_23 == DOUBLE_QUOTE) && ((strict == false)))
                    {
                        int LA13_36 = input.LA(4);
                        if ((LA13_36 == DOT) && ((strict == false)))
                        {
                            alt13 = 1;
                        }
                        else if ((LA13_36 == COLON || LA13_36 == DECIMAL_INTEGER_LITERAL || LA13_36 == DOUBLE_QUOTE || (LA13_36 >= FALSE && LA13_36 <= FLOATING_POINT_LITERAL) || LA13_36 == ID || (LA13_36 >= QUOTED_STRING && LA13_36 <= RPAREN) || (LA13_36 >= TIMESTAMP && LA13_36 <= TRUE)) && ((strict == false)))
                        {
                            alt13 = 2;
                        }

                    }

                }
                else if ((LA13_2 == CONTAINS) && ((strict == false)))
                {
                    int LA13_24 = input.LA(3);
                    if ((LA13_24 == DOUBLE_QUOTE) && ((strict == false)))
                    {
                        int LA13_36 = input.LA(4);
                        if ((LA13_36 == DOT) && ((strict == false)))
                        {
                            alt13 = 1;
                        }
                        else if ((LA13_36 == COLON || LA13_36 == DECIMAL_INTEGER_LITERAL || LA13_36 == DOUBLE_QUOTE || (LA13_36 >= FALSE && LA13_36 <= FLOATING_POINT_LITERAL) || LA13_36 == ID || (LA13_36 >= QUOTED_STRING && LA13_36 <= RPAREN) || (LA13_36 >= TIMESTAMP && LA13_36 <= TRUE)) && ((strict == false)))
                        {
                            alt13 = 2;
                        }

                    }

                }
                else if ((LA13_2 == IN_FOLDER) && ((strict == false)))
                {
                    int LA13_25 = input.LA(3);
                    if ((LA13_25 == DOUBLE_QUOTE) && ((strict == false)))
                    {
                        int LA13_36 = input.LA(4);
                        if ((LA13_36 == DOT) && ((strict == false)))
                        {
                            alt13 = 1;
                        }
                        else if ((LA13_36 == COLON || LA13_36 == DECIMAL_INTEGER_LITERAL || LA13_36 == DOUBLE_QUOTE || (LA13_36 >= FALSE && LA13_36 <= FLOATING_POINT_LITERAL) || LA13_36 == ID || (LA13_36 >= QUOTED_STRING && LA13_36 <= RPAREN) || (LA13_36 >= TIMESTAMP && LA13_36 <= TRUE)) && ((strict == false)))
                        {
                            alt13 = 2;
                        }

                    }

                }
                else if ((LA13_2 == IN_TREE) && ((strict == false)))
                {
                    int LA13_26 = input.LA(3);
                    if ((LA13_26 == DOUBLE_QUOTE) && ((strict == false)))
                    {
                        int LA13_36 = input.LA(4);
                        if ((LA13_36 == DOT) && ((strict == false)))
                        {
                            alt13 = 1;
                        }
                        else if ((LA13_36 == COLON || LA13_36 == DECIMAL_INTEGER_LITERAL || LA13_36 == DOUBLE_QUOTE || (LA13_36 >= FALSE && LA13_36 <= FLOATING_POINT_LITERAL) || LA13_36 == ID || (LA13_36 >= QUOTED_STRING && LA13_36 <= RPAREN) || (LA13_36 >= TIMESTAMP && LA13_36 <= TRUE)) && ((strict == false)))
                        {
                            alt13 = 2;
                        }

                    }

                }
                else if ((LA13_2 == ORDER) && ((strict == false)))
                {
                    int LA13_27 = input.LA(3);
                    if ((LA13_27 == DOUBLE_QUOTE) && ((strict == false)))
                    {
                        int LA13_36 = input.LA(4);
                        if ((LA13_36 == DOT) && ((strict == false)))
                        {
                            alt13 = 1;
                        }
                        else if ((LA13_36 == COLON || LA13_36 == DECIMAL_INTEGER_LITERAL || LA13_36 == DOUBLE_QUOTE || (LA13_36 >= FALSE && LA13_36 <= FLOATING_POINT_LITERAL) || LA13_36 == ID || (LA13_36 >= QUOTED_STRING && LA13_36 <= RPAREN) || (LA13_36 >= TIMESTAMP && LA13_36 <= TRUE)) && ((strict == false)))
                        {
                            alt13 = 2;
                        }

                    }

                }
                else if ((LA13_2 == BY) && ((strict == false)))
                {
                    int LA13_28 = input.LA(3);
                    if ((LA13_28 == DOUBLE_QUOTE) && ((strict == false)))
                    {
                        int LA13_36 = input.LA(4);
                        if ((LA13_36 == DOT) && ((strict == false)))
                        {
                            alt13 = 1;
                        }
                        else if ((LA13_36 == COLON || LA13_36 == DECIMAL_INTEGER_LITERAL || LA13_36 == DOUBLE_QUOTE || (LA13_36 >= FALSE && LA13_36 <= FLOATING_POINT_LITERAL) || LA13_36 == ID || (LA13_36 >= QUOTED_STRING && LA13_36 <= RPAREN) || (LA13_36 >= TIMESTAMP && LA13_36 <= TRUE)) && ((strict == false)))
                        {
                            alt13 = 2;
                        }

                    }

                }
                else if ((LA13_2 == ASC) && ((strict == false)))
                {
                    int LA13_29 = input.LA(3);
                    if ((LA13_29 == DOUBLE_QUOTE) && ((strict == false)))
                    {
                        int LA13_36 = input.LA(4);
                        if ((LA13_36 == DOT) && ((strict == false)))
                        {
                            alt13 = 1;
                        }
                        else if ((LA13_36 == COLON || LA13_36 == DECIMAL_INTEGER_LITERAL || LA13_36 == DOUBLE_QUOTE || (LA13_36 >= FALSE && LA13_36 <= FLOATING_POINT_LITERAL) || LA13_36 == ID || (LA13_36 >= QUOTED_STRING && LA13_36 <= RPAREN) || (LA13_36 >= TIMESTAMP && LA13_36 <= TRUE)) && ((strict == false)))
                        {
                            alt13 = 2;
                        }

                    }

                }
                else if ((LA13_2 == DESC) && ((strict == false)))
                {
                    int LA13_30 = input.LA(3);
                    if ((LA13_30 == DOUBLE_QUOTE) && ((strict == false)))
                    {
                        int LA13_36 = input.LA(4);
                        if ((LA13_36 == DOT) && ((strict == false)))
                        {
                            alt13 = 1;
                        }
                        else if ((LA13_36 == COLON || LA13_36 == DECIMAL_INTEGER_LITERAL || LA13_36 == DOUBLE_QUOTE || (LA13_36 >= FALSE && LA13_36 <= FLOATING_POINT_LITERAL) || LA13_36 == ID || (LA13_36 >= QUOTED_STRING && LA13_36 <= RPAREN) || (LA13_36 >= TIMESTAMP && LA13_36 <= TRUE)) && ((strict == false)))
                        {
                            alt13 = 2;
                        }

                    }

                }
                else if ((LA13_2 == TIMESTAMP) && ((strict == false)))
                {
                    int LA13_31 = input.LA(3);
                    if ((LA13_31 == DOUBLE_QUOTE) && ((strict == false)))
                    {
                        int LA13_36 = input.LA(4);
                        if ((LA13_36 == DOT) && ((strict == false)))
                        {
                            alt13 = 1;
                        }
                        else if ((LA13_36 == COLON || LA13_36 == DECIMAL_INTEGER_LITERAL || LA13_36 == DOUBLE_QUOTE || (LA13_36 >= FALSE && LA13_36 <= FLOATING_POINT_LITERAL) || LA13_36 == ID || (LA13_36 >= QUOTED_STRING && LA13_36 <= RPAREN) || (LA13_36 >= TIMESTAMP && LA13_36 <= TRUE)) && ((strict == false)))
                        {
                            alt13 = 2;
                        }

                    }

                }
                else if ((LA13_2 == TRUE) && ((strict == false)))
                {
                    int LA13_32 = input.LA(3);
                    if ((LA13_32 == DOUBLE_QUOTE) && ((strict == false)))
                    {
                        int LA13_36 = input.LA(4);
                        if ((LA13_36 == DOT) && ((strict == false)))
                        {
                            alt13 = 1;
                        }
                        else if ((LA13_36 == COLON || LA13_36 == DECIMAL_INTEGER_LITERAL || LA13_36 == DOUBLE_QUOTE || (LA13_36 >= FALSE && LA13_36 <= FLOATING_POINT_LITERAL) || LA13_36 == ID || (LA13_36 >= QUOTED_STRING && LA13_36 <= RPAREN) || (LA13_36 >= TIMESTAMP && LA13_36 <= TRUE)) && ((strict == false)))
                        {
                            alt13 = 2;
                        }

                    }

                }
                else if ((LA13_2 == FALSE) && ((strict == false)))
                {
                    int LA13_33 = input.LA(3);
                    if ((LA13_33 == DOUBLE_QUOTE) && ((strict == false)))
                    {
                        int LA13_36 = input.LA(4);
                        if ((LA13_36 == DOT) && ((strict == false)))
                        {
                            alt13 = 1;
                        }
                        else if ((LA13_36 == COLON || LA13_36 == DECIMAL_INTEGER_LITERAL || LA13_36 == DOUBLE_QUOTE || (LA13_36 >= FALSE && LA13_36 <= FLOATING_POINT_LITERAL) || LA13_36 == ID || (LA13_36 >= QUOTED_STRING && LA13_36 <= RPAREN) || (LA13_36 >= TIMESTAMP && LA13_36 <= TRUE)) && ((strict == false)))
                        {
                            alt13 = 2;
                        }

                    }

                }
                else if ((LA13_2 == SCORE) && ((strict == false)))
                {
                    int LA13_34 = input.LA(3);
                    if ((LA13_34 == DOUBLE_QUOTE) && ((strict == false)))
                    {
                        int LA13_36 = input.LA(4);
                        if ((LA13_36 == DOT) && ((strict == false)))
                        {
                            alt13 = 1;
                        }
                        else if ((LA13_36 == COLON || LA13_36 == DECIMAL_INTEGER_LITERAL || LA13_36 == DOUBLE_QUOTE || (LA13_36 >= FALSE && LA13_36 <= FLOATING_POINT_LITERAL) || LA13_36 == ID || (LA13_36 >= QUOTED_STRING && LA13_36 <= RPAREN) || (LA13_36 >= TIMESTAMP && LA13_36 <= TRUE)) && ((strict == false)))
                        {
                            alt13 = 2;
                        }

                    }

                }
                else if ((LA13_2 == ID) && ((strict == false)))
                {
                    int LA13_35 = input.LA(3);
                    if ((LA13_35 == DOUBLE_QUOTE) && ((strict == false)))
                    {
                        int LA13_36 = input.LA(4);
                        if ((LA13_36 == DOT) && ((strict == false)))
                        {
                            alt13 = 1;
                        }
                        else if ((LA13_36 == COLON || LA13_36 == DECIMAL_INTEGER_LITERAL || LA13_36 == DOUBLE_QUOTE || (LA13_36 >= FALSE && LA13_36 <= FLOATING_POINT_LITERAL) || LA13_36 == ID || (LA13_36 >= QUOTED_STRING && LA13_36 <= RPAREN) || (LA13_36 >= TIMESTAMP && LA13_36 <= TRUE)) && ((strict == false)))
                        {
                            alt13 = 2;
                        }

                    }

                }

            }
            else if ((LA13_0 == DECIMAL_INTEGER_LITERAL || (LA13_0 >= FALSE && LA13_0 <= FLOATING_POINT_LITERAL) || LA13_0 == QUOTED_STRING || (LA13_0 >= TIMESTAMP && LA13_0 <= TRUE)))
            {
                alt13 = 3;
            }
            else if ((LA13_0 == COLON) && ((strict == false)))
            {
                alt13 = 3;
            }

            switch (alt13)
            {
            case 1:
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:379:9: qualifier DOT columnName
            {
                pushFollow(FOLLOW_qualifier_in_functionArgument1211);
                qualifier27 = qualifier();
                state._fsp--;
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                    stream_qualifier.add(qualifier27.getTree());
                DOT28 = (Token) match(input, DOT, FOLLOW_DOT_in_functionArgument1213);
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                    stream_DOT.add(DOT28);

                pushFollow(FOLLOW_columnName_in_functionArgument1215);
                columnName29 = columnName();
                state._fsp--;
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                    stream_columnName.add(columnName29.getTree());
                // AST REWRITE
                // elements: columnName, qualifier
                // token labels:
                // rule labels: retval
                // token list labels:
                // rule list labels:
                // wildcard labels:
                if (state.backtracking == 0)
                {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval = new RewriteRuleSubtreeStream(adaptor, "rule retval", retval != null ? retval.getTree() : null);

                    root_0 = (Object) adaptor.nil();
                    // 380:17: -> ^( COLUMN_REF columnName qualifier )
                    {
                        // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:381:25: ^( COLUMN_REF columnName qualifier )
                        {
                            Object root_1 = (Object) adaptor.nil();
                            root_1 = (Object) adaptor.becomeRoot((Object) adaptor.create(COLUMN_REF, "COLUMN_REF"), root_1);
                            adaptor.addChild(root_1, stream_columnName.nextTree());
                            adaptor.addChild(root_1, stream_qualifier.nextTree());
                            adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;
                }

            }
                break;
            case 2:
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:382:11: identifier
            {
                root_0 = (Object) adaptor.nil();

                pushFollow(FOLLOW_identifier_in_functionArgument1277);
                identifier30 = identifier();
                state._fsp--;
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                    adaptor.addChild(root_0, identifier30.getTree());

            }
                break;
            case 3:
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:383:11: literalOrParameterName
            {
                root_0 = (Object) adaptor.nil();

                pushFollow(FOLLOW_literalOrParameterName_in_functionArgument1289);
                literalOrParameterName31 = literalOrParameterName();
                state._fsp--;
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                    adaptor.addChild(root_0, literalOrParameterName31.getTree());

            }
                break;

            }
            retval.stop = input.LT(-1);

            if (state.backtracking == 0)
            {
                retval.tree = (Object) adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        catch (RecognitionException e)
        {
            throw new CmisInvalidArgumentException(getErrorString(e), e);
        }

        finally
        {
            // do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "functionArgument"

    public static class qualifier_return extends ParserRuleReturnScope
    {
        Object tree;

        @Override
        public Object getTree()
        {
            return tree;
        }
    };

    // $ANTLR start "qualifier"
    // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:386:1: qualifier : ( ( tableName )=> tableName -> tableName | correlationName -> correlationName );
    public final CMISParser.qualifier_return qualifier() throws RecognitionException
    {
        CMISParser.qualifier_return retval = new CMISParser.qualifier_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        ParserRuleReturnScope tableName32 = null;
        ParserRuleReturnScope correlationName33 = null;

        RewriteRuleSubtreeStream stream_correlationName = new RewriteRuleSubtreeStream(adaptor, "rule correlationName");
        RewriteRuleSubtreeStream stream_tableName = new RewriteRuleSubtreeStream(adaptor, "rule tableName");

        try
        {
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:387:9: ( ( tableName )=> tableName -> tableName | correlationName -> correlationName )
            int alt14 = 2;
            int LA14_0 = input.LA(1);
            if ((LA14_0 == ID))
            {
                int LA14_1 = input.LA(2);
                if ((synpred1_CMIS()))
                {
                    alt14 = 1;
                }
                else if ((true))
                {
                    alt14 = 2;
                }

            }
            else if ((LA14_0 == DOUBLE_QUOTE) && ((strict == false)))
            {
                int LA14_2 = input.LA(2);
                if ((LA14_2 == SELECT) && ((strict == false)))
                {
                    int LA14_5 = input.LA(3);
                    if ((LA14_5 == DOUBLE_QUOTE) && ((strict == false)))
                    {
                        int LA14_34 = input.LA(4);
                        if ((((strict == false) && synpred1_CMIS())))
                        {
                            alt14 = 1;
                        }
                        else if (((strict == false)))
                        {
                            alt14 = 2;
                        }

                        else
                        {
                            if (state.backtracking > 0)
                            {
                                state.failed = true;
                                return retval;
                            }
                            int nvaeMark = input.mark();
                            try
                            {
                                for (int nvaeConsume = 0; nvaeConsume < 4 - 1; nvaeConsume++)
                                {
                                    input.consume();
                                }
                                NoViableAltException nvae = new NoViableAltException("", 14, 34, input);
                                throw nvae;
                            }
                            finally
                            {
                                input.rewind(nvaeMark);
                            }
                        }

                    }

                }
                else if ((LA14_2 == AS) && ((strict == false)))
                {
                    int LA14_6 = input.LA(3);
                    if ((LA14_6 == DOUBLE_QUOTE) && ((strict == false)))
                    {
                        int LA14_34 = input.LA(4);
                        if ((((strict == false) && synpred1_CMIS())))
                        {
                            alt14 = 1;
                        }
                        else if (((strict == false)))
                        {
                            alt14 = 2;
                        }

                        else
                        {
                            if (state.backtracking > 0)
                            {
                                state.failed = true;
                                return retval;
                            }
                            int nvaeMark = input.mark();
                            try
                            {
                                for (int nvaeConsume = 0; nvaeConsume < 4 - 1; nvaeConsume++)
                                {
                                    input.consume();
                                }
                                NoViableAltException nvae = new NoViableAltException("", 14, 34, input);
                                throw nvae;
                            }
                            finally
                            {
                                input.rewind(nvaeMark);
                            }
                        }

                    }

                }
                else if ((LA14_2 == FROM) && ((strict == false)))
                {
                    int LA14_7 = input.LA(3);
                    if ((LA14_7 == DOUBLE_QUOTE) && ((strict == false)))
                    {
                        int LA14_34 = input.LA(4);
                        if ((((strict == false) && synpred1_CMIS())))
                        {
                            alt14 = 1;
                        }
                        else if (((strict == false)))
                        {
                            alt14 = 2;
                        }

                        else
                        {
                            if (state.backtracking > 0)
                            {
                                state.failed = true;
                                return retval;
                            }
                            int nvaeMark = input.mark();
                            try
                            {
                                for (int nvaeConsume = 0; nvaeConsume < 4 - 1; nvaeConsume++)
                                {
                                    input.consume();
                                }
                                NoViableAltException nvae = new NoViableAltException("", 14, 34, input);
                                throw nvae;
                            }
                            finally
                            {
                                input.rewind(nvaeMark);
                            }
                        }

                    }

                }
                else if ((LA14_2 == JOIN) && ((strict == false)))
                {
                    int LA14_8 = input.LA(3);
                    if ((LA14_8 == DOUBLE_QUOTE) && ((strict == false)))
                    {
                        int LA14_34 = input.LA(4);
                        if ((((strict == false) && synpred1_CMIS())))
                        {
                            alt14 = 1;
                        }
                        else if (((strict == false)))
                        {
                            alt14 = 2;
                        }

                        else
                        {
                            if (state.backtracking > 0)
                            {
                                state.failed = true;
                                return retval;
                            }
                            int nvaeMark = input.mark();
                            try
                            {
                                for (int nvaeConsume = 0; nvaeConsume < 4 - 1; nvaeConsume++)
                                {
                                    input.consume();
                                }
                                NoViableAltException nvae = new NoViableAltException("", 14, 34, input);
                                throw nvae;
                            }
                            finally
                            {
                                input.rewind(nvaeMark);
                            }
                        }

                    }

                }
                else if ((LA14_2 == INNER) && ((strict == false)))
                {
                    int LA14_9 = input.LA(3);
                    if ((LA14_9 == DOUBLE_QUOTE) && ((strict == false)))
                    {
                        int LA14_34 = input.LA(4);
                        if ((((strict == false) && synpred1_CMIS())))
                        {
                            alt14 = 1;
                        }
                        else if (((strict == false)))
                        {
                            alt14 = 2;
                        }

                        else
                        {
                            if (state.backtracking > 0)
                            {
                                state.failed = true;
                                return retval;
                            }
                            int nvaeMark = input.mark();
                            try
                            {
                                for (int nvaeConsume = 0; nvaeConsume < 4 - 1; nvaeConsume++)
                                {
                                    input.consume();
                                }
                                NoViableAltException nvae = new NoViableAltException("", 14, 34, input);
                                throw nvae;
                            }
                            finally
                            {
                                input.rewind(nvaeMark);
                            }
                        }

                    }

                }
                else if ((LA14_2 == LEFT) && ((strict == false)))
                {
                    int LA14_10 = input.LA(3);
                    if ((LA14_10 == DOUBLE_QUOTE) && ((strict == false)))
                    {
                        int LA14_34 = input.LA(4);
                        if ((((strict == false) && synpred1_CMIS())))
                        {
                            alt14 = 1;
                        }
                        else if (((strict == false)))
                        {
                            alt14 = 2;
                        }

                        else
                        {
                            if (state.backtracking > 0)
                            {
                                state.failed = true;
                                return retval;
                            }
                            int nvaeMark = input.mark();
                            try
                            {
                                for (int nvaeConsume = 0; nvaeConsume < 4 - 1; nvaeConsume++)
                                {
                                    input.consume();
                                }
                                NoViableAltException nvae = new NoViableAltException("", 14, 34, input);
                                throw nvae;
                            }
                            finally
                            {
                                input.rewind(nvaeMark);
                            }
                        }

                    }

                }
                else if ((LA14_2 == OUTER) && ((strict == false)))
                {
                    int LA14_11 = input.LA(3);
                    if ((LA14_11 == DOUBLE_QUOTE) && ((strict == false)))
                    {
                        int LA14_34 = input.LA(4);
                        if ((((strict == false) && synpred1_CMIS())))
                        {
                            alt14 = 1;
                        }
                        else if (((strict == false)))
                        {
                            alt14 = 2;
                        }

                        else
                        {
                            if (state.backtracking > 0)
                            {
                                state.failed = true;
                                return retval;
                            }
                            int nvaeMark = input.mark();
                            try
                            {
                                for (int nvaeConsume = 0; nvaeConsume < 4 - 1; nvaeConsume++)
                                {
                                    input.consume();
                                }
                                NoViableAltException nvae = new NoViableAltException("", 14, 34, input);
                                throw nvae;
                            }
                            finally
                            {
                                input.rewind(nvaeMark);
                            }
                        }

                    }

                }
                else if ((LA14_2 == ON) && ((strict == false)))
                {
                    int LA14_12 = input.LA(3);
                    if ((LA14_12 == DOUBLE_QUOTE) && ((strict == false)))
                    {
                        int LA14_34 = input.LA(4);
                        if ((((strict == false) && synpred1_CMIS())))
                        {
                            alt14 = 1;
                        }
                        else if (((strict == false)))
                        {
                            alt14 = 2;
                        }

                        else
                        {
                            if (state.backtracking > 0)
                            {
                                state.failed = true;
                                return retval;
                            }
                            int nvaeMark = input.mark();
                            try
                            {
                                for (int nvaeConsume = 0; nvaeConsume < 4 - 1; nvaeConsume++)
                                {
                                    input.consume();
                                }
                                NoViableAltException nvae = new NoViableAltException("", 14, 34, input);
                                throw nvae;
                            }
                            finally
                            {
                                input.rewind(nvaeMark);
                            }
                        }

                    }

                }
                else if ((LA14_2 == WHERE) && ((strict == false)))
                {
                    int LA14_13 = input.LA(3);
                    if ((LA14_13 == DOUBLE_QUOTE) && ((strict == false)))
                    {
                        int LA14_34 = input.LA(4);
                        if ((((strict == false) && synpred1_CMIS())))
                        {
                            alt14 = 1;
                        }
                        else if (((strict == false)))
                        {
                            alt14 = 2;
                        }

                        else
                        {
                            if (state.backtracking > 0)
                            {
                                state.failed = true;
                                return retval;
                            }
                            int nvaeMark = input.mark();
                            try
                            {
                                for (int nvaeConsume = 0; nvaeConsume < 4 - 1; nvaeConsume++)
                                {
                                    input.consume();
                                }
                                NoViableAltException nvae = new NoViableAltException("", 14, 34, input);
                                throw nvae;
                            }
                            finally
                            {
                                input.rewind(nvaeMark);
                            }
                        }

                    }

                }
                else if ((LA14_2 == OR) && ((strict == false)))
                {
                    int LA14_14 = input.LA(3);
                    if ((LA14_14 == DOUBLE_QUOTE) && ((strict == false)))
                    {
                        int LA14_34 = input.LA(4);
                        if ((((strict == false) && synpred1_CMIS())))
                        {
                            alt14 = 1;
                        }
                        else if (((strict == false)))
                        {
                            alt14 = 2;
                        }

                        else
                        {
                            if (state.backtracking > 0)
                            {
                                state.failed = true;
                                return retval;
                            }
                            int nvaeMark = input.mark();
                            try
                            {
                                for (int nvaeConsume = 0; nvaeConsume < 4 - 1; nvaeConsume++)
                                {
                                    input.consume();
                                }
                                NoViableAltException nvae = new NoViableAltException("", 14, 34, input);
                                throw nvae;
                            }
                            finally
                            {
                                input.rewind(nvaeMark);
                            }
                        }

                    }

                }
                else if ((LA14_2 == AND) && ((strict == false)))
                {
                    int LA14_15 = input.LA(3);
                    if ((LA14_15 == DOUBLE_QUOTE) && ((strict == false)))
                    {
                        int LA14_34 = input.LA(4);
                        if ((((strict == false) && synpred1_CMIS())))
                        {
                            alt14 = 1;
                        }
                        else if (((strict == false)))
                        {
                            alt14 = 2;
                        }

                        else
                        {
                            if (state.backtracking > 0)
                            {
                                state.failed = true;
                                return retval;
                            }
                            int nvaeMark = input.mark();
                            try
                            {
                                for (int nvaeConsume = 0; nvaeConsume < 4 - 1; nvaeConsume++)
                                {
                                    input.consume();
                                }
                                NoViableAltException nvae = new NoViableAltException("", 14, 34, input);
                                throw nvae;
                            }
                            finally
                            {
                                input.rewind(nvaeMark);
                            }
                        }

                    }

                }
                else if ((LA14_2 == NOT) && ((strict == false)))
                {
                    int LA14_16 = input.LA(3);
                    if ((LA14_16 == DOUBLE_QUOTE) && ((strict == false)))
                    {
                        int LA14_34 = input.LA(4);
                        if ((((strict == false) && synpred1_CMIS())))
                        {
                            alt14 = 1;
                        }
                        else if (((strict == false)))
                        {
                            alt14 = 2;
                        }

                        else
                        {
                            if (state.backtracking > 0)
                            {
                                state.failed = true;
                                return retval;
                            }
                            int nvaeMark = input.mark();
                            try
                            {
                                for (int nvaeConsume = 0; nvaeConsume < 4 - 1; nvaeConsume++)
                                {
                                    input.consume();
                                }
                                NoViableAltException nvae = new NoViableAltException("", 14, 34, input);
                                throw nvae;
                            }
                            finally
                            {
                                input.rewind(nvaeMark);
                            }
                        }

                    }

                }
                else if ((LA14_2 == IN) && ((strict == false)))
                {
                    int LA14_17 = input.LA(3);
                    if ((LA14_17 == DOUBLE_QUOTE) && ((strict == false)))
                    {
                        int LA14_34 = input.LA(4);
                        if ((((strict == false) && synpred1_CMIS())))
                        {
                            alt14 = 1;
                        }
                        else if (((strict == false)))
                        {
                            alt14 = 2;
                        }

                        else
                        {
                            if (state.backtracking > 0)
                            {
                                state.failed = true;
                                return retval;
                            }
                            int nvaeMark = input.mark();
                            try
                            {
                                for (int nvaeConsume = 0; nvaeConsume < 4 - 1; nvaeConsume++)
                                {
                                    input.consume();
                                }
                                NoViableAltException nvae = new NoViableAltException("", 14, 34, input);
                                throw nvae;
                            }
                            finally
                            {
                                input.rewind(nvaeMark);
                            }
                        }

                    }

                }
                else if ((LA14_2 == LIKE) && ((strict == false)))
                {
                    int LA14_18 = input.LA(3);
                    if ((LA14_18 == DOUBLE_QUOTE) && ((strict == false)))
                    {
                        int LA14_34 = input.LA(4);
                        if ((((strict == false) && synpred1_CMIS())))
                        {
                            alt14 = 1;
                        }
                        else if (((strict == false)))
                        {
                            alt14 = 2;
                        }

                        else
                        {
                            if (state.backtracking > 0)
                            {
                                state.failed = true;
                                return retval;
                            }
                            int nvaeMark = input.mark();
                            try
                            {
                                for (int nvaeConsume = 0; nvaeConsume < 4 - 1; nvaeConsume++)
                                {
                                    input.consume();
                                }
                                NoViableAltException nvae = new NoViableAltException("", 14, 34, input);
                                throw nvae;
                            }
                            finally
                            {
                                input.rewind(nvaeMark);
                            }
                        }

                    }

                }
                else if ((LA14_2 == IS) && ((strict == false)))
                {
                    int LA14_19 = input.LA(3);
                    if ((LA14_19 == DOUBLE_QUOTE) && ((strict == false)))
                    {
                        int LA14_34 = input.LA(4);
                        if ((((strict == false) && synpred1_CMIS())))
                        {
                            alt14 = 1;
                        }
                        else if (((strict == false)))
                        {
                            alt14 = 2;
                        }

                        else
                        {
                            if (state.backtracking > 0)
                            {
                                state.failed = true;
                                return retval;
                            }
                            int nvaeMark = input.mark();
                            try
                            {
                                for (int nvaeConsume = 0; nvaeConsume < 4 - 1; nvaeConsume++)
                                {
                                    input.consume();
                                }
                                NoViableAltException nvae = new NoViableAltException("", 14, 34, input);
                                throw nvae;
                            }
                            finally
                            {
                                input.rewind(nvaeMark);
                            }
                        }

                    }

                }
                else if ((LA14_2 == NULL) && ((strict == false)))
                {
                    int LA14_20 = input.LA(3);
                    if ((LA14_20 == DOUBLE_QUOTE) && ((strict == false)))
                    {
                        int LA14_34 = input.LA(4);
                        if ((((strict == false) && synpred1_CMIS())))
                        {
                            alt14 = 1;
                        }
                        else if (((strict == false)))
                        {
                            alt14 = 2;
                        }

                        else
                        {
                            if (state.backtracking > 0)
                            {
                                state.failed = true;
                                return retval;
                            }
                            int nvaeMark = input.mark();
                            try
                            {
                                for (int nvaeConsume = 0; nvaeConsume < 4 - 1; nvaeConsume++)
                                {
                                    input.consume();
                                }
                                NoViableAltException nvae = new NoViableAltException("", 14, 34, input);
                                throw nvae;
                            }
                            finally
                            {
                                input.rewind(nvaeMark);
                            }
                        }

                    }

                }
                else if ((LA14_2 == ANY) && ((strict == false)))
                {
                    int LA14_21 = input.LA(3);
                    if ((LA14_21 == DOUBLE_QUOTE) && ((strict == false)))
                    {
                        int LA14_34 = input.LA(4);
                        if ((((strict == false) && synpred1_CMIS())))
                        {
                            alt14 = 1;
                        }
                        else if (((strict == false)))
                        {
                            alt14 = 2;
                        }

                        else
                        {
                            if (state.backtracking > 0)
                            {
                                state.failed = true;
                                return retval;
                            }
                            int nvaeMark = input.mark();
                            try
                            {
                                for (int nvaeConsume = 0; nvaeConsume < 4 - 1; nvaeConsume++)
                                {
                                    input.consume();
                                }
                                NoViableAltException nvae = new NoViableAltException("", 14, 34, input);
                                throw nvae;
                            }
                            finally
                            {
                                input.rewind(nvaeMark);
                            }
                        }

                    }

                }
                else if ((LA14_2 == CONTAINS) && ((strict == false)))
                {
                    int LA14_22 = input.LA(3);
                    if ((LA14_22 == DOUBLE_QUOTE) && ((strict == false)))
                    {
                        int LA14_34 = input.LA(4);
                        if ((((strict == false) && synpred1_CMIS())))
                        {
                            alt14 = 1;
                        }
                        else if (((strict == false)))
                        {
                            alt14 = 2;
                        }

                        else
                        {
                            if (state.backtracking > 0)
                            {
                                state.failed = true;
                                return retval;
                            }
                            int nvaeMark = input.mark();
                            try
                            {
                                for (int nvaeConsume = 0; nvaeConsume < 4 - 1; nvaeConsume++)
                                {
                                    input.consume();
                                }
                                NoViableAltException nvae = new NoViableAltException("", 14, 34, input);
                                throw nvae;
                            }
                            finally
                            {
                                input.rewind(nvaeMark);
                            }
                        }

                    }

                }
                else if ((LA14_2 == IN_FOLDER) && ((strict == false)))
                {
                    int LA14_23 = input.LA(3);
                    if ((LA14_23 == DOUBLE_QUOTE) && ((strict == false)))
                    {
                        int LA14_34 = input.LA(4);
                        if ((((strict == false) && synpred1_CMIS())))
                        {
                            alt14 = 1;
                        }
                        else if (((strict == false)))
                        {
                            alt14 = 2;
                        }

                        else
                        {
                            if (state.backtracking > 0)
                            {
                                state.failed = true;
                                return retval;
                            }
                            int nvaeMark = input.mark();
                            try
                            {
                                for (int nvaeConsume = 0; nvaeConsume < 4 - 1; nvaeConsume++)
                                {
                                    input.consume();
                                }
                                NoViableAltException nvae = new NoViableAltException("", 14, 34, input);
                                throw nvae;
                            }
                            finally
                            {
                                input.rewind(nvaeMark);
                            }
                        }

                    }

                }
                else if ((LA14_2 == IN_TREE) && ((strict == false)))
                {
                    int LA14_24 = input.LA(3);
                    if ((LA14_24 == DOUBLE_QUOTE) && ((strict == false)))
                    {
                        int LA14_34 = input.LA(4);
                        if ((((strict == false) && synpred1_CMIS())))
                        {
                            alt14 = 1;
                        }
                        else if (((strict == false)))
                        {
                            alt14 = 2;
                        }

                        else
                        {
                            if (state.backtracking > 0)
                            {
                                state.failed = true;
                                return retval;
                            }
                            int nvaeMark = input.mark();
                            try
                            {
                                for (int nvaeConsume = 0; nvaeConsume < 4 - 1; nvaeConsume++)
                                {
                                    input.consume();
                                }
                                NoViableAltException nvae = new NoViableAltException("", 14, 34, input);
                                throw nvae;
                            }
                            finally
                            {
                                input.rewind(nvaeMark);
                            }
                        }

                    }

                }
                else if ((LA14_2 == ORDER) && ((strict == false)))
                {
                    int LA14_25 = input.LA(3);
                    if ((LA14_25 == DOUBLE_QUOTE) && ((strict == false)))
                    {
                        int LA14_34 = input.LA(4);
                        if ((((strict == false) && synpred1_CMIS())))
                        {
                            alt14 = 1;
                        }
                        else if (((strict == false)))
                        {
                            alt14 = 2;
                        }

                        else
                        {
                            if (state.backtracking > 0)
                            {
                                state.failed = true;
                                return retval;
                            }
                            int nvaeMark = input.mark();
                            try
                            {
                                for (int nvaeConsume = 0; nvaeConsume < 4 - 1; nvaeConsume++)
                                {
                                    input.consume();
                                }
                                NoViableAltException nvae = new NoViableAltException("", 14, 34, input);
                                throw nvae;
                            }
                            finally
                            {
                                input.rewind(nvaeMark);
                            }
                        }

                    }

                }
                else if ((LA14_2 == BY) && ((strict == false)))
                {
                    int LA14_26 = input.LA(3);
                    if ((LA14_26 == DOUBLE_QUOTE) && ((strict == false)))
                    {
                        int LA14_34 = input.LA(4);
                        if ((((strict == false) && synpred1_CMIS())))
                        {
                            alt14 = 1;
                        }
                        else if (((strict == false)))
                        {
                            alt14 = 2;
                        }

                        else
                        {
                            if (state.backtracking > 0)
                            {
                                state.failed = true;
                                return retval;
                            }
                            int nvaeMark = input.mark();
                            try
                            {
                                for (int nvaeConsume = 0; nvaeConsume < 4 - 1; nvaeConsume++)
                                {
                                    input.consume();
                                }
                                NoViableAltException nvae = new NoViableAltException("", 14, 34, input);
                                throw nvae;
                            }
                            finally
                            {
                                input.rewind(nvaeMark);
                            }
                        }

                    }

                }
                else if ((LA14_2 == ASC) && ((strict == false)))
                {
                    int LA14_27 = input.LA(3);
                    if ((LA14_27 == DOUBLE_QUOTE) && ((strict == false)))
                    {
                        int LA14_34 = input.LA(4);
                        if ((((strict == false) && synpred1_CMIS())))
                        {
                            alt14 = 1;
                        }
                        else if (((strict == false)))
                        {
                            alt14 = 2;
                        }

                        else
                        {
                            if (state.backtracking > 0)
                            {
                                state.failed = true;
                                return retval;
                            }
                            int nvaeMark = input.mark();
                            try
                            {
                                for (int nvaeConsume = 0; nvaeConsume < 4 - 1; nvaeConsume++)
                                {
                                    input.consume();
                                }
                                NoViableAltException nvae = new NoViableAltException("", 14, 34, input);
                                throw nvae;
                            }
                            finally
                            {
                                input.rewind(nvaeMark);
                            }
                        }

                    }

                }
                else if ((LA14_2 == DESC) && ((strict == false)))
                {
                    int LA14_28 = input.LA(3);
                    if ((LA14_28 == DOUBLE_QUOTE) && ((strict == false)))
                    {
                        int LA14_34 = input.LA(4);
                        if ((((strict == false) && synpred1_CMIS())))
                        {
                            alt14 = 1;
                        }
                        else if (((strict == false)))
                        {
                            alt14 = 2;
                        }

                        else
                        {
                            if (state.backtracking > 0)
                            {
                                state.failed = true;
                                return retval;
                            }
                            int nvaeMark = input.mark();
                            try
                            {
                                for (int nvaeConsume = 0; nvaeConsume < 4 - 1; nvaeConsume++)
                                {
                                    input.consume();
                                }
                                NoViableAltException nvae = new NoViableAltException("", 14, 34, input);
                                throw nvae;
                            }
                            finally
                            {
                                input.rewind(nvaeMark);
                            }
                        }

                    }

                }
                else if ((LA14_2 == TIMESTAMP) && ((strict == false)))
                {
                    int LA14_29 = input.LA(3);
                    if ((LA14_29 == DOUBLE_QUOTE) && ((strict == false)))
                    {
                        int LA14_34 = input.LA(4);
                        if ((((strict == false) && synpred1_CMIS())))
                        {
                            alt14 = 1;
                        }
                        else if (((strict == false)))
                        {
                            alt14 = 2;
                        }

                        else
                        {
                            if (state.backtracking > 0)
                            {
                                state.failed = true;
                                return retval;
                            }
                            int nvaeMark = input.mark();
                            try
                            {
                                for (int nvaeConsume = 0; nvaeConsume < 4 - 1; nvaeConsume++)
                                {
                                    input.consume();
                                }
                                NoViableAltException nvae = new NoViableAltException("", 14, 34, input);
                                throw nvae;
                            }
                            finally
                            {
                                input.rewind(nvaeMark);
                            }
                        }

                    }

                }
                else if ((LA14_2 == TRUE) && ((strict == false)))
                {
                    int LA14_30 = input.LA(3);
                    if ((LA14_30 == DOUBLE_QUOTE) && ((strict == false)))
                    {
                        int LA14_34 = input.LA(4);
                        if ((((strict == false) && synpred1_CMIS())))
                        {
                            alt14 = 1;
                        }
                        else if (((strict == false)))
                        {
                            alt14 = 2;
                        }

                        else
                        {
                            if (state.backtracking > 0)
                            {
                                state.failed = true;
                                return retval;
                            }
                            int nvaeMark = input.mark();
                            try
                            {
                                for (int nvaeConsume = 0; nvaeConsume < 4 - 1; nvaeConsume++)
                                {
                                    input.consume();
                                }
                                NoViableAltException nvae = new NoViableAltException("", 14, 34, input);
                                throw nvae;
                            }
                            finally
                            {
                                input.rewind(nvaeMark);
                            }
                        }

                    }

                }
                else if ((LA14_2 == FALSE) && ((strict == false)))
                {
                    int LA14_31 = input.LA(3);
                    if ((LA14_31 == DOUBLE_QUOTE) && ((strict == false)))
                    {
                        int LA14_34 = input.LA(4);
                        if ((((strict == false) && synpred1_CMIS())))
                        {
                            alt14 = 1;
                        }
                        else if (((strict == false)))
                        {
                            alt14 = 2;
                        }

                        else
                        {
                            if (state.backtracking > 0)
                            {
                                state.failed = true;
                                return retval;
                            }
                            int nvaeMark = input.mark();
                            try
                            {
                                for (int nvaeConsume = 0; nvaeConsume < 4 - 1; nvaeConsume++)
                                {
                                    input.consume();
                                }
                                NoViableAltException nvae = new NoViableAltException("", 14, 34, input);
                                throw nvae;
                            }
                            finally
                            {
                                input.rewind(nvaeMark);
                            }
                        }

                    }

                }
                else if ((LA14_2 == SCORE) && ((strict == false)))
                {
                    int LA14_32 = input.LA(3);
                    if ((LA14_32 == DOUBLE_QUOTE) && ((strict == false)))
                    {
                        int LA14_34 = input.LA(4);
                        if ((((strict == false) && synpred1_CMIS())))
                        {
                            alt14 = 1;
                        }
                        else if (((strict == false)))
                        {
                            alt14 = 2;
                        }

                        else
                        {
                            if (state.backtracking > 0)
                            {
                                state.failed = true;
                                return retval;
                            }
                            int nvaeMark = input.mark();
                            try
                            {
                                for (int nvaeConsume = 0; nvaeConsume < 4 - 1; nvaeConsume++)
                                {
                                    input.consume();
                                }
                                NoViableAltException nvae = new NoViableAltException("", 14, 34, input);
                                throw nvae;
                            }
                            finally
                            {
                                input.rewind(nvaeMark);
                            }
                        }

                    }

                }
                else if ((LA14_2 == ID) && ((strict == false)))
                {
                    int LA14_33 = input.LA(3);
                    if ((LA14_33 == DOUBLE_QUOTE) && ((strict == false)))
                    {
                        int LA14_34 = input.LA(4);
                        if ((((strict == false) && synpred1_CMIS())))
                        {
                            alt14 = 1;
                        }
                        else if (((strict == false)))
                        {
                            alt14 = 2;
                        }

                        else
                        {
                            if (state.backtracking > 0)
                            {
                                state.failed = true;
                                return retval;
                            }
                            int nvaeMark = input.mark();
                            try
                            {
                                for (int nvaeConsume = 0; nvaeConsume < 4 - 1; nvaeConsume++)
                                {
                                    input.consume();
                                }
                                NoViableAltException nvae = new NoViableAltException("", 14, 34, input);
                                throw nvae;
                            }
                            finally
                            {
                                input.rewind(nvaeMark);
                            }
                        }

                    }

                }

            }

            switch (alt14)
            {
            case 1:
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:388:9: ( tableName )=> tableName
            {
                pushFollow(FOLLOW_tableName_in_qualifier1328);
                tableName32 = tableName();
                state._fsp--;
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                    stream_tableName.add(tableName32.getTree());
                // AST REWRITE
                // elements: tableName
                // token labels:
                // rule labels: retval
                // token list labels:
                // rule list labels:
                // wildcard labels:
                if (state.backtracking == 0)
                {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval = new RewriteRuleSubtreeStream(adaptor, "rule retval", retval != null ? retval.getTree() : null);

                    root_0 = (Object) adaptor.nil();
                    // 389:17: -> tableName
                    {
                        adaptor.addChild(root_0, stream_tableName.nextTree());
                    }

                    retval.tree = root_0;
                }

            }
                break;
            case 2:
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:390:11: correlationName
            {
                pushFollow(FOLLOW_correlationName_in_qualifier1360);
                correlationName33 = correlationName();
                state._fsp--;
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                    stream_correlationName.add(correlationName33.getTree());
                // AST REWRITE
                // elements: correlationName
                // token labels:
                // rule labels: retval
                // token list labels:
                // rule list labels:
                // wildcard labels:
                if (state.backtracking == 0)
                {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval = new RewriteRuleSubtreeStream(adaptor, "rule retval", retval != null ? retval.getTree() : null);

                    root_0 = (Object) adaptor.nil();
                    // 391:17: -> correlationName
                    {
                        adaptor.addChild(root_0, stream_correlationName.nextTree());
                    }

                    retval.tree = root_0;
                }

            }
                break;

            }
            retval.stop = input.LT(-1);

            if (state.backtracking == 0)
            {
                retval.tree = (Object) adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        catch (RecognitionException e)
        {
            throw new CmisInvalidArgumentException(getErrorString(e), e);
        }

        finally
        {
            // do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "qualifier"

    public static class fromClause_return extends ParserRuleReturnScope
    {
        Object tree;

        @Override
        public Object getTree()
        {
            return tree;
        }
    };

    // $ANTLR start "fromClause"
    // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:394:1: fromClause : FROM tableReference -> tableReference ;
    public final CMISParser.fromClause_return fromClause() throws RecognitionException
    {
        CMISParser.fromClause_return retval = new CMISParser.fromClause_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token FROM34 = null;
        ParserRuleReturnScope tableReference35 = null;

        Object FROM34_tree = null;
        RewriteRuleTokenStream stream_FROM = new RewriteRuleTokenStream(adaptor, "token FROM");
        RewriteRuleSubtreeStream stream_tableReference = new RewriteRuleSubtreeStream(adaptor, "rule tableReference");

        paraphrases.push("in fromClause");

        try
        {
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:403:9: ( FROM tableReference -> tableReference )
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:404:9: FROM tableReference
            {
                FROM34 = (Token) match(input, FROM, FOLLOW_FROM_in_fromClause1423);
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                    stream_FROM.add(FROM34);

                pushFollow(FOLLOW_tableReference_in_fromClause1425);
                tableReference35 = tableReference();
                state._fsp--;
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                    stream_tableReference.add(tableReference35.getTree());
                // AST REWRITE
                // elements: tableReference
                // token labels:
                // rule labels: retval
                // token list labels:
                // rule list labels:
                // wildcard labels:
                if (state.backtracking == 0)
                {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval = new RewriteRuleSubtreeStream(adaptor, "rule retval", retval != null ? retval.getTree() : null);

                    root_0 = (Object) adaptor.nil();
                    // 405:17: -> tableReference
                    {
                        adaptor.addChild(root_0, stream_tableReference.nextTree());
                    }

                    retval.tree = root_0;
                }

            }

            retval.stop = input.LT(-1);

            if (state.backtracking == 0)
            {
                retval.tree = (Object) adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
            if (state.backtracking == 0)
            {
                paraphrases.pop();
            }
        }

        catch (RecognitionException e)
        {
            throw new CmisInvalidArgumentException(getErrorString(e), e);
        }

        finally
        {
            // do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "fromClause"

    public static class tableReference_return extends ParserRuleReturnScope
    {
        Object tree;

        @Override
        public Object getTree()
        {
            return tree;
        }
    };

    // $ANTLR start "tableReference"
    // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:408:1: tableReference : singleTable ( joinedTable )* -> ^( SOURCE singleTable ( joinedTable )* ) ;
    public final CMISParser.tableReference_return tableReference() throws RecognitionException
    {
        CMISParser.tableReference_return retval = new CMISParser.tableReference_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        ParserRuleReturnScope singleTable36 = null;
        ParserRuleReturnScope joinedTable37 = null;

        RewriteRuleSubtreeStream stream_singleTable = new RewriteRuleSubtreeStream(adaptor, "rule singleTable");
        RewriteRuleSubtreeStream stream_joinedTable = new RewriteRuleSubtreeStream(adaptor, "rule joinedTable");

        paraphrases.push("in tableReference");

        try
        {
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:417:9: ( singleTable ( joinedTable )* -> ^( SOURCE singleTable ( joinedTable )* ) )
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:418:9: singleTable ( joinedTable )*
            {
                pushFollow(FOLLOW_singleTable_in_tableReference1488);
                singleTable36 = singleTable();
                state._fsp--;
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                    stream_singleTable.add(singleTable36.getTree());
                // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:418:21: ( joinedTable )*
                loop15: while (true)
                {
                    int alt15 = 2;
                    int LA15_0 = input.LA(1);
                    if ((LA15_0 == INNER || (LA15_0 >= JOIN && LA15_0 <= LEFT)))
                    {
                        alt15 = 1;
                    }

                    switch (alt15)
                    {
                    case 1:
                    // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:418:21: joinedTable
                    {
                        pushFollow(FOLLOW_joinedTable_in_tableReference1490);
                        joinedTable37 = joinedTable();
                        state._fsp--;
                        if (state.failed)
                            return retval;
                        if (state.backtracking == 0)
                            stream_joinedTable.add(joinedTable37.getTree());
                    }
                        break;

                    default:
                        break loop15;
                    }
                }

                // AST REWRITE
                // elements: singleTable, joinedTable
                // token labels:
                // rule labels: retval
                // token list labels:
                // rule list labels:
                // wildcard labels:
                if (state.backtracking == 0)
                {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval = new RewriteRuleSubtreeStream(adaptor, "rule retval", retval != null ? retval.getTree() : null);

                    root_0 = (Object) adaptor.nil();
                    // 419:17: -> ^( SOURCE singleTable ( joinedTable )* )
                    {
                        // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:420:25: ^( SOURCE singleTable ( joinedTable )* )
                        {
                            Object root_1 = (Object) adaptor.nil();
                            root_1 = (Object) adaptor.becomeRoot((Object) adaptor.create(SOURCE, "SOURCE"), root_1);
                            adaptor.addChild(root_1, stream_singleTable.nextTree());
                            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:420:46: ( joinedTable )*
                            while (stream_joinedTable.hasNext())
                            {
                                adaptor.addChild(root_1, stream_joinedTable.nextTree());
                            }
                            stream_joinedTable.reset();

                            adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;
                }

            }

            retval.stop = input.LT(-1);

            if (state.backtracking == 0)
            {
                retval.tree = (Object) adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
            if (state.backtracking == 0)
            {
                paraphrases.pop();
            }
        }

        catch (RecognitionException e)
        {
            throw new CmisInvalidArgumentException(getErrorString(e), e);
        }

        finally
        {
            // do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "tableReference"

    public static class singleTable_return extends ParserRuleReturnScope
    {
        Object tree;

        @Override
        public Object getTree()
        {
            return tree;
        }
    };

    // $ANTLR start "singleTable"
    // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:427:1: singleTable : ( simpleTable -> simpleTable | complexTable -> ^( TABLE complexTable ) );
    public final CMISParser.singleTable_return singleTable() throws RecognitionException
    {
        CMISParser.singleTable_return retval = new CMISParser.singleTable_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        ParserRuleReturnScope simpleTable38 = null;
        ParserRuleReturnScope complexTable39 = null;

        RewriteRuleSubtreeStream stream_simpleTable = new RewriteRuleSubtreeStream(adaptor, "rule simpleTable");
        RewriteRuleSubtreeStream stream_complexTable = new RewriteRuleSubtreeStream(adaptor, "rule complexTable");

        paraphrases.push("in singleTable");

        try
        {
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:436:9: ( simpleTable -> simpleTable | complexTable -> ^( TABLE complexTable ) )
            int alt16 = 2;
            int LA16_0 = input.LA(1);
            if ((LA16_0 == ID))
            {
                alt16 = 1;
            }
            else if ((LA16_0 == DOUBLE_QUOTE) && ((strict == false)))
            {
                alt16 = 1;
            }
            else if ((LA16_0 == LPAREN))
            {
                alt16 = 2;
            }

            else
            {
                if (state.backtracking > 0)
                {
                    state.failed = true;
                    return retval;
                }
                NoViableAltException nvae = new NoViableAltException("", 16, 0, input);
                throw nvae;
            }

            switch (alt16)
            {
            case 1:
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:437:9: simpleTable
            {
                pushFollow(FOLLOW_simpleTable_in_singleTable1588);
                simpleTable38 = simpleTable();
                state._fsp--;
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                    stream_simpleTable.add(simpleTable38.getTree());
                // AST REWRITE
                // elements: simpleTable
                // token labels:
                // rule labels: retval
                // token list labels:
                // rule list labels:
                // wildcard labels:
                if (state.backtracking == 0)
                {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval = new RewriteRuleSubtreeStream(adaptor, "rule retval", retval != null ? retval.getTree() : null);

                    root_0 = (Object) adaptor.nil();
                    // 438:17: -> simpleTable
                    {
                        adaptor.addChild(root_0, stream_simpleTable.nextTree());
                    }

                    retval.tree = root_0;
                }

            }
                break;
            case 2:
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:439:11: complexTable
            {
                pushFollow(FOLLOW_complexTable_in_singleTable1620);
                complexTable39 = complexTable();
                state._fsp--;
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                    stream_complexTable.add(complexTable39.getTree());
                // AST REWRITE
                // elements: complexTable
                // token labels:
                // rule labels: retval
                // token list labels:
                // rule list labels:
                // wildcard labels:
                if (state.backtracking == 0)
                {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval = new RewriteRuleSubtreeStream(adaptor, "rule retval", retval != null ? retval.getTree() : null);

                    root_0 = (Object) adaptor.nil();
                    // 440:17: -> ^( TABLE complexTable )
                    {
                        // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:441:25: ^( TABLE complexTable )
                        {
                            Object root_1 = (Object) adaptor.nil();
                            root_1 = (Object) adaptor.becomeRoot((Object) adaptor.create(TABLE, "TABLE"), root_1);
                            adaptor.addChild(root_1, stream_complexTable.nextTree());
                            adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;
                }

            }
                break;

            }
            retval.stop = input.LT(-1);

            if (state.backtracking == 0)
            {
                retval.tree = (Object) adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
            if (state.backtracking == 0)
            {
                paraphrases.pop();
            }
        }

        catch (RecognitionException e)
        {
            throw new CmisInvalidArgumentException(getErrorString(e), e);
        }

        finally
        {
            // do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "singleTable"

    public static class simpleTable_return extends ParserRuleReturnScope
    {
        Object tree;

        @Override
        public Object getTree()
        {
            return tree;
        }
    };

    // $ANTLR start "simpleTable"
    // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:444:1: simpleTable : tableName ( ( AS )? correlationName )? -> ^( TABLE_REF tableName ( correlationName )? ) ;
    public final CMISParser.simpleTable_return simpleTable() throws RecognitionException
    {
        CMISParser.simpleTable_return retval = new CMISParser.simpleTable_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token AS41 = null;
        ParserRuleReturnScope tableName40 = null;
        ParserRuleReturnScope correlationName42 = null;

        Object AS41_tree = null;
        RewriteRuleTokenStream stream_AS = new RewriteRuleTokenStream(adaptor, "token AS");
        RewriteRuleSubtreeStream stream_correlationName = new RewriteRuleSubtreeStream(adaptor, "rule correlationName");
        RewriteRuleSubtreeStream stream_tableName = new RewriteRuleSubtreeStream(adaptor, "rule tableName");

        paraphrases.push("in simpleTable");

        try
        {
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:453:9: ( tableName ( ( AS )? correlationName )? -> ^( TABLE_REF tableName ( correlationName )? ) )
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:454:9: tableName ( ( AS )? correlationName )?
            {
                pushFollow(FOLLOW_tableName_in_simpleTable1711);
                tableName40 = tableName();
                state._fsp--;
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                    stream_tableName.add(tableName40.getTree());
                // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:454:19: ( ( AS )? correlationName )?
                int alt18 = 2;
                int LA18_0 = input.LA(1);
                if ((LA18_0 == AS || LA18_0 == ID))
                {
                    alt18 = 1;
                }
                else if ((LA18_0 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    alt18 = 1;
                }
                switch (alt18)
                {
                case 1:
                // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:454:20: ( AS )? correlationName
                {
                    // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:454:20: ( AS )?
                    int alt17 = 2;
                    int LA17_0 = input.LA(1);
                    if ((LA17_0 == AS))
                    {
                        alt17 = 1;
                    }
                    switch (alt17)
                    {
                    case 1:
                    // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:454:20: AS
                    {
                        AS41 = (Token) match(input, AS, FOLLOW_AS_in_simpleTable1714);
                        if (state.failed)
                            return retval;
                        if (state.backtracking == 0)
                            stream_AS.add(AS41);

                    }
                        break;

                    }

                    pushFollow(FOLLOW_correlationName_in_simpleTable1717);
                    correlationName42 = correlationName();
                    state._fsp--;
                    if (state.failed)
                        return retval;
                    if (state.backtracking == 0)
                        stream_correlationName.add(correlationName42.getTree());
                }
                    break;

                }

                // AST REWRITE
                // elements: correlationName, tableName
                // token labels:
                // rule labels: retval
                // token list labels:
                // rule list labels:
                // wildcard labels:
                if (state.backtracking == 0)
                {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval = new RewriteRuleSubtreeStream(adaptor, "rule retval", retval != null ? retval.getTree() : null);

                    root_0 = (Object) adaptor.nil();
                    // 455:17: -> ^( TABLE_REF tableName ( correlationName )? )
                    {
                        // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:456:25: ^( TABLE_REF tableName ( correlationName )? )
                        {
                            Object root_1 = (Object) adaptor.nil();
                            root_1 = (Object) adaptor.becomeRoot((Object) adaptor.create(TABLE_REF, "TABLE_REF"), root_1);
                            adaptor.addChild(root_1, stream_tableName.nextTree());
                            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:456:47: ( correlationName )?
                            if (stream_correlationName.hasNext())
                            {
                                adaptor.addChild(root_1, stream_correlationName.nextTree());
                            }
                            stream_correlationName.reset();

                            adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;
                }

            }

            retval.stop = input.LT(-1);

            if (state.backtracking == 0)
            {
                retval.tree = (Object) adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
            if (state.backtracking == 0)
            {
                paraphrases.pop();
            }
        }

        catch (RecognitionException e)
        {
            throw new CmisInvalidArgumentException(getErrorString(e), e);
        }

        finally
        {
            // do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "simpleTable"

    public static class joinedTable_return extends ParserRuleReturnScope
    {
        Object tree;

        @Override
        public Object getTree()
        {
            return tree;
        }
    };

    // $ANTLR start "joinedTable"
    // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:459:1: joinedTable : ( joinType )? JOIN tableReference joinSpecification -> ^( JOIN tableReference ( joinType )? joinSpecification ) ;
    public final CMISParser.joinedTable_return joinedTable() throws RecognitionException
    {
        CMISParser.joinedTable_return retval = new CMISParser.joinedTable_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token JOIN44 = null;
        ParserRuleReturnScope joinType43 = null;
        ParserRuleReturnScope tableReference45 = null;
        ParserRuleReturnScope joinSpecification46 = null;

        Object JOIN44_tree = null;
        RewriteRuleTokenStream stream_JOIN = new RewriteRuleTokenStream(adaptor, "token JOIN");
        RewriteRuleSubtreeStream stream_tableReference = new RewriteRuleSubtreeStream(adaptor, "rule tableReference");
        RewriteRuleSubtreeStream stream_joinType = new RewriteRuleSubtreeStream(adaptor, "rule joinType");
        RewriteRuleSubtreeStream stream_joinSpecification = new RewriteRuleSubtreeStream(adaptor, "rule joinSpecification");

        paraphrases.push("in joinedTable");

        try
        {
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:468:9: ( ( joinType )? JOIN tableReference joinSpecification -> ^( JOIN tableReference ( joinType )? joinSpecification ) )
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:469:9: ( joinType )? JOIN tableReference joinSpecification
            {
                // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:469:9: ( joinType )?
                int alt19 = 2;
                int LA19_0 = input.LA(1);
                if ((LA19_0 == INNER || LA19_0 == LEFT))
                {
                    alt19 = 1;
                }
                switch (alt19)
                {
                case 1:
                // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:469:9: joinType
                {
                    pushFollow(FOLLOW_joinType_in_joinedTable1813);
                    joinType43 = joinType();
                    state._fsp--;
                    if (state.failed)
                        return retval;
                    if (state.backtracking == 0)
                        stream_joinType.add(joinType43.getTree());
                }
                    break;

                }

                JOIN44 = (Token) match(input, JOIN, FOLLOW_JOIN_in_joinedTable1816);
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                    stream_JOIN.add(JOIN44);

                pushFollow(FOLLOW_tableReference_in_joinedTable1818);
                tableReference45 = tableReference();
                state._fsp--;
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                    stream_tableReference.add(tableReference45.getTree());
                pushFollow(FOLLOW_joinSpecification_in_joinedTable1820);
                joinSpecification46 = joinSpecification();
                state._fsp--;
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                    stream_joinSpecification.add(joinSpecification46.getTree());
                // AST REWRITE
                // elements: joinType, JOIN, tableReference, joinSpecification
                // token labels:
                // rule labels: retval
                // token list labels:
                // rule list labels:
                // wildcard labels:
                if (state.backtracking == 0)
                {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval = new RewriteRuleSubtreeStream(adaptor, "rule retval", retval != null ? retval.getTree() : null);

                    root_0 = (Object) adaptor.nil();
                    // 470:17: -> ^( JOIN tableReference ( joinType )? joinSpecification )
                    {
                        // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:471:25: ^( JOIN tableReference ( joinType )? joinSpecification )
                        {
                            Object root_1 = (Object) adaptor.nil();
                            root_1 = (Object) adaptor.becomeRoot(stream_JOIN.nextNode(), root_1);
                            adaptor.addChild(root_1, stream_tableReference.nextTree());
                            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:471:47: ( joinType )?
                            if (stream_joinType.hasNext())
                            {
                                adaptor.addChild(root_1, stream_joinType.nextTree());
                            }
                            stream_joinType.reset();

                            adaptor.addChild(root_1, stream_joinSpecification.nextTree());
                            adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;
                }

            }

            retval.stop = input.LT(-1);

            if (state.backtracking == 0)
            {
                retval.tree = (Object) adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
            if (state.backtracking == 0)
            {
                paraphrases.pop();
            }
        }

        catch (RecognitionException e)
        {
            throw new CmisInvalidArgumentException(getErrorString(e), e);
        }

        finally
        {
            // do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "joinedTable"

    public static class complexTable_return extends ParserRuleReturnScope
    {
        Object tree;

        @Override
        public Object getTree()
        {
            return tree;
        }
    };

    // $ANTLR start "complexTable"
    // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:474:1: complexTable : ( ( LPAREN singleTable ( joinedTable )+ RPAREN )=> LPAREN singleTable ( joinedTable )+ RPAREN -> ^( SOURCE singleTable ( joinedTable )+ ) | LPAREN complexTable RPAREN -> complexTable );
    public final CMISParser.complexTable_return complexTable() throws RecognitionException
    {
        CMISParser.complexTable_return retval = new CMISParser.complexTable_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token LPAREN47 = null;
        Token RPAREN50 = null;
        Token LPAREN51 = null;
        Token RPAREN53 = null;
        ParserRuleReturnScope singleTable48 = null;
        ParserRuleReturnScope joinedTable49 = null;
        ParserRuleReturnScope complexTable52 = null;

        Object LPAREN47_tree = null;
        Object RPAREN50_tree = null;
        Object LPAREN51_tree = null;
        Object RPAREN53_tree = null;
        RewriteRuleTokenStream stream_RPAREN = new RewriteRuleTokenStream(adaptor, "token RPAREN");
        RewriteRuleTokenStream stream_LPAREN = new RewriteRuleTokenStream(adaptor, "token LPAREN");
        RewriteRuleSubtreeStream stream_singleTable = new RewriteRuleSubtreeStream(adaptor, "rule singleTable");
        RewriteRuleSubtreeStream stream_joinedTable = new RewriteRuleSubtreeStream(adaptor, "rule joinedTable");
        RewriteRuleSubtreeStream stream_complexTable = new RewriteRuleSubtreeStream(adaptor, "rule complexTable");

        paraphrases.push("in complexTable");

        try
        {
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:483:9: ( ( LPAREN singleTable ( joinedTable )+ RPAREN )=> LPAREN singleTable ( joinedTable )+ RPAREN -> ^( SOURCE singleTable ( joinedTable )+ ) | LPAREN complexTable RPAREN -> complexTable )
            int alt21 = 2;
            int LA21_0 = input.LA(1);
            if ((LA21_0 == LPAREN))
            {
                int LA21_1 = input.LA(2);
                if ((synpred2_CMIS()))
                {
                    alt21 = 1;
                }
                else if ((true))
                {
                    alt21 = 2;
                }

            }

            else
            {
                if (state.backtracking > 0)
                {
                    state.failed = true;
                    return retval;
                }
                NoViableAltException nvae = new NoViableAltException("", 21, 0, input);
                throw nvae;
            }

            switch (alt21)
            {
            case 1:
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:484:9: ( LPAREN singleTable ( joinedTable )+ RPAREN )=> LPAREN singleTable ( joinedTable )+ RPAREN
            {
                LPAREN47 = (Token) match(input, LPAREN, FOLLOW_LPAREN_in_complexTable1929);
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                    stream_LPAREN.add(LPAREN47);

                pushFollow(FOLLOW_singleTable_in_complexTable1931);
                singleTable48 = singleTable();
                state._fsp--;
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                    stream_singleTable.add(singleTable48.getTree());
                // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:484:72: ( joinedTable )+
                int cnt20 = 0;
                loop20: while (true)
                {
                    int alt20 = 2;
                    int LA20_0 = input.LA(1);
                    if ((LA20_0 == INNER || (LA20_0 >= JOIN && LA20_0 <= LEFT)))
                    {
                        alt20 = 1;
                    }

                    switch (alt20)
                    {
                    case 1:
                    // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:484:72: joinedTable
                    {
                        pushFollow(FOLLOW_joinedTable_in_complexTable1933);
                        joinedTable49 = joinedTable();
                        state._fsp--;
                        if (state.failed)
                            return retval;
                        if (state.backtracking == 0)
                            stream_joinedTable.add(joinedTable49.getTree());
                    }
                        break;

                    default:
                        if (cnt20 >= 1)
                            break loop20;
                        if (state.backtracking > 0)
                        {
                            state.failed = true;
                            return retval;
                        }
                        EarlyExitException eee = new EarlyExitException(20, input);
                        throw eee;
                    }
                    cnt20++;
                }

                RPAREN50 = (Token) match(input, RPAREN, FOLLOW_RPAREN_in_complexTable1936);
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                    stream_RPAREN.add(RPAREN50);

                // AST REWRITE
                // elements: joinedTable, singleTable
                // token labels:
                // rule labels: retval
                // token list labels:
                // rule list labels:
                // wildcard labels:
                if (state.backtracking == 0)
                {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval = new RewriteRuleSubtreeStream(adaptor, "rule retval", retval != null ? retval.getTree() : null);

                    root_0 = (Object) adaptor.nil();
                    // 485:17: -> ^( SOURCE singleTable ( joinedTable )+ )
                    {
                        // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:486:25: ^( SOURCE singleTable ( joinedTable )+ )
                        {
                            Object root_1 = (Object) adaptor.nil();
                            root_1 = (Object) adaptor.becomeRoot((Object) adaptor.create(SOURCE, "SOURCE"), root_1);
                            adaptor.addChild(root_1, stream_singleTable.nextTree());
                            if (!(stream_joinedTable.hasNext()))
                            {
                                throw new RewriteEarlyExitException();
                            }
                            while (stream_joinedTable.hasNext())
                            {
                                adaptor.addChild(root_1, stream_joinedTable.nextTree());
                            }
                            stream_joinedTable.reset();

                            adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;
                }

            }
                break;
            case 2:
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:487:11: LPAREN complexTable RPAREN
            {
                LPAREN51 = (Token) match(input, LPAREN, FOLLOW_LPAREN_in_complexTable1999);
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                    stream_LPAREN.add(LPAREN51);

                pushFollow(FOLLOW_complexTable_in_complexTable2001);
                complexTable52 = complexTable();
                state._fsp--;
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                    stream_complexTable.add(complexTable52.getTree());
                RPAREN53 = (Token) match(input, RPAREN, FOLLOW_RPAREN_in_complexTable2003);
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                    stream_RPAREN.add(RPAREN53);

                // AST REWRITE
                // elements: complexTable
                // token labels:
                // rule labels: retval
                // token list labels:
                // rule list labels:
                // wildcard labels:
                if (state.backtracking == 0)
                {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval = new RewriteRuleSubtreeStream(adaptor, "rule retval", retval != null ? retval.getTree() : null);

                    root_0 = (Object) adaptor.nil();
                    // 488:17: -> complexTable
                    {
                        adaptor.addChild(root_0, stream_complexTable.nextTree());
                    }

                    retval.tree = root_0;
                }

            }
                break;

            }
            retval.stop = input.LT(-1);

            if (state.backtracking == 0)
            {
                retval.tree = (Object) adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
            if (state.backtracking == 0)
            {
                paraphrases.pop();
            }
        }

        catch (RecognitionException e)
        {
            throw new CmisInvalidArgumentException(getErrorString(e), e);
        }

        finally
        {
            // do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "complexTable"

    public static class joinType_return extends ParserRuleReturnScope
    {
        Object tree;

        @Override
        public Object getTree()
        {
            return tree;
        }
    };

    // $ANTLR start "joinType"
    // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:491:1: joinType : ( INNER -> INNER | LEFT ( OUTER )? -> LEFT );
    public final CMISParser.joinType_return joinType() throws RecognitionException
    {
        CMISParser.joinType_return retval = new CMISParser.joinType_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token INNER54 = null;
        Token LEFT55 = null;
        Token OUTER56 = null;

        Object INNER54_tree = null;
        Object LEFT55_tree = null;
        Object OUTER56_tree = null;
        RewriteRuleTokenStream stream_OUTER = new RewriteRuleTokenStream(adaptor, "token OUTER");
        RewriteRuleTokenStream stream_INNER = new RewriteRuleTokenStream(adaptor, "token INNER");
        RewriteRuleTokenStream stream_LEFT = new RewriteRuleTokenStream(adaptor, "token LEFT");

        try
        {
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:492:9: ( INNER -> INNER | LEFT ( OUTER )? -> LEFT )
            int alt23 = 2;
            int LA23_0 = input.LA(1);
            if ((LA23_0 == INNER))
            {
                alt23 = 1;
            }
            else if ((LA23_0 == LEFT))
            {
                alt23 = 2;
            }

            else
            {
                if (state.backtracking > 0)
                {
                    state.failed = true;
                    return retval;
                }
                NoViableAltException nvae = new NoViableAltException("", 23, 0, input);
                throw nvae;
            }

            switch (alt23)
            {
            case 1:
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:493:9: INNER
            {
                INNER54 = (Token) match(input, INNER, FOLLOW_INNER_in_joinType2056);
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                    stream_INNER.add(INNER54);

                // AST REWRITE
                // elements: INNER
                // token labels:
                // rule labels: retval
                // token list labels:
                // rule list labels:
                // wildcard labels:
                if (state.backtracking == 0)
                {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval = new RewriteRuleSubtreeStream(adaptor, "rule retval", retval != null ? retval.getTree() : null);

                    root_0 = (Object) adaptor.nil();
                    // 494:17: -> INNER
                    {
                        adaptor.addChild(root_0, stream_INNER.nextNode());
                    }

                    retval.tree = root_0;
                }

            }
                break;
            case 2:
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:495:11: LEFT ( OUTER )?
            {
                LEFT55 = (Token) match(input, LEFT, FOLLOW_LEFT_in_joinType2088);
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                    stream_LEFT.add(LEFT55);

                // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:495:16: ( OUTER )?
                int alt22 = 2;
                int LA22_0 = input.LA(1);
                if ((LA22_0 == OUTER))
                {
                    alt22 = 1;
                }
                switch (alt22)
                {
                case 1:
                // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:495:16: OUTER
                {
                    OUTER56 = (Token) match(input, OUTER, FOLLOW_OUTER_in_joinType2090);
                    if (state.failed)
                        return retval;
                    if (state.backtracking == 0)
                        stream_OUTER.add(OUTER56);

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
                if (state.backtracking == 0)
                {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval = new RewriteRuleSubtreeStream(adaptor, "rule retval", retval != null ? retval.getTree() : null);

                    root_0 = (Object) adaptor.nil();
                    // 496:17: -> LEFT
                    {
                        adaptor.addChild(root_0, stream_LEFT.nextNode());
                    }

                    retval.tree = root_0;
                }

            }
                break;

            }
            retval.stop = input.LT(-1);

            if (state.backtracking == 0)
            {
                retval.tree = (Object) adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        catch (RecognitionException e)
        {
            throw new CmisInvalidArgumentException(getErrorString(e), e);
        }

        finally
        {
            // do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "joinType"

    public static class joinSpecification_return extends ParserRuleReturnScope
    {
        Object tree;

        @Override
        public Object getTree()
        {
            return tree;
        }
    };

    // $ANTLR start "joinSpecification"
    // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:499:1: joinSpecification : ON lhs= columnReference EQUALS rhs= columnReference -> ^( ON $lhs EQUALS $rhs) ;
    public final CMISParser.joinSpecification_return joinSpecification() throws RecognitionException
    {
        CMISParser.joinSpecification_return retval = new CMISParser.joinSpecification_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token ON57 = null;
        Token EQUALS58 = null;
        ParserRuleReturnScope lhs = null;
        ParserRuleReturnScope rhs = null;

        Object ON57_tree = null;
        Object EQUALS58_tree = null;
        RewriteRuleTokenStream stream_ON = new RewriteRuleTokenStream(adaptor, "token ON");
        RewriteRuleTokenStream stream_EQUALS = new RewriteRuleTokenStream(adaptor, "token EQUALS");
        RewriteRuleSubtreeStream stream_columnReference = new RewriteRuleSubtreeStream(adaptor, "rule columnReference");

        paraphrases.push("in join condition");

        try
        {
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:508:9: ( ON lhs= columnReference EQUALS rhs= columnReference -> ^( ON $lhs EQUALS $rhs) )
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:509:9: ON lhs= columnReference EQUALS rhs= columnReference
            {
                ON57 = (Token) match(input, ON, FOLLOW_ON_in_joinSpecification2154);
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                    stream_ON.add(ON57);

                pushFollow(FOLLOW_columnReference_in_joinSpecification2158);
                lhs = columnReference();
                state._fsp--;
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                    stream_columnReference.add(lhs.getTree());
                EQUALS58 = (Token) match(input, EQUALS, FOLLOW_EQUALS_in_joinSpecification2160);
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                    stream_EQUALS.add(EQUALS58);

                pushFollow(FOLLOW_columnReference_in_joinSpecification2164);
                rhs = columnReference();
                state._fsp--;
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                    stream_columnReference.add(rhs.getTree());
                // AST REWRITE
                // elements: ON, EQUALS, rhs, lhs
                // token labels:
                // rule labels: retval, rhs, lhs
                // token list labels:
                // rule list labels:
                // wildcard labels:
                if (state.backtracking == 0)
                {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval = new RewriteRuleSubtreeStream(adaptor, "rule retval", retval != null ? retval.getTree() : null);
                    RewriteRuleSubtreeStream stream_rhs = new RewriteRuleSubtreeStream(adaptor, "rule rhs", rhs != null ? rhs.getTree() : null);
                    RewriteRuleSubtreeStream stream_lhs = new RewriteRuleSubtreeStream(adaptor, "rule lhs", lhs != null ? lhs.getTree() : null);

                    root_0 = (Object) adaptor.nil();
                    // 510:17: -> ^( ON $lhs EQUALS $rhs)
                    {
                        // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:511:25: ^( ON $lhs EQUALS $rhs)
                        {
                            Object root_1 = (Object) adaptor.nil();
                            root_1 = (Object) adaptor.becomeRoot(stream_ON.nextNode(), root_1);
                            adaptor.addChild(root_1, stream_lhs.nextTree());
                            adaptor.addChild(root_1, stream_EQUALS.nextNode());
                            adaptor.addChild(root_1, stream_rhs.nextTree());
                            adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;
                }

            }

            retval.stop = input.LT(-1);

            if (state.backtracking == 0)
            {
                retval.tree = (Object) adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
            if (state.backtracking == 0)
            {
                paraphrases.pop();
            }
        }

        catch (RecognitionException e)
        {
            throw new CmisInvalidArgumentException(getErrorString(e), e);
        }

        finally
        {
            // do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "joinSpecification"

    public static class whereClause_return extends ParserRuleReturnScope
    {
        Object tree;

        @Override
        public Object getTree()
        {
            return tree;
        }
    };

    // $ANTLR start "whereClause"
    // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:518:1: whereClause : WHERE searchOrCondition -> searchOrCondition ;
    public final CMISParser.whereClause_return whereClause() throws RecognitionException
    {
        CMISParser.whereClause_return retval = new CMISParser.whereClause_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token WHERE59 = null;
        ParserRuleReturnScope searchOrCondition60 = null;

        Object WHERE59_tree = null;
        RewriteRuleTokenStream stream_WHERE = new RewriteRuleTokenStream(adaptor, "token WHERE");
        RewriteRuleSubtreeStream stream_searchOrCondition = new RewriteRuleSubtreeStream(adaptor, "rule searchOrCondition");

        paraphrases.push("in where");

        try
        {
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:527:9: ( WHERE searchOrCondition -> searchOrCondition )
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:528:9: WHERE searchOrCondition
            {
                WHERE59 = (Token) match(input, WHERE, FOLLOW_WHERE_in_whereClause2264);
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                    stream_WHERE.add(WHERE59);

                pushFollow(FOLLOW_searchOrCondition_in_whereClause2266);
                searchOrCondition60 = searchOrCondition();
                state._fsp--;
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                    stream_searchOrCondition.add(searchOrCondition60.getTree());
                // AST REWRITE
                // elements: searchOrCondition
                // token labels:
                // rule labels: retval
                // token list labels:
                // rule list labels:
                // wildcard labels:
                if (state.backtracking == 0)
                {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval = new RewriteRuleSubtreeStream(adaptor, "rule retval", retval != null ? retval.getTree() : null);

                    root_0 = (Object) adaptor.nil();
                    // 529:17: -> searchOrCondition
                    {
                        adaptor.addChild(root_0, stream_searchOrCondition.nextTree());
                    }

                    retval.tree = root_0;
                }

            }

            retval.stop = input.LT(-1);

            if (state.backtracking == 0)
            {
                retval.tree = (Object) adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
            if (state.backtracking == 0)
            {
                paraphrases.pop();
            }
        }

        catch (RecognitionException e)
        {
            throw new CmisInvalidArgumentException(getErrorString(e), e);
        }

        finally
        {
            // do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "whereClause"

    public static class searchOrCondition_return extends ParserRuleReturnScope
    {
        Object tree;

        @Override
        public Object getTree()
        {
            return tree;
        }
    };

    // $ANTLR start "searchOrCondition"
    // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:535:1: searchOrCondition : searchAndCondition ( OR searchAndCondition )* -> ^( DISJUNCTION ( searchAndCondition )+ ) ;
    public final CMISParser.searchOrCondition_return searchOrCondition() throws RecognitionException
    {
        CMISParser.searchOrCondition_return retval = new CMISParser.searchOrCondition_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token OR62 = null;
        ParserRuleReturnScope searchAndCondition61 = null;
        ParserRuleReturnScope searchAndCondition63 = null;

        Object OR62_tree = null;
        RewriteRuleTokenStream stream_OR = new RewriteRuleTokenStream(adaptor, "token OR");
        RewriteRuleSubtreeStream stream_searchAndCondition = new RewriteRuleSubtreeStream(adaptor, "rule searchAndCondition");

        try
        {
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:536:9: ( searchAndCondition ( OR searchAndCondition )* -> ^( DISJUNCTION ( searchAndCondition )+ ) )
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:537:9: searchAndCondition ( OR searchAndCondition )*
            {
                pushFollow(FOLLOW_searchAndCondition_in_searchOrCondition2321);
                searchAndCondition61 = searchAndCondition();
                state._fsp--;
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                    stream_searchAndCondition.add(searchAndCondition61.getTree());
                // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:537:28: ( OR searchAndCondition )*
                loop24: while (true)
                {
                    int alt24 = 2;
                    int LA24_0 = input.LA(1);
                    if ((LA24_0 == OR))
                    {
                        alt24 = 1;
                    }

                    switch (alt24)
                    {
                    case 1:
                    // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:537:29: OR searchAndCondition
                    {
                        OR62 = (Token) match(input, OR, FOLLOW_OR_in_searchOrCondition2324);
                        if (state.failed)
                            return retval;
                        if (state.backtracking == 0)
                            stream_OR.add(OR62);

                        pushFollow(FOLLOW_searchAndCondition_in_searchOrCondition2326);
                        searchAndCondition63 = searchAndCondition();
                        state._fsp--;
                        if (state.failed)
                            return retval;
                        if (state.backtracking == 0)
                            stream_searchAndCondition.add(searchAndCondition63.getTree());
                    }
                        break;

                    default:
                        break loop24;
                    }
                }

                // AST REWRITE
                // elements: searchAndCondition
                // token labels:
                // rule labels: retval
                // token list labels:
                // rule list labels:
                // wildcard labels:
                if (state.backtracking == 0)
                {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval = new RewriteRuleSubtreeStream(adaptor, "rule retval", retval != null ? retval.getTree() : null);

                    root_0 = (Object) adaptor.nil();
                    // 538:17: -> ^( DISJUNCTION ( searchAndCondition )+ )
                    {
                        // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:539:25: ^( DISJUNCTION ( searchAndCondition )+ )
                        {
                            Object root_1 = (Object) adaptor.nil();
                            root_1 = (Object) adaptor.becomeRoot((Object) adaptor.create(DISJUNCTION, "DISJUNCTION"), root_1);
                            if (!(stream_searchAndCondition.hasNext()))
                            {
                                throw new RewriteEarlyExitException();
                            }
                            while (stream_searchAndCondition.hasNext())
                            {
                                adaptor.addChild(root_1, stream_searchAndCondition.nextTree());
                            }
                            stream_searchAndCondition.reset();

                            adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;
                }

            }

            retval.stop = input.LT(-1);

            if (state.backtracking == 0)
            {
                retval.tree = (Object) adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        catch (RecognitionException e)
        {
            throw new CmisInvalidArgumentException(getErrorString(e), e);
        }

        finally
        {
            // do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "searchOrCondition"

    public static class searchAndCondition_return extends ParserRuleReturnScope
    {
        Object tree;

        @Override
        public Object getTree()
        {
            return tree;
        }
    };

    // $ANTLR start "searchAndCondition"
    // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:545:1: searchAndCondition : searchNotCondition ( AND searchNotCondition )* -> ^( CONJUNCTION ( searchNotCondition )+ ) ;
    public final CMISParser.searchAndCondition_return searchAndCondition() throws RecognitionException
    {
        CMISParser.searchAndCondition_return retval = new CMISParser.searchAndCondition_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token AND65 = null;
        ParserRuleReturnScope searchNotCondition64 = null;
        ParserRuleReturnScope searchNotCondition66 = null;

        Object AND65_tree = null;
        RewriteRuleTokenStream stream_AND = new RewriteRuleTokenStream(adaptor, "token AND");
        RewriteRuleSubtreeStream stream_searchNotCondition = new RewriteRuleSubtreeStream(adaptor, "rule searchNotCondition");

        try
        {
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:546:9: ( searchNotCondition ( AND searchNotCondition )* -> ^( CONJUNCTION ( searchNotCondition )+ ) )
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:547:9: searchNotCondition ( AND searchNotCondition )*
            {
                pushFollow(FOLLOW_searchNotCondition_in_searchAndCondition2412);
                searchNotCondition64 = searchNotCondition();
                state._fsp--;
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                    stream_searchNotCondition.add(searchNotCondition64.getTree());
                // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:547:28: ( AND searchNotCondition )*
                loop25: while (true)
                {
                    int alt25 = 2;
                    int LA25_0 = input.LA(1);
                    if ((LA25_0 == AND))
                    {
                        alt25 = 1;
                    }

                    switch (alt25)
                    {
                    case 1:
                    // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:547:29: AND searchNotCondition
                    {
                        AND65 = (Token) match(input, AND, FOLLOW_AND_in_searchAndCondition2415);
                        if (state.failed)
                            return retval;
                        if (state.backtracking == 0)
                            stream_AND.add(AND65);

                        pushFollow(FOLLOW_searchNotCondition_in_searchAndCondition2417);
                        searchNotCondition66 = searchNotCondition();
                        state._fsp--;
                        if (state.failed)
                            return retval;
                        if (state.backtracking == 0)
                            stream_searchNotCondition.add(searchNotCondition66.getTree());
                    }
                        break;

                    default:
                        break loop25;
                    }
                }

                // AST REWRITE
                // elements: searchNotCondition
                // token labels:
                // rule labels: retval
                // token list labels:
                // rule list labels:
                // wildcard labels:
                if (state.backtracking == 0)
                {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval = new RewriteRuleSubtreeStream(adaptor, "rule retval", retval != null ? retval.getTree() : null);

                    root_0 = (Object) adaptor.nil();
                    // 548:17: -> ^( CONJUNCTION ( searchNotCondition )+ )
                    {
                        // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:549:25: ^( CONJUNCTION ( searchNotCondition )+ )
                        {
                            Object root_1 = (Object) adaptor.nil();
                            root_1 = (Object) adaptor.becomeRoot((Object) adaptor.create(CONJUNCTION, "CONJUNCTION"), root_1);
                            if (!(stream_searchNotCondition.hasNext()))
                            {
                                throw new RewriteEarlyExitException();
                            }
                            while (stream_searchNotCondition.hasNext())
                            {
                                adaptor.addChild(root_1, stream_searchNotCondition.nextTree());
                            }
                            stream_searchNotCondition.reset();

                            adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;
                }

            }

            retval.stop = input.LT(-1);

            if (state.backtracking == 0)
            {
                retval.tree = (Object) adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        catch (RecognitionException e)
        {
            throw new CmisInvalidArgumentException(getErrorString(e), e);
        }

        finally
        {
            // do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "searchAndCondition"

    public static class searchNotCondition_return extends ParserRuleReturnScope
    {
        Object tree;

        @Override
        public Object getTree()
        {
            return tree;
        }
    };

    // $ANTLR start "searchNotCondition"
    // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:552:1: searchNotCondition : ( NOT searchTest -> ^( NEGATION searchTest ) | searchTest -> searchTest );
    public final CMISParser.searchNotCondition_return searchNotCondition() throws RecognitionException
    {
        CMISParser.searchNotCondition_return retval = new CMISParser.searchNotCondition_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token NOT67 = null;
        ParserRuleReturnScope searchTest68 = null;
        ParserRuleReturnScope searchTest69 = null;

        Object NOT67_tree = null;
        RewriteRuleTokenStream stream_NOT = new RewriteRuleTokenStream(adaptor, "token NOT");
        RewriteRuleSubtreeStream stream_searchTest = new RewriteRuleSubtreeStream(adaptor, "rule searchTest");

        try
        {
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:553:9: ( NOT searchTest -> ^( NEGATION searchTest ) | searchTest -> searchTest )
            int alt26 = 2;
            alt26 = dfa26.predict(input);
            switch (alt26)
            {
            case 1:
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:554:9: NOT searchTest
            {
                NOT67 = (Token) match(input, NOT, FOLLOW_NOT_in_searchNotCondition2501);
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                    stream_NOT.add(NOT67);

                pushFollow(FOLLOW_searchTest_in_searchNotCondition2503);
                searchTest68 = searchTest();
                state._fsp--;
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                    stream_searchTest.add(searchTest68.getTree());
                // AST REWRITE
                // elements: searchTest
                // token labels:
                // rule labels: retval
                // token list labels:
                // rule list labels:
                // wildcard labels:
                if (state.backtracking == 0)
                {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval = new RewriteRuleSubtreeStream(adaptor, "rule retval", retval != null ? retval.getTree() : null);

                    root_0 = (Object) adaptor.nil();
                    // 555:17: -> ^( NEGATION searchTest )
                    {
                        // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:556:25: ^( NEGATION searchTest )
                        {
                            Object root_1 = (Object) adaptor.nil();
                            root_1 = (Object) adaptor.becomeRoot((Object) adaptor.create(NEGATION, "NEGATION"), root_1);
                            adaptor.addChild(root_1, stream_searchTest.nextTree());
                            adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;
                }

            }
                break;
            case 2:
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:557:11: searchTest
            {
                pushFollow(FOLLOW_searchTest_in_searchNotCondition2563);
                searchTest69 = searchTest();
                state._fsp--;
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                    stream_searchTest.add(searchTest69.getTree());
                // AST REWRITE
                // elements: searchTest
                // token labels:
                // rule labels: retval
                // token list labels:
                // rule list labels:
                // wildcard labels:
                if (state.backtracking == 0)
                {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval = new RewriteRuleSubtreeStream(adaptor, "rule retval", retval != null ? retval.getTree() : null);

                    root_0 = (Object) adaptor.nil();
                    // 558:17: -> searchTest
                    {
                        adaptor.addChild(root_0, stream_searchTest.nextTree());
                    }

                    retval.tree = root_0;
                }

            }
                break;

            }
            retval.stop = input.LT(-1);

            if (state.backtracking == 0)
            {
                retval.tree = (Object) adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        catch (RecognitionException e)
        {
            throw new CmisInvalidArgumentException(getErrorString(e), e);
        }

        finally
        {
            // do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "searchNotCondition"

    public static class searchTest_return extends ParserRuleReturnScope
    {
        Object tree;

        @Override
        public Object getTree()
        {
            return tree;
        }
    };

    // $ANTLR start "searchTest"
    // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:561:1: searchTest : ( predicate -> predicate | LPAREN searchOrCondition RPAREN -> searchOrCondition );
    public final CMISParser.searchTest_return searchTest() throws RecognitionException
    {
        CMISParser.searchTest_return retval = new CMISParser.searchTest_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token LPAREN71 = null;
        Token RPAREN73 = null;
        ParserRuleReturnScope predicate70 = null;
        ParserRuleReturnScope searchOrCondition72 = null;

        Object LPAREN71_tree = null;
        Object RPAREN73_tree = null;
        RewriteRuleTokenStream stream_RPAREN = new RewriteRuleTokenStream(adaptor, "token RPAREN");
        RewriteRuleTokenStream stream_LPAREN = new RewriteRuleTokenStream(adaptor, "token LPAREN");
        RewriteRuleSubtreeStream stream_predicate = new RewriteRuleSubtreeStream(adaptor, "rule predicate");
        RewriteRuleSubtreeStream stream_searchOrCondition = new RewriteRuleSubtreeStream(adaptor, "rule searchOrCondition");

        try
        {
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:562:9: ( predicate -> predicate | LPAREN searchOrCondition RPAREN -> searchOrCondition )
            int alt27 = 2;
            int LA27_0 = input.LA(1);
            if ((LA27_0 == ID))
            {
                alt27 = 1;
            }
            else if ((LA27_0 == DOUBLE_QUOTE) && ((strict == false)))
            {
                alt27 = 1;
            }
            else if ((LA27_0 == SCORE))
            {
                alt27 = 1;
            }
            else if ((LA27_0 == AND || LA27_0 == AS || LA27_0 == FROM || (LA27_0 >= IN && LA27_0 <= INNER) || (LA27_0 >= IS && LA27_0 <= LEFT) || LA27_0 == LIKE || LA27_0 == NOT || LA27_0 == NULL || (LA27_0 >= ON && LA27_0 <= OR) || LA27_0 == OUTER || LA27_0 == SELECT || LA27_0 == WHERE) && ((strict == false)))
            {
                alt27 = 1;
            }
            else if ((LA27_0 == ANY || LA27_0 == CONTAINS || (LA27_0 >= IN_FOLDER && LA27_0 <= IN_TREE)))
            {
                alt27 = 1;
            }
            else if ((LA27_0 == ASC || LA27_0 == BY || LA27_0 == DESC || LA27_0 == ORDER) && ((strict == false)))
            {
                alt27 = 1;
            }
            else if ((LA27_0 == DECIMAL_INTEGER_LITERAL || (LA27_0 >= FALSE && LA27_0 <= FLOATING_POINT_LITERAL) || LA27_0 == QUOTED_STRING || (LA27_0 >= TIMESTAMP && LA27_0 <= TRUE)))
            {
                alt27 = 1;
            }
            else if ((LA27_0 == COLON) && ((strict == false)))
            {
                alt27 = 1;
            }
            else if ((LA27_0 == LPAREN))
            {
                alt27 = 2;
            }

            else
            {
                if (state.backtracking > 0)
                {
                    state.failed = true;
                    return retval;
                }
                NoViableAltException nvae = new NoViableAltException("", 27, 0, input);
                throw nvae;
            }

            switch (alt27)
            {
            case 1:
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:563:9: predicate
            {
                pushFollow(FOLLOW_predicate_in_searchTest2616);
                predicate70 = predicate();
                state._fsp--;
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                    stream_predicate.add(predicate70.getTree());
                // AST REWRITE
                // elements: predicate
                // token labels:
                // rule labels: retval
                // token list labels:
                // rule list labels:
                // wildcard labels:
                if (state.backtracking == 0)
                {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval = new RewriteRuleSubtreeStream(adaptor, "rule retval", retval != null ? retval.getTree() : null);

                    root_0 = (Object) adaptor.nil();
                    // 564:17: -> predicate
                    {
                        adaptor.addChild(root_0, stream_predicate.nextTree());
                    }

                    retval.tree = root_0;
                }

            }
                break;
            case 2:
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:565:11: LPAREN searchOrCondition RPAREN
            {
                LPAREN71 = (Token) match(input, LPAREN, FOLLOW_LPAREN_in_searchTest2648);
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                    stream_LPAREN.add(LPAREN71);

                pushFollow(FOLLOW_searchOrCondition_in_searchTest2650);
                searchOrCondition72 = searchOrCondition();
                state._fsp--;
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                    stream_searchOrCondition.add(searchOrCondition72.getTree());
                RPAREN73 = (Token) match(input, RPAREN, FOLLOW_RPAREN_in_searchTest2652);
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                    stream_RPAREN.add(RPAREN73);

                // AST REWRITE
                // elements: searchOrCondition
                // token labels:
                // rule labels: retval
                // token list labels:
                // rule list labels:
                // wildcard labels:
                if (state.backtracking == 0)
                {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval = new RewriteRuleSubtreeStream(adaptor, "rule retval", retval != null ? retval.getTree() : null);

                    root_0 = (Object) adaptor.nil();
                    // 566:17: -> searchOrCondition
                    {
                        adaptor.addChild(root_0, stream_searchOrCondition.nextTree());
                    }

                    retval.tree = root_0;
                }

            }
                break;

            }
            retval.stop = input.LT(-1);

            if (state.backtracking == 0)
            {
                retval.tree = (Object) adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        catch (RecognitionException e)
        {
            throw new CmisInvalidArgumentException(getErrorString(e), e);
        }

        finally
        {
            // do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "searchTest"

    public static class predicate_return extends ParserRuleReturnScope
    {
        Object tree;

        @Override
        public Object getTree()
        {
            return tree;
        }
    };

    // $ANTLR start "predicate"
    // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:569:1: predicate : ( comparisonPredicate | inPredicate | likePredicate | nullPredicate | quantifiedComparisonPredicate | quantifiedInPredicate | textSearchPredicate | folderPredicate );
    public final CMISParser.predicate_return predicate() throws RecognitionException
    {
        CMISParser.predicate_return retval = new CMISParser.predicate_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        ParserRuleReturnScope comparisonPredicate74 = null;
        ParserRuleReturnScope inPredicate75 = null;
        ParserRuleReturnScope likePredicate76 = null;
        ParserRuleReturnScope nullPredicate77 = null;
        ParserRuleReturnScope quantifiedComparisonPredicate78 = null;
        ParserRuleReturnScope quantifiedInPredicate79 = null;
        ParserRuleReturnScope textSearchPredicate80 = null;
        ParserRuleReturnScope folderPredicate81 = null;

        try
        {
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:570:9: ( comparisonPredicate | inPredicate | likePredicate | nullPredicate | quantifiedComparisonPredicate | quantifiedInPredicate | textSearchPredicate | folderPredicate )
            int alt28 = 8;
            alt28 = dfa28.predict(input);
            switch (alt28)
            {
            case 1:
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:571:9: comparisonPredicate
            {
                root_0 = (Object) adaptor.nil();

                pushFollow(FOLLOW_comparisonPredicate_in_predicate2705);
                comparisonPredicate74 = comparisonPredicate();
                state._fsp--;
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                    adaptor.addChild(root_0, comparisonPredicate74.getTree());

            }
                break;
            case 2:
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:572:11: inPredicate
            {
                root_0 = (Object) adaptor.nil();

                pushFollow(FOLLOW_inPredicate_in_predicate2717);
                inPredicate75 = inPredicate();
                state._fsp--;
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                    adaptor.addChild(root_0, inPredicate75.getTree());

            }
                break;
            case 3:
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:573:11: likePredicate
            {
                root_0 = (Object) adaptor.nil();

                pushFollow(FOLLOW_likePredicate_in_predicate2729);
                likePredicate76 = likePredicate();
                state._fsp--;
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                    adaptor.addChild(root_0, likePredicate76.getTree());

            }
                break;
            case 4:
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:574:11: nullPredicate
            {
                root_0 = (Object) adaptor.nil();

                pushFollow(FOLLOW_nullPredicate_in_predicate2741);
                nullPredicate77 = nullPredicate();
                state._fsp--;
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                    adaptor.addChild(root_0, nullPredicate77.getTree());

            }
                break;
            case 5:
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:575:11: quantifiedComparisonPredicate
            {
                root_0 = (Object) adaptor.nil();

                pushFollow(FOLLOW_quantifiedComparisonPredicate_in_predicate2753);
                quantifiedComparisonPredicate78 = quantifiedComparisonPredicate();
                state._fsp--;
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                    adaptor.addChild(root_0, quantifiedComparisonPredicate78.getTree());

            }
                break;
            case 6:
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:576:11: quantifiedInPredicate
            {
                root_0 = (Object) adaptor.nil();

                pushFollow(FOLLOW_quantifiedInPredicate_in_predicate2765);
                quantifiedInPredicate79 = quantifiedInPredicate();
                state._fsp--;
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                    adaptor.addChild(root_0, quantifiedInPredicate79.getTree());

            }
                break;
            case 7:
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:577:11: textSearchPredicate
            {
                root_0 = (Object) adaptor.nil();

                pushFollow(FOLLOW_textSearchPredicate_in_predicate2777);
                textSearchPredicate80 = textSearchPredicate();
                state._fsp--;
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                    adaptor.addChild(root_0, textSearchPredicate80.getTree());

            }
                break;
            case 8:
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:578:11: folderPredicate
            {
                root_0 = (Object) adaptor.nil();

                pushFollow(FOLLOW_folderPredicate_in_predicate2789);
                folderPredicate81 = folderPredicate();
                state._fsp--;
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                    adaptor.addChild(root_0, folderPredicate81.getTree());

            }
                break;

            }
            retval.stop = input.LT(-1);

            if (state.backtracking == 0)
            {
                retval.tree = (Object) adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        catch (RecognitionException e)
        {
            throw new CmisInvalidArgumentException(getErrorString(e), e);
        }

        finally
        {
            // do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "predicate"

    public static class comparisonPredicate_return extends ParserRuleReturnScope
    {
        Object tree;

        @Override
        public Object getTree()
        {
            return tree;
        }
    };

    // $ANTLR start "comparisonPredicate"
    // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:581:1: comparisonPredicate : valueExpression compOp literalOrParameterName -> ^( PRED_COMPARISON SINGLE_VALUED_PROPERTY valueExpression compOp literalOrParameterName ) ;
    public final CMISParser.comparisonPredicate_return comparisonPredicate() throws RecognitionException
    {
        CMISParser.comparisonPredicate_return retval = new CMISParser.comparisonPredicate_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        ParserRuleReturnScope valueExpression82 = null;
        ParserRuleReturnScope compOp83 = null;
        ParserRuleReturnScope literalOrParameterName84 = null;

        RewriteRuleSubtreeStream stream_valueExpression = new RewriteRuleSubtreeStream(adaptor, "rule valueExpression");
        RewriteRuleSubtreeStream stream_compOp = new RewriteRuleSubtreeStream(adaptor, "rule compOp");
        RewriteRuleSubtreeStream stream_literalOrParameterName = new RewriteRuleSubtreeStream(adaptor, "rule literalOrParameterName");

        try
        {
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:582:9: ( valueExpression compOp literalOrParameterName -> ^( PRED_COMPARISON SINGLE_VALUED_PROPERTY valueExpression compOp literalOrParameterName ) )
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:583:9: valueExpression compOp literalOrParameterName
            {
                pushFollow(FOLLOW_valueExpression_in_comparisonPredicate2822);
                valueExpression82 = valueExpression();
                state._fsp--;
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                    stream_valueExpression.add(valueExpression82.getTree());
                pushFollow(FOLLOW_compOp_in_comparisonPredicate2824);
                compOp83 = compOp();
                state._fsp--;
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                    stream_compOp.add(compOp83.getTree());
                pushFollow(FOLLOW_literalOrParameterName_in_comparisonPredicate2826);
                literalOrParameterName84 = literalOrParameterName();
                state._fsp--;
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                    stream_literalOrParameterName.add(literalOrParameterName84.getTree());
                // AST REWRITE
                // elements: literalOrParameterName, valueExpression, compOp
                // token labels:
                // rule labels: retval
                // token list labels:
                // rule list labels:
                // wildcard labels:
                if (state.backtracking == 0)
                {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval = new RewriteRuleSubtreeStream(adaptor, "rule retval", retval != null ? retval.getTree() : null);

                    root_0 = (Object) adaptor.nil();
                    // 584:17: -> ^( PRED_COMPARISON SINGLE_VALUED_PROPERTY valueExpression compOp literalOrParameterName )
                    {
                        // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:585:25: ^( PRED_COMPARISON SINGLE_VALUED_PROPERTY valueExpression compOp literalOrParameterName )
                        {
                            Object root_1 = (Object) adaptor.nil();
                            root_1 = (Object) adaptor.becomeRoot((Object) adaptor.create(PRED_COMPARISON, "PRED_COMPARISON"), root_1);
                            adaptor.addChild(root_1, (Object) adaptor.create(SINGLE_VALUED_PROPERTY, "SINGLE_VALUED_PROPERTY"));
                            adaptor.addChild(root_1, stream_valueExpression.nextTree());
                            adaptor.addChild(root_1, stream_compOp.nextTree());
                            adaptor.addChild(root_1, stream_literalOrParameterName.nextTree());
                            adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;
                }

            }

            retval.stop = input.LT(-1);

            if (state.backtracking == 0)
            {
                retval.tree = (Object) adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        catch (RecognitionException e)
        {
            throw new CmisInvalidArgumentException(getErrorString(e), e);
        }

        finally
        {
            // do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "comparisonPredicate"

    public static class compOp_return extends ParserRuleReturnScope
    {
        Object tree;

        @Override
        public Object getTree()
        {
            return tree;
        }
    };

    // $ANTLR start "compOp"
    // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:588:1: compOp : ( EQUALS | NOTEQUALS | LESSTHAN | GREATERTHAN | LESSTHANOREQUALS | GREATERTHANOREQUALS );
    public final CMISParser.compOp_return compOp() throws RecognitionException
    {
        CMISParser.compOp_return retval = new CMISParser.compOp_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token set85 = null;

        Object set85_tree = null;

        try
        {
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:589:9: ( EQUALS | NOTEQUALS | LESSTHAN | GREATERTHAN | LESSTHANOREQUALS | GREATERTHANOREQUALS )
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:
            {
                root_0 = (Object) adaptor.nil();

                set85 = input.LT(1);
                if (input.LA(1) == EQUALS || (input.LA(1) >= GREATERTHAN && input.LA(1) <= GREATERTHANOREQUALS) || (input.LA(1) >= LESSTHAN && input.LA(1) <= LESSTHANOREQUALS) || input.LA(1) == NOTEQUALS)
                {
                    input.consume();
                    if (state.backtracking == 0)
                        adaptor.addChild(root_0, (Object) adaptor.create(set85));
                    state.errorRecovery = false;
                    state.failed = false;
                }
                else
                {
                    if (state.backtracking > 0)
                    {
                        state.failed = true;
                        return retval;
                    }
                    MismatchedSetException mse = new MismatchedSetException(null, input);
                    throw mse;
                }
            }

            retval.stop = input.LT(-1);

            if (state.backtracking == 0)
            {
                retval.tree = (Object) adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        catch (RecognitionException e)
        {
            throw new CmisInvalidArgumentException(getErrorString(e), e);
        }

        finally
        {
            // do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "compOp"

    public static class literalOrParameterName_return extends ParserRuleReturnScope
    {
        Object tree;

        @Override
        public Object getTree()
        {
            return tree;
        }
    };

    // $ANTLR start "literalOrParameterName"
    // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:598:1: literalOrParameterName : ( literal |{...}? => parameterName );
    public final CMISParser.literalOrParameterName_return literalOrParameterName() throws RecognitionException
    {
        CMISParser.literalOrParameterName_return retval = new CMISParser.literalOrParameterName_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        ParserRuleReturnScope literal86 = null;
        ParserRuleReturnScope parameterName87 = null;

        try
        {
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:599:9: ( literal |{...}? => parameterName )
            int alt29 = 2;
            int LA29_0 = input.LA(1);
            if ((LA29_0 == DECIMAL_INTEGER_LITERAL || (LA29_0 >= FALSE && LA29_0 <= FLOATING_POINT_LITERAL) || LA29_0 == QUOTED_STRING || (LA29_0 >= TIMESTAMP && LA29_0 <= TRUE)))
            {
                alt29 = 1;
            }
            else if ((LA29_0 == COLON) && ((strict == false)))
            {
                alt29 = 2;
            }

            switch (alt29)
            {
            case 1:
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:600:9: literal
            {
                root_0 = (Object) adaptor.nil();

                pushFollow(FOLLOW_literal_in_literalOrParameterName3006);
                literal86 = literal();
                state._fsp--;
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                    adaptor.addChild(root_0, literal86.getTree());

            }
                break;
            case 2:
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:601:11: {...}? => parameterName
            {
                root_0 = (Object) adaptor.nil();

                if (!((strict == false)))
                {
                    if (state.backtracking > 0)
                    {
                        state.failed = true;
                        return retval;
                    }
                    throw new FailedPredicateException(input, "literalOrParameterName", "strict == false");
                }
                pushFollow(FOLLOW_parameterName_in_literalOrParameterName3021);
                parameterName87 = parameterName();
                state._fsp--;
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                    adaptor.addChild(root_0, parameterName87.getTree());

            }
                break;

            }
            retval.stop = input.LT(-1);

            if (state.backtracking == 0)
            {
                retval.tree = (Object) adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        catch (RecognitionException e)
        {
            throw new CmisInvalidArgumentException(getErrorString(e), e);
        }

        finally
        {
            // do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "literalOrParameterName"

    public static class literal_return extends ParserRuleReturnScope
    {
        Object tree;

        @Override
        public Object getTree()
        {
            return tree;
        }
    };

    // $ANTLR start "literal"
    // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:604:1: literal : ( signedNumericLiteral | characterStringLiteral | booleanLiteral | datetimeLiteral );
    public final CMISParser.literal_return literal() throws RecognitionException
    {
        CMISParser.literal_return retval = new CMISParser.literal_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        ParserRuleReturnScope signedNumericLiteral88 = null;
        ParserRuleReturnScope characterStringLiteral89 = null;
        ParserRuleReturnScope booleanLiteral90 = null;
        ParserRuleReturnScope datetimeLiteral91 = null;

        try
        {
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:605:9: ( signedNumericLiteral | characterStringLiteral | booleanLiteral | datetimeLiteral )
            int alt30 = 4;
            switch (input.LA(1))
            {
            case DECIMAL_INTEGER_LITERAL:
            case FLOATING_POINT_LITERAL:
            {
                alt30 = 1;
            }
                break;
            case QUOTED_STRING:
            {
                alt30 = 2;
            }
                break;
            case FALSE:
            case TRUE:
            {
                alt30 = 3;
            }
                break;
            case TIMESTAMP:
            {
                alt30 = 4;
            }
                break;
            default:
                if (state.backtracking > 0)
                {
                    state.failed = true;
                    return retval;
                }
                NoViableAltException nvae = new NoViableAltException("", 30, 0, input);
                throw nvae;
            }
            switch (alt30)
            {
            case 1:
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:606:9: signedNumericLiteral
            {
                root_0 = (Object) adaptor.nil();

                pushFollow(FOLLOW_signedNumericLiteral_in_literal3054);
                signedNumericLiteral88 = signedNumericLiteral();
                state._fsp--;
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                    adaptor.addChild(root_0, signedNumericLiteral88.getTree());

            }
                break;
            case 2:
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:607:11: characterStringLiteral
            {
                root_0 = (Object) adaptor.nil();

                pushFollow(FOLLOW_characterStringLiteral_in_literal3066);
                characterStringLiteral89 = characterStringLiteral();
                state._fsp--;
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                    adaptor.addChild(root_0, characterStringLiteral89.getTree());

            }
                break;
            case 3:
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:608:11: booleanLiteral
            {
                root_0 = (Object) adaptor.nil();

                pushFollow(FOLLOW_booleanLiteral_in_literal3078);
                booleanLiteral90 = booleanLiteral();
                state._fsp--;
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                    adaptor.addChild(root_0, booleanLiteral90.getTree());

            }
                break;
            case 4:
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:609:11: datetimeLiteral
            {
                root_0 = (Object) adaptor.nil();

                pushFollow(FOLLOW_datetimeLiteral_in_literal3090);
                datetimeLiteral91 = datetimeLiteral();
                state._fsp--;
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                    adaptor.addChild(root_0, datetimeLiteral91.getTree());

            }
                break;

            }
            retval.stop = input.LT(-1);

            if (state.backtracking == 0)
            {
                retval.tree = (Object) adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        catch (RecognitionException e)
        {
            throw new CmisInvalidArgumentException(getErrorString(e), e);
        }

        finally
        {
            // do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "literal"

    public static class inPredicate_return extends ParserRuleReturnScope
    {
        Object tree;

        @Override
        public Object getTree()
        {
            return tree;
        }
    };

    // $ANTLR start "inPredicate"
    // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:612:1: inPredicate : columnReference ( NOT )? IN LPAREN inValueList RPAREN -> ^( PRED_IN SINGLE_VALUED_PROPERTY columnReference inValueList ( NOT )? ) ;
    public final CMISParser.inPredicate_return inPredicate() throws RecognitionException
    {
        CMISParser.inPredicate_return retval = new CMISParser.inPredicate_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token NOT93 = null;
        Token IN94 = null;
        Token LPAREN95 = null;
        Token RPAREN97 = null;
        ParserRuleReturnScope columnReference92 = null;
        ParserRuleReturnScope inValueList96 = null;

        Object NOT93_tree = null;
        Object IN94_tree = null;
        Object LPAREN95_tree = null;
        Object RPAREN97_tree = null;
        RewriteRuleTokenStream stream_RPAREN = new RewriteRuleTokenStream(adaptor, "token RPAREN");
        RewriteRuleTokenStream stream_IN = new RewriteRuleTokenStream(adaptor, "token IN");
        RewriteRuleTokenStream stream_NOT = new RewriteRuleTokenStream(adaptor, "token NOT");
        RewriteRuleTokenStream stream_LPAREN = new RewriteRuleTokenStream(adaptor, "token LPAREN");
        RewriteRuleSubtreeStream stream_columnReference = new RewriteRuleSubtreeStream(adaptor, "rule columnReference");
        RewriteRuleSubtreeStream stream_inValueList = new RewriteRuleSubtreeStream(adaptor, "rule inValueList");

        try
        {
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:613:9: ( columnReference ( NOT )? IN LPAREN inValueList RPAREN -> ^( PRED_IN SINGLE_VALUED_PROPERTY columnReference inValueList ( NOT )? ) )
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:614:9: columnReference ( NOT )? IN LPAREN inValueList RPAREN
            {
                pushFollow(FOLLOW_columnReference_in_inPredicate3123);
                columnReference92 = columnReference();
                state._fsp--;
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                    stream_columnReference.add(columnReference92.getTree());
                // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:614:25: ( NOT )?
                int alt31 = 2;
                int LA31_0 = input.LA(1);
                if ((LA31_0 == NOT))
                {
                    alt31 = 1;
                }
                switch (alt31)
                {
                case 1:
                // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:614:25: NOT
                {
                    NOT93 = (Token) match(input, NOT, FOLLOW_NOT_in_inPredicate3125);
                    if (state.failed)
                        return retval;
                    if (state.backtracking == 0)
                        stream_NOT.add(NOT93);

                }
                    break;

                }

                IN94 = (Token) match(input, IN, FOLLOW_IN_in_inPredicate3128);
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                    stream_IN.add(IN94);

                LPAREN95 = (Token) match(input, LPAREN, FOLLOW_LPAREN_in_inPredicate3130);
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                    stream_LPAREN.add(LPAREN95);

                pushFollow(FOLLOW_inValueList_in_inPredicate3132);
                inValueList96 = inValueList();
                state._fsp--;
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                    stream_inValueList.add(inValueList96.getTree());
                RPAREN97 = (Token) match(input, RPAREN, FOLLOW_RPAREN_in_inPredicate3134);
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                    stream_RPAREN.add(RPAREN97);

                // AST REWRITE
                // elements: columnReference, NOT, inValueList
                // token labels:
                // rule labels: retval
                // token list labels:
                // rule list labels:
                // wildcard labels:
                if (state.backtracking == 0)
                {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval = new RewriteRuleSubtreeStream(adaptor, "rule retval", retval != null ? retval.getTree() : null);

                    root_0 = (Object) adaptor.nil();
                    // 615:17: -> ^( PRED_IN SINGLE_VALUED_PROPERTY columnReference inValueList ( NOT )? )
                    {
                        // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:616:25: ^( PRED_IN SINGLE_VALUED_PROPERTY columnReference inValueList ( NOT )? )
                        {
                            Object root_1 = (Object) adaptor.nil();
                            root_1 = (Object) adaptor.becomeRoot((Object) adaptor.create(PRED_IN, "PRED_IN"), root_1);
                            adaptor.addChild(root_1, (Object) adaptor.create(SINGLE_VALUED_PROPERTY, "SINGLE_VALUED_PROPERTY"));
                            adaptor.addChild(root_1, stream_columnReference.nextTree());
                            adaptor.addChild(root_1, stream_inValueList.nextTree());
                            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:616:86: ( NOT )?
                            if (stream_NOT.hasNext())
                            {
                                adaptor.addChild(root_1, stream_NOT.nextNode());
                            }
                            stream_NOT.reset();

                            adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;
                }

            }

            retval.stop = input.LT(-1);

            if (state.backtracking == 0)
            {
                retval.tree = (Object) adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        catch (RecognitionException e)
        {
            throw new CmisInvalidArgumentException(getErrorString(e), e);
        }

        finally
        {
            // do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "inPredicate"

    public static class inValueList_return extends ParserRuleReturnScope
    {
        Object tree;

        @Override
        public Object getTree()
        {
            return tree;
        }
    };

    // $ANTLR start "inValueList"
    // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:619:1: inValueList : literalOrParameterName ( COMMA literalOrParameterName )* -> ^( LIST ( literalOrParameterName )+ ) ;
    public final CMISParser.inValueList_return inValueList() throws RecognitionException
    {
        CMISParser.inValueList_return retval = new CMISParser.inValueList_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token COMMA99 = null;
        ParserRuleReturnScope literalOrParameterName98 = null;
        ParserRuleReturnScope literalOrParameterName100 = null;

        Object COMMA99_tree = null;
        RewriteRuleTokenStream stream_COMMA = new RewriteRuleTokenStream(adaptor, "token COMMA");
        RewriteRuleSubtreeStream stream_literalOrParameterName = new RewriteRuleSubtreeStream(adaptor, "rule literalOrParameterName");

        try
        {
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:620:9: ( literalOrParameterName ( COMMA literalOrParameterName )* -> ^( LIST ( literalOrParameterName )+ ) )
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:621:9: literalOrParameterName ( COMMA literalOrParameterName )*
            {
                pushFollow(FOLLOW_literalOrParameterName_in_inValueList3222);
                literalOrParameterName98 = literalOrParameterName();
                state._fsp--;
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                    stream_literalOrParameterName.add(literalOrParameterName98.getTree());
                // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:621:32: ( COMMA literalOrParameterName )*
                loop32: while (true)
                {
                    int alt32 = 2;
                    int LA32_0 = input.LA(1);
                    if ((LA32_0 == COMMA))
                    {
                        alt32 = 1;
                    }

                    switch (alt32)
                    {
                    case 1:
                    // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:621:33: COMMA literalOrParameterName
                    {
                        COMMA99 = (Token) match(input, COMMA, FOLLOW_COMMA_in_inValueList3225);
                        if (state.failed)
                            return retval;
                        if (state.backtracking == 0)
                            stream_COMMA.add(COMMA99);

                        pushFollow(FOLLOW_literalOrParameterName_in_inValueList3227);
                        literalOrParameterName100 = literalOrParameterName();
                        state._fsp--;
                        if (state.failed)
                            return retval;
                        if (state.backtracking == 0)
                            stream_literalOrParameterName.add(literalOrParameterName100.getTree());
                    }
                        break;

                    default:
                        break loop32;
                    }
                }

                // AST REWRITE
                // elements: literalOrParameterName
                // token labels:
                // rule labels: retval
                // token list labels:
                // rule list labels:
                // wildcard labels:
                if (state.backtracking == 0)
                {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval = new RewriteRuleSubtreeStream(adaptor, "rule retval", retval != null ? retval.getTree() : null);

                    root_0 = (Object) adaptor.nil();
                    // 622:17: -> ^( LIST ( literalOrParameterName )+ )
                    {
                        // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:623:25: ^( LIST ( literalOrParameterName )+ )
                        {
                            Object root_1 = (Object) adaptor.nil();
                            root_1 = (Object) adaptor.becomeRoot((Object) adaptor.create(LIST, "LIST"), root_1);
                            if (!(stream_literalOrParameterName.hasNext()))
                            {
                                throw new RewriteEarlyExitException();
                            }
                            while (stream_literalOrParameterName.hasNext())
                            {
                                adaptor.addChild(root_1, stream_literalOrParameterName.nextTree());
                            }
                            stream_literalOrParameterName.reset();

                            adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;
                }

            }

            retval.stop = input.LT(-1);

            if (state.backtracking == 0)
            {
                retval.tree = (Object) adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        catch (RecognitionException e)
        {
            throw new CmisInvalidArgumentException(getErrorString(e), e);
        }

        finally
        {
            // do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "inValueList"

    public static class likePredicate_return extends ParserRuleReturnScope
    {
        Object tree;

        @Override
        public Object getTree()
        {
            return tree;
        }
    };

    // $ANTLR start "likePredicate"
    // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:626:1: likePredicate : columnReference ( NOT )? LIKE characterStringLiteral -> ^( PRED_LIKE columnReference characterStringLiteral ( NOT )? ) ;
    public final CMISParser.likePredicate_return likePredicate() throws RecognitionException
    {
        CMISParser.likePredicate_return retval = new CMISParser.likePredicate_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token NOT102 = null;
        Token LIKE103 = null;
        ParserRuleReturnScope columnReference101 = null;
        ParserRuleReturnScope characterStringLiteral104 = null;

        Object NOT102_tree = null;
        Object LIKE103_tree = null;
        RewriteRuleTokenStream stream_NOT = new RewriteRuleTokenStream(adaptor, "token NOT");
        RewriteRuleTokenStream stream_LIKE = new RewriteRuleTokenStream(adaptor, "token LIKE");
        RewriteRuleSubtreeStream stream_columnReference = new RewriteRuleSubtreeStream(adaptor, "rule columnReference");
        RewriteRuleSubtreeStream stream_characterStringLiteral = new RewriteRuleSubtreeStream(adaptor, "rule characterStringLiteral");

        try
        {
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:627:9: ( columnReference ( NOT )? LIKE characterStringLiteral -> ^( PRED_LIKE columnReference characterStringLiteral ( NOT )? ) )
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:628:9: columnReference ( NOT )? LIKE characterStringLiteral
            {
                pushFollow(FOLLOW_columnReference_in_likePredicate3311);
                columnReference101 = columnReference();
                state._fsp--;
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                    stream_columnReference.add(columnReference101.getTree());
                // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:628:25: ( NOT )?
                int alt33 = 2;
                int LA33_0 = input.LA(1);
                if ((LA33_0 == NOT))
                {
                    alt33 = 1;
                }
                switch (alt33)
                {
                case 1:
                // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:628:25: NOT
                {
                    NOT102 = (Token) match(input, NOT, FOLLOW_NOT_in_likePredicate3313);
                    if (state.failed)
                        return retval;
                    if (state.backtracking == 0)
                        stream_NOT.add(NOT102);

                }
                    break;

                }

                LIKE103 = (Token) match(input, LIKE, FOLLOW_LIKE_in_likePredicate3316);
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                    stream_LIKE.add(LIKE103);

                pushFollow(FOLLOW_characterStringLiteral_in_likePredicate3318);
                characterStringLiteral104 = characterStringLiteral();
                state._fsp--;
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                    stream_characterStringLiteral.add(characterStringLiteral104.getTree());
                // AST REWRITE
                // elements: NOT, characterStringLiteral, columnReference
                // token labels:
                // rule labels: retval
                // token list labels:
                // rule list labels:
                // wildcard labels:
                if (state.backtracking == 0)
                {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval = new RewriteRuleSubtreeStream(adaptor, "rule retval", retval != null ? retval.getTree() : null);

                    root_0 = (Object) adaptor.nil();
                    // 629:17: -> ^( PRED_LIKE columnReference characterStringLiteral ( NOT )? )
                    {
                        // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:630:25: ^( PRED_LIKE columnReference characterStringLiteral ( NOT )? )
                        {
                            Object root_1 = (Object) adaptor.nil();
                            root_1 = (Object) adaptor.becomeRoot((Object) adaptor.create(PRED_LIKE, "PRED_LIKE"), root_1);
                            adaptor.addChild(root_1, stream_columnReference.nextTree());
                            adaptor.addChild(root_1, stream_characterStringLiteral.nextTree());
                            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:630:76: ( NOT )?
                            if (stream_NOT.hasNext())
                            {
                                adaptor.addChild(root_1, stream_NOT.nextNode());
                            }
                            stream_NOT.reset();

                            adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;
                }

            }

            retval.stop = input.LT(-1);

            if (state.backtracking == 0)
            {
                retval.tree = (Object) adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        catch (RecognitionException e)
        {
            throw new CmisInvalidArgumentException(getErrorString(e), e);
        }

        finally
        {
            // do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "likePredicate"

    public static class nullPredicate_return extends ParserRuleReturnScope
    {
        Object tree;

        @Override
        public Object getTree()
        {
            return tree;
        }
    };

    // $ANTLR start "nullPredicate"
    // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:633:1: nullPredicate : ( columnReference IS NULL -> ^( PRED_EXISTS columnReference NOT ) | columnReference IS NOT NULL -> ^( PRED_EXISTS columnReference ) );
    public final CMISParser.nullPredicate_return nullPredicate() throws RecognitionException
    {
        CMISParser.nullPredicate_return retval = new CMISParser.nullPredicate_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token IS106 = null;
        Token NULL107 = null;
        Token IS109 = null;
        Token NOT110 = null;
        Token NULL111 = null;
        ParserRuleReturnScope columnReference105 = null;
        ParserRuleReturnScope columnReference108 = null;

        Object IS106_tree = null;
        Object NULL107_tree = null;
        Object IS109_tree = null;
        Object NOT110_tree = null;
        Object NULL111_tree = null;
        RewriteRuleTokenStream stream_NOT = new RewriteRuleTokenStream(adaptor, "token NOT");
        RewriteRuleTokenStream stream_IS = new RewriteRuleTokenStream(adaptor, "token IS");
        RewriteRuleTokenStream stream_NULL = new RewriteRuleTokenStream(adaptor, "token NULL");
        RewriteRuleSubtreeStream stream_columnReference = new RewriteRuleSubtreeStream(adaptor, "rule columnReference");

        try
        {
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:634:9: ( columnReference IS NULL -> ^( PRED_EXISTS columnReference NOT ) | columnReference IS NOT NULL -> ^( PRED_EXISTS columnReference ) )
            int alt34 = 2;
            alt34 = dfa34.predict(input);
            switch (alt34)
            {
            case 1:
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:635:9: columnReference IS NULL
            {
                pushFollow(FOLLOW_columnReference_in_nullPredicate3404);
                columnReference105 = columnReference();
                state._fsp--;
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                    stream_columnReference.add(columnReference105.getTree());
                IS106 = (Token) match(input, IS, FOLLOW_IS_in_nullPredicate3406);
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                    stream_IS.add(IS106);

                NULL107 = (Token) match(input, NULL, FOLLOW_NULL_in_nullPredicate3408);
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                    stream_NULL.add(NULL107);

                // AST REWRITE
                // elements: columnReference
                // token labels:
                // rule labels: retval
                // token list labels:
                // rule list labels:
                // wildcard labels:
                if (state.backtracking == 0)
                {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval = new RewriteRuleSubtreeStream(adaptor, "rule retval", retval != null ? retval.getTree() : null);

                    root_0 = (Object) adaptor.nil();
                    // 636:17: -> ^( PRED_EXISTS columnReference NOT )
                    {
                        // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:637:25: ^( PRED_EXISTS columnReference NOT )
                        {
                            Object root_1 = (Object) adaptor.nil();
                            root_1 = (Object) adaptor.becomeRoot((Object) adaptor.create(PRED_EXISTS, "PRED_EXISTS"), root_1);
                            adaptor.addChild(root_1, stream_columnReference.nextTree());
                            adaptor.addChild(root_1, (Object) adaptor.create(NOT, "NOT"));
                            adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;
                }

            }
                break;
            case 2:
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:638:11: columnReference IS NOT NULL
            {
                pushFollow(FOLLOW_columnReference_in_nullPredicate3470);
                columnReference108 = columnReference();
                state._fsp--;
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                    stream_columnReference.add(columnReference108.getTree());
                IS109 = (Token) match(input, IS, FOLLOW_IS_in_nullPredicate3472);
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                    stream_IS.add(IS109);

                NOT110 = (Token) match(input, NOT, FOLLOW_NOT_in_nullPredicate3474);
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                    stream_NOT.add(NOT110);

                NULL111 = (Token) match(input, NULL, FOLLOW_NULL_in_nullPredicate3476);
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                    stream_NULL.add(NULL111);

                // AST REWRITE
                // elements: columnReference
                // token labels:
                // rule labels: retval
                // token list labels:
                // rule list labels:
                // wildcard labels:
                if (state.backtracking == 0)
                {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval = new RewriteRuleSubtreeStream(adaptor, "rule retval", retval != null ? retval.getTree() : null);

                    root_0 = (Object) adaptor.nil();
                    // 639:17: -> ^( PRED_EXISTS columnReference )
                    {
                        // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:640:25: ^( PRED_EXISTS columnReference )
                        {
                            Object root_1 = (Object) adaptor.nil();
                            root_1 = (Object) adaptor.becomeRoot((Object) adaptor.create(PRED_EXISTS, "PRED_EXISTS"), root_1);
                            adaptor.addChild(root_1, stream_columnReference.nextTree());
                            adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;
                }

            }
                break;

            }
            retval.stop = input.LT(-1);

            if (state.backtracking == 0)
            {
                retval.tree = (Object) adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        catch (RecognitionException e)
        {
            throw new CmisInvalidArgumentException(getErrorString(e), e);
        }

        finally
        {
            // do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "nullPredicate"

    public static class quantifiedComparisonPredicate_return extends ParserRuleReturnScope
    {
        Object tree;

        @Override
        public Object getTree()
        {
            return tree;
        }
    };

    // $ANTLR start "quantifiedComparisonPredicate"
    // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:643:1: quantifiedComparisonPredicate : literalOrParameterName compOp ANY columnReference -> ^( PRED_COMPARISON ANY literalOrParameterName compOp columnReference ) ;
    public final CMISParser.quantifiedComparisonPredicate_return quantifiedComparisonPredicate() throws RecognitionException
    {
        CMISParser.quantifiedComparisonPredicate_return retval = new CMISParser.quantifiedComparisonPredicate_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token ANY114 = null;
        ParserRuleReturnScope literalOrParameterName112 = null;
        ParserRuleReturnScope compOp113 = null;
        ParserRuleReturnScope columnReference115 = null;

        Object ANY114_tree = null;
        RewriteRuleTokenStream stream_ANY = new RewriteRuleTokenStream(adaptor, "token ANY");
        RewriteRuleSubtreeStream stream_columnReference = new RewriteRuleSubtreeStream(adaptor, "rule columnReference");
        RewriteRuleSubtreeStream stream_compOp = new RewriteRuleSubtreeStream(adaptor, "rule compOp");
        RewriteRuleSubtreeStream stream_literalOrParameterName = new RewriteRuleSubtreeStream(adaptor, "rule literalOrParameterName");

        try
        {
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:644:9: ( literalOrParameterName compOp ANY columnReference -> ^( PRED_COMPARISON ANY literalOrParameterName compOp columnReference ) )
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:645:9: literalOrParameterName compOp ANY columnReference
            {
                pushFollow(FOLLOW_literalOrParameterName_in_quantifiedComparisonPredicate3557);
                literalOrParameterName112 = literalOrParameterName();
                state._fsp--;
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                    stream_literalOrParameterName.add(literalOrParameterName112.getTree());
                pushFollow(FOLLOW_compOp_in_quantifiedComparisonPredicate3559);
                compOp113 = compOp();
                state._fsp--;
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                    stream_compOp.add(compOp113.getTree());
                ANY114 = (Token) match(input, ANY, FOLLOW_ANY_in_quantifiedComparisonPredicate3561);
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                    stream_ANY.add(ANY114);

                pushFollow(FOLLOW_columnReference_in_quantifiedComparisonPredicate3563);
                columnReference115 = columnReference();
                state._fsp--;
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                    stream_columnReference.add(columnReference115.getTree());
                // AST REWRITE
                // elements: ANY, literalOrParameterName, columnReference, compOp
                // token labels:
                // rule labels: retval
                // token list labels:
                // rule list labels:
                // wildcard labels:
                if (state.backtracking == 0)
                {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval = new RewriteRuleSubtreeStream(adaptor, "rule retval", retval != null ? retval.getTree() : null);

                    root_0 = (Object) adaptor.nil();
                    // 646:17: -> ^( PRED_COMPARISON ANY literalOrParameterName compOp columnReference )
                    {
                        // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:647:25: ^( PRED_COMPARISON ANY literalOrParameterName compOp columnReference )
                        {
                            Object root_1 = (Object) adaptor.nil();
                            root_1 = (Object) adaptor.becomeRoot((Object) adaptor.create(PRED_COMPARISON, "PRED_COMPARISON"), root_1);
                            adaptor.addChild(root_1, stream_ANY.nextNode());
                            adaptor.addChild(root_1, stream_literalOrParameterName.nextTree());
                            adaptor.addChild(root_1, stream_compOp.nextTree());
                            adaptor.addChild(root_1, stream_columnReference.nextTree());
                            adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;
                }

            }

            retval.stop = input.LT(-1);

            if (state.backtracking == 0)
            {
                retval.tree = (Object) adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        catch (RecognitionException e)
        {
            throw new CmisInvalidArgumentException(getErrorString(e), e);
        }

        finally
        {
            // do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "quantifiedComparisonPredicate"

    public static class quantifiedInPredicate_return extends ParserRuleReturnScope
    {
        Object tree;

        @Override
        public Object getTree()
        {
            return tree;
        }
    };

    // $ANTLR start "quantifiedInPredicate"
    // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:650:1: quantifiedInPredicate : ANY columnReference ( NOT )? IN LPAREN inValueList RPAREN -> ^( PRED_IN ANY columnReference inValueList ( NOT )? ) ;
    public final CMISParser.quantifiedInPredicate_return quantifiedInPredicate() throws RecognitionException
    {
        CMISParser.quantifiedInPredicate_return retval = new CMISParser.quantifiedInPredicate_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token ANY116 = null;
        Token NOT118 = null;
        Token IN119 = null;
        Token LPAREN120 = null;
        Token RPAREN122 = null;
        ParserRuleReturnScope columnReference117 = null;
        ParserRuleReturnScope inValueList121 = null;

        Object ANY116_tree = null;
        Object NOT118_tree = null;
        Object IN119_tree = null;
        Object LPAREN120_tree = null;
        Object RPAREN122_tree = null;
        RewriteRuleTokenStream stream_RPAREN = new RewriteRuleTokenStream(adaptor, "token RPAREN");
        RewriteRuleTokenStream stream_ANY = new RewriteRuleTokenStream(adaptor, "token ANY");
        RewriteRuleTokenStream stream_IN = new RewriteRuleTokenStream(adaptor, "token IN");
        RewriteRuleTokenStream stream_NOT = new RewriteRuleTokenStream(adaptor, "token NOT");
        RewriteRuleTokenStream stream_LPAREN = new RewriteRuleTokenStream(adaptor, "token LPAREN");
        RewriteRuleSubtreeStream stream_columnReference = new RewriteRuleSubtreeStream(adaptor, "rule columnReference");
        RewriteRuleSubtreeStream stream_inValueList = new RewriteRuleSubtreeStream(adaptor, "rule inValueList");

        try
        {
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:651:9: ( ANY columnReference ( NOT )? IN LPAREN inValueList RPAREN -> ^( PRED_IN ANY columnReference inValueList ( NOT )? ) )
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:652:9: ANY columnReference ( NOT )? IN LPAREN inValueList RPAREN
            {
                ANY116 = (Token) match(input, ANY, FOLLOW_ANY_in_quantifiedInPredicate3650);
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                    stream_ANY.add(ANY116);

                pushFollow(FOLLOW_columnReference_in_quantifiedInPredicate3652);
                columnReference117 = columnReference();
                state._fsp--;
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                    stream_columnReference.add(columnReference117.getTree());
                // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:652:29: ( NOT )?
                int alt35 = 2;
                int LA35_0 = input.LA(1);
                if ((LA35_0 == NOT))
                {
                    alt35 = 1;
                }
                switch (alt35)
                {
                case 1:
                // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:652:29: NOT
                {
                    NOT118 = (Token) match(input, NOT, FOLLOW_NOT_in_quantifiedInPredicate3654);
                    if (state.failed)
                        return retval;
                    if (state.backtracking == 0)
                        stream_NOT.add(NOT118);

                }
                    break;

                }

                IN119 = (Token) match(input, IN, FOLLOW_IN_in_quantifiedInPredicate3657);
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                    stream_IN.add(IN119);

                LPAREN120 = (Token) match(input, LPAREN, FOLLOW_LPAREN_in_quantifiedInPredicate3659);
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                    stream_LPAREN.add(LPAREN120);

                pushFollow(FOLLOW_inValueList_in_quantifiedInPredicate3661);
                inValueList121 = inValueList();
                state._fsp--;
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                    stream_inValueList.add(inValueList121.getTree());
                RPAREN122 = (Token) match(input, RPAREN, FOLLOW_RPAREN_in_quantifiedInPredicate3663);
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                    stream_RPAREN.add(RPAREN122);

                // AST REWRITE
                // elements: columnReference, inValueList, ANY, NOT
                // token labels:
                // rule labels: retval
                // token list labels:
                // rule list labels:
                // wildcard labels:
                if (state.backtracking == 0)
                {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval = new RewriteRuleSubtreeStream(adaptor, "rule retval", retval != null ? retval.getTree() : null);

                    root_0 = (Object) adaptor.nil();
                    // 653:17: -> ^( PRED_IN ANY columnReference inValueList ( NOT )? )
                    {
                        // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:654:25: ^( PRED_IN ANY columnReference inValueList ( NOT )? )
                        {
                            Object root_1 = (Object) adaptor.nil();
                            root_1 = (Object) adaptor.becomeRoot((Object) adaptor.create(PRED_IN, "PRED_IN"), root_1);
                            adaptor.addChild(root_1, stream_ANY.nextNode());
                            adaptor.addChild(root_1, stream_columnReference.nextTree());
                            adaptor.addChild(root_1, stream_inValueList.nextTree());
                            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:654:67: ( NOT )?
                            if (stream_NOT.hasNext())
                            {
                                adaptor.addChild(root_1, stream_NOT.nextNode());
                            }
                            stream_NOT.reset();

                            adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;
                }

            }

            retval.stop = input.LT(-1);

            if (state.backtracking == 0)
            {
                retval.tree = (Object) adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        catch (RecognitionException e)
        {
            throw new CmisInvalidArgumentException(getErrorString(e), e);
        }

        finally
        {
            // do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "quantifiedInPredicate"

    public static class textSearchPredicate_return extends ParserRuleReturnScope
    {
        Object tree;

        @Override
        public Object getTree()
        {
            return tree;
        }
    };

    // $ANTLR start "textSearchPredicate"
    // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:657:1: textSearchPredicate : CONTAINS LPAREN ( qualifier COMMA )? textSearchExpression RPAREN -> ^( PRED_FTS textSearchExpression ( qualifier )? ) ;
    public final CMISParser.textSearchPredicate_return textSearchPredicate() throws RecognitionException
    {
        CMISParser.textSearchPredicate_return retval = new CMISParser.textSearchPredicate_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token CONTAINS123 = null;
        Token LPAREN124 = null;
        Token COMMA126 = null;
        Token RPAREN128 = null;
        ParserRuleReturnScope qualifier125 = null;
        ParserRuleReturnScope textSearchExpression127 = null;

        Object CONTAINS123_tree = null;
        Object LPAREN124_tree = null;
        Object COMMA126_tree = null;
        Object RPAREN128_tree = null;
        RewriteRuleTokenStream stream_RPAREN = new RewriteRuleTokenStream(adaptor, "token RPAREN");
        RewriteRuleTokenStream stream_COMMA = new RewriteRuleTokenStream(adaptor, "token COMMA");
        RewriteRuleTokenStream stream_CONTAINS = new RewriteRuleTokenStream(adaptor, "token CONTAINS");
        RewriteRuleTokenStream stream_LPAREN = new RewriteRuleTokenStream(adaptor, "token LPAREN");
        RewriteRuleSubtreeStream stream_qualifier = new RewriteRuleSubtreeStream(adaptor, "rule qualifier");
        RewriteRuleSubtreeStream stream_textSearchExpression = new RewriteRuleSubtreeStream(adaptor, "rule textSearchExpression");

        try
        {
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:658:9: ( CONTAINS LPAREN ( qualifier COMMA )? textSearchExpression RPAREN -> ^( PRED_FTS textSearchExpression ( qualifier )? ) )
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:659:9: CONTAINS LPAREN ( qualifier COMMA )? textSearchExpression RPAREN
            {
                CONTAINS123 = (Token) match(input, CONTAINS, FOLLOW_CONTAINS_in_textSearchPredicate3751);
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                    stream_CONTAINS.add(CONTAINS123);

                LPAREN124 = (Token) match(input, LPAREN, FOLLOW_LPAREN_in_textSearchPredicate3753);
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                    stream_LPAREN.add(LPAREN124);

                // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:659:25: ( qualifier COMMA )?
                int alt36 = 2;
                int LA36_0 = input.LA(1);
                if ((LA36_0 == ID))
                {
                    alt36 = 1;
                }
                else if ((LA36_0 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    alt36 = 1;
                }
                switch (alt36)
                {
                case 1:
                // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:659:26: qualifier COMMA
                {
                    pushFollow(FOLLOW_qualifier_in_textSearchPredicate3756);
                    qualifier125 = qualifier();
                    state._fsp--;
                    if (state.failed)
                        return retval;
                    if (state.backtracking == 0)
                        stream_qualifier.add(qualifier125.getTree());
                    COMMA126 = (Token) match(input, COMMA, FOLLOW_COMMA_in_textSearchPredicate3758);
                    if (state.failed)
                        return retval;
                    if (state.backtracking == 0)
                        stream_COMMA.add(COMMA126);

                }
                    break;

                }

                pushFollow(FOLLOW_textSearchExpression_in_textSearchPredicate3762);
                textSearchExpression127 = textSearchExpression();
                state._fsp--;
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                    stream_textSearchExpression.add(textSearchExpression127.getTree());
                RPAREN128 = (Token) match(input, RPAREN, FOLLOW_RPAREN_in_textSearchPredicate3764);
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                    stream_RPAREN.add(RPAREN128);

                // AST REWRITE
                // elements: qualifier, textSearchExpression
                // token labels:
                // rule labels: retval
                // token list labels:
                // rule list labels:
                // wildcard labels:
                if (state.backtracking == 0)
                {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval = new RewriteRuleSubtreeStream(adaptor, "rule retval", retval != null ? retval.getTree() : null);

                    root_0 = (Object) adaptor.nil();
                    // 660:17: -> ^( PRED_FTS textSearchExpression ( qualifier )? )
                    {
                        // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:661:25: ^( PRED_FTS textSearchExpression ( qualifier )? )
                        {
                            Object root_1 = (Object) adaptor.nil();
                            root_1 = (Object) adaptor.becomeRoot((Object) adaptor.create(PRED_FTS, "PRED_FTS"), root_1);
                            adaptor.addChild(root_1, stream_textSearchExpression.nextTree());
                            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:661:57: ( qualifier )?
                            if (stream_qualifier.hasNext())
                            {
                                adaptor.addChild(root_1, stream_qualifier.nextTree());
                            }
                            stream_qualifier.reset();

                            adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;
                }

            }

            retval.stop = input.LT(-1);

            if (state.backtracking == 0)
            {
                retval.tree = (Object) adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        catch (RecognitionException e)
        {
            throw new CmisInvalidArgumentException(getErrorString(e), e);
        }

        finally
        {
            // do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "textSearchPredicate"

    public static class folderPredicate_return extends ParserRuleReturnScope
    {
        Object tree;

        @Override
        public Object getTree()
        {
            return tree;
        }
    };

    // $ANTLR start "folderPredicate"
    // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:664:1: folderPredicate : ( IN_FOLDER folderPredicateArgs -> ^( PRED_CHILD folderPredicateArgs ) | IN_TREE folderPredicateArgs -> ^( PRED_DESCENDANT folderPredicateArgs ) );
    public final CMISParser.folderPredicate_return folderPredicate() throws RecognitionException
    {
        CMISParser.folderPredicate_return retval = new CMISParser.folderPredicate_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token IN_FOLDER129 = null;
        Token IN_TREE131 = null;
        ParserRuleReturnScope folderPredicateArgs130 = null;
        ParserRuleReturnScope folderPredicateArgs132 = null;

        Object IN_FOLDER129_tree = null;
        Object IN_TREE131_tree = null;
        RewriteRuleTokenStream stream_IN_TREE = new RewriteRuleTokenStream(adaptor, "token IN_TREE");
        RewriteRuleTokenStream stream_IN_FOLDER = new RewriteRuleTokenStream(adaptor, "token IN_FOLDER");
        RewriteRuleSubtreeStream stream_folderPredicateArgs = new RewriteRuleSubtreeStream(adaptor, "rule folderPredicateArgs");

        try
        {
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:665:9: ( IN_FOLDER folderPredicateArgs -> ^( PRED_CHILD folderPredicateArgs ) | IN_TREE folderPredicateArgs -> ^( PRED_DESCENDANT folderPredicateArgs ) )
            int alt37 = 2;
            int LA37_0 = input.LA(1);
            if ((LA37_0 == IN_FOLDER))
            {
                alt37 = 1;
            }
            else if ((LA37_0 == IN_TREE))
            {
                alt37 = 2;
            }

            else
            {
                if (state.backtracking > 0)
                {
                    state.failed = true;
                    return retval;
                }
                NoViableAltException nvae = new NoViableAltException("", 37, 0, input);
                throw nvae;
            }

            switch (alt37)
            {
            case 1:
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:666:9: IN_FOLDER folderPredicateArgs
            {
                IN_FOLDER129 = (Token) match(input, IN_FOLDER, FOLLOW_IN_FOLDER_in_folderPredicate3848);
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                    stream_IN_FOLDER.add(IN_FOLDER129);

                pushFollow(FOLLOW_folderPredicateArgs_in_folderPredicate3850);
                folderPredicateArgs130 = folderPredicateArgs();
                state._fsp--;
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                    stream_folderPredicateArgs.add(folderPredicateArgs130.getTree());
                // AST REWRITE
                // elements: folderPredicateArgs
                // token labels:
                // rule labels: retval
                // token list labels:
                // rule list labels:
                // wildcard labels:
                if (state.backtracking == 0)
                {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval = new RewriteRuleSubtreeStream(adaptor, "rule retval", retval != null ? retval.getTree() : null);

                    root_0 = (Object) adaptor.nil();
                    // 667:17: -> ^( PRED_CHILD folderPredicateArgs )
                    {
                        // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:668:25: ^( PRED_CHILD folderPredicateArgs )
                        {
                            Object root_1 = (Object) adaptor.nil();
                            root_1 = (Object) adaptor.becomeRoot((Object) adaptor.create(PRED_CHILD, "PRED_CHILD"), root_1);
                            adaptor.addChild(root_1, stream_folderPredicateArgs.nextTree());
                            adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;
                }

            }
                break;
            case 2:
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:669:11: IN_TREE folderPredicateArgs
            {
                IN_TREE131 = (Token) match(input, IN_TREE, FOLLOW_IN_TREE_in_folderPredicate3910);
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                    stream_IN_TREE.add(IN_TREE131);

                pushFollow(FOLLOW_folderPredicateArgs_in_folderPredicate3912);
                folderPredicateArgs132 = folderPredicateArgs();
                state._fsp--;
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                    stream_folderPredicateArgs.add(folderPredicateArgs132.getTree());
                // AST REWRITE
                // elements: folderPredicateArgs
                // token labels:
                // rule labels: retval
                // token list labels:
                // rule list labels:
                // wildcard labels:
                if (state.backtracking == 0)
                {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval = new RewriteRuleSubtreeStream(adaptor, "rule retval", retval != null ? retval.getTree() : null);

                    root_0 = (Object) adaptor.nil();
                    // 670:17: -> ^( PRED_DESCENDANT folderPredicateArgs )
                    {
                        // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:671:25: ^( PRED_DESCENDANT folderPredicateArgs )
                        {
                            Object root_1 = (Object) adaptor.nil();
                            root_1 = (Object) adaptor.becomeRoot((Object) adaptor.create(PRED_DESCENDANT, "PRED_DESCENDANT"), root_1);
                            adaptor.addChild(root_1, stream_folderPredicateArgs.nextTree());
                            adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;
                }

            }
                break;

            }
            retval.stop = input.LT(-1);

            if (state.backtracking == 0)
            {
                retval.tree = (Object) adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        catch (RecognitionException e)
        {
            throw new CmisInvalidArgumentException(getErrorString(e), e);
        }

        finally
        {
            // do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "folderPredicate"

    public static class folderPredicateArgs_return extends ParserRuleReturnScope
    {
        Object tree;

        @Override
        public Object getTree()
        {
            return tree;
        }
    };

    // $ANTLR start "folderPredicateArgs"
    // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:674:1: folderPredicateArgs : LPAREN ( qualifier COMMA )? folderId RPAREN -> folderId ( qualifier )? ;
    public final CMISParser.folderPredicateArgs_return folderPredicateArgs() throws RecognitionException
    {
        CMISParser.folderPredicateArgs_return retval = new CMISParser.folderPredicateArgs_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token LPAREN133 = null;
        Token COMMA135 = null;
        Token RPAREN137 = null;
        ParserRuleReturnScope qualifier134 = null;
        ParserRuleReturnScope folderId136 = null;

        Object LPAREN133_tree = null;
        Object COMMA135_tree = null;
        Object RPAREN137_tree = null;
        RewriteRuleTokenStream stream_RPAREN = new RewriteRuleTokenStream(adaptor, "token RPAREN");
        RewriteRuleTokenStream stream_COMMA = new RewriteRuleTokenStream(adaptor, "token COMMA");
        RewriteRuleTokenStream stream_LPAREN = new RewriteRuleTokenStream(adaptor, "token LPAREN");
        RewriteRuleSubtreeStream stream_qualifier = new RewriteRuleSubtreeStream(adaptor, "rule qualifier");
        RewriteRuleSubtreeStream stream_folderId = new RewriteRuleSubtreeStream(adaptor, "rule folderId");

        try
        {
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:675:9: ( LPAREN ( qualifier COMMA )? folderId RPAREN -> folderId ( qualifier )? )
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:676:9: LPAREN ( qualifier COMMA )? folderId RPAREN
            {
                LPAREN133 = (Token) match(input, LPAREN, FOLLOW_LPAREN_in_folderPredicateArgs3993);
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                    stream_LPAREN.add(LPAREN133);

                // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:676:16: ( qualifier COMMA )?
                int alt38 = 2;
                int LA38_0 = input.LA(1);
                if ((LA38_0 == ID))
                {
                    alt38 = 1;
                }
                else if ((LA38_0 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    alt38 = 1;
                }
                switch (alt38)
                {
                case 1:
                // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:676:17: qualifier COMMA
                {
                    pushFollow(FOLLOW_qualifier_in_folderPredicateArgs3996);
                    qualifier134 = qualifier();
                    state._fsp--;
                    if (state.failed)
                        return retval;
                    if (state.backtracking == 0)
                        stream_qualifier.add(qualifier134.getTree());
                    COMMA135 = (Token) match(input, COMMA, FOLLOW_COMMA_in_folderPredicateArgs3998);
                    if (state.failed)
                        return retval;
                    if (state.backtracking == 0)
                        stream_COMMA.add(COMMA135);

                }
                    break;

                }

                pushFollow(FOLLOW_folderId_in_folderPredicateArgs4002);
                folderId136 = folderId();
                state._fsp--;
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                    stream_folderId.add(folderId136.getTree());
                RPAREN137 = (Token) match(input, RPAREN, FOLLOW_RPAREN_in_folderPredicateArgs4004);
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                    stream_RPAREN.add(RPAREN137);

                // AST REWRITE
                // elements: folderId, qualifier
                // token labels:
                // rule labels: retval
                // token list labels:
                // rule list labels:
                // wildcard labels:
                if (state.backtracking == 0)
                {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval = new RewriteRuleSubtreeStream(adaptor, "rule retval", retval != null ? retval.getTree() : null);

                    root_0 = (Object) adaptor.nil();
                    // 677:17: -> folderId ( qualifier )?
                    {
                        adaptor.addChild(root_0, stream_folderId.nextTree());
                        // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:677:29: ( qualifier )?
                        if (stream_qualifier.hasNext())
                        {
                            adaptor.addChild(root_0, stream_qualifier.nextTree());
                        }
                        stream_qualifier.reset();

                    }

                    retval.tree = root_0;
                }

            }

            retval.stop = input.LT(-1);

            if (state.backtracking == 0)
            {
                retval.tree = (Object) adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        catch (RecognitionException e)
        {
            throw new CmisInvalidArgumentException(getErrorString(e), e);
        }

        finally
        {
            // do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "folderPredicateArgs"

    public static class orderByClause_return extends ParserRuleReturnScope
    {
        Object tree;

        @Override
        public Object getTree()
        {
            return tree;
        }
    };

    // $ANTLR start "orderByClause"
    // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:680:1: orderByClause : ORDER BY sortSpecification ( COMMA sortSpecification )* -> ^( ORDER ( sortSpecification )+ ) ;
    public final CMISParser.orderByClause_return orderByClause() throws RecognitionException
    {
        CMISParser.orderByClause_return retval = new CMISParser.orderByClause_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token ORDER138 = null;
        Token BY139 = null;
        Token COMMA141 = null;
        ParserRuleReturnScope sortSpecification140 = null;
        ParserRuleReturnScope sortSpecification142 = null;

        Object ORDER138_tree = null;
        Object BY139_tree = null;
        Object COMMA141_tree = null;
        RewriteRuleTokenStream stream_BY = new RewriteRuleTokenStream(adaptor, "token BY");
        RewriteRuleTokenStream stream_ORDER = new RewriteRuleTokenStream(adaptor, "token ORDER");
        RewriteRuleTokenStream stream_COMMA = new RewriteRuleTokenStream(adaptor, "token COMMA");
        RewriteRuleSubtreeStream stream_sortSpecification = new RewriteRuleSubtreeStream(adaptor, "rule sortSpecification");

        paraphrases.push("in order by");

        try
        {
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:689:9: ( ORDER BY sortSpecification ( COMMA sortSpecification )* -> ^( ORDER ( sortSpecification )+ ) )
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:690:9: ORDER BY sortSpecification ( COMMA sortSpecification )*
            {
                ORDER138 = (Token) match(input, ORDER, FOLLOW_ORDER_in_orderByClause4070);
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                    stream_ORDER.add(ORDER138);

                BY139 = (Token) match(input, BY, FOLLOW_BY_in_orderByClause4072);
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                    stream_BY.add(BY139);

                pushFollow(FOLLOW_sortSpecification_in_orderByClause4074);
                sortSpecification140 = sortSpecification();
                state._fsp--;
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                    stream_sortSpecification.add(sortSpecification140.getTree());
                // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:690:36: ( COMMA sortSpecification )*
                loop39: while (true)
                {
                    int alt39 = 2;
                    int LA39_0 = input.LA(1);
                    if ((LA39_0 == COMMA))
                    {
                        alt39 = 1;
                    }

                    switch (alt39)
                    {
                    case 1:
                    // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:690:37: COMMA sortSpecification
                    {
                        COMMA141 = (Token) match(input, COMMA, FOLLOW_COMMA_in_orderByClause4077);
                        if (state.failed)
                            return retval;
                        if (state.backtracking == 0)
                            stream_COMMA.add(COMMA141);

                        pushFollow(FOLLOW_sortSpecification_in_orderByClause4079);
                        sortSpecification142 = sortSpecification();
                        state._fsp--;
                        if (state.failed)
                            return retval;
                        if (state.backtracking == 0)
                            stream_sortSpecification.add(sortSpecification142.getTree());
                    }
                        break;

                    default:
                        break loop39;
                    }
                }

                // AST REWRITE
                // elements: sortSpecification, ORDER
                // token labels:
                // rule labels: retval
                // token list labels:
                // rule list labels:
                // wildcard labels:
                if (state.backtracking == 0)
                {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval = new RewriteRuleSubtreeStream(adaptor, "rule retval", retval != null ? retval.getTree() : null);

                    root_0 = (Object) adaptor.nil();
                    // 691:17: -> ^( ORDER ( sortSpecification )+ )
                    {
                        // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:692:25: ^( ORDER ( sortSpecification )+ )
                        {
                            Object root_1 = (Object) adaptor.nil();
                            root_1 = (Object) adaptor.becomeRoot(stream_ORDER.nextNode(), root_1);
                            if (!(stream_sortSpecification.hasNext()))
                            {
                                throw new RewriteEarlyExitException();
                            }
                            while (stream_sortSpecification.hasNext())
                            {
                                adaptor.addChild(root_1, stream_sortSpecification.nextTree());
                            }
                            stream_sortSpecification.reset();

                            adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;
                }

            }

            retval.stop = input.LT(-1);

            if (state.backtracking == 0)
            {
                retval.tree = (Object) adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
            if (state.backtracking == 0)
            {
                paraphrases.pop();
            }
        }

        catch (RecognitionException e)
        {
            throw new CmisInvalidArgumentException(getErrorString(e), e);
        }

        finally
        {
            // do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "orderByClause"

    public static class sortSpecification_return extends ParserRuleReturnScope
    {
        Object tree;

        @Override
        public Object getTree()
        {
            return tree;
        }
    };

    // $ANTLR start "sortSpecification"
    // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:695:1: sortSpecification : ( columnReference -> ^( SORT_SPECIFICATION columnReference ASC ) | columnReference (by= ASC |by= DESC ) -> ^( SORT_SPECIFICATION columnReference $by) );
    public final CMISParser.sortSpecification_return sortSpecification() throws RecognitionException
    {
        CMISParser.sortSpecification_return retval = new CMISParser.sortSpecification_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token by = null;
        ParserRuleReturnScope columnReference143 = null;
        ParserRuleReturnScope columnReference144 = null;

        Object by_tree = null;
        RewriteRuleTokenStream stream_ASC = new RewriteRuleTokenStream(adaptor, "token ASC");
        RewriteRuleTokenStream stream_DESC = new RewriteRuleTokenStream(adaptor, "token DESC");
        RewriteRuleSubtreeStream stream_columnReference = new RewriteRuleSubtreeStream(adaptor, "rule columnReference");

        try
        {
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:696:9: ( columnReference -> ^( SORT_SPECIFICATION columnReference ASC ) | columnReference (by= ASC |by= DESC ) -> ^( SORT_SPECIFICATION columnReference $by) )
            int alt41 = 2;
            alt41 = dfa41.predict(input);
            switch (alt41)
            {
            case 1:
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:697:9: columnReference
            {
                pushFollow(FOLLOW_columnReference_in_sortSpecification4163);
                columnReference143 = columnReference();
                state._fsp--;
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                    stream_columnReference.add(columnReference143.getTree());
                // AST REWRITE
                // elements: columnReference
                // token labels:
                // rule labels: retval
                // token list labels:
                // rule list labels:
                // wildcard labels:
                if (state.backtracking == 0)
                {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval = new RewriteRuleSubtreeStream(adaptor, "rule retval", retval != null ? retval.getTree() : null);

                    root_0 = (Object) adaptor.nil();
                    // 698:17: -> ^( SORT_SPECIFICATION columnReference ASC )
                    {
                        // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:699:25: ^( SORT_SPECIFICATION columnReference ASC )
                        {
                            Object root_1 = (Object) adaptor.nil();
                            root_1 = (Object) adaptor.becomeRoot((Object) adaptor.create(SORT_SPECIFICATION, "SORT_SPECIFICATION"), root_1);
                            adaptor.addChild(root_1, stream_columnReference.nextTree());
                            adaptor.addChild(root_1, (Object) adaptor.create(ASC, "ASC"));
                            adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;
                }

            }
                break;
            case 2:
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:700:11: columnReference (by= ASC |by= DESC )
            {
                pushFollow(FOLLOW_columnReference_in_sortSpecification4225);
                columnReference144 = columnReference();
                state._fsp--;
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                    stream_columnReference.add(columnReference144.getTree());
                // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:701:9: (by= ASC |by= DESC )
                int alt40 = 2;
                int LA40_0 = input.LA(1);
                if ((LA40_0 == ASC))
                {
                    alt40 = 1;
                }
                else if ((LA40_0 == DESC))
                {
                    alt40 = 2;
                }

                else
                {
                    if (state.backtracking > 0)
                    {
                        state.failed = true;
                        return retval;
                    }
                    NoViableAltException nvae = new NoViableAltException("", 40, 0, input);
                    throw nvae;
                }

                switch (alt40)
                {
                case 1:
                // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:702:17: by= ASC
                {
                    by = (Token) match(input, ASC, FOLLOW_ASC_in_sortSpecification4255);
                    if (state.failed)
                        return retval;
                    if (state.backtracking == 0)
                        stream_ASC.add(by);

                }
                    break;
                case 2:
                // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:703:19: by= DESC
                {
                    by = (Token) match(input, DESC, FOLLOW_DESC_in_sortSpecification4277);
                    if (state.failed)
                        return retval;
                    if (state.backtracking == 0)
                        stream_DESC.add(by);

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
                if (state.backtracking == 0)
                {
                    retval.tree = root_0;
                    RewriteRuleTokenStream stream_by = new RewriteRuleTokenStream(adaptor, "token by", by);
                    RewriteRuleSubtreeStream stream_retval = new RewriteRuleSubtreeStream(adaptor, "rule retval", retval != null ? retval.getTree() : null);

                    root_0 = (Object) adaptor.nil();
                    // 705:17: -> ^( SORT_SPECIFICATION columnReference $by)
                    {
                        // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:706:25: ^( SORT_SPECIFICATION columnReference $by)
                        {
                            Object root_1 = (Object) adaptor.nil();
                            root_1 = (Object) adaptor.becomeRoot((Object) adaptor.create(SORT_SPECIFICATION, "SORT_SPECIFICATION"), root_1);
                            adaptor.addChild(root_1, stream_columnReference.nextTree());
                            adaptor.addChild(root_1, stream_by.nextNode());
                            adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;
                }

            }
                break;

            }
            retval.stop = input.LT(-1);

            if (state.backtracking == 0)
            {
                retval.tree = (Object) adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        catch (RecognitionException e)
        {
            throw new CmisInvalidArgumentException(getErrorString(e), e);
        }

        finally
        {
            // do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "sortSpecification"

    public static class correlationName_return extends ParserRuleReturnScope
    {
        Object tree;

        @Override
        public Object getTree()
        {
            return tree;
        }
    };

    // $ANTLR start "correlationName"
    // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:709:1: correlationName : identifier ;
    public final CMISParser.correlationName_return correlationName() throws RecognitionException
    {
        CMISParser.correlationName_return retval = new CMISParser.correlationName_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        ParserRuleReturnScope identifier145 = null;

        try
        {
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:710:9: ( identifier )
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:711:9: identifier
            {
                root_0 = (Object) adaptor.nil();

                pushFollow(FOLLOW_identifier_in_correlationName4371);
                identifier145 = identifier();
                state._fsp--;
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                    adaptor.addChild(root_0, identifier145.getTree());

            }

            retval.stop = input.LT(-1);

            if (state.backtracking == 0)
            {
                retval.tree = (Object) adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        catch (RecognitionException e)
        {
            throw new CmisInvalidArgumentException(getErrorString(e), e);
        }

        finally
        {
            // do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "correlationName"

    public static class tableName_return extends ParserRuleReturnScope
    {
        Object tree;

        @Override
        public Object getTree()
        {
            return tree;
        }
    };

    // $ANTLR start "tableName"
    // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:719:1: tableName : identifier -> identifier ;
    public final CMISParser.tableName_return tableName() throws RecognitionException
    {
        CMISParser.tableName_return retval = new CMISParser.tableName_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        ParserRuleReturnScope identifier146 = null;

        RewriteRuleSubtreeStream stream_identifier = new RewriteRuleSubtreeStream(adaptor, "rule identifier");

        try
        {
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:720:9: ( identifier -> identifier )
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:721:9: identifier
            {
                pushFollow(FOLLOW_identifier_in_tableName4407);
                identifier146 = identifier();
                state._fsp--;
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                    stream_identifier.add(identifier146.getTree());
                // AST REWRITE
                // elements: identifier
                // token labels:
                // rule labels: retval
                // token list labels:
                // rule list labels:
                // wildcard labels:
                if (state.backtracking == 0)
                {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval = new RewriteRuleSubtreeStream(adaptor, "rule retval", retval != null ? retval.getTree() : null);

                    root_0 = (Object) adaptor.nil();
                    // 722:17: -> identifier
                    {
                        adaptor.addChild(root_0, stream_identifier.nextTree());
                    }

                    retval.tree = root_0;
                }

            }

            retval.stop = input.LT(-1);

            if (state.backtracking == 0)
            {
                retval.tree = (Object) adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        catch (RecognitionException e)
        {
            throw new CmisInvalidArgumentException(getErrorString(e), e);
        }

        finally
        {
            // do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "tableName"

    public static class columnName_return extends ParserRuleReturnScope
    {
        Object tree;

        @Override
        public Object getTree()
        {
            return tree;
        }
    };

    // $ANTLR start "columnName"
    // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:725:1: columnName : identifier -> identifier ;
    public final CMISParser.columnName_return columnName() throws RecognitionException
    {
        CMISParser.columnName_return retval = new CMISParser.columnName_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        ParserRuleReturnScope identifier147 = null;

        RewriteRuleSubtreeStream stream_identifier = new RewriteRuleSubtreeStream(adaptor, "rule identifier");

        try
        {
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:726:9: ( identifier -> identifier )
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:727:9: identifier
            {
                pushFollow(FOLLOW_identifier_in_columnName4460);
                identifier147 = identifier();
                state._fsp--;
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                    stream_identifier.add(identifier147.getTree());
                // AST REWRITE
                // elements: identifier
                // token labels:
                // rule labels: retval
                // token list labels:
                // rule list labels:
                // wildcard labels:
                if (state.backtracking == 0)
                {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval = new RewriteRuleSubtreeStream(adaptor, "rule retval", retval != null ? retval.getTree() : null);

                    root_0 = (Object) adaptor.nil();
                    // 728:17: -> identifier
                    {
                        adaptor.addChild(root_0, stream_identifier.nextTree());
                    }

                    retval.tree = root_0;
                }

            }

            retval.stop = input.LT(-1);

            if (state.backtracking == 0)
            {
                retval.tree = (Object) adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        catch (RecognitionException e)
        {
            throw new CmisInvalidArgumentException(getErrorString(e), e);
        }

        finally
        {
            // do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "columnName"

    public static class parameterName_return extends ParserRuleReturnScope
    {
        Object tree;

        @Override
        public Object getTree()
        {
            return tree;
        }
    };

    // $ANTLR start "parameterName"
    // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:731:1: parameterName : COLON identifier -> ^( PARAMETER identifier ) ;
    public final CMISParser.parameterName_return parameterName() throws RecognitionException
    {
        CMISParser.parameterName_return retval = new CMISParser.parameterName_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token COLON148 = null;
        ParserRuleReturnScope identifier149 = null;

        Object COLON148_tree = null;
        RewriteRuleTokenStream stream_COLON = new RewriteRuleTokenStream(adaptor, "token COLON");
        RewriteRuleSubtreeStream stream_identifier = new RewriteRuleSubtreeStream(adaptor, "rule identifier");

        try
        {
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:732:9: ( COLON identifier -> ^( PARAMETER identifier ) )
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:733:9: COLON identifier
            {
                COLON148 = (Token) match(input, COLON, FOLLOW_COLON_in_parameterName4513);
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                    stream_COLON.add(COLON148);

                pushFollow(FOLLOW_identifier_in_parameterName4515);
                identifier149 = identifier();
                state._fsp--;
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                    stream_identifier.add(identifier149.getTree());
                // AST REWRITE
                // elements: identifier
                // token labels:
                // rule labels: retval
                // token list labels:
                // rule list labels:
                // wildcard labels:
                if (state.backtracking == 0)
                {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval = new RewriteRuleSubtreeStream(adaptor, "rule retval", retval != null ? retval.getTree() : null);

                    root_0 = (Object) adaptor.nil();
                    // 734:17: -> ^( PARAMETER identifier )
                    {
                        // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:735:25: ^( PARAMETER identifier )
                        {
                            Object root_1 = (Object) adaptor.nil();
                            root_1 = (Object) adaptor.becomeRoot((Object) adaptor.create(PARAMETER, "PARAMETER"), root_1);
                            adaptor.addChild(root_1, stream_identifier.nextTree());
                            adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;
                }

            }

            retval.stop = input.LT(-1);

            if (state.backtracking == 0)
            {
                retval.tree = (Object) adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        catch (RecognitionException e)
        {
            throw new CmisInvalidArgumentException(getErrorString(e), e);
        }

        finally
        {
            // do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "parameterName"

    public static class folderId_return extends ParserRuleReturnScope
    {
        Object tree;

        @Override
        public Object getTree()
        {
            return tree;
        }
    };

    // $ANTLR start "folderId"
    // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:738:1: folderId : characterStringLiteral -> characterStringLiteral ;
    public final CMISParser.folderId_return folderId() throws RecognitionException
    {
        CMISParser.folderId_return retval = new CMISParser.folderId_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        ParserRuleReturnScope characterStringLiteral150 = null;

        RewriteRuleSubtreeStream stream_characterStringLiteral = new RewriteRuleSubtreeStream(adaptor, "rule characterStringLiteral");

        try
        {
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:739:9: ( characterStringLiteral -> characterStringLiteral )
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:740:9: characterStringLiteral
            {
                pushFollow(FOLLOW_characterStringLiteral_in_folderId4596);
                characterStringLiteral150 = characterStringLiteral();
                state._fsp--;
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                    stream_characterStringLiteral.add(characterStringLiteral150.getTree());
                // AST REWRITE
                // elements: characterStringLiteral
                // token labels:
                // rule labels: retval
                // token list labels:
                // rule list labels:
                // wildcard labels:
                if (state.backtracking == 0)
                {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval = new RewriteRuleSubtreeStream(adaptor, "rule retval", retval != null ? retval.getTree() : null);

                    root_0 = (Object) adaptor.nil();
                    // 741:17: -> characterStringLiteral
                    {
                        adaptor.addChild(root_0, stream_characterStringLiteral.nextTree());
                    }

                    retval.tree = root_0;
                }

            }

            retval.stop = input.LT(-1);

            if (state.backtracking == 0)
            {
                retval.tree = (Object) adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        catch (RecognitionException e)
        {
            throw new CmisInvalidArgumentException(getErrorString(e), e);
        }

        finally
        {
            // do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "folderId"

    public static class textSearchExpression_return extends ParserRuleReturnScope
    {
        Object tree;

        @Override
        public Object getTree()
        {
            return tree;
        }
    };

    // $ANTLR start "textSearchExpression"
    // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:744:1: textSearchExpression : QUOTED_STRING ;
    public final CMISParser.textSearchExpression_return textSearchExpression() throws RecognitionException
    {
        CMISParser.textSearchExpression_return retval = new CMISParser.textSearchExpression_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token QUOTED_STRING151 = null;

        Object QUOTED_STRING151_tree = null;

        try
        {
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:745:9: ( QUOTED_STRING )
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:746:9: QUOTED_STRING
            {
                root_0 = (Object) adaptor.nil();

                QUOTED_STRING151 = (Token) match(input, QUOTED_STRING, FOLLOW_QUOTED_STRING_in_textSearchExpression4649);
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                {
                    QUOTED_STRING151_tree = (Object) adaptor.create(QUOTED_STRING151);
                    adaptor.addChild(root_0, QUOTED_STRING151_tree);
                }

            }

            retval.stop = input.LT(-1);

            if (state.backtracking == 0)
            {
                retval.tree = (Object) adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        catch (RecognitionException e)
        {
            throw new CmisInvalidArgumentException(getErrorString(e), e);
        }

        finally
        {
            // do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "textSearchExpression"

    public static class identifier_return extends ParserRuleReturnScope
    {
        Object tree;

        @Override
        public Object getTree()
        {
            return tree;
        }
    };

    // $ANTLR start "identifier"
    // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:749:1: identifier : ( ID -> ID |{...}? => DOUBLE_QUOTE keyWordOrId DOUBLE_QUOTE -> ^( keyWordOrId ) );
    public final CMISParser.identifier_return identifier() throws RecognitionException
    {
        CMISParser.identifier_return retval = new CMISParser.identifier_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token ID152 = null;
        Token DOUBLE_QUOTE153 = null;
        Token DOUBLE_QUOTE155 = null;
        ParserRuleReturnScope keyWordOrId154 = null;

        Object ID152_tree = null;
        Object DOUBLE_QUOTE153_tree = null;
        Object DOUBLE_QUOTE155_tree = null;
        RewriteRuleTokenStream stream_ID = new RewriteRuleTokenStream(adaptor, "token ID");
        RewriteRuleTokenStream stream_DOUBLE_QUOTE = new RewriteRuleTokenStream(adaptor, "token DOUBLE_QUOTE");
        RewriteRuleSubtreeStream stream_keyWordOrId = new RewriteRuleSubtreeStream(adaptor, "rule keyWordOrId");

        try
        {
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:750:9: ( ID -> ID |{...}? => DOUBLE_QUOTE keyWordOrId DOUBLE_QUOTE -> ^( keyWordOrId ) )
            int alt42 = 2;
            int LA42_0 = input.LA(1);
            if ((LA42_0 == ID))
            {
                alt42 = 1;
            }
            else if ((LA42_0 == DOUBLE_QUOTE) && ((strict == false)))
            {
                alt42 = 2;
            }

            switch (alt42)
            {
            case 1:
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:751:9: ID
            {
                ID152 = (Token) match(input, ID, FOLLOW_ID_in_identifier4682);
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                    stream_ID.add(ID152);

                // AST REWRITE
                // elements: ID
                // token labels:
                // rule labels: retval
                // token list labels:
                // rule list labels:
                // wildcard labels:
                if (state.backtracking == 0)
                {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval = new RewriteRuleSubtreeStream(adaptor, "rule retval", retval != null ? retval.getTree() : null);

                    root_0 = (Object) adaptor.nil();
                    // 752:17: -> ID
                    {
                        adaptor.addChild(root_0, stream_ID.nextNode());
                    }

                    retval.tree = root_0;
                }

            }
                break;
            case 2:
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:753:11: {...}? => DOUBLE_QUOTE keyWordOrId DOUBLE_QUOTE
            {
                if (!((strict == false)))
                {
                    if (state.backtracking > 0)
                    {
                        state.failed = true;
                        return retval;
                    }
                    throw new FailedPredicateException(input, "identifier", "strict == false");
                }
                DOUBLE_QUOTE153 = (Token) match(input, DOUBLE_QUOTE, FOLLOW_DOUBLE_QUOTE_in_identifier4717);
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                    stream_DOUBLE_QUOTE.add(DOUBLE_QUOTE153);

                pushFollow(FOLLOW_keyWordOrId_in_identifier4719);
                keyWordOrId154 = keyWordOrId();
                state._fsp--;
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                    stream_keyWordOrId.add(keyWordOrId154.getTree());
                DOUBLE_QUOTE155 = (Token) match(input, DOUBLE_QUOTE, FOLLOW_DOUBLE_QUOTE_in_identifier4721);
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                    stream_DOUBLE_QUOTE.add(DOUBLE_QUOTE155);

                // AST REWRITE
                // elements: keyWordOrId
                // token labels:
                // rule labels: retval
                // token list labels:
                // rule list labels:
                // wildcard labels:
                if (state.backtracking == 0)
                {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval = new RewriteRuleSubtreeStream(adaptor, "rule retval", retval != null ? retval.getTree() : null);

                    root_0 = (Object) adaptor.nil();
                    // 754:17: -> ^( keyWordOrId )
                    {
                        // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:755:25: ^( keyWordOrId )
                        {
                            Object root_1 = (Object) adaptor.nil();
                            root_1 = (Object) adaptor.becomeRoot(stream_keyWordOrId.nextNode(), root_1);
                            adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;
                }

            }
                break;

            }
            retval.stop = input.LT(-1);

            if (state.backtracking == 0)
            {
                retval.tree = (Object) adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        catch (RecognitionException e)
        {
            throw new CmisInvalidArgumentException(getErrorString(e), e);
        }

        finally
        {
            // do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "identifier"

    public static class signedNumericLiteral_return extends ParserRuleReturnScope
    {
        Object tree;

        @Override
        public Object getTree()
        {
            return tree;
        }
    };

    // $ANTLR start "signedNumericLiteral"
    // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:758:1: signedNumericLiteral : ( FLOATING_POINT_LITERAL -> ^( NUMERIC_LITERAL FLOATING_POINT_LITERAL ) | integerLiteral -> integerLiteral );
    public final CMISParser.signedNumericLiteral_return signedNumericLiteral() throws RecognitionException
    {
        CMISParser.signedNumericLiteral_return retval = new CMISParser.signedNumericLiteral_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token FLOATING_POINT_LITERAL156 = null;
        ParserRuleReturnScope integerLiteral157 = null;

        Object FLOATING_POINT_LITERAL156_tree = null;
        RewriteRuleTokenStream stream_FLOATING_POINT_LITERAL = new RewriteRuleTokenStream(adaptor, "token FLOATING_POINT_LITERAL");
        RewriteRuleSubtreeStream stream_integerLiteral = new RewriteRuleSubtreeStream(adaptor, "rule integerLiteral");

        try
        {
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:759:9: ( FLOATING_POINT_LITERAL -> ^( NUMERIC_LITERAL FLOATING_POINT_LITERAL ) | integerLiteral -> integerLiteral )
            int alt43 = 2;
            int LA43_0 = input.LA(1);
            if ((LA43_0 == FLOATING_POINT_LITERAL))
            {
                alt43 = 1;
            }
            else if ((LA43_0 == DECIMAL_INTEGER_LITERAL))
            {
                alt43 = 2;
            }

            else
            {
                if (state.backtracking > 0)
                {
                    state.failed = true;
                    return retval;
                }
                NoViableAltException nvae = new NoViableAltException("", 43, 0, input);
                throw nvae;
            }

            switch (alt43)
            {
            case 1:
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:760:9: FLOATING_POINT_LITERAL
            {
                FLOATING_POINT_LITERAL156 = (Token) match(input, FLOATING_POINT_LITERAL, FOLLOW_FLOATING_POINT_LITERAL_in_signedNumericLiteral4800);
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                    stream_FLOATING_POINT_LITERAL.add(FLOATING_POINT_LITERAL156);

                // AST REWRITE
                // elements: FLOATING_POINT_LITERAL
                // token labels:
                // rule labels: retval
                // token list labels:
                // rule list labels:
                // wildcard labels:
                if (state.backtracking == 0)
                {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval = new RewriteRuleSubtreeStream(adaptor, "rule retval", retval != null ? retval.getTree() : null);

                    root_0 = (Object) adaptor.nil();
                    // 761:17: -> ^( NUMERIC_LITERAL FLOATING_POINT_LITERAL )
                    {
                        // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:762:25: ^( NUMERIC_LITERAL FLOATING_POINT_LITERAL )
                        {
                            Object root_1 = (Object) adaptor.nil();
                            root_1 = (Object) adaptor.becomeRoot((Object) adaptor.create(NUMERIC_LITERAL, "NUMERIC_LITERAL"), root_1);
                            adaptor.addChild(root_1, stream_FLOATING_POINT_LITERAL.nextNode());
                            adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;
                }

            }
                break;
            case 2:
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:763:11: integerLiteral
            {
                pushFollow(FOLLOW_integerLiteral_in_signedNumericLiteral4860);
                integerLiteral157 = integerLiteral();
                state._fsp--;
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                    stream_integerLiteral.add(integerLiteral157.getTree());
                // AST REWRITE
                // elements: integerLiteral
                // token labels:
                // rule labels: retval
                // token list labels:
                // rule list labels:
                // wildcard labels:
                if (state.backtracking == 0)
                {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval = new RewriteRuleSubtreeStream(adaptor, "rule retval", retval != null ? retval.getTree() : null);

                    root_0 = (Object) adaptor.nil();
                    // 764:17: -> integerLiteral
                    {
                        adaptor.addChild(root_0, stream_integerLiteral.nextTree());
                    }

                    retval.tree = root_0;
                }

            }
                break;

            }
            retval.stop = input.LT(-1);

            if (state.backtracking == 0)
            {
                retval.tree = (Object) adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        catch (RecognitionException e)
        {
            throw new CmisInvalidArgumentException(getErrorString(e), e);
        }

        finally
        {
            // do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "signedNumericLiteral"

    public static class integerLiteral_return extends ParserRuleReturnScope
    {
        Object tree;

        @Override
        public Object getTree()
        {
            return tree;
        }
    };

    // $ANTLR start "integerLiteral"
    // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:767:1: integerLiteral : DECIMAL_INTEGER_LITERAL -> ^( NUMERIC_LITERAL DECIMAL_INTEGER_LITERAL ) ;
    public final CMISParser.integerLiteral_return integerLiteral() throws RecognitionException
    {
        CMISParser.integerLiteral_return retval = new CMISParser.integerLiteral_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token DECIMAL_INTEGER_LITERAL158 = null;

        Object DECIMAL_INTEGER_LITERAL158_tree = null;
        RewriteRuleTokenStream stream_DECIMAL_INTEGER_LITERAL = new RewriteRuleTokenStream(adaptor, "token DECIMAL_INTEGER_LITERAL");

        try
        {
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:768:9: ( DECIMAL_INTEGER_LITERAL -> ^( NUMERIC_LITERAL DECIMAL_INTEGER_LITERAL ) )
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:769:9: DECIMAL_INTEGER_LITERAL
            {
                DECIMAL_INTEGER_LITERAL158 = (Token) match(input, DECIMAL_INTEGER_LITERAL, FOLLOW_DECIMAL_INTEGER_LITERAL_in_integerLiteral4913);
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                    stream_DECIMAL_INTEGER_LITERAL.add(DECIMAL_INTEGER_LITERAL158);

                // AST REWRITE
                // elements: DECIMAL_INTEGER_LITERAL
                // token labels:
                // rule labels: retval
                // token list labels:
                // rule list labels:
                // wildcard labels:
                if (state.backtracking == 0)
                {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval = new RewriteRuleSubtreeStream(adaptor, "rule retval", retval != null ? retval.getTree() : null);

                    root_0 = (Object) adaptor.nil();
                    // 770:17: -> ^( NUMERIC_LITERAL DECIMAL_INTEGER_LITERAL )
                    {
                        // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:771:25: ^( NUMERIC_LITERAL DECIMAL_INTEGER_LITERAL )
                        {
                            Object root_1 = (Object) adaptor.nil();
                            root_1 = (Object) adaptor.becomeRoot((Object) adaptor.create(NUMERIC_LITERAL, "NUMERIC_LITERAL"), root_1);
                            adaptor.addChild(root_1, stream_DECIMAL_INTEGER_LITERAL.nextNode());
                            adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;
                }

            }

            retval.stop = input.LT(-1);

            if (state.backtracking == 0)
            {
                retval.tree = (Object) adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        catch (RecognitionException e)
        {
            throw new CmisInvalidArgumentException(getErrorString(e), e);
        }

        finally
        {
            // do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "integerLiteral"

    public static class booleanLiteral_return extends ParserRuleReturnScope
    {
        Object tree;

        @Override
        public Object getTree()
        {
            return tree;
        }
    };

    // $ANTLR start "booleanLiteral"
    // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:774:1: booleanLiteral : ( TRUE -> ^( BOOLEAN_LITERAL TRUE ) | FALSE -> ^( BOOLEAN_LITERAL FALSE ) );
    public final CMISParser.booleanLiteral_return booleanLiteral() throws RecognitionException
    {
        CMISParser.booleanLiteral_return retval = new CMISParser.booleanLiteral_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token TRUE159 = null;
        Token FALSE160 = null;

        Object TRUE159_tree = null;
        Object FALSE160_tree = null;
        RewriteRuleTokenStream stream_FALSE = new RewriteRuleTokenStream(adaptor, "token FALSE");
        RewriteRuleTokenStream stream_TRUE = new RewriteRuleTokenStream(adaptor, "token TRUE");

        try
        {
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:775:9: ( TRUE -> ^( BOOLEAN_LITERAL TRUE ) | FALSE -> ^( BOOLEAN_LITERAL FALSE ) )
            int alt44 = 2;
            int LA44_0 = input.LA(1);
            if ((LA44_0 == TRUE))
            {
                alt44 = 1;
            }
            else if ((LA44_0 == FALSE))
            {
                alt44 = 2;
            }

            else
            {
                if (state.backtracking > 0)
                {
                    state.failed = true;
                    return retval;
                }
                NoViableAltException nvae = new NoViableAltException("", 44, 0, input);
                throw nvae;
            }

            switch (alt44)
            {
            case 1:
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:776:9: TRUE
            {
                TRUE159 = (Token) match(input, TRUE, FOLLOW_TRUE_in_booleanLiteral4994);
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                    stream_TRUE.add(TRUE159);

                // AST REWRITE
                // elements: TRUE
                // token labels:
                // rule labels: retval
                // token list labels:
                // rule list labels:
                // wildcard labels:
                if (state.backtracking == 0)
                {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval = new RewriteRuleSubtreeStream(adaptor, "rule retval", retval != null ? retval.getTree() : null);

                    root_0 = (Object) adaptor.nil();
                    // 777:17: -> ^( BOOLEAN_LITERAL TRUE )
                    {
                        // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:778:25: ^( BOOLEAN_LITERAL TRUE )
                        {
                            Object root_1 = (Object) adaptor.nil();
                            root_1 = (Object) adaptor.becomeRoot((Object) adaptor.create(BOOLEAN_LITERAL, "BOOLEAN_LITERAL"), root_1);
                            adaptor.addChild(root_1, stream_TRUE.nextNode());
                            adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;
                }

            }
                break;
            case 2:
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:779:11: FALSE
            {
                FALSE160 = (Token) match(input, FALSE, FOLLOW_FALSE_in_booleanLiteral5054);
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                    stream_FALSE.add(FALSE160);

                // AST REWRITE
                // elements: FALSE
                // token labels:
                // rule labels: retval
                // token list labels:
                // rule list labels:
                // wildcard labels:
                if (state.backtracking == 0)
                {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval = new RewriteRuleSubtreeStream(adaptor, "rule retval", retval != null ? retval.getTree() : null);

                    root_0 = (Object) adaptor.nil();
                    // 780:17: -> ^( BOOLEAN_LITERAL FALSE )
                    {
                        // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:781:25: ^( BOOLEAN_LITERAL FALSE )
                        {
                            Object root_1 = (Object) adaptor.nil();
                            root_1 = (Object) adaptor.becomeRoot((Object) adaptor.create(BOOLEAN_LITERAL, "BOOLEAN_LITERAL"), root_1);
                            adaptor.addChild(root_1, stream_FALSE.nextNode());
                            adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;
                }

            }
                break;

            }
            retval.stop = input.LT(-1);

            if (state.backtracking == 0)
            {
                retval.tree = (Object) adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        catch (RecognitionException e)
        {
            throw new CmisInvalidArgumentException(getErrorString(e), e);
        }

        finally
        {
            // do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "booleanLiteral"

    public static class datetimeLiteral_return extends ParserRuleReturnScope
    {
        Object tree;

        @Override
        public Object getTree()
        {
            return tree;
        }
    };

    // $ANTLR start "datetimeLiteral"
    // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:784:1: datetimeLiteral : TIMESTAMP QUOTED_STRING -> ^( DATETIME_LITERAL QUOTED_STRING ) ;
    public final CMISParser.datetimeLiteral_return datetimeLiteral() throws RecognitionException
    {
        CMISParser.datetimeLiteral_return retval = new CMISParser.datetimeLiteral_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token TIMESTAMP161 = null;
        Token QUOTED_STRING162 = null;

        Object TIMESTAMP161_tree = null;
        Object QUOTED_STRING162_tree = null;
        RewriteRuleTokenStream stream_QUOTED_STRING = new RewriteRuleTokenStream(adaptor, "token QUOTED_STRING");
        RewriteRuleTokenStream stream_TIMESTAMP = new RewriteRuleTokenStream(adaptor, "token TIMESTAMP");

        try
        {
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:785:9: ( TIMESTAMP QUOTED_STRING -> ^( DATETIME_LITERAL QUOTED_STRING ) )
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:786:9: TIMESTAMP QUOTED_STRING
            {
                TIMESTAMP161 = (Token) match(input, TIMESTAMP, FOLLOW_TIMESTAMP_in_datetimeLiteral5135);
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                    stream_TIMESTAMP.add(TIMESTAMP161);

                QUOTED_STRING162 = (Token) match(input, QUOTED_STRING, FOLLOW_QUOTED_STRING_in_datetimeLiteral5137);
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                    stream_QUOTED_STRING.add(QUOTED_STRING162);

                // AST REWRITE
                // elements: QUOTED_STRING
                // token labels:
                // rule labels: retval
                // token list labels:
                // rule list labels:
                // wildcard labels:
                if (state.backtracking == 0)
                {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval = new RewriteRuleSubtreeStream(adaptor, "rule retval", retval != null ? retval.getTree() : null);

                    root_0 = (Object) adaptor.nil();
                    // 787:17: -> ^( DATETIME_LITERAL QUOTED_STRING )
                    {
                        // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:788:25: ^( DATETIME_LITERAL QUOTED_STRING )
                        {
                            Object root_1 = (Object) adaptor.nil();
                            root_1 = (Object) adaptor.becomeRoot((Object) adaptor.create(DATETIME_LITERAL, "DATETIME_LITERAL"), root_1);
                            adaptor.addChild(root_1, stream_QUOTED_STRING.nextNode());
                            adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;
                }

            }

            retval.stop = input.LT(-1);

            if (state.backtracking == 0)
            {
                retval.tree = (Object) adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        catch (RecognitionException e)
        {
            throw new CmisInvalidArgumentException(getErrorString(e), e);
        }

        finally
        {
            // do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "datetimeLiteral"

    public static class characterStringLiteral_return extends ParserRuleReturnScope
    {
        Object tree;

        @Override
        public Object getTree()
        {
            return tree;
        }
    };

    // $ANTLR start "characterStringLiteral"
    // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:791:1: characterStringLiteral : QUOTED_STRING -> ^( STRING_LITERAL QUOTED_STRING ) ;
    public final CMISParser.characterStringLiteral_return characterStringLiteral() throws RecognitionException
    {
        CMISParser.characterStringLiteral_return retval = new CMISParser.characterStringLiteral_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token QUOTED_STRING163 = null;

        Object QUOTED_STRING163_tree = null;
        RewriteRuleTokenStream stream_QUOTED_STRING = new RewriteRuleTokenStream(adaptor, "token QUOTED_STRING");

        try
        {
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:792:9: ( QUOTED_STRING -> ^( STRING_LITERAL QUOTED_STRING ) )
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:793:9: QUOTED_STRING
            {
                QUOTED_STRING163 = (Token) match(input, QUOTED_STRING, FOLLOW_QUOTED_STRING_in_characterStringLiteral5218);
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                    stream_QUOTED_STRING.add(QUOTED_STRING163);

                // AST REWRITE
                // elements: QUOTED_STRING
                // token labels:
                // rule labels: retval
                // token list labels:
                // rule list labels:
                // wildcard labels:
                if (state.backtracking == 0)
                {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval = new RewriteRuleSubtreeStream(adaptor, "rule retval", retval != null ? retval.getTree() : null);

                    root_0 = (Object) adaptor.nil();
                    // 794:17: -> ^( STRING_LITERAL QUOTED_STRING )
                    {
                        // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:795:25: ^( STRING_LITERAL QUOTED_STRING )
                        {
                            Object root_1 = (Object) adaptor.nil();
                            root_1 = (Object) adaptor.becomeRoot((Object) adaptor.create(STRING_LITERAL, "STRING_LITERAL"), root_1);
                            adaptor.addChild(root_1, stream_QUOTED_STRING.nextNode());
                            adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;
                }

            }

            retval.stop = input.LT(-1);

            if (state.backtracking == 0)
            {
                retval.tree = (Object) adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        catch (RecognitionException e)
        {
            throw new CmisInvalidArgumentException(getErrorString(e), e);
        }

        finally
        {
            // do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "characterStringLiteral"

    public static class keyWord_return extends ParserRuleReturnScope
    {
        Object tree;

        @Override
        public Object getTree()
        {
            return tree;
        }
    };

    // $ANTLR start "keyWord"
    // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:798:1: keyWord : ( SELECT | AS | FROM | JOIN | INNER | LEFT | OUTER | ON | WHERE | OR | AND | NOT | IN | LIKE | IS | NULL | ANY | CONTAINS | IN_FOLDER | IN_TREE | ORDER | BY | ASC | DESC | TIMESTAMP | TRUE | FALSE | cmisFunction );
    public final CMISParser.keyWord_return keyWord() throws RecognitionException
    {
        CMISParser.keyWord_return retval = new CMISParser.keyWord_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token SELECT164 = null;
        Token AS165 = null;
        Token FROM166 = null;
        Token JOIN167 = null;
        Token INNER168 = null;
        Token LEFT169 = null;
        Token OUTER170 = null;
        Token ON171 = null;
        Token WHERE172 = null;
        Token OR173 = null;
        Token AND174 = null;
        Token NOT175 = null;
        Token IN176 = null;
        Token LIKE177 = null;
        Token IS178 = null;
        Token NULL179 = null;
        Token ANY180 = null;
        Token CONTAINS181 = null;
        Token IN_FOLDER182 = null;
        Token IN_TREE183 = null;
        Token ORDER184 = null;
        Token BY185 = null;
        Token ASC186 = null;
        Token DESC187 = null;
        Token TIMESTAMP188 = null;
        Token TRUE189 = null;
        Token FALSE190 = null;
        ParserRuleReturnScope cmisFunction191 = null;

        Object SELECT164_tree = null;
        Object AS165_tree = null;
        Object FROM166_tree = null;
        Object JOIN167_tree = null;
        Object INNER168_tree = null;
        Object LEFT169_tree = null;
        Object OUTER170_tree = null;
        Object ON171_tree = null;
        Object WHERE172_tree = null;
        Object OR173_tree = null;
        Object AND174_tree = null;
        Object NOT175_tree = null;
        Object IN176_tree = null;
        Object LIKE177_tree = null;
        Object IS178_tree = null;
        Object NULL179_tree = null;
        Object ANY180_tree = null;
        Object CONTAINS181_tree = null;
        Object IN_FOLDER182_tree = null;
        Object IN_TREE183_tree = null;
        Object ORDER184_tree = null;
        Object BY185_tree = null;
        Object ASC186_tree = null;
        Object DESC187_tree = null;
        Object TIMESTAMP188_tree = null;
        Object TRUE189_tree = null;
        Object FALSE190_tree = null;

        try
        {
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:799:9: ( SELECT | AS | FROM | JOIN | INNER | LEFT | OUTER | ON | WHERE | OR | AND | NOT | IN | LIKE | IS | NULL | ANY | CONTAINS | IN_FOLDER | IN_TREE | ORDER | BY | ASC | DESC | TIMESTAMP | TRUE | FALSE | cmisFunction )
            int alt45 = 28;
            switch (input.LA(1))
            {
            case SELECT:
            {
                alt45 = 1;
            }
                break;
            case AS:
            {
                alt45 = 2;
            }
                break;
            case FROM:
            {
                alt45 = 3;
            }
                break;
            case JOIN:
            {
                alt45 = 4;
            }
                break;
            case INNER:
            {
                alt45 = 5;
            }
                break;
            case LEFT:
            {
                alt45 = 6;
            }
                break;
            case OUTER:
            {
                alt45 = 7;
            }
                break;
            case ON:
            {
                alt45 = 8;
            }
                break;
            case WHERE:
            {
                alt45 = 9;
            }
                break;
            case OR:
            {
                alt45 = 10;
            }
                break;
            case AND:
            {
                alt45 = 11;
            }
                break;
            case NOT:
            {
                alt45 = 12;
            }
                break;
            case IN:
            {
                alt45 = 13;
            }
                break;
            case LIKE:
            {
                alt45 = 14;
            }
                break;
            case IS:
            {
                alt45 = 15;
            }
                break;
            case NULL:
            {
                alt45 = 16;
            }
                break;
            case ANY:
            {
                alt45 = 17;
            }
                break;
            case CONTAINS:
            {
                alt45 = 18;
            }
                break;
            case IN_FOLDER:
            {
                alt45 = 19;
            }
                break;
            case IN_TREE:
            {
                alt45 = 20;
            }
                break;
            case ORDER:
            {
                alt45 = 21;
            }
                break;
            case BY:
            {
                alt45 = 22;
            }
                break;
            case ASC:
            {
                alt45 = 23;
            }
                break;
            case DESC:
            {
                alt45 = 24;
            }
                break;
            case TIMESTAMP:
            {
                alt45 = 25;
            }
                break;
            case TRUE:
            {
                alt45 = 26;
            }
                break;
            case FALSE:
            {
                alt45 = 27;
            }
                break;
            case SCORE:
            {
                alt45 = 28;
            }
                break;
            default:
                if (state.backtracking > 0)
                {
                    state.failed = true;
                    return retval;
                }
                NoViableAltException nvae = new NoViableAltException("", 45, 0, input);
                throw nvae;
            }
            switch (alt45)
            {
            case 1:
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:800:9: SELECT
            {
                root_0 = (Object) adaptor.nil();

                SELECT164 = (Token) match(input, SELECT, FOLLOW_SELECT_in_keyWord5299);
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                {
                    SELECT164_tree = (Object) adaptor.create(SELECT164);
                    adaptor.addChild(root_0, SELECT164_tree);
                }

            }
                break;
            case 2:
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:801:11: AS
            {
                root_0 = (Object) adaptor.nil();

                AS165 = (Token) match(input, AS, FOLLOW_AS_in_keyWord5311);
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                {
                    AS165_tree = (Object) adaptor.create(AS165);
                    adaptor.addChild(root_0, AS165_tree);
                }

            }
                break;
            case 3:
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:802:11: FROM
            {
                root_0 = (Object) adaptor.nil();

                FROM166 = (Token) match(input, FROM, FOLLOW_FROM_in_keyWord5323);
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                {
                    FROM166_tree = (Object) adaptor.create(FROM166);
                    adaptor.addChild(root_0, FROM166_tree);
                }

            }
                break;
            case 4:
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:803:11: JOIN
            {
                root_0 = (Object) adaptor.nil();

                JOIN167 = (Token) match(input, JOIN, FOLLOW_JOIN_in_keyWord5335);
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                {
                    JOIN167_tree = (Object) adaptor.create(JOIN167);
                    adaptor.addChild(root_0, JOIN167_tree);
                }

            }
                break;
            case 5:
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:804:11: INNER
            {
                root_0 = (Object) adaptor.nil();

                INNER168 = (Token) match(input, INNER, FOLLOW_INNER_in_keyWord5347);
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                {
                    INNER168_tree = (Object) adaptor.create(INNER168);
                    adaptor.addChild(root_0, INNER168_tree);
                }

            }
                break;
            case 6:
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:805:11: LEFT
            {
                root_0 = (Object) adaptor.nil();

                LEFT169 = (Token) match(input, LEFT, FOLLOW_LEFT_in_keyWord5359);
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                {
                    LEFT169_tree = (Object) adaptor.create(LEFT169);
                    adaptor.addChild(root_0, LEFT169_tree);
                }

            }
                break;
            case 7:
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:806:11: OUTER
            {
                root_0 = (Object) adaptor.nil();

                OUTER170 = (Token) match(input, OUTER, FOLLOW_OUTER_in_keyWord5371);
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                {
                    OUTER170_tree = (Object) adaptor.create(OUTER170);
                    adaptor.addChild(root_0, OUTER170_tree);
                }

            }
                break;
            case 8:
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:807:11: ON
            {
                root_0 = (Object) adaptor.nil();

                ON171 = (Token) match(input, ON, FOLLOW_ON_in_keyWord5383);
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                {
                    ON171_tree = (Object) adaptor.create(ON171);
                    adaptor.addChild(root_0, ON171_tree);
                }

            }
                break;
            case 9:
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:808:11: WHERE
            {
                root_0 = (Object) adaptor.nil();

                WHERE172 = (Token) match(input, WHERE, FOLLOW_WHERE_in_keyWord5395);
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                {
                    WHERE172_tree = (Object) adaptor.create(WHERE172);
                    adaptor.addChild(root_0, WHERE172_tree);
                }

            }
                break;
            case 10:
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:809:11: OR
            {
                root_0 = (Object) adaptor.nil();

                OR173 = (Token) match(input, OR, FOLLOW_OR_in_keyWord5407);
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                {
                    OR173_tree = (Object) adaptor.create(OR173);
                    adaptor.addChild(root_0, OR173_tree);
                }

            }
                break;
            case 11:
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:810:11: AND
            {
                root_0 = (Object) adaptor.nil();

                AND174 = (Token) match(input, AND, FOLLOW_AND_in_keyWord5419);
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                {
                    AND174_tree = (Object) adaptor.create(AND174);
                    adaptor.addChild(root_0, AND174_tree);
                }

            }
                break;
            case 12:
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:811:11: NOT
            {
                root_0 = (Object) adaptor.nil();

                NOT175 = (Token) match(input, NOT, FOLLOW_NOT_in_keyWord5431);
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                {
                    NOT175_tree = (Object) adaptor.create(NOT175);
                    adaptor.addChild(root_0, NOT175_tree);
                }

            }
                break;
            case 13:
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:812:11: IN
            {
                root_0 = (Object) adaptor.nil();

                IN176 = (Token) match(input, IN, FOLLOW_IN_in_keyWord5443);
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                {
                    IN176_tree = (Object) adaptor.create(IN176);
                    adaptor.addChild(root_0, IN176_tree);
                }

            }
                break;
            case 14:
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:813:11: LIKE
            {
                root_0 = (Object) adaptor.nil();

                LIKE177 = (Token) match(input, LIKE, FOLLOW_LIKE_in_keyWord5455);
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                {
                    LIKE177_tree = (Object) adaptor.create(LIKE177);
                    adaptor.addChild(root_0, LIKE177_tree);
                }

            }
                break;
            case 15:
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:814:11: IS
            {
                root_0 = (Object) adaptor.nil();

                IS178 = (Token) match(input, IS, FOLLOW_IS_in_keyWord5467);
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                {
                    IS178_tree = (Object) adaptor.create(IS178);
                    adaptor.addChild(root_0, IS178_tree);
                }

            }
                break;
            case 16:
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:815:11: NULL
            {
                root_0 = (Object) adaptor.nil();

                NULL179 = (Token) match(input, NULL, FOLLOW_NULL_in_keyWord5479);
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                {
                    NULL179_tree = (Object) adaptor.create(NULL179);
                    adaptor.addChild(root_0, NULL179_tree);
                }

            }
                break;
            case 17:
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:816:11: ANY
            {
                root_0 = (Object) adaptor.nil();

                ANY180 = (Token) match(input, ANY, FOLLOW_ANY_in_keyWord5491);
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                {
                    ANY180_tree = (Object) adaptor.create(ANY180);
                    adaptor.addChild(root_0, ANY180_tree);
                }

            }
                break;
            case 18:
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:817:11: CONTAINS
            {
                root_0 = (Object) adaptor.nil();

                CONTAINS181 = (Token) match(input, CONTAINS, FOLLOW_CONTAINS_in_keyWord5503);
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                {
                    CONTAINS181_tree = (Object) adaptor.create(CONTAINS181);
                    adaptor.addChild(root_0, CONTAINS181_tree);
                }

            }
                break;
            case 19:
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:818:11: IN_FOLDER
            {
                root_0 = (Object) adaptor.nil();

                IN_FOLDER182 = (Token) match(input, IN_FOLDER, FOLLOW_IN_FOLDER_in_keyWord5515);
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                {
                    IN_FOLDER182_tree = (Object) adaptor.create(IN_FOLDER182);
                    adaptor.addChild(root_0, IN_FOLDER182_tree);
                }

            }
                break;
            case 20:
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:819:11: IN_TREE
            {
                root_0 = (Object) adaptor.nil();

                IN_TREE183 = (Token) match(input, IN_TREE, FOLLOW_IN_TREE_in_keyWord5527);
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                {
                    IN_TREE183_tree = (Object) adaptor.create(IN_TREE183);
                    adaptor.addChild(root_0, IN_TREE183_tree);
                }

            }
                break;
            case 21:
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:820:11: ORDER
            {
                root_0 = (Object) adaptor.nil();

                ORDER184 = (Token) match(input, ORDER, FOLLOW_ORDER_in_keyWord5539);
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                {
                    ORDER184_tree = (Object) adaptor.create(ORDER184);
                    adaptor.addChild(root_0, ORDER184_tree);
                }

            }
                break;
            case 22:
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:821:11: BY
            {
                root_0 = (Object) adaptor.nil();

                BY185 = (Token) match(input, BY, FOLLOW_BY_in_keyWord5551);
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                {
                    BY185_tree = (Object) adaptor.create(BY185);
                    adaptor.addChild(root_0, BY185_tree);
                }

            }
                break;
            case 23:
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:822:11: ASC
            {
                root_0 = (Object) adaptor.nil();

                ASC186 = (Token) match(input, ASC, FOLLOW_ASC_in_keyWord5563);
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                {
                    ASC186_tree = (Object) adaptor.create(ASC186);
                    adaptor.addChild(root_0, ASC186_tree);
                }

            }
                break;
            case 24:
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:823:11: DESC
            {
                root_0 = (Object) adaptor.nil();

                DESC187 = (Token) match(input, DESC, FOLLOW_DESC_in_keyWord5575);
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                {
                    DESC187_tree = (Object) adaptor.create(DESC187);
                    adaptor.addChild(root_0, DESC187_tree);
                }

            }
                break;
            case 25:
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:824:11: TIMESTAMP
            {
                root_0 = (Object) adaptor.nil();

                TIMESTAMP188 = (Token) match(input, TIMESTAMP, FOLLOW_TIMESTAMP_in_keyWord5587);
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                {
                    TIMESTAMP188_tree = (Object) adaptor.create(TIMESTAMP188);
                    adaptor.addChild(root_0, TIMESTAMP188_tree);
                }

            }
                break;
            case 26:
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:825:11: TRUE
            {
                root_0 = (Object) adaptor.nil();

                TRUE189 = (Token) match(input, TRUE, FOLLOW_TRUE_in_keyWord5599);
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                {
                    TRUE189_tree = (Object) adaptor.create(TRUE189);
                    adaptor.addChild(root_0, TRUE189_tree);
                }

            }
                break;
            case 27:
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:826:11: FALSE
            {
                root_0 = (Object) adaptor.nil();

                FALSE190 = (Token) match(input, FALSE, FOLLOW_FALSE_in_keyWord5611);
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                {
                    FALSE190_tree = (Object) adaptor.create(FALSE190);
                    adaptor.addChild(root_0, FALSE190_tree);
                }

            }
                break;
            case 28:
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:827:11: cmisFunction
            {
                root_0 = (Object) adaptor.nil();

                pushFollow(FOLLOW_cmisFunction_in_keyWord5623);
                cmisFunction191 = cmisFunction();
                state._fsp--;
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                    adaptor.addChild(root_0, cmisFunction191.getTree());

            }
                break;

            }
            retval.stop = input.LT(-1);

            if (state.backtracking == 0)
            {
                retval.tree = (Object) adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        catch (RecognitionException e)
        {
            throw new CmisInvalidArgumentException(getErrorString(e), e);
        }

        finally
        {
            // do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "keyWord"

    public static class cmisFunction_return extends ParserRuleReturnScope
    {
        Object tree;

        @Override
        public Object getTree()
        {
            return tree;
        }
    };

    // $ANTLR start "cmisFunction"
    // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:830:1: cmisFunction : SCORE -> SCORE ;
    public final CMISParser.cmisFunction_return cmisFunction() throws RecognitionException
    {
        CMISParser.cmisFunction_return retval = new CMISParser.cmisFunction_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token SCORE192 = null;

        Object SCORE192_tree = null;
        RewriteRuleTokenStream stream_SCORE = new RewriteRuleTokenStream(adaptor, "token SCORE");

        try
        {
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:831:9: ( SCORE -> SCORE )
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:832:9: SCORE
            {
                SCORE192 = (Token) match(input, SCORE, FOLLOW_SCORE_in_cmisFunction5656);
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                    stream_SCORE.add(SCORE192);

                // AST REWRITE
                // elements: SCORE
                // token labels:
                // rule labels: retval
                // token list labels:
                // rule list labels:
                // wildcard labels:
                if (state.backtracking == 0)
                {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval = new RewriteRuleSubtreeStream(adaptor, "rule retval", retval != null ? retval.getTree() : null);

                    root_0 = (Object) adaptor.nil();
                    // 833:17: -> SCORE
                    {
                        adaptor.addChild(root_0, stream_SCORE.nextNode());
                    }

                    retval.tree = root_0;
                }

            }

            retval.stop = input.LT(-1);

            if (state.backtracking == 0)
            {
                retval.tree = (Object) adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        catch (RecognitionException e)
        {
            throw new CmisInvalidArgumentException(getErrorString(e), e);
        }

        finally
        {
            // do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "cmisFunction"

    public static class keyWordOrId_return extends ParserRuleReturnScope
    {
        Object tree;

        @Override
        public Object getTree()
        {
            return tree;
        }
    };

    // $ANTLR start "keyWordOrId"
    // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:836:1: keyWordOrId : ( keyWord -> keyWord | ID -> ID );
    public final CMISParser.keyWordOrId_return keyWordOrId() throws RecognitionException
    {
        CMISParser.keyWordOrId_return retval = new CMISParser.keyWordOrId_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token ID194 = null;
        ParserRuleReturnScope keyWord193 = null;

        Object ID194_tree = null;
        RewriteRuleTokenStream stream_ID = new RewriteRuleTokenStream(adaptor, "token ID");
        RewriteRuleSubtreeStream stream_keyWord = new RewriteRuleSubtreeStream(adaptor, "rule keyWord");

        try
        {
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:837:9: ( keyWord -> keyWord | ID -> ID )
            int alt46 = 2;
            int LA46_0 = input.LA(1);
            if (((LA46_0 >= AND && LA46_0 <= ASC) || LA46_0 == BY || LA46_0 == CONTAINS || LA46_0 == DESC || LA46_0 == FALSE || LA46_0 == FROM || (LA46_0 >= IN && LA46_0 <= LEFT) || LA46_0 == LIKE || LA46_0 == NOT || LA46_0 == NULL || (LA46_0 >= ON && LA46_0 <= OUTER) || (LA46_0 >= SCORE && LA46_0 <= SELECT) || (LA46_0 >= TIMESTAMP && LA46_0 <= WHERE)))
            {
                alt46 = 1;
            }
            else if ((LA46_0 == ID))
            {
                alt46 = 2;
            }

            else
            {
                if (state.backtracking > 0)
                {
                    state.failed = true;
                    return retval;
                }
                NoViableAltException nvae = new NoViableAltException("", 46, 0, input);
                throw nvae;
            }

            switch (alt46)
            {
            case 1:
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:838:9: keyWord
            {
                pushFollow(FOLLOW_keyWord_in_keyWordOrId5709);
                keyWord193 = keyWord();
                state._fsp--;
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                    stream_keyWord.add(keyWord193.getTree());
                // AST REWRITE
                // elements: keyWord
                // token labels:
                // rule labels: retval
                // token list labels:
                // rule list labels:
                // wildcard labels:
                if (state.backtracking == 0)
                {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval = new RewriteRuleSubtreeStream(adaptor, "rule retval", retval != null ? retval.getTree() : null);

                    root_0 = (Object) adaptor.nil();
                    // 839:17: -> keyWord
                    {
                        adaptor.addChild(root_0, stream_keyWord.nextTree());
                    }

                    retval.tree = root_0;
                }

            }
                break;
            case 2:
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:840:11: ID
            {
                ID194 = (Token) match(input, ID, FOLLOW_ID_in_keyWordOrId5741);
                if (state.failed)
                    return retval;
                if (state.backtracking == 0)
                    stream_ID.add(ID194);

                // AST REWRITE
                // elements: ID
                // token labels:
                // rule labels: retval
                // token list labels:
                // rule list labels:
                // wildcard labels:
                if (state.backtracking == 0)
                {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval = new RewriteRuleSubtreeStream(adaptor, "rule retval", retval != null ? retval.getTree() : null);

                    root_0 = (Object) adaptor.nil();
                    // 841:17: -> ID
                    {
                        adaptor.addChild(root_0, stream_ID.nextNode());
                    }

                    retval.tree = root_0;
                }

            }
                break;

            }
            retval.stop = input.LT(-1);

            if (state.backtracking == 0)
            {
                retval.tree = (Object) adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        catch (RecognitionException e)
        {
            throw new CmisInvalidArgumentException(getErrorString(e), e);
        }

        finally
        {
            // do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "keyWordOrId"

    // $ANTLR start synpred1_CMIS
    public final void synpred1_CMIS_fragment() throws RecognitionException
    {
        // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:388:9: ( tableName )
        // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:388:10: tableName
        {
            pushFollow(FOLLOW_tableName_in_synpred1_CMIS1323);
            tableName();
            state._fsp--;
            if (state.failed)
                return;

        }

    }
    // $ANTLR end synpred1_CMIS

    // $ANTLR start synpred2_CMIS
    public final void synpred2_CMIS_fragment() throws RecognitionException
    {
        // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:484:9: ( LPAREN singleTable ( joinedTable )+ RPAREN )
        // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:484:10: LPAREN singleTable ( joinedTable )+ RPAREN
        {
            match(input, LPAREN, FOLLOW_LPAREN_in_synpred2_CMIS1917);
            if (state.failed)
                return;

            pushFollow(FOLLOW_singleTable_in_synpred2_CMIS1919);
            singleTable();
            state._fsp--;
            if (state.failed)
                return;

            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:484:29: ( joinedTable )+
            int cnt47 = 0;
            loop47: while (true)
            {
                int alt47 = 2;
                int LA47_0 = input.LA(1);
                if ((LA47_0 == INNER || (LA47_0 >= JOIN && LA47_0 <= LEFT)))
                {
                    alt47 = 1;
                }

                switch (alt47)
                {
                case 1:
                // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS.g:484:29: joinedTable
                {
                    pushFollow(FOLLOW_joinedTable_in_synpred2_CMIS1921);
                    joinedTable();
                    state._fsp--;
                    if (state.failed)
                        return;

                }
                    break;

                default:
                    if (cnt47 >= 1)
                        break loop47;
                    if (state.backtracking > 0)
                    {
                        state.failed = true;
                        return;
                    }
                    EarlyExitException eee = new EarlyExitException(47, input);
                    throw eee;
                }
                cnt47++;
            }

            match(input, RPAREN, FOLLOW_RPAREN_in_synpred2_CMIS1924);
            if (state.failed)
                return;

        }

    }
    // $ANTLR end synpred2_CMIS

    // Delegated rules

    public final boolean synpred2_CMIS()
    {
        state.backtracking++;
        int start = input.mark();
        try
        {
            synpred2_CMIS_fragment(); // can never throw exception
        }
        catch (RecognitionException re)
        {
            System.err.println("impossible: " + re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed = false;
        return success;
    }

    public final boolean synpred1_CMIS()
    {
        state.backtracking++;
        int start = input.mark();
        try
        {
            synpred1_CMIS_fragment(); // can never throw exception
        }
        catch (RecognitionException re)
        {
            System.err.println("impossible: " + re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed = false;
        return success;
    }

    protected DFA12 dfa12 = new DFA12(this);
    protected DFA26 dfa26 = new DFA26(this);
    protected DFA28 dfa28 = new DFA28(this);
    protected DFA34 dfa34 = new DFA34(this);
    protected DFA41 dfa41 = new DFA41(this);
    static final String DFA12_eotS = "\173\uffff";
    static final String DFA12_eofS = "\173\uffff";
    static final String DFA12_minS = "\1\5\1\61\1\uffff\2\13\1\5\5\13\1\110\1\33\1\0\1\33\1\0\35\33\5\0\2\13" +
            "\1\5\1\uffff\1\13\1\5\1\13\2\0\35\33\1\0\35\33\1\0\2\13\2\0";
    static final String DFA12_maxS = "\1\127\1\61\1\uffff\2\126\1\127\5\126\1\110\1\45\1\0\1\45\1\0\35\33\5" +
            "\0\2\126\1\127\1\uffff\1\126\1\127\1\126\2\0\35\33\1\0\35\33\1\0\2\126" +
            "\2\0";
    static final String DFA12_acceptS = "\2\uffff\1\2\62\uffff\1\1\105\uffff";
    static final String DFA12_specialS = "\1\15\14\uffff\1\6\1\uffff\1\7\35\uffff\1\5\1\4\1\3\1\10\1\11\7\uffff" +
            "\1\12\1\13\35\uffff\1\14\35\uffff\1\2\2\uffff\1\1\1\0}>";
    static final String[] DFA12_transitionS = {
            "\4\2\1\uffff\1\2\6\uffff\1\2\3\uffff\1\2\11\uffff\1\2\1\uffff\1\2\3\uffff" +
                    "\10\2\2\uffff\1\2\5\uffff\1\2\1\uffff\1\2\1\uffff\4\2\15\uffff\1\1\1" +
                    "\2\11\uffff\3\2",
            "\1\3",
            "",
            "\1\14\7\uffff\1\7\7\uffff\1\5\3\uffff\1\12\1\6\4\uffff\1\4\42\uffff" +
                    "\1\10\1\15\13\uffff\1\13\1\11",
            "\1\14\7\uffff\1\7\4\uffff\1\16\2\uffff\1\5\3\uffff\1\12\1\6\4\uffff" +
                    "\1\4\42\uffff\1\10\1\17\13\uffff\1\13\1\11",
            "\1\32\1\40\1\21\1\46\1\uffff\1\45\6\uffff\1\41\3\uffff\1\47\11\uffff" +
                    "\1\52\1\uffff\1\22\3\uffff\1\54\1\34\1\24\1\42\1\43\1\36\1\23\1\25\2" +
                    "\uffff\1\35\5\uffff\1\33\1\uffff\1\37\1\uffff\1\27\1\31\1\44\1\26\15" +
                    "\uffff\1\53\1\20\11\uffff\1\50\1\51\1\30",
            "\1\14\7\uffff\1\7\7\uffff\1\5\3\uffff\1\12\1\6\4\uffff\1\4\42\uffff" +
                    "\1\10\1\55\13\uffff\1\13\1\11",
            "\1\14\7\uffff\1\7\7\uffff\1\5\3\uffff\1\12\1\6\4\uffff\1\4\42\uffff" +
                    "\1\10\1\56\13\uffff\1\13\1\11",
            "\1\14\7\uffff\1\7\7\uffff\1\5\3\uffff\1\12\1\6\4\uffff\1\4\42\uffff" +
                    "\1\10\1\57\13\uffff\1\13\1\11",
            "\1\14\7\uffff\1\7\7\uffff\1\5\3\uffff\1\12\1\6\4\uffff\1\4\42\uffff" +
                    "\1\10\1\60\13\uffff\1\13\1\11",
            "\1\14\7\uffff\1\7\7\uffff\1\5\3\uffff\1\12\1\6\4\uffff\1\4\42\uffff" +
                    "\1\10\1\61\13\uffff\1\13\1\11",
            "\1\62",
            "\1\64\11\uffff\1\63",
            "\1\uffff",
            "\1\67\11\uffff\1\66",
            "\1\uffff",
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
            "\1\14\7\uffff\1\7\7\uffff\1\5\3\uffff\1\12\1\6\4\uffff\1\4\42\uffff" +
                    "\1\10\1\71\13\uffff\1\13\1\11",
            "\1\14\7\uffff\1\7\7\uffff\1\5\3\uffff\1\12\1\6\4\uffff\1\4\42\uffff" +
                    "\1\10\1\72\13\uffff\1\13\1\11",
            "\1\105\1\113\1\74\1\121\1\uffff\1\120\6\uffff\1\114\3\uffff\1\122\11" +
                    "\uffff\1\125\1\uffff\1\75\3\uffff\1\127\1\107\1\77\1\115\1\116\1\111" +
                    "\1\76\1\100\2\uffff\1\110\5\uffff\1\106\1\uffff\1\112\1\uffff\1\102\1" +
                    "\104\1\117\1\101\15\uffff\1\126\1\73\11\uffff\1\123\1\124\1\103",
            "",
            "\1\14\7\uffff\1\7\7\uffff\1\5\3\uffff\1\12\1\6\4\uffff\1\4\42\uffff" +
                    "\1\10\1\130\13\uffff\1\13\1\11",
            "\1\143\1\151\1\132\1\157\1\uffff\1\156\6\uffff\1\152\3\uffff\1\160\11" +
                    "\uffff\1\163\1\uffff\1\133\3\uffff\1\165\1\145\1\135\1\153\1\154\1\147" +
                    "\1\134\1\136\2\uffff\1\146\5\uffff\1\144\1\uffff\1\150\1\uffff\1\140" +
                    "\1\142\1\155\1\137\15\uffff\1\164\1\131\11\uffff\1\161\1\162\1\141",
            "\1\14\7\uffff\1\7\4\uffff\1\16\2\uffff\1\5\3\uffff\1\12\1\6\4\uffff" +
                    "\1\4\42\uffff\1\10\1\166\13\uffff\1\13\1\11",
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
            "\1\14\7\uffff\1\7\7\uffff\1\5\3\uffff\1\12\1\6\4\uffff\1\4\42\uffff" +
                    "\1\10\1\171\13\uffff\1\13\1\11",
            "\1\14\7\uffff\1\7\7\uffff\1\5\3\uffff\1\12\1\6\4\uffff\1\4\42\uffff" +
                    "\1\10\1\172\13\uffff\1\13\1\11",
            "\1\uffff",
            "\1\uffff"
    };

    static final short[] DFA12_eot = DFA.unpackEncodedString(DFA12_eotS);
    static final short[] DFA12_eof = DFA.unpackEncodedString(DFA12_eofS);
    static final char[] DFA12_min = DFA.unpackEncodedStringToUnsignedChars(DFA12_minS);
    static final char[] DFA12_max = DFA.unpackEncodedStringToUnsignedChars(DFA12_maxS);
    static final short[] DFA12_accept = DFA.unpackEncodedString(DFA12_acceptS);
    static final short[] DFA12_special = DFA.unpackEncodedString(DFA12_specialS);
    static final short[][] DFA12_transition;

    static
    {
        int numStates = DFA12_transitionS.length;
        DFA12_transition = new short[numStates][];
        for (int i = 0; i < numStates; i++)
        {
            DFA12_transition[i] = DFA.unpackEncodedString(DFA12_transitionS[i]);
        }
    }

    protected class DFA12 extends DFA
    {

        public DFA12(BaseRecognizer recognizer)
        {
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

        @Override
        public String getDescription()
        {
            return "359:1: valueFunction : (cmisFunctionName= cmisFunction LPAREN ( functionArgument )* RPAREN -> ^( FUNCTION $cmisFunctionName LPAREN ( functionArgument )* RPAREN ) |{...}? =>functionName= keyWordOrId LPAREN ( functionArgument )* RPAREN -> ^( FUNCTION $functionName LPAREN ( functionArgument )* RPAREN ) );";
        }

        @Override
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException
        {
            TokenStream input = (TokenStream) _input;
            int _s = s;
            switch (s)
            {
            case 0:
                int LA12_122 = input.LA(1);

                int index12_122 = input.index();
                input.rewind();
                s = -1;
                if ((!(((strict == false)))))
                {
                    s = 53;
                }
                else if (((strict == false)))
                {
                    s = 2;
                }

                input.seek(index12_122);
                if (s >= 0)
                    return s;
                break;

            case 1:
                int LA12_121 = input.LA(1);

                int index12_121 = input.index();
                input.rewind();
                s = -1;
                if ((!(((strict == false)))))
                {
                    s = 53;
                }
                else if (((strict == false)))
                {
                    s = 2;
                }

                input.seek(index12_121);
                if (s >= 0)
                    return s;
                break;

            case 2:
                int LA12_118 = input.LA(1);

                int index12_118 = input.index();
                input.rewind();
                s = -1;
                if ((!(((strict == false)))))
                {
                    s = 53;
                }
                else if (((strict == false)))
                {
                    s = 2;
                }

                input.seek(index12_118);
                if (s >= 0)
                    return s;
                break;

            case 3:
                int LA12_47 = input.LA(1);

                int index12_47 = input.index();
                input.rewind();
                s = -1;
                if ((!(((strict == false)))))
                {
                    s = 53;
                }
                else if (((strict == false)))
                {
                    s = 2;
                }

                input.seek(index12_47);
                if (s >= 0)
                    return s;
                break;

            case 4:
                int LA12_46 = input.LA(1);

                int index12_46 = input.index();
                input.rewind();
                s = -1;
                if ((!(((strict == false)))))
                {
                    s = 53;
                }
                else if (((strict == false)))
                {
                    s = 2;
                }

                input.seek(index12_46);
                if (s >= 0)
                    return s;
                break;

            case 5:
                int LA12_45 = input.LA(1);

                int index12_45 = input.index();
                input.rewind();
                s = -1;
                if ((!(((strict == false)))))
                {
                    s = 53;
                }
                else if (((strict == false)))
                {
                    s = 2;
                }

                input.seek(index12_45);
                if (s >= 0)
                    return s;
                break;

            case 6:
                int LA12_13 = input.LA(1);

                int index12_13 = input.index();
                input.rewind();
                s = -1;
                if ((!(((strict == false)))))
                {
                    s = 53;
                }
                else if (((strict == false)))
                {
                    s = 2;
                }

                input.seek(index12_13);
                if (s >= 0)
                    return s;
                break;

            case 7:
                int LA12_15 = input.LA(1);

                int index12_15 = input.index();
                input.rewind();
                s = -1;
                if ((!(((strict == false)))))
                {
                    s = 53;
                }
                else if (((strict == false)))
                {
                    s = 2;
                }

                input.seek(index12_15);
                if (s >= 0)
                    return s;
                break;

            case 8:
                int LA12_48 = input.LA(1);

                int index12_48 = input.index();
                input.rewind();
                s = -1;
                if ((!(((strict == false)))))
                {
                    s = 53;
                }
                else if (((strict == false)))
                {
                    s = 2;
                }

                input.seek(index12_48);
                if (s >= 0)
                    return s;
                break;

            case 9:
                int LA12_49 = input.LA(1);

                int index12_49 = input.index();
                input.rewind();
                s = -1;
                if ((!(((strict == false)))))
                {
                    s = 53;
                }
                else if (((strict == false)))
                {
                    s = 2;
                }

                input.seek(index12_49);
                if (s >= 0)
                    return s;
                break;

            case 10:
                int LA12_57 = input.LA(1);

                int index12_57 = input.index();
                input.rewind();
                s = -1;
                if ((!(((strict == false)))))
                {
                    s = 53;
                }
                else if (((strict == false)))
                {
                    s = 2;
                }

                input.seek(index12_57);
                if (s >= 0)
                    return s;
                break;

            case 11:
                int LA12_58 = input.LA(1);

                int index12_58 = input.index();
                input.rewind();
                s = -1;
                if ((!(((strict == false)))))
                {
                    s = 53;
                }
                else if (((strict == false)))
                {
                    s = 2;
                }

                input.seek(index12_58);
                if (s >= 0)
                    return s;
                break;

            case 12:
                int LA12_88 = input.LA(1);

                int index12_88 = input.index();
                input.rewind();
                s = -1;
                if ((!(((strict == false)))))
                {
                    s = 53;
                }
                else if (((strict == false)))
                {
                    s = 2;
                }

                input.seek(index12_88);
                if (s >= 0)
                    return s;
                break;

            case 13:
                int LA12_0 = input.LA(1);

                int index12_0 = input.index();
                input.rewind();
                s = -1;
                if ((LA12_0 == SCORE))
                {
                    s = 1;
                }
                else if (((LA12_0 >= AND && LA12_0 <= ASC) || LA12_0 == BY || LA12_0 == CONTAINS || LA12_0 == DESC || LA12_0 == FALSE || LA12_0 == FROM || (LA12_0 >= ID && LA12_0 <= LEFT) || LA12_0 == LIKE || LA12_0 == NOT || LA12_0 == NULL || (LA12_0 >= ON && LA12_0 <= OUTER) || LA12_0 == SELECT || (LA12_0 >= TIMESTAMP && LA12_0 <= WHERE)) && ((strict == false)))
                {
                    s = 2;
                }

                input.seek(index12_0);
                if (s >= 0)
                    return s;
                break;
            }
            if (state.backtracking > 0)
            {
                state.failed = true;
                return -1;
            }
            NoViableAltException nvae = new NoViableAltException(getDescription(), 12, _s, input);
            error(nvae);
            throw nvae;
        }
    }

    static final String DFA26_eotS = "\167\uffff";
    static final String DFA26_eofS = "\167\uffff";
    static final String DFA26_minS = "\2\5\12\uffff\1\5\1\13\1\5\1\61\5\13\1\33\1\uffff\36\33\2\13\1\5\1\13" +
            "\1\5\1\13\72\33\2\13";
    static final String DFA26_maxS = "\2\127\12\uffff\1\127\1\126\1\127\1\110\5\126\1\45\1\uffff\1\45\35\33" +
            "\2\126\1\127\1\126\1\127\1\126\72\33\2\126";
    static final String DFA26_acceptS = "\2\uffff\11\2\1\1\12\uffff\1\2\140\uffff";
    static final String DFA26_specialS = "\1\4\13\uffff\1\7\1\15\2\uffff\1\12\1\13\1\3\1\5\1\1\40\uffff\1\0\1\10" +
            "\1\uffff\1\2\1\uffff\1\14\72\uffff\1\11\1\6}>";
    static final String[] DFA26_transitionS = {
            "\1\5\1\6\1\5\1\7\1\uffff\1\7\1\11\5\uffff\1\6\1\uffff\1\10\1\uffff\1" +
                    "\7\5\uffff\1\3\3\uffff\2\10\1\5\3\uffff\1\2\2\5\2\6\3\5\2\uffff\1\5\1" +
                    "\uffff\1\12\3\uffff\1\1\1\uffff\1\5\1\uffff\2\5\1\7\1\5\13\uffff\1\10" +
                    "\1\uffff\1\4\1\5\11\uffff\2\10\1\5",
            "\4\13\1\uffff\2\13\5\uffff\1\13\1\uffff\1\13\1\uffff\1\13\5\uffff\1" +
                    "\13\3\uffff\3\13\3\uffff\10\13\2\uffff\1\13\1\uffff\1\14\3\uffff\1\13" +
                    "\1\uffff\1\13\1\uffff\4\13\13\uffff\1\13\1\uffff\2\13\11\uffff\3\13",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\4\13\1\uffff\1\13\1\25\5\uffff\1\13\1\uffff\1\23\1\uffff\1\13\5\uffff" +
                    "\1\16\3\uffff\1\21\1\22\1\13\3\uffff\1\15\7\13\2\uffff\1\13\1\uffff\1" +
                    "\13\3\uffff\1\13\1\uffff\1\13\1\uffff\4\13\13\uffff\1\24\1\26\2\13\11" +
                    "\uffff\1\17\1\20\1\13",
            "\1\26\7\uffff\1\26\4\uffff\1\27\2\uffff\1\26\1\uffff\1\13\1\uffff\2" +
                    "\26\2\uffff\2\13\1\26\1\13\3\uffff\1\13\2\uffff\3\13\1\uffff\1\13\3\uffff" +
                    "\2\13\21\uffff\2\26\13\uffff\2\26",
            "\1\42\1\50\1\31\1\56\1\uffff\1\55\6\uffff\1\51\3\uffff\1\57\11\uffff" +
                    "\1\62\1\uffff\1\32\3\uffff\1\64\1\44\1\34\1\52\1\53\1\46\1\33\1\35\2" +
                    "\uffff\1\45\5\uffff\1\43\1\uffff\1\47\1\uffff\1\37\1\41\1\54\1\36\15" +
                    "\uffff\1\63\1\30\11\uffff\1\60\1\61\1\40",
            "\1\13\26\uffff\1\65",
            "\1\26\7\uffff\1\26\7\uffff\1\26\1\uffff\1\13\1\uffff\2\26\2\uffff\2" +
                    "\13\1\26\7\uffff\2\13\2\uffff\1\13\4\uffff\1\13\21\uffff\2\26\13\uffff" +
                    "\2\26",
            "\1\26\7\uffff\1\26\7\uffff\1\26\1\uffff\1\13\1\uffff\2\26\2\uffff\2" +
                    "\13\1\26\7\uffff\2\13\2\uffff\1\13\4\uffff\1\13\21\uffff\2\26\13\uffff" +
                    "\2\26",
            "\1\26\7\uffff\1\26\7\uffff\1\26\1\uffff\1\13\1\uffff\2\26\2\uffff\2" +
                    "\13\1\26\7\uffff\2\13\7\uffff\1\13\21\uffff\2\26\13\uffff\2\26",
            "\1\26\7\uffff\1\26\7\uffff\1\26\1\uffff\1\13\1\uffff\2\26\2\uffff\2" +
                    "\13\1\26\7\uffff\2\13\7\uffff\1\13\21\uffff\2\26\13\uffff\2\26",
            "\1\26\7\uffff\1\26\7\uffff\1\26\1\uffff\1\13\1\uffff\2\26\2\uffff\2" +
                    "\13\1\26\7\uffff\2\13\7\uffff\1\13\21\uffff\2\26\13\uffff\2\26",
            "\1\67\11\uffff\1\66",
            "",
            "\1\71\11\uffff\1\70",
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
            "\1\26\7\uffff\1\26\7\uffff\1\26\1\uffff\1\13\1\uffff\2\26\2\uffff\2" +
                    "\13\1\26\7\uffff\2\13\7\uffff\1\13\21\uffff\2\26\13\uffff\2\26",
            "\1\26\7\uffff\1\26\7\uffff\1\26\1\uffff\1\13\1\uffff\2\26\2\uffff\2" +
                    "\13\1\26\7\uffff\2\13\7\uffff\1\13\21\uffff\2\26\13\uffff\2\26",
            "\1\105\1\113\1\74\1\121\1\uffff\1\120\6\uffff\1\114\3\uffff\1\122\11" +
                    "\uffff\1\125\1\uffff\1\75\3\uffff\1\127\1\107\1\77\1\115\1\116\1\111" +
                    "\1\76\1\100\2\uffff\1\110\5\uffff\1\106\1\uffff\1\112\1\uffff\1\102\1" +
                    "\104\1\117\1\101\15\uffff\1\126\1\73\11\uffff\1\123\1\124\1\103",
            "\1\26\7\uffff\1\26\7\uffff\1\26\1\uffff\1\13\1\uffff\2\26\2\uffff\2" +
                    "\13\1\26\1\13\3\uffff\1\13\2\uffff\3\13\5\uffff\2\13\21\uffff\2\26\13" +
                    "\uffff\2\26",
            "\1\142\1\150\1\131\1\156\1\uffff\1\155\6\uffff\1\151\3\uffff\1\157\11" +
                    "\uffff\1\162\1\uffff\1\132\3\uffff\1\164\1\144\1\134\1\152\1\153\1\146" +
                    "\1\133\1\135\2\uffff\1\145\5\uffff\1\143\1\uffff\1\147\1\uffff\1\137" +
                    "\1\141\1\154\1\136\15\uffff\1\163\1\130\11\uffff\1\160\1\161\1\140",
            "\1\26\7\uffff\1\26\4\uffff\1\27\2\uffff\1\26\1\uffff\1\13\1\uffff\2" +
                    "\26\2\uffff\2\13\1\26\1\13\3\uffff\1\13\2\uffff\3\13\5\uffff\2\13\21" +
                    "\uffff\2\26\13\uffff\2\26",
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
            "\1\26\7\uffff\1\26\7\uffff\1\26\1\uffff\1\13\1\uffff\2\26\2\uffff\2" +
                    "\13\1\26\7\uffff\2\13\7\uffff\1\13\21\uffff\2\26\13\uffff\2\26",
            "\1\26\7\uffff\1\26\7\uffff\1\26\1\uffff\1\13\1\uffff\2\26\2\uffff\2" +
                    "\13\1\26\1\13\3\uffff\1\13\2\uffff\3\13\5\uffff\2\13\21\uffff\2\26\13" +
                    "\uffff\2\26"
    };

    static final short[] DFA26_eot = DFA.unpackEncodedString(DFA26_eotS);
    static final short[] DFA26_eof = DFA.unpackEncodedString(DFA26_eofS);
    static final char[] DFA26_min = DFA.unpackEncodedStringToUnsignedChars(DFA26_minS);
    static final char[] DFA26_max = DFA.unpackEncodedStringToUnsignedChars(DFA26_maxS);
    static final short[] DFA26_accept = DFA.unpackEncodedString(DFA26_acceptS);
    static final short[] DFA26_special = DFA.unpackEncodedString(DFA26_specialS);
    static final short[][] DFA26_transition;

    static
    {
        int numStates = DFA26_transitionS.length;
        DFA26_transition = new short[numStates][];
        for (int i = 0; i < numStates; i++)
        {
            DFA26_transition[i] = DFA.unpackEncodedString(DFA26_transitionS[i]);
        }
    }

    protected class DFA26 extends DFA
    {

        public DFA26(BaseRecognizer recognizer)
        {
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

        @Override
        public String getDescription()
        {
            return "552:1: searchNotCondition : ( NOT searchTest -> ^( NEGATION searchTest ) | searchTest -> searchTest );";
        }

        @Override
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException
        {
            TokenStream input = (TokenStream) _input;
            int _s = s;
            switch (s)
            {
            case 0:
                int LA26_53 = input.LA(1);

                int index26_53 = input.index();
                input.rewind();
                s = -1;
                if ((LA26_53 == EQUALS || (LA26_53 >= GREATERTHAN && LA26_53 <= GREATERTHANOREQUALS) || (LA26_53 >= LESSTHAN && LA26_53 <= LESSTHANOREQUALS) || LA26_53 == NOTEQUALS))
                {
                    s = 11;
                }
                else if ((LA26_53 == COLON || LA26_53 == DECIMAL_INTEGER_LITERAL || LA26_53 == DOUBLE_QUOTE || (LA26_53 >= FALSE && LA26_53 <= FLOATING_POINT_LITERAL) || LA26_53 == ID || (LA26_53 >= QUOTED_STRING && LA26_53 <= RPAREN) || (LA26_53 >= TIMESTAMP && LA26_53 <= TRUE)) && ((strict == false)))
                {
                    s = 22;
                }

                input.seek(index26_53);
                if (s >= 0)
                    return s;
                break;

            case 1:
                int LA26_20 = input.LA(1);

                int index26_20 = input.index();
                input.rewind();
                s = -1;
                if ((LA26_20 == EQUALS || (LA26_20 >= GREATERTHAN && LA26_20 <= GREATERTHANOREQUALS) || (LA26_20 >= LESSTHAN && LA26_20 <= LESSTHANOREQUALS) || LA26_20 == NOTEQUALS))
                {
                    s = 11;
                }
                else if ((LA26_20 == COLON || LA26_20 == DECIMAL_INTEGER_LITERAL || LA26_20 == DOUBLE_QUOTE || (LA26_20 >= FALSE && LA26_20 <= FLOATING_POINT_LITERAL) || LA26_20 == ID || (LA26_20 >= QUOTED_STRING && LA26_20 <= RPAREN) || (LA26_20 >= TIMESTAMP && LA26_20 <= TRUE)) && ((strict == false)))
                {
                    s = 22;
                }

                input.seek(index26_20);
                if (s >= 0)
                    return s;
                break;

            case 2:
                int LA26_56 = input.LA(1);

                int index26_56 = input.index();
                input.rewind();
                s = -1;
                if ((LA26_56 == EQUALS || (LA26_56 >= GREATERTHAN && LA26_56 <= GREATERTHANOREQUALS) || LA26_56 == IN || LA26_56 == IS || (LA26_56 >= LESSTHAN && LA26_56 <= LIKE) || (LA26_56 >= NOT && LA26_56 <= NOTEQUALS)))
                {
                    s = 11;
                }
                else if ((LA26_56 == COLON || LA26_56 == DECIMAL_INTEGER_LITERAL || LA26_56 == DOUBLE_QUOTE || (LA26_56 >= FALSE && LA26_56 <= FLOATING_POINT_LITERAL) || LA26_56 == ID || (LA26_56 >= QUOTED_STRING && LA26_56 <= RPAREN) || (LA26_56 >= TIMESTAMP && LA26_56 <= TRUE)) && ((strict == false)))
                {
                    s = 22;
                }

                input.seek(index26_56);
                if (s >= 0)
                    return s;
                break;

            case 3:
                int LA26_18 = input.LA(1);

                int index26_18 = input.index();
                input.rewind();
                s = -1;
                if ((LA26_18 == EQUALS || (LA26_18 >= GREATERTHAN && LA26_18 <= GREATERTHANOREQUALS) || (LA26_18 >= LESSTHAN && LA26_18 <= LESSTHANOREQUALS) || LA26_18 == NOTEQUALS))
                {
                    s = 11;
                }
                else if ((LA26_18 == COLON || LA26_18 == DECIMAL_INTEGER_LITERAL || LA26_18 == DOUBLE_QUOTE || (LA26_18 >= FALSE && LA26_18 <= FLOATING_POINT_LITERAL) || LA26_18 == ID || (LA26_18 >= QUOTED_STRING && LA26_18 <= RPAREN) || (LA26_18 >= TIMESTAMP && LA26_18 <= TRUE)) && ((strict == false)))
                {
                    s = 22;
                }

                input.seek(index26_18);
                if (s >= 0)
                    return s;
                break;

            case 4:
                int LA26_0 = input.LA(1);

                int index26_0 = input.index();
                input.rewind();
                s = -1;
                if ((LA26_0 == NOT))
                {
                    s = 1;
                }
                else if ((LA26_0 == ID))
                {
                    s = 2;
                }
                else if ((LA26_0 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 3;
                }
                else if ((LA26_0 == SCORE))
                {
                    s = 4;
                }
                else if ((LA26_0 == AND || LA26_0 == AS || LA26_0 == FROM || (LA26_0 >= IN && LA26_0 <= INNER) || (LA26_0 >= IS && LA26_0 <= LEFT) || LA26_0 == LIKE || LA26_0 == NULL || (LA26_0 >= ON && LA26_0 <= OR) || LA26_0 == OUTER || LA26_0 == SELECT || LA26_0 == WHERE) && ((strict == false)))
                {
                    s = 5;
                }
                else if ((LA26_0 == ANY || LA26_0 == CONTAINS || (LA26_0 >= IN_FOLDER && LA26_0 <= IN_TREE)))
                {
                    s = 6;
                }
                else if ((LA26_0 == ASC || LA26_0 == BY || LA26_0 == DESC || LA26_0 == ORDER) && ((strict == false)))
                {
                    s = 7;
                }
                else if ((LA26_0 == DECIMAL_INTEGER_LITERAL || (LA26_0 >= FALSE && LA26_0 <= FLOATING_POINT_LITERAL) || LA26_0 == QUOTED_STRING || (LA26_0 >= TIMESTAMP && LA26_0 <= TRUE)))
                {
                    s = 8;
                }
                else if ((LA26_0 == COLON) && ((strict == false)))
                {
                    s = 9;
                }
                else if ((LA26_0 == LPAREN))
                {
                    s = 10;
                }

                input.seek(index26_0);
                if (s >= 0)
                    return s;
                break;

            case 5:
                int LA26_19 = input.LA(1);

                int index26_19 = input.index();
                input.rewind();
                s = -1;
                if ((LA26_19 == EQUALS || (LA26_19 >= GREATERTHAN && LA26_19 <= GREATERTHANOREQUALS) || (LA26_19 >= LESSTHAN && LA26_19 <= LESSTHANOREQUALS) || LA26_19 == NOTEQUALS))
                {
                    s = 11;
                }
                else if ((LA26_19 == COLON || LA26_19 == DECIMAL_INTEGER_LITERAL || LA26_19 == DOUBLE_QUOTE || (LA26_19 >= FALSE && LA26_19 <= FLOATING_POINT_LITERAL) || LA26_19 == ID || (LA26_19 >= QUOTED_STRING && LA26_19 <= RPAREN) || (LA26_19 >= TIMESTAMP && LA26_19 <= TRUE)) && ((strict == false)))
                {
                    s = 22;
                }

                input.seek(index26_19);
                if (s >= 0)
                    return s;
                break;

            case 6:
                int LA26_118 = input.LA(1);

                int index26_118 = input.index();
                input.rewind();
                s = -1;
                if ((LA26_118 == EQUALS || (LA26_118 >= GREATERTHAN && LA26_118 <= GREATERTHANOREQUALS) || LA26_118 == IN || LA26_118 == IS || (LA26_118 >= LESSTHAN && LA26_118 <= LIKE) || (LA26_118 >= NOT && LA26_118 <= NOTEQUALS)))
                {
                    s = 11;
                }
                else if ((LA26_118 == COLON || LA26_118 == DECIMAL_INTEGER_LITERAL || LA26_118 == DOUBLE_QUOTE || (LA26_118 >= FALSE && LA26_118 <= FLOATING_POINT_LITERAL) || LA26_118 == ID || (LA26_118 >= QUOTED_STRING && LA26_118 <= RPAREN) || (LA26_118 >= TIMESTAMP && LA26_118 <= TRUE)) && ((strict == false)))
                {
                    s = 22;
                }

                input.seek(index26_118);
                if (s >= 0)
                    return s;
                break;

            case 7:
                int LA26_12 = input.LA(1);

                int index26_12 = input.index();
                input.rewind();
                s = -1;
                if (((LA26_12 >= AND && LA26_12 <= ASC) || LA26_12 == BY || LA26_12 == CONTAINS || LA26_12 == DESC || LA26_12 == FROM || (LA26_12 >= IN && LA26_12 <= LEFT) || LA26_12 == LIKE || LA26_12 == LPAREN || LA26_12 == NOT || LA26_12 == NULL || (LA26_12 >= ON && LA26_12 <= OUTER) || (LA26_12 >= SCORE && LA26_12 <= SELECT) || LA26_12 == WHERE))
                {
                    s = 11;
                }
                else if ((LA26_12 == ID))
                {
                    s = 13;
                }
                else if ((LA26_12 == DOUBLE_QUOTE))
                {
                    s = 14;
                }
                else if ((LA26_12 == TIMESTAMP))
                {
                    s = 15;
                }
                else if ((LA26_12 == TRUE))
                {
                    s = 16;
                }
                else if ((LA26_12 == FALSE))
                {
                    s = 17;
                }
                else if ((LA26_12 == FLOATING_POINT_LITERAL))
                {
                    s = 18;
                }
                else if ((LA26_12 == DECIMAL_INTEGER_LITERAL))
                {
                    s = 19;
                }
                else if ((LA26_12 == QUOTED_STRING))
                {
                    s = 20;
                }
                else if ((LA26_12 == COLON))
                {
                    s = 21;
                }
                else if ((LA26_12 == RPAREN) && ((strict == false)))
                {
                    s = 22;
                }

                input.seek(index26_12);
                if (s >= 0)
                    return s;
                break;

            case 8:
                int LA26_54 = input.LA(1);

                int index26_54 = input.index();
                input.rewind();
                s = -1;
                if ((LA26_54 == EQUALS || (LA26_54 >= GREATERTHAN && LA26_54 <= GREATERTHANOREQUALS) || (LA26_54 >= LESSTHAN && LA26_54 <= LESSTHANOREQUALS) || LA26_54 == NOTEQUALS))
                {
                    s = 11;
                }
                else if ((LA26_54 == COLON || LA26_54 == DECIMAL_INTEGER_LITERAL || LA26_54 == DOUBLE_QUOTE || (LA26_54 >= FALSE && LA26_54 <= FLOATING_POINT_LITERAL) || LA26_54 == ID || (LA26_54 >= QUOTED_STRING && LA26_54 <= RPAREN) || (LA26_54 >= TIMESTAMP && LA26_54 <= TRUE)) && ((strict == false)))
                {
                    s = 22;
                }

                input.seek(index26_54);
                if (s >= 0)
                    return s;
                break;

            case 9:
                int LA26_117 = input.LA(1);

                int index26_117 = input.index();
                input.rewind();
                s = -1;
                if ((LA26_117 == EQUALS || (LA26_117 >= GREATERTHAN && LA26_117 <= GREATERTHANOREQUALS) || (LA26_117 >= LESSTHAN && LA26_117 <= LESSTHANOREQUALS) || LA26_117 == NOTEQUALS))
                {
                    s = 11;
                }
                else if ((LA26_117 == COLON || LA26_117 == DECIMAL_INTEGER_LITERAL || LA26_117 == DOUBLE_QUOTE || (LA26_117 >= FALSE && LA26_117 <= FLOATING_POINT_LITERAL) || LA26_117 == ID || (LA26_117 >= QUOTED_STRING && LA26_117 <= RPAREN) || (LA26_117 >= TIMESTAMP && LA26_117 <= TRUE)) && ((strict == false)))
                {
                    s = 22;
                }

                input.seek(index26_117);
                if (s >= 0)
                    return s;
                break;

            case 10:
                int LA26_16 = input.LA(1);

                int index26_16 = input.index();
                input.rewind();
                s = -1;
                if ((LA26_16 == EQUALS || (LA26_16 >= GREATERTHAN && LA26_16 <= GREATERTHANOREQUALS) || (LA26_16 >= LESSTHAN && LA26_16 <= LESSTHANOREQUALS) || LA26_16 == LPAREN || LA26_16 == NOTEQUALS))
                {
                    s = 11;
                }
                else if ((LA26_16 == COLON || LA26_16 == DECIMAL_INTEGER_LITERAL || LA26_16 == DOUBLE_QUOTE || (LA26_16 >= FALSE && LA26_16 <= FLOATING_POINT_LITERAL) || LA26_16 == ID || (LA26_16 >= QUOTED_STRING && LA26_16 <= RPAREN) || (LA26_16 >= TIMESTAMP && LA26_16 <= TRUE)) && ((strict == false)))
                {
                    s = 22;
                }

                input.seek(index26_16);
                if (s >= 0)
                    return s;
                break;

            case 11:
                int LA26_17 = input.LA(1);

                int index26_17 = input.index();
                input.rewind();
                s = -1;
                if ((LA26_17 == EQUALS || (LA26_17 >= GREATERTHAN && LA26_17 <= GREATERTHANOREQUALS) || (LA26_17 >= LESSTHAN && LA26_17 <= LESSTHANOREQUALS) || LA26_17 == LPAREN || LA26_17 == NOTEQUALS))
                {
                    s = 11;
                }
                else if ((LA26_17 == COLON || LA26_17 == DECIMAL_INTEGER_LITERAL || LA26_17 == DOUBLE_QUOTE || (LA26_17 >= FALSE && LA26_17 <= FLOATING_POINT_LITERAL) || LA26_17 == ID || (LA26_17 >= QUOTED_STRING && LA26_17 <= RPAREN) || (LA26_17 >= TIMESTAMP && LA26_17 <= TRUE)) && ((strict == false)))
                {
                    s = 22;
                }

                input.seek(index26_17);
                if (s >= 0)
                    return s;
                break;

            case 12:
                int LA26_58 = input.LA(1);

                int index26_58 = input.index();
                input.rewind();
                s = -1;
                if ((LA26_58 == DOT))
                {
                    s = 23;
                }
                else if ((LA26_58 == EQUALS || (LA26_58 >= GREATERTHAN && LA26_58 <= GREATERTHANOREQUALS) || LA26_58 == IN || LA26_58 == IS || (LA26_58 >= LESSTHAN && LA26_58 <= LIKE) || (LA26_58 >= NOT && LA26_58 <= NOTEQUALS)))
                {
                    s = 11;
                }
                else if ((LA26_58 == COLON || LA26_58 == DECIMAL_INTEGER_LITERAL || LA26_58 == DOUBLE_QUOTE || (LA26_58 >= FALSE && LA26_58 <= FLOATING_POINT_LITERAL) || LA26_58 == ID || (LA26_58 >= QUOTED_STRING && LA26_58 <= RPAREN) || (LA26_58 >= TIMESTAMP && LA26_58 <= TRUE)) && ((strict == false)))
                {
                    s = 22;
                }

                input.seek(index26_58);
                if (s >= 0)
                    return s;
                break;

            case 13:
                int LA26_13 = input.LA(1);

                int index26_13 = input.index();
                input.rewind();
                s = -1;
                if ((LA26_13 == DOT))
                {
                    s = 23;
                }
                else if ((LA26_13 == EQUALS || (LA26_13 >= GREATERTHAN && LA26_13 <= GREATERTHANOREQUALS) || LA26_13 == IN || LA26_13 == IS || (LA26_13 >= LESSTHAN && LA26_13 <= LIKE) || LA26_13 == LPAREN || (LA26_13 >= NOT && LA26_13 <= NOTEQUALS)))
                {
                    s = 11;
                }
                else if ((LA26_13 == COLON || LA26_13 == DECIMAL_INTEGER_LITERAL || LA26_13 == DOUBLE_QUOTE || (LA26_13 >= FALSE && LA26_13 <= FLOATING_POINT_LITERAL) || LA26_13 == ID || (LA26_13 >= QUOTED_STRING && LA26_13 <= RPAREN) || (LA26_13 >= TIMESTAMP && LA26_13 <= TRUE)) && ((strict == false)))
                {
                    s = 22;
                }

                input.seek(index26_13);
                if (s >= 0)
                    return s;
                break;
            }
            if (state.backtracking > 0)
            {
                state.failed = true;
                return -1;
            }
            NoViableAltException nvae = new NoViableAltException(getDescription(), 26, _s, input);
            error(nvae);
            throw nvae;
        }
    }

    static final String DFA28_eotS = "\u00e5\uffff";
    static final String DFA28_eofS = "\u0083\uffff\1\145\36\uffff\1\u0084\35\uffff\1\u0084\44\uffff";
    static final String DFA28_minS = "\1\5\1\30\1\5\2\uffff\1\33\4\61\2\35\2\uffff\1\33\2\uffff\1\46\3\uffff" +
            "\35\33\1\uffff\3\13\1\uffff\1\35\1\5\1\30\1\13\1\5\2\13\1\5\2\13\1\5\1" +
            "\13\36\33\1\46\4\uffff\35\33\1\5\1\uffff\35\33\1\5\35\33\1\5\2\35\1\5" +
            "\3\13\35\33\1\35";
    static final String DFA28_maxS = "\1\127\1\66\1\127\2\uffff\4\61\1\110\2\66\2\uffff\1\45\2\uffff\1\57\3" +
            "\uffff\35\33\1\uffff\3\126\1\uffff\1\66\1\127\1\66\1\126\1\127\2\126\1" +
            "\127\2\126\1\127\1\126\35\33\1\45\1\57\4\uffff\35\33\1\111\1\uffff\35" +
            "\33\1\111\35\33\1\111\2\66\1\127\3\126\35\33\1\66";
    static final String DFA28_acceptS = "\3\uffff\2\1\7\uffff\2\5\1\uffff\2\1\1\uffff\1\2\1\3\1\4\35\uffff\1\6" +
            "\3\uffff\1\5\53\uffff\1\2\1\3\1\4\1\7\36\uffff\1\10\140\uffff";
    static final String DFA28_specialS = "\1\42\1\102\1\72\2\uffff\1\2\3\uffff\1\51\1\12\1\14\11\uffff\1\104\1\112" +
            "\1\117\1\124\1\106\1\113\1\73\1\75\1\50\1\56\1\34\1\43\1\20\1\25\1\10" +
            "\1\5\1\13\1\0\1\4\1\125\1\121\1\114\1\105\1\76\1\70\1\57\1\47\1\67\1\115" +
            "\1\uffff\1\120\1\40\1\37\3\uffff\1\123\1\1\1\uffff\1\64\1\27\1\uffff\1" +
            "\7\1\26\1\uffff\1\6\35\uffff\1\60\1\62\41\uffff\1\122\36\uffff\1\22\35" +
            "\uffff\1\32\1\uffff\1\53\1\3\1\31\1\24\1\23\1\21\1\17\1\16\1\15\1\44\1" +
            "\41\1\36\1\35\1\33\1\30\1\55\1\61\1\63\1\45\1\46\1\52\1\54\1\77\1\100" +
            "\1\101\1\66\1\71\1\74\1\110\1\107\1\111\1\103\1\116\1\11\1\65}>";
    static final String[] DFA28_transitionS = {
            "\1\4\1\5\2\4\1\uffff\1\4\1\15\5\uffff\1\6\1\uffff\1\14\1\uffff\1\4\5" +
                    "\uffff\1\2\3\uffff\1\13\1\14\1\4\3\uffff\1\1\2\4\1\7\1\10\3\4\2\uffff" +
                    "\1\4\5\uffff\1\4\1\uffff\1\4\1\uffff\4\4\13\uffff\1\14\1\uffff\1\3\1" +
                    "\4\11\uffff\1\11\1\12\1\4",
            "\1\16\4\uffff\1\17\5\uffff\2\17\1\uffff\1\22\3\uffff\1\24\2\uffff\2" +
                    "\17\1\23\1\uffff\1\20\3\uffff\1\21\1\17",
            "\1\37\1\45\1\26\1\53\1\uffff\1\52\6\uffff\1\46\3\uffff\1\54\11\uffff" +
                    "\1\57\1\uffff\1\27\3\uffff\1\61\1\41\1\31\1\47\1\50\1\43\1\30\1\32\2" +
                    "\uffff\1\42\5\uffff\1\40\1\uffff\1\44\1\uffff\1\34\1\36\1\51\1\33\15" +
                    "\uffff\1\60\1\25\11\uffff\1\55\1\56\1\35",
            "",
            "",
            "\1\62\11\uffff\1\62\13\uffff\1\20",
            "\1\63",
            "\1\64",
            "\1\65",
            "\1\20\26\uffff\1\66",
            "\1\66\5\uffff\2\66\10\uffff\2\66\2\uffff\1\20\4\uffff\1\66",
            "\1\66\5\uffff\2\66\10\uffff\2\66\2\uffff\1\20\4\uffff\1\66",
            "",
            "",
            "\1\70\11\uffff\1\67",
            "",
            "",
            "\1\22\10\uffff\1\23",
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
            "\1\20\7\uffff\1\20\7\uffff\1\73\3\uffff\2\20\4\uffff\1\72\42\uffff\1" +
                    "\74\1\20\13\uffff\2\20",
            "\1\20\7\uffff\1\20\7\uffff\1\76\3\uffff\2\20\4\uffff\1\75\42\uffff\1" +
                    "\77\1\20\13\uffff\2\20",
            "\1\20\7\uffff\1\20\7\uffff\1\101\3\uffff\2\20\4\uffff\1\100\42\uffff" +
                    "\1\102\1\20\13\uffff\2\20",
            "",
            "\1\17\5\uffff\2\17\1\uffff\1\22\3\uffff\1\24\2\uffff\2\17\1\23\5\uffff" +
                    "\1\21\1\17",
            "\1\115\1\123\1\104\1\131\1\uffff\1\130\6\uffff\1\124\3\uffff\1\132\11" +
                    "\uffff\1\135\1\uffff\1\105\3\uffff\1\137\1\117\1\107\1\125\1\126\1\121" +
                    "\1\106\1\110\2\uffff\1\120\5\uffff\1\116\1\uffff\1\122\1\uffff\1\112" +
                    "\1\114\1\127\1\111\15\uffff\1\136\1\103\11\uffff\1\133\1\134\1\113",
            "\1\140\4\uffff\1\20\5\uffff\2\20\1\uffff\1\142\3\uffff\1\144\2\uffff" +
                    "\2\20\1\143\5\uffff\1\141\1\20",
            "\1\20\3\uffff\1\145\3\uffff\1\20\4\uffff\1\20\2\uffff\1\20\3\uffff\2" +
                    "\20\4\uffff\1\20\42\uffff\2\20\13\uffff\2\20",
            "\1\160\1\166\1\147\1\174\1\uffff\1\173\6\uffff\1\167\3\uffff\1\175\11" +
                    "\uffff\1\u0080\1\uffff\1\150\3\uffff\1\u0082\1\162\1\152\1\170\1\171" +
                    "\1\164\1\151\1\153\2\uffff\1\163\5\uffff\1\161\1\uffff\1\165\1\uffff" +
                    "\1\155\1\157\1\172\1\154\15\uffff\1\u0081\1\146\11\uffff\1\176\1\177" +
                    "\1\156",
            "\1\20\7\uffff\1\20\7\uffff\1\20\3\uffff\2\20\4\uffff\1\20\42\uffff\1" +
                    "\20\1\u0083\13\uffff\2\20",
            "\1\20\3\uffff\1\u0084\3\uffff\1\20\4\uffff\1\20\2\uffff\1\20\3\uffff" +
                    "\2\20\4\uffff\1\20\42\uffff\2\20\13\uffff\2\20",
            "\1\u008f\1\u0095\1\u0086\1\u009b\1\uffff\1\u009a\6\uffff\1\u0096\3\uffff" +
                    "\1\u009c\11\uffff\1\u009f\1\uffff\1\u0087\3\uffff\1\u00a1\1\u0091\1\u0089" +
                    "\1\u0097\1\u0098\1\u0093\1\u0088\1\u008a\2\uffff\1\u0092\5\uffff\1\u0090" +
                    "\1\uffff\1\u0094\1\uffff\1\u008c\1\u008e\1\u0099\1\u008b\15\uffff\1\u00a0" +
                    "\1\u0085\11\uffff\1\u009d\1\u009e\1\u008d",
            "\1\20\7\uffff\1\20\7\uffff\1\20\3\uffff\2\20\4\uffff\1\20\42\uffff\1" +
                    "\20\1\u00a2\13\uffff\2\20",
            "\1\20\3\uffff\1\u0084\3\uffff\1\20\4\uffff\1\20\2\uffff\1\20\3\uffff" +
                    "\2\20\4\uffff\1\20\42\uffff\2\20\13\uffff\2\20",
            "\1\u00ad\1\u00b3\1\u00a4\1\u00b9\1\uffff\1\u00b8\6\uffff\1\u00b4\3\uffff" +
                    "\1\u00ba\11\uffff\1\u00bd\1\uffff\1\u00a5\3\uffff\1\u00bf\1\u00af\1\u00a7" +
                    "\1\u00b5\1\u00b6\1\u00b1\1\u00a6\1\u00a8\2\uffff\1\u00b0\5\uffff\1\u00ae" +
                    "\1\uffff\1\u00b2\1\uffff\1\u00aa\1\u00ac\1\u00b7\1\u00a9\15\uffff\1\u00be" +
                    "\1\u00a3\11\uffff\1\u00bb\1\u00bc\1\u00ab",
            "\1\20\7\uffff\1\20\7\uffff\1\20\3\uffff\2\20\4\uffff\1\20\42\uffff\1" +
                    "\20\1\u00c0\13\uffff\2\20",
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
            "\1\u00c3\11\uffff\1\u00c2",
            "\1\142\10\uffff\1\143",
            "",
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
            "\1\145\27\uffff\1\20\5\uffff\2\20\10\uffff\2\20\7\uffff\1\20\3\uffff" +
                    "\2\145\15\uffff\1\145",
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
            "\1\u0084\27\uffff\1\20\5\uffff\2\20\10\uffff\2\20\7\uffff\1\20\3\uffff" +
                    "\2\u0084\15\uffff\1\u0084",
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
            "\1\u0084\27\uffff\1\20\5\uffff\2\20\10\uffff\2\20\7\uffff\1\20\3\uffff" +
                    "\2\u0084\15\uffff\1\u0084",
            "\1\17\5\uffff\2\17\1\uffff\1\22\3\uffff\1\24\2\uffff\2\17\1\23\5\uffff" +
                    "\1\21\1\17",
            "\1\20\5\uffff\2\20\1\uffff\1\142\3\uffff\1\144\2\uffff\2\20\1\143\5" +
                    "\uffff\1\141\1\20",
            "\1\u00d1\1\u00d7\1\u00c8\1\u00dd\1\uffff\1\u00dc\6\uffff\1\u00d8\3\uffff" +
                    "\1\u00de\11\uffff\1\u00e1\1\uffff\1\u00c9\3\uffff\1\u00e3\1\u00d3\1\u00cb" +
                    "\1\u00d9\1\u00da\1\u00d5\1\u00ca\1\u00cc\2\uffff\1\u00d4\5\uffff\1\u00d2" +
                    "\1\uffff\1\u00d6\1\uffff\1\u00ce\1\u00d0\1\u00db\1\u00cd\15\uffff\1\u00e2" +
                    "\1\u00c7\11\uffff\1\u00df\1\u00e0\1\u00cf",
            "\1\20\3\uffff\1\145\3\uffff\1\20\4\uffff\1\20\2\uffff\1\20\3\uffff\2" +
                    "\20\4\uffff\1\20\42\uffff\2\20\13\uffff\2\20",
            "\1\20\3\uffff\1\u0084\3\uffff\1\20\4\uffff\1\20\2\uffff\1\20\3\uffff" +
                    "\2\20\4\uffff\1\20\42\uffff\2\20\13\uffff\2\20",
            "\1\20\3\uffff\1\u0084\3\uffff\1\20\4\uffff\1\20\2\uffff\1\20\3\uffff" +
                    "\2\20\4\uffff\1\20\42\uffff\2\20\13\uffff\2\20",
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
            "\1\20\5\uffff\2\20\1\uffff\1\142\3\uffff\1\144\2\uffff\2\20\1\143\5" +
                    "\uffff\1\141\1\20"
    };

    static final short[] DFA28_eot = DFA.unpackEncodedString(DFA28_eotS);
    static final short[] DFA28_eof = DFA.unpackEncodedString(DFA28_eofS);
    static final char[] DFA28_min = DFA.unpackEncodedStringToUnsignedChars(DFA28_minS);
    static final char[] DFA28_max = DFA.unpackEncodedStringToUnsignedChars(DFA28_maxS);
    static final short[] DFA28_accept = DFA.unpackEncodedString(DFA28_acceptS);
    static final short[] DFA28_special = DFA.unpackEncodedString(DFA28_specialS);
    static final short[][] DFA28_transition;

    static
    {
        int numStates = DFA28_transitionS.length;
        DFA28_transition = new short[numStates][];
        for (int i = 0; i < numStates; i++)
        {
            DFA28_transition[i] = DFA.unpackEncodedString(DFA28_transitionS[i]);
        }
    }

    protected class DFA28 extends DFA
    {

        public DFA28(BaseRecognizer recognizer)
        {
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

        @Override
        public String getDescription()
        {
            return "569:1: predicate : ( comparisonPredicate | inPredicate | likePredicate | nullPredicate | quantifiedComparisonPredicate | quantifiedInPredicate | textSearchPredicate | folderPredicate );";
        }

        @Override
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException
        {
            TokenStream input = (TokenStream) _input;
            int _s = s;
            switch (s)
            {
            case 0:
                int LA28_38 = input.LA(1);

                int index28_38 = input.index();
                input.rewind();
                s = -1;
                if ((LA28_38 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 57;
                }

                input.seek(index28_38);
                if (s >= 0)
                    return s;
                break;

            case 1:
                int LA28_58 = input.LA(1);

                int index28_58 = input.index();
                input.rewind();
                s = -1;
                if ((LA28_58 == COMMA))
                {
                    s = 101;
                }
                else if ((LA28_58 == COLON || LA28_58 == DECIMAL_INTEGER_LITERAL || LA28_58 == DOT || LA28_58 == DOUBLE_QUOTE || (LA28_58 >= FALSE && LA28_58 <= FLOATING_POINT_LITERAL) || LA28_58 == ID || (LA28_58 >= QUOTED_STRING && LA28_58 <= RPAREN) || (LA28_58 >= TIMESTAMP && LA28_58 <= TRUE)) && ((strict == false)))
                {
                    s = 16;
                }

                input.seek(index28_58);
                if (s >= 0)
                    return s;
                break;

            case 2:
                int LA28_5 = input.LA(1);

                int index28_5 = input.index();
                input.rewind();
                s = -1;
                if ((LA28_5 == LPAREN) && ((strict == false)))
                {
                    s = 16;
                }
                else if ((LA28_5 == DOUBLE_QUOTE || LA28_5 == ID))
                {
                    s = 50;
                }

                input.seek(index28_5);
                if (s >= 0)
                    return s;
                break;

            case 3:
                int LA28_195 = input.LA(1);

                int index28_195 = input.index();
                input.rewind();
                s = -1;
                if ((LA28_195 == SELECT) && ((strict == false)))
                {
                    s = 199;
                }
                else if ((LA28_195 == AS) && ((strict == false)))
                {
                    s = 200;
                }
                else if ((LA28_195 == FROM) && ((strict == false)))
                {
                    s = 201;
                }
                else if ((LA28_195 == JOIN) && ((strict == false)))
                {
                    s = 202;
                }
                else if ((LA28_195 == INNER) && ((strict == false)))
                {
                    s = 203;
                }
                else if ((LA28_195 == LEFT) && ((strict == false)))
                {
                    s = 204;
                }
                else if ((LA28_195 == OUTER) && ((strict == false)))
                {
                    s = 205;
                }
                else if ((LA28_195 == ON) && ((strict == false)))
                {
                    s = 206;
                }
                else if ((LA28_195 == WHERE) && ((strict == false)))
                {
                    s = 207;
                }
                else if ((LA28_195 == OR) && ((strict == false)))
                {
                    s = 208;
                }
                else if ((LA28_195 == AND) && ((strict == false)))
                {
                    s = 209;
                }
                else if ((LA28_195 == NOT) && ((strict == false)))
                {
                    s = 210;
                }
                else if ((LA28_195 == IN) && ((strict == false)))
                {
                    s = 211;
                }
                else if ((LA28_195 == LIKE) && ((strict == false)))
                {
                    s = 212;
                }
                else if ((LA28_195 == IS) && ((strict == false)))
                {
                    s = 213;
                }
                else if ((LA28_195 == NULL) && ((strict == false)))
                {
                    s = 214;
                }
                else if ((LA28_195 == ANY) && ((strict == false)))
                {
                    s = 215;
                }
                else if ((LA28_195 == CONTAINS) && ((strict == false)))
                {
                    s = 216;
                }
                else if ((LA28_195 == IN_FOLDER) && ((strict == false)))
                {
                    s = 217;
                }
                else if ((LA28_195 == IN_TREE) && ((strict == false)))
                {
                    s = 218;
                }
                else if ((LA28_195 == ORDER) && ((strict == false)))
                {
                    s = 219;
                }
                else if ((LA28_195 == BY) && ((strict == false)))
                {
                    s = 220;
                }
                else if ((LA28_195 == ASC) && ((strict == false)))
                {
                    s = 221;
                }
                else if ((LA28_195 == DESC) && ((strict == false)))
                {
                    s = 222;
                }
                else if ((LA28_195 == TIMESTAMP) && ((strict == false)))
                {
                    s = 223;
                }
                else if ((LA28_195 == TRUE) && ((strict == false)))
                {
                    s = 224;
                }
                else if ((LA28_195 == FALSE) && ((strict == false)))
                {
                    s = 225;
                }
                else if ((LA28_195 == SCORE) && ((strict == false)))
                {
                    s = 226;
                }
                else if ((LA28_195 == ID) && ((strict == false)))
                {
                    s = 227;
                }

                input.seek(index28_195);
                if (s >= 0)
                    return s;
                break;

            case 4:
                int LA28_39 = input.LA(1);

                int index28_39 = input.index();
                input.rewind();
                s = -1;
                if ((LA28_39 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 57;
                }

                input.seek(index28_39);
                if (s >= 0)
                    return s;
                break;

            case 5:
                int LA28_36 = input.LA(1);

                int index28_36 = input.index();
                input.rewind();
                s = -1;
                if ((LA28_36 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 57;
                }

                input.seek(index28_36);
                if (s >= 0)
                    return s;
                break;

            case 6:
                int LA28_66 = input.LA(1);

                int index28_66 = input.index();
                input.rewind();
                s = -1;
                if ((LA28_66 == RPAREN))
                {
                    s = 192;
                }
                else if ((LA28_66 == COLON || LA28_66 == DECIMAL_INTEGER_LITERAL || LA28_66 == DOUBLE_QUOTE || (LA28_66 >= FALSE && LA28_66 <= FLOATING_POINT_LITERAL) || LA28_66 == ID || LA28_66 == QUOTED_STRING || (LA28_66 >= TIMESTAMP && LA28_66 <= TRUE)) && ((strict == false)))
                {
                    s = 16;
                }

                input.seek(index28_66);
                if (s >= 0)
                    return s;
                break;

            case 7:
                int LA28_63 = input.LA(1);

                int index28_63 = input.index();
                input.rewind();
                s = -1;
                if ((LA28_63 == RPAREN))
                {
                    s = 162;
                }
                else if ((LA28_63 == COLON || LA28_63 == DECIMAL_INTEGER_LITERAL || LA28_63 == DOUBLE_QUOTE || (LA28_63 >= FALSE && LA28_63 <= FLOATING_POINT_LITERAL) || LA28_63 == ID || LA28_63 == QUOTED_STRING || (LA28_63 >= TIMESTAMP && LA28_63 <= TRUE)) && ((strict == false)))
                {
                    s = 16;
                }

                input.seek(index28_63);
                if (s >= 0)
                    return s;
                break;

            case 8:
                int LA28_35 = input.LA(1);

                int index28_35 = input.index();
                input.rewind();
                s = -1;
                if ((LA28_35 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 57;
                }

                input.seek(index28_35);
                if (s >= 0)
                    return s;
                break;

            case 9:
                int LA28_227 = input.LA(1);

                int index28_227 = input.index();
                input.rewind();
                s = -1;
                if ((LA28_227 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 228;
                }

                input.seek(index28_227);
                if (s >= 0)
                    return s;
                break;

            case 10:
                int LA28_10 = input.LA(1);

                int index28_10 = input.index();
                input.rewind();
                s = -1;
                if ((LA28_10 == LPAREN) && ((strict == false)))
                {
                    s = 16;
                }
                else if ((LA28_10 == EQUALS || (LA28_10 >= GREATERTHAN && LA28_10 <= GREATERTHANOREQUALS) || (LA28_10 >= LESSTHAN && LA28_10 <= LESSTHANOREQUALS) || LA28_10 == NOTEQUALS))
                {
                    s = 54;
                }

                input.seek(index28_10);
                if (s >= 0)
                    return s;
                break;

            case 11:
                int LA28_37 = input.LA(1);

                int index28_37 = input.index();
                input.rewind();
                s = -1;
                if ((LA28_37 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 57;
                }

                input.seek(index28_37);
                if (s >= 0)
                    return s;
                break;

            case 12:
                int LA28_11 = input.LA(1);

                int index28_11 = input.index();
                input.rewind();
                s = -1;
                if ((LA28_11 == LPAREN) && ((strict == false)))
                {
                    s = 16;
                }
                else if ((LA28_11 == EQUALS || (LA28_11 >= GREATERTHAN && LA28_11 <= GREATERTHANOREQUALS) || (LA28_11 >= LESSTHAN && LA28_11 <= LESSTHANOREQUALS) || LA28_11 == NOTEQUALS))
                {
                    s = 54;
                }

                input.seek(index28_11);
                if (s >= 0)
                    return s;
                break;

            case 13:
                int LA28_202 = input.LA(1);

                int index28_202 = input.index();
                input.rewind();
                s = -1;
                if ((LA28_202 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 228;
                }

                input.seek(index28_202);
                if (s >= 0)
                    return s;
                break;

            case 14:
                int LA28_201 = input.LA(1);

                int index28_201 = input.index();
                input.rewind();
                s = -1;
                if ((LA28_201 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 228;
                }

                input.seek(index28_201);
                if (s >= 0)
                    return s;
                break;

            case 15:
                int LA28_200 = input.LA(1);

                int index28_200 = input.index();
                input.rewind();
                s = -1;
                if ((LA28_200 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 228;
                }

                input.seek(index28_200);
                if (s >= 0)
                    return s;
                break;

            case 16:
                int LA28_33 = input.LA(1);

                int index28_33 = input.index();
                input.rewind();
                s = -1;
                if ((LA28_33 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 57;
                }

                input.seek(index28_33);
                if (s >= 0)
                    return s;
                break;

            case 17:
                int LA28_199 = input.LA(1);

                int index28_199 = input.index();
                input.rewind();
                s = -1;
                if ((LA28_199 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 228;
                }

                input.seek(index28_199);
                if (s >= 0)
                    return s;
                break;

            case 18:
                int LA28_162 = input.LA(1);

                int index28_162 = input.index();
                input.rewind();
                s = -1;
                if ((LA28_162 == EQUALS || (LA28_162 >= GREATERTHAN && LA28_162 <= GREATERTHANOREQUALS) || (LA28_162 >= LESSTHAN && LA28_162 <= LESSTHANOREQUALS) || LA28_162 == NOTEQUALS) && ((strict == false)))
                {
                    s = 16;
                }
                else if ((LA28_162 == EOF || LA28_162 == AND || (LA28_162 >= OR && LA28_162 <= ORDER) || LA28_162 == RPAREN))
                {
                    s = 132;
                }

                input.seek(index28_162);
                if (s >= 0)
                    return s;
                break;

            case 19:
                int LA28_198 = input.LA(1);

                int index28_198 = input.index();
                input.rewind();
                s = -1;
                if ((LA28_198 == COLON || LA28_198 == DECIMAL_INTEGER_LITERAL || LA28_198 == DOT || LA28_198 == DOUBLE_QUOTE || (LA28_198 >= FALSE && LA28_198 <= FLOATING_POINT_LITERAL) || LA28_198 == ID || (LA28_198 >= QUOTED_STRING && LA28_198 <= RPAREN) || (LA28_198 >= TIMESTAMP && LA28_198 <= TRUE)) && ((strict == false)))
                {
                    s = 16;
                }
                else if ((LA28_198 == COMMA))
                {
                    s = 132;
                }

                input.seek(index28_198);
                if (s >= 0)
                    return s;
                break;

            case 20:
                int LA28_197 = input.LA(1);

                int index28_197 = input.index();
                input.rewind();
                s = -1;
                if ((LA28_197 == COLON || LA28_197 == DECIMAL_INTEGER_LITERAL || LA28_197 == DOT || LA28_197 == DOUBLE_QUOTE || (LA28_197 >= FALSE && LA28_197 <= FLOATING_POINT_LITERAL) || LA28_197 == ID || (LA28_197 >= QUOTED_STRING && LA28_197 <= RPAREN) || (LA28_197 >= TIMESTAMP && LA28_197 <= TRUE)) && ((strict == false)))
                {
                    s = 16;
                }
                else if ((LA28_197 == COMMA))
                {
                    s = 132;
                }

                input.seek(index28_197);
                if (s >= 0)
                    return s;
                break;

            case 21:
                int LA28_34 = input.LA(1);

                int index28_34 = input.index();
                input.rewind();
                s = -1;
                if ((LA28_34 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 57;
                }

                input.seek(index28_34);
                if (s >= 0)
                    return s;
                break;

            case 22:
                int LA28_64 = input.LA(1);

                int index28_64 = input.index();
                input.rewind();
                s = -1;
                if ((LA28_64 == COLON || LA28_64 == DECIMAL_INTEGER_LITERAL || LA28_64 == DOT || LA28_64 == DOUBLE_QUOTE || (LA28_64 >= FALSE && LA28_64 <= FLOATING_POINT_LITERAL) || LA28_64 == ID || (LA28_64 >= QUOTED_STRING && LA28_64 <= RPAREN) || (LA28_64 >= TIMESTAMP && LA28_64 <= TRUE)) && ((strict == false)))
                {
                    s = 16;
                }
                else if ((LA28_64 == COMMA))
                {
                    s = 132;
                }

                input.seek(index28_64);
                if (s >= 0)
                    return s;
                break;

            case 23:
                int LA28_61 = input.LA(1);

                int index28_61 = input.index();
                input.rewind();
                s = -1;
                if ((LA28_61 == COLON || LA28_61 == DECIMAL_INTEGER_LITERAL || LA28_61 == DOT || LA28_61 == DOUBLE_QUOTE || (LA28_61 >= FALSE && LA28_61 <= FLOATING_POINT_LITERAL) || LA28_61 == ID || (LA28_61 >= QUOTED_STRING && LA28_61 <= RPAREN) || (LA28_61 >= TIMESTAMP && LA28_61 <= TRUE)) && ((strict == false)))
                {
                    s = 16;
                }
                else if ((LA28_61 == COMMA))
                {
                    s = 132;
                }

                input.seek(index28_61);
                if (s >= 0)
                    return s;
                break;

            case 24:
                int LA28_208 = input.LA(1);

                int index28_208 = input.index();
                input.rewind();
                s = -1;
                if ((LA28_208 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 228;
                }

                input.seek(index28_208);
                if (s >= 0)
                    return s;
                break;

            case 25:
                int LA28_196 = input.LA(1);

                int index28_196 = input.index();
                input.rewind();
                s = -1;
                if ((LA28_196 == COMMA))
                {
                    s = 101;
                }
                else if ((LA28_196 == COLON || LA28_196 == DECIMAL_INTEGER_LITERAL || LA28_196 == DOT || LA28_196 == DOUBLE_QUOTE || (LA28_196 >= FALSE && LA28_196 <= FLOATING_POINT_LITERAL) || LA28_196 == ID || (LA28_196 >= QUOTED_STRING && LA28_196 <= RPAREN) || (LA28_196 >= TIMESTAMP && LA28_196 <= TRUE)) && ((strict == false)))
                {
                    s = 16;
                }

                input.seek(index28_196);
                if (s >= 0)
                    return s;
                break;

            case 26:
                int LA28_192 = input.LA(1);

                int index28_192 = input.index();
                input.rewind();
                s = -1;
                if ((LA28_192 == EQUALS || (LA28_192 >= GREATERTHAN && LA28_192 <= GREATERTHANOREQUALS) || (LA28_192 >= LESSTHAN && LA28_192 <= LESSTHANOREQUALS) || LA28_192 == NOTEQUALS) && ((strict == false)))
                {
                    s = 16;
                }
                else if ((LA28_192 == EOF || LA28_192 == AND || (LA28_192 >= OR && LA28_192 <= ORDER) || LA28_192 == RPAREN))
                {
                    s = 132;
                }

                input.seek(index28_192);
                if (s >= 0)
                    return s;
                break;

            case 27:
                int LA28_207 = input.LA(1);

                int index28_207 = input.index();
                input.rewind();
                s = -1;
                if ((LA28_207 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 228;
                }

                input.seek(index28_207);
                if (s >= 0)
                    return s;
                break;

            case 28:
                int LA28_31 = input.LA(1);

                int index28_31 = input.index();
                input.rewind();
                s = -1;
                if ((LA28_31 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 57;
                }

                input.seek(index28_31);
                if (s >= 0)
                    return s;
                break;

            case 29:
                int LA28_206 = input.LA(1);

                int index28_206 = input.index();
                input.rewind();
                s = -1;
                if ((LA28_206 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 228;
                }

                input.seek(index28_206);
                if (s >= 0)
                    return s;
                break;

            case 30:
                int LA28_205 = input.LA(1);

                int index28_205 = input.index();
                input.rewind();
                s = -1;
                if ((LA28_205 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 228;
                }

                input.seek(index28_205);
                if (s >= 0)
                    return s;
                break;

            case 31:
                int LA28_53 = input.LA(1);

                int index28_53 = input.index();
                input.rewind();
                s = -1;
                if ((LA28_53 == ID))
                {
                    s = 64;
                }
                else if ((LA28_53 == DOUBLE_QUOTE))
                {
                    s = 65;
                }
                else if ((LA28_53 == COLON || LA28_53 == DECIMAL_INTEGER_LITERAL || (LA28_53 >= FALSE && LA28_53 <= FLOATING_POINT_LITERAL) || LA28_53 == RPAREN || (LA28_53 >= TIMESTAMP && LA28_53 <= TRUE)) && ((strict == false)))
                {
                    s = 16;
                }
                else if ((LA28_53 == QUOTED_STRING))
                {
                    s = 66;
                }

                input.seek(index28_53);
                if (s >= 0)
                    return s;
                break;

            case 32:
                int LA28_52 = input.LA(1);

                int index28_52 = input.index();
                input.rewind();
                s = -1;
                if ((LA28_52 == ID))
                {
                    s = 61;
                }
                else if ((LA28_52 == DOUBLE_QUOTE))
                {
                    s = 62;
                }
                else if ((LA28_52 == COLON || LA28_52 == DECIMAL_INTEGER_LITERAL || (LA28_52 >= FALSE && LA28_52 <= FLOATING_POINT_LITERAL) || LA28_52 == RPAREN || (LA28_52 >= TIMESTAMP && LA28_52 <= TRUE)) && ((strict == false)))
                {
                    s = 16;
                }
                else if ((LA28_52 == QUOTED_STRING))
                {
                    s = 63;
                }

                input.seek(index28_52);
                if (s >= 0)
                    return s;
                break;

            case 33:
                int LA28_204 = input.LA(1);

                int index28_204 = input.index();
                input.rewind();
                s = -1;
                if ((LA28_204 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 228;
                }

                input.seek(index28_204);
                if (s >= 0)
                    return s;
                break;

            case 34:
                int LA28_0 = input.LA(1);

                int index28_0 = input.index();
                input.rewind();
                s = -1;
                if ((LA28_0 == ID))
                {
                    s = 1;
                }
                else if ((LA28_0 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 2;
                }
                else if ((LA28_0 == SCORE))
                {
                    s = 3;
                }
                else if ((LA28_0 == AND || (LA28_0 >= AS && LA28_0 <= ASC) || LA28_0 == BY || LA28_0 == DESC || LA28_0 == FROM || (LA28_0 >= IN && LA28_0 <= INNER) || (LA28_0 >= IS && LA28_0 <= LEFT) || LA28_0 == LIKE || LA28_0 == NOT || LA28_0 == NULL || (LA28_0 >= ON && LA28_0 <= OUTER) || LA28_0 == SELECT || LA28_0 == WHERE) && ((strict == false)))
                {
                    s = 4;
                }
                else if ((LA28_0 == ANY))
                {
                    s = 5;
                }
                else if ((LA28_0 == CONTAINS))
                {
                    s = 6;
                }
                else if ((LA28_0 == IN_FOLDER))
                {
                    s = 7;
                }
                else if ((LA28_0 == IN_TREE))
                {
                    s = 8;
                }
                else if ((LA28_0 == TIMESTAMP))
                {
                    s = 9;
                }
                else if ((LA28_0 == TRUE))
                {
                    s = 10;
                }
                else if ((LA28_0 == FALSE))
                {
                    s = 11;
                }
                else if ((LA28_0 == DECIMAL_INTEGER_LITERAL || LA28_0 == FLOATING_POINT_LITERAL || LA28_0 == QUOTED_STRING))
                {
                    s = 12;
                }
                else if ((LA28_0 == COLON) && ((strict == false)))
                {
                    s = 13;
                }

                input.seek(index28_0);
                if (s >= 0)
                    return s;
                break;

            case 35:
                int LA28_32 = input.LA(1);

                int index28_32 = input.index();
                input.rewind();
                s = -1;
                if ((LA28_32 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 57;
                }

                input.seek(index28_32);
                if (s >= 0)
                    return s;
                break;

            case 36:
                int LA28_203 = input.LA(1);

                int index28_203 = input.index();
                input.rewind();
                s = -1;
                if ((LA28_203 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 228;
                }

                input.seek(index28_203);
                if (s >= 0)
                    return s;
                break;

            case 37:
                int LA28_212 = input.LA(1);

                int index28_212 = input.index();
                input.rewind();
                s = -1;
                if ((LA28_212 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 228;
                }

                input.seek(index28_212);
                if (s >= 0)
                    return s;
                break;

            case 38:
                int LA28_213 = input.LA(1);

                int index28_213 = input.index();
                input.rewind();
                s = -1;
                if ((LA28_213 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 228;
                }

                input.seek(index28_213);
                if (s >= 0)
                    return s;
                break;

            case 39:
                int LA28_47 = input.LA(1);

                int index28_47 = input.index();
                input.rewind();
                s = -1;
                if ((LA28_47 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 57;
                }

                input.seek(index28_47);
                if (s >= 0)
                    return s;
                break;

            case 40:
                int LA28_29 = input.LA(1);

                int index28_29 = input.index();
                input.rewind();
                s = -1;
                if ((LA28_29 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 57;
                }

                input.seek(index28_29);
                if (s >= 0)
                    return s;
                break;

            case 41:
                int LA28_9 = input.LA(1);

                int index28_9 = input.index();
                input.rewind();
                s = -1;
                if ((LA28_9 == QUOTED_STRING))
                {
                    s = 54;
                }
                else if ((LA28_9 == LPAREN) && ((strict == false)))
                {
                    s = 16;
                }

                input.seek(index28_9);
                if (s >= 0)
                    return s;
                break;

            case 42:
                int LA28_214 = input.LA(1);

                int index28_214 = input.index();
                input.rewind();
                s = -1;
                if ((LA28_214 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 228;
                }

                input.seek(index28_214);
                if (s >= 0)
                    return s;
                break;

            case 43:
                int LA28_194 = input.LA(1);

                int index28_194 = input.index();
                input.rewind();
                s = -1;
                if ((LA28_194 == EQUALS || (LA28_194 >= GREATERTHAN && LA28_194 <= GREATERTHANOREQUALS) || (LA28_194 >= LESSTHAN && LA28_194 <= LESSTHANOREQUALS) || LA28_194 == NOTEQUALS) && ((strict == false)))
                {
                    s = 16;
                }
                else if ((LA28_194 == NOT) && ((strict == false)))
                {
                    s = 97;
                }
                else if ((LA28_194 == IN) && ((strict == false)))
                {
                    s = 98;
                }
                else if ((LA28_194 == LIKE) && ((strict == false)))
                {
                    s = 99;
                }
                else if ((LA28_194 == IS) && ((strict == false)))
                {
                    s = 100;
                }

                input.seek(index28_194);
                if (s >= 0)
                    return s;
                break;

            case 44:
                int LA28_215 = input.LA(1);

                int index28_215 = input.index();
                input.rewind();
                s = -1;
                if ((LA28_215 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 228;
                }

                input.seek(index28_215);
                if (s >= 0)
                    return s;
                break;

            case 45:
                int LA28_209 = input.LA(1);

                int index28_209 = input.index();
                input.rewind();
                s = -1;
                if ((LA28_209 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 228;
                }

                input.seek(index28_209);
                if (s >= 0)
                    return s;
                break;

            case 46:
                int LA28_30 = input.LA(1);

                int index28_30 = input.index();
                input.rewind();
                s = -1;
                if ((LA28_30 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 57;
                }

                input.seek(index28_30);
                if (s >= 0)
                    return s;
                break;

            case 47:
                int LA28_46 = input.LA(1);

                int index28_46 = input.index();
                input.rewind();
                s = -1;
                if ((LA28_46 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 57;
                }

                input.seek(index28_46);
                if (s >= 0)
                    return s;
                break;

            case 48:
                int LA28_96 = input.LA(1);

                int index28_96 = input.index();
                input.rewind();
                s = -1;
                if ((LA28_96 == ID) && ((strict == false)))
                {
                    s = 194;
                }
                else if ((LA28_96 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 195;
                }

                input.seek(index28_96);
                if (s >= 0)
                    return s;
                break;

            case 49:
                int LA28_210 = input.LA(1);

                int index28_210 = input.index();
                input.rewind();
                s = -1;
                if ((LA28_210 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 228;
                }

                input.seek(index28_210);
                if (s >= 0)
                    return s;
                break;

            case 50:
                int LA28_97 = input.LA(1);

                int index28_97 = input.index();
                input.rewind();
                s = -1;
                if ((LA28_97 == IN) && ((strict == false)))
                {
                    s = 98;
                }
                else if ((LA28_97 == LIKE) && ((strict == false)))
                {
                    s = 99;
                }

                input.seek(index28_97);
                if (s >= 0)
                    return s;
                break;

            case 51:
                int LA28_211 = input.LA(1);

                int index28_211 = input.index();
                input.rewind();
                s = -1;
                if ((LA28_211 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 228;
                }

                input.seek(index28_211);
                if (s >= 0)
                    return s;
                break;

            case 52:
                int LA28_60 = input.LA(1);

                int index28_60 = input.index();
                input.rewind();
                s = -1;
                if ((LA28_60 == RPAREN))
                {
                    s = 131;
                }
                else if ((LA28_60 == COLON || LA28_60 == DECIMAL_INTEGER_LITERAL || LA28_60 == DOUBLE_QUOTE || (LA28_60 >= FALSE && LA28_60 <= FLOATING_POINT_LITERAL) || LA28_60 == ID || LA28_60 == QUOTED_STRING || (LA28_60 >= TIMESTAMP && LA28_60 <= TRUE)) && ((strict == false)))
                {
                    s = 16;
                }

                input.seek(index28_60);
                if (s >= 0)
                    return s;
                break;

            case 53:
                int LA28_228 = input.LA(1);

                int index28_228 = input.index();
                input.rewind();
                s = -1;
                if ((LA28_228 == EQUALS || (LA28_228 >= GREATERTHAN && LA28_228 <= GREATERTHANOREQUALS) || (LA28_228 >= LESSTHAN && LA28_228 <= LESSTHANOREQUALS) || LA28_228 == NOTEQUALS) && ((strict == false)))
                {
                    s = 16;
                }
                else if ((LA28_228 == NOT) && ((strict == false)))
                {
                    s = 97;
                }
                else if ((LA28_228 == IN) && ((strict == false)))
                {
                    s = 98;
                }
                else if ((LA28_228 == LIKE) && ((strict == false)))
                {
                    s = 99;
                }
                else if ((LA28_228 == IS) && ((strict == false)))
                {
                    s = 100;
                }

                input.seek(index28_228);
                if (s >= 0)
                    return s;
                break;

            case 54:
                int LA28_219 = input.LA(1);

                int index28_219 = input.index();
                input.rewind();
                s = -1;
                if ((LA28_219 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 228;
                }

                input.seek(index28_219);
                if (s >= 0)
                    return s;
                break;

            case 55:
                int LA28_48 = input.LA(1);

                int index28_48 = input.index();
                input.rewind();
                s = -1;
                if ((LA28_48 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 57;
                }

                input.seek(index28_48);
                if (s >= 0)
                    return s;
                break;

            case 56:
                int LA28_45 = input.LA(1);

                int index28_45 = input.index();
                input.rewind();
                s = -1;
                if ((LA28_45 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 57;
                }

                input.seek(index28_45);
                if (s >= 0)
                    return s;
                break;

            case 57:
                int LA28_220 = input.LA(1);

                int index28_220 = input.index();
                input.rewind();
                s = -1;
                if ((LA28_220 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 228;
                }

                input.seek(index28_220);
                if (s >= 0)
                    return s;
                break;

            case 58:
                int LA28_2 = input.LA(1);

                int index28_2 = input.index();
                input.rewind();
                s = -1;
                if ((LA28_2 == SELECT) && ((strict == false)))
                {
                    s = 21;
                }
                else if ((LA28_2 == AS) && ((strict == false)))
                {
                    s = 22;
                }
                else if ((LA28_2 == FROM) && ((strict == false)))
                {
                    s = 23;
                }
                else if ((LA28_2 == JOIN) && ((strict == false)))
                {
                    s = 24;
                }
                else if ((LA28_2 == INNER) && ((strict == false)))
                {
                    s = 25;
                }
                else if ((LA28_2 == LEFT) && ((strict == false)))
                {
                    s = 26;
                }
                else if ((LA28_2 == OUTER) && ((strict == false)))
                {
                    s = 27;
                }
                else if ((LA28_2 == ON) && ((strict == false)))
                {
                    s = 28;
                }
                else if ((LA28_2 == WHERE) && ((strict == false)))
                {
                    s = 29;
                }
                else if ((LA28_2 == OR) && ((strict == false)))
                {
                    s = 30;
                }
                else if ((LA28_2 == AND) && ((strict == false)))
                {
                    s = 31;
                }
                else if ((LA28_2 == NOT) && ((strict == false)))
                {
                    s = 32;
                }
                else if ((LA28_2 == IN) && ((strict == false)))
                {
                    s = 33;
                }
                else if ((LA28_2 == LIKE) && ((strict == false)))
                {
                    s = 34;
                }
                else if ((LA28_2 == IS) && ((strict == false)))
                {
                    s = 35;
                }
                else if ((LA28_2 == NULL) && ((strict == false)))
                {
                    s = 36;
                }
                else if ((LA28_2 == ANY) && ((strict == false)))
                {
                    s = 37;
                }
                else if ((LA28_2 == CONTAINS) && ((strict == false)))
                {
                    s = 38;
                }
                else if ((LA28_2 == IN_FOLDER) && ((strict == false)))
                {
                    s = 39;
                }
                else if ((LA28_2 == IN_TREE) && ((strict == false)))
                {
                    s = 40;
                }
                else if ((LA28_2 == ORDER) && ((strict == false)))
                {
                    s = 41;
                }
                else if ((LA28_2 == BY) && ((strict == false)))
                {
                    s = 42;
                }
                else if ((LA28_2 == ASC) && ((strict == false)))
                {
                    s = 43;
                }
                else if ((LA28_2 == DESC) && ((strict == false)))
                {
                    s = 44;
                }
                else if ((LA28_2 == TIMESTAMP) && ((strict == false)))
                {
                    s = 45;
                }
                else if ((LA28_2 == TRUE) && ((strict == false)))
                {
                    s = 46;
                }
                else if ((LA28_2 == FALSE) && ((strict == false)))
                {
                    s = 47;
                }
                else if ((LA28_2 == SCORE) && ((strict == false)))
                {
                    s = 48;
                }
                else if ((LA28_2 == ID) && ((strict == false)))
                {
                    s = 49;
                }

                input.seek(index28_2);
                if (s >= 0)
                    return s;
                break;

            case 59:
                int LA28_27 = input.LA(1);

                int index28_27 = input.index();
                input.rewind();
                s = -1;
                if ((LA28_27 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 57;
                }

                input.seek(index28_27);
                if (s >= 0)
                    return s;
                break;

            case 60:
                int LA28_221 = input.LA(1);

                int index28_221 = input.index();
                input.rewind();
                s = -1;
                if ((LA28_221 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 228;
                }

                input.seek(index28_221);
                if (s >= 0)
                    return s;
                break;

            case 61:
                int LA28_28 = input.LA(1);

                int index28_28 = input.index();
                input.rewind();
                s = -1;
                if ((LA28_28 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 57;
                }

                input.seek(index28_28);
                if (s >= 0)
                    return s;
                break;

            case 62:
                int LA28_44 = input.LA(1);

                int index28_44 = input.index();
                input.rewind();
                s = -1;
                if ((LA28_44 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 57;
                }

                input.seek(index28_44);
                if (s >= 0)
                    return s;
                break;

            case 63:
                int LA28_216 = input.LA(1);

                int index28_216 = input.index();
                input.rewind();
                s = -1;
                if ((LA28_216 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 228;
                }

                input.seek(index28_216);
                if (s >= 0)
                    return s;
                break;

            case 64:
                int LA28_217 = input.LA(1);

                int index28_217 = input.index();
                input.rewind();
                s = -1;
                if ((LA28_217 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 228;
                }

                input.seek(index28_217);
                if (s >= 0)
                    return s;
                break;

            case 65:
                int LA28_218 = input.LA(1);

                int index28_218 = input.index();
                input.rewind();
                s = -1;
                if ((LA28_218 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 228;
                }

                input.seek(index28_218);
                if (s >= 0)
                    return s;
                break;

            case 66:
                int LA28_1 = input.LA(1);

                int index28_1 = input.index();
                input.rewind();
                s = -1;
                if ((LA28_1 == DOT))
                {
                    s = 14;
                }
                else if ((LA28_1 == EQUALS || (LA28_1 >= GREATERTHAN && LA28_1 <= GREATERTHANOREQUALS) || (LA28_1 >= LESSTHAN && LA28_1 <= LESSTHANOREQUALS) || LA28_1 == NOTEQUALS))
                {
                    s = 15;
                }
                else if ((LA28_1 == LPAREN) && ((strict == false)))
                {
                    s = 16;
                }
                else if ((LA28_1 == NOT))
                {
                    s = 17;
                }
                else if ((LA28_1 == IN))
                {
                    s = 18;
                }
                else if ((LA28_1 == LIKE))
                {
                    s = 19;
                }
                else if ((LA28_1 == IS))
                {
                    s = 20;
                }

                input.seek(index28_1);
                if (s >= 0)
                    return s;
                break;

            case 67:
                int LA28_225 = input.LA(1);

                int index28_225 = input.index();
                input.rewind();
                s = -1;
                if ((LA28_225 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 228;
                }

                input.seek(index28_225);
                if (s >= 0)
                    return s;
                break;

            case 68:
                int LA28_21 = input.LA(1);

                int index28_21 = input.index();
                input.rewind();
                s = -1;
                if ((LA28_21 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 57;
                }

                input.seek(index28_21);
                if (s >= 0)
                    return s;
                break;

            case 69:
                int LA28_43 = input.LA(1);

                int index28_43 = input.index();
                input.rewind();
                s = -1;
                if ((LA28_43 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 57;
                }

                input.seek(index28_43);
                if (s >= 0)
                    return s;
                break;

            case 70:
                int LA28_25 = input.LA(1);

                int index28_25 = input.index();
                input.rewind();
                s = -1;
                if ((LA28_25 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 57;
                }

                input.seek(index28_25);
                if (s >= 0)
                    return s;
                break;

            case 71:
                int LA28_223 = input.LA(1);

                int index28_223 = input.index();
                input.rewind();
                s = -1;
                if ((LA28_223 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 228;
                }

                input.seek(index28_223);
                if (s >= 0)
                    return s;
                break;

            case 72:
                int LA28_222 = input.LA(1);

                int index28_222 = input.index();
                input.rewind();
                s = -1;
                if ((LA28_222 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 228;
                }

                input.seek(index28_222);
                if (s >= 0)
                    return s;
                break;

            case 73:
                int LA28_224 = input.LA(1);

                int index28_224 = input.index();
                input.rewind();
                s = -1;
                if ((LA28_224 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 228;
                }

                input.seek(index28_224);
                if (s >= 0)
                    return s;
                break;

            case 74:
                int LA28_22 = input.LA(1);

                int index28_22 = input.index();
                input.rewind();
                s = -1;
                if ((LA28_22 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 57;
                }

                input.seek(index28_22);
                if (s >= 0)
                    return s;
                break;

            case 75:
                int LA28_26 = input.LA(1);

                int index28_26 = input.index();
                input.rewind();
                s = -1;
                if ((LA28_26 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 57;
                }

                input.seek(index28_26);
                if (s >= 0)
                    return s;
                break;

            case 76:
                int LA28_42 = input.LA(1);

                int index28_42 = input.index();
                input.rewind();
                s = -1;
                if ((LA28_42 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 57;
                }

                input.seek(index28_42);
                if (s >= 0)
                    return s;
                break;

            case 77:
                int LA28_49 = input.LA(1);

                int index28_49 = input.index();
                input.rewind();
                s = -1;
                if ((LA28_49 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 57;
                }

                input.seek(index28_49);
                if (s >= 0)
                    return s;
                break;

            case 78:
                int LA28_226 = input.LA(1);

                int index28_226 = input.index();
                input.rewind();
                s = -1;
                if ((LA28_226 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 228;
                }

                input.seek(index28_226);
                if (s >= 0)
                    return s;
                break;

            case 79:
                int LA28_23 = input.LA(1);

                int index28_23 = input.index();
                input.rewind();
                s = -1;
                if ((LA28_23 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 57;
                }

                input.seek(index28_23);
                if (s >= 0)
                    return s;
                break;

            case 80:
                int LA28_51 = input.LA(1);

                int index28_51 = input.index();
                input.rewind();
                s = -1;
                if ((LA28_51 == ID))
                {
                    s = 58;
                }
                else if ((LA28_51 == DOUBLE_QUOTE))
                {
                    s = 59;
                }
                else if ((LA28_51 == QUOTED_STRING))
                {
                    s = 60;
                }
                else if ((LA28_51 == COLON || LA28_51 == DECIMAL_INTEGER_LITERAL || (LA28_51 >= FALSE && LA28_51 <= FLOATING_POINT_LITERAL) || LA28_51 == RPAREN || (LA28_51 >= TIMESTAMP && LA28_51 <= TRUE)) && ((strict == false)))
                {
                    s = 16;
                }

                input.seek(index28_51);
                if (s >= 0)
                    return s;
                break;

            case 81:
                int LA28_41 = input.LA(1);

                int index28_41 = input.index();
                input.rewind();
                s = -1;
                if ((LA28_41 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 57;
                }

                input.seek(index28_41);
                if (s >= 0)
                    return s;
                break;

            case 82:
                int LA28_131 = input.LA(1);

                int index28_131 = input.index();
                input.rewind();
                s = -1;
                if ((LA28_131 == EOF || LA28_131 == AND || (LA28_131 >= OR && LA28_131 <= ORDER) || LA28_131 == RPAREN))
                {
                    s = 101;
                }
                else if ((LA28_131 == EQUALS || (LA28_131 >= GREATERTHAN && LA28_131 <= GREATERTHANOREQUALS) || (LA28_131 >= LESSTHAN && LA28_131 <= LESSTHANOREQUALS) || LA28_131 == NOTEQUALS) && ((strict == false)))
                {
                    s = 16;
                }

                input.seek(index28_131);
                if (s >= 0)
                    return s;
                break;

            case 83:
                int LA28_57 = input.LA(1);

                int index28_57 = input.index();
                input.rewind();
                s = -1;
                if ((LA28_57 == DOT) && ((strict == false)))
                {
                    s = 96;
                }
                else if ((LA28_57 == EQUALS || (LA28_57 >= GREATERTHAN && LA28_57 <= GREATERTHANOREQUALS) || (LA28_57 >= LESSTHAN && LA28_57 <= LESSTHANOREQUALS) || LA28_57 == NOTEQUALS) && ((strict == false)))
                {
                    s = 16;
                }
                else if ((LA28_57 == NOT) && ((strict == false)))
                {
                    s = 97;
                }
                else if ((LA28_57 == IN) && ((strict == false)))
                {
                    s = 98;
                }
                else if ((LA28_57 == LIKE) && ((strict == false)))
                {
                    s = 99;
                }
                else if ((LA28_57 == IS) && ((strict == false)))
                {
                    s = 100;
                }

                input.seek(index28_57);
                if (s >= 0)
                    return s;
                break;

            case 84:
                int LA28_24 = input.LA(1);

                int index28_24 = input.index();
                input.rewind();
                s = -1;
                if ((LA28_24 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 57;
                }

                input.seek(index28_24);
                if (s >= 0)
                    return s;
                break;

            case 85:
                int LA28_40 = input.LA(1);

                int index28_40 = input.index();
                input.rewind();
                s = -1;
                if ((LA28_40 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 57;
                }

                input.seek(index28_40);
                if (s >= 0)
                    return s;
                break;
            }
            if (state.backtracking > 0)
            {
                state.failed = true;
                return -1;
            }
            NoViableAltException nvae = new NoViableAltException(getDescription(), 28, _s, input);
            error(nvae);
            throw nvae;
        }
    }

    static final String DFA34_eotS = "\151\uffff";
    static final String DFA34_eofS = "\151\uffff";
    static final String DFA34_minS = "\1\33\1\30\1\5\1\33\1\65\35\33\1\52\1\5\2\uffff\1\30\36\33\1\65\2\52\1" +
            "\5\2\uffff\35\33\1\52";
    static final String DFA34_maxS = "\1\45\1\52\1\127\1\45\1\67\35\33\1\52\1\127\2\uffff\1\52\35\33\1\45\1" +
            "\67\2\52\1\127\2\uffff\35\33\1\52";
    static final String DFA34_acceptS = "\44\uffff\1\1\1\2\43\uffff\1\1\1\2\36\uffff";
    static final String DFA34_specialS = "\1\46\1\uffff\1\50\2\uffff\1\35\1\26\1\31\1\33\1\44\1\47\1\45\1\37\1\42" +
            "\1\73\1\72\1\75\1\57\1\65\1\63\1\7\1\1\1\20\1\24\1\14\1\32\1\27\1\30\1" +
            "\36\1\34\1\43\1\41\1\52\1\71\4\uffff\1\40\35\uffff\1\16\1\66\1\uffff\1" +
            "\62\1\25\2\uffff\1\12\1\15\1\13\1\21\1\17\1\23\1\22\1\2\1\0\1\4\1\3\1" +
            "\6\1\5\1\11\1\10\1\70\1\67\1\64\1\61\1\60\1\55\1\54\1\51\1\101\1\77\1" +
            "\76\1\74\1\100\1\56\1\53}>";
    static final String[] DFA34_transitionS = {
            "\1\2\11\uffff\1\1",
            "\1\3\21\uffff\1\4",
            "\1\17\1\25\1\6\1\33\1\uffff\1\32\6\uffff\1\26\3\uffff\1\34\11\uffff" +
                    "\1\37\1\uffff\1\7\3\uffff\1\41\1\21\1\11\1\27\1\30\1\23\1\10\1\12\2\uffff" +
                    "\1\22\5\uffff\1\20\1\uffff\1\24\1\uffff\1\14\1\16\1\31\1\13\15\uffff" +
                    "\1\40\1\5\11\uffff\1\35\1\36\1\15",
            "\1\43\11\uffff\1\42",
            "\1\45\1\uffff\1\44",
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
            "\1\61\1\67\1\50\1\75\1\uffff\1\74\6\uffff\1\70\3\uffff\1\76\11\uffff" +
                    "\1\101\1\uffff\1\51\3\uffff\1\103\1\63\1\53\1\71\1\72\1\65\1\52\1\54" +
                    "\2\uffff\1\64\5\uffff\1\62\1\uffff\1\66\1\uffff\1\56\1\60\1\73\1\55\15" +
                    "\uffff\1\102\1\47\11\uffff\1\77\1\100\1\57",
            "",
            "",
            "\1\104\21\uffff\1\105",
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
            "\1\110\11\uffff\1\107",
            "\1\112\1\uffff\1\111",
            "\1\4",
            "\1\105",
            "\1\125\1\133\1\114\1\141\1\uffff\1\140\6\uffff\1\134\3\uffff\1\142\11" +
                    "\uffff\1\145\1\uffff\1\115\3\uffff\1\147\1\127\1\117\1\135\1\136\1\131" +
                    "\1\116\1\120\2\uffff\1\130\5\uffff\1\126\1\uffff\1\132\1\uffff\1\122" +
                    "\1\124\1\137\1\121\15\uffff\1\146\1\113\11\uffff\1\143\1\144\1\123",
            "",
            "",
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
            "\1\105"
    };

    static final short[] DFA34_eot = DFA.unpackEncodedString(DFA34_eotS);
    static final short[] DFA34_eof = DFA.unpackEncodedString(DFA34_eofS);
    static final char[] DFA34_min = DFA.unpackEncodedStringToUnsignedChars(DFA34_minS);
    static final char[] DFA34_max = DFA.unpackEncodedStringToUnsignedChars(DFA34_maxS);
    static final short[] DFA34_accept = DFA.unpackEncodedString(DFA34_acceptS);
    static final short[] DFA34_special = DFA.unpackEncodedString(DFA34_specialS);
    static final short[][] DFA34_transition;

    static
    {
        int numStates = DFA34_transitionS.length;
        DFA34_transition = new short[numStates][];
        for (int i = 0; i < numStates; i++)
        {
            DFA34_transition[i] = DFA.unpackEncodedString(DFA34_transitionS[i]);
        }
    }

    protected class DFA34 extends DFA
    {

        public DFA34(BaseRecognizer recognizer)
        {
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

        @Override
        public String getDescription()
        {
            return "633:1: nullPredicate : ( columnReference IS NULL -> ^( PRED_EXISTS columnReference NOT ) | columnReference IS NOT NULL -> ^( PRED_EXISTS columnReference ) );";
        }

        @Override
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException
        {
            TokenStream input = (TokenStream) _input;
            int _s = s;
            switch (s)
            {
            case 0:
                int LA34_83 = input.LA(1);

                int index34_83 = input.index();
                input.rewind();
                s = -1;
                if ((LA34_83 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 104;
                }

                input.seek(index34_83);
                if (s >= 0)
                    return s;
                break;

            case 1:
                int LA34_21 = input.LA(1);

                int index34_21 = input.index();
                input.rewind();
                s = -1;
                if ((LA34_21 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 38;
                }

                input.seek(index34_21);
                if (s >= 0)
                    return s;
                break;

            case 2:
                int LA34_82 = input.LA(1);

                int index34_82 = input.index();
                input.rewind();
                s = -1;
                if ((LA34_82 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 104;
                }

                input.seek(index34_82);
                if (s >= 0)
                    return s;
                break;

            case 3:
                int LA34_85 = input.LA(1);

                int index34_85 = input.index();
                input.rewind();
                s = -1;
                if ((LA34_85 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 104;
                }

                input.seek(index34_85);
                if (s >= 0)
                    return s;
                break;

            case 4:
                int LA34_84 = input.LA(1);

                int index34_84 = input.index();
                input.rewind();
                s = -1;
                if ((LA34_84 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 104;
                }

                input.seek(index34_84);
                if (s >= 0)
                    return s;
                break;

            case 5:
                int LA34_87 = input.LA(1);

                int index34_87 = input.index();
                input.rewind();
                s = -1;
                if ((LA34_87 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 104;
                }

                input.seek(index34_87);
                if (s >= 0)
                    return s;
                break;

            case 6:
                int LA34_86 = input.LA(1);

                int index34_86 = input.index();
                input.rewind();
                s = -1;
                if ((LA34_86 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 104;
                }

                input.seek(index34_86);
                if (s >= 0)
                    return s;
                break;

            case 7:
                int LA34_20 = input.LA(1);

                int index34_20 = input.index();
                input.rewind();
                s = -1;
                if ((LA34_20 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 38;
                }

                input.seek(index34_20);
                if (s >= 0)
                    return s;
                break;

            case 8:
                int LA34_89 = input.LA(1);

                int index34_89 = input.index();
                input.rewind();
                s = -1;
                if ((LA34_89 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 104;
                }

                input.seek(index34_89);
                if (s >= 0)
                    return s;
                break;

            case 9:
                int LA34_88 = input.LA(1);

                int index34_88 = input.index();
                input.rewind();
                s = -1;
                if ((LA34_88 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 104;
                }

                input.seek(index34_88);
                if (s >= 0)
                    return s;
                break;

            case 10:
                int LA34_75 = input.LA(1);

                int index34_75 = input.index();
                input.rewind();
                s = -1;
                if ((LA34_75 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 104;
                }

                input.seek(index34_75);
                if (s >= 0)
                    return s;
                break;

            case 11:
                int LA34_77 = input.LA(1);

                int index34_77 = input.index();
                input.rewind();
                s = -1;
                if ((LA34_77 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 104;
                }

                input.seek(index34_77);
                if (s >= 0)
                    return s;
                break;

            case 12:
                int LA34_24 = input.LA(1);

                int index34_24 = input.index();
                input.rewind();
                s = -1;
                if ((LA34_24 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 38;
                }

                input.seek(index34_24);
                if (s >= 0)
                    return s;
                break;

            case 13:
                int LA34_76 = input.LA(1);

                int index34_76 = input.index();
                input.rewind();
                s = -1;
                if ((LA34_76 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 104;
                }

                input.seek(index34_76);
                if (s >= 0)
                    return s;
                break;

            case 14:
                int LA34_68 = input.LA(1);

                int index34_68 = input.index();
                input.rewind();
                s = -1;
                if ((LA34_68 == ID) && ((strict == false)))
                {
                    s = 71;
                }
                else if ((LA34_68 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 72;
                }

                input.seek(index34_68);
                if (s >= 0)
                    return s;
                break;

            case 15:
                int LA34_79 = input.LA(1);

                int index34_79 = input.index();
                input.rewind();
                s = -1;
                if ((LA34_79 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 104;
                }

                input.seek(index34_79);
                if (s >= 0)
                    return s;
                break;

            case 16:
                int LA34_22 = input.LA(1);

                int index34_22 = input.index();
                input.rewind();
                s = -1;
                if ((LA34_22 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 38;
                }

                input.seek(index34_22);
                if (s >= 0)
                    return s;
                break;

            case 17:
                int LA34_78 = input.LA(1);

                int index34_78 = input.index();
                input.rewind();
                s = -1;
                if ((LA34_78 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 104;
                }

                input.seek(index34_78);
                if (s >= 0)
                    return s;
                break;

            case 18:
                int LA34_81 = input.LA(1);

                int index34_81 = input.index();
                input.rewind();
                s = -1;
                if ((LA34_81 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 104;
                }

                input.seek(index34_81);
                if (s >= 0)
                    return s;
                break;

            case 19:
                int LA34_80 = input.LA(1);

                int index34_80 = input.index();
                input.rewind();
                s = -1;
                if ((LA34_80 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 104;
                }

                input.seek(index34_80);
                if (s >= 0)
                    return s;
                break;

            case 20:
                int LA34_23 = input.LA(1);

                int index34_23 = input.index();
                input.rewind();
                s = -1;
                if ((LA34_23 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 38;
                }

                input.seek(index34_23);
                if (s >= 0)
                    return s;
                break;

            case 21:
                int LA34_72 = input.LA(1);

                int index34_72 = input.index();
                input.rewind();
                s = -1;
                if ((LA34_72 == SELECT) && ((strict == false)))
                {
                    s = 75;
                }
                else if ((LA34_72 == AS) && ((strict == false)))
                {
                    s = 76;
                }
                else if ((LA34_72 == FROM) && ((strict == false)))
                {
                    s = 77;
                }
                else if ((LA34_72 == JOIN) && ((strict == false)))
                {
                    s = 78;
                }
                else if ((LA34_72 == INNER) && ((strict == false)))
                {
                    s = 79;
                }
                else if ((LA34_72 == LEFT) && ((strict == false)))
                {
                    s = 80;
                }
                else if ((LA34_72 == OUTER) && ((strict == false)))
                {
                    s = 81;
                }
                else if ((LA34_72 == ON) && ((strict == false)))
                {
                    s = 82;
                }
                else if ((LA34_72 == WHERE) && ((strict == false)))
                {
                    s = 83;
                }
                else if ((LA34_72 == OR) && ((strict == false)))
                {
                    s = 84;
                }
                else if ((LA34_72 == AND) && ((strict == false)))
                {
                    s = 85;
                }
                else if ((LA34_72 == NOT) && ((strict == false)))
                {
                    s = 86;
                }
                else if ((LA34_72 == IN) && ((strict == false)))
                {
                    s = 87;
                }
                else if ((LA34_72 == LIKE) && ((strict == false)))
                {
                    s = 88;
                }
                else if ((LA34_72 == IS) && ((strict == false)))
                {
                    s = 89;
                }
                else if ((LA34_72 == NULL) && ((strict == false)))
                {
                    s = 90;
                }
                else if ((LA34_72 == ANY) && ((strict == false)))
                {
                    s = 91;
                }
                else if ((LA34_72 == CONTAINS) && ((strict == false)))
                {
                    s = 92;
                }
                else if ((LA34_72 == IN_FOLDER) && ((strict == false)))
                {
                    s = 93;
                }
                else if ((LA34_72 == IN_TREE) && ((strict == false)))
                {
                    s = 94;
                }
                else if ((LA34_72 == ORDER) && ((strict == false)))
                {
                    s = 95;
                }
                else if ((LA34_72 == BY) && ((strict == false)))
                {
                    s = 96;
                }
                else if ((LA34_72 == ASC) && ((strict == false)))
                {
                    s = 97;
                }
                else if ((LA34_72 == DESC) && ((strict == false)))
                {
                    s = 98;
                }
                else if ((LA34_72 == TIMESTAMP) && ((strict == false)))
                {
                    s = 99;
                }
                else if ((LA34_72 == TRUE) && ((strict == false)))
                {
                    s = 100;
                }
                else if ((LA34_72 == FALSE) && ((strict == false)))
                {
                    s = 101;
                }
                else if ((LA34_72 == SCORE) && ((strict == false)))
                {
                    s = 102;
                }
                else if ((LA34_72 == ID) && ((strict == false)))
                {
                    s = 103;
                }

                input.seek(index34_72);
                if (s >= 0)
                    return s;
                break;

            case 22:
                int LA34_6 = input.LA(1);

                int index34_6 = input.index();
                input.rewind();
                s = -1;
                if ((LA34_6 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 38;
                }

                input.seek(index34_6);
                if (s >= 0)
                    return s;
                break;

            case 23:
                int LA34_26 = input.LA(1);

                int index34_26 = input.index();
                input.rewind();
                s = -1;
                if ((LA34_26 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 38;
                }

                input.seek(index34_26);
                if (s >= 0)
                    return s;
                break;

            case 24:
                int LA34_27 = input.LA(1);

                int index34_27 = input.index();
                input.rewind();
                s = -1;
                if ((LA34_27 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 38;
                }

                input.seek(index34_27);
                if (s >= 0)
                    return s;
                break;

            case 25:
                int LA34_7 = input.LA(1);

                int index34_7 = input.index();
                input.rewind();
                s = -1;
                if ((LA34_7 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 38;
                }

                input.seek(index34_7);
                if (s >= 0)
                    return s;
                break;

            case 26:
                int LA34_25 = input.LA(1);

                int index34_25 = input.index();
                input.rewind();
                s = -1;
                if ((LA34_25 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 38;
                }

                input.seek(index34_25);
                if (s >= 0)
                    return s;
                break;

            case 27:
                int LA34_8 = input.LA(1);

                int index34_8 = input.index();
                input.rewind();
                s = -1;
                if ((LA34_8 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 38;
                }

                input.seek(index34_8);
                if (s >= 0)
                    return s;
                break;

            case 28:
                int LA34_29 = input.LA(1);

                int index34_29 = input.index();
                input.rewind();
                s = -1;
                if ((LA34_29 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 38;
                }

                input.seek(index34_29);
                if (s >= 0)
                    return s;
                break;

            case 29:
                int LA34_5 = input.LA(1);

                int index34_5 = input.index();
                input.rewind();
                s = -1;
                if ((LA34_5 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 38;
                }

                input.seek(index34_5);
                if (s >= 0)
                    return s;
                break;

            case 30:
                int LA34_28 = input.LA(1);

                int index34_28 = input.index();
                input.rewind();
                s = -1;
                if ((LA34_28 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 38;
                }

                input.seek(index34_28);
                if (s >= 0)
                    return s;
                break;

            case 31:
                int LA34_12 = input.LA(1);

                int index34_12 = input.index();
                input.rewind();
                s = -1;
                if ((LA34_12 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 38;
                }

                input.seek(index34_12);
                if (s >= 0)
                    return s;
                break;

            case 32:
                int LA34_38 = input.LA(1);

                int index34_38 = input.index();
                input.rewind();
                s = -1;
                if ((LA34_38 == DOT) && ((strict == false)))
                {
                    s = 68;
                }
                else if ((LA34_38 == IS) && ((strict == false)))
                {
                    s = 69;
                }

                input.seek(index34_38);
                if (s >= 0)
                    return s;
                break;

            case 33:
                int LA34_31 = input.LA(1);

                int index34_31 = input.index();
                input.rewind();
                s = -1;
                if ((LA34_31 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 38;
                }

                input.seek(index34_31);
                if (s >= 0)
                    return s;
                break;

            case 34:
                int LA34_13 = input.LA(1);

                int index34_13 = input.index();
                input.rewind();
                s = -1;
                if ((LA34_13 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 38;
                }

                input.seek(index34_13);
                if (s >= 0)
                    return s;
                break;

            case 35:
                int LA34_30 = input.LA(1);

                int index34_30 = input.index();
                input.rewind();
                s = -1;
                if ((LA34_30 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 38;
                }

                input.seek(index34_30);
                if (s >= 0)
                    return s;
                break;

            case 36:
                int LA34_9 = input.LA(1);

                int index34_9 = input.index();
                input.rewind();
                s = -1;
                if ((LA34_9 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 38;
                }

                input.seek(index34_9);
                if (s >= 0)
                    return s;
                break;

            case 37:
                int LA34_11 = input.LA(1);

                int index34_11 = input.index();
                input.rewind();
                s = -1;
                if ((LA34_11 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 38;
                }

                input.seek(index34_11);
                if (s >= 0)
                    return s;
                break;

            case 38:
                int LA34_0 = input.LA(1);

                int index34_0 = input.index();
                input.rewind();
                s = -1;
                if ((LA34_0 == ID))
                {
                    s = 1;
                }
                else if ((LA34_0 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 2;
                }

                input.seek(index34_0);
                if (s >= 0)
                    return s;
                break;

            case 39:
                int LA34_10 = input.LA(1);

                int index34_10 = input.index();
                input.rewind();
                s = -1;
                if ((LA34_10 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 38;
                }

                input.seek(index34_10);
                if (s >= 0)
                    return s;
                break;

            case 40:
                int LA34_2 = input.LA(1);

                int index34_2 = input.index();
                input.rewind();
                s = -1;
                if ((LA34_2 == SELECT) && ((strict == false)))
                {
                    s = 5;
                }
                else if ((LA34_2 == AS) && ((strict == false)))
                {
                    s = 6;
                }
                else if ((LA34_2 == FROM) && ((strict == false)))
                {
                    s = 7;
                }
                else if ((LA34_2 == JOIN) && ((strict == false)))
                {
                    s = 8;
                }
                else if ((LA34_2 == INNER) && ((strict == false)))
                {
                    s = 9;
                }
                else if ((LA34_2 == LEFT) && ((strict == false)))
                {
                    s = 10;
                }
                else if ((LA34_2 == OUTER) && ((strict == false)))
                {
                    s = 11;
                }
                else if ((LA34_2 == ON) && ((strict == false)))
                {
                    s = 12;
                }
                else if ((LA34_2 == WHERE) && ((strict == false)))
                {
                    s = 13;
                }
                else if ((LA34_2 == OR) && ((strict == false)))
                {
                    s = 14;
                }
                else if ((LA34_2 == AND) && ((strict == false)))
                {
                    s = 15;
                }
                else if ((LA34_2 == NOT) && ((strict == false)))
                {
                    s = 16;
                }
                else if ((LA34_2 == IN) && ((strict == false)))
                {
                    s = 17;
                }
                else if ((LA34_2 == LIKE) && ((strict == false)))
                {
                    s = 18;
                }
                else if ((LA34_2 == IS) && ((strict == false)))
                {
                    s = 19;
                }
                else if ((LA34_2 == NULL) && ((strict == false)))
                {
                    s = 20;
                }
                else if ((LA34_2 == ANY) && ((strict == false)))
                {
                    s = 21;
                }
                else if ((LA34_2 == CONTAINS) && ((strict == false)))
                {
                    s = 22;
                }
                else if ((LA34_2 == IN_FOLDER) && ((strict == false)))
                {
                    s = 23;
                }
                else if ((LA34_2 == IN_TREE) && ((strict == false)))
                {
                    s = 24;
                }
                else if ((LA34_2 == ORDER) && ((strict == false)))
                {
                    s = 25;
                }
                else if ((LA34_2 == BY) && ((strict == false)))
                {
                    s = 26;
                }
                else if ((LA34_2 == ASC) && ((strict == false)))
                {
                    s = 27;
                }
                else if ((LA34_2 == DESC) && ((strict == false)))
                {
                    s = 28;
                }
                else if ((LA34_2 == TIMESTAMP) && ((strict == false)))
                {
                    s = 29;
                }
                else if ((LA34_2 == TRUE) && ((strict == false)))
                {
                    s = 30;
                }
                else if ((LA34_2 == FALSE) && ((strict == false)))
                {
                    s = 31;
                }
                else if ((LA34_2 == SCORE) && ((strict == false)))
                {
                    s = 32;
                }
                else if ((LA34_2 == ID) && ((strict == false)))
                {
                    s = 33;
                }

                input.seek(index34_2);
                if (s >= 0)
                    return s;
                break;

            case 41:
                int LA34_97 = input.LA(1);

                int index34_97 = input.index();
                input.rewind();
                s = -1;
                if ((LA34_97 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 104;
                }

                input.seek(index34_97);
                if (s >= 0)
                    return s;
                break;

            case 42:
                int LA34_32 = input.LA(1);

                int index34_32 = input.index();
                input.rewind();
                s = -1;
                if ((LA34_32 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 38;
                }

                input.seek(index34_32);
                if (s >= 0)
                    return s;
                break;

            case 43:
                int LA34_104 = input.LA(1);

                int index34_104 = input.index();
                input.rewind();
                s = -1;
                if ((LA34_104 == IS) && ((strict == false)))
                {
                    s = 69;
                }

                input.seek(index34_104);
                if (s >= 0)
                    return s;
                break;

            case 44:
                int LA34_96 = input.LA(1);

                int index34_96 = input.index();
                input.rewind();
                s = -1;
                if ((LA34_96 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 104;
                }

                input.seek(index34_96);
                if (s >= 0)
                    return s;
                break;

            case 45:
                int LA34_95 = input.LA(1);

                int index34_95 = input.index();
                input.rewind();
                s = -1;
                if ((LA34_95 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 104;
                }

                input.seek(index34_95);
                if (s >= 0)
                    return s;
                break;

            case 46:
                int LA34_103 = input.LA(1);

                int index34_103 = input.index();
                input.rewind();
                s = -1;
                if ((LA34_103 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 104;
                }

                input.seek(index34_103);
                if (s >= 0)
                    return s;
                break;

            case 47:
                int LA34_17 = input.LA(1);

                int index34_17 = input.index();
                input.rewind();
                s = -1;
                if ((LA34_17 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 38;
                }

                input.seek(index34_17);
                if (s >= 0)
                    return s;
                break;

            case 48:
                int LA34_94 = input.LA(1);

                int index34_94 = input.index();
                input.rewind();
                s = -1;
                if ((LA34_94 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 104;
                }

                input.seek(index34_94);
                if (s >= 0)
                    return s;
                break;

            case 49:
                int LA34_93 = input.LA(1);

                int index34_93 = input.index();
                input.rewind();
                s = -1;
                if ((LA34_93 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 104;
                }

                input.seek(index34_93);
                if (s >= 0)
                    return s;
                break;

            case 50:
                int LA34_71 = input.LA(1);

                int index34_71 = input.index();
                input.rewind();
                s = -1;
                if ((LA34_71 == IS) && ((strict == false)))
                {
                    s = 69;
                }

                input.seek(index34_71);
                if (s >= 0)
                    return s;
                break;

            case 51:
                int LA34_19 = input.LA(1);

                int index34_19 = input.index();
                input.rewind();
                s = -1;
                if ((LA34_19 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 38;
                }

                input.seek(index34_19);
                if (s >= 0)
                    return s;
                break;

            case 52:
                int LA34_92 = input.LA(1);

                int index34_92 = input.index();
                input.rewind();
                s = -1;
                if ((LA34_92 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 104;
                }

                input.seek(index34_92);
                if (s >= 0)
                    return s;
                break;

            case 53:
                int LA34_18 = input.LA(1);

                int index34_18 = input.index();
                input.rewind();
                s = -1;
                if ((LA34_18 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 38;
                }

                input.seek(index34_18);
                if (s >= 0)
                    return s;
                break;

            case 54:
                int LA34_69 = input.LA(1);

                int index34_69 = input.index();
                input.rewind();
                s = -1;
                if ((LA34_69 == NULL) && ((strict == false)))
                {
                    s = 73;
                }
                else if ((LA34_69 == NOT) && ((strict == false)))
                {
                    s = 74;
                }

                input.seek(index34_69);
                if (s >= 0)
                    return s;
                break;

            case 55:
                int LA34_91 = input.LA(1);

                int index34_91 = input.index();
                input.rewind();
                s = -1;
                if ((LA34_91 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 104;
                }

                input.seek(index34_91);
                if (s >= 0)
                    return s;
                break;

            case 56:
                int LA34_90 = input.LA(1);

                int index34_90 = input.index();
                input.rewind();
                s = -1;
                if ((LA34_90 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 104;
                }

                input.seek(index34_90);
                if (s >= 0)
                    return s;
                break;

            case 57:
                int LA34_33 = input.LA(1);

                int index34_33 = input.index();
                input.rewind();
                s = -1;
                if ((LA34_33 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 38;
                }

                input.seek(index34_33);
                if (s >= 0)
                    return s;
                break;

            case 58:
                int LA34_15 = input.LA(1);

                int index34_15 = input.index();
                input.rewind();
                s = -1;
                if ((LA34_15 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 38;
                }

                input.seek(index34_15);
                if (s >= 0)
                    return s;
                break;

            case 59:
                int LA34_14 = input.LA(1);

                int index34_14 = input.index();
                input.rewind();
                s = -1;
                if ((LA34_14 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 38;
                }

                input.seek(index34_14);
                if (s >= 0)
                    return s;
                break;

            case 60:
                int LA34_101 = input.LA(1);

                int index34_101 = input.index();
                input.rewind();
                s = -1;
                if ((LA34_101 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 104;
                }

                input.seek(index34_101);
                if (s >= 0)
                    return s;
                break;

            case 61:
                int LA34_16 = input.LA(1);

                int index34_16 = input.index();
                input.rewind();
                s = -1;
                if ((LA34_16 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 38;
                }

                input.seek(index34_16);
                if (s >= 0)
                    return s;
                break;

            case 62:
                int LA34_100 = input.LA(1);

                int index34_100 = input.index();
                input.rewind();
                s = -1;
                if ((LA34_100 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 104;
                }

                input.seek(index34_100);
                if (s >= 0)
                    return s;
                break;

            case 63:
                int LA34_99 = input.LA(1);

                int index34_99 = input.index();
                input.rewind();
                s = -1;
                if ((LA34_99 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 104;
                }

                input.seek(index34_99);
                if (s >= 0)
                    return s;
                break;

            case 64:
                int LA34_102 = input.LA(1);

                int index34_102 = input.index();
                input.rewind();
                s = -1;
                if ((LA34_102 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 104;
                }

                input.seek(index34_102);
                if (s >= 0)
                    return s;
                break;

            case 65:
                int LA34_98 = input.LA(1);

                int index34_98 = input.index();
                input.rewind();
                s = -1;
                if ((LA34_98 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 104;
                }

                input.seek(index34_98);
                if (s >= 0)
                    return s;
                break;
            }
            if (state.backtracking > 0)
            {
                state.failed = true;
                return -1;
            }
            NoViableAltException nvae = new NoViableAltException(getDescription(), 34, _s, input);
            error(nvae);
            throw nvae;
        }
    }

    static final String DFA41_eotS = "\153\uffff";
    static final String DFA41_eofS = "\1\uffff\1\4\41\uffff\1\4\1\uffff\1\104\40\uffff\1\111\1\113\42\uffff" +
            "\1\113";
    static final String DFA41_minS = "\1\33\1\10\1\5\1\33\2\uffff\35\33\1\10\1\5\1\10\36\33\2\uffff\2\10\1\5" +
            "\4\uffff\35\33\1\10";
    static final String DFA41_maxS = "\1\45\1\30\1\127\1\45\2\uffff\35\33\1\25\1\127\1\30\35\33\1\45\2\uffff" +
            "\2\25\1\127\4\uffff\35\33\1\25";
    static final String DFA41_acceptS = "\4\uffff\1\1\1\2\76\uffff\1\1\1\2\3\uffff\1\1\1\2\1\1\1\2\36\uffff";
    static final String DFA41_specialS = "\1\43\1\uffff\1\52\3\uffff\1\36\1\26\1\32\1\34\1\47\1\51\1\50\1\42\1\45" +
            "\1\72\1\71\1\74\1\60\1\65\1\63\1\7\1\1\1\20\1\24\1\14\1\33\1\27\1\31\1" +
            "\40\1\35\1\46\1\44\1\54\1\70\2\uffff\1\30\35\uffff\1\16\3\uffff\1\37\1" +
            "\25\4\uffff\1\12\1\15\1\13\1\21\1\17\1\23\1\22\1\2\1\0\1\4\1\3\1\6\1\5" +
            "\1\11\1\10\1\67\1\66\1\64\1\62\1\61\1\56\1\55\1\53\1\100\1\76\1\75\1\73" +
            "\1\77\1\57\1\41}>";
    static final String[] DFA41_transitionS = {
            "\1\2\11\uffff\1\1",
            "\1\5\6\uffff\1\4\5\uffff\1\5\2\uffff\1\3",
            "\1\20\1\26\1\7\1\34\1\uffff\1\33\6\uffff\1\27\3\uffff\1\35\11\uffff" +
                    "\1\40\1\uffff\1\10\3\uffff\1\42\1\22\1\12\1\30\1\31\1\24\1\11\1\13\2" +
                    "\uffff\1\23\5\uffff\1\21\1\uffff\1\25\1\uffff\1\15\1\17\1\32\1\14\15" +
                    "\uffff\1\41\1\6\11\uffff\1\36\1\37\1\16",
            "\1\44\11\uffff\1\43",
            "",
            "",
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
            "\1\5\6\uffff\1\4\5\uffff\1\5",
            "\1\60\1\66\1\47\1\74\1\uffff\1\73\6\uffff\1\67\3\uffff\1\75\11\uffff" +
                    "\1\100\1\uffff\1\50\3\uffff\1\102\1\62\1\52\1\70\1\71\1\64\1\51\1\53" +
                    "\2\uffff\1\63\5\uffff\1\61\1\uffff\1\65\1\uffff\1\55\1\57\1\72\1\54\15" +
                    "\uffff\1\101\1\46\11\uffff\1\76\1\77\1\56",
            "\1\105\6\uffff\1\104\5\uffff\1\105\2\uffff\1\103",
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
            "\1\110\11\uffff\1\107",
            "",
            "",
            "\1\5\6\uffff\1\4\5\uffff\1\112",
            "\1\105\6\uffff\1\104\5\uffff\1\114",
            "\1\127\1\135\1\116\1\143\1\uffff\1\142\6\uffff\1\136\3\uffff\1\144\11" +
                    "\uffff\1\147\1\uffff\1\117\3\uffff\1\151\1\131\1\121\1\137\1\140\1\133" +
                    "\1\120\1\122\2\uffff\1\132\5\uffff\1\130\1\uffff\1\134\1\uffff\1\124" +
                    "\1\126\1\141\1\123\15\uffff\1\150\1\115\11\uffff\1\145\1\146\1\125",
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
            "\1\105\6\uffff\1\104\5\uffff\1\114"
    };

    static final short[] DFA41_eot = DFA.unpackEncodedString(DFA41_eotS);
    static final short[] DFA41_eof = DFA.unpackEncodedString(DFA41_eofS);
    static final char[] DFA41_min = DFA.unpackEncodedStringToUnsignedChars(DFA41_minS);
    static final char[] DFA41_max = DFA.unpackEncodedStringToUnsignedChars(DFA41_maxS);
    static final short[] DFA41_accept = DFA.unpackEncodedString(DFA41_acceptS);
    static final short[] DFA41_special = DFA.unpackEncodedString(DFA41_specialS);
    static final short[][] DFA41_transition;

    static
    {
        int numStates = DFA41_transitionS.length;
        DFA41_transition = new short[numStates][];
        for (int i = 0; i < numStates; i++)
        {
            DFA41_transition[i] = DFA.unpackEncodedString(DFA41_transitionS[i]);
        }
    }

    protected class DFA41 extends DFA
    {

        public DFA41(BaseRecognizer recognizer)
        {
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

        @Override
        public String getDescription()
        {
            return "695:1: sortSpecification : ( columnReference -> ^( SORT_SPECIFICATION columnReference ASC ) | columnReference (by= ASC |by= DESC ) -> ^( SORT_SPECIFICATION columnReference $by) );";
        }

        @Override
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException
        {
            TokenStream input = (TokenStream) _input;
            int _s = s;
            switch (s)
            {
            case 0:
                int LA41_85 = input.LA(1);

                int index41_85 = input.index();
                input.rewind();
                s = -1;
                if ((LA41_85 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 106;
                }

                input.seek(index41_85);
                if (s >= 0)
                    return s;
                break;

            case 1:
                int LA41_22 = input.LA(1);

                int index41_22 = input.index();
                input.rewind();
                s = -1;
                if ((LA41_22 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 37;
                }

                input.seek(index41_22);
                if (s >= 0)
                    return s;
                break;

            case 2:
                int LA41_84 = input.LA(1);

                int index41_84 = input.index();
                input.rewind();
                s = -1;
                if ((LA41_84 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 106;
                }

                input.seek(index41_84);
                if (s >= 0)
                    return s;
                break;

            case 3:
                int LA41_87 = input.LA(1);

                int index41_87 = input.index();
                input.rewind();
                s = -1;
                if ((LA41_87 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 106;
                }

                input.seek(index41_87);
                if (s >= 0)
                    return s;
                break;

            case 4:
                int LA41_86 = input.LA(1);

                int index41_86 = input.index();
                input.rewind();
                s = -1;
                if ((LA41_86 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 106;
                }

                input.seek(index41_86);
                if (s >= 0)
                    return s;
                break;

            case 5:
                int LA41_89 = input.LA(1);

                int index41_89 = input.index();
                input.rewind();
                s = -1;
                if ((LA41_89 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 106;
                }

                input.seek(index41_89);
                if (s >= 0)
                    return s;
                break;

            case 6:
                int LA41_88 = input.LA(1);

                int index41_88 = input.index();
                input.rewind();
                s = -1;
                if ((LA41_88 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 106;
                }

                input.seek(index41_88);
                if (s >= 0)
                    return s;
                break;

            case 7:
                int LA41_21 = input.LA(1);

                int index41_21 = input.index();
                input.rewind();
                s = -1;
                if ((LA41_21 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 37;
                }

                input.seek(index41_21);
                if (s >= 0)
                    return s;
                break;

            case 8:
                int LA41_91 = input.LA(1);

                int index41_91 = input.index();
                input.rewind();
                s = -1;
                if ((LA41_91 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 106;
                }

                input.seek(index41_91);
                if (s >= 0)
                    return s;
                break;

            case 9:
                int LA41_90 = input.LA(1);

                int index41_90 = input.index();
                input.rewind();
                s = -1;
                if ((LA41_90 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 106;
                }

                input.seek(index41_90);
                if (s >= 0)
                    return s;
                break;

            case 10:
                int LA41_77 = input.LA(1);

                int index41_77 = input.index();
                input.rewind();
                s = -1;
                if ((LA41_77 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 106;
                }

                input.seek(index41_77);
                if (s >= 0)
                    return s;
                break;

            case 11:
                int LA41_79 = input.LA(1);

                int index41_79 = input.index();
                input.rewind();
                s = -1;
                if ((LA41_79 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 106;
                }

                input.seek(index41_79);
                if (s >= 0)
                    return s;
                break;

            case 12:
                int LA41_25 = input.LA(1);

                int index41_25 = input.index();
                input.rewind();
                s = -1;
                if ((LA41_25 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 37;
                }

                input.seek(index41_25);
                if (s >= 0)
                    return s;
                break;

            case 13:
                int LA41_78 = input.LA(1);

                int index41_78 = input.index();
                input.rewind();
                s = -1;
                if ((LA41_78 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 106;
                }

                input.seek(index41_78);
                if (s >= 0)
                    return s;
                break;

            case 14:
                int LA41_67 = input.LA(1);

                int index41_67 = input.index();
                input.rewind();
                s = -1;
                if ((LA41_67 == ID) && ((strict == false)))
                {
                    s = 71;
                }
                else if ((LA41_67 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 72;
                }

                input.seek(index41_67);
                if (s >= 0)
                    return s;
                break;

            case 15:
                int LA41_81 = input.LA(1);

                int index41_81 = input.index();
                input.rewind();
                s = -1;
                if ((LA41_81 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 106;
                }

                input.seek(index41_81);
                if (s >= 0)
                    return s;
                break;

            case 16:
                int LA41_23 = input.LA(1);

                int index41_23 = input.index();
                input.rewind();
                s = -1;
                if ((LA41_23 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 37;
                }

                input.seek(index41_23);
                if (s >= 0)
                    return s;
                break;

            case 17:
                int LA41_80 = input.LA(1);

                int index41_80 = input.index();
                input.rewind();
                s = -1;
                if ((LA41_80 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 106;
                }

                input.seek(index41_80);
                if (s >= 0)
                    return s;
                break;

            case 18:
                int LA41_83 = input.LA(1);

                int index41_83 = input.index();
                input.rewind();
                s = -1;
                if ((LA41_83 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 106;
                }

                input.seek(index41_83);
                if (s >= 0)
                    return s;
                break;

            case 19:
                int LA41_82 = input.LA(1);

                int index41_82 = input.index();
                input.rewind();
                s = -1;
                if ((LA41_82 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 106;
                }

                input.seek(index41_82);
                if (s >= 0)
                    return s;
                break;

            case 20:
                int LA41_24 = input.LA(1);

                int index41_24 = input.index();
                input.rewind();
                s = -1;
                if ((LA41_24 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 37;
                }

                input.seek(index41_24);
                if (s >= 0)
                    return s;
                break;

            case 21:
                int LA41_72 = input.LA(1);

                int index41_72 = input.index();
                input.rewind();
                s = -1;
                if ((LA41_72 == SELECT) && ((strict == false)))
                {
                    s = 77;
                }
                else if ((LA41_72 == AS) && ((strict == false)))
                {
                    s = 78;
                }
                else if ((LA41_72 == FROM) && ((strict == false)))
                {
                    s = 79;
                }
                else if ((LA41_72 == JOIN) && ((strict == false)))
                {
                    s = 80;
                }
                else if ((LA41_72 == INNER) && ((strict == false)))
                {
                    s = 81;
                }
                else if ((LA41_72 == LEFT) && ((strict == false)))
                {
                    s = 82;
                }
                else if ((LA41_72 == OUTER) && ((strict == false)))
                {
                    s = 83;
                }
                else if ((LA41_72 == ON) && ((strict == false)))
                {
                    s = 84;
                }
                else if ((LA41_72 == WHERE) && ((strict == false)))
                {
                    s = 85;
                }
                else if ((LA41_72 == OR) && ((strict == false)))
                {
                    s = 86;
                }
                else if ((LA41_72 == AND) && ((strict == false)))
                {
                    s = 87;
                }
                else if ((LA41_72 == NOT) && ((strict == false)))
                {
                    s = 88;
                }
                else if ((LA41_72 == IN) && ((strict == false)))
                {
                    s = 89;
                }
                else if ((LA41_72 == LIKE) && ((strict == false)))
                {
                    s = 90;
                }
                else if ((LA41_72 == IS) && ((strict == false)))
                {
                    s = 91;
                }
                else if ((LA41_72 == NULL) && ((strict == false)))
                {
                    s = 92;
                }
                else if ((LA41_72 == ANY) && ((strict == false)))
                {
                    s = 93;
                }
                else if ((LA41_72 == CONTAINS) && ((strict == false)))
                {
                    s = 94;
                }
                else if ((LA41_72 == IN_FOLDER) && ((strict == false)))
                {
                    s = 95;
                }
                else if ((LA41_72 == IN_TREE) && ((strict == false)))
                {
                    s = 96;
                }
                else if ((LA41_72 == ORDER) && ((strict == false)))
                {
                    s = 97;
                }
                else if ((LA41_72 == BY) && ((strict == false)))
                {
                    s = 98;
                }
                else if ((LA41_72 == ASC) && ((strict == false)))
                {
                    s = 99;
                }
                else if ((LA41_72 == DESC) && ((strict == false)))
                {
                    s = 100;
                }
                else if ((LA41_72 == TIMESTAMP) && ((strict == false)))
                {
                    s = 101;
                }
                else if ((LA41_72 == TRUE) && ((strict == false)))
                {
                    s = 102;
                }
                else if ((LA41_72 == FALSE) && ((strict == false)))
                {
                    s = 103;
                }
                else if ((LA41_72 == SCORE) && ((strict == false)))
                {
                    s = 104;
                }
                else if ((LA41_72 == ID) && ((strict == false)))
                {
                    s = 105;
                }

                input.seek(index41_72);
                if (s >= 0)
                    return s;
                break;

            case 22:
                int LA41_7 = input.LA(1);

                int index41_7 = input.index();
                input.rewind();
                s = -1;
                if ((LA41_7 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 37;
                }

                input.seek(index41_7);
                if (s >= 0)
                    return s;
                break;

            case 23:
                int LA41_27 = input.LA(1);

                int index41_27 = input.index();
                input.rewind();
                s = -1;
                if ((LA41_27 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 37;
                }

                input.seek(index41_27);
                if (s >= 0)
                    return s;
                break;

            case 24:
                int LA41_37 = input.LA(1);

                int index41_37 = input.index();
                input.rewind();
                s = -1;
                if ((LA41_37 == DOT) && ((strict == false)))
                {
                    s = 67;
                }
                else if ((LA41_37 == EOF || LA41_37 == COMMA) && ((strict == false)))
                {
                    s = 68;
                }
                else if ((LA41_37 == ASC || LA41_37 == DESC) && ((strict == false)))
                {
                    s = 69;
                }

                input.seek(index41_37);
                if (s >= 0)
                    return s;
                break;

            case 25:
                int LA41_28 = input.LA(1);

                int index41_28 = input.index();
                input.rewind();
                s = -1;
                if ((LA41_28 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 37;
                }

                input.seek(index41_28);
                if (s >= 0)
                    return s;
                break;

            case 26:
                int LA41_8 = input.LA(1);

                int index41_8 = input.index();
                input.rewind();
                s = -1;
                if ((LA41_8 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 37;
                }

                input.seek(index41_8);
                if (s >= 0)
                    return s;
                break;

            case 27:
                int LA41_26 = input.LA(1);

                int index41_26 = input.index();
                input.rewind();
                s = -1;
                if ((LA41_26 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 37;
                }

                input.seek(index41_26);
                if (s >= 0)
                    return s;
                break;

            case 28:
                int LA41_9 = input.LA(1);

                int index41_9 = input.index();
                input.rewind();
                s = -1;
                if ((LA41_9 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 37;
                }

                input.seek(index41_9);
                if (s >= 0)
                    return s;
                break;

            case 29:
                int LA41_30 = input.LA(1);

                int index41_30 = input.index();
                input.rewind();
                s = -1;
                if ((LA41_30 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 37;
                }

                input.seek(index41_30);
                if (s >= 0)
                    return s;
                break;

            case 30:
                int LA41_6 = input.LA(1);

                int index41_6 = input.index();
                input.rewind();
                s = -1;
                if ((LA41_6 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 37;
                }

                input.seek(index41_6);
                if (s >= 0)
                    return s;
                break;

            case 31:
                int LA41_71 = input.LA(1);

                int index41_71 = input.index();
                input.rewind();
                s = -1;
                if ((LA41_71 == COMMA) && ((strict == false)))
                {
                    s = 68;
                }
                else if ((LA41_71 == EOF) && ((strict == false)))
                {
                    s = 75;
                }
                else if ((LA41_71 == ASC) && ((strict == false)))
                {
                    s = 69;
                }
                else if ((LA41_71 == DESC) && ((strict == false)))
                {
                    s = 76;
                }

                input.seek(index41_71);
                if (s >= 0)
                    return s;
                break;

            case 32:
                int LA41_29 = input.LA(1);

                int index41_29 = input.index();
                input.rewind();
                s = -1;
                if ((LA41_29 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 37;
                }

                input.seek(index41_29);
                if (s >= 0)
                    return s;
                break;

            case 33:
                int LA41_106 = input.LA(1);

                int index41_106 = input.index();
                input.rewind();
                s = -1;
                if ((LA41_106 == COMMA) && ((strict == false)))
                {
                    s = 68;
                }
                else if ((LA41_106 == EOF) && ((strict == false)))
                {
                    s = 75;
                }
                else if ((LA41_106 == ASC) && ((strict == false)))
                {
                    s = 69;
                }
                else if ((LA41_106 == DESC) && ((strict == false)))
                {
                    s = 76;
                }

                input.seek(index41_106);
                if (s >= 0)
                    return s;
                break;

            case 34:
                int LA41_13 = input.LA(1);

                int index41_13 = input.index();
                input.rewind();
                s = -1;
                if ((LA41_13 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 37;
                }

                input.seek(index41_13);
                if (s >= 0)
                    return s;
                break;

            case 35:
                int LA41_0 = input.LA(1);

                int index41_0 = input.index();
                input.rewind();
                s = -1;
                if ((LA41_0 == ID))
                {
                    s = 1;
                }
                else if ((LA41_0 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 2;
                }

                input.seek(index41_0);
                if (s >= 0)
                    return s;
                break;

            case 36:
                int LA41_32 = input.LA(1);

                int index41_32 = input.index();
                input.rewind();
                s = -1;
                if ((LA41_32 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 37;
                }

                input.seek(index41_32);
                if (s >= 0)
                    return s;
                break;

            case 37:
                int LA41_14 = input.LA(1);

                int index41_14 = input.index();
                input.rewind();
                s = -1;
                if ((LA41_14 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 37;
                }

                input.seek(index41_14);
                if (s >= 0)
                    return s;
                break;

            case 38:
                int LA41_31 = input.LA(1);

                int index41_31 = input.index();
                input.rewind();
                s = -1;
                if ((LA41_31 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 37;
                }

                input.seek(index41_31);
                if (s >= 0)
                    return s;
                break;

            case 39:
                int LA41_10 = input.LA(1);

                int index41_10 = input.index();
                input.rewind();
                s = -1;
                if ((LA41_10 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 37;
                }

                input.seek(index41_10);
                if (s >= 0)
                    return s;
                break;

            case 40:
                int LA41_12 = input.LA(1);

                int index41_12 = input.index();
                input.rewind();
                s = -1;
                if ((LA41_12 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 37;
                }

                input.seek(index41_12);
                if (s >= 0)
                    return s;
                break;

            case 41:
                int LA41_11 = input.LA(1);

                int index41_11 = input.index();
                input.rewind();
                s = -1;
                if ((LA41_11 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 37;
                }

                input.seek(index41_11);
                if (s >= 0)
                    return s;
                break;

            case 42:
                int LA41_2 = input.LA(1);

                int index41_2 = input.index();
                input.rewind();
                s = -1;
                if ((LA41_2 == SELECT) && ((strict == false)))
                {
                    s = 6;
                }
                else if ((LA41_2 == AS) && ((strict == false)))
                {
                    s = 7;
                }
                else if ((LA41_2 == FROM) && ((strict == false)))
                {
                    s = 8;
                }
                else if ((LA41_2 == JOIN) && ((strict == false)))
                {
                    s = 9;
                }
                else if ((LA41_2 == INNER) && ((strict == false)))
                {
                    s = 10;
                }
                else if ((LA41_2 == LEFT) && ((strict == false)))
                {
                    s = 11;
                }
                else if ((LA41_2 == OUTER) && ((strict == false)))
                {
                    s = 12;
                }
                else if ((LA41_2 == ON) && ((strict == false)))
                {
                    s = 13;
                }
                else if ((LA41_2 == WHERE) && ((strict == false)))
                {
                    s = 14;
                }
                else if ((LA41_2 == OR) && ((strict == false)))
                {
                    s = 15;
                }
                else if ((LA41_2 == AND) && ((strict == false)))
                {
                    s = 16;
                }
                else if ((LA41_2 == NOT) && ((strict == false)))
                {
                    s = 17;
                }
                else if ((LA41_2 == IN) && ((strict == false)))
                {
                    s = 18;
                }
                else if ((LA41_2 == LIKE) && ((strict == false)))
                {
                    s = 19;
                }
                else if ((LA41_2 == IS) && ((strict == false)))
                {
                    s = 20;
                }
                else if ((LA41_2 == NULL) && ((strict == false)))
                {
                    s = 21;
                }
                else if ((LA41_2 == ANY) && ((strict == false)))
                {
                    s = 22;
                }
                else if ((LA41_2 == CONTAINS) && ((strict == false)))
                {
                    s = 23;
                }
                else if ((LA41_2 == IN_FOLDER) && ((strict == false)))
                {
                    s = 24;
                }
                else if ((LA41_2 == IN_TREE) && ((strict == false)))
                {
                    s = 25;
                }
                else if ((LA41_2 == ORDER) && ((strict == false)))
                {
                    s = 26;
                }
                else if ((LA41_2 == BY) && ((strict == false)))
                {
                    s = 27;
                }
                else if ((LA41_2 == ASC) && ((strict == false)))
                {
                    s = 28;
                }
                else if ((LA41_2 == DESC) && ((strict == false)))
                {
                    s = 29;
                }
                else if ((LA41_2 == TIMESTAMP) && ((strict == false)))
                {
                    s = 30;
                }
                else if ((LA41_2 == TRUE) && ((strict == false)))
                {
                    s = 31;
                }
                else if ((LA41_2 == FALSE) && ((strict == false)))
                {
                    s = 32;
                }
                else if ((LA41_2 == SCORE) && ((strict == false)))
                {
                    s = 33;
                }
                else if ((LA41_2 == ID) && ((strict == false)))
                {
                    s = 34;
                }

                input.seek(index41_2);
                if (s >= 0)
                    return s;
                break;

            case 43:
                int LA41_99 = input.LA(1);

                int index41_99 = input.index();
                input.rewind();
                s = -1;
                if ((LA41_99 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 106;
                }

                input.seek(index41_99);
                if (s >= 0)
                    return s;
                break;

            case 44:
                int LA41_33 = input.LA(1);

                int index41_33 = input.index();
                input.rewind();
                s = -1;
                if ((LA41_33 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 37;
                }

                input.seek(index41_33);
                if (s >= 0)
                    return s;
                break;

            case 45:
                int LA41_98 = input.LA(1);

                int index41_98 = input.index();
                input.rewind();
                s = -1;
                if ((LA41_98 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 106;
                }

                input.seek(index41_98);
                if (s >= 0)
                    return s;
                break;

            case 46:
                int LA41_97 = input.LA(1);

                int index41_97 = input.index();
                input.rewind();
                s = -1;
                if ((LA41_97 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 106;
                }

                input.seek(index41_97);
                if (s >= 0)
                    return s;
                break;

            case 47:
                int LA41_105 = input.LA(1);

                int index41_105 = input.index();
                input.rewind();
                s = -1;
                if ((LA41_105 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 106;
                }

                input.seek(index41_105);
                if (s >= 0)
                    return s;
                break;

            case 48:
                int LA41_18 = input.LA(1);

                int index41_18 = input.index();
                input.rewind();
                s = -1;
                if ((LA41_18 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 37;
                }

                input.seek(index41_18);
                if (s >= 0)
                    return s;
                break;

            case 49:
                int LA41_96 = input.LA(1);

                int index41_96 = input.index();
                input.rewind();
                s = -1;
                if ((LA41_96 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 106;
                }

                input.seek(index41_96);
                if (s >= 0)
                    return s;
                break;

            case 50:
                int LA41_95 = input.LA(1);

                int index41_95 = input.index();
                input.rewind();
                s = -1;
                if ((LA41_95 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 106;
                }

                input.seek(index41_95);
                if (s >= 0)
                    return s;
                break;

            case 51:
                int LA41_20 = input.LA(1);

                int index41_20 = input.index();
                input.rewind();
                s = -1;
                if ((LA41_20 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 37;
                }

                input.seek(index41_20);
                if (s >= 0)
                    return s;
                break;

            case 52:
                int LA41_94 = input.LA(1);

                int index41_94 = input.index();
                input.rewind();
                s = -1;
                if ((LA41_94 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 106;
                }

                input.seek(index41_94);
                if (s >= 0)
                    return s;
                break;

            case 53:
                int LA41_19 = input.LA(1);

                int index41_19 = input.index();
                input.rewind();
                s = -1;
                if ((LA41_19 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 37;
                }

                input.seek(index41_19);
                if (s >= 0)
                    return s;
                break;

            case 54:
                int LA41_93 = input.LA(1);

                int index41_93 = input.index();
                input.rewind();
                s = -1;
                if ((LA41_93 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 106;
                }

                input.seek(index41_93);
                if (s >= 0)
                    return s;
                break;

            case 55:
                int LA41_92 = input.LA(1);

                int index41_92 = input.index();
                input.rewind();
                s = -1;
                if ((LA41_92 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 106;
                }

                input.seek(index41_92);
                if (s >= 0)
                    return s;
                break;

            case 56:
                int LA41_34 = input.LA(1);

                int index41_34 = input.index();
                input.rewind();
                s = -1;
                if ((LA41_34 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 37;
                }

                input.seek(index41_34);
                if (s >= 0)
                    return s;
                break;

            case 57:
                int LA41_16 = input.LA(1);

                int index41_16 = input.index();
                input.rewind();
                s = -1;
                if ((LA41_16 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 37;
                }

                input.seek(index41_16);
                if (s >= 0)
                    return s;
                break;

            case 58:
                int LA41_15 = input.LA(1);

                int index41_15 = input.index();
                input.rewind();
                s = -1;
                if ((LA41_15 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 37;
                }

                input.seek(index41_15);
                if (s >= 0)
                    return s;
                break;

            case 59:
                int LA41_103 = input.LA(1);

                int index41_103 = input.index();
                input.rewind();
                s = -1;
                if ((LA41_103 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 106;
                }

                input.seek(index41_103);
                if (s >= 0)
                    return s;
                break;

            case 60:
                int LA41_17 = input.LA(1);

                int index41_17 = input.index();
                input.rewind();
                s = -1;
                if ((LA41_17 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 37;
                }

                input.seek(index41_17);
                if (s >= 0)
                    return s;
                break;

            case 61:
                int LA41_102 = input.LA(1);

                int index41_102 = input.index();
                input.rewind();
                s = -1;
                if ((LA41_102 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 106;
                }

                input.seek(index41_102);
                if (s >= 0)
                    return s;
                break;

            case 62:
                int LA41_101 = input.LA(1);

                int index41_101 = input.index();
                input.rewind();
                s = -1;
                if ((LA41_101 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 106;
                }

                input.seek(index41_101);
                if (s >= 0)
                    return s;
                break;

            case 63:
                int LA41_104 = input.LA(1);

                int index41_104 = input.index();
                input.rewind();
                s = -1;
                if ((LA41_104 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 106;
                }

                input.seek(index41_104);
                if (s >= 0)
                    return s;
                break;

            case 64:
                int LA41_100 = input.LA(1);

                int index41_100 = input.index();
                input.rewind();
                s = -1;
                if ((LA41_100 == DOUBLE_QUOTE) && ((strict == false)))
                {
                    s = 106;
                }

                input.seek(index41_100);
                if (s >= 0)
                    return s;
                break;
            }
            if (state.backtracking > 0)
            {
                state.failed = true;
                return -1;
            }
            NoViableAltException nvae = new NoViableAltException(getDescription(), 41, _s, input);
            error(nvae);
            throw nvae;
        }
    }

    public static final BitSet FOLLOW_SELECT_in_query415 = new BitSet(new long[]{0x1EA09FE2882205E0L, 0x0000000000E10C00L});
    public static final BitSet FOLLOW_selectList_in_query417 = new BitSet(new long[]{0x0000000200000000L});
    public static final BitSet FOLLOW_fromClause_in_query419 = new BitSet(new long[]{0x0800000000000000L, 0x0000000000800000L});
    public static final BitSet FOLLOW_whereClause_in_query421 = new BitSet(new long[]{0x0800000000000000L});
    public static final BitSet FOLLOW_orderByClause_in_query424 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_query427 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STAR_in_selectList526 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_selectSubList_in_selectList584 = new BitSet(new long[]{0x0000000000008002L});
    public static final BitSet FOLLOW_COMMA_in_selectList587 = new BitSet(new long[]{0x1EA09FE2882205E0L, 0x0000000000E00C00L});
    public static final BitSet FOLLOW_selectSubList_in_selectList589 = new BitSet(new long[]{0x0000000000008002L});
    public static final BitSet FOLLOW_valueExpression_in_selectSubList673 = new BitSet(new long[]{0x0000002008000082L});
    public static final BitSet FOLLOW_AS_in_selectSubList676 = new BitSet(new long[]{0x0000002008000000L});
    public static final BitSet FOLLOW_columnName_in_selectSubList679 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_qualifier_in_selectSubList744 = new BitSet(new long[]{0x0000000004000000L});
    public static final BitSet FOLLOW_DOTSTAR_in_selectSubList746 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_columnReference_in_valueExpression837 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_valueFunction_in_valueExpression869 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_qualifier_in_columnReference933 = new BitSet(new long[]{0x0000000001000000L});
    public static final BitSet FOLLOW_DOT_in_columnReference935 = new BitSet(new long[]{0x0000002008000000L});
    public static final BitSet FOLLOW_columnName_in_columnReference939 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cmisFunction_in_valueFunction1035 = new BitSet(new long[]{0x0002000000000000L});
    public static final BitSet FOLLOW_LPAREN_in_valueFunction1037 = new BitSet(new long[]{0x0000002188080800L, 0x0000000000600300L});
    public static final BitSet FOLLOW_functionArgument_in_valueFunction1039 = new BitSet(new long[]{0x0000002188080800L, 0x0000000000600300L});
    public static final BitSet FOLLOW_RPAREN_in_valueFunction1042 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_keyWordOrId_in_valueFunction1115 = new BitSet(new long[]{0x0002000000000000L});
    public static final BitSet FOLLOW_LPAREN_in_valueFunction1117 = new BitSet(new long[]{0x0000002188080800L, 0x0000000000600300L});
    public static final BitSet FOLLOW_functionArgument_in_valueFunction1119 = new BitSet(new long[]{0x0000002188080800L, 0x0000000000600300L});
    public static final BitSet FOLLOW_RPAREN_in_valueFunction1122 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_qualifier_in_functionArgument1211 = new BitSet(new long[]{0x0000000001000000L});
    public static final BitSet FOLLOW_DOT_in_functionArgument1213 = new BitSet(new long[]{0x0000002008000000L});
    public static final BitSet FOLLOW_columnName_in_functionArgument1215 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_identifier_in_functionArgument1277 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_literalOrParameterName_in_functionArgument1289 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_tableName_in_qualifier1328 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_correlationName_in_qualifier1360 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FROM_in_fromClause1423 = new BitSet(new long[]{0x0002002008000000L});
    public static final BitSet FOLLOW_tableReference_in_fromClause1425 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_singleTable_in_tableReference1488 = new BitSet(new long[]{0x0000188000000002L});
    public static final BitSet FOLLOW_joinedTable_in_tableReference1490 = new BitSet(new long[]{0x0000188000000002L});
    public static final BitSet FOLLOW_simpleTable_in_singleTable1588 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_complexTable_in_singleTable1620 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_tableName_in_simpleTable1711 = new BitSet(new long[]{0x0000002008000082L});
    public static final BitSet FOLLOW_AS_in_simpleTable1714 = new BitSet(new long[]{0x0000002008000000L});
    public static final BitSet FOLLOW_correlationName_in_simpleTable1717 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_joinType_in_joinedTable1813 = new BitSet(new long[]{0x0000080000000000L});
    public static final BitSet FOLLOW_JOIN_in_joinedTable1816 = new BitSet(new long[]{0x0002002008000000L});
    public static final BitSet FOLLOW_tableReference_in_joinedTable1818 = new BitSet(new long[]{0x0200000000000000L});
    public static final BitSet FOLLOW_joinSpecification_in_joinedTable1820 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_complexTable1929 = new BitSet(new long[]{0x0002002008000000L});
    public static final BitSet FOLLOW_singleTable_in_complexTable1931 = new BitSet(new long[]{0x0000188000000000L});
    public static final BitSet FOLLOW_joinedTable_in_complexTable1933 = new BitSet(new long[]{0x0000188000000000L, 0x0000000000000200L});
    public static final BitSet FOLLOW_RPAREN_in_complexTable1936 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_complexTable1999 = new BitSet(new long[]{0x0002000000000000L});
    public static final BitSet FOLLOW_complexTable_in_complexTable2001 = new BitSet(new long[]{0x0000000000000000L, 0x0000000000000200L});
    public static final BitSet FOLLOW_RPAREN_in_complexTable2003 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_INNER_in_joinType2056 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LEFT_in_joinType2088 = new BitSet(new long[]{0x1000000000000002L});
    public static final BitSet FOLLOW_OUTER_in_joinType2090 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ON_in_joinSpecification2154 = new BitSet(new long[]{0x0000002008000000L});
    public static final BitSet FOLLOW_columnReference_in_joinSpecification2158 = new BitSet(new long[]{0x0000000020000000L});
    public static final BitSet FOLLOW_EQUALS_in_joinSpecification2160 = new BitSet(new long[]{0x0000002008000000L});
    public static final BitSet FOLLOW_columnReference_in_joinSpecification2164 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_WHERE_in_whereClause2264 = new BitSet(new long[]{0x1EA29FE3882A0DE0L, 0x0000000000E00D00L});
    public static final BitSet FOLLOW_searchOrCondition_in_whereClause2266 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_searchAndCondition_in_searchOrCondition2321 = new BitSet(new long[]{0x0400000000000002L});
    public static final BitSet FOLLOW_OR_in_searchOrCondition2324 = new BitSet(new long[]{0x1EA29FE3882A0DE0L, 0x0000000000E00D00L});
    public static final BitSet FOLLOW_searchAndCondition_in_searchOrCondition2326 = new BitSet(new long[]{0x0400000000000002L});
    public static final BitSet FOLLOW_searchNotCondition_in_searchAndCondition2412 = new BitSet(new long[]{0x0000000000000022L});
    public static final BitSet FOLLOW_AND_in_searchAndCondition2415 = new BitSet(new long[]{0x1EA29FE3882A0DE0L, 0x0000000000E00D00L});
    public static final BitSet FOLLOW_searchNotCondition_in_searchAndCondition2417 = new BitSet(new long[]{0x0000000000000022L});
    public static final BitSet FOLLOW_NOT_in_searchNotCondition2501 = new BitSet(new long[]{0x1EA29FE3882A0DE0L, 0x0000000000E00D00L});
    public static final BitSet FOLLOW_searchTest_in_searchNotCondition2503 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_searchTest_in_searchNotCondition2563 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_predicate_in_searchTest2616 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_searchTest2648 = new BitSet(new long[]{0x1EA29FE3882A0DE0L, 0x0000000000E00D00L});
    public static final BitSet FOLLOW_searchOrCondition_in_searchTest2650 = new BitSet(new long[]{0x0000000000000000L, 0x0000000000000200L});
    public static final BitSet FOLLOW_RPAREN_in_searchTest2652 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_comparisonPredicate_in_predicate2705 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_inPredicate_in_predicate2717 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_likePredicate_in_predicate2729 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_nullPredicate_in_predicate2741 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_quantifiedComparisonPredicate_in_predicate2753 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_quantifiedInPredicate_in_predicate2765 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_textSearchPredicate_in_predicate2777 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_folderPredicate_in_predicate2789 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_valueExpression_in_comparisonPredicate2822 = new BitSet(new long[]{0x0040601820000000L});
    public static final BitSet FOLLOW_compOp_in_comparisonPredicate2824 = new BitSet(new long[]{0x0000000180080800L, 0x0000000000600100L});
    public static final BitSet FOLLOW_literalOrParameterName_in_comparisonPredicate2826 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_literal_in_literalOrParameterName3006 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_parameterName_in_literalOrParameterName3021 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_signedNumericLiteral_in_literal3054 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_characterStringLiteral_in_literal3066 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_booleanLiteral_in_literal3078 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_datetimeLiteral_in_literal3090 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_columnReference_in_inPredicate3123 = new BitSet(new long[]{0x0020004000000000L});
    public static final BitSet FOLLOW_NOT_in_inPredicate3125 = new BitSet(new long[]{0x0000004000000000L});
    public static final BitSet FOLLOW_IN_in_inPredicate3128 = new BitSet(new long[]{0x0002000000000000L});
    public static final BitSet FOLLOW_LPAREN_in_inPredicate3130 = new BitSet(new long[]{0x0000000180080800L, 0x0000000000600100L});
    public static final BitSet FOLLOW_inValueList_in_inPredicate3132 = new BitSet(new long[]{0x0000000000000000L, 0x0000000000000200L});
    public static final BitSet FOLLOW_RPAREN_in_inPredicate3134 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_literalOrParameterName_in_inValueList3222 = new BitSet(new long[]{0x0000000000008002L});
    public static final BitSet FOLLOW_COMMA_in_inValueList3225 = new BitSet(new long[]{0x0000000180080800L, 0x0000000000600100L});
    public static final BitSet FOLLOW_literalOrParameterName_in_inValueList3227 = new BitSet(new long[]{0x0000000000008002L});
    public static final BitSet FOLLOW_columnReference_in_likePredicate3311 = new BitSet(new long[]{0x0020800000000000L});
    public static final BitSet FOLLOW_NOT_in_likePredicate3313 = new BitSet(new long[]{0x0000800000000000L});
    public static final BitSet FOLLOW_LIKE_in_likePredicate3316 = new BitSet(new long[]{0x0000000000000000L, 0x0000000000000100L});
    public static final BitSet FOLLOW_characterStringLiteral_in_likePredicate3318 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_columnReference_in_nullPredicate3404 = new BitSet(new long[]{0x0000040000000000L});
    public static final BitSet FOLLOW_IS_in_nullPredicate3406 = new BitSet(new long[]{0x0080000000000000L});
    public static final BitSet FOLLOW_NULL_in_nullPredicate3408 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_columnReference_in_nullPredicate3470 = new BitSet(new long[]{0x0000040000000000L});
    public static final BitSet FOLLOW_IS_in_nullPredicate3472 = new BitSet(new long[]{0x0020000000000000L});
    public static final BitSet FOLLOW_NOT_in_nullPredicate3474 = new BitSet(new long[]{0x0080000000000000L});
    public static final BitSet FOLLOW_NULL_in_nullPredicate3476 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_literalOrParameterName_in_quantifiedComparisonPredicate3557 = new BitSet(new long[]{0x0040601820000000L});
    public static final BitSet FOLLOW_compOp_in_quantifiedComparisonPredicate3559 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_ANY_in_quantifiedComparisonPredicate3561 = new BitSet(new long[]{0x0000002008000000L});
    public static final BitSet FOLLOW_columnReference_in_quantifiedComparisonPredicate3563 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ANY_in_quantifiedInPredicate3650 = new BitSet(new long[]{0x0000002008000000L});
    public static final BitSet FOLLOW_columnReference_in_quantifiedInPredicate3652 = new BitSet(new long[]{0x0020004000000000L});
    public static final BitSet FOLLOW_NOT_in_quantifiedInPredicate3654 = new BitSet(new long[]{0x0000004000000000L});
    public static final BitSet FOLLOW_IN_in_quantifiedInPredicate3657 = new BitSet(new long[]{0x0002000000000000L});
    public static final BitSet FOLLOW_LPAREN_in_quantifiedInPredicate3659 = new BitSet(new long[]{0x0000000180080800L, 0x0000000000600100L});
    public static final BitSet FOLLOW_inValueList_in_quantifiedInPredicate3661 = new BitSet(new long[]{0x0000000000000000L, 0x0000000000000200L});
    public static final BitSet FOLLOW_RPAREN_in_quantifiedInPredicate3663 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_CONTAINS_in_textSearchPredicate3751 = new BitSet(new long[]{0x0002000000000000L});
    public static final BitSet FOLLOW_LPAREN_in_textSearchPredicate3753 = new BitSet(new long[]{0x0000002008000000L, 0x0000000000000100L});
    public static final BitSet FOLLOW_qualifier_in_textSearchPredicate3756 = new BitSet(new long[]{0x0000000000008000L});
    public static final BitSet FOLLOW_COMMA_in_textSearchPredicate3758 = new BitSet(new long[]{0x0000000000000000L, 0x0000000000000100L});
    public static final BitSet FOLLOW_textSearchExpression_in_textSearchPredicate3762 = new BitSet(new long[]{0x0000000000000000L, 0x0000000000000200L});
    public static final BitSet FOLLOW_RPAREN_in_textSearchPredicate3764 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IN_FOLDER_in_folderPredicate3848 = new BitSet(new long[]{0x0002000000000000L});
    public static final BitSet FOLLOW_folderPredicateArgs_in_folderPredicate3850 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IN_TREE_in_folderPredicate3910 = new BitSet(new long[]{0x0002000000000000L});
    public static final BitSet FOLLOW_folderPredicateArgs_in_folderPredicate3912 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_folderPredicateArgs3993 = new BitSet(new long[]{0x0000002008000000L, 0x0000000000000100L});
    public static final BitSet FOLLOW_qualifier_in_folderPredicateArgs3996 = new BitSet(new long[]{0x0000000000008000L});
    public static final BitSet FOLLOW_COMMA_in_folderPredicateArgs3998 = new BitSet(new long[]{0x0000000000000000L, 0x0000000000000100L});
    public static final BitSet FOLLOW_folderId_in_folderPredicateArgs4002 = new BitSet(new long[]{0x0000000000000000L, 0x0000000000000200L});
    public static final BitSet FOLLOW_RPAREN_in_folderPredicateArgs4004 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ORDER_in_orderByClause4070 = new BitSet(new long[]{0x0000000000000400L});
    public static final BitSet FOLLOW_BY_in_orderByClause4072 = new BitSet(new long[]{0x0000002008000000L});
    public static final BitSet FOLLOW_sortSpecification_in_orderByClause4074 = new BitSet(new long[]{0x0000000000008002L});
    public static final BitSet FOLLOW_COMMA_in_orderByClause4077 = new BitSet(new long[]{0x0000002008000000L});
    public static final BitSet FOLLOW_sortSpecification_in_orderByClause4079 = new BitSet(new long[]{0x0000000000008002L});
    public static final BitSet FOLLOW_columnReference_in_sortSpecification4163 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_columnReference_in_sortSpecification4225 = new BitSet(new long[]{0x0000000000200100L});
    public static final BitSet FOLLOW_ASC_in_sortSpecification4255 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DESC_in_sortSpecification4277 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_identifier_in_correlationName4371 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_identifier_in_tableName4407 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_identifier_in_columnName4460 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_COLON_in_parameterName4513 = new BitSet(new long[]{0x0000002008000000L});
    public static final BitSet FOLLOW_identifier_in_parameterName4515 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_characterStringLiteral_in_folderId4596 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_QUOTED_STRING_in_textSearchExpression4649 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_identifier4682 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DOUBLE_QUOTE_in_identifier4717 = new BitSet(new long[]{0x1EA09FE2802205E0L, 0x0000000000E00C00L});
    public static final BitSet FOLLOW_keyWordOrId_in_identifier4719 = new BitSet(new long[]{0x0000000008000000L});
    public static final BitSet FOLLOW_DOUBLE_QUOTE_in_identifier4721 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FLOATING_POINT_LITERAL_in_signedNumericLiteral4800 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_integerLiteral_in_signedNumericLiteral4860 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DECIMAL_INTEGER_LITERAL_in_integerLiteral4913 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TRUE_in_booleanLiteral4994 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FALSE_in_booleanLiteral5054 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TIMESTAMP_in_datetimeLiteral5135 = new BitSet(new long[]{0x0000000000000000L, 0x0000000000000100L});
    public static final BitSet FOLLOW_QUOTED_STRING_in_datetimeLiteral5137 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_QUOTED_STRING_in_characterStringLiteral5218 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SELECT_in_keyWord5299 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_AS_in_keyWord5311 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FROM_in_keyWord5323 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_JOIN_in_keyWord5335 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_INNER_in_keyWord5347 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LEFT_in_keyWord5359 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_OUTER_in_keyWord5371 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ON_in_keyWord5383 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_WHERE_in_keyWord5395 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_OR_in_keyWord5407 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_AND_in_keyWord5419 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NOT_in_keyWord5431 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IN_in_keyWord5443 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LIKE_in_keyWord5455 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IS_in_keyWord5467 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NULL_in_keyWord5479 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ANY_in_keyWord5491 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_CONTAINS_in_keyWord5503 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IN_FOLDER_in_keyWord5515 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IN_TREE_in_keyWord5527 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ORDER_in_keyWord5539 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_BY_in_keyWord5551 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ASC_in_keyWord5563 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DESC_in_keyWord5575 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TIMESTAMP_in_keyWord5587 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TRUE_in_keyWord5599 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FALSE_in_keyWord5611 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cmisFunction_in_keyWord5623 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SCORE_in_cmisFunction5656 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_keyWord_in_keyWordOrId5709 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_keyWordOrId5741 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_tableName_in_synpred1_CMIS1323 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_synpred2_CMIS1917 = new BitSet(new long[]{0x0002002008000000L});
    public static final BitSet FOLLOW_singleTable_in_synpred2_CMIS1919 = new BitSet(new long[]{0x0000188000000000L});
    public static final BitSet FOLLOW_joinedTable_in_synpred2_CMIS1921 = new BitSet(new long[]{0x0000188000000000L, 0x0000000000000200L});
    public static final BitSet FOLLOW_RPAREN_in_synpred2_CMIS1924 = new BitSet(new long[]{0x0000000000000002L});
}
