/*-
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
package org.alfresco.rest.v0;

import static org.alfresco.rest.core.v0.APIUtils.convertHTTPResponseToJSON;
import static org.apache.http.HttpStatus.SC_OK;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.alfresco.rest.core.v0.APIUtils;
import org.alfresco.rest.core.v0.BaseAPI;
import org.alfresco.rest.rm.community.model.hold.HoldEntry;
import org.alfresco.rest.rm.community.util.PojoUtility;
import org.alfresco.utility.model.UserModel;
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

    /**
     * The URI to add items to hold or to remove items from hold
     */
    private static final String RM_HOLDS_API = "{0}rma/holds";

    /**
     * The URI to  get holds.
     */
    private static final String GET_RM_HOLDS = RM_HOLDS_API + "?{1}";

    /**
     * Util method to create a hold
     *
     * @param user        the user creating the hold
     * @param password    the user's password
     * @param holdName    the hold name
     * @param reason      hold reason
     * @param description hold description
     * @return The HTTP response.
     */
    public HttpResponse createHold(String user, String password, String holdName, String reason, String description)
    {
        return createHold(user, password, holdName, reason, description, SC_OK);
    }

    /**
     * Util method to create a hold
     *
     * @param user        the user creating the hold
     * @param password    the user's password
     * @param holdName    the hold name
     * @param reason      hold reason
     * @param description hold description
     * @param expectedStatusCode The expected return status code.
     * @return The HTTP response or throws AssertionError if the returned status code is not as expected.
     */
    public HttpResponse createHold(String user, String password, String holdName, String reason, String description,
                                   int expectedStatusCode)
    {
        // retrieve the Holds container nodeRef
        final String parentNodeRef = getItemNodeRef(user, password, "/" + HOLDS_CONTAINER);

        final JSONObject requestParams = new JSONObject();
        requestParams.put("alf_destination", getNodeRefSpacesStore() + parentNodeRef);
        requestParams.put("prop_cm_name", holdName);
        requestParams.put("prop_cm_description", description);
        requestParams.put("prop_rma_holdReason", reason);

        return doPostJsonRequest(user, password, expectedStatusCode, requestParams, CREATE_HOLDS_API);
    }

    /**
     * Create a hold and get the node ref of the hold from the response body
     *
     * @param user        the user creating the hold
     * @param password    the user's password
     * @param holdName    the hold name to be created
     * @param reason      reason of the hold to be created
     * @param description hold description
     * @return node ref of the hold created
     */
    public String createHoldAndGetNodeRef(String user, String password,
                                     String holdName, String reason, String description)
    {
        final HttpResponse httpResponse = createHold(user, password, holdName, reason, description);

        try
        {
           return convertHTTPResponseToJSON(httpResponse).getString("persistedObject")
                                .replace(NODE_REF_WORKSPACE_SPACES_STORE, "");
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
     * Deletes hold using RM Actions API and expect action to be successful
     *
     * @param user the user who does the request
     * @param holdNodeRef the hold node ref
     * @return The HTTP Response or throws AssertionError if the request is not successful.
     */
    public HttpResponse deleteHold(UserModel user, String holdNodeRef)
    {
        return deleteHold(user.getUsername(), user.getPassword(), holdNodeRef, SC_OK);
    }

    /**
     * Deletes hold using RM Actions API and expect a specific status code
     *
     * @param username user's username
     * @param password its password
     * @param holdNodeRef the hold node ref
     * @return The HTTP Response or throws AssertionError if the returned status code is not as expected.
     */
    public HttpResponse deleteHold(String username, String password, String holdNodeRef, int expectedStatusCode)
    {
        JSONObject requestParams = new JSONObject();
        requestParams.put("name", "deleteHold");
        requestParams.put("nodeRef", getNodeRefSpacesStore() + holdNodeRef);

        return doPostJsonRequest(username, password, expectedStatusCode, requestParams, RM_ACTIONS_API);
    }

    /**
     * Deletes hold using cmis
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
     * Adds item(content/record/record folder) to the hold
     *
     * @param user        the user who adds the item to the hold
     * @param password    the user's password
     * @param itemNodeRef the nodeRef of the item to be added to hold
     * @param holdName    the hold name
     * @return The HTTP response
     */
    public HttpResponse addItemToHold(String user, String password, String itemNodeRef, String holdName)
    {
        return addItemsToHolds(user, password, Collections.singletonList(itemNodeRef), Collections.singletonList(holdName));
    }

    /**
     * Adds a list of items (content/record/record folder) to a list of holds
     *
     * @param user         the user who adds the items to the holds
     * @param password     the user's password
     * @param itemNodeRefs the list of items nodeRefs to be added to holds
     * @param holdNames    the list of holds
     * @return The HTTP response
     */
    public HttpResponse addItemsToHolds(String user, String password, List<String> itemNodeRefs, List<String> holdNames)
    {
        final List<String> holdNodeRefs = holdNames.stream()
                                                   .map(hold -> getItemNodeRef(user, password, String.format("/%s/%s", HOLDS_CONTAINER, hold)))
                                                   .collect(Collectors.toList());
        return addItemsToHolds(user, password, SC_OK, itemNodeRefs, holdNodeRefs);
    }

    /**
     * Adds a list of items (content/record/record folder) to a list of holds
     *
     * @param user         the user who adds the items to the holds
     * @param password     the user's password
     * @param itemNodeRefs the list of items nodeRefs to be added to holds
     * @param holdNodeRefs the list of holds
     * @return The HTTP response
     */
    public HttpResponse addItemsToHolds(String user, String password, int expectedStatus, List<String> itemNodeRefs,
                                        List<String> holdNodeRefs)
    {
        final JSONObject requestParams = addOrRemoveToFromHoldJsonObject(itemNodeRefs, holdNodeRefs);
        return doPostJsonRequest(user, password, expectedStatus, requestParams, RM_HOLDS_API);
    }

    /**
     * Util method to add item(content/record/record folder) to the hold and get the error message
     *
     * @param user        the user who adds the item to the hold
     * @param password    the user's password
     * @param itemNodeRef the nodeRef of the item to be added to hold
     * @param holdNodeRef the hold node ref
     * @return The error message
     */
    public String addToHoldAndGetMessage(String user, String password, int expectedStatus, String itemNodeRef, String
            holdNodeRef)
    {
        final HttpResponse httpResponse = addItemsToHolds(user, password, expectedStatus, Collections.singletonList(itemNodeRef),
                Collections.singletonList(holdNodeRef));
        return APIUtils.extractErrorMessageFromHttpResponse(httpResponse);
    }

    /**
     * Util method to create the request body used when adding items to holds or when removing items from holds
     *
     * @param items        list of items node refs to be added to holds
     * @param holdNodeRefs list of hold node refs for add/remove items
     * @return JSONObject fo
     */
    private JSONObject addOrRemoveToFromHoldJsonObject(List<String> items, List<String> holdNodeRefs)
    {
        final JSONArray nodeRefs = new JSONArray();
        items.forEach(itemNodeRef -> nodeRefs.put(getNodeRefSpacesStore() + itemNodeRef));
        final JSONArray holds = new JSONArray();
        holdNodeRefs.forEach(holdNodeRef -> holds.put(getNodeRefSpacesStore() + holdNodeRef));
        final JSONObject requestParams = new JSONObject();
        requestParams.put("nodeRefs", nodeRefs);
        requestParams.put("holds", holds);
        return requestParams;
    }

    /**
     * Remove item(content/record/record folder) from hold
     *
     * @param user        the user who removes the item from the hold
     * @param password    the user's password
     * @param itemNodeRef the nodeRef of the item to be removed from hold
     * @param holdName    the hold name
     * @return The HTTP response
     */
    public HttpResponse removeItemFromHold(String user, String password, String itemNodeRef, String holdName)
    {
        return removeItemsFromHolds(user, password, Collections.singletonList(itemNodeRef), Collections.singletonList(holdName));
    }

    /**
     * Remove a list of items (content/record/record folder) from a list of holds
     *
     * @param user           the user who removes the item from the hold
     * @param password       the user's password
     * @param itemNodeRefs   the list of items nodeRefs to be removed from hold
     * @param holdNames      the list of hold names
     * @return The HTTP response
     */
    public HttpResponse removeItemsFromHolds(String user, String password, List<String> itemNodeRefs, List<String> holdNames)
    {
        final List<String> holdNodeRefs = holdNames.stream()
                                                   .map(hold -> getItemNodeRef(user, password, String.format("/%s/%s", HOLDS_CONTAINER, hold)))
                                                   .collect(Collectors.toList());
        return removeItemsFromHolds(user, password, SC_OK, itemNodeRefs, holdNodeRefs);
    }

    /**
     * Remove a list of items (content/record/record folder) from a list of holds
     *
     * @param user           the user who removes the item from the hold
     * @param password       the user's password
     * @param expectedStatus https status code expected
     * @param itemNodeRefs   the list of items nodeRefs to be removed from hold
     * @param holdNodeRefs   the list of hold node refs
     * @return The HTTP response
     */
    public HttpResponse removeItemsFromHolds(String user, String password, int expectedStatus, List<String> itemNodeRefs,
                                             List<String> holdNodeRefs)
    {
        final JSONObject requestParams = addOrRemoveToFromHoldJsonObject(itemNodeRefs, holdNodeRefs);
        return doPutJsonRequest(user, password, expectedStatus, requestParams, RM_HOLDS_API);
    }

    /**
     * Util method to remove item(content/record/record folder) from hold and get the error message
     *
     * @param user        the user who removes the item from hold
     * @param password    the user's password
     * @param itemNodeRef the nodeRef of the item to be removed from hold
     * @param holdNodeRef the hold node ref
     * @return The error message
     */
    public String removeFromHoldAndGetMessage(String user, String password, int expectedStatus, String itemNodeRef, String
            holdNodeRef)
    {
        final HttpResponse httpResponse = removeItemsFromHolds(user, password, expectedStatus, Collections.singletonList(itemNodeRef),
                Collections.singletonList(holdNodeRef));
        return APIUtils.extractErrorMessageFromHttpResponse(httpResponse);
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
