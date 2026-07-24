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
package org.alfresco.repo.search.impl.elasticsearch.query;

import java.util.List;
import java.util.Map;

import org.opensearch.client.opensearch._types.SortOptions;
import org.opensearch.client.opensearch._types.Time;
import org.opensearch.client.opensearch._types.aggregations.Aggregation;
import org.opensearch.client.opensearch._types.aggregations.TermsAggregation;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.opensearch.client.opensearch.core.SearchRequest;
import org.opensearch.client.opensearch.core.search.Pit;
import org.opensearch.client.opensearch.core.search.SourceConfig;
import org.opensearch.client.opensearch.core.search.TrackHits;

import org.alfresco.repo.search.impl.elasticsearch.client.ElasticsearchHttpClientFactory;
import org.alfresco.repo.search.impl.elasticsearch.query.aggregation.ElasticsearchAggregationBuilder;
import org.alfresco.repo.search.impl.elasticsearch.query.highlight.ElasticsearchHighlightBuilder;
import org.alfresco.repo.search.impl.elasticsearch.query.language.LanguageQueryBuilder;
import org.alfresco.repo.search.impl.elasticsearch.query.sort.ElasticsearchSortBuilder;
import org.alfresco.repo.search.impl.elasticsearch.resultset.AggregationNameUtil;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchParameters;

@SuppressWarnings("PMD.AvoidThrowingRawExceptionTypes")
public class SearchRequestBuilderService
{

    private final LanguageQueryBuilder languageQueryBuilder;
    private final ElasticsearchHttpClientFactory httpClientFactory;
    private final ElasticsearchSortBuilder elasticsearchSortBuilder;
    private final ElasticsearchAggregationBuilder elasticsearchAggregationBuilder;
    private final ElasticsearchHighlightBuilder elasticsearchHighlightBuilder;
    public static final int TRACK_TOTAL_HITS_ACCURATE = 2_147_483_647;
    public static final int DEFAULT_TRACK_TOTAL_HITS_UP_TO = 10000;

    public SearchRequestBuilderService(
            LanguageQueryBuilder languageQueryBuilder, ElasticsearchHttpClientFactory httpClientFactory,
            ElasticsearchSortBuilder elasticsearchSortBuilder,
            ElasticsearchAggregationBuilder elasticsearchAggregationBuilder,
            ElasticsearchHighlightBuilder elasticsearchHighlightBuilder)
    {
        this.languageQueryBuilder = languageQueryBuilder;
        this.httpClientFactory = httpClientFactory;
        this.elasticsearchSortBuilder = elasticsearchSortBuilder;
        this.elasticsearchAggregationBuilder = elasticsearchAggregationBuilder;
        this.elasticsearchHighlightBuilder = elasticsearchHighlightBuilder;
    }

    /**
     * Build a standard (non-scroll) search request.
     *
     * @param searchParameters
     *            Alfresco search configuration.
     * @param queryWithPermissions
     *            Query including permissions.
     * @param from
     *            Result offset.
     * @param size
     *            Number of results to return.
     * @param indexName
     *            Target index name.
     */
    public SearchRequest buildSearchRequest(
            SearchParameters searchParameters,
            Query queryWithPermissions,
            int from,
            int size,
            String indexName)
    {
        SearchRequest.Builder builder = baseBuilder(queryWithPermissions)
                .trackTotalHits(new TrackHits.Builder().count(resolveTrackTotalHitsLimit(searchParameters)).build())
                .from(from)
                .size(size);

        applyCommon(searchParameters, indexName, builder);
        return builder.build();
    }

    /**
     * Build a scroll search request.
     *
     * @param searchParameters
     *            Alfresco search configuration.
     * @param queryWithPermissions
     *            Query including permissions.
     * @param size
     *            Batch size per scroll page.
     * @param scrollTime
     *            Scroll context duration.
     * @param indexName
     *            Target index name.
     */
    public SearchRequest buildSearchRequest(
            SearchParameters searchParameters,
            Query queryWithPermissions,
            int size,
            Time scrollTime,
            String indexName)
    {
        SearchRequest.Builder builder = baseBuilder(queryWithPermissions)
                .trackTotalHits(new TrackHits.Builder().count(TRACK_TOTAL_HITS_ACCURATE).build())
                .size(size)
                .scroll(scrollTime);

        applyCommon(searchParameters, indexName, builder);
        return builder.build();
    }

    // Previous unified method with boolean flag removed. Update callers accordingly.

