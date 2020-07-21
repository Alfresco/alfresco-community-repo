/*
 * #%L
 * Alfresco Remote API
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
package org.alfresco.repo.web.scripts.servlet;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.alfresco.error.ExceptionStackUtil;
import org.alfresco.repo.SessionUser;
import org.alfresco.repo.management.subsystems.ActivateableBean;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.external.RemoteUserMapper;
import org.alfresco.repo.web.auth.AuthenticationListener;
import org.alfresco.repo.web.auth.TicketCredentials;
import org.alfresco.repo.web.auth.WebCredentials;
import org.alfresco.repo.webdav.auth.AuthenticationDriver;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.Authenticator;
import org.springframework.extensions.webscripts.Description.RequiredAuthentication;
import org.springframework.extensions.webscripts.WebScript;
import org.springframework.extensions.webscripts.servlet.WebScriptServletRequest;
import org.springframework.extensions.webscripts.servlet.WebScriptServletResponse;

import net.sf.acegisecurity.DisabledException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Authenticator to provide Remote User based Header authentication dropping back to Basic Auth otherwise. 
 * Statelessly authenticating via a secure header now does not require a Session so can be used with
 * request-level load balancers which was not previously possible.
 * <p>
 * @see web-scripts-application-context.xml and web.xml - bean id 'webscripts.authenticator.remoteuser'
 * <p>
 * This authenticator can be bound to /service and does not require /wcservice (Session) mapping.
 * 
 * @since 5.1
 * @author Kevin Roast
 */
public class RemoteUserAuthenticatorFactory extends BasicHttpAuthenticatorFactory
{
    private static Log logger = LogFactory.getLog(RemoteUserAuthenticatorFactory.class);
    public static final long GET_REMOTE_USER_TIMEOUT_MILLISECONDS_DEFAULT = 10000L; // 10 sec

    protected RemoteUserMapper remoteUserMapper;
    protected AuthenticationComponent authenticationComponent;

    private boolean alwaysAllowBasicAuthForAdminConsole = true;
    List<String> adminConsoleScriptFamilies;
    long getRemoteUserTimeoutMilliseconds = GET_REMOTE_USER_TIMEOUT_MILLISECONDS_DEFAULT;

    public void setRemoteUserMapper(RemoteUserMapper remoteUserMapper)
    {
        this.remoteUserMapper = remoteUserMapper;
    }
    
    public void setAuthenticationComponent(AuthenticationComponent authenticationComponent)
    {
        this.authenticationComponent = authenticationComponent;
    }

    public boolean isAlwaysAllowBasicAuthForAdminConsole()
    {
        return alwaysAllowBasicAuthForAdminConsole;
    }

    public void setAlwaysAllowBasicAuthForAdminConsole(boolean alwaysAllowBasicAuthForAdminConsole)
    {
        this.alwaysAllowBasicAuthForAdminConsole = alwaysAllowBasicAuthForAdminConsole;
    }

    public List<String> getAdminConsoleScriptFamilies()
    {
        return adminConsoleScriptFamilies;
    }

    public void setAdminConsoleScriptFamilies(List<String> adminConsoleScriptFamilies)
    {
        this.adminConsoleScriptFamilies = adminConsoleScriptFamilies;
    }

    public long getGetRemoteUserTimeoutMilliseconds()
    {
        return getRemoteUserTimeoutMilliseconds;
    }

    public void setGetRemoteUserTimeoutMilliseconds(long getRemoteUserTimeoutMilliseconds)
    {
        this.getRemoteUserTimeoutMilliseconds = getRemoteUserTimeoutMilliseconds;
    }

    @Override
    public Authenticator create(WebScriptServletRequest req, WebScriptServletResponse res)
    {
        return new RemoteUserAuthenticator(req, res, this.listener);
    }

    /**
     * Remote User authenticator - adds header authentication onto Basic Auth. Stateless does not require Session.
     * 
     * @author Kevin Roast
     */
    public class RemoteUserAuthenticator extends BasicHttpAuthenticator
    {
        public RemoteUserAuthenticator(WebScriptServletRequest req, WebScriptServletResponse res, AuthenticationListener listener)
        {
            super(req, res, listener);
        }
        
