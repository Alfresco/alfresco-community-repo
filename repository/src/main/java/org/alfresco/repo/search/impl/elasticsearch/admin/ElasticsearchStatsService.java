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
package org.alfresco.repo.search.impl.elasticsearch.admin;

import static org.alfresco.elasticsearch.shared.ElasticsearchConstants.ALIVE;
import static org.alfresco.elasticsearch.shared.ElasticsearchConstants.CM_CONTENT_TR_STATUS;
import static org.alfresco.elasticsearch.shared.ElasticsearchConstants.CONTENT_ATTRIBUTE_NAME;
import static org.alfresco.elasticsearch.shared.ElasticsearchConstants.TYPE;
import static org.alfresco.elasticsearch.shared.translator.AlfrescoQualifiedNameTranslator.encode;

import java.io.IOException;
import java.util.Map;
import java.util.OptionalLong;

import org.opensearch.client.opensearch._types.FieldValue;
import org.opensearch.client.opensearch._types.query_dsl.BoolQuery;
import org.opensearch.client.opensearch._types.query_dsl.QueryBuilders;
import org.opensearch.client.opensearch._types.query_dsl.TermsQueryField;
import org.opensearch.client.opensearch.core.CountRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.alfresco.repo.event2.filter.NodeTypeFilter;
import org.alfresco.repo.search.IndexerAndSearcher;
import org.alfresco.repo.search.impl.elasticsearch.ElasticStatsResult;
import org.alfresco.repo.search.impl.elasticsearch.client.ElasticsearchHttpClientFactory;
import org.alfresco.repo.search.impl.lucene.LuceneQueryLanguageSPI;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.search.StatsParameters;
import org.alfresco.service.cmr.search.StatsResultSet;
import org.alfresco.service.cmr.search.StatsService;
import org.alfresco.service.namespace.QName;

/**
 * Class to obtain statistics from an Elasticsearch cluster.
 */
