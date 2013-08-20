package org.alfresco.rest.framework.resource.content;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * A binary resource based on a Node reference.
 * 
 * @author Gethin James
 */
public class NodeBinaryResource implements BinaryResource
{

    final NodeRef nodeRef;
    final QName propertyQName;
    
    public NodeBinaryResource(NodeRef nodeRef, QName propertyQName)
    {
        super();
        this.nodeRef = nodeRef;
        this.propertyQName = propertyQName;
    }

    public NodeRef getNodeRef()
    {
        return this.nodeRef;
    }

    public QName getPropertyQName()
    {
        return this.propertyQName;
    }
}
