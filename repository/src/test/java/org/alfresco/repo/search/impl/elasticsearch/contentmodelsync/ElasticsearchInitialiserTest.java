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

import static java.lang.Integer.MAX_VALUE;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

import static org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.ElasticsearchInitialiser.LOCK_QNAME;
import static org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.ElasticsearchInitialiser.LOCK_TTL;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import org.alfresco.repo.dictionary.CompiledModel;
import org.alfresco.repo.dictionary.DictionaryDAOImpl;
import org.alfresco.repo.lock.JobLockService;
import org.alfresco.repo.lock.LockAcquisitionException;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.GUID;

/**
 * Unit tests for {@link ElasticsearchInitialiser}.
 */
public class ElasticsearchInitialiserTest
{

    private static final int RETRY_ATTEMPTS = 2;
    private static final int MAX_TIMEOUT_MS = 10000;

    @Rule
    public Timeout globalTimeout = new Timeout(MAX_TIMEOUT_MS, MILLISECONDS);

    /**
     * The class under test.
     */
    @Mock
    private ElasticsearchIndexService mockElasticSearchIndexService;
    @Mock
    private ContentModelSynchronizer mockContentModelSynchronizer;
    @Mock
    private JobLockService jobLockService;
    @Mock
    private DictionaryDAOImpl mockDictionary;
    @Mock
    private QName mockModel;
    @Mock
    private CompiledModel mockCompiledModel;
    @Mock
    private ContentModelSynchronizer.IndexMappingResult indexMappingResult;

    @InjectMocks
    private ElasticsearchInitialiser toTest;

    @Before
    public void setUp() throws IOException
    {
        openMocks(this);
        toTest.setLockRetryAttempts(RETRY_ATTEMPTS);
        toTest.setRetryAttempts(RETRY_ATTEMPTS);
    }

    /**
     * Check that we load the basic mappings but not the analysers if the index already exists and creation is turned off.
     */
    @Test
    public void testUseExistingIndexWhenCreationTurnedOff()
    {
        toTest.setCreateIndexIfNotExists(false);
        when(mockElasticSearchIndexService.indexExists()).thenReturn(true);
        when(mockElasticSearchIndexService.isMappingLoaded()).thenReturn(false);
        when(mockContentModelSynchronizer.loadBasicIndexMappingsOnStartup()).thenReturn(true);

        toTest.init();

        // Check the mappings are loaded but the analysers are assumed to already exist.
        verify(mockContentModelSynchronizer, never()).loadSupportedAnalyzersOnStartup();
        verify(mockElasticSearchIndexService, never()).createIndex();
        verify(mockContentModelSynchronizer).loadBasicIndexMappingsOnStartup();
    }

    /**
     * Check that we wait for the index to exist if creation is turned off.
     */
    @Test
    public void testWaitOnMissingIndexWhenCreationTurnedOff() throws IOException
    {
        toTest.setCreateIndexIfNotExists(false);
        // Pretend that the index doesn't exist for the first two tries, and then pretend it is created.
        when(mockElasticSearchIndexService.indexExists()).thenReturn(false, false, true);
        when(mockElasticSearchIndexService.isMappingLoaded()).thenReturn(false);
        when(mockContentModelSynchronizer.loadBasicIndexMappingsOnStartup()).thenReturn(true);

        toTest.init();

        // Check the mappings are loaded but the analysers are assumed to already exist.
        verify(mockContentModelSynchronizer, never()).loadSupportedAnalyzersOnStartup();
        verify(mockContentModelSynchronizer).loadBasicIndexMappingsOnStartup();
    }

    /**
     * Check that we create the index if it doesn't exist and creation is turned on.
     */
    @Test
    public void testSuccessfullyCreatingIndex()
    {
        toTest.setCreateIndexIfNotExists(true);
        // The first check is before creation, the second check is afterwards.
        when(mockElasticSearchIndexService.indexExists()).thenReturn(false, true);
        when(mockElasticSearchIndexService.createIndex()).thenReturn(true);
        when(mockContentModelSynchronizer.loadSupportedAnalyzersOnStartup()).thenReturn(true);
        when(mockContentModelSynchronizer.loadBasicIndexMappingsOnStartup()).thenReturn(true);

        toTest.init();

        // Check the index is created and the analysers and basic mappings are loaded.
        verify(mockElasticSearchIndexService).createIndex();
        verify(mockContentModelSynchronizer).loadSupportedAnalyzersOnStartup();
        verify(mockContentModelSynchronizer).loadBasicIndexMappingsOnStartup();
    }

