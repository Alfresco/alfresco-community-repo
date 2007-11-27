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
package org.alfresco.repo.web.scripts;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.web.scripts.TestWebScriptServer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;


/**
 * Stand-alone Web Script Test Server
 * 
 * @author davidc
 */
public class TestWebScriptRepoServer extends TestWebScriptServer
{
    private RetryingTransactionHelper retryingTransactionHelper;
    private AuthenticationService authenticationService;
    
    
    /**
     * Sets helper that provides transaction callbacks
     */
    public void setTransactionHelper(RetryingTransactionHelper retryingTransactionHelper)
    {
        this.retryingTransactionHelper = retryingTransactionHelper;
    }
    
    /**
     * @param authenticationService
     */
    public void setAuthenticationService(AuthenticationService authenticationService)
    {
        this.authenticationService = authenticationService;
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
        String[] CONFIG_LOCATIONS = new String[]
        {
            "classpath:alfresco/application-context.xml",
            "classpath:alfresco/web-framework-application-context.xml",
            "classpath:alfresco/web-scripts-application-context.xml",
            "classpath:alfresco/web-scripts-application-context-test.xml",
            "classpath:alfresco/web-client-application-context.xml"
        };
        ApplicationContext context = new ClassPathXmlApplicationContext(CONFIG_LOCATIONS);
        TestWebScriptServer testServer = (TestWebScriptRepoServer)context.getBean("webscripts.test");
        return testServer;
    }
    
    /**
     * Interpret a single command using the BufferedReader passed in for any data needed.
     * 
     * @param line The unparsed command
     * @return The textual output of the command.
     */
    @Override
    protected String interpretCommand(final String line)
        throws IOException
    {
        try
        {
            if (username.startsWith("TICKET_"))
            {
                try
                {
                    retryingTransactionHelper.doInTransaction(new RetryingTransactionCallback<Object>()
                    {
                        public Object execute() throws Exception
                        {
                            authenticationService.validate(username);
                            return null;
                        }
                    });
                    return executeCommand(line);
                }
                finally
                {
                    authenticationService.clearCurrentSecurityContext();
                }
            }
        }
        catch(AuthenticationException e)
        {
            executeCommand("user admin");
        }
        
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
            
}
