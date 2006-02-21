/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.repo.admin.patch.impl;

import org.alfresco.i18n.I18NUtil;
import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.repo.importer.ImporterBootstrap;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PermissionService;

/**
 * Change Spaces Root Node permission from Guest to Read
 * 
 * Guest (now Consumer) permission is not valid for sys:store_root type.
 */
public class SpacesRootPermissionPatch extends AbstractPatch
{
    private static final String MSG_SUCCESS = "patch.spacesRootPermission.result";

    private ImporterBootstrap spacesBootstrap;
    private NodeService nodeService;
    private PermissionService permissionService;
    
    
    public SpacesRootPermissionPatch()
    {
        super();
    }

    public void setSpacesBootstrap(ImporterBootstrap spacesBootstrap)
    {
        this.spacesBootstrap = spacesBootstrap;
    }

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public void setPermissionService(PermissionService permissionService)
    {
        this.permissionService = permissionService;
    }
    
    
    @Override
    protected String applyInternal() throws Exception
    {
        NodeRef rootNodeRef = nodeService.getRootNode(spacesBootstrap.getStoreRef());
        permissionService.deletePermission(rootNodeRef, PermissionService.ALL_AUTHORITIES, PermissionService.CONSUMER, true);
        permissionService.setPermission(rootNodeRef, PermissionService.ALL_AUTHORITIES, PermissionService.READ, true);

        return I18NUtil.getMessage(MSG_SUCCESS);
    }

}
