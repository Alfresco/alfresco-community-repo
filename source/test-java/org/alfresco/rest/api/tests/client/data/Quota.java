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
