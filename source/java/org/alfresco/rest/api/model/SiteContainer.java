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
