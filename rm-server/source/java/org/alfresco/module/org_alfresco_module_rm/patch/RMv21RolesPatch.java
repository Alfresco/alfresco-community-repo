/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService;
import org.alfresco.module.org_alfresco_module_rm.role.FilePlanRoleService;
import org.alfresco.module.org_alfresco_module_rm.role.Role;
import org.alfresco.repo.security.authority.RMAuthority;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthorityService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanNameAware;

/**
 * Adds the existing rm roles to a new zone "APP.RM"
 *
 * @author Tuna Aksoy
 * @since 2.1
 */
public class RMv21RolesPatch extends ModulePatchComponent implements BeanNameAware
{
    /** logger */
    private static Log logger = LogFactory.getLog(RMv21RolesPatch.class); 
    
    private FilePlanService filePlanService;
    private FilePlanRoleService filePlanRoleService;
    private AuthorityService authorityService;

    public void setFilePlanService(FilePlanService filePlanService)
    {
        this.filePlanService = filePlanService;
    }

    public void setFilePlanRoleService(FilePlanRoleService filePlanRoleService)
    {
        this.filePlanRoleService = filePlanRoleService;
    }

    public void setAuthorityService(AuthorityService authorityService)
    {
        this.authorityService = authorityService;
    }

    @Override
    protected void executePatch() throws Throwable
    {
        if (logger.isDebugEnabled() == true)
        {
            logger.debug("RM module: RMv21RolesPatch executing ...");
        }
        
        Set<NodeRef> filePlans = filePlanService.getFilePlans();
        
        if (logger.isDebugEnabled() == true)
        {
            logger.debug(" ... updating " + filePlans.size() + " file plans");
        }
        
        for (NodeRef filePlan : filePlans)
        {
            boolean parentAddedToZone = false;
            Set<Role> roles = filePlanRoleService.getRoles(filePlan);
            for (Role role : roles)
            {
                String roleGroupName = role.getRoleGroupName();                
                if (authorityService.getAuthorityZones(roleGroupName).contains(RMAuthority.ZONE_APP_RM) == false)
                {
                    if (logger.isDebugEnabled() == true)
                    {
                        logger.debug(" ... updating " + roleGroupName + " in file plan " + filePlan.toString());
                    }
                    
                    addAuthorityToZone(roleGroupName);
                    if (parentAddedToZone == false)
                    {
                        String allRolesGroupName = filePlanRoleService.getAllRolesContainerGroup(filePlan);
                        addAuthorityToZone(allRolesGroupName);
                        parentAddedToZone = true;
                    }
                }
            }
        }
        
        if (logger.isDebugEnabled() == true)
        {
            logger.debug(" ... complete");
        }
    }

    private void addAuthorityToZone(String roleGroupName)
    {
        authorityService.addAuthorityToZones(roleGroupName, new HashSet<String>(Arrays.asList(RMAuthority.ZONE_APP_RM)));
    }
}