    /**
     * Check that if the index already exists and creation is turned on then we still try to load the analysers and mappings.
     */
    @Test
    public void testIndexAlreadyExistsWithCreationTurnedOn()
    {
        toTest.setCreateIndexIfNotExists(true);
        when(mockElasticSearchIndexService.indexExists()).thenReturn(true);
        when(mockContentModelSynchronizer.loadSupportedAnalyzersOnStartup()).thenReturn(true);
        when(mockContentModelSynchronizer.loadBasicIndexMappingsOnStartup()).thenReturn(true);

        toTest.init();

        // Check the index is not recreated.
        verify(mockElasticSearchIndexService, never()).createIndex();
        verify(mockContentModelSynchronizer).loadSupportedAnalyzersOnStartup();
        verify(mockContentModelSynchronizer).loadBasicIndexMappingsOnStartup();
    }

    @Test
    public void shouldSkipBasiFieldsMappingLoadingWhenAlreadyExist()
    {
        toTest.setCreateIndexIfNotExists(true);
        when(mockElasticSearchIndexService.indexExists()).thenReturn(true);
        when(mockElasticSearchIndexService.isMappingLoaded()).thenReturn(true);

        toTest.init();

        verify(mockContentModelSynchronizer, never()).loadBasicIndexMappingsOnStartup();
    }

    @Test(expected = RuntimeException.class)
    public void shouldReleaseLockOnException()
    {
        toTest.setCreateIndexIfNotExists(true);
        toTest.setLockRetryAttempts(3);
        String guid = GUID.generate();
        when(jobLockService.getLock(eq(LOCK_QNAME), eq(LOCK_TTL), any(JobLockService.JobLockRefreshCallback.class)))
                .thenReturn(guid);
        when(mockElasticSearchIndexService.createIndex())
                .thenThrow(new RuntimeException("Test exception"));

        toTest.initWithLock();

        verify(jobLockService, times(1)).releaseLock(eq(guid), eq(LOCK_QNAME));
    }

    @Test
    public void shouldReleaseLock()
    {
        toTest.setCreateIndexIfNotExists(true);
        toTest.setLockRetryAttempts(3);
        String guid = GUID.generate();
        when(jobLockService.getLock(eq(LOCK_QNAME), eq(LOCK_TTL), any(JobLockService.JobLockRefreshCallback.class)))
                .thenReturn(guid);

        when(mockElasticSearchIndexService.indexExists()).thenReturn(false, true);
        when(mockElasticSearchIndexService.createIndex()).thenReturn(true);
        when(mockContentModelSynchronizer.loadSupportedAnalyzersOnStartup()).thenReturn(true);
        when(mockContentModelSynchronizer.loadBasicIndexMappingsOnStartup()).thenReturn(true);

        toTest.initWithLock();

        verify(jobLockService, times(1)).releaseLock(eq(guid), eq(LOCK_QNAME));
    }

    @Test
    public void shouldSkipSecondInitWhenALockItIsActive() throws InterruptedException
    {
        toTest.setCreateIndexIfNotExists(true);
        toTest.setLockRetryAttempts(3);
        String guid = GUID.generate();
        String secondLockGuid = GUID.generate();
        when(jobLockService.getLock(eq(LOCK_QNAME), eq(LOCK_TTL), any(JobLockService.JobLockRefreshCallback.class)))
                .thenReturn(guid)
                .thenThrow(LockAcquisitionException.class)
                .thenReturn(secondLockGuid);

        when(mockElasticSearchIndexService.indexExists()).thenReturn(false, true);
        when(mockElasticSearchIndexService.createIndex()).thenReturn(true);
        when(mockContentModelSynchronizer.loadSupportedAnalyzersOnStartup()).thenReturn(true);
        when(mockContentModelSynchronizer.loadBasicIndexMappingsOnStartup()).thenReturn(true);
        when(mockElasticSearchIndexService.isMappingLoaded()).thenReturn(false, true);

        Thread thread1 = new Thread(() -> toTest.initWithLock());
        thread1.start();
        Thread thread2 = new Thread(() -> toTest.initWithLock());
        thread2.start();
        thread1.join();
        thread2.join();

        verify(jobLockService, times(1)).releaseLock(eq(guid), eq(LOCK_QNAME));
        verify(jobLockService, times(1)).releaseLock(eq(secondLockGuid), eq(LOCK_QNAME));
    }

