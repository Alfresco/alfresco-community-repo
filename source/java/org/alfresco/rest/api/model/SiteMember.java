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

import org.alfresco.rest.api.people.PeopleEntityResource;
import org.alfresco.rest.framework.resource.EmbeddedEntityResource;
import org.alfresco.rest.framework.resource.UniqueId;
import org.alfresco.service.cmr.site.SiteRole;

/**
 * Represents site membership.
 * 
 * @author steveglover
 *
 */
public class SiteMember
{
	private String personId;
	private SiteRole role;

	public SiteMember()
	{
	}

	public SiteMember(String personId, SiteRole role)
	{
		super();
		if(personId == null)
		{
			throw new IllegalArgumentException();
		}
		if(role == null)
		{
			throw new IllegalArgumentException();
		}
		this.personId = personId;
		this.role = role;
	}

	@UniqueId
	@EmbeddedEntityResource(propertyName = "person", entityResource = PeopleEntityResource.class)
	public String getPersonId()
	{
		return personId;
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

	public void setPersonId(String personId)
	{
		if(personId == null)
		{
			throw new IllegalArgumentException();
		}
		this.personId = personId;
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
		
		return(role == other.role);
	}

	@Override
	public String toString()
	{
		return "SiteMember [personId=" + personId + ", role=" + role + "]";
	}
	
}