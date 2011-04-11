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
package org.alfresco.repo.webdav.auth;

import java.io.IOException;
import java.io.Reader;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.SessionUser;
import org.alfresco.repo.management.subsystems.ActivateableBean;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * A base class for authentication filters. Handles management of the session user.
 * 
 * @author dward
 */
public abstract class BaseAuthenticationFilter
{
    /** Indication by an up-stream filter that no authentication checks are required. */
    protected static final String NO_AUTH_REQUIRED = "alfNoAuthRequired"; 

    /** The default session attribute used to cache the user. Subclasses may override this with {@link #setUserAttributeName(String)}. */
    public static final String AUTHENTICATION_USER = "_alfDAVAuthTicket";
    
    /** The session attribute that indicates external authentication. */
    private static final String LOGIN_EXTERNAL_AUTH = "_alfExternalAuth";
    
    /** The name of the ticket argument. */
    protected static final String ARG_TICKET = "ticket";
    
    /** The authentication service. */
    protected AuthenticationService authenticationService;
    
    /** The person service. */
    protected PersonService personService;
    
    /** The node service. */
    protected NodeService nodeService;
    
    /** The transaction service. */
    protected TransactionService transactionService;
    
    /** The authentication component. */
    protected AuthenticationComponent authenticationComponent;
    
    /** The remote user mapper. */
    protected RemoteUserMapper remoteUserMapper;

    /** The configured user attribute name. */
    private String userAttributeName = AUTHENTICATION_USER;

    

    /**
     * Sets the authentication service.
     * 
     * @param authenticationService
     *            the authService to set
     */
    public void setAuthenticationService(AuthenticationService authenticationService)
    {
        this.authenticationService = authenticationService;
    }

    /**
     * Sets the person service.
     * 
     * @param personService
     *            the personService to set
     */
    public void setPersonService(PersonService personService)
    {
        this.personService = personService;
    }

