package org.alfresco.rest.api.model;

import org.alfresco.service.cmr.site.SiteInfo;

/**
 * Represents a user's favourite site.
 * 
 * Represented by a separate class in order to allow other attributes to be added.
 * 
 * @author steveglover
 *
 */
public class FavouriteSite extends Site
{
	public FavouriteSite()
	{
	}
	
	public FavouriteSite(SiteInfo siteInfo, String role)
	{
		super(siteInfo, role);
	}

	@Override
	public String toString()
	{
		return "FavouriteSite [id=" + id + ", guid=" + guid + ", title="
				+ title + ", description=" + description + ", visibility="
				+ visibility + ", role=" + role + "]";
	}
}
