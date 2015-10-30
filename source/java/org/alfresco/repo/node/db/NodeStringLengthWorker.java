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
package org.alfresco.repo.node.db;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.batch.BatchProcessWorkProvider;
import org.alfresco.repo.batch.BatchProcessor;
import org.alfresco.repo.batch.BatchProcessor.BatchProcessWorker;
import org.alfresco.repo.batch.BatchProcessor.BatchProcessWorkerAdaptor;
import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.repo.domain.node.NodePropertyEntity;
import org.alfresco.repo.domain.node.NodePropertyValue;
import org.alfresco.repo.domain.qname.QNameDAO;
import org.alfresco.repo.domain.schema.SchemaBootstrap;
import org.alfresco.repo.lock.JobLockService;
import org.alfresco.repo.lock.JobLockService.JobLockRefreshCallback;
import org.alfresco.repo.lock.LockAcquisitionException;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * <h1>Max String Length Worker</h1>
 * 
 * <h2>What it is</h2>
 * A worker for a scheduled job that checks and adjusts string storage for persisted strings in the system.
 * <p>
 * <h2>Settings that control the behaviour</h2>
 * <ul>
 *  <li><b>${system.maximumStringLength}</b> - the maximum length of a string that can be persisted in the *alf_node_properties.string_value* column.</li>
 *  <li><b>${system.maximumStringLength.jobQueryRange}</b> - the node ID range to query for.
 *         The process will repeat from the first to the last node, querying for up to this many nodes.
 *         Only reduce the value if the NodeDAO query takes a long time.</li>
 *  <li><b>${system.maximumStringLength.jobThreadCount}</b> - the number of threads that will handle persistence checks and changes.
 *         Increase or decrease this to allow for free CPU capacity on the machine executing the job.</li>
 * </ul>
 * <h2>How to use it</h2>
 * sdfsf
 * 
 * @author Derek Hulley
 * @since 4.1.9.2
 */
