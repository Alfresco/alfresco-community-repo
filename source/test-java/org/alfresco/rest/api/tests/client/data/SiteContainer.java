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

public class SiteContainer implements Serializable, ExpectedComparison, Comparable<SiteContainer>
{
	private static final long serialVersionUID = 535206187221924534L;

	private String siteId;
	private String id;
	private String folderId;

	public SiteContainer(String siteId, String folderId, String id)
	{
		super();
		this.siteId = siteId;
		this.folderId = folderId;
		this.id = id;
	}
	
	public String getSiteId()
	{
		return siteId;
	}

	public String getFolderId()
	{
		return folderId;
	}

	public String getId()
	{
		return id;
	}
	
	@Override
	public String toString()
	{
		return "SiteContainer [siteId=" + siteId + ", folderId=" + folderId
				+ ", id=" + id + "]";
	}

	public static SiteContainer parseSiteContainer(String siteId, JSONObject json)
	{
		SiteContainer siteContainer = null;

		if(json != null)
		{
			siteContainer = new SiteContainer(siteId, (String)json.get("folderId"), (String)json.get("id"));
		}

		return siteContainer;
	}

	public static ListResponse<SiteContainer> parseSiteContainers(JSONObject jsonObject)
	{
		List<SiteContainer> siteContainers = new ArrayList<SiteContainer>();

		JSONObject jsonList = (JSONObject)jsonObject.get("list");
		assertNotNull(jsonList);

		JSONArray jsonEntries = (JSONArray)jsonList.get("entries");
		assertNotNull(jsonEntries);

		for(int i = 0; i < jsonEntries.size(); i++)
		{
			JSONObject jsonEntry = (JSONObject)jsonEntries.get(i);
			JSONObject entry = (JSONObject)jsonEntry.get("entry");
			siteContainers.add(SiteContainer.parseSiteContainer(null, entry));
		}

		ExpectedPaging paging = ExpectedPaging.parsePagination(jsonList);

		ListResponse<SiteContainer> resp = new ListResponse<SiteContainer>(paging, siteContainers);
		return resp;
	}
	
	@SuppressWarnings("unchecked")
	public JSONObject toJSON()
	{
		JSONObject siteContainerJson = new JSONObject();
		siteContainerJson.put("id", id);
		siteContainerJson.put("folderId", folderId);
		return siteContainerJson;
	}

	@Override
	public void expected(Object o)
	{
		assertTrue(o instanceof SiteContainer);
		
		SiteContainer other = (SiteContainer)o;

		AssertUtil.assertEquals("id", id, other.getId());
		AssertUtil.assertEquals("folderId", folderId, other.getFolderId());
	}

	@Override
	public int compareTo(SiteContainer o)
	{
		return folderId.compareTo(o.getFolderId());
	}
	
}
