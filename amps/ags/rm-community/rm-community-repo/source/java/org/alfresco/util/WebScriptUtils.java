/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.util;

import static org.alfresco.util.ParameterCheck.mandatory;
import static org.alfresco.util.ParameterCheck.mandatoryString;
import static org.apache.commons.lang3.StringUtils.isBlank;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Utility class for handling webscript requests
 *
 * @author Tuna Aksoy
 * @since 2.3
 */
public final class WebScriptUtils
{
    private WebScriptUtils()
    {
        // Will not be called
    }

    /**
     * Gets the template variable substitutions map
     *
     * @param req The webscript request
     * @return The template variable substitutions
     */
    public static Map<String, String> getTemplateVars(WebScriptRequest req)
    {
        mandatory("req", req);

        if (req.getServiceMatch() == null)
        {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "The matching API Service for the request is null.");
        }

        Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
        if (templateVars == null)
        {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "The template variable substitutions map is null");
        }

        return templateVars;
    }

    /**
     * Gets the value of a request parameter
     *
     * @param req The webscript request
     * @param parameter The request parameter
     * @return The value of the request parameter
     */
    public static String getRequestParameterValue(WebScriptRequest req, String parameter)
    {
        mandatory("req", req);
        mandatoryString("parameter", parameter);

        return getRequestParameterValue(req, parameter, true);
    }

    /**
     * Gets the value of a request parameter
     *
     * @param req The webscript request
     * @param parameter The request parameter
     * @param checkValue Determines if the value of the parameter should be checked or not
     * @return The value of the request parameter
     */
    public static String getRequestParameterValue(WebScriptRequest req, String parameter, boolean checkValue)
    {
        mandatory("req", req);
        mandatoryString("parameter", parameter);

        Map<String, String> templateVars = getTemplateVars(req);
        String value = templateVars.get(parameter);

        if (checkValue && isBlank(value))
        {
            throw new WebScriptException(Status.STATUS_NOT_FOUND, "The value for the parameter '" + parameter + "' is blank.");
        }

        return value;
    }

    /**
     * Gets the request content as JSON object
     *
     * @param req The webscript request
     * @return The request content as JSON object
     */
    public static JSONObject getRequestContentAsJSONObject(WebScriptRequest req)
    {
        mandatory("req", req);

        Content reqContent = req.getContent();
        if (reqContent == null)
        {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Missing request body.");
        }

        String content;
        try
        {
            content = reqContent.getContent();
        }
        catch (IOException error)
        {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Could not get content from the request.", error);
        }

        if (isBlank(content))
        {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Content does not exist.");
        }

        JSONTokener jsonTokener = new JSONTokener(content);

        JSONObject json;
        try
        {
            json = new JSONObject(jsonTokener);
        }
        catch (JSONException error)
        {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Unable to parse request body.", error);
        }

        return json;
    }

    /**
     * Checks if the json object contains an entry with the specified parameter name
     *
     * @param jsonObject The json object
     * @param paramName The parameter name to check for
     */
    public static void checkMandatoryJSONParam(JSONObject jsonObject, String paramName)
    {
        mandatory("jsonObject", jsonObject);
        mandatoryString("paramName", paramName);

        if (!jsonObject.has(paramName))
        {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "The json object does not contain an entry with parameter '" + paramName + "'.");
        }
    }

    /**
     * Checks if the json object contains entries with the specified parameter names
     *
     * @param jsonObject The json object.
     * @param paramNames The parameter names to check for
     */
    public static void checkMandatoryJSONParams(JSONObject jsonObject, List<String> paramNames)
    {
        mandatory("jsonObject", jsonObject);
        mandatory("paramNames", paramNames);

        for (String name : paramNames)
        {
            checkMandatoryJSONParam(jsonObject, name);
        }
    }

    /**
     * Gets the {@link String} value of a given key from a json object
     *
     * @param jsonObject The json object
     * @param key The key
     * @return The {@link String} value of the given key from the json object
     */
    public static String getStringValueFromJSONObject(JSONObject jsonObject, String key)
    {
        mandatory("jsonObject", jsonObject);
        mandatoryString("key", key);

        return getStringValueFromJSONObject(jsonObject, key, true, true);
    }

    /**
     * Gets the {@link String} value of a given key from a json object
     *
     * @param jsonObject The json object
     * @param key The key
     * @param checkKey Determines if the existence of the key should be checked
     * @param checkValue Determines if the value should be checked if it is blank or not
     * @return The {@link String} value of the given key from the json object
     */
    public static String getStringValueFromJSONObject(JSONObject jsonObject, String key, boolean checkKey, boolean checkValue)
    {
        mandatory("jsonObject", jsonObject);
        mandatoryString("key", key);

        if (checkKey)
        {
            checkMandatoryJSONParam(jsonObject, key);
        }

        String value = null;

        try
        {
            value = jsonObject.get(key).toString();
            if (checkValue && isBlank(value))
            {
                throw new WebScriptException(Status.STATUS_BAD_REQUEST, "The value is missing for the key '" + key + "'.");
            }
        }
        catch (JSONException error)
        {
            if (checkValue)
            {
                throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Could not get value for the key '" + key + "'.", error);
            }
        }

        return value;
    }

    /**
     * Puts the given key and value to the json object
     *
     * @param jsonObject The json object
     * @param key The key
     * @param value The value
     */
    public static void putValueToJSONObject(JSONObject jsonObject, String key, Object value)
    {
        mandatory("jsonObject", jsonObject);
        mandatoryString("key", key);
        mandatory("value", value);

        try
        {
            jsonObject.put(key, value);
        }
        catch (JSONException error)
        {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Could not put the key '" + key + "' with the value '" + value + "' to the json object.", error);
        }
    }

    /**
     * Gets the value of an element from a json array at the given index
     *
     * @param jsonArray The json array
     * @param index The index
     * @return The value of the element
     */
    public static Object getJSONArrayValue(JSONArray jsonArray, int index)
    {
        mandatory("jsonArray", jsonArray);

        Object value;

        try
        {
            value = jsonArray.get(index);
        }
        catch (JSONException error)
        {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Could not get value for the index '" + index + "' from the JSON Array.", error);
        }

        return value;
    }

    /**
     * Creates a json object from the given {@link String}
     *
     * @param json The json object as {@link String}
     * @return The json object created from the given {@link String}
     */
    public static JSONObject createJSONObject(String json)
    {
        mandatory("json", json);

        JSONObject jsonObject;

        try
        {
            jsonObject = new JSONObject(json);
        }
        catch (JSONException error)
        {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Cannot create a json object from the given string '" + json + "'.", error);
        }

        return jsonObject;
    }

    /**
     * Creates a json array from the given {@link String}
     *
     * @param json The json array as {@link String}
     * @return The json array created from the given {@link String}
     */
    public static JSONArray createJSONArray(String json)
    {
        mandatory("json", json);

        JSONArray jsonArray;

        try
        {
            jsonArray = new JSONArray(json);
        }
        catch (JSONException error)
        {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Cannot create a json array from the given string '" + json + "'.", error);
        }

        return jsonArray;
    }

    /**
     * Gets the {@link JSONArray} value of a given key from a json object
     *
     * @param jsonObject The json object
     * @param key The key
     * @return The {@link JSONArray} value of the given key from the json object
     */
    public static JSONArray getJSONArrayFromJSONObject(JSONObject jsonObject, String key)
    {
        JSONArray jsonArray;

        mandatory("jsonObject", jsonObject);
        mandatory("key", key);

        try
        {
            jsonArray = jsonObject.getJSONArray(key);
        }
        catch (JSONException error)
        {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Could not get the json array for the key '" + key + "'.", error);
        }

        return jsonArray;
    }

    /**
     * Returns {@code true} if the provided {@link WebScriptException}
     * represents an HTTP 4xx error, else {@code false}.
     */
    public static boolean is4xxError(WebScriptException e)
    {
        return isStatusInRange(e, 400, 500);
    }

    /**
     * Returns {@code true} if the provided {@link WebScriptException}
     * represents an HTTP 5xx error, else {@code false}.
     */
    public static boolean is5xxError(WebScriptException e)
    {
        return isStatusInRange(e, 500, 600);
    }

    private static boolean isStatusInRange(WebScriptException e, int lowerLimitInclusive, int upperLimitExclusive)
    {
        final int status = e.getStatus();
        return status >= lowerLimitInclusive && status < upperLimitExclusive;
    }

    /**
     * Gets the {@link JSONObject} value of a given key from a json object
     *
     * @param jsonObject The json object
     * @param key The key
     * @return The {@link JSONObject} value of the given key from the json object
     */
    public static JSONObject getValueFromJSONObject(JSONObject jsonObject, String key)
    {
        mandatory("jsonObject", jsonObject);
        mandatoryString("key", key);

        JSONObject value = null;

        try
        {
            value = jsonObject.getJSONObject(key);
        }
        catch (JSONException error)
        {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Could not get value for the key '" + key + "'.", error);
        }

        return value;
    }
}
