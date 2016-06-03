package org.alfresco.opencmis.mapping;

import java.io.Serializable;

import org.alfresco.opencmis.CMISConnector;
import org.alfresco.opencmis.dictionary.CMISNodeInfo;
import org.alfresco.service.ServiceRegistry;

/**
 * Get the CMIS object id property.
 */
public class NodeRefProperty extends AbstractProperty
{
    public static final String NodeRefPropertyId = "alfcmis:nodeRef";

    /**
     * Construct
     * 
     * @param serviceRegistry ServiceRegistry
     */
    public NodeRefProperty(ServiceRegistry serviceRegistry, CMISConnector connector)
    {
        super(serviceRegistry, connector, NodeRefPropertyId);
    }

    @Override
    public Serializable getValueInternal(CMISNodeInfo nodeInfo)
    {
        if (nodeInfo.getNodeRef() != null)
        {
            if (nodeInfo.isCurrentVersion())
            {
                return nodeInfo.getCurrentNodeNodeRef().toString();
            } else
            {
                return nodeInfo.getNodeRef().toString();
            }
        } else if (nodeInfo.getAssociationRef() != null)
        {
            return nodeInfo.getAssociationRef().toString();
        }

        return null;
    }
}
