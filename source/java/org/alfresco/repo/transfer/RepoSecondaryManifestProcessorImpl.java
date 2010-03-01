/*
 * Copyright (C) 2009-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */

package org.alfresco.repo.transfer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.transfer.manifest.TransferManifestDeletedNode;
import org.alfresco.repo.transfer.manifest.TransferManifestHeader;
import org.alfresco.repo.transfer.manifest.TransferManifestNodeHelper;
import org.alfresco.repo.transfer.manifest.TransferManifestNormalNode;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.transfer.TransferReceiver;
import org.alfresco.service.namespace.RegexQNamePattern;

/**
 * @author brian
 * 
 */
public class RepoSecondaryManifestProcessorImpl extends AbstractManifestProcessorBase
{
    private NodeService nodeService;
    private CorrespondingNodeResolver nodeResolver;

    /**
     * @param receiver 
     * @param transferId
     */
    public RepoSecondaryManifestProcessorImpl(TransferReceiver receiver, String transferId)
    {
        super(receiver, transferId);
    }

    protected void endManifest()
    {
        //NOOP
    }
    
    protected void processNode(TransferManifestDeletedNode node)
    {
        //NOOP
    }

    protected void processNode(TransferManifestNormalNode node)
    {
        NodeRef correspondingNodeRef = nodeResolver.resolveCorrespondingNode(node.getNodeRef(),
                TransferManifestNodeHelper.getPrimaryParentAssoc(node), node.getParentPath()).resolvedChild;

        if (correspondingNodeRef == null)
        {
            correspondingNodeRef = node.getNodeRef();
        }


        //Process parent assocs...
        List<ChildAssociationRef> requiredAssocs = node.getParentAssocs();
        List<ChildAssociationRef> currentAssocs = nodeService.getParentAssocs(correspondingNodeRef);
        processParentChildAssociations(requiredAssocs, currentAssocs, correspondingNodeRef, false);

        //Process child assocs...
        requiredAssocs = node.getChildAssocs();
        currentAssocs = nodeService.getChildAssocs(correspondingNodeRef);
        processParentChildAssociations(requiredAssocs, currentAssocs, correspondingNodeRef, true);
        

        //Process "target" peer associations (associations *from* this node)
        List<AssociationRef> requiredPeerAssocs = node.getTargetAssocs();
        List<AssociationRef> currentPeerAssocs = nodeService.getTargetAssocs(correspondingNodeRef, RegexQNamePattern.MATCH_ALL);
        processPeerAssociations(requiredPeerAssocs, currentPeerAssocs, correspondingNodeRef, true);

        //Process "source" peer associations (associations *to* this node)
        requiredPeerAssocs = node.getSourceAssocs();
        currentPeerAssocs = nodeService.getSourceAssocs(correspondingNodeRef, RegexQNamePattern.MATCH_ALL);
        processPeerAssociations(requiredPeerAssocs, currentPeerAssocs, correspondingNodeRef, true);

    }


    /**
     * @param requiredAssocs
     * @param currentAssocs
     * @param correspondingNodeRef
     * @param isSource
     */
    private void processPeerAssociations(List<AssociationRef> requiredAssocs,
            List<AssociationRef> currentAssocs, NodeRef nodeRef, boolean isSource)
    {
        if (requiredAssocs == null) {
            requiredAssocs = new ArrayList<AssociationRef>();
        }
        if (currentAssocs == null) {
            currentAssocs = new ArrayList<AssociationRef>();
        }
        
        List<AssociationRef> assocsToAdd = new ArrayList<AssociationRef>();
        List<AssociationRef> assocsToRemove = new ArrayList<AssociationRef>();
        
        Map<NodeRef, AssociationRef> currentAssocMap = new HashMap<NodeRef, AssociationRef>();
        
        for (AssociationRef currentAssoc : currentAssocs) {
            NodeRef otherNode = isSource ? currentAssoc.getTargetRef() : currentAssoc.getSourceRef();
            currentAssocMap.put(otherNode, currentAssoc);
        }
        
        for (AssociationRef requiredAssoc : requiredAssocs)
        {
            NodeRef otherNode = isSource ? requiredAssoc.getTargetRef() : requiredAssoc.getSourceRef();
            AssociationRef existingAssociation = currentAssocMap.remove(otherNode);
            if (existingAssociation != null) {
                //We already have an association with the required node. 
                //Check whether it is correct
                if (!existingAssociation.getTypeQName().equals(requiredAssoc.getTypeQName())) {
                    //No, the existing one doesn't match the required one
                    assocsToRemove.add(existingAssociation);
                    assocsToAdd.add(requiredAssoc);
                }
            } else {
                //We don't have an existing association with this required node
                //Check that the required node exists in this repo, and record it for adding
                //if it does
                if (nodeService.exists(otherNode)) {
                    assocsToAdd.add(requiredAssoc);
                }
            }
        }
        //Once we get here, any entries remaining in currentParentMap are associations that need to be deleted.
        assocsToRemove.addAll(currentAssocMap.values());
        //Deal with associations to be removed
        for (AssociationRef assocToRemove : assocsToRemove) {
            nodeService.removeAssociation(assocToRemove.getSourceRef(), assocToRemove.getTargetRef(), assocToRemove.getTypeQName());
        }
        //Deal with associations to be added
        for (AssociationRef assocToAdd : assocsToAdd) {
            NodeRef source = isSource ? nodeRef : assocToAdd.getSourceRef();
            NodeRef target = isSource ? assocToAdd.getTargetRef() : nodeRef;
            nodeService.createAssociation(source, target, assocToAdd.getTypeQName());
        }
    }
    

