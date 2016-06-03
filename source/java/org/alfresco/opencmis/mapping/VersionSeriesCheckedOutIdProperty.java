package org.alfresco.opencmis.mapping;

import java.io.Serializable;

import org.alfresco.opencmis.CMISConnector;
import org.alfresco.opencmis.dictionary.CMISNodeInfo;
import org.alfresco.service.ServiceRegistry;
import org.apache.chemistry.opencmis.commons.PropertyIds;

/**
 * Get the CMIS version series checked out id property
 * 
 * @author florian.mueller
 */
public class VersionSeriesCheckedOutIdProperty extends AbstractProperty
{
    /**
     * Construct
     * 
     * @param serviceRegistry ServiceRegistry
     * @param connector CMISConnector
     */
    public VersionSeriesCheckedOutIdProperty(ServiceRegistry serviceRegistry, CMISConnector connector)
    {
        super(serviceRegistry, connector, PropertyIds.VERSION_SERIES_CHECKED_OUT_ID);
    }

    public Serializable getValueInternal(CMISNodeInfo nodeInfo)
    {
        if (!nodeInfo.hasPWC())
        {
            return null;
        }

        return connector.constructObjectId(nodeInfo.getCurrentNodeId(), CMISConnector.PWC_VERSION_LABEL);
    }
}
