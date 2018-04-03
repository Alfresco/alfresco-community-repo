/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2018 Alfresco Software Limited
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

import static org.alfresco.dataprep.AlfrescoHttpClient.MIME_TYPE_JSON;
import static org.apache.http.HttpStatus.SC_OK;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;

import java.io.IOException;
import java.text.MessageFormat;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

import org.alfresco.dataprep.AlfrescoHttpClient;
import org.alfresco.dataprep.AlfrescoHttpClientFactory;
import org.alfresco.dataprep.ContentService;
import org.alfresco.dataprep.UserService;
import org.alfresco.rest.core.v0.BaseAPI;
import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Methods to make API requests using v0 API on RM items (move, update and other actions) including adding users to RM roles
 *
 * @author Oana Nechiforescu
 * @since 2.5
 */
@Component
public class RMRolesAndActionsAPI extends BaseAPI
{
    /** The URI to view the configured roles and capabilities. */
    private static final String RM_ROLES = "{0}rma/admin/rmroles";
    /** The URI for REST requests about a particular configured role. */
    private static final String RM_ROLES_ROLE = RM_ROLES + "/{1}";
    private static final String RM_ROLES_AUTHORITIES = "{0}rm/roles/{1}/authorities/{2}?alf_ticket={3}";

    // logger
    private static final Logger LOGGER = LoggerFactory.getLogger(RMRolesAndActionsAPI.class);
    private static final String MOVE_ACTIONS_API = "action/rm-move-to/site/rm/documentLibrary/{0}";
    private static final String CREATE_HOLDS_API = "{0}type/rma:hold/formprocessor";

    /** http client factory */
    @Autowired
    private AlfrescoHttpClientFactory alfrescoHttpClientFactory;

    /** user service */
    @Autowired
    private UserService userService;

    @Autowired
    private ContentService contentService;

    /**
     * Get all the configured RM roles.
     *
     * @param adminUser The RM admin user.
     * @param adminPassword The password of the user.
     * @return The RM roles in the system (Note that this will be the internal names, not the display labels).
     */
    public Set<String> getConfiguredRoles(String adminUser, String adminPassword)
    {
        // Using "is=true" includes the in-place readers and writers.
        JSONObject jsonObject = doGetRequest(adminUser, adminPassword, RM_ROLES + "?is=true").getJSONObject("data");
        return jsonObject.toMap().keySet();
    }

    /**
     * Get the capabilities for a given role.
     *
     * @param adminUser The RM admin user.
     * @param adminPassword The password of the user.
     * @param role The role to get capabilities for.
     * @return The set of system names for the capabilities.
     */
    public Set<String> getCapabilitiesForRole(String adminUser, String adminPassword, String role)
    {
        JSONObject jsonObject = doGetRequest(adminUser, adminPassword, RM_ROLES + "?is=true").getJSONObject("data");
        assertTrue("Could not find role '" + role + "' in " + jsonObject.keySet(), jsonObject.has(role));
        return jsonObject.getJSONObject(role).getJSONObject("capabilities").keySet();
    }

    /**
     * Create a new RM role.
     *
     * @param adminUser The username of the admin user.
     * @param adminPassword The password for the admin user.
     * @param roleName The name of the new role.
     * @param roleDisplayLabel A human-readable label for the role.
     * @param capabilities A list of capabilities for the role.
     */
    public void createRole(String adminUser, String adminPassword, String roleName, String roleDisplayLabel, Set<String> capabilities)
    {
        JSONObject requestBody = new JSONObject();
        requestBody.put("name", roleName);
        requestBody.put("displayLabel", roleDisplayLabel);
        JSONArray capabilitiesArray = new JSONArray();
        capabilities.forEach(capabilitiesArray::put);
        requestBody.put("capabilities", capabilitiesArray);
        doPostJsonRequest(adminUser, adminPassword, HttpStatus.SC_OK, requestBody, RM_ROLES);
    }

    /**
     * Update an existing RM role.
     *
     * @param adminUser The username of the admin user.
     * @param adminPassword The password for the admin user.
     * @param roleName The name of the new role.
     * @param roleDisplayLabel A human-readable label for the role.
     * @param capabilities A list of capabilities for the role.
     */
    public void updateRole(String adminUser, String adminPassword, String roleName, String roleDisplayLabel, Set<String> capabilities)
    {
        JSONObject requestBody = new JSONObject();
        requestBody.put("name", roleName);
        requestBody.put("displayLabel", roleDisplayLabel);
        JSONArray capabilitiesArray = new JSONArray();
        capabilities.forEach(capabilitiesArray::put);
        requestBody.put("capabilities", capabilitiesArray);
        doPutJsonRequest(adminUser, adminPassword, HttpStatus.SC_OK, requestBody, RM_ROLES_ROLE, roleName);
    }