public class ElasticsearchStatsService implements StatsService
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticsearchStatsService.class);
    private static final String TRANSFORM_FAILED = "TRANSFORM_FAILED";
    private static final String NO_TRANSFORM = "NO_TRANSFORM";
    private static final String TRANSFORM_OUTDATED = "TRANSFORM_OUTDATED";

    private ElasticsearchHttpClientFactory elasticsearchHttpClientFactory;
    private NodeTypeFilter nodeTypeFilter;

    private IndexerAndSearcher searcher;
    private NodeService nodeService;

    public void setSearcher(IndexerAndSearcher searcher)
    {
        this.searcher = searcher;
    }

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * Get the number of documents currently indexed by the Elasticsearch cluster. Note that because we use a flat strategy for indexing documents with their ACL information then each documents indexed from the repo results in one document in ES.
     *
     * @return The number of documents or null if it was not possible to determine the number of indexed documents.
     */
    public OptionalLong getCount()
    {
        var query = constructCommonRestrictions().build();
        var request = prepareCountRequest(query);
        return invokeCountRequest(request, "All documents count");
    }

    /**
     * @return The number of documents which content was correctly indexed in the Elasticsearch cluster, or empty optional if it was not possible to determine it.
     */
    public OptionalLong getContentIndexingSuccessCount()
    {
        var query = constructCommonRestrictions()
                .must(QueryBuilders.exists().field(encode(CONTENT_ATTRIBUTE_NAME)).build().toQuery())
                .mustNot(QueryBuilders.exists().field(encode(CM_CONTENT_TR_STATUS)).build().toQuery())
                .build();
        var request = prepareCountRequest(query);
        return invokeCountRequest(request, "Content indexing success count");
    }

    /**
     * @return The number of documents which content indexing failed in the Elasticsearch cluster, or empty optional if it was not possible to determine it.
     */
    public OptionalLong getContentIndexingFailuresCount()
    {
        var query = constructCommonRestrictions()
                .must(QueryBuilders.term().field(encode(CM_CONTENT_TR_STATUS)).value(FieldValue.of(TRANSFORM_FAILED)).build().toQuery())
                .build();
        var request = prepareCountRequest(query);
        return invokeCountRequest(request, "Content indexing failures count");
    }

    /**
     * @return The number of newly added documents waiting for content indexing in the Elasticsearch cluster, or empty optional if it was not possible to determine it.
     */
    public OptionalLong getNewContentIndexingInProgressCount()
    {
        var query = constructCommonRestrictions()
                .must(QueryBuilders.term().field(encode(CM_CONTENT_TR_STATUS)).value(FieldValue.of(NO_TRANSFORM)).build().toQuery())
                .build();
        var request = prepareCountRequest(query);
        return invokeCountRequest(request, "New content indexing in progress count");
    }

    /**
     * @return The number of modified added documents waiting for content indexing update in the Elasticsearch cluster, or empty optional if it was not possible to determine it.
     */
    public OptionalLong getOutdatedContentIndexingInProgressCount()
    {
        var query = constructCommonRestrictions()
                .must(QueryBuilders.term().field(encode(CM_CONTENT_TR_STATUS)).value(FieldValue.of(TRANSFORM_OUTDATED)).build().toQuery())
                .build();
        var request = prepareCountRequest(query);
        return invokeCountRequest(request, "Outdated content indexing in progress count");
    }

    public void setElasticsearchHttpClientFactory(ElasticsearchHttpClientFactory elasticsearchHttpClientFactory)
    {
        this.elasticsearchHttpClientFactory = elasticsearchHttpClientFactory;
    }

    public void setNodeTypeFilter(NodeTypeFilter nodeTypeFilter)
    {
        this.nodeTypeFilter = nodeTypeFilter;
    }

    private CountRequest prepareCountRequest(BoolQuery boolQuery)
    {
        var index = elasticsearchHttpClientFactory.getIndexName();
        return new CountRequest.Builder()
                .index(index)
                .query(boolQuery.toQuery())
                .build();
    }

    /**
     * @return builder with common restrictions for all count requests.
     */
    private BoolQuery.Builder constructCommonRestrictions()
    {
        var excludedNodeTypesFieldValue = nodeTypeFilter.getExcludedTypes().stream()
                .map(QName::getPrefixString)
                .map(FieldValue::of)
                .toList();
        return QueryBuilders.bool()
                .must(QueryBuilders.match().field(ALIVE).query(FieldValue.of(true)).build().toQuery())
                .mustNot(QueryBuilders.terms().field(TYPE).terms(new TermsQueryField.Builder().value(excludedNodeTypesFieldValue).build()).build().toQuery());
    }

    private OptionalLong invokeCountRequest(CountRequest request, String description)
    {
        try
        {
            LOGGER.trace("Executing count request '{}'", description);
            var countResponse = elasticsearchHttpClientFactory.getElasticsearchClient()
                    .count(request);
            LOGGER.trace("Count request '{}' was executed: result={}", description, countResponse.count());
            return OptionalLong.of(countResponse.count());
        }
        catch (IOException e)
        {
            LOGGER.error("Failed to get count of documents for request '{}' in index: {}", description, request.index(), e);
        }
        return OptionalLong.empty();
    }

    @Override
    public StatsResultSet query(StatsParameters searchParameters)
    {
        if (searcher == null)
        {
            LOGGER.error("Searcher is not set in ElasticsearchStatsService");
            throw new org.alfresco.repo.search.SearcherException(
                    "Unable to find searcher " + searcher.getClass().getName());
        }

        searchParameters.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);

        Map<String, LuceneQueryLanguageSPI> languages = (searcher != null) ? searcher.getQueryLanguages() : null;
        LuceneQueryLanguageSPI language = (languages != null) ? languages.get(searchParameters.getLanguage().toLowerCase()) : null;

        if (language != null && SearchService.LANGUAGE_INDEX_FTS_ALFRESCO.equals(language.getName()))
        {
            try
            {

                SearchParameters searchParams = convertStatsToSearchParameters(searchParameters);
                ResultSet result = language.executeQuery(searchParams);
                return new ElasticStatsResult(result, nodeService);
            }
            catch (Exception e)
            {
                throw new org.alfresco.repo.search.SearcherException(
                        "Failed to invoke executeStatsQuery on language: " + language.getClass().getName(), e);
            }
        }
        // unknown or unsupported language
        throw new org.alfresco.repo.search.SearcherException("Unknown stats query language: " + searchParameters.getLanguage());
    }

    private SearchParameters convertStatsToSearchParameters(StatsParameters statsParams)
    {
        if (statsParams == null)
        {
            return null;
        }

        SearchParameters searchParams = new SearchParameters();

        searchParams.setQuery(statsParams.getQuery());
        searchParams.setLanguage(statsParams.getLanguage());

        if (statsParams.getStores() != null)
        {
            for (StoreRef store : statsParams.getStores())
            {
                searchParams.addStore(store);
            }
        }
        return searchParams;
    }
}
