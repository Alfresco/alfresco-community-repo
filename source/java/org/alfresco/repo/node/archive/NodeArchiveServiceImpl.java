/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.repo.node.archive;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.batch.BatchProcessWorkProvider;
import org.alfresco.repo.batch.BatchProcessor;
import org.alfresco.repo.batch.BatchProcessor.BatchProcessWorker;
import org.alfresco.repo.lock.JobLockService;
import org.alfresco.repo.lock.LockAcquisitionException;
import org.alfresco.repo.node.archive.RestoreNodeReport.RestoreStatus;
import org.alfresco.repo.search.results.ChildAssocRefResultSet;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.EqualsHelper;
import org.alfresco.util.VmShutdownListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Implementation of the node archive abstraction.
 * 
 * @author Derek Hulley
 */
public class NodeArchiveServiceImpl implements NodeArchiveService
{
    private static final QName LOCK_QNAME = QName.createQName(NamespaceService.ALFRESCO_URI, "NodeArchive");
    private static final long LOCK_TTL = 60000;
    
    private static final String MSG_BUSY = "node.archive.msg.busy";
    
    private static Log logger = LogFactory.getLog(NodeArchiveServiceImpl.class);
    
    private NodeService nodeService;
    private TransactionService transactionService;
    private JobLockService jobLockService;

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
        logger.warn("Property 'searchService' has been deprecated as of 3.4.0b");
    }

    public NodeRef getStoreArchiveNode(StoreRef originalStoreRef)
    {
        return nodeService.getStoreArchiveNode(originalStoreRef);
    }

    public void setJobLockService(JobLockService jobLockService)
    {
        this.jobLockService = jobLockService;
    }

    public NodeRef getArchivedNode(NodeRef originalNodeRef)
    {
        StoreRef orginalStoreRef = originalNodeRef.getStoreRef();
        NodeRef archiveRootNodeRef = nodeService.getStoreArchiveNode(orginalStoreRef);
        // create the likely location of the archived node
        NodeRef archivedNodeRef = new NodeRef(
                archiveRootNodeRef.getStoreRef(),
                originalNodeRef.getId());
        return archivedNodeRef;
    }
    
    /**
     * Get all the nodes that were archived <b>from</b> the given store.
     * 
     * @param originalStoreRef      the original store to process
     * @param skipCount             the number of results to skip (used for paging)
     * @param limit                 the number of items to retrieve or -1 to get the all    
     * 
     * @deprecated          To be replaced with a limiting search against the database
     */
    private ResultSet getArchivedNodes(StoreRef originalStoreRef, int skipCount, int limit)
    {
        NodeRef archiveParentNodeRef = nodeService.getStoreArchiveNode(originalStoreRef);
        List<ChildAssociationRef> archivedAssocs = nodeService.getChildAssocs(archiveParentNodeRef);
        ResultSet rs = new ChildAssocRefResultSet(nodeService, archivedAssocs);
        // Done
        return rs;
    }
    
    /**
     * @return                      Returns a work provider for batch processing
     * 
     * @since 3.3.4
     */
    private BatchProcessWorkProvider<NodeRef> getArchivedNodesWorkProvider(final StoreRef originalStoreRef, final String lockToken)
    {
        return new BatchProcessWorkProvider<NodeRef>()
        {
            private VmShutdownListener vmShutdownLister = new VmShutdownListener("getArchivedNodesWorkProvider");
            public int getTotalEstimatedWorkSize()
            {
                return 0;
            }
            public synchronized Collection<NodeRef> getNextWork()
            {
                if (vmShutdownLister.isVmShuttingDown())
                {
                    return Collections.emptyList();
                }
                // Make sure we still have the lock
                try
                {
                    // TODO: Replace with joblock callback mechanism that provides shutdown hints
                    jobLockService.refreshLock(lockToken, LOCK_QNAME, LOCK_TTL);
                }
                catch (LockAcquisitionException e)
                {
                    // This is OK.  We don't have the lock so just quit
                    return Collections.emptyList();
                }
                
                Collection<NodeRef> results = new ArrayList<NodeRef>(100);
                ResultSet rs = null;
                try
                {
                    // The results may be limited by permissions, but 0 results really means 0 results
                    rs = getArchivedNodes(originalStoreRef, 0, 100);
                    for (ResultSetRow row : rs)
                    {
                        results.add(row.getNodeRef());
                    }
                }
                finally
                {
                    if (rs != null) { rs.close(); }
                }
                return results;
            }
        };
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
            RetryingTransactionHelper txnHelper = transactionService.getRetryingTransactionHelper();
            RetryingTransactionCallback<NodeRef> restoreCallback = new RetryingTransactionCallback<NodeRef>()
            {
                public NodeRef execute() throws Exception
                {
                    return nodeService.restoreNode(archivedNodeRef, destinationNodeRef, assocTypeQName, assocQName);
                }
            };
            NodeRef newNodeRef = txnHelper.doInTransaction(restoreCallback, false, true);
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
        catch (AccessDeniedException e)
        {
            report.setCause(e);
            report.setStatus(RestoreStatus.FAILURE_PERMISSION);
        }
        catch (Throwable e)
        {
            report.setCause(e);
            report.setStatus(RestoreStatus.FAILURE_OTHER);
            logger.error("An unhandled exception stopped the restore", e);
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
     * Uses batch processing and job locking to purge all archived nodes
     */
    public List<RestoreNodeReport> restoreAllArchivedNodes(StoreRef originalStoreRef)
    {
        final String user = AuthenticationUtil.getFullyAuthenticatedUser();
        if (user == null)
        {
            throw new IllegalStateException("Cannot restore as there is no authenticated user.");
        }
        
        final List<RestoreNodeReport> results = Collections.synchronizedList(new ArrayList<RestoreNodeReport>(1000));
        /**
         * Worker that purges each node
         */
        BatchProcessWorker<NodeRef> worker = new BatchProcessor.BatchProcessWorkerAdaptor<NodeRef>()
        {
            public void process(NodeRef entry) throws Throwable
            {
                AuthenticationUtil.pushAuthentication();
                try
                {
                    AuthenticationUtil.setFullyAuthenticatedUser(user);
                    if (nodeService.exists(entry))
                    {
                        RestoreNodeReport report = restoreArchivedNode(entry);
                        // Append the results (it is synchronized)
                        results.add(report);
                    }
                }
                finally
                {
                    AuthenticationUtil.popAuthentication();
                }
            }
        };
        doBulkOperation(user, originalStoreRef, worker);
        return results;
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
        ResultSet rs = getArchivedNodes(originalStoreRef, 0, -1);
        try
        {
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
        finally
        {
            rs.close();
        }
    }

    /**
     * This is the primary purge methd that all purge methods fall back on.  It isolates the delete
     * work in a new transaction.
     */
    public void purgeArchivedNode(final NodeRef archivedNodeRef)
    {
        RetryingTransactionHelper txnHelper = transactionService.getRetryingTransactionHelper();
        RetryingTransactionCallback<Object> deleteCallback = new RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Exception
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
        txnHelper.doInTransaction(deleteCallback, false, true);
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

    /**
     * Uses batch processing and job locking to purge all archived nodes
     */
    public void purgeAllArchivedNodes(StoreRef originalStoreRef)
    {
        final String user = AuthenticationUtil.getFullyAuthenticatedUser();
        if (user == null)
        {
            throw new IllegalStateException("Cannot purge as there is no authenticated user.");
        }
        
        /**
         * Worker that purges each node
         */
        BatchProcessWorker<NodeRef> worker = new BatchProcessor.BatchProcessWorkerAdaptor<NodeRef>()
        {
            public void process(NodeRef entry) throws Throwable
            {
                AuthenticationUtil.pushAuthentication();
                try
                {
                    AuthenticationUtil.setFullyAuthenticatedUser(user);
                    if (nodeService.exists(entry))
                    {
                        nodeService.deleteNode(entry);
                    }
                }
                finally
                {
                    AuthenticationUtil.popAuthentication();
                }
            }
        };
        doBulkOperation(user, originalStoreRef, worker);
    }
    
    /**
     * Do batch-controlled work
     */
    private void doBulkOperation(final String user, StoreRef originalStoreRef, BatchProcessWorker<NodeRef> worker)
    {
        String lockToken = null;
        try
        {
            // Get a lock to keep refreshing
            lockToken = jobLockService.getLock(LOCK_QNAME, LOCK_TTL);
            // TODO: Should merely trigger a background job i.e. perhaps it should not be
            //       triggered by a user-based thread
            BatchProcessor<NodeRef> batchProcessor = new BatchProcessor<NodeRef>(
                    "ArchiveBulkPurgeOrRestore",
                    transactionService.getRetryingTransactionHelper(),
                    getArchivedNodesWorkProvider(originalStoreRef, lockToken),
                    2, 100,
                    null, null, 1000);
            batchProcessor.process(worker, true);
        }
        catch (LockAcquisitionException e)
        {
            throw new AlfrescoRuntimeException(MSG_BUSY);
        }
        finally
        {
            try
            {
                if (lockToken != null ) {jobLockService.releaseLock(lockToken, LOCK_QNAME); }
            }
            catch (LockAcquisitionException e)
            {
                // Ignore
            }
        }
    }
}
