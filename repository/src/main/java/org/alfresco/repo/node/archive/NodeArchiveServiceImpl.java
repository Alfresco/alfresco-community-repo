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
package org.alfresco.repo.node.archive;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.query.CannedQuery;
import org.alfresco.query.CannedQueryFactory;
import org.alfresco.query.CannedQueryResults;
import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.batch.BatchProcessWorkProvider;
import org.alfresco.repo.batch.BatchProcessor;
import org.alfresco.repo.batch.BatchProcessor.BatchProcessWorker;
import org.alfresco.repo.lock.JobLockService;
import org.alfresco.repo.lock.LockAcquisitionException;
import org.alfresco.repo.node.NodeArchiveServicePolicies;
import org.alfresco.repo.node.NodeArchiveServicePolicies.BeforePurgeNodePolicy;
import org.alfresco.repo.node.NodeArchiveServicePolicies.BeforeRestoreArchivedNodePolicy;
import org.alfresco.repo.node.NodeArchiveServicePolicies.OnRestoreArchivedNodePolicy;
import org.alfresco.repo.node.archive.RestoreNodeReport.RestoreStatus;
import org.alfresco.repo.policy.ClassPolicyDelegate;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.DuplicateChildNodeNameException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.EqualsHelper;
import org.alfresco.util.Pair;
import org.alfresco.util.ParameterCheck;
import org.alfresco.util.VmShutdownListener;
import org.alfresco.util.registry.NamedObjectRegistry;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Implementation of the node archive abstraction.
 * 
 * @author Derek Hulley, Jamal Kaabi-Mofrad
 */
public class NodeArchiveServiceImpl implements NodeArchiveService
{
    private static final QName LOCK_QNAME = QName.createQName(NamespaceService.ALFRESCO_URI, "NodeArchive");
    private static final long LOCK_TTL = 60000;
    
    private static final String MSG_BUSY = "node.archive.msg.busy";
    private static final String CANNED_QUERY_ARCHIVED_NODES_LIST = "archivedNodesCannedQueryFactory";
        
    private static Log logger = LogFactory.getLog(NodeArchiveServiceImpl.class);
    
    protected NodeService nodeService;
    private PermissionService permissionService;
    private TransactionService transactionService;
    private JobLockService jobLockService;
    private AuthorityService authorityService;
    private NamedObjectRegistry<CannedQueryFactory<ArchivedNodeEntity>> cannedQueryRegistry;
    private TenantService tenantService;
    private boolean userNamesAreCaseSensitive = false;

    /** controls policy delegates */
    private PolicyComponent policyComponent;
    private ClassPolicyDelegate<BeforePurgeNodePolicy> beforePurgeNodeDelegate;
    private ClassPolicyDelegate<BeforeRestoreArchivedNodePolicy> beforeRestoreArchivedNodeDelegate;
    private ClassPolicyDelegate<OnRestoreArchivedNodePolicy> onRestoreArchivedNodeDelegate;

