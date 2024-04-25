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
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationEventPublisher;

public abstract class BulkBaseService<T> implements InitializingBean
{
    private static final Log logger = LogFactory.getLog(BulkBaseService.class);

    private ServiceRegistry serviceRegistry;
    private SearchService searchService;
    private TransactionService transactionService;
    private SearchMapper searchMapper;
    private BulkMonitor<T> bulkMonitor;
    private ApplicationEventPublisher applicationEventPublisher;

    private int threadCount;
    private int batchSize;
    private long maxItems;
    private int loggingIntervalMs;

    @Override
    public void afterPropertiesSet() throws Exception
    {
        this.searchService = serviceRegistry.getSearchService();
    }

    public T execute(NodeRef holdRef, BulkOperation bulkOperation)
    {
        long totalItems = getTotalItems(bulkOperation.searchQuery());
        if (maxItems < totalItems)
        {
            throw new RuntimeException("Too many items to process. Please refine your query.");
        }
        String processId = UUID.randomUUID().toString();
        T initBulkStatus = getInitBulkStatus(processId, totalItems);
        bulkMonitor.updateBulkStatus(initBulkStatus);
        bulkMonitor.registerProcess(holdRef, processId);
        BatchProcessWorker<NodeRef> batchProcessWorker = getWorkerProvider(holdRef, bulkOperation);
        BatchProcessor<NodeRef> batchProcessor = new BatchProcessor<NodeRef>(
            processId,
            transactionService.getRetryingTransactionHelper(),
            getWorkProvider(bulkOperation, totalItems),
            threadCount,
            batchSize,
            applicationEventPublisher,
            logger,
            loggingIntervalMs);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(runBatchProcessor(batchProcessor, batchProcessWorker));
        return initBulkStatus;
    }

    protected Callable<Void> runBatchProcessor(BatchProcessor<NodeRef> batchProcessor,
        BatchProcessWorker<NodeRef> batchProcessWorker)
    {
        return () -> {
            TaskScheduler taskScheduler = getTaskScheduler(batchProcessor, bulkMonitor);
            taskScheduler.schedule(loggingIntervalMs);
            try
            {
                batchProcessor.processLong(batchProcessWorker, true);
                taskScheduler.runTask();
            }
            catch (Throwable t)
            {
                //TODO: handle exception
            }
            finally
            {
                taskScheduler.stopListening();
            }
            return null;
        };
    }

    protected abstract T getInitBulkStatus(String processId, long totalItems);

    protected abstract TaskScheduler getTaskScheduler(BatchProcessor<NodeRef> batchProcessor, BulkMonitor<T> monitor);

    protected abstract BatchProcessWorkProvider<NodeRef> getWorkProvider(BulkOperation bulkOperation, long totalItems);

    protected abstract BatchProcessWorker<NodeRef> getWorkerProvider(NodeRef nodeRef, BulkOperation bulkOperation);

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

    public SearchMapper getSearchMapper()
    {
        return searchMapper;
    }

    public int getBatchSize()
    {
        return batchSize;
    }

    public SearchService getSearchService()
    {
        return searchService;
    }
}