    private void processParentChildAssociations(List<ChildAssociationRef> requiredAssocs, 
            List<ChildAssociationRef> currentAssocs, NodeRef nodeRef, boolean isParent) {
        
        if (requiredAssocs == null) {
            requiredAssocs = new ArrayList<ChildAssociationRef>();
        }
        if (currentAssocs == null) {
            currentAssocs = new ArrayList<ChildAssociationRef>();
        }
        
        List<ChildAssociationRef> assocsToAdd = new ArrayList<ChildAssociationRef>();
        List<ChildAssociationRef> assocsToRemove = new ArrayList<ChildAssociationRef>();
        
        Map<NodeRef, ChildAssociationRef> currentAssocMap = new HashMap<NodeRef, ChildAssociationRef>();
        
        for (ChildAssociationRef currentAssoc : currentAssocs) {
            if (!currentAssoc.isPrimary()) {
                NodeRef key = isParent ? currentAssoc.getChildRef() : currentAssoc.getParentRef();
                currentAssocMap.put(key, currentAssoc);
            }
        }
        
        for (ChildAssociationRef requiredAssoc : requiredAssocs)
        {
            // We skip the primary parent, since this has already been handled
            if (!requiredAssoc.isPrimary())
            {
                NodeRef otherNode = isParent ? requiredAssoc.getChildRef() : requiredAssoc.getParentRef();
                ChildAssociationRef existingAssociation = currentAssocMap.remove(otherNode);
                if (existingAssociation != null) {
                    //We already have an association with the required parent. 
                    //Check whether it is correct
                    if (!existingAssociation.getQName().equals(requiredAssoc.getQName()) ||
                            !existingAssociation.getTypeQName().equals(requiredAssoc.getTypeQName())) {
                        //No, the existing one doesn't match the required one
                        assocsToRemove.add(existingAssociation);
                        assocsToAdd.add(requiredAssoc);
                    }
                } else {
                    //We don't have an existing association with this required parent
                    //Check that the requiredParent exists in this repo, and record it for adding
                    //if it does
                    if (nodeService.exists(otherNode)) {
                        assocsToAdd.add(requiredAssoc);
                    }
                }
            }
        }
        //Once we get here, any entries remaining in currentParentMap are associations that need to be deleted.
        assocsToRemove.addAll(currentAssocMap.values());
        //Deal with associations to be removed
        for (ChildAssociationRef assocToRemove : assocsToRemove) {
            nodeService.removeChildAssociation(assocToRemove);
        }
        //Deal with associations to be added
        for (ChildAssociationRef assocToAdd : assocsToAdd) {
            NodeRef parent = isParent ? nodeRef : assocToAdd.getParentRef();
            NodeRef child = isParent ? assocToAdd.getChildRef() : nodeRef;
            nodeService.addChild(parent, child, 
                    assocToAdd.getTypeQName(), assocToAdd.getQName());
        }
    }
    
    
    protected void processHeader(TransferManifestHeader header)
    {
        //NOOP
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.transfer.manifest.TransferManifestProcessor#startTransferManifest()
     */
    protected void startManifest()
    {
        //NOOP
    }

    /**
     * @param nodeService
     *            the nodeService to set
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * @param nodeResolver
     *            the nodeResolver to set
     */
    public void setNodeResolver(CorrespondingNodeResolver nodeResolver)
    {
        this.nodeResolver = nodeResolver;
    }
}
