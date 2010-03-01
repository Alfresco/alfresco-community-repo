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
package org.alfresco.repo.batch;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.rule.RuleService;
import org.apache.commons.logging.Log;
import org.springframework.context.ApplicationEventPublisher;

/**
 * A <code>BatchProcessor</code> manages the running and monitoring of a potentially long-running transactional batch
 * process. It iterates over a collection, and queues jobs that fire a worker on a batch of members. The queued jobs
 * handle progress / error reporting, transaction delineation and retrying. They are processed in parallel by a pool of
 * threads of a configurable size. The job processing is designed to be fault tolerant and will continue in the event of
 * errors. When the batch is complete a summary of the number of errors and the last error stack trace will be logged at
 * ERROR level. Each individual error is logged at WARN level and progress information is logged at INFO level. Through
 * the {@link BatchMonitor} interface, it also supports the real-time monitoring of batch metrics (e.g. over JMX in the
 * Enterprise Edition).
 * 
 * @author dward
 */
public class BatchProcessor<T> implements BatchMonitor
{
    /** The logger to use. */
    private final Log logger;

    /** The retrying transaction helper. */
    private final RetryingTransactionHelper retryingTransactionHelper;

    /** The rule service. */
    private final RuleService ruleService;

    /** The collection. */
    private final Collection<T> collection;

    /** The process name. */
    private final String processName;

    /** The number of entries to process before reporting progress. */
    private final int loggingInterval;

    /** The number of worker threads. */
    private final int workerThreads;

    /** The number of entries we process at a time in a transaction *. */
    private final int batchSize;

    /** The current entry id. */
    private String currentEntryId;

    /** The last error. */
    private Throwable lastError;

    /** The last error entry id. */
    private String lastErrorEntryId;

    /** The total number of errors. */
    private int totalErrors;

    /** The number of successfully processed entries. */
    private int successfullyProcessedEntries;

    /** The start time. */
    private Date startTime;

    /** The end time. */
    private Date endTime;

