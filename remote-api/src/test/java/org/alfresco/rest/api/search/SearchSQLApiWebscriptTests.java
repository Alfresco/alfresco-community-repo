/*-
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
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
package org.alfresco.rest.api.search;

import static java.util.Collections.emptyList;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;

import java.util.Collections;

import org.alfresco.rest.api.search.model.SearchSQLQuery;
import org.alfresco.service.cmr.search.SearchParameters;
import org.junit.Test;

/**
 * Tests the SearchSQLApiWebscript class
 *
 * @author Michael Suzuki
 */
public class SearchSQLApiWebscriptTests
{
    static SearchSQLApiWebscript webscript = new SearchSQLApiWebscript();

    @Test
    public void testSearchQueryParams() throws Exception
    {
        String query = "select SITE from alfresco";
        SearchSQLQuery searchQuery = new SearchSQLQuery(query, "solr", emptyList(), 1000, false, "", emptyList());
        SearchParameters sparams = webscript.buildSearchParameters(searchQuery);
        
        assertNotNull(sparams);
        assertEquals(query, sparams.getQuery());
        assertEquals(false, sparams.isIncludeMetadata());
        assertEquals(Collections.EMPTY_LIST, sparams.getLocales());
        assertEquals("solr",sparams.getExtraParameters().get("format"));
        assertEquals(null, sparams.getTimezone());
    }
    @Test
    public void testSearchQueryParamsTimezone() throws Exception
    {
        String query = "select SITE from alfresco";
        SearchSQLQuery searchQuery = new SearchSQLQuery(query, "solr", emptyList(), 1000, false, "Israel", emptyList());
        SearchParameters sparams = webscript.buildSearchParameters(searchQuery);
        
        assertNotNull(sparams);
        assertEquals(query, sparams.getQuery());
        assertEquals(false, sparams.isIncludeMetadata());
        assertEquals(Collections.EMPTY_LIST, sparams.getLocales());
        assertEquals("solr", sparams.getExtraParameters().get("format"));
        assertEquals("Israel", sparams.getTimezone());
    }
    @Test
    public void testSearchQueryParamsFormatNull() throws Exception
    {
        String query = "select SITE from alfresco";
        SearchSQLQuery searchQuery = new SearchSQLQuery(query, "", emptyList(), 1000, false, "", emptyList());
        SearchParameters sparams = webscript.buildSearchParameters(searchQuery);
        
        assertNotNull(sparams);
        assertEquals(query, sparams.getQuery());
        assertEquals(false, sparams.isIncludeMetadata());
        assertEquals(Collections.EMPTY_LIST, sparams.getLocales());
        assertEquals(null, sparams.getExtraParameters().get("format"));
        assertEquals(null, sparams.getTimezone());
        
        searchQuery = new SearchSQLQuery(query, null, emptyList(), 1000, true, "", emptyList());
        sparams = webscript.buildSearchParameters(searchQuery);
        
        assertNotNull(sparams);
        assertEquals(query, sparams.getQuery());
        assertEquals(true, sparams.isIncludeMetadata());
        assertEquals(Collections.EMPTY_LIST, sparams.getLocales());
        assertEquals(null, sparams.getExtraParameters().get("format"));
        assertEquals(null, sparams.getTimezone());
    }
    @Test
    public void testSearchQueryNullStmt() throws Exception
    {
        SearchSQLQuery searchQuery = new SearchSQLQuery(null, "solr", emptyList(), null, false, null, emptyList());
        try
        {
            webscript.buildSearchParameters(searchQuery);
        }
        catch (Exception e) 
        {
            assertEquals(true, e.getMessage().contains("Required stmt parameter is missing."));
        }
    }


}
