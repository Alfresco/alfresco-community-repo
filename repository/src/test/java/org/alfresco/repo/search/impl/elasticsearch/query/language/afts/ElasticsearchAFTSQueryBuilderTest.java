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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import junit.framework.TestCase;
import org.apache.lucene.queryparser.classic.ParseException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.opensearch.client.opensearch._types.query_dsl.BoolQuery;
import org.opensearch.client.opensearch._types.query_dsl.Query;

import org.alfresco.repo.search.impl.elasticsearch.util.MockNamespaceService;
import org.alfresco.repo.search.impl.elasticsearch.query.language.EsTypeResolver;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

@RunWith(MockitoJUnitRunner.class)
public class ElasticsearchAFTSQueryBuilderTest extends TestCase
{
    private DictionaryService dictionaryService;
    private SiteService siteService;

    private MockNamespaceService namespace;

    private EsTypeResolver esTypeResolver;

    @Before
    public void setup()
    {
        this.namespace = new MockNamespaceService();
        namespace.registerNamespace(NamespaceService.CONTENT_MODEL_PREFIX, NamespaceService.CONTENT_MODEL_1_0_URI);
        namespace.registerNamespace(NamespaceService.DICTIONARY_MODEL_PREFIX, NamespaceService.DICTIONARY_MODEL_1_0_URI);

        dictionaryService = mock(DictionaryService.class);

        var captor = ArgumentCaptor.forClass(QName.class);
        when(dictionaryService.getProperty(captor.capture())).thenAnswer((Answer<PropertyDefinition>) invocation -> {
            captor.getValue();
            var propertyDefinition = mock(PropertyDefinition.class);
            when(propertyDefinition.getName()).thenReturn(captor.getValue());
            return propertyDefinition;
        });

        esTypeResolver = mock(EsTypeResolver.class);

        siteService = mock(SiteService.class);
    }

    @Test
    public void shouldGetSameQuery_whenThereIsOnlyOneWord() throws ParseException
    {
        AFTSQueryBuilder elasticsearchAFTSQueryBuilder = givenAFTSQueryBuilder();
        SearchParameters searchParameters = new SearchParameters();
        searchParameters.addTextAttribute("cm:content");
        searchParameters.setQuery("test");

        Query ftsQuery = elasticsearchAFTSQueryBuilder.getQuery(searchParameters);

        assertThat(ftsQuery.queryString().query(), is("(cm%3Acontent:test)"));
    }

    @Test
    public void shouldGetBooleanShouldQuery_whenThereAreMultipleWords() throws ParseException
    {
        AFTSQueryBuilder elasticsearchAFTSQueryBuilder = givenAFTSQueryBuilder();
        SearchParameters searchParameters = new SearchParameters();
        searchParameters.addTextAttribute("cm:content");
        searchParameters.setQuery("test anotherTest");
        Query ftsQuery = elasticsearchAFTSQueryBuilder.getQuery(searchParameters);
        BoolQuery query = ftsQuery.bool();
        assertEquals(2, query.should().size());
    }

    @Test
    public void shouldGetAndOperator_whenDefaultOperatorIsAnd() throws ParseException
    {
        AFTSQueryBuilder elasticsearchAFTSQueryBuilder = givenAFTSQueryBuilder();
        SearchParameters searchParameters = new SearchParameters();
        searchParameters.addTextAttribute("cm:content");
        searchParameters.setQuery("test anotherTest");
        searchParameters.setDefaultOperator(SearchParameters.Operator.AND);
        Query ftsQuery = elasticsearchAFTSQueryBuilder.getQuery(searchParameters);
        BoolQuery query = ftsQuery.bool();
        assertEquals(2, query.must().size());
        assertThat(query.must().get(0).queryString().query(), is("+(cm%3Acontent:test)"));
        assertThat(query.must().get(1).queryString().query(), is("+(cm%3Acontent:anotherTest)"));
    }

    @Test
    public void shouldGetSameQuery_whenThereIsOnlyOneWordA() throws ParseException
    {
        AFTSQueryBuilder elasticsearchAFTSQueryBuilder = givenAFTSQueryBuilder();
        SearchParameters searchParameters = new SearchParameters();
        searchParameters.setQuery("test");

        Query ftsQuery = elasticsearchAFTSQueryBuilder.getQuery(searchParameters);

        assertThat(ftsQuery.queryString().query(), is("(cm%3Acontent:test cm%3Aname:test cm%3Adescription:test cm%3Atitle:test)"));
    }

    @Test
    public void shouldGetRangeQuery_whenTodayTermIsUsedOnCreatedField() throws ParseException
    {
        when(esTypeResolver.resolve(any())).thenReturn(Optional.of("date"));
        AFTSQueryBuilder elasticsearchAFTSQueryBuilder = givenAFTSQueryBuilder();
        SearchParameters searchParameters = new SearchParameters();
        searchParameters.setQuery("created:today");

        Query ftsQuery = elasticsearchAFTSQueryBuilder.getQuery(searchParameters);

        assertThat(ftsQuery.queryString().query(), is("cm%3Acreated:[now/d TO now+1d/d}"));
    }

    @Test
    public void shouldGetRangeQuery_whenTodayTermIsUsedOnModifiedField() throws ParseException
    {
        when(esTypeResolver.resolve(any())).thenReturn(Optional.of("date"));
        AFTSQueryBuilder elasticsearchAFTSQueryBuilder = givenAFTSQueryBuilder();
        SearchParameters searchParameters = new SearchParameters();
        searchParameters.setQuery("modified:today");

        Query ftsQuery = elasticsearchAFTSQueryBuilder.getQuery(searchParameters);

        assertThat(ftsQuery.queryString().query(), is("cm%3Amodified:[now/d TO now+1d/d}"));
    }

    @Test
    public void shouldGetFieldQuery_whenTodayTermIsUsedOnTextField() throws ParseException
    {
        when(esTypeResolver.resolve(any())).thenReturn(Optional.of("text"));
        AFTSQueryBuilder elasticsearchAFTSQueryBuilder = givenAFTSQueryBuilder();
        SearchParameters searchParameters = new SearchParameters();
        searchParameters.setQuery("name:today");

        Query ftsQuery = elasticsearchAFTSQueryBuilder.getQuery(searchParameters);

        assertThat(ftsQuery.queryString().query(), is("cm%3Aname:today"));
    }

    private AFTSQueryBuilder givenAFTSQueryBuilder()
    {
        return new AFTSQueryBuilder(namespace, dictionaryService, null, siteService, esTypeResolver);
    }
}