    public void setPolicyComponent(PolicyComponent policyComponent)
    {
        this.policyComponent = policyComponent;
    }

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public void setPermissionService(PermissionService permissionService)
    {
        this.permissionService = permissionService;
    }

    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }

    public NodeRef getStoreArchiveNode(StoreRef originalStoreRef)
    {
        return nodeService.getStoreArchiveNode(originalStoreRef);
    }

    public void setJobLockService(JobLockService jobLockService)
    {
        this.jobLockService = jobLockService;
    }

    public void init()
    {
        // Register the various policies
        beforePurgeNodeDelegate = policyComponent.registerClassPolicy(NodeArchiveServicePolicies.BeforePurgeNodePolicy.class);
        beforeRestoreArchivedNodeDelegate = policyComponent.registerClassPolicy(NodeArchiveServicePolicies.BeforeRestoreArchivedNodePolicy.class);
        onRestoreArchivedNodeDelegate = policyComponent.registerClassPolicy(NodeArchiveServicePolicies.OnRestoreArchivedNodePolicy.class);
    }

    public void setAuthorityService(AuthorityService authorityService)
    {
        this.authorityService = authorityService;
    }
    
    public void setCannedQueryRegistry(NamedObjectRegistry<CannedQueryFactory<ArchivedNodeEntity>> cannedQueryRegistry)
    {
        this.cannedQueryRegistry = cannedQueryRegistry;
    }
    
    public void setTenantService(TenantService tenantService)
    {
        this.tenantService = tenantService;
    }
    
    public void setUserNamesAreCaseSensitive(boolean userNamesAreCaseSensitive)
    {
        this.userNamesAreCaseSensitive = userNamesAreCaseSensitive;
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
     */
    private List<NodeRef> getArchivedNodes(StoreRef originalStoreRef)
    {
        // Get the archive location
        final NodeRef archiveParentNodeRef = nodeService.getStoreArchiveNode(originalStoreRef);
        RunAsWork<List<ChildAssociationRef>> runAsWork = new RunAsWork<List<ChildAssociationRef>>()
        {
            @Override
            public List<ChildAssociationRef> doWork() throws Exception
            {
                String currentUser = AuthenticationUtil.getFullyAuthenticatedUser();
                if (currentUser == null)
                {
                    throw new AccessDeniedException("No authenticated user; cannot get archived nodes.");
                }
                return nodeService.getChildAssocs(
                        archiveParentNodeRef,
                        ContentModel.ASSOC_CHILDREN,
                        NodeArchiveService.QNAME_ARCHIVED_ITEM);
            }
        };
        // Fetch all children as 'system' user to bypass permission checks
        List<ChildAssociationRef> archivedAssocs = AuthenticationUtil.runAs(runAsWork, AuthenticationUtil.getSystemUserName());
        // Iterate and pull out NodeRefs with a permission check
        List<NodeRef> nodeRefs = new ArrayList<NodeRef>(archivedAssocs.size());
        for (ChildAssociationRef childAssociationRef : archivedAssocs)
        {
            NodeRef nodeRef = childAssociationRef.getChildRef();
            // Eliminate if the current user doesn't have permission to delete
            if (permissionService.hasPermission(nodeRef, PermissionService.DELETE) == AccessStatus.ALLOWED)
            {
                nodeRefs.add(nodeRef);
            }
        }
        return nodeRefs;
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
            private List<NodeRef> nodeRefs;
            private boolean done;
            private synchronized List<NodeRef> getNodeRefs()
            {
                if (nodeRefs == null)
                {
                    nodeRefs = getArchivedNodes(originalStoreRef);
                }
                return nodeRefs;
            }
            /**
             * @return              Returns 0, always
             */
            public synchronized int getTotalEstimatedWorkSize()
            {
                return 0;
            }

            /**
             * @return              Returns 0, always
             */
            public synchronized long getTotalEstimatedWorkSizeLong()
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
                
                if (done)
                {
                    return Collections.emptyList();
                }
                else
                {
                    done = true;
                    return getNodeRefs();
                }
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
                    NodeRef restoredNodeRef = null;
                    invokeBeforeRestoreArchivedNode(archivedNodeRef);
                    try
                    {
                        restoredNodeRef = nodeService.restoreNode(archivedNodeRef, destinationNodeRef, assocTypeQName, assocQName);
                    }
                    finally
                    {
                        invokeOnRestoreArchivedNode(restoredNodeRef);
                    }
                    return restoredNodeRef;
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
        catch (DuplicateChildNodeNameException e)
        {
            report.setCause(e);
            report.setStatus(RestoreStatus.FAILURE_DUPLICATE_CHILD_NODE_NAME);
            logger.error(e);
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

    protected void invokeBeforeRestoreArchivedNode(NodeRef nodeRef)
    {
        if (nodeRef == null || ignorePolicy(nodeRef))
        {
            return;
        }

        // get qnames to invoke against
        Set<QName> qnames = getTypeAndAspectQNames(nodeRef);
        // execute policy for node type and aspects
        NodeArchiveServicePolicies.BeforeRestoreArchivedNodePolicy policy = beforeRestoreArchivedNodeDelegate.get(nodeRef, qnames);
        policy.beforeRestoreArchivedNode(nodeRef);
    }

    protected void invokeOnRestoreArchivedNode(NodeRef nodeRef)
    {
        if (nodeRef == null || ignorePolicy(nodeRef))
        {
            return;
        }

        // get qnames to invoke against
        Set<QName> qnames = getTypeAndAspectQNames(nodeRef);
        // execute policy for node type and aspects
        NodeArchiveServicePolicies.OnRestoreArchivedNodePolicy policy = onRestoreArchivedNodeDelegate.get(nodeRef, qnames);
        policy.onRestoreArchivedNode(nodeRef);
    }

    /**
     * This is the primary purge methd that all purge methods fall back on.  It isolates the delete
     * work in a new transaction.
     */
    public void purgeArchivedNode(final NodeRef archivedNodeRef)
    {
        RetryingTransactionHelper txnHelper = transactionService.getRetryingTransactionHelper();
        RetryingTransactionCallback<Void> deleteCallback = new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Exception
            {
                if (!nodeService.exists(archivedNodeRef))
                {
                    // Node has disappeared
                    return null;
                }
                invokeBeforePurgeNode(archivedNodeRef);
                nodeService.deleteNode(archivedNodeRef);
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
            @Override
            public void beforeProcess() throws Throwable
            {
                AuthenticationUtil.pushAuthentication();
            }
            public void process(NodeRef nodeRef) throws Throwable
            {
                AuthenticationUtil.setFullyAuthenticatedUser(user);
                if (nodeService.exists(nodeRef))
                {
                    invokeBeforePurgeNode(nodeRef);
                    nodeService.deleteNode(nodeRef);
                }
            }
            @Override
            public void afterProcess() throws Throwable
            {
                AuthenticationUtil.popAuthentication();
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
                    2, 20,
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

    /**
     * {@inheritDoc}
     */
    public PagingResults<NodeRef> listArchivedNodes(ArchivedNodesCannedQueryBuilder cannedQueryBuilder)
    {
        ParameterCheck.mandatory("cannedQueryBuilder", cannedQueryBuilder);

        Long start = (logger.isDebugEnabled() ? System.currentTimeMillis() : null);

        // get canned query
        GetArchivedNodesCannedQueryFactory getArchivedNodesCannedQueryFactory = (GetArchivedNodesCannedQueryFactory) cannedQueryRegistry
                    .getNamedObject(CANNED_QUERY_ARCHIVED_NODES_LIST);

        Pair<NodeRef, QName> archiveNodeRefAssocTypePair = getArchiveNodeRefAssocTypePair(cannedQueryBuilder.getArchiveRootNodeRef());
        GetArchivedNodesCannedQuery cq = (GetArchivedNodesCannedQuery) getArchivedNodesCannedQueryFactory
                    .getCannedQuery(archiveNodeRefAssocTypePair.getFirst(), archiveNodeRefAssocTypePair.getSecond(),
                                cannedQueryBuilder.getFilter(),
                                cannedQueryBuilder.isFilterIgnoreCase(),
                                cannedQueryBuilder.getPagingRequest(),
                                cannedQueryBuilder.getSortOrderAscending());

        // execute canned query
        final CannedQueryResults<ArchivedNodeEntity> results = ((CannedQuery<ArchivedNodeEntity>) cq).execute();

        final List<ArchivedNodeEntity> page;
        if (results.getPageCount() > 0)
        {
            page = results.getPages().get(0);
        }
        else
        {
            page = Collections.emptyList();
        }
        
        // set total count
        final Pair<Integer, Integer> totalCount;
        PagingRequest pagingRequest = cannedQueryBuilder.getPagingRequest();
        if (pagingRequest.getRequestTotalCountMax() > 0)
        {
            totalCount = results.getTotalResultCount();
        }
        else
        {
            totalCount = null;
        }

        if (start != null)
        {
            int skipCount = pagingRequest.getSkipCount();
            int maxItems = pagingRequest.getMaxItems();
            int pageNum = (skipCount / maxItems) + 1;
            
            if (logger.isDebugEnabled())
            {
                StringBuilder sb = new StringBuilder(300);
                sb.append("listArchivedNodes: ").append(page.size()).append(" items in ")
                            .append((System.currentTimeMillis() - start)).append("ms ")
                            .append("[pageNum=").append(pageNum).append(", skip=").append(skipCount)
                            .append(", max=").append(maxItems).append(", hasMorePages=")
                            .append(results.hasMoreItems()).append(", totalCount=")
                            .append(totalCount).append(", filter=")
                            .append(cannedQueryBuilder.getFilter()).append(", sortOrderAscending=")
                            .append(cannedQueryBuilder.getSortOrderAscending()).append("]");

                logger.debug(sb.toString());
            }
        }
        return new PagingResults<NodeRef>()
        {
            @Override
            public String getQueryExecutionId()
            {
                return results.getQueryExecutionId();
            }

            @Override
            public List<NodeRef> getPage()
            {
                List<NodeRef> nodeRefs = new ArrayList<NodeRef>(page.size());
                for (ArchivedNodeEntity entity : page)
                {
                    nodeRefs.add(entity.getNodeRef());
                }
                return nodeRefs;
            }

            @Override
            public boolean hasMoreItems()
            {
                return results.hasMoreItems();
            }

            @Override
            public Pair<Integer, Integer> getTotalResultCount()
            {
                return totalCount;
            }
        };
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean hasFullAccess(NodeRef nodeRef)
    {
        ParameterCheck.mandatory("nodeRef", nodeRef);

        String currentUser = getCurrentUser();        
        if (hasAdminAccess(currentUser))
        {
            return true;
        }
        else
        {
            String archivedBy = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_ARCHIVED_BY);
            if(!userNamesAreCaseSensitive && archivedBy != null)
            {
                archivedBy = archivedBy.toLowerCase();
            }
            return currentUser.equals(archivedBy);
        }
    }
    
    protected boolean hasAdminAccess(String userID)
    {
        return authorityService.isAdminAuthority(userID);
    }
    
    private Pair<NodeRef, QName> getArchiveNodeRefAssocTypePair(final NodeRef archiveStoreRootNodeRef)
    {
        final String currentUser = getCurrentUser();

        if (archiveStoreRootNodeRef == null || !nodeService.exists(archiveStoreRootNodeRef))
        {
            throw new InvalidNodeRefException("Invalid archive store root node Ref.",
                        archiveStoreRootNodeRef);
        }

        if (hasAdminAccess(currentUser))
        {
            return new Pair<NodeRef, QName>(archiveStoreRootNodeRef, ContentModel.ASSOC_CHILDREN);
        }
        else
        {
            List<ChildAssociationRef> list = AuthenticationUtil.runAs(new RunAsWork<List<ChildAssociationRef>>()
            {
                @Override
                public List<ChildAssociationRef> doWork() throws Exception
                {
                    return nodeService.getChildrenByName(archiveStoreRootNodeRef,
                            ContentModel.ASSOC_ARCHIVE_USER_LINK,
                            Collections.singletonList(currentUser));
                }
            }, AuthenticationUtil.getAdminUserName());

            // Empty list means that the current user hasn't deleted anything yet.
            if (list == null || list.isEmpty())
            {
                return new Pair<NodeRef, QName>(null, null);
            }
            NodeRef userArchive = list.get(0).getChildRef();
            return new Pair<NodeRef, QName>(userArchive, ContentModel.ASSOC_ARCHIVED_LINK);
        }
    }
    
    private String getCurrentUser()
    {
        String currentUser = AuthenticationUtil.getFullyAuthenticatedUser();
        if (currentUser == null)
        {
            throw new AccessDeniedException("No authenticated user; cannot get archived nodes.");
        }

        if (!userNamesAreCaseSensitive
                    && !AuthenticationUtil.getSystemUserName().equals(
                                tenantService.getBaseNameUser(currentUser)))
        {
            // user names are not case-sensitive
            currentUser = currentUser.toLowerCase();
        }
        return currentUser;
    }

    protected void invokeBeforePurgeNode(NodeRef nodeRef)
    {
        if (ignorePolicy(nodeRef))
        {
            return;
        }
        
        // get qnames to invoke against
        Set<QName> qnames = getTypeAndAspectQNames(nodeRef);
        // execute policy for node type and aspects
        NodeArchiveServicePolicies.BeforePurgeNodePolicy policy = beforePurgeNodeDelegate.get(nodeRef, qnames);
        policy.beforePurgeNode(nodeRef);
    }

    /**
     * Get all aspect and node type qualified names
     * 
     * @param nodeRef
     *            the node we are interested in
     * @return Returns a set of qualified names containing the node type and all
     *         the node aspects, or null if the node no longer exists
     */
    protected Set<QName> getTypeAndAspectQNames(NodeRef nodeRef)
    {
        Set<QName> qnames = null;
        try
        {
            Set<QName> aspectQNames = nodeService.getAspects(nodeRef);
            
            QName typeQName = nodeService.getType(nodeRef);
            
            qnames = new HashSet<QName>(aspectQNames.size() + 1);
            qnames.addAll(aspectQNames);
            qnames.add(typeQName);
        }
        catch (InvalidNodeRefException e)
        {
            qnames = Collections.emptySet();
        }
        // done
        return qnames;
    }

    private boolean ignorePolicy(NodeRef nodeRef)
    {
        return false;
    }
}
