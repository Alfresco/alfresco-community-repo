/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2020 Alfresco Software Limited
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
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

import org.junit.Before;
import org.junit.Test;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchParameters;

public class ResultSetIT extends LuceneOrAFTSQueryIT
{
    private static final int ALL_DOCUMENTS_COUNT = 20;

    public ResultSetIT(String language)
    {
        super(language);
    }

    @Before
    public void setup() throws Exception
    {
        IntStream.range(0, ALL_DOCUMENTS_COUNT)
                .forEachOrdered(i -> indexDocument("the content"));
    }

    @Test
    public void whenQueryingUsingSkipCount_shouldReturnsDifferentPages()
    {
        SearchParameters searchParams = new SearchParameters();
        searchParams.setLanguage(language);
        searchParams.setQuery("TEXT:content");
        searchParams.setLimit(10);
        searchParams.setSkipCount(0);
        searchParams.addStore(new StoreRef(StoreRef.PROTOCOL_WORKSPACE + StoreRef.URI_FILLER));

        ResultSet rsFirstPage = aftsQueryExecutor.executeQuery(searchParams);

        // ask for the second page
        searchParams.setSkipCount(10);
        ResultSet rsSecondPage = aftsQueryExecutor.executeQuery(searchParams);

        List<NodeRef> firstPage = StreamSupport.stream(rsFirstPage.spliterator(), false).map(ResultSetRow::getNodeRef)
                .collect(Collectors.toList());
        List<NodeRef> secondPage = StreamSupport.stream(rsSecondPage.spliterator(), false).map(ResultSetRow::getNodeRef)
                .collect(Collectors.toList());

        assertEquals("First page should have limited amount of results", 10, rsFirstPage.length());
        assertEquals("Total number of hits should match all documents", ALL_DOCUMENTS_COUNT, rsFirstPage.getNumberFound());

        assertEquals("Second page should have limited amount of results", 10, rsSecondPage.length());
        assertEquals("Total number of hits should match all documents", ALL_DOCUMENTS_COUNT, rsSecondPage.getNumberFound());

        // check that the second page is not contained in the first page
        assertFalse("The pages need to have different results", firstPage.containsAll(secondPage));
    }

    @Test
    public void whenSearchForATermWithLimit()
    {
        SearchParameters searchParams = new SearchParameters();
        searchParams.setQuery("content");
        searchParams.setLanguage(language);
        searchParams.setLimit(10);
        searchParams.setSkipCount(0);
        searchParams.addStore(new StoreRef(StoreRef.PROTOCOL_WORKSPACE + StoreRef.URI_FILLER));

        var executor = "lucene".equals(language) ? luceneQueryExecutor : aftsQueryExecutor;
        ResultSet rs = executor.executeQuery(searchParams);

        assertEquals("Results should be limited", 10, rs.length());
        assertEquals("Total number of hits should match all documents", ALL_DOCUMENTS_COUNT, rs.getNumberFound());
    }

    @Test
    public void whenSearchForATermWithTrackHitsEnabled()
    {
        SearchParameters searchParams = new SearchParameters();
        searchParams.setQuery("content");
        searchParams.setLanguage(language);
        searchParams.setTrackTotalHits(12);
        searchParams.addStore(new StoreRef(StoreRef.PROTOCOL_WORKSPACE + StoreRef.URI_FILLER));

        var executor = "lucene".equals(language) ? luceneQueryExecutor : aftsQueryExecutor;
        ResultSet rs = executor.executeQuery(searchParams);

        assertEquals("Total number of hits should be limited when trackTotalHits is set", 12, rs.getNumberFound());
    }

    @Test
    public void whenSearchForATermWithTrackHitsEnabledNoLimit()
    {
        SearchParameters searchParams = new SearchParameters();
        searchParams.setQuery("content");
        searchParams.setLanguage(language);
        searchParams.setTrackTotalHits(-1);
        searchParams.addStore(new StoreRef(StoreRef.PROTOCOL_WORKSPACE + StoreRef.URI_FILLER));

        var executor = "lucene".equals(language) ? luceneQueryExecutor : aftsQueryExecutor;
        ResultSet rs = executor.executeQuery(searchParams);

        assertEquals("Total number of hits should match all documents when -1", ALL_DOCUMENTS_COUNT, rs.getNumberFound());
    }

    @Test
    public void whenSearchForATermWithTrackHitsEnabledWithZero()
    {
        SearchParameters searchParams = new SearchParameters();
        searchParams.setQuery("content");
        searchParams.setLanguage(language);
        searchParams.setTrackTotalHits(0);
        searchParams.addStore(new StoreRef(StoreRef.PROTOCOL_WORKSPACE + StoreRef.URI_FILLER));

        var executor = "lucene".equals(language) ? luceneQueryExecutor : aftsQueryExecutor;
        ResultSet rs = executor.executeQuery(searchParams);

        // Default value is 10.000, so we should get all hits
        assertEquals("Total number of hits should match all documents up to default value", ALL_DOCUMENTS_COUNT, rs.getNumberFound());
    }

}
