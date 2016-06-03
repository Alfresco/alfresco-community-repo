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
	private String role;

	public SiteMember()
	{
	}

	public SiteMember(String personId, String role)
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

	public String getRole()
	{
		return role;
	}

	public void setRole(String role)
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