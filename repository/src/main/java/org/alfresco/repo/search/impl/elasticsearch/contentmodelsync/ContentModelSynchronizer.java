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
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import jakarta.json.Json;
import jakarta.json.JsonReader;

import org.json.JSONObject;
import org.opensearch.client.opensearch._types.OpenSearchException;
import org.opensearch.client.opensearch.generic.Body;
import org.opensearch.client.opensearch.generic.Request;
import org.opensearch.client.opensearch.generic.Requests;
import org.opensearch.client.opensearch.generic.Response;
import org.opensearch.client.opensearch.indices.CloseIndexRequest;
import org.opensearch.client.opensearch.indices.OpenRequest;
import org.opensearch.client.opensearch.indices.OpenResponse;
import org.opensearch.client.opensearch.indices.OpenSearchIndicesClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.util.StreamUtils;

import org.alfresco.repo.search.impl.elasticsearch.client.ElasticsearchHttpClientFactory;
import org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.utils.ResourceUtils;
import org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.utils.SettingsJsonUtils;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.util.Pair;

/**
 * The entry point for the Content Model synchronization
 */
public class ContentModelSynchronizer
{

    private static final Logger LOGGER = LoggerFactory.getLogger(ContentModelSynchronizer.class);

    static final String CONFIG_PATH = "/alfresco/search/elasticsearch/config/";
    static final String LOCALE_PATH = "/alfresco/search/elasticsearch/config/locale/";
    static final List<String> MANDATORY_ANALYZERS_SETTINGS_ATTRIBUTES = List.of(
            "settings", "analysis", "analyzer", "filter", "locale_content", "locale_text_index", "locale_text_query", "path_emulator", "locale_cross_text_index", "locale_cross_text_query");
    static final boolean PRESERVE_EXISTING = true;
    private static final String SUPPORTED_ANALYZERS_FILE = "supportedAnalyzers.json";

    private final FieldMappingBuilder elasticsearchFieldBuilder;
    private final ElasticsearchHttpClientFactory httpClientFactory;
    private final String indexLocale;
    private final IndexConfigurationInitializer indexConfigurationInitializer;

    private Resource[] customAnalyzerConfigFiles;

    public ContentModelSynchronizer(FieldMappingBuilder elasticsearchFieldBuilder, ElasticsearchHttpClientFactory clientFactory, String indexLocale, IndexConfigurationInitializer indexConfigurationInitializer)
    {
        this.elasticsearchFieldBuilder = elasticsearchFieldBuilder;
        this.httpClientFactory = clientFactory;
        this.indexLocale = indexLocale;
        this.indexConfigurationInitializer = indexConfigurationInitializer;
    }

    /**
     * Loads supported analyzers on startup if they do not exist already.
     * 
     * @return true on success false if the configured settings are invalid in case there is a problem sending the request or parsing back the response log IOException
     */
    public boolean loadSupportedAnalyzersOnStartup()
    {
        try
        {
            JSONObject jsonWithAnalyzersSettings = readJSONsWithSettingsFromFiles();
            SettingsJsonUtils.validate(jsonWithAnalyzersSettings, MANDATORY_ANALYZERS_SETTINGS_ATTRIBUTES);

            boolean success = areSupportedAnalyzersLoaded() || updateSettings(jsonWithAnalyzersSettings, !PRESERVE_EXISTING);
            if (success)
            {
                logSuccess();
            }
            else
            {
                LOGGER.error("Attempt to load analysers failed.");
            }
            return success;
        }
        catch (IOException e)
        {
            LOGGER.error("Failed to load analysers", e);
            return false;
        }
    }

    /**
     * Check if the given index has the supported mandatory analyzers loaded.
     *
     * @return true if the mandatory analyzers are loaded.
     */
    public boolean areSupportedAnalyzersLoaded()
    {
        String indexName = httpClientFactory.getIndexName();

        Request request = Requests.builder().method("GET")
                .endpoint("/" + indexName + "/_settings/*analyzer*").build();

        try (var response = httpClientFactory.getElasticsearchClient().generic().execute(request))
        {
            var existingSettings = response.getBody()
                    .map(Body::body)
                    .map(Json::createReader)
                    .map(JsonReader::readObject)
                    .map(JSONObject::new).orElse(null);

            boolean success = existingSettings != null;

            for (String mandatoryAttribute : MANDATORY_ANALYZERS_SETTINGS_ATTRIBUTES)
            {
                if (!SettingsJsonUtils.keyExists(existingSettings, mandatoryAttribute))
                {
                    success = false;
                }
            }

            LOGGER.info("Supported analyzers already loaded: {}", success);
            return success;
        }
        catch (OpenSearchException | IOException e)
        {
            LOGGER.error("Failed to check if supported analyzers for given index {} are loaded.", indexName, e);
            return false;
        }
    }

