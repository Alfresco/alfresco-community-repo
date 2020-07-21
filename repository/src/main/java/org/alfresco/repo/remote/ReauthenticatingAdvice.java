/*
 * #%L
 * Alfresco Repository
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
package org.alfresco.repo.remote;

import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.framework.ReflectiveMethodInvocation;

/**
 * This is an interceptor that continuosly tries to reauthenticate when
 * a method call results in an AuthenticationException.
 * @author britt
 */
public class ReauthenticatingAdvice implements MethodInterceptor   
{
    /**
     * The authentication service reference.
     */
    private AuthenticationService fAuthService;
    
    /**
     * The ticket holder.
     */
    private ClientTicketHolder fTicketHolder;
    
    /**
     * The user name.
     */
    private String fUser;
    
    /**
     * The user's password.
     */
    private String fPassword;
    
    /**
     * The time in milliseconds to wait between attempts to reauthenticate.
     */
    private long fRetryInterval;
    
    /**
     * Default constructor.
     */
    public ReauthenticatingAdvice()
    {
        super();
    }

    /**
     * Setter.
     */
    public void setAuthenticationService(AuthenticationService service)
    {
        fAuthService = service;
    }

    /**
     * Setter.
     */
    public void setClientTicketHolder(ClientTicketHolder ticketHolder)
    {
        fTicketHolder = ticketHolder;
    }
    
    /**
     * Setter.
     */
    public void setUser(String user)
    {
        fUser = user;
    }
    
    /**
     * Setter.
     */
    public void setPassword(String password)
    {
        fPassword = password;
    }
    
    /**
     * Setter.
     */
    public void setRetryInterval(long retryInterval)
    {
        fRetryInterval = retryInterval;
    }
    
    /* (non-Javadoc)
     * @see org.aopalliance.intercept.MethodInterceptor#invoke(org.aopalliance.intercept.MethodInvocation)
     */
    public Object invoke(MethodInvocation mi) throws Throwable 
    {
        while (true)
        {
            try
            {
                MethodInvocation clone = ((ReflectiveMethodInvocation)mi).invocableClone();
                return clone.proceed();
            }
            catch (AuthenticationException ae)
            {
                // Sleep for an interval and try again.
                try
                {
                    Thread.sleep(fRetryInterval);
                }
                catch (InterruptedException ie)
                {
                    // Do nothing.
                }
                try
                {
                    // Reauthenticate.
                    fAuthService.authenticate(fUser, fPassword.toCharArray());
                    String ticket = fAuthService.getCurrentTicket();
                    fTicketHolder.setTicket(ticket);
                    // Modify the ticket argument.
                    mi.getArguments()[0] = ticket;
                }
                catch (Exception e)
                {
                    // Do nothing.
                }
            }
        }
    }
}
