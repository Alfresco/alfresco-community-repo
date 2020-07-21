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

import org.json.simple.JSONObject;

public class Quota
{
	private String id;
	private Object limit;
	private Object quota;

	public Quota(String id, Object limit, Object quota)
	{
		super();
		this.id = id;
		this.limit = limit;
		this.quota = quota;
	}

	public String getId() {
		return id;
	}

	public Object getLimit() {
		return limit;
	}

	public Object getQuota() {
		return quota;
	}

	public static Quota parseQuota(JSONObject jsonObject)
	{
		String id = (String)jsonObject.get("id");
		Object limit = jsonObject.get("limit");
		Object quota = jsonObject.get("quota");
		Quota ret = new Quota(id, limit, quota);
		return ret;
	}
	
	@SuppressWarnings("unchecked")
	public JSONObject toJSON()
	{
		JSONObject quotaJson = new JSONObject();
		quotaJson.put("id", getId());
		quotaJson.put("quota", getQuota().toString());
		quotaJson.put("limit", getLimit().toString());
		return quotaJson;
	}

	@Override
	public String toString()
	{
		return "Quota [id=" + id + ", limit=" + limit + ", quota=" + quota
				+ "]";
	}
}
