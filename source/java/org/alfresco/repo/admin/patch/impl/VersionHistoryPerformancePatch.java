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
import org.alfresco.repo.version.VersionModel;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.namespace.QName;

public class VersionHistoryPerformancePatch  extends AbstractPatch
{
    private static final String MSG_SUCCESS = "patch.versionHistoryPerformance.result";
    
    private VersionService versionService;
    
    public void setVersionService(VersionService versionService)
    {
        this.versionService = versionService;
    }

    @Override
    protected String applyInternal() throws Exception
    {
        // Set the aspect on the root node of the version store
        StoreRef versionStoreRef = this.versionService.getVersionStoreReference();
        NodeRef rootNodeRef = this.nodeService.getRootNode(versionStoreRef);
        this.nodeService.addAspect(rootNodeRef, VersionModel.ASPECT_VERSION_STORE_ROOT, null);
        
        List<ChildAssociationRef> assocs = this.nodeService.getChildAssocs(rootNodeRef);
        int updateCount = 0;

        for(ChildAssociationRef childAssocRef : assocs)
        {
            NodeRef nodeRef = childAssocRef.getChildRef();
            if (VersionModel.TYPE_QNAME_VERSION_HISTORY.equals(this.nodeService.getType(nodeRef)) == true)
            {            
                // Get the id
                String versionedNodeId = (String)this.nodeService.getProperty(nodeRef, VersionModel.PROP_QNAME_VERSIONED_NODE_ID);
                
                if (versionedNodeId != null)
                {
                	// Set the cm:name
                	this.nodeService.setProperty(nodeRef, ContentModel.PROP_NAME, versionedNodeId);
                
                	// Move the node
                	this.nodeService.moveNode(  nodeRef, 
                								rootNodeRef, 
                								VersionModel.CHILD_QNAME_VERSION_HISTORIES, 
                								QName.createQName(VersionModel.NAMESPACE_URI, versionedNodeId));
                }
                
                updateCount++;
            }
        }
        
        // Build the result message
        return I18NUtil.getMessage(MSG_SUCCESS, updateCount);
    }

}
