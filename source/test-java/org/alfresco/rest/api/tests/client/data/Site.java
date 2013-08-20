package org.alfresco.rest.api.tests.client.data;

import org.json.simple.JSONObject;

public interface Site extends JSONAble
{
	Boolean getCreated();
	String getGuid();
	String getNetworkId();
	Boolean isCreated();
	String getSiteId();
	String getTitle();
	String getDescription();
	String getVisibility();
	String getType();
	SiteRole getRole();
	void expected(Object o);
	JSONObject toJSON();
}
