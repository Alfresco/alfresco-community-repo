/*
 * #%L
 * Alfresco Repository
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
import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.alfresco.rest.api.search.model.SearchSQLQuery;
import org.junit.BeforeClass;
import org.junit.Test;

public class SearchSQLParseTest
{
    private static SerializerTestHelper helper;

    @BeforeClass
    public static void setupTests() throws Exception
    {
        helper = new SerializerTestHelper();
    }
    @Test
    public void testSQLDeserializeQuery() throws IOException
    {
        String query = "{\"stmt\": \"select cm_name from alfresco\"}";
        SearchSQLQuery searchQuery = parse(query);
        assertEquals("select cm_name from alfresco", searchQuery.getStmt());
    }
    @Test
    public void testSQLDeserializeSelectQuery() throws IOException
    {
        String query = "{\"stmt\": \"select SITE from alfresco\"}";
        SearchSQLQuery searchQuery = parse(query);
        assertEquals("select SITE from alfresco", searchQuery.getStmt());
    }
    private SearchSQLQuery parse(String json) throws IOException
    {
        return (SearchSQLQuery) helper.searchSQLQueryFromJson(json, SearchSQLQuery.class);
    }
}
