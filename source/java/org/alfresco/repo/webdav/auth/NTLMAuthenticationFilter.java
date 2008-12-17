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
package org.alfresco.repo.webdav.auth;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.transaction.UserTransaction;

import org.alfresco.jlan.server.auth.ntlm.NTLM;
import org.alfresco.jlan.server.auth.ntlm.NTLMMessage;
import org.alfresco.jlan.server.auth.ntlm.Type1NTLMMessage;
import org.alfresco.jlan.server.auth.ntlm.Type3NTLMMessage;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.SessionUser;
import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * WebDav NTLM Authentication Filter Class
 * 
 * @author GKSpencer
 */
public class NTLMAuthenticationFilter extends BaseNTLMAuthenticationFilter
{
    // Authenticated user session object name
    public final static String AUTHENTICATION_USER = "_alfDAVAuthTicket";
    
    // Allow an authenitcation ticket to be passed as part of a request to bypass authentication
    private static final String ARG_TICKET = "ticket";
    
    // Debug logging
    private static Log logger = LogFactory.getLog(NTLMAuthenticationFilter.class);
    
    
    /**
     * Run the filter
     * 
     * @param sreq ServletRequest
     * @param sresp ServletResponse
     * @param chain FilterChain
     * @exception IOException
     * @exception ServletException
     */
    public void doFilter(ServletRequest sreq, ServletResponse sresp, FilterChain chain) throws IOException,
            ServletException
    {
        // Get the HTTP request/response/session
        HttpServletRequest req = (HttpServletRequest) sreq;
        HttpServletResponse resp = (HttpServletResponse) sresp;
        HttpSession httpSess = req.getSession(true);
        
        // Check if there is an authorization header with an NTLM security blob
        String authHdr = req.getHeader(AUTHORIZATION);
        boolean reqAuth = (authHdr != null && authHdr.startsWith(AUTH_NTLM));
        
        // Check if the user is already authenticated
        WebDAVUser user = (WebDAVUser) httpSess.getAttribute(AUTHENTICATION_USER);
        if (user != null && reqAuth == false)
        {
            try
            {
                if (logger.isDebugEnabled())
                    logger.debug("User " + user.getUserName() + " validate ticket");
                
                // Validate the user ticket
                m_authService.validate( user.getTicket());
                reqAuth = false;
            }
            catch (AuthenticationException ex)
            {
                if (logger.isErrorEnabled())
                    logger.error("Failed to validate user " + user.getUserName(), ex);
                
                reqAuth = true;
            }
        }

        // If the user has been validated and we do not require re-authentication then continue to
        // the next filter
        if (reqAuth == false && user != null)
        {
            if (logger.isDebugEnabled())
                logger.debug("Authentication not required, chaining ...");
            
            // Chain to the next filter
            chain.doFilter(sreq, sresp);
            return;
        }

        // Check the authorization header
        if (authHdr == null)
        {
        	// Check if the request includes an authentication ticket
        	String ticket = req.getParameter(ARG_TICKET);
        	if (ticket != null && ticket.length() != 0)
        	{
                if (logger.isDebugEnabled())
                    logger.debug("Logon via ticket from " + req.getRemoteHost() + " (" +
                            req.getRemoteAddr() + ":" + req.getRemotePort() + ")" + " ticket=" + ticket);
                
        		UserTransaction tx = null;
        		try
        		{
        		    // Validate the ticket
        		    m_authService.validate(ticket);
        		    
                    if (user == null)
                    {
            		    // Start a transaction
            		    tx = m_transactionService.getUserTransaction();
            		    tx.begin();
            		    
                        // Need to create the User instance if not already available
                        String currentUsername = m_authService.getCurrentUserName();
                        
            		    NodeRef personRef = m_personService.getPerson(currentUsername);
            		    user = new WebDAVUser(currentUsername, m_authService.getCurrentTicket(), personRef);
            		    NodeRef homeRef = (NodeRef)m_nodeService.getProperty(personRef, ContentModel.PROP_HOMEFOLDER);
            		    user.setHomeNode(homeRef);
            		    
            		    tx.commit();
            		    tx = null; 
            		    
            		    // Store the User object in the Session - the authentication servlet will then proceed
            		    req.getSession().setAttribute(AUTHENTICATION_USER, user);
                    }
        		    
        		    // Chain to the next filter
        		    chain.doFilter(sreq, sresp);
        		    return;
        		}
        		catch (AuthenticationException authErr)
            	{
            		if (logger.isDebugEnabled())
            		    logger.debug("Failed to authenticate user ticket: " + authErr.getMessage(), authErr);
            	}
            	catch (Throwable e)
            	{
            		if (logger.isDebugEnabled())
                        logger.debug("Error during ticket validation and user creation: " + e.getMessage(), e);
            	}
            	finally
            	{
            		try
            	    {
            			if (tx != null)
            	        {
            				tx.rollback();
           	        	}
            	    }
            	    catch (Exception tex)
            	    {
            	    }
            	}
        	}
        	
            if (logger.isDebugEnabled())
                logger.debug("New NTLM auth request from " + req.getRemoteHost() + " (" +
                        req.getRemoteAddr() + ":" + req.getRemotePort() + ")");
            
            // Send back a request for NTLM authentication
            restartLoginChallenge(resp, httpSess);
        }
        else
        {
            // Decode the received NTLM blob and validate
            final byte[] ntlmByts = Base64.decodeBase64(authHdr.substring(5).getBytes());
            int ntlmTyp = NTLMMessage.isNTLMType(ntlmByts);
            if (ntlmTyp == NTLM.Type1)
            {
                // Process the type 1 NTLM message
                Type1NTLMMessage type1Msg = new Type1NTLMMessage(ntlmByts);
                processType1(type1Msg, req, resp, httpSess);
            }
            else if (ntlmTyp == NTLM.Type3)
            {
                // Process the type 3 NTLM message
                Type3NTLMMessage type3Msg = new Type3NTLMMessage(ntlmByts);
                processType3(type3Msg, req, resp, httpSess, chain);
            }
            else
            {
                if (logger.isDebugEnabled())
                    logger.debug("NTLM blob not handled, restarting login challenge.");
                
                restartLoginChallenge(resp, httpSess);
            }
        }
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.webdav.auth.BaseNTLMAuthenticationFilter#getSessionUser(javax.servlet.http.HttpSession)
     */
    @Override
    protected SessionUser getSessionUser(HttpSession session)
    {
        return (SessionUser)session.getAttribute(AUTHENTICATION_USER);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.webdav.auth.BaseNTLMAuthenticationFilter#onValidate(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpSession)
     */
    @Override
    protected void onValidate(HttpServletRequest req, HttpSession session)
    {
        // nothing to do for webdav filter
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.webdav.auth.BaseNTLMAuthenticationFilter#onValidateFailed(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, javax.servlet.http.HttpSession)
     */
    @Override
    protected void onValidateFailed(HttpServletRequest req, HttpServletResponse res, HttpSession session)
        throws IOException
    {
        // restart the login challenge process if validation fails
        restartLoginChallenge(res, session);
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.webdav.auth.BaseNTLMAuthenticationFilter#createUserEnvironment(javax.servlet.http.HttpSession, java.lang.String)
     */
    @Override
    protected SessionUser createUserEnvironment(HttpSession session, String userName)
        throws IOException, ServletException
    {
        Log logger = getLogger();
        
        SessionUser user = null;
        
        UserTransaction tx = m_transactionService.getUserTransaction();
        
        try
        {
            tx.begin();
            
            // Get user details for the authenticated user
            m_authComponent.setCurrentUser(userName.toLowerCase());
            
            // The user name used may be a different case to the NTLM supplied user name,
            // read the current user and use that name
            userName = m_authComponent.getCurrentUserName();
            
            // Setup User object and Home space ID etc.
            NodeRef personNodeRef = m_personService.getPerson(userName);
            String currentTicket = m_authService.getCurrentTicket();
            user = new WebDAVUser(userName, currentTicket, personNodeRef);
            
            NodeRef homeSpaceRef = (NodeRef) m_nodeService.getProperty(personNodeRef, ContentModel.PROP_HOMEFOLDER);
            ((WebDAVUser)user).setHomeNode(homeSpaceRef);
            
            tx.commit();
        }
        catch (Throwable ex)
        {
            try
            {
                tx.rollback();
            }
            catch (Exception ex2)
            {
                logger.error("Failed to rollback transaction", ex2);
            }
            if (ex instanceof RuntimeException)
            {
                throw (RuntimeException)ex;
            }
            else if (ex instanceof IOException)
            {
                throw (IOException)ex;
            }
            else if (ex instanceof ServletException)
            {
                throw (ServletException)ex;
            }
            else
            {
                throw new RuntimeException("Authentication setup failed", ex);
            }
        }
        
        // Store the user on the session
        session.setAttribute(AUTHENTICATION_USER, user);
        
        return user;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.webdav.auth.BaseNTLMAuthenticationFilter#onLoginComplete(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected boolean onLoginComplete(HttpServletRequest req, HttpServletResponse res) throws IOException
    {
        // no futher processing to do, allow to complete
        return true;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.webdav.auth.BaseNTLMAuthenticationFilter#getLogger()
     */
    @Override
    final protected Log getLogger()
    {
        return logger;
    }
}
