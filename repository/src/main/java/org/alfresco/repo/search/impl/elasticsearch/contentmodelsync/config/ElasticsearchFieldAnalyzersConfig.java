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

package org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.config;

import java.io.IOException;
import jakarta.json.Json;
import jakarta.json.JsonReader;

import io.netty.handler.codec.http.HttpMethod;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch.generic.Body;
import org.opensearch.client.opensearch.generic.Requests;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.search.impl.elasticsearch.client.ElasticsearchHttpClientFactory;
import org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.field.property.ElasticsearchAnalyzer;

public class ElasticsearchFieldAnalyzersConfig
{
    private final String indexName;
    private final OpenSearchClient elasticsearchClient;

    public ElasticsearchFieldAnalyzersConfig(ElasticsearchHttpClientFactory factory)
    {
        indexName = factory.getIndexName();
        elasticsearchClient = factory.getElasticsearchClient();
    }

    public boolean isAnalyzerDefinedInElasticsearch(ElasticsearchAnalyzer analyzer)
    {
        var request = Requests.builder()
                .endpoint("/" + indexName + "/_settings/" + analyzerPath(analyzer.getName()))
                .method(HttpMethod.GET.name())
                .build();

        try (var response = elasticsearchClient.generic().execute(request))
        {
            return response
                    .getBody()
                    .map(Body::body)
                    .map(Json::createReader)
                    .map(JsonReader::readObject)
                    .map(jsonObject -> !jsonObject.isEmpty())
                    .orElse(false);
        }
        catch (IOException e)
        {
            throw new AlfrescoRuntimeException("Cannot check if analyzer is defined in elasticsearch", e);
        }
    }

    private String analyzerPath(String analyzerName)
    {
        return "index.analysis.analyzer." + analyzerName + ".*";
    }

}
