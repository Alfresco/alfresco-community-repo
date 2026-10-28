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

package org.alfresco;

import org.junit.experimental.categories.Categories;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import org.alfresco.repo.search.impl.elasticsearch.ElasticsearchCategoryServiceTest;
import org.alfresco.repo.search.impl.elasticsearch.ElasticsearchSearchServiceTest;
import org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.ContentModelSynchronizerTest;
import org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.ElasticsearchInitialiserTest;
import org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.FieldMappingBuilderTest;
import org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.IndexConfigurationInitializerIntegrationTest;
import org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.config.ElasticsearchExactTermSearchConfigIT;
import org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.field.mapper.predefined.DateFieldMapperTest;
import org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.field.mapper.predefined.PathFieldMapperTest;
import org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.field.mapper.predefined.SimpleFieldMapperTest;
import org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.field.mapper.predefined.TextFieldMapperTest;
import org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.utils.ResourceUtilsTest;
import org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.utils.SettingsJsonUtilsTest;
import org.alfresco.repo.search.impl.elasticsearch.model.FieldNameTest;
import org.alfresco.repo.search.impl.elasticsearch.permission.FlatElasticsearchPermissionQueryFactoryTest;
import org.alfresco.repo.search.impl.elasticsearch.query.ElasticsearchQueryHelperTest;
import org.alfresco.repo.search.impl.elasticsearch.query.FilterAggregationsIT;
import org.alfresco.repo.search.impl.elasticsearch.query.MultiValueFieldQueryIT;
import org.alfresco.repo.search.impl.elasticsearch.query.QueryByIdIT;
import org.alfresco.repo.search.impl.elasticsearch.query.SiteQueryIT;
import org.alfresco.repo.search.impl.elasticsearch.query.StoreRefStripperTest;
import org.alfresco.repo.search.impl.elasticsearch.query.TagQueryIT;
import org.alfresco.repo.search.impl.elasticsearch.query.aggregation.ElasticsearchAggregationBuilderTest;
import org.alfresco.repo.search.impl.elasticsearch.query.aggregation.SiteMembershipCacheEvictionListenerTest;
import org.alfresco.repo.search.impl.elasticsearch.query.aggregation.SiteTermsAggregationBuilderTest;
import org.alfresco.repo.search.impl.elasticsearch.query.filter.DefaultAliveStateFilterBuilderTest;
import org.alfresco.repo.search.impl.elasticsearch.query.highlight.ElasticsearchHighlightBuilderTest;
import org.alfresco.repo.search.impl.elasticsearch.query.language.afts.AFTSBooleanOperatorsAdaptorTest;
import org.alfresco.repo.search.impl.elasticsearch.query.language.afts.ElasticsearchAFTSQueryBuilderTest;
import org.alfresco.repo.search.impl.elasticsearch.query.language.afts.ElasticsearchQueryParserAdaptorTest;
import org.alfresco.repo.search.impl.elasticsearch.query.language.lucene.EscapeCharacterTest;
import org.alfresco.repo.search.impl.elasticsearch.query.language.lucene.FuzzyQueryTest;
import org.alfresco.repo.search.impl.elasticsearch.query.language.lucene.PrefixQueryTest;
import org.alfresco.repo.search.impl.elasticsearch.query.language.lucene.RangeQueryTest;
import org.alfresco.repo.search.impl.elasticsearch.query.language.lucene.TermOrPhraseQueryTest;
import org.alfresco.repo.search.impl.elasticsearch.query.language.lucene.WildcardQueryTest;
import org.alfresco.repo.search.impl.elasticsearch.query.sort.ElasticsearchSortBuilderTest;
import org.alfresco.repo.search.impl.elasticsearch.resultset.AggregationHandlerTest;
import org.alfresco.repo.search.impl.elasticsearch.resultset.AggregationNameUtilTest;
import org.alfresco.repo.search.impl.elasticsearch.resultset.ElasticsearchResultSetBuilderTest;
import org.alfresco.repo.search.impl.elasticsearch.resultset.HighlightsHandlerTest;

/**
 * Elasticsearch community tests, part 1 of 3: the unit tests plus part of the integration tests.
 * <p>
 * The suite is split only to shorten CI: each part runs as its own job. {@link ElasticsearchCommunityTestsTestSuite} still runs all parts.
 */
@RunWith(Categories.class)
@Suite.SuiteClasses({
        // Content model sync
        ElasticsearchInitialiserTest.class,
        ContentModelSynchronizerTest.class,
        FieldMappingBuilderTest.class,
        IndexConfigurationInitializerIntegrationTest.class,
        ElasticsearchExactTermSearchConfigIT.class,
        SettingsJsonUtilsTest.class,
        ResourceUtilsTest.class,

        // Field mappers
        DateFieldMapperTest.class,
        PathFieldMapperTest.class,
        SimpleFieldMapperTest.class,
        TextFieldMapperTest.class,

        // Search service
        ElasticsearchSearchServiceTest.class,
        ElasticsearchCategoryServiceTest.class,

        // Model
        FieldNameTest.class,

        // Permissions
        FlatElasticsearchPermissionQueryFactoryTest.class,

        // Filters
        DefaultAliveStateFilterBuilderTest.class,

        // Query building
        AFTSBooleanOperatorsAdaptorTest.class,
        ElasticsearchAFTSQueryBuilderTest.class,
        ElasticsearchQueryParserAdaptorTest.class,
        ElasticsearchQueryHelperTest.class,
        StoreRefStripperTest.class,
        ElasticsearchHighlightBuilderTest.class,
        ElasticsearchSortBuilderTest.class,

        // Result sets
        ElasticsearchResultSetBuilderTest.class,
        AggregationNameUtilTest.class,
        HighlightsHandlerTest.class,
        AggregationHandlerTest.class,

        // Aggregations
        ElasticsearchAggregationBuilderTest.class,
        SiteTermsAggregationBuilderTest.class,
        SiteMembershipCacheEvictionListenerTest.class,

        // Query ITs
        QueryByIdIT.class,
        FilterAggregationsIT.class,
        SiteQueryIT.class,
        TagQueryIT.class,
        MultiValueFieldQueryIT.class,

        // Lucene unit tests
        FuzzyQueryTest.class,
        PrefixQueryTest.class,
        RangeQueryTest.class,
        TermOrPhraseQueryTest.class,
        WildcardQueryTest.class,
        EscapeCharacterTest.class,

        // language = afts ITs
        org.alfresco.repo.search.impl.elasticsearch.query.language.afts.TermQueryIT.class,
        org.alfresco.repo.search.impl.elasticsearch.query.language.afts.PermissionQueryIT.class,

        // language = lucene ITs
        org.alfresco.repo.search.impl.elasticsearch.query.language.lucene.ContentAndContentMetadataIT.class,
        org.alfresco.repo.search.impl.elasticsearch.query.language.lucene.EscapeCharacterIT.class,
        org.alfresco.repo.search.impl.elasticsearch.query.language.lucene.PermissionQueryIT.class,

        // language = cmis ITs
        org.alfresco.repo.search.impl.elasticsearch.query.language.cmis.EscapeCharacterIT.class,
        org.alfresco.repo.search.impl.elasticsearch.query.language.cmis.PhraseQueryIT.class
})

public class ElasticsearchCommunityTestsPart1TestSuite
{}
