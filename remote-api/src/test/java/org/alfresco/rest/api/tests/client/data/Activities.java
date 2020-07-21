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
