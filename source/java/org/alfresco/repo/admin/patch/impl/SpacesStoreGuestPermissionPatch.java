/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.repo.admin.patch.impl;

import java.util.List;

import org.springframework.extensions.surf.util.I18NUtil;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.repo.importer.ImporterBootstrap;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.admin.PatchException;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Grant <b>Read</b> permission to <b>Guest</b> in <b>SpacesStore</b> root node.
 * Fix for bug ETWOONE-163.
 * <p>
 * [KR] Now correctly applies modified permissions to immediate child nodes of the
 * root node:
 * <p>
 * sys:system - Changed inherit=false, Added GROUP_EVERYONE=READ (to disallow guest)
 * cm:categoryRoot - Removed guest=READ (as already inherits)
 * 
 * @author Arseny Kovalchuk
 * @author kevinr
 */
public class SpacesStoreGuestPermissionPatch extends AbstractPatch
{
    private static Log logger = LogFactory.getLog(SpacesStoreGuestPermissionPatch.class);
    
    private static final String MSG_RESULT = "patch.spacesStoreGuestPermission.result";
    
    private PermissionService permissionService;
    private ImporterBootstrap importerBootstrap;
    
    
    public void setPermissionService(PermissionService permissionService)
    {
        this.permissionService = permissionService;
    }
    
    public void setImporterBootstrap(ImporterBootstrap importerBootstrap)
    {
        this.importerBootstrap = importerBootstrap;
    }

    
    @Override
    protected String applyInternal() throws Exception
    {
        StoreRef store = importerBootstrap.getStoreRef();
        if (store == null)
        {
            throw new PatchException("Bootstrap store has not been set");
        }
        
        NodeRef rootRef = nodeService.getRootNode(store);
        if (logger.isDebugEnabled())
        {
            logger.debug("Store Ref:" + store + " NodeRef: " + rootRef);
        }
        permissionService.setPermission(
                rootRef, AuthenticationUtil.getGuestUserName(), PermissionService.READ, true);
        
        String sysQName = importerBootstrap.getConfiguration().getProperty("system.system_container.childname");
        String catQName = "cm:categoryRoot";
        List<ChildAssociationRef> refs = nodeService.getChildAssocs(
                rootRef, ContentModel.ASSOC_CHILDREN, RegexQNamePattern.MATCH_ALL);
        for (ChildAssociationRef ref : refs)
        {
            if (ref.getQName().equals(QName.createQName(sysQName, namespaceService)))
            {
                // found sys:system node
                permissionService.setInheritParentPermissions(ref.getChildRef(), false);
                permissionService.setPermission(
                        ref.getChildRef(), PermissionService.ALL_AUTHORITIES, PermissionService.READ, true);
            }
            else if (ref.getQName().equals(QName.createQName(catQName, namespaceService)))
            {
                // found cm:categoryRoot node
                permissionService.clearPermission(ref.getChildRef(), AuthenticationUtil.getGuestUserName());
            }
        }
        
        return I18NUtil.getMessage(MSG_RESULT);
    }
}