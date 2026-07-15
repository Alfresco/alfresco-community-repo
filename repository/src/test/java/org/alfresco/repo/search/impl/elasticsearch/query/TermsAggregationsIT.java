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

import static org.alfresco.repo.search.adaptor.QueryConstants.FIELD_SITE;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import org.alfresco.repo.search.impl.elasticsearch.ElasticsearchChildApplicationContextFactory;
import org.alfresco.repo.search.impl.elasticsearch.query.aggregation.TermsAggregationWrapper;
import org.alfresco.repo.search.impl.elasticsearch.query.aggregation.TermsAggregationWrapper.ComplementaryAggregation;
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
        List<TermsAggregationWrapper> termsAggregationBuilders = elasticsearchAggregationBuilder
                .termsAggregations(testParam, elasticsearchAFTSQueryBuilder).toList();

        assertEquals(3, termsAggregationBuilders.size());
        assertEquals(termsAggregationBuilders.get(0).name(), CONTENT_FIELD);
        assertEquals(termsAggregationBuilders.get(1).name(), CREATOR_FIELD);
        assertEquals(termsAggregationBuilders.get(2).name(), MIMETYPE_FIELD);

        boolean isFacetLimitCorrect = termsAggregationBuilders.stream()
                .allMatch(item -> item.termsAggregation().size() == defaultFacetLimit);
        assertTrue("Requested facets limit should be set to the configured value", isFacetLimitCorrect);
    }

    @Test
    public void givenCustomFacetLimitTermsAggregationShouldUseUpdatedValue()
    {
        elasticsearchAggregationBuilder.setDefaultFacetLimit(CUSTOM_FACET_LIMIT);

        SearchParameters testParam = createSearchParametersWithDefaultFacetFields(List.of(CONTENT_FIELD, CREATOR_FIELD, MIMETYPE_FIELD));
        List<TermsAggregationWrapper> termsAggregationBuilders = elasticsearchAggregationBuilder
                .termsAggregations(testParam, elasticsearchAFTSQueryBuilder).toList();

        assertEquals(3, termsAggregationBuilders.size());
        assertEquals(termsAggregationBuilders.get(0).name(), CONTENT_FIELD);
        assertEquals(termsAggregationBuilders.get(1).name(), CREATOR_FIELD);
        assertEquals(termsAggregationBuilders.get(2).name(), MIMETYPE_FIELD);

        boolean isFacetLimitCorrect = termsAggregationBuilders.stream()
                .allMatch(item -> item.termsAggregation().size() == CUSTOM_FACET_LIMIT);
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

        List<TermsAggregationWrapper> termsAggregationBuilders = elasticsearchAggregationBuilder
                .termsAggregations(testParam, elasticsearchAFTSQueryBuilder).toList();

        assertEquals(3, termsAggregationBuilders.size());
        assertEquals(termsAggregationBuilders.get(0).name(), CONTENT_FIELD);
        assertEquals(termsAggregationBuilders.get(1).name(), CREATOR_FIELD);
        assertEquals(termsAggregationBuilders.get(2).name(), MIMETYPE_FIELD);

        assertEquals("Two facets should have default limit", 2, countFacetsWithSameLimit(defaultFacetLimit, termsAggregationBuilders));
        assertEquals("One facet should have limit value modified", 1, countFacetsWithSameLimit(CUSTOM_FACET_LIMIT, termsAggregationBuilders));
    }

    @Test
    public void givenSiteFacet_termsAggregationShouldUsePrimaryHierarchyField()
    {
        SearchParameters testParam = new SearchParameters();
        testParam.addFieldFacet(new SearchParameters.FieldFacet(FIELD_SITE));

        List<TermsAggregationWrapper> wrappers = elasticsearchAggregationBuilder
                .termsAggregations(testParam, elasticsearchAFTSQueryBuilder).toList();

        // The shared Files folder should always be resolvable in the test environment, so we
        // expect at least one wrapper to be produced.
        assertFalse("SITE facet should produce at least one wrapper when shared home is available",
                wrappers.isEmpty());

        TermsAggregationWrapper siteWrapper = wrappers.get(0);
        assertEquals("SITE facet must target the primaryHierarchy field",
                "primaryHierarchy", siteWrapper.termsAggregation().field());
    }

    @Test
    public void givenSiteFacet_aggregationWrapperShouldHaveComplementaryAggregation()
    {
        SearchParameters testParam = new SearchParameters();
        testParam.addFieldFacet(new SearchParameters.FieldFacet(FIELD_SITE));

        List<TermsAggregationWrapper> wrappers = elasticsearchAggregationBuilder
                .termsAggregations(testParam, elasticsearchAFTSQueryBuilder).toList();

        assertFalse("SITE facet should produce at least one wrapper", wrappers.isEmpty());

        Optional<ComplementaryAggregation> complementary = wrappers.get(0).complementaryAggregation();
        assertTrue("SITE wrapper must contain a complementary aggregation for _REPOSITORY_",
                complementary.isPresent());
        assertEquals("Complementary aggregation display label must be _REPOSITORY_",
                "_REPOSITORY_", complementary.get().displayLabel());
        assertTrue("Complementary aggregation name must include the main aggregation name as prefix",
                complementary.get().aggregationName().startsWith(wrappers.get(0).name()));
    }

    @Test
    public void givenSiteFacet_aggregationWrapperShouldHavePostProcessingDataWithSharedFilesEntry()
    {
        SearchParameters testParam = new SearchParameters();
        testParam.addFieldFacet(new SearchParameters.FieldFacet(FIELD_SITE));

        List<TermsAggregationWrapper> wrappers = elasticsearchAggregationBuilder
                .termsAggregations(testParam, elasticsearchAFTSQueryBuilder).toList();

        assertFalse("SITE facet should produce at least one wrapper", wrappers.isEmpty());

        Map<String, String> postProcessingData = wrappers.get(0).postProcessingData();
        assertFalse("Post-processing data must not be empty", postProcessingData.isEmpty());
        assertTrue("Post-processing data must map a UUID to _SHARED_FILES_",
                postProcessingData.containsValue("_SHARED_FILES_"));
    }

    @Test
    public void givenMixedFacets_siteFacetShouldNotAffectOtherFacets()
    {
        SearchParameters testParam = new SearchParameters();
        testParam.addFieldFacet(new SearchParameters.FieldFacet(FIELD_SITE));
        testParam.addFieldFacet(new SearchParameters.FieldFacet(CREATOR_FIELD));

        List<TermsAggregationWrapper> wrappers = elasticsearchAggregationBuilder
                .termsAggregations(testParam, elasticsearchAFTSQueryBuilder).toList();

        // Locate the cm:creator wrapper (non-SITE)
        Optional<TermsAggregationWrapper> creatorWrapper = wrappers.stream()
                .filter(w -> CREATOR_FIELD.equals(w.name()))
                .findFirst();

        assertTrue("cm:creator facet should still produce a wrapper alongside SITE facet",
                creatorWrapper.isPresent());
        assertFalse("cm:creator wrapper must not have a complementary aggregation",
                creatorWrapper.get().complementaryAggregation().isPresent());
        assertTrue("cm:creator wrapper post-processing data must be empty",
                creatorWrapper.get().postProcessingData().isEmpty());
    }

    private long countFacetsWithSameLimit(int limit, List<TermsAggregationWrapper> termsAggregationBuilders)
    {
        return termsAggregationBuilders.stream()
                .filter(item -> item.termsAggregation().size() == limit)
                .count();
    }

    private SearchParameters createSearchParametersWithDefaultFacetFields(List<String> facetNames)
    {
        SearchParameters testParam = new SearchParameters();
        facetNames.forEach(facetName -> testParam.addFieldFacet(new SearchParameters.FieldFacet(facetName)));
        return testParam;
    }
}
