/*
 * Copyright 2005 - 2020 Alfresco Software Limited.
 *
 * This file is part of the Alfresco software.
 * If the software was purchased under a paid Alfresco license, the terms of the paid license agreement will prevail.
 * Otherwise, the software is provided under the following open source license terms:
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
package org.alfresco.repo.web.scripts.permission;

import java.util.HashSet;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.web.scripts.BaseWebScriptTest;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.GUID;
import org.alfresco.util.PropertyMap;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.TestWebScriptServer.GetRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.PostRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.Response;

/**
 * Test for RestAPI permission services
 * 
 * @author alex.mukha
 * @since 4.2.3
 */
public class PermissionServiceTest extends BaseWebScriptTest
{
    private MutableAuthenticationService authenticationService;
    private AuthenticationComponent authenticationComponent;
    private PersonService personService;
    private NodeService nodeService;
    private PermissionService permissionService;
    private FileFolderService fileFolderService;
    private SiteService siteService;

    private static final String USER_ONE = "USER_ONE_" + GUID.generate();
    private static final String USER_TWO = "USER_TWO_" + GUID.generate();
    private static final String USER_THREE = "USER_THREE_" + GUID.generate();
    private static final String URL_DOCLIB_PERMISSIONS = "/slingshot/doclib/permissions";
    
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        
        this.authenticationService = (MutableAuthenticationService)getServer().getApplicationContext().getBean("AuthenticationService");
        this.authenticationComponent = (AuthenticationComponent)getServer().getApplicationContext().getBean("authenticationComponent");
        this.personService = (PersonService)getServer().getApplicationContext().getBean("PersonService");
        this.nodeService = (NodeService)getServer().getApplicationContext().getBean("NodeService");
        this.permissionService = (PermissionService)getServer().getApplicationContext().getBean("PermissionService");
        this.fileFolderService = (FileFolderService)getServer().getApplicationContext().getBean("FileFolderService");
        this.siteService = (SiteService)getServer().getApplicationContext().getBean("SiteService");
        
