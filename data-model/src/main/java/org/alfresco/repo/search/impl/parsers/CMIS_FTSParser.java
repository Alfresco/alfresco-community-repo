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
// $ANTLR 3.5.2 W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS_FTS.g 2015-06-18 19:37:58

package org.alfresco.repo.search.impl.parsers;

import java.util.List;
import java.util.Stack;

import org.antlr.runtime.*;
import org.antlr.runtime.tree.*;

@SuppressWarnings("all")
public class CMIS_FTSParser extends Parser
{
    public static final String[] tokenNames = new String[]{
            "<invalid>", "<EOR>", "<DOWN>", "<UP>", "CONJUNCTION", "DEFAULT", "DISJUNCTION",
            "EXCLUDE", "FTSPHRASE", "FTSWORD", "F_ESC", "IN_WORD", "MINUS", "OR",
            "PHRASE", "START_WORD", "TERM", "WS"
    };
    public static final int EOF = -1;
    public static final int CONJUNCTION = 4;
    public static final int DEFAULT = 5;
    public static final int DISJUNCTION = 6;
    public static final int EXCLUDE = 7;
    public static final int FTSPHRASE = 8;
    public static final int FTSWORD = 9;
    public static final int F_ESC = 10;
    public static final int IN_WORD = 11;
    public static final int MINUS = 12;
    public static final int OR = 13;
    public static final int PHRASE = 14;
    public static final int START_WORD = 15;
    public static final int TERM = 16;
    public static final int WS = 17;

    // delegates
    public Parser[] getDelegates()
    {
        return new Parser[]{};
    }

    // delegators

    public CMIS_FTSParser(TokenStream input)
    {
        this(input, new RecognizerSharedState());
    }

