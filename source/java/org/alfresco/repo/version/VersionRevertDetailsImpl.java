package org.alfresco.repo.version;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * Implementation of VersionRevertDetails
 * @author mrogers
 * @since 4.2
 */
/*package*/class VersionRevertDetailsImpl implements VersionRevertDetails
{
	private NodeRef nodeRef;
	private QName nodeType;
	
	
	Map<String, Serializable> revertedProperties;

	public void setNodeRef(NodeRef nodeRef) 
	{
		this.nodeRef = nodeRef;
	}
	
	@Override
	public NodeRef getNodeRef() 
	{
		return nodeRef;
	}

	public void setNodeType(QName nodeType) {
		this.nodeType = nodeType;
	}

	@Override
	public QName getNodeType() {
		return nodeType;
	}

}
