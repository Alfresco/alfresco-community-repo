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
package org.alfresco.rest.v0;

import static org.alfresco.dataprep.AlfrescoHttpClient.MIME_TYPE_JSON;

import java.io.IOException;
import java.text.MessageFormat;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Map;

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
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class RMRolesAndActionsAPI extends BaseAPI
{
    private static final String RM_ROLES_AUTHORITIES = "{0}rm/roles/{1}/authorities/{2}?alf_ticket={3}";

    // logger
    private static final Logger LOGGER = LoggerFactory.getLogger(RMRolesAndActionsAPI.class);
    private static final String MOVE_ACTIONS_API = "action/rm-move-to/site/rm/documentLibrary/{0}";
    private static final String CREATE_HOLDS_API = "{0}type/rma:hold/formprocessor";

    /** http client factory */
    @Autowired
    private AlfrescoHttpClientFactory alfrescoHttpClientFactory;

    private ApplicationContext applicationContext;

    /** user service */
    @Autowired
    private UserService userService;

    @Autowired
    private ContentService contentService;

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
        assignUserToRole(adminUser, adminPassword, userName, role);
    }

    /**
     * assign user to records management role
     */
    public boolean assignUserToRole(String adminUser, String adminPassword, String userName, String role)
    {
        AlfrescoHttpClient client = alfrescoHttpClientFactory.getObject();
        String reqURL = MessageFormat.format(
                RM_ROLES_AUTHORITIES,
                client.getApiUrl(),
                role,
                userName,
                client.getAlfTicket(adminUser, adminPassword));

        HttpPost request = null;
        HttpResponse response = null;
        try
        {
            request = new HttpPost(reqURL);
            response = client.execute(adminUser, adminPassword, request);
            switch (response.getStatusLine().getStatusCode())
            {
                case HttpStatus.SC_OK:
                    return true;
                case HttpStatus.SC_CONFLICT:
                    break;
                default:
                    break;
            }
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

    /**
     * Move action
     *
     * @param user            the user to move the contentPath
     * @param password        the user's password
     * @param contentPath     path to the content to be moved
     * @param destinationPath destination path
     * @return true if the action completed successfully
     */
    public boolean moveTo(String user, String password, String contentPath, String destinationPath)
    {
        String contentNodeRef = NODE_REF_WORKSPACE_SPACES_STORE + getItemNodeRef(user, password, contentPath);
        AlfrescoHttpClient client = alfrescoHttpClientFactory.getObject();
        String url = MessageFormat.format(client.getAlfrescoUrl() + "alfresco/s/slingshot/doclib/" + MOVE_ACTIONS_API, destinationPath);
        HttpPost request = new HttpPost(url);

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
                    return (Boolean) json.get("overallSuccess");
                case HttpStatus.SC_NOT_FOUND:
                    LOGGER.info("The provided paths couldn't be found " + response.toString());
                default:
                    LOGGER.error("Unable to move: " + response.toString());
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
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

        return false;
    }

    /**
     * Perform an action on the record folder
     *
     * @param user        the user executing the action
     * @param password    the user's password
     * @param contentName the content name
     * @return true if the action completed successfully
     */
    public boolean executeAction(String user, String password, String contentName, RM_ACTIONS rm_action)
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
     * @return true if the action completed successfully
     */
    public boolean executeAction(String user, String password, String contentName, RM_ACTIONS action, ZonedDateTime date)
    {
        String recNodeRef = NODE_REF_WORKSPACE_SPACES_STORE + contentService.getNodeRef(user, password, RM_SITE_ID, contentName);
        try
        {
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
            return doPostJsonRequest(user, password, requestParams, RM_ACTIONS_API);
        }
        catch (JSONException error)
        {
            LOGGER.error("Unable to extract response parameter", error);
        }
        return false;
    }

    /**
     * Deletes every item in the given container
     *
     * @param username      the user's username
     * @param password      its password
     * @param siteId        the site id in which the container is located
     * @param containerName the container to look for items into
     * @return true if the deletion has been successful
     */
    public boolean deleteAllItemsInContainer(String username, String password, String siteId, String containerName)
    {
        for (CmisObject item : contentService.getFolderObject(contentService.getCMISSession(username, password), siteId, containerName).getChildren())
        {
            item.delete();
        }
        return !(contentService.getFolderObject(contentService.getCMISSession(username, password), siteId, containerName).getChildren().getHasMoreItems());
    }

    /**
     * Deletes hold
     *
     * @param username user's username
     * @param password its password
     * @param holdName the hold name
     * @return true if the delete is successful
     */
    public boolean deleteHold(String username, String password, String holdName)
    {
        return deleteItem(username, password, "/Holds/" + holdName);
    }


    /**
     * Util method to create a hold
     *
     * @param user        the user creating the category
     * @param password    the user's password
     * @param holdName    the hold name
     * @param reason      hold reason
     * @param description hold description
     * @return true if the hold creation has been successful
     */
    public boolean createHold(String user, String password, String holdName, String reason, String description)
    {
        // if the hold already exists don't try to create it again
        String holdsContainerPath = FILE_PLAN_PATH + "/Holds";

        CmisObject hold = getObjectByPath(user, password, holdsContainerPath + "/" + holdName);
        if (hold != null)
        {
            return true;
        }
        // retrieve the Holds container nodeRef
        String parentNodeRef = getItemNodeRef(user, password, "/Holds");

        try
        {
            JSONObject requestParams = new JSONObject();
            requestParams.put("alf_destination", NODE_REF_WORKSPACE_SPACES_STORE + parentNodeRef);
            requestParams.put("prop_cm_name", holdName);
            requestParams.put("prop_cm_description", description);
            requestParams.put("prop_rma_holdReason", reason);

            boolean requestSucceeded = doPostJsonRequest(user, password, requestParams, CREATE_HOLDS_API);
            return requestSucceeded && getObjectByPath(user, password, holdsContainerPath + "/" + holdName) != null;
        }
        catch (JSONException error)
        {
            LOGGER.error("Unable to extract response parameter", error);
        }
        return false;
    }

    /**
     * Updates metadata, can be used on records, folders and categories
     *
     * @param username    the user updating the item
     * @param password    the user's password
     * @param itemNodeRef the item noderef
     * @return true if the update of the item properties has been successful
     */
    public boolean updateMetadata(String username, String password, String itemNodeRef, Map<RMProperty, String> properties)
    {
        try
        {
            JSONObject requestParams = new JSONObject();
            addPropertyToRequest(requestParams, "prop_cm_name", properties, RMProperty.NAME);
            addPropertyToRequest(requestParams, "prop_cm_title", properties, RMProperty.TITLE);
            addPropertyToRequest(requestParams, "prop_cm_description", properties, RMProperty.DESCRIPTION);
            addPropertyToRequest(requestParams, "prop_cm_author", properties, RMProperty.AUTHOR);

            return doPostJsonRequest(username, password, requestParams, MessageFormat.format(UPDATE_METADATA_API, "{0}", itemNodeRef));
        }
        catch (JSONException error)
        {
            LOGGER.error("Unable to extract response parameter", error);
        }
        return false;
    }
}
