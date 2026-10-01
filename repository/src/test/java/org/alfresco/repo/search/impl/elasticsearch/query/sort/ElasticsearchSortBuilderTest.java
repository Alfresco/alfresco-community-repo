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
package org.alfresco.repo.search.impl.elasticsearch.query.sort;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.opensearch.client.opensearch._types.SortOptions;
import org.opensearch.client.opensearch._types.SortOrder;

import org.alfresco.repo.dictionary.IndexTokenisationMode;
import org.alfresco.repo.dictionary.NamespaceDAO;
import org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.IndexConfigurationInitializer;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.namespace.NamespaceService;

public class ElasticsearchSortBuilderTest
{
    private ElasticsearchSortBuilder sortBuilder;
    private PropertyDefinition propertyDefinition;

    @Before
    public void setUp()
    {
        NamespaceDAO namespaceDAO = mock(NamespaceDAO.class);
        when(namespaceDAO.getNamespaceURI(NamespaceService.CONTENT_MODEL_PREFIX)).thenReturn(NamespaceService.CONTENT_MODEL_1_0_URI);
        when(namespaceDAO.getNamespaceURI(NamespaceService.SYSTEM_MODEL_PREFIX)).thenReturn(NamespaceService.SYSTEM_MODEL_1_0_URI);
        when(namespaceDAO.getPrefixes(NamespaceService.CONTENT_MODEL_1_0_URI)).thenReturn(List.of(NamespaceService.CONTENT_MODEL_PREFIX));
        when(namespaceDAO.getPrefixes(NamespaceService.SYSTEM_MODEL_1_0_URI)).thenReturn(List.of(NamespaceService.SYSTEM_MODEL_PREFIX));

        propertyDefinition = mock(PropertyDefinition.class);
        when(propertyDefinition.isIndexed()).thenReturn(true);
        when(propertyDefinition.getIndexTokenisationMode()).thenReturn(IndexTokenisationMode.FALSE);

        DictionaryService dictionaryService = mock(DictionaryService.class);
        when(dictionaryService.getProperty(any())).thenReturn(propertyDefinition);

        sortBuilder = new ElasticsearchSortBuilder(namespaceDAO, dictionaryService, mock(IndexConfigurationInitializer.class));
    }

    @Test
    public void shouldAppendNodeDbidToFieldSort()
    {
        SearchParameters searchParameters = searchParametersWithSort(SearchParameters.SortDefinition.SortType.FIELD, "cm:name", false);

        List<SortOptions> sorts = sortBuilder.getSortBuilders(searchParameters);

        assertEquals(2, sorts.size());
        assertFieldSort(sorts.get(0), "cm%3Aname_untokenized", SortOrder.Desc);
        assertFieldSort(sorts.get(1), "sys%3Anode%2Ddbid_untokenized", SortOrder.Asc);
    }

    @Test
    public void shouldNotDuplicateNodeDbidSort()
    {
        SearchParameters searchParameters = searchParametersWithSort(SearchParameters.SortDefinition.SortType.FIELD, "sys:node-dbid", true);

        List<SortOptions> sorts = sortBuilder.getSortBuilders(searchParameters);

        assertEquals(1, sorts.size());
        assertFieldSort(sorts.get(0), "sys%3Anode%2Ddbid_untokenized", SortOrder.Asc);
    }

    @Test
    public void shouldSortByNodeDbidDespiteRepositoryDictionaryIndexFlag()
    {
        when(propertyDefinition.isIndexed()).thenReturn(false);
        when(propertyDefinition.getIndexTokenisationMode()).thenReturn(IndexTokenisationMode.TRUE);
        SearchParameters searchParameters = searchParametersWithSort(SearchParameters.SortDefinition.SortType.FIELD, "sys:node-dbid", true);

        List<SortOptions> sorts = sortBuilder.getSortBuilders(searchParameters);

        assertEquals(1, sorts.size());
        assertFieldSort(sorts.get(0), "sys%3Anode%2Ddbid_untokenized", SortOrder.Asc);
    }

    @Test
    public void shouldAppendNodeDbidToScoreSort()
    {
        SearchParameters searchParameters = searchParametersWithSort(SearchParameters.SortDefinition.SortType.SCORE, null, false);

        List<SortOptions> sorts = sortBuilder.getSortBuilders(searchParameters);

        assertEquals(2, sorts.size());
        assertTrue(sorts.get(0).isScore());
        assertFieldSort(sorts.get(1), "sys%3Anode%2Ddbid_untokenized", SortOrder.Asc);
    }

    @Test
    public void shouldPreserveDocumentSortWithoutTieBreaker()
    {
        SearchParameters searchParameters = searchParametersWithSort(SearchParameters.SortDefinition.SortType.DOCUMENT, null, true);

        List<SortOptions> sorts = sortBuilder.getSortBuilders(searchParameters);

        assertEquals(1, sorts.size());
        assertFieldSort(sorts.get(0), "_doc", SortOrder.Asc);
    }

    private SearchParameters searchParametersWithSort(SearchParameters.SortDefinition.SortType type, String field, boolean ascending)
    {
        SearchParameters searchParameters = new SearchParameters();
        searchParameters.addSort(new SearchParameters.SortDefinition(type, field, ascending));
        return searchParameters;
    }

    private void assertFieldSort(SortOptions sort, String field, SortOrder order)
    {
        assertTrue(sort.isField());
        assertEquals(field, sort.field().field());
        assertEquals(order, sort.field().order());
    }
}
