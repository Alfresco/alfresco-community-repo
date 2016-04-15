package org.alfresco.opencmis.mapping;

import java.io.Serializable;

import org.alfresco.opencmis.CMISConnector;
import org.alfresco.opencmis.dictionary.CMISNodeInfo;
import org.alfresco.service.ServiceRegistry;
import org.apache.chemistry.opencmis.commons.PropertyIds;

/**
 * @author florian.mueller
 */
public class VersionSeriesIdProperty extends AbstractProperty
{
    /**
     * Construct
     */
    public VersionSeriesIdProperty(ServiceRegistry serviceRegistry, CMISConnector connector)
    {
        super(serviceRegistry, connector, PropertyIds.VERSION_SERIES_ID);
    }
    
    @Override
    public Serializable getValueInternal(CMISNodeInfo nodeInfo)
    {
    	return connector.constructObjectId(nodeInfo.getCurrentNodeId(), null);
    }
}
