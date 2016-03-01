 
package org.alfresco.module.org_alfresco_module_rm.patch.v23;

import org.alfresco.module.org_alfresco_module_rm.patch.common.CapabilityPatch;
import org.alfresco.module.org_alfresco_module_rm.role.FilePlanRoleService;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * RM v2.3 patch to add new end retention capability.
 *
 * @author Alex Balan
 * @since 2.3
 */
public class RMv23EndRetentionCapabilityPatch extends CapabilityPatch
{
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.patch.common.CapabilityPatch#applyCapabilityPatch(org.alfresco.service.cmr.repository.NodeRef)
     */
    protected void applyCapabilityPatch(NodeRef filePlan) 
    {
        // add new capability
        addCapability(filePlan,
                      "EndRetention",
                      FilePlanRoleService.ROLE_ADMIN,
                      FilePlanRoleService.ROLE_RECORDS_MANAGER);
        
    }
}
