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

package org.alfresco.module.org_alfresco_module_rm.security;

import java.util.Set;

import org.alfresco.module.org_alfresco_module_rm.capability.Capability;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.model.security.ModelSecurityService;
import org.alfresco.module.org_alfresco_module_rm.role.FilePlanRoleService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * Records management permission service implementation
 *
 * @author Roy Wetherall
 */
@SuppressWarnings("deprecation")
public class RecordsManagementSecurityServiceImpl implements RecordsManagementSecurityService,
                                                             RecordsManagementModel
{
    /** Model security service */
    private ModelSecurityService modelSecurityService;
    
    /** File plan role service */
    private FilePlanRoleService filePlanRoleService;
    
    /** File plan permission service */
    private FilePlanPermissionService filePlanPermissionService;

    /**
     * @param modelSecurityService  model security service
     */
    public void setModelSecurityService(ModelSecurityService modelSecurityService)
    {
        this.modelSecurityService = modelSecurityService;
    }
    
    /**
     * @param filePlanRoleService   file plan role service
     */
    public void setFilePlanRoleService(FilePlanRoleService filePlanRoleService)
    {
        this.filePlanRoleService = filePlanRoleService;
    }

    /**
     * @param filePlanPermissionService file plan permission service
     */
    public void setFilePlanPermissionService(FilePlanPermissionService filePlanPermissionService)
    {
        this.filePlanPermissionService = filePlanPermissionService;
    }
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.security.RecordsManagementSecurityService#getProtectedAspects()
     */
    @Deprecated
    @Override
    public Set<QName> getProtectedAspects()
    {
        return modelSecurityService.getProtectedAspects();
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.security.RecordsManagementSecurityService#getProtectedProperties()
     */
    @Deprecated
    @Override
    public Set<QName> getProtectedProperties()
    {
        return modelSecurityService.getProtectedProperties();
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.security.RecordsManagementSecurityService#assignRoleToAuthority(org.alfresco.service.cmr.repository.NodeRef, java.lang.String, java.lang.String)
     */
    @Deprecated
    @Override
    public void assignRoleToAuthority(NodeRef rmRootNode, String role, String authorityName)
    {
        filePlanRoleService.assignRoleToAuthority(rmRootNode, role, authorityName);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.security.RecordsManagementSecurityService#bootstrapDefaultRoles(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Deprecated
    @Override
    public void bootstrapDefaultRoles(NodeRef rmRootNode)
    {
        throw new UnsupportedOperationException("This method is no longer supported.");
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.security.RecordsManagementSecurityService#createRole(org.alfresco.service.cmr.repository.NodeRef, java.lang.String, java.lang.String, java.util.Set)
     */
    @Deprecated
    @Override
    public Role createRole(NodeRef rmRootNode, String role, String roleDisplayLabel, Set<Capability> capabilities)
    {
        return Role.toRole(filePlanRoleService.createRole(rmRootNode, role, roleDisplayLabel, capabilities));
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.security.RecordsManagementSecurityService#deleteRole(org.alfresco.service.cmr.repository.NodeRef, java.lang.String)
     */
    @Deprecated
    @Override
    public void deleteRole(NodeRef rmRootNode, String role)
    {
        filePlanRoleService.deleteRole(rmRootNode, role);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.security.RecordsManagementSecurityService#existsRole(org.alfresco.service.cmr.repository.NodeRef, java.lang.String)
     */
    @Deprecated
    @Override
    public boolean existsRole(NodeRef rmRootNode, String role)
    {
        return filePlanRoleService.existsRole(rmRootNode, role);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.security.RecordsManagementSecurityService#getAllRolesContainerGroup(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Deprecated
    @Override
    public String getAllRolesContainerGroup(NodeRef filePlan)
    {
        return filePlanRoleService.getAllRolesContainerGroup(filePlan);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.security.RecordsManagementSecurityService#getRole(org.alfresco.service.cmr.repository.NodeRef, java.lang.String)
     */
    @Deprecated
    @Override
    public Role getRole(NodeRef rmRootNode, String role)
    {
        return Role.toRole(filePlanRoleService.getRole(rmRootNode, role));
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.security.RecordsManagementSecurityService#getRoles(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Deprecated
    @Override
    public Set<Role> getRoles(NodeRef rmRootNode)
    {
        return Role.toRoleSet(filePlanRoleService.getRoles(rmRootNode));
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.security.RecordsManagementSecurityService#getRolesByUser(org.alfresco.service.cmr.repository.NodeRef, java.lang.String)
     */
    @Deprecated
    @Override
    public Set<Role> getRolesByUser(NodeRef rmRootNode, String user)
    {
        return Role.toRoleSet(filePlanRoleService.getRolesByUser(rmRootNode, user));
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.security.RecordsManagementSecurityService#hasRMAdminRole(org.alfresco.service.cmr.repository.NodeRef, java.lang.String)
     */
    @Deprecated
    @Override
    public boolean hasRMAdminRole(NodeRef rmRootNode, String user)
    {
        return filePlanRoleService.hasRMAdminRole(rmRootNode, user);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.security.RecordsManagementSecurityService#updateRole(org.alfresco.service.cmr.repository.NodeRef, java.lang.String, java.lang.String, java.util.Set)
     */
    @Deprecated
    @Override
    public Role updateRole(NodeRef rmRootNode, String role, String roleDisplayLabel, Set<Capability> capabilities)
    {
        return Role.toRole(filePlanRoleService.updateRole(rmRootNode, role, roleDisplayLabel, capabilities));
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.security.RecordsManagementSecurityService#deletePermission(org.alfresco.service.cmr.repository.NodeRef, java.lang.String, java.lang.String)
     */
    @Deprecated
    @Override
    public void deletePermission(NodeRef nodeRef, String authority, String permission)
    {
        filePlanPermissionService.deletePermission(nodeRef, authority, permission);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.security.RecordsManagementSecurityService#setPermission(org.alfresco.service.cmr.repository.NodeRef, java.lang.String, java.lang.String)
     */
    @Deprecated
    @Override
    public void setPermission(NodeRef nodeRef, String authority, String permission)
    {
        filePlanPermissionService.setPermission(nodeRef, authority, permission);
    }
}
