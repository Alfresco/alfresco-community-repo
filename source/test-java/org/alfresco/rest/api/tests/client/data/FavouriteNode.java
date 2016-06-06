/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
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
 
package org.alfresco.rest.api.tests.client.data;

import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.Date;

import org.json.simple.JSONObject;

/**
 * Alfresco API - Favourite node representation.
 * 
 * @author steveglover
 *
 */
public class FavouriteNode implements Serializable, ExpectedComparison
{
	private static final long serialVersionUID = -6881545732441221372L;

	protected String nodeId;
	protected String guid;
	protected String name;
	protected String title;
	protected String description;
	protected Date createdAt;
	protected Date modifiedAt;
	protected String createdBy;
	protected String modifiedBy;

	public FavouriteNode()
	{
	}

	/**
	 * For POSTs
	 * 
	 * @param guid String
	 */
	public FavouriteNode(String guid)
	{
		this.guid = guid;
	}

	public FavouriteNode(String id, String guid)
	{
		this.nodeId = id;
		this.guid = guid;
	}

	public void setGuid(String guid)
	{
		this.guid = guid;
	}

	public String getGuid()
	{
		return guid;
	}

	public String getRawNodeId()
	{
		return nodeId;
	}

	public String getNodeId()
	{
		return nodeId;
	}

	public boolean isFolder()
	{
		return false;
	}
	
	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getTitle()
	{
		return title;
	}

	public void setTitle(String title)
	{
		this.title = title;
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
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

	public String getCreatedBy()
	{
		return createdBy;
	}

	public void setCreatedBy(String createdBy)
	{
		this.createdBy = createdBy;
	}

	public String getModifiedBy()
	{
		return modifiedBy;
	}

	public void setModifiedBy(String modifiedBy)
	{
		this.modifiedBy = modifiedBy;
	}

	public void setNodeId(String nodeId)
	{
		this.nodeId = nodeId;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((nodeId == null) ? 0 : nodeId.hashCode());
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
		FavouriteNode other = (FavouriteNode) obj;
		if (nodeId == null)
		{
			if (other.nodeId != null)
				return false;
		} else if (!nodeId.equals(other.nodeId))
			return false;
		return true;
	}

	@SuppressWarnings("unchecked")
	public JSONObject toJSON()
	{
		JSONObject json = new JSONObject();
		json.put("guid", getGuid());
		json.put("id", getNodeId());
		return json;
	}

	@Override
	public void expected(Object o)
	{
		assertTrue(o instanceof FavouriteNode);

		FavouriteNode other = (FavouriteNode) o;

		AssertUtil.assertEquals("id", nodeId, other.getNodeId());
		AssertUtil.assertEquals("guid", guid, other.getGuid());
		AssertUtil.assertEquals("name", name, other.getName());
		AssertUtil.assertEquals("title", title, other.getTitle());
		AssertUtil.assertEquals("description", description, other.getDescription());
		AssertUtil.assertEquals("createdAt", createdAt, other.getCreatedAt());
		if(modifiedAt != null)
		{
			assertTrue(modifiedAt.before(other.getModifiedAt()) || modifiedAt.equals(other.getModifiedAt()));
		}
		AssertUtil.assertEquals("createdBy", createdBy, other.getCreatedBy());
		AssertUtil.assertEquals("modifiedBy", modifiedBy, other.getModifiedBy());
	}

	@Override
	public String toString()
	{
		return "Node [nodeId=" + nodeId + ", guid=" + guid + ", name=" + name
				+ ", title=" + title + ", description=" + description
				+ ", createdAt=" + createdAt + ", modifiedAt=" + modifiedAt
				+ ", createdBy=" + createdBy + ", modifiedBy=" + modifiedBy
				+ "]";
	}
}
