package org.alfresco.rest.api.model;

import java.text.Collator;
import java.util.Date;

import org.alfresco.rest.api.sites.SiteEntityResource;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.rest.framework.resource.EmbeddedEntityResource;
import org.alfresco.rest.framework.resource.UniqueId;
import org.alfresco.util.Pair;
import org.codehaus.jackson.annotate.JsonIgnore;

/**
 * Representation of a site membership request for a specific user.
 * 
 * Ordering is by id (site id).
 * 
 * @author steveglover
 *
 */
public class SiteMembershipRequest implements Comparable<SiteMembershipRequest>
{
	private static Collator collator = Collator.getInstance();

    private String id; // site id
    private String message;
    private Date createdAt;
    private Date modifiedAt;
    private String title; // for sorting only

	public static Pair<String, String> splitId(String id)
	{
		int idx = id.indexOf(":");
		if(idx != -1)
		{
			String workflowId = id.substring(0, idx);
			String key = id.substring(idx + 1);
			Pair<String, String> ret = new Pair<String, String>(workflowId, key);
			return ret;
		}
		else
		{
			throw new InvalidArgumentException("Site invite id is invalid: " + id);
		}
	}

    public SiteMembershipRequest()
    {
    }

    @EmbeddedEntityResource(propertyName = "site", entityResource = SiteEntityResource.class)
	@UniqueId
    public String getId()
    {
		return id;
    }

    public void setId(String id)
    {
		this.id = id;
    }
    
	public void setTitle(String title)
	{
		this.title = title;
	}

	@JsonIgnore
	public String getTitle()
	{
		return title;
	}

	public Date getCreatedAt()
	{
		return createdAt;
	}

	public void setCreatedAt(Date createdAt)
	{
		this.createdAt = createdAt;
	}

	public Date getModifiedAt()
	{
		return modifiedAt;
	}

	public void setModifiedAt(Date modifiedAt)
	{
		this.modifiedAt = modifiedAt;
	}

	public String getMessage()
	{
		return message;
	}

	public void setMessage(String message)
	{
		this.message = message;
	}

	@Override
	public String toString()
	{
		return "SiteMembershipRequest [id=" + id + ", message=" + message + ", createdAt=" + createdAt
				+ ", modifiedAt=" + modifiedAt + "]";
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
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SiteMembershipRequest other = (SiteMembershipRequest) obj;
		if (id == null)
		{
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	@Override
	public int compareTo(SiteMembershipRequest o)
	{
		int ret = 0;

		if(title == null && o.getTitle() != null)
		{
			ret = -1;
		}
		else if(title != null && o.getTitle() == null)
		{
			ret = 1;
		}
		else
		{
			ret = collator.compare(title, o.getTitle());
		}

		return ret;
	}
}
