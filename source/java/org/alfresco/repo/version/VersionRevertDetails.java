package org.alfresco.repo.version;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

public interface VersionRevertDetails 
{
	/**
	 * Node to revert
	 * @return the noderef of the node to revert
	 */
	NodeRef getNodeRef();
	
	/**
	 * Type of node that is being reverted
	 * @return the type of the node that is being reverted
	 */
	public QName getNodeType();
	
}
