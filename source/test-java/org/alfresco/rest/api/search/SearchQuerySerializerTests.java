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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.alfresco.rest.api.model.Comment;
import org.alfresco.rest.api.search.model.Default;
import org.alfresco.rest.api.search.model.SearchQuery;
import org.alfresco.rest.framework.jacksonextensions.JacksonHelper;
import org.alfresco.rest.framework.jacksonextensions.RestJsonModule;
import org.alfresco.rest.framework.tools.ApiAssistant;
import org.alfresco.rest.framework.tools.RequestReader;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.WebScriptRequest;

import java.io.IOException;
import java.io.StringReader;

/**
 * Tests json -> SearchQuery deserialization
 *
 * @author Gethin James
 */
public class SearchQuerySerializerTests
{

    private static SerializerTestHelper helper;

    @BeforeClass
    public static void setupTests() throws Exception
    {
        helper = new SerializerTestHelper();
    }

    @Test
    public void testDeserializeQuery() throws IOException
    {
        SearchQuery searchQuery = helper.searchQueryFromJson();
        assertEquals(SearchQuery.class, searchQuery.getClass());
        assertEquals("afts", searchQuery.getQuery().getLanguage());
        assertEquals("g*", searchQuery.getQuery().getQuery());
        assertEquals("great", searchQuery.getQuery().getUserQuery());
        assertEquals(99, searchQuery.getPaging().getMaxItems());
        assertEquals(4, searchQuery.getPaging().getSkipCount());
        assertEquals(2, searchQuery.getInclude().size());
        assertTrue(searchQuery.getInclude().contains("aspectNames"));
        assertTrue(searchQuery.getInclude().contains("properties"));
        assertEquals(1, searchQuery.getSort().size());
        assertEquals(2, searchQuery.getTemplates().size());
        Default defaults = searchQuery.getDefaults();
        assertEquals("namesp",  defaults.getNamespace());
        assertEquals("myfield", defaults.getDefaultFieldName());
        assertEquals("AND",     defaults.getDefaultFTSOperator());
        assertEquals(2, defaults.getTextAttributes().size());
        assertTrue(defaults.getTextAttributes().contains("roy"));
        assertTrue(defaults.getTextAttributes().contains("king"));
    }

}
