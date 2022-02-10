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

package org.alfresco.module.org_alfresco_module_rm.patch.v21;

import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService;
import org.alfresco.module.org_alfresco_module_rm.role.FilePlanRoleService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PermissionService;

/**
 * RM v2.1 patch to updated modified capabilities.
 *
 * @author Roy Wetherall
 * @since 2.1
 */
@SuppressWarnings("deprecation")
public class RMv21CapabilityPatch extends RMv21PatchComponent
{
    /** File plan service */
    private FilePlanService filePlanService;
    
    /** authority service */
    private AuthorityService authorityService;
    
    /** permission service */
    private PermissionService permissionService;

    /**
     * @param filePlanService   file plan service
     */
    public void setFilePlanService(FilePlanService filePlanService)
    {
        this.filePlanService = filePlanService;
    }

    /**
     * Helper method to get the file plans
     *
     * @return Set of file plan node references
     */
    protected Set<NodeRef> getFilePlans()
    {
        return filePlanService.getFilePlans();
    }   
    
    /**
     * @param authorityService authority service
     */
    public void setAuthorityService(AuthorityService authorityService)
    {
        this.authorityService = authorityService;
    }
    
    /**
     * @param permissionService permission service
     */
    public void setPermissionService(PermissionService permissionService)
    {
        this.permissionService = permissionService;
    }

    /**
     * Adds a new capability to the specified roles.
     *
     * @param filePlan          file plan
     * @param capabilityName    capability name
     * @param roles             roles
     */
    private void addCapability(NodeRef filePlan, String capabilityName, String ... roles)
    {
        for (String role : roles)
        {
            String fullRoleName = role + filePlan.getId();
            String roleAuthority = authorityService.getName(AuthorityType.GROUP, fullRoleName);
            if (roleAuthority == null)
            {
                throw new AlfrescoRuntimeException("Role " + role + " does not exist.");
            }
            else
            {
                permissionService.setPermission(filePlan, roleAuthority, capabilityName, true);
            }
        }
    }

    /**
     * @see org.alfresco.repo.module.AbstractModuleComponent#executeInternal()
     */
    @Override
    protected void executePatch()
    {
        Set<NodeRef> filePlans = getFilePlans();

        if (LOGGER.isDebugEnabled())
        {
            LOGGER.debug("  ... updating " + filePlans.size() + " file plans");
        }

        for (NodeRef filePlan : filePlans)
        {
            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug("  ... updating file plan " + filePlan.toString());
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
