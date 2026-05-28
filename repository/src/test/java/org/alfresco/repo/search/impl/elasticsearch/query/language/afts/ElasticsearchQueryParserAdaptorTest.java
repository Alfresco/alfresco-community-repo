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
package org.alfresco.repo.search.impl.elasticsearch.query.language.afts;

import static java.util.Collections.singletonList;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.hamcrest.core.IsNull.nullValue;
import static org.hamcrest.core.StringContains.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import static org.alfresco.repo.search.adaptor.QueryConstants.FIELD_ANCESTOR;
import static org.alfresco.repo.search.adaptor.QueryConstants.FIELD_PARENT;
import static org.alfresco.repo.search.adaptor.QueryConstants.FIELD_PRIMARYPARENT;
import static org.alfresco.repo.search.impl.elasticsearch.query.language.lucene.LuceneQueryParser.PRIMARY_HIERARCHY_FIELD;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.apache.lucene.queryparser.classic.ParseException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.opensearch.client.opensearch._types.FieldValue;
import org.opensearch.client.opensearch._types.query_dsl.BoolQuery;
import org.opensearch.client.opensearch._types.query_dsl.MatchAllQuery;
import org.opensearch.client.opensearch._types.query_dsl.MatchQuery;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.opensearch.client.opensearch._types.query_dsl.QueryBuilders;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.search.adaptor.AnalysisMode;
import org.alfresco.repo.search.adaptor.LuceneFunction;
import org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.config.ElasticsearchExactTermSearchConfig;
import org.alfresco.repo.search.impl.elasticsearch.query.language.EsTypeResolver;
import org.alfresco.repo.search.impl.elasticsearch.query.language.lucene.QueryConverter;
import org.alfresco.repo.search.impl.elasticsearch.util.MockNamespaceService;
import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

@SuppressWarnings("PMD.TooManyMethods")
@RunWith(MockitoJUnitRunner.class)
public class ElasticsearchQueryParserAdaptorTest
{
    private AFTSQueryParserAdaptor aftsQueryParserAdaptor;
    private DictionaryService dictionaryService;
    private EsTypeResolver esTypeResolver;
    private ElasticsearchExactTermSearchConfig exactTermSearchConfig;

    @Before
    public void setup() throws IOException
    {
        var resolver = new MockNamespaceService();
        resolver.registerNamespace(NamespaceService.CONTENT_MODEL_PREFIX, NamespaceService.CONTENT_MODEL_1_0_URI);
        resolver.registerNamespace(NamespaceService.DICTIONARY_MODEL_PREFIX, NamespaceService.DICTIONARY_MODEL_1_0_URI);

        QueryConverter.setAnalyzeWildcardFields("cm:content,cm:persondescription,cm:preferenceValues,cm:tagScopeCache,sys:keyStore,sys:versionEdition,sys:versionProperties");

        dictionaryService = mock(DictionaryService.class);
        esTypeResolver = mock(EsTypeResolver.class);
        exactTermSearchConfig = mock(ElasticsearchExactTermSearchConfig.class);

        var captor = ArgumentCaptor.forClass(QName.class);
        when(dictionaryService.getProperty(captor.capture())).thenAnswer((Answer<PropertyDefinition>) invocation -> {
            captor.getValue();
            var propertyDefinition = mock(PropertyDefinition.class);
            when(propertyDefinition.getName()).thenReturn(captor.getValue());
            return propertyDefinition;
        });

        SiteService siteService = mock(SiteService.class);

        SearchParameters searchParameter = new SearchParameters();
        aftsQueryParserAdaptor = new AFTSQueryParserAdaptor(searchParameter, resolver, dictionaryService, siteService, exactTermSearchConfig,
                esTypeResolver);
    }

