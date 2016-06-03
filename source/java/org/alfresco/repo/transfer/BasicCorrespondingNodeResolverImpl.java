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

import java.util.List;

import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.repository.Path.Element;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author brian
 * 
 */
public class BasicCorrespondingNodeResolverImpl implements CorrespondingNodeResolver
{
    private static final Log log = LogFactory.getLog(BasicCorrespondingNodeResolverImpl.class);
    
    private static final String MSG_SPECIFIED_STORE_DOES_NOT_EXIST = "transfer_service.receiver.specified_store_nonexistent";
    private NodeService nodeService;

    public ResolvedParentChildPair resolveCorrespondingNode(NodeRef sourceNodeRef, ChildAssociationRef primaryAssoc,
            Path parentPath)
    {
        if (log.isDebugEnabled()) {
            log.debug("Attempting to resolve corresponding node for noderef " + sourceNodeRef);
            log.debug("Supplied parent path: " + parentPath);
            log.debug("Supplied parent assoc: " + primaryAssoc.toString());
        }
        ResolvedParentChildPair result = new ResolvedParentChildPair(null, null);

        // Does a node with the same NodeRef already exist in this repo?
        if (nodeService.exists(sourceNodeRef))
        {
            result.resolvedChild = sourceNodeRef;
        }

        // Find where this node should live
        NodeRef parentNodeRef = primaryAssoc.getParentRef();
        if (!nodeService.exists(parentNodeRef.getStoreRef()))
        {
            throw new TransferProcessingException(MSG_SPECIFIED_STORE_DOES_NOT_EXIST);
        }
        if (!nodeService.exists(parentNodeRef))
        {
            if (log.isDebugEnabled()) 
            {
                log.debug("Unable to find node's parent by node ref: " + parentNodeRef);
            }
            parentNodeRef = resolveParentPath(primaryAssoc.getParentRef().getStoreRef(), parentPath);
        }
        else
        {
            if (log.isDebugEnabled())
            {
                log.debug("Node's parent has been resolved by noderef: " + parentNodeRef);
            }
        }
        if (log.isDebugEnabled())
        {
            log.debug("Parent noderef resolved to node: " + parentNodeRef);
        }
        result.resolvedParent = parentNodeRef;
        if ((parentNodeRef != null) && (result.resolvedChild == null))
        {
            //We've managed to find the approprate parent node, but not the child.
            //See if we can find the child by looking at the parent's child associations
            List<ChildAssociationRef> children = nodeService.getChildAssocs(parentNodeRef, RegexQNamePattern.MATCH_ALL,
                    primaryAssoc.getQName());
            if (!children.isEmpty())
            {
                result.resolvedChild = children.get(0).getChildRef();
            }
        }
        if (log.isDebugEnabled())
        {
            log.debug("Resolved child node to: " + result.resolvedChild);
        }
        return result;
    }

    /**
     *
     * @param store StoreRef
     * @param parentPath Path
     * @return NodeRef
     */
    private NodeRef resolveParentPath(StoreRef store, Path parentPath)
    {
        if (log.isDebugEnabled()) 
        {
            log.debug("Trying to resolve parent path " + parentPath);
        }
        NodeRef node = nodeService.getRootNode(store);
        int index = 1;
        while (index < parentPath.size())
        {
            Element element = parentPath.get(index++);
            QName name = QName.createQName(element.getElementString());
            List<ChildAssociationRef> children = nodeService.getChildAssocs(node, RegexQNamePattern.MATCH_ALL, name);

            if (children.isEmpty())
            {
                if (log.isDebugEnabled())
                {
                    log.debug("Failed to resolve path element " + element.getElementString());
                }
                return null;
            }
            if (log.isDebugEnabled()) 
            {
                log.debug("Resolved path element " + element.getElementString());
            }
            node = children.get(0).getChildRef();
        }
        return node;
    }

    /**
     * @param nodeService the nodeService to set
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

}
