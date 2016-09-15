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
package org.alfresco.repo.search.impl.parsers;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.alfresco.repo.search.impl.querymodel.Column;
import org.alfresco.repo.search.impl.querymodel.FunctionEvaluationContext;
import org.alfresco.repo.search.impl.querymodel.QueryModelFactory;
import org.alfresco.repo.search.impl.querymodel.Selector;
import org.alfresco.repo.search.impl.querymodel.QueryOptions.Connective;
import org.alfresco.repo.search.impl.querymodel.impl.lucene.LuceneQueryModelFactory;
import org.alfresco.service.namespace.NamespaceService;
import org.antlr.gunit.GrammarInfo;
import org.antlr.gunit.gUnitLexer;
import org.antlr.gunit.gUnitParser;
import org.antlr.runtime.ANTLRInputStream;
import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.Tree;

public class FTSTest extends TestCase
{
    public FTSTest()
    {
        // TODO Auto-generated constructor stub
    }

    public FTSTest(String name)
    {
        super(name);
        // TODO Auto-generated constructor stub
    }

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

    }

    public void testLexer() throws IOException, RecognitionException
    {
        ClassLoader cl = FTSTest.class.getClassLoader();
        InputStream modelStream = cl.getResourceAsStream("org/alfresco/repo/search/impl/parsers/fts_test.gunit");

        CharStream input = new ANTLRInputStream(modelStream);

        gUnitExecutor executer = new gUnitExecutor(parse(input), "FTS");

        String result = executer.execTest();
        System.out.print(executer.execTest()); // unit test result

        assertEquals("Failures: " + result, 0, executer.failures.size());
        assertEquals("Invalids " + result, 0, executer.invalids.size());
    }

    private GrammarInfo parse(CharStream input) throws RecognitionException
    {
        gUnitLexer lexer = new gUnitLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);

        GrammarInfo grammarInfo = new GrammarInfo();
        gUnitParser parser = new gUnitParser(tokens, grammarInfo);
        parser.gUnitDef(); // parse gunit script and save elements to grammarInfo
        return grammarInfo;
    }

    public void testMapLoneStar() throws Exception
    {
        final String ftsExpression = "* AND * AND * AND * AND * AND * AND * AND * AND * AND * AND *";
        AlfrescoFunctionEvaluationContext functionContext = new AlfrescoFunctionEvaluationContext(null, null, NamespaceService.CONTENT_MODEL_1_0_URI);

        Map<String, String> templates = new HashMap<String, String>();
        String keywordsTemplate = "%(cm:name cm:title cm:description ia:whatEvent ia:descriptionEvent lnk:title lnk:description TEXT TAG)";
        String keywordsKey = "keywords";
        templates.put(keywordsKey, keywordsTemplate);

        final FTSParser.Mode mode = FTSParser.Mode.DEFAULT_DISJUNCTION;
        final Connective defaultFieldConnective = Connective.OR;

        class TestMock extends FTSQueryParser.TestNodeBuilder
        {
            private void test(CommonTree initialNode, CommonTree replacedNode)
            {
                if (initialNode.getType() == FTSParser.TERM &&
                        initialNode.getChildCount() == 1 &&
                        initialNode.getChild(0).getType() == FTSParser.STAR)
                {
                    // input is the lone star
                    Tree node = replacedNode;
                    while (true)
                    {
                        if (node.getChildCount() == 1)
                        {
                            node = node.getChild(0);
                            if (node.getType() == FTSParser.TERM)
                            {
                                assertEquals("Lone star should be mapped to " + FTSQueryParser.VALUE_REPLACELONESTAR, node.getChildCount(), 2);
                                Tree child1 = node.getChild(0);
                                assertEquals("Lone star should be mapped to " + FTSQueryParser.VALUE_REPLACELONESTAR, child1.getType(), FTSParser.ID);
                                assertEquals("Lone star should be mapped to " + FTSQueryParser.VALUE_REPLACELONESTAR, child1.getText(), "T");
                                Tree child2 = node.getChild(1);
                                assertEquals("Lone star should be mapped to " + FTSQueryParser.VALUE_REPLACELONESTAR, child2.getType(), FTSParser.FIELD_REF);
                                assertEquals("Lone star should be mapped to " + FTSQueryParser.VALUE_REPLACELONESTAR, child2.getChild(0).getText(), "ISNODE");
                                // checking done
                                break;
                            }
                        }
                        else
                        {
                            // wrong structure of the replaced node
                            fail("Lone star should be mapped to " + FTSQueryParser.VALUE_REPLACELONESTAR);
                        }
                    }
                }
            }
            
            @Override
            protected CommonTree build(CommonTree fieldReferenceNode, CommonTree argNode, QueryModelFactory factory, FunctionEvaluationContext functionEvaluationContext,
                    Selector selector, Map<String, Column> columnMap, Map<String, CommonTree> templateTrees, String defaultField)
            {
                CommonTree testNode = super.build(fieldReferenceNode, argNode, factory, functionEvaluationContext, selector, columnMap, templateTrees, defaultField);
                test(argNode, testNode);
                return testNode;
            }
        }

        FTSQueryParser.setTestNodeBuilder(new TestMock());
        try
        {
            FTSQueryParser.buildFTS(ftsExpression, new LuceneQueryModelFactory(), functionContext, null, null,
                    mode, defaultFieldConnective, templates, keywordsKey, FTSQueryParser.RerankPhase.SINGLE_PASS);
        }
        finally
        {
            // set default logic
            FTSQueryParser.setTestNodeBuilder(new FTSQueryParser.TestNodeBuilder());
        }
    }

}
