package org.alfresco.rest.api.model;

import java.util.Date;

import org.alfresco.rest.framework.resource.UniqueId;

/**
 * Representation of a favourite (document, folder, site, ...).
 * 
 * @author steveglover
 *
 */
public class Favourite
{
	private String targetGuid;
	private Date createdAt;
	private Target target;

	public Date getCreatedAt()
	{
		return createdAt;
	}

	public void setCreatedAt(Date createdAt)
	{
		this.createdAt = createdAt;
	}

	@UniqueId(name="targetGuid")
	public String getTargetGuid()
	{
		return targetGuid;
	}
	
	public void setTargetGuid(String targetGuid)
	{
	    this.targetGuid = targetGuid;
	}
	
	public Target getTarget()
	{
		return target;
	}

	public void setTarget(Target target)
	{
		this.target = target;
	}

	@Override
	public String toString()
	{
		return "Favourite [targetGuid=" + targetGuid
				+ ", createdAt=" + createdAt + ", target=" + target + "]";
	}
}