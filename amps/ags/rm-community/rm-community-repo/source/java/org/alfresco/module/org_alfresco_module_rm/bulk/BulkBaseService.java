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
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.alfresco.repo.batch.BatchProcessWorkProvider;
import org.alfresco.repo.batch.BatchProcessor;
import org.alfresco.repo.batch.BatchProcessor.BatchProcessWorker;
import org.alfresco.rest.api.search.impl.SearchMapper;
import org.alfresco.rest.api.search.model.Query;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationEventPublisher;

/**
 * A base class for executing bulk operations on nodes based on search query results
 */
public abstract class BulkBaseService<T> implements InitializingBean
{
    private static final Log logger = LogFactory.getLog(BulkBaseService.class);

    protected ServiceRegistry serviceRegistry;
    protected SearchService searchService;
    protected TransactionService transactionService;
    protected SearchMapper searchMapper;
    protected BulkMonitor<T> bulkMonitor;
    protected ApplicationEventPublisher applicationEventPublisher;

    protected int threadCount;
    protected int batchSize;
    protected long maxItems;
    protected int loggingIntervalMs;

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

        long totalItems = getTotalItems(bulkOperation.searchQuery());
        if (maxItems < totalItems)
        {
            throw new InvalidArgumentException("Too many items to process. Please refine your query.");
        }
        // Generate a random process id
        String processId = UUID.randomUUID().toString();

        T initBulkStatus = getInitBulkStatus(processId, totalItems);
        bulkMonitor.updateBulkStatus(initBulkStatus);
        bulkMonitor.registerProcess(nodeRef, processId);
        BatchProcessWorker<NodeRef> batchProcessWorker = getWorkerProvider(nodeRef, bulkOperation);
        BatchProcessor<NodeRef> batchProcessor = new BatchProcessor<NodeRef>(
            processId,
            transactionService.getRetryingTransactionHelper(),
            getWorkProvider(bulkOperation, totalItems),
            threadCount,
            1,
            applicationEventPublisher,
            logger,
            loggingIntervalMs);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(runBatchProcessor(batchProcessor, batchProcessWorker));
        return initBulkStatus;
    }

    /**
     * Run batch processor and schedule a task during processing
     */
    protected Callable<Void> runBatchProcessor(BatchProcessor<NodeRef> batchProcessor,
        BatchProcessWorker<NodeRef> batchProcessWorker)
    {
        return () -> {
            TaskScheduler taskScheduler = getTaskScheduler(batchProcessor, bulkMonitor);
            taskScheduler.schedule(loggingIntervalMs);
            try
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug("Started processing batch with name: " + batchProcessor.getProcessName());
                }
                batchProcessor.processLong(batchProcessWorker, true);
                if (logger.isDebugEnabled())
                {
                    logger.debug("Processing batch with name: " + batchProcessor.getProcessName() + " completed");
                }
            }
            catch (Throwable t)
            {
                logger.error("Error processing batch with name: " + batchProcessor.getProcessName(), t);
            }
            finally
            {
                taskScheduler.runTask();
                taskScheduler.unschedule();
            }
            return null;
        };
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
     * Get task scheduler
     *
     * @param batchProcessor batch processor
     * @param monitor        bulk monitor
     * @return task scheduler
     */
    protected abstract TaskScheduler getTaskScheduler(BatchProcessor<NodeRef> batchProcessor, BulkMonitor<T> monitor);

    /**
     * Get work provider
     *
     * @param bulkOperation bulk operation
     * @param totalItems    total items
     * @return work provider
     */
    protected abstract BatchProcessWorkProvider<NodeRef> getWorkProvider(BulkOperation bulkOperation, long totalItems);

    /**
     * Get worker provider
     *
     * @param nodeRef       node reference
     * @param bulkOperation bulk operation
     * @return worker provider
     */
    protected abstract BatchProcessWorker<NodeRef> getWorkerProvider(NodeRef nodeRef, BulkOperation bulkOperation);

    /**
     * Check permissions
     *
     * @param nodeRef       node reference
     * @param bulkOperation bulk operation
     */
    protected abstract void checkPermissions(NodeRef nodeRef, BulkOperation bulkOperation);

    protected long getTotalItems(Query searchQuery)
    {
        SearchParameters searchParams = new SearchParameters();
        searchMapper.setDefaults(searchParams);
        searchMapper.fromQuery(searchParams, searchQuery);
        searchParams.setSkipCount(0);
        searchParams.setMaxItems(1);
        return searchService.query(searchParams).getNumberFound();
    }

    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher)
    {
        this.applicationEventPublisher = applicationEventPublisher;
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

    public void setMaxItems(long maxItems)
    {
        this.maxItems = maxItems;
    }

    public void setLoggingIntervalMs(int loggingIntervalMs)
    {
        this.loggingIntervalMs = loggingIntervalMs;
    }
}