    public CMIS_FTSParser(TokenStream input, RecognizerSharedState state)
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
        return CMIS_FTSParser.tokenNames;
    }

    @Override
    public String getGrammarFileName()
    {
        return "W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS_FTS.g";
    }

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

    public static class cmisFtsQuery_return extends ParserRuleReturnScope
    {
        Object tree;

        @Override
        public Object getTree()
        {
            return tree;
        }
    };

    // $ANTLR start "cmisFtsQuery"
    // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS_FTS.g:278:1: cmisFtsQuery : ftsCmisDisjunction EOF -> ftsCmisDisjunction ;
    public final CMIS_FTSParser.cmisFtsQuery_return cmisFtsQuery() throws RecognitionException
    {
        CMIS_FTSParser.cmisFtsQuery_return retval = new CMIS_FTSParser.cmisFtsQuery_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token EOF2 = null;
        ParserRuleReturnScope ftsCmisDisjunction1 = null;

        Object EOF2_tree = null;
        RewriteRuleTokenStream stream_EOF = new RewriteRuleTokenStream(adaptor, "token EOF");
        RewriteRuleSubtreeStream stream_ftsCmisDisjunction = new RewriteRuleSubtreeStream(adaptor, "rule ftsCmisDisjunction");

        try
        {
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS_FTS.g:279:9: ( ftsCmisDisjunction EOF -> ftsCmisDisjunction )
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS_FTS.g:280:9: ftsCmisDisjunction EOF
            {
                pushFollow(FOLLOW_ftsCmisDisjunction_in_cmisFtsQuery194);
                ftsCmisDisjunction1 = ftsCmisDisjunction();
                state._fsp--;

                stream_ftsCmisDisjunction.add(ftsCmisDisjunction1.getTree());
                EOF2 = (Token) match(input, EOF, FOLLOW_EOF_in_cmisFtsQuery196);
                stream_EOF.add(EOF2);

                // AST REWRITE
                // elements: ftsCmisDisjunction
                // token labels:
                // rule labels: retval
                // token list labels:
                // rule list labels:
                // wildcard labels:
                retval.tree = root_0;
                RewriteRuleSubtreeStream stream_retval = new RewriteRuleSubtreeStream(adaptor, "rule retval", retval != null ? retval.getTree() : null);

                root_0 = (Object) adaptor.nil();
                // 281:17: -> ftsCmisDisjunction
                {
                    adaptor.addChild(root_0, stream_ftsCmisDisjunction.nextTree());
                }

                retval.tree = root_0;

            }

            retval.stop = input.LT(-1);

            retval.tree = (Object) adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }

        catch (RecognitionException e)
        {
            throw e;
        }

        finally
        {
            // do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "cmisFtsQuery"

    public static class ftsCmisDisjunction_return extends ParserRuleReturnScope
    {
        Object tree;

        @Override
        public Object getTree()
        {
            return tree;
        }
    };

    // $ANTLR start "ftsCmisDisjunction"
    // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS_FTS.g:289:1: ftsCmisDisjunction : ftsCmisConjunction ( or ftsCmisConjunction )* -> ^( DISJUNCTION ( ftsCmisConjunction )+ ) ;
    public final CMIS_FTSParser.ftsCmisDisjunction_return ftsCmisDisjunction() throws RecognitionException
    {
        CMIS_FTSParser.ftsCmisDisjunction_return retval = new CMIS_FTSParser.ftsCmisDisjunction_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        ParserRuleReturnScope ftsCmisConjunction3 = null;
        ParserRuleReturnScope or4 = null;
        ParserRuleReturnScope ftsCmisConjunction5 = null;

        RewriteRuleSubtreeStream stream_ftsCmisConjunction = new RewriteRuleSubtreeStream(adaptor, "rule ftsCmisConjunction");
        RewriteRuleSubtreeStream stream_or = new RewriteRuleSubtreeStream(adaptor, "rule or");

        try
        {
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS_FTS.g:290:9: ( ftsCmisConjunction ( or ftsCmisConjunction )* -> ^( DISJUNCTION ( ftsCmisConjunction )+ ) )
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS_FTS.g:291:9: ftsCmisConjunction ( or ftsCmisConjunction )*
            {
                pushFollow(FOLLOW_ftsCmisConjunction_in_ftsCmisDisjunction252);
                ftsCmisConjunction3 = ftsCmisConjunction();
                state._fsp--;

                stream_ftsCmisConjunction.add(ftsCmisConjunction3.getTree());
                // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS_FTS.g:291:28: ( or ftsCmisConjunction )*
                loop1: while (true)
                {
                    int alt1 = 2;
                    int LA1_0 = input.LA(1);
                    if ((LA1_0 == OR))
                    {
                        alt1 = 1;
                    }

                    switch (alt1)
                    {
                    case 1:
                    // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS_FTS.g:291:29: or ftsCmisConjunction
                    {
                        pushFollow(FOLLOW_or_in_ftsCmisDisjunction255);
                        or4 = or();
                        state._fsp--;

                        stream_or.add(or4.getTree());
                        pushFollow(FOLLOW_ftsCmisConjunction_in_ftsCmisDisjunction257);
                        ftsCmisConjunction5 = ftsCmisConjunction();
                        state._fsp--;

                        stream_ftsCmisConjunction.add(ftsCmisConjunction5.getTree());
                    }
                        break;

                    default:
                        break loop1;
                    }
                }

                // AST REWRITE
                // elements: ftsCmisConjunction
                // token labels:
                // rule labels: retval
                // token list labels:
                // rule list labels:
                // wildcard labels:
                retval.tree = root_0;
                RewriteRuleSubtreeStream stream_retval = new RewriteRuleSubtreeStream(adaptor, "rule retval", retval != null ? retval.getTree() : null);

                root_0 = (Object) adaptor.nil();
                // 292:17: -> ^( DISJUNCTION ( ftsCmisConjunction )+ )
                {
                    // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS_FTS.g:293:25: ^( DISJUNCTION ( ftsCmisConjunction )+ )
                    {
                        Object root_1 = (Object) adaptor.nil();
                        root_1 = (Object) adaptor.becomeRoot((Object) adaptor.create(DISJUNCTION, "DISJUNCTION"), root_1);
                        if (!(stream_ftsCmisConjunction.hasNext()))
                        {
                            throw new RewriteEarlyExitException();
                        }
                        while (stream_ftsCmisConjunction.hasNext())
                        {
                            adaptor.addChild(root_1, stream_ftsCmisConjunction.nextTree());
                        }
                        stream_ftsCmisConjunction.reset();

                        adaptor.addChild(root_0, root_1);
                    }

                }

                retval.tree = root_0;

            }

            retval.stop = input.LT(-1);

            retval.tree = (Object) adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }

        catch (RecognitionException e)
        {
            throw e;
        }

        finally
        {
            // do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "ftsCmisDisjunction"

    public static class ftsCmisConjunction_return extends ParserRuleReturnScope
    {
        Object tree;

        @Override
        public Object getTree()
        {
            return tree;
        }
    };

    // $ANTLR start "ftsCmisConjunction"
    // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS_FTS.g:296:1: ftsCmisConjunction : ( ftsCmisPrefixed )+ -> ^( CONJUNCTION ( ftsCmisPrefixed )+ ) ;
    public final CMIS_FTSParser.ftsCmisConjunction_return ftsCmisConjunction() throws RecognitionException
    {
        CMIS_FTSParser.ftsCmisConjunction_return retval = new CMIS_FTSParser.ftsCmisConjunction_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        ParserRuleReturnScope ftsCmisPrefixed6 = null;

        RewriteRuleSubtreeStream stream_ftsCmisPrefixed = new RewriteRuleSubtreeStream(adaptor, "rule ftsCmisPrefixed");

        try
        {
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS_FTS.g:297:9: ( ( ftsCmisPrefixed )+ -> ^( CONJUNCTION ( ftsCmisPrefixed )+ ) )
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS_FTS.g:298:9: ( ftsCmisPrefixed )+
            {
                // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS_FTS.g:298:9: ( ftsCmisPrefixed )+
                int cnt2 = 0;
                loop2: while (true)
                {
                    int alt2 = 2;
                    int LA2_0 = input.LA(1);
                    if (((LA2_0 >= FTSPHRASE && LA2_0 <= FTSWORD) || LA2_0 == MINUS))
                    {
                        alt2 = 1;
                    }

                    switch (alt2)
                    {
                    case 1:
                    // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS_FTS.g:298:9: ftsCmisPrefixed
                    {
                        pushFollow(FOLLOW_ftsCmisPrefixed_in_ftsCmisConjunction341);
                        ftsCmisPrefixed6 = ftsCmisPrefixed();
                        state._fsp--;

                        stream_ftsCmisPrefixed.add(ftsCmisPrefixed6.getTree());
                    }
                        break;

                    default:
                        if (cnt2 >= 1)
                            break loop2;
                        EarlyExitException eee = new EarlyExitException(2, input);
                        throw eee;
                    }
                    cnt2++;
                }

                // AST REWRITE
                // elements: ftsCmisPrefixed
                // token labels:
                // rule labels: retval
                // token list labels:
                // rule list labels:
                // wildcard labels:
                retval.tree = root_0;
                RewriteRuleSubtreeStream stream_retval = new RewriteRuleSubtreeStream(adaptor, "rule retval", retval != null ? retval.getTree() : null);

                root_0 = (Object) adaptor.nil();
                // 299:17: -> ^( CONJUNCTION ( ftsCmisPrefixed )+ )
                {
                    // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS_FTS.g:300:25: ^( CONJUNCTION ( ftsCmisPrefixed )+ )
                    {
                        Object root_1 = (Object) adaptor.nil();
                        root_1 = (Object) adaptor.becomeRoot((Object) adaptor.create(CONJUNCTION, "CONJUNCTION"), root_1);
                        if (!(stream_ftsCmisPrefixed.hasNext()))
                        {
                            throw new RewriteEarlyExitException();
                        }
                        while (stream_ftsCmisPrefixed.hasNext())
                        {
                            adaptor.addChild(root_1, stream_ftsCmisPrefixed.nextTree());
                        }
                        stream_ftsCmisPrefixed.reset();

                        adaptor.addChild(root_0, root_1);
                    }

                }

                retval.tree = root_0;

            }

            retval.stop = input.LT(-1);

            retval.tree = (Object) adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }

        catch (RecognitionException e)
        {
            throw e;
        }

        finally
        {
            // do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "ftsCmisConjunction"

    public static class ftsCmisPrefixed_return extends ParserRuleReturnScope
    {
        Object tree;

        @Override
        public Object getTree()
        {
            return tree;
        }
    };

    // $ANTLR start "ftsCmisPrefixed"
    // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS_FTS.g:303:1: ftsCmisPrefixed : ( cmisTest -> ^( DEFAULT cmisTest ) | MINUS cmisTest -> ^( EXCLUDE cmisTest ) );
    public final CMIS_FTSParser.ftsCmisPrefixed_return ftsCmisPrefixed() throws RecognitionException
    {
        CMIS_FTSParser.ftsCmisPrefixed_return retval = new CMIS_FTSParser.ftsCmisPrefixed_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token MINUS8 = null;
        ParserRuleReturnScope cmisTest7 = null;
        ParserRuleReturnScope cmisTest9 = null;

        Object MINUS8_tree = null;
        RewriteRuleTokenStream stream_MINUS = new RewriteRuleTokenStream(adaptor, "token MINUS");
        RewriteRuleSubtreeStream stream_cmisTest = new RewriteRuleSubtreeStream(adaptor, "rule cmisTest");

        try
        {
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS_FTS.g:304:9: ( cmisTest -> ^( DEFAULT cmisTest ) | MINUS cmisTest -> ^( EXCLUDE cmisTest ) )
            int alt3 = 2;
            int LA3_0 = input.LA(1);
            if (((LA3_0 >= FTSPHRASE && LA3_0 <= FTSWORD)))
            {
                alt3 = 1;
            }
            else if ((LA3_0 == MINUS))
            {
                alt3 = 2;
            }

            else
            {
                NoViableAltException nvae = new NoViableAltException("", 3, 0, input);
                throw nvae;
            }

            switch (alt3)
            {
            case 1:
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS_FTS.g:305:9: cmisTest
            {
                pushFollow(FOLLOW_cmisTest_in_ftsCmisPrefixed424);
                cmisTest7 = cmisTest();
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
                RewriteRuleSubtreeStream stream_retval = new RewriteRuleSubtreeStream(adaptor, "rule retval", retval != null ? retval.getTree() : null);

                root_0 = (Object) adaptor.nil();
                // 306:17: -> ^( DEFAULT cmisTest )
                {
                    // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS_FTS.g:307:25: ^( DEFAULT cmisTest )
                    {
                        Object root_1 = (Object) adaptor.nil();
                        root_1 = (Object) adaptor.becomeRoot((Object) adaptor.create(DEFAULT, "DEFAULT"), root_1);
                        adaptor.addChild(root_1, stream_cmisTest.nextTree());
                        adaptor.addChild(root_0, root_1);
                    }

                }

                retval.tree = root_0;

            }
                break;
            case 2:
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS_FTS.g:308:11: MINUS cmisTest
            {
                MINUS8 = (Token) match(input, MINUS, FOLLOW_MINUS_in_ftsCmisPrefixed484);
                stream_MINUS.add(MINUS8);

                pushFollow(FOLLOW_cmisTest_in_ftsCmisPrefixed486);
                cmisTest9 = cmisTest();
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
                RewriteRuleSubtreeStream stream_retval = new RewriteRuleSubtreeStream(adaptor, "rule retval", retval != null ? retval.getTree() : null);

                root_0 = (Object) adaptor.nil();
                // 309:17: -> ^( EXCLUDE cmisTest )
                {
                    // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS_FTS.g:310:25: ^( EXCLUDE cmisTest )
                    {
                        Object root_1 = (Object) adaptor.nil();
                        root_1 = (Object) adaptor.becomeRoot((Object) adaptor.create(EXCLUDE, "EXCLUDE"), root_1);
                        adaptor.addChild(root_1, stream_cmisTest.nextTree());
                        adaptor.addChild(root_0, root_1);
                    }

                }

                retval.tree = root_0;

            }
                break;

            }
            retval.stop = input.LT(-1);

            retval.tree = (Object) adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }

        catch (RecognitionException e)
        {
            throw e;
        }

        finally
        {
            // do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "ftsCmisPrefixed"

    public static class cmisTest_return extends ParserRuleReturnScope
    {
        Object tree;

        @Override
        public Object getTree()
        {
            return tree;
        }
    };

    // $ANTLR start "cmisTest"
    // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS_FTS.g:313:1: cmisTest : ( cmisTerm -> ^( TERM cmisTerm ) | cmisPhrase -> ^( PHRASE cmisPhrase ) );
    public final CMIS_FTSParser.cmisTest_return cmisTest() throws RecognitionException
    {
        CMIS_FTSParser.cmisTest_return retval = new CMIS_FTSParser.cmisTest_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        ParserRuleReturnScope cmisTerm10 = null;
        ParserRuleReturnScope cmisPhrase11 = null;

        RewriteRuleSubtreeStream stream_cmisPhrase = new RewriteRuleSubtreeStream(adaptor, "rule cmisPhrase");
        RewriteRuleSubtreeStream stream_cmisTerm = new RewriteRuleSubtreeStream(adaptor, "rule cmisTerm");

        try
        {
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS_FTS.g:314:9: ( cmisTerm -> ^( TERM cmisTerm ) | cmisPhrase -> ^( PHRASE cmisPhrase ) )
            int alt4 = 2;
            int LA4_0 = input.LA(1);
            if ((LA4_0 == FTSWORD))
            {
                alt4 = 1;
            }
            else if ((LA4_0 == FTSPHRASE))
            {
                alt4 = 2;
            }

            else
            {
                NoViableAltException nvae = new NoViableAltException("", 4, 0, input);
                throw nvae;
            }

            switch (alt4)
            {
            case 1:
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS_FTS.g:315:9: cmisTerm
            {
                pushFollow(FOLLOW_cmisTerm_in_cmisTest567);
                cmisTerm10 = cmisTerm();
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
                RewriteRuleSubtreeStream stream_retval = new RewriteRuleSubtreeStream(adaptor, "rule retval", retval != null ? retval.getTree() : null);

                root_0 = (Object) adaptor.nil();
                // 316:17: -> ^( TERM cmisTerm )
                {
                    // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS_FTS.g:317:25: ^( TERM cmisTerm )
                    {
                        Object root_1 = (Object) adaptor.nil();
                        root_1 = (Object) adaptor.becomeRoot((Object) adaptor.create(TERM, "TERM"), root_1);
                        adaptor.addChild(root_1, stream_cmisTerm.nextTree());
                        adaptor.addChild(root_0, root_1);
                    }

                }

                retval.tree = root_0;

            }
                break;
            case 2:
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS_FTS.g:318:11: cmisPhrase
            {
                pushFollow(FOLLOW_cmisPhrase_in_cmisTest627);
                cmisPhrase11 = cmisPhrase();
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
                RewriteRuleSubtreeStream stream_retval = new RewriteRuleSubtreeStream(adaptor, "rule retval", retval != null ? retval.getTree() : null);

                root_0 = (Object) adaptor.nil();
                // 319:17: -> ^( PHRASE cmisPhrase )
                {
                    // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS_FTS.g:320:25: ^( PHRASE cmisPhrase )
                    {
                        Object root_1 = (Object) adaptor.nil();
                        root_1 = (Object) adaptor.becomeRoot((Object) adaptor.create(PHRASE, "PHRASE"), root_1);
                        adaptor.addChild(root_1, stream_cmisPhrase.nextTree());
                        adaptor.addChild(root_0, root_1);
                    }

                }

                retval.tree = root_0;

            }
                break;

            }
            retval.stop = input.LT(-1);

            retval.tree = (Object) adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }

        catch (RecognitionException e)
        {
            throw e;
        }

        finally
        {
            // do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "cmisTest"

    public static class cmisTerm_return extends ParserRuleReturnScope
    {
        Object tree;

        @Override
        public Object getTree()
        {
            return tree;
        }
    };

    // $ANTLR start "cmisTerm"
    // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS_FTS.g:324:1: cmisTerm : FTSWORD -> FTSWORD ;
    public final CMIS_FTSParser.cmisTerm_return cmisTerm() throws RecognitionException
    {
        CMIS_FTSParser.cmisTerm_return retval = new CMIS_FTSParser.cmisTerm_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token FTSWORD12 = null;

        Object FTSWORD12_tree = null;
        RewriteRuleTokenStream stream_FTSWORD = new RewriteRuleTokenStream(adaptor, "token FTSWORD");

        try
        {
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS_FTS.g:325:9: ( FTSWORD -> FTSWORD )
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS_FTS.g:326:9: FTSWORD
            {
                FTSWORD12 = (Token) match(input, FTSWORD, FOLLOW_FTSWORD_in_cmisTerm717);
                stream_FTSWORD.add(FTSWORD12);

                // AST REWRITE
                // elements: FTSWORD
                // token labels:
                // rule labels: retval
                // token list labels:
                // rule list labels:
                // wildcard labels:
                retval.tree = root_0;
                RewriteRuleSubtreeStream stream_retval = new RewriteRuleSubtreeStream(adaptor, "rule retval", retval != null ? retval.getTree() : null);

                root_0 = (Object) adaptor.nil();
                // 327:17: -> FTSWORD
                {
                    adaptor.addChild(root_0, stream_FTSWORD.nextNode());
                }

                retval.tree = root_0;

            }

            retval.stop = input.LT(-1);

            retval.tree = (Object) adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }

        catch (RecognitionException e)
        {
            throw e;
        }

        finally
        {
            // do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "cmisTerm"

    public static class cmisPhrase_return extends ParserRuleReturnScope
    {
        Object tree;

        @Override
        public Object getTree()
        {
            return tree;
        }
    };

    // $ANTLR start "cmisPhrase"
    // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS_FTS.g:330:1: cmisPhrase : FTSPHRASE -> FTSPHRASE ;
    public final CMIS_FTSParser.cmisPhrase_return cmisPhrase() throws RecognitionException
    {
        CMIS_FTSParser.cmisPhrase_return retval = new CMIS_FTSParser.cmisPhrase_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token FTSPHRASE13 = null;

        Object FTSPHRASE13_tree = null;
        RewriteRuleTokenStream stream_FTSPHRASE = new RewriteRuleTokenStream(adaptor, "token FTSPHRASE");

        try
        {
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS_FTS.g:331:9: ( FTSPHRASE -> FTSPHRASE )
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS_FTS.g:332:9: FTSPHRASE
            {
                FTSPHRASE13 = (Token) match(input, FTSPHRASE, FOLLOW_FTSPHRASE_in_cmisPhrase770);
                stream_FTSPHRASE.add(FTSPHRASE13);

                // AST REWRITE
                // elements: FTSPHRASE
                // token labels:
                // rule labels: retval
                // token list labels:
                // rule list labels:
                // wildcard labels:
                retval.tree = root_0;
                RewriteRuleSubtreeStream stream_retval = new RewriteRuleSubtreeStream(adaptor, "rule retval", retval != null ? retval.getTree() : null);

                root_0 = (Object) adaptor.nil();
                // 333:17: -> FTSPHRASE
                {
                    adaptor.addChild(root_0, stream_FTSPHRASE.nextNode());
                }

                retval.tree = root_0;

            }

            retval.stop = input.LT(-1);

            retval.tree = (Object) adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }

        catch (RecognitionException e)
        {
            throw e;
        }

        finally
        {
            // do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "cmisPhrase"

    public static class or_return extends ParserRuleReturnScope
    {
        Object tree;

        @Override
        public Object getTree()
        {
            return tree;
        }
    };

    // $ANTLR start "or"
    // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS_FTS.g:336:1: or : OR ;
    public final CMIS_FTSParser.or_return or() throws RecognitionException
    {
        CMIS_FTSParser.or_return retval = new CMIS_FTSParser.or_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token OR14 = null;

        Object OR14_tree = null;

        try
        {
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS_FTS.g:337:9: ( OR )
            // W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS_FTS.g:338:9: OR
            {
                root_0 = (Object) adaptor.nil();

                OR14 = (Token) match(input, OR, FOLLOW_OR_in_or823);
                OR14_tree = (Object) adaptor.create(OR14);
                adaptor.addChild(root_0, OR14_tree);

            }

            retval.stop = input.LT(-1);

            retval.tree = (Object) adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }

        catch (RecognitionException e)
        {
            throw e;
        }

        finally
        {
            // do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "or"

    // Delegated rules

    public static final BitSet FOLLOW_ftsCmisDisjunction_in_cmisFtsQuery194 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_cmisFtsQuery196 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ftsCmisConjunction_in_ftsCmisDisjunction252 = new BitSet(new long[]{0x0000000000002002L});
    public static final BitSet FOLLOW_or_in_ftsCmisDisjunction255 = new BitSet(new long[]{0x0000000000001300L});
    public static final BitSet FOLLOW_ftsCmisConjunction_in_ftsCmisDisjunction257 = new BitSet(new long[]{0x0000000000002002L});
    public static final BitSet FOLLOW_ftsCmisPrefixed_in_ftsCmisConjunction341 = new BitSet(new long[]{0x0000000000001302L});
    public static final BitSet FOLLOW_cmisTest_in_ftsCmisPrefixed424 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_MINUS_in_ftsCmisPrefixed484 = new BitSet(new long[]{0x0000000000000300L});
    public static final BitSet FOLLOW_cmisTest_in_ftsCmisPrefixed486 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cmisTerm_in_cmisTest567 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cmisPhrase_in_cmisTest627 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FTSWORD_in_cmisTerm717 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FTSPHRASE_in_cmisPhrase770 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_OR_in_or823 = new BitSet(new long[]{0x0000000000000002L});
}
