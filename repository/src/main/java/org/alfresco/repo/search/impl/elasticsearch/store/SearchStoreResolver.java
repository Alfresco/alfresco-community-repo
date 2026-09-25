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
package org.alfresco.repo.search.impl.elasticsearch.store;

import java.util.List;

import org.alfresco.repo.search.impl.elasticsearch.client.ElasticsearchHttpClientFactory;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchParameters;

/**
 * Resolves the single Alfresco {@link StoreRef} an Elasticsearch query targets and the index it maps to.
 */
public class SearchStoreResolver
{
    private final ElasticsearchHttpClientFactory httpClientFactory;

    public SearchStoreResolver(ElasticsearchHttpClientFactory httpClientFactory)
    {
        this.httpClientFactory = httpClientFactory;
    }

    /**
     * Resolves the single store targeted by the query.
     *
     * @param searchParameters
     *            the current search parameters
     * @return the single {@link StoreRef} the query targets
     */
    public StoreRef resolveStore(SearchParameters searchParameters)
    {
        return resolveStore(searchParameters.getStores());
    }

    /**
     * Resolves the single store from a store list. Elasticsearch supports exactly one store per query.
     *
     * @param stores
     *            the requested stores
     * @return the single {@link StoreRef}
     * @throws IllegalArgumentException
     *             if the list does not contain exactly one store
     */
    public StoreRef resolveStore(List<StoreRef> stores)
    {
        if (stores == null || stores.size() != 1)
        {
            throw new IllegalArgumentException("Querying Elasticsearch with a store list " + stores + " is not supported");
        }
        return stores.getFirst();
    }

    /**
     * Resolves the Elasticsearch index name for the store targeted by the query.
     *
     * @param stores
     *            the requested stores
     * @return the Elasticsearch index name
     */
    public String resolveIndex(List<StoreRef> stores)
    {
        StoreRef store = resolveStore(stores);
        return switch (store.getProtocol())
        {
        case StoreRef.PROTOCOL_WORKSPACE, StoreRef.PROTOCOL_ARCHIVE -> httpClientFactory.getIndexName();
        default -> throw new IllegalArgumentException(
                "Protocol " + store.getProtocol() + " is not supported when using Elasticsearch");
        };
    }

    /**
     * @param searchParameters
     *            the current search parameters
     * @return {@code true} if the query targets the archive ("deleted-nodes") scope
     */
    public boolean isArchiveScope(SearchParameters searchParameters)
    {
        return StoreRef.PROTOCOL_ARCHIVE.equals(resolveStore(searchParameters).getProtocol());
    }
}
