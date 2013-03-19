package org.alfresco.opencmis;

import org.alfresco.service.cmr.repository.NodeRef;

public class PassthroughObjectFilter implements ObjectFilter
{
	@Override
	public boolean filter(NodeRef nodeRef)
	{
		return false;
	}

}
