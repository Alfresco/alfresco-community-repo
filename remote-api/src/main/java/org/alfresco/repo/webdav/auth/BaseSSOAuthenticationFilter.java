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
package org.alfresco.repo.webdav.auth;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.regex.Pattern;

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
import org.alfresco.jlan.server.config.SecurityConfigSection;
import org.alfresco.jlan.util.IPAddress;
import org.alfresco.repo.SessionUser;
import org.alfresco.repo.management.subsystems.ActivateableBean;
import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.repo.web.auth.WebCredentials;
import org.alfresco.repo.web.filter.beans.DependencyInjectedFilter;
import org.alfresco.rest.api.PublicApiTenantWebScriptServletRequest;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.extensions.webscripts.RuntimeContainer;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.extensions.webscripts.Description.RequiredAuthentication;
import org.springframework.extensions.surf.util.URLDecoder;
import org.springframework.extensions.webscripts.Match;

/**
 * Base class with common code and initialisation for single signon authentication filters.
 * 
 * @author gkspencer
 * @author kroast
 */
public abstract class BaseSSOAuthenticationFilter extends BaseAuthenticationFilter implements DependencyInjectedFilter, AuthenticationDriver, ActivateableBean, InitializingBean
{   
	private static final Pattern CMIS_URI_PATTERN = Pattern.compile(".*/cmis/versions/[0-9]+\\.[0-9]+/.*");
	
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
            
    private AuthenticationDriver fallbackDelegate;
    private boolean m_isFallbackEnabled = true;
            
    protected static final String MIME_HTML_TEXT = "text/html";

    protected String loginPageLink;

    /**
     * @return login page link, which is send back to the client if the login fails in the filter.
     *         Override to change the default behaviour.
     */
    public String getLoginPageLink()
    {
        if (loginPageLink == null || loginPageLink.isEmpty())
        {
            return "/faces" + getLoginPage();
        }
        else
        {
            return loginPageLink;
        }
    }

    public void setLoginPageLink(String loginPageLink)
    {
        this.loginPageLink = loginPageLink;
    }

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

    /**
     * Sets the fallback authentication support for this filter
     * 
     * @param delegate AuthenticationDriver
     */
    public final void setFallback(AuthenticationDriver delegate)
    {
        this.fallbackDelegate = delegate;
    }

    /**
     * Activates or deactivates the fallback authentication support for this filter
     * 
     * @param fallbackEnabled
     */
    public final void setFallbackEnabled(boolean fallbackEnabled)
    {
        this.m_isFallbackEnabled = fallbackEnabled;
    }

