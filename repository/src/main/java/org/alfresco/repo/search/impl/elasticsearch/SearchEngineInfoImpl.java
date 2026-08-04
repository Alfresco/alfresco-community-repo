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

import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.OpenSearchVersionInfo;
import org.opensearch.client.opensearch.core.InfoResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.alfresco.repo.search.impl.elasticsearch.client.ElasticsearchHttpClientFactory;

/**
 * Default {@link SearchEngineInfo} implementation. It detects the search engine provider and version by calling the
 * engine's info endpoint ({@code GET /}) through the shared {@link ElasticsearchHttpClientFactory} client and caches the
 * result so it can be consumed anywhere in the repository.
 */
public class SearchEngineInfoImpl implements SearchEngineInfo
{
    private static final Logger LOGGER = LoggerFactory.getLogger(SearchEngineInfoImpl.class);

    private final ElasticsearchHttpClientFactory clientFactory;

    private volatile SearchEngineType type = SearchEngineType.UNKNOWN;
    private volatile String version;
    private volatile String distribution;
    private volatile boolean detected;

    public SearchEngineInfoImpl(ElasticsearchHttpClientFactory clientFactory)
    {
        this.clientFactory = clientFactory;
    }

    @Override
    public synchronized void detect()
    {
        try
        {
            OpenSearchClient client = clientFactory.getElasticsearchClient();
            InfoResponse info = client.info();
            OpenSearchVersionInfo versionInfo = info.version();

            String detectedDistribution = versionInfo == null ? null : versionInfo.distribution();
            String detectedVersion = versionInfo == null ? null : versionInfo.number();

            this.distribution = detectedDistribution;
            this.version = detectedVersion;
            this.type = SearchEngineType.fromDistribution(detectedDistribution);
            this.detected = true;

            LOGGER.info("Detected search engine provider '{}' (distribution='{}') version '{}'.",
                    type.getDisplayName(), detectedDistribution, detectedVersion);
        }
        catch (Exception e)
        {
            this.type = SearchEngineType.UNKNOWN;
            this.version = null;
            this.distribution = null;
            this.detected = false;
            LOGGER.warn("Unable to detect search engine provider and version: {}", e.getMessage());
            LOGGER.debug("Search engine detection failure detail", e);
        }
    }

    @Override
    public SearchEngineType getType()
    {
        return type;
    }

    @Override
    public String getVersion()
    {
        return version;
    }

    @Override
    public String getDistribution()
    {
        return distribution;
    }

    @Override
    public boolean isDetected()
    {
        return detected;
    }
}
