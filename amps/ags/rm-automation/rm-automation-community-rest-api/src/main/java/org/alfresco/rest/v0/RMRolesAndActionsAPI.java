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
package org.alfresco.rest.v0;

import static org.alfresco.dataprep.AlfrescoHttpClient.MIME_TYPE_JSON;
import static org.alfresco.rest.core.v0.APIUtils.ISO_INSTANT_FORMATTER;
import static org.apache.http.HttpStatus.SC_OK;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;

import java.io.IOException;
import java.text.MessageFormat;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

import org.alfresco.dataprep.AlfrescoHttpClient;
import org.alfresco.dataprep.AlfrescoHttpClientFactory;
import org.alfresco.dataprep.UserService;
import org.alfresco.rest.core.v0.BaseAPI;
import org.alfresco.rest.core.v0.RMEvents;
import org.alfresco.utility.data.DataUserAIS;
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

    /** http client factory */
    @Autowired
    private AlfrescoHttpClientFactory alfrescoHttpClientFactory;

    /** user service */
    @Autowired
    private UserService userService;

    @Autowired
    private DataUserAIS dataUser;
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
        final JSONObject jsonObject = doGetRequest(adminUser, adminPassword, RM_ROLES + "?is=true").getJSONObject(
                "data");
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
        final JSONObject jsonObject = doGetRequest(adminUser, adminPassword, RM_ROLES + "?is=true").getJSONObject(
                "data");
        assertTrue("Could not find role '" + role + "' in " + jsonObject.keySet(), jsonObject.has(role));
        return jsonObject.getJSONObject(role).getJSONObject("capabilities").keySet();
    }

    /**
     * Creates the body for PUT/POST Roles API requests
     *
     * @param roleName         the role name
     * @param roleDisplayLabel a human-readable label for the role
     * @param capabilities     a list of capabilities for the role
     * @return
     */
    private JSONObject roleRequestBody(String roleName, String roleDisplayLabel, Set<String> capabilities)
    {
        final JSONObject requestBody = new JSONObject();
        requestBody.put("name", roleName);
        requestBody.put("displayLabel", roleDisplayLabel);
        final JSONArray capabilitiesArray = new JSONArray();
        capabilities.forEach(capabilitiesArray::put);
        requestBody.put("capabilities", capabilitiesArray);
        return requestBody;
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
        doPostJsonRequest(adminUser, adminPassword, HttpStatus.SC_OK, roleRequestBody(roleName, roleDisplayLabel, capabilities),
                RM_ROLES);
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
        doPutJsonRequest(adminUser, adminPassword, HttpStatus.SC_OK, roleRequestBody(roleName, roleDisplayLabel, capabilities),
                RM_ROLES_ROLE, roleName);
    }

    /**
     * Delete a created RM role.
     *
     * @param adminUser The username of the admin user.
     * @param adminPassword The password for the admin user.
     * @param roleName The name of the role to be deleted.
     */
    public void deleteRole(String adminUser, String adminPassword, String roleName)
    {
        doDeleteRequest(adminUser, adminPassword, MessageFormat.format(RM_ROLES_ROLE, "{0}", roleName));
        assertFalse("Failed to delete role " + roleName + " with " + adminUser,
                getConfiguredRoles(adminUser, adminPassword).contains(roleName));
    }

    /**
     * create user and assign to records management role
     */
    public void createUserAndAssignToRole(
            String adminUser,
            String adminPassword,
            String userName,
            String password,
            String role)
    {
        if (!userService.userExists(adminUser, adminPassword, userName))
        {
            dataUser.createUser(userName, password);

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
        final AlfrescoHttpClient client = alfrescoHttpClientFactory.getObject();
        final String reqURL = MessageFormat.format(
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
            LOGGER.error(e.toString());
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
     * Perform an action on the given content
     *
     * @param user        the user executing the action
     * @param password    the user's password
     * @param contentName the content name
     * @return The HTTP response.
     */
    public HttpResponse executeAction(String user, String password, String contentName, RM_ACTIONS action)
    {
        return executeAction(user, password, contentName, action, null, SC_OK);
    }
    /**
     * Perform an action on the given content
     *
     * @param user        the user executing the action
     * @param password    the user's password
     * @param contentName the content name
     * @return The HTTP response.
     */
    public HttpResponse executeActionAndExpectResponseCode(String user, String password, String contentName, RM_ACTIONS action,
                                                           int status)
    {
        return executeAction(user, password, contentName, action, null, status);
    }

    /**
     * Perform an action on the given content
     *
     * @param user        the user executing the action
     * @param password    the user's password
     * @param contentName the content name
     * @return The HTTP response.
     */
    public HttpResponse executeAction(String user, String password, String contentName, RM_ACTIONS action,
                                      ZonedDateTime date)
    {
        return executeAction(user, password, contentName, action, date, SC_OK);
    }

    /**
     * Creates the body for Actions API requests
     *
     * @param user          the user executing the action
     * @param password      the user's password
     * @param contentName   the content on which the action is executed
     * @param action        the action executed
     * @param actionsParams the request parameters
     * @return the JSONObject created
     */
    private JSONObject actionsRequestBody(String user, String password, String contentName, RM_ACTIONS action,
                                          JSONObject actionsParams)
    {
        final String recNodeRef = getNodeRefSpacesStore() + contentService.getNodeRef(user, password, RM_SITE_ID, contentName);
        final JSONObject requestParams = new JSONObject();
        requestParams.put("name", action.getAction());
        requestParams.put("nodeRef", recNodeRef);
        if (actionsParams != null)
        {
            requestParams.put("params", actionsParams);
        }
        return requestParams;
    }

    /**
     * Perform an action on the record folder
     *
     * @param user        the user executing the action
     * @param password    the user's password
     * @param contentName the content name
     * @param date        the date to be updated
     * @return The HTTP response.
     */
    public HttpResponse executeAction(String user, String password, String contentName, RM_ACTIONS action,
                                      ZonedDateTime date, int status)
    {
        final JSONObject actionParams = new JSONObject();
        if (date != null)
        {
            actionParams.put("asOfDate", new JSONObject().put("iso8601", ISO_INSTANT_FORMATTER.format(date)));
        }
        final JSONObject requestParams = actionsRequestBody(user, password, contentName, action, actionParams);
        return doPostJsonRequest(user, password, status, requestParams, RM_ACTIONS_API);
    }

    /**
     * Complete an event on the record/record folder
     *
     * @param user        the user executing the action
     * @param password    the user's password
     * @param nodeName    the node name
     * @param event       the event to be completed
     * @param date        the date to be updated
     * @return The HTTP response.
     */
    public HttpResponse completeEvent(String user, String password, String nodeName, RMEvents event, Instant date)
    {
        date = (date != null) ? date : Instant.now();
        final JSONObject actionParams = new JSONObject().put("eventName", event.getEventName())
                                                        .put("eventCompletedBy", user)
                                                        .put("eventCompletedAt", new JSONObject()
                                                                .put("iso8601", ISO_INSTANT_FORMATTER.format(date))
                                                            );
        final JSONObject requestParams = actionsRequestBody(user, password, nodeName, RM_ACTIONS.COMPLETE_EVENT,
                actionParams);
        return doPostJsonRequest(user, password, SC_OK, requestParams, RM_ACTIONS_API);
    }

    /**
     * Undo an event on the record/record folder
     *
     * @param user        the user executing the action
     * @param password    the user's password
     * @param contentName the content name
     * @param event       the event to be undone
     * @return The HTTP response.
     */
    public HttpResponse undoEvent(String user, String password, String contentName, RMEvents event)
    {
        final JSONObject requestParams = actionsRequestBody(user, password, contentName, RM_ACTIONS.UNDO_EVENT,
                new JSONObject().put("eventName", event.getEventName()));
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
        assertFalse("Not all items were deleted from " + containerName,
                contentService.getFolderObject(contentService.getCMISSession(username, password), siteId, containerName).getChildren().getHasMoreItems());
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
