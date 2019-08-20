/*-
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2019 Alfresco Software Limited
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
package org.alfresco.rest.v0;

import static org.alfresco.rest.core.v0.APIUtils.convertHTTPResponseToJSON;
import static org.apache.http.HttpStatus.SC_OK;
import static org.testng.AssertJUnit.assertNotNull;

import javax.json.Json;
import javax.json.JsonReader;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.alfresco.rest.core.v0.BaseAPI;
import org.alfresco.rest.rm.community.model.hold.HoldEntry;
import org.alfresco.rest.rm.community.util.PojoUtility;
import org.alfresco.utility.Utility;
import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

/**
 * Methods to make API requests using v0 API for generalized holds
 *
 * @author Rodica Sutu
 * @since 3.2
 */
@Component
public class HoldsAPI extends BaseAPI
{
    public static final String HOLDS_CONTAINER = "Holds";
    /**
     * The URI to create a hold
     */
    private static final String CREATE_HOLDS_API = "{0}type/rma:hold/formprocessor";

    /** The URI to add items to hold.*/
    private static final String RM_HOLDS_API = "{0}rma/holds";
    /**
     * The URI to  get holds.
     */
    private static final String GET_RM_HOLDS = RM_HOLDS_API + "?{1}";

    /**
     * Util method to create a hold
     *
     * @param user        the user creating the category
     * @param password    the user's password
     * @param holdName    the hold name
     * @param reason      hold reason
     * @param description hold description
     * @return The HTTP response (or null if no POST call was needed).
     */
    public HttpResponse createHold(String user, String password,
                                          String holdName, String reason, String description)
    {
        // if the hold already exists don't try to create it again
        final String fullHoldPath = Utility.buildPath(getFilePlanPath(), HOLDS_CONTAINER) + holdName;
        final CmisObject hold = getObjectByPath(user, password, fullHoldPath);
        if (hold != null)
        {
            return null;
        }
        // retrieve the Holds container nodeRef
        final String parentNodeRef = getItemNodeRef(user, password, "/" + HOLDS_CONTAINER);

        final JSONObject requestParams = new JSONObject();
        requestParams.put("alf_destination", getNodeRefSpacesStore() + parentNodeRef);
        requestParams.put("prop_cm_name", holdName);
        requestParams.put("prop_cm_description", description);
        requestParams.put("prop_rma_holdReason", reason);

        // Make the POST request and throw an assertion error if it fails.
        final HttpResponse httpResponse = doPostJsonRequest(user, password, SC_OK, requestParams, CREATE_HOLDS_API);
        assertNotNull("Expected object to have been created at " + fullHoldPath,
                getObjectByPath(user, password, fullHoldPath));
        return httpResponse;
    }

    /**
     *  Create a hold and get the node ref of the hold from the response body
     *
     * @param user
     * @param password
     * @param holdName
     * @param reason
     * @param description
     * @return node ref of the hold created
     */

    public String createHoldAndGetNodeRef(String user, String password,
                                     String holdName, String reason, String description)
    {
        final HttpResponse httpResponse = createHold(user, password, holdName, reason, description);

        try
        {
           return convertHTTPResponseToJSON(httpResponse).getString("persistedObject")
                                .replaceAll(NODE_REF_WORKSPACE_SPACES_STORE, "");
        }
        catch(JSONException error)
        {
            LOGGER.error("Converting message body to JSON failed. Body: {}", httpResponse, error);
        }
        catch(ParseException error)
        {
            LOGGER.error("Parsing message body failed.", error);
        }

        return null;
    }



    /**
     * Deletes hold
     *
     * @param username user's username
     * @param password its password
     * @param holdName the hold name
     * @throws AssertionError if the deletion was unsuccessful.
     */
    public void deleteHold( String username, String password, String holdName)
    {
        deleteItem(username, password, String.format("/%s/%s", HOLDS_CONTAINER, holdName));
    }

    /**
     * Adds item (active content /record/ record folder) to the hold
     *
     * @param user        the user who adds the item to the hold
     * @param password    the user's password
     * @param itemNodeRef the nodeRef of the item to be added to hold
     * @param holdName    the hold name
     * @return The HTTP response
     */
    public HttpResponse addItemToHold(String user, String password, String itemNodeRef, String holdName)
    {
        return addItemToHold(user, password, SC_OK, itemNodeRef, holdName);
    }

    /**
     * Adds item (record/ record folder) to the hold
     *
     * @param user        the user who adds the item to the hold
     * @param password    the user's password
     * @param itemNodeRef the nodeRef of the item to be added to hold
     * @param holdName    the hold name
     * @return The HTTP response
     */
    public HttpResponse addItemToHold(String user, String password, int expectedStatus, String itemNodeRef,
                                      String holdName)
    {
        final JSONObject requestParams = addToHoldJsonObject(user, password, itemNodeRef, holdName);
        return doPostJsonRequest(user, password, expectedStatus, requestParams, RM_HOLDS_API);
    }