    /** Delete a created RM role.
     *
     * @param adminUser The username of the admin user.
     * @param adminPassword The password for the admin user.
     * @param roleName The name of the role to be deleted.
     */
    public void deleteRole(String adminUser, String adminPassword, String roleName)
    {
        doDeleteRequest(adminUser, adminPassword, MessageFormat.format(RM_ROLES_ROLE, "{0}",roleName));
        boolean success = !getConfiguredRoles(adminUser, adminPassword).contains(roleName);
        assertTrue("Failed to delete role " + roleName + " with " + adminUser, success);
    }

    /**
     * create user and assign to records management role
     */
    public void createUserAndAssignToRole(
            String adminUser,
            String adminPassword,
            String userName,
            String password,
            String email,
            String role,
            String firstName,
            String lastName)
    {
        if (!userService.userExists(adminUser, adminPassword, userName))
        {
            userService.create(adminUser, adminPassword, userName, password, email, firstName, lastName);
        }
        assignRoleToUser(adminUser, adminPassword, userName, role);
    }

    /**
     * Assign a records management role to a user.
     *
     * @throws AssertionError if the assignation is unsuccessful.
     */
    public void assignRoleToUser(String adminUser, String adminPassword, String userName, String role)
    {
        AlfrescoHttpClient client = alfrescoHttpClientFactory.getObject();
        String reqURL = MessageFormat.format(
                RM_ROLES_AUTHORITIES,
                client.getApiUrl(),
                role,
                userName,
                client.getAlfTicket(adminUser, adminPassword));

        HttpPost request = null;
        HttpResponse response;
        try
        {
            request = new HttpPost(reqURL);
            response = client.execute(adminUser, adminPassword, request);
        }
        finally
        {
            if (request != null)
            {
                request.releaseConnection();
            }
            client.close();
        }
        assertEquals("Assigning role " + role + " to user " + userName + " failed.", SC_OK,
                    response.getStatusLine().getStatusCode());
    }

    /**
     * Move action
     *
     * @param user            the user to move the contentPath
     * @param password        the user's password
     * @param contentPath     path to the content to be moved
     * @param destinationPath destination path
     * @throws AssertionError if the move was unsuccessful.
     */
    public void moveTo(String user, String password, String contentPath, String destinationPath)
    {
        String contentNodeRef = getNodeRefSpacesStore() + getItemNodeRef(user, password, contentPath);
        AlfrescoHttpClient client = alfrescoHttpClientFactory.getObject();
        String url = MessageFormat.format(client.getAlfrescoUrl() + "alfresco/s/slingshot/doclib/" + MOVE_ACTIONS_API, destinationPath);
        HttpPost request = new HttpPost(url);

        boolean success = false;
        try
        {
            JSONObject body = new JSONObject();
            body.put("nodeRefs", new JSONArray(Arrays.asList(contentNodeRef)));
            StringEntity se = new StringEntity(body.toString(), AlfrescoHttpClient.UTF_8_ENCODING);
            se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, MIME_TYPE_JSON));
            request.setEntity(se);

