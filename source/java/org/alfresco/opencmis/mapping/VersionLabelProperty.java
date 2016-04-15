package org.alfresco.opencmis.mapping;

import java.io.Serializable;

import org.alfresco.opencmis.CMISConnector;
import org.alfresco.opencmis.dictionary.CMISNodeInfo;
import org.alfresco.service.ServiceRegistry;
import org.apache.chemistry.opencmis.commons.PropertyIds;

/**
 * @author florian.mueller
 */
public class VersionLabelProperty extends AbstractProperty
{
    /**
     * Construct
     * 
     * @param serviceRegistry ServiceRegistry
     * @param connector CMISConnector
     */
    public VersionLabelProperty(ServiceRegistry serviceRegistry, CMISConnector connector)
    {
        super(serviceRegistry, connector, PropertyIds.VERSION_LABEL);
    }

    public Serializable getValueInternal(CMISNodeInfo nodeInfo)
    {
        return nodeInfo.getVersionLabel();
    }
}
