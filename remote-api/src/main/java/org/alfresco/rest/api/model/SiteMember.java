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

import com.fasterxml.jackson.annotation.JsonProperty;

import org.alfresco.rest.api.people.PeopleEntityResource;
import org.alfresco.rest.framework.resource.EmbeddedEntityResource;
import org.alfresco.rest.framework.resource.UniqueId;

/**
 * Represents site membership.
 * 
 * @author steveglover
 *
 */
public class SiteMember
{
    private String personId;
    private String role;
    private boolean isMemberOfGroup;

    public SiteMember()
    {}

    public SiteMember(String personId, String role)
    {
        super();
        if (personId == null)
        {
            throw new IllegalArgumentException();
        }
        if (role == null)
        {
            throw new IllegalArgumentException();
        }
        this.personId = personId;
        this.role = role;
    }

    public SiteMember(String personId, String role, boolean isMemberOfGroup)
    {
        super();
        if (personId == null)
        {
            throw new IllegalArgumentException();
        }
        if (role == null)
        {
            throw new IllegalArgumentException();
        }
        this.personId = personId;
        this.role = role;
        this.isMemberOfGroup = isMemberOfGroup;
    }

    @JsonProperty("id")
    @UniqueId
    @EmbeddedEntityResource(propertyName = "person", entityResource = PeopleEntityResource.class)
    public String getPersonId()
    {
        return personId;
    }

    public String getRole()
    {
        return role;
    }

    public void setRole(String role)
    {
        if (role == null)
        {
            throw new IllegalArgumentException();
        }
        this.role = role;
    }

    public void setPersonId(String personId)
    {
        if (personId == null)
        {
            throw new IllegalArgumentException();
        }
        this.personId = personId;
    }

    public void setIsMemberOfGroup(boolean isMemberOfGroup)
    {
        this.isMemberOfGroup = isMemberOfGroup;
    }

    public boolean getIsMemberOfGroup()
    {
        return isMemberOfGroup;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((personId == null) ? 0 : personId.hashCode());
        result = prime * result + ((role == null) ? 0 : role.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }

        if (obj == null)
        {
            return false;
        }

        if (getClass() != obj.getClass())
        {
            return false;
        }

        SiteMember other = (SiteMember) obj;
        if (!personId.equals(other.personId))
        {
            return false;
        }

        if (isMemberOfGroup != other.isMemberOfGroup)
        {
            return false;
        }

        return (role == other.role);
    }

    @Override
    public String toString()
    {
        return "SiteMember [personId=" + personId + ", isMemberOfGroup=" + isMemberOfGroup + ", role=" + role + "]";
    }

}
