/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */
package org.alfresco.repo.search.impl.parsers;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import junit.framework.TestCase;

import org.alfresco.repo.node.BaseNodeServiceTest;
import org.antlr.gunit.GrammarInfo;
import org.antlr.gunit.gUnitLexer;
import org.antlr.gunit.gUnitParser;
import org.antlr.runtime.ANTLRInputStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonToken;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.apache.tools.ant.filters.StringInputStream;

public class CMIS_FTSTest extends TestCase
{
    public CMIS_FTSTest()
    {
        // TODO Auto-generated constructor stub
    }

    public CMIS_FTSTest(String name)
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
        ClassLoader cl = BaseNodeServiceTest.class.getClassLoader();
        InputStream modelStream = cl.getResourceAsStream("org/alfresco/repo/search/impl/parsers/cmis_fts_test.gunit");

        CharStream input = new ANTLRInputStream(modelStream);
        
        
        gUnitExecutor executer = new gUnitExecutor(parse(input), "FTS");
        
        System.out.print(executer.execTest());  // unit test result
        
        assertEquals("Failures ", 0, executer.failures.size()); 
        assertEquals("Invalids ", 0, executer.invalids.size()); 
    }

    public void testLexerOutput() throws IOException
    {
        CharStream input = new ANTLRInputStream(new StringInputStream("~woof^2"));
        FTSLexer lexer = new FTSLexer(input);
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        for(CommonToken token : (List<CommonToken>)tokenStream.getTokens())
        {
            System.out.println(token.toString());
        }
        
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

    
}
