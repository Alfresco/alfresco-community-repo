package org.alfresco.rest.api.tests.client.data;

import static org.junit.Assert.assertTrue;

import org.json.simple.JSONObject;

public class SiteFavouriteTarget implements FavouritesTarget
{
	private Site site;

	public SiteFavouriteTarget(Site site)
	{
		super();
		this.site = site;
	}

	public Site getSite()
	{
		return site;
	}

	@SuppressWarnings("unchecked")
	@Override
	public JSONObject toJSON()
	{
		JSONObject favouriteJson = new JSONObject();
		favouriteJson.put("site", getSite().toJSON());
		return favouriteJson;
	}

	@Override
	public String toString()
	{
		return "SiteFavouritesTarget [site=" + site + "]";
	}

	@Override
	public void expected(Object o)
	{
		assertTrue(o instanceof SiteFavouriteTarget);

		SiteFavouriteTarget other = (SiteFavouriteTarget) o;
		site.expected(other.getSite());
	}
	
	public String getTargetGuid()
	{
		return site.getGuid();
	}
}
