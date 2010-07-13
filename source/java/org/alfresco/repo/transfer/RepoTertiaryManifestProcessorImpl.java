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
 * If the transfer is not "complete" then this processor does nothing.
 * 
 */
public class RepoTertiaryManifestProcessorImpl extends AbstractManifestProcessorBase
{
    private NodeService nodeService;
    private CorrespondingNodeResolver nodeResolver;
    
    private static final Log log = LogFactory.getLog(RepoTertiaryManifestProcessorImpl.class);

    /**
     *  Is this a "sync" transfer.  If not then does nothing.
     */
    boolean isSync = false;

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
        log.debug("processNode " + nodeRef);
        
        if(isSync)
        {
            
            List<ChildAssociationRef> expectedChildren = node.getChildAssocs();
            
            List<NodeRef> expectedChildNodeRefs = new ArrayList<NodeRef>();
            
            for(ChildAssociationRef ref : expectedChildren)
            {
                log.debug("expecting child" + ref);
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
                             * it needs to be deleted.  If it is a local node then we don't 
                             * touch it. 
                             */
                            log.debug("an unexpected child node:" + child);
                            if(nodeService.hasAspect(childNodeRef, TransferModel.ASPECT_TRANSFERRED))
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
    
    protected void processHeader(TransferManifestHeader header)
    {
        isSync = header.isSync();
        log.debug("isSync :" + isSync);
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
