/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */
package org.alfresco.module.org_alfresco_module_rm.test.webscript;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Set;

import org.alfresco.module.org_alfresco_module_rm.role.FilePlanRoleService;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMWebScriptTestCase;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.util.GUID;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.TestWebScriptServer.DeleteRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.PostRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.Response;

/**
 * REST API Tests for adding/removing users/groups to/from a role
 *
 * @author Tuna Aksoy
 * @since 2.1
 */
public class RmAuthoritiesRestApiTest extends BaseRMWebScriptTestCase
{
    /** URL for the REST APIs */
    private static final String RM_CHILDREN_URL = "/api/rm/%s/roles/%s/authorities/%s";

    /** Constant for the content type */
    private static final String APPLICATION_JSON = "application/json";

    /**
     * Test the REST API to add/remove a user to/from a role
     *
     * @throws IOException
     * @throws JSONException
     */
    public void testRmAddRemoveUser() throws IOException, JSONException
    {
        // Create a test user
        String userName = GUID.generate();
        createUser(userName);

        // Get the name
        String name = authorityService.getName(AuthorityType.USER, userName);

        // Check if the user is already assigned to the role
        assertFalse(getUsersAssignedToRole().contains(name));

        // Format url and send request
        String url = getFormattedUrlString(name);
        Response response = postRequestSuccess(url);

        // Check the content from the response
        checkContent(response);

        // The user should be added to the role
        assertTrue(getUsersAssignedToRole().contains(name));

        // Remove the user from the role
        response = deleteRequestSuccess(url);

        // Check the content from the response
        checkContent(response);

        // The user should be removed from the role
        assertFalse(getUsersAssignedToRole().contains(name));

        // Delete the user
        deleteUser(name);
    }

    /**
     * Test the REST API to add/remove a group to/from a role
     *
     * @throws IOException
     * @throws JSONException
     */
    public void testRmAddRemoveGroup() throws IOException, JSONException
    {
        // Create a group
        String groupName = GUID.generate();
        createGroup(groupName);

        // Get the name
        String name = authorityService.getName(AuthorityType.GROUP, groupName);

        // Check if the group is already assigned to the role
        assertFalse(getGroupsAssignedToRole().contains(name));

        // Format url and send request
        String url = getFormattedUrlString(name);
        Response response = postRequestSuccess(url);

        // Check the content from the response
        checkContent(response);

        // The group should be added to the role
        assertTrue(getGroupsAssignedToRole().contains(name));

        // Remove the group from the role
        response = deleteRequestSuccess(url);

        // Check the content from the response
        checkContent(response);

        // The user should be removed from the role
        assertFalse(getGroupsAssignedToRole().contains(name));

        // Delete the group
        deleteGroup(name);
    }

    /**
     * Util method to get a set of groups assigned to a role
     *
     * @return Returns a set of groups assigned to a role
     */
    private Set<String> getGroupsAssignedToRole()
    {
        return filePlanRoleService.getGroupsAssignedToRole(filePlan, FilePlanRoleService.ROLE_SECURITY_OFFICER);
    }

    /**
     * Util method to get a set of users assigned to a role
     *
     * @return Returns a set of users assigned to a role
     */
    private Set<String> getUsersAssignedToRole()
    {
        return filePlanRoleService.getUsersAssignedToRole(filePlan, FilePlanRoleService.ROLE_SECURITY_OFFICER);
    }

    /**
     * Util method to get a formatted nodeRef string
     *
     * @return Returns a formatted nodeRef string
     */
    private String getFormattedFilePlanString()
    {
        StoreRef storeRef = filePlan.getStoreRef();
        String storeType = storeRef.getProtocol();
        String storeId = storeRef.getIdentifier();
        String id = filePlan.getId();

        StringBuffer sb = new StringBuffer(32);
        sb.append(storeType);
        sb.append("/");
        sb.append(storeId);
        sb.append("/");
        sb.append(id);

        return sb.toString();
    }

    /**
     * Util method to get a formatted url string
     *
     * @param authorityName The name of the authority which should be added/removed to/from a role
     * @return Returns a formatted url string
     */
    private String getFormattedUrlString(String authorityName)
    {
        return String.format(RM_CHILDREN_URL, getFormattedFilePlanString(), FilePlanRoleService.ROLE_SECURITY_OFFICER, authorityName);
    }

    /**
     * Util method to send a post request
     *
     * @param url The url which should be used to make the post request
     * @return Returns the response from the server
     * @throws UnsupportedEncodingException
     * @throws IOException
     */
    private Response postRequestSuccess(String url) throws UnsupportedEncodingException, IOException
    {
        return sendRequest(new PostRequest(url, new JSONObject().toString(), APPLICATION_JSON), Status.STATUS_OK);
    }

    /**
     * Util method to send a delete request
     *
     * @param url The url which should be used to make the delete request
     * @return Returns the response from the server
     * @throws IOException
     */
    private Response deleteRequestSuccess(String url) throws IOException
    {
        return sendRequest(new DeleteRequest(url), Status.STATUS_OK);
    }

    /**
     * Util method to check the server response
     *
     * @param response The server response
     * @throws UnsupportedEncodingException
     */
    private void checkContent(Response response) throws UnsupportedEncodingException
    {
        String contentAsString = response.getContentAsString();
        assertNotNull(contentAsString);
        assertTrue(contentAsString.equals("{}"));
    }
}