    private JSONObject readJSONsWithSettingsFromFiles() throws IOException
    {
        JSONObject localeCrossAnalyzersSettings = ResourceUtils.readJSONFromFile(CONFIG_PATH + SUPPORTED_ANALYZERS_FILE, getClass());
        JSONObject localeAnalyzersSettings = ResourceUtils.readJSONFromFile(LOCALE_PATH + determineLocaleAnalyzerFile(), getClass());
        JSONObject customAnalyzersSettings = SettingsJsonUtils.deepMergeMultipleJSONs(ResourceUtils.readJSONsFromResources(customAnalyzerConfigFiles));
        return SettingsJsonUtils.deepMergeMultipleJSONs(localeCrossAnalyzersSettings, localeAnalyzersSettings, customAnalyzersSettings);
    }

    private void logSuccess()
    {
        LOGGER.info("Successfully loaded analysers.");
        LOGGER.info("Locale analyzer was loaded from file: {}", determineLocaleAnalyzerFile());
        if (customAnalyzerConfigFiles != null)
        {
            for (Resource resource : customAnalyzerConfigFiles)
            {
                if (resource.isReadable())
                {
                    LOGGER.info("Custom analyzer was loaded from file: {}", resource.getFilename());
                }
            }
        }
    }

    boolean updateSettings(JSONObject settings, boolean preserveExisting) throws IOException
    {
        String indexName = httpClientFactory.getIndexName();

        Request requests = Requests.builder()
                .method("PUT")
                .endpoint("/" + indexName + "/_settings")
                .json(settings.toString())
                .query(Collections.singletonMap("preserve_existing", String.valueOf(preserveExisting)))
                .build();

        OpenSearchIndicesClient indices = httpClientFactory.getElasticsearchClient()
                .indices();
        indices.close(new CloseIndexRequest.Builder().index(indexName)
                .build());
        Response response = httpClientFactory.getElasticsearchClient()
                .generic()
                .execute(requests);

        OpenResponse openResponse = indices.open(new OpenRequest.Builder().index(indexName)
                .build());

        return response.getStatus() == 200 && openResponse.acknowledged();
    }

    private String determineLocaleAnalyzerFile()
    {
        if (indexLocale == null || indexLocale.isEmpty())
        {
            return "en_locale.json";
        }
        return indexLocale + "_locale.json";
    }

    public boolean loadBasicIndexMappingsOnStartup()
    {
        try (InputStream basicFieldsInputStream = indexConfigurationInitializer.loadConfigurationAsInputStream())
        {
            String jsonInput = StreamUtils.copyToString(basicFieldsInputStream, StandardCharsets.UTF_8);
            JSONObject jsonObject = new JSONObject(jsonInput);
            String indexName = httpClientFactory.getIndexName();

            Request requests = Requests.builder().method("PUT")
                    .endpoint("/" + indexName + "/_mapping").json(jsonObject.toString()).build();

            try (Response response = httpClientFactory.getElasticsearchClient().generic().execute(requests))
            {
                boolean success = response.getStatus() == 200;
                if (success)
                {
                    LOGGER.info("Successfully loaded basic mappings.");
                }
                else
                {
                    LOGGER.error("Attempt to load basic mappings failed.");
                }
                return success;
            }
        }
        catch (IOException e)
        {
            LOGGER.error("Failed to load basic mappings.", e);
            return false;
        }
    }

    /**
     * @param properties
     *            properties to send to Elasticsearch
     * @return number of the successfully updated properties. If the mapping acknowledge fails the method will returns -1
     * @throws IOException
     *             if it fails to execute mapping request.
     */
    public IndexMappingResult initializeElasticsearchIndexMappings(Collection<PropertyDefinition> properties) throws IOException
    {
        Pair<Request, Integer> mappingRequestBuilder = elasticsearchFieldBuilder.buildFieldsMappings(httpClientFactory.getIndexName(), properties);
        Request request = mappingRequestBuilder.getFirst();
        try (Response response = httpClientFactory.getElasticsearchClient().generic().execute(request))
        {
            boolean success = response.getStatus() == 200;
            if (!success)
            {
                LOGGER.warn("Elasticsearch mappings could not be updated, check Elasticsearch log for more details, {} model, {} request", properties,
                        request.getBody().get().bodyAsString());
            }
            Integer successfullyMappedPropertiesCount = mappingRequestBuilder.getSecond();
            return new IndexMappingResult(success, successfullyMappedPropertiesCount);
        }
    }

    public void setCustomAnalyzerConfigFiles(Resource[] customAnalyzerConfigFiles)
    {
        this.customAnalyzerConfigFiles = customAnalyzerConfigFiles;
    }

    public static class IndexMappingResult
    {
        private final boolean acknowledged;
        private final int successfullyMappedPropertiesCount;

        private IndexMappingResult(boolean acknowledged, int successfullyMappedPropertiesCount)
        {
            this.acknowledged = acknowledged;
            this.successfullyMappedPropertiesCount = successfullyMappedPropertiesCount;
        }

        public boolean isAcknowledged()
        {
            return acknowledged;
        }

        public int getSuccessfullyMappedPropertiesCount()
        {
            return successfullyMappedPropertiesCount;
        }
    }
}
