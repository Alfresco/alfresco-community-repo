/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.module.org_alfresco_module_rm.patch;

import java.util.Set;

import org.alfresco.module.org_alfresco_module_rm.role.FilePlanRoleService;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * RM v2.1 patch to updated modified capabilities.
 *
 * @author Roy Wetherall
 * @since 2.1
 */
public class RMv21CapabilityPatch extends BaseRMCapabilityPatch
{
    /**
     * @see org.alfresco.repo.module.AbstractModuleComponent#executeInternal()
     */
    @Override
    protected void executePatch() throws Throwable
    {
        Set<NodeRef> filePlans = getFilePlans();

        if (logger.isDebugEnabled() == true)
        {
            logger.debug("  ... updating " + filePlans.size() + " file plans");
        }

        for (NodeRef filePlan : filePlans)
        {
            if (logger.isDebugEnabled() == true)
            {
                logger.debug("  ... updating file plan " + filePlan.toString());
            }

            // add new capabilities
            addCapability(filePlan,
                          "CreateRecords",
                          FilePlanRoleService.ROLE_ADMIN,
                          FilePlanRoleService.ROLE_POWER_USER,
                          FilePlanRoleService.ROLE_RECORDS_MANAGER,
                          FilePlanRoleService.ROLE_SECURITY_OFFICER);
            addCapability(filePlan,
                          "ManageRules",
                          FilePlanRoleService.ROLE_ADMIN);
            addCapability(filePlan,
                          "RequestRecordInformation",
                          FilePlanRoleService.ROLE_ADMIN,
                          FilePlanRoleService.ROLE_POWER_USER,
                          FilePlanRoleService.ROLE_RECORDS_MANAGER,
                          FilePlanRoleService.ROLE_SECURITY_OFFICER);
            addCapability(filePlan,
                          "FileDestructionReport",
                          FilePlanRoleService.ROLE_ADMIN,
                          FilePlanRoleService.ROLE_RECORDS_MANAGER);
            addCapability(filePlan,
                          "RejectRecords",
                          FilePlanRoleService.ROLE_ADMIN,
                          FilePlanRoleService.ROLE_POWER_USER,
                          FilePlanRoleService.ROLE_RECORDS_MANAGER,
                          FilePlanRoleService.ROLE_SECURITY_OFFICER);
            addCapability(filePlan,
                          "FileUnfiledRecords",
                          FilePlanRoleService.ROLE_ADMIN,
                          FilePlanRoleService.ROLE_POWER_USER,
                          FilePlanRoleService.ROLE_RECORDS_MANAGER,
                          FilePlanRoleService.ROLE_SECURITY_OFFICER);
            addCapability(filePlan,
                          "LinkToRecords",
                          FilePlanRoleService.ROLE_ADMIN,
                          FilePlanRoleService.ROLE_POWER_USER,
                          FilePlanRoleService.ROLE_RECORDS_MANAGER,
                          FilePlanRoleService.ROLE_SECURITY_OFFICER);
        }
    }
}
