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
package org.alfresco.web.app.servlet;

import java.io.IOException;

import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.extensions.config.ConfigService;
import org.alfresco.repo.SessionUser;
import org.alfresco.repo.webdav.auth.BaseKerberosAuthenticationFilter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.NavigationBean;
import org.alfresco.web.bean.repository.PreferencesService;
import org.alfresco.web.bean.repository.User;
import org.alfresco.web.config.ClientConfigElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Kerberos Authentication Filter Class
 * 
 * @author GKSpencer
 */
public class KerberosAuthenticationFilter extends BaseKerberosAuthenticationFilter
{
    // Debug logging
    
    private static Log logger = LogFactory.getLog(KerberosAuthenticationFilter.class);
  
    // Various services required by Kerberos authenticator
    private ConfigService m_configService;
    
    /**
     * @param configService the configService to set
     */
    public void setConfigService(ConfigService configService)
    {
        m_configService = configService;
    }

    
    /* (non-Javadoc)
     * @see org.alfresco.repo.webdav.auth.BaseKerberosAuthenticationFilter#init()
     */
    @Override
    protected void init() throws ServletException
    {
        // Call the base Kerberos filter initialization
        super.init();

        ClientConfigElement clientConfig = (ClientConfigElement) m_configService.getGlobalConfig().getConfigElement(
                ClientConfigElement.CONFIG_ELEMENT_ID);

        if (clientConfig != null)
        {
            setLoginPage(clientConfig.getLoginPage());
        }
        
        // Use the web client user attribute name
        setUserAttributeName(AuthenticationHelper.AUTHENTICATION_USER);
    }

        
    /* (non-Javadoc)
     * @see org.alfresco.repo.webdav.auth.BaseAuthenticationFilter#createUserObject(java.lang.String, java.lang.String, org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    protected SessionUser createUserObject(String userName, String ticket, NodeRef personNode, NodeRef homeSpaceRef)
    {
		// Create a web client user object
		
		User user = new User( userName, ticket, personNode);
		user.setHomeSpaceId( homeSpaceRef.getId());
		
		return user;
	}

    /*
     * (non-Javadoc)
     * @see org.alfresco.repo.webdav.auth.BaseSSOAuthenticationFilter#onValidate(javax.servlet.ServletContext,
     * javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
   protected void onValidate(ServletContext sc, HttpServletRequest req, HttpServletResponse res)
   {
        // Set the locale using the session
        AuthenticationHelper.setupThread(sc, req, res, true);
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.webdav.auth.BaseSSOAuthenticationFilter#onValidateFailed(javax.servlet.ServletContext, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, javax.servlet.http.HttpSession)
     */
    @Override
    protected void onValidateFailed(ServletContext sc, HttpServletRequest req, HttpServletResponse res, HttpSession session)
        throws IOException
    {
        // Redirect to the login page if user validation fails

    	redirectToLoginPage(req, res);
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.webdav.auth.BaseNTLMAuthenticationFilter#onLoginComplete(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected boolean onLoginComplete(ServletContext sc, HttpServletRequest req, HttpServletResponse res, boolean userInit)
        throws IOException
    {
        // If the original URL requested was the login page then redirect to the browse view
        String requestURI = req.getRequestURI();
        if (requestURI.startsWith(req.getContextPath() + BaseServlet.FACES_SERVLET) && (userInit || requestURI.endsWith(getLoginPage())))
        {
            if (logger.isDebugEnabled() && requestURI.endsWith(getLoginPage()))
                logger.debug("Login page requested - redirecting to initially configured page");
            if (logger.isDebugEnabled() && userInit)
                logger.debug("Session reinitialised - redirecting to initially configured page");
            
            FacesContext fc = FacesHelper.getFacesContext(req, res, sc);
            ConfigService configService = Application.getConfigService(fc);
            ClientConfigElement configElement = (ClientConfigElement)configService.getGlobalConfig().getConfigElement("client");
            String location = configElement.getInitialLocation();
            
            String preference = (String)PreferencesService.getPreferences(fc).getValue("start-location");
            if (preference != null)
            {
                location = preference;
            }
            
            if (NavigationBean.LOCATION_MYALFRESCO.equals(location))
            {
                // Clear previous location - Fixes the issue ADB-61
                NavigationBean navigationBean = (NavigationBean)FacesHelper.getManagedBean(fc, "NavigationBean");
                if (navigationBean != null)
                {
                    navigationBean.setLocation(null);
                    navigationBean.setToolbarLocation(null);
                }
                res.sendRedirect(req.getContextPath() + BaseServlet.FACES_SERVLET + "/jsp/dashboards/container.jsp");
            }
            else
            {
                res.sendRedirect(req.getContextPath() + BaseServlet.FACES_SERVLET + FacesHelper.BROWSE_VIEW_ID);
            }
            
            return false;
        }
        else
        {
            return true;
        }
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
