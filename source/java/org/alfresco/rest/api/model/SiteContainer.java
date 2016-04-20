package org.alfresco.rest.api.model;

import org.alfresco.rest.framework.resource.UniqueId;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Represents a site container.
 * 
 * @author steveglover
 *
 */
public class SiteContainer implements Comparable<SiteContainer>
{
	private String folderId;
	private NodeRef nodeRef;

	public SiteContainer()
	{
	}

	public SiteContainer(String folderId, NodeRef nodeRef)
	{
		super();
		if(folderId == null)
		{
			throw new IllegalArgumentException();
		}
		if(nodeRef == null)
		{
			throw new IllegalArgumentException();
		}
		this.folderId = folderId;
		this.nodeRef = nodeRef;
	}

	public String getFolderId()
	{
		return folderId;
	}

	@UniqueId
	public NodeRef getNodeRef()
	{
		return nodeRef;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((folderId == null) ? 0 : folderId.hashCode());
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
		
		SiteContainer other = (SiteContainer) obj;
		return nodeRef.equals(other.getNodeRef());
	}

	@Override
	public int compareTo(SiteContainer other)
	{
		return folderId.compareTo(other.getFolderId());
	}

	@Override
	public String toString()
	{
		return "SiteContainer [folderId=" + folderId + ", nodeRef="
				+ nodeRef + "]";
	}
	
}
