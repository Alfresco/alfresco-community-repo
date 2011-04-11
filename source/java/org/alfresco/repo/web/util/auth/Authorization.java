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
package org.alfresco.repo.web.util.auth;

import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.util.ParameterCheck;

/**
 * Helper to process username / password pairs passed to the remote tier
 * 
 * Identifies whether username / password is a ticket.
 * 
 * Is ticket, if one of the following is true:
 * 
 * a) Username == "ROLE_TICKET" (in any case)
 * b) Username is not specified (i.e. null)
 * c) Username is zero length
 */
public class Authorization
{
    public static String TICKET_USERID = PermissionService.ROLE_PREFIX + "TICKET";
    
    private String username;
    private String password;
    private String ticket;
    
    /**
     * Construct
     * 
     * @param authorization
     */
    public Authorization(String authorization)
    {
        ParameterCheck.mandatoryString("authorization", authorization);
        String[] parts = authorization.split(":");
        if (parts.length == 1)
        {
            setUser(null, parts[0]);
        }
        else if (parts.length == 2)
        {
            setUser(parts[0], parts[1]);
        }
        else
        {
            throw new IllegalArgumentException("authorization does not consist of username and password");
        }
    }

    /**
     * Construct
     * 
     * @param username
     * @param password
     */
    public Authorization(String username, String password)
    {
        setUser(username, password);
    }

    private void setUser(String username, String password)
    {
        this.username = username;
        this.password = password;
        if (username == null || username.length() == 0 || username.equalsIgnoreCase(TICKET_USERID))
        {
            
            this.ticket = password;
        }
    }
    
    public String getUserName()
    {
        return username;
    }
    
    public String getPassword()
    {
        return password;
    }
    
    public boolean isTicket()
    {
        return ticket != null;
    }
    
    public String getTicket()
    {
        return ticket;
    }
    
}
