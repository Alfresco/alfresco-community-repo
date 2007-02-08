/*
 * Copyright (C) 2005 Alfresco, Inc.
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.alfresco.repo.admin.patch.impl;

import java.util.List;

import org.alfresco.i18n.I18NUtil;
import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.repo.importer.ImporterBootstrap;
import org.alfresco.service.cmr.admin.PatchException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.NamespaceService;

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
    private SearchService searchService;
    private NamespaceService namespaceService;
    private NodeService nodeService;
    
    
    public void setPermissionService(PermissionService permissionService)
    {
        this.permissionService = permissionService;
    }

    public void setSpacesBootstrap(ImporterBootstrap spacesBootstrap)
    {
        this.spacesBootstrap = spacesBootstrap;
    }
    
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
    
    public void setSearchService(SearchService searchService)
    {
        this.searchService = searchService;
    }
    
    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
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
        permissionService.setPermission(categoryRootRef, PermissionService.GUEST_AUTHORITY, PermissionService.READ, true);

        // done
        String msg = I18NUtil.getMessage(MSG_RESULT, categoryRootPath);
        return msg;
    }
}
