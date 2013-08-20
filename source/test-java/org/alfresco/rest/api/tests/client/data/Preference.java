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
		String value = (String)jsonObject.get("value");
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
