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

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Locale;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.alfresco.repo.search.impl.elasticsearch.ElasticsearchSpringTest;
import org.alfresco.repo.search.impl.elasticsearch.client.ElasticsearchHttpClientFactory;

/**
 * Integration tests for {@link ElasticsearchIndexService}.
 */
public class ElasticsearchIndexServiceIT extends ElasticsearchSpringTest
{
    private static final String TEST_INDEX_NAME = "alfresco_test_service";
    private ElasticsearchIndexService toTest;
    private ContentModelSynchronizer contentModelSynchronizer;

    @Before
    public void setUp() throws Exception
    {
        ElasticsearchHttpClientFactory elasticsearchHttpClientFactory = (ElasticsearchHttpClientFactory) spy(
                elasticsearchContext.getBean("elasticsearchHttpClientFactory"));
        when(elasticsearchHttpClientFactory.getIndexName()).thenReturn(TEST_INDEX_NAME);
        toTest = new ElasticsearchIndexService(elasticsearchHttpClientFactory, 2000, 10000);
        FieldMappingBuilder fieldMappingBuilder = (FieldMappingBuilder) elasticsearchContext
                .getBean("search.fieldMappingBuilder");
        contentModelSynchronizer = new ContentModelSynchronizer(fieldMappingBuilder, elasticsearchHttpClientFactory, Locale.ENGLISH.getLanguage(), indexConfigurationInitializer);
        deleteIndex(TEST_INDEX_NAME);
    }

    @After
    public void cleanUp()
    {
        deleteIndex(TEST_INDEX_NAME);
    }

    @Test
    public void shouldIndexNotExists()
    {
        assertFalse(toTest.indexExists());
    }

    @Test
    public void shouldIndexExistsWhenCreated() throws IOException
    {
        toTest.createIndex();
        assertTrue(toTest.indexExists());
    }

    @Test
    public void shouldBasicMappingNotExists() throws IOException
    {
        toTest.createIndex();
        assertFalse(toTest.isMappingLoaded());
    }

    @Test
    public void shouldMappingExistsWhenLoaded() throws IOException
    {
        toTest.createIndex();
        contentModelSynchronizer.loadSupportedAnalyzersOnStartup();
        contentModelSynchronizer.loadBasicIndexMappingsOnStartup();
        assertTrue(toTest.isMappingLoaded());
    }

}
