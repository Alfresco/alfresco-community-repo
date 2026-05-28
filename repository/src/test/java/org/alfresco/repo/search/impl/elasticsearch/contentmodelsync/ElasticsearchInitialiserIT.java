/*-
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

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.atMostOnce;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.alfresco.util.log4j.Log4jAppenderUtil.addAbstractAppenderToLogger;
import static org.alfresco.util.log4j.Log4jAppenderUtil.removeAbstractAppenderFromLogger;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import org.apache.http.HttpStatus;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.message.Message;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.opensearch.client.opensearch._types.OpenSearchException;
import org.opensearch.client.opensearch.indices.CreateIndexRequest;
import org.opensearch.client.opensearch.indices.ExistsRequest;
import org.opensearch.client.opensearch.indices.GetFieldMappingRequest;
import org.opensearch.client.opensearch.indices.GetFieldMappingResponse;
import org.opensearch.client.opensearch.indices.OpenSearchIndicesClient;
import org.opensearch.client.opensearch.indices.PutMappingRequest;

import org.alfresco.repo.dictionary.DictionaryDAOImpl;
import org.alfresco.repo.lock.JobLockService;
import org.alfresco.repo.search.impl.elasticsearch.ElasticsearchSpringTest;
import org.alfresco.repo.search.impl.elasticsearch.client.ElasticsearchHttpClientFactory;
import org.alfresco.repo.search.impl.elasticsearch.util.LogListAppender;

/**
 * Integration tests for {@link ElasticsearchInitialiser}.
 */
public class ElasticsearchInitialiserIT extends ElasticsearchSpringTest
{
    public static final int CONCURRENT_INIT_REPEAT_COUNT = 2;
    private static final String FIELD_REFERENCE = "cm%3Aname";

    @Rule
    public Timeout globalTimeout = new Timeout(70, TimeUnit.SECONDS);

    private ElasticsearchInitialiser toTest;

    private OpenSearchIndicesClient indicesSpy;
    private String testIndexName;

    @Before
    public void setUp() throws Exception
    {
        testIndexName = UUID.randomUUID().toString();

        DictionaryDAOImpl dictionaryDAOImpl = (DictionaryDAOImpl) this.applicationContext.getBean("dictionaryDAO");
        JobLockService jobLockService = (JobLockService) this.applicationContext.getBean("jobLockService");
        ElasticsearchHttpClientFactory elasticsearchHttpClientFactory = (ElasticsearchHttpClientFactory) spy(elasticsearchContext.getBean("elasticsearchHttpClientFactory"));
        when(elasticsearchHttpClientFactory.getIndexName()).thenReturn(getTestIndexName());
        ElasticsearchIndexService elasticSearchIndexServiceSpy = new ElasticsearchIndexService(elasticsearchHttpClientFactory, 2000, 10000);

        FieldMappingBuilder fieldMappingBuilder = (FieldMappingBuilder) elasticsearchContext
                .getBean("search.fieldMappingBuilder");
        ContentModelSynchronizer contentModelSynchronizer = new ContentModelSynchronizer(
                fieldMappingBuilder, elasticsearchHttpClientFactory, Locale.ENGLISH.getLanguage(), indexConfigurationInitializer);

        toTest = new ElasticsearchInitialiser(dictionaryDAOImpl, elasticSearchIndexServiceSpy, contentModelSynchronizer,
                jobLockService, 1, 1, 1, 1, true);
        client = elasticsearchHttpClientFactory.getElasticsearchClient();
        this.indicesSpy = spy(client.indices());
    }

    @After
    public void cleanUp()
    {
        deleteIndex(getTestIndexName());
    }

    @Test
    public void shouldCreateIndex() throws IOException
    {
        ExistsRequest request = new ExistsRequest.Builder().index(getTestIndexName()).build();
        assertFalse(client.indices().exists(request).value());
        toTest.initWithLock();
        assertTrue(client.indices().exists(request).value());
    }

    @Test
    public void shouldCreateIndexAsync() throws IOException, InterruptedException
    {
        ExistsRequest request = new ExistsRequest.Builder().index(getTestIndexName()).build();
        assertFalse(client.indices().exists(request).value());
        Thread thread = toTest.initAsync();
        thread.join();
        assertTrue(client.indices().exists(request).value());
    }

