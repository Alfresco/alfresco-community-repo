package org.alfresco.repo.site;

import org.alfresco.service.cmr.repository.NodeRef;

public class SiteContainersCannedQueryParams
{
	public static enum SortFields { ContainerName };

	private NodeRef siteNodeRef;

	public SiteContainersCannedQueryParams(NodeRef siteNodeRef)
	{
		super();
		this.siteNodeRef = siteNodeRef;
	}

	public NodeRef getSiteNodeRef()
	{
		return siteNodeRef;
	}
}
