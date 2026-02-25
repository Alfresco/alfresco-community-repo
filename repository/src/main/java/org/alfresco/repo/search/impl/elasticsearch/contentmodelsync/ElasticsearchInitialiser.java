/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2025 Alfresco Software Limited
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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.alfresco.repo.dictionary.CompiledModel;
import org.alfresco.repo.dictionary.DictionaryDAOImpl;
import org.alfresco.repo.dictionary.DictionaryListener;
import org.alfresco.repo.lock.JobLockService;
import org.alfresco.repo.lock.JobLockService.JobLockRefreshCallback;
import org.alfresco.repo.lock.LockAcquisitionException;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * Class responsible for initialising Elasticsearch when the subsystem is started.
 */
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

    // The map model method will be invoked multiple times, in order to avoid to map the same property twice we use a cache
    private final Set<QName> modelCache = new HashSet<>();
    // This counter will be used during the map model execution
    private final AtomicInteger globalModelInitialisedCounter = new AtomicInteger(0);

    public ElasticsearchInitialiser(DictionaryDAOImpl dictionary, ElasticsearchIndexService elasticsearchIndexService,
            ContentModelSynchronizer contentModelSynchronizer, JobLockService jobLockService, int retryAttempts, int retryPeriodSeconds,
            int lockRetryAttempts, int lockRetryPeriodSeconds, boolean createIndexIfNotExists)
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
    }

    public ElasticsearchInitialiser()
    {}

    /**
     * Stop the index initialization. This method is required when the Spring context is reloaded in order to stop the asynchronous initialization.
     */
    public void stop()
    {
        LOGGER.debug("Elasticsearch index initialising stopped");
        isTerminated.set(true);
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
                            "Elasticsearch mappings update completed. {} out of {} types were mapped successfully. "
                                    + "Turn on DEBUG for elasticsearch.contentmodelsync.FieldMappingBuilder and restart "
                                    + "the server for more information",
                            globalModelInitialisedCounter.get(), modelCache.size());
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
        Collection<QName> modelsToInit = dictionaryDAO.getModels(true);
        int currentPropertiesInitialisedCounter = 0;
        while (!modelsToInit.isEmpty() && attemptsRemaining > 0)
        {
            attemptsRemaining--;
            List<QName> failedModels = new LinkedList<>();
            LOGGER.trace("Elasticsearch Field Mapping update started");
            if (elasticsearchIndexService.indexExists())
            {
                for (QName model : modelsToInit)
                {
                    CompiledModel toInit = dictionaryDAO.getCompiledModel(model);
                    try
                    {
                        ContentModelSynchronizer.IndexMappingResult modelInitialised = contentModelSynchronizer
                                .initializeElasticsearchIndexMappings(getNotCachedModels(toInit));

                        if (modelInitialised.isAcknowledged())
                        {
                            currentPropertiesInitialisedCounter += modelInitialised.getSuccessfullyMappedPropertiesCount();
                            cacheInitialisedModels(toInit);
                        }
                        else
                        {
                            failedModels.add(model);
                        }
                    }
                    catch (IOException e)
                    {
                        failedModels.add(model);
                        LOGGER.warn("Elasticsearch is not responding, {} model, {} attempts left", model.toString(),
                                attemptsRemaining, e);
                    }
                }
                modelsToInit = failedModels;
            }
            else
            {
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
            LOGGER.error("Elasticsearch mappings update failed for models: " + Arrays.toString(modelsToInit.toArray()));
            return false;
        }
    }

    private void cacheInitialisedModels(CompiledModel toInit)
    {
        modelCache.addAll(extractQNames(toInit.getProperties()));
    }

    private Set<PropertyDefinition> getNotCachedModels(CompiledModel toInit)
    {
        return toInit.getProperties().stream().filter(Predicate.not(o -> modelCache.contains(o.getName())))
                .collect(Collectors.toSet());
    }

    private Set<QName> extractQNames(Collection<PropertyDefinition> properties)
    {
        return properties.stream().map(PropertyDefinition::getName).collect(Collectors.toSet());
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
