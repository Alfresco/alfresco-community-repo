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
package org.alfresco.repo.domain.permissions;

import static org.apache.commons.lang3.BooleanUtils.toBoolean;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import com.google.common.collect.Sets;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.batch.BatchProcessWorkProvider;
import org.alfresco.repo.batch.BatchProcessor;
import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.repo.domain.node.NodeDAO.NodeRefQueryCallback;
import org.alfresco.repo.lock.JobLockService;
import org.alfresco.repo.lock.JobLockService.JobLockRefreshCallback;
import org.alfresco.repo.lock.LockAcquisitionException;
import org.alfresco.repo.policy.ClassPolicyDelegate;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.security.permissions.PermissionServicePolicies;
import org.alfresco.repo.security.permissions.PermissionServicePolicies.OnInheritPermissionsDisabled;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.transaction.TransactionListenerAdapter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.Pair;
import org.alfresco.util.PolicyIgnoreUtil;

/**
 * Finds nodes with ASPECT_PENDING_FIX_ACL aspect and sets fixed ACLs for them
 * 
 * @author Andreea Dragoi
 * @author sglover
 * @since 4.2.7
 */
public class FixedAclUpdater extends TransactionListenerAdapter implements ApplicationContextAware
{
    private static final Log log = LogFactory.getLog(FixedAclUpdater.class);
    private static final Set<QName> PENDING_FIX_ACL_ASPECT_PROPS = pendingFixAclAspectProps();

    public static final String FIXED_ACL_ASYNC_REQUIRED_KEY = "FIXED_ACL_ASYNC_REQUIRED";
    public static final String FIXED_ACL_ASYNC_CALL_KEY = "FIXED_ACL_ASYNC_CALL";

