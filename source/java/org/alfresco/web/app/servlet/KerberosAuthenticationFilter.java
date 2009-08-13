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
import java.util.List;
import java.util.Locale;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.alfresco.config.ConfigService;
import org.alfresco.i18n.I18NUtil;
import org.alfresco.repo.SessionUser;
import org.alfresco.repo.webdav.auth.BaseKerberosAuthenticationFilter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.User;
import org.alfresco.web.config.ClientConfigElement;
import org.alfresco.web.config.LanguagesConfigElement;
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

    // Constants
    //
    // Locale object stored in the session
    
    private static final String LOCALE = "locale";
    public static final String MESSAGE_BUNDLE = "alfresco.messages.webclient";
    
    // Various services required by Kerberos authenticator
    private ConfigService m_configService;
    
    // List of available locales (from the web-client configuration)
    
    private List<String> m_languages;        
    
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

        // Get a list of the available locales
        LanguagesConfigElement config = (LanguagesConfigElement) m_configService.getConfig("Languages")
                .getConfigElement(LanguagesConfigElement.CONFIG_ELEMENT_ID);

        m_languages = config.getLanguages();

        ClientConfigElement clientConfig = (ClientConfigElement) m_configService.getGlobalConfig().getConfigElement(
                ClientConfigElement.CONFIG_ELEMENT_ID);

        if (clientConfig != null)
        {
            setLoginPage(clientConfig.getLoginPage());
        }
    }

	/* (non-Javadoc)
	 * @see org.alfresco.repo.webdav.auth.BaseSSOAuthenticationFilter#createUserObject(java.lang.String, java.lang.String, org.alfresco.service.cmr.repository.NodeRef, java.lang.String)
	 */
	@Override
	protected SessionUser createUserObject(String userName, String ticket, NodeRef personNode, String homeSpace) {
		
		// Create a web client user object
		
		User user = new User( userName, ticket, personNode);
		user.setHomeSpaceId( homeSpace);
		
		return user;
	}

    /* (non-Javadoc)
     * @see org.alfresco.repo.webdav.auth.BaseNTLMAuthenticationFilter#onValidate(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpSession)
     */
    @Override
    protected void onValidate(HttpServletRequest req, HttpSession session)
    {
        // Set the current locale from the Accept-Lanaguage header if available

    	Locale userLocale = AbstractAuthenticationFilter.parseAcceptLanguageHeader(req, m_languages);
        if (userLocale != null)
        {
            session.setAttribute(LOCALE, userLocale);
            session.removeAttribute(MESSAGE_BUNDLE);
        }

        // Set the locale using the session

        I18NUtil.setLocale(Application.getLanguage(session));
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
