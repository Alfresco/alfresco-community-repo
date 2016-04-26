package org.alfresco.opencmis.mapping;

import java.io.Serializable;

import org.alfresco.opencmis.CMISConnector;
import org.alfresco.opencmis.dictionary.CMISNodeInfo;
import org.alfresco.service.ServiceRegistry;
import org.apache.chemistry.opencmis.commons.PropertyIds;

/**
 * Get the CMIS version series checked out property
 * 
 * @author florian.mueller
 */
public class IsVersionSeriesCheckedOutProperty extends AbstractProperty
{
    /**
     * Construct
     */
    public IsVersionSeriesCheckedOutProperty(ServiceRegistry serviceRegistry, CMISConnector connector)
    {
        super(serviceRegistry, connector, PropertyIds.IS_VERSION_SERIES_CHECKED_OUT);
    }

    @Override
    public Serializable getValueInternal(CMISNodeInfo nodeInfo)
    {
        return nodeInfo.hasPWC();
    }
}
