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
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.UserTransaction;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.repo.web.filter.beans.DependencyInjectedFilter;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.NoSuchPersonException;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * WebDAV Authentication Filter Class
 * 
 * @author GKSpencer
 */
public class AuthenticationFilter implements DependencyInjectedFilter
{
    // Debug logging
    
    private static Log logger = LogFactory.getLog(NTLMAuthenticationFilter.class);
    
    // Authenticated user session object name

    public final static String AUTHENTICATION_USER = "_alfDAVAuthTicket";

    // Allow an authentication ticket to be passed as part of a request to bypass authentication
    
    private static final String ARG_TICKET = "ticket";
    private static final String PPT_EXTN = ".ppt";
    private static final String VTI_IGNORE = "&vtiIgnore";
    
    // Various services required by NTLM authenticator
    
    private AuthenticationService authService;
    private PersonService personService;
    private NodeService nodeService;
    private TransactionService transactionService;
    
    
    /**
     * @param authService the authService to set
     */
    public void setAuthenticationService(AuthenticationService authService)
    {
        this.authService = authService;
    }

    /**
     * @param personService the personService to set
     */
    public void setPersonService(PersonService personService)
    {
        this.personService = personService;
    }

    /**
     * @param nodeService the nodeService to set
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * @param transactionService the transactionService to set
     */
    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }

    /**
     * Run the authentication filter
     * 
     * @param context ServletContext
     * @param req ServletRequest
     * @param resp ServletResponse
     * @param chain FilterChain
     * @exception ServletException
     * @exception IOException
     */
    public void doFilter(ServletContext context, ServletRequest req, ServletResponse resp, FilterChain chain)
            throws IOException, ServletException
    {
        // Assume it's an HTTP request

        HttpServletRequest httpReq = (HttpServletRequest) req;
        HttpServletResponse httpResp = (HttpServletResponse) resp;

        // Get the user details object from the session

        WebDAVUser user = (WebDAVUser) httpReq.getSession().getAttribute(AUTHENTICATION_USER);

        if (user == null)
        {
            // Get the authorization header
            
            String authHdr = httpReq.getHeader("Authorization");
            
            if ( authHdr != null && authHdr.length() > 5 && authHdr.substring(0,5).equalsIgnoreCase("BASIC"))
            {
                // Basic authentication details present

                String basicAuth = new String(Base64.decodeBase64(authHdr.substring(5).getBytes()));
                
                // Split the username and password
                
                String username = null;
                String password = null;
                
                int pos = basicAuth.indexOf(":");
                if ( pos != -1)
                {
                    username = basicAuth.substring(0, pos);
                    password = basicAuth.substring(pos + 1);
                }
                else
                {
                    username = basicAuth;
                    password = "";
                }
                
                try
                {
                    // Authenticate the user

                	authService.authenticate(username, password.toCharArray());
                    
                    // Set the user name as stored by the back end
                    username = authService.getCurrentUserName();
                    
                    // Get the user node and home folder
                	
                    NodeRef personNodeRef = personService.getPerson(username);
                    NodeRef homeSpaceRef = (NodeRef) nodeService.getProperty(personNodeRef, ContentModel.PROP_HOMEFOLDER);
                    
                    // Setup User object and Home space ID etc.
                    
                    user = new WebDAVUser(username, authService.getCurrentTicket(), homeSpaceRef);
                    
                    httpReq.getSession().setAttribute(AUTHENTICATION_USER, user);
                }
                catch ( AuthenticationException ex)
                {
                    // Do nothing, user object will be null
                }
                catch (NoSuchPersonException e)
                {
                    // Do nothing, user object will be null
                }
            }
            else
            {
            	// Check if the request includes an authentication ticket
            
            	String ticket = req.getParameter( ARG_TICKET);
            	
            	if ( ticket != null &&  ticket.length() > 0)
            	{
            		// PowerPoint bug fix
            		if (ticket.endsWith(PPT_EXTN))
            		{
            			ticket = ticket.substring(0, ticket.length() - PPT_EXTN.length());
            		}

                	// Debug
                    
                    if ( logger.isDebugEnabled())
                        logger.debug("Logon via ticket from " + req.getRemoteHost() + " (" +
                                req.getRemoteAddr() + ":" + req.getRemotePort() + ")" + " ticket=" + ticket);
                    
            		UserTransaction tx = null;
            	    try
            	    {
            	    	// Validate the ticket
            	    	  
            	    	authService.validate(ticket);

            	    	// Need to create the User instance if not already available
            	    	  
            	        String currentUsername = authService.getCurrentUserName();

            	        // Start a transaction
            	          
          	            tx = transactionService.getUserTransaction();
            	        tx.begin();
            	            
            	        NodeRef personRef = personService.getPerson(currentUsername);
            	        user = new WebDAVUser( currentUsername, authService.getCurrentTicket(), personRef);
            	        NodeRef homeRef = (NodeRef) nodeService.getProperty(personRef, ContentModel.PROP_HOMEFOLDER);
            	            
            	        // Check that the home space node exists - else Login cannot proceed
            	            
            	        if (nodeService.exists(homeRef) == false)
            	        {
            	        	throw new InvalidNodeRefException(homeRef);
            	        }
            	        user.setHomeNode(homeRef);
            	            
            	        tx.commit();
            	        tx = null; 
            	            
            	        // Store the User object in the Session - the authentication servlet will then proceed
            	            
            	        httpReq.getSession().setAttribute( AUTHENTICATION_USER, user);
            	    }
	            	catch (AuthenticationException authErr)
	            	{
	            		// Clear the user object to signal authentication failure
	            		
	            		user = null;
	            	}
	            	catch (Throwable e)
	            	{
	            		// Clear the user object to signal authentication failure
	            		
	            		user = null;
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
            }
            
            // Check if the user is authenticated, if not then prompt again
            
            if ( user == null)
            {
                // No user/ticket, force the client to prompt for logon details
    
                httpResp.setHeader("WWW-Authenticate", "BASIC realm=\"Alfresco DAV Server\"");
                httpResp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    
                httpResp.flushBuffer();
                return;
            }
        }
        else
        {
            try
            {
               // Setup the authentication context
               authService.validate(user.getTicket());

               // Set the current locale

               // I18NUtil.setLocale(Application.getLanguage(httpRequest.getSession()));
            }
            catch (Exception ex)
            {
               // No user/ticket, force the client to prompt for logon details
               
               httpResp.setHeader("WWW-Authenticate", "BASIC realm=\"Alfresco DAV Server\"");
               httpResp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
   
               httpResp.flushBuffer();
               return;
            }
        }

        // Chain other filters

        chain.doFilter(req, resp);
    }

    /**
     * Cleanup filter resources
     */
    public void destroy()
    {
        // Nothing to do
    }
}