    /**
     * Build a {@code search_after} deep-pagination request over a Point-In-Time (PIT). It pins {@code from=0}, applies the requested page size, pins the request to the session's PIT (refreshing its {@code keep_alive}), adds the (optional) {@code search_after} cursor values, and uses a sort that always ends with the {@code _shard_doc} tiebreaker so ordering is a strict total order. The index is carried by the PIT, so it must NOT be set on the request body.
     *
     * @param searchParameters
     *            Alfresco search configuration.
     * @param queryWithPermissions
     *            Query including permissions.
     * @param size
     *            Number of results to return for this page.
     * @param context
     *            The PIT id, its {@code keep_alive}, and the previous page's sort values.
     */
    public SearchRequest buildSearchAfterRequest(
            SearchParameters searchParameters,
            Query queryWithPermissions,
            int size,
            SearchAfterContext context)
    {
        SearchRequest.Builder builder = baseBuilder(queryWithPermissions)
                .trackTotalHits(new TrackHits.Builder().count(resolveTrackTotalHitsLimit(searchParameters)).build())
                .from(0)
                .size(size)
                .pit(new Pit.Builder().id(context.getPitId()).keepAlive(context.getKeepAlive()).build());

        if (!context.getSearchAfter().isEmpty())
        {
            builder.searchAfterVals(context.getSearchAfter());
        }

        applyCommonWithoutIndex(searchParameters, builder, elasticsearchSortBuilder.getSortBuildersWithTiebreaker(searchParameters));
        return builder.build();
    }

    private int resolveTrackTotalHitsLimit(SearchParameters searchParameters)
    {
        if (searchParameters.getTrackTotalHits() == -1)
        {
            return TRACK_TOTAL_HITS_ACCURATE;
        }
        if (searchParameters.getTrackTotalHits() > 0)
        {
            return searchParameters.getTrackTotalHits();
        }
        return DEFAULT_TRACK_TOTAL_HITS_UP_TO;
    }

    private SearchRequest.Builder baseBuilder(Query queryWithPermissions)
    {
        return new SearchRequest.Builder()
                .query(queryWithPermissions)
                .source(new SourceConfig.Builder().fetch(false).build());
    }

    private void applyCommon(SearchParameters searchParameters, String indexName, SearchRequest.Builder builder)
    {
        applyCommon(searchParameters, indexName, builder, elasticsearchSortBuilder.getSortBuilders(searchParameters));
    }

    private void applyCommon(SearchParameters searchParameters, String indexName, SearchRequest.Builder builder, List<SortOptions> sortOptions)
    {
        applyCommonWithoutIndex(searchParameters, builder, sortOptions);
        builder.index(indexName);
    }

    private void applyCommonWithoutIndex(SearchParameters searchParameters, SearchRequest.Builder builder, List<SortOptions> sortOptions)
    {
        builder.trackScores(searchParameters.isTrackScore());
        sortOptions.forEach(builder::sort);

        elasticsearchAggregationBuilder.filterAggregation(searchParameters, languageQueryBuilder)
                .entrySet()
                .stream()
                .map(res -> Map.of(AggregationNameUtil.encode(res.getKey()),
                        Aggregation.of(agg -> agg.filter(res.getValue()))))
                .forEach(builder::aggregations);

        elasticsearchAggregationBuilder.termsAggregations(searchParameters, languageQueryBuilder)
                .map(res -> Map.of(AggregationNameUtil.encode(res.name()), Aggregation.of(agg -> agg.terms(
                        new TermsAggregation.Builder().field(res.field())
                                .order(res.order())
                                .include(res.include())
                                .size(res.size())
                                .minDocCount(res.minDocCount())
                                .missing(res.missing())
                                .build()))))
                .forEach(builder::aggregations);

        builder.highlight(elasticsearchHighlightBuilder.getHighlightBuilder(searchParameters));
    }

    /**
     * Translates the Alfresco Store name to an Elasticsearch Index name
     *
     * @param stores
     *            List of Alfresco Store names, it must contain only one element
     * @return String with the name of the Elasticsearch Index name corresponding to the Alfresco Store name
     */
    public String getElasticIndex(List<StoreRef> stores)
    {
        if (stores.size() != 1)
        {
            throw new RuntimeException("Querying Elasticsearch with an store list " + stores + " is not supported");
        }
        return switch (stores.get(0).getProtocol())
        {
        case StoreRef.PROTOCOL_WORKSPACE -> httpClientFactory.getIndexName();
        case StoreRef.PROTOCOL_ARCHIVE -> httpClientFactory.getArchiveIndexName();
        default -> throw new RuntimeException(
                "Protocol " + stores.get(0).getProtocol() + " is not supported when using Elasticsearch");
        };
    }
}
