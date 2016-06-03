package org.alfresco.opencmis.mapping;

import org.alfresco.opencmis.dictionary.CMISNodeInfo;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.apache.chemistry.opencmis.commons.enums.Action;

/**
 * Alfresco Permission based Action Evaluator
 * 
 * @author davidc
 */
public class PermissionActionEvaluator extends AbstractActionEvaluator
{
    private String[] permissions;
    private PermissionService permissionService;

    /**
     * Construct
     * 
     * @param serviceRegistry ServiceRegistry
     * @param action Action
     * @param permission String...
     */
    protected PermissionActionEvaluator(ServiceRegistry serviceRegistry, Action action, String... permission)
    {
        super(serviceRegistry, action);
        this.permissions = permission;
        this.permissionService = serviceRegistry.getPermissionService();
    }

    public boolean isAllowed(CMISNodeInfo nodeInfo)
    {
        for (String permission : permissions)
        {
            if (permissionService.hasPermission(nodeInfo.getNodeRef(), permission) == AccessStatus.DENIED)
            {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("PermissionActionEvaluator[action=").append(getAction());
        builder.append(", permissions=");
        for (String permission : permissions)
        {
            builder.append(permission).append(",");
        }
        builder.append("]");
        return builder.toString();
    }

}
