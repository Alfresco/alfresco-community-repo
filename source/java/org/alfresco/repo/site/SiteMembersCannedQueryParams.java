package org.alfresco.repo.site;

/**
 * Conveys parameters for the site members canned query.
 * 
 * @author steveglover
 *
 */
public class SiteMembersCannedQueryParams
{
	private String shortName;
	private boolean collapseGroups;

	public SiteMembersCannedQueryParams(String shortName, boolean collapseGroups)
	{
		super();
		this.shortName = shortName;
		this.collapseGroups = collapseGroups;
	}

	public String getShortName()
	{
		return shortName;
	}

	public boolean isCollapseGroups()
	{
		return collapseGroups;
	}
}
