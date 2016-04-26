/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.transfer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.alfresco.repo.transfer.CorrespondingNodeResolver.ResolvedParentChildPair;
import org.alfresco.repo.transfer.manifest.TransferManifestDeletedNode;
import org.alfresco.repo.transfer.manifest.TransferManifestHeader;
import org.alfresco.repo.transfer.manifest.TransferManifestNormalNode;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.transfer.TransferReceiver;
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
    CorrespondingNodeResolver nodeResolver;
    
    private static final Log log = LogFactory.getLog(RepoTertiaryManifestProcessorImpl.class);

    /**
     *  Is this a "sync" transfer.  If not then does nothing.
     */
    boolean isSync = false;
    String manifestRepositoryId;

    /**
     * @param receiver TransferReceiver
     * @param transferId String
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
        
        if (log.isDebugEnabled())
        {
            log.debug("Processing node with incoming noderef of " + node.getNodeRef());
        }
        logComment("Tertiary Processing incoming node: " + node.getNodeRef() + " --  Source path = " + node.getParentPath() + "/" + node.getPrimaryParentAssoc().getQName());

        /**
         * This processor only does processes sync requests.
         */
        if(isSync)
        {  
            ChildAssociationRef primaryParentAssoc = node.getPrimaryParentAssoc();
        
            CorrespondingNodeResolver.ResolvedParentChildPair resolvedNodes = nodeResolver.resolveCorrespondingNode(node
                .getNodeRef(), primaryParentAssoc, node.getParentPath());    
        
            NodeRef nodeRef = resolvedNodes.resolvedChild;
                               
            if(nodeService.exists(nodeRef))
            {
                log.debug("destination node exists - check the children");
                
                //TODO Use more efficient query here.
                List<ChildAssociationRef> expectedChildren = node.getChildAssocs();

                if (log.isDebugEnabled())
                {
                    log.debug("Checking node in TERTIARY_MANIFEST_PROCESSOR");
                }

                if ((null != resolvedNodes.resolvedParent) && nodeService.exists(resolvedNodes.resolvedParent))
                {
                    if (log.isTraceEnabled())
                    {
                        logInvasionHierarchy(resolvedNodes.resolvedParent, nodeRef, nodeService, log);
                    }
                }
                else
                {
                    List<ChildAssociationRef> parentAssocs = nodeService.getParentAssocs(nodeRef);

                    if (log.isTraceEnabled())
                    {
                        logInvasionHierarchy(parentAssocs.iterator().next().getParentRef(), nodeRef, nodeService, log);
                    }
                }

                List<NodeRef> expectedChildNodeRefs = new ArrayList<NodeRef>();
                Set<Path> expectedChildNodePaths = new HashSet<Path>();

                if (log.isDebugEnabled())
                {
                    log.debug("Expected children:");
                }

                for(ChildAssociationRef ref : expectedChildren)
                {
                    if(log.isDebugEnabled())
                    {
                        log.debug("Expecting child node " + ref);
                    }

                    NodeRef childRef = null;
                    if (nodeService.exists(ref.getChildRef()))
                    {
                        childRef = ref.getChildRef();

                        if (log.isTraceEnabled())
                        {
                            logInvasionHierarchy(nodeRef, ref.getChildRef(), nodeService, log);
                        }
                    }
                    else
                    {
                        Path parentPath = node.getParentPath();
                        parentPath = parentPath.subPath(0, (parentPath.size() - 1));
                        parentPath.append(new Path.ChildAssocElement(ref));
                        ResolvedParentChildPair resolvedChild = nodeResolver.resolveCorrespondingNode(ref.getChildRef(), ref, parentPath);

                        if (null != resolvedChild.resolvedChild)
                        {
                            childRef = resolvedChild.resolvedChild;

                            if (log.isDebugEnabled())
                            {
                                log.debug("The node has been RESOLVED!");
                            }

                            if (log.isTraceEnabled())
                            {
                                logInvasionHierarchy(resolvedChild.resolvedParent, resolvedChild.resolvedChild, nodeService, log);
                            }
                        }
                        else
                        {
                            if (log.isDebugEnabled())
                            {
                                log.debug("The node DOES NOT exist in current repository! Processing will be made by its PATH!");
                            }

                            expectedChildNodePaths.add(parentPath);
                        }
                    }

                    if (null != childRef)
                    {
                        expectedChildNodeRefs.add(childRef);
                    }
                }
                
                List<ChildAssociationRef> actualChildren = nodeService.getChildAssocs(nodeRef);

                /**
                 * For each actual child association
                 */
                if (log.isDebugEnabled())
                {
                    log.debug("Traversing ACTUAL children:");
                }

                for(ChildAssociationRef child : actualChildren)
                {
                    log.debug("checking child: " + child);
                    if(child.isPrimary())
                    {
                        if (log.isTraceEnabled())
                        {
                            logInvasionHierarchy(child.getParentRef(), child.getChildRef(), nodeService, log);
                        }

                        /**
                         * yes it is a primary assoc
                         * should it be there ?
                         */
                        NodeRef childNodeRef = child.getChildRef();
                        Path actualChildPath = nodeService.getPath(childNodeRef);

                        if(!expectedChildNodeRefs.contains(childNodeRef) && !expectedChildNodePaths.contains(actualChildPath))
                        {
                            if (log.isDebugEnabled())
                            {
                                log.debug("This child IS NOT EXPECTED!");
                            }

                            /**
                             * An unexpected child - if this node has been transferred then
                             * it may need to be deleted.  
                             *  
                             * If from another repository then we have to prune the alien children 
                             * rather than deleting it.
                             */
                            if(nodeService.hasAspect(childNodeRef, TransferModel.ASPECT_TRANSFERRED))
                            {
                                log.debug("an unexpected transferred child node:" + child);
                                logComment("Transfer sync mode - checking unexpected child node:" + child);
                                String fromRepositoryId = (String) nodeService.getProperty(childNodeRef, TransferModel.PROP_FROM_REPOSITORY_ID);
                                
                                // Yes this is a transferred node.  When syncing we only delete nodes that are "from" 
                                // the system that is transferring to this repo.

                                if (log.isDebugEnabled())
                                {
                                    log.debug("'manifestRepositoryId': " + manifestRepositoryId);
                                }

                                if(fromRepositoryId != null &&  manifestRepositoryId != null)
                                {
                                    if(nodeService.hasAspect(childNodeRef, TransferModel.ASPECT_ALIEN))
                                    {
                                         /**
                                         * This node can't be deleted since it contains alien content
                                         * it needs to be "pruned" of the transferring repo's content instead.
                                         */
                                        log.debug("node to be deleted contains alien content so needs to be pruned." + childNodeRef);
                                        logComment("Transfer sync mode - node contains alien content so can't be deleted. " +  childNodeRef);
                                        alienProcessor.pruneNode(childNodeRef, manifestRepositoryId);
                                    }
                                    else
                                    {
                                        // Node
                                        if (log.isDebugEnabled())
                                        {
                                            log.debug("Node not alien. Trying to delete the node...");
                                        }

                                        String initialRepositoryId = (String) nodeService.getProperty(childNodeRef, TransferModel.PROP_FROM_REPOSITORY_ID);

                                        if(manifestRepositoryId.equalsIgnoreCase(initialRepositoryId))
                                        {
                                            if (log.isDebugEnabled())
                                            {
                                                log.debug("Replication is initiated from the same repository from which this node was transferred! Deleting");
                                            }
                                            // Yes the manifest repository Id and the from repository Id match.
                                            // Destination node if from the transferring repo and needs to be deleted. 
                                            logDeleted(node.getNodeRef(), childNodeRef, nodeService.getPath(childNodeRef).toString());
                                            logSummaryDeleted(node.getNodeRef(), childNodeRef, nodeService.getPath(childNodeRef).toString());
                                            nodeService.deleteNode(childNodeRef);
                                        }
                                        else
                                        {
                                            if (log.isDebugEnabled())
                                            {
                                                log
                                                        .debug("It is not an alien, but 'fromRepositoryId' is not equal to the 'manifestRepositoryId'! Cannot delete the foreign node...");
                                            }
                                        }
                                    }
                                }
                                else
                                {
                                    log.debug("node does not have a transferred aspect");
                                }
                            }
                        }
                    }
                }
            }
        
            else
            {
                log.debug("not sync mode - do nothing");
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
    
    public void setAlienProcessor(AlienProcessor alienProcessor)
    {
        this.alienProcessor = alienProcessor;
    }

    public AlienProcessor getAlienProcessor()
    {
        return alienProcessor;
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
