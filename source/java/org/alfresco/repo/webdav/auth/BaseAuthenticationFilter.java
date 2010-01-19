/*
 * Copyright (C) 2005-2009 Alfresco Software Limited.
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
 * FLOSS exception.  You should have received a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.webdav.auth;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.SessionUser;
import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;

/**
 * A base class for authentication filters. Handles management of the session user.
 * 
 * @author dward
 */
public abstract class BaseAuthenticationFilter
{
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
        String sessionAttrib = getUserAttributeName(); 
        HttpSession session = httpServletRequest.getSession();
        SessionUser sessionUser = (SessionUser) session.getAttribute(sessionAttrib);
        if (sessionUser != null)
        {
            try
            {
                authenticationService.validate(sessionUser.getTicket(), session.getId());
                setExternalAuth(session, externalAuth);
            }
            catch (AuthenticationException e)
            {
                // The ticket may have expired or the person could have been removed
                invalidateSession(httpServletRequest);
                sessionUser = null;
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
                return transactionService.getRetryingTransactionHelper().doInTransaction(callback);
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
     * Return the logger.
     * 
     * @return Log
     */
    protected abstract Log getLogger();

}