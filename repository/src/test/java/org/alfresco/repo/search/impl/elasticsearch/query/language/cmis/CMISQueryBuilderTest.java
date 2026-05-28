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
package org.alfresco.repo.search.impl.elasticsearch.query.language.cmis;

import static java.util.Arrays.asList;
import static java.util.Collections.singleton;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.chemistry.opencmis.commons.enums.CapabilityJoin;
import org.apache.lucene.queryparser.classic.ParseException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opensearch.client.opensearch._types.query_dsl.Query;

import org.alfresco.opencmis.dictionary.CMISDictionaryService;
import org.alfresco.opencmis.search.CMISQueryOptions;
import org.alfresco.opencmis.search.CMISQueryParser;
import org.alfresco.opencmis.search.CmisFunctionEvaluationContext;
import org.alfresco.repo.search.impl.querymodel.Column;
import org.alfresco.repo.search.impl.querymodel.Function;
import org.alfresco.repo.search.impl.querymodel.FunctionEvaluationContext;
import org.alfresco.repo.search.impl.querymodel.Order;
import org.alfresco.repo.search.impl.querymodel.Ordering;
import org.alfresco.repo.search.impl.querymodel.PropertyArgument;
import org.alfresco.repo.search.impl.querymodel.QueryModelFactory;
import org.alfresco.repo.search.impl.querymodel.Source;
import org.alfresco.repo.search.impl.querymodel.impl.functions.PropertyAccessor;
import org.alfresco.repo.search.impl.querymodel.impl.lucene.LuceneQuery;
import org.alfresco.repo.search.impl.querymodel.impl.lucene.QueryBuilderContext;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchParameters.SortDefinition;

/** Unit tests for {@link CMISQueryBuilder}. */
@RunWith(MockitoJUnitRunner.class)
public class CMISQueryBuilderTest
{
    /** A store to use for the tests. */
    private static final StoreRef STORE_REF = new StoreRef("fake://store");
    /** This is the default return value when no selector groups are provided in the query. */
    public static final Set<String> DEFAULT_SELECTOR_GROUPS = singleton("");
    @InjectMocks
    private CMISQueryBuilder cmisQueryBuilder;
    @Mock
    private SearchParameters searchParameters;
    @Mock
    private CMISQueryParserFactory cmisQueryParserFactory;
    @Mock
    private CMISQueryParser cmisQueryParser;
    @Mock
    private LuceneQuery queryModel;
    @Mock
    private Source source;
    @Mock
    private Query queryBuilder;
    @Mock
    private CMISDictionaryService cmisDictionaryService;
    @Mock
    private CmisFunctionEvaluationContext functionContext;
    @Mock
    private Ordering ordering;
    @Mock
    private Column column;
    @Mock
    private Function function;
    @Mock
    private PropertyArgument argument;
    @Mock
    private Ordering secondOrdering;
    @Mock
    private Column secondColumn;
    @Mock
    private Function secondFunction;
    @Mock
    private PropertyArgument secondArgument;
    @Captor
    private ArgumentCaptor<SortDefinition> sortDefinition;

    @Before
    public void setUp()
    {
        openMocks(this);

        when(searchParameters.getStores()).thenReturn(new ArrayList<>(List.of(STORE_REF)));
        when(cmisQueryParserFactory.makeParser(any(CMISQueryOptions.class), eq(cmisDictionaryService), any(CapabilityJoin.class))).thenReturn(cmisQueryParser);
        when(cmisQueryParser.parse(any(QueryModelFactory.class), any(FunctionEvaluationContext.class))).thenReturn(queryModel);
        when(queryModel.getSource()).thenReturn(source);
        when(source.getSelectorGroups(any(FunctionEvaluationContext.class))).thenReturn(asList(DEFAULT_SELECTOR_GROUPS));

        when(ordering.getColumn()).thenReturn(column);
        when(column.getFunction()).thenReturn(function);
        when(function.getName()).thenReturn(PropertyAccessor.NAME);
        when(functionContext.getLuceneFieldName("propertyName")).thenReturn("luceneFieldName");
        when(argument.getPropertyName()).thenReturn("propertyName");

        when(secondOrdering.getColumn()).thenReturn(secondColumn);
        when(secondColumn.getFunction()).thenReturn(secondFunction);
        when(secondFunction.getName()).thenReturn(PropertyAccessor.NAME);
        when(secondColumn.getFunctionArguments()).thenReturn(Map.of(PropertyAccessor.ARG_PROPERTY, secondArgument));
    }

