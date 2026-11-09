/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2018 Alfresco Software Limited
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
package org.alfresco.repo.util;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.alfresco.repo.rawevents.TransactionAwareEventProducer;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.tenant.TenantUtil;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.transaction.TransactionListenerAdapter;

/**
 * Schedules a callback to a post-commit phase.
 * <p>
 * With {@code threadPoolSize > 0} the callback runs on a background thread and its messages are sent only after its transaction has released the DB connection, so neither a request thread nor a DB connection waits for the message broker. Pending callbacks are kept in memory only; a newer callback with the same id replaces a pending one.
 *
 * @author alex.mukha
 */
public class PostTxnCallbackScheduler
{
    private static Log logger = LogFactory.getLog(PostTxnCallbackScheduler.class);

    private TransactionService transactionService;
    private int threadPoolSize;
    private int queueSizeWarning;

    private final Map<String, PostTxTransactionListener> pending = new ConcurrentHashMap<>();
    private ExecutorService executorService;

    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }

    public void setThreadPoolSize(int threadPoolSize)
    {
        this.threadPoolSize = threadPoolSize;
    }

    public void setQueueSizeWarning(int queueSizeWarning)
    {
        this.queueSizeWarning = queueSizeWarning;
    }

    public int getPendingCount()
    {
        return pending.size();
    }

    public void init()
    {
        if (threadPoolSize > 0)
        {
            var threadFactory = new ThreadFactoryBuilder().setNameFormat("rendition-dispatch-%d").setDaemon(true).build();
            executorService = Executors.newFixedThreadPool(threadPoolSize, threadFactory);
        }
    }

    public void destroy()
    {
        if (executorService != null)
        {
            executorService.shutdownNow();
            if (!pending.isEmpty())
            {
                logger.warn(pending.size() + " pending rendition/transform requests were not sent and are lost.");
            }
        }
    }

    /**
     * @param callback
     *            The callback to be scheduled in a post-commit phase
     * @param uniqueId
     *            The unique id of the callback. Consecutive requests to schedule the callback with the same id will overwrite the previously scheduled one.
     */
    public void scheduleRendition(RetryingTransactionHelper.RetryingTransactionCallback callback, String uniqueId)
    {
        AlfrescoTransactionSupport.bindListener(new PostTxTransactionListener(callback, uniqueId));
    }

    private class PostTxTransactionListener extends TransactionListenerAdapter
    {
        private final RetryingTransactionHelper.RetryingTransactionCallback callback;
        private final String id;
        private String user;
        private String tenantDomain;

        PostTxTransactionListener(RetryingTransactionHelper.RetryingTransactionCallback callback, String uniqueId)
        {
            this.callback = callback;
            this.id = uniqueId;
            logger.debug("Created lister with id = " + id);
        }

        @Override
        public void afterCommit()
        {
            String runAsUser = AuthenticationUtil.getRunAsUser();
            boolean runOnThisThread = executorService == null || runAsUser == null;
            if (runOnThisThread)
            {
                // run on the committing thread.
                runCallback();
            }
            else
            {
                // run on a background thread, outside the committing thread and its transaction.
                user = runAsUser;
                tenantDomain = TenantUtil.getCurrentDomain();
                boolean alreadyQueued = pending.put(id, this) != null;
                if (alreadyQueued)
                {
                    logger.trace("Callback " + id + " is already queued; the queued entry now runs this newer callback.");
                }
                else
                {
                    executorService.execute(() -> pending.remove(id).runOnBackgroundThread());
                    warnIfQueueIsLarge();
                }
            }
        }

        private void warnIfQueueIsLarge()
        {
            int queueSize = pending.size();
            if (queueSizeWarning > 0 && queueSize % queueSizeWarning == 0)
            {
                logger.warn(queueSize + " rendition/transform requests are waiting to be sent. Check that the message broker is reachable.");
            }
        }

        /** same user and tenant as the committing thread. */
        private void runOnBackgroundThread()
        {
            try
            {
                TenantUtil.runAsUserTenant(this::runCallbackThenSendMessages, user, tenantDomain);
            }
            catch (Exception e)
            {
                logger.error("The after commit callback " + id + " failed to execute: " + e.getMessage(), e);
            }
        }

        private Void runCallbackThenSendMessages()
        {
            TransactionAwareEventProducer.enableDeferredSendsOnThisThread();
            try
            {
                runCallback();
            }
            finally
            {
                TransactionAwareEventProducer.sendAndDisableDeferredSendsOnThisThread();
            }
            return null;
        }

        private void runCallback()
        {
            try
            {
                transactionService.getRetryingTransactionHelper().doInTransaction(callback);
            }
            catch (Exception e)
            {
                logger.debug("The after commit callback " + id + " failed to execute: " + e.getMessage());
                // consume exception in afterCommit
            }
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o)
            {
                return true;
            }
            if (o == null || getClass() != o.getClass())
            {
                return false;
            }
            PostTxTransactionListener that = (PostTxTransactionListener) o;
            return Objects.equals(id, that.id);
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(id);
        }
    }
}