    /**
     * Util method to add item (active content /record/ record folder) to the hold and get the error message
     *
     * @param user        the user who adds the item to the hold
     * @param password    the user's password
     * @param itemNodeRef the nodeRef of the item to be added to hold
     * @param holdName    the hold name
     * @return The error message
     */
    public String addToHoldAndGetMessage(String user, String password, int expectedStatus, String itemNodeRef, String
            holdName)
    {
        final HttpResponse httpResponse = addItemToHold(user, password, expectedStatus, itemNodeRef, holdName);
        return extractErrorMessageFromHttpResponse(httpResponse);
    }

    /**
     * Util method to create the request body for adding an item to hold
     * @param user
     * @param password
     * @param itemNodeRef
     * @param holdName
     * @return JSONObject fo
     */
    private JSONObject addToHoldJsonObject(String user, String password, String itemNodeRef, String holdName)
    {

        final JSONArray nodeRefs = new JSONArray().put(getNodeRefSpacesStore() + itemNodeRef);
        final List<String> holdNames = Arrays.asList(holdName.split(","));
        final List<String> holdNoderefs = holdNames.stream().map(hold ->

                getNodeRefSpacesStore() + getItemNodeRef(user, password, String.format("/%s/%s", HOLDS_CONTAINER, hold)))
                                             .collect(Collectors.toList());
        final JSONArray holds = new JSONArray();
        holdNoderefs.forEach(holds::put);
        final JSONObject requestParams = new JSONObject();
        requestParams.put("nodeRefs", nodeRefs);
        requestParams.put("holds", holds);
        return requestParams;
    }

    /**
     * Remove item (active content /record/ record folder) from the hold
     *
     * @param user        the user who adds the item to the hold
     * @param password    the user's password
     * @param itemNodeRef the nodeRef of the item to be added to hold
     * @param holdName    the hold name
     * @return The HTTP response
     */
    public HttpResponse removeItemFromHold(String user, String password, String itemNodeRef, String holdName)
    {
        return removeItemFromHold(user, password, SC_OK, itemNodeRef, holdName);
    }

    /**
     * Remove item (record/ record folder) to the hold
     *
     * @param user        the user who adds the item to the hold
     * @param password    the user's password
     * @param expectedStatus  https status code expected
     * @param itemNodeRef the nodeRef of the item to be added to hold
     * @param holdName    the hold name
     * @return The HTTP response
     */
    public HttpResponse removeItemFromHold(String user, String password, int expectedStatus, String itemNodeRef, String
            holdName)
    {
        final JSONObject requestParams = addToHoldJsonObject(user, password, itemNodeRef, holdName);
        return doPutJsonRequest(user, password, expectedStatus, requestParams, RM_HOLDS_API);
    }

    /**
     * Util method to remove item (active content /record/ record folder) from hold and get the error message
     *
     * @param user        the user who adds the item to the hold
     * @param password    the user's password
     * @param itemNodeRef the nodeRef of the item to be added to hold
     * @param holdName    the hold name
     * @return The error message
     */
    public String removeFromHoldAndGetMessage(String user, String password, int expectedStatus, String itemNodeRef, String
            holdName)
    {
        final HttpResponse httpResponse = removeItemFromHold(user, password, expectedStatus, itemNodeRef, holdName);
        return extractErrorMessageFromHttpResponse(httpResponse);
    }

    /**
     * Util method to extract the message string from the HTTP response
     * @param httpResponse
     * @return
     */
    private String extractErrorMessageFromHttpResponse(HttpResponse httpResponse)
    {
        final HttpEntity entity = httpResponse.getEntity();
        JsonReader reader = null;
        try
        {
            final InputStream responseStream = entity.getContent();
            reader = Json.createReader(responseStream);
            return reader.readObject().getString("message");
        }
        catch (JSONException error)
        {
            LOGGER.error("Converting message body to JSON failed. Body: {}", httpResponse, error);
        }
        catch (ParseException | IOException error)
        {
            LOGGER.error("Parsing message body failed.", error);
        }
        finally
        {
            if (reader != null)
            {
                reader.close();
            }
        }
        return null;
    }

    /**
     *  Get the list of the available holds which have the item node reference if includedInHold parameter is true,
     *   otherwise a list of hold node references will be retrieved which do not include the given node reference.
     *
     * @param user     The username of the user to use.
     * @param password The password of the user.
     * @param itemNodeRef The item node reference
     * @param includedInHold True to retrieve the holds which have the item node reference
     * @param fileOnly True if only files should be return
     * @return return a list of hold entries
     */
    public List<HoldEntry> getHolds(String user, String password, final String itemNodeRef,
                                    final Boolean includedInHold, final Boolean fileOnly)
    {
        final String parameters = (itemNodeRef != null ? "itemNodeRef=" + NODE_REF_WORKSPACE_SPACES_STORE + itemNodeRef  : "")
                             + (includedInHold != null ? "&includedInHold=" + includedInHold : "")
                             + (fileOnly != null ? "&fileOnly=" + fileOnly : "");

        final JSONArray holdEntries = doGetRequest(user, password,
                MessageFormat.format(GET_RM_HOLDS, "{0}", parameters)).getJSONObject("data").getJSONArray("holds");

        return PojoUtility.jsonToObject(holdEntries, HoldEntry.class);
    }
}
