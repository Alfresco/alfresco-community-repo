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
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.alfresco.error.AlfrescoRuntimeException;
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
 * The callback runs on a background thread, as the user and tenant that scheduled it, and its messages are sent only after its transaction has released the DB connection, so neither a request thread nor a DB connection waits for the message broker. Pending callbacks are kept in memory only; a newer callback with the same id replaces a pending one.
 *
 * @author alex.mukha
 */
public class PostTxnCallbackScheduler
{
    private static final Log LOGGER = LogFactory.getLog(PostTxnCallbackScheduler.class);

    private TransactionService transactionService;
    private int threadPoolSize;
    private int queueSizeWarning;

    private final Map<String, PostTxTransactionListener> pending = new ConcurrentHashMap<>();
    private ThreadPoolExecutor executorService;

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

    public int getActiveCount()
    {
        return executorService == null ? 0 : executorService.getActiveCount();
    }

    public void init()
    {
        if (threadPoolSize < 1)
        {
            throw new AlfrescoRuntimeException("renditionService2.dispatch.threadPoolSize must be at least 1, but is " + threadPoolSize);
        }
        var threadFactory = new ThreadFactoryBuilder().setNameFormat("rendition-dispatch-%d").setDaemon(true).build();
        executorService = new ThreadPoolExecutor(threadPoolSize, threadPoolSize, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(), threadFactory);
    }

    public void destroy()
    {
        if (executorService != null)
        {
            executorService.shutdownNow();
            if (!pending.isEmpty())
            {
                LOGGER.warn(pending.size() + " pending rendition/transform requests were not sent and are lost.");
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
        private final String user;
        private final String tenantDomain;

        PostTxTransactionListener(RetryingTransactionHelper.RetryingTransactionCallback callback, String uniqueId)
        {
            this.callback = callback;
            this.id = uniqueId;
            String runAsUser = AuthenticationUtil.getRunAsUser();
            this.user = runAsUser == null ? AuthenticationUtil.getSystemUserName() : runAsUser;
            this.tenantDomain = TenantUtil.getCurrentDomain();
            LOGGER.debug("Created lister with id = " + id);
        }

        @Override
        public void afterCommit()
        {
            boolean alreadyQueued = pending.put(id, this) != null;
            if (alreadyQueued)
            {
                LOGGER.trace("Callback " + id + " is already queued; the queued entry now runs this newer callback.");
            }
            else
            {
                queue();
            }
        }

        private void queue()
        {
            try
            {
                executorService.execute(() -> pending.remove(id).runOnBackgroundThread());
                warnIfQueueIsLarge();
            }
            catch (RejectedExecutionException e)
            {
                pending.remove(id);
                LOGGER.warn("Callback " + id + " was not run: the repository is shutting down.");
            }
        }

        private void warnIfQueueIsLarge()
        {
            int queueSize = pending.size();
            if (queueSizeWarning > 0 && queueSize > 0 && queueSize % queueSizeWarning == 0)
            {
                LOGGER.warn(queueSize + " rendition/transform requests are waiting to be sent. Check that the message broker is reachable.");
            }
        }

        private void runOnBackgroundThread()
        {
            try
            {
                TenantUtil.runAsUserTenant(this::runCallbackThenSendMessages, user, tenantDomain);
            }
            catch (Exception e)
            {
                LOGGER.error("The after commit callback " + id + " failed to execute: " + e.getMessage(), e);
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
                LOGGER.debug("The after commit callback " + id + " failed to execute: " + e.getMessage());
                // consume exception, as before the background dispatch
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
