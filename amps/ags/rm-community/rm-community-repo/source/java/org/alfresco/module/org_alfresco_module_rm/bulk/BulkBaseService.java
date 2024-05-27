/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2024 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
package org.alfresco.module.org_alfresco_module_rm.bulk;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.alfresco.repo.batch.BatchProcessWorkProvider;
import org.alfresco.repo.batch.BatchProcessor;
import org.alfresco.repo.batch.BatchProcessor.BatchProcessWorker;
import org.alfresco.rest.api.search.impl.SearchMapper;
import org.alfresco.rest.api.search.model.Query;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;

/**
 * A base class for executing bulk operations on nodes based on search query results
 */
public abstract class BulkBaseService<T> implements InitializingBean
{
    private static final Log LOG = LogFactory.getLog(BulkBaseService.class);

    protected ServiceRegistry serviceRegistry;
    protected SearchService searchService;
    protected TransactionService transactionService;
    protected SearchMapper searchMapper;
    protected BulkMonitor<T> bulkMonitor;

    protected int threadCount;
    protected int batchSize;
    protected int itemsPerTransaction;
    protected int maxItems;
    protected int loggingInterval;

    @Override
    public void afterPropertiesSet() throws Exception
    {
        this.searchService = serviceRegistry.getSearchService();
    }

    /**
     * Execute bulk operation on node based on the search query results
     *
     * @param nodeRef       node reference
     * @param bulkOperation bulk operation
     * @return bulk status
     */
    public T execute(NodeRef nodeRef, BulkOperation bulkOperation)
    {
        checkPermissions(nodeRef, bulkOperation);

        ResultSet resultSet = getTotalItems(bulkOperation.searchQuery(), maxItems);
        if (maxItems < resultSet.getNumberFound() || resultSet.hasMore())
        {
            throw new InvalidArgumentException("Too many items to process. Please refine your query.");
        }
        long totalItems = resultSet.getNumberFound();
        // Generate a random process id
        String processId = UUID.randomUUID().toString();

        T initBulkStatus = getInitBulkStatus(processId, totalItems);
        bulkMonitor.updateBulkStatus(initBulkStatus);
        bulkMonitor.registerProcess(nodeRef, processId);

        BulkProgress bulkProgress = new BulkProgress(totalItems, processId);
        BatchProcessWorker<NodeRef> batchProcessWorker = getWorkerProvider(nodeRef, bulkOperation, bulkProgress);
        BulkStatusUpdater bulkStatusUpdater = getBulkStatusUpdater();

        BatchProcessor<NodeRef> batchProcessor = new BatchProcessor<>(
            processId,
            transactionService.getRetryingTransactionHelper(),
            getWorkProvider(bulkOperation, bulkStatusUpdater, bulkProgress),
            threadCount,
            itemsPerTransaction,
            bulkStatusUpdater,
            LOG,
            loggingInterval);

        runAsyncBatchProcessor(batchProcessor, batchProcessWorker, bulkStatusUpdater);
        return initBulkStatus;
    }

    /**
     * Run batch processor
     */
    protected void runAsyncBatchProcessor(BatchProcessor<NodeRef> batchProcessor,
        BatchProcessWorker<NodeRef> batchProcessWorker, BulkStatusUpdater bulkStatusUpdater)
    {
        Runnable backgroundLogic = () -> {
            try
            {
                if (LOG.isDebugEnabled())
                {
                    LOG.debug("Started processing batch with name: " + batchProcessor.getProcessName());
                }
                batchProcessor.processLong(batchProcessWorker, true);
                if (LOG.isDebugEnabled())
                {
                    LOG.debug("Processing batch with name: " + batchProcessor.getProcessName() + " completed");
                }
            }
            catch (Exception exception)
            {
                LOG.error("Error processing batch with name: " + batchProcessor.getProcessName(), exception);
            }
            finally
            {
                bulkStatusUpdater.update();
            }
        };

        Thread backgroundThread = new Thread(backgroundLogic, "BulkBaseService-BackgroundThread");
        backgroundThread.start();
    }

    /**
     * Get initial bulk status
     *
     * @param processId  process id
     * @param totalItems total items
     * @return bulk status
     */
    protected abstract T getInitBulkStatus(String processId, long totalItems);

    /**
     * Get bulk status updater
     *
     * @return bulk status updater
     */
    protected abstract BulkStatusUpdater getBulkStatusUpdater();

    /**
     * Get work provider
     *
     * @param bulkOperation     bulk operation
     * @param totalItems        total items
     * @param bulkStatusUpdater bulk status updater
     * @return work provider
     */
    protected abstract BatchProcessWorkProvider<NodeRef> getWorkProvider(BulkOperation bulkOperation,
        BulkStatusUpdater bulkStatusUpdater, BulkProgress bulkProgress);

    /**
     * Get worker provider
     *
     * @param nodeRef       node reference
     * @param bulkOperation bulk operation
     * @return worker provider
     */
    protected abstract BatchProcessWorker<NodeRef> getWorkerProvider(NodeRef nodeRef, BulkOperation bulkOperation, BulkProgress bulkProgress);

    /**
     * Check permissions
     *
     * @param nodeRef       node reference
     * @param bulkOperation bulk operation
     */
    protected abstract void checkPermissions(NodeRef nodeRef, BulkOperation bulkOperation);

    protected ResultSet getTotalItems(Query searchQuery, int skipCount)
    {
        SearchParameters searchParams = new SearchParameters();
        searchMapper.setDefaults(searchParams);
        searchMapper.fromQuery(searchParams, searchQuery);
        searchParams.setSkipCount(skipCount);
        searchParams.setMaxItems(1);
        return searchService.query(searchParams);
    }

    protected final class BulkProgress
    {
        private final long totalItems;
        private final String processId;
        private final AtomicBoolean cancelled = new AtomicBoolean(false);
        private final AtomicInteger currentNodeNumber = new AtomicInteger(0);

        public BulkProgress(long totalItems, String processId)
        {
            this.totalItems = totalItems;
            this.processId = processId;
        }

        public AtomicBoolean isCancelled()
        {
            return cancelled;
        }

        public long getTotalItems()
        {
            return totalItems;
        }

        public String getProcessId()
        {
            return processId;
        }

        public AtomicBoolean getCancelled()
        {
            return cancelled;
        }

        public AtomicInteger getCurrentNodeNumber()
        {
            return currentNodeNumber;
        }
    }

    public void setServiceRegistry(ServiceRegistry serviceRegistry)
    {
        this.serviceRegistry = serviceRegistry;
    }

    public void setSearchService(SearchService searchService)
    {
        this.searchService = searchService;
    }

    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }

    public void setSearchMapper(SearchMapper searchMapper)
    {
        this.searchMapper = searchMapper;
    }

    public void setBulkMonitor(BulkMonitor<T> bulkMonitor)
    {
        this.bulkMonitor = bulkMonitor;
    }

    public void setThreadCount(int threadCount)
    {
        this.threadCount = threadCount;
    }

    public void setBatchSize(int batchSize)
    {
        this.batchSize = batchSize;
    }

    public void setMaxItems(int maxItems)
    {
        this.maxItems = maxItems;
    }

    public void setLoggingInterval(int loggingInterval)
    {
        this.loggingInterval = loggingInterval;
    }

    public void setItemsPerTransaction(int itemsPerTransaction)
    {
        this.itemsPerTransaction = itemsPerTransaction;
    }
}
