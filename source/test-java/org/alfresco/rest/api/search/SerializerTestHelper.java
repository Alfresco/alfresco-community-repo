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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.alfresco.rest.api.search.model.SearchQuery;
import org.alfresco.rest.framework.jacksonextensions.JacksonHelper;
import org.alfresco.rest.framework.jacksonextensions.RestJsonModule;
import org.alfresco.rest.framework.tools.RequestReader;
import org.junit.BeforeClass;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.WebScriptRequest;

import java.io.IOException;
import java.io.StringReader;

/**
 * Sets up an parses a valid JSON request.
 *
 * @author Gethin James
 */
public class SerializerTestHelper implements RequestReader
{

    JacksonHelper jsonHelper = null;

    public static final String JSON = "{ \"query\": {\"query\": \"g*\",\"userQuery\": \"great\",\"language\": \"afts\"}, "
                + "\"paging\": {\"maxItems\": \"99\",\"skipCount\": \"4\"},"
                + "\"sort\": {\"type\": \"FIELD\",\"field\": \"cm:title\",\"ascending\": \"true\"},"
                + "\"templates\": [{\"name\": \"mytemp\",\"template\": \"ATEMP\"}, {\"name\": \"yourtemp\",\"template\": \"%cm:content\"}],"
                + "\"include\": [\"aspectNames\", \"properties\"]}";

    public SerializerTestHelper()
    {
        this.jsonHelper = new JacksonHelper();
        RestJsonModule module = new RestJsonModule();
        jsonHelper.setModule(module);
        try
        {
            jsonHelper.afterPropertiesSet();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private SearchQuery extractFromJson(String json) throws IOException
    {
        Content content = mock(Content.class);
        when(content.getReader()).thenReturn(new StringReader(json));
        WebScriptRequest request = mock(WebScriptRequest.class);
        when(request.getContent()).thenReturn(content);
        return extractJsonContent(request, jsonHelper, SearchQuery.class);
    }

    public SearchQuery searchQueryFromJson() throws IOException
    {
        return extractFromJson(JSON);
    }
}
