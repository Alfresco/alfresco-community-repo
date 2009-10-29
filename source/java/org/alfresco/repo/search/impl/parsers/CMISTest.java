package org.alfresco.repo.search.impl.parsers;

import java.io.IOException;
import java.io.InputStream;

import junit.framework.TestCase;

import org.alfresco.repo.node.BaseNodeServiceTest;
import org.antlr.gunit.GrammarInfo;
import org.antlr.gunit.gUnitExecutor;
import org.antlr.gunit.gUnitLexer;
import org.antlr.gunit.gUnitParser;
import org.antlr.runtime.ANTLRInputStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;

public class CMISTest extends TestCase
{
    public CMISTest()
    {
        // TODO Auto-generated constructor stub
    }

    public CMISTest(String name)
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
        InputStream modelStream = cl.getResourceAsStream("org/alfresco/repo/search/impl/parsers/cmis_test.gunit");

        CharStream input = new ANTLRInputStream(modelStream);
        
        
        gUnitExecutor executer = new gUnitExecutor(parse(input));
        
        System.out.print(executer.execTest());  // unit test result
        
        assertEquals("Failures ", 0, executer.failures.size()); 
        assertEquals("Invalids ", 0, executer.invalids.size()); 
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
