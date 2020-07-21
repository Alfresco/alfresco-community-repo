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
/*
 [The "BSD licence"]
 Copyright (c) 2007-2008 Leon Jen-Yuan Su
 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions
 are met:
 1. Redistributions of source code must retain the above copyright
    notice, this list of conditions and the following disclaimer.
 2. Redistributions in binary form must reproduce the above copyright
    notice, this list of conditions and the following disclaimer in the
    documentation and/or other materials provided with the distribution.
 3. The name of the author may not be used to endorse or promote products
    derived from this software without specific prior written permission.

 THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.alfresco.repo.search.impl.parsers;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import org.antlr.gunit.GrammarInfo;
import org.antlr.gunit.gUnitTestInput;
import org.antlr.gunit.gUnitTestResult;
import org.antlr.runtime.ANTLRFileStream;
import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.Lexer;
import org.antlr.runtime.Token;
import org.antlr.runtime.TokenStream;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.TreeAdaptor;
import org.antlr.stringtemplate.StringTemplate;

public class gUnitExecutor extends org.antlr.gunit.gUnitExecutor
{
    private String testsuiteDir;

    private PrintStream console = System.out;

    private PrintStream consoleErr = System.err;

    public gUnitExecutor(GrammarInfo grammarInfo, String testsuiteDir)
    {
        super(grammarInfo, testsuiteDir);
        this.testsuiteDir = testsuiteDir;
    }

    // TODO: throw proper exceptions
    protected gUnitTestResult runParser(String parserName, String lexerName, String testRuleName, gUnitTestInput testInput) throws Exception
    {
        CharStream input;
        Class lexer = null;
        Class parser = null;
        PrintStream ps = null; // for redirecting stdout later
        PrintStream ps2 = null; // for redirecting stderr later
        try
        {
            /** Set up ANTLR input stream based on input source, file or String */
            input = getANTLRInputStream(testInput);

            /** Use Reflection to create instances of lexer and parser */
            lexer = classForName(lexerName);
            Class[] lexArgTypes = new Class[] { CharStream.class }; // assign type to lexer's args
            Constructor lexConstructor = lexer.getConstructor(lexArgTypes);
            Object[] lexArgs = new Object[] { input }; // assign value to lexer's args
            Object lexObj = lexConstructor.newInstance(lexArgs); // makes new instance of lexer

            CommonTokenStream tokens = new CommonTokenStream((Lexer) lexObj);

            parser = classForName(parserName);
            Class[] parArgTypes = new Class[] { TokenStream.class }; // assign type to parser's args
            Constructor parConstructor = parser.getConstructor(parArgTypes);
            Object[] parArgs = new Object[] { tokens }; // assign value to parser's args
            Object parObj = parConstructor.newInstance(parArgs); // makes new instance of parser

            // set up customized tree adaptor if necessary
            if (grammarInfo.getAdaptor() != null)
            {
                parArgTypes = new Class[] { TreeAdaptor.class };
                Method _setTreeAdaptor = parser.getMethod("setTreeAdaptor", parArgTypes);
                Class _treeAdaptor = classForName(grammarInfo.getAdaptor());
                _setTreeAdaptor.invoke(parObj, _treeAdaptor.newInstance());
            }

            Method ruleName = parser.getMethod(testRuleName);

            /** Start of I/O Redirecting */
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ByteArrayOutputStream err = new ByteArrayOutputStream();
            ps = new PrintStream(out);
            ps2 = new PrintStream(err);
            System.setOut(ps);
            System.setErr(ps2);
            /** End of redirecting */

            /** Invoke grammar rule, and store if there is a return value */
            Object ruleReturn = ruleName.invoke(parObj);
            String astString = null;
            String stString = null;
            /** If rule has return value, determine if it contains an AST or a ST */
            if (ruleReturn != null)
            {
                if (ruleReturn.getClass().toString().indexOf(testRuleName + "_return") > 0)
                {
                    try
                    { // NullPointerException may happen here...
                        Class _return = classForName(parserName + "$" + testRuleName + "_return");
                        Method[] methods = _return.getDeclaredMethods();
                        for (Method method : methods)
                        {
                            if (method.getName().equals("getTree"))
                            {
                                Method returnName = _return.getMethod("getTree");
                                CommonTree tree = (CommonTree) returnName.invoke(ruleReturn);
                                astString = tree.toStringTree();
                            }
                            else if (method.getName().equals("getTemplate"))
                            {
                                Method returnName = _return.getMethod("getTemplate");
                                StringTemplate st = (StringTemplate) returnName.invoke(ruleReturn);
                                stString = st.toString();
                            }
                        }
                    }
                    catch (Exception e)
                    {
                        System.err.println(e); // Note: If any exception occurs, the test is viewed as failed.
                    }
                }
            }

            // Count tokens EOF only once ....
            int count = 0;
            boolean foundEof = false;
            for (Token token : (List<Token>) tokens.getTokens())
            {
                if (!foundEof)
                {
                    if (token.getType() == Token.EOF)
                    {
                        foundEof = true;
                        if(testRuleName.equals("ftsQuery") || testRuleName.equals("query"))
                        {
                            count++;
                        }
                    }
                    else
                    {
                        count++;
                    }
                   
                }
            }

            /** Invalid input */
            if (tokens.index() != count)
            {
                // throw new InvalidInputException();
                ps2.print("Invalid input");
            }

            if (err.toString().length() > 0)
            {
                gUnitTestResult testResult = new gUnitTestResult(false, err.toString());
                testResult.setError(err.toString());
                return testResult;
            }
            String stdout = null;
            // TODO: need to deal with the case which has both ST return value and stdout
            if (out.toString().length() > 0)
            {
                stdout = out.toString();
            }
            if (astString != null)
            { // Return toStringTree of AST
                return new gUnitTestResult(true, stdout, astString);
            }
            else if (stString != null)
            {// Return toString of ST
                return new gUnitTestResult(true, stdout, stString);
            }

            if (ruleReturn != null)
            {
                // TODO: currently only works for a single return with int or String value
                return new gUnitTestResult(true, stdout, String.valueOf(ruleReturn));
            }
            return new gUnitTestResult(true, stdout, stdout);
        }
        catch (IOException e)
        {
            return getTestExceptionResult(e);
        }
        catch (ClassNotFoundException e)
        {
            e.printStackTrace();
            System.exit(1);
        }
        catch (SecurityException e)
        {
            e.printStackTrace();
            System.exit(1);
        }
        catch (NoSuchMethodException e)
        {
            e.printStackTrace();
            System.exit(1);
        }
        catch (IllegalArgumentException e)
        {
            e.printStackTrace();
            System.exit(1);
        }
        catch (InstantiationException e)
        {
            e.printStackTrace();
            System.exit(1);
        }
        catch (IllegalAccessException e)
        {
            e.printStackTrace();
            System.exit(1);
        }
        catch (InvocationTargetException e)
        { // This exception could be caused from ANTLR Runtime Exception, e.g. MismatchedTokenException
            return getTestExceptionResult(e);
        }
        finally
        {
            try
            {
                if (ps != null)
                    ps.close();
                if (ps2 != null)
                    ps2.close();
                System.setOut(console); // Reset standard output
                System.setErr(consoleErr); // Reset standard err out
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        // TODO: verify this:
        throw new Exception("This should be unreachable?");
    }

    // Create ANTLR input stream based on input source, file or String
    private CharStream getANTLRInputStream(gUnitTestInput testInput) throws IOException
    {
        CharStream input;
        if (testInput.isFile)
        {
            String filePath = testInput.input;
            File testInputFile = new File(filePath);
            // if input test file is not found under the current dir, try to look for it from dir where the testsuite
            // file locates
            if (!testInputFile.exists())
            {
                testInputFile = new File(this.testsuiteDir, filePath);
                if (testInputFile.exists())
                    filePath = testInputFile.getCanonicalPath();
                // if still not found, also try to look for it under the package dir
                else if (grammarInfo.getGrammarPackage() != null)
                {
                    testInputFile = new File("." + File.separator + grammarInfo.getGrammarPackage().replace(".", File.separator), filePath);
                    if (testInputFile.exists())
                        filePath = testInputFile.getCanonicalPath();
                }
            }
            input = new ANTLRFileStream(filePath);
        }
        else
        {
            input = new ANTLRStringStream(testInput.input);
        }
        return input;
    }

    // set up the cause of exception or the exception name into a gUnitTestResult instance
    private gUnitTestResult getTestExceptionResult(Exception e)
    {
        gUnitTestResult testResult;
        if (e.getCause() != null)
        {
            testResult = new gUnitTestResult(false, e.getCause().toString(), true);
            testResult.setError(e.getCause().toString());
        }
        else
        {
            testResult = new gUnitTestResult(false, e.toString(), true);
            testResult.setError(e.toString());
        }
        return testResult;
    }

}
