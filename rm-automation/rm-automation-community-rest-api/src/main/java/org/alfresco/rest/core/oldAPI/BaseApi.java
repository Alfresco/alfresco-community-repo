/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2017 Alfresco Software Limited
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
package org.alfresco.rest.core.oldAPI;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.dataprep.AlfrescoHttpClient;
import org.alfresco.dataprep.AlfrescoHttpClientFactory;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class BaseApi
{
    // logger
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseApi.class);

    /** exception key in JSON response body */
    private static final String EXCEPTION_KEY = "exception";

    @Autowired
    private AlfrescoHttpClientFactory alfrescoHttpClientFactory;

    /**
     * Helper method to extract list of properties values from result.
     *
     * @param result
     * @return list of specified property values in result
     * @throws RuntimeException for malformed response
     */
    public List<String> getPropertyValues(JSONObject result, String propertyName)
    {
        ArrayList<String> results = new ArrayList<String>();
        try
        {
            JSONArray items = result.getJSONArray("items");

            for (int i = 0; i < items.length(); i++)
            {
                results.add(items.getJSONObject(i).getString(propertyName));
            }
        }
        catch (JSONException error)
        {
            throw new RuntimeException("Unable to parse result", error);
        }

        return results;
    }

    /**
     * Helper method to extract property values from request result and put them in map as a list that corresponds to a unique property value.
     *
     * @param requestResult
     * @return a map containing information about multiple properties values that correspond to a unique one
     * @throws RuntimeException for malformed response
     */
    public Map<String, List<String>> getPropertyValuesByUniquePropertyValue(JSONObject requestResult, String uniqueProperty, List<String> otherProperties)
    {
        Map<String, List<String>> valuesByUniqueProperty = new HashMap<>();
        try
        {
            JSONArray items = requestResult.getJSONArray("items");

            for (int i = 0; i < items.length(); i++)
            {
                List<String> otherPropertiesValues = new ArrayList<>();

                for (int j = 0; j < otherProperties.size(); j++)
                {
                    otherPropertiesValues.add(items.getJSONObject(i).get(otherProperties.get(j)).toString());
                }
                valuesByUniqueProperty.put(items.getJSONObject(i).getString(uniqueProperty), otherPropertiesValues);
            }
        }
        catch (JSONException error)
        {
            throw new RuntimeException("Unable to parse result", error);
        }

        return valuesByUniqueProperty;
    }

    /**
     * Generic faceted request.
     *
     * @param username
     * @param password
     * @param parameters if the request has parameters
     * @return result object (see API reference for more details), null for any errors
     */
    public JSONObject facetedRequest(String username, String password, List<NameValuePair> parameters, String requestURI)
    {
        String requestURL;
        AlfrescoHttpClient client = alfrescoHttpClientFactory.getObject();

        if (parameters == null || parameters.isEmpty())
        {
             requestURL = MessageFormat.format(
                    requestURI,
                    client.getAlfrescoUrl());
        }
        else
        {
             requestURL = MessageFormat.format(
                    requestURI,
                    client.getAlfrescoUrl(),
                    URLEncodedUtils.format(parameters, "UTF-8"));
        }
        client.close();
        return doGetRequest(username, password, requestURL);
    }

    /**
     * Helper method for GET requests
     * @param adminUser user with administrative privileges
     * @param adminPassword password for adminUser
     * @param urlTemplate request URL template
     * @param urlTemplateParams zero or more parameters used with <i>urlTemplate</i>
     */
    public JSONObject doGetRequest(String adminUser,
        String adminPassword,
        String urlTemplate,
        String ... urlTemplateParams)
    {
        AlfrescoHttpClient client = alfrescoHttpClientFactory.getObject();
        String requestUrl = MessageFormat.format(
            urlTemplate,
            client.getApiUrl(),
            urlTemplateParams);
        client.close();

        try
        {
            return doRequest(HttpGet.class, requestUrl, adminUser, adminPassword, null);
        }
        catch (InstantiationException | IllegalAccessException error)
        {
            throw new IllegalArgumentException("doGetRequest failed", error);
        }
    }

    /**
     * Helper method for Delete requests
     * @param adminUser user with administrative privileges
     * @param adminPassword password for adminUser
     * @param urlTemplate request URL template
     * @param urlTemplateParams zero or more parameters used with <i>urlTemplate</i>
     */
    public JSONObject doDeleteRequest(String adminUser,
        String adminPassword,
        String urlTemplate,
        String ... urlTemplateParams)
    {
        AlfrescoHttpClient client = alfrescoHttpClientFactory.getObject();
        String requestUrl = MessageFormat.format(
            urlTemplate,
            client.getApiUrl(),
            urlTemplateParams);
        client.close();

        try
        {
            return doRequest(HttpDelete.class, requestUrl, adminUser, adminPassword, null);
        }
        catch (InstantiationException | IllegalAccessException error)
        {
            throw new IllegalArgumentException("doDeleteRequest failed", error);
        }
    }

    /**
     * Helper method for PUT requests
     * @param adminUser user with administrative privileges
     * @param adminPassword password for adminUser
     * @param requestParams zero or more endpoint specific request parameters
     * @param urlTemplate request URL template
     * @param urlTemplateParams zero or more parameters used with <i>urlTemplate</i>
     */
    public JSONObject doPutRequest(String adminUser,
        String adminPassword,
        JSONObject requestParams,
        String urlTemplate,
        String ... urlTemplateParams)
    {
        AlfrescoHttpClient client = alfrescoHttpClientFactory.getObject();
        String requestUrl = MessageFormat.format(
            urlTemplate,
            client.getApiUrl(),
            urlTemplateParams);
        client.close();

        try
        {
            return doRequest(HttpPut.class, requestUrl, adminUser, adminPassword, requestParams);
        }
        catch (InstantiationException | IllegalAccessException error)
        {
            throw new IllegalArgumentException("doPutRequest failed", error);
        }
    }

    /**
     * Helper method for POST requests
     * @param adminUser user with administrative privileges
     * @param adminPassword password for adminUser
     * @param requestParams zero or more endpoint specific request parameters
     * @param urlTemplate request URL template
     * @param urlTemplateParams zero or more parameters used with <i>urlTemplate</i>
     */
    public JSONObject doPostRequest(String adminUser,
        String adminPassword,
        JSONObject requestParams,
        String urlTemplate,
        String ... urlTemplateParams)
    {
        AlfrescoHttpClient client = alfrescoHttpClientFactory.getObject();
        String requestUrl = MessageFormat.format(
            urlTemplate,
            client.getApiUrl(),
            urlTemplateParams);
        client.close();

        try
        {
            return doRequest(HttpPost.class, requestUrl, adminUser, adminPassword, requestParams);
        }
        catch (InstantiationException | IllegalAccessException error)
        {
            throw new IllegalArgumentException("doPostRequest failed", error);
        }
    }

    /**
     * Helper method for POST requests
     *
     * @param adminUser         user with administrative privileges
     * @param adminPassword     password for adminUser
     * @param requestParams     zero or more endpoint specific request parameters
     * @param urlTemplate       request URL template
     * @param urlTemplateParams zero or more parameters used with <i>urlTemplate</i>
     */
    public boolean doPostJsonRequest(String adminUser,
                                    String adminPassword,
                                    JSONObject requestParams,
                                    String urlTemplate,
                                    String... urlTemplateParams)
    {
        AlfrescoHttpClient client = alfrescoHttpClientFactory.getObject();
        String requestUrl = MessageFormat.format(
                urlTemplate,
                client.getApiUrl(),
                urlTemplateParams);
        client.close();

        try
        {
            return doRequestJson(HttpPost.class, requestUrl, adminUser, adminPassword, requestParams);
        }
        catch (InstantiationException | IllegalAccessException error)
        {
            throw new IllegalArgumentException("doPostRequest failed", error);
        }
    }

    /**
     * Helper method for handling generic HTTP requests
     * @param requestType request type (a subclass of {@link HttpRequestBase})
     * @param requestUrl URL the request is to be sent to
     * @param adminUser user with administrative privileges
     * @param adminPassword password for adminUser
     * @param requestParams endpoint specific request parameters
     * @return response body
     * @throws IllegalAccessException for invalid <i>requestType</i>
     * @throws InstantiationException for invalid <i>requestType</i>
     */
    private <T extends HttpRequestBase> JSONObject doRequest(
        Class<T> requestType,
        String requestUrl,
        String adminUser,
        String adminPassword,
        JSONObject requestParams) throws InstantiationException, IllegalAccessException
    {
        AlfrescoHttpClient client = alfrescoHttpClientFactory.getObject();
        T request = requestType.newInstance();

        HttpResponse response = null;
        JSONObject responseBody = null;
        JSONObject returnValues = null;

        try
        {
            request.setURI(new URI(requestUrl));

            if (requestParams != null && request instanceof HttpEntityEnclosingRequestBase)
            {
                ((HttpEntityEnclosingRequestBase) request).setEntity(new StringEntity(requestParams.toString()));
            }
            response = client.execute(adminUser, adminPassword, request);

            try
            {
                responseBody = new JSONObject(EntityUtils.toString(response.getEntity()));
            }
            catch (ParseException | IOException | JSONException error)
            {
                LOGGER.error("Parsing message body failed", error);
            }

            switch (response.getStatusLine().getStatusCode())
            {
                case HttpStatus.SC_OK:
                case HttpStatus.SC_CREATED:
                    // request successful
                    if (responseBody != null)
                    {
                        returnValues = responseBody;
                    }
                    break;

                case HttpStatus.SC_INTERNAL_SERVER_ERROR:
                case HttpStatus.SC_BAD_REQUEST:
                    if (responseBody != null  && responseBody.has(EXCEPTION_KEY))
                    {
                        LOGGER.error("Request failed: " + responseBody.getString(EXCEPTION_KEY));
                    }
                    break;

                default:
                    LOGGER.error("Request returned unexpected HTTP status " + response.getStatusLine().getStatusCode());
                    break;
            }
        }
        catch (JSONException error)
        {
            LOGGER.error("Unable to extract response parameter", error);
        }
        catch (UnsupportedEncodingException | URISyntaxException error1)
        {
            LOGGER.error("Unable to construct request", error1);
        }
        finally
        {
            if (request != null)
            {
                request.releaseConnection();
            }
            client.close();
        }

        return returnValues;
    }

    private <T extends HttpRequestBase> boolean doRequestJson(
            Class<T> requestType,
            String requestUrl,
            String adminUser,
            String adminPassword,
            JSONObject requestParams) throws InstantiationException, IllegalAccessException
    {
        AlfrescoHttpClient client = alfrescoHttpClientFactory.getObject();
        T request = requestType.newInstance();

        try
        {
            request.setURI(new URI(requestUrl));
            request.setHeader("Content-Type", "application/json");

            if (requestParams != null && request instanceof HttpEntityEnclosingRequestBase)
            {
                ((HttpEntityEnclosingRequestBase) request).setEntity(new StringEntity(requestParams.toString()));
            }

            return client.execute(adminUser, adminPassword, request).getStatusLine().getStatusCode() == HttpStatus.SC_OK;
        }
        catch (UnsupportedEncodingException | URISyntaxException error1)
        {
            LOGGER.error("Unable to construct request", error1);
        }
        finally
        {
            if (request != null)
            {
                request.releaseConnection();
            }
            client.close();
        }

        return false;
    }
}
