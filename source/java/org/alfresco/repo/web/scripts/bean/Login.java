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

import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Login and establish a ticket
 * 
 * @author davidc
 */
public class Login extends AbstractLoginBean
{   
    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.DeclarativeWebScript#executeImpl(org.alfresco.web.scripts.WebScriptRequest, org.alfresco.web.scripts.WebScriptResponse)
     */
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status)
    {
        // extract username and password
        String username = req.getParameter("u");
        if (username == null || username.length() == 0)
        {
            throw new WebScriptException(HttpServletResponse.SC_BAD_REQUEST, "Username not specified");
        }
        String password = req.getParameter("pw");
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
}