/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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
package org.alfresco.repo.surf.policy;

import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies.BeforeDeleteNodePolicy;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.web.scripts.bean.ADMRemoteStore;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Delete Node Policy to remove surf-config files for a deleted user.
 * 
 * @author Dmitry Velichkevich
 * @author Kevin Roast
 */
public class SurfConfigCleaner extends ADMRemoteStore implements BeforeDeleteNodePolicy
{
    private PolicyComponent policyComponent;

    public void init()
    {
        this.policyComponent.bindClassBehaviour(
                BeforeDeleteNodePolicy.QNAME,
                ContentModel.TYPE_PERSON,
                new JavaBehaviour(this, BeforeDeleteNodePolicy.QNAME.getLocalName()));
    }

    @Override
    public void beforeDeleteNode(NodeRef nodeRef)
    {
        final String userName = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_USERNAME);
        final NodeRef componentsRef = getGlobalComponentsNodeRef();
        final NodeRef usersFolderRef = getGlobalUserFolderNodeRef();
        
        // Remove the user Surf config folder, contains dynamic page definitions such as dashboard.xml
        // For example, qname path to user folder:
        //  /app:company_home/st:sites/cm:surf-config/cm:pages/cm:user/cm:admin
        //                                                                ^^^^^ encoded username
        if (usersFolderRef != null)
        {
            NodeRef userFolderNodeRef = nodeService.getChildByName(usersFolderRef, ContentModel.ASSOC_CONTAINS, encodePath(userName));
            if (userFolderNodeRef != null)
            {
                nodeService.deleteNode(userFolderNodeRef);
            }
        }
        
        // Remove each component Surf config file related to the user, such as the dashboard dashlet component references
        // For example, qname path to user component file:
        //  /app:company_home/st:sites/cm:surf-config/cm:components/cm:page.component-1-1.user~admin~dashboard.xml
        //                                                                                     ^^^^^ encoded username
        if (componentsRef != null)
        {
            List<FileInfo> configNodes = getFileNodes(
                    fileFolderService.getFileInfo(componentsRef),
                    buildUserConfigSearchPattern(userName),
                    true).getPage();
            
            for (FileInfo fileInfo : configNodes)
            {
                nodeService.deleteNode(fileInfo.getNodeRef());
            }
        }
    }

    public void setPolicyComponent(PolicyComponent policyComponent)
    {
        this.policyComponent = policyComponent;
    }
}
