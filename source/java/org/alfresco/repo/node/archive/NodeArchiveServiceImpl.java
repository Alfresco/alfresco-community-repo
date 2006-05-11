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

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.archive.RestoreNodeReport.RestoreStatus;
import org.alfresco.repo.transaction.TransactionUtil;
import org.alfresco.repo.transaction.TransactionUtil.TransactionWork;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.EqualsHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Implementation of the node archive abstraction.
 * 
 * @author Derek Hulley
 */
public class NodeArchiveServiceImpl implements NodeArchiveService
{
    private static Log logger = LogFactory.getLog(NodeArchiveServiceImpl.class);
    
    private NodeService nodeService;
    private SearchService searchService;
    private TransactionService transactionService;

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }

    public void setSearchService(SearchService searchService)
    {
        this.searchService = searchService;
    }

    public NodeRef getStoreArchiveNode(StoreRef originalStoreRef)
    {
        return nodeService.getStoreArchiveNode(originalStoreRef);
    }

    /**
     * Get all the nodes that were archived <b>from</b> the given store.
     */
    private ResultSet getArchivedNodes(StoreRef originalStoreRef)
    {
        // Get the archive location
        NodeRef archiveParentNodeRef = nodeService.getStoreArchiveNode(originalStoreRef);
        StoreRef archiveStoreRef = archiveParentNodeRef.getStoreRef();
        // build the query
        String query = String.format("PARENT:\"%s\" AND ASPECT:\"%s\"", archiveParentNodeRef, ContentModel.ASPECT_ARCHIVED);
        // search parameters
        SearchParameters params = new SearchParameters();
        params.addStore(archiveStoreRef);
        params.setLanguage(SearchService.LANGUAGE_LUCENE);
        params.setQuery(query);
//        params.addSort(ContentModel.PROP_ARCHIVED_DATE.toString(), false);
        // get all archived children using a search
        ResultSet rs = searchService.query(params);
        // done
        return rs;
    }

    /**
     * This is the primary restore method that all <code>restore</code> methods fall back on.
     * It executes the restore for the node in a separate transaction and attempts to catch
     * the known conditions that can be reported back to the client.
     */
    public RestoreNodeReport restoreArchivedNode(
            final NodeRef archivedNodeRef,
            final NodeRef destinationNodeRef,
            final QName assocTypeQName,
            final QName assocQName)
    {
        RestoreNodeReport report = new RestoreNodeReport(archivedNodeRef);
        report.setTargetParentNodeRef(destinationNodeRef);
        try
        {
            // Transactional wrapper to attempt the restore
            TransactionWork<NodeRef> restoreWork = new TransactionWork<NodeRef>()
            {
                public NodeRef doWork() throws Exception
                {
                    return nodeService.restoreNode(archivedNodeRef, destinationNodeRef, assocTypeQName, assocQName);
                }
            };
            NodeRef newNodeRef = TransactionUtil.executeInNonPropagatingUserTransaction(transactionService, restoreWork);
            // success
            report.setRestoredNodeRef(newNodeRef);
            report.setStatus(RestoreStatus.SUCCESS);
        }
        catch (InvalidNodeRefException e)
        {
            report.setCause(e);
            NodeRef invalidNodeRef = e.getNodeRef();
            if (archivedNodeRef.equals(invalidNodeRef))
            {
                // not too serious, but the node to archive is missing
                report.setStatus(RestoreStatus.FAILURE_INVALID_ARCHIVE_NODE);
            }
            else if (EqualsHelper.nullSafeEquals(destinationNodeRef, invalidNodeRef))
            {
                report.setStatus(RestoreStatus.FAILURE_INVALID_PARENT);
            }
            else if (destinationNodeRef == null)
            {
                // get the original parent of the archived node
                ChildAssociationRef originalParentAssocRef = (ChildAssociationRef) nodeService.getProperty(
                        archivedNodeRef,
                        ContentModel.PROP_ARCHIVED_ORIGINAL_PARENT_ASSOC);
                NodeRef originalParentNodeRef = originalParentAssocRef.getParentRef();
                if (EqualsHelper.nullSafeEquals(originalParentNodeRef, invalidNodeRef))
                {
                    report.setStatus(RestoreStatus.FAILURE_INVALID_PARENT);
                }
                else
                {
                    // some other invalid node was detected
                    report.setStatus(RestoreStatus.FAILURE_OTHER);
                }
            }
            else
            {
                // some other invalid node was detected
                report.setStatus(RestoreStatus.FAILURE_OTHER);
            }
        }
        // TODO: Catch permission exceptions
        catch (Throwable e)
        {
            report.setCause(e);
            report.setStatus(RestoreStatus.FAILURE_OTHER);
        }
        // done
        if (logger.isDebugEnabled())
        {
            logger.debug("Attempted node restore: "+ report);
        }
        return report;
    }

    /**
     * @see #restoreArchivedNode(NodeRef, NodeRef, QName, QName)
     */
    public RestoreNodeReport restoreArchivedNode(NodeRef archivedNodeRef)
    {
        return restoreArchivedNode(archivedNodeRef, null, null, null);
    }

    /**
     * @see #restoreArchivedNodes(List, NodeRef, QName, QName)
     */
    public List<RestoreNodeReport> restoreArchivedNodes(List<NodeRef> archivedNodeRefs)
    {
        return restoreArchivedNodes(archivedNodeRefs, null, null, null);
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
     * @see #restoreAllArchivedNodes(StoreRef, NodeRef, QName, QName)
     */
    public List<RestoreNodeReport> restoreAllArchivedNodes(StoreRef originalStoreRef)
    {
        return restoreAllArchivedNodes(originalStoreRef, null, null, null);
    }

    /**
     * Finds the archive location for nodes that were deleted from the given store
     * and attempt to restore each node.
     * 
     * @see NodeService#getStoreArchiveNode(StoreRef)
     * @see #restoreArchivedNode(NodeRef, NodeRef, QName, QName)
     */
    public List<RestoreNodeReport> restoreAllArchivedNodes(
            StoreRef originalStoreRef,
            NodeRef destinationNodeRef,
            QName assocTypeQName,
            QName assocQName)
    {
        // get all archived children using a search
        ResultSet rs = getArchivedNodes(originalStoreRef);
        // loop through the resultset and attempt to restore all the nodes
        List<RestoreNodeReport> results = new ArrayList<RestoreNodeReport>(1000);
        for (ResultSetRow row : rs)
        {
            NodeRef archivedNodeRef = row.getNodeRef();
            RestoreNodeReport result = restoreArchivedNode(archivedNodeRef, destinationNodeRef, assocTypeQName, assocQName);
            results.add(result);
        }
        // done
        if (logger.isDebugEnabled())
        {
            logger.debug("Restored " + results.size() + " nodes into store " + originalStoreRef);
        }
        return results;
    }

    /**
     * This is the primary purge methd that all purge methods fall back on.  It isolates the delete
     * work in a new transaction.
     */
    public void purgeArchivedNode(final NodeRef archivedNodeRef)
    {
        TransactionWork<Object> deleteWork = new TransactionWork<Object>()
        {
            public Object doWork() throws Exception
            {
                try
                {
                    nodeService.deleteNode(archivedNodeRef);
                }
                catch (InvalidNodeRefException e)
                {
                    // ignore
                }
                return null;
            }
        };
        TransactionUtil.executeInNonPropagatingUserTransaction(transactionService, deleteWork);
    }

    /**
     * @see #purgeArchivedNode(NodeRef)
     */
    public void purgeArchivedNodes(List<NodeRef> archivedNodes)
    {
        for (NodeRef archivedNodeRef : archivedNodes)
        {
            purgeArchivedNode(archivedNodeRef);
        }
        // done
    }

    public void purgeAllArchivedNodes(StoreRef originalStoreRef)
    {
        // get all archived children using a search
        ResultSet rs = getArchivedNodes(originalStoreRef);
        // loop through the resultset and attempt to restore all the nodes
        List<RestoreNodeReport> results = new ArrayList<RestoreNodeReport>(1000);
        for (ResultSetRow row : rs)
        {
            NodeRef archivedNodeRef = row.getNodeRef();
            purgeArchivedNode(archivedNodeRef);
        }
        // done
        if (logger.isDebugEnabled())
        {
            logger.debug("Deleted " + results.size() + " nodes originally in store " + originalStoreRef);
        }
    }

}
