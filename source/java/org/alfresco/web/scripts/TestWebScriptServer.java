/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.web.scripts;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.transaction.TransactionService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSource;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;


/**
 * Stand-alone Web Script Test Server
 * 
 * @author davidc
 */
public class TestWebScriptServer
{
    // dependencies
    protected TransactionService transactionService;
    protected DeclarativeWebScriptRegistry registry;
    
    /** The reader for interaction. */
    private BufferedReader fIn;
    
    /** Last command issued */
    private String lastCommand = null;

    /** Current user */
    private String username = "admin";
    
    /** I18N Messages */
    private MessageSource m_messages;    
    
    
    /**
     * Sets the transaction service
     * 
     * @param transactionService
     */
    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }
    
    /**
     * Sets the Web Script Registry
     * 
     * @param registry
     */
    public void setRegistry(DeclarativeWebScriptRegistry registry)
    {
        this.registry = registry;
    }
    
    /**
     * Sets the Messages resource bundle
     * 
     * @param messages
     * @throws IOException
     */
    public void setMessages(MessageSource messages)
        throws IOException
    {
        this.m_messages = messages;
    }

    
    /**
     * Initialise the Test Web Script Server
     * 
     * @throws Exception
     */
    public void init()
    {
        registry.initWebScripts();
    }
    
    /**
     * Main entry point.
     */
    public static void main(String[] args)
    {
        try
        {
            TestWebScriptServer testServer = getTestServer();
            testServer.rep();
        }
        catch(Throwable e)
        {
            StringWriter strWriter = new StringWriter();
            PrintWriter printWriter = new PrintWriter(strWriter);
            e.printStackTrace(printWriter);
            System.out.println(strWriter.toString());
        }
        finally
        {
            System.exit(0);
        }
    }

    /**
     * Retrieve an instance of the TestWebScriptServer
     *  
     * @return  Test Server
     */
    public static TestWebScriptServer getTestServer()
    {
        String[] CONFIG_LOCATIONS = new String[] { "classpath:alfresco/application-context.xml", "classpath:alfresco/web-scripts-application-context.xml", "classpath:alfresco/web-scripts-application-context-test.xml" };
        ApplicationContext context = new ClassPathXmlApplicationContext(CONFIG_LOCATIONS);
        TestWebScriptServer testServer = (TestWebScriptServer)context.getBean("webscripts.test");
        testServer.init();
        return testServer;
    }
    
    /**
     * Submit a Web Script Request
     * 
     * @param method  http method
     * @param uri  web script uri (relative to /alfresco/service)
     * @return  response
     * @throws IOException
     */
    public MockHttpServletResponse submitRequest(String method, String uri)
        throws IOException
    {
        MockHttpServletRequest req = createRequest("get", uri);
        MockHttpServletResponse res = new MockHttpServletResponse();
        
        WebScriptMatch match = registry.findWebScript(req.getMethod(), uri);
        if (match == null)
        {
            throw new WebScriptException("No service bound to uri '" + uri + "'");
        }
    
        WebScriptRequest apiReq = new WebScriptRequest(req, match);
        WebScriptResponse apiRes = new WebScriptResponse(res);
        match.getWebScript().execute(apiReq, apiRes);
        return res;
    }
    
    /**
     * A Read-Eval-Print loop.
     */
    /*package*/ void rep()
    {
        // accept commands
        fIn = new BufferedReader(new InputStreamReader(System.in));
        while (true)
        {
            System.out.print("ok> ");
            try
            {
                // get command
                final String line = fIn.readLine();
                if (line.equals("exit") || line.equals("quit"))
                {
                    return;
                }
                
                // execute command in context of currently selected user
                long startms = System.currentTimeMillis();
                System.out.print(interpretCommand(line));
                System.out.println("" + (System.currentTimeMillis() - startms) + "ms");
            }
            catch (Exception e)
            {
                e.printStackTrace(System.err);
                System.out.println("");
            }
        }
    }
    
    /**
     * Interpret a single command using the BufferedReader passed in for any data needed.
     * 
     * @param line The unparsed command
     * @return The textual output of the command.
     */
    private String interpretCommand(final String line)
        throws IOException
    {
        // execute command in context of currently selected user
        return AuthenticationUtil.runAs(new RunAsWork<String>()
        {
            @SuppressWarnings("synthetic-access")
            public String doWork() throws Exception
            {
                return executeCommand(line);
            }
        }, username);
    }
    
    /**
     * Execute a single command using the BufferedReader passed in for any data needed.
     * 
     * TODO: Use decent parser!
     * 
     * @param line The unparsed command
     * @return The textual output of the command.
     */
    private String executeCommand(String line)
        throws IOException
    {
        String[] command = line.split(" ");
        if (command.length == 0)
        {
            command = new String[1];
            command[0] = line;
        }
        
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(bout);

        // repeat last command?
        if (command[0].equals("r"))
        {
            if (lastCommand == null)
            {
                return "No command entered yet.";
            }
            return "repeating command " + lastCommand + "\n\n" + interpretCommand(lastCommand);
        }
        
        // remember last command
        lastCommand = line;

        // execute command
        if (command[0].equals("help"))
        {
            String helpFile = m_messages.getMessage("testserver.help", null, null);
            ClassPathResource helpResource = new ClassPathResource(helpFile);
            byte[] helpBytes = new byte[500];
            InputStream helpStream = helpResource.getInputStream();
            try
            {
                int read = helpStream.read(helpBytes);
                while (read != -1)
                {
                    bout.write(helpBytes, 0, read);
                    read = helpStream.read(helpBytes);
                }
            }
            finally
            {
                helpStream.close();
            }
        }
        
        else if (command[0].equals("user"))
        {
            if (command.length == 2)
            {
                username = command[1];
            }
            out.println("using user " + username);
        }
        
        else if (command[0].equals("get"))
        {
            if (command.length < 2)
            {
                return "Syntax Error.\n";
            }

            String uri = command[1];
            MockHttpServletResponse res = submitRequest("get", uri);
            bout.write(res.getContentAsByteArray());
            out.println();
        }
        
        else
        {
            return "Syntax Error.\n";
        }
 
        out.flush();
        String retVal = new String(bout.toByteArray());
        out.close();
        return retVal;
    }

    /**
     * Create a Mock HTTP Servlet Request
     * 
     * @param method
     * @param uri
     * @return  mock http servlet request
     */
    private MockHttpServletRequest createRequest(String method, String uri)
    {
        MockHttpServletRequest req = new MockHttpServletRequest("get", uri);

        // set parameters
        int iArgIndex = uri.indexOf('?');
        if (iArgIndex != -1 && iArgIndex != uri.length() -1)
        {
            String uriArgs = uri.substring(iArgIndex +1);
            String[] args = uriArgs.split("&");
            for (String arg : args)
            {
                String[] parts = arg.split("=");
                req.addParameter(parts[0], (parts.length == 2) ? parts[1] : null);
            }
        }
        
        // set paths
        req.setContextPath("/alfresco");
        req.setServletPath("/service");
        req.setPathInfo(iArgIndex == -1 ? uri : uri.substring(0, iArgIndex));
        
        return req;
    }
    
}
