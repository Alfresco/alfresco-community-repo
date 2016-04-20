package org.alfresco.opencmis.mapping;

import java.io.Serializable;

import org.alfresco.opencmis.CMISConnector;
import org.alfresco.opencmis.dictionary.CMISNodeInfo;
import org.alfresco.service.ServiceRegistry;
import org.apache.chemistry.opencmis.commons.PropertyIds;

/**
 * Accesser for CMIS is latest version property
 * 
 * @author florian.mueller
 */
public class IsLatestVersionProperty extends AbstractProperty
{
    /**
     * Construct
     */
    public IsLatestVersionProperty(ServiceRegistry serviceRegistry, CMISConnector connector)
    {
        super(serviceRegistry, connector, PropertyIds.IS_LATEST_VERSION);
    }

    @Override
    public Serializable getValueInternal(CMISNodeInfo nodeInfo)
    {
        return nodeInfo.isLatestVersion();
    }
}
