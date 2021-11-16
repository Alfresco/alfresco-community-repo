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
package org.alfresco.repo.batch;

import org.alfresco.api.AlfrescoPublicApi;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.node.integrity.IntegrityException;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.util.TraceableThreadFactory;
import org.alfresco.util.transaction.TransactionListenerAdapter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationEventPublisher;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

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
@AlfrescoPublicApi
public class BatchProcessor<T> implements BatchMonitor
{
    /** The factory for all new threads */
    private TraceableThreadFactory threadFactory;
    
    /** The logger to use. */
    private final Log logger;

    /** The retrying transaction helper. */
    private final RetryingTransactionHelper retryingTransactionHelper;

    /** The source of the work being done. */
    private BatchProcessWorkProvider<T> workProvider;

    /** The process name. */
    private final String processName;

    /** The number of entries to process before reporting progress. */
    private final int loggingInterval;

    /** The number of worker threads. */
    private final int workerThreads;

    /** The number of entries we process at a time in a transaction. */
    private final int batchSize;
    
    /** The current entry id. */
    private String currentEntryId;

    /** The number of batches currently executing. */
    private int executingCount;
    
    /** What transactions need to be retried?. We do these single-threaded in order to avoid cross-dependency issues */
    private SortedSet<Integer> retryTxns = new TreeSet<Integer>();

    /** The last error. */
    private Throwable lastError;

    /** The last error entry id. */
    private String lastErrorEntryId;

    /** The total number of errors. */
    private long totalErrors;

    /** The number of successfully processed entries. */
    private long successfullyProcessedEntries;

    /** The start time. */
    private Date startTime;

    /** The end time. */
    private Date endTime;

    /**
     * Instantiates a new batch processor.
     * 
     * @param processName
     *            the process name
     * @param retryingTransactionHelper
     *            the retrying transaction helper
     * @param collection
     *            the collection
     * @param workerThreads
     *            the number of worker threads
     * @param batchSize
     *            the number of entries we process at a time in a transaction
     * @param applicationEventPublisher
     *            the application event publisher (may be <tt>null</tt>)
     * @param logger
     *            the logger to use (may be <tt>null</tt>)
     * @param loggingInterval
     *            the number of entries to process before reporting progress
     *            
     * @deprecated Since 3.4, use the {@link BatchProcessWorkProvider} instead of the <tt>Collection</tt>
     */
    public BatchProcessor(
            String processName,
            RetryingTransactionHelper retryingTransactionHelper,
            final Collection<T> collection,
            int workerThreads, int batchSize,
            ApplicationEventPublisher applicationEventPublisher,
            Log logger,
            int loggingInterval)
    {
        this(
                    processName,
                    retryingTransactionHelper,
                    new BatchProcessWorkProvider<T>()
                    {
                        boolean hasMore = true;

                        @Override public int getTotalEstimatedWorkSize()
                        {
                            return (int) getTotalEstimatedWorkSizeLong();
                        }

                        @Override public long getTotalEstimatedWorkSizeLong()
                        {
                            return collection.size();
                        }

                        public Collection<T> getNextWork()
                        {
                            // Only return the collection once
                            if (hasMore)
                            {
                                hasMore = false;
                                return collection;
                            }
                            else
                            {
                                return Collections.emptyList();
                            }
                        }
                    },
                    workerThreads, batchSize,
                    applicationEventPublisher, logger, loggingInterval);
    }

    /**
     * Instantiates a new batch processor.
     * 
     * @param processName
     *            the process name
     * @param retryingTransactionHelper
     *            the retrying transaction helper
     * @param workProvider
     *            the object providing the work packets
     * @param workerThreads
     *            the number of worker threads
     * @param batchSize
     *            the number of entries we process at a time in a transaction
     * @param applicationEventPublisher
     *            the application event publisher (may be <tt>null</tt>)
     * @param logger
     *            the logger to use (may be <tt>null</tt>)
     * @param loggingInterval
     *            the number of entries to process before reporting progress
     *            
     * @since 3.4 
     */
    public BatchProcessor(
            String processName,
            RetryingTransactionHelper retryingTransactionHelper,
            BatchProcessWorkProvider<T> workProvider,
            int workerThreads, int batchSize,
            ApplicationEventPublisher applicationEventPublisher,
            Log logger,
            int loggingInterval)
    {
        this.threadFactory = new TraceableThreadFactory();
        this.threadFactory.setNamePrefix(processName);
        this.threadFactory.setThreadDaemon(true);
        
        this.processName = processName;
        this.retryingTransactionHelper = retryingTransactionHelper;
        this.workProvider = workProvider;
        this.workerThreads = workerThreads;
        this.batchSize = batchSize;
        if (logger == null)
        {
            this.logger = LogFactory.getLog(this.getClass());
        }
        else
        {
            this.logger = logger;
        }
        this.loggingInterval = loggingInterval;
        
        // Let the (enterprise) monitoring side know of our presence
        if (applicationEventPublisher != null)
        {
            applicationEventPublisher.publishEvent(new BatchMonitorEvent(this));
        }
    }