    /** 
     * @return <code>true</code> if fallback authentication enabled
     */
    public final boolean isFallbackEnabled()
    {
        return m_isFallbackEnabled && fallbackDelegate != null;
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
    	// Get the publicapi.container bean.
        ApplicationContext appContext = WebApplicationContextUtils.getRequiredWebApplicationContext(context);
        RuntimeContainer container = (RuntimeContainer) appContext.getBean("publicapi.container");

        // Get the HTTP request/response
        HttpServletRequest req = (HttpServletRequest) request;

        Match match = container.getRegistry().findWebScript(req.getMethod(), getScriptUrl(req));
        
        // If a filter up the chain has marked the request as not requiring auth then respect it        
        if (request.getAttribute(NO_AUTH_REQUIRED) != null)
        {
            if ( getLogger().isTraceEnabled())
            {
                getLogger().trace("Authentication not required (filter), chaining ...");
            }
            chain.doFilter(request, response);
        }
       // check the authentication required - if none then we don't want any of the filters down the chain to require any authentication checks
       else if ((match != null) && (match.getWebScript() != null) && (RequiredAuthentication.none == match.getWebScript().getDescription().getRequiredAuthentication()))
        {
        	if (getLogger().isDebugEnabled())    
        	{
                    getLogger().debug("Found webscript with no authentication - set NO_AUTH_REQUIRED flag.");
            }

            req.setAttribute(NO_AUTH_REQUIRED, Boolean.TRUE);
          
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
    protected void onValidate(ServletContext sc, HttpServletRequest req, HttpServletResponse res, WebCredentials credentials)
    {
        authenticationListener.userAuthenticated(credentials);
    }
    
    /**
     * Callback executed on failed authentication of a user ticket during Type3 Message processing
     * 
     *  @param sc the servlet context
     *  @param req HttpServletRequest
     *  @param res HttpServletResponse
     *  @param session HttpSession
     */
    protected void onValidateFailed(ServletContext sc, HttpServletRequest req, HttpServletResponse res, HttpSession session, WebCredentials credentials)
        throws IOException
    {
        authenticationListener.authenticationFailed(credentials);
        
        // Restart the login challenge process if validation fails
        
        restartLoginChallenge(sc, req, res);
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
            if (getLogger().isTraceEnabled())
            {
                getLogger().trace(
                    "Logon via ticket from " + req.getRemoteHost() + " (" + req.getRemoteAddr() + ":" + req.getRemotePort() + ")" +
                        " ticket=" + ticket);
            }
            
            UserTransaction tx = null;
            try
            {
                // Get a cached user with a valid ticket
                SessionUser user = getSessionUser(servletContext, req, resp, true);
                
                // If this isn't the same ticket, invalidate the session
                if (user != null && !ticket.equals(user.getTicket()))
                {
                   if (getLogger().isDebugEnabled())
                   {
                       getLogger().debug("The ticket doesn't match, invalidate the session.");
                   }
                   invalidateSession(req);
                   user = null;
                }
                
                // If we don't yet have a valid cached user, validate the ticket and create one
                if (user == null)
                {
                   if (getLogger().isDebugEnabled())
                   {
                       getLogger().debug("There is no valid cached user, validate the ticket and create one.");
                   }
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
                {
                    getLogger().debug("Failed to authenticate user ticket: " + authErr.getMessage(), authErr);
                }
            }
            catch (Throwable e)
            {
                if (getLogger().isDebugEnabled())
                {
                    getLogger().debug("Error during ticket validation and user creation: " + e.getMessage(), e);
                }
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
     * @param res HttpServletResponse
     * @exception IOException
     */
    protected void redirectToLoginPage(HttpServletRequest req, HttpServletResponse res)
        throws IOException
    {
        if (getLogger().isTraceEnabled())
        {
            getLogger().trace("redirectToLoginPage...");
        }
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
        if (getLogger().isTraceEnabled())
        {
            getLogger().trace("Searching for local server name.");
        }
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
        if (getLogger().isTraceEnabled())
        {
            getLogger().trace("Found server name in the file server configuration: " + srvName);
        }
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
                        if (getLogger().isDebugEnabled())
                        {
                            getLogger().debug("Failed to resolve the configured name.");
                        }

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
                    {
                        getLogger().warn("NTLM filter, error resolving CIFS host name" + m_lastConfiguredServerName);
                    }
                }
            }

            // If we still do not have a name use the DNS name of the server, with the domain part removed

            if (m_lastResolvedServerName == null)
            {
                m_lastResolvedServerName = serverConfiguration.getLocalServerName(true);

                // DEBUG

                if (getLogger().isInfoEnabled())
                {
                    getLogger().info("NTLM filter using server name " + m_lastResolvedServerName);
                }
            }
        }
        else
        {
            // Get the host name
            try
            {
                // Get the local host name

                m_lastResolvedServerName = InetAddress.getLocalHost().getHostName();

                if (getLogger().isInfoEnabled())
                {
                    getLogger().info("Found FQDN " + m_lastResolvedServerName);
                }
                // Strip any domain name

                int pos = m_lastResolvedServerName.indexOf(".");
                if (pos != -1)
                {
                    m_lastResolvedServerName = m_lastResolvedServerName.substring(0, pos);
                }
            }
            catch (UnknownHostException ex)
            {
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
     * @param context ServletContext
     * @param req HttpServletRequest
     * @param resp HttpServletResponse
     * @throws IOException
     */
    protected void writeLoginPageLink(ServletContext context, HttpServletRequest req, HttpServletResponse resp) throws IOException
    {
        if ( hasLoginPage())
        {
            resp.setContentType(MIME_HTML_TEXT);

            try (PrintWriter out = resp.getWriter())
            {
                out.println("<html><head>");
                // Removed the auto refresh to avoid refresh loop, MNT-16931
                // Removed the link to the login page, MNT-20200
                out.println("</head><body><p>Login failed. Please try again.</p>");
                out.println("</body></html>");
            }
        }
    }

    /**
     * Include into response authentication method that is supported by fallback mechanism
     * 
     * @param context ServletContext
     * @param req HttpServletRequest
     * @param resp HttpServletResponse
     * @throws IOException
     */
    protected void includeFallbackAuth(ServletContext context, HttpServletRequest req, HttpServletResponse resp) throws IOException
    {
        fallbackDelegate.restartLoginChallenge(context, req, resp);
    }
    
    /**
     * Delegate authentication to the fallback mechanism
     * 
     * @param context ServletContext
     * @param req HttpServletRequest
     * @param resp HttpServletResponse
     * @return boolean
     * @throws IOException
     * @throws ServletException
     */
    protected boolean performFallbackAuthentication(ServletContext context, HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException
    {
        if (getLogger().isTraceEnabled())
        {
            getLogger().trace("Performing fallback authentication...");
        }
        
        boolean fallbackSuccess = fallbackDelegate.authenticateRequest(context, req, resp);
        
        if (!fallbackSuccess)
        {
            restartLoginChallenge(context, req, resp);
    
            if (getLogger().isDebugEnabled())
            {
                getLogger().debug("Fallback authentication failed. Restarting login...");
            }
        }
    
        if (fallbackSuccess && getLogger().isDebugEnabled())
        {
            getLogger().debug("Fallback authentication succeeded.");
        }
        
        return fallbackSuccess;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.WebScriptRuntime#getScriptUrl()
     */
    private String getScriptUrl(HttpServletRequest req)
    {
        // NOTE: Don't use req.getPathInfo() - it truncates the path at first semi-colon in Tomcat
        final String requestURI = req.getRequestURI();
        final String serviceContextPath = req.getContextPath() + req.getServletPath();
        String pathInfo;
        
        if (serviceContextPath.length() > requestURI.length())
        {
            // NOTE: assume a redirect has taken place e.g. tomcat welcome-page
            // NOTE: this is unlikely, and we'll take the hit if the path contains a semi-colon
            pathInfo = req.getPathInfo();
        }
        // MNT-13057 fix, do not decode CMIS uris.
        else if (CMIS_URI_PATTERN.matcher(requestURI).matches())
        {
    	   pathInfo = requestURI.substring(serviceContextPath.length());
        }
        else
        {
            pathInfo = URLDecoder.decode(requestURI.substring(serviceContextPath.length()));
        }
        
        // NOTE: must contain at least root / and single character for tenant name
        if (pathInfo.length() < 2 || pathInfo.equals("/"))
        {
            // url path has no tenant id -> get networks request
            pathInfo = PublicApiTenantWebScriptServletRequest.NETWORKS_PATH;
        }
        else
        {
            if(!pathInfo.substring(0, 6).toLowerCase().equals("/cmis/") && !pathInfo.equals("/discovery"))
            {
                // remove tenant
                int idx = pathInfo.indexOf('/', 1);
                pathInfo = pathInfo.substring(idx == -1 ? pathInfo.length() : idx);
                if(pathInfo.equals("") || pathInfo.equals("/"))
                {
                    // url path is just a tenant id -> get network request
                    pathInfo = PublicApiTenantWebScriptServletRequest.NETWORK_PATH;
                }
            }
        }

        return pathInfo;
    }
}
