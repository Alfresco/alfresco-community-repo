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
package org.alfresco.web.app.servlet;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.transaction.UserTransaction;

import org.alfresco.config.ConfigService;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.i18n.I18NUtil;
import org.alfresco.jlan.server.auth.ntlm.NTLM;
import org.alfresco.jlan.server.auth.ntlm.NTLMMessage;
import org.alfresco.jlan.server.auth.ntlm.Type1NTLMMessage;
import org.alfresco.jlan.server.auth.ntlm.Type3NTLMMessage;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.SessionUser;
import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.repo.webdav.auth.BaseNTLMAuthenticationFilter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.LoginBean;
import org.alfresco.web.bean.repository.User;
import org.alfresco.web.config.LanguagesConfigElement;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * NTLM Authentication Filter Class
 * 
 * @author GKSpencer
 */
public class NTLMAuthenticationFilter extends BaseNTLMAuthenticationFilter
{
    // Locale object stored in the session
    private static final String LOCALE = "locale";
    private static final String MESSAGE_BUNDLE = "alfresco.messages.webclient";
    
    // Debug logging
    private static Log logger = LogFactory.getLog(NTLMAuthenticationFilter.class);
    
    // Various services required by NTLM authenticator
    private ConfigService m_configService;
    
    // Login page address
    private String m_loginPage;

    // List of available locales (from the web-client configuration)
    private List<String> m_languages;
    
    
    /**
     * Initialize the filter
     * 
     * @param args FilterConfig
     * @exception ServletException
     */
    public void init(FilterConfig args) throws ServletException
    {
        super.init(args);
        
        // Setup the authentication context
        WebApplicationContext ctx = WebApplicationContextUtils.getRequiredWebApplicationContext(m_context);
        m_configService = (ConfigService)ctx.getBean("webClientConfigService");
        
        // Get a list of the available locales
        LanguagesConfigElement config = (LanguagesConfigElement) m_configService.
              getConfig("Languages").getConfigElement(LanguagesConfigElement.CONFIG_ELEMENT_ID);
        
        m_languages = config.getLanguages();
    }

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
        User user = (User) httpSess.getAttribute(AuthenticationHelper.AUTHENTICATION_USER);
        if (user != null && reqAuth == false)
        {
            try
            {
                if (logger.isDebugEnabled())
                    logger.debug("User " + user.getUserName() + " validate ticket");
                
                // Validate the user ticket
                m_authService.validate(user.getTicket());
                reqAuth = false;
                
                // Set the current locale
                I18NUtil.setLocale(Application.getLanguage(httpSess));
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

        // Check if the login page is being accessed, do not intercept the login page
        if (req.getRequestURI().endsWith(getLoginPage()) == true)
        {
            if (logger.isDebugEnabled())
                logger.debug("Login page requested, chaining ...");
            
            // Chain to the next filter
            chain.doFilter( sreq, sresp);
            return;
        }
        
        // Check if the browser is Opera, if so then display the login page as Opera does not
        // support NTLM and displays an error page if a request to use NTLM is sent to it
        String userAgent = req.getHeader("user-agent");
        if (userAgent != null && userAgent.indexOf("Opera ") != -1)
        {
            if (logger.isDebugEnabled())
                logger.debug("Opera detected, redirecting to login page");

            // Redirect to the login page
            resp.sendRedirect(req.getContextPath() + "/faces" + getLoginPage());
            return;
        }
        
        // Check the authorization header
        if (authHdr == null)
        {
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
                    logger.debug("NTLM blob not handled, redirecting to login page.");
                
                // Redirect to the login page
                resp.sendRedirect(req.getContextPath() + "/faces" + getLoginPage());
            }
        }
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.webdav.auth.BaseNTLMAuthenticationFilter#getSessionUser(javax.servlet.http.HttpSession)
     */
    @Override
    protected SessionUser getSessionUser(HttpSession session)
    {
        return (SessionUser)session.getAttribute(AuthenticationHelper.AUTHENTICATION_USER);
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
        res.sendRedirect(req.getContextPath() + "/faces" + getLoginPage());
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
        NodeRef homeSpaceRef = null;
        
        try
        {
            tx.begin();
            
            // Setup User object and Home space ID etc.
            NodeRef personNodeRef = m_personService.getPerson(userName);
            
            // Use the system user context to do the user lookup
            m_authComponent.setCurrentUser(m_authComponent.getSystemUserName());
   
            // User name should match the uid in the person entry found
            m_authComponent.setSystemUserAsCurrentUser();
            userName = (String) m_nodeService.getProperty(personNodeRef, ContentModel.PROP_USERNAME);
            
            m_authComponent.setCurrentUser(userName);
            String currentTicket = m_authService.getCurrentTicket();
            user = new User(userName, currentTicket, personNodeRef);
            
            homeSpaceRef = (NodeRef) m_nodeService.getProperty(personNodeRef, ContentModel.PROP_HOMEFOLDER);
            ((User)user).setHomeSpaceId(homeSpaceRef.getId());
            
            tx.commit();
        }
        catch (Throwable ex)
        {
            try
            {
                tx.rollback();
            }
            catch (Exception err)
            {
                logger.error("Failed to rollback transaction", err);
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
        session.setAttribute(AuthenticationHelper.AUTHENTICATION_USER, user);
        session.setAttribute(LoginBean.LOGIN_EXTERNAL_AUTH, Boolean.TRUE);
        
        return user;
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

    /**
     * Return the login page address
     * 
     * @return String
     */
    private String getLoginPage()
    {
       if (m_loginPage == null)
       {
          m_loginPage = Application.getLoginPage(m_context);
       }
       
       return m_loginPage;
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
