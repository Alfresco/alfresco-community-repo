/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.admin.patch.impl;

import java.util.List;

import org.springframework.extensions.surf.util.I18NUtil;
import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.repo.importer.ImporterBootstrap;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.admin.PatchException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.PermissionService;

/**
 * Grant <b>Consumer</b> role to <b>Guest</b> in <b>Category Root</b> folder.
 * <p>
 * This patch expects the folder to be present.
 */
public class CategoryRootPermissionPatch extends AbstractPatch
{
    private static final String MSG_RESULT = "patch.categoryRootPermission.result";
    private static final String ERR_NOT_FOUND = "patch.categoryRootPermission.err.not_found";
    
    private PermissionService permissionService;
    private ImporterBootstrap spacesBootstrap;
    
    
    public void setPermissionService(PermissionService permissionService)
    {
        this.permissionService = permissionService;
    }

    public void setSpacesBootstrap(ImporterBootstrap spacesBootstrap)
    {
        this.spacesBootstrap = spacesBootstrap;
    }
    
    @Override
    protected String applyInternal() throws Exception
    {
        String categoryRootPath = "/cm:categoryRoot";

        // find category root
        NodeRef rootNodeRef = nodeService.getRootNode(spacesBootstrap.getStoreRef());
        List<NodeRef> nodeRefs = searchService.selectNodes(rootNodeRef, categoryRootPath, null, namespaceService, false);
        if (nodeRefs.size() == 0)
        {
            String msg = I18NUtil.getMessage(ERR_NOT_FOUND, categoryRootPath);
            throw new PatchException(msg);
        }
        NodeRef categoryRootRef = nodeRefs.get(0);
        
        // apply permission
        permissionService.setPermission(
                categoryRootRef,
                AuthenticationUtil.getGuestUserName(),
                PermissionService.READ,
                true);

        // done
        String msg = I18NUtil.getMessage(MSG_RESULT, categoryRootPath);
        return msg;
    }
}
