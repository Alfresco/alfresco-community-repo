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
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.servlet.FilterChain;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.transaction.UserTransaction;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.filesys.ExtendedServerConfigurationAccessor;
import org.alfresco.jlan.server.auth.ntlm.NTLM;
import org.alfresco.jlan.server.auth.passthru.DomainMapping;
import org.alfresco.jlan.server.config.SecurityConfigSection;
import org.alfresco.jlan.util.IPAddress;
import org.alfresco.repo.SessionUser;
import org.alfresco.repo.management.subsystems.ActivateableBean;
import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.repo.web.filter.beans.DependencyInjectedFilter;
import org.springframework.beans.factory.InitializingBean;

/**
 * Base class with common code and initialisation for single signon authentication filters.
 * 
 * @author gkspencer
 * @author kroast
 */
public abstract class BaseSSOAuthenticationFilter extends BaseAuthenticationFilter implements DependencyInjectedFilter, AuthenticationDriver, ActivateableBean, InitializingBean
{   
    // Allow an authentication ticket to be passed as part of a request to bypass authentication

    private ExtendedServerConfigurationAccessor serverConfiguration;
    
    // Various services required by NTLM authenticator

    private String m_loginPage;
    
    // Indicate whether ticket based logons are supported
    
    private boolean m_ticketLogons;
    
    // User object attribute name
    
    private String m_lastConfiguredServerName;
    private String m_lastResolvedServerName;
    
    private boolean m_isActive = true;
            
    protected static final String MIME_HTML_TEXT = "text/html";

    /**
     * @param serverConfiguration the serverConfiguration to set
     */
    public void setServerConfiguration(ExtendedServerConfigurationAccessor serverConfiguration)
    {
        this.serverConfiguration = serverConfiguration;
    }
    

