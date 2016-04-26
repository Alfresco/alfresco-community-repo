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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.repo.domain.node.NodeDAO.NodeRefQueryCallback;
import org.alfresco.repo.lock.JobLockService;
import org.alfresco.repo.lock.LockAcquisitionException;
import org.alfresco.repo.lock.JobLockService.JobLockRefreshCallback;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.transaction.TransactionListenerAdapter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.Pair;
import org.alfresco.util.VmShutdownListener.VmShutdownException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Finds nodes with ASPECT_PENDING_FIX_ACL aspect and sets fixed ACLs for them
 * 
 * @author Andreea Dragoi
 * @since 4.2.7
 */
public class FixedAclUpdater extends TransactionListenerAdapter
{
    private static final Log log = LogFactory.getLog(FixedAclUpdater.class);
    private static final Set<QName> PENDING_FIX_ACL_ASPECT_PROPS = pendingFixAclAspectProps();

    public static final String FIXED_ACL_ASYNC_REQUIRED_KEY = "FIXED_ACL_ASYNC_REQUIRED";
    public static final String FIXED_ACL_ASYNC_CALL_KEY = "FIXED_ACL_ASYNC_CALL";

    private JobLockService jobLockService;
    private TransactionService transactionService;
    private AccessControlListDAO accessControlListDAO;
    private NodeDAO nodeDAO;
    private QName lockQName = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "FixedAclUpdater");
    private long lockTimeToLive = 10000;
    private long lockRefreshTime = lockTimeToLive / 2;
    private int maxItemBatchSize = 100;

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

    public void setLockTimeToLive(long lockTimeToLive)
    {
        this.lockTimeToLive = lockTimeToLive;
        this.lockRefreshTime = lockTimeToLive / 2;
    }

    private static Set<QName> pendingFixAclAspectProps()
    {
        Set<QName> props = new HashSet<>();
        props.add(ContentModel.PROP_SHARED_ACL_TO_REPLACE);
        props.add(ContentModel.PROP_INHERIT_FROM_ACL);
        return props;
    }

    private int findAndUpdateAcl(FixedAclUpdaterJobLockRefreshCallback jobCallback)
    {
        final Set<QName> aspects = new HashSet<>(1);
        aspects.add(ContentModel.ASPECT_PENDING_FIX_ACL);

        List<NodeRef> nodesToUpdate = getNodesWithAspects(aspects);
        int processedNodes = 0;

        // loop over results
        for (final NodeRef nodeRef : nodesToUpdate)
        {
            // Check if we have been terminated
            if (!jobCallback.isActive.get())
            {
                if (log.isDebugEnabled())
                {
                    log.debug(String.format("Processing node failed %s. Job not active", nodeRef));
                }
                // terminate
                break;
            }
            if (log.isDebugEnabled())
            {
                log.debug(String.format("Processing node %s", nodeRef));
            }
            final Long nodeId = nodeDAO.getNodePair(nodeRef).getFirst();

            try
            {
                transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
                {
                    public Void execute() throws Throwable
                    {
                        // retrieve acl properties from node
                        Long inheritFrom = (Long) nodeDAO.getNodeProperty(nodeId, ContentModel.PROP_INHERIT_FROM_ACL);
                        Long sharedAclToReplace = (Long) nodeDAO.getNodeProperty(nodeId, ContentModel.PROP_SHARED_ACL_TO_REPLACE);

                        // set inheritance using retrieved prop
                        accessControlListDAO.setInheritanceForChildren(nodeRef, inheritFrom, sharedAclToReplace, true);

                        nodeDAO.removeNodeAspects(nodeId, aspects);
                        nodeDAO.removeNodeProperties(nodeId, PENDING_FIX_ACL_ASPECT_PROPS);

                        if (log.isDebugEnabled())
                        {
                            log.debug(String.format("Node processed ", nodeRef));
                        }
                        return null;
                    }
                }, false, true);
            }
            catch (Exception e)
            {
                if (log.isDebugEnabled())
                {
                    log.debug(String.format("Could not process node ", nodeRef), e);
                }
            }

            processedNodes++;
        }

        if (log.isDebugEnabled())
        {
            log.debug(String.format("Nodes found %s; nodes processed %s", nodesToUpdate.size(), processedNodes));
        }

        return processedNodes;
    }

    public void execute()
    {
        String lockToken = null;
        FixedAclUpdaterJobLockRefreshCallback callback = new FixedAclUpdaterJobLockRefreshCallback();
        try
        {
            RunAsWork<Integer> findAndUpdateAclRunAsWork = findAndUpdateAclRunAsWork(callback);
            lockToken = jobLockService.getLock(lockQName, lockTimeToLive, 0, 1);
            while (true)
            {
                jobLockService.refreshLock(lockToken, lockQName, lockRefreshTime, callback);
                Integer processed = AuthenticationUtil.runAs(findAndUpdateAclRunAsWork, AuthenticationUtil.getSystemUserName());
                if (processed.intValue() == 0)
                {
                    // There is no more to process
                    break;
                }
                // There is still more to process, so continue
            }
        }
        catch (LockAcquisitionException e)
        {
            // already running
        }
        catch (VmShutdownException e)
        {
            if (log.isDebugEnabled())
            {
                log.debug("FixedAclUpdater aborted");
            }
        }
        finally
        {
            callback.isActive.set(false);
            jobLockService.releaseLock(lockToken, lockQName);
        }
    }

    private RunAsWork<Integer> findAndUpdateAclRunAsWork(final FixedAclUpdaterJobLockRefreshCallback callback)
    {
        final RetryingTransactionCallback<Integer> findAndUpdateAclWork = new RetryingTransactionCallback<Integer>()
        {
            public Integer execute() throws Exception
            {
                return findAndUpdateAcl(callback);
            }
        };

        // execute as system user to ensure fast, accurate results
        RunAsWork<Integer> findAndUpdateAclRunAsWork = new RunAsWork<Integer>()
        {
            @Override
            public Integer doWork() throws Exception
            {
                return transactionService.getRetryingTransactionHelper().doInTransaction(findAndUpdateAclWork, false, true);
            }
        };
        return findAndUpdateAclRunAsWork;
    }

    private List<NodeRef> getNodesWithAspects(final Set<QName> aspects)
    {
        return transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<List<NodeRef>>()
        {
            @Override
            public List<NodeRef> execute() throws Throwable
            {
                GetNodesWithAspectCallback callback = new GetNodesWithAspectCallback();
                nodeDAO.getNodesWithAspects(aspects, 1L, null, callback);
                return callback.getNodes();
            }
        }, false, true);
    }

    private class GetNodesWithAspectCallback implements NodeRefQueryCallback
    {
        private List<NodeRef> nodes = new ArrayList<>();

        @Override
        public boolean handle(Pair<Long, NodeRef> nodePair)
        {
            if (nodes.size() < maxItemBatchSize)
            {
                nodes.add(nodePair.getSecond());
                return true;
            }
            return false;
        }

        public List<NodeRef> getNodes()
        {
            return nodes;
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

    @Override
    public void afterCommit()
    {
        Thread t = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                execute();
            }
        });
        t.start();
    }
}
