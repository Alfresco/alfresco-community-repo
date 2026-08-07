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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.alfresco.repo.search.SearchEngineInfo;
import org.alfresco.repo.search.impl.elasticsearch.client.ElasticsearchHttpClientFactory;

/**
 * Detects the configured search engine (Elasticsearch vs OpenSearch) and its version when the
 * elasticsearch subsystem starts, storing the result in the shared {@link SearchEngineInfo} bean.
 * <p>
 * This replaces the previous standalone {@code DetectSearchEngine} lifecycle bean, which used a raw
 * {@link java.net.http.HttpClient} with hardcoded {@code http://elasticsearch:9200} URLs. Instead,
 * it reuses the subsystem's existing REST client ({@link ElasticsearchHttpClientFactory}, already
 * configured via {@code elasticsearch.host}, {@code elasticsearch.port}, {@code elasticsearch.baseUrl}
 * and the associated TLS / authentication properties) and issues a single {@code GET /} through
 * {@link org.opensearch.client.opensearch.OpenSearchClient#info()}.
 * <p>
 * Detection rules (from the {@code version} object in the response):
 * <ul>
 *   <li>{@code version.distribution == "opensearch"} &rarr; provider is OpenSearch</li>
 *   <li>{@code version.distribution} absent &rarr; provider is Elasticsearch</li>
 *   <li>{@code version.number} &rarr; engine version</li>
 *   <li>{@code version.lucene_version} &rarr; Lucene version</li>
 * </ul>
 * If detection fails (for example the engine is unreachable at start-up), a warning is logged and
 * safe default values are used so that subsystem start-up is never blocked.
 */
public class SearchEngineInfoDetector
{
    private static final Log logger = LogFactory.getLog(SearchEngineInfoDetector.class);

    private final ElasticsearchHttpClientFactory httpClientFactory;
    private final SearchEngineInfo searchEngineInfo;

    public SearchEngineInfoDetector(ElasticsearchHttpClientFactory httpClientFactory, SearchEngineInfo searchEngineInfo)
    {
        this.httpClientFactory = httpClientFactory;
        this.searchEngineInfo = searchEngineInfo;
    }

    /**
     * Query the configured search engine and populate {@link SearchEngineInfo}. Invoked as the
     * Spring {@code init-method} when the elasticsearch subsystem context starts.
     */
    public void detect()
    {
        try
        {
            var version = httpClientFactory.getElasticsearchClient().info().version();

            String distribution = version.distribution();
            String provider = (distribution != null && distribution.equalsIgnoreCase(SearchEngineInfo.PROVIDER_OPENSEARCH))
                    ? SearchEngineInfo.PROVIDER_OPENSEARCH
                    : SearchEngineInfo.PROVIDER_ELASTICSEARCH;

            searchEngineInfo.setSearchEngineName(provider);
            searchEngineInfo.setSearchEngineVersion(defaultIfBlank(version.number()));
            searchEngineInfo.setSearchEngineLuceneVersion(defaultIfBlank(version.luceneVersion()));

            logger.info("Detected search engine: name=" + searchEngineInfo.getSearchEngineName()
                    + ", version=" + searchEngineInfo.getSearchEngineVersion()
                    + ", luceneVersion=" + searchEngineInfo.getSearchEngineLuceneVersion());
        }
        catch (Exception e)
        {
            searchEngineInfo.setSearchEngineName(SearchEngineInfo.PROVIDER_ELASTICSEARCH);
            searchEngineInfo.setSearchEngineVersion(SearchEngineInfo.UNKNOWN);
            searchEngineInfo.setSearchEngineLuceneVersion(SearchEngineInfo.UNKNOWN);

            logger.warn("Unable to detect the search engine via GET / using the elasticsearch subsystem client; "
                    + "defaulting to name=" + SearchEngineInfo.PROVIDER_ELASTICSEARCH
                    + ", version=" + SearchEngineInfo.UNKNOWN
                    + ". Cause: " + e.getMessage(), e);
        }
    }

    private static String defaultIfBlank(String value)
    {
        return (value == null || value.isBlank()) ? SearchEngineInfo.UNKNOWN : value;
    }
}
