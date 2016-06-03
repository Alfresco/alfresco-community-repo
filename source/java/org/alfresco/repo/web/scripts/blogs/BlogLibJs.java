package org.alfresco.repo.web.scripts.blogs;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.BlogIntegrationModel;
import org.alfresco.service.namespace.QName;
import org.json.simple.JSONObject;

/**
 * This class is a port of a previous JavaScript library.
 * 
 * @author Neil Mc Erlean (based on previous JavaScript)
 * @since 4.0
 * @deprecated This class should not be extended or reused as it may be refactored.
 */
public class BlogLibJs
{
    /**
     * Fetches the blog properties from the json object and adds them to an array
     * using the correct property names as indexes.
     */
    public static Map<QName, Serializable> getBlogPropertiesArray(JSONObject json)
    {
        Map<QName, Serializable> arr = new HashMap<QName, Serializable>();
        
        putJSONEntryInMap(json, arr, "blogType",        BlogIntegrationModel.PROP_BLOG_IMPLEMENTATION);
        putJSONEntryInMap(json, arr, "blogId",          BlogIntegrationModel.PROP_ID);
        putJSONEntryInMap(json, arr, "blogName",        BlogIntegrationModel.PROP_NAME);
        putJSONEntryInMap(json, arr, "blogDescription", BlogIntegrationModel.PROP_DESCRIPTION);
        putJSONEntryInMap(json, arr, "blogUrl",         BlogIntegrationModel.PROP_URL);
        putJSONEntryInMap(json, arr, "username",        BlogIntegrationModel.PROP_USER_NAME);
        putJSONEntryInMap(json, arr, "password",        BlogIntegrationModel.PROP_PASSWORD);
        return arr;
    }

    private static void putJSONEntryInMap(JSONObject json,
            Map<QName, Serializable> arr, String jsonKey, QName mapKey)
    {
         if (json.containsKey(jsonKey))
         {
             arr.put(mapKey, (Serializable)json.get(jsonKey));
         }
    }
}
