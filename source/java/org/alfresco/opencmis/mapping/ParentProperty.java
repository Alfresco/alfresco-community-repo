package org.alfresco.opencmis.mapping;

import java.io.Serializable;
import java.util.List;

import org.alfresco.opencmis.CMISConnector;
import org.alfresco.opencmis.dictionary.CMISNodeInfo;
import org.alfresco.service.ServiceRegistry;
import org.apache.chemistry.opencmis.commons.PropertyIds;

/**
 * Get the CMIS parent property
 * 
 * @author florian.mueller
 * 
 */
public class ParentProperty extends AbstractProperty
{
    /**
     * Construct
     * 
     * @param serviceRegistry ServiceRegistry
     * @param connector CMISConnector
     */
    public ParentProperty(ServiceRegistry serviceRegistry, CMISConnector connector)
    {
        super(serviceRegistry, connector, PropertyIds.PARENT_ID);
    }

    public Serializable getValueInternal(CMISNodeInfo nodeInfo)
    {
        if (nodeInfo.isRootFolder())
        {
            return null;
        }

        List<CMISNodeInfo> parents = nodeInfo.getParents();
        if (!parents.isEmpty())
        {
            return parents.get(0).getObjectId();
        }

        return null;
    }
}
