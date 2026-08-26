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
package org.alfresco.repo.search.impl.elasticsearch.admin;

import java.io.IOException;
import java.util.Locale;

import org.json.JSONObject;
import org.opensearch.client.opensearch.generic.Body;
import org.opensearch.client.opensearch.generic.Request;
import org.opensearch.client.opensearch.generic.Requests;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.alfresco.repo.search.impl.elasticsearch.client.ElasticsearchHttpClientFactory;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.attributes.AttributeService;
import org.alfresco.service.transaction.TransactionService;

/**
 * Detects the search engine provider (OpenSearch vs Elasticsearch) and version, and persists it via {@link AttributeService}. Invoked by {@link org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.ElasticsearchInitialiser} once the engine is confirmed reachable.
 */
public class SearchEngineDetector
{
    private static final Logger LOGGER = LoggerFactory.getLogger(SearchEngineDetector.class);

    public static final String ATTR_ROOT = ".searchEngine";
    public static final String ATTR_SEARCH_ENGINE_NAME = "name"; // "OpenSearch" | "Elasticsearch"
    public static final String ATTR_SEARCH_ENGINE_VERSION = "version"; // e.g. "2.13.0"
    // Access the search engine info using something like:
    // Search engine name: attributeService.getAttribute(ATTR_ROOT, ATTR_SEARCH_ENGINE_NAME)
    // Search engine version: attributeService.getAttribute(ATTR_ROOT, ATTR_SEARCH_ENGINE_VERSION)

    private ElasticsearchHttpClientFactory httpClientFactory;
    private AttributeService attributeService;
    private TransactionService transactionService;

    public void detectAndStore()
    {
        try
        {
            SearchEngineInfo searchEngineInfo = detect();// [provider, version]
            store(searchEngineInfo);
            LOGGER.info("Detected search engine: {} {}", searchEngineInfo.getSearchEngineName(), searchEngineInfo.getSearchEngineVersion());
        }
        catch (Exception e)
        {
            LOGGER.warn("Could not detect the search engine provider/version", e);
        }
    }

    private SearchEngineInfo detect() throws IOException
    {
        Request request = Requests.builder().method("GET").endpoint("/").build();
        try (var response = httpClientFactory.getElasticsearchClient().generic().execute(request))
        {
            String raw = response.getBody()
                    .map(Body::bodyAsString)
                    .orElseThrow(() -> new IOException("Empty response from root endpoint"));

            JSONObject root = new JSONObject(raw);
            String versionNumber = root.getJSONObject("version").getString("number");

            SearchEngine searchEngine = switch (root.getString("tagline").toLowerCase(Locale.ROOT))
            {
            case "you know, for search" -> SearchEngine.ELASTICSEARCH;
            case "the opensearch project: https://opensearch.org/" -> SearchEngine.OPENSEARCH;
            default -> SearchEngine.UNKNOWN;
            };
            String provider = searchEngine.name().toLowerCase(Locale.ROOT);
            return new SearchEngineInfo(provider, versionNumber);
        }
    }

    private void store(SearchEngineInfo searchEngineInfo)
    {
        AuthenticationUtil.runAs((AuthenticationUtil.RunAsWork<Void>) () -> {
            RetryingTransactionHelper txnHelper = transactionService.getRetryingTransactionHelper();
            txnHelper.setForceWritable(true);
            return txnHelper.doInTransaction(() -> {
                attributeService.setAttribute(searchEngineInfo.getSearchEngineName(), ATTR_ROOT, ATTR_SEARCH_ENGINE_NAME);
                attributeService.setAttribute(searchEngineInfo.getSearchEngineVersion(), ATTR_ROOT, ATTR_SEARCH_ENGINE_VERSION);
                return null;
            }, false, true);
        }, AuthenticationUtil.getSystemUserName());
    }

    public void setHttpClientFactory(ElasticsearchHttpClientFactory httpClientFactory)
    {
        this.httpClientFactory = httpClientFactory;
    }

    public void setAttributeService(AttributeService attributeService)
    {
        this.attributeService = attributeService;
    }

    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }
}
