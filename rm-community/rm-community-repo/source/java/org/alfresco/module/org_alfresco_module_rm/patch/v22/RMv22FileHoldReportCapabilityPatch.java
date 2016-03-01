 
package org.alfresco.module.org_alfresco_module_rm.patch.v22;

import org.alfresco.module.org_alfresco_module_rm.patch.common.CapabilityPatch;
import org.alfresco.module.org_alfresco_module_rm.role.FilePlanRoleService;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * RM v2.2 patch to add FileHoldReport capability.
 *
 * @author Roy Wetherall
 * @since 2.2
 */
public class RMv22FileHoldReportCapabilityPatch extends CapabilityPatch
{
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.patch.common.CapabilityPatch#applyCapabilityPatch(org.alfresco.service.cmr.repository.NodeRef)
     */
    protected void applyCapabilityPatch(NodeRef filePlan) 
    {
        // add new capability
        addCapability(filePlan,
                      "FileHoldReport",
                      FilePlanRoleService.ROLE_ADMIN,
                      FilePlanRoleService.ROLE_RECORDS_MANAGER);
    }
}
