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
package org.alfresco.repo.site;

import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteRole;

/**
 * Conveys information for a member of a site.
 * 
 * @author steveglover
 *
 */
public class SiteMembership
{
	private SiteInfo siteInfo;
	private String personId;
	private String firstName;
	private String lastName;
	private SiteRole role;

	public SiteMembership(SiteInfo siteInfo, String personId, String firstName, String lastName, SiteRole role)
	{
		super();
		if(siteInfo == null)
		{
			throw new java.lang.IllegalArgumentException();
		}
		if(personId == null)
		{
			throw new java.lang.IllegalArgumentException();
		}
		if(firstName == null)
		{
			throw new java.lang.IllegalArgumentException();
		}
		if(lastName == null)
		{
			throw new java.lang.IllegalArgumentException();
		}
		if(role == null)
		{
			throw new java.lang.IllegalArgumentException();
		}
		this.siteInfo = siteInfo;
		this.personId = personId;
		this.firstName = firstName;
		this.lastName = lastName;
		this.role = role;
	}

	public SiteMembership(SiteInfo siteInfo, String personId, SiteRole role)
	{
		super();
		if(siteInfo == null)
		{
			throw new java.lang.IllegalArgumentException();
		}
		if(personId == null)
		{
			throw new java.lang.IllegalArgumentException();
		}
		if(role == null)
		{
			throw new java.lang.IllegalArgumentException();
		}

		this.siteInfo = siteInfo;
		this.personId = personId;
		this.role = role;
	}

	public SiteInfo getSiteInfo()
	{
		return siteInfo;
	}

	public String getPersonId()
	{
		return personId;
	}

	public String getFirstName()
	{
		return firstName;
	}

	public String getLastName()
	{
		return lastName;
	}

	public SiteRole getRole()
	{
		return role;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((personId == null) ? 0 : personId.hashCode());
		result = prime * result + ((role == null) ? 0 : role.hashCode());
		result = prime * result
				+ ((getSiteInfo() == null) ? 0 : getSiteInfo().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SiteMembership other = (SiteMembership) obj;
		if (personId == null) {
			if (other.personId != null)
				return false;
		} else if (!personId.equals(other.personId))
			return false;
		if (role != other.role)
			return false;
		if (getSiteInfo() == null) {
			if (other.getSiteInfo() != null)
				return false;
		} else if (!getSiteInfo().equals(other.getSiteInfo()))
			return false;
		return true;
	}

	@Override
	public String toString()
	{
		return "SiteMembership [siteInfo=" + getSiteInfo()
				+ ", personId=" + personId + ", firstName=" + firstName
				+ ", lastName=" + lastName + ", role=" + role + "]";
	}

}
