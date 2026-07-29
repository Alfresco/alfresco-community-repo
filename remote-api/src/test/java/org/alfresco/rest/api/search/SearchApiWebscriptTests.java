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
import static junit.framework.TestCase.assertNull;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;

import java.util.Arrays;

import org.junit.Test;

import org.alfresco.rest.api.search.model.Query;
import org.alfresco.rest.api.search.model.SearchQuery;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.rest.framework.resource.parameters.Paging;
import org.alfresco.rest.framework.resource.parameters.Params;
import org.alfresco.service.cmr.search.SearchParameters;

/**
 * Tests the SearchApiWebscript class
 *
 * @author Gethin James
 */
public class SearchApiWebscriptTests
{
    static SearchApiWebscript webscript = new SearchApiWebscript();

    @Test
    public void testPaging() throws Exception
    {
        Params params = webscript.getParams(null, null, null, null);
        // Defaults
        assertNotNull(params.getPaging());
        assertEquals(Paging.DEFAULT_MAX_ITEMS, params.getPaging().getMaxItems());
        assertEquals(Paging.DEFAULT_SKIP_COUNT, params.getPaging().getSkipCount());

        params = webscript.getParams(null, null, null, Paging.valueOf(4, 20));
        assertEquals(20, params.getPaging().getMaxItems());
        assertEquals(4, params.getPaging().getSkipCount());

    }

    @Test
    public void testFilter() throws Exception
    {
        Params params = webscript.getParams(null, null, null, null);
        // Defaults
        assertNotNull(params.getFilter());

        params = webscript.getParams(null, null, Arrays.asList("name", "size"), null);
        assertTrue("This isn't used until include is also specfied", params.getFilter().isAllowed("name"));

        assertTrue("Anything is allowed if include hasn't been specfied", params.getFilter().isAllowed("horse"));

        params = webscript.getParams(null, Arrays.asList("cat", "dog"), null, null);
        assertTrue(params.getFilter().isAllowed("cat"));
        assertTrue(params.getFilter().isAllowed("dog"));
        assertFalse(params.getFilter().isAllowed("horse"));

        params = webscript.getParams(null, Arrays.asList("cat", "dog"), Arrays.asList("name", "size"), null);
        assertTrue(params.getFilter().isAllowed("cat"));
        assertFalse(params.getFilter().isAllowed("horse"));
        assertTrue("name and size should be automatically added to the filter list", params.getFilter().isAllowed("name"));
        assertTrue("name and size should be automatically added to the filter list", params.getFilter().isAllowed("size"));
    }

    @Test
    public void testInclude() throws Exception
    {
        Params params = webscript.getParams(null, null, null, null);
        // Defaults
        assertNotNull(params.getInclude());
        assertEquals(0, params.getInclude().size());

        params = webscript.getParams(null, null, Arrays.asList("name", "size"), null);
        assertNotNull(params.getInclude());
        assertEquals(2, params.getInclude().size());
    }

    private static SearchQuery searchQuery(Paging paging, String searchAfter)
    {
        return new SearchQuery(new Query("afts", "a*", ""), paging,
                null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null,
                searchAfter);
    }

    @Test
    public void offsetPagingLeavesSearchAfterNull()
    {
        SearchParameters sp = new SearchParameters();

        webscript.applySearchAfter(sp, searchQuery(Paging.valueOf(10, 100), null));

        assertNull(sp.getSearchAfterToken());
    }

    @Test
    public void searchAfterMapsToSearchParameters()
    {
        SearchParameters sp = new SearchParameters();

        webscript.applySearchAfter(sp, searchQuery(null, "SEARCH_AFTER_TOKEN"));

        assertEquals("SEARCH_AFTER_TOKEN", sp.getSearchAfterToken());
    }

    @Test
    public void firstPageUsesEmptySearchAfterPosition()
    {
        SearchParameters sp = new SearchParameters();

        // An explicit empty searchAfter starts a new cursor-paging session (first page).
        webscript.applySearchAfter(sp, searchQuery(null, ""));

        assertEquals("", sp.getSearchAfterToken());
    }

    @Test(expected = InvalidArgumentException.class)
    public void skipCountCombinedWithSearchAfterIsRejected()
    {
        webscript.applySearchAfter(new SearchParameters(), searchQuery(Paging.valueOf(10, 100), "SEARCH_AFTER_TOKEN"));
    }
}
