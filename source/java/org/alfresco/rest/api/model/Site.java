package org.alfresco.rest.api.model;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.site.SiteVisibility;

/**
 * Represents a site.
 * 
 * @author steveglover
 *
 */
public interface Site
{
	public static final String ROLE = "role";

	String getId();
	void setId(String id);
	NodeRef getGuid();
	String getTitle();
	String getDescription();
	SiteVisibility getVisibility();
	String getRole();
}