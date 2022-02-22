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

package org.alfresco.module.org_alfresco_module_rm.test.legacy.webscript;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.Set;

import org.alfresco.module.org_alfresco_module_rm.capability.Capability;
import org.alfresco.module.org_alfresco_module_rm.capability.RMPermissionModel;
import org.alfresco.module.org_alfresco_module_rm.role.FilePlanRoleService;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMWebScriptTestCase;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
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

    /** Constant for users and groups */
    private static final String USER_WITH_CAPABILITY = GUID.generate();
    private static final String USER_WITHOUT_CAPABILITY = GUID.generate();
    private static final String ROLE_INCLUDING_CAPABILITY = GUID.generate();
    private static final String ROLE_NOT_INCLUDING_CAPABILITY = GUID.generate();
    private static final String USER_TO_ADD_TO_ROLE = GUID.generate();
    private static final String GROUP_TO_ADD_TO_ROLE = GUID.generate();

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMWebScriptTestCase#setupTestData()
     */
    @Override
    protected void setupTestData()
    {
        super.setupTestData();

        retryingTransactionHelper.doInTransaction(new RetryingTransactionCallback<Object>()
        {
            @Override
            public Object execute() throws Throwable
            {
                AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());

                // Create test user WITH required capability
                createUser(USER_WITH_CAPABILITY);
                // Create test role
                Set<Capability> capabilities = new HashSet<>(2);
                capabilities.add(capabilityService.getCapability(RMPermissionModel.VIEW_RECORDS));
                capabilities.add(capabilityService.getCapability(RMPermissionModel.MANAGE_ACCESS_CONTROLS));
                filePlanRoleService.createRole(filePlan, ROLE_INCLUDING_CAPABILITY, ROLE_INCLUDING_CAPABILITY, capabilities);
                // Add user to the role
                filePlanRoleService.assignRoleToAuthority(filePlan, ROLE_INCLUDING_CAPABILITY, USER_WITH_CAPABILITY);

                // Create test user WITHOUT required capability
                createUser(USER_WITHOUT_CAPABILITY);
                // Create test role
                filePlanRoleService.createRole(filePlan, ROLE_NOT_INCLUDING_CAPABILITY, ROLE_NOT_INCLUDING_CAPABILITY, new HashSet<>(1));
                // Add user to the role
                filePlanRoleService.assignRoleToAuthority(filePlan, ROLE_NOT_INCLUDING_CAPABILITY, USER_WITHOUT_CAPABILITY);

                // Create a test user to add to role
                createUser(USER_TO_ADD_TO_ROLE);

                // Create a group to add to role
                createGroup(GROUP_TO_ADD_TO_ROLE);

                return null;
            }
        });
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMWebScriptTestCase#tearDownImpl()
     */
    @Override
    protected void tearDownImpl()
    {
        super.tearDownImpl();

        // Delete test user WITH required capability
        deleteUser(USER_WITH_CAPABILITY);
        // Delete test role
        filePlanRoleService.deleteRole(filePlan, ROLE_INCLUDING_CAPABILITY);

        // Delete test user WITHOUT required capability
        deleteUser(USER_WITHOUT_CAPABILITY);
        // Add user to the role
        filePlanRoleService.deleteRole(filePlan, ROLE_NOT_INCLUDING_CAPABILITY);

        // Delete the user which was added to the role
        deleteUser(getTestUserName());

        // Delete the group which was added to the role
        deleteGroup(getTestGroupName());
    }

    /**
     * Test the REST API to add/remove a user to/from a role
     *
     * @throws IOException
     * @throws JSONException
     */
    public void testRmAddRemoveUser() throws IOException, JSONException
    {
        // Do the positive test with a user with the needed capabilities
        AuthenticationUtil.setFullyAuthenticatedUser(USER_WITH_CAPABILITY);

        // Get the user name
        String userName = getTestUserName();

        // Check if the user is already assigned to the role
        assertFalse(getUsersAssignedToRole().contains(userName));

        // Format url, send the request and check the content
        String url = getFormattedUrlString(userName);
        checkContent(postRequestSuccess(url));

        // The user should be added to the role
        assertTrue(getUsersAssignedToRole().contains(userName));

        // Remove the user from the role and check the content
        checkContent(deleteRequestSuccess(url));

        // The user should be removed from the role
        assertFalse(getUsersAssignedToRole().contains(userName));

        // Do the negative test with a user without any capabilities
        AuthenticationUtil.setFullyAuthenticatedUser(USER_WITHOUT_CAPABILITY);

        // Send a request. The expectation is an internal server error
        postRequestFailure(url);
    }

    /**
     * Test the REST API to add/remove a group to/from a role
     *
     * @throws IOException
     * @throws JSONException
     */
    public void testRmAddRemoveGroup() throws IOException, JSONException
    {
        // Do the positive test with a user with the needed capabilities
        AuthenticationUtil.setFullyAuthenticatedUser(USER_WITH_CAPABILITY);

        // Get the group name
        String groupName = getTestGroupName();

        // Check if the group is already assigned to the role
        assertFalse(getGroupsAssignedToRole().contains(groupName));

        // Format url, send the request and check the content
        String url = getFormattedUrlString(groupName);
        checkContent(postRequestSuccess(url));

        // The group should be added to the role
        assertTrue(getGroupsAssignedToRole().contains(groupName));

        // Remove the group from the role and check the content
        checkContent(deleteRequestSuccess(url));

        // The user should be removed from the role
        assertFalse(getGroupsAssignedToRole().contains(groupName));

        // Do the negative test with a user without any capabilities
        AuthenticationUtil.setFullyAuthenticatedUser(USER_WITHOUT_CAPABILITY);

        // Send a request. The expectation is an internal server error
        deleteRequestFailure(url);
    }

    /**
     * Util method to get the user name which will be added/removed to/from the role
     *
     * @return Returns the user name which will be added/removed to/from the role
     */
    private String getTestUserName()
    {
        return authorityService.getName(AuthorityType.USER, USER_TO_ADD_TO_ROLE);
    }

    /**
     * Util method to get the group name which will be added/removed to/from the role
     *
     * @return Returns the user group which will be added/removed to/from the role
     */
    private String getTestGroupName()
    {
        return authorityService.getName(AuthorityType.GROUP, GROUP_TO_ADD_TO_ROLE);
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
     * Util method to send a post request. The expected status is success.
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
     * Util method to send a post request. The expected status is an internal server error.
     *
     * @param url The url which should be used to make the post request
     * @return Returns the response from the server
     * @throws UnsupportedEncodingException
     * @throws IOException
     */
    private Response postRequestFailure(String url) throws UnsupportedEncodingException, IOException
    {
        return sendRequest(new PostRequest(url, new JSONObject().toString(), APPLICATION_JSON), Status.STATUS_INTERNAL_SERVER_ERROR);
    }

    /**
     * Util method to send a delete request. The expected status is success.
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
     * Util method to send a delete request. The expected status is an internal server error.
     *
     * @param url The url which should be used to make the delete request
     * @return Returns the response from the server
     * @throws IOException
     */
    private Response deleteRequestFailure(String url) throws IOException
    {
        return sendRequest(new DeleteRequest(url), Status.STATUS_INTERNAL_SERVER_ERROR);
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
        assertTrue(contentAsString.contains("{}"));
    }
}