        this.authenticationComponent.setCurrentUser(AuthenticationUtil.getAdminUserName());
        // Create users
        createUser(USER_ONE);
    }
    
    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();
        this.authenticationComponent.setCurrentUser(AuthenticationUtil.getAdminUserName());
        
        // Clear the users
        deleteUser(USER_ONE);     
    }
    
    /**
     * Test for MNT-11725
     */
    public void testDowngradePermissions() throws Exception
    {
        NodeRef rootNodeRef = this.nodeService.getRootNode(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
        NodeRef folderRef = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName(NamespaceService.ALFRESCO_URI, "testFolder"),
                ContentModel.TYPE_FOLDER).getChildRef();
        permissionService.setPermission(folderRef, USER_ONE, PermissionService.COORDINATOR, true);
        permissionService.setInheritParentPermissions(folderRef, false);

        authenticationComponent.setCurrentUser(USER_ONE);
        
        // JSON fromat
        //  {"permissions":
        //  [{"authority":"userA",
        //  "role":"Consumer"},
        //  {"authority":"userA",
        //  "role":"Coordinator",
        //  "remove":true}],
        //  "isInherited":true}
        
        /*  negative test, we are first deleting the coordinator role and then try to add consumer */
        JSONObject changePermission = new JSONObject();
        JSONArray permissions = new JSONArray();
        // First delete permission, then add
        JSONObject addPermission = new JSONObject();
        addPermission.put("authority", USER_ONE);
        addPermission.put("role", PermissionService.CONSUMER);
        JSONObject removePermission = new JSONObject();
        removePermission.put("authority", USER_ONE);
        removePermission.put("role", PermissionService.COORDINATOR);
        removePermission.put("remove","true");
        permissions.put(removePermission);
        permissions.put(addPermission);
        changePermission.put("permissions", permissions);
        changePermission.put("isInherited", "true");
        
        sendRequest(new PostRequest(URL_DOCLIB_PERMISSIONS +
                "/" + StoreRef.STORE_REF_WORKSPACE_SPACESSTORE.getProtocol() +
                "/" + StoreRef.STORE_REF_WORKSPACE_SPACESSTORE.getIdentifier() +
                "/" + folderRef.getId(),  changePermission.toString(), "application/json"), Status.STATUS_INTERNAL_SERVER_ERROR);  
        
        /*  positive test  */
        changePermission = new JSONObject();
        permissions = new JSONArray();
        // First add permission, then delete
        addPermission = new JSONObject();
        addPermission.put("authority", USER_ONE);
        addPermission.put("role", PermissionService.CONSUMER);
        removePermission = new JSONObject();
        removePermission.put("authority", USER_ONE);
        removePermission.put("role", PermissionService.COORDINATOR);
        removePermission.put("remove","true");
        permissions.put(addPermission);
        permissions.put(removePermission);
        changePermission.put("permissions", permissions);
        changePermission.put("isInherited", "true");
        
        sendRequest(new PostRequest(URL_DOCLIB_PERMISSIONS +
                "/" + StoreRef.STORE_REF_WORKSPACE_SPACESSTORE.getProtocol() +
                "/" + StoreRef.STORE_REF_WORKSPACE_SPACESSTORE.getIdentifier() +
                "/" + folderRef.getId(),  changePermission.toString(), "application/json"), Status.STATUS_OK);   
        
        AccessStatus accessStatus = permissionService.hasPermission(folderRef, PermissionService.CONSUMER);
        assertTrue("The permission was not set correctly", accessStatus == AccessStatus.ALLOWED);

        this.authenticationComponent.setCurrentUser(AuthenticationUtil.getAdminUserName());
        nodeService.deleteNode(folderRef);
    }
    
    private void createUser(String userName)
    {
        if (this.authenticationService.authenticationExists(userName) == false)
        {
            this.authenticationService.createAuthentication(userName, "PWD".toCharArray());
            
            PropertyMap properties = new PropertyMap(4);
            properties.put(ContentModel.PROP_USERNAME, userName);
            properties.put(ContentModel.PROP_FIRSTNAME, "firstName");
            properties.put(ContentModel.PROP_LASTNAME, "lastName");
            properties.put(ContentModel.PROP_EMAIL, "email@email.com");
            properties.put(ContentModel.PROP_JOBTITLE, "jobTitle");
            
            this.personService.createPerson(properties);
        }
    }
    
    private void deleteUser(String username)
    {
       this.personService.deletePerson(username);
       if(this.authenticationService.authenticationExists(username))
       {
          this.authenticationService.deleteAuthentication(username);
       }
    }

    /**
     * Test for MNT-15509 Grant or/and Deny the same permission on multiple ancestors of a
     * file and check if the file has duplicate inherited permissions
     */
    public void testMultipleInheritedPermissions() throws Exception
    {
        // Create the site
        String siteName = GUID.generate();
        siteService.createSite("Testing", siteName, siteName, null, SiteVisibility.PUBLIC);

        // Ensure we have a doclib
        NodeRef siteContainer = siteService.createContainer(siteName, SiteService.DOCUMENT_LIBRARY, ContentModel.TYPE_FOLDER, null);

        // Create Folder1
        // Give USER_ONE COORDINATOR role to Folder1
        // Give USER_TWO CONTRIBUTOR role to Folder1
        // Deny USER_THREE CONSUMER role to Folder1
        NodeRef folder1 = fileFolderService.create(siteContainer, "Folder1", ContentModel.TYPE_FOLDER).getNodeRef();
        permissionService.setPermission(folder1, USER_ONE, PermissionService.COORDINATOR, true);
        permissionService.setPermission(folder1, USER_TWO, PermissionService.CONTRIBUTOR, true);
        permissionService.setPermission(folder1, USER_THREE, PermissionService.CONSUMER, false);
        permissionService.setInheritParentPermissions(folder1, true);

        // Create Folder2 in Folder1 
        // Give USER_ONE COORDINATOR role to Folder2
        // Deny USER_TWO CONTRIBUTOR role to Folder2
        // Give USER_THREE CONSUMER role to Folder2
        NodeRef folder2 = fileFolderService.create(folder1, "Folder2", ContentModel.TYPE_FOLDER).getNodeRef();
        permissionService.setPermission(folder2, USER_ONE, PermissionService.COORDINATOR, true);
        permissionService.setPermission(folder2, USER_TWO, PermissionService.CONTRIBUTOR, false);
        permissionService.setPermission(folder2, USER_THREE, PermissionService.CONSUMER, true);
        permissionService.setInheritParentPermissions(folder2, true);

        // Create Folder3 in Folder2
        // Give USER_ONE CONSUMER role to Folder3 twice
        NodeRef folder3 = fileFolderService.create(folder2, "Folder3", ContentModel.TYPE_FOLDER).getNodeRef();
        permissionService.setPermission(folder3, USER_ONE, PermissionService.CONSUMER, true);
        permissionService.setPermission(folder3, USER_ONE, PermissionService.CONSUMER, true);
        permissionService.setPermission(folder3, USER_ONE, PermissionService.COORDINATOR, true);
        permissionService.setInheritParentPermissions(folder3, true);

        // Get Folder3's inherited permissions
        Response response = sendRequest(
                new GetRequest(URL_DOCLIB_PERMISSIONS + "/" + StoreRef.STORE_REF_WORKSPACE_SPACESSTORE.getProtocol() + "/"
                        + StoreRef.STORE_REF_WORKSPACE_SPACESSTORE.getIdentifier() + "/" + folder3.getId()), Status.STATUS_OK);
        
        JSONObject jsonResponse = new JSONObject(response.getContentAsString());
        
        //Check if the request returns duplicate direct permissions
        HashSet<AccessPermission> directPermissions = new HashSet<>();
        JSONArray directPermissionsArray = jsonResponse.getJSONArray("direct");
        for (int i = 0; i < directPermissionsArray.length(); i++)
        {
            AccessPermission permission = new AccessPermission(directPermissionsArray.getJSONObject(i));
            
            assertTrue(directPermissions.add(permission));
        }
        
        //used to check allow/deny permission inheritance
        AccessPermission denyPermissionInheritedTest = null;
        AccessPermission allowPermissionInheritedTest = null;

        // Check if the request returns duplicate inherited permissions
        HashSet<AccessPermission> inheritedPermissions = new HashSet<>();
        JSONArray inheritedPermissionsArray = jsonResponse.getJSONArray("inherited");
        for (int i = 0; i < inheritedPermissionsArray.length(); i++)
        {
            AccessPermission permission = new AccessPermission(inheritedPermissionsArray.getJSONObject(i));
            if (USER_TWO.equals(permission.getAuthority().getName()) && PermissionService.CONTRIBUTOR.equals(permission.getRole()))
            {
                denyPermissionInheritedTest = permission;
            }
            if (USER_THREE.equals(permission.getAuthority().getName()) && PermissionService.CONSUMER.equals(permission.getRole()))
            {
                allowPermissionInheritedTest = permission;
            }
            assertTrue(inheritedPermissions.add(permission));
        }

        // Check if on folder3 USER_TWO inherits DENY for CONTRIBUTOR role from
        // folder 2 although on folder 1 was ALLOW
        assertNull(denyPermissionInheritedTest);
        
        // Check if on folder3 USER_THREE inherits ALLOW for CONSUMER role from
        // folder 2 although on folder 1 was DENY
        assertNotNull(allowPermissionInheritedTest);
    }
}

