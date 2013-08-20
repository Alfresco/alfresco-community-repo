package org.alfresco.rest.api.model;


/**
 * A site target favourite.
 * 
 * @author steveglover
 *
 */
public class SiteTarget extends Target
{
	private Site site;

	public SiteTarget()
	{
		super();
	}

	public SiteTarget(Site site)
	{
		super();
		this.site = site;
	}

	public void setSite(Site site)
	{
		this.site = site;
	}

	public Site getSite()
	{
		return site;
	}

	@Override
	public String toString()
	{
		return "SiteTarget [site=" + site + "]";
	}
}
