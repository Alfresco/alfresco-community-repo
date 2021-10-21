/*
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

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.alfresco.rest.api.search.model.SearchQuery;
import org.alfresco.rest.framework.jacksonextensions.JacksonHelper;
import org.alfresco.rest.framework.jacksonextensions.JacksonHelper.Writer;
import org.alfresco.rest.framework.jacksonextensions.RestJsonModule;
import org.alfresco.rest.framework.tools.RequestReader;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.WebScriptRequest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;

/**
 * Sets up an parses a valid JSON request.
 *
 * @author Gethin James
 */
public class SerializerTestHelper implements RequestReader
{

    JacksonHelper jsonHelper;

    public static final String JSON = "{ \"query\": {\"query\": \"g*\",\"userQuery\": \"great\",\"language\": \"afts\"}, "
                + "\"paging\": {\"maxItems\": \"99\",\"skipCount\": \"4\"},"
                + "\"includeRequest\": true,"
                + "\"sort\": {\"type\": \"FIELD\",\"field\": \"cm:title\",\"ascending\": \"true\"},"
                + "\"templates\": [{\"name\": \"mytemp\",\"template\": \"ATEMP\"}, {\"name\": \"yourtemp\",\"template\": \"%cm:content\"}],"
                + "\"defaults\": {\"namespace\": \"namesp\",\"defaultFieldName\": \"myfield\",\"defaultFTSOperator\": \"AND\", \"textAttributes\": [\"roy\", \"king\"]},"
                + "\"filterQueries\": [{\"query\": \"myquery\",\"tags\": [\"tag1\", \"tag2\"]},{\"query\": \"myquery2\"}],"
                + "\"facetFields\": {\"facets\": [{\"field\": \"cm:creator\",\"prefix\": \"myquery2\",\"sort\": \"COUNT\",\"missing\": \"false\"}, {\"field\": \"modifier\",\"label\": \"mylabel\",\"method\": \"FC\",\"mincount\": \"5\"}, {\"field\": \"owner\",\"label\": \"ownerLabel\"}]},"
                + "\"facetQueries\": [{\"query\": \"cm:created:bob\",\"label\": \"small\"}],"
                + "\"pivots\": [{\"key\": \"mylabel\"}],"
                + "\"ranges\": [{\"field\": \"content.size\",\"start\": \"0\",\"end\": \"300\",\"gap\": \"100\",\"include\":[\"lower\"]}],"
                + "\"facetIntervals\": {\"sets\": [{ \"label\": \"king\", \"start\": \"1\", \"end\": \"2\",\"startInclusive\": true,\"endInclusive\": false}]"
                + ",\"intervals\": [{\"field\": \"cm:creator\",\"label\": \"creator\","
                + "\"sets\": [{\"label\": \"last\",\"start\": \"a\",\"end\": \"b\",\"startInclusive\": false}]"
                + "},"
                + "{\"label\":\"TheCreated\",\"field\":\"cm:created\",\"sets\":[{\"label\":\"lastYear\",\"start\":\"2016\",\"end\":\"2017\",\"endInclusive\":false},{\"label\":\"currentYear\",\"start\":\"NOW/YEAR\",\"end\":\"NOW/YEAR+1YEAR\"},{\"label\":\"earlier\",\"start\":\"*\",\"end\":\"2016\",\"endInclusive\":false}]}"
                + "]},"
                + "\"stats\": [{\"field\": \"cm:creator\", \"label\": \"mylabel\"}],"
                + "\"spellcheck\": {\"query\": \"alfrezco\"},"
                + "\"limits\": {\"permissionEvaluationCount\": \"2000\",\"permissionEvaluationTime\": \"5000\"},"
                + "\"scope\": { \"locations\": [\"nodes\"]},"
                + "\"fields\": [\"id\", \"name\"],"
                + "\"highlight\": {\"prefix\": \"[\",\"postfix\": \"]\",\"snippetCount\": \"20\","
                + "\"fragmentSize\": \"10\",\"mergeContiguous\": \"true\",\"maxAnalyzedChars\": \"40\", \"usePhraseHighlighter\": \"true\","
                + "\"fields\": [ "
                +" {\"field\": \"my\", \"snippetCount\": \"23\", \"fragmentSize\": \"5\", \"mergeContiguous\": \"true\", \"prefix\": \"?\", \"postfix\": \"¡\"  }, "
                +" {\"field\": \"your\", \"snippetCount\": \"3\", \"fragmentSize\": \"15\", \"mergeContiguous\": \"false\", \"prefix\": \"(\", \"postfix\": \")\"  } "
                + " ]" + " },"
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

    public SearchQuery extractFromJson(String json)
    {
        Content content = mock(Content.class);
        try
        {
            when(content.getReader()).thenReturn(new StringReader(json));
        }
        catch (IOException e)
        {
            throw new IllegalStateException("Unexpectedly received exception when configuring mock.", e);
        }
        WebScriptRequest request = mock(WebScriptRequest.class);
        when(request.getContent()).thenReturn(content);
        return extractJsonContent(request, jsonHelper, SearchQuery.class);
    }

    public SearchQuery searchQueryFromJson()
    {
        return extractFromJson(JSON);
    }

    public String writeResponse(final Object respons) throws IOException
    {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        jsonHelper.withWriter(out, new Writer()
        {
            @Override
            public void writeContents(JsonGenerator generator, ObjectMapper objectMapper)
                        throws JsonGenerationException, JsonMappingException, IOException
            {
                objectMapper.writeValue(generator, respons);
            }
        });
        return out.toString();
    }
    public Object searchSQLQueryFromJson(String query, Class<?> classz) throws IOException
    {
        Content content = mock(Content.class);
        when(content.getReader()).thenReturn(new StringReader(query));
        WebScriptRequest request = mock(WebScriptRequest.class);
        when(request.getContent()).thenReturn(content);
        return extractJsonContent(request, jsonHelper, classz);
    }
}
