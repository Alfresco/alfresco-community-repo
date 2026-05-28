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
import org.alfresco.repo.search.impl.elasticsearch.ElasticsearchSearchServiceFactoryTest;
import org.alfresco.repo.search.impl.elasticsearch.ElasticsearchSearchServiceTest;
import org.alfresco.repo.search.impl.elasticsearch.ElasticsearchTagSupportIT;
import org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.ContentModelSynchronizerTest;
import org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.ElasticsearchIndexServiceIT;
import org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.ElasticsearchInitialiserIT;
import org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.ElasticsearchInitialiserTest;
import org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.FieldMappingBuilderTest;
import org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.IndexConfigurationInitializerIntegrationTest;
import org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.IndexingIT;
import org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.config.ElasticsearchExactTermSearchConfigIT;
import org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.config.ElasticsearchFieldAnalyzersConfigIT;
import org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.field.mapper.predefined.DateFieldMapperTest;
import org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.field.mapper.predefined.PathFieldMapperTest;
import org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.field.mapper.predefined.SimpleFieldMapperTest;
import org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.field.mapper.predefined.TextFieldMapperTest;
import org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.utils.ResourceUtilsTest;
import org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.utils.SettingsJsonUtilsTest;
import org.alfresco.repo.search.impl.elasticsearch.model.FieldNameTest;
import org.alfresco.repo.search.impl.elasticsearch.permission.FlatElasticsearchPermissionQueryFactoryTest;
import org.alfresco.repo.search.impl.elasticsearch.query.AspectQueryIT;
import org.alfresco.repo.search.impl.elasticsearch.query.ClassQueryIT;
import org.alfresco.repo.search.impl.elasticsearch.query.ElasticsearchQueryHelperTest;
import org.alfresco.repo.search.impl.elasticsearch.query.FilterAggregationsIT;
import org.alfresco.repo.search.impl.elasticsearch.query.FilterQueryIT;
import org.alfresco.repo.search.impl.elasticsearch.query.IgnoreUnsupportedPropertyIT;
import org.alfresco.repo.search.impl.elasticsearch.query.MultiValueFieldQueryIT;
import org.alfresco.repo.search.impl.elasticsearch.query.PathQueryIT;
import org.alfresco.repo.search.impl.elasticsearch.query.QueryByIdIT;
import org.alfresco.repo.search.impl.elasticsearch.query.ResultSetIT;
import org.alfresco.repo.search.impl.elasticsearch.query.SiteQueryIT;
import org.alfresco.repo.search.impl.elasticsearch.query.SortIT;
import org.alfresco.repo.search.impl.elasticsearch.query.StoreRefStripperTest;
import org.alfresco.repo.search.impl.elasticsearch.query.TagQueryIT;
import org.alfresco.repo.search.impl.elasticsearch.query.TermsAggregationsIT;
import org.alfresco.repo.search.impl.elasticsearch.query.TypeQueryIT;
import org.alfresco.repo.search.impl.elasticsearch.query.highlight.ElasticsearchHighlightBuilderTest;
import org.alfresco.repo.search.impl.elasticsearch.query.language.afts.AFTSBooleanOperatorsAdaptorTest;
import org.alfresco.repo.search.impl.elasticsearch.query.language.afts.ElasticsearchAFTSQueryBuilderTest;
import org.alfresco.repo.search.impl.elasticsearch.query.language.afts.ElasticsearchQueryParserAdaptorTest;
import org.alfresco.repo.search.impl.elasticsearch.query.language.afts.ExactTermQueryIT;
import org.alfresco.repo.search.impl.elasticsearch.query.language.afts.IndexLocaleAnalyzerIT;
import org.alfresco.repo.search.impl.elasticsearch.query.language.lucene.EscapeCharacterTest;
import org.alfresco.repo.search.impl.elasticsearch.query.language.lucene.FuzzyQueryTest;
import org.alfresco.repo.search.impl.elasticsearch.query.language.lucene.PrefixQueryTest;
import org.alfresco.repo.search.impl.elasticsearch.query.language.lucene.RangeQueryTest;
import org.alfresco.repo.search.impl.elasticsearch.query.language.lucene.TermOrPhraseQueryTest;
import org.alfresco.repo.search.impl.elasticsearch.query.language.lucene.WildcardQueryTest;
import org.alfresco.repo.search.impl.elasticsearch.resultset.AggregationNameUtilTest;
import org.alfresco.repo.search.impl.elasticsearch.resultset.ElasticsearchResultSetBuilderTest;
import org.alfresco.repo.search.impl.elasticsearch.resultset.HighlightsHandlerTest;

/**
 * Community Elasticsearch tests suite.
 */