            HttpResponse response = client.execute(user, password, request);
            switch (response.getStatusLine().getStatusCode())
            {
                case HttpStatus.SC_OK:
                    JSONObject json = new JSONObject(EntityUtils.toString(response.getEntity()));
                    success = (Boolean) json.get("overallSuccess");
                    break;
                case HttpStatus.SC_NOT_FOUND:
                    LOGGER.info("The provided paths couldn't be found " + response.toString());
                    break;
                default:
                    LOGGER.error("Unable to move: " + response.toString());
                    break;
            }
        }
        catch (JSONException | IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (request != null)
            {
                request.releaseConnection();
            }
            client.close();
        }

        assertTrue("Moving " + contentPath + " to " + destinationPath + " failed.", success);
    }

    /**
     * Move action
     *
     * @param user            the user to move the contentPath
     * @param password        the user's password
     * @param contentPath     path to the content to be moved
     * @param destinationPath destination path
     * @throws AssertionError if the move was unexpectedly successful.
     */
    public void moveToAndExpectFailure(String user, String password, String contentPath, String destinationPath)
    {
        try
        {
            moveTo(user, password, contentPath, destinationPath);
        }
        catch(AssertionError e)
        {
            // We are expecting the move to fail.
            return;
        }
        fail("Moving " + contentPath + " to " + destinationPath + " succeeded unexpectedly.");
    }

    /**
     * Perform an action on the record folder
     *
     * @param user        the user executing the action
     * @param password    the user's password
     * @param contentName the content name
     * @return The HTTP response.
     */
    public HttpResponse executeAction(String user, String password, String contentName, RM_ACTIONS rm_action)
    {
        return executeAction(user, password, contentName, rm_action, null);
    }

    /**
     * Perform an action on the record folder
     *
     * @param user        the user closing the folder
     * @param password    the user's password
     * @param contentName the record folder name
     * @param date        the date to be updated
     * @return The HTTP response.
     */
    public HttpResponse executeAction(String user, String password, String contentName, RM_ACTIONS action, ZonedDateTime date)
    {
        String recNodeRef = getNodeRefSpacesStore() + contentService.getNodeRef(user, password, RM_SITE_ID, contentName);
        JSONObject requestParams = new JSONObject();
        requestParams.put("name", action.getAction());
        requestParams.put("nodeRef", recNodeRef);
        if (date != null)
        {
            String thisMoment = date.format(DateTimeFormatter.ISO_INSTANT);
            requestParams.put("params", new JSONObject()
                            .put("asOfDate", new JSONObject()
                                    .put("iso8601", thisMoment)
                                )
                             );
        }
        return doPostJsonRequest(user, password, SC_OK, requestParams, RM_ACTIONS_API);
    }

    /**
     * Deletes every item in the given container
     *
     * @param username      the user's username
     * @param password      its password
     * @param siteId        the site id in which the container is located
     * @param containerName the container to look for items into
     * @throws AssertionError if not all items could be deleted.
     */
    public void deleteAllItemsInContainer(String username, String password, String siteId, String containerName)
    {
        for (CmisObject item : contentService.getFolderObject(contentService.getCMISSession(username, password), siteId, containerName).getChildren())
        {
            item.delete();
        }
        boolean success = !(contentService.getFolderObject(contentService.getCMISSession(username, password), siteId, containerName).getChildren().getHasMoreItems());
        assertTrue("Not all items were deleted from " + containerName, success);
    }

    /**
     * Deletes hold
     *
     * @param username user's username
     * @param password its password
     * @param holdName the hold name
     * @throws AssertionError if the deletion was unsuccessful.
     */
    public void deleteHold(String username, String password, String holdName)
    {
        deleteItem(username, password, "/Holds/" + holdName);
    }


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
    public HttpResponse createHold(String user, String password, String holdName, String reason, String description)
    {
        // if the hold already exists don't try to create it again
        String holdsContainerPath = getFilePlanPath() + "/Holds";
        String fullHoldPath = holdsContainerPath + "/" + holdName;
        CmisObject hold = getObjectByPath(user, password, fullHoldPath);
        if (hold != null)
        {
            return null;
        }
        // retrieve the Holds container nodeRef
        String parentNodeRef = getItemNodeRef(user, password, "/Holds");

        JSONObject requestParams = new JSONObject();
        requestParams.put("alf_destination", getNodeRefSpacesStore() + parentNodeRef);
        requestParams.put("prop_cm_name", holdName);
        requestParams.put("prop_cm_description", description);
        requestParams.put("prop_rma_holdReason", reason);

        // Make the POST request and throw an assertion error if it fails.
        HttpResponse httpResponse = doPostJsonRequest(user, password, SC_OK, requestParams, CREATE_HOLDS_API);
        assertNotNull("Expected object to have been created at " + fullHoldPath,
                    getObjectByPath(user, password, fullHoldPath));
        return httpResponse;
    }

    /**
     * Updates metadata, can be used on records, folders and categories
     *
     * @param username    the user updating the item
     * @param password    the user's password
     * @param itemNodeRef the item noderef
     * @return The HTTP response.
     */
    public HttpResponse updateMetadata(String username, String password, String itemNodeRef, Map<RMProperty, String> properties)
    {
        JSONObject requestParams = new JSONObject();
        addPropertyToRequest(requestParams, "prop_cm_name", properties, RMProperty.NAME);
        addPropertyToRequest(requestParams, "prop_cm_title", properties, RMProperty.TITLE);
        addPropertyToRequest(requestParams, "prop_cm_description", properties, RMProperty.DESCRIPTION);
        addPropertyToRequest(requestParams, "prop_cm_author", properties, RMProperty.AUTHOR);
        addPropertyToRequest(requestParams, "prop_dod_originator", properties, RMProperty.ORIGINATOR);
        addPropertyToRequest(requestParams, "prop_dod_originatingOrganization", properties, RMProperty
                .ORIGINATING_ORGANIZATION);
        addPropertyToRequest(requestParams, "prop_dod_publicationDate", properties, RMProperty.PUBLICATION_DATE);

        return doPostJsonRequest(username, password, SC_OK, requestParams, MessageFormat.format(UPDATE_METADATA_API, "{0}", itemNodeRef));
    }
}
