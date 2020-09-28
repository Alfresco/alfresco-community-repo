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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.rest.api.tests.client.PublicApiClient.ExpectedPaging;
import org.alfresco.rest.api.tests.client.PublicApiClient.ListResponse;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class Tag implements Serializable, ExpectedComparison, Comparable<Tag>
{
	private static final long serialVersionUID = -5730063374759199632L;

	private String nodeId;
	private String id;
	private String tag;

	public Tag(String tag)
	{
		super();
		this.tag = tag;
	}

	public Tag(String id, String tag)
	{
		super();
		this.id = id;
		this.tag = tag;
	}

	public Tag(String nodeId, String id, String tag)
	{
		super();
		this.nodeId = nodeId;
		this.id = id;
		this.tag = tag;
	}

	public void setTag(String tag)
	{
		this.tag = tag;
	}

	public String getNodeId()
	{
		return nodeId;
	}

	public void setNodeId(String nodeId)
	{
		this.nodeId = nodeId;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public String getId()
	{
		return id;
	}
	
	public String getTag()
	{
		return tag;
	}
	
	@Override
	public String toString()
	{
		return "Tag [nodeId=" + nodeId + ", id=" + id + ", tag=" + tag
				+ "]";
	}

	public static Tag parseTag(String nodeId, JSONObject jsonObject)
	{
		String id = (String)jsonObject.get("id");
		String value = (String)jsonObject.get("tag");
		Tag tag = new Tag(nodeId, id, value);
		return tag;
	}
	
	public static ListResponse<Tag> parseTags(String nodeId, JSONObject jsonObject)
	{
		List<Tag> tags = new ArrayList<Tag>();

		JSONObject jsonList = (JSONObject)jsonObject.get("list");
		assertNotNull(jsonList);

		JSONArray jsonEntries = (JSONArray)jsonList.get("entries");
		assertNotNull(jsonEntries);

		for(int i = 0; i < jsonEntries.size(); i++)
		{
			JSONObject jsonEntry = (JSONObject)jsonEntries.get(i);
			JSONObject entry = (JSONObject)jsonEntry.get("entry");
			tags.add(parseTag(nodeId, entry));
		}
		
		ExpectedPaging paging = ExpectedPaging.parsePagination(jsonList);
		ListResponse<Tag> resp = new ListResponse<Tag>(paging, tags);
		return resp;
	}
	
	@SuppressWarnings("unchecked")
	public JSONObject toJSON()
	{
		JSONObject entry = new JSONObject();
//		entry.put("id", getId());
		entry.put("tag", getTag());
		return entry;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Tag other = (Tag) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	@Override
	public int compareTo(Tag o)
	{
		return tag.compareTo(o.getTag());
	}

	@Override
	public void expected(Object o)
	{
		assertTrue(o instanceof Tag);
		
		Tag other = (Tag)o;
		
		AssertUtil.assertEquals("id", id, other.getId());
		// case insensitive comparison
		AssertUtil.assertEquals("tag", tag.toUpperCase(), other.getTag().toUpperCase());
	}
	
}
