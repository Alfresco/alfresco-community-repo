/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2026 Alfresco Software Limited
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
package org.alfresco.repo.search.impl.elasticsearch.contentmodelsync;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.alfresco.repo.dictionary.CompiledModel;
import org.alfresco.repo.dictionary.DictionaryDAOImpl;
import org.alfresco.repo.dictionary.DictionaryListener;
import org.alfresco.repo.lock.JobLockService;
import org.alfresco.repo.lock.JobLockService.JobLockRefreshCallback;
import org.alfresco.repo.lock.LockAcquisitionException;
import org.alfresco.repo.search.impl.elasticsearch.admin.SearchEngineDetector;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * Class responsible for initialising Elasticsearch when the subsystem is started.
 */
@SuppressWarnings({"PMD.GodClass", "PMD.SingularField", "PMD.LongVariable", "PMD.GuardLogStatement"})
public class ElasticsearchInitialiser implements DictionaryListener
{

    /**
     * The name of the lock used to ensure that Elasticsearch configuration does not run on more than one node at the same time.
     */
    public static final QName LOCK_QNAME = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI,
            "ElasticSearchConfiguration");

    /**
     * The time this lock will persist in the database (60 sec but refreshed at regular intervals)
     */
    public static final long LOCK_TTL = 1000 * 60;

    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticsearchInitialiser.class);

    /**
     * Upper bound on how long {@link #stop()} will wait for the initialiser thread to terminate before giving up and logging a warning. Prevents Spring context reload or shutdown from blocking indefinitely if the worker is stuck in non-interruptible work.
     */
    private static final long STOP_MAX_WAIT_MILLIS = TimeUnit.SECONDS.toMillis(30);

    /**
     * HTTP status used to identify an invalid mapping definition.
     */
    private static final int HTTP_STATUS_BAD_REQUEST = 400;

    private DictionaryDAOImpl dictionaryDAO;
    private ContentModelSynchronizer contentModelSynchronizer;
    private ElasticsearchIndexService elasticsearchIndexService;
    private boolean createIndexIfNotExists;

    private int retryAttempts;
    private int lockRetryAttempts;
    private int lockRetryPeriodSeconds;
    private int retryPeriodSeconds;
    private JobLockService jobLockService;
    private Thread thread;
    private AtomicBoolean isTerminated = new AtomicBoolean(false);

    // Keeps track of properties already processed to avoid mapping them again.
    private final Set<QName> mappedPropertyCache = ConcurrentHashMap.newKeySet();

    // Maximum number of properties included in one Elasticsearch mapping request.
    // Overridden by elasticsearch.mappingBatchSize; this default applies when constructed directly.
    private int mappingBatchSize = 500;

    // This counter will be used during the map model execution
    private final AtomicInteger globalModelInitialisedCounter = new AtomicInteger(0);
    private SearchEngineDetector searchEngineDetector;

    public ElasticsearchInitialiser(DictionaryDAOImpl dictionary, ElasticsearchIndexService elasticsearchIndexService,
            ContentModelSynchronizer contentModelSynchronizer, JobLockService jobLockService, int retryAttempts, int retryPeriodSeconds,
            int lockRetryAttempts, int lockRetryPeriodSeconds, boolean createIndexIfNotExists, SearchEngineDetector searchEngineDetector)
    {
        this.dictionaryDAO = dictionary;
        this.dictionaryDAO.registerListener(this);
        this.contentModelSynchronizer = contentModelSynchronizer;
        this.jobLockService = jobLockService;
        this.retryAttempts = retryAttempts;
        this.retryPeriodSeconds = retryPeriodSeconds;
        this.lockRetryAttempts = lockRetryAttempts;
        this.lockRetryPeriodSeconds = lockRetryPeriodSeconds;
        this.createIndexIfNotExists = createIndexIfNotExists;
        this.elasticsearchIndexService = elasticsearchIndexService;
        this.searchEngineDetector = searchEngineDetector;
    }

    public ElasticsearchInitialiser()
    {}

    /**
     * Stop the index initialization. This method is required when the Spring context is reloaded in order to stop the asynchronous initialization.
     */
    @SuppressWarnings("PMD.CompareObjectsWithEquals")
    public void stop()
    {
        LOGGER.debug("Elasticsearch index initialising stopped");
        isTerminated.set(true);

        Thread initialiserThread = thread;
        // Intentional reference comparison: we need to know whether the caller is the initialiser thread itself.
        if (initialiserThread != null && initialiserThread != Thread.currentThread())
        {
            initialiserThread.interrupt();
            waitForThreadToStop(initialiserThread);
        }
    }

    private void waitForThreadToStop(Thread initialiserThread)
    {
        boolean interrupted = false;
        long deadline = System.currentTimeMillis() + STOP_MAX_WAIT_MILLIS;
        try
        {
            while (initialiserThread.isAlive())
            {
                long remaining = deadline - System.currentTimeMillis();
                if (remaining <= 0)
                {
                    LOGGER.warn("Elasticsearch initialiser thread did not stop within {} ms; abandoning wait",
                            STOP_MAX_WAIT_MILLIS);
                    break;
                }
                try
                {
                    initialiserThread.join(Math.min(TimeUnit.SECONDS.toMillis(1), remaining));
                }
                catch (InterruptedException exception)
                {
                    interrupted = true;
                    LOGGER.debug("Interrupted while waiting for Elasticsearch initialiser thread to stop", exception);
                }
            }
        }
        finally
        {
            if (interrupted)
            {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Async initialisation method, a lock will be used to avoid multiple initialisation
     *
     * @return the started thread created to execute the asynchronous initialisation.
     */
    public Thread initAsync()
    {
        thread = new Thread(this::initWithLock);
        thread.setName("elasticsearch-initializer");
        thread.start();
        return thread;
    }

    /**
     * Initialisation method, a lock will be used to avoid multiple initialisation
     */
    public void initWithLock()
    {
        String lockToken = null;

        ElasticsearchInitialiserJobLock jobLockRefreshCallback = new ElasticsearchInitialiserJobLock();

        boolean success = false;
        int retryAttemptsRemaining = lockRetryAttempts;
        while (!success && !isTerminated.get() && retryAttemptsRemaining > 0)
        {
            retryAttemptsRemaining--;
            try
            {
                lockToken = jobLockService.getLock(LOCK_QNAME, LOCK_TTL, jobLockRefreshCallback);
                LOGGER.trace("Elasticsearch index initialising started");

                init();

                success = true;
                LOGGER.trace("Elasticsearch index initialising completed");
            }
            catch (LockAcquisitionException e)
            {
                LOGGER.debug("Elasticsearch index initialising already underway, {} attempts left", retryAttemptsRemaining);
                waitBeforeRetry(lockRetryPeriodSeconds);
            }
            catch (Exception ex)
            {
                LOGGER.error("Error initialising Elasticsearch index", ex);
                throw ex;
            }
            finally
            {
                // The lock will self-release if answer isActive in the negative
                jobLockRefreshCallback.lockReleased();
                if (lockToken != null)
                {
                    jobLockService.releaseLock(lockToken, LOCK_QNAME);
                }
            }
        }

        if (!success)
        {
            LOGGER.debug("Elasticsearch index initialising not completed due to concurrent thread already performing the model mapping");
        }
    }

    /**
     * Check if the index exists and attempt to create it if the configuration permits. When creating the index then also load the analysers and the basic mappings. Retry periodically on failure, and only return once the index exists. The index will be automatically created only if elasticsearch.createIndexIfNotExists property is true.
     */
    public void init()
    {
        boolean indexCreated = elasticsearchIndexService.indexExists();
        boolean analysersLoaded;
        boolean success = false;
        while (!success && !isTerminated.get())
        {
            if (createIndexIfNotExists)
            {
                indexCreated = indexCreated || elasticsearchIndexService.createIndex();
                analysersLoaded = indexCreated && contentModelSynchronizer.loadSupportedAnalyzersOnStartup();
            }
            else
            {
                // Assume that if the index has been created externally then the analysers are correct.
                analysersLoaded = elasticsearchIndexService.indexExists();
            }
            if (elasticsearchIndexService.isMappingLoaded())
            {
                success = true;
            }
            else
            {
                success = analysersLoaded && contentModelSynchronizer.loadBasicIndexMappingsOnStartup();
                if (!success)
                {
                    waitBeforeRetry(retryPeriodSeconds);
                }
            }
        }
        LOGGER.info("Successfully connected to Elasticsearch index.");

        if (!isTerminated.get() && searchEngineDetector != null)
        {
            searchEngineDetector.detectAndStore();
        }
        // Attempt to map the models.
        mapModels();
    }

    /**
     * Wait for the configured period.
     *
     * @param waitPeriod
     */
    private void waitBeforeRetry(long waitPeriod)
    {
        try
        {
            TimeUnit.SECONDS.sleep(waitPeriod);
        }
        catch (InterruptedException e)
        {
            LOGGER.debug("Waiting for lock interrupted", e);
        }
    }

    @Override
    public void onDictionaryInit()
    {
        // the content model synchronization doesn't need to do anything on a Dictionary Init
    }

    @Override
    public void afterDictionaryDestroy()
    {
        // the content model synchronization doesn't need to do anything after a Dictionary Destroy
    }

    /**
     * Get a lock before mapping models. Reuse the same lock as {@link #initWithLock}.
     */
    @Override
    public void afterDictionaryInit()
    {
        String lockToken = null;

        ElasticsearchInitialiserJobLock jobLockRefreshCallback = new ElasticsearchInitialiserJobLock();

        boolean success = false;
        int retryAttemptsRemaining = lockRetryAttempts;
        while (!success && retryAttemptsRemaining > 0)
        {
            retryAttemptsRemaining--;
            try
            {
                lockToken = jobLockService.getLock(LOCK_QNAME, LOCK_TTL, jobLockRefreshCallback);
                LOGGER.trace("Model mapping started");

                boolean hasMappedNewModels = mapModels();

                if (hasMappedNewModels)
                {
                    LOGGER.info(
                            "Elasticsearch mappings update completed. {} out of {} properties were mapped successfully. "
                                    + "Turn on DEBUG for elasticsearch.contentmodelsync.FieldMappingBuilder and restart "
                                    + "the server for more information",
                            globalModelInitialisedCounter.get(), mappedPropertyCache.size());
                }

                success = true;
            }
            catch (LockAcquisitionException e)
            {
                LOGGER.debug("Model mapping already underway, {} attempts left", retryAttemptsRemaining);
                waitBeforeRetry(lockRetryPeriodSeconds);
            }
            catch (Exception ex)
            {
                LOGGER.error("Error updating elasticseach mappings", ex);
                throw ex;
            }
            finally
            {
                jobLockRefreshCallback.lockReleased();
                if (lockToken != null)
                {
                    jobLockService.releaseLock(lockToken, LOCK_QNAME);
                }
            }
        }

        if (!success)
        {
            LOGGER.debug("Elasticsearch mappings update not completed due to concurrent thread already performing the model mapping");
        }
    }

    /**
     * This method will be invoked at startup and every time a afterDictionaryInit event is triggered.
     *
     * @return true if new models were mapped during the method execution, false otherwise.
     */
    private boolean mapModels()
    {
        int attemptsRemaining = retryAttempts;
        String lastFailureReason = "No failure detected";
        Collection<QName> modelsToInit = dictionaryDAO.getModels(true);
        int currentPropertiesInitialisedCounter = 0;
        while (!modelsToInit.isEmpty() && attemptsRemaining > 0)
        {
            attemptsRemaining--;
            List<QName> failedModels = new LinkedList<>();
            LOGGER.trace("Elasticsearch Field Mapping update started");
            if (elasticsearchIndexService.indexExists())
            {
                List<PendingProperty> pending = new ArrayList<>();
                for (QName model : modelsToInit)
                {
                    CompiledModel toInit = dictionaryDAO.getCompiledModel(model);
                    for (PropertyDefinition property : getUnmappedProperties(toInit))
                    {
                        pending.add(new PendingProperty(model, property));
                    }
                }
                Set<QName> failed = new LinkedHashSet<>();
                currentPropertiesInitialisedCounter += mapInBatches(pending, modelsToInit, failed);
                failedModels.addAll(failed);
                if (!failedModels.isEmpty())
                {
                    lastFailureReason = "Mappings update failed or was not acknowledged by Elasticsearch";
                }
                modelsToInit = failedModels;
            }
            else
            {
                lastFailureReason = "Index does not exist";
                LOGGER.warn("Elasticsearch mappings could not be updated as the Index does not exist, {} attempts left",
                        attemptsRemaining);
            }
            if (!modelsToInit.isEmpty())
            {
                LOGGER.trace("Elasticsearch field mapping update, {} attempts left, {} models left to map",
                        attemptsRemaining, modelsToInit.size());
                waitBeforeRetry(retryPeriodSeconds);
            }
        }
        if (modelsToInit.isEmpty())
        {
            globalModelInitialisedCounter.addAndGet(currentPropertiesInitialisedCounter);
            boolean hasNewModelsMapped = currentPropertiesInitialisedCounter > 0;
            return hasNewModelsMapped;
        }
        else
        {
            LOGGER.error("Elasticsearch mappings update failed after {} attempts. {} models were not mapped. reason='{}', models={}",
                    retryAttempts, modelsToInit.size(), lastFailureReason, modelsToInit);
            return false;
        }
    }

    /**
     * Groups properties into batches and sends them to Elasticsearch.
     *
     * @param pending
     *            properties waiting to be mapped
     * @param processedModels
     *            models being processed
     * @param failedModels
     *            models whose properties could not be mapped
     * @return the number of successfully mapped properties
     */
    private int mapInBatches(List<PendingProperty> pending, Collection<QName> processedModels, Set<QName> failedModels)
    {
        if (pending.isEmpty())
        {
            // Send an empty request to verify Elasticsearch is available.
            if (sendMappingRequest(Set.of()).outcome() != MappingOutcome.MAPPED)
            {
                failedModels.addAll(processedModels);
            }
            return 0;
        }

        int mappedProperties = 0;
        List<PendingProperty> batch = new ArrayList<>();

        for (PendingProperty entry : pending)
        {
            batch.add(entry);
            // Send the batch when it reaches the configured size.
            if (batch.size() >= mappingBatchSize)
            {
                BatchResult result = mapBatch(batch, failedModels);
                mappedProperties += result.mappedProperties();
                if (result.elasticsearchUnavailable())
                {
                    failedModels.addAll(processedModels);
                    return mappedProperties;
                }
                batch = new ArrayList<>();
            }
        }
        // Map any remaining properties that don't fill a complete batch.
        if (!batch.isEmpty())
        {
            BatchResult result = mapBatch(batch, failedModels);
            mappedProperties += result.mappedProperties();
            if (result.elasticsearchUnavailable())
            {
                failedModels.addAll(processedModels);
            }
        }
        return mappedProperties;
    }

    /**
     * Maps a batch of properties to Elasticsearch. Invalid batches are split to isolate the rejected property. Elasticsearch failures are returned so the batch can be retried later.
     *
     * @param batch
     *            properties to map
     * @param failedModels
     *            models containing rejected properties
     * @return the number of mapped properties and whether Elasticsearch is unavailable
     */
    private BatchResult mapBatch(List<PendingProperty> batch, Set<QName> failedModels)
    {
        Set<PropertyDefinition> properties = batch.stream()
                .map(PendingProperty::property)
                .collect(Collectors.toSet());
        MappingResult result = sendMappingRequest(properties);
        if (result.outcome() == MappingOutcome.MAPPED)
        {
            batch.forEach(entry -> mappedPropertyCache.add(entry.property().getName()));
            return new BatchResult(result.mappedProperties(), false);
        }
        if (result.outcome() == MappingOutcome.UNAVAILABLE)
        {
            return new BatchResult(0, true);
        }
        if (batch.size() == 1)
        {
            PendingProperty rejected = batch.get(0);
            failedModels.add(rejected.model());
            LOGGER.warn("Elasticsearch rejected property {} from model {} other properties are unaffected",
                    rejected.property().getName(), rejected.model());
            return new BatchResult(0, false);
        }
        int midpoint = batch.size() / 2;
        BatchResult first = mapBatch(new ArrayList<>(batch.subList(0, midpoint)), failedModels);
        if (first.elasticsearchUnavailable())
        {
            return first;
        }
        BatchResult second = mapBatch(new ArrayList<>(batch.subList(midpoint, batch.size())), failedModels);
        return new BatchResult(first.mappedProperties() + second.mappedProperties(), second.elasticsearchUnavailable());
    }

    /**
     * Sends the given properties to Elasticsearch and returns the mapping result.
     *
     * @param properties
     *            properties to map
     * @return the mapping outcome and number of mapped properties
     */
    private MappingResult sendMappingRequest(Set<PropertyDefinition> properties)
    {
        try
        {
            ContentModelSynchronizer.IndexMappingResult result = contentModelSynchronizer
                    .initializeElasticsearchIndexMappings(properties);
            if (result.isAcknowledged())
            {
                return new MappingResult(MappingOutcome.MAPPED, result.getSuccessfullyMappedPropertiesCount());
            }
            if (result.getStatus() == HTTP_STATUS_BAD_REQUEST)
            {
                return new MappingResult(MappingOutcome.REJECTED, 0);
            }
            LOGGER.warn("Elasticsearch could not map {} properties, status={}, the batch will be retried whole",
                    properties.size(), result.getStatus());
            return new MappingResult(MappingOutcome.UNAVAILABLE, 0);
        }
        catch (IOException e)
        {
            LOGGER.warn("Elasticsearch mapping request failed for {} properties, the batch will be retried whole",
                    properties.size(), e);
            return new MappingResult(MappingOutcome.UNAVAILABLE, 0);
        }
    }

    private enum MappingOutcome
    {
        MAPPED, REJECTED, UNAVAILABLE
    }

    private record MappingResult(MappingOutcome outcome, int mappedProperties)
    {}

    private record BatchResult(int mappedProperties, boolean elasticsearchUnavailable)
    {}

    private record PendingProperty(QName model, PropertyDefinition property)
    {}

    private Set<PropertyDefinition> getUnmappedProperties(CompiledModel toInit)
    {
        return toInit.getProperties().stream()
                .filter(property -> !mappedPropertyCache.contains(property.getName()))
                .collect(Collectors.toSet());
    }

    public DictionaryDAOImpl getDictionaryDAO()
    {
        return dictionaryDAO;
    }

    public void setDictionaryDAO(DictionaryDAOImpl dictionaryDAO)
    {
        this.dictionaryDAO = dictionaryDAO;
    }

    public ContentModelSynchronizer getContentModelSynchronizer()
    {
        return contentModelSynchronizer;
    }

    public ElasticsearchIndexService getElasticsearchIndexService()
    {
        return elasticsearchIndexService;
    }

    public boolean isCreateIndexIfNotExists()
    {
        return createIndexIfNotExists;
    }

    public int getRetryAttempts()
    {
        return retryAttempts;
    }

    public int getLockRetryAttempts()
    {
        return lockRetryAttempts;
    }

    public int getLockRetryPeriodSeconds()
    {
        return lockRetryPeriodSeconds;
    }

    public JobLockService getJobLockService()
    {
        return jobLockService;
    }

    public void setJobLockService(JobLockService jobLockService)
    {
        this.jobLockService = jobLockService;
    }

    public int getRetryPeriodSeconds()
    {
        return retryPeriodSeconds;
    }

    public void setContentModelSynchronizer(ContentModelSynchronizer contentModelSynchronizer)
    {
        this.contentModelSynchronizer = contentModelSynchronizer;
    }

    public void setElasticsearchIndexService(ElasticsearchIndexService elasticsearchIndexService)
    {
        this.elasticsearchIndexService = elasticsearchIndexService;
    }

    public void setCreateIndexIfNotExists(boolean createIndexIfNotExists)
    {
        this.createIndexIfNotExists = createIndexIfNotExists;
    }

    public void setRetryAttempts(int retryAttempts)
    {
        this.retryAttempts = retryAttempts;
    }

    public void setLockRetryPeriodSeconds(int lockRetryPeriodSeconds)
    {
        this.lockRetryPeriodSeconds = lockRetryPeriodSeconds;
    }

    public void setRetryPeriodSeconds(int retryPeriodSeconds)
    {
        this.retryPeriodSeconds = retryPeriodSeconds;
    }

    public void setLockRetryAttempts(int lockRetryAttempts)
    {
        this.lockRetryAttempts = lockRetryAttempts;
    }

    public void setMappingBatchSize(int mappingBatchSize)
    {
        this.mappingBatchSize = mappingBatchSize;
    }

    public int getMappingBatchSize()
    {
        return mappingBatchSize;
    }

    private class ElasticsearchInitialiserJobLock implements JobLockRefreshCallback
    {
        private final AtomicBoolean runningFlag;

        ElasticsearchInitialiserJobLock()
        {
            this.runningFlag = new AtomicBoolean(true);
        }

        @Override
        public boolean isActive()
        {
            return runningFlag.get();
        }

        @Override
        public void lockReleased()
        {
            runningFlag.set(false);
        }
    }
}
