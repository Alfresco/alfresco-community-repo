/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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

import java.util.HashSet;
import java.util.Set;

import org.alfresco.module.org_alfresco_module_rm.capability.Capability;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMWebScriptTestCase;
import org.alfresco.util.GUID;
import org.json.JSONArray;
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
public class RoleRestApiTest extends BaseRMWebScriptTestCase implements RecordsManagementModel
{
    protected static final String GET_ROLES_URL = "/api/rma/admin/rmroles";
    protected static final String SERVICE_URL_PREFIX = "/alfresco/service";
    protected static final String APPLICATION_JSON = "application/json";    

    public void testGetRoles() throws Exception
    {
        String role1 = GUID.generate();
        String role2 = GUID.generate();
        
        // Create a couple or roles by hand
        securityService.createRole(filePlan, role1, "My Test Role", getListOfCapabilities(5));
        securityService.createRole(filePlan, role2, "My Test Role Too", getListOfCapabilities(5));
        
        // Add the admin user to one of the roles
        securityService.assignRoleToAuthority(filePlan, role1, "admin");
        
        try
        {
            // Get the roles
            Response rsp = sendRequest(new GetRequest(GET_ROLES_URL),200);
            String rspContent = rsp.getContentAsString();
            
            JSONObject obj = new JSONObject(rspContent);
            JSONObject roles = obj.getJSONObject("data");
            assertNotNull(roles);
            
            JSONObject roleObj = roles.getJSONObject(role1);
            assertNotNull(roleObj);
            assertEquals(role1, roleObj.get("name"));
            assertEquals("My Test Role", roleObj.get("displayLabel"));
            JSONArray caps = roleObj.getJSONArray("capabilities");
            assertNotNull(caps);
            assertEquals(5, caps.length());
            
            roleObj = roles.getJSONObject(role2);
            assertNotNull(roleObj);
            assertEquals(role2, roleObj.get("name"));
            assertEquals("My Test Role Too", roleObj.get("displayLabel"));
            caps = roleObj.getJSONArray("capabilities");
            assertNotNull(caps);
            assertEquals(5, caps.length());   
            
            // Get the roles for "admin"
            rsp = sendRequest(new GetRequest(GET_ROLES_URL + "?user=admin"),200);
            rspContent = rsp.getContentAsString();
            
            obj = new JSONObject(rspContent);
            roles = obj.getJSONObject("data");
            assertNotNull(roles);
            
            roleObj = roles.getJSONObject(role1);
            assertNotNull(roleObj);
            assertEquals(role1, roleObj.get("name"));
            assertEquals("My Test Role", roleObj.get("displayLabel"));
            caps = roleObj.getJSONArray("capabilities");
            assertNotNull(caps);
            assertEquals(5, caps.length());
            
            assertFalse(roles.has(role2));
        }
        finally
        {
            // Clean up 
            securityService.deleteRole(filePlan, role1);
            securityService.deleteRole(filePlan, role2);
        }
        
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
        
        Response rsp = sendRequest(new PostRequest(GET_ROLES_URL, obj.toString(), APPLICATION_JSON),200);
        try
        {
            String rspContent = rsp.getContentAsString();
            
            JSONObject resultObj = new JSONObject(rspContent);
            JSONObject roleObj = resultObj.getJSONObject("data");
            assertNotNull(roleObj);
            
            assertNotNull(roleObj);
            assertEquals(roleName, roleObj.get("name"));
            assertEquals("Display Label", roleObj.get("displayLabel"));
            JSONArray resultCaps = roleObj.getJSONArray("capabilities");
            assertNotNull(resultCaps);
            assertEquals(5, resultCaps.length());
        }
        finally
        {
            securityService.deleteRole(filePlan, roleName);
        }
        
    }
    
    public void testPutRole() throws Exception
    {
        String role1 = GUID.generate();        
        securityService.createRole(filePlan, role1, "My Test Role", getListOfCapabilities(5));
        
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
            Response rsp = sendRequest(new PutRequest(GET_ROLES_URL + "/" + role1, obj.toString(), APPLICATION_JSON),200);
            String rspContent = rsp.getContentAsString();
            
            JSONObject result = new JSONObject(rspContent);
            JSONObject roleObj = result.getJSONObject("data");
            assertNotNull(roleObj);
            
            assertNotNull(roleObj);
            assertEquals(role1, roleObj.get("name"));
            assertEquals("Changed", roleObj.get("displayLabel"));
            JSONArray bob = roleObj.getJSONArray("capabilities");
            assertNotNull(bob);
            assertEquals(4, bob.length());      
            
            // Bad requests
            sendRequest(new PutRequest(GET_ROLES_URL + "/cheese", obj.toString(), APPLICATION_JSON), 404);   
        }
        finally
        {
            // Clean up 
            securityService.deleteRole(filePlan, role1);
        }
        
    }
    
    public void testGetRole() throws Exception
    {
        String role1 = GUID.generate();        
        securityService.createRole(filePlan, role1, "My Test Role", getListOfCapabilities(5));
        
        try
        {
            // Get the roles
            Response rsp = sendRequest(new GetRequest(GET_ROLES_URL + "/" + role1),200);
            String rspContent = rsp.getContentAsString();
            
            JSONObject obj = new JSONObject(rspContent);
            JSONObject roleObj = obj.getJSONObject("data");
            assertNotNull(roleObj);
            
            assertNotNull(roleObj);
            assertEquals(role1, roleObj.get("name"));
            assertEquals("My Test Role", roleObj.get("displayLabel"));
            JSONArray caps = roleObj.getJSONArray("capabilities");
            assertNotNull(caps);
            assertEquals(5, caps.length());       
            
            // Bad requests
            sendRequest(new GetRequest(GET_ROLES_URL + "/cheese"), 404);
        }
        finally
        {
            // Clean up 
            securityService.deleteRole(filePlan, role1);
        }
        
    }
    
    public void testDeleteRole() throws Exception
    {
        String role1 = GUID.generate();
        assertFalse(securityService.existsRole(filePlan, role1));        
        securityService.createRole(filePlan, role1, "My Test Role", getListOfCapabilities(5));        
        assertTrue(securityService.existsRole(filePlan, role1));        
        sendRequest(new DeleteRequest(GET_ROLES_URL + "/" + role1),200);        
        assertFalse(securityService.existsRole(filePlan, role1));     
        
        // Bad request
        sendRequest(new DeleteRequest(GET_ROLES_URL + "/cheese"), 404);  
    }
    
    private Set<Capability> getListOfCapabilities(int size)
    {
        return getListOfCapabilities(size, 0);
    }
    
    private Set<Capability> getListOfCapabilities(int size, int offset)
    {
        Set<Capability> result = new HashSet<Capability>(size);
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