    @Test
    public void shouldGetRangeQueryWhenTermIsTodayIgnoringCaseAndFieldIsDate() throws ParseException
    {
        when(esTypeResolver.resolve(any())).thenReturn(Optional.of("date"));

        for (String field : List.of("created", "modified"))
        {
            for (String term : List.of("TODAY", "today", "ToDaY", "tOdAy"))
            {
                String fieldName = "{http://www.alfresco.org/model/content/1.0}" + field;

                Query aftsQuery = aftsQueryParserAdaptor.getFieldQuery(fieldName, term);

                String expected = "cm%3A" + field + ":[now/d TO now+1d/d}";
                assertThat(aftsQuery.queryString().query(), is(expected));
            }
        }
    }

    @Test
    public void shouldGetFieldQueryWhenTermIsTodayIgnoringCaseAndFieldIsNotDate() throws ParseException
    {
        when(esTypeResolver.resolve(any())).thenReturn(Optional.of("text"));

        for (String field : List.of("name", "userName", "publisher"))
        {
            for (String term : List.of("TODAY", "today", "ToDaY", "tOdAy"))
            {
                String fieldName = "{http://www.alfresco.org/model/content/1.0}" + field;

                Query aftsQuery = aftsQueryParserAdaptor.getFieldQuery(fieldName, term);

                String expected = "cm%3A" + field + ":" + term;
                assertThat(aftsQuery.queryString().query(), is(expected));
            }
        }
    }

    @Test
    public void shouldGetFieldQueryWhenFieldNameIsAGenericProperty() throws ParseException
    {
        Query aftsQuery = aftsQueryParserAdaptor
                .getFieldQuery("{http://www.alfresco.org/model/content/1.0}name", "test");

        assertThat(aftsQuery.queryString().query(), is("cm%3Aname:test"));
    }

    @Test
    public void shouldGetFieldQueryWhenFieldNameIsContent() throws ParseException
    {
        Query queryBuilderFullName = aftsQueryParserAdaptor
                .getFieldQuery("{http://www.alfresco.org/model/content/1.0}content",
                        "test");
        Query queryBuilderField = aftsQueryParserAdaptor.getFieldQuery("cm:content", "test");

        assertThat(queryBuilderFullName.queryString().query(), is("cm%3Acontent:test"));
        assertThat(queryBuilderField.queryString().query(), is("cm%3Acontent:test"));
    }

    @Test
    public void shouldGetFieldQueryWhenFieldNameIsDefaultAttributes() throws ParseException
    {
        Query queryBuilderNull = aftsQueryParserAdaptor.getFieldQuery(null, "test");
        Query queryBuilderTEXT = aftsQueryParserAdaptor.getFieldQuery("TEXT", "test");

        assertThat(queryBuilderNull.queryString().query(), is("(cm%3Acontent:test cm%3Aname:test cm%3Adescription:test cm%3Atitle:test)"));
        assertThat(queryBuilderTEXT.queryString().query(), is("(cm%3Acontent:test cm%3Aname:test cm%3Adescription:test cm%3Atitle:test)"));
    }

    @Test
    public void dateMathConversion()
    {
        Query rangeQuery = aftsQueryParserAdaptor.getRangeQuery("cm:created", "NOW-1MONTH/DAY", "NOW+2WEEKS/DAY", true, false, AnalysisMode.DEFAULT, LuceneFunction.FIELD);
        assertThat(rangeQuery.queryString().query(), is("cm%3Acreated:[now-1M/d TO now+2w/d}"));
    }

    /** Check that a query by id is converted into a match query. */
    @Test
    public void getIdentifierQuery_returnsMatchQuery()
    {
        Query query = aftsQueryParserAdaptor.getIdentifierQuery("ID", "4aaeca6f-b407-45f9-a4bf-ebf33e387e01", AnalysisMode.DEFAULT, LuceneFunction.FIELD);

        assertThat(query, instanceOf(Query.class));
        Query queryStringQuery = query;
        assertThat(queryStringQuery.queryString().query(), containsString("_id:4aaeca6f-b407-45f9-a4bf-ebf33e387e01"));
    }

