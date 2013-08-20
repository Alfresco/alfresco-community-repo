package org.alfresco.rest.api.tests.client.data;

import static org.junit.Assert.assertNotNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.rest.api.tests.client.PublicApiClient.ExpectedPaging;
import org.alfresco.rest.api.tests.client.PublicApiClient.ListResponse;
import org.alfresco.rest.api.tests.client.PublicApiClient.Paging;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class Activities implements Serializable
{
	private static final long serialVersionUID = -6280142299994200394L;

	private String userId;
	private ActivitiesParameters activitiesParams;

	public Activities()
	{
	}

	public Activities(String userId, ActivitiesParameters activitiesParams)
	{
		super();
		if(userId == null)
		{
			throw new IllegalArgumentException();
		}
		this.userId = userId;
		this.activitiesParams = activitiesParams;
	}

	public void setUserId(String userId)
	{
		this.userId = userId;
	}

	public String getUserId()
	{
		return userId;
	}

	public ActivitiesParameters getActivitiesParams()
	{
		return activitiesParams;
	}

	public void setActivitiesParams(ActivitiesParameters activitiesParams)
	{
		this.activitiesParams = activitiesParams;
	}

	@Override
	public String toString()
	{
		return "Activities [userId=" + userId + ", activitiesParams="
				+ activitiesParams + "]";
	}
	
	public static ListResponse<Activity> parseActivities(JSONObject jsonObject)
	{
		List<Activity> activities = new ArrayList<Activity>();

		JSONObject jsonList = (JSONObject)jsonObject.get("list");
		assertNotNull(jsonList);

		JSONArray jsonEntries = (JSONArray)jsonList.get("entries");
		assertNotNull(jsonEntries);

		for(int i = 0; i < jsonEntries.size(); i++)
		{
			JSONObject jsonEntry = (JSONObject)jsonEntries.get(i);
			JSONObject entry = (JSONObject)jsonEntry.get("entry");
			activities.add(Activity.parseActivity(entry));
		}

		ExpectedPaging paging = ExpectedPaging.parsePagination(jsonList);
		return new ListResponse<Activity>(paging, activities);
	}
	
	

}
