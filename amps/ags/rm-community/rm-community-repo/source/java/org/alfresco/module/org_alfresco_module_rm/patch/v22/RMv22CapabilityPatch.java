/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

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
