/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2021 Alfresco Software Limited
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

import org.junit.Before;
import org.junit.Test;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;

public class FilterQueryIT extends ElasticsearchBaseQueryIT
{

    private static NodeRef redPepper;
    private static NodeRef greenPepper;
    private static NodeRef yellowPepper;
    private static NodeRef whitePepper;
    private static NodeRef anotherWhitePepper;

    @Before
    public void setUp() throws Exception
    {
        super.setUp();
        redPepper = indexDocument("red pepper", "text number one");
        greenPepper = indexDocument("green pepper", "text number one");
        yellowPepper = indexDocument("yellow", "text number three");
        whitePepper = indexDocument("white pepper", "number four");
        anotherWhitePepper = indexDocument("another pepper white", "text four");
    }

    public ResultSet searchWithFilters(String query, String... filterQueries)
    {
        SearchParameters searchParams = new SearchParameters();
        searchParams.setQuery(query);
        searchParams.setLimit(10);
        searchParams.setSkipCount(0);
        for (String fq : filterQueries)
        {
            searchParams.addFilterQuery(fq);
        }
        searchParams.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
        return aftsQueryExecutor.executeQuery(searchParams);
    }

    @Test
    public void whenSingleFilterIsApplied()
    {
        assertContainsOnly(searchWithFilters("text", "cm:name:\"red pepper\""), redPepper);
        assertContainsOnly(searchWithFilters("text", "cm:name:red cm:name:pepper"), redPepper, greenPepper, anotherWhitePepper);
        assertContainsOnly(searchWithFilters("text number", "cm:name:white"), whitePepper, anotherWhitePepper);
    }

    @Test
    public void whenMultipleFiltersAreApplied()
    {
        assertContainsOnly(searchWithFilters("text", "cm:name:\"red pepper\"", "cm:name:\"green pepper\""));
        assertContainsOnly(searchWithFilters("text number", "cm:name:white"), whitePepper, anotherWhitePepper);
        assertContainsOnly(searchWithFilters("text", "cm:name:red cm:name:pepper", "cm:name:red"), redPepper);
        assertContainsOnly(searchWithFilters("text number", "cm:name:\"white\""), whitePepper, anotherWhitePepper);
    }

    @Test
    public void whenNoFilterIsApplied()
    {
        assertContainsOnly(searchWithFilters("text"), redPepper, greenPepper, yellowPepper, anotherWhitePepper);
    }

    @Test
    public void whenFilterQueriesContainSolrPrefixes()
    {
        assertContainsOnly(searchWithFilters("text", "{!afts }  cm:name:green"), greenPepper);
        assertContainsOnly(searchWithFilters("text", "{!lucene tag=test}cm:name:\"red pepper\"", "{!afts}cm:name:\"pepper\""), redPepper);
    }

}
