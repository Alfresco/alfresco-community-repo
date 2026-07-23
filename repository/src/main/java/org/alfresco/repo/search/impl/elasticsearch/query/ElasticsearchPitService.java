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
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.opensearch.client.opensearch.generic.Body;
import org.opensearch.client.opensearch.generic.Request;
import org.opensearch.client.opensearch.generic.Requests;
import org.opensearch.client.opensearch.generic.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.alfresco.repo.search.impl.elasticsearch.client.ElasticsearchHttpClientFactory;

// Opens and closes Elasticsearch Point-In-Time (PIT) snapshots for search_after deep pagination, via the low-level transport (ES uses /_pit, not the OpenSearch client's endpoint).
public class ElasticsearchPitService
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticsearchPitService.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final ElasticsearchHttpClientFactory httpClientFactory;

    public ElasticsearchPitService(ElasticsearchHttpClientFactory httpClientFactory)
    {
        this.httpClientFactory = httpClientFactory;
    }

    public String open(String indexName, String keepAlive) throws IOException
    {
        Request request = Requests.builder()
                .method("POST")
                .endpoint("/" + indexName + "/_pit")
                .query(Map.of("keep_alive", keepAlive))
                .build();

        try (Response response = httpClientFactory.getElasticsearchClient().generic().execute(request))
        {
            int status = response.getStatus();
            if (status < 200 || status >= 300)
            {
                throw new IllegalStateException("Failed to open PIT on index " + indexName + " (status " + status + "): " + response.getReason());
            }

            String body = response.getBody()
                    .map(Body::bodyAsString)
                    .orElseThrow(() -> new IllegalStateException("Empty response opening PIT on index " + indexName));

            String pitId = MAPPER.readTree(body).path("id").asText(null);
            if (pitId == null || pitId.isBlank())
            {
                throw new IllegalStateException("PIT open response did not contain an id for index " + indexName + ": " + body);
            }
            return pitId;
        }
    }

    public void close(String pitId)
    {
        if (pitId == null || pitId.isBlank())
        {
            return;
        }

        try
        {
            String body = MAPPER.writeValueAsString(Map.of("id", pitId));
            Request request = Requests.builder()
                    .method("DELETE")
                    .endpoint("/_pit")
                    .json(body)
                    .build();

            try (Response response = httpClientFactory.getElasticsearchClient().generic().execute(request))
            {
                int status = response.getStatus();
                if (status < 200 || status >= 300)
                {
                    LOGGER.warn("Best-effort PIT close returned status {}: {}", status, response.getReason());
                }
            }
        }
        catch (Exception exception)
        {
            LOGGER.warn("Best-effort PIT close failed (suppressed): {}", exception.toString());
        }
    }
}
