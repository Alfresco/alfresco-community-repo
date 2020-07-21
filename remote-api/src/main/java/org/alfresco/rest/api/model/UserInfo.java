/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.rest.api.model;

import java.io.Serializable;

/**
 * Representation of a user info
 *
 * @author janv
 */
public class UserInfo implements Serializable
{
    private String id;
    private String displayName;

    public UserInfo()
    {
    }

    public UserInfo(String id, String firstName, String lastName)
    {
        this.id = id;
        this.displayName = ((firstName != null ? firstName + " " : "") + (lastName != null ? lastName : "")).trim();
    }

    public String getDisplayName()
    {
        return displayName;
    }

    public String getId()
    {
        return id;
    }

    @Override
    public String toString()
    {
        return "User [id=" + id + ", displayName=" + displayName + "]";
    }
}
