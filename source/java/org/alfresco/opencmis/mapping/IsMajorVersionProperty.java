package org.alfresco.opencmis.mapping;

import java.io.Serializable;

import org.alfresco.opencmis.CMISConnector;
import org.alfresco.opencmis.dictionary.CMISNodeInfo;
import org.alfresco.service.ServiceRegistry;
import org.apache.chemistry.opencmis.commons.PropertyIds;

/**
 * Accessor for CMIS is major version property
 * 
 * @author florian.mueller
 */
public class IsMajorVersionProperty extends AbstractProperty
{
    /**
     * Construct
     * 
     * @param serviceRegistry ServiceRegistry
     * @param connector CMISConnector
     */
    public IsMajorVersionProperty(ServiceRegistry serviceRegistry, CMISConnector connector)
    {
        super(serviceRegistry, connector, PropertyIds.IS_MAJOR_VERSION);
    }

    public Serializable getValueInternal(CMISNodeInfo nodeInfo)
    {
        return nodeInfo.isMajorVersion();
    }
}