    protected static final QName LOCK_Q_NAME = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "FixedAclUpdater");

    private static final int DEFAULT_MAX_ITEMS = Integer.MAX_VALUE;

    /** A set of listeners to receive callback events whenever permissions are updated by this class. */
    private static Set<FixedAclUpdaterListener> listeners = Sets.newConcurrentHashSet();

    private ApplicationContext applicationContext;
    private JobLockService jobLockService;
    private TransactionService transactionService;
    private AccessControlListDAO accessControlListDAO;
    private NodeDAO nodeDAO;
    private long lockTimeToLive = 10000;
    private long lockRefreshTime = lockTimeToLive / 2;

    private int maxItemBatchSize = 100;
    private int numThreads = 4;
    private boolean forceSharedACL = false;
    private int maxItems = DEFAULT_MAX_ITEMS;
    private boolean orderNodes = true;

    private ClassPolicyDelegate<OnInheritPermissionsDisabled> onInheritPermissionsDisabledDelegate;
    private PolicyComponent policyComponent;
    private PolicyIgnoreUtil policyIgnoreUtil;

    public void setNumThreads(int numThreads)
    {
        this.numThreads = numThreads;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException
    {
        this.applicationContext = applicationContext;
    };

    public void setJobLockService(JobLockService jobLockService)
    {
        this.jobLockService = jobLockService;
    }

    public void setNodeDAO(NodeDAO nodeDAO)
    {
        this.nodeDAO = nodeDAO;
    }

    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }

    public void setAccessControlListDAO(AccessControlListDAO accessControlListDAO)
    {
        this.accessControlListDAO = accessControlListDAO;
    }

    public void setMaxItemBatchSize(int maxItemBatchSize)
    {
        this.maxItemBatchSize = maxItemBatchSize;
    }

    public void setForceSharedACL(boolean forceSharedACL)
    {
        this.forceSharedACL = forceSharedACL;
    }

    public void setOrderNodes(boolean orderNodes)
    {
        this.orderNodes = orderNodes;
    }

    public void setLockTimeToLive(long lockTimeToLive)
    {
        this.lockTimeToLive = lockTimeToLive;
        this.lockRefreshTime = lockTimeToLive / 2;
    }

    public void setMaxItems(int maxItems)
    {
        this.maxItems = maxItems > 0 ? maxItems : DEFAULT_MAX_ITEMS;
    }

    public void setPolicyComponent(PolicyComponent policyComponent)
    {
        this.policyComponent = policyComponent;
    }

    public void setPolicyIgnoreUtil(PolicyIgnoreUtil policyIgnoreUtil)
    {
        this.policyIgnoreUtil = policyIgnoreUtil;
    }

    /** Register a {@link FixedAclUpdaterListener} to be notified when a node is updated by an instance of this class. */
    public static void registerListener(FixedAclUpdaterListener listener)
    {
        listeners.add(listener);
    }

    /** Unregister a {@link FixedAclUpdaterListener} to be notified when a node is updated by an instance of this class. */
    public static void unregisterListener(FixedAclUpdaterListener listener)
    {
        listeners.remove(listener);
    }

    public void init()
    {
        onInheritPermissionsDisabledDelegate = policyComponent
                .registerClassPolicy(PermissionServicePolicies.OnInheritPermissionsDisabled.class);
    }

    private class GetNodesWithAspects
    {
        private Set<QName> aspects;
        private int workSize;
        private GetNodesWithAspectCallback getNodesCallback;

        GetNodesWithAspects(Set<QName> aspects)
        {
            this.aspects = aspects;

            this.getNodesCallback = new GetNodesWithAspectCallback();
            this.workSize = countNodesWithAspects();
        }

        int getWorkSize()
        {
            return workSize;
        }

        List<NodeRef> getNodesWithAspects()
        {
            List<NodeRef> nodes = transactionService.getRetryingTransactionHelper()
                    .doInTransaction(new RetryingTransactionCallback<List<NodeRef>>() {
                        @Override
                        public List<NodeRef> execute() throws Throwable
                        {
                            getNodesCallback.init();
                            nodeDAO.getNodesWithAspects(aspects, getNodesCallback.getMinNodeId(), null, orderNodes, maxItemBatchSize, getNodesCallback);
                            getNodesCallback.done();

                            return getNodesCallback.getNodes();
                        }
                    }, false, true);
            return nodes;
        }

        int countNodesWithAspects()
        {
            if (maxItems < DEFAULT_MAX_ITEMS)
            {
                log.info("Job limited to process a maximum of " + maxItems + " Pending Acls");
                return maxItems;
            }

            final CountNodesWithAspectCallback countNodesCallback = new CountNodesWithAspectCallback();
            int count = transactionService.getRetryingTransactionHelper()
                    .doInTransaction(new RetryingTransactionCallback<Integer>() {
                        @Override
                        public Integer execute() throws Throwable
                        {
                            nodeDAO.getNodesWithAspects(aspects, 0l, null, countNodesCallback);
                            return countNodesCallback.getCount();
                        }
                    }, false, true);
            return count;
        }
    }

    private class AclWorkProvider implements BatchProcessWorkProvider<NodeRef>
    {
        private GetNodesWithAspects getNodesWithAspects;
        private long estimatedUpdatedItems;
        private long execTime;
        private long execBatches;

        AclWorkProvider()
        {
            getNodesWithAspects = new GetNodesWithAspects(Collections.singleton(ContentModel.ASPECT_PENDING_FIX_ACL));
        }

        @Override
        public int getTotalEstimatedWorkSize()
        {
            return (int) getTotalEstimatedWorkSizeLong();
        }

        @Override
        public long getTotalEstimatedWorkSizeLong()
        {
            return getNodesWithAspects.getWorkSize();
        }

        @Override
        public Collection<NodeRef> getNextWork()
        {
            if (estimatedUpdatedItems >= maxItems)
            {
                log.info("Reached max items to process. Nodes Processed: " + estimatedUpdatedItems + "/" + maxItems);
                return Collections.emptyList();
            }

            long initTime = System.currentTimeMillis();
            Collection<NodeRef> batchNodes = getNodesWithAspects.getNodesWithAspects();
            long endTime = System.currentTimeMillis();

            if (log.isDebugEnabled())
            {
                log.debug("Query for batch executed in " + (endTime - initTime) + " ms");
            }

            if (!batchNodes.isEmpty())
            {
                // Increment estimatedUpdatedItems with the expected number of nodes to process
                estimatedUpdatedItems += batchNodes.size();
                execTime += endTime - initTime;
                execBatches++;
            }

            return batchNodes;
        }

        public double getAverageQueryExecutionTime()
        {
            return execBatches > 0 ? execTime / execBatches : 0;
        }

    }

    protected class AclWorker implements BatchProcessor.BatchProcessWorker<NodeRef>
    {
        private Set<QName> aspects = new HashSet<>(1);

        AclWorker()
        {
            aspects.add(ContentModel.ASPECT_PENDING_FIX_ACL);
        }

        public String getIdentifier(NodeRef nodeRef)
        {
            return String.valueOf(nodeRef.toString());
        }

        public void beforeProcess() throws Throwable
        {}

        public void afterProcess() throws Throwable
        {}

        public void process(final NodeRef nodeRef)
        {
            RunAsWork<Void> findAndUpdateAclRunAsWork = new RunAsWork<Void>() {
                @Override
                public Void doWork() throws Exception
                {
                    if (log.isDebugEnabled())
                    {
                        log.debug(String.format("Processing node %s", nodeRef));
                    }

                    try
                    {
                        final Long nodeId = nodeDAO.getNodePair(nodeRef).getFirst();

                        // MNT-22009 - If node was deleted and in archive store, remove the aspect and properties and do
                        // not
                        // process
                        if (nodeRef.getStoreRef().equals(StoreRef.STORE_REF_ARCHIVE_SPACESSTORE))
                        {
                            accessControlListDAO.removePendingAclAspect(nodeId);
                            return null;
                        }

                        // retrieve acl properties from node
                        Long inheritFrom = (Long) nodeDAO.getNodeProperty(nodeId, ContentModel.PROP_INHERIT_FROM_ACL);
                        Long sharedAclToReplace = (Long) nodeDAO.getNodeProperty(nodeId, ContentModel.PROP_SHARED_ACL_TO_REPLACE);

                        // set inheritance using retrieved prop
                        accessControlListDAO.setInheritanceForChildren(nodeRef, inheritFrom, sharedAclToReplace, true,
                                forceSharedACL);

                        // Remove aspect
                        accessControlListDAO.removePendingAclAspect(nodeId);

                        if (!policyIgnoreUtil.ignorePolicy(nodeRef))
                        {
                            boolean transformedToAsyncOperation = toBoolean((Boolean) AlfrescoTransactionSupport
                                    .getResource(FixedAclUpdater.FIXED_ACL_ASYNC_REQUIRED_KEY));

                            OnInheritPermissionsDisabled onInheritPermissionsDisabledPolicy = onInheritPermissionsDisabledDelegate
                                    .get(ContentModel.TYPE_BASE);
                            onInheritPermissionsDisabledPolicy.onInheritPermissionsDisabled(nodeRef, transformedToAsyncOperation);
                        }
                    }
                    catch (Exception e)
                    {
                        log.error("Job could not process pending ACL node " + nodeRef + ": " + e);
                        e.printStackTrace();
                    }

                    listeners.forEach(listener -> listener.permissionsUpdatedAsynchronously(nodeRef));

                    if (log.isDebugEnabled())
                    {
                        log.debug(String.format("Node processed %s", nodeRef));
                    }

                    return null;
                }

            };

            AuthenticationUtil.runAs(findAndUpdateAclRunAsWork, AuthenticationUtil.getSystemUserName());
        }
    };

    /** Create a new AclWorker. */
    protected AclWorker createAclWorker()
    {
        return new AclWorker();
    }

    class GetNodesWithAspectCallback implements NodeRefQueryCallback
    {
        private List<NodeRef> nodes = new ArrayList<>();
        private long minNodeId;
        private long maxNodeId;

        void init()
        {
            nodes.clear();
        }

        void done()
        {
            this.minNodeId = maxNodeId + 1;
        }

        @Override
        public boolean handle(Pair<Long, NodeRef> nodePair)
        {
            if (nodes.size() < maxItemBatchSize)
            {
                nodes.add(nodePair.getSecond());
                if (nodePair.getFirst() > maxNodeId)
                {
                    maxNodeId = nodePair.getFirst();
                }
                return true;
            }
            return false;
        }

        long getMinNodeId()
        {
            return minNodeId;
        }

        public List<NodeRef> getNodes()
        {
            return nodes;
        }
    }

    private class CountNodesWithAspectCallback implements NodeRefQueryCallback
    {
        private int count = 0;

        @Override
        public boolean handle(Pair<Long, NodeRef> nodePair)
        {
            count++;
            return true;
        }

        public int getCount()
        {
            return count;
        }
    }

    private static class FixedAclUpdaterJobLockRefreshCallback implements JobLockRefreshCallback
    {
        public AtomicBoolean isActive = new AtomicBoolean(true);

        @Override
        public boolean isActive()
        {
            return isActive.get();
        }

        @Override
        public void lockReleased()
        {
            isActive.set(false);
        }
    }

    private static Set<QName> pendingFixAclAspectProps()
    {
        Set<QName> props = new HashSet<>();
        props.add(ContentModel.PROP_SHARED_ACL_TO_REPLACE);
        props.add(ContentModel.PROP_INHERIT_FROM_ACL);
        return props;
    }

    public int execute()
    {
        String lockToken = null;
        FixedAclUpdaterJobLockRefreshCallback jobLockRefreshCallback = new FixedAclUpdaterJobLockRefreshCallback();

        try
        {
            log.info("Running FixedAclUpdater. Max Items: " + maxItems + ", Impose order: " + orderNodes);
            lockToken = jobLockService.getLock(LOCK_Q_NAME, lockTimeToLive, 0, 1);
            jobLockService.refreshLock(lockToken, LOCK_Q_NAME, lockRefreshTime, jobLockRefreshCallback);

            AclWorkProvider provider = new AclWorkProvider();
            AclWorker worker = createAclWorker();
            BatchProcessor<NodeRef> bp = new BatchProcessor<>("FixedAclUpdater",
                    transactionService.getRetryingTransactionHelper(), provider, numThreads, maxItemBatchSize, applicationContext,
                    log, 100);
            int count = bp.process(worker, true);
            log.info("FixedAclUpdater updated " + count + ". Average query time " + provider.getAverageQueryExecutionTime() + " ms");
            return count;
        }
        catch (LockAcquisitionException e)
        {
            // already running
            return 0;
        }
        finally
        {
            jobLockRefreshCallback.isActive.set(false);
            if (lockToken != null)
            {
                jobLockService.releaseLock(lockToken, LOCK_Q_NAME);
            }
        }
    }

    @Override
    public void afterCommit()
    {
        execute();
    }
}