        @Override
        public boolean authenticate(RequiredAuthentication required, boolean isGuest)
        {
            boolean authenticated = false;

            if (logger.isTraceEnabled())
            {
                logger.trace("Authenticate level required: " + required + " is guest: " + isGuest);
            }

            String userId = null;
            if (isRemoteUserMapperActive())
            {
                if (isAlwaysAllowBasicAuthForAdminConsole())
                {
                    final boolean useTimeoutForAdminAccessingAdminConsole = shouldUseTimeoutForAdminAccessingAdminConsole(required, isGuest);

                    if (useTimeoutForAdminAccessingAdminConsole && isBasicAuthHeaderPresentForAdmin())
                    {
                        return callBasicAuthForAdminConsoleAccess(required, isGuest);
                    }
                    try
                    {
                        userId = getRemoteUserWithTimeout(useTimeoutForAdminAccessingAdminConsole);
                    }
                    catch (AuthenticationTimeoutException e)
                    {
                        //return basic auth challenge
                        return false;
                    }
                }
                else
                {
                    // retrieve the remote user if configured and available - authenticate that user directly
                    userId = getRemoteUser();
                }
            }

            if (userId != null)
            {
                try
                {
                    authenticationComponent.setCurrentUser(userId);
                    listener.userAuthenticated(new TicketCredentials(authenticationService.getCurrentTicket()));
                    authenticated = true;
                }
                catch (AuthenticationException authErr)
                {
                    // don't propagate if the user is disabled
                    Throwable disabledCause = ExceptionStackUtil.getCause(authErr, DisabledException.class);
                    if(disabledCause != null)
                    {
                    	listener.authenticationFailed(new WebCredentials() {});
                    }
                    else
                    {
                        throw authErr;
                    }
                }
            }
            else
            {
                // is there a Session which might contain a valid user ticket?
                HttpSession session = servletReq.getHttpServletRequest().getSession(false);
                if (session != null)
                {
                    try
                    {
                        SessionUser user = (SessionUser)session.getAttribute(AuthenticationDriver.AUTHENTICATION_USER);
                        if (user != null)
                        {
                            // Validate the ticket for the current SessionUser
                            authenticationService.validate(user.getTicket());
                            if (logger.isDebugEnabled())
                            {
                                logger.debug("Ticket is valid. Retaining cached user in session.");
                            }
                            listener.userAuthenticated(new TicketCredentials(user.getTicket()));
                            authenticated = true;
                        }
                        else
                        {
                            authenticated = super.authenticate(required, isGuest);
                        }
                    }
                    catch (AuthenticationException authErr)
                    {
                        if (logger.isDebugEnabled())
                        {
                            logger.debug("An Authentication error occur. Removing User session.", authErr);
                        }
                        session.removeAttribute(AuthenticationDriver.AUTHENTICATION_USER);
                        session.invalidate();
                        listener.authenticationFailed(new WebCredentials() {});
                    }
                }
                else
                {
                    authenticated = super.authenticate(required, isGuest);
                }
            }
            return authenticated;
        }

        private boolean callBasicAuthForAdminConsoleAccess(RequiredAuthentication required, boolean isGuest)
        {
            // return REST call, after a timeout/basic auth challenge
            if (logger.isTraceEnabled())
            {
                logger.trace("An Admin Console request has come in with Basic Auth headers present for an admin user.");
            }
            // In order to prompt for another password, in case it was not entered correctly,
            // the output of this method should be returned by the calling "authenticate" method;
            // This would also mean, that once the admin basic auth header is present,
            // the authentication chain will not be used for the admin console access
            return super.authenticate(required, isGuest);
        }

        private boolean shouldUseTimeoutForAdminAccessingAdminConsole(RequiredAuthentication required, boolean isGuest)
        {
            boolean useTimeoutForAdminAccessingAdminConsole = RequiredAuthentication.admin.equals(required) && !isGuest &&
                servletReq.getServiceMatch() != null && isAdminConsoleWebScript(servletReq.getServiceMatch().getWebScript());

            if (logger.isTraceEnabled())
            {
                logger.trace("Should ensure that the admins can login with basic auth: " + useTimeoutForAdminAccessingAdminConsole);
            }
            return useTimeoutForAdminAccessingAdminConsole;
        }

        private boolean isRemoteUserMapperActive()
        {
            return remoteUserMapper != null && (!(remoteUserMapper instanceof ActivateableBean) || ((ActivateableBean) remoteUserMapper).isActive());
        }

