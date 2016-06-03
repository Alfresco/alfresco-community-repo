package org.alfresco.opencmis.mapping;

import java.io.Serializable;

import org.alfresco.opencmis.CMISConnector;
import org.alfresco.opencmis.dictionary.CMISNodeInfo;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.lock.LockType;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.chemistry.opencmis.commons.PropertyIds;

/**
 * Property accessor for CMIS is immutable property
 * 
 * @author florian.mueller
 */
public class IsImmutableProperty extends AbstractProperty
{
    private LockService lockService;

    /**
     * Construct
     */
    public IsImmutableProperty(ServiceRegistry serviceRegistry, CMISConnector connector)
    {
        super(serviceRegistry, connector, PropertyIds.IS_IMMUTABLE);
        lockService = serviceRegistry.getLockService();
    }

    @Override
    public Serializable getValueInternal(CMISNodeInfo nodeInfo)
    {
        NodeRef nodeRef = nodeInfo.getNodeRef();
        if (nodeInfo.isVersion() || (nodeInfo.hasPWC() && !nodeInfo.isPWC()) || (lockService.getLockType(nodeRef) == LockType.READ_ONLY_LOCK))
        {
            return true;
        }

        return false;
    }
}
