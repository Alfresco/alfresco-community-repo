/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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

import org.alfresco.rest.framework.resource.UniqueId;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteRole;
import org.alfresco.service.cmr.site.SiteVisibility;

/**
 * Represents a site.
 * 
 * @author steveglover
 *
 */
public class SiteImpl implements Site, Comparable<SiteImpl>
{
	protected String id;
	protected NodeRef guid;
	protected String title;
	protected String description;
	protected SiteVisibility visibility;
	protected SiteRole role;
	
	public SiteImpl()
	{
	}
	
	public SiteImpl(SiteInfo siteInfo, SiteRole role)
	{
		if(siteInfo == null)
		{
			throw new IllegalArgumentException("Must provide siteInfo");
		}
		this.id = siteInfo.getShortName();
		this.guid = siteInfo.getNodeRef();
		this.title = siteInfo.getTitle();
		this.description = siteInfo.getDescription();
		this.visibility = siteInfo.getVisibility();
		this.role = role;
	}

	@UniqueId
	public String getId()
	{
		return id;
	}
	
	public void setId(String id)
	{
		this.id = id;
	}
	
	public NodeRef getGuid()
	{
		return guid;
	}

	public void setGuid(NodeRef guid)
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
		
		SiteImpl other = (SiteImpl) obj;
		return id.equals(other.id);
	}

	@Override
	public int compareTo(SiteImpl site)
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

	@Override
	public SiteRole getRole()
	{
		return role;
	}
}