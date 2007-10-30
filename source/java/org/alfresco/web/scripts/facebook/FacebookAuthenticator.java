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
package org.alfresco.web.scripts.facebook;

import java.io.IOException;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.web.scripts.WebScriptException;
import org.alfresco.web.scripts.WebScriptServletAuthenticator;
import org.alfresco.web.scripts.WebScriptServletRequest;
import org.alfresco.web.scripts.WebScriptServletResponse;
import org.alfresco.web.scripts.WebScriptDescription.RequiredAuthentication;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class FacebookAuthenticator implements WebScriptServletAuthenticator
{
    // Logger
    private static final Log logger = LogFactory.getLog(FacebookAuthenticator.class);

    // FBML for Facebook login redirect
    private static final String LOGIN_REDIRECT = "<fb:redirect url=\"http://www.facebook.com/login.php?api_key=%s&v=1.0%s\">";
    
    
    /* (non-Javadoc)
     * @see org.aopalliance.intercept.MethodInterceptor#invoke(org.aopalliance.intercept.MethodInvocation)
     */
    public boolean authenticate(RequiredAuthentication required, boolean isGuest, WebScriptServletRequest req, WebScriptServletResponse res)
    {
        // TODO: Refactor with Web Script F/W Extraction
        FacebookServletRequest fbreq = (FacebookServletRequest)req;
        
	    String sessionKey = fbreq.getSessionKey();
	    String user = fbreq.getUserId();

	    if (logger.isDebugEnabled())
	    {
	    	logger.debug("fb_sig_session_key = '" + sessionKey + "'");
	    	logger.debug("fb_sig_user = '" + user + "'");
	    }
	    
        if ((sessionKey == null || sessionKey.length() == 0) || (user == null || user.length() == 0))
        {
        	// session has not been established, redirect to login
        	
        	String apiKey = fbreq.getApiKey();
        	String canvas = (fbreq.isInCanvas()) ? "&canvas" : "";

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
        		
        		res.getWriter().write(redirect);
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
        AuthenticationUtil.setCurrentUser(user);
    	return true;
    }

}
