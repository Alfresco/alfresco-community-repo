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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.alfresco.rest.framework.resource.UniqueId;

/**
 * Represents a group.
 * 
 * @author cturlica
 *
 */
public class Group implements Comparable<Group>
{

    protected String id; // group id (aka authority name)
    protected String displayName;
    protected Boolean isRoot;
    protected Set<String> parentIds;
    protected Set<String> zones;

    private Map<String, Boolean> setFields = new HashMap<>(7);

    public static final String ID = "id";
    public static final String DISPLAY_NAME = "displayName";
    public static final String IS_ROOT = "isRoot";
    public static final String PARENT_IDS = "parentIds";
    public static final String ZONES = "zones";

    public Group()
    {
    }

    @UniqueId
    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
        setFields.put(ID, true);
    }

    public String getDisplayName()
    {
        return displayName;
    }

    public void setDisplayName(String displayName)
    {
        this.displayName = displayName;
        setFields.put(DISPLAY_NAME, true);
    }

    public Boolean getIsRoot()
    {
        return isRoot;
    }

    public void setIsRoot(Boolean isRoot)
    {
        this.isRoot = isRoot;
        setFields.put(IS_ROOT, true);
    }

    public Set<String> getParentIds()
    {
        return parentIds;
    }

    public void setParentIds(Set<String> parentIds)
    {
        this.parentIds = parentIds;
        setFields.put(PARENT_IDS, true);
    }

    public Set<String> getZones()
    {
        return zones;
    }

    public void setZones(Set<String> zones)
    {
        this.zones = zones;
        setFields.put(ZONES, true);
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

        Group other = (Group) obj;
        return id.equals(other.id);
    }

    @Override
    public int compareTo(Group group)
    {
        return id.compareTo(group.getId());
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public String toString()
    {
        return "Group [id=" + id + ", displayName=" + displayName + ", isRoot=" + isRoot + "]";
    }

    public boolean wasSet(String fieldName)
    {
        Boolean b = setFields.get(fieldName);
        return (b != null ? b : false);
    }
}