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

import org.alfresco.rest.api.tests.client.PublicApiClient;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.Serializable;
import java.text.Collator;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class SiteGroup implements Serializable, ExpectedComparison, Comparable<SiteGroup>
{
	private static final long serialVersionUID = 505331886661880399L;

	private String role;
	private String id; // group id (aka authority name)
	private Group group;

	public SiteGroup()
	{
		super();
	}

	public SiteGroup(String id, String role)
	{
		this.role = role;
		this.id = id;
	}

	public SiteGroup(String id, Group group, String role)
	{
		this.role = role;
		this.id = id;
		this.group = group;
	}

	public String getRole()
	{
		return role;
	}

	public void setRole(String role)
	{
		this.role = role;
	}

	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public Group getGroup()
	{
		return group;
	}

	public void setGroup(Group group)
	{
		this.group = group;
	}

	public Collator getCollator()
	{
		return collator;
	}

	public void setCollator(Collator collator)
	{
		this.collator = collator;
	}

	public static SiteGroup parseSiteGroup(String siteId, JSONObject jsonObject)
	{
		String id = (String)jsonObject.get("id");
		String role = (String)jsonObject.get("role");
		JSONObject personJSON = (JSONObject)jsonObject.get("group");
		Group group = Group.parseGroup(personJSON);
		SiteGroup siteMember = new SiteGroup(id, group, role);
		return siteMember;
	}

	public static PublicApiClient.ListResponse<SiteGroup> parseGroupMemberOfSites(String siteId, JSONObject jsonObject)
	{
		List<SiteGroup> groups = new ArrayList<SiteGroup>();

		JSONObject jsonList = (JSONObject)jsonObject.get("list");
		assertNotNull(jsonList);

		JSONArray jsonEntries = (JSONArray)jsonList.get("entries");
		assertNotNull(jsonEntries);

		for(int i = 0; i < jsonEntries.size(); i++)
		{
			JSONObject jsonEntry = (JSONObject)jsonEntries.get(i);
			JSONObject entry = (JSONObject)jsonEntry.get("entry");
			groups.add(parseSiteGroup(siteId, entry));
		}

		PublicApiClient.ExpectedPaging paging = PublicApiClient.ExpectedPaging.parsePagination(jsonList);
		return new PublicApiClient.ListResponse<SiteGroup>(paging, groups);
	}

	@SuppressWarnings("unchecked")
	public JSONObject toJSON()
	{
		JSONObject entry = new JSONObject();

		if (getId() != null)
		{
			entry.put("id", getId());
		}

		if (getRole() != null)
		{
			entry.put("role", getRole());
		}

		return entry;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result;
		result = prime * result + ((role == null) ? 0 : role.hashCode());
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
		SiteGroup other = (SiteGroup) obj;
		if (getId() == null)
		{
			if (other.getId() != null)
				return false;
		} else if (!getId().equals(other.getId()))
			return false;
		if (role == null)
		{
			if (other.role != null)
				return false;
		} else if (!role.equals(other.role))
			return false;
		return true;
	}

	@Override
	public void expected(Object o)
	{
		assertTrue(o instanceof SiteGroup);
		SiteGroup other = (SiteGroup)o;
		AssertUtil.assertEquals("id", getId(), other.getId());
	}

	private Collator collator = Collator.getInstance();

	@Override
	public int compareTo(SiteGroup o)
	{
		String displayName = group.getDisplayName();
		return collator.compare(displayName, o.getGroup().getDisplayName());
	}

}
