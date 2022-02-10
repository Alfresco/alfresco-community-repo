/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.module.org_alfresco_module_rm.test.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.util.EqualsHelper;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.extensions.webscripts.TestWebScriptServer;

/**
 * Stand-alone Web Script Test Server
 *
 * @author davidc
 */
public class TestWebScriptRepoServer extends TestWebScriptServer
{
    /**
     * Main entry point.
     */
    public static void main(String[] args)
    {
        try
        {
            TestWebScriptServer testServer = getTestServer();
            AuthenticationUtil.setRunAsUserSystem();
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

    private final static String[] CONFIG_LOCATIONS = new String[]
    {
        "classpath:alfresco/application-context.xml",
        "classpath:alfresco/web-scripts-application-context.xml",
        "classpath:alfresco/web-scripts-application-context-test.xml"
    };

    /** A static reference to the application context being used */
    private static ClassPathXmlApplicationContext ctx;
    private static String appendedTestConfiguration;

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
     * Get default user name
     */
    protected String getDefaultUserName()
    {
        return AuthenticationUtil.getAdminUserName();
    }

    /**
     * {@inheritDoc #getTestServer(String)}
     */
    public static TestWebScriptServer getTestServer()
    {
        return getTestServer(null);
    }

    /**
     * Start up a context and get the server bean.
     * <p>
     * This method will close and restart the application context only if the configuration has
     * changed.
     *
     * @param appendTestConfigLocation      additional context file to include in the application context
     * @return  Test Server
     */
    public static synchronized TestWebScriptServer getTestServer(String appendTestConfigLocation)
    {
        if (TestWebScriptRepoServer.ctx != null)
        {
            boolean configChanged = !EqualsHelper.nullSafeEquals(
                    appendTestConfigLocation,
                    TestWebScriptRepoServer.appendedTestConfiguration);
            if (configChanged)
            {
                // The config changed, so close the context (it'll be restarted later)
                try
                {
                    ctx.close();
                    ctx = null;
                }
                catch (Throwable e)
                {
                    throw new RuntimeException("Failed to shut down existing application context", e);
                }
            }
            else
            {
                // There is already a context with the required configuration
            }
        }

        // Check if we need to start/restart the context
        if (TestWebScriptRepoServer.ctx == null)
        {
            // Restart it
            final String[] configLocations;
            if (appendTestConfigLocation == null)
            {
                configLocations = CONFIG_LOCATIONS;
            }
            else
            {
                configLocations = new String[CONFIG_LOCATIONS.length+1];
                System.arraycopy(CONFIG_LOCATIONS, 0, configLocations, 0, CONFIG_LOCATIONS.length);
                configLocations[CONFIG_LOCATIONS.length] = appendTestConfigLocation;
            }
            TestWebScriptRepoServer.ctx = new ClassPathXmlApplicationContext(configLocations);
            TestWebScriptRepoServer.appendedTestConfiguration = appendTestConfigLocation;
        }

        // Get the bean
        TestWebScriptServer testServer = (org.alfresco.repo.web.scripts.TestWebScriptRepoServer)TestWebScriptRepoServer.ctx.getBean("webscripts.test");
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
            executeCommand("user " + getDefaultUserName());
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
