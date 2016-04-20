package org.alfresco.opencmis.mapping;

import java.io.Serializable;

import org.alfresco.opencmis.CMISConnector;
import org.alfresco.opencmis.dictionary.CMISNodeInfo;
import org.alfresco.service.ServiceRegistry;
import org.apache.chemistry.opencmis.commons.PropertyIds;

/**
 * Accessor for the Source Id (relationship)
 * 
 * @author florian.mueller
 */
public class SourceIdProperty extends AbstractProperty
{
    /**
     * Construct
     * 
     * @param serviceRegistry ServiceRegistry
     * @param connector CMISConnector
     */
    public SourceIdProperty(ServiceRegistry serviceRegistry, CMISConnector connector)
    {
        super(serviceRegistry, connector, PropertyIds.SOURCE_ID);
    }

    public Serializable getValueInternal(CMISNodeInfo nodeInfo)
    {
        return createNodeInfo(nodeInfo.getAssociationRef().getSourceRef()).getObjectId();
    }
}
