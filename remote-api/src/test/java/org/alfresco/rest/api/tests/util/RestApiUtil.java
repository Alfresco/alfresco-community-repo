/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2018 Alfresco Software Limited
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

package org.alfresco.rest.api.tests.util;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import org.alfresco.rest.api.tests.client.PublicApiClient;

/**
 * A utility class for Rest API tests
 * 
 * @author Jamal Kaabi-Mofrad
 */
public class RestApiUtil
{
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private RestApiUtil()
    {}

    /**
     * Parses the alfresco REST API response for a collection of entries. Basically, it looks for the {@code list} JSON object, then it uses {@literal Jackson} to convert the list's entries to their corresponding POJOs based on the given {@code clazz}.
     * 
     * @param jsonObject
     *            the {@code JSONObject} derived from the response
     * @param clazz
     *            the class which represents the JSON payload
     * @return list of POJOs of the given {@code clazz} type
     * @throws Exception
     */
    public static <T> List<T> parseRestApiEntries(JSONObject jsonObject, Class<T> clazz) throws Exception
    {
        assertNotNull(jsonObject);
        assertNotNull(clazz);

        List<T> models = new ArrayList<>();

        JSONObject jsonList = (JSONObject) jsonObject.get("list");
        assertNotNull(jsonList);

        JSONArray jsonEntries = (JSONArray) jsonList.get("entries");
        assertNotNull(jsonEntries);

        for (int i = 0; i < jsonEntries.size(); i++)
        {
            JSONObject jsonEntry = (JSONObject) jsonEntries.get(i);
            T pojoModel = parseRestApiEntry(jsonEntry, clazz);
            models.add(pojoModel);
        }

        return models;
    }

    /**
     * Parses the alfresco REST API response for a single entry. Basically, it looks for the {@code entry} JSON object, then it uses {@literal Jackson} to convert it to its corresponding POJO based on the given {@code clazz}.
     * 
     * @param jsonObject
     *            the {@code JSONObject} derived from the response
     * @param clazz
     *            the class which represents the JSON payload
     * @return the POJO of the given {@code clazz} type
     * @throws Exception
     */
    public static <T> T parseRestApiEntry(JSONObject jsonObject, Class<T> clazz) throws Exception
    {
        return parsePojo("entry", jsonObject, clazz);
    }

    /**
     * Parses the alfresco REST API response object and extracts the paging information.
     * 
     * @param jsonObject
     *            the {@code JSONObject} derived from the response
     * @return ExpectedPaging the paging
     * @throws Exception
     */
    public static PublicApiClient.ExpectedPaging parsePaging(JSONObject jsonObject) throws Exception
    {
        assertNotNull(jsonObject);
        JSONObject jsonList = (JSONObject) jsonObject.get("list");
        assertNotNull(jsonList);
        return parsePojo("pagination", jsonList, PublicApiClient.ExpectedPaging.class);
    }

    /**
     * Parses the alfresco REST API response, uses {@literal Jackson} to convert it to its corresponding POJO based on the given {@code clazz}.
     *
     * @param jsonObject
     *            the {@code JSONObject} derived from the response
     * @param clazz
     *            the class which represents the JSON payload
     * @return the POJO of the given {@code clazz} type
     * @throws Exception
     */
    public static <T> T parsePojo(String key, JSONObject jsonObject, Class<T> clazz) throws Exception
    {
        assertNotNull(jsonObject);
        assertNotNull(clazz);

        JSONObject pojo = (JSONObject) jsonObject.get(key);
        T pojoModel = OBJECT_MAPPER.readValue(pojo.toJSONString(), clazz);
        assertNotNull(pojoModel);

        return pojoModel;
    }

    /**
     * Parses the alfresco REST API error response.
     *
     * @param jsonObject
     *            the {@code JSONObject} derived from the response
     * @return ExpectedErrorResponse the error object
     * @throws Exception
     */
    public static PublicApiClient.ExpectedErrorResponse parseErrorResponse(JSONObject jsonObject) throws Exception
    {
        return parsePojo("error", jsonObject, PublicApiClient.ExpectedErrorResponse.class);
    }

    /**
     * Converts the POJO which represents the JSON payload into a JSON string
     */
    public static String toJsonAsString(Object object) throws Exception
    {
        assertNotNull(object);
        return OBJECT_MAPPER.writeValueAsString(object);
    }

    /**
     * Converts the POJO which represents the JSON payload into a JSON string. null values will be ignored.
     */
    public static String toJsonAsStringNonNull(Object object) throws IOException
    {
        assertNotNull(object);
        ObjectMapper om = new ObjectMapper();
        om.setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL);
        // keep null values in maps (like 'properties' for example) since these are part of the partial update/delete payload
        om.configOverride(java.util.Map.class)
                .setIncludeAsProperty(JsonInclude.Value.construct(JsonInclude.Include.ALWAYS, JsonInclude.Include.ALWAYS));
        return om.writeValueAsString(object);
    }
}
