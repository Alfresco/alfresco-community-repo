/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2023 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.rest.api.model;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;
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
	private Integer count;

    public Tag()
	{
	}
	
	public Tag(NodeRef nodeRef, String tag)
	{
		this.nodeRef = nodeRef;
		this.tag = tag;
	}

	@JsonProperty("id")
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
	
	public Integer getCount()
	{
	
	    return count;
	}

	public void setCount(Integer count)
	{
	    this.count = count;
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
	public boolean equals(Object o)
	{
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		Tag tag1 = (Tag) o;
		return Objects.equals(nodeRef, tag1.nodeRef) && Objects.equals(tag, tag1.tag) && Objects.equals(count, tag1.count);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(nodeRef, tag, count);
	}

	@Override
	public String toString()
	{
		return "Tag{" + "nodeRef=" + nodeRef + ", tag='" + tag + '\'' + ", count=" + count + '}';
	}

	public static Builder builder()
	{
		return new Builder();
	}

	public static class Builder
	{
		private NodeRef nodeRef;
		private String tag;
		private Integer count;

		public Builder nodeRef(NodeRef nodeRef)
		{
			this.nodeRef = nodeRef;
			return this;
		}

		public Builder tag(String tag)
		{
			this.tag = tag;
			return this;
		}

		public Builder count(Integer count)
		{
			this.count = count;
			return this;
		}

		public Tag create()
		{
			final Tag tag = new Tag();
			tag.setNodeRef(nodeRef);
			tag.setTag(this.tag);
			tag.setCount(count);
			return tag;
		}
	}
}