    /**
     * Instantiates a new batch processor.
     * 
     * @param logger
     *            the logger to use
     * @param retryingTransactionHelper
     *            the retrying transaction helper
     * @param ruleService
     *            the rule service
     * @param collection
     *            the collection
     * @param processName
     *            the process name
     * @param loggingInterval
     *            the number of entries to process before reporting progress
     * @param applicationEventPublisher
     *            the application event publisher
     * @param workerThreads
     *            the number of worker threads
     * @param batchSize
     *            the number of entries we process at a time in a transaction
     */
    public BatchProcessor(Log logger, RetryingTransactionHelper retryingTransactionHelper, RuleService ruleService,
            ApplicationEventPublisher applicationEventPublisher, Collection<T> collection, String processName,
            int loggingInterval, int workerThreads, int batchSize)
    {
        this.logger = logger;
        this.retryingTransactionHelper = retryingTransactionHelper;
        this.ruleService = ruleService;
        this.collection = collection;
        this.processName = processName;
        this.loggingInterval = loggingInterval;
        this.workerThreads = workerThreads;
        this.batchSize = batchSize;
        // Let the (enterprise) monitoring side know of our presence
        applicationEventPublisher.publishEvent(new BatchMonitorEvent(this));
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.repo.security.sync.BatchMonitor#getCurrentEntryId()
     */
    public synchronized String getCurrentEntryId()
    {
        return this.currentEntryId;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.repo.security.sync.BatchMonitor#getLastError()
     */
    public synchronized String getLastError()
    {
        if (this.lastError == null)
        {
            return null;
        }
        Writer buff = new StringWriter(1024);
        PrintWriter out = new PrintWriter(buff);
        this.lastError.printStackTrace(out);
        out.close();
        return buff.toString();
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.repo.security.sync.BatchMonitor#getLastErrorEntryId()
     */
    public synchronized String getLastErrorEntryId()
    {
        return this.lastErrorEntryId;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.repo.security.sync.BatchMonitor#getBatchType()
     */
    public synchronized String getProcessName()
    {
        return this.processName;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.repo.security.sync.BatchMonitor#getSuccessfullyProcessedResults()
     */
    public synchronized int getSuccessfullyProcessedEntries()
    {
        return this.successfullyProcessedEntries;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.repo.security.sync.BatchMonitor#getPercentComplete()
     */
    public synchronized String getPercentComplete()
    {
        int totalResults = this.collection.size();
        int processed = this.successfullyProcessedEntries + this.totalErrors;
        return processed <= totalResults ? NumberFormat.getPercentInstance().format(
                totalResults == 0 ? 1.0F : (float) processed / totalResults) : "Unknown";
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.repo.security.sync.BatchMonitor#getTotalErrors()
     */
    public synchronized int getTotalErrors()
    {
        return this.totalErrors;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.repo.security.sync.BatchMonitor#getTotalResults()
     */
    public int getTotalResults()
    {
        return this.collection.size();
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.repo.security.sync.BatchMonitor#getEndTime()
     */
    public synchronized Date getEndTime()
    {
        return this.endTime;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.repo.security.sync.BatchMonitor#getStartTime()
     */
    public synchronized Date getStartTime()
    {
        return this.startTime;
    }

    /**
     * Invokes the worker for each entry in the collection, managing transactions and collating success / failure
     * information.
     * 
     * @param worker
     *            the worker
     * @param splitTxns
     *            Can the modifications to Alfresco be split across multiple transactions for maximum performance? If
     *            <code>true</code>, worker invocations are isolated in separate transactions in batches of 10 for
     *            increased performance. If <code>false</code>, all invocations are performed in the current
     *            transaction. This is required if calling synchronously (e.g. in response to an authentication event in
     *            the same transaction).
     * @return the number of invocations
     */
    @SuppressWarnings("serial")
    public int process(final Worker<T> worker, final boolean splitTxns)
    {
        int count = this.collection.size();
        synchronized (this)
        {
            this.startTime = new Date();
            if (this.logger.isInfoEnabled())
            {
                if (count >= 0)
                {
                    this.logger.info(getProcessName() + ": Commencing batch of " + count + " entries");
                }
                else
                {
                    this.logger.info(getProcessName() + ": Commencing batch");

                }
            }
        }

        // Create a thread pool executor with the specified number of threads and a finite blocking queue of jobs
        ExecutorService executorService = splitTxns && this.workerThreads > 1 ? new ThreadPoolExecutor(
                this.workerThreads, this.workerThreads, 0L, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(
                        this.workerThreads * this.batchSize * 10)
                {
                    // Add blocking behaviour to work queue
                    @Override
                    public boolean offer(Runnable o)
                    {
                        try
                        {
                            put(o);
                        }
                        catch (InterruptedException e)
                        {
                            return false;
                        }
                        return true;
                    }

                }) : null;
        try
        {
            Iterator<T> iterator = this.collection.iterator();
            List<T> batch = new ArrayList<T>(this.batchSize);
            while (iterator.hasNext())
            {
                batch.add(iterator.next());
                boolean hasNext = iterator.hasNext();
                if (batch.size() >= this.batchSize || !hasNext)
                {
                    final TxnCallback callback = new TxnCallback(worker, batch, splitTxns);
                    if (hasNext)
                    {
                        batch = new ArrayList<T>(this.batchSize);
                    }
                    if (executorService == null)
                    {
                        callback.run();
                    }
                    else
                    {
                        executorService.execute(callback);
                    }
                }
            }
            return count;
        }
        finally
        {
            if (executorService != null)
            {
                executorService.shutdown();
                try
                {
                    executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
                }
                catch (InterruptedException e)
                {
                }
            }
            synchronized (this)
            {
                reportProgress(true);
                this.endTime = new Date();
                if (this.logger.isInfoEnabled())
                {
                    if (count >= 0)
                    {
                        this.logger.info(getProcessName() + ": Completed batch of " + count + " entries");
                    }
                    else
                    {
                        this.logger.info(getProcessName() + ": Completed batch");

                    }
                }
                if (this.totalErrors > 0 && this.logger.isErrorEnabled())
                {
                    this.logger.error(getProcessName() + ": " + this.totalErrors
                            + " error(s) detected. Last error from entry \"" + this.lastErrorEntryId + "\"",
                            this.lastError);
                }
            }
        }
    }

    /**
     * Reports the current progress.
     * 
     * @param last
     *            Have all jobs been processed? If <code>false</code> then progress is only reported after the number of
     *            entries indicated by {@link #loggingInterval}. If <code>true</code> then progress is reported if this
     *            is not one of the entries indicated by {@link #loggingInterval}.
     */
    private synchronized void reportProgress(boolean last)
    {
        int processed = this.successfullyProcessedEntries + this.totalErrors;
        if (processed % this.loggingInterval == 0 ^ last)
        {
            StringBuilder message = new StringBuilder(100).append(getProcessName()).append(": Processed ").append(
                    processed).append(" entries");
            int totalResults = this.collection.size();
            if (totalResults >= processed)
            {
                message.append(" out of ").append(totalResults).append(". ").append(
                        NumberFormat.getPercentInstance().format(
                                totalResults == 0 ? 1.0F : (float) processed / totalResults)).append(" complete");
            }
            long duration = System.currentTimeMillis() - this.startTime.getTime();
            if (duration > 0)
            {
                message.append(". Rate: ").append(processed * 1000L / duration).append(" per second");
            }
            message.append(". " + this.totalErrors + " failures detected.");
            this.logger.info(message);
        }
    }

    /**
     * An interface for workers to be invoked by the {@link BatchProcessor}.
     */
    public interface Worker<T>
    {

        /**
         * Gets an identifier for the given entry (for monitoring / logging purposes).
         * 
         * @param entry
         *            the entry
         * @return the identifier
         */
        public String getIdentifier(T entry);

        /**
         * Processes the given entry.
         * 
         * @param entry
         *            the entry
         * @throws Throwable
         *             on any error
         */
        public void process(T entry) throws Throwable;
    }

    /**
     * A callback that invokes a worker on a batch, optionally in a new transaction.
     */
    class TxnCallback implements RetryingTransactionCallback<Object>, Runnable
    {

        /**
         * Instantiates a new callback.
         * 
         * @param worker
         *            the worker
         * @param batch
         *            the batch to process
         * @param splitTxns
         *            If <code>true</code>, the worker invocation is made in a new transaction.
         */
        public TxnCallback(Worker<T> worker, List<T> batch, boolean splitTxns)
        {
            this.worker = worker;
            this.batch = batch;
            this.splitTxns = splitTxns;
        }

        /** The worker. */
        private final Worker<T> worker;

        /** The batch. */
        private final List<T> batch;

        /** If <code>true</code>, the worker invocation is made in a new transaction. */
        private final boolean splitTxns;

        /** The total number of errors. */
        private int txnErrors;

        /** The number of successfully processed entries. */
        private int txnSuccesses;

        /** The current entry being processed in the transaction */
        private String txnEntryId;

        /** The last error. */
        private Throwable txnLastError;

        /** The last error entry id. */
        private String txnLastErrorEntryId;

        /*
         * (non-Javadoc)
         * @see org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback#execute ()
         */
        public Object execute() throws Throwable
        {
            reset();
            for (T entry : this.batch)
            {
                this.txnEntryId = this.worker.getIdentifier(entry);
                synchronized (BatchProcessor.this)
                {
                    BatchProcessor.this.currentEntryId = this.txnEntryId;
                }
                try
                {
                    this.worker.process(entry);
                    this.txnSuccesses++;
                }
                catch (Throwable t)
                {
                    if (RetryingTransactionHelper.extractRetryCause(t) == null)
                    {
                        if (BatchProcessor.this.logger.isWarnEnabled())
                        {
                            BatchProcessor.this.logger.warn(getProcessName() + ": Failed to process entry \""
                                    + this.txnEntryId + "\".", t);
                        }
                        this.txnLastError = t;
                        this.txnLastErrorEntryId = this.txnEntryId;
                        this.txnErrors++;
                    }
                    else
                    {
                        throw t;
                    }
                }
            }
            return null;
        }

        /*
         * (non-Javadoc)
         * @see java.lang.Runnable#run()
         */
        public void run()
        {
            // Disable rules for this thread
            BatchProcessor.this.ruleService.disableRules();
            try
            {
                BatchProcessor.this.retryingTransactionHelper.doInTransaction(this, false, this.splitTxns);
            }
            catch (Throwable t)
            {
                // If the callback was in its own transaction, it must have run out of retries
                if (this.splitTxns)
                {
                    this.txnLastError = t;
                    this.txnLastErrorEntryId = this.txnEntryId;
                    this.txnErrors++;
                    if (BatchProcessor.this.logger.isWarnEnabled())
                    {
                        BatchProcessor.this.logger.warn(getProcessName() + ": Failed to process entry \""
                                + BatchProcessor.this.currentEntryId + "\".", t);
                    }
                }
                // Otherwise, we have a retryable exception that we should propagate
                else
                {
                    if (t instanceof RuntimeException)
                    {
                        throw (RuntimeException) t;
                    }
                    if (t instanceof Error)
                    {
                        throw (Error) t;
                    }
                    throw new AlfrescoRuntimeException("Transactional error during " + getProcessName(), t);
                }
            }
            finally
            {
                // Re-enable rules
                BatchProcessor.this.ruleService.enableRules();
            }

            commitProgress();
        }

        /**
         * Resets the callback state for a retry.
         */
        private void reset()
        {
            this.txnLastError = null;
            this.txnLastErrorEntryId = null;
            this.txnSuccesses = this.txnErrors = 0;
        }

        /**
         * Commits progress from this transaction after a successful commit.
         */
        private void commitProgress()
        {
            synchronized (BatchProcessor.this)
            {
                if (this.txnErrors > 0)
                {
                    int processed = BatchProcessor.this.successfullyProcessedEntries + BatchProcessor.this.totalErrors;
                    int currentIncrement = processed % BatchProcessor.this.loggingInterval;
                    int newErrors = BatchProcessor.this.totalErrors + this.txnErrors;
                    // Work out the number of logging intervals we will cross and report them
                    int intervals = (this.txnErrors + currentIncrement) / BatchProcessor.this.loggingInterval;
                    if (intervals > 0)
                    {
                        BatchProcessor.this.totalErrors += BatchProcessor.this.loggingInterval - currentIncrement;
                        reportProgress(false);
                        while (--intervals > 0)
                        {
                            BatchProcessor.this.totalErrors += BatchProcessor.this.loggingInterval;
                            reportProgress(false);
                        }
                    }
                    BatchProcessor.this.totalErrors = newErrors;
                }

                if (this.txnSuccesses > 0)
                {
                    int processed = BatchProcessor.this.successfullyProcessedEntries + BatchProcessor.this.totalErrors;
                    int currentIncrement = processed % BatchProcessor.this.loggingInterval;
                    int newSuccess = BatchProcessor.this.successfullyProcessedEntries + this.txnSuccesses;
                    // Work out the number of logging intervals we will cross and report them
                    int intervals = (this.txnSuccesses + currentIncrement) / BatchProcessor.this.loggingInterval;
                    if (intervals > 0)
                    {
                        BatchProcessor.this.successfullyProcessedEntries += BatchProcessor.this.loggingInterval
                                - currentIncrement;
                        reportProgress(false);
                        while (--intervals > 0)
                        {
                            BatchProcessor.this.successfullyProcessedEntries += BatchProcessor.this.loggingInterval;
                            reportProgress(false);
                        }
                    }
                    BatchProcessor.this.successfullyProcessedEntries = newSuccess;
                }

                if (this.txnLastError != null)
                {
                    BatchProcessor.this.lastError = this.txnLastError;
                    BatchProcessor.this.lastErrorEntryId = this.txnLastErrorEntryId;
                }

                reset();
            }
        }
    }

}
