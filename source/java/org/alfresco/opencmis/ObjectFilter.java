package org.alfresco.opencmis;

import org.alfresco.service.cmr.repository.NodeRef;

public interface ObjectFilter
{
	public boolean filter(NodeRef nodeRef);
}
