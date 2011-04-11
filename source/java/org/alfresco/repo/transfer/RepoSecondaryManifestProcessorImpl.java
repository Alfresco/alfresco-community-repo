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
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.util.EqualsHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author brian
 * 
 * The secondary manifest processor performs a second parse of the snapshot file.
 * 
 * It is responsible for linking nodes together.   
 * 
 * At the point that this processor runs both ends (source and target) of the nodes' associations should be 
 * available in the receiving repository.
 * 
 */
public class RepoSecondaryManifestProcessorImpl extends AbstractManifestProcessorBase
{
    private NodeService nodeService;
    private CorrespondingNodeResolver nodeResolver;
    
    private static final Log log = LogFactory.getLog(RepoSecondaryManifestProcessorImpl.class);

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
        logComment("Seconday Processing incoming node: " + node.getNodeRef());
      
        log.debug("Seconday Processing incoming node: " + node.getNodeRef());
        
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
        List<AssociationRef> requiredTargetAssocs = node.getTargetAssocs();
        List<AssociationRef> currentTargetAssocs = nodeService.getTargetAssocs(correspondingNodeRef, RegexQNamePattern.MATCH_ALL);
        processPeerAssociations(requiredTargetAssocs, currentTargetAssocs, correspondingNodeRef, true);

        //Process "source" peer associations (associations *to* this node)
        List<AssociationRef> requiredSourceAssocs = node.getSourceAssocs();
        List<AssociationRef> currentSourceAssocs = nodeService.getSourceAssocs(correspondingNodeRef, RegexQNamePattern.MATCH_ALL);
        processPeerAssociations(requiredSourceAssocs, currentSourceAssocs, correspondingNodeRef, false);

    }


    /**
     * Process the peer associations
     * 
     * @param requiredAssocs
     * @param currentAssocs
     * @param correspondingNodeRef
     * @param isSource
     */
    private void processPeerAssociations(List<AssociationRef> requiredAssocs,
            List<AssociationRef> currentAssocs, 
            NodeRef nodeRef, 
            boolean isSource)
    {
        if (requiredAssocs == null) 
        {
            requiredAssocs = new ArrayList<AssociationRef>();
        }
        if (currentAssocs == null) 
        {
            currentAssocs = new ArrayList<AssociationRef>();
        }
               
        List<AssociationRefKey> keysRequired = new ArrayList<AssociationRefKey>();
        List<AssociationRefKey> keysCurrent = new ArrayList<AssociationRefKey>();
        
        /**
         *  Which assocs do we need to add ?
         *  
         *  Need to compare on sourceNodeRef, targetNodeRef and qname but ignore, irrelevant id property 
         *  which is why we need to introduce AssociationRefKey
         */
        for(AssociationRef ref : requiredAssocs)
        {
            keysRequired.add(new AssociationRefKey(ref));
        }
       
        for(AssociationRef ref : currentAssocs )
        {
            keysCurrent.add(new AssociationRefKey(ref));
        }
          
        /**
         * Which assocs do we need to add?
         */
        for(AssociationRefKey ref : keysRequired)
        {
            
            if(!keysCurrent.contains(ref))
            {
                //We don't have an existing association with this required node
                NodeRef otherNode = isSource ? ref.targetRef : ref.sourceRef;
                if (nodeService.exists(otherNode)) 
                {
                    //the other node exists in this repo
                    if(log.isDebugEnabled())
                    {
                        log.debug("need to add peer assoc from:" + ref.sourceRef + ", to:" + ref.targetRef +", qname:" + ref.assocTypeQName);
                    }
                    nodeService.createAssociation(ref.sourceRef, ref.targetRef, ref.assocTypeQName);
                }
            }
        }
        
        /** 
         * Which assocs do we need to remove ?
         * 
         * Only remove the assocs for this node. 
         * 
         * TODO ALF-6543 - What if there is a local, non transferred, peer assoc, for example a rendition 
         * or a rule or something? Is there some way to say that that link was not transferred?
         */
//        if(isSource)
//        {
            for(AssociationRefKey ref : keysCurrent)
            {               
                if(!keysRequired.contains(ref))
                {
                    if(log.isDebugEnabled())
                    {
                        log.debug("need to remove peer assoc from:" + ref.sourceRef + ", to:" + ref.targetRef +", qname:" + ref.assocTypeQName);
                    }
                    nodeService.removeAssociation(ref.sourceRef, ref.targetRef, ref.assocTypeQName);
                }
             }
//        }             
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
    
    /**
     * The AssociationRefKey is the key value for peer associations.
     * 
     * In particular is differs from AssociationRef in that it does not have the "id" property.
     */
    private class AssociationRefKey
    {
        NodeRef sourceRef;
        NodeRef targetRef;
        QName assocTypeQName;
        
        /**
         * 
         * @param sourceRef
         * @param targetRef
         * @param assocTypeQName
         */
        public AssociationRefKey(NodeRef sourceRef, NodeRef targetRef, QName assocTypeQName)
        {
            this.sourceRef = sourceRef;
            this.targetRef = targetRef;
            this.assocTypeQName = assocTypeQName;
        }
        
        /**
         * 
         * @param sourceRef
         * @param targetRef
         * @param assocTypeQName
         */
        public AssociationRefKey(AssociationRef ref)
        {
            this.sourceRef = ref.getSourceRef();
            this.targetRef = ref.getTargetRef();
            this.assocTypeQName = ref.getTypeQName();
        }
        
        /**
         * Compares:
         * <ul>
         * <li>{@link #id}</li>
         * <li>{@link #sourceRef}</li>
         * <li>{@link #targetRef}</li>
         * <li>{@link #assocTypeQName}</li>
         * </ul>
         */
        public boolean equals(Object o)
        {
            if (this == o)
            {
                return true;
            }
            if (!(o instanceof AssociationRefKey))
            {
                return false;
            }
            AssociationRefKey other = (AssociationRefKey) o;

            return 
                    EqualsHelper.nullSafeEquals(this.sourceRef, other.sourceRef)
                    && EqualsHelper.nullSafeEquals(this.assocTypeQName, other.assocTypeQName)
                    && EqualsHelper.nullSafeEquals(this.targetRef, other.targetRef);
        }

        public int hashCode()
        {
            int hashCode = targetRef.hashCode();
            hashCode = 37 * hashCode + ((sourceRef == null) ? 0 : sourceRef.hashCode());
            hashCode = 37 * hashCode + ((assocTypeQName == null) ? 0 : assocTypeQName.hashCode());
            return hashCode;
        }
        
    }
}