    @Test
    public void shouldCreateIndexOnce_whenInitInvokedMultiple() throws IOException
    {
        // This test will invoke the init method twice in parallel, emulating a multiple initialisation like a clustered system.
        // The lock will avoid multiple index creation.
        assertFalse(indicesSpy.exists(new ExistsRequest.Builder().index(getTestIndexName()).build()).value());

        repeatWithIndexCheck(() -> toTest.initWithLock(), CONCURRENT_INIT_REPEAT_COUNT);

        assertTrue(indicesSpy.exists(new ExistsRequest.Builder().index(getTestIndexName()).build()).value());

        verify(indicesSpy, atMostOnce()).create(any(CreateIndexRequest.class));
    }

    @Test
    public void shouldCreateMultiple_whenInitInvokedByPassingLock() throws IOException
    {
        // The main test purpose is to verify that if we call the inner init method then the threads do not wait for the lock and so the create method is invoked twice.
        repeatWithIndexCheck(() -> toTest.init(), CONCURRENT_INIT_REPEAT_COUNT);
        verify(indicesSpy, atMost(CONCURRENT_INIT_REPEAT_COUNT))
                .create(any(CreateIndexRequest.class));
    }

    /** Check that a field from the standard content model is mapped after dictionary initialisation. */
    @Test
    public void shouldMapModels() throws IOException
    {
        createIndexWithoutMappingModels();
        // Check that the field is not mapped initially.
        GetFieldMappingRequest getFieldMappingsRequest = new GetFieldMappingRequest.Builder().index(
                getTestIndexName()).fields(FIELD_REFERENCE).build();
        GetFieldMappingResponse fieldMapping = client.indices().getFieldMapping(getFieldMappingsRequest);
        assertNull("Unexpectedly found mapping before method called.", fieldMapping.get(testIndexName).mappings().get(FIELD_REFERENCE));

        // Call the method under test.
        toTest.afterDictionaryInit();

        // Check the field is mapped.
        fieldMapping = client.indices().getFieldMapping(getFieldMappingsRequest);
        assertNotNull("Could not find mapping after method called.", fieldMapping.get(testIndexName).mappings().get(FIELD_REFERENCE));
    }

    /** Check that the report log message will be printed once. */
    @Test
    public void shouldLogOnce()
    {
        createIndexWithoutMappingModels();

        LogListAppender logListAppender = LogListAppender.getInstance(Level.INFO);
        Logger elasticsearchLogger = LogManager.getLogger(ElasticsearchInitialiser.class);
        Configurator.setLevel(elasticsearchLogger, Level.INFO);

        addAbstractAppenderToLogger(logListAppender, elasticsearchLogger);

        // Call the method under test for the first time.
        toTest.afterDictionaryInit();

        // Verify that the method logs the success message
        String expectedMessage = "Elasticsearch mappings update completed";
        assertEquals("The info log doesn't contains \"" + expectedMessage + "\"", 1, countLogMessages(logListAppender, expectedMessage));
        logListAppender.clear();

        // Invoke the under test method again and check that they aren't new info log messages
        toTest.afterDictionaryInit();
        assertEquals(0, countLogMessages(logListAppender, expectedMessage));
        removeAbstractAppenderFromLogger(logListAppender, elasticsearchLogger);
    }

    /** Check that model mapping only happens once when two nodes (threads) receive the dictionary event at once. */
    @Test
    public void shouldMapModelsOnce() throws IOException
    {
        createIndexWithoutMappingModels();
        // Check that the field is not mapped initially.
        GetFieldMappingRequest getFieldMappingsRequest = new GetFieldMappingRequest.Builder().index(
                getTestIndexName()).fields(FIELD_REFERENCE).build();
        GetFieldMappingResponse fieldMapping = client.indices().getFieldMapping(getFieldMappingsRequest);
        assertNull("Unexpectedly found mapping before method called.", fieldMapping.get(testIndexName).mappings().get(FIELD_REFERENCE));

        // Call the method under test.
        repeatInParallel(() -> toTest.afterDictionaryInit(), CONCURRENT_INIT_REPEAT_COUNT);

        // Check the field is mapped.
        fieldMapping = client.indices().getFieldMapping(getFieldMappingsRequest);
        assertNotNull("Could not find mapping after method called.", fieldMapping.get(testIndexName).mappings().get(FIELD_REFERENCE));
        // Check it's only mapped once.
        verify(indicesSpy, atMostOnce()).putMapping(any(PutMappingRequest.class));
    }

