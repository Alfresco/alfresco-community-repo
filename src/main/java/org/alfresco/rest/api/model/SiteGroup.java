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
import org.alfresco.rest.api.groups.GroupsEntityResource;
import org.alfresco.rest.framework.resource.EmbeddedEntityResource;
import org.alfresco.rest.framework.resource.UniqueId;

public class SiteGroup implements Comparable<SiteGroup>
{
    private String role;
    private String id; // group id (aka authority name)

    public SiteGroup() {}

    public SiteGroup(String id, String role)
    {
        if (id == null)
        {
            throw new IllegalArgumentException();
        }
        if (role == null)
        {
            throw new IllegalArgumentException();
        }
        this.role = role;
        this.id = id;
    }

    public static SiteGroup getMemberOfSite(String id, String role)
    {
        return new SiteGroup(id, role);
    }

    @JsonProperty("id")
    @UniqueId
    @EmbeddedEntityResource(propertyName = "group", entityResource = GroupsEntityResource.class)
    public String getId()
    {
        return id;
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

    public void setId(String id)
    {
        if (id == null)
        {
            throw new IllegalArgumentException();
        }
        this.id = id;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((role == null) ? 0 : role.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
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

        SiteGroup other = (SiteGroup) obj;
        if (role != other.role)
        {
            return false;
        }

        return id.equals(other.id);
    }

    @Override
    public int compareTo(SiteGroup o)
    {
        int i = id.compareTo(o.getId());
        if (i == 0)
        {
            i = role.compareTo(o.getRole());
        }
        return i;
    }

    @Override
    public String toString()
    {
        return "SiteGroup [role='" + role + '\'' + ", id='" + id + '\'' + ", role='" + role + '\'' + "]";
    }
}