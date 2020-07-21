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
package org.alfresco.rest.api.tests.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import org.apache.commons.httpclient.HttpMethod;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class AbstractHttp
{
    public static final String JSON_DATA = "data";

    /**
     * Extract the "data" JSON-object from the method's response.
     * @param method the method containing the response
     * @return the "data" object. Returns null if response is not JSON or no data-object
     *         is present.
     */
    public JSONObject getDataFromResponse(HttpMethod method)
    {
        JSONObject result = null;
        Object object = getObjectFromResponse(method);
        
        // Extract object for "data" property
        object = ((JSONObject) object).get(JSON_DATA);
        if(object instanceof JSONObject)
        {
            result = (JSONObject) object;
        }
        return result;
    }
    
    /**
     * Extract the "data" JSON-array from the method's response.
     * @param method the method containing the response
     * @return the "data" object. Returns null if response is not JSON or no data-object
     *         is present.
     */
    public JSONArray getDataArrayFromResponse(HttpMethod method)
    {
        JSONArray result = null;
        Object object = getObjectFromResponse(method);
        if(object != null)
        {
            // Extract object for "data" property
            object = ((JSONObject) object).get(JSON_DATA);
            if(object instanceof JSONArray)
            {
                result = (JSONArray) object;
            }
        }
        return result;
    }
    
    /**
     * Extract JSON-object from the method's response.
     * @param method the method containing the response
     * @return the json object. Returns null if response is not JSON or no data-object
     *         is present.
     */
    public JSONObject getObjectFromResponse(HttpMethod method)
    {
        JSONObject result = null;

        try
        {
            InputStream response = method.getResponseBodyAsStream();
            if(response != null)
            {
                Object object = new JSONParser().parse(new InputStreamReader(response, Charset.forName("UTF-8")));
                if(object instanceof JSONObject)
                {
                   return (JSONObject) object;
                }
            }
        }
        catch (IOException error)
        {
            // Ignore errors, returning null
        }
        catch (ParseException error)
        {
            // Ignore errors, returning null
        }
       
        return result;
    }
    
    /**
     * Gets a string-value from the given JSON-object for the given key.
     * @param json the json object
     * @param key key pointing to the value
     * @param defaultValue if value is not set or if value is not of type "String", this value is returned
     */
    public String getString(JSONObject json, String key, String defaultValue)
    {
        String result = defaultValue;
        
        if(json != null)
        {
            Object value = json.get(key);
            if(value instanceof String)
            {
                result = (String) value;
            }
        }
        return result;
    }

    /**
     * @param json JSON to extract array from
     * @param key key under which array is present on JSON
     * @return the {@link JSONArray}. Returns null, if the value is null or not an array.
     */
    public JSONArray getArray(JSONObject json, String key)
    {
        Object object = json.get(key);
        if(object instanceof JSONArray)
        {
            return (JSONArray) object;
        }
        return null;
    }

    /**
     * @param json JSON to extract object from
     * @param key key under which object is present on JSON
     * @return the {@link JSONObject}. Returns null, if the value is null or not an object.
     */
    public JSONObject getObject(JSONObject json, String key)
    {
        Object object = json.get(key);
        if(object instanceof JSONArray)
        {
            return (JSONObject) object;
        }
        return null;
    }
}
