 
package org.alfresco.module.org_alfresco_module_rm.patch.v22;

import org.alfresco.module.org_alfresco_module_rm.patch.common.CapabilityPatch;
import org.alfresco.module.org_alfresco_module_rm.role.FilePlanRoleService;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * RM v2.2 patch to updated modified capabilities.
 *
 * @author Tuna Aksoy
 * @since 2.2
 */
public class RMv22CapabilityPatch extends CapabilityPatch
{
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.patch.common.CapabilityPatch#applyCapabilityPatch(org.alfresco.service.cmr.repository.NodeRef)
     */
    protected void applyCapabilityPatch(NodeRef filePlan) 
    {
        // add new capbilities
        addCapability(filePlan,
                      "FileDestructionReport",
                      FilePlanRoleService.ROLE_ADMIN,
                      FilePlanRoleService.ROLE_RECORDS_MANAGER);
        addCapability(filePlan,
                      "CreateHold",
                      FilePlanRoleService.ROLE_ADMIN,
                      FilePlanRoleService.ROLE_RECORDS_MANAGER);
        addCapability(filePlan,
                      "AddToHold",
                      FilePlanRoleService.ROLE_ADMIN,
                      FilePlanRoleService.ROLE_RECORDS_MANAGER);
        addCapability(filePlan,
                      "RemoveFromHold",
                      FilePlanRoleService.ROLE_ADMIN,
                      FilePlanRoleService.ROLE_RECORDS_MANAGER);
        
        // @see https://issues.alfresco.com/jira/browse/RM-2058
        addCapability(filePlan, 
        		      "ManageAccessControls", 
        		      FilePlanRoleService.ROLE_SECURITY_OFFICER,
                      FilePlanRoleService.ROLE_RECORDS_MANAGER);        
    }
}