    /** Check when searching for private working copy of document that we get no results. */
    @Test
    public void getIdentifierQueryWithPWC_returnsNoResults()
    {
        Query query = aftsQueryParserAdaptor.getIdentifierQuery("ID", "4aaeca6f-b407-45f9-a4bf-ebf33e387e01;pwc", AnalysisMode.DEFAULT, LuceneFunction.FIELD);

        assertThat(query, instanceOf(Query.class));
        BoolQuery boolQuery = query.bool();
        assertThat(boolQuery.mustNot().get(0).matchAll(), instanceOf(MatchAllQuery.class));
    }

    /** Check that appending a version causes the query to include a term for the version label. */
    @Test
    public void getIdentifierQueryWithVersion2_1()
    {
        Query query = aftsQueryParserAdaptor.getIdentifierQuery("ID", "4aaeca6f-b407-45f9-a4bf-ebf33e387e01;2.1", AnalysisMode.DEFAULT, LuceneFunction.FIELD);

        assertThat(query, instanceOf(Query.class));
        BoolQuery boolQuery = query.bool();
        List<Query> mustClauses = boolQuery.must();
        Query idClause = mustClauses.get(0);
        assertThat(idClause.queryString().query(), containsString("_id:4aaeca6f-b407-45f9-a4bf-ebf33e387e01"));

        BoolQuery expectedVersionClause = boolQuery.toBuilder().should(new Query.Builder().match(new MatchQuery.Builder().field("@" + ContentModel.PROP_VERSION_LABEL).query(FieldValue.of("2.1")).build()).build()).build();
        assertThat(mustClauses.get(1).bool().should().get(0).match().field(), is(expectedVersionClause.should().get(0).match().field()));
    }

    /** Version 1.0 is a special case since unversionable nodes are implicitly version 1.0. */
    @Test
    public void getIdentifierQueryWithVersion1_0()
    {
        // Set up dictionary service to handle versionable aspect as a passthrough.
        AspectDefinition mockVersionableAspect = mock(AspectDefinition.class);
        when(mockVersionableAspect.getName()).thenReturn(ContentModel.ASPECT_VERSIONABLE);
        when(dictionaryService.getAspect(ContentModel.ASPECT_VERSIONABLE)).thenReturn(mockVersionableAspect);
        when(dictionaryService.getSubAspects(ContentModel.ASPECT_VERSIONABLE, true)).thenReturn(singletonList(ContentModel.ASPECT_VERSIONABLE));

        // Call the method under test.
        Query query = aftsQueryParserAdaptor.getIdentifierQuery("ID", "4aaeca6f-b407-45f9-a4bf-ebf33e387e01;1.0", AnalysisMode.DEFAULT, LuceneFunction.FIELD);

        // Check that there's a clause to match the id.
        assertThat(query.bool().toBuilder(), instanceOf(BoolQuery.Builder.class));
        BoolQuery.Builder boolQuery = query.bool().toBuilder();
        List<Query> mustClauses = boolQuery.build().must();
        Query idClause = mustClauses.get(0);
        assertThat(idClause.queryString().query(), containsString("_id:4aaeca6f-b407-45f9-a4bf-ebf33e387e01"));
        // Check that there's a clause to optionally match version 1.0.
        List<Query> versionClauses = mustClauses.get(1).bool().should();
        MatchQuery expectedVersionClause = QueryBuilders.match().field("@" + ContentModel.PROP_VERSION_LABEL).query(FieldValue.of("1.0")).build();
        assertThat(versionClauses.get(0).match().field(), is(expectedVersionClause.field()));
        assertThat(versionClauses.get(0).match().query()._toJsonString(), is(expectedVersionClause.query()._toJsonString()));
        // ...and another clause to optionally match nodes without the versionable aspect.
        List<Query> mustNotClauses = versionClauses.get(1).bool().mustNot();
        Query aspectClause = mustNotClauses.get(0);
        assertThat(aspectClause.queryString().query(), is("ASPECT:cm\\:versionable"));
    }

