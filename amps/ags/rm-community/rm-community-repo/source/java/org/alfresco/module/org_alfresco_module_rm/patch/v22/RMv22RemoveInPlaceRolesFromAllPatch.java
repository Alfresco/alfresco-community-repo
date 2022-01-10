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

import java.util.Set;

import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService;
import org.alfresco.module.org_alfresco_module_rm.patch.AbstractModulePatch;
import org.alfresco.module.org_alfresco_module_rm.role.FilePlanRoleService;
import org.alfresco.module.org_alfresco_module_rm.role.Role;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthorityService;

/**
 * Removes the in-place groups from the all roles group.
 *
 * @author Roy Wetherall
 * @since 2.2
 */
public class RMv22RemoveInPlaceRolesFromAllPatch extends AbstractModulePatch
{
    /** file plan service */
    private FilePlanService filePlanService;
    
    /** file plan role service */
    private FilePlanRoleService filePlanRoleService;
    
    /** authority service */
    private AuthorityService authorityService;
    
    /**
     * @param filePlanService   file plan service
     */
    public void setFilePlanService(FilePlanService filePlanService)
    {
        this.filePlanService = filePlanService;
    }
    
    /**
     * @param filePlanRoleService   file plan role service
     */
    public void setFilePlanRoleService(FilePlanRoleService filePlanRoleService)
    {
        this.filePlanRoleService = filePlanRoleService;
    }
    
    /**
     * @param authorityService  authority service
     */
    public void setAuthorityService(AuthorityService authorityService)
    {
        this.authorityService = authorityService;
    }
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.patch.AbstractModulePatch#applyInternal()
     */
    @Override
    public void applyInternal()
    {
        // get all file plans
        Set<NodeRef> filePlans = filePlanService.getFilePlans();        
        for (NodeRef filePlan : filePlans)
        {
            Role extendedReaders = filePlanRoleService.getRole(filePlan, FilePlanRoleService.ROLE_EXTENDED_READERS);
            Role extendedWriters = filePlanRoleService.getRole(filePlan, FilePlanRoleService.ROLE_EXTENDED_WRITERS);
            
            // remove extended readers and writers roles from the all roles group
            String allRolesGroup = filePlanRoleService.getAllRolesContainerGroup(filePlan);              
            Set<String> members = authorityService.getContainedAuthorities(null, allRolesGroup, true);
            if (members.contains(extendedReaders.getRoleGroupName()))
            {
                authorityService.removeAuthority(allRolesGroup, extendedReaders.getRoleGroupName());
            }
            if (members.contains(extendedWriters.getRoleGroupName()))
            {
                authorityService.removeAuthority(allRolesGroup, extendedWriters.getRoleGroupName());
            }
        }
    }
}
