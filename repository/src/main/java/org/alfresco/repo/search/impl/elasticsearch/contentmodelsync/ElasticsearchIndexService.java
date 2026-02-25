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

import static org.alfresco.elasticsearch.shared.ElasticsearchConstants.OWNER;

import java.io.IOException;
import jakarta.json.Json;

import org.apache.commons.httpclient.HttpStatus;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.OpenSearchException;
import org.opensearch.client.opensearch.generic.Request;
import org.opensearch.client.opensearch.generic.Requests;
import org.opensearch.client.opensearch.generic.Response;
import org.opensearch.client.opensearch.indices.ExistsRequest;
import org.opensearch.client.opensearch.indices.GetFieldMappingRequest;
import org.opensearch.client.opensearch.indices.GetFieldMappingResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.alfresco.repo.search.impl.elasticsearch.client.ElasticsearchHttpClientFactory;

/**
 * This class aims to interact with Elasticsearch for any operation strict related to index management.
 */
public class ElasticsearchIndexService
{

    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticsearchIndexService.class);

    private final OpenSearchClient client;
    private final ElasticsearchHttpClientFactory elasticsearchHttpClientFactory;

    private final int fieldsLimit;
    private final int maxResultWindow;

    public ElasticsearchIndexService(ElasticsearchHttpClientFactory elasticsearchHttpClientFactory, int fieldsLimit, int maxResultWindow)
    {
        this.client = elasticsearchHttpClientFactory.getElasticsearchClient();
        this.elasticsearchHttpClientFactory = elasticsearchHttpClientFactory;
        this.fieldsLimit = fieldsLimit;
        this.maxResultWindow = maxResultWindow;
    }

    /**
     * Check if the given index exists.
     *
     * @return true if the index is found.
     */
    public boolean indexExists()
    {
        String index = elasticsearchHttpClientFactory.getIndexName();
        ExistsRequest request = new ExistsRequest.Builder().index(index).build();
        try
        {
            return client.indices().exists(request).value();
        }
        catch (IOException e)
        {
            LOGGER.error("Failed to check if index {} exists", index, e);
        }
        return false;
    }

    /**
     * Creates an index with the given name in the Elasticsearch server.
     *
     * @return true if the index exists after the call.
     */
    public boolean createIndex()
    {
        String index = elasticsearchHttpClientFactory.getIndexName();
        Request requests = Requests.builder().method("PUT")
                .endpoint("/" + index).json(Json.createObjectBuilder().add("settings", Json.createObjectBuilder()
                        .add("index.mapping.total_fields.limit", fieldsLimit)
                        .add("index.max_result_window", maxResultWindow)))
                .build();

        try
        {
            Response response = client.generic().execute(requests);
            int statusCode = response.getStatus();
            if (statusCode != HttpStatus.SC_OK && statusCode != HttpStatus.SC_CREATED)
            {
                LOGGER.error("Timed out waiting for index with name {} to be created", index);
            }
            LOGGER.info("Index created with name {}", index);
        }
        catch (OpenSearchException | IOException e)
        {
            LOGGER.error("Failed to create index with name {}", index, e);
        }
        return indexExists();
    }

    /**
     * This method check if the basic mapping is loaded checking if a basic field mapping defined in resources/alfresco/search/elasticsearch/config/basicFields.json exists.
     * 
     * @return true if mapping exists, false otherwise
     */
    public boolean isMappingLoaded()
    {
        boolean result = false;
        String index = elasticsearchHttpClientFactory.getIndexName();
        try
        {
            GetFieldMappingRequest request = new GetFieldMappingRequest.Builder().fields(OWNER).index(index).build();
            GetFieldMappingResponse fieldMappingResponse = client.indices()
                    .getFieldMapping(request);

            result = !fieldMappingResponse.result().get(index).mappings().isEmpty();
        }
        catch (IOException e)
        {
            LOGGER.error("Failed to check if basic mapping is loaded on {}", index, e);
        }
        return result;
    }
}