    /** Check that a descendant query by id is converted into a match query. */
    @Test
    public void getDescendantQuery_returnsMatchQuery()
    {
        Query query = aftsQueryParserAdaptor.getFieldQuery(PRIMARY_HIERARCHY_FIELD, "4aaeca6f-b407-45f9-a4bf-ebf33e387e01", AnalysisMode.DEFAULT, LuceneFunction.FIELD);

        assertThat(query, instanceOf(Query.class));
        Query queryStringQuery = query;
        assertThat(queryStringQuery.queryString().query(), containsString("cm%3AprimaryHierarchy:4aaeca6f-b407-45f9-a4bf-ebf33e387e01"));
    }

    /** Check that a query by primary parent id is converted into a match query. */
    @Test
    public void getPrimaryParentQuery_returnsMatchQuery()
    {
        Query query = aftsQueryParserAdaptor.getFieldQuery(FIELD_PRIMARYPARENT, "4aaeca6f-b407-45f9-a4bf-ebf33e387e01", AnalysisMode.DEFAULT, LuceneFunction.FIELD);

        assertThat(query, instanceOf(Query.class));
        Query queryStringQuery = query;
        assertThat(queryStringQuery.queryString().query(), containsString("PRIMARYPARENT:4aaeca6f-b407-45f9-a4bf-ebf33e387e01"));
    }

    /** Check that a query by parent id is converted into a match query. */
    @Test
    public void testGetFieldQuery_returnsParentMatchQuery()
    {
        Query query = aftsQueryParserAdaptor.getFieldQuery(FIELD_PARENT, "4aaeca6f-b407-45f9-a4bf-ebf33e387e01", AnalysisMode.DEFAULT, LuceneFunction.FIELD);

        assertThat(query, instanceOf(Query.class));
        Query queryStringQuery = query;
        assertThat(queryStringQuery.queryString().query(), containsString("PARENT:4aaeca6f-b407-45f9-a4bf-ebf33e387e01"));
    }

    /** Check that a query by ancestor id is converted into a match query. */
    @Test
    public void testGetFieldQuery_returnsAncestorMatchQuery()
    {
        Query query = aftsQueryParserAdaptor.getFieldQuery(FIELD_ANCESTOR, "4aaeca6f-b407-45f9-a4bf-ebf33e387e01", AnalysisMode.DEFAULT, LuceneFunction.FIELD);

        assertThat(query, instanceOf(Query.class));
        Query queryStringQuery = query;
        assertThat(queryStringQuery.queryString().query(), containsString("ANCESTOR:4aaeca6f-b407-45f9-a4bf-ebf33e387e01"));
    }

    /** Check that a returned query contains the exact term in the given field if the type is `text`. */
    @Test
    public void getExactTermSearch()
    {
        when(esTypeResolver.resolve(any())).thenReturn(Optional.of("text"));
        when(exactTermSearchConfig.isExactTermSearchEnabled(any())).thenReturn(true);

        Query aftsQuery = aftsQueryParserAdaptor
                .getFieldQuery("{http://www.alfresco.org/model/content/1.0}name", "test", AnalysisMode.IDENTIFIER, LuceneFunction.FIELD);

        assertThat(aftsQuery.queryString().query(), is("cm%3Aname_exact:test"));
    }

    /** Check that a returned query contains the exact term in the given field if the type cannot be read from ES. */
    @Test
    public void shouldGetExactTermSearchWhenFieldTypeCannotBeFound()
    {
        when(esTypeResolver.resolve(any())).thenReturn(Optional.empty());
        when(exactTermSearchConfig.isExactTermSearchEnabled(any())).thenReturn(true);

        Query aftsQuery = aftsQueryParserAdaptor
                .getFieldQuery("{http://www.alfresco.org/model/content/1.0}name", "test", AnalysisMode.IDENTIFIER, LuceneFunction.FIELD);

        assertThat(aftsQuery.queryString().query(), is("cm%3Aname_exact:test"));
    }

