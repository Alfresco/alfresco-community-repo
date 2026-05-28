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
package org.alfresco.repo.search.impl.elasticsearch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import static org.alfresco.service.cmr.search.SearchService.LANGUAGE_FTS_ALFRESCO;
import static org.alfresco.service.cmr.search.SearchService.LANGUAGE_LUCENE;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import org.alfresco.repo.search.QueryRegisterComponent;
import org.alfresco.repo.search.SearcherException;
import org.alfresco.repo.search.impl.lucene.LuceneQueryLanguageSPI;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;

@RunWith(MockitoJUnitRunner.class)
public class ElasticsearchSearchServiceTest
{
    private static final String LANGUAGE = LANGUAGE_LUCENE;

    private ElasticsearchSearchService searchService;

    @Mock
    private QueryRegisterComponent queryRegister;

    @Mock
    private LuceneQueryLanguageSPI queryLanguage;

    @Mock
    private NodeService nodeService;

    @Mock
    private DictionaryService dictionaryService;

    @Mock
    private SearchParameters searchParameters;

    @Mock
    private ResultSet resultSet;

    @Before
    public void setUp() throws Exception
    {
        Map<String, LuceneQueryLanguageSPI> languages = new HashMap<>();
        languages.put(LANGUAGE.toLowerCase(Locale.getDefault()), queryLanguage);

        searchService = new ElasticsearchSearchService(queryRegister, languages, nodeService, dictionaryService);

        when(searchParameters.getLanguage()).thenReturn(LANGUAGE);
        when(queryLanguage.executeQuery(any(SearchParameters.class))).thenReturn(resultSet);
    }

    @Test
    public void queryWithSearchParameters_shouldExecuteQueryUsingMatchingLanguage()
    {
        searchService.query(searchParameters);

        verify(queryLanguage, only()).executeQuery(any());
    }

    @Test
    public void queryWithSearchParameters_shouldThrowWhenLanguageIsUnknown()
    {
        when(searchParameters.getLanguage()).thenReturn("chupacabra");

        assertThatExceptionOfType(SearcherException.class)
                .isThrownBy(() -> searchService.query(searchParameters))
                .withMessageContaining("Unknown query language: chupacabra");

    }

    @Test
    public void luceneQueryWithSecondaryPath_isTransformedToAFTSTagQuery()
    {
        when(searchParameters.getQuery()).thenReturn("+PATH:\"/cm:taggable/cm:alfresco/member\"");

        searchService.query(searchParameters);

        verify(searchParameters, times(1)).setQuery("TAG:alfresco");
        verify(searchParameters, times(1)).setLanguage(LANGUAGE_FTS_ALFRESCO);
    }

    @Test
    public void luceneQueryWithOptionalSecondaryPathQuery_isTransformedToAFTSTagQuery()
    {
        when(searchParameters.getQuery()).thenReturn("FIELD:value PATH:\"/cm:taggable/cm:alfresco/member\"");

        searchService.query(searchParameters);

        verify(searchParameters, times(1)).setQuery("FIELD:value TAG:alfresco");
        verify(searchParameters, times(1)).setLanguage(LANGUAGE_FTS_ALFRESCO);
    }

    @Test
    public void luceneQueryWithMultipleSecondaryPaths_isTransformedToAFTSTagQuery()
    {
        when(searchParameters.getQuery()).thenReturn("+PATH:\"/cm:taggable/cm:alfresco/member\" +PATH:\"/cm:taggable/cm:nuxeo/member\"");

        searchService.query(searchParameters);

        verify(searchParameters, times(1)).setQuery("TAG:alfresco TAG:nuxeo");
        verify(searchParameters, times(1)).setLanguage(LANGUAGE_FTS_ALFRESCO);
    }

    @Test
    public void luceneQueryWithLogicalOperatorAndSecondaryPaths_isTransformedToAFTSTagQuery()
    {
        when(searchParameters.getQuery()).thenReturn("FIELD:value AND (+PATH:\"/cm:taggable/cm:alfresco/member\" OR +PATH:\"/cm:taggable/cm:nuxeo/member\")");

        searchService.query(searchParameters);

        verify(searchParameters, times(1)).setQuery("FIELD:value AND (TAG:alfresco OR TAG:nuxeo)");
        verify(searchParameters, times(1)).setLanguage(LANGUAGE_FTS_ALFRESCO);
    }

    @Test
    public void luceneQuery_whenNotMatchingTagQuery_shouldNotBeModified()
    {
        List<String> queries = List.of(
                // different field is used, instead of PATH
                "+NAME:\"/cm:taggable/cm:alfresco/member\"",

                // node path not in taggable
                "+PATH:\"/cm:other/cm:alfresco/member\"",

                // different namespace instead of cm
                "+PATH:\"/he:taggable/ll:alfresco/member\"",

                // missing +PATH
                "\"/cm:taggable/cm:alfresco/member\"",

                // missing member in node path
                "+PATH:\"/cm:taggable/cm:alfresco\"",

                // something other than member in node path
                "+PATH:\"/cm:taggable/cm:alfresco/user\"");

        assertThat(queries).allSatisfy(query -> {
            reset(searchParameters);
            when(searchParameters.getLanguage()).thenReturn(LANGUAGE);
            when(searchParameters.getQuery()).thenReturn(query);

            searchService.query(searchParameters);

            verify(searchParameters, never()).setQuery(any());
            verify(searchParameters, never()).setLanguage(LANGUAGE_FTS_ALFRESCO);
        });
    }
}
