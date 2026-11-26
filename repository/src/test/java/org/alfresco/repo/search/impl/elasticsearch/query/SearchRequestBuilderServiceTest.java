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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opensearch.client.opensearch._types.aggregations.AggregationBuilders;
import org.opensearch.client.opensearch._types.aggregations.TermsAggregation;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.opensearch.client.opensearch.core.search.Highlight;

import org.alfresco.repo.search.impl.elasticsearch.query.aggregation.ElasticsearchAggregationBuilder;
import org.alfresco.repo.search.impl.elasticsearch.query.aggregation.TermsAggregationWrapper;
import org.alfresco.repo.search.impl.elasticsearch.query.aggregation.TermsAggregationWrapper.ComplementaryAggregation;
import org.alfresco.repo.search.impl.elasticsearch.query.highlight.ElasticsearchHighlightBuilder;
import org.alfresco.repo.search.impl.elasticsearch.query.language.LanguageQueryBuilder;
import org.alfresco.repo.search.impl.elasticsearch.query.sort.ElasticsearchSortBuilder;
import org.alfresco.repo.search.impl.elasticsearch.resultset.AggregationNameUtil;
import org.alfresco.repo.search.impl.elasticsearch.store.SearchStoreResolver;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.util.Pair;

public class SearchRequestBuilderServiceTest
{

    @Mock
    private SearchStoreResolver searchStoreResolver;
    @Mock
    private LanguageQueryBuilder languageQueryBuilder;
    @Mock
    private ElasticsearchSortBuilder elasticsearchSortBuilder;
    @Mock
    private ElasticsearchAggregationBuilder elasticsearchAggregationBuilder;
    @Mock
    private ElasticsearchHighlightBuilder elasticsearchHighlightBuilder;

    private SearchRequestBuilderService service;

    @Before
    public void setUp()
    {
        MockitoAnnotations.openMocks(this);
        service = new SearchRequestBuilderService(
                languageQueryBuilder,
                searchStoreResolver,
                elasticsearchSortBuilder,
                elasticsearchAggregationBuilder,
                elasticsearchHighlightBuilder);
    }

    /**
     * A Lucene-syntax SITE facet name ("@SITE") requires URL-encoding when used as an ES aggregation name ('@' -> "%40"). AggregationHandler stores field facets keyed by the 'decoded' terms aggregation name, so the complementary buckets translator must point back to that same decoded name, not a re-encoded one.
     */
    @Test
    public void buildSearchRequest_withComplementaryAggregation_translatorKeysOwnerByDecodedName()
    {
        String aggName = "@SITE";
        String complementaryAggName = aggName + "__REPOSITORY__";

        TermsAggregation termsAggregation = AggregationBuilders.terms()
                .name(aggName)
                .field("primaryHierarchy")
                .build();
        ComplementaryAggregation complementaryAggregation = new ComplementaryAggregation(
                complementaryAggName,
                Query.of(q -> q.matchAll(m -> m)),
                "_REPOSITORY_");
        TermsAggregationWrapper wrapper = new TermsAggregationWrapper(
                aggName, termsAggregation, Collections.emptyMap(), Optional.of(complementaryAggregation));

        when(elasticsearchAggregationBuilder.filterAggregation(any(), any())).thenReturn(Collections.emptyMap());
        when(elasticsearchAggregationBuilder.termsAggregations(any(), any())).thenReturn(Stream.of(wrapper));
        when(elasticsearchSortBuilder.getSortBuilders(any())).thenReturn(List.of());
        when(elasticsearchHighlightBuilder.getHighlightBuilder(any())).thenReturn(mock(Highlight.class));

        SearchParameters searchParameters = new SearchParameters();
        Query queryWithPermissions = Query.of(q -> q.matchAll(m -> m));

        SearchRequestWrapper result = service.buildSearchRequest(searchParameters, queryWithPermissions, 0, 10, "alfresco");

        Map<String, Pair<String, String>> complementaryBucketsTranslator = result.complementaryBucketsTranslator();
        Pair<String, String> pair = complementaryBucketsTranslator.get(AggregationNameUtil.encode(complementaryAggName));

        assertEquals("Complementary bucket must be attributed to the plain (decoded) terms aggregation name",
                aggName, pair.getFirst());
    }

    @Test
    public void testGetElasticIndex_delegatesToStoreResolver()
    {
        StoreRef store = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore");
        when(searchStoreResolver.resolveIndex(List.of(store))).thenReturn("workspace-index");
        String index = service.getElasticIndex(List.of(store));
        assertEquals("workspace-index", index);
    }

    @Test
    public void testGetElasticIndex_propagatesResolverException()
    {
        StoreRef store = new StoreRef("unsupported", "SpacesStore");
        when(searchStoreResolver.resolveIndex(List.of(store)))
                .thenThrow(new IllegalArgumentException("Protocol unsupported is not supported when using Elasticsearch"));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.getElasticIndex(List.of(store)));
        assertTrue(exception.getMessage().contains("is not supported"));
    }
}