    /** Check that a returned query doesn't contain the exact term in the given field if the type isn't `text`. */
    @Test
    public void shouldNotGetExactTermSearch() throws IOException
    {
        when(esTypeResolver.resolve(any())).thenReturn(Optional.of("date"));

        Query aftsQuery = aftsQueryParserAdaptor
                .getFieldQuery("{http://www.alfresco.org/model/content/1.0}created", "test", AnalysisMode.IDENTIFIER, LuceneFunction.FIELD);

        assertThat(aftsQuery.queryString().query(), is("cm%3Acreated:test"));
    }

    @Test
    public void shouldStripStoreRefIfRequired()
    {
        Query aftsQuery = aftsQueryParserAdaptor
                .getFieldQuery("@{http://www.alfresco.org/model/content/1.0}categories", "workspace://SpacesStore/741231c5-e2e1-4434-9231-c5e2e1343450", AnalysisMode.DEFAULT, 0, LuceneFunction.FIELD);

        assertThat(aftsQuery.queryString().query(), is("cm%3Acategories:741231c5-e2e1-4434-9231-c5e2e1343450"));
    }

    @Test
    public void testIfStrippingStoreRefIsNotRequired()
    {
        Query aftsQuery = aftsQueryParserAdaptor
                .getFieldQuery("@{http://www.alfresco.org/model/content/1.0}categories", "821231c5-e2e1-4434-9231-c5e2e1343450", AnalysisMode.DEFAULT, 0, LuceneFunction.FIELD);

        assertThat(aftsQuery.queryString().query(), is("cm%3Acategories:821231c5-e2e1-4434-9231-c5e2e1343450"));
    }

    @Test
    public void testTagFieldValueIfHyphenIsThere()
    {
        Query aftsQuery = aftsQueryParserAdaptor.getFieldQuery("TAG", "-00");
        assertThat(aftsQuery.queryString().query(), is("TAG:\\-00"));
    }

    @Test
    public void testFieldValueIfNoHyphenIsThere()
    {
        Query aftsQuery = aftsQueryParserAdaptor.getFieldQuery("TAG", "00");
        assertThat(aftsQuery.queryString().query(), is("TAG:00"));
    }

    @Test
    public void shouldSetAnalyzeWildcardTrueForCmContentField()
    {
        Query aftsQuery = aftsQueryParserAdaptor.getFieldQuery("cm:content", "gosling*", AnalysisMode.DEFAULT, 0, LuceneFunction.FIELD);
        assertThat(aftsQuery.queryString().query(), is("cm%3Acontent:gosling*"));
        assertThat(aftsQuery.queryString().analyzeWildcard(), is(true));
    }

    @Test
    public void shouldSetAnalyzeWildcardTrueForFullQNameContentField()
    {
        Query aftsQuery = aftsQueryParserAdaptor.getFieldQuery("@{http://www.alfresco.org/model/content/1.0}content", "gosling*", AnalysisMode.DEFAULT, 0, LuceneFunction.FIELD);
        assertThat(aftsQuery.queryString().query(), is("cm%3Acontent:gosling*"));
        assertThat(aftsQuery.queryString().analyzeWildcard(), is(true));
    }

    @Test
    public void shouldNotSetAnalyzeWildcardForOtherFields()
    {
        Query aftsQuery = aftsQueryParserAdaptor.getFieldQuery("cm:name", "gosling*", AnalysisMode.DEFAULT, 0, LuceneFunction.FIELD);
        assertThat(aftsQuery.queryString().query(), is("cm%3Aname:gosling*"));
        assertThat(aftsQuery.queryString().analyzeWildcard(), is(nullValue()));
    }
}
