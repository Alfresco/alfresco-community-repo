package org.alfresco.opencmis.mapping;

import java.io.Serializable;

import org.alfresco.opencmis.CMISConnector;
import org.alfresco.opencmis.dictionary.CMISNodeInfo;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.chemistry.opencmis.commons.PropertyIds;

/**
 * Accessor for the Target Id (relationship)
 * 
 * @author florian.mueller
 */
public class TargetIdProperty extends AbstractProperty
{
    /**
     * Construct
     * 
     * @param serviceRegistry ServiceRegistry
     * @param connector CMISConnector
     */
    public TargetIdProperty(ServiceRegistry serviceRegistry, CMISConnector connector)
    {
        super(serviceRegistry, connector, PropertyIds.TARGET_ID);
    }

    public Serializable getValueInternal(CMISNodeInfo nodeInfo)
    {
        NodeRef targetNodeRef = nodeInfo.getAssociationRef().getTargetRef();
        return createNodeInfo(targetNodeRef).getObjectId();
    }
}
