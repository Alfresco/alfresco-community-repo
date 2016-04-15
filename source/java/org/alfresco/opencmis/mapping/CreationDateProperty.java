package org.alfresco.opencmis.mapping;

import java.io.Serializable;

import org.alfresco.opencmis.CMISConnector;
import org.alfresco.opencmis.dictionary.CMISNodeInfo;
import org.alfresco.service.ServiceRegistry;
import org.apache.chemistry.opencmis.commons.PropertyIds;

/**
 * Get the CMIS object creation date property.
 * 
 * @author florian.mueller
 */
public class CreationDateProperty extends AbstractProperty
{
    /**
     * Construct
     * 
     * @param serviceRegistry ServiceRegistry
     * @param connector CMISConnector
     */
    public CreationDateProperty(ServiceRegistry serviceRegistry, CMISConnector connector)
    {
        super(serviceRegistry, connector, PropertyIds.CREATION_DATE);
    }

    public Serializable getValueInternal(CMISNodeInfo nodeInfo)
    {
        return nodeInfo.getCreationDate();
    }
}
