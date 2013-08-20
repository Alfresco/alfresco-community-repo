package org.alfresco.rest.api.tests.client.data;

import java.io.Serializable;

public class ActivitiesParameters implements Serializable
{
	private static final long serialVersionUID = 13166440680499285L;

	private String siteId;
	private String who;

	public ActivitiesParameters(String siteId, String who)
	{
		super();
		this.siteId = siteId;
		this.who = who;
	}

	public String getSiteId()
	{
		return siteId;
	}

	public String getWho()
	{
		return who;
	}
	
}
