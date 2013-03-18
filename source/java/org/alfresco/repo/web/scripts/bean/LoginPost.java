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
package org.alfresco.repo.web.scripts.bean;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Post based login script
 */
public class LoginPost extends AbstractLoginBean
{
    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.DeclarativeWebScript#executeImpl(org.alfresco.web.scripts.WebScriptRequest, org.alfresco.web.scripts.WebScriptResponse)
     */
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status)
    {
        // Extract user and password from JSON POST
        Content c = req.getContent();
        if (c == null)
        {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Missing POST body.");
        }
        
        // TODO accept xml type.
        
        // extract username and password from JSON object
        JSONObject json;
        try
        {
            json = new JSONObject(c.getContent());
            String username = json.getString("username");
            String password = json.getString("password");

            if (username == null || username.length() == 0)
            {
                throw new WebScriptException(HttpServletResponse.SC_BAD_REQUEST, "Username not specified");
            }

            if (password == null)
            {
                throw new WebScriptException(HttpServletResponse.SC_BAD_REQUEST, "Password not specified");
            }

            try
            {
                return login(username, password);
            }
            catch(WebScriptException e)
            {
                status.setCode(e.getStatus());
                status.setMessage(e.getMessage());
                status.setRedirect(true);
                return null;
            }
        } 
        catch (JSONException jErr)
        {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST,
                    "Unable to parse JSON POST body: " + jErr.getMessage());
        }
        catch (IOException ioErr)
        {
            throw new WebScriptException(Status.STATUS_INTERNAL_SERVER_ERROR,
                    "Unable to retrieve POST body: " + ioErr.getMessage());
        }
    }
}