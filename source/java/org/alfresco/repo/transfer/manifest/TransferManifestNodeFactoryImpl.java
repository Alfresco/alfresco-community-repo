/*
 * Copyright (C) 2009-2010 Alfresco Software Limited.
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
 * FLOSS exception.  You should have received a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.transfer.manifest;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.transfer.TransferException;
import org.alfresco.service.namespace.RegexQNamePattern;

public class TransferManifestNodeFactoryImpl implements TransferManifestNodeFactory
{
    private NodeService nodeService;
    
    public void init()
    {
        
    }
    
    public TransferManifestNode createTransferManifestNode(NodeRef nodeRef)
    {
        NodeRef.Status status = nodeService.getNodeStatus(nodeRef);   
        
        if(status == null)
        {
            throw new TransferException("Unable to get node status for node : " + nodeRef);
        }

        /**
         * Work out whether this is a deleted node or not
         */
        if(nodeRef.getStoreRef().equals(StoreRef.STORE_REF_ARCHIVE_SPACESSTORE) || status.isDeleted())
        {
            if(nodeService.hasAspect(nodeRef, ContentModel.ASPECT_ARCHIVED))
            {
                // Yes we have an archived aspect
                ChildAssociationRef car = (ChildAssociationRef)nodeService.getProperty(nodeRef, 
                      ContentModel.PROP_ARCHIVED_ORIGINAL_PARENT_ASSOC);
                
                TransferManifestDeletedNode node = new TransferManifestDeletedNode();
                NodeRef parentNodeRef = car.getParentRef();
                node.setNodeRef(car.getChildRef());
                node.setPrimaryParentAssoc(car);
                
                if(nodeService.exists(parentNodeRef))
                {
                    // The parent node still exists so it still has a path.
                    Path parentPath = nodeService.getPath(parentNodeRef);
                    node.setParentPath(parentPath);
                }
                
                return node;
            }
            
            // No we don't have an archived aspect - maybe we are not yet committed
            TransferManifestDeletedNode node = new TransferManifestDeletedNode();
            node.setNodeRef(nodeRef);
            ChildAssociationRef parentAssocRef = nodeService.getPrimaryParent(nodeRef);
            if(parentAssocRef != null && parentAssocRef.getParentRef() != null)
            {   
                NodeRef parentNodeRef = parentAssocRef.getParentRef();
                node.setPrimaryParentAssoc(parentAssocRef);
                Path parentPath = nodeService.getPath(parentNodeRef);
                node.setParentPath(parentPath);
            }
            
            return node;
        }
        else
        {
            // This is a "normal" node
        
            TransferManifestNormalNode node = new TransferManifestNormalNode();
            node.setNodeRef(nodeRef);
            node.setProperties(nodeService.getProperties(nodeRef));
            node.setAspects(nodeService.getAspects(nodeRef));
            node.setType(nodeService.getType(nodeRef));   
            ChildAssociationRef parentAssocRef = nodeService.getPrimaryParent(nodeRef);
            if(parentAssocRef != null && parentAssocRef.getParentRef() != null)
            {   
                NodeRef parentNodeRef = parentAssocRef.getParentRef();
                node.setPrimaryParentAssoc(parentAssocRef);
                Path parentPath = nodeService.getPath(parentNodeRef);
                node.setParentPath(parentPath);
            }
            node.setChildAssocs(nodeService.getChildAssocs(nodeRef));
            node.setParentAssocs(nodeService.getParentAssocs(nodeRef));
            node.setTargetAssocs(nodeService.getTargetAssocs(nodeRef, RegexQNamePattern.MATCH_ALL ));
            node.setSourceAssocs(nodeService.getSourceAssocs(nodeRef, RegexQNamePattern.MATCH_ALL));
        
            return node;
        }
    }


    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public NodeService getNodeService()
    {
        return nodeService;
    }
}
