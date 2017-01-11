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

import org.alfresco.rest.framework.resource.UniqueId;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteVisibility;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a site.
 * 
 * @author steveglover
 *
 */
public class Site implements Comparable<Site>
{
    protected String id; // site id (aka short name)
    protected String guid; // site nodeId
    protected String title;
    protected String description;
    protected SiteVisibility visibility;
    protected String preset;
    protected String role;

    private Map<String, Boolean> setFields = new HashMap<>(7);

    public static final String ID = "id";
    public static final String GUID = "guid";
    public static final String TITLE = "title";
    public static final String DESCRIPTION = "description";
    public static final String VISIBILITY = "visibility";
    public static final String PRESET = "preset";
    public static final String ROLE = "role";

    public Site()
    {
    }

    public Site(SiteInfo siteInfo, String role)
    {
        if (siteInfo == null)
        {
            throw new IllegalArgumentException("Must provide siteInfo");
        }
        setId(siteInfo.getShortName());
        setGuid(siteInfo.getNodeRef().getId());
        setTitle(siteInfo.getTitle());
        setDescription(siteInfo.getDescription());
        setVisibility(siteInfo.getVisibility());
        setPreset(siteInfo.getSitePreset());
        setRole(role);
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

    public String getGuid()
    {
        return guid;
    }

    public void setGuid(String guid)
    {
        this.guid = guid;
        setFields.put(GUID, true);
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
        setFields.put(TITLE, true);
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
        setFields.put(DESCRIPTION, true);
    }

    public SiteVisibility getVisibility()
    {
        return visibility;
    }

    public void setVisibility(SiteVisibility visibility)
    {
        this.visibility = visibility;
        setFields.put(VISIBILITY, true);
    }

    public String getPreset()
    {
        return preset;
    }

    public void setPreset(String preset)
    {
        this.preset = preset;
        setFields.put(PRESET, true);
    }

    public String getRole()
    {
        return role;
    }

    public void setRole(String role)
    {
        this.role = role;
        setFields.put(ROLE, true);
    }

    public boolean wasSet(String fieldName)
    {
        Boolean b = setFields.get(fieldName);
        return (b != null ? b : false);
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

        Site other = (Site) obj;
        return id.equals(other.id);
    }

    @Override
    public int compareTo(Site site)
    {
        return id.compareTo(site.getId());
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public String toString()
    {
        return "Site [id=" + id + ", guid=" + guid + ", title=" + title
                + ", description=" + description + ", visibility=" + visibility
                + "]";
    }
}