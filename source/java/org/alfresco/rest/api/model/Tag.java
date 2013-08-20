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
 * Represents a node tag.
 * 
 * @author steveglover
 *
 */
public class Tag implements Comparable<Tag>
{
	private NodeRef nodeRef;
	private String tag;

	public Tag()
	{
	}
	
	public Tag(NodeRef nodeRef, String tag)
	{
		this.nodeRef = nodeRef;
		this.tag = tag;
	}

	@UniqueId
	public NodeRef getNodeRef()
	{
		return nodeRef;
	}

	public void setNodeRef(NodeRef nodeRef)
	{
		this.nodeRef = nodeRef;
	}

	public String getTag()
	{
		return tag;
	}

	public void setTag(String tag)
	{
		this.tag = tag;
	}

	/*
	 * Note that comparison of tags is based on their string value. This should still
	 * be consistent with equals since tags that are equal implies NodeRefs that are equal.
	 * 
	 * (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Tag o)
	{
		int ret = getTag().compareTo(o.getTag());
		return ret;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((nodeRef == null) ? 0 : nodeRef.hashCode());
		return result;
	}

	/*
	 * Tags are equal if they have the same NodeRef
	 * 
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Tag other = (Tag) obj;
		if (nodeRef == null) {
			if (other.nodeRef != null)
				return false;
		} else if (!nodeRef.equals(other.nodeRef))
			return false;
		return true;
	}

	@Override
	public String toString()
	{
		return "Tag [nodeRef=" + nodeRef + ", tag=" + tag + "]";
	}
	
}
