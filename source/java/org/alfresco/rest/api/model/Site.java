/*
 * Copyright (C) 2005-2016 Alfresco Software Limited.
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
package org.alfresco.rest.api.model;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteVisibility;

/**
 * Represents a site.
 * 
 * @author steveglover
 *
 */
public class Site implements Comparable<Site>
{
	public static final String ROLE = "role";

	protected String id; // site id (aka short name)
	protected String guid; // site nodeId
	protected String title;
	protected String description;

	public void setTitle(String title)
	{
		this.title = title;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public void setVisibility(SiteVisibility visibility)
	{
		this.visibility = visibility;
	}

	public void setRole(String role)
	{
		this.role = role;
	}

	protected SiteVisibility visibility;
	protected String role;
	
	public Site()
	{
	}
	
	public Site(SiteInfo siteInfo, String role)
	{
		if(siteInfo == null)
		{
			throw new IllegalArgumentException("Must provide siteInfo");
		}
		this.id = siteInfo.getShortName();
		this.guid = siteInfo.getNodeRef().getId();
		this.title = siteInfo.getTitle();
		this.description = siteInfo.getDescription();
		this.visibility = siteInfo.getVisibility();
		this.role = role;
	}

	public String getId()
	{
		return id;
	}
	
	public void setId(String id)
	{
		this.id = id;
	}
	
	public String getGuid()
	{
		return guid;
	}

	public void setGuid(String guid)
	{
		this.guid = guid;
	}

	public String getTitle()
	{
		return title;
	}

	public String getDescription()
	{
		return description;
	}

	public SiteVisibility getVisibility()
	{
		return visibility;
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

	public String getRole()
	{
		return role;
	}
}