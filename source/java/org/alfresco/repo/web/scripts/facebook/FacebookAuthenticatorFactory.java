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
package org.alfresco.repo.web.scripts.facebook;

import java.io.IOException;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.Authenticator;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.Description.RequiredAuthentication;
import org.springframework.extensions.webscripts.servlet.ServletAuthenticatorFactory;
import org.springframework.extensions.webscripts.servlet.WebScriptServletRequest;
import org.springframework.extensions.webscripts.servlet.WebScriptServletResponse;

/**
 * Web Script Authenticator that supports Facebook authentication
 * mechanism.
 * 
 * Upon success, the request is authenticated as the Facebook User Id.
 * 
 * @author davidc
 */
public class FacebookAuthenticatorFactory implements ServletAuthenticatorFactory
{
    // Logger
    private static final Log logger = LogFactory.getLog(FacebookAuthenticator.class);

    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.servlet.ServletAuthenticatorFactory#create(org.alfresco.web.scripts.servlet.WebScriptServletRequest, org.alfresco.web.scripts.servlet.WebScriptServletResponse)
     */
    public Authenticator create(WebScriptServletRequest req, WebScriptServletResponse res)
    {
        if (!(req instanceof FacebookServletRequest))
        {
            throw new WebScriptException("Facebook request is required; instead a " + req.getClass().getName() + " has been provided");
        }
        return new FacebookAuthenticator((FacebookServletRequest)req, res);
    }


    /**
     * Web Script Authenticator that supports Facebook authentication
     * mechanism.
     * 
     * Upon success, the request is authenticated as the Facebook User Id.
     * 
     * @author davidc
     */
    public class FacebookAuthenticator implements Authenticator
    {
    
        // FBML for Facebook login redirect
        private static final String LOGIN_REDIRECT = "<fb:redirect url=\"http://www.facebook.com/login.php?api_key=%s&v=1.0%s\">";
        
        
        // dependencies
        private FacebookServletRequest fbReq;
        private WebScriptServletResponse fbRes;
        
        private String sessionKey;
        private String user;
        
        /**
         * Construct
         * 
         * @param authenticationService
         * @param req
         * @param res
         */
        public FacebookAuthenticator(FacebookServletRequest req, WebScriptServletResponse res)
        {
            this.fbReq = req;
            this.fbRes = res;
            
            this.sessionKey = fbReq.getSessionKey();
            this.user = fbReq.getUserId();
        }
        
        /* (non-Javadoc)
         * @see org.aopalliance.intercept.MethodInterceptor#invoke(org.aopalliance.intercept.MethodInvocation)
         */
        public boolean authenticate(RequiredAuthentication required, boolean isGuest)
        {
    	    if (logger.isDebugEnabled())
    	    {
    	    	logger.debug("fb_sig_session_key = '" + sessionKey + "'");
    	    	logger.debug("fb_sig_user = '" + user + "'");
    	    }
    	    
            if (emptyCredentials())
            {
            	// session has not been established, redirect to login
            	
            	String apiKey = fbReq.getApiKey();
            	String canvas = (fbReq.isInCanvas()) ? "&canvas" : "";
    
            	if (logger.isDebugEnabled())
            	{
        	    	logger.debug("fb_sig_api_key = '" + apiKey + "'");
        	    	logger.debug("fb_sig_in_canvas = '" + canvas + "'");
            	}
            	
            	try
            	{
            		String redirect = String.format(LOGIN_REDIRECT, apiKey, canvas);
    
                	if (logger.isDebugEnabled())
                		logger.debug("Facebook session not established; redirecting via " + redirect);
            		
            		fbRes.getWriter().write(redirect);
    			}
            	catch (IOException e)
            	{
            		throw new WebScriptException("Redirect to login failed", e);
    			}
            	return false;
            }
    
        	if (logger.isDebugEnabled())
        		logger.debug("Facebook session established; authenticating as user " + user);
    
        	// session has been established, authenticate as Facebook user id
            AuthenticationUtil.setFullyAuthenticatedUser(user);
        	return true;
        }
        
        /* (non-Javadoc)
         * @see org.alfresco.web.scripts.Authenticator#emptyCredentials()
         */
        public boolean emptyCredentials()
        {
            return ((sessionKey == null || sessionKey.length() == 0) || (user == null || user.length() == 0));
        }
    }

}