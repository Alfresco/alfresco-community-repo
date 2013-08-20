package org.alfresco.rest.api.tests.client.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.rest.api.tests.client.PublicApiClient.ExpectedPaging;
import org.alfresco.rest.api.tests.client.PublicApiClient.ListResponse;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class FavouriteSite extends SiteImpl implements Serializable
{
	private static final long serialVersionUID = 6106140056062813842L;

	public FavouriteSite(String siteId)
	{
		this.siteId = siteId;
	}

	public FavouriteSite(Site site)
	{
		super(null, site.getSiteId(), site.getGuid(), site.getTitle(), site.getDescription(), site.getVisibility(), site.getType(), site.getRole());
	}

	public FavouriteSite(String networkId, String siteId, String siteGuid, String title, String description,
			String visibility, String type, SiteRole role)
	{
		super(networkId, siteId, siteGuid, title, description, visibility, type, role);
	}
	
	public static FavouriteSite parseFavouriteSite(JSONObject entry)
	{
		Site site = SiteImpl.parseSite(entry);
		FavouriteSite favouriteSite = new FavouriteSite(site);
		return favouriteSite;
	}
	
	public static ListResponse<FavouriteSite> parseFavouriteSites(JSONObject jsonObject)
	{
		JSONObject jsonList = (JSONObject)jsonObject.get("list");
		List<FavouriteSite> favouriteSites = new ArrayList<FavouriteSite>(jsonList.size());
		if(jsonList != null)
		{
			JSONArray jsonEntries = (JSONArray)jsonList.get("entries");
			if(jsonEntries != null)
			{
				for(int i = 0; i < jsonEntries.size(); i++)
				{
					JSONObject jsonEntry = (JSONObject)jsonEntries.get(i);
					JSONObject entry = (JSONObject)jsonEntry.get("entry");
					favouriteSites.add(parseFavouriteSite(entry));
				}
			}
		}

		ExpectedPaging paging = ExpectedPaging.parsePagination(jsonList);
		return new ListResponse<FavouriteSite>(paging, favouriteSites);
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
		FavouriteSite other = (FavouriteSite) obj;
		return getSiteId().equals(other.getSiteId());
	}
}
