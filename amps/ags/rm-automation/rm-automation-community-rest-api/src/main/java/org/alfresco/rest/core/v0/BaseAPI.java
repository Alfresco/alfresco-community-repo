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
package org.alfresco.rest.core.v0;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNull;

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
import org.alfresco.dataprep.ContentService;
import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
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

/**
 * The base API class containing common methods for making v0 API requests
 *
 * @author Kristijan Conkas
 * @since 2.5
 */
public abstract class BaseAPI
{
    // logger
    protected static final Logger LOGGER = LoggerFactory.getLogger(BaseAPI.class);

    /** exception key in JSON response body */
    private static final String EXCEPTION_KEY = "exception";
    private static final String MESSAGE_KEY = "message";
    public static final String NODE_PREFIX = "workspace/SpacesStore/";
    protected static final String UPDATE_METADATA_API = "{0}node/{1}/formprocessor";
    protected static final String ACTIONS_API = "{0}actionQueue";
    protected static final String RM_ACTIONS_API = "{0}rma/actions/ExecutionQueue";
    public static final String RM_SITE_ID = "rm";
    protected static final String SHARE_ACTION_API = "{0}internal/shared/share/workspace/SpacesStore/{1}";
    private static final String SLINGSHOT_PREFIX = "alfresco/s/slingshot/";

    @Autowired
    private AlfrescoHttpClientFactory alfrescoHttpClientFactory;

    @Autowired
    protected ContentService contentService;

    public static final String NODE_REF_WORKSPACE_SPACES_STORE = "workspace://SpacesStore/";
    private static final String FILE_PLAN_PATH = "/Sites/rm/documentLibrary";

