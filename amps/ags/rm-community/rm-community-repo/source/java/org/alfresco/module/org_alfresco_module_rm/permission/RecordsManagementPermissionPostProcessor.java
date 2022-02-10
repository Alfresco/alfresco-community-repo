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

package org.alfresco.module.org_alfresco_module_rm.permission;

import java.util.List;

import org.alfresco.module.org_alfresco_module_rm.capability.RMPermissionModel;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.repo.security.permissions.PermissionReference;
import org.alfresco.repo.security.permissions.impl.model.PermissionModel;
import org.alfresco.repo.security.permissions.processor.impl.PermissionPostProcessorBaseImpl;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;

/**
 * Records management permission post processor.
 *
 * @author Roy Wetherall
 * @since 2.4.a
 */
public class RecordsManagementPermissionPostProcessor extends PermissionPostProcessorBaseImpl
{
    /** node service */
    private NodeService nodeService;
    public void setNodeService(NodeService nodeService) {this.nodeService=nodeService;}

    /** permission service */
    private PermissionService permissionService;
    public void setPermissionService(PermissionService permissionService) {this.permissionService=permissionService;}

    /** The permission model DAO. */
    private PermissionModel permissionModel;
    public void setPermissionModel(PermissionModel permissionModel) {this.permissionModel=permissionModel;}

    /**
     * @see org.alfresco.repo.security.permissions.processor.PermissionPostProcessor#process(AccessStatus, NodeRef, String, List, List)
     */
    @Override
    public AccessStatus process(AccessStatus accessStatus, NodeRef nodeRef, String perm,
                                List<String> configuredReadPermissions, List<String> configuredFilePermissions)
    {
        AccessStatus result = accessStatus;
        if (AccessStatus.DENIED.equals(accessStatus) &&
            nodeService.hasAspect(nodeRef, RecordsManagementModel.ASPECT_FILE_PLAN_COMPONENT))
        {
            // if read denied on rm artifact
            if (PermissionService.READ.equals(perm) || isPermissionContained(perm, configuredReadPermissions))
            {
                // check for read record
                result = permissionService.hasPermission(nodeRef, RMPermissionModel.READ_RECORDS);
            }
            // if write denied on rm artifact
            else if (PermissionService.WRITE.equals(perm) || isPermissionContained(perm, configuredFilePermissions))
            {
                // check for file record
                result = permissionService.hasPermission(nodeRef, RMPermissionModel.FILE_RECORDS);
            }
        }

        return result;

    }

    /**
     * Check if a given permission is implied by a list of permission groups.
     *
     * @param perm The name of the permission in question.
     * @param configuredPermissions The list of permission group names.
     * @return true if the permission is contained or implied by the list of permissions.
     */
    private boolean isPermissionContained(String perm, List<String> configuredPermissions)
    {
        // Check if the permission is explicitly in the list
        if (configuredPermissions.contains(perm))
        {
            return true;
        }
        // Check if the permission is implied by one from the list.
        for (String configuredPermission : configuredPermissions)
        {
            // TODO: Here we are assuming the permission name is unique across all contexts (but I think we're doing that in the properties file anyway).
            PermissionReference permissionReference = permissionModel.getPermissionReference(null, configuredPermission);
            for (PermissionReference granteePermission : permissionModel.getGranteePermissions(permissionReference))
            {
                if (granteePermission.getName().equals(perm))
                {
                    return true;
                }
            }
        }
        return false;
    }

}
