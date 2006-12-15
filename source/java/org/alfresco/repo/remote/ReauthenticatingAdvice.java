/**
 * 
 */
package org.alfresco.repo.remote;

import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.framework.ReflectiveMethodInvocation;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

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
                    ClientTicketHolder.SetTicket(ticket);
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
