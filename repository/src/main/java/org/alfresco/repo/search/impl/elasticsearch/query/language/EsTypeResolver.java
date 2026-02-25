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
package org.alfresco.repo.search.impl.elasticsearch.query.language;

import java.io.IOException;
import java.util.Optional;

import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.mapping.FieldMapping;
import org.opensearch.client.opensearch.indices.GetFieldMappingRequest;
import org.opensearch.client.opensearch.indices.GetFieldMappingResponse;

import org.alfresco.repo.search.impl.elasticsearch.client.ElasticsearchHttpClientFactory;

public class EsTypeResolver
{
    private final ElasticsearchHttpClientFactory elasticsearchHttpClientFactory;

    public EsTypeResolver(ElasticsearchHttpClientFactory elasticsearchHttpClientFactory)
    {
        this.elasticsearchHttpClientFactory = elasticsearchHttpClientFactory;
    }

    public Optional<String> resolve(String esFieldName)
    {
        OpenSearchClient client = elasticsearchHttpClientFactory.getElasticsearchClient();
        String index = elasticsearchHttpClientFactory.getIndexName();
        try
        {
            GetFieldMappingRequest request = new GetFieldMappingRequest.Builder().fields(esFieldName).index(index).build();

            return Optional.ofNullable(client.indices().getFieldMapping(request))
                    .map(GetFieldMappingResponse::result)
                    .map(indexMapping -> indexMapping.get(index))
                    .map(fieldMapping -> fieldMapping.mappings().get(esFieldName))
                    .map(FieldMapping::mapping)
                    .map(metadataFieldMapping -> metadataFieldMapping.get(esFieldName))
                    .map(source -> source._kind().jsonValue());
        }
        catch (IOException e)
        {
            return Optional.empty();
        }
    }
}