    @Test
    public void testGetQuery() throws Throwable
    {
        when(queryModel.buildQuery(eq(DEFAULT_SELECTOR_GROUPS), any(QueryBuilderContext.class), any(FunctionEvaluationContext.class))).thenReturn(queryBuilder);

        Query actual = cmisQueryBuilder.getQuery(searchParameters);

        assertEquals("Unexpected return value.", queryBuilder, actual);
    }

    @Test
    public void testGetQuery_ParseException() throws Throwable
    {
        when(queryModel.buildQuery(eq(DEFAULT_SELECTOR_GROUPS), any(QueryBuilderContext.class), any(FunctionEvaluationContext.class))).thenThrow(new ParseException());

        try
        {
            cmisQueryBuilder.getQuery(searchParameters);
            fail("Expected to receive ParseException from buildQuery.");
        }
        catch (ParseException e)
        {
            // Expect to receive an exception.
        }
    }

    @Test
    public void testSortQueryWhenOrderingIsNull()
    {
        cmisQueryBuilder.sortQuery(queryModel, functionContext, searchParameters);
        verifyNoInteractions(searchParameters);
    }

    @Test(expected = IllegalStateException.class)
    public void testSortWhenMissingPropertyName()
    {
        when(queryModel.getOrderings()).thenReturn(List.of(ordering));
        cmisQueryBuilder.sortQuery(queryModel, functionContext, searchParameters);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testUnsupportedSortingMethod()
    {
        when(queryModel.getOrderings()).thenReturn(List.of(ordering));

        // Bypassing setUp method to force exception
        when(function.getName()).thenReturn("unsupportedMethod");
        cmisQueryBuilder.sortQuery(queryModel, functionContext, searchParameters);
    }

    @Test
    public void testSortAscending()
    {
        when(queryModel.getOrderings()).thenReturn(List.of(ordering));
        when(column.getFunctionArguments()).thenReturn(Map.of(PropertyAccessor.ARG_PROPERTY, argument));
        when(ordering.getOrder()).thenReturn(Order.ASCENDING);

        cmisQueryBuilder.sortQuery(queryModel, functionContext, searchParameters);

        verify(searchParameters).addSort(sortDefinition.capture());
        SortDefinition actual = sortDefinition.getValue();
        assertEquals("luceneFieldName", actual.getField());
        assertTrue("Expected ascending order", actual.isAscending());
    }

    @Test
    public void testSortDescending()
    {
        when(queryModel.getOrderings()).thenReturn(List.of(ordering));
        when(column.getFunctionArguments()).thenReturn(Map.of(PropertyAccessor.ARG_PROPERTY, argument));
        when(ordering.getOrder()).thenReturn(Order.DESCENDING);

        cmisQueryBuilder.sortQuery(queryModel, functionContext, searchParameters);

        verify(searchParameters).addSort(sortDefinition.capture());
        SortDefinition actual = sortDefinition.getValue();
        assertEquals("luceneFieldName", actual.getField());
        assertTrue("Expected descending order", !actual.isAscending());
    }

    /** Check that we can sort by one column descending and then a second column ascending. */
    @Test
    public void testMultipleFieldSort()
    {
        when(queryModel.getOrderings()).thenReturn(List.of(ordering, secondOrdering));
        when(column.getFunctionArguments()).thenReturn(Map.of(PropertyAccessor.ARG_PROPERTY, argument));
        when(ordering.getOrder()).thenReturn(Order.DESCENDING);
        when(secondOrdering.getOrder()).thenReturn(Order.ASCENDING);
        when(functionContext.getLuceneFieldName("propertyName")).thenReturn("luceneFieldName");
        when(argument.getPropertyName()).thenReturn("propertyName");

        // Set up second column for sorting.
        when(secondArgument.getPropertyName()).thenReturn("secondPropertyName");
        when(functionContext.getLuceneFieldName("secondPropertyName")).thenReturn("secondLuceneName");

        cmisQueryBuilder.sortQuery(queryModel, functionContext, searchParameters);

        verify(searchParameters, times(2)).addSort(sortDefinition.capture());

        List<SortDefinition> actual = sortDefinition.getAllValues();
        assertEquals("luceneFieldName", actual.get(0).getField());
        assertFalse("Expected descending order", actual.get(0).isAscending());
        assertEquals("secondLuceneName", actual.get(1).getField());
        assertTrue("Expected ascending order", actual.get(1).isAscending());
    }
}
