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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author mrogers
 * 
 * The tertiary manifest processor performs a third parse of the snapshot file.
 * 
 * For a complete transfer it is responsible for deleting any replicated nodes 
 * which exist in the target repository that do not exist in the source repository.
 * 
 * If the transfer is not "sync" then this processor does nothing.
 */
public class RepoTertiaryManifestProcessorImpl extends AbstractManifestProcessorBase
{
    private NodeService nodeService;
    private AlienProcessor alienProcessor;
    
    private static final Log log = LogFactory.getLog(RepoTertiaryManifestProcessorImpl.class);

    /**
     *  Is this a "sync" transfer.  If not then does nothing.
     */
    boolean isSync = false;
    String manifestRepositoryId;

    /**
     * @param receiver 
     * @param transferId
     */
    public RepoTertiaryManifestProcessorImpl(TransferReceiver receiver, String transferId)
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
        NodeRef nodeRef = node.getNodeRef();
        log.debug("processNode : " + nodeRef);
        
        if(isSync)
        {
            
            List<ChildAssociationRef> expectedChildren = node.getChildAssocs();
            
            List<NodeRef> expectedChildNodeRefs = new ArrayList<NodeRef>();
            
            for(ChildAssociationRef ref : expectedChildren)
            {
                if(log.isDebugEnabled())
                {
                    log.debug("expecting child" + ref);
                }
                expectedChildNodeRefs.add(ref.getChildRef());
            }
            

            // TODO Do we need to worry about path based nodes ? Assuming no at the moment.
            if(nodeService.exists(nodeRef))
            {
                log.debug("destination node exists");
                
                /**
                 * yes this node exists in the destination.
                 */
                List<ChildAssociationRef> actualChildren = nodeService.getChildAssocs(nodeRef);

                /**
                 * For each destination child association
                 */
                for(ChildAssociationRef child : actualChildren)
                {
                    log.debug("checking child: " + child);
                    if(child.isPrimary())
                    {
                        /**
                         * yes it is a primary assoc
                         * should it be there ?
                         */
                        NodeRef childNodeRef = child.getChildRef();
                        
                        if(!expectedChildNodeRefs.contains(childNodeRef))
                        {
                            /**
                             * An unexpected child - if this node has been transferred then
                             * it needs to be deleted.  
                             *  
                             * another repository then we have to prune the alien children 
                             * rather than deleting it.
                             */
                            log.debug("an unexpected child node:" + child);
                            if(nodeService.hasAspect(childNodeRef, TransferModel.ASPECT_TRANSFERRED))
                            {
                                String fromRepositoryId = (String)nodeService.getProperty(childNodeRef, TransferModel.PROP_FROM_REPOSITORY_ID);
                                
                                // Yes this is a transferred node.  When syncing we only delete nodes that are "from" 
                                // the system that is transferring to this repo.
                                if(fromRepositoryId != null &&  manifestRepositoryId != null)
                                {
                                    if(manifestRepositoryId.equalsIgnoreCase(fromRepositoryId))
                                    {
                                        // Yes the manifest repository Id and the from repository Id match.
                                        if(nodeService.hasAspect(childNodeRef, TransferModel.ASPECT_ALIEN))
                                        {
                                            /**
                                             * This node can't be deleted since it contains alien content
                                             * it needs to be "pruned" of the transferring repo's content instead.
                                             */
                                            log.debug("node to be deleted contains alien content so needs to be pruned." + childNodeRef);
                                            alienProcessor.pruneNode(childNodeRef, fromRepositoryId);
                                            //pruneNode(childNodeRef, fromRepositoryId);
                                        }
                                        else
                                        {
                                            // Destination node needs to be deleted.                              
                                            nodeService.deleteNode(childNodeRef);
                                            log.debug("deleted node:" + childNodeRef);
                                        }
                                    }
                                }    
                            }
                        }
                    }
                }
            }
        }
    }
    
    protected void processHeader(TransferManifestHeader header)
    {
        isSync = header.isSync();
        log.debug("isSync :" + isSync);
        
        manifestRepositoryId = header.getRepositoryId();
        log.debug("fromRepositoryId:" +  manifestRepositoryId);
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
    
//    /**
//     * Prune out the non aliens from the specified repository
//     * 
//     * Need to walk the tree downwards pruning any aliens for this repository
//     * 
//     * Also any folders remaining need to have their invaded by field rippled upwards since they may no 
//     * longer be invaded by the specified repository if all the alien children have been pruned.  
//     * 
//     * @param nodeRef the node to prune
//     * @param fromRepositoryId the repository id of the nodes to prune.
//     */
//    private void pruneNode(NodeRef parentNodeRef, String fromRepositoryId)
//    { 
//        Stack<NodeRef> nodesToPrune = new Stack<NodeRef>();
//        Stack<NodeRef> foldersToRecalculate = new Stack<NodeRef>(); 
//        nodesToPrune.add(parentNodeRef);
//                
//        while(!nodesToPrune.isEmpty())
//        {
//            /**
//             *  for all alien children
//             * 
//             *  if from the repo with no (other) aliens - delete
//             *  
//             *  if from the repo with multiple alien invasions - leave alone but process children
//             */
//            NodeRef currentNodeRef = nodesToPrune.pop();
//            
//            log.debug("pruneNode:" + currentNodeRef);
//            
//            if(nodeService.hasAspect(currentNodeRef, TransferModel.ASPECT_ALIEN))
//            {
//                // Yes this is an alien node
//                List<String>invadedBy = (List<String>)nodeService.getProperty(currentNodeRef, TransferModel.PROP_INVADED_BY);
//                if(invadedBy.contains(fromRepositoryId))
//                {
//                    if(invadedBy.size() == 1)
//                    {
//                        // we are invaded by a single repository which must be fromRepositoryId
//                        log.debug("pruned - deleted node:" + currentNodeRef);
//                        nodeService.deleteNode(currentNodeRef);
//                    }
//                    else
//                    {
//                        log.debug("folder has multiple invaders");
//                        // multiple invasion - so it must be a folder
//                        //TODO replace with a more efficient query
//                        List<ChildAssociationRef> refs = nodeService.getChildAssocs(parentNodeRef);
//                        for(ChildAssociationRef ref : refs)
//                        {
//                            if(log.isDebugEnabled())
//                            {
//                                log.debug("will need to check child:" + ref);
//                            }
//                            nodesToPrune.push(ref.getChildRef()); 
//                            
//                            /**
//                             * This folder can't be deleted so its invaded flag needs to be re-calculated 
//                             */
//                            if(!foldersToRecalculate.contains(ref.getParentRef()))
//                            {
//                                foldersToRecalculate.push(ref.getParentRef());
//                            }
//                        }
//                    }
//                }
//                else
//                {
//                    /**
//                     * Current node has been invaded by another repository  
//                     *
//                     * Need to check fromRepositoryId since its children may need to be pruned
//                     */
//                    nodeService.hasAspect(currentNodeRef, TransferModel.ASPECT_TRANSFERRED);
//                    {
//                        String fromRepoId = (String)nodeService.getProperty(currentNodeRef, TransferModel.PROP_FROM_REPOSITORY_ID);
//                        if(fromRepositoryId.equalsIgnoreCase(fromRepoId))
//                        {
//                            log.debug("folder is from the transferring repository");
//                            // invaded from somewhere else - so it must be a folder
//                            List<ChildAssociationRef> refs = nodeService.getChildAssocs(currentNodeRef);
//                            for(ChildAssociationRef ref : refs)
//                            {
//                                if(log.isDebugEnabled())
//                                {
//                                    log.debug("will need to check child:" + ref);
//                                }
//                                nodesToPrune.push(ref.getChildRef()); 
//                                
//                                /**
//                                 * This folder can't be deleted so its invaded flag needs to be re-calculated 
//                                 */
//                                if(!foldersToRecalculate.contains(ref.getParentRef()))
//                                {
//                                    foldersToRecalculate.push(ref.getParentRef());
//                                }
//                            }
//                        }
//                    }        
//                }
//            }
//            else
//            {
//                // Current node does not contain alien nodes so it can be deleted.
//                nodeService.hasAspect(currentNodeRef, TransferModel.ASPECT_TRANSFERRED);
//                {
//                    String fromRepoId = (String)nodeService.getProperty(currentNodeRef, TransferModel.PROP_FROM_REPOSITORY_ID);
//                    if(fromRepositoryId.equalsIgnoreCase(fromRepoId))
//                    {
//                        // we are invaded by a single repository
//                        log.debug("pruned - deleted non alien node:" + currentNodeRef);
//                        nodeService.deleteNode(currentNodeRef);
//                    }
//                }
//            }
//        }
//        
//        /**
//         * Now ripple the "invadedBy" flag upwards.
//         */
//        
//        while(!foldersToRecalculate.isEmpty())
//        {
//            NodeRef folderNodeRef = foldersToRecalculate.pop();
//            
//            log.debug("recalculate invadedBy :" + folderNodeRef);
//            
//            List<String>folderInvadedBy = (List<String>)nodeService.getProperty(folderNodeRef, TransferModel.PROP_INVADED_BY);
//            
//            boolean stillInvaded = false;
//            //TODO need a more efficient query here
//            List<ChildAssociationRef> refs = nodeService.getChildAssocs(folderNodeRef);
//            for(ChildAssociationRef ref : refs)
//            {
//                NodeRef childNode = ref.getChildRef();
//                List<String>childInvadedBy = (List<String>)nodeService.getProperty(childNode, TransferModel.PROP_INVADED_BY);
//                
//                if(childInvadedBy.contains(fromRepositoryId))
//                {
//                    log.debug("folder is still invaded");
//                    stillInvaded = true;
//                    break;
//                }
//            }
//            
//            if(!stillInvaded)
//            {
//                List<String> newInvadedBy = new ArrayList<String>(folderInvadedBy);
//                folderInvadedBy.remove(fromRepositoryId);
//                nodeService.setProperty(folderNodeRef, TransferModel.PROP_INVADED_BY, (Serializable)newInvadedBy);
//            }
//        }
//        log.debug("pruneNode: end");
//    }

    public void setAlienProcessor(AlienProcessor alienProcessor)
    {
        this.alienProcessor = alienProcessor;
    }

    public AlienProcessor getAlienProcessor()
    {
        return alienProcessor;
    }
}
