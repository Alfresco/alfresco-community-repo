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

import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.alfresco.module.org_alfresco_module_rm.capability.Capability;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMWebScriptTestCase;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.util.GUID;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.TestWebScriptServer.DeleteRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.GetRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.PostRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.PutRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.Response;

/**
 * This class tests the Rest API for disposition related operations
 *
 * @author Roy Wetherall
 */
public class RoleRestApiTest extends BaseRMWebScriptTestCase
                             implements RecordsManagementModel
{
    protected static final String GET_ROLES_URL_BY_SITE = "/api/rma/admin/{0}/rmroles";
    protected static final String GET_ROLES_URL_BY_FILEPLAN = "/api/rma/admin/{0}/{1}/{2}/rmroles";
    protected static final String SERVICE_URL_PREFIX = "/alfresco/service";
    protected static final String APPLICATION_JSON = "application/json";

    private String getRolesUrlBySite()
    {
        return MessageFormat.format(GET_ROLES_URL_BY_SITE, siteId);
    }

    private String getRoleUrlByFilePlan()
    {
        return MessageFormat.format(GET_ROLES_URL_BY_FILEPLAN, filePlan.getStoreRef().getProtocol(), filePlan.getStoreRef().getIdentifier(), filePlan.getId());
    }

    public void testGetRoles() throws Exception
    {
        String role1 = GUID.generate();
        String role2 = GUID.generate();
        String role3 = GUID.generate();

        // Create a couple or roles by hand
        filePlanRoleService.createRole(filePlan, role1, "My Test Role", getListOfCapabilities(5));
        filePlanRoleService.createRole(filePlan, role2, "My Test Role Too", getListOfCapabilities(5));
        
        //The user can either enter a plain text label or a key to look up in a property file.
        filePlanRoleService.createRole(filePlan, role3, "System Administrator", getListOfCapabilities(5));

        // create test group
        String groupName = GUID.generate();
        String group = authorityService.createAuthority(AuthorityType.GROUP, groupName, "monkey", null);

        // Add the admin user to one of the roles
        filePlanRoleService.assignRoleToAuthority(filePlan, role1, "admin");
        filePlanRoleService.assignRoleToAuthority(filePlan, role1, group);

        try
        {
            // Get the roles (for the default file plan)
            Response rsp = sendRequest(new GetRequest(getRolesUrlBySite()),200);
            String rspContent = rsp.getContentAsString();

            JSONObject obj = new JSONObject(rspContent);
            JSONObject roles = obj.getJSONObject("data");
            assertNotNull(roles);

            JSONObject roleObj = roles.getJSONObject(role1);
            assertNotNull(roleObj);
            assertEquals(role1, roleObj.get("name"));
            assertEquals("My Test Role", roleObj.get("displayLabel"));
            checkCapabilities(roleObj, 5);

            roleObj = roles.getJSONObject(role2);
            assertNotNull(roleObj);
            assertEquals(role2, roleObj.get("name"));
            assertEquals("My Test Role Too", roleObj.get("displayLabel"));
            checkCapabilities(roleObj, 5);
            
            //Custom role with a user entered message key
            roleObj = roles.getJSONObject(role3);
            assertNotNull(roleObj);
            assertEquals(role3, roleObj.get("name"));
            assertEquals("System Administrator", roleObj.get("displayLabel"));

            // Get the roles, specifying the file plan
            rsp = sendRequest(new GetRequest(getRoleUrlByFilePlan()),200);
            rspContent = rsp.getContentAsString();

            obj = new JSONObject(rspContent);
            roles = obj.getJSONObject("data");
            assertNotNull(roles);

            roleObj = roles.getJSONObject(role1);
            assertNotNull(roleObj);
            assertEquals(role1, roleObj.get("name"));
            assertEquals("My Test Role", roleObj.get("displayLabel"));
            checkCapabilities(roleObj, 5);

            roleObj = roles.getJSONObject(role2);
            assertNotNull(roleObj);
            assertEquals(role2, roleObj.get("name"));
            assertEquals("My Test Role Too", roleObj.get("displayLabel"));
            checkCapabilities(roleObj, 5);

            // Get the roles for "admin"
            rsp = sendRequest(new GetRequest(getRolesUrlBySite() + "?user=admin"),200);
            rspContent = rsp.getContentAsString();

            obj = new JSONObject(rspContent);
            roles = obj.getJSONObject("data");
            assertNotNull(roles);

            roleObj = roles.getJSONObject(role1);
            assertNotNull(roleObj);
            assertEquals(role1, roleObj.get("name"));
            assertEquals("My Test Role", roleObj.get("displayLabel"));
            checkCapabilities(roleObj, 5);

            assertFalse(roles.has(role2));

            // Get the roles including assigned authorities
            rsp = sendRequest(new GetRequest(getRoleUrlByFilePlan() + "?auths=true"),200);
            rspContent = rsp.getContentAsString();

            System.out.println(rspContent);

            obj = new JSONObject(rspContent);
            roles = obj.getJSONObject("data");
            assertNotNull(roles);

            roleObj = roles.getJSONObject(role1);
            assertNotNull(roleObj);
            assertEquals(role1, roleObj.get("name"));
            assertEquals("My Test Role", roleObj.get("displayLabel"));
            checkCapabilities(roleObj, 5);

            JSONArray users = roleObj.getJSONArray("assignedUsers");
            assertNotNull(users);
            assertEquals(1, users.length());

            JSONArray groups = roleObj.getJSONArray("assignedGroups");
            assertNotNull(groups);
            assertEquals(1, groups.length());

            roleObj = roles.getJSONObject(role2);
            assertNotNull(roleObj);
            assertEquals(role2, roleObj.get("name"));
            assertEquals("My Test Role Too", roleObj.get("displayLabel"));
            checkCapabilities(roleObj, 5);

            users = roleObj.getJSONArray("assignedUsers");
            assertNotNull(users);
            assertEquals(0, users.length());

            groups = roleObj.getJSONArray("assignedGroups");
            assertNotNull(groups);
            assertEquals(0, groups.length());
        }
        finally
        {
            // Clean up
            filePlanRoleService.deleteRole(filePlan, role1);
            filePlanRoleService.deleteRole(filePlan, role2);
        }

    }

    @SuppressWarnings("unchecked")
    private void checkCapabilities(JSONObject role, int expectedCount) throws JSONException
    {
        JSONObject capabilities = role.getJSONObject("capabilities");
        assertNotNull(capabilities);

        int count = 0;
        Iterator<String> it = capabilities.keys();
        while (it.hasNext())
        {
            String key = it.next();
            assertNotNull(key);
            assertNotNull(capabilities.getString(key));
            count ++;
        }

        assertEquals(expectedCount, count);
    }

    public void testPostRoles() throws Exception
    {
        Set<Capability> caps = getListOfCapabilities(5);
        JSONArray arrCaps = new JSONArray();
        for (Capability cap : caps)
        {
            arrCaps.put(cap.getName());
        }

        String roleName = GUID.generate();

        JSONObject obj = new JSONObject();
        obj.put("name", roleName);
        obj.put("displayLabel", "Display Label");
        obj.put("capabilities", arrCaps);

        Response rsp = sendRequest(new PostRequest(getRolesUrlBySite(), obj.toString(), APPLICATION_JSON),200);
        try
        {
            String rspContent = rsp.getContentAsString();

            JSONObject resultObj = new JSONObject(rspContent);
            JSONObject roleObj = resultObj.getJSONObject("data");
            assertNotNull(roleObj);

            assertNotNull(roleObj);
            assertEquals(roleName, roleObj.get("name"));
            assertEquals("Display Label", roleObj.get("displayLabel"));
            checkCapabilities(roleObj, 5);
        }
        finally
        {
            filePlanRoleService.deleteRole(filePlan, roleName);
        }

    }

    public void testPutRole() throws Exception
    {
        String role1 = GUID.generate();
        filePlanRoleService.createRole(filePlan, role1, "My Test Role", getListOfCapabilities(5));

        try
        {
            Set<Capability> caps = getListOfCapabilities(4,8);
            JSONArray arrCaps = new JSONArray();
            for (Capability cap : caps)
            {
                System.out.println(cap.getName());
                arrCaps.put(cap.getName());
            }

            JSONObject obj = new JSONObject();
            obj.put("name", role1);
            obj.put("displayLabel", "Changed");
            obj.put("capabilities", arrCaps);

            // Get the roles
            Response rsp = sendRequest(new PutRequest(getRolesUrlBySite() + "/" + role1, obj.toString(), APPLICATION_JSON),200);
            String rspContent = rsp.getContentAsString();

            JSONObject result = new JSONObject(rspContent);
            JSONObject roleObj = result.getJSONObject("data");
            assertNotNull(roleObj);

            assertNotNull(roleObj);
            assertEquals(role1, roleObj.get("name"));
            assertEquals("Changed", roleObj.get("displayLabel"));
            checkCapabilities(roleObj, 4);

            // Bad requests
            sendRequest(new PutRequest(getRolesUrlBySite() + "/cheese", obj.toString(), APPLICATION_JSON), 404);
        }
        finally
        {
            // Clean up
            filePlanRoleService.deleteRole(filePlan, role1);
        }

    }

    public void testGetRole() throws Exception
    {
        String role1 = GUID.generate();
        filePlanRoleService.createRole(filePlan, role1, "My Test Role", getListOfCapabilities(5));

        try
        {
            // Get the roles
            Response rsp = sendRequest(new GetRequest(getRolesUrlBySite() + "/" + role1),200);
            String rspContent = rsp.getContentAsString();

            JSONObject obj = new JSONObject(rspContent);
            JSONObject roleObj = obj.getJSONObject("data");
            assertNotNull(roleObj);

            assertNotNull(roleObj);
            assertEquals(role1, roleObj.get("name"));
            assertEquals("My Test Role", roleObj.get("displayLabel"));
            checkCapabilities(roleObj, 5);

            // Bad requests
            sendRequest(new GetRequest(getRolesUrlBySite() + "/cheese"), 404);
        }
        finally
        {
            // Clean up
            filePlanRoleService.deleteRole(filePlan, role1);
        }

    }

    public void testDeleteRole() throws Exception
    {
        String role1 = GUID.generate();
        assertFalse(filePlanRoleService.existsRole(filePlan, role1));
        filePlanRoleService.createRole(filePlan, role1, "My Test Role", getListOfCapabilities(5));
        assertTrue(filePlanRoleService.existsRole(filePlan, role1));
        sendRequest(new DeleteRequest(getRolesUrlBySite() + "/" + role1),200);
        assertFalse(filePlanRoleService.existsRole(filePlan, role1));

        // Bad request
        sendRequest(new DeleteRequest(getRolesUrlBySite() + "/cheese"), 404);
    }

    private Set<Capability> getListOfCapabilities(int size)
    {
        return getListOfCapabilities(size, 0);
    }

    private Set<Capability> getListOfCapabilities(int size, int offset)
    {
        Set<Capability> result = new HashSet<>(size);
        Set<Capability> caps = capabilityService.getCapabilities(false);
        int count = 0;
        for (Capability cap : caps)
        {
            if (count < size+offset)
            {
                if (count >= offset)
                {
                    result.add(cap);
                }
            }
            else
            {
                break;
            }
            count ++;
        }
        return result;
    }

}
