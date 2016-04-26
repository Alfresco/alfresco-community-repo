
package org.alfresco.rest.api.tests.util;

import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.rest.api.tests.client.PublicApiClient;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * A utility class for Rest API tests
 * 
 * @author Jamal Kaabi-Mofrad
 */
public class RestApiUtil
{
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private RestApiUtil()
    {
    }

    /**
     * Parses the alfresco REST API response for a collection of entries.
     * Basically, it looks for the {@code list} JSON object, then it uses
     * {@literal Jackson} to convert the list's entries to their corresponding
     * POJOs based on the given {@code clazz}.
     * 
     * @param jsonObject the {@code JSONObject} derived from the response
     * @param clazz the class which represents the JSON payload
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
     * Parses the alfresco REST API response for a single entry. Basically, it
     * looks for the {@code entry} JSON object, then it uses {@literal Jackson}
     * to convert it to its corresponding POJO based on the given {@code clazz}.
     * 
     * @param jsonObject the {@code JSONObject} derived from the response
     * @param clazz the class which represents the JSON payload
     * @return the POJO of the given {@code clazz} type
     * @throws Exception
     */
    public static <T> T parseRestApiEntry(JSONObject jsonObject, Class<T> clazz) throws Exception
    {
        return parsePojo("entry",jsonObject, clazz);
    }

    /**
     * Parses the alfresco REST API response object and extracts the paging information.
     * @param jsonObject the {@code JSONObject} derived from the response
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
     * Parses the alfresco REST API response, uses {@literal Jackson}
     * to convert it to its corresponding POJO based on the given {@code clazz}.
     *
     * @param jsonObject the {@code JSONObject} derived from the response
     * @param clazz the class which represents the JSON payload
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
     * Converts the POJO which represents the JSON payload into a JSON string
     */
    public static String toJsonAsString(Object object) throws Exception
    {
        assertNotNull(object);
        return OBJECT_MAPPER.writeValueAsString(object);
    }
}
