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

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;
import static junit.framework.TestCase.fail;
import static org.alfresco.service.cmr.search.SearchService.LANGUAGE_CMIS_ALFRESCO;
import static org.alfresco.service.cmr.search.SearchService.LANGUAGE_FTS_ALFRESCO;
import static org.alfresco.service.cmr.search.SearchService.LANGUAGE_LUCENE;
import org.alfresco.rest.api.search.impl.SearchMapper;
import org.alfresco.rest.api.search.model.Query;
import org.alfresco.rest.api.search.model.SearchQuery;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.rest.framework.resource.parameters.Paging;
import org.alfresco.rest.framework.resource.parameters.Params;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchParameters;
import org.junit.Test;

/**
 * Tests the SearchMapper class
 *
 * @author Gethin James
 */
public class SearchMapperTests
{

    static SearchMapper searchMapper = new SearchMapper();

    @Test(expected = IllegalArgumentException.class)
    public void testMandatory() throws Exception
    {
        Params params = Params.valueOf(null, null, new SearchQuery(), null);
        SearchParameters searchParameters = searchMapper.toSearchParameters(params);
    }

    @Test
    public void toSearchParameters() throws Exception
    {

        Params params = Params.valueOf(null, null, minimalQuery(), null);
        SearchParameters searchParameters = searchMapper.toSearchParameters(params);
        assertNotNull(searchParameters);

        //Test defaults
        assertEquals("There should be only 1 default store", 1,searchParameters.getStores().size());
        assertEquals("workspaces store is the default", StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, searchParameters.getStores().get(0));
    }

    @Test
    public void fromQuery() throws Exception
    {
        SearchParameters searchParameters = new SearchParameters();
        try
        {
            searchMapper.fromQuery(searchParameters, new Query());
            fail();
        } catch (IllegalArgumentException iae)
        {
            assertTrue(iae.getLocalizedMessage().contains("query is a mandatory parameter"));
        }

        Query q = new Query();
        q.setQuery("hello");

        try
        {
            searchMapper.fromQuery(searchParameters, q);
            fail();
        } catch (IllegalArgumentException iae)
        {
            assertTrue(iae.getLocalizedMessage().contains("language is a mandatory parameter"));
        }

        q.setLanguage("world");

        try
        {
            searchMapper.fromQuery(searchParameters, q);
            fail();
        } catch (InvalidArgumentException iae)
        {
            assertNotNull(iae);
            //world is not a valid language type
        }

        q.setLanguage("afts");
        searchMapper.fromQuery(searchParameters, q);
        assertEquals(LANGUAGE_FTS_ALFRESCO, searchParameters.getLanguage());

        q.setLanguage("cMiS");
        searchMapper.fromQuery(searchParameters, q);
        assertEquals(LANGUAGE_CMIS_ALFRESCO, searchParameters.getLanguage());

        q.setLanguage("LuCENE");
        searchMapper.fromQuery(searchParameters, q);
        assertEquals(LANGUAGE_LUCENE, searchParameters.getLanguage());

        assertEquals("hello", searchParameters.getQuery());

        q.setUserQuery("Heload");
        searchMapper.fromQuery(searchParameters, q);
        assertEquals("Heload", searchParameters.getSearchTerm());
    }

    @Test
    public void fromPaging() throws Exception
    {
        SearchParameters searchParameters = new SearchParameters();
        //Doesn't error
        searchMapper.fromPaging(searchParameters, null);
        Paging paging = Paging.DEFAULT;
        searchMapper.fromPaging(searchParameters, paging);
        assertEquals(searchParameters.getMaxItems(),paging.getMaxItems());
        assertEquals(searchParameters.getSkipCount(),paging.getSkipCount());
    }

    private SearchQuery minimalQuery()
    {
        SearchQuery sq = new SearchQuery();
        Query query = new Query();
        sq.setQuery(query);
        query.setQuery("foo");
        query.setLanguage("cmis");
        return sq;
    }
}
