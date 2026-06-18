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

import org.junit.Before;
import org.junit.Test;
import org.opensearch.client.opensearch._types.aggregations.TermsAggregation;

import org.alfresco.repo.search.impl.elasticsearch.ElasticsearchChildApplicationContextFactory;
import org.alfresco.service.cmr.search.SearchParameters;

public class TermsAggregationsIT extends ElasticsearchBaseQueryIT
{
    private static final String CONTENT_FIELD = "cm:content";
    private static final String CREATOR_FIELD = "cm:creator";
    private static final String MIMETYPE_FIELD = "cm:content.mimetype";
    private static final int CUSTOM_FACET_LIMIT = 12;
    private int defaultFacetLimit;

    @Before
    public void setup()
    {
        ElasticsearchChildApplicationContextFactory elasticsearchContextFactory = (ElasticsearchChildApplicationContextFactory) elasticsearchContext.getBean("elasticsearch");
        defaultFacetLimit = Integer.parseInt(elasticsearchContextFactory.getProperty("elasticsearch.defaultFacetLimit"));
    }

    @Test
    public void givenDefaultFacetLimitTermsAggregationShouldUseDefaultValue()
    {
        SearchParameters testParam = createSearchParametersWithDefaultFacetFields(List.of(CONTENT_FIELD, CREATOR_FIELD, MIMETYPE_FIELD));
        List<TermsAggregation> termsAggregationBuilders = elasticsearchAggregationBuilder
                .termsAggregations(testParam, elasticsearchAFTSQueryBuilder).toList();

        assertEquals(3, termsAggregationBuilders.size());
        assertEquals(termsAggregationBuilders.get(0).name(), CONTENT_FIELD);
        assertEquals(termsAggregationBuilders.get(1).name(), CREATOR_FIELD);
        assertEquals(termsAggregationBuilders.get(2).name(), MIMETYPE_FIELD);

        boolean isFacetLimitCorrect = termsAggregationBuilders.stream()
                .allMatch(item -> item.size() == defaultFacetLimit);
        assertTrue("Requested facets limit should be set to the configured value", isFacetLimitCorrect);
    }

    @Test
    public void givenCustomFacetLimitTermsAggregationShouldUseUpdatedValue()
    {
        elasticsearchAggregationBuilder.setDefaultFacetLimit(CUSTOM_FACET_LIMIT);

        SearchParameters testParam = createSearchParametersWithDefaultFacetFields(List.of(CONTENT_FIELD, CREATOR_FIELD, MIMETYPE_FIELD));
        List<TermsAggregation> termsAggregationBuilders = elasticsearchAggregationBuilder
                .termsAggregations(testParam, elasticsearchAFTSQueryBuilder).toList();

        assertEquals(3, termsAggregationBuilders.size());
        assertEquals(termsAggregationBuilders.get(0).name(), CONTENT_FIELD);
        assertEquals(termsAggregationBuilders.get(1).name(), CREATOR_FIELD);
        assertEquals(termsAggregationBuilders.get(2).name(), MIMETYPE_FIELD);

        boolean isFacetLimitCorrect = termsAggregationBuilders.stream()
                .allMatch(item -> item.size() == CUSTOM_FACET_LIMIT);
        assertTrue("Requested facets limit should be set to the custom value", isFacetLimitCorrect);

        elasticsearchAggregationBuilder.setDefaultFacetLimit(defaultFacetLimit);
    }

    @Test
    public void facetQueryDecoratedWithLimitOrNullParameterShouldOverwriteDefaultFacetLimit()
    {
        SearchParameters testParam = createSearchParametersWithDefaultFacetFields(List.of(CONTENT_FIELD, CREATOR_FIELD));
        SearchParameters.FieldFacet mimetypeFieldFacet = new SearchParameters.FieldFacet(MIMETYPE_FIELD);
        mimetypeFieldFacet.setLimitOrNull(CUSTOM_FACET_LIMIT);
        testParam.addFieldFacet(mimetypeFieldFacet);

        List<TermsAggregation> termsAggregationBuilders = elasticsearchAggregationBuilder
                .termsAggregations(testParam, elasticsearchAFTSQueryBuilder).toList();

        assertEquals(3, termsAggregationBuilders.size());
        assertEquals(termsAggregationBuilders.get(0).name(), CONTENT_FIELD);
        assertEquals(termsAggregationBuilders.get(1).name(), CREATOR_FIELD);
        assertEquals(termsAggregationBuilders.get(2).name(), MIMETYPE_FIELD);

        assertEquals("Two facets should have default limit", 2, countFacetsWithSameLimit(defaultFacetLimit, termsAggregationBuilders));
        assertEquals("One facet should have limit value modified", 1, countFacetsWithSameLimit(CUSTOM_FACET_LIMIT, termsAggregationBuilders));
    }

    private long countFacetsWithSameLimit(int limit, List<TermsAggregation> termsAggregationBuilders)
    {
        return termsAggregationBuilders.stream()
                .filter(item -> item.size() == limit)
                .count();
    }

    private SearchParameters createSearchParametersWithDefaultFacetFields(List<String> facetNames)
    {
        SearchParameters testParam = new SearchParameters();
        facetNames.forEach(facetName -> testParam.addFieldFacet(new SearchParameters.FieldFacet(facetName)));
        return testParam;
    }
}
