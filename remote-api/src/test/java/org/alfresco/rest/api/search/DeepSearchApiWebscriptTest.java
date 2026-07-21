/*-
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2026 Alfresco Software Limited
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

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import org.alfresco.rest.api.search.model.Query;
import org.alfresco.rest.api.search.model.SearchQuery;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.rest.framework.resource.parameters.Paging;
import org.alfresco.service.cmr.search.SearchParameters;

public class DeepSearchApiWebscriptTest
{
    private final DeepSearchApiWebscript webscript = new DeepSearchApiWebscript();

    private static SearchQuery searchQuery(Paging paging, String searchAfter)
    {
        return new SearchQuery(new Query("afts", "a*", ""), paging,
                null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null,
                searchAfter);
    }

    @Test
    public void searchAfterMapsToSearchParameters()
    {
        SearchParameters sp = new SearchParameters();

        webscript.applyPaginationContract(sp, searchQuery(null, "SEARCH_AFTER_TOKEN"));

        assertEquals("SEARCH_AFTER_TOKEN", sp.getSearchAfter());
    }

    @Test
    public void firstPageUsesEmptySearchAfterPosition()
    {
        SearchParameters sp = new SearchParameters();

        webscript.applyPaginationContract(sp, searchQuery(null, null));

        assertEquals("", sp.getSearchAfter());
    }

    @Test(expected = InvalidArgumentException.class)
    public void skipCountIsRejected()
    {
        webscript.applyPaginationContract(new SearchParameters(), searchQuery(Paging.valueOf(10, 100), "SEARCH_AFTER_TOKEN"));
    }
}