    /**
     * Verifies the ElasticsearchInitialiser::stop method during infinite loop in init.
     */
    @Test
    public void shouldStopInitialisationDuringInit() throws InterruptedException
    {
        toTest.setCreateIndexIfNotExists(true);
        doReturn(GUID.generate()).when(jobLockService).getLock(eq(LOCK_QNAME), eq(LOCK_TTL), any(JobLockService.JobLockRefreshCallback.class));
        // simulates infinite loop
        doReturn(false).when(mockElasticSearchIndexService).indexExists();

        var thread = toTest.initAsync();
        try
        {
            verify(mockElasticSearchIndexService, timeout(MAX_TIMEOUT_MS).atLeast(1)).createIndex();
            assertThat(thread.isAlive()).isTrue();

            // when
            toTest.stop();

            // then thread should terminate
            thread.join(MAX_TIMEOUT_MS);
            assertThat(thread.isAlive()).isFalse();
        }
        finally
        {
            thread.interrupt();
        }
    }

    /**
     * Verifies the ElasticsearchInitialiser::stop method during loop in lock retries.
     */
    @Test
    public void shouldStopInitialisationDuringLock() throws InterruptedException
    {
        // the big value of lockRetryAttempts is used to keep thread in desired state
        var lockRetryAttempts = MAX_VALUE;
        toTest.setLockRetryAttempts(lockRetryAttempts);
        doThrow(LockAcquisitionException.class).when(jobLockService).getLock(eq(LOCK_QNAME), eq(LOCK_TTL), any(JobLockService.JobLockRefreshCallback.class));

        var thread = toTest.initAsync();
        try
        {
            verify(jobLockService, timeout(MAX_TIMEOUT_MS).atLeast(1))
                    .getLock(eq(LOCK_QNAME), eq(LOCK_TTL), any(JobLockService.JobLockRefreshCallback.class));
            assertThat(thread.isAlive()).isTrue();

            // when
            toTest.stop();

            // then wait for thread termination
            thread.join(MAX_TIMEOUT_MS);
            assertThat(thread.isAlive()).isFalse();
            verify(jobLockService, atMost(lockRetryAttempts - 1))
                    .getLock(eq(LOCK_QNAME), eq(LOCK_TTL), any(JobLockService.JobLockRefreshCallback.class));
        }
        finally
        {
            thread.interrupt();
        }
    }

