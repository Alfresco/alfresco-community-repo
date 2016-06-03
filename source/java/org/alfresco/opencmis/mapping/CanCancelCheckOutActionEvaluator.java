package org.alfresco.opencmis.mapping;

import org.alfresco.opencmis.dictionary.CMISNodeInfo;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.security.PermissionService;
import org.apache.chemistry.opencmis.commons.enums.Action;

/**
 * Alfresco Permission based Action Evaluator
 * 
 * @author florian.mueller
 */
public class CanCancelCheckOutActionEvaluator extends AbstractActionEvaluator
{
    private PermissionActionEvaluator permissionEvaluator;

    /**
     * Construct
     */
    protected CanCancelCheckOutActionEvaluator(ServiceRegistry serviceRegistry)
    {
        super(serviceRegistry, Action.CAN_CANCEL_CHECK_OUT);
        permissionEvaluator = new PermissionActionEvaluator(
                serviceRegistry,
                Action.CAN_CANCEL_CHECK_OUT,
                PermissionService.CANCEL_CHECK_OUT);
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