        protected boolean isAdminConsoleWebScript(WebScript webScript)
        {
            if (webScript == null || adminConsoleScriptFamilies == null || webScript.getDescription() == null
                || webScript.getDescription().getFamilys() == null)
            {
                return false;
            }

            if (logger.isTraceEnabled())
            {
                logger.trace("WebScript: " + webScript + " has these families: " + webScript.getDescription().getFamilys());
            }

            // intersect the "family" sets defined
            Set<String> families = new HashSet<String>(webScript.getDescription().getFamilys());
            families.retainAll(adminConsoleScriptFamilies);
            final boolean isAdminConsole = !families.isEmpty();

            if (logger.isTraceEnabled() && isAdminConsole)
            {
                logger.trace("Detected an Admin Console webscript: " + webScript );
            }

            return isAdminConsole;
        }

        protected String getRemoteUserWithTimeout(boolean useTimeout) throws AuthenticationTimeoutException
        {
            if (!useTimeout)
            {
                return getRemoteUser();
            }

            String returnedRemoteUser = null;
            GetRemoteUserRunnable getRemoteUserRunnable = new GetRemoteUserRunnable();
            Thread workerGettingTheRemoteUser = new Thread(getRemoteUserRunnable);
            workerGettingTheRemoteUser.start();
            try
            {
                synchronized (workerGettingTheRemoteUser)
                {
                    workerGettingTheRemoteUser.join(getRemoteUserTimeoutMilliseconds);
                }
            }
            catch (Exception e)
            {
                logger.warn("Exception trying to get the remote user: " + e.getMessage(), e);
            }

            returnedRemoteUser = getRemoteUserRunnable.getReturnedRemoteUser();

            if (workerGettingTheRemoteUser.isAlive())
            {
                // we timed out
                // we should request basic authentication as the chain can't be usable
                cleanupThread(workerGettingTheRemoteUser);

                final String message = "Could not get the remote user in a reasonable time: " + getRemoteUserTimeoutMilliseconds + " milliseconds. "
                    + "Adjust the timeout property 'authentication.getRemoteUserTimeoutMilliseconds' if required.";

                if (logger.isWarnEnabled())
                {
                    logger.warn("Returning basic auth challenge for Admin Console. Cause: " + message);
                }
                HttpServletResponse res = servletRes.getHttpServletResponse();
                res.setStatus(401);
                res.setHeader("WWW-Authenticate", "Basic realm=\"Alfresco\"");

                throw new AuthenticationTimeoutException(message);
            }
            return returnedRemoteUser;
        }

        private void cleanupThread(Thread workerGettingTheRemoteUser)
        {
            try
            {
                // try to clean up the thread we created, to use resources optimally
                workerGettingTheRemoteUser.interrupt();
            }
            catch (Exception e)
            {
                // we can't really handle anything here
            }
        }

        /**
         * Retrieve the remote user from servlet request header when using a secure connection.
         * The RemoteUserMapper bean must be active and configured.
         * 
         * @return remote user ID or null if not active or found
         */
        protected String getRemoteUser()
        {
            String userId = null;
            
            // If the remote user mapper is configured, we may be able to map in an externally authenticated user
            if (isRemoteUserMapperActive())
            {
                userId = remoteUserMapper.getRemoteUser(this.servletReq.getHttpServletRequest());
            }

            logRemoteUserID(userId);

            return userId;
        }

        private void logRemoteUserID(String userId)
        {
            if (logger.isDebugEnabled())
            {
                String message = (userId == null) ?
                    "No external user ID in request." :
                    "Extracted external user ID from request: " + AuthenticationUtil.maskUsername(userId);
                logger.debug(message);
            }
        }

        class GetRemoteUserRunnable implements Runnable
        {
            private volatile String returnedRemoteUser;

            @Override
            public void run()
            {
                returnedRemoteUser = getRemoteUser();
            }

            public String getReturnedRemoteUser()
            {
                return returnedRemoteUser;
            }
        }

    }
}

class AuthenticationTimeoutException extends Exception
{
    static final long serialVersionUID = -3387511013124229948L;

    public AuthenticationTimeoutException()
    {
        super();
    }

    public AuthenticationTimeoutException(String message)
    {
        super(message);
    }

    public AuthenticationTimeoutException(String message, Throwable t)
    {
        super(message, t);
    }
}
