package org.alfresco.opencmis.mapping;

import org.alfresco.opencmis.dictionary.CMISNodeInfo;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.security.PermissionService;
import org.apache.chemistry.opencmis.commons.enums.Action;

/**
 * Alfresco Permission based Action Evaluator
 * 
 * @author davidc
 */
public class CanCheckInActionEvaluator extends AbstractActionEvaluator
{
    private PermissionActionEvaluator permissionEvaluator;

    /**
     * Construct
     */
    protected CanCheckInActionEvaluator(ServiceRegistry serviceRegistry)
    {
        super(serviceRegistry, Action.CAN_CHECK_IN);
        permissionEvaluator = new PermissionActionEvaluator(
                serviceRegistry,
                Action.CAN_CHECK_IN,
                PermissionService.CHECK_IN);
    }

    public boolean isAllowed(CMISNodeInfo nodeInfo)
    {
        if (nodeInfo.isPWC())
        {
            return permissionEvaluator.isAllowed(nodeInfo);
        }

        return false;
    }
}
