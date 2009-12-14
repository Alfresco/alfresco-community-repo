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
package org.alfresco.web.app.servlet;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.extensions.config.ConfigService;
import org.alfresco.repo.SessionUser;
import org.alfresco.repo.webdav.auth.BaseNTLMAuthenticationFilter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.web.bean.repository.User;
import org.alfresco.web.config.ClientConfigElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Web-client NTLM Authentication Filter Class
 * 
 * @author GKSpencer
 */
public class NTLMAuthenticationFilter extends BaseNTLMAuthenticationFilter
{
    // Debug logging
    private static Log logger = LogFactory.getLog(NTLMAuthenticationFilter.class);
    
    protected ConfigService m_configService;
    
    /**
     * @param configService the configService to set
     */
    public void setConfigService(ConfigService configService)
    {
        m_configService = configService;
    }

    
	/* (non-Javadoc)
	 * @see org.alfresco.repo.webdav.auth.BaseNTLMAuthenticationFilter#init()
	 */
	@Override
    protected void init() throws ServletException
    {
        // Call the base NTLM filter initialization
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
	
    /* (non-Javadoc)
     * @see org.alfresco.repo.webdav.auth.BaseSSOAuthenticationFilter#onValidate(javax.servlet.ServletContext, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void onValidate(ServletContext sc, HttpServletRequest req, HttpServletResponse res)
    {
        // Set the locale using the session
        AuthenticationHelper.setupThread(sc, req, res);
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.webdav.auth.BaseNTLMAuthenticationFilter#onValidateFailed(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, javax.servlet.http.HttpSession)
     */
    @Override
    protected void onValidateFailed(HttpServletRequest req, HttpServletResponse res, HttpSession session)
        throws IOException
    {
        // Redirect to the login page if user validation fails

    	redirectToLoginPage(req, res);
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.webdav.auth.BaseNTLMAuthenticationFilter#onLoginComplete(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected boolean onLoginComplete(HttpServletRequest req, HttpServletResponse res)
        throws IOException
    {
        // If the original URL requested was the login page then redirect to the browse view
        if (req.getRequestURI().endsWith(getLoginPage()) == true)
        {
            if (logger.isDebugEnabled())
                logger.debug("Login page requested, redirecting to browse page");

            //  Redirect to the browse view
            res.sendRedirect(req.getContextPath() + "/faces/jsp/browse/browse.jsp");
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
