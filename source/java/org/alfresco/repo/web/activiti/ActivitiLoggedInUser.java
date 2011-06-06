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

package org.alfresco.repo.web.activiti;

import org.activiti.explorer.identity.LoggedInUser;

/**
 * Logged in user for Activiti admin ui, based on the authenticated person node
 * properties.
 * 
 * @author Frederik Heremans
 */
public class ActivitiLoggedInUser implements LoggedInUser
{

    private static final long serialVersionUID = 1L;

    private String id;

    private String firstName;

    private String lastName;

    private boolean admin;

    private boolean user;

    public ActivitiLoggedInUser(String id)
    {
        this.id = id;
    }

    public String getFirstName()
    {
        return firstName;
    }

    public String getFullName()
    {
        return getFirstName() + " " + getLastName();
    }

    public String getId()
    {
        return id;
    }

    public String getLastName()
    {
        return lastName;
    }

    public String getPassword()
    {
        // Password is not exposed, not needed anymore after authentication
        return null;
    }

    public boolean isAdmin()
    {
        return admin;
    }

    public boolean isUser()
    {
        return user;
    }
    
    public void setFirstName(String firstName)
    {
        this.firstName = firstName;
    }
    
    public void setLastName(String lastName)
    {
        this.lastName = lastName;
    }
    
    public void setAdmin(boolean admin)
    {
        this.admin = admin;
    }
    
    public void setUser(boolean user)
    {
        this.user = user;
    }
}