public class NodeStringLengthWorker implements ApplicationContextAware
{
    private static final QName LOCK = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "NodeStringLengthWorker");
    private static final long LOCK_TTL = 60000L;
    
    private static Log logger = LogFactory.getLog(NodeStringLengthWorker.class);
    
    private final NodeDAO nodeDAO;
    private final JobLockService jobLockService;
    private final TransactionService transactionService;
    private final QNameDAO qnameDAO;
    private final BehaviourFilter behaviourFilter;
    private ApplicationContext ctx;
    
    private final int queryRange;
    private final int threadCount;
    private final int batchSize;
    
    public NodeStringLengthWorker(
            NodeDAO nodeDAO, JobLockService jobLockService, TransactionService transactionService, QNameDAO qnameDAO,
            BehaviourFilter behaviourFilter,
            int queryRange, int threadCount)
    {
        this.nodeDAO = nodeDAO;
        this.jobLockService = jobLockService;
        this.transactionService = transactionService;
        this.qnameDAO = qnameDAO;
        this.behaviourFilter = behaviourFilter;
        
        this.queryRange = queryRange;
        this.threadCount = threadCount;
        this.batchSize = 100;
    }

    /**
     * Set the application context for event publishing during batch processing
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException
    {
        this.ctx = applicationContext;
    }

    /**
     * Performs the work, including logging details of progress.
     */
    public NodeStringLengthWorkResult execute()
    {
        // Build refresh callback
        final NodeStringLengthWorkResult progress = new NodeStringLengthWorkResult();
        JobLockRefreshCallback lockCallback = new JobLockRefreshCallback()
        {
            @Override
            public void lockReleased()
            {
                progress.inProgress.set(false);
            }
            
            @Override
            public boolean isActive()
            {
                return progress.inProgress.get();
            }
        };
        
        String lockToken = null;
        try
        {
            progress.inProgress.set(true);
            // Get the lock
            lockToken = jobLockService.getLock(LOCK, LOCK_TTL);
            // Start the refresh timer
            jobLockService.refreshLock(lockToken, LOCK, LOCK_TTL, lockCallback);

            // Now we know that we'll do something
            if (logger.isInfoEnabled())
            {
                logger.info("NodeStringLengthWorker: Starting");
            }
            
            // Do the work
            doWork(progress);
            // Done
            if (logger.isInfoEnabled())
            {
                logger.info("NodeStringLengthWorker: " + progress);
            }
        }
        catch (LockAcquisitionException e)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Skipping node string length job: " + e.getMessage());
            }
        }
        catch (Exception e)
        {
            progress.inProgress.set(false);
            logger.error("Node string length job " + progress);
            logger.error("Stopping node string length job with exception.", e);
        }
        finally
        {
            if (lockToken != null)
            {
                jobLockService.releaseLock(lockToken, LOCK);
            }
            progress.inProgress.set(false);                // The background 
        }
        // Done
        return progress;
    }
    
    /**
     * @param progress          the thread-safe progress
     */
    private synchronized void doWork(NodeStringLengthWorkResult progress) throws Exception
    {
        // Build batch processor
        BatchProcessWorkProvider<NodePropertyEntity> workProvider = new NodeStringLengthWorkProvider(progress);
        BatchProcessWorker<NodePropertyEntity> worker = new NodeStringLengthBatch(progress);
        RetryingTransactionHelper retryingTransactionHelper = transactionService.getRetryingTransactionHelper();
        retryingTransactionHelper.setForceWritable(true);
        
        BatchProcessor<NodePropertyEntity> batchProcessor = new BatchProcessor<NodePropertyEntity>(
                "NodeStringLengthWorker",
                retryingTransactionHelper,
                workProvider,
                threadCount,
                batchSize,
                ctx,
                logger,
                1000);
        batchProcessor.process(worker, true);
    }
    
    /**
     * Work provider for batch job providing string properties to process
     * @author Derek Hulley
     * @since 4.1.9.2
     */
    private class NodeStringLengthWorkProvider implements BatchProcessWorkProvider<NodePropertyEntity>
    {
        private final long maxNodeId;
        private final NodeStringLengthWorkResult progress;
        
        private NodeStringLengthWorkProvider(NodeStringLengthWorkResult progress)
        {
            this.progress = progress;
            this.maxNodeId = nodeDAO.getMaxNodeId();
        }
        
        @Override
        public int getTotalEstimatedWorkSize()
        {
            return -1;
        }

        @Override
        public Collection<NodePropertyEntity> getNextWork()
        {
            // Check that there are not too many errors
            if (progress.errors.get() > 1000)
            {
                logger.warn("Node string length work terminating; too many errors.");
                return Collections.emptyList();
            }
            
            // Keep shifting the query window up until we get results or we hit the original max node ID
            List<NodePropertyEntity> ret = Collections.emptyList();
            while (ret.isEmpty() && progress.currentMinNodeId.get() < maxNodeId)
            {
                // Calculate the node ID range
                Long minNodeId = null;
                if (progress.currentMinNodeId.get() == 0L)
                {
                    minNodeId = nodeDAO.getMinNodeId();
                    progress.currentMinNodeId.set(minNodeId);
                }
                else
                {
                    minNodeId = progress.currentMinNodeId.addAndGet(queryRange);
                }
                long maxNodeId = minNodeId + queryRange;
                
                // Query for the properties
                ret = nodeDAO.selectNodePropertiesByDataType(DataTypeDefinition.TEXT, minNodeId, maxNodeId);
            }
            
            // Done
            if (logger.isDebugEnabled())
            {
                logger.debug("Node string length work provider found " + ret.size() + " new property entities.");
            }
            return ret;
        }
    }
    
    /**
     * Class that does the actual node manipulation to change the string storage
     * @author Derek Hulley
     * @since 4.1.9.2
     */
    private class NodeStringLengthBatch extends BatchProcessWorkerAdaptor<NodePropertyEntity>
    {
        private final int typeOrdinalText = NodePropertyValue.convertToTypeOrdinal(DataTypeDefinition.TEXT);
        private final int typeOrdinalAny = NodePropertyValue.convertToTypeOrdinal(DataTypeDefinition.ANY);
        private final NodeStringLengthWorkResult progress;
        
        private NodeStringLengthBatch(NodeStringLengthWorkResult progress)
        {
            this.progress = progress;
        }

        @Override
        public void process(NodePropertyEntity entry) throws Throwable
        {
            progress.propertiesProcessed.incrementAndGet();
            
            try
            {
                Long nodeId = entry.getNodeId();
                NodePropertyValue prop = entry.getValue();
                // Get the current string value
                String text = (String) prop.getValue(DataTypeDefinition.TEXT);
                
                // Decide if the string needs changing or not
                boolean repersist = false;
                int persistedTypeOrdinal = prop.getPersistedType().intValue();
                if (text.length() > SchemaBootstrap.getMaxStringLength())
                {
                    // The text needs to be stored as a serializable_value (ANY)
                    if (typeOrdinalAny != persistedTypeOrdinal)
                    {
                        repersist = true;
                    }
                }
                else
                {
                    // The text is shorter than the current max, so it should be stored as a string_value (TEXT)
                    if (typeOrdinalText != persistedTypeOrdinal)
                    {
                        repersist = true;
                    }
                }
                
                // Only do any work if we need to
                if (repersist)
                {
                    // We do not want any behaviours associated with our transactions
                    behaviourFilter.disableBehaviour();
                    
                    progress.propertiesChanged.incrementAndGet();
                    if (logger.isTraceEnabled())
                    {
                        logger.trace("Fixing property " + getIdentifier(entry) + ".  Value: " + text);
                    }
                    else if (logger.isDebugEnabled())
                    {
                        logger.debug("Fixing property " + getIdentifier(entry));
                    }
                    Long propQNameId = entry.getKey().getQnameId();
                    QName propQName = qnameDAO.getQName(propQNameId).getSecond();
                    nodeDAO.removeNodeProperties(nodeId, Collections.singleton(propQName));
                    nodeDAO.addNodeProperty(nodeId, propQName, text);
                }
            }
            catch (Exception e)
            {
                // Record the failure
                progress.errors.incrementAndGet();
                // Rethrow so that the processing framework can handle things
                throw e;
            }
        }

        @Override
        public String getIdentifier(NodePropertyEntity entry)
        {
            Long nodeId = entry.getNodeId();
            NodePropertyValue prop = entry.getValue();
            return ("Property with persisted type " + prop.getPersistedType() + " on node " + nodeDAO.getNodePair(nodeId));
        }
    }

    /**
     * Thread-safe helper class to carry the job progress information
     * @author Derek Hulley
     * @since 4.1.9.2
     */
    public static class NodeStringLengthWorkResult
    {
        private final AtomicBoolean inProgress = new AtomicBoolean(false);
        private final AtomicInteger propertiesProcessed = new AtomicInteger(0);
        private final AtomicInteger propertiesChanged = new AtomicInteger(0);
        private final AtomicInteger errors = new AtomicInteger(0);
        private final AtomicLong currentMinNodeId = new AtomicLong(0L);
        @Override
        public String toString()
        {
            String part1 = "Changed";
            String part2 = String.format(" %4d out of a potential %4d properties. ", propertiesChanged.get(), propertiesProcessed.get());
            String part3 = String.format("[%2d Errors]", errors.get());
            return part1 + part2 + part3;
        }
        
        public int getPropertiesProcessed()
        {
            return propertiesProcessed.get();
        }
        
        public int getPropertiesChanged()
        {
            return propertiesChanged.get();
        }
        
        public int getErrors()
        {
            return errors.get();
        }
    }
    
    /**
     * A scheduled job that checks and adjusts string storage for persisted strings in the system.
     * <p>
     * Job data: 
     * <ul>
     *  <li><b>nodeStringLengthWorker</b> - The worker that performs the actual processing.</li>
     * </ul>
     * 
     * @author Derek Hulley
     * @since 4.1.9.2
     * @see NodeStringLengthWorker
     */
    public static class NodeStringLengthJob implements Job
    {
        public static final String JOB_DATA_NODE_WORKER = "nodeStringLengthWorker";
        
        public void execute(JobExecutionContext context) throws JobExecutionException
        {
            JobDataMap jobData = context.getJobDetail().getJobDataMap();
            // extract the content Cleanup to use
            Object nodeStringLengthWorkerObj = jobData.get(JOB_DATA_NODE_WORKER);
            if (nodeStringLengthWorkerObj == null || !(nodeStringLengthWorkerObj instanceof NodeStringLengthWorker))
            {
                throw new AlfrescoRuntimeException(
                        "MaxStringLengthJob data '" + JOB_DATA_NODE_WORKER + "' must reference a " + NodeStringLengthWorker.class.getSimpleName());
            }
            NodeStringLengthWorker worker = (NodeStringLengthWorker) nodeStringLengthWorkerObj;
            worker.execute();
        }
    }
}
