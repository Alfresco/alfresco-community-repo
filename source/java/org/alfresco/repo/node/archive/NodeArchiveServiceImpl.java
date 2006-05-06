/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.repo.node.archive;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;

/**
 * Implementation of the node archive abstraction.
 * 
 * @author Derek Hulley
 */
public class NodeArchiveServiceImpl implements NodeArchiveService
{
    private NodeService nodeService;
    private TransactionService transactionService;

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }

    public NodeRef getStoreArchiveNode(StoreRef storeRef)
    {
        return nodeService.getStoreArchiveNode(storeRef);
    }

    /**
     * @see #restoreArchivedNode(NodeRef, NodeRef, QName, QName)
     */
    public RestoreNodeReport restoreArchivedNode(NodeRef archivedNodeRef)
    {
        return restoreArchivedNode(archivedNodeRef, null, null, null);
    }

    public RestoreNodeReport restoreArchivedNode(
            NodeRef archivedNodeRef,
            NodeRef destinationNodeRef,
            QName assocTypeQName,
            QName assocQName)
    {
        throw new UnsupportedOperationException();
    }

    /**
     * @see #restoreArchivedNode(NodeRef, NodeRef, QName, QName)
     */
    public List<RestoreNodeReport> restoreArchivedNodes(List<NodeRef> archivedNodeRefs)
    {
        List<RestoreNodeReport> results = new ArrayList<RestoreNodeReport>(archivedNodeRefs.size());
        for (NodeRef nodeRef : archivedNodeRefs)
        {
            RestoreNodeReport result = restoreArchivedNode(nodeRef, null, null, null);
            results.add(result);
        }
        return results;
    }

    /**
     * @see #restoreArchivedNode(NodeRef, NodeRef, QName, QName)
     */
    public List<RestoreNodeReport> restoreArchivedNodes(
            List<NodeRef> archivedNodeRefs,
            NodeRef destinationNodeRef,
            QName assocTypeQName,
            QName assocQName)
    {
        List<RestoreNodeReport> results = new ArrayList<RestoreNodeReport>(archivedNodeRefs.size());
        for (NodeRef nodeRef : archivedNodeRefs)
        {
            RestoreNodeReport result = restoreArchivedNode(nodeRef, destinationNodeRef, assocTypeQName, assocQName);
            results.add(result);
        }
        return results;
    }

    /**
     * @see #restoreArchivedNode(NodeRef, NodeRef, QName, QName)
     */
    public List<RestoreNodeReport> restoreAllArchivedNodes(StoreRef originalStoreRef)
    {
        throw new UnsupportedOperationException();
    }

    public List<RestoreNodeReport> restoreAllArchivedNodes(
            StoreRef originalStoreRef,
            NodeRef destinationNodeRef,
            QName assocTypeQName,
            QName assocQName)
    {
        throw new UnsupportedOperationException();
    }

    public void purgeArchivedNode(NodeRef archivedNodeRef)
    {
        throw new UnsupportedOperationException();
    }

    public void purgeArchivedNodes(List<NodeRef> archivedNodes)
    {
        throw new UnsupportedOperationException();
    }

    public void purgeAllArchivedNodes(StoreRef originalStoreRef)
    {
        throw new UnsupportedOperationException();
    }

}