    /**
     * Helper method to extract list of properties values from result.
     *
     * @param result the response
     * @return list of specified property values in result
     * @throws RuntimeException for malformed response
     */
    protected List<String> getPropertyValues(JSONObject result, String propertyName)
    {
        ArrayList<String> results = new ArrayList<>();
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
     * Helper method to extract the property value for the given nodeRef and property name
     * 
     * @param result
     * @param nodeRef
     * @param propertyName
     * @return
     */
    protected String getPropertyValue(JSONObject result, String nodeRef, String propertyName)
    {
        String propertyValue = "";
        try
        {
            JSONArray items = result.getJSONArray("items");
            for (int i = 0; i < items.length(); i++)
            {
                JSONObject item = items.getJSONObject(i);
                if(nodeRef.equals(item.getString("nodeRef")))
                {
                    propertyValue = item.getJSONObject("properties").getString(propertyName);
                }
            }
        }
        catch (JSONException error)
        {
            throw new RuntimeException("Unable to parse result", error);
        }

        return propertyValue;
    }
    
    /**
     * Helper method to extract property values from request result and put them in map as a list that corresponds to a unique property value.
     *
     * @param requestResult the request response
     * @return a map containing information about multiple properties values that correspond to a unique one
     * @throws RuntimeException for malformed response
     */
    protected Map<String, List<String>> getPropertyValuesByUniquePropertyValue(JSONObject requestResult, String uniqueProperty, List<String> otherProperties)
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
     * Retrieves the nodeRef of an item (category, folder or record) with the given path
     *
     * @param username the username
     * @param password the password
     * @param path     the path to the container eg. in case of a category it would be the category name,
     *                 in case of a folder it would be /categoryName/folderName
     *                 when trying to get File Plan, the path would be ""
     * @return the container nodeRef
     */
    public String getItemNodeRef(String username, String password, String path)
    {
        return contentService.getNodeRefByPath(username, password, FILE_PLAN_PATH + path);
    }

    /**
     * Retrieve a Cmis object by its path
     *
     * @param username the user's username
     * @param password its password
     * @param path     the object path
     * @return the object in case it exists, null if its does not exist
     */
    protected CmisObject getObjectByPath(String username, String password, String path)
    {
        CmisObject object;
        try
        {
            object = contentService.getCMISSession(username, password).getObjectByPath(path);
        } catch (CmisObjectNotFoundException notFoundError)
        {
            return null;
        }
        return object;
    }

    /**
     * Generic faceted request.
     *
     * @param username the username
     * @param password the password
     * @param parameters if the request has parameters
     * @return result object (see API reference for more details), null for any errors
     */
    protected JSONObject facetedRequest(String username, String password, List<NameValuePair> parameters, String requestURI)
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
        LOGGER.info("On GET {}, received following response: ", requestURL);
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
    protected JSONObject doGetRequest(String adminUser,
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
    protected JSONObject doDeleteRequest(String adminUser,
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
    protected JSONObject doPutRequest(String adminUser,
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
     * Helper method for PUT requests
     *
     * @param adminUser user with administrative privileges
     * @param adminPassword password for adminUser
     * @param expectedStatusCode The expected return status code.
     * @param requestParams zero or more endpoint specific request parameters
     * @param urlTemplate request URL template
     * @param urlTemplateParams zero or more parameters used with <i>urlTemplate</i>
     */
    protected HttpResponse doPutJsonRequest(String adminUser,
                String adminPassword,
                int expectedStatusCode,
                JSONObject requestParams,
                String urlTemplate,
                String... urlTemplateParams)
    {
        AlfrescoHttpClient client = alfrescoHttpClientFactory.getObject();
        return doPutJsonRequest(adminUser, adminPassword, expectedStatusCode, client.getApiUrl(), requestParams, urlTemplate, urlTemplateParams);
    }

    /**
     * Helper method for PUT requests
     *
     * @param adminUser user with administrative privileges
     * @param adminPassword password for adminUser
     * @param expectedStatusCode The expected return status code.
     * @param urlStart the start of the URL (for example "alfresco/s/slingshot").
     * @param requestParams zero or more endpoint specific request parameters
     * @param urlTemplate request URL template
     * @param urlTemplateParams zero or more parameters used with <i>urlTemplate</i>
     * @throws AssertionError if the returned status code is not as expected.
     */
    private HttpResponse doPutJsonRequest(String adminUser,
                String adminPassword,
                int expectedStatusCode,
                String urlStart,
                JSONObject requestParams,
                String urlTemplate,
                String... urlTemplateParams)
    {
        String requestUrl = formatRequestUrl(urlStart, urlTemplate, urlTemplateParams);
        try
        {
            HttpResponse httpResponse = doRequestJson(HttpPut.class, requestUrl, adminUser, adminPassword, requestParams);
            assertEquals("PUT request to " + requestUrl + " was not successful.", expectedStatusCode, httpResponse.getStatusLine().getStatusCode());
            return httpResponse;
        }
        catch (InstantiationException | IllegalAccessException error)
        {
            throw new IllegalArgumentException("doPutRequest failed", error);
        }
    }

    /**
     * Fill in the parameters for a URL template.
     *
     * @param urlStart The start of the URL.
     * @param urlTemplate The template.
     * @param urlTemplateParams Any parameters that need to be filled into the URL template.
     * @return The resultant URL.
     */
    private String formatRequestUrl(String urlStart, String urlTemplate, String[] urlTemplateParams)
    {
        if (urlTemplateParams.length == 1)
        {
            // The format method needs some help to know not to use the whole array object.
            return MessageFormat.format(urlTemplate, urlStart, urlTemplateParams[0]);
        }
        return MessageFormat.format(urlTemplate, urlStart, urlTemplateParams);
    }

    /**
     * Helper method for POST requests
     * @param adminUser user with administrative privileges
     * @param adminPassword password for adminUser
     * @param requestParams zero or more endpoint specific request parameters
     * @param urlTemplate request URL template
     * @param urlTemplateParams zero or more parameters used with <i>urlTemplate</i>
     */
    protected JSONObject doPostRequest(String adminUser,
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
     * @param adminUser user with administrative privileges
     * @param adminPassword password for adminUser
     * @param expectedStatusCode The expected return status code.
     * @param requestParams zero or more endpoint specific request parameters
     * @param urlTemplate request URL template
     * @param urlTemplateParams zero or more parameters used with <i>urlTemplate</i>
     */
    protected HttpResponse doPostJsonRequest(String adminUser,
                                    String adminPassword,
                                    int expectedStatusCode,
                                    JSONObject requestParams,
                                    String urlTemplate,
                                    String... urlTemplateParams)
    {
        AlfrescoHttpClient client = alfrescoHttpClientFactory.getObject();
        return doPostJsonRequest(adminUser, adminPassword, expectedStatusCode, client.getApiUrl(), requestParams, urlTemplate, urlTemplateParams);
    }

    /**
     * Helper method for POST requests to slingshot.
     *
     * @param adminUser user with administrative privileges
     * @param adminPassword password for adminUser
     * @param expectedStatusCode The expected return status code.
     * @param requestParams zero or more endpoint specific request parameters
     * @param urlTemplate request URL template
     * @param urlTemplateParams zero or more parameters used with <i>urlTemplate</i>
     */
    protected HttpResponse doSlingshotPostJsonRequest(String adminUser,
                String adminPassword,
                int expectedStatusCode,
                JSONObject requestParams,
                String urlTemplate,
                String... urlTemplateParams)
    {
        AlfrescoHttpClient client = alfrescoHttpClientFactory.getObject();
        return doPostJsonRequest(adminUser, adminPassword, expectedStatusCode, client.getAlfrescoUrl() + SLINGSHOT_PREFIX, requestParams, urlTemplate, urlTemplateParams);
    }

    /**
     * Helper method for POST requests
     *
     * @param adminUser user with administrative privileges
     * @param adminPassword password for adminUser
     * @param expectedStatusCode The expected return status code.
     * @param urlStart the start of the URL (for example "alfresco/s/slingshot").
     * @param requestParams zero or more endpoint specific request parameters
     * @param urlTemplate request URL template
     * @param urlTemplateParams zero or more parameters used with <i>urlTemplate</i>
     * @throws AssertionError if the returned status code is not as expected.
     */
    private HttpResponse doPostJsonRequest(String adminUser,
                String adminPassword,
                int expectedStatusCode,
                String urlStart,
                JSONObject requestParams,
                String urlTemplate,
                String... urlTemplateParams)
    {
        String requestUrl;
        requestUrl = formatRequestUrl(urlStart, urlTemplate, urlTemplateParams);
        try
        {
            HttpResponse httpResponse = doRequestJson(HttpPost.class, requestUrl, adminUser, adminPassword, requestParams);
            assertEquals("POST request to " + requestUrl + " was not successful.", expectedStatusCode, httpResponse.getStatusLine().getStatusCode());
            return httpResponse;
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

        JSONObject responseBody = null;
        JSONObject returnValues = null;

        try
        {
            request.setURI(new URI(requestUrl));

            if (requestParams != null && request instanceof HttpEntityEnclosingRequestBase)
            {
                ((HttpEntityEnclosingRequestBase) request).setEntity(new StringEntity(requestParams.toString()));
            }
            LOGGER.info("Sending {} request to {}", requestType.getSimpleName(), requestUrl);
            LOGGER.info("Request body: {}", requestParams);
            HttpResponse response = client.execute(adminUser, adminPassword, request);
            LOGGER.info("Response: {}", response.getStatusLine());

            try
            {
                responseBody = new JSONObject(EntityUtils.toString(response.getEntity()));
            }
            catch (JSONException error)
            {
                LOGGER.error("Converting message body to JSON failed. Body: {}", responseBody, error);
            }
            catch (ParseException | IOException error)
            {
                LOGGER.error("Parsing message body failed.", error);
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
					if (responseBody != null  && responseBody.has(EXCEPTION_KEY))
                    {
                        LOGGER.error("Request failed with error message: {}", responseBody.getString(MESSAGE_KEY));
                        returnValues = responseBody;
                    }
                    break;
                case HttpStatus.SC_BAD_REQUEST:
                case HttpStatus.SC_UNPROCESSABLE_ENTITY:
                    if (responseBody != null  && responseBody.has(EXCEPTION_KEY))
                    {
                        LOGGER.error("Request failed: {}", responseBody.getString(EXCEPTION_KEY));
                        returnValues = responseBody;
                    }
                    break;

                default:
                    LOGGER.error("Request returned unexpected HTTP status {}", response.getStatusLine().getStatusCode());
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

    private <T extends HttpRequestBase> HttpResponse doRequestJson(
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

            LOGGER.info("Sending {} request to {}", requestType.getSimpleName(), requestUrl);
            LOGGER.info("Request body: {}", requestParams);
            HttpResponse httpResponse = client.execute(adminUser, adminPassword, request);
            LOGGER.info("Response: {}", httpResponse.getStatusLine());
            return httpResponse;
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

        return null;
    }

    /**
     * Used to set RM items properties
     * including records, categories and folders
     */
    public enum RMProperty
    {
        NAME,
        TITLE,
        CONTENT,
        DESCRIPTION,
        AUTHOR,
        PHYSICAL_SIZE,
        NUMBER_OF_COPIES,
        STORAGE_LOCATION,
        SHELF,
        BOX,
        FILE,
        ORIGINATOR,
        ORIGINATING_ORGANIZATION,
        PUBLICATION_DATE
    }

    public enum RETENTION_SCHEDULE
    {
        NAME,
        DESCRIPTION,
        RETENTION_AUTHORITY,
        RETENTION_INSTRUCTIONS,
        RETENTION_PERIOD,
        RETENTION_LOCATION,
        RETENTION_PERIOD_PROPERTY,
        RETENTION_GHOST,
        RETENTION_ELIGIBLE_FIRST_EVENT,
        RETENTION_EVENTS,
        COMBINE_DISPOSITION_STEP_CONDITIONS
    }

    /**
     * Used to execute rm actions on a node
     */
    public enum RM_ACTIONS
    {
        EDIT_DISPOSITION_DATE("editDispositionActionAsOfDate"),
        END_RETENTION("retain"),
        CUT_OFF("cutoff"),
        UNDO_CUT_OFF("undoCutoff"),
        TRANSFER("transfer"),
        COMPLETE_EVENT("completeEvent"),
        UNDO_EVENT("undoEvent"),
        DESTROY("destroy");
        String action;

        private RM_ACTIONS(String action)
        {
            this.action = action;
        }

        public String getAction()
        {
            return action;
        }
    }

    public enum PermissionType
    {
        SET_READ,
        REMOVE_READ,
        SET_READ_AND_FILE,
        REMOVE_READ_AND_FILE,
    }

    /**
     * Util to return the property value from a map
     *
     * @param properties the map containing properties
     * @param property   to get value for
     * @return the property value
     */
    public <K extends Enum<?>> String getPropertyValue(Map<K, String> properties, Enum<?> property)
    {
        String value = properties.get(property);
        if (value == null)
        {
            return "";
        }
        return value;
    }

    /**
     * Retrieves the property value and decides if that gets to be added to the request
     *
     * @param requestParams        the request parameters
     * @param propertyRequestValue the property name in the request, eg. "prop_cm_name"
     * @param itemProperties       map of item's properties values
     * @param property             the property in the property map to check value for
     * @return the json object used in request with the property with its value added if that is not null or empty
     */
    protected <K extends Enum<?>> JSONObject addPropertyToRequest(JSONObject requestParams, String propertyRequestValue, Map<K, String> itemProperties, Enum<?> property) throws JSONException
    {
        String propertyValue = getPropertyValue(itemProperties, property);

        if (!propertyValue.equals(""))
        {
            requestParams.put(propertyRequestValue, propertyValue);
        }
        return requestParams;
    }

    /**
     * Deletes the category, folder or record given as parameter
     *
     * @param username the username with whom the delete is performed
     * @param password the user's password
     * @param itemPath the path to the item eg. in case of a category it would be the "/" + category name,
     *                 in case of a folder or subCategory it would be /categoryName/folderName or /categoryName/subCategoryName/
     *                 in case of a record /categoryName/folderName/recordName
     * @throws AssertionError if the delete was not successful.
     */
    protected void deleteItem(String username, String password, String itemPath)
    {
        CmisObject container = getObjectByPath(username, password, FILE_PLAN_PATH + itemPath);
        if (container != null)
        {
            container.delete();
        }
        assertNull("Could not delete " + itemPath, getObjectByPath(username, password, itemPath));
    }

    /**
     * Retrieve the node ref spaces store value
     *
     * @return node ref spaces store
     */
    public static String getNodeRefSpacesStore()
    {
        return NODE_REF_WORKSPACE_SPACES_STORE;
    }

    /**
     * Retrieve the File Plan path
     *
     * @return the File Plan path
     */
    public static String getFilePlanPath()
    {
        return FILE_PLAN_PATH;
    }
}
