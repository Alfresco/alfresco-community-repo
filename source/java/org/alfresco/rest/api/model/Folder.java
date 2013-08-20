package org.alfresco.rest.api.model;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.chemistry.opencmis.commons.data.Properties;

/**
 * Representation of a folder node.
 * 
 * @author steveglover
 *
 */
public class Folder extends Node
{
	public Folder()
	{
		super();
	}

	public Folder(NodeRef nodeRef, Properties properties)
	{
		super(nodeRef, properties);
	}

	public Folder(NodeRef nodeRef, Map<QName, Serializable> nodeProps)
	{
		super(nodeRef, nodeProps);
	}

	@Override
	public String toString()
	{
		return "Folder [nodeRef=" + nodeRef + ", name=" + name + ", title="
				+ title + ", description=" + description + ", createdAt="
				+ createdAt + ", modifiedAt=" + modifiedAt + ", createdBy="
				+ createdBy + ", modifiedBy=" + modifiedBy + "]";
	}
}
