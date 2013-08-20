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

import org.alfresco.rest.api.sites.SiteEntityResource;
import org.alfresco.rest.framework.resource.EmbeddedEntityResource;
import org.alfresco.rest.framework.resource.UniqueId;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteRole;

/**
 * Represents membership of a site.
 * 
 * @author steveglover
 *
 */
public class MemberOfSite implements Comparable<MemberOfSite>
{
	private SiteRole role;
	private String siteShortName;
	private NodeRef guid;

	public MemberOfSite()
	{
	}

    public MemberOfSite(String siteShortName, NodeRef siteGuid, SiteRole role)
    {
		super();
		if(siteShortName == null)
		{
			throw new IllegalArgumentException();
		}
		if(role == null)
		{
			throw new IllegalArgumentException();
		}
		if(siteGuid == null)
		{
			throw new IllegalArgumentException();
		}
		this.role = role;
		this.siteShortName = siteShortName;
		this.guid = siteGuid;
	}

    public static MemberOfSite getMemberOfSite(SiteInfo siteInfo, SiteRole siteRole)
    {
    	MemberOfSite memberOfSite = new MemberOfSite(siteInfo.getShortName(), siteInfo.getNodeRef(), siteRole);
    	return memberOfSite;
    }

	@UniqueId
    @EmbeddedEntityResource(propertyName = "site", entityResource = SiteEntityResource.class)
    public String getSiteShortName()
	{
		return siteShortName;
	}
	
	public NodeRef getGuid()
	{
		return guid;
	}
	
	public void setGuid(NodeRef guid)
	{
		this.guid = guid;
	}

	public SiteRole getRole()
	{
		return role;
	}
	
	public void setRole(SiteRole role)
	{
		if(role == null)
		{
			throw new IllegalArgumentException();
		}
		this.role = role;
	}

	public void setSiteShortName(String siteShortName)
	{
		if(siteShortName == null)
		{
			throw new IllegalArgumentException();
		}
		this.siteShortName = siteShortName;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((role == null) ? 0 : role.hashCode());
		result = prime * result
				+ ((siteShortName == null) ? 0 : siteShortName.hashCode());
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
		
		MemberOfSite other = (MemberOfSite) obj;
		if (role != other.role)
		{
			return false;
		}

		return siteShortName.equals(other.siteShortName);
	}

	@Override
	public int compareTo(MemberOfSite o)
	{
        int i = siteShortName.compareTo(o.getSiteShortName());
        if(i == 0)
        {
        	i = role.compareTo(o.getRole());
        }
        return i;
	}

	@Override
	public String toString()
	{
		return "MemberOfSite [role=" + role + ", siteShortName="
				+ siteShortName + ", siteGuid=" + guid + "]";
	}
}