    @Test
    public void shouldDoSecondInitWhenFirstFailsBecauseOfLockAcquisitionException()
            throws InterruptedException
    {
        String guid = GUID.generate();
        when(jobLockService.getLock(eq(LOCK_QNAME), eq(LOCK_TTL), any(JobLockService.JobLockRefreshCallback.class)))
                .thenReturn(guid) // first lock
                .thenThrow(new LockAcquisitionException(LOCK_QNAME, null)) // second lock fails
                .thenReturn(guid); // second lock will have success because of the exception thrown during the first lock

        // mocked ContentModelSynchronizer and ElasticsearchIndexService to use in the second ElasticsearchInitializer
        // A second instance of these mocked classes have been created because they are used in two elasticsearchInitializers
        // that run asynchronously. For this reason, it is not predictable the call sequences in case a single
        // mocked class is used.
        ContentModelSynchronizer secondContentModelSynchronizer = mock(ContentModelSynchronizer.class);
        ElasticsearchIndexService secondElasticsearchIndexService = mock(ElasticsearchIndexService.class);
        when(secondContentModelSynchronizer.loadSupportedAnalyzersOnStartup()).thenReturn(true);
        when(secondContentModelSynchronizer.loadBasicIndexMappingsOnStartup()).thenReturn(true);

        when(mockElasticSearchIndexService.indexExists()).thenReturn(false);
        when(mockElasticSearchIndexService.createIndex()).thenThrow(new RuntimeException("Test exception"));

        when(secondElasticsearchIndexService.indexExists()).thenReturn(true);
        when(secondElasticsearchIndexService.createIndex()).thenReturn(true);

        ElasticsearchInitialiser elasticsearchInitialiser = new ElasticsearchInitialiser(mockDictionary,
                mockElasticSearchIndexService, mockContentModelSynchronizer, jobLockService, 0, 0, 3, 0, true);

        ElasticsearchInitialiser secondElasticsearchInitialiser = new ElasticsearchInitialiser(mockDictionary,
                secondElasticsearchIndexService, secondContentModelSynchronizer, jobLockService, 0, 0, 3, 0, true);

        Thread thread1 = new Thread(() -> elasticsearchInitialiser.initWithLock());
        thread1.start();
        Thread thread2 = new Thread(() -> secondElasticsearchInitialiser.initWithLock());
        thread2.start();
        thread1.join();
        thread2.join();

        // Both initialiser will release the lock, the first because of the exception and the second one because it will complete the operation.
        verify(jobLockService, atMost(2)).releaseLock(eq(guid), eq(LOCK_QNAME));
        verify(this.mockContentModelSynchronizer, never()).loadSupportedAnalyzersOnStartup();
        verify(this.mockContentModelSynchronizer, never()).loadBasicIndexMappingsOnStartup();
        verify(secondContentModelSynchronizer, times(1)).loadSupportedAnalyzersOnStartup();
        verify(secondContentModelSynchronizer, times(1)).loadBasicIndexMappingsOnStartup();
    }

    @Test
    public void onModelUpdate_failure_shouldRetryNTimes() throws IOException
    {
        int retryAttempts = 3;
        toTest.setCreateIndexIfNotExists(true);
        toTest.setRetryAttempts(retryAttempts);
        toTest.setRetryPeriodSeconds(0);
        toTest.setLockRetryAttempts(3);
        when(mockElasticSearchIndexService.indexExists()).thenReturn(true);

        Collection<QName> models = Arrays.asList(mockModel);
        when(mockDictionary.getModels(true)).thenReturn(models);
        when(mockDictionary.getCompiledModel(any(QName.class))).thenReturn(mockCompiledModel);
        when(mockContentModelSynchronizer.initializeElasticsearchIndexMappings(anyCollection())).thenThrow(new IOException());

        toTest.afterDictionaryInit();

        verify(mockContentModelSynchronizer, times(retryAttempts)).initializeElasticsearchIndexMappings(anyCollection());
    }

    @Test
    public void onModelUpdate_indexNotExisting_shouldNotUpdate() throws IOException
    {
        int retryAttempts = 3;
        toTest.setCreateIndexIfNotExists(true);
        toTest.setRetryAttempts(retryAttempts);
        toTest.setRetryPeriodSeconds(0);

        when(mockElasticSearchIndexService.indexExists()).thenReturn(false);

        toTest.afterDictionaryInit();

        verify(mockContentModelSynchronizer, never()).initializeElasticsearchIndexMappings(anyCollection());
    }

    @Test
    public void onModelUpdate_onceIndexIsCreated_shouldUpdate() throws IOException
    {
        int retryAttempts = 3;
        toTest.setCreateIndexIfNotExists(true);
        toTest.setRetryAttempts(retryAttempts);
        toTest.setLockRetryAttempts(3);
        when(mockElasticSearchIndexService.indexExists()).thenReturn(false, false, true);
        Collection<QName> models = Arrays.asList(mockModel);
        when(mockDictionary.getModels(true)).thenReturn(models);
        when(mockContentModelSynchronizer.initializeElasticsearchIndexMappings(anyCollection())).thenReturn(indexMappingResult);
        when(mockDictionary.getCompiledModel(any(QName.class))).thenReturn(mockCompiledModel);

        toTest.afterDictionaryInit();

        verify(mockContentModelSynchronizer, times(1)).initializeElasticsearchIndexMappings(anyCollection());
    }

}
