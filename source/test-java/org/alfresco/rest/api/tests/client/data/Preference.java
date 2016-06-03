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

public class Preference	implements Serializable, ExpectedComparison, Comparable<Preference>
{
	private static final long serialVersionUID = -4187479305268687836L;
	
	private String id;
	private String value;
	
	public Preference(String id, String value)
	{
		super();
		this.id = id;
		this.value = value;
	}

	public String getId()
	{
		return id;
	}

	public String getValue()
	{
		return value;
	}

	@Override
	public String toString()
	{
		return "Preference [id=" + id + ", value=" + value + "]";
	}
	
	public static Preference parsePreference(JSONObject jsonObject)
	{
		String id = (String)jsonObject.get("id");
		String value = jsonObject.get("value").toString();
		Preference preference = new Preference(id, value);
		return preference;
	}
	
	public static ListResponse<Preference> parsePreferences(JSONObject jsonObject)
	{
		List<Preference> preferences = new ArrayList<Preference>();

		JSONObject jsonList = (JSONObject)jsonObject.get("list");
		assertNotNull(jsonList);

		JSONArray jsonEntries = (JSONArray)jsonList.get("entries");
		assertNotNull(jsonEntries);

		for(int i = 0; i < jsonEntries.size(); i++)
		{
			JSONObject jsonEntry = (JSONObject)jsonEntries.get(i);
			JSONObject entry = (JSONObject)jsonEntry.get("entry");
			preferences.add(Preference.parsePreference(entry));
		}

		ExpectedPaging paging = ExpectedPaging.parsePagination(jsonList);
		ListResponse<Preference> resp = new ListResponse<Preference>(paging, preferences);
		return resp;
	}

	@SuppressWarnings("unchecked")
	public JSONObject toJSON()
	{
		JSONObject entry = new JSONObject();
		entry.put("id", getId());
		entry.put("value", getValue());
		return entry;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
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
		Preference other = (Preference) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}
	
	@Override
	public void expected(Object o)
	{
		assertTrue(o instanceof Preference);
		
		Preference other = (Preference)o;
		
		AssertUtil.assertEquals("id", id, other.getId());
		AssertUtil.assertEquals("value", value, other.getValue());
	}

	@Override
	public int compareTo(Preference o)
	{
		return id.compareTo(o.getId());
	}

}
