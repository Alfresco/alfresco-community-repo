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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.alfresco.rest.api.model.Comment;
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
public class SearchQuerySerializerTests implements RequestReader
{
    static JacksonHelper jsonHelper = null;

    @BeforeClass
    public static void setupTests() throws Exception
    {
        jsonHelper = new JacksonHelper();
        RestJsonModule module = new RestJsonModule();
        jsonHelper.setModule(module);
        jsonHelper.afterPropertiesSet();
    }

    @Test
    public void testDeserializeQuery() throws IOException
    {
        String json = "{ \"query\": {\"query\": \"g*\",\"userQuery\": \"great\",\"language\": \"bob\"}}";
        SearchQuery searchQuery = extractFromJson(json);
        assertEquals(SearchQuery.class, searchQuery.getClass());
        assertEquals("bob", searchQuery.getQuery().getLanguage());
        assertEquals("g*", searchQuery.getQuery().getQuery());
        assertEquals("great", searchQuery.getQuery().getUserQuery());
    }

    private SearchQuery extractFromJson(String json) throws IOException
    {
        Content content = mock(Content.class);
        when(content.getReader()).thenReturn(new StringReader(json));
        WebScriptRequest request = mock(WebScriptRequest.class);
        when(request.getContent()).thenReturn(content);
        return extractJsonContent(request, jsonHelper, SearchQuery.class);
    }

}
