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
package org.alfresco.repo.search.impl.elasticsearch.query.aggregation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import static org.alfresco.repo.search.adaptor.QueryConstants.FIELD_SITE;

import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import org.alfresco.repo.dictionary.NamespaceDAO;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.search.SearchParameters;

/**
 * Unit tests for {@link ElasticsearchAggregationBuilder}, focused on the SITE-facet dispatch to {@link SiteTermsAggregationBuilder}.
 * <p>
 * Detailed SITE aggregation behaviour (include lists, complementary aggregation, Shared Files resolution, etc.) is covered by {@link SiteTermsAggregationBuilderTest}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ElasticsearchAggregationBuilderTest
{
    @Mock
    private NamespaceDAO namespaceDAO;
    @Mock
    private DictionaryService dictionaryService;
    @Mock
    private SiteTermsAggregationBuilder siteTermsAggregationBuilder;

    private ElasticsearchAggregationBuilder builder;

    @Before
    public void setUp()
    {
        builder = new ElasticsearchAggregationBuilder(namespaceDAO, dictionaryService, siteTermsAggregationBuilder);
        builder.setDefaultFacetLimit(10);
    }

    @Test
    public void termsAggregations_withSiteFacet_delegatesToSiteTermsAggregationBuilder()
    {
        SearchParameters.FieldFacet facet = new SearchParameters.FieldFacet(FIELD_SITE);
        SearchParameters params = new SearchParameters();
        params.addFieldFacet(facet);

        TermsAggregationWrapper expectedWrapper = mock(TermsAggregationWrapper.class);
        when(siteTermsAggregationBuilder.build(facet)).thenReturn(Optional.of(expectedWrapper));

        List<TermsAggregationWrapper> wrappers = builder.termsAggregations(params, null).toList();

        assertEquals(1, wrappers.size());
        assertSame("The wrapper produced by SiteTermsAggregationBuilder must be returned as-is",
                expectedWrapper, wrappers.get(0));
    }

    @Test
    public void termsAggregations_withLuceneSyntaxSiteFacet_delegatesToSiteTermsAggregationBuilder()
    {
        // Lucene syntax prefixes field names with '@'; this must still be detected as SITE.
        SearchParameters.FieldFacet facet = new SearchParameters.FieldFacet("@" + FIELD_SITE);
        SearchParameters params = new SearchParameters();
        params.addFieldFacet(facet);

        when(siteTermsAggregationBuilder.build(facet)).thenReturn(Optional.of(mock(TermsAggregationWrapper.class)));

        List<TermsAggregationWrapper> wrappers = builder.termsAggregations(params, null).toList();

        assertEquals(1, wrappers.size());
        verify(siteTermsAggregationBuilder).build(facet);
    }

    @Test
    public void termsAggregations_withSiteFacet_emptyResultFromSiteBuilder_producesEmptyStream()
    {
        SearchParameters.FieldFacet facet = new SearchParameters.FieldFacet(FIELD_SITE);
        SearchParameters params = new SearchParameters();
        params.addFieldFacet(facet);

        when(siteTermsAggregationBuilder.build(facet)).thenReturn(Optional.empty());

        List<TermsAggregationWrapper> wrappers = builder.termsAggregations(params, null).toList();

        assertTrue("No wrapper should be produced when SiteTermsAggregationBuilder returns empty", wrappers.isEmpty());
    }

    @Test
    public void termsAggregations_withNonSiteFacet_doesNotDelegateToSiteTermsAggregationBuilder()
    {
        // PATH is one of AlfrescoFunctionEvaluationContext's EXPOSED_FIELDS, so it resolves
        // without needing real namespace/dictionary lookups.
        SearchParameters.FieldFacet facet = new SearchParameters.FieldFacet("PATH");
        SearchParameters params = new SearchParameters();
        params.addFieldFacet(facet);

        List<TermsAggregationWrapper> wrappers = builder.termsAggregations(params, null).toList();

        assertEquals(1, wrappers.size());
        verifyNoInteractions(siteTermsAggregationBuilder);
    }
}