    /** Attempt to map the models before the index is created. */
    @Test
    public void attemptToMapModelsBeforeInitialisation() throws IOException
    {
        // Call the method under test and check that no exception is thrown.
        toTest.afterDictionaryInit();

        // Check the field is not mapped.
        try
        {
            GetFieldMappingRequest getFieldMappingsRequest = new GetFieldMappingRequest.Builder().index(
                    getTestIndexName()).fields(FIELD_REFERENCE).build();
            client.indices().getFieldMapping(getFieldMappingsRequest);
            fail("Expected exception as index should not exist.");
        }
        catch (OpenSearchException e)
        {
            assertEquals("Expected NOT FOUND status code to be returned.", HttpStatus.SC_NOT_FOUND, e.status());
        }
    }

    /** Create the index and check the models are also mapped. */
    @Test
    public void initialisationMapsModels() throws IOException
    {
        // Call the method under test and check that no exception is thrown.
        toTest.initWithLock();

        // Check the field is mapped.
        GetFieldMappingRequest getFieldMappingsRequest = new GetFieldMappingRequest.Builder().index(
                getTestIndexName()).fields(FIELD_REFERENCE).build();
        GetFieldMappingResponse fieldMapping = client.indices().getFieldMapping(getFieldMappingsRequest);
        assertNotNull("Could not find mapping after method called.", fieldMapping.get(testIndexName).mappings().get(FIELD_REFERENCE));
    }

    /** Calls to init (due to subsystem creation) and mapModels (due to dictionary creation) only maps models once. */
    @Test
    public void modelsOnlyMappedOnceAtStartUp() throws IOException
    {

        toTest.initWithLock();
        toTest.afterDictionaryInit();

        // Check the field is mapped.
        GetFieldMappingRequest getFieldMappingsRequest = new GetFieldMappingRequest.Builder().index(
                getTestIndexName()).fields(FIELD_REFERENCE).build();
        GetFieldMappingResponse fieldMapping = client.indices().getFieldMapping(getFieldMappingsRequest);
        assertNotNull("Could not find mapping after method called.", fieldMapping.get(testIndexName).mappings().get(FIELD_REFERENCE));
        // Check it's only mapped once.
        verify(indicesSpy, atMostOnce()).putMapping(any(PutMappingRequest.class));
    }

    /** Check that the test index doesn't exist and then call the given method in parallel. */
    private void repeatWithIndexCheck(Runnable initMethod, int repeat) throws IOException
    {
        ExistsRequest request = new ExistsRequest.Builder().index(getTestIndexName()).build();
        assertFalse(indicesSpy.exists(request).value());

        repeatInParallel(initMethod, repeat);
    }

    /** Repeat the given method in parallel. */
    private void repeatInParallel(Runnable initMethod, int repeat)
    {
        List<Thread> threads = IntStream.range(0, repeat)
                .mapToObj(i -> new Thread(initMethod))
                .peek(Thread::start)
                .toList();

        threads.forEach(t -> {
            try
            {
                t.join();
            }
            catch (InterruptedException e)
            {
                throw new RuntimeException(e);
            }
        });
    }

    /** Create the index, load the analysers and do the basic mappings. Don't do the model mappings though. */
    private void createIndexWithoutMappingModels()
    {
        // Create the test index but don't map the models.
        toTest.setRetryAttempts(0);
        toTest.init();
        // Re-enable model mapping.
        toTest.setRetryAttempts(3);
    }

    private String getTestIndexName()
    {
        return testIndexName;
    }

    private long countLogMessages(LogListAppender logListAppender, String expectedMessage)
    {
        return logListAppender
                .getLogMessages()
                .stream()
                .map(LogEvent::getMessage)
                .map(Message::getFormattedMessage)
                .map(Object::toString)
                .filter(m -> m.contains(expectedMessage))
                .count();
    }

}