@RunWith(Categories.class)
@Suite.SuiteClasses({
        // Content model sync
        ElasticsearchInitialiserTest.class,
        ElasticsearchInitialiserIT.class,
        ElasticsearchIndexServiceIT.class,
        ContentModelSynchronizerTest.class,
        FieldMappingBuilderTest.class,
        IndexConfigurationInitializerIntegrationTest.class,
        IndexingIT.class,
        ElasticsearchExactTermSearchConfigIT.class,
        ElasticsearchFieldAnalyzersConfigIT.class,
        SettingsJsonUtilsTest.class,
        ResourceUtilsTest.class,

        // Field mappers
        DateFieldMapperTest.class,
        PathFieldMapperTest.class,
        SimpleFieldMapperTest.class,
        TextFieldMapperTest.class,

        // Search service
        ElasticsearchSearchServiceTest.class,
        ElasticsearchSearchServiceFactoryTest.class,
        ElasticsearchCategoryServiceTest.class,
        ElasticsearchTagSupportIT.class,

        // Model
        FieldNameTest.class,

        // Permissions
        FlatElasticsearchPermissionQueryFactoryTest.class,

        // Query building
        AFTSBooleanOperatorsAdaptorTest.class,
        ElasticsearchAFTSQueryBuilderTest.class,
        ElasticsearchQueryParserAdaptorTest.class,
        ElasticsearchQueryHelperTest.class,
        StoreRefStripperTest.class,
        ElasticsearchHighlightBuilderTest.class,

        // Result sets
        ElasticsearchResultSetBuilderTest.class,
        AggregationNameUtilTest.class,
        HighlightsHandlerTest.class,

        // Query ITs
        FilterQueryIT.class,
        QueryByIdIT.class,
        FilterAggregationsIT.class,
        TermsAggregationsIT.class,
        ResultSetIT.class,
        SiteQueryIT.class,
        TagQueryIT.class,
        TypeQueryIT.class,
        MultiValueFieldQueryIT.class,
        PathQueryIT.class,
        AspectQueryIT.class,
        ClassQueryIT.class,
        IgnoreUnsupportedPropertyIT.class,
        SortIT.class,

        // Lucene unit tests
        FuzzyQueryTest.class,
        PrefixQueryTest.class,
        RangeQueryTest.class,
        TermOrPhraseQueryTest.class,
        WildcardQueryTest.class,
        EscapeCharacterTest.class,

        // language = afts ITs
        ExactTermQueryIT.class,
        IndexLocaleAnalyzerIT.class,
        org.alfresco.repo.search.impl.elasticsearch.query.language.afts.TermQueryIT.class,
        org.alfresco.repo.search.impl.elasticsearch.query.language.afts.PhraseQueryIT.class,
        org.alfresco.repo.search.impl.elasticsearch.query.language.afts.WildcardQueryIT.class,
        org.alfresco.repo.search.impl.elasticsearch.query.language.afts.RangeQueryIT.class,
        org.alfresco.repo.search.impl.elasticsearch.query.language.afts.DateMathQueryIT.class,
        org.alfresco.repo.search.impl.elasticsearch.query.language.afts.BooleanQueryIT.class,
        org.alfresco.repo.search.impl.elasticsearch.query.language.afts.ContentAndContentMetadataIT.class,
        org.alfresco.repo.search.impl.elasticsearch.query.language.afts.PermissionQueryIT.class,
        org.alfresco.repo.search.impl.elasticsearch.query.language.afts.EscapeCharacterIT.class,

        // language = lucene ITs
        org.alfresco.repo.search.impl.elasticsearch.query.language.lucene.TermQueryIT.class,
        org.alfresco.repo.search.impl.elasticsearch.query.language.lucene.WildcardQueryIT.class,
        org.alfresco.repo.search.impl.elasticsearch.query.language.lucene.PhraseQueryIT.class,
        org.alfresco.repo.search.impl.elasticsearch.query.language.lucene.RangeQueryIT.class,
        org.alfresco.repo.search.impl.elasticsearch.query.language.lucene.BooleanQueryIT.class,
        org.alfresco.repo.search.impl.elasticsearch.query.language.lucene.ContentAndContentMetadataIT.class,
        org.alfresco.repo.search.impl.elasticsearch.query.language.lucene.EscapeCharacterIT.class,
        org.alfresco.repo.search.impl.elasticsearch.query.language.lucene.FieldQueryIT.class,
        org.alfresco.repo.search.impl.elasticsearch.query.language.lucene.PermissionQueryIT.class,

        // language = cmis ITs
        org.alfresco.repo.search.impl.elasticsearch.query.language.cmis.EscapeCharacterIT.class,
        org.alfresco.repo.search.impl.elasticsearch.query.language.cmis.TermQueryIT.class,
        org.alfresco.repo.search.impl.elasticsearch.query.language.cmis.PhraseQueryIT.class,
        org.alfresco.repo.search.impl.elasticsearch.query.language.cmis.WildcardQueryIT.class
})

public class ElasticsearchCommunityTestsTestSuite
{}
