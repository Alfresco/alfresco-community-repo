/*
 * Copyright (C) 2009-2010 Alfresco Software Limited.
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
package org.alfresco.repo.transfer.manifest;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.transfer.TransferException;
import org.alfresco.service.namespace.RegexQNamePattern;

/**
 * Factory to build TransferManifestNodes given their repository NodeRef.
 * Extracts values from the nodeService and instantiates TransferManifestNode.
 *
 * @author Mark Rogers
 */
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