    /**
     * {@inheritDoc}
     */
    public synchronized String getCurrentEntryId()
    {
        return this.currentEntryId;
    }

    /**
     * {@inheritDoc}
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

    /**
     * {@inheritDoc}
     */
    public synchronized String getLastErrorEntryId()
    {
        return this.lastErrorEntryId;
    }

    /**
     * {@inheritDoc}
     */
    public synchronized String getProcessName()
    {
        return this.processName;
    }

    /**
     * {@inheritDoc}
     */
    @Deprecated public synchronized int getSuccessfullyProcessedEntries()
    {
        return Math.toIntExact(this.successfullyProcessedEntries);
    }

    /**
     * {@inheritDoc}
     */
    public synchronized long getSuccessfullyProcessedEntriesLong()
    {
        return this.successfullyProcessedEntries;
    }

    /**
     * {@inheritDoc}
     */
    public synchronized String getPercentComplete()
    {
        long totalResults = this.workProvider.getTotalEstimatedWorkSizeLong();
        long processed = this.successfullyProcessedEntries + this.totalErrors;
        return processed <= totalResults ? NumberFormat.getPercentInstance().format(
                totalResults == 0 ? 1.0F : (float) processed / totalResults) : "Unknown";
    }

    /**
     * {@inheritDoc}
     */
    @Deprecated public synchronized int getTotalErrors()
    {
        return Math.toIntExact(this.totalErrors);
    }

    /**
     * {@inheritDoc}
     */
    @Deprecated public int getTotalResults()
    {
        return this.workProvider.getTotalEstimatedWorkSize();
    }

    /**
     * {@inheritDoc}
     */
    public synchronized long getTotalErrorsLong()
    {
        return this.totalErrors;
    }

    /**
     * {@inheritDoc}
     */
    public long getTotalResultsLong()
    {
        return this.workProvider.getTotalEstimatedWorkSizeLong();
    }

    /**
     * {@inheritDoc}
     */
    public synchronized Date getEndTime()
    {
        return this.endTime;
    }

    /**
     * {@inheritDoc}
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
     *            <code>true</code>, worker invocations are isolated in separate transactions in batches for
     *            increased performance. If <code>false</code>, all invocations are performed in the current
     *            transaction. This is required if calling synchronously (e.g. in response to an authentication event in
     *            the same transaction).
     * @deprecated use {@link #processLong(BatchProcessWorker, boolean)} instead
     * @return the number of invocations
     */
    @SuppressWarnings("serial")
    @Deprecated public int process(final BatchProcessWorker<T> worker, final boolean splitTxns)
    {
        int count = workProvider.getTotalEstimatedWorkSize();
        return (int)process(worker, splitTxns, count);

    }

    /**
     * Invokes the worker for each entry in the collection, managing transactions and collating success / failure
     * information.
     *
     * @param worker
     *            the worker
     * @param splitTxns
     *            Can the modifications to Alfresco be split across multiple transactions for maximum performance? If
     *            <code>true</code>, worker invocations are isolated in separate transactions in batches for
     *            increased performance. If <code>false</code>, all invocations are performed in the current
     *            transaction. This is required if calling synchronously (e.g. in response to an authentication event in
     *            the same transaction).
     * @return the number of invocations
     */
    @SuppressWarnings("serial")
    public long processLong(final BatchProcessWorker<T> worker, final boolean splitTxns)
    {
        long count = workProvider.getTotalEstimatedWorkSizeLong();
        return process(worker, splitTxns, count);

    }