    /**
     * Activates or deactivates the bean
     * 
     * @param active
     *            <code>true</code> if the bean is active and initialization should complete
     */
    public final void setActive(boolean active)
    {
        this.m_isActive = active;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.repo.management.subsystems.ActivateableBean#isActive()
     */
    public final boolean isActive()
    {
        return m_isActive;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    public final void afterPropertiesSet() throws ServletException
    {
        // Don't trigger initialization if this component has been disabled
        if (isActive())
        {
            init();
        }
    }
    
    /*
     * (non-Javadoc)
     * @see org.alfresco.repo.web.filter.beans.DependencyInjectedFilter#doFilter(javax.servlet.ServletContext,
     * javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
     */
    public void doFilter(ServletContext context, ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException
    {
        // If a filter up the chain has marked the request as not requiring auth then respect it        
        if (request.getAttribute( NO_AUTH_REQUIRED) != null)
        {
            if ( getLogger().isDebugEnabled())
                getLogger().debug("Authentication not required (filter), chaining ...");
            chain.doFilter(request, response);
        }
        else if (authenticateRequest(context, (HttpServletRequest) request, (HttpServletResponse) response))
        {
            chain.doFilter(request, response);
        }
    }

    /**
     * Initializes the filter. Only called if the filter is active, as indicated by {@link #isActive()}. Subclasses
     * should override.
     */
    protected void init() throws ServletException
    {
    }
    
    
    
    /**
     * Callback executed on successful ticket validation during Type3 Message processing.
     * 
     * @param sc
     *           the servlet context
     * @param req
     *           the request
     * @param res
     *           the response
     */
    protected void onValidate(ServletContext sc, HttpServletRequest req, HttpServletResponse res)
    {
    }
    
    /**
     * Callback executed on failed authentication of a user ticket during Type3 Message processing
     * 
     *  @param sc the servlet context
     *  @param req HttpServletRequest
     *  @param res HttpServletResponse
     *  @param session HttpSession
     */
    protected void onValidateFailed(ServletContext sc, HttpServletRequest req, HttpServletResponse res, HttpSession session)
        throws IOException
    {
    }
    
    /**
     * Callback executed on completion of NTLM login
     * 
     * @param req HttpServletRequest
     * @param res HttpServletResponse
     * @return true to continue filter chaining, false otherwise
     */
    protected boolean onLoginComplete(ServletContext sc, HttpServletRequest req, HttpServletResponse res, boolean userInit)
        throws IOException
    {
    	return true;
    }
    
    /**
     * Map a client IP address to a domain
     * 
     * @param clientIP String
     * @return String
     */
    protected final String mapClientAddressToDomain(String clientIP)
    {
        // Check if there are any domain mappings
    	SecurityConfigSection securityConfigSection = getSecurityConfigSection();
        if (securityConfigSection != null && securityConfigSection.hasDomainMappings() == false)
        {
            return null;
        }
        
        if (securityConfigSection != null)
        {
            // Convert the client IP address to an integer value
        	
            int clientAddr = IPAddress.parseNumericAddress(clientIP);
            for (DomainMapping domainMap : securityConfigSection.getDomainMappings())
            {
                if (domainMap.isMemberOfDomain(clientAddr))
                {
                    if (getLogger().isDebugEnabled())
                        getLogger().debug("Mapped client IP " + clientIP + " to domain " + domainMap.getDomain());
                
                    return domainMap.getDomain();
                }
            }
        }
        
        if (getLogger().isDebugEnabled())
            getLogger().debug("Failed to map client IP " + clientIP + " to a domain");
        
        // No domain mapping for the client address
        
        return null;
    }
    
    /**
     * Check if the request has specified a ticket parameter to bypass the standard authentication.
     * 
     * @param servletContext
     *            the servlet context
     * @param req
     *            the request
     * @param resp
     *            the response
     * @return boolean
     */
    protected boolean checkForTicketParameter(ServletContext servletContext, HttpServletRequest req, HttpServletResponse resp)
    {
    	// Check if the request includes an authentication ticket

    	boolean ticketValid = false;
    	String ticket = req.getParameter(ARG_TICKET);
    	
    	if (ticket != null && ticket.length() != 0)
    	{
         if (getLogger().isDebugEnabled())
            getLogger().debug(
                  "Logon via ticket from " + req.getRemoteHost() + " (" + req.getRemoteAddr() + ":"
                        + req.getRemotePort() + ")" + " ticket=" + ticket);

         UserTransaction tx = null;
         try
         {
            // Get a cached user with a valid ticket
            SessionUser user = getSessionUser(servletContext, req, resp, true);

            // If this isn't the same ticket, invalidate the session
            if (user != null && !ticket.equals(user.getTicket()))
            {
               invalidateSession(req);
               user = null;
            }

            // If we don't yet have a valid cached user, validate the ticket and create one
            if (user == null)
            {
               authenticationService.validate(ticket);
               user = createUserEnvironment(req.getSession(), authenticationService.getCurrentUserName(),
                     authenticationService.getCurrentTicket(), true);
            }

            // Indicate the ticket parameter was specified, and valid

            ticketValid = true;
         }
    		catch (AuthenticationException authErr)
        	{
        		if (getLogger().isDebugEnabled())
        		    getLogger().debug("Failed to authenticate user ticket: " + authErr.getMessage(), authErr);
        	}
        	catch (Throwable e)
        	{
        		if (getLogger().isDebugEnabled())
                    getLogger().debug("Error during ticket validation and user creation: " + e.getMessage(), e);
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
    	
    	// Return the ticket parameter status
    	
    	return ticketValid;
    }
    
    /**
     * Redirect to the login page
     * 
     * @param req HttpServletRequest
     * @param req HttpServletResponse
     * @exception IOException
     */
    protected void redirectToLoginPage(HttpServletRequest req, HttpServletResponse res)
    	throws IOException
    {
        if (getLogger().isDebugEnabled())
            getLogger().debug("redirectToLoginPage...");
    	if (hasLoginPage())
    		res.sendRedirect(req.getContextPath() + "/faces" + getLoginPage());
    }
    
    /**
     * Determine if the login page is available
     * 
     * @return boolean
     */
    protected final boolean hasLoginPage()
    {
    	return m_loginPage != null ? true : false;
    }
    
    /**
     * Return the login page address
     * 
     * @return String
     */
    protected final String getLoginPage()
    {
    	return m_loginPage;
    }
    
    /**
     * Set the login page address
     * 
     * @param loginPage String
     */
    protected final void setLoginPage( String loginPage)
    {
    	m_loginPage = loginPage;
    }
    
    /**
     * Check if ticket based logons are allowed
     * 
     * @return boolean
     */
    protected final boolean allowsTicketLogons()
    {
    	return m_ticketLogons;
    }
    
    /**
     * Set the ticket based logons allowed flag
     * 
     * @param ticketsAllowed boolean
     */
    public final void setTicketLogons( boolean ticketsAllowed)
    {
    	m_ticketLogons = ticketsAllowed;
    }

    /**
     * Check if a security blob starts with the NTLMSSP signature
     * 
     * @param byts byte[]
     * @param offset int
     * @return boolean
     */
    protected final boolean isNTLMSSPBlob( byte[] byts, int offset)
    {
	    // Check if the blob has the NTLMSSP signature

    	boolean isNTLMSSP = false;
    	
	    if (( byts.length - offset) >= NTLM.Signature.length) {
	      
	      // Check for the NTLMSSP signature
	      
	      int idx = 0;
	      while ( idx < NTLM.Signature.length && byts[offset + idx] == NTLM.Signature[ idx])
	        idx++;
	      
	      if ( idx == NTLM.Signature.length)
	        isNTLMSSP = true;
	    }
	    
	    return isNTLMSSP;
    }

    /**
     * Because the file server configuration may change during the lifetime of this filter, this method checks against
     * the last configured server name before returning a cached result
     * 
     * @return resolved local server name
     */
    protected synchronized String getServerName()
    {
        // Get the local server name, try the file server config first
        String srvName = null;
        if (serverConfiguration != null)
        {
            srvName = serverConfiguration.getServerName();
            if (srvName != null && srvName.length() == 0)
            {
                srvName = null;
            }

        }

        if (m_lastResolvedServerName != null
                && (m_lastConfiguredServerName == null && srvName == null || m_lastConfiguredServerName.equals(srvName)))
        {
            return m_lastResolvedServerName;
        }

        m_lastResolvedServerName = null;
        m_lastConfiguredServerName = srvName;
        if (serverConfiguration != null)
        {
            if (m_lastConfiguredServerName != null)
            {
                try
                {
                    InetAddress resolved = InetAddress.getByName(m_lastConfiguredServerName);
                    if (resolved == null)
                    {
                        // Failed to resolve the configured name

                        m_lastResolvedServerName = serverConfiguration.getLocalServerName(true);
                    }
                    else
                    {
                        m_lastResolvedServerName = m_lastConfiguredServerName;
                    }
                }
                catch (UnknownHostException ex)
                {
                    if (getLogger().isWarnEnabled())
                        getLogger().warn("NTLM filter, error resolving CIFS host name" + m_lastConfiguredServerName);
                }
            }

            // If we still do not have a name use the DNS name of the server, with the domain part removed

            if (m_lastResolvedServerName == null)
            {
                m_lastResolvedServerName = serverConfiguration.getLocalServerName(true);

                // DEBUG

                if (getLogger().isInfoEnabled())
                    getLogger().info("NTLM filter using server name " + m_lastResolvedServerName);
            }
        }
        else
        {
            // Get the host name
            try
            {
                // Get the local host name

                m_lastResolvedServerName = InetAddress.getLocalHost().getHostName();

                // Strip any domain name

                int pos = m_lastResolvedServerName.indexOf(".");
                if (pos != -1)
                {
                    m_lastResolvedServerName = m_lastResolvedServerName.substring(0, pos);
                }
            }
            catch (UnknownHostException ex)
            {
                if (getLogger().isErrorEnabled())
                    getLogger().error("NTLM filter, error getting local host name", ex);
            }
        }

        // Check if the server name is valid

        if (m_lastResolvedServerName == null || m_lastResolvedServerName.length() == 0)
        {
            throw new AlfrescoRuntimeException("Failed to get local server name");
        }

        return m_lastResolvedServerName;
    }
    
    protected SecurityConfigSection getSecurityConfigSection()
    {
        return serverConfiguration == null ? null : (SecurityConfigSection) serverConfiguration.getConfigSection(SecurityConfigSection.SectionName);
    }
    
    /**
     * Writes link to login page and refresh tag which cause user
     * to be redirected to the login page.
     * 
     * @param resp HttpServletResponse
     * @param httpSess HttpSession
     * @throws IOException
     */
    protected void writeLoginPageLink(HttpServletRequest req, HttpServletResponse resp) throws IOException
    {
        if ( hasLoginPage())
        {
            resp.setContentType(MIME_HTML_TEXT);

            final PrintWriter out = resp.getWriter();
            out.println("<html><head>");
            out.println("<meta http-equiv=\"Refresh\" content=\"0; url=" + 
                    req.getContextPath() + "/faces" + getLoginPage() +
                    "\">");
            out.println("</head><body><p>Please <a href=\"" +
                    req.getContextPath() + "/faces" + getLoginPage() +
                    "\">log in</a>.</p>");
            out.println("</body></html>");
            out.close();
        }
    }

    
    
}
