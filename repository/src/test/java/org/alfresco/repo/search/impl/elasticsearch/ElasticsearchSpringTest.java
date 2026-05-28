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
package org.alfresco.repo.search.impl.elasticsearch;

import java.io.IOException;
import java.util.Locale;

import org.junit.Before;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch.indices.DeleteIndexRequest;
import org.opensearch.client.opensearch.indices.ExistsRequest;
import org.springframework.context.ApplicationContext;

import org.alfresco.repo.dictionary.DictionaryDAOImpl;
import org.alfresco.repo.lock.JobLockService;
import org.alfresco.repo.management.subsystems.SwitchableApplicationContextFactory;
import org.alfresco.repo.search.impl.elasticsearch.client.ElasticsearchHttpClientFactory;
import org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.ContentModelSynchronizer;
import org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.ElasticsearchIndexService;
import org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.ElasticsearchInitialiser;
import org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.FieldMappingBuilder;
import org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.IndexConfigurationInitializer;
import org.alfresco.util.BaseSpringTest;

/**
 * Common class to initialize common resources for Elasticsearch tests using an Index.
 */
public abstract class ElasticsearchSpringTest extends BaseSpringTest
{
    protected OpenSearchClient client;
    protected ElasticsearchHttpClientFactory elasticsearchHttpClientFactory;
    protected SwitchableApplicationContextFactory subsystemManager;
    protected ApplicationContext elasticsearchContext;
    protected String indexName;
    protected IndexConfigurationInitializer indexConfigurationInitializer;
    protected ElasticsearchIndexService baseElasticsearchIndexService;
    protected ElasticsearchInitialiser baseElasticsearchInitialiser;

    @Before
    public void buildElasticsearchClient()
    {
        subsystemManager = applicationContext.getBean("Search", SwitchableApplicationContextFactory.class);
        elasticsearchContext = subsystemManager.getApplicationContext();
        elasticsearchHttpClientFactory = elasticsearchContext.getBean("elasticsearchHttpClientFactory", ElasticsearchHttpClientFactory.class);
        indexConfigurationInitializer = elasticsearchContext.getBean("indexConfigurationInitializer", IndexConfigurationInitializer.class);

        DictionaryDAOImpl dictionaryDAOImpl = applicationContext.getBean("dictionaryDAO", DictionaryDAOImpl.class);
        JobLockService jobLockService = applicationContext.getBean("jobLockService", JobLockService.class);
        FieldMappingBuilder fieldMappingBuilder = elasticsearchContext.getBean("search.fieldMappingBuilder", FieldMappingBuilder.class);
        ContentModelSynchronizer contentModelSynchronizer = new ContentModelSynchronizer(
                fieldMappingBuilder, elasticsearchHttpClientFactory, Locale.ENGLISH.getLanguage(), indexConfigurationInitializer);

        client = elasticsearchHttpClientFactory.getElasticsearchClient();
        indexName = elasticsearchHttpClientFactory.getIndexName();
        baseElasticsearchIndexService = new ElasticsearchIndexService(elasticsearchHttpClientFactory, 2000, 10000);

        baseElasticsearchInitialiser = new ElasticsearchInitialiser(dictionaryDAOImpl, baseElasticsearchIndexService, contentModelSynchronizer,
                jobLockService, 1, 1, 1, 1, true);
    }

    /** Get the name of the index. */
    public String getIndex()
    {
        return elasticsearchHttpClientFactory.getIndexName();
    }

    public void deleteIndex(String indexName)
    {
        try
        {
            if (client != null && client.indices().exists(new ExistsRequest.Builder().index(indexName).build()).value())

            {
                client.indices().delete(new DeleteIndexRequest.Builder().index(indexName).build());
            }
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

}
