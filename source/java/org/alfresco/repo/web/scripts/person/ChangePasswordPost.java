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
 * http://www.alfresco.com/legal/licensing
 */
package org.alfresco.repo.web.scripts.person;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.springframework.extensions.surf.util.Content;
import org.alfresco.web.scripts.DeclarativeWebScript;
import org.alfresco.web.scripts.Status;
import org.alfresco.web.scripts.WebScriptException;
import org.alfresco.web.scripts.WebScriptRequest;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Webscript implementation for the POST method for 'changepassword' API.
 * 
 * @author Kevin Roast
 */
public class ChangePasswordPost extends DeclarativeWebScript
{
    private static final String PARAM_NEWPW = "newpw";
    private static final String PARAM_OLDPW = "oldpw";
    private AuthenticationService authenticationService;
    private AuthorityService authorityService;
    
    
    /**
     * @param authenticationService    the AuthenticationService to set
     */
    public void setAuthenticationService(AuthenticationService authenticationService)
    {
        this.authenticationService = authenticationService;
    }
    
    /**
     * @param authorityService          the AuthorityService to set
     */
    public void setAuthorityService(AuthorityService authorityService)
    {
        this.authorityService = authorityService;
    }
    
    
    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.DeclarativeWebScript#executeImpl(org.alfresco.web.scripts.WebScriptRequest, org.alfresco.web.scripts.Status)
     */
    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status)
    {
        // Extract user name from the URL - cannot be null or webscript desc would not match
        String userName = req.getExtensionPath();
        
        // Extract old and new password details from JSON POST
        Content c = req.getContent();
        if (c == null)
        {
            throw new WebScriptException(Status.STATUS_INTERNAL_SERVER_ERROR, "Missing POST body.");
        }
        JSONObject json;
        try
        {
            json = new JSONObject(c.getContent());
            
            String oldPassword = null;
            String newPassword;
            
            // admin users can change/set a password without knowing the old one
            boolean isAdmin = authorityService.hasAdminAuthority();
            if (!isAdmin || (userName.equalsIgnoreCase(authenticationService.getCurrentUserName())))
            {
                if (!json.has(PARAM_OLDPW) || json.getString(PARAM_OLDPW).length() == 0)
                {
                    throw new WebScriptException(Status.STATUS_BAD_REQUEST,
                        "Old password 'oldpw' is a required POST parameter.");
                }
                oldPassword = json.getString(PARAM_OLDPW);
            }
            if (!json.has(PARAM_NEWPW) || json.getString(PARAM_NEWPW).length() == 0)
            {
                throw new WebScriptException(Status.STATUS_BAD_REQUEST,
                    "New password 'newpw' is a required POST parameter.");
            }
            newPassword = json.getString(PARAM_NEWPW);
            
            // update the password
            // an Admin user can update without knowing the original pass - but must know their own!
            if (!isAdmin || (userName.equalsIgnoreCase(authenticationService.getCurrentUserName())))
            {
                authenticationService.updateAuthentication(userName, oldPassword.toCharArray(), newPassword.toCharArray());
            }
            else
            {
                authenticationService.setAuthentication(userName, newPassword.toCharArray());
            }
        }
        catch (AuthenticationException err)
        {
            throw new WebScriptException(Status.STATUS_INTERNAL_SERVER_ERROR,
                    "Do not have appropriate auth or wrong auth details provided.");
        }
        catch (JSONException jErr)
        {
            throw new WebScriptException(Status.STATUS_INTERNAL_SERVER_ERROR,
                    "Unable to parse JSON POST body: " + jErr.getMessage());
        }
        catch (IOException ioErr)
        {
            throw new WebScriptException(Status.STATUS_INTERNAL_SERVER_ERROR,
                    "Unable to retrieve POST body: " + ioErr.getMessage());
        }
        
        Map<String, Object> model = new HashMap<String, Object>(1, 1.0f);
        model.put("success", Boolean.TRUE);
        return model;
    }
}