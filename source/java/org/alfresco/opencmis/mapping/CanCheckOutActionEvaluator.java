package org.alfresco.opencmis.mapping;

import org.alfresco.opencmis.dictionary.CMISNodeInfo;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.lock.LockType;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.PermissionService;
import org.apache.chemistry.opencmis.commons.enums.Action;

/**
 * Alfresco Permission based Action Evaluator
 * 
 * @author florian.mueller
 */
public class CanCheckOutActionEvaluator extends AbstractActionEvaluator
{
    private PermissionActionEvaluator permissionEvaluator;
    private LockService lockService;

    /**
     * Construct
     */
    protected CanCheckOutActionEvaluator(ServiceRegistry serviceRegistry)
    {
        super(serviceRegistry, Action.CAN_CHECK_OUT);
        permissionEvaluator = new PermissionActionEvaluator(
                serviceRegistry,
                Action.CAN_CHECK_OUT,
                PermissionService.CHECK_OUT);
        lockService = serviceRegistry.getLockService();
    }

    /**
     * Node must be versionable, must not have a Private Working Copy and must not be locked.
     */
    public boolean isAllowed(CMISNodeInfo nodeInfo)
    {
        NodeRef nodeRef = nodeInfo.getNodeRef();
        if (nodeInfo.hasPWC() || lockService.getLockType(nodeRef) == LockType.READ_ONLY_LOCK)
        {
            return false;
        }

        return permissionEvaluator.isAllowed(nodeInfo);
    }
}