class AccessPermission
{
    private PermissionAuthority authority;
    private String role;

    public AccessPermission(JSONObject jsonPermission) throws JSONException
    {
        authority = new PermissionAuthority(jsonPermission.getJSONObject("authority"));
        role = jsonPermission.getString("role");
    }

    public PermissionAuthority getAuthority()
    {
        return this.authority;
    }

    public String getRole()
    {
        return this.role;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null || !(obj instanceof AccessPermission))
        {
            return false;
        }

        return (role.equals(((AccessPermission) obj).getRole()) && authority.equals(((AccessPermission) obj).getAuthority()));
    }

    @Override
    public int hashCode()
    {
        return authority.hashCode() + role.hashCode();
    }
}

class PermissionAuthority
{
    private String name;
    private String displayName;

    public PermissionAuthority(JSONObject jsonAuth) throws JSONException
    {
        name = jsonAuth.getString("name");
        displayName = jsonAuth.getString("displayName");
    }

    public String getName()
    {
        return this.name;
    }

    public String getDisplayName()
    {
        return this.displayName;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null || !(obj instanceof PermissionAuthority))
        {
            return false;
        }

        return (name.equals(((PermissionAuthority) obj).getName()) && displayName.equals(((PermissionAuthority) obj).getDisplayName()));
    }

    @Override
    public int hashCode()
    {
        return name.hashCode() + displayName.hashCode();
    }
}
