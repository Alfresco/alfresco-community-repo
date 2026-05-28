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
package org.alfresco.repo.search.impl.elasticsearch.query.language.afts;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Locale;
import jakarta.json.Json;
import jakarta.json.JsonReader;

import io.netty.handler.codec.http.HttpMethod;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opensearch.client.opensearch.generic.Body;
import org.opensearch.client.opensearch.generic.Requests;

import org.alfresco.repo.search.impl.elasticsearch.ElasticsearchSpringTest;
import org.alfresco.repo.search.impl.elasticsearch.client.ElasticsearchHttpClientFactory;
import org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.ContentModelSynchronizer;
import org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.ElasticsearchIndexService;
import org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.FieldMappingBuilder;
import org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.field.property.ElasticsearchAnalyzer;

public class IndexLocaleAnalyzerIT extends ElasticsearchSpringTest
{

    private static final String ENGLISH_ANALYZER_TYPE = "english";
    private static final String GERMAN_ANALYZER_TYPE = "german";
    private static final String DEFAULT_ANALYZER_TYPE = ENGLISH_ANALYZER_TYPE;

    private static final String ANALYZER_TYPE_SETTINGS_PATH = "index.analysis.analyzer." + ElasticsearchAnalyzer.DEFAULT_ANALYZER.getName() + ".type";

    private static final String TEST_INDEX_NAME = "index_locale";
    private ElasticsearchHttpClientFactory elasticHttpClientFactory;
    private ContentModelSynchronizer toTest;
    private FieldMappingBuilder fieldMappingBuilder;
    private ElasticsearchIndexService elasticsearchIndexService;

    @Before
    public void setUp() throws Exception
    {
        elasticHttpClientFactory = (ElasticsearchHttpClientFactory) spy(
                elasticsearchContext.getBean("elasticsearchHttpClientFactory"));
        when(elasticHttpClientFactory.getIndexName()).thenReturn(TEST_INDEX_NAME);

        elasticsearchIndexService = new ElasticsearchIndexService(elasticHttpClientFactory, 2000, 10000);
        fieldMappingBuilder = (FieldMappingBuilder) elasticsearchContext.getBean("search.fieldMappingBuilder");
    }

    @After
    public void cleanUp()
    {
        deleteIndex(TEST_INDEX_NAME);
    }

    @Test
    public void testIfDefaultEnglishAnalyzerIsUsedWhenNullLocaleProvided() throws IOException
    {
        elasticsearchIndexService.createIndex();
        toTest = new ContentModelSynchronizer(fieldMappingBuilder, elasticHttpClientFactory, null, indexConfigurationInitializer);

        toTest.loadSupportedAnalyzersOnStartup();
        toTest.loadBasicIndexMappingsOnStartup();

        assertAnalyzerTypeUsedInSettings(DEFAULT_ANALYZER_TYPE);
    }

    @Test
    public void testIfDefaultEnglishAnalyzerIsUsedWhenEmptyLocaleProvided() throws IOException
    {
        elasticsearchIndexService.createIndex();
        toTest = new ContentModelSynchronizer(fieldMappingBuilder, elasticHttpClientFactory, "", indexConfigurationInitializer);

        toTest.loadSupportedAnalyzersOnStartup();
        toTest.loadBasicIndexMappingsOnStartup();

        assertAnalyzerTypeUsedInSettings(DEFAULT_ANALYZER_TYPE);
    }

    @Test
    public void testIfGermanAnalyzerIsUsed() throws IOException
    {
        elasticsearchIndexService.createIndex();
        toTest = new ContentModelSynchronizer(fieldMappingBuilder, elasticHttpClientFactory, Locale.GERMAN.getLanguage(), indexConfigurationInitializer);

        toTest.loadSupportedAnalyzersOnStartup();
        toTest.loadBasicIndexMappingsOnStartup();

        assertAnalyzerTypeUsedInSettings(GERMAN_ANALYZER_TYPE);
    }

    @Test
    public void testIfEnglishAnalyzerIsUsed() throws IOException
    {
        elasticsearchIndexService.createIndex();
        toTest = new ContentModelSynchronizer(fieldMappingBuilder, elasticHttpClientFactory, Locale.ENGLISH.getLanguage(), indexConfigurationInitializer);

        toTest.loadSupportedAnalyzersOnStartup();
        toTest.loadBasicIndexMappingsOnStartup();

        assertAnalyzerTypeUsedInSettings(ENGLISH_ANALYZER_TYPE);
    }

    private void assertAnalyzerTypeUsedInSettings(String expectedAnalyzerType) throws IOException
    {

        var request = Requests.builder()
                .endpoint("/" + TEST_INDEX_NAME + "/_settings/" + ANALYZER_TYPE_SETTINGS_PATH)
                .method(HttpMethod.GET.name())
                .build();
        var response = client.generic().execute(request);
        String jsonResponse = response.getBody()
                .map(Body::body)
                .map(Json::createReader)
                .map(JsonReader::readObject)
                .map(jsonObject -> jsonObject.toString()).orElse("null");

        JSONObject jsonObject = new JSONObject(jsonResponse);
        JSONObject settings = (JSONObject) jsonObject.get("index_locale");
        JSONObject index = (JSONObject) settings.get("settings");
        JSONObject analysis = (JSONObject) index.get("index");
        JSONObject analyzer = (JSONObject) analysis.get("analysis");
        JSONObject localeContent = (JSONObject) analyzer.get("analyzer");
        JSONObject type = (JSONObject) localeContent.get("locale_content");

        String actualCustomAnalyzerType = (String) type.get("type");
        assertEquals(actualCustomAnalyzerType, expectedAnalyzerType);
    }
}
