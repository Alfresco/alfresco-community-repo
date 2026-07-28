/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2026 Alfresco Software Limited
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
package org.alfresco.repo.search.impl.elasticsearch.query;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.opensearch.client.opensearch.generic.Body;
import org.opensearch.client.opensearch.generic.Request;
import org.opensearch.client.opensearch.generic.Requests;
import org.opensearch.client.opensearch.generic.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.alfresco.repo.search.impl.elasticsearch.client.ElasticsearchHttpClientFactory;

/**
 * Opens and closes Point-In-Time (PIT) snapshots used by {@code search_after} deep pagination. The PIT endpoint and id field diverged between Elasticsearch ({@code /_pit}, id field {@code id}) and OpenSearch ({@code /_search/point_in_time}, id field {@code pit_id}); the engine in use is read from {@link ElasticsearchHttpClientFactory#getEngine()}, and requests are issued via the client's low-level (generic) transport since the typed client only exists for the OpenSearch endpoint.
 */
public class ElasticsearchPitService
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticsearchPitService.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String OPENSEARCH_ENGINE = "opensearch";

    private final ElasticsearchHttpClientFactory httpClientFactory;

    public ElasticsearchPitService(ElasticsearchHttpClientFactory httpClientFactory)
    {
        this.httpClientFactory = httpClientFactory;
    }

    public String open(String indexName, String keepAlive) throws IOException
    {
        boolean openSearch = isOpenSearchEngine();
        String endpoint = "/" + indexName + (openSearch ? "/_search/point_in_time" : "/_pit");
        JsonNode response = post(endpoint, Map.of("keep_alive", keepAlive));

        String pitId = response.path(openSearch ? "pit_id" : "id").asText(null);
        if (pitId == null || pitId.isBlank())
        {
            throw new PointInTimeException("PIT open response did not contain a pit id for index " + indexName);
        }
        return pitId;
    }

    public void close(String pitId)
    {
        if (pitId == null || pitId.isBlank())
        {
            return;
        }

        try
        {
            boolean openSearch = isOpenSearchEngine();
            String endpoint = openSearch ? "/_search/point_in_time" : "/_pit";
            Object body = openSearch ? Map.of("pit_id", List.of(pitId)) : Map.of("id", pitId);
            delete(endpoint, body);
        }
        catch (IOException | PointInTimeException exception)
        {
            LOGGER.warn("Best-effort PIT close failed (suppressed): {}", exception.toString());
        }
    }

    private boolean isOpenSearchEngine()
    {
        return OPENSEARCH_ENGINE.equalsIgnoreCase(httpClientFactory.getEngine());
    }

    private JsonNode post(String endpoint, Map<String, String> queryParams) throws IOException
    {
        return execute(Requests.builder().method("POST").endpoint(endpoint).query(queryParams).build(), "POST " + endpoint);
    }

    private JsonNode delete(String endpoint, Object jsonBody) throws IOException
    {
        return execute(
                Requests.builder().method("DELETE").endpoint(endpoint).json(MAPPER.writeValueAsString(jsonBody)).build(),
                "DELETE " + endpoint);
    }

    private JsonNode execute(Request request, String description) throws IOException
    {
        try (Response response = httpClientFactory.getElasticsearchClient().generic().execute(request))
        {
            int status = response.getStatus();
            if (status < 200 || status >= 300)
            {
                throw new PointInTimeException(description + " failed (status " + status + "): " + response.getReason());
            }
            return MAPPER.readTree(response.getBody().map(Body::bodyAsString).orElse("{}"));
        }
    }
}