    private long process(final BatchProcessWorker<T> worker, final boolean splitTxns, long count)
    {
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
        ExecutorService executorService = splitTxns && this.workerThreads > 1 ?
                    new ThreadPoolExecutor(
                                this.workerThreads, this.workerThreads, 0L, TimeUnit.MILLISECONDS,
                                new ArrayBlockingQueue<Runnable>(this.workerThreads * this.batchSize * 10)
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

                                },
                                threadFactory) : null;
        try
        {
            Iterator<T> iterator = new WorkProviderIterator<T>(this.workProvider);
            int id=0;
            List<T> batch = new ArrayList<T>(this.batchSize);
            while (iterator.hasNext())
            {
                batch.add(iterator.next());
                boolean hasNext = iterator.hasNext();
                if (batch.size() >= this.batchSize || !hasNext)
                {
                    final TxnCallback callback = new TxnCallback(id++, worker, batch, splitTxns);
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
        long processed = this.successfullyProcessedEntries + this.totalErrors;
        if (processed % this.loggingInterval == 0 ^ last)
        {
            StringBuilder message = new StringBuilder(100).append(getProcessName()).append(": Processed ").append(
                    processed).append(" entries");
            long totalResults = 0;
            try
            {
                 totalResults = this.workProvider.getTotalEstimatedWorkSizeLong();
            }
            catch (UnsupportedOperationException uoe)
            {
                totalResults = this.workProvider.getTotalEstimatedWorkSize();
            }
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
    public interface BatchProcessWorker<T>
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
         * Callback to allow thread initialization before the work entries are
         * {@link #process(Object) processed}.  Typically, this will include authenticating
         * as a valid user and disbling or enabling any system flags that might affect the
         * entry processing.
         */
        public void beforeProcess() throws Throwable;

        /**
         * Processes the given entry.
         * 
         * @param entry
         *            the entry
         * @throws Throwable
         *             on any error
         */
        public void process(T entry) throws Throwable;

        /**
         * Callback to allow thread cleanup after the work entries have been
         * {@link #process(Object) processed}.
         * Typically, this will involve cleanup of authentication and resetting any
         * system flags previously set.
         * <p/>
         * This call is made regardless of the outcome of the entry processing.
         */
        public void afterProcess() throws Throwable;
    }
    
    /**
     * Adaptor that allows implementations to only implement {@link #process(Object)}
     */
    @AlfrescoPublicApi
    public static abstract class BatchProcessWorkerAdaptor<TT> implements BatchProcessWorker<TT>
    {
        /**
         * @return  Returns the <code>toString()</code> of the entry
         */
        public String getIdentifier(TT entry)
        {
            return entry.toString();
        }
        /** No-op */
        public void beforeProcess() throws Throwable
        {
        }
        /** No-op */
        public void afterProcess() throws Throwable
        {
        }
    }
    
    /**
     * Small iterator that repeatedly gets the next batch of work from a {@link BatchProcessWorkProvider}

     * @author Derek Hulley
     */
    private static class WorkProviderIterator<T> implements Iterator<T>
    {
        private BatchProcessWorkProvider<T> workProvider;
        private Iterator<T> currentIterator;
        
        private WorkProviderIterator(BatchProcessWorkProvider<T> workProvider)
        {
            this.workProvider = workProvider;
        }
        
        public boolean hasNext()
        {
            boolean hasNext = false;
            if (workProvider == null)
            {
                // The workProvider was exhausted
                hasNext = false;
            }
            else
            {
                if (currentIterator != null)
                {
                    // See if there there is any more on this specific iterator
                    hasNext = currentIterator.hasNext();
                }
                
                // If we don't have a next (remember that the workProvider is still available)
                // go and get more results
                if (!hasNext)
                {
                    Collection<T> nextWork = workProvider.getNextWork();
                    if (nextWork == null)
                    {
                        throw new RuntimeException("BatchProcessWorkProvider returned 'null' work: " + workProvider);
                    }
                    // Check that there are some results at all
                    if (nextWork.size() == 0)
                    {
                        // An empty collection indicates that there are no more results
                        workProvider = null;
                        currentIterator = null;
                        hasNext = false;
                    }
                    else
                    {
                        // There were some results, so get a new iterator
                        currentIterator = nextWork.iterator();
                        hasNext = currentIterator.hasNext();
                    }
                }
            }
            return hasNext;
        }

        public T next()
        {
            if (!hasNext())
            {
                throw new NoSuchElementException();
            }
            return currentIterator.next();
        }

        public void remove()
        {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * A callback that invokes a worker on a batch, optionally in a new transaction.
     */
    class TxnCallback extends TransactionListenerAdapter implements RetryingTransactionCallback<Object>, Runnable
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
        public TxnCallback(int id, BatchProcessWorker<T> worker, List<T> batch, boolean splitTxns)
        {
            this.id = id;
            this.worker = worker;
            this.batch = batch;
            this.splitTxns = splitTxns;
        }

        private final int id;
        
        /** The worker. */
        private final BatchProcessWorker<T> worker;

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
        
        public Object execute() throws Throwable
        {
            reset();
            if (this.batch.isEmpty())
            {
                return null;
            }
            
            // Bind this instance to the transaction
            AlfrescoTransactionSupport.bindListener(this);

            synchronized (BatchProcessor.this)
            {
                if (BatchProcessor.this.logger.isDebugEnabled())
                {
                    BatchProcessor.this.logger.debug("RETRY TXNS: " + BatchProcessor.this.retryTxns);
                }
                // If we are retrying after failure, assume there are cross-dependencies and wait for other
                // executing batches to complete
                while (!BatchProcessor.this.retryTxns.isEmpty()
                        && (BatchProcessor.this.retryTxns.first() < this.id || BatchProcessor.this.retryTxns.first() == this.id
                                && BatchProcessor.this.executingCount > 0)
                        && BatchProcessor.this.retryTxns.last() >= this.id)
                {
                    if (BatchProcessor.this.logger.isDebugEnabled())
                    {
                        BatchProcessor.this.logger.debug(Thread.currentThread().getName()
                                + " Recoverable failure: waiting for other batches to complete");
                    }
                    BatchProcessor.this.wait();
                }
                if (BatchProcessor.this.logger.isDebugEnabled())
                {
                    BatchProcessor.this.logger.debug(Thread.currentThread().getName() + " ready to execute");
                }
                BatchProcessor.this.currentEntryId = this.worker.getIdentifier(this.batch.get(0));
                BatchProcessor.this.executingCount++;
            }

            for (T entry : this.batch)
            {
                this.txnEntryId = this.worker.getIdentifier(entry);                
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
                        // Next time we retry, we will wait for other executing batches to complete
                        throw t;
                    }
                }
            }
            return null;
        }

        public void run()
        {
            try
            {
            }
            catch (Throwable e)
            {
                BatchProcessor.this.logger.error("Failed to cleanup Worker after processing.", e);
            }

            
            final BatchProcessor<T>.TxnCallback callback = this;
            try
            {
                Throwable tt = null;
                worker.beforeProcess();
                try
                {
                    BatchProcessor.this.retryingTransactionHelper.doInTransaction(callback, false, splitTxns);
                }
                catch (Throwable t)
                {
                    // Keep this and rethrow
                    tt = t;
                }
                worker.afterProcess();
                // Throw if there was a processing exception
                if (tt != null)
                {
                    throw tt;
                }
            }
            catch (Throwable t)
            {
                // If the callback was in its own transaction, it must have run out of retries
                if (this.splitTxns)
                {
                    this.txnLastError = t;
                    this.txnLastErrorEntryId = (t instanceof IntegrityException) ? "unknown" : this.txnEntryId;
                    this.txnErrors++;
                    if (BatchProcessor.this.logger.isWarnEnabled())
                    {
                        String message = (t instanceof IntegrityException) ? ": Failed on batch commit." : ": Failed to process entry \""
                                + this.txnEntryId + "\".";
                        BatchProcessor.this.logger.warn(getProcessName() + message, t);
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
                    long processed = BatchProcessor.this.successfullyProcessedEntries + BatchProcessor.this.totalErrors;
                    long currentIncrement = processed % BatchProcessor.this.loggingInterval;
                    long newErrors = BatchProcessor.this.totalErrors + this.txnErrors;
                    // Work out the number of logging intervals we will cross and report them
                    long intervals = (this.txnErrors + currentIncrement) / BatchProcessor.this.loggingInterval;
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
                    long processed = BatchProcessor.this.successfullyProcessedEntries + BatchProcessor.this.totalErrors;
                    long currentIncrement = processed % BatchProcessor.this.loggingInterval;
                    long newSuccess = BatchProcessor.this.successfullyProcessedEntries + this.txnSuccesses;
                    // Work out the number of logging intervals we will cross and report them
                    long intervals = (this.txnSuccesses + currentIncrement) / BatchProcessor.this.loggingInterval;
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
                
                // Make sure we don't wait for a failing transaction
                BatchProcessor.this.retryTxns.remove(this.id);
                BatchProcessor.this.notifyAll();                
            }
        }

        @Override
        public void afterCommit()
        {
            // Wake up any waiting batches
            synchronized (BatchProcessor.this)
            {
                BatchProcessor.this.executingCount--;
                // We do the final notifications in commitProgress so we can handle a transaction ending in a rollback
            }
        }

        @Override
        public void afterRollback()
        {
            // Wake up any waiting batches
            synchronized (BatchProcessor.this)
            {
                BatchProcessor.this.executingCount--;
                BatchProcessor.this.retryTxns.add(this.id);
                BatchProcessor.this.notifyAll();
            }
        }
    }
}
