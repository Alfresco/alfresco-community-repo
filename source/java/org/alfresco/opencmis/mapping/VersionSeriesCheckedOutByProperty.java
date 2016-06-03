package org.alfresco.opencmis.mapping;

import java.io.Serializable;

import org.alfresco.model.ContentModel;
import org.alfresco.opencmis.CMISConnector;
import org.alfresco.opencmis.dictionary.CMISNodeInfo;
import org.alfresco.service.ServiceRegistry;
import org.apache.chemistry.opencmis.commons.PropertyIds;

/**
 * Get the CMIS version series checked out by property
 * 
 * @author florian.mueller
 */
public class VersionSeriesCheckedOutByProperty extends AbstractProperty
{
    /**
     * Construct
     * 
     * @param serviceRegistry ServiceRegistry
     * @param connector CMISConnector
     */
    public VersionSeriesCheckedOutByProperty(ServiceRegistry serviceRegistry, CMISConnector connector)
    {
        super(serviceRegistry, connector, PropertyIds.VERSION_SERIES_CHECKED_OUT_BY);
    }

    public Serializable getValueInternal(CMISNodeInfo nodeInfo)
    {
        if (!nodeInfo.hasPWC())
        {
            return null;
        }

        if (nodeInfo.isPWC())
        {
            return nodeInfo.getNodeProps().get(ContentModel.PROP_WORKING_COPY_OWNER);
        } else
        {
            return getServiceRegistry().getNodeService().getProperty(nodeInfo.getCurrentNodeNodeRef(),
                    ContentModel.PROP_LOCK_OWNER);
        }
    }
}
