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
package org.alfresco.repo.admin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.transaction.TransactionService;
import org.springframework.extensions.surf.util.AbstractLifecycleBean;
import org.alfresco.util.ApplicationContextHelper;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;

/**
 * An interactive console
 *
 */
public abstract class BaseInterpreter extends AbstractLifecycleBean
{
    // dependencies
    protected TransactionService transactionService;
    protected AuthorityService authorityService;

    /**4
     * The reader for interaction.
     */
    private BufferedReader fIn;

    /**
     * Current context
     */
    private String username = null;       
    
    /**
     * Last command issued
     */
    protected String lastCommand = null;
    
    
    public static void runMain(String beanName)
    {
        ApplicationContext context = ApplicationContextHelper.getApplicationContext();
        runMain(context, beanName);
    }
    
    public static void runMain(ApplicationContext context, String beanName)
    {
        BaseInterpreter console = getConsoleBean(context, beanName);

        console.username = AuthenticationUtil.getAdminUserName();
        console.rep();
        System.exit(0);
    }
    
    public static BaseInterpreter getConsoleBean(ApplicationContext context, String beanName)
    {
        return (BaseInterpreter)context.getBean(beanName);
    }

    /**
     * Make up a new console.
     */
    public BaseInterpreter()
    {
        fIn = new BufferedReader(new InputStreamReader(System.in));
    }
    
    
    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }
    
    public void setAuthorityService(AuthorityService authorityService)
    {
        this.authorityService = authorityService;
    }
    


    /**
     * A Read-Eval-Print loop.
     */
    public void rep()
    {
        // accept commands
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
            catch (Throwable t)
            {
                t.printStackTrace(System.err);
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
    public String interpretCommand(final String line)
        throws IOException
    {
        String currentUserName = getCurrentUserName();
        if (hasAuthority(currentUserName))
        {        
            // execute command in context of currently selected user
            return AuthenticationUtil.runAs(new RunAsWork<String>()
            {
                public String doWork() throws Exception
                {
                    RetryingTransactionCallback<String> txnWork = new RetryingTransactionCallback<String>()
                    {
                        public String execute() throws Exception
                        {
                            return executeCommand(line);
                        }
                    };
                    return transactionService.getRetryingTransactionHelper().doInTransaction(txnWork);
                }
            }, currentUserName);
        }
        else
        {
            return("Error: User '"+ currentUserName + "' not authorised");
        }
    }
    
    protected boolean hasAuthority(String username)
    {
        if (authorityService == null)
        {
            // default for backwards compatibility - eg. upgrade of existing MT instance (mt-admin-context.xml.sample)
            authorityService = (AuthorityService)getApplicationContext().getBean("AuthorityService");
        }
        
        return ((username != null) && (authorityService.isAdminAuthority(username)));
    }

    /**
     * Execute a single command using the BufferedReader passed in for any data needed.
     *
     * TODO: Use decent parser!
     *
     * @param line The unparsed command
     * @return The textual output of the command.
     */
    protected abstract String executeCommand(String line) throws IOException;
  
    /**
     * Get current user name
     * 
     * @return  user name
     */
    public String getCurrentUserName()
    {
        if (username == null)
        {
            return AuthenticationUtil.getFullyAuthenticatedUser();
        }
        return username;
    }
    
    public void setCurrentUserName(String username)
    {
        this.username = username;
    }

    /* (non-Javadoc)
     * @see org.springframework.extensions.surf.util.AbstractLifecycleBean#onBootstrap(org.springframework.context.ApplicationEvent)
     */
    @Override
    protected void onBootstrap(ApplicationEvent event)
    {
        // NOOP
    }

    /* (non-Javadoc)
     * @see org.springframework.extensions.surf.util.AbstractLifecycleBean#onShutdown(org.springframework.context.ApplicationEvent)
     */
    @Override
    protected void onShutdown(ApplicationEvent event)
    {
        // NOOP
    }
}