    /**
     * Sets the node service.
     * 
     * @param nodeService
     *            the nodeService to set
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * Sets the transaction service.
     * 
     * @param transactionService
     *            the transactionService to set
     */
    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }

    /**
     * Sets the authentication component.
     * 
     * @param authenticationComponent
     *            the authentication component
     */
    public void setAuthenticationComponent(AuthenticationComponent authenticationComponent)
    {
        this.authenticationComponent = authenticationComponent;
    }

    /**
     * Sets the remote user mapper.
     * 
     * @param remoteUserMapper
     *            the remote user mapper
     */
    public void setRemoteUserMapper(RemoteUserMapper remoteUserMapper)
    {
        this.remoteUserMapper = remoteUserMapper;
    }

    /**
     * Create the user object that will be stored in the session.
     * 
     * @param userName
     *            String
     * @param ticket
     *            String
     * @param personNode
     *            NodeRef
     * @param homeSpaceRef
     *            NodeRef
     * @return SessionUser
     */
    protected SessionUser createUserObject(String userName, String ticket, NodeRef personNode, NodeRef homeSpaceRef)
    {
        return new WebDAVUser(userName, ticket, homeSpaceRef);
    }

    /**
     * Callback to get the specific impl of the Session User for a filter.
     * 
     * @param servletContext
     *            the servlet context
     * @param httpServletRequest
     *            the http servlet request
     * @param httpServletResponse
     *            the http servlet response
     * @param externalAuth
     *            has the user been authenticated by SSO?
     * @return User from the session
     */
    protected SessionUser getSessionUser(ServletContext servletContext, final HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse, final boolean externalAuth)
    {
        String userId = null;

        // If the remote user mapper is configured, we may be able to map in an externally authenticated user
        if (remoteUserMapper != null
                && (!(remoteUserMapper instanceof ActivateableBean) || ((ActivateableBean) remoteUserMapper).isActive()))
        {
            userId = remoteUserMapper.getRemoteUser(httpServletRequest);
        }
        
        String sessionAttrib = getUserAttributeName(); 
        HttpSession session = httpServletRequest.getSession();
        SessionUser sessionUser = (SessionUser) session.getAttribute(sessionAttrib);
        if (sessionUser != null)
        {
            try
            {
                authenticationService.validate(sessionUser.getTicket());
                setExternalAuth(session, externalAuth);
            }
            catch (AuthenticationException e)
            {
                // The ticket may have expired or the person could have been removed
                invalidateSession(httpServletRequest);
                sessionUser = null;
            }
        }
        
        if (userId != null)
        {
            // We have a previously-cached user with the wrong identity - replace them
            if (sessionUser != null && !sessionUser.getUserName().equals(userId))
            {
                session.removeAttribute(sessionAttrib);
                session.invalidate();
                sessionUser = null;
            }

            if (sessionUser == null)
            {
               // If we have been authenticated by other means, just propagate through the user identity
               authenticationComponent.setCurrentUser(userId);
               session = httpServletRequest.getSession();

               try
               {
                   sessionUser = createUserEnvironment(session, authenticationService.getCurrentUserName(), authenticationService.getCurrentTicket(), true);
               }
               catch (Throwable e)
               {
                   if (getLogger().isDebugEnabled())
                       getLogger().debug("Error during ticket validation and user creation: " + e.getMessage(), e);
               }
            }
        }
        
        return sessionUser;
    }

    /**
     * Remove the user from the session and expire the session - after failed ticket auth.
     * 
     * @param session
     *            the session
     */
    protected void invalidateSession(HttpServletRequest req)
    {
        HttpSession session = req.getSession(false);
        if (session != null)
        {
            setExternalAuth(session, false);
            session.removeAttribute(getUserAttributeName());
            session.invalidate();
        }
    }
    
    /**
     * Executes a callback in a transaction as the system user
     * 
     * @param callback
     *            the callback
     * @return the return value from the callback
     */
    protected <T> T doInSystemTransaction(final RetryingTransactionHelper.RetryingTransactionCallback<T> callback)
    {
        return AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<T>()
        {
            public T doWork() throws Exception
            {
                return transactionService.getRetryingTransactionHelper().doInTransaction(callback, transactionService.isReadOnly());
            }
        }, AuthenticationUtil.SYSTEM_USER_NAME);
    }

    /**
     * Return the user object session attribute name.
     * 
     * @return the user object session attribute name
     */
    protected final String getUserAttributeName()
    {
    	return userAttributeName;
    }

    /**
     * Set the user object attribute name.
     * 
     * @param userAttr
     *            the user object session attribute name
     */
    protected final void setUserAttributeName(String userAttr)
    {
    	userAttributeName = userAttr;
    }

    /**
     * Callback to create the User environment as appropriate for a filter impl.
     * 
     * @param session
     *            HttpSession
     * @param userName
     *            String
     * @param ticket
     *            the ticket
     * @param externalAuth
     *            has the user been authenticated by SSO?
     * @return SessionUser
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws ServletException
     *             the servlet exception
     */
    protected SessionUser createUserEnvironment(HttpSession session, final String userName, final String ticket, boolean externalAuth)
            throws IOException, ServletException
    {
        SessionUser user = doInSystemTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<SessionUser>()
        {
            public SessionUser execute() throws Throwable
            {
                // Setup User object and Home space ID etc.
                final NodeRef personNodeRef = personService.getPerson(userName);

                String name = (String) nodeService.getProperty(personNodeRef, ContentModel.PROP_USERNAME);

                NodeRef homeSpaceRef = (NodeRef) nodeService.getProperty(personNodeRef, ContentModel.PROP_HOMEFOLDER);

                return createUserObject(name, ticket, personNodeRef, homeSpaceRef);
            }
        });

        // Store the user on the session
        session.setAttribute(getUserAttributeName(), user);        
        setExternalAuth(session, externalAuth);
        return user;
    }

    private void setExternalAuth(HttpSession session, boolean externalAuth)
    {
        if (externalAuth)
        {
            session.setAttribute(LOGIN_EXTERNAL_AUTH, Boolean.TRUE);
        }
        else
        {
           session.removeAttribute(LOGIN_EXTERNAL_AUTH);
        }
    }

    /**
     * Callback to create the User environment as appropriate for a filter impl
     * 
     * @param session
     *            HttpSession
     * @param userName
     *            String
     * @return SessionUser
     * @throws IOException
     * @throws ServletException
     */
    protected SessionUser createUserEnvironment(final HttpSession session, final String userName) throws IOException,
            ServletException
    {
        return this.transactionService.getRetryingTransactionHelper().doInTransaction(
                new RetryingTransactionHelper.RetryingTransactionCallback<SessionUser>()
                {

                    public SessionUser execute() throws Throwable
                    {
                        authenticationComponent.setCurrentUser(userName);
                        return createUserEnvironment(session, userName, authenticationService.getCurrentTicket(), true);
                    }
                }, transactionService.isReadOnly());
    }

    /**
     * Return the logger.
     * 
     * @return Log
     */
    protected abstract Log getLogger();
    
    /**
     * Handles the login form directly, allowing management of the session user.
     * 
     * @param req
     *            the request
     * @param res
     *            the response
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws ServletException
     *             on error
     */
    protected void handleLoginForm(HttpServletRequest req, HttpServletResponse res) throws IOException,
            ServletException
    {
        // Invalidate current session
        HttpSession session = req.getSession(false);
        if (session != null)
        {
            session.invalidate();
        }
        StringBuilder out = new StringBuilder(1024);
        Reader in = req.getReader();
        char[] buff = new char[1024];
        int charsRead;
        while ((charsRead = in.read(buff)) != -1)
        {
            out.append(buff, 0, charsRead);
        }
        in.close();

        try
        {
            JSONObject json = new JSONObject(out.toString());
            String username = json.getString("username");
            String password = json.getString("password");

            if (username == null || username.length() == 0)
            {
                res.sendError(HttpServletResponse.SC_BAD_REQUEST, "Username not specified");
                return;
            }

            if (password == null)
            {
                res.sendError(HttpServletResponse.SC_BAD_REQUEST, "Password not specified");
                return;
            }

            authenticationService.authenticate(username, password.toCharArray());
            session = req.getSession();            
            createUserEnvironment(session, username, authenticationService.getCurrentTicket(), false);
            res.setStatus(HttpServletResponse.SC_NO_CONTENT);
        }
        catch (AuthenticationException e)
        {
            res.sendError(HttpServletResponse.SC_FORBIDDEN, "Login failed");
        }
        catch (JSONException jErr)
        {
            res.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unable to parse JSON POST body: " + jErr.getMessage());
        }
    }
}